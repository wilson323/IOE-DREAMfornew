package net.lab1024.sa.admin.module.attendance.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 补卡申请DTO
 *
 * <p>
 * 补卡申请数据传输对象，用于员工申请补卡
 * 包含员工ID、补卡类型、补卡日期、原因等字段
 * 使用Jakarta EE验证注解确保数据有效性
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2024-07-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "补卡申请对象")
public class MakeupPunchRequest {

    @NotNull(message = "员工ID不能为空")
    @Schema(description = "员工ID", required = true)
    private Long employeeId;

    @NotBlank(message = "补卡类型不能为空")
    @Pattern(regexp = "^(IN|OUT)$", message = "补卡类型必须是IN或OUT")
    @Schema(description = "补卡类型: IN-上班补卡, OUT-下班补卡", required = true)
    private String makeupType;

    @NotNull(message = "补卡日期不能为空")
    @Schema(description = "补卡日期", required = true)
    private LocalDate makeupDate;

    @NotNull(message = "补卡时间不能为空")
    @Schema(description = "补卡时间", required = true)
    private String makeupTime;

    @NotBlank(message = "补卡原因不能为空")
    @Schema(description = "补卡原因", required = true)
    private String makeupReason;

    @Schema(description = "附件URL")
    private String attachmentUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "申请时间")
    private LocalDate applyDate;

    @Schema(description = "是否紧急")
    private Boolean isUrgent;

    @Schema(description = "审核状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝")
    private String auditStatus;

    @Schema(description = "审核人ID")
    private Long auditorId;

    @Schema(description = "审核时间")
    private LocalDate auditDate;

    @Schema(description = "审核意见")
    private String auditComment;

    // ========== 手动添加getter方法解决@Data注解SB问题 ==========

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getMakeupType() {
        return makeupType;
    }

    public LocalDate getMakeupDate() {
        return makeupDate;
    }

    public String getMakeupTime() {
        return makeupTime;
    }

    public String getMakeupReason() {
        return makeupReason;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public String getRemark() {
        return remark;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public LocalDate getApplyDate() {
        return applyDate;
    }

    public Boolean getIsUrgent() {
        return isUrgent;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public Long getAuditorId() {
        return auditorId;
    }

    public LocalDate getAuditDate() {
        return auditDate;
    }

    public String getAuditComment() {
        return auditComment;
    }
}