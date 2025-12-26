package net.lab1024.sa.consume.domain.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 区域定值配置 - 对应POSID_AREA.fixed_value_config字段
 *
 * 存储各餐别的定值金额配置
 * 支持全局默认值和餐别级别覆盖
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Data
public class FixedValueConfig {

    /**
     * 全局默认定值金额（当没有特定餐别配置时使用）
     */
    @JsonProperty("defaultAmount")
    private BigDecimal defaultAmount;

    /**
     * 餐别定值金额映射
     * key: 餐别编码（BREAKFAST/LUNCH/DINNER/SNACK）
     * value: 该餐别的定值金额
     *
     * 示例：
     * {
     *   "BREAKFAST": 15.00,
     *   "LUNCH": 25.00,
     *   "DINNER": 20.00,
     *   "SNACK": 10.00
     * }
     */
    @JsonProperty("mealAmounts")
    private Map<String, BigDecimal> mealAmounts;

    /**
     * 管理模式（1-餐别制，2-超市制，3-混合）
     */
    @JsonProperty("manageMode")
    private Integer manageMode;

    /**
     * 是否启用定值模式
     */
    @JsonProperty("enabled")
    private Boolean enabled;
}
