# 🎯 开发策略专家技能

**技能名称**: development-strategy-specialist
**技能等级**: ★★★ 高级
**适用角色**: 项目架构师、技术负责人、AI开发助手
**前置技能**: 无
**预计学时**: 2小时

---

## 📋 技能概述

开发策略专家专注于为IOE-DREAM项目提供最佳的开发技能选择和组合策略，确保每个开发任务都能使用最合适的技能组合，实现高质量、高效率的开发目标。

### 🎯 核心能力

- **智能技能选择**: 基于任务类型、模块特性、复杂度等因素，智能选择最优技能组合
- **开发流程规划**: 为不同类型的开发任务制定最优的开发流程和步骤
- **质量保障策略**: 制定贯穿开发全流程的质量检查和验证策略
- **风险管理**: 识别开发过程中的潜在风险并提供预防措施

---

## 🛠️ 操作步骤

### 第一步：任务分析和类型识别

#### 1.1 基础任务类型分析
```bash
function analyze_task_type() {
    local task_description="$1"
    local module_type="$2"

    echo "🔍 任务类型分析..."
    echo "任务描述: $task_description"
    echo "模块类型: $module_type"

    # 新模块开发任务特征识别
    if [[ "$task_description" =~ "新增|创建|开发|实现.*模块" ]]; then
        echo "✅ 识别为: 新模块开发任务"
        return 1
    fi

    # 代码修复任务特征识别
    if [[ "$task_description" =~ "修复|解决|处理.*错误|问题|异常" ]]; then
        echo "✅ 识别为: 代码修复任务"
        return 2
    fi

    # 性能优化任务特征识别
    if [[ "$task_description" =~ "优化|提升|改进.*性能|效率|速度" ]]; then
        echo "✅ 识别为: 性能优化任务"
        return 3
    fi

    # 架构设计任务特征识别
    if [[ "$task_description" =~ "设计|架构|重构.*系统|模块|组件" ]]; then
        echo "✅ 识别为: 架构设计任务"
        return 4
    fi

    # 测试相关任务特征识别
    if [[ "$task_description" =~ "测试|验证|检查.*质量|功能|性能" ]]; then
        echo "✅ 识别为: 测试验证任务"
        return 5
    fi

    echo "❓ 未识别到明确任务类型，使用通用开发策略"
    return 0
}
```

#### 1.2 复杂度评估
```bash
function assess_task_complexity() {
    local task_description="$1"

    echo "🔬 评估任务复杂度..."

    complexity_score=0

    # 基于关键词评估复杂度
    if [[ "$task_description" =~ "复杂|困难|挑战" ]]; then
        ((complexity_score += 3))
    fi

    if [[ "$task_description" =~ "集成|关联|多个" ]]; then
        ((complexity_score += 2))
    fi

    if [[ "$task_description" =~ "性能|高并发|大数据" ]]; then
        ((complexity_score += 2))
    fi

    if [[ "$task_description" =~ "安全|权限|加密" ]]; then
        ((complexity_score += 1))
    fi

    if [ $complexity_score -ge 5 ]; then
        echo "✅ 复杂度: 高复杂度 (得分: $complexity_score)"
        return 3
    elif [ $complexity_score -ge 2 ]; then
        echo "✅ 复杂度: 中等复杂度 (得分: $complexity_score)"
        return 2
    else
        echo "✅ 复杂度: 低复杂度 (得分: $complexity_score)"
        return 1
    fi
}
```

### 第二步：技能组合策略制定

