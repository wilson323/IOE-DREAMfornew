package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 移动端心跳响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端心跳响应")
public class MobileHeartbeatVO {

    @Schema(description = "设备ID", example = "MOBILE_001")
    private String deviceId;

    @Schema(description = "服务器时间戳", example = "1705880400000")
    private Long serverTimestamp;

    @Schema(description = "心跳状态", example = "success")
    private String status;

    @Schema(description = "心跳消息", example = "心跳接收成功")
    private String message;

    @Schema(description = "下次心跳间隔（秒）", example = "30")
    private Integer nextHeartbeatInterval;

    @Schema(description = "需要更新", example = "false")
    private Boolean needUpdate;

    @Schema(description = "最新版本", example = "1.1.0")
    private String latestVersion;

    @Schema(description = "更新URL", example = "https://example.com/app-update.apk")
    private String updateUrl;

    @Schema(description = "维护模式", example = "false")
    private Boolean maintenanceMode;

    @Schema(description = "维护消息", example = "系统维护中，预计12:00恢复")
    private String maintenanceMessage;

    @Schema(description = "记录时间", example = "2025-01-30T12:00:00")
    private LocalDateTime recordTime;
}
