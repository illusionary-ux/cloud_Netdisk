package edu.cuit.cloud_netdisk.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("folder")
public class Folder {
    /**
     * 文件夹ID
     */
    @TableId(type = IdType.AUTO)
    private Long folderId;

    /**
     * 文件夹名称
     */
    private String folderName;

    /**
     * 创建者ID
     */
    private Long userId;

    /**
     * 父文件夹ID，根文件夹为0
     */
    private Long parentFolderId;

    /**
     * 文件夹大小（字节）
     */
    private Long folderSize;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer isDeleted;
}