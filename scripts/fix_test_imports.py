"""
批量修复测试文件中的导入路径错误
专门处理测试文件中的base.common.domain导入
"""
import re
from pathlib import Path
from typing import List, Tuple

# 测试文件导入路径映射表
TEST_IMPORT_MAPPINGS = {
    # ResponseDTO
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.ResponseDTO;':
        'import net.lab1024.sa.common.domain.ResponseDTO;',

    # PageParam
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.PageParam;':
        'import net.lab1024.sa.common.domain.PageParam;',
    r'import\s+net\.lab1024\.sa\.base\.common\.page\.PageParam;':
        'import net.lab1024.sa.common.domain.PageParam;',

    # PageResult
    r'import\s+net\.lab1024\.sa\.base\.common\.domain\.PageResult;':
        'import net.lab1024.sa.common.domain.PageResult;',
    r'import\s+net\.lab1024\.sa\.base\.common\.page\.PageResult;':
        'import net.lab1024.sa.common.domain.PageResult;',

    # SmartException
    r'import\s+net\.lab1024\.sa\.base\.common\.exception\.SmartException;':
        'import net.lab1024.sa.common.exception.SmartException;',

    # SmartRequestUtil
    r'import\s+net\.lab1024\.sa\.base\.common\.util\.SmartRequestUtil;':
        'import net.lab1024.sa.common.util.SmartRequestUtil;',
}

def fix_test_imports_in_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复测试文件中的导入路径

    Args:
        file_path: Java文件路径

    Returns:
        (是否修改, 修改说明列表)
    """
    if not file_path.exists() or not file_path.suffix == '.java':
        return False, []

    # 只处理测试文件
    if 'test' not in str(file_path):
        return False, []

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        try:
            with open(file_path, 'r', encoding='gbk') as f:
                content = f.read()
        except:
            return False, []

    original = content
    changes = []

    # 应用所有映射
    for pattern, replacement in TEST_IMPORT_MAPPINGS.items():
        matches = re.findall(pattern, content)
        if matches:
            content = re.sub(pattern, replacement, content)
            changes.append(f"修复导入: {matches[0]} -> {replacement}")

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

    print("开始修复测试文件导入路径...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有测试Java文件
    for java_file in microservices_dir.rglob("*Test.java"):
        modified, changes = fix_test_imports_in_file(java_file)
        if modified:
            fixed_files.append((java_file, changes))
            total_changes += len(changes)
            rel_path = java_file.relative_to(microservices_dir)
            print(f"\n修复: {rel_path}")
            for change in changes:
                print(f"  - {change}")

    print("\n" + "=" * 80)
    print(f"修复完成！共修复 {len(fixed_files)} 个测试文件，{total_changes} 处修改")

if __name__ == "__main__":
    main()

