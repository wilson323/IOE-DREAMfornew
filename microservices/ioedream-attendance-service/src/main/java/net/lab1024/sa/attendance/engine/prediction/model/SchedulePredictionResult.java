package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 排班预测结果模型
 * <p>
 * 封装排班预测的结果数据
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
public class SchedulePredictionResult {
    private String predictionId;
    private LocalDateTime predictionTime;
    private Double accuracy;
    private Double confidence;
    private List<Map<String, Object>> predictedRecords;
    private Map<String, Object> metadata;
}
