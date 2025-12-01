package net.lab1024.sa.monitor.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.monitor.dao.AlertDao;
import net.lab1024.sa.monitor.dao.AlertRuleDao;
import net.lab1024.sa.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.monitor.domain.entity.AlertRuleEntity;
import net.lab1024.sa.monitor.domain.form.AlertRuleAddForm;
import net.lab1024.sa.monitor.domain.form.AlertRuleQueryForm;
import net.lab1024.sa.monitor.domain.vo.AlertRuleVO;
import net.lab1024.sa.monitor.manager.NotificationManager;
import net.lab1024.sa.monitor.service.AlertService;

/**
 * 告警管理服务实现类
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Service
public class AlertServiceImpl implements AlertService {

    @Resource
    private AlertDao alertDao;

    @Resource
    private AlertRuleDao alertRuleDao;

    @Resource
    private NotificationManager notificationManager;

    @Override
    public Long addAlertRule(AlertRuleAddForm addForm) {
        log.info("添加告警规则，规则名称：{}，监控指标：{}", addForm.getRuleName(), addForm.getMetricName());

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setRuleName(addForm.getRuleName());
            alertRule.setRuleDescription(addForm.getRuleDescription());
            alertRule.setMetricName(addForm.getMetricName());
            alertRule.setMonitorType(addForm.getMonitorType());
            alertRule.setConditionOperator(addForm.getConditionOperator());
            alertRule.setThresholdValue(addForm.getThresholdValue());
            alertRule.setAlertLevel(addForm.getAlertLevel());
            alertRule.setStatus(addForm.getStatus() != null ? addForm.getStatus() : "ENABLED");
            alertRule.setDurationMinutes(addForm.getDurationMinutes());
            alertRule.setNotificationChannels(addForm.getNotificationChannels());
            alertRule.setNotificationUsers(addForm.getNotificationUsers());
            alertRule.setNotificationInterval(addForm.getNotificationInterval());
            alertRule.setSuppressionDuration(addForm.getSuppressionDuration());
            alertRule.setRuleExpression(addForm.getRuleExpression());
            alertRule.setPriority(addForm.getPriority());
            alertRule.setTags(addForm.getTags());
            alertRule.setApplicableServices(addForm.getApplicableServices());
            alertRule.setApplicableEnvironments(addForm.getApplicableEnvironments());
            alertRule.setCreateTime(LocalDateTime.now());
            alertRule.setUpdateTime(LocalDateTime.now());
            alertRule.setCreateUserId(1L); // 实际应该从当前用户获取
            alertRule.setDeletedFlag(0);

            alertRuleDao.insert(alertRule);

            log.info("告警规则添加成功，ID：{}", alertRule.getRuleId());
            return alertRule.getRuleId();

        } catch (Exception e) {
            log.error("添加告警规则失败", e);
            return null;
        }
    }

    @Override
    public PageResult<AlertRuleVO> queryAlertRulePage(AlertRuleQueryForm queryForm) {
        log.debug("分页查询告警规则");

        try {
            // 模拟分页查询
            List<AlertRuleVO> alertRules = new ArrayList<>();

            AlertRuleVO rule1 = new AlertRuleVO();
            rule1.setRuleId(1L);
            rule1.setRuleName("CPU使用率告警");
            rule1.setMetricName("cpu_usage");
            rule1.setThresholdValue(80.0);
            rule1.setAlertLevel("WARNING");
            rule1.setStatus("ENABLED");
            alertRules.add(rule1);

            AlertRuleVO rule2 = new AlertRuleVO();
            rule2.setRuleId(2L);
            rule2.setRuleName("内存使用率告警");
            rule2.setMetricName("memory_usage");
            rule2.setThresholdValue(85.0);
            rule2.setAlertLevel("ERROR");
            rule2.setStatus("ENABLED");
            alertRules.add(rule2);

            return PageResult.of(alertRules, 2L, 1L, 10L);

        } catch (Exception e) {
            log.error("分页查询告警规则失败", e);
            return PageResult.of(new ArrayList<>(), 0L, 1L, 10L);
        }
    }

    @Override
    public AlertRuleVO getAlertRuleDetail(Long ruleId) {
        log.debug("获取告警规则详情，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = alertRuleDao.selectById(ruleId);
            if (alertRule == null) {
                return null;
            }

            AlertRuleVO vo = new AlertRuleVO();
            vo.setRuleId(alertRule.getRuleId());
            vo.setRuleName(alertRule.getRuleName());
            vo.setRuleDescription(alertRule.getRuleDescription());
            vo.setMetricName(alertRule.getMetricName());
            vo.setThresholdValue(alertRule.getThresholdValue());
            vo.setAlertLevel(alertRule.getAlertLevel());
            vo.setStatus(alertRule.getStatus());

            return vo;

        } catch (Exception e) {
            log.error("获取告警规则详情失败，ID：{}", ruleId, e);
            return null;
        }
    }

    @Override
    public void enableAlertRule(Long ruleId) {
        log.info("启用告警规则，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setRuleId(ruleId);
            alertRule.setStatus("ENABLED");
            alertRule.setUpdateTime(LocalDateTime.now());

            alertRuleDao.updateById(alertRule);

            log.info("告警规则启用成功，ID：{}", ruleId);

        } catch (Exception e) {
            log.error("启用告警规则失败，ID：{}", ruleId, e);
        }
    }

    @Override
    public void disableAlertRule(Long ruleId) {
        log.info("禁用告警规则，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setRuleId(ruleId);
            alertRule.setStatus("DISABLED");
            alertRule.setUpdateTime(LocalDateTime.now());

            alertRuleDao.updateById(alertRule);

            log.info("告警规则禁用成功，ID：{}", ruleId);

        } catch (Exception e) {
            log.error("禁用告警规则失败，ID：{}", ruleId, e);
        }
    }

    @Override
    public void deleteAlertRule(Long ruleId) {
        log.info("删除告警规则，ID：{}", ruleId);

        try {
            alertRuleDao.deleteById(ruleId);
            log.info("告警规则删除成功，ID：{}", ruleId);

        } catch (Exception e) {
            log.error("删除告警规则失败，ID：{}", ruleId, e);
        }
    }

    @Override
    public PageResult<Map<String, Object>> getAlertHistory(Integer pageNum, Integer pageSize, String severity,
            String status, Long startTime, Long endTime) {
        log.debug("获取告警历史，页码：{}，页大小：{}", pageNum, pageSize);

        try {
            LocalDateTime start = startTime != null ? LocalDateTime.ofEpochSecond(startTime / 1000, 0, null) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.ofEpochSecond(endTime / 1000, 0, null) : null;

            List<AlertEntity> alerts;
            if (severity != null && !severity.isEmpty()) {
                alerts = alertDao.selectByAlertLevel(severity, start, end);
            } else {
                alerts = alertDao.selectRecentAlerts(24); // 最近24小时
            }

            List<Map<String, Object>> alertMaps = alerts.stream()
                    .skip((long) (pageNum - 1) * pageSize)
                    .limit(pageSize)
                    .map(this::convertAlertToMap)
                    .collect(Collectors.toList());

            return PageResult.of(alertMaps, (long) alerts.size(), (long) pageNum, (long) pageSize);

        } catch (Exception e) {
            log.error("获取告警历史失败", e);
            return PageResult.of(new ArrayList<>(), 0L, (long) pageNum, (long) pageSize);
        }
    }

    @Override
    public Map<String, Object> getActiveAlertCount() {
        log.debug("获取活跃告警统计");

        try {
            List<AlertEntity> activeAlerts = alertDao.selectActiveAlerts();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", activeAlerts.size());

            Map<String, Long> countByLevel = activeAlerts.stream()
                    .collect(Collectors.groupingBy(AlertEntity::getAlertLevel, Collectors.counting()));

            statistics.put("critical", countByLevel.getOrDefault("CRITICAL", 0L));
            statistics.put("error", countByLevel.getOrDefault("ERROR", 0L));
            statistics.put("warning", countByLevel.getOrDefault("WARNING", 0L));
            statistics.put("info", countByLevel.getOrDefault("INFO", 0L));

            statistics.put("statisticsTime", LocalDateTime.now());

            return statistics;

        } catch (Exception e) {
            log.error("获取活跃告警统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getAlertStatistics(Integer days) {
        log.debug("获取告警统计，天数：{}", days);

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days != null ? days : 7);

            Map<String, Object> statistics = new HashMap<>();

            // 按级别统计
            List<Map<String, Object>> levelStats = alertDao.countAlertsByLevel(startTime, endTime);
            statistics.put("levelStatistics", levelStats);

            // 按类型统计
            List<Map<String, Object>> typeStats = alertDao.countAlertsByType(startTime, endTime);
            statistics.put("typeStatistics", typeStats);

            // 按服务统计
            List<Map<String, Object>> serviceStats = alertDao.countAlertsByService(startTime, endTime);
            statistics.put("serviceStatistics", serviceStats);

            statistics.put("statisticsTime", LocalDateTime.now());
            statistics.put("timeRange", days + " days");

            return statistics;

        } catch (Exception e) {
            log.error("获取告警统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> testNotification(String notificationType, List<String> recipients) {
        log.info("测试通知，类型：{}，接收人：{}", notificationType, recipients);

        try {
            String recipient = recipients != null && !recipients.isEmpty() ? recipients.get(0) : "test@example.com";
            String title = "IOE-DREAM监控服务测试通知";
            String content = "这是一条测试通知，用于验证通知配置是否正确。\n\n测试时间：" + LocalDateTime.now();

            return notificationManager.sendTestNotification(notificationType, recipient, title, content);

        } catch (Exception e) {
            log.error("测试通知失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "测试通知失败：" + e.getMessage());
            return result;
        }
    }

    @Override
    public List<Map<String, Object>> getNotificationChannels() {
        log.debug("获取通知渠道");

        try {
            List<Map<String, Object>> channels = new ArrayList<>();

            Map<String, Object> emailChannel = new HashMap<>();
            emailChannel.put("type", "EMAIL");
            emailChannel.put("name", "邮件通知");
            emailChannel.put("description", "通过邮件发送告警通知");
            emailChannel.put("enabled", true);
            channels.add(emailChannel);

            Map<String, Object> smsChannel = new HashMap<>();
            smsChannel.put("type", "SMS");
            smsChannel.put("name", "短信通知");
            smsChannel.put("description", "通过短信发送严重告警通知");
            smsChannel.put("enabled", true);
            channels.add(smsChannel);

            Map<String, Object> webhookChannel = new HashMap<>();
            webhookChannel.put("type", "WEBHOOK");
            webhookChannel.put("name", "Webhook通知");
            webhookChannel.put("description", "通过HTTP Webhook发送通知");
            webhookChannel.put("enabled", true);
            channels.add(webhookChannel);

            Map<String, Object> wechatChannel = new HashMap<>();
            wechatChannel.put("type", "WECHAT");
            wechatChannel.put("name", "微信通知");
            wechatChannel.put("description", "通过企业微信发送通知");
            wechatChannel.put("enabled", true);
            channels.add(wechatChannel);

            return channels;

        } catch (Exception e) {
            log.error("获取通知渠道失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Integer> batchResolveAlerts(List<Long> alertIds, String resolution) {
        log.info("批量解决告警，数量：{}，解决说明：{}", alertIds.size(), resolution);

        Map<String, Integer> result = new HashMap<>();
        result.put("success", 0);
        result.put("failed", 0);

        for (Long alertId : alertIds) {
            try {
                LocalDateTime resolveTime = LocalDateTime.now();
                Long resolveUserId = 1L; // 实际应该从当前用户获取

                int updateResult = alertDao.updateResolveInfo(alertId, resolveTime, resolveUserId, resolution);
                if (updateResult > 0) {
                    result.put("success", result.get("success") + 1);
                } else {
                    result.put("failed", result.get("failed") + 1);
                }

            } catch (Exception e) {
                log.error("解决告警失败，ID：{}", alertId, e);
                result.put("failed", result.get("failed") + 1);
            }
        }

        log.info("批量解决告警完成，成功：{}，失败：{}", result.get("success"), result.get("failed"));
        return result;
    }

    @Override
    public List<Map<String, Object>> getAlertTrends(Integer days) {
        log.debug("获取告警趋势，天数：{}", days);

        try {
            return alertDao.selectAlertTrends(days);
        } catch (Exception e) {
            log.error("获取告警趋势失败", e);
            return new ArrayList<>();
        }
    }

    // 私有辅助方法

    private Map<String, Object> convertAlertToMap(AlertEntity alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("alertId", alert.getAlertId());
        map.put("alertTitle", alert.getAlertTitle());
        map.put("alertDescription", alert.getAlertDescription());
        map.put("alertLevel", alert.getAlertLevel());
        map.put("alertType", alert.getAlertType());
        map.put("serviceName", alert.getServiceName());
        map.put("instanceId", alert.getInstanceId());
        map.put("status", alert.getStatus());
        map.put("alertSource", alert.getAlertSource());
        map.put("alertValue", alert.getAlertValue());
        map.put("thresholdValue", alert.getThresholdValue());
        map.put("alertTime", alert.getAlertTime());
        map.put("resolveTime", alert.getResolveTime());
        map.put("resolution", alert.getResolution());
        return map;
    }
}
