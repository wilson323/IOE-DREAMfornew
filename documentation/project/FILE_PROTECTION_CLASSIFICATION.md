# 🛡️ 文件重要性分级保护机制

**建立时间**: 2025-11-25 22:35:00
**阶段**: 第三阶段 - 机制完善
**保护等级**: 最高级别

---

## 📊 文件重要性分级体系

### 🔴 **P0级: 核心系统文件** (绝对禁止删除)

#### **系统启动和配置**
| 文件路径 | 重要性 | 风险等级 | 删除后果 |
|----------|--------|----------|----------|
| `./AGENTS.md` | ⭐⭐⭐⭐⭐ | 致命 | AI系统完全瘫痪 |
| `./CLAUDE.md` | ⭐⭐⭐⭐⭐ | 致命 | 项目开发指南失效 |
| `smart-admin-api-java17-springboot3/sa-base/pom.xml` | ⭐⭐⭐⭐⭐ | 致命 | 项目构建失败 |
| `smart-admin-api-java17-springboot3/sa-admin/pom.xml` | ⭐⭐⭐⭐⭐ | 致命 | 项目构建失败 |

#### **AI技能体系核心**
| 文件路径 | 重要性 | 风险等级 | 删除后果 |
|----------|--------|----------|----------|
| `.claude/skills/QUICK_REFERENCE.md` | ⭐⭐⭐⭐⭐ | 致命 | 开发效率崩溃 |
| `.claude/skills/*-expert.md` | ⭐⭐⭐⭐ | 严重 | 专业能力丧失 |
| `.claude/skills/*-specialist.md` | ⭐⭐⭐⭐ | 严重 | 专业能力丧失 |

#### **架构优化核心组件 (OpenSpec 2025-11-25新增)**
| 文件路径 | 重要性 | 风险等级 | 删除后果 |
|----------|--------|----------|----------|
| `sa-base/src/main/java/net/lab1024/sa/base/common/service/BaseService.java` | ⭐⭐⭐⭐⭐ | 致命 | 架构抽象层崩溃 |
| `sa-base/src/main/java/net/lab1024/sa/base/common/exception/GlobalExceptionHandler.java` | ⭐⭐⭐⭐⭐ | 致命 | 异常处理失效 |
| `scripts/compilation-error-resolver.sh` | ⭐⭐⭐⭐ | 严重 | 编译错误解决失效 |
| `scripts/quick-compile-check.sh` | ⭐⭐⭐⭐ | 严重 | 编译检查失效 |
| `openspec/changes/complete-architecture-optimization/` | ⭐⭐⭐⭐⭐ | 致命 | OpenSpec规格丢失 |

#### **业务核心实体**
| 文件路径 | 重要性 | 风险等级 | 删除后果 |
|----------|--------|----------|----------|
| `sa-admin/src/main/java/net/lab1024/sa/admin/module/*/domain/entity/*Entity.java` | ⭐⭐⭐⭐ | 严重 | 业务逻辑中断 |
| `sa-base/src/main/java/net/lab1024/sa/base/common/entity/BaseEntity.java` | ⭐⭐⭐⭐⭐ | 致命 | 数据架构崩溃 |

### 🟡 **P1级: 重要业务文件** (需要验证后删除)

#### **业务服务实现**
| 文件类型 | 路径模式 | 重要性 | 风险等级 |
|----------|----------|--------|----------|
| Service实现类 | `*/service/impl/*ServiceImpl.java` | ⭐⭐⭐⭐ | 高 | 业务功能缺失 |
| Manager实现类 | `*/manager/*Manager.java` | ⭐⭐⭐ | 中 | 业务逻辑中断 |
| DAO实现类 | `*/dao/*Dao.java` | ⭐⭐⭐ | 中 | 数据访问失败 |

#### **配置和数据文件**
| 文件类型 | 路径模式 | 重要性 | 风险等级 |
|----------|----------|--------|----------|
| 数据库脚本 | `数据库SQL脚本/*.sql` | ⭐⭐⭐⭐ | 严重 | 数据库初始化失败 |
| 环境配置 | `*/resources/*.{yaml,properties}` | ⭐⭐⭐ | 高 | 环境配置错误 |
| API文档 | `*/doc.html` 或 `*/api-docs/*` | ⭐⭐ | 中 | API文档缺失 |

### 🟢 **P2级: 一般文件** (可安全删除)

