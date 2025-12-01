package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一设备更新表单
 * <p>
 * 用于设备更新的数据验证和传输
 * 严格遵循repowiki规范：完整的参数验证和业务规则
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "统一设备更新表单")
public class UnifiedDeviceUpdateForm extends UnifiedDeviceAddForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deviceId;
}
