# 考勤管理模块 - 移动端前端设计

> **版本**: v2.0.0  
> **更新日期**: 2025-01-30  
> **适用范围**: IOE-DREAM智慧园区一卡通管理平台 - 考勤微服务移动端

---

## 1. 移动端设计概述

### 1.1 设计定位

基于现有移动端代码分析（`smart-app/src/pages/attendance/`），考勤管理模块的移动端设计遵循 IOE-DREAM 智慧园区一卡通管理平台的统一规范，面向企业员工提供便捷的移动考勤服务。

### 1.2 设计原则

| 原则 | 说明 | 实现方式 |
|------|------|---------|
| **场景驱动** | 移动端聚焦员工高频使用场景 | 打卡、请假、查询为核心功能 |
| **位置优先** | GPS定位打卡为核心功能 | 多种定位方式+位置验证 |
| **离线支持** | 支持离线打卡数据缓存和同步 | 本地存储+自动同步机制 |
| **简洁高效** | 一键打卡，操作流程最短化 | 首页直达+快捷操作 |
| **安全可靠** | 防作弊机制+数据安全 | 位置验证+照片采集 |

### 1.3 用户角色

| 角色 | 功能范围 | 使用场景 |
|------|---------|---------|
| 普通员工 | 打卡、请假、加班、补卡、出差申请 | 日常考勤操作 |
| 部门主管 | 员工功能+审批提醒（跳转PC） | 移动审批入口 |
| 外勤人员 | 员工功能+外勤打卡 | 外出拜访时打卡 |

## 2. 技术架构

### 2.1 技术栈

| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|---------|
| Vue 3.x | 3.4+ | 核心框架 | Composition API，性能优异 |
| Uni-app | 3.0+ | 跨平台开发 | 一套代码多端运行 |
| Pinia | 2.x | 状态管理 | Vue3官方推荐，轻量高效 |
| uni-ui | 1.5+ | UI组件库 | 跨平台兼容，组件丰富 |
| dayjs | 1.11+ | 日期处理 | 轻量级，API友好 |

### 2.2 架构分层

```
┌─────────────────────────────────────────────┐
│                   视图层 (Views)              │
│  pages/attendance/*.vue                      │
├─────────────────────────────────────────────┤
│                 组件层 (Components)           │
│  components/attendance/*.vue                 │
├─────────────────────────────────────────────┤
│                状态管理层 (Store)             │
│  store/modules/attendance.js                 │
├─────────────────────────────────────────────┤
│                 服务层 (API)                  │
│  api/business/attendance/attendance-api.js   │
├─────────────────────────────────────────────┤
│                工具层 (Utils)                 │
│  utils/location.js, utils/offline.js         │
└─────────────────────────────────────────────┘
```

## 3. 移动端功能范围

### 3.1 移动端实现功能（基于业务场景）

| 功能模块 | 优先级 | 业务场景说明 | 技术要点 |
|---------|-------|-------------|---------|
| 考勤首页 | P0 | 今日考勤状态、快速打卡入口 | 状态实时刷新 |
| GPS打卡 | P0 | 位置验证+一键打卡 | 高精度定位+围栏验证 |
| 打卡记录 | P0 | 历史打卡记录查询 | 分页加载+日历视图 |
| 请假申请 | P0 | 移动端提交请假 | 表单验证+附件上传 |
| 加班申请 | P1 | 移动端提交加班 | 时间选择器 |
| 补卡申请 | P1 | 漏打卡补卡申请 | 审批流程对接 |
| 出差申请 | P1 | 移动端提交出差 | 多日期选择 |
| 我的排班 | P1 | 查看个人排班信息 | 日历组件+班次展示 |
| 考勤统计 | P2 | 个人月度统计 | 图表展示 |
| 离线打卡 | P0 | 网络断开时缓存打卡 | IndexedDB+自动同步 |

### 3.2 仅PC端实现功能

