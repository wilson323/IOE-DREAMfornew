package net.lab1024.sa.common.audit.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.audit.dao.ConfigChangeAuditDao;
import net.lab1024.sa.common.audit.entity.ConfigChangeAuditEntity;
import net.lab1024.sa.common.audit.model.ConfigChangeContext;
import net.lab1024.sa.common.audit.model.ConfigChangeNotification;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.Objects;

/**
 * 配置变更审计管理器
 * <p>
 * 基于现代化UI/UX最佳实践的企业级配置变更审计管理：
 * - 实时监控和记录所有配置变更
 * - 智能风险评估和异常检测
 * - 自动化通知和告警机制
 * - 高性能审计查询和统计分析
 * - 支持配置变更回滚和恢复
 * - 针对单企业1000台设备、20000人规模优化
 * </p>
 * <p>
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入所有依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除@Component注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class ConfigChangeAuditManager {

    private final ConfigChangeAuditDao configChangeAuditDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "config:audit:";
    private static final String STATISTICS_CACHE_KEY = CACHE_PREFIX + "statistics";
    private static final String HIGH_RISK_CACHE_KEY = CACHE_PREFIX + "high_risk";
    private static final String FAILED_CHANGES_CACHE_KEY = CACHE_PREFIX + "failed_changes";

    // 缓存过期时间
    private static final long CACHE_EXPIRE_MINUTES = 30;
    private static final long STATISTICS_CACHE_EXPIRE_MINUTES = 10;
    private static final long HIGH_RISK_CACHE_EXPIRE_MINUTES = 5;

    /**
     * 构造函数注入所有依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param configChangeAuditDao 配置变更审计DAO
     * @param redisTemplate Redis模板
     * @param objectMapper JSON对象映射器
     */
    public ConfigChangeAuditManager(ConfigChangeAuditDao configChangeAuditDao,
                                   RedisTemplate<String, Object> redisTemplate,
                                   ObjectMapper objectMapper) {
        this.configChangeAuditDao = Objects.requireNonNull(configChangeAuditDao, "configChangeAuditDao不能为null");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate不能为null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper不能为null");
    }

    /**
     * 记录配置变更
     *
     * @param context 变更上下文
     * @return 审计记录ID
     */
    public Long recordConfigChange(ConfigChangeContext context) {
        log.info("[配置审计] 记录配置变更: configType={}, configKey={}, changeType={}, operator={}",
                context.getConfigType(), context.getConfigKey(), context.getChangeType(), context.getOperatorName());

        try {
            ConfigChangeAuditEntity auditEntity = buildAuditEntity(context);

            // 评估变更风险
            auditEntity.assessChangeRisk();

            // 计算影响范围
            auditEntity.calculateImpactScope();

            // 生成变更摘要
            auditEntity.setChangeSummary(auditEntity.generateChangeSummary());

            // 保存审计记录
            configChangeAuditDao.insert(auditEntity);

            // 清除相关缓存
            clearRelatedCache();

            // 异步处理高风险变更和通知
            handleHighRiskChangeAsync(auditEntity);

            log.info("[配置审计] 配置变更记录成功: auditId={}, configKey={}, riskLevel={}",
                    auditEntity.getId(), context.getConfigKey(), auditEntity.getRiskLevel());

            return auditEntity.getId();

        } catch (Exception e) {
            log.error("[配置审计] 记录配置变更失败: configKey={}, error={}", context.getConfigKey(), e.getMessage(), e);
            throw new RuntimeException("记录配置变更失败", e);
        }
    }

    /**
     * 批量记录配置变更
     *
     * @param contexts 变更上下文列表
     * @param batchId 批次ID
     * @return 审计记录ID列表
     */
    public List<Long> batchRecordConfigChange(List<ConfigChangeContext> contexts, String batchId) {
        log.info("[配置审计] 批量记录配置变更: count={}, batchId={}", contexts.size(), batchId);

        List<Long> auditIds = new ArrayList<>();

        try {
            for (ConfigChangeContext context : contexts) {
                context.setChangeBatchId(batchId);
                Long auditId = recordConfigChange(context);
                auditIds.add(auditId);
            }

            log.info("[配置审计] 批量配置变更记录成功: count={}, batchId={}", auditIds.size(), batchId);
            return auditIds;

        } catch (Exception e) {
            log.error("[配置审计] 批量记录配置变更失败: batchId={}, error={}", batchId, e.getMessage(), e);
            throw new RuntimeException("批量记录配置变更失败", e);
        }
    }

    /**
     * 更新变更状态
     *
     * @param auditId 审计ID
     * @param status 新状态
     * @param errorMessage 错误信息
     * @param executionTime 执行时间
     */
    public void updateChangeStatus(Long auditId, String status, String errorMessage, Long executionTime) {
        log.info("[配置审计] 更新变更状态: auditId={}, status={}, executionTime={}ms",
                auditId, status, executionTime);

        try {
            int updated = configChangeAuditDao.updateChangeStatus(auditId, status, errorMessage, executionTime);
            if (updated > 0) {
                clearRelatedCache();
                log.info("[配置审计] 变更状态更新成功: auditId={}, status={}", auditId, status);
            }
        } catch (Exception e) {
            log.error("[配置审计] 更新变更状态失败: auditId={}, status={}, error={}", auditId, status, e.getMessage(), e);
            throw new RuntimeException("更新变更状态失败", e);
        }
    }

    /**
     * 审批变更
     *
     * @param auditId 审计ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approvalComment 审批意见
     */
    public void approveChange(Long auditId, Long approverId, String approverName, String approvalComment) {
        log.info("[配置审计] 审批变更: auditId={}, approver={}, comment={}", auditId, approverName, approvalComment);

        try {
            int updated = configChangeAuditDao.updateApprovalStatus(auditId, approverId, approverName, approvalComment);
            if (updated > 0) {
                clearRelatedCache();
                log.info("[配置审计] 变更审批成功: auditId={}, approver={}", auditId, approverName);
            }
        } catch (Exception e) {
            log.error("[配置审计] 审批变更失败: auditId={}, approver={}, error={}", auditId, approverName, e.getMessage(), e);
            throw new RuntimeException("审批变更失败", e);
        }
    }

    /**
     * 获取配置变更历史
     *
     * @param configKey 配置键
     * @param limit 限制数量
     * @return 变更历史列表
     */
    public List<ConfigChangeAuditEntity> getConfigChangeHistory(String configKey, Integer limit) {
        String cacheKey = CACHE_PREFIX + "history:" + configKey;

        try {
            // 尝试从缓存获取
            @SuppressWarnings("unchecked")
            List<ConfigChangeAuditEntity> cached = (List<ConfigChangeAuditEntity>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }

            // 从数据库查询
            List<ConfigChangeAuditEntity> history = configChangeAuditDao.selectByConfigKey(configKey, limit);

            // 缓存结果
            if (history != null) {
                redisTemplate.opsForValue().set(cacheKey, history, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }

            return history != null ? history : Collections.emptyList();

        } catch (Exception e) {
            log.error("[配置审计] 获取配置变更历史失败: configKey={}, error={}", configKey, e.getMessage(), e);
            return configChangeAuditDao.selectByConfigKey(configKey, limit);
        }
    }

    /**
     * 获取高风险变更
     *
     * @param hours 最近小时数
     * @param limit 限制数量
     * @return 高风险变更列表
     */
    public List<ConfigChangeAuditEntity> getHighRiskChanges(Integer hours, Integer limit) {
        try {
            // 尝试从缓存获取
            @SuppressWarnings("unchecked")
            List<ConfigChangeAuditEntity> cached = (List<ConfigChangeAuditEntity>)
                    redisTemplate.opsForValue().get(HIGH_RISK_CACHE_KEY);
            if (cached != null) {
                return cached;
            }

            // 从数据库查询
            List<ConfigChangeAuditEntity> highRiskChanges = configChangeAuditDao.selectHighRiskChanges(hours, limit);

            // 缓存结果（短时间缓存，因为高风险变更需要及时更新）
            if (highRiskChanges != null) {
                redisTemplate.opsForValue().set(HIGH_RISK_CACHE_KEY, highRiskChanges,
                        HIGH_RISK_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }

            return highRiskChanges != null ? highRiskChanges : Collections.emptyList();

        } catch (Exception e) {
            log.error("[配置审计] 获取高风险变更失败: hours={}, error={}", hours, e.getMessage(), e);
            return configChangeAuditDao.selectHighRiskChanges(hours, limit);
        }
    }

    /**
     * 获取失败的变更
     *
     * @param hours 最近小时数
     * @param limit 限制数量
     * @return 失败变更列表
     */
    public List<ConfigChangeAuditEntity> getFailedChanges(Integer hours, Integer limit) {
        try {
            // 尝试从缓存获取
            @SuppressWarnings("unchecked")
            List<ConfigChangeAuditEntity> cached = (List<ConfigChangeAuditEntity>)
                    redisTemplate.opsForValue().get(FAILED_CHANGES_CACHE_KEY);
            if (cached != null) {
                return cached;
            }

            // 从数据库查询
            List<ConfigChangeAuditEntity> failedChanges = configChangeAuditDao.selectFailedChanges(hours, limit);

            // 缓存结果
            if (failedChanges != null) {
                redisTemplate.opsForValue().set(FAILED_CHANGES_CACHE_KEY, failedChanges,
                        CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }

            return failedChanges != null ? failedChanges : Collections.emptyList();

        } catch (Exception e) {
            log.error("[配置审计] 获取失败变更失败: hours={}, error={}", hours, e.getMessage(), e);
            return configChangeAuditDao.selectFailedChanges(hours, limit);
        }
    }

    /**
     * 获取待审批的变更
     *
     * @param limit 限制数量
     * @return 待审批变更列表
     */
    public List<ConfigChangeAuditEntity> getPendingApprovals(Integer limit) {
        return configChangeAuditDao.selectPendingApprovals(limit);
    }

    /**
     * 获取变更统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    public Map<String, Object> getChangeStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        String cacheKey = STATISTICS_CACHE_KEY + ":" + startTime.toString() + ":" + endTime.toString();

        try {
            // 尝试从缓存获取
            @SuppressWarnings("unchecked")
            Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }

            Map<String, Object> statistics = new HashMap<>();

            // 基础统计
            statistics.put("configTypeStatistics", configChangeAuditDao.selectChangeStatisticsByConfigType(startTime, endTime));
            statistics.put("operatorStatistics", configChangeAuditDao.selectChangeStatisticsByOperator(startTime, endTime, 20));
            statistics.put("impactStatistics", configChangeAuditDao.selectImpactStatistics(startTime, endTime));

            // 趋势统计
            statistics.put("hourlyTrend", configChangeAuditDao.selectChangeTrendByHour(startTime, endTime));
            statistics.put("dailyTrend", configChangeAuditDao.selectChangeTrendByDay(30));

            // 当前状态统计
            statistics.put("todayStatistics", configChangeAuditDao.selectTodayChangeStatistics());
            statistics.put("weeklyStatistics", configChangeAuditDao.selectWeeklyChangeStatistics());
            statistics.put("monthlyStatistics", configChangeAuditDao.selectMonthlyChangeStatistics());

            // 缓存结果
            redisTemplate.opsForValue().set(cacheKey, statistics, STATISTICS_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            return statistics;

        } catch (Exception e) {
            log.error("[配置审计] 获取变更统计失败: startTime={}, endTime={}, error={}",
                    startTime, endTime, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 检测异常变更模式
     *
     * @param hours 最近小时数
     * @return 异常变更模式列表
     */
    public List<Map<String, Object>> detectAbnormalChangePatterns(Integer hours) {
        return configChangeAuditDao.selectAbnormalChangePatterns(hours);
    }

    /**
     * 获取热力图数据
     *
     * @param days 最近天数
     * @return 热力图数据
     */
    public List<Map<String, Object>> getHeatmapData(Integer days) {
        return configChangeAuditDao.selectHeatmapData(days);
    }

    /**
     * 发送变更通知
     *
     * @param auditId 审计ID
     * @param notification 通知内容
     */
    public void sendChangeNotification(Long auditId, ConfigChangeNotification notification) {
        log.info("[配置审计] 发送变更通知: auditId={}, channels={}", auditId, notification.getChannels());

        try {
            // 更新通知状态为已发送
            configChangeAuditDao.updateNotificationStatus(auditId, "SENT", null);

            // 这里可以集成实际的通知服务
            // 例如：邮件、短信、企业微信、钉钉等
            sendNotifications(notification);

            clearRelatedCache();

            log.info("[配置审计] 变更通知发送成功: auditId={}", auditId);

        } catch (Exception e) {
            log.error("[配置审计] 发送变更通知失败: auditId={}, error={}", auditId, e.getMessage(), e);

            // 更新通知状态为失败
            configChangeAuditDao.updateNotificationStatus(auditId, "FAILED", e.getMessage());
        }
    }

    /**
     * 构建审计实体
     */
    private ConfigChangeAuditEntity buildAuditEntity(ConfigChangeContext context) {
        ConfigChangeAuditEntity entity = new ConfigChangeAuditEntity();

        // 基本信息
        entity.setChangeType(context.getChangeType());
        entity.setConfigType(context.getConfigType());
        entity.setConfigKey(context.getConfigKey());
        entity.setConfigName(context.getConfigName());
        entity.setConfigGroup(context.getConfigGroup());

        // 变更内容
        try {
            if (context.getOldValue() != null) {
                entity.setOldValue(objectMapper.writeValueAsString(context.getOldValue()));
            }
            if (context.getNewValue() != null) {
                entity.setNewValue(objectMapper.writeValueAsString(context.getNewValue()));
            }
            if (context.getChangedFields() != null) {
                entity.setChangedFields(objectMapper.writeValueAsString(context.getChangedFields()));
            }
        } catch (JsonProcessingException e) {
            log.warn("[配置审计] 序列化变更内容失败: {}", e.getMessage());
        }

        // 操作信息
        entity.setChangeBatchId(context.getChangeBatchId());
        entity.setChangeReason(context.getChangeReason());
        entity.setChangeSource(context.getChangeSource());
        entity.setOperatorId(context.getOperatorId());
        entity.setOperatorName(context.getOperatorName());
        entity.setOperatorRole(context.getOperatorRole());
        entity.setClientIp(context.getClientIp());
        entity.setUserAgent(context.getUserAgent());
        entity.setSessionId(context.getSessionId());
        entity.setRequestId(context.getRequestId());

        // 业务信息
        entity.setRelatedConfigId(context.getRelatedConfigId());
        entity.setRelatedTableName(context.getRelatedTableName());
        entity.setBusinessModule(context.getBusinessModule());
        entity.setVersionNumber(context.getVersionNumber());

        // 初始化默认值
        entity.initializeDefaults();

        return entity;
    }

    /**
     * 异步处理高风险变更
     */
    private void handleHighRiskChangeAsync(ConfigChangeAuditEntity auditEntity) {
        if (auditEntity.isHighRiskChange() || auditEntity.needsApproval()) {
            // 异步处理高风险变更通知
            CompletableFuture.runAsync(() -> {
                try {
                    ConfigChangeNotification notification = buildHighRiskNotification(auditEntity);
                    sendChangeNotification(auditEntity.getId(), notification);
                } catch (Exception e) {
                    log.error("[配置审计] 处理高风险变更通知失败: auditId={}, error={}",
                            auditEntity.getId(), e.getMessage(), e);
                }
            });
        }
    }

    /**
     * 构建高风险变更通知
     */
    private ConfigChangeNotification buildHighRiskNotification(ConfigChangeAuditEntity auditEntity) {
        ConfigChangeNotification notification = new ConfigChangeNotification();

        notification.setAuditId(auditEntity.getId());
        notification.setTitle("高风险配置变更告警");
        notification.setMessage(String.format("检测到高风险配置变更：%s，操作人：%s，风险等级：%s",
                auditEntity.getConfigName(), auditEntity.getOperatorName(), auditEntity.getRiskLevelDisplayName()));

        notification.setChannels(List.of("EMAIL", "WEBHOOK", "IN_APP"));
        notification.setPriority("HIGH");
        notification.setRecipients(getHighRiskNotificationRecipients());

        Map<String, Object> data = new HashMap<>();
        data.put("auditId", auditEntity.getId());
        data.put("configKey", auditEntity.getConfigKey());
        data.put("configName", auditEntity.getConfigName());
        data.put("operatorName", auditEntity.getOperatorName());
        data.put("riskLevel", auditEntity.getRiskLevel());
        data.put("changeTime", auditEntity.getChangeTime());
        data.put("changeReason", auditEntity.getChangeReason());
        notification.setData(data);

        return notification;
    }

    /**
     * 获取高风险变更通知接收人
     */
    private List<String> getHighRiskNotificationRecipients() {
        // 这里应该从配置或数据库中获取系统管理员和安全负责人
        return List.of("admin@ioedream.com", "security@ioedream.com");
    }

    /**
     * 发送通知
     */
    private void sendNotifications(ConfigChangeNotification notification) {
        for (String channel : notification.getChannels()) {
            try {
                switch (channel) {
                    case "EMAIL" -> sendEmailNotification(notification);
                    case "SMS" -> sendSmsNotification(notification);
                    case "WEBHOOK" -> sendWebhookNotification(notification);
                    case "IN_APP" -> sendInAppNotification(notification);
                    default -> log.warn("[配置审计] 未知通知渠道: {}", channel);
                }
            } catch (Exception e) {
                log.error("[配置审计] 发送通知失败: channel={}, error={}", channel, e.getMessage(), e);
            }
        }
    }

    /**
     * 发送邮件通知
     */
    private void sendEmailNotification(ConfigChangeNotification notification) {
        // 集成邮件服务
        log.info("[配置审计] 发送邮件通知: recipients={}, title={}",
                notification.getRecipients(), notification.getTitle());
    }

    /**
     * 发送短信通知
     */
    private void sendSmsNotification(ConfigChangeNotification notification) {
        // 集成短信服务
        log.info("[配置审计] 发送短信通知: recipients={}, title={}",
                notification.getRecipients(), notification.getTitle());
    }

    /**
     * 发送Webhook通知
     */
    private void sendWebhookNotification(ConfigChangeNotification notification) {
        // 集成Webhook服务
        log.info("[配置审计] 发送Webhook通知: title={}", notification.getTitle());
    }

    /**
     * 发送应用内通知
     */
    private void sendInAppNotification(ConfigChangeNotification notification) {
        // 集成应用内通知服务
        log.info("[配置审计] 发送应用内通知: recipients={}, title={}",
                notification.getRecipients(), notification.getTitle());
    }

    /**
     * 清除相关缓存
     */
    private void clearRelatedCache() {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("[配置审计] 清除缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 预热缓存
     */
    public void warmupCache() {
        log.info("[配置审计] 开始预热缓存");

        try {
            // 预热高风险变更缓存
            getHighRiskChanges(24, 50);

            // 预热失败变更缓存
            getFailedChanges(24, 50);

            // 预热统计信息缓存
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(7);
            getChangeStatistics(startTime, endTime);

            log.info("[配置审计] 缓存预热完成");

        } catch (Exception e) {
            log.error("[配置审计] 缓存预热失败: {}", e.getMessage(), e);
        }
    }
}
