package net.lab1024.sa.consume.manager.device;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.device.DeviceHealthMetricEntity;
import net.lab1024.sa.common.entity.device.DeviceQualityRecordEntity;
import net.lab1024.sa.common.entity.device.QualityAlarmEntity;
import net.lab1024.sa.common.entity.device.QualityDiagnosisRuleEntity;
import net.lab1024.sa.common.entity.organization.DeviceEntity;
import net.lab1024.sa.consume.dao.device.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备质量诊断管理器 - 核心业务编排
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Component
public class DeviceQualityManager {

    @Resource
    private DeviceQualityDao deviceQualityDao;

    @Resource
    private DeviceHealthMetricDao deviceHealthMetricDao;

    @Resource
    private QualityDiagnosisRuleDao qualityDiagnosisRuleDao;

    @Resource
    private QualityAlarmDao qualityAlarmDao;

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 执行设备质量诊断（完整流程）
     *
     * @param deviceId 设备ID
     * @return 质量记录ID
     */
    public Long diagnoseDevice(String deviceId) {
        log.info("[设备质量诊断] 开始诊断: deviceId={}", deviceId);

        // 1. 查询设备信息
        DeviceEntity device = deviceDao.selectById(deviceId);
        if (device == null) {
            log.warn("[设备质量诊断] 设备不存在: deviceId={}", deviceId);
            return null;
        }

        // 2. 查询设备健康指标（最近30天）
        List<DeviceHealthMetricEntity> metrics = queryHealthMetrics(deviceId, 30);

        // 3. 计算健康度评分
        Integer healthScore = calculateHealthScore(device, metrics);

        // 4. 判定质量等级
        String qualityLevel = getQualityLevel(healthScore);

        // 5. 执行规则诊断
        List<QualityAlarmEntity> alarms = executeRuleDiagnosis(device, metrics);

        // 6. 确定告警级别
        Integer alarmLevel = determineAlarmLevel(alarms);

        // 7. 创建质量记录
        DeviceQualityRecordEntity record = new DeviceQualityRecordEntity();
        record.setDeviceId(deviceId);
        record.setDeviceName(device.getDeviceName());
        record.setDeviceType(device.getDeviceType());
        record.setHealthScore(healthScore);
        record.setQualityLevel(qualityLevel);
        record.setAlarmLevel(alarmLevel);
        record.setDiagnosisTime(LocalDateTime.now());

        // 保存诊断结果（JSON格式）
        try {
            Map<String, Object> diagnosisResult = new HashMap<>();
            diagnosisResult.put("healthScore", healthScore);
            diagnosisResult.put("qualityLevel", qualityLevel);
            diagnosisResult.put("metricsCount", metrics.size());
            diagnosisResult.put("alarmsCount", alarms.size());
            diagnosisResult.put("diagnosisTime", LocalDateTime.now());
            record.setDiagnosisResult(objectMapper.writeValueAsString(diagnosisResult));
        } catch (Exception e) {
            log.error("[设备质量诊断] JSON序列化失败", e);
        }

        deviceQualityDao.insert(record);

        // 8. 保存告警记录
        for (QualityAlarmEntity alarm : alarms) {
            qualityAlarmDao.insert(alarm);
        }

        log.info("[设备质量诊断] 诊断完成: deviceId={}, score={}, level={}, alarms={}",
                deviceId, healthScore, qualityLevel, alarms.size());

        return record.getRecordId();
    }

    /**
     * 计算设备健康度评分（0-100分）
     *
     * @param device 设备实体
     * @param metrics 健康指标列表
     * @return 健康评分
     */
    public Integer calculateHealthScore(DeviceEntity device, List<DeviceHealthMetricEntity> metrics) {
        log.debug("[设备质量诊断] 计算健康度: deviceId={}", device.getDeviceId());

        double score = 100.0;

        // 1. 在线状态评分（权重30%）
        int statusScore = calculateStatusScore(device.getStatus());
        score = score * 0.7 + statusScore * 0.3;

        // 2. 性能指标评分（权重40%）
        if (!metrics.isEmpty()) {
            double performanceScore = calculatePerformanceScore(metrics);
            score = score * 0.6 + performanceScore * 0.4;
        }

        // 3. 故障历史评分（权重20%）
        double faultScore = calculateFaultScore(device.getDeviceId());
        score = score * 0.8 + faultScore * 0.2;

        // 4. 维护记录评分（权重10%）
        double maintenanceScore = calculateMaintenanceScore(device.getDeviceId());
        score = score * 0.9 + maintenanceScore * 0.1;

        return (int) Math.round(score);
    }

    /**
     * 在线状态评分
     */
    private int calculateStatusScore(Integer status) {
        if (status == null) {
            return 50;  // 未知状态
        }
        return switch (status) {
            case 1 -> 100;  // 在线
            case 2 -> 60;   // 离线
            case 3 -> 20;   // 故障
            case 4 -> 0;    // 停用
            default -> 50;
        };
    }

