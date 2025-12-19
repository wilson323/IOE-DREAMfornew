package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缺勤模式分析（占位）
 *
 * <p>当前用于承载缺勤模式分析的结构化输出，后续可扩展字段。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenteeismPatternAnalysis {
    private String summary;
}

