package net.lab1024.sa.access.domain.form;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 门禁权限申请表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessPermissionApplyForm {

    /**
     * 申请人ID
     */
    @NotNull(message = "申请人ID不能为空")
    private Long applicantId;

    /**
     * 区域ID
     */
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    /**
     * 申请类型
     * <p>
     * NORMAL-常规访问申请
     * EMERGENCY-紧急访问申请
     * </p>
     */
    @NotBlank(message = "申请类型不能为空")
    private String applyType;

    /**
     * 申请事由
     */
    @NotBlank(message = "申请事由不能为空")
    @Size(max = 500, message = "申请事由长度不能超过500个字符")
    private String applyReason;

    /**
     * 申请开始时间
     */
    private LocalDateTime startTime;

    /**
     * 申请结束时间
     */
    private LocalDateTime endTime;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}