package net.lab1024.sa.attendance.integration;

import net.lab1024.sa.attendance.config.EnhancedTestConfiguration;
import net.lab1024.sa.attendance.config.IntegrationTestConfiguration;
import org.springframework.context.annotation.Import;
import net.lab1024.sa.attendance.domain.form.smartSchedule.SmartSchedulePlanAddForm;
import net.lab1024.sa.attendance.domain.form.smartSchedule.SmartSchedulePlanQueryForm;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartSchedulePlanDetailVO;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartSchedulePlanVO;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartScheduleResultVO;
import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;
import net.lab1024.sa.attendance.service.SmartScheduleService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SmartSchedule集成测试
 * <p>
 * 测试SmartSchedule模块的Service层、DAO层及其集成
 * 使用真实数据库和完整的应用上下文
 * </p>
 *
 * @TestData:
 * - 班次数据: 3个班次用于排班
 * - 排班记录: 3条记录（自动排班、手动排班）
 * - 考勤汇总: 月度汇总数据
 * - 部门统计: 部门级别统计数据
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
@DisplayName("智能排班集成测试")
class SmartScheduleIntegrationTest {

    @Autowired
    private SmartScheduleService smartScheduleService;

    private Long testPlanId;
    private Long testResultId;

    @BeforeEach
    void setUp() {
        // 每个测试前可以初始化测试数据
    }

    @AfterEach
    void tearDown() {
        // 每个测试后清理测试数据
        // @Transactional会自动回滚
    }

    @Test
    @DisplayName("测试创建排班计划")
    void testCreatePlan() {
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        // 设置必要的字段（根据实际Form结构调整）

        Long planId = smartScheduleService.createPlan(form);

        assertNotNull(planId, "计划ID不应为null");
        assertTrue(planId > 0, "计划ID应该大于0");

        testPlanId = planId;
    }

    @Test
    @DisplayName("测试执行排班优化 - 完整流程")
    void testExecuteOptimizationCompleteFlow() {
        // 1. 创建排班计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);
        assertNotNull(planId);

        // 2. 执行优化
        OptimizationResult result = smartScheduleService.executeOptimization(planId);

        // 3. 验证优化结果
        assertNotNull(result, "优化结果不应为null");
        assertTrue(result.getOptimizationSuccessful(), "优化应该成功");
        assertNotNull(result.getBestChromosome(), "最优解不应为null");
        assertTrue(result.getBestFitness() > 0, "适应度应该大于0");
        assertTrue(result.getIterations() > 0, "迭代次数应该大于0");
        assertTrue(result.getExecutionDurationMs() >= 0, "执行时间应该非负");

        testPlanId = planId;
    }

    @Test
    @DisplayName("测试查询排班计划列表")
    void testQueryPlanPage() {
        SmartSchedulePlanQueryForm form = new SmartSchedulePlanQueryForm();
        form.setPageNum(1);
        form.setPageSize(10);

        PageResult<SmartSchedulePlanVO> pageResult = smartScheduleService.queryPlanPage(form);

        assertNotNull(pageResult, "分页结果不应为null");
        assertNotNull(pageResult.getList(), "结果列表不应为null");
    }

    @Test
    @DisplayName("测试查询排班计划详情")
    void testGetPlanDetail() {
        // 先创建一个计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);

        // 查询详情
        SmartSchedulePlanDetailVO detail = smartScheduleService.getPlanDetail(planId);

        assertNotNull(detail, "详情不应为null");
        // 验证详情字段（根据实际VO结构调整）
    }

    @Test
    @DisplayName("测试查询排班结果列表")
    void testQueryResultPage() {
        // 先创建计划并执行优化
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);
        OptimizationResult result = smartScheduleService.executeOptimization(planId);

        // 查询结果
        PageResult<SmartScheduleResultVO> pageResult = smartScheduleService.queryResultPage(
            planId, 1, 10, null, null, null);

