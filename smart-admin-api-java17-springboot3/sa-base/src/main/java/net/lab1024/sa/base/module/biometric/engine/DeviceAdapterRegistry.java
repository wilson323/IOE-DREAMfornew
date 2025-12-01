package net.lab1024.sa.base.module.biometric.engine;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.common.device.DeviceAdapterInterface;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备适配器注册表
 * <p>
 * 管理所有设备适配器的注册、查找和调用
 * 支持动态注册适配器和按设备类型查找适配器
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Component
public class DeviceAdapterRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    // 按设备类型存储适配器映射
    private final Map<String, DeviceAdapterInterface> deviceTypeAdapters = new ConcurrentHashMap<>();

    // 按适配器名称存储适配器映射
    private final Map<String, DeviceAdapterInterface> namedAdapters = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        log.info("初始化设备适配器注册表...");

        // 自动注册所有设备适配器Bean
        Map<String, DeviceAdapterInterface> adapterBeans =
            applicationContext.getBeansOfType(DeviceAdapterInterface.class);

        for (Map.Entry<String, DeviceAdapterInterface> entry : adapterBeans.entrySet()) {
            String beanName = entry.getKey();
            DeviceAdapterInterface adapter = entry.getValue();

            registerAdapter(beanName, adapter);
        }

        log.info("设备适配器注册表初始化完成，共注册 {} 个适配器", adapterBeans.size());
    }

    /**
     * 注册设备适配器
     *
     * @param name 适配器名称
     * @param adapter 适配器实例
     */
    public void registerAdapter(String name, DeviceAdapterInterface adapter) {
        if (name == null || adapter == null) {
            log.warn("适配器名称或实例为空，跳过注册");
            return;
        }

        try {
            // 按名称注册
            namedAdapters.put(name, adapter);

            // 按支持的设备类型注册
            String supportedDeviceType = adapter.getSupportedDeviceType();
            if (supportedDeviceType != null && !supportedDeviceType.trim().isEmpty()) {
                deviceTypeAdapters.put(supportedDeviceType.toUpperCase(), adapter);
                log.debug("注册设备适配器: name={}, type={}", name, supportedDeviceType);
            } else {
                log.warn("适配器未指定支持的设备类型: name={}", name);
            }

        } catch (Exception e) {
            log.error("注册设备适配器异常: name={}", name, e);
        }
    }

    /**
     * 根据设备获取适配器
     *
     * @param device 设备实体
     * @return 设备适配器，如果不存在则返回null
     */
    public DeviceAdapterInterface getAdapter(SmartDeviceEntity device) {
        if (device == null) {
            return null;
        }

        // 首先按设备类型查找
        String deviceType = device.getDeviceType();
        if (deviceType != null && !deviceType.trim().isEmpty()) {
            DeviceAdapterInterface adapter = deviceTypeAdapters.get(deviceType.toUpperCase());
            if (adapter != null && adapter.supportsDevice(device)) {
                return adapter;
            }
        }

        // 如果按类型找不到，遍历所有适配器查找支持的适配器
        for (DeviceAdapterInterface adapter : namedAdapters.values()) {
            if (adapter.supportsDevice(device)) {
                return adapter;
            }
        }

        log.warn("未找到支持的设备适配器: deviceId={}, deviceType={}, manufacturer={}",
                device.getDeviceId(), device.getDeviceType(), device.getManufacturer());
        return null;
    }

    /**
     * 根据设备类型获取适配器
     *
     * @param deviceType 设备类型
     * @return 设备适配器，如果不存在则返回null
     */
    public DeviceAdapterInterface getAdapterByType(String deviceType) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            return null;
        }

        return deviceTypeAdapters.get(deviceType.toUpperCase());
    }

    /**
     * 根据名称获取适配器
     *
     * @param name 适配器名称
     * @return 设备适配器，如果不存在则返回null
     */
    public DeviceAdapterInterface getAdapterByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        return namedAdapters.get(name);
    }

    /**
     * 获取所有已注册的适配器
     *
     * @return 适配器列表
     */
    public List<DeviceAdapterInterface> getAllAdapters() {
        return new ArrayList<>(namedAdapters.values());
    }

    /**
     * 获取所有支持的设备类型
     *
     * @return 设备类型列表
     */
    public Set<String> getSupportedDeviceTypes() {
        return new HashSet<>(deviceTypeAdapters.keySet());
    }

    /**
     * 检查设备类型是否被支持
     *
     * @param deviceType 设备类型
     * @return 是否支持
     */
    public boolean isDeviceTypeSupported(String deviceType) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            return false;
        }

        return deviceTypeAdapters.containsKey(deviceType.toUpperCase());
    }

    /**
     * 获取支持指定设备的所有适配器
     *
     * @param device 设备实体
     * @return 支持的适配器列表
     */
    public List<DeviceAdapterInterface> getAdaptersForDevice(SmartDeviceEntity device) {
        if (device == null) {
            return new ArrayList<>();
        }

        List<DeviceAdapterInterface> supportedAdapters = new ArrayList<>();

        for (DeviceAdapterInterface adapter : namedAdapters.values()) {
            if (adapter.supportsDevice(device)) {
                supportedAdapters.add(adapter);
            }
        }

        return supportedAdapters;
    }

    /**
     * 移除适配器
     *
     * @param name 适配器名称
     * @return 是否成功移除
     */
    public boolean removeAdapter(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        DeviceAdapterInterface adapter = namedAdapters.remove(name);
        if (adapter != null) {
            // 从设备类型映射中移除
            String supportedDeviceType = adapter.getSupportedDeviceType();
            if (supportedDeviceType != null && !supportedDeviceType.trim().isEmpty()) {
                deviceTypeAdapters.remove(supportedDeviceType.toUpperCase());
            }

            log.info("移除设备适配器: name={}, type={}", name, supportedDeviceType);
            return true;
        }

        return false;
    }

    /**
     * 清空所有适配器
     */
    public void clear() {
        log.info("清空所有设备适配器");
        namedAdapters.clear();
        deviceTypeAdapters.clear();
    }

    /**
     * 获取注册统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getRegistryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAdapters", namedAdapters.size());
        stats.put("deviceTypeCount", deviceTypeAdapters.size());
        stats.put("supportedDeviceTypes", new ArrayList<>(deviceTypeAdapters.keySet()));

        Map<String, String> adapterDetails = new HashMap<>();
        for (Map.Entry<String, DeviceAdapterInterface> entry : namedAdapters.entrySet()) {
            DeviceAdapterInterface adapter = entry.getValue();
            adapterDetails.put(entry.getKey(), adapter.getSupportedDeviceType());
        }
        stats.put("adapterDetails", adapterDetails);

        return stats;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}