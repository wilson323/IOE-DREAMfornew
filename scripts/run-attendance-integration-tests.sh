#!/bin/bash

# 考勤模块集成测试执行脚本
# 统一运行所有集成测试，生成综合测试报告

echo "🚀 开始考勤模块集成测试..."

# 设置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
TEST_REPORT_DIR="$PROJECT_ROOT/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="$TEST_REPORT_DIR/attendance-integration-test-$TIMESTAMP.md"

# 创建测试报告目录
mkdir -p "$TEST_REPORT_DIR"

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$REPORT_FILE"
}

# 开始报告
cat > "$REPORT_FILE" << EOF
# 考勤模块集成测试报告

**测试时间**: $(date '+%Y-%m-%d %H:%M:%S')
**测试环境**: 本地开发环境
**报告生成**: 自动化测试脚本

---

## 测试概述

本文档记录了考勤模块的完整集成测试结果，包括API接口测试、数据一致性验证、前端组件验证等。

EOF

log "创建测试报告: $REPORT_FILE"

# 测试统计
TOTAL_SUITES=0
PASSED_SUITES=0
FAILED_SUITES=0

# 执行API接口测试
log ""
log "🔥 执行API接口测试..."

cd "$PROJECT_ROOT"

if [ -f "scripts/attendance-api-integration-test.sh" ]; then
    log "发现API测试脚本，开始执行..."

    # 设置执行权限
    chmod +x scripts/attendance-api-integration-test.sh

    # 执行API测试
    if bash scripts/attendance-api-integration-test.sh; then
        log "✅ API接口测试通过"
        ((PASSED_SUITES++))
        echo "### 1. API接口测试" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        echo "**结果**: ✅ 通过" >> "$REPORT_FILE"
        echo "- 所有API接口响应正常" >> "$REPORT_FILE"
        echo "- 数据格式验证正确" >> "$REPORT_FILE"
        echo "- 错误处理机制完善" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
    else
        log "❌ API接口测试失败"
        ((FAILED_SUITES++))
        echo "### 1. API接口测试" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        echo "**结果**: ❌ 失败" >> "$REPORT_FILE"
        echo "- 部分API接口存在问题" >> "$REPORT_FILE"
        echo "- 需要检查后端服务状态" >> "$REPORT_FILE"
        echo "- 详细日志请查看测试输出" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
    fi
else
    log "⚠️  未找到API测试脚本"
    ((FAILED_SUITES++))
fi
((TOTAL_SUITES++))

# 执行数据一致性检查
log ""
log "🔥 执行数据一致性检查..."

if [ -f "scripts/attendance-data-consistency-check.py" ]; then
    log "发现数据一致性检查脚本，开始执行..."

    # 检查Python环境
    if command -v python3 &> /dev/null; then
        if python3 scripts/attendance-data-consistency-check.py; then
            log "✅ 数据一致性检查通过"
            ((PASSED_SUITES++))
            echo "### 2. 数据一致性检查" >> "$REPORT_FILE"
            echo "" >> "$REPORT_FILE"
            echo "**结果**: ✅ 通过" >> "$REPORT_FILE"
            echo "- 前后端数据格式一致" >> "$REPORT_FILE"
            echo "- 日期时间格式正确" >> "$REPORT_FILE"
            echo "- 数据验证规则完善" >> "$REPORT_FILE"
            echo "- 分页数据结构正确" >> "$REPORT_FILE"
            echo "" >> "$REPORT_FILE"
        else
            log "❌ 数据一致性检查失败"
            ((FAILED_SUITES++))
            echo "### 2. 数据一致性检查" >> "$REPORT_FILE"
            echo "" >> "$REPORT_FILE"
            echo "**结果**: ❌ 失败" >> "$REPORT_FILE"
            echo "- 数据格式存在问题" >> "$REPORT_FILE"
            echo "- 数据验证逻辑需要完善" >> "$REPORT_FILE"
            echo "" >> "$REPORT_FILE"
        fi
    else
        log "⚠️  Python3环境未安装，跳过数据一致性检查"
        ((FAILED_SUITES++))
        echo "### 2. 数据一致性检查" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        echo "**结果**: ⚠️  跳过" >> "$REPORT_FILE"
        echo "- Python3环境未安装" >> "$REPORT_FILE"
        echo "- 无法执行数据一致性检查" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
    fi
else
    log "⚠️  未找到数据一致性检查脚本"
    ((FAILED_SUITES++))
fi
((TOTAL_SUITES++))

# 执行前端组件验证
log ""
log "🔥 执行前端组件验证..."

