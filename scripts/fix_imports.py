"""
批量修复导入路径错误
将错误的导入路径替换为正确的路径
"""
import re
from pathlib import Path
from typing import List, Tuple

# 导入路径映射表
IMPORT_MAPPINGS = {
    # RequireResource注解
    r'import\s+net\.lab1024\.sa\.base\.authz\.rac\.annotation\.RequireResource;':
        'import net.lab1024.sa.common.annotation.RequireResource;',
    r'import\s+net\.lab1024\.sa\.base\.module\.support\.rbac\.RequireResource;':
        'import net.lab1024.sa.common.annotation.RequireResource;',
    r'import\s+net\.lab1024\.sa\.base\.module\.support\.rbac\.annotation\.RequireResource;':
        'import net.lab1024.sa.common.annotation.RequireResource;',

    # PageResult
    r'import\s+net\.lab1024\.sa\.base\.common\.page\.PageResult;':
        'import net.lab1024.sa.common.domain.PageResult;',
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.PageResult;':
        'import net.lab1024.sa.common.domain.PageResult;',

    # PageParam
    r'import\s+net\.lab1024\.sa\.base\.common\.page\.PageParam;':
        'import net.lab1024.sa.common.domain.PageParam;',
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.PageParam;':
        'import net.lab1024.sa.common.domain.PageParam;',

    # PageForm
    r'import\s+net\.lab1024\.sa\.base\.common\.page\.PageForm;':
        'import net.lab1024.sa.common.domain.PageForm;',
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.PageForm;':
        'import net.lab1024.sa.common.domain.PageForm;',

    # ResponseDTO
    r'import\s+net\.lab1024\.sa\.base\.common\.response\.ResponseDTO;':
        'import net.lab1024.sa.common.domain.ResponseDTO;',
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.ResponseDTO;':
        'import net.lab1024.sa.common.domain.ResponseDTO;',

    # SmartResponseUtil
    r'import\s+net\.lab1024\.sa\.base\.common\.util\.SmartResponseUtil;':
        'import net.lab1024.sa.common.util.SmartResponseUtil;',

    # BusinessException
    r'import\s+net\.lab1024\.sa\.base\.common\.exception\.BusinessException;':
        'import net.lab1024.sa.common.exception.BusinessException;',

    # BaseEntity
    r'import\s+net\.lab1024\.sa\.base\.common\.entity\.BaseEntity;':
        'import net.lab1024.sa.common.entity.BaseEntity;',
    r'import\s+net\.lab1024\.base\.common\.entity\.BaseEntity;':
        'import net.lab1024.sa.common.entity.BaseEntity;',

    # SmartException
    r'import\s+net\.lab1024\.sa\.base\.common\.exception\.SmartException;':
        'import net.lab1024.sa.common.exception.SmartException;',

    # 注解类
    r'import\s+net\.lab1024\.sa\.base\.common\.annotation\.SaCheckPermission;':
        'import net.lab1024.sa.common.annotation.SaCheckPermission;',
    r'import\s+net\.lab1024\.sa\.base\.common\.annotation\.SaCheckLogin;':
        'import net.lab1024.sa.common.annotation.SaCheckLogin;',
    r'import\s+net\.lab1024\.sa\.base\.common\.annotation\.NoNeedLogin;':
        'import net.lab1024.sa.common.annotation.NoNeedLogin;',

    # 工具类
    r'import\s+net\.lab1024\.sa\.base\.common\.util\.SmartPageUtil;':
        'import net.lab1024.sa.common.util.SmartPageUtil;',
    r'import\s+net\.lab1024\.sa\.base\.common\.util\.SmartBeanUtil;':
        'import net.lab1024.sa.common.util.SmartBeanUtil;',
    r'import\s+net\.lab1024\.sa\.base\.common\.util\.SmartRequestUtil;':
        'import net.lab1024.sa.common.util.SmartRequestUtil;',
    r'import\s+net\.lab1024\.sa\.base\.common\.util\.SmartVerificationUtil;':
        'import net.lab1024.sa.common.util.SmartVerificationUtil;',
    r'import\s+net\.lab1024\.sa\.base\.common\.util\.SmartStringUtil;':
        'import net.lab1024.sa.common.util.SmartStringUtil;',
    r'import\s+net\.lab1024\.sa\.base\.common\.util\.SmartDateUtil;':
        'import net.lab1024.sa.common.util.SmartDateUtil;',
    r'import\s+net\.lab1024\.sa\.base\.common\.util\.SmartEnumUtil;':
        'import net.lab1024.sa.common.util.SmartEnumUtil;',

    # Controller基类
    r'import\s+net\.lab1024\.sa\.base\.common\.controller\.SupportBaseController;':
        'import net.lab1024.sa.common.controller.SupportBaseController;',

    # base.common.page.* -> common.domain.*
    r'import\s+net\.lab1024\.sa\.base\.common\.page\.PageParam;':
        'import net.lab1024.sa.common.domain.PageParam;',
    r'import\s+net\.lab1024\.sa\.base\.common\.page\.PageResult;':
        'import net.lab1024.sa.common.domain.PageResult;',
    r'import\s+net\.lab1024\.sa\.base\.common\.page\.PageForm;':
        'import net.lab1024.sa.common.domain.PageForm;',

    # base.common.device.* -> common.device.*
    r'import\s+net\.lab1024\.sa\.base\.common\.device\.DeviceConnectionTest;':
        'import net.lab1024.sa.common.device.DeviceConnectionTest;',
    r'import\s+net\.lab1024\.sa\.base\.common\.device\.DeviceDispatchResult;':
        'import net.lab1024.sa.common.device.DeviceDispatchResult;',

    # base.common.domain.* -> common.domain.* (测试文件中的导入)
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.ResponseDTO;':
        'import net.lab1024.sa.common.domain.ResponseDTO;',
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.PageParam;':
        'import net.lab1024.sa.common.domain.PageParam;',
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.PageResult;':
        'import net.lab1024.sa.common.domain.PageResult;',

    # admin.module.* -> 移除或注释（微服务架构中不存在）
    r'import\s+net\.lab1024\.sa\.admin\.module\.attendance\.([^;]+);':
        r'// TEMP: admin.module.attendance.\1 not available in microservices',
    r'import\s+net\.lab1024\.sa\.admin\.module\.smart\.([^;]+);':
        r'// TEMP: admin.module.smart.\1 not available in microservices',
    r'import\s+net\.lab1024\.sa\.admin\.module\.consume\.([^;]+);':
        r'// TEMP: admin.module.consume.\1 not available in microservices',

    # base.module.* -> 注释掉（微服务架构中不存在）
    r'import\s+net\.lab1024\.sa\.base\.module\.([^;]+);':
        r'// TEMP: base.module.\1 not available in microservices',

    # 其他常见导入修复
    r'import\s+net\.lab1024\.sa\.base\.config\.([^;]+);':
        r'import net.lab1024.sa.common.config.\1;',
    r'import\s+net\.lab1024\.base\.config\.([^;]+);':
        r'import net.lab1024.sa.common.config.\1;',
}

