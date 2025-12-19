package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缺勤影响因子（占位）
 *
 * <p>用于描述影响缺勤的因素（如健康、天气、通勤等）。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenteeismFactor {
    private String factorName;
    private Double weight;
}

