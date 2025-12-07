#!/bin/bash

# RESTful API设计违规分析脚本
# 分析项目中的API设计是否符合RESTful规范

set -e

echo "🔍 分析RESTful API设计违规..."

CONTROLLER_FILES=$(find . -name "*.java" -path "*/controller/*" | wc -l)
TOTAL_APIS=0
POST_APIS=0
GET_APIS=0
PUT_APIS=0
DELETE_APIS=0
PATCH_APIS=0
VIOLATIONS=0

REPORT_FILE="RESTFUL_API_ANALYSIS_REPORT.md"

# 创建报告文件
cat > "$REPORT_FILE" << EOF
# RESTful API设计分析报告

**分析日期**: $(date '+%Y-%m-%d %H:%M:%S')
**分析范围**: IOE-DREAM项目所有Controller API接口
**任务状态**: 🔍 **分析完成**
**优先级**: 🔴 P0级API设计规范

---

## 📋 分析概览

EOF

echo "📊 Controller文件总数: $CONTROLLER_FILES"

# 分析每个Controller文件
find . -name "*.java" -path "*/controller/*" | sort | while read controller_file; do
    if [ -f "$controller_file" ]; then
        service_name=$(echo "$controller_file" | sed 's|.*/\([^/]*-service\).*|\1|')
        controller_name=$(basename "$controller_file" .java)

        echo ""
        echo "分析: $service_name/$controller_name"
        echo "### $controller_name ($service_name)" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        echo "**文件路径**: \`$controller_file\`" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"

        # 分析API接口
        api_count=0
        post_count=0
        get_count=0
        put_count=0
        delete_count=0
        patch_count=0
        violations_count=0

        echo "  📋 API接口分析:" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        echo "| HTTP方法 | 路径 | 是否符合RESTful | 建议 |" >> "$REPORT_FILE"
        echo "|---------|------|----------------|------|" >> "$REPORT_FILE"

        # 提取API接口信息
        grep -n "@\(Get\|Post\|Put\|Delete\|Patch\|GetMapping\|PostMapping\|PutMapping\|DeleteMapping\|PatchMapping\)" "$controller_file" | while read line; do
            api_count=$((api_count + 1))
            TOTAL_APIS=$((TOTAL_APIS + 1))

            # 解析HTTP方法和路径
            if echo "$line" | grep -q "@\(Get\|GetMapping\)"; then
                http_method="GET"
                get_count=$((get_count + 1))
                GET_APIS=$((GET_APIS + 1))
                compliance="✅ 符合"
                suggestion="查询操作，RESTful规范"
            elif echo "$line" | grep -q "@\(Post\|PostMapping\)"; then
                http_method="POST"
                post_count=$((post_count + 1))
                POST_APIS=$((POST_APIS + 1))

                # 判断POST使用是否合理
                if echo "$line" | grep -q -E "login|logout|upload|batch|execute|process|search"; then
                    compliance="✅ 符合"
                    suggestion="合适的POST使用场景"
                else
                    compliance="❌ 可能违规"
                    suggestion="考虑使用GET/PUT/DELETE替代"
                    violations_count=$((violations_count + 1))
                    VIOLATIONS=$((VIOLATIONS + 1))
                fi
            elif echo "$line" | grep -q "@\(Put\|PutMapping\)"; then
                http_method="PUT"
                put_count=$((put_count + 1))
                PUT_APIS=$((PUT_APIS + 1))
                compliance="✅ 符合"
                suggestion="更新操作，RESTful规范"
            elif echo "$line" | grep -q "@\(Delete\|DeleteMapping\)"; then
                http_method="DELETE"
                delete_count=$((delete_count + 1))
                DELETE_APIS=$((DELETE_APIS + 1))
                compliance="✅ 符合"
                suggestion="删除操作，RESTful规范"
            elif echo "$line" | grep -q "@\(Patch\|PatchMapping\)"; then
                http_method="PATCH"
                patch_count=$((patch_count + 1))
                PATCH_APIS=$((PATCH_APIS + 1))
                compliance="✅ 符合"
                suggestion="部分更新，RESTful规范"
            else
                http_method="UNKNOWN"
                compliance="❓ 未知"
                suggestion="需要手动检查"
            fi

            # 提取路径信息
            path=$(echo "$line" | sed -n 's/.*value\s*=\s*["\x27]([^"\x27]*)["\x27].*/\1/p')
            if [ -z "$path" ]; then
                path=$(echo "$line" | sed -n 's/.*["\x27]\([^"\x27]*)["\x27].*/\1/p')
            fi
            if [ -z "$path" ]; then
                path="未提取"
            fi

            echo "| $http_method | $path | $compliance | $suggestion |" >> "$REPORT_FILE"
            echo "    - $http_method $path: $compliance"
        done

        echo "" >> "$REPORT_FILE"
        echo "**接口统计**:" >> "$REPORT_FILE"
        echo "- 总接口数: $api_count" >> "$REPORT_FILE"
        echo "- GET接口: $get_count" >> "$REPORT_FILE"
        echo "- POST接口: $post_count" >> "$REPORT_FILE"
        echo "- PUT接口: $put_count" >> "$REPORT_FILE"
        echo "- DELETE接口: $delete_count" >> "$REPORT_FILE"
        echo "- PATCH接口: $patch_count" >> "$REPORT_FILE"
        if [ $violations_count -gt 0 ]; then
            echo "- 🚨 潜在违规: $violations_count" >> "$REPORT_FILE"
        fi

        echo "" >> "$REPORT_FILE"
        echo "---" >> "$REPORT_FILE"

        echo "  📊 统计: 总数=$api_count, GET=$get_count, POST=$post_count, PUT=$put_count, DELETE=$delete_count, PATCH=$patch_count"
        if [ $violations_count -gt 0 ]; then
            echo "  ⚠️  发现 $violations_count 个潜在违规"
        fi
    fi
