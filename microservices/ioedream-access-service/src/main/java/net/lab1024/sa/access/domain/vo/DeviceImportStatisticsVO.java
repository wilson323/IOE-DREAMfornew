package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备导入统计信息VO
 * <p>
 * 用于返回导入操作的统计数据：
 * - 总导入次数
 * - 总导入记录数
 * - 成功率统计
 * - 最近导入统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "设备导入统计信息VO")
public class DeviceImportStatisticsVO {

    @Schema(description = "总导入次数", example = "50")
    private Integer totalImportCount;

    @Schema(description = "总导入记录数", example = "5000")
    private Integer totalRecordCount;

    @Schema(description = "总成功数量", example = "4750")
    private Integer totalSuccessCount;

    @Schema(description = "总失败数量", example = "150")
    private Integer totalFailedCount;

    @Schema(description = "总跳过数量", example = "100")
    private Integer totalSkippedCount;

    @Schema(description = "整体成功率（%）", example = "95.0")
    private Double overallSuccessRate;

    @Schema(description = "今日导入次数", example = "5")
    private Integer todayImportCount;

    @Schema(description = "今日导入记录数", example = "500")
    private Integer todayRecordCount;

    @Schema(description = "今日成功数量", example = "480")
    private Integer todaySuccessCount;

    @Schema(description = "今日失败数量", example = "15")
    private Integer todayFailedCount;

    @Schema(description = "今日成功率（%）", example = "96.0")
    private Double todaySuccessRate;

    @Schema(description = "最近7天导入次数", example = "20")
    private Integer last7DaysImportCount;

    @Schema(description = "最近7天导入记录数", example = "2000")
    private Integer last7DaysRecordCount;

    @Schema(description = "最近7天成功率（%）", example = "94.5")
    private Double last7DaysSuccessRate;

    @Schema(description = "平均导入耗时（秒）", example = "8.5")
    private Double averageDurationSeconds;

    @Schema(description = "最快导入耗时（秒）", example = "2.3")
    private Double minDurationSeconds;

    @Schema(description = "最慢导入耗时（秒）", example = "45.6")
    private Double maxDurationSeconds;

    @Schema(description = "最近导入批次数量", example = "10")
    private Integer recentImportCount;

    @Schema(description = "成功批次数量", example = "8")
    private Integer successBatchCount;

    @Schema(description = "部分失败批次数量", example = "1")
    private Integer partialFailBatchCount;

    @Schema(description = "完全失败批次数量", example = "1")
    private Integer totalFailBatchCount;
}
