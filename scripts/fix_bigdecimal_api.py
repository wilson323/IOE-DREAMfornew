#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修复已弃用的BigDecimal API
将 BigDecimal.ROUND_HALF_UP 替换为 RoundingMode.HALF_UP
"""

import os
import re
from pathlib import Path

# 项目根目录
PROJECT_ROOT = Path(r"D:\IOE-DREAM\smart-admin-api-java17-springboot3")

# 需要修复的文件列表
FILES_TO_FIX = [
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\AttendanceScheduleEntity.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\AttendanceStatisticsEntity.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\analysis\BehaviorAnalysis.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\dto\AccountDeductResult.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\dto\AccountValidationResult.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\dto\ConsumeModeResult.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\enums\CategoryDiscountEnum.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\enums\MemberLevelEnum.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\pattern\ConsumptionPattern.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\pattern\DevicePattern.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\pattern\LocationPattern.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\result\AmountAnomalyResult.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\result\FrequencyAnomalyResult.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\result\LocationAnomalyResult.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\result\SequenceAnomalyResult.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\vo\ConsumeStatistics.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\vo\LimitUsageReport.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\vo\LimitValidationResult.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\vo\NotificationStatistics.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\manager\ConsumeCacheManager.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\service\impl\ConsumeLimitConfigServiceImpl.java",
    r"sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\service\impl\ConsumeServiceImpl.java",
]

def fix_bigdecimal_api(file_path):
    """修复单个文件的BigDecimal API"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        original_content = content
        needs_import = False

        # 检查是否需要添加RoundingMode导入
        if 'BigDecimal.ROUND_HALF_UP' in content:
            needs_import = True

        # 替换 divide(BigDecimal, int, BigDecimal.ROUND_HALF_UP)
        content = re.sub(
            r'\.divide\(([^,]+),\s*(\d+),\s*BigDecimal\.ROUND_HALF_UP\)',
            r'.divide(\1, \2, RoundingMode.HALF_UP)',
            content
        )

        # 替换 setScale(int, BigDecimal.ROUND_HALF_UP)
        content = re.sub(
            r'\.setScale\((\d+),\s*BigDecimal\.ROUND_HALF_UP\)',
            r'.setScale(\1, RoundingMode.HALF_UP)',
            content
        )

        # 如果内容有变化，添加RoundingMode导入
        if content != original_content and needs_import:
            # 检查是否已有RoundingMode导入
            if 'import java.math.RoundingMode;' not in content:
                # 在BigDecimal导入后添加RoundingMode导入
                if 'import java.math.BigDecimal;' in content:
                    content = content.replace(
                        'import java.math.BigDecimal;',
                        'import java.math.BigDecimal;\nimport java.math.RoundingMode;'
                    )
                else:
                    # 如果没有BigDecimal导入，在package声明后添加
                    package_match = re.search(r'(package\s+[^;]+;)', content)
                    if package_match:
                        pos = package_match.end()
                        content = content[:pos] + '\n\nimport java.math.RoundingMode;' + content[pos:]

            # 保存文件
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        return False
    except Exception as e:
        print(f"Error fixing {file_path}: {e}")
        return False

def main():
    """主函数"""
    print("Starting batch fix for deprecated BigDecimal API...")
    fixed_count = 0

    for file_rel_path in FILES_TO_FIX:
        file_path = PROJECT_ROOT / file_rel_path
        if file_path.exists():
            if fix_bigdecimal_api(file_path):
                print(f"[OK] Fixed: {file_rel_path}")
                fixed_count += 1
            else:
                print(f"[-] Skip: {file_rel_path} (no changes needed)")
        else:
            print(f"[ERROR] File not found: {file_rel_path}")

    print(f"\nDone! Fixed {fixed_count} / {len(FILES_TO_FIX)} files")

if __name__ == '__main__':
    main()

