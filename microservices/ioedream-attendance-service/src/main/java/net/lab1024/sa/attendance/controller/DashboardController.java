package net.lab1024.sa.attendance.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.domain.vo.*;
import net.lab1024.sa.attendance.service.DashboardService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 考勤仪表中心控制器
 * <p>
 * 提供仪表中心数据查询接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/dashboard")
@Tag(name = "考勤仪表中心")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    /**
     * 获取首页概览数据
     */
    @GetMapping("/overview")
    @Operation(summary = "获取首页概览数据")
    @RateLimiter(name = "dashboardApi", fallbackMethod = "overviewFallback")
    public ResponseDTO<DashboardOverviewVO> getOverviewData() {
        log.info("[仪表中心] 查询首页概览数据");

        DashboardOverviewVO overviewData = dashboardService.getOverviewData();

        return ResponseDTO.ok(overviewData);
    }

    /**
     * 获取个人看板数据
     */
    @GetMapping("/personal/{userId}")
    @Operation(summary = "获取个人看板数据")
    public ResponseDTO<DashboardPersonalVO> getPersonalDashboard(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId
    ) {
        log.info("[仪表中心] 查询个人看板数据: userId={}", userId);

        DashboardPersonalVO personalData = dashboardService.getPersonalDashboard(userId);

        return ResponseDTO.ok(personalData);
    }

    /**
     * 获取部门看板数据
     */
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "获取部门看板数据")
    public ResponseDTO<DashboardDepartmentVO> getDepartmentDashboard(
            @Parameter(description = "部门ID", required = true)
            @PathVariable Long departmentId
    ) {
        log.info("[仪表中心] 查询部门看板数据: departmentId={}", departmentId);

        DashboardDepartmentVO departmentData = dashboardService.getDepartmentDashboard(departmentId);

        return ResponseDTO.ok(departmentData);
    }

    /**
     * 获取企业看板数据
     */
    @GetMapping("/enterprise")
    @Operation(summary = "获取企业看板数据")
    public ResponseDTO<DashboardEnterpriseVO> getEnterpriseDashboard() {
        log.info("[仪表中心] 查询企业看板数据");

        DashboardEnterpriseVO enterpriseData = dashboardService.getEnterpriseDashboard();

        return ResponseDTO.ok(enterpriseData);
    }

    /**
     * 获取考勤趋势数据
     */
    @GetMapping("/trend")
    @Operation(summary = "获取考勤趋势数据")
    public ResponseDTO<DashboardTrendVO> getTrendData(
            @Parameter(description = "时间维度（DAY/WEEK/MONTH）", required = true)
            @RequestParam String timeDimension,
            @Parameter(description = "趋势类型（ATTENDANCE_RATE/WORK_HOURS/OVERTIME）", required = true)
            @RequestParam String trendType,
            @Parameter(description = "开始日期", required = true)
            @RequestParam String startDate,
            @Parameter(description = "结束日期", required = true)
            @RequestParam String endDate
    ) {
        log.info("[仪表中心] 查询趋势数据: timeDimension={}, trendType={}, startDate={}, endDate={}",
                timeDimension, trendType, startDate, endDate);

        DashboardTrendVO trendData = dashboardService.getTrendData(timeDimension, trendType, startDate, endDate);

        return ResponseDTO.ok(trendData);
    }

    /**
     * 获取部门热力图数据
     */
    @GetMapping("/heatmap")
    @Operation(summary = "获取部门热力图数据")
    public ResponseDTO<DashboardHeatmapVO> getHeatmapData(
            @Parameter(description = "热力图类型（DEPARTMENT/ATTENDANCE_RATE/WORK_HOURS）", required = true)
            @RequestParam String heatmapType,
            @Parameter(description = "统计日期", required = true)
            @RequestParam String statisticsDate
    ) {
        log.info("[仪表中心] 查询热力图数据: heatmapType={}, statisticsDate={}",
                heatmapType, statisticsDate);

        DashboardHeatmapVO heatmapData = dashboardService.getHeatmapData(heatmapType, statisticsDate);

        return ResponseDTO.ok(heatmapData);
    }

    /**
     * 获取实时统计数据
     */
    @GetMapping("/realtime")
    @Operation(summary = "获取实时统计数据")
    public ResponseDTO<DashboardRealtimeVO> getRealtimeData() {
        log.info("[仪表中心] 查询实时统计数据");

        DashboardRealtimeVO realtimeData = dashboardService.getRealtimeData();

        return ResponseDTO.ok(realtimeData);
    }

    /**
     * 获取快速操作权限
     */
    @GetMapping("/quick-actions/{userId}")
    @Operation(summary = "获取快速操作权限")
    public ResponseDTO<java.util.Map<String, Boolean>> getQuickActionPermissions(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId
    ) {
        log.info("[仪表中心] 查询快速操作权限: userId={}", userId);

        java.util.Map<String, Boolean> permissions = dashboardService.getQuickActionPermissions(userId);

        return ResponseDTO.ok(permissions);
    }

    /**
     * 刷新看板数据
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新看板数据")
    public ResponseDTO<String> refreshDashboard(
            @Parameter(description = "刷新类型（ALL/DEPARTMENT/PERSONAL）", required = true)
            @RequestParam String refreshType,
            @Parameter(description = "目标ID（刷新特定部门或个人时需要）")
            @RequestParam(required = false) Long targetId
    ) {
        log.info("[仪表中心] 刷新看板数据: refreshType={}, targetId={}", refreshType, targetId);

        String result = dashboardService.refreshDashboard(refreshType, targetId);

        return ResponseDTO.ok(result);
    }

    /**
     * 限流降级方法 - 首页概览
     */
    public ResponseDTO<DashboardOverviewVO> overviewFallback(Exception ex) {
        log.warn("[仪表中心] 首页概览API触发限流降级: error={}", ex.getMessage());

        // 返回缓存数据或默认值
        DashboardOverviewVO fallbackData = DashboardOverviewVO.builder()
                .todayPunchCount(0)
                .todayPresentCount(0)
                .todayAttendanceRate(java.math.BigDecimal.ZERO)
                .monthWorkDays(0)
                .pendingApprovalCount(0)
                .departmentCount(0)
                .build();

        return ResponseDTO.ok(fallbackData);
    }
}
