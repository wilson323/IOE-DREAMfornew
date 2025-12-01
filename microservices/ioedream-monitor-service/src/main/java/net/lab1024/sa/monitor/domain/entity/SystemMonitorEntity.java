package net.lab1024.sa.monitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统监控实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_system_monitor")
public class SystemMonitorEntity {

    /**
     * 监控ID
     */
    @TableId(type = IdType.AUTO)
    private Long monitorId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务实例ID
     */
    private String instanceId;

    /**
     * 监控类型 (CPU、MEMORY、DISK、NETWORK、HEALTH)
     */
    private String monitorType;

    /**
     * 监控指标名称
     */
    private String metricName;

    /**
     * 监控值
     */
    private Double metricValue;

    /**
     * 监控单位
     */
    private String metricUnit;

    /**
     * 状态 (NORMAL、WARNING、CRITICAL)
     */
    private String status;

    /**
     * 告警阈值
     */
    private Double alertThreshold;

    /**
     * 监控时间
     */
    private LocalDateTime monitorTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 删除标记
     */
    private Integer deletedFlag;
}