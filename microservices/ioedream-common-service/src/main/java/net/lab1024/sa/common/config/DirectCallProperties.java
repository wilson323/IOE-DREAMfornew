package net.lab1024.sa.common.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 直连调用配置属性
 * <p>
 * 用于同域热路径白名单直连与S2S HMAC鉴权。
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "ioedream.direct-call")
public class DirectCallProperties {

    /**
     * 是否启用直连鉴权过滤器
     */
    private boolean enabled = false;

    /**
     * 允许直连的路径白名单（前缀匹配）
     */
    private List<String> allowlistPaths = new ArrayList<>(List.of(
            "/api/v1/account-kind/",
            "/api/v1/device/"
    ));

    /**
     * 服务 sharedSecret 映射，key为source serviceName
     */
    private Map<String, String> serviceSecrets = new HashMap<>();

    /**
     * 签名时间窗（毫秒），默认5分钟
     */
    private long timestampWindowMs = 300_000L;

    /**
     * nonce 过期时间（毫秒），默认5分钟
     */
    private long nonceTtlMs = 300_000L;

    /**
     * nonce 缓存最大条数（超过触发清理）
     */
    private int nonceCacheMaxSize = 10_000;
}

