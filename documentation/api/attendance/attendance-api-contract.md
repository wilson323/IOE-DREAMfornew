# 考勤模块API接口契约文档

**生成时间**: 2025-01-30  
**文档版本**: v1.0.0  
**模块**: 考勤管理模块 (ioedream-attendance-service)  
**状态**: ✅ **已完成**

---

## 📋 文档说明

本文档定义了考勤模块的所有API接口契约，包括：
- 移动端API接口
- PC端API接口（待补充）
- 请假管理API接口
- 加班管理API接口
- 出差管理API接口
- 补卡管理API接口

**API基础路径**: `/api/attendance` 或 `/api/attendance/mobile`

---

## 📱 一、移动端API接口

### 1.1 GPS定位打卡接口

**基础路径**: `/api/attendance/mobile`

#### 1.1.1 GPS定位打卡

**接口**: `POST /api/attendance/mobile/gps-punch`

**功能**: 移动端GPS定位打卡

**请求参数**:
```typescript
interface GpsPunchRequest {
  employeeId: number;         // 员工ID
  latitude: number;          // 纬度
  longitude: number;         // 经度
  address?: string;          // 地址（可选）
  punchType: string;         // 打卡类型（CHECK_IN/CHECK_OUT）
  deviceInfo?: string;       // 设备信息（可选）
}
```

**响应数据**:
```typescript
interface PunchResult {
  success: boolean;           // 是否成功
  punchId: string;           // 打卡记录ID
  punchTime: string;         // 打卡时间（ISO格式）
  locationValid: boolean;     // 位置是否有效
  message?: string;          // 提示信息
}
```

**Controller**: `AttendanceMobileController.gpsPunch()`

---

#### 1.1.2 位置验证

**接口**: `POST /api/attendance/mobile/location/validate`

**功能**: 验证GPS位置是否有效

**请求参数**:
```typescript
interface LocationValidationRequest {
  employeeId: number;         // 员工ID
  latitude: number;          // 纬度
  longitude: number;         // 经度
  punchType: string;         // 打卡类型
}
```

**响应数据**:
```typescript
interface LocationValidationResult {
  valid: boolean;            // 位置是否有效
  distance?: number;         // 距离打卡点距离（米）
  allowedRange?: number;     // 允许范围（米）
  message?: string;          // 提示信息
}
```

**Controller**: `AttendanceMobileController.validateLocation()`

---

### 1.2 离线打卡接口

**基础路径**: `/api/attendance/mobile/offline`

#### 1.2.1 离线打卡数据缓存

**接口**: `POST /api/attendance/mobile/offline/cache`

**功能**: 缓存移动端离线打卡数据

**请求参数**:
```typescript
interface OfflinePunchRequest {
  employeeId: number;         // 员工ID
  punchDataList: OfflinePunchData[]; // 离线打卡数据列表
}

interface OfflinePunchData {
  punchTime: string;         // 打卡时间（ISO格式）
  latitude: number;          // 纬度
  longitude: number;         // 经度
  address?: string;          // 地址
  punchType: string;         // 打卡类型
  deviceInfo?: string;       // 设备信息
}
```

**响应数据**:
```typescript
interface OfflineCacheResult {
  success: boolean;           // 是否成功
  cachedCount: number;        // 缓存数量
  message?: string;          // 提示信息
}
```

**Controller**: `AttendanceMobileController.cacheOfflinePunch()`

---

#### 1.2.2 离线数据同步

**接口**: `POST /api/attendance/mobile/offline/sync`

**功能**: 同步移动端离线打卡数据到服务器

**请求参数**:
```typescript
interface OfflineSyncRequest {
  employeeId: number;         // 员工ID
  syncToken?: string;         // 同步令牌（可选）
}
```

**响应数据**:
```typescript
interface OfflineSyncResult {
  success: boolean;           // 是否成功
  syncedCount: number;        // 同步成功数量
  failedCount: number;        // 同步失败数量
  failedPunchList?: OfflinePunchData[]; // 失败打卡列表
  message?: string;          // 提示信息
}
```

