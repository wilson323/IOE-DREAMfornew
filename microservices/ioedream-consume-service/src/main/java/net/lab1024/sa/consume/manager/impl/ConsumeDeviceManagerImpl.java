package net.lab1024.sa.consume.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.util.JsonUtil;
import net.lab1024.sa.consume.manager.ConsumeDeviceManager;

/**
 * 消费设备管理Manager实现类
 * <p>
 * 实现设备相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager实现类在ioedream-consume-service中
 * - 通过构造函数注入依赖
 * - 保持为纯Java类（不使用Spring注解）
 * </p>
 * <p>
 * 业务场景：
 * - 设备信息查询
 * - 设备状态管理
 * - 设备权限验证
 * - 设备消费模式配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class ConsumeDeviceManagerImpl implements ConsumeDeviceManager {

    private final GatewayServiceClient gatewayServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：通过构造函数接收依赖
     * 使用Spring Boot标准方案：优先使用注入的ObjectMapper Bean（通过JacksonConfiguration配置）
     * 如果没有注入ObjectMapper，则使用JsonUtil作为fallback（向后兼容）
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     * @param objectMapper         JSON解析器（Spring Boot ObjectMapper Bean，推荐使用）
     */
    public ConsumeDeviceManagerImpl(GatewayServiceClient gatewayServiceClient, ObjectMapper objectMapper) {
        this.gatewayServiceClient = gatewayServiceClient;
        // 优先使用Spring Boot的ObjectMapper Bean，如果不可用则使用JsonUtil作为fallback（向后兼容）
        // 说明：单元测试可能传入 Mockito mock 的 ObjectMapper（getTypeFactory() 为 null），此时需要降级为真实
        // ObjectMapper。
        if (objectMapper == null || objectMapper.getTypeFactory() == null) {
            this.objectMapper = JsonUtil.getObjectMapper();
        } else {
            this.objectMapper = objectMapper;
        }
    }

    /**
     * 根据设备ID获取设备信息
     * <p>
     * 类型安全改进：返回具体类型DeviceEntity，而非Object
     * 提升代码可读性和类型安全性
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备信息，如果不存在则返回null
     */
    @Override
    public DeviceEntity getDeviceById(String deviceId) {
        log.debug("[设备管理] 根据设备ID获取设备信息，deviceId={}", deviceId);
        try {
            // 通过网关调用公共服务获取设备信息
            net.lab1024.sa.common.dto.ResponseDTO<DeviceEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/device/" + deviceId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    DeviceEntity.class);
            if (response != null && response.isSuccess()) {
                return response.getData();
            }
            return null;
        } catch (Exception e) {
            log.error("[设备管理] 获取设备信息失败，deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 验证设备是否在线
     *
     * @param deviceId 设备ID
     * @return 是否在线
     */
    @Override
    public boolean isDeviceOnline(String deviceId) {
        log.debug("[设备管理] 验证设备是否在线，deviceId={}", deviceId);
        try {
            // DeviceEntity#deviceId 为 String（ASSIGN_ID），不可假设可解析为 Long
            DeviceEntity device = getDeviceById(deviceId);
            if (device == null) {
                return false;
            }
            // DeviceEntity.deviceStatus：1-在线 2-离线 3-故障 4-维护 5-停用
            return Integer.valueOf(1).equals(device.getDeviceStatus());
        } catch (Exception e) {
            log.error("[设备管理] 验证设备在线状态失败，deviceId={}", deviceId, e);
            return false;
        }
    }

    /**
     * 验证设备是否支持指定的消费模式
     *
     * @param deviceId    设备ID
     * @param consumeMode 消费模式（FIXED/AMOUNT/PRODUCT/COUNT）
     * @return 是否支持
     */
    @Override
    public boolean isConsumeModeSupported(String deviceId, String consumeMode) {
        log.debug("[设备管理] 验证设备是否支持消费模式，deviceId={}, consumeMode={}", deviceId, consumeMode);
        try {
            // DeviceEntity#deviceId 为 String（ASSIGN_ID），不可假设可解析为 Long
            DeviceEntity device = getDeviceById(deviceId);
            if (device == null) {
                return false;
            }

            // 从扩展属性中获取支持的消费模式
            String extendedAttributes = device.getExtendedAttributes();
            if (extendedAttributes == null || extendedAttributes.trim().isEmpty()) {
                return true; // 默认支持所有模式
            }

            // 解析扩展属性，检查支持的消费模式
            try {
                // 解析JSON格式的扩展属性
                Map<String, Object> attributes = objectMapper.readValue(extendedAttributes,
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

                // 检查是否包含支持的消费模式配置
                Object supportedModesObj = attributes.get("supportedConsumeModes");
                if (supportedModesObj == null) {
                    // 如果没有配置支持的消费模式，默认支持所有模式
                    return true;
                }

                // 如果supportedConsumeModes是列表
                if (supportedModesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> supportedModes = (List<String>) supportedModesObj;
                    return supportedModes.contains(consumeMode);
                }

                // 如果supportedConsumeModes是字符串（逗号分隔）
                if (supportedModesObj instanceof String) {
                    String supportedModesStr = (String) supportedModesObj;
                    String[] modes = supportedModesStr.split(",");
                    for (String mode : modes) {
                        if (mode.trim().equalsIgnoreCase(consumeMode)) {
                            return true;
                        }
                    }
                    return false;
                }

                // 其他类型，默认支持
                return true;
            } catch (Exception e) {
                log.warn("[设备管理] 解析扩展属性失败，使用默认配置，deviceId={}, extendedAttributes={}",
                        deviceId, extendedAttributes, e);
                return true; // 解析失败时默认支持所有模式
            }

        } catch (Exception e) {
            log.error("[设备管理] 验证消费模式支持失败，deviceId={}, consumeMode={}", deviceId, consumeMode, e);
            return false;
        }
    }

    /**
     * 根据设备ID获取消费设备信息
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    @Override
    public DeviceEntity getConsumeDeviceById(Long deviceId) {
        log.debug("[设备管理] 根据设备ID获取消费设备信息，deviceId={}", deviceId);
        try {
            // 通过网关调用公共服务获取设备信息
            net.lab1024.sa.common.dto.ResponseDTO<DeviceEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/device/" + deviceId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    DeviceEntity.class);
            if (response != null && response.isSuccess()) {
                return response.getData();
            }
            return null;
        } catch (Exception e) {
            log.error("[设备管理] 获取消费设备信息失败，deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 获取消费设备列表
     *
     * @param areaId 区域ID
     * @param status 设备状态（可选）
     * @return 设备列表
     */
    @Override
    public List<DeviceEntity> getConsumeDevices(String areaId, Integer status) {
        log.debug("[设备管理] 获取消费设备列表，areaId={}, status={}", areaId, status);
        try {
            // 通过网关调用公共服务获取设备列表
            String url = "/api/v1/device/list?areaId=" + areaId;
            if (status != null) {
                url += "&status=" + status;
            }

            // 通过网关调用公共服务获取设备列表
            // 注意：这里需要根据实际的GatewayServiceClient方法签名调整
            net.lab1024.sa.common.dto.ResponseDTO<?> response = gatewayServiceClient.callCommonService(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    Object.class);
            if (response != null && response.isSuccess() && response.getData() != null) {
                Object data = response.getData();
                if (data instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> dataList = (List<Object>) data;
                    List<DeviceEntity> devices = new java.util.ArrayList<>();
                    for (Object item : dataList) {
                        if (item instanceof DeviceEntity) {
                            devices.add((DeviceEntity) item);
                        } else if (item instanceof java.util.Map) {
                            // 如果返回的是Map，使用JsonUtil转换为DeviceEntity
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> itemMap = (java.util.Map<String, Object>) item;
                            try {
                                // 使用ObjectMapper进行类型安全的转换（优先使用注入的ObjectMapper Bean）
                                String json = objectMapper.writeValueAsString(itemMap);
                                DeviceEntity device = objectMapper.readValue(json, DeviceEntity.class);
                                if (device != null) {
                                    devices.add(device);
                                }
                            } catch (Exception e) {
                                log.warn("[设备管理] Map转DeviceEntity失败，跳过该项", e);
                            }
                        }
                    }
                    return devices;
                }
            }
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            log.error("[设备管理] 获取消费设备列表失败，areaId={}, status={}", areaId, status, e);
            return new ArrayList<>();
        }
    }
}
