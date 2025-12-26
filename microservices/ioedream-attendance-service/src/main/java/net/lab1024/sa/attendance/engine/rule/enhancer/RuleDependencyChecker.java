package net.lab1024.sa.attendance.engine.rule.enhancer;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.rule.loader.RuleLoader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 考勤规则依赖检查器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * Manager层负责业务编排和复杂业务逻辑处理
 * </p>
 * <p>
 * 核心职责：
 * - 检查规则之间的依赖关系
 * - 检测循环依赖
 * - 验证依赖完整性
 * - 生成依赖关系图
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class RuleDependencyChecker {

    private final RuleLoader ruleLoader;

    // 依赖关系缓存
    private final Map<Long, Set<Long>> dependencyCache = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> reverseDependencyCache = new ConcurrentHashMap<>();
    private LocalDateTime lastCacheUpdateTime;

    /**
     * 构造函数注入依赖
     */
    public RuleDependencyChecker(RuleLoader ruleLoader) {
        this.ruleLoader = ruleLoader;
        log.info("[规则依赖检查] 规则依赖检查器初始化完成");
    }

    /**
     * 检查所有规则的依赖关系
     */
    public DependencyCheckResult checkAllDependencies() {
        log.info("[规则依赖检查] 开始检查所有规则依赖");

        DependencyCheckResult result = new DependencyCheckResult();
        result.setCheckTime(LocalDateTime.now());

        try {
            // 1. 重新加载依赖关系
            reloadDependencyGraph();

            // 2. 检查循环依赖
            List<List<Long>> cycles = detectCyclicDependencies();
            if (!cycles.isEmpty()) {
                result.setHasCyclicDependency(true);
                result.setCyclicDependencies(cycles);
                log.warn("[规则依赖检查] 检测到循环依赖，数量: {}", cycles.size());
            } else {
                result.setHasCyclicDependency(false);
                log.info("[规则依赖检查] 未检测到循环依赖");
            }

            // 3. 检查缺失依赖
            List<Long> missingDependencies = checkMissingDependencies();
            if (!missingDependencies.isEmpty()) {
                result.setHasMissingDependency(true);
                result.setMissingDependencies(missingDependencies);
                log.warn("[规则依赖检查] 检测到缺失依赖，数量: {}", missingDependencies.size());
            } else {
                result.setHasMissingDependency(false);
                log.info("[规则依赖检查] 未检测到缺失依赖");
            }

            // 4. 统计依赖关系
            result.setTotalRules(dependencyCache.size());
            result.setTotalDependencies(calculateTotalDependencies());

            boolean validCheck = !result.isHasCyclicDependency() && !result.isHasMissingDependency();
            result.setValid(validCheck);

            if (validCheck) {
                log.info("[规则依赖检查] 依赖关系检查通过，总规则数: {}, 总依赖数: {}",
                        result.getTotalRules(), result.getTotalDependencies());
            } else {
                log.error("[规则依赖检查] 依赖关系检查失败，存在循环依赖或缺失依赖");
            }

        } catch (Exception e) {
            log.error("[规则依赖检查] 依赖关系检查异常", e);
            result.setValid(false);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * 检查单个规则的依赖
     */
    public RuleDependencyInfo checkRuleDependency(Long ruleId) {
        log.debug("[规则依赖检查] 检查规则依赖: ruleId={}", ruleId);

        RuleDependencyInfo info = new RuleDependencyInfo();
        info.setRuleId(ruleId);

        try {
            // 1. 获取规则配置
            Map<String, Object> ruleConfig = ruleLoader.loadRuleConfig(ruleId);
            if (ruleConfig == null) {
                info.setValid(false);
                info.setErrorMessage("规则不存在");
                return info;
            }

            // 2. 获取依赖的规则
            Set<Long> dependencies = dependencyCache.getOrDefault(ruleId, Collections.emptySet());
            info.setDependencies(dependencies);

            // 3. 获取依赖此规则的其他规则
            Set<Long> dependents = reverseDependencyCache.getOrDefault(ruleId, Collections.emptySet());
            info.setDependents(dependents);

            // 4. 检查依赖是否存在
            Set<Long> missingDeps = new HashSet<>();
            for (Long depId : dependencies) {
                if (!dependencyCache.containsKey(depId)) {
                    missingDeps.add(depId);
                }
            }

            if (!missingDeps.isEmpty()) {
                info.setValid(false);
                info.setMissingDependencies(missingDeps);
                log.warn("[规则依赖检查] 规则存在缺失依赖: ruleId={}, missing={}", ruleId, missingDeps);
            } else {
                info.setValid(true);
                log.debug("[规则依赖检查] 规则依赖检查通过: ruleId={}", ruleId);
            }

        } catch (Exception e) {
            log.error("[规则依赖检查] 检查规则依赖异常: ruleId={}", ruleId, e);
            info.setValid(false);
            info.setErrorMessage(e.getMessage());
        }

        return info;
    }

    /**
     * 获取规则的所有依赖（直接和间接）
     */
    public Set<Long> getAllDependencies(Long ruleId) {
        Set<Long> allDependencies = new HashSet<>();
        collectAllDependencies(ruleId, allDependencies, new HashSet<>());
        return allDependencies;
    }

    /**
     * 递归收集所有依赖
     */
    private void collectAllDependencies(Long ruleId, Set<Long> allDependencies, Set<Long> visited) {
        if (visited.contains(ruleId)) {
            // 检测到循环
            return;
        }

        visited.add(ruleId);

        Set<Long> directDependencies = dependencyCache.getOrDefault(ruleId, Collections.emptySet());
        for (Long depId : directDependencies) {
            if (allDependencies.add(depId)) {
                collectAllDependencies(depId, allDependencies, visited);
            }
        }

        visited.remove(ruleId);
    }

    /**
     * 检测循环依赖
     */
    private List<List<Long>> detectCyclicDependencies() {
        List<List<Long>> cycles = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();

        for (Long ruleId : dependencyCache.keySet()) {
            if (!visited.contains(ruleId)) {
                List<Long> cycle = new ArrayList<>();
                if (detectCycleDFS(ruleId, visited, recursionStack, cycle)) {
                    cycles.add(cycle);
                }
            }
        }

        return cycles;
    }

    /**
     * DFS检测循环
     */
    private boolean detectCycleDFS(Long ruleId, Set<Long> visited, Set<Long> recursionStack, List<Long> cycle) {
        visited.add(ruleId);
        recursionStack.add(ruleId);

        Set<Long> dependencies = dependencyCache.getOrDefault(ruleId, Collections.emptySet());
        for (Long depId : dependencies) {
            if (!visited.contains(depId)) {
                cycle.add(ruleId);
                if (detectCycleDFS(depId, visited, recursionStack, cycle)) {
                    return true;
                }
                cycle.remove(cycle.size() - 1);
            } else if (recursionStack.contains(depId)) {
                // 找到循环
                int startIndex = cycle.indexOf(depId);
                if (startIndex >= 0) {
                    cycle.add(ruleId);
                    return true;
                }
            }
        }

        recursionStack.remove(ruleId);
        return false;
    }

    /**
     * 检查缺失依赖
     */
    private List<Long> checkMissingDependencies() {
        List<Long> missing = new ArrayList<>();

        for (Map.Entry<Long, Set<Long>> entry : dependencyCache.entrySet()) {
            Long ruleId = entry.getKey();
            Set<Long> dependencies = entry.getValue();

            for (Long depId : dependencies) {
                if (!dependencyCache.containsKey(depId)) {
                    missing.add(depId);
                }
            }
        }

        return missing;
    }

    /**
     * 重新加载依赖关系图
     */
    private void reloadDependencyGraph() {
        log.debug("[规则依赖检查] 重新加载依赖关系图");

        dependencyCache.clear();
        reverseDependencyCache.clear();

        try {
            // 1. 加载所有启用的规则
            List<Long> allRuleIds = ruleLoader.loadAllActiveRules();

            // 2. 为每个规则加载依赖
            for (Long ruleId : allRuleIds) {
                Map<String, Object> ruleConfig = ruleLoader.loadRuleConfig(ruleId);
                if (ruleConfig == null) {
                    continue;
                }

                // 3. 解析依赖配置
                Set<Long> dependencies = parseDependencies(ruleConfig);
                dependencyCache.put(ruleId, dependencies);

                // 4. 构建反向依赖
                for (Long depId : dependencies) {
                    reverseDependencyCache.computeIfAbsent(depId, k -> new HashSet<>()).add(ruleId);
                }
            }

            lastCacheUpdateTime = LocalDateTime.now();
            log.info("[规则依赖检查] 依赖关系图加载完成，规则数: {}", dependencyCache.size());

        } catch (Exception e) {
            log.error("[规则依赖检查] 加载依赖关系图失败", e);
        }
    }

    /**
     * 解析规则依赖配置
     */
    private Set<Long> parseDependencies(Map<String, Object> ruleConfig) {
        Set<Long> dependencies = new HashSet<>();

        Object dependsOn = ruleConfig.get("dependsOn");
        if (dependsOn instanceof List) {
            List<?> depList = (List<?>) dependsOn;
            for (Object dep : depList) {
                if (dep instanceof Number) {
                    dependencies.add(((Number) dep).longValue());
                }
            }
        } else if (dependsOn instanceof Number) {
            dependencies.add(((Number) dependsOn).longValue());
        }

        return dependencies;
    }

    /**
     * 计算总依赖数
     */
    private int calculateTotalDependencies() {
        int total = 0;
        for (Set<Long> deps : dependencyCache.values()) {
            total += deps.size();
        }
        return total;
    }

    /**
     * 清除依赖缓存
     */
    public void clearDependencyCache() {
        log.info("[规则依赖检查] 清除依赖关系缓存");
        dependencyCache.clear();
        reverseDependencyCache.clear();
        lastCacheUpdateTime = null;
    }

    // ==================== 内部类 ====================

    /**
     * 依赖检查结果
     */
    public static class DependencyCheckResult {
        private LocalDateTime checkTime;
        private boolean valid;
        private boolean hasCyclicDependency;
        private boolean hasMissingDependency;
        private List<List<Long>> cyclicDependencies;
        private List<Long> missingDependencies;
        private int totalRules;
        private int totalDependencies;
        private String errorMessage;

        // Getters and Setters
        public LocalDateTime getCheckTime() {
            return checkTime;
        }

        public void setCheckTime(LocalDateTime checkTime) {
            this.checkTime = checkTime;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public boolean isHasCyclicDependency() {
            return hasCyclicDependency;
        }

        public void setHasCyclicDependency(boolean hasCyclicDependency) {
            this.hasCyclicDependency = hasCyclicDependency;
        }

        public boolean isHasMissingDependency() {
            return hasMissingDependency;
        }

        public void setHasMissingDependency(boolean hasMissingDependency) {
            this.hasMissingDependency = hasMissingDependency;
        }

        public List<List<Long>> getCyclicDependencies() {
            return cyclicDependencies;
        }

        public void setCyclicDependencies(List<List<Long>> cyclicDependencies) {
            this.cyclicDependencies = cyclicDependencies;
        }

        public List<Long> getMissingDependencies() {
            return missingDependencies;
        }

        public void setMissingDependencies(List<Long> missingDependencies) {
            this.missingDependencies = missingDependencies;
        }

        public int getTotalRules() {
            return totalRules;
        }

        public void setTotalRules(int totalRules) {
            this.totalRules = totalRules;
        }

        public int getTotalDependencies() {
            return totalDependencies;
        }

        public void setTotalDependencies(int totalDependencies) {
            this.totalDependencies = totalDependencies;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        /**
         * 生成文本报告
         */
        public String generateTextReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n==================== 规则依赖检查报告 ====================\n");
            sb.append(String.format("检查时间: %s\n", checkTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            sb.append(String.format("检查结果: %s\n", valid ? "通过" : "失败"));
            sb.append(String.format("总规则数: %d\n", totalRules));
            sb.append(String.format("总依赖数: %d\n", totalDependencies));

            if (hasCyclicDependency) {
                sb.append("\n【循环依赖】\n");
                for (int i = 0; i < cyclicDependencies.size(); i++) {
                    List<Long> cycle = cyclicDependencies.get(i);
                    sb.append(String.format("%d. %s\n", i + 1, cycle.toString()));
                }
            }

            if (hasMissingDependency) {
                sb.append("\n【缺失依赖】\n");
                sb.append(missingDependencies.toString()).append("\n");
            }

            if (errorMessage != null) {
                sb.append("\n【错误信息】\n");
                sb.append(errorMessage).append("\n");
            }

            sb.append("========================================================\n");
            return sb.toString();
        }
    }

    /**
     * 单个规则依赖信息
     */
    public static class RuleDependencyInfo {
        private Long ruleId;
        private boolean valid;
        private Set<Long> dependencies;
        private Set<Long> dependents;
        private Set<Long> missingDependencies;
        private String errorMessage;

        // Getters and Setters
        public Long getRuleId() {
            return ruleId;
        }

        public void setRuleId(Long ruleId) {
            this.ruleId = ruleId;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public Set<Long> getDependencies() {
            return dependencies;
        }

        public void setDependencies(Set<Long> dependencies) {
            this.dependencies = dependencies;
        }

        public Set<Long> getDependents() {
            return dependents;
        }

        public void setDependents(Set<Long> dependents) {
            this.dependents = dependents;
        }

        public Set<Long> getMissingDependencies() {
            return missingDependencies;
        }

        public void setMissingDependencies(Set<Long> missingDependencies) {
            this.missingDependencies = missingDependencies;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
