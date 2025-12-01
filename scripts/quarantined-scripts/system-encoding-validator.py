#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import glob
import re

class SystematicEncodingValidator:
    def __init__(self):
        self.total_files = 0
        self.valid_files = 0
        self.invalid_files = 0
        self.issues_found = []

    def validate_file(self, file_path):
        """系统性验证单个文件编码"""
        issues = []

        try:
            # 检查文件编码
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            # 检查BOM
            if content.startswith('\ufeff'):
                issues.append("包含BOM标记")

            # 检查换行符
            if '\r\n' in content:
                issues.append("包含CRLF换行符")

            # 检查乱码模式
            garbage_patterns = ['????', '???', '涓?', '鏂?', '锟斤拷', '乱码']
            for pattern in garbage_patterns:
                if pattern in content:
                    issues.append(f"包含乱码: {pattern}")

            # 检查编码一致性
            try:
                content.encode('ascii')
            except UnicodeEncodeError:
                # 这是正常的，因为包含中文字符
                pass

            return len(issues) == 0, issues

        except Exception as e:
            return False, [f"验证失败: {str(e)}"]

    def validate_directory(self, directory, pattern="*.java"):
        """系统性验证目录中的文件"""
        print(f"开始验证目录: {directory}")

        file_paths = glob.glob(os.path.join(directory, '**', pattern), recursive=True)
        self.total_files = len(file_paths)

        print(f"找到 {len(file_paths)} 个文件")

        for file_path in file_paths:
            is_valid, issues = self.validate_file(file_path)
            if is_valid:
                self.valid_files += 1
            else:
                self.invalid_files += 1
                self.issues_found.append({
                    'file': file_path,
                    'issues': issues
                })
                print(f"❌ 验证失败: {file_path}")
                for issue in issues:
                    print(f"    - {issue}")

    def generate_report(self):
        """生成验证报告"""
        print(f"\n系统性编码验证报告")
        print("=" * 50)
        print(f"总文件数: {self.total_files}")
        print(f"验证通过: {self.valid_files}")
        print(f"验证失败: {self.invalid_files}")
        print(f"验证通过率: {self.valid_files * 100 / self.total_files:.1f}%" if self.total_files > 0 else "0%")

        if self.invalid_files > 0:
            print(f"\n问题文件详情:")
            for item in self.issues_found:
                print(f"\n文件: {item['file']}")
                for issue in item['issues']:
                    print(f"  - {issue}")

        return {
            'total_files': self.total_files,
            'valid_files': self.valid_files,
            'invalid_files': self.invalid_files,
            'issues_count': len(self.issues_found),
            'success_rate': f"{self.valid_files * 100 / self.total_files:.1f}%" if self.total_files > 0 else "0%"
        }

if __name__ == "__main__":
    validator = SystematicEncodingValidator()

    # 验证所有Java文件
    validator.validate_directory("smart-admin-api-java17-springboot3", "*.java")

    # 生成报告
    report = validator.generate_report()

    # 输出结论
    if report['invalid_files'] == 0:
        print("\n✅ 所有文件编码验证通过！")
        exit(0)
    else:
        print(f"\n❌ 编码验证失败！发现 {report['invalid_files']} 个问题文件")
        exit(1)
