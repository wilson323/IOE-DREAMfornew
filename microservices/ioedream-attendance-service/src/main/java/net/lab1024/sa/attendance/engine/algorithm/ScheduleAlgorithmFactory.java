package net.lab1024.sa.attendance.engine.algorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import net.lab1024.sa.attendance.engine.algorithm.impl.BacktrackAlgorithmImpl;
import net.lab1024.sa.attendance.engine.algorithm.impl.GeneticAlgorithmImpl;
import net.lab1024.sa.attendance.engine.algorithm.impl.GreedyAlgorithmImpl;
import net.lab1024.sa.attendance.engine.algorithm.impl.HeuristicAlgorithmImpl;

/**
 * 排班算法工厂
 * <p>
 * 负责创建和管理各种排班算法实例，支持动态注册和配置
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public class ScheduleAlgorithmFactory {

    // 算法注册表
    private final Map<String, AlgorithmProvider> algorithmProviders = new HashMap<>();

    // 算法实例缓存
    private final Map<String, ScheduleAlgorithm> algorithmCache = new HashMap<>();

    // 默认参数配置
    private final Map<String, Map<String, Object>> defaultParameters = new HashMap<>();

    /**
     * 构造函数
     */
    public ScheduleAlgorithmFactory() {
        initializeDefaultAlgorithms();
        initializeDefaultParameters();
    }

    /**
     * 获取指定类型的算法实例
     *
     * @param algorithmType 算法类型
     * @return 算法实例
     */
    public ScheduleAlgorithm getAlgorithm(String algorithmType) {
        return getAlgorithm(algorithmType, null);
    }

    /**
     * 获取指定类型的算法实例（带参数）
     *
     * @param algorithmType 算法类型
     * @param parameters    算法参数
     * @return 算法实例
     */
    public ScheduleAlgorithm getAlgorithm(String algorithmType, Map<String, Object> parameters) {
        String cacheKey = generateCacheKey(algorithmType, parameters);

        // 检查缓存
        ScheduleAlgorithm cachedAlgorithm = algorithmCache.get(cacheKey);
        if (cachedAlgorithm != null) {
            return cachedAlgorithm;
        }

        // 创建新实例
        AlgorithmProvider provider = algorithmProviders.get(algorithmType);
        if (provider == null) {
            throw new IllegalArgumentException("不支持的算法类型: " + algorithmType);
        }

        ScheduleAlgorithm algorithm = provider.createAlgorithm();

        // 合并默认参数和用户参数
        Map<String, Object> mergedParameters = mergeParameters(algorithmType, parameters);

        // 初始化算法
        if (mergedParameters != null && !mergedParameters.isEmpty()) {
            algorithm.initialize(mergedParameters);
        }

        // 缓存算法实例
        algorithmCache.put(cacheKey, algorithm);

        return algorithm;
    }

    /**
     * 注册算法提供者
     *
     * @param algorithmType 算法类型
     * @param provider      算法提供者
     */
    public void registerAlgorithm(String algorithmType, AlgorithmProvider provider) {
        algorithmProviders.put(algorithmType, provider);
    }

    /**
     * 注销算法提供者
     *
     * @param algorithmType 算法类型
     */
    public void unregisterAlgorithm(String algorithmType) {
        algorithmProviders.remove(algorithmType);
        // 清除相关缓存
        algorithmCache.entrySet().removeIf(entry -> entry.getKey().startsWith(algorithmType + ":"));
    }

    /**
     * 获取支持的算法类型列表
     *
     * @return 算法类型列表
     */
    public List<String> getSupportedAlgorithms() {
        return List.copyOf(algorithmProviders.keySet());
    }

    /**
     * 检查是否支持指定算法类型
     *
     * @param algorithmType 算法类型
     * @return 是否支持
     */
    public boolean isSupported(String algorithmType) {
        return algorithmProviders.containsKey(algorithmType);
    }

    /**
     * 获取算法信息
     *
     * @param algorithmType 算法类型
     * @return 算法信息
     */
    public AlgorithmInfo getAlgorithmInfo(String algorithmType) {
        AlgorithmProvider provider = algorithmProviders.get(algorithmType);
        if (provider == null) {
            return null;
        }

        ScheduleAlgorithm algorithm = provider.createAlgorithm();
        return AlgorithmInfo.builder()
                .algorithmType(algorithmType)
                .algorithmName(algorithm.getAlgorithmName())
                .description(algorithm.getAlgorithmDescription())
                .complexity(algorithm.getComplexity())
                .applicationScenarios(algorithm.getApplicationScenarios())
                .build();
    }

    /**
     * 获取所有算法信息
     *
     * @return 算法信息列表
     */
    public List<AlgorithmInfo> getAllAlgorithmInfo() {
        return algorithmProviders.keySet().stream()
                .map(this::getAlgorithmInfo)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 清除算法缓存
     */
    public void clearCache() {
        algorithmCache.clear();
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    public CacheStatistics getCacheStatistics() {
        return CacheStatistics.builder()
                .totalCached(algorithmCache.size())
                .totalProviders(algorithmProviders.size())
                .cacheKeys(algorithmCache.keySet())
                .build();
    }

    /**
     * 设置默认参数
     *
     * @param algorithmType 算法类型
     * @param parameters    参数配置
     */
    public void setDefaultParameters(String algorithmType, Map<String, Object> parameters) {
        defaultParameters.put(algorithmType, parameters);
    }

    /**
     * 获取默认参数
     *
     * @param algorithmType 算法类型
     * @return 参数配置
     */
    public Map<String, Object> getDefaultParameters(String algorithmType) {
        return defaultParameters.getOrDefault(algorithmType, new HashMap<>());
    }

    /**
     * 初始化默认算法
     */
    private void initializeDefaultAlgorithms() {
        // 注册贪心算法
        registerAlgorithm("GREEDY", new GreedyAlgorithmProvider());

        // 注册遗传算法
        registerAlgorithm("GENETIC", new GeneticAlgorithmProvider());

        // 注册回溯算法
        registerAlgorithm("BACKTRACK", new BacktrackAlgorithmProvider());

        // 注册启发式算法
        registerAlgorithm("HEURISTIC", new HeuristicAlgorithmProvider());
    }

    /**
     * 初始化默认参数
     */
    private void initializeDefaultParameters() {
        // 贪心算法默认参数
        Map<String, Object> greedyParams = new HashMap<>();
        greedyParams.put("priorityStrategy", "FAIRNESS");
        greedyParams.put("maxIterations", 1000);
        greedyParams.put("timeLimit", 30000);
        defaultParameters.put("GREEDY", greedyParams);

        // 遗传算法默认参数
        Map<String, Object> geneticParams = new HashMap<>();
        geneticParams.put("populationSize", 100);
        geneticParams.put("maxGenerations", 500);
        geneticParams.put("crossoverRate", 0.8);
        geneticParams.put("mutationRate", 0.1);
        geneticParams.put("eliteRate", 0.1);
        geneticParams.put("timeLimit", 60000);
        defaultParameters.put("GENETIC", geneticParams);

        // 回溯算法默认参数
        Map<String, Object> backtrackParams = new HashMap<>();
        backtrackParams.put("maxDepth", 10);
        backtrackParams.put("pruningStrategy", "FORWARD_CHECKING");
        backtrackParams.put("timeLimit", 45000);
        defaultParameters.put("BACKTRACK", backtrackParams);

        // 启发式算法默认参数
        Map<String, Object> heuristicParams = new HashMap<>();
        heuristicParams.put("heuristicFunction", "LEAST_CONFLICTING");
        heuristicParams.put("maxIterations", 2000);
        heuristicParams.put("timeLimit", 40000);
        defaultParameters.put("HEURISTIC", heuristicParams);
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String algorithmType, Map<String, Object> parameters) {
        StringBuilder keyBuilder = new StringBuilder(algorithmType);
        if (parameters != null && !parameters.isEmpty()) {
            keyBuilder.append(":");
            parameters.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> keyBuilder.append(entry.getKey()).append("=").append(entry.getValue())
                            .append(";"));
        }
        return keyBuilder.toString();
    }

    /**
     * 合并参数
     */
    private Map<String, Object> mergeParameters(String algorithmType, Map<String, Object> userParameters) {
        Map<String, Object> merged = new HashMap<>(getDefaultParameters(algorithmType));
        if (userParameters != null) {
            merged.putAll(userParameters);
        }
        return merged;
    }

    /**
     * 算法提供者接口
     */
    @FunctionalInterface
    public interface AlgorithmProvider {
        ScheduleAlgorithm createAlgorithm();
    }

    /**
     * 算法信息类
     */
    @Getter
    @Setter
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlgorithmInfo {
        private String algorithmType;
        private String algorithmName;
        private String description;
        private ScheduleAlgorithm.AlgorithmComplexity complexity;
        private java.util.List<String> applicationScenarios;
    }

    /**
     * 缓存统计类
     */
    @Getter
    @Setter
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheStatistics {
        private int totalCached;
        private int totalProviders;
        private java.util.Set<String> cacheKeys;
    }

    // 算法提供者实现类
    private static class GreedyAlgorithmProvider implements AlgorithmProvider {
        @Override
        public ScheduleAlgorithm createAlgorithm() {
            return new GreedyAlgorithmImpl();
        }
    }

    private static class GeneticAlgorithmProvider implements AlgorithmProvider {
        @Override
        public ScheduleAlgorithm createAlgorithm() {
            return new GeneticAlgorithmImpl();
        }
    }

    private static class BacktrackAlgorithmProvider implements AlgorithmProvider {
        @Override
        public ScheduleAlgorithm createAlgorithm() {
            return new BacktrackAlgorithmImpl();
        }
    }

    private static class HeuristicAlgorithmProvider implements AlgorithmProvider {
        @Override
        public ScheduleAlgorithm createAlgorithm() {
            return new HeuristicAlgorithmImpl();
        }
    }
}
