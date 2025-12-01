# 多阶段构建Dockerfile
# 构建消费模块的生产级Docker镜像

# 构建阶段
FROM maven:3.8.6-openjdk-17 AS builder
WORKDIR /app

# 复制pom.xml文件
COPY pom.xml .
COPY sa-admin/pom.xml sa-admin/

# 复制依赖文件
COPY sa-base/pom.xml sa-base/
COPY sa-module/pom.xml sa-module/

# 构建应用
RUN cd sa-admin && \
    mvn clean package -DskipTests -q

# 生产阶段
FROM openjdk:17-jre-slim

# 安装必要的工具
RUN apt-get update && \
    apt-get install -y curl netcat && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制JAR文件
COPY --from=builder /app/sa-admin/target/sa-admin-*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log/consume-module && \
    chown -R appuser:appuser /var/log/consume-module

# 创建健康检查脚本
RUN echo '#!/bin/sh\ncurl -f http://localhost:1024/api/consume/health || exit 1' > /app/healthcheck.sh && \
    chmod +x /app/healthcheck.sh && \
    chown appuser:appuser /app/healthcheck.sh

# 创建就绪检查脚本
RUN echo '#!/bin/sh\ncurl -f http://localhost:1024/api/consume/ready || exit 1' > /app/readycheck.sh && \
    chmod +x /app/readycheck.sh && \
    chown appuser:appuser /app/readycheck.sh

# 切换到应用用户
USER appuser

# 暴露端口
EXPOSE 1024

# 设置JVM参数
ENV JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/consume-module -Dspring.profiles.active=prod"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 --executable=/app/healthcheck.sh

# 就绪检查
READYCHECK --interval=10s --timeout=5s --start-period=30s --retries=3 --executable=/app/readycheck.sh

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./.java.policy -Dspring.profiles.active=prod -jar app.jar"]