done

echo ""
echo "📊 全局统计:"
echo "总Controller文件: $CONTROLLER_FILES"
echo "总API接口数: $TOTAL_APIS"
echo "GET接口: $GET_APIS"
echo "POST接口: $POST_APIS"
echo "PUT接口: $PUT_APIS"
echo "DELETE接口: $DELETE_APIS"
echo "PATCH接口: $PATCH_APIS"
echo "潜在违规: $VIOLATIONS"

# 计算违规比例
if [ $POST_APIS -gt 0 ]; then
    violation_rate=$(echo "scale=1; $VIOLATIONS * 100 / $POST_APIS" | bc -l 2>/dev/null || echo "计算中")
else
    violation_rate="0.0"
fi

# 更新报告
cat >> "$REPORT_FILE" << EOF

## 📊 全局统计汇总

| 统计项目 | 数量 | 占比 |
|---------|------|------|
| **总Controller文件** | $CONTROLLER_FILES | 100% |
| **总API接口数** | $TOTAL_APIS | 100% |
| **GET接口** | $GET_APIS | $(echo "scale=1; $GET_APIS * 100 / $TOTAL_APIS" | bc -l 2>/dev/null || echo "计算中")% |
| **POST接口** | $POST_APIS | $(echo "scale=1; $POST_APIS * 100 / $TOTAL_APIS" | bc -l 2>/dev/null || echo "计算中")% |
| **PUT接口** | $PUT_APIS | $(echo "scale=1; $PUT_APIS * 100 / $TOTAL_APIS" | bc -l 2>/dev/null || echo "计算中")% |
| **DELETE接口** | $DELETE_APIS | $(echo "scale=1; $DELETE_APIS * 100 / $TOTAL_APIS" | bc -l 2>/dev/null || echo "计算中")% |
| **PATCH接口** | $PATCH_APIS | $(echo "scale=1; $PATCH_APIS * 100 / $TOTAL_APIS" | bc -l 2>/dev/null || echo "计算中")% |
| **潜在违规** | $VIOLATIONS | $(echo "scale=1; $VIOLATIONS * 100 / $TOTAL_APIS" | bc -l 2>/dev/null || echo "计算中")% |

