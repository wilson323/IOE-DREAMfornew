"""
批量修复方法调用错误
修复常见的方法调用错误
"""
import re
from pathlib import Path
from typing import List, Tuple

def fix_method_calls_in_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复文件中的方法调用错误

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

    # 修复 LivenessDetectionResponse.addWarning() -> getWarnings().add()
    # 匹配 response.addWarning("...") 或 response.addWarning(warning)
    pattern1 = r'(\w+)\.addWarning\(([^)]+)\)'
    def replacer1(match):
        var_name = match.group(1)
        param = match.group(2)
        changes.append(f"修复方法调用: {var_name}.addWarning({param}) -> {var_name}.getWarnings().add({param})")
        return f'{var_name}.getWarnings().add({param})'

    if re.search(pattern1, content):
        content = re.sub(pattern1, replacer1, content)

    # 修复 AuthenticationResponse.setRequestTime() -> setResponseTime()
    pattern2 = r'(\w+)\.setRequestTime\(([^)]+)\)'
    def replacer2(match):
        var_name = match.group(1)
        param = match.group(2)
        changes.append(f"修复方法调用: {var_name}.setRequestTime({param}) -> {var_name}.setResponseTime({param})")
        return f'{var_name}.setResponseTime({param})'

    if re.search(pattern2, content):
        content = re.sub(pattern2, replacer2, content)

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

    print("开始修复方法调用错误...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有Java文件
    for java_file in microservices_dir.rglob("*.java"):
        modified, changes = fix_method_calls_in_file(java_file)
        if modified:
            fixed_files.append((java_file, changes))
            total_changes += len(changes)
            rel_path = java_file.relative_to(microservices_dir)
            print(f"\n修复: {rel_path}")
            for change in changes:
                print(f"  - {change}")

    print("\n" + "=" * 80)
    print(f"修复完成！共修复 {len(fixed_files)} 个文件，{total_changes} 处修改")

    if fixed_files:
        print("\n修复的文件列表:")
        for java_file, changes in fixed_files:
            print(f"  - {java_file.relative_to(microservices_dir)}")

if __name__ == '__main__':
    main()

