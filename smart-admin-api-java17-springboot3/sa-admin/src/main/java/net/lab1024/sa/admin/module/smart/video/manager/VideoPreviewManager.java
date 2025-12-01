package net.lab1024.sa.admin.module.smart.video.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.smart.video.dao.VideoDeviceDao;
import net.lab1024.sa.base.common.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.base.common.manager.BaseCacheManager;

/**
 * 视频预览管理器 基于 BaseCacheManager 实现的多级缓存： - L1: Caffeine 本地缓存（5 分钟过期） - L2: Redis
 * 分布式缓存（30 分钟过期）
 *
 * 职责（符合 repowiki Manager 层规范）： - 视频流地址管理 - 设备连接状态管理 - PTZ 控制指令处理 - 实时预览会话管理 -
 * 第三方视频服务聚合
 *
 * 缓存 Key 约定： - video:stream:{deviceId} - video:snapshot:{deviceId} -
 * video:session:{sessionId} -
 * video:status:{deviceId} - video:capability:{deviceId}
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Component
public class VideoPreviewManager extends BaseCacheManager {

    private static final Logger log = LoggerFactory.getLogger(VideoPreviewManager.class);

    @Resource
    private VideoDeviceDao videoDeviceDao;

    // 活跃中的预览会话管理
    private final Map<String, PreviewSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    protected String getCachePrefix() {
        return "video:";
    }

    /**
     * 获取实时视频流地址（可缓存）
     *
     * @param deviceId 设备ID
     * @param quality  画质（HD/SD/AUTO）
     * @return 视频流地址
     */
    public String getLiveStreamUrl(Long deviceId, String quality) {
        String cacheKey = buildCacheKey(deviceId, ":stream:" + quality);

        return this.getCache(cacheKey, () -> {
            // 获取设备信息
            VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
            if (device == null || !"ONLINE".equals(device.getDeviceStatus())) {
                return null;
            }

            // 构建视频流地址
            String protocol = "rtsp";
            String host = device.getDeviceIp();
            Integer port = device.getStreamPort() != null ? device.getStreamPort() : 554;
            String path = String.format("/stream/%d/%s", deviceId, quality.toLowerCase());

            return String.format("%s://%s:%d%s", protocol, host, port, path);
        });
    }

    /**
     * 获取设备实时截图（可缓存，短过期）
     *
     * @param deviceId 设备ID
     * @return 截图路径
     */
    public String getRealtimeSnapshot(Long deviceId) {
        String cacheKey = buildCacheKey(deviceId, ":snapshot");

        return this.getCache(cacheKey, () -> {
            // 这里应调用设备 API 获取实时截图
            // 为示例返回默认截图路径
            return "/snapshots/video_" + deviceId + "_" + System.currentTimeMillis() + ".jpg";
        });
    }

    /**
     * 云台（PTZ）控制
     *
     * @param deviceId 设备ID
     * @param command  控制指令
     * @param speed    控制速度
     * @param preset   预置位
     * @return 控制结果
     */
    public boolean ptzControl(Long deviceId, String command, Integer speed, Integer preset) {
        try {
            // 这里应调用设备 PTZ 控制 API
            log.info("PTZ控制: deviceId={}, command={}, speed={}, preset={}", deviceId, command,
                    speed, preset);

            // 清除设备状态缓存
            this.removeCache(buildCacheKey(deviceId, ":status"));

            // 模拟控制结果
            return true;
        } catch (Exception e) {
            log.error("PTZ控制失败: deviceId={}, command={}", deviceId, command, e);
            return false;
        }
    }

    /**
     * 获取设备预览状态（可缓存）
     *
     * @param deviceId 设备ID
     * @return 设备预览状态
     */
    public Map<String, Object> getPreviewStatus(Long deviceId) {
        String cacheKey = buildCacheKey(deviceId, ":status");

        return this.getCache(cacheKey, () -> {
            Map<String, Object> status = new HashMap<>();

            VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
            if (device != null) {
                status.put("deviceId", deviceId);
                status.put("deviceName", device.getDeviceName());
                status.put("deviceStatus", device.getDeviceStatus());
                status.put("deviceType", device.getDeviceType());
                status.put("lastUpdate", System.currentTimeMillis());
            }

            return status;
        });
    }

    /**
     * 启动视频预览会话
     *
     * @param deviceId 设备ID
     * @param userId   用户ID
     * @return 会话ID
     */
    public String startPreviewSession(Long deviceId, Long userId) {
        String sessionId = UUID.randomUUID().toString();

        PreviewSession session = new PreviewSession();
        session.sessionId = sessionId;
        session.deviceId = deviceId;
        session.userId = userId;
        session.startTime = System.currentTimeMillis();
        session.status = "ACTIVE";

        activeSessions.put(sessionId, session);

        log.info("启动预览会话: sessionId={}, deviceId={}, userId={}", sessionId, deviceId, userId);

        return sessionId;
    }

    /**
     * 停止视频预览会话
     *
     * @param sessionId 会话ID
     * @return 停止结果
     */
    public boolean stopPreviewSession(String sessionId) {
        PreviewSession session = activeSessions.remove(sessionId);
        if (session != null) {
            session.endTime = System.currentTimeMillis();
            session.status = "STOPPED";

            log.info("停止预览会话: sessionId={}, deviceId={}, duration={}ms", sessionId,
                    session.deviceId, session.endTime - session.startTime);
            return true;
        }
        return false;
    }

    /**
     * 获取活跃的预览会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    public List<PreviewSession> getActivePreviewSessions(Long userId) {
        List<PreviewSession> sessions = new ArrayList<>();

        for (PreviewSession session : activeSessions.values()) {
            if (Objects.equals(session.userId, userId) && "ACTIVE".equals(session.status)) {
                sessions.add(session);
            }
        }

        return sessions;
    }

    /**
     * 获取设备能力信息（可缓存）
     *
     * @param deviceId 设备ID
     * @return 设备能力信息
     */
    public Map<String, Object> getDeviceCapabilities(Long deviceId) {
        String cacheKey = buildCacheKey(deviceId, ":capability");

        return this.getCache(cacheKey, () -> {
            Map<String, Object> capabilities = new HashMap<>();

            VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
            if (device != null) {
                // 根据设备类型设置能力信息
                capabilities.put("deviceId", deviceId);
                capabilities.put("deviceType", device.getDeviceType());
                capabilities.put("ptzSupport", true);
                capabilities.put("audioSupport", true);
                capabilities.put("recordingSupport", true);
                capabilities.put("snapshotSupport", true);
                capabilities.put("streamQualities", Arrays.asList("HD", "SD", "AUTO"));
                capabilities.put("maxPresets", 16);
            }

            return capabilities;
        });
    }

    /**
     * 清除设备相关缓存
     *
     * @param deviceId 设备ID
     */
    public void removeDeviceCache(Long deviceId) {
        this.removeCache(buildCacheKey(deviceId, ":stream:HD"));
        this.removeCache(buildCacheKey(deviceId, ":stream:SD"));
        this.removeCache(buildCacheKey(deviceId, ":stream:AUTO"));
        this.removeCache(buildCacheKey(deviceId, ":snapshot"));
        this.removeCache(buildCacheKey(deviceId, ":status"));
        this.removeCache(buildCacheKey(deviceId, ":capability"));
    }

    /**
     * 清理过期的预览会话
     */
    public void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        long expireTime = 30 * 60 * 1000; // 30 分钟

        activeSessions.entrySet().removeIf(entry -> {
            PreviewSession session = entry.getValue();
            boolean expired = (currentTime - session.startTime) > expireTime;

            if (expired) {
                log.info("清理过期预览会话: sessionId={}, deviceId={}", entry.getKey(), session.deviceId);
            }

            return expired;
        });
    }

    /**
     * 获取预览会话统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long currentTime = System.currentTimeMillis();
        long activeCount = activeSessions.values().stream()
                .filter(session -> "ACTIVE".equals(session.status)).count();

        stats.put("totalSessions", activeSessions.size());
        stats.put("activeSessions", activeCount);
        stats.put("timestamp", currentTime);

        return stats;
    }

    /**
     * 预览会话内部类
     */
    public static class PreviewSession {
        public String sessionId;
        public Long deviceId;
        public Long userId;
        public long startTime;
        public long endTime;
        public String status;
    }
}
