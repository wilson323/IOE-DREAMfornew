package net.lab1024.sa.attendance.engine.prediction.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预测统计（简化）
 *
 * <p>用于记录某类预测的累计次数、平均准确率与耗时。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionStatistics {

    /**
     * 预测类型
     */
    private String predictionType;

    /**
     * 总预测次数
     */
    private Integer totalPredictions;

    /**
     * 平均准确率
     */
    private Double averageAccuracy;

    /**
     * 平均预测耗时（毫秒）
     */
    private Double averagePredictionTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;
}

