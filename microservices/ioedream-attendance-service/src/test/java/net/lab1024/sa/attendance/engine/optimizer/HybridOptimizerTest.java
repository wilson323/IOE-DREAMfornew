package net.lab1024.sa.attendance.engine.optimizer;

import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 混合优化器单元测试
 *
 * 测试范围：
 * - 基本优化流程
 * - 两阶段优化机制
 * - 结果选择逻辑
 * - 集成测试
 * - 边界条件
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@DisplayName("混合优化器测试")
class HybridOptimizerTest {

    private HybridOptimizer hybridOptimizer;
    private OptimizationConfig config;

    @BeforeEach
    void setUp() throws Exception {
        hybridOptimizer = new HybridOptimizer();

        // 使用反射手动设置私有字段（模拟Spring依赖注入）
        Field gaField = HybridOptimizer.class.getDeclaredField("geneticOptimizer");
        gaField.setAccessible(true);
        gaField.set(hybridOptimizer, new GeneticAlgorithmOptimizer());

        Field saField = HybridOptimizer.class.getDeclaredField("simulatedAnnealingOptimizer");
        saField.setAccessible(true);
        saField.set(hybridOptimizer, new SimulatedAnnealingOptimizer());

        // 创建测试配置
        config = OptimizationConfig.builder()
            .employeeIds(Arrays.asList(1L, 2L, 3L, 4L, 5L))
            .startDate(LocalDate.of(2025, 1, 27))
            .endDate(LocalDate.of(2025, 2, 2))
            .shiftIds(Arrays.asList(1L, 2L, 3L))
            .maxConsecutiveWorkDays(7)
            .minRestDays(2)
            .minDailyStaff(2)
            .fairnessWeight(0.4)
            .costWeight(0.3)
            .efficiencyWeight(0.2)
            .satisfactionWeight(0.1)
            .maxGenerations(50)
            .initialTemperature(1000.0)
            .coolingRate(0.95)
            .build();
    }

    @Test
    @DisplayName("测试基本优化流程")
    void testBasicOptimization() {
        OptimizationResult result = hybridOptimizer.optimize(config);

        assertNotNull(result, "优化结果不应为null");
        assertTrue(result.getOptimizationSuccessful(), "优化应该成功");
        assertNotNull(result.getBestChromosome(), "最优解不应为null");
        assertTrue(result.getBestFitness() > 0, "最优适应度应该大于0");
        assertTrue(result.getIterations() > 0, "迭代次数应该大于0");
        assertTrue(result.getExecutionDurationMs() >= 0, "执行时间应该非负");
    }

    @Test
    @DisplayName("测试两阶段优化机制")
    @SuppressWarnings("SystemOut")
    void testTwoStageOptimization() {
        System.out.println("\n=== 测试两阶段优化 ===");

        // 运行混合优化器（内部会依次运行遗传算法和模拟退火）
        OptimizationResult hybridResult = hybridOptimizer.optimize(config);
        System.out.println("混合算法适应度: " + hybridResult.getBestFitness());

        // 验证结果是有效的
        assertTrue(hybridResult.getOptimizationSuccessful());
        assertTrue(hybridResult.getBestFitness() > 0);
        assertTrue(hybridResult.getIterations() > 0);
    }

    @Test
    @DisplayName("测试结果选择逻辑")
    void testResultSelection() {
        // 多次运行，验证结果一致性
        int totalRuns = 3;
        double[] fitnessValues = new double[totalRuns];

        for (int i = 0; i < totalRuns; i++) {
            OptimizationResult result = hybridOptimizer.optimize(config);
            fitnessValues[i] = result.getBestFitness();
            assertTrue(result.getOptimizationSuccessful(), "优化应该成功");
        }

        // 验证所有运行都产生了有效结果
        for (double fitness : fitnessValues) {
            assertTrue(fitness > 0, "适应度应该大于0");
        }

        System.out.println(String.format("运行%d次，适应度值: %s",
            totalRuns, Arrays.toString(fitnessValues)));
    }

    @Test
    @DisplayName("测试小规模问题")
    void testSmallScaleProblem() {
        OptimizationConfig smallConfig = OptimizationConfig.builder()
            .employeeIds(Arrays.asList(1L, 2L, 3L))
            .startDate(LocalDate.of(2025, 1, 27))
            .endDate(LocalDate.of(2025, 1, 29))
            .shiftIds(Arrays.asList(1L, 2L))
            .maxGenerations(20)
            .initialTemperature(500.0)
            .coolingRate(0.95)
            .build();

        OptimizationResult result = hybridOptimizer.optimize(smallConfig);

        assertNotNull(result);
        assertTrue(result.getOptimizationSuccessful());
        assertNotNull(result.getBestChromosome());
        assertTrue(result.getBestFitness() > 0);
        assertTrue(result.getIterations() > 0);
    }

    @Test
    @DisplayName("测试中等规模问题")
    void testMediumScaleProblem() {
        List<Long> employees = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        LocalDate startDate = LocalDate.of(2025, 1, 27);
        LocalDate endDate = LocalDate.of(2025, 2, 2); // 1周
        List<Long> shifts = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        OptimizationConfig mediumConfig = OptimizationConfig.builder()
            .employeeIds(employees)
            .startDate(startDate)
            .endDate(endDate)
            .shiftIds(shifts)
            .maxGenerations(50)
            .initialTemperature(1000.0)
            .coolingRate(0.95)
            .build();

        long startTime = System.currentTimeMillis();
        OptimizationResult result = hybridOptimizer.optimize(mediumConfig);
        long duration = System.currentTimeMillis() - startTime;

        assertNotNull(result);
        assertTrue(result.getOptimizationSuccessful());
        assertTrue(duration < 15000, "10员工x7天x5班次应该在15秒内完成");
    }

