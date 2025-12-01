"""
错误分析脚本
用于解析erro.text中的JSON格式错误并分类统计
"""
import json
import re
from collections import defaultdict
from pathlib import Path
from typing import Dict, List, Any

def parse_error_file(file_path: str) -> List[Dict[str, Any]]:
    """解析错误文件

    Args:
        file_path: 错误文件路径

    Returns:
        错误列表
    """
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 尝试解析JSON数组
    try:
        errors = json.loads(content)
        return errors
    except json.JSONDecodeError:
        # 如果不是标准JSON，尝试逐行解析
        errors = []
        for line in content.split('\n'):
            line = line.strip()
            if line.startswith('{') and line.endswith('}'):
                try:
                    errors.append(json.loads(line))
                except:
                    pass
        return errors

def classify_errors(errors: List[Dict[str, Any]]) -> Dict[str, List[Dict[str, Any]]]:
    """分类错误

    Args:
        errors: 错误列表

    Returns:
        分类后的错误字典
    """
    classified = defaultdict(list)

    for error in errors:
        message = error.get('message', '')
        severity = error.get('severity', 0)
        resource = error.get('resource', '')

        # 只处理严重错误（severity 8）
        if severity != 8:
            continue

        # 分类错误
        if 'Missing artifact' in message:
            classified['missing_dependency'].append(error)
        elif 'cannot be resolved' in message or 'cannot resolve' in message:
            classified['import_error'].append(error)
        elif 'undefined' in message.lower() or 'is undefined' in message:
            classified['undefined_method'].append(error)
        elif 'The constructor' in message:
            classified['constructor_error'].append(error)
        elif 'The method' in message:
            classified['method_error'].append(error)
        elif 'The type' in message:
            classified['type_error'].append(error)
        else:
            classified['other'].append(error)

    return dict(classified)

def generate_report(classified: Dict[str, List[Dict[str, Any]]]) -> str:
    """生成分析报告

    Args:
        classified: 分类后的错误

    Returns:
        报告文本
    """
    report = []
    report.append("=" * 80)
    report.append("错误分析报告")
    report.append("=" * 80)
    report.append("")

    total_errors = sum(len(errors) for errors in classified.values())
    report.append(f"总错误数: {total_errors}")
    report.append("")

    # 按类型统计
    for error_type, errors in sorted(classified.items(), key=lambda x: len(x[1]), reverse=True):
        report.append(f"{error_type}: {len(errors)} 个")
        if errors:
            # 显示前5个示例
            for i, error in enumerate(errors[:5], 1):
                resource = error.get('resource', 'N/A')
                message = error.get('message', 'N/A')
                line = error.get('startLineNumber', 'N/A')
                report.append(f"  {i}. [{resource}:{line}] {message[:100]}")
            if len(errors) > 5:
                report.append(f"  ... 还有 {len(errors) - 5} 个类似错误")
        report.append("")

    return "\n".join(report)

def extract_unique_errors(classified: Dict[str, List[Dict[str, Any]]]) -> Dict[str, set]:
    """提取唯一错误模式

    Args:
        classified: 分类后的错误

    Returns:
        唯一错误模式字典
    """
    unique = {}

    for error_type, errors in classified.items():
        patterns = set()
        for error in errors:
            message = error.get('message', '')
            resource = error.get('resource', '')
            # 提取错误模式（去除具体路径和行号）
            pattern = re.sub(r':\d+', ':N', message)
            pattern = re.sub(r'/[^/]+/', '/PATH/', pattern)
            patterns.add((pattern, resource))
        unique[error_type] = patterns

    return unique

def main():
    """主函数"""
    error_file = Path(__file__).parent.parent / "erro.text"

    if not error_file.exists():
        print(f"错误文件不存在: {error_file}")
        return

    print("正在解析错误文件...")
    errors = parse_error_file(str(error_file))
    print(f"解析到 {len(errors)} 个错误记录")

    print("正在分类错误...")
    classified = classify_errors(errors)

    print("正在生成报告...")
    report = generate_report(classified)

    # 保存报告
    report_file = error_file.parent / "error_analysis_report.txt"
    with open(report_file, 'w', encoding='utf-8') as f:
        f.write(report)

    print(f"\n报告已保存到: {report_file}")
    print("\n" + report)

    # 提取唯一错误模式
    unique = extract_unique_errors(classified)
    print("\n唯一错误模式:")
    for error_type, patterns in unique.items():
        print(f"\n{error_type}: {len(patterns)} 种模式")
        for pattern, resource in list(patterns)[:10]:
            print(f"  - {pattern[:80]}")

if __name__ == "__main__":
    main()

