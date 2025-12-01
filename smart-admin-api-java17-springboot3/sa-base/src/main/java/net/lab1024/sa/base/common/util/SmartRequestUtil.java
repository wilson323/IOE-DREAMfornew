package net.lab1024.sa.base.common.util;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.RequestUser;
import net.lab1024.sa.base.common.domain.RequestUrlVO;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求用户  工具类
 *
 * @Author 1024创新实验室: 罗伊
 * @Date 2022-05-30 21:22:12
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
@Slf4j
public class SmartRequestUtil {

    private static final ThreadLocal<RequestUser> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    public static void setRequestUser(RequestUser requestUser) {
        if(requestUser == null){
            return;
        }
        REQUEST_THREAD_LOCAL.set(requestUser);
    }

    public static RequestUser getRequestUser() {
        return REQUEST_THREAD_LOCAL.get();
    }

    public static Long getRequestUserId() {
        RequestUser requestUser = getRequestUser();
        return null == requestUser ? null : requestUser.getUserId();
    }


    public static void remove() {
        REQUEST_THREAD_LOCAL.remove();
    }

    /**
     * 获取当前请求对象
     */
    public static HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("获取HttpServletRequest失败", e);
            return null;
        }
    }

    /**
     * 获取请求参数字符串
     */
    public static String getParamStr(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        StringBuilder paramStr = new StringBuilder();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);

            if (paramValues != null) {
                for (String paramValue : paramValues) {
                    if (paramStr.length() > 0) {
                        paramStr.append("&");
                    }
                    paramStr.append(paramName).append("=").append(paramValue);
                }
            }
        }

        return paramStr.toString();
    }

    /**
     * 获取客户端IP地址
     */
    public static String getClientIP(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        try {
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

            // 如果是本地IP，尝试获取本机IP
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    log.warn("获取本机IP失败", e);
                }
            }

            // 对于多个代理的情况，取第一个IP
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            return ip;
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
            return "unknown";
        }
    }

}
