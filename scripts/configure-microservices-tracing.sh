#!/bin/bash

# 微服务分布式追踪批量配置脚本
# 为所有微服务添加Spring Cloud Sleuth追踪配置

set -e

echo "🔧 批量配置微服务分布式追踪..."

# 获取所有微服务目录
MICROSERVICES_DIR="microservices"
CONFIG_COUNT=0

# 创建需要配置的服务列表
find "$MICROSERVICES_DIR" -maxdepth 1 -type d -name "ioedream-*" | sort > all_services.txt

echo "📊 发现 $(wc -l < all_services.txt) 个微服务需要配置"

while IFS= read -r service_dir; do
    if [ -d "$service_dir" ]; then
        service_name=$(basename "$service_dir")
        pom_file="$service_dir/pom.xml"

        echo ""
        echo "🔧 配置服务: $service_name"

        if [ -f "$pom_file" ]; then
            # 备份原pom.xml
            cp "$pom_file" "$pom_file.backup"
            echo "  ✅ 备份: $pom_file.backup"

            # 检查是否已配置追踪依赖
            if ! grep -q "spring-cloud-starter-sleuth" "$pom_file"; then
                # 查找dependencies标签位置并添加依赖
                sed -i '/<dependencies>/a\
        <!-- Spring Cloud Sleuth 分布式追踪 -->\
        <dependency>\
            <groupId>org.springframework.cloud</groupId>\
            <artifactId>spring-cloud-starter-sleuth</artifactId>\
        </dependency>\
        <!-- Zipkin Reporting -->\
        <dependency>\
            <groupId>org.springframework.cloud</groupId>\
            <artifactId>spring-cloud-sleuth-zipkin</artifactId>\
        </dependency>\
        <!-- Micrometer Tracing -->\
        <dependency>\
            <groupId>io.micrometer</groupId>\
            <artifactId>micrometer-tracing-bridge-brave</artifactId>\
        </dependency>\
        <!-- Zipkin Reporter -->\
        <dependency>\
            <groupId>io.zipkin.reporter2</groupId>\
            <artifactId>zipkin-reporter-brave</artifactId>\
        </dependency>' "$pom_file"

                echo "  ✅ 添加分布式追踪依赖"
                CONFIG_COUNT=$((CONFIG_COUNT + 1))
            else
                echo "  ⚠️  追踪依赖已存在"
            fi

            # 配置bootstrap.yml或application.yml
            bootstrap_file="$service_dir/src/main/resources/bootstrap.yml"
            app_file="$service_dir/src/main/resources/application.yml"

            if [ -f "$bootstrap_file" ]; then
                config_file="$bootstrap_file"
                echo "  📄 使用 bootstrap.yml 配置文件"
            elif [ -f "$app_file" ]; then
                config_file="$app_file"
                echo "  📄 使用 application.yml 配置文件"
            else
                # 创建application.yml文件
                mkdir -p "$service_dir/src/main/resources"
                config_file="$app_file"
                cat > "$config_file" << 'EOF'
spring:
  application:
    name: ${SERVICE_NAME:your-service-name}
EOF
                echo "  📄 创建 application.yml 配置文件"
            fi

            # 备份配置文件
            cp "$config_file" "$config_file.backup"

            # 添加分布式追踪配置
            if ! grep -q "spring.sleuth" "$config_file"; then
                cat >> "$config_file" << 'EOF'

