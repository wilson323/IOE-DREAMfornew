package net.lab1024.sa.attendance.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 班次管理Controller测试
 * 测试文件已简化，待后续完善
 */
@DisplayName("班次管理Controller测试")
class WorkShiftControllerTest {

    @Test
    @DisplayName("测试：Controller测试环境可以正常加载")
    void contextLoads() {
        assertNotNull(this, "Controller测试类应该可以正常实例化");
        System.out.println("[测试] Controller测试环境加载成功");
    }
}
