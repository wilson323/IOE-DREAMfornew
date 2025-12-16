#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM Entity设计规范检查脚本

检查项目中的Entity和DAO是否遵循设计规范
- Entity行数检查（≤400行上限）
- Repository违规检查（禁止使用@Repository注解）
- 重复Entity定义检查
"""

import os
import re
import sys
from pathlib import Path
from typing import List, Dict, Tuple, Optional
from datetime import datetime

class EntityStandardsChecker:
    def __init__(self, project_path: str = "."):
        self.project_path = Path(project_path)
        self.results = {
            'large_entities': [],
            'repository_violations': [],
            'duplicate_entities': [],
            'summary': {
                'total_entities': 0,
                'total_daos': 0,
                'huge_entity_count': 0,
                'large_entity_count': 0,
                'repository_violation_count': 0,
                'duplicate_entity_count': 0,
                'compliance_rate': 0
            }
        }

    def find_files(self, pattern: str) -> List[Path]:
        """查找指定模式的文件"""
        files = []
        for file_path in self.project_path.rglob(pattern):
            # 排除构建目录和缓存目录
            if not any(part in str(file_path) for part in ['target', 'build', 'node_modules', '.git']):
                files.append(file_path)
        return files

    def check_entity_standards(self, file_path: Path) -> Optional[Dict]:
        """检查Entity文件规范"""
        try:
            content = file_path.read_text(encoding='utf-8')
            lines = content.split('\n')
            line_count = len(lines)
            relative_path = str(file_path.relative_to(self.project_path)).replace('\\', '/')

            entity_info = {
                'file': str(file_path),
                'relative_path': relative_path,
                'lines': line_count,
                'fields': 0,
                'has_business_logic': False,
                'has_static_methods': False,
                'extends_base_entity': False,
                'has_table_name': False,
                'status': 'Unknown'
            }

            # 检查字段数量
            field_matches = re.findall(r'private\s+\w+\s+\w+;', content)
            entity_info['fields'] = len(field_matches)

            # 检查是否继承BaseEntity
            entity_info['extends_base_entity'] = 'extends BaseEntity' in content

            # 检查@TableName注解
            entity_info['has_table_name'] = '@TableName' in content

            # 检查业务逻辑方法
            method_matches = re.findall(r'public\s+\w+\s+\w+\([^)]*\)\s*\{', content)
            for match in method_matches:
                # 查找方法内容（简化版）
                method_start = content.find(match)
                if method_start != -1:
                    method_content = content[method_start:method_start + 500]
                    if any(keyword in method_content for keyword in ['calculate', 'process', 'validate', 'format', 'parse', 'convert']):
                        entity_info['has_business_logic'] = True
                        break

            # 检查静态方法
            entity_info['has_static_methods'] = 'public static' in content

            # 判断状态
            if line_count > 400:
                entity_info['status'] = '超大Entity'
            elif line_count > 200:
                entity_info['status'] = '大型Entity'
            elif entity_info['has_business_logic'] or entity_info['has_static_methods']:
                entity_info['status'] = '包含业务逻辑'
            elif entity_info['fields'] > 30:
                entity_info['status'] = '字段过多'
            elif not entity_info['extends_base_entity']:
                entity_info['status'] = '未继承BaseEntity'
            elif not entity_info['has_table_name']:
                entity_info['status'] = '缺少@TableName'
            else:
                entity_info['status'] = '符合规范'

            return entity_info

        except Exception as e:
            print(f"⚠️ 检查Entity文件失败: {file_path} - {str(e)}")
            return None

    def check_dao_standards(self, file_path: Path) -> Optional[Dict]:
        """检查DAO文件规范"""
        try:
            content = file_path.read_text(encoding='utf-8')
            relative_path = str(file_path.relative_to(self.project_path)).replace('\\', '/')

            dao_info = {
                'file': str(file_path),
                'relative_path': relative_path,
                'has_repository_annotation': '@Repository' in content,
                'has_mapper_annotation': '@Mapper' in content,
                'extends_base_mapper': 'extends BaseMapper' in content,
                'status': 'Unknown'
            }

            # 判断状态
            if dao_info['has_repository_annotation']:
                dao_info['status'] = '违规使用@Repository'
            elif not dao_info['has_mapper_annotation']:
                dao_info['status'] = '缺少@Mapper注解'
            elif not dao_info['extends_base_mapper']:
                dao_info['status'] = '未继承BaseMapper'
            else:
                dao_info['status'] = '符合规范'

            return dao_info

        except Exception as e:
            print(f"⚠️ 检查DAO文件失败: {file_path} - {str(e)}")
            return None

    def find_duplicate_entities(self, entity_files: List[Path]) -> List[Dict]:
        """查找重复Entity定义"""
        entity_names = {}
        duplicates = []

        for file_path in entity_files:
            try:
                content = file_path.read_text(encoding='utf-8')
                match = re.search(r'class\s+(\w+Entity)\s+extends', content)
                if match:
                    entity_name = match.group(1)
                    relative_path = str(file_path.relative_to(self.project_path)).replace('\\', '/')

                    if entity_name in entity_names:
                        duplicates.append({
                            'entity_name': entity_name,
                            'files': [entity_names[entity_name], relative_path]
                        })
                    else:
                        entity_names[entity_name] = relative_path
            except Exception as e:
                print(f"⚠️ 解析Entity文件失败: {file_path} - {str(e)}")

        return duplicates

    def fix_repository_violations(self, violations: List[Dict]) -> int:
        """修复Repository违规"""
        fixed_count = 0

        for violation in violations:
            try:
                file_path = Path(violation['file'])
                content = file_path.read_text(encoding='utf-8')

                if '@Repository' in content:
                    content = content.replace('@Repository', '@Mapper')
                    file_path.write_text(content, encoding='utf-8')
                    print(f"  ✅ 修复: {violation['relative_path']}")
                    fixed_count += 1
            except Exception as e:
                print(f"  ❌ 修复失败: {violation['file']} - {str(e)}")

        return fixed_count

    def run_check(self, fix_violations: bool = False, detailed: bool = False) -> Dict:
        """执行检查"""
        print("🔍 IOE-DREAM Entity设计规范检查")
        print("=" * 40)
        print(f"项目路径: {self.project_path}")
        print(f"自动修复: {fix_violations}")
        print(f"详细输出: {detailed}")
        print()

        # 查找文件
        print("📁 扫描项目文件...")
        entity_files = self.find_files("*Entity.java")
        dao_files = self.find_files("*Dao.java")

        self.results['summary']['total_entities'] = len(entity_files)
        self.results['summary']['total_daos'] = len(dao_files)

        print(f"发现Entity文件: {len(entity_files)}个")
        print(f"发现DAO文件: {len(dao_files)}个")
        print()

        # 检查Entity
        print("🔍 检查Entity设计规范...")
        for file_path in entity_files:
            entity_info = self.check_entity_standards(file_path)
            if entity_info:
                if entity_info['status'] != '符合规范':
                    if entity_info['status'] == '超大Entity':
                        self.results['summary']['huge_entity_count'] += 1
                    elif entity_info['status'] == '大型Entity':
                        self.results['summary']['large_entity_count'] += 1
                    self.results['large_entities'].append(entity_info)

                if detailed:
                    status_symbol = "✅" if entity_info['status'] == '符合规范' else "⚠️"
                    print(f"  {status_symbol} {entity_info['status']}: {entity_info['relative_path']} ({entity_info['lines']}行, {entity_info['fields']}字段)")

        # 检查DAO
        print("\n🔍 检查DAO设计规范...")
        for file_path in dao_files:
            dao_info = self.check_dao_standards(file_path)
            if dao_info:
                if dao_info['status'] != '符合规范':
                    self.results['summary']['repository_violation_count'] += 1
                    self.results['repository_violations'].append(dao_info)

                if detailed:
                    status_symbol = "✅" if dao_info['status'] == '符合规范' else "❌"
                    print(f"  {status_symbol} {dao_info['status']}: {dao_info['relative_path']}")

        # 检查重复Entity
        print("\n🔍 检查重复Entity定义...")
        self.results['duplicate_entities'] = self.find_duplicate_entities(entity_files)
        self.results['summary']['duplicate_entity_count'] = len(self.results['duplicate_entities'])

        if self.results['duplicate_entities']:
            print("  发现重复Entity:")
            for duplicate in self.results['duplicate_entities']:
                print(f"    {duplicate['entity_name']}:")
                for file_path in duplicate['files']:
                    print(f"      - {file_path}")
        else:
            print("  ✅ 无重复Entity定义")

        # 计算合规率
        total_issues = (self.results['summary']['huge_entity_count'] +
                       self.results['summary']['large_entity_count'] +
                       self.results['summary']['repository_violation_count'] +
                       self.results['summary']['duplicate_entity_count'])
        total_checks = self.results['summary']['total_entities'] + self.results['summary']['total_daos']
        self.results['summary']['compliance_rate'] = round(((total_checks - total_issues) / total_checks) * 100, 2)

        # 输出结果
        self.print_results()

        # 自动修复
        if fix_violations and self.results['summary']['repository_violation_count'] > 0:
            print(f"\n🔧 开始自动修复Repository违规...")
            fixed_count = self.fix_repository_violations(self.results['repository_violations'])
            if fixed_count > 0:
                print(f"✅ 自动修复完成，修复了 {fixed_count} 个文件")
                self.results['summary']['repository_violation_count'] -= fixed_count

        return self.results

    def print_results(self):
        """打印检查结果"""
        print(f"\n📊 Entity规范检查报告")
        print("=" * 40)

        print(f"\n📈 总体统计:")
        print(f"  Entity文件总数: {self.results['summary']['total_entities']}")
        print(f"  DAO文件总数: {self.results['summary']['total_daos']}")
        print(f"  超大Entity数量: {self.results['summary']['huge_entity_count']}")
        print(f"  大型Entity数量: {self.results['summary']['large_entity_count']}")
        print(f"  Repository违规数量: {self.results['summary']['repository_violation_count']}")
        print(f"  重复Entity数量: {self.results['summary']['duplicate_entity_count']}")

        compliance_rate = self.results['summary']['compliance_rate']
        rate_color = "✅ 优秀" if compliance_rate >= 95 else "⚠️ 良好" if compliance_rate >= 80 else "❌ 需改进"
        print(f"  合规率: {compliance_rate}% {rate_color}")

        # 输出问题详情
        if self.results['summary']['huge_entity_count'] > 0:
            print(f"\n🚨 P0级问题 - 超大Entity:")
            for entity in self.results['large_entities']:
                if entity['status'] == '超大Entity':
                    print(f"  ❌ {entity['relative_path']} - {entity['lines']}行 (必须拆分)")

        if self.results['summary']['repository_violation_count'] > 0:
            print(f"\n🚨 P0级问题 - Repository违规:")
            for violation in self.results['repository_violations']:
                print(f"  ❌ {violation['relative_path']} - 必须使用@Mapper注解")

        if self.results['summary']['large_entity_count'] > 0:
            print(f"\n⚠️ P1级问题 - 大型Entity:")
            for entity in self.results['large_entities']:
                if entity['status'] == '大型Entity':
                    print(f"  ⚠️ {entity['relative_path']} - {entity['lines']}行 (建议优化)")

        # 最终状态
        print(f"\n🎯 检查完成！")
        if compliance_rate >= 95:
            print("✅ 优秀！Entity设计完全符合规范")
        elif compliance_rate >= 80:
            print("⚠️ 良好！大部分Entity符合规范，建议优化剩余问题")
        else:
            print("❌ 需要改进！存在较多规范问题，请及时修复")

        if self.results['summary']['huge_entity_count'] > 0 or self.results['summary']['repository_violation_count'] > 0:
            print(f"\n🚨 发现严重问题，建议立即修复")
            print(f"   运行修复命令: python scripts/check_entity_standards.py --fix")

def main():
    import argparse

    parser = argparse.ArgumentParser(description='IOE-DREAM Entity设计规范检查脚本')
    parser.add_argument('--path', default='.', help='项目根路径')
    parser.add_argument('--fix', action='store_true', help='自动修复Repository违规')
    parser.add_argument('--detailed', action='store_true', help='输出详细检查信息')
    parser.add_argument('--report', help='报告输出路径')

    args = parser.parse_args()

    checker = EntityStandardsChecker(args.path)
    results = checker.run_check(fix_violations=args.fix, detailed=args.detailed)

    if args.report:
        # 生成报告文件
        report_content = generate_markdown_report(results, args.path)
        try:
            Path(args.report).write_text(report_content, encoding='utf-8')
            print(f"\n📄 详细报告已保存到: {args.report}")
        except Exception as e:
            print(f"⚠️ 保存报告失败: {str(e)}")

def generate_markdown_report(results: Dict, project_path: str) -> str:
    """生成Markdown格式报告"""
    summary = results['summary']

    report = f"""# IOE-DREAM Entity设计规范检查报告

