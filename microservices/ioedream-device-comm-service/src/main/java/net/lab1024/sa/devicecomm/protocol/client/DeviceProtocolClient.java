package net.lab1024.sa.devicecomm.protocol.client;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
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
 * 设备协议客户端
 * <p>
 * 承载设备协议选择、命令构造/发送、响应解析等“协议与传输”细节，供 DeviceSyncService 编排层调用。
 * </p>
 */
@Slf4j
@Service
public class DeviceProtocolClient {

    @Resource
    private ProtocolAdapterFactory protocolAdapterFactory;

    // 设备命令发送超时时间（秒）
    private static final int DEVICE_COMMAND_TIMEOUT_SECONDS = 10;

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
    public boolean performDeviceUserSync(DeviceEntity device, Long userId, Map<String, Object> syncData) {
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行设备用户同步参数错误: deviceId={}, userId={}, error={}", device != null ? device.getId() : "null", userId, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行设备用户同步业务异常: deviceId={}, userId={}, code={}, message={}", device != null ? device.getId() : "null", userId, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行设备用户同步系统异常: deviceId={}, userId={}, code={}, message={}", device != null ? device.getId() : "null", userId, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备用户同步未知异常: deviceId={}, userId={}", device != null ? device.getId() : "null", userId, e);
            return false; // For boolean return methods, return false on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行用户同步命令参数错误: deviceId={}, userId={}, error={}", device.getId(), userId, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行用户同步命令业务异常: deviceId={}, userId={}, code={}, message={}", device.getId(), userId, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行用户同步命令系统异常: deviceId={}, userId={}, code={}, message={}", device.getId(), userId, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 执行用户同步命令未知异常: deviceId={}, userId={}", device.getId(), userId, e);
            return false; // For boolean return methods, return false on unknown error
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
    public boolean performDeviceUserRevoke(DeviceEntity device, Long userId) {
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行设备用户权限撤销参数错误: deviceId={}, userId={}, error={}", device != null ? device.getId() : "null", userId, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行设备用户权限撤销业务异常: deviceId={}, userId={}, code={}, message={}", device != null ? device.getId() : "null", userId, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行设备用户权限撤销系统异常: deviceId={}, userId={}, code={}, message={}", device != null ? device.getId() : "null", userId, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备用户权限撤销未知异常: deviceId={}, userId={}", device != null ? device.getId() : "null", userId, e);
            return false; // For boolean return methods, return false on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行权限撤销命令参数错误: deviceId={}, userId={}, error={}", device.getId(), userId, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行权限撤销命令业务异常: deviceId={}, userId={}, code={}, message={}", device.getId(), userId, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行权限撤销命令系统异常: deviceId={}, userId={}, code={}, message={}", device.getId(), userId, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 执行权限撤销命令未知异常: deviceId={}, userId={}", device.getId(), userId, e);
            return false; // For boolean return methods, return false on unknown error
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
    public List<String> performDeviceUserQuery(DeviceEntity device) {
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行设备用户列表查询参数错误: deviceId={}, error={}", device != null ? device.getId() : "null", e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行设备用户列表查询业务异常: deviceId={}, code={}, message={}", device != null ? device.getId() : "null", e.getCode(), e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行设备用户列表查询系统异常: deviceId={}, code={}, message={}", device != null ? device.getId() : "null", e.getCode(), e.getMessage(), e);
            return Collections.emptyList(); // For List return methods, return empty list on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备用户列表查询未知异常: deviceId={}", device != null ? device.getId() : "null", e);
            return Collections.emptyList(); // For List return methods, return empty list on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行用户列表查询命令参数错误: deviceId={}, error={}", device.getId(), e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行用户列表查询命令业务异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行用户列表查询命令系统异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage(), e);
            return Collections.emptyList(); // For List return methods, return empty list on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 执行用户列表查询命令未知异常: deviceId={}", device.getId(), e);
            return Collections.emptyList(); // For List return methods, return empty list on unknown error
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
    public boolean performDeviceAttributeSync(DeviceEntity device, Map<String, Object> attributes) {
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行设备业务属性同步参数错误: deviceId={}, error={}", device != null ? device.getId() : "null", e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行设备业务属性同步业务异常: deviceId={}, code={}, message={}", device != null ? device.getId() : "null", e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行设备业务属性同步系统异常: deviceId={}, code={}, message={}", device != null ? device.getId() : "null", e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备业务属性同步未知异常: deviceId={}", device != null ? device.getId() : "null", e);
            return false; // For boolean return methods, return false on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行业务属性同步命令参数错误: deviceId={}, error={}", device.getId(), e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行业务属性同步命令业务异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行业务属性同步命令系统异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 执行业务属性同步命令未知异常: deviceId={}", device.getId(), e);
            return false; // For boolean return methods, return false on unknown error
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
    public Map<String, Object> performDeviceHealthCheck(DeviceEntity device) {
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行设备健康检查参数错误: deviceId={}, error={}", device != null ? device.getId() : "null", e.getMessage());
            // 降级处理：返回错误状态
            return Map.of(
                    "deviceId", device != null ? device.getId().toString() : "null",
                    "healthStatus", "ERROR",
                    "message", "健康检查失败：参数错误 - " + e.getMessage(),
                    "checkTime", LocalDateTime.now()
            );
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行设备健康检查业务异常: deviceId={}, code={}, message={}", device != null ? device.getId() : "null", e.getCode(), e.getMessage());
            // 降级处理：返回错误状态
            return Map.of(
                    "deviceId", device != null ? device.getId().toString() : "null",
                    "healthStatus", "ERROR",
                    "message", "健康检查失败：" + e.getMessage(),
                    "checkTime", LocalDateTime.now()
            );
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行设备健康检查系统异常: deviceId={}, code={}, message={}", device != null ? device.getId() : "null", e.getCode(), e.getMessage(), e);
            // 降级处理：返回错误状态
            return Map.of(
                    "deviceId", device != null ? device.getId().toString() : "null",
                    "healthStatus", "ERROR",
                    "message", "健康检查失败：" + e.getMessage(),
                    "checkTime", LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备健康检查未知异常: deviceId={}", device != null ? device.getId() : "null", e);
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行健康检查命令参数错误: deviceId={}, error={}", device.getId(), e.getMessage());
            // 降级处理：返回错误状态
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("deviceId", device.getId().toString());
            errorStatus.put("healthStatus", "ERROR");
            errorStatus.put("checkTime", LocalDateTime.now());
            errorStatus.put("message", "健康检查命令执行失败：参数错误 - " + e.getMessage());
            return errorStatus;
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行健康检查命令业务异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage());
            // 降级处理：返回错误状态
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("deviceId", device.getId().toString());
            errorStatus.put("healthStatus", "ERROR");
            errorStatus.put("checkTime", LocalDateTime.now());
            errorStatus.put("message", "健康检查命令执行失败：" + e.getMessage());
            return errorStatus;
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行健康检查命令系统异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage(), e);
            // 降级处理：返回错误状态
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("deviceId", device.getId().toString());
            errorStatus.put("healthStatus", "ERROR");
            errorStatus.put("checkTime", LocalDateTime.now());
            errorStatus.put("message", "健康检查命令执行失败：" + e.getMessage());
            return errorStatus;
        } catch (Exception e) {
            log.error("[设备同步服务] 执行健康检查命令未知异常: deviceId={}", device.getId(), e);
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
    public Map<String, Object> performDeviceMetricsCollection(DeviceEntity device) {
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行设备性能指标收集参数错误: deviceId={}, error={}", device != null ? device.getId() : "null", e.getMessage());
            // 降级处理：返回默认值
            return getDefaultMetrics(device != null ? device.getId().toString() : "null");
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行设备性能指标收集业务异常: deviceId={}, code={}, message={}", device != null ? device.getId() : "null", e.getCode(), e.getMessage());
            // 降级处理：返回默认值
            return getDefaultMetrics(device != null ? device.getId().toString() : "null");
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行设备性能指标收集系统异常: deviceId={}, code={}, message={}", device != null ? device.getId() : "null", e.getCode(), e.getMessage(), e);
            // 降级处理：返回默认值
            return getDefaultMetrics(device != null ? device.getId().toString() : "null");
        } catch (Exception e) {
            log.error("[设备同步服务] 执行设备性能指标收集未知异常: deviceId={}", device != null ? device.getId() : "null", e);
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 执行性能指标收集命令参数错误: deviceId={}, error={}", device.getId(), e.getMessage());
            // 降级处理：返回默认值
            return getDefaultMetrics(device.getId().toString());
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 执行性能指标收集命令业务异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage());
            // 降级处理：返回默认值
            return getDefaultMetrics(device.getId().toString());
        } catch (SystemException e) {
            log.error("[设备同步服务] 执行性能指标收集命令系统异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage(), e);
            // 降级处理：返回默认值
            return getDefaultMetrics(device.getId().toString());
        } catch (Exception e) {
            log.error("[设备同步服务] 执行性能指标收集命令未知异常: deviceId={}", device.getId(), e);
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 发送命令到设备参数错误: deviceId={}, commandType={}, error={}", device.getId(), commandMessage != null ? commandMessage.getMessageType() : "unknown", e.getMessage());
            return null; // For ProtocolMessage return methods, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 发送命令到设备业务异常: deviceId={}, commandType={}, code={}, message={}", device.getId(), commandMessage != null ? commandMessage.getMessageType() : "unknown", e.getCode(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 发送命令到设备系统异常: deviceId={}, commandType={}, code={}, message={}", device.getId(), commandMessage != null ? commandMessage.getMessageType() : "unknown", e.getCode(), e.getMessage(), e);
            return null; // For ProtocolMessage return methods, return null on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 发送命令到设备未知异常: deviceId={}, commandType={}", device.getId(), commandMessage != null ? commandMessage.getMessageType() : "unknown", e);
            return null; // For ProtocolMessage return methods, return null on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 构建用户同步命令参数错误: deviceId={}, userId={}, error={}", device.getId(), userId, e.getMessage());
            return null; // For ProtocolMessage return methods, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 构建用户同步命令业务异常: deviceId={}, userId={}, code={}, message={}", device.getId(), userId, e.getCode(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 构建用户同步命令系统异常: deviceId={}, userId={}, code={}, message={}", device.getId(), userId, e.getCode(), e.getMessage(), e);
            return null; // For ProtocolMessage return methods, return null on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 构建用户同步命令未知异常: deviceId={}, userId={}", device.getId(), userId, e);
            return null; // For ProtocolMessage return methods, return null on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 构建权限撤销命令参数错误: deviceId={}, userId={}, error={}", device.getId(), userId, e.getMessage());
            return null; // For ProtocolMessage return methods, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 构建权限撤销命令业务异常: deviceId={}, userId={}, code={}, message={}", device.getId(), userId, e.getCode(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 构建权限撤销命令系统异常: deviceId={}, userId={}, code={}, message={}", device.getId(), userId, e.getCode(), e.getMessage(), e);
            return null; // For ProtocolMessage return methods, return null on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 构建权限撤销命令未知异常: deviceId={}, userId={}", device.getId(), userId, e);
            return null; // For ProtocolMessage return methods, return null on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 构建用户列表查询命令参数错误: deviceId={}, error={}", device.getId(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 构建用户列表查询命令业务异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 构建用户列表查询命令系统异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage(), e);
            return null; // For ProtocolMessage return methods, return null on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 构建用户列表查询命令未知异常: deviceId={}", device.getId(), e);
            return null; // For ProtocolMessage return methods, return null on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 构建业务属性同步命令参数错误: deviceId={}, error={}", device.getId(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 构建业务属性同步命令业务异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 构建业务属性同步命令系统异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage(), e);
            return null; // For ProtocolMessage return methods, return null on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 构建业务属性同步命令未知异常: deviceId={}", device.getId(), e);
            return null; // For ProtocolMessage return methods, return null on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 构建健康检查命令参数错误: deviceId={}, error={}", device.getId(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 构建健康检查命令业务异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 构建健康检查命令系统异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage(), e);
            return null; // For ProtocolMessage return methods, return null on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 构建健康检查命令未知异常: deviceId={}", device.getId(), e);
            return null; // For ProtocolMessage return methods, return null on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 构建性能指标收集命令参数错误: deviceId={}, error={}", device.getId(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 构建性能指标收集命令业务异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage());
            return null; // For ProtocolMessage return methods, return null on business error
        } catch (SystemException e) {
            log.error("[设备同步服务] 构建性能指标收集命令系统异常: deviceId={}, code={}, message={}", device.getId(), e.getCode(), e.getMessage(), e);
            return null; // For ProtocolMessage return methods, return null on system error
        } catch (Exception e) {
            log.error("[设备同步服务] 构建性能指标收集命令未知异常: deviceId={}", device.getId(), e);
            return null; // For ProtocolMessage return methods, return null on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 解析健康检查响应参数错误: error={}", e.getMessage());
            return buildUnknownHealthStatus(
                    responseMessage.getDeviceId() != null ? responseMessage.getDeviceId().toString() : "unknown",
                    responseMessage.getProtocolType(),
                    "解析响应异常：参数错误 - " + e.getMessage()
            );
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 解析健康检查响应业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return buildUnknownHealthStatus(
                    responseMessage.getDeviceId() != null ? responseMessage.getDeviceId().toString() : "unknown",
                    responseMessage.getProtocolType(),
                    "解析响应异常：" + e.getMessage()
            );
        } catch (SystemException e) {
            log.error("[设备同步服务] 解析健康检查响应系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return buildUnknownHealthStatus(
                    responseMessage.getDeviceId() != null ? responseMessage.getDeviceId().toString() : "unknown",
                    responseMessage.getProtocolType(),
                    "解析响应异常：" + e.getMessage()
            );
        } catch (Exception e) {
            log.error("[设备同步服务] 解析健康检查响应未知异常", e);
            return buildUnknownHealthStatus(
                    responseMessage.getDeviceId() != null ? responseMessage.getDeviceId().toString() : "unknown",
                    responseMessage.getProtocolType(),
                    "解析响应异常：" + e.getMessage()
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[设备同步服务] 解析性能指标响应参数错误: error={}", e.getMessage());
            return getDefaultMetrics(responseMessage.getDeviceId() != null ?
                    responseMessage.getDeviceId().toString() : "unknown");
        } catch (BusinessException e) {
            log.warn("[设备同步服务] 解析性能指标响应业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return getDefaultMetrics(responseMessage.getDeviceId() != null ?
                    responseMessage.getDeviceId().toString() : "unknown");
        } catch (SystemException e) {
            log.error("[设备同步服务] 解析性能指标响应系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return getDefaultMetrics(responseMessage.getDeviceId() != null ?
                    responseMessage.getDeviceId().toString() : "unknown");
        } catch (Exception e) {
            log.error("[设备同步服务] 解析性能指标响应未知异常", e);
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
