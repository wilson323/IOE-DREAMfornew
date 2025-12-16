package net.lab1024.sa.video.sdk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI SDK工厂单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@DisplayName("AI SDK工厂测试")
class AiSdkFactoryTest {

    private AiSdkFactory aiSdkFactory;

    @BeforeEach
    void setUp() {
        aiSdkFactory = new AiSdkFactory();
    }

    @Test
    @DisplayName("获取本地SDK提供者")
    void getProvider_local_shouldReturnLocalProvider() {
        AiSdkConfig config = new AiSdkConfig();
        config.setSdkType("LOCAL");
        config.setModelPath("/models");

        AiSdkProvider provider = aiSdkFactory.getProvider("LOCAL", config);

        assertNotNull(provider);
        assertEquals("LOCAL", provider.getName());
        assertTrue(provider.isAvailable());
    }

    @Test
    @DisplayName("获取未知类型SDK - 降级为本地SDK")
    void getProvider_unknown_shouldFallbackToLocal() {
        AiSdkConfig config = new AiSdkConfig();
        config.setSdkType("UNKNOWN");

        AiSdkProvider provider = aiSdkFactory.getProvider("UNKNOWN", config);

        assertNotNull(provider);
        assertEquals("LOCAL", provider.getName());
    }

    @Test
    @DisplayName("重复获取同一类型SDK - 返回缓存实例")
    void getProvider_sameTwice_shouldReturnCachedInstance() {
        AiSdkConfig config = new AiSdkConfig();
        config.setSdkType("LOCAL");

        AiSdkProvider provider1 = aiSdkFactory.getProvider("LOCAL", config);
        AiSdkProvider provider2 = aiSdkFactory.getProvider("LOCAL", config);

        assertSame(provider1, provider2);
    }

    @Test
    @DisplayName("销毁所有SDK")
    void destroyAll_shouldDestroyAllProviders() {
        AiSdkConfig config = new AiSdkConfig();
        AiSdkProvider provider = aiSdkFactory.getProvider("LOCAL", config);
        assertTrue(provider.isAvailable());

        aiSdkFactory.destroyAll();

        assertFalse(provider.isAvailable());
    }
}
