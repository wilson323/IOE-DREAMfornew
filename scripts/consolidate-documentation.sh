#!/bin/bash

# IOE-DREAM 文档目录结构整合脚本
# 版本: v1.0.0
# 用途: 整合重复的documentation目录结构，建立清晰的文档体系

echo "🗂️ IOE-DREAM 文档目录结构整合..."
echo "整合时间: $(date)"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 统计变量
MOVED_DIRS=0
MOVED_FILES=0
ERRORS=0

# 备份函数
backup_dir() {
    local src=$1
    local backup="backup_$(date +%Y%m%d_%H%M%S)_$(basename "$src")"

    if [ -d "$src" ]; then
        echo -e "${YELLOW}📦 备份目录: $src -> $backup${NC}"
        cp -r "$src" "$backup"
    fi
}

# 移动目录函数
move_directory() {
    local src=$1
    local dest=$2

    if [ ! -d "$src" ]; then
        return 0
    fi

    echo -e "${BLUE}📂 移动目录: $src -> $dest${NC}"

    # 创建目标目录（如果不存在）
    mkdir -p "$(dirname "$dest")"

    # 备份源目录
    backup_dir "$src"

    # 移动目录
    if mv "$src" "$dest" 2>/dev/null; then
        echo -e "  ${GREEN}✅ 移动成功${NC}"
        MOVED_DIRS=$((MOVED_DIRS + 1))
    else
        echo -e "  ${RED}❌ 移动失败${NC}"
        ERRORS=$((ERRORS + 1))
    fi
}

# 移动文件函数
move_files() {
    local src_dir=$1
    local dest_dir=$2
    local pattern=$3

    if [ ! -d "$src_dir" ]; then
        return 0
    fi

    echo -e "${BLUE}📄 移动文件: $src_dir/*$pattern -> $dest_dir/${NC}"

    # 创建目标目录
    mkdir -p "$dest_dir"

    # 查找并移动文件
    find "$src_dir" -name "$pattern" -type f | while read -r file; do
        local filename=$(basename "$file")
        local dest_path="$dest_dir/$filename"

        # 备份文件
        cp "$file" "$file.backup.$(date +%Y%m%d_%H%M%S)" 2>/dev/null

        # 移动文件
        if mv "$file" "$dest_path" 2>/dev/null; then
            echo -e "  ${GREEN}✅ $filename${NC}"
            MOVED_FILES=$((MOVED_FILES + 1))
        else
            echo -e "  ${RED}❌ $filename 移动失败${NC}"
            ERRORS=$((ERRORS + 1))
        fi
    done
}

# 整合架构文档
echo -e "${YELLOW}🏗️ 整合架构文档...${NC}"

