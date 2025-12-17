# 消费管理模块 - 移动端前端设计

> **版本**: v2.0.0  
> **更新日期**: 2025-01-30  
> **适用范围**: IOE-DREAM智慧园区一卡通管理平台 - 消费微服务移动端

---

## 1. 移动端设计概述

### 1.1 设计定位

基于现有移动端代码分析（`smart-app/src/pages/consume/`），消费管理模块的移动端设计遵循 IOE-DREAM 智慧园区一卡通管理平台的统一规范，面向企业员工提供便捷的移动消费服务。

### 1.2 设计原则

| 原则 | 说明 | 实现方式 |
|------|------|---------|
| **场景驱动** | 移动端聚焦员工高频消费场景 | 支付、充值、查询为核心功能 |
| **支付优先** | 多种支付方式为核心功能 | 扫码/NFC/人脸/快速支付 |
| **离线支持** | 支持离线消费数据缓存和同步 | 本地存储+自动同步机制 |
| **简洁高效** | 一键支付，操作流程最短化 | 首页直达+快捷操作 |
| **安全可靠** | 支付安全+数据安全 | 多重验证+加密传输 |

### 1.3 用户角色

| 角色 | 功能范围 | 使用场景 |
|------|---------|---------|
| 普通员工 | 消费支付、充值、查询记录 | 日常消费操作 |
| 财务人员 | 员工功能+审批提醒（跳转PC） | 退款审批入口 |
| 商户操作员 | 收款、交易查询 | POS终端操作 |

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
│  pages/consume/*.vue                         │
├─────────────────────────────────────────────┤
│                 组件层 (Components)           │
│  components/consume/*.vue                    │
├─────────────────────────────────────────────┤
│                状态管理层 (Store)             │
│  store/modules/consume.js                    │
├─────────────────────────────────────────────┤
│                 服务层 (API)                  │
│  api/business/consume/consume-api.js         │
├─────────────────────────────────────────────┤
│                工具层 (Utils)                 │
│  utils/payment.js, utils/encrypt.js          │
└─────────────────────────────────────────────┘
```

## 3. 移动端功能范围

### 3.1 移动端实现功能（基于业务场景）

| 功能模块 | 优先级 | 业务场景说明 | 技术要点 |
|---------|-------|-------------|---------|
| 消费首页 | P0 | 账户余额、快捷消费入口 | 余额实时刷新 |
| 扫码支付 | P0 | 扫描商户二维码支付 | 相机扫码+解析 |
| NFC支付 | P0 | 刷卡消费 | NFC读写+设备兼容 |
| 人脸支付 | P1 | 人脸识别消费 | 活体检测+人脸比对 |
| 快速支付 | P0 | 一键快速消费 | 预设金额+快捷操作 |
| 账户管理 | P0 | 查看余额、充值入口 | 多账户展示 |
| 充值功能 | P0 | 在线充值 | 第三方支付对接 |
| 交易记录 | P0 | 消费明细查询 | 分页加载+筛选 |
| 退款申请 | P1 | 申请退款 | 审批流程对接 |
| 付款码 | P0 | 展示付款二维码 | 动态码+定时刷新 |

### 3.2 仅PC端实现功能

| 功能模块 | 原因说明 | 替代方案 |
|---------|---------|---------|
| 账户类型管理 | 管理类功能，操作频次低 | 无 |
| 消费规则配置 | 复杂配置，适合PC操作 | 无 |
| 补贴管理 | 批量操作，PC效率高 | 移动端查看余额 |
| 统计报表 | 图表展示，PC体验更佳 | 移动端简化版 |
| 商户管理 | 管理员专用功能 | 无 |
| 设备管理 | 管理员专用功能 | 无 |
| 批量充值 | 文件操作，PC更便捷 | 无 |

## 4. 核心页面设计

### 4.1 消费首页 (`consume/index.vue`)

**页面结构**:
```
┌─────────────────────────────────┐
│         导航栏：智能消费         │
├─────────────────────────────────┤
│  账户余额卡片                    │
│  ┌─────────────────────────┐   │
│  │  账户余额               │   │
│  │  ¥ 1,234.56            │   │
│  │  补贴余额: ¥200.00      │   │
│  │  [充值]    [明细]       │   │
│  └─────────────────────────┘   │
├─────────────────────────────────┤
│  快捷消费                        │
│  ┌──┐ ┌──┐ ┌──┐ ┌──┐          │
│  │扫码│ │NFC│ │人脸│ │快速│          │
│  │支付│ │支付│ │支付│ │支付│          │
│  └──┘ └──┘ └──┘ └──┘          │
├─────────────────────────────────┤
│  当前餐别: 午餐 (11:00-13:30)   │
├─────────────────────────────────┤
│  最近交易           [查看全部]   │
│  ┌─────────────────────────┐   │
│  │ 食堂一楼   01-30 12:30  │   │
│  │ 午餐消费   -¥15.00     │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │ 便利店     01-30 09:15  │   │
│  │ 早餐消费   -¥8.50      │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

**核心功能详述**:

| 功能区域 | 功能点 | 交互说明 |
|---------|-------|---------|
| 余额卡片 | 账户余额+补贴余额展示 | 实时刷新，点击查看详情 |
| 充值按钮 | 跳转充值页面 | 快捷入口 |
| 明细按钮 | 跳转交易记录 | 快捷入口 |
| 支付方式 | 4种支付入口 | 点击进入对应支付页面 |
| 餐别提示 | 当前可用餐别 | 实时计算显示 |
| 最近交易 | 近5条交易记录 | 点击查看详情 |

### 4.2 扫码支付页面 (`consume/qrcode.vue`)

**页面结构**:
```
┌─────────────────────────────────┐
│         导航栏：扫码支付         │
├─────────────────────────────────┤
│  扫码区域                        │
│  ┌─────────────────────────┐   │
│  │                          │   │
│  │      [相机扫码区域]       │   │
│  │                          │   │
│  └─────────────────────────┘   │
│       扫描商户二维码支付         │
├─────────────────────────────────┤
│  手动输入                        │
│  ┌─────────────────────────┐   │
│  │ [输入商户码]      [确认] │   │
│  └─────────────────────────┘   │
├─────────────────────────────────┤
│  付款码                          │
│  ┌─────────────────────────┐   │
│  │      [我的付款码]        │   │
│  │   点击展示付款二维码      │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

### 4.3 账户管理页面 (`consume/account.vue`)

**账户信息展示**:

| 信息项 | 说明 | 数据来源 |
|-------|------|---------|
| 账户余额 | 可用消费金额 | 实时查询 |
| 补贴余额 | 企业补贴金额 | 实时查询 |
| 冻结金额 | 不可用金额 | 实时查询 |
| 本月消费 | 月度消费统计 | 统计接口 |
| 账户状态 | 正常/冻结/注销 | 状态查询 |

### 4.4 充值页面 (`consume/recharge.vue`)

**充值金额设计**:

| 金额档位 | 优惠说明 | 推荐标识 |
|---------|---------|---------|
| ¥50 | 无优惠 | - |
| ¥100 | 无优惠 | - |
| ¥200 | 送¥5 | 推荐 |
| ¥300 | 送¥10 | - |
| ¥500 | 送¥20 | 热门 |
| 自定义 | 按规则计算 | - |

**支付方式**:

| 支付方式 | 图标 | 说明 |
|---------|------|------|
| 微信支付 | 微信图标 | 调用微信支付SDK |
| 支付宝 | 支付宝图标 | 调用支付宝SDK |
| 银行卡 | 银行卡图标 | 银联快捷支付 |

### 4.5 交易记录页面 (`consume/record.vue`)

**筛选条件设计**:

| 筛选项 | 选项值 | 默认值 |
|-------|-------|-------|
| 交易类型 | 全部/消费/充值/退款/补贴 | 全部 |
| 时间范围 | 今日/本周/本月/自定义 | 本月 |
| 金额范围 | 不限/0-50/50-100/100+ | 不限 |

## 5. API接口设计

### 5.1 接口规范

```
基础路径: /api/v1/consume/mobile/
认证方式: Bearer Token (Sa-Token)
数据格式: application/json
字符编码: UTF-8
```

### 5.2 核心接口列表

| 模块 | 接口 | 方法 | 说明 | 请求参数 |
|------|------|------|------|---------|
| **消费交易** | `/transaction/quick` | POST | 快速消费 | userId, amount, mealType, deviceId |
| | `/transaction/scan` | POST | 扫码消费 | userId, qrCode, amount |
| | `/transaction/nfc` | POST | NFC刷卡消费 | cardNo, amount, deviceId |
| | `/transaction/face` | POST | 人脸识别消费 | faceData, amount, deviceId |
| **账户管理** | `/user/quick` | GET | 快速用户查询 | queryType, queryValue |
| | `/user/{userId}` | GET | 获取用户信息 | - |
| | `/user/consume-info/{userId}` | GET | 获取消费信息 | - |
| | `/account/balance/{userId}` | GET | 获取账户余额 | - |
| | `/user/{userId}/summary` | GET | 获取用户摘要 | - |
| **交易记录** | `/history/recent` | GET | 最近交易记录 | userId, limit |
| | `/history` | GET | 交易历史 | userId, type, startDate, endDate, pageNum, pageSize |
| | `/history/{transactionId}` | GET | 交易详情 | - |
| **餐别管理** | `/meal/available` | GET | 有效餐别 | - |
| | `/meals/available/{areaId}` | GET | 区域可用餐别 | - |
| | `/meals/current` | GET | 当前餐别 | - |
| **统计分析** | `/stats/{userId}` | GET | 用户统计 | - |
| | `/stats` | GET | 消费统计 | startDate, endDate |
| | `/device/today-stats/{deviceId}` | GET | 设备今日统计 | - |
| | `/transaction/summary` | GET | 实时交易汇总 | areaId |
| **离线同步** | `/sync/offline` | POST | 离线数据同步 | deviceId, transactions |
| | `/sync/offline/{deviceId}` | GET | 获取离线数据 | - |
| | `/sync/batch-download` | POST | 批量下载数据 | deviceId, dataTypes |
| **设备管理** | `/device/auth` | POST | 设备认证 | deviceId, deviceSecret |
| | `/device/register` | POST | 设备注册 | deviceInfo |
| | `/device/heartbeat` | POST | 设备心跳 | deviceId, status |
| | `/device/config/{deviceId}` | GET | 获取设备配置 | - |
| | `/device/config` | PUT | 更新设备配置 | deviceId, config |
| **权限验证** | `/validate/permission` | POST | 验证消费权限 | userId, mealType, amount |
| | `/permission/validate` | POST | 权限验证(通用) | userId, permissionType |
| **异常处理** | `/device/exception` | POST | 上报设备异常 | deviceId, exceptionType, details |
| | `/transaction/handle-exception` | POST | 处理交易异常 | transactionId, handleType |
| | `/exception/report` | POST | 上报异常 | exceptionData |

### 5.3 接口响应格式

```json
{
  "code": 200,
  "success": true,
  "message": "操作成功",
  "data": {
    "balance": 1234.56,
    "subsidyBalance": 200.00,
    "frozenAmount": 0.00
  },
  "timestamp": 1706601600000
}
```

### 5.4 错误码定义

| 错误码 | 说明 | 处理方式 |
|-------|------|---------|
| 4001 | 余额不足 | 提示充值入口 |
| 4002 | 非餐别时间 | 提示当前餐别信息 |
| 4003 | 超出消费限额 | 提示限额规则 |
| 4004 | 账户已冻结 | 提示联系管理员 |
| 4005 | 重复消费 | 提示上次消费时间 |
| 4006 | 权限不足 | 提示区域限制 |
| 5001 | 支付服务异常 | 提示稍后重试 |
| 5002 | 设备认证失败 | 重新认证 |

## 6. 状态管理

### 6.1 Pinia Store 结构

```javascript
// store/modules/consume.js
import { defineStore } from 'pinia'
import consumeApi from '@/api/business/consume/consume-api'

export const useConsumeStore = defineStore('consume', {
  state: () => ({
    // 账户信息
    account: {
      balance: 0,           // 账户余额
      subsidyBalance: 0,    // 补贴余额
      frozenAmount: 0,      // 冻结金额
      monthlyConsume: 0,    // 本月消费
      status: 'NORMAL'      // 账户状态
    },
    
    // 最近交易
    recentTransactions: [],
    
    // 当前餐别
    currentMeal: {
      mealType: null,
      mealName: '',
      startTime: '',
      endTime: '',
      isAvailable: false
    },
    
    // 支付方式
    paymentMethod: 'scan',  // scan/nfc/face/quick
    
    // 离线缓存
    offlineData: [],
    
    // 加载状态
    loading: {
      balance: false,
      transactions: false,
      payment: false
    }
  }),
  
  getters: {
    // 总可用余额
    totalBalance: (state) => state.account.balance + state.account.subsidyBalance,
    
    // 是否可以消费
    canConsume: (state) => 
      state.account.status === 'NORMAL' && 
      state.currentMeal.isAvailable &&
      state.account.balance > 0,
    
    // 是否有离线数据待同步
    hasOfflineData: (state) => state.offlineData.length > 0
  },
  
  actions: {
    // 加载账户余额
    async loadAccountBalance(userId) {
      this.loading.balance = true
      try {
        const res = await consumeApi.accountApi.getAccountBalance(userId)
        if (res.success) {
          this.account = { ...this.account, ...res.data }
        }
      } catch (error) {
        console.error('加载账户余额失败:', error)
      } finally {
        this.loading.balance = false
      }
    },
    
    // 加载最近交易
    async loadRecentTransactions(params) {
      this.loading.transactions = true
      try {
        const res = await consumeApi.historyApi.getRecentHistory(params)
        if (res.success) {
          this.recentTransactions = res.data
        }
      } catch (error) {
        console.error('加载最近交易失败:', error)
      } finally {
        this.loading.transactions = false
      }
    },
    
    // 加载当前餐别
    async loadCurrentMeal() {
      try {
        const res = await consumeApi.mealApi.getCurrentMeal()
        if (res.success) {
          this.currentMeal = res.data
        }
      } catch (error) {
        console.error('加载当前餐别失败:', error)
      }
    },
    
    // 快速消费
    async quickConsume(data) {
      this.loading.payment = true
      try {
        const res = await consumeApi.transactionApi.quickConsume(data)
        if (res.success) {
          // 更新余额
          await this.loadAccountBalance(data.userId)
          return { success: true, message: '消费成功', data: res.data }
        }
        return { success: false, message: res.message }
      } catch (error) {
        // 网络异常，缓存到离线数据
        this.cacheOfflineTransaction(data)
        return { success: true, message: '已缓存，网络恢复后自动同步' }
      } finally {
        this.loading.payment = false
      }
    },
    
    // 扫码消费
    async scanConsume(data) {
      this.loading.payment = true
      try {
        const res = await consumeApi.transactionApi.scanConsume(data)
        if (res.success) {
          await this.loadAccountBalance(data.userId)
          return { success: true, message: '支付成功', data: res.data }
        }
        return { success: false, message: res.message }
      } catch (error) {
        return { success: false, message: '支付失败，请重试' }
      } finally {
        this.loading.payment = false
      }
    },
    
    // 缓存离线交易
    cacheOfflineTransaction(data) {
      const offlineRecord = {
        ...data,
        id: Date.now(),
        syncStatus: 'PENDING',
        createTime: new Date().toISOString()
      }
      this.offlineData.push(offlineRecord)
      uni.setStorageSync('OFFLINE_CONSUME_DATA', this.offlineData)
    },
    
    // 同步离线数据
    async syncOfflineData(deviceId) {
      if (this.offlineData.length === 0) return
      
      try {
        const res = await consumeApi.syncApi.offlineSync({
          deviceId,
          transactions: this.offlineData
        })
        if (res.success) {
          this.offlineData = []
          uni.removeStorageSync('OFFLINE_CONSUME_DATA')
        }
      } catch (error) {
        console.error('同步离线数据失败:', error)
      }
    }
  }
})
```

## 7. 离线消费机制

### 7.1 离线数据存储

```javascript
// 离线消费数据结构
const offlineConsumeData = {
  id: 1706601600000,          // 本地唯一ID
  userId: 1001,               // 用户ID
  deviceId: 'DEV001',         // 设备ID
  amount: 15.00,              // 消费金额
  mealType: 'LUNCH',          // 餐别类型
  transactionTime: '2025-01-30T12:30:00',  // 交易时间
  paymentMethod: 'NFC',       // 支付方式
  cardNo: '****1234',         // 卡号(脱敏)
  merchantId: 'M001',         // 商户ID
  merchantName: '食堂一楼',    // 商户名称
  syncStatus: 'PENDING',      // PENDING-待同步 SYNCING-同步中 SUCCESS-成功 FAILED-失败
  syncAttempts: 0,            // 同步尝试次数
  lastSyncTime: null,         // 最后同步时间
  createTime: '2025-01-30T12:30:00'  // 创建时间
}
```

### 7.2 离线同步流程

```
┌─────────────────────────────────────────────────┐
│                   消费操作                       │
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
│   实时扣款       │    │   本地扣款       │
└────────┬────────┘    └────────┬────────┘
         ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│   服务端处理     │    │   本地余额扣减   │
│   返回结果       │    │   缓存交易数据   │
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
                      │   服务端对账     │
                      │   更新本地余额   │
                      └─────────────────┘
```

### 7.3 同步策略

| 策略 | 说明 | 配置 |
|------|------|------|
| 自动同步 | 网络恢复后自动同步 | 默认开启 |
| 重试机制 | 失败后指数退避重试 | 最多3次，间隔1s/2s/4s |
| 批量同步 | 多条数据批量上传 | 每批最多50条 |
| 冲突处理 | 服务端余额为准 | 本地余额自动校正 |
| 数据清理 | 同步成功后清理 | 保留30天历史 |

## 8. 交互规范

### 8.1 支付操作

| 场景 | 交互设计 | 反馈方式 |
|------|---------|---------|
| 余额检查 | 支付前验证余额 | 余额不足提示充值 |
| 餐别检查 | 验证当前餐别 | 非餐别时间提示 |
| 支付中 | 全屏Loading，禁止返回 | 文字"支付中..." |
| 支付成功 | Toast提示+跳转结果页 | 显示"支付成功 ¥15.00" |
| 支付失败 | Modal弹窗 | 显示失败原因+重试按钮 |
| 大额支付 | 二次确认 | Modal确认弹窗 |

### 8.2 加载状态

| 场景 | 加载方式 | 说明 |
|------|---------|------|
| 余额加载 | 数字占位符 | 显示"--"直到加载完成 |
| 列表加载 | 骨架屏 | 占位显示，避免布局抖动 |
| 支付操作 | 全屏Loading | 防止误操作 |
| 充值操作 | 按钮禁用+Loading | 防止重复提交 |

### 8.3 错误处理

| 错误类型 | 处理方式 | 用户提示 |
|---------|---------|---------|
| 余额不足 | Modal弹窗 | 提示充值入口 |
| 非餐别时间 | Toast提示 | 显示可用餐别时间 |
| 超出限额 | Modal弹窗 | 显示限额规则 |
| 网络错误 | 自动缓存 | Toast提示"已缓存" |
| 支付失败 | Modal弹窗 | 显示原因+重试 |

## 9. 样式规范

### 9.1 颜色体系

```scss
// ==================== 消费模块专用颜色 ====================

// 主题渐变色
$consume-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$consume-gradient-light: linear-gradient(135deg, #7c8cf5 0%, #8f6db8 100%);

// 金额颜色
$amount-expense: #ff4d4f;     // 支出(红色)
$amount-income: #52c41a;      // 收入(绿色)
$amount-frozen: #faad14;      // 冻结(黄色)
$amount-subsidy: #1890ff;     // 补贴(蓝色)

// 支付方式图标色
$pay-scan: #1890ff;           // 扫码支付
$pay-nfc: #52c41a;            // NFC支付
$pay-face: #722ed1;           // 人脸支付
$pay-quick: #fa8c16;          // 快速支付

// 餐别颜色
$meal-breakfast: #ffc53d;     // 早餐
$meal-lunch: #ff7a45;         // 午餐
$meal-dinner: #597ef7;        // 晚餐
$meal-snack: #9254de;         // 加餐

// 交易类型颜色
$type-consume: #ff4d4f;       // 消费
$type-recharge: #52c41a;      // 充值
$type-refund: #faad14;        // 退款
$type-subsidy: #1890ff;       // 补贴
```

### 9.2 组件样式

```scss
// 余额卡片
.balance-card {
  background: $consume-gradient;
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  color: white;
  box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.4);
  
  .balance-info {
    margin-bottom: 30rpx;
    
    .balance-label {
      font-size: 28rpx;
      opacity: 0.9;
    }
    
    .balance-amount {
      margin-top: 16rpx;
      display: flex;
      align-items: baseline;
      
      .currency {
        font-size: 36rpx;
        margin-right: 8rpx;
      }
      
      .amount {
        font-size: 72rpx;
        font-weight: bold;
        letter-spacing: -2rpx;
      }
    }
    
    .subsidy-info {
      margin-top: 16rpx;
      font-size: 24rpx;
      opacity: 0.8;
    }
  }
  
  .balance-actions {
    display: flex;
    gap: 20rpx;
    
    .action-btn {
      flex: 1;
      height: 72rpx;
      border-radius: 36rpx;
      border: 2rpx solid rgba(255, 255, 255, 0.6);
      background-color: rgba(255, 255, 255, 0.2);
      color: #fff;
      font-size: 28rpx;
      font-weight: 500;
      
      &:active {
        background-color: rgba(255, 255, 255, 0.3);
      }
    }
  }
}

// 支付方式网格
.consume-methods {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  
  .method-item {
    background-color: #fff;
    border-radius: 16rpx;
    padding: 30rpx 20rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    
    &:active {
      transform: scale(0.95);
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
    }
    
    .method-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16rpx;
      font-size: 40rpx;
      
      &.scan { background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%); }
      &.nfc { background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%); }
      &.face { background: linear-gradient(135deg, #722ed1 0%, #531dab 100%); }
      &.quick { background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%); }
    }
    
    .method-text {
      font-size: 24rpx;
      color: #333;
      font-weight: 500;
    }
  }
}

// 交易记录项
.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .transaction-info {
    flex: 1;
    
    .merchant-name {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
      margin-bottom: 8rpx;
    }
    
    .transaction-time {
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .transaction-amount {
    .amount-value {
      font-size: 32rpx;
      font-weight: 600;
      
      &.expense { color: $amount-expense; }
      &.income { color: $amount-income; }
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
      "path": "pages/consume/index",
      "style": {
        "navigationBarTitleText": "智能消费",
        "navigationBarBackgroundColor": "#667eea",
        "navigationBarTextStyle": "white",
        "enablePullDownRefresh": true
      }
    },
    {
      "path": "pages/consume/account",
      "style": {
        "navigationBarTitleText": "我的账户"
      }
    },
    {
      "path": "pages/consume/recharge",
      "style": {
        "navigationBarTitleText": "在线充值"
      }
    },
    {
      "path": "pages/consume/record",
      "style": {
        "navigationBarTitleText": "交易记录",
        "enablePullDownRefresh": true
      }
    },
    {
      "path": "pages/consume/qrcode",
      "style": {
        "navigationBarTitleText": "扫码支付",
        "navigationBarBackgroundColor": "#000000",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/consume/payment",
      "style": {
        "navigationBarTitleText": "支付"
      }
    },
    {
      "path": "pages/consume/refund",
      "style": {
        "navigationBarTitleText": "退款申请"
      }
    },
    {
      "path": "pages/consume/transaction",
      "style": {
        "navigationBarTitleText": "交易详情"
      }
    },
    {
      "path": "pages/consume/paycode",
      "style": {
        "navigationBarTitleText": "付款码",
        "navigationBarBackgroundColor": "#1890ff",
        "navigationBarTextStyle": "white"
      }
    }
  ]
}
```

## 11. 支付安全

### 11.1 安全机制

| 安全措施 | 说明 | 实现方式 |
|---------|------|---------|
| 支付密码 | 大额消费需输入支付密码 | 6位数字密码，3次错误锁定 |
| 人脸验证 | 人脸支付需活体检测 | 眨眼/摇头/张嘴动作 |
| 交易限额 | 单笔/日累计限额控制 | 可配置，默认单笔500/日2000 |
| 设备绑定 | 支付设备需绑定认证 | 最多绑定3台设备 |
| 异常监控 | 异常交易实时告警 | 短信+App推送 |
| 交易签名 | 每笔交易签名验证 | RSA2048签名 |

### 11.2 数据加密

| 加密项 | 加密方式 | 说明 |
|-------|---------|------|
| 支付密码 | AES-256-GCM | 传输加密 |
| 卡号信息 | 脱敏展示 | ****1234 |
| 人脸数据 | 不存储原图 | 仅存储特征值 |
| 通信安全 | HTTPS+证书锁定 | 防中间人攻击 |
| 本地存储 | AES加密 | 离线数据加密 |

### 11.3 风控规则

| 规则 | 触发条件 | 处理方式 |
|------|---------|---------|
| 频繁消费 | 5分钟内>3次 | 需要验证码 |
| 异地消费 | IP/GPS异常 | 短信验证 |
| 大额消费 | 超过日限额80% | 二次确认 |
| 异常时段 | 凌晨0-6点 | 需要密码 |
| 新设备 | 首次使用设备 | 短信验证 |

## 12. 权限控制

### 12.1 功能权限

| 功能 | 权限标识 | 角色 | 说明 |
|------|---------|------|------|
| 消费支付 | `consume:pay` | 所有员工 | 基础消费功能 |
| 查看余额 | `consume:balance:view` | 所有员工 | 查看账户余额 |
| 在线充值 | `consume:recharge` | 所有员工 | 自助充值 |
| 退款申请 | `consume:refund:apply` | 所有员工 | 提交退款申请 |
| 查看记录 | `consume:record:view` | 所有员工 | 查看交易记录 |
| 付款码 | `consume:paycode` | 所有员工 | 展示付款码 |

### 12.2 消费权限

| 权限类型 | 说明 | 配置方式 |
|---------|------|---------|
| 餐别限制 | 按配置的餐别时间消费 | 后台配置餐别时间段 |
| 区域限制 | 按账户类型限制消费区域 | 账户类型绑定区域 |
| 限额控制 | 单笔/日累计限额 | 账户类型配置限额 |
| 次数限制 | 每餐消费次数限制 | 防止重复消费 |

## 13. 性能优化

### 13.1 优化策略

| 优化项 | 策略 | 效果 |
|-------|------|------|
| 首屏加载 | 余额优先加载 | 首屏时间<1s |
| 支付响应 | 乐观更新 | 即时反馈 |
| 列表渲染 | 虚拟列表 | 大数据量流畅 |
| 二维码生成 | 本地生成 | 无网络依赖 |
| 接口请求 | 请求合并+缓存 | 减少请求次数 |

### 13.2 缓存策略

| 数据类型 | 缓存方式 | 有效期 | 更新策略 |
|---------|---------|-------|---------|
| 账户余额 | Pinia + Storage | 页面生命周期 | 每次进入刷新 |
| 餐别信息 | Storage | 1天 | 每日首次请求更新 |
| 商户列表 | Storage | 7天 | 手动刷新 |
| 交易记录 | Pinia | 页面生命周期 | 下拉刷新 |
| 离线数据 | IndexedDB | 永久 | 同步后清理 |

## 14. 测试规范

### 14.1 测试用例

| 测试场景 | 测试点 | 预期结果 |
|---------|-------|---------|
| 正常消费 | 余额充足+餐别时间内 | 消费成功，余额扣减 |
| 余额不足 | 余额<消费金额 | 提示余额不足 |
| 非餐别时间 | 当前不在餐别时间 | 提示非消费时间 |
| 超出限额 | 超过单笔/日限额 | 提示限额规则 |
| 离线消费 | 网络断开时消费 | 缓存成功，稍后同步 |
| 充值成功 | 完成第三方支付 | 余额增加 |
| 退款申请 | 提交退款申请 | 进入审批流程 |

### 14.2 兼容性测试

| 平台 | 最低版本 | 测试机型 |
|------|---------|---------|
| iOS | 12.0+ | iPhone 8/X/12/14 |
| Android | 7.0+ | 小米/华为/OPPO/vivo |
| 微信小程序 | 基础库2.20+ | 各主流机型 |
| NFC功能 | Android 5.0+ | 支持NFC的设备 |

## 15. 文档版本

| 版本 | 日期 | 更新内容 | 作者 |
|------|------|---------|------|
| v1.0.0 | 2025-01-30 | 初始版本，基于现有代码分析 | IOE-DREAM Team |
| v2.0.0 | 2025-01-30 | 完善企业级标准内容 | IOE-DREAM Team |
