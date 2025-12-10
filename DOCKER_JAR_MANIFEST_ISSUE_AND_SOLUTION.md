# Docker微服务JAR包清单问题及解决方案

**问题时间**: 2025-12-08 00:15  
**问题级别**: 🔴 P0 - 阻塞性问题  
**影响范围**: 所有9个微服务

---

## 🚨 问题描述

### 症状

所有微服务容器持续重启:
```
ioedream-common-service        Restarting (1) 25 seconds ago
ioedream-gateway-service       Restarting (1) 27 seconds ago
ioedream-access-service        Restarting (1) 25 seconds ago
... (所有9个微服务)
```

### 错误日志

```bash
docker logs ioedream-common-service --tail 50
```

输出:
```
no main manifest attribute, in /app/app.jar
no main manifest attribute, in /app/app.jar
no main manifest attribute, in /app/app.jar
(持续重复)
```

---

## 🔍 根本原因分析

### Maven构建参数错误

**问题代码**: `microservices/ioedream-common-service/Dockerfile` 第36行

```dockerfile
# ❌ 错误: 使用了 -N (Non-recursive) 参数
RUN cd microservices && \
    cp pom.xml pom-original.xml && \
    awk '/<modules>/,/<\/modules>/ {next} {print}' pom-original.xml > pom.xml && \
    mvn install:install-file -Dfile=pom.xml -DgroupId=net.lab1024.sa -DartifactId=ioedream-microservices-parent -Dversion=1.0.0 -Dpackaging=pom && \
    cd microservices-common && \
    mvn clean install -N -DskipTests && \          # ❌ 问题1: -N导致不完整构建
    cd ../ioedream-common-service && \
    mvn clean package -N -DskipTests                # ❌ 问题2: -N导致不生成可执行JAR
```

### -N参数的影响

**Maven `-N` 参数含义**: Non-recursive (不递归构建子模块)

**实际效果**:
1. ✅ 编译了Java源码 → 生成了class文件
2. ❌ 没有运行Spring Boot Maven Plugin → 没有生成可执行JAR
3. ❌ JAR包缺少MANIFEST.MF中的Main-Class属性
4. ❌ JAR包不包含依赖库

### 验证问题

```bash
# 检查JAR包内容
unzip -l app.jar | grep MANIFEST

# 正常的Spring Boot JAR应该包含:
BOOT-INF/classes/           # 应用代码
BOOT-INF/lib/               # 依赖库
META-INF/MANIFEST.MF        # 清单文件，包含Main-Class
org/springframework/boot/   # Spring Boot Loader

# 问题JAR只有:
META-INF/MANIFEST.MF        # ❌ 缺少Main-Class
com/yourpackage/            # 应用代码
```

---

## ✅ 解决方案

### 方案1: 修复Dockerfile (推荐用于生产环境)

**修改所有微服务的Dockerfile**:

#### 修复common-service Dockerfile

```dockerfile
# ✅ 正确: 移除 -N 参数，完整构建
RUN cd microservices && \
    cp pom.xml pom-original.xml && \
    awk '/<modules>/,/<\/modules>/ {next} {print}' pom-original.xml > pom.xml && \
    mvn install:install-file -Dfile=pom.xml -DgroupId=net.lab1024.sa -DartifactId=ioedream-microservices-parent -Dversion=1.0.0 -Dpackaging=pom && \
    cd microservices-common && \
    mvn clean install -DskipTests && \              # ✅ 完整构建common模块
    cd ../ioedream-common-service && \
    mvn clean package -DskipTests                   # ✅ 生成可执行JAR
```

**需要修复的Dockerfile文件列表**:
1. ✅ `microservices/ioedream-common-service/Dockerfile` (已修复)
2. ⏳ `microservices/ioedream-gateway-service/Dockerfile`
3. ⏳ `microservices/ioedream-device-comm-service/Dockerfile`
4. ⏳ `microservices/ioedream-oa-service/Dockerfile`
5. ⏳ `microservices/ioedream-access-service/Dockerfile`
6. ⏳ `microservices/ioedream-attendance-service/Dockerfile`
7. ⏳ `microservices/ioedream-video-service/Dockerfile`
8. ⏳ `microservices/ioedream-consume-service/Dockerfile`
9. ⏳ `microservices/ioedream-visitor-service/Dockerfile`

#### 重新构建镜像

```bash
# 1. 停止所有服务
docker-compose -f docker-compose-all.yml down

# 2. 删除旧镜像
docker rmi $(docker images 'ioedream/*' -q)

# 3. 重新构建（需要较长时间，约30-60分钟）
docker-compose -f docker-compose-all.yml build --no-cache

# 4. 启动服务
docker-compose -f docker-compose-all.yml up -d
```

**预计时间**: 30-60分钟（首次构建）

---

### 方案2: 使用本地JAR包部署 (推荐用于开发调试)

**优势**:
- ✅ 构建速度快（使用本地Maven缓存）
- ✅ 便于调试和快速迭代
- ✅ 不需要Docker镜像构建
- ✅ 资源占用少

#### 步骤1: 本地构建所有JAR包

```bash
cd microservices
mvn clean package -DskipTests
```

**预计时间**: 2-5分钟

#### 步骤2: 使用Docker Compose本地JAR模式

创建 `docker-compose-local-jar.yml`:

