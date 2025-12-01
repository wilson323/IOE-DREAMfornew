package net.lab1024.sa.admin.module.smart.access.protocol;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.smart.access.protocol.impl.HttpProtocolAdapter;
import net.lab1024.sa.admin.module.smart.access.protocol.impl.TcpProtocolAdapter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备协议适配器工厂
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - 策略模式实现多协议切换
 * - 单例模式管理适配器实例
 * <p>
 * 根据设备协议类型自动选择合适的协议适配器
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@Slf4j
@Component
public class DeviceProtocolAdapterFactory {

    @Resource
    private TcpProtocolAdapter tcpProtocolAdapter;

    @Resource
    private HttpProtocolAdapter httpProtocolAdapter;

    /**
     * 适配器缓存（单例模式）
     */
    private final Map<DeviceProtocolAdapter.ProtocolType, DeviceProtocolAdapter> adapterCache = 
            new ConcurrentHashMap<>();

    /**
     * 根据设备获取协议适配器
     * <p>
     * 根据设备的protocol字段自动选择合适的适配器
     *
     * @param device 门禁设备实体
     * @return 协议适配器
     * @throws DeviceProtocolException 如果协议类型不支持
     */
    public DeviceProtocolAdapter getAdapter(AccessDeviceEntity device) throws DeviceProtocolException {
        if (device == null) {
            throw new DeviceProtocolException(
                    "设备实体为空",
                    null,
                    null,
                    "getAdapter"
            );
        }

        // 获取设备协议类型
        DeviceProtocolAdapter.ProtocolType protocolType = DeviceProtocolAdapter.ProtocolType.fromString(
                device.getProtocol()
        );

        // 从缓存获取适配器
        DeviceProtocolAdapter adapter = adapterCache.get(protocolType);
        if (adapter != null) {
            return adapter;
        }

        // 根据协议类型创建适配器
        switch (protocolType) {
            case TCP:
                adapter = tcpProtocolAdapter;
                break;
            case HTTP:
                adapter = httpProtocolAdapter;
                break;
            case MQTT:
                // TODO: 实现MQTT协议适配器
                throw new DeviceProtocolException(
                        "MQTT协议适配器尚未实现",
                        device.getAccessDeviceId(),
                        protocolType,
                        "getAdapter"
                );
            default:
                throw new DeviceProtocolException(
                        "不支持的协议类型: " + protocolType,
                        device.getAccessDeviceId(),
                        protocolType,
                        "getAdapter"
                );
        }

        // 缓存适配器
        adapterCache.put(protocolType, adapter);
        log.debug("获取协议适配器成功，deviceId: {}, protocolType: {}", 
                device.getAccessDeviceId(), protocolType);

        return adapter;
    }

    /**
     * 获取所有支持的协议类型
     *
     * @return 协议类型列表
     */
    public List<DeviceProtocolAdapter.ProtocolType> getSupportedProtocolTypes() {
        return List.of(
                DeviceProtocolAdapter.ProtocolType.TCP,
                DeviceProtocolAdapter.ProtocolType.HTTP
        );
    }

    /**
     * 检查协议类型是否支持
     *
     * @param protocolType 协议类型
     * @return 是否支持
     */
    public boolean isProtocolSupported(DeviceProtocolAdapter.ProtocolType protocolType) {
        return getSupportedProtocolTypes().contains(protocolType);
    }
}