| 功能模块 | 原因说明 | 替代方案 |
|---------|---------|---------|
| 排班管理 | 需要复杂排班配置，适合PC操作 | 移动端仅查看 |
| 考勤规则配置 | 管理类功能，操作频次低 | 无 |
| 部门统计报表 | 图表展示，PC体验更佳 | 移动端简化版 |
| 审批管理 | 复杂审批流程，PC效率高 | 移动端提醒+跳转 |
| 考勤设备管理 | 管理员专用功能 | 无 |
| 批量导入导出 | 文件操作，PC更便捷 | 无 |

## 4. 核心页面设计

### 4.1 考勤首页 (`attendance/index.vue`)

**页面结构**:
```
┌─────────────────────────────────┐
│      今日考勤状态卡片            │
│  ┌─────────────────────────┐   │
│  │  今日考勤  2025年01月30日 │   │
│  │  上班打卡    下班打卡     │   │
│  │  09:00      未打卡       │   │
│  └─────────────────────────┘   │
├─────────────────────────────────┤
│  快速打卡区域                    │
│  ┌────────┐  ┌────────┐        │
│  │上班打卡 │  │下班打卡 │        │
│  └────────┘  └────────┘        │
│  [✓ 位置验证通过]               │
├─────────────────────────────────┤
│  考勤功能菜单 (4x2网格)          │
│  ┌──┐┌──┐┌──┐┌──┐             │
│  │记录││请假││加班││统计│             │
│  └──┘└──┘└──┘└──┘             │
│  ┌──┐┌──┐┌──┐┌──┐             │
│  │排班││补卡││出差││汇总│             │
│  └──┘└──┘└──┘└──┘             │
├─────────────────────────────────┤
│  最近打卡记录                    │
│  ┌─────────────────────────┐   │
│  │ 01-30 上班  09:00       │   │
│  │ 01-29 下班  18:30       │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

**核心功能详述**:

| 功能区域 | 功能点 | 交互说明 |
|---------|-------|---------|
| 状态卡片 | 今日打卡状态展示 | 实时刷新，显示上下班打卡时间 |
| 打卡按钮 | 上班/下班一键打卡 | 位置验证通过后可点击，已打卡则禁用 |
| 位置状态 | 位置验证结果 | 绿色通过/红色不在范围 |
| 功能菜单 | 8个功能入口 | 点击跳转对应页面 |
| 最近记录 | 近7天打卡记录 | 点击查看详情 |

### 4.2 打卡记录页面

**页面结构**:
```
┌─────────────────────────────────┐
│         导航栏：打卡记录         │
├─────────────────────────────────┤
│  日期选择器：[2025-01] ▼        │
├─────────────────────────────────┤
│  月度统计摘要                    │
│  正常:22天 迟到:1天 早退:0天     │
├─────────────────────────────────┤
│  记录列表                        │
│  ┌─────────────────────────┐   │
│  │ 01-30 周四               │   │
│  │ 上班 09:00  下班 18:30   │   │
│  │ [正常]                   │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │ 01-29 周三               │   │
│  │ 上班 09:15  下班 18:00   │   │
│  │ [迟到15分钟]             │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

### 4.3 请假申请页面

**表单字段设计**:

| 字段 | 类型 | 必填 | 验证规则 |
|------|------|------|---------|
| 请假类型 | 下拉选择 | ✅ | 事假/病假/年假/婚假/产假等 |
| 开始时间 | 日期时间选择器 | ✅ | 不早于当前时间 |
| 结束时间 | 日期时间选择器 | ✅ | 不早于开始时间 |
| 请假时长 | 自动计算 | - | 根据开始结束时间计算 |
| 请假原因 | 文本域 | ✅ | 10-500字符 |
| 附件 | 文件上传 | 病假必填 | 图片/PDF，最大10MB |

### 4.4 我的排班页面

**日历视图设计**:
```
┌─────────────────────────────────┐
│         导航栏：我的排班         │
├─────────────────────────────────┤
│  周选择器：< 本周 >             │
├─────────────────────────────────┤
│  日历视图                        │
│  ┌─────────────────────────┐   │
│  │ 一  二  三  四  五  六  日 │   │
│  │ 27  28  29  30  31  1   2  │   │
│  │ 早  早  早  早  早  休  休 │   │
│  └─────────────────────────┘   │
├─────────────────────────────────┤
│  今日排班详情                    │
│  班次：早班                      │
│  上班时间：09:00                │
│  下班时间：18:00                │
│  午休时间：12:00 - 13:00        │
│  弹性时间：±30分钟              │
└─────────────────────────────────┘
```

