package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 离线记录上传结果VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Data
@Schema(description = "离线记录上传结果VO")
public class OfflineRecordUploadResultVO {

    @Schema(description = "总记录数")
    private Integer total;

    @Schema(description = "成功上传数")
    private Integer successCount;

    @Schema(description = "失败数")
    private Integer failedCount;

    @Schema(description = "成功的记录ID列表")
    private List<String> successRecordIds;

    @Schema(description = "失败的记录ID列表")
    private List<String> failedRecordIds;

    @Schema(description = "错误信息映射")
    private List<RecordError> errors;

    /**
     * 记录错误信息
     */
    @Data
    @Schema(description = "记录错误信息")
    public static class RecordError {
        @Schema(description = "记录ID")
        private String recordId;

        @Schema(description = "错误信息")
        private String errorMessage;

        @Schema(description = "错误码")
        private String errorCode;
    }
}
