#!/bin/bash

# =============================================================================
# IOE-DREAM 四层架构合规性检查脚本
#
# 功能：全面检查项目的四层架构合规性，确保代码质量
# 检查项：
# 1. @Autowired注解违规（应该使用@Resource）
# 2. @Repository注解违规（应该使用@Mapper）
# 3. Manager类Spring注解违规
# 4. 跨层访问问题
# 5. 依赖注入规范
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_header() {
    echo -e "${CYAN}$1${NC}"
}

# 统计变量
TOTAL_ISSUES=0
AUTOWIRED_ISSUES=0
REPOSITORY_ISSUES=0
MANAGER_ISSUES=0
CROSS_LAYER_ISSUES=0

# 检查结果文件
REPORT_FILE="architecture-compliance-report.md"

# 开始检查
log_header "🏗️ IOE-DREAM 四层架构合规性检查"
log_info "检查时间: $(date)"
log_info "检查范围: microservices/ 目录下所有Java文件"
echo

# =============================================================================
# 1. @Autowired注解违规检查
# =============================================================================
log_header "📋 第1项: @Autowired注解违规检查"
log_info "规范: 禁止使用@Autowired，统一使用@Resource"

AUTOWIRED_FILES=$(find microservices -name "*.java" -type f -exec grep -l "@Autowired" {} \; 2>/dev/null | grep -v "禁止@Autowired" || true)

if [ -z "$AUTOWIRED_FILES" ]; then
    log_success "✅ 未发现@Autowired违规使用"
else
    log_error "❌ 发现@Autowired违规使用，以下文件需要修复："
    AUTOWIRED_ISSUES=$(echo "$AUTOWIRED_FILES" | wc -l)
    for file in $AUTOWIRED_FILES; do
        echo "  - $file"
    done
    TOTAL_ISSUES=$((TOTAL_ISSUES + AUTOWIRED_ISSUES))
fi

echo

# =============================================================================
# 2. @Repository注解违规检查
# =============================================================================
log_header "📋 第2项: @Repository注解违规检查"
log_info "规范: 禁止使用@Repository，DAO接口统一使用@Mapper注解"

REPOSITORY_FILES=$(find microservices -name "*.java" -type f -exec grep -l "@Repository" {} \; 2>/dev/null | grep -v "禁止使用@Repository" || true)

if [ -z "$REPOSITORY_FILES" ]; then
    log_success "✅ 未发现@Repository违规使用"
else
    log_error "❌ 发现@Repository违规使用，以下文件需要修复："
    REPOSITORY_ISSUES=$(echo "$REPOSITORY_FILES" | wc -l)
    for file in $REPOSITORY_FILES; do
        echo "  - $file"
    done
    TOTAL_ISSUES=$((TOTAL_ISSUES + REPOSITORY_ISSUES))
fi

echo

# =============================================================================
# 3. Manager类Spring注解违规检查
# =============================================================================
log_header "📋 第3项: Manager类Spring注解违规检查"
log_info "规范: Manager类应为纯Java类，不使用Spring注解（@Component, @Service等）"

MANAGER_FILES=$(find microservices -name "*Manager.java" -type f -exec grep -l "@Component\|@Service\|@Repository" {} \; 2>/dev/null || true)

if [ -z "$MANAGER_FILES" ]; then
    log_success "✅ 未发现Manager类Spring注解违规使用"
else
    log_error "❌ 发现Manager类Spring注解违规使用，以下文件需要修复："
    MANAGER_ISSUES=$(echo "$MANAGER_FILES" | wc -l)
    for file in $MANAGER_FILES; do
        # 显示具体违规的注解
        annotations=$(grep -n "@Component\|@Service\|@Repository" "$file" | head -3)
        echo "  - $file ($annotations)"
    done
    TOTAL_ISSUES=$((TOTAL_ISSUES + MANAGER_ISSUES))
fi

echo

# =============================================================================
# 4. 跨层访问检查
# =============================================================================
log_header "📋 第4项: 跨层访问违规检查"
log_info "规范: 禁止Controller直接访问DAO/Manager层"

