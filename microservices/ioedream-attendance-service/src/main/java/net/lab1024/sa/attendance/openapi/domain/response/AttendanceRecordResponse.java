package net.lab1024.sa.attendance.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤记录响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤记录响应")
public class AttendanceRecordResponse {

    @Schema(description = "记录ID", example = "100001")
    private Long recordId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "打卡类型", example = "on", allowableValues = {"on", "off", "break_start", "break_end"})
    private String clockType;

    @Schema(description = "打卡类型名称", example = "上班打卡")
    private String clockTypeName;

    @Schema(description = "打卡时间", example = "2025-12-16T09:00:00")
    private LocalDateTime clockTime;

    @Schema(description = "计划时间", example = "2025-12-16T09:00:00")
    private LocalDateTime scheduledTime;

    @Schema(description = "打卡状态", example = "normal", allowableValues = {"normal", "late", "early", "absent", "leave"})
    private String clockStatus;

    @Schema(description = "打卡状态名称", example = "正常")
    private String clockStatusName;

    @Schema(description = "打卡方式", example = "face", allowableValues = {"face", "fingerprint", "card", "password", "location", "manual"})
    private String clockMethod;

    @Schema(description = "打卡方式名称", example = "人脸识别")
    private String clockMethodName;

    @Schema(description = "设备ID", example = "ATTEND_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "前台考勤机")
    private String deviceName;

    @Schema(description = "位置信息", example = "公司前台")
    private String location;

    @Schema(description = "体温数据", example = "36.5")
    private Double temperature;

    @Schema(description = "体温状态", example = "normal", allowableValues = {"normal", "fever", "low"})
    private String temperatureStatus;

    @Schema(description = "是否佩戴口罩", example = "true")
    private Boolean wearingMask;

    @Schema(description = "照片URL", example = "https://example.com/clock_photo.jpg")
    private String photoUrl;

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

    @Schema(description = "迟到金额", example = "0.00")
    private java.math.BigDecimal lateAmount;

    @Schema(description = "早退金额", example = "0.00")
    private java.math.BigDecimal earlyAmount;

    @Schema(description = "是否异常记录", example = "false")
    private Boolean isAbnormal;

    @Schema(description = "异常类型", example = "")
    private String abnormalType;

    @Schema(description = "异常原因", example = "")
    private String abnormalReason;

    @Schema(description = "处理状态", example = "")
    private String processStatus;

    @Schema(description = "处理人", example = "")
    private String processor;

    @Schema(description = "处理时间", example = "")
    private LocalDateTime processTime;

    @Schema(description = "是否补卡记录", example = "false")
    private Boolean isSupplement;

    @Schema(description = "补卡申请ID", example = "")
    private Long supplementApplicationId;

    @Schema(description = "补卡原因", example = "")
    private String supplementReason;

    @Schema(description = "备注", example = "正常打卡")
    private String remark;
}