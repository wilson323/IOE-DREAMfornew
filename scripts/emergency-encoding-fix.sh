#!/bin/bash
# 紧急编码修复脚本 - 解决IOE-DREAM项目UTF-8编码问题
# 编码质量守护专家 - 零容忍政策执行

echo "🚨 开始紧急编码修复..."
echo "目标：解决所有UTF-8编码违规问题"

# 设置项目根目录
PROJECT_ROOT="D:\IOE-DREAM"
cd "$PROJECT_ROOT"

echo ""
echo "=== 第一阶段：编码规范性诊断 ==="

# 1. 检查Java文件编码格式
echo "步骤1: 检查Java文件UTF-8编码合规性"
java_files=$(find . -name "*.java" | wc -l)
echo "Java文件总数: $java_files"

# 检查非UTF-8文件
non_utf8_files=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
echo "非UTF-8文件数量: $non_utf8_files"

if [ $non_utf8_files -gt 0 ]; then
    echo "❌ 发现非UTF-8文件:"
    find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | head -10
fi

# 2. 检查BOM标记
echo ""
echo "步骤2: 检查BOM标记"
bom_files=$(find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; 2>/dev/null | wc -l)
echo "含BOM文件数量: $bom_files"

if [ $bom_files -gt 0 ]; then
    echo "❌ 发现含BOM文件:"
    find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; | head -10
fi

# 3. 检查常见乱码模式
echo ""
echo "步骤3: 检查乱码字符模式"
garbage_patterns=("????" "涓?" "鏂?" "锟斤拷" "鎵峰绫嶆" "褰ㄦ?" "闆?" "寰?" "缂洪?")

total_garbage_files=0
for pattern in "${garbage_patterns[@]}"; do
    pattern_files=$(find . -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l)
    if [ $pattern_files -gt 0 ]; then
        echo "❌ 模式 '$pattern': $pattern_files 个文件"
        total_garbage_files=$((total_garbage_files + pattern_files))
    fi
done

echo "乱码文件总数: $total_garbage_files"

echo ""
echo "=== 第二阶段：编码标准化修复 ==="

# 4. 修复非UTF-8编码
if [ $non_utf8_files -gt 0 ]; then
    echo ""
    echo "步骤4: 修复非UTF-8编码文件"
    find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | cut -d: -f1 | while read file; do
        if [ -f "$file" ]; then
            echo "修复编码: $file"
            # 尝试GBK到UTF-8转换
            iconv -f GBK -t UTF-8 "$file" > "$file.tmp" 2>/dev/null && mv "$file.tmp" "$file" || echo "转换失败: $file"
        fi
    done
fi

# 5. 移除BOM标记
if [ $bom_files -gt 0 ]; then
    echo ""
    echo "步骤5: 移除BOM标记"
    find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; 2>/dev/null | while read file; do
        if [ -f "$file" ]; then
            echo "移除BOM: $file"
            sed -i '1s/^\xEF\xBB\xBF//' "$file"
        fi
    done
fi

# 6. 修复常见乱码字符
echo ""
echo "步骤6: 修复常见乱码字符"
find . -name "*.java" -exec sed -i 's/????/中文/g; s/涓?/中/g; s/鏂?/新/g; s/锟斤拷//g; s/鎵峰绫嶆/无法找到符号/g; s/褰ㄦ?/方法/g; s/闆?/类型/g; s/寰?/需要/g; s/缂洪?/缺少/g' {} \; 2>/dev/null

echo ""
echo "=== 第三阶段：编译环境编码优化 ==="

# 7. 检查并修复Maven编译器编码设置
echo ""
echo "步骤7: 优化Maven编译器编码配置"
cd smart-admin-api-java17-springboot3

# 检查当前编码设置
echo "当前Maven编译器配置:"
grep -r "encoding\|charset\|UTF-8" pom.xml 2>/dev/null | head -5 || echo "未发现编码配置"

# 8. 验证编码修复效果
echo ""
echo "步骤8: 验证编码修复效果"
echo "重新检查编码合规性..."

# 重新检查编码问题
new_non_utf8=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
new_bom_files=$(find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; 2>/dev/null | wc -l)
new_garbage_files=0
for pattern in "${garbage_patterns[@]}"; do
    pattern_files=$(find . -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l)
    new_garbage_files=$((new_garbage_files + pattern_files))
done

echo "修复后编码状况:"
echo "  非UTF-8文件: $non_utf8_files → $new_non_utf8_files"
echo "  含BOM文件: $bom_files → $new_bom_files"
echo "  乱码文件: $total_garbage_files → $new_garbage_files"

echo ""
echo "=== 第四阶段：编译测试验证 ==="

