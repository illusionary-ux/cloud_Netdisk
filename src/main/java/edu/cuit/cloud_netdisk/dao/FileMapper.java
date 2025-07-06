package edu.cuit.cloud_netdisk.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.cuit.cloud_netdisk.pojo.entity.File;
import edu.cuit.cloud_netdisk.pojo.vo.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文件数据访问层接口
 * 负责文件的增删改查等数据库操作
 */
@Repository
@Mapper
public interface FileMapper extends BaseMapper<FileInfo> {
    /**
     * 保存文件信息到数据库
     * @param file 文件实体对象
     * @return 影响的行数
     */
    int save(File file);

    /**
     * 更新文件信息
     * @param file 文件实体对象
     * @return 影响的行数
     */
    int update(File file);

    /**
     * 根据文件ID和用户ID删除文件（软删除）
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 影响的行数
     */
    int deleteById(@Param("fileId") String fileId,@Param("userId") Long userId);

    /**
     * 根据文件ID和用户ID恢复已删除的文件
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 恢复的文件对象
     */
    int recoverById(@Param("fileId") String fileId,@Param("userId") Long userId);

    /**
     * 根据文件ID查询文件信息
     * @param fileId 文件ID
     * @return 文件对象
     */
    File findById(@Param("fileId") String fileId);

    /**
     * 检查文件是否存在
     * @param fileId 文件ID
     * @return 是否存在
     */
    boolean existsById(@Param("fileId") String fileId);


    /**
     * 根据文件夹ID查询文件列表
     * @param folderId 文件夹ID
     * @return 文件列表
     */
    List<File> findByFolderId(Integer folderId);

    /**
     * 根据文件ID快速查询文件存储路径
     * @param fileId 文件ID
     * @return 文件存储路径（如果文件不存在或已删除，返回 null）
     */
    String quickSearchByFileId(@Param("fileId") String fileId);
    /**
     * 安全增量更新用户已用存储空间（带限额检查）
     * @param userId 用户ID
     * @param deltaStorage 要增加的存储值（正数增加，负数减少）
     * @return 更新后的 used_storage 值（如果更新失败返回 null）
     */
    Long safeIncrementUsedStorage(
            @Param("userId") Long userId,
            @Param("deltaStorage") Long deltaStorage
    );
    /**
     * 获取用户更新后的已用存储空间
     * @param userId 用户ID
     * @return 更新后的已用存储空间
     */
    Long getUpdatedUsedStorage(@Param("userId") Long userId);
    /**
     * 根据文件ID查询文件大小
     * @param fileId 文件ID
     * @return 文件大小（字节数），如果文件不存在返回null
     */
    Long getFileSize(@Param("fileId") String fileId);

    /**
     * 根据文件ID查询文件大小（包括已删除的文件）
     * @param fileId 文件ID
     * @param includeDeleted 是否包含已删除的文件
     * @return 文件大小（字节数），如果文件不存在返回null
     */
    Long getFileSizeWithDeleted(@Param("fileId") String fileId, @Param("includeDeleted") Boolean includeDeleted);

    /**
     * 统计OSS路径在file表中的出现次数
     * @param storagePath OSS存储路径
     * @return 出现次数
     */
    int countByStoragePath(@Param("storagePath") String storagePath);

    /**
     * 根据文件ID查询文件路径（包含已删除文件）
     * @param fileId 文件ID
     * @return 文件路径
     */
    String getFilePathByIdIncludeDeleted(@Param("fileId") String fileId);

    /**
     * 查询某文件夹下所有文件的路径（包含已删除文件）
     * @param folderId 文件夹ID
     * @return 文件路径列表
     */
    List<String> getAllFilePathsByFolderIdIncludeDeleted(@Param("folderId") String folderId);

    /**
     * 查询某文件夹及其所有子文件夹下所有文件的路径（包含已删除文件）
     * @param folderId 文件夹ID
     * @return 文件路径列表
     */
    List<String> getAllFilePathsByFolderIdRecursive(@Param("folderId") String folderId);

    Integer softDeleteFilesByFolderRecursive(@Param("folderId") Long folderId);

    List<String> selectAllFileIdsByFolderRecursive(@Param("folderId") Long folderId);

    /**
     * 根据文件夹ID查询文件列表（包含被删除的文件）
     * @param folderId 文件夹ID
     * @return 文件列表
     */
    List<File> findByFolderIdIncludeDeleted(Integer folderId);
}