package net.lab1024.sa.attendance.integration;

import net.lab1024.sa.attendance.config.EnhancedTestConfiguration;
import net.lab1024.sa.attendance.config.IntegrationTestConfiguration;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/**
 * 跨天班次集成测试
 * <p>
 * 测试跨天班次的考勤计算和处理逻辑
 * 使用H2内存数据库和测试数据脚本
 * </p>
 *
 * @TestData:
 * - 班次数据: 包含晚班（16:00-24:00）跨天场景
 * - 考勤记录: 跨天打卡记录
 * - 异常记录: 跨天异常检测
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@SpringBootTest(classes = IntegrationTestConfiguration.class)
@Import(EnhancedTestConfiguration.class)
@ActiveProfiles("h2-test")
@Transactional
@Sql(scripts = "/sql/00-test-schema.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/01-test-basic-data.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("跨天班次集成测试")
class CrossDayShiftIntegrationTest {

    @Test
    @DisplayName("测试：集成测试环境可以正常加载")
    void contextLoads() {
        System.out.println("[测试] 集成测试环境加载成功");
    }
}
