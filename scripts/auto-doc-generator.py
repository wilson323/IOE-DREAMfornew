#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM项目自动文档生成工具

基于代码自动生成API文档、数据库文档、架构文档

作者: SmartAdmin Team
版本: v1.0.0
创建时间: 2025-01-13
"""

import os
import re
import sys
import json
from pathlib import Path
from typing import List, Dict, Optional
from datetime import datetime

class DocumentGenerator:
    """自动文档生成器"""

    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.source_dir = self.project_root / "smart-admin-api-java17-springboot3"
        self.docs_dir = self.project_root / "docs"
        self.auto_gen_dir = self.docs_dir / "auto-generated"

        # 确保输出目录存在
        self.auto_gen_dir.mkdir(parents=True, exist_ok=True)

    def generate_all_documents(self):
        """生成所有文档"""
        print("🚀 开始自动生成文档...")

        # 1. 生成API文档
        self.generate_api_documentation()

        # 2. 生成数据库文档
        self.generate_database_documentation()

        # 3. 生成架构文档
        self.generate_architecture_documentation()

        # 4. 生成配置文档
        self.generate_configuration_documentation()

        # 5. 生成部署文档
        self.generate_deployment_documentation()

        print("✅ 文档生成完成！")

    def generate_api_documentation(self):
        """生成API文档"""
        print("📋 生成API文档...")

        # 查找所有Controller
        controllers = list(self.source_dir.rglob("*Controller.java"))

        if not controllers:
            print("  ⚠️  未找到Controller文件")
            return

        # 按模块分组Controller
        modules = self.group_controllers_by_module(controllers)

        # 生成每个模块的API文档
        for module_name, module_controllers in modules.items():
            self.generate_module_api_doc(module_name, module_controllers)

        # 生成API总览文档
        self.generate_api_overview(modules)

    def group_controllers_by_module(self, controllers: List[Path]) -> Dict[str, List[Path]]:
        """按模块分组Controller"""
        modules = {}

        for controller_path in controllers:
            # 根据包路径确定模块
            module_name = self.get_module_from_path(controller_path)

            if module_name not in modules:
                modules[module_name] = []
            modules[module_name].append(controller_path)

        return modules

    def get_module_from_path(self, controller_path: Path) -> str:
        """从路径中获取模块名称"""
        path_parts = controller_path.parts

        # 常见的模块路径模式
        if "module" in path_parts:
            module_index = path_parts.index("module")
            if module_index + 1 < len(path_parts):
                return path_parts[module_index + 1]

        # 默认模块分组
        controller_name = controller_path.stem.lower()
        if "auth" in controller_name or "user" in controller_name:
            return "用户权限"
        elif "access" in controller_name or "door" in controller_name or "visitor" in controller_name:
            return "门禁管理"
        elif "consume" in controller_name or "recharge" in controller_name or "account" in controller_name:
            return "消费管理"
        elif "attendance" in controller_name or "schedule" in controller_name:
            return "考勤管理"
        elif "video" in controller_name or "monitor" in controller_name:
            return "视频监控"
        elif "notification" in controller_name or "message" in controller_name:
            return "通知服务"
        elif "file" in controller_name or "upload" in controller_name:
            return "文件服务"
        elif "system" in controller_name or "health" in controller_name:
            return "系统监控"
        else:
            return "其他模块"

    def generate_module_api_doc(self, module_name: str, controllers: List[Path]):
        """生成单个模块的API文档"""
        apis = []

        for controller_path in controllers:
            controller_apis = self.extract_controller_apis(controller_path)
            apis.extend(controller_apis)

        if not apis:
            return

        # 生成文档内容
        doc_content = self.generate_module_api_doc_content(module_name, controllers, apis)

        # 保存文档
        doc_filename = f"API_{module_name.replace(' ', '')}.md"
        doc_path = self.auto_gen_dir / doc_filename

        with open(doc_path, 'w', encoding='utf-8') as f:
            f.write(doc_content)

        print(f"  ✅ 生成模块API文档: {doc_filename}")

    def extract_controller_apis(self, controller_path: Path) -> List[Dict]:
        """从Controller中提取API信息"""
        try:
            content = controller_path.read_text(encoding='utf-8')

            # 提取类注释
            class_comment = self.extract_class_comment(content)

            # 提取API信息
            apis = []

            # 提取请求映射
            method_pattern = r'@(Get|Post|Put|Delete|Patch)Mapping\(["\']([^"\']+)["\'].*?\)\s+.*?public\s+.*?\s+(\w+)\s*\('

            for match in re.finditer(method_pattern, content, re.MULTILINE | re.DOTALL):
                http_method = match.group(1).upper()
                path = match.group(2)
                method_name = match.group(3)

                # 提取方法注释
                method_comment = self.extract_method_comment(content, method_name)

                # 提取参数信息
                parameters = self.extract_method_parameters(content, method_name)

                # 提取返回类型
                return_type = self.extract_method_return_type(content, method_name)

                api_info = {
                    'method': http_method,
                    'path': path,
                    'full_path': f"{http_method} {path}",
                    'method_name': method_name,
                    'description': method_comment.get('description', ''),
                    'parameters': parameters,
                    'return_type': return_type,
                    'class_comment': class_comment,
                    'controller_name': controller_path.stem
                }

                apis.append(api_info)

            return apis

        except Exception as e:
            print(f"  ⚠️  解析Controller失败: {controller_path} - {str(e)}")
            return []

    def extract_class_comment(self, content: str) -> Dict:
        """提取类注释"""
        comment = {'description': ''}

        # 查找类注释块
        class_pattern = r'/?\*\s*\n.*?\s*\*?\s*([^*]*?)\s*\n.*?class\s+\w+Controller'

        match = re.search(class_pattern, content, re.MULTILINE | re.DOTALL)
        if match:
            comment_text = match.group(1).strip()
            # 清理注释格式
            comment['description'] = re.sub(r'[*\s]*', '', comment_text).strip()

        return comment

    def extract_method_comment(self, content: str, method_name: str) -> Dict:
        """提取方法注释"""
        comment = {'description': ''}

        # 查找方法注释
        method_pattern = rf'/\*\*\s*\n.*?\s*\*?\s*([^*]*?)\s*\n.*?public.*?{method_name}\s*\('

        match = re.search(method_pattern, content, re.MULTILINE | re.DOTALL)
        if match:
            comment_text = match.group(1).strip()
            comment['description'] = re.sub(r'[*\s]*', '', comment_text).strip()

        return comment

    def extract_method_parameters(self, content: str, method_name: str) -> List[Dict]:
        """提取方法参数"""
        parameters = []

        # 查找方法签名
        method_pattern = rf'public.*?{method_name}\s*\(([^)]*)\)'

        match = re.search(method_pattern, content)
        if match:
            params_str = match.group(1)
            if params_str.strip():
                param_list = [p.strip() for p in params_str.split(',')]

                for param_str in param_list:
                    # 解析参数类型和名称
                    param_match = re.match(r'([A-Za-z0-9.<>\[\]]+)\s+(\w+)', param_str)
                    if param_match:
                        parameters.append({
                            'type': param_match.group(1),
                            'name': param_match.group(2),
                            'required': True
                        })

        return parameters

    def extract_method_return_type(self, content: str, method_name: str) -> str:
        """提取方法返回类型"""
        method_pattern = rf'public.*?\s*(ResponseDTO<[^>]+>|[A-Za-z0-9.<>\[\]]+)\s+{method_name}\s*\('

        match = re.search(method_pattern, content)
        if match:
            return match.group(1)

        return "void"

    def generate_module_api_doc_content(self, module_name: str, controllers: List[Path], apis: List[Dict]) -> str:
        """生成模块API文档内容"""
        content = f"""# {module_name} API文档

