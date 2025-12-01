package net.lab1024.sa.admin.module.consume.domain.entity;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 操作事件实体
 * <p>
 * 用于记录用户操作事件，支持异常检测和行为分析
 * 严格遵循repowiki规范：
 * - 继承BaseEntity获得审计字段
 * - 使用MyBatis-Plus注解进行ORM映射
 * - 包含完整的操作事件字段
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_operation_event")
public class OperationEventEntity extends BaseEntity {

    /**
     * 事件ID
     */
    @TableId(type = IdType.AUTO)
    private Long eventId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 位置信息
     */
    private String location;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 结果状态
     */
    private String resultStatus;

    /**
     * 异常标识
     */
    private Boolean isAbnormal;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 扩展数据
     */
    private String extendData;
}