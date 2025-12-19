package net.lab1024.sa.attendance.engine.prediction.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量预测结果模型
 * <p>
 * 封装批量预测的结果数据 严格遵循CLAUDE.md全局架构规范
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
public class BatchPredictionResult {
    private String batchId;
    private Integer totalRequests;
    private Integer successfulPredictions;
    private Integer failedPredictions;
    private Double successRate;

    private List<SchedulePredictionResult> results;
    private Integer totalCount;
    private Integer successCount;
}
