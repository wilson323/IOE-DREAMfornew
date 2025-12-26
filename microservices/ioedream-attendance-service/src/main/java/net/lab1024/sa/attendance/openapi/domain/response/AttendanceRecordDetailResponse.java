package net.lab1024.sa.attendance.openapi.domain.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考勤记录详情响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤记录详情响应")
public class AttendanceRecordDetailResponse {

    @Schema(description = "记录基础信息")
    private AttendanceRecordResponse record;

    @Schema(description = "补卡申请信息（如存在）")
    private SupplementApplicationResponse supplementApplication;

    @Schema(description = "处理流水")
    private List<ProcessTrace> processTraces;

    @Schema(description = "创建时间", example = "2025-12-16T09:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-16T09:05:00")
    private LocalDateTime updateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "处理流水")
    public static class ProcessTrace {

        @Schema(description = "处理节点", example = "SYSTEM")
        private String node;

        @Schema(description = "处理动作", example = "AUTO_PROCESS")
        private String action;

        @Schema(description = "处理结果", example = "SUCCESS")
        private String result;

        @Schema(description = "处理人", example = "系统")
        private String operator;

        @Schema(description = "处理时间", example = "2025-12-16T09:02:00")
        private LocalDateTime time;

        @Schema(description = "备注", example = "自动判定正常")
        private String remark;
    }
}
