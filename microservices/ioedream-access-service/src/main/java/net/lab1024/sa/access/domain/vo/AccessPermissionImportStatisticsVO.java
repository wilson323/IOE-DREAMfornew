package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁权限导入统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "门禁权限导入统计")
public class AccessPermissionImportStatisticsVO {

    /**
     * 总导入批次
     */
    @Schema(description = "总导入批次", example = "100")
    private Integer totalBatches;

    /**
     * 成功批次
     */
    @Schema(description = "成功批次", example = "90")
    private Integer successBatches;

    /**
     * 失败批次
     */
    @Schema(description = "失败批次", example = "8")
    private Integer failedBatches;

    /**
     * 待处理批次
     */
    @Schema(description = "待处理批次", example = "2")
    private Integer pendingBatches;

    /**
     * 总导入记录数
     */
    @Schema(description = "总导入记录数", example = "10000")
    private Integer totalRecords;

    /**
     * 成功导入记录数
     */
    @Schema(description = "成功导入记录数", example = "9500")
    private Integer successRecords;

    /**
     * 失败记录数
     */
    @Schema(description = "失败记录数", example = "500")
    private Integer errorRecords;

    /**
     * 整体成功率
     */
    @Schema(description = "整体成功率（%）", example = "95.00")
    private Double successRate;

    /**
     * 今日导入批次
     */
    @Schema(description = "今日导入批次", example = "10")
    private Integer todayBatches;

    /**
     * 今日导入记录数
     */
    @Schema(description = "今日导入记录数", example = "1000")
    private Integer todayRecords;

    /**
     * 平均处理时间（毫秒）
     */
    @Schema(description = "平均处理时间（毫秒）", example = "3000")
    private Long averageDuration;
}
