package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工冲突检测结果
 * <p>
 * 用于汇总某个员工在指定排班数据下的冲突检测结果。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeConflictResult {

    /** 员工ID */
    private Long employeeId;

    /** 是否存在冲突 */
    private Boolean hasConflicts;

    /** 冲突总数 */
    private Integer totalConflicts;

    /** 严重冲突数 */
    @Builder.Default
    private Integer severeConflicts = 0;

    /** 轻微冲突数 */
    @Builder.Default
    private Integer minorConflicts = 0;

    /** 冲突类型统计 */
    @Builder.Default
    private Map<String, Integer> conflictTypeStatistics = new HashMap<>();

    /** 冲突明细（允许混合冲突类型） */
    @Builder.Default
    private List<Object> conflicts = new ArrayList<>();

    /** 时间冲突检测结果 */
    private TimeConflictResult timeConflictResult;

    /** 技能冲突检测结果 */
    private SkillConflictResult skillConflictResult;

    /** 工作时长冲突检测结果 */
    private WorkHourConflictResult workHourConflictResult;

    /**
     * 增加冲突
     *
     * @param conflict 冲突对象
     */
    public void addConflict(Object conflict) {
        if (conflict == null) {
            return;
        }
        if (conflicts == null) {
            conflicts = new ArrayList<>();
        }
        conflicts.add(conflict);
    }
}



