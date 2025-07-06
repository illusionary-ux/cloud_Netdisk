package edu.cuit.cloud_netdisk.pojo.dto;

import lombok.Data;

@Data
public class FolderDTO {
    /**
     * 文件夹名称
     */
    private String folderName;

    /**
     * 父文件夹ID
     */
    private Long parentFolderId;

    /**
     * 创建者ID
     */
    private Long userId;
}
