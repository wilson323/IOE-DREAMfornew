#!/usr/bin/env python3
"""
IOE-DREAM Entity文件大小检查脚本
用于检测超过指定行数的Entity文件
版本: 1.0.0
日期: 2025-12-04
"""

import os
import glob
import sys
from pathlib import Path

def check_entity_size(max_lines=400, base_path="microservices"):
    """
    检查Entity文件大小

    Args:
        max_lines: 最大允许行数
        base_path: 基础路径

    Returns:
        tuple: (违规文件列表, 总文件数)
    """
    print(f"正在扫描 {base_path} 目录下的Entity文件...")
    print(f"最大允许行数: {max_lines}")
    print("")

    # 查找所有Entity.java文件
    pattern = f"{base_path}/**/src/main/java/**/*Entity.java"
    entity_files = glob.glob(pattern, recursive=True)

    large_files = []

    for file_path in entity_files:
        if not os.path.isfile(file_path):
            continue

        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                lines = len(f.readlines())

            if lines > max_lines:
                relative_path = file_path.replace(os.getcwd() + os.sep, '')
                large_files.append((relative_path, lines))
        except Exception as e:
            print(f"警告: 无法读取文件 {file_path}: {e}")

    # 按行数排序
    large_files.sort(key=lambda x: x[1], reverse=True)

    return large_files, len(entity_files)

def main():
    """主函数"""
    max_lines = 400

    # 从命令行参数获取最大行数
    if len(sys.argv) > 1 and sys.argv[1].startswith('--max-lines='):
        max_lines = int(sys.argv[1].split('=')[1])

    large_files, total_files = check_entity_size(max_lines)

    print(f"扫描完成：共检查 {total_files} 个Entity文件")
    print("")

    if large_files:
        print(f"❌ 发现 {len(large_files)} 个超过{max_lines}行的Entity文件：")
        print("")

        for i, (file_path, lines) in enumerate(large_files, 1):
            severity = "🔴" if lines > 700 else "🟡" if lines > 500 else "⚠️"
            print(f"{severity} {i}. {file_path}")
            print(f"   行数: {lines} (超出 {lines - max_lines} 行)")
            print("")

        print(f"建议：将这些Entity拆分为更小的类，理想情况下每个Entity≤200行")
        sys.exit(1)
    else:
        print(f"✅ 所有Entity文件都≤{max_lines}行，符合规范！")
        sys.exit(0)

if __name__ == "__main__":
    main()

