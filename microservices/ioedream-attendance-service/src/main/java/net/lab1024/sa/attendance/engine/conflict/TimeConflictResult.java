package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 时间冲突检测结果
 * <p>
 * 描述对排班记录进行时间冲突检测后的结果。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeConflictResult {

    /** 是否存在冲突（用于统一判定） */
    private Boolean hasConflict;

    /** 单条冲突（用于快速获取） */
    private TimeConflict conflict;

    /** 时间冲突列表 */
    private List<TimeConflict> conflicts;

    /**
     * 是否存在冲突（兼容实现层调用）
     *
     * @return true-存在冲突
     */
    public boolean hasConflict() {
        return Boolean.TRUE.equals(hasConflict);
    }
}


