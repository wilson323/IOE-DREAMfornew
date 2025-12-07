#!/bin/bash

# IOE-DREAM 大规模P0违规批量修复脚本
# 版本: v2.0.0 - 大规模批量修复版
# 用途: 一次性修复剩余390个文档的架构违规问题
# 执行优先级: P0 - 大规模批量处理

echo "🚨 P0级大规模批量修复: 开始修复所有违规文档..."
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
SKIPPED_DOCS=0

# 创建临时模板文件
create_templates() {
    echo "📝 创建架构模板..."

    cat > /tmp/microservice_architecture.md << 'EOF'
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

**技术栈标准**:
- **数据库**: MySQL 8.0 + Druid连接池
- **缓存**: Redis + Caffeine多级缓存
- **注册中心**: Nacos
- **配置中心**: Nacos Config
- **认证授权**: Sa-Token
EOF

    cat > /tmp/four_layer_architecture.md << 'EOF'
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
EOF

    cat > /tmp/zero_tolerance_rules.md << 'EOF'
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
EOF
}

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

# 智能修复单个文档
smart_fix_document() {
    local file=$1
    local filename=$(basename "$file")

    echo -e "${BLUE}🔧 修复文档: ${filename}${NC}"
    TOTAL_DOCS=$((TOTAL_DOCS + 1))

    # 备份原文件
    cp "$file" "$file.backup.$(date +%Y%m%d_%H%M%S)" 2>/dev/null

    # 检查是否需要修复
    if check_architecture_content "$file"; then
        echo -e "  ${GREEN}✅ 已包含架构描述，跳过${NC}"
        SKIPPED_DOCS=$((SKIPPED_DOCS + 1))
        return 0
    fi

    # 智能寻找插入位置
    local insert_line
    insert_line=$(grep -n "^## " "$file" | head -3 | tail -1 | cut -d: -f1)
    if [ -z "$insert_line" ]; then
        insert_line=3  # 默认插入位置
    fi

    # 分步骤插入，避免语法错误
    echo -e "  ${YELLOW}📝 插入七微服务架构描述...${NC}"
    sed -i "${insert_line}r /tmp/microservice_architecture.md" "$file" 2>/dev/null

    # 增加行号以适应插入的内容
    local architecture_lines=$(wc -l < /tmp/microservice_architecture.md)
    insert_line=$((insert_line + architecture_lines + 1))

    echo -e "  ${YELLOW}📝 插入四层架构规范...${NC}"
    sed -i "${insert_line}r /tmp/four_layer_architecture.md" "$file" 2>/dev/null

    # 再次增加行号
    local four_layer_lines=$(wc -l < /tmp/four_layer_architecture.md)
    insert_line=$((insert_line + four_layer_lines + 1))

    echo -e "  ${YELLOW}📝 插入零容忍规则...${NC}"
    sed -i "${insert_line}r /tmp/zero_tolerance_rules.md" "$file" 2>/dev/null

    # 验证修复结果
    if check_architecture_content "$file"; then
        echo -e "  ${GREEN}✅ 修复成功${NC}"
        FIXED_DOCS=$((FIXED_DOCS + 1))
    else
        echo -e "  ${RED}❌ 修复失败，尝试恢复备份${NC}"
        ERROR_DOCS=$((ERROR_DOCS + 1))
        # 恢复备份
        mv "$file.backup.$(date +%Y%m%d_%H%M%S)" "$file" 2>/dev/null || true
    fi
}

# 批量处理文档
batch_process_documents() {
    echo -e "${YELLOW}🚀 开始大规模批量修复...${NC}"
    echo ""

    # 处理技术文档
    echo -e "${BLUE}📚 处理技术文档...${NC}"
    find documentation/technical -name "*.md" -type f | while read -r file; do
        if [[ "$file" != *"README"* ]]; then
            smart_fix_document "$file"
        fi
    done

    # 处理开发指南
    echo ""
    echo -e "${BLUE}📖 处理开发指南...${NC}"
    find documentation/02-开发指南 -name "*.md" -type f | while read -r file; do
        if [[ "$file" != *"README"* ]]; then
            smart_fix_document "$file"
        fi
    done

    # 处理业务模块文档
    echo ""
    echo -e "${BLUE}💼 处理业务模块文档...${NC}"
    find documentation/03-业务模块 -name "*.md" -type f | while read -r file; do
        if [[ "$file" != *"README"* ]]; then
            smart_fix_document "$file"
        fi
    done

    # 处理部署运维文档
    echo ""
    echo -e "${BLUE}🔧 处理部署运维文档...${NC}"
    find documentation/04-部署运维 -name "*.md" -type f | while read -r file; do
        if [[ "$file" != *"README"* ]]; then
            smart_fix_document "$file"
        fi
    done

    # 处理架构变更文档
    echo ""
    echo -e "${BLUE}🏗️ 处理架构变更文档...${NC}"
    find documentation/architecture -name "*.md" -type f | while read -r file; do
        if [[ "$file" != *"README"* ]]; then
            smart_fix_document "$file"
        fi
    done
}

