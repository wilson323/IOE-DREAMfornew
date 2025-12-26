#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM 代码质量综合检查脚本
"""

import os
import re
import subprocess
from pathlib import Path
from datetime import datetime
from collections import defaultdict

class QualityAnalyzer:
    def __init__(self, project_path):
        self.project_path = Path(project_path)
        self.results = {
            'total_files': 0,
            'encoding_issues': [],
            'logging_issues': [],
            'complexity_issues': [],
            'comment_coverage': 0,
            'code_smells': []
        }

    def count_java_files(self):
        """统计Java文件数量"""
        java_files = list(self.project_path.rglob("*.java"))
        self.results['total_files'] = len(java_files)
        return len(java_files)

    def check_encoding(self):
        """检查UTF-8编码规范性"""
        print("检查UTF-8编码...")
        utf8_count = 0
        non_utf8_files = []

        for java_file in self.project_path.rglob("*.java"):
            try:
                with open(java_file, 'r', encoding='utf-8') as f:
                    f.read()
                utf8_count += 1
            except UnicodeDecodeError:
                non_utf8_files.append(str(java_file))

        self.results['encoding_compliance'] = int((utf8_count / self.results['total_files']) * 100)
        self.results['encoding_issues'] = non_utf8_files[:20]  # 只保留前20个

        return utf8_count, len(non_utf8_files)

    def check_logging(self):
        """检查日志规范"""
        print("检查日志规范...")
        slf4j_count = 0
        loggerfactory_files = []

        for java_file in self.project_path.rglob("*.java"):
            try:
                content = java_file.read_text(encoding='utf-8', errors='ignore')
                if '@Slf4j' in content:
                    slf4j_count += 1
                if 'LoggerFactory.getLogger' in content:
                    loggerfactory_files.append(str(java_file))
            except:
                pass

        total_logging = slf4j_count + len(loggerfactory_files)
        if total_logging > 0:
            self.results['logging_compliance'] = int((slf4j_count / total_logging) * 100)
        else:
            self.results['logging_compliance'] = 0

        self.results['slf4j_count'] = slf4j_count
        self.results['logging_issues'] = loggerfactory_files[:20]

        return slf4j_count, len(loggerfactory_files)

    def check_comments(self):
        """检查注释完整性"""
        print("检查注释完整性...")
        javadoc_count = 0

        for java_file in self.project_path.rglob("*.java"):
            try:
                content = java_file.read_text(encoding='utf-8', errors='ignore')
                if '/**' in content:
                    javadoc_count += 1
            except:
                pass

        self.results['comment_coverage'] = int((javadoc_count / self.results['total_files']) * 100)
        return javadoc_count

    def check_complexity(self):
        """检查代码复杂度"""
        print("检查代码复杂度...")
        large_files_500 = []
        large_files_1000 = []

        for java_file in self.project_path.rglob("*.java"):
            try:
                lines = len(java_file.read_text(encoding='utf-8', errors='ignore').splitlines())
                if lines > 1000:
                    large_files_1000.append((str(java_file), lines))
                elif lines > 500:
                    large_files_500.append((str(java_file), lines))
            except:
                pass

        # 按行数排序
        large_files_500.sort(key=lambda x: x[1], reverse=True)
        large_files_1000.sort(key=lambda x: x[1], reverse=True)

        self.results['large_files_500'] = large_files_500[:10]
        self.results['large_files_1000'] = large_files_1000[:10]
        self.results['complexity_score'] = max(0, 100 - (len(large_files_500) * 100 // self.results['total_files']) * 2)

        return len(large_files_500), len(large_files_1000)

    def check_code_smells(self):
        """检查代码异味"""
        print("检查代码异味...")
        system_out_count = 0
        printstack_count = 0

        for java_file in self.project_path.rglob("*.java"):
            try:
                content = java_file.read_text(encoding='utf-8', errors='ignore')
                system_out_count += content.count('System.out.println')
                printstack_count += content.count('printStackTrace')
            except:
                pass

        self.results['system_out_count'] = system_out_count
        self.results['printstack_count'] = printstack_count

        return system_out_count, printstack_count

    def analyze_modules(self):
        """分析各模块统计信息"""
        print("分析各模块...")
        modules = {}

        for module_dir in self.project_path.glob("ioedream-*/"):
            module_name = module_dir.name
            java_files = list(module_dir.rglob("*.java"))
            file_count = len(java_files)
            total_lines = 0

            for jf in java_files:
                try:
                    total_lines += len(jf.read_text(encoding='utf-8', errors='ignore').splitlines())
                except:
                    pass

            avg_lines = total_lines // file_count if file_count > 0 else 0
            modules[module_name] = {
                'files': file_count,
                'lines': total_lines,
                'avg_lines': avg_lines
            }

        self.results['modules'] = modules
        return modules

    def calculate_score(self):
        """计算综合质量评分"""
        encoding_score = self.results.get('encoding_compliance', 100)
        logging_score = self.results.get('logging_compliance', 100)
        comment_score = self.results.get('comment_coverage', 0)
        complexity_score = self.results.get('complexity_score', 100)

        # 加权平均
        total_score = (
            encoding_score * 20 +
            logging_score * 25 +
            comment_score * 25 +
            complexity_score * 30
        ) // 100

        self.results['total_score'] = total_score
        return total_score

    def generate_report(self):
        """生成质量报告"""
        score = self.results['total_score']

        if score >= 90:
            grade = "优秀 (A)"
        elif score >= 80:
            grade = "良好 (B)"
        elif score >= 70:
            grade = "中等 (C)"
        else:
            grade = "需改进 (D)"

        report = f"""
