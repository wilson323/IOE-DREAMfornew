package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 置信区间（简化）
 *
 * <p>用于表示预测值的上下界与置信度。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfidenceInterval {

    /**
     * 下界
     */
    private Double lower;

    /**
     * 均值
     */
    private Double mean;

    /**
     * 上界
     */
    private Double upper;

    /**
     * 置信水平（0-1）
     */
    private Double confidenceLevel;
}

