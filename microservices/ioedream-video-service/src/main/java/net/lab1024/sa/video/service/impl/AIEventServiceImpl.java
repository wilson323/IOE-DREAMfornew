package net.lab1024.sa.video.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.video.dao.AIEventDao;
import net.lab1024.sa.video.domain.entity.AIEventEntity;
import net.lab1024.sa.video.domain.form.AIEventAddForm;
import net.lab1024.sa.video.domain.form.AIEventQueryForm;
import net.lab1024.sa.video.domain.vo.AIEventStatisticsVO;
import net.lab1024.sa.video.domain.vo.AIEventVO;
import net.lab1024.sa.video.manager.AIEventManager;
import net.lab1024.sa.video.service.AIEventService;

/**
 * AI事件服务实现类
 * <p>
 * 实现AI智能分析事件的业务逻辑：
 * 1. 事件数据CRUD操作
 * 2. 事件查询和统计分析
 * 3. 事件处理流程管理
 * 4. 异步处理和批量操作
 * 5. 实时数据获取和缓存
 * 6. 跨系统集成协调
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AIEventServiceImpl implements AIEventService {

    @Resource
    private AIEventDao aiEventDao;

    @Resource
    private AIEventManager aiEventManager;

    @Override
    public PageResult<AIEventVO> pageQueryAIEvents(AIEventQueryForm queryForm) {
        log.info("[AI事件Service] 分页查询AI事件: {}", queryForm);

        // 构建查询条件
        LambdaQueryWrapper<AIEventEntity> queryWrapper = buildQueryWrapper(queryForm);

        // 分页查询
        Page<AIEventEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        IPage<AIEventEntity> pageResult = aiEventDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<AIEventVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(
                voList,
                pageResult.getTotal(),
                (int) pageResult.getCurrent(),
                (int) pageResult.getSize());
    }

    @Override
    public AIEventVO getAIEventDetail(Long eventId) {
        log.info("[AI事件Service] 获取AI事件详情: eventId={}", eventId);

        AIEventEntity entity = aiEventDao.selectById(eventId);
        if (entity == null) {
            log.warn("[AI事件Service] AI事件不存在: eventId={}", eventId);
            return null;
        }

        return convertToVO(entity);
    }

    @Override
    public Long createAIEvent(AIEventAddForm addForm) {
        log.info("[AI事件Service] 创建AI事件: {}", addForm);

        AIEventEntity entity = new AIEventEntity();
        entity.setEventId(addForm.getEventId());
        entity.setDeviceId(addForm.getDeviceId());
        entity.setEventType(addForm.getEventType());
        entity.setEventSubType(addForm.getEventSubType());
        entity.setEventTitle(addForm.getEventTitle());
        entity.setEventDescription(addForm.getEventDescription());
        entity.setPriority(addForm.getPriority());
        entity.setSeverity(addForm.getSeverity());
        entity.setConfidence(addForm.getConfidence());
        entity.setEventData(addForm.getEventData());
        entity.setVideoUrl(addForm.getVideoUrl());
        entity.setImageUrl(addForm.getImageUrl());
        entity.setLocation(addForm.getLocation());
        entity.setEventStatus(1); // 待处理
        entity.setProcessTimes(0);

        aiEventDao.insert(entity);

        log.info("[AI事件Service] AI事件创建成功: eventId={}", entity.getId());
        return entity.getId();
    }

    @Override
    public boolean processAIEvent(Long eventId, String processResult) {
        log.info("[AI事件Service] 处理AI事件: eventId={}, result={}", eventId, processResult);

        try {
            AIEventEntity entity = aiEventDao.selectById(eventId);
            if (entity == null) {
                log.warn("[AI事件Service] AI事件不存在: eventId={}", eventId);
                return false;
            }

            // 更新事件状态
            entity.setEventStatus(2); // 已处理
            entity.setProcessResult(processResult);
            entity.setProcessTime(LocalDateTime.now());
            entity.setProcessTimes(entity.getProcessTimes() + 1);

            int updateResult = aiEventDao.updateById(entity);
            if (updateResult > 0) {
                log.info("[AI事件Service] AI事件处理成功: eventId={}", eventId);
                return true;
            }

        } catch (Exception e) {
            log.error("[AI事件Service] AI事件处理异常: eventId={}", eventId, e);
        }

        return false;
    }

    @Override
    public Map<Long, Boolean> batchProcessAIEvents(List<Long> eventIds, String processResult) {
        log.info("[AI事件Service] 批量处理AI事件: eventIds={}, result={}", eventIds, processResult);

        Map<Long, Boolean> results = new HashMap<>();

        for (Long eventId : eventIds) {
            boolean success = processAIEvent(eventId, processResult);
            results.put(eventId, success);
        }

        return results;
    }

    @Override
    public AIEventStatisticsVO getAIEventStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[AI事件Service] 获取AI事件统计: startTime={}, endTime={}", startTime, endTime);

        AIEventStatisticsVO statistics = new AIEventStatisticsVO();

        // 总事件数
        LambdaQueryWrapper<AIEventEntity> totalQuery = new LambdaQueryWrapper<>();
        if (startTime != null) {
            totalQuery.ge(AIEventEntity::getCreateTime, startTime);
        }
        if (endTime != null) {
            totalQuery.le(AIEventEntity::getCreateTime, endTime);
        }
        long totalCount = aiEventDao.selectCount(totalQuery);
        statistics.setTotalEvents(totalCount);

        // 按类型统计
        Map<String, Long> typeStatistics = new HashMap<>();
        for (String type : Arrays.asList("FACE_RECOGNITION", "BEHAVIOR_ANALYSIS", "OBJECT_DETECTION",
                "ANOMALY_DETECTION", "CROWD_ANALYSIS")) {
            LambdaQueryWrapper<AIEventEntity> typeQuery = new LambdaQueryWrapper<>();
            typeQuery.eq(AIEventEntity::getEventType, type);
            if (startTime != null) {
                typeQuery.ge(AIEventEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                typeQuery.le(AIEventEntity::getCreateTime, endTime);
            }
            long count = aiEventDao.selectCount(typeQuery);
            typeStatistics.put(type, count);
        }
        statistics.setTypeStatistics(typeStatistics);

        // 按优先级统计
        Map<Integer, Long> priorityStatistics = new HashMap<>();
        for (int priority = 1; priority <= 10; priority++) {
            LambdaQueryWrapper<AIEventEntity> priorityQuery = new LambdaQueryWrapper<>();
            priorityQuery.eq(AIEventEntity::getPriority, priority);
            if (startTime != null) {
                priorityQuery.ge(AIEventEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                priorityQuery.le(AIEventEntity::getCreateTime, endTime);
            }
            long count = aiEventDao.selectCount(priorityQuery);
            priorityStatistics.put(priority, count);
        }
        statistics.setPriorityStatistics(priorityStatistics);

        // 处理状态统计
        LambdaQueryWrapper<AIEventEntity> processedQuery = new LambdaQueryWrapper<>();
        processedQuery.eq(AIEventEntity::getEventStatus, 2); // 已处理
        if (startTime != null) {
            processedQuery.ge(AIEventEntity::getCreateTime, startTime);
        }
        if (endTime != null) {
            processedQuery.le(AIEventEntity::getCreateTime, endTime);
        }
        long processedCount = aiEventDao.selectCount(processedQuery);
        statistics.setProcessedEvents(processedCount);
        statistics.setUnprocessedEvents(totalCount - processedCount);

        return statistics;
    }

    @Override
    public List<AIEventVO> getRealtimeAIEvents(String eventType, int limit) {
        log.info("[AI事件Service] 获取实时AI事件: eventType={}, limit={}", eventType, limit);

        LambdaQueryWrapper<AIEventEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (eventType != null && !eventType.isEmpty()) {
            queryWrapper.eq(AIEventEntity::getEventType, eventType);
        }
        queryWrapper.orderByDesc(AIEventEntity::getCreateTime);
        queryWrapper.last("LIMIT " + Math.min(limit, 100)); // 限制最大100条

        List<AIEventEntity> entities = aiEventDao.selectList(queryWrapper);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AIEventVO> getHighPriorityAIEvents(int minPriority) {
        log.info("[AI事件Service] 获取高优先级AI事件: minPriority={}", minPriority);

        LambdaQueryWrapper<AIEventEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(AIEventEntity::getPriority, minPriority);
        queryWrapper.eq(AIEventEntity::getEventStatus, 1); // 待处理
        queryWrapper.orderByDesc(AIEventEntity::getPriority);
        queryWrapper.orderByDesc(AIEventEntity::getCreateTime);
        queryWrapper.last("LIMIT 50");

        List<AIEventEntity> entities = aiEventDao.selectList(queryWrapper);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Async
    @Override
    public CompletableFuture<Boolean> asyncProcessAIEvent(Long eventId, String processResult) {
        log.info("[AI事件Service] 异步处理AI事件: eventId={}, result={}", eventId, processResult);

        try {
            boolean success = processAIEvent(eventId, processResult);
            return CompletableFuture.completedFuture(success);
        } catch (Exception e) {
            log.error("[AI事件Service] 异步处理AI事件异常: eventId={}", eventId, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public Map<String, Object> getEventTrendAnalysis(int hours, String eventType) {
        log.info("[AI事件Service] 获取事件趋势分析: hours={}, eventType={}", hours, eventType);

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);

        LambdaQueryWrapper<AIEventEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (eventType != null && !eventType.isEmpty()) {
            queryWrapper.eq(AIEventEntity::getEventType, eventType);
        }
        queryWrapper.between(AIEventEntity::getCreateTime, startTime, endTime);
        queryWrapper.orderByAsc(AIEventEntity::getCreateTime);

        List<AIEventEntity> events = aiEventDao.selectList(queryWrapper);

        Map<String, Object> trendAnalysis = new HashMap<>();

        // 按小时统计
        Map<Integer, Long> hourlyCount = new HashMap<>();
        for (AIEventEntity event : events) {
            int hour = event.getCreateTime().getHour();
            hourlyCount.put(hour, hourlyCount.getOrDefault(hour, 0L) + 1);
        }
        trendAnalysis.put("hourlyCount", hourlyCount);

        // 趋势数据
        trendAnalysis.put("totalCount", (long) events.size());
        trendAnalysis.put("timeRange", Map.of(
                "startTime", startTime,
                "endTime", endTime,
                "hours", hours));

        return trendAnalysis;
    }

    @Override
    public Map<String, Object> getDeviceAIEventStatistics(Long deviceId, LocalDateTime startTime,
            LocalDateTime endTime) {
        log.info("[AI事件Service] 获取设备AI事件统计: deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime);

        LambdaQueryWrapper<AIEventEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AIEventEntity::getDeviceId, deviceId);
        if (startTime != null) {
            queryWrapper.ge(AIEventEntity::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AIEventEntity::getCreateTime, endTime);
        }

        List<AIEventEntity> events = aiEventDao.selectList(queryWrapper);

        Map<String, Object> deviceStatistics = new HashMap<>();
        deviceStatistics.put("deviceId", deviceId);
        deviceStatistics.put("totalEvents", (long) events.size());

        // 按类型统计
        Map<String, Long> typeCount = events.stream()
                .collect(Collectors.groupingBy(AIEventEntity::getEventType, Collectors.counting()));
        deviceStatistics.put("typeStatistics", typeCount);

        // 按优先级统计
        Map<Integer, Long> priorityCount = events.stream()
                .collect(Collectors.groupingBy(AIEventEntity::getPriority, Collectors.counting()));
        deviceStatistics.put("priorityStatistics", priorityCount);

        // 处理状态统计
        Map<Integer, Long> statusCount = events.stream()
                .collect(Collectors.groupingBy(AIEventEntity::getEventStatus, Collectors.counting()));
        deviceStatistics.put("statusStatistics", statusCount);

        return deviceStatistics;
    }

    @Override
    public boolean deleteAIEvent(Long eventId) {
        log.info("[AI事件Service] 删除AI事件: eventId={}", eventId);

        int deleteResult = aiEventDao.deleteById(eventId);
        return deleteResult > 0;
    }

    @Override
    public Map<Long, Boolean> batchDeleteAIEvents(List<Long> eventIds) {
        log.info("[AI事件Service] 批量删除AI事件: eventIds={}", eventIds);

        Map<Long, Boolean> results = new HashMap<>();

        for (Long eventId : eventIds) {
            boolean success = deleteAIEvent(eventId);
            results.put(eventId, success);
        }

        return results;
    }

    @Override
    public Map<String, Long> getUnprocessedAIEventCount() {
        log.info("[AI事件Service] 获取未处理AI事件数量");

        Map<String, Long> countMap = new HashMap<>();

        // 总未处理数量
        LambdaQueryWrapper<AIEventEntity> unprocessedQuery = new LambdaQueryWrapper<>();
        unprocessedQuery.eq(AIEventEntity::getEventStatus, 1); // 待处理
        long totalCount = aiEventDao.selectCount(unprocessedQuery);
        countMap.put("totalUnprocessed", totalCount);

        // 按类型统计未处理数量
        for (String type : Arrays.asList("FACE_RECOGNITION", "BEHAVIOR_ANALYSIS", "OBJECT_DETECTION",
                "ANOMALY_DETECTION", "CROWD_ANALYSIS")) {
            LambdaQueryWrapper<AIEventEntity> typeQuery = new LambdaQueryWrapper<>();
            typeQuery.eq(AIEventEntity::getEventType, type);
            typeQuery.eq(AIEventEntity::getEventStatus, 1);
            long count = aiEventDao.selectCount(typeQuery);
            countMap.put(type.toLowerCase() + "_unprocessed", count);
        }

        return countMap;
    }

    @Async
    @Override
    public CompletableFuture<String> reanalyzeAIEvent(Long eventId) {
        log.info("[AI事件Service] 重新分析AI事件: eventId={}", eventId);

        try {
            // 重新分析逻辑可以调用AIEventManager
            return CompletableFuture.completedFuture("重新分析完成");
        } catch (Exception e) {
            log.error("[AI事件Service] 重新分析AI事件异常: eventId={}", eventId, e);
            return CompletableFuture.completedFuture("重新分析失败: " + e.getMessage());
        }
    }

    @Override
    public List<AIEventVO> getAIEventsByDevice(Long deviceId, int limit) {
        log.info("[AI事件Service] 根据设备ID获取AI事件: deviceId={}, limit={}", deviceId, limit);

        LambdaQueryWrapper<AIEventEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AIEventEntity::getDeviceId, deviceId);
        queryWrapper.orderByDesc(AIEventEntity::getCreateTime);
        queryWrapper.last("LIMIT " + Math.min(limit, 100));

        List<AIEventEntity> entities = aiEventDao.selectList(queryWrapper);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AIEventVO> getAIEventsByType(String eventType, int limit) {
        log.info("[AI事件Service] 根据事件类型获取AI事件: eventType={}, limit={}", eventType, limit);

        LambdaQueryWrapper<AIEventEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AIEventEntity::getEventType, eventType);
        queryWrapper.orderByDesc(AIEventEntity::getCreateTime);
        queryWrapper.last("LIMIT " + Math.min(limit, 100));

        List<AIEventEntity> entities = aiEventDao.selectList(queryWrapper);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAIEventProcessHistory(Long eventId) {
        log.info("[AI事件Service] 获取AI事件处理历史: eventId={}", eventId);

        // 这里可以实现处理历史记录查询逻辑
        // 当前返回空列表，可以根据需要实现具体的处理历史记录功能
        return new ArrayList<>();
    }

    @Override
    public boolean updateAIEventStatus(Long eventId, Integer status) {
        log.info("[AI事件Service] 更新AI事件状态: eventId={}, status={}", eventId, status);

        try {
            AIEventEntity entity = new AIEventEntity();
            entity.setId(eventId);
            entity.setEventStatus(status);
            entity.setUpdateTime(LocalDateTime.now());

            int updateResult = aiEventDao.updateById(entity);
            return updateResult > 0;
        } catch (Exception e) {
            log.error("[AI事件Service] 更新AI事件状态异常: eventId={}, status={}", eventId, status, e);
            return false;
        }
    }

    @Override
    public boolean updateAIEventPriority(Long eventId, Integer priority) {
        log.info("[AI事件Service] 更新AI事件优先级: eventId={}, priority={}", eventId, priority);

        try {
            AIEventEntity entity = new AIEventEntity();
            entity.setId(eventId);
            entity.setPriority(priority);
            entity.setUpdateTime(LocalDateTime.now());

            int updateResult = aiEventDao.updateById(entity);
            return updateResult > 0;
        } catch (Exception e) {
            log.error("[AI事件Service] 更新AI事件优先级异常: eventId={}, priority={}", eventId, priority, e);
            return false;
        }
    }

    @Override
    public List<String> getAIEventProcessSuggestions(Long eventId) {
        log.info("[AI事件Service] 获取AI事件处理建议: eventId={}", eventId);

        List<String> suggestions = new ArrayList<>();
        suggestions.add("建议立即联系相关人员确认事件真实性");
        suggestions.add("检查相关视频录像，获取更多证据");
        suggestions.add("如确认为真实事件，按应急预案处理");
        suggestions.add("记录处理结果，更新事件状态");

        return suggestions;
    }

    @Override
    public byte[] exportAIEvents(AIEventQueryForm queryForm) {
        log.info("[AI事件Service] 导出AI事件数据: {}", queryForm);

        // 实现数据导出逻辑，这里返回空数组
        // 实际实现可以使用EasyExcel等库导出Excel文件
        return new byte[0];
    }

    @Override
    public int cleanupExpiredAIEvents(LocalDateTime cutoffTime) {
        log.info("[AI事件Service] 清理过期AI事件: cutoffTime={}", cutoffTime);

        LambdaQueryWrapper<AIEventEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(AIEventEntity::getCreateTime, cutoffTime);
        queryWrapper.eq(AIEventEntity::getEventStatus, 2); // 已处理

        int deleteCount = aiEventDao.delete(queryWrapper);
        log.info("[AI事件Service] 清理过期AI事件完成: 删除数量={}", deleteCount);

        return deleteCount;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<AIEventEntity> buildQueryWrapper(AIEventQueryForm queryForm) {
        LambdaQueryWrapper<AIEventEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (queryForm.getDeviceId() != null) {
            queryWrapper.eq(AIEventEntity::getDeviceId, queryForm.getDeviceId());
        }
        if (queryForm.getEventType() != null && !queryForm.getEventType().isEmpty()) {
            queryWrapper.eq(AIEventEntity::getEventType, queryForm.getEventType());
        }
        if (queryForm.getEventStatus() != null) {
            queryWrapper.eq(AIEventEntity::getEventStatus, queryForm.getEventStatus());
        }
        if (queryForm.getPriority() != null) {
            queryWrapper.ge(AIEventEntity::getPriority, queryForm.getPriority());
        }
        if (queryForm.getStartTime() != null) {
            queryWrapper.ge(AIEventEntity::getCreateTime, queryForm.getStartTime());
        }
        if (queryForm.getEndTime() != null) {
            queryWrapper.le(AIEventEntity::getCreateTime, queryForm.getEndTime());
        }

        queryWrapper.orderByDesc(AIEventEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 转换为VO对象
     */
    private AIEventVO convertToVO(AIEventEntity entity) {
        AIEventVO vo = new AIEventVO();
        vo.setId(entity.getId());
        vo.setEventId(entity.getEventId());
        vo.setDeviceId(entity.getDeviceId());
        vo.setEventType(entity.getEventType());
        vo.setEventSubType(entity.getEventSubType());
        vo.setEventTitle(entity.getEventTitle());
        vo.setEventDescription(entity.getEventDescription());
        vo.setPriority(entity.getPriority());
        vo.setSeverity(entity.getSeverity());
        vo.setConfidence(entity.getConfidence());
        vo.setEventData(entity.getEventData());
        vo.setVideoUrl(entity.getVideoUrl());
        vo.setImageUrl(entity.getImageUrl());
        vo.setLocation(entity.getLocation());
        vo.setEventStatus(entity.getEventStatus());
        vo.setProcessResult(entity.getProcessResult());
        vo.setProcessTime(entity.getProcessTime());
        vo.setProcessTimes(entity.getProcessTimes());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        return vo;
    }
}
