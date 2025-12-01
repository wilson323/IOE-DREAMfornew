#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
权限标识提取和映射工具
用于自动提取后端权限标识并生成前端权限映射
"""

import os
import re
import json
import sys
from typing import Dict, List, Set, Tuple
from pathlib import Path

class PermissionExtractor:
    def __init__(self, backend_dir: str = "smart-admin-api-java17-springboot3",
                 frontend_dir: str = "smart-admin-web-javascript"):
        self.backend_dir = Path(backend_dir)
        self.frontend_dir = Path(frontend_dir)
        self.backend_permissions: Dict[str, Set[str]] = {}
        self.frontend_files: List[Path] = []

    def extract_backend_permissions(self) -> Dict[str, Set[str]]:
        """提取后端所有权限标识"""
        print("🔍 提取后端权限标识...")

        if not self.backend_dir.exists():
            print(f"❌ 后端目录不存在: {self.backend_dir}")
            return {}

        # 遍历所有Controller文件
        controller_files = list(self.backend_dir.rglob("*Controller.java"))
        print(f"找到 {len(controller_files)} 个Controller文件")

        for controller_file in controller_files:
            try:
                with open(controller_file, 'r', encoding='utf-8') as f:
                    content = f.read()

                # 提取权限注解
                matches = re.findall(r'@SaCheckPermission\("([^"]+)"\)', content)

                if matches:
                    # 确定模块名称
                    module = self._determine_module(controller_file)
                    if module not in self.backend_permissions:
                        self.backend_permissions[module] = set()
                    self.backend_permissions[module].update(matches)

                    print(f"  ✓ {controller_file.name}: {len(matches)} 权限")

            except Exception as e:
                print(f"❌ 读取文件失败 {controller_file}: {e}")

        # 转换为可序列化的格式
        result = {}
        for module, permissions in self.backend_permissions.items():
            result[module] = sorted(list(permissions))

        print(f"✅ 后端权限提取完成，共 {len(result)} 个模块")
        return result

    def _determine_module(self, controller_file: Path) -> str:
        """根据文件路径确定模块名称"""
        path_parts = controller_file.parts

        # 尝试从路径中提取模块名
        for part in path_parts:
            if part in ['module', 'admin']:
                continue
            if 'consume' in part.lower():
                return 'consume'
            elif 'attendance' in part.lower():
                return 'attendance'
            elif 'access' in part.lower() or 'door' in part.lower():
                return 'access'
            elif 'video' in part.lower() or 'surveillance' in part.lower():
                return 'video'
            elif 'device' in part.lower():
                return 'device'
            elif 'hr' in part.lower() or 'employee' in part.lower():
                return 'hr'
            elif 'oa' in part.lower() or 'document' in part.lower():
                return 'oa'
            elif 'cache' in part.lower():
                return 'cache'
            elif 'config' in part.lower() or 'support' in part.lower():
                return 'support'

        # 默认使用文件名
        return controller_file.stem.replace('Controller', '').lower()

    def analyze_frontend_files(self) -> List[Path]:
        """分析前端Vue文件"""
        print("🔍 分析前端Vue文件...")

        if not self.frontend_dir.exists():
            print(f"❌ 前端目录不存在: {self.frontend_dir}")
            return []

        vue_files = list(self.frontend_dir.rglob("*.vue"))
        self.frontend_files = [f for f in vue_files if 'views' in str(f)]

        print(f"找到 {len(self.frontend_files)} 个Vue页面文件")
        return self.frontend_files

    def generate_permission_mapping(self) -> Dict[str, Dict]:
        """生成权限映射表"""
        print("📋 生成权限映射表...")

        backend_perms = self.extract_backend_permissions()
        frontend_files = self.analyze_frontend_files()

        mapping = {
            "backend_permissions": backend_perms,
            "frontend_mapping": {},
            "missing_permissions": [],
            "recommendations": []
        }

        # 为每个前端文件生成权限映射建议
        for vue_file in frontend_files:
            relative_path = vue_file.relative_to(self.frontend_dir)
            file_key = str(relative_path)

            # 确定文件所属模块
            module = self._determine_frontend_module(relative_path)

            # 生成权限映射建议
            recommendations = self._generate_recommendations(vue_file, module, backend_perms)

            mapping["frontend_mapping"][file_key] = {
                "module": module,
                "recommendations": recommendations,
                "existing_permissions": self._get_existing_permissions(vue_file)
            }

        # 识别缺失的权限
        mapping["missing_permissions"] = self._find_missing_permissions(backend_perms, frontend_files)

        print("✅ 权限映射表生成完成")
        return mapping

    def _determine_frontend_module(self, relative_path: Path) -> str:
        """确定前端文件所属模块"""
        path_str = str(relative_path).lower()

        if 'consume' in path_str:
            return 'consume'
        elif 'attendance' in path_str:
            return 'attendance'
        elif 'access' in path_str or 'door' in path_str:
            return 'access'
        elif 'video' in path_str or 'surveillance' in path_str:
            return 'video'
        elif 'device' in path_str:
            return 'device'
        elif 'hr' in path_str or 'employee' in path_str:
            return 'hr'
        elif 'oa' in path_str or 'document' in path_str:
            return 'oa'
        elif 'cache' in path_str:
            return 'cache'
        elif 'support' in path_str or 'config' in path_str:
            return 'support'

        return 'unknown'

    def _generate_recommendations(self, vue_file: Path, module: str,
                                backend_perms: Dict[str, Set[str]]) -> List[Dict]:
        """为前端文件生成权限映射建议"""
        recommendations = []

        try:
            with open(vue_file, 'r', encoding='utf-8') as f:
                content = f.read()
        except Exception as e:
            print(f"❌ 读取前端文件失败 {vue_file}: {e}")
            return recommendations

        # 检查常见的操作和对应的权限
        operations = {
            '新增|添加|创建|开户': f"{module}:add",
            '编辑|修改|更新': f"{module}:update",
            '删除|移除': f"{module}:delete",
            '详情|查看|详情页': f"{module}:detail",
            '导出|下载': f"{module}:export",
            '导入|上传': f"{module}:import",
            '查询|搜索|列表': f"{module}:query",
            '启用|激活': f"{module}:enable",
            '禁用|停用': f"{module}:disable",
            '审批|审核': f"{module}:approve",
            '充值|缴费': f"{module}:recharge",
            '退款|退费': f"{module}:refund",
            '冻结|锁定': f"{module}:freeze",
            '解冻|解锁': f"{module}:unfreeze"
        }

        # 检查文件中是否包含相关操作
        for pattern, permission in operations.items():
            if re.search(pattern, content, re.IGNORECASE):
                # 检查后端是否有对应权限
                if self._permission_exists_in_backend(permission, backend_perms):
                    recommendations.append({
                        "operation": pattern,
                        "permission": permission,
                        "priority": "high",
                        "reason": f"检测到'{pattern}'操作，建议添加权限控制"
                    })
                else:
                    recommendations.append({
                        "operation": pattern,
                        "permission": permission,
                        "priority": "medium",
                        "reason": f"检测到'{pattern}'操作，但后端未找到对应权限标识"
                    })

        return recommendations

    def _permission_exists_in_backend(self, permission: str,
                                    backend_perms: Dict[str, Set[str]]) -> bool:
        """检查权限是否在后端存在"""
        module = permission.split(':')[0]
        if module in backend_perms:
            return permission in backend_perms[module]
        return False

    def _get_existing_permissions(self, vue_file: Path) -> List[str]:
        """获取文件中已存在的权限控制"""
        try:
            with open(vue_file, 'r', encoding='utf-8') as f:
                content = f.read()

            # 提取v-permission指令
            matches = re.findall(r'v-permission="\[([^\]]+)"\]', content)
            existing_perms = []

            for match in matches:
                # 解析权限标识
                perms = [p.strip().strip("'\"") for p in match.split(',')]
                existing_perms.extend(perms)

            return list(set(existing_perms))
        except Exception:
            return []

    def _find_missing_permissions(self, backend_perms: Dict[str, Set[str]],
                                frontend_files: List[Path]) -> List[str]:
        """识别在前端缺失的权限控制"""
        all_frontend_perms = set()

        for vue_file in frontend_files:
            existing_perms = self._get_existing_permissions(vue_file)
            all_frontend_perms.update(existing_perms)

        all_backend_perms = set()
        for module_perms in backend_perms.values():
            all_backend_perms.update(module_perms)

        missing = all_backend_perms - all_frontend_perms
        return sorted(list(missing))

    def save_mapping(self, mapping: Dict, output_file: str = "permission_mapping.json"):
        """保存权限映射到文件"""
        try:
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(mapping, f, indent=2, ensure_ascii=False)
            print(f"✅ 权限映射已保存到: {output_file}")
        except Exception as e:
            print(f"❌ 保存权限映射失败: {e}")

    def generate_fix_script(self, mapping: Dict, script_file: str = "auto_permission_fix.sh"):
        """生成自动修复脚本"""
        print("🔧 生成自动修复脚本...")

        script_content = """#!/bin/bash

