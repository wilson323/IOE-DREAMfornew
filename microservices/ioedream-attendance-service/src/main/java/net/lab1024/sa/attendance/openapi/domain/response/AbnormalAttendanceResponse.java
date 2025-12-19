package net.lab1024.sa.attendance.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤异常响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤异常响应")
public class AbnormalAttendanceResponse {

    @Schema(description = "异常ID", example = "90001")
    private Long abnormalId;

    @Schema(description = "记录ID", example = "100001")
    private Long recordId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "异常类型", example = "late")
    private String abnormalType;

    @Schema(description = "异常原因", example = "迟到 10 分钟")
    private String abnormalReason;

    @Schema(description = "严重程度", example = "medium", allowableValues = {"low", "medium", "high", "critical"})
    private String severityLevel;

    @Schema(description = "异常时间", example = "2025-12-16T09:10:00")
    private LocalDateTime abnormalTime;

    @Schema(description = "处理状态", example = "unprocessed", allowableValues = {"unprocessed", "processing", "processed", "ignored"})
    private String processStatus;

    @Schema(description = "处理人", example = "管理员")
    private String processor;

    @Schema(description = "处理时间", example = "2025-12-16T10:00:00")
    private LocalDateTime processTime;
}

