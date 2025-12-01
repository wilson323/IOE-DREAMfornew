#!/bin/bash
# Skills验证和优化脚本
# 确保所有skill文件符合Claude Code标准规范

set -e

SKILLS_DIR="D:/IOE-DREAM/.claude/skills"
VALID_SKILLS=(
    "code-quality-protector.md"
    "spring-boot-jakarta-guardian.md"
    "four-tier-architecture-guardian.md"
    "database-design-specialist.md"
    "business-module-developer.md"
    "quality-assurance-expert.md"
    "frontend-development-specialist.md"
    "intelligent-operations-expert.md"
    "access-control-business-specialist.md"
    "openspec-compliance-specialist.md"
)

echo "🔍 开始验证和优化Claude Skills..."

# 1. 验证YAML frontmatter标准
validate_yaml_frontmatter() {
    local file="$1"
    echo "📋 验证YAML frontmatter: $file"

    # 检查必需字段
    local required_fields=("name" "description" "tools" "color")
    for field in "${required_fields[@]}"; do
        if ! grep -q "^$field:" "$file"; then
            echo "❌ 缺少必需字段: $field 在文件 $file"
            return 1
        fi
    done

    # 验证tools字段格式
    if ! grep -q "^tools: Read, Write, Glob, Grep, Bash" "$file"; then
        echo "⚠️ tools字段格式可能不标准: $file"
    fi

    echo "✅ YAML frontmatter验证通过: $file"
}

# 2. 验证Markdown结构
validate_markdown_structure() {
    local file="$1"
    echo "📝 验证Markdown结构: $file"

    # 检查必需的章节标题
    local required_sections=(
        "# 核心职责"
        "## 核心能力"
        "## 错误预防机制"
    )

    for section in "${required_sections[@]}"; do
        if ! grep -q "$section" "$file"; then
            echo "❌ 缺少必需章节: $section 在文件 $file"
            return 1
        fi
    done

    echo "✅ Markdown结构验证通过: $file"
}

# 3. 验证repowiki规范符合性
validate_repowiki_compliance() {
    local file="$1"
    echo "📚 验证repowiki规范符合性: $file"

    # 检查是否包含repowiki相关内容
    local repowiki_keywords=(
        "repowiki"
        "四层架构"
        "jakarta"
        "UTF-8"
        "Sa-Token"
        "MyBatis-Plus"
    )

    local compliance_score=0
    for keyword in "${repowiki_keywords[@]}"; do
        if grep -q "$keyword" "$file"; then
            ((compliance_score++))
        fi
    done

    if [ $compliance_score -lt 3 ]; then
        echo "⚠️ repowiki规范符合性较低 ($compliance_score/6): $file"
    else
        echo "✅ repowiki规范符合性良好 ($compliance_score/6): $file"
    fi
}

# 4. 验证代码示例质量
validate_code_examples() {
    local file="$1"
    echo "💻 验证代码示例质量: $file"

    # 检查Java代码示例
    if grep -q "```java" "$file"; then
        # 检查是否使用正确的包名
        if grep -q "javax\." "$file"; then
            echo "❌ 发现违规javax包使用: $file"
            return 1
        fi

        # 检查是否使用@Resource而非@Autowired
        if grep -q "@Autowired" "$file"; then
            echo "❌ 发现违规@Autowired使用: $file"
            return 1
        fi

        echo "✅ Java代码示例质量良好: $file"
    fi

    # 检查Shell脚本示例
    if grep -q "```bash" "$file"; then
        # 检查脚本安全性
        if grep -q "rm -rf" "$file" && ! grep -q "确认" "$file"; then
            echo "⚠️ 危险操作缺少确认: $file"
        fi

        echo "✅ Shell脚本示例质量良好: $file"
    fi
}

