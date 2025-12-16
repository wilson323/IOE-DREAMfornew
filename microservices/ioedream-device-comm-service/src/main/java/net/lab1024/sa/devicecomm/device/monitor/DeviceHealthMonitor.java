package net.lab1024.sa.devicecomm.device.monitor;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.devicecomm.device.manager.DeviceStatusManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;

import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 设备健康监控器
 * <p>
 * 统一监控所有设备的健康状态
 * 提供设备状态检查、性能监控、告警管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class DeviceHealthMonitor {

    private final DeviceDao deviceDao;
    private final DeviceStatusManager deviceStatusManager; // 设备状态管理器，用于检查设备在线状态
    private final GatewayServiceClient gatewayServiceClient; // 网关服务客户端，用于调用设备通讯服务
    private final ScheduledExecutorService scheduler;
    private final Map<String, DeviceHealthStatus> deviceHealthMap;
    private final Map<String, LocalDateTime> deviceHealthUpdateTimeMap; // 设备健康状态更新时间映射
    private final Map<String, DeviceMonitorConfig> monitorConfigMap;

    // 心跳超时时间配置（默认5分钟）
    private static final int DEFAULT_HEARTBEAT_TIMEOUT_SECONDS = 300;
    // 健康检查超时时间（默认3秒）
    private static final int HEALTH_CHECK_TIMEOUT_SECONDS = 3;
    // 响应时间阈值（默认1000ms）
    private static final int RESPONSE_TIME_THRESHOLD_MS = 1000;

    // 构造函数注入依赖
    public DeviceHealthMonitor(DeviceDao deviceDao, DeviceStatusManager deviceStatusManager,
                              GatewayServiceClient gatewayServiceClient) {
        this.deviceDao = deviceDao;
        this.deviceStatusManager = deviceStatusManager;
        this.gatewayServiceClient = gatewayServiceClient;
        this.scheduler = Executors.newScheduledThreadPool(4);
        this.deviceHealthMap = new ConcurrentHashMap<>();
        this.deviceHealthUpdateTimeMap = new ConcurrentHashMap<>(); // 初始化更新时间映射
        this.monitorConfigMap = new ConcurrentHashMap<>();

        initializeMonitorConfigs();
        startHealthMonitoring();
    }

    /**
     * 设备健康状态枚举
     */
    public enum DeviceHealthStatus {
        HEALTHY(1, "健康"),
        WARNING(2, "警告"),
        ERROR(3, "错误"),
        OFFLINE(4, "离线"),
        MAINTENANCE(5, "维护中"),
        UNKNOWN(6, "未知");

        private final int code;
        private final String description;

        DeviceHealthStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 设备监控配置
     */
    public static class DeviceMonitorConfig {
        private int healthCheckInterval; // 健康检查间隔（秒）
        private int timeoutThreshold;    // 超时阈值（秒）
        private int maxRetryCount;       // 最大重试次数
        private boolean enablePerformanceMonitor; // 是否启用性能监控

        // 构造函数
        public DeviceMonitorConfig(int healthCheckInterval, int timeoutThreshold,
                                   int maxRetryCount, boolean enablePerformanceMonitor) {
            this.healthCheckInterval = healthCheckInterval;
            this.timeoutThreshold = timeoutThreshold;
            this.maxRetryCount = maxRetryCount;
            this.enablePerformanceMonitor = enablePerformanceMonitor;
        }

        // getters
        public int getHealthCheckInterval() { return healthCheckInterval; }
        public int getTimeoutThreshold() { return timeoutThreshold; }
        public int getMaxRetryCount() { return maxRetryCount; }
        public boolean isEnablePerformanceMonitor() { return enablePerformanceMonitor; }
    }

    /**
     * 设备性能指标
     */
    public static class DevicePerformanceMetrics {
        private double cpuUsage;           // CPU使用率
        private long memoryUsage;         // 内存使用量
        private double networkLatency;    // 网络延迟
        private int responseTime;         // 响应时间
        private int errorRate;            // 错误率
        private LocalDateTime lastUpdateTime; // 最后更新时间

        // 构造函数
        public DevicePerformanceMetrics(double cpuUsage, long memoryUsage, double networkLatency,
                                       int responseTime, int errorRate) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.networkLatency = networkLatency;
            this.responseTime = responseTime;
            this.errorRate = errorRate;
            this.lastUpdateTime = LocalDateTime.now();
        }

        // getters
        public double getCpuUsage() { return cpuUsage; }
        public long getMemoryUsage() { return memoryUsage; }
        public double getNetworkLatency() { return networkLatency; }
        public int getResponseTime() { return responseTime; }
        public int getErrorRate() { return errorRate; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
    }

    /**
     * 检查设备健康状态
     *
     * @param deviceId 设备ID
     * @return 健康状态
     */
    public DeviceHealthStatus checkDeviceHealth(String deviceId) {
        log.debug("[设备健康监控] 检查设备健康状态, deviceId={}", deviceId);

        try {
            // 1. 获取设备信息
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[设备健康监控] 设备不存在, deviceId={}", deviceId);
                return DeviceHealthStatus.UNKNOWN;
            }

            // 2. 检查设备基本状态
            if (!isDeviceOnline(device)) {
                updateDeviceHealthStatus(deviceId, DeviceHealthStatus.OFFLINE);
                return DeviceHealthStatus.OFFLINE;
            }

            // 3. 执行健康检查
            DeviceHealthStatus healthStatus = performHealthCheck(device);

            // 4. 更新健康状态
            updateDeviceHealthStatus(deviceId, healthStatus);

            return healthStatus;

        } catch (Exception e) {
            log.error("[设备健康监控] 健康检查异常, deviceId={}", deviceId, e);
            updateDeviceHealthStatus(deviceId, DeviceHealthStatus.ERROR);
            return DeviceHealthStatus.ERROR;
        }
    }

    /**
     * 获取设备性能指标
     *
     * @param deviceId 设备ID
     * @return 性能指标
     */
    public DevicePerformanceMetrics getDevicePerformance(String deviceId) {
        log.debug("[设备健康监控] 获取设备性能指标, deviceId={}", deviceId);

        try {
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return null;
            }

            DeviceMonitorConfig config = getMonitorConfig(device.getDeviceType());
            if (!config.isEnablePerformanceMonitor()) {
                return null;
            }

            return collectPerformanceMetrics(device);

        } catch (Exception e) {
            log.error("[设备健康监控] 获取性能指标异常, deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 获取所有设备健康状态概览
     *
     * @return 健康状态统计
     */
    public Map<String, Object> getDeviceHealthOverview() {
        log.debug("[设备健康监控] 获取设备健康状态概览");

        Map<String, Object> overview = new ConcurrentHashMap<>();

        // 统计各状态设备数量
        Map<DeviceHealthStatus, Long> statusCount = new ConcurrentHashMap<>();
        statusCount.put(DeviceHealthStatus.HEALTHY, 0L);
        statusCount.put(DeviceHealthStatus.WARNING, 0L);
        statusCount.put(DeviceHealthStatus.ERROR, 0L);
        statusCount.put(DeviceHealthStatus.OFFLINE, 0L);
        statusCount.put(DeviceHealthStatus.MAINTENANCE, 0L);
        statusCount.put(DeviceHealthStatus.UNKNOWN, 0L);

        // 遍历设备健康状态
        deviceHealthMap.values().forEach(status -> {
            if (status != null) {
                // 使用lambda表达式避免Null type safety警告
                statusCount.merge(status, 1L, (oldValue, newValue) -> oldValue + newValue);
            }
        });

        // 计算总体健康度
        long totalDevices = statusCount.values().stream().mapToLong(Long::longValue).sum();
        long healthyDevices = statusCount.getOrDefault(DeviceHealthStatus.HEALTHY, 0L);
        double healthPercentage = totalDevices > 0 ? (healthyDevices * 100.0 / totalDevices) : 0.0;

        overview.put("totalDevices", totalDevices);
        overview.put("healthyDevices", healthyDevices);
        overview.put("healthPercentage", Math.round(healthPercentage * 100.0) / 100.0);
        overview.put("statusDistribution", statusCount);
        overview.put("lastUpdateTime", LocalDateTime.now());

        return overview;
    }

    /**
     * 获取需要关注的设备列表
     *
     * @return 问题设备列表
     */
    public List<String> getProblematicDevices() {
        log.debug("[设备健康监控] 获取问题设备列表");

        return deviceHealthMap.entrySet().stream()
                .filter(entry -> {
                    DeviceHealthStatus status = entry.getValue();
                    return status == DeviceHealthStatus.ERROR
                           || status == DeviceHealthStatus.OFFLINE;
                })
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * 初始化监控配置
     */
    private void initializeMonitorConfigs() {
        // 门禁设备配置
        monitorConfigMap.put("ACCESS", new DeviceMonitorConfig(30, 10, 3, true));

        // 考勤设备配置
        monitorConfigMap.put("ATTENDANCE", new DeviceMonitorConfig(60, 15, 3, true));

        // 消费设备配置
        monitorConfigMap.put("CONSUME", new DeviceMonitorConfig(30, 8, 3, true));

        // 视频设备配置
        monitorConfigMap.put("VIDEO", new DeviceMonitorConfig(60, 20, 3, true));

        // 默认配置
        DeviceMonitorConfig defaultConfig = new DeviceMonitorConfig(60, 10, 3, false);
        monitorConfigMap.put("DEFAULT", defaultConfig);
    }

    /**
     * 启动健康监控
     */
    private void startHealthMonitoring() {
        // 定时健康检查
        scheduler.scheduleAtFixedRate(this::performBatchHealthCheck,
                                      30, 30, TimeUnit.SECONDS);

        // 定时性能指标收集
        scheduler.scheduleAtFixedRate(this::collectPerformanceMetricsBatch,
                                      60, 60, TimeUnit.SECONDS);

        // 定时清理过期数据
        scheduler.scheduleAtFixedRate(this::cleanupExpiredData,
                                      300, 300, TimeUnit.SECONDS);
    }

    /**
     * 批量健康检查
     */
    private void performBatchHealthCheck() {
        try {
            List<DeviceEntity> devices = deviceDao.selectList(null);
            for (DeviceEntity device : devices) {
                scheduler.submit(() -> checkDeviceHealth(device.getId().toString()));
            }
        } catch (Exception e) {
            log.error("[设备健康监控] 批量健康检查异常", e);
        }
    }

    /**
     * 批量收集性能指标
     */
    private void collectPerformanceMetricsBatch() {
        try {
            deviceHealthMap.keySet().forEach(deviceId ->
                scheduler.submit(() -> getDevicePerformance(deviceId)));
        } catch (Exception e) {
            log.error("[设备健康监控] 批量收集性能指标异常", e);
        }
    }

    /**
     * 清理过期数据
     * <p>
     * 清理超过1小时未更新的设备健康状态
     * 使用线程安全的ConcurrentHashMap确保并发安全
     * </p>
     */
    private void cleanupExpiredData() {
        try {
            // 清理超过1小时未更新的设备状态
            LocalDateTime expireTime = LocalDateTime.now().minusHours(1);

            // 记录清理前的数量
            int beforeCount = deviceHealthMap.size();

            // 使用迭代器安全删除，避免ConcurrentModificationException
            deviceHealthMap.entrySet().removeIf(entry -> {
                String deviceId = entry.getKey();
                LocalDateTime lastUpdateTime = deviceHealthUpdateTimeMap.get(deviceId);

                // 如果没有更新时间记录，或者更新时间超过1小时，则清理
                if (lastUpdateTime == null || lastUpdateTime.isBefore(expireTime)) {
                    // 同时清理更新时间映射
                    deviceHealthUpdateTimeMap.remove(deviceId);
                    log.debug("[设备健康监控] 清理过期设备健康状态, deviceId={}, lastUpdateTime={}",
                            deviceId, lastUpdateTime);
                    return true;
                }
                return false;
            });

            // 计算清理数量
            int cleanedCount = beforeCount - deviceHealthMap.size();
            if (cleanedCount > 0) {
                log.info("[设备健康监控] 清理过期数据完成, cleanedCount={}, remainingCount={}",
                        cleanedCount, deviceHealthMap.size());
            }

        } catch (Exception e) {
            log.error("[设备健康监控] 清理过期数据异常", e);
        }
    }

    /**
     * 检查设备是否在线
     * <p>
     * 综合判断设备在线状态：
     * 1. 检查最后心跳时间（通过DeviceStatusManager）
     * 2. 检查设备最后在线时间（lastOnlineTime字段）
     * 3. 检查设备状态（通过DeviceStatusManager）
     * </p>
     *
     * @param device 设备实体
     * @return 是否在线
     */
    private boolean isDeviceOnline(DeviceEntity device) {
        if (device == null) {
            return false;
        }

        try {
            // 1. 检查设备状态（通过DeviceStatusManager）
            if (deviceStatusManager != null) {
                boolean isOnlineByStatus = deviceStatusManager.isDeviceOnline(device.getId().toString());
                if (!isOnlineByStatus) {
                    log.debug("[设备健康监控] 设备状态为离线, deviceId={}", device.getId());
                    return false;
                }
            }

            // 2. 检查最后在线时间
            LocalDateTime lastOnlineTime = device.getLastOnlineTime();
            if (lastOnlineTime != null) {
                long minutesSinceLastOnline = ChronoUnit.MINUTES.between(lastOnlineTime, LocalDateTime.now());
                // 如果超过5分钟未在线，视为离线
                if (minutesSinceLastOnline > (DEFAULT_HEARTBEAT_TIMEOUT_SECONDS / 60)) {
                    log.debug("[设备健康监控] 设备最后在线时间超过阈值, deviceId={}, minutesSinceLastOnline={}",
                            device.getId(), minutesSinceLastOnline);
                    return false;
                }
            } else {
                // 如果没有最后在线时间记录，检查设备状态字段
                String deviceStatus = device.getStatus();
                if (deviceStatus == null || !"1".equals(deviceStatus)) {
                    log.debug("[设备健康监控] 设备状态字段显示离线, deviceId={}, status={}", device.getId(), deviceStatus);
                    return false;
                }
            }

            // 3. 检查设备启用标志
            Integer enabledFlag = device.getEnabledFlag();
            if (enabledFlag == null || enabledFlag == 0) {
                log.debug("[设备健康监控] 设备未启用, deviceId={}", device.getId());
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("[设备健康监控] 检查设备在线状态异常, deviceId={}", device.getId(), e);
            return false;
        }
    }

    /**
     * 执行健康检查
     * <p>
     * 通过设备通讯服务进行健康检查
     * 支持超时控制和异常处理
     * </p>
     *
     * @param device 设备实体
     * @return 健康状态
     */
    private DeviceHealthStatus performHealthCheck(DeviceEntity device) {
        try {
            // 1. 调用设备通讯服务进行健康检查
            if (gatewayServiceClient != null) {
                try {
                    // 异步调用健康检查接口，设置超时时间
                    @SuppressWarnings("unchecked")
                    CompletableFuture<ResponseDTO<Map<String, Object>>> healthCheckFuture =
                            CompletableFuture.supplyAsync(() -> {
                                try {
                                    return (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) gatewayServiceClient.callDeviceCommService(
                                            "/api/v1/device/health/check/" + device.getId(),
                                            HttpMethod.GET,
                                            null,
                                            Map.class
                                    );
                                } catch (Exception e) {
                                    log.warn("[设备健康监控] 健康检查接口调用异常, deviceId={}, error={}",
                                            device.getId(), e.getMessage());
                                    return null;
                                }
                            });

                    // 等待响应，设置超时时间
                    ResponseDTO<Map<String, Object>> healthCheckResponse = healthCheckFuture
                            .get(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                    if (healthCheckResponse != null && healthCheckResponse.isSuccess()) {
                        Map<String, Object> healthData = healthCheckResponse.getData();
                        if (healthData != null) {
                            // 解析健康检查结果
                            Object healthStatus = healthData.get("healthStatus");
                            if ("HEALTHY".equals(healthStatus)) {
                                return DeviceHealthStatus.HEALTHY;
                            } else if ("WARNING".equals(healthStatus)) {
                                return DeviceHealthStatus.WARNING;
                            } else if ("ERROR".equals(healthStatus)) {
                                return DeviceHealthStatus.ERROR;
                            }
                        }
                    }
                } catch (java.util.concurrent.TimeoutException e) {
                    log.warn("[设备健康监控] 健康检查超时, deviceId={}, timeout={}s",
                            device.getId(), HEALTH_CHECK_TIMEOUT_SECONDS);
                    return DeviceHealthStatus.WARNING;
                } catch (Exception e) {
                    log.warn("[设备健康监控] 健康检查接口调用失败, deviceId={}, error={}",
                            device.getId(), e.getMessage());
                    // 健康检查失败不影响设备状态，继续使用其他方式判断
                }
            }

            // 2. 降级处理：检查设备响应性
            boolean isResponsive = checkDeviceResponsiveness(device);
            if (!isResponsive) {
                return DeviceHealthStatus.WARNING;
            }

            // 3. 检查设备状态
            if (device.getStatus() == null || !"1".equals(device.getStatus())) {
                return DeviceHealthStatus.ERROR;
            }

            return DeviceHealthStatus.HEALTHY;

        } catch (Exception e) {
            log.error("[设备健康监控] 健康检查执行异常, deviceId={}", device.getId(), e);
            return DeviceHealthStatus.ERROR;
        }
    }

    /**
     * 检查设备响应性
     * <p>
     * 通过发送心跳请求测试设备响应性
     * 检查响应时间是否在阈值范围内
     * </p>
     *
     * @param device 设备实体
     * @return 是否响应正常
     */
    private boolean checkDeviceResponsiveness(DeviceEntity device) {
        try {
            // 1. 发送心跳请求到设备（通过设备通讯服务）
            if (gatewayServiceClient != null) {
                long startTime = System.currentTimeMillis();

                try {
                    // 异步发送心跳请求
                    @SuppressWarnings("unchecked")
                    CompletableFuture<ResponseDTO<Map<String, Object>>> heartbeatFuture =
                            CompletableFuture.supplyAsync(() -> {
                                try {
                                    // 调用设备通讯服务的心跳接口
                                    Map<String, Object> heartbeatRequest = new HashMap<>();
                                    heartbeatRequest.put("deviceId", device.getId());
                                    heartbeatRequest.put("timestamp", System.currentTimeMillis());

                                    return (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) gatewayServiceClient.callDeviceCommService(
                                            "/api/v1/device/heartbeat",
                                            HttpMethod.POST,
                                            heartbeatRequest,
                                            Map.class
                                    );
                                } catch (Exception e) {
                                    log.debug("[设备健康监控] 心跳请求发送异常, deviceId={}, error={}",
                                            device.getId(), e.getMessage());
                                    return null;
                                }
                            });

                    // 等待响应，设置超时时间（响应时间阈值）
                    ResponseDTO<Map<String, Object>> heartbeatResponse = heartbeatFuture
                            .get(RESPONSE_TIME_THRESHOLD_MS, TimeUnit.MILLISECONDS);

                    long responseTime = System.currentTimeMillis() - startTime;

                    // 2. 检查响应时间和响应结果
                    if (heartbeatResponse != null && heartbeatResponse.isSuccess()) {
                        // 响应时间在阈值内，且响应成功
                        if (responseTime <= RESPONSE_TIME_THRESHOLD_MS) {
                            log.debug("[设备健康监控] 设备响应正常, deviceId={}, responseTime={}ms",
                                    device.getId(), responseTime);
                            return true;
                        } else {
                            log.warn("[设备健康监控] 设备响应时间过长, deviceId={}, responseTime={}ms, threshold={}ms",
                                    device.getId(), responseTime, RESPONSE_TIME_THRESHOLD_MS);
                            return false;
                        }
                    } else {
                        log.debug("[设备健康监控] 设备心跳响应失败, deviceId={}, response={}",
                                device.getId(), heartbeatResponse != null ? heartbeatResponse.getMessage() : "null");
                        return false;
                    }
                } catch (java.util.concurrent.TimeoutException e) {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.warn("[设备健康监控] 设备响应超时, deviceId={}, responseTime={}ms, threshold={}ms",
                            device.getId(), responseTime, RESPONSE_TIME_THRESHOLD_MS);
                    return false;
                } catch (Exception e) {
                    log.debug("[设备健康监控] 设备响应性检查异常, deviceId={}, error={}",
                            device.getId(), e.getMessage());
                    return false;
                }
            }

            // 2. 降级处理：检查最后在线时间
            LocalDateTime lastOnlineTime = device.getLastOnlineTime();
            if (lastOnlineTime != null) {
                long minutesSinceLastOnline = ChronoUnit.MINUTES.between(lastOnlineTime, LocalDateTime.now());
                // 如果5分钟内在线过，认为设备响应正常
                return minutesSinceLastOnline <= (DEFAULT_HEARTBEAT_TIMEOUT_SECONDS / 60);
            }

            // 3. 默认返回true（避免误判）
            return true;

        } catch (Exception e) {
            log.debug("[设备健康监控] 设备响应性检查失败, deviceId={}", device.getId(), e);
            return false;
        }
    }

    /**
     * 收集性能指标
     * <p>
     * 通过设备通讯服务获取设备性能指标
     * 收集CPU使用率、内存使用量、网络延迟、响应时间、错误率
     * </p>
     *
     * @param device 设备实体
     * @return 性能指标
     */
    private DevicePerformanceMetrics collectPerformanceMetrics(DeviceEntity device) {
        try {
            // 1. 通过设备通讯服务获取性能指标
            if (gatewayServiceClient != null) {
                try {
                    @SuppressWarnings("unchecked")
                    ResponseDTO<Map<String, Object>> metricsResponse = (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) gatewayServiceClient.callDeviceCommService(
                            "/api/v1/device/metrics/" + device.getId(),
                            HttpMethod.GET,
                            null,
                            Map.class
                    );

                    if (metricsResponse != null && metricsResponse.isSuccess()) {
                        Map<String, Object> metricsData = metricsResponse.getData();
                        if (metricsData != null) {
                            // 解析性能指标数据
                            double cpuUsage = getDoubleValue(metricsData.get("cpuUsage"), 0.0);
                            long memoryUsage = getLongValue(metricsData.get("memoryUsage"), 0L);
                            double networkLatency = getDoubleValue(metricsData.get("networkLatency"), 0.0);
                            int responseTime = getIntValue(metricsData.get("responseTime"), 0);
                            int errorRate = getIntValue(metricsData.get("errorRate"), 0);

                            log.debug("[设备健康监控] 获取设备性能指标成功, deviceId={}, cpuUsage={}%, memoryUsage={}MB, networkLatency={}ms",
                                    device.getId(), cpuUsage, memoryUsage / (1024 * 1024), networkLatency);

                            return new DevicePerformanceMetrics(
                                    cpuUsage,
                                    memoryUsage,
                                    networkLatency,
                                    responseTime,
                                    errorRate
                            );
                        }
                    }
                } catch (Exception e) {
                    log.warn("[设备健康监控] 获取设备性能指标失败, deviceId={}, error={}",
                            device.getId(), e.getMessage());
                    // 性能指标获取失败不影响健康检查，使用降级处理
                }
            }

            // 2. 降级处理：返回默认值（某些设备可能不支持性能指标）
            log.debug("[设备健康监控] 使用默认性能指标, deviceId={}", device.getId());
            return new DevicePerformanceMetrics(
                    0.0,  // CPU使用率（未知）
                    0L,   // 内存使用量（未知）
                    0.0,  // 网络延迟（未知）
                    0,    // 响应时间（未知）
                    0     // 错误率（未知）
            );

        } catch (Exception e) {
            log.error("[设备健康监控] 收集性能指标异常, deviceId={}", device.getId(), e);
            return null;
        }
    }

    /**
     * 获取Double值（辅助方法）
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
            log.debug("[设备健康监控] Double值解析失败，使用默认值: value={}, defaultValue={}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 获取Long值（辅助方法）
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
            log.debug("[设备健康监控] Long值解析失败，使用默认值: value={}, defaultValue={}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 获取Int值（辅助方法）
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
            log.debug("[设备健康监控] Integer值解析失败，使用默认值: value={}, defaultValue={}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 更新设备健康状态
     * <p>
     * 同时更新健康状态和更新时间，用于后续的过期数据清理
     * </p>
     *
     * @param deviceId 设备ID
     * @param healthStatus 健康状态
     */
    private void updateDeviceHealthStatus(String deviceId, DeviceHealthStatus healthStatus) {
        DeviceHealthStatus oldStatus = deviceHealthMap.get(deviceId);
        deviceHealthMap.put(deviceId, healthStatus);
        deviceHealthUpdateTimeMap.put(deviceId, LocalDateTime.now()); // 更新状态变更时间

        // 记录状态变更日志
        if (oldStatus != healthStatus) {
            log.info("[设备健康监控] 设备状态更新, deviceId={}, oldStatus={}, newStatus={}",
                    deviceId, oldStatus != null ? oldStatus.getDescription() : "null", healthStatus.getDescription());

            // 发送状态变更通知（如果状态发生变化）
            sendHealthStatusChangeNotification(deviceId, oldStatus, healthStatus);
        } else {
            log.debug("[设备健康监控] 设备状态未变化, deviceId={}, status={}", deviceId, healthStatus.getDescription());
        }
    }

    /**
     * 发送健康状态变更通知
     * <p>
     * 当设备健康状态发生变化时发送通知
     * 避免频繁通知（相同状态变更5分钟内不重复通知）
     * </p>
     *
     * @param deviceId 设备ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    private void sendHealthStatusChangeNotification(String deviceId, DeviceHealthStatus oldStatus,
                                                     DeviceHealthStatus newStatus) {
        // 架构设计说明：
        // Manager类在microservices-common中，遵循CLAUDE.md规范，不应该直接依赖Service层
        // 因此通知发送功能需要在Service层实现，通过事件机制或回调方式触发
        //
        // 使用方式：
        // 1. 在Service层注入DeviceHealthMonitor实例
        // 2. 监听设备健康状态变更事件（通过事件监听器或回调接口）
        // 3. 在事件处理中调用NotificationManager发送通知
        //
        // 示例代码（在Service层）：
        // @EventListener
        // public void handleDeviceHealthStatusChange(DeviceHealthStatusChangeEvent event) {
        //     notificationManager.sendDeviceHealthStatusChangeNotification(
        //         event.getDeviceId(), event.getOldStatus(), event.getNewStatus()
        //     );
        // }

        log.info("[设备健康监控] 设备健康状态变更通知, deviceId={}, oldStatus={}, newStatus={}",
                deviceId, oldStatus != null ? oldStatus.getDescription() : "null", newStatus.getDescription());

        // 注意：实际通知发送需要在Service层实现，通过事件机制触发
        // 这里只记录日志，保持Manager层的独立性
    }

    /**
     * 获取监控配置
     */
    private DeviceMonitorConfig getMonitorConfig(String deviceType) {
        return monitorConfigMap.getOrDefault(deviceType, monitorConfigMap.get("DEFAULT"));
    }

    /**
     * 关闭监控器
     */
    public void shutdown() {
        log.info("[设备健康监控] 关闭设备健康监控器");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.debug("[设备健康监控] 调度器关闭被中断");
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

