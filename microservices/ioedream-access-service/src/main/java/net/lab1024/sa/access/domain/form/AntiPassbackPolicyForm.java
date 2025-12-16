package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁反潜回策略配置表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀标识表单对象
 * - 使用验证注解确保数据完整性
 * - 包含Swagger注解便于API文档生成
 * - 支持四种反潜回策略配置
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
@Schema(description = "门禁反潜回策略配置表单")
public class AntiPassbackPolicyForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 反潜回类型
     */
    @NotBlank(message = "反潜回类型不能为空")
    @Pattern(regexp = "^(NONE|HARD|SOFT|AREA|GLOBAL)$", message = "反潜回类型必须是NONE、HARD、SOFT、AREA、GLOBAL之一")
    @Schema(description = "反潜回类型", example = "HARD", allowableValues = {"NONE", "HARD", "SOFT", "AREA", "GLOBAL"})
    private String antiPassbackType;

    /**
     * 时间窗口（分钟）
     */
    @NotNull(message = "时间窗口不能为空")
    @Schema(description = "时间窗口（分钟）", example = "5")
    private Integer timeWindowMinutes;

    /**
     * 是否启用区域人数限制
     */
    @Schema(description = "是否启用区域人数限制", example = "true")
    private Boolean areaCapacityLimitEnabled;

    /**
     * 区域最大人数
     */
    @Schema(description = "区域最大人数", example = "50")
    private Integer maxAreaCapacity;

    /**
     * 是否启用双向通行
     */
    @Schema(description = "是否启用双向通行", example = "true")
    private Boolean bidirectionalAccessEnabled;

    /**
     * 是否记录软反潜回异常
     */
    @Schema(description = "是否记录软反潜回异常", example = "true")
    private Boolean recordSoftExceptionsEnabled;

    /**
     * 是否发送实时告警
     */
    @Schema(description = "是否发送实时告警", example = "true")
    private Boolean realTimeAlertEnabled;

    /**
     * 告警阈值（违规次数）
     */
    @Schema(description = "告警阈值（违规次数）", example = "5")
    private Integer alertThreshold;

    /**
     * 策略描述
     */
    @Schema(description = "策略描述", example = "高风险区域硬反潜回策略")
    private String description;

    /**
     * 生效时间
     */
    @Schema(description = "生效时间", example = "2025-01-30T08:00:00")
    private String effectiveTime;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private String expireTime;

    /**
     * 策略优先级
     */
    @Schema(description = "策略优先级", example = "1")
    private Integer priority;

    /**
     * 扩展配置（JSON格式）
     */
    @Schema(description = "扩展配置（JSON格式）", example = "{\"customRules\": {\"specialHours\": true}}")
    private String extendedConfig;
}