# 消费模块API接口契约文档

**生成时间**: 2025-01-30  
**文档版本**: v1.0.0  
**模块**: 消费管理模块 (ioedream-consume-service)  
**状态**: ✅ **已完成**

---

## 📋 文档说明

本文档定义了消费模块的所有API接口契约，包括：
- 移动端API接口
- PC端API接口（待补充）
- 支付相关API接口
- 对账相关API接口
- 退款/报销申请API接口

**API基础路径**: `/api/v1/consume`

---

## 📱 一、移动端API接口

### 1.1 消费交易接口

**基础路径**: `/api/v1/consume/mobile/transaction`

#### 1.1.1 快速消费

**接口**: `POST /api/v1/consume/mobile/transaction/quick`

**功能**: 移动端快速消费交易

**请求参数**:
```typescript
interface ConsumeMobileQuickForm {
  deviceId: number;      // 设备ID
  userId: number;        // 用户ID
  amount: number;        // 消费金额（单位：元）
  consumeMode?: string;  // 消费模式（FIXED/AMOUNT/PRODUCT/COUNT）
  remark?: string;       // 备注
}
```

**响应数据**:
```typescript
interface ConsumeMobileResultVO {
  transactionNo: string;     // 交易流水号
  success: boolean;           // 是否成功
  message?: string;           // 提示信息
  balance?: number;           // 账户余额（单位：元）
  consumeAmount: number;      // 消费金额（单位：元）
}
```

**Controller**: `ConsumeMobileController.quickConsume()`

---

#### 1.1.2 扫码消费

**接口**: `POST /api/v1/consume/mobile/transaction/scan`

**功能**: 移动端扫码消费交易

**请求参数**:
```typescript
interface ConsumeMobileScanForm {
  deviceId: number;      // 设备ID
  qrCode: string;       // 二维码内容
  amount?: number;      // 消费金额（可选，商品模式时从商品获取）
}
```

**响应数据**: 同快速消费

**Controller**: `ConsumeMobileController.scanConsume()`

---

#### 1.1.3 NFC消费

**接口**: `POST /api/v1/consume/mobile/transaction/nfc`

**功能**: 移动端NFC消费交易

**请求参数**:
```typescript
interface ConsumeMobileNfcForm {
  deviceId: number;      // 设备ID
  cardNumber: string;   // 卡号
  amount?: number;      // 消费金额（可选）
}
```

**响应数据**: 同快速消费

**Controller**: `ConsumeMobileController.nfcConsume()`

---

#### 1.1.4 人脸识别消费

**接口**: `POST /api/v1/consume/mobile/transaction/face`

**功能**: 移动端人脸识别消费交易

**请求参数**:
```typescript
interface ConsumeMobileFaceForm {
  deviceId: number;      // 设备ID
  faceFeatures: string;  // 人脸特征值
  amount?: number;      // 消费金额（可选）
}
```

**响应数据**: 同快速消费

**Controller**: `ConsumeMobileController.faceConsume()`

---

### 1.2 用户查询接口

**基础路径**: `/api/v1/consume/mobile/user`

#### 1.2.1 快速用户查询

**接口**: `GET /api/v1/consume/mobile/user/quick`

**功能**: 根据类型快速查询用户信息

**请求参数**:
- `queryType`: string - 查询类型（phone/cardNumber/faceId/userId）
- `queryValue`: string - 查询值

**响应数据**:
```typescript
interface ConsumeMobileUserVO {
  userId: number;           // 用户ID
  userName: string;         // 用户名称
  phone?: string;          // 手机号
  cardNumber?: string;     // 卡号
  accountBalance: number;  // 账户余额（单位：元）
  accountStatus: number;   // 账户状态（1-正常 2-冻结 3-注销）
}
```

**Controller**: `ConsumeMobileController.quickUserInfo()`

---

#### 1.2.2 获取用户消费信息

**接口**: `GET /api/v1/consume/mobile/user/consume-info/{userId}`

**功能**: 获取指定用户的消费信息

**路径参数**:
- `userId`: number - 用户ID

