package edu.cuit.cloud_netdisk.service;

import edu.cuit.cloud_netdisk.pojo.entity.Folder;
import edu.cuit.cloud_netdisk.pojo.dto.FolderDTO;
import edu.cuit.cloud_netdisk.pojo.dto.FolderRenameDTO;
import edu.cuit.cloud_netdisk.pojo.vo.FolderVO;
import java.util.List;

/**
 * 文件夹服务接口
 */
public interface FolderService {
    /**
     * 创建文件夹
     *
     * @param folderDTO 文件夹信息
     * @return 创建的文件夹信息
     */
    FolderVO createFolder(FolderDTO folderDTO);

    /**
     * 重命名文件夹
     *
     * @param folderId 文件夹ID
     * @param renameDTO 重命名信息
     * @return 更新后的文件夹信息
     */
    FolderVO renameFolder(Long folderId, FolderRenameDTO renameDTO);

    /**
     * 删除文件夹
     *
     * @param folderId 文件夹ID
     */
    void deleteFolder(Long folderId);

    /**
     * 获取文件夹列表
     *
     * @param parentFolderId 父文件夹ID
     * @return 文件夹列表
     */
    List<FolderVO> getFolderList(Long parentFolderId);

    /**
     * 获取文件夹路径
     *
     * @param folderId 文件夹ID
     * @return 从根目录到当前文件夹的路径列表
     */
    List<FolderVO> getFolderPath(Long folderId);

    /**
     * 检查文件夹名称是否已存在
     *
     * @param parentFolderId 父文件夹ID
     * @param folderName 文件夹名称
     * @return 是否存在
     */
    boolean checkFolderNameExist(Long parentFolderId, String folderName);

    /**
     * 批量恢复指定文件夹及其所有内容
     */
    void restoreFolderAndChildren(Long folderId);

    /**
     * 根据文件夹ID获取文件夹大小
     * @param folderId 文件夹ID
     * @return 文件夹大小（字节）
     */
    Long getFolderSize(Long folderId);

    /**
     * 统计指定文件夹及其所有子文件夹下所有文件（包括被删除文件）总大小
     * @param folderId 文件夹ID
     * @return 总大小（字节）
     */
    Long getFolderTotalSizeIncludeDeleted(Long folderId);

    /**
     * 查询指定文件夹下所有内容，并将所有文件id和文件夹id分别存入两个容器中
     * @param folderId 文件夹ID
     * @param fileIdList 文件ID列表（输出参数）
     * @param folderIdList 文件夹ID列表（输出参数）
     */
    void collectAllFileAndFolderIds(Long folderId, java.util.List<String> fileIdList, java.util.List<Long> folderIdList);
}