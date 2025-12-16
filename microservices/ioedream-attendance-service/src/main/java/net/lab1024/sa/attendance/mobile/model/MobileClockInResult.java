package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 移动端上班打卡结果
 * <p>
 * 封装移动端上班打卡的响应结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "移动端上班打卡结果")
public class MobileClockInResult {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private boolean success;

    /**
     * 打卡时间
     */
    @Schema(description = "打卡时间", example = "2023-12-20T09:00:00")
    private LocalDateTime clockInTime;

    /**
     * 打卡记录ID
     */
    @Schema(description = "打卡记录ID", example = "1001")
    private Long recordId;

    /**
     * 是否迟到
     */
    @Schema(description = "是否迟到", example = "false")
    private boolean isLate;

    /**
     * 迟到分钟数
     */
    @Schema(description = "迟到分钟数", example = "0")
    private int lateMinutes;

    /**
     * 标准上班时间
     */
    @Schema(description = "标准上班时间", example = "2023-12-20T09:00:00")
    private LocalDateTime standardClockInTime;

    /**
     * 消息
     */
    @Schema(description = "消息", example = "上班打卡成功")
    private String message;

    /**
     * 错误码
     */
    @Schema(description = "错误码", example = "CLOCK_IN_FAILED")
    private String errorCode;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳", example = "1703020800000")
    private long timestamp;

    /**
     * 考勤状态
     */
    @Schema(description = "考勤状态", example = "NORMAL", allowableValues = {"NORMAL", "LATE", "EARLY", "ABSENT"})
    private String attendanceStatus;
}