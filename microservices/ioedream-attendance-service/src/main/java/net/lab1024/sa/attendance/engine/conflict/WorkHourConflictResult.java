package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工作时长冲突检测结果
 * <p>
 * 描述工作时长约束相关冲突的检测结果。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkHourConflictResult {

    /** 是否存在冲突（用于统一判定） */
    private Boolean hasConflict;

    /** 单条冲突（用于快速获取） */
    private WorkHourConflict conflict;

    /** 工作时长冲突列表 */
    private List<WorkHourConflict> conflicts;

    /**
     * 是否存在冲突（兼容实现层调用）
     *
     * @return true-存在冲突
     */
    public boolean hasConflict() {
        return Boolean.TRUE.equals(hasConflict);
    }
}