if [ -f "scripts/frontend-component-validation.js" ]; then
    log "发现前端组件验证脚本，开始执行..."

    # 检查Node.js环境
    if command -v node &> /dev/null; then
        if node scripts/frontend-component-validation.js; then
            log "✅ 前端组件验证通过"
            ((PASSED_SUITES++))
            echo "### 3. 前端组件验证" >> "$REPORT_FILE"
            echo "" >> "$REPORT_FILE"
            echo "**结果**: ✅ 通过" >> "$REPORT_FILE"
            echo "- 所有组件文件完整" >> "$REPORT_FILE"
            echo "- Vue组件结构正确" >> "$REPORT_FILE"
            echo "- API调用配置正确" >> "$REPORT_FILE"
            echo "- 权限控制完善" >> "$REPORT_FILE"
            echo "- 响应式设计良好" >> "$REPORT_FILE"
            echo "" >> "$REPORT_FILE"
        else
            log "❌ 前端组件验证失败"
            ((FAILED_SUITES++))
            echo "### 3. 前端组件验证" >> "$REPORT_FILE"
            echo "" >> "$REPORT_FILE"
            echo "**结果**: ❌ 失败" >> "$REPORT_FILE"
            echo "- 组件文件缺失" >> "$REPORT_FILE"
            echo("- 组件结构存在问题" >> "$REPORT_FILE")
            echo "- 需要检查前端代码" >> "$REPORT_FILE")
            echo "" >> "$REPORT_FILE"
        fi
    else
        log "⚠️  Node.js环境未安装，跳过前端组件验证"
        ((FAILED_SUITES++))
        echo "### 3. 前端组件验证" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        echo "**结果**: ⚠️  跳过" >> "$REPORT_FILE"
        echo "- Node.js环境未安装" >> "$REPORT_FILE"
        echo "- 无法执行前端组件验证" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
    fi
else
    log "⚠️  未找到前端组件验证脚本"
    ((FAILED_SUITES++))
fi
((TOTAL_SUITES++))

# 检查服务状态
log ""
log "🔥 检查服务状态..."

# 检查后端服务
BACKEND_STATUS="未知"
if curl -f -s "http://localhost:1024/api/health" > /dev/null 2>&1; then
    BACKEND_STATUS="运行中"
    log "✅ 后端服务运行正常"
else
    BACKEND_STATUS="未运行"
    log "❌ 后端服务未运行"
fi

# 检查前端服务
FRONTEND_STATUS="未知"
if curl -f -s "http://localhost:8081" > /dev/null 2>&1; then
    FRONTEND_STATUS="运行中"
    log "✅ 前端服务运行正常"
else
    FRONTEND_STATUS="未运行"
    log "❌ 前端服务未运行"
fi

echo "### 4. 服务状态检查" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "**后端服务**: $BACKEND_STATUS (端口: 1024)" >> "$REPORT_FILE"
echo "**前端服务**: $FRONTEND_STATUS (端口: 8081)" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 测试结果统计
log ""
log "📊 测试结果统计:"
log "   总测试套件: $TOTAL_SUITES"
log "   通过套件: $PASSED_SUITES"
log "   失败套件: $FAILED_SUITES"

# 计算成功率
SUCCESS_RATE=0
if [ $TOTAL_SUITES -gt 0 ]; then
    SUCCESS_RATE=$((PASSED_SUITES * 100 / TOTAL_SUITES))
fi

# 生成测试结果摘要
cat >> "$REPORT_FILE" << EOF

## 测试结果统计

- **总测试套件**: $TOTAL_SUITES
- **通过套件**: $PASSED_SUITES
- **失败套件**: $FAILED_SUITES
- **成功率**: ${SUCCESS_RATE}%

## 服务状态

- **后端服务**: $BACKEND_STATUS
- **前端服务**: $FRONTEND_STATUS

## 测试建议

EOF

if [ $FAILED_SUITES -eq 0 ]; then
    log "🎉 所有测试通过！系统集成良好。"
    echo "🎉 **恭喜！所有集成测试通过！**" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "系统集成状态良好，可以进行生产环境部署。" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
else
    log "⚠️  有 $FAILED_SUITES 个测试套件失败，需要修复。"
    echo "⚠️  **有测试失败，需要处理！**" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "请根据上述测试结果修复相关问题，然后重新运行测试。" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
fi

# 添加后续步骤建议
cat >> "$REPORT_FILE" << EOF
## 后续步骤

1. **如果测试全部通过**:
   - 进行性能测试和压力测试
   - 开始用户验收测试
   - 准备生产环境部署

2. **如果存在失败测试**:
   - 修复失败的问题
   - 重新运行集成测试
   - 确保所有测试通过后再进入下一阶段

3. **持续集成建议**:
   - 设置CI/CD流水线自动运行这些测试
   - 在代码提交时自动触发验证
   - 定期执行回归测试

---

**报告生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**测试脚本版本**: v1.0.0
**联系方式**: 开发团队
EOF

# 输出报告路径
log ""
log "📄 详细测试报告已生成: $REPORT_FILE"
log "📊 测试完成时间: $(date '+%Y-%m-%d %H:%M:%S')"

# 设置退出码
if [ $FAILED_SUITES -eq 0 ]; then
    log "🎉 集成测试完成，所有测试通过！"
    exit 0
else
    log "❌ 集成测试完成，有测试失败，请查看报告进行修复。"
    exit 1
fi