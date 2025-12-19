package net.lab1024.sa.attendance.engine.algorithm.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.algorithm.AlgorithmMetadata;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithm;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;

/**
 * 遗传算法实现类
 * <p>
 * 使用遗传算法优化排班方案，支持多目标优化和约束满足
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class GeneticAlgorithmImpl implements ScheduleAlgorithm {

    // 算法参数
    private Map<String, Object> parameters;

    // 算法状态
    private volatile AlgorithmStatus status = AlgorithmStatus.INITIALIZED;

    // 算法回调
    private AlgorithmCallback callback;

    // 执行控制
    private volatile boolean shouldStop = false;
    private volatile boolean shouldPause = false;

    // 统计信息
    private final AtomicLong totalGenerations = new AtomicLong(0);
    private final AtomicInteger populationSize = new AtomicInteger(0);
    private final AtomicInteger bestFitness = new AtomicInteger(Integer.MIN_VALUE);

    // 默认参数常量
    private static final int DEFAULT_POPULATION_SIZE = 100;
    private static final int DEFAULT_MAX_GENERATIONS = 500;
    private static final double DEFAULT_CROSSOVER_RATE = 0.8;
    private static final double DEFAULT_MUTATION_RATE = 0.1;
    private static final double DEFAULT_ELITE_RATE = 0.1;
    private static final long DEFAULT_TIME_LIMIT = 60000; // 60秒

    /**
     * 生成排班方案
     */
    @Override
    public ScheduleResult generateSchedule(ScheduleData scheduleData) {
        log.info("[遗传算法] 开始生成排班方案");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        status = AlgorithmStatus.RUNNING;

        try {
            // 1. 验证输入数据
            if (!validateInputData(scheduleData)) {
                return createErrorResult("输入数据验证失败");
            }

            // 2. 初始化算法参数
            int popSize = getPopulationSize();
            int maxGenerations = getMaxGenerations();
            double crossoverRate = getCrossoverRate();
            double mutationRate = getMutationRate();
            double eliteRate = getEliteRate();
            long timeLimit = getTimeLimit();

            // 3. 初始化种群
            List<Chromosome> population = initializePopulation(scheduleData, popSize);
            populationSize.set(popSize);

            // 4. 进化循环
            Chromosome bestChromosome = null;
            int generation = 0;

            while (generation < maxGenerations && !shouldStop) {
                while (shouldPause) {
                    Thread.sleep(100);
                }

                // 检查时间限制
                if (stopWatch.getTotalTimeMillis() > timeLimit) {
                    log.warn("[遗传算法] 达到时间限制，停止进化");
                    break;
                }

                // 更新进度
                updateProgress((double) generation / maxGenerations, "进化第" + generation + "代");

                // 评估种群适应度
                evaluatePopulation(population, scheduleData);

                // 找出最佳个体
                Chromosome currentBest = findBestChromosome(population);
                if (bestChromosome == null || currentBest.getFitness() > bestChromosome.getFitness()) {
                    bestChromosome = currentBest;
                    bestFitness.set((int) currentBest.getFitness());
                }

                // 选择操作
                List<Chromosome> selectedPopulation = selection(population);

                // 交叉操作
                List<Chromosome> offspringPopulation = crossover(selectedPopulation, crossoverRate);

                // 变异操作
                mutate(offspringPopulation, mutationRate);

                // 精英保留策略
                population = elitism(population, offspringPopulation, eliteRate);

                generation++;
                totalGenerations.incrementAndGet();
            }

            // 5. 构建最终排班方案
            List<ScheduleRecord> scheduleRecords = convertChromosomeToSchedule(bestChromosome, scheduleData);

            // 6. 构建结果
            List<ScheduleResult.ScheduleRecord> resultRecords = new ArrayList<>();
            for (ScheduleRecord record : scheduleRecords) {
                resultRecords.add(convertToResultScheduleRecord(record));
            }

            ScheduleResult result = ScheduleResult.builder()
                    .status("SUCCESS")
                    .message("遗传算法排班完成")
                    .scheduleRecords(resultRecords)
                    .executionTime(stopWatch.getTotalTimeMillis())
                    .algorithmUsed(getAlgorithmType())
                    .qualityScore(calculateQualityScore(bestChromosome))
                    .build();

            // 7. 添加统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalGenerations", totalGenerations.get());
            statistics.put("populationSize", populationSize.get());
            statistics.put("bestFitness", bestFitness.get());
            statistics.put("crossoverRate", crossoverRate);
            statistics.put("mutationRate", mutationRate);
            statistics.put("eliteRate", eliteRate);
            statistics.put("convergenceGeneration", generation);
            result.setOptimizationMetrics(statistics);

            stopWatch.stop();
            status = AlgorithmStatus.COMPLETED;

            log.info("[遗传算法] 排班完成，耗时: {}ms，进化代数: {}, 最佳适应度: {}",
                    stopWatch.getTotalTimeMillis(), totalGenerations.get(), bestFitness.get());

            return result;

        } catch (Exception e) {
            log.error("[遗传算法] 生成排班方案失败", e);
            status = AlgorithmStatus.ERROR;
            return createErrorResult("算法执行异常: " + e.getMessage());
        } finally {
            stopWatch.stop();
        }
    }

    /**
     * 初始化算法参数
     */
    @Override
    public void initialize(Map<String, Object> parameters) {
        this.parameters = new HashMap<>(parameters != null ? parameters : new HashMap<>());
        log.debug("[遗传算法] 初始化参数: {}", this.parameters);
    }

    /**
     * 获取算法参数
     */
    @Override
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters != null ? parameters : new HashMap<>());
    }

    /**
     * 验证算法参数
     */
    @Override
    public AlgorithmValidationResult validateParameters(Map<String, Object> parameters) {
        AlgorithmValidationResult result = new AlgorithmValidationResult();

        try {
            // 验证种群大小
            Object popSizeObj = parameters.get("populationSize");
            if (popSizeObj != null) {
                try {
                    int popSize = Integer.parseInt(popSizeObj.toString());
                    if (popSize < 10 || popSize > 1000) {
                        result.setValid(false);
                        result.setErrorMessage("种群大小必须在10-1000之间");
                        return result;
                    }
                } catch (NumberFormatException e) {
                    result.setValid(false);
                    result.setErrorMessage("种群大小必须是数字");
                    return result;
                }
            }

            // 验证最大进化代数
            Object maxGenObj = parameters.get("maxGenerations");
            if (maxGenObj != null) {
                try {
                    int maxGen = Integer.parseInt(maxGenObj.toString());
                    if (maxGen < 1 || maxGen > 10000) {
                        result.setValid(false);
                        result.setErrorMessage("最大进化代数必须在1-10000之间");
                        return result;
                    }
                } catch (NumberFormatException e) {
                    result.setValid(false);
                    result.setErrorMessage("最大进化代数必须是数字");
                    return result;
                }
            }

            // 验证交叉率
            Object crossoverRateObj = parameters.get("crossoverRate");
            if (crossoverRateObj != null) {
                try {
                    double crossoverRate = Double.parseDouble(crossoverRateObj.toString());
                    if (crossoverRate < 0.0 || crossoverRate > 1.0) {
                        result.setValid(false);
                        result.setErrorMessage("交叉率必须在0.0-1.0之间");
                        return result;
                    }
                } catch (NumberFormatException e) {
                    result.setValid(false);
                    result.setErrorMessage("交叉率必须是数字");
                    return result;
                }
            }

            // 验证变异率
            Object mutationRateObj = parameters.get("mutationRate");
            if (mutationRateObj != null) {
                try {
                    double mutationRate = Double.parseDouble(mutationRateObj.toString());
                    if (mutationRate < 0.0 || mutationRate > 1.0) {
                        result.setValid(false);
                        result.setErrorMessage("变异率必须在0.0-1.0之间");
                        return result;
                    }
                } catch (NumberFormatException e) {
                    result.setValid(false);
                    result.setErrorMessage("变异率必须是数字");
                    return result;
                }
            }

            result.setValid(true);
            result.addValidationDetail("validatedParameters", parameters);

        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMessage("参数验证异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 估算算法执行时间
     */
    @Override
    public long estimateExecutionTime(ScheduleData scheduleData) {
        int employeeCount = scheduleData.getEmployees().size();
        int popSize = getPopulationSize();
        int maxGenerations = getMaxGenerations();

        // 基础时间：每个个体评估1毫秒
        long baseTime = popSize * 1L;

        // 进化时间：每代1毫秒
        long evolutionTime = maxGenerations * 1L;

        // 总时间
        long totalTime = baseTime + evolutionTime;

        // 根据员工数量调整
        totalTime = totalTime * (1 + employeeCount / 50);

        return Math.min(totalTime, getTimeLimit());
    }

    /**
     * 获取算法复杂度
     */
    @Override
    public ScheduleAlgorithm.AlgorithmComplexity getComplexity() {
        return new ScheduleAlgorithm.AlgorithmComplexity("O(G*P*N)", "O(G*P)");
    }

    /**
     * 检查算法是否适用于指定的数据规模
     */
    @Override
    public boolean isApplicable(int employeeCount, int shiftCount, int timeRange) {
        // 遗传算法适用于中小规模数据，能找到较优解
        return employeeCount >= 20 && employeeCount <= 500 &&
                shiftCount >= 1 && shiftCount <= 50 &&
                timeRange >= 1 && timeRange <= 90;
    }

    /**
     * 获取算法适用场景
     */
    @Override
    public List<String> getApplicationScenarios() {
        return Arrays.asList(
                "复杂约束排班",
                "多目标优化",
                "大规模排班问题",
                "质量敏感排班",
                "长期排班规划");
    }

    /**
     * 设置算法回调
     */
    @Override
    public void setCallback(AlgorithmCallback callback) {
        this.callback = callback;
    }

    /**
     * 获取算法状态
     */
    @Override
    public AlgorithmStatus getStatus() {
        return status;
    }

    /**
     * 停止算法执行
     */
    @Override
    public void stop() {
        shouldStop = true;
        status = AlgorithmStatus.STOPPED;
        log.info("[遗传算法] 停止执行");
    }

    /**
     * 暂停算法执行
     */
    @Override
    public void pause() {
        shouldPause = true;
        status = AlgorithmStatus.PAUSED;
        log.info("[遗传算法] 暂停执行");
    }

    /**
     * 恢复算法执行
     */
    @Override
    public void resume() {
        shouldPause = false;
        status = AlgorithmStatus.RUNNING;
        log.info("[遗传算法] 恢复执行");
    }

    /**
     * 获取算法类型
     */
    @Override
    public String getAlgorithmType() {
        return "GENETIC";
    }

    /**
     * 获取算法名称
     */
    @Override
    public String getAlgorithmName() {
        return "遗传排班算法";
    }

    /**
     * 获取算法描述
     */
    @Override
    public String getAlgorithmDescription() {
        return "使用遗传算法模拟自然选择过程，通过选择、交叉、变异操作逐步优化排班方案";
    }

    /**
     * 获取算法元数据
     */
    @Override
    public AlgorithmMetadata getMetadata() {
        return AlgorithmMetadata.builder()
                .name(getAlgorithmName())
                .version("1.0.0")
                .description(getAlgorithmDescription())
                .author("IOE-DREAM架构团队")
                .createdDate(LocalDateTime.now())
                .parameters(Arrays.asList(
                        AlgorithmMetadata.AlgorithmParameter.builder()
                                .name("populationSize")
                                .displayName("种群大小")
                                .type("Integer")
                                .defaultValue(DEFAULT_POPULATION_SIZE)
                                .minValue(10)
                                .maxValue(1000)
                                .description("遗传算法种群大小")
                                .required(false)
                                .build(),
                        AlgorithmMetadata.AlgorithmParameter.builder()
                                .name("maxGenerations")
                                .displayName("最大进化代数")
                                .type("Integer")
                                .defaultValue(DEFAULT_MAX_GENERATIONS)
                                .minValue(1)
                                .maxValue(10000)
                                .description("最大进化代数")
                                .required(false)
                                .build(),
                        AlgorithmMetadata.AlgorithmParameter.builder()
                                .name("crossoverRate")
                                .displayName("交叉率")
                                .type("Double")
                                .defaultValue(DEFAULT_CROSSOVER_RATE)
                                .minValue(0.0)
                                .maxValue(1.0)
                                .description("交叉概率")
                                .required(false)
                                .build(),
                        AlgorithmMetadata.AlgorithmParameter.builder()
                                .name("mutationRate")
                                .displayName("变异率")
                                .type("Double")
                                .defaultValue(DEFAULT_MUTATION_RATE)
                                .minValue(0.0)
                                .maxValue(1.0)
                                .description("变异概率")
                                .required(false)
                                .build()))
                .build();
    }

    /**
     * 验证输入数据
     */
    private boolean validateInputData(ScheduleData scheduleData) {
        if (scheduleData == null) {
            log.error("[遗传算法] 排班数据为空");
            return false;
        }

        if (scheduleData.getEmployees() == null || scheduleData.getEmployees().isEmpty()) {
            log.error("[遗传算法] 员工数据为空");
            return false;
        }

        if (scheduleData.getAvailableShifts() == null || scheduleData.getAvailableShifts().isEmpty()) {
            log.error("[遗传算法] 班次数据为空");
            return false;
        }

        return true;
    }

    /**
     * 获取种群大小
     */
    private int getPopulationSize() {
        Object value = parameters.get("populationSize");
        if (value != null) {
            return Integer.parseInt(value.toString());
        }
        return DEFAULT_POPULATION_SIZE;
    }

    /**
     * 获取最大进化代数
     */
    private int getMaxGenerations() {
        Object value = parameters.get("maxGenerations");
        if (value != null) {
            return Integer.parseInt(value.toString());
        }
        return DEFAULT_MAX_GENERATIONS;
    }

    /**
     * 获取交叉率
     */
    private double getCrossoverRate() {
        Object value = parameters.get("crossoverRate");
        if (value != null) {
            return Double.parseDouble(value.toString());
        }
        return DEFAULT_CROSSOVER_RATE;
    }

    /**
     * 获取变异率
     */
    private double getMutationRate() {
        Object value = parameters.get("mutationRate");
        if (value != null) {
            return Double.parseDouble(value.toString());
        }
        return DEFAULT_MUTATION_RATE;
    }

    /**
     * 获取精英保留率
     */
    private double getEliteRate() {
        Object value = parameters.get("eliteRate");
        if (value != null) {
            return Double.parseDouble(value.toString());
        }
        return DEFAULT_ELITE_RATE;
    }

    /**
     * 获取时间限制
     */
    private long getTimeLimit() {
        Object value = parameters.get("timeLimit");
        if (value != null) {
            return Long.parseLong(value.toString());
        }
        return DEFAULT_TIME_LIMIT;
    }

    /**
     * 初始化种群
     */
    private List<Chromosome> initializePopulation(ScheduleData scheduleData, int popSize) {
        List<Chromosome> population = new ArrayList<>();

        for (int i = 0; i < popSize; i++) {
            Chromosome chromosome = createRandomChromosome(scheduleData);
            chromosome.setId(i);
            population.add(chromosome);
        }

        log.debug("[遗传算法] 初始化种群完成，大小: {}", popSize);
        return population;
    }

    /**
     * 创建随机染色体
     */
    private Chromosome createRandomChromosome(ScheduleData scheduleData) {
        Chromosome chromosome = new Chromosome();

        // 生成随机基因序列
        List<Gene> genes = new ArrayList<>();
        List<ScheduleData.EmployeeData> employees = scheduleData.getEmployees();
        List<ScheduleData.ShiftData> shifts = scheduleData.getAvailableShifts();

        // TODO: 根据实际业务需求生成随机基因
        for (int i = 0; i < employees.size(); i++) {
            Gene gene = new Gene();
            gene.setEmployeeId(getEmployeeId(employees.get(i)));
            gene.setShiftId(getShiftId(shifts.get(ThreadLocalRandom.current().nextInt(shifts.size()))));
            genes.add(gene);
        }

        chromosome.setGenes(genes);
        chromosome.setFitness(0.0);

        return chromosome;
    }

    /**
     * 评估种群适应度
     */
    private void evaluatePopulation(List<Chromosome> population, ScheduleData scheduleData) {
        for (Chromosome chromosome : population) {
            double fitness = calculateFitness(chromosome, scheduleData);
            chromosome.setFitness(fitness);
        }
    }

    /**
     * 计算适应度
     */
    private double calculateFitness(Chromosome chromosome, ScheduleData scheduleData) {
        // 多目标适应度函数
        double fitness = 0.0;

        // 1. 约束违反惩罚
        double penalty = calculateConstraintViolation(chromosome, scheduleData);
        fitness -= penalty * 1000;

        // 2. 公平性奖励
        double fairness = calculateFairness(chromosome);
        fitness += fairness * 100;

        // 3. 效率奖励
        double efficiency = calculateEfficiency(chromosome, scheduleData);
        fitness += efficiency * 50;

        // 4. 成本奖励
        double cost = calculateCost(chromosome, scheduleData);
        fitness -= cost * 0.1;

        return fitness;
    }

    /**
     * 计算约束违反
     */
    private double calculateConstraintViolation(Chromosome chromosome, ScheduleData scheduleData) {
        double violations = 0.0;

        // TODO: 实现约束违反计算逻辑
        // - 时间冲突
        // - 技能匹配
        // - 工作时长限制
        // - 法规合规

        return violations;
    }

    /**
     * 计算公平性
     */
    private double calculateFairness(Chromosome chromosome) {
        // TODO: 实现公平性计算
        return Math.random();
    }

    /**
     * 计算效率
     */
    private double calculateEfficiency(Chromosome chromosome, ScheduleData scheduleData) {
        // TODO: 实现效率计算
        return Math.random();
    }

    /**
     * 计算成本
     */
    private double calculateCost(Chromosome chromosome, ScheduleData scheduleData) {
        // TODO: 实现成本计算
        return Math.random() * 1000;
    }

    /**
     * 找出最佳染色体
     */
    private Chromosome findBestChromosome(List<Chromosome> population) {
        return population.stream()
                .max(Comparator.comparingDouble(Chromosome::getFitness))
                .orElse(null);
    }

    /**
     * 选择操作（轮盘赌选择）
     */
    private List<Chromosome> selection(List<Chromosome> population) {
        List<Chromosome> selected = new ArrayList<>();

        // 计算累积适应度
        double totalFitness = population.stream()
                .mapToDouble(Chromosome::getFitness)
                .sum();

        if (totalFitness <= 0) {
            // 如果所有适应度都为负，使用随机选择
            return new ArrayList<>(population);
        }

        // 轮盘赌选择
        for (int i = 0; i < population.size(); i++) {
            double randomValue = Math.random() * totalFitness;
            double cumulativeFitness = 0.0;

            for (Chromosome chromosome : population) {
                cumulativeFitness += chromosome.getFitness();
                if (cumulativeFitness >= randomValue) {
                    selected.add(chromosome);
                    break;
                }
            }
        }

        return selected;
    }

    /**
     * 交叉操作
     */
    private List<Chromosome> crossover(List<Chromosome> population, double crossoverRate) {
        List<Chromosome> offspring = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < population.size(); i += 2) {
            Chromosome parent1 = population.get(i);
            Chromosome parent2 = population.get((i + 1) % population.size());

            if (random.nextDouble() < crossoverRate) {
                // 执行交叉
                Chromosome child1 = parent1.clone();
                Chromosome child2 = parent2.clone();

                crossoverChromosomes(child1, child2);

                offspring.add(child1);
                offspring.add(child2);
            } else {
                // 直接复制
                offspring.add(parent1.clone());
                offspring.add(parent2.clone());
            }
        }

        return offspring;
    }

    /**
     * 交叉两个染色体
     */
    private void crossoverChromosomes(Chromosome child1, Chromosome child2) {
        List<Gene> genes1 = child1.getGenes();
        List<Gene> genes2 = child2.getGenes();

        if (genes1.size() < 2 || genes2.size() < 2) {
            return;
        }

        // 单点交叉
        int crossoverPoint = ThreadLocalRandom.current().nextInt(Math.min(genes1.size(), genes2.size()) - 1) + 1;

        // 交换交叉点后的基因
        for (int i = crossoverPoint; i < Math.min(genes1.size(), genes2.size()); i++) {
            Gene temp = genes1.get(i);
            genes1.set(i, genes2.get(i));
            genes2.set(i, temp);
        }
    }

    /**
     * 变异操作
     */
    private void mutate(List<Chromosome> population, double mutationRate) {
        Random random = new Random();

        for (Chromosome chromosome : population) {
            for (Gene gene : chromosome.getGenes()) {
                if (random.nextDouble() < mutationRate) {
                    // 执行变异
                    mutateGene(gene);
                }
            }
        }
    }

    /**
     * 变异基因
     */
    private void mutateGene(Gene gene) {
        // TODO: 根据业务需求实现基因变异逻辑
        // 随机改变班次分配
    }

    /**
     * 精英保留策略
     */
    private List<Chromosome> elitism(List<Chromosome> oldPopulation, List<Chromosome> offspring, double eliteRate) {
        int eliteSize = (int) (oldPopulation.size() * eliteRate);

        // 按适应度排序，保留精英个体
        List<Chromosome> sortedPopulation = oldPopulation.stream()
                .sorted(Comparator.comparingDouble(Chromosome::getFitness).reversed())
                .collect(Collectors.toList());

        List<Chromosome> eliteChromosomes = sortedPopulation.subList(0, eliteSize);

        // 组合新种群
        List<Chromosome> newPopulation = new ArrayList<>(eliteChromosomes);

        // 填充剩余位置
        int remainingSize = oldPopulation.size() - eliteSize;
        for (int i = 0; i < remainingSize && i < offspring.size(); i++) {
            newPopulation.add(offspring.get(i));
        }

        return newPopulation;
    }

    /**
     * 将染色体转换为排班记录
     */
    private List<ScheduleRecord> convertChromosomeToSchedule(Chromosome chromosome, ScheduleData scheduleData) {
        List<ScheduleRecord> scheduleRecords = new ArrayList<>();

        for (Gene gene : chromosome.getGenes()) {
            ScheduleRecord record = new ScheduleRecord();
            record.setEmployeeId(gene.getEmployeeId());
            record.setShiftId(gene.getShiftId());
            record.setAssignedTime(LocalDateTime.now());
            record.setAlgorithmUsed(getAlgorithmType());
            record.setFitnessScore(chromosome.getFitness());
            scheduleRecords.add(record);
        }

        return scheduleRecords;
    }

    /**
     * 计算质量评分
     */
    private double calculateQualityScore(Chromosome chromosome) {
        if (chromosome == null) {
            return 0.0;
        }

        // 将适应度转换为0-100的质量评分
        double maxPossibleFitness = 1000.0; // 假设的最大可能适应度
        double minFitness = -1000.0; // 假设的最小适应度

        double normalizedFitness = (chromosome.getFitness() - minFitness) / (maxPossibleFitness - minFitness);
        return Math.max(0.0, Math.min(100.0, normalizedFitness * 100));
    }

    /**
     * 更新进度
     */
    private void updateProgress(double progress, String phase) {
        if (callback != null) {
            AlgorithmProgress progressInfo = new AlgorithmProgress(progress, phase);
            progressInfo.setMessage(String.format("进化进度: %.1f%% - %s", progress * 100, phase));
            callback.onProgress(progressInfo);
        }
    }

    /**
     * 创建错误结果
     */
    private ScheduleResult createErrorResult(String errorMessage) {
        return ScheduleResult.builder()
                .status("ERROR")
                .message(errorMessage)
                .executionTime(0L)
                .algorithmUsed(getAlgorithmType())
                .build();
    }

    /**
     * 创建排班统计信息
     */
    private Map<String, Object> createScheduleStatistics(Map<String, Object> statistics) {
        Map<String, Object> scheduleStats = new HashMap<>();
        scheduleStats.put("algorithmType", getAlgorithmType());
        scheduleStats.put("algorithmName", getAlgorithmName());
        scheduleStats.put("totalGenerations", statistics.get("totalGenerations"));
        scheduleStats.put("populationSize", statistics.get("populationSize"));
        scheduleStats.put("bestFitness", statistics.get("bestFitness"));
        scheduleStats.put("crossoverRate", statistics.get("crossoverRate"));
        scheduleStats.put("mutationRate", statistics.get("mutationRate"));
        scheduleStats.put("eliteRate", statistics.get("eliteRate"));
        return scheduleStats;
    }

    /**
     * 获取员工ID
     */
    private Long getEmployeeId(ScheduleData.EmployeeData employee) {
        if (employee == null) {
            return null;
        }
        return employee.getEmployeeId();
    }

    /**
     * 获取班次ID
     */
    private Long getShiftId(ScheduleData.ShiftData shift) {
        if (shift == null) {
            return null;
        }
        return shift.getShiftId();
    }

    /**
     * 转换排班记录为ScheduleResult内部记录类型
     *
     * @param record 排班记录
     * @return 排班结果内部记录
     */
    private ScheduleResult.ScheduleRecord convertToResultScheduleRecord(ScheduleRecord record) {
        if (record == null) {
            return null;
        }
        return ScheduleResult.ScheduleRecord.builder()
                .recordId(record.getRecordId())
                .userId(record.getEmployeeId())
                .departmentId(record.getDepartmentId())
                .shiftId(record.getShiftId())
                .scheduleDate(record.getScheduleDate())
                .startTime(record.getStartTime() != null ? record.getStartTime().toString() : null)
                .endTime(record.getEndTime() != null ? record.getEndTime().toString() : null)
                .workLocation(record.getWorkLocation())
                .priority(record.getPriority())
                .autoGenerated(record.getAutoGenerated())
                .source(record.getSource())
                .status(record.getStatus())
                .assignedTime(record.getAssignedTime())
                .algorithmUsed(record.getAlgorithmUsed())
                .fitnessScore(record.getFitnessScore())
                .build();
    }

    /**
     * 染色体内部类
     */
    private static class Chromosome implements Cloneable {
        private int id;
        private List<Gene> genes;
        private double fitness;

        public Chromosome() {
            this.genes = new ArrayList<>();
        }

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<Gene> getGenes() {
            return genes;
        }

        public void setGenes(List<Gene> genes) {
            this.genes = genes;
        }

        public double getFitness() {
            return fitness;
        }

        public void setFitness(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public Chromosome clone() {
            try {
                Chromosome cloned = (Chromosome) super.clone();
                cloned.genes = new ArrayList<>(this.genes);
                return cloned;
            } catch (CloneNotSupportedException e) {
                // 降级到手动复制
                Chromosome cloned = new Chromosome();
                cloned.setId(this.id);
                cloned.setFitness(this.fitness);
                cloned.setGenes(new ArrayList<>(this.genes));
                return cloned;
            }
        }
    }

    /**
     * 基因内部类
     */
    private static class Gene {
        private Long employeeId;
        private Long shiftId;

        // Getters and Setters
        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public Long getShiftId() {
            return shiftId;
        }

        public void setShiftId(Long shiftId) {
            this.shiftId = shiftId;
        }
    }
}
