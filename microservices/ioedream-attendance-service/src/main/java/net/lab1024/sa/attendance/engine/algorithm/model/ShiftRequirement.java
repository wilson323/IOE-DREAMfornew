package net.lab1024.sa.attendance.engine.algorithm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 班次需求模型（算法内部使用）
 * <p>
 * 用于排班算法中的班次需求表示
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequirement {

    private Long shiftId;
    private Integer requiredCount;
    private Integer minCount;
    private Integer maxCount;
    private Integer assignedCount; // 已分配数量
    private Integer priority; // 优先级
}