# 分布式追踪配置
spring:
  sleuth:
    zipkin:
      base-url: ${ZIPKIN_BASE_URL:http://localhost:9411}
      enabled: true
      service:
        name: ${spring.application.name}
      sender:
        type: web
      message-timeout: 5s
      compression:
        enabled: true
    sampler:
      probability: ${TRACING_SAMPLE_RATE:0.1}  # 10%采样率
    propagation:
      type: w3c  # 使用W3C Trace Context标准
    ignored-patterns:
      - /actuator/.*
      - /health
      - /info
      - /metrics

  # 集成Micrometer
  zipkin:
    locator:
      discovery:
        enabled: true

# 管理端点配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,tracing
  endpoint:
    tracing:
      enabled: true
  tracing:
    sampling:
      probability: ${TRACING_SAMPLE_RATE:0.1}

# 日志配置（集成Trace ID）
logging:
  pattern:
    # 控制台日志格式包含Trace ID
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n"
    # 文件日志格式包含Trace ID
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n"
EOF

                echo "  ✅ 添加分布式追踪配置"
            else
                echo "  ⚠️  追踪配置已存在"
            fi

        else
            echo "  ❌ pom.xml文件不存在"
        fi
    fi
done < all_services.txt

# 清理临时文件
rm -f all_services.txt

echo ""
echo "✅ 批量配置完成"
echo "📊 配置服务数: $CONFIG_COUNT"
echo ""

# 创建验证脚本
cat > scripts/verify-microservices-tracing.sh << 'VERIFY_EOF'
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
VERIFY_EOF

chmod +x scripts/verify-microservices-tracing.sh

# 创建Zipkin部署脚本
cat > scripts/deploy-zipkin.sh << 'ZIPKIN_EOF'
#!/bin/bash

echo "🚀 部署Zipkin分布式追踪服务..."

# 创建zipkin目录
mkdir -p zipkin-deployment
cd zipkin-deployment

# 创建docker-compose配置
cat > docker-compose.yml << 'COMPOSE_EOF'
version: '3.8'
services:
  zipkin:
    image: openzipkin/zipkin:latest
    container_name: ioedream-zipkin
    ports:
      - "9411:9411"
    environment:
      - STORAGE_TYPE=mysql
      - MYSQL_HOST=mysql
      - MYSQL_TCP_PORT=3306
      - MYSQL_DB=zipkin
      - MYSQL_USER=zipkin
      - MYSQL_PASS=zipkin123
      - JAVA_OPTS=-Xms512m -Xmx512m
    depends_on:
      - mysql
    restart: unless-stopped
    networks:
      - zipkin-network

  mysql:
    image: mysql:8.0
    container_name: ioedream-zipkin-mysql
    ports:
      - "3307:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root123456
      - MYSQL_DATABASE=zipkin
      - MYSQL_USER=zipkin
      - MYSQL_PASSWORD=zipkin123
    volumes:
      - zipkin-mysql-data:/var/lib/mysql
    restart: unless-stopped
    networks:
      - zipkin-network

volumes:
  zipkin-mysql-data:

networks:
  zipkin-network:
    driver: bridge
COMPOSE_EOF

echo "📋 Docker Compose配置已创建"

# 启动Zipkin服务
echo "🚀 启动Zipkin服务..."
docker-compose up -d

echo ""
echo "✅ Zipkin服务部署完成"
echo ""
echo "🌐 服务访问地址:"
echo "- Zipkin Web UI: http://localhost:9411"
echo "- MySQL数据库: localhost:3307"
echo ""
echo "🔧 验证服务状态:"
echo "docker-compose ps"
echo ""
echo "📋 查看服务日志:"
echo "docker-compose logs -f zipkin"
echo ""
echo "⚠️  注意事项:"
echo "- 确保端口9411和3307未被占用"
echo "- 生产环境请修改默认密码"
echo "- 建议配置数据持久化"

cd ..
ZIPKIN_EOF

chmod +x scripts/deploy-zipkin.sh

# 创建测试脚本
cat > scripts/test-distributed-tracing.sh << 'TEST_EOF'
#!/bin/bash

echo "🧪 测试分布式追踪功能..."

# 检查Zipkin服务状态
echo "📊 检查Zipkin服务..."
if curl -s http://localhost:9411/health > /dev/null; then
    echo "  ✅ Zipkin服务正常"
else
    echo "  ❌ Zipkin服务异常，请先部署Zipkin服务"
    echo "  运行: bash scripts/deploy-zipkin.sh"
    exit 1
fi

# 模拟API调用测试
echo ""
echo "🧪 模拟微服务调用测试..."
echo "请启动任意一个已配置分布式追踪的微服务，然后访问其API接口"
echo ""
echo "示例测试命令:"
echo "curl -H 'X-B3-TraceId: 1234567890abcdef' http://localhost:8080/actuator/health"
echo ""
echo "📋 验证方法:"
echo "1. 查看微服务日志，确认包含Trace ID"
echo "2. 访问Zipkin Web UI: http://localhost:9411"
echo "3. 查看追踪数据是否正常上报"
echo ""
echo "📊 期望结果:"
echo "- 微服务日志包含: [traceId:1234567890abcdef,spanId:xxxxxxxxxxxxxxxx]"
echo "- Zipkin UI显示服务调用链路"
echo "- 可以查看详细的调用时间线和依赖关系"
TEST_EOF

chmod +x scripts/test-distributed-tracing.sh

echo "🎯 下一步操作:"
echo "1. 部署Zipkin服务: bash scripts/deploy-zipkin.sh"
echo "2. 验证配置效果: bash scripts/verify-microservices-tracing.sh"
echo "3. 测试追踪功能: bash scripts/test-distributed-tracing.sh"
echo ""
echo "📝 重要提醒:"
echo "- 确保Zipkin服务在微服务启动前部署完成"
echo "- 根据网络环境调整Zipkin地址配置"
echo "- 生产环境建议调整采样率和存储配置"
echo ""
echo "✅ 分布式追踪批量配置完成！"