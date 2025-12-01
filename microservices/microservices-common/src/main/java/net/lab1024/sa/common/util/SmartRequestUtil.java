package net.lab1024.sa.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求工具类
 * 严格遵循repowiki规范
 */
@Slf4j
public class SmartRequestUtil {

    private static final String UNKNOWN = "unknown";

    /**
     * 获取请求对象
     */
    public static HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return requestAttributes != null ? requestAttributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("获取HttpServletRequest失败", e);
            return null;
        }
    }

    /**
     * 获取客户端IP地址
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 对于多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 获取User-Agent
     */
    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }

    /**
     * 获取请求的完整URL
     */
    public static String getRequestUrl() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getRequestURL().toString();
    }

    /**
     * 获取请求URI
     */
    public static String getRequestUri() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getRequestURI();
    }

    /**
     * 获取请求方法
     */
    public static String getRequestMethod() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getMethod();
    }

    /**
     * 获取请求参数
     */
    public static String getQueryString() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getQueryString();
    }

    /**
     * 获取Referer
     */
    public static String getReferer() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader("Referer");
    }

    /**
     * 是否为Ajax请求
     */
    public static boolean isAjaxRequest() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return false;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");

        return "XMLHttpRequest".equals(xRequestedWith) ||
               (accept != null && accept.contains("application/json"));
    }

    /**
     * 是否为移动端请求
     */
    public static boolean isMobileRequest() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return false;
        }

        String userAgent = getUserAgent();
        if (userAgent == null) {
            return false;
        }

        return userAgent.contains("Mobile") ||
               userAgent.contains("Android") ||
               userAgent.contains("iPhone") ||
               userAgent.contains("iPad") ||
               userAgent.contains("iPod");
    }

    /**
     * 获取请求头信息
     */
    public static String getHeader(String headerName) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(headerName);
    }

    /**
     * 获取请求参数
     */
    public static String getParameter(String paramName) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getParameter(paramName);
    }

    /**
     * 获取请求参数数组
     */
    public static String[] getParameterValues(String paramName) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getParameterValues(paramName);
    }

    /**
     * 判断是否为GET请求
     */
    public static boolean isGetRequest() {
        return "GET".equalsIgnoreCase(getRequestMethod());
    }

    /**
     * 判断是否为POST请求
     */
    public static boolean isPostRequest() {
        return "POST".equalsIgnoreCase(getRequestMethod());
    }

    /**
     * 判断是否为PUT请求
     */
    public static boolean isPutRequest() {
        return "PUT".equalsIgnoreCase(getRequestMethod());
    }

    /**
     * 判断是否为DELETE请求
     */
    public static boolean isDeleteRequest() {
        return "DELETE".equalsIgnoreCase(getRequestMethod());
    }

    /**
     * 获取当前登录用户ID
     * 从Request属性或Header获取
     */
    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        return getUserId(request);
    }

    /**
     * 获取当前请求用户ID（别名方法）
     * 从Request属性或Header获取
     */
    public static Long getRequestUserId() {
        return getUserId();
    }

    /**
     * 获取当前登录用户ID
     * 从指定的HttpServletRequest获取
     */
    public static Long getUserId(HttpServletRequest request) {
        try {
            if (request != null) {
                // 1. 先从Request属性获取
                Object userId = request.getAttribute("userId");
                if (userId instanceof Long) {
                    return (Long) userId;
                }
                if (userId instanceof String) {
                    try {
                        return Long.parseLong((String) userId);
                    } catch (NumberFormatException e) {
                        log.warn("解析用户ID失败: {}", userId);
                    }
                }

                // 2. 尝试从Header获取
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    try {
                        return Long.parseLong(userIdHeader);
                    } catch (NumberFormatException e) {
                        log.warn("解析用户ID失败: {}", userIdHeader);
                    }
                }

                // 3. 尝试从Session获取
                try {
                    Object sessionUserId = request.getSession().getAttribute("userId");
                    if (sessionUserId instanceof Long) {
                        return (Long) sessionUserId;
                    }
                    if (sessionUserId instanceof String) {
                        return Long.parseLong((String) sessionUserId);
                    }
                } catch (Exception e) {
                    // Session访问失败时忽略
                }
            }

            log.warn("无法获取当前用户ID");
            return null;
        } catch (Exception e) {
            log.warn("获取用户ID时发生异常", e);
            return null;
        }
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        try {
            HttpServletRequest request = getRequest();
            if (request != null) {
                // 1. 先从Request属性获取
                Object username = request.getAttribute("username");
                if (username instanceof String) {
                    return (String) username;
                }

                // 2. 尝试从Header获取
                String usernameHeader = request.getHeader("X-Username");
                if (usernameHeader != null && !usernameHeader.isEmpty()) {
                    return usernameHeader;
                }

                // 3. 尝试从Session获取
                try {
                    Object sessionUsername = request.getSession().getAttribute("username");
                    if (sessionUsername instanceof String) {
                        return (String) sessionUsername;
                    }
                } catch (Exception e) {
                    // Session访问失败时忽略
                }
            }

            log.warn("无法获取当前用户名");
            return null;
        } catch (Exception e) {
            log.warn("获取用户名时发生异常", e);
            return null;
        }
    }

    /**
     * 获取当前登录用户信息
     */
    public static Object getCurrentUser() {
        try {
            HttpServletRequest request = getRequest();
            if (request != null) {
                // 1. 先从Request属性获取
                Object user = request.getAttribute("currentUser");
                if (user != null) {
                    return user;
                }

                // 2. 尝试从Session获取
                try {
                    Object sessionUser = request.getSession().getAttribute("currentUser");
                    if (sessionUser != null) {
                        return sessionUser;
                    }
                } catch (Exception e) {
                    // Session访问失败时忽略
                }
            }

            log.warn("无法获取当前用户信息");
            return null;
        } catch (Exception e) {
            log.warn("获取用户信息时发生异常", e);
            return null;
        }
    }

    /**
     * 获取用户真实IP地址（适用于代理服务器环境）
     */
    public static String getRealIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    /**
     * 判断是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // 内网IP段
        return ip.startsWith("192.168.") ||
               ip.startsWith("10.") ||
               ip.startsWith("172.") ||
               ip.equals("127.0.0.1") ||
               ip.equals("localhost");
    }
}