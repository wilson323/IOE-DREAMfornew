package net.lab1024.sa.attendance.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 考勤异常查询请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "考勤异常查询请求")
public class AbnormalAttendanceQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "异常类型", example = "late", allowableValues = {
            "late", "early", "absent", "forgot_clock", "illegal_location", "abnormal_temperature", "no_face_verify"})
    private String abnormalType;

    @Schema(description = "开始日期", example = "2025-12-16")
    private String startDate;

    @Schema(description = "结束日期", example = "2025-12-16")
    private String endDate;

    @Schema(description = "处理状态", example = "unprocessed", allowableValues = {
            "unprocessed", "processing", "processed", "ignored"})
    private String processStatus;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "排班ID", example = "1")
    private Long shiftId;

    @Schema(description = "严重程度", example = "medium", allowableValues = {"low", "medium", "high", "critical"})
    private String severityLevel;

    @Schema(description = "是否需要处理", example = "true")
    private Boolean requireProcess;

    @Schema(description = "异常时间段", example = "morning", allowableValues = {"morning", "afternoon", "evening", "night"})
    private String abnormalTimePeriod;

    @Schema(description = "打卡方式", example = "face", allowableValues = {"face", "fingerprint", "card", "password", "location", "manual"})
    private String clockMethod;

    @Schema(description = "设备ID", example = "ATTEND_001")
    private String deviceId;

    @Schema(description = "排序字段", example = "createTime")
    private String sortField;

    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder;

    @Schema(description = "是否已通知", example = "false")
    private Boolean notified;

    @Schema(description = "创建人ID", example = "1002")
    private Long creatorId;
}
