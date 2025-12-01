#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM System Service UTF-8编码修复工具
修复Java文件中的中文编码问题
"""

import os
import re
from pathlib import Path

def fix_java_encoding(file_path):
    """修复单个Java文件的UTF-8编码问题"""
    try:
        # 读取文件内容
        with open(file_path, 'r', encoding='utf-8', errors='replace') as f:
            content = f.read()

        # 修复常见的编码问题
        fixes = [
            # 修复截断的中文字符
            (r'部门负责人姓�?\?', '部门负责人姓名'),
            (r'部门电话格式不正�?\?', '部门电话格式不正确'),
            (r'部门邮箱格式不正�?\?', '部门邮箱格式不正确'),
            (r'性别�?�?2�?', '性别：1-男，2-女'),
            (r'手机�?\s*\*\*\/', '手机号码'),
            (r'工号�?\s*\*\/', '工号'),
            (r'入职日期�?\s*\*\/', '入职日期'),
            (r'离职日期�?\s*\*\/', '离职日期'),
            (r'部门名称�?\s*\*\/', '部门名称'),
            (r'员工状态�?\s*\*\/', '员工状态'),
            (r'员工类型�?\s*\*\/', '员工类型'),
            (r'工作地点�?\s*\*\/', '工作地点'),
            (r'住址�?\s*\*\/', '住址'),
            (r'籍贯�?\s*\*\/', '籍贯'),
            (r'学历�?\s*\*\/', '学历'),
            (r'专业�?\s*\*\/', '专业'),
            (r'毕业院校�?\s*\*\/', '毕业院校'),
            (r'政治面貌�?\s*\*\/', '政治面貌'),
            (r'婚姻状况�?\s*\*\/', '婚姻状况'),
            (r'紧急联系人�?\s*\*\/', '紧急联系人'),
            (r'紧急电话�?\s*\*\/', '紧急电话'),
            (r'备注�?\s*\*\/', '备注'),
            (r'创建时间�?\s*\*\/', '创建时间'),
            (r'更新时间�?\s*\*\/', '更新时间'),
            (r'删除标记�?\s*\*\/', '删除标记'),
            (r'是否启用�?\s*\*\/', '是否启用'),
            # 修复未结束的字符串
            (r'"([^"]*?)\?\s*\*\/', r'"\1"'),
            (r"'([^']*?)\?\s*\*\/", r"'\1'"),
        ]

        # 应用修复
        for old, new in fixes:
            content = content.replace(old, new)

        # 修复Validation注解中的未结束字符串
        content = fix_validation_annotations(content)

        # 写回文件，确保使用UTF-8编码
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

        return True, f"修复成功: {file_path}"

    except Exception as e:
        return False, f"修复失败 {file_path}: {str(e)}"

def fix_validation_annotations(content):
    """修复Validation注解中的字符串问题"""
    # 修复常见的验证注解模式
    patterns = [
        (r'@NotBlank\(message\s*=\s*("[^"]*?)([^"]*?)\?\s*\*\/', r'\1\2")'),
        (r'@Email\(message\s*=\s*("[^"]*?)([^"]*?)\?\s*\*\/', r'\1\2")'),
        (r'@Pattern\(regexp\s*=\s*("[^"]*?)([^"]*?)\s*,\s*message\s*=\s*("[^"]*?)([^"]*?)\?\s*\*\/', r'\1\2", message = \3\4")'),
        (r'@NotNull\(message\s*=\s*("[^"]*?)([^"]*?)\?\s*\*\/', r'\1\2")'),
        (r'@Size\(.*?message\s*=\s*("[^"]*?)([^"]*?)\?\s*\*\/', r'\1\2")'),
    ]

    for pattern, replacement in patterns:
        content = re.sub(pattern, replacement, content, flags=re.MULTILINE | re.DOTALL)

    return content

def main():
    """主函数"""
    base_dir = Path("D:/IOE-DREAM/microservices/ioedream-system-service/src/main/java")

    if not base_dir.exists():
        print("❌ System Service目录不存在")
        return

    print("开始修复System Service UTF-8编码问题...")

    # 需要修复的文件列表
    target_files = [
        "net/lab1024/sa/system/department/domain/form/DepartmentAddForm.java",
        "net/lab1024/sa/system/department/domain/form/DepartmentUpdateForm.java",
        "net/lab1024/sa/system/department/domain/entity/DepartmentEntity.java",
        "net/lab1024/sa/system/employee/domain/entity/EmployeeEntity.java",
        "net/lab1024/sa/system/employee/domain/form/EmployeeAddForm.java",
        "net/lab1024/sa/system/employee/domain/form/EmployeeUpdateForm.java",
        "net/lab1024/sa/system/employee/domain/form/EmployeeQueryForm.java",
        "net/lab1024/sa/system/employee/domain/vo/EmployeeVO.java",
        "net/lab1024/sa/system/employee/service/EmployeeService.java",
        "net/lab1024/sa/system/employee/service/impl/EmployeeServiceImpl.java",
        "net/lab1024/sa/system/employee/manager/EmployeeManager.java",
        "net/lab1024/sa/system/role/domain/vo/RoleVO.java",
        "net/lab1024/sa/system/role/service/RoleService.java",
        "net/lab1024/sa/system/menu/service/MenuService.java",
        "net/lab1024/sa/system/menu/domain/form/MenuAddForm.java",
        "net/lab1024/sa/system/dict/service/DictDataService.java",
        "net/lab1024/sa/system/domain/vo/DictTypeVO.java",
        "net/lab1024/sa/system/domain/form/DictQueryForm.java",
        "net/lab1024/s/system/domain/form/DictDataUpdateForm.java",
        "net/lab1024/sa/system/domain/form/DictTypeAddForm.java",
        "net/lab1024/sa/system/domain/vo/DepartmentVO.java",
        "net/lab1024/s/system/dao/UnifiedDeviceDao.java",
        "net/lab1024/sa/system/service/UnifiedDeviceService.java",
        "net/lab1024/sa/system/service/impl/UnifiedDeviceServiceImpl.java",
    ]

    success_count = 0
    error_count = 0

    for file_path in target_files:
        full_path = base_dir / file_path
        if full_path.exists():
            success, message = fix_java_encoding(full_path)
            if success:
                print(f"✅ {message}")
                success_count += 1
            else:
                print(f"❌ {message}")
                error_count += 1
        else:
            print(f"⚠️  文件不存在: {file_path}")

    print(f"\n📊 修复结果统计:")
    print(f"   成功: {success_count} 个文件")
    print(f"   失败: {error_count} 个文件")
    print(f"   总计: {success_count + error_count} 个文件")

    print("\n🔍 验证修复效果...")
    os.chdir("D:/IOE-DREAM/microservices/ioedream-system-service")
    import subprocess
    result = subprocess.run(["mvn", "clean", "compile", "-DskipTests"],
                          capture_output=True, text=True, encoding='utf-8')

    if result.returncode == 0:
        print("🎉 编译成功！UTF-8编码问题已修复")
    else:
        print("❌ 编译仍有问题，请查看详细信息")
        print(f"错误信息: {result.stderr[:500]}...")

if __name__ == "__main__":
    main()