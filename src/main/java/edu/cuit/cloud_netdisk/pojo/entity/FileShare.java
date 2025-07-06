package edu.cuit.cloud_netdisk.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileShare {
    private String urlid;
    private Long creatorId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime; // 分享链接过期时间
    
    private Integer shareCount;
    private Boolean isExpired;
    private String fileid;
    private String filename;
    private String password;
} 