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
 * 设备维护计划实体
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_maintenance_plan")
public class DeviceMaintenancePlanEntity extends BaseEntity {

    /**
     * 计划ID
     */
    @TableId(type = IdType.AUTO)
    private Long planId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 计划状态(pending/in-progress/done/cancelled)
     */
    private String planStatus;

    /**
     * 触发原因(low-score/trend-alert/manual/schedule)
     */
    private String triggerReason;

    /**
     * 计划类型(preventive/corrective/emergency)
     */
    private String planType;

    /**
     * 优先级(1:高 2:中 3:低)
     */
    private Integer priorityLevel;

    /**
     * 创建时健康评分
     */
    private BigDecimal scoreOnCreate;

    /**
     * 指派给用户ID
     */
    private Long assignedTo;

    /**
     * 计划开始时间
     */
    private LocalDateTime scheduleStart;

    /**
     * 计划结束时间
     */
    private LocalDateTime scheduleEnd;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStart;

    /**
     * 实际结束时间
     */
    private LocalDateTime actualEnd;

    /**
     * 预计耗时(分钟)
     */
    private Integer estimatedDuration;

    /**
     * 维护描述
     */
    private String description;

    /**
     * 维护结果备注
     */
    private String resultNote;

    /**
     * 维护费用
     */
    private BigDecimal costAmount;

    /**
     * 使用的零件
     */
    private String partsUsed;

    /**
     * 完成率(百分比)
     */
    private BigDecimal completionRate;

    // 注意：BaseEntity已包含所有审计字段，无需重复定义
    // - createUserId (创建人ID)
    // - updateUserId (更新人ID)
    // - createTime (创建时间)
    // - updateTime (更新时间)
    // - deletedFlag (删除标识)
    // - version (版本号)
}