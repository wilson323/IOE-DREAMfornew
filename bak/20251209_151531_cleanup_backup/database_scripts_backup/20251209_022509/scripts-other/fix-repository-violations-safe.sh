#!/bin/bash

# Repository违规安全修复脚本
# 修复所有@Repository注解违规和Repository命名问题

set -e

echo "🔧 开始安全修复Repository违规..."

# 统计违规数量
echo "📊 统计违规情况..."
REPOSITORY_FILES=$(find . -name "*.java" -not -path "*/target/*" -not -path "*/build/*" -exec grep -l "@Repository" {} \; | wc -l)
REPOSITORY_NAMING=$(find . -name "*Repository.java" -not -path "*/target/*" -not -path "*/build/*" | wc -l)

echo "📊 @Repository注解违规文件: $REPOSITORY_FILES"
echo "📊 Repository命名违规文件: $REPOSITORY_NAMING"
echo "📊 总违规数量: $((REPOSITORY_FILES + REPOSITORY_NAMING))"

# 创建修复日志
LOG_FILE="repository_fix_log_$(date +%Y%m%d_%H%M%S).log"
echo "📋 修复日志: $LOG_FILE"

echo "=== Repository修复开始 ===" > "$LOG_FILE"
echo "时间: $(date)" >> "$LOG_FILE"
echo "@Repository违规文件: $REPOSITORY_FILES" >> "$LOG_FILE"
echo "Repository命名违规: $REPOSITORY_NAMING" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# 使用临时文件存储要处理的文件列表
REPOSITORY_TEMP_FILE="/tmp/repository_files.tmp"
NAMING_TEMP_FILE="/tmp/naming_files.tmp"

# 生成要处理的文件列表
find . -name "*.java" -not -path "*/target/*" -not -path "*/build/*" -exec grep -l "@Repository" {} \; > "$REPOSITORY_TEMP_FILE"
find . -name "*Repository.java" -not -path "*/target/*" -not -path "*/build/*" > "$NAMING_TEMP_FILE"

FIXED_COUNT=0
BACKUP_COUNT=0

echo ""
echo "🔧 第一阶段：修复@Repository注解违规..."

# 处理@Repository注解文件
while IFS= read -r file; do
    if [ -f "$file" ] && [ -r "$file" ]; then
        echo "处理文件: $file"
        echo "处理文件: $file" >> "$LOG_FILE"

        # 备份原文件
        backup_file="${file}.backup"
        if [ ! -f "$backup_file" ]; then
            cp "$file" "$backup_file"
            echo "  ✅ 备份: $backup_file"
            echo "  备份: $backup_file" >> "$LOG_FILE"
            BACKUP_COUNT=$((BACKUP_COUNT + 1))
        fi

        # 替换@Repository为@Mapper
        sed -i 's/@Repository/@Mapper/g' "$file"

        # 检查是否需要添加Mapper import
        if ! grep -q "import org.apache.ibatis.annotations.Mapper" "$file"; then
            # 查找package行并在其后添加import
            awk '
                /^package / {
                    print $0
                    print "import org.apache.ibatis.annotations.Mapper;"
                    next
                }
                { print }
            ' "$file" > "${file}.tmp" && mv "${file}.tmp" "$file"
            echo "  ✅ 添加Mapper import"
            echo "  添加Mapper import" >> "$LOG_FILE"
        fi

        # 清理可能的重复Mapper import
        while [ $(grep -c "import org.apache.ibatis.annotations.Mapper" "$file") -gt 1 ]; do
            sed -i '0,/import org.apache.ibatis.annotations.Mapper/! { /import org.apache.ibatis.annotations.Mapper/d; }' "$file"
        done

        echo "  ✅ @Repository → @Mapper"
        echo "  @Repository → @Mapper" >> "$LOG_FILE"
        FIXED_COUNT=$((FIXED_COUNT + 1))
        echo "" >> "$LOG_FILE"
    else
        echo "  ⚠️  跳过无效文件: $file"
    fi
