# IOE-DREAM Gateway Service Dockerfile
# 使用预构建的JAR文件，需要先在本地执行: mvn clean install -DskipTests -Dpmd.skip=true
FROM eclipse-temurin:17-jre-alpine

# 安装必要工具
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建应用目录
WORKDIR /app

# 复制预构建的JAR
COPY microservices/ioedream-gateway-service/target/ioedream-gateway-service-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动配置 - 网关服务JVM优化
ENV JAVA_OPTS="-server -Xms1g -Xmx2g -Xmn512m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:G1HeapRegionSize=16m -XX:InitiatingHeapOccupancyPercent=45 -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump/ -XX:+PrintGCDetails -Xloggc:/app/logs/gc/gc.log"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
