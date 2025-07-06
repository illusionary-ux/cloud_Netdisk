package edu.cuit.cloud_netdisk.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role")
public class Role {
    private Long roleId;
    private String roleName;        // 角色名称
    private String description; // 角色描述
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}