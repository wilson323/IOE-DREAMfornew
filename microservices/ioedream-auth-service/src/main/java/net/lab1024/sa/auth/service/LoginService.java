package net.lab1024.sa.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import net.lab1024.sa.auth.domain.vo.RequestEmployee;

/**
 * 登录服务接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
public interface LoginService {

    /**
     * 根据用户ID获取员工信息
     *
     * @param userId 用户ID
     * @return 员工信息
     */
    RequestEmployee getRequestEmployee(Long userId);

    /**
     * 根据token获取员工信息
     *
     * @param token token
     * @return 员工信息
     */
    RequestEmployee getRequestEmployee(String token);

    /**
     * 根据HTTP请求获取员工信息
     *
     * @param request HTTP请求
     * @return 员工信息
     */
    RequestEmployee getRequestEmployee(HttpServletRequest request);

    /**
     * 根据登录ID和HTTP请求获取员工信息
     *
     * @param loginId 登录ID
     * @param request HTTP请求
     * @return 员工信息
     */
    RequestEmployee getLoginEmployee(String loginId, HttpServletRequest request);
}