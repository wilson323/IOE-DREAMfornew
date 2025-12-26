package net.lab1024.sa.common.util;

import net.lab1024.sa.common.auth.JwtTokenParserHolder;
import net.lab1024.sa.common.auth.UserContext;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求工具类
 * <p>
 * 提供HTTP请求相关的工具方法
 * 严格遵循CLAUDE.md规范：
 * - 工具类使用静态方法
 * - 完整的参数验证
 * - 统一的异常处理
 * </p>
 * <p>
 * 迁移说明：从microservices-common迁移到microservices-common-gateway-client
 * 原因：移除microservices-common聚合依赖后，需要为所有服务提供SmartRequestUtil访问
 * gateway-client是所有服务都依赖的细粒度模块，适合放置此类工具
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 * @updated 2025-12-22 P0架构违规修复，迁移到gateway-client模块
 */
@Slf4j
public class SmartRequestUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private SmartRequestUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 获取当前请求对象
     *
     * @return HttpServletRequest 当前请求对象
     * @throws IllegalStateException 当不存在请求上下文时抛出
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("当前线程不存在请求上下文");
        }
        return attributes.getRequest();
    }

    /**
     * 获取客户端IP地址
     *
     * @return 客户端IP地址
     */
    public static String getClientIpAddress() {
        try {
            HttpServletRequest request = getCurrentRequest();
            String ipAddress = request.getHeader("X-Forwarded-For");

            if (StringUtils.isNotBlank(ipAddress)) {
                // X-Forwarded-For可能包含多个IP，取第一个
                return ipAddress.split(",")[0].trim();
            }

            ipAddress = request.getHeader("X-Real-IP");
            if (StringUtils.isNotBlank(ipAddress)) {
                return ipAddress;
            }

            ipAddress = request.getHeader("Proxy-Client-IP");
            if (StringUtils.isNotBlank(ipAddress)) {
                return ipAddress;
            }

            ipAddress = request.getHeader("WL-Proxy-Client-IP");
            if (StringUtils.isNotBlank(ipAddress)) {
                return ipAddress;
            }

            return request.getRemoteAddr();

        } catch (Exception e) {
            log.warn("获取客户端IP地址失败", e);
            return "unknown";
        }
    }

    /**
     * 获取用户代理字符串
     *
     * @return User-Agent字符串
     */
    public static String getUserAgent() {
        try {
            HttpServletRequest request = getCurrentRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            log.warn("获取User-Agent失败", e);
            return "unknown";
        }
    }

    /**
     * 获取请求URI
     *
     * @return 请求URI
     */
    public static String getRequestUri() {
        try {
            HttpServletRequest request = getCurrentRequest();
            return request.getRequestURI();
        } catch (Exception e) {
            log.warn("获取请求URI失败", e);
            return "unknown";
        }
    }

    /**
     * 获取请求方法
     *
     * @return HTTP方法
     */
    public static String getRequestMethod() {
        try {
            HttpServletRequest request = getCurrentRequest();
            return request.getMethod();
        } catch (Exception e) {
            log.warn("获取请求方法失败", e);
            return "unknown";
        }
    }

    /**
     * 判断是否为Ajax请求
     *
     * @return true如果是Ajax请求
     */
    public static boolean isAjaxRequest() {
        try {
            HttpServletRequest request = getCurrentRequest();
            String requestedWith = request.getHeader("X-Requested-With");
            return "XMLHttpRequest".equals(requestedWith);
        } catch (Exception e) {
            log.warn("判断Ajax请求失败", e);
            return false;
        }
    }

    /**
     * 获取请求参数
     *
     * @param paramName 参数名
     * @return 参数值
     */
    public static String getParameter(String paramName) {
        try {
            HttpServletRequest request = getCurrentRequest();
            return request.getParameter(paramName);
        } catch (Exception e) {
            log.warn("获取请求参数失败: {}", paramName, e);
            return null;
        }
    }

    /**
     * 获取请求头
     *
     * @param headerName 请求头名称
     * @return 请求头值
     */
    public static String getHeader(String headerName) {
        try {
            HttpServletRequest request = getCurrentRequest();
            return request.getHeader(headerName);
        } catch (Exception e) {
            log.warn("获取请求头失败: {}", headerName, e);
            return null;
        }
    }

    /**
     * 获取上下文路径
     *
     * @return 上下文路径
     */
    public static String getContextPath() {
        try {
            HttpServletRequest request = getCurrentRequest();
            return request.getContextPath();
        } catch (Exception e) {
            log.warn("获取上下文路径失败", e);
            return "";
        }
    }

    /**
     * 获取完整的请求URL
     *
     * @return 完整的请求URL
     */
    public static String getFullUrl() {
        try {
            HttpServletRequest request = getCurrentRequest();
            StringBuilder url = new StringBuilder();
            url.append(request.getScheme()).append("://");
            url.append(request.getServerName());

            int port = request.getServerPort();
            if (port != 80 && port != 443) {
                url.append(":").append(port);
            }

            url.append(request.getRequestURI());

            String queryString = request.getQueryString();
            if (StringUtils.isNotBlank(queryString)) {
                url.append("?").append(queryString);
            }

            return url.toString();
        } catch (Exception e) {
            log.warn("获取完整URL失败", e);
            return "unknown";
        }
    }

    /**
     * 获取当前用户ID
     * <p>
     * 从请求头中获取用户ID（优先级从高到低）：
     * 1. X-User-Id请求头（手动设置的用户ID）
     * 2. Authorization请求头（JWT Token解析）
     * 3. Session中获取（如果有）
     * </p>
     *
     * @return 当前用户ID，如果无法获取则返回null
     */
    public static String getUserId() {
        try {
            HttpServletRequest request = getCurrentRequest();

            // 1. 优先从请求头获取用户ID（手动设置）
            String userId = request.getHeader("X-User-Id");
            if (StringUtils.isNotBlank(userId)) {
                log.debug("[请求工具] 从X-User-Id获取: userId={}", userId);
                return userId;
            }

            // 2. 从Authorization头中解析JWT Token
            String authorization = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authorization)) {
                // 检查JWT解析器是否已初始化
                if (JwtTokenParserHolder.isInitialized()) {
                    UserContext context = JwtTokenParserHolder.getParser().parseToken(authorization);
                    if (context != null && context.getUserId() != null) {
                        log.debug("[请求工具] 从JWT Token解析: userId={}", context.getUserId());
                        return String.valueOf(context.getUserId());
                    }
                } else {
                    log.debug("[请求工具] JWT解析器未初始化");
                }
            }

            // 3. 无法获取用户ID
            log.debug("[请求工具] 无法获取用户ID");
            return null;

        } catch (Exception e) {
            log.warn("[请求工具] 获取用户ID异常", e);
            return null;
        }
    }

    /**
     * 获取当前用户名
     * <p>
     * 从请求头中获取用户名（优先级从高到低）：
     * 1. X-User-Name请求头（手动设置的用户名）
     * 2. Authorization请求头（JWT Token解析）
     * 3. Session中获取（如果有）
     * </p>
     *
     * @return 当前用户名，如果无法获取则返回null
     */
    public static String getUserName() {
        try {
            HttpServletRequest request = getCurrentRequest();

            // 1. 优先从请求头获取用户名（手动设置）
            String userName = request.getHeader("X-User-Name");
            if (StringUtils.isNotBlank(userName)) {
                log.debug("[请求工具] 从X-User-Name获取: userName={}", userName);
                return userName;
            }

            // 2. 从Authorization头中解析JWT Token
            String authorization = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authorization)) {
                // 检查JWT解析器是否已初始化
                if (JwtTokenParserHolder.isInitialized()) {
                    UserContext context = JwtTokenParserHolder.getParser().parseToken(authorization);
                    if (context != null && StringUtils.isNotBlank(context.getUserName())) {
                        log.debug("[请求工具] 从JWT Token解析: userName={}", context.getUserName());
                        return context.getUserName();
                    }
                } else {
                    log.debug("[请求工具] JWT解析器未初始化");
                }
            }

            // 3. 无法获取用户名
            log.debug("[请求工具] 无法获取用户名");
            return null;

        } catch (Exception e) {
            log.warn("[请求工具] 获取用户名异常", e);
            return null;
        }
    }

    /**
     * 获取当前用户ID（Long类型）
     * <p>
     * 便捷方法，自动转换String类型为Long
     * 如果无法获取或转换失败，返回null
     * </p>
     *
     * @return 当前用户ID（Long类型），如果无法获取则返回null
     */
    public static Long getRequestUserId() {
        String userId = getUserId();
        if (StringUtils.isNotBlank(userId)) {
            try {
                return Long.parseLong(userId);
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userId);
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户名（便捷方法）
     * <p>
     * 与getUserName()功能相同，提供更符合命名习惯的方法名
     * </p>
     *
     * @return 当前用户名，如果无法获取则返回null
     */
    public static String getRequestUserName() {
        return getUserName();
    }
}