> **生成时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
> **检查路径**: {project_path}
> **脚本版本**: v1.0.0

## 📊 检查概览

| 指标 | 数量 | 说明 |
|------|------|------|
| Entity文件总数 | {summary['total_entities']} | 项目中所有Entity类 |
| DAO文件总数 | {summary['total_daos']} | 数据访问层接口 |
| 超大Entity(>400行) | {summary['huge_entity_count']} | 🔴 严重违规 |
| 大型Entity(200-400行) | {summary['large_entity_count']} | 🟡 需要优化 |
| Repository违规 | {summary['repository_violation_count']} | 🔴 严重违规 |
| 重复Entity定义 | {summary['duplicate_entity_count']} | 🟡 需要清理 |
| **合规率** | **{summary['compliance_rate']}%** | {'✅ 优秀' if summary['compliance_rate'] >= 95 else '⚠️ 良好' if summary['compliance_rate'] >= 80 else '❌ 需改进'}

## 🚨 严重问题 (P0级)

### 超大Entity文件

"""

    if summary['huge_entity_count'] > 0:
        huge_entities = [e for e in results['large_entities'] if e['status'] == '超大Entity']
        for entity in huge_entities:
            report += f"- **{entity['relative_path']}** - {entity['lines']}行\n"
    else:
        report += "✅ 无超大Entity文件\n"

    report += "\n### Repository注解违规\n\n"

    if summary['repository_violation_count'] > 0:
        for violation in results['repository_violations']:
            report += f"- **{violation['relative_path']}** - 使用了@Repository注解\n"
    else:
        report += "✅ 无Repository违规\n"

    report += "\n## ⚠️ 一般问题 (P1级)\n\n"

    # 继续生成报告其余部分...
    return report

if __name__ == '__main__':
    main()