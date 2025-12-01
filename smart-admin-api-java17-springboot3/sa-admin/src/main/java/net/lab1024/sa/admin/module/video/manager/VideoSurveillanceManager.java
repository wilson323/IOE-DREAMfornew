package net.lab1024.sa.admin.module.video.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.video.dao.VideoDeviceDao;
import net.lab1024.sa.admin.module.video.dao.VideoRecordingDao;
import net.lab1024.sa.admin.module.video.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.admin.module.video.domain.entity.VideoRecordingEntity;
import net.lab1024.sa.admin.module.video.service.VideoSurveillanceService;

/**
 * 视频监控管理器
 *
 * <p>
 * 视频模块的监控业务逻辑封装
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 实现四层架构中的Manager层，封装复杂的监控管理业务逻辑
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
@Component
public class VideoSurveillanceManager {
    private static final Logger log = LoggerFactory.getLogger(VideoSurveillanceManager.class);

    @Resource
    private VideoDeviceDao videoDeviceDao;

    @Resource
    private VideoRecordingDao videoRecordingDao;

    @Resource
    private VideoSurveillanceService videoSurveillanceService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 启动设备监控
     *
     * @param deviceId 设备ID
     * @return 启动结果
     */
    public boolean startDeviceMonitoring(Long deviceId) {
        log.info("启动视频设备监控: 设备ID={}", deviceId);

        try {
            VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("设备不存在: 设备ID={}", deviceId);
                return false;
            }

            // 检查设备状态
            if (!"ONLINE".equals(device.getDeviceStatus())) {
                log.warn("设备不在线，无法启动监控: 设备ID={}, 状态={}", deviceId, device.getDeviceStatus());
                return false;
            }

            // 调用Service层启动监控
            boolean success = videoSurveillanceService.startLiveMonitor(deviceId);

            if (success) {
                // 启动后台监控任务
                startBackgroundMonitoring(deviceId);
                log.info("设备监控启动成功: 设备ID={}, 设备名称={}", deviceId, device.getDeviceName());
            } else {
                log.warn("设备监控启动失败: 设备ID={}", deviceId);
            }

            return success;

        } catch (Exception e) {
            log.error("启动设备监控时发生异常: 设备ID={}", deviceId, e);
            return false;
        }
    }

    /**
     * 停止设备监控
     *
     * @param deviceId 设备ID
     * @return 停止结果
     */
    public boolean stopDeviceMonitoring(Long deviceId) {
        log.info("停止视频设备监控: 设备ID={}", deviceId);

        try {
            // 调用Service层停止监控
            boolean success = videoSurveillanceService.stopLiveMonitor(deviceId);

            // 停止后台监控任务
            stopBackgroundMonitoring(deviceId);

            log.info("设备监控停止结果: 设备ID={}, 成功={}", deviceId, success);
            return success;

        } catch (Exception e) {
            log.error("停止设备监控时发生异常: 设备ID={}", deviceId, e);
            return false;
        }
    }

    /**
     * 批量启动监控
     *
     * @param deviceIds 设备ID列表
     * @return 批量启动结果
     */
    public Map<Long, Boolean> batchStartMonitoring(List<Long> deviceIds) {
        log.info("批量启动视频监控: 设备数量={}", deviceIds != null ? deviceIds.size() : 0);

        Map<Long, Boolean> results = new HashMap<>();

        if (CollectionUtils.isEmpty(deviceIds)) {
            return results;
        }

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Long deviceId : deviceIds) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    boolean success = startDeviceMonitoring(deviceId);
                    results.put(deviceId, success);
                }, executorService);

                futures.add(future);
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 统计结果
            long successCount = results.values().stream().mapToInt(success -> success ? 1 : 0).sum();
            log.info("批量启动监控完成: 总数={}, 成功={}, 失败={}",
                     deviceIds.size(), successCount, deviceIds.size() - successCount);

        } catch (Exception e) {
            log.error("批量启动监控时发生异常", e);
        }

        return results;
    }

    /**
     * 获取监控状态
     *
     * @param deviceId 设备ID
     * @return 监控状态信息
     */
    public Map<String, Object> getMonitoringStatus(Long deviceId) {
        log.info("获取视频监控状态: 设备ID={}", deviceId);

        Map<String, Object> status = new HashMap<>();

        try {
            VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
            if (device == null) {
                status.put("monitoring", false);
                status.put("message", "设备不存在");
                return status;
            }

            status.put("deviceId", deviceId);
            status.put("deviceName", device.getDeviceName());
            status.put("deviceStatus", device.getDeviceStatus());
            status.put("monitoringTime", LocalDateTime.now());

            // 检查是否正在录制
            boolean isRecording = checkIfRecording(deviceId);
            status.put("isRecording", isRecording);

            // 获取最近录制记录
            VideoRecordingEntity latestRecording = getLatestRecording(deviceId);
            if (latestRecording != null) {
                status.put("latestRecordingTime", latestRecording.getStartTime());
                status.put("latestRecordingDuration", latestRecording.getDuration());
            }

            log.info("监控状态获取成功: 设备ID={}, 监控状态={}", deviceId, status.get("isRecording"));

        } catch (Exception e) {
            log.error("获取监控状态时发生异常: 设备ID={}", deviceId, e);
            status.put("monitoring", false);
            status.put("message", "获取状态异常: " + e.getMessage());
        }

        return status;
    }

    /**
     * 获取设备监控统计
     *
     * @return 监控统计信息
     */
    public Map<String, Object> getMonitoringStatistics() {
        log.info("获取视频监控统计信息");

        Map<String, Object> statistics = new HashMap<>();

        try {
            // 总设备数
            int totalDevices = videoDeviceDao.selectCount(null);
            statistics.put("totalDevices", totalDevices);

            // 在线设备数
            int onlineDevices = videoDeviceDao.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VideoDeviceEntity>()
                    .eq("device_status", "ONLINE")
            );
            statistics.put("onlineDevices", onlineDevices);

            // 启用录制的设备数
            int recordingEnabledDevices = videoDeviceDao.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VideoDeviceEntity>()
                    .eq("device_status", "ONLINE")
                    .eq("recording_enabled", 1)
            );
            statistics.put("recordingEnabledDevices", recordingEnabledDevices);

            // 今日录制记录数
            String today = LocalDateTime.now().toLocalDate().toString();
            int todayRecordings = videoRecordingDao.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VideoRecordingEntity>()
                    .ge("start_time", today + " 00:00:00")
                    .le("start_time", today + " 23:59:59")
            );
            statistics.put("todayRecordings", todayRecordings);

            // 存储空间使用情况
            long totalStorageSize = calculateStorageUsage();
            statistics.put("totalStorageSize", totalStorageSize);

            // 按设备类型统计
            Map<String, Integer> typeStatistics = new HashMap<>();
            List<VideoDeviceEntity> devices = videoDeviceDao.selectList(null);
            for (VideoDeviceEntity device : devices) {
                String cameraType = device.getCameraType();
                typeStatistics.put(cameraType, typeStatistics.getOrDefault(cameraType, 0) + 1);
            }
            statistics.put("cameraTypeStatistics", typeStatistics);

            log.info("监控统计信息获取成功: 设备数={}, 在线={}, 录制={}",
                     totalDevices, onlineDevices, recordingEnabledDevices);

        } catch (Exception e) {
            log.error("获取监控统计信息时发生异常", e);
        }

        return statistics;
    }

    /**
     * 启动后台监控任务
     */
    private void startBackgroundMonitoring(Long deviceId) {
        log.debug("启动后台监控任务: 设备ID={}", deviceId);

        // 这里可以实现后台监控逻辑
        // 例如：定期检查设备状态、自动录制、异常报警等
    }

    /**
     * 停止后台监控任务
     */
    private void stopBackgroundMonitoring(Long deviceId) {
        log.debug("停止后台监控任务: 设备ID={}", deviceId);

        // 这里可以实现停止后台监控的逻辑
    }

    /**
     * 检查设备是否正在录制
     */
    private boolean checkIfRecording(Long deviceId) {
        try {
            VideoRecordingEntity recording = videoRecordingDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VideoRecordingEntity>()
                    .eq("device_id", deviceId)
                    .eq("status", "RECORDING")
                    .orderBy("start_time", false)
                    .last("limit", 1)
            );
            return recording != null;

        } catch (Exception e) {
            log.error("检查录制状态时发生异常: 设备ID={}", deviceId, e);
            return false;
        }
    }

    /**
     * 获取最近的录制记录
     */
    private VideoRecordingEntity getLatestRecording(Long deviceId) {
        try {
            return videoRecordingDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VideoRecordingEntity>()
                    .eq("device_id", deviceId)
                    .orderBy("start_time", false)
                    .last("limit", 1)
            );

        } catch (Exception e) {
            log.error("获取最近录制记录时发生异常: 设备ID={}", deviceId, e);
            return null;
        }
    }

    /**
     * 计算存储使用量
     */
    private long calculateStorageUsage() {
        try {
            List<VideoRecordingEntity> recordings = videoRecordingDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VideoRecordingEntity>()
                    .select("file_size")
                    .isNotNull("file_size")
            );

            return recordings.stream()
                    .mapToLong(VideoRecordingEntity::getFileSize)
                    .sum();

        } catch (Exception e) {
            log.error("计算存储使用量时发生异常", e);
            return 0L;
        }
    }

    /**
     * 监控设备异常
     *
     * @param deviceId 设备ID
     * @param monitorTime 监控间隔（秒）
     */
    public void startDeviceAnomalyMonitoring(Long deviceId, int monitorTime) {
        log.info("启动设备异常监控: 设备ID={}, 监控间隔={}秒", deviceId, monitorTime);

        CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    try {
                        Thread.sleep(monitorTime * 1000L);

                        // 检查设备状态
                        Map<String, Object> status = getMonitoringStatus(deviceId);

                        // 检查是否异常
                        if (!Boolean.TRUE.equals(status.get("monitoring"))) {
                            log.warn("设备监控异常: 设备ID={}, 状态={}", deviceId, status.get("message"));
                            // 可以在这里实现报警逻辑
                        }

                    } catch (InterruptedException e) {
                        log.info("设备异常监控被中断: 设备ID={}", deviceId);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("设备异常监控发生异常: 设备ID={}", deviceId, e);
            }
        }, executorService);
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}