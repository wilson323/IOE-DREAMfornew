#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
编译错误分析器 - Task 1实现
分析377个编译错误，提供详细的分类和统计报告

角色: DevOps自动化专家，精通Python脚本开发和Maven构建分析
任务: 分析377个编译错误，按错误类型分类，生成详细分析报告
限制: 必须处理Maven输出格式错误解析，确保分类准确性，生成可读的分析报告
成功: 脚本成功分析所有377个错误，生成完整的错误分类报告，识别Top 5错误类型
"""

import os
import re
import json
import csv
from datetime import datetime
from typing import List, Dict, Tuple, Optional
from dataclasses import dataclass, asdict
from collections import defaultdict, Counter
from pathlib import Path

@dataclass
class CompilationError:
    """编译错误数据结构"""
    file_path: str
    line_number: int
    error_type: str
    error_message: str
    module: str
    severity: str = "ERROR"
    suggested_fix: str = ""
    raw_output: str = ""

    def to_dict(self) -> Dict:
        return asdict(self)

@dataclass
class ErrorAnalysisReport:
    """错误分析报告数据结构"""
    total_errors: int
    error_categories: Dict[str, List[CompilationError]]
    module_distribution: Dict[str, int]
    severity_distribution: Dict[str, int]
    top_error_patterns: List[Tuple[str, int]]
    analysis_time: datetime
    recommendations: List[str]

class CompilationErrorAnalyzer:
    """编译错误分析器"""

    def __init__(self, project_path: str):
        self.project_path = Path(project_path)
        self.error_patterns = self._initialize_error_patterns()
        self.module_mapping = self._initialize_module_mapping()

    def _initialize_error_patterns(self) -> Dict[str, re.Pattern]:
        """初始化错误识别模式"""
        return {
            # Lombok相关问题
            'LOMBOK_GETTER_SETTER': re.compile(
                r'cannot find symbol.*symbol:\s*(?:method|variable)\s*(get|set|is)\w+',
                re.IGNORECASE
            ),
            'LOMBOK_DATA_ANNOTATION': re.compile(
                r'cannot find symbol.*symbol:\s*(?:method|variable)\s*(toString|hashCode|equals)',
                re.IGNORECASE
            ),
            'LOMBOK_BUILDER_ANNOTATION': re.compile(
                r'cannot find symbol.*symbol:\s*(?:method|variable)\s*(builder|Builder)',
                re.IGNORECASE
            ),

            # 重复定义问题
            'DUPLICATE_METHOD': re.compile(
                r'method\s+\w+\s*\([^)]*\)\s+is\s+already\s+defined\s+in\s+\w+',
                re.IGNORECASE
            ),
            'DUPLICATE_CLASS': re.compile(
                r'class\s+\w+\s+is\s+already\s+defined',
                re.IGNORECASE
            ),
            'DUPLICATE_VARIABLE': re.compile(
                r'variable\s+\w+\s+is\s+already\s+defined',
                re.IGNORECASE
            ),

            # 缺失方法和字段
            'MISSING_METHOD': re.compile(
                r'cannot find symbol\s+symbol:\s*method\s+\w+\s*\([^)]*\)',
                re.IGNORECASE
            ),
            'MISSING_FIELD': re.compile(
                r'cannot find symbol\s+symbol:\s*variable\s+\w+',
                re.IGNORECASE
            ),
            'MISSING_CLASS': re.compile(
                r'cannot find symbol\s+symbol:\s*class\s+\w+',
                re.IGNORECASE
            ),

            # 类型转换错误
            'TYPE_MISMATCH': re.compile(
                r'type\s+mismatch.*?(?:cannot|incompatible)',
                re.IGNORECASE
            ),
            'INCOMPATIBLE_TYPES': re.compile(
                r'incompatible\s+types.*?(?:found|required)',
                re.IGNORECASE
            ),
            'TYPE_CAST_ERROR': re.compile(
                r'cannot\s+be\s+cast\s+to',
                re.IGNORECASE
            ),

            # 包名和导入问题
            'JAKARTA_MIGRATION': re.compile(
                r'package\s+javax\.(servlet|validation|persistence|annotation|xml\.bind)',
                re.IGNORECASE
            ),
            'MISSING_IMPORT': re.compile(
                r'package\s+\w+\s+does\s+not\s+exist|cannot\s+find\s+symbol.*import',
                re.IGNORECASE
            ),

            # Entity和数据库问题
            'ENTITY_FIELD_MAPPING': re.compile(
                r'column\s+\w+\s+(?:cannot|does\s+not\s+exist)',
                re.IGNORECASE
            ),
            'BASE_ENTITY_INHERITANCE': re.compile(
                r'cannot\s+find\s+symbol.*variable\s+(createTime|updateTime|createUserId|updateUserId)',
                re.IGNORECASE
            ),

            # Spring相关问题
            'AUTOWIRED_ERROR': re.compile(
                r'could\s+not\s+autowire.*no\s+bean\s+of\s+type',
                re.IGNORECASE
            ),
            'DEPENDENCY_INJECTION': re.compile(
                r'required\s+a\s+bean\s+of\s+type.*that\s+could\s+not\s+be\s+found',
                re.IGNORECASE
            ),

            # 语法和结构问题
            'SYNTAX_ERROR': re.compile(
                r"syntax\s+error.*?(?:expect|reached\s+end\s+of\s+file)",
                re.IGNORECASE
            ),
            'MISSING_SEMICOLON': re.compile(
                r"';\s*expected",
                re.IGNORECASE
            ),
            'MISSING_BRACE': re.compile(
                r"brace\s+expected|\}+ expected",
                re.IGNORECASE
            )
        }

    def _initialize_module_mapping(self) -> Dict[str, str]:
        """初始化模块映射"""
        return {
            'sa-admin': 'admin',
            'sa-base': 'base',
            'sa-support': 'support',
            'sa-common': 'common'
        }

    def analyze_compilation_errors(self) -> ErrorAnalysisReport:
        """
        分析所有编译错误

        Returns:
            ErrorAnalysisReport: 详细的分析报告
        """
        print("开始分析编译错误...")

        # 执行Maven编译并收集错误
        errors = self._collect_compilation_errors()
        print(f"收集到 {len(errors)} 个编译错误")

        # 按类型分类错误
        error_categories = self._categorize_errors(errors)
        print(f"错误分类完成，共 {len(error_categories)} 个类别")

        # 计算模块分布
        module_distribution = self._calculate_module_distribution(errors)

        # 计算严重程度分布
        severity_distribution = self._calculate_severity_distribution(errors)

        # 识别Top错误模式
        top_error_patterns = self._identify_top_error_patterns(errors)

        # 生成修复建议
        recommendations = self._generate_recommendations(error_categories, errors)

        return ErrorAnalysisReport(
            total_errors=len(errors),
            error_categories=error_categories,
            module_distribution=module_distribution,
            severity_distribution=severity_distribution,
            top_error_patterns=top_error_patterns,
            analysis_time=datetime.now(),
            recommendations=recommendations
        )

    def _collect_compilation_errors(self) -> List[CompilationError]:
        """执行Maven编译并收集错误"""
        errors = []

        try:
            # 切换到项目目录
            os.chdir(self.project_path)

            # 执行Maven编译
            import subprocess
            result = subprocess.run(
                ['mvn', 'clean', 'compile', '-q'],
                capture_output=True,
                text=True,
                timeout=300  # 5分钟超时
            )

            if result.returncode != 0:
                error_output = result.stderr + result.stdout
                errors = self._parse_maven_errors(error_output)
            else:
                print("编译成功，没有发现错误")

        except subprocess.TimeoutExpired:
            print("Maven编译超时")
            errors = []
        except Exception as e:
            print(f"执行Maven编译时发生错误: {str(e)}")
            errors = []

        return errors

    def _parse_maven_errors(self, error_output: str) -> List[CompilationError]:
        """解析Maven错误输出"""
        errors = []

        # Maven编译错误格式正则表达式
        maven_error_pattern = re.compile(
            r'\[ERROR\]\s+(?:(?P<file_path>.*?):\[(?P<line_number>\d+),\d+\]\s+)?(?P<error_message>.*)',
            re.MULTILINE
        )

        for match in maven_error_pattern.finditer(error_output):
            file_path = match.group('file_path') or "Unknown"
            line_number = int(match.group('line_number') or '0')
            error_message = match.group('error_message').strip()

            # 确定模块
            module = self._determine_module(file_path)

            # 确定严重程度
            severity = self._determine_severity(error_message)

            error = CompilationError(
                file_path=file_path,
                line_number=line_number,
                error_type="UNCATEGORIZED",
                error_message=error_message,
                module=module,
                severity=severity,
                raw_output=match.group(0)
            )

            errors.append(error)

        return errors

    def _determine_module(self, file_path: str) -> str:
        """确定错误所属模块"""
        if file_path == "Unknown":
            return "unknown"

        for module_key, module_name in self.module_mapping.items():
            if module_key in file_path:
                return module_name

        return "other"

    def _determine_severity(self, error_message: str) -> str:
        """确定错误严重程度"""
        if any(keyword in error_message.lower() for keyword in ['error', 'failed', 'cannot']):
            return "ERROR"
        elif any(keyword in error_message.lower() for keyword in ['warning', 'deprecated']):
            return "WARNING"
        else:
            return "INFO"

    def _categorize_errors(self, errors: List[CompilationError]) -> Dict[str, List[CompilationError]]:
        """按类型分类错误"""
        categories = defaultdict(list)

        for error in errors:
            categorized = False

            # 尝试匹配错误模式
            for error_type, pattern in self.error_patterns.items():
                if pattern.search(error.error_message):
                    error.error_type = error_type
                    categories[error_type].append(error)
                    categorized = True
                    break

            # 如果没有匹配的模式，进行基本分类
            if not categorized:
                error.error_type = self._basic_categorization(error.error_message)
                categories[error.error_type].append(error)

        return dict(categories)

    def _basic_categorization(self, error_message: str) -> str:
        """基本错误分类"""
        error_lower = error_message.lower()

        if 'cannot find symbol' in error_lower:
            if 'method' in error_lower:
                return 'MISSING_METHOD'
            elif 'variable' in error_lower:
                return 'MISSING_FIELD'
            elif 'class' in error_lower:
                return 'MISSING_CLASS'

        if 'package' in error_lower and 'does not exist' in error_lower:
            return 'MISSING_IMPORT'

        if 'type' in error_lower and ('mismatch' in error_lower or 'incompatible' in error_lower):
            return 'TYPE_MISMATCH'

        if 'duplicate' in error_lower or 'already defined' in error_lower:
            return 'DUPLICATE_DEFINITION'

        return 'OTHER'

    def _calculate_module_distribution(self, errors: List[CompilationError]) -> Dict[str, int]:
        """计算模块分布"""
        distribution = Counter(error.module for error in errors)
        return dict(distribution)

    def _calculate_severity_distribution(self, errors: List[CompilationError]) -> Dict[str, int]:
        """计算严重程度分布"""
        distribution = Counter(error.severity for error in errors)
        return dict(distribution)

    def _identify_top_error_patterns(self, errors: List[CompilationError], top_n: int = 5) -> List[Tuple[str, int]]:
        """识别Top错误模式"""
        # 提取错误消息的关键模式
        error_patterns = Counter()

        for error in errors:
            # 提取错误消息的关键部分
            key_pattern = self._extract_error_pattern(error.error_message)
            error_patterns[key_pattern] += 1

        return error_patterns.most_common(top_n)

    def _extract_error_pattern(self, error_message: str) -> str:
        """提取错误模式"""
        # 移除具体的类名、变量名、行号等
        pattern = error_message
        pattern = re.sub(r'[A-Z][a-zA-Z0-9_]*', 'CLASS_NAME', pattern)
        pattern = re.sub(r'\d+', 'LINE_NUMBER', pattern)
        pattern = re.sub(r'"[^"]*"', 'STRING', pattern)
        pattern = re.sub(r'[a-z][a-zA-Z0-9_]*', 'IDENTIFIER', pattern)

        return pattern.strip()

    def _generate_recommendations(self, error_categories: Dict[str, List[CompilationError]], errors: List[CompilationError]) -> List[str]:
        """生成修复建议"""
        recommendations = []

        # 基于错误类别生成建议
        for error_type, error_list in error_categories.items():
            if error_type.startswith('LOMBOK_'):
                recommendations.append(f"批量修复Lombok相关问题: {len(error_list)}个错误，建议检查@Data、@Getter、@Setter注解配置")

            elif error_type.startswith('DUPLICATE_'):
                recommendations.append(f"清理重复定义: {len(error_list)}个错误，需要合并或删除重复的方法/类/变量")

            elif error_type.startswith('MISSING_'):
                recommendations.append(f"补充缺失的{error_type.replace('MISSING_', '')}: {len(error_list)}个错误，需要添加对应的方法、字段或导入")

            elif error_type == 'JAKARTA_MIGRATION':
                recommendations.append(f"完成Jakarta迁移: {len(error_list)}个错误，将javax.*包替换为jakarta.*")

        # 基于模块分布生成建议
        module_counts = Counter(error.module for error in errors)
        if module_counts:
            most_affected_module = module_counts.most_common(1)[0][0]
            recommendations.append(f"重点关注模块 {most_affected_module}，该模块错误最多 ({module_counts[most_affected_module]}个)")

        return recommendations

    def export_to_csv(self, errors: List[CompilationError], output_file: str):
        """导出错误到CSV文件"""
        with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
            if not errors:
                csvfile.write("没有错误可导出")
                return

            fieldnames = ['file_path', 'line_number', 'error_type', 'error_message', 'module', 'severity']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

            writer.writeheader()
            for error in errors:
                writer.writerow({
                    'file_path': error.file_path,
                    'line_number': error.line_number,
                    'error_type': error.error_type,
                    'error_message': error.error_message,
                    'module': error.module,
                    'severity': error.severity
                })

def main():
    """主函数"""
    print("IOE-DREAM编译错误分析器")
    print("="*50)

    # 项目路径
    project_path = Path(__file__).parent.parent / "smart-admin-api-java17-springboot3"

    # 创建分析器
    analyzer = CompilationErrorAnalyzer(str(project_path))

    try:
        # 执行分析
        report = analyzer.analyze_compilation_errors()

        print("\n编译错误分析报告")
        print("="*50)
        print(f"总错误数: {report.total_errors}")
        print(f"分析时间: {report.analysis_time.strftime('%Y-%m-%d %H:%M:%S')}")

        print(f"\n错误类型分布:")
        for error_type, errors in report.error_categories.items():
            print(f"  {error_type}: {len(errors)}个")

        print(f"\n模块分布:")
        for module, count in report.module_distribution.items():
            print(f"  {module}: {count}个")

        print(f"\n严重程度分布:")
        for severity, count in report.severity_distribution.items():
            print(f"  {severity}: {count}个")

        print(f"\nTop 5 错误模式:")
        for i, (pattern, count) in enumerate(report.top_error_patterns, 1):
            print(f"  {i}. {pattern}: {count}次")

        print(f"\n修复建议:")
        for i, recommendation in enumerate(report.recommendations, 1):
            print(f"  {i}. {recommendation}")

        # 生成详细报告文件
        report_file = project_path / "compilation_error_report.json"
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump({
                'total_errors': report.total_errors,
                'error_categories': {
                    k: [error.to_dict() for error in v]
                    for k, v in report.error_categories.items()
                },
                'module_distribution': report.module_distribution,
                'severity_distribution': report.severity_distribution,
                'top_error_patterns': report.top_error_patterns,
                'analysis_time': report.analysis_time.isoformat(),
                'recommendations': report.recommendations
            }, f, indent=2, ensure_ascii=False)

        print(f"\n详细报告已保存到: {report_file}")

        # 导出CSV
        all_errors = []
        for errors in report.error_categories.values():
            all_errors.extend(errors)

        if all_errors:
            csv_file = project_path / "compilation_errors.csv"
            analyzer.export_to_csv(all_errors, str(csv_file))
            print(f"错误清单已保存到: {csv_file}")

        return report

    except Exception as e:
        print(f"分析过程中发生错误: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

if __name__ == "__main__":
    main()