## 5. API接口设计

### 5.1 接口规范

```
基础路径: /api/attendance/mobile/
认证方式: Bearer Token (Sa-Token)
数据格式: application/json
字符编码: UTF-8
```

### 5.2 核心接口列表

| 模块 | 接口 | 方法 | 说明 | 请求参数 |
|------|------|------|------|---------|
| **GPS打卡** | `/gps-punch` | POST | GPS定位打卡 | employeeId, latitude, longitude, photoUrl, address |
| | `/punch/photo/upload` | POST | 上传打卡照片 | FormData(file) |
| | `/punch/status/today` | GET | 今日打卡状态 | employeeId |
| | `/punch/records` | GET | 打卡记录列表 | employeeId, startDate, endDate, pageNum, pageSize |
| **位置验证** | `/location/validate` | POST | 验证GPS位置 | employeeId, latitude, longitude |
| | `/locations` | GET | 考勤地点列表 | employeeId |
| | `/location/nearest` | GET | 最近考勤地点 | employeeId |
| **离线打卡** | `/offline/cache` | POST | 缓存离线数据 | employeeId, punchDataList |
| | `/offline/sync/{id}` | POST | 同步离线数据 | employeeId |
| | `/offline/status` | GET | 离线数据状态 | employeeId |
| | `/offline/clear/{id}` | POST | 清除离线缓存 | employeeId |
| **请假管理** | `/leave/apply` | POST | 申请请假 | employeeId, leaveType, startTime, endTime, reason |
| | `/leave/records` | GET | 请假记录 | employeeId, status, pageNum, pageSize |
| | `/leave/cancel/{id}` | POST | 取消请假 | employeeId |
| **加班管理** | `/overtime/apply` | POST | 申请加班 | employeeId, overtimeDate, startTime, endTime, reason |
| | `/overtime/records` | GET | 加班记录 | employeeId, status, pageNum, pageSize |
| **补卡管理** | `/supplement/apply` | POST | 申请补卡 | employeeId, supplementType, supplementTime, reason |
| | `/supplement/records` | GET | 补卡记录 | employeeId, status, pageNum, pageSize |
| **出差管理** | `/travel/apply` | POST | 申请出差 | employeeId, travelType, startTime, endTime, destination, reason |
| | `/travel/records` | GET | 出差记录 | employeeId, status, pageNum, pageSize |
| **排班查询** | `/schedule/personal` | GET | 个人排班 | employeeId, startDate, endDate |
| | `/schedule/today` | GET | 今日排班 | employeeId |
| | `/schedule/week` | GET | 本周排班 | employeeId |
| **统计分析** | `/statistics/personal` | GET | 个人统计 | employeeId, startDate, endDate |
| | `/statistics/monthly` | GET | 月度汇总 | employeeId, month |
| | `/statistics/abnormal` | GET | 异常记录 | employeeId, startDate, endDate |

### 5.3 接口响应格式

```json
{
  "code": 200,
  "success": true,
  "message": "操作成功",
  "data": {
    "morningPunch": "09:00:00",
    "eveningPunch": null,
    "locationValid": true
  },
  "timestamp": 1706601600000
}
```

### 5.4 错误码定义

| 错误码 | 说明 | 处理方式 |
|-------|------|---------|
| 4001 | 位置验证失败 | 提示用户移动到考勤范围内 |
| 4002 | 重复打卡 | 提示已打卡时间 |
| 4003 | 非工作时间 | 提示当前不在排班时间 |
| 4004 | 请假时间冲突 | 提示冲突的请假记录 |
| 4005 | 余额不足 | 提示年假/调休余额 |
| 5001 | 定位服务异常 | 提示开启定位权限 |

## 6. 状态管理

### 6.1 Pinia Store 结构

