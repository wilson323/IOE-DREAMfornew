package net.lab1024.sa.common.audit.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 合规报告查询DTO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@Accessors(chain = true)
public class ComplianceReportQueryDTO {

    /**
     * 报告类型
     */
    private String reportType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 报告状态
     */
    private String status;

    /**
     * 合规等级
     */
    private String complianceLevel;
}

