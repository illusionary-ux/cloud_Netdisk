package edu.cuit.cloud_netdisk.controller;

import cn.dev33.satoken.stp.StpUtil;
import edu.cuit.cloud_netdisk.pojo.dto.EmailUpdateDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserQueryDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserRegisterDTO;
import edu.cuit.cloud_netdisk.pojo.dto.UserloginDTO;
import edu.cuit.cloud_netdisk.pojo.entity.User;
import edu.cuit.cloud_netdisk.pojo.vo.LoginResultVO;
import edu.cuit.cloud_netdisk.pojo.vo.PageResultVO;
import edu.cuit.cloud_netdisk.result.Result;
import edu.cuit.cloud_netdisk.service.Impl.EmailCodeService;
import edu.cuit.cloud_netdisk.service.UserService;
import edu.cuit.cloud_netdisk.service.Impl.UserServiceImpl;
import edu.cuit.cloud_netdisk.exception.EmailAlreadyExistException;
import edu.cuit.cloud_netdisk.exception.UsernameAlreadyExistException;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口", description = "包含用户登录、注册、邮箱验证等接口")
public class UserController {
    @Autowired
    private EmailCodeService emailCodeService;

    @Autowired
    private UserServiceImpl userServiceImpl;

    /**
     * 登录接口
     */
    @ApiOperation(value = "用户登录", notes = "支持密码登录和验证码登录两种方式")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "loginDTO", value = "登录信息", required = true, dataTypeClass = UserloginDTO.class)
    })
    @ApiResponses({
        @ApiResponse(code = 1, message = "登录成功"),
        @ApiResponse(code = 400, message = "登录失败")
    })
    @PostMapping("/login")
    public Result<LoginResultVO> login(@Validated @RequestBody UserloginDTO loginDTO) {
        log.info("用户登录请求: {}", loginDTO.getEmail());
        try {
            LoginResultVO loginResultVO = userServiceImpl.login(loginDTO);
            log.info("用户登录成功: {}", loginDTO.getEmail());
            return Result.success(loginResultVO);
        } catch (Exception e) {
            log.error("用户登录失败: {}, 原因: {}", loginDTO.getEmail(), e.getMessage());
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 退出登录接口
     */
    @ApiOperation(value = "用户退出登录", notes = "退出当前登录状态")
    @ApiResponses({
        @ApiResponse(code = 1, message = "退出成功"),
        @ApiResponse(code = 400, message = "退出失败"),
        @ApiResponse(code = 401,message = "用户未登录")
    })
    @PostMapping("/logout")
    public Result<String> logout() {
        log.info("用户退出登录");
        try {
            // 检查是否已登录
            if (!StpUtil.isLogin()) {
                log.warn("用户未登录，尝试退出");
                return Result.error(401, "用户未登录");
            }
            StpUtil.logout();
            log.info("用户退出登录成功");
            return Result.success(1, "退出成功");
        } catch (Exception e) {
            log.error("用户退出登录失败: {}", e.getMessage());
            return Result.error(500, "退出失败：" + e.getMessage());
        }
    }

    /**
     * 发送验证码接口
     */
    @ApiOperation(value = "邮箱验证码发送", notes = "向指定邮箱发送6位数字验证码，5分钟内有效")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "email", value = "邮箱地址", required = true, dataTypeClass = String.class)
    })
    @ApiResponses({
        @ApiResponse(code = 1, message = "验证码发送成功"),
        @ApiResponse(code = 400, message = "验证码发送失败")
    })
    @PostMapping("/send-code")
    public Result<String> sendVerificationCode(@RequestParam String email) {
        if (!StringUtils.hasText(email)) {
            return Result.error("邮箱不能为空");
        }
        log.info("发送验证码请求: {}", email);
        try {
            emailCodeService.sendVerificationCode(email);
            log.info("验证码发送成功: {}", email);
            return Result.success("验证码已发送");
        } catch (Exception e) {
            log.error("验证码发送失败: {}, 原因: {}", email, e.getMessage());
            return Result.error("验证码发送失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "邮箱更新", notes = "更新用户邮箱地址，需要验证码验证")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "emailUpdateDTO", value = "邮箱更新信息", required = true, dataTypeClass = EmailUpdateDTO.class)
    })
    @ApiResponses({
        @ApiResponse(code = 1, message = "邮箱更新成功"),
        @ApiResponse(code = 400, message = "邮箱更新失败")
    })
    @PutMapping("/email")
    public Result<String> updateEmail(@Validated @RequestBody EmailUpdateDTO emailUpdateDTO) {
        //登录鉴权
        StpUtil.checkLogin();

        log.info("邮箱更新请求: {}", emailUpdateDTO.getEmail());

        //邮箱验证码校验
        if (!emailCodeService.VerifyCode(emailUpdateDTO.getNew_email(), emailUpdateDTO.getVerificationCode())) {
            return Result.error("验证码错误或已过期");
        }

        try {
            userServiceImpl.emailUpdate(emailUpdateDTO);
            log.info("邮箱更新成功: {}", emailUpdateDTO.getNew_email());

            // 获取当前用户ID并踢下线
            Long userId = StpUtil.getLoginIdAsLong();
            StpUtil.logout(userId);

            return Result.success("邮箱已更新");
        } catch (Exception e) {
            log.error("邮箱更新失败: {}, 原因: {}", emailUpdateDTO.getEmail(), e.getMessage());
            return Result.error("邮箱更新失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "用户注册", notes = "新用户注册，需要提供用户名、密码、邮箱验证码等信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userRegisterDTO", value = "用户注册信息", required = true, dataTypeClass = UserRegisterDTO.class)
    })
    @ApiResponses({
        @ApiResponse(code = 1, message = "注册成功"),
        @ApiResponse(code = 400, message = "注册失败")
    })
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册请求: {}", userRegisterDTO.getUsername());
        
        // 验证码校验
        if (!emailCodeService.VerifyCode(userRegisterDTO.getEmail(), userRegisterDTO.getVerificationCode())) {
            log.warn("用户注册失败: {}, 原因: 验证码错误或已过期", userRegisterDTO.getUsername());
            return Result.error("验证码错误或已过期");
        }
        
        try {
            userServiceImpl.register(userRegisterDTO);
            log.info("用户注册成功: {}", userRegisterDTO.getUsername());
            return Result.success("注册成功");
        } catch (EmailAlreadyExistException e) {
            log.warn("用户注册失败: {}, 原因: 邮箱已存在", userRegisterDTO.getUsername());
            return Result.error("该邮箱已被注册");
        } catch (UsernameAlreadyExistException e) {
            log.warn("用户注册失败: {}, 原因: 用户名已存在", userRegisterDTO.getUsername());
            return Result.error("该用户名已被使用");
        } catch (Exception e) {
            log.error("用户注册失败: {}, 原因: {}", userRegisterDTO.getUsername(), e.getMessage());
            return Result.error("注册失败，请稍后重试");
        }
    }

    /**
     * 获取用户个人信息接口
     */
    @ApiOperation(value = "获取用户个人信息", notes = "获取当前登录用户的个人信息")
    @ApiResponses({
        @ApiResponse(code = 1, message = "获取成功"),
        @ApiResponse(code = 401, message = "未登录")
    })
    @GetMapping("/profile")
    public Result<User> getUserProfile() {
        log.info("获取用户个人信息");
        try {
            // 检查是否已登录
            if (!StpUtil.isLogin()) {
                log.warn("用户未登录，无法获取个人信息");
                return Result.error(401, "用户未登录");
            }
            
            // 获取当前登录用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userServiceImpl.getUserById(userId);
            
            log.info("获取用户个人信息成功: {}", user.getUsername());
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户个人信息失败: {}", e.getMessage());
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 用户升级为VIP接口
     */
    @ApiOperation(value = "用户升级为VIP", notes = "将当前用户角色升级为VIP")
    @ApiResponses({
            @ApiResponse(code = 1, message = "升级成功"),
            @ApiResponse(code = 401, message = "用户未登录"),
            @ApiResponse(code = 500, message = "升级失败")
    })
    @PostMapping("/upgrade-vip")
    public Result<String> upgradeToVip() {
        log.info("用户请求升级VIP");
        try {
            // 检查是否已登录
            if (!StpUtil.isLogin()) {
                log.warn("用户未登录，无法升级VIP");
                return Result.error(401, "用户未登录");
            }

            // 获取当前登录用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            userServiceImpl.upgradeToVip(userId);

            log.info("用户{}升级VIP成功", userId);
            return Result.success("升级VIP成功");
        } catch (Exception e) {
            log.error("用户升级VIP失败: {}", e.getMessage());
            return Result.error("升级VIP失败：" + e.getMessage());
        }
    }

    @ApiOperation("分页查询用户列表")
    @GetMapping("/list")
    public Result<PageResultVO<User>> listUsers(UserQueryDTO queryDTO) {
        log.info("开始查询用户列表，查询参数：pageNum={}, pageSize={}, username={}, email={}", 
            queryDTO.getPageNum(), 
            queryDTO.getPageSize(), 
            queryDTO.getUsername(), 
            queryDTO.getEmail());
        try {
            PageResultVO<User> result = userServiceImpl.listUsers(queryDTO);
            log.info("用户列表查询成功，总记录数：{}，当前页数据量：{}", 
                result.getTotal(), 
                result.getList() != null ? result.getList().size() : 0);
            // 确保返回的数据结构符合前端期望
            if (result.getList() == null) {
                result.setList(java.util.Collections.emptyList());
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return Result.error("查询用户列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{userId}")
    public Result<String> deleteUser(@PathVariable Long userId) {
        try {
            userServiceImpl.deleteUser(userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户角色接口
     */
    @ApiOperation(value = "获取用户角色", notes = "获取当前登录用户的角色列表")
    @ApiResponses({
        @ApiResponse(code = 1, message = "获取成功"),
        @ApiResponse(code = 401, message = "未登录")
    })
    @GetMapping("/roles")
    public Result<List<String>> getUserRoles() {
        log.info("获取用户角色");
        try {
            // 检查是否已登录
            if (!StpUtil.isLogin()) {
                log.warn("用户未登录，无法获取角色");
                return Result.error(401, "用户未登录");
            }
            
            // 获取当前登录用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            List<String> roles = userServiceImpl.getRolesByUserId(userId);
            
            log.info("获取用户角色成功: {}", roles);
            return Result.success(roles);
        } catch (Exception e) {
            log.error("获取用户角色失败: {}", e.getMessage());
            return Result.error("获取用户角色失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户权限接口
     */
    @ApiOperation(value = "获取用户权限", notes = "获取当前登录用户的权限列表")
    @ApiResponses({
        @ApiResponse(code = 1, message = "获取成功"),
        @ApiResponse(code = 401, message = "未登录")
    })
    @GetMapping("/permissions")
    public Result<List<String>> getUserPermissions() {
        log.info("获取用户权限");
        try {
            // 检查是否已登录
            if (!StpUtil.isLogin()) {
                log.warn("用户未登录，无法获取权限");
                return Result.error(401, "用户未登录");
            }
            
            // 获取当前登录用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            List<String> permissions = userServiceImpl.getPermissionsByUserId(userId);
            
            log.info("获取用户权限成功: {}", permissions);
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("获取用户权限失败: {}", e.getMessage());
            return Result.error("获取用户权限失败：" + e.getMessage());
        }
    }
}
