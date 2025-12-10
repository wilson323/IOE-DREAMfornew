#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
RESTful API设计违规检测工具
扫描Controller文件，识别POST滥用等RESTful设计违规
"""

import os
import re
import json
from pathlib import Path
from datetime import datetime

class RestfulViolationScanner:
    def __init__(self, project_root):
        self.project_root = Path(project_root)
        self.violations = []
        self.controllers = []

    def scan_controllers(self):
        """扫描所有Controller文件"""
        print("🔍 扫描Controller文件...")

        # 查找所有Controller文件
        controller_files = list(self.project_root.rglob("**/controller/**/*Controller.java"))

        print(f"📊 发现 {len(controller_files)} 个Controller文件")

        for controller_file in controller_files:
            # 跳过target目录
            if 'target/' in str(controller_file):
                continue

            self.scan_controller(controller_file)

        return self.controllers

    def scan_controller(self, controller_file):
        """扫描单个Controller文件"""
        try:
            with open(controller_file, 'r', encoding='utf-8') as f:
                content = f.read()

            # 提取微服务名称
            service_match = re.search(r'microservices/([^/]+)/', str(controller_file))
            service_name = service_match.group(1) if service_match else "unknown"

            controller_info = {
                'file': str(controller_file),
                'service': service_name,
                'controller_name': controller_file.stem,
                'apis': [],
                'violations': []
            }

            # 解析API接口
            api_lines = []
            lines = content.split('\n')

            for i, line in enumerate(lines):
                # 查找API映射注解
                if re.search(r'@(Get|Post|Put|Delete|Patch)(Mapping)?\s*\(', line):
                    api_info = self.parse_api_annotation(lines, i)
                    if api_info:
                        controller_info['apis'].append(api_info)

                        # 检查违规
                        violations = self.check_violations(api_info, line)
                        if violations:
                            controller_info['violations'].extend(violations)

            # 统计违规
            total_violations = len(controller_info['violations'])
            if total_violations > 0:
                print(f"  ⚠️  {controller_file.name}: {total_violations} 个违规")

            self.controllers.append(controller_info)

        except Exception as e:
            print(f"  ❌ 扫描文件失败: {controller_file} - {str(e)}")

    def parse_api_annotation(self, lines, start_line):
        """解析API注解"""
        try:
            api_line = lines[start_line]

            # 确定HTTP方法
            http_method = self.extract_http_method(api_line)
            if not http_method:
                return None

            # 提取路径
            path = self.extract_path(api_line)

            # 查找对应的方法定义
            method_name = None
            for i in range(start_line + 1, min(start_line + 20, len(lines))):
                if 'public' in lines[i] and '(' in lines[i] and ')' in lines[i]:
                    method_match = re.search(r'(\w+)\s*\(', lines[i])
                    if method_match:
                        method_name = method_match.group(1)
                    break

            return {
                'http_method': http_method,
                'path': path,
                'method_name': method_name,
                'line_number': start_line + 1
            }

        except Exception:
            return None

    def extract_http_method(self, line):
        """提取HTTP方法"""
        if '@GetMapping' in line or '@GetMapping' in line:
            return 'GET'
        elif '@PostMapping' in line or '@PostMapping' in line:
            return 'POST'
        elif '@PutMapping' in line or '@PutMapping' in line:
            return 'PUT'
        elif '@DeleteMapping' in line or '@DeleteMapping' in line:
            return 'DELETE'
        elif '@PatchMapping' in line or '@PatchMapping' in line:
            return 'PATCH'
        elif '@RequestMapping' in line or '@RequestMapping' in line:
            # 检查方法参数
            if 'method = RequestMethod.GET' in line:
                return 'GET'
            elif 'method = RequestMethod.POST' in line:
                return 'POST'
            elif 'method = RequestMethod.PUT' in line:
                return 'PUT'
            elif 'method = RequestMethod.DELETE' in line:
                return 'DELETE'
            elif 'method = RequestMethod.PATCH' in line:
                return 'PATCH'
        return None

    def extract_path(self, line):
        """提取API路径"""
        # 提取value或path参数
        path_match = re.search(r'(?:value|path|value\s*=\s*|path\s*=\s*)["\']([^"\']+)["\']', line)
        if path_match:
            return path_match.group(1)

        # 简单提取第一个路径
        path_match = re.search(r'["\']([^"\']+)["\']', line)
        if path_match:
            return path_match.group(1)

        return ""

    def check_violations(self, api_info, line_content):
        """检查RESTful违规"""
        violations = []

        method = api_info['http_method']
        path = api_info['path']
        method_name = api_info['method_name']

        # 检查POST违规
        if method == 'POST':
            # 查询操作违规
            if self.is_query_operation(method_name, path, line_content):
                violations.append({
                    'type': 'QUERY_USING_POST',
                    'description': '查询操作应该使用GET',
                    'suggestion': f'改为 GET /api/v1/{self.extract_resource_name(path)}'
                })

            # 更新操作违规
            elif self.is_update_operation(method_name, path, line_content):
                violations.append({
                    'type': 'UPDATE_USING_POST',
                    'description': '更新操作应该使用PUT',
                    'suggestion': f'改为 PUT /api/v1/{self.extract_resource_name(path)}/{{id}}'
                })

            # 删除操作违规
            elif self.is_delete_operation(method_name, path, line_content):
                violations.append({
                    'type': 'DELETE_USING_POST',
                    'description': '删除操作应该使用DELETE',
                    'suggestion': f'改为 DELETE /api/v1/{self.extract_resource_name(path)}/{{id}}'
                })

            # 状态更新违规
            elif self.is_status_update(method_name, path, line_content):
                violations.append({
                    'type': 'STATUS_USING_POST',
                    'description': '状态更新应该使用PATCH',
                    'suggestion': f'改为 PATCH /api/v1/{self.extract_resource_name(path)}/{{id}}/status'
                })

        # 检查URL设计违规
        if path:
            url_violations = self.check_url_design(path, method)
            violations.extend(url_violations)

        return violations

    def is_query_operation(self, method_name, path, line_content):
        """判断是否为查询操作"""
        query_keywords = [
            'list', 'get', 'query', 'search', 'find', 'select', 'page',
            '列表', '查询', '搜索', '查找', '选择', '分页'
        ]

        # 检查方法名
        if any(keyword in method_name.lower() for keyword in query_keywords):
            return True

        # 检查路径
        if any(keyword in path.lower() for keyword in query_keywords):
            return True

        return False

    def is_update_operation(self, method_name, path, line_content):
        """判断是否为更新操作"""
        update_keywords = [
            'update', 'edit', 'modify', 'change', 'set', 'save',
            '更新', '编辑', '修改', '变更', '设置', '保存'
        ]

        if any(keyword in method_name.lower() for keyword in update_keywords):
            return True

        return False

    def is_delete_operation(self, method_name, path, line_content):
        """判断是否为删除操作"""
        delete_keywords = [
            'delete', 'remove', 'del', 'rm', 'destroy',
            '删除', '移除', '删除', '移除', '销毁'
        ]

        if any(keyword in method_name.lower() for keyword in delete_keywords):
            return True

        return False

    def is_status_update(self, method_name, path, line_content):
        """判断是否为状态更新"""
        status_keywords = [
            'status', 'state', 'enable', 'disable', 'activate', 'deactivate',
            '状态', '启用', '禁用', '激活', '停用'
        ]

        if any(keyword in method_name.lower() for keyword in status_keywords):
            return True

        if any(keyword in path.lower() for keyword in status_keywords):
            return True

        return False

    def check_url_design(self, path, method):
        """检查URL设计违规"""
        violations = []

        # 检查是否包含动词
        verb_keywords = ['get', 'set', 'create', 'update', 'delete', 'list', 'search']
        path_parts = path.strip('/').split('/')

        for part in path_parts:
            if part in verb_keywords:
                violations.append({
                    'type': 'URL_CONTAINS_VERB',
                    'description': f'URL包含动词 "{part}"',
                    'suggestion': f'移除URL中的动词，使用HTTP方法表达操作'
                })
                break

        # 检查是否为复数名词
        if not path.endswith('s') and not '{' in path and not path.endswith('}') and path != '':
            violations.append({
                'type': 'URL_NOT_PLURAL',
                'description': 'URL应该使用复数名词',
                'suggestion': f'将 "{path}" 改为复数形式'
            })

        return violations

    def extract_resource_name(self, path):
        """提取资源名称"""
        # 简单提取：取路径的最后一部分作为资源名
        parts = path.strip('/').split('/')
        if parts:
            return parts[-1].replace('{', '').replace('}', '')
        return 'resource'

    def generate_report(self):
        """生成违规报告"""
        total_violations = sum(len(controller['violations']) for controller in self.controllers)

        report = {
            'scan_time': datetime.now().isoformat(),
            'total_controllers': len(self.controllers),
            'total_violations': total_violations,
            'controllers': self.controllers,
            'violation_summary': self.get_violation_summary()
        }

        return report

    def get_violation_summary(self):
        """获取违规统计摘要"""
        summary = {}

        for controller in self.controllers:
            for violation in controller['violations']:
                violation_type = violation['type']
                summary[violation_type] = summary.get(violation_type, 0) + 1

        return summary

def main():
    """主函数"""
    print("🚀 开始RESTful API违规检测...")

    # 获取项目根目录
    project_root = os.path.dirname(os.path.abspath(__file__))

    # 创建扫描器
    scanner = RestfulViolationScanner(project_root)

    # 执行扫描
    controllers = scanner.scan_controllers()

    # 生成报告
    report = scanner.generate_report()

    # 输出统计信息
    print(f"\n📊 扫描统计:")
    print(f"总Controller文件: {report['total_controllers']}")
    print(f"总违规数量: {report['total_violations']}")

    if report['total_violations'] > 0:
        print(f"\n🚨 违规类型统计:")
        for violation_type, count in report['violation_summary'].items():
            print(f"  - {violation_type}: {count} 个")

        # 保存报告到文件
        with open('RESTFUL_VIOLATION_SCAN_RESULT.json', 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)

        print(f"\n📄 详细报告已保存到: RESTFUL_VIOLATION_SCAN_RESULT.json")
    else:
        print(f"\n✅ 未发现RESTful设计违规!")

    return report

if __name__ == "__main__":
    main()