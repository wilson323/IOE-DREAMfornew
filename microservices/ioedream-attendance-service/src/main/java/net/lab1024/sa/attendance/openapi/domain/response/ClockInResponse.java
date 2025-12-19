package net.lab1024.sa.attendance.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤打卡响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤打卡响应")
public class ClockInResponse {

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "打卡结果码", example = "200")
    private String resultCode;

    @Schema(description = "打卡结果消息", example = "打卡成功")
    private String resultMessage;

    @Schema(description = "打卡记录ID", example = "100001")
    private Long recordId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "打卡类型", example = "on", allowableValues = {"on", "off", "break_start", "break_end"})
    private String clockType;

    @Schema(description = "打卡类型名称", example = "上班打卡")
    private String clockTypeName;

    @Schema(description = "打卡时间", example = "2025-12-16T09:00:00")
    private LocalDateTime clockTime;

    @Schema(description = "计划时间", example = "2025-12-16T09:00:00")
    private LocalDateTime scheduledTime;

    @Schema(description = "打卡状态", example = "normal", allowableValues = {"normal", "late", "early", "normal"})
    private String clockStatus;

    @Schema(description = "打卡状态名称", example = "正常")
    private String clockStatusName;

    @Schema(description = "打卡方式", example = "face")
    private String clockMethod;

    @Schema(description = "打卡方式名称", example = "人脸识别")
    private String clockMethodName;

    @Schema(description = "设备ID", example = "ATTEND_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "前台考勤机")
    private String deviceName;

    @Schema(description = "位置信息", example = "公司前台")
    private String location;

    @Schema(description = "经度", example = "116.397128")
    private Double longitude;

    @Schema(description = "纬度", example = "39.916527")
    private Double latitude;

    @Schema(description = "位置验证结果", example = "true")
    private Boolean locationVerified;

    @Schema(description = "位置验证距离（米）", example = "15")
    private Double locationDistance;

    @Schema(description = "体温数据", example = "36.5")
    private Double temperature;

    @Schema(description = "体温状态", example = "normal", allowableValues = {"normal", "fever", "low"})
    private String temperatureStatus;

    @Schema(description = "是否佩戴口罩", example = "true")
    private Boolean wearingMask;

    @Schema(description = "照片URL", example = "https://example.com/clock_photo.jpg")
    private String photoUrl;

    @Schema(description = "人脸匹配度", example = "98.5")
    private Double faceMatchScore;

    @Schema(description = "指纹匹配度", example = "95.2")
    private Double fingerprintMatchScore;

    @Schema(description = "排班ID", example = "1")
    private Long shiftId;

    @Schema(description = "排班名称", example = "标准班")
    private String shiftName;

    @Schema(description = "工作时间段", example = "09:00-18:00")
    private String workTimeSlot;

    @Schema(description = "迟到时间（分钟）", example = "0")
    private Integer lateMinutes;

    @Schema(description = "早退时间（分钟）", example = "0")
    private Integer earlyMinutes;

    @Schema(description = "是否需要补卡", example = "false")
    private Boolean needSupplement;

    @Schema(description = "异常信息", example = "")
    private String abnormalMessage;

    @Schema(description = "连续打卡天数", example = "15")
    private Integer consecutiveDays;

    @Schema(description = "月度打卡次数", example = "22")
    private Integer monthlyClockCount;

    @Schema(description = "今日工作时间（分钟）", example = "0")
    private Integer todayWorkMinutes;

    @Schema(description = "本周工作时间（小时）", example = "32.5")
    private Double weeklyWorkHours;

    @Schema(description = "语音提示", example = "打卡成功，祝您工作愉快")
    private String voicePrompt;

    @Schema(description = "警告信息", example = "")
    private String warningMessage;

    @Schema(description = "扩展信息", example = "{\"key1\":\"value1\"}")
    private String extendedInfo;

    @Schema(description = "相关提醒事项")
    private List<ReminderInfo> reminders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "提醒信息")
    public static class ReminderInfo {

        @Schema(description = "提醒类型", example = "birthday")
        private String reminderType;

        @Schema(description = "提醒标题", example = "生日快乐")
        private String reminderTitle;

        @Schema(description = "提醒内容", example = "今天是您的生日，祝您生日快乐！")
        private String reminderContent;

        @Schema(description = "提醒时间", example = "2025-12-16T00:00:00")
        private LocalDateTime reminderTime;

        @Schema(description = "是否已查看", example = "false")
        private Boolean viewed;
    }
}