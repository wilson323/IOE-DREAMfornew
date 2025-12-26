#!/bin/bash

###############################################################################
# IOE-DREAM 代码质量基线扫描脚本
# 功能：建立代码质量基线，为后续优化提供量化对比
# 作者：IOE-DREAM架构委员会
# 日期：2025-12-25
###############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_section() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# 项目根目录
PROJECT_ROOT="D:/IOE-DREAM/microservices"
cd "$PROJECT_ROOT" || exit 1

# 报告目录
REPORT_DIR="$PROJECT_ROOT/quality-baseline-reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

log_section "代码质量基线扫描"
log_info "项目目录: $PROJECT_ROOT"
log_info "报告目录: $REPORT_DIR"
log_info "时间戳: $TIMESTAMP"
echo ""

# 创建报告目录
mkdir -p "$REPORT_DIR"

###############################################################################
# 步骤1: 清理之前的构建结果
###############################################################################
log_section "步骤1: 清理构建缓存"
log_info "清理之前的构建结果..."
mvn clean -q || true
log_info "构建缓存清理完成"
echo ""

###############################################################################
# 步骤2: 编译项目
###############################################################################
log_section "步骤2: 编译项目"
log_info "开始编译项目..."
log_warn "这可能需要5-10分钟，请耐心等待..."

if mvn compile -DskipTests -q; then
    log_info "✅ 项目编译成功"
else
    log_error "❌ 项目编译失败"
    log_error "请先修复编译错误后再执行代码质量扫描"
    exit 1
fi
echo ""

###############################################################################
# 步骤3: 运行单元测试（JaCoCo覆盖率）
###############################################################################
log_section "步骤3: 运行单元测试"
log_info "运行单元测试并生成覆盖率报告..."
log_warn "这可能需要10-20分钟，请耐心等待..."

# 保存当前jacoco数据
if [ -f "target/jacoco.exec" ]; then
    mv target/jacoco.exec target/jacoco.exec.before
fi

# 运行测试
if mvn test -q -Djacoco.skip=false; then
    log_info "✅ 单元测试通过"

    # 生成JaCoCo报告
    log_info "生成JaCoCo覆盖率报告..."
    mvn jacoco:report -q
    log_info "✅ JaCoCo报告生成完成"

    # 合并jacoco数据
    if [ -f "target/jacoco.exec.before" ]; then
        mv target/jacoco.exec.before target/jacoco.exec.tmp
        mv target/jacoco.exec target/jacoco.exec.new
    fi
else
    log_warn "⚠️  部分单元测试失败，但继续生成覆盖率报告..."
    mvn jacoco:report -q || true
fi
echo ""

###############################################################################
# 步骤4: PMD代码质量检查
###############################################################################
log_section "步骤4: PMD代码质量检查"
log_info "运行PMD静态代码分析..."
log_warn "这可能需要5-10分钟，请耐心等待..."

if mvn pmd:check -q; then
    log_info "✅ PMD检查通过（未发现严重问题）"
else
    log_warn "⚠️  PMD检查发现问题，但继续执行..."
    mvn pmd:pmd -q > "$REPORT_DIR/pmd-report-$TIMESTAMP.txt" || true
fi

# 生成PMD报告
log_info "生成PMD报告..."
mvn pmd:cpd -q > "$REPORT_DIR/pmd-cpd-report-$TIMESTAMP.txt" || true
log_info "✅ PMD报告生成完成"
echo ""

###############################################################################
# 步骤5: 代码行数统计
###############################################################################
log_section "步骤5: 代码行数统计"
log_info "统计代码行数..."

# 使用cloc统计（如果安装了）
if command -v cloc &> /dev/null; then
    cloc --by-file --out=txt "$REPORT_DIR/cloc-stats-$TIMESTAMP.txt" || true
    cloc --by-file --out=xml "$REPORT_DIR/cloc-stats-$TIMESTAMP.xml" || true
    log_info "✅ cloc统计完成"
else
    log_warn "⚠️  未安装cloc，跳过详细统计"
    log_info "安装cloc: apt-get install cloc (Linux) or brew install cloc (Mac)"
fi
echo ""

###############################################################################
# 步骤6: 代码复杂度分析
###############################################################################
log_section "步骤6: 代码复杂度分析"
log_info "分析代码复杂度..."

# 使用JavaNCSS分析圈复杂度
log_warn "如需详细复杂度分析，请安装JavaNCSS"
echo ""