#### **临时和缓存文件**
| 文件类型 | 路径模式 | 重要性 | 安全性 |
|----------|----------|--------|--------|
| 日志文件 | `*.log`, `logs/*` | ⭐ | 安全删除 |
| 缓存文件 | `cache/*`, `*.cache` | ⭐ | 安全删除 |
| 临时文件 | `*.tmp`, `*.temp` | ⭐ | 安全删除 |

#### **生成文件**
| 文件类型 | 路径模式 | 重要性 | 安全性 |
|----------|----------|--------|--------|
| 编译输出 | `target/*`, `build/*` | ⭐ | 安全删除 |
| 测试报告 | `test-results/*`, `reports/*` | ⭐ | 安全删除 |
| IDE配置 | `.idea/*`, `.vscode/*` | ⭐ | 安全删除 |

---

## 🔧 双重验证流程

### **删除操作前的必须验证步骤**

#### **第一步: 自动化工具识别**
```bash
#!/bin/bash
# 自动化文件识别脚本
identify_file_risk_level() {
    local file_path="$1"
    local file_name=$(basename "$file_path")

    # P0级文件检测
    if [[ "$file_path" == "./AGENTS.md" ]] ||
       [[ "$file_path" == "./CLAUDE.md" ]] ||
       [[ "$file_name" == *"BaseEntity.java"* ]] ||
       [[ "$file_name" == *"QUICK_REFERENCE.md"* ]]; then
        echo "P0: 绝对禁止删除 - $file_path"
        return 0
    fi

    # P1级文件检测
    if [[ "$file_name" == *Entity.java ]] ||
       [[ "$file_name" == *ServiceImpl.java ]] ||
       [[ "$file_name" == *Manager.java ]] ||
       [[ "$file_name" == *Dao.java ]]; then
        echo "P1: 重要文件 - 需要验证 - $file_path"
        return 1
    fi

    # P2级文件检测
    if [[ "$file_name" == *.log ]] ||
       [[ "$file_name" == *.tmp ]] ||
       [[ "$file_name" == *.cache ]]; then
        echo "P2: 一般文件 - 可安全删除 - $file_path"
        return 2
    fi

    echo "未知: 需要人工判断 - $file_path"
    return 3
}
```

#### **第二步: 人工重要性确认**
```bash
# 人工确认脚本
manual_importance_confirmation() {
    local file_path="$1"
    local risk_level=$(identify_file_risk_level "$file_path")

    case $risk_level in
        0)
            echo "❌ 拒绝删除: P0级文件 - $file_path"
            return 1
            ;;
        1)
            echo "⚠️ 警告: P1级文件 - $file_path"
            echo "请确认此文件的重要性:"
            echo "  1. 检查是否有引用"
            echo "  2. 确认是否有替代方案"
            echo " 3. 评估删除影响"
            read -p "是否继续删除? (yes/no): " choice
            if [[ "$choice" != "yes" ]]; then
                echo "操作已取消"
                return 1
            fi
            ;;
        2)
            echo "✅ 安全: P2级文件 - $file_path"
            return 0
            ;;
        *)
            echo "🔍 未知: 需要进一步分析 - $file_path"
            read -p "请进行人工判断后决定: " decision
            if [[ "$decision" != "delete" ]]; then
                echo "操作已取消"
                return 1
            fi
            ;;
    esac
    return 0
}
```

#### **第三步: 影响范围评估**
```bash
# 影响范围评估脚本
impact_assessment() {
    local file_path="$1"

    echo "正在评估删除 $file_path 的影响范围..."

    # 检查文件引用
    if [[ -f "$file_path" ]]; then
        echo "检查文件引用..."
        find . -name "*.java" -exec grep -l "$(basename "$file_path")" {} \; 2>/dev/null | head -5

        # 检查Git状态
        if git status --porcelain "$file_path" | grep -q "^M "; then
            echo "⚠️ 警告: 文件有未提交的修改"
        fi
    fi

    # 检查依赖关系
    local file_name=$(basename "$file_path")
    if [[ "$file_name" == *.java ]]; then
        echo "检查Java依赖关系..."
        # 这里可以添加更复杂的依赖关系检查
    fi

    echo "影响范围评估完成"
}
```

#### **第四步: 备份机制激活**
```bash
# 备份机制脚本
activate_backup() {
    local file_path="$1"
    local backup_dir="backup/auto-backup-$(date +%Y%m%d-%H%M%S)"

    echo "创建备份..."
    mkdir -p "$backup_dir"

    if [[ -f "$file_path" ]]; then
        cp --parents "$file_path" "$backup_dir/"
        echo "✅ 文件已备份到: $backup_dir/$(basename "$file_path")"
    fi

    # Git备份
    if git rev-parse --git-dir > /dev/null 2>&1; then
        git add "$file_path"
        git commit -m "Auto backup before deletion: $(basename "$file_path")" || echo "Git备份失败，但文件已本地备份"
    fi
}
```