#### 2.1 新模块开发技能组合
```bash
function new_module_development_strategy() {
    local module_type="$1"
    local complexity_level="$2"

    echo "🚀 制定新模块开发技能组合策略..."
    echo "模块类型: $module_type"
    echo "复杂度级别: $complexity_level"

    # 核心技能序列 (按优先级排序)
    declare -a core_skills=(
        "openspec-compliance-specialist"     # OpenSpec规范检查
        "development-standards-specialist"   # 开发规范检查
        "four-tier-architecture-guardian"    # 四层架构设计
        "database-design-specialist"         # 数据库设计
        "spring-boot-jakarta-guardian"      # SpringBoot开发
        "cache-architecture-specialist"      # 缓存架构
        "business-module-developer"          # 业务模块开发
        "frontend-development-specialist"    # 前端开发
        "quality-assurance-expert"           # 质量保证
        "code-quality-protector"             # 代码质量检查
    )

    # 业务专家技能 (基于模块类型)
    case "$module_type" in
        "access_control")
            core_skills+=("access-control-business-specialist")
            ;;
        "consume")
            core_skills+=("business-module-developer")
            ;;
        "attendance")
            core_skills+=("business-module-developer")
            ;;
        "video")
            core_skills+=("intelligent-operations-expert")
            ;;
    esac

    # 高复杂度任务增加额外技能
    if [ "$complexity_level" -ge 3 ]; then
        core_skills+=("quality-assurance-expert")    # 额外质量保证
        core_skills+=("code-quality-protector")      # 额外代码质量检查
    fi

    echo "📋 技能调用序列:"
    for i in "${!core_skills[@]}"; do
        echo "  $((i+1)). ${core_skills[i]}"
    done

    return 0
}
```

#### 2.2 代码修复技能组合
```bash
function code_fix_strategy() {
    local error_type="$1"

    echo "🔧 制定代码修复技能组合策略..."
    echo "错误类型: $error_type"

    # 基于错误类型选择技能
    declare -a fix_skills=()

    # 编译错误修复序列
    if [[ "$error_type" =~ "编译|包名|依赖" ]]; then
        fix_skills=(
            "spring-boot-jakarta-guardian"       # Jakarta包名问题
            "four-tier-architecture-guardian"    # 架构问题
            "code-quality-protector"             # 编码质量问题
        )
    fi

    # 运行时错误修复序列
    if [[ "$error_type" =~ "运行时|异常|空指针" ]]; then
        fix_skills=(
            "business-module-developer"          # 业务逻辑问题
            "cache-architecture-specialist"      # 缓存问题
            "code-quality-protector"             # 代码质量问题
        )
    fi

    # 性能问题修复序列
    if [[ "$error_type" =~ "性能|慢|超时" ]]; then
        fix_skills=(
            "cache-architecture-specialist"      # 缓存优化
            "database-design-specialist"         # 数据库优化
            "quality-assurance-expert"           # 性能测试
        )
    fi

    # 默认修复序列
    if [ ${#fix_skills[@]} -eq 0 ]; then
        fix_skills=(
            "spring-boot-jakarta-guardian"      # 通用SpringBoot问题
            "code-quality-protector"            # 通用代码质量问题
            "four-tier-architecture-guardian"   # 架构问题检查
        )
    fi

    echo "📋 修复技能调用序列:"
    for i in "${!fix_skills[@]}"; do
        echo "  $((i+1)). ${fix_skills[i]}"
    done

    return 0
}
```

#### 2.3 性能优化技能组合
```bash
function performance_optimization_strategy() {
    local optimization_target="$1"

    echo "⚡ 制定性能优化技能组合策略..."
    echo "优化目标: $optimization_target"

    declare -a optimization_skills=(
        "cache-architecture-specialist"        # 缓存架构优化
        "database-design-specialist"           # 数据库优化
        "quality-assurance-expert"             # 性能测试验证
    )

    # 基于优化目标增加专项技能
    case "$optimization_target" in
        "缓存")
            optimization_skills=("cache-architecture-specialist")
            ;;
        "数据库")
            optimization_skills=("database-design-specialist")
            ;;
        "接口响应")
            optimization_skills+=("spring-boot-jakarta-guardian")
            ;;
        "前端性能")
            optimization_skills+=("frontend-development-specialist")
            ;;
    esac

    echo "📋 优化技能调用序列:"
    for i in "${!optimization_skills[@]}"; do
        echo "  $((i+1)). ${optimization_skills[i]}"
    done

    return 0
}
```

