package net.lab1024.sa.admin.module.consume.service;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

import java.util.Map;

/**
 * 消费模块索引优化服务接口
 * <p>
 * 定义索引优化相关的业务逻辑接口，遵循四层架构规范
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public interface IIndexOptimizationService {

    /**
     * 获取数据库索引优化建议
     *
     * @param form 查询表单
     * @return 索引优化建议分页结果
     */
    PageResult<?> getIndexOptimizationSuggestions();

    /**
     * 分析数据库索引性能
     *
     * @return 索引性能分析结果
     */
    ResponseDTO<Map<String, Object>> analyzeIndexPerformance();

    /**
     * 获取缺失的索引建议
     *
     * @return 缺失索引建议列表
     */
    ResponseDTO<?> getMissingIndexSuggestions();

    /**
     * 获取冗余索引建议
     *
     * @return 冗余索引建议列表
     */
    ResponseDTO<?> getRedundantIndexSuggestions();

    /**
     * 异步执行索引优化分析
     *
     * @return 异步任务ID
     */
    ResponseDTO<String> executeAsyncIndexAnalysis();

    /**
     * 获取索引优化统计信息
     *
     * @return 索引优化统计信息
     */
    ResponseDTO<Map<String, Object>> getIndexOptimizationStatistics();

    /**
     * 验证索引优化建议的有效性
     *
     * @param form 索引优化表单
     * @return 验证结果
     */
    ResponseDTO<Boolean> validateIndexOptimization();

    /**
     * 验证索引优化效果（Task 1.9.4）
     *
     * @return 索引效果验证结果，包含性能提升数据
     */
    ResponseDTO<Map<String, Object>> verifyIndexOptimizationEffect();

    /**
     * 获取索引优化历史记录
     *
     * @param form 查询表单
     * @return 索引优化历史记录
     */
    PageResult<?> getIndexOptimizationHistory();
}