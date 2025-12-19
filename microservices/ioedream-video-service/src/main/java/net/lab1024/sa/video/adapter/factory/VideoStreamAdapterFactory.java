package net.lab1024.sa.video.adapter.factory;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.adapter.IVideoStreamAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 视频流适配器工厂
 * <p>
 * 统一管理所有视频流适配器实现
 * 严格遵循CLAUDE.md规范：
 * - 使用工厂模式实现
 * - 支持动态加载适配器
 * - 支持适配器优先级排序
 * - 支持适配器热插拔
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class VideoStreamAdapterFactory {

    /**
     * 适配器注册表
     * Key: 厂商名称, Value: 适配器实例
     */
    private final Map<String, IVideoStreamAdapter> adapterRegistry = new ConcurrentHashMap<>();

    /**
     * 适配器列表（按优先级排序）
     */
    private final List<IVideoStreamAdapter> sortedAdapters = new ArrayList<>();

    private final ApplicationContext applicationContext;

    /**
     * 构造函数注入ApplicationContext
     * <p>
     * 严格遵循CLAUDE.md规范：
     * - 使用构造函数注入（Spring 4.3+自动识别，无需@Autowired注解）
     * - 构造函数注入是符合规范的依赖注入方式
     * - 无需使用@Resource或@Autowired注解
     * </p>
     */
    public VideoStreamAdapterFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化工厂
     * <p>
     * 自动扫描并注册所有IVideoStreamAdapter实现
     * </p>
     */
    @PostConstruct
    public void initialize() {
        log.info("[视频流适配器工厂] 开始初始化");

        // 从Spring容器中获取所有适配器实现
        Map<String, IVideoStreamAdapter> adapters = applicationContext.getBeansOfType(IVideoStreamAdapter.class);

        // 注册所有适配器
        adapters.values().forEach(this::registerAdapter);

        // 按优先级排序
        sortedAdapters.sort(Comparator.comparing(IVideoStreamAdapter::getPriority).reversed());

        log.info("[视频流适配器工厂] 初始化完成, 注册适配器数量: {}", adapterRegistry.size());
    }

    /**
     * 注册适配器
     *
     * @param adapter 视频流适配器
     */
    public void registerAdapter(IVideoStreamAdapter adapter) {
        if (adapter == null) {
            log.warn("[视频流适配器工厂] 尝试注册空的适配器");
            return;
        }

        String vendorName = adapter.getVendorName();
        log.info("[视频流适配器工厂] 注册视频流适配器: {}", vendorName);

        adapterRegistry.put(vendorName.toUpperCase(), adapter);
    }

    /**
     * 根据厂商名称获取适配器
     *
     * @param vendorName 厂商名称（如HIKVISION、DAHUA）
     * @return 视频流适配器，如果不存在返回null
     */
    public IVideoStreamAdapter getAdapter(String vendorName) {
        if (vendorName == null || vendorName.trim().isEmpty()) {
            return null;
        }

        IVideoStreamAdapter adapter = adapterRegistry.get(vendorName.toUpperCase());
        if (adapter == null) {
            log.warn("[视频流适配器工厂] 未找到厂商对应的适配器: {}", vendorName);
        }

        return adapter;
    }

    /**
     * 获取所有已注册的适配器
     *
     * @return 适配器列表（按优先级排序）
     */
    public List<IVideoStreamAdapter> getAllAdapters() {
        return new ArrayList<>(sortedAdapters);
    }

    /**
     * 获取支持的厂商列表
     *
     * @return 厂商名称列表
     */
    public List<String> getSupportedVendors() {
        return new ArrayList<>(adapterRegistry.keySet());
    }
}