### 第三步：开发阶段规划

#### 3.1 标准开发阶段规划
```bash
function plan_development_phases() {
    local task_type="$1"
    local total_duration="$2"  # 预计总时长(天)

    echo "📅 制定开发阶段规划..."
    echo "任务类型: $task_type"
    echo "预计总时长: ${total_duration}天"

    case "$task_type" in
        "新模块开发")
            echo "🏗️ 新模块开发阶段规划:"
            echo "  阶段1: 需求分析与设计 ($(( total_duration * 20 / 100 ))天)"
            echo "  阶段2: 环境准备与验证 ($(( total_duration * 10 / 100 ))天)"
            echo "  阶段3: 数据库设计实施 ($(( total_duration * 15 / 100 ))天)"
            echo "  阶段4: 后端代码开发 ($(( total_duration * 30 / 100 ))天)"
            echo "  阶段5: 前端代码开发 ($(( total_duration * 15 / 100 ))天)"
            echo "  阶段6: 测试开发与验证 ($(( total_duration * 10 / 100 ))天)"
            ;;
        "代码修复")
            echo "🔧 代码修复阶段规划:"
            echo "  阶段1: 问题分析和定位 ($(( total_duration * 30 / 100 ))天)"
            echo "  阶段2: 修复方案设计 ($(( total_duration * 20 / 100 ))天)"
            echo "  阶段3: 代码实施和测试 ($(( total_duration * 40 / 100 ))天)"
            echo "  阶段4: 验证和部署 ($(( total_duration * 10 / 100 ))天)"
            ;;
        "性能优化")
            echo "⚡ 性能优化阶段规划:"
            echo "  阶段1: 性能瓶颈分析 ($(( total_duration * 25 / 100 ))天)"
            echo "  阶段2: 优化方案设计 ($(( total_duration * 25 / 100 ))天)"
            echo "  阶段3: 优化实施 ($(( total_duration * 35 / 100 ))天)"
            echo "  阶段4: 性能测试和验证 ($(( total_duration * 15 / 100 ))天)"
            ;;
    esac

    return 0
}
```

### 第四步：质量保障策略

#### 4.1 质量检查点设置
```bash
function setup_quality_checkpoints() {
    local task_type="$1"

    echo "🛡️ 设置质量检查点..."
    echo "任务类型: $task_type"

    # 通用质量检查点
    echo "📋 通用质量检查点:"
    echo "  ✓ 编码规范检查 (零容忍)"
    echo "  ✓ 编译完整性检查"
    echo "  ✓ 缓存架构规范检查"
    echo "  ✓ 安全规范检查"
    echo "  ✓ repowiki规范符合性检查"

    # 任务类型专项检查点
    case "$task_type" in
        "新模块开发")
            echo "📋 新模块开发专项检查:"
            echo "  ✓ 数据库设计规范检查"
            echo "  ✓ API设计规范检查"
            echo "  ✓ 前后端接口一致性检查"
            echo "  ✓ 权限设计完整性检查"
            echo "  ✓ Docker部署验证 (120秒监控)"
            ;;
        "代码修复")
            echo "📋 代码修复专项检查:"
            echo "  ✓ 问题修复验证检查"
            echo "  ✓ 回归测试检查"
            echo "  ✓ 性能影响检查"
            ;;
        "性能优化")
            echo "📋 性能优化专项检查:"
            echo "  ✓ 性能基准对比检查"
            echo "  ✓ 资源使用率检查"
            echo "  ✓ 并发性能检查"
            ;;
    esac

    return 0
}
```

