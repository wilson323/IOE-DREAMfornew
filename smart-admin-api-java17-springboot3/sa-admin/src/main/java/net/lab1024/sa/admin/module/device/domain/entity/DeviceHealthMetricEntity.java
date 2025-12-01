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
 * 设备健康指标实体
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_health_metric")
public class DeviceHealthMetricEntity extends BaseEntity {

    /**
     * 指标ID
     */
    @TableId(type = IdType.AUTO)
    private Long metricId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 指标代码
     */
    private String metricCode;

    /**
     * 指标值
     */
    private BigDecimal metricValue;

    /**
     * 指标时间
     */
    private LocalDateTime metricTime;

    /**
     * 数据源类型(heartbeat/snmp/sensor/log)
     */
    private String sourceType;

    /**
     * 单位
     */
    private String unit;

    /**
     * 最小阈值
     */
    private BigDecimal thresholdMin;

    /**
     * 最大阈值
     */
    private BigDecimal thresholdMax;
}