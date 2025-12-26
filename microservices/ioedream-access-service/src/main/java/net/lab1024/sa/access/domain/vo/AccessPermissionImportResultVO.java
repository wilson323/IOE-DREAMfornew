package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 门禁权限导入结果VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "门禁权限导入结果")
public class AccessPermissionImportResultVO {

    /**
     * 批次ID
     */
    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    /**
     * 导入状态
     */
    @Schema(description = "导入状态（SUCCESS-成功 PARTIAL-部分成功 FAILED-失败）",
             example = "PARTIAL")
    private String importStatus;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Integer totalCount;

    /**
     * 成功数
     */
    @Schema(description = "成功数", example = "95")
    private Integer successCount;

    /**
     * 失败数
     */
    @Schema(description = "失败数", example = "5")
    private Integer errorCount;

    /**
     * 成功率
     */
    @Schema(description = "成功率（%）", example = "95.00")
    private Double successRate;

    /**
     * 处理耗时（毫秒）
     */
    @Schema(description = "处理耗时（毫秒）", example = "5000")
    private Long duration;

    /**
     * 错误列表（仅返回前100条）
     */
    @Schema(description = "错误列表（仅返回前100条）")
    private List<AccessPermissionImportErrorVO> errors;

    /**
     * 成功消息
     */
    @Schema(description = "成功消息", example = "导入完成！成功95条，失败5条")
    private String successMessage;

    /**
     * 异步任务ID（异步导入时）
     */
    @Schema(description = "异步任务ID", example = "task-123456")
    private String asyncTaskId;
}
