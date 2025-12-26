package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反潜回规则配置视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用OpenAPI 3.0注解（@Schema）
 * - 提供完整的配置信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "反潜回规则配置")
public class AntiPassbackRuleConfigVO {

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "1")
    private Long ruleId;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "办公区域A")
    private String areaName;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "标准反潜回规则")
    private String ruleName;

    /**
     * 反潜回类型
     */
    @Schema(description = "反潜回类型", example = "HARD")
    private String antiPassbackType;

    /**
     * 反潜回类型描述
     */
    @Schema(description = "反潜回类型描述", example = "硬反潜")
    private String antiPassbackTypeDesc;

    /**
     * 时间窗口（秒）
     */
    @Schema(description = "时间窗口（秒）", example = "300")
    private Integer timeWindowSeconds;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 例外规则列表
     */
    @Schema(description = "例外规则列表")
    private List<ExceptionRuleVO> exceptionRules;

    /**
     * 优先级
     */
    @Schema(description = "优先级", example = "1")
    private Integer priority;

    /**
     * 规则描述
     */
    @Schema(description = "规则描述", example = "标准区域反潜回规则")
    private String description;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-12-26T10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-12-26T10:30:00")
    private LocalDateTime updateTime;

    /**
     * 例外规则VO
     */
    @Data
    @Schema(description = "例外规则")
    public static class ExceptionRuleVO {
        @Schema(description = "例外规则ID", example = "1")
        private Long exceptionRuleId;

        @Schema(description = "例外类型", example = "EMERGENCY")
        private String exceptionType;

        @Schema(description = "例外类型描述", example = "紧急情况")
        private String exceptionTypeDesc;

        @Schema(description = "适用用户ID列表", example = "[1, 2, 3]")
        private List<Long> userIds;

        @Schema(description = "适用时间段", example = "0 0-23 * * *")
        private String timeRange;

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;
    }
}
