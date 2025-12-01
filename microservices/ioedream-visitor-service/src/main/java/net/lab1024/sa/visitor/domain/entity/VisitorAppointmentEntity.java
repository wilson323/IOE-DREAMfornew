package net.lab1024.sa.visitor.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客预约实体类
 * 存储访客预约申请信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_appointment")
public class VisitorAppointmentEntity extends BaseEntity {

    /**
     * 预约ID（主键）
     */
    @TableId(value = "appointment_id", type = IdType.AUTO)
    private Long appointmentId;

    /**
     * 预约编号
     */
    private String appointmentNo;

    /**
     * 访客ID
     */
    private Long visitorId;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 申请人部门
     */
    private String applicantDept;

    /**
     * 申请类型代码
     */
    private String applicationTypeCode;

    /**
     * 申请类型名称
     */
    private String applicationTypeName;

    /**
     * 紧急程度代码
     */
    private String urgencyCode;

    /**
     * 紧急程度名称
     */
    private String urgencyName;

    /**
     * 预约开始时间
     */
    private LocalDateTime appointmentStartTime;

    /**
     * 预约结束时间
     */
    private LocalDateTime appointmentEndTime;

    /**
     * 预计停留时长（小时）
     */
    private Integer estimatedDuration;

    /**
     * 访问目的
     */
    private String visitPurpose;

    /**
     * 访问区域
     */
    private String visitArea;

    /**
     * 被访人员ID
     */
    private Long visitUserId;

    /**
     * 被访人员姓名
     */
    private String visitUserName;

    /**
     * 被访人员部门
     */
    private String visitUserDept;

    /**
     * 被访人员职位
     */
    private String visitUserPosition;

    /**
     * 陪同要求
     */
    private String escortRequirement;

    /**
     * 预约状态代码
     */
    private String statusCode;

    /**
     * 预约状态名称
     */
    private String statusName;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 审批结果
     */
    private String approvalResult;

    /**
     * 审批备注
     */
    private String approvalRemarks;

    /**
     * 拒绝原因
     */
    private String rejectionReason;

    /**
     * 访客须知确认状态
     */
    private Boolean visitorNoticeConfirmed;

    /**
     * 访客须知确认时间
     */
    private LocalDateTime noticeConfirmTime;

    /**
     * 短信通知状态
     */
    private String smsNotificationStatus;

    /**
     * 邮件通知状态
     */
    private String emailNotificationStatus;

    /**
     * 附件文件路径
     */
    private String attachmentPath;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 预约来源
     */
    private String appointmentSource;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 同步状态
     */
    private String syncStatus;
}