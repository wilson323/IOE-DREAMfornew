# 🔧 Maven网络问题修复指南

**问题时间**: 2025-12-07  
**问题**: Maven依赖下载时SSL握手失败  
**状态**: ✅ **Dockerfile V5方案已生效，现在是Maven网络问题**

---

## ✅ 好消息

**Dockerfile V5方案已成功生效！**

从构建日志可以看到：
```dockerfile
cp pom.xml pom-original.xml && \
awk '/<modules>/,/<\/modules>/ {next} {print}' pom-original.xml > pom.xml && \
mvn install:install-file -Dfile=pom.xml
```

✅ 不再出现`Child module ... does not exist`错误  
✅ 父POM成功安装  
✅ microservices-common成功构建

---

## ❌ 新问题：Maven网络/SSL问题

**错误信息**:
```
Could not transfer artifact com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery:jar:2022.0.0.0 
from/to central (https://repo.maven.apache.org/maven2): 
Remote host terminated the handshake: SSL peer shut down incorrectly
```

**原因**:
- 网络连接不稳定
- Maven中央仓库SSL握手问题
- 需要配置国内镜像源加速

---

## ✅ 解决方案

### 方案1: 在Dockerfile中配置Maven镜像（推荐）

修改所有Dockerfile，在构建阶段添加Maven settings.xml：

```dockerfile
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# 配置Maven使用阿里云镜像
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">' >> /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>aliyunmaven</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>*</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <name>阿里云公共仓库</name>' >> /root/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

WORKDIR /build
# ... 其余内容保持不变
```

### 方案2: 使用外部settings.xml文件

1. **创建Maven settings.xml**:
   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts\create-maven-settings.ps1
   ```

2. **修改Dockerfile**:
   ```dockerfile
   FROM maven:3.9.5-eclipse-temurin-17 AS builder
   
   # 复制Maven settings.xml
   COPY maven-settings.xml /root/.m2/settings.xml
   
   WORKDIR /build
   # ... 其余内容保持不变
   ```

### 方案3: 重试构建（网络临时问题）

如果是临时网络问题，可以重试：
```powershell
docker-compose -f docker-compose-all.yml build --no-cache consume-service
```

---

## 🔍 验证步骤

### 检查Maven镜像配置

```powershell
# 检查Dockerfile中是否配置了Maven镜像
Get-Content microservices\ioedream-consume-service\Dockerfile | Select-String -Pattern "aliyun|settings.xml"
```

### 测试Maven连接

```powershell
# 在Docker容器中测试Maven连接
docker run --rm maven:3.9.5-eclipse-temurin-17 mvn dependency:get -Dartifact=com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery:2022.0.0.0
```

---

## 📊 问题分析

### 已解决的问题

- ✅ Dockerfile V5方案已生效
- ✅ 父POM安装成功
- ✅ microservices-common构建成功

### 当前问题

- ❌ Maven依赖下载网络问题
- ❌ SSL握手失败
- ❌ 需要配置镜像源

---

## 🎯 立即执行

**推荐方案**: 在Dockerfile中配置Maven镜像

```powershell
# 1. 创建Maven settings.xml
powershell -ExecutionPolicy Bypass -File scripts\create-maven-settings.ps1

# 2. 修改所有Dockerfile添加Maven镜像配置
# （需要批量更新所有9个Dockerfile）

# 3. 重新构建
docker-compose -f docker-compose-all.yml build --no-cache consume-service
```

---

## 📞 相关文档

- **最终解决方案**: `documentation/deployment/FINAL_SOLUTION.md`
- **开始构建**: `START_BUILD.md`
- **立即修复**: `FIX_NOW.md`

---

**最后更新**: 2025-12-07  
**当前状态**: ✅ V5方案已生效，需要解决Maven网络问题
