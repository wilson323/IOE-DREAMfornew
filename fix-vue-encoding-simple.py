#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复Vue文件中的UTF-8编码问题 - 简化版本
"""

import os
import re
from pathlib import Path

def fix_vue_encoding(file_path):
    """修复单个Vue文件的编码问题"""
    try:
        # 读取文件内容
        with open(file_path, 'r', encoding='utf-8', errors='replace') as f:
            content = f.read()

        original_content = content

        # 修复常见的编码问题
        # 替换无效的UTF-8字符（替换字符）
        content = content.replace('', '\n')  # 替换无效的UTF-8换行符

        # 修复未闭合的字符串字面量 - 特别处理title字段
        content = re.sub(r'title:\s*[\'"]([^\'"]*)$',
                           lambda m: f"title: '{m.group(1)}'",
                           content, flags=re.MULTILINE)

        content = re.sub(r'title:\s*[\'"]([^\'"]*)[\\\'"]\s*$',
                           lambda m: f"title: '{m.group(1)}'",
                           content, flags=re.MULTILINE)

        # 修复包含无效字符的行
        lines = content.split('\n')
        fixed_lines = []
        for line in lines:
            # 移除或替换无效字符
            if '' in line:
                # 尝试修复包含替换字符的行
                line = line.replace('', '').strip()

            # 移除行首和行尾的空白字符，但保留有用的内容
            line = line.strip()

            # 如果行不是空的，添加到修复后的行列表
            if line:
                fixed_lines.append(line)

        # 如果有修复，重新组合内容
        if fixed_lines != lines:
            content = '\n'.join(fixed_lines)

        # 只有内容发生变化时才写入文件
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"[修复] {file_path}")
            return True
        else:
            print(f"[跳过] {file_path} (无需修复)")
            return True

    except Exception as e:
        print(f"[错误] {file_path} - {str(e)}")
        return False

def main():
    """主函数"""
    print("开始修复Vue文件编码问题...")

    # 查找所有Vue文件
    vue_files = []
    for pattern in ["**/*.vue"]:
        vue_files.extend(Path(".").rglob(pattern))
        # 排除node_modules和target目录
        vue_files = [f for f in vue_files if 'node_modules' not in str(f) and 'target' not in str(f)]

    print(f"找到 {len(vue_files)} 个Vue文件")

    fixed_count = 0
    for file_path in vue_files:
        if fix_vue_encoding(file_path):
            fixed_count += 1

    print(f"\n修复完成!")
    print(f"总文件数: {len(vue_files)}")
    print(f"修复文件数: {fixed_count}")

if __name__ == "__main__":
    main()