package net.lab1024.sa.consume.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费统计服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public interface ConsumeStatisticsService {

    /**
     * 获取总体消费统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总体统计
     */
    Map<String, Object> getConsumeOverview(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取用户消费排行榜
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 用户排行榜
     */
    List<Map<String, Object>> getUserConsumeRanking(LocalDateTime startDate, LocalDateTime endDate, Integer limit);

    /**
     * 获取商户销售排行榜
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 商户排行榜
     */
    List<Map<String, Object>> getMerchantSalesRanking(LocalDateTime startDate, LocalDateTime endDate, Integer limit);

    /**
     * 获取商品销售排行榜
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 商品排行榜
     */
    List<Map<String, Object>> getProductSalesRanking(LocalDateTime startDate, LocalDateTime endDate, Integer limit);

    /**
     * 获取时段消费分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param granularity 粒度（hour/day/week/month）
     * @return 时段分析
     */
    Map<String, Object> getTimePeriodAnalysis(LocalDateTime startDate, LocalDateTime endDate, String granularity);

    /**
     * 获取消费方式统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 方式统计
     */
    Map<String, Object> getPaymentMethodStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取设备消费统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备统计
     */
    Map<String, Object> getDeviceStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取区域消费统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 区域统计
     */
    Map<String, Object> getAreaStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取消费趋势数据
     *
     * @param type 趋势类型（amount/count/avg）
     * @param days 天数
     * @return 趋势数据
     */
    Map<String, Object> getConsumeTrends(String type, Integer days);

    /**
     * 获取用户消费分析
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户分析
     */
    Map<String, Object> getUserConsumeAnalysis(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取实时消费数据
     *
     * @return 实时数据
     */
    Map<String, Object> getRealtimeStatistics();

    /**
     * 获取对比数据
     *
     * @param currentPeriodStart 当前周期开始
     * @param currentPeriodEnd 当前周期结束
     * @param previousPeriodStart 对比周期开始
     * @param previousPeriodEnd 对比周期结束
     * @return 对比数据
     */
    Map<String, Object> getComparisonData(LocalDateTime currentPeriodStart, LocalDateTime currentPeriodEnd,
                                         LocalDateTime previousPeriodStart, LocalDateTime previousPeriodEnd);

    /**
     * 导出统计报表
     *
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 导出结果
     */
    String exportStatisticsReport(String reportType, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取消费预测数据
     *
     * @param days 预测天数
     * @return 预测数据
     */
    Map<String, Object> getConsumePrediction(Integer days);
}