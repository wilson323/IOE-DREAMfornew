package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * 移动端请假申请请求
 * <p>
 * 封装移动端请假申请的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端请假申请请求")
public class MobileLeaveApplicationRequest {

    /**
     * 请假类型
     */
    @NotBlank(message = "请假类型不能为空")
    @Schema(description = "请假类型", example = "ANNUAL", allowableValues = {"ANNUAL", "SICK", "PERSONAL", "OTHER"})
    private String leaveType;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @Schema(description = "开始日期", example = "2025-02-01")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    @Schema(description = "结束日期", example = "2025-02-03")
    private LocalDate endDate;

    /**
     * 请假天数
     */
    @Schema(description = "请假天数", example = "3")
    private Double leaveDays;

    /**
     * 请假原因
     */
    @NotBlank(message = "请假原因不能为空")
    @Schema(description = "请假原因", example = "家中有事")
    private String reason;

    /**
     * 附件URL列表
     */
    @Schema(description = "附件URL列表")
    private java.util.List<String> attachments;
}
