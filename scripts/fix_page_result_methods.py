"""
修复PageResult和PageParam的方法调用错误
将错误的方法名替换为正确的方法名
"""
import re
from pathlib import Path
from typing import List, Tuple

# 方法名映射
METHOD_MAPPINGS = {
    # PageResult方法
    r'\.setCurrent\((\w+)\)': r'.setPageNum(\1)',
    r'\.setSize\((\w+)\)': r'.setPageSize(\1)',
    r'\.setRecords\(([^)]+)\)': r'.setList(\1)',
    # PageParam方法
    r'pageParam\.setCurrent\(': 'pageParam.setPageNum(',
    r'pageParam\.setSize\(': 'pageParam.setPageSize(',
}

def fix_methods_in_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复文件中的方法调用

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
        try:
            with open(file_path, 'r', encoding='gbk') as f:
                content = f.read()
        except:
            return False, []

    original = content
    changes = []

    # 应用所有映射
    for pattern, replacement in METHOD_MAPPINGS.items():
        matches = re.findall(pattern, content)
        if matches:
            content = re.sub(pattern, replacement, content)
            changes.append(f"修复方法调用: {pattern} -> {replacement}")

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

    print("开始修复PageResult/PageParam方法调用...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有Java文件（包括测试文件）
    for java_file in microservices_dir.rglob("*.java"):
        modified, changes = fix_methods_in_file(java_file)
        if modified:
            fixed_files.append((java_file, changes))
            total_changes += len(changes)
            rel_path = java_file.relative_to(microservices_dir)
            print(f"\n修复: {rel_path}")
            for change in changes:
                print(f"  - {change}")

    print("\n" + "=" * 80)
    print(f"修复完成！共修复 {len(fixed_files)} 个文件，{total_changes} 处修改")

if __name__ == "__main__":
    main()

