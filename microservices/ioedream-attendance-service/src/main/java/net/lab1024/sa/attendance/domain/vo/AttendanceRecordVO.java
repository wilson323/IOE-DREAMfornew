package net.lab1024.sa.attendance.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 考勤记录视图对象
 * <p>
 * 用于返回考勤记录信息
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含展示字段
 * - 使用@Schema注解描述字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "考勤记录视图对象")
public class AttendanceRecordVO {

    @Schema(description = "考勤记录ID", example = "1")
    private Long recordId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "班次ID", example = "1")
    private Long shiftId;

    @Schema(description = "班次名称", example = "正常班")
    private String shiftName;

    @Schema(description = "考勤日期", example = "2025-01-30")
    private LocalDate attendanceDate;

    @Schema(description = "打卡时间", example = "2025-01-30 09:00:00")
    private LocalDateTime punchTime;

    @Schema(description = "考勤状态", example = "NORMAL")
    private String attendanceStatus;

    @Schema(description = "考勤类型", example = "CHECK_IN")
    private String attendanceType;

    @Schema(description = "打卡地址", example = "北京市朝阳区xxx")
    private String punchAddress;

    @Schema(description = "设备名称", example = "考勤机001")
    private String deviceName;
}



