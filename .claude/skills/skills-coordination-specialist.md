# 🤝 技能协同协调专家

> **版本**: v1.0.0 - 技能协同治理
> **更新时间**: 2025-11-23
> **分类**: 技能管理技能 > 协同机制
> **标签**: ["技能协同", "工作流编排", "智能调度", "效果叠加"]
> **技能等级**: ★★★ 专家级
> **适用角色**: AI系统架构师、技能体系设计师、DevOps专家

---

## 📋 技能概述

本技能专门解决多个专家技能之间的协同工作问题，建立高效的技能编排和调度机制，确保技能组合使用时产生1+1>2的效果。

**核心能力**: 将独立的专家技能组织成协同工作的技能网络，实现复杂任务的自动化分解和智能调度。

## 🚨 当前技能协同问题分析

### 1. 技能调用顺序混乱
**问题现象**:
```bash
# 用户随意调用技能，没有考虑依赖关系
Skill("code-quality-protector")      # 先调用了质量检查
Skill("spring-boot-jakarta-guardian") # 后调用了基础修复
# 结果：基础修复后质量检查结果失效
```

**根本原因**:
- 缺乏技能调用依赖关系定义
- 没有智能的技能编排算法
- 用户需要手动规划技能调用顺序

### 2. 技能效果冲突
**问题现象**:
```bash
# 编译错误预防专家优化了代码结构
Skill("compilation-error-prevention-specialist")

# 代码质量守护专家又进行了风格检查
Skill("code-quality-protector")
# 结果：两个技能对同一代码进行了不同方向的修改
```

**根本原因**:
- 技能之间缺乏协调机制
- 没有效果叠加和冲突检测
- 缺乏技能执行状态共享

### 3. 资源重复消耗
**问题现象**:
```bash
# 每个技能都独立执行编译检查
Skill("spring-boot-jakarta-guardian")    # mvn compile
Skill("code-quality-protector")          # mvn compile
Skill("four-tier-architecture-guardian") # mvn compile
# 结果：同一操作执行了3次，浪费时间
```

## 🛠️ 技能协同机制设计

### 1. 技能依赖关系图
```java
/**
 * 技能依赖关系管理器
 */
@Component
@Slf4j
public class SkillDependencyManager {

    // 技能依赖关系定义
    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();

    // 技能冲突关系定义
    private final Map<String, Set<String>> conflictGraph = new HashMap<>();

    // 技能优先级定义
    private final Map<String, Integer> skillPriorities = new HashMap<>();

    @PostConstruct
    public void initDependencyGraph() {
        // 基础修复技能（优先级1-最高）
        addSkill("spring-boot-jakarta-guardian", 1);
        addDependency("spring-boot-jakarta-guardian", Set.of());

        // 实体建模技能（优先级2）
        addSkill("entity-relationship-modeling-specialist", 2);
        addDependency("entity-relationship-modeling-specialist",
                     Set.of("spring-boot-jakarta-guardian"));

        // 代码质量技能（优先级3）
        addSkill("code-quality-protector", 3);
        addDependency("code-quality-protector",
                     Set.of("spring-boot-jakarta-guardian",
                           "entity-relationship-modeling-specialist"));

        // 架构设计技能（优先级4）
        addSkill("four-tier-architecture-guardian", 4);
        addDependency("four-tier-architecture-guardian",
                     Set.of("spring-boot-jakarta-guardian"));

        // 技术栈统一技能（优先级5）
        addSkill("tech-stack-unification-specialist", 5);
        addDependency("tech-stack-unification-specialist",
                     Set.of("spring-boot-jakarta-guardian",
                           "code-quality-protector"));

        // 自动化检查技能（优先级6-最后）
        addSkill("automated-code-quality-checker", 6);
        addDependency("automated-code-quality-checker",
                     Set.of("spring-boot-jakarta-guardian",
                           "entity-relationship-modeling-specialist",
                           "code-quality-protector",
                           "four-tier-architecture-guardian",
                           "tech-stack-unification-specialist"));

        // 定义技能冲突关系
        addConflict("code-quality-protector", "compilation-error-prevention-specialist");

        log.info("技能依赖关系图初始化完成，包含{}个技能", dependencyGraph.size());
    }

    /**
     * 获取技能执行顺序
     */
    public List<String> getExecutionOrder(Set<String> requestedSkills) {
        List<String> executionOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        // 拓扑排序
        for (String skill : requestedSkills) {
            if (!visited.contains(skill)) {
                topologicalSort(skill, visited, visiting, executionOrder);
            }
        }

        // 检查冲突
        executionOrder = resolveConflicts(executionOrder);

        log.info("技能执行顺序: {}", executionOrder);
        return executionOrder;
    }

    private void topologicalSort(String skill, Set<String> visited,
                                Set<String> visiting, List<String> order) {
        if (visiting.contains(skill)) {
            throw new SkillDependencyException("检测到循环依赖: " + skill);
        }

        if (visited.contains(skill)) {
            return;
        }

        visiting.add(skill);

        // 先处理依赖
        Set<String> dependencies = dependencyGraph.getOrDefault(skill, Set.of());
        for (String dependency : dependencies) {
            topologicalSort(dependency, visited, visiting, order);
        }

        visiting.remove(skill);
        visited.add(skill);
        order.add(skill);
    }

    private List<String> resolveConflicts(List<String> skills) {
        List<String> result = new ArrayList<>();
        Set<String> removedSkills = new HashSet<>();

        for (String skill : skills) {
            Set<String> conflicts = conflictGraph.getOrDefault(skill, Set.of());

            boolean hasConflict = conflicts.stream()
                .anyMatch(conflict -> skills.contains(conflict) && !removedSkills.contains(conflict));

            if (hasConflict) {
                log.warn("技能冲突已解决: {} 与已执行技能冲突，跳过执行", skill);
                removedSkills.add(skill);
            } else {
                result.add(skill);
            }
        }

        return result;
    }

    private void addSkill(String skillName, int priority) {
        skillPriorities.put(skillName, priority);
        dependencyGraph.putIfAbsent(skillName, new HashSet<>());
        conflictGraph.putIfAbsent(skillName, new HashSet<>());
    }

    private void addDependency(String skill, Set<String> dependencies) {
        dependencyGraph.put(skill, new HashSet<>(dependencies));
    }

    private void addConflict(String skill1, String skill2) {
        conflictGraph.get(skill1).add(skill2);
        conflictGraph.get(skill2).add(skill1);
    }
}
```

