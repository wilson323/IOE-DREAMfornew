package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预测验证结果模型
 * <p>
 * 封装预测验证的结果数据 严格遵循CLAUDE.md全局架构规范
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
public class PredictionValidationResult {
    private String predictionType;
    private Integer sampleSize;
    private Double meanAbsoluteError;
    private Double meanAbsolutePercentageError;
    private Double rootMeanSquareError;
    private Double rSquared;
    private Double overallAccuracy;
    private Boolean validationSuccessful;

    private Double accuracy;
    private Double errorRate;
    private String validationStatus;
}
