# IOE-DREAM Device Communication Service Dockerfile
# 使用预构建的JAR文件，需要先在本地执行: mvn clean install -DskipTests -Dpmd.skip=true
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

WORKDIR /app

COPY microservices/ioedream-device-comm-service/target/ioedream-device-comm-service-1.0.0.jar app.jar

EXPOSE 8087

HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

# 启动配置 - 设备通讯服务JVM优化
ENV JAVA_OPTS="-server -Xms256m -Xmx512m -XX:MaxDirectMemorySize=256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:MaxGCPauseMillis=150 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump/"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
