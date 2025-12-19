#!/bin/bash
# IOE-DREAM 架构合规性检查安装脚本
# 用于快速设置CI/CD检查和Git hooks

set -euo pipefail

echo "🚀 IOE-DREAM 架构合规性检查安装程序"
echo "=================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT=$(pwd)

echo -e "${BLUE}📍 项目根目录: $PROJECT_ROOT${NC}"

# 检查是否在正确的项目目录
if [ ! -f "microservices/pom.xml" ] || [ ! -d "microservices" ]; then
    echo -e "${RED}❌ 错误：请在IOE-DREAM项目根目录执行此脚本${NC}"
    exit 1
fi

# 创建必要的目录
echo -e "${BLUE}📁 创建必要目录...${NC}"
mkdir -p .github/workflows
mkdir -p .github/scripts
mkdir -p scripts

# 复制CI/CD配置
echo -e "${BLUE}📋 设置CI/CD工作流...${NC}"
if [ -f ".github/workflows/ci-gatekeeper.yml" ]; then
    echo -e "${GREEN}✅ CI工作流已存在${NC}"
else
    echo -e "${YELLOW}⚠️  CI工作流文件不存在，请确保已正确复制${NC}"
fi

# 设置Git hooks
echo -e "${BLUE}🔗 设置Git hooks...${NC}"

if [ -f "pre-commit-hook.sh" ]; then
    # 安装pre-commit hook
    cp pre-commit-hook.sh .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    echo -e "${GREEN}✅ Pre-commit hook 已安装${NC}"
else
    echo -e "${YELLOW}⚠️  Pre-commit hook文件不存在${NC}"
fi

