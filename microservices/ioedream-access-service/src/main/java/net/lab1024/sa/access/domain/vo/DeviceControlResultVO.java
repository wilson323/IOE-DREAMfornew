package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 璁惧鎺у埗缁撴灉VO
 * 鐢ㄤ簬杩斿洖璁惧鎺у埗鎿嶄綔鐨勭粨鏋?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "璁惧鎺у埗缁撴灉VO")
public class DeviceControlResultVO {

    @Schema(description = "鎺у埗浠诲姟ID", example = "TASK20251216001")
    private String taskId;

    @Schema(description = "璁惧ID", example = "1001")
    private Long deviceId;

    @Schema(description = "璁惧鍚嶇О", example = "涓诲叆鍙ｉ棬绂?)
    private String deviceName;

    @Schema(description = "鎺у埗鍛戒护", example = "restart")
    private String command;

    @Schema(description = "鎺у埗鐘舵€?, example = "success")
    private String status;

    @Schema(description = "鎺у埗鐘舵€佹弿杩?, example = "鎵ц鎴愬姛")
    private String statusDesc;

    @Schema(description = "鎵ц缁撴灉", example = "璁惧閲嶅惎瀹屾垚")
    private String result;

    @Schema(description = "鎵ц鏃堕棿(姣)", example = "2500")
    private Long executionTime;

    @Schema(description = "寮€濮嬫椂闂?, example = "2025-12-16T14:30:00")
    private LocalDateTime startTime;

    @Schema(description = "缁撴潫鏃堕棿", example = "2025-12-16T14:30:02")
    private LocalDateTime endTime;

    @Schema(description = "鎿嶄綔鍛?, example = "admin")
    private String operator;

    @Schema(description = "鎿嶄綔鍘熷洜", example = "瀹氭湡缁存姢閲嶅惎")
    private String reason;

    @Schema(description = "鏄惁闇€瑕佸悗缁搷浣?, example = "false")
    private Boolean needFollowUp;

    @Schema(description = "鍚庣画鎿嶄綔鎻忚堪", example = "鏃?)
    private String followUpAction;

    @Schema(description = "閿欒淇℃伅", example = "")
    private String errorMessage;

    @Schema(description = "閿欒浠ｇ爜", example = "")
    private String errorCode;

    // 璁惧鐘舵€佸彉鍖?

    @Schema(description = "鎺у埗鍓嶈澶囩姸鎬?, example = "1")
    private Integer beforeStatus;

    @Schema(description = "鎺у埗鍚庤澶囩姸鎬?, example = "1")
    private Integer afterStatus;

    @Schema(description = "鎺у埗鍓嶈澶囩姸鎬佸悕绉?, example = "鍦ㄧ嚎")
    private String beforeStatusName;

    @Schema(description = "鎺у埗鍚庤澶囩姸鎬佸悕绉?, example = "鍦ㄧ嚎")
    private String afterStatusName;

    // 璇︾粏鎵ц淇℃伅

    @Schema(description = "鎵ц姝ラ鏁?, example = "3")
    private Integer totalSteps;

    @Schema(description = "宸叉墽琛屾楠ゆ暟", example = "3")
    private Integer completedSteps;

    @Schema(description = "褰撳墠鎵ц姝ラ", example = "璁惧閲嶅惎楠岃瘉")
    private String currentStep;

    @Schema(description = "鎵ц杩涘害(鐧惧垎姣?", example = "100")
    private Integer progress;

    @Schema(description = "璁惧鍝嶅簲鏃堕棿(姣)", example = "1200")
    private Long deviceResponseTime;

    @Schema(description = "缃戠粶寤惰繜(姣)", example = "50")
    private Long networkLatency;

    // 缁存姢鐩稿叧

    @Schema(description = "缁存姢寮€濮嬫椂闂?, example = "2025-12-16T14:30:00")
    private LocalDateTime maintenanceStartTime;

    @Schema(description = "缁存姢缁撴潫鏃堕棿", example = "2025-12-17T14:30:00")
    private LocalDateTime maintenanceEndTime;

    @Schema(description = "缁存姢鎸佺画鏃堕棿(灏忔椂)", example = "24")
    private Integer maintenanceDuration;

    // 鏍″噯鐩稿叧

    @Schema(description = "鏍″噯绫诲瀷", example = "face")
    private String calibrationType;

    @Schema(description = "鏍″噯绮惧害", example = "high")
    private String calibrationPrecision;

    @Schema(description = "鏍″噯鍓嶅噯纭巼", example = "95.5")
    private Double beforeAccuracy;

    @Schema(description = "鏍″噯鍚庡噯纭巼", example = "98.2")
    private Double afterAccuracy;

    // 鏃ュ織璁板綍

    @Schema(description = "鎿嶄綔鏃ュ織ID", example = "LOG20251216001")
    private String logId;

    @Schema(description = "瀹¤璁板綍ID", example = "AUDIT20251216001")
    private String auditId;

    @Schema(description = "閫氱煡娑堟伅", example = "璁惧閲嶅惎鎿嶄綔宸插畬鎴?)
    private String notificationMessage;

    @Schema(description = "鏄惁鍙戦€侀€氱煡", example = "true")
    private Boolean notificationSent;
}