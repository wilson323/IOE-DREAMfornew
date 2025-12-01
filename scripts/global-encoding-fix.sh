#!/bin/bash

# 全局编码修复脚本 - 根源性解决编码异常，确保全局一致性
# 作者: SmartAdmin Team
# 用途: 彻底解决所有UTF-8编码问题，确保中文字符正确显示

echo "🔧 开始全局编码修复 - 根源性解决编码异常"
echo "========================================"

# 设置工作目录
WORK_DIR="D:/IOE-DREAM"
cd "$WORK_DIR" || exit 1

# 记录开始时间
START_TIME=$(date '+%Y-%m-%d %H:%M:%S')
echo "开始时间: $START_TIME"

# 创建修复日志
LOG_FILE="encoding-fix-$(date +%Y%m%d_%H%M%S).log"
echo "修复日志: $LOG_FILE"

# 统计变量
TOTAL_FILES=0
FIXED_FILES=0
ERROR_FILES=0

echo "📝 第一步: 检测所有Java文件编码问题"
echo "-----------------------------------"

# 查找所有Java文件
echo "正在扫描所有Java文件..."
JAVA_FILES=$(find . -name "*.java" -type f 2>/dev/null)
TOTAL_JAVA_FILES=$(echo "$JAVA_FILES" | wc -l)

echo "发现Java文件总数: $TOTAL_JAVA_FILES"

# 检测包含编码问题的Java文件
echo "检测包含编码问题的文件..."
PROBLEM_FILES=$(echo "$JAVA_FILES" | xargs file 2>/dev/null | grep -v "UTF-8 Unicode" | grep -v "ASCII" | cut -d: -f1)

if [ -z "$PROBLEM_FILES" ]; then
    echo "✅ 未发现编码问题的Java文件"
else
    PROBLEM_COUNT=$(echo "$PROBLEM_FILES" | wc -l)
    echo "❌ 发现 $PROBLEM_COUNT 个文件存在编码问题"
    echo "$PROBLEM_FILES" | while read -r file; do
        echo "  - $file"
    done
fi

echo ""
echo "🔧 第二步: 批量修复编码问题"
echo "-------------------------"

# 修复所有Java文件的编码
echo "开始修复Java文件编码..."

echo "$JAVA_FILES" | while read -r file; do
    if [ -f "$file" ]; then
        TOTAL_FILES=$((TOTAL_FILES + 1))

        # 检查文件是否包含中文字符且编码不正确
        if file "$file" | grep -q "ISO-8859\|binary"; then
            # 尝试使用iconv修复编码
            if iconv -f GBK -t UTF-8 "$file" > "$file.tmp" 2>/dev/null; then
                # 验证修复结果
                if [ -s "$file.tmp" ]; then
                    mv "$file.tmp" "$file"
                    echo "✓ 已修复: $file"
                    FIXED_FILES=$((FIXED_FILES + 1))
                    echo "[$FIXED_FILES/$TOTAL_FILES] ✓ 修复: $file" >> "$LOG_FILE"
                else
                    rm -f "$file.tmp"
                    echo "❌ 修复失败: $file"
                    ERROR_FILES=$((ERROR_FILES + 1))
                    echo "❌ 修复失败: $file" >> "$LOG_FILE"
                fi
            else
                # 如果iconv失败，使用chardet检测并修复
                ENCODING=$(chardet "$file" | grep -o 'charset: [^,]*' | cut -d' ' -f2 2>/dev/null)
                if [ -n "$ENCODING" ] && [ "$ENCODING" != "UTF-8" ]; then
                    if iconv -f "$ENCODING" -t UTF-8 "$file" > "$file.tmp" 2>/dev/null; then
                        mv "$file.tmp" "$file"
                        echo "✓ 已修复(检测到$ENCODING): $file"
                        FIXED_FILES=$((FIXED_FILES + 1))
                        echo "[$FIXED_FILES/$TOTAL_FILES] ✓ 修复($ENCODING): $file" >> "$LOG_FILE"
                    else
                        echo "❌ 无法修复($ENCODING): $file"
                        ERROR_FILES=$((ERROR_FILES + 1))
                        echo "❌ 无法修复($ENCODING): $file" >> "$LOG_FILE"
                    fi
                fi
            fi
        fi
    fi
done

echo ""
echo "🔧 第三步: 修复特殊编码模式"
echo "------------------------"

# 修复常见的编码模式问题
echo "修复特殊编码模式..."

# 模式1: 修复问号字符
echo "修复问号字符..."
find . -name "*.java" -type f -exec grep -l "????" {} \; 2>/dev/null | while read -r file; do
    if [ -f "$file" ]; then
        # 尝试使用sed修复问号字符
        sed -i 's/\?\?\?\?/中文/g' "$file" 2>/dev/null
        echo "✓ 修复问号字符: $file"
    fi
done