**Controller**: `AttendanceMobileController.syncOfflinePunch()`

---

### 1.3 考勤记录查询接口

**基础路径**: `/api/attendance/mobile`

#### 1.3.1 查询考勤记录

**接口**: `GET /api/attendance/mobile/records`

**功能**: 查询用户考勤记录

**请求参数**:
- `employeeId`: number - 员工ID
- `startDate`: string - 开始日期（yyyy-MM-dd，可选）
- `endDate`: string - 结束日期（yyyy-MM-dd，可选）
- `pageNum`: number - 页码（默认1）
- `pageSize`: number - 每页大小（默认20）

**响应数据**:
```typescript
interface PageResult<AttendanceRecordVO> {
  list: AttendanceRecordVO[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

interface AttendanceRecordVO {
  recordId: number;           // 记录ID
  employeeId: number;        // 员工ID
  employeeName: string;      // 员工名称
  date: string;             // 日期（yyyy-MM-dd）
  checkInTime?: string;      // 上班打卡时间
  checkOutTime?: string;     // 下班打卡时间
  workDuration?: number;     // 工作时长（分钟）
  status: string;           // 考勤状态（NORMAL/LATE/EARLY_LEAVE/ABSENT）
  location?: string;        // 打卡位置
}
```

**Controller**: `AttendanceMobileController.queryAttendanceRecords()`

---

## 💻 二、PC端API接口（待补充）

### 2.1 考勤管理接口

**基础路径**: `/api/attendance`

> ⚠️ **注意**: PC端Controller待创建，以下接口为规划接口

#### 2.1.1 考勤记录查询

**接口**: `POST /api/attendance/record/query`

**功能**: 分页查询考勤记录

**请求参数**:
```typescript
interface AttendanceRecordQueryForm {
  employeeId?: number;       // 员工ID（可选）
  departmentId?: number;      // 部门ID（可选）
  startDate?: string;         // 开始日期（yyyy-MM-dd）
  endDate?: string;          // 结束日期（yyyy-MM-dd）
  status?: string;           // 考勤状态（可选）
  pageNum: number;          // 页码
  pageSize: number;         // 每页大小
}
```

**响应数据**: `PageResult<AttendanceRecordVO>`

---

#### 2.1.2 考勤统计接口

**基础路径**: `/api/attendance/statistics`

**接口列表**:
- `POST /api/attendance/statistics/daily` - 日报统计
- `POST /api/attendance/statistics/monthly` - 月报统计
- `POST /api/attendance/statistics/department` - 部门统计
- `POST /api/attendance/statistics/employee` - 员工统计

> ⚠️ **注意**: 这些接口需要创建PC端Controller实现

---

## 📝 三、请假管理API接口

### 3.1 请假申请接口

**基础路径**: `/api/attendance/leave`

#### 3.1.1 提交请假申请

**接口**: `POST /api/attendance/leave/submit`

**功能**: 提交请假申请并启动审批流程

**请求参数**:
```typescript
interface LeaveApplicationForm {
  employeeId: number;         // 员工ID
  leaveType: string;         // 请假类型（SICK/ANNUAL/COMPASSIONATE/OTHER）
  startTime: string;         // 开始时间（ISO格式）
  endTime: string;          // 结束时间（ISO格式）
  duration: number;         // 请假时长（天）
  reason: string;           // 请假原因
  attachments?: string[];    // 附件URL列表（可选）
}
```

**响应数据**:
```typescript
interface LeaveApplicationEntity {
  leaveNo: string;           // 请假申请编号
  employeeId: number;        // 员工ID
  leaveType: string;         // 请假类型
  startTime: string;         // 开始时间
  endTime: string;          // 结束时间
  duration: number;         // 请假时长（天）
  status: string;           // 申请状态（PENDING/APPROVED/REJECTED）
  submitTime: string;        // 提交时间
  approvalTime?: string;     // 审批时间
  approvalComment?: string; // 审批意见
}
```

