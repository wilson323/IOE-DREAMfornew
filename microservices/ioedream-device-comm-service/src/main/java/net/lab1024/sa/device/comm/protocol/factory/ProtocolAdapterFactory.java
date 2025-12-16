package net.lab1024.sa.device.comm.protocol.factory;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.ProtocolAdapter;
import net.lab1024.sa.device.comm.protocol.entropy.AccessEntropyV48Adapter;
import net.lab1024.sa.device.comm.protocol.zkteco.ConsumeZktecoV10Adapter;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议适配器工厂
 * <p>
 * 负责创建、管理和获取不同厂商的协议适配器实例
 * 支持协议适配器的动态注册和查找
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class ProtocolAdapterFactory {

    /**
     * 协议适配器注册表
     * Key: 协议类型, Value: 协议适配器实例
     */
    private final Map<String, ProtocolAdapter> adapterRegistry = new ConcurrentHashMap<>();

    /**
     * 设备型号到协议类型的映射
     * Key: 设备型号, Value: 协议类型
     */
    private final Map<String, String> deviceModelToProtocolMap = new ConcurrentHashMap<>();

    /**
     * 设备SN到协议类型的缓存
     * Key: 设备SN, Value: 协议类型
     */
    private final Map<String, String> deviceSnToProtocolCache = new ConcurrentHashMap<>();

    @Resource
    private AccessEntropyV48Adapter accessEntropyV48Adapter;

    @Resource
    private ConsumeZktecoV10Adapter consumeZktecoV10Adapter;

    /**
     * 初始化协议适配器工厂
     */
    public void initialize() {
        log.info("[协议适配器工厂] 开始初始化");

        // 注册熵基科技门禁协议V4.8适配器
        registerAdapter(accessEntropyV48Adapter);

        // 注册中控智慧消费协议V1.0适配器
        registerAdapter(consumeZktecoV10Adapter);

        log.info("[协议适配器工厂] 初始化完成, 注册适配器数量: {}", adapterRegistry.size());
    }

    /**
     * 注册协议适配器
     *
     * @param adapter 协议适配器
     */
    public void registerAdapter(ProtocolAdapter adapter) {
        if (adapter == null) {
            log.warn("[协议适配器工厂] 尝试注册空的适配器");
            return;
        }

        String protocolType = adapter.getProtocolType();
        String manufacturer = adapter.getManufacturer();
        String version = adapter.getVersion();

        log.info("[协议适配器工厂] 注册协议适配器: {} - {} {}", protocolType, manufacturer, version);

        // 注册到适配器表
        adapterRegistry.put(protocolType, adapter);

        // 注册设备型号映射
        String[] supportedModels = adapter.getSupportedDeviceModels();
        if (supportedModels != null) {
            for (String model : supportedModels) {
                if (model != null && !model.trim().isEmpty()) {
                    deviceModelToProtocolMap.put(model.toUpperCase(), protocolType);
                    log.debug("[协议适配器工厂] 注册设备型号映射: {} -> {}", model, protocolType);
                }
            }
        }

        log.info("[协议适配器工厂] 协议适配器注册成功: {} (支持设备型号: {})",
            protocolType, supportedModels != null ? supportedModels.length : 0);
    }

    /**
     * 根据协议类型获取适配器
     *
     * @param protocolType 协议类型
     * @return 协议适配器，如果不存在返回null
     */
    public ProtocolAdapter getAdapter(String protocolType) {
        if (protocolType == null || protocolType.trim().isEmpty()) {
            return null;
        }

        ProtocolAdapter adapter = adapterRegistry.get(protocolType);
        if (adapter == null) {
            log.warn("[协议适配器工厂] 未找到协议类型对应的适配器: {}", protocolType);
        }

        return adapter;
    }

    /**
     * 根据设备型号获取适配器
     *
     * @param deviceModel 设备型号
     * @return 协议适配器，如果不存在返回null
     */
    public ProtocolAdapter getAdapterByDeviceModel(String deviceModel) {
        if (deviceModel == null || deviceModel.trim().isEmpty()) {
            return null;
        }

        String protocolType = deviceModelToProtocolMap.get(deviceModel.toUpperCase());
        if (protocolType == null) {
            log.warn("[协议适配器工厂] 未找到设备型号对应的协议类型: {}", deviceModel);
            return null;
        }

        return getAdapter(protocolType);
    }

    /**
     * 根据设备SN获取适配器
     * 先从缓存查找，如果缓存中没有，则尝试从数据库查询
     *
     * @param deviceSn 设备SN
     * @return 协议适配器，如果不存在返回null
     */
    public ProtocolAdapter getAdapterByDeviceSn(String deviceSn) {
        if (deviceSn == null || deviceSn.trim().isEmpty()) {
            return null;
        }

        // 先从缓存查找
        String protocolType = deviceSnToProtocolCache.get(deviceSn);
        if (protocolType != null) {
            return getAdapter(protocolType);
        }

        // 缓存中没有，从数据库查询
        // TODO: 从数据库查询设备信息和对应的协议类型
        // 查询到后更新缓存
        // deviceSnToProtocolCache.put(deviceSn, protocolType);

        log.warn("[协议适配器工厂] 未找到设备SN对应的协议类型: {}", deviceSn);
        return null;
    }

    /**
     * 根据设备ID获取适配器
     *
     * @param deviceId 设备ID
     * @return 协议适配器，如果不存在返回null
     */
    public ProtocolAdapter getAdapterByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        // TODO: 从数据库查询设备信息和对应的协议类型
        // 1. 查询设备信息（包含设备型号和SN）
        // 2. 根据设备型号或SN获取对应的协议适配器

        log.warn("[协议适配器工厂] 未找到设备ID对应的协议适配器: {}", deviceId);
        return null;
    }

    /**
     * 获取所有注册的协议适配器
     *
     * @return 协议适配器映射
     */
    public Map<String, ProtocolAdapter> getAllAdapters() {
        return new HashMap<>(adapterRegistry);
    }

    /**
     * 获取所有支持的协议类型
     *
     * @return 协议类型列表
     */
    public java.util.List<String> getSupportedProtocolTypes() {
        return new java.util.ArrayList<>(adapterRegistry.keySet());
    }

    /**
     * 获取所有支持的设备型号
     *
     * @return 设备型号列表
     */
    public java.util.List<String> getSupportedDeviceModels() {
        return new java.util.ArrayList<>(deviceModelToProtocolMap.keySet());
    }

    /**
     * 检查协议类型是否支持
     *
     * @param protocolType 协议类型
     * @return 是否支持
     */
    public boolean isProtocolTypeSupported(String protocolType) {
        return adapterRegistry.containsKey(protocolType);
    }

    /**
     * 检查设备型号是否支持
     *
     * @param deviceModel 设备型号
     * @return 是否支持
     */
    public boolean isDeviceModelSupported(String deviceModel) {
        if (deviceModel == null || deviceModel.trim().isEmpty()) {
            return false;
        }
        return deviceModelToProtocolMap.containsKey(deviceModel.toUpperCase());
    }

    /**
     * 注销协议适配器
     *
     * @param protocolType 协议类型
     */
    public void unregisterAdapter(String protocolType) {
        if (protocolType == null || protocolType.trim().isEmpty()) {
            return;
        }

        ProtocolAdapter adapter = adapterRegistry.remove(protocolType);
        if (adapter != null) {
            log.info("[协议适配器工厂] 注销协议适配器: {}", protocolType);

            // 移除设备型号映射
            String[] supportedModels = adapter.getSupportedDeviceModels();
            if (supportedModels != null) {
                for (String model : supportedModels) {
                    if (model != null && !model.trim().isEmpty()) {
                        deviceModelToProtocolMap.remove(model.toUpperCase());
                    }
                }
            }

            // 清理设备SN缓存中该协议类型的条目
            deviceSnToProtocolCache.entrySet().removeIf(
                entry -> protocolType.equals(entry.getValue())
            );
        }
    }

    /**
     * 更新设备SN到协议类型的缓存
     *
     * @param deviceSn 设备SN
     * @param protocolType 协议类型
     */
    public void updateDeviceSnProtocolCache(String deviceSn, String protocolType) {
        if (deviceSn != null && !deviceSn.trim().isEmpty() &&
            protocolType != null && !protocolType.trim().isEmpty()) {

            deviceSnToProtocolCache.put(deviceSn, protocolType);
            log.debug("[协议适配器工厂] 更新设备SN协议缓存: {} -> {}", deviceSn, protocolType);
        }
    }

    /**
     * 清理设备SN缓存
     *
     * @param deviceSn 设备SN
     */
    public void clearDeviceSnCache(String deviceSn) {
        if (deviceSn != null && !deviceSn.trim().isEmpty()) {
            deviceSnToProtocolCache.remove(deviceSn);
            log.debug("[协议适配器工厂] 清理设备SN缓存: {}", deviceSn);
        }
    }

    /**
     * 清理所有设备SN缓存
     */
    public void clearAllDeviceSnCache() {
        deviceSnToProtocolCache.clear();
        log.info("[协议适配器工厂] 清理所有设备SN缓存");
    }

    /**
     * 获取工厂统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getFactoryStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("registeredAdapterCount", adapterRegistry.size());
        stats.put("supportedDeviceModelCount", deviceModelToProtocolMap.size());
        stats.put("cachedDeviceSnCount", deviceSnToProtocolCache.size());
        stats.put("supportedProtocolTypes", getSupportedProtocolTypes());
        stats.put("supportedDeviceModels", getSupportedDeviceModels());

        // 适配器详细统计
        Map<String, Object> adapterStats = new HashMap<>();
        for (Map.Entry<String, ProtocolAdapter> entry : adapterRegistry.entrySet()) {
            String protocolType = entry.getKey();
            ProtocolAdapter adapter = entry.getValue();

            Map<String, Object> adapterInfo = new HashMap<>();
            adapterInfo.put("manufacturer", adapter.getManufacturer());
            adapterInfo.put("version", adapter.getVersion());
            adapterInfo.put("supportedModelCount",
                adapter.getSupportedDeviceModels() != null ? adapter.getSupportedDeviceModels().length : 0);
            adapterInfo.put("status", adapter.getAdapterStatus());

            adapterStats.put(protocolType, adapterInfo);
        }
        stats.put("adapterDetails", adapterStats);

        return stats;
    }

    /**
     * 重新加载适配器
     * 重新初始化所有已注册的适配器
     */
    public void reloadAdapters() {
        log.info("[协议适配器工厂] 开始重新加载适配器");

        // 销毁现有适配器
        for (ProtocolAdapter adapter : adapterRegistry.values()) {
            try {
                adapter.destroy();
            } catch (Exception e) {
                log.error("[协议适配器工厂] 销毁适配器异常", e);
            }
        }

        // 清空注册表
        adapterRegistry.clear();
        deviceModelToProtocolMap.clear();
        deviceSnToProtocolCache.clear();

        // 重新初始化
        initialize();

        log.info("[协议适配器工厂] 适配器重新加载完成");
    }

    /**
     * 检查适配器健康状态
     *
     * @return 健康状态报告
     */
    public Map<String, Object> checkAdapterHealth() {
        Map<String, Object> healthReport = new HashMap<>();
        healthReport.put("factoryStatus", "HEALTHY");
        healthReport.put("adapterCount", adapterRegistry.size());

        Map<String, String> adapterHealth = new HashMap<>();
        for (Map.Entry<String, ProtocolAdapter> entry : adapterRegistry.entrySet()) {
            String protocolType = entry.getKey();
            ProtocolAdapter adapter = entry.getValue();

            try {
                String status = adapter.getAdapterStatus();
                adapterHealth.put(protocolType, status);

                // 如果适配器状态不是RUNNING，标记工厂为警告状态
                if (!"RUNNING".equals(status)) {
                    healthReport.put("factoryStatus", "WARNING");
                }

            } catch (Exception e) {
                adapterHealth.put(protocolType, "ERROR: " + e.getMessage());
                healthReport.put("factoryStatus", "ERROR");
                log.error("[协议适配器工厂] 检查适配器健康状态异常: {}", protocolType, e);
            }
        }

        healthReport.put("adapterStatus", adapterHealth);

        return healthReport;
    }
}