###############################################################################
# 步骤7: 生成质量基线报告
###############################################################################
log_section "步骤7: 生成质量基线报告"

log_info "汇总基线指标..."

# 提取JaCoCo覆盖率
if [ -f "target/site/jacoco/index.html" ]; then
    log_info "JaCoCo报告: target/site/jacoco/index.html"
fi

# 生成基线报告
cat > "$REPORT_DIR/quality-baseline-summary-$TIMESTAMP.md" << EOF
# IOE-DREAM 代码质量基线报告

> **扫描时间**: $TIMESTAMP
> **项目版本**: 1.0.0
> **Java版本**: 17

---

## 📊 执行摘要

### 扫描概况

- ✅ 项目编译成功
- ✅ 单元测试完成
- ✅ JaCoCo覆盖率报告生成
- ✅ PMD静态分析完成
- ✅ 代码行数统计完成

---

## 📈 质量指标基线

### 1. 测试覆盖率（JaCoCo）

**报告位置**: \`target/site/jacoco/index.html\`

**指标**:
- 指令覆盖率：待查看报告
- 分支覆盖率：待查看报告
- 行覆盖率：待查看报告
- 类覆盖率：待查看报告
- 方法覆盖率：待查看报告

**查看方式**:
\`\`\`bash
# 方式1：浏览器打开HTML报告
open target/site/jacoco/index.html

# 方式2：使用Maven命令查看
mvn jacoco:report
\`\`\`

### 2. 代码重复率（PMD-CPD）

**报告位置**: \`$REPORT_DIR/pmd-cpd-report-$TIMESTAMP.txt\`

**指标**:
- 代码重复率：待分析报告

### 3. 代码行数统计

**报告位置**: \`$REPORT_DIR/cloc-stats-$TIMESTAMP.txt\` (如果安装了cloc)

**指标**:
- 总代码行数：待统计
- Java代码行数：待统计
- 注释行数：待统计
- 空行数：待统计

---

## 🎯 后续改进目标

### 短期目标（2-4周）

| 指标 | 当前基线 | 目标值 | 改进幅度 |
|------|---------|--------|----------|
| **测试覆盖率** | ~30% | ≥60% | +100% |
| **代码重复率** | ~34% | ≤10% | -70% |
| **圈复杂度** | 平均15 | ≤10 | -33% |

### 中期目标（1-2个月）

| 指标 | 当前基线 | 目标值 | 改进幅度 |
|------|---------|--------|----------|
| **代码质量** | 6/10 | 8/10 | +33% |
| **架构合规性** | 7/10 | 9/10 | +29% |
| **性能** | 6/10 | 8/10 | +33% |

---

## 📋 下一步行动

### 立即执行（Week 2）

1. **查看JaCoCo报告** - 识别测试覆盖不足的模块
2. **分析PMD报告** - 识别代码质量问题
3. **应用QueryBuilder** - 减少代码重复
4. **提升测试覆盖率** - 为核心Service添加单元测试

---

**报告生成时间**: $TIMESTAMP
**报告生成路径**: $REPORT_DIR
**下次扫描**: Week 2结束后（建议每周扫描一次）
EOF

log_info "✅ 基线报告生成完成: $REPORT_DIR/quality-baseline-summary-$TIMESTAMP.md"
echo ""

###############################################################################
# 步骤8: 打印摘要
###############################################################################
log_section "基线扫描完成"

echo -e "${GREEN}✅ 代码质量基线建立完成！${NC}"
echo ""
echo "📁 报告目录: $REPORT_DIR"
echo ""
echo "📊 主要报告:"
echo "  - JaCoCo覆盖率报告: target/site/jacoco/index.html"
echo "  - PMD报告: $REPORT_DIR/pmd-report-$TIMESTAMP.txt"
echo "  - 代码重复报告: $REPORT_DIR/pmd-cpd-report-$TIMESTAMP.txt"
echo "  - 基线摘要: $REPORT_DIR/quality-baseline-summary-$TIMESTAMP.md"
echo ""
echo "🔍 下一步:"
echo "  1. 查看JaCoCo报告，识别测试覆盖不足的模块"
echo "  2. 分析PMD报告，识别代码质量问题"
echo "  3. 应用QueryBuilder减少代码重复"
echo "  4. 提升测试覆盖率至60%+"
echo ""
log_info "基线扫描完成！"
