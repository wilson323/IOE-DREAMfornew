# SmartAdmin 后端 Dockerfile
# 多阶段构建：构建阶段 + 运行阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制pom文件
COPY pom.xml .
COPY sa-support/pom.xml ./sa-support/
COPY sa-base/pom.xml ./sa-base/
COPY sa-admin/pom.xml ./sa-admin/

# 下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY sa-support/src ./sa-support/src
COPY sa-base/src ./sa-base/src
COPY sa-admin/src ./sa-admin/src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

# 安装必要的工具
RUN apk add --no-cache curl

# 创建应用用户（安全考虑）
RUN addgroup -g 1000 appuser && adduser -D -s /bin/sh -u 1000 -G appuser appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=builder /app/sa-admin/target/sa-admin-*.jar app.jar

# 创建完整的日志目录结构并设置权限
RUN mkdir -p /home/logs/smart_admin_v3/sa-admin/docker/tomcat-logs \
    && mkdir -p /app/logs \
    && chown -R appuser:appuser /app \
    && chown -R appuser:appuser /home

# 切换到应用用户
USER appuser

# 暴露端口
EXPOSE 1024

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:1024/api/health || exit 1

# 设置JVM参数 - 强制使用tmp目录作为日志目录
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport" \
    CATALINA_OUT="/tmp/catalina.out" \
    CATALINA_TMPDIR="/tmp"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]