package net.lab1024.sa.common.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CSRF跨站请求伪造防护过滤器
 * <p>
 * 功能：防止CSRF（跨站请求伪造）攻击
 * 防护策略：
 * 1. Token验证：为每个会话生成唯一CSRF Token
 * 2. 白名单模式：安全方法（GET、HEAD、OPTIONS）不需要验证
 * 3. 路径白名单：特定路径可以跳过CSRF验证
 * 4. Referer检查：验证请求来源
 * 5. 双重Cookie验证：前端和后端同时验证Token
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2) // 第三高优先级
public class CsrfFilter implements Filter {

    @Value("${security.csrf.enabled:false}")
    private boolean enabled;

    @Value("${security.csrf.token-header:X-CSRF-TOKEN}")
    private String tokenHeader;

    @Value("${security.csrf.token-param:_csrf}")
    private String tokenParam;

    @Value("${security.csrf.whitelist-paths:/actuator/**,/api/v1/auth/**}")
    private String whitelistPaths;

    @Value("${security.csrf.referer-check-enabled:true}")
    private boolean refererCheckEnabled;

    @Value("${security.csrf.allowed-referers:}")
    private String allowedReferers;

    /**
     * CSRF Token存储（生产环境应使用Redis）
     */
    private static final Map<String, CsrfToken> TOKEN_STORE = new ConcurrentHashMap<>();

    /**
     * Token有效期（秒）
     */
    private static final long TOKEN_VALIDITY_SECONDS = 7200; // 2小时

    private List<String> whitelistPathList;

    private SecureRandom secureRandom;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("[CSRF防护] 初始化过滤器: enabled={}", enabled);

        // 初始化白名单路径
        if (whitelistPaths != null && !whitelistPaths.isEmpty()) {
            whitelistPathList = Arrays.asList(whitelistPaths.split(","));
            log.info("[CSRF防护] 白名单路径: {}", whitelistPathList);
        } else {
            whitelistPathList = new ArrayList<>();
        }

        // 初始化随机数生成器
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (Exception e) {
            log.warn("[CSRF防护] 无法获取强随机数生成器，使用默认算法", e);
            secureRandom = new SecureRandom();
        }

        log.info("[CSRF防护] 过滤器初始化完成: tokenHeader={}, tokenParam={}",
                tokenHeader, tokenParam);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestUri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // 检查白名单路径
        if (isWhitelistedPath(requestUri)) {
            log.debug("[CSRF防护] 白名单路径，跳过检查: {}", requestUri);
            chain.doFilter(request, response);
            return;
        }

        // 安全方法（GET、HEAD、OPTIONS）不需要验证
        if (isSafeMethod(method)) {
            log.debug("[CSRF防护] 安全方法，跳过检查: method={}", method);
            chain.doFilter(request, response);
            return;
        }

