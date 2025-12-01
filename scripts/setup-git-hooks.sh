#!/bin/bash

##############################################################################
# Git Pre-commit Hook 配置脚本
# 
# 功能：自动配置Git pre-commit hook，确保每次提交前自动执行代码检查
# 用法：./scripts/setup-git-hooks.sh
##############################################################################

echo "🔧 配置Git Pre-commit Hook"
echo "================================"

# 检查是否在Git仓库中
if [ ! -d ".git" ]; then
    echo "❌ 错误：当前目录不是Git仓库"
    exit 1
fi

# 创建pre-commit hook文件
HOOK_FILE=".git/hooks/pre-commit"

echo "📝 创建pre-commit hook文件..."

cat > "$HOOK_FILE" << 'EOF'
#!/bin/bash

##############################################################################
# Git Pre-commit Hook - 自动代码检查
# 
# 此Hook会在每次git commit前自动执行以下检查：
# 1. 基础规范检查（javax、@Autowired、System.out）
# 2. AI代码验证
# 3. 编译检查
##############################################################################

echo ""
echo "🔍 执行Pre-commit检查..."
echo "================================"

# 检查是否有staged文件
STAGED_FILES=$(git diff --name-only --cached)
if [ -z "$STAGED_FILES" ]; then
    echo "⚠️  没有文件被staged"
    exit 0
fi

ERROR_COUNT=0

##############################################################################
# 第1项：快速规范检查
##############################################################################
echo "1️⃣  快速规范检查..."

# 检查javax包使用（排除javax.sql.DataSource）
for file in $STAGED_FILES; do
    if [[ "$file" == *.java ]]; then
        if grep -q "import javax\." "$file"; then
            if ! grep -q "import javax\.sql\.DataSource" "$file"; then
                echo "  ❌ $file 使用了javax包，应使用jakarta包"
                ((ERROR_COUNT++))
            fi
        fi
    fi
done

# 检查@Autowired使用
for file in $STAGED_FILES; do
    if [[ "$file" == *.java ]]; then
        if grep -q "@Autowired" "$file"; then
            echo "  ❌ $file 使用了@Autowired，应使用@Resource"
            ((ERROR_COUNT++))
        fi
    fi
done

# 检查System.out使用
for file in $STAGED_FILES; do
    if [[ "$file" == *.java ]]; then
        if grep -q "System\.out\.println" "$file"; then
            echo "  ❌ $file 使用了System.out.println，应使用SLF4J"
            ((ERROR_COUNT++))
        fi
    fi
done

if [ $ERROR_COUNT -eq 0 ]; then
    echo "  ✅ 快速规范检查通过"
else
    echo "  ❌ 快速规范检查失败: $ERROR_COUNT 个错误"
fi

##############################################################################
# 第2项：AI代码验证（如果存在ai-code-validation.sh）
##############################################################################
if [ -f "scripts/ai-code-validation.sh" ]; then
    echo ""
    echo "2️⃣  AI代码验证..."
    
    bash scripts/ai-code-validation.sh
    if [ $? -ne 0 ]; then
        echo "  ❌ AI代码验证失败"
        ((ERROR_COUNT++))
    else
        echo "  ✅ AI代码验证通过"
    fi
fi

##############################################################################
# 第3项：Entity审计字段重复检查
##############################################################################
echo ""
echo "3️⃣  Entity审计字段检查..."

for file in $STAGED_FILES; do
    if [[ "$file" == *"Entity.java" ]]; then
        if grep -q "extends BaseEntity" "$file"; then
            # 检查是否重复定义了审计字段
            if grep -q "private LocalDateTime createTime" "$file"; then
                echo "  ❌ $file 重复定义了createTime字段（BaseEntity已包含）"
                ((ERROR_COUNT++))
            fi
            if grep -q "private LocalDateTime updateTime" "$file"; then
                echo "  ❌ $file 重复定义了updateTime字段（BaseEntity已包含）"
                ((ERROR_COUNT++))
            fi
            if grep -q "private Long createUserId" "$file"; then
                echo "  ❌ $file 重复定义了createUserId字段（BaseEntity已包含）"
                ((ERROR_COUNT++))
            fi
            if grep -q "private Integer deletedFlag" "$file"; then
                echo "  ❌ $file 重复定义了deletedFlag字段（BaseEntity已包含）"
                ((ERROR_COUNT++))
            fi
        fi
    fi
done

if [ $ERROR_COUNT -eq 0 ]; then
    echo "  ✅ Entity审计字段检查通过"
fi

##############################################################################
# 总结
##############################################################################
echo ""
echo "================================"
if [ $ERROR_COUNT -eq 0 ]; then
    echo "✅ Pre-commit检查全部通过"
    echo "================================"
    exit 0
else
    echo "❌ Pre-commit检查失败: $ERROR_COUNT 个错误"
    echo "================================"
    echo ""
    echo "💡 修复建议："
    echo "  1. 使用jakarta.*替换javax.*（除javax.sql.DataSource）"
    echo "  2. 使用@Resource替换@Autowired"
    echo "  3. 使用SLF4J替换System.out.println"
    echo "  4. Entity继承BaseEntity后不要重复定义审计字段"
    echo ""
    echo "如需跳过检查（不推荐），使用: git commit --no-verify"
    exit 1
fi
EOF

# 设置hook文件为可执行
chmod +x "$HOOK_FILE"

echo "✅ Pre-commit hook配置成功！"
echo ""
echo "📋 配置信息："
echo "  - Hook文件: $HOOK_FILE"
echo "  - 检查项:"
echo "    1. javax包使用检查"
echo "    2. @Autowired注解检查"
echo "    3. System.out使用检查"
echo "    4. Entity审计字段重复检查"
echo "    5. AI代码验证（如果存在）"
echo ""
echo "💡 使用说明："
echo "  - 每次git commit时会自动执行检查"
echo "  - 检查失败会阻止提交"
echo "  - 紧急情况可使用 git commit --no-verify 跳过"
echo ""
echo "🎉 配置完成！"
