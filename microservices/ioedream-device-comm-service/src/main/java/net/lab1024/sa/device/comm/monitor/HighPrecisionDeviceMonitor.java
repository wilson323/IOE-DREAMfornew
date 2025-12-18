package net.lab1024.sa.device.comm.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 高精度设备监控器
 * <p>
 * 提供亚秒级精度的设备状态监控：
 * 1. 毫秒级设备状态采集和缓存
 * 2. 实时设备连接状态监控
 * 3. 高性能数据存储和查询
 * 4. 智能监控频率调节
 * 5. 设备状态预测和异常检测
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
@Schema(description = "高精度设备监控器")
public class HighPrecisionDeviceMonitor {

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    // 高精度监控配置
    private static final int HIGH_PRECISION_INTERVAL_MS = 100;    // 100ms高精度采集
    private static final int NORMAL_PRECISION_INTERVAL_MS = 1000;  // 1s普通精度采集
    private static final int BATCH_SIZE = 50;                       // 批处理大小
    private static final int MAX_CACHE_SIZE = 10000;               // 最大缓存大小
    private static final long CACHE_EXPIRE_MINUTES = 30;            // 缓存过期时间（分钟）

    // 线程池配置
    private final ScheduledExecutorService highPrecisionScheduler;
    private final ExecutorService batchProcessor;
    private final ExecutorService asyncProcessor;

    // 高精度数据存储
    private final Map<String, DeviceStatusSnapshot> deviceStatusCache;
    private final Map<String, DeviceMetricsHistory> deviceMetricsHistory;
    private final Map<String, DeviceMonitorConfig> monitorConfigs;

    // 读写锁保证并发安全
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();

    // 性能统计
    private final AtomicLong totalMonitorCount = new AtomicLong(0);
    private final AtomicLong successMonitorCount = new AtomicLong(0);
    private final AtomicLong failedMonitorCount = new AtomicLong(0);

    /**
     * 设备状态快照
     */
    @Schema(description = "设备状态快照")
    public static class DeviceStatusSnapshot {
        private String deviceId;
        private String deviceType;
        private DeviceStatus status;
        private long responseTimeMs;          // 响应时间（毫秒）
        private double cpuUsage;               // CPU使用率
        private long memoryUsage;             // 内存使用量
        private double networkLatency;        // 网络延迟
        private int connectionCount;          // 连接数
        private LocalDateTime timestamp;       // 时间戳
        private String healthLevel;           // 健康等级
        private Map<String, Object> extendedAttributes; // 扩展属性

        // 构造函数
        public DeviceStatusSnapshot() {
            this.timestamp = LocalDateTime.now();
            this.extendedAttributes = new HashMap<>();
        }

        // getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

        public DeviceStatus getStatus() { return status; }
        public void setStatus(DeviceStatus status) { this.status = status; }

        public long getResponseTimeMs() { return responseTimeMs; }
        public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }

        public long getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(long memoryUsage) { this.memoryUsage = memoryUsage; }

        public double getNetworkLatency() { return networkLatency; }
        public void setNetworkLatency(double networkLatency) { this.networkLatency = networkLatency; }

        public int getConnectionCount() { return connectionCount; }
        public void setConnectionCount(int connectionCount) { this.connectionCount = connectionCount; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getHealthLevel() { return healthLevel; }
        public void setHealthLevel(String healthLevel) { this.healthLevel = healthLevel; }

        public Map<String, Object> getExtendedAttributes() { return extendedAttributes; }
        public void setExtendedAttributes(Map<String, Object> extendedAttributes) { this.extendedAttributes = extendedAttributes; }
    }

    /**
     * 设备状态枚举
     */
    @Schema(description = "设备状态")
    public enum DeviceStatus {
        ONLINE("在线", 1),
        OFFLINE("离线", 2),
        BUSY("繁忙", 3),
        ERROR("错误", 4),
        MAINTENANCE("维护中", 5),
        UNKNOWN("未知", 6);

