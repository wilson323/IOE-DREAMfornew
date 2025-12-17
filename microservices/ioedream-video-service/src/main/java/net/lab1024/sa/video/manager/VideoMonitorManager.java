package net.lab1024.sa.video.manager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.video.dao.VideoMonitorDao;
import net.lab1024.sa.video.entity.VideoMonitorEntity;

/**
 * 视频监控管理器
 * <p>
 * 提供监控会话管理相关的业务编排功能，包括会话管理、状态监控、资源优化等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class VideoMonitorManager {

    private final VideoMonitorDao videoMonitorDao;

    // 监控会话缓存
    private final Map<Long, VideoMonitorEntity> sessionCache = new ConcurrentHashMap<>();

    // 用户会话映射
    private final Map<Long, Long> userSessionMap = new ConcurrentHashMap<>();

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * 构造函数注入依赖
     *
     * @param videoMonitorDao 视频监控数据访问层
     */
    public VideoMonitorManager(VideoMonitorDao videoMonitorDao) {
        this.videoMonitorDao = videoMonitorDao;
        initScheduledTasks();
    }

    /**
     * 创建监控会话
     *
     * @param monitorEntity 监控会话实体
     * @return 创建的监控会话
     */
    public VideoMonitorEntity createSession(VideoMonitorEntity monitorEntity) {
        log.info("[监控管理] 创建监控会话，userId={}, clientIp={}, screenLayout={}",
                monitorEntity.getUserId(), monitorEntity.getClientIp(), monitorEntity.getScreenLayout());

        try {
            // 设置默认值
            setDefaultValues(monitorEntity);

            // 插入数据库
            int result = videoMonitorDao.insert(monitorEntity);
            if (result <= 0) {
                throw new RuntimeException("创建监控会话失败");
            }

            // 更新缓存
            sessionCache.put(monitorEntity.getMonitorId(), monitorEntity);
            userSessionMap.put(monitorEntity.getUserId(), monitorEntity.getMonitorId());

            log.info("[监控管理] 监控会话创建成功，monitorId={}", monitorEntity.getMonitorId());
            return monitorEntity;

        } catch (Exception e) {
            log.error("[监控管理] 创建监控会话失败，userId={}", monitorEntity.getUserId(), e);
            throw new RuntimeException("创建监控会话失败", e);
        }
    }

    /**
     * 启动监控会话
     *
     * @param monitorId 监控会话ID
     * @return 是否成功启动
     */
    public boolean startSession(Long monitorId) {
        log.info("[监控管理] 启动监控会话，monitorId={}", monitorId);

        try {
            VideoMonitorEntity session = getSessionFromCache(monitorId);
            if (session == null) {
                log.warn("[监控管理] 监控会话不存在，monitorId={}", monitorId);
                return false;
            }

            // 更新会话状态
            int result = videoMonitorDao.updateSessionStatus(monitorId, 1);
            if (result <= 0) {
                log.warn("[监控管理] 更新会话状态失败，monitorId={}", monitorId);
                return false;
            }

            // 更新缓存
            session.setSessionStatus(1);
            session.setLastActiveTime(LocalDateTime.now());
            sessionCache.put(monitorId, session);

            log.info("[监控管理] 监控会话启动成功，monitorId={}", monitorId);
            return true;

        } catch (Exception e) {
            log.error("[监控管理] 启动监控会话失败，monitorId={}", monitorId, e);
            return false;
        }
    }

    /**
     * 暂停监控会话
     *
     * @param monitorId 监控会话ID
     * @return 是否成功暂停
     */
    public boolean pauseSession(Long monitorId) {
        log.info("[监控管理] 暂停监控会话，monitorId={}", monitorId);

        try {
            VideoMonitorEntity session = getSessionFromCache(monitorId);
            if (session == null) {
                log.warn("[监控管理] 监控会话不存在，monitorId={}", monitorId);
                return false;
            }

            // 更新会话状态
            int result = videoMonitorDao.updateSessionStatus(monitorId, 2);
            if (result <= 0) {
                log.warn("[监控管理] 更新会话状态失败，monitorId={}", monitorId);
                return false;
            }

            // 更新缓存
            session.setSessionStatus(2);
            sessionCache.put(monitorId, session);

            log.info("[监控管理] 监控会话暂停成功，monitorId={}", monitorId);
            return true;

        } catch (Exception e) {
            log.error("[监控管理] 暂停监控会话失败，monitorId={}", monitorId, e);
            return false;
        }
    }

    /**
     * 终止监控会话
     *
     * @param monitorId 监控会话ID
     * @return 是否成功终止
     */
    public boolean terminateSession(Long monitorId) {
        log.info("[监控管理] 终止监控会话，monitorId={}", monitorId);

        try {
            VideoMonitorEntity session = getSessionFromCache(monitorId);
            if (session == null) {
                log.warn("[监控管理] 监控会话不存在，monitorId={}", monitorId);
                return false;
            }

            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(session.getStartTime(), endTime).getSeconds();

            // 终止会话
            int result = videoMonitorDao.terminateSession(monitorId, endTime, duration);
            if (result <= 0) {
                log.warn("[监控管理] 终止会话失败，monitorId={}", monitorId);
                return false;
            }

            // 更新缓存
            session.setSessionStatus(3);
            session.setEndTime(endTime);
            session.setDuration(duration);
            sessionCache.put(monitorId, session);

            // 清理用户会话映射
            userSessionMap.remove(session.getUserId());

            log.info("[监控管理] 监控会话终止成功，monitorId={}, duration={}s", monitorId, duration);
            return true;

        } catch (Exception e) {
            log.error("[监控管理] 终止监控会话失败，monitorId={}", monitorId, e);
            return false;
        }
    }

    /**
     * 更新会话活跃时间
     *
     * @param monitorId 监控会话ID
     * @return 是否成功更新
     */
    public boolean updateLastActiveTime(Long monitorId) {
        try {
            LocalDateTime now = LocalDateTime.now();

            int result = videoMonitorDao.updateLastActiveTime(monitorId, now);
            if (result > 0) {
                // 更新缓存
                VideoMonitorEntity session = getSessionFromCache(monitorId);
                if (session != null) {
                    session.setLastActiveTime(now);
                    sessionCache.put(monitorId, session);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("[监控管理] 更新活跃时间失败，monitorId={}", monitorId, e);
            return false;
        }
    }

    /**
     * 更新网络状态
     *
     * @param monitorId 监控会话ID
     * @param networkStatus 网络状态
     * @param networkLatency 网络延迟
     * @param packetLossRate 丢包率
     * @param bandwidthUsage 带宽占用
     * @return 是否成功更新
     */
    public boolean updateNetworkStatus(Long monitorId, Integer networkStatus,
                                       Integer networkLatency, Double packetLossRate, Integer bandwidthUsage) {
        try {
            int result = videoMonitorDao.updateNetworkStatus(monitorId, networkStatus,
                    networkLatency, packetLossRate, bandwidthUsage);
            if (result > 0) {
                // 更新缓存
                VideoMonitorEntity session = getSessionFromCache(monitorId);
                if (session != null) {
                    session.setNetworkStatus(networkStatus);
                    session.setNetworkLatency(networkLatency);
                    session.setPacketLossRate(packetLossRate);
                    session.setBandwidthUsage(bandwidthUsage);
                    sessionCache.put(monitorId, session);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("[监控管理] 更新网络状态失败，monitorId={}", monitorId, e);
            return false;
        }
    }

    /**
     * 更新资源使用情况
     *
     * @param monitorId 监控会话ID
     * @param cpuUsage CPU使用率
     * @param memoryUsage 内存使用率
     * @return 是否成功更新
     */
    public boolean updateResourceUsage(Long monitorId, Double cpuUsage, Double memoryUsage) {
        try {
            int result = videoMonitorDao.updateResourceUsage(monitorId, cpuUsage, memoryUsage);
            if (result > 0) {
                // 更新缓存
                VideoMonitorEntity session = getSessionFromCache(monitorId);
                if (session != null) {
                    session.setCpuUsage(cpuUsage);
                    session.setMemoryUsage(memoryUsage);
                    sessionCache.put(monitorId, session);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("[监控管理] 更新资源使用情况失败，monitorId={}", monitorId, e);
            return false;
        }
    }

    /**
     * 记录异常信息
     *
     * @param monitorId 监控会话ID
     * @param exceptionMessage 异常信息
     * @return 是否成功记录
     */
    public boolean recordException(Long monitorId, String exceptionMessage) {
        try {
            LocalDateTime exceptionTime = LocalDateTime.now();

            int result = videoMonitorDao.recordException(monitorId, exceptionTime, exceptionMessage);
            if (result > 0) {
                // 更新缓存
                VideoMonitorEntity session = getSessionFromCache(monitorId);
                if (session != null) {
                    session.setExceptionCount(session.getExceptionCount() + 1);
                    session.setLastExceptionTime(exceptionTime);
                    session.setExceptionMessage(exceptionMessage);
                    sessionCache.put(monitorId, session);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("[监控管理] 记录异常信息失败，monitorId={}", monitorId, e);
            return false;
        }
    }

    /**
     * 强制断开用户的所有会话
     *
     * @param userId 用户ID
     * @return 断开的会话数量
     */
    public int forceTerminateUserSessions(Long userId) {
        log.info("[监控管理] 强制断开用户所有会话，userId={}", userId);

        try {
            int result = videoMonitorDao.forceTerminateUserSessions(userId, LocalDateTime.now());

            // 清理缓存
            Long sessionId = userSessionMap.remove(userId);
            if (sessionId != null) {
                sessionCache.remove(sessionId);
            }

            log.info("[监控管理] 强制断开用户会话完成，userId={}, count={}", userId, result);
            return result;

        } catch (Exception e) {
            log.error("[监控管理] 强制断开用户会话失败，userId={}", userId, e);
            return 0;
        }
    }

    /**
     * 清理过期会话
     *
     * @param expireHours 过期时间（小时）
     * @return 清理的会话数量
     */
    public int cleanupExpiredSessions(Integer expireHours) {
        log.info("[监控管理] 清理过期会话，expireHours={}", expireHours);

        try {
            int result = videoMonitorDao.cleanupExpiredSessions(expireHours);

            // 清理缓存（这里简单处理，实际应该更精确）
            sessionCache.entrySet().removeIf(entry -> {
                VideoMonitorEntity session = entry.getValue();
                return session.getStartTime() != null &&
                       session.getStartTime().isBefore(LocalDateTime.now().minusHours(expireHours));
            });

            log.info("[监控管理] 过期会话清理完成，count={}", result);
            return result;

        } catch (Exception e) {
            log.error("[监控管理] 清理过期会话失败", e);
            return 0;
        }
    }

    /**
     * 获取活跃会话列表
     *
     * @return 活跃会话列表
     */
    public List<VideoMonitorEntity> getActiveSessions() {
        try {
            return videoMonitorDao.selectActiveSessions();
        } catch (Exception e) {
            log.error("[监控管理] 获取活跃会话失败", e);
            return List.of();
        }
    }

    /**
     * 获取超时会话列表
     *
     * @param timeoutMinutes 超时时间（分钟）
     * @return 超时会话列表
     */
    public List<VideoMonitorEntity> getTimeoutSessions(Integer timeoutMinutes) {
        try {
            return videoMonitorDao.selectTimeoutSessions(timeoutMinutes);
        } catch (Exception e) {
            log.error("[监控管理] 获取超时会话失败", e);
            return List.of();
        }
    }

    /**
     * 获取会话统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getSessionStatistics() {
        try {
            Map<String, Object> statistics = new java.util.HashMap<>();

            // 总数统计
            statistics.put("totalSessions", videoMonitorDao.countMonitors(null, null, null));

            // 按状态统计
            Map<String, Object> statusStats = new java.util.HashMap<>();
            videoMonitorDao.countMonitorsByStatus().forEach(row -> {
                statusStats.put("status_" + row.get("session_status"), row.get("count"));
            });
            statistics.put("statusStatistics", statusStats);

            // 按客户端类型统计
            Map<String, Object> clientTypeStats = new java.util.HashMap<>();
            videoMonitorDao.countMonitorsByClientType().forEach(row -> {
                clientTypeStats.put("clientType_" + row.get("client_type"), row.get("count"));
            });
            statistics.put("clientTypeStatistics", clientTypeStats);

            // 按屏幕布局统计
            Map<String, Object> layoutStats = new java.util.HashMap<>();
            videoMonitorDao.countMonitorsByLayout().forEach(row -> {
                layoutStats.put("layout_" + row.get("screen_layout"), row.get("count"));
            });
            statistics.put("layoutStatistics", layoutStats);

            return statistics;

        } catch (Exception e) {
            log.error("[监控管理] 获取会话统计信息失败", e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 从缓存获取会话信息
     *
     * @param monitorId 监控会话ID
     * @return 会话信息
     */
    private VideoMonitorEntity getSessionFromCache(Long monitorId) {
        VideoMonitorEntity session = sessionCache.get(monitorId);
        if (session == null) {
            session = videoMonitorDao.selectById(monitorId);
            if (session != null) {
                sessionCache.put(monitorId, session);
            }
        }
        return session;
    }

    /**
     * 设置默认值
     *
     * @param monitorEntity 监控会话实体
     */
    private void setDefaultValues(VideoMonitorEntity monitorEntity) {
        if (monitorEntity.getSessionStatus() == null) {
            monitorEntity.setSessionStatus(1); // 默认为活跃状态
        }
        if (monitorEntity.getScreenCount() == null) {
            monitorEntity.setScreenCount(calculateScreenCount(monitorEntity.getScreenLayout()));
        }
        if (monitorEntity.getAudioEnabled() == null) {
            monitorEntity.setAudioEnabled(0); // 默认关闭音频
        }
        if (monitorEntity.getRecordEnabled() == null) {
            monitorEntity.setRecordEnabled(0); // 默认不录制
        }
        if (monitorEntity.getPtzEnabled() == null) {
            monitorEntity.setPtzEnabled(1); // 默认启用云台控制
        }
        if (monitorEntity.getNetworkStatus() == null) {
            monitorEntity.setNetworkStatus(1); // 默认网络状态良好
        }
    }

    /**
     * 根据屏幕布局计算屏幕数量
     *
     * @param screenLayout 屏幕布局
     * @return 屏幕数量
     */
    private Integer calculateScreenCount(Integer screenLayout) {
        switch (screenLayout) {
            case 1: return 1;  // 单屏
            case 2: return 4;  // 四分屏
            case 3: return 9;  // 九分屏
            case 4: return 16; // 十六分屏
            case 5: return 25; // 二十五分屏
            default: return 1;
        }
    }

    /**
     * 初始化定时任务
     */
    private void initScheduledTasks() {
        // 每5分钟检查超时会话
        scheduler.scheduleAtFixedRate(this::checkTimeoutSessions, 5, 5, TimeUnit.MINUTES);

        // 每小时清理过期会话
        scheduler.scheduleAtFixedRate(() -> cleanupExpiredSessions(24), 1, 1, TimeUnit.HOURS);
    }

    /**
     * 检查超时会话
     */
    private void checkTimeoutSessions() {
        try {
            List<VideoMonitorEntity> timeoutSessions = getTimeoutSessions(30); // 30分钟超时

            for (VideoMonitorEntity session : timeoutSessions) {
                log.warn("[监控管理] 发现超时会话，monitorId={}, lastActiveTime={}",
                        session.getMonitorId(), session.getLastActiveTime());

                // 自动断开超时会话
                terminateSession(session.getMonitorId());
            }

        } catch (Exception e) {
            log.error("[监控管理] 检查超时会话失败", e);
        }
    }
}
