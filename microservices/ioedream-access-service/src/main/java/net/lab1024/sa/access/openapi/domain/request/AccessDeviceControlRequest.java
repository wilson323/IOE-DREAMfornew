package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 门禁设备控制请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "门禁设备控制请求")
public class AccessDeviceControlRequest {

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型", example = "open", required = true,
            allowableValues = {"open", "close", "lock", "unlock", "restart", "config"})
    private String action;

    @Schema(description = "操作参数（JSON格式）", example = "{\"duration\":30}")
    private String parameters;

    @Schema(description = "操作原因", example = "客户来访")
    private String reason;

    @Schema(description = "是否立即执行", example = "true")
    private Boolean immediate = true;

    @Schema(description = "执行时间", example = "2025-12-16T15:30:00")
    private String executeTime;

    @Schema(description = "重复次数", example = "1")
    private Integer repeatCount = 1;

    @Schema(description = "重复间隔（秒）", example = "60")
    private Integer repeatInterval = 60;
}