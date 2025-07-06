package edu.cuit.cloud_netdisk.pojo.vo;

import edu.cuit.cloud_netdisk.pojo.entity.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "登录结果视图对象")
public class LoginResultVO implements Serializable {
    @ApiModelProperty(value = "用户信息", required = true)
    private User user;

    @ApiModelProperty(value = "登录令牌", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
}
