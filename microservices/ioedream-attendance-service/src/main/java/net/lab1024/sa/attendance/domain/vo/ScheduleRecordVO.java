package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班记录视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "排班记录视图对象")
public class ScheduleRecordVO {

    @Schema(description = "排班记录ID", example = "1001")
    private Long scheduleId;

    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "排班日期", example = "2025-01-30")
    private LocalDate scheduleDate;

    @Schema(description = "班次ID", example = "101")
    private Long shiftId;

    @Schema(description = "班次名称", example = "正常班")
    private String shiftName;

    @Schema(description = "工作开始时间", example = "09:00:00")
    private String workStartTime;

    @Schema(description = "工作结束时间", example = "18:00:00")
    private String workEndTime;

    @Schema(description = "工作时长", example = "8.0")
    private Double workHours;

    @Schema(description = "排班类型", example = "正常排班")
    private String scheduleType;

    @Schema(description = "是否临时排班", example = "false")
    private Boolean isTemporary;

    @Schema(description = "排班原因", example = "项目需要")
    private String reason;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "正常")
    private String statusDesc;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "创建时间", example = "2025-01-01 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "创建人ID", example = "2001")
    private Long createUserId;

    @Schema(description = "创建人姓名", example = "管理员")
    private String createUserName;
}