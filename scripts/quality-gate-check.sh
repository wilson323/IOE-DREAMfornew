#!/bin/bash

# =============================================================================
# IOE-DREAM 质量门禁检查脚本
#
# 功能：在CI/CD流水线中执行质量检查，确保代码质量标准
# 检查项：
# 1. 架构合规性检查
# 2. 技术栈版本一致性检查
# 3. 代码质量标准检查
# 4. 依赖结构检查
# 5. 编译通过检查
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

# 检查结果统计
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0
WARNING_CHECKS=0

# 质量门禁配置
GATE_MODE=${1:-"check"}  # check | enforce
REPORT_FILE="quality-gate-report-$(date '+%Y%m%d_%H%M%S').json"

# 开始检查
log_header "🚪 IOE-DREAM 质量门禁检查"
log_info "检查时间: $(date)"
log_info "检查模式: $GATE_MODE"
log_info "报告文件: $REPORT_FILE"
echo

# =============================================================================
# 初始化JSON报告
# =============================================================================
cat > "$REPORT_FILE" << EOF
{
  "checkTime": "$(date -Iseconds)",
  "checkMode": "$GATE_MODE",
  "results": {
    "architecture": {},
    "techStack": {},
    "codeQuality": {},
    "dependency": {},
    "compilation": {}
  },
  "summary": {
    "totalChecks": 0,
    "passedChecks": 0,
    "failedChecks": 0,
    "warningChecks": 0,
    "overallStatus": "UNKNOWN"
  }
}
EOF

# =============================================================================
# 1. 架构合规性检查
# =============================================================================
log_header "📋 第1项: 架构合规性检查"
log_info "检查四层架构规范、@Autowired/@Repository违规、Manager类规范等"

((TOTAL_CHECKS++))

# 运行架构合规性检查
if [ -f "scripts/architecture-compliance-check.sh" ]; then
    log_info "运行架构合规性检查脚本..."

    # 捕获脚本输出
    ARCH_OUTPUT=$(bash scripts/architecture-compliance-check.sh 2>&1 || true)

    # 提取违规数量
    if echo "$ARCH_OUTPUT" | grep -q "总违规数量: 0 项"; then
        log_success "✅ 架构合规性检查通过"
        ((PASSED_CHECKS++))
        ARCH_STATUS="PASS"
        ARCH_VIOLATIONS=0
        ARCH_MESSAGE="无架构违规"
    else
        ARCH_VIOLATIONS=$(echo "$ARCH_OUTPUT" | grep "总违规数量:" | sed 's/.*总违规数量: \([0-9]*\) 项.*/\1/' || echo "1")
        if [ "$ARCH_VIOLATIONS" -gt 0 ]; then
            log_error "❌ 发现 $ARCH_VIOLATIONS 项架构违规"
            ((FAILED_CHECKS++))
            ARCH_STATUS="FAIL"
            ARCH_MESSAGE="发现 $ARCH_VIOLATIONS 项架构违规，需要修复"
        else
            log_warning "⚠️ 架构检查存在警告"
            ((WARNING_CHECKS++))
            ARCH_STATUS="WARN"
            ARCH_MESSAGE="架构检查存在警告"
        fi
    fi
else
    log_warning "⚠️ 架构合规性检查脚本不存在，跳过检查"
    ((WARNING_CHECKS++))
    ARCH_STATUS="SKIP"
    ARCH_MESSAGE="检查脚本不存在"
fi

# 更新JSON报告
if command -v jq >/dev/null 2>&1; then
    jq --arg status "$ARCH_STATUS" --arg violations "$ARCH_VIOLATIONS" --arg message "$ARCH_MESSAGE" '
       .results.architecture = {
         "status": $status,
         "violations": ($violations | tonumber),
         "message": $message
       }
    ' "$REPORT_FILE" > tmp.json && mv tmp.json "$REPORT_FILE"
fi

echo

# =============================================================================
# 2. 技术栈版本一致性检查
# =============================================================================
log_header "📋 第2项: 技术栈版本一致性检查"
log_info "检查Spring Boot、Java、数据库等核心组件版本一致性"

((TOTAL_CHECKS++))