```javascript
// store/modules/attendance.js
import { defineStore } from 'pinia'
import attendanceApi from '@/api/business/attendance/attendance-api'

export const useAttendanceStore = defineStore('attendance', {
  state: () => ({
    // 今日打卡状态
    todayStatus: {
      morningPunch: null,      // 上班打卡时间
      eveningPunch: null,      // 下班打卡时间
      isLate: false,           // 是否迟到
      isEarly: false           // 是否早退
    },
    
    // 当前位置信息
    currentLocation: {
      latitude: 0,
      longitude: 0,
      address: '',
      accuracy: 0,
      isValid: false,          // 是否在考勤范围内
      nearestLocation: null    // 最近的考勤地点
    },
    
    // 打卡记录
    punchRecords: [],
    
    // 排班信息
    schedule: {
      today: null,
      week: []
    },
    
    // 统计数据
    statistics: {
      normalDays: 0,
      lateDays: 0,
      earlyDays: 0,
      absentDays: 0,
      leaveDays: 0,
      overtimeHours: 0
    },
    
    // 离线缓存
    offlineData: [],
    
    // 加载状态
    loading: {
      punch: false,
      records: false,
      schedule: false
    }
  }),
  
  getters: {
    // 是否可以打卡
    canPunch: (state) => state.currentLocation.isValid,
    
    // 是否有离线数据待同步
    hasOfflineData: (state) => state.offlineData.length > 0,
    
    // 今日是否已完成打卡
    todayCompleted: (state) => 
      state.todayStatus.morningPunch && state.todayStatus.eveningPunch
  },
  
  actions: {
    // 加载今日状态
    async loadTodayStatus(employeeId) {
      try {
        const res = await attendanceApi.punchApi.getTodayPunchStatus(employeeId)
        if (res.success) {
          this.todayStatus = res.data
        }
      } catch (error) {
        console.error('加载今日状态失败:', error)
      }
    },
    
    // GPS打卡
    async gpsPunch(data) {
      this.loading.punch = true
      try {
        const res = await attendanceApi.punchApi.gpsPunch(data)
        if (res.success) {
          await this.loadTodayStatus(data.employeeId)
          return { success: true, message: '打卡成功' }
        }
        return { success: false, message: res.message }
      } catch (error) {
        // 网络异常，缓存到离线数据
        this.cacheOfflinePunch(data)
        return { success: true, message: '已缓存，网络恢复后自动同步' }
      } finally {
        this.loading.punch = false
      }
    },
    
    // 验证位置
    async validateLocation(data) {
      try {
        const res = await attendanceApi.locationApi.validateLocation(data)
        if (res.success) {
          this.currentLocation.isValid = res.data.valid
          this.currentLocation.nearestLocation = res.data.nearestLocation
        }
        return res.data
      } catch (error) {
        console.error('位置验证失败:', error)
        return { valid: false }
      }
    },
    
    // 缓存离线打卡
    cacheOfflinePunch(data) {
      const offlineRecord = {
        ...data,
        id: Date.now(),
        syncStatus: 'PENDING',
        createTime: new Date().toISOString()
      }
      this.offlineData.push(offlineRecord)
      // 持久化到本地存储
      uni.setStorageSync('OFFLINE_PUNCH_DATA', this.offlineData)
    },
    
    // 同步离线数据
    async syncOfflineData(employeeId) {
      if (this.offlineData.length === 0) return
      
      try {
        const res = await attendanceApi.offlineApi.syncOfflinePunches(employeeId)
        if (res.success) {
          this.offlineData = []
          uni.removeStorageSync('OFFLINE_PUNCH_DATA')
        }
      } catch (error) {
        console.error('同步离线数据失败:', error)
      }
    }
  }
})
```

## 7. 离线打卡机制

### 7.1 离线数据存储

```javascript
// 离线数据结构
const offlinePunchData = {
  id: 1706601600000,          // 本地唯一ID
  employeeId: 1001,           // 员工ID
  punchType: 'IN',            // IN-上班 OUT-下班
  punchTime: '2025-01-30T09:00:00',  // 打卡时间
  latitude: 39.9042,          // 纬度
  longitude: 116.4074,        // 经度
  accuracy: 10,               // 定位精度(米)
  address: '北京市朝阳区xxx',  // 地址信息
  photoUrl: '',               // 照片URL(可选)
  deviceInfo: {               // 设备信息
    model: 'iPhone 14',
    system: 'iOS 17.0'
  },
  syncStatus: 'PENDING',      // PENDING-待同步 SYNCING-同步中 SUCCESS-成功 FAILED-失败
  syncAttempts: 0,            // 同步尝试次数
  lastSyncTime: null,         // 最后同步时间
  createTime: '2025-01-30T09:00:00'  // 创建时间
}
```

