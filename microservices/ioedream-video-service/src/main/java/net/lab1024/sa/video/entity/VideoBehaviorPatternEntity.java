package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 行为模式分析实体
 * 存储AI学习到的行为模式和规则
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_behavior_pattern")
@Schema(description = "行为模式分析实体")
public class VideoBehaviorPatternEntity extends BaseEntity {

    /**
     * 模式ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "模式ID", example = "1698745325654325123")
    private Long patternId;

    /**
     * 模式名称
     */
    @TableField("pattern_name")
    @NotBlank(message = "模式名称不能为空")
    @Size(max = 100, message = "模式名称长度不能超过100个字符")
    @Schema(description = "模式名称", example = "门口徘徊模式")
    private String patternName;

    /**
     * 模式类型
     * 1-时间模式 2-空间模式 3-行为模式 4-异常模式 5-预测模式 6-聚类模式
     */
    @TableField("pattern_type")
    @NotNull(message = "模式类型不能为空")
    @Min(value = 1, message = "模式类型必须在1-6之间")
    @Max(value = 6, message = "模式类型必须在1-6之间")
    @Schema(description = "模式类型", example = "1")
    private Integer patternType;

    /**
     * 模式描述
     */
    @TableField("pattern_description")
    @Size(max = 500, message = "模式描述长度不能超过500个字符")
    @Schema(description = "模式描述", example = "人员在门口区域徘徊超过5分钟的行为模式")
    private String patternDescription;

    /**
     * 行为类型
     * 1-人员检测 2-车辆检测 3-物体检测 4-人脸检测 5-异常行为 6-正常行为
     */
    @TableField("behavior_type")
    @NotNull(message = "行为类型不能为空")
    @Min(value = 1, message = "行为类型必须在1-6之间")
    @Max(value = 6, message = "行为类型必须在1-6之间")
    @Schema(description = "行为类型", example = "5")
    private Integer behaviorType;

    /**
     * 行为子类型
     */
    @TableField("behavior_sub_type")
    @Min(value = 1, message = "行为子类型不能小于1")
    @Schema(description = "行为子类型", example = "1")
    private Integer behaviorSubType;

    /**
     * 模式规则（JSON格式，存储具体的模式规则）
     */
    @TableField("pattern_rules")
    @NotNull(message = "模式规则不能为空")
    @Size(max = 2000, message = "模式规则长度不能超过2000个字符")
    @Schema(description = "模式规则", example = "{\"minDuration\":300,\"maxDuration\":1800,\"region\":\"entrance\"}")
    private String patternRules;

    /**
     * 触发条件（JSON格式）
     */
    @TableField("trigger_conditions")
    @Size(max = 2000, message = "触发条件长度不能超过2000个字符")
    @Schema(description = "触发条件", example = "{\"targetCount\":[\"1\",\"2\"],\"timeRange\":[\"08:00\",\"18:00\"]}")
    private String triggerConditions;

    /**
     * 阈值设置（JSON格式：置信度、持续时间、目标数量等阈值）
     */
    @TableField("threshold_settings")
    @Size(max = 2000, message = "阈值设置长度不能超过2000个字符")
    @Schema(description = "阈值设置", example = "{\"confidence\":80,\"duration\":300,\"targetCount\":1}")
    private String thresholdSettings;

    /**
     * 模式状态
     * 1-启用 2-禁用 3-测试中 4-审核中
     */
    @TableField("pattern_status")
    @Min(value = 1, message = "模式状态必须在1-4之间")
    @Max(value = 4, message = "模式状态必须在1-4之间")
    @Schema(description = "模式状态", example = "1")
    private Integer patternStatus;

    /**
     * 模式优先级
     * 1-低 2-中 3-高 4-紧急
     */
    @TableField("pattern_priority")
    @Min(value = 1, message = "模式优先级必须在1-4之间")
    @Max(value = 4, message = "模式优先级必须在1-4之间")
    @Schema(description = "模式优先级", example = "2")
    private Integer patternPriority;

    /**
     * 算法模型ID
     */
    @TableField("algorithm_model_id")
    @Schema(description = "算法模型ID", example = "MODEL_001")
    private String algorithmModelId;

    /**
     * 训练数据样本数
     */
    @TableField("training_samples")
    @Min(value = 0, message = "训练数据样本数不能小于0")
    @Schema(description = "训练数据样本数", example = "10000")
    private Long trainingSamples;

    /**
     * 训练准确率
     */
    @TableField("training_accuracy")
    @Min(value = 0, message = "训练准确率不能小于0")
    @Max(value = 100, message = "训练准确率不能大于100")
    @Schema(description = "训练准确率", example = "95.5")
    private BigDecimal trainingAccuracy;

    /**
     * 验证准确率
     */
    @TableField("validation_accuracy")
    @Min(value = 0, message = "验证准确率不能小于0")
    @Max(value = 100, message = "验证准确率不能大于100")
    @Schema(description = "验证准确率", example = "93.2")
    private BigDecimal validationAccuracy;

