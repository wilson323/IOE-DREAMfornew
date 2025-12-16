package net.lab1024.sa.visitor.domain.form;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端访客预约表单
 * <p>
 * 用于移动端访客预约功能
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 包含完整的参数验证注解
 * - 符合企业级表单设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class VisitorMobileForm {

    /**
     * 访客姓名
     */
    @NotBlank(message = "访客姓名不能为空")
    private String visitorName;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    private String idCardNumber;

    /**
     * 预约类型
     */
    private String appointmentType;

    /**
     * 被访人ID
     */
    @NotNull(message = "被访人ID不能为空")
    private Long visitUserId;

    /**
     * 被访人姓名
     */
    private String visitUserName;

    /**
     * 预约开始时间
     */
    @NotNull(message = "预约开始时间不能为空")
    private LocalDateTime appointmentStartTime;

    /**
     * 预约结束时间
     */
    @NotNull(message = "预约结束时间不能为空")
    private LocalDateTime appointmentEndTime;

    /**
     * 访问目的
     */
    private String visitPurpose;

    /**
     * 备注
     */
    private String remark;
}

