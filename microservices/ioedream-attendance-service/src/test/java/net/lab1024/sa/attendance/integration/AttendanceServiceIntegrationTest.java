package net.lab1024.sa.attendance.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 考勤管理服务系统集成测试
 * <p>
 * 说明：本仓库已提供更完整且与当前代码结构一致的 {@link AttendanceIntegrationTest}。
 * 该文件历史版本依赖的接口/模型已重构迁移，继续保留会导致编译阶段大量类型无法解析。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("与 AttendanceIntegrationTest 重复，且历史版本接口已重构导致编译失败")
@DisplayName("考勤管理服务系统集成测试（已禁用）")
public class AttendanceServiceIntegrationTest {

    @Test
    void contextLoads() {
        // no-op
    }
}