**响应数据**:
```typescript
interface ConsumeMobileUserInfoVO {
  userId: number;              // 用户ID
  userName: string;            // 用户名称
  accountBalance: number;      // 账户余额（单位：元）
  allowanceBalance: number;    // 补贴余额（单位：元）
  frozenBalance: number;       // 冻结余额（单位：元）
  todayConsumeAmount: number;  // 今日消费金额（单位：元）
  todayConsumeCount: number;   // 今日消费次数
  accountStatus: number;       // 账户状态
}
```

**Controller**: `ConsumeMobileController.getUserConsumeInfo()`

---

### 1.3 餐别管理接口

**基础路径**: `/api/v1/consume/mobile/meal`

#### 1.3.1 获取有效餐别

**接口**: `GET /api/v1/consume/mobile/meal/available`

**功能**: 获取当前有效的餐别列表

**响应数据**:
```typescript
interface ConsumeMobileMealVO {
  mealId: number;        // 餐别ID
  mealName: string;      // 餐别名称（早餐/午餐/晚餐）
  startTime: string;     // 开始时间（HH:mm）
  endTime: string;       // 结束时间（HH:mm）
  isCurrent: boolean;    // 是否为当前餐别
}
```

**Controller**: `ConsumeMobileController.getAvailableMeals()`

---

### 1.4 设备管理接口

**基础路径**: `/api/v1/consume/mobile/device`

#### 1.4.1 获取设备配置

**接口**: `GET /api/v1/consume/mobile/device/config/{deviceId}`

**功能**: 获取指定消费设备的配置信息

**路径参数**:
- `deviceId`: number - 设备ID

**响应数据**:
```typescript
interface ConsumeDeviceConfigVO {
  deviceId: number;           // 设备ID
  deviceName: string;         // 设备名称
  areaId: string;             // 区域ID
  manageMode: number;         // 经营模式（1-餐别制 2-超市制 3-混合模式）
  consumeModes: string[];     // 支持的消费模式
  offlineEnabled: boolean;    // 是否支持离线消费
  // ... 其他配置
}
```

**Controller**: `ConsumeMobileController.getDeviceConfig()`

---

#### 1.4.2 获取设备今日统计

**接口**: `GET /api/v1/consume/mobile/device/today-stats/{deviceId}`

**功能**: 获取指定设备今日的消费统计数据

**路径参数**:
- `deviceId`: number - 设备ID

**响应数据**:
```typescript
interface ConsumeMobileStatsVO {
  deviceId: number;           // 设备ID
  todayConsumeCount: number;  // 今日消费次数
  todayConsumeAmount: number; // 今日消费金额（单位：元）
  todayUserCount: number;     // 今日消费用户数
  lastConsumeTime?: string;   // 最后消费时间
}
```

**Controller**: `ConsumeMobileController.getDeviceTodayStats()`

---

### 1.5 统计接口

**基础路径**: `/api/v1/consume/mobile`

#### 1.5.1 获取实时交易汇总

**接口**: `GET /api/v1/consume/mobile/transaction/summary`

**功能**: 获取指定区域的实时交易汇总

**请求参数**:
- `areaId`: string - 区域ID（可选）

**响应数据**:
```typescript
interface ConsumeMobileSummaryVO {
  areaId: string;             // 区域ID
  todayConsumeCount: number;   // 今日消费次数
  todayConsumeAmount: number;  // 今日消费金额（单位：元）
  todayUserCount: number;      // 今日消费用户数
  realtimeConsumeCount: number; // 实时消费次数（最近1小时）
  realtimeConsumeAmount: number; // 实时消费金额（最近1小时，单位：元）
}
```

**Controller**: `ConsumeMobileController.getTransactionSummary()`

---

### 1.6 离线同步接口

**基础路径**: `/api/v1/consume/mobile/sync`

#### 1.6.1 离线交易同步

**接口**: `POST /api/v1/consume/mobile/sync/offline`

**功能**: 同步移动端离线消费交易数据

**请求参数**:
```typescript
interface ConsumeOfflineSyncForm {
  deviceId: number;           // 设备ID
  transactions: OfflineTransaction[]; // 离线交易列表
}

interface OfflineTransaction {
  transactionNo: string;      // 交易流水号（设备生成）
  userId: number;            // 用户ID
  amount: number;            // 消费金额（单位：元）
  consumeTime: string;       // 消费时间（ISO格式）
  consumeMode: string;       // 消费模式
}
```

