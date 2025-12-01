#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复脚本生成的无效getter/setter方法
"""

import os
import re

def fix_invalid_methods(file_path):
    """修复单个文件中的无效方法"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        original_content = content

        # 移除无效的getter/setter方法
        # 匹配类似于 public static getType() { return Type; } 的无效方法
        invalid_method_pattern = r'\s*public\s+(?:static\s+)?(?:final\s+)?(get|set)(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|void)\s*\([^)]*\)\s*\{\s*[^}]*\s*(?:return\s+(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character);)?\s*[^}]*\s*\}\s*'

        # 找到所有无效方法并移除它们
        invalid_methods = re.findall(invalid_method_pattern, content, re.MULTILINE | re.DOTALL)

        if invalid_methods:
            print(f"  在 {file_path} 中发现 {len(invalid_methods)} 个无效方法，正在移除...")
            content = re.sub(invalid_method_pattern, '', content, flags=re.MULTILINE | re.DOTALL)

            # 清理多余的空行
            content = re.sub(r'\n\s*\n\s*\n', '\n\n', content)

            if content != original_content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                return True

        return False

    except Exception as e:
        print(f"  处理文件 {file_path} 时出错: {e}")
        return False

def main():
    """主函数"""
    result_dir = "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/result"
    vo_dir = "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo"

    fixed_count = 0

    # 修复result目录
    for root, dirs, files in os.walk(result_dir):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                if fix_invalid_methods(file_path):
                    fixed_count += 1

    # 修复vo目录
    for root, dirs, files in os.walk(vo_dir):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                if fix_invalid_methods(file_path):
                    fixed_count += 1

    print(f"修复完成: 修复了 {fixed_count} 个文件")

if __name__ == '__main__':
    main()