package net.lab1024.sa.common.monitor.service.impl;

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

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.monitor.dao.AlertDao;
import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
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
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 使用@Transactional管理事务
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AlertServiceImpl implements AlertService {

    @Resource
    private AlertDao alertDao;

    @Resource
    private AlertRuleDao alertRuleDao;

    @Resource
    private NotificationManager notificationManager;

    @Override
    public Long addAlertRule(AlertRuleAddDTO addDTO) {
        log.info("添加告警规则，规则名称：{}，监控指标：{}", addDTO.getRuleName(), addDTO.getMetricName());

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setRuleName(addDTO.getRuleName());
            alertRule.setRuleDescription(addDTO.getRuleDescription());
            alertRule.setMetricName(addDTO.getMetricName());
            alertRule.setMonitorType(addDTO.getMonitorType());
            alertRule.setConditionOperator(addDTO.getConditionOperator());
            alertRule.setThresholdValue(addDTO.getThresholdValue());
            alertRule.setAlertLevel(addDTO.getAlertLevel());
            alertRule.setStatus(addDTO.getStatus() != null ? addDTO.getStatus() : "ENABLED");
            alertRule.setDurationMinutes(addDTO.getDurationMinutes());
            alertRule.setNotificationChannels(addDTO.getNotificationChannels());
            alertRule.setNotificationUsers(addDTO.getNotificationUsers());
            alertRule.setNotificationInterval(addDTO.getNotificationInterval());
            alertRule.setSuppressionDuration(addDTO.getSuppressionDuration());
            alertRule.setRuleExpression(addDTO.getRuleExpression());
            alertRule.setPriority(addDTO.getPriority());
            alertRule.setTags(addDTO.getTags());
            alertRule.setApplicableServices(addDTO.getApplicableServices());
            alertRule.setApplicableEnvironments(addDTO.getApplicableEnvironments());

            alertRuleDao.insert(alertRule);

            log.info("告警规则添加成功，ID：{}", alertRule.getRuleId());
            return alertRule.getRuleId();

        } catch (Exception e) {
            log.error("添加告警规则失败", e);
            throw e;
        }
    }

    @Override
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

        } catch (Exception e) {
            log.error("分页查询告警规则失败", e);
            return PageResult.of(new ArrayList<>(), 0L, 1, 10);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AlertRuleVO getAlertRuleDetail(Long ruleId) {
        log.debug("获取告警规则详情，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = alertRuleDao.selectById(ruleId);
            if (alertRule == null || alertRule.getDeletedFlag() == 1) {
                return null;
            }

            return convertToVO(alertRule);

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

            alertRuleDao.updateById(alertRule);

            log.info("告警规则启用成功，ID：{}", ruleId);

        } catch (Exception e) {
            log.error("启用告警规则失败，ID：{}", ruleId, e);
            throw e;
        }
    }

    @Override
    public void disableAlertRule(Long ruleId) {
        log.info("禁用告警规则，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setRuleId(ruleId);
            alertRule.setStatus("DISABLED");

            alertRuleDao.updateById(alertRule);

            log.info("告警规则禁用成功，ID：{}", ruleId);

        } catch (Exception e) {
            log.error("禁用告警规则失败，ID：{}", ruleId, e);
            throw e;
        }
    }

    @Override
    public void deleteAlertRule(Long ruleId) {
        log.info("删除告警规则，ID：{}", ruleId);

        try {
            AlertRuleEntity alertRule = new AlertRuleEntity();
            alertRule.setRuleId(ruleId);
            alertRule.setDeletedFlag(1);

            alertRuleDao.updateById(alertRule);
            log.info("告警规则删除成功，ID：{}", ruleId);

        } catch (Exception e) {
            log.error("删除告警规则失败，ID：{}", ruleId, e);
            throw e;
        }
    }

    @Override
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

        } catch (Exception e) {
            log.error("获取告警历史失败", e);
            return PageResult.of(new ArrayList<>(), 0L, 1, 10);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getActiveAlertCount() {
        log.debug("获取活跃告警统计");

        try {
            QueryWrapper<AlertEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "ACTIVE");
            queryWrapper.eq("deleted_flag", 0);

            long totalCount = alertDao.selectCount(queryWrapper);

            // 按级别统计
            Map<String, Long> countByLevel = new HashMap<>();
            countByLevel.put("CRITICAL", alertDao.selectCount(new QueryWrapper<AlertEntity>()
                    .eq("status", "ACTIVE")
                    .eq("alert_level", "CRITICAL")
                    .eq("deleted_flag", 0)));
            countByLevel.put("ERROR", alertDao.selectCount(new QueryWrapper<AlertEntity>()
                    .eq("status", "ACTIVE")
                    .eq("alert_level", "ERROR")
                    .eq("deleted_flag", 0)));
            countByLevel.put("WARNING", alertDao.selectCount(new QueryWrapper<AlertEntity>()
                    .eq("status", "ACTIVE")
                    .eq("alert_level", "WARNING")
                    .eq("deleted_flag", 0)));

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("countByLevel", countByLevel);

            return result;

        } catch (Exception e) {
            log.error("获取活跃告警统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
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
                    .eq("status", "RESOLVED")
                    .eq("deleted_flag", 0));

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalAlerts", totalAlerts);
            statistics.put("resolvedAlerts", resolvedAlerts);
            statistics.put("activeAlerts", totalAlerts - resolvedAlerts);
            statistics.put("resolutionRate", totalAlerts > 0 ? (double) resolvedAlerts / totalAlerts * 100 : 0);

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
            // 将通知类型字符串转换为渠道编码
            // EMAIL(1), SMS(2), WEBHOOK(3), WECHAT(4)
            Integer channel = convertNotificationTypeToChannel(notificationType);

            boolean success = notificationManager.sendTestNotification(channel, recipients);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "测试通知发送成功" : "测试通知发送失败");
            result.put("timestamp", System.currentTimeMillis());

            return result;

        } catch (Exception e) {
            log.error("测试通知失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "测试通知发送失败：" + e.getMessage());
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

    @Override
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

    @Override
    public Map<String, Integer> batchResolveAlerts(List<Long> alertIds, String resolution) {
        log.info("批量解决告警，数量：{}", alertIds.size());

        int successCount = 0;
        int failedCount = 0;

        for (Long alertId : alertIds) {
            try {
                AlertEntity alert = new AlertEntity();
                alert.setAlertId(alertId);
                alert.setStatus("RESOLVED");
                alert.setResolutionNotes(resolution);
                alert.setResolvedTime(LocalDateTime.now());

                alertDao.updateById(alert);
                successCount++;

            } catch (Exception e) {
                log.error("解决告警失败，ID：{}", alertId, e);
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

    @Override
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

        } catch (Exception e) {
            log.error("获取告警趋势失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 转换AlertRuleEntity为VO
     */
    private AlertRuleVO convertToVO(AlertRuleEntity entity) {
        AlertRuleVO vo = new AlertRuleVO();
        vo.setRuleId(entity.getRuleId());
        vo.setRuleName(entity.getRuleName());
        vo.setRuleDescription(entity.getRuleDescription());
        vo.setMetricName(entity.getMetricName());
        vo.setMonitorType(entity.getMonitorType());
        vo.setConditionOperator(entity.getConditionOperator());
        vo.setThresholdValue(entity.getThresholdValue());
        vo.setAlertLevel(entity.getAlertLevel());
        vo.setStatus(entity.getStatus());
        vo.setDurationMinutes(entity.getDurationMinutes());
        vo.setNotificationChannels(entity.getNotificationChannels());
        vo.setNotificationUsers(entity.getNotificationUsers());
        vo.setNotificationInterval(entity.getNotificationInterval());
        vo.setSuppressionDuration(entity.getSuppressionDuration());
        vo.setRuleExpression(entity.getRuleExpression());
        vo.setPriority(entity.getPriority());
        vo.setTags(entity.getTags());
        vo.setApplicableServices(entity.getApplicableServices());
        vo.setApplicableEnvironments(entity.getApplicableEnvironments());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    /**
     * 转换AlertEntity为Map
     */
    private Map<String, Object> convertAlertToMap(AlertEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("alertId", entity.getAlertId());
        map.put("alertLevel", entity.getAlertLevel());
        map.put("alertTitle", entity.getAlertTitle());
        map.put("alertMessage", entity.getAlertMessage());
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
    @Override
    public Long createAlert(AlertEntity alert) {
        log.info("创建报警记录，报警标题：{}，报警级别：{}", alert.getAlertTitle(), alert.getAlertLevel());

        try {
            // 设置创建时间
            if (alert.getCreateTime() == null) {
                alert.setCreateTime(LocalDateTime.now());
            }

            // 设置报警时间
            if (alert.getAlertTime() == null) {
                alert.setAlertTime(LocalDateTime.now());
            }

            // 设置默认状态
            if (alert.getStatus() == null) {
                alert.setStatus("ACTIVE"); // 默认激活状态
            }

            // 设置默认删除标记
            if (alert.getDeletedFlag() == null) {
                alert.setDeletedFlag(0);
            }

            // 插入报警记录
            int result = alertDao.insert(alert);

            if (result > 0 && alert.getAlertId() != null) {
                log.info("报警记录创建成功，报警ID：{}", alert.getAlertId());
                return alert.getAlertId();
            } else {
                log.warn("报警记录创建失败，报警标题：{}", alert.getAlertTitle());
                throw new RuntimeException("创建报警记录失败");
            }

        } catch (Exception e) {
            log.error("创建报警记录异常，报警标题：{}", alert.getAlertTitle(), e);
            throw new RuntimeException("创建报警记录异常: " + e.getMessage(), e);
        }
    }
}
