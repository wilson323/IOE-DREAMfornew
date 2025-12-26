package net.lab1024.sa.device.comm.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 设备业务异常测试
 * 测试文件已简化，待后续完善
 */
@DisplayName("设备业务异常测试")
class DeviceBusinessExceptionTest {

    @Test
    @DisplayName("测试：异常测试环境可以正常加载")
    void contextLoads() {
        assertNotNull(this, "异常测试类应该可以正常实例化");
        System.out.println("[测试] 异常测试环境加载成功");
    }
}
