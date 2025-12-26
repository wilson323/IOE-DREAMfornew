package net.lab1024.sa.gateway.filter;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.gateway.config.RbacProperties;
import net.lab1024.sa.gateway.config.WebFluxSecurityConfig;
import reactor.core.publisher.Mono;

/**
 * 网关JWT鉴权全局过滤器（B1：网关统一鉴权）
 * <p>
 * - 白名单路径放行 - 非白名单路径强制校验 Authorization: Bearer <token> - 校验通过后将用户信息透传到下游（X-User-Id/X-User-Name 等）
 * </p>
 */
@Component
@Slf4j
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";

    private final AntPathMatcher pathMatcher = new AntPathMatcher ();

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private WebFluxSecurityConfig.WhitelistProperties whitelistProperties;

    @Resource
    private RbacProperties rbacProperties;

    @Override
    public int getOrder () {
        // 尽量靠前执行，避免无意义的下游转发
        return -100;
    }

    @Override
    public Mono<Void> filter (ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest ().getURI ().getPath ();

        if (isWhitelisted (path)) {
            return chain.filter (exchange);
        }

        String token = resolveBearerToken (exchange.getRequest ().getHeaders ());
        if (token == null) {
            return writeJson(exchange, HttpStatus.UNAUTHORIZED, ResponseDTO.error(401, "未登录或缺少令牌"));
        }

        if (!jwtTokenUtil.validateToken (token)) {
            return writeJson(exchange, HttpStatus.UNAUTHORIZED, ResponseDTO.error(401, "令牌无效或已过期"));
        }

        if (!jwtTokenUtil.isAccessToken (token)) {
            return writeJson(exchange, HttpStatus.UNAUTHORIZED, ResponseDTO.error(401, "令牌类型不支持"));
        }

        Map<String, Object> claims = jwtTokenUtil.parseJwtTokenToMap (token);
        if (claims == null) {
            return writeJson(exchange, HttpStatus.UNAUTHORIZED, ResponseDTO.error(401, "令牌解析失败"));
        }

        String userId = claims.get ("userId") == null ? null : String.valueOf (claims.get ("userId"));
        String username = claims.get ("username") == null ? null : String.valueOf (claims.get ("username"));

        String roles = flattenToCsv (claims.get ("roles"));
        String permissions = flattenToCsv (claims.get ("permissions"));

        if (isForbiddenByRbac (path, claims)) {
            return writeJson(exchange, HttpStatus.FORBIDDEN, ResponseDTO.error(403, "权限不足"));
        }

        ServerWebExchange mutated = exchange.mutate ().request (builder -> {
            if (userId != null) {
                builder.header (HEADER_USER_ID, userId);
            }
            if (username != null) {
                builder.header (HEADER_USER_NAME, username);
            }
            if (roles != null) {
                builder.header (HEADER_USER_ROLES, roles);
            }
            if (permissions != null) {
                builder.header (HEADER_USER_PERMISSIONS, permissions);
            }
        }).build ();

        return chain.filter (mutated);
    }

    private boolean isWhitelisted (String path) {
        LinkedHashSet<String> effective = new LinkedHashSet<> (Arrays.asList (getStaticWhitelistFallback ()));
        List<String> dynamic = whitelistProperties == null ? null : whitelistProperties.getPaths ();
        if (dynamic != null) {
            effective.addAll (dynamic);
        }
        for (String pattern : effective) {
            if (pattern == null || pattern.isBlank ()) {
                continue;
            }
            if (pathMatcher.match (pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private boolean isForbiddenByRbac (String path, Map<String, Object> claims) {
        if (rbacProperties == null || !rbacProperties.isEnabled ()) {
            return false;
        }

        List<RbacProperties.Rule> rules = rbacProperties.getRules ();
        if (rules == null || rules.isEmpty ()) {
            return false;
        }

        Set<String> userRoles = normalizeToSet (claims.get ("roles"));
        Set<String> userPermissions = normalizeToSet (claims.get ("permissions"));

        for (RbacProperties.Rule rule : rules) {
            if (rule == null) {
                continue;
            }

            if (!matchesAnyPathPattern (rule.getPathPatterns (), path)) {
                continue;
            }

            boolean hasCriteria = hasAnyCriteria (rule);
            if (!hasCriteria) {
                // 规则命中但未配置任何角色/权限要求：默认放行，避免误伤
                return false;
            }

            if (matchesRule (rule, userRoles, userPermissions)) {
                return false;
            }

            // 命中规则且不满足要求：拒绝
            return true;
        }

        // 未命中任何规则：不拦截（粗粒度RBAC只对配置的路径生效）
        return false;
    }

    private boolean matchesAnyPathPattern (List<String> patterns, String path) {
        if (patterns == null || patterns.isEmpty ()) {
            return false;
        }
        for (String pattern : patterns) {
            if (pattern == null || pattern.isBlank ()) {
                continue;
            }
            if (pathMatcher.match (pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnyCriteria (RbacProperties.Rule rule) {
        return (rule.getRequiredAnyRoles () != null && !rule.getRequiredAnyRoles ().isEmpty ())
                || (rule.getRequiredAnyPermissions () != null && !rule.getRequiredAnyPermissions ().isEmpty ())
                || (rule.getRequiredAnyPermissionPrefixes () != null
                        && !rule.getRequiredAnyPermissionPrefixes ().isEmpty ());
    }

    private static boolean matchesRule (RbacProperties.Rule rule, Set<String> userRoles, Set<String> userPermissions) {
        // 采用"任一满足即放行"的策略
        if (rule.getRequiredAnyRoles () != null && !rule.getRequiredAnyRoles ().isEmpty ()) {
            for (String required : rule.getRequiredAnyRoles ()) {
                if (required != null && userRoles.contains (required)) {
                    return true;
                }
            }
        }

        if (rule.getRequiredAnyPermissions () != null && !rule.getRequiredAnyPermissions ().isEmpty ()) {
            for (String required : rule.getRequiredAnyPermissions ()) {
                if (required != null && userPermissions.contains (required)) {
                    return true;
                }
            }
        }

        if (rule.getRequiredAnyPermissionPrefixes () != null && !rule.getRequiredAnyPermissionPrefixes ().isEmpty ()) {
            for (String prefix : rule.getRequiredAnyPermissionPrefixes ()) {
                if (prefix == null || prefix.isBlank ()) {
                    continue;
                }
                for (String perm : userPermissions) {
                    if (perm != null && perm.startsWith (prefix)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static Set<String> normalizeToSet (Object value) {
        if (value == null) {
            return Set.of ();
        }
        if (value instanceof List< ? > list) {
            LinkedHashSet<String> set = new LinkedHashSet<> ();
            for (Object item : list) {
                if (item == null) {
                    continue;
                }
                set.add (String.valueOf (item));
            }
            return set;
        }
        if (value instanceof String s) {
            if (s.isBlank ()) {
                return Set.of ();
            }
            String[] parts = s.split (",");
            LinkedHashSet<String> set = new LinkedHashSet<> ();
            for (String part : parts) {
                if (part != null && !part.isBlank ()) {
                    set.add (part.trim ());
                }
            }
            return set;
        }
        return Set.of (String.valueOf (value));
    }

    private String resolveBearerToken (HttpHeaders headers) {
        String auth = headers.getFirst (HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith (BEARER_PREFIX)) {
            String token = auth.substring (BEARER_PREFIX.length ()).trim ();
            return token.isEmpty () ? null : token;
        }

        // 兼容部分客户端用 token 头传递
        String token = headers.getFirst ("token");
        if (token != null && !token.isBlank ()) {
            return token.trim ();
        }
        return null;
    }

    private Mono<Void> writeJson (ServerWebExchange exchange, HttpStatus status, Object body) {
        try {
            byte[] bytes = objectMapper.writeValueAsString (body).getBytes (StandardCharsets.UTF_8);
            exchange.getResponse ().setStatusCode (status);
            exchange.getResponse ().getHeaders ().setContentType (MediaType.APPLICATION_JSON);
            exchange.getResponse ().getHeaders ().setCacheControl ("no-store");
            return exchange.getResponse ()
                    .writeWith (Mono.just (exchange.getResponse ().bufferFactory ().wrap (bytes)));
        } catch (Exception e) {
            log.error ("[GatewayAuth] write response failed: {}", e.getMessage (), e);
            exchange.getResponse ().setStatusCode (status);
            return exchange.getResponse ().setComplete ();
        }
    }

    private static String flattenToCsv (Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof List< ? > list) {
            if (list.isEmpty ()) {
                return null;
            }
            StringBuilder sb = new StringBuilder ();
            for (Object item : list) {
                if (item == null) {
                    continue;
                }
                if (!sb.isEmpty ()) {
                    sb.append (",");
                }
                sb.append (String.valueOf (item));
            }
            return sb.isEmpty () ? null : sb.toString ();
        }
        return String.valueOf (value);
    }

    private static String[] getStaticWhitelistFallback () {
        // 与 WebFluxSecurityConfig.STATIC_WHITE_LIST 保持一致（此处不直接访问私有常量，避免耦合）
        return new String[] { "/actuator/**", "/doc.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
                "/webjars/**", "/favicon.ico", "/api/v1/auth/**", "/login/**", "/public/**", "/static/**",
                "/gateway/health" };
    }
}