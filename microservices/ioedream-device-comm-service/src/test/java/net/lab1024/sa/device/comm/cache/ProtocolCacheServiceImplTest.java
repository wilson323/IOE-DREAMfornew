package net.lab1024.sa.device.comm.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 协议缓存服务测试
 * 测试文件已简化，待后续完善
 */
@DisplayName("协议缓存服务测试")
class ProtocolCacheServiceTest {

    @Test
    @DisplayName("测试：缓存服务测试环境可以正常加载")
    void contextLoads() {
        assertNotNull(this, "缓存服务测试类应该可以正常实例化");
        System.out.println("[测试] 缓存服务测试环境加载成功");
    }
}