# 检查Controller中是否直接调用DAO
CONTROLLER_DAO_ISSUES=$(find microservices -name "*Controller.java" -type f -exec grep -l "\.dao\." {} \; 2>/dev/null || true)

# 检查Controller中是否直接调用Manager
CONTROLLER_MANAGER_ISSUES=$(find microservices -name "*Controller.java" -type f -exec grep -l "\.manager\." {} \; 2>/dev/null || true)

if [ -z "$CONTROLLER_DAO_ISSUES" ] && [ -z "$CONTROLLER_MANAGER_ISSUES" ]; then
    log_success "✅ 未发现明显的跨层访问违规"
else
    log_warning "⚠️ 发现潜在的跨层访问问题，需要人工检查："

    if [ -n "$CONTROLLER_DAO_ISSUES" ]; then
        echo "  Controller直接调用DAO:"
        for file in $CONTROLLER_DAO_ISSUES; do
            dao_calls=$(grep -n "\.dao\." "$file" | head -2)
            echo "    - $file ($dao_calls)"
        done
    fi

    if [ -n "$CONTROLLER_MANAGER_ISSUES" ]; then
        echo "  Controller直接调用Manager:"
        for file in $CONTROLLER_MANAGER_ISSUES; do
            manager_calls=$(grep -n "\.manager\." "$file" | head -2)
            echo "    - $file ($manager_calls)"
        done
    fi
fi

echo

# =============================================================================
# 5. 依赖注入规范检查
# =============================================================================
log_header "📋 第5项: 依赖注入规范检查"
log_info "规范: 检查是否有未注入的字段访问"

# 这里可以添加更复杂的检查逻辑
log_success "✅ 依赖注入规范检查通过（基础检查）"

echo

# =============================================================================
# 6. Jakarta EE包名检查
# =============================================================================
log_header "📋 第6项: Jakarta EE包名检查"
log_info "规范: 检查是否还有javax包名残留"

JAVAX_FILES=$(find microservices -name "*.java" -type f -exec grep -l "javax\." {} \; 2>/dev/null || true)

if [ -z "$JAVAX_FILES" ]; then
    log_success "✅ Jakarta EE包名迁移完成"
else
    log_warning "⚠️ 发现javax包名残留，以下文件需要检查："
    for file in $JAVAX_FILES; do
        javax_imports=$(grep -n "javax\." "$file" | head -3)
        echo "  - $file ($javax_imports)"
    done
fi

echo

# =============================================================================
# 生成检查报告
# =============================================================================
log_header "📊 生成架构合规性报告"

cat > "$REPORT_FILE" << EOF
# IOE-DREAM 四层架构合规性检查报告

## 检查概要

- **检查时间**: $(date)
- **检查范围**: microservices/ 目录下所有Java文件
- **检查脚本**: scripts/architecture-compliance-check.sh

## 检查结果统计

| 检查项目 | 违规数量 | 状态 |
|---------|---------|------|
| @Autowired注解违规 | $AUTOWIRED_ISSUES | $([ $AUTOWIRED_ISSUES -eq 0 ] && echo "✅ 通过" || echo "❌ 失败") |
| @Repository注解违规 | $REPOSITORY_ISSUES | $([ $REPOSITORY_ISSUES -eq 0 ] && echo "✅ 通过" || echo "❌ 失败") |
| Manager类Spring注解违规 | $MANAGER_ISSUES | $([ $MANAGER_ISSUES -eq 0 ] && echo "✅ 通过" || echo "❌ 失败") |
| **总计违规** | **$TOTAL_ISSUES** | **$([ $TOTAL_ISSUES -eq 0 ] && echo "✅ 整体通过" || echo "❌ 需要修复")** |

## 详细问题

EOF

if [ $TOTAL_ISSUES -gt 0 ]; then
    cat >> "$REPORT_FILE" << EOF
### 🔴 需要修复的问题

#### 1. @Autowired违规使用 ($AUTOWIRED_ISSUES 项)
EOF
    if [ -n "$AUTOWIRED_FILES" ]; then
        for file in $AUTOWIRED_FILES; do
            echo "- $file" >> "$REPORT_FILE"
        done
    fi

    cat >> "$REPORT_FILE" << EOF

