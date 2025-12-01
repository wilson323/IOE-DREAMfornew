package net.lab1024.sa.admin.module.device.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备健康快照实体
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_health_snapshot")
public class DeviceHealthSnapshotEntity extends BaseEntity {

    /**
     * 快照ID
     */
    @TableId(type = IdType.AUTO)
    private Long snapshotId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 健康评分(0-100)
     */
    private BigDecimal scoreValue;

    /**
     * 健康等级(healthy/warning/critical)
     */
    private String levelCode;

    /**
     * 心跳延迟(毫秒)
     */
    private Integer heartbeatLatencyMs;

    /**
     * CPU使用率(百分比)
     */
    private BigDecimal cpuUsagePct;

    /**
     * 设备温度(摄氏度)
     */
    private BigDecimal temperatureCelsius;

    /**
     * 指令成功率(百分比)
     */
    private BigDecimal commandSuccessRatio;

    /**
     * 告警数量
     */
    private Integer alarmCount;

    /**
     * 连续运行时间(小时)
     */
    private BigDecimal uptimeHours;

    /**
     * 维护延迟天数
     */
    private BigDecimal maintenanceDelayDays;

    /**
     * 快照时间
     */
    private LocalDateTime snapshotTime;
}