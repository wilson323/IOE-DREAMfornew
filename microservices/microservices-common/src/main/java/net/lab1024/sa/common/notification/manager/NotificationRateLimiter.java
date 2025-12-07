package net.lab1024.sa.common.notification.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * 通知限流管理器
 * <p>
 * 统一管理所有通知渠道的限流策略
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 企业级特性：
 * - 滑动窗口限流算法
 * - 支持多渠道独立限流
 * - 支持动态限流配置
 * - 线程安全实现
 * </p>
 * <p>
 * 限流策略（基于第三方API限制）：
 * - 钉钉：20条/分钟
 * - 企业微信：100条/分钟
 * - Webhook：无限制（可配置）
 * - 邮件：无限制（可配置）
 * - 短信：100条/分钟（可配置）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class NotificationRateLimiter {

    /**
     * 限流配置：渠道 -> 限流阈值（条/分钟）
     */
    private final Map<String, Integer> rateLimitConfig;

    /**
     * 限流窗口大小（毫秒）：60秒
     */
    private static final long RATE_LIMIT_WINDOW_MS = 60_000L;

    /**
     * 当前窗口计数：渠道 -> 计数
     */
    private final Map<String, Integer> currentWindowCount;

    /**
     * 当前窗口开始时间：渠道 -> 时间戳
     */
    private final Map<String, Long> currentWindowStartTime;

    /**
     * 构造函数
     * <p>
     * 初始化默认限流配置
     * </p>
     */
    public NotificationRateLimiter() {
        this.rateLimitConfig = new ConcurrentHashMap<>();
        this.currentWindowCount = new ConcurrentHashMap<>();
        this.currentWindowStartTime = new ConcurrentHashMap<>();

        // 初始化默认限流配置
        initDefaultRateLimitConfig();
    }

    /**
     * 构造函数（支持自定义限流配置）
     *
     * @param customRateLimitConfig 自定义限流配置
     */
    public NotificationRateLimiter(Map<String, Integer> customRateLimitConfig) {
        this.rateLimitConfig = new ConcurrentHashMap<>();
        this.currentWindowCount = new ConcurrentHashMap<>();
        this.currentWindowStartTime = new ConcurrentHashMap<>();

        // 初始化默认配置
        initDefaultRateLimitConfig();

        // 合并自定义配置
        if (customRateLimitConfig != null) {
            this.rateLimitConfig.putAll(customRateLimitConfig);
        }
    }

    /**
     * 初始化默认限流配置
     * <p>
     * 基于第三方API的实际限制
     * </p>
     */
    private void initDefaultRateLimitConfig() {
        rateLimitConfig.put("DINGTALK", 20);      // 钉钉：20条/分钟
        rateLimitConfig.put("WECHAT", 100);       // 企业微信：100条/分钟
        rateLimitConfig.put("WEBHOOK", Integer.MAX_VALUE);  // Webhook：无限制
        rateLimitConfig.put("EMAIL", Integer.MAX_VALUE);    // 邮件：无限制
        rateLimitConfig.put("SMS", 100);          // 短信：100条/分钟
    }

    /**
     * 检查是否允许发送
     * <p>
     * 使用滑动窗口算法实现限流
     * 线程安全实现
     * </p>
     *
     * @param channel 通知渠道（DINGTALK、WECHAT、WEBHOOK、EMAIL、SMS）
     * @return 是否允许发送
     */
    public synchronized boolean tryAcquire(String channel) {
        if (channel == null || channel.isEmpty()) {
            log.warn("[通知限流] 渠道为空，允许发送");
            return true;
        }

        String channelUpper = channel.toUpperCase();
        Integer limit = rateLimitConfig.get(channelUpper);

        // 如果未配置限流，允许发送
        if (limit == null || limit == Integer.MAX_VALUE) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        Long windowStartTime = currentWindowStartTime.get(channelUpper);
        Integer count = currentWindowCount.get(channelUpper);

        // 初始化窗口
        if (windowStartTime == null || count == null) {
            currentWindowStartTime.put(channelUpper, currentTime);
            currentWindowCount.put(channelUpper, 0);
            return true;
        }

        // 检查是否进入新的时间窗口
        if (currentTime - windowStartTime >= RATE_LIMIT_WINDOW_MS) {
            // 重置计数器和时间窗口
            currentWindowCount.put(channelUpper, 0);
            currentWindowStartTime.put(channelUpper, currentTime);
            log.debug("[通知限流] 进入新时间窗口，渠道：{}", channelUpper);
        }

        // 检查是否超过限流阈值
        int currentCount = currentWindowCount.get(channelUpper);
        if (currentCount >= limit) {
            log.warn("[通知限流] 超过限流阈值，渠道：{}，当前计数：{}，限流阈值：{}",
                    channelUpper, currentCount, limit);
            return false;
        }

        return true;
    }

    /**
     * 更新限流计数
     * <p>
     * 发送成功后更新当前窗口的计数
     * </p>
     *
     * @param channel 通知渠道
     */
    public synchronized void incrementCount(String channel) {
        if (channel == null || channel.isEmpty()) {
            return;
        }

        String channelUpper = channel.toUpperCase();
        Integer currentCount = currentWindowCount.get(channelUpper);
        if (currentCount == null) {
            currentWindowCount.put(channelUpper, 1);
        } else {
            currentWindowCount.put(channelUpper, currentCount + 1);
        }

        log.debug("[通知限流] 更新计数，渠道：{}，当前计数：{}",
                channelUpper, currentWindowCount.get(channelUpper));
    }

    /**
     * 获取当前限流计数
     *
     * @param channel 通知渠道
     * @return 当前计数
     */
    public int getCurrentCount(String channel) {
        if (channel == null || channel.isEmpty()) {
            return 0;
        }

        String channelUpper = channel.toUpperCase();
        Integer count = currentWindowCount.get(channelUpper);
        return count == null ? 0 : count;
    }

    /**
     * 获取限流阈值
     *
     * @param channel 通知渠道
     * @return 限流阈值（条/分钟）
     */
    public int getRateLimit(String channel) {
        if (channel == null || channel.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        String channelUpper = channel.toUpperCase();
        Integer limit = rateLimitConfig.get(channelUpper);
        return limit == null ? Integer.MAX_VALUE : limit;
    }

    /**
     * 更新限流配置
     * <p>
     * 支持动态更新限流阈值
     * </p>
     *
     * @param channel 通知渠道
     * @param limit   限流阈值（条/分钟）
     */
    public void updateRateLimit(String channel, Integer limit) {
        if (channel == null || channel.isEmpty() || limit == null || limit <= 0) {
            log.warn("[通知限流] 更新限流配置失败，参数无效，渠道：{}，限流阈值：{}", channel, limit);
            return;
        }

        String channelUpper = channel.toUpperCase();
        rateLimitConfig.put(channelUpper, limit);
        log.info("[通知限流] 更新限流配置，渠道：{}，新限流阈值：{}", channelUpper, limit);
    }

    /**
     * 重置限流计数
     * <p>
     * 用于测试或手动重置
     * </p>
     *
     * @param channel 通知渠道
     */
    public synchronized void resetCount(String channel) {
        if (channel == null || channel.isEmpty()) {
            return;
        }

        String channelUpper = channel.toUpperCase();
        currentWindowCount.put(channelUpper, 0);
        currentWindowStartTime.put(channelUpper, System.currentTimeMillis());
        log.info("[通知限流] 重置限流计数，渠道：{}", channelUpper);
    }
}
