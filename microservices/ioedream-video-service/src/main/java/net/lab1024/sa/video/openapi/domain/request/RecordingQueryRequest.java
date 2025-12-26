package net.lab1024.sa.video.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 录像查询请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "录像查询请求")
public class RecordingQueryRequest {

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "CAM001", required = true)
    private String deviceId;

    @Schema(description = "开始时间", example = "2025-12-16T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "录像类型", example = "continuous", allowableValues = {"continuous", "event", "manual"})
    private String recordingType;

    @Schema(description = "事件类型列表", example = "[\"intrusion\", \"loitering\"]")
    private List<String> eventTypes;

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;
}
