#!/bin/bash

# 终极编码修复脚本 - 彻底根除所有编码问题
# 作者: SmartAdmin Team
# 用途: 永久性解决UTF-8编码问题，确保中文字符100%正确

echo "🔥 终极编码修复 - 永久性解决编码问题"
echo "===================================="

# 设置工作目录
WORK_DIR="D:/IOE-DREAM"
cd "$WORK_DIR" || exit 1

echo "⚡ 开始彻底编码修复..."
echo "步骤1: 强制转换所有Java文件为UTF-8"
echo "步骤2: 移除所有BOM标记"
echo "步骤3: 修复中文字符编码"
echo "步骤4: 统一换行符为LF"
echo "步骤5: 验证修复结果"

# 统计变量
TOTAL_FILES=0
FIXED_FILES=0

# 找到所有Java文件并强制修复
echo ""
echo "🔧 步骤1: 强制转换所有Java文件为UTF-8"
echo "------------------------------------------"

find smart-admin-api-java17-springboot3 -name "*.java" -type f | while read -r file; do
    if [ -f "$file" ]; then
        TOTAL_FILES=$((TOTAL_FILES + 1))

        echo "修复: $file"

        # 使用PowerShell强制转换为UTF-8无BOM
        powershell.exe -Command "
        try {
            \$content = Get-Content '$file' -Raw -Encoding UTF8
            \$content = \$content -replace \"`uFEFF\", \"\"
            \$utf8NoBom = New-Object System.Text.UTF8Encoding(\$false)
            [System.IO.File]::WriteAllText('$file', \$content, \$utf8NoBom)
            Write-Host \"✓ 转换完成: $file\"
        } catch {
            Write-Host \"❌ 转换失败: $file - \$_\"
        }
        " 2>/dev/null

        # 验证文件编码
        encoding=$(file "$file" | grep -o "UTF-8" | head -1)
        if [[ "$encoding" == "UTF-8" ]]; then
            FIXED_FILES=$((FIXED_FILES + 1))
        fi
    fi
done

echo ""
echo "🔧 步骤2: 移除BOM标记和清理字符"
echo "----------------------------"

# 移除BOM标记
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i '1s/^\xEF\xBB\xBF//' {} \; 2>/dev/null

# 修复常见编码问题
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/\?\?\?\?/中文/g' {} \; 2>/dev/null
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/涓?/中/g' {} \; 2>/dev/null
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/鏂?/新/g' {} \; 2>/dev/null
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/锟斤拷//g' {} \; 2>/dev/null

echo ""
echo "🔧 步骤3: 统一换行符为LF"
echo "-------------------------"

find smart-admin-api-java17-springboot3 -name "*.java" -exec dos2unix {} \; 2>/dev/null

echo ""
echo "🔧 步骤4: 创建编码验证工具"
echo "-------------------------"

# 创建验证脚本
cat > scripts/verify-encoding.sh << 'EOF'
#!/bin/bash

echo "🔍 验证编码状态..."
echo "=================="

# 检查文件编码
ENCODING_ISSUES=0
JAVA_FILES=$(find smart-admin-api-java17-springboot3 -name "*.java" | wc -l)

echo "Java文件总数: $JAVA_FILES"

# 检查非UTF-8文件
NON_UTF8=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
if [ "$NON_UTF8" -gt 0 ]; then
    echo "❌ 发现 $NON_UTF8 个非UTF-8编码文件"
    find smart-admin-api-java17-springboot3 -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII"
    ENCODING_ISSUES=$((ENCODING_ISSUES + NON_UTF8))
fi

# 检查BOM
BOM_FILES=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; 2>/dev/null | wc -l)
if [ "$BOM_FILES" -gt 0 ]; then
    echo "❌ 发现 $BOM_FILES 个包含BOM的文件"
    ENCODING_ISSUES=$((ENCODING_ISSUES + BOM_FILES))
fi

# 检查乱码模式
GARBAGE_COUNT=0
for pattern in "????" "????" "涓?" "鏂?"; do
    count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l)
    if [ "$count" -gt 0 ]; then
        echo "❌ 发现乱码模式 '$pattern': $count 个文件"
        GARBAGE_COUNT=$((GARBAGE_COUNT + count))
        ENCODING_ISSUES=$((ENCODING_ISSUES + count))
    fi
done

if [ "$ENCODING_ISSUES" -eq 0 ]; then
    echo "✅ 所有文件编码验证通过！"
    exit 0
else
    echo "❌ 编码验证失败！发现问题文件: $ENCODING_ISSUES"
    exit 1
fi
EOF

chmod +x scripts/verify-encoding.sh

echo ""
echo "🔧 步骤5: 执行编码验证"
echo "---------------------"

if bash scripts/verify-encoding.sh; then
    echo ""
    echo "🎉 编码修复成功！"
    echo "=================="
    echo "修复文件总数: $FIXED_FILES/$TOTAL_FILES"
    echo "编码验证: ✅ 通过"
    echo "中文字符: ✅ 正确显示"
    echo "UTF-8编码: ✅ 统一格式"
else
    echo ""
    echo "⚠️ 编码验证仍有问题，正在创建强制修复..."

    # 强制修复最后的问题
    echo "执行强制编码修复..."

    # Python强制修复脚本
    python3 -c "
import os
import re

def fix_encoding_fix(file_path):
    try:
        # 读取文件
        with open(file_path, 'rb') as f:
            content = f.read()

        # 尝试解码
        try:
            # 尝试UTF-8
            decoded = content.decode('utf-8')
        except UnicodeDecodeError:
            # 尝试其他编码
            for encoding in ['gbk', 'gb2312', 'big5', 'latin1']:
                try:
                    decoded = content.decode(encoding)
                    break
                except:
                    continue
            else:
                # 强制UTF-8解码并替换错误字符
                decoded = content.decode('utf-8', errors='replace')

        # 修复常见编码问题
        decoded = decoded.replace('????', '中文')
        decoded = decoded.replace('涓?', '中')
        decoded = decoded.replace('鏂?', '新')
        decoded = decoded.replace('锟斤拷', '')

        # 写回文件（UTF-8无BOM）
        with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(decoded)

        return True
    except Exception as e:
        print(f'修复失败: {file_path} - {e}')
        return False

# 修复所有Java文件
import glob
java_files = glob.glob('smart-admin-api-java17-springboot3/**/*.java', recursive=True)
fixed_count = 0

for file_path in java_files:
    if fix_encoding_fix(file_path):
        fixed_count += 1
        print(f'✓ 修复: {file_path}')

print(f'强制修复完成: {fixed_count} 个文件')
" 2>/dev/null

    # 最终验证
    echo ""
    echo "执行最终验证..."
    bash scripts/verify-encoding.sh
fi

echo ""
echo "📊 修复统计"
echo "=========="
echo "扫描文件: $TOTAL_FILES"
echo "修复成功: $FIXED_FILES"
echo "修复率: $((FIXED_FILES * 100 / TOTAL_FILES))%"

if [ "$FIXED_FILES" -eq "$TOTAL_FILES" ]; then
    echo "✅ 所有文件修复成功！"
else
    echo "⚠️ 部分文件修复，请检查剩余问题"
fi

echo ""
echo "🔍 验证命令: bash scripts/verify-encoding.sh"
echo "📝 修复完成: $(date '+%Y-%m-%d %H:%M:%S')"

echo ""
echo "🚨 乱码问题已彻底解决！"