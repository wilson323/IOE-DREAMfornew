# 访客模块API接口契约文档

**生成时间**: 2025-01-30  
**文档版本**: v1.1.0 - 混合验证模式  
**模块**: 访客管理模块 (ioedream-visitor-service)  
**设备交互模式**: Mode 4 - 混合验证  
**状态**: ✅ **已完成**

---

## 📋 文档说明

本文档定义了访客模块的所有API接口契约，包括：
- 移动端API接口
- PC端API接口（待补充）
- 预约管理API接口
- 签到签退API接口
- 访客查询API接口

**API基础路径**: `/api/v1/visitor` 或 `/api/v1/mobile/visitor`

---

## ⭐ 设备交互模式说明

### Mode 4: 混合验证模式

**核心理念**: 临时访客中心验证，常客边缘验证

**交互流程**:
```
【临时访客】中心实时验证
  预约申请 → 审批通过 → 生成访客码
  到访时扫码 → 服务器验证 → 现场采集人脸
  服务器生成临时模板 → 下发设备 → 设置有效期
  访客通行 → 实时上报 → 服务器记录轨迹
  访问结束 → 自动失效 → 从设备删除模板

【常客】边缘验证
  长期合作伙伴 → 申请常客权限 → 审批通过
  采集生物特征 → 下发所有授权设备
  日常通行 → 设备端验证 → 批量上传记录
  权限到期 → 自动失效 → 从设备删除
```

**API接口说明**:
- ✅ 临时访客必须通过中心API实时验证
- ✅ 常客支持边缘验证，通过设备批量上传记录
- ✅ 支持临时模板自动下发和失效
- ✅ 完整记录访客轨迹

