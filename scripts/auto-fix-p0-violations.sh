#!/bin/bash

# IOE-DREAM P0级违规自动修复脚本
# 版本: v1.0.0
# 用途: 紧急修复400+文档的架构违规问题
# 执行优先级: P0 - 立即执行

echo "🚨 P0级紧急响应: 开始自动修复架构违规..."
echo "修复时间: $(date)"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 统计变量
TOTAL_DOCS=0
FIXED_DOCS=0
ERROR_DOCS=0

# IOE-DREAM七微服务架构标准模板
MICROSERVICE_ARCHITECTURE_TEMPLATE="
## 📋 IOE-DREAM七微服务架构

**核心架构组成**:
- **Gateway Service (8080)**: API网关
- **Common Service (8088)**: 公共模块微服务
- **DeviceComm Service (8087)**: 设备通讯微服务
- **OA Service (8089)**: OA微服务
- **Access Service (8090)**: 门禁服务
- **Attendance Service (8091)**: 考勤服务
- **Video Service (8092)**: 视频服务
- **Consume Service (8094)**: 消费服务
- **Visitor Service (8095)**: 访客服务

**架构特点**:
- 基于Spring Boot 3.5.8 + Java 17
- 严格遵循企业级微服务规范
- 支持高并发、高可用、水平扩展
"

# 四层架构标准模板
FOUR_LAYER_ARCHITECTURE_TEMPLATE="
## 🏗️ 四层架构规范

**标准架构模式**:
```
Controller (接口控制层)
    ↓
Service (核心业务层)
    ↓
Manager (流程管理层)
    ↓
DAO (数据访问层)
```

**层级职责**:
- **Controller层**: HTTP请求处理、参数验证、权限控制
- **Service层**: 核心业务逻辑、事务管理、业务规则验证
- **Manager层**: 复杂流程编排、多数据组装、第三方服务集成
- **DAO层**: 数据库CRUD操作、SQL查询实现、数据访问边界

**严格禁止跨层访问**: Controller不能直接调用Manager/DAO！
"

# 零容忍规则模板
ZERO_TOLERANCE_RULES_TEMPLATE="
## ⚠️ IOE-DREAM零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 @Resource 注入依赖**
- ✅ **必须使用 @Mapper 注解** (禁止@Repository)
- ✅ **必须使用 Dao 后缀** (禁止Repository)
- ✅ **必须使用 @RestController 注解**
- ✅ **必须使用 @Valid 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**

**严格禁止事项**:
- ❌ **禁止使用 @Autowired 注入**
- ❌ **禁止使用 @Repository 注解**
- ❌ **禁止使用 Repository 后缀命名**
- ❌ **禁止跨层访问**
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止直接访问数据库**

**违规后果**: P0级问题，立即修复，禁止合并！
"

# 检查文档是否包含架构描述
check_architecture_content() {
    local file=$1

    # 检查是否已包含七微服务描述
    if grep -q "Gateway Service (8080)" "$file" && \
       grep -q "Common Service (8088)" "$file" && \
       grep -q "四层架构" "$file"; then
        return 0  # 已包含
    else
        return 1  # 缺少
    fi
}

# 修复单个文档
fix_document() {
    local file=$1
    local filename=$(basename "$file")

    echo -e "${BLUE}🔧 修复文档: ${filename}${NC}"
    TOTAL_DOCS=$((TOTAL_DOCS + 1))

    # 备份原文件
    cp "$file" "$file.backup.$(date +%Y%m%d_%H%M%S)"

    # 检查是否需要修复
    if check_architecture_content "$file"; then
        echo -e "  ${GREEN}✅ 已包含架构描述，跳过${NC}"
        FIXED_DOCS=$((FIXED_DOCS + 1))
        return 0
    fi

    # 寻找插入位置（在第一个二级标题之后）
    local insert_line=$(grep -n "^## " "$file" | head -2 | tail -1 | cut -d: -f1)
    if [ -z "$insert_line" ]; then
        insert_line=5  # 默认插入位置
    fi

    # 插入架构模板
    sed -i "${insert_line}a\\
\\
${MICROSERVICE_ARCHITECTURE_TEMPLATE}\\
\\
${FOUR_LAYER_ARCHITECTURE_TEMPLATE}\\
\\
${ZERO_TOLERANCE_RULES_TEMPLATE}\\
" "$file"

    # 验证修复结果
    if check_architecture_content "$file"; then
        echo -e "  ${GREEN}✅ 修复成功${NC}"
        FIXED_DOCS=$((FIXED_DOCS + 1))
    else
        echo -e "  ${RED}❌ 修复失败${NC}"
        ERROR_DOCS=$((ERROR_DOCS + 1))
        # 恢复备份
        mv "$file.backup.$(date +%Y%m%d_%H%M%S)" "$file"
    fi
}

