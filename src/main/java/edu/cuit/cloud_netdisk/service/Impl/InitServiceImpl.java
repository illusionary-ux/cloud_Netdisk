package edu.cuit.cloud_netdisk.service.Impl;

import edu.cuit.cloud_netdisk.dao.FolderMapper;
import edu.cuit.cloud_netdisk.pojo.entity.Folder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Slf4j
@Service
public class InitServiceImpl {
    
    @Autowired
    private FolderMapper folderMapper;

    @PostConstruct
    @Transactional
    public void init() {
        // 检查root文件夹是否存在（包括已删除的）
        Folder rootFolder = folderMapper.selectByFolderId((long) 1);
        if (rootFolder == null) {
            // 创建root文件夹
            rootFolder = new Folder();
            rootFolder.setFolderId(1L);
            rootFolder.setFolderName("root");
            rootFolder.setUserId(0L); // 系统用户ID
            rootFolder.setParentFolderId(0L);
            rootFolder.setCreateTime(LocalDateTime.now());
            rootFolder.setUpdateTime(LocalDateTime.now());
            rootFolder.setIsDeleted(0);
            
            folderMapper.insert(rootFolder);
            log.info("Root folder initialized successfully，数据库初始化成功");
        } else if (rootFolder.getIsDeleted() == 1) {
            // 如果root文件夹存在但被删除了，则恢复它
            rootFolder.setIsDeleted(0);
            rootFolder.setUpdateTime(LocalDateTime.now());
            folderMapper.updateById(rootFolder);
            log.info("Root folder restored successfully");
        } else {
            log.info("Root folder already exists");
        }
    }
} 