package net.lab1024.sa.common.monitor.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.monitor.dao.AlertDao;
import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
import net.lab1024.sa.common.monitor.domain.constant.SecurityAlertConstants;
import net.lab1024.sa.common.monitor.domain.dto.AlertRuleAddDTO;
import net.lab1024.sa.common.monitor.domain.dto.AlertRuleQueryDTO;
import net.lab1024.sa.common.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.common.monitor.domain.entity.AlertRuleEntity;
import net.lab1024.sa.common.monitor.domain.vo.AlertRuleVO;
import net.lab1024.sa.common.monitor.manager.NotificationManager;
import net.lab1024.sa.common.monitor.service.AlertService;

/**
 * 告警管理服务实现类
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解标识服务实现
 * - 使用@Resource依赖注入（符合架构规范）
 * - 使用@Transactional管理事务
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AlertServiceImpl implements AlertService {


    @Resource
    private AlertDao alertDao;

    @Resource
    private AlertRuleDao alertRuleDao;

    @Resource
    private NotificationManager notificationManager;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Observed(name = "alert.rule.add", contextualName = "alert-rule-add")
    public Long addAlertRule(AlertRuleAddDTO addDTO) {
        log.info("添加告警规则，规则名称：{}，监控指标：{}", addDTO.getRuleName(), addDTO.getMetricName());

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setRuleName(addDTO.getRuleName());
            alertRule.setRuleType(addDTO.getMonitorType()); // 使用monitorType作为ruleType
            // 将String类型的alertLevel转换为Integer（使用安防告警标准：P1=1, P2=2, P3=3, P4=4）
            Integer alertLevelInt = SecurityAlertConstants.AlertLevel.fromString(addDTO.getAlertLevel());
            alertRule.setAlertLevel(alertLevelInt);
            // notifyChannels是List<String>，需要转换为String（JSON格式或逗号分隔）
            String notifyChannelsStr = addDTO.getNotificationChannels() != null
                    ? String.join(",", addDTO.getNotificationChannels())
                    : null;
            alertRule.setNotifyChannels(notifyChannelsStr);
            alertRule.setStatus(addDTO.getStatus() != null && "ENABLED".equals(addDTO.getStatus()) ? 1 : 0); // 转换为Integer
            alertRule.setRemark(addDTO.getRuleDescription());

            // 将详细配置序列化到ruleConfig JSON中
            Map<String, Object> ruleConfig = new HashMap<>();
            ruleConfig.put("ruleDescription", addDTO.getRuleDescription());
            ruleConfig.put("metricName", addDTO.getMetricName());
            ruleConfig.put("monitorType", addDTO.getMonitorType());
            ruleConfig.put("conditionOperator", addDTO.getConditionOperator());
            ruleConfig.put("thresholdValue", addDTO.getThresholdValue());
            ruleConfig.put("durationMinutes", addDTO.getDurationMinutes());
            ruleConfig.put("notificationUsers", addDTO.getNotificationUsers());
            ruleConfig.put("notificationInterval", addDTO.getNotificationInterval());
            ruleConfig.put("suppressionDuration", addDTO.getSuppressionDuration());
            ruleConfig.put("ruleExpression", addDTO.getRuleExpression());
            ruleConfig.put("priority", addDTO.getPriority());
            ruleConfig.put("tags", addDTO.getTags());
            ruleConfig.put("applicableServices", addDTO.getApplicableServices());
            ruleConfig.put("applicableEnvironments", addDTO.getApplicableEnvironments());

            try {
                String ruleConfigJson = objectMapper.writeValueAsString(ruleConfig);
                alertRule.setRuleConfig(ruleConfigJson);
            } catch (Exception e) {
                log.error("[告警服务] 序列化ruleConfig失败", e);
                throw new SystemException("ALERT_RULE_CONFIG_SERIALIZE_ERROR", "告警规则配置序列化失败", e);
            }

            alertRuleDao.insert(alertRule);

            log.info("告警规则添加成功，ID：{}", alertRule.getRuleId());
            return alertRule.getRuleId();

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 添加告警规则参数异常, error={}", e.getMessage());
            throw new ParamException("ALERT_RULE_ADD_PARAM_ERROR", "添加告警规则参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[告警服务] 添加告警规则系统异常", e);
            throw new SystemException("ALERT_RULE_ADD_SYSTEM_ERROR", "添加告警规则系统异常", e);
        }
    }

    @Override
    @Observed(name = "alert.rule.queryPage", contextualName = "alert-rule-query-page")
    @Transactional(readOnly = true)
    public PageResult<AlertRuleVO> queryAlertRulePage(AlertRuleQueryDTO queryDTO) {
        log.debug("分页查询告警规则");

        try {
            QueryWrapper<AlertRuleEntity> queryWrapper = new QueryWrapper<>();

            if (queryDTO.getRuleName() != null && !queryDTO.getRuleName().trim().isEmpty()) {
                queryWrapper.like("rule_name", queryDTO.getRuleName());
            }

            if (queryDTO.getMetricName() != null && !queryDTO.getMetricName().trim().isEmpty()) {
                queryWrapper.eq("metric_name", queryDTO.getMetricName());
            }

            if (queryDTO.getStatus() != null && !queryDTO.getStatus().trim().isEmpty()) {
                queryWrapper.eq("status", queryDTO.getStatus());
            }

            queryWrapper.eq("deleted_flag", 0);
            queryWrapper.orderByDesc("create_time");

            Page<AlertRuleEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            IPage<AlertRuleEntity> pageResult = alertRuleDao.selectPage(page, queryWrapper);

            List<AlertRuleVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return PageResult.of(voList, pageResult.getTotal(), queryDTO.getPageNum(),
                    queryDTO.getPageSize());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 分页查询告警规则参数异常, error={}", e.getMessage());
            return PageResult.of(new ArrayList<>(), 0L, 1, 10);
        } catch (Exception e) {
            log.error("[告警服务] 分页查询告警规则系统异常", e);
            return PageResult.of(new ArrayList<>(), 0L, 1, 10);
        }
    }

    @Override
    @Observed(name = "alert.rule.getDetail", contextualName = "alert-rule-get-detail")
    @Transactional(readOnly = true)
    public AlertRuleVO getAlertRuleDetail(Long ruleId) {
        log.debug("获取告警规则详情，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = alertRuleDao.selectById(ruleId);
            if (alertRule == null || alertRule.getDeletedFlag() == 1) {
                return null;
            }

            return convertToVO(alertRule);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 获取告警规则详情参数异常, ruleId={}, error={}", ruleId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("[告警服务] 获取告警规则详情系统异常, ruleId={}", ruleId, e);
            return null;
        }
    }

    @Override
    @Observed(name = "alert.rule.enable", contextualName = "alert-rule-enable")
    public void enableAlertRule(Long ruleId) {
        log.info("启用告警规则，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setRuleId(ruleId);
            alertRule.setStatus(1); // 1-启用

            alertRuleDao.updateById(alertRule);

            log.info("告警规则启用成功，ID：{}", ruleId);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 启用告警规则参数异常, ruleId={}, error={}", ruleId, e.getMessage());
            throw new ParamException("ALERT_RULE_ENABLE_PARAM_ERROR", "启用告警规则参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[告警服务] 启用告警规则系统异常, ruleId={}", ruleId, e);
            throw new SystemException("ALERT_RULE_ENABLE_SYSTEM_ERROR", "启用告警规则系统异常", e);
        }
    }

    @Override
    @Observed(name = "alert.rule.disable", contextualName = "alert-rule-disable")
    public void disableAlertRule(Long ruleId) {
        log.info("禁用告警规则，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setId(ruleId);
            alertRule.setStatus(0); // 0-禁用 1-启用

            alertRuleDao.updateById(alertRule);

            log.info("告警规则禁用成功，ID：{}", ruleId);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 禁用告警规则参数异常, ruleId={}, error={}", ruleId, e.getMessage());
            throw new ParamException("ALERT_RULE_DISABLE_PARAM_ERROR", "禁用告警规则参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[告警服务] 禁用告警规则系统异常, ruleId={}", ruleId, e);
            throw new SystemException("ALERT_RULE_DISABLE_SYSTEM_ERROR", "禁用告警规则系统异常", e);
        }
    }

    @Override
    @Observed(name = "alert.rule.delete", contextualName = "alert-rule-delete")
    public void deleteAlertRule(Long ruleId) {
        log.info("删除告警规则，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setRuleId(ruleId);
            alertRule.setDeletedFlag(1);

            alertRuleDao.updateById(alertRule);
            log.info("告警规则删除成功，ID：{}", ruleId);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 删除告警规则参数异常, ruleId={}, error={}", ruleId, e.getMessage());
            throw new ParamException("ALERT_RULE_DELETE_PARAM_ERROR", "删除告警规则参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[告警服务] 删除告警规则系统异常, ruleId={}", ruleId, e);
            throw new SystemException("ALERT_RULE_DELETE_SYSTEM_ERROR", "删除告警规则系统异常", e);
        }
    }

    @Observed(name = "alert.history.get", contextualName = "alert-history-get")
    @Transactional(readOnly = true)
    public PageResult<Map<String, Object>> getAlertHistory(Integer pageNum, Integer pageSize, String severity,
            String status, Long startTime, Long endTime) {
        log.debug("获取告警历史，页码：{}，页大小：{}", pageNum, pageSize);

        try {
            QueryWrapper<AlertEntity> queryWrapper = new QueryWrapper<>();

            if (severity != null && !severity.trim().isEmpty()) {
                queryWrapper.eq("alert_level", severity);
            }

            if (status != null && !status.trim().isEmpty()) {
                queryWrapper.eq("status", status);
            }

            if (startTime != null) {
                queryWrapper.ge("create_time",
                        LocalDateTime.ofEpochSecond(startTime / 1000, 0, java.time.ZoneOffset.ofHours(8)));
            }

            if (endTime != null) {
                queryWrapper.le("create_time",
                        LocalDateTime.ofEpochSecond(endTime / 1000, 0, java.time.ZoneOffset.ofHours(8)));
            }

            queryWrapper.orderByDesc("create_time");

            Page<AlertEntity> page = new Page<>(pageNum, pageSize);
            IPage<AlertEntity> pageResult = alertDao.selectPage(page, queryWrapper);

            List<Map<String, Object>> resultList = pageResult.getRecords().stream()
                    .map(this::convertAlertToMap)
                    .collect(Collectors.toList());

            return PageResult.of(resultList, pageResult.getTotal(), pageNum, pageSize);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 获取告警历史参数异常, error={}", e.getMessage());
            return PageResult.of(new ArrayList<>(), 0L, 1, 10);
        } catch (Exception e) {
            log.error("[告警服务] 获取告警历史系统异常", e);
            return PageResult.of(new ArrayList<>(), 0L, 1, 10);
        }
    }

    @Observed(name = "alert.active.count", contextualName = "alert-active-count")
    @Transactional(readOnly = true)
    public Map<String, Object> getActiveAlertCount() {
        log.debug("获取活跃告警统计");

        try {
            QueryWrapper<AlertEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", SecurityAlertConstants.AlertStatus.PENDING); // 使用安防告警标准状态（待处理）
            queryWrapper.eq("deleted_flag", 0);

            long totalCount = alertDao.selectCount(queryWrapper);

            // 按级别统计（使用安防告警标准级别：P1/P2/P3/P4）
            Map<String, Long> countByLevel = new HashMap<>();
            countByLevel.put("P1", alertDao.selectCount(new QueryWrapper<AlertEntity>()
                    .eq("status", SecurityAlertConstants.AlertStatus.PENDING)
                    .eq("alert_level", SecurityAlertConstants.AlertLevel.P1_URGENT)
                    .eq("deleted_flag", 0)));
            countByLevel.put("P2", alertDao.selectCount(new QueryWrapper<AlertEntity>()
                    .eq("status", SecurityAlertConstants.AlertStatus.PENDING)
                    .eq("alert_level", SecurityAlertConstants.AlertLevel.P2_IMPORTANT)
                    .eq("deleted_flag", 0)));
            countByLevel.put("P3", alertDao.selectCount(new QueryWrapper<AlertEntity>()
                    .eq("status", SecurityAlertConstants.AlertStatus.PENDING)
                    .eq("alert_level", SecurityAlertConstants.AlertLevel.P3_NORMAL)
                    .eq("deleted_flag", 0)));
            countByLevel.put("P4", alertDao.selectCount(new QueryWrapper<AlertEntity>()
                    .eq("status", SecurityAlertConstants.AlertStatus.PENDING)
                    .eq("alert_level", SecurityAlertConstants.AlertLevel.P4_INFO)
                    .eq("deleted_flag", 0)));

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("countByLevel", countByLevel);

            return result;

        } catch (Exception e) {
            log.error("[告警服务] 获取活跃告警统计系统异常", e);
            return new HashMap<>();
        }
    }

    @Observed(name = "alert.statistics.get", contextualName = "alert-statistics-get")
    @Transactional(readOnly = true)
    public Map<String, Object> getAlertStatistics(Integer days) {
        log.debug("获取告警统计，天数：{}", days);

        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days != null ? days : 7);

            QueryWrapper<AlertEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.ge("create_time", startTime);
            queryWrapper.eq("deleted_flag", 0);

            long totalAlerts = alertDao.selectCount(queryWrapper);
            long resolvedAlerts = alertDao.selectCount(new QueryWrapper<AlertEntity>()
                    .ge("create_time", startTime)
                    .eq("status", SecurityAlertConstants.AlertStatus.RESOLVED) // 使用安防告警标准状态
                    .eq("deleted_flag", 0));

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalAlerts", totalAlerts);
            statistics.put("resolvedAlerts", resolvedAlerts);
            statistics.put("activeAlerts", totalAlerts - resolvedAlerts);
            statistics.put("resolutionRate", totalAlerts > 0 ? (double) resolvedAlerts / totalAlerts * 100 : 0);

            return statistics;

        } catch (Exception e) {
            log.error("[告警服务] 获取告警统计系统异常", e);
            return new HashMap<>();
        }
    }

    @Observed(name = "alert.notification.test", contextualName = "alert-notification-test")
    public Map<String, Object> testNotification(String notificationType, List<String> recipients) {
        log.info("测试通知，类型：{}，接收人：{}", notificationType, recipients);

        try {
            // 将通知类型字符串转换为渠道编码
            // EMAIL(1), SMS(2), WEBHOOK(3), WECHAT(4)
            Integer channel = convertNotificationTypeToChannel(notificationType);

            boolean success = notificationManager.sendTestNotification(channel, recipients);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "测试通知发送成功" : "测试通知发送失败");
            result.put("timestamp", System.currentTimeMillis());

            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 测试通知参数异常, error={}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "测试通知参数异常：" + e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("[告警服务] 测试通知系统异常", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "测试通知系统异常：" + e.getMessage());
            return result;
        }
    }

    /**
     * 将通知类型字符串转换为渠道编码
     */
    private Integer convertNotificationTypeToChannel(String notificationType) {
        if (notificationType == null) {
            return 1; // 默认邮件
        }

        switch (notificationType.toUpperCase()) {
            case "EMAIL":
                return 1;
            case "SMS":
                return 2;
            case "WEBHOOK":
                return 3;
            case "WECHAT":
                return 4;
            default:
                return 1; // 默认邮件
        }
    }

    @Observed(name = "alert.notification.channels", contextualName = "alert-notification-channels")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getNotificationChannels() {
        log.debug("获取通知渠道");

        List<Map<String, Object>> channels = new ArrayList<>();

        channels.add(Map.of("type", "EMAIL", "name", "邮件", "enabled", true));
        channels.add(Map.of("type", "SMS", "name", "短信", "enabled", true));
        channels.add(Map.of("type", "WEBHOOK", "name", "Webhook", "enabled", true));
        channels.add(Map.of("type", "WECHAT", "name", "微信", "enabled", true));

        return channels;
    }

    @Observed(name = "alert.batch.resolve", contextualName = "alert-batch-resolve")
    public Map<String, Integer> batchResolveAlerts(List<Long> alertIds, String resolution) {
        log.info("批量解决告警，数量：{}", alertIds.size());

        int successCount = 0;
        int failedCount = 0;

        for (Long alertId : alertIds) {
            try {
                AlertEntity alert = new AlertEntity();
                alert.setId(alertId);
                alert.setStatus(2); // 2-已解决（假设：0-未处理 1-处理中 2-已解决）
                // AlertEntity中没有resolutionNotes和resolvedTime字段，使用handleRemark和handleTime
                alert.setHandleRemark(resolution);
                alert.setHandleTime(LocalDateTime.now());

                alertDao.updateById(alert);
                successCount++;

            } catch (Exception e) {
                log.error("[告警服务] 解决告警系统异常, alertId={}", alertId, e);
                failedCount++;
            }
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("totalCount", alertIds.size());

        log.info("批量解决告警完成，成功：{}，失败：{}", successCount, failedCount);

        return result;
    }

    @Observed(name = "alert.trends.get", contextualName = "alert-trends-get")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAlertTrends(Integer days) {
        log.debug("获取告警趋势，天数：{}", days);

        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days != null ? days : 7);

            QueryWrapper<AlertEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.ge("create_time", startTime);
            queryWrapper.eq("deleted_flag", 0);
            queryWrapper.select("DATE(create_time) as date", "COUNT(*) as count", "alert_level");
            queryWrapper.groupBy("DATE(create_time)", "alert_level");
            queryWrapper.orderByAsc("DATE(create_time)");

            List<Map<String, Object>> trends = alertDao.selectMaps(queryWrapper);

            return trends;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 获取告警趋势参数异常, error={}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("[告警服务] 获取告警趋势系统异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 转换AlertRuleEntity为VO
     */
    private AlertRuleVO convertToVO(AlertRuleEntity entity) {
        AlertRuleVO vo = new AlertRuleVO();
        vo.setId(entity.getRuleId());
        vo.setRuleName(entity.getRuleName());
        vo.setMonitorType(entity.getRuleType());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        // 从rule_config JSON中解析详细配置
        if (entity.getRuleConfig() != null && !entity.getRuleConfig().isEmpty()) {
            try {
                Map<String, Object> ruleConfig = objectMapper.readValue(
                        entity.getRuleConfig(),
                        new TypeReference<Map<String, Object>>() {
                        });
                vo.setRuleDescription((String) ruleConfig.get("ruleDescription"));
                vo.setMetricName((String) ruleConfig.get("metricName"));
                vo.setConditionOperator((String) ruleConfig.get("conditionOperator"));
                if (ruleConfig.get("thresholdValue") != null) {
                    vo.setThresholdValue(Double.valueOf(ruleConfig.get("thresholdValue").toString()));
                }
                if (ruleConfig.get("durationMinutes") != null) {
                    vo.setDurationMinutes(Integer.valueOf(ruleConfig.get("durationMinutes").toString()));
                }
                if (ruleConfig.get("notificationChannels") != null) {
                    vo.setNotificationChannels((List<String>) ruleConfig.get("notificationChannels"));
                }
                if (ruleConfig.get("notificationUsers") != null) {
                    vo.setNotificationUsers((List<Long>) ruleConfig.get("notificationUsers"));
                }
                if (ruleConfig.get("notificationInterval") != null) {
                    vo.setNotificationInterval(Integer.valueOf(ruleConfig.get("notificationInterval").toString()));
                }
                if (ruleConfig.get("suppressionDuration") != null) {
                    vo.setSuppressionDuration(Integer.valueOf(ruleConfig.get("suppressionDuration").toString()));
                }
                vo.setRuleExpression((String) ruleConfig.get("ruleExpression"));
                if (ruleConfig.get("priority") != null) {
                    vo.setPriority(Integer.valueOf(ruleConfig.get("priority").toString()));
                }
                vo.setTags((List<String>) ruleConfig.get("tags"));
                vo.setApplicableServices((List<String>) ruleConfig.get("applicableServices"));
                vo.setApplicableEnvironments((List<String>) ruleConfig.get("applicableEnvironments"));
            } catch (Exception e) {
                log.warn("[告警服务] 解析rule_config JSON失败, ruleId={}, error={}", entity.getRuleId(), e.getMessage());
            }
        }

        // 告警级别转换：1=CRITICAL, 2=ERROR, 3=WARNING, 4=INFO
        if (entity.getAlertLevel() != null) {
            switch (entity.getAlertLevel()) {
                case 1:
                    vo.setAlertLevel("CRITICAL");
                    break;
                case 2:
                    vo.setAlertLevel("ERROR");
                    break;
                case 3:
                    vo.setAlertLevel("WARNING");
                    break;
                case 4:
                    vo.setAlertLevel("INFO");
                    break;
                default:
                    vo.setAlertLevel("ERROR");
                    break;
            }
        }

        // 状态转换：1=ENABLED, 0=DISABLED
        vo.setStatus(entity.getStatus() != null && entity.getStatus() == 1 ? "ENABLED" : "DISABLED");

        return vo;
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
     * 将Integer类型的告警级别转换为String（使用安防告警标准）
     *
     * @param alertLevel 告警级别整数
     * @return 告警级别字符串（P1/P2/P3/P4）
     */
    private String convertAlertLevelToString(Integer alertLevel) {
        return SecurityAlertConstants.AlertLevel.toString(alertLevel);
    }

    /**
     * 转换AlertEntity为Map
     */
    private Map<String, Object> convertAlertToMap(AlertEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("alertId", entity.getId());
        map.put("alertLevel", entity.getAlertLevel());
        map.put("alertTitle", entity.getAlertTitle());
        map.put("alertMessage", entity.getAlertContent());
        map.put("status", entity.getStatus());
        map.put("createTime", entity.getCreateTime());
        return map;
    }

    /**
     * 创建报警记录
     * <p>
     * 用于设备协议推送报警记录
     * </p>
     *
     * @param alert 报警实体
     * @return 创建的报警ID
     */
    @Observed(name = "alert.create", contextualName = "alert-create")
    public Long createAlert(AlertEntity alert) {
        log.info("创建报警记录，报警标题：{}，报警级别：{}", alert.getAlertTitle(), alert.getAlertLevel());

        try {
            // 设置创建时间
            if (alert.getCreateTime() == null) {
                alert.setCreateTime(LocalDateTime.now());
            }

            // 设置默认状态（0-未处理 1-处理中 2-已解决）
            if (alert.getStatus() == null) {
                alert.setStatus(0); // 默认未处理状态
            }

            // 插入报警记录
            int result = alertDao.insert(alert);

            if (result > 0 && alert.getId() != null) {
                log.info("报警记录创建成功，报警ID：{}", alert.getId());
                return alert.getId();
            } else {
                log.warn("[告警服务] 报警记录创建失败，报警标题：{}", alert.getAlertTitle());
                throw new BusinessException("ALERT_CREATE_FAILED", "创建报警记录失败");
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[告警服务] 创建报警记录参数异常, alertTitle={}, error={}", alert.getAlertTitle(), e.getMessage());
            throw new ParamException("ALERT_CREATE_PARAM_ERROR", "创建报警记录参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[告警服务] 创建报警记录系统异常，报警标题：{}", alert.getAlertTitle(), e);
            throw new SystemException("ALERT_CREATE_SYSTEM_ERROR", "创建报警记录系统异常", e);
        }
    }
}

