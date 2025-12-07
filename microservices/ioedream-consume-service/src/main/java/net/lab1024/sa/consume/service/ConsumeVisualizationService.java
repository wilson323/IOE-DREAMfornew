package net.lab1024.sa.consume.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumeStatisticsForm;
import net.lab1024.sa.consume.domain.vo.ConsumeComparisonVO;
import net.lab1024.sa.consume.domain.vo.ConsumeForecastAnalysisVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRankingVO;
import net.lab1024.sa.consume.domain.vo.ConsumeStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTrendVO;
import net.lab1024.sa.consume.domain.vo.ConsumeUserBehaviorAnalysisVO;

/**
 * 消费可视化统计服务接口
 * <p>
 * 提供消费数据的可视化分析和统计功能
 * 包括基础统计、趋势分析、排名分析、对比分析、用户行为分析、预测分析等
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 * @version 1.0.0
 */
public interface ConsumeVisualizationService {

    /**
     * 获取消费统计数据
     *
     * @param form 统计查询表单
     * @return 统计数据
     */
    ResponseDTO<ConsumeStatisticsVO> getStatistics(ConsumeStatisticsForm form);

    /**
     * 获取消费趋势数据
     *
     * @param form 统计查询表单
     * @return 趋势数据
     */
    ResponseDTO<ConsumeTrendVO> getTrend(ConsumeStatisticsForm form);

    /**
     * 获取消费排名数据
     *
     * @param form 统计查询表单
     * @return 排名数据
     */
    ResponseDTO<ConsumeRankingVO> getRanking(ConsumeStatisticsForm form);

    /**
     * 获取消费对比分析数据
     *
     * @param form 统计查询表单
     * @return 对比分析数据
     */
    ResponseDTO<ConsumeComparisonVO> getComparison(ConsumeStatisticsForm form);

    /**
     * 获取用户行为分析数据
     *
     * @param form 统计查询表单
     * @return 用户行为分析数据
     */
    ResponseDTO<ConsumeUserBehaviorAnalysisVO> getUserBehaviorAnalysis(ConsumeStatisticsForm form);

    /**
     * 获取消费预测分析数据
     *
     * @param form 统计查询表单
     * @return 预测分析数据
     */
    ResponseDTO<ConsumeForecastAnalysisVO> getForecastAnalysis(ConsumeStatisticsForm form);
}
