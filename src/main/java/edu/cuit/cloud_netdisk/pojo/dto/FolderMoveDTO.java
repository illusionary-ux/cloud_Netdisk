package edu.cuit.cloud_netdisk.pojo.dto;

import lombok.Data;

@Data
public class FolderMoveDTO {
    /**
     * 目标父文件夹ID
     */
    private Long newParentId;
}
