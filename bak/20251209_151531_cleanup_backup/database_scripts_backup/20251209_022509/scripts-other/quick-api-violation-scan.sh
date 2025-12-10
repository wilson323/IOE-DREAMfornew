#!/bin/bash

# RESTful API违规快速扫描脚本
# 检测POST滥用和URL设计问题

set -e

echo "开始RESTful API违规扫描..."

# 统计Controller文件数量
CONTROLLER_COUNT=$(find . -name "*Controller.java" -path "*/controller/*" | wc -l)
echo "发现 $CONTROLLER_COUNT 个Controller文件"

# 统计POST接口数量
POST_API_COUNT=$(find . -name "*Controller.java" -path "*/controller/*" -exec grep -l "@PostMapping" {} \; | wc -l)
echo "发现 $POST_API_COUNT 个文件包含POST接口"

# 统计其他HTTP方法
GET_API_COUNT=$(find . -name "*Controller.java" -path "*/controller/*" -exec grep -l "@GetMapping" {} \; | wc -l)
PUT_API_COUNT=$(find . -name "*Controller.java" -path "*/controller/*" -exec grep -l "@PutMapping" {} \; | wc -l)
DELETE_API_COUNT=$(find . -name "*Controller.java" -path "*/controller/*" -exec grep -l "@DeleteMapping" {} \; | wc -l)
PATCH_API_COUNT=$(find . -name "*Controller.java" -path "*/controller/*" -exec grep -l "@PatchMapping" {} \; | wc -l)

echo "HTTP方法分布:"
echo "  GET接口: $GET_API_COUNT"
echo "  POST接口: $POST_API_COUNT"
echo "  PUT接口: $PUT_API_COUNT"
echo "  DELETE接口: $DELETE_API_COUNT"
echo "  PATCH接口: $PATCH_API_COUNT"

# 创建分析报告
REPORT_FILE="RESTFUL_API_QUICK_ANALYSIS.md"
cat > "$REPORT_FILE" << EOF
# RESTful API快速分析报告

**分析日期**: $(date '+%Y-%m-%d %H:%M:%S')
**分析范围**: IOE-DREAM项目所有Controller API接口
**任务状态**: 完成

---

## 📊 基础统计

| 统计项目 | 数量 |
|---------|------|
| **Controller文件总数** | $CONTROLLER_COUNT |
| **GET接口文件** | $GET_API_COUNT |
| **POST接口文件** | $POST_API_COUNT |
| **PUT接口文件** | $PUT_API_COUNT |
| **DELETE接口文件** | $DELETE_API_COUNT |
| **PATCH接口文件** | $PATCH_API_COUNT |

## 🚨 潜在问题分析

EOF

# 计算POST接口占比
if [ $POST_API_COUNT -gt 0 ]; then
    POST_RATIO=$(echo "scale=1; $POST_API_COUNT * 100 / $CONTROLLER_COUNT" | bc -l 2>/dev/null || echo "计算中")
    echo "POST接口占比: ${POST_RATIO}%" | tee -a "$REPORT_FILE"
else
    echo "POST接口占比: 0%" | tee -a "$REPORT_FILE"
fi

# 检查典型违规模式
echo ""
echo "🔍 检查典型违规模式..."

VIOLATION_COUNT=0

# 查找查询操作使用POST的违规
echo "检查查询操作POST违规..."
QUERY_VIOLATIONS=$(find . -name "*Controller.java" -path "*/controller/*" -exec grep -l "@PostMapping" {} \; | xargs grep -l -i "list\|get\|query\|search\|page" | wc -l)
echo "  - 查询操作使用POST: $QUERY_VIOLATIONS 个文件"
VIOLATION_COUNT=$((VIOLATION_COUNT + QUERY_VIOLATIONS))

# 查找更新操作使用POST的违规
echo "检查更新操作POST违规..."
UPDATE_VIOLATIONS=$(find . -name "*Controller.java" -path "*/controller/*" -exec grep -l "@PostMapping" {} \; | xargs grep -l -i "update\|edit\|modify" | wc -l)
echo "  - 更新操作使用POST: $UPDATE_VIOLATIONS 个文件"
VIOLATION_COUNT=$((VIOLATION_COUNT + UPDATE_VIOLATIONS))

# 查找删除操作使用POST的违规
echo "检查删除操作POST违规..."
DELETE_VIOLATIONS=$(find . -name "*Controller.java" -path "*/controller/*" -exec grep -l "@PostMapping" {} \; | xargs grep -l -i "delete\|remove" | wc -l)
echo "  - 删除操作使用POST: $DELETE_VIOLATIONS 个文件"
VIOLATION_COUNT=$((VIOLATION_COUNT + DELETE_VIOLATIONS))

# 更新报告
cat >> "$REPORT_FILE" << EOF

### 常见违规统计

| 违规类型 | 违规文件数 | 风险等级 |
|---------|-----------|---------|
| **查询使用POST** | $QUERY_VIOLATIONS | 🔴 高风险 |
| **更新使用POST** | $UPDATE_VIOLATIONS | 🔴 高风险 |
| **删除使用POST** | $DELETE_VIOLATIONS | 🔴 高风险 |
| **总违规数** | $VIOLATION_COUNT | 🔴 高风险 |

## 🔧 修复建议

### 立即修复（高优先级）

1. **查询接口重构**
   - 将查询类POST接口改为GET
   - 使用查询参数替代请求体
   - 保持幂等性

2. **更新接口重构**
   - 将更新操作改为PUT（完整更新）
   - 将状态更新改为PATCH（部分更新）
   - 使用路径参数标识资源

3. **删除接口重构**
   - 将删除操作改为DELETE
   - 使用资源路径：DELETE /api/v1/users/{id}

### URL设计优化

1. **使用复数名词**
   - /user → /users
   - /item → /items

2. **移除URL中的动词**
   - /getUser → GET /users/{id}
   - /deleteUser → DELETE /users/{id}

3. **资源导向设计**
   - 操作意图由HTTP方法表达
   - URL只表示资源

## 📋 修复检查清单

- [ ] 查询接口改为GET方法
- [ ] 更新接口改为PUT/PATCH方法
- [ ] 删除接口改为DELETE方法
- [ ] URL使用复数名词
- [ ] 移除URL中的动词
- [ ] 验证接口幂等性
- [ ] 更新API文档

---

**分析完成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**分析执行人**: RESTful API扫描工具
**下一步**: 基于分析结果制定具体重构计划

EOF

echo ""
echo "✅ 分析完成"
echo "📊 总违规数: $VIOLATION_COUNT"
echo "📄 详细报告: $REPORT_FILE"

if [ $VIOLATION_COUNT -gt 0 ]; then
    echo ""
    echo "🚨 发现 $VIOLATION_COUNT 个潜在的RESTful违规，建议进行重构"
else
    echo ""
    echo "✅ API设计基本符合RESTful规范"
fi

# 生成需要重构的文件列表
if [ $VIOLATION_COUNT -gt 0 ]; then
    echo ""
    echo "📋 生成违规文件列表..."
    find . -name "*Controller.java" -path "*/controller/*" -exec grep -l "@PostMapping" {} \; | xargs grep -l -i "list\|get\|query\|search\|page\|update\|edit\|modify\|delete\|remove" > VIOlation_FILES.txt
    echo "违规文件列表已保存到: VIOlation_FILES.txt"
    echo "违规文件数: $(wc -l < VIOlation_FILES.txt)"
fi