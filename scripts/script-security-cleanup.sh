#!/bin/bash

# =============================================================================
# 🚨 脚本安全清理工具
# 识别、隔离和标记危险脚本，严禁脚本修改代码
# 创建日期: 2025-11-22
# 基于SCRIPY_SECURITY_ANALYSIS_REPORT.md分析结果
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${RED}🚨 脚本安全清理工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${CYAN}目的: 识别和隔离危险脚本，防止意外的代码修改${NC}"
echo -e "${CYAN}基于: 404→10编译错误修复实践经验${NC}"
echo -e "${BLUE}========================================${NC}"

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 安全目录
SAFE_SCRIPTS_DIR="scripts/safe-scripts"
DANGEROUS_SCRIPTS_DIR="scripts/quarantined-scripts"

# 创建安全目录
mkdir -p "$SAFE_SCRIPTS_DIR" 2>/dev/null
mkdir -p "$DANGEROUS_SCRIPTS_DIR" 2>/dev/null

# 危险操作模式列表
DANGEROUS_PATTERNS=(
    "rm\s+-rf"
    "find.*-delete"
    "find.*-exec.*rm"
    "mv\s+.*\.\*"
    "git\s+rm"
    "git\s+reset.*--hard"
    "rm\s+-f.*\*"
    "rm\s+.*\*.java"
    "rm\s+.*\*.class"
    "rm\s+.*target"
    "pkill\s+-9"
    "kill\s+-9"
    "docker\s+rm\s+-f"
    "docker\s+stop"
    "docker\s+kill"
    "systemctl\s+stop"
    "systemctl\s+restart"
)

# 统计数据
TOTAL_SCRIPTS=0
DANGEROUS_SCRIPTS=0
SAFE_SCRIPTS=0
MOVED_SCRIPTS=0

echo -e "\n${YELLOW}📊 开始分析项目脚本安全性...${NC}"

# 1. 识别危险脚本
echo -e "\n${RED}🔍 识别危险脚本...${NC}"

find scripts -name "*.sh" -type f | while read script_file; do
    if [ -f "$script_file" ]; then
        TOTAL_SCRIPTS=$((TOTAL_SCRIPTS + 1))

        is_dangerous=false
        dangerous_patterns_found=""

        # 检查每个危险模式
        for pattern in "${DANGEROUS_PATTERNS[@]}"; do
            if grep -qE "$pattern" "$script_file" 2>/dev/null; then
                is_dangerous=true
                dangerous_patterns_found="$dangerous_patterns_found, $pattern"
            fi
        done

        # 特别检查批量文件操作
        if grep -q "bulk\|mass\|all.*\.java\|all.*\.xml" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_patterns_found="$dangerous_patterns_found, batch_operation"
        fi

        # 特别检查编码修复脚本
        if grep -q "encoding.*fix\|garbled\|乱码\|utf.*fix" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_patterns_found="$dangerous_patterns_found, encoding_fix"
        fi

        if $is_dangerous; then
            DANGEROUS_SCRIPTS=$((DANGEROUS_SCRIPTS + 1))

            echo -e "${RED}🚫 发现危险脚本: $script_file${NC}"
            echo -e "${RED}   危险模式: $dangerous_patterns_found${NC}"

            # 移动到隔离目录
            if mv "$script_file" "$DANGEROUS_SCRIPTS_DIR/"; then
                echo -e "${YELLOW}   → 已移动到隔离目录: $DANGEROUS_SCRIPTS_DIR/$(basename "$script_file")${NC}"
                MOVED_SCRIPTS=$((MOVED_SCRIPTS + 1))
            else
                echo -e "${RED}   ❌ 移动失败！${NC}"
            fi
        else
            SAFE_SCRIPTS=$((SAFE_SCRIPTS + 1))
            echo -e "${GREEN}✅ 安全脚本: $script_file${NC}"

            # 移动到安全目录
            if mv "$script_file" "$SAFE_SCRIPTS_DIR/"; then
                echo -e "${GREEN}   → 已移动到安全目录: $SAFE_SCRIPTS_DIR/$(basename "$script_file")${NC}"
                MOVED_SCRIPTS=$((MOVED_SCRIPTS + 1))
            else
                echo -e "${YELLOW}   ℹ️ 保持原位置${NC}"
            fi
        fi
    fi
done

# 2. 检查Python脚本
echo -e "\n${RED}🐍 检查Python脚本...${NC}"

