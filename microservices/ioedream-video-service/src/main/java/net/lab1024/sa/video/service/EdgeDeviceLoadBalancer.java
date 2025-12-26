package net.lab1024.sa.video.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.model.HardwareSpec;
import net.lab1024.sa.video.edge.model.InferenceRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 边缘设备负载均衡服务
 *
 * 功能特性:
 * 1. 智能任务分配 - 根据设备负载分配推理任务
 * 2. 多因素决策 - 考虑算力、负载、网络延迟、优先级
 * 3. 设备健康监控 - 实时监控边缘设备状态
 * 4. 负载统计 - 设备利用率统计
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class EdgeDeviceLoadBalancer {

    /**
     * 边缘设备注册表
     * Key: deviceId
     * Value: 设备状态信息
     */
    private final ConcurrentHashMap<String, EdgeDeviceStatus> deviceRegistry = new ConcurrentHashMap<>();

    /**
     * 选择最佳边缘设备执行推理任务
     *
     * @param request 推理请求
     * @param availableDevices 可用设备列表
     * @return 最佳设备
     */
    public EdgeDevice selectBestDevice(InferenceRequest request, List<EdgeDevice> availableDevices) {
        if (availableDevices == null || availableDevices.isEmpty()) {
            log.warn("[负载均衡] 无可用设备");
            return null;
        }

        if (availableDevices.size() == 1) {
            return availableDevices.get(0);
        }

        log.debug("[负载均衡] 开始选择最佳设备: requestType={}, deviceCount={}",
                request.getRequestType(), availableDevices.size());

        // 计算每个设备的得分
        Map<EdgeDevice, DeviceScore> deviceScores = new HashMap<>();
        for (EdgeDevice device : availableDevices) {
            DeviceScore score = calculateDeviceScore(device, request);
            deviceScores.put(device, score);
        }

        // 按得分排序，选择最佳设备
        EdgeDevice bestDevice = deviceScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (bestDevice != null) {
            DeviceScore score = deviceScores.get(bestDevice);
            log.info("[负载均衡] 选择最佳设备: deviceId={}, score={}, " +
                            "cpuLoad={}, memoryLoad={}, networkLatency={}",
                    bestDevice.getDeviceId(),
                    String.format("%.2f", score.getTotalScore()),
                    score.getCpuLoad(),
                    score.getMemoryLoad(),
                    score.getNetworkLatency());

            // 更新设备任务计数
            incrementDeviceTaskCount(bestDevice.getDeviceId());
        }

        return bestDevice;
    }

    /**
     * 计算设备得分（多因素综合评估）
     *
     * @param device 边缘设备
     * @param request 推理请求
     * @return 设备得分
     */
    private DeviceScore calculateDeviceScore(EdgeDevice device, InferenceRequest request) {
        EdgeDeviceStatus status = deviceRegistry.get(device.getDeviceId());
        if (status == null) {
            status = initializeDeviceStatus(device);
        }

        HardwareSpec spec = device.getHardwareSpec();

        // 1. CPU负载得分（0-30分）
        double cpuScore = calculateCpuScore(status.getCpuUsage());

        // 2. 内存负载得分（0-25分）
        double memoryScore = calculateMemoryScore(status.getMemoryUsage());

        // 3. GPU利用率得分（0-25分）
        double gpuScore = calculateGpuScore(status.getGpuUsage());

        // 4. 网络延迟得分（0-10分）
        double networkScore = calculateNetworkScore(status.getNetworkLatency());

        // 5. 当前任务数得分（0-10分）
        double taskScore = calculateTaskScore(status.getCurrentTaskCount());

        // 总分（0-100分，越高越好）
        double totalScore = cpuScore + memoryScore + gpuScore + networkScore + taskScore;

        return DeviceScore.builder()
                .deviceId(device.getDeviceId())
                .cpuLoad(status.getCpuUsage())
                .memoryLoad(status.getMemoryUsage())
                .gpuLoad(status.getGpuUsage())
                .networkLatency(status.getNetworkLatency())
                .currentTaskCount(status.getCurrentTaskCount())
                .cpuScore(cpuScore)
                .memoryScore(memoryScore)
                .gpuScore(gpuScore)
                .networkScore(networkScore)
                .taskScore(taskScore)
                .totalScore(totalScore)
                .build();
    }

    /**
     * 计算CPU负载得分
     * - 0-50%: 30分（最优）
     * - 50-70%: 20分
     * - 70-85%: 10分
     * - 85-100%: 0分（过载）
     */
    private double calculateCpuScore(double cpuUsage) {
        if (cpuUsage <= 50) return 30.0;
        if (cpuUsage <= 70) return 20.0;
        if (cpuUsage <= 85) return 10.0;
        return 0.0;
    }

    /**
     * 计算内存负载得分
     * - 0-60%: 25分（最优）
     * - 60-80%: 15分
     * - 80-90%: 5分
     * - 90-100%: 0分（过载）
     */
    private double calculateMemoryScore(double memoryUsage) {
        if (memoryUsage <= 60) return 25.0;
        if (memoryUsage <= 80) return 15.0;
        if (memoryUsage <= 90) return 5.0;
        return 0.0;
    }

    /**
     * 计算GPU利用率得分
     * - 0-60%: 25分（最优）
     * - 60-80%: 15分
     * - 80-95%: 5分
     * - 95-100%: 0分（过载）
     */
    private double calculateGpuScore(double gpuUsage) {
        if (gpuUsage <= 60) return 25.0;
        if (gpuUsage <= 80) return 15.0;
        if (gpuUsage <= 95) return 5.0;
        return 0.0;
    }

    /**
     * 计算网络延迟得分
     * - 0-10ms: 10分（最优）
     * - 10-50ms: 8分
     * - 50-100ms: 5分
     * - 100-200ms: 2分
     * - >200ms: 0分（高延迟）
     */
    private double calculateNetworkScore(long networkLatency) {
        if (networkLatency <= 10) return 10.0;
        if (networkLatency <= 50) return 8.0;
        if (networkLatency <= 100) return 5.0;
        if (networkLatency <= 200) return 2.0;
        return 0.0;
    }

    /**
     * 计算任务数得分
     * - 0-2个任务: 10分（空闲）
     * - 3-5个任务: 7分
     * - 6-8个任务: 4分
     * - 9-10个任务: 2分
     * - >10个任务: 0分（繁忙）
     */
    private double calculateTaskScore(int currentTaskCount) {
        if (currentTaskCount <= 2) return 10.0;
        if (currentTaskCount <= 5) return 7.0;
        if (currentTaskCount <= 8) return 4.0;
        if (currentTaskCount <= 10) return 2.0;
        return 0.0;
    }

    /**
     * 初始化设备状态
     *
     * @param device 边缘设备
     * @return 设备状态
     */
    private EdgeDeviceStatus initializeDeviceStatus(EdgeDevice device) {
        EdgeDeviceStatus status = EdgeDeviceStatus.builder()
                .deviceId(device.getDeviceId())
                .deviceName(device.getDeviceName())
                .cpuUsage(0.0)
                .memoryUsage(0.0)
                .gpuUsage(0.0)
                .networkLatency(0L)
                .currentTaskCount(0)
                .totalTaskCount(0L)
                .status("ONLINE")
                .lastUpdateTime(LocalDateTime.now())
                .build();

        deviceRegistry.put(device.getDeviceId(), status);
        return status;
    }

    /**
     * 增加设备任务计数
     *
     * @param deviceId 设备ID
     */
    public void incrementDeviceTaskCount(String deviceId) {
        EdgeDeviceStatus status = deviceRegistry.get(deviceId);
        if (status != null) {
            status.setCurrentTaskCount(status.getCurrentTaskCount() + 1);
            status.setTotalTaskCount(status.getTotalTaskCount() + 1);
            status.setLastUpdateTime(LocalDateTime.now());
        }
    }

    /**
     * 减少设备任务计数
     *
     * @param deviceId 设备ID
     */
    public void decrementDeviceTaskCount(String deviceId) {
        EdgeDeviceStatus status = deviceRegistry.get(deviceId);
        if (status != null && status.getCurrentTaskCount() > 0) {
            status.setCurrentTaskCount(status.getCurrentTaskCount() - 1);
            status.setLastUpdateTime(LocalDateTime.now());
        }
    }

    /**
     * 更新设备负载信息
     *
     * @param deviceId 设备ID
     * @param cpuUsage CPU使用率
     * @param memoryUsage 内存使用率
     * @param gpuUsage GPU使用率
     * @param networkLatency 网络延迟
     */
    public void updateDeviceLoad(String deviceId, Double cpuUsage, Double memoryUsage,
                                  Double gpuUsage, Long networkLatency) {
        EdgeDeviceStatus status = deviceRegistry.get(deviceId);
        if (status != null) {
            status.setCpuUsage(cpuUsage);
            status.setMemoryUsage(memoryUsage);
            status.setGpuUsage(gpuUsage);
            status.setNetworkLatency(networkLatency);
            status.setLastUpdateTime(LocalDateTime.now());
        }
    }

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    public EdgeDeviceStatus getDeviceStatus(String deviceId) {
        return deviceRegistry.get(deviceId);
    }

    /**
     * 获取所有设备状态
     *
     * @return 设备状态列表
     */
    public List<EdgeDeviceStatus> getAllDeviceStatus() {
        return new ArrayList<>(deviceRegistry.values());
    }

    /**
     * 获取设备利用率统计
     *
     * @return 设备利用率统计
     */
    public DeviceUtilizationStatistics getUtilizationStatistics() {
        List<EdgeDeviceStatus> allStatus = getAllDeviceStatus();

        if (allStatus.isEmpty()) {
            return DeviceUtilizationStatistics.builder()
                    .totalDevices(0)
                    .onlineDevices(0)
                    .avgCpuUsage(0.0)
                    .avgMemoryUsage(0.0)
                    .avgGpuUsage(0.0)
                    .avgTaskCount(0.0)
                    .build();
        }

        int onlineCount = (int) allStatus.stream()
                .filter(s -> "ONLINE".equals(s.getStatus()))
                .count();

        double avgCpu = allStatus.stream()
                .mapToDouble(EdgeDeviceStatus::getCpuUsage)
                .average()
                .orElse(0.0);

        double avgMemory = allStatus.stream()
                .mapToDouble(EdgeDeviceStatus::getMemoryUsage)
                .average()
                .orElse(0.0);

        double avgGpu = allStatus.stream()
                .mapToDouble(EdgeDeviceStatus::getGpuUsage)
                .average()
                .orElse(0.0);

        double avgTasks = allStatus.stream()
                .mapToInt(EdgeDeviceStatus::getCurrentTaskCount)
                .average()
                .orElse(0.0);

        return DeviceUtilizationStatistics.builder()
                .totalDevices(allStatus.size())
                .onlineDevices(onlineCount)
                .avgCpuUsage(avgCpu)
                .avgMemoryUsage(avgMemory)
                .avgGpuUsage(avgGpu)
                .avgTaskCount(avgTasks)
                .build();
    }

    /**
     * 设备状态
     */
    @lombok.Data
    @lombok.Builder
    public static class EdgeDeviceStatus {
        private String deviceId;
        private String deviceName;
        private Double cpuUsage;         // CPU使用率（0-100）
        private Double memoryUsage;      // 内存使用率（0-100）
        private Double gpuUsage;         // GPU使用率（0-100）
        private Long networkLatency;     // 网络延迟（毫秒）
        private Integer currentTaskCount;  // 当前任务数
        private Long totalTaskCount;     // 累计任务数
        private String status;           // ONLINE, OFFLINE, BUSY
        private LocalDateTime lastUpdateTime;
    }

    /**
     * 设备得分
     */
    @lombok.Data
    @lombok.Builder
    public static class DeviceScore {
        private String deviceId;
        private Double cpuLoad;
        private Double memoryLoad;
        private Double gpuLoad;
        private Long networkLatency;
        private Integer currentTaskCount;
        private Double cpuScore;
        private Double memoryScore;
        private Double gpuScore;
        private Double networkScore;
        private Double taskScore;
        private Double totalScore;      // 总分（0-100）
    }

    /**
     * 设备利用率统计
     */
    @lombok.Data
    @lombok.Builder
    public static class DeviceUtilizationStatistics {
        private Integer totalDevices;
        private Integer onlineDevices;
        private Double avgCpuUsage;
        private Double avgMemoryUsage;
        private Double avgGpuUsage;
        private Double avgTaskCount;
    }
}