## 🚨 RESTful规范违规分析

### 当前问题
EOF

if [ $VIOLATIONS -gt 0 ]; then
    cat >> "$REPORT_FILE" << EOF
1. **POST滥用**: 发现 $VIOLATIONS 个可能滥用的POST接口
2. **HTTP方法误用**: 部分接口使用了不合适的HTTP方法
3. **语义不清晰**: 接口语义与HTTP方法不匹配
4. **查询操作POST化**: 本应使用GET的查询使用了POST

### 违规比例
- **POST接口违规率**: ${violation_rate}%
- **整体违规率**: $(echo "scale=1; $VIOLATIONS * 100 / $TOTAL_APIS" | bc -l 2>/dev/null || echo "计算中")%

EOF
else
    cat >> "$REPORT_FILE" << EOF
✅ **未发现明显违规**: API设计基本符合RESTful规范

EOF
fi

cat >> "$REPORT_FILE" << EOF
## 📋 RESTful设计规范

### HTTP方法语义
- **GET**: 查询资源，幂等，安全
- **POST**: 创建资源，非幂等，非安全
- **PUT**: 完整更新资源，幂等，非安全
- **PATCH**: 部分更新资源，非幂等，非安全
- **DELETE**: 删除资源，幂等，非安全

### 接口设计原则
1. **资源导向**: URL应该表示资源而不是操作
2. **HTTP语义**: 使用正确的HTTP方法表达意图
3. **状态码**: 返回合适的HTTP状态码
4. **幂等性**: 确保操作的幂等性

### 常见违规模式
1. **查询使用POST**: 搜索、列表查询使用POST
2. **更新使用POST**: 更新操作应该用PUT/PATCH
3. **删除使用POST**: 删除操作应该用DELETE
4. **操作URL**: URL中包含动词

## 🛠️ 修复建议

### 立即修复（高优先级）
EOF

if [ $VIOLATIONS -gt 0 ]; then
    cat >> "$REPORT_FILE" << EOF
1. **重构查询接口**: 将查询类POST接口改为GET
2. **修复更新接口**: 将更新操作POST接口改为PUT/PATCH
3. **修正删除接口**: 将删除操作POST接口改为DELETE
4. **优化URL设计**: 移除URL中的动词，使用名词

EOF
else
    cat >> "$REPORT_FILE" << EOF
1. **保持当前标准**: 继续遵循RESTful设计规范
2. **代码审查**: 建立RESTful设计审查机制
3. **团队培训**: 加强RESTful设计理念培训

EOF
fi

cat >> "$REPORT_FILE" << EOF
### 优化建议（中优先级）
1. **统一响应格式**: 建立统一的API响应格式
2. **错误处理**: 规范化错误码和错误信息
3. **API文档**: 完善Swagger/Knife4j文档
4. **版本管理**: 实现API版本管理机制

---

**分析完成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**分析执行人**: RESTful API分析工具
**下一步**: 基于分析结果制定重构计划

EOF

echo ""
echo "✅ RESTful API分析完成"
echo "📊 详细报告: $REPORT_FILE"
echo "📊 统计结果:"
echo "  - 总API接口: $TOTAL_APIS"
echo "  - POST接口: $POST_APIS ($(echo "scale=1; $POST_APIS * 100 / $TOTAL_APIS" | bc -l 2>/dev/null || echo "计算中")%)"
echo "  - 潜在违规: $VIOLATIONS"

if [ $VIOLATIONS -gt 0 ]; then
    echo ""
    echo "🚨 发现 $VIOLATIONS 个潜在违规，建议进行RESTful重构"
    echo "📋 违规率: ${violation_rate}%"
else
    echo ""
    echo "✅ API设计基本符合RESTful规范"
fi