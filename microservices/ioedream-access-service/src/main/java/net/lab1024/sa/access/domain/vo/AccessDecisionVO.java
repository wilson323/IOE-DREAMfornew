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
 * 访问决策视图对象
 * <p>
 * 智能访问控制决策结果的数据传输对象
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
@Schema(description = "访问决策信息")
public class AccessDecisionVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "3001")
    private Long deviceId;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "2001")
    private Long areaId;

    /**
     * 风险等级
     */
    @Schema(description = "风险等级", example = "MEDIUM")
    private String riskLevel;

    /**
     * 风险评分
     */
    @Schema(description = "风险评分", example = "35.5")
    private BigDecimal riskScore;

    /**
     * 访问级别
     * DENIED - 拒绝
     * RESTRICTED - 受限
     * NORMAL - 正常
     * ENHANCED - 增强
     */
    @Schema(description = "访问级别", example = "NORMAL")
    private String accessLevel;

    /**
     * 是否需要二次验证
     */
    @Schema(description = "是否需要二次验证", example = "false")
    private Boolean requireSecondaryVerification;

    /**
     * 额外安全措施
     */
    @Schema(description = "额外安全措施")
    private List<String> additionalSecurityMeasures;

    /**
     * 决策时间
     */
    @Schema(description = "决策时间", example = "2025-01-30T15:45:00")
    private LocalDateTime decisionTime;
}