### 7.2 离线同步流程

```
┌─────────────────────────────────────────────────┐
│                   打卡操作                       │
└────────────────────┬────────────────────────────┘
                     ▼
┌─────────────────────────────────────────────────┐
│               检测网络状态                       │
└────────────────────┬────────────────────────────┘
                     ▼
         ┌──────────┴──────────┐
         ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│   网络正常       │    │   网络异常       │
│   直接上传       │    │   本地缓存       │
└────────┬────────┘    └────────┬────────┘
         ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│   上传成功       │    │   存储到         │
│   更新状态       │    │   IndexedDB     │
└─────────────────┘    └────────┬────────┘
                               ▼
                      ┌─────────────────┐
                      │   监听网络恢复   │
                      └────────┬────────┘
                               ▼
                      ┌─────────────────┐
                      │   自动触发同步   │
                      └────────┬────────┘
                               ▼
                      ┌─────────────────┐
                      │   批量上传数据   │
                      └────────┬────────┘
                               ▼
                      ┌─────────────────┐
                      │   更新同步状态   │
                      └─────────────────┘
```

### 7.3 同步策略

| 策略 | 说明 | 配置 |
|------|------|------|
| 自动同步 | 网络恢复后自动同步 | 默认开启 |
| 重试机制 | 失败后指数退避重试 | 最多3次，间隔1s/2s/4s |
| 批量同步 | 多条数据批量上传 | 每批最多20条 |
| 冲突处理 | 服务端时间为准 | 本地记录标记为已同步 |
| 数据清理 | 同步成功后清理 | 保留7天历史 |

## 8. 交互规范

### 8.1 打卡操作

| 场景 | 交互设计 | 反馈方式 |
|------|---------|---------|
| 位置验证中 | 按钮显示Loading | 文字"定位中..." |
| 位置验证通过 | 按钮可点击，绿色状态 | 显示"位置验证通过" |
| 位置验证失败 | 按钮禁用，红色状态 | 显示"不在考勤范围内" |
| 打卡中 | 全屏Loading，禁止返回 | 文字"打卡中..." |
| 打卡成功 | Toast提示 | 显示"打卡成功 09:00" |
| 打卡失败 | Modal弹窗 | 显示失败原因+重试按钮 |
| 已打卡 | 按钮禁用 | 显示已打卡时间 |

### 8.2 加载状态

| 场景 | 加载方式 | 说明 |
|------|---------|------|
| 列表首次加载 | 骨架屏 | 占位显示，避免布局抖动 |
| 列表刷新 | 下拉刷新动画 | 顶部显示刷新状态 |
| 列表加载更多 | 底部Loading | 滚动到底部触发 |
| 按钮操作 | 按钮禁用+Loading | 防止重复提交 |
| 页面切换 | 统一过渡动画 | 300ms滑动动画 |

### 8.3 错误处理

| 错误类型 | 处理方式 | 用户提示 |
|---------|---------|---------|
| 位置获取失败 | 引导开启权限 | Modal提示开启定位权限 |
| 网络错误 | 自动缓存离线 | Toast提示"已缓存，稍后自动同步" |
| 业务错误 | Modal弹窗 | 显示具体错误原因 |
| 表单验证失败 | 输入框标红 | 显示具体验证错误 |
| 服务器错误 | 重试按钮 | Modal提示"服务异常，请稍后重试" |

## 9. 样式规范

### 9.1 颜色体系