# 运行技术栈验证
if [ -f "scripts/tech-stack-validation.sh" ]; then
    log_info "运行技术栈版本验证脚本..."

    # 捕获脚本输出
    TECH_OUTPUT=$(bash scripts/tech-stack-validation.sh 2>&1 || true)

    # 提取合规性评分
    if echo "$TECH_OUTPUT" | grep -q "恭喜！技术栈版本完全统一"; then
        log_success "✅ 技术栈版本完全统一"
        ((PASSED_CHECKS++))
        TECH_STATUS="PASS"
        TECH_SCORE=100
        TECH_MESSAGE="技术栈版本完全符合企业级标准"
    else
        TECH_SCORE=$(echo "$TECH_OUTPUT" | grep "技术栈合规性评分:" | sed 's/.*技术栈合规性评分: \([0-9]*\)\/.*/\1/' || echo "0")
        if [ "$TECH_SCORE" -ge 85 ]; then
            log_warning "⚠️ 技术栈版本基本统一，评分: $TECH_SCORE/100"
            ((WARNING_CHECKS++))
            TECH_STATUS="WARN"
            TECH_MESSAGE="技术栈版本基本统一，有少量改进空间"
        else
            log_error "❌ 技术栈版本不统一，评分: $TECH_SCORE/100"
            ((FAILED_CHECKS++))
            TECH_STATUS="FAIL"
            TECH_MESSAGE="技术栈版本存在严重不统一问题，需要修复"
        fi
    fi
else
    log_warning "⚠️ 技术栈验证脚本不存在，跳过检查"
    ((WARNING_CHECKS++))
    TECH_STATUS="SKIP"
    TECH_MESSAGE="验证脚本不存在"
    TECH_SCORE=0
fi

# 更新JSON报告
if command -v jq >/dev/null 2>&1; then
    jq --arg status "$TECH_STATUS" --arg score "$TECH_SCORE" --arg message "$TECH_MESSAGE" '
       .results.techStack = {
         "status": $status,
         "score": ($score | tonumber),
         "message": $message
       }
    ' "$REPORT_FILE" > tmp.json && mv tmp.json "$REPORT_FILE"
fi

echo

# =============================================================================
# 3. 代码质量标准检查
# =============================================================================
log_header "📋 第3项: 代码质量标准检查"
log_info "检查Entity文件大小、代码规范、日志标准等"

((TOTAL_CHECKS++))

# 检查超大Entity文件
ENTITY_VIOLATIONS=0
ENTITY_FILES=$(find microservices -name "*Entity.java" -type f -exec wc -l {} \; 2>/dev/null | awk '$1 > 400 {print $2}' | wc -l)

if [ "$ENTITY_FILES" -gt 0 ]; then
    ENTITY_VIOLATIONS=$ENTITY_FILES
    log_error "❌ 发现 $ENTITY_VIOLATIONS 个超大Entity文件(>400行)"
    # 列出违规文件
    find microservices -name "*Entity.java" -type f -exec wc -l {} \; 2>/dev/null | awk '$1 > 400 {print "  - " $2 " (" $1 "行)"}'
fi

# 检查SLF4J违规
SLF4J_VIOLATIONS=0
if command -v rg >/dev/null 2>&1; then
    SLF4J_VIOLATIONS=$(rg "LoggerFactory\.getLogger" microservices --type java | wc -l)
    if [ "$SLF4J_VIOLATIONS" -gt 0 ]; then
        log_error "❌ 发现 $SLF4J_VIOLATIONS 处SLF4J违规使用"
    fi
else
    # 使用grep作为备选方案
    SLF4J_VIOLATIONS=$(find microservices -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null | wc -l)
    if [ "$SLF4J_VIOLATIONS" -gt 0 ]; then
        log_error "❌ 发现 $SLF4J_VIOLATIONS 处SLF4J违规使用"
    fi
fi

# 综合评估
if [ "$ENTITY_VIOLATIONS" -eq 0 ] && [ "$SLF4J_VIOLATIONS" -eq 0 ]; then
    log_success "✅ 代码质量标准检查通过"
    ((PASSED_CHECKS++))
    CODE_STATUS="PASS"
    CODE_MESSAGE="代码质量符合企业级标准"
