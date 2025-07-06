package edu.cuit.cloud_netdisk.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("回收站项目")
public class RecycleBinItemVO {
    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("项目ID（文件ID或文件夹ID）")
    private String itemId;

    @ApiModelProperty("项目类型（1-文件，2-文件夹）")
    private Integer itemType;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("删除时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;

    @ApiModelProperty("删除用户ID")
    private Long deleteUser;

    @ApiModelProperty("原文件夹ID")
    private Integer originalFolderId;
} 