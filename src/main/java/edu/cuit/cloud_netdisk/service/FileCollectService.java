package edu.cuit.cloud_netdisk.service;

import com.github.pagehelper.PageInfo;
import edu.cuit.cloud_netdisk.pojo.entity.FileCollect;
import edu.cuit.cloud_netdisk.pojo.vo.PageResultVO;

public interface FileCollectService {
    
    /**
     * 收藏文件
     */
    void collectFile(Long userId, String fileId);
    
    /**
     * 取消收藏
     */
    void cancelCollect(Long userId, String fileId);
    
    /**
     * 通过文件ID删除所有收藏记录
     * @param fileId 文件ID
     */
    void cancelAllCollectsByFileId(String fileId);
    
    /**
     * 分页查询用户收藏的文件
     */
    PageResultVO<FileCollect> getCollectedFiles(Long userId, int pageNum, int pageSize);
    
    /**
     * 检查文件是否已被收藏
     */
    boolean isCollected(Long userId, String fileId);
} 