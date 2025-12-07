#!/bin/bash

echo "🔍 验证微服务分布式追踪配置..."

MICROSERVICES_DIR="microservices"
TOTAL_SERVICES=0
CONFIGURED_SERVICES=0

find "$MICROSERVICES_DIR" -maxdepth 1 -type d -name "ioedream-*" | sort | while read service_dir; do
    if [ -d "$service_dir" ]; then
        service_name=$(basename "$service_dir")
        pom_file="$service_dir/pom.xml"
        config_file=""

        # 查找配置文件
        if [ -f "$service_dir/src/main/resources/bootstrap.yml" ]; then
            config_file="$service_dir/src/main/resources/bootstrap.yml"
        elif [ -f "$service_dir/src/main/resources/application.yml" ]; then
            config_file="$service_dir/src/main/resources/application.yml"
        fi

        echo "检查服务: $service_name"

        # 检查pom.xml依赖
        has_sleuth=false
        has_zipkin=false
        if [ -f "$pom_file" ]; then
            if grep -q "spring-cloud-starter-sleuth" "$pom_file" 2>/dev/null; then
                has_sleuth=true
            fi
            if grep -q "spring-cloud-sleuth-zipkin" "$pom_file" 2>/dev/null; then
                has_zipkin=true
            fi
        fi

        # 检查配置文件
        has_sleuth_config=false
        if [ -f "$config_file" ]; then
            if grep -q "spring.sleuth" "$config_file" 2>/dev/null; then
                has_sleuth_config=true
            fi
        fi

        # 判断配置状态
        if [ "$has_sleuth" = true ] && [ "$has_zipkin" = true ] && [ "$has_sleuth_config" = true ]; then
            echo "  ✅ 分布式追踪配置完整"
            CONFIGURED_SERVICES=$((CONFIGURED_SERVICES + 1))
        else
            echo "  ❌ 配置不完整:"
            [ "$has_sleuth" = false ] && echo "    - 缺少 spring-cloud-starter-sleuth"
            [ "$has_zipkin" = false ] && echo "    - 缺少 spring-cloud-sleuth-zipkin"
            [ "$has_sleuth_config" = false ] && echo "    - 缺少 spring.sleuth 配置"
        fi

        TOTAL_SERVICES=$((TOTAL_SERVICES + 1))
    fi
done

echo ""
echo "📊 验证结果:"
echo "总微服务数: $TOTAL_SERVICES"
echo "配置完整: $CONFIGURED_SERVICES"
echo "配置覆盖率: $(echo "scale=1; $CONFIGURED_SERVICES * 100 / $TOTAL_SERVICES" | bc -l 2>/dev/null || echo "计算中")%"

if [ $CONFIGURED_SERVICES -eq $TOTAL_SERVICES ]; then
    echo "🎉 所有微服务分布式追踪配置完成！"
    exit 0
else
    echo "❌ 还有微服务需要配置"
    exit 1
fi
