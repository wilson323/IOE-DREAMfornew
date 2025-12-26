package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 反潜回配置VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "反潜回配置")
public class AntiPassbackConfigVO {

    /**
     * 配置ID
     */
    @Schema(description = "配置ID", example = "1")
    private Long configId;

    /**
     * 反潜回模式
     * 1-全局反潜回
     * 2-区域反潜回
     * 3-软反潜回
     * 4-硬反潜回
     */
    @Schema(description = "反潜回模式", example = "1")
    private Integer mode;

    /**
     * 模式名称
     */
    @Schema(description = "模式名称", example = "全局反潜回")
    private String modeName;

    /**
     * 区域ID（区域模式时必填）
     */
    @Schema(description = "区域ID", example = "100")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "A栋")
    private String areaName;

    /**
     * 时间窗口（毫秒）
     */
    @Schema(description = "时间窗口（毫秒）", example = "300000")
    private Long timeWindow;

    /**
     * 时间窗口描述
     */
    @Schema(description = "时间窗口描述", example = "5分钟")
    private String timeWindowDesc;

    /**
     * 最大允许通行次数
     */
    @Schema(description = "最大通行次数", example = "1")
    private Integer maxPassCount;

    /**
     * 启用状态
     * 0-禁用 1-启用
     */
    @Schema(description = "启用状态", example = "1")
    private Integer enabled;

    /**
     * 生效时间
     */
    @Schema(description = "生效时间", example = "2025-01-30T00:00:00")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;

    /**
     * 是否启用告警
     */
    @Schema(description = "告警启用", example = "1")
    private Integer alertEnabled;

    /**
     * 告警方式
     */
    @Schema(description = "告警方式", example = "WEBSOCKET")
    private String alertMethods;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "1706584800000")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "1706584800000")
    private LocalDateTime updatedTime;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID", example = "1")
    private Long updatedBy;
}
