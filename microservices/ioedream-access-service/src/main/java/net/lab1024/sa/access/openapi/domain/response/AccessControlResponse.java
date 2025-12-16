package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门禁控制响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁控制响应")
public class AccessControlResponse {

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "控制结果码", example = "200")
    private String resultCode;

    @Schema(description = "控制结果消息", example = "操作成功")
    private String resultMessage;

    @Schema(description = "设备ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "主门禁")
    private String deviceName;

    @Schema(description = "操作类型", example = "open")
    private String action;

    @Schema(description = "操作时间", example = "2025-12-16T15:30:00")
    private LocalDateTime operationTime;

    @Schema(description = "执行时间", example = "2025-12-16T15:30:01")
    private LocalDateTime executeTime;

    @Schema(description = "操作状态", example = "completed", allowableValues = {"pending", "executing", "completed", "failed"})
    private String operationStatus;

    @Schema(description = "操作人ID", example = "1001")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "张三")
    private String operatorName;

    @Schema(description = "操作原因", example = "访客通行")
    private String reason;

    @Schema(description = "当前设备状态", example = "open", allowableValues = {"open", "close", "lock", "unlock", "fault"})
    private String deviceStatus;

    @Schema(description = "设备响应消息", example = "门已开启")
    private String deviceResponse;

    @Schema(description = "开门时长（秒）", example = "30")
    private Integer openDuration;

    @Schema(description = "预计关闭时间", example = "2025-12-16T15:30:30")
    private LocalDateTime estimatedCloseTime;

    @Schema(description = "操作记录ID", example = "100001")
    private Long operationRecordId;

    @Schema(description = "是否需要确认", example = "false")
    private Boolean needConfirmation;

    @Schema(description = "确认超时时间（秒）", example = "60")
    private Integer confirmationTimeout;

    @Schema(description = "警告信息", example = "")
    private String warningMessage;

    @Schema(description = "错误代码", example = "")
    private String errorCode;

    @Schema(description = "错误详情", example = "")
    private String errorDetail;

    @Schema(description = "扩展信息", example = "{\"key1\":\"value1\"}")
    private String extendedInfo;
}