#!/bin/bash

# IOE-DREAM 包路径统一化更新脚本
# 用途：批量更新所有导入路径到新的platform包结构
# 作者：IOE-DREAM 架构委员会
# 日期：2025-12-22

set -e

echo "🔧 IOE-DREAM 包路径统一化更新脚本"
echo "======================================"
echo "⚠️  此脚本将更新所有导入路径，请确认已备份代码"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# 检查当前目录
if [ ! -d "microservices" ]; then
    log_error "请在项目根目录执行此脚本"
    exit 1
fi

# 统计函数
count_and_update() {
    local pattern=$1
    local replacement=$2
    local description=$3

    log_info "正在处理: $description"

    # 查找需要更新的文件
    local files=$(find microservices -name "*.java" -exec grep -l "$pattern" {} \;)
    local count=$(echo "$files" | wc -l)

    if [ "$count" -eq 0 ]; then
        log_warning "  没有找到需要更新的文件"
        return
    fi

    log_warning "  找到 $count 个文件需要更新"

    # 确认更新
    echo "$files" | head -3

    # 批量更新
    find microservices -name "*.java" -exec sed -i "s|$pattern|$replacement|g" {} \;

    log_success "  ✓ 已更新 $count 个文件的导入路径"
    echo ""
}

# Phase 1: 更新ResponseDTO导入路径
log_info "Phase 1: 更新ResponseDTO导入路径"
count_and_update "import.*net\.lab1024\.sa\.common\.\(domain\.\)\{0,1\}ResponseDTO" "import net.lab1024.sa.platform.core.dto.ResponseDTO" "ResponseDTO导入路径统一化"

# Phase 2: 更新异常类导入路径
log_info "Phase 2: 更新异常类导入路径"
count_and_update "import.*net\.lab1024\.sa\.common\.exception\." "import net.lab1024.sa.platform.core.exception." "异常类导入路径统一化"

# Phase 3: 更新PageResult导入路径
log_info "Phase 3: 更新PageResult导入路径"
count_and_update "import.*net\.lab1024\.sa\.common\.domain\.PageResult" "import net.lab1024.sa.platform.core.dto.PageResult" "PageResult导入路径统一化"

# Phase 4: 更新工具类导入路径
log_info "Phase 4: 更新工具类导入路径"
count_and_update "import.*net\.lab1024\.sa\.common\.util\." "import net.lab1024.sa.platform.core.util." "工具类导入路径统一化"

# 生成验证报告
log_info "Phase 5: 生成验证报告"
report_file="IMPORT_PATH_UPDATE_REPORT_$(date +%Y%m%d_%H%M%S).md"

cat > "$report_file" << EOF
# IOE-DREAM 导入路径更新报告

## 更新时间
- 开始时间: $(date)
- 执行脚本: scripts/update-import-paths.sh

## 更新统计

### ResponseDTO 导入更新
- 更新前: \`import net.lab1024.sa.common.domain.ResponseDTO\`
- 更新后: \`import net.lab1024.sa.platform.core.dto.ResponseDTO\`

### 异常类 导入更新
- 更新前: \`import net.lab1024.sa.common.exception.*\`
- 更新后: \`import net.lab1024.sa.platform.core.exception.*\`

### PageResult 导入更新
- 更新前: \`import net.lab1024.sa.common.domain.PageResult\`
- 更新后: \`import net.lab1024.sa.platform.core.dto.PageResult\`

### 工具类 导入更新
- 更新前: \`import net.lab1024.sa.common.util.*\`
- 更新后: \`import net.lab1024.sa.platform.core.util.*\`

## 验证检查清单

### 编译验证
- [ ] mvn clean compile -DskipTests
- [ ] 所有服务编译成功
- [ ] 零编译错误

### 运行时验证
- [ ] 各服务启动正常
- [ ] API接口响应正常
- [ ] 异常处理正常

### 功能验证
- [ ] 核心业务功能测试通过
- [ ] 单元测试通过
- [ ] 集成测试通过

## 回滚说明

如需回滚，请执行：
\`\`\`bash
git checkout -- microservices/
git status
\`\`\`

---

报告生成时间: $(date)
EOF

log_success "验证报告已生成: $report_file"
echo ""

log_success "🎉 导入路径统一化完成！"
echo ""
log_info "下一步操作:"
echo "1. 运行编译验证: mvn clean compile -DskipTests"
echo "2. 查看详细报告: $report_file"
echo "3. 如有问题可使用 git checkout -- microservices/ 回滚"
echo ""
log_warning "⚠️  请务必验证编译和运行状态！"