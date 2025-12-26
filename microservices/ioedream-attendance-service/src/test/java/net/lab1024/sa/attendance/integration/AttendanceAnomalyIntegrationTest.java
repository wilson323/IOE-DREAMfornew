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
 * 考勤异常集成测试
 * <p>
 * 测试考勤异常检测、申请和审批流程
 * 使用H2内存数据库和测试数据脚本
 * </p>
 *
 * @TestData:
 * - 班次数据: 3个班次（正常班、早班、晚班）
 * - 考勤规则: 2个规则配置（默认、严格）
 * - 考勤记录: 6条记录（正常、迟到、早退场景）
 * - 异常记录: 2条记录（缺卡、旷工）
 * - 异常申请: 3条申请（补卡、申诉）
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
@Sql(scripts = "/sql/02-test-extended-data.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("考勤异常集成测试")
class AttendanceAnomalyIntegrationTest {

    @Test
    @DisplayName("测试：集成测试环境可以正常加载")
    void contextLoads() {
        System.out.println("[测试] 集成测试环境加载成功");
    }
}