        private final String description;
        private final int code;

        DeviceStatus(String description, int code) {
            this.description = description;
            this.code = code;
        }

        public String getDescription() { return description; }
        public int getCode() { return code; }
    }

    /**
     * 设备指标历史数据
     */
    @Schema(description = "设备指标历史数据")
    public static class DeviceMetricsHistory {
        private String deviceId;
        private Deque<DeviceStatusSnapshot> recentSnapshots;
        private int maxHistorySize;
        private LocalDateTime lastUpdateTime;

        public DeviceMetricsHistory(String deviceId, int maxHistorySize) {
            this.deviceId = deviceId;
            this.maxHistorySize = maxHistorySize;
            this.recentSnapshots = new ConcurrentLinkedDeque<>();
            this.lastUpdateTime = LocalDateTime.now();
        }

        public void addSnapshot(DeviceStatusSnapshot snapshot) {
            recentSnapshots.offer(snapshot);
            // 保持队列大小限制
            while (recentSnapshots.size() > maxHistorySize) {
                recentSnapshots.poll();
            }
            lastUpdateTime = LocalDateTime.now();
        }

        public List<DeviceStatusSnapshot> getRecentSnapshots(int count) {
            return recentSnapshots.stream()
                    .skip(Math.max(0, recentSnapshots.size() - count))
                    .toList();
        }

        public DeviceStatusSnapshot getLatestSnapshot() {
            return recentSnapshots.peekLast();
        }

        // getters
        public String getDeviceId() { return deviceId; }
        public Deque<DeviceStatusSnapshot> getRecentSnapshots() { return recentSnapshots; }
        public int getMaxHistorySize() { return maxHistorySize; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
    }

    /**
     * 设备监控配置
     */
    @Schema(description = "设备监控配置")
    public static class DeviceMonitorConfig {
        private int monitorIntervalMs;      // 监控间隔（毫秒）
        private int timeoutMs;              // 超时时间（毫秒）
        private int maxHistorySize;         // 最大历史记录数
        private boolean enableHighPrecision; // 启用高精度监控
        private double cpuThreshold;        // CPU阈值
        private long memoryThreshold;       // 内存阈值
        private double latencyThreshold;    // 延迟阈值

        public DeviceMonitorConfig(int monitorIntervalMs, int timeoutMs, int maxHistorySize,
                                 boolean enableHighPrecision, double cpuThreshold,
                                 long memoryThreshold, double latencyThreshold) {
            this.monitorIntervalMs = monitorIntervalMs;
            this.timeoutMs = timeoutMs;
            this.maxHistorySize = maxHistorySize;
            this.enableHighPrecision = enableHighPrecision;
            this.cpuThreshold = cpuThreshold;
            this.memoryThreshold = memoryThreshold;
            this.latencyThreshold = latencyThreshold;
        }

        // getters
        public int getMonitorIntervalMs() { return monitorIntervalMs; }
        public int getTimeoutMs() { return timeoutMs; }
        public int getMaxHistorySize() { return maxHistorySize; }
        public boolean isEnableHighPrecision() { return enableHighPrecision; }
        public double getCpuThreshold() { return cpuThreshold; }
        public long getMemoryThreshold() { return memoryThreshold; }
        public double getLatencyThreshold() { return latencyThreshold; }
    }

