package edu.cuit.cloud_netdisk.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 用户-角色关联数据访问层接口
 * 负责用户和角色之间关联关系的数据库操作
 */
@Mapper
public interface UserRoleMapper {
    /**
     * 添加用户-角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    @Insert("INSERT INTO user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    void insert(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 删除用户-角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    void deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据角色ID查询用户ID列表
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByRoleId(Long roleId);
}