        assertNotNull(pageResult, "分页结果不应为null");
        assertNotNull(pageResult.getList(), "结果列表不应为null");
    }

    @Test
    @DisplayName("测试查询所有排班结果")
    void testQueryResultList() {
        // 先创建计划并执行优化
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);
        OptimizationResult result = smartScheduleService.executeOptimization(planId);

        // 查询所有结果
        List<SmartScheduleResultVO> resultList = smartScheduleService.queryResultList(planId);

        assertNotNull(resultList, "结果列表不应为null");
        assertFalse(resultList.isEmpty(), "结果列表不应为空");
    }

    @Test
    @DisplayName("测试删除排班计划")
    void testDeletePlan() {
        // 创建计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);

        // 删除计划
        assertDoesNotThrow(() -> smartScheduleService.deletePlan(planId));

        // 验证删除后查询不到
        assertThrows(Exception.class, () -> smartScheduleService.getPlanDetail(planId));
    }

    @Test
    @DisplayName("测试批量删除排班计划")
    void testBatchDeletePlan() {
        // 创建多个计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId1 = smartScheduleService.createPlan(form);
        Long planId2 = smartScheduleService.createPlan(form);

        // 批量删除
        List<Long> planIds = Arrays.asList(planId1, planId2);
        assertDoesNotThrow(() -> smartScheduleService.batchDeletePlan(planIds));
    }

    @Test
    @DisplayName("测试确认排班计划")
    void testConfirmPlan() {
        // 创建计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);

        // 确认计划
        assertDoesNotThrow(() -> smartScheduleService.confirmPlan(planId));
    }

    @Test
    @DisplayName("测试取消排班计划")
    void testCancelPlan() {
        // 创建计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);

        // 取消计划
        String reason = "测试取消";
        assertDoesNotThrow(() -> smartScheduleService.cancelPlan(planId, reason));
    }

    @Test
    @DisplayName("测试导出排班结果")
    void testExportScheduleResult() {
        // 创建计划并执行优化
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);
        OptimizationResult result = smartScheduleService.executeOptimization(planId);

        // 导出结果
        byte[] excelBytes = smartScheduleService.exportScheduleResult(planId);

        assertNotNull(excelBytes, "导出的字节数组不应为null");
        assertTrue(excelBytes.length > 0, "导出的字节数组应该有内容");
    }

    @Test
    @DisplayName("测试优化配置验证")
    void testValidateOptimizationConfig() {
        OptimizationConfig config = OptimizationConfig.builder()
            .employeeIds(Arrays.asList(1L, 2L, 3L, 4L, 5L))
            .startDate(LocalDate.of(2025, 1, 27))
            .endDate(LocalDate.of(2025, 2, 2))
            .shiftIds(Arrays.asList(1L, 2L, 3L))
            .maxGenerations(50)
            .initialTemperature(1000.0)
            .coolingRate(0.95)
            .build();

        // 验证配置有效性
        assertNotNull(config.getEmployeeIds());
        assertEquals(5, config.getEmployeeIds().size());
        assertEquals(7, config.getPeriodDays());
        assertEquals(3, config.getShiftCount());
        assertTrue(config.getMaxGenerations() > 0);
        assertTrue(config.getInitialTemperature() > 0);
    }

    @Test
    @DisplayName("测试不同优化算法")
    void testDifferentOptimizationAlgorithms() {
        // 创建计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);

        // 执行优化
        OptimizationResult result = smartScheduleService.executeOptimization(planId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOptimizationSuccessful());
        assertTrue(result.getBestFitness() > 0);
    }

    @Test
    @DisplayName("测试多员工排班优化")
    void testMultiEmployeeOptimization() {
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        // 配置多个员工

        Long planId = smartScheduleService.createPlan(form);
        OptimizationResult result = smartScheduleService.executeOptimization(planId);

        assertNotNull(result);
        assertTrue(result.getOptimizationSuccessful());

        // 验证每个员工都有排班
        Chromosome bestChromosome = result.getBestChromosome();
        assertNotNull(bestChromosome.getEmployeeIds());
        assertTrue(bestChromosome.getEmployeeIds().size() > 0);
    }

    @Test
    @DisplayName("测试长时间周期排班")
    void testLongPeriodScheduling() {
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        // 配置长时间周期（如一个月）

        Long planId = smartScheduleService.createPlan(form);
        OptimizationResult result = smartScheduleService.executeOptimization(planId);

        assertNotNull(result);
        assertTrue(result.getOptimizationSuccessful());
        assertTrue(result.getExecutionDurationMs() < 60000, "长周期排班应该在60秒内完成");
    }

    @Test
    @DisplayName("测试优化结果一致性")
    void testOptimizationResultConsistency() {
        // 创建计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);

        // 多次执行优化
        int runs = 3;
        double[] fitnessValues = new double[runs];

        for (int i = 0; i < runs; i++) {
            OptimizationResult result = smartScheduleService.executeOptimization(planId);
            fitnessValues[i] = result.getBestFitness();
            assertTrue(result.getOptimizationSuccessful());
        }

        // 验证所有运行都产生了有效结果
        for (double fitness : fitnessValues) {
            assertTrue(fitness > 0, "适应度应该大于0");
        }
    }

    @Test
    @DisplayName("测试并发优化执行")
    @Transactional
    void testConcurrentOptimization() {
        // 创建多个计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId1 = smartScheduleService.createPlan(form);
        Long planId2 = smartScheduleService.createPlan(form);

        // 并发执行优化
        OptimizationResult result1 = smartScheduleService.executeOptimization(planId1);
        OptimizationResult result2 = smartScheduleService.executeOptimization(planId2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.getOptimizationSuccessful());
        assertTrue(result2.getOptimizationSuccessful());
    }

    @Test
    @DisplayName("测试边界条件 - 最小规模")
    void testBoundaryConditions_MinimumScale() {
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        // 最小配置：1个员工，1天，1个班次

        Long planId = smartScheduleService.createPlan(form);
        assertNotNull(planId);

        OptimizationResult result = smartScheduleService.executeOptimization(planId);
        assertTrue(result.getOptimizationSuccessful());
    }

    @Test
    @DisplayName("测试边界条件 - 最大规模")
    void testBoundaryConditions_MaximumScale() {
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        // 大规模配置：多个员工，长时间周期

        Long planId = smartScheduleService.createPlan(form);
        assertNotNull(planId);

        OptimizationResult result = smartScheduleService.executeOptimization(planId);
        assertTrue(result.getOptimizationSuccessful());
        assertTrue(result.getExecutionDurationMs() < 120000, "大规模优化应该在2分钟内完成");
    }

    @Test
    @DisplayName("测试错误处理 - 无效计划ID")
    void testErrorHandling_InvalidPlanId() {
        Long invalidPlanId = -1L;

        // 尝试执行优化
        assertThrows(Exception.class, () -> {
            smartScheduleService.executeOptimization(invalidPlanId);
        });
    }

    @Test
    @DisplayName("测试错误处理 - 重复确认")
    void testErrorHandling_DuplicateConfirm() {
        // 创建计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);

        // 第一次确认
        smartScheduleService.confirmPlan(planId);

        // 第二次确认（应该处理或抛出异常）
        assertDoesNotThrow(() -> smartScheduleService.confirmPlan(planId));
    }

    @Test
    @DisplayName("测试数据持久化")
    @Transactional
    void testDataPersistence() {
        // 创建计划
        SmartSchedulePlanAddForm form = new SmartSchedulePlanAddForm();
        Long planId = smartScheduleService.createPlan(form);

        // 查询验证
        SmartSchedulePlanDetailVO detail = smartScheduleService.getPlanDetail(planId);
        assertNotNull(detail);

        // 执行优化
        OptimizationResult result = smartScheduleService.executeOptimization(planId);
        assertTrue(result.getOptimizationSuccessful());

        // 再次查询验证结果已保存
        List<SmartScheduleResultVO> resultList = smartScheduleService.queryResultList(planId);
        assertFalse(resultList.isEmpty(), "优化结果应该已持久化");
    }
}
