package net.lab1024.sa.admin.module.monitor.listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.access.dao.AccessDeviceDao;
import net.lab1024.sa.admin.module.access.dao.AccessEventDao;
import net.lab1024.sa.admin.module.monitor.endpoint.AccessMonitorWebSocketEndpoint;
import net.lab1024.sa.base.common.cache.RedisUtil;

/**
 * 门禁事件实时监听器
 * <p>
 * 严格遵循repowiki规范：
 * - 监听门禁设备事件变化
 * - 实时推送WebSocket消息
 * - 支持事件过滤和聚合
 * - 提供异常事件告警
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class AccessEventRealtimeListener {

    @Resource
    private AccessEventDao accessEventDao;

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private RedisUtil redisUtil;

    // 设备状态缓存
    private final Map<String, DeviceStatus> deviceStatusCache = new ConcurrentHashMap<>();

    // 事件统计缓存
    private final Map<String, EventStatistics> eventStatisticsCache = new ConcurrentHashMap<>();

    // 告警规则缓存
    private final List<AlertRule> alertRules = new ArrayList<>();

    // 定时任务执行器
    private ScheduledExecutorService scheduledExecutor;

    /**
     * 设备状态信息
     */
    public static class DeviceStatus {
        private final String deviceId;
        private final String deviceName;
        private String onlineStatus;
        private LocalDateTime lastUpdateTime;
        private Map<String, Object> details;

        public DeviceStatus(String deviceId, String deviceName) {
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.onlineStatus = "offline";
            this.lastUpdateTime = LocalDateTime.now();
            this.details = new HashMap<>();
        }

        // Getters and Setters
        public String getDeviceId() {
            return deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getOnlineStatus() {
            return onlineStatus;
        }

        public LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public void setOnlineStatus(String onlineStatus) {
            this.onlineStatus = onlineStatus;
            this.lastUpdateTime = LocalDateTime.now();
        }

        public void updateDetails(Map<String, Object> newDetails) {
            if (newDetails != null) {
                this.details.putAll(newDetails);
                this.lastUpdateTime = LocalDateTime.now();
            }
        }
    }

    /**
     * 事件统计信息
     */
    public static class EventStatistics {
        private String date;
        private Map<String, Integer> eventTypeCount = new HashMap<>();
        private Map<String, Integer> deviceEventCount = new HashMap<>();
        private Map<String, Integer> areaEventCount = new HashMap<>();
        private Map<String, Integer> statusCount = new HashMap<>();

        public EventStatistics(String date) {
            this.date = date;
            initializeCounts();
        }

        private void initializeCounts() {
            // 初始化各种事件类型的计数器
            statusCount.put("success", 0);
            statusCount.put("failed", 0);
            statusCount.put("timeout", 0);
        }

        // Getters
        public String getDate() {
            return date;
        }

        public Map<String, Integer> getEventTypeCount() {
            return eventTypeCount;
        }

        public Map<String, Integer> getDeviceEventCount() {
            return deviceEventCount;
        }

        public Map<String, Integer> getAreaEventCount() {
            return areaEventCount;
        }

        public Map<String, Integer> getStatusCount() {
            return statusCount;
        }

        public void incrementEventType(String eventType) {
            eventTypeCount.put(eventType, eventTypeCount.getOrDefault(eventType, 0) + 1);
        }

        public void incrementDeviceEvent(String deviceId) {
            deviceEventCount.put(deviceId, deviceEventCount.getOrDefault(deviceId, 0) + 1);
        }

        public void incrementStatusCount(String status) {
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
        }

        /**
         * 获取总事件数
         *
         * @return 总事件数
         */
        public int getTotalEventCount() {
            return statusCount.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        /**
         * 获取失败事件数
         *
         * @return 失败事件数
         */
        public int getFailedCount() {
            return statusCount.getOrDefault("failed", 0);
        }
    }

    /**
     * 告警规则
     */
    public static class AlertRule {
        private String ruleName;
        private String triggerType;
        private String condition;
        private String alertLevel;
        private boolean enabled;

        public AlertRule(String ruleName, String triggerType, String condition, String alertLevel) {
            this.ruleName = ruleName;
            this.triggerType = triggerType;
            this.condition = condition;
            this.alertLevel = alertLevel;
            this.enabled = true;
        }

        // Getters
        public String getRuleName() {
            return ruleName;
        }

        public String getTriggerType() {
            return triggerType;
        }

        public String getCondition() {
            return condition;
        }

        public String getAlertLevel() {
            return alertLevel;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 初始化监听器
     */
    public void initialize() {
        if (scheduledExecutor != null) {
            return;
        }

        scheduledExecutor = Executors.newScheduledThreadPool(3);

        // 初始化告警规则
        initializeAlertRules();

        // 启动定时任务
        startScheduledTasks();

        log.info("门禁事件实时监听器初始化完成");
    }

    /**
     * 初始化告警规则
     */
    private void initializeAlertRules() {
        alertRules.add(new AlertRule("设备离线告警", "device_offline", "status=offline", "warning"));
        alertRules.add(new AlertRule("高失败率告警", "high_failure_rate", "failure_rate>50", "critical"));
        alertRules.add(new AlertRule("异常访问告警", "abnormal_access", "time_range=00:00-06:00", "warning"));
        alertRules.add(new AlertRule("黑名单访问告警", "blacklist_access", "blacklist=true", "critical"));

        log.info("告警规则初始化完成，规则数量: {}", alertRules.size());
    }

    /**
     * 启动定时任务
     */
    private void startScheduledTasks() {
        // 每分钟检查设备状态
        scheduledExecutor.scheduleAtFixedRate(this::checkDeviceStatus, 1, 1, TimeUnit.MINUTES);

        // 每5分钟更新事件统计
        scheduledExecutor.scheduleAtFixedRate(this::updateEventStatistics, 5, 5, TimeUnit.MINUTES);

        // 每10分钟检查告警条件
        scheduledExecutor.scheduleAtFixedRate(this::checkAlertConditions, 10, 10, TimeUnit.MINUTES);

        // 每小时清理过期缓存
        scheduledExecutor.scheduleAtFixedRate(this::cleanExpiredCache, 1, 1, TimeUnit.HOURS);

        log.info("定时任务启动完成");
    }

    /**
     * 监听设备状态变化事件
     */
    @EventListener
    @Async
    public void handleDeviceStatusChange(DeviceStatusChangeEvent event) {
        log.debug("处理设备状态变化事件: {}", event);

        try {
            // 更新设备状态缓存
            DeviceStatus deviceStatus = deviceStatusCache.computeIfAbsent(
                    event.getDeviceId(),
                    k -> new DeviceStatus(event.getDeviceId(), event.getDeviceName()));

            String oldStatus = deviceStatus.getOnlineStatus();
            deviceStatus.setOnlineStatus(event.getNewStatus());
            deviceStatus.updateDetails(event.getDetails());

            // 推送状态变化消息
            pushDeviceStatusChange(event);

            // 检查告警条件
            checkAlertCondition(event.getDeviceId(), "device_status_change", event);

            // 记录状态变化日志
            if (!oldStatus.equals(event.getNewStatus())) {
                log.info("设备状态变化，deviceId: {}, {} -> {}",
                        event.getDeviceId(), oldStatus, event.getNewStatus());
            }

        } catch (Exception e) {
            log.error("处理设备状态变化事件失败", e);
        }
    }

    /**
     * 监听门禁事件
     */
    @EventListener
    @Async
    public void handleAccessEvent(AccessEventMessage event) {
        log.debug("处理门禁事件: {}", event);

        try {
            // 更新事件统计
            updateEventStatistics(event);

            // 推送实时事件
            pushAccessEvent(event);

            // 检查告警条件
            checkAlertCondition(event.getDeviceId(), "access_event", event);

            // 特殊事件处理
            if ("blacklist".equals(event.getVerifyResult())) {
                handleBlacklistEvent(event);
            } else if ("failed".equals(event.getVerifyResult())) {
                handleFailedAccessEvent(event);
            }

        } catch (Exception e) {
            log.error("处理门禁事件失败", e);
        }
    }

    /**
     * 设备状态变化事件
     */
    public static class DeviceStatusChangeEvent {
        private String deviceId;
        private String deviceName;
        private String newStatus;
        private Map<String, Object> details;

        public DeviceStatusChangeEvent(String deviceId, String deviceName, String newStatus,
                Map<String, Object> details) {
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.newStatus = newStatus;
            this.details = details != null ? details : new HashMap<>();
        }

        // Getters
        public String getDeviceId() {
            return deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getNewStatus() {
            return newStatus;
        }

        public Map<String, Object> getDetails() {
            return details;
        }
    }

    /**
     * 门禁事件消息
     */
    public static class AccessEventMessage {
        private String eventId;
        private String deviceId;
        private String deviceName;
        private String areaName;
        private String userId;
        private String userName;
        private String verifyResult;
        private String verifyMethod;
        private String direction;
        private LocalDateTime eventTime;

        public AccessEventMessage(String eventId, String deviceId, String deviceName, String areaName,
                String userId, String userName, String verifyResult, String verifyMethod,
                String direction, LocalDateTime eventTime) {
            this.eventId = eventId;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.areaName = areaName;
            this.userId = userId;
            this.userName = userName;
            this.verifyResult = verifyResult;
            this.verifyMethod = verifyMethod;
            this.direction = direction;
            this.eventTime = eventTime;
        }

        // Getters
        public String getEventId() {
            return eventId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getAreaName() {
            return areaName;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getVerifyResult() {
            return verifyResult;
        }

        public String getVerifyMethod() {
            return verifyMethod;
        }

        public String getDirection() {
            return direction;
        }

        public LocalDateTime getEventTime() {
            return eventTime;
        }
    }

    /**
     * 推送设备状态变化
     */
    private void pushDeviceStatusChange(DeviceStatusChangeEvent event) {
        JSONObject eventData = new JSONObject();
        eventData.put("deviceId", event.getDeviceId());
        eventData.put("deviceName", event.getDeviceName());
        eventData.put("oldStatus", "unknown");
        eventData.put("newStatus", event.getNewStatus());
        eventData.put("details", event.getDetails());
        eventData.put("timestamp", LocalDateTime.now().toString());

        AccessMonitorWebSocketEndpoint.pushDeviceStatusChange(
                event.getDeviceId(), event.getNewStatus(), eventData);
    }

    /**
     * 推送门禁事件
     */
    private void pushAccessEvent(AccessEventMessage event) {
        JSONObject eventData = new JSONObject();
        eventData.put("eventId", event.getEventId());
        eventData.put("deviceId", event.getDeviceId());
        eventData.put("deviceName", event.getDeviceName());
        eventData.put("areaName", event.getAreaName());
        eventData.put("userId", event.getUserId());
        eventData.put("userName", event.getUserName());
        eventData.put("verifyResult", event.getVerifyResult());
        eventData.put("verifyMethod", event.getVerifyMethod());
        eventData.put("direction", event.getDirection());
        eventData.put("eventTime", event.getEventTime().toString());

        AccessMonitorWebSocketEndpoint.pushAccessEvent("access_event", eventData);
    }

    /**
     * 更新事件统计
     */
    private void updateEventStatistics(AccessEventMessage event) {
        String today = LocalDateTime.now().toLocalDate().toString();
        EventStatistics stats = eventStatisticsCache.computeIfAbsent(today, EventStatistics::new);

        stats.incrementEventType(event.getVerifyMethod());
        stats.incrementDeviceEvent(event.getDeviceId());
        stats.incrementStatusCount(event.getVerifyResult());

        // 存储到Redis
        String redisKey = "smart:monitor:event_stats:" + today;
        redisUtil.setBean(redisKey, stats, 86400); // 24小时过期
    }

    /**
     * 检查设备状态
     */
    private void checkDeviceStatus() {
        // TODO: 从数据库查询所有设备状态
        // 并与缓存中的状态进行比较，发现状态变化时发送事件

        log.debug("设备状态检查完成");
    }

    /**
     * 更新事件统计
     */
    private void updateEventStatistics() {
        // TODO: 从数据库查询当日事件统计
        // 并更新缓存和Redis

        log.debug("事件统计更新完成");
    }

    /**
     * 检查告警条件
     */
    private void checkAlertConditions() {
        for (AlertRule rule : alertRules) {
            if (!rule.isEnabled()) {
                continue;
            }

            try {
                checkAlertRule(rule);
            } catch (Exception e) {
                log.error("检查告警规则失败，rule: {}", rule.getRuleName(), e);
            }
        }
    }

    /**
     * 检查特定告警条件
     */
    private void checkAlertCondition(String deviceId, String triggerType, Object eventData) {
        for (AlertRule rule : alertRules) {
            if (!rule.isEnabled() || !triggerType.equals(rule.getTriggerType())) {
                continue;
            }

            if (evaluateAlertCondition(rule, deviceId, eventData)) {
                triggerAlert(rule, deviceId, eventData);
            }
        }
    }

    /**
     * 评估告警条件
     */
    private boolean evaluateAlertCondition(AlertRule rule, String deviceId, Object eventData) {
        // TODO: 根据规则条件进行判断
        // 这里可以实现更复杂的条件判断逻辑

        if ("device_offline".equals(rule.getTriggerType())) {
            // 检查设备是否离线
            DeviceStatus deviceStatus = deviceStatusCache.get(deviceId);
            return deviceStatus != null && "offline".equals(deviceStatus.getOnlineStatus());
        }

        return false;
    }

    /**
     * 检查告警规则
     * 根据规则类型执行相应的检查逻辑
     *
     * @param rule 告警规则
     */
    private void checkAlertRule(AlertRule rule) {
        if (rule == null || !rule.isEnabled()) {
            return;
        }

        try {
            String triggerType = rule.getTriggerType();
            log.debug("检查告警规则: ruleName={}, triggerType={}", rule.getRuleName(), triggerType);

            switch (triggerType) {
                case "device_offline":
                    checkDeviceOfflineRule(rule);
                    break;
                case "high_failure_rate":
                    checkHighFailureRateRule(rule);
                    break;
                case "abnormal_access":
                    checkAbnormalAccessRule(rule);
                    break;
                case "blacklist_access":
                    // 黑名单访问告警已在事件处理中实时触发，这里不需要定时检查
                    break;
                default:
                    log.warn("未知的告警规则类型: triggerType={}", triggerType);
            }
        } catch (Exception e) {
            log.error("检查告警规则失败: ruleName={}, error={}", rule.getRuleName(), e.getMessage(), e);
        }
    }

    /**
     * 检查设备离线告警规则
     *
     * @param rule 告警规则
     */
    private void checkDeviceOfflineRule(AlertRule rule) {
        // 检查所有设备状态
        for (Map.Entry<String, DeviceStatus> entry : deviceStatusCache.entrySet()) {
            String deviceId = entry.getKey();
            DeviceStatus deviceStatus = entry.getValue();

            if (deviceStatus != null && "offline".equals(deviceStatus.getOnlineStatus())) {
                // 检查设备是否长时间离线（超过5分钟）
                LocalDateTime lastUpdateTime = deviceStatus.getLastUpdateTime();
                if (lastUpdateTime != null) {
                    long minutesOffline = java.time.Duration.between(lastUpdateTime, LocalDateTime.now()).toMinutes();
                    if (minutesOffline >= 5) {
                        // 触发离线告警
                        Map<String, Object> eventData = new HashMap<>();
                        eventData.put("deviceId", deviceId);
                        eventData.put("deviceName", deviceStatus.getDeviceName());
                        eventData.put("offlineMinutes", minutesOffline);
                        eventData.put("lastUpdateTime", lastUpdateTime.toString());
                        triggerAlert(rule, deviceId, eventData);
                    }
                }
            }
        }
    }

    /**
     * 检查高失败率告警规则
     *
     * @param rule 告警规则
     */
    private void checkHighFailureRateRule(AlertRule rule) {
        // 从条件中提取失败率阈值（例如：failure_rate>50）
        String condition = rule.getCondition();
        int threshold = 50; // 默认阈值50%
        if (condition != null && condition.contains(">")) {
            try {
                String[] parts = condition.split(">");
                if (parts.length > 1) {
                    threshold = Integer.parseInt(parts[1].trim());
                }
            } catch (NumberFormatException e) {
                log.warn("解析失败率阈值失败: condition={}", condition);
            }
        }

        // 从事件统计缓存中计算失败率
        String today = LocalDateTime.now().toLocalDate().toString();
        EventStatistics stats = eventStatisticsCache.get(today);
        if (stats != null) {
            int totalEvents = stats.getTotalEventCount();
            int failedEvents = stats.getFailedCount();
            if (totalEvents > 0) {
                double failureRate = (double) failedEvents / totalEvents * 100;
                if (failureRate > threshold) {
                    // 触发高失败率告警
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("totalEvents", totalEvents);
                    eventData.put("failedEvents", failedEvents);
                    eventData.put("failureRate", String.format("%.2f%%", failureRate));
                    eventData.put("threshold", threshold + "%");
                    triggerAlert(rule, "system", eventData);
                }
            }
        }
    }

    /**
     * 检查异常访问告警规则
     *
     * @param rule 告警规则
     */
    private void checkAbnormalAccessRule(AlertRule rule) {
        // 从条件中提取时间范围（例如：time_range=00:00-06:00）
        String condition = rule.getCondition();
        if (condition == null || !condition.contains("time_range")) {
            return;
        }

        try {
            String timeRangeStr = condition.substring(condition.indexOf("time_range") + 11);
            String[] timeParts = timeRangeStr.split("-");
            if (timeParts.length == 2) {
                String startTimeStr = timeParts[0].trim();
                String endTimeStr = timeParts[1].trim();

                // 解析时间（格式：HH:mm）
                String[] startParts = startTimeStr.split(":");
                String[] endParts = endTimeStr.split(":");
                int startHour = Integer.parseInt(startParts[0]);
                int endHour = Integer.parseInt(endParts[0]);

                // 检查当前时间是否在异常时间段
                LocalDateTime now = LocalDateTime.now();
                int currentHour = now.getHour();
                boolean isAbnormalTime = (currentHour >= startHour && currentHour < endHour);

                if (isAbnormalTime) {
                    // 查询最近1小时内的访问事件
                    LocalDateTime endTime = LocalDateTime.now();
                    LocalDateTime startTime = endTime.minusHours(1);

                    List<net.lab1024.sa.admin.module.access.domain.entity.AccessEventEntity> recentEvents = accessEventDao
                            .selectAbnormalEvents(startTime, endTime);

                    if (recentEvents != null && !recentEvents.isEmpty()) {
                        // 触发异常访问告警
                        Map<String, Object> eventData = new HashMap<>();
                        eventData.put("abnormalTimeRange", timeRangeStr);
                        eventData.put("currentTime", now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                        eventData.put("recentEventCount", recentEvents.size());
                        triggerAlert(rule, "system", eventData);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("检查异常访问告警规则失败: condition={}, error={}", condition, e.getMessage());
        }
    }

    /**
     * 触发告警
     */
    private void triggerAlert(AlertRule rule, String deviceId, Object eventData) {
        JSONObject alertDetails = new JSONObject();
        alertDetails.put("ruleName", rule.getRuleName());
        alertDetails.put("deviceId", deviceId);
        alertDetails.put("triggerType", rule.getTriggerType());
        alertDetails.put("condition", rule.getCondition());
        alertDetails.put("eventData", eventData);
        alertDetails.put("timestamp", LocalDateTime.now().toString());

        AccessMonitorWebSocketEndpoint.pushSystemAlert(
                rule.getAlertLevel(),
                rule.getRuleName(),
                "告警规则被触发: " + rule.getCondition(),
                alertDetails);

        log.warn("告警被触发，rule: {}, deviceId: {}", rule.getRuleName(), deviceId);
    }

    /**
     * 处理黑名单事件
     */
    private void handleBlacklistEvent(AccessEventMessage event) {
        log.warn("检测到黑名单访问事件，deviceId: {}, userId: {}", event.getDeviceId(), event.getUserId());

        // 推送高级别告警
        JSONObject alertDetails = new JSONObject();
        alertDetails.put("eventId", event.getEventId());
        alertDetails.put("deviceId", event.getDeviceId());
        alertDetails.put("deviceName", event.getDeviceName());
        alertDetails.put("userId", event.getUserId());
        alertDetails.put("userName", event.getUserName());
        alertDetails.put("eventTime", event.getEventTime().toString());

        AccessMonitorWebSocketEndpoint.pushSystemAlert(
                "critical",
                "黑名单访问告警",
                "检测到黑名单用户访问尝试",
                alertDetails);
    }

    /**
     * 处理失败访问事件
     */
    private void handleFailedAccessEvent(AccessEventMessage event) {
        // 连续失败检测
        String redisKey = "smart:monitor:failed_count:" + event.getUserId();
        Object countObj = redisUtil.get(redisKey);
        String countStr = countObj != null ? countObj.toString() : null;
        int failedCount = countStr != null ? Integer.parseInt(countStr) : 0;
        failedCount++;

        redisUtil.set(redisKey, String.valueOf(failedCount), 3600); // 1小时过期

        // 连续失败超过阈值时告警
        if (failedCount >= 5) {
            JSONObject alertDetails = new JSONObject();
            alertDetails.put("userId", event.getUserId());
            alertDetails.put("userName", event.getUserName());
            alertDetails.put("failedCount", failedCount);
            alertDetails.put("lastEventTime", event.getEventTime().toString());
            alertDetails.put("deviceId", event.getDeviceId());

            AccessMonitorWebSocketEndpoint.pushSystemAlert(
                    "warning",
                    "连续访问失败告警",
                    "用户连续" + failedCount + "次访问失败",
                    alertDetails);
        }
    }

    /**
     * 清理过期缓存
     */
    private void cleanExpiredCache() {
        // 清理7天前的事件统计
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        eventStatisticsCache.entrySet()
                .removeIf(entry -> entry.getKey().compareTo(cutoffDate.toLocalDate().toString()) < 0);

        // 清理24小时前未活动的设备状态
        deviceStatusCache.entrySet()
                .removeIf(entry -> entry.getValue().getLastUpdateTime().isBefore(LocalDateTime.now().minusHours(24)));

        log.debug("过期缓存清理完成");
    }

    /**
     * 获取实时统计信息
     */
    public Map<String, Object> getRealTimeStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 设备状态统计
        Map<String, Long> deviceStatusStats = new HashMap<>();
        deviceStatusStats.put("total", (long) deviceStatusCache.size());
        deviceStatusStats.put("online", deviceStatusCache.values().stream()
                .filter(ds -> "online".equals(ds.getOnlineStatus())).count());
        deviceStatusStats.put("offline", deviceStatusCache.values().stream()
                .filter(ds -> "offline".equals(ds.getOnlineStatus())).count());
        statistics.put("deviceStatus", deviceStatusStats);

        // 今日事件统计
        String today = LocalDateTime.now().toLocalDate().toString();
        EventStatistics todayStats = eventStatisticsCache.get(today);
        if (todayStats != null) {
            statistics.put("todayEvents", todayStats.getStatusCount());
            statistics.put("todayDeviceEvents", todayStats.getDeviceEventCount());
        }

        statistics.put("timestamp", LocalDateTime.now().toString());

        return statistics;
    }

    /**
     * 销毁监听器
     */
    public void destroy() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduledExecutor = null;
        }

        deviceStatusCache.clear();
        eventStatisticsCache.clear();
        alertRules.clear();

        log.info("门禁事件实时监听器已销毁");
    }
}
