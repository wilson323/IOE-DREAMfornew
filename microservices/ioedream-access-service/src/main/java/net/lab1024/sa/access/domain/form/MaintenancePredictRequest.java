package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 维护预测请求表单
 * <p>
 * 用于设备预测性维护的请求参数
 * 严格遵循CLAUDE.md规范：
 * - 使用@Data注解
 * - 完整的字段验证注解
 * - Swagger文档注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "维护预测请求")
public class MaintenancePredictRequest {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 预测天数
     */
    @Min(value = 1, message = "预测天数不能小于1")
    @Max(value = 365, message = "预测天数不能大于365")
    @Schema(description = "预测天数", example = "30")
    private Integer predictionDays = 30;

    /**
     * 预测模型类型
     * ML - 机器学习模型
     * STATISTICAL - 统计模型
     * HYBRID - 混合模型
     */
    @Schema(description = "预测模型类型", example = "HYBRID", allowableValues = {"ML", "STATISTICAL", "HYBRID"})
    private String modelType = "HYBRID";

    /**
     * 风险阈值
     */
    @Min(value = 0, message = "风险阈值不能小于0")
    @Max(value = 100, message = "风险阈值不能大于100")
    @Schema(description = "风险阈值（%）", example = "75")
    private Integer riskThreshold = 75;

    /**
     * 是否包含成本预估
     */
    @Schema(description = "是否包含成本预估", example = "true")
    private Boolean includeCostEstimation = true;

    /**
     * 是否推荐维护方案
     */
    @Schema(description = "是否推荐维护方案", example = "true")
    private Boolean includeMaintenancePlan = true;
}