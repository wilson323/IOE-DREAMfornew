# IOE-DREAM Access Control Service Dockerfile
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# 配置Maven使用阿里云镜像加速（解决SSL握手失败问题）
RUN mkdir -p /root/.m2 && \
    cat > /root/.m2/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF

WORKDIR /build

COPY microservices/pom.xml ./microservices/pom.xml
COPY microservices/microservices-common ./microservices/microservices-common
COPY microservices/ioedream-access-service ./microservices/ioedream-access-service

# 先安装父POM,然后安装common,最后构建服务
# 使用awk创建临时父POM（移除modules部分）以避免模块检查错误
# 关键：先备份原始pom.xml，创建临时POM，然后重命名临时POM为pom.xml，这样Maven只会读取修改后的版本
RUN cd microservices && \
    cp pom.xml pom-original.xml && \
    awk '/<modules>/,/<\/modules>/ {next} {print}' pom-original.xml > pom.xml && \
    mvn install:install-file -Dfile=pom.xml -DgroupId=net.lab1024.sa -DartifactId=ioedream-microservices-parent -Dversion=1.0.0 -Dpackaging=pom && \
    cd microservices-common && \
    mvn clean install -N -DskipTests && \
    cd ../ioedream-access-service && \
    mvn clean package -N -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

WORKDIR /app

COPY --from=builder /build/microservices/ioedream-access-service/target/*.jar app.jar

EXPOSE 8090

HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8090/actuator/health || exit 1

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