#### 4.2 风险识别和预防
```bash
function identify_and_prevent_risks() {
    local task_type="$1"
    local complexity_level="$2"

    echo "⚠️ 风险识别和预防..."
    echo "任务类型: $task_type"
    echo "复杂度级别: $complexity_level"

    echo "🔍 已识别风险点:"

    # 高复杂度任务风险
    if [ "$complexity_level" -ge 3 ]; then
        echo "  ⚠️ 高复杂度风险: 任务难度大，可能需要更多时间和资源"
        echo "  🛡️ 预防措施: 增加设计评审时间，准备备选方案"
    fi

    # 新模块开发风险
    if [ "$task_type" = "新模块开发" ]; then
        echo "  ⚠️ 架构设计风险: 可能影响现有系统稳定性"
        echo "  🛡️ 预防措施: 严格遵循四层架构，充分测试集成"
        echo "  ⚠️ 数据库设计风险: 可能影响数据一致性和性能"
        echo "  🛡️ 预防措施: 遵循数据库设计规范，建立索引策略"
    fi

    # 代码修复风险
    if [ "$task_type" = "代码修复" ]; then
        echo "  ⚠️ 修复引入风险: 可能引入新的问题"
        echo "  🛡️ 预防措施: 充分测试，建立回滚机制"
    fi

    # 性能优化风险
    if [ "$task_type" = "性能优化" ]; then
        echo "  ⚠️ 性能优化风险: 可能影响代码可读性和维护性"
        echo "  🛡️ 预防措施: 保持代码简洁，添加详细注释"
    fi

    return 0
}
```

### 第五步：技能执行策略

#### 5.1 技能调用时机控制
```bash
function plan_skill_execution_timing() {
    local task_type="$1"

    echo "⏰ 规划技能调用时机..."
    echo "任务类型: $task_type"

    case "$task_type" in
        "新模块开发")
            echo "🕐 新模块开发技能调用时机:"
            echo "  开发前: openspec-compliance-specialist, development-standards-specialist"
            echo "  设计阶段: four-tier-architecture-guardian, database-design-specialist"
            echo "  开发阶段: spring-boot-jakarta-guardian, business-module-developer"
            echo "  缓存集成: cache-architecture-specialist"
            echo "  前端开发: frontend-development-specialist"
            echo "  测试阶段: quality-assurance-expert"
            echo "  质量检查: code-quality-protector"
            ;;
        "代码修复")
            echo "🕐 代码修复技能调用时机:"
            echo "  问题分析: spring-boot-jakarta-guardian"
            echo "  修复实施: four-tier-architecture-guardian, business-module-developer"
            echo "  质量验证: code-quality-protector, quality-assurance-expert"
            ;;
        "性能优化")
            echo "🕐 性能优化技能调用时机:"
            echo "  瓶颈分析: cache-architecture-specialist, database-design-specialist"
            echo "  优化实施: 相关业务专家技能"
            echo "  性能验证: quality-assurance-expert"
            ;;
    esac

    return 0
}
```

#### 5.2 技能依赖关系管理
```bash
function manage_skill_dependencies() {
    local skill_list=("$@")

    echo "🔗 管理技能依赖关系..."
    echo "技能列表: ${skill_list[*]}"

    # 定义技能依赖关系
    declare -A skill_dependencies=(
        ["business-module-developer"]="spring-boot-jakarta-guardian four-tier-architecture-guardian"
        ["cache-architecture-specialist"]="four-tier-architecture-guardian"
        ["frontend-development-specialist"]="development-standards-specialist"
        ["quality-assurance-expert"]="code-quality-protector"
    )

    echo "📋 技能依赖关系分析:"

    for skill in "${skill_list[@]}"; do
        if [[ -n "${skill_dependencies[$skill]}" ]]; then
            echo "  $skill 依赖于: ${skill_dependencies[$skill]}"
        else
            echo "  $skill: 无依赖"
        fi
    done

    # 生成最优执行顺序
    echo ""
    echo "📅 建议执行顺序:"

    # 首先执行无依赖的技能
    for skill in "${skill_list[@]}"; do
        if [[ -z "${skill_dependencies[$skill]}" ]]; then
            echo "  第一阶段: $skill"
        fi
    done

    # 然后执行有依赖的技能
    for skill in "${skill_list[@]}"; do
        if [[ -n "${skill_dependencies[$skill]}" ]]; then
            echo "  第二阶段: $skill (依赖: ${skill_dependencies[$skill]})"
        fi
    done

    return 0
}
```

