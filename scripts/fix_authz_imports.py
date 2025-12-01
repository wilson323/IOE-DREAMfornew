"""
批量修复authz.rac.annotation导入路径错误
将base.authz.rac.annotation替换为common.annotation
"""
import re
from pathlib import Path
from typing import List, Tuple

def fix_authz_imports_in_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复文件中的authz导入路径

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

    # 修复 base.authz.rac.annotation -> common.annotation
    pattern = r'import\s+net\.lab1024\.sa\.base\.authz\.rac\.annotation\.([a-zA-Z]+);'

    def replacer(match):
        class_name = match.group(1)
        old_import = match.group(0)
        new_import = f'import net.lab1024.sa.common.annotation.{class_name};'
        changes.append(f"修复导入: {old_import} -> {new_import}")
        return new_import

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

    print("开始修复authz.rac.annotation导入路径...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有Java文件
    for java_file in microservices_dir.rglob("*.java"):
        modified, changes = fix_authz_imports_in_file(java_file)
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

