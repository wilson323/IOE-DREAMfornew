package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 告警规则查询表单
 * <p>
 * 用于查询告警规则列表
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
@Schema(description = "告警规则查询表单")
public class AlertRuleQueryForm {

    /**
     * 规则名称（模糊查询）
     */
    @Schema(description = "规则名称", example = "离线")
    private String ruleName;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型", example = "DEVICE_OFFLINE")
    private String ruleType;

    /**
     * 告警级别
     */
    @Schema(description = "告警级别", example = "3")
    private Integer alertLevel;

    /**
     * 启用状态
     */
    @Schema(description = "启用状态", example = "1")
    private Integer enabled;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    @Schema(description = "页大小", example = "20")
    private Integer pageSize = 20;
}
