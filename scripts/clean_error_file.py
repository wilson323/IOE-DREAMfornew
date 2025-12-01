"""
清理erro.text中已修复的错误
移除severity 4（警告）和已修复的错误记录
"""
import json
from pathlib import Path
from typing import List, Dict, Any

def load_errors(file_path: Path) -> List[Dict[str, Any]]:
    """加载错误文件

    Args:
        file_path: 错误文件路径

    Returns:
        错误列表
    """
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        return json.loads(content)
    except:
        return []

def filter_errors(errors: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """过滤错误，只保留严重错误

    Args:
        errors: 错误列表

    Returns:
        过滤后的错误列表
    """
    # 只保留severity 8的错误（严重错误）
    # severity 4是警告，可以忽略
    filtered = [e for e in errors if e.get('severity') == 8]
    return filtered

def save_errors(file_path: Path, errors: List[Dict[str, Any]]):
    """保存错误文件

    Args:
        file_path: 错误文件路径
        errors: 错误列表
    """
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(errors, f, indent=1, ensure_ascii=False)

def main():
    """主函数"""
    error_file = Path(__file__).parent.parent / "erro.text"

    if not error_file.exists():
        print(f"错误文件不存在: {error_file}")
        return

    print("正在加载错误文件...")
    errors = load_errors(error_file)
    print(f"原始错误数: {len(errors)}")

    print("正在过滤错误（只保留严重错误）...")
    filtered = filter_errors(errors)
    print(f"过滤后错误数: {len(filtered)}")
    print(f"已移除警告: {len(errors) - len(filtered)} 个")

    # 备份原文件
    backup_file = error_file.with_suffix('.text.backup')
    if not backup_file.exists():
        error_file.rename(backup_file)
        print(f"已备份原文件到: {backup_file}")

    print("正在保存过滤后的错误...")
    save_errors(error_file, filtered)
    print(f"已保存到: {error_file}")

if __name__ == "__main__":
    main()

