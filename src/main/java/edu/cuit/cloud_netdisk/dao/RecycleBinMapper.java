package edu.cuit.cloud_netdisk.dao;

import edu.cuit.cloud_netdisk.pojo.vo.RecycleBinItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RecycleBinMapper {
    /**
     * 查询回收站列表
     */
    List<RecycleBinItemVO> selectRecycleBinItems();

    /**
     * 根据用户ID查询回收站列表
     * @param userId 用户ID
     * @return 用户的回收站项目列表
     */
    List<RecycleBinItemVO> selectRecycleBinItemsByUserId(@Param("userId") Long userId);

    /**
     * 根据ID查询回收站项目
     */
    RecycleBinItemVO selectById(@Param("id") Long id);

    /**
     * 恢复文件
     */
    void restoreFile(@Param("fileId") String fileId);

    /**
     * 恢复文件夹
     */
    void restoreFolder(@Param("folderId") String folderId);

    /**
     * 物理删除文件
     */
    void deleteFile(@Param("fileId") String fileId);

    /**
     * 物理删除文件夹
     */
    void deleteFolder(@Param("folderId") String folderId);

    /**
     * 删除回收站记录
     */
    void deleteById(@Param("id") Long id);

    /**
     * 清空回收站
     */
    void deleteAll();

    /**
     * 添加项目到回收站
     */
    void insertRecycleBinItem(
        @Param("itemId") String itemId,
        @Param("itemType") Integer itemType,
        @Param("deleteTime") LocalDateTime deleteTime,
        @Param("deleteUser") Long deleteUser,
        @Param("originalFolderId") Integer originalFolderId
    );

    /**
     * 递归删除文件夹及其所有子文件夹和文件
     * @param folderId 文件夹ID
     */
    void deleteFolderAndFilesRecursive(@Param("folderId") String folderId);

    void deleteFilesByFolderRecursive(@Param("folderId") String folderId);
    void deleteFoldersRecursive(@Param("folderId") String folderId);

    /**
     * 根据文件夹ID和用户ID查询回收站记录
     */
    Long selectByFolderId(@Param("folderId") Long folderId, @Param("userId") Long userId);

    /**
     * 根据文件ID和用户ID查询回收站记录
     */
    Long selectByFileId(@Param("fileId") String fileId, @Param("userId") Long userId);

    /**
     * 根据item_id和item_type删除回收站记录
     */
    void deleteByItemIdAndType(@Param("itemId") String itemId, @Param("itemType") Integer itemType);

    /**
     * 根据original_folder_id查找所有在回收站中的文件记录ID
     */
    List<Long> selectFileRecycleIdsByFolderId(@Param("folderId") String folderId);
} 