package net.lab1024.sa.common.monitor.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 系统监控实体
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 继承BaseEntity获取公共字段
 * - 使用@TableName指定数据库表名
 * - 支持多种监控类型（CPU、内存、磁盘、网络、健康检查）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_system_monitor")
public class SystemMonitorEntity extends BaseEntity {

    /**
     * 监控ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列monitor_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "monitor_id", type = IdType.AUTO)
    private Long id;

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
}