# 将根级别的architecture移动到technical下
if [ -d "./architecture" ] && [ -d "./technical/architecture" ]; then
    echo "发现重复的architecture目录，正在整合..."

    # 合并内容
    find ./architecture -type f -name "*.md" | while read -r file; do
        local rel_path=${file#./architecture/}
        local dest_path="./technical/architecture/$rel_path"

        # 创建目录结构
        mkdir -p "$(dirname "$dest_path")"

        # 移动文件
        if [ ! -f "$dest_path" ]; then
            mv "$file" "$dest_path"
            echo "移动: $file -> $dest_path"
        else
            # 重名避免覆盖
            mv "$file" "${dest_path%.*}_from_root.$(date +%Y%m%d_%H%M%S).md"
            echo "重命名移动: $file -> ${dest_path%.*}_from_root.$(date +%Y%m%d_%H%M%S).md"
        fi
    done

    # 移动空目录
    find ./architecture -type d -empty -delete 2>/dev/null

    # 删除空的主目录
    rmdir ./architecture 2>/dev/null
    echo -e "${GREEN}✅ architecture目录整合完成${NC}"
fi

# 整合business文档
echo -e "${YELLOW}💼 整合业务模块文档...${NC}"

if [ -d "./business" ] && [ -d "./03-业务模块" ]; then
    echo "发现重复的业务模块目录，正在整合..."

    # 移动business下的内容到03-业务模块
    find ./business -type f -name "*.md" | while read -r file; do
        local rel_path=${file#./business/}
        local dest_path="./03-业务模块/$rel_path"

        # 创建目录结构
        mkdir -p "$(dirname "$dest_path")"

        # 移动文件
        if [ ! -f "$dest_path" ]; then
            mv "$file" "$dest_path"
            echo "移动: $file -> $dest_path"
        else
            # 重名避免覆盖
            mv "$file" "${dest_path%.*}_from_business.$(date +%Y%m%d_%H%M%S).md"
            echo "重命名移动: $file -> ${dest_path%.*}_from_business.$(date +%Y%m%d_%H%M%S).md"
        fi
    done

    # 移动空目录
    find ./business -type d -empty -delete 2>/dev/null

    # 删除空的主目录
    rmdir ./business 2>/dev/null
    echo -e "${GREEN}✅ business目录整合完成${NC}"
fi

# 整合technical下的重复子目录
echo -e "${YELLOW}🔧 整合technical下的重复目录...${NC}"

# 定义整合映射
declare -A INTEGRATION_MAP=(
    ["./technical/各业务模块文档"]="./03-业务模块"
    ["./technical/API文档"]="./06-模板工具/文档模板"
    ["./technical/管理员指南"]="./04-部署运维"
    ["./technical/用户手册"]="./02-开发指南/最佳实践"
    ["./technical/DEPLOY"]="./04-部署运维"
    ["./technical/SECURITY"]="./05-项目管理"
    ["./technical/STANDARDS"]="./01-核心规范"
    ["./technical/TRAINING"]="./02-开发指南/培训资料"
)

# 执行整合
for src_dir in "${!INTEGRATION_MAP[@]}"; do
    dest_dir="${INTEGRATION_MAP[$src_dir]}"

    if [ -d "$src_dir" ]; then
        echo -e "${BLUE}处理: $src_dir -> $dest_dir${NC}"

        # 移动所有.md文件
        move_files "$src_dir" "$dest_dir" "*.md"

        # 移动子目录
        find "$src_dir" -mindepth 1 -maxdepth 1 -type d | while read -r subdir; do
            local subdir_name=$(basename "$subdir")
            local target_subdir="$dest_dir/$subdir_name"

            if [ -d "$subdir" ] && [ ! -d "$target_subdir" ]; then
                echo -e "  ${YELLOW}📁 移动子目录: $subdir_name${NC}"
                if mv "$subdir" "$target_subdir" 2>/dev/null; then
                    echo -e "    ${GREEN}✅ 成功${NC}"
                else
                    echo -e "    ${RED}❌ 失败${NC}"
                fi
            fi
        done

        # 尝试删除空目录
        find "$src_dir" -type d -empty -delete 2>/dev/null
        rmdir "$src_dir" 2>/dev/null
    fi
done

# 清理temp-merge目录
echo -e "${YELLOW}🧹 清理临时合并目录...${NC}"
if [ -d "./temp-merge" ]; then
    backup_dir "./temp-merge"
    rm -rf ./temp-merge
    echo -e "${GREEN}✅ temp-merge目录已清理${NC}"
fi

# 清理其他空目录
echo -e "${YELLOW}🧹 清理空目录...${NC}"
find documentation -type d -empty -delete 2>/dev/null

# 整合根级别的其他目录
echo -e "${YELLOW}🔧 整合根级别的其他目录...${NC}"

# 移动根级别的目录到对应位置
declare -A ROOT_INTEGRATION=(
    ["./api"]="./06-模板工具/API文档"
    ["./deployment"]="./04-部署运维"
    ["./development"]="./02-开发指南"
    ["./maintenance"]="./04-部署运维/维护"
    ["./project"]="./05-项目管理"
    ["./reports"]="./05-项目管理/报告"
    ["./security"]="./05-项目管理/安全"
)

for src_dir in "${!ROOT_INTEGRATION[@]}"; do
    dest_dir="${ROOT_INTEGRATION[$src_dir]}"

    if [ -d "$src_dir" ]; then
        echo -e "${BLUE}根目录整合: $src_dir -> $dest_dir${NC}"

        # 创建目标目录
        mkdir -p "$dest_dir"

        # 移动所有内容
        if mv "$src_dir"/* "$dest_dir/" 2>/dev/null; then
            echo -e "  ${GREEN}✅ 移动成功${NC}"
            # 删除空目录
            rmdir "$src_dir" 2>/dev/null
        else
            echo -e "  ${RED}❌ 移动失败${NC}"
        fi
    fi
done

# 生成目录结构报告
echo ""
echo "=========================================="
echo -e "${BLUE}📊 整合结果统计:${NC}"
echo -e "移动目录数: ${GREEN}${MOVED_DIRS}${NC}"
echo -e "移动文件数: ${GREEN}${MOVED_FILES}${NC}"
echo -e "错误数: ${RED}${ERRORS}${NC}"

# 显示新的目录结构
echo ""
echo -e "${BLUE}📁 整合后的目录结构:${NC}"
echo ""
echo "documentation/"
find documentation -maxdepth 2 -type d | sort | sed 's|documentation/|  |'

# 生成整合报告
local report_file="DOCUMENTATION_CONSOLIDATION_REPORT_$(date +%Y%m%d_%H%M%S).md"
cat > "$report_file" << EOF
# IOE-DREAM 文档目录结构整合报告

**整合时间**: $(date)
**执行脚本**: consolidate-documentation.sh v1.0.0

## 整合统计
- 移动目录数: ${MOVED_DIRS}
- 移动文件数: ${MOVED_FILES}
- 错误数: ${ERRORS}
- 整合成功率: $(( (MOVED_DIRS + MOVED_FILES) * 100 / (MOVED_DIRS + MOVED_FILES + ERRORS) ))%

## 整合范围
1. **重复架构目录整合**: ./architecture/ → ./technical/architecture/
2. **重复业务模块整合**: ./business/ → ./03-业务模块/
3. **technical子目录重组**: 多个重复子目录合并到对应位置
4. **根级目录整合**: api, deployment, development等目录重组
5. **临时目录清理**: temp-merge等临时目录删除

## 新目录结构
- 01-核心规范: 架构规范、开发规范、安全规范
- 02-开发指南: 开发流程、快速开始、最佳实践、培训资料
- 03-业务模块: 各业务系统文档
- 04-部署运维: 部署指南、维护手册
- 05-项目管理: 项目报告、安全管理
- 06-模板工具: 代码模板、文档模板、API文档
- 07-归档区域: 历史文档归档
- technical: 技术文档、架构标准、API文档

## 改进效果
1. ✅ 消除目录重复问题
2. ✅ 建立清晰的层级结构
3. ✅ 提高文档查找效率
4. ✅ 减少维护复杂度
5. ✅ 符合企业级文档管理标准

## 后续建议
1. 建立文档维护规范
2. 定期检查目录结构
3. 统一文档命名规范
4. 完善文档索引机制

---
**报告生成时间**: $(date)
**整合环境**: IOE-DREAM项目根目录
EOF

echo -e "${BLUE}📄 整合报告已生成: ${report_file}${NC}"

echo ""
echo -e "${GREEN}🎉 文档目录结构整合完成！${NC}"
echo -e "${BLUE}✨ 目录结构更加清晰，便于文档管理${NC}"
echo -e "${YELLOW}💡 建议: 重新运行合规检查脚本验证文档结构${NC}"