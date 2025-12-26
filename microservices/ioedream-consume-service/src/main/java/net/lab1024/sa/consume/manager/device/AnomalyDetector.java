package net.lab1024.sa.consume.manager.device;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.device.DeviceHealthMetricEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 异常检测器 - 统计学异常检测算法
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Component
public class AnomalyDetector {

    /**
     * 统计学异常检测（3-Sigma原则）
     *
     * @param metrics 历史指标数据
     * @param current 当前指标值
     * @return true-异常 false-正常
     */
    public boolean detectAnomaly(List<DeviceHealthMetricEntity> metrics, DeviceHealthMetricEntity current) {
        if (metrics == null || metrics.size() < 30) {
            log.debug("[异常检测器] 数据不足，无法进行异常检测: count={}",
                    metrics == null ? 0 : metrics.size());
            return false;
        }

        // 计算均值和标准差
        double mean = calculateMean(metrics);
        double stddev = calculateStdDev(metrics, mean);

        // 3-Sigma原则：超出3个标准差视为异常
        double currentValue = current.getMetricValue().doubleValue();
        double zscore = (currentValue - mean) / stddev;

        boolean isAnomaly = Math.abs(zscore) > 3.0;

        if (isAnomaly) {
            log.warn("[异常检测器] 检测到异常: metricType={}, value={}, mean={}, stddev={}, zscore={}",
                    current.getMetricType(), currentValue, mean, stddev, zscore);
        }

        return isAnomaly;
    }

    /**
     * 计算均值
     */
    private double calculateMean(List<DeviceHealthMetricEntity> metrics) {
        return metrics.stream()
                .mapToDouble(m -> m.getMetricValue().doubleValue())
                .average()
                .orElse(0.0);
    }

    /**
     * 计算标准差
     */
    private double calculateStdDev(List<DeviceHealthMetricEntity> metrics, double mean) {
        double variance = metrics.stream()
                .mapToDouble(m -> {
                    double diff = m.getMetricValue().doubleValue() - mean;
                    return diff * diff;
                })
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }
}