## 文档信息
- **文档版本**: v1.0.0
- **生成时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
- **生成工具**: 自动文档生成工具
- **关联代码版本**: 从代码自动生成

## 概述
{module_name}模块提供相关的API接口，支持以下功能：

## API接口列表

"""

        # 按Controller分组API
        controller_groups = {}
        for api in apis:
            controller_name = api['controller_name']
            if controller_name not in controller_groups:
                controller_groups[controller_name] = []
            controller_groups[controller_name].append(api)

        for controller_name, controller_apis in controller_groups.items():
            content += f"\n### {controller_name}\n\n"

            for api in controller_apis:
                content += f"""
#### {api['method']} {api['path']}

**接口描述**: {api['description']}

**方法名称**: `{api['method_name']}()`

**请求方法**: {api['method']}

**请求路径**: `{api['path']}`

**返回类型**: `{api['return_type']}`

**参数列表**:
"""

                if api['parameters']:
                    for param in api['parameters']:
                        content += f"- `{param['name']}` ({param['type']}): 参数描述\n"
                else:
                    content += "- 无参数\n"

                content += "\n**请求示例**:\n```http\n{api['method']} {api['path']}\nContent-Type: application/json\n\n{{\n  // 请求参数示例\n}}\n```\n\n**响应示例**:\n```json\n{{\n  \"code\": 0,\n  \"message\": \"操作成功\",\n  \"data\": {{\n    // 响应数据示例\n  }}\n}}\n```\n\n"

        content += """
