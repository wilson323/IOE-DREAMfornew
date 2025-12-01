#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM 全局乱码修复脚本 (Python版本)
批量修复项目中所有Java文件的乱码问题
"""

import os
import sys
import re
from pathlib import Path

# 乱码修复映射表
ENCODING_FIXES = {
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
}

def fix_file_encoding(file_path):
    """修复单个文件的编码问题"""
    try:
        # 尝试多种编码读取
        content = None
        encoding_used = None
        
        encodings_to_try = ['utf-8', 'utf-8-sig', 'gbk', 'gb2312', 'big5']
        
        for encoding in encodings_to_try:
            try:
                with open(file_path, 'r', encoding=encoding, errors='replace') as f:
                    content = f.read()
                encoding_used = encoding
                break
            except (UnicodeDecodeError, LookupError):
                continue
        
        if content is None:
            # 最后尝试：二进制读取后解码
            with open(file_path, 'rb') as f:
                raw_content = f.read()
            try:
                content = raw_content.decode('gbk', errors='replace')
                encoding_used = 'gbk'
            except:
                content = raw_content.decode('utf-8', errors='replace')
                encoding_used = 'utf-8-replace'
        
        original_content = content
        has_changes = False
        
        # 移除BOM标记
        if content.startswith('\ufeff'):
            content = content[1:]
            has_changes = True
        
        # 应用乱码修复映射
        for pattern, replacement in ENCODING_FIXES.items():
            if pattern in content:
                content = content.replace(pattern, replacement)
                has_changes = True
        
        # 修复ReconciliationService.java中的特定乱码
        if file_path.name == "ReconciliationService.java":
            fixes = {
                "一致性检查": "一致性检查",
                "格式：YYYY-MM）": "格式：YYYY-MM）",
                "一致性": "一致性",
                "检查结果": "检查结果",
                "批量检查结果": "批量检查结果",
                "不一致": "不一致",
                "开始时间": "开始时间",
                "DAILY/MONTHLY/CUSTOM）": "DAILY/MONTHLY/CUSTOM）",
                "完整性": "完整性",
                "对账": "对账",
                "并行处理": "并行处理",
            }
            
            for pattern, replacement in fixes.items():
                if pattern in content:
                    content = content.replace(pattern, replacement)
                    has_changes = True
            
            # 删除接口定义后的实现类代码
            lines = content.split('\n')
            new_lines = []
            in_interface = True
            brace_count = 0
            
            for line in lines:
                if in_interface:
                    new_lines.append(line)
                    # 计算大括号
                    brace_count += line.count('{') - line.count('}')
                    # 如果接口定义结束（大括号匹配）
                    if brace_count == 0 and line.strip().endswith('}'):
                        in_interface = False
                else:
                    # 跳过实现类代码
                    if line.strip() and not line.strip().startswith('//'):
                        break
            
            content = '\n'.join(new_lines)
            has_changes = True
        
        # 如果有修改或编码不是UTF-8，保存文件
        if has_changes or encoding_used != 'utf-8':
            # 使用UTF-8无BOM保存
            with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
                f.write(content)
            return True, encoding_used
        
        return False, encoding_used
        
    except Exception as e:
        print(f"  [ERROR] {file_path}: {e}")
        return False, None

def main():
    project_root = Path("D:/IOE-DREAM")
    java_dir = project_root / "smart-admin-api-java17-springboot3"
    
    print("=" * 60)
    print("🔧 IOE-DREAM 全局乱码修复脚本")
    print("=" * 60)
    print()
    
    # 获取所有Java文件
    java_files = list(java_dir.rglob("*.java"))
    
    print(f"发现 {len(java_files)} 个Java文件需要检查\n")
    
    fixed_files = 0
    error_files = 0
    
    for i, file_path in enumerate(java_files, 1):
        success, encoding = fix_file_encoding(file_path)
        
        if success:
            fixed_files += 1
            if encoding != 'utf-8':
                print(f"  [FIXED] {file_path.relative_to(project_root)} (编码: {encoding} -> UTF-8)")
            else:
                print(f"  [FIXED] {file_path.relative_to(project_root)} (乱码修复)")
        
        if not success and encoding is None:
            error_files += 1
        
        # 每处理100个文件显示进度
        if i % 100 == 0:
            print(f"进度: {i} / {len(java_files)} 文件已处理...")
    
    print()
    print("=" * 60)
    print("📊 修复结果汇总")
    print("=" * 60)
    print(f"总文件数: {len(java_files)}")
    print(f"修复文件数: {fixed_files}")
    print(f"错误文件数: {error_files}")
    print("=" * 60)
    print()
    
    if error_files == 0:
        print("[SUCCESS] 所有文件乱码修复完成！\n")
        return 0
    else:
        print(f"[WARNING] {error_files} 个文件修复失败，请检查错误信息\n")
        return 1

if __name__ == "__main__":
    sys.exit(main())

