#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
最终清理脚本 - 彻底修复所有无效方法
"""

import os
import re

def final_cleanup_file(file_path):
    """最终清理单个文件"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        original_content = content

        # 移除所有包含Java关键字作为字段名的方法
        content = re.sub(r'\n\s*public\s+(?:static\s+)?(?:final\s+)?(?:get|set)(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|void|int|float|double|boolean|long|short|byte|char)\s*\([^)]*\)\s*\{[^}]*\}', '\n', content)

        # 移除单独的类型声明
        content = re.sub(r'\n\s*(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|int|float|double|boolean|long|short|byte|char);?\s*\n', '\n', content)

        # 清理方法中的错误赋值
        content = re.sub(r'this\.(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|int|float|double|boolean|long|short|byte|char)\s*=\s*(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|int|float|double|boolean|long|short|byte|char)', '// Invalid assignment removed', content)

        # 清理return语句中的错误返回
        content = re.sub(r'return\s+(?:String|Integer|Double|Boolean|Long|Float|Short|Byte|Character|int|float|double|boolean|long|short|byte|char);', '// Invalid return removed', content)

        # 移除空的public void行
        content = re.sub(r'\n\s*public\s+void\s*\n', '\n', content)

        # 清理多余的空行
        content = re.sub(r'\n\s*\n\s*\n+', '\n\n', content)
        content = re.sub(r'\n\s*\n\s*}\s*$', '\n}', content)

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
        "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo",
        "sa-admin/src/main/java/net/lab1024/sa/admin/module/attendance/domain/vo",
        "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/biometric/domain/vo"
    ]

    fixed_count = 0

    for directory in directories:
        if not os.path.exists(directory):
            continue

        for root, dirs, files in os.walk(directory):
            for file in files:
                if file.endswith('.java'):
                    file_path = os.path.join(root, file)
                    if final_cleanup_file(file_path):
                        fixed_count += 1

    print(f"最终清理完成: 修复了 {fixed_count} 个文件")

if __name__ == '__main__':
    main()