# 自动权限控制修复脚本
# 由 permission_extractor.py 自动生成

set -e

echo "🚀 开始自动权限控制修复..."

FRONTEND_DIR="smart-admin-web-javascript"
FIXED_COUNT=0
TOTAL_CHANGES=0

"""

        # 为每个前端文件生成修复命令
        for file_path, file_data in mapping["frontend_mapping"].items():
            recommendations = file_data.get("recommendations", [])
            if not recommendations:
                continue

            script_content += f"""
# 修复文件: {file_path}
echo "🔧 修复: {file_path}"
"""

            for rec in recommendations:
                if rec["priority"] == "high":
                    permission = rec["permission"]
                    operation_pattern = rec["operation"]

                    # 生成sed命令
                    script_content += f"""
# 添加 {rec['operation']} 权限控制
sed -i 's/@click="[^"]*"[^>]*>/& v-permission="['\\'{permission}\\']]/g' "$FRONTEND_DIR/{file_path}"
"""

                    TOTAL_CHANGES += 1

            FIXED_COUNT += 1

        # 添加验证部分
        script_content += f"""

echo "🔍 验证修复结果..."
vue_with_perm=$(find "$FRONTEND_DIR/src/views" -name "*.vue" -exec grep -l "v-permission" {{}} \\; | wc -l)
total_vue=$(find "$FRONTEND_DIR/src/views" -name "*.vue" | wc -l)
coverage=$(awk "BEGIN {{printf \\\"%.1f\\\", $vue_with_perm * 100 / $total_vue}}")

