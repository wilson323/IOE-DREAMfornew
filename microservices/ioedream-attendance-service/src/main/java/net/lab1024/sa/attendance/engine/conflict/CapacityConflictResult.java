package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 班次容量冲突检测结果
 * <p>
 * 描述班次容量约束相关冲突的检测结果。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacityConflictResult {

    /** 是否存在冲突（用于统一判定） */
    private Boolean hasConflict;

    /** 单条冲突（用于快速获取） */
    private CapacityConflict conflict;

    /** 容量冲突列表 */
    private List<CapacityConflict> conflicts;

    /**
     * 是否存在冲突（兼容实现层调用）
     *
     * @return true-存在冲突
     */
    public boolean hasConflict() {
        return Boolean.TRUE.equals(hasConflict);
    }
}


