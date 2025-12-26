#!/bin/bash

# IOE-DREAM 异常处理质量门禁脚本
# 作者: IOE-DREAM 架构委员会
# 版本: 1.0.0
# 日期: 2025-12-22

set -e

echo "========================================"
echo "🚨 IOE-DREAM 异常处理质量门禁检查"
echo "========================================"

# 统计变量
TOTAL_ISSUES=0
TOTAL_FILES=0
ERROR_FILES=0
WARNING_FILES=0
PASSED_FILES=0

# 输出文件
REPORT_FILE="exception-handling-quality-report-$(date +%Y%m%d_%H%M%S).md"
TEMP_DIR="temp_exception_check"

# 创建临时目录
mkdir -p "$TEMP_DIR"

echo "# IOE-DREAM 异常处理质量门禁报告" > "$REPORT_FILE"
echo "## 生成时间: $(date)" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 定义检查规则
declare -A CHECK_RULES=(
    ["IllegalArgumentException"]="禁止使用IllegalArgumentException，必须使用BusinessException"
    ["UnsupportedOperationException"]="禁止使用UnsupportedOperationException，必须使用BusinessException"
    ["throw new.*Exception.*\".*\""]="字符串错误码异常，必须使用ErrorCode枚举"
    ["RuntimeException.*直接抛出"]="禁止直接抛出RuntimeException，必须使用BusinessException或SystemException"
    ["Exception.*catch.*catch.*Exception"]="通用Exception捕获，应该捕获具体异常类型"
)

# 定义质量标准
declare -A QUALITY_STANDARDS=(
    ["ErrorCode枚举使用"]="必须使用ErrorCode枚举定义错误类型"
    ["BusinessException工厂方法"]="必须提供便捷工厂方法简化异常创建"
    ["GlobalExceptionHandler"]="必须使用统一的GlobalExceptionHandler"
    ["异常分类"]="必须按业务异常和系统异常分类处理"
    ["异常日志记录"]="必须记录异常堆栈信息用于故障排查"
)

echo "📋 检查标准定义:" >> "$REPORT_FILE"
for standard in "${!QUALITY_STANDARDS[@]}"; do
    echo "- $standard" >> "$REPORT_FILE"
done
echo "" >> "$REPORT_FILE"

# 遍历所有Java文件进行检查
echo "🔍 开始检查Java文件异常处理质量..."

find . -name "*.java" -type f -not -path "./target/*" -not -path "./.git/*" | while read -r file; do
    ((TOTAL_FILES++))
    file_has_issues=false
    file_issue_count=0
    file_issues=()

    echo "📄 检查文件: $file" | tee -a "$REPORT_FILE"

    for rule_key in "${!CHECK_RULES[@]}"; do
        rule_pattern="$rule_key"
        rule_description="${CHECK_RULES[$rule_key]}"

        # 使用grep搜索模式
        if grep -qE "$rule_pattern" "$file"; then
            file_has_issues=true
            ((file_issue_count++))

            # 获取匹配行号和内容
            grep -nE "$rule_pattern" "$file" | while IFS=: read -r line_num content; do
                issue_entry="❌ 第$line_num行: $content ($rule_description)"
                file_issues+=("$issue_entry")
                echo "  $issue_entry" | tee -a "$REPORT_FILE"
                ((TOTAL_ISSUES++))
            done
        fi
    done

    if [ "$file_has_issues" = true ]; then
        ((ERROR_FILES++))
        file_status="❌ 存在问题"
    else
        # 检查是否使用了正确的异常处理模式
        if grep -q "BusinessException.*ErrorCode\|throw.*BusinessException.*ErrorCode" "$file"; then
            ((PASSED_FILES++))
            file_status="✅ 符合标准"
        else
            ((WARNING_FILES++))
            file_status="⚠️ 未发现异常处理代码"
            echo "  ℹ️ 注意: 文件中未发现异常处理代码，请确认是否遗漏" | tee -a "$REPORT_FILE"
        fi
    fi

    echo "  📊 状态: $file_status" | tee -a "$REPORT_FILE"
    echo "  🔢 问题数量: $file_issue_count" | tee -a "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    # 记录文件状态到临时文件
    echo "$file:$file_status:$file_issue_count" >> "$TEMP_DIR/file_status.txt"
