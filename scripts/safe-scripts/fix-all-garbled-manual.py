#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM 手动修复所有乱码脚本
全面扫描并修复项目中的所有乱码字符
"""

import os
import sys
import re
from pathlib import Path
from typing import List, Tuple, Dict

# 项目根目录
PROJECT_ROOT = Path(r"D:\IOE-DREAM")

# 定义乱码模式
GARBLED_PATTERNS = [
    "",
    "",
    "涓",
    "鏂",
    "锘烘湰",
    "鑰",
    "绠",
    "悊",
    "嫟",
    "鎵",
    "崱",
    "庡",
    "煡",
    "璇",
    "娑",
    "樺",
    "煑",
    "检查",
    "结果",
    "不一致",
    "时间",
    "处理",
    "不能为空",
    "长度不能超过",
    "格式：YYYY-MM）",
    "一致性",
    "完整性",
    "对账",
    "并行处理",
    "检查结果",
    "批量检查结果",
    "DAILY/MONTHLY/CUSTOM）",
    "",
    "??",
    "?"
]

# 定义修复映射表
FIX_MAPPINGS = {
    "检查": "检查",
    "结果": "结果",
    "不一致": "不一致",
    "时间": "时间",
    "处理": "处理",
    "不能为空": "不能为空",
    "长度不能超过": "长度不能超过",
    "格式：YYYY-MM）": "格式：YYYY-MM）",
    "一致性": "一致性",
    "完整性": "完整性",
    "对账": "对账",
    "并行处理": "并行处理",
    "检查结果": "检查结果",
    "批量检查结果": "批量检查结果",
    "DAILY/MONTHLY/CUSTOM）": "DAILY/MONTHLY/CUSTOM）",
}

# 需要检查的文件类型
FILE_TYPES = [
    "*.java", "*.ps1", "*.py", "*.md", "*.sh",
    "*.xml", "*.yaml", "*.yml", "*.properties", "*.txt"
]

# 排除的目录
EXCLUDE_DIRS = {
    "node_modules", ".git", "target", "build", "venv", "__pycache__",
    ".idea", ".vscode", "dist", "out"
}


def should_process_file(file_path: Path) -> bool:
    """判断文件是否需要处理"""
    # 检查是否在排除目录中
    parts = file_path.parts
    for exclude_dir in EXCLUDE_DIRS:
        if exclude_dir in parts:
            return False
    return True


def detect_encoding(file_path: Path) -> Tuple[str, str]:
    """检测文件编码"""
    encodings_to_try = ['utf-8', 'utf-8-sig', 'gbk', 'gb2312', 'big5', 'latin1']
    
    with open(file_path, 'rb') as f:
        raw_content = f.read()
    
    for encoding in encodings_to_try:
        try:
            content = raw_content.decode(encoding)
            return content, encoding
        except (UnicodeDecodeError, LookupError):
            continue
    
    # 如果所有编码都失败，使用utf-8并替换错误字符
    try:
        content = raw_content.decode('utf-8', errors='replace')
        return content, 'utf-8-replace'
    except Exception:
        return None, None


def has_garbled_characters(content: str) -> bool:
    """检查内容是否包含乱码"""
    for pattern in GARBLED_PATTERNS:
        if pattern in content:
            return True
    return False


def fix_garbled_characters(content: str) -> Tuple[str, bool]:
    """修复乱码字符"""
    original_content = content
    has_changes = False
    
    # 移除BOM
    if content.startswith('\ufeff'):
        content = content[1:]
        has_changes = True
    
    # 应用修复映射
    for key, value in FIX_MAPPINGS.items():
        if key in content:
            content = content.replace(key, value)
            has_changes = True
    
    # 移除其他乱码字符
    replacements = {
        "": "",
        "": "",
        "涓": "",
        "鏂": "",
    }
    
    for key, value in replacements.items():
        if key in content:
            content = content.replace(key, value)
            has_changes = True
    
    return content, has_changes


def process_file(file_path: Path) -> Tuple[bool, str, int]:
    """
    处理单个文件
    返回: (是否成功, 消息, 修复类型)
    修复类型: 0=无修复, 1=编码转换, 2=乱码修复, 3=BOM移除
    """
    try:
        # 检测编码并读取内容
        content, encoding = detect_encoding(file_path)
        
        if content is None:
            return False, f"无法读取文件: {file_path}", 0
        
        original_content = content
        has_garbled = has_garbled_characters(content)
        
        # 修复乱码
        fixed_content, has_changes = fix_garbled_characters(content)
        
        # 判断是否需要保存
        needs_save = False
        fix_type = 0
        
        if encoding != 'utf-8' and encoding != 'utf-8-sig':
            needs_save = True
            fix_type = 1  # 编码转换
        elif has_garbled:
            needs_save = True
            fix_type = 2  # 乱码修复
        elif has_changes:
            needs_save = True
            fix_type = 3  # BOM移除
        
        if needs_save:
            # 保存为UTF-8无BOM
            with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
                f.write(fixed_content)
            
            messages = {
                1: f"编码转换: {encoding} -> UTF-8",
                2: "乱码已修复",
                3: "BOM已移除"
            }
            return True, messages.get(fix_type, "已修复"), fix_type
        
        return True, "无需修复", 0
        
    except Exception as e:
        return False, f"处理失败: {str(e)}", 0


def get_all_files() -> List[Path]:
    """获取所有需要检查的文件"""
    all_files = []
    
    for file_type in FILE_TYPES:
        pattern = file_type.replace('*', '')
        for file_path in PROJECT_ROOT.rglob(f"*{pattern}"):
            if file_path.is_file() and should_process_file(file_path):
                all_files.append(file_path)
    
    return all_files


def main():
    """主函数"""
    print("\n" + "=" * 50)
    print("IOE-DREAM 手动乱码修复工具")
    print("=" * 50 + "\n")
    
    # 切换到项目根目录
    os.chdir(PROJECT_ROOT)
    
    # 获取所有文件
    print("正在扫描文件...")
    all_files = get_all_files()
    print(f"找到 {len(all_files)} 个文件需要检查\n")
    
    # 统计信息
    fixed_files = 0
    error_files = 0
    files_with_garbled = []
    
    # 处理每个文件
    for idx, file_path in enumerate(all_files, 1):
        success, message, fix_type = process_file(file_path)
        
        if success:
            if fix_type > 0:
                fixed_files += 1
                print(f"  [FIXED] {file_path.name} ({message})")
            if fix_type == 2:
                files_with_garbled.append(str(file_path))
        else:
            error_files += 1
            print(f"  [ERROR] {file_path}: {message}")
        
        # 显示进度
        if idx % 100 == 0:
            print(f"进度: {idx} / {len(all_files)} 文件已处理...")
    
    # 输出总结
    print("\n" + "=" * 50)
    print("修复总结")
    print("=" * 50)
    print(f"总文件数: {len(all_files)}")
    print(f"修复文件数: {fixed_files}")
    print(f"错误文件数: {error_files}")
    print(f"发现乱码文件数: {len(files_with_garbled)}")
    
    if files_with_garbled:
        print("\n包含乱码的文件列表:")
        for file_path in files_with_garbled:
            print(f"  - {file_path}")
    
    print("\n" + "=" * 50 + "\n")
    
    if error_files == 0 and len(files_with_garbled) == 0:
        print("[SUCCESS] 所有乱码已修复完成!\n")
        return 0
    elif error_files == 0:
        print("[WARNING] 部分文件仍包含乱码，请手动检查\n")
        return 1
    else:
        print("[ERROR] 修复过程中出现错误，请检查\n")
        return 1


if __name__ == "__main__":
    sys.exit(main())




