package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量同步结果VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量同步结果")
public class SyncResultVO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "总记录数")
    private Integer totalCount;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;

    @Schema(description = "冲突数量")
    private Integer conflictCount;

    @Schema(description = "错误信息")
    private List<String> errors;

    @Schema(description = "同步批次号")
    private String syncBatchNo;

    @Schema(description = "耗时（毫秒）")
    private Long durationMs;

    public static SyncResultVO success(String syncBatchNo, Integer totalCount, Long durationMs) {
        return SyncResultVO.builder()
            .success(true)
            .totalCount(totalCount)
            .successCount(totalCount)
            .failedCount(0)
            .conflictCount(0)
            .syncBatchNo(syncBatchNo)
            .durationMs(durationMs)
            .build();
    }

    public static SyncResultVO fail(List<String> errors) {
        return SyncResultVO.builder()
            .success(false)
            .totalCount(0)
            .successCount(0)
            .failedCount(0)
            .conflictCount(0)
            .errors(errors)
            .build();
    }
}
