package net.lab1024.sa.device.comm.protocol.factory;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.ProtocolAdapter;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 设备适配器工厂
 * <p>
 * 作为ProtocolAdapterFactory的别名，提供统一的设备适配器工厂接口
 * 严格遵循CLAUDE.md规范：
 * - 使用工厂模式实现
 * - 委托给ProtocolAdapterFactory处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class DeviceAdapterFactory {

    @Resource
    private ProtocolAdapterFactory protocolAdapterFactory;

    /**
     * 根据协议类型获取适配器
     *
     * @param protocolType 协议类型
     * @return 协议适配器，如果不存在返回null
     */
    public ProtocolAdapter getAdapter(String protocolType) {
        return protocolAdapterFactory.getAdapter(protocolType);
    }

    /**
     * 根据设备型号获取适配器
     *
     * @param deviceModel 设备型号
     * @return 协议适配器，如果不存在返回null
     */
    public ProtocolAdapter getAdapterByDeviceModel(String deviceModel) {
        return protocolAdapterFactory.getAdapterByDeviceModel(deviceModel);
    }

    /**
     * 根据设备SN获取适配器
     *
     * @param deviceSn 设备SN
     * @return 协议适配器，如果不存在返回null
     */
    public ProtocolAdapter getAdapterByDeviceSn(String deviceSn) {
        return protocolAdapterFactory.getAdapterByDeviceSn(deviceSn);
    }

    /**
     * 注册适配器
     *
     * @param adapter 协议适配器
     */
    public void registerAdapter(ProtocolAdapter adapter) {
        protocolAdapterFactory.registerAdapter(adapter);
    }

    /**
     * 获取所有已注册的适配器
     *
     * @return 适配器映射表
     */
    public java.util.Map<String, ProtocolAdapter> getAllAdapters() {
        return protocolAdapterFactory.getAllAdapters();
    }

    /**
     * 获取支持的协议类型列表
     *
     * @return 协议类型列表
     */
    public java.util.List<String> getSupportedProtocols() {
        return protocolAdapterFactory.getSupportedProtocolTypes();
    }
}
