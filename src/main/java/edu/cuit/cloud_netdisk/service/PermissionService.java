// src/main/java/edu/cuit/cloud_netdisk/service/PermissionService.java
package edu.cuit.cloud_netdisk.service;

import edu.cuit.cloud_netdisk.pojo.entity.Permission;
import java.util.List;
import java.util.Set;

public interface PermissionService {
    /**
     * 初始化用户权限
     * @param userId 用户ID
     */
    void initUserPermissions(Long userId);

    /**
     * 检查用户是否拥有指定权限
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(String permissionCode);

    /**
     * 检查用户是否拥有指定权限中的任意一个
     * @param permissionCodes 权限编码列表
     * @return 是否拥有权限
     */
    boolean hasAnyPermission(String... permissionCodes);

    /**
     * 检查用户是否拥有所有指定权限
     * @param permissionCodes 权限编码列表
     * @return 是否拥有所有权限
     */
    boolean hasAllPermissions(String... permissionCodes);

    /**
     * 获取用户权限列表
     * @param userId 用户ID
     * @return 权限编码集合
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 更新角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void updateRolePermissions(Long roleId, List<Long> permissionIds);

    /**
     * 清除用户权限缓存
     * @param userId 用户ID
     */
    void clearUserPermissionsCache(Long userId);

    /**
     * 获取角色所有权限
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getRolePermissions(Long roleId);

    /**
     * 获取用户所有权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getUserPermissionList(Long userId);

    /**
     * 检查用户是否是指定角色
     * @param roleCode 角色编码
     * @return 是否是指定角色
     */
    boolean hasRole(String roleCode);

    /**
     * 获取用户所有角色
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 添加用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void addUserRole(Long userId, Long roleId);

    /**
     * 移除用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void removeUserRole(Long userId, Long roleId);
}