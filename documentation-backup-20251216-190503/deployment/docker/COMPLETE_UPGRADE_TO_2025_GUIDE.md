# Spring Cloud Alibaba 完整升级到 2025.0.0.0 指南

> **升级日期**: 2025-12-08  
> **目标版本**: **2025.0.0.0**  
> **状态**: ✅ 配置已完成，等待重新构建

---

## 🎯 升级目标

**完整升级到 Spring Cloud Alibaba 2025.0.0.0**，包括：
- ✅ 所有配置文件已更新
- ✅ 所有依赖版本已更新
- ⏳ **需要重新构建JAR和Docker镜像**

---

## ⚠️ 关键问题说明

### 为什么仍然报错？

**当前状态**：
- ✅ `pom.xml` 已更新到 2025.0.0.0
- ✅ `application.yml` 已正确配置 `optional:nacos:`
- ✅ `docker-compose-all.yml` 已正确配置环境变量
- ❌ **Docker容器中运行的JAR仍然是旧版本（2022.0.0.0）**

**根本原因**：
- Docker镜像中打包的JAR文件是使用旧版本依赖构建的
- 即使配置文件更新了，运行时的代码仍然是旧版本
- 旧版本的 `NacosConfigDataLocationResolver` 仍然要求 `dataId`

**解决方案**：
- **必须重新构建所有JAR和Docker镜像**

---

## 📋 完整升级步骤

### 步骤1: 执行完整升级脚本（推荐）

```powershell
# 完整升级（清理缓存 + 重新构建 + 重建镜像）
.\scripts\complete-upgrade-to-2025.ps1 -Clean -RebuildImages -SkipTests
```

**脚本功能**：
1. ✅ 验证版本配置（确保pom.xml是2025.0.0.0）
2. ✅ 清理Maven本地缓存（删除旧版本依赖）
3. ✅ 停止Docker服务
4. ✅ 重新构建microservices-common（必须先构建）
5. ✅ 重新构建所有微服务JAR
6. ✅ 重新构建所有Docker镜像
7. ✅ 验证配置一致性

### 步骤2: 手动执行（分步）

如果脚本执行有问题，可以手动执行：

#### 2.1 清理Maven缓存

```powershell
# 删除旧版本的Spring Cloud Alibaba依赖
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\alibaba\cloud\spring-cloud-alibaba-dependencies\2022.0.0.0" -ErrorAction SilentlyContinue
```

#### 2.2 停止Docker服务

```powershell
docker-compose -f docker-compose-all.yml down
```

#### 2.3 重新构建microservices-common（必须先构建）

```powershell
cd microservices
mvn clean install -pl microservices-common -am -DskipTests
```

**为什么必须先构建common？**
- 所有微服务都依赖 `microservices-common`
- 如果common没有先构建，其他服务会找不到依赖
- 这是项目的强制构建顺序要求

#### 2.4 重新构建所有微服务

```powershell
# 在microservices目录下
mvn clean install -DskipTests
```

**构建顺序**（Maven会自动处理）：
1. microservices-common（已构建）
2. 其他所有微服务（并行构建）

#### 2.5 重新构建Docker镜像

```powershell
# 返回项目根目录
cd ..

# 方式1: 使用Docker Compose构建（推荐）
docker-compose -f docker-compose-all.yml build

# 方式2: 手动构建每个服务
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

foreach ($service in $services) {
    cd "microservices\$service"
    docker build -t $service.ToLower():latest .
    cd ..\..
}
```

#### 2.6 启动服务

```powershell
docker-compose -f docker-compose-all.yml up -d
```

---

## ✅ 验证升级

### 1. 检查服务状态

```powershell
docker-compose -f docker-compose-all.yml ps
```

**预期结果**：
- 所有服务状态为 `Up` 或 `Up (healthy)`
- 无服务处于 `Restarting` 或 `Exited` 状态

### 2. 检查服务日志（无dataId错误）

