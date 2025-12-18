package net.lab1024.sa.biometric.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模板同步记录视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@Schema(description = "模板同步记录视图对象")
public class TemplateSyncRecordVO {

    @Schema(description = "设备ID", example = "DEVICE_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "门禁设备001")
    private String deviceName;

    @Schema(description = "同步状态", example = "1", allowableValues = {"1", "2", "3"})
    private Integer syncStatus;

    @Schema(description = "同步状态描述", example = "成功")
    private String syncStatusDesc;

    @Schema(description = "错误信息", example = "设备离线")
    private String errorMessage;

    @Schema(description = "同步时间", example = "2025-01-30T14:30:00")
    private java.time.LocalDateTime syncTime;
}
