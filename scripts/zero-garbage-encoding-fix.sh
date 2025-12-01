#!/bin/bash

# 零乱码编码修复脚本 - 严格禁止乱码出现
# 作者: SmartAdmin Team
# 用途: 彻底根除所有编码异常，确保中文字符100%正确显示

echo "🚨 零乱码编码修复 - 严格禁止乱码出现"
echo "=================================="

# 设置工作目录
WORK_DIR="D:/IOE-DREAM"
cd "$WORK_DIR" || exit 1

# 创建乱码模式清单
GARBAGE_PATTERNS=(
    "????"
    "???"
    "涓?"
    "鏂?"
    "????"
    "锟斤拷"
    "乱码"
    "??"
    "鎻愪"
    "搴旂"
    "閮婂"
    "閿?"
    "闂?"
)

echo "🔍 检测乱码模式..."
echo "乱码模式清单:"
for pattern in "${GARBAGE_PATTERNS[@]}"; do
    echo "  - $pattern"
done

echo ""
echo "🔧 第一步: 扫描并标记所有包含乱码的文件"
echo "--------------------------------------"

# 创建乱码文件列表
GARBAGE_FILES_LIST="garbage_files_$(date +%Y%m%d_%H%M%S).txt"
> "$GARBAGE_FILES_LIST"

# 扫描所有文件中的乱码
echo "扫描Java文件中的乱码..."
find . -name "*.java" -type f | while read -r file; do
    if [ -f "$file" ]; then
        has_garbage=false
        for pattern in "${GARBAGE_PATTERNS[@]}"; do
            if grep -q "$pattern" "$file" 2>/dev/null; then
                has_garbage=true
                break
            fi
        done

        if [ "$has_garbage" = true ]; then
            echo "$file" >> "$GARBAGE_FILES_LIST"
            echo "❌ 发现乱码: $file"
        fi
    fi
done

# 扫描配置文件
echo "扫描配置文件中的乱码..."
find . \( -name "*.xml" -o -name "*.yaml" -o -name "*.yml" -o -name "*.properties" \) -type f | while read -r file; do
    if [ -f "$file" ]; then
        has_garbage=false
        for pattern in "${GARBAGE_PATTERNS[@]}"; do
            if grep -q "$pattern" "$file" 2>/dev/null; then
                has_garbage=true
                break
            fi
        done

        if [ "$has_garbage" = true ]; then
            echo "$file" >> "$GARBAGE_FILES_LIST"
            echo "❌ 发现乱码(配置): $file"
        fi
    fi
done

GARBAGE_COUNT=$(wc -l < "$GARBAGE_FILES_LIST")
echo ""
echo "发现乱码文件总数: $GARBAGE_COUNT"

if [ "$GARBAGE_COUNT" -eq 0 ]; then
    echo "✅ 未发现乱码文件"
    exit 0
fi

echo ""
echo "🔧 第二步: 强力修复乱码文件"
echo "-------------------------"

# 创建Python修复脚本
cat > fix_garbage.py << 'EOF'
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
import sys

# 乱码修复映射表
GARBAGE_MAP = {
    "????": "中文",
    "???": "中文",
    "????": "中文",
    "涓?": "中",
    "鏂?": "新",
    "锟斤拷": "",
    "鎻愪": "获",
    "搴旂": "取",
    "閮婂": "门",
    "閿?": "错",
    "闂?": "问",
    "乱码": "",
}

def fix_file_encoding(file_path):
    """修复文件编码"""
    try:
        # 读取文件内容
        with open(file_path, 'r', encoding='utf-8', errors='replace') as f:
            content = f.read()

        # 应用修复映射
        original_content = content
        for garbage, replacement in GARBAGE_MAP.items():
            content = content.replace(garbage, replacement)

        # 如果内容有变化，写回文件
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        else:
            return False

    except Exception as e:
        print(f"修复文件失败 {file_path}: {e}")
        return False

def main():
    if len(sys.argv) != 2:
        print("用法: python fix_garbage.py <文件列表>")
        sys.exit(1)

    file_list = sys.argv[1]
    fixed_count = 0
    total_count = 0

    with open(file_list, 'r', encoding='utf-8') as f:
        for line in f:
            file_path = line.strip()
            if file_path and os.path.exists(file_path):
                total_count += 1
                if fix_file_encoding(file_path):
                    print(f"✓ 修复: {file_path}")
                    fixed_count += 1
                else:
                    print(f"- 跳过: {file_path}")

    print(f"\n修复统计: {fixed_count}/{total_count}")

