# 门禁模块构建说明

> **重要提醒**: 必须严格按照构建顺序执行，否则会导致编译失败

---

## 🔨 构建顺序（强制执行）

### 步骤1: 构建公共模块（必须首先执行）

```powershell
# 进入microservices目录
cd D:\IOE-DREAM\microservices

# 构建microservices-common-business（包含AreaAccessExtEntity等）
mvn clean install -pl microservices-common-business -am -DskipTests

# 验证JAR文件已安装
Test-Path "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common-business\*\microservices-common-business-*.jar"
```

### 步骤2: 构建门禁服务

```powershell
# 构建ioedream-access-service
mvn clean install -pl ioedream-access-service -am -DskipTests
```

---

## ⚠️ 依赖关系说明

### 门禁服务依赖的公共模块类

**实体类**:
- `net.lab1024.sa.common.organization.entity.AreaAccessExtEntity`
- `net.lab1024.sa.common.organization.entity.AntiPassbackRecordEntity`

**DAO接口**:
- `net.lab1024.sa.common.organization.dao.AreaAccessExtDao`
- `net.lab1024.sa.common.organization.dao.AntiPassbackRecordDao`
- `net.lab1024.sa.common.organization.dao.UserAreaPermissionDao`

**Manager类**:
- `net.lab1024.sa.common.organization.manager.UserAreaPermissionManager`

**其他**:
- `net.lab1024.sa.common.gateway.GatewayServiceClient`

### 如果构建失败

**错误**: `The import net.lab1024.sa.common cannot be resolved`

**解决方案**:
1. 确保已先构建`microservices-common-business`
2. 检查Maven本地仓库中是否存在JAR文件
3. 运行`mvn dependency:resolve`解析依赖

---

## 📋 验证清单

构建完成后，验证以下内容：

- [ ] `microservices-common-business`构建成功
- [ ] `ioedream-access-service`构建成功
- [ ] 所有导入的类都能正确解析
- [ ] 单元测试可以运行
- [ ] 服务可以正常启动

---

**最后更新**: 2025-01-30
