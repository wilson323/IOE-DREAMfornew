package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 视频设备查询表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * - 继承分页参数规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class VideoDeviceQueryForm {

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
     * 关键词（设备名称、设备编号）
     */
    @Nullable
    private String keyword;

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 设备状态
     * <p>
     * 枚举值：
     * - 1 - 在线
     * - 2 - 离线
     * - 3 - 故障
     * </p>
     */
    private Integer status;
}

