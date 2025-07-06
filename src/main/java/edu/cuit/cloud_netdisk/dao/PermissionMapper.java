package edu.cuit.cloud_netdisk.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 权限数据访问层接口
 * 负责权限相关的数据库操作
 */
@Mapper
public interface PermissionMapper {
    /**
     * 根据角色ID查询权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<String> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询该用户拥有的所有权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> selectPermissionsByUserId(Long userId);
}