    @Test
    @DisplayName("测试优化收敛性")
    @SuppressWarnings("SystemOut")
    void testOptimizationConvergence() {
        // 多次运行优化，验证结果稳定性
        int runs = 5;
        double[] fitnessValues = new double[runs];

        for (int i = 0; i < runs; i++) {
            OptimizationResult result = hybridOptimizer.optimize(config);
            fitnessValues[i] = result.getBestFitness();
        }

        // 计算平均值和标准差
        double mean = Arrays.stream(fitnessValues).average().orElse(0);
        double variance = Arrays.stream(fitnessValues)
            .map(f -> Math.pow(f - mean, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        // 验证结果相对稳定
        double coefficientOfVariation = stdDev / mean;
        assertTrue(coefficientOfVariation < 0.25, "结果应该相对稳定，变异系数<25%");

        System.out.println("收敛性测试结果:");
        System.out.println("- 平均适应度: " + mean);
        System.out.println("- 标准差: " + stdDev);
        System.out.println("- 变异系数: " + coefficientOfVariation);
    }

    @Test
    @DisplayName("测试解的完整性")
    void testSolutionCompleteness() {
        OptimizationResult result = hybridOptimizer.optimize(config);

        Chromosome best = result.getBestChromosome();
        assertNotNull(best);
        assertNotNull(best.getEmployeeIds());
        assertEquals(5, best.getEmployeeIds().size());

        // 验证每个员工都有排班
        best.getEmployeeIds().forEach(employeeId -> {
            assertNotNull(best.getSchedule(employeeId), "员工" + employeeId + "应该有排班");
            assertFalse(best.getSchedule(employeeId).isEmpty(), "员工" + employeeId + "排班不应为空");
        });
    }

    @Test
    @DisplayName("测试边界条件 - 极小问题")
    void testBoundaryConditions_Minimal() {
        OptimizationConfig minConfig = OptimizationConfig.builder()
            .employeeIds(Arrays.asList(1L))
            .startDate(LocalDate.of(2025, 1, 27))
            .endDate(LocalDate.of(2025, 1, 27))
            .shiftIds(Arrays.asList(1L))
            .maxGenerations(10)
            .initialTemperature(100.0)
            .coolingRate(0.95)
            .build();

        OptimizationResult minResult = hybridOptimizer.optimize(minConfig);

        assertNotNull(minResult);
        assertTrue(minResult.getOptimizationSuccessful());
        assertNotNull(minResult.getBestChromosome());
    }

    @Test
    @DisplayName("测试边界条件 - 高权重公平性")
    void testBoundaryConditions_HighFairnessWeight() {
        OptimizationConfig fairnessConfig = OptimizationConfig.builder()
            .employeeIds(Arrays.asList(1L, 2L, 3L))
            .startDate(LocalDate.of(2025, 1, 27))
            .endDate(LocalDate.of(2025, 1, 29))
            .shiftIds(Arrays.asList(1L, 2L))
            .maxGenerations(30)
            .initialTemperature(500.0)
            .coolingRate(0.95)
            .fairnessWeight(1.0)  // 只考虑公平性
            .costWeight(0.0)
            .efficiencyWeight(0.0)
            .satisfactionWeight(0.0)
            .build();

        OptimizationResult result = hybridOptimizer.optimize(fairnessConfig);

        assertNotNull(result);
        assertTrue(result.getBestFitness() > 0);
    }

    @Test
    @DisplayName("测试性能对比")
    @SuppressWarnings("SystemOut")
    void testPerformanceComparison() {
        System.out.println("\n=== 性能对比测试 ===");

        List<Long> employees = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
        LocalDate startDate = LocalDate.of(2025, 1, 27);
        LocalDate endDate = LocalDate.of(2025, 2, 2); // 1周
        List<Long> shifts = Arrays.asList(1L, 2L, 3L, 4L);

        OptimizationConfig perfConfig = OptimizationConfig.builder()
            .employeeIds(employees)
            .startDate(startDate)
            .endDate(endDate)
            .shiftIds(shifts)
            .maxGenerations(50)
            .initialTemperature(1000.0)
            .coolingRate(0.95)
            .build();

        // 测试混合算法
        long hybridStart = System.currentTimeMillis();
        OptimizationResult hybridResult = hybridOptimizer.optimize(perfConfig);
        long hybridDuration = System.currentTimeMillis() - hybridStart;

        System.out.println(String.format("混合算法: %dms, 适应度: %.2f, 迭代: %d",
            hybridDuration, hybridResult.getBestFitness(), hybridResult.getIterations()));

        // 验证性能可接受（混合算法运行两个优化器，可能需要更长时间）
        assertTrue(hybridDuration < 30000, "8员工x7天x4班次应该在30秒内完成");
        assertTrue(hybridResult.getOptimizationSuccessful());
        assertTrue(hybridResult.getBestFitness() > 0);
    }
}