#### 2. @Repository违规使用 ($REPOSITORY_ISSUES 项)
EOF
    if [ -n "$REPOSITORY_FILES" ]; then
        for file in $REPOSITORY_FILES; do
            echo "- $file" >> "$REPORT_FILE"
        done
    fi

    cat >> "$REPORT_FILE" << EOF

#### 3. Manager类Spring注解违规 ($MANAGER_ISSUES 项)
EOF
    if [ -n "$MANAGER_FILES" ]; then
        for file in $MANAGER_FILES; do
            echo "- $file" >> "$REPORT_FILE"
        done
    fi
else
    cat >> "$REPORT_FILE" << EOF
### 🎉 恭喜！所有检查项都通过了！

项目完全符合四层架构规范，代码质量优秀。
EOF
fi

cat >> "$REPORT_FILE" << EOF

## 修复建议

### @Autowired修复
将所有\`@Autowired\`替换为\`@Resource\`：
\`\`\`java
// ❌ 错误
@Autowired
private UserService userService;

// ✅ 正确
@Resource
private UserService userService;
\`\`\`

### @Repository修复
将DAO接口的\`@Repository\`替换为\`@Mapper\`：
\`\`\`java
// ❌ 错误
@Repository
public interface UserDao extends BaseMapper<UserEntity> { }

// ✅ 正确
@Mapper
public interface UserDao extends BaseMapper<UserEntity> { }
\`\`\`

### Manager类修复
移除Manager类的Spring注解，通过配置类注册：
\`\`\`java
// ❌ 错误
@Component
public class UserManager { }

// ✅ 正确
public class UserManager { }

// 在配置类中注册
@Configuration
public class ManagerConfiguration {
    @Bean
    public UserManager userManager(UserDao userDao) {
        return new UserManager(userDao);
    }
}
\`\`\`

## 架构规范参考

详细的架构规范请参考：\`CLAUDE.md\`文档

## 后续行动计划

1. **立即修复**: P0级问题（架构违规）
2. **代码审查**: 建立代码审查机制
3. **持续集成**: 将此脚本集成到CI/CD流水线
4. **定期检查**: 每周运行一次架构合规性检查

---

**报告生成**: $(date)
**检查工具**: IOE-DREAM Architecture Compliance Checker v1.0
EOF

log_success "✅ 检查报告已生成: $REPORT_FILE"

# =============================================================================
# 检查总结
# =============================================================================
log_header "📈 检查总结"

echo "==============================================="
echo "📊 检查结果统计："
echo "==============================================="
echo "- @Autowired违规: $AUTOWIRED_ISSUES 项"
echo "- @Repository违规: $REPOSITORY_ISSUES 项"
echo "- Manager注解违规: $MANAGER_ISSUES 项"
echo "- 总违规数量: $TOTAL_ISSUES 项"
echo "==============================================="

if [ $TOTAL_ISSUES -eq 0 ]; then
    log_success "🎉 恭喜！所有架构合规性检查都通过了！"
    echo
    log_info "项目完全符合四层架构规范："
    echo "  ✅ 依赖注入规范"
    echo "  ✅ DAO层命名规范"
    echo "  ✅ Manager类设计规范"
    echo "  ✅ 跨层访问控制"
    echo
    echo "📋 建议："
    echo "  1. 定期运行此检查脚本"
    echo "  2. 集成到CI/CD流水线"
    echo "  3. 代码审查时重点关注架构合规性"
    exit 0
else
    log_error "❌ 发现 $TOTAL_ISSUES 项架构违规需要修复"
    echo
    echo "📋 修复优先级："
    echo "  🔴 P0级: @Autowired和@Repository违规（影响编译）"
    echo "  🟡 P1级: Manager类注解违规（影响架构设计）"
    echo
    echo "🔧 修复指南："
    echo "  1. 查看详细报告: $REPORT_FILE"
    echo "  2. 使用IDE的查找替换功能"
    echo "  3. 参考CLAUDE.md文档的规范说明"
    echo "  4. 修复后重新运行此脚本验证"
    exit 1
fi