#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
系统性修复@Slf4j注解为标准Logger定义的脚本
作者: IOE-DREAM代码质量守护专家
创建时间: 2025-11-23
遵循repowiki编码规范
"""

import os
import re
import sys
from pathlib import Path

class Slf4jFixer:
    def __init__(self, base_path):
        self.base_path = Path(base_path)
        self.total_files = 0
        self.fixed_count = 0
        self.error_count = 0

    def find_slf4j_files(self):
        """查找所有包含@Slf4j注解的Java文件"""
        slf4j_files = []

        # 递归查找所有Java文件
        for java_file in self.base_path.rglob("*.java"):
            try:
                with open(java_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                    # 查找真正的@Slf4j注解（不在注释中的）
                    slf4j_pattern = r'^[^/\n]*@\s*Slf4j'
                    if re.search(slf4j_pattern, content, re.MULTILINE):
                        slf4j_files.append(java_file)
            except Exception as e:
                print(f"[ERROR] 读取文件失败 {java_file}: {e}")

        return slf4j_files

    def extract_class_name(self, content):
        """从文件内容中提取类名"""
        # 查找public class定义
        class_match = re.search(r'public\s+class\s+(\w+)', content)
        if class_match:
            return class_match.group(1)

        # 查找其他class定义（如abstract class）
        class_match = re.search(r'(?:abstract\s+)?class\s+(\w+)', content)
        if class_match:
            return class_match.group(1)

        return None

    def fix_slf4j_file(self, file_path):
        """修复单个Java文件中的@Slf4j注解"""
        try:
            # 读取文件内容
            with open(file_path, 'r', encoding='utf-8') as f:
                original_content = f.read()

            # 提取类名
            class_name = self.extract_class_name(original_content)
            if not class_name:
                print(f"[ERROR] 无法提取类名: {file_path}")
                return False

            print(f"[PROCESSING] 正在处理: {file_path}")
            print(f"[INFO] 类名: {class_name}")

            # 步骤1: 移除lombok.extern.slf4j import
            content = re.sub(r'import\s+lombok\.extern\.slf4j\.Slf4j;\s*\n?', '', original_content)

            # 步骤2: 替换@Slf4j注解为注释（只替换独立的注解，不在注释中的）
            # 使用更精确的正则表达式，只匹配行首的@Slf4j注解
            content = re.sub(r'^\s*@\s*Slf4j\s*\n?', '// @Slf4j - 手动添加log变量替代Lombok注解\n', content, flags=re.MULTILINE)

            # 步骤3: 添加SLF4J imports（如果还没有）
            if 'import org.slf4j.Logger;' not in content:
                # 在package声明后添加import
                package_pattern = r'(package\s+[^;]+;)'
                slf4j_imports = '\n\nimport org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;'
                content = re.sub(package_pattern, r'\1' + slf4j_imports, content)

            # 步骤4: 添加Logger定义（如果还没有）
            if 'LoggerFactory.getLogger' not in original_content:
                # 查找类定义位置
                class_pattern_str = r'(public\s+(?:abstract\s+)?class\s+' + class_name + r'\s*(?:extends\s+\w+\s*)?(?:implements\s+[^{]+)?\s*\{)'
                class_pattern = re.compile(class_pattern_str)

                logger_definition = f'''    // @Slf4j - 手动添加log变量替代Lombok注解
    private static final Logger log = LoggerFactory.getLogger({class_name}.class);'''

                def replace_class_with_logger(match):
                    return match.group(1) + '\n\n' + logger_definition

                content = re.sub(class_pattern, replace_class_with_logger, content)
            else:
                print("[WARNING] 文件中已存在Logger定义，跳过添加Logger定义")

            # 步骤5: 写入修复后的内容
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)

            # 步骤6: 验证修复结果
            success = self.verify_fix(original_content, content, class_name)

            if success:
                print("[SUCCESS] 文件修复成功")
                self.fixed_count += 1
            else:
                print("[ERROR] 文件修复失败")
                self.error_count += 1

            return success

        except Exception as e:
            print(f"[ERROR] 处理文件时出错 {file_path}: {e}")
            self.error_count += 1
            return False

    def verify_fix(self, original_content, modified_content, class_name):
        """验证修复结果"""
        success = True

        # 检查1: 移除了@Slf4j import
        if 'import lombok.extern.slf4j.Slf4j;' in modified_content:
            print("[ERROR] 未成功移除@Slf4j import")
            success = False
        else:
            print("[OK] 成功移除@Slf4j import")

        # 检查2: 添加了SLF4J imports
        if 'import org.slf4j.Logger;' in modified_content:
            print("[OK] 成功添加Logger import")
        else:
            print("[ERROR] 未添加Logger import")
            success = False

        if 'import org.slf4j.LoggerFactory;' in modified_content:
            print("[OK] 成功添加LoggerFactory import")
        else:
            print("[ERROR] 未添加LoggerFactory import")
            success = False

        # 检查3: 替换了@Slf4j注解（修复验证逻辑）
        # 检查是否还有独立的@Slf4j注解（不在注释中的）
        slf4j_pattern = r'^[^/\n]*@\s*Slf4j'
        if re.search(slf4j_pattern, modified_content, re.MULTILINE):
            print("[ERROR] 未成功替换@Slf4j注解")
            success = False
        else:
            print("[OK] 成功替换@Slf4j注解")

        # 检查4: 添加了Logger定义
        if 'LoggerFactory.getLogger' in original_content:
            print("[INFO] 原文件已包含Logger定义")
        elif f'LoggerFactory.getLogger({class_name}.class)' in modified_content:
            print("[OK] 成功添加Logger定义")
        else:
            print("[ERROR] 未添加Logger定义")
            success = False

        return success

    def fix_all_files(self):
        """修复所有包含@Slf4j注解的文件"""
        print("[START] 开始系统性修复@Slf4j注解...")

        # 查找所有需要修复的文件
        slf4j_files = self.find_slf4j_files()
        self.total_files = len(slf4j_files)

        print(f"[INFO] 总计需要修复的文件数量: {self.total_files}")

        if self.total_files == 0:
            print("[SUCCESS] 没有找到需要修复的文件！")
            return True

        # 处理每个文件
        for i, file_path in enumerate(slf4j_files, 1):
            print(f"\n[PROGRESS] 进度: {i}/{self.total_files}")
            self.fix_slf4j_file(file_path)

        # 输出最终统计
        self.print_final_statistics()

        return self.error_count == 0

    def print_final_statistics(self):
        """输出最终统计信息"""
        print("\n" + "="*50)
        print("[COMPLETE] @Slf4j修复完成！")
        print("[STATISTICS] 最终统计:")
        print(f"  [SUCCESS] 成功修复: {self.fixed_count} 个文件")
        print(f"  [ERROR] 错误数量: {self.error_count} 个")

        if self.total_files > 0:
            success_rate = (self.fixed_count * 100) // self.total_files
            print(f"  [RATE] 成功率: {success_rate}%")

        if self.error_count == 0:
            print("[COMPLETE] 所有文件修复成功，0错误！")
        else:
            print("[WARNING] 发现错误，请检查上述日志")

def main():
    """主函数"""
    base_path = r"D:\IOE-DREAM\smart-admin-api-java17-springboot3"

    # 检查路径是否存在
    if not os.path.exists(base_path):
        print(f"[ERROR] 路径不存在 {base_path}")
        sys.exit(1)

    # 创建修复器并执行修复
    fixer = Slf4jFixer(base_path)
    success = fixer.fix_all_files()

    # 根据结果退出
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()