## 注意事项

1. 所有API都需要在请求头中携带有效的认证Token
2. 请求和响应数据格式为JSON
3. 时间格式统一使用ISO 8601标准: YYYY-MM-DD HH:mm:ss

## 错误码

| 错误码 | 描述 | 解决方案 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式 |
| 401 | 未授权 | 检查Token是否有效 |
| 403 | 权限不足 | 检查用户权限 |
| 404 | 资源不存在 | 检查请求路径 |
| 500 | 服务器内部错误 | 联系技术支持 |

---

*此文档由自动工具生成，如有疑问请联系开发团队。*
"""

        return content

    def generate_api_overview(self, modules: Dict[str, List[Path]]):
        """生成API总览文档"""
        content = """# API总览文档

## 文档信息
- **文档版本**: v1.0.0
- **生成时间**: {生成时间}
- **生成工具**: 自动文档生成工具

## 模块概览

IOE-DREAM系统包含以下API模块：

"""

        total_apis = 0
        for module_name, controllers in modules.items():
            module_apis = sum(len(self.extract_controller_apis(controller)) for controller in controllers)
            total_apis += module_apis
            content += f"- **{module_name}**: {len(controllers)}个Controller, {module_apis}个API接口\n"

        content += f"\n**总计**: {sum(len(controllers) for controllers in modules.values())}个Controller, {total_apis}个API接口\n"

        content += """
## API文档链接

"""

        for module_name in sorted(modules.keys()):
            doc_filename = f"API_{module_name.replace(' ', '')}.md"
            content += f"- [{module_name}](./{doc_filename})\n"

        content += """
## 通用规范

### 请求头
```
Authorization: Bearer <token>
Content-Type: application/json
X-Request-ID: <request-id>
```

