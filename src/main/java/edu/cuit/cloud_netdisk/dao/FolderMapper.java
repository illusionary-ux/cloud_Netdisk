package edu.cuit.cloud_netdisk.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.cuit.cloud_netdisk.pojo.entity.Folder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FolderMapper extends BaseMapper<Folder> {
    /**
     * 根据父文件夹ID查询文件夹列表
     */
    List<Folder> selectByParentId(@Param("parentFolderId") Long parentFolderId);

    /**
     * 根据文件夹ID查询文件夹信息
     */
    Folder selectByFolderId(@Param("folderId") Long folderId);

    /**
     * 检查文件夹名称是否已存在
     */
    Integer checkFolderNameExist(@Param("parentFolderId") Long parentFolderId, @Param("folderName") String folderName);

    /**
     * 软删除文件夹
     */
    Integer softDeleteFolder(@Param("folderId") Long folderId);

    /**
     * 软删除文件夹（递归）
     */
    Integer softDeleteFolderRecursive(@Param("folderId") Long folderId);

    /**
     * 更新文件夹大小
     * @param folderId 文件夹ID
     * @param sizeDelta 大小变化值（正数表示增加，负数表示减少）
     */
    Integer updateFolderSize(@Param("folderId") Long folderId, @Param("sizeDelta") Long sizeDelta);

    /**
     * 查询文件夹路径
     * 使用递归查询获取从根目录到当前文件夹的完整路径
     *
     * @param folderId 当前文件夹ID
     * @return 文件夹路径列表（从根目录到当前文件夹）
     */
    List<Folder> selectFolderPath(@Param("folderId") Long folderId);

    List<Long> selectAllFolderIdsRecursive(@Param("folderId") Long folderId);

    Integer restoreFolder(@Param("folderId") Long folderId);

    /**
     * 根据文件夹ID查询文件夹大小
     */
    Long selectFolderSizeById(@Param("folderId") Long folderId);
}