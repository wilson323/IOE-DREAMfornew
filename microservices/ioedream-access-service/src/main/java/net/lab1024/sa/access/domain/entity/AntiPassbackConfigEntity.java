package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门禁反潜回配置实体
 * <p>
 * 支持4种反潜回模式：
 * - 全局反潜回（mode=1）：跨所有区域检测
 * - 区域反潜回（mode=2）：同一区域内检测
 * - 软反潜回（mode=3）：记录告警但不阻止通行
 * - 硬反潜回（mode=4）：检测到违规时阻止通行
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
@TableName("t_access_anti_passback_config")
@Schema(description = "门禁反潜回配置")
public class AntiPassbackConfigEntity {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "配置ID", example = "1")
    private Long configId;

    /**
     * 反潜回模式
     * 1-全局反潜回: 跨所有区域检测
     * 2-区域反潜回: 同一区域内检测
     * 3-软反潜回: 记录告警但不阻止
     * 4-硬反潜回: 检测到违规时阻止
     */
    @TableField("mode")
    @Schema(description = "反潜回模式", example = "1")
    private Integer mode;

    /**
     * 区域ID（区域模式时必填）
     */
    @TableField("area_id")
    @Schema(description = "区域ID", example = "100")
    private Long areaId;

    /**
     * 时间窗口（毫秒）
     * 默认5分钟（300000毫秒）
     */
    @TableField("time_window")
    @Schema(description = "时间窗口（毫秒）", example = "300000")
    private Long timeWindow;

    /**
     * 最大允许通行次数（时间窗口内）
     * 默认1次
     */
    @TableField("max_pass_count")
    @Schema(description = "最大通行次数", example = "1")
    private Integer maxPassCount;

    /**
     * 启用状态
     * 0-禁用 1-启用
     */
    @TableField("enabled")
    @Schema(description = "启用状态", example = "1")
    private Integer enabled;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    @Schema(description = "生效时间", example = "2025-01-30T00:00:00")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间（NULL表示永久有效）
     */
    @TableField("expire_time")
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;

    /**
     * 是否启用告警
     * 0-禁用 1-启用
     */
    @TableField("alert_enabled")
    @Schema(description = "告警启用", example = "1")
    private Integer alertEnabled;

    /**
     * 告警方式（逗号分隔）
     * EMAIL, SMS, WEBSOCKET
     */
    @TableField("alert_methods")
    @Schema(description = "告警方式", example = "WEBSOCKET")
    private String alertMethods;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "1706584800000")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "1706584800000")
    private LocalDateTime updatedTime;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID", example = "1")
    private Long updatedBy;

    /**
     * 删除标记
     * 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Schema(description = "版本号", example = "0")
    private Integer version;
}
