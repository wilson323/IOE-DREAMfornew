package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 访问模式分析视图对象
 * <p>
 * 用户访问模式分析结果的数据传输对象
 * 严格遵循CLAUDE.md规范：
 * - 使用@Data注解
 * - 完整的字段类型注释
 * - 构建器模式支持
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
@Schema(description = "访问模式分析信息")
public class AccessPatternAnalysisVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "2001")
    private Long areaId;

    /**
     * 模式评分(0-100分)
     */
    @Schema(description = "模式评分(0-100分)", example = "85.5")
    private BigDecimal patternScore;

    /**
     * 是否检测到异常
     */
    @Schema(description = "是否检测到异常", example = "false")
    private Boolean anomalyDetected;

    /**
     * 预测访问时间模式
     */
    @Schema(description = "预测访问时间模式", example = "WORKING_HOURS")
    private String regularTimePattern;

    /**
     * 访问频率级别
     * LOW - 低频
     * NORMAL - 正常
     * HIGH - 高频
     * EXCESSIVE - 过频
     */
    @Schema(description = "访问频率级别", example = "NORMAL")
    private String accessFrequencyLevel;

    /**
     * 一致性评分
     */
    @Schema(description = "一致性评分", example = "92.3")
    private BigDecimal consistencyScore;

    /**
     * 分析时间
     */
    @Schema(description = "分析时间", example = "2025-01-30T15:45:00")
    private LocalDateTime analysisTime;
}