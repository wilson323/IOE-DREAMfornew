package net.lab1024.sa.attendance.engine.algorithm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 排班解决方案模型（算法内部使用）
 * <p>
 * 用于排班算法中的解决方案表示
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
public class ScheduleSolution {

    private List<ScheduleAssignment> assignments;
    private Double score;
    private Integer conflictCount;
}