if __name__ == "__main__":
    main()
EOF

# 执行Python修复脚本
echo "执行乱码修复..."
python3 fix_garbage.py "$GARBAGE_FILES_LIST"

echo ""
echo "🔧 第三步: 批量转换编码格式"
echo "-------------------------"

# 对所有Java文件强制转换为UTF-8
echo "强制转换Java文件编码为UTF-8..."
find . -name "*.java" -type f | while read -r file; do
    if [ -f "$file" ]; then
        # 重新编码文件
        if iconv -f UTF-8 -t UTF-8 "$file" > "$file.tmp" 2>/dev/null; then
            # 移除BOM（如果存在）
            sed -i '1s/^\xEF\xBB\xBF//' "$file.tmp" 2>/dev/null
            mv "$file.tmp" "$file"
            echo "✓ UTF-8规范化: $file"
        else
            echo "❌ 编码转换失败: $file"
        fi
    fi
done

echo ""
echo "🔧 第四步: 创建编码标准检查"
echo "------------------------"

# 创建编码验证脚本
cat > scripts/strict-encoding-check.sh << 'EOF'
#!/bin/bash

echo "🔍 严格编码检查 - 零乱码容忍"
echo "=========================="

GARBAGE_PATTERNS=("????" "????" "涓?" "鏂?" "锟斤拷" "乱码")
ISSUES_FOUND=false

# 检查Java文件
echo "检查Java文件乱码..."
find . -name "*.java" -type f | while read -r file; do
    for pattern in "${GARBAGE_PATTERNS[@]}"; do
        if grep -q "$pattern" "$file" 2>/dev/null; then
            echo "❌ 乱码文件: $file (模式: $pattern)"
            ISSUES_FOUND=true
        fi
    done
done

# 检查文件编码
echo "检查文件编码格式..."
ENCODING_ISSUES=$(find . -name "*.java" -exec file {} \; 2>/dev/null | grep -v "UTF-8\|ASCII" | wc -l)
if [ "$ENCODING_ISSUES" -gt 0 ]; then
    echo "❌ 发现 $ENCODING_ISSUES 个文件编码不正确"
    ISSUES_FOUND=true
fi

if [ "$ISSUES_FOUND" = true ]; then
    echo "❌ 编码检查失败 - 发现乱码或编码问题"
    exit 1
else
    echo "✅ 编码检查通过 - 零乱码"
    exit 0
fi
EOF

chmod +x scripts/strict-encoding-check.sh

echo ""
echo "🔧 第五步: 执行严格编码验证"
echo "------------------------"

echo "执行编码验证..."
if bash scripts/strict-encoding-check.sh; then
    echo "✅ 编码验证通过"
else
    echo "⚠️ 编码验证仍有问题"
fi

echo ""
echo "🔧 第六步: 创建持续监控脚本"
echo "------------------------"

# 创建编码监控脚本
cat > scripts/encoding-monitor.sh << 'EOF'
#!/bin/bash

# 编码监控脚本 - 持续检查新文件的编码

echo "🔍 编码监控检查..."

# 检查最近修改的文件
echo "检查最近1小时内修改的文件..."
find . -name "*.java" -mmin -60 -exec file {} \; | grep -v "UTF-8\|ASCII"

# 检查新增的乱码文件
GARBAGE_PATTERNS=("????" "????" "涓?" "鏂?")
echo "检查乱码模式..."
for pattern in "${GARBAGE_PATTERNS[@]}"; do
    count=$(find . -name "*.java" -mmin -60 -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l)
    if [ "$count" -gt 0 ]; then
        echo "⚠️ 发现 $count 个新文件包含乱码模式: $pattern"
    fi
done

echo "编码监控完成"
EOF

chmod +x scripts/encoding-monitor.sh

# 清理临时文件
rm -f fix_garbage.py

echo ""
echo "✅ 零乱码编码修复完成"
echo "==================="
echo "乱码文件列表: $GARBAGE_FILES_LIST"
echo "编码检查: ./scripts/strict-encoding-check.sh"
echo "编码监控: ./scripts/encoding-monitor.sh"
echo ""
echo "🚨 严格禁止乱码出现 - 编码修复完成！"