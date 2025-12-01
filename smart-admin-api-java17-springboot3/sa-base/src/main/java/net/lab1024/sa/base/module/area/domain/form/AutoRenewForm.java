package net.lab1024.sa.base.module.area.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 自动续期表单
 * 用于设置人员区域关联的自动续期规则
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "自动续期表单")
public class AutoRenewForm {

    /**
     * 关联ID
     */
    @NotNull(message = "关联ID不能为空")
    @Schema(description = "关联ID", example = "1")
    private Long relationId;

    /**
     * 是否启用自动续期
     */
    @Schema(description = "是否启用自动续期", example = "true")
    private Boolean enableAutoRenew = false;

    /**
     * 续期策略
     * FIXED-固定周期，RELATIVE-相对当前时间，MANUAL-手动指定
     */
    @Schema(description = "续期策略", example = "FIXED", allowableValues = {"FIXED", "RELATIVE", "MANUAL"})
    private String renewStrategy = "FIXED";

    /**
     * 续期周期（天）
     * 当续期策略为FIXED时使用
     */
    @Min(value = 1, message = "续期周期不能小于1天")
    @Max(value = 3650, message = "续期周期不能大于3650天")
    @Schema(description = "续期周期（天）", example = "30")
    private Integer renewPeriodDays = 30;

    /**
     * 延长期限（天）
     * 续期后的有效期
     */
    @Min(value = 1, message = "延长期限不能小于1天")
    @Max(value = 3650, message = "延长期限不能大于3650天")
    @Schema(description = "延长期限（天）", example = "30")
    private Integer extendDays = 30;

    /**
     * 续期开始时间
     * 当续期策略为MANUAL时使用
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "续期开始时间", example = "2025-12-01 00:00:00")
    private LocalDateTime renewStartTime;

    /**
     * 续期结束时间
     * 当续期策略为MANUAL时使用
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "续期结束时间", example = "2025-12-31 23:59:59")
    private LocalDateTime renewEndTime;

    /**
     * 最大续期次数
     * 0表示无限制
     */
    @Min(value = 0, message = "最大续期次数不能小于0")
    @Max(value = 100, message = "最大续期次数不能大于100")
    @Schema(description = "最大续期次数", example = "10")
    private Integer maxRenewCount = 0;

    /**
     * 续期提前时间（天）
     * 在到期前多少天开始续期
     */
    @Min(value = 1, message = "续期提前时间不能小于1天")
    @Max(value = 30, message = "续期提前时间不能大于30天")
    @Schema(description = "续期提前时间（天）", example = "7")
    private Integer renewAdvanceDays = 7;

    /**
     * 续期通知用户ID
     * 续期成功后通知的用户
     */
    @Schema(description = "续期通知用户ID", example = "1001")
    private Long notifyUserId;

    /**
     * 续期失败重试次数
     */
    @Min(value = 0, message = "续期失败重试次数不能小于0")
    @Max(value = 5, message = "续期失败重试次数不能大于5")
    @Schema(description = "续期失败重试次数", example = "3")
    private Integer retryCount = 3;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "设置临时区域自动续期")
    private String remark;

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return enableAutoRenew != null && enableAutoRenew;
    }

    /**
     * 是否需要手动指定时间
     */
    public boolean needsManualTime() {
        return "MANUAL".equals(renewStrategy);
    }

    /**
     * 是否有续期限制
     */
    public boolean hasRenewLimit() {
        return maxRenewCount != null && maxRenewCount > 0;
    }
}