```scss
// ==================== 考勤模块专用颜色 ====================

// 主题渐变色
$attendance-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$attendance-gradient-light: linear-gradient(135deg, #7c8cf5 0%, #8f6db8 100%);

// 打卡按钮色
$punch-in-color: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
$punch-in-hover: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
$punch-out-color: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
$punch-out-hover: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);

// 考勤状态色
$status-normal: #4ade80;      // 正常
$status-late: #fbbf24;        // 迟到
$status-early: #fb923c;       // 早退
$status-absent: #f87171;      // 缺勤
$status-leave: #60a5fa;       // 请假
$status-overtime: #a78bfa;    // 加班
$status-travel: #38bdf8;      // 出差

// 功能图标色
$icon-record: #4ade80;
$icon-leave: #60a5fa;
$icon-overtime: #fbbf24;
$icon-statistics: #f87171;
$icon-schedule: #a78bfa;
$icon-repair: #fb923c;
$icon-travel: #38bdf8;
$icon-summary: #c084fc;
```

### 9.2 组件样式

```scss
// 状态卡片
.status-card {
  background: $attendance-gradient;
  border-radius: 20rpx;
  padding: 30rpx;
  color: white;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 30rpx;
  }
  
  .punch-status {
    display: flex;
    justify-content: space-around;
    
    .status-item {
      text-align: center;
      
      .status-time.punched {
        color: #4ade80;
      }
    }
  }
}

// 打卡按钮
.punch-btn {
  flex: 0 0 48%;
  height: 120rpx;
  border-radius: 15rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: none;
  transition: all 0.3s ease;
  
  &.punch-in {
    background: $punch-in-color;
    
    &:active {
      background: $punch-in-hover;
      transform: scale(0.98);
    }
  }
  
  &.punch-out {
    background: $punch-out-color;
    
    &:active {
      background: $punch-out-hover;
      transform: scale(0.98);
    }
  }
  
  &:disabled {
    opacity: 0.5;
    transform: none;
  }
}

// 功能菜单
.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  
  .menu-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24rpx 12rpx;
    border-radius: 16rpx;
    background-color: #f8fafc;
    transition: all 0.3s ease;
    
    &:active {
      transform: scale(0.95);
      background-color: #f1f5f9;
    }
    
    .menu-icon-wrapper {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 20rpx;
      margin-bottom: 12rpx;
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
    }
  }
}
```

## 10. 页面路由配置

```json
// pages.json
{
  "pages": [
    {
      "path": "pages/attendance/index",
      "style": {
        "navigationBarTitleText": "考勤打卡",
        "navigationBarBackgroundColor": "#667eea",
        "navigationBarTextStyle": "white",
        "enablePullDownRefresh": true
      }
    },
    {
      "path": "pages/attendance/records",
      "style": {
        "navigationBarTitleText": "打卡记录",
        "enablePullDownRefresh": true
      }
    },
    {
      "path": "pages/attendance/leave",
      "style": {
        "navigationBarTitleText": "请假申请"
      }
    },
    {
      "path": "pages/attendance/leave-list",
      "style": {
        "navigationBarTitleText": "请假记录",
        "enablePullDownRefresh": true
      }
    },
    {
      "path": "pages/attendance/overtime",
      "style": {
        "navigationBarTitleText": "加班申请"
      }
    },
    {
      "path": "pages/attendance/statistics",
      "style": {
        "navigationBarTitleText": "考勤统计"
      }
    },
    {
      "path": "pages/attendance/schedule",
      "style": {
        "navigationBarTitleText": "我的排班"
      }
    },
    {
      "path": "pages/attendance/repair",
      "style": {
        "navigationBarTitleText": "补卡申请"
      }
    },
    {
      "path": "pages/attendance/trip",
      "style": {
        "navigationBarTitleText": "出差申请"
      }
    },
    {
      "path": "pages/attendance/summary",
      "style": {
        "navigationBarTitleText": "考勤汇总"
      }
    }
  ]
}
```

## 11. 权限控制

### 11.1 功能权限