### 2. 智能技能调度器
```java
/**
 * 智能技能调度器
 */
@Component
@Slf4j
public class IntelligentSkillScheduler {

    @Resource
    private SkillDependencyManager dependencyManager;

    @Resource
    private SkillEffectivenessTracker effectivenessTracker;

    @Resource
    private SkillResourceManager resourceManager;

    /**
     * 智能调度技能执行
     */
    public SkillExecutionResult scheduleSkills(Set<String> requestedSkills,
                                             Map<String, Object> context) {
        try {
            log.info("开始智能调度技能执行: {}", requestedSkills);

            // 1. 分析项目状态，推荐必要技能
            Set<String> recommendedSkills = analyzeAndRecommendSkills(requestedSkills, context);

            // 2. 获取执行顺序
            List<String> executionOrder = dependencyManager.getExecutionOrder(recommendedSkills);

            // 3. 创建执行计划
            SkillExecutionPlan plan = createExecutionPlan(executionOrder, context);

            // 4. 执行技能
            SkillExecutionResult result = executeSkillPlan(plan);

            log.info("技能调度执行完成: 成功={}, 耗时={}ms",
                    result.isSuccess(), result.getExecutionTimeMs());

            return result;

        } catch (Exception e) {
            log.error("技能调度失败", e);
            return SkillExecutionResult.failure("技能调度失败: " + e.getMessage());
        }
    }

    /**
     * 分析项目状态并推荐技能
     */
    private Set<String> analyzeAndRecommendSkills(Set<String> requestedSkills,
                                                 Map<String, Object> context) {
        Set<String> recommendedSkills = new HashSet<>(requestedSkills);

        // 获取项目当前状态
        ProjectHealthStatus healthStatus = getProjectHealthStatus();

        // 根据项目状态推荐额外技能
        if (healthStatus.getCompilationErrorCount() > 50) {
            recommendedSkills.add("compilation-error-prevention-specialist");
        }

        if (healthStatus.getJakartaViolationCount() > 0) {
            recommendedSkills.add("spring-boot-jakarta-guardian");
        }

        if (healthStatus.getArchitectureViolationCount() > 0) {
            recommendedSkills.add("four-tier-architecture-guardian");
        }

        if (healthStatus.getEntityIssueCount() > 0) {
            recommendedSkills.add("entity-relationship-modeling-specialist");
        }

        if (healthStatus.getTechStackInconsistencyCount() > 0) {
            recommendedSkills.add("tech-stack-unification-specialist");
        }

        // 始终添加最终的自动化检查
        recommendedSkills.add("automated-code-quality-checker");

        log.info("技能推荐完成: 用户请求{}个，推荐{}个",
                requestedSkills.size(), recommendedSkills.size());

        return recommendedSkills;
    }

    /**
     * 创建技能执行计划
     */
    private SkillExecutionPlan createExecutionPlan(List<String> executionOrder,
                                                 Map<String, Object> context) {
        SkillExecutionPlan plan = new SkillExecutionPlan();

        for (String skillName : executionOrder) {
            SkillExecutionStep step = new SkillExecutionStep();
            step.setSkillName(skillName);
            step.setEstimatedTimeMs(estimateExecutionTime(skillName));
            step.setRequiredResources(getRequiredResources(skillName));
            step.setCheckpoint(isCheckpointSkill(skillName));

            // 设置共享缓存
            if (resourceManager.canShareCache(skillName)) {
                step.setUseSharedCache(true);
            }

            plan.addStep(step);
        }

        return plan;
    }

    /**
     * 执行技能计划
     */
    private SkillExecutionResult executeSkillPlan(SkillExecutionPlan plan) {
        long startTime = System.currentTimeMillis();
        SkillExecutionResult result = new SkillExecutionResult();
        Map<String, Object> sharedContext = new HashMap<>();

        try {
            for (SkillExecutionStep step : plan.getSteps()) {
                log.debug("执行技能步骤: {}", step.getSkillName());

                // 执行前检查
                if (!resourceManager.checkResourcesAvailable(step.getRequiredResources())) {
                    throw new SkillExecutionException("资源不足，无法执行: " + step.getSkillName());
                }

                // 执行技能
                SkillStepResult stepResult = executeSkillStep(step, sharedContext);

                if (!stepResult.isSuccess()) {
                    log.error("技能执行失败: {}, 错误: {}", step.getSkillName(), stepResult.getErrorMessage());
                    result.addFailedStep(step.getSkillName(), stepResult.getErrorMessage());

                    if (step.isCheckpoint()) {
                        throw new SkillExecutionException("关键检查点技能失败: " + step.getSkillName());
                    }
                } else {
                    result.addSuccessfulStep(step.getSkillName());

                    // 更新共享上下文
                    if (stepResult.getOutputData() != null) {
                        sharedContext.putAll(stepResult.getOutputData());
                    }
                }

                // 记录技能效果
                effectivenessTracker.recordSkillExecution(step.getSkillName(), stepResult);
            }

            result.setSuccess(true);
            return result;

        } catch (Exception e) {
            log.error("技能计划执行失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            return result;
        } finally {
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 执行单个技能步骤
     */
    private SkillStepResult executeSkillStep(SkillExecutionStep step, Map<String, Object> sharedContext) {
        try {
            // 准备执行环境
            Map<String, Object> executionContext = new HashMap<>(sharedContext);

            // 如果使用共享缓存，添加缓存信息
            if (step.isUseSharedCache()) {
                executionContext.put("sharedCache", resourceManager.getSharedCache());
            }

            // 调用技能（这里需要集成实际的技能调用机制）
            log.info("开始执行技能: {}", step.getSkillName());

            // 模拟技能执行
            SkillStepResult result = executeActualSkill(step.getSkillName(), executionContext);

            log.info("技能执行完成: {}, 耗时: {}ms", step.getSkillName(), result.getExecutionTimeMs());

            return result;

        } catch (Exception e) {
            log.error("技能步骤执行失败: {}", step.getSkillName(), e);
            return SkillStepResult.failure(e.getMessage());
        }
    }

    // 其他辅助方法...
    private ProjectHealthStatus getProjectHealthStatus() {
        // 实现项目健康状态检查
        return new ProjectHealthStatus();
    }

    private long estimateExecutionTime(String skillName) {
        // 基于历史数据估算执行时间
        return effectivenessTracker.getAverageExecutionTime(skillName);
    }

    private Set<String> getRequiredResources(String skillName) {
        // 获取技能所需资源
        return resourceManager.getRequiredResources(skillName);
    }

    private boolean isCheckpointSkill(String skillName) {
        // 判断是否为关键检查点技能
        return Set.of("spring-boot-jakarta-guardian", "automated-code-quality-checker")
                .contains(skillName);
    }

    private SkillStepResult executeActualSkill(String skillName, Map<String, Object> context) {
        // 实际的技能执行逻辑
        // 这里需要与实际的技能系统集成
        return SkillStepResult.success("技能执行成功");
    }
}
```

