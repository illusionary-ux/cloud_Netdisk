package edu.cuit.cloud_netdisk.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "邮箱更新数据传输对象")
public class EmailUpdateDTO implements Serializable {

    @ApiModelProperty(value = "当前邮箱地址", required = true, example = "old@example.com")
    private String email;

    @ApiModelProperty(value = "新邮箱地址", required = true, example = "new@example.com")
    private String new_email;

    @ApiModelProperty(value = "验证码", required = true, example = "123456")
    private String verificationCode;
}
