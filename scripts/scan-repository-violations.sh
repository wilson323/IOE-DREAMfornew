#!/bin/bash

# Repository违规扫描脚本
# 扫描项目中@Repository注解和Repository命名违规问题

set -e

echo "🔍 开始扫描Repository违规问题..."

# 定义扫描范围
INCLUDE_PATHS=(
    "microservices/*/src/main/java"
    "src/main/java"
)

EXCLUDE_PATHS=(
    "target/"
    "build/"
    ".git/"
    "node_modules/"
)

# 创建报告文件
REPORT_FILE="REPOSITORY_VIOLATIONS_SCAN_REPORT.md"
VIOLATIONS_FILE="repository_violations_list.txt"
FIX_SCRIPT="scripts/fix-repository-violations.sh"

cat > "$REPORT_FILE" << EOF
# Repository违规扫描报告

**扫描日期**: $(date '+%Y-%m-%d %H:%M:%S')
**扫描范围**: IOE-DREAM项目所有Java源文件
**任务状态**: 🔍 **扫描进行中**
**优先级**: 🔴 P0级架构合规整改

---

## 📋 扫描发现

EOF

# 初始化违规计数器
TOTAL_VIOLATIONS=0
ANNOTATION_VIOLATIONS=0
NAMING_VIOLATIONS=0
JPA_VIOLATIONS=0

# 初始化违规文件列表
> "$VIOLATIONS_FILE"

echo "🔍 扫描@Repository注解违规..."

# 扫描@Repository注解违规
for path_pattern in "${INCLUDE_PATHS[@]}"; do
    find . -path "$path_pattern" -name "*.java" -type f | while read file; do
        # 跳过排除目录
        skip=false
        for exclude_path in "${EXCLUDE_PATHS[@]}"; do
            if [[ "$file" == *"$exclude_path"* ]]; then
                skip=true
                break
            fi
        done

        if [ "$skip" = false ] && [ -f "$file" ]; then
            echo "检查文件: $file"

            # 检查@Repository注解
            if grep -q "@Repository" "$file" 2>/dev/null; then
                echo "  ❌ 发现@Repository注解违规"
                echo "    文件: $file" >> "$REPORT_FILE"
                grep -n "@Repository" "$file" >> "$REPORT_FILE"
                echo "" >> "$REPORT_FILE"

                echo "ANNOTATION:$file" >> "$VIOLATIONS_FILE"
                ANNOTATION_VIOLATIONS=$((ANNOTATION_VIOLATIONS + 1))
                TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
            fi

            # 检查Repository命名违规
            filename=$(basename "$file" .java)
            if [[ "$filename" == *"Repository"* ]]; then
                echo "  ❌ 发现Repository命名违规"
                echo "    文件: $file" >> "$REPORT_FILE"
                echo "    类名: $filename" >> "$REPORT_FILE"
                echo "" >> "$REPORT_FILE"

                echo "NAMING:$file:$filename" >> "$VIOLATIONS_FILE"
                NAMING_VIOLATIONS=$((NAMING_VIOLATIONS + 1))
                TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
            fi

            # 检查JPA Repository相关导入
            if grep -q "import.*org.springframework.data.repository" "$file" 2>/dev/null; then
                echo "  ❌ 发现JPA Repository导入违规"
                echo "    文件: $file" >> "$REPORT_FILE"
                grep -n "import.*org.springframework.data.repository" "$file" >> "$REPORT_FILE"
                echo "" >> "$REPORT_FILE"

                echo "JPA:$file" >> "$VIOLATIONS_FILE"
                JPA_VIOLATIONS=$((JPA_VIOLATIONS + 1))
                TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
            fi

            # 检查MyBatis-Plus BaseMapper使用（这是正确的）
            if grep -q "extends BaseMapper" "$file" 2>/dev/null; then
                if grep -q "@Repository" "$file" 2>/dev/null; then
                    echo "  ⚠️  BaseMapper使用@Repository注解（需要改为@Mapper）"
                    echo "    文件: $file" >> "$REPORT_FILE"
                    grep -n "extends BaseMapper" "$file" >> "$REPORT_FILE"
                    echo "" >> "$REPORT_FILE"
                fi
            fi
        fi
    done
