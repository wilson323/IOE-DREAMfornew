package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 维护预测视图对象
 * <p>
 * 设备预测性维护结果的数据传输对象
 * 严格遵循CLAUDE.md规范：
 * - 使用@Data注解
 * - 完整的字段文档注解
 * - 构建者模式支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "维护预测信息")
public class MaintenancePredictionVO {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主入口门禁控制器")
    private String deviceName;

    /**
     * 预测维护类型
     * PREVENTIVE - 预防性维护
     * CORRECTIVE - 纠正性维护
     * PREDICTIVE - 预测性维护
     * EMERGENCY - 紧急维护
     */
    @Schema(description = "预测维护类型", example = "PREDICTIVE")
    private String maintenanceType;

    /**
     * 预测故障概率（%）
     */
    @Schema(description = "预测故障概率（%）", example = "23.5")
    private BigDecimal failureProbability;

    /**
     * 风险等级
     * LOW - 低风险
     * MEDIUM - 中等风险
     * HIGH - 高风险
     * CRITICAL - 严重风险
     */
    @Schema(description = "风险等级", example = "MEDIUM")
    private String riskLevel;

    /**
     * 建议维护时间
     */
    @Schema(description = "建议维护时间", example = "2025-02-15T09:00:00")
    private LocalDateTime recommendedMaintenanceTime;

    /**
     * 维护优先级
     */
    @Schema(description = "维护优先级", example = "2")
    private Integer priority;

    /**
     * 预计停机时长（小时）
     */
    @Schema(description = "预计停机时长（小时）", example = "2")
    private Integer estimatedDowntimeHours;

    /**
     * 预计维护成本
     */
    @Schema(description = "预计维护成本", example = "1200.00")
    private BigDecimal estimatedMaintenanceCost;

    /**
     * 预计减少损失
     */
    @Schema(description = "预计减少损失", example = "5800.00")
    private BigDecimal estimatedLossReduction;

    /**
     * 故障描述
     */
    @Schema(description = "故障描述", example = "基于历史数据分析，设备网络模块可能在15天后出现连接不稳定")
    private String failureDescription;

    /**
     * 影响分析
     */
    @Schema(description = "影响分析", example = "可能导致门禁响应延迟，影响员工通行效率")
    private String impactAnalysis;

    /**
     * 维护建议
     */
    @Schema(description = "维护建议", example = "建议检查网络连接器，更新固件版本")
    private String maintenanceRecommendation;

    /**
     * 所需备件
     */
    @Schema(description = "所需备件")
    private List<RequiredPartVO> requiredParts;

    /**
     * 维护步骤
     */
    @Schema(description = "维护步骤")
    private List<MaintenanceStepVO> maintenanceSteps;

    /**
     * 预测置信度（%）
     */
    @Schema(description = "预测置信度（%）", example = "87.5")
    private BigDecimal confidenceLevel;

    /**
     * 预测模型
     */
    @Schema(description = "预测模型", example = "HYBRID_ML_STATISTICAL")
    private String predictionModel;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源", example = "设备运行日志、故障历史、环境数据")
    private String dataSource;

    /**
     * 预测生成时间
     */
    @Schema(description = "预测生成时间", example = "2025-01-30T15:45:00")
    private LocalDateTime predictionTime;

    /**
     * 所需备件内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "所需备件")
    public static class RequiredPartVO {

        @Schema(description = "备件名称", example = "网络模块")
        private String partName;

        @Schema(description = "备件型号", example = "NM-2000A")
        private String partModel;

        @Schema(description = "备件数量", example = "1")
        private Integer quantity;

        @Schema(description = "预计成本", example = "450.00")
        private BigDecimal estimatedCost;

        @Schema(description = "供应商", example = "设备原厂")
        private String supplier;
    }

    /**
     * 维护步骤内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "维护步骤")
    public static class MaintenanceStepVO {

        @Schema(description = "步骤序号", example = "1")
        private Integer stepNumber;

        @Schema(description = "步骤描述", example = "断开设备电源")
        private String stepDescription;

        @Schema(description = "预计耗时（分钟）", example = "5")
        private Integer estimatedMinutes;

        @Schema(description = "技能要求", example = "BASIC")
        private String skillRequirement;

        @Schema(description = "安全注意事项", example = "确保完全断电后再操作")
        private String safetyNote;
    }
}