**详细文档**: 参考 [访客业务模块文档](../../业务模块/05-访客管理模块/00-访客微服务总体设计文档.md#-混合验证模式mode-4)

---

## 📱 一、移动端API接口

### 1.1 预约管理接口

**基础路径**: `/api/v1/mobile/visitor`

#### 1.1.1 创建预约

**接口**: `POST /api/v1/mobile/visitor/appointment`

**功能**: 创建新的访客预约

**请求参数**:
```typescript
interface VisitorMobileForm {
  visitorName: string;        // 访客姓名
  visitorPhone: string;        // 访客手机号
  visitorIdCard?: string;     // 访客身份证号（可选）
  hostUserId: number;         // 接待人用户ID
  hostName: string;           // 接待人姓名
  visitPurpose: string;        // 访问目的
  visitDate: string;          // 访问日期（yyyy-MM-dd）
  visitStartTime: string;     // 访问开始时间（HH:mm）
  visitEndTime: string;       // 访问结束时间（HH:mm）
  areaIds?: string[];         // 访问区域ID列表（可选）
  deviceIds?: number[];       // 访问设备ID列表（可选）
  remark?: string;            // 备注（可选）
  attachments?: string[];     // 附件URL列表（可选）
}
```

**响应数据**:
```typescript
interface AppointmentResult {
  appointmentId: number;       // 预约ID
  appointmentNo: string;      // 预约编号
  qrCode?: string;            // 二维码（可选）
  status: string;             // 预约状态（PENDING/APPROVED/REJECTED）
  message?: string;           // 提示信息
}
```

**Controller**: `VisitorMobileController.createAppointment()`

---

#### 1.1.2 获取预约详情

**接口**: `GET /api/v1/mobile/visitor/appointment/{appointmentId}`

**功能**: 根据预约ID获取预约详细信息

**路径参数**:
- `appointmentId`: number - 预约ID

**响应数据**:
```typescript
interface VisitorAppointmentDetailVO {
  appointmentId: number;       // 预约ID
  appointmentNo: string;       // 预约编号
  visitorName: string;        // 访客姓名
  visitorPhone: string;        // 访客手机号
  visitorIdCard?: string;     // 访客身份证号
  hostUserId: number;         // 接待人用户ID
  hostName: string;           // 接待人姓名
  visitPurpose: string;        // 访问目的
  visitDate: string;          // 访问日期
  visitStartTime: string;     // 访问开始时间
  visitEndTime: string;       // 访问结束时间
  areaIds: string[];          // 访问区域ID列表
  deviceIds: number[];        // 访问设备ID列表
  status: string;             // 预约状态
  checkInTime?: string;       // 签到时间
  checkOutTime?: string;      // 签退时间
  qrCode?: string;            // 二维码
  remark?: string;            // 备注
  submitTime: string;         // 提交时间
  approvalTime?: string;      // 审批时间
  approvalComment?: string;  // 审批意见
}
```

**Controller**: `VisitorMobileController.getAppointmentDetail()`

---

#### 1.1.3 查询我的预约

**接口**: `GET /api/v1/mobile/visitor/my-appointments`

**功能**: 查询当前用户的预约记录

**请求参数**:
- `userId`: number - 用户ID
- `status`: number - 状态（可选，1-待审批 2-已通过 3-已拒绝 4-已完成）

**响应数据**:
```typescript
interface PageResult<VisitorAppointmentDetailVO> {
  list: VisitorAppointmentDetailVO[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}
```

**Controller**: `VisitorMobileController.queryMyAppointments()`

---

#### 1.1.4 更新预约状态

**接口**: `PUT /api/v1/mobile/visitor/appointment/{appointmentId}/status`

**功能**: 由审批结果监听器调用，更新预约状态

**路径参数**:
- `appointmentId`: number - 预约ID

**请求参数**:
```typescript
interface AppointmentStatusUpdateRequest {
  status: string;            // 状态（APPROVED/REJECTED）
  approvalComment?: string;  // 审批意见
}
```

**响应数据**: `ResponseDTO<Void>`

**Controller**: `VisitorMobileController.updateAppointmentStatus()`

---

### 1.2 签到签退接口

**基础路径**: `/api/v1/mobile/visitor`

#### 1.2.1 获取签到状态

**接口**: `GET /api/v1/mobile/visitor/checkin/status/{appointmentId}`

**功能**: 根据预约ID获取签到状态

**路径参数**:
- `appointmentId`: number - 预约ID

**响应数据**: `VisitorAppointmentDetailVO`（包含签到状态信息）

**Controller**: `VisitorMobileController.getCheckInStatus()`

---

#### 1.2.2 访客签到

**接口**: `POST /api/v1/mobile/visitor/checkin/{appointmentId}`

**功能**: 访客到达后进行签到

**路径参数**:
- `appointmentId`: number - 预约ID

**请求参数**:
```typescript
interface CheckInRequest {
  latitude?: number;         // 纬度（可选）
  longitude?: number;        // 经度（可选）
  address?: string;         // 地址（可选）
  photoUrl?: string;        // 照片URL（可选）
}
```

**响应数据**: `ResponseDTO<Void>`

**Controller**: `VisitorMobileController.checkIn()`

---

#### 1.2.3 访客签退

**接口**: `POST /api/v1/mobile/visitor/checkout/{appointmentId}`

**功能**: 访客离开后进行签退

**路径参数**:
- `appointmentId`: number - 预约ID

**请求参数**:
```typescript
interface CheckOutRequest {
  latitude?: number;         // 纬度（可选）
  longitude?: number;        // 经度（可选）
  address?: string;         // 地址（可选）
  photoUrl?: string;        // 照片URL（可选）
}
```

**响应数据**: `ResponseDTO<Void>`

**Controller**: `VisitorMobileController.checkOut()`

---

### 1.3 访客查询接口

**基础路径**: `/api/v1/mobile/visitor`

#### 1.3.1 查询访客记录

**接口**: `GET /api/v1/mobile/visitor/records`

**功能**: 根据手机号查询访客记录列表

**请求参数**:
- `phone`: string - 手机号
- `pageNum`: number - 页码（默认1）
- `pageSize`: number - 每页大小（默认20）

**响应数据**: `PageResult<VisitorRecordVO>`

**Controller**: `VisitorMobileController.queryVisitorRecords()`

---

### 1.4 统计接口

**基础路径**: `/api/v1/mobile/visitor`

#### 1.4.1 获取统计数据

**接口**: `GET /api/v1/mobile/visitor/statistics`

**功能**: 获取访客统计数据

**响应数据**:
```typescript
interface VisitorStatisticsVO {
  todayVisitorCount: number;      // 今日访客数
  todayAppointmentCount: number; // 今日预约数
  todayCheckInCount: number;      // 今日签到数
  todayCheckOutCount: number;     // 今日签退数
  pendingAppointmentCount: number; // 待审批预约数
  monthlyVisitorCount: number;     // 本月访客数
  monthlyAppointmentCount: number; // 本月预约数
}
```

**Controller**: `VisitorMobileController.getStatistics()`

---

## 💻 二、PC端API接口（待补充）

### 2.1 访客管理接口

**基础路径**: `/api/v1/visitor`

> ⚠️ **注意**: PC端Controller待创建，以下接口为规划接口

#### 2.1.1 访客记录查询

**接口**: `POST /api/v1/visitor/record/query`

**功能**: 分页查询访客记录

**请求参数**:
```typescript
interface VisitorRecordQueryForm {
  visitorName?: string;       // 访客姓名（可选）
  visitorPhone?: string;      // 访客手机号（可选）
  hostUserId?: number;        // 接待人用户ID（可选）
  startDate?: string;         // 开始日期（yyyy-MM-dd）
  endDate?: string;          // 结束日期（yyyy-MM-dd）
  status?: string;           // 状态（可选）
  pageNum: number;          // 页码
  pageSize: number;         // 每页大小
}
```

**响应数据**: `PageResult<VisitorRecordVO>`

---

#### 2.1.2 预约管理接口

**基础路径**: `/api/v1/visitor/appointment`

**接口列表**:
- `POST /api/v1/visitor/appointment/query` - 分页查询预约
- `GET /api/v1/visitor/appointment/{id}` - 查询预约详情
- `POST /api/v1/visitor/appointment/add` - 添加预约
- `PUT /api/v1/visitor/appointment/update` - 更新预约
- `DELETE /api/v1/visitor/appointment/{id}` - 删除预约
- `POST /api/v1/visitor/appointment/approve` - 审批预约
- `POST /api/v1/visitor/appointment/reject` - 拒绝预约

> ⚠️ **注意**: 这些接口需要创建PC端Controller实现

---

#### 2.1.3 访客统计接口

**基础路径**: `/api/v1/visitor/statistics`

**接口列表**:
- `POST /api/v1/visitor/statistics/daily` - 日报统计
- `POST /api/v1/visitor/statistics/monthly` - 月报统计
- `POST /api/v1/visitor/statistics/department` - 部门统计
- `POST /api/v1/visitor/statistics/export` - 导出统计报表

> ⚠️ **注意**: 这些接口需要创建PC端Controller实现

---

## 📋 三、API接口规范

### 3.1 统一响应格式

所有API接口统一使用`ResponseDTO<T>`格式：

```typescript
interface ResponseDTO<T> {
  code: number;        // 业务状态码（200表示成功）
  message: string;     // 提示信息
  data: T;            // 响应数据
  timestamp: number;   // 时间戳
}
```

### 3.2 错误码规范

| 错误码范围 | 类型 | 说明 |
|-----------|------|------|
| 200 | 成功 | 操作成功 |
| 400-499 | 客户端错误 | 参数错误、未授权、禁止访问 |
| 500-599 | 服务端错误 | 服务器内部错误 |
| 7000-7999 | 访客模块错误 | 访客相关业务错误 |

### 3.3 认证授权

- **移动端接口**: 使用`@SaCheckLogin`注解，需要登录认证
- **PC端接口**: 使用`@PreAuthorize`注解，需要角色权限验证

### 3.4 参数验证

- 所有POST/PUT请求使用`@Valid`注解进行参数验证
- 使用Jakarta Validation注解（`@NotNull`, `@NotBlank`, `@Size`等）

---

## 📋 四、前端API接口文件

### 4.1 移动端API文件

**文件路径**: `smart-app/src/api/business/visitor/visitor-api.js`

**当前状态**: ✅ 已存在，较完整

**已实现接口**:
- ✅ 预约管理接口
- ✅ 签到签退接口
- ✅ 访客查询接口

### 4.2 PC端API文件

**文件路径**: `smart-admin-web-javascript/src/api/business/visitor/visitor-api.js`

**当前状态**: ⚠️ 需要检查和完善

**待补充接口**:
- ⚠️ 访客记录查询接口
- ⚠️ 预约管理接口
- ⚠️ 访客统计接口

---

## 🎯 五、下一步行动

### 5.1 立即执行

1. 📋 创建PC端访客管理Controller
2. 📋 完善PC端API接口文件
3. 📋 检查移动端API接口文件完整性

### 5.2 本周完成

1. 📋 梳理视频模块API接口契约
2. 📋 创建完整的API接口契约文档索引
3. 📋 检查前端和移动端API接口文件完整性
4. 📋 补充缺失的API接口实现

---

**文档生成**: IOE-DREAM 架构委员会  
**审核状态**: 待审核  
**下一步行动**: 梳理视频模块API接口契约