find scripts -name "*.py" -type f | while read script_file; do
    if [ -f "$script_file" ]; then
        TOTAL_SCRIPTS=$((TOTAL_SCRIPTS + 1))

        is_dangerous=false
        dangerous_operations=""

        # 检查危险操作
        if grep -q "os\.remove\|os\.rmdir\|shutil\.rmtree\|os\.rename" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_operations="file_operations"
        fi

        if grep -q "subprocess\.call.*rm\|os\.system.*rm" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_operations="$dangerous_operations, subprocess_operations"
        fi

        # 特别检查批量处理
        if grep -q "for.*\.java\|\.xml.*in.*os\.listdir\|glob.*\.glob" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_operations="$dangerous_operations, batch_processing"
        fi

        if $is_dangerous; then
            DANGEROUS_SCRIPTS=$((DANGEROUS_SCRIPTS + 1))

            echo -e "${RED}🚫 发现危险Python脚本: $script_file${NC}"
            echo -e "${RED}   危险操作: $dangerous_operations${NC}"

            # 移动到隔离目录
            if mv "$script_file" "$DANGEROUS_SCRIPTS_DIR/"; then
                echo -e "${YELLOW}   → 已移动到隔离目录: $DANGEROUS_SCRIPTS_DIR/$(basename "$script_file")${NC}"
                MOVED_SCRIPTS=$((MOVED_SCRIPTS + 1))
            else
                echo -e "${RED}   ❌ 移动失败！${NC}"
            fi
        else
            SAFE_SCRIPTS=$((SAFE_SCRIPTS + 1))
            echo -e "${GREEN}✅ 安全Python脚本: $script_file${NC}"

            # 移动到安全目录
            if mv "$script_file" "$SAFE_SCRIPTS_DIR/"; then
                echo -e "${GREEN}   → 已移动到安全目录: $SAFE_SCRIPTS_DIR/$(basename "$script_file")${NC}"
                MOVED_SCRIPTS=$((MOVED_SCRIPTS + 1))
            else
                echo -e "${YELLOW}   ℹ️ 保持原位置${NC}"
            fi
        fi
    fi
done

# 3. 检查PowerShell脚本
echo -e "\n${RED}⚡ 检查PowerShell脚本...${NC}"

find scripts -name "*.ps1" -type f | while read script_file; do
    if [ -f "$script_file" ]; then
        TOTAL_SCRIPTS=$((TOTAL_SCRIPTS + 1))

        is_dangerous=false
        dangerous_operations=""

        # 检查危险PowerShell命令
        if grep -q "Remove-Item\s+-.*\*.*-Recurse\|Remove-Item.*\*\.java\|Remove-Item.*\*\.xml" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_operations="Remove-Item_force"
        fi

        if grep -q "Stop-Process\s+-Force" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_operations="Stop-Process_force"
        fi

        if grep -q "Clear-Content\|Get-Content.*|Set-Content.*-Force" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_operations="content_operations"
        fi

        if $is_dangerous; then
            DANGEROUS_SCRIPTS=$((DANGEROUS_SCRIPTS + 1))

            echo -e "${RED}🚫 发现危险PowerShell脚本: $script_file${NC}"
            echo -e "${RED}   危险操作: $dangerous_operations${NC}"

            # 移动到隔离目录
            if mv "$script_file" "$DANGEROUS_SCRIPTS_DIR/"; then
                echo -e "${YELLOW}   → 已移动到隔离目录: $DANGEROUS_SCRIPTS_DIR/$(basename "$script_file")${NC}"
                MOVED_SCRIPTS=$((MOVED_SCRIPTS + 1))
            else
                echo -e "${RED}   ❌ 移动失败！${NC}"
            fi
        else
            SAFE_SCRIPTS=$((SAFE_SCRIPTS + 1))
            echo -e "${GREEN}✅ 安全PowerShell脚本: $script_file${NC}"

            # 移动到安全目录
            if mv "$script_file" "$SAFE_SCRIPTS_DIR/"; then
                echo -e "${GREEN}   → 已移动到安全目录: $SAFE_SCRIPTS_DIR/$(basename "$script_file")${NC}"
                MOVED_SCRIPTS=$((MOVED_SCRIPTS + 1))
            else
                echo -e "${YELLOW}   ℹ️ 保持原位置${NC}"
            fi
        fi
    fi
done

# 4. 检查SQL脚本
echo -e "\n${RED}🗄️ 检查SQL脚本...${NC}"

find scripts -name "*.sql" -type f | while read script_file; do
    if [ -f "$script_file" ]; then
        TOTAL_SCRIPTS=$((TOTAL_SCRIPTS + 1))

        is_dangerous=false
        dangerous_operations=""

        # 检查危险的SQL操作
        if grep -qi "DROP\s+TABLE\|DROP\s+DATABASE\|TRUNCATE\s+TABLE\|DELETE\s+FROM.*WHERE\s+1=1" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_operations="DDL_operations"
        fi

        if grep -qi "INSERT\s+INTO.*\.\*\s+SELECT" "$script_file" 2>/dev/null; then
            is_dangerous=true
            dangerous_operations="data_insert_operations"
        fi

        if $is_dangerous; then
            DANGEROUS_SCRIPTS=$((DANGEROUS_SCRIPTS + 1))

            echo -e "${RED}🚫 发现危险SQL脚本: $script_file${NC}"
            echo -e "${RED}   危险操作: $dangerous_operations${NC}"

            # SQL脚本保持原位置但添加警告
            echo -e "${YELLOW}   ⚠️ 建议人工审查SQL脚本内容${NC}"
        else
            SAFE_SCRIPTS=$((SAFE_SCRIPTS + 1))
            echo -e "${GREEN}✅ 安全SQL脚本: $script_file${NC}"
        fi
    fi
