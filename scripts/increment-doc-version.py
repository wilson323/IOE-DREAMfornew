#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM项目文档版本递增工具

自动递增文档版本号并更新相关元数据

作者: SmartAdmin Team
版本: v1.0.0
创建时间: 2025-01-13
"""

import os
import sys
import re
import argparse
from pathlib import Path
from datetime import datetime
from typing import Optional, Tuple

class DocumentVersionIncrementer:
    """文档版本递增器"""

    def __init__(self, docs_dir: str):
        self.docs_dir = Path(docs_dir)
        self.author = self.get_git_author()
        self.current_date = datetime.now().strftime('%Y-%m-%d')

    def get_git_author(self) -> str:
        """获取Git配置的作者信息"""
        try:
            import subprocess
            result = subprocess.run(
                ['git', 'config', 'user.name'],
                capture_output=True,
                text=True,
                timeout=5
            )
            if result.returncode == 0:
                return result.stdout.strip()
        except Exception:
            pass

        # 获取系统用户名
        import getpass
        return getpass.getuser()

    def extract_version_from_file(self, file_path: Path) -> Optional[str]:
        """从文件中提取当前版本号"""
        try:
            content = file_path.read_text(encoding='utf-8')

            # 多种版本号格式匹配
            version_patterns = [
                r'文档版本:\s*v?(\d+\.\d+\.\d+)(?:\s*[\-_]\s*[a-zA-Z0-9]+)?',
                r'version[:\s]*["\']v?(\d+\.\d+\.\d+)["\']',
                r'@version\s+(\d+\.\d+\.\d+)',
                r'VE[RS]ION["\']\s*v?(\d+\.\d+\.\d+)["\']',
                r'Ve[rs]ion\s*v?(\d+\.\d+\.\d+)'
            ]

            for pattern in version_patterns:
                match = re.search(pattern, content, re.IGNORECASE | re.MULTILINE)
                if match:
                    return match.group(1)

        except Exception as e:
            print(f"读取文件失败: {file_path} - {str(e)}")

        return None

    def parse_version(self, version_str: str) -> Tuple[int, int, int]:
        """解析版本号字符串"""
        try:
            parts = version_str.split('.')
            if len(parts) == 3:
                return int(parts[0]), int(parts[1]), int(parts[2])
        except (ValueError, IndexError):
            print(f"无效的版本号格式: {version_str}")
            raise

    def format_version(self, major: int, minor: int, patch: int) -> str:
        """格式化版本号"""
        return f"{major}.{minor}.{patch}"

    def get_next_version(self, current_version: str, increment_type: str = 'patch') -> str:
        """获取下一个版本号"""
        try:
            major, minor, patch = self.parse_version(current_version)

            if increment_type == 'major':
                major += 1
                minor = 0
                patch = 0
            elif increment_type == 'minor':
                minor += 1
                patch = 0
            elif increment_type == 'patch':
                patch += 1
            else:
                raise ValueError(f"无效的递增类型: {increment_type}")

            return self.format_version(major, minor, patch)

        except ValueError as e:
            print(f"版本号解析失败: {str(e)}")
            raise

    def update_version_in_file(self, file_path: Path, old_version: str, new_version: str) -> bool:
        """更新文件中的版本号"""
        try:
            content = file_path.read_text(encoding='utf-8')
            original_content = content

            # 更新各种版本的版本号
            content = self.update_version_patterns(content, old_version, new_version)

            # 如果内容有变化，保存文件
            if content != original_content:
                file_path.write_text(content, encoding='utf-8')
                return True

        except Exception as e:
            print(f"更新文件失败: {file_path} - {str(e)}")

        return False

    def update_version_patterns(self, content: str, old_version: str, new_version: str) -> str:
        """更新内容中的版本号模式"""
        # 版本号替换模式
        patterns = [
            # 标准格式
            (r'文档版本:\s*v?' + re.escape(old_version), r'文档版本: v' + new_version),
            (r'version[:\s]*["\']v?' + re.escape(old_version) + '["\']', r'version: "' + new_version + '"'),
            (r'@version\s+' + re.escape(old_version), r'@version ' + new_version),
            (r'VE[RS]ION["\']\s*v?' + re.escape(old_version) + '["\']', r'VE[RS]ION "' + new_version + '"'),
            (r'Ve[rs]ion\s*v?' + re.escape(old_version), r'Ve[rs]ion v' + new_version),

            # 带后缀的版本号（v1.0.0-alpha, v1.0.0-beta等）
            (r'文档版本:\s*v?' + re.escape(old_version) + r'[\-_][a-zA-Z0-9]+',
             lambda m: f'文档版本: v{new_version}' + m.group(0).split(old_version)[-1]),

            # 代码中的版本号（如Java常量）
            (r'DOCUMENT_VERSION\s*=\s*["\']v?' + re.escape(old_version) + '["\']',
             r'DOCUMENT_VERSION = "' + new_version + '"'),
        ]

        updated_content = content
        for pattern, replacement in patterns:
            if callable(replacement):
                updated_content = re.sub(pattern, replacement, updated_content, flags=re.IGNORECASE)
            else:
                updated_content = re.sub(pattern, replacement, updated_content, flags=re.IGNORECASE)

        return updated_content

    def update_change_history(self, file_path: Path, old_version: str, new_version: str,
                             increment_type: str, reason: str) -> bool:
        """更新文档变更历史"""
        try:
            content = file_path.read_text(encoding='utf-8')

            # 查找变更历史部分
            history_section_pattern = r'(## 变更历史[\s\S]*)(\n[\s]*\|[\s]*版本[\s]*\|)'
            history_match = re.search(history_section_pattern, content)

            if not history_match:
                # 如果没有变更历史部分，创建一个
                new_history = f"""
{history_match.group(1) if history_match else ''}## 变更历史

