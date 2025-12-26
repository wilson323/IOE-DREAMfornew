package net.lab1024.sa.attendance.solver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 班次实体 - OptaPlanner Problem Fact
 *
 * 核心概念:
 * - Problem Fact: 不可变的问题事实
 * - 包含班次的时间、地点、技能需求等信息
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

    /**
     * 班次ID
     */
    private Long id;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 班次开始时间
     */
    private LocalDateTime startTime;

    /**
     * 班次结束时间
     */
    private LocalDateTime endTime;

    /**
     * 班次日期
     */
    private LocalDate shiftDate;

    /**
     * 班次类型 (MORNING-早班, AFTERNOON-午班, NIGHT-夜班)
     */
    private String shiftType;

    /**
     * 所需技能列表 (例如: ["门禁操作", "急救证书"])
     */
    private List<String> requiredSkills;

    /**
     * 需要的员工数 (默认1人)
     */
    @Builder.Default
    private Integer requiredEmployees = 1;

    /**
     * 已分配员工数 (计算属性)
     */
    @Builder.Default
    private Integer assignedEmployees = 0;

    /**
     * 工作地点
     */
    private String location;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 班次角色 (普通员工、组长、主管等)
     */
    private String shiftRole;

    /**
     * 是否跨天班次
     */
    private boolean isCrossDay;

    /**
     * 班次时长（小时）
     */
    public double getDurationHours() {
        if (startTime == null || endTime == null) {
            return 0.0;
        }
        return java.time.Duration.between(startTime, endTime).toHours();
    }

    /**
     * 检查班次是否在指定日期
     */
    public boolean isOnDate(LocalDate date) {
        if (shiftDate != null) {
            return shiftDate.equals(date);
        }
        if (startTime != null) {
            return startTime.toLocalDate().equals(date);
        }
        return false;
    }

    /**
     * 检查时间是否冲突
     */
    public boolean hasTimeConflict(Shift other) {
        if (this.startTime == null || this.endTime == null ||
            other.startTime == null || other.endTime == null) {
            return false;
        }

        // 时间重叠检查
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }

    /**
     * 计算班次难度
     */
    public int getDifficultyLevel() {
        int difficulty = 0;

        // 基于所需技能数量
        if (requiredSkills != null) {
            difficulty += requiredSkills.size() * 2;
        }

        // 基于班次时长
        double hours = getDurationHours();
        if (hours > 8) {
            difficulty += 3; // 长班次
        } else if (hours < 4) {
            difficulty += 1; // 短班次
        }

        // 基于班次类型
        if ("NIGHT".equals(shiftType)) {
            difficulty += 2; // 夜班更难
        }

        return difficulty;
    }
}