def fix_imports_in_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复文件中的导入路径

    Args:
        file_path: Java文件路径

    Returns:
        (是否修改, 修改说明列表)
    """
    if not file_path.exists() or not file_path.suffix == '.java':
        return False, []

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        # 尝试其他编码
        try:
            with open(file_path, 'r', encoding='gbk') as f:
                content = f.read()
        except:
            return False, []

    original = content
    changes = []

    # 应用所有映射
    for pattern, replacement in IMPORT_MAPPINGS.items():
        matches = re.findall(pattern, content)
        if matches:
            content = re.sub(pattern, replacement, content)
            changes.append(f"修复导入: {matches[0]} -> {replacement}")

    # 处理通用模式：base.common.* -> common.*
    def replacer(match):
        module = match.group(1)
        class_name = match.group(2)
        old_import = match.group(0)
        # 特殊处理：page -> domain
        if module == 'page':
            module = 'domain'
        new_import = f'import net.lab1024.sa.common.{module}.{class_name};'
        changes.append(f"修复导入: {old_import} -> {new_import}")
        return new_import

    generic_pattern = r'import\s+net\.lab1024\.sa\.base\.common\.([a-zA-Z]+)\.([a-zA-Z]+);'
    if re.search(generic_pattern, content):
        content = re.sub(generic_pattern, replacer, content)

    # 处理 net.lab1024.base.common.* -> net.lab1024.sa.common.*
    def replacer2(match):
        module = match.group(1)
        class_name = match.group(2)
        old_import = match.group(0)
        if module == 'page':
            module = 'domain'
        new_import = f'import net.lab1024.sa.common.{module}.{class_name};'
        changes.append(f"修复导入: {old_import} -> {new_import}")
        return new_import

    generic_pattern2 = r'import\s+net\.lab1024\.base\.common\.([a-zA-Z]+)\.([a-zA-Z]+);'
    if re.search(generic_pattern2, content):
        content = re.sub(generic_pattern2, replacer2, content)

    # 处理 admin.module.* -> 注释掉
    def replacer3(match):
        module_path = match.group(1)
        old_import = match.group(0)
        new_import = f'// TEMP: admin.module.{module_path} not available in microservices'
        changes.append(f"注释导入: {old_import}")
        return new_import

    admin_pattern = r'import\s+net\.lab1024\.sa\.admin\.module\.([^;]+);'
    if re.search(admin_pattern, content):
        content = re.sub(admin_pattern, replacer3, content)

    # 处理 base.module.* -> 注释掉
    def replacer4(match):
        module_path = match.group(1)
        old_import = match.group(0)
        new_import = f'// TEMP: base.module.{module_path} not available in microservices'
        changes.append(f"注释导入: {old_import}")
        return new_import

    base_module_pattern = r'import\s+net\.lab1024\.sa\.base\.module\.([^;]+);'
    if re.search(base_module_pattern, content):
        content = re.sub(base_module_pattern, replacer4, content)

    # 处理 base.config.* -> common.config.*
    def replacer5(match):
        class_name = match.group(1)
        old_import = match.group(0)
        new_import = f'import net.lab1024.sa.common.config.{class_name};'
        changes.append(f"修复导入: {old_import} -> {new_import}")
        return new_import

    config_pattern = r'import\s+net\.lab1024\.(sa\.)?base\.config\.([^;]+);'
    if re.search(config_pattern, content):
        content = re.sub(config_pattern, replacer5, content)

    if content != original:
        try:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True, changes
        except Exception as e:
            print(f"写入文件失败 {file_path}: {e}")
            return False, []

    return False, []

def main():
    """主函数"""
    microservices_dir = Path(__file__).parent.parent / "microservices"

    if not microservices_dir.exists():
        print(f"微服务目录不存在: {microservices_dir}")
        return

    print("开始修复导入路径...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有Java文件
    for java_file in microservices_dir.rglob("*.java"):
        # 跳过测试文件（先修复主要代码）
        if 'test' in str(java_file):
            continue

        modified, changes = fix_imports_in_file(java_file)
        if modified:
            fixed_files.append((java_file, changes))
            total_changes += len(changes)
            rel_path = java_file.relative_to(microservices_dir)
            print(f"\n修复: {rel_path}")
            for change in changes[:3]:  # 只显示前3个
                print(f"  - {change}")
            if len(changes) > 3:
                print(f"  ... 还有 {len(changes) - 3} 处修改")

    print("\n" + "=" * 80)
    print(f"修复完成！共修复 {len(fixed_files)} 个文件，{total_changes} 处修改")

    if len(fixed_files) > 20:
        print(f"\n（仅显示前20个文件，共 {len(fixed_files)} 个）")
        for java_file, changes in fixed_files[:20]:
            print(f"  - {java_file.relative_to(microservices_dir)}")
    else:
        print("\n修复的文件列表:")
        for java_file, changes in fixed_files:
            print(f"  - {java_file.relative_to(microservices_dir)}")

if __name__ == "__main__":
    main()

