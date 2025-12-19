package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/**
 * 反潜回记录查询表单
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
public class AntiPassbackQueryForm {

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
     * 用户ID
     */
    @Nullable
    private Long userId;

    /**
     * 设备ID
     */
    @Nullable
    private Long deviceId;

    /**
     * 区域ID
     */
    @Nullable
    private Long areaId;

    /**
     * 进出状态
     * 1=进
     * 2=出
     */
    @Nullable
    private Integer inOutStatus;

    /**
     * 开始时间
     */
    @Nullable
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Nullable
    private LocalDateTime endTime;
}
