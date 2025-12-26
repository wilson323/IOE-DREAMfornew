package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 反潜回配置表单
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
public class AntiPassbackConfigForm {

    /**
     * 配置ID（更新时必填）
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
    @NotNull(message = "反潜回模式不能为空")
    @Min(value = 1, message = "反潜回模式值不正确")
    @Max(value = 4, message = "反潜回模式值不正确")
    @Schema(description = "反潜回模式", example = "1")
    private Integer mode;

    /**
     * 区域ID（区域模式时必填）
     */
    @Schema(description = "区域ID", example = "100")
    private Long areaId;

    /**
     * 时间窗口（毫秒）
     * 默认5分钟（300000毫秒）
     */
    @NotNull(message = "时间窗口不能为空")
    @Min(value = 60000, message = "时间窗口不能少于1分钟")
    @Max(value = 3600000, message = "时间窗口不能超过1小时")
    @Schema(description = "时间窗口（毫秒）", example = "300000")
    private Long timeWindow;

    /**
     * 最大允许通行次数（时间窗口内）
     */
    @NotNull(message = "最大通行次数不能为空")
    @Min(value = 1, message = "最大通行次数不能少于1")
    @Max(value = 10, message = "最大通行次数不能超过10")
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
    @NotNull(message = "生效时间不能为空")
    @Schema(description = "生效时间", example = "2025-01-30T00:00:00")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间（NULL表示永久有效）
     */
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;

    /**
     * 是否启用告警
     */
    @Schema(description = "告警启用", example = "1")
    private Integer alertEnabled;

    /**
     * 告警方式（逗号分隔）
     * EMAIL, SMS, WEBSOCKET
     */
    @Schema(description = "告警方式", example = "WEBSOCKET")
    private String alertMethods;
}
