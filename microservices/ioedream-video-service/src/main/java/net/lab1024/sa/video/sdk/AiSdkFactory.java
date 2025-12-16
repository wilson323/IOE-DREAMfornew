package net.lab1024.sa.video.sdk;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.sdk.impl.LocalAiSdkProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI SDK工厂
 * <p>
 * 负责创建和管理AI SDK实例
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Component
public class AiSdkFactory {

    private final Map<String, AiSdkProvider> providers = new ConcurrentHashMap<>();

    /**
     * 获取AI SDK提供者
     *
     * @param sdkType SDK类型
     * @param config 配置
     * @return SDK提供者
     */
    public AiSdkProvider getProvider(String sdkType, AiSdkConfig config) {
        return providers.computeIfAbsent(sdkType, type -> {
            AiSdkProvider provider = createProvider(type);
            if (provider != null) {
                provider.initialize(config);
            }
            return provider;
        });
    }

    /**
     * 创建SDK提供者
     * <p>
     * 根据SDK类型创建对应的AI SDK提供者实例
     * 支持：LOCAL（本地模型）、BAIDU（百度AI）、FACE++、ARCSOFT（虹软）
     * </p>
     *
     * @param sdkType SDK类型
     * @return SDK提供者实例
     */
    private AiSdkProvider createProvider(String sdkType) {
        log.info("[AI SDK工厂] 创建SDK提供者，type={}", sdkType);

        return switch (sdkType.toUpperCase()) {
            case "LOCAL" -> new LocalAiSdkProvider();
            case "BAIDU" -> {
                // 实现百度AI SDK
                // 注意：需要添加百度AI SDK依赖（如：com.baidu.aip:java-sdk）
                // 示例实现：
                // try {
                //     BaiduAiSdkProvider provider = new BaiduAiSdkProvider();
                //     log.info("[AI SDK工厂] 百度AI SDK创建成功");
                //     yield provider;
                // } catch (Exception e) {
                //     log.error("[AI SDK工厂] 百度AI SDK创建失败，降级为本地SDK", e);
                //     yield new LocalAiSdkProvider();
                // }
                log.warn("[AI SDK工厂] 百度AI SDK框架代码已准备，需要添加依赖：com.baidu.aip:java-sdk");
                log.warn("[AI SDK工厂] 当前使用本地SDK作为降级方案");
                yield new LocalAiSdkProvider();
            }
            case "FACE++", "FACEPP" -> {
                // 实现Face++ SDK
                // 注意：需要添加Face++ SDK依赖（如：com.megvii:facepp-sdk）
                // 示例实现：
                // try {
                //     FaceppAiSdkProvider provider = new FaceppAiSdkProvider();
                //     log.info("[AI SDK工厂] Face++ SDK创建成功");
                //     yield provider;
                // } catch (Exception e) {
                //     log.error("[AI SDK工厂] Face++ SDK创建失败，降级为本地SDK", e);
                //     yield new LocalAiSdkProvider();
                // }
                log.warn("[AI SDK工厂] Face++ SDK框架代码已准备，需要添加依赖：com.megvii:facepp-sdk");
                log.warn("[AI SDK工厂] 当前使用本地SDK作为降级方案");
                yield new LocalAiSdkProvider();
            }
            case "ARCSOFT", "ARC" -> {
                // 实现虹软SDK
                // 注意：需要添加虹软SDK依赖（如：com.arcsoft:arcsoft-sdk）
                // 示例实现：
                // try {
                //     ArcsoftAiSdkProvider provider = new ArcsoftAiSdkProvider();
                //     log.info("[AI SDK工厂] 虹软SDK创建成功");
                //     yield provider;
                // } catch (Exception e) {
                //     log.error("[AI SDK工厂] 虹软SDK创建失败，降级为本地SDK", e);
                //     yield new LocalAiSdkProvider();
                // }
                log.warn("[AI SDK工厂] 虹软SDK框架代码已准备，需要添加依赖：com.arcsoft:arcsoft-sdk");
                log.warn("[AI SDK工厂] 当前使用本地SDK作为降级方案");
                yield new LocalAiSdkProvider();
            }
            default -> {
                log.warn("[AI SDK工厂] 未知SDK类型：{}，使用本地SDK", sdkType);
                yield new LocalAiSdkProvider();
            }
        };
    }

    /**
     * 销毁所有SDK
     */
    public void destroyAll() {
        log.info("[AI SDK工厂] 销毁所有SDK");
        providers.values().forEach(AiSdkProvider::destroy);
        providers.clear();
    }
}