done

echo "" >> "$REPORT_FILE"
echo "## 📊 质量统计摘要" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "| 指标 | 数量 | 百分比 | 状态 |" >> "$REPORT_FILE"
echo "|------|------|--------|------|" >> "$REPORT_FILE"
echo "| 检查文件总数 | $TOTAL_FILES | 100% | - |" >> "$REPORT_FILE"
echo "| 存在问题文件 | $ERROR_FILES | $(echo "scale=1; $ERROR_FILES * 100 / $TOTAL_FILES" | bc)% | ❌ |" >> "$REPORT_FILE"
echo "| 警告文件数 | $WARNING_FILES | $(echo "scale=1; $WARNING_FILES * 100 / $TOTAL_FILES" | bc)% | ⚠️ |" >> "$REPORT_FILE"
echo "| 符合标准文件 | $PASSED_FILES | $(echo "scale=1; $PASSED_FILES * 100 / $TOTAL_FILES" | bc)% | ✅ |" >> "$REPORT_FILE"
echo "| 问题总数 | $TOTAL_ISSUES | - | - |" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 问题分类统计
echo "## 📋 问题分类统计" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 统计各类问题数量
declare -A issue_types=(
    ["IllegalArgumentException"]=0
    ["UnsupportedOperationException"]=0
    ["字符串错误码异常"]=0
    ["RuntimeException直接抛出"]=0
    ["通用异常捕获"]=0
)

# 重新扫描统计问题类型
for rule_key in "${!CHECK_RULES[@]}"; do
    rule_pattern="$rule_key"
    count=$(find . -name "*.java" -type f -not -path "./target/*" -not -path "./.git/*" -exec grep -lE "$rule_pattern" {} \; | wc -l)
    issue_types["$rule_key"]=$count
done

echo "| 问题类型 | 文件数量 | 占比 |" >> "$REPORT_FILE"
echo "|----------|----------|------|" >> "$REPORT_FILE"
for issue_type in "${!issue_types[@]}"; do
    count=${issue_types[$issue_type]}
    if [ "$count" -gt 0 ]; then
        percentage=$(echo "scale=1; $count * 100 / $ERROR_FILES" | bc)
        echo "| $issue_type | $count | ${percentage}% |" >> "$REPORT_FILE"
    fi
done
echo "" >> "$REPORT_FILE"

# 详细问题文件列表
echo "## 📄 问题文件详细列表" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

if [ "$ERROR_FILES" -gt 0 ]; then
    echo "### ❌ 需要修复的文件" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    while IFS=: read -r file_path status issue_count; do
        if [[ "$status" == *"存在问题"* ]]; then
            file_name=$(basename "$file_path")
            echo "#### $file_name" >> "$REPORT_FILE"
            echo "- 问题数量: $issue_count" >> "$REPORT_FILE"
            echo "- 文件路径: $file_path" >> "$REPORT_FILE"
            echo "" >> "$REPORT_FILE"
        fi
    done < "$TEMP_DIR/file_status.txt"
fi

# 修复建议
echo "## 🔧 修复建议和最佳实践" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

echo "### 1. 立即修复 (P0级)" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "- **禁止使用 IllegalArgumentException**" >> "$REPORT_FILE"
echo "  ```java" >> "$REPORT_FILE"
echo "  // ❌ 错误用法" >> "$REPORT_FILE"
echo "  throw new IllegalArgumentException(\"参数错误\");" >> "$REPORT_FILE"
echo "  " >> "$REPORT_FILE"
echo "  // ✅ 正确用法" >> "$REPORT_FILE"
echo "  throw new BusinessException(ErrorCode.PARAM_ERROR, \"参数错误\");" >> "$REPORT_FILE"
echo "  " >> "$REPORT_FILE"
echo "  // ✅ 便捷方法" >> "$REPORT_FILE"
echo "  throw BusinessException.paramError(\"参数错误\");" >> "$REPORT_FILE"
echo "  ```" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

