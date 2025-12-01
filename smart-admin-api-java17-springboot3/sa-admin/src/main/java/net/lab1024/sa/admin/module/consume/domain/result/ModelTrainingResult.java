package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 模型训练结果
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "模型训练结果")
public class ModelTrainingResult {

    @Schema(description = "训练ID")
    private Long trainingId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "训练状态")
    private String trainingStatus;

    @Schema(description = "准确率")
    private Double accuracy;

    @Schema(description = "训练耗时(秒)")
    private Long trainingDuration;

    @Schema(description = "训练时间")
    private LocalDateTime trainingTime;

    @Schema(description = "性能指标")
    private Map<String, Double> performanceMetrics;

    public static ModelTrainingResult success(String modelName, double accuracy, long duration) {
        return ModelTrainingResult.builder()
                .trainingId(System.currentTimeMillis())
                .modelName(modelName)
                .trainingStatus("COMPLETED")
                .accuracy(accuracy)
                .trainingDuration(duration)
                .trainingTime(LocalDateTime.now())
                .build();
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)














}
