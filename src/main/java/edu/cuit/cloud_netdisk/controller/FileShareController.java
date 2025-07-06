package edu.cuit.cloud_netdisk.controller;

import edu.cuit.cloud_netdisk.pojo.entity.File;
import edu.cuit.cloud_netdisk.pojo.entity.FileShare;
import edu.cuit.cloud_netdisk.pojo.vo.PageResultVO;
import edu.cuit.cloud_netdisk.result.Result;
import edu.cuit.cloud_netdisk.service.FileShareService;
import edu.cuit.cloud_netdisk.service.Impl.FileServiceImpl;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/fileshare")
public class FileShareController {
    @Autowired
    private FileShareService fileShareService;

    @Autowired
    private FileServiceImpl fileService;

    @ApiOperation(value = "添加文件分享", notes = "根据文件id和用户id生成唯一urlid并添加分享记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileId", value = "文件ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true, dataTypeClass = Long.class),
            @ApiImplicitParam(name = "password", value = "提取码", dataTypeClass = String.class)
    })
    @ApiResponses({
            @ApiResponse(code = 1, message = "分享添加成功"),
            @ApiResponse(code = 400, message = "分享添加失败")
    })
    @PostMapping("/add")
    public Result<String> addFileShare(@RequestParam @NotBlank String fileId, @RequestParam Long userId, @RequestParam(required = false) String password) {
        try {
            // 检查该文件是否已有有效的分享链接
            int validShareCount = fileShareService.countValidSharesByFileId(fileId);
            if (validShareCount > 0) {
                return Result.error("该文件已有有效的分享链接，请先删除现有链接再创建新链接");
            }
            
            // 生成urlid（MD5(fileId+userId+时间戳)）
            String urlid = md5(fileId + userId + System.currentTimeMillis());
            FileShare fileShare = new FileShare();
            fileShare.setUrlid(urlid);
            fileShare.setFileid(fileId);
            fileShare.setCreatorId(userId);
            fileShare.setCreateTime(LocalDateTime.now());
            fileShare.setShareCount(0);
            fileShare.setIsExpired(false);
            fileShare.setFilename(fileService.findbyId(fileId).getFileName());
            fileShare.setPassword(password == null ? "" : password);
            fileShareService.insertFileShare(fileShare);
            String link = "http://localhost:8080/fileshare/acceptShare?urlid=" + urlid;
            return Result.success(link);
        } catch (Exception e) {
            return Result.error("分享添加失败: " + e.getMessage());
        }
    }

    private String md5(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(str.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    @DeleteMapping("/delete/{urlid}")
    public Result<Void> deleteFileShare(@PathVariable String urlid) {
        fileShareService.deleteFileShare(urlid);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateFileShare(@RequestBody FileShare fileShare) {
        fileShareService.updateFileShare(fileShare);
        return Result.success();
    }

    @GetMapping("/get/{urlid}")
    public Result<FileShare> getFileShare(@PathVariable String urlid) {
        FileShare fileShare = fileShareService.selectFileShareById(urlid);
        return Result.success(fileShare);
    }

    @GetMapping("/list")
    public Result<List<FileShare>> listFileShares() {
        List<FileShare> list = fileShareService.selectAllFileShares();
        return Result.success(list);
    }

    @GetMapping("/listFileShares")
    public Result<PageResultVO<FileShare>> listFileShares(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResultVO<FileShare> pageResult = fileShareService.getFileSharesByUserId(userId, pageNum, pageSize);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("获取分享列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/acceptShare")
    public Result<String> acceptShare(@RequestParam Long userId,@RequestParam Integer folderId, @RequestParam String urlid, @RequestParam(required = false) String password) {
        try {
            log.info("[接受分享] userId={}, folderId={}, urlid={}, password={}", userId, folderId, urlid, password);
            FileShare fileShare = fileShareService.selectFileShareById(urlid);
            if (fileShare == null) {
                log.warn("[接受分享] 分享链接不存在: urlid={}", urlid);
                return Result.error("分享链接不存在");
            }
            if (fileShare.getIsExpired() != null && fileShare.getIsExpired()) {
                log.warn("[接受分享] 分享链接已失效: urlid={}", urlid);
                return Result.error("分享链接已失效");
            }
            if (fileShare.getPassword() != null && !fileShare.getPassword().isEmpty()) {
                if (password == null || !fileShare.getPassword().equals(password)) {
                    log.warn("[接受分享] 提取码错误: urlid={}", urlid);
                    return Result.error("提取码错误");
                }
            }

            // 更新分享次数
            fileShare.setShareCount(fileShare.getShareCount() == null ? 1 : fileShare.getShareCount() + 1);
            fileShareService.updateFileShare(fileShare);
            File oldfile = fileService.findbyId(fileShare.getFileid());
//            更新使用空间
            Long newUsedStorage = fileService.updateStorageSafely(userId, oldfile.getFileSize());
            log.info("用户 {} 存储空间更新成功，当前已用空间: {} 字节", userId, newUsedStorage);
//


            File newFile = new File();
            newFile.setFileId(UUID.randomUUID().toString());
            newFile.setFileName(oldfile.getFileName());
            newFile.setFileType(oldfile.getFileType());
            newFile.setFileSize(oldfile.getFileSize());
            newFile.setStoragePath(oldfile.getStoragePath());
            newFile.setUploadUser(userId);
            newFile.setFolder(folderId);
            newFile.setUploadTime(LocalDateTime.now());
            fileService.saveFile(newFile);
            log.info("[接受分享] 新文件已保存: {}", newFile);
            return Result.success("分享接受成功");
        } catch (Exception e) {
            log.error("[接受分享] 异常: ", e);
            return Result.error("接受分享失败: " + e.getMessage());
        }
    }

    /**
     * 通过文件ID查找分享信息
     * @param fileid 文件ID
     * @return 分享信息列表
     */
    @GetMapping("/byFileId/{fileid}")
    public Result<List<FileShare>> getFileSharesByFileId(@PathVariable String fileid) {
        List<FileShare> list = fileShareService.selectFileSharesByFileId(fileid);
        return Result.success(list);
    }

    /**
     * 标记分享链接为失效
     * @param urlid 分享链接ID
     * @return 操作结果
     */
    @PutMapping("/markExpired/{urlid}")
    public Result<Void> markFileShareAsExpired(@PathVariable String urlid) {
        try {
            FileShare fileShare = fileShareService.selectFileShareById(urlid);
            if (fileShare == null) {
                return Result.error("分享链接不存在");
            }
            fileShare.setIsExpired(true);
            fileShareService.updateFileShare(fileShare);
            return Result.success();
        } catch (Exception e) {
            return Result.error("标记失效失败: " + e.getMessage());
        }
    }
} 