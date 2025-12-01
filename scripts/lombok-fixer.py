#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Lombok修复器 - Task 2实现
批量修复Lombok相关问题，添加缺失的getter/setter方法

角色: Java代码修复专家，精通Lombok注解处理和代码生成
任务: 创建Python脚本批量修复Lombok相关问题，扫描所有使用@Data注解的Java文件
限制: 必须保持Lombok注解优先，只在必要时手动补充，不破坏现有业务逻辑
成功: 所有Lombok相关问题修复完成，Entity类编译通过，getter/setter方法完整性100%
"""

import os
import re
import json
from datetime import datetime
from typing import List, Dict, Tuple, Optional, Set
from dataclasses import dataclass, asdict
from pathlib import Path
from collections import defaultdict

@dataclass
class EntityInfo:
    """Entity类信息"""
    file_path: str
    class_name: str
    fields: List[Dict[str, str]]  # field_name, field_type
    has_data_annotation: bool
    missing_methods: List[str]
    extends_base_entity: bool

@dataclass
class FixResult:
    """修复结果"""
    total_entities: int
    fixed_entities: int
    failed_entities: int
    added_methods: int
    fix_details: List[Dict[str, str]]

class LombokFixer:
    """Lombok修复器"""

    def __init__(self, project_path: str):
        self.project_path = Path(project_path)
        self.entity_pattern = re.compile(r'class\s+(\w+Entity)\s+.*?extends\s+(\w+)?')
        self.field_pattern = re.compile(r'private\s+(\w+(?:<[^>]*>)?)\s+(\w+)(?:\s*=\s*[^;]+)?;')
        self.method_pattern = re.compile(r'public\s+\w+\s+(get\w+|set\w+|is\w+)\s*\([^)]*\)')

    def fix_lombok_issues(self) -> FixResult:
        """
        修复Lombok相关问题

        Returns:
            FixResult: 修复结果
        """
        print("开始修复Lombok相关问题...")

        # 扫描Entity类
        entity_infos = self._scan_entity_classes()
        print(f"扫描到 {len(entity_infos)} 个Entity类")

        fix_result = FixResult(
            total_entities=len(entity_infos),
            fixed_entities=0,
            failed_entities=0,
            added_methods=0,
            fix_details=[]
        )

        # 修复每个Entity类
        for entity_info in entity_infos:
            if entity_info.missing_methods:
                success = self._add_missing_getter_setter(entity_info)
                if success:
                    fix_result.fixed_entities += 1
                    fix_result.added_methods += len(entity_info.missing_methods)
                    fix_result.fix_details.append({
                        'entity': entity_info.class_name,
                        'file': entity_info.file_path,
                        'methods': entity_info.missing_methods,
                        'status': 'success'
                    })
                else:
                    fix_result.failed_entities += 1
                    fix_result.fix_details.append({
                        'entity': entity_info.class_name,
                        'file': entity_info.file_path,
                        'methods': entity_info.missing_methods,
                        'status': 'failed'
                    })

        print(f"修复完成: {fix_result.fixed_entities}/{fix_result.total_entities} 个类")
        print(f"添加方法: {fix_result.added_methods} 个")

        return fix_result

    def _scan_entity_classes(self) -> List[EntityInfo]:
        """
        扫描所有Entity类

        Returns:
            List[EntityInfo]: Entity类信息列表
        """
        entity_infos = []

        # 查找所有Entity类文件
        entity_files = []
        for root, dirs, files in os.walk(self.project_path):
            for file in files:
                if file.endswith('Entity.java'):
                    entity_files.append(Path(root) / file)

        print(f"找到 {len(entity_files)} 个Entity文件")

        for entity_file in entity_files:
            entity_info = self._analyze_entity_file(str(entity_file))
            if entity_info:
                entity_infos.append(entity_info)

        return entity_infos

    def _analyze_entity_file(self, file_path: str) -> Optional[EntityInfo]:
        """
        分析Entity文件

        Args:
            file_path: Entity文件路径

        Returns:
            EntityInfo: Entity类信息
        """
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            # 提取类名和继承关系
            class_match = re.search(r'class\s+(\w+Entity)(?:\s+extends\s+(\w+))?', content)
            if not class_match:
                return None

            class_name = class_match.group(1)
            parent_class = class_match.group(2) if class_match.group(2) else None
            extends_base_entity = parent_class == 'BaseEntity' if parent_class else False

            # 检查@Data注解
            has_data = '@Data' in content

            # 提取字段
            fields = []
            for match in self.field_pattern.finditer(content):
                field_type = match.group(1)
                field_name = match.group(2)
                fields.append({
                    'name': field_name,
                    'type': field_type
                })

            # 检查缺失的方法
            missing_methods = []
            if fields and not has_data:
                existing_methods = set()
                for match in self.method_pattern.finditer(content):
                    existing_methods.add(match.group(1))

                # 为每个字段生成getter/setter
                for field in fields:
                    field_name = field['name']
                    field_type = field['type']

                    # Getter方法
                    if field_type.lower() == 'boolean':
                        getter_name = f"is{field_name[0].upper()}{field_name[1:]}"
                    else:
                        getter_name = f"get{field_name[0].upper()}{field_name[1:]}"

                    if getter_name not in existing_methods:
                        missing_methods.append(getter_name)

                    # Setter方法
                    setter_name = f"set{field_name[0].upper()}{field_name[1:]}"
                    if setter_name not in existing_methods:
                        missing_methods.append(setter_name)

            return EntityInfo(
                file_path=file_path,
                class_name=class_name,
                fields=fields,
                has_data_annotation=has_data,
                missing_methods=missing_methods,
                extends_base_entity=extends_base_entity
            )

        except Exception as e:
            print(f"分析Entity文件失败 {file_path}: {str(e)}")
            return None

    def _add_missing_getter_setter(self, entity_info: EntityInfo) -> bool:
        """
        添加缺失的getter/setter方法

        Args:
            entity_info: Entity类信息

        Returns:
            bool: 是否成功添加
        """
        try:
            with open(entity_info.file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            # 找到类的结束位置
            class_end_pattern = re.search(r'^\}', content, re.MULTILINE)
            if not class_end_pattern:
                print(f"找不到类结束位置: {entity_info.file_path}")
                return False

            insert_position = class_end_pattern.start()

            # 生成缺失的方法
            methods_to_add = []
            for field in entity_info.fields:
                field_name = field['name']
                field_type = field['type']

                # 检查是否需要添加getter
                getter_name = f"get{field_name[0].upper()}{field_name[1:]}" if field_type.lower() != 'boolean' else f"is{field_name[0].upper()}{field_name[1:]}"
                if getter_name in entity_info.missing_methods:
                    methods_to_add.append(self._generate_getter_method(field_name, field_type))

                # 检查是否需要添加setter
                setter_name = f"set{field_name[0].upper()}{field_name[1:]}"
                if setter_name in entity_info.missing_methods:
                    methods_to_add.append(self._generate_setter_method(field_name, field_type))

            if not methods_to_add:
                print(f"没有需要添加的方法: {entity_info.class_name}")
                return True

            # 添加方法到类中
            methods_code = '\n\n    ' + '\n    '.join(methods_to_add)

            new_content = (
                content[:insert_position] +
                methods_code + '\n' +
                content[insert_position:]
            )

            # 如果没有@Data注解，添加@Data注解
            if not entity_info.has_data_annotation:
                class_start_pattern = re.search(r'public\s+class\s+' + entity_info.class_name, content)
                if class_start_pattern:
                    insert_pos = class_start_pattern.start()
                    new_content = (
                        content[:insert_pos] +
                        '@Data\n' +
                        content[insert_pos:]
                    )

            # 写回文件
            with open(entity_info.file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)

            print(f"成功修复Entity类: {entity_info.class_name}, 添加 {len(methods_to_add)} 个方法")
            return True

        except Exception as e:
            print(f"添加方法失败 {entity_info.file_path}: {str(e)}")
            return False

    def _generate_getter_method(self, field_name: str, field_type: str) -> str:
        """
        生成getter方法

        Args:
            field_name: 字段名
            field_type: 字段类型

        Returns:
            str: getter方法代码
        """
        if field_type.lower() == 'boolean':
            method_name = f"is{field_name[0].upper()}{field_name[1:]}"
        else:
            method_name = f"get{field_name[0].upper()}{field_name[1:]}"

        return f"""public {field_type} {method_name}() {{
        return this.{field_name};
    }}"""

    def _generate_setter_method(self, field_name: str, field_type: str) -> str:
        """
        生成setter方法

        Args:
            field_name: 字段名
            field_type: 字段类型

        Returns:
            str: setter方法代码
        """
        method_name = f"set{field_name[0].upper()}{field_name[1:]}"

        return f"""public void {method_name}({field_type} {field_name}) {{
        this.{field_name} = {field_name};
    }}"""

    def validate_lombok_fix(self) -> Dict[str, any]:
        """
        验证Lombok修复效果

        Returns:
            Dict: 验证结果
        """
        print("开始验证Lombok修复效果...")

        try:
            # 执行Maven编译检查
            import subprocess
            os.chdir(self.project_path)
            result = subprocess.run(
                ['mvn', 'compile', '-q'],
                capture_output=True,
                text=True,
                timeout=300
            )

            # 统计错误数量
            error_count = result.stderr.count('[ERROR]')

            # 检查特定的Lombok相关错误
            lombok_errors = []
            error_lines = result.stderr.split('\n')
            for line in error_lines:
                if '找不到符号.*方法.*get' in line or '找不到符号.*方法.*set' in line:
                    lombok_errors.append(line.strip())

            validation_result = {
                'success': result.returncode == 0,
                'error_count': error_count,
                'lombok_errors': len(lombok_errors),
                'lombok_error_details': lombok_errors[:10],  # 只保留前10个
                'validation_time': datetime.now().isoformat()
            }

            print(f"验证结果: {'成功' if validation_result['success'] else '失败'}")
            print(f"错误数量: {validation_result['error_count']}")
            print(f"Lombok错误: {validation_result['lombok_errors']}")

            return validation_result

        except Exception as e:
            print(f"验证过程发生错误: {str(e)}")
            return {
                'success': False,
                'error': str(e),
                'validation_time': datetime.now().isoformat()
            }

def main():
    """主函数"""
    print("Lombok修复器")
    print("="*50)

    # 项目路径
    project_path = Path(__file__).parent.parent / "smart-admin-api-java17-springboot3"

    # 创建修复器
    fixer = LombokFixer(str(project_path))

    try:
        # 执行修复
        result = fixer.fix_lombok_issues()

        print("\n修复结果统计")
        print("="*50)
        print(f"总Entity类数: {result.total_entities}")
        print(f"成功修复类数: {result.fixed_entities}")
        print(f"修复失败类数: {result.failed_entities}")
        print(f"添加方法总数: {result.added_methods}")

        if result.fix_details:
            print("\n修复详情:")
            for detail in result.fix_details:
                print(f"  {detail['entity']}: {detail['status']} ({len(detail['methods'])}个方法)")

        # 验证修复效果
        print("\n开始验证修复效果...")
        validation = fixer.validate_lombok_fix()

        # 保存修复报告
        report = {
            'fix_result': {
                'total_entities': result.total_entities,
                'fixed_entities': result.fixed_entities,
                'failed_entities': result.failed_entities,
                'added_methods': result.added_methods,
                'fix_details': result.fix_details
            },
            'validation_result': validation,
            'fix_time': datetime.now().isoformat()
        }

        report_file = project_path / "lombok_fix_report.json"
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)

        print(f"\n修复报告已保存到: {report_file}")

        return result

    except Exception as e:
        print(f"修复过程中发生错误: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

if __name__ == "__main__":
    main()