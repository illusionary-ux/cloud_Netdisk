package edu.cuit.cloud_netdisk.dao;

import edu.cuit.cloud_netdisk.pojo.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色数据访问层接口
 * 负责角色相关的数据库操作
 */
@Mapper
public interface RoleMapper {
    /**
     * 根据角色名称查找角色ID
     * @param roleName 角色名称
     * @return 角色ID
     */
    @Select("SELECT role_id FROM role WHERE role_name = #{roleName}")
    Long findIdByName(String roleName);

    /**
     * 根据用户ID查询该用户拥有的所有角色
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> selectRolesByUserId(Long userId);
}