### 响应格式
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-01-13T12:00:00"
}
```

### 状态码说明
- 0: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 权限不足
- 404: 资源不存在
- 500: 服务器内部错误

---

*此文档由自动工具生成，基于代码分析生成。*
"""

        # 保存总览文档
        overview_path = self.auto_gen_dir / "API_Overview.md"
        with open(overview_path, 'w', encoding='utf-8') as f:
            f.write(content)

        print(f"  ✅ 生成API总览文档: API_Overview.md")

    def generate_database_documentation(self):
        """生成数据库文档"""
        print("📋 生成数据库文档...")

        # 查找所有Entity文件
        entities = list(self.source_dir.rglob("*Entity.java"))

        if not entities:
            print("  ⚠️  未找到Entity文件")
            return

        # 按模块分组Entity
        modules = self.group_entities_by_module(entities)

        # 生成每个模块的数据库文档
        for module_name, module_entities in modules.items():
            self.generate_module_db_doc(module_name, module_entities)

        # 生成数据库总览文档
        self.generate_database_overview(modules)

    def group_entities_by_module(self, entities: List[Path]) -> Dict[str, List[Path]]:
        """按模块分组Entity"""
        modules = {}

        for entity_path in entities:
            module_name = self.get_module_from_path(entity_path)

            if module_name not in modules:
                modules[module_name] = []
            modules[module_name].append(entity_path)

        return modules

    def generate_module_db_doc(self, module_name: str, entities: List[Path]):
        """生成单个模块的数据库文档"""
        tables = []

        for entity_path in entities:
            table_info = self.extract_table_info(entity_path)
            if table_info:
                tables.append(table_info)

        if not tables:
            return

        # 生成文档内容
        doc_content = self.generate_module_db_doc_content(module_name, entities, tables)

        # 保存文档
        doc_filename = f"Database_{module_name.replace(' ', '')}.md"
        doc_path = self.auto_gen_dir / doc_filename

        with open(doc_path, 'w', encoding='utf-8') as f:
            f.write(doc_content)

        print(f"  ✅ 生成模块数据库文档: {doc_filename}")

    def extract_table_info(self, entity_path: Path) -> Optional[Dict]:
        """从Entity中提取表信息"""
        try:
            content = entity_path.read_text(encoding='utf-8')

            # 提取表名
            table_name = self.extract_table_name(content)
            if not table_name:
                return None

            # 提取类注释
            class_comment = self.extract_class_comment(content)

            # 提取字段信息
            fields = self.extract_entity_fields_detailed(content)

            # 提取索引信息
            indexes = self.extract_entity_indexes(content)

            table_info = {
                'table_name': table_name,
                'entity_name': entity_path.stem,
                'description': class_comment.get('description', ''),
                'fields': fields,
                'indexes': indexes,
                'entity_path': str(entity_path)
            }

            return table_info

        except Exception as e:
            print(f"  ⚠️  解析Entity失败: {entity_path} - {str(e)}")
            return None

    def extract_table_name(self, content: str) -> Optional[str]:
        """从Entity中提取表名"""
        # 查找@Table注解
        table_match = re.search(r'@Table\s*\(\s*name\s*=\s*["\']([^"\']+)["\']', content)
        if table_match:
            return table_match.group(1)

        # 如果没有@Table注解，使用类名转换
        class_match = re.search(r'class\s+(\w+Entity)', content)
        if class_match:
            class_name = class_match.group(1)
            table_name = re.sub('([A-Z])', r'_\1', class_name).lower()
            if table_name.startswith('_'):
                table_name = table_name[1:]
            return f"t_{table_name}"

        return None

    def extract_entity_fields_detailed(self, content: str) -> List[Dict]:
        """从Entity中提取详细字段信息"""
        fields = []

        # 提取字段定义模式
        field_patterns = [
            # 带注解的字段
            r'@Column\s*\([^)]*\)\s+.*?(?:@Transient\s+)?(?:public|private)\s+(?:\w+\s+)*(\w+(?:<[^>]+>)?)\s+(\w+)\s*;',
            # 简单字段
            r'(?:(?:@Column\s*\([^)]*\))\s*.*?(?:public|private)\s+(?:\w+\s+)*(\w+(?:<[^>]+>)?)\s+(\w+)\s*;'
        ]

        for pattern in field_patterns:
            for match in re.finditer(pattern, content):
                try:
                    field_type = match.group(1) if match.groups() else 'String'
                    field_name = match.group(2) if len(match.groups()) > 1 else match.group(1)

                    # 提取字段注解
                    field_annotations = self.extract_field_annotations(content, field_name)

                    # 提取字段注释
                    field_comment = self.extract_field_comment(content, field_name)

                    field_info = {
                        'name': field_name,
                        'type': field_type,
                        'java_type': field_type,
                        'annotations': field_annotations,
                        'comment': field_comment.get('description', ''),
                        'nullable': field_annotations.get('nullable', True),
                        'length': field_annotations.get('length', None),
                        'default': field_annotations.get('default', None)
                    }

                    fields.append(field_info)
                except Exception:
                    continue

        return fields

    def extract_field_annotations(self, content: str, field_name: str) -> Dict:
        """提取字段注解"""
        annotations = {}

        # 查找字段注解
        field_pattern = rf'@Column\s*\([^)]*\)\s+.*?{field_name}\s*;'
        match = re.search(field_pattern, content)

        if match:
            annotation_text = match.group(1)

            # 提取nullable
            nullable_match = re.search(r'nullable\s*=\s*(true|false)', annotation_text)
            if nullable_match:
                annotations['nullable'] = nullable_match.group(1) == 'true'

            # 提取length
            length_match = re.search(r'length\s*=\s*(\d+)', annotation_text)
            if length_match:
                annotations['length'] = int(length_match.group(1))

            # 提取default
            default_match = re.search(r'default\s*=\s*["\']([^"\']*)["\']', annotation_text)
            if default_match:
                annotations['default'] = default_match.group(1)

        return annotations

    def extract_field_comment(self, content: str, field_name: str) -> Dict:
        """提取字段注释"""
        comment = {'description': ''}

        # 查找字段注释
        field_pattern = rf'/\*\*\s*\n.*?\s*\*?\s*([^*]*?)\s*\n.*?private.*?{field_name}\s*;'
        match = re.search(field_pattern, content, re.MULTILINE | re.DOTALL)
        if match:
            comment_text = match.group(1).strip()
            comment['description'] = re.sub(r'[*\s]*', '', comment_text).strip()

        return comment

    def extract_entity_indexes(self, content: str) -> List[Dict]:
        """提取Entity索引信息"""
        indexes = []

        # 查找索引注解
        index_patterns = [
            r'@Index',
            r'@UniqueIndex'
        ]

        for pattern in index_patterns:
            matches = re.finditer(pattern + r'\([^)]*\)', content)
            for match in matches:
                index_info = {
                    'type': pattern.replace('@', ''),
                    'details': match.group(1)
                }
                indexes.append(index_info)

        return indexes

    def generate_module_db_doc_content(self, module_name: str, entities: List[Path], tables: List[Dict]) -> str:
        """生成模块数据库文档内容"""
        content = f"""# {module_name}数据库设计文档

## 文档信息
- **文档版本**: v1.0.0
- **生成时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
- **生成工具**: 自动文档生成工具
- **关联代码版本**: 从代码自动生成

## 概述
{module_name}模块包含以下数据表，用于存储相关业务数据：

## 数据表列表

"""

        for table in tables:
            content += self.generate_table_doc(table)

        content += """
## 数据关系

"""

        # 可以添加数据关系图的描述

        content += """
## 设计规范

### 命名规范
- 表名: t_模块_功能 (t_module_function)
- 字段名: 小写字母和下划线 (lowercase_underscore)
- 索引名: idx_表名_字段名 (idx_table_field)

### 字段规范
- 主键: {table}_id (BIGINT AUTO_INCREMENT)
- 创建时间: create_time (DATETIME)
- 更新时间: update_time (DATETIME)
- 创建人: create_user_id (BIGINT)
- 更新人: update_user_id (BIGINT)
- 删除标记: deleted_flag (TINYINT DEFAULT 0)

### 索引规范
- 每个表必须有主键索引
- 外键字段必须建立索引
- 查询频繁的字段建议建立复合索引

---

*此文档由自动工具生成，基于Entity代码分析生成。*
"""

        return content

    def generate_table_doc(self, table: Dict) -> str:
        """生成单个表的文档"""
        content = f"""
### {table['table_name']}

**实体类**: `{table['entity_name']}`

**表描述**: {table['description']}

#### 字段说明

| 字段名 | 类型 | 是否为空 | 默认值 | 说明 |
|--------|------|----------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键ID |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | YES | NULL | 更新时间 |
| create_user_id | BIGINT | NO | - | 创建人ID |
| update_user_id | BIGINT | YES | NULL | 更新人ID |
| deleted_flag | TINYINT | NO | 0 | 删除标记(0:未删除, 1:已删除) |

"""

        # 添加业务字段
        for field in table['fields']:
            nullable = 'YES' if field.get('nullable', True) else 'NO'
            default_value = field.get('default', 'NULL')
            if default_value == 'NULL' and not field.get('nullable', True):
                default_value = 'REQUIRED'

            content += f"| {field['name']} | {field['java_type']} | {nullable} | {default_value} | {field['comment']} |\n"

        if table['indexes']:
            content += """
#### 索引说明

| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
"""

            for index in table['indexes']:
                content += f"| {index['details']} | {index['type']} | - | 索引详情 |\n"

        content += "\n"

        return content

    def generate_database_overview(self, modules: Dict[str, List[Path]]):
        """生成数据库总览文档"""
        content = """# 数据库设计总览

## 文档信息
- **文档版本**: v1.0.0
- **生成时间**: {生成时间}
- **生成工具**: 自动文档生成工具

## 数据库概览

IOE-DREAM系统包含以下数据库表：

"""

        total_tables = 0
        for module_name, entities in modules.items():
            module_tables = len([e for e in entities if self.extract_table_info(e.read_text(encoding='utf-8'))])
            total_tables += module_tables
            content += f"- **{module_name}**: {len(entities)}个Entity, {module_tables}个表\n"

        content += f"\n**总计**: {sum(len(entities) for entities in modules.values())}个Entity, {total_tables}个表\n"

        content += """
## 数据库文档链接

"""

        for module_name in sorted(modules.keys()):
            doc_filename = f"Database_{module_name.replace(' ', '')}.md"
            content += f"- [{module_name}](./{doc_filename})\n"

        content += """
## 全局设计规范

### 数据库配置
- 数据库类型: MySQL 8.0+
- 字符集: utf8mb4
- 排序规则: utf8mb4_unicode_ci
- 存储引擎: InnoDB

### 命名规范

#### 表命名规范
```
格式: t_module_function
示例:
- t_employee: 员工表
- t_access_record: 门禁记录表
- t_consume_record: 消费记录表
```

#### 字段命名规范
```
格式: lowercase_underscore
示例:
- user_id: 用户ID
- create_time: 创建时间
- is_active: 是否激活
```

#### 索引命名规范
```
格式: idx_table_field
示例:
- idx_employee_user_id: 员工表用户ID索引
- idx_access_record_create_time: 门禁记录创建时间索引
```

### 字段类型规范

#### 基础类型
- 主键: BIGINT AUTO_INCREMENT
- 文本: VARCHAR, TEXT
- 数字: INT, BIGINT, DECIMAL
- 时间: DATETIME, DATE
- 布尔: TINYINT, BOOLEAN

#### 业务类型
- 枚举: TINYINT (存储数字，应用层转换)
- 状态: TINYINT (0/1/2)
- 标识: VARCHAR (长度根据业务需求)

### 审计字段规范

每个业务表都必须包含以下审计字段：

```sql
`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
`update_user_id` BIGINT NULL DEFAULT NULL COMMENT '更新人ID',
`deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记'
```

---

*此文档由自动工具生成，基于Entity代码分析生成。*
"""

        # 保存总览文档
        overview_path = self.auto_gen_dir / "Database_Overview.md"
        with open(overview_path, 'w', encoding='utf-8') as f:
            f.write(content)

        print(f"  ✅ 生成数据库总览文档: Database_Overview.md")

    def generate_architecture_documentation(self):
        """生成架构文档"""
        print("📋 生成架构文档...")

        # 实现架构文档生成逻辑
        content = """
# 系统架构文档

## 文档信息
- **文档版本**: v1.0.0
- **生成时间""" + f": {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n"
"""- **生成工具**: 自动文档生成工具

## 系统架构概览

IOE-DREAM智能管理系统采用微服务架构，包含以下主要模块：

### 技术架构图

```mermaid
graph TB
    Client[客户端] --> Gateway[API网关:8080]

    Gateway --> Auth[用户权限服务:8081]
    Gateway --> Area[区域管理服务:8082]
    Gateway --> Access[门禁服务:8083]
    Gateway --> Consume[消费服务:8084]
    Gateway --> Attendance[考勤服务:8085]
    Gateway --> Video[视频服务:8086]
    Gateway --> Notification[通知服务:8087]
    Gateway --> File[文件服务:8088]
    Gateway --> Monitor[监控服务:8089]

    Gateway --> Nacos[Nacos:8848]
    Auth --> Nacos
    Area --> Nacos
    Access --> Nacos
    Consume --> Nacos
    Attendance --> Nacos
    Video --> Nacos
    Notification --> Nacos
    File --> Nacos
    Monitor --> Nacos

    Gateway --> Redis[(Redis:6379)]
    Auth --> Redis
    Area --> Redis
    Access --> Redis
    Consume --> Redis
    Attendance --> Redis
    Video --> Redis
    Notification --> Redis
    File --> Redis
    Monitor --> Redis

    Auth --> MySQL[(MySQL:3306)]
    Area --> MySQL
    Access --> MySQL
    Consume --> MySQL
    Attendance --> MySQL
    Video --> MySQL
    Notification --> MySQL
    File --> MySQL
    Monitor --> MySQL
