package net.lab1024.sa.consume.manager.device;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.device.DeviceHealthMetricEntity;
import net.lab1024.sa.common.entity.device.QualityAlarmEntity;
import net.lab1024.sa.common.entity.device.QualityDiagnosisRuleEntity;
import net.lab1024.sa.common.entity.organization.DeviceEntity;
import net.lab1024.sa.consume.dao.device.QualityDiagnosisRuleDao;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 质量规则引擎 - 执行诊断规则并生成告警
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Component
public class QualityRuleEngine {

    @Resource
    private QualityDiagnosisRuleDao qualityDiagnosisRuleDao;

    /**
     * 执行规则诊断
     *
     * @param device 设备实体
     * @param metrics 健康指标列表
     * @return 触发的告警列表
     */
    public List<QualityAlarmEntity> executeRuleDiagnosis(DeviceEntity device, List<DeviceHealthMetricEntity> metrics) {
        log.debug("[质量规则引擎] 执行规则诊断: deviceId={}", device.getDeviceId());

        List<QualityAlarmEntity> alarms = new ArrayList<>();

        // 1. 查询适用的诊断规则
        List<QualityDiagnosisRuleEntity> rules = queryApplicableRules(device.getDeviceType());

        // 2. 执行每条规则
        for (QualityDiagnosisRuleEntity rule : rules) {
            QualityAlarmEntity alarm = evaluateRule(device, metrics, rule);
            if (alarm != null) {
                alarms.add(alarm);
            }
        }

        log.info("[质量规则引擎] 规则诊断完成: deviceId={}, rules={}, alarms={}",
                device.getDeviceId(), rules.size(), alarms.size());

        return alarms;
    }

    /**
     * 查询适用的诊断规则
     */
    private List<QualityDiagnosisRuleEntity> queryApplicableRules(Integer deviceType) {
        LambdaQueryWrapper<QualityDiagnosisRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QualityDiagnosisRuleEntity::getDeviceType, deviceType)
                .eq(QualityDiagnosisRuleEntity::getRuleStatus, 1)  // 启用的规则
                .orderByAsc(QualityDiagnosisRuleEntity::getAlarmLevel);  // 按告警级别排序

        return qualityDiagnosisRuleDao.selectList(queryWrapper);
    }

    /**
     * 评估单条规则
     *
     * @param device 设备实体
     * @param metrics 健康指标列表
     * @param rule 诊断规则
     * @return 如果触发告警则返回告警实体，否则返回null
     */
    private QualityAlarmEntity evaluateRule(DeviceEntity device, List<DeviceHealthMetricEntity> metrics, QualityDiagnosisRuleEntity rule) {
        log.debug("[质量规则引擎] 评估规则: deviceId={}, ruleCode={}", device.getDeviceId(), rule.getRuleCode());

        // 特殊处理：在线状态规则
        if ("online_status".equals(rule.getMetricType())) {
            return evaluateOnlineStatusRule(device, rule);
        }

        // 查找对应的指标值
        DeviceHealthMetricEntity latestMetric = findLatestMetric(metrics, rule.getMetricType());
        if (latestMetric == null) {
            log.debug("[质量规则引擎] 指标不存在: metricType={}", rule.getMetricType());
            return null;
        }

        // 根据规则表达式判断是否触发告警
        boolean triggered = evaluateCondition(latestMetric.getMetricValue(), rule);

        if (triggered) {
            // 创建告警实体
            QualityAlarmEntity alarm = new QualityAlarmEntity();
            alarm.setDeviceId(device.getDeviceId());
            alarm.setDeviceName(device.getDeviceName());
            alarm.setRuleId(rule.getRuleId());
            alarm.setAlarmLevel(rule.getAlarmLevel());
            alarm.setAlarmTitle(rule.getRuleName());
            alarm.setAlarmContent(buildAlarmContent(device, latestMetric, rule));
            alarm.setAlarmStatus(1);  // 待处理
            alarm.setCreateTime(LocalDateTime.now());

            log.info("[质量规则引擎] 触发告警: deviceId={}, ruleCode={}, value={}",
                    device.getDeviceId(), rule.getRuleCode(), latestMetric.getMetricValue());

            return alarm;
        }

        return null;
    }

    /**
     * 评估在线状态规则
     */
    private QualityAlarmEntity evaluateOnlineStatusRule(DeviceEntity device, QualityDiagnosisRuleEntity rule) {
        Integer status = device.getStatus();

        // 离线状态 = 0
        boolean triggered = (status == null || status == 2 || status == 3);

        if (triggered) {
            QualityAlarmEntity alarm = new QualityAlarmEntity();
            alarm.setDeviceId(device.getDeviceId());
            alarm.setDeviceName(device.getDeviceName());
            alarm.setRuleId(rule.getRuleId());
            alarm.setAlarmLevel(rule.getAlarmLevel());
            alarm.setAlarmTitle(rule.getRuleName());
            alarm.setAlarmContent(String.format("设备状态异常: %s", getStatusName(status)));
            alarm.setAlarmStatus(1);
            alarm.setCreateTime(LocalDateTime.now());

            return alarm;
        }

        return null;
    }

    /**
     * 查找最新的指标值
     */
    private DeviceHealthMetricEntity findLatestMetric(List<DeviceHealthMetricEntity> metrics, String metricType) {
        if (metrics == null || metrics.isEmpty()) {
            return null;
        }

        return metrics.stream()
                .filter(m -> metricType.equals(m.getMetricType()))
                .max(Comparator.comparing(DeviceHealthMetricEntity::getCollectTime))
                .orElse(null);
    }

    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(BigDecimal value, QualityDiagnosisRuleEntity rule) {
        BigDecimal threshold = rule.getThresholdValue();
        if (threshold == null) {
            return false;
        }

        String expression = rule.getRuleExpression();
        int comparison = value.compareTo(threshold);

        return switch (expression) {
            case "eq" -> comparison == 0;  // 等于
            case "gt" -> comparison > 0;   // 大于
            case "lt" -> comparison < 0;   // 小于
            case "gte" -> comparison >= 0; // 大于等于
            case "lte" -> comparison <= 0; // 小于等于
            default -> false;
        };
    }

    /**
     * 构建告警内容
     */
    private String buildAlarmContent(DeviceEntity device, DeviceHealthMetricEntity metric, QualityDiagnosisRuleEntity rule) {
        return String.format("设备【%s】指标【%s】当前值【%s %s】，超过阈值【%s %s】",
                device.getDeviceName(),
                metric.getMetricType(),
                metric.getMetricValue(),
                metric.getMetricUnit(),
                rule.getThresholdValue(),
                metric.getMetricUnit());
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "在线";
            case 2 -> "离线";
            case 3 -> "故障";
            case 4 -> "停用";
            default -> "未知";
        };
    }
}
