#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修复乱码中文编码问题
修复测试文件中的乱码中文注释
"""

import os
import re
from pathlib import Path

# 乱码映射表（按长度从长到短排序）
GARBLED_MAP = {
    "娴嬭瘯绀轰緥-鎴愬姛鍦烘櫙": "测试示例-成功场景",
    "TODO: 鍑嗗璇锋眰鏁版嵁": "TODO: 准备请求数据",
    "TODO: 鎵цController鏂规硶": "TODO: 执行Controller方法",
    "TODO: 楠岃瘉缁撴灉": "TODO: 验证结果",
    "鐩爣瑕嗙洊鐜囷細鈮?0%": "目标覆盖率：>= 80%",
    "测试范围锛?{className}核心API方法": "测试范围：{className}核心API方法",
    "测试范围锛?{className}核心业务方法": "测试范围：{className}核心业务方法",
    "{className}鏍稿績API鏂规硶": "{className}核心API方法",
    "{className}鏍稿績涓氬姟鏂规硶": "{className}核心业务方法",
    "鏍稿績涓氬姟鏂规硶": "核心业务方法",
    "鏍稿績API鏂规硶": "核心API方法",
    "鍑嗗娴嬭瘯鏁版嵁": "准备测试数据",
    "娴嬭瘯鑼冨洿锛?{className}": "测试范围：{className}",
    "测试范围锛?{className}": "测试范围：{className}",
    "鍗曞厓娴嬭瘯": "单元测试",
    "鐩爣瑕嗙洊鐜": "目标覆盖率",
    "鐩爣瑕嗙洊鐜囷細鈮?0%": "目标覆盖率：>= 80%",
    "目标覆盖率：≥80%": "目标覆盖率：>= 80%",
    "娴嬭瘯鑼冨洿": "测试范围",
    "娴嬭瘯绀轰緥": "测试示例",
    "鎴愬姛鍦烘櫙": "成功场景",
    "鍑嗗璇锋眰": "准备请求",
    "鎵цController": "执行Controller",
    "楠岃瘉缁撴灉": "验证结果",
    "鏍稿績": "核心",
    "涓氬姟": "业务",
    "鏂规硶": "方法",
    "鍑嗗": "准备",
    "鏁版嵁": "数据",
    "娴嬭瘯": "测试",
    "绀轰緥": "示例",
    "鎴愬姛": "成功",
    "鍦烘櫙": "场景",
    "璇锋眰": "请求",
    "鎵ц": "执行",
    "楠岃瘉": "验证",
    "缁撴灉": "结果",
}

def fix_garbled_file(file_path):
    """修复单个文件的乱码"""
    try:
        # 读取文件内容
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # 替换所有乱码（按长度从长到短排序，避免部分匹配）
        sorted_map = sorted(GARBLED_MAP.items(), key=lambda x: len(x[0]), reverse=True)
        for garbled, correct in sorted_map:
            content = content.replace(garbled, correct)
        
        # 修复残留的乱码字符（先处理，避免影响后续替换）
        # 修复"准备测试数据"、"准备请求数据"等中间有乱码的情况（匹配任何非中文字符）
        content = re.sub(r'准备[^\u4e00-\u9fff]*测试数据', '准备测试数据', content)
        content = re.sub(r'准备[^\u4e00-\u9fff]*请求数据', '准备请求数据', content)
        content = re.sub(r'执行[^\u4e00-\u9fff]*Controller方法', '执行Controller方法', content)
        content = re.sub(r'执行[^\u4e00-\u9fff]*被测试方法', '执行被测试方法', content)
        # 修复"{className}"占位符，替换为实际的类名
        if '{className}' in content:
            # 从类定义中提取类名
            class_match = re.search(r'class\s+(\w+)Test', content)
            if class_match:
                class_name = class_match.group(1)
                content = content.replace('{className}', class_name)
        # 修复"鐩爣瑕嗙洊"这种残留乱码（使用更宽松的匹配）
        content = re.sub(r'鐩[^\u4e00-\u9fff]*爣[^\u4e00-\u9fff]*瑕[^\u4e00-\u9fff]*嗙[^\u4e00-\u9fff]*洊[^\u4e00-\u9fff]*鐜[^\u4e00-\u9fff]*囷[^\u4e00-\u9fff]*細[^\u4e00-\u9fff]*鈮[^\u4e00-\u9fff]*\?[^\u4e00-\u9fff]*0%', '目标覆盖率：>= 80%', content)
        content = re.sub(r'鐩[^\u4e00-\u9fff]*爣[^\u4e00-\u9fff]*瑕[^\u4e00-\u9fff]*嗙[^\u4e00-\u9fff]*洊[^\u4e00-\u9fff]*鐜', '目标覆盖率', content)
        # 修复"{className}"占位符
        content = re.sub(r'测试范围：\{className\}核心API方法', lambda m: content.split('class ')[1].split('Test')[0] + '核心API方法' if 'class ' in content else m.group(0), content)
        content = re.sub(r'测试范围：\{className\}核心业务方法', lambda m: content.split('class ')[1].split('Test')[0] + '核心业务方法' if 'class ' in content else m.group(0), content)
        
        # 修复常见的语法错误
        # 修复缺少变量名的@InjectMocks（如：private LoginController ;）
        content = re.sub(r'@InjectMocks\s+private\s+(\w+)\s+;', r'@InjectMocks\n    private \1 \1;', content)
        # 修复变量名重复的问题（如：private VisitorController VisitorController;）
        # 将"private ClassName ClassName;"改为"private ClassName className;"（首字母小写）
        def fix_duplicate_var(match):
            class_name = match.group(1)
            var_name = class_name[0].lower() + class_name[1:] if len(class_name) > 1 else class_name.lower()
            return f'private {class_name} {var_name};'
        content = re.sub(r'private\s+(\w+)\s+(\1);', fix_duplicate_var, content)
        
        # 如果内容有变化，保存文件
        if content != original_content:
            # 使用UTF-8 without BOM保存
            with open(file_path, 'w', encoding='utf-8', newline='') as f:
                f.write(content)
            return True
        return False
    except Exception as e:
        print(f"Error fixing {file_path}: {e}")
        return False

def main():
    """主函数"""
    base_dir = Path("microservices")
    test_files = list(base_dir.rglob("*Test.java"))
    
    fixed_count = 0
    error_count = 0
    
    for file_path in test_files:
        try:
            # 检查文件是否包含乱码
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            has_garbled = any(garbled in content for garbled in GARBLED_MAP.keys())
            
            if has_garbled:
                print(f"[处理] {file_path}")
                if fix_garbled_file(file_path):
                    fixed_count += 1
                    print(f"[OK] 已修复: {file_path.name}")
                else:
                    print(f"[SKIP] 无需修复: {file_path.name}")
        except Exception as e:
            error_count += 1
            print(f"[ERROR] 修复失败: {file_path} - {e}")
    
    print(f"\n[总结]")
    print(f"  总文件数: {len(test_files)}")
    print(f"  修复成功: {fixed_count}")
    print(f"  修复失败: {error_count}")

if __name__ == "__main__":
    main()

