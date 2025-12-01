#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修复Lombok注解处理器失效问题的脚本
自动为@Data注解的类添加手动getter/setter方法
"""

import os
import re
import sys
from pathlib import Path

def find_java_files(root_dir):
    """查找所有Java文件"""
    java_files = []
    for root, dirs, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
    return java_files

def has_lombok_annotation(file_path):
    """检查文件是否包含Lombok注解"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            return '@Data' in content or '@Getter' in content or '@Setter' in content
    except:
        return False

def get_class_name_and_fields(file_path):
    """提取类名和字段信息"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # 查找类名
        class_match = re.search(r'public\s+class\s+(\w+)', content)
        if not class_match:
            return None, None
        class_name = class_match.group(1)

        # 提取字段（简化版，只处理private字段）
        field_pattern = r'@Schema\(description\s*=\s*"[^"]*"\)\s*\n\s*private\s+(\w+(?:<[^>]+>)?)\s+(\w+)'
        fields = re.findall(field_pattern, content)

        # 也尝试匹配简单private字段
        simple_field_pattern = r'private\s+(\w+(?:<[^>]+>)?)\s+(\w+)'
        simple_fields = re.findall(simple_field_pattern, content)

        # 合并字段
        all_fields = fields + simple_fields
        unique_fields = []
        seen = set()
        for field_type, field_name in all_fields:
            if field_name not in seen:
                unique_fields.append((field_type, field_name))
                seen.add(field_name)

        return class_name, unique_fields
    except Exception as e:
        print(f"解析文件 {file_path} 时出错: {e}")
        return None, None

def generate_getter_setter(class_name, fields):
    """生成getter/setter方法"""
    methods = []

    for field_type, field_name in fields:
        # 生成getter方法
        if field_type in ['boolean', 'Boolean']:
            getter_method = f"    public {field_type} is{field_name[0].upper()}{field_name[1:]}() {{\n        return {field_name};\n    }}"
        else:
            getter_method = f"    public {field_type} get{field_name[0].upper()}{field_name[1:]}() {{\n        return {field_name};\n    }}"

        # 生成setter方法
        setter_method = f"    public void set{field_name[0].upper()}{field_name[1:]}({field_type} {field_name}) {{\n        this.{field_name} = {field_name};\n    }}"

        methods.extend([getter_method, setter_method, ""])

    return "\n".join(methods)

def fix_file(file_path):
    """修复单个文件的Lombok问题"""
    try:
        print(f"正在修复文件: {file_path}")

        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        class_name, fields = get_class_name_and_fields(file_path)
        if not class_name or not fields:
            print(f"  跳过: 无法提取类或字段信息")
            return False

        # 检查是否已经有手动方法
        if 'get' + fields[0][1][0].upper() + fields[0][1][1:] in content:
            print(f"  跳过: 已有手动方法")
            return False

        # 生成getter/setter方法
        methods = generate_getter_setter(class_name, fields)

        # 找到类的结束位置，在之前添加方法
        class_end_pattern = r'(public\s+class\s+' + re.escape(class_name) + r'[^{]*\{[^}]*)}'

        # 简单的方法：找到最后一个}之前的位置
        last_brace_pos = content.rfind('}')
        if last_brace_pos == -1:
            print(f"  跳过: 无法找到类结束位置")
            return False

        # 在最后一个}之前添加方法
        new_content = content[:last_brace_pos] + f"\n\n    // 手动添加的getter/setter方法 (Lombok失效备用)\n    {methods}\n" + content[last_brace_pos:]

        # 写回文件
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)

        print(f"  完成: 为{class_name}添加了{len(fields)}个字段的getter/setter方法")
        return True

    except Exception as e:
        print(f"  错误: {e}")
        return False

def main():
    """主函数"""
    if len(sys.argv) != 2:
        print("用法: python fix_lombok_batch.py <Java文件目录>")
        sys.exit(1)

    root_dir = sys.argv[1]
    if not os.path.isdir(root_dir):
        print(f"错误: 目录不存在: {root_dir}")
        sys.exit(1)

    print(f"开始扫描目录: {root_dir}")

    java_files = find_java_files(root_dir)
    print(f"找到 {len(java_files)} 个Java文件")

    # 筛选包含Lombok注解的文件
    lombok_files = [f for f in java_files if has_lombok_annotation(f)]
    print(f"其中 {len(lombok_files)} 个文件包含Lombok注解")

    # 修复文件
    fixed_count = 0
    for java_file in lombok_files:
        if fix_file(java_file):
            fixed_count += 1

    print(f"\n修复完成: 成功修复了 {fixed_count} 个文件")

if __name__ == '__main__':
    main()