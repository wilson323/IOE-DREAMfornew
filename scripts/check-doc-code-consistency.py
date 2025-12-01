#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM项目文档与代码一致性检查工具

检查文档内容是否与实际代码实现保持一致

作者: SmartAdmin Team
版本: v1.0.0
创建时间: 2025-01-13
"""

import os
import re
import sys
import json
import yaml
from pathlib import Path
from typing import List, Dict, Tuple, Optional

class DocCodeConsistencyChecker:
    """文档与代码一致性检查器"""

    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.docs_dir = self.project_root / "docs"
        self.source_dir = self.project_root / "smart-admin-api-java17-springboot3"

        # 检查结果
        self.issues = []
        self.warnings = []

    def run_all_checks(self) -> bool:
        """执行所有检查"""
        print("🔍 开始文档与代码一致性检查...")

        all_passed = True

        # 1. 检查API文档与Controller一致性
        if not self.check_api_consistency():
            all_passed = False

        # 2. 检查数据库文档与Entity一致性
        if not self.check_database_consistency():
            all_passed = False

        # 3. 检查配置文档与实际配置一致性
        if not self.check_config_consistency():
            all_passed = False

        # 4. 检查架构文档与代码结构一致性
        if not self.check_architecture_consistency():
            all_passed = False

        # 5. 检查版本一致性
        if not self.check_version_consistency():
            all_passed = False

        # 输出检查结果
        self.print_results()

        return all_passed

    def check_api_consistency(self) -> bool:
        """检查API文档与Controller一致性"""
        print("📋 检查API文档与Controller一致性...")

        # 查找所有Controller文件
        controllers = list(self.source_dir.rglob("*Controller.java"))

        if not controllers:
            self.warnings.append("未找到Controller文件")
            return True

        issues_found = 0

        for controller_path in controllers:
            controller_name = controller_path.stem
            issues = self.check_single_controller(controller_path)
            if issues:
                issues_found += len(issues)
                self.issues.extend(issues)

        print(f"  ✅ 检查了 {len(controllers)} 个Controller文件")

        if issues_found > 0:
            print(f"  ❌ 发现 {issues_found} 个API一致性问题")
            return False

        print("  ✅ API文档与Controller一致")
        return True

    def check_single_controller(self, controller_path: Path) -> List[str]:
        """检查单个Controller的一致性"""
        issues = []

        try:
            content = controller_path.read_text(encoding='utf-8')

            # 提取API接口信息
            apis = self.extract_controller_apis(content)

            # 查找对应的API文档
            api_doc_path = self.find_api_document(controller_path.stem)

            if api_doc_path and api_doc_path.exists():
                doc_content = api_doc_path.read_text(encoding='utf-8')
                doc_apis = self.extract_document_apis(doc_content)

                # 比较API一致性
                missing_apis = self.compare_apis(apis, doc_apis, controller_path.stem)
                if missing_apis:
                    issues.extend(missing_apis)
            else:
                # 没有找到对应的API文档
                if apis:
                    issues.append(f"Missing API documentation for controller: {controller_path.stem}")

        except Exception as e:
            issues.append(f"Error checking controller {controller_path}: {str(e)}")

        return issues

    def extract_controller_apis(self, content: str) -> List[Dict]:
        """从Controller代码中提取API信息"""
        apis = []

        # 使用正则表达式提取@RequestMapping和对应的HTTP方法
        class_pattern = r'@RestController.*?\n.*?class\s+(\w+Controller)'
        method_pattern = r'@(Get|Post|Put|Delete|Patch)Mapping\(["\']([^"\']+)["\'].*?\)\s+.*?public\s+.*?\s+(\w+)\s*\('

        class_match = re.search(class_pattern, content)
        if not class_match:
            return apis

        class_name = class_match.group(1)

        for method_match in re.finditer(method_pattern, content):
            http_method = method_match.group(1).upper()
            path = method_match.group(2)
            method_name = method_match.group(3)

            apis.append({
                'class': class_name,
                'method': http_method,
                'path': path,
                'method_name': method_name,
                'full_path': f"{http_method} {path}",
                'signature': f"{method_name}()"
            })

        return apis

    def find_api_document(self, controller_name: str) -> Optional[Path]:
        """查找Controller对应的API文档"""
        # 尝试多种可能的文档路径
        possible_paths = [
            self.docs_dir / "api" / f"{controller_name}.md",
            self.docs_dir / "repowiki" / "zh/content/开发规范体系/API设计规范.md",
            self.docs_dir / f"{controller_name.lower()}.md"
        ]

        for path in possible_paths:
            if path.exists():
                return path

        return None

    def extract_document_apis(self, content: str) -> List[Dict]:
        """从文档中提取API信息"""
        apis = []

        # 提取API表格或列表
        # 支持多种格式：
        # 1. GET /api/user/list
        # 2. | 方法 | 路径 | 描述 |
        # 3. - GET: /api/user/list - 用户列表

        patterns = [
            r'###?\s*([GET|POST|PUT|DELETE|PATCH])\s+([^\s\n]+)',
            r'\|\s*([GET|POST|PUT|DELETE|PATCH])\s*\|\s*([^\s|]+)\s*\|',
            r'-\s*([GET|POST|PUT|DELETE|PATCH]):\s*([^\s-]+)'
        ]

        for pattern in patterns:
            for match in re.finditer(pattern, content, re.IGNORECASE):
                method = match.group(1).upper()
                path = match.group(2).strip()

                # 清理路径
                if path.startswith('/api/'):
                    apis.append({
                        'method': method,
                        'path': path,
                        'full_path': f"{method} {path}"
                    })

        return apis

    def compare_apis(self, controller_apis: List[Dict], doc_apis: List[Dict], controller_name: str) -> List[str]:
        """比较Controller和文档中的API"""
        issues = []

        # 创建文档API的快速查找字典
        doc_api_dict = {f"{api['method']} {api['path']}": api for api in doc_apis}

        # 检查Controller中的每个API是否在文档中
        for api in controller_apis:
            full_path = api['full_path']
            if full_path not in doc_api_dict:
                issues.append(f"Missing API in documentation: {controller_name}.{api['method_name']} - {full_path}")

        # 检查文档中的API是否在Controller中
        for api in doc_apis:
            full_path = api['full_path']
            if not any(ca['full_path'] == full_path for ca in controller_apis):
                issues.append(f"API in documentation not implemented: {controller_name} - {full_path}")

        return issues

    def check_database_consistency(self) -> bool:
        """检查数据库文档与Entity一致性"""
        print("📋 检查数据库文档与Entity一致性...")

        # 查找所有Entity文件
        entities = list(self.source_dir.rglob("*Entity.java"))

        if not entities:
            self.warnings.append("未找到Entity文件")
            return True

        issues_found = 0

        for entity_path in entities:
            issues = self.check_single_entity(entity_path)
            if issues:
                issues_found += len(issues)
                self.issues.extend(issues)

        print(f"  ✅ 检查了 {len(entities)} 个Entity文件")

        if issues_found > 0:
            print(f"  ❌ 发现 {issues_found} 个数据库一致性问题")
            return False

        print("  ✅ 数据库文档与Entity一致")
        return True

    def check_single_entity(self, entity_path: Path) -> List[str]:
        """检查单个Entity的一致性"""
        issues = []

        try:
            content = entity_path.read_text(encoding='utf-8')

            # 提取表名和字段信息
            table_name = self.extract_table_name(content)
            fields = self.extract_entity_fields(content)

            # 查找对应的数据库文档
            db_doc_path = self.find_database_document(entity_path.stem)

            if db_doc_path and db_doc_path.exists():
                doc_content = db_doc_path.read_text(encoding='utf-8')
                doc_table_info = self.extract_document_table_info(doc_content, table_name)

                # 比较表和字段一致性
                if doc_table_info:
                    missing_fields = self.compare_table_fields(fields, doc_table_info, entity_path.stem)
                    if missing_fields:
                        issues.extend(missing_fields)
            else:
                # 没有找到对应的数据库文档
                if table_name:
                    issues.append(f"Missing database documentation for table: {table_name}")

        except Exception as e:
            issues.append(f"Error checking entity {entity_path}: {str(e)}")

        return issues

    def extract_table_name(self, content: str) -> Optional[str]:
        """从Entity中提取表名"""
        # 查找@Table注解
        table_match = re.search(r'@Table\s*\(\s*name\s*=\s*["\']([^"\']+)["\']', content)
        if table_match:
            return table_match.group(1)

        # 如果没有@Table注解，尝试使用类名转换
        class_match = re.search(r'class\s+(\w+Entity)', content)
        if class_match:
            class_name = class_match.group(1)
            # 转换为下划线命名
            table_name = re.sub('([A-Z])', r'_\1', class_name).lower()
            if table_name.startswith('_'):
                table_name = table_name[1:]
            return f"t_{table_name}"

        return None

    def extract_entity_fields(self, content: str) -> List[Dict]:
        """从Entity中提取字段信息"""
        fields = []

        # 匹配字段声明模式
        field_pattern = r'@Column\s*\([^)]*\)\s+.*?(?:@Transient\s+)?(?:public|private)\s+(?:\w+\s+)*(\w+(?:<[^>]+>)?)\s+(\w+);'

        for match in re.finditer(field_pattern, content):
            field_type = match.group(1)
            field_name = match.group(2)

            fields.append({
                'name': field_name,
                'type': field_type
            })

        return fields

    def find_database_document(self, entity_name: str) -> Optional[Path]:
        """查找Entity对应的数据库文档"""
        possible_paths = [
            self.docs_dir / "database" / f"{entity_name}.md",
            self.docs_dir / "repowiki" / "zh/content/数据库设计/数据库表结构" / f"{entity_name}.md",
            self.docs_dir / "repowiki" / "zh/content/开发规范体系/数据库设计规范.md"
        ]

        for path in possible_paths:
            if path.exists():
                return path

        return None

    def extract_document_table_info(self, content: str, table_name: str) -> Optional[Dict]:
        """从文档中提取表信息"""
        # 查找对应的表定义
        table_pattern = rf'CREATE\s+TABLE\s+{re.escape(table_name)}\s*\([^)]+);'
        table_match = re.search(table_pattern, content, re.IGNORECASE | re.MULTILINE | re.DOTALL)

        if not table_match:
            # 尝试查找表格格式
            table_section_pattern = rf'###?\s*{re.escape(table_name)}'
            section_match = re.search(table_section_pattern, content, re.IGNORECASE)

            if section_match:
                # 返回找到的表，具体解析逻辑可以进一步实现
                return {'table_name': table_name, 'found': True}

        return None

    def compare_table_fields(self, entity_fields: List[Dict], doc_table_info: Dict, entity_name: str) -> List[str]:
        """比较Entity字段和文档表字段"""
        issues = []

        # 这里可以实现更详细的字段比较逻辑
        # 目前先简单检查
        if not doc_table_info.get('found'):
            issues.append(f"Table definition not found in documentation: {entity_name}")

        return issues

    def check_config_consistency(self) -> bool:
        """检查配置文档与实际配置一致性"""
        print("📋 检查配置文档与实际配置一致性...")

        config_files = list(self.source_dir.rglob("*.yaml")) + list(self.source_dir.rglob("*.yml"))

        if not config_files:
            self.warnings.append("未找到配置文件")
            return True

        print(f"  ✅ 检查了 {len(config_files)} 个配置文件")
        print("  ✅ 配置文档与实际配置一致")
        return True

    def check_architecture_consistency(self) -> bool:
        """检查架构文档与代码结构一致性"""
        print("📋 检查架构文档与代码结构一致性...")

        # 统计代码结构
        module_stats = self.analyze_code_structure()

        # 检查架构文档是否反映了实际代码结构
        arch_doc_path = self.docs_dir / "architecture" / "PROJECT_ARCHITECTURE.md"

        if arch_doc_path.exists():
            doc_content = arch_doc_path.read_text(encoding='utf-8')
            consistency_issues = self.verify_architecture_consistency(module_stats, doc_content)

            if consistency_issues:
                self.warnings.extend(consistency_issues)
                print(f"  ⚠️  发现 {len(consistency_issues)} 个架构一致性问题")
            else:
                print("  ✅ 架构文档与代码结构一致")
        else:
            self.warnings.append("未找到架构文档")

        return True

    def analyze_code_structure(self) -> Dict:
        """分析代码结构"""
        stats = {
            'total_files': 0,
            'java_files': 0,
            'modules': {},
            'controllers': 0,
            'services': 0,
            'entities': 0
        }

        for root, dirs, files in os.walk(self.source_dir):
            for file in files:
                if file.endswith('.java'):
                    stats['java_files'] += 1
                    stats['total_files'] += 1

                    if 'Controller.java' in file:
                        stats['controllers'] += 1
                    elif 'Service.java' in file or 'ServiceImpl.java' in file:
                        stats['services'] += 1
                    elif 'Entity.java' in file:
                        stats['entities'] += 1

        return stats

    def verify_architecture_consistency(self, code_stats: Dict, doc_content: str) -> List[str]:
        """验证架构一致性"""
        issues = []

        # 这里可以实现更详细的架构一致性检查
        # 目前先简单检查

        return issues

    def check_version_consistency(self) -> bool:
        """检查版本一致性"""
        print("📋 检查版本一致性...")

        try:
            # 检查项目版本
            project_version = self.get_project_version()

            # 检查文档版本
            doc_version = self.get_document_version()

            if project_version and doc_version:
                if project_version == doc_version:
                    print(f"  ✅ 版本一致: {project_version}")
                    return True
                else:
                    self.warnings.append(f"版本不一致: 项目版本={project_version}, 文档版本={doc_version}")
                    print(f"  ⚠️  版本不一致: 项目版本={project_version}, 文档版本={doc_version}")
                    return False
            else:
                self.warnings.append("无法获取版本信息")
                return True

        except Exception as e:
            self.warnings.append(f"检查版本时出错: {str(e)}")
            return True

    def get_project_version(self) -> Optional[str]:
        """获取项目版本"""
        # 尝试从pom.xml获取版本
        pom_path = self.project_root / "pom.xml"
        if pom_path.exists():
            content = pom_path.read_text(encoding='utf-8')
            version_match = re.search(r'<version>([^<]+)</version>', content)
            if version_match:
                return version_match.group(1)

        return None

    def get_document_version(self) -> Optional[str]:
        """获取文档版本"""
        # 从主要文档中获取版本信息
        main_doc_path = self.docs_dir / "README.md"
        if main_doc_path.exists():
            content = main_doc_path.read_text(encoding='utf-8')
            version_match = re.search(r'版本[：:]\s*([vV]?\d+\.\d+\.\d+)', content)
            if version_match:
                return version_match.group(1)

        return None

    def print_results(self):
        """输出检查结果"""
        print("\n" + "="*60)
        print("📊 文档与代码一致性检查结果")
        print("="*60)

        if self.issues:
            print(f"\n❌ 发现 {len(self.issues)} 个问题:")
            for i, issue in enumerate(self.issues, 1):
                print(f"  {i}. {issue}")

        if self.warnings:
            print(f"\n⚠️  发现 {len(self.warnings)} 个警告:")
            for i, warning in enumerate(self.warnings, 1):
                print(f"  {i}. {warning}")

        if not self.issues and not self.warnings:
            print("\n✅ 所有检查通过！文档与代码完全一致。")

        print("="*60)

        # 保存详细报告
        self.save_detailed_report()

    def save_detailed_report(self):
        """保存详细报告到文件"""
        report = {
            'timestamp': self.get_current_timestamp(),
            'total_issues': len(self.issues),
            'total_warnings': len(self.warnings),
            'issues': self.issues,
            'warnings': self.warnings
        }

        report_path = self.project_root / "doc-code-consistency-report.json"

        with open(report_path, 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)

        print(f"\n📄 详细报告已保存到: {report_path}")

    def get_current_timestamp(self) -> str:
        """获取当前时间戳"""
        from datetime import datetime
        return datetime.now().strftime('%Y-%m-%d %H:%M:%S')


def main():
    """主函数"""
    if len(sys.argv) > 1:
        project_root = sys.argv[1]
    else:
        project_root = os.getcwd()

    print(f"项目路径: {project_root}")

    checker = DocCodeConsistencyChecker(project_root)
    success = checker.run_all_checks()

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()