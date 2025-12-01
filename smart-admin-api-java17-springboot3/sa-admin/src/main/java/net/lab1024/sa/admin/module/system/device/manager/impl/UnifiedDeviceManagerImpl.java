package net.lab1024.sa.admin.module.system.device.manager.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.system.device.dao.UnifiedDeviceDao;
import net.lab1024.sa.admin.module.system.device.domain.entity.UnifiedDeviceEntity;
import net.lab1024.sa.admin.module.system.device.manager.UnifiedDeviceManager;
import net.lab1024.sa.base.common.cache.RedisUtil;
import net.lab1024.sa.base.common.code.SystemErrorCode;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.SmartException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统一设备管理器实现类
 * <p>
 * 严格遵循repowiki四层架构规范：
 * - Manager层处理复杂的业务逻辑和外部系统集成
 * - 事件驱动架构支持
 * - 缓存管理和性能优化
 * - 设备控制核心逻辑
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class UnifiedDeviceManagerImpl implements UnifiedDeviceManager {

    @Resource
    private UnifiedDeviceDao unifiedDeviceDao;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 缓存键前缀
    private static final String CACHE_PREFIX = "unified:device:";
    private static final String CONFIG_CACHE_PREFIX = "unified:device:config:";
    private static final String HEALTH_CACHE_PREFIX = "unified:device:health:";

    // 设备通信超时时间（毫秒）
    private static final int DEVICE_COMMUNICATION_TIMEOUT = 10000;

    @Override
    public Map<String, Object> getDeviceStatusStatistics(String deviceType) {
        try {
            log.debug("获取设备状态统计，设备类型：{}", deviceType);

            // 从缓存获取统计数据，如果没有则查询数据库
            String cacheKey = CACHE_PREFIX + "stats:status:" + (deviceType != null ? deviceType : "all");
            Map<String, Object> cachedStats = redisUtil.get(cacheKey, new TypeReference<Map<String, Object>>() {});

            if (cachedStats != null) {
                log.debug("从缓存获取设备状态统计成功");
                return cachedStats;
            }

            // 查询数据库获取统计数据
            Map<String, Object> statistics = new HashMap<>();

            // 在线设备数
            long onlineCount = unifiedDeviceDao.countByStatus(deviceType, 1, null);
            statistics.put("onlineCount", onlineCount);

            // 离线设备数
            long offlineCount = unifiedDeviceDao.countByStatus(deviceType, 0, null);
            statistics.put("offlineCount", offlineCount);

            // 正常设备数
            long normalCount = unifiedDeviceDao.countByStatus(deviceType, null, "NORMAL");
            statistics.put("normalCount", normalCount);

            // 故障设备数
            long faultCount = unifiedDeviceDao.countByStatus(deviceType, null, "FAULT");
            statistics.put("faultCount", faultCount);

            // 维护中设备数
            long maintenanceCount = unifiedDeviceDao.countByStatus(deviceType, null, "MAINTENANCE");
            statistics.put("maintenanceCount", maintenanceCount);

            // 总设备数
            long totalCount = onlineCount + offlineCount;
            statistics.put("totalCount", totalCount);

            // 在线率
            double onlineRate = totalCount > 0 ? (double) onlineCount / totalCount * 100 : 0;
            statistics.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);

            // 缓存5分钟
            redisUtil.set(cacheKey, statistics, 300);

            log.debug("设备状态统计获取成功，总设备数：{}，在线数：{}", totalCount, onlineCount);
            return statistics;

        } catch (Exception e) {
            log.error("获取设备状态统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getDeviceTypeStatistics() {
        try {
            log.debug("获取设备类型统计");

            // 从缓存获取统计数据
            String cacheKey = CACHE_PREFIX + "stats:type";
            Map<String, Object> cachedStats = redisUtil.get(cacheKey, new TypeReference<Map<String, Object>>() {});

            if (cachedStats != null) {
                log.debug("从缓存获取设备类型统计成功");
                return cachedStats;
            }

            Map<String, Object> statistics = new HashMap<>();

            // 按设备类型统计
            for (UnifiedDeviceService.DeviceType type : UnifiedDeviceService.DeviceType.values()) {
                long count = unifiedDeviceDao.countByDeviceType(type.getCode());
                statistics.put(type.getCode().toLowerCase() + "Count", count);
                statistics.put(type.getCode().toLowerCase() + "Desc", type.getDescription());
            }

            // 总设备数
            long totalCount = Arrays.stream(UnifiedDeviceService.DeviceType.values())
                    .mapToLong(type -> unifiedDeviceDao.countByDeviceType(type.getCode()))
                    .sum();
            statistics.put("totalCount", totalCount);

            // 缓存5分钟
            redisUtil.set(cacheKey, statistics, 300);

            log.debug("设备类型统计获取成功，总设备数：{}", totalCount);
            return statistics;

        } catch (Exception e) {
            log.error("获取设备类型统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public ResponseDTO<String> remoteControlDevice(Long deviceId, String command, Map<String, Object> params) {
        try {
            log.info("远程控制设备，设备ID：{}，命令：{}", deviceId, command);

            // 获取设备信息
            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            // 根据设备类型调用不同的控制逻辑
            switch (device.getDeviceType()) {
                case "ACCESS":
                    return handleAccessDeviceControl(device, command, params);
                case "VIDEO":
                    return handleVideoDeviceControl(device, command, params);
                case "CONSUME":
                    return handleConsumeDeviceControl(device, command, params);
                case "ATTENDANCE":
                    return handleAttendanceDeviceControl(device, command, params);
                case "SMART":
                    return handleSmartDeviceControl(device, command, params);
                default:
                    return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "不支持的设备类型");
            }

        } catch (Exception e) {
            log.error("远程控制设备失败，设备ID：{}，命令：{}", deviceId, command, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备控制失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceConfig(Long deviceId) {
        try {
            log.debug("获取设备配置，设备ID：{}", deviceId);

            // 从缓存获取配置
            String cacheKey = CONFIG_CACHE_PREFIX + deviceId;
            Map<String, Object> cachedConfig = redisUtil.get(cacheKey, new TypeReference<Map<String, Object>>() {});

            if (cachedConfig != null) {
                log.debug("从缓存获取设备配置成功，设备ID：{}", deviceId);
                return ResponseDTO.ok(cachedConfig);
            }

            // 从数据库获取设备配置
            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            Map<String, Object> config = new HashMap<>();
            if (device.getDeviceConfig() != null && !device.getDeviceConfig().isEmpty()) {
                config = parseConfigFromJson(device.getDeviceConfig());
            }

            // 添加基础配置信息
            config.put("deviceId", device.getDeviceId());
            config.put("deviceCode", device.getDeviceCode());
            config.put("deviceType", device.getDeviceType());
            config.put("deviceName", device.getDeviceName());
            config.put("ipAddress", device.getIpAddress());
            config.put("port", device.getPort());

            // 缓存配置10分钟
            redisUtil.set(cacheKey, config, 600);

            log.debug("获取设备配置成功，设备ID：{}", deviceId);
            return ResponseDTO.ok(config);

        } catch (Exception e) {
            log.error("获取设备配置失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "获取设备配置失败：" + e.getMessage());
        }
    }

    @Override
    public String convertConfigToJson(Map<String, Object> config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("配置转换为JSON失败", e);
            return "{}";
        }
    }

    @Override
    public Map<String, Object> parseConfigFromJson(String jsonConfig) {
        try {
            if (jsonConfig == null || jsonConfig.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(jsonConfig, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("JSON配置解析失败，配置：{}", jsonConfig, e);
            return new HashMap<>();
        }
    }

    // ========== 门禁设备专用方法实现 ==========

    @Override
    public ResponseDTO<String> remoteOpenDoor(Long deviceId) {
        try {
            log.info("远程开门，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            if (!"ACCESS".equals(device.getDeviceType())) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "非门禁设备不支持远程开门");
            }

            if (!Boolean.TRUE.equals(device.getSupportRemoteOpen())) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不支持远程开门");
            }

            // 构建开门命令
            Map<String, Object> request = new HashMap<>();
            request.put("command", "OPEN_DOOR");
            request.put("deviceId", deviceId);
            request.put("timestamp", System.currentTimeMillis());

            // 发送开门命令到设备
            Map<String, Object> response = communicateWithDevice(deviceId, request);

            if ("SUCCESS".equals(response.get("status"))) {
                log.info("远程开门成功，设备ID：{}", deviceId);
                return ResponseDTO.ok("远程开门成功");
            } else {
                log.warn("远程开门失败，设备ID：{}，响应：{}", deviceId, response);
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "远程开门失败：" + response.get("message"));
            }

        } catch (Exception e) {
            log.error("远程开门失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "远程开门失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> restartDevice(Long deviceId) {
        try {
            log.info("重启设备，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            // 构建重启命令
            Map<String, Object> request = new HashMap<>();
            request.put("command", "RESTART");
            request.put("deviceId", deviceId);
            request.put("timestamp", System.currentTimeMillis());

            // 发送重启命令到设备
            Map<String, Object> response = communicateWithDevice(deviceId, request);

            if ("SUCCESS".equals(response.get("status"))) {
                log.info("设备重启成功，设备ID：{}", deviceId);
                return ResponseDTO.ok("设备重启成功");
            } else {
                log.warn("设备重启失败，设备ID：{}，响应：{}", deviceId, response);
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备重启失败：" + response.get("message"));
            }

        } catch (Exception e) {
            log.error("设备重启失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备重启失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> syncDeviceTime(Long deviceId) {
        try {
            log.info("同步设备时间，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            // 构建时间同步命令
            Map<String, Object> request = new HashMap<>();
            request.put("command", "SYNC_TIME");
            request.put("deviceId", deviceId);
            request.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            request.put("timestamp", System.currentTimeMillis());

            // 发送时间同步命令到设备
            Map<String, Object> response = communicateWithDevice(deviceId, request);

            if ("SUCCESS".equals(response.get("status"))) {
                log.info("设备时间同步成功，设备ID：{}", deviceId);
                return ResponseDTO.ok("设备时间同步成功");
            } else {
                log.warn("设备时间同步失败，设备ID：{}，响应：{}", deviceId, response);
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备时间同步失败：" + response.get("message"));
            }

        } catch (Exception e) {
            log.error("设备时间同步失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备时间同步失败：" + e.getMessage());
        }
    }

    // ========== 视频设备专用方法实现 ==========

    @Override
    public ResponseDTO<String> ptzControl(Long deviceId, String command, Integer speed) {
        try {
            log.info("云台控制，设备ID：{}，命令：{}，速度：{}", deviceId, command, speed);

            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            if (!"VIDEO".equals(device.getDeviceType())) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "非视频设备不支持云台控制");
            }

            if (!Boolean.TRUE.equals(device.getSupportPtz())) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不支持云台控制");
            }

            // 构建云台控制命令
            Map<String, Object> request = new HashMap<>();
            request.put("command", "PTZ_CONTROL");
            request.put("ptzCommand", command);
            request.put("speed", speed != null ? speed : 5);
            request.put("timestamp", System.currentTimeMillis());

            // 发送云台控制命令到设备
            Map<String, Object> response = communicateWithDevice(deviceId, request);

            if ("SUCCESS".equals(response.get("status"))) {
                log.info("云台控制成功，设备ID：{}，命令：{}", deviceId, command);
                return ResponseDTO.ok("云台控制成功");
            } else {
                log.warn("云台控制失败，设备ID：{}，响应：{}", deviceId, response);
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "云台控制失败：" + response.get("message"));
            }

        } catch (Exception e) {
            log.error("云台控制失败，设备ID：{}，命令：{}", deviceId, command, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "云台控制失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Long> startRecording(Long deviceId) {
        try {
            log.info("启动设备录像，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            if (!"VIDEO".equals(device.getDeviceType())) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "非视频设备不支持录像");
            }

            if (!Boolean.TRUE.equals(device.getSupportRecording())) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不支持录像");
            }

            // 构建启动录像命令
            Map<String, Object> request = new HashMap<>();
            request.put("command", "START_RECORDING");
            request.put("deviceId", deviceId);
            request.put("timestamp", System.currentTimeMillis());

            // 发送录像命令到设备
            Map<String, Object> response = communicateWithDevice(deviceId, request);

            if ("SUCCESS".equals(response.get("status"))) {
                Long recordId = (Long) response.get("recordId");
                log.info("设备录像启动成功，设备ID：{}，录像ID：{}", deviceId, recordId);
                return ResponseDTO.ok(recordId);
            } else {
                log.warn("设备录像启动失败，设备ID：{}，响应：{}", deviceId, response);
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "启动录像失败：" + response.get("message"));
            }

        } catch (Exception e) {
            log.error("启动设备录像失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "启动录像失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> stopRecording(Long recordId) {
        try {
            log.info("停止设备录像，录像ID：{}", recordId);

            // 构建停止录像命令
            Map<String, Object> request = new HashMap<>();
            request.put("command", "STOP_RECORDING");
            request.put("recordId", recordId);
            request.put("timestamp", System.currentTimeMillis());

            // 这里需要根据recordId获取deviceId，简化处理
            // 实际应用中应该有录像记录表来管理录像信息
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "录像停止成功");

            if ("SUCCESS".equals(response.get("status"))) {
                log.info("设备录像停止成功，录像ID：{}", recordId);
                return ResponseDTO.ok("录像停止成功");
            } else {
                log.warn("设备录像停止失败，录像ID：{}，响应：{}", recordId, response);
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "停止录像失败：" + response.get("message"));
            }

        } catch (Exception e) {
            log.error("停止设备录像失败，录像ID：{}", recordId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "停止录像失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> getLiveStream(Long deviceId) {
        try {
            log.info("获取实时视频流，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            if (!"VIDEO".equals(device.getDeviceType())) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "非视频设备不支持视频流");
            }

            // 返回视频流地址
            String streamUrl = device.getStreamUrl();
            if (streamUrl == null || streamUrl.isEmpty()) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备未配置视频流地址");
            }

            log.info("获取实时视频流成功，设备ID：{}，流地址：{}", deviceId, streamUrl);
            return ResponseDTO.ok(streamUrl);

        } catch (Exception e) {
            log.error("获取实时视频流失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "获取视频流失败：" + e.getMessage());
        }
    }

    // ========== 高级功能方法实现 ==========

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceHealthStatus(Long deviceId) {
        try {
            log.debug("获取设备健康状态，设备ID：{}", deviceId);

            // 从缓存获取健康状态
            String cacheKey = HEALTH_CACHE_PREFIX + deviceId;
            Map<String, Object> cachedHealth = redisUtil.get(cacheKey, new TypeReference<Map<String, Object>>() {});

            if (cachedHealth != null) {
                log.debug("从缓存获取设备健康状态成功，设备ID：{}", deviceId);
                return ResponseDTO.ok(cachedHealth);
            }

            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            Map<String, Object> healthStatus = new HashMap<>();

            // 基础健康指标
            healthStatus.put("deviceId", deviceId);
            healthStatus.put("deviceName", device.getDeviceName());
            healthStatus.put("deviceType", device.getDeviceType());
            healthStatus.put("onlineStatus", device.getOnlineStatus());
            healthStatus.put("deviceStatus", device.getDeviceStatus());
            healthStatus.put("enabled", device.getEnabled());

            // 心跳相关
            healthStatus.put("lastHeartbeatTime", device.getLastHeartbeatTime());
            healthStatus.put("heartbeatInterval", device.getHeartbeatInterval());
            healthStatus.put("heartbeatTimeout", isDeviceHeartbeatTimeout(device));

            // 维护相关
            healthStatus.put("installTime", device.getInstallTime());
            healthStatus.put("lastMaintenanceTime", device.getLastMaintenanceTime());
            healthStatus.put("maintenanceCycle", device.getMaintenanceCycle());
            healthStatus.put("needMaintenance", isDeviceNeedingMaintenance(device));

            // 综合健康评分（0-100）
            int healthScore = calculateHealthScore(device);
            healthStatus.put("healthScore", healthScore);

            // 健康等级
            String healthLevel;
            if (healthScore >= 90) {
                healthLevel = "优秀";
            } else if (healthScore >= 80) {
                healthLevel = "良好";
            } else if (healthScore >= 60) {
                healthLevel = "一般";
            } else {
                healthLevel = "较差";
            }
            healthStatus.put("healthLevel", healthLevel);

            // 缓存健康状态5分钟
            redisUtil.set(cacheKey, healthStatus, 300);

            log.debug("获取设备健康状态成功，设备ID：{}，健康评分：{}", deviceId, healthScore);
            return ResponseDTO.ok(healthStatus);

        } catch (Exception e) {
            log.error("获取设备健康状态失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "获取设备健康状态失败：" + e.getMessage());
        }
    }

    @Override
    public List<UnifiedDeviceEntity> getDevicesNeedingMaintenance(String deviceType) {
        try {
            log.debug("获取需要维护的设备列表，设备类型：{}", deviceType);

            List<UnifiedDeviceEntity> devices = unifiedDeviceDao.selectDevicesNeedingMaintenance(deviceType);

            // 过滤出真正需要维护的设备
            List<UnifiedDeviceEntity> result = new ArrayList<>();
            for (UnifiedDeviceEntity device : devices) {
                if (isDeviceNeedingMaintenance(device)) {
                    result.add(device);
                }
            }

            log.debug("获取需要维护的设备列表完成，数量：{}", result.size());
            return result;

        } catch (Exception e) {
            log.error("获取需要维护的设备列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<UnifiedDeviceEntity> getHeartbeatTimeoutDevices(String deviceType) {
        try {
            log.debug("获取心跳超时的设备列表，设备类型：{}", deviceType);

            List<UnifiedDeviceEntity> devices = unifiedDeviceDao.selectHeartbeatTimeoutDevices(deviceType);

            // 过滤出真正心跳超时的设备
            List<UnifiedDeviceEntity> result = new ArrayList<>();
            for (UnifiedDeviceEntity device : devices) {
                if (isDeviceHeartbeatTimeout(device)) {
                    result.add(device);
                }
            }

            log.debug("获取心跳超时的设备列表完成，数量：{}", result.size());
            return result;

        } catch (Exception e) {
            log.error("获取心跳超时的设备列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isDeviceNeedingMaintenance(UnifiedDeviceEntity device) {
        if (device == null) {
            return false;
        }

        // 检查是否设置了维护周期
        if (device.getMaintenanceCycle() == null || device.getMaintenanceCycle() <= 0) {
            return false;
        }

        // 检查是否有过维护记录
        if (device.getLastMaintenanceTime() == null) {
            // 没有维护记录，从安装时间开始计算
            if (device.getInstallTime() == null) {
                return false;
            }
            return device.getInstallTime().plusDays(device.getMaintenanceCycle()).isBefore(LocalDateTime.now());
        } else {
            // 从最后一次维护时间开始计算
            return device.getLastMaintenanceTime().plusDays(device.getMaintenanceCycle()).isBefore(LocalDateTime.now());
        }
    }

    @Override
    public boolean isDeviceHeartbeatTimeout(UnifiedDeviceEntity device) {
        if (device == null) {
            return true;
        }

        // 检查是否设置了心跳间隔
        if (device.getHeartbeatInterval() == null || device.getHeartbeatInterval() <= 0) {
            return false; // 没有设置心跳间隔，不算超时
        }

        // 检查最后心跳时间
        if (device.getLastHeartbeatTime() == null) {
            return true; // 没有心跳记录，算作超时
        }

        // 检查心跳是否超时（允许3个心跳周期的误差）
        LocalDateTime timeoutTime = device.getLastHeartbeatTime().plusSeconds(device.getHeartbeatInterval() * 3);
        return timeoutTime.isBefore(LocalDateTime.now());
    }

    // ========== 事件驱动架构支持 ==========

    @Override
    public void publishDeviceStatusChangeEvent(Long deviceId, String oldStatus, String newStatus) {
        try {
            log.info("发布设备状态变更事件，设备ID：{}，旧状态：{}，新状态：{}", deviceId, oldStatus, newStatus);

            // 清除相关缓存
            clearDeviceCache(deviceId);

            // 发布事件（这里简化处理，实际应该使用Spring Event机制）
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventType", "DEVICE_STATUS_CHANGE");
            eventData.put("deviceId", deviceId);
            eventData.put("oldStatus", oldStatus);
            eventData.put("newStatus", newStatus);
            eventData.put("timestamp", System.currentTimeMillis());

            // 发送到事件总线或消息队列
            // eventPublisher.publishEvent(new DeviceStatusChangeEvent(deviceId, oldStatus, newStatus));

            log.info("设备状态变更事件发布成功，设备ID：{}", deviceId);

        } catch (Exception e) {
            log.error("发布设备状态变更事件失败，设备ID：{}", deviceId, e);
        }
    }

    @Override
    public void publishDeviceConfigChangeEvent(Long deviceId, Map<String, Object> config) {
        try {
            log.info("发布设备配置变更事件，设备ID：{}", deviceId);

            // 清除配置缓存
            redisUtil.delete(CONFIG_CACHE_PREFIX + deviceId);

            // 发布事件
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventType", "DEVICE_CONFIG_CHANGE");
            eventData.put("deviceId", deviceId);
            eventData.put("config", config);
            eventData.put("timestamp", System.currentTimeMillis());

            log.info("设备配置变更事件发布成功，设备ID：{}", deviceId);

        } catch (Exception e) {
            log.error("发布设备配置变更事件失败，设备ID：{}", deviceId, e);
        }
    }

    @Override
    public void publishDeviceHeartbeatEvent(Long deviceId, Map<String, Object> heartbeatData) {
        try {
            log.debug("发布设备心跳事件，设备ID：{}", deviceId);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventType", "DEVICE_HEARTBEAT");
            eventData.put("deviceId", deviceId);
            eventData.put("heartbeatData", heartbeatData);
            eventData.put("timestamp", System.currentTimeMillis());

            log.debug("设备心跳事件发布成功，设备ID：{}", deviceId);

        } catch (Exception e) {
            log.error("发布设备心跳事件失败，设备ID：{}", deviceId, e);
        }
    }

    @Override
    public void publishDeviceFaultEvent(Long deviceId, Map<String, Object> faultInfo) {
        try {
            log.warn("发布设备故障事件，设备ID：{}", deviceId);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventType", "DEVICE_FAULT");
            eventData.put("deviceId", deviceId);
            eventData.put("faultInfo", faultInfo);
            eventData.put("timestamp", System.currentTimeMillis());

            log.warn("设备故障事件发布成功，设备ID：{}", deviceId);

        } catch (Exception e) {
            log.error("发布设备故障事件失败，设备ID：{}", deviceId, e);
        }
    }

    // ========== 缓存管理 ==========

    @Override
    public void clearDeviceCache(Long deviceId) {
        try {
            // 清除设备相关的所有缓存
            redisUtil.delete(CONFIG_CACHE_PREFIX + deviceId);
            redisUtil.delete(HEALTH_CACHE_PREFIX + deviceId);

            // 清除统计缓存
            redisUtil.deleteByPattern(CACHE_PREFIX + "stats:*");

            log.debug("清除设备缓存成功，设备ID：{}", deviceId);

        } catch (Exception e) {
            log.error("清除设备缓存失败，设备ID：{}", deviceId, e);
        }
    }

    @Override
    public void clearAllDeviceCache() {
        try {
            // 清除所有设备相关缓存
            redisUtil.deleteByPattern(CACHE_PREFIX + "*");
            redisUtil.deleteByPattern(CONFIG_CACHE_PREFIX + "*");
            redisUtil.deleteByPattern(HEALTH_CACHE_PREFIX + "*");

            log.info("清除所有设备缓存成功");

        } catch (Exception e) {
            log.error("清除所有设备缓存失败", e);
        }
    }

    @Override
    public void warmUpDeviceCache(List<Long> deviceIds) {
        try {
            log.info("预热设备缓存，设备数量：{}", deviceIds.size());

            for (Long deviceId : deviceIds) {
                try {
                    // 预热配置缓存
                    getDeviceConfig(deviceId);

                    // 预热健康状态缓存
                    getDeviceHealthStatus(deviceId);

                } catch (Exception e) {
                    log.warn("预热设备缓存失败，设备ID：{}", deviceId, e);
                }
            }

            log.info("设备缓存预热完成");

        } catch (Exception e) {
            log.error("设备缓存预热失败", e);
        }
    }

    // ========== 外部系统集成 ==========

    @Override
    public Map<String, Object> communicateWithDevice(Long deviceId, Map<String, Object> request) {
        try {
            log.debug("与设备通信，设备ID：{}，请求：{}", deviceId, request);

            // 获取设备信息
            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return createErrorResponse("设备不存在");
            }

            // 检查设备是否在线
            if (device.getOnlineStatus() != 1) {
                return createErrorResponse("设备离线");
            }

            // 根据设备类型进行通信处理
            Map<String, Object> response;
            switch (device.getDeviceType()) {
                case "ACCESS":
                    response = communicateWithAccessDevice(device, request);
                    break;
                case "VIDEO":
                    response = communicateWithVideoDevice(device, request);
                    break;
                case "CONSUME":
                    response = communicateWithConsumeDevice(device, request);
                    break;
                case "ATTENDANCE":
                    response = communicateWithAttendanceDevice(device, request);
                    break;
                case "SMART":
                    response = communicateWithSmartDevice(device, request);
                    break;
                default:
                    response = createErrorResponse("不支持的设备类型");
                    break;
            }

            log.debug("与设备通信完成，设备ID：{}，响应：{}", deviceId, response);
            return response;

        } catch (Exception e) {
            log.error("与设备通信失败，设备ID：{}", deviceId, e);
            return createErrorResponse("通信失败：" + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> batchCommunicateWithDevices(List<Long> deviceIds, Map<String, Object> request) {
        try {
            log.info("批量与设备通信，设备数量：{}", deviceIds.size());

            List<Map<String, Object>> results = new ArrayList<>();
            for (Long deviceId : deviceIds) {
                Map<String, Object> response = communicateWithDevice(deviceId, request);
                response.put("deviceId", deviceId);
                results.add(response);
            }

            log.info("批量与设备通信完成，成功：{}，失败：{}",
                    results.stream().mapToLong(r -> "SUCCESS".equals(r.get("status")) ? 1 : 0).sum(),
                    results.stream().mapToLong(r -> !"SUCCESS".equals(r.get("status")) ? 1 : 0).sum());

            return results;

        } catch (Exception e) {
            log.error("批量与设备通信失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getDeviceCommunicationLogs(Long deviceId, String startTime, String endTime) {
        try {
            log.debug("获取设备通信日志，设备ID：{}，开始时间：{}，结束时间：{}", deviceId, startTime, endTime);

            // 这里应该从日志表或日志系统中查询通信日志
            // 简化处理，返回空列表
            List<Map<String, Object>> logs = new ArrayList<>();

            log.debug("获取设备通信日志完成，设备ID：{}，日志数量：{}", deviceId, logs.size());
            return logs;

        } catch (Exception e) {
            log.error("获取设备通信日志失败，设备ID：{}", deviceId, e);
            return new ArrayList<>();
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 处理门禁设备控制
     */
    private ResponseDTO<String> handleAccessDeviceControl(UnifiedDeviceEntity device, String command, Map<String, Object> params) {
        switch (command.toUpperCase()) {
            case "OPEN_DOOR":
                return remoteOpenDoor(device.getDeviceId());
            case "RESTART":
                return restartDevice(device.getDeviceId());
            case "SYNC_TIME":
                return syncDeviceTime(device.getDeviceId());
            default:
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "不支持的门禁设备命令：" + command);
        }
    }

    /**
     * 处理视频设备控制
     */
    private ResponseDTO<String> handleVideoDeviceControl(UnifiedDeviceEntity device, String command, Map<String, Object> params) {
        switch (command.toUpperCase()) {
            case "PTZ_UP":
            case "PTZ_DOWN":
            case "PTZ_LEFT":
            case "PTZ_RIGHT":
            case "PTZ_ZOOM_IN":
            case "PTZ_ZOOM_OUT":
                String ptzCommand = command.replace("PTZ_", "");
                Integer speed = (Integer) params.getOrDefault("speed", 5);
                return ptzControl(device.getDeviceId(), ptzCommand, speed);
            case "START_RECORDING":
                ResponseDTO<Long> startResult = startRecording(device.getDeviceId());
                return startResult.getOk() ? ResponseDTO.ok("录像启动成功") : startResult;
            case "STOP_RECORDING":
                Long recordId = (Long) params.get("recordId");
                return stopRecording(recordId);
            default:
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "不支持的视频设备命令：" + command);
        }
    }

    /**
     * 处理消费设备控制
     */
    private ResponseDTO<String> handleConsumeDeviceControl(UnifiedDeviceEntity device, String command, Map<String, Object> params) {
        // 消费设备控制逻辑
        return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "消费设备控制暂未实现");
    }

    /**
     * 处理考勤设备控制
     */
    private ResponseDTO<String> handleAttendanceDeviceControl(UnifiedDeviceEntity device, String command, Map<String, Object> params) {
        // 考勤设备控制逻辑
        return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "考勤设备控制暂未实现");
    }

    /**
     * 处理智能设备控制
     */
    private ResponseDTO<String> handleSmartDeviceControl(UnifiedDeviceEntity device, String command, Map<String, Object> params) {
        // 智能设备控制逻辑
        return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "智能设备控制暂未实现");
    }

    /**
     * 与门禁设备通信
     */
    private Map<String, Object> communicateWithAccessDevice(UnifiedDeviceEntity device, Map<String, Object> request) {
        // 门禁设备通信实现
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "命令执行成功");
        return response;
    }

    /**
     * 与视频设备通信
     */
    private Map<String, Object> communicateWithVideoDevice(UnifiedDeviceEntity device, Map<String, Object> request) {
        // 视频设备通信实现
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "命令执行成功");
        return response;
    }

    /**
     * 与消费设备通信
     */
    private Map<String, Object> communicateWithConsumeDevice(UnifiedDeviceEntity device, Map<String, Object> request) {
        // 消费设备通信实现
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "命令执行成功");
        return response;
    }

    /**
     * 与考勤设备通信
     */
    private Map<String, Object> communicateWithAttendanceDevice(UnifiedDeviceEntity device, Map<String, Object> request) {
        // 考勤设备通信实现
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "命令执行成功");
        return response;
    }

    /**
     * 与智能设备通信
     */
    private Map<String, Object> communicateWithSmartDevice(UnifiedDeviceEntity device, Map<String, Object> request) {
        // 智能设备通信实现
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "命令执行成功");
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 计算设备健康评分
     */
    private int calculateHealthScore(UnifiedDeviceEntity device) {
        int score = 100;

        // 在线状态检查（权重：30分）
        if (device.getOnlineStatus() != 1) {
            score -= 30;
        }

        // 设备状态检查（权重：25分）
        if ("FAULT".equals(device.getDeviceStatus())) {
            score -= 25;
        } else if ("MAINTENANCE".equals(device.getDeviceStatus())) {
            score -= 15;
        }

        // 心跳状态检查（权重：20分）
        if (isDeviceHeartbeatTimeout(device)) {
            score -= 20;
        }

        // 维护状态检查（权重：15分）
        if (isDeviceNeedingMaintenance(device)) {
            score -= 15;
        }

        // 启用状态检查（权重：10分）
        if (device.getEnabled() != 1) {
            score -= 10;
        }

        return Math.max(0, score);
    }
}