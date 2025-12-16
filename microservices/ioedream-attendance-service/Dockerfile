# 多阶段构建 - 构建阶段
FROM openjdk:17-jdk-slim as builder

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    git \
    maven \
    && rm -rf /var/lib/apt/lists/*

# 复制项目文件
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=docker"

# 生产阶段
FROM openjdk:17-jre-slim

# 维护者信息
LABEL maintainer="IOE-DREAM Team <dev@ioedream.com>"
LABEL description="IOE-DREAM Attendance Management Service"
LABEL version="1.0.0"

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 设置工作目录
WORKDIR /app

# 安装必要的运行时依赖
RUN apt-get update && apt-get install -y \
    curl \
    netcat \
    tzdata \
    && rm -rf /var/lib/apt/lists/* \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# 从构建阶段复制应用文件
COPY --from=builder /app/target/ioedream-attendance-service-*.jar ./app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 复制启动脚本
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh && \
    chown appuser:appuser /usr/local/bin/docker-entrypoint.sh

# 切换到应用用户
USER appuser

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8091/actuator/health || exit 1

# 暴露端口
EXPOSE 8091

# JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
           -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs \
           -Djava.security.egd=file:/dev/./urandom \
           -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# Spring Boot参数
ENV SPRING_OPTS="--spring.config.location=classpath:/application.yml,/app/config/ \
                --spring.profiles.active=docker \
                --management.endpoints.web.exposure.include=health,info,metrics,prometheus \
                --management.endpoint.health.show-details=always"

# 启动命令
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["java", "$JAVA_OPTS", "-jar", "app.jar", "$SPRING_OPTS"]