echo "- **禁止使用 UnsupportedOperationException**" >> "$REPORT_FILE"
echo "  ```java" >> "$REPORT_FILE"
echo "  // ❌ 错误用法" >> "$REPORTException-handling-quality-gate.sh: 159" >> "$REPORT_FILE"
echo "  throw new UnsupportedOperationException(\"不支持的操作\");" >> "$REPORT_FILE"
echo "  " >> "$REPORT_FILE"
echo "  // ✅ 正确用法" >> "$REPORT_FILE"
echo "  throw new BusinessException(ErrorCode.OPERATION_NOT_SUPPORTED, \"不支持的操作\");" >> "$REPORT_FILE"
echo "  ```" >> "$_REPORT_FILE"
echo "" >> "$REPORT_FILE"

echo "- **使用 ErrorCode 枚举**" >> "$REPORT_FILE"
echo "  ```java" >> "$REPORT_FILE"
echo "  // ❌ 错误用法" >> "$REPORT_FILE"
echo "  throw new BusinessException(\"USER_NOT_FOUND\", \"用户不存在\");" >> "$REPORT_FILE"
echo "  " >> "$REPORT_FILE"
echo "  // ✅ 正确用法 - 定义ErrorCode枚举" >> "$REPORT_FILE"
echo "  public enum ErrorCode {" >> "$REPORT_FILE"
echo "      USER_NOT_FOUND(\"USER_NOT_FOUND\", \"用户不存在\")," >> "$REPORT_FILE"
echo "      INVALID_PARAMETER(\"INVALID_PARAMETER\", \"参数无效\")," >> "$REPORT_FILE"
echo "      OPERATION_NOT_SUPPORTED(\"OPERATION_NOT_SUPPORTED\", \"不支持的操作\");" >> "$REPORT_FILE"
echo "  }" >> "$REPORT_FILE"
echo "  " >> "$REPORT_FILE"
echo "  // 使用枚举" >> "$REPORT_FILE"
echo "  throw new BusinessException(ErrorCode.USER_NOT_FOUND, \"用户不存在\", userId);" >> "$REPORT_FILE"
echo "  ```" >> "$REPORT_FILE"
echo "" >>_REPORT_FILE"

echo "### 2. 优化建议 (P1级)" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "- **增加便捷工厂方法**" >> "$REPORT_FILE"
echo "  ```java" >> "$REPORT_FILE"
echo "  public class BusinessException extends RuntimeException {" >> "$REPORT_FILE"
echo "      // 便捷工厂方法" >> "$REPORT_FILE"
echo "      public static BusinessException paramEmpty(String paramName) {" >> "$REPORT_FILE"
echo "          return new BusinessException(ErrorCode.PARAM_EMPTY, \"参数不能为空: \" + paramName);" >> "$REPORT_FILE"
echo "      }" >> "$REPORT_FILE"
echo "      " >> "$REPORT_FILE"
echo "      public static BusinessException notFound(Object businessId) {" >> "$REPORT_FILE"
echo "          return new BusinessException(ErrorCode.NOT_FOUND, \"资源不存在\", businessId);" >> "$REPORT_FILE"
echo "      }" >> "$REPORT_FILE"
echo "      " >> "$REPORT_FILE"
echo "      public static BusinessException duplicate(String key) {" >> "$REPORT_FILE"
echo "          return new BusinessException(ErrorCode.DUPLICATE, \"资源已存在: \" + key, key);" >> "$REPORT_FILE"
echo "      }" >> "$REPORT_FILE"
echo "  }" >> "$REPORT_FILE"
echo "  ```" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

echo "- **统一异常分类**" >> "$REPORT_FILE"
echo "  ```java" >> "$REPORT_FILE"
echo "  // 业务异常 - 可预期，业务逻辑中的错误" >> "$REPORT_FILE"
echo "  throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, \"违反业务规则\");" >> "$REPORT_FILE"
echo "  " >> "$REPORT_FILE"
echo "  // 系统异常 - 不可预期，系统级错误" >> "$REPORT_FILE"
echo "  throw new SystemException(ErrorCode.SYSTEM_ERROR, \"系统内部错误\", e);" >> "$REPORT_FILE"
echo "  ```" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 质量门禁结果
echo "" >> "$REPORT_FILE"
echo "## 🚦 质量门禁结果" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

