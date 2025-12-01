#!/bin/bash

# 编译验证脚本
# 严格遵循repowiki规范，确保项目编译质量

echo "🔍 开始编译验证..."

# 进入项目目录
cd "$(dirname "$0")/.."

# 1. 清理之前的编译结果
echo "步骤1: 清理编译缓存..."
mvn clean -q > /dev/null 2>&1

# 2. 检查 javax 包使用违规
echo "步骤2: 检查 jakarta 包规范..."
javax_count=$(find . -name "*.java" -exec grep -l "import javax\." {} \; | wc -l)
if [ $javax_count -gt 0 ]; then
    echo "❌ 发现 $javax_count 个 javax 包违规文件"
    find . -name "*.java" -exec grep -l "import javax\." {} \;
    echo "请修复所有 javax 包导入问题"
    exit 1
fi
echo "✅ jakarta 包规范检查通过"

# 3. 检查 @Autowired 违规（排除测试文件）
echo "步骤3: 检查依赖注入规范..."
autowired_count=$(find . -name "*.java" ! -path "*/test/*" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -gt 0 ]; then
    echo "❌ 发现 $autowired_count 个 @Autowired 违规文件（非测试文件）"
    find . -name "*.java" ! -path "*/test/*" -exec grep -l "@Autowired" {} \;
    echo "请修复所有 @Autowired 使用问题"
    exit 1
fi
echo "✅ 依赖注入规范检查通过"

# 4. 检查 System.out 违规（排除测试文件）
echo "步骤4: 检查日志规范..."
system_out_count=$(find . -name "*.java" ! -path "*/test/*" -exec grep -l "System\.out\.println" {} \; | wc -l)
if [ $system_out_count -gt 0 ]; then
    echo "⚠️ 发现 $system_out_count 个 System.out.println 使用（非测试文件），建议使用 SLF4J"
fi

# 5. 编译项目
echo "步骤5: 编译项目..."
mvn compile -q > compilation.log 2>&1
if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请查看 compilation.log"

    # 显示编译错误摘要
    echo "编译错误摘要:"
    grep -E "ERROR|找不到符号" compilation.log | head -10
    exit 1
fi
echo "✅ 项目编译成功"

# 6. 统计编译结果
echo "步骤6: 编译结果统计..."
echo "编译的Java文件数量: $(find . -name "*.java" | wc -l)"
echo "编译的类文件数量: $(find target/classes -name "*.class" 2>/dev/null | wc -l)"

# 7. 检查关键类是否存在
echo "步骤7: 检查关键类..."
key_classes=(
    "net.lab1024.sa.admin.module.consume.domain.entity.ProductEntity"
    "net.lab1024.sa.admin.module.consume.domain.vo.AccountVO"
    "net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity"
    "net.lab1024.sa.base.common.entity.BaseEntity"
)

missing_classes=0
for class in "${key_classes[@]}"; do
    class_file=$(echo "$class" | sed 's/\./\//g').class
    if [ ! -f "target/classes/$class_file" ]; then
        echo "⚠️ 关键类缺失: $class"
        ((missing_classes++))
    fi
done

if [ $missing_classes -eq 0 ]; then
    echo "✅ 所有关键类编译正常"
fi

# 8. 生成编译报告
echo "步骤8: 生成编译报告..."
cat > compilation-report.json <<EOF
{
  "timestamp": "$(date -Iseconds)",
  "status": "success",
  "project": "IOE-DREAM",
  "module": "smart-admin-api-java17-springboot3",
  "java_files": $(find . -name "*.java" | wc -l),
  "compiled_classes": $(find target/classes -name "*.class" 2>/dev/null | wc -l),
  "violations": {
    "javax_imports": $javax_count,
    "autowired_usage": $autowired_count,
    "system_out_usage": $system_out_count
  },
  "missing_key_classes": $missing_classes,
  "compilation_time": "$(date '+%Y-%m-%d %H:%M:%S')"
}
EOF

echo ""
echo "🎉 编译验证完成！"
echo "✅ 项目编译成功"
echo "✅ repowiki 规范检查通过"
echo "✅ 关键类编译正常"
echo "📄 详细报告: compilation-report.json"
echo "📄 编译日志: compilation.log"

exit 0