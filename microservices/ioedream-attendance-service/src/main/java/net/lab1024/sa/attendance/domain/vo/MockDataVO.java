package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Mock数据视图对象
 * <p>
 * 用于返回生成的Mock数据
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
@Schema(description = "Mock数据VO")
public class MockDataVO {

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
     * Mock数据类型
     */
    @Schema(description = "Mock数据类型", example = "EMPLOYEE")
    private String mockDataType;

    /**
     * Mock场景
     */
    @Schema(description = "Mock场景", example = "NORMAL")
    private String mockScenario;

    /**
     * 生成的数据
     */
    @Schema(description = "生成的Mock数据")
    private Object data;

    /**
     * 数据数量
     */
    @Schema(description = "数据数量", example = "100")
    private Integer dataCount;

    /**
     * 生成耗时（毫秒）
     */
    @Schema(description = "生成耗时（毫秒）", example = "50")
    private Long generationTimeMs;

    /**
     * 额外信息
     */
    @Schema(description = "额外信息")
    private Map<String, Object> metadata;
}
