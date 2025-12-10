#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IOE-DREAM 根目录文档清理脚本
"""
import os
import shutil
import fnmatch
from pathlib import Path

ROOT = Path(r"d:\IOE-DREAM")
ARCHIVE = ROOT / "documentation" / "archive" / "root-reports"

# 归档模式
ARCHIVE_PATTERNS = [
    "*FINAL*.md", "*COMPLETE*.md", "MERGE_*.md", "*REPORT*.md",
    "*FIX*.md", "*ERROR*.md", "*COMPILATION*.md", "*SUMMARY*.md",
    "TEST_*.md", "*ANALYSIS*.md", "*VERIFICATION*.md", "*EXECUTION*.md",
    "*PROGRESS*.md", "*IMPLEMENTATION*.md", "FIX_NOW.md", "EXECUTE_NOW.md",
    "START_BUILD.md", "README_BUILD.md", "*业务模块*.md", "*工作流*.md",
    "*全局*.md", "*代码质量*.md", "*紧急*.md", "*区域管理*.md",
    "TODO_*.md", "*UNIT_TEST*.md", "*USER_ROLE*.md", "API_*.md",
    "ARCHITECTURE_*.md", "BEAN_*.md", "BUSINESS_*.md", "CODE_*.md",
    "COMPILATION_*.md", "CONSUME_*.md", "CONTROLLER_*.md", "DUPLICATE_*.md",
    "ENCODING_*.md", "FRONTEND_*.md", "GIT_*.md", "GLOBAL_*.md",
    "INTEGRATION_*.md", "IOE-DREAM_*.md", "PRODUCTION_*.md"
]

# 有用文档
USEFUL_DOCS = {
    "DEPLOYMENT.md": ROOT / "documentation" / "deployment" / "DEPLOYMENT.md",
    "MCP配置说明.md": ROOT / "documentation" / "development" / "MCP配置说明.md",
}

def should_archive(name):
    """判断是否应该归档"""
    if name in ["CLAUDE.md", "CLAUDE.md.bak"]:
        return False
    return any(fnmatch.fnmatch(name, p) for p in ARCHIVE_PATTERNS)

def main():
    print("=" * 60)
    print("IOE-DREAM 根目录文档清理")
    print("=" * 60)
    
    # 创建归档目录
    ARCHIVE.mkdir(parents=True, exist_ok=True)
    print(f"\n归档目录: {ARCHIVE}")
    
    stats = {"archived": 0, "moved": 0, "skipped": 0}
    
    # 处理文件
    md_files = [f for f in ROOT.glob("*.md") if f.is_file()]
    
    for md_file in md_files:
        name = md_file.name
        
        if name in ["CLAUDE.md", "CLAUDE.md.bak"]:
            continue
        
        if name in USEFUL_DOCS:
            # 移动有用文档
            target = USEFUL_DOCS[name]
            target.parent.mkdir(parents=True, exist_ok=True)
            
            if target.exists():
                backup = target.with_suffix(".bak")
                shutil.copy2(target, backup)
                print(f"备份: {target.name} -> {backup.name}")
            
            shutil.move(str(md_file), str(target))
            print(f"移动: {name} -> {target.relative_to(ROOT)}")
            stats["moved"] += 1
        
        elif should_archive(name):
            # 归档临时报告
            target = ARCHIVE / name
            shutil.move(str(md_file), str(target))
            print(f"归档: {name}")
            stats["archived"] += 1
        
        else:
            print(f"跳过: {name}")
            stats["skipped"] += 1
    
    print("\n" + "=" * 60)
    print("清理统计")
    print("=" * 60)
    print(f"归档: {stats['archived']} 个")
    print(f"移动: {stats['moved']} 个")
    print(f"跳过: {stats['skipped']} 个")
    print("\n完成!")

if __name__ == "__main__":
    main()
