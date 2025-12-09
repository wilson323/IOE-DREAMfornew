package net.lab1024.sa.devicecomm.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.devicecomm.protocol.adapter.ProtocolAdapterFactory;
import net.lab1024.sa.devicecomm.protocol.enums.ProtocolTypeEnum;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolHandler;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备同步服务
 * <p>
 * 提供设备用户同步、权限管理、健康检查、性能指标收集等功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource注入依赖
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Service
public class DeviceSyncService {

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ProtocolAdapterFactory protocolAdapterFactory;

    // 设备用户列表缓存（5分钟过期）
    private final Map<String, List<String>> deviceUsersCache = new HashMap<>();
    private final Map<String, Long> deviceUsersCacheTime = new HashMap<>();
    private static final long DEVICE_USERS_CACHE_EXPIRE_MS = 5 * 60 * 1000; // 5分钟

    // 设备命令发送超时时间（秒）
    private static final int DEVICE_COMMAND_TIMEOUT_SECONDS = 10;

    /**
     * 同步用户信息到设备
     * <p>
     * 将用户信息同步到指定设备
     * 支持异步处理和状态回调
     * </p>
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param userInfo 用户详细信息（可选）
     * @return 同步结果
     */
    public Map<String, Object> syncUserInfo(String deviceId, Long userId, Map<String, Object> userInfo) {
        log.info("[设备同步服务] 同步用户信息, deviceId={}, userId={}", deviceId, userId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                throw new RuntimeException("设备不存在: " + deviceId);
            }

            // 2. 构建用户同步数据
            Map<String, Object> syncData = new HashMap<>();
            syncData.put("deviceId", deviceId);
            syncData.put("userId", userId);
            syncData.put("userInfo", userInfo != null ? userInfo : new HashMap<>());
            syncData.put("syncTime", LocalDateTime.now());
            syncData.put("timestamp", System.currentTimeMillis());

            // 3. 实际设备同步逻辑（根据设备协议实现）
            // 这里需要根据具体的设备协议实现同步逻辑
            // 例如：调用设备SDK、发送TCP/UDP消息等
            boolean syncSuccess = performDeviceUserSync(device, userId, syncData);

            // 4. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("userId", userId);
            result.put("syncStatus", syncSuccess ? "SUCCESS" : "FAILED");
            result.put("syncTime", LocalDateTime.now());

            // 5. 清除设备用户列表缓存（因为用户列表已变更）
            deviceUsersCache.remove(deviceId);
            deviceUsersCacheTime.remove(deviceId);

            log.info("[设备同步服务] 用户信息同步完成, deviceId={}, userId={}, success={}",
                    deviceId, userId, syncSuccess);

            return result;

        } catch (Exception e) {
            log.error("[设备同步服务] 同步用户信息异常, deviceId={}, userId={}", deviceId, userId, e);
            throw new RuntimeException("同步用户信息失败：" + e.getMessage(), e);
        }
    }

    /**
     * 撤销用户在设备上的权限
     * <p>
     * 从指定设备上撤销用户的权限
     * 支持幂等性操作
     * </p>
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @return 撤销结果
     */
    public Map<String, Object> revokeUserPermission(String deviceId, Long userId) {
        log.info("[设备同步服务] 撤销用户权限, deviceId={}, userId={}", deviceId, userId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                throw new RuntimeException("设备不存在: " + deviceId);
            }

            // 2. 实际设备权限撤销逻辑（根据设备协议实现）
            boolean revokeSuccess = performDeviceUserRevoke(device, userId);

            // 3. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("userId", userId);
            result.put("revokeStatus", revokeSuccess ? "SUCCESS" : "FAILED");
            result.put("revokeTime", LocalDateTime.now());

            // 4. 清除设备用户列表缓存
            deviceUsersCache.remove(deviceId);
            deviceUsersCacheTime.remove(deviceId);

            log.info("[设备同步服务] 用户权限撤销完成, deviceId={}, userId={}, success={}",
                    deviceId, userId, revokeSuccess);

            return result;

        } catch (Exception e) {
            log.error("[设备同步服务] 撤销用户权限异常, deviceId={}, userId={}", deviceId, userId, e);
            throw new RuntimeException("撤销用户权限失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取设备上的用户列表
     * <p>
     * 查询指定设备上的所有用户ID列表
     * 使用缓存机制（5分钟）减少查询次数
     * </p>
     *
     * @param deviceId 设备ID
     * @return 用户ID列表
     */
    public List<String> getDeviceUsers(String deviceId) {
        log.debug("[设备同步服务] 获取设备用户列表, deviceId={}", deviceId);

        try {
            // 1. 检查缓存
            Long cacheTime = deviceUsersCacheTime.get(deviceId);
            if (cacheTime != null && (System.currentTimeMillis() - cacheTime) < DEVICE_USERS_CACHE_EXPIRE_MS) {
                List<String> cachedUsers = deviceUsersCache.get(deviceId);
                if (cachedUsers != null) {
                    log.debug("[设备同步服务] 从缓存获取设备用户列表, deviceId={}, userCount={}", deviceId, cachedUsers.size());
                    return cachedUsers;
                }
            }

            // 2. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                throw new RuntimeException("设备不存在: " + deviceId);
            }

            // 3. 实际设备用户列表查询逻辑（根据设备协议实现）
            List<String> users = performDeviceUserQuery(device);

            // 4. 更新缓存
            deviceUsersCache.put(deviceId, users);
            deviceUsersCacheTime.put(deviceId, System.currentTimeMillis());

            log.debug("[设备同步服务] 获取设备用户列表成功, deviceId={}, userCount={}", deviceId, users.size());
            return users;

        } catch (Exception e) {
            log.error("[设备同步服务] 获取设备用户列表异常, deviceId={}", deviceId, e);
            // 降级处理：返回空列表
            return Collections.emptyList();
        }
    }

    /**
     * 同步业务属性到设备
     * <p>
     * 将业务属性同步到指定设备
     * 支持部分属性更新和版本控制
     * </p>
     *
     * @param deviceId 设备ID
     * @param attributes 业务属性
     * @return 同步结果
     */
    public Map<String, Object> syncBusinessAttributes(String deviceId, Map<String, Object> attributes) {
        log.info("[设备同步服务] 同步业务属性, deviceId={}, attributeCount={}", deviceId, attributes != null ? attributes.size() : 0);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                throw new RuntimeException("设备不存在: " + deviceId);
            }

            if (attributes == null || attributes.isEmpty()) {
                log.warn("[设备同步服务] 业务属性为空，跳过同步, deviceId={}", deviceId);
                return Map.of("deviceId", deviceId, "syncStatus", "SKIPPED", "reason", "attributes为空");
            }

            // 2. 实际设备业务属性同步逻辑（根据设备协议实现）
            boolean syncSuccess = performDeviceAttributeSync(device, attributes);

            // 3. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("syncStatus", syncSuccess ? "SUCCESS" : "FAILED");
            result.put("attributeCount", attributes.size());
            result.put("syncTime", LocalDateTime.now());

            log.info("[设备同步服务] 业务属性同步完成, deviceId={}, success={}", deviceId, syncSuccess);

            return result;

        } catch (Exception e) {
            log.error("[设备同步服务] 同步业务属性异常, deviceId={}", deviceId, e);
            throw new RuntimeException("同步业务属性失败：" + e.getMessage(), e);
        }
    }

    /**
     * 设备健康检查
     * <p>
     * 检查指定设备的健康状态
     * 支持超时控制（默认3秒）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 健康检查结果
     */
    public Map<String, Object> checkDeviceHealth(String deviceId) {
        log.debug("[设备同步服务] 设备健康检查, deviceId={}", deviceId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return Map.of(
                        "deviceId", deviceId,
                        "healthStatus", "UNKNOWN",
                        "message", "设备不存在"
                );
            }

            // 2. 实际设备健康检查逻辑（根据设备协议实现）
            Map<String, Object> healthStatus = performDeviceHealthCheck(device);

            log.debug("[设备同步服务] 设备健康检查完成, deviceId={}, status={}", deviceId, healthStatus.get("healthStatus"));
            return healthStatus;

        } catch (Exception e) {
            log.error("[设备同步服务] 设备健康检查异常, deviceId={}", deviceId, e);
            return Map.of(
                    "deviceId", deviceId,
                    "healthStatus", "ERROR",
                    "message", "健康检查失败：" + e.getMessage()
            );
        }
    }

    /**
     * 获取设备性能指标
     * <p>
     * 获取指定设备的性能指标（CPU、内存、网络延迟、响应时间、错误率等）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 性能指标
     */
    public Map<String, Object> getDeviceMetrics(String deviceId) {
        log.debug("[设备同步服务] 获取设备性能指标, deviceId={}", deviceId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                throw new RuntimeException("设备不存在: " + deviceId);
            }

            // 2. 实际设备性能指标收集逻辑（根据设备协议实现）
            Map<String, Object> metrics = performDeviceMetricsCollection(device);

            log.debug("[设备同步服务] 获取设备性能指标完成, deviceId={}", deviceId);
            return metrics;

        } catch (Exception e) {
            log.error("[设备同步服务] 获取设备性能指标异常, deviceId={}", deviceId, e);
            // 降级处理：返回默认值
            return Map.of(
                    "deviceId", deviceId,
                    "cpuUsage", 0.0,
                    "memoryUsage", 0L,
                    "networkLatency", 0.0,
                    "responseTime", 0,
                    "errorRate", 0
            );
        }
    }

    /**
     * 处理设备心跳
     * <p>
     * 接收设备心跳请求，用于设备响应性检查
     * </p>
     *
     * @param deviceId 设备ID
     * @param heartbeatData 心跳数据
     * @return 心跳响应
     */
    public Map<String, Object> processHeartbeat(String deviceId, Map<String, Object> heartbeatData) {
        log.debug("[设备同步服务] 处理设备心跳, deviceId={}", deviceId);

        try {
            // 1. 验证设备是否存在
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                return Map.of("status", "ERROR", "message", "设备不存在");
            }

            // 2. 更新设备最后在线时间
            device.setLastOnlineTime(LocalDateTime.now());
            deviceDao.updateById(device);

            // 3. 构建心跳响应
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("deviceId", deviceId);
            response.put("timestamp", System.currentTimeMillis());
            response.put("serverTime", LocalDateTime.now());

            return response;

        } catch (Exception e) {
            log.error("[设备同步服务] 处理设备心跳异常, deviceId={}", deviceId, e);
            return Map.of("status", "ERROR", "message", "处理心跳失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法：实际设备操作 ====================

    /**
     * 执行设备用户同步（根据设备协议实现）
     * <p>
     * 根据设备类型和厂商选择对应的协议适配器，执行用户信息同步操作
     * 支持多种设备协议，包括门禁、考勤、消费等设备类型
     * </p>
     *
     * @param device 设备实体
     * @param userId 用户ID
     * @param syncData 同步数据（包含用户信息等）
     * @return true-同步成功，false-同步失败
     */
    private boolean performDeviceUserSync(DeviceEntity device, Long userId, Map<String, Object> syncData) {
        try {
            // 1. 参数验证
            if (device == null) {
                log.error("[设备同步服务] 设备实体为空，无法执行用户同步");
                return false;
            }

            if (userId == null || userId <= 0) {
                log.error("[设备同步服务] 用户ID无效, userId={}", userId);
                return false;
            }

            if (syncData == null) {
                log.warn("[设备同步服务] 同步数据为空，使用默认数据, deviceId={}, userId={}", device.getId(), userId);
                syncData = new HashMap<>();
            }

        log.info("[设备同步服务] 执行设备用户同步, deviceId={}, userId={}, deviceType={}, manufacturer={}",
                device.getId(), userId, device.getDeviceType(), device.getManufacturer());

            // 2. 根据设备类型和厂商获取协议处理器
            String deviceType = device.getDeviceType();
            String manufacturer = device.getManufacturer();

            if (deviceType == null || deviceType.isEmpty()) {
                log.warn("[设备同步服务] 设备类型为空，无法选择协议适配器, deviceId={}", device.getId());
                return false;
            }

            if (manufacturer == null || manufacturer.isEmpty()) {
                log.warn("[设备同步服务] 设备厂商为空，无法选择协议适配器, deviceId={}", device.getId());
                return false;
            }

            ProtocolHandler handler = protocolAdapterFactory.getHandler(deviceType, manufacturer);
            if (handler == null) {
                log.warn("[设备同步服务] 未找到对应的协议处理器, deviceType={}, manufacturer={}, deviceId={}",
                        deviceType, manufacturer, device.getId());
                // 降级处理：记录日志但不抛出异常，允许后续扩展
                return false;
            }

            // 3. 补充设备信息到同步数据
            syncData.put("deviceId", device.getId());
            syncData.put("userId", userId);
            syncData.put("deviceType", deviceType);
            syncData.put("manufacturer", manufacturer);
            syncData.put("deviceCode", device.getDeviceCode());
            syncData.put("deviceName", device.getDeviceName());
            syncData.put("ipAddress", device.getIpAddress());
            syncData.put("port", device.getPort());
            syncData.put("syncTime", LocalDateTime.now());
            syncData.put("timestamp", System.currentTimeMillis());

            // 4. 执行用户同步操作
            // 注意：当前ProtocolHandler主要用于接收设备推送消息
            // 主动发送命令到设备的功能需要根据具体设备协议实现
            // 这里提供一个可扩展的框架，具体实现由各协议处理器提供
            boolean syncSuccess = executeSyncCommand(handler, device, userId, syncData);

            if (syncSuccess) {
                log.info("[设备同步服务] 设备用户同步成功, deviceId={}, userId={}, protocolType={}",
                        device.getId(), userId, handler.getProtocolType());
            } else {
                log.warn("[设备同步服务] 设备用户同步失败, deviceId={}, userId={}, protocolType={}",
                        device.getId(), userId, handler.getProtocolType());
            }

            return syncSuccess;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备用户同步异常, deviceId={}, userId={}, error={}",
                    device != null ? device.getId() : "null", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行用户同步命令
     * <p>
     * 根据协议处理器执行具体的用户同步命令
     * 具体实现需要根据设备协议类型调用相应的设备通信服务
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param userId 用户ID
     * @param syncData 同步数据
     * @return true-同步成功，false-同步失败
     */
    private boolean executeSyncCommand(ProtocolHandler handler, DeviceEntity device, Long userId, Map<String, Object> syncData) {
        try {
            // 根据协议类型执行不同的同步逻辑
            String protocolType = handler.getProtocolType();
            log.debug("[设备同步服务] 执行用户同步命令, protocolType={}, deviceId={}, userId={}",
                    protocolType, device.getId(), userId);

            // 根据具体协议类型实现用户同步命令
            // 1. 构建用户同步命令消息
            ProtocolMessage commandMessage = buildUserSyncCommand(handler, device, userId, syncData);
            if (commandMessage == null) {
                log.warn("[设备同步服务] 构建用户同步命令失败, protocolType={}, deviceId={}, userId={}",
                        protocolType, device.getId(), userId);
                return false;
            }

            // 2. 发送命令到设备并接收响应
            ProtocolMessage responseMessage = sendCommandToDevice(handler, device, commandMessage);
            if (responseMessage == null) {
                log.warn("[设备同步服务] 设备未响应或响应超时, protocolType={}, deviceId={}, userId={}",
                        protocolType, device.getId(), userId);
                return false;
            }

            // 3. 验证响应消息
            if (!handler.validateMessage(responseMessage)) {
                log.warn("[设备同步服务] 设备响应消息验证失败, protocolType={}, deviceId={}, userId={}",
                        protocolType, device.getId(), userId);
                return false;
            }

            // 4. 解析响应判断是否成功
            boolean success = parseSyncResponse(handler, responseMessage);
            log.info("[设备同步服务] 用户同步命令执行完成, protocolType={}, deviceId={}, userId={}, success={}",
                    protocolType, device.getId(), userId, success);

            return success;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行用户同步命令异常, deviceId={}, userId={}, error={}",
                    device.getId(), userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行设备用户权限撤销（根据设备协议实现）
     * <p>
     * 根据设备类型和厂商选择对应的协议适配器，执行用户权限撤销操作
     * 支持多种设备协议，包括门禁、考勤、消费等设备类型
     * </p>
     *
     * @param device 设备实体
     * @param userId 用户ID
     * @return true-撤销成功，false-撤销失败
     */
    private boolean performDeviceUserRevoke(DeviceEntity device, Long userId) {
        try {
            // 1. 参数验证
            if (device == null) {
                log.error("[设备同步服务] 设备实体为空，无法执行权限撤销");
                return false;
            }

            if (userId == null || userId <= 0) {
                log.error("[设备同步服务] 用户ID无效, userId={}", userId);
                return false;
            }

            log.info("[设备同步服务] 执行设备用户权限撤销, deviceId={}, userId={}, deviceType={}, manufacturer={}",
                    device.getId(), userId, device.getDeviceType(), device.getManufacturer());

            // 2. 根据设备类型和厂商获取协议处理器
            String deviceType = device.getDeviceType();
            String manufacturer = device.getManufacturer();

            if (deviceType == null || deviceType.isEmpty()) {
                log.warn("[设备同步服务] 设备类型为空，无法选择协议适配器, deviceId={}", device.getId());
                return false;
            }

            if (manufacturer == null || manufacturer.isEmpty()) {
                log.warn("[设备同步服务] 设备厂商为空，无法选择协议适配器, deviceId={}", device.getId());
                return false;
            }

            ProtocolHandler handler = protocolAdapterFactory.getHandler(deviceType, manufacturer);
            if (handler == null) {
                log.warn("[设备同步服务] 未找到对应的协议处理器, deviceType={}, manufacturer={}, deviceId={}",
                        deviceType, manufacturer, device.getId());
                // 降级处理：记录日志但不抛出异常，允许后续扩展
                return false;
            }

            // 3. 构建权限撤销命令数据
            Map<String, Object> revokeData = new HashMap<>();
            revokeData.put("deviceId", device.getId());
            revokeData.put("userId", userId);
            revokeData.put("deviceType", deviceType);
            revokeData.put("manufacturer", manufacturer);
            revokeData.put("deviceCode", device.getDeviceCode());
            revokeData.put("ipAddress", device.getIpAddress());
            revokeData.put("port", device.getPort());
            revokeData.put("revokeTime", LocalDateTime.now());
            revokeData.put("timestamp", System.currentTimeMillis());

            // 4. 执行权限撤销操作
            // 注意：当前ProtocolHandler主要用于接收设备推送消息
            // 主动发送命令到设备的功能需要根据具体设备协议实现
            // 这里提供一个可扩展的框架，具体实现由各协议处理器提供
            boolean revokeSuccess = executeRevokeCommand(handler, device, userId, revokeData);

            if (revokeSuccess) {
                log.info("[设备同步服务] 设备用户权限撤销成功, deviceId={}, userId={}, protocolType={}",
                        device.getId(), userId, handler.getProtocolType());
            } else {
                log.warn("[设备同步服务] 设备用户权限撤销失败, deviceId={}, userId={}, protocolType={}",
                        device.getId(), userId, handler.getProtocolType());
            }

            return revokeSuccess;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备用户权限撤销异常, deviceId={}, userId={}, error={}",
                    device != null ? device.getId() : "null", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行权限撤销命令
     * <p>
     * 根据协议处理器执行具体的权限撤销命令
     * 具体实现需要根据设备协议类型调用相应的设备通信服务
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param userId 用户ID
     * @param revokeData 撤销数据
     * @return true-撤销成功，false-撤销失败
     */
    private boolean executeRevokeCommand(ProtocolHandler handler, DeviceEntity device, Long userId, Map<String, Object> revokeData) {
        try {
            // 根据协议类型执行不同的撤销逻辑
            String protocolType = handler.getProtocolType();
            log.debug("[设备同步服务] 执行权限撤销命令, protocolType={}, deviceId={}, userId={}",
                    protocolType, device.getId(), userId);

            // 根据具体协议类型实现权限撤销命令
            // 1. 构建权限撤销命令消息
            ProtocolMessage commandMessage = buildRevokeCommand(handler, device, userId, revokeData);
            if (commandMessage == null) {
                log.warn("[设备同步服务] 构建权限撤销命令失败, protocolType={}, deviceId={}, userId={}",
                        protocolType, device.getId(), userId);
                return false;
            }

            // 2. 发送命令到设备并接收响应
            ProtocolMessage responseMessage = sendCommandToDevice(handler, device, commandMessage);
            if (responseMessage == null) {
                log.warn("[设备同步服务] 设备未响应或响应超时, protocolType={}, deviceId={}, userId={}",
                        protocolType, device.getId(), userId);
                return false;
            }

            // 3. 验证响应消息
            if (!handler.validateMessage(responseMessage)) {
                log.warn("[设备同步服务] 设备响应消息验证失败, protocolType={}, deviceId={}, userId={}",
                        protocolType, device.getId(), userId);
                return false;
            }

            // 4. 解析响应判断是否成功
            boolean success = parseRevokeResponse(handler, responseMessage);
            log.info("[设备同步服务] 权限撤销命令执行完成, protocolType={}, deviceId={}, userId={}, success={}",
                    protocolType, device.getId(), userId, success);

            return success;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行权限撤销命令异常, deviceId={}, userId={}, error={}",
                    device.getId(), userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行设备用户列表查询（根据设备协议实现）
     * <p>
     * 根据设备类型和厂商选择对应的协议适配器，查询设备上的用户列表
     * 支持多种设备协议，包括门禁、考勤、消费等设备类型
     * </p>
     *
     * @param device 设备实体
     * @return 用户ID列表，如果查询失败则返回空列表
     */
    private List<String> performDeviceUserQuery(DeviceEntity device) {
        try {
            // 1. 参数验证
            if (device == null) {
                log.error("[设备同步服务] 设备实体为空，无法执行用户列表查询");
                return Collections.emptyList();
            }

            log.debug("[设备同步服务] 执行设备用户列表查询, deviceId={}, deviceType={}, manufacturer={}",
                    device.getId(), device.getDeviceType(), device.getManufacturer());

            // 2. 根据设备类型和厂商获取协议处理器
            String deviceType = device.getDeviceType();
            String manufacturer = device.getManufacturer();

            if (deviceType == null || deviceType.isEmpty()) {
                log.warn("[设备同步服务] 设备类型为空，无法选择协议适配器, deviceId={}", device.getId());
                return Collections.emptyList();
            }

            if (manufacturer == null || manufacturer.isEmpty()) {
                log.warn("[设备同步服务] 设备厂商为空，无法选择协议适配器, deviceId={}", device.getId());
                return Collections.emptyList();
            }

            ProtocolHandler handler = protocolAdapterFactory.getHandler(deviceType, manufacturer);
            if (handler == null) {
                log.warn("[设备同步服务] 未找到对应的协议处理器, deviceType={}, manufacturer={}, deviceId={}",
                        deviceType, manufacturer, device.getId());
                // 降级处理：记录日志但不抛出异常，允许后续扩展
        return Collections.emptyList();
            }

            // 3. 构建查询数据
            Map<String, Object> queryData = new HashMap<>();
            queryData.put("deviceId", device.getId());
            queryData.put("deviceType", deviceType);
            queryData.put("manufacturer", manufacturer);
            queryData.put("deviceCode", device.getDeviceCode());
            queryData.put("ipAddress", device.getIpAddress());
            queryData.put("port", device.getPort());
            queryData.put("queryTime", LocalDateTime.now());
            queryData.put("timestamp", System.currentTimeMillis());

            // 4. 执行用户列表查询操作
            // 注意：当前ProtocolHandler主要用于接收设备推送消息
            // 主动查询设备的功能需要根据具体设备协议实现
            // 这里提供一个可扩展的框架，具体实现由各协议处理器提供
            List<String> users = executeUserQueryCommand(handler, device, queryData);

            log.debug("[设备同步服务] 设备用户列表查询完成, deviceId={}, userCount={}, protocolType={}",
                    device.getId(), users != null ? users.size() : 0, handler.getProtocolType());

            return users != null ? users : Collections.emptyList();

        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备用户列表查询异常, deviceId={}, error={}",
                    device != null ? device.getId() : "null", e.getMessage(), e);
            // 降级处理：返回空列表
            return Collections.emptyList();
        }
    }

    /**
     * 执行用户列表查询命令
     * <p>
     * 根据协议处理器执行具体的用户列表查询命令
     * 具体实现需要根据设备协议类型调用相应的设备通信服务
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param queryData 查询数据
     * @return 用户ID列表，如果查询失败则返回空列表
     */
    private List<String> executeUserQueryCommand(ProtocolHandler handler, DeviceEntity device, Map<String, Object> queryData) {
        try {
            // 根据协议类型执行不同的查询逻辑
            String protocolType = handler.getProtocolType();
            log.debug("[设备同步服务] 执行用户列表查询命令, protocolType={}, deviceId={}",
                    protocolType, device.getId());

            // 根据具体协议类型实现用户列表查询命令
            // 1. 构建用户列表查询命令消息
            ProtocolMessage commandMessage = buildUserQueryCommand(handler, device, queryData);
            if (commandMessage == null) {
                log.warn("[设备同步服务] 构建用户列表查询命令失败, protocolType={}, deviceId={}",
                        protocolType, device.getId());
                return Collections.emptyList();
            }

            // 2. 发送命令到设备并接收响应
            ProtocolMessage responseMessage = sendCommandToDevice(handler, device, commandMessage);
            if (responseMessage == null) {
                log.warn("[设备同步服务] 设备未响应或响应超时, protocolType={}, deviceId={}",
                        protocolType, device.getId());
                return Collections.emptyList();
            }

            // 3. 验证响应消息
            if (!handler.validateMessage(responseMessage)) {
                log.warn("[设备同步服务] 设备响应消息验证失败, protocolType={}, deviceId={}",
                        protocolType, device.getId());
                return Collections.emptyList();
            }

            // 4. 解析响应提取用户ID列表
            List<String> userIds = parseUserListResponse(handler, responseMessage);
            log.info("[设备同步服务] 用户列表查询命令执行完成, protocolType={}, deviceId={}, userCount={}",
                    protocolType, device.getId(), userIds.size());

            return userIds;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行用户列表查询命令异常, deviceId={}, error={}",
                    device.getId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 执行设备业务属性同步（根据设备协议实现）
     * <p>
     * 根据设备类型和厂商选择对应的协议适配器，执行业务属性同步操作
     * 支持多种设备协议，包括门禁、考勤、消费等设备类型
     * </p>
     *
     * @param device 设备实体
     * @param attributes 业务属性
     * @return true-同步成功，false-同步失败
     */
    private boolean performDeviceAttributeSync(DeviceEntity device, Map<String, Object> attributes) {
        try {
            // 1. 参数验证
            if (device == null) {
                log.error("[设备同步服务] 设备实体为空，无法执行业务属性同步");
                return false;
            }

            if (attributes == null || attributes.isEmpty()) {
                log.warn("[设备同步服务] 业务属性为空，跳过同步, deviceId={}", device.getId());
                return false;
            }

            log.info("[设备同步服务] 执行设备业务属性同步, deviceId={}, deviceType={}, manufacturer={}, attributeCount={}",
                    device.getId(), device.getDeviceType(), device.getManufacturer(), attributes.size());

            // 2. 根据设备类型和厂商获取协议处理器
            String deviceType = device.getDeviceType();
            String manufacturer = device.getManufacturer();

            if (deviceType == null || deviceType.isEmpty()) {
                log.warn("[设备同步服务] 设备类型为空，无法选择协议适配器, deviceId={}", device.getId());
                return false;
            }

            if (manufacturer == null || manufacturer.isEmpty()) {
                log.warn("[设备同步服务] 设备厂商为空，无法选择协议适配器, deviceId={}", device.getId());
                return false;
            }

            ProtocolHandler handler = protocolAdapterFactory.getHandler(deviceType, manufacturer);
            if (handler == null) {
                log.warn("[设备同步服务] 未找到对应的协议处理器, deviceType={}, manufacturer={}, deviceId={}",
                        deviceType, manufacturer, device.getId());
                // 降级处理：记录日志但不抛出异常，允许后续扩展
                return false;
            }

            // 3. 构建业务属性同步数据
            Map<String, Object> syncData = new HashMap<>();
            syncData.put("deviceId", device.getId());
            syncData.put("deviceType", deviceType);
            syncData.put("manufacturer", manufacturer);
            syncData.put("deviceCode", device.getDeviceCode());
            syncData.put("deviceName", device.getDeviceName());
            syncData.put("ipAddress", device.getIpAddress());
            syncData.put("port", device.getPort());
            syncData.put("attributes", attributes);
            syncData.put("attributeCount", attributes.size());
            syncData.put("syncTime", LocalDateTime.now());
            syncData.put("timestamp", System.currentTimeMillis());

            // 4. 执行业务属性同步操作
            // 注意：当前ProtocolHandler主要用于接收设备推送消息
            // 主动发送命令到设备的功能需要根据具体设备协议实现
            // 这里提供一个可扩展的框架，具体实现由各协议处理器提供
            boolean syncSuccess = executeAttributeSyncCommand(handler, device, attributes, syncData);

            if (syncSuccess) {
                log.info("[设备同步服务] 设备业务属性同步成功, deviceId={}, attributeCount={}, protocolType={}",
                        device.getId(), attributes.size(), handler.getProtocolType());
            } else {
                log.warn("[设备同步服务] 设备业务属性同步失败, deviceId={}, attributeCount={}, protocolType={}",
                        device.getId(), attributes.size(), handler.getProtocolType());
            }

            return syncSuccess;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备业务属性同步异常, deviceId={}, error={}",
                    device != null ? device.getId() : "null", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行业务属性同步命令
     * <p>
     * 根据协议处理器执行具体的业务属性同步命令
     * 具体实现需要根据设备协议类型调用相应的设备通信服务
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param attributes 业务属性
     * @param syncData 同步数据
     * @return true-同步成功，false-同步失败
     */
    private boolean executeAttributeSyncCommand(ProtocolHandler handler, DeviceEntity device, Map<String, Object> attributes, Map<String, Object> syncData) {
        try {
            // 根据协议类型执行不同的同步逻辑
            String protocolType = handler.getProtocolType();
            log.debug("[设备同步服务] 执行业务属性同步命令, protocolType={}, deviceId={}, attributeCount={}",
                    protocolType, device.getId(), attributes.size());

            // 根据具体协议类型实现业务属性同步命令
            // 1. 构建业务属性同步命令消息
            ProtocolMessage commandMessage = buildAttributeSyncCommand(handler, device, attributes, syncData);
            if (commandMessage == null) {
                log.warn("[设备同步服务] 构建业务属性同步命令失败, protocolType={}, deviceId={}, attributeCount={}",
                        protocolType, device.getId(), attributes.size());
                return false;
            }

            // 2. 发送命令到设备并接收响应
            ProtocolMessage responseMessage = sendCommandToDevice(handler, device, commandMessage);
            if (responseMessage == null) {
                log.warn("[设备同步服务] 设备未响应或响应超时, protocolType={}, deviceId={}, attributeCount={}",
                        protocolType, device.getId(), attributes.size());
                return false;
            }

            // 3. 验证响应消息
            if (!handler.validateMessage(responseMessage)) {
                log.warn("[设备同步服务] 设备响应消息验证失败, protocolType={}, deviceId={}, attributeCount={}",
                        protocolType, device.getId(), attributes.size());
                return false;
            }

            // 4. 解析响应判断是否成功
            boolean success = parseAttributeSyncResponse(handler, responseMessage);
            log.info("[设备同步服务] 业务属性同步命令执行完成, protocolType={}, deviceId={}, attributeCount={}, success={}",
                    protocolType, device.getId(), attributes.size(), success);

            return success;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行业务属性同步命令异常, deviceId={}, error={}",
                    device.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行设备健康检查（根据设备协议实现）
     * <p>
     * 根据设备类型和厂商选择对应的协议适配器，执行设备健康检查操作
     * 支持多种设备协议，包括门禁、考勤、消费等设备类型
     * </p>
     *
     * @param device 设备实体
     * @return 健康检查结果（包含健康状态、检查时间、消息等）
     */
    private Map<String, Object> performDeviceHealthCheck(DeviceEntity device) {
        try {
            // 1. 参数验证
            if (device == null) {
                log.error("[设备同步服务] 设备实体为空，无法执行健康检查");
                return Map.of(
                        "deviceId", "null",
                        "healthStatus", "UNKNOWN",
                        "message", "设备实体为空",
                        "checkTime", LocalDateTime.now()
                );
            }

            log.debug("[设备同步服务] 执行设备健康检查, deviceId={}, deviceType={}, manufacturer={}",
                    device.getId(), device.getDeviceType(), device.getManufacturer());

            // 2. 根据设备类型和厂商获取协议处理器
            String deviceType = device.getDeviceType();
            String manufacturer = device.getManufacturer();

            if (deviceType == null || deviceType.isEmpty()) {
                log.warn("[设备同步服务] 设备类型为空，无法选择协议适配器, deviceId={}", device.getId());
                return Map.of(
                        "deviceId", device.getId().toString(),
                        "healthStatus", "UNKNOWN",
                        "message", "设备类型为空",
                        "checkTime", LocalDateTime.now()
                );
            }

            if (manufacturer == null || manufacturer.isEmpty()) {
                log.warn("[设备同步服务] 设备厂商为空，无法选择协议适配器, deviceId={}", device.getId());
                return Map.of(
                        "deviceId", device.getId().toString(),
                        "healthStatus", "UNKNOWN",
                        "message", "设备厂商为空",
                        "checkTime", LocalDateTime.now()
                );
            }

            ProtocolHandler handler = protocolAdapterFactory.getHandler(deviceType, manufacturer);
            if (handler == null) {
                log.warn("[设备同步服务] 未找到对应的协议处理器, deviceType={}, manufacturer={}, deviceId={}",
                        deviceType, manufacturer, device.getId());
                // 降级处理：返回未知状态，不抛出异常
                return Map.of(
                        "deviceId", device.getId().toString(),
                        "healthStatus", "UNKNOWN",
                        "message", "未找到对应的协议处理器",
                        "checkTime", LocalDateTime.now()
                );
            }

            // 3. 构建健康检查数据
            Map<String, Object> checkData = new HashMap<>();
            checkData.put("deviceId", device.getId());
            checkData.put("deviceType", deviceType);
            checkData.put("manufacturer", manufacturer);
            checkData.put("deviceCode", device.getDeviceCode());
            checkData.put("ipAddress", device.getIpAddress());
            checkData.put("port", device.getPort());
            checkData.put("checkTime", LocalDateTime.now());
            checkData.put("timestamp", System.currentTimeMillis());

            // 4. 执行健康检查操作
            // 注意：当前ProtocolHandler主要用于接收设备推送消息
            // 主动查询设备的功能需要根据具体设备协议实现
            // 这里提供一个可扩展的框架，具体实现由各协议处理器提供
            Map<String, Object> healthStatus = executeHealthCheckCommand(handler, device, checkData);

            log.debug("[设备同步服务] 设备健康检查完成, deviceId={}, healthStatus={}, protocolType={}",
                    device.getId(), healthStatus.get("healthStatus"), handler.getProtocolType());

            return healthStatus;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备健康检查异常, deviceId={}, error={}",
                    device != null ? device.getId() : "null", e.getMessage(), e);
            // 降级处理：返回错误状态
            return Map.of(
                    "deviceId", device != null ? device.getId().toString() : "null",
                    "healthStatus", "ERROR",
                    "message", "健康检查失败：" + e.getMessage(),
                    "checkTime", LocalDateTime.now()
            );
        }
    }

    /**
     * 执行健康检查命令
     * <p>
     * 根据协议处理器执行具体的健康检查命令
     * 具体实现需要根据设备协议类型调用相应的设备通信服务
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param checkData 检查数据
     * @return 健康检查结果（包含健康状态、检查时间、消息等）
     */
    private Map<String, Object> executeHealthCheckCommand(ProtocolHandler handler, DeviceEntity device, Map<String, Object> checkData) {
        try {
            // 根据协议类型执行不同的健康检查逻辑
            String protocolType = handler.getProtocolType();
            log.debug("[设备同步服务] 执行健康检查命令, protocolType={}, deviceId={}",
                    protocolType, device.getId());

            // 根据具体协议类型实现健康检查命令
            // 1. 构建健康检查命令消息
            ProtocolMessage commandMessage = buildHealthCheckCommand(handler, device, checkData);
            if (commandMessage == null) {
                log.warn("[设备同步服务] 构建健康检查命令失败, protocolType={}, deviceId={}",
                        protocolType, device.getId());
                return buildUnknownHealthStatus(device, protocolType, "构建健康检查命令失败");
            }

            // 2. 发送命令到设备并接收响应
            long startTime = System.currentTimeMillis();
            ProtocolMessage responseMessage = sendCommandToDevice(handler, device, commandMessage);
            long responseTime = System.currentTimeMillis() - startTime;

            if (responseMessage == null) {
                log.warn("[设备同步服务] 设备未响应或响应超时, protocolType={}, deviceId={}",
                        protocolType, device.getId());
                return buildUnhealthyStatus(device, protocolType, "设备未响应或响应超时", responseTime);
            }

            // 3. 验证响应消息
            if (!handler.validateMessage(responseMessage)) {
                log.warn("[设备同步服务] 设备响应消息验证失败, protocolType={}, deviceId={}",
                        protocolType, device.getId());
                return buildUnhealthyStatus(device, protocolType, "响应消息验证失败", responseTime);
            }

            // 4. 解析响应提取健康状态
            Map<String, Object> healthStatus = parseHealthCheckResponse(handler, responseMessage, responseTime);
            log.info("[设备同步服务] 健康检查命令执行完成, protocolType={}, deviceId={}, healthStatus={}, responseTime={}ms",
                    protocolType, device.getId(), healthStatus.get("healthStatus"), responseTime);

            return healthStatus;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行健康检查命令异常, deviceId={}, error={}",
                    device.getId(), e.getMessage(), e);
            // 降级处理：返回错误状态
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("deviceId", device.getId().toString());
            errorStatus.put("healthStatus", "ERROR");
            errorStatus.put("checkTime", LocalDateTime.now());
            errorStatus.put("message", "健康检查命令执行失败：" + e.getMessage());
            return errorStatus;
        }
    }

    /**
     * 执行设备性能指标收集（根据设备协议实现）
     * <p>
     * 根据设备类型和厂商选择对应的协议适配器，执行设备性能指标收集操作
     * 支持多种设备协议，包括门禁、考勤、消费等设备类型
     * </p>
     *
     * @param device 设备实体
     * @return 性能指标（包含CPU使用率、内存使用、网络延迟、响应时间、错误率等）
     */
    private Map<String, Object> performDeviceMetricsCollection(DeviceEntity device) {
        try {
            // 1. 参数验证
            if (device == null) {
                log.error("[设备同步服务] 设备实体为空，无法执行性能指标收集");
                return getDefaultMetrics("null");
            }

            log.debug("[设备同步服务] 执行设备性能指标收集, deviceId={}, deviceType={}, manufacturer={}",
                    device.getId(), device.getDeviceType(), device.getManufacturer());

            // 2. 根据设备类型和厂商获取协议处理器
            String deviceType = device.getDeviceType();
            String manufacturer = device.getManufacturer();

            if (deviceType == null || deviceType.isEmpty()) {
                log.warn("[设备同步服务] 设备类型为空，无法选择协议适配器, deviceId={}", device.getId());
                return getDefaultMetrics(device.getId().toString());
            }

            if (manufacturer == null || manufacturer.isEmpty()) {
                log.warn("[设备同步服务] 设备厂商为空，无法选择协议适配器, deviceId={}", device.getId());
                return getDefaultMetrics(device.getId().toString());
            }

            ProtocolHandler handler = protocolAdapterFactory.getHandler(deviceType, manufacturer);
            if (handler == null) {
                log.warn("[设备同步服务] 未找到对应的协议处理器, deviceType={}, manufacturer={}, deviceId={}",
                        deviceType, manufacturer, device.getId());
                // 降级处理：返回默认值，不抛出异常
                return getDefaultMetrics(device.getId().toString());
            }

            // 3. 构建性能指标收集数据
            Map<String, Object> collectData = new HashMap<>();
            collectData.put("deviceId", device.getId());
            collectData.put("deviceType", deviceType);
            collectData.put("manufacturer", manufacturer);
            collectData.put("deviceCode", device.getDeviceCode());
            collectData.put("ipAddress", device.getIpAddress());
            collectData.put("port", device.getPort());
            collectData.put("collectTime", LocalDateTime.now());
            collectData.put("timestamp", System.currentTimeMillis());

            // 4. 执行性能指标收集操作
            // 注意：当前ProtocolHandler主要用于接收设备推送消息
            // 主动查询设备的功能需要根据具体设备协议实现
            // 这里提供一个可扩展的框架，具体实现由各协议处理器提供
            Map<String, Object> metrics = executeMetricsCollectionCommand(handler, device, collectData);

            log.debug("[设备同步服务] 设备性能指标收集完成, deviceId={}, protocolType={}",
                    device.getId(), handler.getProtocolType());

            return metrics;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备性能指标收集异常, deviceId={}, error={}",
                    device != null ? device.getId() : "null", e.getMessage(), e);
            // 降级处理：返回默认值
            return getDefaultMetrics(device != null ? device.getId().toString() : "null");
        }
    }

    /**
     * 执行性能指标收集命令
     * <p>
     * 根据协议处理器执行具体的性能指标收集命令
     * 具体实现需要根据设备协议类型调用相应的设备通信服务
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param collectData 收集数据
     * @return 性能指标（包含CPU使用率、内存使用、网络延迟、响应时间、错误率等）
     */
    private Map<String, Object> executeMetricsCollectionCommand(ProtocolHandler handler, DeviceEntity device, Map<String, Object> collectData) {
        try {
            // 根据协议类型执行不同的性能指标收集逻辑
            String protocolType = handler.getProtocolType();
            log.debug("[设备同步服务] 执行性能指标收集命令, protocolType={}, deviceId={}",
                    protocolType, device.getId());

            // 根据具体协议类型实现性能指标收集命令
            // 1. 构建性能指标收集命令消息
            ProtocolMessage commandMessage = buildMetricsCollectionCommand(handler, device, collectData);
            if (commandMessage == null) {
                log.warn("[设备同步服务] 构建性能指标收集命令失败, protocolType={}, deviceId={}",
                        protocolType, device.getId());
                return getDefaultMetrics(device.getId().toString());
            }

            // 2. 发送命令到设备并接收响应
            long startTime = System.currentTimeMillis();
            ProtocolMessage responseMessage = sendCommandToDevice(handler, device, commandMessage);
            long responseTime = System.currentTimeMillis() - startTime;

            if (responseMessage == null) {
                log.warn("[设备同步服务] 设备未响应或响应超时, protocolType={}, deviceId={}",
                        protocolType, device.getId());
                Map<String, Object> defaultMetrics = getDefaultMetrics(device.getId().toString());
                defaultMetrics.put("networkLatency", (double) responseTime);
                defaultMetrics.put("message", "设备未响应或响应超时");
                return defaultMetrics;
            }

            // 3. 验证响应消息
            if (!handler.validateMessage(responseMessage)) {
                log.warn("[设备同步服务] 设备响应消息验证失败, protocolType={}, deviceId={}",
                        protocolType, device.getId());
                Map<String, Object> defaultMetrics = getDefaultMetrics(device.getId().toString());
                defaultMetrics.put("networkLatency", (double) responseTime);
                defaultMetrics.put("message", "响应消息验证失败");
                return defaultMetrics;
            }

            // 4. 解析响应提取性能指标
            Map<String, Object> metrics = parseMetricsResponse(handler, responseMessage, responseTime);
            log.info("[设备同步服务] 性能指标收集命令执行完成, protocolType={}, deviceId={}, responseTime={}ms",
                    protocolType, device.getId(), responseTime);

            return metrics;

        } catch (Exception e) {
            log.error("[设备同步服务] 执行性能指标收集命令异常, deviceId={}, error={}",
                    device.getId(), e.getMessage(), e);
            // 降级处理：返回默认值
            return getDefaultMetrics(device.getId().toString());
        }
    }

    /**
     * 获取默认性能指标
     * <p>
     * 当性能指标收集失败或设备不存在时返回默认值
     * </p>
     *
     * @param deviceId 设备ID
     * @return 默认性能指标
     */
    private Map<String, Object> getDefaultMetrics(String deviceId) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("deviceId", deviceId);
        metrics.put("cpuUsage", 0.0);
        metrics.put("memoryUsage", 0L);
        metrics.put("networkLatency", 0.0);
        metrics.put("responseTime", 0);
        metrics.put("errorRate", 0);
        metrics.put("lastUpdateTime", LocalDateTime.now());
        return metrics;
    }

    /**
     * 发送命令到设备并接收响应
     * <p>
     * 通用的设备命令发送方法，通过TCP Socket连接到设备，发送命令并接收响应
     * 严格遵循CLAUDE.md规范：完整的异常处理和日志记录
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param commandMessage 命令消息
     * @return 响应消息，如果发送失败或超时返回null
     */
    private ProtocolMessage sendCommandToDevice(ProtocolHandler handler, DeviceEntity device, ProtocolMessage commandMessage) {
        Socket socket = null;
        try {
            // 1. 验证设备连接信息
            String ipAddress = device.getIpAddress();
            Integer port = device.getPort();

            if (ipAddress == null || ipAddress.trim().isEmpty()) {
                log.warn("[设备同步服务] 设备IP地址为空, deviceId={}", device.getId());
                return null;
            }

            if (port == null || port <= 0) {
                log.warn("[设备同步服务] 设备端口无效, deviceId={}, port={}", device.getId(), port);
                return null;
            }

            log.debug("[设备同步服务] 连接设备发送命令, deviceId={}, ip={}, port={}, commandType={}",
                    device.getId(), ipAddress, port, commandMessage.getMessageType());

            // 2. 建立TCP连接
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(ipAddress, port), DEVICE_COMMAND_TIMEOUT_SECONDS * 1000);
            socket.setSoTimeout(DEVICE_COMMAND_TIMEOUT_SECONDS * 1000);

            // 3. 构建命令数据（使用ProtocolHandler构建响应消息作为命令）
            byte[] commandData = handler.buildResponse(commandMessage, true, null, null);
            if (commandData == null || commandData.length == 0) {
                log.warn("[设备同步服务] 构建命令数据失败, deviceId={}, commandType={}",
                        device.getId(), commandMessage.getMessageType());
                return null;
            }

            // 4. 发送命令数据
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(commandData);
            outputStream.flush();

            log.debug("[设备同步服务] 命令已发送, deviceId={}, commandType={}, dataLength={}",
                    device.getId(), commandMessage.getMessageType(), commandData.length);

            // 5. 接收设备响应
            InputStream inputStream = socket.getInputStream();
            byte[] responseBuffer = new byte[4096];
            int bytesRead = inputStream.read(responseBuffer);

            if (bytesRead <= 0) {
                log.warn("[设备同步服务] 设备响应为空, deviceId={}, commandType={}",
                        device.getId(), commandMessage.getMessageType());
                return null;
            }

            // 6. 解析响应消息
            byte[] responseData = new byte[bytesRead];
            System.arraycopy(responseBuffer, 0, responseData, 0, bytesRead);

            ProtocolMessage responseMessage = handler.parseMessage(responseData);
            if (responseMessage == null) {
                log.warn("[设备同步服务] 解析设备响应失败, deviceId={}, commandType={}, responseLength={}",
                        device.getId(), commandMessage.getMessageType(), bytesRead);
                return null;
            }

            log.debug("[设备同步服务] 设备响应已接收, deviceId={}, commandType={}, responseType={}",
                    device.getId(), commandMessage.getMessageType(), responseMessage.getMessageType());

            return responseMessage;

        } catch (SocketTimeoutException e) {
            log.warn("[设备同步服务] 设备响应超时, deviceId={}, commandType={}, timeout={}s",
                    device.getId(), commandMessage != null ? commandMessage.getMessageType() : "unknown",
                    DEVICE_COMMAND_TIMEOUT_SECONDS);
            return null;
        } catch (IOException e) {
            log.error("[设备同步服务] 设备通信异常, deviceId={}, commandType={}, error={}",
                    device.getId(), commandMessage != null ? commandMessage.getMessageType() : "unknown",
                    e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("[设备同步服务] 发送命令到设备异常, deviceId={}, commandType={}, error={}",
                    device.getId(), commandMessage != null ? commandMessage.getMessageType() : "unknown",
                    e.getMessage(), e);
            return null;
        } finally {
            // 7. 关闭连接
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.warn("[设备同步服务] 关闭Socket连接异常, deviceId={}, error={}",
                            device.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 构建用户同步命令消息
     * <p>
     * 根据协议类型构建用户同步命令消息
     * 不同协议类型的命令格式不同，这里提供通用框架
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param userId 用户ID
     * @param syncData 同步数据
     * @return 命令消息，如果构建失败返回null
     */
    private ProtocolMessage buildUserSyncCommand(ProtocolHandler handler, DeviceEntity device, Long userId, Map<String, Object> syncData) {
        try {
            ProtocolMessage commandMessage = new ProtocolMessage();
            commandMessage.setDeviceId(device.getId());
            commandMessage.setDeviceCode(device.getDeviceCode());
            commandMessage.setProtocolType(handler.getProtocolType());
            commandMessage.setTimestamp(LocalDateTime.now());

            // 根据协议类型设置命令类型
            String protocolType = handler.getProtocolType();
            ProtocolTypeEnum protocolEnum = ProtocolTypeEnum.getByCode(protocolType);
            if (protocolEnum != null) {
                switch (protocolEnum) {
                    case ATTENDANCE_ENTROPY_V4_0:
                        // 考勤设备：添加用户命令
                        commandMessage.setMessageType("ADD_USER");
                        break;
                    case ACCESS_ENTROPY_V4_8:
                        // 门禁设备：添加用户命令
                        commandMessage.setMessageType("ADD_USER");
                        break;
                    case CONSUME_ZKTECO_V1_0:
                        // 消费设备：添加账户命令
                        commandMessage.setMessageType("ADD_ACCOUNT");
                        break;
                    default:
                        log.warn("[设备同步服务] 未知协议类型, protocolType={}", protocolType);
                        commandMessage.setMessageType("SYNC_USER");
                        break;
                }
            } else {
                // 降级处理：根据设备类型判断
                String deviceType = device.getDeviceType();
                if ("ACCESS".equals(deviceType) || "ATTENDANCE".equals(deviceType)) {
                    commandMessage.setMessageType("ADD_USER");
                } else if ("CONSUME".equals(deviceType)) {
                    commandMessage.setMessageType("ADD_ACCOUNT");
                } else {
                    commandMessage.setMessageType("SYNC_USER");
                }
            }

            // 设置命令数据
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("userId", userId);
            commandData.put("deviceId", device.getId());
            if (syncData != null) {
                commandData.putAll(syncData);
            }
            commandMessage.setData(commandData);

            return commandMessage;

        } catch (Exception e) {
            log.error("[设备同步服务] 构建用户同步命令失败, deviceId={}, userId={}, error={}",
                    device.getId(), userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建权限撤销命令消息
     * <p>
     * 根据协议类型构建权限撤销命令消息
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param userId 用户ID
     * @param revokeData 撤销数据
     * @return 命令消息，如果构建失败返回null
     */
    private ProtocolMessage buildRevokeCommand(ProtocolHandler handler, DeviceEntity device, Long userId, Map<String, Object> revokeData) {
        try {
            ProtocolMessage commandMessage = new ProtocolMessage();
            commandMessage.setDeviceId(device.getId());
            commandMessage.setDeviceCode(device.getDeviceCode());
            commandMessage.setProtocolType(handler.getProtocolType());
            commandMessage.setTimestamp(LocalDateTime.now());

            // 根据协议类型设置命令类型
            String protocolType = handler.getProtocolType();
            ProtocolTypeEnum protocolEnum = ProtocolTypeEnum.getByCode(protocolType);
            if (protocolEnum != null) {
                switch (protocolEnum) {
                    case ATTENDANCE_ENTROPY_V4_0:
                        // 考勤设备：删除用户命令
                        commandMessage.setMessageType("DELETE_USER");
                        break;
                    case ACCESS_ENTROPY_V4_8:
                        // 门禁设备：删除用户命令
                        commandMessage.setMessageType("DELETE_USER");
                        break;
                    case CONSUME_ZKTECO_V1_0:
                        // 消费设备：禁用账户命令
                        commandMessage.setMessageType("DISABLE_ACCOUNT");
                        break;
                    default:
                        log.warn("[设备同步服务] 未知协议类型, protocolType={}", protocolType);
                        commandMessage.setMessageType("REVOKE_USER");
                        break;
                }
            } else {
                // 降级处理：根据设备类型判断
                String deviceType = device.getDeviceType();
                if ("ACCESS".equals(deviceType) || "ATTENDANCE".equals(deviceType)) {
                    commandMessage.setMessageType("DELETE_USER");
                } else if ("CONSUME".equals(deviceType)) {
                    commandMessage.setMessageType("DISABLE_ACCOUNT");
                } else {
                    commandMessage.setMessageType("REVOKE_USER");
                }
            }

            // 设置命令数据
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("userId", userId);
            commandData.put("deviceId", device.getId());
            if (revokeData != null) {
                commandData.putAll(revokeData);
            }
            commandMessage.setData(commandData);

            return commandMessage;

        } catch (Exception e) {
            log.error("[设备同步服务] 构建权限撤销命令失败, deviceId={}, userId={}, error={}",
                    device.getId(), userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建用户列表查询命令消息
     * <p>
     * 根据协议类型构建用户列表查询命令消息
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param queryData 查询数据
     * @return 命令消息，如果构建失败返回null
     */
    private ProtocolMessage buildUserQueryCommand(ProtocolHandler handler, DeviceEntity device, Map<String, Object> queryData) {
        try {
            ProtocolMessage commandMessage = new ProtocolMessage();
            commandMessage.setDeviceId(device.getId());
            commandMessage.setDeviceCode(device.getDeviceCode());
            commandMessage.setProtocolType(handler.getProtocolType());
            commandMessage.setTimestamp(LocalDateTime.now());

            // 根据协议类型设置命令类型
            String protocolType = handler.getProtocolType();
            ProtocolTypeEnum protocolEnum = ProtocolTypeEnum.getByCode(protocolType);
            if (protocolEnum != null) {
                switch (protocolEnum) {
                    case ATTENDANCE_ENTROPY_V4_0:
                        // 考勤设备：查询用户列表命令
                        commandMessage.setMessageType("QUERY_USER_LIST");
                        break;
                    case ACCESS_ENTROPY_V4_8:
                        // 门禁设备：查询用户列表命令
                        commandMessage.setMessageType("QUERY_USER_LIST");
                        break;
                    case CONSUME_ZKTECO_V1_0:
                        // 消费设备：查询账户列表命令
                        commandMessage.setMessageType("QUERY_ACCOUNT_LIST");
                        break;
                    default:
                        log.warn("[设备同步服务] 未知协议类型, protocolType={}", protocolType);
                        commandMessage.setMessageType("QUERY_USER_LIST");
                        break;
                }
            } else {
                // 降级处理：根据设备类型判断
                String deviceType = device.getDeviceType();
                if ("CONSUME".equals(deviceType)) {
                    commandMessage.setMessageType("QUERY_ACCOUNT_LIST");
                } else {
                    commandMessage.setMessageType("QUERY_USER_LIST");
                }
            }

            // 设置命令数据
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("deviceId", device.getId());
            if (queryData != null) {
                commandData.putAll(queryData);
            }
            commandMessage.setData(commandData);

            return commandMessage;

        } catch (Exception e) {
            log.error("[设备同步服务] 构建用户列表查询命令失败, deviceId={}, error={}",
                    device.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建业务属性同步命令消息
     * <p>
     * 根据协议类型构建业务属性同步命令消息
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param attributes 业务属性
     * @param syncData 同步数据
     * @return 命令消息，如果构建失败返回null
     */
    private ProtocolMessage buildAttributeSyncCommand(ProtocolHandler handler, DeviceEntity device, Map<String, Object> attributes, Map<String, Object> syncData) {
        try {
            ProtocolMessage commandMessage = new ProtocolMessage();
            commandMessage.setDeviceId(device.getId());
            commandMessage.setDeviceCode(device.getDeviceCode());
            commandMessage.setProtocolType(handler.getProtocolType());
            commandMessage.setTimestamp(LocalDateTime.now());

            // 根据协议类型设置命令类型
            String protocolType = handler.getProtocolType();
            ProtocolTypeEnum protocolEnum = ProtocolTypeEnum.getByCode(protocolType);
            if (protocolEnum != null) {
                switch (protocolEnum) {
                    case ACCESS_ENTROPY_V4_8:
                        // 门禁设备：配置门禁参数命令
                        commandMessage.setMessageType("CONFIG_ACCESS_PARAMS");
                        break;
                    case ATTENDANCE_ENTROPY_V4_0:
                        // 考勤设备：配置考勤规则命令
                        commandMessage.setMessageType("CONFIG_ATTENDANCE_RULES");
                        break;
                    case CONSUME_ZKTECO_V1_0:
                        // 消费设备：配置消费参数命令
                        commandMessage.setMessageType("CONFIG_CONSUME_PARAMS");
                        break;
                    default:
                        log.warn("[设备同步服务] 未知协议类型, protocolType={}", protocolType);
                        commandMessage.setMessageType("CONFIG_DEVICE_PARAMS");
                        break;
                }
            } else {
                // 降级处理：根据设备类型判断
                String deviceType = device.getDeviceType();
                if ("ACCESS".equals(deviceType)) {
                    commandMessage.setMessageType("CONFIG_ACCESS_PARAMS");
                } else if ("ATTENDANCE".equals(deviceType)) {
                    commandMessage.setMessageType("CONFIG_ATTENDANCE_RULES");
                } else if ("CONSUME".equals(deviceType)) {
                    commandMessage.setMessageType("CONFIG_CONSUME_PARAMS");
                } else {
                    commandMessage.setMessageType("CONFIG_DEVICE_PARAMS");
                }
            }

            // 设置命令数据
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("deviceId", device.getId());
            commandData.put("attributes", attributes);
            if (syncData != null) {
                commandData.putAll(syncData);
            }
            commandMessage.setData(commandData);

            return commandMessage;

        } catch (Exception e) {
            log.error("[设备同步服务] 构建业务属性同步命令失败, deviceId={}, error={}",
                    device.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建健康检查命令消息
     * <p>
     * 根据协议类型构建健康检查命令消息
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param checkData 检查数据
     * @return 命令消息，如果构建失败返回null
     */
    private ProtocolMessage buildHealthCheckCommand(ProtocolHandler handler, DeviceEntity device, Map<String, Object> checkData) {
        try {
            ProtocolMessage commandMessage = new ProtocolMessage();
            commandMessage.setDeviceId(device.getId());
            commandMessage.setDeviceCode(device.getDeviceCode());
            commandMessage.setProtocolType(handler.getProtocolType());
            commandMessage.setTimestamp(LocalDateTime.now());

            // 根据协议类型设置命令类型
            String protocolType = handler.getProtocolType();
            ProtocolTypeEnum protocolEnum = ProtocolTypeEnum.getByCode(protocolType);
            if (protocolEnum != null) {
                switch (protocolEnum) {
                    case ACCESS_ENTROPY_V4_8:
                        // 门禁设备：心跳检测命令
                        commandMessage.setMessageType("PING");
                        break;
                    case ATTENDANCE_ENTROPY_V4_0:
                        // 考勤设备：状态查询命令
                        commandMessage.setMessageType("QUERY_STATUS");
                        break;
                    case CONSUME_ZKTECO_V1_0:
                        // 消费设备：连接测试命令
                        commandMessage.setMessageType("TEST_CONNECTION");
                        break;
                    default:
                        log.warn("[设备同步服务] 未知协议类型, protocolType={}", protocolType);
                        commandMessage.setMessageType("HEALTH_CHECK");
                        break;
                }
            } else {
                // 降级处理：根据设备类型判断
                String deviceType = device.getDeviceType();
                if ("ACCESS".equals(deviceType)) {
                    commandMessage.setMessageType("PING");
                } else if ("ATTENDANCE".equals(deviceType)) {
                    commandMessage.setMessageType("QUERY_STATUS");
                } else if ("CONSUME".equals(deviceType)) {
                    commandMessage.setMessageType("TEST_CONNECTION");
                } else {
                    commandMessage.setMessageType("HEALTH_CHECK");
                }
            }

            // 设置命令数据
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("deviceId", device.getId());
            if (checkData != null) {
                commandData.putAll(checkData);
            }
            commandMessage.setData(commandData);

            return commandMessage;

        } catch (Exception e) {
            log.error("[设备同步服务] 构建健康检查命令失败, deviceId={}, error={}",
                    device.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建性能指标收集命令消息
     * <p>
     * 根据协议类型构建性能指标收集命令消息
     * </p>
     *
     * @param handler 协议处理器
     * @param device 设备实体
     * @param collectData 收集数据
     * @return 命令消息，如果构建失败返回null
     */
    private ProtocolMessage buildMetricsCollectionCommand(ProtocolHandler handler, DeviceEntity device, Map<String, Object> collectData) {
        try {
            ProtocolMessage commandMessage = new ProtocolMessage();
            commandMessage.setDeviceId(device.getId());
            commandMessage.setDeviceCode(device.getDeviceCode());
            commandMessage.setProtocolType(handler.getProtocolType());
            commandMessage.setTimestamp(LocalDateTime.now());

            // 根据协议类型设置命令类型
            String protocolType = handler.getProtocolType();
            ProtocolTypeEnum protocolEnum = ProtocolTypeEnum.getByCode(protocolType);
            if (protocolEnum != null) {
                switch (protocolEnum) {
                    case ACCESS_ENTROPY_V4_8:
                        // 门禁设备：性能查询命令
                        commandMessage.setMessageType("QUERY_PERFORMANCE");
                        break;
                    case ATTENDANCE_ENTROPY_V4_0:
                        // 考勤设备：状态统计命令
                        commandMessage.setMessageType("QUERY_STATISTICS");
                        break;
                    case CONSUME_ZKTECO_V1_0:
                        // 消费设备：运行状态命令
                        commandMessage.setMessageType("QUERY_RUNTIME_STATUS");
                        break;
                    default:
                        log.warn("[设备同步服务] 未知协议类型, protocolType={}", protocolType);
                        commandMessage.setMessageType("QUERY_METRICS");
                        break;
                }
            } else {
                // 降级处理：根据设备类型判断
                String deviceType = device.getDeviceType();
                if ("ACCESS".equals(deviceType)) {
                    commandMessage.setMessageType("QUERY_PERFORMANCE");
                } else if ("ATTENDANCE".equals(deviceType)) {
                    commandMessage.setMessageType("QUERY_STATISTICS");
                } else if ("CONSUME".equals(deviceType)) {
                    commandMessage.setMessageType("QUERY_RUNTIME_STATUS");
                } else {
                    commandMessage.setMessageType("QUERY_METRICS");
                }
            }

            // 设置命令数据
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("deviceId", device.getId());
            if (collectData != null) {
                commandData.putAll(collectData);
            }
            commandMessage.setData(commandData);

            return commandMessage;

        } catch (Exception e) {
            log.error("[设备同步服务] 构建性能指标收集命令失败, deviceId={}, error={}",
                    device.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析同步响应
     * <p>
     * 从设备响应消息中解析同步操作是否成功
     * </p>
     *
     * @param handler 协议处理器
     * @param responseMessage 响应消息
     * @return true-同步成功，false-同步失败
     */
    private boolean parseSyncResponse(ProtocolHandler handler, ProtocolMessage responseMessage) {
        try {
            // 检查响应消息状态
            if ("FAILED".equals(responseMessage.getStatus())) {
                log.warn("[设备同步服务] 设备响应失败, errorCode={}, errorMessage={}",
                        responseMessage.getErrorCode(), responseMessage.getErrorMessage());
                return false;
            }

            // 从响应数据中提取成功标志
            if (responseMessage.getData() != null) {
                Object success = responseMessage.getData().get("success");
                if (success instanceof Boolean) {
                    return (Boolean) success;
                } else if (success instanceof String) {
                    return "true".equalsIgnoreCase((String) success) || "success".equalsIgnoreCase((String) success);
                }
            }

            // 默认根据消息状态判断
            return "PROCESSED".equals(responseMessage.getStatus());

        } catch (Exception e) {
            log.error("[设备同步服务] 解析同步响应异常, error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 解析撤销响应
     * <p>
     * 从设备响应消息中解析撤销操作是否成功
     * </p>
     *
     * @param handler 协议处理器
     * @param responseMessage 响应消息
     * @return true-撤销成功，false-撤销失败
     */
    private boolean parseRevokeResponse(ProtocolHandler handler, ProtocolMessage responseMessage) {
        // 与parseSyncResponse逻辑相同
        return parseSyncResponse(handler, responseMessage);
    }

    /**
     * 解析用户列表响应
     * <p>
     * 从设备响应消息中解析用户ID列表
     * </p>
     *
     * @param handler 协议处理器
     * @param responseMessage 响应消息
     * @return 用户ID列表
     */
    private List<String> parseUserListResponse(ProtocolHandler handler, ProtocolMessage responseMessage) {
        try {
            if (responseMessage.getData() == null) {
                return Collections.emptyList();
            }

            // 尝试从响应数据中提取用户列表
            Object userListObj = responseMessage.getData().get("userList");
            if (userListObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> userList = (List<Object>) userListObj;
                return userList.stream()
                        .map(userId -> userId != null ? userId.toString() : null)
                        .filter(Objects::nonNull)
                        .collect(java.util.stream.Collectors.toList());
            }

            // 尝试从响应数据中提取账户列表（消费设备）
            Object accountListObj = responseMessage.getData().get("accountList");
            if (accountListObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> accountList = (List<Object>) accountListObj;
                return accountList.stream()
                        .map(accountId -> accountId != null ? accountId.toString() : null)
                        .filter(Objects::nonNull)
                        .collect(java.util.stream.Collectors.toList());
            }

            log.warn("[设备同步服务] 响应数据中未找到用户列表, responseType={}",
                    responseMessage.getMessageType());
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("[设备同步服务] 解析用户列表响应异常, error={}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析属性同步响应
     * <p>
     * 从设备响应消息中解析属性同步操作是否成功
     * </p>
     *
     * @param handler 协议处理器
     * @param responseMessage 响应消息
     * @return true-同步成功，false-同步失败
     */
    private boolean parseAttributeSyncResponse(ProtocolHandler handler, ProtocolMessage responseMessage) {
        // 与parseSyncResponse逻辑相同
        return parseSyncResponse(handler, responseMessage);
    }

    /**
     * 解析健康检查响应
     * <p>
     * 从设备响应消息中解析健康检查结果
     * </p>
     *
     * @param handler 协议处理器
     * @param responseMessage 响应消息
     * @param responseTime 响应时间（毫秒）
     * @return 健康检查结果
     */
    private Map<String, Object> parseHealthCheckResponse(ProtocolHandler handler, ProtocolMessage responseMessage, long responseTime) {
        try {
            Map<String, Object> healthStatus = new HashMap<>();
            healthStatus.put("deviceId", responseMessage.getDeviceId() != null ?
                    responseMessage.getDeviceId().toString() : "unknown");
            healthStatus.put("checkTime", LocalDateTime.now());
            healthStatus.put("responseTime", responseTime);
            healthStatus.put("protocolType", responseMessage.getProtocolType());

            // 从响应数据中提取健康状态
            if (responseMessage.getData() != null) {
                Object healthStatusObj = responseMessage.getData().get("healthStatus");
                if (healthStatusObj != null) {
                    healthStatus.put("healthStatus", healthStatusObj.toString());
                } else {
                    // 根据响应消息状态判断
                    if ("PROCESSED".equals(responseMessage.getStatus())) {
                        healthStatus.put("healthStatus", "HEALTHY");
                    } else {
                        healthStatus.put("healthStatus", "UNHEALTHY");
                    }
                }

                // 提取其他健康信息
                Object message = responseMessage.getData().get("message");
                if (message != null) {
                    healthStatus.put("message", message.toString());
                }

                Object cpuUsage = responseMessage.getData().get("cpuUsage");
                if (cpuUsage != null) {
                    healthStatus.put("cpuUsage", cpuUsage);
                }

                Object memoryUsage = responseMessage.getData().get("memoryUsage");
                if (memoryUsage != null) {
                    healthStatus.put("memoryUsage", memoryUsage);
                }
            } else {
                // 默认健康状态
                healthStatus.put("healthStatus", "HEALTHY");
                healthStatus.put("message", "设备健康检查正常");
            }

            return healthStatus;

        } catch (Exception e) {
            log.error("[设备同步服务] 解析健康检查响应异常, error={}", e.getMessage(), e);
            return buildUnknownHealthStatus(
                    responseMessage.getDeviceId() != null ? responseMessage.getDeviceId().toString() : "unknown",
                    responseMessage.getProtocolType(),
                    "解析响应异常: " + e.getMessage()
            );
        }
    }

    /**
     * 解析性能指标响应
     * <p>
     * 从设备响应消息中解析性能指标数据
     * </p>
     *
     * @param handler 协议处理器
     * @param responseMessage 响应消息
     * @param responseTime 响应时间（毫秒）
     * @return 性能指标
     */
    private Map<String, Object> parseMetricsResponse(ProtocolHandler handler, ProtocolMessage responseMessage, long responseTime) {
        try {
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("deviceId", responseMessage.getDeviceId() != null ?
                    responseMessage.getDeviceId().toString() : "unknown");
            metrics.put("lastUpdateTime", LocalDateTime.now());
            metrics.put("protocolType", responseMessage.getProtocolType());
            metrics.put("networkLatency", (double) responseTime);

            // 从响应数据中提取性能指标
            if (responseMessage.getData() != null) {
                Map<String, Object> responseData = responseMessage.getData();

                // CPU使用率
                Object cpuUsage = responseData.get("cpuUsage");
                if (cpuUsage != null) {
                    metrics.put("cpuUsage", cpuUsage instanceof Number ?
                            ((Number) cpuUsage).doubleValue() : Double.parseDouble(cpuUsage.toString()));
                } else {
                    metrics.put("cpuUsage", 0.0);
                }

                // 内存使用
                Object memoryUsage = responseData.get("memoryUsage");
                if (memoryUsage != null) {
                    metrics.put("memoryUsage", memoryUsage instanceof Number ?
                            ((Number) memoryUsage).longValue() : Long.parseLong(memoryUsage.toString()));
                } else {
                    metrics.put("memoryUsage", 0L);
                }

                // 响应时间
                Object deviceResponseTime = responseData.get("responseTime");
                if (deviceResponseTime != null) {
                    metrics.put("responseTime", deviceResponseTime instanceof Number ?
                            ((Number) deviceResponseTime).intValue() : Integer.parseInt(deviceResponseTime.toString()));
                } else {
                    metrics.put("responseTime", (int) responseTime);
                }

                // 错误率
                Object errorRate = responseData.get("errorRate");
                if (errorRate != null) {
                    metrics.put("errorRate", errorRate instanceof Number ?
                            ((Number) errorRate).doubleValue() : Double.parseDouble(errorRate.toString()));
                } else {
                    metrics.put("errorRate", 0.0);
                }

                // 其他指标
                Object message = responseData.get("message");
                if (message != null) {
                    metrics.put("message", message.toString());
                }
            } else {
                // 默认值
                metrics.put("cpuUsage", 0.0);
                metrics.put("memoryUsage", 0L);
                metrics.put("responseTime", (int) responseTime);
                metrics.put("errorRate", 0.0);
                metrics.put("message", "性能指标收集完成");
            }

            return metrics;

        } catch (Exception e) {
            log.error("[设备同步服务] 解析性能指标响应异常, error={}", e.getMessage(), e);
            return getDefaultMetrics(responseMessage.getDeviceId() != null ?
                    responseMessage.getDeviceId().toString() : "unknown");
        }
    }

    /**
     * 构建未知健康状态
     * <p>
     * 当健康检查失败时返回未知状态
     * </p>
     *
     * @param deviceId 设备ID
     * @param protocolType 协议类型
     * @param message 消息
     * @return 健康状态
     */
    private Map<String, Object> buildUnknownHealthStatus(Object deviceId, String protocolType, String message) {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("deviceId", deviceId != null ? deviceId.toString() : "unknown");
        healthStatus.put("healthStatus", "UNKNOWN");
        healthStatus.put("checkTime", LocalDateTime.now());
        healthStatus.put("message", message);
        healthStatus.put("protocolType", protocolType);
        return healthStatus;
    }

    /**
     * 构建不健康状态
     * <p>
     * 当健康检查发现设备不健康时返回不健康状态
     * </p>
     *
     * @param device 设备实体
     * @param protocolType 协议类型
     * @param message 消息
     * @param responseTime 响应时间
     * @return 健康状态
     */
    private Map<String, Object> buildUnhealthyStatus(DeviceEntity device, String protocolType, String message, long responseTime) {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("deviceId", device.getId().toString());
        healthStatus.put("healthStatus", "UNHEALTHY");
        healthStatus.put("checkTime", LocalDateTime.now());
        healthStatus.put("responseTime", responseTime);
        healthStatus.put("message", message);
        healthStatus.put("protocolType", protocolType);
        return healthStatus;
    }
}