```powershell
# 检查所有服务日志
docker-compose -f docker-compose-all.yml logs | Select-String -Pattern "dataId must be specified" -Context 2

# 如果没有输出，说明没有错误 ✅
```

**预期结果**：
- ✅ 无 `dataId must be specified` 错误
- ✅ 无版本兼容性错误
- ✅ 服务正常启动

### 3. 检查Nacos服务注册

```powershell
# 访问Nacos控制台
# http://localhost:8848/nacos
# 用户名: nacos
# 密码: nacos
```

**预期结果**：
- ✅ 所有9个微服务都已注册到Nacos
- ✅ 服务状态为健康

### 4. 验证依赖版本

```powershell
# 检查JAR文件中的依赖版本
cd microservices\ioedream-gateway-service\target
jar -xf ioedream-gateway-service-*.jar
cat BOOT-INF\lib\spring-cloud-starter-alibaba-nacos-config-*.jar.pom | Select-String "version"
```

**预期结果**：
- ✅ 版本应为 2025.0.0.0 相关版本

---

## 📊 升级完成清单

### 配置文件更新 ✅

- [x] `microservices/pom.xml` - 版本更新到 2025.0.0.0
- [x] 所有9个微服务的 `application.yml` - 配置 `optional:nacos:` 和 `enabled: true`
- [x] `docker-compose-all.yml` - 环境变量 `SPRING_CONFIG_IMPORT=optional:nacos:`

### 构建和部署 ⏳

- [ ] Maven本地缓存已清理
- [ ] microservices-common 已重新构建
- [ ] 所有微服务JAR已重新构建
- [ ] 所有Docker镜像已重新构建
- [ ] 所有服务已重新启动
- [ ] 验证无 `dataId must be specified` 错误
- [ ] 验证服务正常注册到Nacos

---

## 🔧 故障排查

### 问题1: Maven构建失败

**错误**: `The import net.lab1024.sa.common cannot be resolved`

**原因**: microservices-common 没有先构建

**解决**:
```powershell
cd microservices
mvn clean install -pl microservices-common -am -DskipTests
```

### 问题2: Docker镜像构建失败

**错误**: `COPY failed: file not found`

**原因**: JAR文件没有构建成功

**解决**:
```powershell
# 先确保Maven构建成功
cd microservices
mvn clean install -DskipTests

# 再构建Docker镜像
cd ..
docker-compose -f docker-compose-all.yml build
```

### 问题3: 仍然报 dataId 错误

**原因**: Docker镜像中仍然是旧JAR

**解决**:
```powershell
# 1. 强制重新构建（不使用缓存）
docker-compose -f docker-compose-all.yml build --no-cache

# 2. 重新启动服务
docker-compose -f docker-compose-all.yml up -d --force-recreate
```

### 问题4: 服务启动后立即退出

**检查**:
```powershell
# 查看服务日志
docker-compose -f docker-compose-all.yml logs [service-name]

# 检查容器状态
docker ps -a | Select-String "ioedream"
```

---

## 📚 相关文档

- **完整升级报告**: `documentation/deployment/docker/SPRING_CLOUD_ALIBABA_UPGRADE_FULL_REPORT.md`
- **升级完成报告**: `documentation/deployment/docker/SPRING_CLOUD_ALIBABA_2025_UPGRADE_COMPLETE.md`
- **升级总结**: `documentation/deployment/docker/UPGRADE_COMPLETE_SUMMARY.md`
- **验证总结**: `documentation/deployment/docker/UPGRADE_VERIFICATION_SUMMARY.md`

---

## 🚀 快速执行命令

```powershell
# 一键完整升级
.\scripts\complete-upgrade-to-2025.ps1 -Clean -RebuildImages -SkipTests

# 启动服务
docker-compose -f docker-compose-all.yml up -d

# 检查状态
docker-compose -f docker-compose-all.yml ps

# 查看日志
docker-compose -f docker-compose-all.yml logs -f
```

---

**升级完成时间**: 待执行构建后  
**升级版本**: 2025.0.0.0  
**状态**: ✅ **配置已完成，等待重新构建和部署**