elif [ "$ENTITY_VIOLATIONS" -gt 0 ] || [ "$SLF4J_VIOLATIONS" -gt 0 ]; then
    TOTAL_VIOLATIONS=$((ENTITY_VIOLATIONS + SLF4J_VIOLATIONS))
    log_error "❌ 发现 $TOTAL_VIOLATIONS 项代码质量问题"
    ((FAILED_CHECKS++))
    CODE_STATUS="FAIL"
    CODE_MESSAGE="发现代码质量问题，需要修复"
else
    log_warning "⚠️ 代码质量检查存在警告"
    ((WARNING_CHECKS++))
    CODE_STATUS="WARN"
    CODE_MESSAGE="代码质量检查存在警告"
fi

# 更新JSON报告
if command -v jq >/dev/null 2>&1; then
    jq --arg status "$CODE_STATUS" --arg violations "$((ENTITY_VIOLATIONS + SLF4J_VIOLATIONS))" --arg message "$CODE_MESSAGE" '
       .results.codeQuality = {
         "status": $status,
         "violations": ($violations | tonumber),
         "entityViolations": '$ENTITY_VIOLATIONS',
         "slf4jViolations": '$SLF4J_VIOLATIONS',
         "message": $message
       }
    ' "$REPORT_FILE" > tmp.json && mv tmp.json "$REPORT_FILE"
fi

echo

# =============================================================================
# 4. 依赖结构检查
# =============================================================================
log_header "📋 第4项: 依赖结构检查"
log_info "检查循环依赖、非法依赖、依赖方向等"

((TOTAL_CHECKS++))

# 基础检查：检查microservices-common的pom.xml
if [ -f "microservices/microservices-common/pom.xml" ]; then
    # 检查是否有非法依赖
    ILLEGAL_DEPS=$(grep -E "microservices-common-(monitor|business)" microservices/microservices-common/pom.xml | wc -l)
    if [ "$ILLEGAL_DEPS" -gt 0 ]; then
        log_error "❌ 发现 microservices-common 存在非法依赖"
        ((FAILED_CHECKS++))
        DEP_STATUS="FAIL"
        DEP_MESSAGE="microservices-common存在非法依赖"
    else
        log_success "✅ microservices-common依赖检查通过"
        ((PASSED_CHECKS++))
        DEP_STATUS="PASS"
        DEP_MESSAGE="基础依赖检查通过"
    fi
else
    log_warning "⚠️ 无法找到microservices-common的pom.xml"
    ((WARNING_CHECKS++))
    DEP_STATUS="WARN"
    DEP_MESSAGE="无法进行依赖检查"
fi

# 更新JSON报告
if command -v jq >/dev/null 2>&1; then
    jq --arg status "$DEP_STATUS" --arg message "$DEP_MESSAGE" '
       .results.dependency = {
         "status": $status,
         "message": $message
       }
    ' "$REPORT_FILE" > tmp.json && mv tmp.json "$REPORT_FILE"
fi

echo

# =============================================================================
# 5. 编译通过检查
# =============================================================================
log_header "📋 第5项: 编译通过检查"
log_info "检查项目是否能够成功编译"

((TOTAL_CHECKS++))

# 检查Maven是否可用
if command -v mvn >/dev/null 2>&1; then
    log_info "运行Maven编译检查..."

    # 运行编译检查（跳过测试以节省时间）
    if mvn clean compile -q -DskipTests > /dev/null 2>&1; then
        log_success "✅ 项目编译通过"
        ((PASSED_CHECKS++))
        COMPILE_STATUS="PASS"
        COMPILE_MESSAGE="项目编译成功"
    else
        log_error "❌ 项目编译失败"
        ((FAILED_CHECKS++))
        COMPILE_STATUS="FAIL"
        COMPILE_MESSAGE="项目编译失败，请检查代码"
    fi
else
    log_warning "⚠️ Maven不可用，跳过编译检查"
    ((WARNING_CHECKS++))
    COMPILE_STATUS="SKIP"
    COMPILE_MESSAGE="Maven工具不可用"
fi

