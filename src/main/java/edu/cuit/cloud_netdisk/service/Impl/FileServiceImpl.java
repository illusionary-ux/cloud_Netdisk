package edu.cuit.cloud_netdisk.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cuit.cloud_netdisk.constant.MessageConstant;
import edu.cuit.cloud_netdisk.dao.FileMapper;
import edu.cuit.cloud_netdisk.dao.FolderMapper;
import edu.cuit.cloud_netdisk.dao.RecycleBinMapper;
import edu.cuit.cloud_netdisk.dao.UserMapper;
import edu.cuit.cloud_netdisk.exception.FileNotFoundException;
import edu.cuit.cloud_netdisk.exception.FileUploadException;
import edu.cuit.cloud_netdisk.exception.StorageExceededException;
import edu.cuit.cloud_netdisk.pojo.entity.File;
import edu.cuit.cloud_netdisk.pojo.entity.Folder;
import edu.cuit.cloud_netdisk.pojo.entity.User;
import edu.cuit.cloud_netdisk.pojo.vo.FileInfo;
import edu.cuit.cloud_netdisk.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transactional
@Service(value = "fileService")
public class FileServiceImpl extends ServiceImpl<FileMapper, FileInfo> implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private RecycleBinMapper recycleBinMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Lazy
    private FileServiceImpl fileServiceImpl;


    /**
     *   文件保存服务
     * */

    @Override
    @Transactional
    public void saveFile(File file) {
        // 验证文件信息
        validateFileBeforeSave(file);

        // 保存文件信息
        fileMapper.save(file);

        // 更新父文件夹大小
        updateFolderSize(file.getFolder().longValue(), file.getFileSize());

        log.info("[添加文件] 用户ID:{}，新建文件ID:{}，文件大小:{} 字节", file.getUploadUser(), file.getFileId(), file.getFileSize());
    }

    /**
     * 递归更新文件夹大小
     * @param folderId 文件夹ID
     * @param sizeDelta 大小变化值
     */
    private void updateFolderSize(Long folderId, Long sizeDelta) {
        while (folderId != null && folderId > 0) {
            // 更新当前文件夹大小
            folderMapper.updateFolderSize(folderId, sizeDelta);
            
            // 获取父文件夹ID
            Folder folder = folderMapper.selectByFolderId(folderId);
            if (folder == null || folder.getParentFolderId() == null || folder.getParentFolderId() == 0) {
                break;
            }
            folderId = folder.getParentFolderId();
        }
    }

    /**
     *   数据库文件更新服务
     * */
    @Override
    @Transactional
    public void updateFile(File file) {
        File oldFile = fileMapper.findById(file.getFileId());
        if (oldFile == null) {
            throw new FileNotFoundException(MessageConstant.FILE_NOT_FOUND);
        }
        // 文件夹变更
        if (!oldFile.getFolder().equals(file.getFolder())) {
            updateFolderSizeOnly(oldFile.getFolder().longValue(), -oldFile.getFileSize());
            updateFolderSizeOnly(file.getFolder().longValue(), file.getFileSize());
        } else if (!oldFile.getFileSize().equals(file.getFileSize())) {
            Long sizeDelta = file.getFileSize() - oldFile.getFileSize();
            updateFolderSizeOnly(oldFile.getFolder().longValue(), sizeDelta);
        }
        fileMapper.update(file);
    }
    /**
     *   数据库文件删除服务
     * */
    @Override
    @Transactional
    public Long deleteFile(String fileId, Long userId) {
        File file = fileMapper.findById(fileId);
        if (file == null) {
            throw new FileNotFoundException(MessageConstant.FILE_NOT_FOUND);
        }
        fileMapper.deleteById(fileId, userId);
        updateFolderSize(file.getFolder().longValue(), -file.getFileSize());
        recycleBinMapper.insertRecycleBinItem(
            fileId,
            1,
            LocalDateTime.now(),
            userId,
            file.getFolder()
        );
        log.info("[删除文件] 用户ID:{}，文件ID:{}，减少存储空间:{} 字节", userId, fileId, file.getFileSize());
        return file.getFileSize();
    }
    /**
     *   数据库文件恢复服务
     * */
    @Override
    @Transactional
    public File recoverFile(String fileId,Long userId) {
        fileMapper.recoverById(fileId,userId);
        File file = fileMapper.findById(fileId);
        log.info("[恢复文件] 用户ID:{}，文件ID:{}，增加存储空间:{} 字节", userId, fileId, file != null ? file.getFileSize() : null);
        return file;
    }
    /**
     *   数据库文件合法性验证服务
     * */
    private void validateFileBeforeSave(File file) {
        if (file.getFileName() == null || file.getFileName().isEmpty()) {
            throw new FileUploadException(MessageConstant.FILE_NAME_EMPTY);
        }
        if (file.getFileType() == null || file.getFileType().isEmpty()) {
            throw new FileUploadException(MessageConstant.FILE_TYPE_EMPTY);
        }
        if (file.getStoragePath() == null || file.getStoragePath().isEmpty()) {
            throw new FileUploadException(MessageConstant.FILE_STORAGE_PATH_EMPTY);
        }
        if (file.getUploadUser() == null) {
            throw new FileUploadException(MessageConstant.FILE_UPLOAD_USER_EMPTY);
        }
    }

    /**
     *   数据库文件根据文件ID查询服务
     * */
    public File findbyId(String fileId){
        return fileMapper.findById(fileId);
    }


    @Override
    public List<File> getFilesByFolderId(Integer folderId) {
        return fileMapper.findByFolderId(folderId);
    }
    /**
     *   数据库文件根据文件ID查询文件路径path服务
     * */
    @Override
    public String getFilePathById(String fileId) {
        String path = fileMapper.quickSearchByFileId(fileId);
        if (path == null) {
            throw new RuntimeException("文件不存在或已被删除");
        }
        return path;
    }
    /**
     * 安全更新用户存储空间
     * @param userId 用户ID
     * @param deltaStorage 变化量（正数增加，负数减少）
     * @return 更新后的已用存储空间
     * @throws StorageExceededException 如果超出限额
     */
    @Transactional
    public Long updateStorageSafely(Long userId, Long deltaStorage) {
        log.info("开始更新用户存储空间 - userId: {}, deltaStorage: {}", userId, deltaStorage);
        
        // 1. 获取用户的已使用空间大小和限制大小
        Long currentUsedStorage = fileMapper.getUpdatedUsedStorage(userId);
        User user = userMapper.getById(userId);
        Long userLimit = user.getStorageLimit();
        log.info("当前存储信息 - 已用空间: {}, 限制空间: {}", currentUsedStorage, userLimit);
        
        // 2. 根据使用空间 + 文件大小 < 限制的条件判断是否超过限制
        Long expectedUsedStorage = currentUsedStorage + deltaStorage;
        log.info("预期更新后存储空间: {}", expectedUsedStorage);
        
        if (expectedUsedStorage < 0) {
            log.error("存储空间更新失败 - 预期结果小于0: {}", expectedUsedStorage);
            throw new StorageExceededException("存储空间不能为负数");
        }
        
        if (expectedUsedStorage > userLimit) {
            log.error("存储空间更新失败 - 超出限制: 预期{} > 限制{}", expectedUsedStorage, userLimit);
            throw new StorageExceededException("存储空间不足，无法完成操作");
        }
        
        // 3. 可以更新就更新用户已使用大小
        int updatedRows = userMapper.updateUsedStorage(userId, deltaStorage);
        log.info("更新结果 - updatedRows: {}", updatedRows);
        
        if (updatedRows == 0) {
            log.error("存储空间更新失败 - 数据库更新返回0行");
            throw new StorageExceededException("存储空间更新失败");
        }
        
        // 返回更新后的值
        Long afterUsedStorage = fileMapper.getUpdatedUsedStorage(userId);
        log.info("存储空间更新成功 - userId: {}, 更新前: {}, 变化量: {}, 更新后: {}", 
                userId, currentUsedStorage, deltaStorage, afterUsedStorage);
        return afterUsedStorage;
    }
    /**
     * 统计OSS路径在file表中的出现次数
     * @param storagePath OSS存储路径
     * @return 出现次数
     */
    @Override
    public int countByStoragePath(String storagePath) {
        return fileMapper.countByStoragePath(storagePath);
    }
    
    /**
     * 根据文件ID查询文件大小
     * @param fileId 文件ID
     * @return 文件大小（字节数），如果文件不存在返回null
     */
    public Long getFileSize(String fileId){
       return fileMapper.getFileSize(fileId);
    }
    
    /**
     * 递归统计文件夹及其所有子文件夹和文件的总大小
     */
    private Long getFolderTotalSize(Long folderId) {
        Long totalSize = 0L;
        // 统计当前文件夹下所有文件大小
        List<File> files = fileMapper.findByFolderId(folderId.intValue());
        if (files != null) {
            for (File file : files) {
                totalSize += file.getFileSize();
            }
        }
        // 统计所有子文件夹
        List<Folder> subFolders = folderMapper.selectByParentId(folderId);
        if (subFolders != null) {
            for (Folder sub : subFolders) {
                totalSize += getFolderTotalSize(sub.getFolderId());
            }
        }
        return totalSize;
    }

    /**
     * 只对当前文件夹做加减
     */
    private void updateFolderSizeOnly(Long folderId, Long sizeDelta) {
        folderMapper.updateFolderSize(folderId, sizeDelta);
    }

}