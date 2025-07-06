package edu.cuit.cloud_netdisk.service;

import edu.cuit.cloud_netdisk.pojo.dto.EmailUpdateDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserQueryDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserRegisterDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserloginDTO;
import edu.cuit.cloud_netdisk.pojo.entity.User;
import edu.cuit.cloud_netdisk.pojo.vo.LoginResultVO;
import edu.cuit.cloud_netdisk.pojo.vo.PageResultVO;

public interface UserService {
    /**
     * 用户登录主入口
     * @param userloginDTO
     * @return
     */
    public LoginResultVO login(UserloginDTO userloginDTO);

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getUserById(Long userId);

    /**
     * 用户邮箱更新
     * @param emailUpdateDTO
     */
    public void emailUpdate(EmailUpdateDTO emailUpdateDTO);

    /**
     * 用户注册
     * @param userRegisterDTO
     */
    public void register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户升级为VIP
     * @param userId 用户ID
     */
    public void upgradeToVip(Long userId);


    /**
     * 分页查询用户列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResultVO<User> listUsers(UserQueryDTO queryDTO);

    /**
     * 删除用户
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 根据用户ID获取已用空间
     */
    Long getUsedStorage(Long userId);

    /**
     * 根据用户ID获取存储限制
     */
    Long getStorageLimit(Long userId);

    /**
     * 安全更新用户已用空间（已用+delta<=限制时才更新，否则抛异常）
     * @param userId 用户ID
     * @param delta 变化量（正数增加，负数减少）
     * @throws RuntimeException 超出限制时抛出
     */
    void safeUpdateUsedStorage(Long userId, Long delta);
}