### 3. 技能资源管理器
```java
/**
 * 技能资源管理器
 */
@Component
@Slf4j
public class SkillResourceManager {

    // 共享缓存
    private final Map<String, Object> sharedCache = new ConcurrentHashMap<>();

    // 资源使用情况
    private final Map<String, Integer> resourceUsage = new ConcurrentHashMap<>();

    // 资源限制配置
    private final Map<String, Integer> resourceLimits = Map.of(
        "compilation", 1,  // 同时只能有一个编译任务
        "file_scan", 3,   // 最多3个文件扫描任务
        "network", 2      // 最多2个网络请求任务
    );

    /**
     * 检查资源是否可用
     */
    public boolean checkResourcesAvailable(Set<String> requiredResources) {
        for (String resource : requiredResources) {
            int currentUsage = resourceUsage.getOrDefault(resource, 0);
            int limit = resourceLimits.getOrDefault(resource, 1);

            if (currentUsage >= limit) {
                log.debug("资源不足: {} (当前使用: {}, 限制: {})", resource, currentUsage, limit);
                return false;
            }
        }
        return true;
    }

    /**
     * 预留资源
     */
    public boolean reserveResources(Set<String> requiredResources, String skillName) {
        if (!checkResourcesAvailable(requiredResources)) {
            return false;
        }

        for (String resource : requiredResources) {
            resourceUsage.merge(resource, 1, Integer::sum);
        }

        log.debug("资源预留成功: {}, 预留资源: {}", skillName, requiredResources);
        return true;
    }

    /**
     * 释放资源
     */
    public void releaseResources(Set<String> requiredResources, String skillName) {
        for (String resource : requiredResources) {
            resourceUsage.computeIfPresent(resource, (k, v) -> v > 1 ? v - 1 : null);
        }

        log.debug("资源释放成功: {}, 释放资源: {}", skillName, requiredResources);
    }

    /**
     * 获取共享缓存
     */
    public Map<String, Object> getSharedCache() {
        return sharedCache;
    }

    /**
     * 检查技能是否可以使用共享缓存
     */
    public boolean canShareCache(String skillName) {
        // 某些技能的结果可以被其他技能复用
        return Set.of(
            "spring-boot-jakarta-guardian",
            "entity-relationship-modeling-specialist",
            "tech-stack-unification-specialist"
        ).contains(skillName);
    }

    /**
     * 获取技能所需资源
     */
    public Set<String> getRequiredResources(String skillName) {
        return switch (skillName) {
            case "spring-boot-jakarta-guardian",
                 "code-quality-protector" -> Set.of("compilation", "file_scan");
            case "four-tier-architecture-guardian" -> Set.of("file_scan");
            case "tech-stack-unification-specialist" -> Set.of("file_scan", "network");
            default -> Set.of("file_scan");
        };
    }

    /**
     * 获取资源使用统计
     */
    public Map<String, ResourceUsageStats> getResourceUsageStats() {
        Map<String, ResourceUsageStats> stats = new HashMap<>();

        for (Map.Entry<String, Integer> limit : resourceLimits.entrySet()) {
            String resource = limit.getKey();
            int currentUsage = resourceUsage.getOrDefault(resource, 0);
            int maxUsage = limit.getValue();

            ResourceUsageStats stat = new ResourceUsageStats();
            stat.setResourceName(resource);
            stat.setCurrentUsage(currentUsage);
            stat.setMaxUsage(maxUsage);
            stat.setUtilizationRate((double) currentUsage / maxUsage);

            stats.put(resource, stat);
        }

        return stats;
    }
}
```

