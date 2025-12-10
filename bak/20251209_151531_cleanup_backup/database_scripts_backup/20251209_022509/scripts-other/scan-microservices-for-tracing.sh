#!/bin/bash

# 微服务分布式追踪配置扫描脚本
# 扫描所有微服务的追踪配置状态

set -e

echo "🔍 扫描微服务分布式追踪配置状态..."

MICROSERVICES_DIR="microservices"
REPORT_FILE="MICROSERVICES_TRACING_SCAN_REPORT.md"

# 初始化统计
TOTAL_SERVICES=0
CONFIGURED_SERVICES=0
NEED_CONFIG_SERVICES=0

# 创建报告文件
cat > "$REPORT_FILE" << EOF
# 微服务分布式追踪扫描报告

**扫描日期**: $(date '+%Y-%m-%d %H:%M:%S')
**扫描范围**: IOE-DREAM项目所有微服务
**任务状态**: 🔍 **扫描完成**
**优先级**: 🔴 P0级企业级可观测性要求

---

## 📋 扫描发现

EOF

echo "🔍 扫描微服务目录..."

# 扫描所有微服务
find "$MICROSERVICES_DIR" -maxdepth 1 -type d -name "ioedream-*" | sort | while read service_dir; do
    if [ -d "$service_dir" ]; then
        service_name=$(basename "$service_dir")
        pom_file="$service_dir/pom.xml"
        bootstrap_file="$service_dir/src/main/resources/bootstrap.yml"
        app_file="$service_dir/src/main/resources/application.yml"

        echo ""
        echo "检查服务: $service_name"
        echo "### $service_name" >> "$REPORT_FILE"

        # 检查pom.xml
        has_sleuth=false
        has_zipkin=false
        has_tracing=false

        if [ -f "$pom_file" ]; then
            if grep -q "spring-cloud-starter-sleuth" "$pom_file" 2>/dev/null; then
                has_sleuth=true
            fi
            if grep -q "spring-cloud-sleuth-zipkin" "$pom_file" 2>/dev/null; then
                has_zipkin=true
            fi
            if grep -q "micrometer-tracing" "$pom_file" 2>/dev/null; then
                has_tracing=true
            fi
        fi

        # 检查配置文件
        has_sleuth_config=false
        if [ -f "$bootstrap_file" ]; then
            if grep -q "spring.sleuth" "$bootstrap_file" 2>/dev/null; then
                has_sleuth_config=true
            fi
        elif [ -f "$app_file" ]; then
            if grep -q "spring.sleuth" "$app_file" 2>/dev/null; then
                has_sleuth_config=true
            fi
        fi

        # 生成状态报告
        echo "**配置文件**: pom.xml ✅" >> "$REPORT_FILE"
        if [ -f "$bootstrap_file" ]; then
            echo "**配置文件**: bootstrap.yml ✅" >> "$REPORT_FILE"
        elif [ -f "$app_file" ]; then
            echo "**配置文件**: application.yml ✅" >> "$REPORT_FILE"
        else
            echo "**配置文件**: ❌ 未找到" >> "$REPORT_FILE"
        fi

        echo "" >> "$REPORT_FILE"
        echo "**依赖状态**:" >> "$REPORT_FILE"
        echo "- spring-cloud-starter-sleuth: $([ "$has_sleuth" = true ] && echo "✅ 已配置" || echo "❌ 未配置")" >> "$REPORT_FILE"
        echo "- spring-cloud-sleuth-zipkin: $([ "$has_zipkin" = true ] && echo "✅ 已配置" || echo "❌ 未配置")" >> "$REPORT_FILE"
        echo "- micrometer-tracing: $([ "$has_tracing" = true ] && echo "✅ 已配置" || echo "❌ 未配置")" >> "$REPORT_FILE"

        echo "" >> "$REPORT_FILE"
        echo "**配置状态**:" >> "$REPORT_FILE"
        echo "- spring.sleuth配置: $([ "$has_sleuth_config" = true ] && echo "✅ 已配置" || echo "❌ 未配置")" >> "$REPORT_FILE"

        # 判断整体配置状态
        if [ "$has_sleuth" = true ] && [ "$has_zipkin" = true ] && [ "$has_sleuth_config" = true ]; then
            echo "**整体状态**: ✅ **已配置分布式追踪**" >> "$REPORT_FILE"
            echo "  ✅ 已配置分布式追踪"
            CONFIGURED_SERVICES=$((CONFIGURED_SERVICES + 1))
        else
            echo "**整体状态**: ❌ **需要配置分布式追踪**" >> "$REPORT_FILE"
            echo "  ❌ 需要配置分布式追踪"
            NEED_CONFIG_SERVICES=$((NEED_CONFIG_SERVICES + 1))
            echo "$service_dir" >> services_need_tracing.txt
        fi

        echo "" >> "$REPORT_FILE"
        echo "---" >> "$REPORT_FILE"
        TOTAL_SERVICES=$((TOTAL_SERVICES + 1))
    fi
