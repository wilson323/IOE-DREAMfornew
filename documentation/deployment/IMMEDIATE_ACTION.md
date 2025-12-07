# ⚡ 立即执行 - Docker缓存问题修复

**问题**: Docker使用缓存的旧版本Dockerfile（V4方案），导致构建失败  
**解决方案**: 彻底清理Docker缓存并强制重建

---

## 🚨 问题确认

从错误日志看，Docker构建时显示：
```
awk '/<modules>/,/<\/modules>/ {next} {print}' pom.xml > pom-temp.xml
```

这是**V4方案（已废弃）**，但Dockerfile文件本身已经修复为**V5方案**：
```
cp pom.xml pom-original.xml && \
awk '/<modules>/,/<\/modules>/ {next} {print}' pom-original.xml > pom.xml
```

**结论**: Docker使用了缓存的旧版本构建上下文。

---

## ✅ 立即执行（按顺序）

### 步骤1: 验证Dockerfile修复

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-dockerfile-v5.ps1
```

**预期结果**: ✅ 所有9个Dockerfile使用V5方案

### 步骤2: 清理Docker构建缓存（关键！）

```powershell
# 清理所有构建缓存
docker builder prune -af
```

### 步骤3: 强制重新构建

```powershell
# 停止所有容器
docker-compose -f docker-compose-all.yml down -v

# 强制重新构建（不使用缓存，拉取最新基础镜像）
docker-compose -f docker-compose-all.yml build --no-cache --pull
```

### 步骤4: 如果仍然失败，使用核武器级清理

```powershell
# 彻底清理所有Docker资源并重建
powershell -ExecutionPolicy Bypass -File scripts\nuclear-clean-rebuild.ps1
```

---

## 🔍 验证修复

### 检查Dockerfile内容

```powershell
# 检查gateway-service的Dockerfile
Get-Content microservices\ioedream-gateway-service\Dockerfile | Select-String -Pattern "pom-original.xml"
```

**应该看到**:
```
cp pom.xml pom-original.xml && \
awk '/<modules>/,/<\/modules>/ {next} {print}' pom-original.xml > pom.xml && \
mvn install:install-file -Dfile=pom.xml
```

**不应该看到**:
```
awk ... > pom-temp.xml
mvn install:install-file -Dfile=pom-temp.xml
```

---

## 📊 修复方案对比

| 方案 | 命令 | 状态 |
|------|------|------|
| V3 | `python3 -c ...` | ❌ 已废弃（Maven镜像无Python） |
| V4 | `awk ... > pom-temp.xml` | ❌ 已废弃（Maven仍读取原始pom.xml） |
| **V5** | `cp pom.xml pom-original.xml && awk ... > pom.xml` | ✅ **当前方案** |

---

## 🎯 关键命令

### 一键执行（推荐）

```powershell
# 核武器级清理和重建
powershell -ExecutionPolicy Bypass -File scripts\nuclear-clean-rebuild.ps1
```

### 手动执行

```powershell
# 1. 验证修复
powershell -ExecutionPolicy Bypass -File scripts\verify-dockerfile-v5.ps1

# 2. 清理缓存
docker builder prune -af

# 3. 重新构建
docker-compose -f docker-compose-all.yml down -v
docker-compose -f docker-compose-all.yml build --no-cache --pull
```

---

## 📞 相关文档

- **关键修复**: `documentation/deployment/CRITICAL_FIX.md`
- **紧急修复**: `documentation/deployment/URGENT_FIX.md`
- **故障排查**: `documentation/deployment/TROUBLESHOOTING.md`

---

**立即执行**: 
```powershell
docker builder prune -af
docker-compose -f docker-compose-all.yml build --no-cache --pull
```
