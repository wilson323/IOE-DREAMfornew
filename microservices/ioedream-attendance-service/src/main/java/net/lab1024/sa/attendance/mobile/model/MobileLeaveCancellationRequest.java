package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 移动端销假申请请求
 * <p>
 * 封装移动端销假申请的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端销假申请请求")
public class MobileLeaveCancellationRequest {

    /**
     * 请假申请ID
     */
    @NotBlank(message = "请假申请ID不能为空")
    @Schema(description = "请假申请ID", example = "LEAVE_001")
    private String applicationId;

    /**
     * 销假原因
     */
    @NotBlank(message = "销假原因不能为空")
    @Schema(description = "销假原因", example = "提前返回")
    private String reason;
}
