package edu.cuit.cloud_netdisk.controller;
import edu.cuit.cloud_netdisk.pojo.entity.File;
import edu.cuit.cloud_netdisk.result.Result;
import edu.cuit.cloud_netdisk.service.FileCollectService;
import edu.cuit.cloud_netdisk.service.FileShareService;
import edu.cuit.cloud_netdisk.service.Impl.FileServiceImpl;
import edu.cuit.cloud_netdisk.service.Impl.OssService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/files")
@Slf4j
@Api(tags = "OSS文件存储相关", description = "包含文件上传,删除,下载,分享等接口")
public class FileController {


    @Autowired
    private OssService ossService;
    @Autowired
    private FileServiceImpl fileService;

    @Autowired
    private FileCollectService fileCollectService;

    @Autowired
    private FileShareService fileShareService;

    /**
     * 文件上传接口
     */
    @ApiOperation(value = "文件上传", notes = "用户在选定文件夹中上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件实体", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "folderId", value = "文件夹的ID", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "userId", value = "上传用户的ID", required = true, dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "上传成功"),
            @ApiResponse(code = 400, message = "上传失败")
    })
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(value = "folderId", required = true) Integer folderId,
                                                       @RequestParam(value ="userId", required = true) Long userId) {

        if (file.isEmpty()) {
            log.info("用户上传空文件");
            return Result.error("请选择要上传的文件");
        }

        try {
            // 0.用户存储空间预存
            Long newUsedStorage = fileService.updateStorageSafely(userId, file.getSize());
            log.info("用户 {} 存储空间更新成功，当前已用空间: {} 字节", userId, newUsedStorage);
            
            // 1. 生成文件ID
            String fileId = UUID.randomUUID().toString();

            // 2. 获取原始文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));

            // 3. 构建OSS保存的文件名 (原名_ID.扩展名)
            String ossFileName = baseName + "_" + fileId + fileExtension;
            log.info("oss中的文件名是"+ossFileName);
            // 4. 上传到OSS (使用新文件名)
            String fileUrl = ossService.uploadFile(file, ossFileName);
            // 5. 生成文件实体
            File newFile = new File();
            newFile.setFileId(fileId);
            newFile.setFileName(originalFilename);
            newFile.setFileType(fileExtension);
            newFile.setFileSize(file.getSize());
            newFile.setStoragePath(fileUrl);
            newFile.setUploadUser(userId);
            newFile.setFolder(folderId);
            newFile.setUploadTime(LocalDateTime.now());
            log.info("展示文件信息: {}",newFile);

//            6. 数据库保存文件信息
            fileService.saveFile(newFile);

//            try{
//                fileService.saveFile(newFile);
//                return ResponseEntity.ok("数据库保存成功");
//            }catch (Exception e){
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("数据库保存失败: " + e.getMessage());
//            }

            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }


    /**
     * 文件删除接口
     */
    @ApiOperation(value = "文件删除", notes = "用户在选定文件进行删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileId", value = "文件ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "userId", value = "上传用户的ID", required = true, dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "删除成功"),
            @ApiResponse(code = 400, message = "删除失败"),
            @ApiResponse(code = 404, message = "文件不存在")
    })
    @DeleteMapping("/{fileId}")
    public Result<String> deleteFile(
            @PathVariable String fileId,
            @RequestParam(value ="userId", required = true) Long userId) {
        try {
            // 先获取文件大小
            long filesize = fileService.getFileSize(fileId);
            if (filesize == 0) {
                return Result.error("文件不存在或已被删除");
            }
            // 更新用户空间（减少已用空间）
            Long newUsedStorage = fileService.updateStorageSafely(userId, -filesize);
            log.info("用户 {} 删除文件后存储空间更新成功，当前已用空间: {} 字节", userId, newUsedStorage);
            
            // 数据库标记删除
            fileService.deleteFile(fileId, userId);

            // 删除所有用户的收藏记录
            fileCollectService.cancelAllCollectsByFileId(fileId);
            // 标记分享链接为失效
            fileShareService.markFileSharesAsExpiredByFileId(fileId);
            return Result.success("文件删除成功");
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
    @ApiOperation(value = "文件恢复", notes = "恢复已删除的文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileId", value = "文件ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "userId", value = "操作用户ID", required = true, dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "恢复成功"),
            @ApiResponse(code = 400, message = "恢复失败"),
            @ApiResponse(code = 404, message = "文件不存在")
    })
    @PostMapping("/restore")
    public Result<String> restoreFile(
            @RequestParam String fileId,
            @RequestParam Long userId) {

        try {
            // 先获取文件大小
            long filesize = fileService.getFileSize(fileId);
            if (filesize == 0) {
                return Result.error("文件不存在或无法恢复");
            }
            
            // 检查恢复后是否会超出存储限制
            Long newUsedStorage = fileService.updateStorageSafely(userId, filesize);
            log.info("用户 {} 恢复文件后存储空间更新成功，当前已用空间: {} 字节", userId, newUsedStorage);
            
            // 执行恢复操作
            fileService.recoverFile(fileId, userId);

            return Result.success("文件恢复成功");
        } catch (Exception e) {
            log.error("文件恢复失败", e);
            return Result.error("文件恢复失败: " + e.getMessage());
        }
    }
    /**
     * 文件更新接口
     */
    @ApiOperation(value = "文件更新", notes = "更新文件内容或元数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "新文件实体（可选）", required = false, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "fileId", value = "要更新的文件ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "folderId", value = "目标文件夹ID（可选）", required = false, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "userId", value = "操作用户ID", required = true, dataTypeClass = Long.class),
            @ApiImplicitParam(name = "newFileName", value = "新文件名（可选）", required = false, dataTypeClass = String.class)
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功"),
            @ApiResponse(code = 400, message = "更新失败"),
            @ApiResponse(code = 404, message = "文件不存在")
//            @ApiResponse(code = 403, message = "无权限操作")
    })
    @PutMapping("/update")
    public Result<String> updateFile(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("fileId") String fileId,
            @RequestParam(value = "folderId", required = false) Integer folderId,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "newFileName", required = false) String newFileName) {

        try {
            File existingFile = fileService.findbyId(fileId);
            log.info("原数据是: {}", existingFile);
            
            // 1. 如果需要更新文件内容
            if (file != null && !file.isEmpty()) {
                // 先检查空间变化
                long oldFileSize = fileService.getFileSize(fileId);
                long sizeDelta = file.getSize() - oldFileSize;
                Long newUsedStorage = fileService.updateStorageSafely(userId, sizeDelta);
                log.info("用户 {} 更新文件后存储空间更新成功，当前已用空间: {} 字节", userId, newUsedStorage);
                
                // oss删除旧文件
                ossService.deleteFile(existingFile.getStoragePath());
                // oss上传新文件（保持原命名规则）

                String originalFilename = file.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                String ossFileName = baseName + "_" + fileId + fileExtension;

                String newFileUrl = ossService.uploadFile(file, ossFileName);
                existingFile.setStoragePath(newFileUrl);
                existingFile.setFileSize(file.getSize());
                existingFile.setFileType(fileExtension);

                // 如果未提供新文件名，则使用上传文件的原始名
                if (newFileName == null) {
                    existingFile.setFileName(originalFilename);
                }
            }

            // 2. 更新元数据
            if (newFileName != null) {
                existingFile.setFileName(newFileName);
            }

            if (folderId != null) {
                existingFile.setFolder(folderId);
            }

            existingFile.setUploadTime(LocalDateTime.now());
            existingFile.setUploadUser(userId);

            // 4. 保存到数据库
            fileService.updateFile(existingFile);
            log.info("更新后数据是: {}",existingFile);
            return Result.success("文件更新成功");
        } catch (Exception e) {
            log.error("文件更新失败", e);
            return Result.error("文件更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件夹下的文件列表
     */
    @ApiOperation(value = "获取文件列表", notes = "获取指定文件夹下的所有文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "folderId", value = "文件夹ID", required = true, dataTypeClass = Integer.class)
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功"),
            @ApiResponse(code = 400, message = "获取失败")
    })
    @GetMapping("/files")
    public Result<List<File>> getFiles(@RequestParam(required = true) Integer folderId) {
        try {
            List<File> files = fileService.getFilesByFolderId(folderId);
            return Result.success(files);
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return Result.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 重命名文件
     */
    @PutMapping("/{fileId}/rename")
    public Result<String> renameFile(
            @PathVariable String fileId,
            @RequestParam String newName,
            @RequestParam Long userId) {
        try {
            // 获取原文件信息
            File file = fileService.findbyId(fileId);
            if (file == null) {
                return Result.error("文件不存在");
            }

            // 更新文件名
            file.setFileName(newName);
            file.setUploadUser(userId);
            file.setUploadTime(LocalDateTime.now());

            // 保存更新
            fileService.updateFile(file);
            return Result.success("重命名成功");
        } catch (Exception e) {
            log.error("文件重命名失败", e);
            return Result.error("重命名失败: " + e.getMessage());
        }
    }
    /**
     * 下载文件的url
     */
    @ApiOperation(value = "文件下载", notes = "根据返回url下载文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "folderId", value = "文件夹ID", required = true, dataTypeClass = Integer.class)
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "下载成功"),
            @ApiResponse(code = 400, message = "下载失败")
    })
    @GetMapping("/down")
    public Result<String>  shareFiles(@RequestParam(required = true) String fileId) {
        String key = fileService.getFilePathById(fileId);
        String url =ossService.FileShare(key);
        log.info(url);
        return Result.success(url);
    }

    /**
     * 文件预览接口
     */
    @ApiOperation(value = "文件预览", notes = "根据文件ID预览文件（重定向到真实存储地址）")
    @GetMapping("/{fileId}/preview")
    public void previewFile(@PathVariable String fileId, HttpServletResponse response) throws IOException {
        File file = fileService.findbyId(fileId);
        if (file == null || Boolean.TRUE.equals(file.getIsDeleted())) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("文件不存在或已被删除");
            return;
        }
        String fileUrl = file.getStoragePath();
        response.sendRedirect(fileUrl);
    }

    @ApiOperation(value = "获取文件预览信息", notes = "根据文件ID获取预览所需信息")
    @GetMapping("/{fileId}/preview-info")
    public Result<Map<String, Object>> getPreviewInfo(@PathVariable String fileId) {
        try {
            // 获取文件信息
            File file = fileService.findbyId(fileId);
            if (file == null) {
                return Result.error("文件不存在");
            }
            
            // 获取文件类型
            String fileType = file.getFileType().toLowerCase();
            String fileName = file.getFileName();
            String fileUrl = file.getStoragePath();
            
            // 预览信息
            Map<String, Object> previewInfo = new HashMap<>();
            previewInfo.put("fileId", fileId);
            previewInfo.put("fileName", fileName);
            previewInfo.put("fileType", fileType);
            
            // 对于媒体文件(音频)使用签名URL，确保能正确播放
            if (fileType.endsWith(".mp3") || fileType.endsWith(".wav") || 
                fileType.endsWith(".ogg") || fileType.endsWith(".flac") || fileType.endsWith(".aac")) {
                // 获取签名URL
                String signedUrl = ossService.FileShare(fileUrl);
                previewInfo.put("fileUrl", signedUrl);
            } 
            // 视频文件使用专门的视频播放URL
            else if (fileType.endsWith(".mp4") || fileType.endsWith(".webm") || fileType.endsWith(".mov") || 
                     fileType.endsWith(".avi") || fileType.endsWith(".mkv")) {
                // 直接使用下载URL，而不是尝试修改content-type
                String signedUrl = ossService.FileShare(fileUrl);
                previewInfo.put("fileUrl", signedUrl);
                // 添加一个标记，表示这是视频文件
                previewInfo.put("isVideo", true);
            }
            else {
                previewInfo.put("fileUrl", fileUrl);
            }
            
            // 对于office文档，可能需要转换或使用外部服务
            if (fileType.endsWith(".docx") || fileType.endsWith(".doc") || 
                fileType.endsWith(".xlsx") || fileType.endsWith(".xls") || 
                fileType.endsWith(".pptx") || fileType.endsWith(".ppt")) {
                // 使用Microsoft Office Online Viewer
                String encodedUrl = URLEncoder.encode(fileUrl, "UTF-8");
                String previewUrl = "https://view.officeapps.live.com/op/view.aspx?src=" + encodedUrl;
                previewInfo.put("previewUrl", previewUrl);
            }
            
            return Result.success(previewInfo);
        } catch (Exception e) {
            log.error("获取文件预览信息失败", e);
            return Result.error("获取文件预览信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取视频流URL
     * 针对视频文件进行特殊处理
     */
    private String getVideoStreamUrl(String fileUrl) {
        try {
            // 使用视频播放优化的参数生成签名URL
            String signedUrl = ossService.getVideoStreamUrl(fileUrl);
            log.info("生成视频流URL: {}", signedUrl);
            return signedUrl;
        } catch (Exception e) {
            log.error("生成视频流URL失败", e);
            return fileUrl; // 失败时返回原始URL
        }
    }

    /**
     * 直接下载文件（不进行格式转换）
     */
    @ApiOperation(value = "直接下载文件", notes = "返回原始文件而非签名URL，适用于某些浏览器无法预览的格式")
    @GetMapping("/{fileId}/raw")
    public void downloadRawFile(@PathVariable String fileId, HttpServletResponse response) {
        try {
            // 获取文件信息
            File file = fileService.findbyId(fileId);
            if (file == null || Boolean.TRUE.equals(file.getIsDeleted())) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在或已被删除");
                return;
            }

            String fileUrl = file.getStoragePath();
            String fileName = file.getFileName();
            
            // 从OSS获取文件
            java.io.File tempFile = downloadFileFromOss(fileUrl);
            if (tempFile == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("文件下载失败");
                return;
            }
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                    URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLengthLong(tempFile.length());
            
            // 将文件写入响应
            try (java.io.FileInputStream fis = new java.io.FileInputStream(tempFile);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            
            // 删除临时文件
            tempFile.delete();
            
        } catch (Exception e) {
            log.error("文件下载失败", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("文件下载失败: " + e.getMessage());
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
    
    /**
     * 从OSS下载文件到临时目录
     */
    private java.io.File downloadFileFromOss(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 创建临时文件
            java.io.File tempFile = java.io.File.createTempFile("download", ".tmp");
            
            // 下载文件
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            return tempFile;
        } catch (Exception e) {
            log.error("从OSS下载文件失败", e);
            return null;
        }
    }
}
