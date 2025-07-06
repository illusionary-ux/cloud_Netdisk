package edu.cuit.cloud_netdisk.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FolderVO {
    /**
     * 文件夹ID
     */
    private Long folderId;

    /**
     * 文件夹名称
     */
    private String folderName;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 父文件夹ID
     */
    private Long parentFolderId;

    /**
     * 文件夹大小（字节）
     */
    private Long folderSize;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