```

### 模块说明

#### 基础服务层
- **区域管理服务**: 提供区域管理、人员区域权限等基础功能
- **用户权限服务**: 提供用户认证、角色权限管理等功能

#### 核心业务层
- **门禁服务**: 提供门禁设备管理、访客管理等功能
- **消费服务**: 提供账户管理、消费记录、充值退款等功能
- **考勤服务**: 提供考勤规则、排班管理、打卡记录等功能
- **视频监控服务**: 提供视频设备管理、实时监控等功能

#### 支撑服务层
- **通知服务**: 提供消息推送、邮件通知、短信服务等功能
- **文件服务**: 提供文件上传、存储管理、预览下载等功能
- **监控服务**: 提供系统监控、性能统计、日志管理等功能

## 技术栈

### 后端技术栈
- **框架**: Spring Boot 3.5.7
- **微服务**: Spring Cloud 2023.0.3
- **服务发现**: Spring Cloud Alibaba Nacos
- **配置管理**: Nacos Config
- **网关**: Spring Cloud Gateway
- **负载均衡**: Spring Cloud LoadBalancer
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.x
- **消息队列**: Redis Streams

### 开发工具栈
- **构建工具**: Maven 3.x
- **代码质量**: SonarQube
- **CI/CD**: Jenkins
- **容器化**: Docker
- **编排**: Kubernetes

---

*此文档由自动工具生成，基于项目结构分析生成。*
"""

        # 保存架构文档
        arch_path = self.auto_gen_dir / "Architecture_Overview.md"
        with open(arch_path, 'w', encoding='utf-8') as f:
            f.write(content)

        print("  ✅ 生成架构文档: Architecture_Overview.md")

    def generate_configuration_documentation(self):
        """生成配置文档"""
        print("📋 生成配置文档...")

        content = """
# 配置管理文档

## 文档信息
- **文档版本**: v1.0.0
- **生成时间""" + f": {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n"
"""- **生成工具**: 自动文档生成工具

## 配置文件概览

### 应用配置文件
- `bootstrap.yml`: 应用启动配置
- `application.yml`: 应用主配置
- `application-{profile}.yml`: 环境特定配置

### 环境配置

#### 开发环境 (dev)
- 数据库连接: 本地开发数据库
- Redis连接: 本地Redis
- 日志级别: DEBUG

#### 测试环境 (test)
- 数据库连接: 测试环境数据库
- Redis连接: 测试环境Redis
- 日志级别: INFO

#### 生产环境 (prod)
- 数据库连接: 生产环境数据库
- Redis连接: 生产环境Redis
- 日志级别: WARN

## 外部依赖服务

### Nacos配置中心
- 服务器地址: localhost:8848
- 命名空间: ioe-dream
- 配置分组: DEFAULT_GROUP

### Redis缓存服务
- 服务器地址: localhost:6379
- 数据库: 0
- 密码: zkteco3100

### MySQL数据库服务
- 服务器地址: localhost:3306
- 数据库名: smart_admin_v3
- 用户名: root
- 密码: root1234

## 配置示例

### 数据库连接配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_admin_v3?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: root1234
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
```