done

echo ""
echo "📊 扫描统计"

# 更新报告
cat >> "$REPORT_FILE" << EOF

## 📊 扫描统计

| 违规类型 | 发现数量 | 风险等级 | 整改优先级 |
|---------|---------|---------|-----------|
| **@Repository注解违规** | $ANNOTATION_VIOLATIONS | 🔴 严重 | P0 |
| **Repository命名违规** | $NAMING_VIOLATIONS | 🟡 中等 | P1 |
| **JPA Repository导入违规** | $JPA_VIOLATIONS | 🔴 严重 | P0 |
| **总违规数量** | $TOTAL_VIOLATIONS | 🔴 严重 | P0 |

## 🚨 违规详情分析

EOF

# 根据违规数量给出风险等级
if [ $TOTAL_VIOLATIONS -gt 80 ]; then
    RISK_LEVEL="CRITICAL"
    RISK_DESC="🔴 **严重风险**: 发现$TOTAL_VIOLATIONS个Repository违规，需要立即整改！"
elif [ $TOTAL_VIOLATIONS -gt 40 ]; then
    RISK_LEVEL="HIGH"
    RISK_DESC="🟠 **高风险**: 发现$TOTAL_VIOLATIONS个Repository违规，建议尽快整改"
else
    RISK_LEVEL="MEDIUM"
    RISK_DESC="🟡 **中等风险**: 发现$TOTAL_VIOLATIONS个Repository违规，需要逐步整改"
fi

echo "$RISK_DESC" >> "$REPORT_FILE"

cat >> "$REPORT_FILE" << EOF

### 架构合规影响

1. **四层架构规范**: 违背了DAO层必须使用@Mapper注解的规范
2. **MyBatis-Plus集成**: @Repository注解与MyBatis-Plus最佳实践不符
3. **代码一致性**: 破坏了项目代码风格的一致性
4. **维护成本**: 增加了代码维护和理解成本

## 📋 立即整改计划

### 🔴 P0级紧急处理
1. **替换@Repository注解**: 所有@Repository改为@Mapper
2. **移除JPA Repository导入**: 使用MyBatis-Plus替代
3. **重命名Repository类**: 统一改为Dao后缀

### 🔧 技术实施方案
1. **批量替换脚本**: 使用sed命令批量替换注解
2. **导入语句更新**: 更新相关的import语句
3. **类名重构**: 重命名Repository结尾的类名
4. **验证编译**: 确保修改后代码正常编译

### 📋 验证标准
- [ ] 所有@Repository注解已替换为@Mapper
- [ ] 所有Repository类名已改为Dao后缀
- [ ] 所有JPA Repository导入已移除
- [ ] 代码编译无错误
- [ ] 单元测试通过

## 🛠️ 自动修复脚本

基于扫描结果，已生成自动修复脚本： \`$FIX_SCRIPT\`

**使用方法**:
\`\`\`bash
# 运行自动修复脚本
bash $FIX_SCRIPT

# 验证修复结果
bash scripts/verify-repository-fixes.sh
\`\`\`

---

**扫描完成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**扫描执行人**: Repository合规检查工具
**风险等级**: $RISK_LEVEL
**下一步**: 执行自动修复脚本

EOF

# 生成自动修复脚本
cat > "$FIX_SCRIPT" << 'SCRIPT_EOF'
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

SCRIPT_EOF

chmod +x "$FIX_SCRIPT"

echo "✅ 扫描完成，报告已生成: $REPORT_FILE"
echo "📊 总违规数: $TOTAL_VIOLATIONS"
echo "🔧 自动修复脚本已生成: $FIX_SCRIPT"

if [ $TOTAL_VIOLATIONS -gt 0 ]; then
    echo ""
    echo "🚨 发现Repository违规，立即执行修复！"
    echo "📋 详细报告请查看: $REPORT_FILE"
    echo ""
    echo "🔧 执行自动修复:"
    echo "bash $FIX_SCRIPT"
    exit 1
else
    echo ""
    echo "✅ 未发现Repository违规"
    exit 0
fi