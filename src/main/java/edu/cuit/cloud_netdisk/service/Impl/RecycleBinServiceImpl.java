package edu.cuit.cloud_netdisk.service.Impl;

import edu.cuit.cloud_netdisk.dao.FileMapper;
import edu.cuit.cloud_netdisk.dao.RecycleBinMapper;
import edu.cuit.cloud_netdisk.dao.UserMapper;
import edu.cuit.cloud_netdisk.dao.FolderMapper;
import edu.cuit.cloud_netdisk.pojo.entity.File;
import edu.cuit.cloud_netdisk.pojo.entity.User;
import edu.cuit.cloud_netdisk.pojo.entity.Folder;
import edu.cuit.cloud_netdisk.pojo.vo.RecycleBinItemVO;
import edu.cuit.cloud_netdisk.service.RecycleBinService;
import edu.cuit.cloud_netdisk.service.FolderService;
import edu.cuit.cloud_netdisk.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class RecycleBinServiceImpl implements RecycleBinService {

    @Resource
    private RecycleBinMapper recycleBinMapper;

    @Resource
    private FileMapper fileMapper;

    @Autowired
    private OssService ossService;

    @Autowired
    UserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private FolderMapper folderMapper;

    @Resource
    private FolderService folderService;

    @Override
    public List<RecycleBinItemVO> getRecycleBinItems(Long userId) {
        return recycleBinMapper.selectRecycleBinItemsByUserId(userId);
    }

    @Override
    @Transactional
    public void restoreItem(Long id, Long userId) {
        // 1. 获取回收站项目信息
        RecycleBinItemVO item = recycleBinMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("回收站项目不存在");
        }

        // 2. 验证用户权限
        if (!item.getDeleteUser().equals(userId)) {
            throw new RuntimeException("您没有权限操作此项目");
        }


        // 恢复文件 - 先统计需要使用的空间大小
        Long fileSize ;
        if(item.getItemType() == 1){
            fileSize=fileMapper.getFileSize(item.getItemId());
        }else {
//            log.info("文件夹是"+Long.parseLong(item.getItemId()));
//            log.info("恢复要增加的大小为"+folderMapper.selectByFolderId(Long.parseLong(item.getItemId())));
            fileSize=folderMapper.selectFolderSizeById(Long.parseLong(item.getItemId()));
        }
        userService.safeUpdateUsedStorage(userId,fileSize);

        // 3. 根据项目类型恢复文件或文件夹
        if (item.getItemType() == 1) {
            // 恢复文件
            recycleBinMapper.restoreFile(item.getItemId());

            // 删除回收站记录
            recycleBinMapper.deleteById(id);
        } else {
            folderService.restoreFolderAndChildren(Long.parseLong(item.getItemId()));
        }
    }

    @Override
    @Transactional
    public String deleteItem(Long id, Long userId) {
        // 1. 获取回收站项目信息
        RecycleBinItemVO item = recycleBinMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("回收站项目不存在");
        }
        if (!item.getDeleteUser().equals(userId)) {
            throw new RuntimeException("您没有权限操作此项目");
        }
        if (item.getItemType() == 1) {
            // 文件
            String fileUrl = fileMapper.getFilePathByIdIncludeDeleted(item.getItemId());
            if (fileUrl != null) {
                int pathCount = fileMapper.countByStoragePath(fileUrl);
                if (pathCount <= 1) {
                    ossService.deleteFile(fileUrl);
                }
            }
            fileMapper.deleteById(item.getItemId(),userId);
            recycleBinMapper.deleteFile(item.getItemId());
        } else {
            // 文件夹彻底删除
            List<String> fileIdList = new java.util.ArrayList<>();
            List<Long> folderIdList = new java.util.ArrayList<>();
            folderService.collectAllFileAndFolderIds(Long.parseLong(item.getItemId()), fileIdList, folderIdList);
            // 先删除所有文件
            for (String fileId : fileIdList) {
                String fileUrl = fileMapper.getFilePathByIdIncludeDeleted(fileId);
                if (fileUrl != null) {
                    int pathCount = fileMapper.countByStoragePath(fileUrl);
                    if (pathCount <= 1) {
                        ossService.deleteFile(fileUrl);
                    }
                }
                fileMapper.deleteById(fileId, userId);
                recycleBinMapper.deleteFile(fileId);
            }
            // 再删除所有文件夹
            for (Long folderId : folderIdList) {
                folderMapper.deleteById(folderId);
                recycleBinMapper.deleteFolder(folderId.toString());
                recycleBinMapper.deleteByItemIdAndType(folderId.toString(), 2);
            }
        }
        recycleBinMapper.deleteById(id);
        return item.getItemId();
    }

    @Override
    @Transactional
    public void emptyRecycleBin(Long userId) {
        List<RecycleBinItemVO> items = recycleBinMapper.selectRecycleBinItemsByUserId(userId);
        for (RecycleBinItemVO item : items) {
            deleteItem(item.getId(), userId);
        }
    }

    /**
     * 递归统计文件夹及其所有子文件夹和文件的总大小
     */
    private Long getFolderTotalSize(Long folderId) {
        Long totalSize = 0L;
        List<File> files = fileMapper.findByFolderId(folderId.intValue());
        if (files != null) {
            for (File file : files) {
                totalSize += file.getFileSize();
            }
        }
        List<edu.cuit.cloud_netdisk.pojo.entity.Folder> subFolders = folderMapper.selectByParentId(folderId);
        if (subFolders != null) {
            for (edu.cuit.cloud_netdisk.pojo.entity.Folder sub : subFolders) {
                totalSize += getFolderTotalSize(sub.getFolderId());
            }
        }
        return totalSize;
    }

    /**
     * 递归查找并彻底删除文件夹及其所有子文件和子文件夹的回收站记录
     */
    private void deleteFolderAndChildren(Long folderId, Long userId) {
        // 统计并扣减空间
        Long totalSize = getFolderTotalSize(folderId);
        if (totalSize != null && totalSize > 0) {
            userMapper.updateUsedStorage(userId, -totalSize);
        }
        // 彻底删除当前文件夹下所有文件的回收站记录
        List<Long> fileRecycleIds = recycleBinMapper.selectFileRecycleIdsByFolderId(folderId.toString());
        if (fileRecycleIds != null) {
            for (Long rid : fileRecycleIds) {
                // 先物理删除文件
                RecycleBinItemVO fileItem = recycleBinMapper.selectById(rid);
                if (fileItem != null) {
                    String fileUrl = fileMapper.getFilePathByIdIncludeDeleted(fileItem.getItemId());
                    if (fileUrl != null) {
                        int pathCount = fileMapper.countByStoragePath(fileUrl);
                        if (pathCount <= 1) {
                            ossService.deleteFile(fileUrl);
                        }
                    }
                    recycleBinMapper.deleteFile(fileItem.getItemId());
                }
                // 删除回收站记录
                recycleBinMapper.deleteById(rid);
            }
        }
        // 递归彻底删除所有子文件夹
        List<Folder> subFolders = folderMapper.selectByParentId(folderId);
        if (subFolders != null) {
            for (Folder sub : subFolders) {
                deleteFolderAndChildren(sub.getFolderId(), userId);
            }
        }
        // 删除当前文件夹
        recycleBinMapper.deleteFolder(folderId.toString());
        recycleBinMapper.deleteByItemIdAndType(folderId.toString(), 2);
    }
} 