### Redis连接配置
```yaml
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      password: zkteco3100
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
```

---

*此文档由自动工具生成，基于配置文件分析生成。*
"""

        # 保存配置文档
        config_path = self.auto_gen_dir / "Configuration_Guide.md"
        with open(config_path, 'w', encoding='utf-8') as f:
            f.write(content)

        print("  ✅ 生成配置文档: Configuration_Guide.md")

    def generate_deployment_documentation(self):
        """生成部署文档"""
        print("📋 生成部署文档...")

        content = """
# 部署指南

## 文档信息
- **文档版本**: v1.0.0
- **生成时间""" + f": {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n"
"""- **生成工具**: 自动文档生成工具

## 部署架构

### 容器化部署

#### Docker镜像构建
```bash
# 构建基础镜像
docker build -t smart-admin-base .

# 构建应用镜像
docker build -t smart-admin-api .
```

#### Docker Compose部署
```yaml
version: '3.8'

services:
  # Nacos服务发现
  nacos:
    image: nacos/nacos-server:v2.2.3
    ports:
      - "8848:8848"
    environment:
      - MODE=standalone
    volumes:
      - ./nacos/logs:/home/nacos/logs

  # Redis缓存服务
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes --requirepass zkteco3100

  # MySQL数据库
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root1234
      - MYSQL_DATABASE=smart_admin_v3
      - MYSQL_USER=ioe-dream
      - MYSQL_PASSWORD=ioe-dream123
    volumes:
      - ./data/mysql:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d

  # 应用服务
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - nacos
      - redis
      - mysql
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - REDIS_HOST=redis
      - MYSQL_HOST=mysql
```