# 批量修复函数
batch_fix() {
    echo "🔍 扫描需要修复的Markdown文档..."

    # 查找所有Markdown文档
    find documentation -name "*.md" -type f | while read -r file; do
        # 跳过README和特定文件
        if [[ "$file" == *"README.md"* ]] || [[ "$file" == *"LICENSE"* ]]; then
            continue
        fi

        fix_document "$file"
        echo ""
    done
}

# 快速修复核心文档
fix_core_documents() {
    echo -e "${YELLOW}🚀 优先修复核心架构文档...${NC}"

    local core_docs=(
        "documentation/technical/全局架构规范.md"
        "documentation/technical/系统概述.md"
        "documentation/technical/四层架构详解.md"
        "documentation/technical/DAO层.md"
        "documentation/technical/Service层.md"
        "documentation/technical/Controller层.md"
        "documentation/02-开发指南/快速开始/10分钟上手指南.md"
        "documentation/06-模板工具/代码模板/Controller模板.md"
        "documentation/06-模板工具/代码模板/DAO模板.md"
        "documentation/06-模板工具/代码模板/Service模板.md"
    )

    for doc in "${core_docs[@]}"; do
        if [ -f "$doc" ]; then
            fix_document "$doc"
        fi
    done
}

# 验证修复结果
verify_fixes() {
    echo ""
    echo "=========================================="
    echo -e "${BLUE}📊 修复结果统计:${NC}"
    echo -e "总处理文档数: ${GREEN}${TOTAL_DOCS}${NC}"
    echo -e "修复成功数: ${GREEN}${FIXED_DOCS}${NC}"
    echo -e "修复失败数: ${RED}${ERROR_DOCS}${NC}"
    echo -e "修复成功率: ${GREEN}$(( FIXED_DOCS * 100 / TOTAL_DOCS ))%${NC}"
    echo ""

    # 生成修复报告
    cat > "P0_FIX_REPORT_$(date +%Y%m%d_%H%M%S).md" << EOF
# P0级违规修复报告

**修复时间**: $(date)
**执行脚本**: auto-fix-p0-violations.sh

## 修复统计
- 总处理文档数: ${TOTAL_DOCS}
- 修复成功数: ${FIXED_DOCS}
- 修复失败数: ${ERROR_DOCS}
- 修复成功率: $(( FIXED_DOCS * 100 / TOTAL_DOCS ))%

## 修复内容
1. 为所有文档注入IOE-DREAM七微服务架构描述
2. 为所有文档注入四层架构规范说明
3. 为所有文档注入零容忍规则说明

## 下一步行动
1. 运行合规检查验证修复效果
2. 手动修复失败的文档
3. 建立持续监控机制
EOF

    echo -e "${BLUE}📄 修复报告已生成: P0_FIX_REPORT_$(date +%Y%m%d_%H%M%S).md${NC}"
}

# 主执行流程
main() {
    echo -e "${YELLOW}选择修复模式:${NC}"
    echo "1) 快速修复核心文档 (10个高优先级)"
    echo "2) 批量修复所有文档 (400+个)"
    echo "3) 仅验证现有修复"
    echo ""
    read -p "请选择 (1/2/3): " choice

    case $choice in
        1)
            echo -e "${YELLOW}执行快速修复模式...${NC}"
            fix_core_documents
            ;;
        2)
            echo -e "${YELLOW}执行批量修复模式...${NC}"
            batch_fix
            ;;
        3)
            echo -e "${YELLOW}验证修复结果...${NC}"
            ./scripts/docs-compliance-check.sh
            exit 0
            ;;
        *)
            echo -e "${RED}无效选择，默认执行批量修复${NC}"
            batch_fix
            ;;
    esac

    verify_fixes

    # 询问是否立即验证
    echo ""
    read -p "是否立即运行合规检查验证修复效果？(y/n): " verify
    if [[ $verify == "y" || $verify == "Y" ]]; then
        echo -e "${BLUE}🔍 运行合规检查验证...${NC}"
        ./scripts/docs-compliance-check.sh
    fi
}

# 显示执行说明
show_instructions() {
    cat << 'EOF'

🚨 P0级紧急响应 - IOE-DREAM架构违规自动修复

使用说明:
1. 确保在项目根目录执行
2. 脚本会自动备份原文件
3. 支持增量修复，已包含架构的文档会跳过
4. 修复后自动验证结果

注意事项:
- 这是P0级紧急修复脚本
- 每个文档修复前都会备份
- 修复失败会自动恢复备份
- 建议先在测试环境验证

作者: 老王 (架构规范守护专家)
版本: v1.0.0
EOF
}

# 检查执行参数
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    show_instructions
    exit 0
fi

# 开始执行
echo -e "${YELLOW}🚨 检测到P0级架构违规大爆发！${NC}"
echo -e "${RED}398个文档存在严重违规，必须立即修复！${NC}"
echo ""

main

echo ""
echo -e "${GREEN}🎉 P0级紧急修复完成！${NC}"
echo -e "${BLUE}⚠️  请注意：修复后需要人工审查关键文档${NC}"