# 5. 生成技能使用指南
generate_usage_guide() {
    echo "📖 生成技能使用指南..."

    local guide_file="$SKILLS_DIR/USAGE_GUIDE.md"

    cat > "$guide_file" << 'EOF'
# 🎯 Claude Skills 使用指南

## 快速开始

### 调用技能
```bash
# 基于开发任务选择合适的技能
Skill("code-quality-protector")          # 代码质量检查
Skill("spring-boot-jakarta-guardian")    # Spring Boot开发
Skill("four-tier-architecture-guardian") # 架构设计
Skill("database-design-specialist")      # 数据库设计
```

### 技能协同工作
```bash
# 完整的开发流程
Skill("openspec-compliance-specialist")    # 1. 规范检查
Skill("four-tier-architecture-guardian")   # 2. 架构设计
Skill("database-design-specialist")        # 3. 数据库设计
Skill("spring-boot-jakarta-guardian")     # 4. 后端开发
Skill("frontend-development-specialist")   # 5. 前端开发
Skill("quality-assurance-expert")         # 6. 质量保证
```

## 技能分类

### 🔧 核心开发技能
- **代码质量守护**: 编码规范、UTF-8、零容忍政策
- **Spring Boot专家**: Jakarta包名、依赖注入、技术栈合规
- **架构守护**: 四层架构、违规检测、设计模式

### 🏗️ 数据和架构技能
- **数据库专家**: 表设计、索引优化、性能调优
- **业务模块专家**: 门禁、消费、考勤、视频监控
- **前端专家**: Vue3、TypeScript、Ant Design Vue

### 🛡️ 质量和运维技能
- **质量保证**: 测试、CI/CD、性能监控
- **智能运维**: Docker、监控告警、自动化运维
- **规范遵循**: OpenSpec工作流程、规范检查

## 最佳实践

### 1. 开发前检查
```bash
Skill("openspec-compliance-specialist")    # 规范检查
Skill("code-quality-protector")          # 编码规范检查
```

### 2. 架构设计
```bash
Skill("four-tier-architecture-guardian")   # 架构设计
Skill("database-design-specialist")        # 数据库设计
```

### 3. 开发实施
```bash
Skill("spring-boot-jakarta-guardian")     # 后端开发
Skill("frontend-development-specialist")   # 前端开发
Skill("business-module-developer")        # 业务模块开发
```

### 4. 质量保证
```bash
Skill("code-quality-protector")          # 代码质量检查
Skill("quality-assurance-expert")         # 测试和QA
```

## 错误预防

### 常见问题和解决方案
1. **编译错误**: 使用 `spring-boot-jakarta-guardian`
2. **架构违规**: 使用 `four-tier-architecture-guardian`
3. **编码规范**: 使用 `code-quality-protector`
4. **性能问题**: 使用 `quality-assurance-expert`
5. **部署问题**: 使用 `intelligent-operations-expert`

---

*更新时间: 2025-11-16*
*维护者: IOE-DREAM开发团队*
EOF

    echo "✅ 技能使用指南已生成: $guide_file"
}