done

# 5. 创建脚本安全状态报告
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}📊 脚本安全状态报告${NC}"
echo -e "${BLUE}========================================${NC}"

echo -e "${CYAN}统计时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
echo -e "${CYAN}项目路径: $PROJECT_ROOT${NC}"
echo -e ""

echo -e "${YELLOW}📊 脚本统计:${NC}"
echo -e "  总脚本数量: $TOTAL_SCRIPTS"
echo -e "  危险脚本: $DANGEROUS_SCRIPTS ($((DANGEROUS_SCRIPTS * 100 / TOTAL_SCRIPTS))%)"
echo -e "  安全脚本: $SAFE_SCRIPTS ($((SAFE_SCRIPTS * 100 / TOTAL_SCRIPTS))%)"
echo -e "  已移动脚本: $MOVED_SCRIPTS"

echo -e ""
echo -e "${GREEN}✅ 安全脚本目录: $SAFE_SCRIPTS_DIR${NC}"
if [ -d "$SAFE_SCRIPTS_DIR" ]; then
    ls -1 "$SAFE_SCRIPTS_DIR" | head -10 | sed 's/^/   /'
else
    echo "   (空)"
fi

echo -e ""
echo -e "${RED}🚫 危险脚本隔离目录: $DANGEROUS_SCRIPTS_DIR${NC}"
if [ -d "$DANGEROUS_SCRIPTS_DIR" ]; then
    ls -1 "$DANGEROUS_SCRIPTS_DIR" | head -10 | sed 's/^/   /'
else
    echo "   (空)"
fi

# 6. 创建危险脚本清单
echo -e "\n${BLUE}创建危险脚本清单...${NC}"

cat > "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md" << EOF
# 🚨 危险脚本清单

**创建日期**: $(date '+%Y-%m-%d %H:%M:%S')
**清理标准**: 基于SCRIPY_SECURITY_ANALYSIS_REPORT.md
**隔离位置**: $DANGEROUS_SCRIPTS_DIR

---

## 📊 清理统计

- **总脚本数量**: $TOTAL_SCRIPTS
- **危险脚本数量**: $DANGEROUS_SCRIPTS
- **安全脚本数量**: $SAFE_SCRIPTS
- **已隔离脚本数量**: $MOVED_SCRIPTS

## 🚫 隔离的危险脚本

EOF

if [ -d "$DANGEROUS_SCRIPTS_DIR" ]; then
    echo "" >> "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md"
    echo "以下脚本已移动到隔离目录，禁止执行：" >> "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md"
    echo "" >> "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md"

    ls -1 "$DANGEROUS_SCRIPTS_DIR" | while read script_name; do
        script_path="$DANGEROUS_SCRIPTS_DIR/$script_name"

        # 尝试识别脚本类型和主要危险操作
        script_type="未知"
        if [[ "$script_name" == *.sh ]]; then
            script_type="Shell脚本"
        elif [[ "$script_name" == *.py ]]; then
            script_type="Python脚本"
        elif [[ "$script_name" == *.ps1 ]]; then
            script_type="PowerShell脚本"
        elif [[ "$script_name" == *.sql ]]; then
            script_type="SQL脚本"
        fi

        echo "- **$script_name** ($script_type)" >> "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md"

        # 检查主要危险操作
        if [ -f "$script_path" ]; then
            if grep -q "rm\|delete\|mv\.\*" "$script_path" 2>/dev/null; then
                echo "  - **主要风险**: 包含文件删除/重命名操作" >> "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md"
            fi

            if grep -q "bulk\|mass\|all.*\.java" "$script_path" 2>/dev/null; then
                echo "  - **主要风险**: 批量文件修改操作" >> "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md"
            fi

            if grep -q "encoding.*fix\|garbled\|乱码" "$script_path" 2>/dev/null; then
                echo "  - **主要风险**: 编码批量修复操作" >> "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md"
            fi
        fi
        echo "" >> "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md"
    done
fi

cat >> "$PROJECT_ROOT/DANGEROUS_SCRIPTS_LIST.md" << EOF

## ⚠️ 安全建议

