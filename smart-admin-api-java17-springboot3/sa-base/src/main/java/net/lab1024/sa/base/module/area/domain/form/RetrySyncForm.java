package net.lab1024.sa.base.module.area.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 重试同步表单
 * 用于重试失败的设备同步操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "重试同步表单")
public class RetrySyncForm {

    /**
     * 记录ID
     * 要重试的下发记录ID
     */
    @NotNull(message = "记录ID不能为空")
    @Schema(description = "记录ID", example = "1")
    private Long recordId;

    /**
     * 重试次数
     * 最大重试次数为5次
     */
    @Min(value = 1, message = "重试次数不能小于1")
    @Max(value = 5, message = "重试次数不能大于5")
    @Schema(description = "重试次数", example = "2")
    private Integer retryCount = 1;

    /**
     * 重试间隔（秒）
     * 重试之间的等待时间
     */
    @Min(value = 1, message = "重试间隔不能小于1秒")
    @Max(value = 300, message = "重试间隔不能大于300秒")
    @Schema(description = "重试间隔（秒）", example = "30")
    private Integer retryInterval = 30;

    /**
     * 是否强制重试
     * true-强制重试即使之前已成功，false-仅重试失败的记录
     */
    @Schema(description = "是否强制重试", example = "false")
    private Boolean forceRetry = false;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "重试设备连接问题")
    private String remark;
}