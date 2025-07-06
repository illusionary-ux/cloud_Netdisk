package edu.cuit.cloud_netdisk.controller;

import edu.cuit.cloud_netdisk.pojo.entity.File;
import edu.cuit.cloud_netdisk.pojo.vo.RecycleBinItemVO;
import edu.cuit.cloud_netdisk.result.Result;
import edu.cuit.cloud_netdisk.service.Impl.FileServiceImpl;
import edu.cuit.cloud_netdisk.service.Impl.OssService;
import edu.cuit.cloud_netdisk.service.RecycleBinService;
import edu.cuit.cloud_netdisk.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/recycle-bin")
@Api(tags = "回收站管理")
@Slf4j
public class RecycleBinController {

    @Resource
    private RecycleBinService recycleBinService;

    @Autowired
    private FileServiceImpl fileService;

    @Autowired
    private UserService userService;

    @GetMapping
    @ApiOperation("获取回收站列表")
    public Result<List<RecycleBinItemVO>> getRecycleBinItems(
            @RequestParam @ApiParam(value = "用户ID", required = true) Long userId) {
        return Result.success(recycleBinService.getRecycleBinItems(userId));
    }

    @PostMapping("/{id}/restore")
    @ApiOperation("恢复回收站项目")
    public Result<Void> restoreItem(
            @PathVariable @ApiParam(value = "回收站项目ID", required = true) Long id,
            @RequestParam @ApiParam(value = "用户ID", required = true) Long userId) {



        recycleBinService.restoreItem(id, userId);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("彻底删除回收站项目")
    public Result<Void> deleteItem(
            @PathVariable @ApiParam(value = "回收站项目ID", required = true) Long id,
            @RequestParam @ApiParam(value = "用户ID", required = true) Long userId) {
        recycleBinService.deleteItem(id, userId);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("清空回收站")
    public Result<Void> emptyRecycleBin(
            @RequestParam @ApiParam(value = "用户ID", required = true) Long userId) {
        recycleBinService.emptyRecycleBin(userId);
        return Result.success();
    }
} 