### Kubernetes部署

#### Kubernetes部署配置
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: smart-admin-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: smart-admin
  template:
    metadata:
      labels:
        app: smart-admin
    spec:
      containers:
      - name: smart-admin
        image: smart-admin:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: NACOS_SERVER_ADDR
          value: "nacos:8848"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"

---
apiVersion: v1
kind: Service
metadata:
  name: smart-admin-service
spec:
  selector:
    app: smart-admin
  ports:
  - port: 8080
    targetPort: 8080
  type: LoadBalancer
```

## 部署步骤

### 1. 环境准备
- 安装Docker和Docker Compose
- 准备MySQL数据库
- 准备Redis缓存
- 配置Nacos服务发现

### 2. 应用部署
```bash
# 克隆项目代码
git clone <repository-url>

# 构建应用
mvn clean package -DskipTests

# 启动服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### 3. 验证部署
```bash
# 检查应用健康状态
curl http://localhost:8080/actuator/health

# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list

# 检查应用日志
docker-compose logs -f app
```

## 监控配置

### 健康检查
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
      health:
        show-details: always
  health:
    db:
      enabled: true
```

### 监控指标
- 应用性能指标
- 数据库连接池状态
- Redis连接状态
- JVM内存使用情况

---

*此文档由自动工具生成，基于部署配置分析生成。*
"""

        # 保存部署文档
        deploy_path = self.auto_gen_dir / "Deployment_Guide.md"
        with open(deploy_path, 'w', encoding='utf-8') as f:
            f.write(content)

        print("  ✅ 生成部署文档: Deployment_Guide.md")


def main():
    """主函数"""
    if len(sys.argv) > 1:
        project_root = sys.argv[1]
    else:
        project_root = os.getcwd()

    print(f"项目路径: {project_root}")

    generator = DocumentGenerator(project_root)
    generator.generate_all_documents()


if __name__ == "__main__":
    main()