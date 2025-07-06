package edu.cuit.cloud_netdisk.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.cuit.cloud_netdisk.pojo.entity.FileCollect;
import edu.cuit.cloud_netdisk.pojo.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface FileCollectMapper extends BaseMapper<FileCollect> {
    
    @Select("SELECT fc.*, f.file_name as fileName, f.file_size as fileSize, " +
            "f.file_type as fileType, f.upload_time as uploadTime " +
            "FROM file_collect fc " +
            "LEFT JOIN files f ON fc.file_id = f.file_id " +
            "WHERE fc.user_id = #{userId} " +
            "ORDER BY fc.create_time ASC")
    List<FileCollect> findByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(1) FROM file_collect WHERE user_id = #{userId} AND file_id = #{fileId}")
    int checkCollected(@Param("userId") Long userId, @Param("fileId") String fileId);

    /**
     * 通过文件ID删除所有收藏记录
     * @param fileId 文件ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM file_collect WHERE file_id = #{fileId}")
    int deleteAllCollectsByFileId(@Param("fileId") String fileId);

    @Select("SELECT fc.*, f.file_name as fileName, f.file_size as fileSize, " +
            "f.file_type as fileType, f.upload_time as uploadTime " +
            "FROM file_collect fc " +
            "LEFT JOIN files f ON fc.file_id = f.file_id " +
            "WHERE fc.user_id = #{userId} " +
            "ORDER BY fc.create_time ASC " +
            "LIMIT #{pageSize} OFFSET #{offset}")
    List<FileCollect> findByUserIdWithPage(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(1) FROM file_collect WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    @Select("SELECT fc.*, f.file_name as fileName, f.file_size as fileSize, " +
            "f.file_type as fileType, f.upload_time as uploadTime " +
            "FROM file_collect fc " +
            "LEFT JOIN files f ON fc.file_id = f.file_id " +
            "WHERE fc.user_id = #{userId} " +
            "ORDER BY fc.create_time ASC")
    Page<FileCollect> selectPageWithFileInfo(Page<FileCollect> page, @Param("userId") Long userId);
} 