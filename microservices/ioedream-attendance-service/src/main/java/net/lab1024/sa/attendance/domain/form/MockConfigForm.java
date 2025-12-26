package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mock配置表单
 * <p>
 * 用于创建和更新Mock配置
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mock配置表单")
public class MockConfigForm {

    /**
     * 配置ID（更新时需要）
     */
    @Schema(description = "配置ID")
    private Long configId;

    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称", example = "员工数据Mock", required = true)
    private String configName;

    /**
     * 配置类型
     */
    @NotBlank(message = "配置类型不能为空")
    @Schema(description = "配置类型", example = "EMPLOYEE", required = true)
    private String configType;

    /**
     * Mock场景
     */
    @NotBlank(message = "Mock场景不能为空")
    @Schema(description = "Mock场景", example = "NORMAL", required = true)
    private String mockScenario;

    /**
     * Mock状态
     */
    @Schema(description = "Mock状态", example = "ENABLED")
    private String mockStatus;

    /**
     * 数据生成规则（JSON字符串）
     */
    @Schema(description = "数据生成规则（JSON）", example = "{\"dataCount\": 100, \"randomSeed\": 12345}")
    private String generationRules;

    /**
     * Mock数据模板（JSON字符串）
     */
    @Schema(description = "Mock数据模板（JSON）")
    private String dataTemplate;

    /**
     * 返回延迟（毫秒）
     */
    @Schema(description = "返回延迟（毫秒）", example = "100")
    private Long responseDelayMs;

    /**
     * 错误率（百分比）
     */
    @Schema(description = "错误率（百分比）", example = "0.0")
    private Double errorRate;

    /**
     * 超时率（百分比）
     */
    @Schema(description = "超时率（百分比）", example = "0.0")
    private Double timeoutRate;

    /**
     * 是否启用随机延迟
     */
    @Schema(description = "是否启用随机延迟", example = "false")
    private Boolean enableRandomDelay;

    /**
     * 随机延迟最小值（毫秒）
     */
    @Schema(description = "随机延迟最小值（毫秒）", example = "50")
    private Long randomDelayMinMs;

    /**
     * 随机延迟最大值（毫秒）
     */
    @Schema(description = "随机延迟最大值（毫秒）", example = "200")
    private Long randomDelayMaxMs;

    /**
     * 配置描述
     */
    @Schema(description = "配置描述", example = "用于生成员工测试数据")
    private String configDescription;
}
