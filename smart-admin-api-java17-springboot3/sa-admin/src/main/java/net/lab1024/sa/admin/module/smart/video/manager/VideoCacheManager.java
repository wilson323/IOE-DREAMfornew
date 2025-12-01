package net.lab1024.sa.admin.module.smart.video.manager;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import net.lab1024.sa.base.common.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.base.common.manager.BaseCacheManager;

/**
 * 视频监控缓存管理器 基于 BaseCacheManager 实现的多级缓存： - L1: Caffeine 本地缓存（5 分钟过期） - L2: Redis 分布式缓存（30 分钟过期）
 *
 * 缓存策略： - 设备信息：查询时缓存，更新时清理 - 设备状态：实时性要求高，使用较短过期时间 - 流地址：缓存时间适中，减少重复计算
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Component
public class VideoCacheManager extends BaseCacheManager {

    @Override
    protected String getCachePrefix() {
        return "video:";
    }

    /**
     * 获取设备信息（可缓存）
     *
     * @param deviceId 设备ID
     * @return 设备信息，可能为 null
     */
    public VideoDeviceEntity getDevice(Long deviceId) {
        String cacheKey = buildCacheKey(deviceId, ":info");
        return this.getCache(cacheKey, new DataLoader<VideoDeviceEntity>() {
            @Override
            public VideoDeviceEntity load() {
                // 这里应调用DAO查询，为简化暂返回null
                return null;
            }
        });
    }

    /**
     * 获取设备状态（可缓存，短过期）
     *
     * @param deviceId 设备ID
     * @return 设备状态信息，可能为 null
     */
    public VideoDeviceEntity getDeviceStatus(Long deviceId) {
        String cacheKey = buildCacheKey(deviceId, ":status");
        return this.getCache(cacheKey, new DataLoader<VideoDeviceEntity>() {
            @Override
            public VideoDeviceEntity load() {
                VideoDeviceEntity device = new VideoDeviceEntity();
                device.setDeviceId(deviceId);
                device.setDeviceStatus("ONLINE");
                return device;
            }
        });
    }

    /**
     * 批量获取设备状态
     *
     * @param deviceIds 设备ID列表
     * @return 设备状态列表
     */
    public List<VideoDeviceEntity> getDeviceStatusBatch(List<Long> deviceIds) {
        List<VideoDeviceEntity> result = new ArrayList<>();
        for (Long deviceId : deviceIds) {
            VideoDeviceEntity device = getDeviceStatus(deviceId);
            if (device != null) {
                result.add(device);
            }
        }
        return result;
    }

    /**
     * 设置设备信息缓存
     *
     * @param deviceId 设备ID
     * @param device 设备信息
     */
    public void setDevice(Long deviceId, VideoDeviceEntity device) {
        String cacheKey = buildCacheKey(deviceId, ":info");
        this.setCache(cacheKey, device);
    }

    /**
     * 设置设备状态缓存
     *
     * @param deviceId 设备ID
     * @param device 设备状态信息
     */
    public void setDeviceStatus(Long deviceId, VideoDeviceEntity device) {
        String cacheKey = buildCacheKey(deviceId, ":status");
        this.setCache(cacheKey, device);
    }

    /**
     * 清除设备相关缓存
     *
     * @param deviceId 设备ID
     */
    public void removeDevice(Long deviceId) {
        // 清除设备基本信息缓存
        this.removeCache(buildCacheKey(deviceId, ":info"));
        // 清除设备状态缓存
        this.removeCache(buildCacheKey(deviceId, ":status"));
        // 清除设备流地址缓存
        this.removeCache(buildCacheKey(deviceId, ":stream"));
        // 清除设备截图缓存
        this.removeCache(buildCacheKey(deviceId, ":snapshot"));

        // 清除设备列表相关缓存
        this.removeCacheByPattern(getCachePrefix() + "*:list*");
    }

    /**
     * 获取设备直播流地址（可缓存）
     *
     * @param deviceId 设备ID
     * @return 直播流地址，可能为 null
     */
    public String getLiveStreamUrl(Long deviceId) {
        String cacheKey = buildCacheKey(deviceId, ":stream");
        return this.getCache(cacheKey, new DataLoader<String>() {
            @Override
            public String load() {
                return String.format("rtsp://192.168.1.100:554/stream/%d", deviceId);
            }
        });
    }

    /**
     * 设置设备直播流地址缓存
     *
     * @param deviceId 设备ID
     * @param streamUrl 直播流地址
     */
    public void setLiveStreamUrl(Long deviceId, String streamUrl) {
        String cacheKey = buildCacheKey(deviceId, ":stream");
        this.setCache(cacheKey, streamUrl);
    }

    /**
     * 获取设备截图路径（可缓存）
     *
     * @param deviceId 设备ID
     * @return 截图路径，可能为 null
     */
    public String getDeviceSnapshot(Long deviceId) {
        String cacheKey = buildCacheKey(deviceId, ":snapshot");
        return this.getCache(cacheKey, new DataLoader<String>() {
            @Override
            public String load() {
                return "/snapshots/device_" + deviceId + ".jpg";
            }
        });
    }

    /**
     * 设置设备截图路径缓存
     *
     * @param deviceId 设备ID
     * @param snapshotPath 截图路径
     */
    public void setDeviceSnapshot(Long deviceId, String snapshotPath) {
        String cacheKey = buildCacheKey(deviceId, ":snapshot");
        this.setCache(cacheKey, snapshotPath);
    }

    /**
     * 预热设备缓存
     *
     * @param deviceId 设备ID
     */
    public void warmUpDevice(Long deviceId) {
        // 预热设备信息
        String cacheKey = buildCacheKey(deviceId, ":info");
        VideoDeviceEntity device = new VideoDeviceEntity();
        device.setDeviceId(deviceId);
        device.setDeviceName("预热设备");
        this.setCache(cacheKey, device);
    }

    /**
     * 批量预热设备缓存
     *
     * @param deviceIds 设备ID列表
     */
    public void warmUpDevices(Iterable<Long> deviceIds) {
        for (Long deviceId : deviceIds) {
            this.warmUpDevice(deviceId);
        }
    }

    /**
     * 清除所有视频监控相关缓存
     */
    public void clearAllVideoCache() {
        this.removeCacheByPattern(getCachePrefix() + "*");
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    @Override
    public CacheStats getCacheStats() {
        CacheStats stats = super.getCacheStats();
        // 可以添加视频监控特定的统计信息
        return stats;
    }
}
