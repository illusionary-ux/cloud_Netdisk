package edu.cuit.cloud_netdisk.service.Impl;

import edu.cuit.cloud_netdisk.dao.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;

    public Long findRoleIdByName(String roleName) {
        return roleMapper.findIdByName(roleName);
    }
}