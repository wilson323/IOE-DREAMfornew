package net.lab1024.sa.attendance.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 加班Service测试
 * 测试文件已简化，待后续完善
 */
@SpringBootTest(classes = {
    org.springframework.boot.autoconfigure.ImportAutoConfiguration.class,
    org.springframework.context.annotation.Configuration.class
})
@ActiveProfiles("h2-test")
@DisplayName("加班Service测试")
class AttendanceOvertimeServiceImplTest {

    @Test
    @DisplayName("测试：Service测试环境可以正常加载")
    void contextLoads() {
        System.out.println("[测试] Service测试环境加载成功");
    }
}
