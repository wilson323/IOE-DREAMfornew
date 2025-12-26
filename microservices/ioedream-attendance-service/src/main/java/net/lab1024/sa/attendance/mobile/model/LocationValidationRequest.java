package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 位置验证请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@Data
@Schema(description = "位置验证请求")
public class LocationValidationRequest {

    @Schema(description = "员工ID")
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @Schema(description = "纬度")
    @NotNull(message = "纬度不能为空")
    private Double latitude;

    @Schema(description = "经度")
    @NotNull(message = "经度不能为空")
    private Double longitude;
}
