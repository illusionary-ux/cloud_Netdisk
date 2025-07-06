package edu.cuit.cloud_netdisk.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Pointcut("execution(* edu.cuit.cloud_netdisk.controller..*.*(..))")
    public void controllerPoint() {}

    @Pointcut("execution(* edu.cuit.cloud_netdisk.service..*.*(..))")
    public void servicePoint() {}

    @Before("controllerPoint()")
    public void logBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.info("=================请求开始=================");
            log.info("URL: {}", request.getRequestURL().toString());
            log.info("HTTP Method: {}", request.getMethod());
            log.info("Class Method: {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            log.info("IP: {}", request.getRemoteAddr());
            log.info("Request Args: {}", Arrays.toString(joinPoint.getArgs()));
        }
    }

    @Around("servicePoint()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        log.info("开始执行: {}.{}", className, methodName);
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        
        log.info("方法: {}.{} 执行完成, 耗时: {}ms", className, methodName, (endTime - startTime));
        return result;
    }

    @AfterThrowing(pointcut = "controllerPoint() || servicePoint()", throwing = "e")
    public void logError(JoinPoint joinPoint, Throwable e) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.error("方法: {}.{} 发生异常: {}", className, methodName, e.getMessage());
    }
} 