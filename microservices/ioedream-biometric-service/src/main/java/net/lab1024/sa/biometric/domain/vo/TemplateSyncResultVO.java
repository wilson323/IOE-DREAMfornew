package net.lab1024.sa.biometric.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 模板同步结果视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@Schema(description = "模板同步结果视图对象")
public class TemplateSyncResultVO {

    @Schema(description = "总数量", example = "10")
    private Integer totalCount;

    @Schema(description = "成功数量", example = "8")
    private Integer successCount;

    @Schema(description = "失败数量", example = "2")
    private Integer failCount;

    @Schema(description = "同步记录列表")
    private List<TemplateSyncRecordVO> syncRecords;
}
