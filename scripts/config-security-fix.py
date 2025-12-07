#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
IOE-DREAM 配置安全修复工具
功能：自动检测和修复配置文件中的安全问题，包括明文密码、默认账户等
使用方法：python config-security-fix.py [service-name]
"""

import os
import re
import sys
import yaml
import json
import argparse
from pathlib import Path
from typing import Dict, List, Tuple, Optional
from dataclasses import dataclass
from datetime import datetime

# 颜色输出类
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    PURPLE = '\033[0;35m'
    CYAN = '\033[0;36m'
    NC = '\033[0m'  # No Color

# 安全风险数据类
@dataclass
class SecurityRisk:
    file_path: str
    line_number: int
    risk_type: str
    risk_value: str
    suggestion: str
    severity: str  # HIGH, MEDIUM, LOW

# 配置安全检查器类
class ConfigSecurityChecker:

    # 敏感配置模式
    SENSITIVE_PATTERNS = {
        'plain_password': {
            'pattern': r'password\s*[:=]\s*["\']?([^"\'\s]{6,})["\']?',
            'severity': 'HIGH',
            'description': '明文密码'
        },
        'default_username': {
            'pattern': r'username\s*[:=]\s*["\']?(root|admin|sa|postgres)["\']?',
            'severity': 'MEDIUM',
            'description': '默认用户名'
        },
        'weak_password': {
            'pattern': r'password\s*[:=]\s*["\']?(123456|password|admin|root|111111|000000)["\']?',
            'severity': 'HIGH',
            'description': '弱密码'
        },
        'jdbc_plain': {
            'pattern': r'jdbc:[^:]*://[^:]*:([^@]*)@',
            'severity': 'HIGH',
            'description': 'JDBC连接字符串中的明文密码'
        },
        'empty_password': {
            'pattern': r'password\s*[:=]\s*["\']?\s*["\']?',
            'severity': 'MEDIUM',
            'description': '空密码'
        }
    }

    def __init__(self):
        self.risks: List[SecurityRisk] = []
        self.fix_count = 0

    def check_file(self, file_path: str) -> List[SecurityRisk]:
        """检查单个配置文件的安全风险"""
        risks = []

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                lines = content.split('\n')

                for line_num, line in enumerate(lines, 1):
                    for risk_name, risk_info in self.SENSITIVE_PATTERNS.items():
                        matches = re.finditer(risk_info['pattern'], line, re.IGNORECASE)
                        for match in matches:
                            risk = SecurityRisk(
                                file_path=file_path,
                                line_number=line_num,
                                risk_type=risk_info['description'],
                                risk_value=match.group(1) if match.groups() else match.group(0),
                                suggestion=self._get_suggestion(risk_name, match),
                                severity=risk_info['severity']
                            )
                            risks.append(risk)

        except Exception as e:
            print(f"{Colors.YELLOW}警告: 无法读取文件 {file_path}: {e}{Colors.NC}")

        return risks

    def _get_suggestion(self, risk_name: str, match: re.Match) -> str:
        """获取修复建议"""
        suggestions = {
            'plain_password': "使用环境变量或配置中心: ${DB_PASSWORD}",
            'default_username': "使用专用账户: ${DB_USERNAME}",
            'weak_password': "使用强密码并通过安全配置管理",
            'jdbc_plain': "使用配置连接池: ${SPRING_DATASOURCE_URL}",
            'empty_password': "设置强密码或禁用空密码认证"
        }
        return suggestions.get(risk_name, "请检查配置安全性")

    def check_directory(self, directory: str) -> List[SecurityRisk]:
        """检查目录中所有配置文件"""
        all_risks = []
        config_extensions = ['.yml', '.yaml', '.properties', '.conf', '.env']

        for root, dirs, files in os.walk(directory):
            for file in files:
                if any(file.endswith(ext) for ext in config_extensions):
                    file_path = os.path.join(root, file)
                    risks = self.check_file(file_path)
                    all_risks.extend(risks)

        self.risks = all_risks
        return all_risks

    def fix_config_file(self, file_path: str) -> int:
        """修复单个配置文件"""
        fix_count = 0

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            # 备份原文件
            backup_path = f"{file_path}.backup.{datetime.now().strftime('%Y%m%d_%H%M%S')}"
            with open(backup_path, 'w', encoding='utf-8') as f:
                f.write(content)

            print(f"{Colors.BLUE}已创建备份: {backup_path}{Colors.NC}")

            # 修复明文密码
            content = re.sub(
                r'(password\s*[:=]\s*["\']?)([^"\'\s{]+)(["\']?)',
                r'\1${DB_PASSWORD:ENC(\2)}\3',
                content,
                flags=re.IGNORECASE
            )

            # 修复默认用户名
            content = re.sub(
                r'(username\s*[:=]\s*["\']?)(root|admin|sa|postgres)(["\']?)',
                r'\1${DB_USERNAME:\2}\3',
                content,
                flags=re.IGNORECASE
            )

            # 修复弱密码
            weak_passwords = ['123456', 'password', 'admin', 'root', '111111', '000000']
            for weak_pwd in weak_passwords:
                content = re.sub(
                    rf'(password\s*[:=]\s*["\']?)({weak_pwd})(["\']?)',
                    r'\1${DB_PASSWORD:ENC(CHANGE_ME_STRONG_PASSWORD)}\3',
                    content,
                    flags=re.IGNORECASE
                )

            # 写入修复后的内容
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)

            fix_count = len([risk for risk in self.risks if risk.file_path == file_path])

        except Exception as e:
            print(f"{Colors.RED}错误: 无法修复文件 {file_path}: {e}{Colors.NC}")

        return fix_count

    def generate_security_report(self, output_dir: str = ".") -> str:
        """生成安全检查报告"""
        report_path = os.path.join(output_dir, f"config-security-report-{datetime.now().strftime('%Y%m%d_%H%M%S')}.md")

        with open(report_path, 'w', encoding='utf-8') as f:
            f.write("# IOE-DREAM 配置安全检查报告\n\n")
            f.write(f"**生成时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")

            # 统计信息
            high_risks = [r for r in self.risks if r.severity == 'HIGH']
            medium_risks = [r for r in self.risks if r.severity == 'MEDIUM']
            low_risks = [r for r in self.risks if r.severity == 'LOW']

            f.write("## 风险统计\n\n")
            f.write(f"| 风险等级 | 数量 |\n")
            f.write(f"|---------|------|\n")
            f.write(f"| 🔴 高危   | {len(high_risks)} |\n")
            f.write(f"| 🟡 中危   | {len(medium_risks)} |\n")
            f.write(f"| 🟢 低危   | {len(low_risks)} |\n")
            f.write(f"| **总计** | **{len(self.risks)}** |\n\n")

            # 风险详情
            f.write("## 风险详情\n\n")

            for risk in sorted(self.risks, key=lambda x: (x.severity, x.file_path)):
                severity_emoji = {'HIGH': '🔴', 'MEDIUM': '🟡', 'LOW': '🟢'}
                f.write(f"### {severity_emoji.get(risk.severity, '⚪')} {risk.risk_type}\n\n")
                f.write(f"**文件**: `{risk.file_path}:{risk.line_number}`\n\n")
                f.write(f"**风险值**: `{risk.risk_value}`\n\n")
                f.write(f"**修复建议**: {risk.suggestion}\n\n")
                f.write("---\n\n")

            # 修复建议
            f.write("## 系统性修复建议\n\n")
            f.write("### 1. 立即行动项\n")
            f.write("- [ ] 将所有明文密码替换为环境变量\n")
            f.write("- [ ] 集成Nacos配置中心实现配置加密\n")
            f.write("- [ ] 修改默认用户名和弱密码\n")
            f.write("- [ ] 实施配置文件访问权限控制\n\n")

            f.write("### 2. 中期改进项\n")
            f.write("- [ ] 部署配置扫描自动化工具\n")
            f.write("- [ ] 建立配置变更审批流程\n")
            f.write("- [ ] 实现配置版本管理和回滚机制\n\n")

            f.write("### 3. 长期安全措施\n")
            f.write("- [ ] 实施零信任安全架构\n")
            f.write("- [ ] 定期进行安全配置审计\n")
            f.write("- [ ] 建立安全配置知识库\n\n")

        return report_path

def print_logo():
    """打印工具logo"""
    print(f"""
{Colors.CYAN}╔══════════════════════════════════════╗
║     IOE-DREAM 配置安全修复工具      ║
║     Configuration Security Fixer    ║
║                                      ║
║  自动检测和修复配置文件安全问题      ║
║  支持明文密码、默认账户、弱密码检测  ║
╚══════════════════════════════════════╝{Colors.NC}
""")

def main():
    print_logo()

    parser = argparse.ArgumentParser(description='IOE-DREAM 配置安全修复工具')
    parser.add_argument('service', nargs='?', help='指定要检查的微服务名称')
    parser.add_argument('--all', action='store_true', help='检查所有微服务')
    parser.add_argument('--fix', action='store_true', help='自动修复发现的问题')
    parser.add_argument('--report', action='store_true', help='生成详细报告')
    parser.add_argument('--list', action='store_true', help='列出所有可用微服务')

    args = parser.parse_args()

    # 可用的微服务列表
    services = [
        'ioedream-common-service',
        'ioedream-access-service',
        'ioedream-attendance-service',
        'ioedream-consume-service',
        'ioedream-video-service',
        'ioedream-visitor-service',
        'ioedream-oa-service',
        'ioedream-device-comm-service',
        'ioedream-gateway-service',
        'microservices-common'
    ]

    if args.list:
        print(f"{Colors.BLUE}可用的微服务:{Colors.NC}")
        for service in services:
            print(f"  - {service}")
        return

    checker = ConfigSecurityChecker()
    directories_to_check = []

    if args.all:
        directories_to_check = [f"microservices/{service}" for service in services
                              if os.path.exists(f"microservices/{service}")]
    elif args.service:
        service_path = f"microservices/{args.service}"
        if os.path.exists(service_path):
            directories_to_check = [service_path]
        else:
            print(f"{Colors.RED}错误: 微服务 '{args.service}' 不存在{Colors.NC}")
            sys.exit(1)
    else:
        print(f"{Colors.YELLOW}请指定要检查的微服务或使用 --all 检查所有服务{Colors.NC}")
        print(f"使用 --list 查看所有可用微服务")
        sys.exit(1)

    print(f"{Colors.BLUE}开始检查配置安全...{Colors.NC}\n")

    total_risks = []
    for directory in directories_to_check:
        print(f"{Colors.CYAN}检查目录: {directory}{Colors.NC}")
        risks = checker.check_directory(directory)
        total_risks.extend(risks)

        if risks:
            high_risks = [r for r in risks if r.severity == 'HIGH']
            medium_risks = [r for r in risks if r.severity == 'MEDIUM']
            low_risks = [r for r in risks if r.severity == 'LOW']

            print(f"  发现 {len(risks)} 个风险:")
            print(f"    🔴 高危: {len(high_risks)}")
            print(f"    🟡 中危: {len(medium_risks)}")
            print(f"    🟢 低危: {len(low_risks)}")
        else:
            print(f"  {Colors.GREEN}✓ 未发现安全风险{Colors.NC}")
        print()

    # 显示发现的总体风险
    if total_risks:
        print(f"{Colors.YELLOW}总共发现 {len(total_risks)} 个配置安全风险:{Colors.NC}")

        high_risks = [r for r in total_risks if r.severity == 'HIGH']
        medium_risks = [r for r in total_risks if r.severity == 'MEDIUM']

        print(f"  🔴 高危: {len(high_risks)}")
        print(f"  🟡 中危: {len(medium_risks)}")

        if len(high_risks) > 0:
            print(f"\n{Colors.RED}高危风险详情:{Colors.NC}")
            for risk in high_risks[:10]:  # 显示前10个
                print(f"  {risk.file_path}:{risk.line_number} - {risk.risk_type}")

            if len(high_risks) > 10:
                print(f"  ... 还有 {len(high_risks) - 10} 个高危风险")
    else:
        print(f"{Colors.GREEN}✓ 所有配置文件都是安全的！{Colors.NC}")

    # 修复问题
    if args.fix and total_risks:
        print(f"\n{Colors.YELLOW}开始自动修复...{Colors.NC}")

        files_to_fix = set(risk.file_path for risk in total_risks)
        total_fixes = 0

        for file_path in files_to_fix:
            print(f"{Colors.CYAN}修复文件: {file_path}{Colors.NC}")
            fixes = checker.fix_config_file(file_path)
            total_fixes += fixes
            print(f"  {Colors.GREEN}✓ 修复了 {fixes} 个问题{Colors.NC}")

        print(f"\n{Colors.GREEN}总共修复了 {total_fixes} 个配置安全问题{Colors.NC}")
        print(f"{Colors.YELLOW}建议: 请验证修复后的配置并设置正确的环境变量{Colors.NC}")

    # 生成报告
    if args.report:
        report_path = checker.generate_security_report()
        print(f"\n{Colors.BLUE}详细报告已生成: {report_path}{Colors.NC}")

if __name__ == "__main__":
    main()