#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM 根目录文档清理脚本
功能：清理根目录下的临时报告文档，移动到归档目录
"""

import os
import shutil
from pathlib import Path

ROOT_DIR = Path(r"d:\IOE-DREAM")
ARCHIVE_DIR = ROOT_DIR / "documentation" / "archive" / "root-reports"

# 临时报告模式
TEMP_REPORT_PATTERNS = [
    "*FINAL*.md",
    "*COMPLETE*.md",
    "*COMPLETE_*.md",
    "MERGE_*.md",
    "*REPORT*.md",
    "*FIX*.md",
    "*ERROR*.md",
    "*COMPILATION*.md",
    "*SUMMARY*.md",
    "TEST_*.md",
    "*ANALYSIS*.md",
    "*VERIFICATION*.md",
    "*EXECUTION*.md",
    "*PROGRESS*.md",
    "*IMPLEMENTATION*.md",
    "FIX_NOW.md",
    "EXECUTE_NOW.md",
    "START_BUILD.md",
    "README_BUILD.md",
    "*业务模块*.md",
    "*工作流*.md",
    "*全局*.md",
    "*代码质量*.md",
    "*紧急*.md",
    "*区域管理*.md",
    "TODO_*.md",
    "*UNIT_TEST*.md",
    "*USER_ROLE*.md",
    "API_*.md",
    "ARCHITECTURE_*.md",
    "BEAN_*.md",
    "BUSINESS_*.md",
    "CODE_*.md",
    "COMPILATION_*.md",
    "CONSUME_*.md",
    "CONTROLLER_*.md",
    "DUPLICATE_*.md",
    "ENCODING_*.md",
    "FRONTEND_*.md",
    "GIT_*.md",
    "GLOBAL_*.md",
    "INTEGRATION_*.md",
    "IOE-DREAM_*.md",
]

# 有用文档映射
USEFUL_DOCS = {
    "DEPLOYMENT.md": ROOT_DIR / "documentation" / "deployment" / "DEPLOYMENT.md",
    "MCP配置说明.md": ROOT_DIR / "documentation" / "development" / "MCP配置说明.md",
}

def matches_pattern(filename, patterns):
    """检查文件名是否匹配任一模式"""
    import fnmatch
    for pattern in patterns:
        if fnmatch.fnmatch(filename, pattern):
            return True
    return False

def main():
    print("=" * 50)
    print("IOE-DREAM 根目录文档清理脚本")
    print("=" * 50)
    print()
    
    # 创建归档目录
    ARCHIVE_DIR.mkdir(parents=True, exist_ok=True)
    print(f"✅ 归档目录: {ARCHIVE_DIR}")
    print()
    
    stats = {"archived": 0, "moved": 0, "skipped": 0}
    
    # 处理根目录下的MD文件
    md_files = list(ROOT_DIR.glob("*.md"))
    
    for md_file in md_files:
        filename = md_file.name
        
        # 跳过CLAUDE.md
        if filename in ["CLAUDE.md", "CLAUDE.md.bak"]:
            continue
        
        # 检查是否是有用文档
        if filename in USEFUL_DOCS:
            target = USEFUL_DOCS[filename]
            target.parent.mkdir(parents=True, exist_ok=True)
            
            # 如果目标文件存在，先备份
            if target.exists():
                backup = target.with_suffix(target.suffix + ".bak")
                shutil.copy2(target, backup)
                print(f"⚠️  目标文件已存在，已备份: {backup.name}")
            
            shutil.move(str(md_file), str(target))
            print(f"📁 移动: {filename} -> {target.relative_to(ROOT_DIR)}")
            stats["moved"] += 1
        
        # 检查是否是临时报告
        elif matches_pattern(filename, TEMP_REPORT_PATTERNS):
            target = ARCHIVE_DIR / filename
            shutil.move(str(md_file), str(target))
            print(f"📦 归档: {filename} -> archive/root-reports/")
            stats["archived"] += 1
        
        else:
            print(f"⏭️  跳过: {filename} (不在清理列表中)")
            stats["skipped"] += 1
    
    print()
    print("=" * 50)
    print("清理完成统计")
    print("=" * 50)
    print(f"  归档文件: {stats['archived']}")
    print(f"  移动文件: {stats['moved']}")
    print(f"  跳过文件: {stats['skipped']}")
    print()
    print("✅ 清理完成！")

if __name__ == "__main__":
    main()
