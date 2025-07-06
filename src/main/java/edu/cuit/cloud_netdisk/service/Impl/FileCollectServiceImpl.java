package edu.cuit.cloud_netdisk.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.cuit.cloud_netdisk.dao.FileCollectMapper;
import edu.cuit.cloud_netdisk.pojo.entity.FileCollect;
import edu.cuit.cloud_netdisk.service.FileCollectService;
import edu.cuit.cloud_netdisk.pojo.vo.PageResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileCollectServiceImpl implements FileCollectService {

    @Autowired
    private FileCollectMapper fileCollectMapper;

    @Override
    @Transactional
    public void collectFile(Long userId, String fileId) {
        if (!isCollected(userId, fileId)) {
            FileCollect fileCollect = new FileCollect();
            fileCollect.setUserId(userId);
            fileCollect.setFileId(fileId);
            fileCollect.setCreateTime(LocalDateTime.now());
            fileCollect.setUpdateTime(LocalDateTime.now());
            fileCollectMapper.insert(fileCollect);
        }
    }

    @Override
    @Transactional
    public void cancelCollect(Long userId, String fileId) {
        QueryWrapper<FileCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("file_id", fileId);
        fileCollectMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void cancelAllCollectsByFileId(String fileId) {
        fileCollectMapper.deleteAllCollectsByFileId(fileId);
    }

    @Override
    public PageResultVO<FileCollect> getCollectedFiles(Long userId, int pageNum, int pageSize) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        System.out.println("[分页调试] 用户ID: " + userId + ", 页码: " + pageNum + ", 每页大小: " + pageSize + ", 偏移量: " + offset);
        
        // 查询总数
        Long total = fileCollectMapper.countByUserId(userId);
        System.out.println("[分页调试] 总记录数: " + total);
        
        // 查询分页数据
        List<FileCollect> records = fileCollectMapper.findByUserIdWithPage(userId, offset, pageSize);
        System.out.println("[分页调试] 查询到记录数: " + (records != null ? records.size() : 0));
        
        // 构建返回结果
        PageResultVO<FileCollect> result = new PageResultVO<>(total, records, pageNum, pageSize);
        System.out.println("[分页调试] 返回结果 - 总页数: " + result.getPages() + ", 当前页数据量: " + (result.getList() != null ? result.getList().size() : 0));
        
        return result;
    }

    @Override
    public boolean isCollected(Long userId, String fileId) {
        return fileCollectMapper.checkCollected(userId, fileId) > 0;
    }
} 