package net.lab1024.sa.device.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 设备健康实体类
 *
 * 用于存储和管理设备的健康状态信息，包括：
 * - 健康评分
 * - 健康等级
 * - 各项健康指标
 * - 故障预测信息
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_device_health")
public class DeviceHealthEntity extends BaseEntity {

    /**
     * 健康记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "health_id")
    private Long healthId;

    /**
     * 设备ID
     */
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /**
     * 健康评分(0-100)
     */
    @Column(name = "health_score", precision = 5, scale = 2)
    private BigDecimal healthScore;

    /**
     * 健康等级(1-正常 2-警告 3-严重 4-故障)
     */
    @Column(name = "health_level")
    private Integer healthLevel;

    /**
     * 健康状态描述
     */
    @Column(name = "health_status", length = 100)
    private String healthStatus;

    /**
     * CPU使用率(百分比)
     */
    @Column(name = "cpu_usage", precision = 5, scale = 2)
    private BigDecimal cpuUsage;

    /**
     * 内存使用率(百分比)
     */
    @Column(name = "memory_usage", precision = 5, scale = 2)
    private BigDecimal memoryUsage;

    /**
     * 磁盘使用率(百分比)
     */
    @Column(name = "disk_usage", precision = 5, scale = 2)
    private BigDecimal diskUsage;

    /**
     * 网络使用率(百分比)
     */
    @Column(name = "network_usage", precision = 5, scale = 2)
    private BigDecimal networkUsage;

    /**
     * 设备温度(摄氏度)
     */
    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    /**
     * 心跳延迟(毫秒)
     */
    @Column(name = "heartbeat_latency")
    private Integer heartbeatLatency;

    /**
     * 指令成功率(百分比)
     */
    @Column(name = "command_success_rate", precision = 5, scale = 2)
    private BigDecimal commandSuccessRate;

    /**
     * 告警数量
     */
    @Column(name = "alarm_count")
    private Integer alarmCount;

    /**
     * 连续运行时间(小时)
     */
    @Column(name = "uptime_hours", precision = 10, scale = 2)
    private BigDecimal uptimeHours;

    /**
     * 最后维护时间
     */
    @Column(name = "last_maintenance_time")
    private LocalDateTime lastMaintenanceTime;

    /**
     * 维护延迟天数
     */
    @Column(name = "maintenance_delay_days", precision = 5, scale = 2)
    private BigDecimal maintenanceDelayDays;

    /**
     * 故障预测概率(0-100)
     */
    @Column(name = "failure_prediction_probability", precision = 5, scale = 2)
    private BigDecimal failurePredictionProbability;

    /**
     * 预测故障时间
     */
    @Column(name = "predicted_failure_time")
    private LocalDateTime predictedFailureTime;

    /**
     * 健康检查时间
     */
    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;

    /**
     * 备注信息
     */
    @Column(name = "remark", length = 500)
    private String remark;
}
