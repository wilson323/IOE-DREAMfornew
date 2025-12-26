package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mock配置视图对象
 * <p>
 * 用于展示Mock配置信息
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
@Schema(description = "Mock配置VO")
public class MockConfigVO {

    /**
     * 配置ID
     */
    @Schema(description = "配置ID")
    private Long configId;

    /**
     * 配置名称
     */
    @Schema(description = "配置名称", example = "员工数据Mock")
    private String configName;

    /**
     * 配置类型
     */
    @Schema(description = "配置类型", example = "EMPLOYEE")
    private String configType;

    /**
     * Mock场景
     */
    @Schema(description = "Mock场景", example = "NORMAL")
    private String mockScenario;

    /**
     * Mock状态
     */
    @Schema(description = "Mock状态", example = "ENABLED")
    private String mockStatus;

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
     * 使用次数
     */
    @Schema(description = "使用次数", example = "100")
    private Long usageCount;

    /**
     * 最后使用时间
     */
    @Schema(description = "最后使用时间", example = "2025-12-26 10:30:00")
    private LocalDateTime lastUsedTime;

    /**
     * 配置描述
     */
    @Schema(description = "配置描述", example = "用于生成员工测试数据")
    private String configDescription;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-12-26 10:00:00")
    private LocalDateTime createTime;
}
