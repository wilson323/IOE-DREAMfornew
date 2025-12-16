package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户门禁权限响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户门禁权限响应")
public class UserAccessPermissionResponse {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "权限总数", example = "25")
    private Integer totalPermissions;

    @Schema(description = "有效权限数", example = "23")
    private Integer validPermissions;

    @Schema(description = "过期权限数", example = "2")
    private Integer expiredPermissions;

    @Schema(description = "权限列表")
    private List<AccessPermission> permissions;

    @Schema(description = "权限统计信息")
    private PermissionStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "门禁权限")
    public static class AccessPermission {

        @Schema(description = "权限ID", example = "10001")
        private Long permissionId;

        @Schema(description = "设备ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "设备名称", example = "主门禁")
        private String deviceName;

        @Schema(description = "设备类型", example = "access", allowableValues = {"access", "turnstile", "gate"})
        private String deviceType;

        @Schema(description = "设备类型名称", example = "门禁控制器")
        private String deviceTypeName;

        @Schema(description = "区域ID", example = "1")
        private Long areaId;

        @Schema(description = "区域名称", example = "一楼大厅")
        private String areaName;

        @Schema(description = "权限类型", example = "permanent", allowableValues = {"permanent", "temporary", "scheduled"})
        private String permissionType;

        @Schema(description = "权限类型名称", example = "永久权限")
        private String permissionTypeName;

        @Schema(description = "权限级别", example = "normal", allowableValues = {"normal", "vip", "emergency"})
        private String permissionLevel;

        @Schema(description = "权限级别名称", example = "普通")
        private String permissionLevelName;

        @Schema(description = "权限状态", example = "1", allowableValues = {"0", "1", "2"})
        private Integer permissionStatus;

        @Schema(description = "权限状态名称", example = "有效")
        private String permissionStatusName;

        @Schema(description = "生效时间", example = "2025-12-16T00:00:00")
        private LocalDateTime effectiveTime;

        @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
        private LocalDateTime expireTime;

        @Schema(description = "创建时间", example = "2025-12-01T10:30:00")
        private LocalDateTime createTime;

        @Schema(description = "创建人ID", example = "1002")
        private Long creatorId;

        @Schema(description = "创建人姓名", example = "管理员")
        private String creatorName;

        @Schema(description = "审批人ID", example = "1003")
        private Long approverId;

        @Schema(description = "审批人姓名", example = "部门经理")
        private String approverName;

        @Schema(description = "审批时间", example = "2025-12-01T11:00:00")
        private LocalDateTime approveTime;

        @Schema(description = "通行次数限制", example = "100")
        private Integer maxAccessCount;

        @Schema(description = "已使用次数", example = "25")
        private Integer usedAccessCount;

        @Schema(description = "剩余次数", example = "75")
        private Integer remainingAccessCount;

        @Schema(description = "每日通行次数限制", example = "10")
        private Integer maxDailyAccessCount;

        @Schema(description = "今日已使用次数", example = "3")
        private Integer todayUsedCount;

        @Schema(description = "今日剩余次数", example = "7")
        private Integer todayRemainingCount;

        @Schema(description = "允许通行的时间段")
        private List<TimeSlot> allowedTimeSlots;

        @Schema(description = "通行方向", example = "both", allowableValues = {"in", "out", "both"})
        private String allowedDirection;

        @Schema(description = "验证方式", example = "card,face", allowableValues = {"card", "face", "fingerprint", "password", "qr_code"})
        private String allowedVerifyMethods;

        @Schema(description = "权限原因", example = "办公需要")
        private String reason;

        @Schema(description = "备注", example = "")
        private String remark;

        @Schema(description = "扩展参数", example = "{\"key1\":\"value1\"}")
        private String extendedParams;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "时间段")
    public static class TimeSlot {

        @Schema(description = "星期几（1-7，1为周一）", example = "1-5")
        private String dayOfWeek;

        @Schema(description = "星期名称", example = "周一至周五")
        private String dayName;

        @Schema(description = "开始时间", example = "08:00")
        private String startTime;

        @Schema(description = "结束时间", example = "18:00")
        private String endTime;

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限统计信息")
    public static class PermissionStatistics {

        @Schema(description = "总权限数", example = "25")
        private Integer totalPermissions;

        @Schema(description = "永久权限数", example = "20")
        private Integer permanentPermissions;

        @Schema(description = "临时权限数", example = "3")
        private Integer temporaryPermissions;

        @Schema(description = "定时权限数", example = "2")
        private Integer scheduledPermissions;

        @Schema(description = "有效权限数", example = "23")
        private Integer validPermissions;

        @Schema(description = "过期权限数", example = "2")
        private Integer expiredPermissions;

        @Schema(description = "即将过期权限数（7天内）", example = "1")
        private Integer expiringSoonPermissions;

        @Schema(description = "高权限级别数量", example = "5")
        private Integer highLevelPermissions;

        @Schema(description = "普通权限级别数量", example = "15")
        private Integer normalLevelPermissions;

        @Schema(description = "紧急权限级别数量", example = "3")
        private Integer emergencyLevelPermissions;

        @Schema(description = "覆盖区域数量", example = "5")
        private Integer coveredAreas;

        @Schema(description = "覆盖设备数量", example = "15")
        private Integer coveredDevices;

        @Schema(description = "最近使用时间", example = "2025-12-16T08:30:00")
        private LocalDateTime lastUsedTime;

        @Schema(description = "本月使用次数", example = "156")
        private Integer monthlyUsageCount;

        @Schema(description = "权限利用率", example = "78.5")
        private Double utilizationRate;
    }
}