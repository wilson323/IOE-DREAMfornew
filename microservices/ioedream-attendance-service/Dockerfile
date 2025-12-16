# IOE-DREAM Attendance Service Dockerfile
# 使用预构建的JAR文件，需要先在本地执行: mvn clean install -DskipTests -Dpmd.skip=true
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

WORKDIR /app

COPY microservices/ioedream-attendance-service/target/ioedream-attendance-service-1.0.0.jar app.jar

EXPOSE 8091

HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8091/actuator/health || exit 1

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