# IOE-DREAM 代码质量综合分析报告

**分析时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
**项目路径**: {self.project_path}

---

## 项目概览

- **总Java文件数**: {self.results['total_files']}

---

## 1. UTF-8编码规范性

- **合规率**: {self.results.get('encoding_compliance', 0)}%
- **非UTF-8文件数**: {len(self.results.get('encoding_issues', []))}

"""

        if self.results.get('encoding_issues'):
            report += f"\n### ⚠️ 发现非UTF-8文件（前20个）\n\n```\n"
            for issue in self.results['encoding_issues'][:20]:
                report += f"{issue}\n"
            report += "```\n\n"

        report += f"""
## 2. 日志规范检查

- **使用@Slf4j注解**: {self.results.get('slf4j_count', 0)} ✅
- **使用LoggerFactory**: {len(self.results.get('logging_issues', []))} ❌
- **合规率**: {self.results.get('logging_compliance', 0)}%

"""

        if self.results.get('logging_issues'):
            report += f"\n### ❌ 违规文件（前20个）\n\n```\n"
            for issue in self.results['logging_issues'][:20]:
                report += f"{issue}\n"
            report += "```\n\n"

        report += f"""
## 3. 注释完整性

- **注释覆盖率**: {self.results.get('comment_coverage', 0)}%
- **有JavaDoc注释的文件**: {int(self.results.get('comment_coverage', 0) * self.results['total_files'] / 100)}

---

## 4. 代码复杂度分析

- **超大文件(>500行)**: {len(self.results.get('large_files_500', []))}
- **超大文件(>1000行)**: {len(self.results.get('large_files_1000', []))}
- **复杂度得分**: {self.results.get('complexity_score', 0)}/100

"""

        if self.results.get('large_files_1000'):
            report += f"\n### 🔴 超大文件列表（Top 10，>1000行）\n\n```\n"
            for filepath, lines in self.results['large_files_1000'][:10]:
                report += f"{lines:5d} {filepath}\n"
            report += "```\n\n"

        report += f"""
## 5. 代码异味检查

- **System.out.println使用**: {self.results.get('system_out_count', 0)} ❌
- **printStackTrace使用**: {self.results.get('printstack_count', 0)} ❌

---

## 6. 服务模块统计

| 服务模块 | 文件数 | 代码行数 | 平均行/文件 |
|---------|-------|---------|------------|
"""

        for mod_name, mod_info in self.results.get('modules', {}).items():
            report += f"| {mod_name} | {mod_info['files']} | {mod_info['lines']} | {mod_info['avg_lines']} |\n"

        report += f"""
---

## 7. 综合质量评分

### 分项得分

- **编码规范（权重20%）**: {self.results.get('encoding_compliance', 0)}/100
- **日志规范（权重25%）**: {self.results.get('logging_compliance', 0)}/100
- **注释完整（权重25%）**: {self.results.get('comment_coverage', 0)}/100
- **代码复杂度（权重30%）**: {self.results.get('complexity_score', 0)}/100

### 总体评分

# **{score}/100** - {grade}

---

## 8. 质量改进建议

### P0级别（立即修复）

"""

        if self.results.get('logging_issues'):
            report += f"1. **日志规范问题**\n   - 修复{len(self.results['logging_issues'])}个日志规范违规文件\n   - 将所有`LoggerFactory.getLogger`替换为`@Slf4j`注解\n\n"

        if self.results.get('printstack_count', 0) > 0:
            report += f"2. **异常处理问题**\n   - 移除{self.results['printstack_count']}处`printStackTrace`使用\n   - 使用日志框架记录异常\n\n"

        report += f"""
### P1级别（高优先级）

1. **代码复杂度优化**
   - 重构{len(self.results.get('large_files_500', []))}个超大文件（>500行）
   - 目标: 单文件不超过300行
   - 目标: 单方法不超过50行

2. **注释完善**
   - 当前覆盖率: {self.results.get('comment_coverage', 0)}%
   - 目标覆盖率: ≥80%

### P2级别（中优先级）

1. **性能优化**
   - 移除{self.results.get('system_out_count', 0)}处`System.out.println`
   - 优化循环中的字符串拼接
   - 检查资源泄漏风险

---

**报告生成时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
"""

        return report

def main():
    project_path = "/d/IOE-DREAM/microservices"

    print("=" * 50)
    print("IOE-DREAM 代码质量综合检查")
    print("=" * 50)
    print()

    analyzer = QualityAnalyzer(project_path)

    # 执行各项检查
    analyzer.count_java_files()
    print(f"总Java文件数: {analyzer.results['total_files']}")
    print()

    analyzer.check_encoding()
    analyzer.check_logging()
    analyzer.check_comments()
    analyzer.check_complexity()
    analyzer.check_code_smells()
    analyzer.analyze_modules()

    # 计算总分
    score = analyzer.calculate_score()

    print()
    print("=" * 50)
    print(f"综合质量评分: {score}/100")
    print("=" * 50)
    print()

    # 生成报告
    report = analyzer.generate_report()
    report_path = Path("/d/IOE-DREAM/code-quality-reports/quality-summary-report.md")
    report_path.parent.mkdir(parents=True, exist_ok=True)

    with open(report_path, 'w', encoding='utf-8') as f:
        f.write(report)

    print(f"报告已保存: {report_path}")
    print()

    return report

if __name__ == "__main__":
    main()