echo "📊 修复结果："
echo "   修复文件数: {FIXED_COUNT}"
echo "   权限指令数: {TOTAL_CHANGES}"
echo "   覆盖率: $coverage%"

echo "🎉 自动修复完成！"
"""

        try:
            with open(script_file, 'w', encoding='utf-8') as f:
                f.write(script_content)
            os.chmod(script_file, 0o755)
            print(f"✅ 自动修复脚本已生成: {script_file}")
        except Exception as e:
            print(f"❌ 生成修复脚本失败: {e}")


def main():
    """主函数"""
    print("🚀 IOE-DREAM 权限标识提取工具")
    print("=" * 50)

    # 创建提取器实例
    extractor = PermissionExtractor()

    # 生成权限映射
    mapping = extractor.generate_permission_mapping()

    # 保存映射文件
    extractor.save_mapping(mapping, "permission_mapping.json")

    # 生成修复脚本
    extractor.generate_fix_script(mapping, "auto_permission_fix.sh")

    # 输出统计信息
    backend_perms = mapping.get("backend_permissions", {})
    frontend_mapping = mapping.get("frontend_mapping", {})
    missing_perms = mapping.get("missing_permissions", [])

    print("\n" + "=" * 50)
    print("📊 提取统计信息:")
    print(f"   后端模块数: {len(backend_perms)}")
    print(f"   前端文件数: {len(frontend_mapping)}")
    print(f"   缺失权限数: {len(missing_perms)}")

    if missing_perms:
        print(f"\n⚠️  缺失的权限控制:")
        for perm in missing_perms[:10]:  # 只显示前10个
            print(f"   - {perm}")
        if len(missing_perms) > 10:
            print(f"   ... 还有 {len(missing_perms) - 10} 个权限")

    print("\n✅ 权限提取完成！")
    print("📋 下一步操作:")
    print("   1. 查看权限映射: cat permission_mapping.json")
    print("   2. 执行自动修复: ./auto_permission_fix.sh")
    print("   3. 验证修复结果: ./scripts/check-permission-coverage.sh")


if __name__ == "__main__":
    main()