package net.lab1024.sa.common.entity.visitor;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客预约实体类
 * <p>
 * 用于记录访客预约信息，支持工作流审批
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("visitor_appointment")
public class VisitorAppointmentEntity extends BaseEntity {

    /**
     * 预约ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long appointmentId;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 身份证号
     */
    private String idCardNumber;

    /**
     * 预约类型
     */
    private String appointmentType;

    /**
     * 被访人ID
     */
    private Long visitUserId;

    /**
     * 被访人姓名
     */
    private String visitUserName;

    /**
     * 预约开始时间
     */
    private LocalDateTime appointmentStartTime;

    /**
     * 预约结束时间
     */
    private LocalDateTime appointmentEndTime;

    /**
     * 访问目的
     */
    private String visitPurpose;

    /**
     * 预约状态
     * <p>
     * PENDING-待审批
     * APPROVED-已通过
     * REJECTED-已驳回
     * CANCELLED-已取消
     * CHECKED_IN-已签到
     * CHECKED_OUT-已签退
     * </p>
     */
    private String status;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 工作流实例ID
     * <p>
     * 关联OA工作流模块的流程实例ID
     * 用于查询审批状态、审批历史等
     * </p>
     */
    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}

