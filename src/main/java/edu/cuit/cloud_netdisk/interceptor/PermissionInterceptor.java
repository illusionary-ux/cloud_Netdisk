package edu.cuit.cloud_netdisk.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import edu.cuit.cloud_netdisk.annotation.RequirePermission;
import edu.cuit.cloud_netdisk.exception.NoPermissionException;
import edu.cuit.cloud_netdisk.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限拦截器
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果不是映射到方法，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取方法上的注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);

        // 如果没有添加权限注解，直接通过
        if (requirePermission == null) {
            return true;
        }

        // 如果未登录，抛出异常
        if (!StpUtil.isLogin()) {
            throw new NoPermissionException("请先登录");
        }

        // 获取注解中的权限编码
        String permissionCode = requirePermission.value();

        // 校验权限
        if (!permissionService.hasPermission(permissionCode)) {
            throw new NoPermissionException("无权限访问");
        }

        return true;
    }
} 