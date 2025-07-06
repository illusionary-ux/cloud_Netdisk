package edu.cuit.cloud_netdisk.controller;

import cn.dev33.satoken.stp.StpUtil;
import edu.cuit.cloud_netdisk.pojo.entity.File;
import edu.cuit.cloud_netdisk.pojo.entity.FileCollect;
import edu.cuit.cloud_netdisk.result.Result;
import edu.cuit.cloud_netdisk.service.FileCollectService;
import com.github.pagehelper.PageInfo;
import edu.cuit.cloud_netdisk.service.Impl.FileServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import edu.cuit.cloud_netdisk.pojo.vo.PageResultVO;

@Api(tags = "文件收藏接口")
@RestController
@RequestMapping("/file-collect")
public class FileCollectController {

    @Autowired
    private FileCollectService fileCollectService;

    @Autowired
    private FileServiceImpl fileService;

    @ApiOperation("收藏文件")
    @PostMapping("/collect/{fileId}")
    public Result<Void> collectFile(@PathVariable String fileId) {
        Long userId = StpUtil.getLoginIdAsLong();
        fileCollectService.collectFile(userId, fileId);
        return Result.success();
    }

    @ApiOperation("取消收藏")
    @DeleteMapping("/cancel/{fileId}")
    public Result<Void> cancelCollect(@PathVariable String fileId) {
        Long userId = StpUtil.getLoginIdAsLong();
        fileCollectService.cancelCollect(userId, fileId);
        return Result.success();
    }

    @ApiOperation("获取收藏的文件列表")
    @GetMapping("/list")
    public Result<PageResultVO<FileCollect>> getCollectedFiles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        PageResultVO<FileCollect> pageInfo = fileCollectService.getCollectedFiles(userId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @ApiOperation("检查文件是否已收藏")
    @GetMapping("/check/{fileId}")
    public Result<Boolean> checkCollected(@PathVariable String fileId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean collected = fileCollectService.isCollected(userId, fileId);
        return Result.success(collected);
    }

    @ApiOperation("检查文件是否是别人上传的")
    @GetMapping("/checkisother/{fileId}/{userId}")
    public Result<Boolean> checkisother(@PathVariable String fileId, @PathVariable Long userId) {
        // 日志：收到请求参数
        System.out.println("[checkisother] 请求参数 fileId=" + fileId + ", userId=" + userId);
        File file = fileService.findbyId(fileId);
        if (file == null) {
            System.out.println("[checkisother] fileId=" + fileId + " 查无此文件");
            return Result.error("文件不存在");
        }
        Long olduserId = file.getUploadUser();
        // 日志：查到的文件上传者
        System.out.println("[checkisother] fileId=" + fileId + " 的上传者 olduserId=" + olduserId);
        boolean collected = !olduserId.equals(userId);
        // 日志：最终判断结果
        System.out.println("[checkisother] 是否他人文件结果 isOther=" + collected);
        return Result.success(collected);
    }
} 