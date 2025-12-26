package net.lab1024.sa.device.comm.monitor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.device.comm.message.DeviceOfflineEventProducer;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 设备离线检测器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * Manager层负责业务编排和复杂业务逻辑处理
 * </p>
 * <p>
 * 核心职责：
 * - 实时检测设备离线状态
 * - 区分暂时性离线和永久性离线
 * - 发布离线事件到消息队列
 * - 记录离线历史和统计
 * - 自动恢复在线检测
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class DeviceOfflineDetector {

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private DeviceOfflineEventProducer offlineEventProducer;

    // 离线状态缓存
    private final Map<Long, DeviceOfflineStatus> offlineStatusCache = new ConcurrentHashMap<>();

    // 离线历史记录
    private final List<OfflineEventRecord> offlineHistory = new ArrayList<>();

    // 定时检测线程池
    private final ScheduledExecutorService detector = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r, "device-offline-detector");
        thread.setDaemon(true);
        return thread;
    });

    // 配置参数
    private static final long DETECTION_INTERVAL_SECONDS = 30; // 检测间隔（秒）
    private static final long TEMPORARY_OFFLINE_THRESHOLD_MINUTES = 5; // 暂时性离线阈值（分钟）
    private static final long PERMANENT_OFFLINE_THRESHOLD_MINUTES = 30; // 永久性离线阈值（分钟）
    private static final int MAX_HISTORY_SIZE = 500; // 最大历史记录数

    /**
     * 初始化检测器
     */
    public void initialize() {
        log.info("[设备离线检测] 设备离线检测器初始化完成");

        // 启动定时检测任务
        detector.scheduleAtFixedRate(() -> {
            try {
                detectOfflineDevices();
            } catch (Exception e) {
                log.error("[设备离线检测] 定时检测任务执行失败", e);
            }
        }, DETECTION_INTERVAL_SECONDS, DETECTION_INTERVAL_SECONDS, TimeUnit.SECONDS);

        log.info("[设备离线检测] 启动定时检测任务，间隔: {}秒", DETECTION_INTERVAL_SECONDS);
    }

    /**
     * 检测离线设备
     */
    private void detectOfflineDevices() {
        log.debug("[设备离线检测] 开始检测设备离线状态");

        try {
            // 1. 查询所有启用的设备
            LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeviceEntity::getEnabled, 1);

            List<DeviceEntity> devices = deviceDao.selectList(queryWrapper);
            log.debug("[设备离线检测] 查询到设备数量: {}", devices.size());

            int newOfflineCount = 0;
            int recoveredCount = 0;
            int stillOfflineCount = 0;

            LocalDateTime now = LocalDateTime.now();

            // 2. 检查每个设备的在线状态
            for (DeviceEntity device : devices) {
                Long deviceId = device.getDeviceId();
                if (deviceId == null) {
                    continue;
                }

                // 检查最后在线时间
                LocalDateTime lastOnlineTime = device.getLastOnlineTime();
                if (lastOnlineTime == null) {
                    // 从未上线，跳过
                    continue;
                }

                // 计算离线时长（分钟）
                long minutesOffline = ChronoUnit.MINUTES.between(lastOnlineTime, now);

                if (minutesOffline > TEMPORARY_OFFLINE_THRESHOLD_MINUTES) {
                    // 设备离线
                    DeviceOfflineStatus currentStatus = offlineStatusCache.get(deviceId);

                    if (currentStatus == null) {
                        // 新发现的离线设备
                        newOfflineCount++;
                        handleNewOfflineDevice(device, minutesOffline);
                    } else {
                        // 之前已离线，更新状态
                        stillOfflineCount++;
                        updateExistingOfflineStatus(deviceId, minutesOffline);
                    }
                } else {
                    // 设备在线
                    DeviceOfflineStatus currentStatus = offlineStatusCache.get(deviceId);
                    if (currentStatus != null) {
                        // 之前离线，现已恢复
                        recoveredCount++;
                        handleRecoveredDevice(deviceId);
                    }
                }
            }

            if (newOfflineCount > 0 || recoveredCount > 0) {
                log.info("[设备离线检测] 检测完成 - 新离线: {}, 恢复在线: {}, 仍离线: {}",
                        newOfflineCount, recoveredCount, stillOfflineCount);
            } else {
                log.debug("[设备离线检测] 检测完成，无状态变化");
            }

        } catch (Exception e) {
            log.error("[设备离线检测] 检测离线设备失败", e);
        }
    }

    /**
     * 处理新离线设备
     */
    private void handleNewOfflineDevice(DeviceEntity device, long minutesOffline) {
        Long deviceId = device.getDeviceId();

        log.warn("[设备离线检测] 检测到新离线设备: deviceId={}, deviceName={}, 离线时长={}分钟",
                deviceId, device.getDeviceName(), minutesOffline);

        // 1. 创建离线状态
        DeviceOfflineStatus status = new DeviceOfflineStatus();
        status.setDeviceId(deviceId);
        status.setDeviceName(device.getDeviceName());
        status.setDeviceType(device.getDeviceType());
        status.setOfflineStartTime(device.getLastOnlineTime());
        status.setOfflineMinutes(minutesOffline);
        status.setOfflineType(determineOfflineType(minutesOffline));
        status.setFirstDetectedTime(LocalDateTime.now());
        status.setLastDetectedTime(LocalDateTime.now());

        offlineStatusCache.put(deviceId, status);

        // 2. 发布离线事件
        publishOfflineEvent(device, status, "NEW_OFFLINE");

        // 3. 记录历史
        recordOfflineHistory(device, status, "新离线");
    }

    /**
     * 更新已存在的离线状态
     */
    private void updateExistingOfflineStatus(Long deviceId, long minutesOffline) {
        DeviceOfflineStatus status = offlineStatusCache.get(deviceId);
        if (status == null) {
            return;
        }

        // 更新离线时长
        status.setOfflineMinutes(minutesOffline);
        status.setLastDetectedTime(LocalDateTime.now());

        // 更新离线类型
        OfflineType oldType = status.getOfflineType();
        OfflineType newType = determineOfflineType(minutesOffline);
        status.setOfflineType(newType);

        // 如果离线类型升级，发布事件
        if (newType.ordinal() > oldType.ordinal()) {
            log.warn("[设备离线检测] 设备离线状态升级: deviceId={}, 旧类型={}, 新类型={}, 离线时长={}分钟",
                    deviceId, oldType, newType, minutesOffline);

            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device != null) {
                publishOfflineEvent(device, status, "OFFLINE_ESCALATED");
                recordOfflineHistory(device, status, "离线状态升级");
            }
        }
    }

    /**
     * 处理恢复在线的设备
     */
    private void handleRecoveredDevice(Long deviceId) {
        DeviceOfflineStatus status = offlineStatusCache.remove(deviceId);

        if (status != null) {
            log.info("[设备离线检测] 设备恢复在线: deviceId={}, deviceName={}, 离线时长={}分钟",
                    deviceId, status.getDeviceName(), status.getOfflineMinutes());

            // 发布恢复事件
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device != null) {
                publishRecoveryEvent(device, status);
                recordOfflineHistory(device, status, "恢复在线");
            }
        }
    }

    /**
     * 发布离线事件
     */
    private void publishOfflineEvent(DeviceEntity device, DeviceOfflineStatus status, String eventType) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("deviceId", device.getDeviceId());
            eventData.put("deviceName", device.getDeviceName());
            eventData.put("deviceType", device.getDeviceType());
            eventData.put("offlineType", status.getOfflineType().name());
            eventData.put("offlineMinutes", status.getOfflineMinutes());
            eventData.put("offlineStartTime", status.getOfflineStartTime());
            eventData.put("eventType", eventType);
            eventData.put("eventTime", LocalDateTime.now());

            offlineEventProducer.sendOfflineEvent(eventData);

            log.debug("[设备离线检测] 发布离线事件成功: deviceId={}, eventType={}",
                    device.getDeviceId(), eventType);

        } catch (Exception e) {
            log.error("[设备离线检测] 发布离线事件失败: deviceId={}", device.getDeviceId(), e);
        }
    }

    /**
     * 发布恢复事件
     */
    private void publishRecoveryEvent(DeviceEntity device, DeviceOfflineStatus status) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("deviceId", device.getDeviceId());
            eventData.put("deviceName", device.getDeviceName());
            eventData.put("deviceType", device.getDeviceType());
            eventData.put("offlineMinutes", status.getOfflineMinutes());
            eventData.put("offlineStartTime", status.getOfflineStartTime());
            eventData.put("recoveryTime", LocalDateTime.now());
            eventData.put("eventType", "RECOVERED");

            offlineEventProducer.sendRecoveryEvent(eventData);

            log.debug("[设备离线检测] 发布恢复事件成功: deviceId={}", device.getDeviceId());

        } catch (Exception e) {
            log.error("[设备离线检测] 发布恢复事件失败: deviceId={}", device.getDeviceId(), e);
        }
    }

    /**
     * 记录离线历史
     */
    private synchronized void recordOfflineHistory(DeviceEntity device, DeviceOfflineStatus status, String description) {
        OfflineEventRecord record = new OfflineEventRecord();
        record.setDeviceId(device.getDeviceId());
        record.setDeviceName(device.getDeviceName());
        record.setDeviceType(device.getDeviceType());
        record.setOfflineType(status.getOfflineType());
        record.setOfflineMinutes(status.getOfflineMinutes());
        record.setDescription(description);
        record.setEventTime(LocalDateTime.now());

        offlineHistory.add(record);

        // 限制历史记录数量
        if (offlineHistory.size() > MAX_HISTORY_SIZE) {
            offlineHistory.remove(0);
        }
    }

    /**
     * 确定离线类型
     */
    private OfflineType determineOfflineType(long minutesOffline) {
        if (minutesOffline < TEMPORARY_OFFLINE_THRESHOLD_MINUTES) {
            return OfflineType.ONLINE;
        } else if (minutesOffline < PERMANENT_OFFLINE_THRESHOLD_MINUTES) {
            return OfflineType.TEMPORARY;
        } else {
            return OfflineType.PERMANENT;
        }
    }

    /**
     * 手动触发离线检测
     */
    public void triggerDetection() {
        log.info("[设备离线检测] 手动触发离线检测");
        detectOfflineDevices();
    }

    /**
     * 获取设备离线状态
     */
    public DeviceOfflineStatus getDeviceOfflineStatus(Long deviceId) {
        return offlineStatusCache.get(deviceId);
    }

    /**
     * 获取所有离线设备
     */
    public Collection<DeviceOfflineStatus> getAllOfflineDevices() {
        return offlineStatusCache.values();
    }

    /**
     * 获取离线历史记录
     */
    public List<OfflineEventRecord> getOfflineHistory(int limit) {
        synchronized (offlineHistory) {
            int fromIndex = Math.max(0, offlineHistory.size() - limit);
            return new ArrayList<>(offlineHistory.subList(fromIndex, offlineHistory.size()));
        }
    }

    /**
     * 获取离线统计信息
     */
    public OfflineStatistics getOfflineStatistics() {
        OfflineStatistics statistics = new OfflineStatistics();

        long now = System.currentTimeMillis();

        int totalOffline = offlineStatusCache.size();
        int temporaryOffline = 0;
        int permanentOffline = 0;

        for (DeviceOfflineStatus status : offlineStatusCache.values()) {
            if (status.getOfflineType() == OfflineType.TEMPORARY) {
                temporaryOffline++;
            } else if (status.getOfflineType() == OfflineType.PERMANENT) {
                permanentOffline++;
            }
        }

        statistics.setTotalOfflineDevices(totalOffline);
        statistics.setTemporaryOfflineDevices(temporaryOffline);
        statistics.setPermanentOfflineDevices(permanentOffline);
        statistics.setStatisticsTime(LocalDateTime.now());

        return statistics;
    }

    /**
     * 清除离线状态缓存
     */
    public void clearOfflineCache() {
        log.info("[设备离线检测] 清除离线状态缓存");
        offlineStatusCache.clear();
    }

    /**
     * 关闭检测器
     */
    public void shutdown() {
        log.info("[设备离线检测] 关闭设备离线检测器");
        detector.shutdown();
        try {
            if (!detector.awaitTermination(10, TimeUnit.SECONDS)) {
                detector.shutdownNow();
            }
        } catch (InterruptedException e) {
            detector.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ==================== 内部类 ====================

    /**
     * 离线类型枚举
     */
    public enum OfflineType {
        ONLINE("在线"),
        TEMPORARY("暂时性离线"),
        PERMANENT("永久性离线");

        private final String description;

        OfflineType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 设备离线状态
     */
    public static class DeviceOfflineStatus {
        private Long deviceId;
        private String deviceName;
        private Integer deviceType;
        private LocalDateTime offlineStartTime;
        private Long offlineMinutes;
        private OfflineType offlineType;
        private LocalDateTime firstDetectedTime;
        private LocalDateTime lastDetectedTime;

        // Getters and Setters
        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public Integer getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(Integer deviceType) {
            this.deviceType = deviceType;
        }

        public LocalDateTime getOfflineStartTime() {
            return offlineStartTime;
        }

        public void setOfflineStartTime(LocalDateTime offlineStartTime) {
            this.offlineStartTime = offlineStartTime;
        }

        public Long getOfflineMinutes() {
            return offlineMinutes;
        }

        public void setOfflineMinutes(Long offlineMinutes) {
            this.offlineMinutes = offlineMinutes;
        }

        public OfflineType getOfflineType() {
            return offlineType;
        }

        public void setOfflineType(OfflineType offlineType) {
            this.offlineType = offlineType;
        }

        public LocalDateTime getFirstDetectedTime() {
            return firstDetectedTime;
        }

        public void setFirstDetectedTime(LocalDateTime firstDetectedTime) {
            this.firstDetectedTime = firstDetectedTime;
        }

        public LocalDateTime getLastDetectedTime() {
            return lastDetectedTime;
        }

        public void setLastDetectedTime(LocalDateTime lastDetectedTime) {
            this.lastDetectedTime = lastDetectedTime;
        }
    }

    /**
     * 离线事件记录
     */
    public static class OfflineEventRecord {
        private Long deviceId;
        private String deviceName;
        private Integer deviceType;
        private OfflineType offlineType;
        private Long offlineMinutes;
        private String description;
        private LocalDateTime eventTime;

        // Getters and Setters
        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public Integer getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(Integer deviceType) {
            this.deviceType = deviceType;
        }

        public OfflineType getOfflineType() {
            return offlineType;
        }

        public void setOfflineType(OfflineType offlineType) {
            this.offlineType = offlineType;
        }

        public Long getOfflineMinutes() {
            return offlineMinutes;
        }

        public void setOfflineMinutes(Long offlineMinutes) {
            this.offlineMinutes = offlineMinutes;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDateTime getEventTime() {
            return eventTime;
        }

        public void setEventTime(LocalDateTime eventTime) {
            this.eventTime = eventTime;
        }
    }

    /**
     * 离线统计信息
     */
    public static class OfflineStatistics {
        private int totalOfflineDevices;
        private int temporaryOfflineDevices;
        private int permanentOfflineDevices;
        private LocalDateTime statisticsTime;

        // Getters and Setters
        public int getTotalOfflineDevices() {
            return totalOfflineDevices;
        }

        public void setTotalOfflineDevices(int totalOfflineDevices) {
            this.totalOfflineDevices = totalOfflineDevices;
        }

        public int getTemporaryOfflineDevices() {
            return temporaryOfflineDevices;
        }

        public void setTemporaryOfflineDevices(int temporaryOfflineDevices) {
            this.temporaryOfflineDevices = temporaryOfflineDevices;
        }

        public int getPermanentOfflineDevices() {
            return permanentOfflineDevices;
        }

        public void setPermanentOfflineDevices(int permanentOfflineDevices) {
            this.permanentOfflineDevices = permanentOfflineDevices;
        }

        public LocalDateTime getStatisticsTime() {
            return statisticsTime;
        }

        public void setStatisticsTime(LocalDateTime statisticsTime) {
            this.statisticsTime = statisticsTime;
        }

        /**
         * 生成文本报告
         */
        public String generateTextReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n==================== 设备离线统计报告 ====================\n");
            sb.append(String.format("统计时间: %s\n", statisticsTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            sb.append(String.format("总离线设备数: %d\n", totalOfflineDevices));
            sb.append(String.format("暂时性离线: %d\n", temporaryOfflineDevices));
            sb.append(String.format("永久性离线: %d\n", permanentOfflineDevices));
            sb.append("========================================================\n");
            return sb.toString();
        }
    }
}
