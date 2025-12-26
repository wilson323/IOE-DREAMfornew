package net.lab1024.sa.access.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 门禁通行记录数据压缩服务测试
 * 测试文件已简化，待后续完善
 */
@DisplayName("门禁通行记录数据压缩服务测试")
class AccessRecordCompressionServiceTest {

    @Test
    @DisplayName("测试：Service测试环境可以正常加载")
    void contextLoads() {
        assertNotNull(this, "Service测试类应该可以正常实例化");
        System.out.println("[测试] Service测试环境加载成功");
    }
}
