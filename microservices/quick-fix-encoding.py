#!/usr/bin/env python3
import os
import re
from pathlib import Path

def fix_file_encoding(file_path):
    try:
        # 读取文件内容，使用replace处理错误字符
        with open(file_path, 'r', encoding='utf-8', errors='replace') as f:
            content = f.read()

        # 修复常见的编码问题
        content = content.replace('部门负责人姓�?', '部门负责人姓名')
        content = content.replace('部门电话格式不正�?', '部门电话格式不正确')
        content = content.replace('部门邮箱格式不正�?', '部门邮箱格式不正确')
        content = content.replace('性别�?�?2�?', '性别：1-男，2-女')
        content = content.replace('手机�?\n', '手机号码\n')
        content = content.replace('未结束的字符串字面量', '')
        content = content.replace('?\\s*\\*/', '')

        # 写回文件
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

        return True
    except Exception as e:
        print(f"Error fixing {file_path}: {e}")
        return False

def main():
    base_path = Path("D:/IOE-DREAM/microservices/ioedream-system-service/src/main/java")

    if not base_path.exists():
        print("Directory not found")
        return

    # 查找所有Java文件
    java_files = list(base_path.rglob("*.java"))

    fixed_count = 0
    for file_path in java_files:
        if fix_file_encoding(file_path):
            fixed_count += 1

    print(f"Fixed {fixed_count} Java files")

if __name__ == "__main__":
    main()