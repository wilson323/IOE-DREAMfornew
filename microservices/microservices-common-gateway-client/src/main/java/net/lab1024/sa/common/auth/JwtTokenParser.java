package net.lab1024.sa.common.auth;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * JWT Token解析器
 *
 * <p>负责解析和验证JWT Token，提取用户身份信息</p>
 *
 * @author IOE-DREAM Team
 * @since 1.0.0
 */
@Slf4j
public class JwtTokenParser {

    /**
     * 解析JWT Token，提取用户身份信息
     *
     * @param token JWT Token字符串（格式：Bearer {token} 或纯token）
     * @return 用户上下文信息，解析失败返回null
     */
    public UserContext parseToken(String token) {
        try {
            // 1. 参数校验
            if (StringUtils.isBlank(token)) {
                log.debug("[JWT解析] Token为空");
                return null;
            }

            // 2. 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 3. 使用Sa-Token解析 (适配1.34.0 API)
            String loginId = StpUtil.getLoginIdAsString();

            if (loginId == null) {
                log.warn("[JWT解析] Token无效或已过期");
                return null;
            }

            // 4. 构建用户上下文
            UserContext context = new UserContext();
            try {
                context.setUserId(Long.parseLong(loginId));
            } catch (NumberFormatException e) {
                context.setUserId(null);
            }
            context.setUserName(loginId);

            log.info("[JWT解析] 解析成功: userId={}, userName={}",
                     context.getUserId(), context.getUserName());

            return context;

        } catch (Exception e) {
            log.error("[JWT解析] 解析异常: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token字符串
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                return false;
            }

            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            boolean isLogin = StpUtil.isLogin();

            return isLogin;

        } catch (Exception e) {
            log.error("[JWT解析] Token验证失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token JWT Token字符串
     * @return 用户ID，解析失败返回null
     */
    public Long extractUserId(String token) {
        UserContext context = parseToken(token);
        return context != null ? context.getUserId() : null;
    }

    /**
     * 从Token中提取用户名
     *
     * @param token JWT Token字符串
     * @return 用户名，解析失败返回null
     */
    public String extractUserName(String token) {
        UserContext context = parseToken(token);
        return context != null ? context.getUserName() : null;
    }
}
