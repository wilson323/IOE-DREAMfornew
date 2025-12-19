package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客轨迹响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "访客轨迹响应")
public class VisitorTrackingResponse {

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客身份证号")
    private String idCard;

    @Schema(description = "当前状态")
    private String currentStatus;

    @Schema(description = "当前位置")
    private String currentLocation;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdateTime;

    @Schema(description = "轨迹记录")
    private List<TrackingRecord> trackingRecords;

    @Data
    @Schema(description = "轨迹记录")
    public static class TrackingRecord {

        @Schema(description = "记录时间")
        private LocalDateTime recordTime;

        @Schema(description = "位置")
        private String location;

        @Schema(description = "操作类型")
        private String operationType;

        @Schema(description = "设备ID")
        private String deviceId;

        @Schema(description = "备注")
        private String remarks;
    }
}