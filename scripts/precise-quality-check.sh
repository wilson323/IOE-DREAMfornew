#!/bin/bash

# 精确质量检查脚本 - 只检测实际注解使用，排除注释
echo "🎯 IOE-DREAM 精确质量检查"
echo "=============================="
echo "时间: $(date)"
echo "分支: ${GIT_BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"
echo ""

# 检查结果变量
CHECKS_PASSED=true
TOTAL_VIOLATIONS=0

# 函数：检查实际注解使用（排除注释）
check_actual_annotations() {
    local check_name="$1"
    local pattern="$2"
    local description="$3"

    echo "🔍 $check_name"

    # 查找实际的注解使用（非注释行）
    local violations=$(find microservices -name "*.java" -type f -exec grep -E "^\s*$pattern\b" {} \; 2>/dev/null | wc -l)

    if [ "$violations" -eq 0 ]; then
        echo "   ✅ $description: 0个违规"
        return 0
    else
        echo "   ❌ $description: $violations 个违规"
        echo "   📋 违规文件:"
        find microservices -name "*.java" -type f -exec grep -H -n -E "^\s*$pattern\b" {} \; 2>/dev/null | head -3 | while read -r line; do
            echo "      - $line"
        done
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + violations))
        CHECKS_PASSED=false
        return 1
    fi
}

# 函数：检查命名违规
check_naming_violations() {
    local check_name="$1"
    local pattern="$2"
    local description="$3"

    echo "🔍 $check_name"

    local violations=$(find microservices -name "$pattern" -type f 2>/dev/null | wc -l)

    if [ "$violations" -eq 0 ]; then
        echo "   ✅ $description: 0个违规"
        return 0
    else
        echo "   ❌ $description: $violations 个违规"
        echo "   📋 违规文件:"
        find microservices -name "$pattern" -type f 2>/dev/null | head -3 | while read -r file; do
            echo "      - $file"
        done
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + violations))
        CHECKS_PASSED=false
        return 1
    fi
}

# 函数：检查明文密码（排除环境变量）
check_plain_passwords() {
    local check_name="安全配置检查"

    echo "🔍 $check_name"

    # 查找明文密码（排除环境变量、加密格式和注释）
    local violations=$(grep -r "^[^#]*password.*=" microservices --include="*.yml" --include="*.properties" --include="*.yaml" 2>/dev/null | grep -v "ENC(" | grep -v "\${.*}" | wc -l)

    if [ "$violations" -eq 0 ]; then
        echo "   ✅ 明文密码检查: 0个违规"
        return 0
    else
        echo "   ❌ 明文密码检查: $violations 个违规"
        echo "   📋 违规配置:"
        grep -r "password.*=" microservices --include="*.yml" --include="*.properties" --include="*.yaml" 2>/dev/null | grep -v "ENC(" | grep -v "\$\{" | head -3 | while read -r line; do
            echo "      - $line"
        done
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + violations))
        CHECKS_PASSED=false
        return 1
    fi
}

# 执行精确检查

echo "📋 架构规范检查"
echo "----------------"

check_actual_annotations "SLF4J 日志规范" "LoggerFactory\.getLogger" "LoggerFactory使用"
check_actual_annotations "依赖注入规范" "@Autowired" "@Autowired 注解"
check_actual_annotations "数据访问层规范" "@Repository" "@Repository 注解"
check_naming_violations "DAO 命名规范" "*Repository.java" "Repository 后缀命名"
check_plain_passwords

echo ""
echo "=============================="
echo "📊 精确质量检查结果:"

# 计算质量评分
if [ "$TOTAL_VIOLATIONS" -eq 0 ]; then
    quality_score=100
    grade="A+"
    status="✅ 完美"
elif [ "$TOTAL_VIOLATIONS" -le 2 ]; then
    quality_score=95
    grade="A+"
    status="✅ 优秀"
elif [ "$TOTAL_VIOLATIONS" -le 5 ]; then
    quality_score=85
    grade="A"
    status="✅ 良好"
elif [ "$TOTAL_VIOLATIONS" -le 10 ]; then
    quality_score=75
    grade="B"
    status="⚠️ 一般"
elif [ "$TOTAL_VIOLATIONS" -le 20 ]; then
    quality_score=60
    grade="C"
    status="❌ 需改进"
else
    quality_score=40
    grade="D"
    status="❌ 较差"
fi

echo "   总违规数: $TOTAL_VIOLATIONS"
echo "   质量评分: $quality_score/100"
echo "   质量等级: $grade ($status)"

echo ""
echo "=============================="

if $CHECKS_PASSED; then
    echo "🎉 精确质量门禁检查通过！"
    echo "✅ 代码完全符合 IOE-DREAM 架构规范"
    echo ""
    echo "🚀 可以安全提交和部署"
    exit 0
else
    echo "⚠️ 精确质量门禁检查未通过"
    echo "❌ 发现架构违规，需要修复"
    echo ""
    echo "🔧 修复建议:"
    echo "1. 查看上述违规详情"
    echo "2. 运行对应的修复脚本"
    echo "3. 重新运行质量检查"
    echo ""
    exit 1
fi