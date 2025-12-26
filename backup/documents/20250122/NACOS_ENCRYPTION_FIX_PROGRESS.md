# IOE-DREAM Nacos密码加密修复进度跟踪

## 📊 修复进度总览

- **总实例数**: 22个
- **已修复**: 0个
- **待修复**: 22个
- **完成率**: 0%

---

## ✅ 修复状态清单

### 【1】ioedream-gateway-service (4个实例)

#### 文件1: `microservices/ioedream-gateway-service/src/main/resources/application.yml`
- [ ] 第74行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第82行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

#### 文件2: `microservices/ioedream-gateway-service/src/main/resources/bootstrap.yml`
- [ ] 第30行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第45行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

### 【2】ioedream-consume-service (6个实例)

#### 文件1: `microservices/ioedream-consume-service/src/main/resources/application.yml`
- [ ] 第78行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第86行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

#### 文件2: `microservices/ioedream-consume-service/src/main/resources/bootstrap.yml`
- [ ] 第30行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第47行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第101行: `password: ${NACOS_PASSWORD}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第111行: `password: ${NACOS_PASSWORD}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

### 【3】ioedream-access-service (1个实例)

#### 文件: `microservices/ioedream-access-service/src/main/resources/application.yml`
- [ ] 第35行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

### 【4】ioedream-attendance-service (2个实例)

#### 文件: `microservices/ioedream-attendance-service/src/main/resources/bootstrap.yml`
- [ ] 第30行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第48行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

### 【5】ioedream-video-service (2个实例)

#### 文件: `microservices/ioedream-video-service/src/main/resources/bootstrap.yml`
- [ ] 第30行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第48行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

### 【6】ioedream-visitor-service (2个实例)

#### 文件: `microservices/ioedream-visitor-service/src/main/resources/bootstrap.yml`
- [ ] 第30行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第48行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

### 【7】ioedream-device-comm-service (2个实例)

#### 文件: `microservices/ioedream-device-comm-service/src/main/resources/bootstrap.yml`
- [ ] 第30行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第47行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

### 【8】ioedream-oa-service (2个实例)

#### 文件: `microservices/ioedream-oa-service/src/main/resources/bootstrap.yml`
- [ ] 第30行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第47行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

### 【9】ioedream-database-service (1个实例)

#### 文件: `microservices/ioedream-database-service/src/main/resources/application.yml`
- [ ] 第17行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

### 【10】公共配置 (2个实例)

#### 文件: `microservices/common-config/seata/application-seata.yml`
- [ ] 第23行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
- [ ] 第33行: `password: ${NACOS_PASSWORD:nacos}` → `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`

---

## 🔧 快速修复指南

### 修复格式

所有修复都使用相同的格式：

```yaml
# 修复前
password: ${NACOS_PASSWORD:nacos}

# 修复后
password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}
```

### 批量查找替换（IDE操作）

在IDE中使用查找替换功能：

1. **查找**: `password: ${NACOS_PASSWORD:nacos}`
2. **替换**: `password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}`
3. **作用范围**: 仅在 `microservices/**/src/main/resources/*.yml` 文件中

### 特殊格式修复

对于无默认值的情况：

```yaml
# 查找
password: ${NACOS_PASSWORD}

# 替换为
password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vLPrYnCJNA==)}
```

---

## ✅ 验证修复

修复完成后，运行验证脚本：

```powershell
.\scripts\security\verify-encrypted-config.ps1 -Detailed
```

**预期结果**: 
- 加密覆盖率: 100%
- 明文密码: 0个

---

## 📝 修复记录

### 修复日志

| 日期 | 文件 | 修复数量 | 状态 |
|------|------|---------|------|
| - | - | - | 待修复 |

---

**最后更新**: 2025-01-30  
**加密值**: `ENC(I2//JcHFpBc7vLPrYnCJNA==)`  
**状态**: ⏳ 等待手动修复

