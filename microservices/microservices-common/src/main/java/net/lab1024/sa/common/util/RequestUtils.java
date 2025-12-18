package net.lab1024.sa.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求工具类
 * <p>
 * 提供请求上下文相关的工具方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
public final class RequestUtils {

    private RequestUtils() {
        // 私有构造函数，禁止实例化
    }

    /**
     * 获取当前请求
     *
     * @return HttpServletRequest，如果不在请求上下文中则返回null
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     * <p>
     * 从请求头或会话中获取用户ID
     * </p>
     *
     * @return 用户ID，如果未登录或不在请求上下文中则返回null
     */
    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        // 从请求头获取用户ID
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // 从请求属性获取
        Object userId = request.getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
