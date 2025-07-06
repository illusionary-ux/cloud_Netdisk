package edu.cuit.cloud_netdisk.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class File {
    private String fileId; // UUID

    private String fileName;

    private String fileType;

    private Long fileSize;

    private String storagePath;//url

    private Long uploadUser;//上传用户id

    private Integer folder;//所属文件夹id

    private Boolean isFavorite = false;

    private Boolean isDeleted = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;
}
