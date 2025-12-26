package net.lab1024.sa.attendance.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 考勤记录查询请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "考勤记录查询请求")
public class AttendanceRecordQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "打卡类型", example = "on", allowableValues = {"on", "off", "break_start", "break_end"})
    private String clockType;

    @Schema(description = "打卡方式", example = "face", allowableValues = {"face", "fingerprint", "card", "password", "location", "manual"})
    private String clockMethod;

    @Schema(description = "开始日期", example = "2025-12-16")
    private String startDate;

    @Schema(description = "结束日期", example = "2025-12-16")
    private String endDate;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "排班ID", example = "1")
    private Long shiftId;

    @Schema(description = "打卡状态", example = "normal", allowableValues = {"normal", "late", "early", "absent", "leave"})
    private String clockStatus;

    @Schema(description = "是否有异常", example = "false")
    private Boolean hasAbnormal;

    @Schema(description = "设备ID", example = "ATTEND_001")
    private String deviceId;

    @Schema(description = "排序字段", example = "clockTime")
    private String sortField;

    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder;

    @Schema(description = "是否包含补卡记录", example = "true")
    private Boolean includeSupplement;

    @Schema(description = "最低体温", example = "35.0")
    private Double minTemperature;

    @Schema(description = "最高体温", example = "38.0")
    private Double maxTemperature;

    @Schema(description = "是否佩戴口罩过滤", example = "true")
    private Boolean wearingMaskFilter;

    @Schema(description = "佩戴口罩状态", example = "true", allowableValues = {"true", "false"})
    private Boolean wearingMask;
}
