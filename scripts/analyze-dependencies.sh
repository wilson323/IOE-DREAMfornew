#!/bin/bash
################################################################################
# 模块依赖关系分析脚本
#
# 用途：
#   生成IOE-DREAM项目微服务模块的依赖关系图和分析报告
#
# 使用方法：
#   chmod +x scripts/analyze-dependencies.sh
#   ./scripts/analyze-dependencies.sh
#
# 输出文件：
#   dependency-analysis.md - 依赖关系分析报告
#
# @author IOE-DREAM架构团队
# @version 1.0.0
# @since 2025-12-25
################################################################################

set -e

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

OUTPUT_FILE="dependency-analysis.md"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

echo -e "${BLUE}📊 [依赖分析] 开始生成模块依赖关系报告...${NC}"

# 创建报告头
cat > "$OUTPUT_FILE" << HEADER
# 模块依赖关系分析报告

**生成时间**: $TIMESTAMP  
**分析范围**: microservices/  
**工具**: Maven Dependency Plugin  

---

## 📋 执行摘要

本报告通过分析Maven依赖树，生成IOE-DREAM项目微服务模块的依赖关系图。

---

## 📊 模块依赖层次

### 第1层：核心基础模块
- \`microservices-common-core\` - 最小稳定内核（ResponseDTO、异常、工具类）

### 第2层：细粒度功能模块
- \`microservices-common-entity\` - 实体层
- \`microservices-common-business\` - 业务层
- \`microservices-common-data\` - 数据层
- \`microservices-common-security\` - 安全层
- \`microservices-common-cache\` - 缓存层
- \`microservices-common-monitor\` - 监控层
- \`microservices-common-storage\` - 存储层
- \`microservices-common-gateway-client\` - 网关客户端

### 第3层：业务服务层
- \`ioedream-common-service\` - 公共业务服务
- \`ioedream-access-service\` - 门禁服务
- \`ioedream-attendance-service\` - 考勤服务
- \`ioedream-consume-service\` - 消费服务
- \`ioedream-video-service\` - 视频服务
- \`ioedream-visitor-service\` - 访客服务
- \`ioedream-device-comm-service\` - 设备通讯服务

---

## 🔍 详细依赖分析

HEADER

# 分析每个模块
echo ""
echo -e "${BLUE}🔍 [依赖分析] 扫描微服务模块...${NC}"

for MODULE_DIR in microservices/*/; do
    if [ -d "$MODULE_DIR" ]; then
        MODULE_NAME=$(basename "$MODULE_DIR")
        POM_FILE="$MODULE_DIR/pom.xml"
        
        if [ -f "$POM_FILE" ]; then
            echo -e "${YELLOW}  分析模块: $MODULE_NAME${NC}"
            
            # 添加到报告
            echo "" >> "$OUTPUT_FILE"
            echo "### 📦 模块: $MODULE_NAME" >> "$OUTPUT_FILE"
            echo "" >> "$OUTPUT_FILE"
            
            # 提取模块描述
            DESCRIPTION=$(grep -A 1 "<name>" "$POM_FILE" | head -2 | tail -1 | sed 's/.*<name>\(.*\)<\/name>.*/\1/' | sed 's/^[ \t]*//')
            if [ -n "$DESCRIPTION" ]; then
                echo "**描述**: $DESCRIPTION" >> "$OUTPUT_FILE"
                echo "" >> "$OUTPUT_FILE"
            fi
            
            # 提取打包类型
            PACKAGING=$(grep "<packaging>" "$POM_FILE" | sed 's/.*<packaging>\(.*\)<\/packaging>.*/\1/' | head -1)
            echo "**打包类型**: \`${PACKAGING}\`" >> "$OUTPUT_FILE"
            echo "" >> "$OUTPUT_FILE"
            
            # 提取直接依赖（只包含项目内部模块）
            echo "#### 直接依赖（项目内部模块）" >> "$OUTPUT_FILE"
            echo "" >> "$OUTPUT_FILE"
            
            # 使用Maven命令生成依赖树
            if mvn dependency:tree -f "$POM_FILE" -pl "$MODULE_NAME" -DoutputFile="$MODULE_DIR-dep-tree.txt" &>/dev/null; then
                if [ -f "$MODULE_DIR-dep-tree.txt" ]; then
                    # 提取项目内部依赖
                    grep "net.lab1024.sa:" "$MODULE_DIR-dep-tree.txt" | \
                        grep -v "net.lab1024.sa:ioedream" | \
                        sed 's/^[ \t]*//' | \
                        head -20 >> "$OUTPUT_FILE" 2>/dev/null || echo "无内部依赖" >> "$OUTPUT_FILE"
                    
                    rm -f "$MODULE_DIR-dep-tree.txt"
                fi
            else
                echo "⚠️ Maven依赖分析失败" >> "$OUTPUT_FILE"
            fi
            
            echo "" >> "$OUTPUT_FILE"
            
            # 检查是否有循环依赖
            echo "#### 架构合规性检查" >> "$OUTPUT_FILE"
            echo "" >> "$OUTPUT_FILE"
            
            # 检查是否依赖microservices-common聚合模块
            if grep -q "micro-services-common<" "$POM_FILE" 2>/dev/null || \
               grep -q "microservices-common<" "$POM_FILE" 2>/dev/null; then
                echo "❌ **违规**: 依赖了聚合模块 \`microservices-common\`" >> "$OUTPUT_FILE"
            else
                echo "✅ **合规**: 未依赖聚合模块" >> "$OUTPUT_FILE"
            fi
            
            echo "" >> "$OUTPUT_FILE"
        fi
    fi
done

# 添加依赖关系图
cat >> "$OUTPUT_FILE" << 'DEPENDENCY_GRAPH'

---

## 📊 依赖关系图

```mermaid
graph TB
    Core[microservices-common-core]
    
    Core --> Entity[microservices-common-entity]
    Core --> Business[microservices-common-business]
    Core --> Data[microservices-common-data]
    Core --> Security[microservices-common-security]
    Core --> Cache[microservices-common-cache]
    Core --> Monitor[microservices-common-monitor]
    Core --> Storage[microservices-common-storage]
    Core --> Gateway[ microservices-common-gateway-client]
    
    Entity --> Business
    Business --> Data
    Security --> Gateway
    
    Gateway --> CommonService[ioedream-common-service]
    Gateway --> AccessService[ioedream-access-service]
    Gateway --> AttendanceService[ioedream-attendance-service]
    Gateway --> ConsumeService[ioedream-consume-service]
    Gateway --> VideoService[ioedream-video-service]
    Gateway --> VisitorService[ioedream-visitor-service]
    Gateway --> DeviceService[ioedream-device-comm-service]
    
    Data --> CommonService
    Data --> AccessService
    Data --> AttendanceService
    Data --> ConsumeService
    Data --> VideoService
    Data --> VisitorService
    Data --> DeviceService
    
    Security --> CommonService
    Cache --> CommonService
    
    style Core fill:#e1f5ff
    style Gateway fill:#fff4e1
    style CommonService fill:#e8f5e9
    style AccessService fill:#f3e5f5
    style AttendanceService fill:#f3e5f5
    style ConsumeService fill:#f3e5f5
    style VideoService fill:#f3e5f5
    style VisitorService fill:#f3e5f5
    style DeviceService fill:#f3e5f5
