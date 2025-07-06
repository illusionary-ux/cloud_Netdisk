package edu.cuit.cloud_netdisk.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("permission")
public class Permission {
    private Long permissionId;
    private String permissionCode;        // 权限编码
    private String permissionName;        // 权限名称
    private String description; // 权限描述
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}