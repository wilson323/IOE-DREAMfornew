package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 固件升级任务查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "固件升级任务查询表单")
public class FirmwareUpgradeTaskQueryForm {

    @Schema(description = "任务名称（模糊查询）", example = "门禁设备")
    private String taskName;

    @Schema(description = "任务编号", example = "UPG202512250001")
    private String taskNo;

    @Schema(description = "固件ID", example = "1")
    private Long firmwareId;

    @Schema(description = "任务状态：1-待执行 2-执行中 3-已暂停 4-已完成 5-已失败", example = "2")
    private Integer taskStatus;

    @Schema(description = "升级策略：1-立即升级 2-定时升级 3-分批升级", example = "1")
    private Integer upgradeStrategy;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
