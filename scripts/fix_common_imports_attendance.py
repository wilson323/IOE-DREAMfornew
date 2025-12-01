"""
批量修复考勤服务中的公共模块导入问题
优先级1：修复 net.lab1024.sa.common 相关导入
"""
import re
from pathlib import Path
from typing import List, Tuple

# 导入路径映射表 - 针对 attendance-service
IMPORT_MAPPINGS = {
    # ResponseDTO - 最常见的错误
    r'import\s+net\.lab1024\.sa\.common\.ResponseDTO;':
        'import net.lab1024.sa.common.domain.ResponseDTO;',
    r'import\s+net\.lab1024\.base\.common\.response\.ResponseDTO;':
        'import net.lab1024.sa.common.domain.ResponseDTO;',

    # PageParam
    r'import\s+net\.lab1024\.sa\.common\.PageParam;':
        'import net.lab1024.sa.common.domain.PageParam;',

    # PageResult
    r'import\s+net\.lab1024\.sa\.common\.PageResult;':
        'import net.lab1024.sa.common.domain.PageResult;',

    # BaseEntity
    r'import\s+net\.lab1024\.sa\.common\.BaseEntity;':
        'import net.lab1024.sa.common.entity.BaseEntity;',
    r'import\s+net\.lab1024\.base\.common\.entity\.BaseEntity;':
        'import net.lab1024.sa.common.entity.BaseEntity;',

    # SmartException
    r'import\s+net\.lab1024\.sa\.common\.SmartException;':
        'import net.lab1024.sa.common.exception.SmartException;',

    # BusinessException
    r'import\s+net\.lab1024\.sa\.common\.BusinessException;':
        'import net.lab1024.sa.common.exception.BusinessException;',
}

def fix_imports_in_file(file_path: Path) -> Tuple[bool, List[str]]:
    """修复文件中的导入路径"""
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
    for pattern, replacement in IMPORT_MAPPINGS.items():
        if re.search(pattern, content):
            content = re.sub(pattern, replacement, content)
            changes.append(f"修复: {pattern} -> {replacement}")

    # 处理缺失的导入 - 添加缺失的导入
    if 'ResponseDTO' in content and 'import net.lab1024.sa.common.domain.ResponseDTO' not in content:
        if 'import net.lab1024.sa.common.domain' not in content:
            # 在 package 声明后添加导入
            package_match = re.search(r'^package\s+([^;]+);', content, re.MULTILINE)
            if package_match:
                insert_pos = package_match.end()
                content = content[:insert_pos] + '\n\nimport net.lab1024.sa.common.domain.ResponseDTO;' + content[insert_pos:]
                changes.append("添加缺失的 ResponseDTO 导入")

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

    print("开始修复考勤服务中的公共模块导入问题...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有Java文件
    for java_file in attendance_service_dir.rglob("*.java"):
        # 跳过测试文件（先修复主要代码）
        if 'test' in str(java_file):
            continue

        modified, changes = fix_imports_in_file(java_file)
        if modified:
            fixed_files.append((java_file, changes))
            total_changes += len(changes)
            rel_path = java_file.relative_to(attendance_service_dir)
            print(f"\n修复: {rel_path}")
            for change in changes[:5]:  # 显示前5个
                print(f"  - {change}")

    print("\n" + "=" * 80)
    print(f"修复完成！共修复 {len(fixed_files)} 个文件，{total_changes} 处修改")

if __name__ == "__main__":
    main()

