package net.lab1024.sa.access.adapter;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.adapter.protocol.AccessProtocolInterface;
import net.lab1024.sa.access.adapter.protocol.impl.DahuaAdapter;
import net.lab1024.sa.access.adapter.protocol.impl.HikvisionAdapter;
import net.lab1024.sa.access.adapter.protocol.impl.HttpProtocolAdapter;
import net.lab1024.sa.access.adapter.protocol.impl.ZKTecoAdapter;
import net.lab1024.sa.access.domain.entity.AccessDeviceEntity;

/**
 * 设备协议适配器工厂
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - 策略模式实现多协议切换
 * - 单例模式管理适配器实例
 * - 工厂模式统一创建适配器
 * - 支持动态适配器注册
 * </p>
 *
 * 根据设备制造商和协议类型自动选择合适的协议适配器
 * 支持多种厂商设备：大华、海康威视、ZKTeco等
 * 支持多种通信协议：HTTP、TCP、SDK、GB28181等
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Slf4j
@Component
public class DeviceProtocolAdapterFactory {

    /**
     * 所有适配器实例
     */
    @Resource
    private List<AccessProtocolInterface> allAdapters;

    /**
     * Spring应用上下文
     */
    @Resource
    private ApplicationContext applicationContext;

    /**
     * 适配器缓存（设备类型 + 制造商 -> 适配器）
     * Key: "设备类型:制造商"，Value: 适配器实例
     */
    private final Map<String, AccessProtocolInterface> adapterCache = new ConcurrentHashMap<>();

    /**
     * 制造商到适配器映射缓存
     */
    private final Map<String, AccessProtocolInterface> manufacturerAdapterMap = new ConcurrentHashMap<>();

    /**
     * 支持的设备类型
     */
    private static final Set<String> SUPPORTED_DEVICE_TYPES = Set.of("ACCESS");

    /**
     * 初始化适配器映射
     */
    @PostConstruct
    public void initAdapterMappings() {
        log.info("初始化设备协议适配器工厂...");

        // 清空缓存
        adapterCache.clear();
        manufacturerAdapterMap.clear();

        // 按优先级排序适配器（特定厂商适配器优先）
        allAdapters.sort(Comparator.comparingInt(this::getAdapterPriority).reversed());

        // 注册所有适配器
        for (AccessProtocolInterface adapter : allAdapters) {
            registerAdapter(adapter);
        }

        log.info("设备协议适配器工厂初始化完成，注册适配器数量: {}", allAdapters.size());
    }

    /**
     * 注册适配器
     */
    private void registerAdapter(AccessProtocolInterface adapter) {
        try {
            // 获取适配器支持的制造商
            List<String> manufacturers = adapter.getSupportedManufacturers();

            for (String manufacturer : manufacturers) {
                manufacturerAdapterMap.put(manufacturer.toLowerCase(), adapter);
                log.debug("注册制造商适配器: {} -> {}", manufacturer, adapter.getProtocolName());
            }
        } catch (Exception e) {
            log.warn("注册适配器失败: {}, 错误: {}", adapter.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * 根据设备获取协议适配器
     * <p>
     * 根据设备的制造商和协议类型自动选择合适的适配器
     * 优先级：制造商专用适配器 > 通用HTTP适配器
     *
     * @param device 门禁设备实体，不能为null
     * @return 协议适配器，不会返回null
     * @throws IllegalArgumentException 如果设备为null或设备类型不支持
     */
    public AccessProtocolInterface getAdapter(AccessDeviceEntity device) {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }

        String deviceType = device.getAccessDeviceType() != null ? device.getAccessDeviceType().toString() : null;
        if (!SUPPORTED_DEVICE_TYPES.contains(deviceType)) {
            throw new IllegalArgumentException("不支持的设备类型: " + deviceType);
        }

        String manufacturer = device.getManufacturer();
        String protocolType = device.getProtocolType();

        // 构建缓存键：设备类型:制造商:协议类型
        String cacheKey = buildCacheKey(deviceType, manufacturer, protocolType);

        // 从缓存获取适配器
        return adapterCache.computeIfAbsent(cacheKey, k -> {
            return findOrCreateAdapter(device);
        });
    }

    /**
     * 查找或创建适配器
     */
    private AccessProtocolInterface findOrCreateAdapter(AccessDeviceEntity device) {
        String manufacturer = device.getManufacturer();
        String protocolType = device.getProtocolType();

        // 1. 首先尝试制造商专用适配器
        if (manufacturer != null && !manufacturer.trim().isEmpty()) {
            AccessProtocolInterface manufacturerAdapter = manufacturerAdapterMap.get(manufacturer.toLowerCase().trim());
            if (manufacturerAdapter != null && manufacturerAdapter.supportsDevice(device)) {
                log.debug("使用制造商专用适配器: {} -> {}", manufacturer, manufacturerAdapter.getProtocolName());
                return manufacturerAdapter;
            }
        }

        // 2. 尝试HTTP通用适配器（对于Generic制造商或空制造商）
        if (("Generic".equalsIgnoreCase(manufacturer) || manufacturer == null || manufacturer.trim().isEmpty())
                && ("HTTP".equalsIgnoreCase(protocolType) || "HTTPS".equalsIgnoreCase(protocolType))) {

            AccessProtocolInterface httpAdapter = allAdapters.stream()
                    .filter(adapter -> adapter instanceof HttpProtocolAdapter)
                    .findFirst()
                    .orElse(null);

            if (httpAdapter != null && httpAdapter.supportsDevice(device)) {
                log.debug("使用HTTP通用适配器: Generic -> {}", httpAdapter.getProtocolName());
                return httpAdapter;
            }
        }

        // 3. 查找第一个支持该设备的适配器
        Optional<AccessProtocolInterface> supportedAdapter = allAdapters.stream()
                .filter(adapter -> adapter.supportsDevice(device))
                .findFirst();

        if (supportedAdapter.isPresent()) {
            AccessProtocolInterface adapter = supportedAdapter.get();
            log.info("使用适配器: {} -> {}", manufacturer, adapter.getProtocolName());
            return adapter;
        }

        // 4. 没找到合适的适配器
        String errorInfo = String.format("未找到合适的适配器，设备信息: deviceType=%s, manufacturer=%s, protocolType=%s",
                device.getDeviceType(), manufacturer, protocolType);
        log.error(errorInfo);
        throw new IllegalArgumentException(errorInfo);
    }

    /**
     * 获取所有支持的制造商
     *
     * @return 制造商列表
     */
    public Set<String> getSupportedManufacturers() {
        return new HashSet<>(manufacturerAdapterMap.keySet());
    }

    /**
     * 获取所有支持的协议类型
     *
     * @return 协议类型列表
     */
    public Set<String> getSupportedProtocolTypes() {
        return allAdapters.stream()
                .map(AccessProtocolInterface::getProtocolName)
                .collect(Collectors.toSet());
    }

    /**
     * 获取所有适配器信息
     *
     * @return 适配器信息列表
     */
    public List<AdapterInfo> getAdapterInfos() {
        return allAdapters.stream()
                .map(this::createAdapterInfo)
                .collect(Collectors.toList());
    }

    /**
     * 检查设备是否支持
     *
     * @param device 设备实体
     * @return 是否支持
     */
    public boolean isDeviceSupported(AccessDeviceEntity device) {
        if (device == null) {
            return false;
        }

        String deviceType = device.getAccessDeviceType() != null ? device.getAccessDeviceType().toString() : null;
        if (!SUPPORTED_DEVICE_TYPES.contains(deviceType)) {
            return false;
        }

        return allAdapters.stream().anyMatch(adapter -> adapter.supportsDevice(device));
    }

    /**
     * 清除适配器缓存
     */
    public void clearCache() {
        adapterCache.clear();
        log.info("已清除适配器缓存");
    }

    /**
     * 重新初始化适配器映射
     */
    public void reinitialize() {
        log.info("重新初始化设备协议适配器工厂...");
        initAdapterMappings();
    }

    /**
     * 获取适配器优先级
     */
    private int getAdapterPriority(AccessProtocolInterface adapter) {
        // 特定厂商适配器优先级更高
        if (adapter instanceof DahuaAdapter ||
                adapter instanceof HikvisionAdapter ||
                adapter instanceof ZKTecoAdapter) {
            return 100;
        }
        // HTTP通用适配器优先级中等
        if (adapter instanceof HttpProtocolAdapter) {
            return 50;
        }
        // 其他适配器优先级较低
        return 10;
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String deviceType, String manufacturer, String protocolType) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(deviceType);
        if (manufacturer != null && !manufacturer.trim().isEmpty()) {
            keyBuilder.append(":").append(manufacturer.trim().toLowerCase());
        }
        if (protocolType != null && !protocolType.trim().isEmpty()) {
            keyBuilder.append(":").append(protocolType.trim().toUpperCase());
        }
        return keyBuilder.toString();
    }

    /**
     * 创建适配器信息
     */
    private AdapterInfo createAdapterInfo(AccessProtocolInterface adapter) {
        AdapterInfo info = new AdapterInfo();
        info.setProtocolName(adapter.getProtocolName());
        info.setProtocolVersion(adapter.getProtocolVersion());
        info.setSupportedManufacturers(adapter.getSupportedManufacturers());
        info.setAdapterClass(adapter.getClass().getSimpleName());
        info.setFeatures(adapter.getProtocolFeatures());
        return info;
    }

    /**
     * 适配器信息
     */
    public static class AdapterInfo {
        private String protocolName;
        private String protocolVersion;
        private List<String> supportedManufacturers;
        private String adapterClass;
        private List<String> features;

        // Getters and Setters
        public String getProtocolName() {
            return protocolName;
        }

        public void setProtocolName(String protocolName) {
            this.protocolName = protocolName;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public List<String> getSupportedManufacturers() {
            return supportedManufacturers;
        }

        public void setSupportedManufacturers(List<String> supportedManufacturers) {
            this.supportedManufacturers = supportedManufacturers;
        }

        public String getAdapterClass() {
            return adapterClass;
        }

        public void setAdapterClass(String adapterClass) {
            this.adapterClass = adapterClass;
        }

        public List<String> getFeatures() {
            return features;
        }

        public void setFeatures(List<String> features) {
            this.features = features;
        }

        @Override
        public String toString() {
            return String.format("AdapterInfo{protocolName='%s', manufacturer='%s', class='%s'}",
                    protocolName, supportedManufacturers, adapterClass);
        }
    }
}