**响应数据**:
```typescript
interface ConsumeSyncResultVO {
  success: boolean;           // 是否成功
  syncedCount: number;        // 同步成功数量
  failedCount: number;       // 同步失败数量
  failedTransactions?: OfflineTransaction[]; // 失败交易列表
  message?: string;          // 提示信息
}
```

**Controller**: `ConsumeMobileController.syncOfflineTransactions()`

---

#### 1.6.2 获取同步数据

**接口**: `GET /api/v1/consume/mobile/sync/data/{deviceId}`

**功能**: 获取需要同步到移动端的数据

**路径参数**:
- `deviceId`: number - 设备ID

**响应数据**:
```typescript
interface ConsumeSyncDataVO {
  deviceId: number;           // 设备ID
  userList: UserInfo[];       // 用户列表
  productList: ProductInfo[];  // 商品列表
  mealList: MealInfo[];       // 餐别列表
  areaList: AreaInfo[];       // 区域列表
  syncTime: string;          // 同步时间
}
```

**Controller**: `ConsumeMobileController.getSyncData()`

---

### 1.7 权限验证接口

**基础路径**: `/api/v1/consume/mobile/validate`

#### 1.7.1 权限验证

**接口**: `POST /api/v1/consume/mobile/validate/permission`

**功能**: 验证用户消费权限

**请求参数**:
```typescript
interface ConsumePermissionValidateForm {
  userId: number;        // 用户ID
  areaId: string;        // 区域ID
  amount: number;        // 消费金额（单位：元）
  consumeMode?: string;  // 消费模式
}
```

**响应数据**:
```typescript
interface ConsumeValidateResultVO {
  allowed: boolean;          // 是否允许消费
  reason?: string;          // 拒绝原因
  accountBalance: number;   // 账户余额（单位：元）
  requiredAmount: number;   // 需要金额（单位：元）
}
```

**Controller**: `ConsumeMobileController.validatePermission()`

---

## 💻 二、PC端API接口（待补充）

### 2.1 消费管理接口

**基础路径**: `/api/v1/consume`

> ⚠️ **注意**: PC端Controller待创建，以下接口为规划接口

#### 2.1.1 消费记录查询

**接口**: `POST /api/v1/consume/transaction/query`

**功能**: 分页查询消费记录

**请求参数**:
```typescript
interface ConsumeTransactionQueryForm {
  userId?: number;           // 用户ID（可选）
  areaId?: string;          // 区域ID（可选）
  startDate?: string;        // 开始日期（yyyy-MM-dd）
  endDate?: string;         // 结束日期（yyyy-MM-dd）
  consumeMode?: string;     // 消费模式（可选）
  pageNum: number;          // 页码
  pageSize: number;         // 每页大小
}
```

**响应数据**:
```typescript
interface PageResult<ConsumeTransactionVO> {
  list: ConsumeTransactionVO[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

interface ConsumeTransactionVO {
  transactionNo: string;     // 交易流水号
  userId: number;           // 用户ID
  userName: string;        // 用户名称
  amount: number;          // 消费金额（单位：元）
  consumeMode: string;    // 消费模式
  consumeTime: string;     // 消费时间
  deviceId: number;        // 设备ID
  deviceName: string;       // 设备名称
  areaId: string;          // 区域ID
  areaName: string;        // 区域名称
  status: number;          // 交易状态
}
```

---

#### 2.1.2 账户管理接口

**基础路径**: `/api/v1/consume/account`

**接口列表**:
- `POST /api/v1/consume/account/add` - 创建账户
- `GET /api/v1/consume/account/{id}` - 查询账户详情
- `PUT /api/v1/consume/account/update` - 更新账户
- `DELETE /api/v1/consume/account/{id}` - 删除账户
- `POST /api/v1/consume/account/query` - 分页查询账户
- `POST /api/v1/consume/account/balance/add` - 增加余额
- `POST /api/v1/consume/account/balance/deduct` - 扣减余额
- `POST /api/v1/consume/account/balance/freeze` - 冻结余额
- `POST /api/v1/consume/account/balance/unfreeze` - 解冻余额
- `POST /api/v1/consume/account/status/enable` - 启用账户
- `POST /api/v1/consume/account/status/disable` - 禁用账户
- `POST /api/v1/consume/account/status/freeze` - 冻结账户
- `POST /api/v1/consume/account/status/unfreeze` - 解冻账户
- `POST /api/v1/consume/account/status/close` - 关闭账户

