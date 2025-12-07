#!/bin/bash

# Repository违规直接修复脚本
# 修复所有@Repository注解违规和Repository命名问题

set -e

echo "🔧 开始直接修复Repository违规..."

# 统计违规数量
REPOSITORY_FILES=$(find . -name "*.java" -exec grep -l "@Repository" {} \; | wc -l)
echo "📊 发现@Repository违规文件: $REPOSITORY_FILES"

FIXED_COUNT=0
BACKUP_COUNT=0

# 创建修复日志
LOG_FILE="repository_fix_log_$(date +%Y%m%d_%H%M%S).log"
echo "📋 修复日志: $LOG_FILE"

echo "=== Repository修复开始 ===" > "$LOG_FILE"
echo "时间: $(date)" >> "$LOG_FILE"
echo "违规数量: $REPOSITORY_FILES" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# 处理所有@Repository文件
find . -name "*.java" -exec grep -l "@Repository" {} \; | while read file; do
    echo "处理文件: $file"
    echo "处理文件: $file" >> "$LOG_FILE"

    # 跳过target和build目录
    if [[ "$file" == *"target/"* ]] || [[ "$file" == *"build/"* ]]; then
        echo "  跳过构建目录"
        continue
    fi

    # 备份原文件
    backup_file="${file}.backup"
    cp "$file" "$backup_file"
    echo "  ✅ 备份: $backup_file"
    echo "  备份: $backup_file" >> "$LOG_FILE"
    BACKUP_COUNT=$((BACKUP_COUNT + 1))

    # 替换@Repository为@Mapper
    sed -i 's/@Repository/@Mapper/g' "$file"

    # 检查是否需要添加Mapper import
    if ! grep -q "import org.apache.ibatis.annotations.Mapper" "$file"; then
        # 查找package行号并在其后添加import
        package_line=$(grep -n "^package " "$file" | cut -d: -f1 | head -1)
        if [ -n "$package_line" ]; then
            sed -i "${package_line}a import org.apache.ibatis.annotations.Mapper;" "$file"
            echo "  ✅ 添加Mapper import"
            echo "  添加Mapper import" >> "$LOG_FILE"
        fi
    fi

    # 检查是否有重复的Mapper import
    if [ $(grep -c "import org.apache.ibatis.annotations.Mapper" "$file") -gt 1 ]; then
        # 保留第一个import，删除其余的
        sed -i '0,/import org.apache.ibatis.annotations.Mapper/! { /import org.apache.ibatis.annotations.Mapper/d; }' "$file"
        echo "  ✅ 清理重复Mapper import"
        echo "  清理重复Mapper import" >> "$LOG_FILE"
    fi

    echo "  ✅ @Repository → @Mapper"
    echo "  @Repository → @Mapper" >> "$LOG_FILE"
    FIXED_COUNT=$((FIXED_COUNT + 1))
    echo "" >> "$LOG_FILE"
done

echo ""
echo "🔧 处理Repository命名违规..."

# 处理Repository命名的文件
find . -name "*Repository.java" | while read file; do
    # 跳过已备份和处理的文件
    if [[ "$file" == *".backup" ]]; then
        continue
    fi

    echo "处理命名: $file"
    echo "处理命名: $file" >> "$LOG_FILE"

    # 获取目录和文件名
    dir_path=$(dirname "$file")
    filename=$(basename "$file" .java)

    # 生成新类名
    new_name="${filename/Repository/Dao}"

    if [ "$filename" != "$new_name" ]; then
        # 备份原文件
        backup_file="${file}.backup"
        if [ ! -f "$backup_file" ]; then
            cp "$file" "$backup_file"
            echo "  ✅ 备份: $backup_file"
            echo "  备份: $backup_file" >> "$LOG_FILE"
            BACKUP_COUNT=$((BACKUP_COUNT + 1))
        fi

        # 移动并重命名文件
        new_file="${dir_path}/${new_name}.java"
        mv "$file" "$new_file"

        # 更新文件内容中的类名
        sed -i "s/class $filename/class $new_name/g" "$new_file"
        sed -i "s/interface $filename/interface $new_name/g" "$new_file"

        echo "  ✅ $filename → $new_name"
        echo "  $filename → $new_name" >> "$LOG_FILE"
        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done

echo ""
echo "📊 修复统计"
echo "修复文件数: $FIXED_COUNT"
echo "备份文件数: $BACKUP_COUNT"

echo "=== 修复统计 ===" >> "$LOG_FILE"
echo "修复文件数: $FIXED_COUNT" >> "$LOG_FILE"
echo "备份文件数: $BACKUP_COUNT" >> "$LOG_FILE"
echo "完成时间: $(date)" >> "$LOG_FILE"

# 创建验证脚本
cat > scripts/verify-repository-fixes.sh << 'VERIFY_EOF'
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
VERIFY_EOF

chmod +x scripts/verify-repository-fixes.sh

# 创建恢复脚本（如果需要回滚）
cat > scripts/restore-repository-backups.sh << 'RESTORE_EOF'
#!/bin/bash

echo "🔄 恢复Repository备份文件..."

# 恢复所有备份文件
find . -name "*.backup" | while read backup_file; do
    original_file="${backup_file%.backup}"
    echo "恢复: $backup_file → $original_file"
    cp "$backup_file" "$original_file"
done

echo "✅ 恢复完成"
RESTORE_EOF

chmod +x scripts/restore-repository-backups.sh

echo ""
echo "✅ Repository违规修复完成"
echo "📊 修复总数: $FIXED_COUNT"
echo "📋 备份文件: $BACKUP_COUNT"
echo "📄 详细日志: $LOG_FILE"
echo ""
echo "🔧 下一步操作:"
echo "1. 运行验证脚本: bash scripts/verify-repository-fixes.sh"
echo "2. 编译项目验证: mvn clean compile"
echo "3. 运行测试验证: mvn test"
echo ""
echo "⚠️  如需回滚，运行: bash scripts/restore-repository-backups.sh"