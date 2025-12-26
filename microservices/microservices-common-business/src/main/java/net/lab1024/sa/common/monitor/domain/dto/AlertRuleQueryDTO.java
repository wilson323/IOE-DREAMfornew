package net.lab1024.sa.common.monitor.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 告警规则查询DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@Schema(description = "告警规则查询请求")
public class AlertRuleQueryDTO {

    @Schema(description = "规则名称（模糊查询）", example = "CPU")
    private String ruleName;

    @Schema(description = "监控指标名称", example = "cpu.usage")
    private String metricName;

    @Schema(description = "状态", example = "ENABLED", allowableValues = {"ENABLED", "DISABLED"})
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

