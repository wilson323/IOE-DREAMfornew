package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备告警处理表单
 * <p>
 * 用于确认或处理设备告警
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备告警处理表单")
public class DeviceAlertHandleForm {

    /**
     * 告警ID
     */
    @NotNull(message = "告警ID不能为空")
    @Schema(description = "告警ID", example = "1001")
    private Long alertId;

    /**
     * 操作类型
     * CONFIRM-确认告警
     * HANDLE-处理告警
     * IGNORE-忽略告警
     */
    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型：CONFIRM-确认 HANDLE-处理 IGNORE-忽略", example = "CONFIRM")
    private String actionType;

    /**
     * 备注（确认备注或处理结果）
     */
    @Schema(description = "备注", example = "已现场确认，设备温度传感器异常")
    private String remark;
}
