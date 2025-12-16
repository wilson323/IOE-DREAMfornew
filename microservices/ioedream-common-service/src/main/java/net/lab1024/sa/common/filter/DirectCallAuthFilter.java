package net.lab1024.sa.common.filter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.crypto.Mac;
import jakarta.crypto.spec.SecretKeySpec;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.config.DirectCallProperties;

/**
 * 直连调用S2S鉴权过滤器（HMAC）
 * <p>
 * 仅当请求带 X-Direct-Call=true 时启用校验；并要求路径在 allowlist 中。
 * </p>
 */
@Slf4j
public class DirectCallAuthFilter extends OncePerRequestFilter {

    private static final String DIRECT_CALL_HEADER = "X-Direct-Call";
    private static final String SOURCE_SERVICE_HEADER = "X-Source-Service";
    private static final String TIMESTAMP_HEADER = "X-Timestamp";
    private static final String NONCE_HEADER = "X-Nonce";
    private static final String SIGNATURE_HEADER = "X-Signature";

    private final DirectCallProperties properties;
    private final Map<String, Long> nonceCache = new ConcurrentHashMap<>();

    public DirectCallAuthFilter(DirectCallProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, java.io.IOException {

        if (!"true".equalsIgnoreCase(request.getHeader(DIRECT_CALL_HEADER))) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (!isAllowlisted(path)) {
            log.warn("[直连鉴权] 非白名单直连请求拦截: path={}", path);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String sourceService = request.getHeader(SOURCE_SERVICE_HEADER);
        String timestampStr = request.getHeader(TIMESTAMP_HEADER);
        String nonce = request.getHeader(NONCE_HEADER);
        String signature = request.getHeader(SIGNATURE_HEADER);

        if (sourceService == null || timestampStr == null || nonce == null || signature == null) {
            log.warn("[直连鉴权] 缺少必要头: source={}, path={}", sourceService, path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String secret = properties.getServiceSecrets().get(sourceService);
        if (secret == null || secret.isBlank()) {
            log.warn("[直连鉴权] 未配置sharedSecret: source={}, path={}", sourceService, path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        long now = Instant.now().toEpochMilli();
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (Math.abs(now - timestamp) > properties.getTimestampWindowMs()) {
            log.warn("[直连鉴权] 时间窗校验失败: source={}, path={}, ts={}", sourceService, path, timestamp);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String nonceKey = sourceService + ":" + nonce;
        if (isReplay(nonceKey, now)) {
            log.warn("[直连鉴权] nonce 重放: source={}, path={}", sourceService, path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String bodyHash = sha256Hex(new byte[0]); // 试点直连均为GET，无请求体
            String message = request.getMethod() + "\n" + path + "\n" + bodyHash + "\n" + timestamp + "\n" + nonce;
            String expected = hmacSha256Base64(secret, message);
            if (!constantTimeEquals(signature, expected)) {
                log.warn("[直连鉴权] 签名不匹配: source={}, path={}", sourceService, path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.error("[直连鉴权] 校验异常: source={}, path={}", sourceService, path, e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowlisted(String path) {
        for (String prefix : properties.getAllowlistPaths()) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isReplay(String key, long now) {
        Long existingExpire = nonceCache.putIfAbsent(key, now + properties.getNonceTtlMs());
        if (existingExpire != null && existingExpire > now) {
            return true;
        }
        if (nonceCache.size() > properties.getNonceCacheMaxSize()) {
            nonceCache.entrySet().removeIf(e -> e.getValue() < now);
        }
        return false;
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String hmacSha256Base64(String secret, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(raw);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

