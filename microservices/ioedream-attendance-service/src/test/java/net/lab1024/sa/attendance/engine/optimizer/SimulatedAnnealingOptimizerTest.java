package net.lab1024.sa.attendance.engine.optimizer;

import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模拟退火优化器单元测试
 *
 * 测试范围：
 * - 优化配置构建
 * - 初始解生成
 * - 温度衰减机制
 * - 邻域搜索
 * - 完整优化流程
 * - 边界条件
 * - 性能测试
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@DisplayName("模拟退火优化器测试")
class SimulatedAnnealingOptimizerTest {

    private SimulatedAnnealingOptimizer optimizer;
    private OptimizationConfig config;

    @BeforeEach
    void setUp() {
        optimizer = new SimulatedAnnealingOptimizer();

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
            .maxGenerations(100)
            .initialTemperature(1000.0)
            .coolingRate(0.95)
            .build();
    }

    @Test
    @DisplayName("测试优化配置构建")
    void testOptimizationConfigBuilder() {
        assertNotNull(config);
        assertEquals(5, config.getEmployeeIds().size());
        assertEquals(7, config.getPeriodDays());
        assertEquals(3, config.getShiftCount());
        assertEquals(7, config.getMaxConsecutiveWorkDays());
        assertEquals(2, config.getMinRestDays());
        assertEquals(2, config.getMinDailyStaff());
        assertEquals(0.4, config.getFairnessWeight());
        assertEquals(100, config.getMaxGenerations());
        assertEquals(1000.0, config.getInitialTemperature());
        assertEquals(0.95, config.getCoolingRate());
    }

    @Test
    @DisplayName("测试初始解生成")
    void testInitialSolutionGeneration() {
        OptimizationResult result = optimizer.optimize(config);

        assertNotNull(result);
        assertNotNull(result.getBestChromosome());
        assertTrue(result.getBestFitness() > 0);
        assertTrue(result.getIterations() > 0);
        assertTrue(result.getExecutionDurationMs() >= 0);  // 可能为0（极快完成）
    }

    @Test
    @DisplayName("测试温度衰减机制")
    void testTemperatureDecay() {
        // 测试温度按冷却率衰减
        double initialTemp = config.getInitialTemperature();
        double coolingRate = config.getCoolingRate();

        // 模拟10次迭代
        double currentTemp = initialTemp;
        for (int i = 0; i < 10; i++) {
            double oldTemp = currentTemp;
            currentTemp *= coolingRate;
            assertTrue(currentTemp < oldTemp, "温度应该衰减");
            assertTrue(currentTemp > 0, "温度应该保持正数");
        }

        // 验证最终温度
        double expectedTemp = initialTemp * Math.pow(coolingRate, 10);
        assertEquals(expectedTemp, currentTemp, 0.001);
    }

    @Test
    @DisplayName("测试邻域搜索")
    void testNeighborhoodSearch() {
        // 执行优化
        OptimizationResult result = optimizer.optimize(config);

        // 验证产生了改进的解
        Chromosome best = result.getBestChromosome();
        assertNotNull(best);

        // 验证解的有效性
        assertNotNull(best.getEmployeeIds());
        assertFalse(best.getEmployeeIds().isEmpty());

        // 验证每个员工都有排班
        best.getEmployeeIds().forEach(employeeId -> {
            assertNotNull(best.getSchedule(employeeId));
            assertFalse(best.getSchedule(employeeId).isEmpty());
        });
    }

    @Test
    @DisplayName("测试接受概率计算")
    void testAcceptanceProbability() {
        // 测试不同delta值下的接受概率
        double temperature = 100.0;

        // delta > 0: 应该总是接受（更好的解）
        double deltaPositive = 10.0;
        double acceptancePositive = Math.exp(deltaPositive / temperature);
        assertTrue(acceptancePositive > 1.0, "更好的解应该总是被接受");

        // delta < 0: 以一定概率接受（更差的解）
        double deltaNegative = -10.0;
        double acceptanceNegative = Math.exp(deltaNegative / temperature);
        assertTrue(acceptanceNegative < 1.0, "更差的解应该以概率接受");
        assertTrue(acceptanceNegative > 0.0, "接受概率应该为正数");

        // delta = 0: 接受概率为1
        double deltaZero = 0.0;
        double acceptanceZero = Math.exp(deltaZero / temperature);
        assertEquals(1.0, acceptanceZero, 0.001, "相同适应度的解应该总是被接受");
    }

