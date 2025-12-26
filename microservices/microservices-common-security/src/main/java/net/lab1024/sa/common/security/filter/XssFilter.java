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
 * XSS跨站脚本防护过滤器
 * <p>
 * 功能：检测和阻止XSS（跨站脚本）攻击
 * 防护策略：
 * 1. 脚本标签检测：检测&lt;script&gt;等危险标签
 * 2. 事件处理器检测：检测onclick、onerror等事件
 * 3. JavaScript协议检测：检测javascript:等伪协议
 * 4. CSS表达式检测：检测expression()等危险CSS
 * 5. HTML实体编码验证：检测HTML实体注入
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // 第二高优先级
public class XssFilter implements Filter {

    @Value("${security.xss.enabled:true}")
    private boolean enabled;

    @Value("${security.xss.whitelist-paths:/actuator/**,/doc.html,/swagger-ui/**}")
    private String whitelistPaths;

    /**
     * XSS攻击模式正则表达式
     */
    private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
            // Script标签
            Pattern.compile("(?i)<\\s*script[^>]*>.*?<\\s*/\\s*script>"),
            Pattern.compile("(?i)<\\s*script[^>]*>"),
            // 事件处理器
            Pattern.compile("(?i)\\s(on\\w+)\\s*="),
            // JavaScript伪协议
            Pattern.compile("(?i)javascript:"),
            Pattern.compile("(?i)vbscript:"),
            Pattern.compile("(?i)data:text/html"),
            // iframe标签
            Pattern.compile("(?i)<\\s*iframe[^>]*>"),
            // object/embed标签
            Pattern.compile("(?i)<\\s*(object|embed)[^>]*>"),
            // style标签和expression()
            Pattern.compile("(?i)<\\s*style[^>]*>.*?expression\\("),
            Pattern.compile("(?i)expression\\s*\\("),
            Pattern.compile("(?i)behavior:"),
            // meta标签
            Pattern.compile("(?i)<\\s*meta[^>]*http-equiv\\s*=\\s*[\"']?refresh"),
            // img标签动态内容
            Pattern.compile("(?i)<\\s*img[^>]*src\\s*=\\s*[\"']?javascript:"),
            // 链接动态内容
            Pattern.compile("(?i)<\\s*a[^>]*href\\s*=\\s*[\"']?javascript:"),
            // HTML实体注入
            Pattern.compile("&#\\d+;"),
            Pattern.compile("&#x[0-9a-f]+;"),
            // SVG脚本
            Pattern.compile("(?i)<\\s*svg[^>]*>.*?<\\s*script[^>]*>"),
            // eval函数
            Pattern.compile("(?i)eval\\s*\\("),
            // fromCharCode函数
            Pattern.compile("(?i)String\\.fromCharCode"),
            // document.cookie访问
            Pattern.compile("(?i)document\\.cookie"),
            // document.location访问
            Pattern.compile("(?i)document\\.location"),
            // window.location访问
            Pattern.compile("(?i)window\\.location")
    );

    /**
     * 允许的HTML标签（白名单）
     */
    private static final Set<String> ALLOWED_HTML_TAGS = new HashSet<>(Arrays.asList(
            "p", "br", "b", "i", "u", "strong", "em", "span", "div",
            "h1", "h2", "h3", "h4", "h5", "h6",
            "ul", "ol", "li",
            "table", "tr", "td", "th", "thead", "tbody"
    ));

    private List<String> whitelistPathList;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("[XSS防护] 初始化过滤器: enabled={}", enabled);

        // 初始化白名单路径
        if (whitelistPaths != null && !whitelistPaths.isEmpty()) {
            whitelistPathList = Arrays.asList(whitelistPaths.split(","));
            log.info("[XSS防护] 白名单路径: {}", whitelistPathList);
        } else {
            whitelistPathList = new ArrayList<>();
        }

        log.info("[XSS防护] 过滤器初始化完成");
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
            log.debug("[XSS防护] 白名单路径，跳过检查: {}", requestUri);
            chain.doFilter(request, response);
            return;
        }

        try {
            // 检查所有参数
            checkParameters(httpRequest);

            // 检查查询字符串
            checkQueryString(httpRequest);

            // 通过检查，继续处理请求
            chain.doFilter(request, response);

        } catch (XssException e) {
            log.warn("[XSS防护] 检测到XSS攻击: uri={}, message={}",
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
    private void checkParameters(HttpServletRequest request) throws XssException {
        Map<String, String[]> parameterMap = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();

            for (String paramValue : paramValues) {
                if (paramValue != null && !paramValue.isEmpty()) {
                    checkForXss(paramName, paramValue);
                }
            }
        }
    }

    /**
     * 检查查询字符串
     */
    private void checkQueryString(HttpServletRequest request) throws XssException {
        String queryString = request.getQueryString();

        if (queryString != null && !queryString.isEmpty()) {
            // URL解码查询字符串
            try {
                String decodedQuery = java.net.URLDecoder.decode(queryString, "UTF-8");
                checkForXss("queryString", decodedQuery);
            } catch (Exception e) {
                log.debug("[XSS防护] 查询字符串解码失败: {}", queryString);
            }
        }
    }

    /**
     * 检测XSS攻击
     *
     * @param paramName 参数名
     * @param paramValue 参数值
     * @throws XssException 如果检测到XSS攻击
     */
    private void checkForXss(String paramName, String paramValue) throws XssException {
        if (paramValue == null || paramValue.isEmpty()) {
            return;
        }

        // 1. 使用正则表达式检查XSS模式
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(paramValue).find()) {
                throw new XssException(
                    String.format("参数[%s]包含XSS攻击模式[%s]: %s",
                        paramName, pattern.pattern(), paramValue)
                );
            }
        }

        // 2. 检查HTML标签是否在白名单中
        if (containsHtmlTags(paramValue)) {
            if (!containsOnlyAllowedTags(paramValue)) {
                throw new XssException(
                    String.format("参数[%s]包含不允许的HTML标签: %s", paramName, paramValue)
                );
            }
        }

        log.debug("[XSS防护] 参数检查通过: paramName={}, value={}", paramName, paramValue);
    }

    /**
     * 检查是否包含HTML标签
     */
    private boolean containsHtmlTags(String value) {
        return value.matches(".*<[^>]+>.*");
    }

    /**
     * 检查是否只包含允许的HTML标签
     */
    private boolean containsOnlyAllowedTags(String value) {
        // 简单实现：如果包含任何标签，都要求严格验证
        // 实际项目中应该使用HTML清理库（如JSoup）
        return false;
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
        log.info("[XSS防护] 过滤器已销毁");
    }

    /**
     * XSS异常
     */
    public static class XssException extends Exception {
        public XssException(String message) {
            super(message);
        }
    }
}
