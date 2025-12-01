#!/bin/bash
# repowiki-compliance-check.sh - repowiki规范符合性检查脚本

echo "🔍 执行repowiki一级规范符合性检查..."

# 1. jakarta包名检查
echo "步骤1: jakarta包名检查"
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
echo "javax包使用数量: $javax_count（必须为0）"

# 允许的javax包（JDK标准库，非Jakarta EE）
allowed_javax_patterns=("javax\.crypto" "javax\.net" "javax\.security" "javax\.naming")
forbidden_javax_count=0

if [ $javax_count -gt 0 ]; then
    echo "检查javax包详情..."
    find . -name "*.java" -exec grep -H "javax\." {} \; | while read -r line; do
        file=$(echo "$line" | cut -d: -f1)
        import_line=$(echo "$line" | cut -d: -f2-)

        # 检查是否为允许的javax包
        is_allowed=false
        for pattern in "${allowed_javax_patterns[@]}"; do
            if [[ "$import_line" =~ $pattern ]]; then
                is_allowed=true
                break
            fi
        done

        # 检查是否为禁止的Jakarta EE包
        forbidden_patterns=("javax\.servlet" "javax\.validation" "javax\.annotation" "javax\.persistence" "javax\.xml\.bind")
        is_forbidden=false
        for pattern in "${forbidden_patterns[@]}"; do
            if [[ "$import_line" =~ $pattern ]]; then
                is_forbidden=true
                forbidden_javax_count=$((forbidden_jbidden_count + 1))
                echo "❌ 禁止的javax包使用: $file -> $import_line"
            fi
        done

        if [[ "$is_allowed" == false && "$is_forbidden" == false ]]; then
            echo "⚠️  可疑的javax包使用: $file -> $import_line"
        fi
    done
fi

# 2. @Resource注入检查
echo "步骤2: @Resource注入检查"
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
echo "@Autowired使用数量: $autowired_count（必须为0）"

if [ $autowired_count -gt 0 ]; then
    echo "❌ 发现@Autowired使用，违反repowiki一级规范:"
    find . -name "*.java" -exec grep -H "@Autowired" {} \;
fi

# 3. 四层架构检查
echo "步骤3: 四层架构合规检查"
architecture_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . 2>/dev/null | wc -l)
echo "架构违规数量: $architecture_violations（必须为0）"

if [ $architecture_violations -gt 0 ]; then
    echo "❌ 发现架构违规，违反repowoki一级规范:"
    grep -r "@Resource.*Dao" --include="*Controller.java" .
fi

# 4. SLF4J vs System.out检查
echo "步骤4: 日志规范检查"
systemout_count=$(find . -name "*.java" -exec grep -l "System\.out\." {} \; | wc -l)
echo "System.out使用数量: $systemout_count（必须为0）"

if [ $systemout_count -gt 0 ]; then
    echo "❌ 发现System.out使用，违反repowoki一级规范:"
    find . -name "*.java" -exec grep -H "System\.out\." {} \;
fi

# 5. 包名混乱检查
echo "步骤5: 包名重复检查"
conflict_count=$(find . -name "*.java" -exec basename {} \; | sort | uniq -d | wc -l)
echo "重复类名数量: $conflict_count"

# 6. 检查vo包中是否有枚举类
echo "步骤6: 包职责检查"
vo_enums=$(find . -path "*/vo/*" -name "*Priority.java" 2>/dev/null | wc -l)
if [ $vo_enums -gt 0 ]; then
    echo "❌ 枚举类应在enums包中，不在vo包中: $vo_enums个"
    find . -path "*/vo/*" -name "*Priority.java" 2>/dev/null
fi

# 7. 检查类型安全
echo "步骤7: 类型安全检查"
primitive_count=$(find . -name "*.java" -exec grep -l "private.*int.*;" {} \; | wc -l)
echo "原生int字段使用数量: $primitive_count"

# 8. 检查JavaDoc注释
echo "步骤8: JavaDoc注释检查"
no_javadoc_count=$(find . -name "*.java" -exec sh -c '
    file="$1"
    # 检查公共类和方法是否有JavaDoc
    public_classes=$(grep -c "public class" "$file")
    public_methods=$(grep -c "public.*(" "$file")
    javadoc_classes=$(grep -c "/\*\*" "$file")
    javadoc_methods=$(grep -c "\*\*.*@param" "$file")

    if [ "$public_classes" -gt 0 ] && [ "$javadoc_classes" -eq 0 ]; then
        echo "⚠️ 缺少类级JavaDoc: $file"
    fi
    if [ "$public_methods" -gt 0 ] && [ "$javadoc_methods" -eq 0 ]; then
        echo "⚠️ 缺少方法级JavaDoc: $file"
    fi
' _ {} \;)

# 结果统计
echo ""
echo "📊 repowiki规范符合性检查结果:"
echo "==================================="
if [ $forbidden_javax_count -eq 0 ] && [ $autowired_count -eq 0 ] && [ $architecture_violations -eq 0 ] && [ $systemout_count -eq 0 ]; then
    echo "🎉 repowiki一级规范检查完全通过！"
    echo "✅ jakarta包名规范: 通过"
    echo "✅ 依赖注入规范: 通过"
    echo "✅ 四层架构规范: 通过"
    echo "✅ 日志使用规范: 通过"
    exit 0
else
    echo "❌ repowiki一级规范检查失败！需要立即修复:"
    echo "  - 禁止的javax包: $forbidden_javax_count个"
    echo "  - @Autowired使用: $autowired_count个"
    echo "  - 架构违规: $architecture_violations个"
    echo "  - System.out使用: $systemout_count个"
    echo "  - 重复类名: $conflict_count个"
    echo "  - vo包中的枚举: $vo_enums个"
    exit 1
fi