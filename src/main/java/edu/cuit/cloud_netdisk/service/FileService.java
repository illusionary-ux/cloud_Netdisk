package edu.cuit.cloud_netdisk.service;


import com.baomidou.mybatisplus.extension.service.IService;
import edu.cuit.cloud_netdisk.exception.StorageExceededException;
import edu.cuit.cloud_netdisk.pojo.entity.File;
import edu.cuit.cloud_netdisk.pojo.vo.FileInfo;

import java.util.List;

public interface FileService extends IService<FileInfo> {
    /**
     * 将文件信息保存到数据库
     * @param file 已实例化的文件对象
     * @return 保存后的文件实体(包含可能生成的ID等)
     */
    void saveFile(File file);

    /**
     * 更新文件信息
     * @param file 要更新的文件对象
     * @return 更新后的文件实体
     */
    void updateFile(File file);

    /**
     * 根据文件ID删除文件记录
     * @param fileId 文件ID
     */
    Long deleteFile(String fileId,Long userId);
    /**
     * 根据文件ID恢复文件记录
     * @param fileId 文件ID
     */
    File recoverFile(String fileId,Long userId);


    File findbyId(String fileId);

    /**
     * 获取指定文件夹下的所有文件
     * @param folderId 文件夹ID
     * @return 文件列表
     */
    List<File> getFilesByFolderId(Integer folderId);

    /**
     * 根据文件ID快速获取文件存储路径
     * @param fileId 文件ID
     * @return 文件存储路径，如果文件不存在或已删除则返回null
     */
    String getFilePathById(String fileId);
    /**
     * 安全更新用户存储空间
     * @param userId 用户ID
     * @param deltaStorage 变化量（正数增加，负数减少）
     * @return 更新后的已用存储空间
     * @throws StorageExceededException 如果超出限额
     */
    Long updateStorageSafely(Long userId, Long deltaStorage);
    /**
     * 统计OSS路径在file表中的出现次数
     * @param storagePath OSS存储路径
     * @return 出现次数
     */
    int countByStoragePath(String storagePath);
    /**
     * 根据文件ID查询文件大小
     * @param fileId 文件ID
     * @return 文件大小（字节数），如果文件不存在返回null
     */
    Long getFileSize(String fileId);
}