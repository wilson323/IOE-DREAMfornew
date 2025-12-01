package net.lab1024.sa.admin.module.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.device.domain.entity.DeviceHealthMetricEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备健康指标DAO
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Mapper
public interface DeviceHealthMetricDao extends BaseMapper<DeviceHealthMetricEntity> {

    /**
     * 获取设备最新的指标值
     */
    DeviceHealthMetricEntity getLatestMetric(@Param("deviceId") Long deviceId, @Param("metricCode") String metricCode);

    /**
     * 获取设备指定时间范围内的指标数据
     */
    List<DeviceHealthMetricEntity> getMetricsByTimeRange(
            @Param("deviceId") Long deviceId,
            @Param("metricCode") String metricCode,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 获取设备所有指标的最新值
     */
    List<DeviceHealthMetricEntity> getLatestMetricsByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 获取超过阈值的指标数据
     */
    List<DeviceHealthMetricEntity> getExceedThresholdMetrics(@Param("time") LocalDateTime time);

    /**
     * 批量插入指标数据
     */
    int batchInsert(@Param("metrics") List<DeviceHealthMetricEntity> metrics);

    /**
     * 删除指定时间之前的指标数据（数据清理）
     */
    int deleteMetricsBeforeTime(@Param("time") LocalDateTime time, @Param("metricCode") String metricCode);

    /**
     * 获取指标统计信息
     */
    MetricStatistics getMetricStatistics(
            @Param("deviceId") Long deviceId,
            @Param("metricCode") String metricCode,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 指标统计信息
     */
    class MetricStatistics {
        private BigDecimal avgValue;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private Long dataPoints;
        private Long exceedThresholdCount;

        // getters and setters
        public BigDecimal getAvgValue() { return avgValue; }
        public void setAvgValue(BigDecimal avgValue) { this.avgValue = avgValue; }
        public BigDecimal getMinValue() { return minValue; }
        public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
        public BigDecimal getMaxValue() { return maxValue; }
        public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
        public Long getDataPoints() { return dataPoints; }
        public void setDataPoints(Long dataPoints) { this.dataPoints = dataPoints; }
        public Long getExceedThresholdCount() { return exceedThresholdCount; }
        public void setExceedThresholdCount(Long exceedThresholdCount) { this.exceedThresholdCount = exceedThresholdCount; }
    }
}