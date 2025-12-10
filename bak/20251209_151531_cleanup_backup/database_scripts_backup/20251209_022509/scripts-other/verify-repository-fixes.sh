#!/bin/bash

echo "🔍 验证Repository修复结果..."

# 验证@Repository注解
REPOSITORY_COUNT=$(find . -name "*.java" -not -path "*/target/*" -not -path "*/build/*" -exec grep -l "@Repository" {} \; | wc -l)
echo "📊 剩余@Repository注解文件: $REPOSITORY_COUNT"

# 验证@Mapper注解
MAPPER_COUNT=$(find . -name "*.java" -not -path "*/target/*" -not -path "*/build/*" -exec grep -l "@Mapper" {} \; | wc -l)
echo "📊 @Mapper注解文件: $MAPPER_COUNT"

# 验证Repository命名文件
REPO_NAMING_COUNT=$(find . -name "*Repository.java" -not -path "*/target/*" -not -path "*/build/*" | wc -l)
echo "📊 Repository命名文件: $REPO_NAMING_COUNT"

# 验证Dao命名文件
DAO_NAMING_COUNT=$(find . -name "*Dao.java" -not -path "*/target/*" -not -path "*/build/*" | wc -l)
echo "📊 Dao命名文件: $DAO_NAMING_COUNT"

# 检查还有哪些Repository文件需要处理
if [ $REPOSITORY_COUNT -gt 0 ]; then
    echo ""
    echo "❌ 仍有@Repository文件需要处理:"
    find . -name "*.java" -not -path "*/target/*" -not -path "*/build/*" -exec grep -l "@Repository" {} \; | head -10
fi

if [ $REPO_NAMING_COUNT -gt 0 ]; then
    echo ""
    echo "❌ 仍有Repository命名文件需要处理:"
    find . -name "*Repository.java" -not -path "*/target/*" -not -path "*/build/*" | head -10
fi

if [ $REPOSITORY_COUNT -eq 0 ] && [ $REPO_NAMING_COUNT -eq 0 ]; then
    echo ""
    echo "✅ Repository合规验证通过"
    echo "📊 修复完成统计:"
    echo "  - @Mapper注解文件: $MAPPER_COUNT"
    echo "  - Dao命名文件: $DAO_NAMING_COUNT"
    exit 0
else
    echo ""
    echo "❌ 仍有违规需要处理"
    echo "📊 违规统计:"
    echo "  - @Repository注解: $REPOSITORY_COUNT"
    echo "  - Repository命名: $REPO_NAMING_COUNT"
    exit 1
fi
