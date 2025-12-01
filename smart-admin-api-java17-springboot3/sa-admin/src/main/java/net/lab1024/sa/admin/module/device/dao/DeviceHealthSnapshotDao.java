package net.lab1024.sa.admin.module.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.device.domain.entity.DeviceHealthSnapshotEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备健康快照DAO
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Mapper
public interface DeviceHealthSnapshotDao extends BaseMapper<DeviceHealthSnapshotEntity> {

    /**
     * 获取设备最新的健康快照
     */
    DeviceHealthSnapshotEntity getLatestByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 获取设备指定时间范围内的健康快照
     */
    List<DeviceHealthSnapshotEntity> getByDeviceIdAndTimeRange(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 获取指定时间之后的所有设备健康快照
     */
    List<DeviceHealthSnapshotEntity> getAfterTime(@Param("time") LocalDateTime time);

    /**
     * 获取健康评分低于阈值的设备数量
     */
    Long countDevicesWithLowScore(@Param("threshold") BigDecimal threshold);

    /**
     * 获取设备健康评分统计
     */
    List<HealthScoreStatistics> getHealthScoreStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除指定时间之前的快照数据（数据清理）
     */
    int deleteBeforeTime(@Param("time") LocalDateTime time);

    /**
     * 获取设备健康等级分布统计
     */
    List<HealthLevelDistribution> getHealthLevelDistribution(@Param("time") LocalDateTime time);

    /**
     * 健康评分统计结果
     */
    class HealthScoreStatistics {
        private String healthLevel;
        private Long deviceCount;
        private BigDecimal avgScore;
        private BigDecimal minScore;
        private BigDecimal maxScore;

        // getters and setters
        public String getHealthLevel() { return healthLevel; }
        public void setHealthLevel(String healthLevel) { this.healthLevel = healthLevel; }
        public Long getDeviceCount() { return deviceCount; }
        public void setDeviceCount(Long deviceCount) { this.deviceCount = deviceCount; }
        public BigDecimal getAvgScore() { return avgScore; }
        public void setAvgScore(BigDecimal avgScore) { this.avgScore = avgScore; }
        public BigDecimal getMinScore() { return minScore; }
        public void setMinScore(BigDecimal minScore) { this.minScore = minScore; }
        public BigDecimal getMaxScore() { return maxScore; }
        public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }
    }

    /**
     * 健康等级分布统计
     */
    class HealthLevelDistribution {
        private String levelCode;
        private String levelDescription;
        private Long deviceCount;
        private Double percentage;

        // getters and setters
        public String getLevelCode() { return levelCode; }
        public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
        public String getLevelDescription() { return levelDescription; }
        public void setLevelDescription(String levelDescription) { this.levelDescription = levelDescription; }
        public Long getDeviceCount() { return deviceCount; }
        public void setDeviceCount(Long deviceCount) { this.deviceCount = deviceCount; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }
}