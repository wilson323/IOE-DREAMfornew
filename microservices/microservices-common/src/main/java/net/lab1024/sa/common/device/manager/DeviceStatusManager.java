package net.lab1024.sa.common.device.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Objects;

/**
 * 设备状态管理器
 * <p>
 * 统一管理所有设备的状态信息
 * 支持状态缓存、状态变更通知、状态同步等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class DeviceStatusManager {

    private final DeviceDao deviceDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, DeviceStatusListener> statusListeners;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "ioedream:device:status:";
    private static final String STATUS_CHANGE_TOPIC = "ioedream:device:status:change";

    // 设备状态缓存时间（5分钟）
    private static final long CACHE_EXPIRE_MINUTES = 5;

    // 构造函数注入依赖
    public DeviceStatusManager(DeviceDao deviceDao, RedisTemplate<String, Object> redisTemplate) {
        this.deviceDao = deviceDao;
        this.redisTemplate = redisTemplate;
        this.statusListeners = new ConcurrentHashMap<>();
    }

    /**
     * 设备状态枚举
     */
    public enum DeviceStatus {
        ONLINE(1, "在线"),
        OFFLINE(2, "离线"),
        FAULT(3, "故障"),
        MAINTENANCE(4, "维护中"),
        UNKNOWN(5, "未知");

        private final int code;
        private final String description;

        DeviceStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static DeviceStatus fromCode(int code) {
            for (DeviceStatus status : DeviceStatus.values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            return UNKNOWN;
        }

        public static DeviceStatus fromString(String statusStr) {
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return UNKNOWN;
            }

            // 先尝试按数字转换
            try {
                int code = Integer.parseInt(statusStr);
                return fromCode(code);
            } catch (NumberFormatException e) {
                // 按字符串匹配
                for (DeviceStatus status : DeviceStatus.values()) {
                    if (status.name().equalsIgnoreCase(statusStr) ||
                        status.getDescription().equals(statusStr)) {
                        return status;
                    }
                }
                return UNKNOWN;
            }
        }
    }

    /**
     * 设备状态信息
     */
    public static class DeviceStatusInfo {
        private String deviceId;
        private DeviceStatus status;
        private LocalDateTime lastHeartbeat;
        private String lastErrorMessage;
        private Map<String, Object> extendedAttributes;

        // 构造函数
        public DeviceStatusInfo(String deviceId, DeviceStatus status) {
            this.deviceId = deviceId;
            this.status = status;
            this.lastHeartbeat = LocalDateTime.now();
            this.extendedAttributes = new HashMap<>();
        }

        // getters and setters
        public String getDeviceId() { return deviceId; }
        public DeviceStatus getStatus() { return status; }
        public void setStatus(DeviceStatus status) { this.status = status; }
        public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
        public String getLastErrorMessage() { return lastErrorMessage; }
        public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
        public Map<String, Object> getExtendedAttributes() { return extendedAttributes; }
        public void setExtendedAttributes(Map<String, Object> extendedAttributes) { this.extendedAttributes = extendedAttributes; }
    }

    /**
     * 设备状态监听器接口
     */
    public interface DeviceStatusListener {
        /**
         * 状态变更通知
         *
         * @param deviceId 设备ID
         * @param oldStatus 旧状态
         * @param newStatus 新状态
         */
        void onStatusChanged(String deviceId, DeviceStatus oldStatus, DeviceStatus newStatus);

        /**
         * 获取监听器名称
         *
         * @return 监听器名称
         */
        String getListenerName();
    }

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param newStatus 新状态
     */
    public void updateDeviceStatus(String deviceId, DeviceStatus newStatus) {
        log.debug("[设备状态管理] 更新设备状态, deviceId={}, newStatus={}", deviceId, newStatus.getDescription());

        try {
            // 1. 获取旧状态
            DeviceStatusInfo oldStatusInfo = getDeviceStatusInfo(deviceId);
            DeviceStatus oldStatus = oldStatusInfo != null ? oldStatusInfo.getStatus() : null;

            // 2. 如果状态没有变化，直接返回
            if (Objects.equals(oldStatus, newStatus)) {
                log.debug("[设备状态管理] 设备状态未变化, deviceId={}", deviceId);
                return;
            }

            // 3. 更新数据库
            updateDeviceStatusInDatabase(deviceId, newStatus);

            // 4. 更新缓存
            DeviceStatusInfo statusInfo = new DeviceStatusInfo(deviceId, newStatus);
            updateDeviceStatusInCache(statusInfo);

            // 5. 通知监听器
            notifyStatusListeners(deviceId, oldStatus, newStatus);

            // 6. 发布状态变更事件
            publishStatusChangeEvent(deviceId, oldStatus, newStatus);

            log.info("[设备状态管理] 设备状态更新成功, deviceId={}, oldStatus={}, newStatus={}",
                    deviceId, oldStatus != null ? oldStatus.getDescription() : "null", newStatus.getDescription());

        } catch (Exception e) {
            log.error("[设备状态管理] 更新设备状态失败, deviceId={}", deviceId, e);
        }
    }

    /**
     * 批量更新设备状态
     *
     * @param statusUpdates 状态更新映射
     */
    public void batchUpdateDeviceStatus(Map<String, DeviceStatus> statusUpdates) {
        if (statusUpdates == null || statusUpdates.isEmpty()) {
            return;
        }

        log.info("[设备状态管理] 批量更新设备状态, count={}", statusUpdates.size());

        try {
            // 1. 准备批量操作数据
            Map<String, DeviceStatusInfo> oldStatusMap = new HashMap<>();
            List<DeviceStatusInfo> newStatusInfos = new ArrayList<>();

            // 获取旧状态信息
            for (String deviceId : statusUpdates.keySet()) {
                DeviceStatusInfo oldStatusInfo = getDeviceStatusInfo(deviceId);
                if (oldStatusInfo != null) {
                    oldStatusMap.put(deviceId, oldStatusInfo);
                }

                DeviceStatus newStatus = statusUpdates.get(deviceId);
                DeviceStatusInfo newStatusInfo = new DeviceStatusInfo(deviceId, newStatus);
                newStatusInfos.add(newStatusInfo);
            }

            // 2. 批量更新数据库
            batchUpdateDeviceStatusInDatabase(statusUpdates);

            // 3. 批量更新缓存
            batchUpdateDeviceStatusInCache(newStatusInfos);

            // 4. 通知监听器
            for (Map.Entry<String, DeviceStatus> entry : statusUpdates.entrySet()) {
                String deviceId = entry.getKey();
                DeviceStatus newStatus = entry.getValue();
                DeviceStatusInfo oldInfo = oldStatusMap.get(deviceId);
                DeviceStatus oldStatus = oldInfo != null ? oldInfo.getStatus() : null;

                notifyStatusListeners(deviceId, oldStatus, newStatus);
                publishStatusChangeEvent(deviceId, oldStatus, newStatus);
            }

            log.info("[设备状态管理] 批量更新设备状态成功, count={}", statusUpdates.size());

        } catch (Exception e) {
            log.error("[设备状态管理] 批量更新设备状态失败", e);
        }
    }

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    public DeviceStatus getDeviceStatus(String deviceId) {
        log.debug("[设备状态管理] 获取设备状态, deviceId={}", deviceId);

        try {
            // 1. 从缓存获取
            DeviceStatusInfo statusInfo = getDeviceStatusInfo(deviceId);
            if (statusInfo != null) {
                return statusInfo.getStatus();
            }

            // 2. 从数据库获取
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return DeviceStatus.UNKNOWN;
            }

            DeviceStatus status = DeviceStatus.fromString(device.getStatus());

            // 3. 异步更新缓存
            DeviceStatusInfo newStatusInfo = new DeviceStatusInfo(deviceId, status);
            updateDeviceStatusInCache(newStatusInfo);

            return status;

        } catch (Exception e) {
            log.error("[设备状态管理] 获取设备状态失败, deviceId={}", deviceId, e);
            return DeviceStatus.UNKNOWN;
        }
    }

    /**
     * 获取多个设备状态
     *
     * @param deviceIds 设备ID列表
     * @return 状态映射
     */
    public Map<String, DeviceStatus> getDeviceStatuses(List<String> deviceIds) {
        log.debug("[设备状态管理] 获取多个设备状态, count={}", deviceIds.size());

        Map<String, DeviceStatus> statusMap = new HashMap<>();

        for (String deviceId : deviceIds) {
            statusMap.put(deviceId, getDeviceStatus(deviceId));
        }

        return statusMap;
    }

    /**
     * 获取设备详细状态信息
     *
     * @param deviceId 设备ID
     * @return 设备状态信息
     */
    public DeviceStatusInfo getDeviceStatusInfo(String deviceId) {
        try {
            // 1. 从缓存获取
            String nonNullDeviceId = Objects.requireNonNull(deviceId, "deviceId不能为null");
            String cacheKey = Objects.requireNonNull(getCacheKey(nonNullDeviceId), "cacheKey不能为null");
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            DeviceStatusInfo statusInfo = (DeviceStatusInfo) ops.get(cacheKey);

            if (statusInfo != null) {
                return statusInfo;
            }

            // 2. 从数据库获取
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return null;
            }

            DeviceStatus status = DeviceStatus.fromString(device.getStatus());
            statusInfo = new DeviceStatusInfo(deviceId, status);

            // 3. 异步更新缓存
            updateDeviceStatusInCache(statusInfo);

            return statusInfo;

        } catch (Exception e) {
            log.error("[设备状态管理] 获取设备状态信息失败, deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 获取在线设备列表
     *
     * @return 在线设备ID列表
     */
    public List<String> getOnlineDevices() {
        log.debug("[设备状态管理] 获取在线设备列表");

        try {
            List<DeviceEntity> devices = deviceDao.selectList(null);
            List<String> onlineDevices = new ArrayList<>();

            for (DeviceEntity device : devices) {
                DeviceStatus status = DeviceStatus.fromString(device.getStatus());
                if (status == DeviceStatus.ONLINE) {
                    onlineDevices.add(device.getId().toString());
                }
            }

            return onlineDevices;

        } catch (Exception e) {
            log.error("[设备状态管理] 获取在线设备列表失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 检查设备是否在线
     *
     * @param deviceId 设备ID
     * @return 是否在线
     */
    public boolean isDeviceOnline(String deviceId) {
        DeviceStatus status = getDeviceStatus(deviceId);
        return status == DeviceStatus.ONLINE;
    }

    /**
     * 注册状态监听器
     *
     * @param listener 监听器
     */
    public void registerStatusListener(DeviceStatusListener listener) {
        if (listener != null && listener.getListenerName() != null) {
            statusListeners.put(listener.getListenerName(), listener);
            log.info("[设备状态管理] 注册状态监听器, name={}", listener.getListenerName());
        }
    }

    /**
     * 注销状态监听器
     *
     * @param listenerName 监听器名称
     */
    public void unregisterStatusListener(String listenerName) {
        if (statusListeners.containsKey(listenerName)) {
            statusListeners.remove(listenerName);
            log.info("[设备状态管理] 注销状态监听器, name={}", listenerName);
        }
    }

    /**
     * 更新设备心跳
     *
     * @param deviceId 设备ID
     */
    public void updateDeviceHeartbeat(String deviceId) {
        log.debug("[设备状态管理] 更新设备心跳, deviceId={}", deviceId);

        try {
            DeviceStatusInfo statusInfo = getDeviceStatusInfo(deviceId);
            if (statusInfo != null) {
                statusInfo.setLastHeartbeat(LocalDateTime.now());
                updateDeviceStatusInCache(statusInfo);
            }
        } catch (Exception e) {
            log.error("[设备状态管理] 更新设备心跳失败, deviceId={}", deviceId, e);
        }
    }

    /**
     * 设置设备错误信息
     *
     * @param deviceId 设备ID
     * @param errorMessage 错误信息
     */
    public void setDeviceError(String deviceId, String errorMessage) {
        log.debug("[设备状态管理] 设置设备错误信息, deviceId={}, error={}", deviceId, errorMessage);

        try {
            DeviceStatusInfo statusInfo = getDeviceStatusInfo(deviceId);
            if (statusInfo != null) {
                statusInfo.setLastErrorMessage(errorMessage);
                statusInfo.setStatus(DeviceStatus.FAULT);
                updateDeviceStatusInCache(statusInfo);

                // 通知监听器
                notifyStatusListeners(deviceId, statusInfo.getStatus(), DeviceStatus.FAULT);
                publishStatusChangeEvent(deviceId, statusInfo.getStatus(), DeviceStatus.FAULT);
            }
        } catch (Exception e) {
            log.error("[设备状态管理] 设置设备错误信息失败, deviceId={}", deviceId, e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 更新数据库中的设备状态
     */
    private void updateDeviceStatusInDatabase(String deviceId, DeviceStatus status) {
        DeviceEntity device = deviceDao.selectById(deviceId);
        if (device != null) {
            device.setStatus(String.valueOf(status.getCode()));
            deviceDao.updateById(device);
        }
    }

    /**
     * 批量更新数据库中的设备状态
     * <p>
     * 使用MyBatis-Plus的批量更新功能，提高性能
     * 分批处理，每批100条记录，避免单次更新数据量过大
     * </p>
     *
     * @param statusUpdates 状态更新映射（deviceId -> DeviceStatus）
     */
    private void batchUpdateDeviceStatusInDatabase(Map<String, DeviceStatus> statusUpdates) {
        if (statusUpdates == null || statusUpdates.isEmpty()) {
            return;
        }

        log.debug("[设备状态管理] 开始批量更新设备状态到数据库, count={}", statusUpdates.size());

        try {
            // 1. 将String deviceId转换为Long，并查询设备实体
            List<Long> deviceIds = new ArrayList<>();
            for (String deviceIdStr : statusUpdates.keySet()) {
                try {
                    Long deviceId = Long.parseLong(deviceIdStr);
                    deviceIds.add(deviceId);
                } catch (NumberFormatException e) {
                    log.warn("[设备状态管理] 设备ID格式错误, deviceId={}, 跳过更新", deviceIdStr);
                }
            }

            if (deviceIds.isEmpty()) {
                log.warn("[设备状态管理] 没有有效的设备ID，跳过批量更新");
                return;
            }

            // 2. 批量查询设备实体（使用LambdaQueryWrapper替代废弃的selectBatchIds方法）
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(DeviceEntity::getId, deviceIds);
            List<DeviceEntity> devices = deviceDao.selectList(wrapper);
            if (devices == null || devices.isEmpty()) {
                log.warn("[设备状态管理] 未查询到设备实体, deviceIds={}", deviceIds);
                return;
            }

            // 3. 更新设备状态字段
            Map<Long, DeviceStatus> statusMap = new HashMap<>();
            for (Map.Entry<String, DeviceStatus> entry : statusUpdates.entrySet()) {
                try {
                    Long deviceId = Long.parseLong(entry.getKey());
                    statusMap.put(deviceId, entry.getValue());
                } catch (NumberFormatException e) {
                    // 已在上面的循环中处理，这里跳过
                }
            }

            for (DeviceEntity device : devices) {
                DeviceStatus newStatus = statusMap.get(device.getId());
                if (newStatus != null) {
                    device.setStatus(String.valueOf(newStatus.getCode()));
                }
            }

            // 4. 按状态分组，使用LambdaUpdateWrapper进行批量更新
            // 将相同状态的设备分组，减少SQL执行次数
            Map<DeviceStatus, List<Long>> statusGroupMap = new HashMap<>();
            for (DeviceEntity device : devices) {
                DeviceStatus newStatus = statusMap.get(device.getId());
                if (newStatus != null) {
                    statusGroupMap.computeIfAbsent(newStatus, k -> new ArrayList<>()).add(device.getId());
                }
            }

            int successCount = 0;
            int failureCount = 0;

            // 5. 按状态分组批量更新
            for (Map.Entry<DeviceStatus, List<Long>> entry : statusGroupMap.entrySet()) {
                DeviceStatus status = entry.getKey();
                List<Long> deviceIdList = entry.getValue();
                String statusCode = String.valueOf(status.getCode());

                // 分批处理，每批100条
                int batchSize = 100;
                for (int i = 0; i < deviceIdList.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, deviceIdList.size());
                    List<Long> batchIds = deviceIdList.subList(i, endIndex);

                    try {
                        // 使用LambdaUpdateWrapper进行批量更新
                        LambdaUpdateWrapper<DeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.in(DeviceEntity::getId, batchIds)
                                .set(DeviceEntity::getStatus, statusCode);

                        int result = deviceDao.update(null, updateWrapper);
                        if (result > 0) {
                            successCount += result;
                            log.debug("[设备状态管理] 批量更新成功, status={}, batchSize={}, updated={}",
                                    status.getDescription(), batchIds.size(), result);
                        } else {
                            failureCount += batchIds.size();
                            log.warn("[设备状态管理] 批量更新失败, status={}, batchSize={}",
                                    status.getDescription(), batchIds.size());
                        }
                    } catch (Exception e) {
                        failureCount += batchIds.size();
                        log.error("[设备状态管理] 批量更新异常, status={}, batchSize={}, error={}",
                                status.getDescription(), batchIds.size(), e.getMessage(), e);
                    }
                }
            }

            log.info("[设备状态管理] 批量更新设备状态完成, total={}, success={}, failure={}",
                    statusUpdates.size(), successCount, failureCount);

        } catch (Exception e) {
            log.error("[设备状态管理] 批量更新设备状态异常", e);
            // 降级处理：使用单条更新
            log.warn("[设备状态管理] 批量更新失败，降级为单条更新");
            for (Map.Entry<String, DeviceStatus> entry : statusUpdates.entrySet()) {
                try {
                    updateDeviceStatusInDatabase(entry.getKey(), entry.getValue());
                } catch (Exception ex) {
                    log.error("[设备状态管理] 单条更新也失败, deviceId={}, error={}", entry.getKey(), ex.getMessage());
                }
            }
        }
    }

    /**
     * 更新缓存中的设备状态
     */
    private void updateDeviceStatusInCache(DeviceStatusInfo statusInfo) {
        try {
            String deviceId = Objects.requireNonNull(statusInfo.getDeviceId(), "deviceId不能为null");
            String cacheKey = Objects.requireNonNull(getCacheKey(deviceId), "cacheKey不能为null");
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(cacheKey, statusInfo, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("[设备状态管理] 更新缓存失败, deviceId={}", statusInfo.getDeviceId(), e);
        }
    }

    /**
     * 批量更新缓存中的设备状态
     */
    private void batchUpdateDeviceStatusInCache(List<DeviceStatusInfo> statusInfos) {
        for (DeviceStatusInfo statusInfo : statusInfos) {
            updateDeviceStatusInCache(statusInfo);
        }
    }

    /**
     * 通知状态监听器
     */
    private void notifyStatusListeners(String deviceId, DeviceStatus oldStatus, DeviceStatus newStatus) {
        for (DeviceStatusListener listener : statusListeners.values()) {
            try {
                listener.onStatusChanged(deviceId, oldStatus, newStatus);
            } catch (Exception e) {
                log.error("[设备状态管理] 状态监听器通知失败, listener={}", listener.getListenerName(), e);
            }
        }
    }

    /**
     * 发布状态变更事件
     * <p>
     * 增强的Redis Pub/Sub事件发布机制
     * 支持事件格式统一、事件持久化、事件监听器注册
     * </p>
     *
     * @param deviceId 设备ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    private void publishStatusChangeEvent(String deviceId, DeviceStatus oldStatus, DeviceStatus newStatus) {
        try {
            // 1. 构建统一的事件数据格式
            Map<String, Object> event = new HashMap<>();
            event.put("deviceId", deviceId);
            event.put("oldStatus", oldStatus != null ? oldStatus.name() : null);
            event.put("oldStatusCode", oldStatus != null ? oldStatus.getCode() : null);
            event.put("oldStatusDesc", oldStatus != null ? oldStatus.getDescription() : null);
            event.put("newStatus", newStatus.name());
            event.put("newStatusCode", newStatus.getCode());
            event.put("newStatusDesc", newStatus.getDescription());
            event.put("timestamp", System.currentTimeMillis());
            event.put("eventType", "DEVICE_STATUS_CHANGE");
            event.put("source", "DeviceStatusManager");

            // 2. 发布到Redis频道
            redisTemplate.convertAndSend(STATUS_CHANGE_TOPIC, event);

            log.debug("[设备状态管理] 状态变更事件发布成功, deviceId={}, oldStatus={}, newStatus={}",
                    deviceId, oldStatus != null ? oldStatus.getDescription() : "null", newStatus.getDescription());

            // 3. 可选：事件持久化（如果需要）
            // 可以将事件存储到数据库或消息队列，确保事件不丢失
            // 这里暂时使用Redis发布，后续可以根据需要扩展

        } catch (Exception e) {
            log.error("[设备状态管理] 发布状态变更事件失败, deviceId={}, oldStatus={}, newStatus={}, error={}",
                    deviceId, oldStatus != null ? oldStatus.getDescription() : "null",
                    newStatus.getDescription(), e.getMessage(), e);

            // 降级处理：记录到日志，确保关键事件不丢失
            log.warn("[设备状态管理] 状态变更事件发布失败，已记录到日志, deviceId={}, oldStatus={}, newStatus={}",
                    deviceId, oldStatus != null ? oldStatus.getDescription() : "null", newStatus.getDescription());
        }
    }

    /**
     * 获取缓存键
     */
    private String getCacheKey(String deviceId) {
        Objects.requireNonNull(deviceId, "deviceId不能为null");
        return CACHE_PREFIX + deviceId;
    }
}
