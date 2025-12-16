package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 授予门禁权限请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "授予门禁权限请求")
public class GrantAccessPermissionRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001", required = true)
    private Long userId;

    @Schema(description = "设备ID列表", example = "[\"ACCESS_001\", \"ACCESS_002\"]")
    private List<String> deviceIds;

    @Schema(description = "设备类型", example = "access", allowableValues = {"access", "turnstile", "gate"})
    private String deviceType;

    @Schema(description = "区域ID列表", example = "[1, 2]")
    private List<Long> areaIds;

    @Schema(description = "权限类型", example = "permanent", allowableValues = {"permanent", "temporary", "scheduled"})
    private String permissionType = "permanent";

    @Schema(description = "生效时间", example = "2025-12-16T00:00:00")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private LocalDateTime expireTime;

    @Schema(description = "允许通行的时间段", example = "[{\"dayOfWeek\":\"1-5\",\"startTime\":\"08:00\",\"endTime\":\"18:00\"}]")
    private List<TimeSlot> allowedTimeSlots;

    @Schema(description = "通行次数限制", example = "100")
    private Integer maxAccessCount;

    @Schema(description = "每日通行次数限制", example = "10")
    private Integer maxDailyAccessCount;

    @Schema(description = "权限级别", example = "normal", allowableValues = {"normal", "vip", "emergency"})
    private String permissionLevel = "normal";

    @Schema(description = "是否需要审核", example = "false")
    private Boolean requireApproval = false;

    @Schema(description = "审批人ID", example = "1002")
    private Long approverId;

    @Schema(description = "权限原因", example = "办公需要")
    private String reason;

    @Schema(description = "扩展参数（JSON格式）", example = "{\"key1\":\"value1\"}")
    private String extendedParams;

    /**
     * 时间段
     */
    @Data
    @Schema(description = "时间段")
    public static class TimeSlot {

        @Schema(description = "星期几（1-7，1为周一）", example = "1-5")
        private String dayOfWeek;

        @Schema(description = "开始时间", example = "08:00")
        private String startTime;

        @Schema(description = "结束时间", example = "18:00")
        private String endTime;
    }
}