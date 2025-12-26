package net.lab1024.sa.consume.service;

import net.lab1024.sa.consume.domain.form.ConsumptionAnalysisQueryForm;
import net.lab1024.sa.consume.domain.vo.*;

import java.util.List;

/**
 * 消费分析服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
public interface ConsumeAnalysisService {

    /**
     * 获取消费数据分析（完整分析）
     *
     * @param queryForm 查询条件
     * @return 消费分析结果
     */
    ConsumptionAnalysisVO getConsumptionAnalysis(ConsumptionAnalysisQueryForm queryForm);

    /**
     * 获取消费趋势数据
     *
     * @param queryForm 查询条件
     * @return 趋势数据列表
     */
    List<ConsumptionTrendVO> getConsumptionTrend(ConsumptionAnalysisQueryForm queryForm);

    /**
     * 获取消费分类统计
     *
     * @param queryForm 查询条件
     * @return 分类统计列表
     */
    List<CategoryStatsVO> getCategoryStats(ConsumptionAnalysisQueryForm queryForm);

    /**
     * 获取消费习惯分析
     *
     * @param userId 用户ID
     * @param period 时间周期
     * @return 消费习惯分析
     */
    ConsumptionHabitsVO getConsumptionHabits(Long userId, String period);

    /**
     * 获取智能推荐
     *
     * @param userId 用户ID
     * @param period 时间周期
     * @return 智能推荐列表
     */
    List<SmartRecommendationVO> getSmartRecommendations(Long userId, String period);
}