    /**
     * 性能指标评分
     */
    private double calculatePerformanceScore(List<DeviceHealthMetricEntity> metrics) {
        // 按指标类型分组计算平均分
        Map<String, List<DeviceHealthMetricEntity>> groupedMetrics = new HashMap<>();
        for (DeviceHealthMetricEntity metric : metrics) {
            groupedMetrics.computeIfAbsent(metric.getMetricType(), k -> new ArrayList<>()).add(metric);
        }

        double totalScore = 0.0;
        int count = 0;

        for (Map.Entry<String, List<DeviceHealthMetricEntity>> entry : groupedMetrics.entrySet()) {
            String metricType = entry.getKey();
            List<DeviceHealthMetricEntity> typeMetrics = entry.getValue();

            double metricScore = calculateMetricScore(metricType, typeMetrics);
            totalScore += metricScore;
            count++;
        }

        return count > 0 ? totalScore / count : 80.0;
    }

    /**
     * 单项指标评分
     */
    private double calculateMetricScore(String metricType, List<DeviceHealthMetricEntity> metrics) {
        if (metrics.isEmpty()) {
            return 80.0;
        }

        // 取最新值
        DeviceHealthMetricEntity latestMetric = metrics.get(metrics.size() - 1);
        BigDecimal value = latestMetric.getMetricValue();

        return switch (metricType) {
            case "cpu" -> calculateCpuScore(value);
            case "memory" -> calculateMemoryScore(value);
            case "temperature" -> calculateTemperatureScore(value);
            case "delay" -> calculateDelayScore(value);
            case "packet_loss" -> calculatePacketLossScore(value);
            default -> 80.0;
        };
    }

    /**
     * CPU使用率评分
     */
    private double calculateCpuScore(BigDecimal value) {
        double cpu = value.doubleValue();
        if (cpu < 50) return 100.0;
        if (cpu < 70) return 80.0;
        if (cpu < 85) return 60.0;
        if (cpu < 95) return 40.0;
        return 20.0;
    }

    /**
     * 内存使用率评分
     */
    private double calculateMemoryScore(BigDecimal value) {
        double memory = value.doubleValue();
        if (memory < 60) return 100.0;
        if (memory < 75) return 80.0;
        if (memory < 85) return 60.0;
        if (memory < 95) return 40.0;
        return 20.0;
    }

    /**
     * 温度评分
     */
    private double calculateTemperatureScore(BigDecimal value) {
        double temp = value.doubleValue();
        if (temp < 40) return 100.0;
        if (temp < 55) return 80.0;
        if (temp < 65) return 60.0;
        if (temp < 75) return 40.0;
        return 20.0;
    }

    /**
     * 延迟评分
     */
    private double calculateDelayScore(BigDecimal value) {
        double delay = value.doubleValue();
        if (delay < 100) return 100.0;
        if (delay < 300) return 80.0;
        if (delay < 1000) return 60.0;
        if (delay < 3000) return 40.0;
        return 20.0;
    }

    /**
     * 丢包率评分
     */
    private double calculatePacketLossScore(BigDecimal value) {
        double loss = value.doubleValue();
        if (loss < 1) return 100.0;
        if (loss < 3) return 80.0;
        if (loss < 5) return 60.0;
        if (loss < 10) return 40.0;
        return 20.0;
    }

    /**
     * 故障历史评分
     */
    private double calculateFaultScore(String deviceId) {
        // 查询最近30天的告警记录
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<QualityAlarmEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QualityAlarmEntity::getDeviceId, deviceId)
                .ge(QualityAlarmEntity::getCreateTime, thirtyDaysAgo)
                .in(QualityAlarmEntity::getAlarmLevel, Arrays.asList(3, 4));  // 高和紧急

        Long alarmCount = qualityAlarmDao.selectCount(queryWrapper);

        // 根据告警次数评分
        if (alarmCount == 0) return 100.0;
        if (alarmCount <= 2) return 80.0;
        if (alarmCount <= 5) return 60.0;
        if (alarmCount <= 10) return 40.0;
        return 20.0;
    }

    /**
     * 维护记录评分（简化实现）
     */
    private double calculateMaintenanceScore(String deviceId) {
        // TODO: 查询维护记录，计算维护频率和效果
        return 80.0;
    }

    /**
     * 判定质量等级
     */
    public String getQualityLevel(Integer healthScore) {
        if (healthScore == null) {
            return "未知";
        }
        if (healthScore >= 90) return "优秀";  // A级
        if (healthScore >= 80) return "良好";  // B级
        if (healthScore >= 60) return "合格";  // C级
        if (healthScore >= 40) return "较差";  // D级
        return "危险";                          // E级
    }

    /**
     * 确定告警级别
     */
    private Integer determineAlarmLevel(List<QualityAlarmEntity> alarms) {
        if (alarms.isEmpty()) {
            return 0;  // 无告警
        }

        // 取最高告警级别
        int maxLevel = alarms.stream()
                .mapToInt(QualityAlarmEntity::getAlarmLevel)
                .max()
                .orElse(0);

        return maxLevel;
    }

    /**
     * 查询设备健康指标
     */
    private List<DeviceHealthMetricEntity> queryHealthMetrics(String deviceId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<DeviceHealthMetricEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceHealthMetricEntity::getDeviceId, deviceId)
                .ge(DeviceHealthMetricEntity::getCollectTime, startTime)
                .orderByAsc(DeviceHealthMetricEntity::getCollectTime);

        return deviceHealthMetricDao.selectList(queryWrapper);
    }
}
