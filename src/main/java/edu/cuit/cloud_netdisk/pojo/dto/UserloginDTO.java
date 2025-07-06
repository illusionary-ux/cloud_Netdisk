package edu.cuit.cloud_netdisk.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Data
@Slf4j
@ApiModel(description = "用户登录数据传输对象")
public class UserloginDTO implements Serializable {
    @ApiModelProperty(value = "邮箱地址", required = true, example = "user@example.com")
    private String email;

    @ApiModelProperty(value = "密码（密码登录时必填）", example = "password123")
    private String password;

    @ApiModelProperty(value = "验证码（验证码登录时必填）", example = "123456")
    private String verificationCode;

    @ApiModelProperty(value = "登录类型：0-密码登录，1-验证码登录", required = true, example = "0")
    private Integer loginType;
}
