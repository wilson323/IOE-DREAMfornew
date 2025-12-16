package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 行为模式表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Schema(description = "行为模式表单")
public class VideoBehaviorPatternForm {

    /**
     * 模式名称
     */
    @Schema(description = "模式名称", example = "门口徘徊模式")
    @NotBlank(message = "模式名称不能为空")
    @Size(max = 100, message = "模式名称长度不能超过100个字符")
    private String patternName;

    /**
     * 模式类型
     */
    @Schema(description = "模式类型", example = "1")
    @NotNull(message = "模式类型不能为空")
    @Min(value = 1, message = "模式类型必须在1-6之间")
    @Max(value = 6, message = "模式类型必须在1-6之间")
    private Integer patternType;

    /**
     * 模式描述
     */
    @Schema(description = "模式描述", example = "人员在门口区域徘徊超过5分钟的行为模式")
    @Size(max = 500, message = "模式描述长度不能超过500个字符")
    private String patternDescription;

    /**
     * 行为类型
     */
    @Schema(description = "行为类型", example = "5")
    @NotNull(message = "行为类型不能为空")
    @Min(value = 1, message = "行为类型必须在1-6之间")
    @Max(value = 6, message = "行为类型必须在1-6之间")
    private Integer behaviorType;

    /**
     * 行为子类型
     */
    @Schema(description = "行为子类型", example = "1")
    @Min(value = 1, message = "行为子类型不能小于1")
    private Integer behaviorSubType;

    /**
     * 模式规则（JSON格式）
     */
    @Schema(description = "模式规则", example = "{\"minDuration\":300,\"maxDuration\":1800,\"region\":\"entrance\"}")
    @NotBlank(message = "模式规则不能为空")
    @Size(max = 2000, message = "模式规则长度不能超过2000个字符")
    private String patternRules;

    /**
     * 触发条件（JSON格式）
     */
    @Schema(description = "触发条件", example = "{\"targetCount\":[\"1\",\"2\"],\"timeRange\":[\"08:00\",\"18:00\"]}")
    @Size(max = 2000, message = "触发条件长度不能超过2000个字符")
    private String triggerConditions;

    /**
     * 阈值设置（JSON格式）
     */
    @Schema(description = "阈值设置", example = "{\"confidence\":80,\"duration\":300,\"targetCount\":1}")
    @Size(max = 2000, message = "阈值设置长度不能超过2000个字符")
    private String thresholdSettings;

    /**
     * 模式状态
     */
    @Schema(description = "模式状态", example = "1")
    @Min(value = 1, message = "模式状态必须在1-4之间")
    @Max(value = 4, message = "模式状态必须在1-4之间")
    private Integer patternStatus;

    /**
     * 模式优先级
     */
    @Schema(description = "模式优先级", example = "2")
    @Min(value = 1, message = "模式优先级必须在1-4之间")
    @Max(value = 4, message = "模式优先级必须在1-4之间")
    private Integer patternPriority;

    /**
     * 算法模型ID
     */
    @Schema(description = "算法模型ID", example = "MODEL_001")
    @Size(max = 64, message = "算法模型ID长度不能超过64个字符")
    private String algorithmModelId;

    /**
     * 训练数据样本数
     */
    @Schema(description = "训练数据样本数", example = "10000")
    @Min(value = 0, message = "训练数据样本数不能小于0")
    private Long trainingSamples;

    /**
     * 训练准确率
     */
    @Schema(description = "训练准确率", example = "95.5")
    @DecimalMin(value = "0.0", message = "训练准确率不能小于0")
    @DecimalMax(value = "100.0", message = "训练准确率不能大于100")
    private BigDecimal trainingAccuracy;

    /**
     * 验证准确率
     */
    @Schema(description = "验证准确率", example = "93.2")
    @DecimalMin(value = "0.0", message = "验证准确率不能小于0")
    @DecimalMax(value = "100.0", message = "验证准确率不能大于100")
    private BigDecimal validationAccuracy;

    /**
     * 误报率
     */
    @Schema(description = "误报率", example = "2.5")
    @DecimalMin(value = "0.0", message = "误报率不能小于0")
    @DecimalMax(value = "100.0", message = "误报率不能大于100")
    private BigDecimal falsePositiveRate;

