package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端二维码表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端二维码表单")
public class MobileQRCodeForm {

    @Schema(description = "区域ID", required = true, example = "1")
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    @Schema(description = "二维码类型", example = "temporary")
    private String qrCodeType;

    @Schema(description = "有效期（分钟）", example = "5")
    private Integer validityMinutes;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "访客ID", example = "2001")
    private Long visitorId;

    // 添加缺失的字段
    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "设备ID")
    private String deviceId;
}
