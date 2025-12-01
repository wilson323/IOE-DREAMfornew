"""
修复Maven依赖问题脚本
统一groupId、版本号，修复缺失依赖
"""
import re
from pathlib import Path
from typing import List, Tuple

def fix_pom_file(pom_path: Path) -> Tuple[bool, List[str]]:
    """修复单个pom.xml文件

    Args:
        pom_path: pom.xml文件路径

    Returns:
        (是否修改, 修改说明列表)
    """
    if not pom_path.exists():
        return False, []

    with open(pom_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content
    changes = []

    # 1. 修复groupId不一致问题 (net.lab1024 -> net.lab1024.sa)
    if 'net.lab1024</groupId>' in content and 'net.lab1024.sa</groupId>' not in content:
        # 检查是否是microservices-common依赖
        pattern = r'<groupId>net\.lab1024</groupId>\s*<artifactId>microservices-common</artifactId>'
        if re.search(pattern, content):
            content = re.sub(
                r'<groupId>net\.lab1024</groupId>\s*<artifactId>microservices-common</artifactId>',
                '<groupId>net.lab1024.sa</groupId>\n            <artifactId>microservices-common</artifactId>',
                content
            )
            changes.append("修复groupId: net.lab1024 -> net.lab1024.sa (microservices-common)")

    # 2. 修复fastjson2版本缺失问题
    if 'fastjson2</artifactId>' in content and 'version' not in re.search(r'fastjson2</artifactId>.*?</dependency>', content, re.DOTALL).group(0):
        content = re.sub(
            r'(<artifactId>fastjson2</artifactId>)\s*(</dependency>)',
            r'\1\n            <version>${fastjson2.version}</version>\2',
            content
        )
        changes.append("添加fastjson2版本号")

    # 3. 移除已注释但仍在错误日志中的依赖引用
    # 检查是否有microservices-common-transaction或microservices-common-sync的引用
    if 'microservices-common-transaction' in content or 'microservices-common-sync' in content:
        # 如果已经注释掉，确保完全移除
        content = re.sub(
            r'<!--.*?microservices-common-transaction.*?-->',
            '',
            content,
            flags=re.DOTALL
        )
        content = re.sub(
            r'<!--.*?microservices-common-sync.*?-->',
            '',
            content,
            flags=re.DOTALL
        )
        # 移除未注释的引用
        content = re.sub(
            r'<dependency>.*?microservices-common-transaction.*?</dependency>',
            '',
            content,
            flags=re.DOTALL
        )
        content = re.sub(
            r'<dependency>.*?microservices-common-sync.*?</dependency>',
            '',
            content,
            flags=re.DOTALL
        )
        changes.append("移除已废弃的microservices-common-transaction和microservices-common-sync依赖")

    # 4. 确保microservices-common依赖正确
    if 'microservices-common</artifactId>' in content:
        # 检查是否有version
        pattern = r'<artifactId>microservices-common</artifactId>\s*(?:<version>.*?</version>)?\s*</dependency>'
        match = re.search(pattern, content)
        if match and '<version>' not in match.group(0):
            # 添加version
            content = re.sub(
                r'(<artifactId>microservices-common</artifactId>)\s*(</dependency>)',
                r'\1\n            <version>${project.version}</version>\2',
                content
            )
            changes.append("添加microservices-common版本号")

    # 5. 修复commons-io和commons-codec版本
    if 'commons-io</artifactId>' in content:
        pattern = r'<artifactId>commons-io</artifactId>\s*(?:<version>.*?</version>)?\s*</dependency>'
        match = re.search(pattern, content)
        if match and '<version>' not in match.group(0):
            content = re.sub(
                r'(<artifactId>commons-io</artifactId>)\s*(</dependency>)',
                r'\1\n            <version>2.11.0</version>\2',
                content
            )
            changes.append("添加commons-io版本号")

    if 'commons-codec</artifactId>' in content:
        pattern = r'<artifactId>commons-codec</artifactId>\s*(?:<version>.*?</version>)?\s*</dependency>'
        match = re.search(pattern, content)
        if match and '<version>' not in match.group(0):
            content = re.sub(
                r'(<artifactId>commons-codec</artifactId>)\s*(</dependency>)',
                r'\1\n            <version>1.16.0</version>\2',
                content
            )
            changes.append("添加commons-codec版本号")

    if content != original_content:
        with open(pom_path, 'w', encoding='utf-8') as f:
            f.write(content)
        return True, changes

    return False, []

def main():
    """主函数"""
    microservices_dir = Path(__file__).parent.parent / "microservices"

    if not microservices_dir.exists():
        print(f"微服务目录不存在: {microservices_dir}")
        return

    print("开始修复Maven依赖问题...")
    print("=" * 80)

    fixed_files = []
    total_changes = 0

    # 查找所有pom.xml文件
    for pom_file in microservices_dir.rglob("pom.xml"):
        modified, changes = fix_pom_file(pom_file)
        if modified:
            fixed_files.append((pom_file, changes))
            total_changes += len(changes)
            print(f"\n修复: {pom_file.relative_to(microservices_dir)}")
            for change in changes:
                print(f"  - {change}")

    print("\n" + "=" * 80)
    print(f"修复完成！共修复 {len(fixed_files)} 个文件，{total_changes} 处修改")

    if fixed_files:
        print("\n修复的文件列表:")
        for pom_file, changes in fixed_files:
            print(f"  - {pom_file.relative_to(microservices_dir)}")

if __name__ == "__main__":
    main()