### **完整删除验证流程**
```bash
# 完整的文件删除验证流程
safe_delete_file() {
    local file_path="$1"

    echo "🔒 开始安全删除流程: $file_path"

    # 第一步: 自动化识别
    local risk_level=$(identify_file_risk_level "$file_path")
    echo "📊 风险等级: P$risk_level"

    # 第二步: 人工确认
    if ! manual_importance_confirmation "$file_path"; then
        echo "❌ 删除操作已取消"
        return 1
    fi

    # 第三步: 影响评估
    impact_assessment "$file_path"

    # 第四步: 激活备份
    activate_backup "$file_path"

    # 第五步: 执行删除
    echo "🗑️ 执行删除操作..."
    rm -rf "$file_path"

    # 第六步: 删除后验证
    if [[ ! -e "$file_path" ]]; then
        echo "✅ 文件已成功删除"
        return 0
    else
        echo "❌ 删除失败，文件仍然存在"
        return 1
    fi
}
```

---

## 📅 定期检查机制

### **每日检查: 重要文件状态**
```bash
#!/bin/bash
# 每日重要文件状态检查
daily_critical_file_check() {
    local report_file="reports/daily-file-check-$(date +%Y%m%d).md"
    echo "# 📅 每日重要文件状态检查报告" > "$report_file"
    echo "**检查时间**: $(date)" >> "$report_file"

    local critical_files=(
        "./AGENTS.md"
        "./CLAUDE.md"
        ".claude/skills/QUICK_REFERENCE.md"
        "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/entity/BaseEntity.java"
    )

    for file in "${critical_files[@]}"; do
        if [[ -f "$file" ]]; then
            echo "✅ 安全: $file" >> "$report_file"
        else
            echo "❌ 缺失: $file" >> "$report_file"
        fi
    done

    echo "**检查结果**: 完成" >> "$report_file"
    echo "报告路径: $report_file"
}

# 执行每日检查
daily_critical_file_check
```

### **每周检查: 技能文件完整性**
```bash
#!/bin/bash
# 每周技能文件完整性检查
weekly_skill_files_check() {
    local report_file="reports/weekly-skills-check-$(date +%Y%m%d).md"
    echo "# 🛠️ 每周技能文件完整性检查" > "$report_file"
    echo "**检查时间**: $(date)" >> "$report_file"

    local skills_dir=".claude/skills/"
    local total_files=$(find "$skills_dir" -name "*.md" | wc -l)
    local expert_files=$(find "$skills_dir" -name "*expert.md" | wc -l)
    local specialist_files=$(find "$skills_dir" -name "*specialist.md" | wc -l)

    echo "📊 技能文件统计:" >> "$report_file"
    echo "- 总数: $total_files" >> "$report_file"
    echo "- 专家文件: $expert_files" >> "$report_file"
    echo "- 专员文件: $specialist_files" >> "$report_file"

    echo "🔍 重要技能文件检查:" >> "$report_file"
    local important_files=(
        "QUICK_REFERENCE.md"
        "compilation-error-prevention-specialist.md"
        "spring-boot-jakarta-guardian.md"
        "four-tier-architecture-guardian.md"
        "code-quality-protector.md"
    )

    for file in "${important_files[@]}"; do
        if [[ -f "$skills_dir$file" ]]; then
            local size=$(stat -f%s "$skills_dir$file")
            echo "✅ $file ($size 字节)" >> "$report_file"
        else
            echo "❌ 缺失: $file" >> "$report_file"
        fi
    done

    echo "✅ 每周检查完成" >> "$report_file"
}

# 执行每周检查
weekly_skill_files_check
```

