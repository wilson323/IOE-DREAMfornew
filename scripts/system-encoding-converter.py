#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import glob
import re
from pathlib import Path

class SystematicEncodingConverter:
    def __init__(self):
        self.fixed_files = 0
        self.error_files = 0

    def convert_file(self, file_path):
        """系统性转换单个文件编码"""
        try:
            # 读取原始文件
            with open(file_path, 'rb') as f:
                raw_content = f.read()

            # 检测并转换编码
            content = None
            encoding_used = None

            # 尝试各种编码
            encodings_to_try = ['utf-8', 'utf-8-sig', 'gbk', 'gb2312', 'big5', 'latin1']

            for encoding in encodings_to_try:
                try:
                    content = raw_content.decode(encoding)
                    encoding_used = encoding
                    break
                except UnicodeDecodeError:
                    continue
                except Exception:
                    continue

            # 如果所有编码都失败，使用utf-8 with errors
            if content is None:
                content = raw_content.decode('utf-8', errors='replace')
                encoding_used = 'utf-8-replace'

            # 系统性修复常见编码问题
            content = self.fix_encoding_issues(content)

            # 写回文件（UTF-8，无BOM）
            with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
                f.write(content)

            self.fixed_files += 1
            return True, encoding_used

        except Exception as e:
            self.error_files += 1
            print(f"转换失败: {file_path} - {e}")
            return False, None

    def fix_encoding_issues(self, content):
        """系统性修复编码问题"""
        # 移除BOM
        content = content.lstrip('\ufeff')

        # 修复常见乱码模式
        encoding_fixes = {
            '????': '中文',
            '???': '中文',
            '涓?': '中',
            '鏂?': '新',
            '锟斤拷': '',
            '乱码': '',
            '鎻愪': '获',
            '搴旂': '取',
            '閮婂': '门',
            '閿?': '错',
            '闂?': '问',
            '锟斤锟斤': '',
        }

        for pattern, replacement in encoding_fixes.items():
            content = content.replace(pattern, replacement)

        return content

    def convert_directory(self, directory, pattern="*.java"):
        """系统性转换目录中的文件"""
        print(f"开始转换目录: {directory}")
        print(f"文件模式: {pattern}")

        file_paths = glob.glob(os.path.join(directory, '**', pattern), recursive=True)

        print(f"找到 {len(file_paths)} 个文件")

        for file_path in file_paths:
            print(f"转换: {file_path}")
            success, encoding = self.convert_file(file_path)
            if success:
                print(f"  ✓ 转换成功 (原始编码: {encoding})")
            else:
                print(f"  ❌ 转换失败")

    def get_statistics(self):
        """获取转换统计信息"""
        return {
            'fixed_files': self.fixed_files,
            'error_files': self.error_files,
            'total_files': self.fixed_files + self.error_files,
            'success_rate': f"{self.fixed_files * 100 / (self.fixed_files + self.error_files):.1f}%" if (self.fixed_files + self.error_files) > 0 else "0%"
        }

if __name__ == "__main__":
    converter = SystematicEncodingConverter()

    # 转换所有Java文件
    converter.convert_directory("smart-admin-api-java17-springboot3", "*.java")

    # 输出统计信息
    stats = converter.get_statistics()
    print("\n转换统计:")
    print(f"成功转换: {stats['fixed_files']}")
    print(f"转换失败: {stats['error_files']}")
    print(f"文件总数: {stats['total_files']}")
    print(f"成功率: {stats['success_rate']}")
