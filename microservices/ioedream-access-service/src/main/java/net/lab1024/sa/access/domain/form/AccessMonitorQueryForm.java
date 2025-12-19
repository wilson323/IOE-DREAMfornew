package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/**
 * 门禁实时监控查询表单
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
public class AccessMonitorQueryForm {

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
     * 设备ID（可选）
     */
    @Nullable
    private String deviceId;

    /**
     * 区域ID（可选）
     */
    @Nullable
    private Long areaId;

    /**
     * 监控类型
     * <p>
     * 可能的值：
     * - DEVICE_STATUS - 设备状态监控
     * - ALARM - 报警监控
     * - ACCESS_EVENT - 通行事件监控
     * - PERSON_TRACK - 人员追踪
     * </p>
     */
    @Nullable
    private String monitorType;

    /**
     * 报警级别
     * <p>
     * 可能的值：
     * - LOW - 低级别
     * - MEDIUM - 中级别
     * - HIGH - 高级别
     * - CRITICAL - 紧急
     * </p>
     */
    @Nullable
    private String alarmLevel;

    /**
     * 开始时间（可选）
     */
    @Nullable
    private LocalDateTime startTime;

    /**
     * 结束时间（可选）
     */
    @Nullable
    private LocalDateTime endTime;

    /**
     * 用户ID（可选，用于人员追踪）
     */
    @Nullable
    private Long userId;
}