**Controller**: `AttendanceLeaveController.submitLeaveApplication()`

---

#### 3.1.2 查询请假申请

**接口**: `POST /api/attendance/leave/query`

**功能**: 分页查询请假申请记录

**请求参数**:
```typescript
interface LeaveApplicationQueryForm {
  employeeId?: number;       // 员工ID（可选）
  leaveType?: string;        // 请假类型（可选）
  status?: string;          // 申请状态（可选）
  startDate?: string;        // 开始日期（yyyy-MM-dd）
  endDate?: string;         // 结束日期（yyyy-MM-dd）
  pageNum: number;         // 页码
  pageSize: number;        // 每页大小
}
```

**响应数据**: `PageResult<LeaveApplicationEntity>`

**Controller**: `AttendanceLeaveController.queryLeaveApplication()`

---

## ⏰ 四、加班管理API接口

### 4.1 加班申请接口

**基础路径**: `/api/attendance/overtime`

#### 4.1.1 提交加班申请

**接口**: `POST /api/attendance/overtime/submit`

**功能**: 提交加班申请并启动审批流程

**请求参数**:
```typescript
interface OvertimeApplicationForm {
  employeeId: number;         // 员工ID
  overtimeDate: string;       // 加班日期（yyyy-MM-dd）
  startTime: string;         // 开始时间（HH:mm）
  endTime: string;          // 结束时间（HH:mm）
  duration: number;         // 加班时长（小时）
  reason: string;           // 加班原因
  attachments?: string[];    // 附件URL列表（可选）
}
```

**响应数据**:
```typescript
interface OvertimeApplicationEntity {
  overtimeNo: string;         // 加班申请编号
  employeeId: number;         // 员工ID
  overtimeDate: string;       // 加班日期
  startTime: string;         // 开始时间
  endTime: string;          // 结束时间
  duration: number;         // 加班时长（小时）
  status: string;           // 申请状态（PENDING/APPROVED/REJECTED）
  submitTime: string;        // 提交时间
  approvalTime?: string;     // 审批时间
  approvalComment?: string; // 审批意见
}
```

**Controller**: `AttendanceOvertimeController.submitOvertimeApplication()`

---

## ✈️ 五、出差管理API接口

### 5.1 出差申请接口

**基础路径**: `/api/attendance/travel`

#### 5.1.1 提交出差申请

**接口**: `POST /api/attendance/travel/submit`

**功能**: 提交出差申请并启动审批流程

**请求参数**:
```typescript
interface TravelApplicationForm {
  employeeId: number;         // 员工ID
  destination: string;        // 目的地
  startTime: string;         // 开始时间（ISO格式）
  endTime: string;          // 结束时间（ISO格式）
  duration: number;         // 出差时长（天）
  reason: string;           // 出差原因
  travelExpenses?: number;   // 差旅费用（可选）
  attachments?: string[];    // 附件URL列表（可选）
}
```

**响应数据**:
```typescript
interface TravelApplicationEntity {
  travelNo: string;          // 出差申请编号
  employeeId: number;        // 员工ID
  destination: string;       // 目的地
  startTime: string;         // 开始时间
  endTime: string;          // 结束时间
  duration: number;         // 出差时长（天）
  status: string;           // 申请状态（PENDING/APPROVED/REJECTED）
  submitTime: string;        // 提交时间
  approvalTime?: string;     // 审批时间
  approvalComment?: string; // 审批意见
}
```

**Controller**: `AttendanceTravelController.submitTravelApplication()`

---

## 🔄 六、补卡管理API接口

### 6.1 补卡申请接口

**基础路径**: `/api/attendance/supplement`

#### 6.1.1 提交补卡申请

**接口**: `POST /api/attendance/supplement/submit`

**功能**: 提交补卡申请并启动审批流程

**请求参数**:
```typescript
interface SupplementApplicationForm {
  employeeId: number;         // 员工ID
  supplementDate: string;      // 补卡日期（yyyy-MM-dd）
  supplementType: string;     // 补卡类型（CHECK_IN/CHECK_OUT）
  reason: string;            // 补卡原因
  attachments?: string[];     // 附件URL列表（可选）
}
```

