package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 离线通行记录上传表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Data
@Schema(description = "离线通行记录上传表单")
public class OfflineRecordUploadForm {

    @Schema(description = "离线记录列表", required = true)
    @NotEmpty(message = "离线记录列表不能为空")
    private List<OfflineRecordItem> records;

    @Schema(description = "设备ID", example = "device_12345")
    private String deviceId;

    @Schema(description = "设备唯一标识", example = "UUID123456")
    private String deviceUuid;

    /**
     * 离线记录项
     */
    @Data
    @Schema(description = "离线记录项")
    public static class OfflineRecordItem {

        @Schema(description = "记录ID（客户端生成）", example = "offline_rec_123")
        private String recordId;

        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "设备ID", example = "device_123")
        private String deviceId;

        @Schema(description = "通行方式：face-人脸，finger-指纹，card-IC卡，password-密码，qrcode-二维码", example = "face")
        private String passMethod;

        @Schema(description = "通行结果：1-成功，2-失败", example = "1")
        private Integer passResult;

        @Schema(description = "失败原因", example = "权限已过期")
        private String failReason;

        @Schema(description = "通行时间戳（毫秒）", example = "1703424000000")
        private Long passTime;

        @Schema(description = "区域ID", example = "100")
        private Long areaId;

        @Schema(description = "是否离线模式", example = "true")
        private Boolean offlineMode;
    }
}