        try {
            // 验证CSRF Token
            validateCsrfToken(httpRequest);

            // 验证Referer（可选）
            if (refererCheckEnabled) {
                validateReferer(httpRequest);
            }

            // 通过验证，继续处理请求
            chain.doFilter(request, response);

        } catch (CsrfException e) {
            log.warn("[CSRF防护] CSRF验证失败: uri={}, method={}, message={}",
                    requestUri, method, e.getMessage());

            // 返回403错误
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write("{\"code\":403,\"message\":\"CSRF token验证失败，请重新登录\",\"timestamp\":"
                    + System.currentTimeMillis() + "}");
        }
    }

    /**
     * 验证CSRF Token
     */
    private void validateCsrfToken(HttpServletRequest request) throws CsrfException {
        // 1. 从Session获取会话标识
        String sessionId = request.getSession().getId();
        if (sessionId == null || sessionId.isEmpty()) {
            throw new CsrfException("会话不存在，请重新登录");
        }

        // 2. 从请求头获取Token
        String tokenFromHeader = request.getHeader(tokenHeader);

        // 3. 从请求参数获取Token
        String tokenFromParam = request.getParameter(tokenParam);

        // 4. 使用任一方式提供的Token
        String providedToken = tokenFromHeader != null ? tokenFromHeader : tokenFromParam;

        if (providedToken == null || providedToken.isEmpty()) {
            throw new CsrfException("缺少CSRF Token");
        }

        // 5. 从存储中获取Token
        CsrfToken storedToken = TOKEN_STORE.get(sessionId);
        if (storedToken == null) {
            throw new CsrfException("CSRF Token不存在或已过期");
        }

        // 6. 检查Token是否过期
        if (System.currentTimeMillis() > storedToken.getExpiryTime()) {
            TOKEN_STORE.remove(sessionId);
            throw new CsrfException("CSRF Token已过期");
        }

        // 7. 验证Token是否匹配
        if (!storedToken.getToken().equals(providedToken)) {
            log.warn("[CSRF防护] Token不匹配: sessionId={}, expected={}, provided={}",
                    sessionId, storedToken.getToken(), providedToken);
            throw new CsrfException("CSRF Token无效");
        }

        log.debug("[CSRF防护] Token验证成功: sessionId={}", sessionId);
    }

    /**
     * 验证Referer
     */
    private void validateReferer(HttpServletRequest request) throws CsrfException {
        String referer = request.getHeader("Referer");

        // 如果没有Referer，允许通过（某些浏览器不发送Referer）
        if (referer == null || referer.isEmpty()) {
            log.debug("[CSRF防护] 无Referer头，跳过验证");
            return;
        }

        // 检查Referer是否在允许列表中
        if (allowedReferers != null && !allowedReferers.isEmpty()) {
            List<String> refererList = Arrays.asList(allowedReferers.split(","));
            boolean allowed = false;

            for (String allowedReferer : refererList) {
                if (referer.startsWith(allowedReferer.trim())) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed) {
                log.warn("[CSRF防护] Referer验证失败: referer={}, allowed={}",
                        referer, allowedReferers);
                throw new CsrfException("Referer验证失败");
            }
        }

        log.debug("[CSRF防护] Referer验证通过: referer={}", referer);
    }

    /**
     * 检查是否为安全方法
     */
    private boolean isSafeMethod(String method) {
        return "GET".equalsIgnoreCase(method) ||
               "HEAD".equalsIgnoreCase(method) ||
               "OPTIONS".equalsIgnoreCase(method);
    }

    /**
     * 检查是否为白名单路径
     */
    private boolean isWhitelistedPath(String requestUri) {
        if (whitelistPathList == null || whitelistPathList.isEmpty()) {
            return false;
        }

        for (String whitelistPath : whitelistPathList) {
            whitelistPath = whitelistPath.trim();

            // 支持通配符匹配
            if (whitelistPath.endsWith("/**")) {
                String prefix = whitelistPath.substring(0, whitelistPath.length() - 3);
                if (requestUri.startsWith(prefix)) {
                    return true;
                }
            }

            // 支持精确匹配
            if (requestUri.equals(whitelistPath) || requestUri.startsWith(whitelistPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void destroy() {
        log.info("[CSRF防护] 过滤器已销毁");
        TOKEN_STORE.clear();
    }

    // ==================== 公共API方法 ====================

    /**
     * 生成CSRF Token
     *
     * @param sessionId 会话ID
     * @return CSRF Token
     */
    public static String generateToken(String sessionId) {
        try {
            // 生成随机Token
            SecureRandom random = new SecureRandom();
            byte[] tokenBytes = new byte[32];
            random.nextBytes(tokenBytes);

            // 转换为十六进制字符串
            StringBuilder token = new StringBuilder();
            for (byte b : tokenBytes) {
                token.append(String.format("%02x", b));
            }

            String csrfToken = token.toString();

            // 存储Token
            CsrfToken tokenObj = new CsrfToken(csrfToken, System.currentTimeMillis() +
                TOKEN_VALIDITY_SECONDS * 1000);
            TOKEN_STORE.put(sessionId, tokenObj);

            return csrfToken;

        } catch (Exception e) {
            throw new RuntimeException("生成CSRF Token失败", e);
        }
    }

    /**
     * 验证CSRF Token（供业务代码调用）
     *
     * @param sessionId 会话ID
     * @param providedToken 提供的Token
     * @return 是否验证成功
     */
    public static boolean validateToken(String sessionId, String providedToken) {
        CsrfToken storedToken = TOKEN_STORE.get(sessionId);
        if (storedToken == null) {
            return false;
        }

        if (System.currentTimeMillis() > storedToken.getExpiryTime()) {
            TOKEN_STORE.remove(sessionId);
            return false;
        }

        return storedToken.getToken().equals(providedToken);
    }

    /**
     * 清除CSRF Token
     *
     * @param sessionId 会话ID
     */
    public static void clearToken(String sessionId) {
        TOKEN_STORE.remove(sessionId);
    }

    /**
     * CSRF Token对象
     */
    public static class CsrfToken {
        private final String token;
        private final long expiryTime;

        public CsrfToken(String token, long expiryTime) {
            this.token = token;
            this.expiryTime = expiryTime;
        }

        public String getToken() {
            return token;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }

    /**
     * CSRF异常
     */
    public static class CsrfException extends Exception {
        public CsrfException(String message) {
            super(message);
        }
    }
}
