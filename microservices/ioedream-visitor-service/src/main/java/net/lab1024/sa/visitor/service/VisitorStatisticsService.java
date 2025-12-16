package net.lab1024.sa.visitor.service;

import java.time.LocalDate;
import java.util.Map;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 访客统计服务接口
 * <p>
 * 访客数据统计分析核心业务服务
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义核心业务方法
 * - 实现类在service.impl包中
 * - 使用@Resource依赖注入
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VisitorStatisticsService {

    /**
     * 获取访客统计数据
     *
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getStatistics();

    /**
     * 根据时间范围获取访客统计数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 获取个人访问统计
     * <p>
     * 统计用户作为被访人的访客数据
     * </p>
     *
     * @param userId 用户ID
     * @return 个人统计数据（包含todayAppointments、activeVisitors、monthlyVisitors、averageDuration）
     */
    ResponseDTO<Map<String, Object>> getPersonalStatistics(Long userId);
}

