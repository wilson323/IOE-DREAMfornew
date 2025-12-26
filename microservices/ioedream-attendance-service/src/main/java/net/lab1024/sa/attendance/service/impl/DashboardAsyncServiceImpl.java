package net.lab1024.sa.attendance.service.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.domain.vo.*;
import net.lab1024.sa.attendance.service.DashboardAsyncService;
import net.lab1024.sa.attendance.service.DashboardService;

/**
 * 考勤仪表中心异步服务实现类
 * <p>
 * 提供仪表中心数据聚合异步功能，提升并发处理能力
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class DashboardAsyncServiceImpl implements DashboardAsyncService {

    @Resource
    private DashboardService dashboardService;

    /**
     * 异步获取首页概览数据
     */
    @Override
    @Async("attendanceTaskExecutor")
    public CompletableFuture<DashboardOverviewVO> getOverviewDataAsync() {
        log.info("[仪表中心异步] 开始获取首页概览数据");

        try {
            DashboardOverviewVO result = dashboardService.getOverviewData();
            log.info("[仪表中心异步] 首页概览数据获取成功");
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("[仪表中心异步] 首页概览数据获取失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步获取个人仪表数据
     */
    @Override
    @Async("attendanceTaskExecutor")
    public CompletableFuture<DashboardPersonalVO> getPersonalDashboardAsync(Long userId) {
        log.info("[仪表中心异步] 开始获取个人仪表数据: userId={}", userId);

        try {
            DashboardPersonalVO result = dashboardService.getPersonalDashboard(userId);
            log.info("[仪表中心异步] 个人仪表数据获取成功: userId={}", userId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("[仪表中心异步] 个人仪表数据获取失败: userId={}", userId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步获取部门仪表数据
     */
    @Override
    @Async("attendanceTaskExecutor")
    public CompletableFuture<DashboardDepartmentVO> getDepartmentDashboardAsync(Long departmentId) {
        log.info("[仪表中心异步] 开始获取部门仪表数据: departmentId={}", departmentId);

        try {
            DashboardDepartmentVO result = dashboardService.getDepartmentDashboard(departmentId);
            log.info("[仪表中心异步] 部门仪表数据获取成功: departmentId={}", departmentId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("[仪表中心异步] 部门仪表数据获取失败: departmentId={}", departmentId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步获取企业仪表数据
     */
    @Override
    @Async("attendanceTaskExecutor")
    public CompletableFuture<DashboardEnterpriseVO> getEnterpriseDashboardAsync() {
        log.info("[仪表中心异步] 开始获取企业仪表数据");

        try {
            DashboardEnterpriseVO result = dashboardService.getEnterpriseDashboard();
            log.info("[仪表中心异步] 企业仪表数据获取成功");
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("[仪表中心异步] 企业仪表数据获取失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步获取实时统计数据
     */
    @Override
    @Async("attendanceTaskExecutor")
    public CompletableFuture<DashboardRealtimeVO> getRealtimeDataAsync() {
        log.info("[仪表中心异步] 开始获取实时统计数据");

        try {
            DashboardRealtimeVO result = dashboardService.getRealtimeData();
            log.info("[仪表中心异步] 实时统计数据获取成功");
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("[仪表中心异步] 实时统计数据获取失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步刷新看板数据
     */
    @Override
    @Async("attendanceTaskExecutor")
    public CompletableFuture<String> refreshDashboardAsync(String refreshType, Long targetId) {
        log.info("[仪表中心异步] 开始刷新看板数据: refreshType={}, targetId={}", refreshType, targetId);

        try {
            String result = dashboardService.refreshDashboard(refreshType, targetId);
            log.info("[仪表中心异步] 看板数据刷新成功: refreshType={}, targetId={}", refreshType, targetId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("[仪表中心异步] 看板数据刷新失败: refreshType={}, targetId={}", refreshType, targetId, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
