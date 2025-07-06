package edu.cuit.cloud_netdisk.service.Impl;

import edu.cuit.cloud_netdisk.dao.FileShareMapper;
import edu.cuit.cloud_netdisk.pojo.entity.FileShare;
import edu.cuit.cloud_netdisk.pojo.vo.PageResultVO;
import edu.cuit.cloud_netdisk.service.FileShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileShareServiceImpl implements FileShareService {
    @Autowired
    private FileShareMapper fileShareMapper;

    @Override
    public int insertFileShare(FileShare fileShare) {
        return fileShareMapper.insertFileShare(fileShare);
    }

    @Override
    public int deleteFileShare(String urlid) {
        return fileShareMapper.deleteFileShare(urlid);
    }

    @Override
    public int updateFileShare(FileShare fileShare) {
        return fileShareMapper.updateFileShare(fileShare);
    }

    @Override
    public FileShare selectFileShareById(String urlid) {
        return fileShareMapper.selectFileShareById(urlid);
    }

    @Override
    public List<FileShare> selectAllFileShares() {
        return fileShareMapper.selectAllFileShares();
    }

    @Override
    public List<FileShare> selectFileSharesByUserId(Long creatorId) {
        return fileShareMapper.selectFileSharesByUserId(creatorId);
    }

    @Override
    public PageResultVO<FileShare> getFileSharesByUserId(Long userId, int pageNum, int pageSize) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        System.out.println("[分享分页调试] 用户ID: " + userId + ", 页码: " + pageNum + ", 每页大小: " + pageSize + ", 偏移量: " + offset);
        
        // 查询总数
        Long total = fileShareMapper.countByUserId(userId);
        System.out.println("[分享分页调试] 总记录数: " + total);
        
        // 查询分页数据
        List<FileShare> records = fileShareMapper.selectFileSharesByUserIdWithPage(userId, offset, pageSize);
        System.out.println("[分享分页调试] 查询到记录数: " + (records != null ? records.size() : 0));
        
        // 构建返回结果
        PageResultVO<FileShare> result = new PageResultVO<>(total, records, pageNum, pageSize);
        System.out.println("[分享分页调试] 返回结果 - 总页数: " + result.getPages() + ", 当前页数据量: " + (result.getList() != null ? result.getList().size() : 0));
        
        return result;
    }

    @Override
    public List<FileShare> selectFileSharesByFileId(String fileid) {
        return fileShareMapper.selectFileSharesByFileId(fileid);
    }

    @Override
    public int deleteFileSharesByFileId(String fileid) {
        return fileShareMapper.deleteFileSharesByFileId(fileid);
    }

    @Override
    public int markFileSharesAsExpiredByFileId(String fileid) {
        return fileShareMapper.markFileSharesAsExpiredByFileId(fileid);
    }

    @Override
    public int countValidSharesByFileId(String fileid) {
        return fileShareMapper.countValidSharesByFileId(fileid);
    }
} 