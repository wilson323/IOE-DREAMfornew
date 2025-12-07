#!/bin/bash

# Repository违规自动修复脚本

set -e

echo "🔧 开始自动修复Repository违规..."

VIOLATIONS_FILE="repository_violations_list.txt"
FIXED_COUNT=0

if [ ! -f "$VIOLATIONS_FILE" ]; then
    echo "❌ 未找到违规文件列表，请先运行扫描脚本"
    exit 1
fi

echo "📋 处理注解违规..."

# 处理@Repository注解违规
while IFS= read -r line; do
    if [[ "$line" == ANNOTATION:* ]]; then
        file="${line#ANNOTATION:}"
        echo "  修复文件: $file"

        # 备份原文件
        cp "$file" "$file.backup"

        # 替换@Repository为@Mapper
        sed -i 's/@Repository/@Mapper/g' "$file"

        # 检查是否需要添加import
        if ! grep -q "import org.apache.ibatis.annotations.Mapper" "$file"; then
            # 在package后添加Mapper import
            sed -i '/^package /a import org.apache.ibatis.annotations.Mapper;' "$file"
        fi

        echo "    ✅ @Repository → @Mapper"
        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done < "$VIOLATIONS_FILE"

echo "📋 处理命名违规..."

# 处理Repository命名违规
while IFS= read -r line; do
    if [[ "$line" == NAMING:* ]]; then
        file="${line#NAMING:}"
        filename="${file##*:}"
        file_path="${line%:*}"

        echo "  重命名: $filename"

        # 生成新类名
        new_name="${filename/Repository/Dao}"

        # 备份并重命名文件
        cp "$file_path" "$file_path.backup"
        mv "$file_path" "${file_path/$filename/$new_name}"

        # 更新文件内容中的类名
        sed -i "s/class $filename/class $new_name/g" "${file_path/$filename/$new_name}"

        echo "    ✅ $filename → $new_name"
        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done < "$VIOLATIONS_FILE"

echo "📋 处理JPA违规..."

# 处理JPA Repository导入违规
while IFS= read -r line; do
    if [[ "$line" == JPA:* ]]; then
        file="${line#JPA:}"
        echo "  修复JPA导入: $file"

        # 备份原文件
        cp "$file" "$file.backup"

        # 移除JPA Repository相关导入
        sed -i '/import.*org.springframework.data.repository/d' "$file"

        echo "    ✅ 移除JPA Repository导入"
        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done < "$VIOLATIONS_FILE"

echo ""
echo "✅ 自动修复完成"
echo "📊 修复总数: $FIXED_COUNT"
echo "📋 备份文件: *.backup"

# 创建验证脚本
cat > scripts/verify-repository-fixes.sh << 'VERIFY_EOF'
#!/bin/bash

echo "🔍 验证Repository修复结果..."

# 验证@Repository注解
REPOSITORY_COUNT=$(find . -name "*.java" -exec grep -l "@Repository" {} \; | wc -l)
echo "📊 剩余@Repository注解: $REPOSITORY_COUNT"

# 验证@Mapper注解
MAPPER_COUNT=$(find . -name "*.java" -exec grep -l "@Mapper" {} \; | wc -l)
echo "📊 @Mapper注解数量: $MAPPER_COUNT"

# 验证Repository命名
REPO_NAMING_COUNT=$(find . -name "*Repository.java" | wc -l)
echo "📊 Repository命名文件: $REPO_NAMING_COUNT"

# 验证Dao命名
DAO_NAMING_COUNT=$(find . -name "*Dao.java" | wc -l)
echo "📊 Dao命名文件: $DAO_NAMING_COUNT"

if [ $REPOSITORY_COUNT -eq 0 ] && [ $REPO_NAMING_COUNT -eq 0 ]; then
    echo "✅ Repository合规验证通过"
    exit 0
else
    echo "❌ 仍有违规需要处理"
    exit 1
fi
VERIFY_EOF

chmod +x scripts/verify-repository-fixes.sh

echo ""
echo "🚀 下一步操作:"
echo "1. 运行验证脚本: bash scripts/verify-repository-fixes.sh"
echo "2. 编译项目验证: mvn clean compile"
echo "3. 运行测试验证: mvn test"

