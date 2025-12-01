package net.lab1024.sa.admin.module.consume.domain.result;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 机器学习检测结果
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */




@Schema(description = "机器学习检测结果")
public class MLDetectionResult {

    @Schema(description = "检测ID")
    private Long detectionId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "预测结果")
    private String prediction;

    @Schema(description = "置信度")
    private Double confidence;

    @Schema(description = "特征重要性")
    private Map<String, Double> featureImportance;

    @Schema(description = "检测时间")
    private LocalDateTime detectionTime;

    @Schema(description = "原始特征")
    private Map<String, Object> originalFeatures;

    public static MLDetectionResult create(Long userId, String modelName, String prediction, double confidence) {
        return MLDetectionResult.builder()
                .detectionId(System.currentTimeMillis())
                .userId(userId)
                .modelName(modelName)
                .prediction(prediction)
                .confidence(confidence)
                .detectionTime(LocalDateTime.now())
                .build();
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)
















}
