package edu.cuit.cloud_netdisk.controller;

import edu.cuit.cloud_netdisk.annotation.RequirePermission;
import edu.cuit.cloud_netdisk.pojo.dto.FolderDTO;
import edu.cuit.cloud_netdisk.pojo.dto.FolderRenameDTO;
import edu.cuit.cloud_netdisk.pojo.vo.FolderVO;
import edu.cuit.cloud_netdisk.service.FolderService;
import edu.cuit.cloud_netdisk.result.Result;
import edu.cuit.cloud_netdisk.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/folders")
public class FolderController {

    @Resource
    private FolderService folderService;

    @Resource
    private UserService userService;

    /**
     * 创建文件夹
     */
    @RequirePermission("folder:create")
    @PostMapping
    public Result<FolderVO> createFolder(@RequestBody FolderDTO folderDTO) {
        if (folderDTO.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        return Result.success(folderService.createFolder(folderDTO));
    }

    /**
     * 重命名文件夹
     */
    @RequirePermission("folder:rename")
    @PutMapping("/{folderId}/rename")
    public Result<FolderVO> renameFolder(
            @PathVariable Long folderId,
            @RequestBody FolderRenameDTO renameDTO) {
        return Result.success(folderService.renameFolder(folderId, renameDTO));
    }

    /**
     * 删除文件夹
     */
    @DeleteMapping("/{folderId}")
    public Result deleteFolder(@PathVariable Long folderId, @RequestParam Long userId) {
        Long delta=-folderService.getFolderSize(folderId);
        userService.safeUpdateUsedStorage(userId,delta);
        folderService.deleteFolder(folderId);
        return Result.success();
    }

    /**
     * 获取文件夹列表
     */
    @RequirePermission("folder:list")
    @GetMapping("/getfolderlist")
    public Result<List<FolderVO>> getFolderList(
            @RequestParam(defaultValue = "0") Long parentFolderId) {
        return Result.success(folderService.getFolderList(parentFolderId));
    }

    /**
     * 获取文件夹路径
     */
    @RequirePermission("folder:list")
    @GetMapping("/{folderId}/path")
    public Result<List<FolderVO>> getFolderPath(@PathVariable Long folderId) {
        return Result.success(folderService.getFolderPath(folderId));
    }
}
