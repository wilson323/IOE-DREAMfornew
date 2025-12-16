package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁统计信息响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁统计信息响应")
public class AccessStatisticsResponse {

    @Schema(description = "统计类型", example = "daily", allowableValues = {"daily", "weekly", "monthly", "yearly", "custom"})
    private String statisticsType;

    @Schema(description = "统计类型名称", example = "日统计")
    private String statisticsTypeName;

    @Schema(description = "开始日期", example = "2025-12-16")
    private String startDate;

    @Schema(description = "结束日期", example = "2025-12-16")
    private String endDate;

    @Schema(description = "统计时间", example = "2025-12-16T15:30:00")
    private LocalDateTime statisticsTime;

    @Schema(description = "总通行次数", example = "1523")
    private Long totalAccessCount;

    @Schema(description = "成功通行次数", example = "1498")
    private Long successAccessCount;

    @Schema(description = "失败通行次数", example = "25")
    private Long failAccessCount;

    @Schema(description = "通行成功率", example = "98.36")
    private BigDecimal successRate;

    @Schema(description = "进入次数", example = "789")
    private Long enterCount;

    @Schema(description = "离开次数", example = "734")
    private Long exitCount;

    @Schema(description = "平均每次通行耗时（毫秒）", example = "450")
    private Long averageProcessTime;

    @Schema(description = "最大通行耗时（毫秒）", example = "2000")
    private Long maxProcessTime;

    @Schema(description = "最小通行耗时（毫秒）", example = "120")
    private Long minProcessTime;

    @Schema(description = "活跃用户数", example = "234")
    private Integer activeUserCount;

    @Schema(description = "活跃设备数", example = "14")
    private Integer activeDeviceCount;

    @Schema(description = "异常记录数", example = "12")
    private Integer abnormalRecordCount;

    @Schema(description = "体温异常人数", example = "3")
    private Integer temperatureAbnormalCount;

    @Schema(description = "佩戴口罩人数", example = "1156")
    private Integer maskWearingCount;

    @Schema(description = "口罩佩戴率", example = "75.92")
    private BigDecimal maskWearingRate;

    @Schema(description = "时段统计")
    private List<TimeSlotStatistics> timeSlotStatistics;

    @Schema(description = "验证方式统计")
    private List<VerifyMethodStatistics> verifyMethodStatistics;

    @Schema(description = "设备统计")
    private List<DeviceStatistics> deviceStatistics;

    @Schema(description = "区域统计")
    private List<AreaStatistics> areaStatistics;

    @Schema(description = "用户统计")
    private List<UserStatistics> userStatistics;

    @Schema(description = "部门统计")
    private List<DepartmentStatistics> departmentStatistics;

