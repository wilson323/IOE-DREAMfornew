"""
修复Object类型被错误参数化的问题
将Object<XXX>替换为PageResult<XXX>
"""
import re
from pathlib import Path
from typing import List, Tuple

def fix_object_type_in_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复文件中的Object类型错误

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

    # 修复 Object<XXX> -> PageResult<XXX>
    pattern1 = r'Object<([^>]+)>'
    matches1 = re.findall(pattern1, content)
    if matches1:
        # 检查是否在方法返回类型或变量声明中
        # 避免替换Object类本身的引用
        def replacer(match):
            full_match = match.group(0)
            type_param = match.group(1)
            # 如果是在类型声明中（前面有空格、=、<、,、()等）
            if re.search(r'[=<>,\s\(\)]Object<', match.string[:match.start()]):
                changes.append(f"修复类型: Object<{type_param}> -> PageResult<{type_param}>")
                return f'PageResult<{type_param}>'
            return full_match

        content = re.sub(pattern1, replacer, content)

    # 修复 new Object<>() -> new PageResult<>()
    pattern2 = r'new Object<>\(\)'
    matches2 = re.findall(pattern2, content)
    if matches2:
        content = re.sub(pattern2, 'new PageResult<>()', content)
        changes.append(f"修复实例化: new Object<>() -> new PageResult<>() ({len(matches2)}处)")

    # 修复 Object pageParam -> PageParam pageParam
    pattern3 = r'\bObject\s+pageParam\b'
    matches3 = re.findall(pattern3, content)
    if matches3:
        content = re.sub(pattern3, 'PageParam pageParam', content)
        changes.append(f"修复参数类型: Object pageParam -> PageParam pageParam ({len(matches3)}处)")

    if content != original:
        # 确保导入了PageResult和PageParam
        if 'PageResult<' in content and 'import net.lab1024.sa.common.domain.PageResult;' not in content:
            # 在最后一个import后添加
            import_pattern = r'(import\s+[^;]+;\s*\n)(?!import)'
            last_import = list(re.finditer(import_pattern, content))
            if last_import:
                pos = last_import[-1].end()
                content = content[:pos] + 'import net.lab1024.sa.common.domain.PageResult;\n' + content[pos:]
                changes.append("添加PageResult导入")

        if 'PageParam' in content and 'import net.lab1024.sa.common.domain.PageParam;' not in content:
            import_pattern = r'(import\s+[^;]+;\s*\n)(?!import)'
            last_import = list(re.finditer(import_pattern, content))
            if last_import:
                pos = last_import[-1].end()
                content = content[:pos] + 'import net.lab1024.sa.common.domain.PageParam;\n' + content[pos:]
                changes.append("添加PageParam导入")

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

    print("开始修复Object类型错误...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有Java文件
    for java_file in microservices_dir.rglob("*.java"):
        modified, changes = fix_object_type_in_file(java_file)
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

