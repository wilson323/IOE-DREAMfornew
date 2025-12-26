package net.lab1024.sa.video.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.constant.WebSocketConstants;
import net.lab1024.sa.video.domain.dto.EdgeAIEventDTO;

/**
 * 事件缓存管理器
 * <p>
 * 管理WebSocket事件的本地缓存，用于：
 * 1. 新订阅客户端的初始数据加载
 * 2. 事件历史查询
 * 3. 按设备过滤的事件查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class EventCacheManager {

    private final CacheManager cacheManager;

    /**
     * 最近事件缓存（内存）
     */
    private final ConcurrentLinkedQueue<EdgeAIEventDTO> recentEventsCache;

    /**
     * 缓存容量
     */
    private final int cacheCapacity;

    /**
     * 构造函数
     *
     * @param cacheManager Spring CacheManager
     */
    public EventCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.cacheCapacity = WebSocketConstants.MAX_EVENT_CACHE_SIZE;
        this.recentEventsCache = new ConcurrentLinkedQueue<>();
        log.info("[事件缓存] 初始化事件缓存管理器: cacheCapacity={}", cacheCapacity);
    }

    /**
     * 添加事件到缓存
     *
     * @param event AI事件
     */
    public void addEvent(EdgeAIEventDTO event) {
        if (event == null) {
            return;
        }

        // 添加到内存缓存
        recentEventsCache.offer(event);

        // 限制缓存大小
        while (recentEventsCache.size() > cacheCapacity) {
            recentEventsCache.poll();
        }

        // 更新Spring Cache（供查询使用）
        Cache videoEventsCache = cacheManager.getCache("video-events");
        if (videoEventsCache != null) {
            videoEventsCache.put("recent-events", getRecentEvents(WebSocketConstants.DEFAULT_RECENT_EVENT_COUNT));
        }

        // 按设备缓存
        if (event.getDeviceId() != null) {
            Cache deviceEventsCache = cacheManager.getCache("device-events");
            if (deviceEventsCache != null) {
                List<EdgeAIEventDTO> deviceEvents = getDeviceEvents(event.getDeviceId(), 50);
                deviceEventsCache.put("device:" + event.getDeviceId(), deviceEvents);
            }
        }

        log.debug("[事件缓存] 事件已缓存: eventId={}, eventType={}, deviceId={}",
                event.getEventId(), event.getEventType(), event.getDeviceId());
    }

    /**
     * 获取最近的事件
     *
     * @param count 数量
     * @return 事件列表（按时间倒序）
     */
    public List<EdgeAIEventDTO> getRecentEvents(int count) {
        List<EdgeAIEventDTO> events = new ArrayList<>(recentEventsCache);

        // 按时间倒序排序（最新的在前）
        events.sort((a, b) -> {
            if (a.getEventTime() == null && b.getEventTime() == null) {
                return 0;
            }
            if (a.getEventTime() == null) {
                return 1;
            }
            if (b.getEventTime() == null) {
                return -1;
            }
            return b.getEventTime().compareTo(a.getEventTime());
        });

        // 限制返回数量
        if (events.size() > count) {
            events = events.subList(0, count);
        }

        return events;
    }

    /**
     * 获取指定设备的最近事件
     *
     * @param deviceId 设备ID
     * @param count    数量
     * @return 事件列表
     */
    public List<EdgeAIEventDTO> getDeviceEvents(String deviceId, int count) {
        List<EdgeAIEventDTO> deviceEvents = new ArrayList<>();

        for (EdgeAIEventDTO event : recentEventsCache) {
            if (deviceId.equals(event.getDeviceId())) {
                deviceEvents.add(event);
            }
        }

        // 按时间倒序排序
        deviceEvents.sort((a, b) -> {
            if (a.getEventTime() == null && b.getEventTime() == null) {
                return 0;
            }
            if (a.getEventTime() == null) {
                return 1;
            }
            if (b.getEventTime() == null) {
                return -1;
            }
            return b.getEventTime().compareTo(a.getEventTime());
        });

        // 限制返回数量
        if (deviceEvents.size() > count) {
            deviceEvents = deviceEvents.subList(0, count);
        }

        return deviceEvents;
    }

    /**
     * 获取指定时间范围的事件
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 事件列表
     */
    public List<EdgeAIEventDTO> getEventsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<EdgeAIEventDTO> events = new ArrayList<>();

        for (EdgeAIEventDTO event : recentEventsCache) {
            if (event.getEventTime() == null) {
                continue;
            }

            boolean isAfterStart = startTime == null || !event.getEventTime().isBefore(startTime);
            boolean isBeforeEnd = endTime == null || !event.getEventTime().isAfter(endTime);

            if (isAfterStart && isBeforeEnd) {
                events.add(event);
            }
        }

        return events;
    }

    /**
     * 获取指定类型的事件
     *
     * @param eventType 事件类型
     * @param count     数量
     * @return 事件列表
     */
    public List<EdgeAIEventDTO> getEventsByType(String eventType, int count) {
        List<EdgeAIEventDTO> events = new ArrayList<>();

        for (EdgeAIEventDTO event : recentEventsCache) {
            if (eventType.equals(event.getEventType())) {
                events.add(event);
            }
        }

        // 按时间倒序排序
        events.sort((a, b) -> {
            if (a.getEventTime() == null && b.getEventTime() == null) {
                return 0;
            }
            if (a.getEventTime() == null) {
                return 1;
            }
            if (b.getEventTime() == null) {
                return -1;
            }
            return b.getEventTime().compareTo(a.getEventTime());
        });

        // 限制返回数量
        if (events.size() > count) {
            events = events.subList(0, count);
        }

        return events;
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        recentEventsCache.clear();

        Cache videoEventsCache = cacheManager.getCache("video-events");
        if (videoEventsCache != null) {
            videoEventsCache.clear();
        }

        Cache deviceEventsCache = cacheManager.getCache("device-events");
        if (deviceEventsCache != null) {
            deviceEventsCache.clear();
        }

        log.info("[事件缓存] 缓存已清空");
    }

    /**
     * 获取缓存大小
     *
     * @return 当前缓存的事件数量
     */
    public int getCacheSize() {
        return recentEventsCache.size();
    }
}
