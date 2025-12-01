#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
快速编码修复脚本 - 批量修复Java文件编码问题
"""
import os
import sys

def fix_file_encoding(filepath):
    """修复单个文件的编码"""
    try:
        # 尝试以UTF-8读取
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
        except UnicodeDecodeError:
            # 如果UTF-8失败，尝试GBK
            try:
                with open(filepath, 'r', encoding='gbk') as f:
                    content = f.read()
            except:
                # 最后尝试latin-1
                with open(filepath, 'r', encoding='latin-1', errors='replace') as f:
                    content = f.read()

        # 移除BOM标记
        if content.startswith('\ufeff'):
            content = content[1:]

        # 保存为UTF-8无BOM
        with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
            f.write(content)

        return True
    except Exception as e:
        print(f"修复失败: {filepath} - {e}")
        return False

def main():
    project_root = r"D:\IOE-DREAM"
    java_dir = os.path.join(project_root, "smart-admin-api-java17-springboot3")

    if not os.path.exists(java_dir):
        print(f"目录不存在: {java_dir}")
        return

    print("开始修复Java文件编码...")

    fixed_count = 0
    total_count = 0

    for root, dirs, files in os.walk(java_dir):
        # 跳过target目录
        if 'target' in root or '.git' in root:
            continue

        for file in files:
            if file.endswith('.java'):
                filepath = os.path.join(root, file)
                total_count += 1

                if fix_file_encoding(filepath):
                    fixed_count += 1

                if total_count % 100 == 0:
                    print(f"已处理 {total_count} 个文件，修复 {fixed_count} 个...")

    print(f"\n完成！共处理 {total_count} 个文件，修复 {fixed_count} 个")

if __name__ == '__main__':
    main()

