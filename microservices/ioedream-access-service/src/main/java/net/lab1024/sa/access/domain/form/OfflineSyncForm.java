package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 离线数据同步表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Data
@Schema(description = "离线数据同步表单")
public class OfflineSyncForm {

    @Schema(description = "上次同步时间戳（毫秒）", example = "1703424000000")
    private Long lastSyncTime;

    @Schema(description = "数据类型：permissions-权限数据，all-全部数据", example = "permissions")
    private String dataType = "permissions";

    @Schema(description = "客户端版本号", example = "1.0.0")
    private String clientVersion;

    @Schema(description = "设备唯一标识", example = "device_12345")
    private String deviceId;
}