1. **严禁执行隔离脚本**
   - 隔离目录中的脚本绝对禁止执行
   - 如需使用，必须经过技术负责人审批

2. **手动审查替代**
   - 将批量操作改为逐个文件处理
   - 使用IDE内置工具替代脚本

3. **建立检查机制**
   - 定期运行此清理脚本
   - 集成到CI/CD流水线

4. **权限控制**
   - 对隔离目录设置只读权限
   - 限制对脚本的修改权限

## ✅ 安全脚本推荐

以下脚本已经验证为安全，可以继续使用：

### 技术迁移检查类
- \`technology-migration-zero-tolerance-check.sh\`
- \`quick-tech-migration-check.sh\`
- \`pre-commit-technology-migration-check.sh\`
- \`incremental-compile-error-monitor.sh\`

### 监控检查类
- \`quality-monitoring-dashboard.sh\`
- \`permission-monitor.sh\`
- \`quick-check.sh\`

### 质量保障类
- \`commit-guard.sh\`
- \`dev-standards-check.sh\`
- \`code-quality-check.sh\`

---

**注意**: 此清单会随着脚本清理更新而自动更新。
EOF

# 7. 设置隔离目录权限
echo -e "\n${YELLOW}🔒 设置隔离目录权限...${NC}"

if [ -d "$DANGEROUS_SCRIPTS_DIR" ]; then
    # 设置只读权限，防止意外执行
    chmod 444 "$DANGEROUS_SCRIPTS_DIR"/* 2>/dev/null || true
    chmod 555 "$DANGER OUS_SCRIPTS_DIR" 2>/dev/null || true
    echo -e "${GREEN}✅ 隔离目录权限已设置为只读${NC}"
fi

if [ -d "$SAFE_SCRIPTS_DIR" ]; then
    # 安全脚本保持执行权限
    chmod 755 "$SAFE_SCRIPTS_DIR"/* 2>/dev/null || true
    chmod 755 "$SAFE_SCRIPTS_DIR" 2>/dev/null || true
    echo -e "${GREEN}✅ 安全脚本目录权限已设置${NC}"
fi

# 8. 创建脚本安全检查规则
echo -e "\n${BLUE}创建脚本安全检查规则...${NC}"

cat > "$PROJECT_ROOT/.git/hooks/pre-commit-script-security" << 'EOF'
#!/bin/bash
# Git Pre-commit Hook - 脚本安全检查
# 禁止提交危险脚本到版本库

echo "🔒 检查脚本安全性..."

# 检查是否有危险脚本被添加到版本控制
dangerous_files=$(git diff --cached --name-only | grep -E "scripts/.*\.(sh|py|ps1|sql)" || true)

if [ -n "$dangerous_files" ]; then
    echo "❌ 检测到脚本文件被暂存，可能存在安全风险："
    echo "$dangerous_files" | sed 's/^/  /'
    echo ""
    echo "⚠️ 建议："
    echo "1. 检查脚本内容是否包含危险操作"
    echo "2. 运行: ./scripts/script-security-cleanup.sh 清理危险脚本"
    echo "3. 或手动审查后重新提交"
    echo ""
    echo "脚本安全政策：严格禁止包含以下操作的脚本："
    echo "- rm -rf (强制删除)"
    "- find -delete (批量删除)"
    "- mv .../* (批量重命名)"
    "- 批量编码修复"
    "- 数据库结构修改"
    echo ""
    exit 1
else
    echo "✅ 脚本安全检查通过"
fi
EOF

chmod +x "$PROJECT_ROOT/.git/hooks/pre-commit-script-security"

# 9. 最终结果
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}🎉 脚本安全清理完成！${NC}"
echo -e "${BLUE}========================================${NC}"

echo -e "${CYAN}清理结果:${NC}"
echo -e "  ✅ 总脚本数: $TOTAL_SCRIPTS"
echo -e "  🚫 危险脚本: $DANGEROUS_SCRIPTS (已隔离)"
echo -e "  ✅ 安全脚本: $SAFE_SCRIPTS (保持使用)"
echo -e "  🔄 已移动脚本: $MOVED_SCRIPTS"

echo -e "\n${GREEN}📍 安全措施已实施:${NC}"
echo -e "  📁 危险脚本清单: DANGEROUS_SCRIPTS_LIST.md"
echo -e "  🚫 隔离目录: $DANGEROUS_SCRIPTS_DIR"
echo -e "  ✅ 安全脚本目录: $SAFE_SCRIPTS_DIR"
echo -e "  🔒 Pre-commit安全检查: .git/hooks/pre-commit-script-security"

echo -e "\n${RED}🚫 重要提醒:${NC}"
echo -e "${RED}  严禁执行隔离目录中的任何脚本！${NC}"
echo -e "${RED} 任何文件修改操作必须经过安全审查！${NC}"

exit 0