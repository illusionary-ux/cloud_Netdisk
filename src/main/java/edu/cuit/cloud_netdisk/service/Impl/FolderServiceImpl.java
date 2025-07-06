package edu.cuit.cloud_netdisk.service.Impl;

import edu.cuit.cloud_netdisk.dao.FolderMapper;
import edu.cuit.cloud_netdisk.dao.RecycleBinMapper;
import edu.cuit.cloud_netdisk.dao.FileMapper;
import edu.cuit.cloud_netdisk.exception.FolderNameIllegalException;
import edu.cuit.cloud_netdisk.pojo.dto.FolderDTO;
import edu.cuit.cloud_netdisk.pojo.dto.FolderRenameDTO;
import edu.cuit.cloud_netdisk.pojo.entity.File;
import edu.cuit.cloud_netdisk.pojo.entity.Folder;
import edu.cuit.cloud_netdisk.pojo.vo.FolderVO;
import edu.cuit.cloud_netdisk.service.FolderService;
import edu.cuit.cloud_netdisk.service.FileCollectService;
import edu.cuit.cloud_netdisk.service.FileShareService;
import edu.cuit.cloud_netdisk.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FolderServiceImpl implements FolderService {

    private static final Logger log = LoggerFactory.getLogger(FolderServiceImpl.class);

    @Resource
    private FolderMapper folderMapper;

    @Resource
    private RecycleBinMapper recycleBinMapper;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private FileCollectService fileCollectService;

    @Resource
    private FileShareService fileShareService;

    @Resource
    private UserService userService;

    // 根文件夹ID
    private static final Long ROOT_FOLDER_ID = 1L;

    @Override
    @Transactional
    public FolderVO createFolder(FolderDTO folderDTO) {
        log.info("创建文件夹入参: {}", folderDTO);
        // 检查是否是根文件夹
        if (folderDTO.getParentFolderId() == 0) {
            folderDTO.setParentFolderId(ROOT_FOLDER_ID);
        }

        // 检查父文件夹是否存在
        Folder parentFolder = folderMapper.selectByFolderId(folderDTO.getParentFolderId());
        if (parentFolder == null) {
            throw new RuntimeException("父文件夹不存在");
        }

        // 检查文件夹名称是否已存在
        if (checkFolderNameExist(folderDTO.getParentFolderId(), folderDTO.getFolderName())) {
            throw new FolderNameIllegalException("文件夹名称已存在");
        }


        // 创建文件夹实体
        Folder folder = new Folder();
        folder.setFolderName(folderDTO.getFolderName());
        folder.setParentFolderId(folderDTO.getParentFolderId());
        folder.setUserId(folderDTO.getUserId());

        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());
        folder.setIsDeleted(0);

        log.info("插入前 Folder 对象: {}", folder);
        // 保存文件夹
        int result = folderMapper.insert(folder);
        log.info("插入结果: {}, 回填folderId: {}", result, folder.getFolderId());

        if (folder.getFolderId() == null) {
            log.error("插入后 folderId 仍为 null，可能是主键回填失败！");
        }

        // 确保从数据库重新获取最新数据
        Folder savedFolder = folderMapper.selectByFolderId(folder.getFolderId());
        log.info("[添加文件夹] 用户ID:{}，新建文件夹ID:{}，当前存储空间:{} 字节", folderDTO.getUserId(), savedFolder.getFolderId(), savedFolder.getFolderSize());

        // 转换为VO返回
        return convertToVO(savedFolder);
    }

    @Override
    @Transactional
    public FolderVO renameFolder(Long folderId, FolderRenameDTO renameDTO) {
        // 不允许重命名根文件夹
        if (folderId.equals(ROOT_FOLDER_ID)) {
            throw new RuntimeException("不能重命名根文件夹");
        }

        // 获取原文件夹信息
        Folder folder = folderMapper.selectByFolderId(folderId);
        if (folder == null) {
            throw new RuntimeException("文件夹不存在");
        }

        // 检查新名称是否已存在
        if (checkFolderNameExist(folder.getParentFolderId(), renameDTO.getNewName())) {
            throw new FolderNameIllegalException("文件夹名称已存在");
        }

        // 更新文件夹名称
        folder.setFolderName(renameDTO.getNewName());
        folder.setUpdateTime(LocalDateTime.now());
        folderMapper.updateById(folder);

        return convertToVO(folder);
    }

    /**
     * 递归统计文件夹及其所有子文件夹和文件的总大小
     * @param folderId 文件夹ID
     * @param includeDeleted 是否包含被删除的文件
     */
    private Long getFolderTotalSize(Long folderId, boolean includeDeleted) {
        Long totalSize = 0L;
        List<File> files = includeDeleted ? fileMapper.findByFolderIdIncludeDeleted(folderId.intValue()) : fileMapper.findByFolderId(folderId.intValue());
        if (files != null) {
            for (File file : files) {
                totalSize += file.getFileSize();
            }
        }
        List<Folder> subFolders = folderMapper.selectByParentId(folderId);
        if (subFolders != null) {
            for (Folder sub : subFolders) {
                totalSize += getFolderTotalSize(sub.getFolderId(), includeDeleted);
            }
        }
        return totalSize;
    }

    // 兼容原有调用
    private Long getFolderTotalSize(Long folderId) {
        return getFolderTotalSize(folderId, false);
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId) {
        if (folderId.equals(ROOT_FOLDER_ID)) {
            throw new RuntimeException("不能删除根文件夹");
        }
        Folder folder = folderMapper.selectByFolderId(folderId);
        if (folder == null) {
            throw new RuntimeException("文件夹不存在");
        }


        // 收集所有文件夹和文件id
        List<Long> folderIdList = new ArrayList<>();
        List<String> fileIdList = new ArrayList<>();
        collectAllFileAndFolderIds(folderId, fileIdList, folderIdList);
        log.info("[删除文件夹] 文件夹ID:{}，将删除的所有文件夹ID:{}，所有文件ID:{}", folderId, folderIdList, fileIdList);
        // 统计总空间
        Long totalSize = getFolderTotalSize(folderId);

        log.info("[删除文件夹] 用户ID:{}，将减少的存储空间:{} 字节", folder.getUserId(), totalSize);
        // 批量软删除所有文件夹
        if (!folderIdList.isEmpty()) {
            for (Long fid : folderIdList) {
                folderMapper.softDeleteFolder(fid);
            }
        }
        // 批量软删除所有文件
        if (!fileIdList.isEmpty()) {
            for (String fileId : fileIdList) {
                fileMapper.deleteById(fileId, folder.getUserId());
            }
        }
        // 只对直接父文件夹做一次减法
        if (folder.getParentFolderId() != null && folder.getParentFolderId() > 0) {
            if (totalSize != null && totalSize > 0) {
                folderMapper.updateFolderSize(folder.getParentFolderId(), -totalSize);
            }
        }
        // 只插入一条回收站记录（只记录文件夹本身）
        recycleBinMapper.insertRecycleBinItem(
            String.valueOf(folderId),
            2,
            LocalDateTime.now(),
            folder.getUserId(),
            folder.getParentFolderId().intValue()
        );
    }

    /**
     * 批量恢复指定文件夹及其所有内容
     */
    @Transactional
    public void restoreFolderAndChildren(Long folderId) {
        // 收集所有文件夹和文件id
        List<Long> folderIdList = new ArrayList<>();
        List<String> fileIdList = new ArrayList<>();
        collectAllFileAndFolderIds(folderId, fileIdList, folderIdList);
        log.info("[恢复文件夹] 文件夹ID:{}，将恢复的所有文件夹ID:{}，所有文件ID:{}", folderId, folderIdList, fileIdList);
        // 统计总空间（恢复时包含被删除文件）
        Long totalSize = getFolderTotalSize(folderId, true);
        // 这里假设恢复时会增加用户空间
        Folder folder = folderMapper.selectByFolderId(folderId);
        if (folder != null) {
            log.info("[恢复文件夹] 用户ID:{}，将增加的存储空间:{} 字节", folder.getUserId(), totalSize);
        }
        // 批量恢复所有文件夹
        if (!folderIdList.isEmpty()) {
            for (Long fid : folderIdList) {
                folderMapper.restoreFolder(fid);
            }
        }
        // 批量恢复所有文件
        if (!fileIdList.isEmpty()) {
            for (String fileId : fileIdList) {
                fileMapper.recoverById(fileId, null);
            }
        }
        // 恢复后移除回收站记录
        recycleBinMapper.deleteByItemIdAndType(folderId.toString(), 2);
    }

    @Override
    public List<FolderVO> getFolderList(Long parentFolderId) {
        // 如果是根目录，则使用ROOT_FOLDER_ID
        if (parentFolderId == 0) {
            parentFolderId = ROOT_FOLDER_ID;
        }

        List<Folder> folders = folderMapper.selectByParentId(parentFolderId);
        return folders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FolderVO> getFolderPath(Long folderId) {
        // 如果是根目录，返回空列表
        if (folderId == 0 || folderId.equals(ROOT_FOLDER_ID)) {
            return Collections.emptyList();
        }

        List<Folder> folders = folderMapper.selectFolderPath(folderId);
        return folders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkFolderNameExist(Long parentFolderId, String folderName) {
        // 如果是根目录，则使用ROOT_FOLDER_ID
        if (parentFolderId == 0) {
            parentFolderId = ROOT_FOLDER_ID;
        }
        return folderMapper.checkFolderNameExist(parentFolderId, folderName) > 0;
    }

    @Override
    public Long getFolderSize(Long folderId) {
        Folder folder = folderMapper.selectByFolderId(folderId);
        if (folder == null) {
            return null;
        }
        return folder.getFolderSize();
    }

    @Override
    public Long getFolderTotalSizeIncludeDeleted(Long folderId) {
        return getFolderTotalSize(folderId, true);
    }

    /**
     * 将Folder实体转换为FolderVO
     */
    private FolderVO convertToVO(Folder folder) {
        if (folder == null) {
            return null;
        }
        FolderVO vo = new FolderVO();
        vo.setFolderId(folder.getFolderId());
        vo.setFolderName(folder.getFolderName());
        vo.setCreatorId(folder.getUserId());
        vo.setParentFolderId(folder.getParentFolderId());
        vo.setFolderSize(folder.getFolderSize());
        vo.setCreateTime(folder.getCreateTime());
        vo.setUpdateTime(folder.getUpdateTime());
        return vo;
    }

    /**
     * 查询指定文件夹下所有内容，并将所有文件id和文件夹id分别存入两个容器中
     */
    public void collectAllFileAndFolderIds(Long folderId, List<String> fileIdList, List<Long> folderIdList) {
        // 查询所有文件夹id
        List<Long> allFolderIds = folderMapper.selectAllFolderIdsRecursive(folderId);
        folderIdList.addAll(allFolderIds);
        // 查询所有文件id
        List<String> allFileIds = fileMapper.selectAllFileIdsByFolderRecursive(folderId);
        fileIdList.addAll(allFileIds);
    }
} 