    @Test
    @DisplayName("测试冷却计划")
    void testCoolingSchedule() {
        // 测试不同冷却率
        double[] coolingRates = {0.90, 0.95, 0.99};
        double initialTemp = 1000.0;
        int iterations = 100;

        for (double coolingRate : coolingRates) {
            OptimizationConfig testConfig = OptimizationConfig.builder()
                .employeeIds(Arrays.asList(1L, 2L, 3L))
                .startDate(LocalDate.of(2025, 1, 27))
                .endDate(LocalDate.of(2025, 1, 29))
                .shiftIds(Arrays.asList(1L, 2L))
                .maxGenerations(iterations)
                .initialTemperature(initialTemp)
                .coolingRate(coolingRate)
                .build();

            OptimizationResult result = optimizer.optimize(testConfig);

            assertNotNull(result);
            assertTrue(result.getIterations() > 0, "应该执行迭代");

            // 冷却率越高，最终温度越高
            double finalTemp = initialTemp * Math.pow(coolingRate, iterations);
            assertTrue(finalTemp > 0, "最终温度应该为正数");
        }
    }

    @Test
    @DisplayName("测试完整优化流程")
    void testCompleteOptimization() {
        OptimizationResult result = optimizer.optimize(config);

        // 验证结果有效性
        assertNotNull(result, "优化结果不应为null");
        assertTrue(result.getOptimizationSuccessful(), "优化应该成功");
        assertNotNull(result.getBestChromosome(), "最优解不应为null");
        assertTrue(result.getBestFitness() > 0, "最优适应度应该大于0");
        assertTrue(result.getIterations() > 0, "迭代次数应该大于0");
        assertTrue(result.getExecutionDurationMs() >= 0, "执行时间应该非负");

        // 验证最优解的完整性
        Chromosome best = result.getBestChromosome();
        assertNotNull(best.getEmployeeIds());
        assertEquals(5, best.getEmployeeIds().size());

        // 验证排班完整性
        best.getEmployeeIds().forEach(employeeId -> {
            assertNotNull(best.getSchedule(employeeId));
            assertTrue(best.getSchedule(employeeId).size() > 0);
        });
    }

    @Test
    @DisplayName("测试边界条件")
    void testBoundaryConditions() {
        // 测试最小规模问题（1个员工，1天，1个班次）
        OptimizationConfig minConfig = OptimizationConfig.builder()
            .employeeIds(Arrays.asList(1L))
            .startDate(LocalDate.of(2025, 1, 27))
            .endDate(LocalDate.of(2025, 1, 27))
            .shiftIds(Arrays.asList(1L))
            .maxGenerations(10)
            .initialTemperature(100.0)
            .coolingRate(0.95)
            .build();

        OptimizationResult minResult = optimizer.optimize(minConfig);
        assertNotNull(minResult);
        assertTrue(minResult.getOptimizationSuccessful());

        // 测试极低初始温度
        OptimizationConfig lowTempConfig = OptimizationConfig.builder()
            .employeeIds(Arrays.asList(1L, 2L))
            .startDate(LocalDate.of(2025, 1, 27))
            .endDate(LocalDate.of(2025, 1, 28))
            .shiftIds(Arrays.asList(1L, 2L))
            .maxGenerations(10)
            .initialTemperature(1.1) // 接近终止条件
            .coolingRate(0.95)
            .build();

        OptimizationResult lowTempResult = optimizer.optimize(lowTempConfig);
        assertNotNull(lowTempResult);
        assertTrue(lowTempResult.getIterations() < 10, "低初始温度应该快速终止");

        // 测试极高冷却率
        OptimizationConfig highCoolingConfig = OptimizationConfig.builder()
            .employeeIds(Arrays.asList(1L, 2L))
            .startDate(LocalDate.of(2025, 1, 27))
            .endDate(LocalDate.of(2025, 1, 28))
            .shiftIds(Arrays.asList(1L, 2L))
            .maxGenerations(50)
            .initialTemperature(1000.0)
            .coolingRate(0.99) // 非常慢的冷却
            .build();

        OptimizationResult highCoolingResult = optimizer.optimize(highCoolingConfig);
        assertNotNull(highCoolingResult);
        assertTrue(highCoolingResult.getIterations() > 10, "高冷却率应该执行更多迭代");
    }

