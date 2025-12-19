package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 批量冲突检测结果
 * <p>
 * 用于封装批量检测的结果汇总。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchConflictResult {

    /** 是否存在冲突 */
    private Boolean hasConflicts;

    /** 参与检测的记录数 */
    private Integer totalRecords;

    /** 员工维度的冲突结果 */
    private List<EmployeeConflictResult> conflictResults;

    /** 全局冲突汇总（可选） */
    private net.lab1024.sa.attendance.engine.conflict.ConflictDetectionResult globalConflict;

    /** 总员工数 */
    private Integer totalEmployees;

    /** 存在冲突的员工数 */
    private Integer employeesWithConflicts;

    /** 冲突类型统计 */
    private Map<String, Integer> conflictTypeStatistics;

    /** 汇总统计 */
    private ConflictStatistics statistics;
}


