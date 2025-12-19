package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 反潜回配置表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AntiPassbackConfigForm {

    /**
     * 区域ID
     */
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    /**
     * 是否启用反潜回
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    /**
     * 时间窗口（秒）
     * <p>
     * 在时间窗口内不允许重复进入
     * 默认值：300秒（5分钟）
     * </p>
     */
    @Min(value = 1, message = "时间窗口必须大于0")
    private Integer timeWindow = 300;
}
