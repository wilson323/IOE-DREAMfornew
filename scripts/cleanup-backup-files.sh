#!/bin/bash

###############################################################################
# IOE-DREAM 备份文件清理脚本
# 功能：清理项目中的备份文件和临时文件
# 作者：IOE-DREAM架构委员会
# 日期：2025-12-25
###############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 项目根目录
PROJECT_ROOT="D:/IOE-DREAM"
cd "$PROJECT_ROOT" || exit 1

log_info "========== 开始清理备份文件 =========="
log_info "项目目录: $PROJECT_ROOT"
echo ""

# 统计将要删除的文件
log_info "正在统计备份文件..."

BACKUP_COUNT=$(find . -type f \( -name "*.backup*" -o -name "*.bak" -o -name "*.original*" -o -name "*.old" -o -name "*~" \) 2>/dev/null | wc -l)
log_info "发现备份文件数量: $BACKUP_COUNT"

# 按类型分类统计
BACKUP_FILES=$(find . -type f -name "*.backup*" 2>/dev/null | wc -l)
BAK_FILES=$(find . -type f -name "*.bak" 2>/dev/null | wc -l)
ORIGINAL_FILES=$(find . -type f -name "*.original*" 2>/dev/null | wc -l)
OLD_FILES=$(find . -type f -name "*.old" 2>/dev/null | wc -l)
TILDE_FILES=$(find . -type f -name "*~" 2>/dev/null | wc -l)

log_info "  - *.backup* 文件: $BACKUP_FILES"
log_info "  - *.bak 文件: $BAK_FILES"
log_info "  - *.original* 文件: $ORIGINAL_FILES"
log_info "  - *.old 文件: $OLD_FILES"
log_info "  - *~ 文件: $TILDE_FILES"
echo ""

# 创建备份清单
BACKUP_LIST="$PROJECT_ROOT/cleanup-backup-list-$(date +%Y%m%d_%H%M%S).txt"
log_info "正在创建备份清单: $BACKUP_LIST"

find . -type f \( -name "*.backup*" -o -name "*.bak" -o -name "*.original*" -o -name "*.old" -o -name "*~" \) 2>/dev/null > "$BACKUP_LIST"

log_info "备份清单已保存"
log_info "查看清单: cat $BACKUP_LIST"
echo ""

# 确认删除
log_warn "⚠️  即将删除 $BACKUP_COUNT 个备份文件"
log_warn "建议先查看清单确认无误后再执行删除"
echo ""
read -p "是否继续删除? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    log_warn "已取消删除操作"
    log_info "如需手动删除，请执行："
    log_info "  find . -type f \\( -name '*.backup*' -o -name '*.bak' -o -name '*.original*' -o -name '*.old' -o -name '*~' \\) -delete"
    exit 0
fi

# 执行删除
log_info "开始删除备份文件..."

# 删除备份文件（保留.git目录中的备份，可能有用）
find . -type f \( -name "*.backup*" -o -name "*.bak" -o -name "*.original*" -o -name "*.old" -o -name "*~" \) \
    ! -path "./.git/*" \
    -delete

# 验证删除结果
REMAINING_COUNT=$(find . -type f \( -name "*.backup*" -o -name "*.bak" -o -name "*.original*" -o -name "*.old" -o -name "*~" \) 2>/dev/null | wc -l)

DELETED_COUNT=$((BACKUP_COUNT - REMAINING_COUNT))

log_info "========== 清理完成 =========="
log_info "已删除文件数: $DELETED_COUNT"
log_info "剩余文件数: $REMAINING_COUNT（.git目录中的备份文件）"
echo ""

# 计算节省空间（估算）
SAVED_SPACE=$((DELETED_COUNT * 10))  # 假设每个文件平均10KB
log_info "估算节省空间: ${SAVED_SPACE}KB (约$((SAVED_SPACE / 1024))MB)"

log_info "清理完成！"
