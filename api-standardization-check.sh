#!/bin/bash

# API设计标准化检查和修复脚本
echo "开始API设计标准化检查和修复..."

# 统计问题
declare -a issues=()

# 检查所有Controller文件
for file in $(find microservices/ioedream-consume-service/src/main/java -name "*Controller.java" -type f); do
    echo ""
    echo "检查文件: $(basename "$file")"

    # 1. 检查是否使用POST进行查询操作
    post_query_count=$(grep -c "@PostMapping.*[Qq]uery\|@PostMapping.*[Ll]ist\|@PostMapping.*[Ss]earch" "$file" 2>/dev/null || echo 0)
    if [ $post_query_count -gt 0 ]; then
        echo "  ⚠️  发现 $post_query_count 个查询操作使用了POST方法"
        issues+=("POST方法用于查询: $(basename "$file")")
    fi

    # 2. 检查是否有统一的日志记录
    log_count=$(grep -c "log\." "$file" 2>/dev/null || echo 0)
    method_count=$(grep -c "@\(Get\|Post\|Put\|Delete\|Patch\)Mapping" "$file" 2>/dev/null || echo 0)
    if [ $method_count -gt 0 ] && [ $((log_count * 2)) -lt $method_count ]; then
        echo "  ⚠️  日志记录可能不足 (方法数: $method_count, 日志数: $log_count)"
        issues+=("日志记录不足: $(basename "$file")")
    fi

    # 3. 检查RequestMapping路径是否规范
    if grep -q "@RequestMapping.*{.*}" "$file"; then
        echo "  ⚠️  发现使用变量路径的RequestMapping"
        issues+=("路径不规范: $(basename "$file")")
    fi

    # 4. 检查是否有OpenAPI注解
    operation_count=$(grep -c "@Operation" "$file" 2>/dev/null || echo 0)
    if [ $operation_count -eq 0 ]; then
        echo "  ⚠️  缺少OpenAPI @Operation注解"
        issues+=("缺少OpenAPI注解: $(basename "$file")")
    fi

    echo "  ✓ 基本检查完成"
done

echo ""
echo "============================================"
echo "API标准化检查结果总结"
echo "============================================"

if [ ${#issues[@]} -eq 0 ]; then
    echo "✅ 未发现API设计问题"
else
    echo "❌ 发现 ${#issues[@]} 个问题:"
    for issue in "${issues[@]}"; do
        echo "   - $issue"
    done
fi

echo ""
echo "建议的修复措施:"
echo "1. 将查询操作改为GET方法"
echo "2. 为关键API端点添加日志记录"
echo "3. 统一使用/api/v1/前缀"
echo "4. 确保所有方法都有@Operation注解"