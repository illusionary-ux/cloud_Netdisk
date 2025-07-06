package edu.cuit.cloud_netdisk.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_collect")
public class FileCollect {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String fileId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 文件信息
    @TableField(exist = false)
    private String fileName;

    @TableField(exist = false)
    private Long fileSize;

    @TableField(exist = false)
    private String fileType;

    @TableField(exist = false)
    private LocalDateTime uploadTime;
} 