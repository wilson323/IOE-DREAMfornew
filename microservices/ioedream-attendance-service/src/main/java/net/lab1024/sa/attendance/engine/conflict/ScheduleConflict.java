package net.lab1024.sa.attendance.engine.conflict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 排班冲突信息类
 * <p>
 * 表示排班方案中的冲突：
 * - 冲突类型
 * - 冲突描述
 * - 冲突严重程度
 * - 解决建议
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "排班冲突信息")
public class ScheduleConflict {

    // ==================== 冲突标识 ====================

    @Schema(description = "冲突ID", required = true)
    private Long conflictId;

    @Schema(description = "冲突类型", required = true)
    private ConflictType conflictType;

    // ==================== 冲突详情 ====================

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "排班日期")
    private LocalDate scheduleDate;

    @Schema(description = "冲突班次ID")
    private Long shiftId;

    @Schema(description = "冲突班次名称")
    private String shiftName;

    // ==================== 严重程度 ====================

    @Schema(description = "严重程度: 1-低 2-中 3-高 4-严重", required = true, example = "2")
    private Integer severity;

    @Schema(description = "严重程度描述", example = "中")
    public String getSeverityDescription() {
        if (severity == null) {
            return "未知";
        }
        return switch (severity) {
            case 1 -> "低";
            case 2 -> "中";
            case 3 -> "高";
            case 4 -> "严重";
            default -> "未知";
        };
    }

    // ==================== 冲突描述 ====================

    @Schema(description = "冲突标题", required = true)
    private String title;

    @Schema(description = "冲突描述", required = true)
    private String description;

    @Schema(description = "影响范围")
    private String impact;

    // ==================== 解决建议 ====================

    @Schema(description = "解决建议")
    private String suggestion;

    @Schema(description = "建议操作: 1-调整排班 2-增加人员 3-修改班次 4-忽略")
    private Integer suggestedAction;

    // ==================== 冲突状态 ====================

    @Schema(description = "冲突状态: 0-未解决 1-已解决 2-已忽略", required = true, example = "0")
    private Integer status;

    @Schema(description = "状态描述", example = "未解决")
    public String getStatusDescription() {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "未解决";
            case 1 -> "已解决";
            case 2 -> "已忽略";
            default -> "未知";
        };
    }

    // ==================== 冲突类型枚举 ====================

    public enum ConflictType {
        // 员工相关冲突
        EMPLOYEE_CONSECUTIVE_WORK_VIOLATION("连续工作超标", "员工连续工作天数超过上限"),
        EMPLOYEE_REST_DAYS_INSUFFICIENT("休息天数不足", "员工休息天数不足下限"),
        EMPLOYEE_MONTHLY_WORK_DAYS_EXCEEDED("月工作天数超标", "员工月工作天数超过上限"),
        EMPLOYEE_SKILL_MISMATCH("技能不匹配", "员工技能与班次要求不匹配"),

        // 班次相关冲突
        SHIFT_DUAL_ASSIGNMENT("重复排班", "员工同一天被分配多个班次"),
        SHIFT_OVERLAP("班次时间重叠", "员工班次时间存在重叠"),
        SHIFT_COVERAGE_INSUFFICIENT("班次人数不足", "班次在岗人数低于最低要求"),
        SHIFT_COVERAGE_EXCESS("班次人数过多", "班次在岗人数超过最高要求"),

        // 日期相关冲突
        DATE_WEEKEND_OVERTIME("周末加班", "周末安排工作班次"),
        DATE_HOLIDAY_OVERTIME("节假日加班", "节假日安排工作班次"),
        DATE_UNSTAFFED("日期无人排班", "某些日期没有任何员工排班"),

        // 其他冲突
        OTHER("其他", "其他类型的冲突");

        private final String code;
        private final String description;

        ConflictType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 创建连续工作超标冲突
     */
    public static ScheduleConflict consecutiveWorkViolation(Long employeeId, String employeeName,
                                                           int consecutiveDays, int maxAllowed) {
        return ScheduleConflict.builder()
                .conflictType(ConflictType.EMPLOYEE_CONSECUTIVE_WORK_VIOLATION)
                .employeeId(employeeId)
                .employeeName(employeeName)
                .severity(3)
                .title(String.format("员工%s连续工作%d天超标", employeeName, consecutiveDays))
                .description(String.format("员工%s已连续工作%d天，超过最大允许的%d天",
                        employeeName, consecutiveDays, maxAllowed))
                .impact("员工疲劳度增加，可能影响工作效率")
                .suggestion("建议在第" + (maxAllowed + 1) + "天安排休息")
                .suggestedAction(1)
                .status(0)
                .build();
    }

    /**
     * 创建休息天数不足冲突
     */
    public static ScheduleConflict restDaysInsufficient(Long employeeId, String employeeName,
                                                        int restDays, int minRequired) {
        return ScheduleConflict.builder()
                .conflictType(ConflictType.EMPLOYEE_REST_DAYS_INSUFFICIENT)
                .employeeId(employeeId)
                .employeeName(employeeName)
                .severity(3)
                .title(String.format("员工%s休息天数不足", employeeName))
                .description(String.format("员工%s休息天数仅为%d天，低于最小要求的%d天",
                        employeeName, restDays, minRequired))
                .impact("员工可能过度疲劳")
                .suggestion("建议增加休息天数至少" + (minRequired - restDays) + "天")
                .suggestedAction(1)
                .status(0)
                .build();
    }

    /**
     * 创建班次人数不足冲突
     */
    public static ScheduleConflict shiftCoverageInsufficient(LocalDate date, Long shiftId, String shiftName,
                                                             int actualStaff, int requiredStaff) {
        return ScheduleConflict.builder()
                .conflictType(ConflictType.SHIFT_COVERAGE_INSUFFICIENT)
                .scheduleDate(date)
                .shiftId(shiftId)
                .shiftName(shiftName)
                .severity(4)
                .title(String.format("%s班次人数不足", shiftName))
                .description(String.format("%s班次实际在岗%d人，要求最少%d人，缺少%d人",
                        shiftName, actualStaff, requiredStaff, requiredStaff - actualStaff))
                .impact("班次人手不足，可能影响正常运营")
                .suggestion("建议从其他班次调配人员或增加排班")
                .suggestedAction(2)
                .status(0)
                .build();
    }

    /**
     * 创建重复排班冲突
     */
    public static ScheduleConflict dualAssignment(Long employeeId, String employeeName,
                                                 LocalDate date, String shiftNames) {
        return ScheduleConflict.builder()
                .conflictType(ConflictType.SHIFT_DUAL_ASSIGNMENT)
                .employeeId(employeeId)
                .employeeName(employeeName)
                .scheduleDate(date)
                .severity(4)
                .title(String.format("员工%s重复排班", employeeName))
                .description(String.format("员工%s在%s被分配多个班次: %s",
                        employeeName, date, shiftNames))
                .impact("员工无法同时完成多个班次")
                .suggestion("建议删除重复的班次安排")
                .suggestedAction(1)
                .status(0)
                .build();
    }

    /**
     * 判断是否为严重冲突
     */
    public boolean isSevere() {
        return severity != null && severity >= 3;
    }

    /**
     * 判断是否需要解决
     */
    public boolean needsResolution() {
        return status != null && status == 0;
    }
}
