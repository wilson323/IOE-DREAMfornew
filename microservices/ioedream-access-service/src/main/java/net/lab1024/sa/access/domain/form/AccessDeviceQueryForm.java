package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 门禁设备查询表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * - 支持分页查询参数
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessDeviceQueryForm {

    /**
     * 页码（从1开始）
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    /**
     * 关键字（设备名称或设备编码）
     */
    @Nullable
    private String keyword;

    /**
     * 区域ID
     */
    @Nullable
    private Long areaId;

    /**
     * 设备状态
     * <p>
     * 可能的值：
     * - 1 - 在线
     * - 2 - 离线
     * - 3 - 故障
     * - 4 - 维护
     * - 5 - 停用
     * </p>
     */
    @Nullable
    private Integer deviceStatus;

    /**
     * 启用标志
     * <p>
     * 0-禁用
     * 1-启用
     * </p>
     */
    @Nullable
    private Integer enabled;
}
