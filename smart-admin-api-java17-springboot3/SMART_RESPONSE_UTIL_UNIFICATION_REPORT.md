# SmartResponseUtil统一替换报告

**执行日期**: 2025-01-30  
**执行范围**: 全局统一替换SmartResponseUtil为ResponseDTO

## ✅ 已完成替换（consume模块）

### 1. AdvancedReportServiceImpl.java ✅
- **文件**: `sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/report/impl/AdvancedReportServiceImpl.java`
- **替换数量**: 31处
- **替换内容**:
  - `SmartResponseUtil.error(message)` → `ResponseDTO.userErrorParam(message)` 或 `ResponseDTO.error(message)`
  - `SmartResponseUtil.success(data)` → `ResponseDTO.ok(data)`
  - `SmartResponseUtil.success(data, message)` → `ResponseDTO.ok(data, message)`
- **状态**: ✅ 完成

### 2. RefundService.java ✅
- **文件**: `sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/RefundService.java`
- **替换数量**: 31处
- **替换内容**:
  - `SmartResponseUtil.error(ResponseDTO.ERROR_PARAM, message)` → `ResponseDTO.error(UserErrorCode.PARAM_ERROR, message)`
  - `SmartResponseUtil.error(ResponseDTO.ERROR_SERVICE, message)` → `ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, message)`
  - `SmartResponseUtil.success(data)` → `ResponseDTO.ok(data)`
  - `SmartResponseUtil.success(message)` → `ResponseDTO.okMsg(message)`
  - `ResponseDTO.SUCCESS_CODE` → `ResponseDTO.OK_CODE`
- **状态**: ✅ 完成

### 3. ConsumeServiceImpl.java ✅
- **文件**: `sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/impl/ConsumeServiceImpl.java`
- **替换数量**: 2处
- **替换内容**:
  - `SmartResponseUtil.success(message)` → `ResponseDTO.okMsg(message)`
  - `SmartResponseUtil.error(message)` → `ResponseDTO.error(message)`
- **状态**: ✅ 完成

### 4. ConsumePermissionService.java ✅
- **文件**: `sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/ConsumePermissionService.java`
- **替换数量**: 8处
- **替换内容**:
  - `SmartResponseUtil.error(message)` → `ResponseDTO.error(message)`
  - `SmartResponseUtil.success(true)` → `ResponseDTO.ok(true)`
- **状态**: ✅ 完成

### 5. MultiPaymentManager.java ✅
- **文件**: `sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/manager/MultiPaymentManager.java`
- **替换数量**: 3处
- **替换内容**:
  - `SmartResponseUtil.error(message)` → `ResponseDTO.error(message)`
  - `SmartResponseUtil.success(data)` → `ResponseDTO.ok(data)`
- **状态**: ✅ 完成

### 6. PaymentSecurityManager.java ✅
- **文件**: `sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/manager/PaymentSecurityManager.java`
- **操作**: 删除未使用的导入
- **状态**: ✅ 完成

## 📊 替换统计（consume模块）

| 文件 | 替换数量 | 状态 |
|------|---------|------|
| AdvancedReportServiceImpl.java | 31处 | ✅ 完成 |
| RefundService.java | 31处 | ✅ 完成 |
| ConsumeServiceImpl.java | 2处 | ✅ 完成 |
| ConsumePermissionService.java | 8处 | ✅ 完成 |
| MultiPaymentManager.java | 3处 | ✅ 完成 |
| PaymentSecurityManager.java | 1处（导入） | ✅ 完成 |
| **总计** | **76处** | ✅ 完成 |

## 🔄 替换映射规则

### 成功响应
- `SmartResponseUtil.success()` → `ResponseDTO.ok()`
- `SmartResponseUtil.success(data)` → `ResponseDTO.ok(data)`
- `SmartResponseUtil.success(message)` → `ResponseDTO.okMsg(message)` 或 `ResponseDTO.ok(null, message)`
- `SmartResponseUtil.success(data, message)` → `ResponseDTO.ok(data, message)`
- `SmartResponseUtil.success(true)` → `ResponseDTO.ok(true)`

### 错误响应
- `SmartResponseUtil.error(message)` → `ResponseDTO.error(message)` 或 `ResponseDTO.userErrorParam(message)`
- `SmartResponseUtil.error(ResponseDTO.ERROR_PARAM, message)` → `ResponseDTO.error(UserErrorCode.PARAM_ERROR, message)`
- `SmartResponseUtil.error(ResponseDTO.ERROR_SERVICE, message)` → `ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, message)`
- `SmartResponseUtil.error(ErrorCode, message)` → `ResponseDTO.error(ErrorCode, message)`

### 其他
- `ResponseDTO.SUCCESS_CODE` → `ResponseDTO.OK_CODE`

## 🔴 待处理（其他模块）

根据全局搜索，还有其他模块需要替换：

### smart-admin-api-java17-springboot3模块（400+处）

#### 1. SmartDeviceController.java - 17处
#### 2. SmartAccessControlController.java - 4处
#### 3. DocumentServiceImpl.java - 33处
#### 4. EmployeeController.java - 6处
#### 5. CacheManagementController.java - 42处
#### 6. UnifiedDeviceController.java - 54处
#### 7. 其他Controller和服务类 - 200+处

### microservices模块（100+处）

#### 1. ioedream-device-service - 多处
#### 2. ioedream-system-service - 多处
#### 3. ioedream-access-service - 多处
#### 4. 其他服务模块 - 多处

## 📝 替换步骤

### 已完成步骤 ✅
1. ✅ consume模块全部替换完成（76处）
2. ✅ 删除SmartResponseUtil导入
3. ✅ 添加ErrorCode导入（UserErrorCode, SystemErrorCode）
4. ✅ 统一错误码使用

### 下一步计划 🔄
1. 🔄 替换smart-admin-api-java17-springboot3模块其他Controller
2. 🔄 替换microservices其他服务模块
3. 🔄 验证所有替换后的代码编译正常
4. 🔄 运行测试确保功能正常

## ⚠️ 注意事项

1. **错误码选择**:
   - 参数错误 → `UserErrorCode.PARAM_ERROR`
   - 系统错误 → `SystemErrorCode.SYSTEM_ERROR`
   - 业务错误 → 使用对应的UserErrorCode枚举值

2. **方法选择**:
   - 参数验证失败 → `ResponseDTO.userErrorParam(message)`
   - 系统异常 → `ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, message)`
   - 一般错误 → `ResponseDTO.error(message)`

3. **导入添加**:
   - 需要导入 `UserErrorCode` 和 `SystemErrorCode`
   - 删除 `SmartResponseUtil` 导入

---

**报告生成时间**: 2025-01-30  
**当前进度**: consume模块100%完成，其他模块待处理  
**下一步**: 继续替换其他模块