# 更新JSON报告
if command -v jq >/dev/null 2>&1; then
    jq --arg status "$COMPILE_STATUS" --arg message "$COMPILE_MESSAGE" '
       .results.compilation = {
         "status": $status,
         "message": $message
       }
    ' "$REPORT_FILE" > tmp.json && mv tmp.json "$REPORT_FILE"
fi

echo

# =============================================================================
# 生成最终报告
# =============================================================================
log_header "📊 质量门禁检查总结"

# 计算整体状态
if [ "$FAILED_CHECKS" -eq 0 ]; then
    if [ "$WARNING_CHECKS" -eq 0 ]; then
        OVERALL_STATUS="PASS"
        OVERALL_MESSAGE="🎉 所有质量检查通过！代码质量优秀。"
    else
        OVERALL_STATUS="PASS_WITH_WARNINGS"
        OVERALL_MESSAGE="✅ 质量检查通过，但存在少量警告。"
    fi
else
    OVERALL_STATUS="FAIL"
    OVERALL_MESSAGE="❌ 质量检查失败，存在问题需要修复。"
fi

# 更新JSON报告最终统计
if command -v jq >/dev/null 2>&1; then
    jq --arg total "$TOTAL_CHECKS" --arg passed "$PASSED_CHECKS" --arg failed "$FAILED_CHECKS" --arg warning "$WARNING_CHECKS" --arg status "$OVERALL_STATUS" --arg message "$OVERALL_MESSAGE" '
       .summary = {
         "totalChecks": ($total | tonumber),
         "passedChecks": ($passed | tonumber),
         "failedChecks": ($failed | tonumber),
         "warningChecks": ($warning | tonumber),
         "overallStatus": $status,
         "overallMessage": $message
       }
    ' "$REPORT_FILE" > tmp.json && mv tmp.json "$REPORT_FILE"
fi

# 输出总结信息
echo "==============================================="
echo "📊 检查结果统计："
echo "==============================================="
echo "🔢 总检查项: $TOTAL_CHECKS"
echo "✅ 通过检查: $PASSED_CHECKS"
echo "❌ 失败检查: $FAILED_CHECKS"
echo "⚠️ 警告检查: $WARNING_CHECKS"
echo "==============================================="
echo "🎯 整体状态: $OVERALL_STATUS"
echo -e "$OVERALL_MESSAGE"
echo "==============================================="

# 输出详细结果
echo
echo "📋 详细检查结果："
echo "1. 架构合规性: $ARCH_STATUS"
if [ -n "$ARCH_VIOLATIONS" ] && [ "$ARCH_VIOLATIONS" -gt 0 ]; then
    echo "   - 违规数量: $ARCH_VIOLATIONS"
fi
echo "2. 技术栈版本: $TECH_STATUS"
if [ -n "$TECH_SCORE" ]; then
    echo "   - 合规评分: $TECH_SCORE/100"
fi
echo "3. 代码质量: $CODE_STATUS"
if [ "$((ENTITY_VIOLATIONS + SLF4J_VIOLATIONS))" -gt 0 ]; then
    echo "   - 问题数量: $((ENTITY_VIOLATIONS + SLF4J_VIOLATIONS))"
fi
echo "4. 依赖结构: $DEP_STATUS"
echo "5. 编译检查: $COMPILE_STATUS"
echo

log_success "📄 详细质量报告已生成: $REPORT_FILE"

# =============================================================================
# 退出处理
# =============================================================================
if [ "$GATE_MODE" = "enforce" ]; then
    # 强制模式：失败时阻止继续
    if [ "$FAILED_CHECKS" -gt 0 ]; then
        log_error "🚫 质量门禁检查失败，阻止继续执行！"
        echo
        echo "🔧 修复建议："
        echo "1. 查看详细报告: $REPORT_FILE"
        echo "2. 修复失败项后重新运行检查"
        echo "3. 确保所有质量标准都符合要求"
        exit 1
    else
        log_success "🎉 质量门禁检查通过，允许继续执行！"
        exit 0
    fi
else
    # 检查模式：仅输出结果，不阻止
    if [ "$FAILED_CHECKS" -gt 0 ]; then
        log_warning "⚠️ 发现质量问题，建议修复后继续"
        exit 1
    else
        log_success "✅ 质量检查完成"
        exit 0
    fi
fi