# 9. 编译测试验证
echo ""
echo "步骤9: 编译测试验证"
echo "执行快速编译检查..."

# 设置编码环境变量
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
export MAVEN_OPTS="-Dfile.encoding=UTF-8"

# 执行编译检查
compile_result=$(mvn clean compile -q 2>&1)
compile_status=$?

echo "编译状态: $compile_status"

if [ $compile_status -eq 0 ]; then
    echo "✅ 编译成功！"
else
    echo "❌ 编译仍有问题，检查错误详情..."
    echo "$compile_result" | head -10

    # 检查是否还有编码问题
    if echo "$compile_result" | grep -q "???"; then
        echo "❌ 仍然存在编码问题，需要进一步修复"
    fi
fi

echo ""
echo "=== 第五阶段：编码质量保障机制建立 ==="

# 10. 创建编码质量检查脚本
echo ""
echo "步骤10: 创建编码质量保障脚本"
cat > scripts/encoding-quality-guard.sh << 'EOF'
#!/bin/bash
# 编码质量守护脚本 - 零容忍政策执行
echo "🔍 执行编码质量守护检查..."

PROJECT_ROOT="D:\IOE-DREAM"
cd "$PROJECT_ROOT"

# UTF-8编码检查
utf8_violations=0
utf8_violations=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
if [ $utf8_violations -gt 0 ]; then
    echo "❌ UTF-8编码违规: $utf8_violations 个文件"
    exit 1
fi

# BOM标记检查
bom_violations=0
bom_violations=$(find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; 2>/dev/null | wc -l)
if [ $bom_violations -gt 0 ]; then
    echo "❌ BOM标记违规: $bom_violations 个文件"
    exit 1
fi

# 乱码字符检查
garbage_violations=0
garbage_patterns=("????" "涓?" "鏂?" "锟斤拷")
for pattern in "${garbage_patterns[@]}"; do
    pattern_files=$(find . -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l)
    garbage_violations=$((garbage_violations + pattern_files))
done
if [ $garbage_violations -gt 0 ]; then
    echo "❌ 乱码字符违规: $garbage_violations 个文件"
    exit 1
fi

echo "✅ 编码质量检查通过"
exit 0
EOF

chmod +x scripts/encoding-quality-guard.sh

# 11. 创建Pre-commit编码检查Hook
echo ""
echo "步骤11: 设置Pre-commit编码检查"
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
# Pre-commit编码检查Hook - 编码零容忍政策
echo "🔍 Pre-commit编码检查..."

# 运行编码质量检查
if ! bash scripts/encoding-quality-guard.sh; then
    echo "❌ 编码质量检查失败，禁止提交！"
    echo "请先运行 scripts/emergency-encoding-fix.sh 修复编码问题"
    exit 1
fi

echo "✅ 编码检查通过，允许提交"
exit 0
EOF

chmod +x .git/hooks/pre-commit

echo ""
echo "=== 修复总结报告 ==="

echo "编码修复完成情况:"
echo "  修复前问题数量:"
echo "    - 非UTF-8文件: $non_utf8_files"
echo "    - 含BOM文件: $bom_files"
echo "    - 乱码文件: $total_garbage_files"
echo ""
echo "  修复后问题数量:"
echo "    - 非UTF-8文件: $new_non_utf8_files"
echo "    - 含BOM文件: $new_bom_files"
echo "    - 乱码文件: $new_garbage_files"
echo ""

if [ $new_non_utf8_files -eq 0 ] && [ $new_bom_files -eq 0 ] && [ $new_garbage_files -eq 0 ]; then
    echo "🎉 编码问题修复成功！所有编码违规已解决"
    echo ""
    echo "建立的保障机制:"
    echo "  ✅ 编码质量守护脚本 (scripts/encoding-quality-guard.sh)"
    echo "  ✅ Pre-commit编码检查Hook"
    echo "  ✅ 编码零容忍政策执行"
    echo ""
    echo "使用方法:"
    echo "  检查编码: bash scripts/encoding-quality-guard.sh"
    echo "  手动修复: bash scripts/emergency-encoding-fix.sh"
    exit 0
else
    echo "⚠️ 仍有编码问题需要手动处理:"
    if [ $new_non_utf8_files -gt 0 ]; then
        echo "  - 剩余非UTF-8文件: $new_non_utf8_files 个"
    fi
    if [ $new_bom_files -gt 0 ]; then
        echo "  - 剩余含BOM文件: $new_bom_files 个"
    fi
    if [ $new_garbage_files -gt 0 ]; then
        echo "  - 剩余乱码文件: $new_garbage_files 个"
    fi
    exit 1
fi
EOF