**响应数据**:
```typescript
interface SupplementApplicationEntity {
  supplementNo: string;       // 补卡申请编号
  employeeId: number;         // 员工ID
  supplementDate: string;      // 补卡日期
  supplementType: string;     // 补卡类型
  status: string;            // 申请状态（PENDING/APPROVED/REJECTED）
  submitTime: string;         // 提交时间
  approvalTime?: string;      // 审批时间
  approvalComment?: string;  // 审批意见
}
```

**Controller**: `AttendanceSupplementController.submitSupplementApplication()`

---

## 📋 七、班次管理API接口

### 7.1 班次查询接口

**基础路径**: `/api/attendance/shift`

#### 7.1.1 查询班次列表

**接口**: `GET /api/attendance/shift/list`

**功能**: 查询班次列表

**请求参数**:
- `departmentId`: number - 部门ID（可选）

**响应数据**:
```typescript
interface AttendanceShiftVO {
  shiftId: number;           // 班次ID
  shiftName: string;         // 班次名称
  startTime: string;         // 上班时间（HH:mm）
  endTime: string;          // 下班时间（HH:mm）
  workDuration: number;      // 工作时长（分钟）
  departmentId?: number;     // 部门ID（可选）
}
```

**Controller**: `AttendanceShiftController.getShiftList()`

---

## 📋 八、API接口规范

### 8.1 统一响应格式

所有API接口统一使用`ResponseDTO<T>`格式：

```typescript
interface ResponseDTO<T> {
  code: number;        // 业务状态码（200表示成功）
  message: string;     // 提示信息
  data: T;            // 响应数据
  timestamp: number;   // 时间戳
}
```

### 8.2 错误码规范

| 错误码范围 | 类型 | 说明 |
|-----------|------|------|
| 200 | 成功 | 操作成功 |
| 400-499 | 客户端错误 | 参数错误、未授权、禁止访问 |
| 500-599 | 服务端错误 | 服务器内部错误 |
| 6000-6999 | 考勤模块错误 | 考勤相关业务错误 |

### 8.3 认证授权

- **移动端接口**: 使用`@SaCheckLogin`和`@SaCheckPermission`注解，需要登录认证和权限验证
- **PC端接口**: 使用`@PreAuthorize`注解，需要角色权限验证

### 8.4 参数验证

- 所有POST/PUT请求使用`@Valid`注解进行参数验证
- 使用Jakarta Validation注解（`@NotNull`, `@NotBlank`, `@Size`等）

---

## 📋 九、前端API接口文件

### 9.1 移动端API文件

**文件路径**: `smart-app/src/api/business/attendance/attendance-api.js`

**待实现接口**:
- ⚠️ GPS定位打卡接口
- ⚠️ 离线打卡接口
- ⚠️ 考勤记录查询接口

### 9.2 PC端API文件

**文件路径**: `smart-admin-web-javascript/src/api/business/attendance/attendance-api.js`

**当前状态**: ⚠️ 需要创建

**待补充接口**:
- ⚠️ 考勤记录查询接口
- ⚠️ 考勤统计接口
- ⚠️ 请假管理接口
- ⚠️ 加班管理接口
- ⚠️ 出差管理接口
- ⚠️ 补卡管理接口
- ⚠️ 班次管理接口

---

## 🎯 十、下一步行动

### 10.1 立即执行

1. 📋 创建PC端考勤管理Controller
2. 📋 创建PC端API接口文件
3. 📋 完善移动端API接口文件

### 10.2 本周完成

1. 📋 梳理其他业务模块API接口契约
2. 📋 创建完整的API接口契约文档
3. 📋 检查前端和移动端API接口文件完整性
4. 📋 补充缺失的API接口实现

---

**文档生成**: IOE-DREAM 架构委员会  
**审核状态**: 待审核  
**下一步行动**: 继续梳理访客和视频模块API接口契约

