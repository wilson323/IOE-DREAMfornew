#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修复缺失类的脚本
基于编译错误自动生成缺失的VO/DTO类
"""

import os
import re
import json
from pathlib import Path

# 项目根目录
PROJECT_ROOT = Path(__file__).parent
SA_ADMIN_ROOT = PROJECT_ROOT / "sa-admin" / "src" / "main" / "java" / "net" / "lab1024" / "sa" / "admin" / "module" / "consume"

# 需要创建的类映射（精简版本，只包含最关键的类）
MISSING_CLASSES = {
    # 异常检测相关
    "AbnormalDetectionResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long detectionId;",
            "private Long userId;",
            "private String detectionType;",
            "private String riskLevel;",
            "private Double abnormalScore;",
            "private String description;",
            "private LocalDateTime detectionTime;",
            "private String status;"
        ]
    },
    "AccountSecurityResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String operation;",
            "private Boolean success;",
            "private String message;",
            "private LocalDateTime operationTime;"
        ]
    },
    "ConsumeLimitConfig": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long configId;",
            "private Long userId;",
            "private String limitType;",
            "private Double dailyLimit;",
            "private Double monthlyLimit;",
            "private Boolean enabled;",
            "private LocalDateTime createTime;"
        ]
    },
    "PaymentPasswordResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String operation;",
            "private Boolean success;",
            "private String message;",
            "private LocalDateTime operationTime;"
        ]
    },
    "DetectionRule": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long ruleId;",
            "private String ruleName;",
            "private String ruleType;",
            "private String conditionExpression;",
            "private Boolean enabled;",
            "private LocalDateTime createTime;"
        ]
    },
    "UserRiskScore": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private Double riskScore;",
            "private String riskLevel;",
            "private LocalDateTime calculatedTime;"
        ]
    },
    "AccountFreezeInfo": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String freezeReason;",
            "private LocalDateTime freezeTime;",
            "private String status;"
        ]
    }
}

def generate_java_class(class_name, class_info):
    """生成Java类代码"""
    package_name = class_info["package"]
    class_type = class_info["type"]
    fields = class_info["fields"]

    # 确定导入
    imports = []
    if "LocalDateTime" in str(fields):
        imports.append("java.time.LocalDateTime")
    if "BigDecimal" in str(fields):
        imports.append("java.math.BigDecimal")
    if "List" in str(fields):
        imports.append("java.util.List")
    if "Map" in str(fields):
        imports.append("java.util.Map")

    # 生成类内容
    class_content = f"""package {package_name};

import lombok.Data;
"""

    # 添加导入
    for imp in sorted(set(imports)):
        class_content += f"import {imp};\n"

    class_content += f"""

/**
 * {class_name}
 *
 * @author SmartAdmin Team
 * @date 2025-11-17
 */
@Data
public class {class_name} {{
"""

    # 添加字段
    for field in fields:
        class_content += f"    {field}\n"

    class_content += """
}
"""

    return class_content

def main():
    """主函数"""
    print("Starting batch generation of missing Java classes...")

    created_count = 0

    for class_name, class_info in MISSING_CLASSES.items():
        try:
            # 确定文件路径
            package_parts = class_info["package"].split(".")
            relative_path = "/".join(package_parts)

            # 创建目录
            dir_path = SA_ADMIN_ROOT / relative_path
            dir_path.mkdir(parents=True, exist_ok=True)

            # 生成文件路径
            file_path = dir_path / f"{class_name}.java"

            # 生成类内容
            class_content = generate_java_class(class_name, class_info)

            # 写入文件
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(class_content)

            print(f"Created class: {class_name} -> {file_path}")
            created_count += 1

        except Exception as e:
            print(f"Failed to create class: {class_name}, error: {e}")

    print(f"\nBatch generation completed! Created {created_count} classes")

if __name__ == "__main__":
    main()