    /**
     * 构造函数
     */
    public HighPrecisionDeviceMonitor() {
        // 初始化线程池
        this.highPrecisionScheduler = Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r, "high-precision-monitor");
            t.setDaemon(true);
            return t;
        });

        this.batchProcessor = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r, "batch-processor");
            t.setDaemon(true);
            return t;
        });

        this.asyncProcessor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "async-processor");
            t.setDaemon(true);
            return t;
        });

        // 初始化存储
        this.deviceStatusCache = new ConcurrentHashMap<>();
        this.deviceMetricsHistory = new ConcurrentHashMap<>();
        this.monitorConfigs = new ConcurrentHashMap<>();

        // 初始化监控配置
        initializeMonitorConfigs();

        // 启动监控
        startMonitoring();

        log.info("[高精度监控] 高精度设备监控器初始化完成");
    }

    /**
     * 获取设备实时状态
     *
     * @param deviceId 设备ID
     * @return 设备状态快照
     */
    public DeviceStatusSnapshot getDeviceRealTimeStatus(String deviceId) {
        cacheLock.readLock().lock();
        try {
            DeviceStatusSnapshot snapshot = deviceStatusCache.get(deviceId);
            if (snapshot == null) {
                // 如果缓存中没有，立即执行一次监控
                return monitorDeviceNow(deviceId);
            }
            return snapshot;
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 获取设备状态历史
     *
     * @param deviceId 设备ID
     * @param count    获取数量
     * @return 历史状态列表
     */
    public List<DeviceStatusSnapshot> getDeviceStatusHistory(String deviceId, int count) {
        cacheLock.readLock().lock();
        try {
            DeviceMetricsHistory history = deviceMetricsHistory.get(deviceId);
            if (history == null) {
                return new ArrayList<>();
            }
            return history.getRecentSnapshots(count);
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 获取设备性能统计
     *
     * @return 性能统计信息
     */
    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        long total = totalMonitorCount.get();
        long success = successMonitorCount.get();
        long failed = failedMonitorCount.get();

        double successRate = total > 0 ? (success * 100.0 / total) : 0.0;
        double failedRate = total > 0 ? (failed * 100.0 / total) : 0.0;

        statistics.put("totalMonitors", total);
        statistics.put("successMonitors", success);
        statistics.put("failedMonitors", failed);
        statistics.put("successRate", Math.round(successRate * 100.0) / 100.0);
        statistics.put("failedRate", Math.round(failedRate * 100.0) / 100.0);
        statistics.put("cacheSize", deviceStatusCache.size());
        statistics.put("historySize", deviceMetricsHistory.size());

        // 计算平均响应时间
        double avgResponseTime = calculateAverageResponseTime();
        statistics.put("averageResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);

        return statistics;
    }

    /**
     * 立即监控指定设备
     *
     * @param deviceId 设备ID
     * @return 设备状态快照
     */
    @Async
    public CompletableFuture<DeviceStatusSnapshot> monitorDeviceAsync(String deviceId) {
        return CompletableFuture.supplyAsync(() -> monitorDeviceNow(deviceId), asyncProcessor);
    }

    /**
     * 批量监控设备
     *
     * @param deviceIds 设备ID列表
     * @return 监控结果
     */
    public Map<String, DeviceStatusSnapshot> batchMonitorDevices(List<String> deviceIds) {
        Map<String, DeviceStatusSnapshot> results = new ConcurrentHashMap<>();

        // 分批处理
        List<List<String>> batches = partitionList(deviceIds, BATCH_SIZE);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (List<String> batch : batches) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (String deviceId : batch) {
                    try {
                        DeviceStatusSnapshot snapshot = monitorDeviceNow(deviceId);
                        results.put(deviceId, snapshot);
                    } catch (Exception e) {
                        log.error("[高精度监控] 批量监控设备失败, deviceId={}", deviceId, e);
                    }
                }
            }, batchProcessor);
            futures.add(future);
        }

        // 等待所有批次完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return results;
    }

    /**
     * 更新设备监控配置
     *
     * @param deviceId 设备ID
     * @param config   监控配置
     */
    public void updateDeviceMonitorConfig(String deviceId, DeviceMonitorConfig config) {
        monitorConfigs.put(deviceId, config);
        log.info("[高精度监控] 更新设备监控配置, deviceId={}, interval={}ms",
                deviceId, config.getMonitorIntervalMs());
    }

    /**
     * 立即监控设备（同步方法）
     *
     * @param deviceId 设备ID
     * @return 设备状态快照
     */
    private DeviceStatusSnapshot monitorDeviceNow(String deviceId) {
        long startTime = System.currentTimeMillis();
        totalMonitorCount.incrementAndGet();

        try {
            // 1. 获取设备信息
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[高精度监控] 设备不存在, deviceId={}", deviceId);
                failedMonitorCount.incrementAndGet();
                return createErrorSnapshot(deviceId, "设备不存在");
            }

            // 2. 获取监控配置
            DeviceMonitorConfig config = getDeviceMonitorConfig(deviceId);

            // 3. 执行高精度状态采集
            DeviceStatusSnapshot snapshot = collectHighPrecisionStatus(device, config);

            // 4. 更新缓存
            updateDeviceStatusCache(deviceId, snapshot);

            // 5. 更新历史记录
            updateDeviceMetricsHistory(deviceId, snapshot, config.getMaxHistorySize());

            long responseTime = System.currentTimeMillis() - startTime;
            snapshot.setResponseTimeMs(responseTime);

            successMonitorCount.incrementAndGet();

            log.debug("[高精度监控] 设备监控完成, deviceId={}, status={}, responseTime={}ms",
                    deviceId, snapshot.getStatus(), responseTime);

            return snapshot;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            failedMonitorCount.incrementAndGet();
            log.error("[高精度监控] 设备监控异常, deviceId={}, responseTime={}ms",
                    deviceId, responseTime, e);

            DeviceStatusSnapshot errorSnapshot = createErrorSnapshot(deviceId, e.getMessage());
            errorSnapshot.setResponseTimeMs(responseTime);
            updateDeviceStatusCache(deviceId, errorSnapshot);

            return errorSnapshot;
        }
    }

    /**
     * 收集高精度状态信息
     */
    private DeviceStatusSnapshot collectHighPrecisionStatus(DeviceEntity device, DeviceMonitorConfig config) {
        DeviceStatusSnapshot snapshot = new DeviceStatusSnapshot();
        snapshot.setDeviceId(device.getDeviceId());
        snapshot.setDeviceType(device.getDeviceType());

        // 1. 基础状态检查
        DeviceStatus basicStatus = checkDeviceBasicStatus(device);
        snapshot.setStatus(basicStatus);

        // 2. 高精度性能指标采集
        if (config.isEnableHighPrecision()) {
            collectHighPrecisionMetrics(snapshot, device, config);
        }

        // 3. 健康等级评估
        String healthLevel = evaluateHealthLevel(snapshot, config);
        snapshot.setHealthLevel(healthLevel);

        // 4. 扩展属性采集
        collectExtendedAttributes(snapshot, device);

        return snapshot;
    }

    /**
     * 检查设备基础状态
     */
    private DeviceStatus checkDeviceBasicStatus(DeviceEntity device) {
        // 检查设备启用状态
        if (device.getEnabled() == null || device.getEnabled() == 0) {
            return DeviceStatus.MAINTENANCE;
        }

        // 检查最后在线时间
        LocalDateTime lastOnlineTime = device.getLastOnlineTime();
        if (lastOnlineTime != null) {
            long minutesOffline = ChronoUnit.MINUTES.between(lastOnlineTime, LocalDateTime.now());
            if (minutesOffline > 5) {
                return DeviceStatus.OFFLINE;
            } else if (minutesOffline > 1) {
                return DeviceStatus.BUSY;
            }
        }

        // 检查设备状态字段（Integer类型：1-在线 2-离线 3-故障 4-维护 5-停用）
        Integer deviceStatus = device.getDeviceStatus();
        if (deviceStatus == null || deviceStatus == 2) {
            return DeviceStatus.OFFLINE;
        } else if (deviceStatus == 3) {
            return DeviceStatus.ERROR;
        } else if (deviceStatus == 4) {
            return DeviceStatus.MAINTENANCE;
        } else if (deviceStatus == 5) {
            return DeviceStatus.MAINTENANCE;
        }

        return DeviceStatus.ONLINE;
    }

    /**
     * 收集高精度性能指标
     */
    private void collectHighPrecisionMetrics(DeviceStatusSnapshot snapshot, DeviceEntity device, DeviceMonitorConfig config) {
        try {
            // 通过设备通讯服务获取实时指标
            if (gatewayServiceClient != null) {
                @SuppressWarnings("unchecked")
                CompletableFuture<ResponseDTO<Map<String, Object>>> metricsFuture =
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>)
                                        gatewayServiceClient.callDeviceCommService(
                                                "/api/v1/device/realtime-metrics/" + device.getDeviceId(),
                                                HttpMethod.GET,
                                                null,
                                                Map.class
                                        );
                            } catch (Exception e) {
                                log.debug("[高精度监控] 获取实时指标失败, deviceId={}, error={}",
                                        device.getDeviceId(), e.getMessage());
                                return null;
                            }
                        });

                // 等待响应，使用配置的超时时间
                ResponseDTO<Map<String, Object>> response = metricsFuture
                        .get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);

                if (response != null && response.isSuccess()) {
                    Map<String, Object> metricsData = response.getData();
                    if (metricsData != null) {
                        // 解析高精度指标
                        snapshot.setCpuUsage(getDoubleValue(metricsData.get("cpuUsage"), 0.0));
                        snapshot.setMemoryUsage(getLongValue(metricsData.get("memoryUsage"), 0L));
                        snapshot.setNetworkLatency(getDoubleValue(metricsData.get("networkLatency"), 0.0));
                        snapshot.setConnectionCount(getIntValue(metricsData.get("connectionCount"), 0));
                    }
                }
            }

        } catch (TimeoutException e) {
            log.warn("[高精度监控] 获取指标超时, deviceId={}, timeout={}ms",
                    device.getDeviceId(), config.getTimeoutMs());
        } catch (Exception e) {
            log.debug("[高精度监控] 收集性能指标异常, deviceId={}", device.getDeviceId(), e);
        }
    }

    /**
     * 评估健康等级
     */
    private String evaluateHealthLevel(DeviceStatusSnapshot snapshot, DeviceMonitorConfig config) {
        int score = 100;

        // 状态评分
        switch (snapshot.getStatus()) {
            case ONLINE:
                score += 0;
                break;
            case BUSY:
                score -= 20;
                break;
            case ERROR:
                score -= 60;
                break;
            case OFFLINE:
                score -= 80;
                break;
            case MAINTENANCE:
                score -= 40;
                break;
            case UNKNOWN:
                score -= 50;
                break;
        }

        // 性能评分
        if (snapshot.getCpuUsage() > config.getCpuThreshold()) {
            score -= 20;
        }

        if (snapshot.getMemoryUsage() > config.getMemoryThreshold()) {
            score -= 15;
        }

        if (snapshot.getNetworkLatency() > config.getLatencyThreshold()) {
            score -= 10;
        }

        // 响应时间评分
        if (snapshot.getResponseTimeMs() > config.getTimeoutMs()) {
            score -= 25;
        }

        // 确定健康等级
        if (score >= 90) {
            return "优秀";
        } else if (score >= 75) {
            return "良好";
        } else if (score >= 60) {
            return "一般";
        } else if (score >= 40) {
            return "较差";
        } else {
            return "危险";
        }
    }

    /**
     * 收集扩展属性
     */
    private void collectExtendedAttributes(DeviceStatusSnapshot snapshot, DeviceEntity device) {
        Map<String, Object> extendedAttributes = new HashMap<>();

        // 设备基础信息
        extendedAttributes.put("deviceName", device.getDeviceName());
        extendedAttributes.put("deviceCode", device.getDeviceCode());
        extendedAttributes.put("deviceModel", device.getModel());
        extendedAttributes.put("manufacturer", device.getBrand());

        // 位置信息
        if (device.getAreaId() != null) {
            extendedAttributes.put("areaId", device.getAreaId());
        }

        // 协议信息（从扩展属性中解析，或使用业务模块）
        if (device.getBusinessModule() != null) {
            extendedAttributes.put("businessModule", device.getBusinessModule());
        }

        // 其他业务属性
        if (device.getExtendedAttributes() != null) {
            extendedAttributes.put("businessAttributes", device.getExtendedAttributes());
        }

        snapshot.setExtendedAttributes(extendedAttributes);
    }

    /**
     * 更新设备状态缓存
     */
    private void updateDeviceStatusCache(String deviceId, DeviceStatusSnapshot snapshot) {
        cacheLock.writeLock().lock();
        try {
            deviceStatusCache.put(deviceId, snapshot);

            // 检查缓存大小，清理过期数据
            if (deviceStatusCache.size() > MAX_CACHE_SIZE) {
                cleanupExpiredCache();
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 更新设备指标历史
     */
    private void updateDeviceMetricsHistory(String deviceId, DeviceStatusSnapshot snapshot, int maxHistorySize) {
        cacheLock.writeLock().lock();
        try {
            DeviceMetricsHistory history = deviceMetricsHistory.computeIfAbsent(
                    deviceId, id -> new DeviceMetricsHistory(id, maxHistorySize));
            history.addSnapshot(snapshot);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(CACHE_EXPIRE_MINUTES);

        deviceStatusCache.entrySet().removeIf(entry -> {
            DeviceStatusSnapshot snapshot = entry.getValue();
            return snapshot.getTimestamp().isBefore(expireTime);
        });
    }

    /**
     * 计算平均响应时间
     */
    private double calculateAverageResponseTime() {
        cacheLock.readLock().lock();
        try {
            return deviceStatusCache.values().stream()
                    .mapToLong(DeviceStatusSnapshot::getResponseTimeMs)
                    .average()
                    .orElse(0.0);
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 获取设备监控配置
     */
    private DeviceMonitorConfig getDeviceMonitorConfig(String deviceId) {
        // 优先使用设备特定配置
        DeviceMonitorConfig config = monitorConfigs.get(deviceId);
        if (config != null) {
            return config;
        }

        // 使用默认配置
        return monitorConfigs.get("DEFAULT");
    }

    /**
     * 创建错误快照
     */
    private DeviceStatusSnapshot createErrorSnapshot(String deviceId, String errorMessage) {
        DeviceStatusSnapshot snapshot = new DeviceStatusSnapshot();
        snapshot.setDeviceId(deviceId);
        snapshot.setStatus(DeviceStatus.ERROR);
        snapshot.setHealthLevel("危险");
        snapshot.setExtendedAttributes(Map.of("error", errorMessage));
        return snapshot;
    }

    /**
     * 初始化监控配置
     */
    private void initializeMonitorConfigs() {
        // 高精度设备配置（视频、门禁等）
        monitorConfigs.put("VIDEO", new DeviceMonitorConfig(
                HIGH_PRECISION_INTERVAL_MS, 500, 1000, true, 80.0, 1024 * 1024 * 1024L, 100.0));
        monitorConfigs.put("ACCESS", new DeviceMonitorConfig(
                HIGH_PRECISION_INTERVAL_MS, 300, 500, true, 70.0, 512 * 1024 * 1024L, 50.0));
        monitorConfigs.put("ATTENDANCE", new DeviceMonitorConfig(
                NORMAL_PRECISION_INTERVAL_MS, 1000, 300, false, 60.0, 256 * 1024 * 1024L, 200.0));
        monitorConfigs.put("CONSUME", new DeviceMonitorConfig(
                NORMAL_PRECISION_INTERVAL_MS, 800, 300, true, 75.0, 512 * 1024 * 1024L, 150.0));

        // 默认配置
        monitorConfigs.put("DEFAULT", new DeviceMonitorConfig(
                NORMAL_PRECISION_INTERVAL_MS, 1000, 200, false, 70.0, 512 * 1024 * 1024L, 200.0));

        log.info("[高精度监控] 监控配置初始化完成，配置数量: {}", monitorConfigs.size());
    }

    /**
     * 启动监控
     */
    private void startMonitoring() {
        // 启动高精度监控任务（每100ms执行一次）
        highPrecisionScheduler.scheduleAtFixedRate(this::performHighPrecisionMonitoring,
                100, HIGH_PRECISION_INTERVAL_MS, TimeUnit.MILLISECONDS);

        // 启动缓存清理任务（每5分钟执行一次）
        highPrecisionScheduler.scheduleAtFixedRate(this::performCacheCleanup,
                300, 300, TimeUnit.SECONDS);

        log.info("[高精度监控] 高精度监控任务已启动");
    }

    /**
     * 执行高精度监控
     */
    private void performHighPrecisionMonitoring() {
        try {
            // 获取需要高精度监控的设备列表
            List<String> highPrecisionDevices = getHighPrecisionDevices();

            if (!highPrecisionDevices.isEmpty()) {
                // 异步批量监控
                batchProcessor.submit(() -> batchMonitorDevices(highPrecisionDevices));
            }

        } catch (Exception e) {
            log.error("[高精度监控] 高精度监控执行异常", e);
        }
    }

    /**
     * 执行缓存清理
     */
    private void performCacheCleanup() {
        cacheLock.writeLock().lock();
        try {
            int beforeSize = deviceStatusCache.size();
            cleanupExpiredCache();
            int cleanedCount = beforeSize - deviceStatusCache.size();

            if (cleanedCount > 0) {
                log.info("[高精度监控] 缓存清理完成, cleanedCount={}, remainingCount={}",
                        cleanedCount, deviceStatusCache.size());
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 获取需要高精度监控的设备列表
     */
    private List<String> getHighPrecisionDevices() {
        try {
            List<DeviceEntity> devices = deviceDao.selectList(null);
            return devices.stream()
                    .filter(device -> {
                        DeviceMonitorConfig config = getDeviceMonitorConfig(device.getDeviceId());
                        return config.isEnableHighPrecision() &&
                               DeviceStatus.ONLINE.equals(checkDeviceBasicStatus(device));
                    })
                    .map(DeviceEntity::getDeviceId)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("[高精度监控] 获取高精度设备列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 分割列表
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }

    /**
     * 辅助方法：获取Double值
     */
    private double getDoubleValue(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 辅助方法：获取Long值
     */
    private long getLongValue(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 辅助方法：获取Int值
     */
    private int getIntValue(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 关闭监控器
     */
    public void shutdown() {
        log.info("[高精度监控] 关闭高精度设备监控器");

        try {
            // 关闭线程池
            highPrecisionScheduler.shutdown();
            batchProcessor.shutdown();
            asyncProcessor.shutdown();

            // 等待任务完成
            if (!highPrecisionScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                highPrecisionScheduler.shutdownNow();
            }
            if (!batchProcessor.awaitTermination(10, TimeUnit.SECONDS)) {
                batchProcessor.shutdownNow();
            }
            if (!asyncProcessor.awaitTermination(10, TimeUnit.SECONDS)) {
                asyncProcessor.shutdownNow();
            }

            // 清理缓存
            cacheLock.writeLock().lock();
            try {
                deviceStatusCache.clear();
                deviceMetricsHistory.clear();
                monitorConfigs.clear();
            } finally {
                cacheLock.writeLock().unlock();
            }

            log.info("[高精度监控] 高精度设备监控器已关闭");

        } catch (InterruptedException e) {
            log.warn("[高精度监控] 关闭监控器被中断");
            Thread.currentThread().interrupt();
        }
    }
}