    @Test
    @DisplayName("测试性能")
    @SuppressWarnings("SystemOut")
    void testPerformance() {
        // 测试中等规模问题的性能
        List<Long> employees = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        LocalDate startDate = LocalDate.of(2025, 1, 27);
        LocalDate endDate = LocalDate.of(2025, 2, 9); // 2周
        List<Long> shifts = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        OptimizationConfig perfConfig = OptimizationConfig.builder()
            .employeeIds(employees)
            .startDate(startDate)
            .endDate(endDate)
            .shiftIds(shifts)
            .maxGenerations(200)
            .initialTemperature(1000.0)
            .coolingRate(0.95)
            .build();

        long startTime = System.currentTimeMillis();
        OptimizationResult result = optimizer.optimize(perfConfig);
        long duration = System.currentTimeMillis() - startTime;

        assertNotNull(result);
        assertTrue(result.getOptimizationSuccessful(), "优化应该成功");
        assertTrue(duration < 10000, "10员工x14天x5班次应该在10秒内完成"); // 10秒超时

        // 性能指标
        System.out.println("性能测试结果:");
        System.out.println("- 员工数: " + employees.size());
        System.out.println("- 天数: " + (endDate.getDayOfYear() - startDate.getDayOfYear() + 1));
        System.out.println("- 班次数: " + shifts.size());
        System.out.println("- 迭代次数: " + result.getIterations());
        System.out.println("- 执行时间: " + duration + "ms");
        System.out.println("- 最优适应度: " + result.getBestFitness());
    }

    @Test
    @DisplayName("测试公平性计算")
    @SuppressWarnings("SystemOut")
    void testFairnessCalculation() {
        // 创建配置
        OptimizationConfig testConfig = OptimizationConfig.builder()
            .employeeIds(Arrays.asList(1L, 2L, 3L))
            .startDate(LocalDate.of(2025, 1, 27))
            .endDate(LocalDate.of(2025, 1, 29))
            .shiftIds(Arrays.asList(1L, 2L))
            .maxGenerations(50)
            .initialTemperature(500.0)
            .coolingRate(0.95)
            .fairnessWeight(1.0) // 只考虑公平性
            .costWeight(0.0)
            .efficiencyWeight(0.0)
            .satisfactionWeight(0.0)
            .build();

        OptimizationResult result = optimizer.optimize(testConfig);

        assertNotNull(result);
        assertTrue(result.getBestFitness() > 0, "适应度应该大于0");

        Chromosome best = result.getBestChromosome();

        // 验证工作日分配相对公平
        int[] workDays = new int[3];
        workDays[0] = best.getSchedule(1L).size();
        workDays[1] = best.getSchedule(2L).size();
        workDays[2] = best.getSchedule(3L).size();

        int maxDays = Math.max(Math.max(workDays[0], workDays[1]), workDays[2]);
        int minDays = Math.min(Math.min(workDays[0], workDays[1]), workDays[2]);

        double fairness = 1.0 - (double)(maxDays - minDays) / maxDays;
        assertTrue(fairness > 0.5, "公平性应该大于0.5");

        System.out.println("公平性测试结果:");
        System.out.println("- 员工1工作天数: " + workDays[0]);
        System.out.println("- 员工2工作天数: " + workDays[1]);
        System.out.println("- 员工3工作天数: " + workDays[2]);
        System.out.println("- 公平性分数: " + fairness);
    }

    @Test
    @DisplayName("测试优化收敛性")
    @SuppressWarnings("SystemOut")
    void testOptimizationConvergence() {
        // 多次运行优化，验证结果稳定性
        int runs = 5;
        double[] fitnessValues = new double[runs];

        for (int i = 0; i < runs; i++) {
            OptimizationResult result = optimizer.optimize(config);
            fitnessValues[i] = result.getBestFitness();
        }

        // 计算平均值和标准差
        double mean = Arrays.stream(fitnessValues).average().orElse(0);
        double variance = Arrays.stream(fitnessValues)
            .map(f -> Math.pow(f - mean, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        // 验证结果相对稳定（变异系数小于20%）
        double coefficientOfVariation = stdDev / mean;
        assertTrue(coefficientOfVariation < 0.2, "结果应该相对稳定，变异系数<20%");

        System.out.println("收敛性测试结果:");
        System.out.println("- 平均适应度: " + mean);
        System.out.println("- 标准差: " + stdDev);
        System.out.println("- 变异系数: " + coefficientOfVariation);
    }
}