| 版本 | 日期 | 变更内容 | 变更人 | 审批人 | 变更类型 |
|------|------|----------|--------|--------|----------|
| {new_version} | {self.current_date} | {reason} | {self.author} |  | {increment_type.upper()} |

{content[history_match.end():] if history_match else content}"""
            else:
                # 插入新的变更记录到表格顶部
                table_start = history_match.end()

                new_record = f"| {new_version} | {self.current_date} | {reason} | {self.author} |  | {increment_type.upper()} |"

                # 在表格开始后插入新记录
                updated_content = (
                    content[:table_start] +
                    new_record + "\n" +
                    content[table_start:]
                )

                file_path.write_text(updated_content, encoding='utf-8')

            return True

        except Exception as e:
            print(f"更新变更历史失败: {file_path} - {str(e)}")

        return False

    def add_version_metadata(self, file_path: Path, old_version: str, new_version: str,
                           increment_type: str, reason: str) -> bool:
        """添加版本元数据到文件头部"""
        try:
            content = file_path.read_text(encoding='utf-8')

            # 查找文档信息部分
            info_pattern = r'(## 文档信息[\s\S]*)([\s\S]*?)(\n##)'
            info_match = re.search(info_pattern, content)

            if not info_match:
                # 如果没有文档信息部分，创建一个
                new_info = f"""## 文档信息
- **文档版本**: {new_version}
- **创建时间**: {self.current_date}
- **最后更新**: {self.current_date}
- **作者**: {self.author}
- **审批状态**: [稳定]
- **关联代码版本**: 待更新
- **变更原因**: {reason}

