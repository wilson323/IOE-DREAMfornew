package net.lab1024.sa.admin.module.consume.service.report;

import net.lab1024.sa.base.common.domain.ResponseDTO;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 高级报表服务接口
 * 提供高级可视化和数据分析功能
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
public interface AdvancedReportService {

    /**
     * 生成消费热力图数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 热力图数据
     */
    ResponseDTO<Map<String, Object>> generateConsumeHeatmap(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 生成消费漏斗图数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 漏斗图数据
     */
    ResponseDTO<Map<String, Object>> generateConsumeFunnel(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 生成用户消费行为雷达图
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 雷达图数据
     */
    ResponseDTO<Map<String, Object>> generateUserBehaviorRadar(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 生成实时消费监控大盘数据
     *
     * @return 监控大盘数据
     */
    ResponseDTO<Map<String, Object>> generateRealTimeDashboard();

    /**
     * 生成智能预测分析数据
     *
     * @param predictDays 预测天数
     * @return 预测分析数据
     */
    ResponseDTO<Map<String, Object>> generatePredictiveAnalysis(Integer predictDays);
}