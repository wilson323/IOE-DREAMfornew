package net.lab1024.sa.common.monitor.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;

/**
 * 告警规则查询DTO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Min验证分页参数
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class AlertRuleQueryDTO {

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 监控指标
     */
    private String metricName;

    /**
     * 告警级别
     */
    private String alertLevel;

    /**
     * 规则状态
     */
    private String status;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    @Min(value = 1, message = "页大小必须大于0")
    private Integer pageSize = 20;
}

