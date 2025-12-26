#!/bin/bash

# API标准化合规性验证脚本
echo "开始API标准化合规性验证..."
echo ""

# 统计修复结果
total_issues_fixed=0

echo "============================================"
echo "1. 变量路径修复验证"
echo "============================================"

variable_path_fixed=0
variable_path_total=0

for file in $(find microservices/ioedream-consume-service/src/main/java -name "*Controller.java" -type f); do
    # 检查是否还有使用变量路径的RequestMapping
    if grep -q "@RequestMapping.*{.*}" "$file"; then
        echo "❌ $(basename "$file") 仍使用变量路径"
        variable_path_total=$((variable_path_total + 1))
    else
        echo "✅ $(basename "$file") 路径规范"
    fi
    variable_path_fixed=$((variable_path_fixed + 1))
done

echo "变量路径修复: $((variable_path_fixed - variable_path_total))/$variable_path_fixed"
total_issues_fixed=$((total_issues_fixed + variable_path_total))

echo ""
echo "============================================"
echo "2. 日志记录覆盖率验证"
echo "============================================"

log_coverage=0
log_total=0

for file in $(find microservices/ioedream-consume-service/src/main/java -name "*Controller.java" -type f); do
    method_count=$(grep -c "@\(Get\|Post\|Put\|Delete\|Patch\)Mapping" "$file" 2>/dev/null || echo 0)
    log_count=$(grep -c "log\.\(info\|error\|warn\)" "$file" 2>/dev/null || echo 0)

    # 计算日志覆盖率（日志数量 / 方法数量 * 100%）
    if [ $method_count -gt 0 ]; then
        coverage=$((log_count * 100 / method_count))
        if [ $coverage -ge 80 ]; then
            echo "✅ $(basename "$file") 日志覆盖率: ${coverage}%"
            log_coverage=$((log_coverage + 1))
        else
            echo "⚠️  $(basename "$file") 日志覆盖率: ${coverage}% (建议≥80%)"
        fi
    else
        echo "⚠️  $(basename "$file") 无API方法"
    fi

    log_total=$((log_total + 1))
done

echo "日志记录合规: $log_coverage/$log_total"

echo ""
echo "============================================"
echo "3. RESTful API方法验证"
echo "============================================"

restful_compliant=0
restful_total=0

for file in $(find microservices/ioedream-consume-service/src/main/java -name "*Controller.java" -type f); do
    # 检查是否还有POST用于查询的情况
    post_query_count=$(grep -A 1 "@PostMapping.*[Qq]uery\|@PostMapping.*[Ll]ist\|@PostMapping.*[Ss]earch" "$file" | grep -c "@PostMapping" 2>/dev/null || echo 0)

    if [ $post_query_count -gt 0 ]; then
        echo "❌ $(basename "$file") 有 $post_query_count 个查询使用POST"
    else
        echo "✅ $(basename "$file") RESTful规范合规"
        restful_compliant=$((restful_compliant + 1))
    fi
    restful_total=$((restful_total + 1))
done

echo "RESTful规范合规: $restful_compliant/$restful_total"

echo ""
echo "============================================"
echo "4. OpenAPI注解覆盖率验证"
echo "============================================"

api_coverage=0
api_total=0

for file in $(find microservices/ioedream-consume-service/src/main/java -name "*Controller.java" -type f); do
    method_count=$(grep -c "@\(Get\|Post\|Put\|Delete\|Patch\)Mapping" "$file" 2>/dev/null || echo 0)
    operation_count=$(grep -c "@Operation" "$file" 2>/dev/null || echo 0)

    if [ $method_count -gt 0 ] && [ $method_count -eq $operation_count ]; then
        echo "✅ $(basename "$file")) OpenAPI注解完整"
        api_coverage=$((api_coverage + 1))
    elif [ $method_count -gt $operation_count ]; then
        echo "⚠️  $(basename "$file") 缺少 $((method_count - operation_count)) 个@Operation注解"
    else
        echo "✅ $(basename "$file") 无API方法"
    fi

    api_total=$((api_total + 1))
done

echo "OpenAPI注解合规: $api_coverage/$api_total"

echo ""
echo "============================================"
echo "🎯 总体合规性评分"
echo "============================================"

total_checks=4
compliant_checks=0

# 评分标准
score=0
if [ $((variable_path_fixed - variable_path_total)) -eq $variable_path_fixed ]; then
    compliant_checks=$((compliant_checks + 1))
    score=$((score + 25))
fi

if [ $log_coverage -gt 0 ]; then
    compliant_checks=$((compliant_checks + 1))
    score=$((score + 25))
fi

if [ $restful_compliant -eq $restful_total ]; then
    compliant_checks=$((compliant_checks + 1))
    score=$((score + 25))
fi

if [ $api_coverage -eq $api_total ]; then
    compliant_checks=$((compliant_checks + 1))
    score=$((score + 25))
fi

echo "合规检查项: $compliant_checks/$total_checks"
echo "总体评分: $score/100"

echo ""
if [ $score -eq 100 ]; then
    echo "🎉 恭喜！API标准化完全合规！"
elif [ $score -ge 80 ]; then
    echo "✅ API标准化基本合规，有少量改进空间"
elif [ $score -ge 60 ]; then
    echo "⚠️  API标准化部分合规，需要继续改进"
else
    echo "❌ API标准化需要重大改进"
fi

echo ""
echo "修复问题总数: $total_issues_fixed"