package net.lab1024.sa.admin.module.access.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁事件统计DTO
 * <p>
 * 用于返回门禁事件的统计数据
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */

@Schema(description = "门禁事件统计DTO")
public class AccessEventStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计时间范围开始
     */
    @Schema(description = "统计时间范围开始", example = "2025-11-16T00:00:00")
    private LocalDateTime startTime;

    /**
     * 统计时间范围结束
     */
    @Schema(description = "统计时间范围结束", example = "2025-11-16T23:59:59")
    private LocalDateTime endTime;

    /**
     * 总通行次数
     */
    @Schema(description = "总通行次数", example = "1234")
    private Long totalPassCount;

    /**
     * 通行成功次数
     */
    @Schema(description = "通行成功次数", example = "1198")
    private Long successCount;

    /**
     * 通行失败次数
     */
    @Schema(description = "通行失败次数", example = "36")
    private Long failCount;

    /**
     * 通行成功率
     */
    @Schema(description = "通行成功率（百分比）", example = "97.08")
    private Double successRate;

    /**
     * 平均通行时间（毫秒）
     */
    @Schema(description = "平均通行时间（毫秒）", example = "2500")
    private Long avgPassTime;

    /**
     * 高峰期通行次数
     */
    @Schema(description = "高峰期通行次数", example = "456")
    private Long peakHourCount;

    /**
     * 异常事件次数
     */
    @Schema(description = "异常事件次数", example = "12")
    private Long abnormalEventCount;

    /**
     * 黑名单尝试次数
     */
    @Schema(description = "黑名单尝试次数", example = "3")
    private Long blacklistCount;

    /**
     * 温度异常次数
     */
    @Schema(description = "温度异常次数", example = "5")
    private Long temperatureAbnormalCount;

    /**
     * 按小时统计
     */
    @Schema(description = "按小时统计数据")
    private List<HourlyStatistics> hourlyStatistics;

    /**
     * 按区域统计
     */
    @Schema(description = "按区域统计数据")
    private List<AreaStatistics> areaStatistics;

    /**
     * 按设备统计
     */
    @Schema(description = "按设备统计数据")
    private List<DeviceStatistics> deviceStatistics;

    /**
     * 按验证方式统计
     */
    @Schema(description = "按验证方式统计数据")
    private List<VerifyMethodStatistics> verifyMethodStatistics;

    /**
     * 按用户统计（Top 10）
     */
    @Schema(description = "按用户统计数据（Top 10）")
    private List<UserStatistics> topUserStatistics;

    /**
     * 最近24小时趋势数据
     */
    @Schema(description = "最近24小时趋势数据")
    private List<TrendData> trendData24h;

    /**
     * 最近7天趋势数据
     */
    @Schema(description = "最近7天趋势数据")
    private List<TrendData> trendData7d;

    /**
     * 实时统计摘要
     */
    @Schema(description = "实时统计摘要")
    private RealtimeSummary realtimeSummary;

    /**
     * 按小时统计数据内部类
     */
    
    @Schema(description = "按小时统计数据")
    public static class HourlyStatistics {
        @Schema(description = "小时", example = "9")
        private Integer hour;

        @Schema(description = "通行次数", example = "156")
        private Long passCount;

        @Schema(description = "成功次数", example = "152")
        private Long successCount;

        @Schema(description = "失败次数", example = "4")
        private Long failCount;
    }

    /**
     * 按区域统计数据内部类
     */
    
    @Schema(description = "按区域统计数据")
    public static class AreaStatistics {
        @Schema(description = "区域ID", example = "1")
        private Long areaId;

        @Schema(description = "区域名称", example = "主园区")
        private String areaName;

        @Schema(description = "通行次数", example = "234")
        private Long passCount;

        @Schema(description = "占比（百分比）", example = "18.95")
        private Double percentage;
    }

    /**
     * 按设备统计数据内部类
     */
    
    @Schema(description = "按设备统计数据")
    public static class DeviceStatistics {
        @Schema(description = "设备ID", example = "1")
        private Long deviceId;

        @Schema(description = "设备名称", example = "主门禁机")
        private String deviceName;

        @Schema(description = "通行次数", example = "345")
        private Long passCount;

        @Schema(description = "在线状态 0:离线 1:在线", example = "1")
        private Integer onlineStatus;
    }

    /**
     * 按验证方式统计数据内部类
     */
    
    @Schema(description = "按验证方式统计数据")
    public static class VerifyMethodStatistics {
        @Schema(description = "验证方式", example = "4")
        private Integer verifyMethod;

        @Schema(description = "验证方式名称", example = "人脸识别")
        private String verifyMethodName;

        @Schema(description = "使用次数", example = "678")
        private Long count;

        @Schema(description = "占比（百分比）", example = "54.95")
        private Double percentage;

        @Schema(description = "成功率", example = "99.12")
        private Double successRate;
    }

    /**
     * 按用户统计数据内部类
     */
    
    @Schema(description = "按用户统计数据")
    public static class UserStatistics {
        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "用户姓名", example = "张三")
        private String userName;

        @Schema(description = "部门名称", example = "技术部")
        private String departmentName;

        @Schema(description = "通行次数", example = "23")
        private Long passCount;

        @Schema(description = "最后通行时间", example = "2025-11-16T18:30:00")
        private LocalDateTime lastPassTime;
    }

    /**
     * 趋势数据内部类
     */
    
    @Schema(description = "趋势数据")
    public static class TrendData {
        @Schema(description = "时间标签", example = "2025-11-16T09:00:00")
        private LocalDateTime timeLabel;

        @Schema(description = "通行次数", example = "45")
        private Long passCount;

        @Schema(description = "成功次数", example = "44")
        private Long successCount;

        @Schema(description = "失败次数", example = "1")
        private Long failCount;
    }

    /**
     * 实时统计摘要内部类
     */
    
    @Schema(description = "实时统计摘要")
    public static class RealtimeSummary {
        @Schema(description = "在线设备数量", example = "15")
        private Integer onlineDeviceCount;

        @Schema(description = "离线设备数量", example = "2")
        private Integer offlineDeviceCount;

        @Schema(description = "今日已通行人数", example = "234")
        private Integer todayPassPersonCount;

        @Schema(description = "当前在园区人数", example = "189")
        private Integer currentInAreaCount;

        @Schema(description = "未处理异常事件数", example = "3")
        private Integer pendingAbnormalEvents;

        @Schema(description = "最近5分钟通行次数", example = "12")
        private Integer recent5MinPassCount;
    }
}