package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 移动端注销表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端注销表单")
public class MobileLogoutForm {

    @Schema(description = "访问令牌", required = true)
    @NotBlank(message = "访问令牌不能为空")
    private String accessToken;

    @Schema(description = "设备ID", required = true, example = "MOBILE_001")
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;
}
