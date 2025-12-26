package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 反潜回规则配置表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用OpenAPI 3.0注解（@Schema）
 * - 使用Jakarta EE验证注解（@NotNull, @NotBlank）
 * - 提供清晰的字段说明
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "反潜回规则配置表单")
public class AntiPassbackRuleConfigForm {

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", required = true, example = "标准反潜回规则")
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 反潜回类型
     * HARD - 硬反潜（必须进出配对）
     * SOFT - 软反潜（可配置时间窗口）
     * AREA - 区域反潜（同一区域内）
     * GLOBAL - 全局反潜（整个系统）
     */
    @Schema(description = "反潜回类型", required = true, example = "HARD")
    @NotBlank(message = "反潜回类型不能为空")
    private String antiPassbackType;

    /**
     * 时间窗口（秒）
     */
    @Schema(description = "时间窗口（秒）", example = "300")
    private Integer timeWindowSeconds;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    /**
     * 例外规则列表
     */
    @Schema(description = "例外规则列表")
    private List<ExceptionRule> exceptionRules;

    /**
     * 优先级
     */
    @Schema(description = "优先级（数字越小优先级越高）", example = "1")
    private Integer priority;

    /**
     * 规则描述
     */
    @Schema(description = "规则描述", example = "标准区域反潜回规则")
    private String description;

    /**
     * 例外规则
     */
    @Data
    @Schema(description = "例外规则")
    public static class ExceptionRule {

        /**
         * 例外类型
         * EMERGENCY - 紧急情况
         * ADMIN - 管理员特权
         * MULTI_PASS - 多次通行
         */
        @Schema(description = "例外类型", example = "EMERGENCY")
        private String exceptionType;

        /**
         * 适用用户ID列表
         */
        @Schema(description = "适用用户ID列表", example = "[1, 2, 3]")
        private List<Long> userIds;

        /**
         * 适用时间段
         */
        @Schema(description = "适用时间段（cron表达式）", example = "0 0-23 * * *")
        private String timeRange;

        /**
         * 是否启用
         */
        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;
    }
}
