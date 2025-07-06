package edu.cuit.cloud_netdisk.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, message = "用户名长度不能少于3个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String passwordHash;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码长度应为6位")
    private String verificationCode;

}