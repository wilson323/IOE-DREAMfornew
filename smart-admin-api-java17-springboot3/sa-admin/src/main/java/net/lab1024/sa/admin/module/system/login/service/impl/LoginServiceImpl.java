package net.lab1024.sa.admin.module.system.login.service.impl;

import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.system.login.domain.RequestEmployee;
import net.lab1024.sa.admin.module.system.login.service.LoginService;

/**
 * 登录服务实现类
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright 1024创新实验室
 */
@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Override
    public RequestEmployee getRequestEmployee(Long userId) {
        // 简单实现：构造一个基础的员工信息
        RequestEmployee requestEmployee = new RequestEmployee();
        requestEmployee.setUserId(userId);
        requestEmployee.setUserName("admin");
        requestEmployee.setRealName("管理员");
        requestEmployee.setDepartmentId(1L);
        requestEmployee.setDepartmentName("技术部");
        requestEmployee.setLoginTime(System.currentTimeMillis());
        requestEmployee.setAdministratorFlag(true);
        requestEmployee.setUserIp("127.0.0.1");
        requestEmployee.setUserAgent("SmartAdmin");
        return requestEmployee;
    }

    @Override
    public RequestEmployee getRequestEmployee(String token) {
        // 简单实现：从token解析用户ID，默认为1
        try {
            Long userId = 1L; // 实际项目中应从token解析
            return getRequestEmployee(userId);
        } catch (Exception e) {
            log.error("解析token失败", e);
            return null;
        }
    }

    @Override
    public RequestEmployee getRequestEmployee(HttpServletRequest request) {
        // 简单实现：从请求头获取token并解析
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return getRequestEmployee(token);
    }

    @Override
    public RequestEmployee getLoginEmployee(String loginId, HttpServletRequest request) {
        // 简单实现：从loginId获取用户信息
        try {
            Long userId = Long.parseLong(loginId);
            RequestEmployee requestEmployee = getRequestEmployee(userId);
            if (requestEmployee != null && request != null) {
                // 设置真实的IP和UserAgent
                requestEmployee.setUserIp(getClientIpAddr(request));
                requestEmployee.setUserAgent(request.getHeader("User-Agent"));
            }
            return requestEmployee;
        } catch (Exception e) {
            log.error("解析loginId失败: {}", loginId, e);
            return null;
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
