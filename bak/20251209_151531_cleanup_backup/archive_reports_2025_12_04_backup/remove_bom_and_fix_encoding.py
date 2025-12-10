#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
移除所有Java文件的BOM并修复全角符号
"""

import os
from pathlib import Path

def remove_bom_and_fix_file(file_path):
    """移除BOM并修复全角符号"""
    try:
        # 读取文件（检测并移除BOM）
        with open(file_path, 'r', encoding='utf-8-sig') as f:
            content = f.read()

        # 修复全角符号为半角
        replacements = {
            '：': ':',
            '（': '(',
            '）': ')',
            '，': ',',
            '。': '.',
            '？': '?',
            '！': '!',
            '"': '"',
            '"': '"',
            ''': "'",
            ''': "'",
        }

        for old, new in replacements.items():
            content = content.replace(old, new)

        # 写回文件（UTF-8 without BOM）
        with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(content)

        return True
    except Exception as e:
        print(f"Error: {file_path}: {e}")
        return False

def main():
    """主函数"""
    base_dir = Path(r"D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java")

    if not base_dir.exists():
        print(f"Directory not found: {base_dir}")
        return

    fixed_count = 0
    for java_file in base_dir.rglob("*.java"):
        if remove_bom_and_fix_file(java_file):
            fixed_count += 1

    print(f"Successfully processed {fixed_count} Java files")
    print("All files have been cleaned of BOM and fixed for encoding issues")

if __name__ == "__main__":
    main()