| 功能 | 权限标识 | 角色 | 说明 |
|------|---------|------|------|
| GPS打卡 | `attendance:punch` | 所有员工 | 基础打卡功能 |
| 外勤打卡 | `attendance:punch:outside` | 外勤人员 | 不受地点限制 |
| 请假申请 | `attendance:leave:apply` | 所有员工 | 提交请假申请 |
| 加班申请 | `attendance:overtime:apply` | 所有员工 | 提交加班申请 |
| 补卡申请 | `attendance:supplement:apply` | 所有员工 | 限制次数 |
| 出差申请 | `attendance:travel:apply` | 所有员工 | 提交出差申请 |
| 查看统计 | `attendance:statistics:view` | 所有员工 | 个人统计 |
| 查看排班 | `attendance:schedule:view` | 所有员工 | 个人排班 |

### 11.2 数据权限

| 数据类型 | 权限范围 | 说明 |
|---------|---------|------|
| 打卡记录 | 仅本人 | 员工只能查看自己的打卡记录 |
| 请假记录 | 仅本人 | 员工只能查看自己的请假记录 |
| 排班信息 | 仅本人 | 员工只能查看自己的排班 |
| 统计数据 | 仅本人 | 员工只能查看自己的统计 |

## 12. 性能优化

### 12.1 优化策略

| 优化项 | 策略 | 效果 |
|-------|------|------|
| 首屏加载 | 骨架屏+延迟加载 | 首屏时间<1s |
| 列表渲染 | 虚拟列表 | 大数据量流畅滚动 |
| 图片加载 | 懒加载+压缩 | 减少流量消耗 |
| 接口请求 | 请求合并+缓存 | 减少请求次数 |
| 定位服务 | 缓存+节流 | 降低功耗 |

### 12.2 缓存策略

| 数据类型 | 缓存方式 | 有效期 | 更新策略 |
|---------|---------|-------|---------|
| 用户信息 | Pinia + Storage | 登录期间 | 登录时更新 |
| 今日状态 | Pinia | 页面生命周期 | 每次进入刷新 |
| 排班信息 | Storage | 1天 | 每日首次请求更新 |
| 考勤地点 | Storage | 7天 | 手动刷新 |
| 离线数据 | IndexedDB | 永久 | 同步后清理 |

## 13. 安全设计

### 13.1 防作弊机制

| 机制 | 说明 | 实现方式 |
|------|------|---------|
| 位置验证 | GPS定位+围栏验证 | 服务端验证坐标范围 |
| 照片采集 | 打卡时拍照 | 可选配置，防止代打卡 |
| 设备绑定 | 设备指纹验证 | 限制打卡设备数量 |
| 时间校验 | 服务端时间为准 | 防止修改手机时间 |
| 频率限制 | 打卡间隔限制 | 防止频繁打卡 |

### 13.2 数据安全

| 安全措施 | 说明 |
|---------|------|
| HTTPS传输 | 所有接口强制HTTPS |
| Token认证 | Sa-Token会话管理 |
| 数据脱敏 | 敏感信息脱敏展示 |
| 本地加密 | 离线数据AES加密存储 |

## 14. 测试规范

### 14.1 测试用例

| 测试场景 | 测试点 | 预期结果 |
|---------|-------|---------|
| 正常打卡 | 位置在范围内打卡 | 打卡成功，状态更新 |
| 位置异常 | 位置不在范围内 | 提示不在考勤范围 |
| 重复打卡 | 已打卡再次打卡 | 提示已打卡 |
| 离线打卡 | 网络断开时打卡 | 缓存成功，提示稍后同步 |
| 网络恢复 | 离线数据同步 | 自动同步，状态更新 |
| 请假提交 | 填写表单提交 | 提交成功，跳转列表 |
| 表单验证 | 必填项为空 | 显示验证错误 |

### 14.2 兼容性测试

| 平台 | 最低版本 | 测试机型 |
|------|---------|---------|
| iOS | 12.0+ | iPhone 8/X/12/14 |
| Android | 7.0+ | 小米/华为/OPPO/vivo |
| 微信小程序 | 基础库2.20+ | 各主流机型 |

## 15. 文档版本

| 版本 | 日期 | 更新内容 | 作者 |
|------|------|---------|------|
| v1.0.0 | 2025-01-30 | 初始版本，基于现有代码分析 | IOE-DREAM Team |
| v2.0.0 | 2025-01-30 | 完善企业级标准内容 | IOE-DREAM Team |
