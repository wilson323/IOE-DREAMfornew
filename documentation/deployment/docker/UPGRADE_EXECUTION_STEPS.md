# Spring Cloud Alibaba 升级到 2025.0.0.0 - 执行步骤

> **执行日期**: 2025-12-08  
> **目标版本**: **2025.0.0.0**  
> **配置状态**: ✅ **全部完成**  
> **执行状态**: ⏳ **等待执行**

---

## 🎯 执行目标

**完整升级到 Spring Cloud Alibaba 2025.0.0.0**，解决 `dataId must be specified` 错误。

---

## ⚠️ 重要说明

### 为什么需要重新构建？

**当前问题**：
- ✅ 所有配置文件已更新到 2025.0.0.0
- ✅ 所有配置都正确
- ❌ **Docker容器中运行的JAR仍然是旧版本（2022.0.0.0）**

**错误原因**：
```
java.lang.IllegalArgumentException: dataId must be specified
	at com.alibaba.cloud.nacos.configdata.NacosConfigDataLocationResolver.loadConfigDataResources(NacosConfigDataLocationResolver.java:165)
```

这个错误来自**旧版本的代码**（2022.0.0.0），即使配置文件更新了，运行时的JAR文件仍然是旧版本。

**解决方案**：
- **必须重新构建所有JAR和Docker镜像**

---

## 🚀 执行步骤

### 步骤1: 验证配置（可选但推荐）

```powershell
# 验证所有配置是否正确
.\scripts\verify-2025-upgrade-config.ps1
```

**预期输出**：
- ✅ Parent POM version: 2025.0.0.0
- ✅ 所有9个微服务配置正确
- ✅ Docker Compose环境变量正确

---

### 步骤2: 执行完整升级（推荐）

```powershell
# 完整升级（一键执行）
.\scripts\complete-upgrade-to-2025.ps1 -Clean -RebuildImages -SkipTests
```

**脚本功能**：
1. ✅ 验证版本配置
2. ✅ 清理Maven本地缓存（删除旧版本依赖）
3. ✅ 停止Docker服务
4. ✅ 重新构建microservices-common（必须先构建）
5. ✅ 重新构建所有微服务JAR
6. ✅ 重新构建所有Docker镜像
7. ✅ 验证配置一致性

**执行时间**: 约10-20分钟（取决于机器性能）

---

### 步骤3: 启动服务

```powershell
# 启动所有服务
docker-compose -f docker-compose-all.yml up -d

# 检查服务状态
docker-compose -f docker-compose-all.yml ps
```

---

### 步骤4: 验证升级

```powershell
# 检查服务日志（确认无dataId错误）
docker-compose -f docker-compose-all.yml logs | Select-String -Pattern "dataId must be specified"

# 如果没有输出，说明没有错误 ✅
```

**预期结果**：
- ✅ 无 `dataId must be specified` 错误
- ✅ 无版本兼容性错误
- ✅ 服务正常启动

---

## 📋 手动执行步骤（如果脚本失败）

### 2.1 清理Maven缓存

```powershell
# 删除旧版本的Spring Cloud Alibaba依赖
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\alibaba\cloud\spring-cloud-alibaba-dependencies\2022.0.0.0" -ErrorAction SilentlyContinue
```

### 2.2 停止Docker服务

```powershell
docker-compose -f docker-compose-all.yml down
```

### 2.3 重新构建microservices-common（必须先构建）

```powershell
cd microservices
mvn clean install -pl microservices-common -am -DskipTests
```

**为什么必须先构建common？**
- 所有微服务都依赖 `microservices-common`
- 如果common没有先构建，其他服务会找不到依赖
- 这是项目的强制构建顺序要求

### 2.4 重新构建所有微服务

```powershell
# 在microservices目录下
mvn clean install -DskipTests
```

**构建顺序**（Maven会自动处理）：
1. microservices-common（已构建）
2. 其他所有微服务（并行构建）

### 2.5 重新构建Docker镜像

```powershell
# 返回项目根目录
cd ..

# 使用Docker Compose构建（推荐）
docker-compose -f docker-compose-all.yml build
```

### 2.6 启动服务

```powershell
docker-compose -f docker-compose-all.yml up -d
```

---

## ✅ 验证清单

执行升级后，请验证以下项目：

- [ ] 所有服务正常启动
- [ ] 无`dataId must be specified`错误
- [ ] 无版本兼容性错误
- [ ] Nacos服务发现正常（服务注册成功）
- [ ] 服务间调用正常
- [ ] 网关路由正常

**验证命令**:

```powershell
# 检查服务健康状态
docker-compose -f docker-compose-all.yml ps

# 检查服务日志（无错误）
docker-compose -f docker-compose-all.yml logs | Select-String -Pattern "ERROR|Exception" -Context 2

# 检查Nacos服务注册
# 访问 http://localhost:8848/nacos 查看服务列表
```

---

## 🔧 故障排查

### 问题1: Maven构建失败 - microservices-common找不到

**错误**: `The import net.lab1024.sa.common cannot be resolved`

**原因**: microservices-common 没有先构建

**解决**:
```powershell
cd microservices
mvn clean install -pl microservices-common -am -DskipTests
```

### 问题2: Docker镜像构建失败 - JAR文件找不到

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

## 📊 配置验证结果

### 已完成的配置更新 ✅

| 配置项 | 状态 | 说明 |
|--------|------|------|
| 父POM版本 | ✅ | 2025.0.0.0 |
| 9个微服务application.yml | ✅ | 全部配置 `optional:nacos:` 和 `enabled: true` |
| Docker Compose环境变量 | ✅ | 9个服务全部配置 `SPRING_CONFIG_IMPORT=optional:nacos:` |

### 待执行的构建 ⏳

| 构建项 | 状态 | 说明 |
|--------|------|------|
| Maven本地缓存清理 | ⏳ | 需要删除旧版本依赖 |
| microservices-common构建 | ⏳ | 必须先构建 |
| 所有微服务JAR构建 | ⏳ | 使用新版本依赖构建 |
| Docker镜像构建 | ⏳ | 使用新JAR构建镜像 |
| 服务重启 | ⏳ | 使用新镜像启动 |

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

## 📚 相关文档

- **完整升级指南**: `documentation/deployment/docker/COMPLETE_UPGRADE_TO_2025_GUIDE.md`
- **最终总结**: `documentation/deployment/docker/FINAL_UPGRADE_TO_2025_SUMMARY.md`
- **完整升级报告**: `documentation/deployment/docker/SPRING_CLOUD_ALIBABA_UPGRADE_FULL_REPORT.md`

---

**配置完成时间**: 2025-12-08  
**升级版本**: 2025.0.0.0  
**配置状态**: ✅ **全部完成**  
**执行状态**: ⏳ **等待执行**  
**下一步**: 执行 `.\scripts\complete-upgrade-to-2025.ps1 -Clean -RebuildImages -SkipTests`