> ⚠️ **注意**: 这些接口需要创建PC端Controller实现

---

#### 2.1.3 报表统计接口

**基础路径**: `/api/v1/consume/report`

**接口列表**:
- `POST /api/v1/consume/report/generate` - 生成报表
- `POST /api/v1/consume/report/export` - 导出报表（Excel/PDF/CSV）
- `GET /api/v1/consume/report/templates` - 获取报表模板列表
- `POST /api/v1/consume/report/statistics` - 获取统计数据

> ⚠️ **注意**: 这些接口需要创建PC端Controller实现

---

## 💳 三、支付相关API接口

### 3.1 支付接口

**基础路径**: `/api/v1/consume/payment`

> ⚠️ **注意**: 支付相关Controller待创建，以下接口为规划接口

#### 3.1.1 创建支付订单

**接口**: `POST /api/v1/consume/payment/create`

**功能**: 创建支付订单（微信/支付宝/银行卡）

**请求参数**:
```typescript
interface PaymentCreateForm {
  userId: number;           // 用户ID
  amount: number;           // 支付金额（单位：元）
  paymentMethod: string;   // 支付方式（WECHAT/ALIPAY/BANK）
  paymentType: string;     // 支付类型（JSAPI/APP/H5/NATIVE）
  returnUrl?: string;     // 返回URL
  notifyUrl?: string;     // 通知URL
}
```

**响应数据**:
```typescript
interface PaymentOrderVO {
  orderNo: string;         // 订单号
  paymentUrl?: string;     // 支付URL（H5/NATIVE）
  qrCode?: string;        // 二维码（NATIVE）
  paymentParams?: object;  // 支付参数（APP/JSAPI）
}
```

---

#### 3.1.2 支付回调

**接口**: `POST /api/v1/consume/payment/callback/{paymentMethod}`

**功能**: 处理支付回调（微信/支付宝/银行卡）

**路径参数**:
- `paymentMethod`: string - 支付方式（wechat/alipay/bank）

**请求参数**: 第三方支付平台回调参数

**响应数据**: 第三方支付平台要求的响应格式

---

#### 3.1.3 查询支付订单

**接口**: `GET /api/v1/consume/payment/order/{orderNo}`

**功能**: 查询支付订单状态

**路径参数**:
- `orderNo`: string - 订单号

**响应数据**:
```typescript
interface PaymentOrderStatusVO {
  orderNo: string;         // 订单号
  status: string;          // 订单状态（PENDING/SUCCESS/FAILED）
  paymentMethod: string;   // 支付方式
  amount: number;         // 支付金额（单位：元）
  paidAmount?: number;    // 已支付金额（单位：元）
  paidTime?: string;      // 支付时间
  failureReason?: string; // 失败原因
}
```

---

#### 3.1.4 申请退款

**接口**: `POST /api/v1/consume/payment/refund`

**功能**: 申请退款

**请求参数**:
```typescript
interface RefundRequestForm {
  orderNo: string;        // 原订单号
  refundAmount: number;    // 退款金额（单位：元）
  refundReason: string;   // 退款原因
}
```

**响应数据**:
```typescript
interface RefundResultVO {
  refundNo: string;       // 退款单号
  status: string;         // 退款状态
  refundAmount: number;   // 退款金额（单位：元）
  refundTime?: string;    // 退款时间
}
```

---

## 📊 四、对账相关API接口

### 4.1 对账管理接口

**基础路径**: `/api/v1/consume/reconciliation`

#### 4.1.1 执行日终对账

**接口**: `POST /api/v1/consume/reconciliation/daily`

**功能**: 对指定日期的所有账户进行对账

**请求参数**:
- `reconcileDate`: string - 对账日期（yyyy-MM-dd，可选，默认为昨天）

