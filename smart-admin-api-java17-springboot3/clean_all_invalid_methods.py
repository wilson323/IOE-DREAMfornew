#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
全面清理所有无效的getter/setter方法
"""

import os
import re

def clean_invalid_methods_in_file(file_path):
    """清理单个文件中的所有无效方法"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        original_content = content

        # 移除所有包含Java关键字作为字段名的无效方法
        patterns_to_remove = [
            r'public\s+(?:static\s+)?(?:final\s+)?(?:get|set)(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|void|int|float|double|boolean|long|short|byte|char)\s*\([^)]*\)\s*\{[^}]*this\.(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|int|float|double|boolean|long|short|byte|char)\s*=\s*(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|int|float|double|boolean|long|short|byte|char)[^}]*\}',
            r'public\s+(?:static\s+)?(?:final\s+)?(?:get|set)(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|int|float|double|boolean|long|short|byte|char)\s*\([^)]*\)\s*\{\s*return\s+(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|int|float|double|boolean|long|short|byte|char);\s*\}',
            r'public\s+static\s+get(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character)\s*\([^)]*\)\s*\{[^}]*\}',
            r'public\s+void\s+set(?:static\s+)?(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character)\s*\([^)]*\)\s*\{[^}]*\}'
        ]

        for pattern in patterns_to_remove:
            content = re.sub(pattern, '', content, flags=re.MULTILINE | re.DOTALL)

        # 清理多余的空行
        content = re.sub(r'\n\s*\n\s*\n+', '\n\n', content)
        content = re.sub(r'}\s*\n\s*public\s+void\s+set', '}\n\n    public void set', content)

        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True

        return False

    except Exception as e:
        print(f"处理文件 {file_path} 时出错: {e}")
        return False

def main():
    """主函数"""
    # 需要清理的目录
    directories = [
        "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/result",
        "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo"
    ]

    fixed_count = 0

    for directory in directories:
        if not os.path.exists(directory):
            continue

        for root, dirs, files in os.walk(directory):
            for file in files:
                if file.endswith('.java'):
                    file_path = os.path.join(root, file)
                    if clean_invalid_methods_in_file(file_path):
                        print(f"已修复: {file_path}")
                        fixed_count += 1

    print(f"清理完成: 修复了 {fixed_count} 个文件")

if __name__ == '__main__':
    main()