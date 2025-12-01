"""
批量修复admin.module.attendance导入路径错误
将admin.module.attendance.*替换为attendance.*
"""
import re
from pathlib import Path
from typing import List, Tuple

def fix_admin_module_imports_in_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复文件中的admin.module.attendance导入路径

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

    # 修复 admin.module.attendance.* -> attendance.*
    pattern = r'net\.lab1024\.sa\.admin\.module\.attendance\.([a-zA-Z.]+)'

    def replacer(match):
        rest = match.group(1)
        old_full = match.group(0)
        new_full = f'net.lab1024.sa.attendance.{rest}'
        changes.append(f"修复导入: {old_full} -> {new_full}")
        return new_full

    # 替换所有出现的地方（包括import语句和类型引用）
    if re.search(pattern, content):
        content = re.sub(pattern, replacer, content)

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

    print("开始修复admin.module.attendance导入路径...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有Java文件
    for java_file in microservices_dir.rglob("*.java"):
        # 跳过.disabled和.fixed文件
        if '.disabled' in str(java_file) or '.fixed' in str(java_file):
            continue

        modified, changes = fix_admin_module_imports_in_file(java_file)
        if modified:
            fixed_files.append((java_file, changes))
            total_changes += len(changes)
            rel_path = java_file.relative_to(microservices_dir)
            print(f"\n修复: {rel_path}")
            for change in changes[:5]:  # 只显示前5个
                print(f"  - {change}")
            if len(changes) > 5:
                print(f"  ... 还有 {len(changes) - 5} 处修改")

    print("\n" + "=" * 80)
    print(f"修复完成！共修复 {len(fixed_files)} 个文件，{total_changes} 处修改")

if __name__ == "__main__":
    main()

