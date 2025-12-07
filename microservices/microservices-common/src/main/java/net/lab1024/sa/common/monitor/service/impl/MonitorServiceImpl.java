package net.lab1024.sa.common.monitor.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitor.dao.AlertDao;
import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
import net.lab1024.sa.common.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.common.monitor.domain.entity.AlertRuleEntity;
import net.lab1024.sa.common.monitor.domain.vo.AlertRuleVO;
import net.lab1024.sa.common.monitor.domain.vo.ServiceMetricsVO;
import net.lab1024.sa.common.monitor.domain.vo.SystemHealthVO;
import net.lab1024.sa.common.monitor.manager.HealthCheckManager;
import net.lab1024.sa.common.monitor.manager.MetricsCollectorManager;
import net.lab1024.sa.common.monitor.service.MonitorService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 监控服务实现类
 * 整合自ioedream-monitor-service
 *
 * 符合CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 使用@Transactional事务管理
 *
 * 企业级特性：
 * - 实时监控指标采集
 * - 多维度性能分析
 * - 智能告警规则
 * - 告警聚合和降噪
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自monitor-service）
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class MonitorServiceImpl implements MonitorService {

    @Resource
    private AlertDao alertDao;

    @Resource
    private AlertRuleDao alertRuleDao;

    @Resource
    private HealthCheckManager healthCheckManager;

    @Resource
    private MetricsCollectorManager metricsCollectorManager;

    @Override
    public ResponseDTO<SystemHealthVO> getSystemHealth() {
        try {
            SystemHealthVO health = healthCheckManager.checkSystemHealth();
            return ResponseDTO.ok(health);

        } catch (Exception e) {
            log.error("获取系统健康状态失败", e);
            return ResponseDTO.error("获取系统健康状态失败");
        }
    }

    @Override
    public ResponseDTO<ServiceMetricsVO> getServiceMetrics(String serviceName) {
        try {
            ServiceMetricsVO metrics = metricsCollectorManager.collectServiceMetrics(serviceName);
            return ResponseDTO.ok(metrics);

        } catch (Exception e) {
            log.error("获取服务指标失败 - 服务: {}", serviceName, e);
            return ResponseDTO.error("获取服务指标失败");
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getBusinessMetrics(String metricName) {
        try {
            Map<String, Object> metrics = metricsCollectorManager.collectBusinessMetrics(metricName);
            return ResponseDTO.ok(metrics);

        } catch (Exception e) {
            log.error("获取业务指标失败 - 指标: {}", metricName, e);
            return ResponseDTO.error("获取业务指标失败");
        }
    }

    @Override
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

            log.info("告警规则创建成功，规则ID：{}，规则名称：{}", alertRule.getRuleId(), alertRule.getRuleName());
            return ResponseDTO.ok(alertRule.getRuleId());

        } catch (Exception e) {
            log.error("创建告警规则失败，规则名称：{}", ruleVO != null ? ruleVO.getRuleName() : "未知", e);
            return ResponseDTO.error("创建告警规则失败：" + e.getMessage());
        }
    }

    /**
     * 验证告警规则参数
     *
     * @param ruleVO 告警规则VO
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
        String[] validOperators = {"GT", "GTE", "LT", "LTE", "EQ", "NEQ"};
        boolean isValidOperator = false;
        for (String operator : validOperators) {
            if (operator.equals(ruleVO.getConditionOperator().toUpperCase())) {
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
        String[] validLevels = {"INFO", "WARNING", "ERROR", "CRITICAL"};
        boolean isValidLevel = false;
        for (String level : validLevels) {
            if (level.equals(ruleVO.getAlertLevel().toUpperCase())) {
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
     * @param ruleName 规则名称
     * @return true表示已存在，false表示不存在
     */
    private boolean isRuleNameExists(String ruleName) {
        try {
            QueryWrapper<AlertRuleEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rule_name", ruleName);
            queryWrapper.eq("deleted_flag", 0);
            Long count = alertRuleDao.selectCount(queryWrapper);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查规则名称是否存在失败，规则名称：{}", ruleName, e);
            return false;
        }
    }

    /**
     * 将AlertRuleVO转换为AlertRuleEntity
     *
     * @param ruleVO 告警规则VO
     * @return 告警规则实体
     */
    private AlertRuleEntity convertVOToEntity(AlertRuleVO ruleVO) {
        AlertRuleEntity entity = new AlertRuleEntity();
        entity.setRuleName(ruleVO.getRuleName());
        entity.setRuleDescription(ruleVO.getRuleDescription());
        entity.setMetricName(ruleVO.getMetricName());
        entity.setMonitorType(ruleVO.getMonitorType());
        entity.setConditionOperator(ruleVO.getConditionOperator().toUpperCase());
        entity.setThresholdValue(ruleVO.getThresholdValue());
        entity.setAlertLevel(ruleVO.getAlertLevel().toUpperCase());
        entity.setApplicableServices(ruleVO.getApplicableServices());
        entity.setApplicableEnvironments(ruleVO.getApplicableEnvironments());
        entity.setStatus(ruleVO.getStatus());
        entity.setDurationMinutes(ruleVO.getDurationMinutes());
        entity.setNotificationChannels(ruleVO.getNotificationChannels());
        entity.setNotificationUsers(ruleVO.getNotificationUsers());
        entity.setNotificationInterval(ruleVO.getNotificationInterval());
        entity.setSuppressionDuration(ruleVO.getSuppressionDuration());
        entity.setRuleExpression(ruleVO.getRuleExpression());
        entity.setPriority(ruleVO.getPriority());
        entity.setTags(ruleVO.getTags());
        return entity;
    }

    /**
     * 设置默认值
     *
     * @param alertRule 告警规则实体
     */
    private void setDefaultValues(AlertRuleEntity alertRule) {
        // 设置默认状态
        if (alertRule.getStatus() == null || alertRule.getStatus().trim().isEmpty()) {
            alertRule.setStatus("ENABLED");
        }

        // 设置默认优先级（根据告警级别）
        if (alertRule.getPriority() == null) {
            alertRule.setPriority(calculateDefaultPriority(alertRule.getAlertLevel()));
        }

        // 设置默认持续时间（如果未提供）
        if (alertRule.getDurationMinutes() == null) {
            alertRule.setDurationMinutes(5); // 默认5分钟
        }

        // 设置默认通知频率（如果未提供）
        if (alertRule.getNotificationInterval() == null) {
            alertRule.setNotificationInterval(60); // 默认60分钟
        }

        // 设置默认抑制时间（如果未提供）
        if (alertRule.getSuppressionDuration() == null) {
            alertRule.setSuppressionDuration(30); // 默认30分钟
        }
    }

    /**
     * 根据告警级别计算默认优先级
     *
     * @param alertLevel 告警级别
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

    @Override
    public void triggerAlert(String alertType, String alertMessage, Integer severity) {
        try {
            log.warn("触发告警 - 类型: {}, 消息: {}, 严重程度: {}", alertType, alertMessage, severity);

            AlertEntity alert = new AlertEntity();
            alert.setAlertType(alertType);
            alert.setAlertDescription(alertMessage);
            alert.setAlertLevel(getSeverityLevel(severity));
            alert.setStatus("ACTIVE");
            alert.setAlertTime(LocalDateTime.now());
            alert.setCreateTime(LocalDateTime.now());

            alertDao.insert(alert);

        } catch (Exception e) {
            log.error("触发告警失败", e);
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getAlertHistory(Integer limit) {
        try {
            List<AlertEntity> alerts = alertDao.selectRecentAlerts(limit);
            // 转换为Map
            List<Map<String, Object>> alertList = alerts.stream()
                    .map(alert -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", alert.getAlertId() != null ? alert.getAlertId() : 0L);
                        map.put("alertType", alert.getAlertType() != null ? alert.getAlertType() : "");
                        map.put("alertDescription",
                                alert.getAlertDescription() != null ? alert.getAlertDescription() : "");
                        map.put("alertLevel", alert.getAlertLevel() != null ? alert.getAlertLevel() : "");
                        map.put("status", alert.getStatus() != null ? alert.getStatus() : "");
                        map.put("alertTime", alert.getAlertTime() != null ? alert.getAlertTime().toString() : "");
                        map.put("createTime", alert.getCreateTime() != null ? alert.getCreateTime().toString() : "");
                        return map;
                    })
                    .collect(Collectors.toList());
            return ResponseDTO.ok(alertList);

        } catch (Exception e) {
            log.error("获取告警历史失败", e);
            return ResponseDTO.error("获取告警历史失败");
        }
    }

    @Override
    public void recordMetric(String metricName, Double value, Map<String, String> tags) {
        try {
            metricsCollectorManager.recordMetric(metricName, value, tags);

        } catch (Exception e) {
            log.error("记录性能指标失败 - 指标: {}", metricName, e);
        }
    }

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
}
