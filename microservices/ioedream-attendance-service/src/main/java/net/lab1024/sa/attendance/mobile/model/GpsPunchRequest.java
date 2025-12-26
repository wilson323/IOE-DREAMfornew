package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * GPS定位打卡请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@Data
@Schema(description = "GPS定位打卡请求")
public class GpsPunchRequest {

    @Schema(description = "员工ID")
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @Schema(description = "纬度")
    @NotNull(message = "纬度不能为空")
    private Double latitude;

    @Schema(description = "经度")
    @NotNull(message = "经度不能为空")
    private Double longitude;

    @Schema(description = "地址信息")
    private String address;

    @Schema(description = "照片URL")
    private String photoUrl;

    @Schema(description = "打卡类型")
    private String punchType;

    @Schema(description = "设备信息")
    private String deviceInfo;
}
