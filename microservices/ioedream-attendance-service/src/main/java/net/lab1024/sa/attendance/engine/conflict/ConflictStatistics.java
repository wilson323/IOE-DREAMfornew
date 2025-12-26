package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 冲突检测统计
 * <p>
 * 用于输出冲突检测的汇总统计数据。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictStatistics {

    /** 检测次数（实现层使用totalDetections命名） */
    private Integer totalDetections;

    /** 冲突总数 */
    @Builder.Default
    private Integer totalConflicts = 0;

    /** 严重冲突数 */
    @Builder.Default
    private Integer severeConflicts = 0;

    /** 轻微冲突数 */
    @Builder.Default
    private Integer minorConflicts = 0;

    /** 按类型统计 */
    @Builder.Default
    private Map<String, Integer> conflictTypeStatistics = new HashMap<>();

    /** 按严重程度统计 */
    private Map<Integer, Integer> conflictsBySeverity;
}



