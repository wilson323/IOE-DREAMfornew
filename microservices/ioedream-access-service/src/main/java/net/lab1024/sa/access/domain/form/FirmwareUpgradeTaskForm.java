package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 固件升级任务表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "固件升级任务表单")
public class FirmwareUpgradeTaskForm {

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称长度不能超过200个字符")
    @Schema(description = "任务名称", example = "2025年1月门禁设备固件升级")
    private String taskName;

    @NotNull(message = "固件ID不能为空")
    @Schema(description = "固件ID", example = "1")
    private Long firmwareId;

    @NotNull(message = "升级策略不能为空")
    @Schema(description = "升级策略：1-立即升级 2-定时升级 3-分批升级", example = "1")
    private Integer upgradeStrategy;

    @Schema(description = "定时升级时间（strategy=2时使用）")
    private LocalDateTime scheduleTime;

    @Schema(description = "分批大小（strategy=3时使用）", example = "10")
    private Integer batchSize;

    @Schema(description = "分批间隔（秒）", example = "300")
    private Integer batchInterval;

    @NotNull(message = "目标设备不能为空")
    @Schema(description = "目标设备ID列表", example = "[1, 2, 3]")
    private List<Long> deviceIds;

    @Schema(description = "是否支持回滚：0-否 1-是", example = "1")
    private Integer rollbackSupported;

    @Schema(description = "备注", example = "紧急安全补丁升级")
    private String remark;
}
