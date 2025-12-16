package net.lab1024.sa.visitor.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 访客预约详情VO
 * <p>
 * 用于返回访客预约的详细信息
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含完整的业务字段
 * - 符合企业级VO设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class VisitorAppointmentDetailVO {

    /**
     * 预约ID
     */
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
     */
    private Integer appointmentStatus;

    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;

    /**
     * 签退时间
     */
    private LocalDateTime checkOutTime;

    /**
     * 备注
     */
    private String remark;
}