**响应数据**:
```typescript
interface ReconciliationResult {
  reconcileDate: string;        // 对账日期
  status: string;               // 对账状态（SUCCESS/FAILED/PARTIAL）
  totalAccounts: number;         // 总账户数
  matchedAccounts: number;       // 匹配账户数
  unmatchedAccounts: number;     // 不匹配账户数
  differences: ReconciliationDifference[]; // 差异列表
  reportUrl?: string;           // 对账报告URL
}

interface ReconciliationDifference {
  accountId: number;            // 账户ID
  systemBalance: number;        // 系统余额（单位：元）
  calculatedBalance: number;   // 计算余额（单位：元）
  difference: number;           // 差异金额（单位：元）
  reason?: string;             // 差异原因
}
```

**Controller**: `ReconciliationController.performDailyReconciliation()`

---

#### 4.1.2 执行实时对账

**接口**: `POST /api/v1/consume/reconciliation/realtime`

**功能**: 对指定账户或所有账户进行实时余额验证

**请求参数**:
- `accountId`: number - 账户ID（可选，null表示对所有账户对账）

**响应数据**: 同日终对账

**Controller**: `ReconciliationController.performRealtimeReconciliation()`

---

#### 4.1.3 查询对账历史

**接口**: `GET /api/v1/consume/reconciliation/history`

**功能**: 查询指定日期范围内的对账历史记录

**请求参数**:
- `startDate`: string - 开始日期（yyyy-MM-dd，可选）
- `endDate`: string - 结束日期（yyyy-MM-dd，可选）
- `pageNum`: number - 页码（默认1）
- `pageSize`: number - 每页大小（默认20）

**响应数据**:
```typescript
interface PageResult<ReconciliationResult> {
  list: ReconciliationResult[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}
```

**Controller**: `ReconciliationController.queryReconciliationHistory()`

---

## 💰 五、退款/报销申请API接口

### 5.1 退款申请接口

**基础路径**: `/api/v1/consume/refund`

#### 5.1.1 提交退款申请

**接口**: `POST /api/v1/consume/refund/submit`

**功能**: 提交退款申请并启动审批流程

**请求参数**:
```typescript
interface RefundApplicationForm {
  userId: number;              // 用户ID
  paymentRecordId: number;     // 支付记录ID
  refundAmount: number;        // 退款金额（单位：元）
  refundReason: string;        // 退款原因
  attachments?: string[];      // 附件URL列表（可选）
}
```

**响应数据**:
```typescript
interface RefundApplicationEntity {
  refundNo: string;           // 退款申请编号
  userId: number;              // 用户ID
  paymentRecordId: number;     // 支付记录ID
  refundAmount: number;        // 退款金额（单位：元）
  refundReason: string;       // 退款原因
  status: string;             // 申请状态（PENDING/APPROVED/REJECTED）
  submitTime: string;         // 提交时间
  approvalTime?: string;      // 审批时间
  approvalComment?: string;  // 审批意见
}
```

**Controller**: `RefundApplicationController.submitRefundApplication()`

---

#### 5.1.2 更新退款申请状态

**接口**: `PUT /api/v1/consume/refund/{refundNo}/status`

**功能**: 由审批结果监听器调用，更新退款申请状态

**路径参数**:
- `refundNo`: string - 退款申请编号

**请求参数**:
```typescript
interface RefundStatusUpdateRequest {
  status: string;            // 状态（APPROVED/REJECTED）
  approvalComment?: string;  // 审批意见
}
```

**响应数据**: `ResponseDTO<Void>`

**Controller**: `RefundApplicationController.updateRefundStatus()`

---

### 5.2 报销申请接口

**基础路径**: `/api/v1/consume/reimbursement`

#### 5.2.1 提交报销申请

**接口**: `POST /api/v1/consume/reimbursement/submit`

**功能**: 提交报销申请并启动审批流程

**请求参数**:
```typescript
interface ReimbursementApplicationForm {
  userId: number;                    // 用户ID
  reimbursementType: string;        // 报销类型（MEAL/TRAVEL/OTHER）
  totalAmount: number;              // 报销总金额（单位：元）
  reimbursementItems: ReimbursementItem[]; // 报销明细
  attachments?: string[];           // 附件URL列表（可选）
}

interface ReimbursementItem {
  itemName: string;        // 项目名称
  amount: number;          // 金额（单位：元）
  date: string;           // 日期（yyyy-MM-dd）
  description?: string;   // 描述
}
```