QUALITY_SCORE=$(echo "scale=1; 100 - ($TOTAL_ISSUES * 2)" | bc)

if [ "$TOTAL_ISSUES" -eq 0 ]; then
    echo "### 🎉 质量门禁: **通过** ✅" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "**恭喜！** 异常处理质量检查完全通过，所有文件都符合统一异常处理标准。" >> "$REPORT_FILE"
    echo "- ✅ 无 IllegalArgumentException 使用" >> "$REPORT_FILE"
    echo "- ✅ 无 UnsupportedOperationException 使用" >> "$REPORT_FILE"
    echo "- ✅ 使用 ErrorCode 枚举模式" >> "$REPORT_FILE"
    echo "- ✅ 异常分类正确" >> "$REPORT_FILE"
    echo "- ✅ 质量评分: $QUALITY_SCORE/100" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    EXIT_CODE=0
else
    echo "### ⚠️ 质量门禁: **失败** ❌" >> "$REPORT_FILE"
    echo "" >> "$报告_FILE"
    echo "**需要修复的问题:**" >> "$REPORT_FILE"
    echo "- ❌ 发现 $TOTAL_ISSUES 个异常处理问题" >> "$REPORT_FILE"
    echo "- ❌ $ERROR_FILES 个文件存在问题" >> "$REPORT_FILE"
    echo "- ❌ 质量评分: $QUALITY_SCORE/100 (低于80分)" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "**修复优先级:**" >> "$REPORT_FILE"
    echo "1. **P0级**: 立即修复 IllegalArgumentException 和 UnsupportedOperationException" >> "$REPORT_FILE"
    echo "2. **P1级**: 统一使用 ErrorCode 枚举模式" >> "$REPORT_FILE"
    echo "3. **P2级**: 增加便捷工厂方法和异常分类" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "**修复命令建议:**" >> "$REPORT_FILE"
    echo "1. 修复 IllegalArgumentException:" >> "$REPORT_FILE"
    echo "   find . -name \"*.java\" -exec sed -i 's/throw new IllegalArgumentException(/throw new BusinessException(ErrorCode.PARAM_ERROR, /g' {} \;" >> "$REPORT_FILE"
    echo "2. 修复 UnsupportedOperationException:" >> "$REPORT_FILE"
    echo "   find . -name \"*.java\" -exec sed -i 's/throw new UnsupportedOperationException(/throw new BusinessException(ErrorCode.OPERATION_NOT_SUPPORTED, /g' {} \;" >> "$REPORT_FILE"
    echo "3. 验证修复结果:" >> "$REPORT_FILE"
    echo "   ./scripts/exception-handling-quality-gate.sh" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    EXIT_CODE=1
fi

# 清理临时文件
rm -rf "$TEMP_DIR"

echo "" >> "$REPORT_FILE"
echo "---" >> "$REPORT_FILE"
echo "**报告生成时间**: $(date)" >> "$REPORT_FILE"
echo "**质量评分**: $QUALITY_SCORE/100" >> "$REPORT_FILE"
echo "**检查标准**: IOE-DREAM 异常处理统一规范" >> "$REPORT_FILE"

echo ""
echo "========================================"
echo "📊 质量门禁检查完成"
echo "========================================"
echo "📊 检查文件总数: $TOTAL_FILES"
echo "🚫 问题文件数量: $ERROR_FILES"
echo "⚠️  警告文件数量: $WARNING_FILES"
echo "✅ 符合标准文件: $PASSED_FILES"
echo "🔢 发现问题总数: $TOTAL_ISSUES"
echo "📈 质量评分: $QUALITY_SCORE/100"

if [ "$TOTAL_ISSUES" -eq 0 ]; then
    echo "🎉 恭喜！异常处理质量检查通过！"
    echo "📋 详细报告: $REPORT_FILE"
else
    echo "⚠️  发现问题需要修复，请查看详细报告"
    echo "📋 详细报告: $REPORT_FILE"
fi

exit $EXIT_CODE