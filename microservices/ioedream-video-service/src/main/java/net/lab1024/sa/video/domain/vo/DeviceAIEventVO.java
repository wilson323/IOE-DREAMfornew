package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备AI事件VO
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备AI事件VO")
public class DeviceAIEventVO {

    @Schema(description = "事件ID")
    private String eventId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "事件类型描述")
    private String eventTypeName;

    @Schema(description = "置信度")
    private BigDecimal confidence;

    @Schema(description = "边界框")
    private String bbox;

    @Schema(description = "抓拍图片URL")
    private String snapshotUrl;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "扩展属性")
    private String extendedAttributes;

    @Schema(description = "事件状态：0-待处理 1-已处理 2-已忽略")
    private Integer eventStatus;

    @Schema(description = "处理时间")
    private LocalDateTime processTime;

    @Schema(description = "告警ID")
    private String alarmId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