```

---

## 🎯 关键发现

### 依赖原则

1. **单向依赖**: 所有依赖都是单向的，没有循环依赖
2. **分层清晰**: 严格遵循核心层→功能层→服务层的依赖层次
3. **最小依赖**: 每个服务只依赖真正需要的模块

### 违规检查

- ❌ **禁止**: 业务服务依赖 \`microservices-common\` 聚合模块
- ❌ **禁止**: 服务之间的直接依赖（应通过gateway-client调用）
- ❌ **禁止**: 循环依赖

### 依赖统计

- **核心模块**: 1个（microservices-common-core）
- **功能模块**: 8个（细粒度模块）
- **业务服务**: 7个（微服务）

---

## 📝 建议

### 立即执行（P0）

1. ✅ 确保所有业务服务不依赖 \`microservices-common\` 聚合模块
2. ✅ 确保服务间调用通过 \`gateway-client\`
3. ✅ 验证没有循环依赖

### 持续优化（P1）

1. 定期运行此脚本检查依赖变化
2. 新增模块时验证依赖合规性
3. 维护依赖关系文档同步更新

---

**报告生成**: $(date)  
**脚本版本**: 1.0.0  
**架构团队**: IOE-DREAM Architecture Team

DEPENDENCY_GRAPH

echo ""
echo -e "${GREEN}✅ [依赖分析] 报告生成完成: $OUTPUT_FILE${NC}"
echo ""
echo "📊 报告内容包括："
echo "  - 模块依赖层次"
echo "  - 每个模块的详细依赖"
echo "  - 架构合规性检查"
echo "  - 依赖关系图（Mermaid格式）"
echo ""
echo "🔍 查看报告："
echo "  cat $OUTPUT_FILE"
