"""
修复类型错误
修复Object类型被错误参数化的问题
"""
import re
from pathlib import Path
from typing import List, Tuple

def fix_type_errors_in_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复文件中的类型错误

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

    # 修复 ResponseDTO<Object<XXX>> -> ResponseDTO<XXX>
    pattern1 = r'ResponseDTO<Object<([^>]+)>>'
    matches1 = re.findall(pattern1, content)
    if matches1:
        content = re.sub(pattern1, r'ResponseDTO<\1>', content)
        changes.append(f"修复类型: ResponseDTO<Object<...>> -> ResponseDTO<...> ({len(matches1)}处)")

    # 修复 PageResult<Object<XXX>> -> PageResult<XXX>
    pattern2 = r'PageResult<Object<([^>]+)>>'
    matches2 = re.findall(pattern2, content)
    if matches2:
        content = re.sub(pattern2, r'PageResult<\1>', content)
        changes.append(f"修复类型: PageResult<Object<...>> -> PageResult<...> ({len(matches2)}处)")

    # 修复 List<Object<XXX>> -> List<XXX>
    pattern3 = r'List<Object<([^>]+)>>'
    matches3 = re.findall(pattern3, content)
    if matches3:
        content = re.sub(pattern3, r'List<\1>', content)
        changes.append(f"修复类型: List<Object<...>> -> List<...> ({len(matches3)}处)")

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

    print("开始修复类型错误...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有Java文件
    for java_file in microservices_dir.rglob("*.java"):
        modified, changes = fix_type_errors_in_file(java_file)
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