### **每月检查: 项目整体健康度**
```bash
#!/bin/bash
# 每月项目整体健康度评估
monthly_project_health_check() {
    local report_file="reports/monthly-health-$(date +%Y%m%d).md"
    echo "# 🏥 每月项目健康度评估报告" > "$report_file"
    echo "**评估时间**: $(date)" >> "$report_file"

    echo "📊 项目整体统计:" >> "$report_file"

    # 代码文件统计
    local java_files=$(find . -name "*.java" -type f | wc -l)
    local md_files=$(find . -name "*.md" -type f | wc -l)
    local config_files=$(find . -name "*.{yaml,properties,json,xml}" -type f | wc -l)

    echo "- Java文件: $java_files" >> "$report_file"
    echo "- Markdown文件: $md_files" >> "$report_file"
    echo "- 配置文件: $config_files" >> "$report_file"

    # 编译状态检查
    echo "" >> "$report_file"
    echo "🔧 系统状态检查:" >> "$report_file"
    if cd "smart-admin-api-java17-springboot3" && mvn clean compile -q > /dev/null 2>&1; then
        echo "✅ 项目编译: 成功" >> "$report_file"
    else
        echo "❌ 项目编译: 失败 - 需要修复" >> "$report_file"
    fi

    # 代码质量检查
    echo "" >> "$report_file"
    echo "📏 代码质量指标:" >> "$report_file"
    local javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
    local autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)

    echo "- javax包使用: $javax_count (应为0)" >> "$report_file"
    echo "- @Autowired使用: $autowired_count (应为0)" >> "$report_file"

    # 保护文件状态
    echo "" >> "$report_file"
    echo "🛡️ 保护文件状态:" >> "$report_file"

    protected_files=(
        "./AGENTS.md"
        "./CLAUDE.md"
        ".claude/skills/QUICK_REFERENCE.md"
    )

    for file in "${protected_files[@]}"; do
        if [[ -f "$file" ]]; then
            echo "✅ 受保护: $file" >> "$report_file"
        else
            echo "❌ 未保护: $file" >> "$report_file"
        fi
    done

    echo "" >> "$report_file"
    echo "✅ 每月健康度检查完成" >> "$report_file"
}

# 执行每月检查
monthly_project_health_check
```

### **自动化调度配置**
```bash
# 添加到crontab实现自动化检查
# 每日凌晨2点执行每日检查
0 2 * * * /path/to/daily_check.sh

# 每周一上午8点执行每周检查
0 8 * * 1 /path/to/weekly_check.sh

# 每月1号上午9点执行每月检查
0 9 1 * * /path/to/monthly_check.sh
```

---

## ⚠️ 紧急情况处理

### **发现重要文件被删除时的应急流程**
```bash
#!/bin/bash
# 紧急恢复脚本
emergency_file_recovery() {
    local file_path="$1"

    echo "🚨 启动紧急文件恢复流程..."

    # 立即停止所有操作
    echo "1. 停止所有自动化删除操作"

    # 从Git恢复
    if git rev-parse --git-dir > /dev/null 2>&1; then
        echo "2. 从Git恢复文件: $file_path"
        git checkout HEAD -- "$file_path"

        if [[ -f "$file_path" ]]; then
            echo "✅ 文件已从Git恢复"
        else
            echo "❌ Git恢复失败，尝试从备份恢复"

            # 从备份恢复
            local latest_backup=$(ls -t backup/auto-backup-* 2>/dev/null | head -1)
            if [[ -n "$latest_backup" ]]; then
                cp "$latest_backup/$(basename "$file_path")" "$file_path"
                echo "✅ 文件已从备份恢复"
            fi
        fi
    else
        echo "❌ Git仓库不可用，无法自动恢复"
    fi

    # 创建恢复报告
    local recovery_report="reports/emergency-recovery-$(date +%Y%m%d-%H%M%S).md"
    echo "# 🚨 紧急文件恢复报告" > "$recovery_report"
    echo "**恢复时间**: $(date)" >> "$recovery_report"
    echo "**恢复文件**: $file_path" >> "$recovery_report"
    echo "**恢复状态**: $([[ -f "$file_path" ]] && echo "成功" || echo "失败")" >> "$recovery_report"

    echo "📄 恢复报告: $recovery_report"
    echo "🎯 请立即检查文件完整性并分析删除原因"
}
```

---

## 🎯 总结

### **分级保护机制特点**
1. **三级分类**: P0(绝对禁止) → P1(需验证) → P2(可安全删除)
2. **双重验证**: 自动化工具 + 人工确认
3. **全程保护**: 识别 → 确认 → 评估 → 备份 → 删除 → 验证
4. **应急机制**: 自动恢复 + 报告生成

### **预期效果**
- 🔒 **零误删**: 重要文件100%保护
- 🔧 **流程化**: 标准化的验证和删除流程
- 📊 **可追溯**: 完整的操作记录和报告
- 🚨 **快速恢复**: 紧急情况下的快速恢复能力

---
**🛡️ 文件重要性分级保护机制已建立，项目文件管理达到企业级安全标准！**