# IOE-DREAM Nacos密码加密 - 下一步操作指南

## 📊 当前状态

- ✅ **加密值已生成**: `ENC(I2//JcHFpBc7vLPrYnCJNA==)`
- ✅ **扫描完成**: 发现22个明文密码实例
- ✅ **修复清单已创建**: 包含所有修复位置
- ⏳ **等待手动修复**: 0/22 已完成

---

## 🎯 立即开始修复

### 方法1: IDE批量查找替换（推荐，最快）

#### 步骤1: 打开IDE全局查找替换

1. **VS Code**: `Ctrl+Shift+H` (全局查找替换)
2. **IntelliJ IDEA**: `Ctrl+Shift+R` (全局查找替换)
3. **Eclipse**: `Ctrl+H` (搜索)

#### 步骤2: 设置查找替换

**查找内容**:
```
password: ${NACOS_PASSWORD:nacos}
```

**替换为**:
```
password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}
```

**作用范围**: 
- 仅限 `microservices/**/src/main/resources/*.yml` 文件
- 排除 `target` 目录

#### 步骤3: 处理特殊格式

**查找内容**:
```
password: ${NACOS_PASSWORD}
```

**替换为**:
```
password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}
```

**作用范围**: 
- 仅限 `microservices/ioedream-consume-service/src/main/resources/bootstrap.yml`
- 第101行和第111行

---

### 方法2: 逐个文件手动修复

参考详细修复清单: `NACOS_PASSWORD_ENCRYPTION_FIX_LIST.md`

每个文件包含：
- 精确的行号
- 修复前后的完整代码
- 修复格式说明

---

## ✅ 修复后验证

### 快速检查（推荐）

```powershell
.\scripts\security\quick-check-fix.ps1 -Detailed
```

**预期输出**:
```
修复进度统计:
  待修复: 0 个
  已修复: 22 个
  完成率: 100%

✅ 所有明文密码已修复！
```

### 完整验证

```powershell
.\scripts\security\verify-encrypted-config.ps1 -Detailed
```

---

## 📋 修复检查清单

修复完成后，确认以下文件：

- [ ] `ioedream-gateway-service/application.yml` (2处)
- [ ] `ioedream-gateway-service/bootstrap.yml` (2处)
- [ ] `ioedream-consume-service/application.yml` (2处)
- [ ] `ioedream-consume-service/bootstrap.yml` (4处，包含2个无默认值)
- [ ] `ioedream-access-service/application.yml` (1处)
- [ ] `ioedream-attendance-service/bootstrap.yml` (2处)
- [ ] `ioedream-video-service/bootstrap.yml` (2处)
- [ ] `ioedream-visitor-service/bootstrap.yml` (2处)
- [ ] `ioedream-device-comm-service/bootstrap.yml` (2处)
- [ ] `ioedream-oa-service/bootstrap.yml` (2处)
- [ ] `ioedream-database-service/application.yml` (1处)
- [ ] `common-config/seata/application-seata.yml` (2处)

**总计**: 22个实例

---

## 🔧 修复格式说明

### 标准格式修复

```yaml
# 修复前
password: ${NACOS_PASSWORD:nacos}

# 修复后
password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}
```

### 特殊格式修复（无默认值）

```yaml
# 修复前
password: ${NACOS_PASSWORD}

# 修复后
password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}
```

---

## 📚 相关文档

- **详细修复清单**: `NACOS_PASSWORD_ENCRYPTION_FIX_LIST.md` - 包含所有修复位置的详细说明
- **修复进度跟踪**: `NACOS_ENCRYPTION_FIX_PROGRESS.md` - 跟踪修复进度
- **手动修复指南**: `documentation/security/NACOS_ENCRYPTION_MANUAL_FIX_GUIDE.md` - 完整的修复指南

---

## 🚨 重要提醒

1. **备份文件**: 修复前建议备份所有配置文件
2. **逐步验证**: 每修复几个文件，运行快速检查脚本验证
3. **测试启动**: 修复完成后，测试关键服务是否可以正常启动
4. **密钥安全**: 加密密钥 `IOE-DREAM-Nacos-Encryption-Key-2025` 必须安全存储

---

## ⚡ 快速命令

```powershell
# 检查修复进度
.\scripts\security\quick-check-fix.ps1 -Detailed

# 完整验证
.\scripts\security\verify-encrypted-config.ps1 -Detailed

# 重新扫描明文密码
.\scripts\security\scan-plaintext-passwords-v2.ps1 -Detailed
```

---

**状态**: ⏳ 等待手动修复  
**加密值**: `ENC(I2//JcHFpBc7vLPrYnCJNA==)`  
**建议**: 使用IDE批量查找替换功能，最快5分钟完成所有修复