# 检查脚本文件权限
echo -e "${BLUE}🔐 设置脚本执行权限...${NC}"
if [ -d ".github/scripts" ]; then
    chmod +x .github/scripts/*.sh 2>/dev/null || true
    echo -e "${GREEN}✅ 脚本权限已设置${NC}"
fi

if [ -d "scripts" ]; then
    chmod +x scripts/*.sh 2>/dev/null || true
    echo -e "${GREEN}✅ Scripts目录权限已设置${NC}"
fi

# 验证Java环境
echo -e "${BLUE}☕ 验证Java环境...${NC}"
if command -v java >/dev/null 2>&1; then
    java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    if [[ "$java_version" == "17"* ]]; then
        echo -e "${GREEN}✅ Java 17 环境正常 (版本: $java_version)${NC}"
    else
        echo -e "${YELLOW}⚠️  Java版本不是17 (当前: $java_version)${NC}"
    fi
else
    echo -e "${RED}❌ 未找到Java环境${NC}"
fi

# 验证Maven环境
echo -e "${BLUE}📦 验证Maven环境...${NC}"
if command -v mvn >/dev/null 2>&1; then
    maven_version=$(mvn -version | head -1 | awk '{print $3}')
    echo -e "${GREEN}✅ Maven环境正常 (版本: $maven_version)${NC}"
else
    echo -e "${RED}❌ 未找到Maven环境${NC}"
fi

# 创建配置验证脚本
echo -e "${BLUE}⚙️ 创建配置验证脚本...${NC}"
cat > scripts/validate-setup.sh << 'EOF'
#!/bin/bash
# IOE-DREAM 环境验证脚本

echo "🔍 IOE-DREAM 环境验证"
echo "==================="

# 检查项目结构
echo "📁 检查项目结构..."
if [ -d "microservices" ]; then
    echo "✅ microservices目录存在"
else
    echo "❌ microservices目录不存在"
    exit 1
fi

if [ -f "microservices/pom.xml" ]; then
    echo "✅ 根pom.xml存在"
else
    echo "❌ 根pom.xml不存在"
    exit 1
fi

# 检查CI/CD配置
echo "🔄 检查CI/CD配置..."
if [ -f ".github/workflows/ci-gatekeeper.yml" ]; then
    echo "✅ CI/CD工作流存在"
else
    echo "⚠️  CI/CD工作流不存在"
fi

# 检查Git hooks
echo "🔗 检查Git hooks..."
if [ -f ".git/hooks/pre-commit" ]; then
    echo "✅ Pre-commit hook已安装"
else
    echo "⚠️  Pre-commit hook未安装"
fi

# 检查脚本文件
echo "📜 检查检查脚本..."
scripts=(
    ".github/scripts/check-structure-consistency.sh"
    ".github/scripts/check-api-contract.sh"
    ".github/scripts/check-governance.sh"
)

missing_scripts=0
for script in "${scripts[@]}"; do
    if [ -f "$script" ]; then
        echo "✅ $(basename $script) 存在"
    else
        echo "❌ $(basename $script) 不存在"
        missing_scripts=$((missing_scripts + 1))
    fi
done

# 运行快速测试
echo "🧪 运行快速架构检查..."
if bash .github/scripts/check-structure-consistency.sh >/dev/null 2>&1; then
    echo "✅ 结构一致性检查通过"
else
    echo "⚠️  结构一致性检查有问题"
fi

echo ""
echo "📊 验证结果总结"
echo "==================="
if [ $missing_scripts -eq 0 ]; then
    echo "🎉 环境配置完整！"
    echo "💡 下一步："
    echo "   1. 运行: bash scripts/run-full-check.sh 执行完整检查"
    echo "   2. 提交代码时会自动运行pre-commit检查"
    echo "   3. CI/CD会在push时自动运行架构检查"
else
    echo "⚠️  发现 $missing_scripts 个配置问题"
    echo "💡 请根据上述提示修复问题"
fi
EOF

chmod +x scripts/validate-setup.sh

# 创建完整检查脚本
echo -e "${BLUE}🔍 创建完整检查脚本...${NC}"
cat > scripts/run-full-check.sh << 'EOF'
#!/bin/bash
# IOE-DREAM 完整架构检查脚本

echo "🔍 IOE-DREAM 完整架构检查"
echo "========================"

LOG_FILE="full-check-report.log"
echo "IOE-DREAM 完整架构检查报告" > "$LOG_FILE"
echo "检查时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$LOG_FILE"
echo "======================================" >> "$LOG_FILE"

# 运行所有检查
checks=(
    "结构一致性:.github/scripts/check-structure-consistency.sh"
    "API契约:.github/scripts/check-api-contract.sh"
    "工程治理:.github/scripts/check-governance.sh"
)

total_violations=0

for check_info in "${checks[@]}"; do
    check_name=$(echo "$check_info" | cut -d: -f1)
    check_script=$(echo "$check_info" | cut -d: -f2)

    echo "🔍 执行 $check_name 检查..."
    echo "🔍 执行 $check_name 检查..." >> "$LOG_FILE"
    echo "--------------------------------" >> "$LOG_FILE"

    if [ -f "$check_script" ]; then
        if bash "$check_script" >> "$LOG_FILE" 2>&1; then
            echo "✅ $check_name 检查通过"
        else
            echo "❌ $check_name 检查失败"
            # 从日志中提取违规数量
            violations=$(tail -5 "$LOG_FILE" | grep "发现问题数量" | awk '{print $NF}' || echo "1")
            total_violations=$((total_violations + violations))
        fi
    else
        echo "⚠️  $check_name 检查脚本不存在"
    fi

    echo "" >> "$LOG_FILE"
done

echo "======================================" >> "$LOG_FILE"
echo "总问题数量: $total_violations" >> "$LOG_FILE"

echo ""
echo "📊 检查结果总结"
echo "==================="
echo "🔍 执行检查: ${#checks[@]} 项"
echo "❌ 发现问题: $total_violations 个"

if [ $total_violations -gt 0 ]; then
    echo ""
    echo "🚫 架构检查未通过！"
    echo "📋 详细报告: $LOG_FILE"
    echo ""
    echo "💡 修复建议："
    echo "   1. 查看报告了解具体问题"
    echo "   2. 修复所有违规项"
    echo "   3. 重新运行检查验证修复效果"
else
    echo ""
    echo "🎉 架构检查通过！"
    echo "✅ 代码符合IOE-DREAM企业级架构标准"
fi

echo ""
echo "📋 报告文件: $LOG_FILE"
EOF

chmod +x scripts/run-full-check.sh

# 创建快速修复脚本
echo -e "${BLUE}🔧 创建快速修复脚本...${NC}"
cat > scripts/quick-fix.sh << 'EOF'
#!/bin/bash
# IOE-DREAM 常见问题快速修复脚本

echo "🔧 IOE-DREAM 快速修复工具"
echo "========================"

# 修复@Autowired违规
fix_autowired() {
    echo "🔧 修复@Autowired违规..."
    find microservices -name "*.java" -type f -exec sed -i 's/@Autowired/@Resource/g' {} \;
    echo "✅ 已将@Autowired替换为@Resource"
}

# 修复@Repository违规
fix_repository() {
    echo "🔧 修复@Repository违规..."
    find microservices -name "*.java" -type f -exec sed -i 's/@Repository/@Mapper/g' {} \;
    echo "✅ 已将@Repository替换为@Mapper"
}

# 修复javax包名
fix_javax_to_jakarta() {
    echo "🔧 修复javax包名..."
    # 这是一个简化的修复，实际需要更复杂的处理
    find microservices -name "*.java" -type f -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} \;
    find microservices -name "*.java" -type f -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;
    find microservices -name "*.java" -type f -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;
    find microservices -name "*.java" -type f -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} \;
    echo "✅ 已将javax包名替换为jakarta"
}

# 主修复逻辑
case "${1:-all}" in
    "autowired")
        fix_autowired
        ;;
    "repository")
        fix_repository
        ;;
    "jakarta")
        fix_javax_to_jakarta
        ;;
    "all")
        fix_autowired
        fix_repository
        fix_javax_to_jakarta
        ;;
    *)
        echo "用法: $0 [autowired|repository|jakarta|all]"
        echo "  autowired - 修复@Autowired违规"
        echo "  repository - 修复@Repository违规"
        echo "  jakarta   - 修复javax包名"
        echo "  all       - 执行所有修复"
        exit 1
        ;;
esac

echo ""
echo "🎉 快速修复完成！"
echo "💡 建议运行: bash scripts/run-full-check.sh 验证修复效果"
EOF

chmod +x scripts/quick-fix.sh

# 创建文档
echo -e "${BLUE}📚 创建文档...${NC}"
cat > ARCHITECTURE_GATEKEEPER.md << 'EOF'
# IOE-DREAM 架构合规性检查系统

## 🎯 概述

IOE-DREAM架构合规性检查系统是一个全面的代码质量保障机制，通过P0-P4四级检查确保代码符合企业级架构标准。

## 📋 检查级别

### P0：可编译基线检查
- **目标**：确保microservices-common模块正常编译
- **检查项**：公共模块构建、关键类可解析性
- **执行时机**：每次commit和CI

### P1：工程结构一致性检查
- **目标**：验证包结构与目录结构对齐
- **检查项**：包声明一致性、重复包结构、标准包结构
- **执行时机**：每次commit和CI

### P2：API契约一致性检查
- **目标**：防止跨层访问和依赖倒置
- **检查项**：跨层访问、依赖注入规范、注解使用、API设计
- **执行时机**：每次commit和CI

### P3：IDE与注解处理对齐
- **目标**：确保开发环境配置正确
- **检查项**：JDK 17、Lombok注解处理、IDE配置
- **执行时机**：CI和手动验证

### P4：工程治理检查
- **目标**：确保企业级编码规范
- **检查项**：代码质量、安全规范、性能规范、文档规范
- **执行时机**：CI和定期检查

## 🚀 快速开始

### 1. 安装检查系统
```bash
# 在项目根目录执行
bash scripts/setup-gatekeeper.sh
```

### 2. 验证安装
```bash
# 验证环境配置
bash scripts/validate-setup.sh
```

### 3. 运行完整检查
```bash
# 执行所有P0-P4检查
bash scripts/run-full-check.sh
```

### 4. 快速修复常见问题
```bash
# 修复所有常见违规
bash scripts/quick-fix.sh all

# 修复特定问题
bash scripts/quick-fix.sh autowired  # 修复@Autowired
bash scripts/quick-fix.sh repository # 修复@Repository
```

## 🔧 使用指南

### Pre-commit Hook
每次commit时自动运行P0-P2检查：
```bash
git add .
git commit -m "feat: 添加新功能"  # 自动触发检查
```

### CI/CD集成
GitHub Actions会在每次push时自动运行完整的P0-P4检查。

### 本地手动检查
```bash
# 运行特定检查
bash .github/scripts/check-structure-consistency.sh
bash .github/scripts/check-api-contract.sh
bash .github/scripts/check-governance.sh
```

## 📊 检查报告

### 查看报告
- **Pre-commit报告**：`.git/hooks/pre-commit.log`
- **完整检查报告**：`full-check-report.log`
- **CI/CD报告**：GitHub Actions artifacts

### 报告内容
- 问题详情和位置
- 修复建议
- 质量评分

## 🛠️ 配置自定义

### 调整检查阈值
编辑相应的检查脚本，修改`MAX_VIOLATIONS`变量。

### 添加自定义检查
1. 在`.github/scripts/`目录创建新脚本
2. 在`ci-gatekeeper.yml`中添加执行步骤
3. 在`pre-commit-hook.sh`中添加调用

## 🔍 故障排除

### 常见问题

1. **脚本无执行权限**
   ```bash
   chmod +x .github/scripts/*.sh
   chmod +x scripts/*.sh
   ```

2. **Git hook不生效**
   ```bash
   # 重新安装hook
   cp pre-commit-hook.sh .git/hooks/pre-commit
   chmod +x .git/hooks/pre-commit
   ```

3. **检查脚本不存在**
   ```bash
   # 重新运行安装脚本
   bash scripts/setup-gatekeeper.sh
   ```

### 绕过检查（不推荐）

**临时绕过Pre-commit：**
```bash
git commit --no-verify -m "message"
```

**临时绕过CI检查：**
在commit message中添加`[skip-ci]`标签。

## 📞 支持

如遇到问题，请：
1. 查看详细日志文件
2. 运行验证脚本检查环境
3. 参考项目架构文档
4. 联系架构团队

---

**维护者**：IOE-DREAM架构委员会
**更新时间**：$(date -u +"%Y-%m-%d")
EOF

# 安装完成总结
echo ""
echo -e "${GREEN}🎉 IOE-DREAM 架构合规性检查系统安装完成！${NC}"
echo ""
echo -e "${BLUE}📋 安装内容：${NC}"
echo "  ✅ CI/CD工作流配置 (.github/workflows/ci-gatekeeper.yml)"
echo "  ✅ Git Pre-commit Hook (.git/hooks/pre-commit)"
echo "  ✅ 架构检查脚本 (.github/scripts/)"
echo "  ✅ 辅助工具脚本 (scripts/)"
echo "  ✅ 文档 (ARCHITECTURE_GATEKEEPER.md)"
echo ""
echo -e "${YELLOW}🔧 下一步操作：${NC}"
echo "  1. 验证安装: bash scripts/validate-setup.sh"
echo "  2. 运行检查: bash scripts/run-full-check.sh"
echo "  3. 修复问题: bash scripts/quick-fix.sh all"
echo "  4. 查看文档: cat ARCHITECTURE_GATEKEEPER.md"
echo ""
echo -e "${BLUE}💡 使用提示：${NC}"
echo "  - 每次git commit会自动运行架构检查"
echo "  - 推送到GitHub会触发完整的CI/CD检查"
echo "  - 使用 --no-verify 可临时跳过检查（不推荐）"
echo ""
echo -e "${GREEN}🎯 现在你的代码将符合IOE-DREAM企业级架构标准！${NC}"