package edu.cuit.cloud_netdisk.dao;

import edu.cuit.cloud_netdisk.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据访问层接口
 * 负责用户相关的数据库操作
 */
@Mapper
public interface UserMapper {
    /**
     * 根据邮箱查询用户信息
     * @param email 用户邮箱
     * @return 用户对象
     */
    @Select("select * from user where email = #{email}")
    User getByEmail(String email);

    /**
     * 根据用户ID查询用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    @Select("select * from user where user_id = #{userId}")
    User getById(Long userId);

    /**
     * 更新用户邮箱
     * @param user 用户对象
     */
    void updateEmail(User user);

    /**
     * 注册新用户
     * @param user 用户对象
     */
    void insertUser(User user);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 获取用户的所有角色
     * @param userId 用户ID
     * @return 角色列表
     */
    List<String> getRolesByUserId(Long userId);

    /**
     * 根据邮箱获取用户ID
     * @param email 用户邮箱
     * @return 用户ID
     */
    @Select("select user_id from user where email = #{email}")
    Long getIDByEmail(String email);

    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     * @param lastLogin 最后登录时间
     */
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

    /**
     * 更新用户信息
     * @param user 用户对象
     */
    void updateUser(User user);


    /**
     * 分页查询用户列表
     * @param username 用户名（可选）
     * @param email 邮箱（可选）
     * @return 用户列表
     */
    List<User> listUsers(@Param("username") String username, @Param("email") String email);

    /**
     * 删除用户
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 删除用户的所有角色关联
     * @param userId 用户ID
     */
    void deleteUserRoles(Long userId);

    /**
     * 更新用户已使用存储空间
     * @param userId 用户ID
     * @param deltaStorage 变化量（正数增加，负数减少）
     * @return 影响的行数
     */
    int updateUsedStorage(@Param("userId") Long userId, @Param("deltaStorage") Long deltaStorage);

    /**
     * 分页条件查询用户列表
     */
    @org.apache.ibatis.annotations.Select({
        "<script>",
        "SELECT * FROM user",
        "<where>",
        "<if test='username != null and username != \"\"'>",
        "AND username LIKE CONCAT('%', #{username}, '%')",
        "</if>",
        "<if test='email != null and email != \"\"'>",
        "AND email LIKE CONCAT('%', #{email}, '%')",
        "</if>",
        "</where>",
        "ORDER BY user_id ASC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<User> listUsersWithPage(@Param("username") String username, @Param("email") String email, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计用户总数（带条件）
     */
    @org.apache.ibatis.annotations.Select({
        "<script>",
        "SELECT COUNT(1) FROM user",
        "<where>",
        "<if test='username != null and username != \"\"'>",
        "AND username LIKE CONCAT('%', #{username}, '%')",
        "</if>",
        "<if test='email != null and email != \"\"'>",
        "AND email LIKE CONCAT('%', #{email}, '%')",
        "</if>",
        "</where>",
        "</script>"
    })
    Long countUsers(@Param("username") String username, @Param("email") String email);
}
