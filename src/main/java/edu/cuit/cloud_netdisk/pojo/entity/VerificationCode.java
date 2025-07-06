package edu.cuit.cloud_netdisk.pojo.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VerificationCode {
    private String code;
    private Long expireTime;
}