**响应数据**:
```typescript
interface ReimbursementApplicationEntity {
  reimbursementNo: string;          // 报销申请编号
  userId: number;                   // 用户ID
  reimbursementType: string;       // 报销类型
  totalAmount: number;             // 报销总金额（单位：元）
  status: string;                  // 申请状态（PENDING/APPROVED/REJECTED）
  submitTime: string;              // 提交时间
  approvalTime?: string;           // 审批时间
  approvalComment?: string;        // 审批意见
}
```

**Controller**: `ReimbursementApplicationController.submitReimbursementApplication()`

---

#### 5.2.2 更新报销申请状态

**接口**: `PUT /api/v1/consume/reimbursement/{reimbursementNo}/status`

**功能**: 由审批结果监听器调用，更新报销申请状态

**路径参数**:
- `reimbursementNo`: string - 报销申请编号

**请求参数**:
```typescript
interface ReimbursementStatusUpdateRequest {
  status: string;            // 状态（APPROVED/REJECTED）
  approvalComment?: string;  // 审批意见
}
```

**响应数据**: `ResponseDTO<Void>`

**Controller**: `ReimbursementApplicationController.updateReimbursementStatus()`

---

## 📝 六、API接口规范

### 6.1 统一响应格式

所有API接口统一使用`ResponseDTO<T>`格式：

```typescript
interface ResponseDTO<T> {
  code: number;        // 业务状态码（200表示成功）
  message: string;     // 提示信息
  data: T;            // 响应数据
  timestamp: number;   // 时间戳
}
```

### 6.2 错误码规范

| 错误码范围 | 类型 | 说明 |
|-----------|------|------|
| 200 | 成功 | 操作成功 |
| 400-499 | 客户端错误 | 参数错误、未授权、禁止访问 |
| 500-599 | 服务端错误 | 服务器内部错误 |
| 4000-4999 | 消费模块错误 | 消费相关业务错误 |

### 6.3 认证授权

- **移动端接口**: 使用`@SaCheckLogin`注解，需要登录认证
- **PC端接口**: 使用`@PreAuthorize`注解，需要角色权限验证

### 6.4 参数验证

- 所有POST/PUT请求使用`@Valid`注解进行参数验证
- 使用Jakarta Validation注解（`@NotNull`, `@NotBlank`, `@Size`等）

---

## 📋 七、前端API接口文件

### 7.1 移动端API文件

**文件路径**: `smart-app/src/api/business/consume/consume-api.js`

**已实现接口**:
- ✅ 交易相关接口（transactionApi）
- ✅ 账户管理接口（accountApi）
- ✅ 交易记录接口（historyApi）
- ✅ 餐别管理接口（mealApi）
- ✅ 统计接口（statsApi）
- ✅ 离线同步接口（syncApi）
- ✅ 设备管理接口（deviceApi）
- ✅ 权限验证接口（permissionApi）
- ✅ 异常处理接口（exceptionApi）

### 7.2 PC端API文件

**文件路径**: `smart-admin-web-javascript/src/api/business/consumption/consumption-api.js`

**当前状态**: ⚠️ 仅包含Dashboard相关接口，需要完善

**待补充接口**:
- ⚠️ 消费记录查询接口
- ⚠️ 账户管理接口
- ⚠️ 报表统计接口
- ⚠️ 支付管理接口
- ⚠️ 对账管理接口
- ⚠️ 退款/报销申请接口

---

## 🎯 八、下一步行动

### 8.1 立即执行

1. 📋 创建PC端消费管理Controller
2. 📋 完善PC端API接口文件
3. 📋 创建支付相关Controller
4. 📋 创建支付相关API接口文件

### 8.2 本周完成

1. 📋 梳理其他业务模块API接口契约
2. 📋 创建完整的API接口契约文档
3. 📋 检查前端和移动端API接口文件完整性
4. 📋 补充缺失的API接口实现

---

**文档生成**: IOE-DREAM 架构委员会  
**审核状态**: 待审核  
**下一步行动**: 继续梳理其他业务模块API接口契约

