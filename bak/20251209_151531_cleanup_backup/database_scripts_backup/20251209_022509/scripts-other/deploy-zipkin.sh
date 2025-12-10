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
