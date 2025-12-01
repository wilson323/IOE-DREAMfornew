#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM 全面乱码修复脚本 (Python版本)
功能: 系统性地修复项目中所有文件的乱码问题
作者: Claude Code
日期: 2025-11-19
"""

import os
import sys
import re
from pathlib import Path
from typing import Dict, Tuple, Optional

# 项目根目录
PROJECT_ROOT = Path(r"D:\IOE-DREAM")

# 乱码修复映射表
ENCODING_FIXES: Dict[str, str] = {
    # 常见乱码模式修复
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
    
    # GBK乱码修复（常见模式）
    "考勤": "考勤",
    "服务": "服务",
    "实现": "实现",
    "管理": "管理",
    "查询": "查询",
    "打卡": "打卡",
    "员工": "员工",
    "记录": "记录",
    "不能": "不能",
    "为空": "为空",
    "失败": "失败",
    "验证": "验证",
    "位置": "位置",
    "超出": "超出",
    "允许": "允许",
    "范围": "范围",
    "设备": "设备",
    "列表": "列表",
    "日期": "日期",
    "分页": "分页",
    "条件": "条件",
    "按考勤": "按考勤",
    "倒序": "倒序",
    "排列": "排列",
    "执行": "执行",
    "转换": "转换",
    "根据": "根据",
    "不存在": "不存在",
    "参数": "参数",
    "异常": "异常",
    "统一": "统一",
    "响应": "响应",
    "格式": "格式",
    "集成": "集成",
    "缓存": "缓存",
    "管理器": "管理器",
    "规则": "规则",
    "引入": "引入",
    "严格": "严格",
    "遵循": "遵循",
    "规范": "规范",
    "负责": "负责",
    "业务": "业务",
    "逻辑": "逻辑",
    "处理": "处理",
    "事务": "事务",
    "边界": "边界",
    "完整": "完整",
    
    # 其他常见乱码
    "": "",
    "": "",
    "": "",
}

# 需要检查的文件类型
FILE_EXTENSIONS = [
    "*.java", "*.xml", "*.md", "*.js", "*.ts", "*.vue", 
    "*.json", "*.yml", "*.yaml", "*.properties", "*.txt", 
    "*.ps1", "*.sh", "*.py"
]

# 排除的目录
EXCLUDE_DIRS = {
    "node_modules", ".git", "target", "dist", "venv", 
    "__pycache__", ".idea", ".vscode", "build"
}


def detect_encoding(file_path: Path) -> Tuple[Optional[str], bool]:
    """
    检测文件编码
    
    Returns:
        (encoding, has_bom): 编码类型和是否有BOM
    """
    try:
        with open(file_path, 'rb') as f:
            raw = f.read()
            
        # 检测BOM
        if len(raw) >= 3 and raw[0:3] == b'\xEF\xBB\xBF':
            return ("UTF-8-BOM", True)
        if len(raw) >= 2 and raw[0:2] == b'\xFF\xFE':
            return ("UTF-16-LE", True)
        if len(raw) >= 2 and raw[0:2] == b'\xFE\xFF':
            return ("UTF-16-BE", True)
        
        # 尝试UTF-8
        try:
            content = raw.decode('utf-8')
            re_encoded = content.encode('utf-8')
            if raw == re_encoded:
                return ("UTF-8", False)
        except:
            pass
        
        # 尝试GBK
        try:
            content = raw.decode('gbk')
            return ("GBK", False)
        except:
            pass
        
        # 尝试GB2312
        try:
            content = raw.decode('gb2312')
            return ("GB2312", False)
        except:
            pass
        
        return (None, False)
    except Exception as e:
        print(f"  [ERROR] 检测编码失败 {file_path}: {e}")
        return (None, False)


def read_file_content(file_path: Path, encoding: Optional[str]) -> Optional[str]:
    """
    读取文件内容
    """
    try:
        if encoding == "GBK" or encoding == "GB2312":
            with open(file_path, 'rb') as f:
                raw = f.read()
            if encoding == "GBK":
                return raw.decode('gbk', errors='replace')
            else:
                return raw.decode('gb2312', errors='replace')
        else:
            with open(file_path, 'r', encoding='utf-8', errors='replace') as f:
                return f.read()
    except Exception as e:
        print(f"  [ERROR] 读取文件失败 {file_path}: {e}")
        return None


def fix_garbled_characters(content: str) -> Tuple[str, bool]:
    """
    修复乱码字符
    
    Returns:
        (fixed_content, has_changes): 修复后的内容和是否有变化
    """
    original_content = content
    has_changes = False
    
    # 移除BOM标记
    if content.startswith('\ufeff'):
        content = content[1:]
        has_changes = True
    
    # 应用乱码修复映射
    for key, value in ENCODING_FIXES.items():
        if key in content:
            content = content.replace(key, value)
            has_changes = True
    
    # 修复其他常见乱码模式
    # 修复问号结尾的乱码（如"检查" -> "检查"）
    content = re.sub(r'([\u4e00-\u9fa5])\?', r'\1查', content)
    
    # 移除其他特殊乱码字符
    content = content.replace('', '')
    content = content.replace('', '')
    content = content.replace('', '')
    
    return (content, has_changes or content != original_content)


def should_process_file(file_path: Path) -> bool:
    """
    判断是否应该处理该文件
    """
    # 检查是否在排除目录中
    parts = file_path.parts
    for part in parts:
        if part in EXCLUDE_DIRS:
            return False
    return True


def process_file(file_path: Path) -> Tuple[bool, int, int, int]:
    """
    处理单个文件
    
    Returns:
        (success, encoding_converted, bom_removed, garbled_fixed): 
        成功标志、编码转换数、BOM移除数、乱码修复数
    """
    try:
        # 检测编码
        encoding, has_bom = detect_encoding(file_path)
        
        if encoding is None:
            return (False, 0, 0, 0)
        
        needs_conversion = encoding not in ("UTF-8", None)
        needs_bom_removal = has_bom
        
        # 读取文件内容
        content = read_file_content(file_path, encoding)
        if content is None:
            return (False, 0, 0, 0)
        
        # 修复乱码
        fixed_content, has_garbled_fixes = fix_garbled_characters(content)
        
        # 如果需要修复，保存文件
        if needs_conversion or needs_bom_removal or has_garbled_fixes:
            # 使用UTF-8无BOM保存
            with open(file_path, 'w', encoding='utf-8', newline='\n', errors='replace') as f:
                f.write(fixed_content)
            
            fix_messages = []
            encoding_converted = 1 if needs_conversion else 0
            bom_removed = 1 if needs_bom_removal else 0
            garbled_fixed = 1 if has_garbled_fixes else 0
            
            if needs_conversion:
                fix_messages.append(f"编码转换: {encoding} -> UTF-8")
            if needs_bom_removal:
                fix_messages.append("移除BOM")
            if has_garbled_fixes:
                fix_messages.append("修复乱码")
            
            print(f"  [FIXED] {file_path.name} - {', '.join(fix_messages)}")
            return (True, encoding_converted, bom_removed, garbled_fixed)
        
        return (True, 0, 0, 0)
        
    except Exception as e:
        print(f"  [ERROR] 处理文件失败 {file_path}: {e}")
        return (False, 0, 0, 0)


def main():
    """主函数"""
    print("\n" + "=" * 76)
    print("🔧 IOE-DREAM 全面乱码修复脚本 (Python版本)")
    print(f"⏰ 执行时间: {__import__('datetime').datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 76 + "\n")
    
    os.chdir(PROJECT_ROOT)
    
    fixed_files = 0
    error_files = 0
    total_files = 0
    encoding_converted = 0
    bom_removed = 0
    garbled_fixed = 0
    
    print("开始扫描项目文件...\n")
    
    # 处理所有文件类型
    for ext in FILE_EXTENSIONS:
        pattern = ext.replace('*', '')
        files = list(PROJECT_ROOT.rglob(ext))
        files = [f for f in files if should_process_file(f)]
        
        print(f"检查 {ext} 文件: {len(files)} 个")
        
        for file_path in files:
            total_files += 1
            
            success, enc_conv, bom_rm, garb_fix = process_file(file_path)
            
            if success:
                fixed_files += enc_conv + bom_rm + garb_fix
                encoding_converted += enc_conv
                bom_removed += bom_rm
                garbled_fixed += garb_fix
            else:
                error_files += 1
            
            # 每处理100个文件显示进度
            if total_files % 100 == 0:
                print(f"进度: {total_files} 文件已处理...")
    
    print("\n" + "=" * 76)
    print("📊 修复结果汇总")
    print("=" * 76)
    print(f"总文件数: {total_files}")
    print(f"修复文件数: {fixed_files}")
    print(f"编码转换数: {encoding_converted}")
    print(f"BOM移除数: {bom_removed}")
    print(f"乱码修复数: {garbled_fixed}")
    print(f"错误文件数: {error_files}")
    print("=" * 76 + "\n")
    
    if error_files == 0:
        print("[SUCCESS] 所有文件乱码修复完成！\n")
        return 0
    else:
        print("[WARNING] 部分文件修复失败，请检查错误信息\n")
        return 1


if __name__ == "__main__":
    sys.exit(main())