    /**
     * 漏报率
     */
    @Schema(description = "漏报率", example = "1.8")
    @DecimalMin(value = "0.0", message = "漏报率不能小于0")
    @DecimalMax(value = "100.0", message = "漏报率不能大于100")
    private BigDecimal falseNegativeRate;

    /**
     * 适用设备类型列表
     */
    @Schema(description = "适用设备类型列表", example = "[1,2,3]")
    private List<Integer> applicableDeviceTypes;

    /**
     * 适用区域ID列表
     */
    @Schema(description = "适用区域ID列表", example = "[1001,1002,1003]")
    private List<Long> applicableAreas;

    /**
     * 适用时间段列表
     */
    @Schema(description = "适用时间段列表", example = "[{\"start\":\"08:00\",\"end\":\"18:00\"}]")
    private List<String> applicableTimeRanges;

    /**
     * 有效期开始时间
     */
    @Schema(description = "有效期开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime validStartTime;

    /**
     * 有效期结束时间
     */
    @Schema(description = "有效期结束时间", example = "2026-12-31T23:59:59")
    private LocalDateTime validEndTime;

    /**
     * 训练间隔天数
     */
    @Schema(description = "训练间隔天数", example = "30")
    @Min(value = 1, message = "训练间隔天数不能小于1")
    private Integer trainingIntervalDays;

    /**
     * 维护人ID
     */
    @Schema(description = "维护人ID", example = "1001")
    private Long maintainedBy;

    /**
     * 维护人姓名
     */
    @Schema(description = "维护人姓名", example = "AI专家")
    @Size(max = 100, message = "维护人姓名长度不能超过100个字符")
    private String maintainedByName;

    /**
     * 模式标签
     */
    @Schema(description = "模式标签", example = "安全,监控,智能分析")
    @Size(max = 500, message = "模式标签长度不能超过500个字符")
    private String patternTags;

    /**
     * 性能指标（JSON格式）
     */
    @Schema(description = "性能指标", example = "{\"responseTime\":100,\"memoryUsage\":256,\"cpuUsage\":15}")
    @Size(max = 1000, message = "性能指标长度不能超过1000个字符")
    private String performanceMetrics;

    /**
     * 是否自动训练
     */
    @Schema(description = "是否自动训练", example = "true")
    private Boolean autoTraining;

    /**
     * 是否实时监控
     */
    @Schema(description = "是否实时监控", example = "true")
    private Boolean realTimeMonitoring;

    /**
     * 是否启用告警
     */
    @Schema(description = "是否启用告警", example = "true")
    private Boolean enableAlarm;

    /**
     * 告警阈值
     */
    @Schema(description = "告警阈值", example = "80.0")
    @DecimalMin(value = "0.0", message = "告警阈值不能小于0")
    @DecimalMax(value = "100.0", message = "告警阈值不能大于100")
    private BigDecimal alarmThreshold;

    /**
     * 是否生成报告
     */
    @Schema(description = "是否生成报告", example = "false")
    private Boolean generateReports;

    /**
     * 报告生成周期（天）
     */
    @Schema(description = "报告生成周期（天）", example = "7")
    @Min(value = 1, message = "报告生成周期不能小于1天")
    private Integer reportGenerationCycle;

    /**
     * 模式复杂度
     */
    @Schema(description = "模式复杂度", example = "中等")
    @Pattern(regexp = "^(简单|中等|复杂|极复杂)$", message = "模式复杂度必须是：简单、中等、复杂、极复杂")
    private String complexityLevel;

    /**
     * 测试用例
     */
    @Schema(description = "测试用例", example = "测试人员在门口徘徊场景")
    @Size(max = 500, message = "测试用例长度不能超过500个字符")
    private String testCases;

    /**
     * 版本说明
     */
    @Schema(description = "版本说明", example = "v1.0.0 - 初始版本")
    @Size(max = 500, message = "版本说明长度不能超过500个字符")
    private String versionDescription;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "针对出入口安全监控的专用模式")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remark;
}