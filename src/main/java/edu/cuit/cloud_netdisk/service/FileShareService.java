package edu.cuit.cloud_netdisk.service;

import edu.cuit.cloud_netdisk.pojo.entity.FileShare;
import edu.cuit.cloud_netdisk.pojo.vo.PageResultVO;

import java.util.List;

public interface FileShareService {
    int insertFileShare(FileShare fileShare);
    int deleteFileShare(String urlid);
    int updateFileShare(FileShare fileShare);
    FileShare selectFileShareById(String urlid);
    List<FileShare> selectAllFileShares();
    List<FileShare> selectFileSharesByUserId(Long creatorId);
    
    /**
     * 分页查询用户分享的文件
     */
    PageResultVO<FileShare> getFileSharesByUserId(Long userId, int pageNum, int pageSize);
    
    /**
     * 通过文件ID查找分享信息
     * @param fileid 文件ID
     * @return 分享信息列表
     */
    List<FileShare> selectFileSharesByFileId(String fileid);
    /**
     * 通过文件ID删除分享记录
     * @param fileid 文件ID
     * @return 删除的记录数
     */
    int deleteFileSharesByFileId(String fileid);
    /**
     * 通过文件ID标记分享记录为失效
     * @param fileid 文件ID
     * @return 更新的记录数
     */
    int markFileSharesAsExpiredByFileId(String fileid);
    /**
     * 检查文件是否有有效的分享链接
     * @param fileid 文件ID
     * @return 有效分享链接的数量
     */
    int countValidSharesByFileId(String fileid);
} 