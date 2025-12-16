#!/bin/bash
# ============================================================
# IOE-DREAM Repository命名违规修复脚本
# 将所有@Repository替换为@Mapper注解
# ============================================================

echo "🔧 开始Repository命名违规修复..."
echo "修复时间: $(date)"
echo "=================================="

# 查找所有需要修复的Java文件
echo "🔍 搜索使用@Repository注解的Java文件..."
files_to_fix=()
while IFS= read -r -d '' file; do
    if grep -q "@Repository" "$file"; then
        files_to_fix+=("$file")
    fi
done < <(find microservices -name "*.java" -type f -print0)

echo "📊 发现 ${#files_to_fix[@]} 个文件需要修复"

# 修复函数
fix_repository_annotations() {
    local file="$1"
    echo "📝 修复文件: $file"

    # 创建备份
    cp "$file" "${file}.backup.$(date +%Y%m%d_%H%M%S)"

    # 替换@Repository为@Mapper
    sed -i 's/@Repository/@Mapper/g' "$file"

    # 统计替换次数
    local replacements
    replacements=$(grep -c "@Mapper" "$file" || echo "0")
    echo "✅ 修复完成: $file (替换了 $replacements 处)"
}

# 执行修复
for file in "${files_to_fix[@]}"; do
    fix_repository_annotations "$file"
done

echo "=================================="
echo "✅ Repository命名违规修复完成！"
echo "=================================="

# 验证修复结果
echo "🔍 验证修复结果..."
remaining_repositories=0
while IFS= read -r -d '' file; do
    if grep -q "@Repository" "$file"; then
        echo "⚠️ 仍有@Repository注解: $file"
        ((remaining_repositories++))
    fi
done < <(find microservices -name "*.java" -type f -print0)

if [[ $remaining_repositories -eq 0 ]]; then
    echo "🎉 所有@Repository注解已成功替换为@Mapper！"
else
    echo "⚠️ 发现 $remaining_repositories 个文件仍有@Repository注解，需要手动检查"
fi

# 验证Mapper注解数量
echo "📊 修复后统计："
mapper_count=0
while IFS= read -r -d '' file; do
    count=$(grep -c "@Mapper" "$file" || echo "0")
    ((mapper_count += count))
done < <(find microservices -name "*.java" -type f -print0)

echo "✅ 项目中共有 $mapper_count 个@Mapper注解"

echo "=================================="
echo "🎯 建议后续步骤："
echo "1. 运行构建验证: mvn clean compile"
echo "2. 运行测试: mvn test"
echo "3. 检查Spring Boot启动日志"
echo "=================================="