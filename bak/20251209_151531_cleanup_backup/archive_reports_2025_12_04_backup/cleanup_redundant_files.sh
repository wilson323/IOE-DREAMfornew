#!/bin/bash

echo "🧹 IOE-DREAM 文档清理脚本"
echo "清理时间: $(date)"
echo "=========================================="

# 设置颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 统计变量
REMOVED_BACKUPS=0
REMOVED_TEMPS=0
REMOVED_DIRS=0
FREED_SPACE=0

# 清理备份文件
echo -e "${YELLOW}📄 清理备份文件...${NC}"
find documentation/ -name "*.backup.*" -type f | while read -r file; do
    size=$(stat -c%s "$file" 2>/dev/null || stat -f%z "$file" 2>/dev/null || echo 0)
    if rm "$file" 2>/dev/null; then
        echo -e "  ${GREEN}✅ 删除: $(basename "$file") (${size} bytes)${NC}"
        REMOVED_BACKUPS=$((REMOVED_BACKUPS + 1))
        FREED_SPACE=$((FREED_SPACE + size))
    else
        echo -e "  ${RED}❌ 删除失败: $file${NC}"
    fi
done

# 清理临时文件
echo -e "${YELLOW}📄 清理临时文件...${NC}"
find documentation/ -name "*.tmp*" -type f | while read -r file; do
    size=$(stat -c%s "$file" 2>/dev/null || stat -f%z "$file" 2>/dev/null || echo 0)
    if rm "$file" 2>/dev/null; then
        echo -e "  ${GREEN}✅ 删除: $(basename "$file") (${size} bytes)${NC}"
        REMOVED_TEMPS=$((REMOVED_TEMPS + 1))
        FREED_SPACE=$((FREED_SPACE + size))
    else
        echo -e "  ${RED}❌ 删除失败: $file${NC}"
    fi
done

# 清理临时合并目录
echo -e "${YELLOW}📁 清理临时合并目录...${NC}"
find documentation/ -name "*temp-merge*" -type d | while read -r dir; do
    if rm -rf "$dir" 2>/dev/null; then
        echo -e "  ${GREEN}✅ 删除目录: $dir${NC}"
        REMOVED_DIRS=$((REMOVED_DIRS + 1))
    else
        echo -e "  ${RED}❌ 删除失败: $dir${NC}"
    fi
done

# 清理临时脚本文件
echo -e "${YELLOW}📄 清理临时脚本文件...${NC}"
for pattern in "*fix-temp*" "*cleanup-temp*" "*temp-fix*"; do
    find . -maxdepth 1 -name "$pattern" -type f | while read -r file; do
        if rm "$file" 2>/dev/null; then
            echo -e "  ${GREEN}✅ 删除临时脚本: $(basename "$file")${NC}"
        fi
    done
done

echo ""
echo "=========================================="
echo -e "${BLUE}📊 清理结果统计:${NC}"
echo -e "删除备份文件数: ${GREEN}${REMOVED_BACKUPS}${NC}"
echo -e "删除临时文件数: ${GREEN}${REMOVED_TEMPS}${NC}"
echo -e "删除临时目录数: ${GREEN}${REMOVED_DIRS}${NC}"
echo -e "释放空间: ${GREEN}$(( FREED_SPACE / 1024 )) KB${NC}"

echo ""
echo -e "${GREEN}🎉 清理完成！${NC}"
echo -e "${BLUE}✨ 文档目录更加整洁了${NC}"
