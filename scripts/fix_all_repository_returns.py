"""
批量修复所有 Repository 文件中缺失返回值的问题
"""
import re
from pathlib import Path
from typing import List, Tuple

def get_return_type_from_method_signature(method_signature: str) -> str:
    """从方法签名中提取返回类型"""
    # 匹配: public ReturnType methodName(...)
    match = re.search(r'public\s+(\w+(?:<[^>]+>)?)\s+\w+\(', method_signature)
    if match:
        return match.group(1)
    return None

def fix_repository_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复单个 Repository 文件的返回值问题"""
    if not file_path.exists() or not file_path.suffix == '.java':
        return False, []

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
    except UnicodeDecodeError:
        try:
            with open(file_path, 'r', encoding='gbk') as f:
                lines = f.readlines()
        except:
            return False, []

    original_lines = lines.copy()
    changes = []
    i = 0

    while i < len(lines):
        line = lines[i]

        # 查找方法定义
        if re.match(r'\s*public\s+\w+', line):
            # 收集完整的方法签名（可能跨多行）
            method_start = i
            method_signature = line
            brace_count = line.count('{') - line.count('}')
            j = i + 1

            # 找到方法体的开始
            while j < len(lines) and brace_count <= 0:
                method_signature += lines[j]
                brace_count += lines[j].count('{') - lines[j].count('}')
                j += 1

            # 提取返回类型
            return_type_match = re.search(r'public\s+(\w+(?:<[^>]+>)?)\s+\w+\(', method_signature)
            if return_type_match:
                return_type = return_type_match.group(1)

                # 查找方法体中的 catch 块
                method_end = j
                in_catch = False
                catch_start = -1

                for k in range(method_start, min(method_end + 50, len(lines))):
                    if 'catch' in lines[k] and '(' in lines[k]:
                        in_catch = True
                        catch_start = k
                    elif in_catch and '}' in lines[k]:
                        # 检查 catch 块结束前是否有 return
                        catch_block = ''.join(lines[catch_start:k+1])
                        if 'return' not in catch_block and '// TEMP:' in catch_block:
                            # 需要添加返回值
                            return_value = get_default_return_value(return_type)
                            if return_value:
                                # 在 catch 块的最后一个 } 之前插入 return
                                indent = re.match(r'(\s*)', lines[k]).group(1)
                                lines.insert(k, indent + '            ' + return_value + '\n')
                                changes.append(f"修复 {return_type} 返回值")
                                method_end = k + 1
                                break
                        in_catch = False
                        catch_start = -1

            i = method_end if method_end > i else i + 1
        else:
            i += 1

    if lines != original_lines:
        try:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.writelines(lines)
            return True, changes
        except Exception as e:
            print(f"写入文件失败 {file_path}: {e}")
            return False, []

    return False, []

def get_default_return_value(return_type: str) -> str:
    """根据返回类型获取默认返回值"""
    return_type = return_type.strip()

    if return_type.startswith('Optional<'):
        return 'return Optional.empty();'
    elif return_type == 'Long' or return_type == 'long':
        return 'return 0L;'
    elif return_type == 'Integer' or return_type == 'int':
        return 'return 0;'
    elif return_type == 'Boolean' or return_type == 'boolean':
        return 'return false;'
    elif return_type.startswith('List<'):
        return 'return new ArrayList<>();'
    elif return_type.startswith('Map<'):
        return 'return new HashMap<>();'
    elif return_type.startswith('IPage<'):
        return 'return new Page<>();'
    elif return_type.endswith('Entity') or return_type.endswith('VO') or return_type.endswith('DTO'):
        return 'return null;'
    else:
        return None

def main():
    """主函数"""
    attendance_service_dir = Path(__file__).parent.parent / "microservices" / "ioedream-attendance-service"

    if not attendance_service_dir.exists():
        print(f"考勤服务目录不存在: {attendance_service_dir}")
        return

    print("开始修复所有 Repository 文件返回值问题...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有 Repository 文件
    for java_file in attendance_service_dir.rglob("*Repository.java"):
        if 'test' in str(java_file):
            continue

        modified, changes = fix_repository_file(java_file)
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

