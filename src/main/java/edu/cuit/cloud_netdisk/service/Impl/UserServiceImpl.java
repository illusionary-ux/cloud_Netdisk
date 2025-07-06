package edu.cuit.cloud_netdisk.service.Impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.cuit.cloud_netdisk.constant.MessageConstant;
import edu.cuit.cloud_netdisk.dao.UserRoleMapper;
import edu.cuit.cloud_netdisk.dao.RoleMapper;
import edu.cuit.cloud_netdisk.dao.PermissionMapper;
import edu.cuit.cloud_netdisk.exception.*;
import edu.cuit.cloud_netdisk.dao.UserMapper;
import edu.cuit.cloud_netdisk.pojo.dto.EmailUpdateDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserQueryDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserRegisterDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserloginDTO;
import edu.cuit.cloud_netdisk.pojo.entity.User;
import edu.cuit.cloud_netdisk.pojo.entity.Role;
import edu.cuit.cloud_netdisk.pojo.dto.UserDTO;
import edu.cuit.cloud_netdisk.pojo.vo.LoginResultVO;
import edu.cuit.cloud_netdisk.pojo.vo.PageResultVO;
import edu.cuit.cloud_netdisk.service.PermissionService;
import edu.cuit.cloud_netdisk.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.IllegalArgumentException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailCodeService emailCodeService;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 用户登录主入口
     * @param userloginDTO
     * @return
     */
    public LoginResultVO login(UserloginDTO userloginDTO) {
        // 参数校验
        if (userloginDTO == null || userloginDTO.getEmail() == null || userloginDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        User user;
        // 根据登录类型路由到不同的登录方法
        switch (userloginDTO.getLoginType()) {
            case 0: // 密码登录
                user = passwordLogin(userloginDTO.getEmail(), userloginDTO.getPassword());
                break;
            case 1: // 验证码登录
                user = verificationCodeLogin(userloginDTO.getEmail(), userloginDTO.getVerificationCode());
                break;
            default:
                throw new IllegalArgumentException(MessageConstant.UnsupportedLoginMethod);
        }

        // Sa-Token 登录
        StpUtil.login(user.getUserId());

        // 初始化用户权限
        permissionService.initUserPermissions(user.getUserId());

        // 更新最后登录时间
        LocalDateTime now = LocalDateTime.now();
        userMapper.updateLastLogin(user.getUserId(), now);
        
        // 重新获取更新后的用户信息
        user = userMapper.getByEmail(user.getEmail());

        // 获取 Token 信息
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 构建返回结果
        LoginResultVO result = new LoginResultVO();
        result.setUser(user);
        result.setToken(tokenInfo.getTokenValue());

        log.info("用户登录成功: {}", user.getUserId());
        return result;
    }

    /**
     * 密码登录实现
     *
     * @param email    邮箱
     * @param password 密码
     * @return 用户信息
     */
    private User passwordLogin(String email, String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException(MessageConstant.PASSWORD_EMPTY);
        }

        User user = getUserByEmail(email);

        // 密码校验
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            log.warn("密码错误，登录失败，邮箱: {}", email);
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        return user;
    }

    /**
     * 邮箱验证码登录实现
     *
     * @param email 邮箱
     * @param code  验证码
     * @return 用户信息
     */
    private User verificationCodeLogin(String email, String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException(MessageConstant.VIllegalArgumentException);
        }

        // 验证验证码
        if (!emailCodeService.VerifyCode(email, code)) {
            log.warn("验证码错误或已过期，邮箱: {}", email);
            throw new VerificationCodeException(MessageConstant.VERIFICATION_CODE_ERROR);
        }

        return getUserByEmail(email);
    }

    /**
     * 根据邮箱获取用户信息（公共方法）
     *
     * @param email 邮箱
     * @return 用户信息
     * @throws AccountNotFoundException 如果用户不存在
     */
    private User getUserByEmail(String email) {
        User user = userMapper.getByEmail(email);
        if (user == null) {
            log.warn("账号不存在，邮箱: {}", email);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        return user;
    }

    /**
     * 用户邮箱更新
     * @param emailUpdateDTO
     */
    public void emailUpdate(EmailUpdateDTO emailUpdateDTO) {

        String oldMail = emailUpdateDTO.getEmail();

        User user = userMapper.getByEmail(oldMail);
        //验证旧邮件是否存在
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 验证新邮箱格式
        if(!isValidEmail(emailUpdateDTO.getNew_email())) {
            throw new EmailIllegalException(MessageConstant.EmailIllegal);
        }

        // 3. 检查新邮箱是否已被使用
        if (userMapper.getByEmail(emailUpdateDTO.getNew_email()) != null) {
            throw new EmailAlreadyExistException(MessageConstant.EmailAlreadyExist);
        }
        user.setEmail(emailUpdateDTO.getNew_email());
        userMapper.updateEmail(user);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * 用户注册
     * @param userRegisterDTO
     */
    public void register(UserRegisterDTO userRegisterDTO) {
        // 校验用户名
        if (userRegisterDTO.getUsername() == null || userRegisterDTO.getUsername().length() < 3) {
            throw new IllegalArgumentException(MessageConstant.UsernameLenthIllegal);
        }
        // 校验密码
        if (userRegisterDTO.getPasswordHash() == null || userRegisterDTO.getPasswordHash().isEmpty()) {
            throw new PasswordEmptyException(MessageConstant.PASSWORD_EMPTY);
        }
        // 检查用户名是否已存在
        if (userMapper.checkUsernameExists(userRegisterDTO.getUsername())) {
            throw new UsernameAlreadyExistException(MessageConstant.UsernameAlreadyExist);
        }
        // 检查邮箱是否已存在
        if (userMapper.getByEmail(userRegisterDTO.getEmail()) != null) {
            throw new EmailAlreadyExistException(MessageConstant.EmailAlreadyExist);
        }
        
        // 创建User实体
        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setEmail(userRegisterDTO.getEmail());
        
        // 对密码进行加密
        String rawPassword = userRegisterDTO.getPasswordHash();
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt()); // 加密密码
        user.setPasswordHash(hashedPassword); // 替换为加密后的密码
        
        // 设置默认存储限制和已用存储
        user.setStorageLimit(1073741824L);
        user.setUsedStorage(0L);
        // 设置创建时间等默认时间
        user.setCreateTime(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setLastUpdate(LocalDateTime.now());
        // 插入用户到数据库
        userMapper.insertUser(user);

        // 默认分配user角色
        assignDefaultRole(userMapper.getIDByEmail(user.getEmail()));
    }

    /**
     * 为注册用户创建USER角色
     * @param userId
     */
    private void assignDefaultRole(Long userId) {
        // 查找user角色的ID
        Long userRoleId = roleMapper.findIdByName("user");

        // 分配角色
        userRoleMapper.insert(userId, userRoleId);

        StpUtil.renewTimeout(userId);  // 续期并刷新缓存
        StpUtil.getRoleList(userId);   // 重新获取角色列表（会自动刷新）
        StpUtil.getPermissionList(userId); // 重新获取权限列表（会自动刷新）
    }

    public void registerWithRole(UserRegisterDTO userRegisterDTO, String roleName) {
        // 基本注册逻辑
        register(userRegisterDTO);

        // 覆盖默认角色，分配指定角色
        Long userId = userMapper.getIDByEmail(userRegisterDTO.getEmail());
        Long roleId = roleService.findRoleIdByName(roleName); // 使用注入的实例调用方法

        if (roleId != null) {
            // 先删除原有角色关联（如果需要覆盖）
            // 再分配新角色
            userRoleMapper.insert(userId, roleId);
        } else {
            // 角色不存在，使用默认角色
            assignDefaultRole(userMapper.getIDByEmail(userRegisterDTO.getEmail()));
        }
    }

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     * @throws AccountNotFoundException 如果用户不存在
     */
    @Override
    public User getUserById(Long userId) {
        User user = userMapper.getById(userId);
        if (user == null) {
            log.warn("用户不存在，ID: {}", userId);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        return user;
    }


    @Override
    public void upgradeToVip(Long userId) {
        // 检查用户是否存在
        User user = getUserById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 查找vip角色的ID（根据数据库中role表，vip的role_id是3）
        Long vipRoleId = 3L;

        // 删除用户原有的user角色（role_id = 2）
        userRoleMapper.deleteByUserIdAndRoleId(userId, 2L);

        // 分配VIP角色
        userRoleMapper.insert(userId, vipRoleId);

        // 更新用户存储限制（假设VIP用户有10GB存储空间）
        user.setStorageLimit(10L * 1024 * 1024 * 1024); // 10GB in bytes
        user.setLastUpdate(LocalDateTime.now());
        userMapper.updateUser(user);

        // 刷新Sa-Token的权限缓存
        StpUtil.renewTimeout(userId);  // 续期并刷新缓存
        StpUtil.getRoleList(userId);   // 重新获取角色列表（会自动刷新）
        StpUtil.getPermissionList(userId); // 重新获取权限列表（会自动刷新）

        log.info("用户{}已成功升级为VIP", userId);
    }

    @Override
    public PageResultVO<User> listUsers(UserQueryDTO queryDTO) {
        log.info("开始执行用户列表查询，参数：{}", queryDTO);
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        log.info("分页参数：offset={}, pageSize={}", offset, queryDTO.getPageSize());

        // 查询总数
        Long total = userMapper.countUsers(queryDTO.getUsername(), queryDTO.getEmail());
        log.info("用户总数：{}", total);

        // 查询分页数据
        List<User> users = userMapper.listUsersWithPage(queryDTO.getUsername(), queryDTO.getEmail(), offset, queryDTO.getPageSize());
        log.info("分页查询结果数量：{}", users != null ? users.size() : 0);

        PageResultVO<User> result = new PageResultVO<>(total, users, queryDTO.getPageNum(), queryDTO.getPageSize());
        log.info("返回分页结果，总页数：{}，当前页数据量：{}", result.getPages(), result.getList() != null ? result.getList().size() : 0);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        // 检查用户是否存在
        User user = getUserById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        try {
            // 删除用户的所有角色关联
            userMapper.deleteUserRoles(userId);

            // 删除用户
            userMapper.deleteUser(userId);

            // 清除用户的 Sa-Token 缓存
            StpUtil.logout(userId);

            log.info("用户{}及其权限已删除", userId);
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            throw new RuntimeException("删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户的所有角色
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<String> getRolesByUserId(Long userId) {
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的所有权限
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<String> getPermissionsByUserId(Long userId) {
        // 获取用户的所有角色
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        
        // 获取这些角色的所有权限
        Set<String> permissions = new HashSet<>();
        for (Role role : roles) {
            List<String> rolePermissions = permissionMapper.selectPermissionsByRoleId(role.getRoleId());
            permissions.addAll(rolePermissions);
        }
        
        return new ArrayList<>(permissions);
    }

    @Override
    public Long getUsedStorage(Long userId) {
        User user = userMapper.getById(userId);
        if (user == null) return null;
        return user.getUsedStorage();
    }

    @Override
    public Long getStorageLimit(Long userId) {
        User user = userMapper.getById(userId);
        if (user == null) return null;
        return user.getStorageLimit();
    }

    @Override
    public void safeUpdateUsedStorage(Long userId, Long delta) {
        User user = userMapper.getById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        Long used = user.getUsedStorage();
        Long limit = user.getStorageLimit();
        if (used == null || limit == null) throw new RuntimeException("用户存储信息异常");
        Long after = used + delta;
        if (after < 0) after = 0L;
        if (after > limit) throw new RuntimeException("存储空间不足，无法完成操作");
        userMapper.updateUsedStorage(userId, delta);
    }
}