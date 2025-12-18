package net.lab1024.sa.device.comm.service.impl;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.device.comm.monitor.HighPrecisionDeviceMonitor;
import net.lab1024.sa.device.comm.service.HighPrecisionDeviceMonitorService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 高精度设备监控服务实现
 * <p>
 * 严格遵循四层架构规范的Service层实现：
 * - 位于Service层，负责业务接口定义和事务管理
 * - 使用@Resource注解进行依赖注入
 * - 调用Monitor层处理复杂监控逻辑
 * - 处理事务边界和异常转换
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@Schema(description = "高精度设备监控服务实现")
public class HighPrecisionDeviceMonitorServiceImpl implements HighPrecisionDeviceMonitorService {

    @Resource
    private HighPrecisionDeviceMonitor highPrecisionDeviceMonitor;

    @Override
    public HighPrecisionDeviceMonitor.DeviceStatusSnapshot getDeviceRealTimeStatus(String deviceId) {
        try {
            log.debug("[高精度监控服务] 获取设备实时状态, deviceId={}", deviceId);

            // 参数验证
            if (deviceId == null || deviceId.trim().isEmpty()) {
                throw new IllegalArgumentException("设备ID不能为空");
            }

            // 调用Monitor层处理
            HighPrecisionDeviceMonitor.DeviceStatusSnapshot snapshot =
                    highPrecisionDeviceMonitor.getDeviceRealTimeStatus(deviceId);

            if (snapshot == null) {
                log.warn("[高精度监控服务] 设备状态不存在, deviceId={}", deviceId);
                // 返回默认状态快照
                snapshot = createDefaultSnapshot(deviceId);
            }

            return snapshot;

        } catch (IllegalArgumentException e) {
            log.error("[高精度监控服务] 参数错误, deviceId={}", deviceId, e);
            throw e;
        } catch (Exception e) {
            log.error("[高精度监控服务] 获取设备实时状态异常, deviceId={}", deviceId, e);
            throw new RuntimeException("获取设备实时状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<HighPrecisionDeviceMonitor.DeviceStatusSnapshot> monitorDeviceAsync(String deviceId) {
        try {
            log.debug("[高精度监控服务] 异步监控设备, deviceId={}", deviceId);

            // 参数验证
            if (deviceId == null || deviceId.trim().isEmpty()) {
                return CompletableFuture.failedFuture(
                        new IllegalArgumentException("设备ID不能为空"));
            }

            // 调用Monitor层异步处理
            return highPrecisionDeviceMonitor.monitorDeviceAsync(deviceId)
                    .exceptionally(e -> {
                        log.error("[高精度监控服务] 异步监控设备失败, deviceId={}", deviceId, e);
                        throw new RuntimeException("异步监控设备失败: " + e.getMessage(), e);
                    });

        } catch (IllegalArgumentException e) {
            log.error("[高精度监控服务] 参数错误, deviceId={}", deviceId, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("[高精度监控服务] 启动异步监控异常, deviceId={}", deviceId, e);
            return CompletableFuture.failedFuture(
                    new RuntimeException("启动异步监控失败: " + e.getMessage(), e));
        }
    }

    @Override
    public List<HighPrecisionDeviceMonitor.DeviceStatusSnapshot> getDeviceStatusHistory(String deviceId, int count) {
        try {
            log.debug("[高精度监控服务] 获取设备状态历史, deviceId={}, count={}", deviceId, count);

            // 参数验证
            if (deviceId == null || deviceId.trim().isEmpty()) {
                throw new IllegalArgumentException("设备ID不能为空");
            }
            if (count <= 0 || count > 1000) {
                throw new IllegalArgumentException("获取数量必须在1-1000之间");
            }

            // 调用Monitor层处理
            List<HighPrecisionDeviceMonitor.DeviceStatusSnapshot> history =
                    highPrecisionDeviceMonitor.getDeviceStatusHistory(deviceId, count);

            if (history == null || history.isEmpty()) {
                log.debug("[高精度监控服务] 设备状态历史为空, deviceId={}", deviceId);
                return new ArrayList<>();
            }

            return history;

        } catch (IllegalArgumentException e) {
            log.error("[高精度监控服务] 参数错误, deviceId={}, count={}", deviceId, count, e);
            throw e;
        } catch (Exception e) {
            log.error("[高精度监控服务] 获取设备状态历史异常, deviceId={}, count={}", deviceId, count, e);
            throw new RuntimeException("获取设备状态历史失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getPerformanceStatistics() {
        try {
            log.debug("[高精度监控服务] 获取性能统计");

            // 调用Monitor层处理
            Map<String, Object> statistics = highPrecisionDeviceMonitor.getPerformanceStatistics();

            if (statistics == null) {
                log.warn("[高精度监控服务] 性能统计数据为空");
                return createDefaultStatistics();
            }

            // 添加服务层统计信息
            enhanceStatistics(statistics);

            return statistics;

        } catch (Exception e) {
            log.error("[高精度监控服务] 获取性能统计异常", e);
            throw new RuntimeException("获取性能统计失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, HighPrecisionDeviceMonitor.DeviceStatusSnapshot> batchMonitorDevices(List<String> deviceIds) {
        try {
            log.debug("[高精度监控服务] 批量监控设备, deviceCount={}",
                    deviceIds != null ? deviceIds.size() : 0);

            // 参数验证
            if (deviceIds == null || deviceIds.isEmpty()) {
                throw new IllegalArgumentException("设备ID列表不能为空");
            }
            if (deviceIds.size() > 100) {
                throw new IllegalArgumentException("批量监控设备数量不能超过100个");
            }

            // 去重处理
            List<String> uniqueDeviceIds = deviceIds.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            if (uniqueDeviceIds.isEmpty()) {
                log.warn("[高精度监控服务] 批量监控设备列表为空（去重后）");
                return new HashMap<>();
            }

            // 调用Monitor层处理
            Map<String, HighPrecisionDeviceMonitor.DeviceStatusSnapshot> results =
                    highPrecisionDeviceMonitor.batchMonitorDevices(uniqueDeviceIds);

            log.info("[高精度监控服务] 批量监控完成, requestCount={}, resultCount={}",
                    uniqueDeviceIds.size(), results.size());

            return results;

        } catch (IllegalArgumentException e) {
            log.error("[高精度监控服务] 参数错误, deviceCount={}",
                    deviceIds != null ? deviceIds.size() : 0, e);
            throw e;
        } catch (Exception e) {
            log.error("[高精度监控服务] 批量监控设备异常, deviceCount={}",
                    deviceIds != null ? deviceIds.size() : 0, e);
            throw new RuntimeException("批量监控设备失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateDeviceMonitorConfig(String deviceId, HighPrecisionDeviceMonitor.DeviceMonitorConfig config) {
        try {
            log.debug("[高精度监控服务] 更新设备监控配置, deviceId={}", deviceId);

            // 参数验证
            if (deviceId == null || deviceId.trim().isEmpty()) {
                throw new IllegalArgumentException("设备ID不能为空");
            }
            if (config == null) {
                throw new IllegalArgumentException("监控配置不能为空");
            }

            // 配置有效性验证
            validateMonitorConfig(config);

            // 调用Monitor层处理
            highPrecisionDeviceMonitor.updateDeviceMonitorConfig(deviceId, config);

            log.info("[高精度监控服务] 设备监控配置更新成功, deviceId={}, interval={}ms",
                    deviceId, config.getMonitorIntervalMs());

        } catch (IllegalArgumentException e) {
            log.error("[高精度监控服务] 参数错误, deviceId={}", deviceId, e);
            throw e;
        } catch (Exception e) {
            log.error("[高精度监控服务] 更新设备监控配置异常, deviceId={}", deviceId, e);
            throw new RuntimeException("更新设备监控配置失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getDeviceStatusOverview() {
        try {
            log.debug("[高精度监控服务] 获取设备状态概览");

            Map<String, Object> overview = new HashMap<>();

            // 获取性能统计
            Map<String, Object> performanceStats = getPerformanceStatistics();
            overview.put("performanceStatistics", performanceStats);

            // 获取问题设备列表
            List<String> problematicDevices = getProblematicDevices();
            overview.put("problematicDeviceCount", problematicDevices.size());
            overview.put("problematicDevices", problematicDevices);

            // 添加概览时间
            overview.put("overviewTime", new Date());
            overview.put("serviceVersion", "1.0.0");

            return overview;

        } catch (Exception e) {
            log.error("[高精度监控服务] 获取设备状态概览异常", e);
            throw new RuntimeException("获取设备状态概览失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getProblematicDevices() {
        try {
            log.debug("[高精度监控服务] 获取问题设备列表");

            // 获取性能统计
            Map<String, Object> statistics = getPerformanceStatistics();

            // 分析问题设备（简化实现，实际可基于更复杂的逻辑）
            List<String> problematicDevices = new ArrayList<>();

            // 这里可以扩展更复杂的问题设备识别逻辑
            // 例如：基于错误率、响应时间、健康等级等
            Long failedMonitors = (Long) statistics.get("failedMonitors");
            if (failedMonitors != null && failedMonitors > 0) {
                // 这里应该有更精确的逻辑识别具体的问题设备
                log.warn("[高精度监控服务] 发现监控失败设备数量: {}", failedMonitors);
            }

            return problematicDevices;

        } catch (Exception e) {
            log.error("[高精度监控服务] 获取问题设备列表异常", e);
            throw new RuntimeException("获取问题设备列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void startHighPrecisionMonitoring(List<String> deviceIds) {
        try {
            log.debug("[高精度监控服务] 启动高精度监控, deviceCount={}",
                    deviceIds != null ? deviceIds.size() : 0);

            // 参数验证
            if (deviceIds == null || deviceIds.isEmpty()) {
                throw new IllegalArgumentException("设备ID列表不能为空");
            }

            // 为每个设备配置高精度监控
            HighPrecisionDeviceMonitor.DeviceMonitorConfig highPrecisionConfig =
                    new HighPrecisionDeviceMonitor.DeviceMonitorConfig(
                            100,    // 100ms高精度间隔
                            500,    // 500ms超时
                            1000,   // 1000条历史记录
                            true,   // 启用高精度
                            80.0,   // CPU阈值80%
                            1024L * 1024 * 1024, // 内存阈值1GB
                            100.0   // 延迟阈值100ms
                    );

            for (String deviceId : deviceIds) {
                if (deviceId != null && !deviceId.trim().isEmpty()) {
                    updateDeviceMonitorConfig(deviceId.trim(), highPrecisionConfig);
                }
            }

            log.info("[高精度监控服务] 高精度监控启动完成, deviceCount={}", deviceIds.size());

        } catch (IllegalArgumentException e) {
            log.error("[高精度监控服务] 参数错误, deviceCount={}",
                    deviceIds != null ? deviceIds.size() : 0, e);
            throw e;
        } catch (Exception e) {
            log.error("[高精度监控服务] 启动高精度监控异常, deviceCount={}",
                    deviceIds != null ? deviceIds.size() : 0, e);
            throw new RuntimeException("启动高精度监控失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void stopHighPrecisionMonitoring(List<String> deviceIds) {
        try {
            log.debug("[高精度监控服务] 停止高精度监控, deviceCount={}",
                    deviceIds != null ? deviceIds.size() : 0);

            // 参数验证
            if (deviceIds == null || deviceIds.isEmpty()) {
                throw new IllegalArgumentException("设备ID列表不能为空");
            }

            // 为每个设备配置普通精度监控（停止高精度）
            HighPrecisionDeviceMonitor.DeviceMonitorConfig normalConfig =
                    new HighPrecisionDeviceMonitor.DeviceMonitorConfig(
                            1000,   // 1s普通精度间隔
                            1000,   // 1s超时
                            200,    // 200条历史记录
                            false,  // 禁用高精度
                            70.0,   // CPU阈值70%
                            512L * 1024 * 1024,   // 内存阈值512MB
                            200.0   // 延迟阈值200ms
                    );

            for (String deviceId : deviceIds) {
                if (deviceId != null && !deviceId.trim().isEmpty()) {
                    updateDeviceMonitorConfig(deviceId.trim(), normalConfig);
                }
            }

            log.info("[高精度监控服务] 高精度监控停止完成, deviceCount={}", deviceIds.size());

        } catch (IllegalArgumentException e) {
            log.error("[高精度监控服务] 参数错误, deviceCount={}",
                    deviceIds != null ? deviceIds.size() : 0, e);
            throw e;
        } catch (Exception e) {
            log.error("[高精度监控服务] 停止高精度监控异常, deviceCount={}",
                    deviceIds != null ? deviceIds.size() : 0, e);
            throw new RuntimeException("停止高精度监控失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 创建默认状态快照
     */
    private HighPrecisionDeviceMonitor.DeviceStatusSnapshot createDefaultSnapshot(String deviceId) {
        HighPrecisionDeviceMonitor.DeviceStatusSnapshot snapshot =
                new HighPrecisionDeviceMonitor.DeviceStatusSnapshot();
        snapshot.setDeviceId(deviceId);
        snapshot.setStatus(HighPrecisionDeviceMonitor.DeviceStatus.UNKNOWN);
        snapshot.setHealthLevel("未知");
        snapshot.setResponseTimeMs(0);
        snapshot.setTimestamp(java.time.LocalDateTime.now());
        snapshot.setExtendedAttributes(new HashMap<>());
        return snapshot;
    }

    /**
     * 创建默认统计信息
     */
    private Map<String, Object> createDefaultStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalMonitors", 0L);
        statistics.put("successMonitors", 0L);
        statistics.put("failedMonitors", 0L);
        statistics.put("successRate", 0.0);
        statistics.put("failedRate", 0.0);
        statistics.put("cacheSize", 0);
        statistics.put("historySize", 0);
        statistics.put("averageResponseTime", 0.0);
        return statistics;
    }

    /**
     * 增强统计信息
     */
    private void enhanceStatistics(Map<String, Object> statistics) {
        // 添加服务层信息
        statistics.put("serviceStartTime", new Date());
        statistics.put("serviceVersion", "1.0.0");
        statistics.put("javaVersion", System.getProperty("java.version"));
        statistics.put("availableProcessors", Runtime.getRuntime().availableProcessors());

        // 添加内存信息
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("totalMemoryMB", totalMemory / 1024 / 1024);
        memoryInfo.put("usedMemoryMB", usedMemory / 1024 / 1024);
        memoryInfo.put("freeMemoryMB", freeMemory / 1024 / 1024);
        memoryInfo.put("maxMemoryMB", maxMemory / 1024 / 1024);
        memoryInfo.put("memoryUsagePercent", Math.round(usedMemory * 100.0 / maxMemory * 100.0) / 100.0);

        statistics.put("memoryInfo", memoryInfo);
    }

    /**
     * 验证监控配置
     */
    private void validateMonitorConfig(HighPrecisionDeviceMonitor.DeviceMonitorConfig config) {
        if (config.getMonitorIntervalMs() <= 0) {
            throw new IllegalArgumentException("监控间隔必须大于0");
        }
        if (config.getTimeoutMs() <= 0) {
            throw new IllegalArgumentException("超时时间必须大于0");
        }
        if (config.getMaxHistorySize() <= 0 || config.getMaxHistorySize() > 10000) {
            throw new IllegalArgumentException("历史记录大小必须在1-10000之间");
        }
        if (config.getCpuThreshold() < 0 || config.getCpuThreshold() > 100) {
            throw new IllegalArgumentException("CPU阈值必须在0-100之间");
        }
        if (config.getMemoryThreshold() <= 0) {
            throw new IllegalArgumentException("内存阈值必须大于0");
        }
        if (config.getLatencyThreshold() < 0) {
            throw new IllegalArgumentException("延迟阈值不能为负数");
        }
    }
}
