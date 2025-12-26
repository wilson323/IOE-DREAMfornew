#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Python脚本：批量移除Java文件的BOM字符
作者：IOE-DREAM架构团队
日期：2025-12-26
用途：移除Java文件开头的UTF-8 BOM字符（EF BB BF）
"""

import os
import sys
from pathlib import Path

# UTF-8 BOM 字节序列
UTF8_BOM = b'\xef\xbb\xbf'

def remove_bom_from_file(file_path):
    """移除单个文件的BOM字符"""
    try:
        # 读取文件二进制内容
        with open(file_path, 'rb') as f:
            content = f.read()

        # 检查是否有BOM
        if content.startswith(UTF8_BOM):
            # 移除BOM
            content_without_bom = content[len(UTF8_BOM):]

            # 写回文件（保持原有换行符）
            with open(file_path, 'wb') as f:
                f.write(content_without_bom)

            return True
        else:
            return False

    except Exception as e:
        print(f"❌ 处理文件失败: {file_path}, 错误: {e}")
        return False

def scan_directory(directory, recursive=True):
    """扫描目录查找所有Java文件"""
    if recursive:
        return list(Path(directory).rglob('*.java'))
    else:
        return list(Path(directory).glob('*.java'))

def main():
    """主函数"""
    print("=" * 50)
    print("  移除Java文件BOM字符工具（Python版）")
    print("=" * 50)
    print()

    # 定义项目根目录
    project_root = Path("D:/IOE-DREAM/microservices")

    if not project_root.exists():
        print(f"❌ 错误: 目录不存在 - {project_root}")
        sys.exit(1)

    print(f"✅ 找到目录: {project_root}")
    print()

    # 扫描所有Java文件
    print("🔍 扫描Java文件...")
    java_files = scan_directory(project_root)
    print(f"📊 找到 {len(java_files)} 个Java文件")
    print()

    # 处理文件
    bom_count = 0
    fixed_count = 0

    for java_file in java_files:
        # 检查BOM
        try:
            with open(java_file, 'rb') as f:
                header = f.read(3)

            if header == UTF8_BOM:
                bom_count += 1
                print(f"🔍 发现BOM: {java_file.relative_to(project_root)}")

                # 移除BOM
                if remove_bom_from_file(java_file):
                    fixed_count += 1
                    print(f"✅ 移除BOM: {java_file.relative_to(project_root)}")

        except Exception as e:
            print(f"⚠️  检查文件失败: {java_file}, 错误: {e}")

    print()
    print("=" * 50)
    print("  修复完成统计")
    print("=" * 50)
    print(f"📊 发现BOM文件: {bom_count}")
    print(f"✅ 已修复文件: {fixed_count}")
    print()

    if fixed_count > 0:
        print("✅ BOM字符移除完成!")
    else:
        print("ℹ️  没有发现BOM字符")

    print()

if __name__ == "__main__":
    main()