### 4. 技能协同配置
```yaml
# skill-coordination.yml
skill-coordination:
  # 技能依赖关系
  dependencies:
    spring-boot-jakarta-guardian: []
    entity-relationship-modeling-specialist: ["spring-boot-jakarta-guardian"]
    code-quality-protector: ["spring-boot-jakarta-guardian", "entity-relationship-modeling-specialist"]
    four-tier-architecture-guardian: ["spring-boot-jakarta-guardian"]
    tech-stack-unification-specialist: ["spring-boot-jakarta-guardian", "code-quality-protector"]
    automated-code-quality-checker: ["spring-boot-jakarta-guardian", "code-quality-protector"]

  # 技能冲突关系
  conflicts:
    code-quality-protector: ["compilation-error-prevention-specialist"]

  # 技能优先级
  priorities:
    spring-boot-jakarta-guardian: 1
    entity-relationship-modeling-specialist: 2
    compilation-error-prevention-specialist: 2
    code-quality-protector: 3
    four-tier-architecture-guardian: 4
    tech-stack-unification-specialist: 5
    automated-code-quality-checker: 6

  # 资源限制
  resource-limits:
    compilation: 1
    file_scan: 3
    network: 2
    database: 1

  # 共享缓存配置
  shared-cache:
    enabled: true
    ttl: 3600  # 缓存1小时
    max-size: 1000

  # 执行策略
  execution-strategy:
    # 是否启用智能推荐
    enable-smart-recommendation: true
    # 是否允许并行执行
    enable-parallel-execution: true
    # 最大并行技能数
    max-parallel-skills: 3
    # 失败重试次数
    max-retry-count: 2
```

