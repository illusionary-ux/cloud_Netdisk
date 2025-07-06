package edu.cuit.cloud_netdisk.service.Impl;

import cn.dev33.satoken.stp.StpUtil;
import edu.cuit.cloud_netdisk.dao.PermissionMapper;
import edu.cuit.cloud_netdisk.dao.RoleMapper;
import edu.cuit.cloud_netdisk.dao.RolePermissionMapper;
import edu.cuit.cloud_netdisk.dao.UserRoleMapper;
import edu.cuit.cloud_netdisk.pojo.entity.Permission;
import edu.cuit.cloud_netdisk.pojo.entity.Role;
import edu.cuit.cloud_netdisk.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PERMISSION_CACHE_KEY = "user:permissions:";
    private static final String ROLE_CACHE_KEY = "user:roles:";
    private static final long CACHE_EXPIRE_TIME = 30; // 分钟

    @Override
    public void initUserPermissions(Long userId) {
        // 1. 获取用户角色
        List<Role> roles = roleMapper.selectRolesByUserId(userId);

        // 2. 获取角色对应的权限
        Set<String> permissionCodes = new HashSet<>();
        for (Role role : roles) {
            List<String> permissions = permissionMapper.selectPermissionsByRoleId(role.getRoleId());
            permissionCodes.addAll(permissions);
        }

        // 3. 将权限信息存入Sa-Token
        StpUtil.getSession().set("permissions", permissionCodes);

        // 4. 缓存权限信息
        cacheUserPermissions(userId, permissionCodes);

        log.info("用户权限初始化完成，用户ID：{}，权限数量：{}", userId, permissionCodes.size());
    }

    @Override
    public boolean hasPermission(String permissionCode) {
        Long userId = StpUtil.getLoginIdAsLong();
        Set<String> permissions = getUserPermissions(userId);
        return permissions.contains(permissionCode);
    }

    @Override
    public Set<String> getUserPermissions(Long userId) {
        String cacheKey = PERMISSION_CACHE_KEY + userId;
        Set<String> permissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);

        if (permissions == null) {
            List<String> permissionList = permissionMapper.selectPermissionsByUserId(userId);
            permissions = new HashSet<>(permissionList);
            redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }

        return permissions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        // 1. 更新数据库
        rolePermissionMapper.deleteByRoleId(roleId);
        rolePermissionMapper.batchInsert(roleId, permissionIds);

        // 2. 清除相关用户的权限缓存
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(roleId);
        for (Long userId : userIds) {
            clearUserPermissionsCache(userId);
        }

        log.info("角色权限更新完成，角色ID：{}，权限数量：{}", roleId, permissionIds.size());
    }

    @Override
    public void clearUserPermissionsCache(Long userId) {
        String cacheKey = PERMISSION_CACHE_KEY + userId;
        redisTemplate.delete(cacheKey);
        log.info("用户权限缓存已清除，用户ID：{}", userId);
    }

    @Override
    public List<Permission> getRolePermissions(Long roleId) {
        List<String> permissionCodes = permissionMapper.selectPermissionsByRoleId(roleId);
        // 这里需要将权限代码转换为 Permission 对象
        // 如果需要完整的 Permission 对象，你需要添加一个新的 mapper 方法
        return permissionCodes.stream()
            .map(code -> {
                Permission permission = new Permission();
                permission.setPermissionCode(code);
                return permission;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getUserPermissionList(Long userId) {
        List<String> permissionCodes = permissionMapper.selectPermissionsByUserId(userId);
        // 这里需要将权限代码转换为 Permission 对象
        // 如果需要完整的 Permission 对象，你需要添加一个新的 mapper 方法
        return permissionCodes.stream()
            .map(code -> {
                Permission permission = new Permission();
                permission.setPermissionCode(code);
                return permission;
            })
            .collect(Collectors.toList());
    }

    @Override
    public boolean hasAnyPermission(String... permissionCodes) {
        Set<String> userPermissions = getUserPermissions(StpUtil.getLoginIdAsLong());
        for (String code : permissionCodes) {
            if (userPermissions.contains(code)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(String... permissionCodes) {
        Set<String> userPermissions = getUserPermissions(StpUtil.getLoginIdAsLong());
        for (String code : permissionCodes) {
            if (!userPermissions.contains(code)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasRole(String roleCode) {
        List<String> roles = getUserRoles(StpUtil.getLoginIdAsLong());
        return roles.contains(roleCode);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        String cacheKey = ROLE_CACHE_KEY + userId;
        List<String> roles = (List<String>) redisTemplate.opsForValue().get(cacheKey);

        if (roles == null) {
            List<Role> roleList = roleMapper.selectRolesByUserId(userId);
            roles = roleList.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
            redisTemplate.opsForValue().set(cacheKey, roles, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }

        return roles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserRole(Long userId, Long roleId) {
        userRoleMapper.insert(userId, roleId);
        clearUserPermissionsCache(userId);
        clearUserRolesCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserRole(Long userId, Long roleId) {
        userRoleMapper.deleteByUserIdAndRoleId(userId, roleId);
        clearUserPermissionsCache(userId);
        clearUserRolesCache(userId);
    }

    /**
     * 清除用户角色缓存
     */
    private void clearUserRolesCache(Long userId) {
        String cacheKey = ROLE_CACHE_KEY + userId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 缓存用户权限
     */
    private void cacheUserPermissions(Long userId, Set<String> permissions) {
        String cacheKey = PERMISSION_CACHE_KEY + userId;
        redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
    }
}