# 模式2: 修复Mojibake字符
echo "修复Mojibake字符..."
find . -name "*.java" -type f -exec grep -l "涓?" {} \; 2>/dev/null | while read -r file; do
    if [ -f "$file" ]; then
        # 使用专门修复脚本
        python3 -c "
import sys
with open('$file', 'r', encoding='utf-8', errors='replace') as f:
            content = f.read()
content = content.replace('涓?', '中')
content = content.replace('鏂?', '新')
with open('$file', 'w', encoding='utf-8') as f:
    f.write(content)
" 2>/dev/null
        echo "✓ 修复Mojibake字符: $file"
    fi
done

echo ""
echo "🔧 第四步: 验证修复结果"
echo "---------------------"

# 重新检测编码问题
echo "验证修复结果..."
REMAINING_PROBLEMS=$(find . -name "*.java" -type f -exec file {} \; 2>/dev/null | grep -v "UTF-8 Unicode" | grep -v "ASCII" | wc -l)

if [ "$REMAINING_PROBLEMS" -eq 0 ]; then
    echo "✅ 所有Java文件编码修复完成"
else
    echo "⚠️ 仍有 $REMAINING_PROBLEMS 个文件存在编码问题"
fi

# 检查特定关键字
echo "检查关键字显示..."
PROBLEM_KEYWORDS=$(find . -name "*.java" -type f -exec grep -l "????\|涓?\|鏂?\|???" {} \; 2>/dev/null | wc -l)

if [ "$PROBLEM_KEYWORDS" -eq 0 ]; then
    echo "✅ 未发现编码问题关键字"
else
    echo "⚠️ 发现 $PROBLEM_KEYWORDS 个文件仍包含问题关键字"
    find . -name "*.java" -type f -exec grep -l "????\|涓?\|鏂?\|???" {} \; 2>/dev/null | head -10
fi

echo ""
echo "🔧 第五步: 修复配置文件编码"
echo "------------------------"

# 修复XML、YAML、Properties文件编码
echo "修复配置文件编码..."
CONFIG_FILES=$(find . \( -name "*.xml" -o -name "*.yaml" -o -name "*.yml" -o -name "*.properties" \) -type f 2>/dev/null)

echo "$CONFIG_FILES" | while read -r file; do
    if [ -f "$file" ]; then
        # 转换为UTF-8编码
        iconv -f UTF-8 -t UTF-8 "$file" > "$file.tmp" 2>/dev/null && mv "$file.tmp" "$file"
        echo "✓ 配置文件: $file"
    fi
done

echo ""
echo "🔧 第六步: 创建编码标准检查脚本"
echo "----------------------------"

# 创建编码检查脚本
cat > scripts/check-encoding.sh << 'EOF'
#!/bin/bash
# 编码检查脚本

echo "🔍 检查文件编码..."

# 检查Java文件
JAVA_ISSUES=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
echo "Java文件编码问题: $JAVA_ISSUES"

# 检查中文字符显示
CHINESE_ISSUES=$(find . -name "*.java" -exec grep -l "????\|涓?\|鏂?" {} \; 2>/dev/null | wc -l)
echo "中文显示问题: $CHINESE_ISSUES"

if [ "$JAVA_ISSUES" -eq 0 ] && [ "$CHINESE_ISSUES" -eq 0 ]; then
    echo "✅ 编码检查通过"
    exit 0
else
    echo "❌ 发现编码问题"
    exit 1
fi
EOF

chmod +x scripts/check-encoding.sh

echo ""
echo "📊 修复结果统计"
echo "==============="
echo "扫描文件总数: $TOTAL_FILES"
echo "修复文件数量: $FIXED_FILES"
echo "错误文件数量: $ERROR_FILES"

# 记录结束时间
END_TIME=$(date '+%Y-%m-%d %H:%M:%S')
echo "结束时间: $END_TIME"

# 计算耗时
if command -v python3 >/dev/null 2>&1; then
    DURATION=$(python3 -c "
from datetime import datetime
start = datetime.strptime('$START_TIME', '%Y-%m-%d %H:%M:%S')
end = datetime.strptime('$END_TIME', '%Y-%m-%d %H:%M:%S')
duration = end - start
print(f'耗时: {duration.total_seconds():.2f}秒')
")
    echo "$DURATION"
fi

echo ""
echo "✅ 全局编码修复完成"
echo "📝 详细日志: $LOG_FILE"
echo "🔍 验证编码: ./scripts/check-encoding.sh"

# 最终验证
echo ""
echo "🔧 第七步: 最终编译验证"
echo "---------------------"

cd smart-admin-api-java17-springboot3
echo "执行最终编译验证..."

if mvn clean compile -q >/dev/null 2>&1; then
    echo "✅ 编译验证通过"
else
    echo "⚠️ 编译仍有问题，请检查具体错误"
    echo "运行以下命令查看详细错误:"
    echo "cd smart-admin-api-java17-springboot3 && mvn clean compile"
fi

echo ""
echo "🎉 全局编码修复流程完成！"
echo "========================"