"""
批量修复groupId不一致问题
将所有net.lab1024改为net.lab1024.sa
"""
from pathlib import Path
import re

def fix_groupid_in_file(file_path: Path) -> bool:
    """修复文件中的groupId

    Args:
        file_path: 文件路径

    Returns:
        是否修改了文件
    """
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content

    # 修复microservices-common的groupId
    content = re.sub(
        r'<groupId>net\.lab1024</groupId>\s*<artifactId>microservices-common</artifactId>',
        '<groupId>net.lab1024.sa</groupId>\n            <artifactId>microservices-common</artifactId>',
        content
    )

    if content != original:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    return False

def main():
    """主函数"""
    microservices_dir = Path(__file__).parent.parent / "microservices"

    files_to_fix = [
        "ioedream-file-service/pom.xml",
        "ioedream-hr-service/pom.xml",
        "ioedream-identity-service/pom.xml",
        "ioedream-notification-service/pom.xml",
        "ioedream-visitor-service/pom.xml",
        "ioedream-access-service/pom.xml",
    ]

    fixed = []
    for file_rel in files_to_fix:
        file_path = microservices_dir / file_rel
        if file_path.exists():
            if fix_groupid_in_file(file_path):
                fixed.append(file_rel)
                print(f"修复: {file_rel}")

    print(f"\n共修复 {len(fixed)} 个文件")

if __name__ == "__main__":
    main()

