package edu.cuit.cloud_netdisk.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-权限关联数据访问层接口
 * 负责角色和权限之间关联关系的数据库操作
 */
@Mapper
public interface RolePermissionMapper {
    /**
     * 添加角色-权限关联
     * @param roleId 角色ID
     * @param permissionId 权限ID
     */
    void insert(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 删除指定角色的所有权限关联
     * @param roleId 角色ID
     */
    void deleteByRoleId(Long roleId);

    /**
     * 批量添加角色-权限关联
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void batchInsert(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
}