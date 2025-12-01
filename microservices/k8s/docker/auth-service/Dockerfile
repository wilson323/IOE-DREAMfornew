# 多阶段构建 - 认证服务
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 复制父级POM文件
COPY pom.xml .
COPY ../pom.xml .
COPY ../smart-common/pom.xml ../smart-common/
COPY ../microservices-common/pom.xml ../microservices-common/

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:17-jdk-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    jq \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# 复制jar包
COPY --from=builder /app/target/ioedream-auth-service-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

USER appuser

# JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]