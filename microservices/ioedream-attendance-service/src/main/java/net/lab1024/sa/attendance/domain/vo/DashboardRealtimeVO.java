package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 实时统计数据视图对象
 * <p>
 * 用于实时考勤数据展示
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实时统计数据视图对象")
public class DashboardRealtimeVO {

    /**
     * 数据更新时间
     */
    @Schema(description = "数据更新时间", example = "2025-12-23T15:30:00")
    private LocalDateTime updateTime;

    /**
     * 今日已打卡人数
     */
    @Schema(description = "今日已打卡人数", example = "450")
    private Integer todayPunchedCount;

    /**
     * 今日未打卡人数
     */
    @Schema(description = "今日未打卡人数", example = "50")
    private Integer todayNotPunchedCount;

    /**
     * 当前在线设备数
     */
    @Schema(description = "当前在线设备数", example = "45")
    private Integer onlineDeviceCount;

    /**
     * 当前离线设备数
     */
    @Schema(description = "当前离线设备数", example = "5")
    private Integer offlineDeviceCount;

    /**
     * 实时打卡记录（最近10条）
     */
    @Schema(description = "实时打卡记录（最近10条）")
    private List<Map<String, Object>> recentPunchRecords;

    /**
     * 实时告警信息（最近10条）
     */
    @Schema(description = "实时告警信息（最近10条）")
    private List<Map<String, Object>> recentAlerts;

    /**
     * 部门实时打卡统计
     */
    @Schema(description = "部门实时打卡统计")
    private List<Map<String, Object>> departmentPunchStats;

    /**
     * 每小时打卡统计（24小时）
     */
    @Schema(description = "每小时打卡统计（24小时）")
    private List<Map<String, Object>> hourlyPunchStats;

    /**
     * 设备状态统计
     */
    @Schema(description = "设备状态统计")
    private Map<String, Object> deviceStatusStats;
}
