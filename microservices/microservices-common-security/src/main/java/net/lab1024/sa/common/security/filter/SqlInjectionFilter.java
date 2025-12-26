package net.lab1024.sa.common.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * SQL注入防护过滤器
 * <p>
 * 功能：检测和阻止SQL注入攻击
 * 防护策略：
 * 1. 参数值检测：检查所有请求参数是否包含SQL关键词
 * 2. 正则表达式匹配：识别常见SQL注入模式
 * 3. 白名单模式：允许特定路径绕过检查
 * 4. 敏感词检测：识别UNION、DROP、EXEC等危险关键词
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级
public class SqlInjectionFilter implements Filter {

    @Value("${security.sql-injection.enabled:true}")
    private boolean enabled;

    @Value("${security.sql-injection.whitelist-paths:/actuator/**,/doc.html,/swagger-ui/**}")
    private String whitelistPaths;

    /**
     * SQL注入危险关键词
     */
    private static final Set<String> DANGEROUS_KEYWORDS = new HashSet<>(Arrays.asList(
            "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "TRUNCATE",
            "ALTER", "CREATE", "EXEC", "EXECUTE", "UNION", "SCRIPT",
            "JAVASCRIPT", "DECLARE", "TABLE", "GRANT", "REVOKE",
            "SHUTDOWN", "--", ";--", "/*", "*/", "XP_", "SP_",
            "0x", "CHAR(", "VARCHAR", "NVARCHAR", "CAST(", "CONVERT(",
            "WAITFOR", "DELAY", "BENCHMARK", "SLEEP(", "COUNT("
    ));

    /**
     * SQL注入正则表达式模式
     */
    private static final List<Pattern> SQL_INJECTION_PATTERNS = Arrays.asList(
            // SQL注释模式
            Pattern.compile("(?i)(\\-\\-|\\/\\*|\\*\\/|;|#)"),
            // UNION查询
            Pattern.compile("(?i)\\b(UNION|JOIN)\\s+(ALL|SELECT|DISTINCT)"),
            // 条件注入
            Pattern.compile("(?i)\\b(OR|AND)\\s+\\d+\\s*=\\s*\\d+"),
            Pattern.compile("(?i)\\b(OR|AND)\\s+['\"]?\\w+['\"]?\\s*=\\s*['\"]?\\w+['\"]?"),
            // 函数调用
            Pattern.compile("(?i)(EXEC|EXECUTE)\\s*\\("),
            Pattern.compile("(?i)(COUNT|SUM|AVG|MIN|MAX|GROUP|ORDER)\\s*\\("),
            // 时间延迟攻击
            Pattern.compile("(?i)(WAITFOR|DELAY|SLEEP|BENCHMARK)\\s*\\("),
            // 十六进制编码
            Pattern.compile("(?i)0x[0-9a-f]+"),
            // 字符编码攻击
            Pattern.compile("(?i)(CHAR|VARCHAR|NVARCHAR|CAST|CONVERT)\\s*\\("),
            // 存储过程调用
            Pattern.compile("(?i)(XP_|SP_)\\w+"),
            // IF语句
            Pattern.compile("(?i)\\bIF\\s*\\("),
            // 子查询
            Pattern.compile("(?i)\\(\\s*SELECT"),
            // CONCAT攻击
            Pattern.compile("(?i)CONCAT\\s*\\(")
    );

    private List<String> whitelistPathList;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("[SQL注入防护] 初始化过滤器: enabled={}", enabled);

        // 初始化白名单路径
        if (whitelistPaths != null && !whitelistPaths.isEmpty()) {
            whitelistPathList = Arrays.asList(whitelistPaths.split(","));
            log.info("[SQL注入防护] 白名单路径: {}", whitelistPathList);
        } else {
            whitelistPathList = new ArrayList<>();
        }

        log.info("[SQL注入防护] 过滤器初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestUri = httpRequest.getRequestURI();

        // 检查白名单
        if (isWhitelistedPath(requestUri)) {
            log.debug("[SQL注入防护] 白名单路径，跳过检查: {}", requestUri);
            chain.doFilter(request, response);
            return;
        }

        try {
            // 检查所有参数
            checkParameters(httpRequest);

            // 检查请求头
            checkHeaders(httpRequest);

            // 检查请求体（仅针对POST/PUT）
            String method = httpRequest.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                checkRequestBody(httpRequest);
            }

            // 通过检查，继续处理请求
            chain.doFilter(request, response);

        } catch (SqlInjectionException e) {
            log.warn("[SQL注入防护] 检测到SQL注入攻击: uri={}, message={}",
                    requestUri, e.getMessage());

            // 返回400错误
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":400,\"message\":\"请求参数包含非法字符，请联系管理员\",\"timestamp\":"
                    + System.currentTimeMillis() + "}");
        }
    }

    /**
     * 检查请求参数
     */
    private void checkParameters(HttpServletRequest request) throws SqlInjectionException {
        Map<String, String[]> parameterMap = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();

            for (String paramValue : paramValues) {
                if (paramValue != null && !paramValue.isEmpty()) {
                    checkForSqlInjection(paramName, paramValue);
                }
            }
        }
    }

    /**
     * 检查请求头
     */
    private void checkHeaders(HttpServletRequest request) throws SqlInjectionException {
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // 跳过User-Agent等正常请求头
            if ("User-Agent".equalsIgnoreCase(headerName) ||
                "Accept".equalsIgnoreCase(headerName) ||
                "Accept-Language".equalsIgnoreCase(headerName) ||
                "Accept-Encoding".equalsIgnoreCase(headerName)) {
                continue;
            }

            if (headerValue != null && !headerValue.isEmpty()) {
                checkForSqlInjection(headerName, headerValue);
            }
        }
    }

    /**
     * 检查请求体（包装请求以读取请求体）
     */
    private void checkRequestBody(HttpServletRequest request) throws SqlInjectionException {
        // 请求体检查在XSS过滤器中统一处理
        // 这里只记录日志
        log.debug("[SQL注入防护] 跳过请求体检查（由XSS过滤器处理）");
    }

    /**
     * 检测SQL注入
     *
     * @param paramName 参数名
     * @param paramValue 参数值
     * @throws SqlInjectionException 如果检测到SQL注入
     */
    private void checkForSqlInjection(String paramName, String paramValue) throws SqlInjectionException {
        if (paramValue == null || paramValue.isEmpty()) {
            return;
        }

        String upperValue = paramValue.toUpperCase();

        // 1. 检查危险关键词
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperValue.contains(keyword)) {
                throw new SqlInjectionException(
                    String.format("参数[%s]包含危险关键词[%s]: %s", paramName, keyword, paramValue)
                );
            }
        }

        // 2. 使用正则表达式检查SQL注入模式
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(paramValue).find()) {
                throw new SqlInjectionException(
                    String.format("参数[%s]匹配SQL注入模式[%s]: %s", paramName, pattern.pattern(), paramValue)
                );
            }
        }

        // 3. 检查单引号平衡（简单的SQL注入检测）
        long quoteCount = paramValue.chars().filter(ch -> ch == '\'').count();
        if (quoteCount > 2) {
            throw new SqlInjectionException(
                String.format("参数[%s]包含过多单引号[%d]: %s", paramName, quoteCount, paramValue)
            );
        }

        log.debug("[SQL注入防护] 参数检查通过: paramName={}, value={}", paramName, paramValue);
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
        log.info("[SQL注入防护] 过滤器已销毁");
    }

    /**
     * SQL注入异常
     */
    public static class SqlInjectionException extends Exception {
        public SqlInjectionException(String message) {
            super(message);
        }
    }
}