    /**
     * 误报率
     */
    @TableField("false_positive_rate")
    @Min(value = 0, message = "误报率不能小于0")
    @Max(value = 100, message = "误报率不能大于100")
    @Schema(description = "误报率", example = "2.5")
    private BigDecimal falsePositiveRate;

    /**
     * 漏报率
     */
    @TableField("false_negative_rate")
    @Min(value = 0, message = "漏报率不能小于0")
    @Max(value = 100, message = "漏报率不能大于100")
    @Schema(description = "漏报率", example = "1.8")
    private BigDecimal falseNegativeRate;

    /**
     * 适用设备类型（逗号分隔）
     */
    @TableField("applicable_device_types")
    @Size(max = 200, message = "适用设备类型长度不能超过200个字符")
    @Schema(description = "适用设备类型", example = "1,2,3")
    private String applicableDeviceTypes;

    /**
     * 适用区域（逗号分隔的区域ID）
     */
    @TableField("applicable_areas")
    @Size(max = 500, message = "适用区域长度不能超过500个字符")
    @Schema(description = "适用区域", example = "1001,1002,1003")
    private String applicableAreas;

    /**
     * 适用时间段（JSON格式：[{"start":"08:00","end":"18:00"},{"start":"19:00","end":"22:00"}]）
     */
    @TableField("applicable_time_ranges")
    @Size(max = 500, message = "适用时间段长度不能超过500个字符")
    @Schema(description = "适用时间段", example = "[{\"start\":\"08:00\",\"end\":\"18:00\"}]")
    private String applicableTimeRanges;

    /**
     * 有效期开始时间
     */
    @TableField("valid_start_time")
    @Schema(description = "有效期开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime validStartTime;

    /**
     * 有效期结束时间
     */
    @TableField("valid_end_time")
    @Schema(description = "有效期结束时间", example = "2026-12-31T23:59:59")
    private LocalDateTime validEndTime;

    /**
     * 最后训练时间
     */
    @TableField("last_training_time")
    @Schema(description = "最后训练时间", example = "2025-12-16T10:30:00")
    private LocalDateTime lastTrainingTime;

    /**
     * 训练间隔（天数）
     */
    @TableField("training_interval_days")
    @Min(value = 1, message = "训练间隔天数不能小于1")
    @Schema(description = "训练间隔（天数）", example = "30")
    private Integer trainingIntervalDays;

    /**
     * 下次训练时间
     */
    @TableField("next_training_time")
    @Schema(description = "下次训练时间", example = "2026-01-15T10:30:00")
    private LocalDateTime nextTrainingTime;

    /**
     * 模式版本
     */
    @TableField("pattern_version")
    @Size(max = 32, message = "模式版本长度不能超过32个字符")
    @Schema(description = "模式版本", example = "v1.2.0")
    private String patternVersion;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    @Schema(description = "创建人ID", example = "1001")
    private Long createdBy;

    /**
     * 创建人姓名
     */
    @TableField("created_by_name")
    @Size(max = 100, message = "创建人姓名长度不能超过100个字符")
    @Schema(description = "创建人姓名", example = "AI工程师")
    private String createdByName;

    /**
     * 维护人ID
     */
    @TableField("maintained_by")
    @Schema(description = "维护人ID", example = "1002")
    private Long maintainedBy;

    /**
     * 维护人姓名
     */
    @TableField("maintained_by_name")
    @Size(max = 100, message = "维护人姓名长度不能超过100个字符")
    @Schema(description = "维护人姓名", example = "AI专家")
    private String maintainedByName;

    /**
     * 模式标签（逗号分隔）
     */
    @TableField("pattern_tags")
    @Size(max = 500, message = "模式标签长度不能超过500个字符")
    @Schema(description = "模式标签", example = "安全,监控,智能分析")
    private String patternTags;

    /**
     * 性能指标（JSON格式：响应时间、内存使用、CPU使用等）
     */
    @TableField("performance_metrics")
    @Size(max = 1000, message = "性能指标长度不能超过1000个字符")
    @Schema(description = "性能指标", example = "{\"responseTime\":100,\"memoryUsage\":256,\"cpuUsage\":15}")
    private String performanceMetrics;

    /**
     * 使用统计（JSON格式：使用次数、准确率趋势等）
     */
    @TableField("usage_statistics")
    @Size(max = 1000, message = "使用统计长度不能超过1000个字符")
    @Schema(description = "使用统计", example = "{\"usageCount\":1500,\"accuracyTrend\":\"上升\"}")
    private String usageStatistics;

    /**
     * 备注信息
     */
    @TableField("remark")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    @Schema(description = "备注信息", example = "针对出入口安全监控的专用模式")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Size(max = 2000, message = "扩展属性长度不能超过2000个字符")
    @Schema(description = "扩展属性", example = "{\"customParam1\":\"value1\",\"customParam2\":\"value2\"}")
    private String extendedAttributes;

    /**
     * 删除标志
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标志", example = "0")
    private Integer deletedFlag;
}