```yaml
services:
  # 基础设施服务（MySQL, Redis, Nacos）保持不变
  mysql:
    image: mysql:8.0
    # ... 配置同docker-compose-all.yml

  redis:
    image: redis:7-alpine
    # ... 配置同docker-compose-all.yml

  nacos:
    image: nacos/nacos-server:v2.3.0
    # ... 配置同docker-compose-all.yml

  # 微服务使用本地JAR包
  common-service:
    image: eclipse-temurin:17-jre
    container_name: ioedream-common-service
    volumes:
      - ./ioedream-common-service/target/ioedream-common-service-1.0.0.jar:/app/app.jar:ro
    environment:
      - SERVER_PORT=8088
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      # ... 其他环境变量
    command: java -Xms512m -Xmx1024m -jar /app/app.jar
    ports:
      - "8088:8088"
    depends_on:
      nacos:
        condition: service_healthy
    networks:
      - ioedream-network
```

#### 步骤3: 启动服务

```bash
docker-compose -f docker-compose-local-jar.yml up -d
```

---

### 方案3: 直接本地运行 (开发环境最快)

**不使用Docker,直接在本地运行**:

#### 前置条件
1. ✅ Docker运行MySQL和Redis
2. ✅ Docker运行Nacos
3. ✅ 本地有Java 17环境

#### 启动基础设施

```bash
# 只启动MySQL、Redis、Nacos
docker-compose -f docker-compose-all.yml up -d mysql redis nacos
```

#### 本地运行微服务

```bash
# 终端1: Gateway
cd microservices/ioedream-gateway-service
mvn spring-boot:run

# 终端2: Common Service
cd microservices/ioedream-common-service
mvn spring-boot:run

# 终端3-10: 其他微服务...
```

或使用IDE (IntelliJ IDEA / VSCode):
1. 打开每个微服务模块
2. 运行主类(XxxApplication.java)

---

## 📊 方案对比

| 方案 | 构建时间 | 启动时间 | 调试便捷性 | 资源占用 | 适用场景 |
|------|----------|----------|------------|----------|----------|
| **方案1: Docker镜像** | 30-60分钟 | 2-5分钟 | ⭐⭐ | 高 | 生产环境 |
| **方案2: 本地JAR+Docker** | 2-5分钟 | 1-2分钟 | ⭐⭐⭐⭐ | 中 | 开发测试 |
| **方案3: 完全本地运行** | 1-2分钟 | <1分钟 | ⭐⭐⭐⭐⭐ | 低 | 本地开发 |

---

## 🔧 完整修复步骤 (推荐方案2)

### Step 1: 停止当前服务

```bash
docker-compose -f docker-compose-all.yml down
```

### Step 2: 本地构建JAR包

```bash
cd microservices
mvn clean package -DskipTests
```

### Step 3: 验证JAR包

```bash
# 检查JAR包是否存在
ls -lh ioedream-*/target/*.jar

# 验证JAR包可执行
java -jar ioedream-common-service/target/ioedream-common-service-1.0.0.jar --version
```

### Step 4: 创建本地JAR部署配置

创建 `docker-compose-local-jar.yml` (见方案2详细配置)

### Step 5: 启动服务

```bash
docker-compose -f docker-compose-local-jar.yml up -d
```

### Step 6: 验证服务健康

```bash
# 检查容器状态
docker-compose -f docker-compose-local-jar.yml ps

# 检查服务健康
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8088/actuator/health  # Common Service
```

---

## 📋 修复验证清单

### 基础设施
- [ ] MySQL健康运行 (端口3306)
- [ ] Redis健康运行 (端口6379)
- [ ] Nacos健康运行 (端口8848)
- [ ] nacos数据库已初始化

### JAR包构建
- [ ] microservices-common构建成功
- [ ] ioedream-gateway-service JAR包存在
- [ ] ioedream-common-service JAR包存在
- [ ] 其他7个微服务JAR包存在
- [ ] 所有JAR包可执行（包含Main-Class清单）

### 服务启动
- [ ] Gateway Service启动成功
- [ ] Common Service启动成功
- [ ] 所有业务服务启动成功
- [ ] 服务注册到Nacos成功

### 功能验证
- [ ] 可以访问Gateway健康检查
- [ ] 可以访问Nacos控制台
- [ ] 服务间调用正常

---

## 🚀 后续建议

### 立即执行
1. **选择方案2** - 使用本地JAR+Docker模式快速恢复服务
2. **验证功能** - 确保所有服务正常运行
3. **记录问题** - 更新部署文档

### 长期优化
1. **修复所有Dockerfile** - 为生产环境做准备
2. **建立CI/CD** - 自动化构建和部署流程
3. **优化镜像大小** - 使用多阶段构建和分层缓存

### 文档更新
- [ ] 更新部署文档说明构建参数
- [ ] 创建Docker镜像构建规范
- [ ] 编写故障排查手册

---

## 📚 相关文档

- **Docker Compose配置**: [docker-compose-all.yml](./docker-compose-all.yml)
- **Dockerfile模板**: [microservices/*/Dockerfile](./microservices/)
- **Maven构建配置**: [microservices/pom.xml](./microservices/pom.xml)
- **Spring Boot打包插件**: [Spring Boot Maven Plugin文档](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/)

---

**问题分析**: AI Assistant  
**修复方案**: AI Assistant  
**状态**: ⏳ 待执行修复  
**优先级**: 🔴 P0 - 立即处理
