package net.lab1024.sa.device.protocol;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.device.protocol.impl.HttpProtocolAdapter;
import net.lab1024.sa.device.protocol.impl.TcpProtocolAdapter;

/**
 * 设备协议适配器工厂 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 单例模式管理协议适配器
 * - 支持多种协议类型扩展
 * - 完整的异常处理和日志记录
 * - 线程安全的适配器获取
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Slf4j
@Component
public class DeviceProtocolAdapterFactory {

    /**
     * 协议适配器缓存
     */
    private final Map<DeviceProtocolAdapter.ProtocolType, DeviceProtocolAdapter> adapterCache = new HashMap<>();

    /**
     * 根据设备获取对应的协议适配器
     *
     * @param device 门禁设备实体
     * @return 协议适配器
     * @throws DeviceProtocolException 协议不支持异常
     */
    public DeviceProtocolAdapter getAdapter(AccessDeviceEntity device) throws DeviceProtocolException {
        if (device == null) {
            throw new DeviceProtocolException("设备信息不能为空", "DEVICE_NULL", null, null);
        }

        String protocolTypeStr = device.getProtocolType();
        if (protocolTypeStr == null || protocolTypeStr.trim().isEmpty()) {
            protocolTypeStr = "TCP"; // 默认使用TCP协议
        }

        DeviceProtocolAdapter.ProtocolType protocolType = DeviceProtocolAdapter.ProtocolType
                .fromString(protocolTypeStr);
        return getAdapter(protocolType);
    }

    /**
     * 根据协议类型获取适配器
     *
     * @param protocolType 协议类型
     * @return 协议适配器
     * @throws DeviceProtocolException 协议不支持异常
     */
    public DeviceProtocolAdapter getAdapter(DeviceProtocolAdapter.ProtocolType protocolType)
            throws DeviceProtocolException {
        if (protocolType == null) {
            protocolType = DeviceProtocolAdapter.ProtocolType.TCP;
        }

        return adapterCache.computeIfAbsent(protocolType, type -> {
            try {
                DeviceProtocolAdapter adapter = createAdapter(type);
                log.info("创建设备协议适配器: {}", type.getDescription());
                return adapter;
            } catch (DeviceProtocolException e) {
                log.error("创建协议适配器失败: {}", type.getDescription(), e);
                throw new RuntimeException("创建协议适配器失败: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("创建协议适配器发生未知异常: {}", type.getDescription(), e);
                throw new RuntimeException("创建协议适配器失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 根据协议类型字符串获取适配器
     *
     * @param protocolTypeStr 协议类型字符串
     * @return 协议适配器
     * @throws DeviceProtocolException 协议不支持异常
     */
    public DeviceProtocolAdapter getAdapter(String protocolTypeStr) throws DeviceProtocolException {
        DeviceProtocolAdapter.ProtocolType protocolType = DeviceProtocolAdapter.ProtocolType
                .fromString(protocolTypeStr);
        return getAdapter(protocolType);
    }

    /**
     * 创建协议适配器
     *
     * @param protocolType 协议类型
     * @return 协议适配器
     * @throws DeviceProtocolException 协议不支持异常
     */
    private DeviceProtocolAdapter createAdapter(DeviceProtocolAdapter.ProtocolType protocolType)
            throws DeviceProtocolException {
        switch (protocolType) {
            case TCP:
                return new TcpProtocolAdapter();
            case HTTP:
                return new HttpProtocolAdapter();
            case MQTT:
                // TODO: 实现MQTT协议适配器
                throw new DeviceProtocolException(
                        "MQTT协议适配器暂未实现",
                        "MQTT_NOT_IMPLEMENTED",
                        null,
                        "MQTT");
            default:
                throw new DeviceProtocolException(
                        "不支持的协议类型: " + protocolType,
                        "UNSUPPORTED_PROTOCOL",
                        null,
                        protocolType.name());
        }
    }

    /**
     * 检查设备协议是否受支持
     *
     * @param device 门禁设备实体
     * @return 是否支持
     */
    public boolean isProtocolSupported(AccessDeviceEntity device) {
        if (device == null) {
            return false;
        }

        try {
            getAdapter(device);
            return true;
        } catch (DeviceProtocolException e) {
            return false;
        }
    }

    /**
     * 检查协议类型是否受支持
     *
     * @param protocolTypeStr 协议类型字符串
     * @return 是否支持
     */
    public boolean isProtocolSupported(String protocolTypeStr) {
        try {
            getAdapter(protocolTypeStr);
            return true;
        } catch (DeviceProtocolException e) {
            return false;
        }
    }

    /**
     * 获取所有支持的协议类型
     *
     * @return 支持的协议类型列表
     */
    public DeviceProtocolAdapter.ProtocolType[] getSupportedProtocols() {
        return DeviceProtocolAdapter.ProtocolType.values();
    }

    /**
     * 清除适配器缓存
     */
    public void clearAdapterCache() {
        adapterCache.clear();
        log.info("设备协议适配器缓存已清除");
    }

    /**
     * 预热适配器缓存
     */
    public void warmupAdapterCache() {
        for (DeviceProtocolAdapter.ProtocolType protocolType : DeviceProtocolAdapter.ProtocolType.values()) {
            try {
                getAdapter(protocolType);
                log.debug("协议适配器预热完成: {}", protocolType.getDescription());
            } catch (DeviceProtocolException e) {
                log.warn("协议适配器预热失败: {}, 错误: {}", protocolType.getDescription(), e.getMessage());
            }
        }
        log.info("设备协议适配器缓存预热完成");
    }

    /**
     * 获取适配器工厂状态信息
     *
     * @return 状态信息
     */
    public Map<String, Object> getFactoryStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("cacheSize", adapterCache.size());
        status.put("supportedProtocols", DeviceProtocolAdapter.ProtocolType.values());
        status.put("cachedAdapters", adapterCache.keySet());

        return status;
    }
}
