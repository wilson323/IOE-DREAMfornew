package net.lab1024.sa.attendance.service;

import java.util.concurrent.CompletableFuture;

import net.lab1024.sa.attendance.domain.vo.*;

/**
 * 考勤仪表中心异步服务接口
 * <p>
 * 提供仪表中心数据聚合异步功能，提升并发处理能力
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
public interface DashboardAsyncService {

    /**
     * 异步获取首页概览数据
     *
     * @return CompletableFuture<DashboardOverviewVO>
     */
    CompletableFuture<DashboardOverviewVO> getOverviewDataAsync();

    /**
     * 异步获取个人仪表数据
     *
     * @param userId 用户ID
     * @return CompletableFuture<DashboardPersonalVO>
     */
    CompletableFuture<DashboardPersonalVO> getPersonalDashboardAsync(Long userId);

    /**
     * 异步获取部门仪表数据
     *
     * @param departmentId 部门ID
     * @return CompletableFuture<DashboardDepartmentVO>
     */
    CompletableFuture<DashboardDepartmentVO> getDepartmentDashboardAsync(Long departmentId);

    /**
     * 异步获取企业仪表数据
     *
     * @return CompletableFuture<DashboardEnterpriseVO>
     */
    CompletableFuture<DashboardEnterpriseVO> getEnterpriseDashboardAsync();

    /**
     * 异步获取实时统计数据
     *
     * @return CompletableFuture<DashboardRealtimeVO>
     */
    CompletableFuture<DashboardRealtimeVO> getRealtimeDataAsync();

    /**
     * 异步刷新看板数据
     *
     * @param refreshType 刷新类型（overview/personal/department/enterprise）
     * @param targetId 目标ID（用户ID或部门ID）
     * @return CompletableFuture<String>
     */
    CompletableFuture<String> refreshDashboardAsync(String refreshType, Long targetId);
}
