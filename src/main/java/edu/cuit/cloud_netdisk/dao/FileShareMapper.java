package edu.cuit.cloud_netdisk.dao;

import edu.cuit.cloud_netdisk.pojo.entity.FileShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FileShareMapper {
    int insertFileShare(FileShare fileShare);
    int deleteFileShare(@Param("urlid") String urlid);
    int updateFileShare(FileShare fileShare);
    FileShare selectFileShareById(@Param("urlid") String urlid);
    List<FileShare> selectAllFileShares();
    List<FileShare> selectFileSharesByUserId(@Param("creatorId") Long creatorId);
    
    /**
     * 分页查询用户分享的文件
     */
    @Select("SELECT * FROM fileshare WHERE creator_id = #{userId} ORDER BY create_time DESC LIMIT #{pageSize} OFFSET #{offset}")
    List<FileShare> selectFileSharesByUserIdWithPage(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 统计用户分享的文件总数
     */
    @Select("SELECT COUNT(1) FROM fileshare WHERE creator_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * 通过文件ID查找分享信息
     * @param fileid 文件ID
     * @return 分享信息列表
     */
    List<FileShare> selectFileSharesByFileId(@Param("fileid") String fileid);
    /**
     * 通过文件ID删除分享记录
     * @param fileid 文件ID
     * @return 删除的记录数
     */
    int deleteFileSharesByFileId(@Param("fileid") String fileid);
    /**
     * 通过文件ID标记分享记录为失效
     * @param fileid 文件ID
     * @return 更新的记录数
     */
    int markFileSharesAsExpiredByFileId(@Param("fileid") String fileid);
    /**
     * 检查文件是否有有效的分享链接
     * @param fileid 文件ID
     * @return 有效分享链接的数量
     */
    int countValidSharesByFileId(@Param("fileid") String fileid);
} 