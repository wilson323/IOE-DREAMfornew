# 移动端TODO项完善计划

**生成时间**: 2025-01-30  
**版本**: v1.0.0  
**状态**: 🔄 进行中

---

## 📋 TODO项统计

### 发现的TODO项（13个）

| 文件 | TODO数量 | 优先级 | 类型 |
|------|---------|--------|------|
| `visitor/checkin-enhanced.vue` | 2 | P1 | OCR识别、身份证读卡器 |
| `consume/transaction.vue` | 1 | P2 | 统计API调用 |
| `consume/payment.vue` | 1 | P1 | 用户ID获取 |
| `visitor/record.vue` | 1 | P1 | 用户ID获取 |
| `consume/account.vue` | 1 | P1 | 用户ID获取 |
| `consume/index.vue` | 1 | P1 | 用户ID获取 |
| `access/area.vue` | 1 | P1 | 区域列表API |
| `access/permission.vue` | 1 | P1 | 权限列表API |
| `visitor/index.vue` | 2 | P1 | 用户ID获取 |
| `visitor/appointment.vue` | 2 | P1 | 用户ID获取 |

---

## 🎯 完善计划

### P1级：用户ID获取统一化（立即解决）

**问题**: 多个页面使用硬编码`userId = 1`，需要从用户store获取

**涉及文件**:
1. `consume/index.vue` - 消费首页
2. `consume/account.vue` - 账户管理
3. `consume/payment.vue` - 支付页面
4. `visitor/index.vue` - 访客首页
5. `visitor/appointment.vue` - 预约页面
6. `visitor/record.vue` - 记录查询

**解决方案**: 统一使用`useUserStore().employeeId`获取用户ID

### P1级：API调用完善（立即解决）

**问题**: 部分页面缺少API调用实现

**涉及文件**:
1. `access/area.vue` - 区域列表API
2. `access/permission.vue` - 权限列表API
3. `consume/transaction.vue` - 统计API

**解决方案**: 调用对应的API接口

### P2级：OCR和身份证读卡器（1周内）

**问题**: 访客模块需要OCR识别和身份证读卡器功能

**涉及文件**:
1. `visitor/checkin-enhanced.vue` - OCR识别
2. `visitor/checkin-enhanced.vue` - 身份证读卡器

**解决方案**: 集成第三方OCR SDK和身份证读卡器SDK

---

## 📝 实施步骤

### 步骤1: 统一用户ID获取（P1）

**目标**: 所有页面统一使用用户store获取用户ID

**实施**:
1. 在需要用户ID的页面导入`useUserStore`
2. 替换所有硬编码的`userId = 1`为`userStore.employeeId`
3. 添加用户未登录的容错处理

### 步骤2: 完善API调用（P1）

**目标**: 补充缺失的API调用

**实施**:
1. `access/area.vue` - 调用区域列表API
2. `access/permission.vue` - 调用权限列表API
3. `consume/transaction.vue` - 调用统计API

### 步骤3: OCR和身份证读卡器集成（P2）

**目标**: 集成第三方SDK

**实施**:
1. 评估OCR SDK（腾讯云/阿里云）
2. 评估身份证读卡器SDK
3. 集成SDK并实现功能

---

## ✅ 验收标准

1. **用户ID获取**: 所有页面使用统一的用户store获取用户ID
2. **API调用**: 所有API调用完整实现
3. **错误处理**: 添加完善的错误处理和用户提示
4. **代码质量**: 符合项目编码规范

---

**计划制定人**: IOE-DREAM 架构委员会  
**版本**: v1.0.0