{content}"""
                file_path.write_text(new_info, encoding='utf-8')
            else:
                # 更新现有的文档信息
                info_content = info_match.group(2)
                updated_info = self.update_info_metadata(info_content, new_version, increment_type, reason)

                updated_content = (
                    content[:info_match.start(2)] +
                    updated_info +
                    content[info_match.end():]
                )

                file_path.write_text(updated_content, encoding='utf-8')

            return True

        except Exception as e:
            print(f"更新文档信息失败: {file_path} - {str(e)}")

        return False

    def update_info_metadata(self, info_content: str, new_version: str, increment_type: str, reason: str) -> str:
        """更新文档信息元数据"""
        updated_info = info_content

        # 更新版本号
        version_pattern = r'(\* \*\*文档版本\*\*:\s*)(v?\d+\.\d+\.\d+)'
        version_match = re.search(version_pattern, updated_info)
        if version_match:
            updated_info = re.sub(
                version_pattern,
                f'\\1v{new_version}',
                updated_info
            )
        else:
            # 如果没有版本号，添加一个
            updated_info += f"\n- **文档版本**: v{new_version}"

        # 更新最后更新时间
        updated_info = re.sub(
            r'(\* \*\*最后更新\*\*:\s*)([\d-]+\s[\d:]+)',
            f'\\1{self.current_date}',
            updated_info
        )

        # 更新作者
        updated_info = re.sub(
            r'(\* \*\*作者\*\*:\s*)([^\\n]+)',
            f'\\1{self.author}',
            updated_info
        )

        return updated_info

    def increment_file_version(self, file_path: Path, increment_type: str = 'patch',
                              reason: str = None) -> Optional[Tuple[str, str]]:
        """递增单个文件的版本号"""
        # 检查文件是否存在
        if not file_path.exists():
            print(f"文件不存在: {file_path}")
            return None

        # 提取当前版本
        old_version = self.extract_version_from_file(file_path)
        if not old_version:
            print(f"未找到版本号: {file_path}")
            return None

        print(f"当前版本: {old_version} → {file_path}")

        # 获取新版本号
        try:
            new_version = self.get_next_version(old_version, increment_type)
        except ValueError as e:
            print(f"版本号生成失败: {str(e)}")
            return None

        # 设置默认原因
        if not reason:
            reason = f"版本递增({increment_type}): {old_version} → {new_version}"

        print(f"递增版本: {old_version} → {new_version}")

        # 创建备份
        backup_path = file_path.with_suffix(f'.backup.{datetime.now().strftime("%Y%m%d_%H%M%S")}')
        file_path.rename(backup_path)

        try:
            # 从备份恢复文件
            backup_path.rename(file_path)

            # 更新版本号
            if not self.update_version_in_file(file_path, old_version, new_version):
                print(f"版本号更新失败: {file_path}")
                return (old_version, new_version)

            # 更新变更历史
            self.update_change_history(file_path, old_version, new_version, increment_type, reason)

            # 更新文档信息
            self.add_version_metadata(file_path, old_version, new_version, increment_type, reason)

            print(f"  ✓ 版本递增成功: {new_version}")

            # 删除备份文件
            try:
                backup_path.unlink()
            except:
                pass

            return (old_version, new_version)

        except Exception as e:
            print(f"版本递增失败: {file_path} - {str(e)}")
            # 恢复备份文件
            try:
                file_path.unlink()
                backup_path.rename(file_path)
            except:
                pass

            return (old_version, None)

    def increment_directory_versions(self, dir_path: str, file_pattern: str = "*.md",
                                  increment_type: str = 'patch', reason: str = None):
        """递增目录中所有文件的版本号"""
        directory = Path(dir_path)

        if not directory.exists():
            print(f"目录不存在: {dir_path}")
            return

        # 查找匹配的文件
        files = list(directory.glob(file_pattern))

        if not files:
            print(f"未找到匹配的文件: {dir_path}/{file_pattern}")
            return

        print(f"开始递增目录版本: {dir_path}")
        print(f"文件模式: {file_pattern}")
        print(f"递增类型: {increment_type}")

        results = []

        for file_path in files:
            print(f"\n处理文件: {file_path.relative_to(self.project_root)}")
            result = self.increment_file_version(file_path, increment_type, reason)
            if result:
                results.append(result)

        # 输出结果摘要
        print(f"\n{'='*60}")
        print("📊 版本递增结果摘要:")
        print(f"{'='*60}")

        success_count = sum(1 for _, new in results if new is not None)
        total_count = len(results)

        if success_count > 0:
            print(f"✅ 成功递增: {success_count}/{total_count} 个文件")

            print("\n变更详情:")
            for i, (old_version, new_version) in enumerate(results, 1):
                if new_version:
                    print(f"  {i}. {old_version} → {new_version}")
        else:
            print(f"❌ 递增失败: 0/{total_count} 个文件")

        print(f"{'='*60}")

    def batch_increment_with_config(self, config_file: str):
        """根据配置文件批量递增版本"""
        try:
            import json
            with open(config_file, 'r', encoding='utf-8') as f:
                config = json.load(f)

            print(f"使用配置文件: {config_file}")

            # 处理文件列表
            if 'files' in config:
                for file_info in config['files']:
                    file_path = Path(file_info['path'])
                    increment_type = file_info.get('increment_type', 'patch')
                    reason = file_info.get('reason', f"批量版本递增({increment_type})")

                    result = self.increment_file_version(file_path, increment_type, reason)
                    if result:
                        old_version, new_version = result
                        print(f"✅ {file_path}: {old_version} → {new_version}")

            # 处理目录批量递增
            if 'directories' in config:
                for dir_info in config['directories']:
                    dir_path = dir_info['path']
                    file_pattern = dir_info.get('pattern', '*.md')
                    increment_type = dir_info.get('increment_type', 'patch')
                    reason = dir_info.get('reason', f"批量版本递增({increment_type})")

                    self.increment_directory_versions(dir_path, file_pattern, increment_type, reason)

        except Exception as e:
            print(f"配置文件处理失败: {str(e)}")


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='IOE-DREAM文档版本递增工具')
    parser.add_argument('target', help='目标文件或目录路径')
    parser.add_argument('-t', '--type', choices=['major', 'minor', 'patch'],
                        default='patch', help='递增类型 (默认: patch)')
    parser.add_argument('-r', '--reason', help='变更原因')
    parser.add_argument('-p', '--pattern', default='*.md',
                        help='文件模式 (默认: *.md)')
    parser.add_argument('-c', '--config', help='配置文件路径')

    args = parser.parse_args()

    target_path = Path(args.target)

    # 如果指定了配置文件，使用配置文件
    if args.config:
        incrementer = DocumentVersionIncrementer(Path.cwd())
        incrementer.batch_increment_with_config(args.config)
        return

    # 确定项目根目录
    project_root = Path.cwd()

    # 处理相对路径
    if not target_path.is_absolute():
        target_path = project_root / target_path

    incrementer = DocumentVersionIncrementer(project_root)

    if target_path.is_file():
        # 单个文件
        result = incrementer.increment_file_version(target_path, args.type, args.reason)
        if result:
            old_version, new_version = result
            if new_version:
                print(f"\n✅ 版本递增成功: {old_version} → {new_version}")
            else:
                print(f"\n❌ 版本递增失败")
                sys.exit(1)
        else:
            print(f"\n❌ 文件处理失败")
            sys.exit(1)

    elif target_path.is_dir():
        # 目录
        incrementer.increment_directory_versions(str(target_path), args.pattern, args.type, args.reason)
    else:
        print(f"❌ 无效的目标路径: {args.target}")
        sys.exit(1)


if __name__ == "__main__":
    main()