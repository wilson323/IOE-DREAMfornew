package net.lab1024.sa.video.service;

import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.video.domain.form.AIEventAddForm;
import net.lab1024.sa.video.domain.form.AIEventQueryForm;
import net.lab1024.sa.video.domain.vo.AIEventVO;
import net.lab1024.sa.video.domain.vo.AIEventStatisticsVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI事件服务接口
 * <p>
 * 定义AI智能分析事件管理的业务接口：
 * 1. 事件CRUD操作
 * 2. 事件查询和统计
 * 3. 事件处理流程
 * 4. 批量操作支持
 * 5. 异步处理能力
 * 6. 实时数据获取
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AIEventService {

    /**
     * 分页查询AI事件列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<AIEventVO> pageQueryAIEvents(AIEventQueryForm queryForm);

    /**
     * 获取AI事件详情
     *
     * @param eventId 事件ID
     * @return 事件详情
     */
    AIEventVO getAIEventDetail(Long eventId);

    /**
     * 创建AI事件
     *
     * @param addForm 创建表单
     * @return 事件ID
     */
    Long createAIEvent(AIEventAddForm addForm);

    /**
     * 处理AI事件
     *
     * @param eventId 事件ID
     * @param processResult 处理结果
     * @return 是否成功
     */
    boolean processAIEvent(Long eventId, String processResult);

    /**
     * 批量处理AI事件
     *
     * @param eventIds 事件ID列表
     * @param processResult 处理结果
     * @return 处理结果映射
     */
    Map<Long, Boolean> batchProcessAIEvents(List<Long> eventIds, String processResult);

    /**
     * 获取AI事件统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    AIEventStatisticsVO getAIEventStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取实时AI事件列表
     *
     * @param eventType 事件类型
     * @param limit 限制数量
     * @return 实时事件列表
     */
    List<AIEventVO> getRealtimeAIEvents(String eventType, int limit);

    /**
     * 获取高优先级AI事件
     *
     * @param minPriority 最小优先级
     * @return 高优先级事件列表
     */
    List<AIEventVO> getHighPriorityAIEvents(int minPriority);

    /**
     * 异步处理AI事件
     *
     * @param eventId 事件ID
     * @param processResult 处理结果
     * @return 异步处理结果
     */
    CompletableFuture<Boolean> asyncProcessAIEvent(Long eventId, String processResult);

    /**
     * 获取事件趋势分析
     *
     * @param hours 时间范围(小时)
     * @param eventType 事件类型
     * @return 趋势分析数据
     */
    Map<String, Object> getEventTrendAnalysis(int hours, String eventType);

    /**
     * 获取设备AI事件统计
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 设备事件统计
     */
    Map<String, Object> getDeviceAIEventStatistics(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除AI事件
     *
     * @param eventId 事件ID
     * @return 是否成功
     */
    boolean deleteAIEvent(Long eventId);

    /**
     * 批量删除AI事件
     *
     * @param eventIds 事件ID列表
     * @return 删除结果映射
     */
    Map<Long, Boolean> batchDeleteAIEvents(List<Long> eventIds);

    /**
     * 获取未处理AI事件数量
     *
     * @return 未处理事件数量统计
     */
    Map<String, Long> getUnprocessedAIEventCount();

    /**
     * 重新分析AI事件
     *
     * @param eventId 事件ID
     * @return 重新分析结果
     */
    CompletableFuture<String> reanalyzeAIEvent(Long eventId);

    /**
     * 根据设备ID获取AI事件列表
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 事件列表
     */
    List<AIEventVO> getAIEventsByDevice(Long deviceId, int limit);

    /**
     * 根据事件类型获取AI事件列表
     *
     * @param eventType 事件类型
     * @param limit 限制数量
     * @return 事件列表
     */
    List<AIEventVO> getAIEventsByType(String eventType, int limit);

    /**
     * 获取AI事件处理历史
     *
     * @param eventId 事件ID
     * @return 处理历史记录
     */
    List<Map<String, Object>> getAIEventProcessHistory(Long eventId);

    /**
     * 更新AI事件状态
     *
     * @param eventId 事件ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateAIEventStatus(Long eventId, Integer status);

    /**
     * 设置AI事件优先级
     *
     * @param eventId 事件ID
     * @param priority 优先级
     * @return 是否成功
     */
    boolean updateAIEventPriority(Long eventId, Integer priority);

    /**
     * 获取AI事件处理建议
     *
     * @param eventId 事件ID
     * @return 处理建议
     */
    List<String> getAIEventProcessSuggestions(Long eventId);

    /**
     * 导出AI事件数据
     *
     * @param queryForm 查询条件
     * @return 导出数据
     */
    byte[] exportAIEvents(AIEventQueryForm queryForm);

    /**
     * 根据时间范围清理过期AI事件
     *
     * @param cutoffTime 截止时间
     * @return 清理数量
     */
    int cleanupExpiredAIEvents(LocalDateTime cutoffTime);
}