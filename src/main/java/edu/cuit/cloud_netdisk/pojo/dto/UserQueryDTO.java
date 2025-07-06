package edu.cuit.cloud_netdisk.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户查询条件")
public class UserQueryDTO {
    @ApiModelProperty(value = "用户名（模糊查询）")
    private String username;

    @ApiModelProperty(value = "邮箱（模糊查询）")
    private String email;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;
} 