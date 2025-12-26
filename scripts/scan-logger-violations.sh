#!/bin/bash

# LoggerFactory 违规扫描脚本
# 扫描所有微服务中的 LoggerFactory 使用情况

echo "🔍 开始扫描 LoggerFactory 违规情况..."
echo "======================================"

# 总计统计
total_files=0
total_violations=0

# 扫描各个微服务
services=(
    "ioedream-access-service"
    "ioedream-attendance-service"
    "ioedream-oa-service"
    "ioedream-video-service"
    "ioedream-visitor-service"
    "ioedream-device-comm-service"
    "ioedream-biometric-service"
)

for service in "${services[@]}"; do
    service_path="microservices/$service"
    if [ -d "$service_path" ]; then
        echo "📁 扫描服务: $service"

        # 查找 Java 文件中的 LoggerFactory
        violations=$(find "$service_path" -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null | wc -l)
        files=$(find "$service_path" -name "*.java" -type f 2>/dev/null | wc -l)

        echo "   📊 Java 文件总数: $files"
        echo "   ⚠️  LoggerFactory 违规: $violations"

        if [ $violations -gt 0 ]; then
            echo "   📋 违规文件列表:"
            find "$service_path" -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null | head -5 | while read file; do
                echo "      - $file"
            done
            if [ $violations -gt 5 ]; then
                echo "      ... 还有 $((violations - 5)) 个文件"
            fi
        fi

        total_files=$((total_files + files))
        total_violations=$((total_violations + violations))
        echo ""
    fi
done

echo "======================================"
echo "📈 扫描结果汇总:"
echo "   📁 总 Java 文件数: $total_files"
echo "   ⚠️  总 LoggerFactory 违规: $total_violations"
echo "   📊 违规率: $(echo "scale=2; $total_violations * 100 / $total_files" | bc -l)%"
echo ""

if [ $total_violations -gt 0 ]; then
    echo "🚨 发现 $total_violations 个 LoggerFactory 违规，需要修复！"
    exit 1
else
    echo "✅ 所有服务都符合 SLF4J 规范！"
    exit 0
fi