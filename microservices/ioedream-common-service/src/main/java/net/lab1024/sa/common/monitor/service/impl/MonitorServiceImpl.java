package net.lab1024.sa.common.monitor.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.monitor.dao.AlertDao;
import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
import net.lab1024.sa.common.monitor.domain.constant.SecurityAlertConstants;
import net.lab1024.sa.common.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.common.monitor.domain.entity.AlertRuleEntity;
import net.lab1024.sa.common.monitor.domain.vo.AlertRuleVO;
import net.lab1024.sa.common.monitor.domain.vo.ServiceMetricsVO;
import net.lab1024.sa.common.monitor.domain.vo.SystemHealthVO;
import net.lab1024.sa.common.monitor.manager.HealthCheckManager;
import net.lab1024.sa.common.monitor.service.MonitorService;

/**
 * 监控服务实现类 整合自ioedream-monitor-service
 *
 * 符合CLAUDE.md规范： - 使用@Service注解 - 使用@Resource依赖注入 - 使用@Transactional事务管理
 *
 * 企业级特性： - 实时监控指标采集 - 多维度性能分析 - 智能告警规则 - 告警聚合和降噪
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自monitor-service）
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class MonitorServiceImpl implements MonitorService {


    @Resource
    private AlertDao alertDao;

    @Resource
    private AlertRuleDao alertRuleDao;

    @Resource
    private HealthCheckManager healthCheckManager;

    @Resource
    private MeterRegistry meterRegistry;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Observed(name = "monitor.system.health", contextualName = "monitor-system-health")
    public ResponseDTO<SystemHealthVO> getSystemHealth() {
        try {
            SystemHealthVO health = healthCheckManager.checkSystemHealth();
            return ResponseDTO.ok(health);

        } catch (BusinessException e) {
            log.warn("[获取系统健康状态] 业务异常，error={}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[获取系统健康状态] 系统异常", e);
            return ResponseDTO.error("MONITOR_SYSTEM_HEALTH_ERROR", "获取系统健康状态失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "monitor.service.metrics", contextualName = "monitor-service-metrics")
    public ResponseDTO<ServiceMetricsVO> getServiceMetrics(String serviceName) {
        try {
            ServiceMetricsVO metrics = collectServiceMetrics(serviceName);
            return ResponseDTO.ok(metrics);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[获取服务指标] 参数异常，serviceName: {}, error={}", serviceName, e.getMessage());
            return ResponseDTO.error("MONITOR_SERVICE_NAME_INVALID", "服务名称无效: " + e.getMessage());
        } catch (Exception e) {
            log.error("[获取服务指标] 系统异常，serviceName: {}", serviceName, e);
            return ResponseDTO.error("MONITOR_SERVICE_METRICS_ERROR", "获取服务指标失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "monitor.business.metrics", contextualName = "monitor-business-metrics")
    public ResponseDTO<Map<String, Object>> getBusinessMetrics(String metricName) {
        try {
            Map<String, Object> metrics = collectBusinessMetrics(metricName);
            return ResponseDTO.ok(metrics);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[获取业务指标] 参数异常，metricName: {}, error={}", metricName, e.getMessage());
            return ResponseDTO.error("MONITOR_METRIC_NAME_INVALID", "指标名称无效: " + e.getMessage());
        } catch (Exception e) {
            log.error("[获取业务指标] 系统异常，metricName: {}", metricName, e);
            return ResponseDTO.error("MONITOR_BUSINESS_METRICS_ERROR", "获取业务指标失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "monitor.alert.rule.create", contextualName = "monitor-alert-rule-create")
    public ResponseDTO<Long> createAlertRule(AlertRuleVO ruleVO) {
        try {
            log.info("创建告警规则，规则名称：{}，监控指标：{}", ruleVO.getRuleName(), ruleVO.getMetricName());

            // 1. 参数验证
            String validationError = validateAlertRule(ruleVO);
            if (validationError != null) {
                log.warn("告警规则验证失败：{}", validationError);
                return ResponseDTO.error(validationError);
            }

            // 2. 检查规则名称是否已存在
            if (isRuleNameExists(ruleVO.getRuleName())) {
                log.warn("告警规则名称已存在：{}", ruleVO.getRuleName());
                return ResponseDTO.error("告警规则名称已存在，请使用其他名称");
            }

            // 3. 转换为实体对象
            AlertRuleEntity alertRule = convertVOToEntity(ruleVO);

            // 4. 设置默认值
            setDefaultValues(alertRule);

            // 5. 保存到数据库
            alertRuleDao.insert(alertRule);

            log.info("告警规则创建成功，规则ID：{}，规则名称：{}", alertRule.getId(), alertRule.getRuleName());
            return ResponseDTO.ok(alertRule.getId());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[创建告警规则] 参数异常，ruleName: {}, error={}", ruleVO != null ? ruleVO.getRuleName() : "未知",
                    e.getMessage());
            return ResponseDTO.error("MONITOR_ALERT_RULE_PARAM_ERROR", "创建告警规则参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[创建告警规则] 业务异常，ruleName: {}, error={}", ruleVO != null ? ruleVO.getRuleName() : "未知",
                    e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[创建告警规则] 系统异常，ruleName: {}", ruleVO != null ? ruleVO.getRuleName() : "未知", e);
            return ResponseDTO.error("MONITOR_ALERT_RULE_CREATE_ERROR", "创建告警规则失败，请稍后重试");
        }
    }

    /**
     * 验证告警规则参数
     *
     * @param ruleVO
     *               告警规则VO
     * @return 验证错误信息，null表示验证通过
     */
    private String validateAlertRule(AlertRuleVO ruleVO) {
        if (ruleVO == null) {
            return "告警规则不能为空";
        }

        // 验证规则名称
        if (ruleVO.getRuleName() == null || ruleVO.getRuleName().trim().isEmpty()) {
            return "规则名称不能为空";
        }
        if (ruleVO.getRuleName().length() > 100) {
            return "规则名称长度不能超过100个字符";
        }

        // 验证监控指标
        if (ruleVO.getMetricName() == null || ruleVO.getMetricName().trim().isEmpty()) {
            return "监控指标不能为空";
        }
        if (ruleVO.getMetricName().length() > 100) {
            return "监控指标名称长度不能超过100个字符";
        }

        // 验证条件操作符
        if (ruleVO.getConditionOperator() == null || ruleVO.getConditionOperator().trim().isEmpty()) {
            return "告警条件操作符不能为空";
        }
        String[] validOperators = { "GT", "GTE", "LT", "LTE", "EQ", "NEQ" };
        boolean isValidOperator = false;
        for (String operator : validOperators) {
            if (operator.equalsIgnoreCase(ruleVO.getConditionOperator())) {
                isValidOperator = true;
                break;
            }
        }
        if (!isValidOperator) {
            return "告警条件操作符无效，支持的操作符：GT、GTE、LT、LTE、EQ、NEQ";
        }

        // 验证告警阈值
        if (ruleVO.getThresholdValue() == null) {
            return "告警阈值不能为空";
        }
        if (ruleVO.getThresholdValue() < 0) {
            return "告警阈值不能为负数";
        }

        // 验证告警级别
        if (ruleVO.getAlertLevel() == null || ruleVO.getAlertLevel().trim().isEmpty()) {
            return "告警级别不能为空";
        }
        String[] validLevels = { "INFO", "WARNING", "ERROR", "CRITICAL" };
        boolean isValidLevel = false;
        for (String level : validLevels) {
            if (level.equalsIgnoreCase(ruleVO.getAlertLevel())) {
                isValidLevel = true;
                break;
            }
        }
        if (!isValidLevel) {
            return "告警级别无效，支持的级别：INFO、WARNING、ERROR、CRITICAL";
        }

        // 验证持续时间（如果提供）
        if (ruleVO.getDurationMinutes() != null && ruleVO.getDurationMinutes() < 0) {
            return "持续时间不能为负数";
        }

        // 验证通知频率（如果提供）
        if (ruleVO.getNotificationInterval() != null && ruleVO.getNotificationInterval() < 0) {
            return "通知频率不能为负数";
        }

        // 验证抑制时间（如果提供）
        if (ruleVO.getSuppressionDuration() != null && ruleVO.getSuppressionDuration() < 0) {
            return "抑制时间不能为负数";
        }

        return null;
    }

    /**
     * 检查规则名称是否已存在
     *
     * @param ruleName
     *                 规则名称
     * @return true表示已存在，false表示不存在
     */
    private boolean isRuleNameExists(String ruleName) {
        try {
            QueryWrapper<AlertRuleEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rule_name", ruleName);
            queryWrapper.eq("deleted_flag", 0);
            Long count = alertRuleDao.selectCount(queryWrapper);
            return count != null && count > 0;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[检查规则名称] 参数异常，ruleName: {}, error={}", ruleName, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[检查规则名称] 系统异常，ruleName: {}", ruleName, e);
            return false;
        }
    }

    /**
     * 将AlertRuleVO转换为AlertRuleEntity
     * <p>
     * 注意：AlertRuleEntity只有基本字段，详细配置存储在ruleConfig JSON中
     * </p>
     *
     * @param ruleVO 告警规则VO
     * @return 告警规则实体
     */
    private AlertRuleEntity convertVOToEntity(AlertRuleVO ruleVO) {
        AlertRuleEntity entity = new AlertRuleEntity();

        // 设置基本字段
        entity.setId(ruleVO.getId());
        entity.setRuleName(ruleVO.getRuleName());
        entity.setRuleType(ruleVO.getMonitorType()); // 使用monitorType作为ruleType

        // 将String类型的alertLevel转换为Integer（使用安防告警标准：P1=1, P2=2, P3=3, P4=4）
        Integer alertLevelInt = SecurityAlertConstants.AlertLevel.fromString(ruleVO.getAlertLevel());
        entity.setAlertLevel(alertLevelInt);

        // 将List<String>类型的notificationChannels转换为String（逗号分隔）
        String notifyChannelsStr = ruleVO.getNotificationChannels() != null
                ? String.join(",", ruleVO.getNotificationChannels())
                : null;
        entity.setNotifyChannels(notifyChannelsStr);

        // 将String类型的status转换为Integer（ENABLED=1, DISABLED=0）
        Integer statusInt = ruleVO.getStatus() != null && "ENABLED".equals(ruleVO.getStatus()) ? 1 : 0;
        entity.setStatus(statusInt);

        // 将详细配置序列化到ruleConfig JSON中
        Map<String, Object> ruleConfig = new HashMap<>();
        ruleConfig.put("ruleDescription", ruleVO.getRuleDescription());
        ruleConfig.put("metricName", ruleVO.getMetricName());
        ruleConfig.put("monitorType", ruleVO.getMonitorType());
        ruleConfig.put("conditionOperator", ruleVO.getConditionOperator());
        ruleConfig.put("thresholdValue", ruleVO.getThresholdValue());
        ruleConfig.put("alertLevel", ruleVO.getAlertLevel());
        ruleConfig.put("applicableServices", ruleVO.getApplicableServices());
        ruleConfig.put("applicableEnvironments", ruleVO.getApplicableEnvironments());
        ruleConfig.put("durationMinutes", ruleVO.getDurationMinutes());
        ruleConfig.put("notificationUsers", ruleVO.getNotificationUsers());
        ruleConfig.put("notificationInterval", ruleVO.getNotificationInterval());
        ruleConfig.put("suppressionDuration", ruleVO.getSuppressionDuration());
        ruleConfig.put("ruleExpression", ruleVO.getRuleExpression());
        ruleConfig.put("priority", ruleVO.getPriority());
        ruleConfig.put("tags", ruleVO.getTags());

        try {
            // 使用Jackson ObjectMapper序列化（需要注入ObjectMapper）
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            entity.setRuleConfig(objectMapper.writeValueAsString(ruleConfig));
        } catch (Exception e) {
            log.error("[监控服务] 序列化ruleConfig失败", e);
            entity.setRuleConfig("{}");
        }

        return entity;
    }

    /**
     * 将String类型的告警级别转换为Integer（使用安防告警标准）
     * <p>
     * 使用SecurityAlertConstants.AlertLevel统一管理告警级别转换
     * P1紧急=1, P2重要=2, P3普通=3, P4提示=4
     * </p>
     *
     * @param alertLevel 告警级别字符串
     * @return 告警级别整数
     * @deprecated 使用SecurityAlertConstants.AlertLevel.fromString()替代
     */
    @Deprecated
    private Integer convertAlertLevelToInteger(String alertLevel) {
        return SecurityAlertConstants.AlertLevel.fromString(alertLevel);
    }

    /**
     * 设置默认值
     * <p>
     * 注意：AlertRuleEntity的详细字段存储在ruleConfig JSON中，需要解析和更新
     * </p>
     *
     * @param alertRule 告警规则实体
     */
    private void setDefaultValues(AlertRuleEntity alertRule) {
        // 设置默认状态（Integer类型）
        if (alertRule.getStatus() == null) {
            alertRule.setStatus(1); // 1-启用 0-禁用
        }

        // 从ruleConfig JSON中读取和设置默认值
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> ruleConfig = new HashMap<>();

            if (alertRule.getRuleConfig() != null && !alertRule.getRuleConfig().trim().isEmpty()) {
                ruleConfig = objectMapper.readValue(alertRule.getRuleConfig(), new TypeReference<Map<String, Object>>() {});
            }

            // 设置默认优先级（根据告警级别）
            if (!ruleConfig.containsKey("priority") || ruleConfig.get("priority") == null) {
                // 将Integer类型的alertLevel转换为String（使用安防告警标准）
                String alertLevelStr = SecurityAlertConstants.AlertLevel.toString(alertRule.getAlertLevel());
                ruleConfig.put("priority", calculateDefaultPriority(alertLevelStr));
            }

            // 设置默认持续时间（如果未提供）
            if (!ruleConfig.containsKey("durationMinutes") || ruleConfig.get("durationMinutes") == null) {
                ruleConfig.put("durationMinutes", 5); // 默认5分钟
            }

            // 设置默认通知频率（如果未提供）
            if (!ruleConfig.containsKey("notificationInterval") || ruleConfig.get("notificationInterval") == null) {
                ruleConfig.put("notificationInterval", 60); // 默认60分钟
            }

            // 设置默认抑制时间（如果未提供）
            if (!ruleConfig.containsKey("suppressionDuration") || ruleConfig.get("suppressionDuration") == null) {
                ruleConfig.put("suppressionDuration", 30); // 默认30分钟
            }

            // 将更新后的配置写回ruleConfig
            alertRule.setRuleConfig(objectMapper.writeValueAsString(ruleConfig));
        } catch (Exception e) {
            log.error("[监控服务] 设置告警规则默认值失败", e);
        }
    }

    /**
     * 将Integer类型的告警级别转换为String（使用安防告警标准）
     * <p>
     * 使用SecurityAlertConstants.AlertLevel统一管理告警级别转换
     * </p>
     *
     * @param alertLevel 告警级别（Integer）
     * @return 告警级别字符串（P1/P2/P3/P4）
     * @deprecated 使用SecurityAlertConstants.AlertLevel.toString()替代
     */
    @Deprecated
    private String convertAlertLevelToString(Integer alertLevel) {
        return SecurityAlertConstants.AlertLevel.toString(alertLevel);
    }

    /**
     * 根据告警级别计算默认优先级
     *
     * @param alertLevel
     *                   告警级别
     * @return 优先级（数字越小优先级越高）
     */
    private Integer calculateDefaultPriority(String alertLevel) {
        if (alertLevel == null) {
            return 100;
        }
        switch (alertLevel.toUpperCase()) {
            case "CRITICAL":
                return 1; // 最高优先级
            case "ERROR":
                return 10;
            case "WARNING":
                return 50;
            case "INFO":
                return 100; // 最低优先级
            default:
                return 100;
        }
    }

    @Observed(name = "monitor.alert.trigger", contextualName = "monitor-alert-trigger")
    public void triggerAlert(String alertType, String alertMessage, Integer severity) {
        try {
            log.warn("触发告警 - 类型: {}, 消息: {}, 严重程度: {}", alertType, alertMessage, severity);

            AlertEntity alert = new AlertEntity();
            // 将字符串告警类型转换为整数（1-设备告警, 2-AI告警, 3-系统告警）
            alert.setAlertType(convertAlertTypeToInteger(alertType));
            alert.setAlertContent(alertMessage); // 使用alertContent而不是alertDescription
            // 将严重程度转换为安防告警级别（使用SecurityAlertConstants）
            alert.setAlertLevel(SecurityAlertConstants.AlertLevel.fromString(getSeverityLevel(severity)));
            alert.setStatus(SecurityAlertConstants.AlertStatus.PENDING); // 使用安防告警标准状态（待处理）
            alert.setCreateTime(LocalDateTime.now());

            alertDao.insert(alert);

        } catch (BusinessException e) {
            log.warn("[触发告警] 业务异常，alertType: {}, error={}", alertType, e.getMessage());
        } catch (Exception e) {
            log.error("[触发告警] 系统异常，alertType: {}", alertType, e);
        }
    }

    @Observed(name = "monitor.alert.history", contextualName = "monitor-alert-history")
    public ResponseDTO<List<Map<String, Object>>> getAlertHistory(Integer limit) {
        try {
            List<AlertEntity> alerts = alertDao.selectRecentAlerts(limit);
            // 转换为Map
            List<Map<String, Object>> alertList = alerts.stream().map(alert -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", alert.getId() != null ? alert.getId() : 0L);
                map.put("alertType", alert.getAlertType() != null ? alert.getAlertType() : "");
                map.put("alertDescription", alert.getAlertContent() != null ? alert.getAlertContent() : "");
                map.put("alertLevel", alert.getAlertLevel() != null ? alert.getAlertLevel() : 0);
                map.put("status", alert.getStatus() != null ? alert.getStatus() : 0);
                map.put("alertTime", alert.getCreateTime() != null ? alert.getCreateTime().toString() : "");
                map.put("createTime", alert.getCreateTime() != null ? alert.getCreateTime().toString() : "");
                return map;
            }).collect(Collectors.toList());
            return ResponseDTO.ok(alertList);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[获取告警历史] 参数异常，limit: {}, error={}", limit, e.getMessage());
            return ResponseDTO.error("MONITOR_ALERT_HISTORY_PARAM_ERROR", "获取告警历史参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[获取告警历史] 系统异常", e);
            return ResponseDTO.error("MONITOR_ALERT_HISTORY_ERROR", "获取告警历史失败，请稍后重试");
        }
    }

    @Observed(name = "monitor.metric.record", contextualName = "monitor-metric-record")
    public void recordMetric(String metricName, Double value, Map<String, String> tags) {
        try {
            recordMetricToMicrometer(metricName, value, tags);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[记录性能指标] 参数异常，metricName: {}, error={}", metricName, e.getMessage());
        } catch (Exception e) {
            log.error("[记录性能指标] 系统异常，metricName: {}", metricName, e);
        }
    }

    /**
     * 将严重程度转换为通用告警级别字符串
     *
     * @param severity 严重程度（1-4）
     * @return 通用告警级别字符串
     */
    private String getSeverityLevel(Integer severity) {
        switch (severity) {
            case 1:
                return "INFO";
            case 2:
                return "WARNING";
            case 3:
                return "ERROR";
            case 4:
                return "CRITICAL";
            default:
                return "WARNING";
        }
    }

    /**
     * 将字符串告警类型转换为整数
     *
     * @param alertType 告警类型字符串
     * @return 告警类型整数（1-设备告警, 2-AI告警, 3-系统告警）
     */
    private Integer convertAlertTypeToInteger(String alertType) {
        if (alertType == null) {
            return SecurityAlertConstants.AlertType.SYSTEM; // 默认为系统告警
        }
        switch (alertType.toUpperCase()) {
            case "DEVICE":
            case "DEVICE_ALERT":
                return SecurityAlertConstants.AlertType.DEVICE;
            case "AI":
            case "AI_ALERT":
                return SecurityAlertConstants.AlertType.AI;
            case "SYSTEM":
            case "SYSTEM_ALERT":
                return SecurityAlertConstants.AlertType.SYSTEM;
            default:
                return SecurityAlertConstants.AlertType.SYSTEM;
        }
    }

    /**
     * 采集服务指标（使用Micrometer直接查询）
     * <p>
     * 替代已废弃的 MetricsCollectorManager.collectServiceMetrics 使用 Micrometer 标准 API
     * 直接查询指标
     * </p>
     *
     * @param serviceName
     *                    服务名称
     * @return 服务指标VO
     */
    private ServiceMetricsVO collectServiceMetrics(String serviceName) {
        log.debug("开始采集服务指标，服务名称：{}", serviceName);

        ServiceMetricsVO metrics = new ServiceMetricsVO();
        metrics.setServiceName(serviceName);

        try {
            // 1. 计算QPS（每秒请求数）
            Long qps = calculateQPS(serviceName);
            metrics.setQps(qps);

            // 2. 计算TPS（每秒事务数，这里使用QPS作为近似值）
            metrics.setTps(qps);

            // 3. 计算平均响应时间（毫秒）
            Double avgResponseTime = calculateAvgResponseTime(serviceName);
            metrics.setAvgResponseTime(avgResponseTime);

            // 4. 计算错误率
            Double errorRate = calculateErrorRate(serviceName);
            metrics.setErrorRate(errorRate);

            // 5. 获取活跃连接数
            Integer activeConnections = getActiveConnections(serviceName);
            metrics.setActiveConnections(activeConnections);

            // 6. 存储指标到Redis（可选）
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                storeServiceMetrics(serviceName, metrics);
            }

            log.debug("服务指标采集完成，服务：{}，QPS：{}，平均响应时间：{}ms，错误率：{}", serviceName, qps, avgResponseTime, errorRate);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[采集服务指标] 参数异常，serviceName: {}, error={}", serviceName, e.getMessage());
            // 异常情况下返回默认值
            metrics.setQps(0L);
            metrics.setTps(0L);
            metrics.setAvgResponseTime(0.0);
            metrics.setErrorRate(0.0);
            metrics.setActiveConnections(0);
        } catch (Exception e) {
            log.error("[采集服务指标] 系统异常，serviceName: {}", serviceName, e);
            // 异常情况下返回默认值
            metrics.setQps(0L);
            metrics.setTps(0L);
            metrics.setAvgResponseTime(0.0);
            metrics.setErrorRate(0.0);
            metrics.setActiveConnections(0);
        }

        return metrics;
    }

    /**
     * 计算QPS（每秒请求数）
     */
    private Long calculateQPS(String serviceName) {
        try {
            Counter counter = meterRegistry.find("http.server.requests").tag("service", serviceName).counter();

            if (counter != null && counter.count() > 0) {
                double count = counter.count();
                return Math.round(count / 60.0);
            }

            Counter httpCounter = meterRegistry.find("http.server.requests").counter();
            if (httpCounter != null && httpCounter.count() > 0) {
                double count = httpCounter.count();
                return Math.round(count / 60.0);
            }

        } catch (Exception e) {
            log.warn("[计算QPS] 系统异常，服务：{}", serviceName, e);
        }

        return 0L;
    }

    /**
     * 计算平均响应时间
     */
    private Double calculateAvgResponseTime(String serviceName) {
        try {
            Timer timer = meterRegistry.find("http.server.requests").tag("service", serviceName).timer();

            if (timer != null && timer.count() > 0) {
                return timer.mean(TimeUnit.MILLISECONDS);
            }

            Timer httpTimer = meterRegistry.find("http.server.requests").timer();
            if (httpTimer != null && httpTimer.count() > 0) {
                return httpTimer.mean(TimeUnit.MILLISECONDS);
            }

        } catch (Exception e) {
            log.warn("[计算平均响应时间] 系统异常，服务：{}", serviceName, e);
        }

        return 0.0;
    }

    /**
     * 计算错误率
     */
    private Double calculateErrorRate(String serviceName) {
        try {
            Counter totalCounter = meterRegistry.find("http.server.requests").tag("service", serviceName).counter();

            if (totalCounter == null || totalCounter.count() == 0) {
                return 0.0;
            }

            double totalCount = totalCounter.count();
            double[] errorCount = { 0.0 };

            Counter error4xxCounter = meterRegistry.find("http.server.requests").tag("service", serviceName)
                    .tag("status", "4xx").counter();
            if (error4xxCounter != null) {
                errorCount[0] += error4xxCounter.count();
            }

            Counter error5xxCounter = meterRegistry.find("http.server.requests").tag("service", serviceName)
                    .tag("status", "5xx").counter();
            if (error5xxCounter != null) {
                errorCount[0] += error5xxCounter.count();
            }

            if (errorCount[0] == 0) {
                meterRegistry.find("http.server.requests").tag("service", serviceName).counters()
                        .forEach(counter -> {
                            String status = counter.getId().getTag("status");
                            if (status != null && (status.startsWith("4") || status.startsWith("5"))) {
                                errorCount[0] += counter.count();
                            }
                        });
            }

            return errorCount[0] / totalCount;

        } catch (Exception e) {
            log.warn("[计算错误率] 系统异常，服务：{}", serviceName, e);
        }

        return 0.0;
    }

    /**
     * 获取活跃连接数
     */
    private Integer getActiveConnections(String serviceName) {
        try {
            Gauge gauge = meterRegistry.find("http.server.connections.active").tag("service", serviceName).gauge();

            if (gauge != null) {
                return (int) gauge.value();
            }

            Gauge connectionsGauge = meterRegistry.find("http.server.connections.active").gauge();
            if (connectionsGauge != null) {
                return (int) connectionsGauge.value();
            }

        } catch (Exception e) {
            log.warn("[获取活跃连接数] 系统异常，服务：{}", serviceName, e);
        }

        return 0;
    }

    /**
     * 存储服务指标到Redis
     */
    private void storeServiceMetrics(String serviceName, ServiceMetricsVO metrics) {
        try {
            String key = "metrics:service:" + serviceName + ":" + System.currentTimeMillis();
            Map<String, Object> data = new HashMap<>();
            data.put("serviceName", metrics.getServiceName());
            data.put("qps", metrics.getQps());
            data.put("tps", metrics.getTps());
            data.put("avgResponseTime", metrics.getAvgResponseTime());
            data.put("errorRate", metrics.getErrorRate());
            data.put("activeConnections", metrics.getActiveConnections());
            data.put("timestamp", java.time.LocalDateTime.now().toString());

            long expireSeconds = 7 * 24 * 60 * 60; // 7天
            redisTemplate.opsForValue().set(key, data, expireSeconds, TimeUnit.SECONDS);
            log.debug("服务指标已存储到Redis，服务：{}", serviceName);

        } catch (Exception e) {
            log.warn("[存储服务指标到Redis] 系统异常，服务：{}", serviceName, e);
        }
    }

    /**
     * 采集业务指标（使用Micrometer直接查询）
     * <p>
     * 替代已废弃的 MetricsCollectorManager.collectBusinessMetrics 使用 Micrometer 标准 API
     * 直接查询指标
     * </p>
     *
     * @param metricName
     *                   指标名称
     * @return 业务指标数据
     */
    private Map<String, Object> collectBusinessMetrics(String metricName) {
        log.debug("开始采集业务指标，指标名称：{}", metricName);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("metricName", metricName);
        metrics.put("timestamp", System.currentTimeMillis());

        try {
            // 1. 尝试查询Gauge类型指标
            Gauge gauge = meterRegistry.find(metricName).gauge();
            if (gauge != null) {
                metrics.put("value", gauge.value());
                metrics.put("type", "gauge");
                log.debug("采集业务指标完成（Gauge），指标：{}，值：{}", metricName, gauge.value());
                return metrics;
            }

            // 2. 尝试查询Counter类型指标
            Counter counter = meterRegistry.find(metricName).counter();
            if (counter != null) {
                metrics.put("value", counter.count());
                metrics.put("type", "counter");
                log.debug("采集业务指标完成（Counter），指标：{}，值：{}", metricName, counter.count());
                return metrics;
            }

            // 3. 尝试查询Timer类型指标
            Timer timer = meterRegistry.find(metricName).timer();
            if (timer != null) {
                metrics.put("value", timer.mean(TimeUnit.MILLISECONDS));
                metrics.put("count", timer.count());
                metrics.put("type", "timer");
                log.debug("采集业务指标完成（Timer），指标：{}，平均值：{}ms", metricName, timer.mean(TimeUnit.MILLISECONDS));
                return metrics;
            }

            // 4. 如果找不到指标，返回默认值
            log.warn("未找到业务指标：{}，返回默认值", metricName);
            metrics.put("value", 0);
            metrics.put("type", "unknown");

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[采集业务指标] 参数异常，metricName: {}, error={}", metricName, e.getMessage());
            metrics.put("value", 0);
            metrics.put("type", "error");
        } catch (Exception e) {
            log.error("[采集业务指标] 系统异常，metricName: {}", metricName, e);
            metrics.put("value", 0);
            metrics.put("type", "error");
        }

        return metrics;
    }

    /**
     * 记录指标到Micrometer和Redis
     * <p>
     * 替代已废弃的 MetricsCollectorManager.recordMetric 使用 Micrometer 标准 API 记录指标
     * </p>
     *
     * @param metricName
     *                   指标名称
     * @param value
     *                   指标值
     * @param tags
     *                   标签（用于分类和查询）
     */
    private void recordMetricToMicrometer(String metricName, Double value, Map<String, String> tags) {
        log.debug("记录指标，名称：{}，值：{}，标签：{}", metricName, value, tags);

        try {
            // 1. 记录到Micrometer（使用Gauge）
            if (tags != null && !tags.isEmpty()) {
                Counter.Builder builder = Counter.builder(metricName);
                tags.forEach(builder::tag);
                builder.register(meterRegistry).increment(value);
            } else {
                Counter.builder(metricName).register(meterRegistry).increment(value);
            }

            // 2. 存储到Redis（可选，用于时序数据查询）
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                StringBuilder keyBuilder = new StringBuilder("metrics:");
                keyBuilder.append(metricName);

                if (tags != null && !tags.isEmpty()) {
                    tags.forEach((k, v) -> keyBuilder.append(":").append(k).append("=").append(v));
                }

                keyBuilder.append(":").append(System.currentTimeMillis());

                String key = keyBuilder.toString();
                if (key == null || key.isEmpty()) {
                    log.warn("指标Key为空，跳过Redis存储，指标名称：{}", metricName);
                    return;
                }

                Map<String, Object> metricData = new HashMap<>();
                metricData.put("metricName", metricName);
                metricData.put("value", value);
                metricData.put("timestamp", java.time.LocalDateTime.now().toString());
                metricData.put("tags", tags);

                long expireSeconds = 7 * 24 * 60 * 60; // 7天
                redisTemplate.opsForValue().set(key, metricData, expireSeconds, TimeUnit.SECONDS);
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[记录指标] 参数异常，metricName: {}, error={}", metricName, e.getMessage());
        } catch (Exception e) {
            log.error("[记录指标] 系统异常，metricName: {}", metricName, e);
        }
    }
}
