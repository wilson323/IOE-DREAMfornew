# IOE-DREAM Device Service Docker镜像
# 基于现有项目Docker配置重构
#
# @author IOE-DREAM Team
# @since 2025-11-27

# 使用多阶段构建
FROM maven:3.9-openjdk-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制POM文件
COPY pom.xml .

# 下载依赖（利用Docker缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests -B

# 运行时镜像
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    mosquitto-clients \
    && rm -rf /var/lib/apt/lists/*

# 从builder阶段复制JAR文件
COPY --from=builder /app/target/*.jar app.jar

# 设置日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 切换到应用用户
USER appuser

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=docker"
ENV TZ="Asia/Shanghai"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/api/device/health || exit 1

# 暴露端口
EXPOSE 8082
EXPOSE 1883  # MQTT端口

# 设置时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# 标签信息
LABEL maintainer="IOE-DREAM Team" \
      version="1.0.0" \
      description="IOE-DREAM Device Service Microservice" \
      service.name="ioedream-device-service" \
      service.version="1.0.0"