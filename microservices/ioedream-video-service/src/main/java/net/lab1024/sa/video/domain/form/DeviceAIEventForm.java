package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备AI事件接收表单
 * <p>
 * 设备通过此接口上报AI分析结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "设备AI事件接收表单")
public class DeviceAIEventForm {

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @NotBlank(message = "设备编码不能为空")
    @Schema(description = "设备编码", example = "camera_001")
    private String deviceCode;

    @NotBlank(message = "事件类型不能为空")
    @Schema(description = "事件类型", example = "FALL_DETECTION")
    private String eventType;

    @NotNull(message = "置信度不能为空")
    @Schema(description = "置信度", example = "0.95")
    private BigDecimal confidence;

    @Schema(description = "边界框（JSON格式）", example = "{\"x\":100,\"y\":150,\"width\":200,\"height\":300}")
    private String bbox;

    @Schema(description = "抓拍图片（Base64编码）")
    private String snapshot;

    @NotNull(message = "事件时间不能为空")
    @Schema(description = "事件时间", example = "2025-01-30T10:30:00")
    private LocalDateTime eventTime;

    @Schema(description = "扩展属性（JSON格式）", example = "{\"duration\":120}")
    private String extendedAttributes;
}