done

echo ""
echo "📊 扫描结果统计:"
echo "总微服务数: $TOTAL_SERVICES"
echo "已配置追踪: $CONFIGURED_SERVICES"
echo "需要配置: $NEED_CONFIG_SERVICES"

# 更新报告
cat >> "$REPORT_FILE" << EOF

## 📊 扫描统计

| 统计项目 | 数量 | 状态 |
|---------|------|------|
| **总微服务数** | $TOTAL_SERVICES | 📈 |
| **已配置追踪** | $CONFIGURED_SERVICES | $([ $CONFIGURED_SERVICES -gt 0 ] && echo "✅ 已配置" || echo "❌ 未配置") |
| **需要配置** | $NEED_CONFIG_SERVICES | $([ $NEED_CONFIG_SERVICES -gt 0 ] && echo "🔧 需要配置" || echo "✅ 全部配置") |

## 🎯 配置覆盖率

**分布式追踪配置覆盖率**: $(echo "scale=1; $CONFIGURED_SERVICES * 100 / $TOTAL_SERVICES" | bc)%

EOF

if [ $NEED_CONFIG_SERVICES -gt 0 ]; then
    cat >> "$REPORT_FILE" << EOF
## 🔧 需要配置的服务列表

需要配置分布式追踪的 $NEED_CONFIG_SERVICES 个服务：

EOF
    while IFS= read -r service_dir; do
        service_name=$(basename "$service_dir")
        echo "- **$service_name**: \`$service_dir\`" >> "$REPORT_FILE"
    done < services_need_tracing.txt

    cat >> "$REPORT_FILE" << EOF

## 📋 下一步行动

### 🔧 立即执行配置
运行批量配置脚本：
\`\`\`bash
bash scripts/configure-microservices-tracing.sh
\`\`\`

### 🎯 配置内容
1. **添加追踪依赖**: spring-cloud-starter-sleuth等
2. **配置追踪参数**: Zipkin地址、采样率等
3. **更新日志格式**: 包含Trace ID和Span ID
4. **暴露管理端点**: /actuator/tracing

EOF
fi

cat >> "$REPORT_FILE" << EOF
---

**扫描完成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**扫描执行人**: 分布式追踪配置工具
**下一步**: $([ $NEED_CONFIG_SERVICES -gt 0 ] && echo "执行批量配置" || echo "验证配置效果")

EOF

echo ""
echo "✅ 扫描完成"
echo "📊 总微服务数: $TOTAL_SERVICES"
echo "📊 已配置追踪: $CONFIGURED_SERVICES"
echo "📊 需要配置: $NEED_CONFIG_SERVICES"
echo "📄 详细报告: $REPORT_FILE"

if [ $NEED_CONFIG_SERVICES -gt 0 ]; then
    echo ""
    echo "🔧 需要配置 $NEED_CONFIG_SERVICES 个微服务"
    echo "📋 需要配置的服务列表已保存到: services_need_tracing.txt"
    echo ""
    echo "🚀 执行批量配置:"
    echo "bash scripts/configure-microservices-tracing.sh"
else
    echo ""
    echo "🎉 所有微服务已配置分布式追踪！"
fi