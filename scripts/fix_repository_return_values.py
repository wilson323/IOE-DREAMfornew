"""
修复 Repository 类中缺失返回值的问题
在 catch 块中添加默认返回值
"""
import re
from pathlib import Path
from typing import List, Tuple

def fix_repository_methods(file_path: Path) -> Tuple[bool, List[str]]:
    """修复文件中的 Repository 方法返回值问题"""
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

    # 修复模式1: Optional<T> 方法缺少返回值
    # 匹配: public Optional<...> method(...) { ... catch { ... } } 没有 return
    pattern1 = r'(public\s+Optional<[^>]+>\s+\w+\([^)]*\)\s*\{[^}]*catch\s*\([^)]*\)\s*\{[^}]*log\.error[^}]*//\s*TEMP[^}]*)\s*\}'

    def fix_optional_return(match):
        method_body = match.group(1)
        # 提取返回类型
        return_type_match = re.search(r'Optional<([^>]+)>', method_body)
        if return_type_match:
            inner_type = return_type_match.group(1)
            # 在 catch 块末尾添加 return Optional.empty()
            fixed = method_body + '\n            return Optional.empty();'
            changes.append("修复 Optional 返回值")
            return fixed + '\n    }'
        return match.group(0)

    # 修复模式2: 基本类型方法缺少返回值
    # Long, int, boolean, List, Map 等
    patterns = [
        (r'(public\s+Long\s+\w+\([^)]*\)\s*\{[^}]*catch\s*\([^)]*\)\s*\{[^}]*log\.error[^}]*//\s*TEMP[^}]*)\s*\}',
         'return 0L;', 'Long'),
        (r'(public\s+int\s+\w+\([^)]*\)\s*\{[^}]*catch\s*\([^)]*\)\s*\{[^}]*log\.error[^}]*//\s*TEMP[^}]*)\s*\}',
         'return 0;', 'int'),
        (r'(public\s+boolean\s+\w+\([^)]*\)\s*\{[^}]*catch\s*\([^)]*\)\s*\{[^}]*log\.error[^}]*//\s*TEMP[^}]*)\s*\}',
         'return false;', 'boolean'),
        (r'(public\s+List<[^>]+>\s+\w+\([^)]*\)\s*\{[^}]*catch\s*\([^)]*\)\s*\{[^}]*log\.error[^}]*//\s*TEMP[^}]*)\s*\}',
         'return new ArrayList<>();', 'List'),
        (r'(public\s+Map<[^>]+>\s+\w+\([^)]*\)\s*\{[^}]*catch\s*\([^)]*\)\s*\{[^}]*log\.error[^}]*//\s*TEMP[^}]*)\s*\}',
         'return new HashMap<>();', 'Map'),
        (r'(public\s+IPage<[^>]+>\s+\w+\([^)]*\)\s*\{[^}]*catch\s*\([^)]*\)\s*\{[^}]*log\.error[^}]*//\s*TEMP[^}]*)\s*\}',
         'return new Page<>();', 'IPage'),
    ]

    for pattern, return_value, type_name in patterns:
        def make_fixer(ret_val, t_name):
            def fixer(match):
                method_body = match.group(1)
                fixed = method_body + f'\n            {ret_val}'
                changes.append(f"修复 {t_name} 返回值")
                return fixed + '\n    }'
            return fixer

        if re.search(pattern, content, re.DOTALL):
            content = re.sub(pattern, make_fixer(return_value, type_name), content, flags=re.DOTALL)

    # 修复 Optional 返回值
    optional_pattern = r'(public\s+Optional<[^>]+>\s+\w+\([^)]*\)\s*\{[^}]*catch\s*\([^)]*\)\s*\{[^}]*log\.error[^}]*//\s*TEMP[^}]*)\s*\}'
    if re.search(optional_pattern, content, re.DOTALL):
        def fix_optional(match):
            method_body = match.group(1)
            fixed = method_body + '\n            return Optional.empty();'
            changes.append("修复 Optional 返回值")
            return fixed + '\n    }'
        content = re.sub(optional_pattern, fix_optional, content, flags=re.DOTALL)

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
    attendance_service_dir = Path(__file__).parent.parent / "microservices" / "ioedream-attendance-service"

    if not attendance_service_dir.exists():
        print(f"考勤服务目录不存在: {attendance_service_dir}")
        return

    print("开始修复 Repository 方法返回值问题...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有 Repository 文件
    for java_file in attendance_service_dir.rglob("*Repository.java"):
        if 'test' in str(java_file):
            continue

        modified, changes = fix_repository_methods(java_file)
        if modified:
            fixed_files.append((java_file, changes))
            total_changes += len(changes)
            rel_path = java_file.relative_to(attendance_service_dir)
            print(f"\n修复: {rel_path}")
            for change in changes[:5]:
                print(f"  - {change}")

    print("\n" + "=" * 80)
    print(f"修复完成！共修复 {len(fixed_files)} 个文件，{total_changes} 处修改")

if __name__ == "__main__":
    main()