    @Schema(description = "趋势数据")
    private Map<String, Object> trendData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "时段统计")
    public static class TimeSlotStatistics {

        @Schema(description = "时间段", example = "08:00-09:00")
        private String timeSlot;

        @Schema(description = "通行次数", example = "156")
        private Long accessCount;

        @Schema(description = "成功次数", example = "152")
        private Long successCount;

        @Schema(description = "失败次数", example = "4")
        private Long failCount;

        @Schema(description = "成功率", example = "97.44")
        private BigDecimal successRate;

        @Schema(description = "进入次数", example = "89")
        private Long enterCount;

        @Schema(description = "离开次数", example = "67")
        private Long exitCount;

        @Schema(description = "平均耗时（毫秒）", example = "420")
        private Long averageProcessTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "验证方式统计")
    public static class VerifyMethodStatistics {

        @Schema(description = "验证方式", example = "face")
        private String verifyMethod;

        @Schema(description = "验证方式名称", example = "人脸识别")
        private String verifyMethodName;

        @Schema(description = "使用次数", example = "856")
        private Long usageCount;

        @Schema(description = "使用率", example = "56.20")
        private BigDecimal usageRate;

        @Schema(description = "成功次数", example = "845")
        private Long successCount;

        @Schema(description = "失败次数", example = "11")
        private Long failCount;

        @Schema(description = "成功率", example = "98.71")
        private BigDecimal successRate;

        @Schema(description = "平均耗时（毫秒）", example = "380")
        private Long averageProcessTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "设备统计")
    public static class DeviceStatistics {

        @Schema(description = "设备ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "设备名称", example = "主门禁")
        private String deviceName;

        @Schema(description = "设备类型", example = "access")
        private String deviceType;

        @Schema(description = "通行次数", example = "234")
        private Long accessCount;

        @Schema(description = "成功次数", example = "230")
        private Long successCount;

        @Schema(description = "失败次数", example = "4")
        private Long failCount;

        @Schema(description = "成功率", example = "98.29")
        private BigDecimal successRate;

        @Schema(description = "平均耗时（毫秒）", example = "410")
        private Long averageProcessTime;

        @Schema(description = "在线时长（小时）", example = "24.0")
        private Double onlineDuration;

        @Schema(description = "故障次数", example = "0")
        private Integer faultCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "区域统计")
    public static class AreaStatistics {

        @Schema(description = "区域ID", example = "1")
        private Long areaId;

        @Schema(description = "区域名称", example = "一楼大厅")
        private String areaName;

        @Schema(description = "区域类型", example = "entrance")
        private String areaType;

        @Schema(description = "通行次数", example = "567")
        private Long accessCount;

        @Schema(description = "成功次数", example = "558")
        private Long successCount;

        @Schema(description = "失败次数", example = "9")
        private Long failCount;

        @Schema(description = "成功率", example = "98.41")
        private BigDecimal successRate;

        @Schema(description = "进入次数", example = "298")
        private Long enterCount;

        @Schema(description = "离开次数", example = "269")
        private Long exitCount;

        @Schema(description = "峰值时段", example = "09:00-10:00")
        private String peakTimeSlot;

        @Schema(description = "峰值通行次数", example = "89")
        private Long peakAccessCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户统计")
    public static class UserStatistics {

        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "用户名", example = "admin")
        private String username;

        @Schema(description = "真实姓名", example = "系统管理员")
        private String realName;

        @Schema(description = "部门名称", example = "技术部")
        private String departmentName;

        @Schema(description = "通行次数", example = "12")
        private Long accessCount;

        @Schema(description = "成功次数", example = "12")
        private Long successCount;

        @Schema(description = "失败次数", example = "0")
        private Long failCount;

        @Schema(description = "首次通行时间", example = "2025-12-16T08:30:00")
        private LocalDateTime firstAccessTime;

        @Schema(description = "最后通行时间", example = "2025-12-16T18:45:00")
        private LocalDateTime lastAccessTime;

        @Schema(description = "常用验证方式", example = "face")
        private String primaryVerifyMethod;

        @Schema(description = "常用设备", example = "ACCESS_001")
        private String primaryDevice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "部门统计")
    public static class DepartmentStatistics {

        @Schema(description = "部门ID", example = "1")
        private Long departmentId;

        @Schema(description = "部门名称", example = "技术部")
        private String departmentName;

        @Schema(description = "部门人数", example = "25")
        private Integer employeeCount;

        @Schema(description = "活跃人数", example = "23")
        private Integer activeCount;

        @Schema(description = "通行次数", example = "234")
        private Long accessCount;

        @Schema(description = "人均通行次数", example = "9.36")
        private BigDecimal averageAccessCount;

        @Schema(description = "成功次数", example = "230")
        private Long successCount;

        @Schema(description = "成功率", example = "98.29")
        private BigDecimal successRate;

        @Schema(description = "最早通行时间", example = "2025-12-16T08:15:00")
        private LocalDateTime earliestAccessTime;

        @Schema(description = "最晚通行时间", example = "2025-12-16T20:30:00")
        private LocalDateTime latestAccessTime;
    }
}