# 6. 创建技能索引
create_skill_index() {
    echo "📚 创建技能索引..."

    local index_file="$SKILLS_DIR/SKILL_INDEX.md"

    cat > "$index_file" << 'EOF'
# 📋 Claude Skills 索引

## 按功能分类

### 🔧 代码质量和规范
- **[code-quality-protector.md](code-quality-protector.md)** - 代码质量和编码规范守护专家
  - UTF-8编码规范、零容忍政策、自动修复
  - 应用场景: 代码生成后质量检查、编码规范验证

- **[spring-boot-jakarta-guardian.md](spring-boot-jakarta-guardian.md)** - Spring Boot Jakarta守护专家
  - Jakarta包名、@Resource注入、编译错误预防
  - 应用场景: Spring Boot开发、包名迁移、编译错误修复

### 🏗️ 架构和数据库
- **[four-tier-architecture-guardian.md](four-tier-architecture-guardian.md)** - 四层架构守护专家
  - 严格架构模式、跨层访问检测、架构违规预防
  - 应用场景: 架构设计评审、代码架构检查、重构指导

- **[database-design-specialist.md](database-design-specialist.md)** - 数据库设计规范专家
  - 表设计规范、索引优化、字符集规范、审计字段
  - 应用场景: 数据库设计、性能调优、数据迁移

### 💼 业务模块开发
- **[business-module-developer.md](business-module-developer.md)** - 业务模块开发专家
  - 门禁、消费、考勤、视频监控业务模块开发
  - 应用场景: 业务模块开发、状态机设计、业务规则实现

- **[access-control-business-specialist.md](access-control-business-specialist.md)** - 门禁系统业务专家
  - 门禁业务逻辑、设备管理、权限控制、实时监控
  - 应用场景: 门禁系统开发、权限系统设计、设备集成

### 🛡️ 质量保证
- **[quality-assurance-expert.md](quality-assurance-expert.md)** - 质量保证专家
  - 单元测试、集成测试、性能测试、CI/CD
  - 应用场景: 测试策略制定、质量保证、CI/CD配置

### 🎨 前端开发
- **[frontend-development-specialist.md](frontend-development-specialist.md)** - 前端开发规范专家
  - Vue3+TypeScript+Ant Design Vue开发
  - 应用场景: 前端组件开发、Vue3项目构建、UI规范实施

### 🚀 运维部署
- **[intelligent-operations-expert.md](intelligent-operations-expert.md)** - 智能运维专家
  - Docker部署、监控告警、日志分析、性能优化
  - 应用场景: 系统部署、运维自动化、性能监控、故障处理

### 📋 规范管理
- **[openspec-compliance-specialist.md](openspec-compliance-specialist.md)** - OpenSpec规范遵循专家
  - OpenSpec三阶段工作流程、规范遵循、变更管理
  - 应用场景: OpenSpec变更、规范遵循检查、流程管理

## 按应用场景分类

### 开发阶段
1. **项目启动**: openspec-compliance-specialist
2. **架构设计**: four-tier-architecture-guardian, database-design-specialist
3. **后端开发**: spring-boot-jakarta-guardian, business-module-developer
4. **前端开发**: frontend-development-specialist
5. **质量保证**: code-quality-protector, quality-assurance-expert
6. **部署运维**: intelligent-operations-expert

### 问题修复
1. **编译错误**: spring-boot-jakarta-guardian
2. **架构违规**: four-tier-architecture-guardian
3. **编码规范**: code-quality-protector
4. **性能问题**: quality-assurance-expert
5. **部署问题**: intelligent-operations-expert

### 业务模块
1. **门禁系统**: access-control-business-specialist
2. **消费系统**: business-module-developer
3. **考勤系统**: business-module-developer
4. **视频监控**: business-module-developer

---

*最后更新: 2025-11-16*
EOF

    echo "✅ 技能索引已创建: $index_file"
}

# 7. 验证所有技能文件
main() {
    echo "=========================================="
    echo "Claude Skills 验证和优化"
    echo "时间: $(date)"
    echo "=========================================="

    local total_skills=${#VALID_SKILLS[@]}
    local passed_skills=0

    for skill in "${VALID_SKILLS[@]}"; do
        local skill_file="$SKILLS_DIR/$skill"

        echo ""
        echo "📋 验证技能: $skill"
        echo "----------------------------------------"

        if [ -f "$skill_file" ]; then
            if validate_yaml_frontmatter "$skill_file" && \
               validate_markdown_structure "$skill_file" && \
               validate_repowiki_compliance "$skill_file" && \
               validate_code_examples "$skill_file"; then
                ((passed_skills++))
                echo "✅ 技能验证通过: $skill"
            else
                echo "❌ 技能验证失败: $skill"
            fi
        else
            echo "❌ 技能文件不存在: $skill_file"
        fi
    done

    echo ""
    echo "=========================================="
    echo "验证结果汇总:"
    echo "总技能数: $total_skills"
    echo "通过验证: $passed_skills"
    echo "验证通过率: $(( passed_skills * 100 / total_skills ))%"
    echo "=========================================="

    # 生成辅助文档
    generate_usage_guide
    create_skill_index

    if [ $passed_skills -eq $total_skills ]; then
        echo "🎉 所有技能验证通过！"
        echo "✅ Skills已准备好在后续开发中充分利用"
        return 0
    else
        echo "⚠️ 部分技能需要优化，请检查上述失败项目"
        return 1
    fi
}

# 执行主函数
main "$@"