done < "$REPOSITORY_TEMP_FILE"

echo ""
echo "🔧 第二阶段：修复Repository命名违规..."

# 处理Repository命名文件
while IFS= read -r file; do
    if [ -f "$file" ] && [ -r "$file" ] && [[ "$file" != *".backup" ]]; then
        echo "处理命名: $file"
        echo "处理命名: $file" >> "$LOG_FILE"

        # 获取文件名（不含路径和扩展名）
        filename=$(basename "$file" .java)

        # 生成新类名
        new_name="${filename/Repository/Dao}"

        if [ "$filename" != "$new_name" ]; then
            # 获取目录路径
            dir_path=$(dirname "$file")

            # 备份原文件
            backup_file="${file}.backup"
            if [ ! -f "$backup_file" ]; then
                cp "$file" "$backup_file"
                echo "  ✅ 备份: $backup_file"
                echo "  备份: $backup_file" >> "$LOG_FILE"
                BACKUP_COUNT=$((BACKUP_COUNT + 1))
            fi

            # 创建新文件
            new_file="${dir_path}/${new_name}.java"

            # 复制并重命名文件
            cp "$file" "$new_file"

            # 更新文件内容中的类名
            sed -i "s/\\bclass $filename\\b/class $new_name/g" "$new_file"
            sed -i "s/\\binterface $filename\\b/interface $new_name/g" "$new_file"

            # 删除原文件
            rm "$file"

            echo "  ✅ $filename → $new_name"
            echo "  $filename → $new_name" >> "$LOG_FILE"
            FIXED_COUNT=$((FIXED_COUNT + 1))
        else
            echo "  ⚠️  文件名无需修改: $filename"
        fi
        echo "" >> "$LOG_FILE"
    else
        echo "  ⚠️  跳过无效文件: $file"
    fi
done < "$NAMING_TEMP_FILE"

# 清理临时文件
rm -f "$REPOSITORY_TEMP_FILE" "$NAMING_TEMP_FILE"

echo ""
echo "📊 修复统计"
echo "修复操作数: $FIXED_COUNT"
echo "备份文件数: $BACKUP_COUNT"

echo "=== 修复统计 ===" >> "$LOG_FILE"
echo "修复操作数: $FIXED_COUNT" >> "$LOG_FILE"
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
    find . -name "*.java" -not -path "*/target/*" -not -path "*/build/*" -exec grep -l "@Repository" {} \;
fi

if [ $REPO_NAMING_COUNT -gt 0 ]; then
    echo ""
    echo "❌ 仍有Repository命名文件需要处理:"
    find . -name "*Repository.java" -not -path "*/target/*" -not -path "*/build/*"
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

RESTORED_COUNT=0

# 恢复所有备份文件
find . -name "*.backup" | while read backup_file; do
    if [ -f "$backup_file" ]; then
        original_file="${backup_file%.backup}"
        echo "恢复: $backup_file → $original_file"
        cp "$backup_file" "$original_file"
        RESTORED_COUNT=$((RESTORED_COUNT + 1))
    fi
done

echo "✅ 恢复完成，共恢复 $RESTORED_COUNT 个文件"
RESTORE_EOF

chmod +x scripts/restore-repository-backups.sh

echo ""
echo "✅ Repository违规修复完成"
echo "📊 修复操作数: $FIXED_COUNT"
echo "📋 备份文件数: $BACKUP_COUNT"
echo "📄 详细日志: $LOG_FILE"
echo ""
echo "🔧 下一步操作:"
echo "1. 运行验证脚本: bash scripts/verify-repository-fixes.sh"
echo "2. 编译项目验证: mvn clean compile"
echo "3. 运行测试验证: mvn test"
echo ""
echo "⚠️  如需回滚，运行: bash scripts/restore-repository-backups.sh"