package edu.cuit.cloud_netdisk.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDTO implements Serializable {
    private Long user_id;

    private String username;

    private String password_hash;

    private String email;

    private Long storage_limit;

    private Long used_storage;
}
