package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.vo.*;

/**
 * 考勤仪表中心服务接口
 * <p>
 * 提供仪表中心数据聚合功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
public interface DashboardService {

    /**
     * 获取首页概览数据
     *
     * @return 首页概览数据
     */
    DashboardOverviewVO getOverviewData();

    /**
     * 获取个人看板数据
     *
     * @param userId 用户ID
     * @return 个人看板数据
     */
    DashboardPersonalVO getPersonalDashboard(Long userId);

    /**
     * 获取部门看板数据
     *
     * @param departmentId 部门ID
     * @return 部门看板数据
     */
    DashboardDepartmentVO getDepartmentDashboard(Long departmentId);

    /**
     * 获取企业看板数据
     *
     * @return 企业看板数据
     */
    DashboardEnterpriseVO getEnterpriseDashboard();

    /**
     * 获取考勤趋势数据
     *
     * @param timeDimension 时间维度（DAY/WEEK/MONTH）
     * @param trendType 趋势类型（ATTENDANCE_RATE/WORK_HOURS/OVERTIME）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据
     */
    DashboardTrendVO getTrendData(String timeDimension, String trendType, String startDate, String endDate);

    /**
     * 获取部门热力图数据
     *
     * @param heatmapType 热力图类型（DEPARTMENT/ATTENDANCE_RATE/WORK_HOURS）
     * @param statisticsDate 统计日期
     * @return 热力图数据
     */
    DashboardHeatmapVO getHeatmapData(String heatmapType, String statisticsDate);

    /**
     * 获取实时统计数据
     *
     * @return 实时统计数据
     */
    DashboardRealtimeVO getRealtimeData();

    /**
     * 刷新看板数据
     *
     * @param refreshType 刷新类型（ALL/DEPARTMENT/PERSONAL）
     * @param targetId 目标ID（刷新特定部门或个人时需要）
     * @return 刷新结果
     */
    String refreshDashboard(String refreshType, Long targetId);

    /**
     * 获取快速操作权限
     *
     * @param userId 用户ID
     * @return 快速操作权限
     */
    java.util.Map<String, Boolean> getQuickActionPermissions(Long userId);
}
