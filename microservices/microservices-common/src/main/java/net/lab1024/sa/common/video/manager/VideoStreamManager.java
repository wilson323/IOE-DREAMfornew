package net.lab1024.sa.common.video.manager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.video.dao.VideoStreamDao;
import net.lab1024.sa.common.video.entity.VideoStreamEntity;

/**
 * 视频流管理器
 * <p>
 * 提供视频流管理相关的业务编排功能，包括流状态管理、质量控制、负载均衡等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class VideoStreamManager {

    private final VideoStreamDao videoStreamDao;

    // 流状态缓存
    private final Map<Long, VideoStreamEntity> streamCache = new ConcurrentHashMap<>();

    // 设备流计数缓存
    private final Map<Long, Integer> deviceStreamCount = new ConcurrentHashMap<>();

    /**
     * 构造函数注入依赖
     *
     * @param videoStreamDao 视频流数据访问层
     */
    public VideoStreamManager(VideoStreamDao videoStreamDao) {
        this.videoStreamDao = videoStreamDao;
    }

    /**
     * 创建视频流
     *
     * @param streamEntity 视频流实体
     * @return 创建的视频流
     */
    public VideoStreamEntity createStream(VideoStreamEntity streamEntity) {
        log.info("[视频流管理] 创建视频流，deviceId={}, streamType={}, protocol={}",
                streamEntity.getDeviceId(), streamEntity.getStreamType(), streamEntity.getProtocol());

        try {
            // 设置默认值
            setDefaultValues(streamEntity);

            // 插入数据库
            int result = videoStreamDao.insert(streamEntity);
            if (result <= 0) {
                throw new RuntimeException("创建视频流失败");
            }

            // 更新缓存
            streamCache.put(streamEntity.getStreamId(), streamEntity);

            // 更新设备流计数
            updateDeviceStreamCount(streamEntity.getDeviceId(), 1);

            log.info("[视频流管理] 视频流创建成功，streamId={}", streamEntity.getStreamId());
            return streamEntity;

        } catch (Exception e) {
            log.error("[视频流管理] 创建视频流失败，deviceId={}", streamEntity.getDeviceId(), e);
            throw new RuntimeException("创建视频流失败", e);
        }
    }

    /**
     * 启动视频流
     *
     * @param streamId 流ID
     * @return 是否成功启动
     */
    public boolean startStream(Long streamId) {
        log.info("[视频流管理] 启动视频流，streamId={}", streamId);

        try {
            VideoStreamEntity stream = getStreamFromCache(streamId);
            if (stream == null) {
                log.warn("[视频流管理] 视频流不存在，streamId={}", streamId);
                return false;
            }

            // 更新流状态
            int result = videoStreamDao.startStream(streamId, LocalDateTime.now());
            if (result <= 0) {
                log.warn("[视频流管理] 更新流状态失败，streamId={}", streamId);
                return false;
            }

            // 更新缓存
            stream.setStreamStatus(1);
            stream.setStartTime(LocalDateTime.now());
            stream.setEndTime(null);
            streamCache.put(streamId, stream);

            log.info("[视频流管理] 视频流启动成功，streamId={}", streamId);
            return true;

        } catch (Exception e) {
            log.error("[视频流管理] 启动视频流失败，streamId={}", streamId, e);
            return false;
        }
    }

    /**
     * 停止视频流
     *
     * @param streamId 流ID
     * @return 是否成功停止
     */
    public boolean stopStream(Long streamId) {
        log.info("[视频流管理] 停止视频流，streamId={}", streamId);

        try {
            VideoStreamEntity stream = getStreamFromCache(streamId);
            if (stream == null) {
                log.warn("[视频流管理] 视频流不存在，streamId={}", streamId);
                return false;
            }

            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(stream.getStartTime(), endTime).getSeconds();

            // 更新流状态
            int result = videoStreamDao.endStream(streamId, endTime);
            if (result <= 0) {
                log.warn("[视频流管理] 更新流状态失败，streamId={}", streamId);
                return false;
            }

            // 更新缓存
            stream.setStreamStatus(3);
            stream.setEndTime(endTime);
            stream.setViewerCount(0);
            streamCache.put(streamId, stream);

            log.info("[视频流管理] 视频流停止成功，streamId={}, duration={}s", streamId, duration);
            return true;

        } catch (Exception e) {
            log.error("[视频流管理] 停止视频流失败，streamId={}", streamId, e);
            return false;
        }
    }

    /**
     * 用户加入观看
     *
     * @param streamId 流ID
     * @param userId 用户ID
     * @return 是否成功加入
     */
    public boolean joinViewer(Long streamId, Long userId) {
        log.info("[视频流管理] 用户加入观看，streamId={}, userId={}", streamId, userId);

        try {
            VideoStreamEntity stream = getStreamFromCache(streamId);
            if (stream == null) {
                log.warn("[视频流管理] 视频流不存在，streamId={}", streamId);
                return false;
            }

            // 检查是否达到最大观看人数
            if (stream.getMaxViewers() != null && stream.getViewerCount() >= stream.getMaxViewers()) {
                log.warn("[视频流管理] 观看人数已达上限，streamId={}, current={}, max={}",
                        streamId, stream.getViewerCount(), stream.getMaxViewers());
                return false;
            }

            // 增加观看人数
            int result = videoStreamDao.increaseViewerCount(streamId);
            if (result <= 0) {
                log.warn("[视频流管理] 更新观看人数失败，streamId={}", streamId);
                return false;
            }

            // 更新缓存
            stream.setViewerCount(stream.getViewerCount() + 1);
            streamCache.put(streamId, stream);

            log.info("[视频流管理] 用户加入观看成功，streamId={}, userId={}, currentViewers={}",
                    streamId, userId, stream.getViewerCount());
            return true;

        } catch (Exception e) {
            log.error("[视频流管理] 用户加入观看失败，streamId={}, userId={}", streamId, userId, e);
            return false;
        }
    }

    /**
     * 用户离开观看
     *
     * @param streamId 流ID
     * @param userId 用户ID
     * @return 是否成功离开
     */
    public boolean leaveViewer(Long streamId, Long userId) {
        log.info("[视频流管理] 用户离开观看，streamId={}, userId={}", streamId, userId);

        try {
            VideoStreamEntity stream = getStreamFromCache(streamId);
            if (stream == null) {
                log.warn("[视频流管理] 视频流不存在，streamId={}", streamId);
                return false;
            }

            // 减少观看人数
            int result = videoStreamDao.decreaseViewerCount(streamId);
            if (result <= 0) {
                log.warn("[视频流管理] 更新观看人数失败，streamId={}", streamId);
                return false;
            }

            // 更新缓存
            int currentViewers = Math.max(0, stream.getViewerCount() - 1);
            stream.setViewerCount(currentViewers);
            streamCache.put(streamId, stream);

            log.info("[视频流管理] 用户离开观看成功，streamId={}, userId={}, currentViewers={}",
                    streamId, userId, currentViewers);
            return true;

        } catch (Exception e) {
            log.error("[视频流管理] 用户离开观看失败，streamId={}, userId={}", streamId, userId, e);
            return false;
        }
    }

    /**
     * 更新流质量指标
     *
     * @param streamId 流ID
     * @param latency 延迟
     * @param packetLoss 丢包率
     * @param bandwidthUsage 带宽占用
     * @return 是否成功更新
     */
    public boolean updateStreamQuality(Long streamId, Integer latency, Double packetLoss, Integer bandwidthUsage) {
        try {
            int result = videoStreamDao.updateStreamQuality(streamId, latency, packetLoss, bandwidthUsage);
            if (result > 0) {
                // 更新缓存
                VideoStreamEntity stream = getStreamFromCache(streamId);
                if (stream != null) {
                    stream.setLatency(latency);
                    stream.setPacketLoss(packetLoss);
                    stream.setBandwidthUsage(bandwidthUsage);
                    streamCache.put(streamId, stream);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("[视频流管理] 更新流质量失败，streamId={}", streamId, e);
            return false;
        }
    }

    /**
     * 处理断流
     *
     * @param streamId 流ID
     * @return 是否成功处理
     */
    public boolean handleDisconnect(Long streamId) {
        log.info("[视频流管理] 处理断流，streamId={}", streamId);

        try {
            LocalDateTime disconnectTime = LocalDateTime.now();

            // 记录断流
            int result = videoStreamDao.recordDisconnect(streamId, disconnectTime);
            if (result <= 0) {
                log.warn("[视频流管理] 记录断流失败，streamId={}", streamId);
                return false;
            }

            // 更新缓存
            VideoStreamEntity stream = getStreamFromCache(streamId);
            if (stream != null) {
                stream.setStreamStatus(2);
                stream.setLastDisconnectTime(disconnectTime);
                stream.setDisconnectCount(stream.getDisconnectCount() + 1);
                streamCache.put(streamId, stream);
            }

            log.info("[视频流管理] 断流处理成功，streamId={}", streamId);
            return true;

        } catch (Exception e) {
            log.error("[视频流管理] 处理断流失败，streamId={}", streamId, e);
            return false;
        }
    }

    /**
     * 处理重连
     *
     * @param streamId 流ID
     * @return 是否成功处理
     */
    public boolean handleReconnect(Long streamId) {
        log.info("[视频流管理] 处理重连，streamId={}", streamId);

        try {
            LocalDateTime reconnectTime = LocalDateTime.now();

            // 记录重连
            int result = videoStreamDao.recordReconnect(streamId, reconnectTime);
            if (result <= 0) {
                log.warn("[视频流管理] 记录重连失败，streamId={}", streamId);
                return false;
            }

            // 更新缓存
            VideoStreamEntity stream = getStreamFromCache(streamId);
            if (stream != null) {
                stream.setStreamStatus(1);
                stream.setReconnectTime(reconnectTime);
                streamCache.put(streamId, stream);
            }

            log.info("[视频流管理] 重连处理成功，streamId={}", streamId);
            return true;

        } catch (Exception e) {
            log.error("[视频流管理] 处理重连失败，streamId={}", streamId, e);
            return false;
        }
    }

    /**
     * 获取设备可用流
     *
     * @param deviceId 设备ID
     * @return 可用流列表
     */
    public List<VideoStreamEntity> getAvailableStreamsByDevice(Long deviceId) {
        log.debug("[视频流管理] 获取设备可用流，deviceId={}", deviceId);

        try {
            return videoStreamDao.selectAvailableStreams().stream()
                    .filter(stream -> deviceId.equals(stream.getDeviceId()))
                    .toList();
        } catch (Exception e) {
            log.error("[视频流管理] 获取设备可用流失败，deviceId={}", deviceId, e);
            return List.of();
        }
    }

    /**
     * 获取用户可访问的流
     *
     * @param userId 用户ID
     * @param permissionLevel 权限级别
     * @return 可访问流列表
     */
    public List<VideoStreamEntity> getAccessibleStreamsByUser(Long userId, Integer permissionLevel) {
        log.debug("[视频流管理] 获取用户可访问流，userId={}, permissionLevel={}", userId, permissionLevel);

        try {
            return videoStreamDao.selectByUserPermission(userId, permissionLevel);
        } catch (Exception e) {
            log.error("[视频流管理] 获取用户可访问流失败，userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 获取流统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getStreamStatistics() {
        log.debug("[视频流管理] 获取流统计信息");

        try {
            Map<String, Object> statistics = new java.util.HashMap<>();

            // 总数统计
            statistics.put("totalStreams", videoStreamDao.countStreams(null, null, null));

            // 按状态统计
            Map<String, Object> statusStats = new java.util.HashMap<>();
            videoStreamDao.countStreamsByStatus().forEach(row -> {
                statusStats.put("status_" + row.get("stream_status"), row.get("count"));
            });
            statistics.put("statusStatistics", statusStats);

            // 按协议统计
            Map<String, Object> protocolStats = new java.util.HashMap<>();
            videoStreamDao.countStreamsByProtocol().forEach(row -> {
                protocolStats.put("protocol_" + row.get("protocol"), row.get("count"));
            });
            statistics.put("protocolStatistics", protocolStats);

            return statistics;

        } catch (Exception e) {
            log.error("[视频流管理] 获取流统计信息失败", e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 从缓存获取流信息
     *
     * @param streamId 流ID
     * @return 流信息
     */
    private VideoStreamEntity getStreamFromCache(Long streamId) {
        VideoStreamEntity stream = streamCache.get(streamId);
        if (stream == null) {
            stream = videoStreamDao.selectById(streamId);
            if (stream != null) {
                streamCache.put(streamId, stream);
            }
        }
        return stream;
    }

    /**
     * 设置默认值
     *
     * @param streamEntity 视频流实体
     */
    private void setDefaultValues(VideoStreamEntity streamEntity) {
        if (streamEntity.getStreamStatus() == null) {
            streamEntity.setStreamStatus(2); // 默认为断开状态
        }
        if (streamEntity.getViewerCount() == null) {
            streamEntity.setViewerCount(0);
        }
        if (streamEntity.getMaxViewers() == null) {
            streamEntity.setMaxViewers(10); // 默认最大10个观看者
        }
        if (streamEntity.getStreamQuality() == null) {
            streamEntity.setStreamQuality(2); // 默认标准质量
        }
    }

    /**
     * 更新设备流计数
     *
     * @param deviceId 设备ID
     * @param delta 增量
     */
    private void updateDeviceStreamCount(Long deviceId, int delta) {
        deviceStreamCount.merge(deviceId, delta, Integer::sum);
    }

    /**
     * 获取设备流计数
     *
     * @param deviceId 设备ID
     * @return 流数量
     */
    public Integer getDeviceStreamCount(Long deviceId) {
        return deviceStreamCount.getOrDefault(deviceId, 0);
    }
}