## 🎯 技能应用场景

### 1. 复杂任务自动化分解
用户只需说明目标，系统自动：
- 分析项目状态
- 推荐必要技能
- 制定执行顺序
- 协调资源使用

### 2. 技能冲突自动解决
- 检测技能间的依赖和冲突
- 智能调整执行顺序
- 避免重复工作

### 3. 资源优化调度
- 共享编译结果
- 复用文件扫描数据
- 并行执行无冲突技能

## 🔧 技能协同工具

### 智能技能推荐器CLI
```bash
#!/bin/bash
# intelligent-skill-coordinator.sh

# 智能技能协调CLI工具
echo "🤖 IOE-DREAM 智能技能协调系统"

# 用户输入的技能列表
user_skills=("$@")

if [ ${#user_skills[@]} -eq 0 ]; then
    echo "使用方法: $0 <skill1> <skill2> ..."
    echo "示例: $0 code-quality-protector entity-relationship-modeling-specialist"
    exit 1
fi

echo "用户请求的技能: ${user_skills[*]}"

# 1. 分析项目状态
echo "🔍 分析项目状态..."
project_status=$(curl -s "http://localhost:1024/api/skill/analyze-project" 2>/dev/null || echo '{"compilation_errors":0,"jakarta_violations":0}')

# 2. 获取技能推荐
echo "🎯 获取技能推荐..."
recommended_skills=$(curl -s -X POST "http://localhost:1024/api/skill/recommend" \
    -H "Content-Type: application/json" \
    -d "{\"user_skills\":$(printf '%s\n' "${user_skills[@]}" | jq -R . | jq -s .),\"project_status\":$project_status}" 2>/dev/null)

if [ $? -eq 0 ] && [ -n "$recommended_skills" ]; then
    echo "推荐的技能执行顺序:"
    echo "$recommended_skills" | jq -r '.execution_order[]' | while read skill; do
        echo "  - $skill"
    done

    echo ""
    echo "预计执行时间: $(echo "$recommended_skills" | jq -r '.estimated_time_ms')ms"
    echo "所需资源: $(echo "$recommended_skills" | jq -r '.required_resources | join(", ")')"

    # 3. 询问是否执行
    echo ""
    read -p "是否执行推荐的技能序列? (y/N): " confirm

    if [[ $confirm =~ ^[Yy]$ ]]; then
        echo "🚀 开始执行技能序列..."

        # 执行技能序列
        execution_result=$(curl -s -X POST "http://localhost:1024/api/skill/execute" \
            -H "Content-Type: application/json" \
            -d "$recommended_skills" 2>/dev/null)

        if [ $? -eq 0 ]; then
            echo "✅ 技能执行完成"
            echo "$execution_result" | jq '.'
        else
            echo "❌ 技能执行失败"
        fi
    else
        echo "❌ 用户取消执行"
    fi
else
    echo "❌ 获取技能推荐失败"
    echo "使用默认技能序列: ${user_skills[*]}"
fi
```

## 📊 协同效果度量

### 技能协同KPI
- **执行效率提升**: 通过资源复用减少重复工作
- **成功率提升**: 通过智能调度减少冲突
- **时间节省**: 通过并行执行优化总耗时
- **资源利用率**: 通过共享机制提高资源使用效率

---

## 🚀 技能等级要求

### 初级 (★☆☆)
- 了解基本的技能协同概念
- 能够识别简单的技能依赖关系

### 中级 (★★☆)
- 能够设计技能编排流程
- 掌握资源优化调度方法

### 专家级 (★★★)
- 能够设计复杂的技能协同机制
- 掌握智能调度算法实现
- 能够建立技能生态系统

---

**技能使用提示**: 当需要协调多个专家技能协同工作时，调用此技能获得智能的技能编排和调度方案。

**记忆要点**:
- 技能协同的核心是依赖关系管理
- 智能调度能显著提升执行效率
- 资源共享是协同机制的关键
- 自动推荐能简化用户操作
- 冲突检测和解决是稳定性的保障