---

## ⚠️ 注意事项

### 🔴 关键约束

1. **技能调用顺序**: 必须严格按照依赖关系执行，避免顺序错误导致的问题
2. **质量门禁**: 每个阶段完成后必须通过质量检查才能进入下一阶段
3. **规范遵循**: 所有技能调用都必须以repowiki规范为准，确保零异常开发

### 🟡 重要提醒

1. **复杂度评估**: 必须准确评估任务复杂度，以便选择合适的技能组合
2. **风险预防**: 高复杂度任务必须有备选方案和风险预防措施
3. **持续验证**: 在开发过程中要持续验证代码质量和规范遵循情况

### 🟢 最佳实践

1. **技能组合优化**: 根据历史经验持续优化技能组合策略
2. **知识积累**: 记录成功案例和失败教训，形成知识库
3. **团队协作**: 鼓励团队成员分享技能使用经验和最佳实践

---

## 📊 评估标准

### 操作时间评估

| 任务类型 | 简单(1-2小时) | 中等(2-4小时) | 复杂(4-8小时) |
|---------|-------------|-------------|-------------|
| 新模块开发 | 1-2小时 | 2-4小时 | 4-8小时 |
| 代码修复 | 1小时 | 2-3小时 | 3-6小时 |
| 性能优化 | 2小时 | 3-5小时 | 5-8小时 |
| 架构设计 | 2-3小时 | 4-6小时 | 6-10小时 |

### 准确率要求

- **技能选择准确率**: ≥95%
- **时间预估准确率**: ±20%
- **风险识别覆盖率**: ≥90%
- **质量保障通过率**: 100%

### 质量标准

- **策略完整性**: 必须覆盖所有开发阶段和关键节点
- **可操作性**: 所有策略必须具体可执行
- **适应性**: 能够根据项目实际情况灵活调整
- **可追溯性**: 所有决策过程必须有明确记录

---

## 🔧 实用工具函数

### 快速策略生成器
```bash
# 一键生成开发策略
function generate_development_strategy() {
    local task_description="$1"
    local module_type="${2:-general}"
    local estimated_days="${3:-5}"

    echo "🚀 一键生成开发策略..."

    # 1. 任务类型分析
    task_type=$(analyze_task_type "$task_description" "$module_type")

    # 2. 复杂度评估
    complexity=$(assess_task_complexity "$task_description")

    # 3. 生成技能组合
    case "$task_type" in
        1) new_module_development_strategy "$module_type" "$complexity" ;;
        2) code_fix_strategy "general" ;;
        3) performance_optimization_strategy "general" ;;
        4) echo "架构设计任务策略开发中..." ;;
        5) echo "测试验证任务策略开发中..." ;;
        *) echo "通用开发策略" ;;
    esac

    # 4. 生成阶段规划
    plan_development_phases "$task_type" "$estimated_days"

    # 5. 设置质量检查点
    setup_quality_checkpoints "$task_type"

    # 6. 风险识别
    identify_and_prevent_risks "$task_type" "$complexity"

    echo ""
    echo "🎯 开发策略生成完成！"
    echo "请按照以上策略执行开发任务。"
}
```

---

**使用示例**:
```bash
# 为新消费模块生成开发策略
generate_development_strategy "开发新的消费管理模块" "consume" 10

# 为代码修复生成策略
generate_development_strategy "修复用户登录接口的编译错误" "access_control" 2

# 为性能优化生成策略
generate_development_strategy "优化设备查询接口性能" "device" 5
```

---

*此技能文档持续更新，以适应IOE-DREAM项目的发展和变化。*