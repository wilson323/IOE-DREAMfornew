package net.lab1024.sa.common.permission.audit;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.permission.domain.dto.PermissionAuditDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 权限操作审计日志记录器
 * <p>
 * 企业级权限审计日志管理，提供：
 * - 异步审计日志记录，不阻塞业务流程
 * - 审计日志批量处理，提升性能
 * - 敏感操作强化审计，记录详细信息
 * - 审计日志持久化存储，支持合规要求
 * - 实时审计统计分析，支持监控告警
 * - 审计日志检索查询，支持事后追溯
 * </p>
 * <p>
 * 审计日志内容：
 * 1. 用户信息：操作用户ID、用户名、用户角色
 * 2. 操作信息：操作类型、资源、权限、结果
 * 3. 环境信息：IP地址、User-Agent、时间戳
 * 4. 性能信息：权限验证耗时、缓存命中情况
 * 5. 异常信息：失败原因、异常堆栈（脱敏）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class PermissionAuditLogger {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PermissionAuditConfig config;
    private final BlockingQueue<PermissionAuditDTO> auditQueue;
    private final Executor auditExecutor;

    // Redis键前缀
    private static final String AUDIT_LOG_KEY_PREFIX = "audit:permission:log:";
    private static final String AUDIT_STATS_KEY_PREFIX = "audit:permission:stats:";
    private static final String AUDIT_ALERT_KEY_PREFIX = "audit:permission:alert:";

    // 敏感操作类型（需要强化审计）
    private static final List<String> SENSITIVE_OPERATIONS = List.of(
        "GRANT_ADMIN_PERMISSION",
        "REVOKE_ADMIN_PERMISSION",
        "MODIFY_USER_ROLES",
        "BULK_PERMISSION_CHANGE",
        "SYSTEM_CONFIG_CHANGE"
    );

    /**
     * 构造函数
     */
    public PermissionAuditLogger(RedisTemplate<String, Object> redisTemplate,
                               PermissionAuditConfig config,
                               Executor auditExecutor) {
        this.redisTemplate = redisTemplate;
        this.config = config;
        this.auditExecutor = auditExecutor;
        this.auditQueue = new LinkedBlockingQueue<>(config.getQueueSize());

        // 启动审计日志处理线程
        startAuditProcessor();
    }

    /**
     * 记录权限验证审计日志
     *
     * @param auditDTO 审计日志数据
     */
    @Async("auditExecutor")
    public void logPermissionValidation(PermissionAuditDTO auditDTO) {
        if (!config.isEnableAudit()) {
            return;
        }

        try {
            // 设置审计时间戳
            if (auditDTO.getAuditTime() == null) {
                auditDTO.setAuditTime(LocalDateTime.now());
            }

            // 增强敏感操作审计
            if (isSensitiveOperation(auditDTO.getOperation())) {
                enhanceSensitiveAudit(auditDTO);
            }

            // 数据脱敏处理
            if (config.isEnableDataMasking()) {
                maskSensitiveData(auditDTO);
            }

            // 异步处理审计日志
            boolean queued = auditQueue.offer(auditDTO, config.getQueueTimeout(), TimeUnit.MILLISECONDS);
            if (!queued) {
                log.warn("[权限审计] 审计队列已满，丢弃审计日志: {}", auditDTO);
                // 记录队列满的统计
                recordAuditStats("queue_full", 1);
            } else {
                recordAuditStats("audit_queued", 1);
            }

        } catch (Exception e) {
            log.error("[权限审计] 记录审计日志异常", e);
            recordAuditStats("audit_error", 1);
        }
    }

    /**
     * 记录权限验证失败审计
     *
     * @param userId 用户ID
     * @param operation 操作类型
     * @param resource 资源
     * @param reason 失败原因
     */
    public void logPermissionDenied(Long userId, String operation, String resource, String reason) {
        PermissionAuditDTO auditDTO = PermissionAuditDTO.builder()
            .userId(userId)
            .operation(operation)
            .resource(resource)
            .result("DENIED")
            .reason(reason)
            .build();

        logPermissionValidation(auditDTO);

        // 检查是否需要触发告警
        checkAndTriggerAlert(userId, operation, resource, reason);
    }

    /**
     * 记录权限验证成功审计
     *
     * @param userId 用户ID
     * @param operation 操作类型
     * @param resource 资源
     * @param validationTime 验证耗时（毫秒）
     */
    public void logPermissionGranted(Long userId, String operation, String resource, Long validationTime) {
        PermissionAuditDTO auditDTO = PermissionAuditDTO.builder()
            .userId(userId)
            .operation(operation)
            .resource(resource)
            .result("GRANTED")
            .duration(validationTime)
            .build();

        logPermissionValidation(auditDTO);
    }

    /**
     * 批量记录审计日志
     *
     * @param auditLogs 审计日志列表
     */
    @Async("auditExecutor")
    public void logBatchPermissionValidation(List<PermissionAuditDTO> auditLogs) {
        if (auditLogs == null || auditLogs.isEmpty()) {
            return;
        }

        try {
            for (PermissionAuditDTO auditLog : auditLogs) {
                logPermissionValidation(auditLog);
            }

            recordAuditStats("batch_audit", auditLogs.size());
            log.debug("[权限审计] 批量记录审计日志: count={}", auditLogs.size());

        } catch (Exception e) {
            log.error("[权限审计] 批量记录审计日志异常", e);
            recordAuditStats("batch_audit_error", 1);
        }
    }

    /**
     * 查询审计日志
     *
     * @param userId 用户ID（可选）
     * @param operation 操作类型（可选）
     * @param resource 资源（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 审计日志列表
     */
    public List<PermissionAuditDTO> queryAuditLogs(Long userId, String operation, String resource,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 int pageNum, int pageSize) {
        try {
            // 构建查询键模式
            String pattern = buildQueryPattern(userId, operation, resource);

            // 从Redis查询审计日志
            List<Object> logs = redisTemplate.opsForValue().multiGet(
                redisTemplate.keys(pattern + "*")
            );

            List<PermissionAuditDTO> result = new ArrayList<>();
            if (logs != null) {
                for (Object log : logs) {
                    if (log instanceof PermissionAuditDTO) {
                        PermissionAuditDTO auditLog = (PermissionAuditDTO) log;

                        // 时间范围过滤
                        if (isInTimeRange(auditLog.getAuditTime(), startTime, endTime)) {
                            result.add(auditLog);
                        }
                    }
                }
            }

            // 分页处理
            int startIndex = (pageNum - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, result.size());

            if (startIndex >= result.size()) {
                return new ArrayList<>();
            }

            return result.subList(startIndex, endIndex);

        } catch (Exception e) {
            log.error("[权限审计] 查询审计日志异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取审计统计信息
     *
     * @param userId 用户ID（可选，为空则全局统计）
     * @return 统计信息
     */
    public PermissionAuditStats getAuditStats(Long userId) {
        try {
            String statsKey = buildStatsKey(userId);
            Object statsObj = redisTemplate.opsForValue().get(statsKey);

            if (statsObj instanceof PermissionAuditStats) {
                return (PermissionAuditStats) statsObj;
            }

            // 如果没有统计数据，实时计算
            return calculateRealTimeStats(userId);

        } catch (Exception e) {
            log.error("[权限审计] 获取审计统计信息异常", e);
            return new PermissionAuditStats();
        }
    }

    /**
     * 启动审计日志处理器
     */
    private void startAuditProcessor() {
        auditExecutor.execute(() -> {
            List<PermissionAuditDTO> batch = new ArrayList<>(config.getBatchSize());

            while (true) {
                try {
                    // 从队列中取出审计日志
                    PermissionAuditDTO auditDTO = auditQueue.poll(config.getPollTimeout(), TimeUnit.MILLISECONDS);

                    if (auditDTO != null) {
                        batch.add(auditDTO);
                    }

                    // 批量处理条件：达到批次大小或等待超时
                    if (batch.size() >= config.getBatchSize() ||
                        (auditDTO == null && !batch.isEmpty())) {
                        processBatchAuditLogs(batch);
                        batch.clear();
                    }

                } catch (InterruptedException e) {
                    log.warn("[权限审计] 审计处理器被中断");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("[权限审计] 审计处理器异常", e);
                }
            }
        });

        log.info("[权限审计] 审计日志处理器已启动");
    }

    /**
     * 批量处理审计日志
     */
    private void processBatchAuditLogs(List<PermissionAuditDTO> auditLogs) {
        if (auditLogs.isEmpty()) {
            return;
        }

        try {
            for (PermissionAuditDTO auditLog : auditLogs) {
                // 存储到Redis
                String auditKey = buildAuditKey(auditLog);
                redisTemplate.opsForValue().set(
                    auditKey,
                    auditLog,
                    config.getAuditRetentionPeriod()
                );

                // 更新统计信息
                updateAuditStats(auditLog);
            }

            recordAuditStats("batch_processed", auditLogs.size());
            log.debug("[权限审计] 批量处理审计日志完成: count={}", auditLogs.size());

        } catch (Exception e) {
            log.error("[权限审计] 批量处理审计日志异常", e);
            recordAuditStats("batch_process_error", 1);
        }
    }

    /**
     * 检查是否为敏感操作
     */
    private boolean isSensitiveOperation(String operation) {
        return SENSITIVE_OPERATIONS.contains(operation);
    }

    /**
     * 增强敏感操作审计
     */
    private void enhanceSensitiveAudit(PermissionAuditDTO auditDTO) {
        auditDTO.setSensitive(true);

        // 敏感操作需要更详细的信息
        auditDTO.setEnhancedLogging(true);

        // 记录完整的上下文信息
        log.info("[权限审计-敏感] 敏感操作审计: userId={}, operation={}, resource={}, result={}",
                auditDTO.getUserId(), auditDTO.getOperation(),
                auditDTO.getResource(), auditDTO.getResult());
    }

    /**
     * 数据脱敏处理
     */
    private void maskSensitiveData(PermissionAuditDTO auditDTO) {
        // 脱敏IP地址
        if (auditDTO.getClientIp() != null) {
            auditDTO.setClientIp(maskIpAddress(auditDTO.getClientIp()));
        }

        // 脱敏用户代理信息
        if (auditDTO.getUserAgent() != null && auditDTO.getUserAgent().length() > 100) {
            auditDTO.setUserAgent(auditDTO.getUserAgent().substring(0, 100) + "...");
        }
    }

    /**
     * IP地址脱敏
     */
    private String maskIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }

        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + "." + parts[2] + ".***";
        }

        return ip.replaceAll("\\d+$", "***");
    }

    /**
     * 检查并触发告警
     */
    private void checkAndTriggerAlert(Long userId, String operation, String resource, String reason) {
        // 检查失败次数阈值
        long failureCount = getRecentFailureCount(userId);
        if (failureCount >= config.getFailureThreshold()) {
            triggerSecurityAlert(userId, operation, "MULTIPLE_FAILURES",
                "用户权限验证失败次数过多: " + failureCount);
        }

        // 检查敏感操作失败
        if (isSensitiveOperation(operation)) {
            triggerSecurityAlert(userId, operation, "SENSITIVE_OPERATION_DENIED",
                "敏感操作权限验证失败: " + reason);
        }
    }

    /**
     * 获取最近的失败次数
     */
    private long getRecentFailureCount(Long userId) {
        String failureKey = AUDIT_STATS_KEY_PREFIX + "failure:" + userId;
        Object countObj = redisTemplate.opsForValue().get(failureKey);
        return countObj != null ? (Long) countObj : 0;
    }

    /**
     * 触发安全告警
     */
    private void triggerSecurityAlert(Long userId, String operation, String alertType, String message) {
        if (!config.isEnableAlert()) {
            return;
        }

        try {
            String alertKey = AUDIT_ALERT_KEY_PREFIX + System.currentTimeMillis();

            PermissionAlert alert = PermissionAlert.builder()
                .userId(userId)
                .operation(operation)
                .alertType(alertType)
                .message(message)
                .createTime(LocalDateTime.now())
                .severity("HIGH")
                .build();

            redisTemplate.opsForValue().set(alertKey, alert, config.getAlertRetentionPeriod());

            log.warn("[权限审计-告警] {}", message);
            // TODO: 发送告警通知（邮件、短信、钉钉等）

        } catch (Exception e) {
            log.error("[权限审计] 触发安全告警异常", e);
        }
    }

    /**
     * 更新审计统计信息
     */
    private void updateAuditStats(PermissionAuditDTO auditDTO) {
        try {
            String statsKey = buildStatsKey(auditDTO.getUserId());

            // 使用Redis原子操作更新统计
            redisTemplate.opsForHash().increment(statsKey, "totalCount", 1);
            redisTemplate.opsForHash().increment(statsKey, "result:" + auditDTO.getResult(), 1);

            if ("DENIED".equals(auditDTO.getResult())) {
                redisTemplate.opsForHash().increment(statsKey, "failureCount", 1);
            }

            if (auditDTO.getDuration() != null) {
                redisTemplate.opsForHash().increment(statsKey, "totalTime", auditDTO.getDuration());
            }

            // 设置过期时间
            redisTemplate.expire(statsKey, config.getStatsRetentionPeriod());

        } catch (Exception e) {
            log.error("[权限审计] 更新审计统计异常", e);
        }
    }

    /**
     * 记录审计统计
     */
    private void recordAuditStats(String key, long value) {
        try {
            redisTemplate.opsForHash().increment(
                AUDIT_STATS_KEY_PREFIX + "global",
                key,
                value
            );
        } catch (Exception e) {
            log.error("[权限审计] 记录审计统计异常", e);
        }
    }

    /**
     * 构建审计日志键
     */
    private String buildAuditKey(PermissionAuditDTO auditDTO) {
        return AUDIT_LOG_KEY_PREFIX +
               auditDTO.getUserId() + ":" +
               auditDTO.getOperation() + ":" +
               System.currentTimeMillis();
    }

    /**
     * 构建统计键
     */
    private String buildStatsKey(Long userId) {
        return userId != null ?
            AUDIT_STATS_KEY_PREFIX + "user:" + userId :
            AUDIT_STATS_KEY_PREFIX + "global";
    }

    /**
     * 构建查询模式
     */
    private String buildQueryPattern(Long userId, String operation, String resource) {
        StringBuilder pattern = new StringBuilder(AUDIT_LOG_KEY_PREFIX);

        if (userId != null) {
            pattern.append(userId);
        } else {
            pattern.append("*");
        }

        pattern.append(":");

        if (operation != null) {
            pattern.append(operation);
        } else {
            pattern.append("*");
        }

        pattern.append(":*");

        return pattern.toString();
    }

    /**
     * 检查时间范围
     */
    private boolean isInTimeRange(LocalDateTime auditTime, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && auditTime.isBefore(startTime)) {
            return false;
        }
        if (endTime != null && auditTime.isAfter(endTime)) {
            return false;
        }
        return true;
    }

    /**
     * 实时计算统计信息
     */
    private PermissionAuditStats calculateRealTimeStats(Long userId) {
        // TODO: 实现实时统计计算逻辑
        return new PermissionAuditStats();
    }

    /**
     * 权限审计配置
     */
    public static class PermissionAuditConfig {
        private final boolean enableAudit;
        private final boolean enableDataMasking;
        private final boolean enableAlert;
        private final int queueSize;
        private final long queueTimeout;
        private final int batchSize;
        private final long pollTimeout;
        private final java.time.Duration auditRetentionPeriod;
        private final java.time.Duration statsRetentionPeriod;
        private final java.time.Duration alertRetentionPeriod;
        private final int failureThreshold;

        public PermissionAuditConfig(boolean enableAudit, boolean enableDataMasking, boolean enableAlert,
                                   int queueSize, long queueTimeout, int batchSize, long pollTimeout,
                                   java.time.Duration auditRetentionPeriod, java.time.Duration statsRetentionPeriod,
                                   java.time.Duration alertRetentionPeriod, int failureThreshold) {
            this.enableAudit = enableAudit;
            this.enableDataMasking = enableDataMasking;
            this.enableAlert = enableAlert;
            this.queueSize = queueSize;
            this.queueTimeout = queueTimeout;
            this.batchSize = batchSize;
            this.pollTimeout = pollTimeout;
            this.auditRetentionPeriod = auditRetentionPeriod;
            this.statsRetentionPeriod = statsRetentionPeriod;
            this.alertRetentionPeriod = alertRetentionPeriod;
            this.failureThreshold = failureThreshold;
        }

        public static PermissionAuditConfig defaultConfig() {
            return new PermissionAuditConfig(
                true,                           // 启用审计
                true,                           // 启用数据脱敏
                true,                           // 启用告警
                10000,                          // 队列大小
                5000,                           // 队列超时(ms)
                100,                            // 批次大小
                1000,                           // 轮询超时(ms)
                java.time.Duration.ofDays(30),  // 审计日志保留30天
                java.time.Duration.ofDays(7),   // 统计信息保留7天
                java.time.Duration.ofDays(3),   // 告警信息保留3天
                10                              // 失败阈值
            );
        }

        // Getter方法
        public boolean isEnableAudit() { return enableAudit; }
        public boolean isEnableDataMasking() { return enableDataMasking; }
        public boolean isEnableAlert() { return enableAlert; }
        public int getQueueSize() { return queueSize; }
        public long getQueueTimeout() { return queueTimeout; }
        public int getBatchSize() { return batchSize; }
        public long getPollTimeout() { return pollTimeout; }
        public java.time.Duration getAuditRetentionPeriod() { return auditRetentionPeriod; }
        public java.time.Duration getStatsRetentionPeriod() { return statsRetentionPeriod; }
        public java.time.Duration getAlertRetentionPeriod() { return alertRetentionPeriod; }
        public int getFailureThreshold() { return failureThreshold; }
    }

    /**
     * 权限告警信息
     */
    public static class PermissionAlert {
        private Long userId;
        private String operation;
        private String alertType;
        private String message;
        private LocalDateTime createTime;
        private String severity;
        private String status; // ACTIVE, RESOLVED

        public PermissionAlert() {}

        private PermissionAlert(Builder builder) {
            this.userId = builder.userId;
            this.operation = builder.operation;
            this.alertType = builder.alertType;
            this.message = builder.message;
            this.createTime = builder.createTime;
            this.severity = builder.severity;
            this.status = builder.status;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public Long getUserId() { return userId; }
        public String getOperation() { return operation; }
        public String getAlertType() { return alertType; }
        public String getMessage() { return message; }
        public LocalDateTime getCreateTime() { return createTime; }
        public String getSeverity() { return severity; }
        public String getStatus() { return status; }

        // Setters
        public void setUserId(Long userId) { this.userId = userId; }
        public void setOperation(String operation) { this.operation = operation; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public void setMessage(String message) { this.message = message; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        public void setSeverity(String severity) { this.severity = severity; }
        public void setStatus(String status) { this.status = status; }

        public static class Builder {
            private Long userId;
            private String operation;
            private String alertType;
            private String message;
            private LocalDateTime createTime;
            private String severity;
            private String status;

            public Builder userId(Long userId) { this.userId = userId; return this; }
            public Builder operation(String operation) { this.operation = operation; return this; }
            public Builder alertType(String alertType) { this.alertType = alertType; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder createTime(LocalDateTime createTime) { this.createTime = createTime; return this; }
            public Builder severity(String severity) { this.severity = severity; return this; }
            public Builder status(String status) { this.status = status; return this; }

            public PermissionAlert build() {
                return new PermissionAlert(this);
            }
        }
    }

    /**
     * 权限审计统计信息
     */
    public static class PermissionAuditStats {
        private Long totalCount = 0L;
        private Long grantedCount = 0L;
        private Long deniedCount = 0L;
        private Long failureCount = 0L;
        private Double averageValidationTime = 0.0;
        private LocalDateTime lastUpdateTime;
        private String summary;

        // Getters
        public Long getTotalCount() { return totalCount; }
        public Long getGrantedCount() { return grantedCount; }
        public Long getDeniedCount() { return deniedCount; }
        public Long getFailureCount() { return failureCount; }
        public Double getAverageValidationTime() { return averageValidationTime; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public String getSummary() { return summary; }

        // Setters
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        public void setGrantedCount(Long grantedCount) { this.grantedCount = grantedCount; }
        public void setDeniedCount(Long deniedCount) { this.deniedCount = deniedCount; }
        public void setFailureCount(Long failureCount) { this.failureCount = failureCount; }
        public void setAverageValidationTime(Double averageValidationTime) { this.averageValidationTime = averageValidationTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        public void setSummary(String summary) { this.summary = summary; }
    }
}