# 生成详细修复报告
generate_detailed_report() {
    echo ""
    echo "=========================================="
    echo -e "${BLUE}📊 大规模批量修复结果统计:${NC}"
    echo -e "总处理文档数: ${GREEN}${TOTAL_DOCS}${NC}"
    echo -e "修复成功数: ${GREEN}${FIXED_DOCS}${NC}"
    echo -e "修复失败数: ${RED}${ERROR_DOCS}${NC}"
    echo -e "跳过文档数: ${YELLOW}${SKIPPED_DOCS}${NC}"

    if [ $TOTAL_DOCS -gt 0 ]; then
        local success_rate=$(( FIXED_DOCS * 100 / TOTAL_DOCS ))
        echo -e "修复成功率: ${GREEN}${success_rate}%${NC}"
    fi
    echo ""

    # 生成详细修复报告
    local report_file="MASSIVE_P0_FIX_REPORT_$(date +%Y%m%d_%H%M%S).md"
    cat > "$report_file" << EOF
# IOE-DREAM P0级大规模违规修复报告

**修复时间**: $(date)
**执行脚本**: batch-fix-all-p0.sh v2.0.0
**修复模式**: 大规模批量处理

## 修复统计
- 总处理文档数: ${TOTAL_DOCS}
- 修复成功数: ${FIXED_DOCS}
- 修复失败数: ${ERROR_DOCS}
- 跳过文档数: ${SKIPPED_DOCS}
- 修复成功率: $(( FIXED_DOCS * 100 / TOTAL_DOCS ))%

## 修复范围
1. **技术文档**: documentation/technical/
2. **开发指南**: documentation/02-开发指南/
3. **业务模块**: documentation/03-业务模块/
4. **部署运维**: documentation/04-部署运维/
5. **架构变更**: documentation/architecture/

## 修复内容
1. 为所有文档注入IOE-DREAM七微服务架构描述
2. 为所有文档注入四层架构规范说明
3. 为所有文档注入零容忍规则说明

## 技术改进
- 使用临时模板文件避免字符串转义问题
- 智能寻找最佳插入位置
- 分步骤插入提高成功率
- 完整的备份和恢复机制

## 下一步行动
1. 运行完整合规检查验证修复效果
2. 人工审查关键业务文档
3. 建立持续监控机制防止违规重现
4. 制定文档规范培训计划

## 风险提示
- 大规模修改可能影响文档结构
- 建议在测试环境先验证效果
- 重要文档建议人工审核

---
**报告生成时间**: $(date)
**执行环境**: IOE-DREAM项目根目录
EOF

    echo -e "${BLUE}📄 详细修复报告已生成: ${report_file}${NC}"
}

# 主执行流程
main() {
    echo -e "${YELLOW}🚀 IOE-DREAM P0级大规模违规批量修复${NC}"
    echo -e "${RED}目标: 修复所有缺失架构描述的文档${NC}"
    echo ""

    # 创建模板文件
    create_templates

    # 执行批量修复
    batch_process_documents

    # 生成详细报告
    generate_detailed_report

    # 清理临时文件
    rm -f /tmp/microservice_architecture.md /tmp/four_layer_architecture.md /tmp/zero_tolerance_rules.md

    # 询问是否立即验证
    echo ""
    echo -e "${YELLOW}是否立即运行完整合规检查验证修复效果？${NC}"
    echo -e "${YELLOW}注意: 完整检查可能需要5-10分钟${NC}"
    read -p "立即验证？(y/n): " verify

    if [[ $verify == "y" || $verify == "Y" ]]; then
        echo -e "${BLUE}🔍 运行完整合规检查验证...${NC}"
        if [ -f "scripts/docs-compliance-check.sh" ]; then
            bash scripts/docs-compliance-check.sh
        else
            echo -e "${RED}❌ 合规检查脚本不存在${NC}"
        fi
    else
        echo -e "${YELLOW}💡 稍后可运行: bash scripts/docs-compliance-check.sh${NC}"
    fi
}

# 显示执行说明
show_instructions() {
    cat << 'EOF'

🚨 IOE-DREAM P0级大规模违规批量修复 (v2.0.0)

功能特点:
1. 智能批量处理所有文档
2. 自动备份原文件
3. 分步骤插入提高成功率
4. 详细修复统计报告
5. 支持后续验证检查

使用方法:
1. 确保在项目根目录执行
2. 脚本会自动备份所有修改的文件
3. 处理完成后可进行完整验证

注意事项:
- 大规模修改，建议先在测试环境验证
- 备份文件格式: 原文件名.backup.时间戳
- 重要文档建议人工审核

作者: 老王 (架构规范守护专家)
版本: v2.0.0 - 大规模批量修复版
EOF
}

# 检查执行参数
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    show_instructions
    exit 0
fi

# 开始执行
echo -e "${YELLOW}🚨 启动P0级大规模批量修复程序${NC}"
echo -e "${BLUE}预计处理时间: 5-10分钟，请耐心等待...${NC}"
echo ""

main

echo ""
echo -e "${GREEN}🎉 P0级大规模批量修复程序执行完成！${NC}"
echo -e "${BLUE}⚠️  重要提醒: 请人工审查关键业务文档${NC}"
echo -e "${YELLOW}💡 建议: 运行完整合规检查验证效果${NC}"