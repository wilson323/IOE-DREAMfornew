# 门禁模块移动端设计文档

> **版本**: v1.0.0  
> **创建日期**: 2025-12-17  
> **适用范围**: IOE-DREAM智慧园区门禁系统移动端

---

## 📋 概述

本文档定义门禁模块移动端功能设计，基于现有 `smart-app/src/pages/access/` 代码结构，结合实际业务场景进行功能规划。

### 移动端定位

移动端聚焦于**轻量级、高频操作场景**，复杂配置操作保留在PC端：

| 功能类型 | 移动端 | PC端 | 说明 |
|---------|--------|------|------|
| 设备状态查看 | ✅ | ✅ | 随时查看设备在线状态 |
| 远程开锁 | ✅ | ✅ | 紧急情况快速开锁 |
| 通行记录查询 | ✅ | ✅ | 查看个人通行记录 |
| 实时监控 | ✅ | ✅ | 查看实时通行事件 |
| 区域信息查看 | ✅ | ✅ | 查看有权限的区域 |
| 审批处理 | ✅ | ✅ | 移动端快速审批 |
| 告警接收处理 | ✅ | ✅ | 实时接收告警通知 |
| 设备管理配置 | ❌ | ✅ | 复杂配置需PC端 |
| 系统参数配置 | ❌ | ✅ | 安全要求需PC端 |
| 高级功能配置 | ❌ | ✅ | 专业操作需PC端 |

---

## 📱 现有页面结构

```
smart-app/src/pages/access/
├── area.vue        # 区域管理 - 查看有权限的区域列表
├── device.vue      # 设备管理 - 查看设备状态、远程控制
├── monitor.vue     # 实时监控 - 设备状态统计、实时事件
├── permission.vue  # 权限管理 - 查看个人权限
└── record.vue      # 通行记录 - 查看通行历史、筛选统计
```

---

## 🎯 功能模块设计

### 1. 区域管理页面 (area.vue)

**功能说明**:
- 展示用户有权限访问的区域列表
- 显示区域内设备数量和权限数量
- 支持查看区域详情

**用户故事**:
> 作为员工，我希望查看自己能进入哪些区域，以便规划出行路线。

**数据接口**:
```javascript
// 获取用户有权限的区域列表
GET /api/access/v1/mobile/areas
Response: {
  areaId, areaName, areaType, deviceCount, permissionCount
}
```

### 2. 设备管理页面 (device.vue)

**功能说明**:
- 展示设备列表和在线状态
- 支持设备搜索和筛选
- 远程开锁操作（有权限时）

**用户故事**:
> 作为安保人员，我希望随时查看设备状态，在紧急情况下远程开锁。

**数据接口**:
```javascript
// 获取设备列表
GET /api/access/v1/mobile/devices
Response: { deviceId, deviceName, areaName, online }

// 远程开锁
POST /api/access/v1/mobile/devices/{deviceId}/unlock
Request: { reason: "紧急开锁" }
```

### 3. 实时监控页面 (monitor.vue)

**功能说明**:
- 显示设备状态统计（在线/离线/告警）
- 今日通行次数统计
- 实时事件列表（最近20条）
- 支持下拉刷新

**用户故事**:
> 作为安保管理员，我希望实时了解门禁系统运行状态。

**数据接口**:
```javascript
// 获取统计数据
GET /api/access/v1/mobile/statistics
Response: { onlineDevices, offlineDevices, todayAccess, activeAlerts }

// 获取实时事件
GET /api/access/v1/mobile/events?limit=20
Response: [{ eventType, eventTime, description, deviceName }]
```

### 4. 通行记录页面 (record.vue)

**功能说明**:
- 个人通行记录列表
- 支持按时间、状态筛选
- 统计今日/本周通行次数和成功率
- 显示通行详情（设备、方式、结果）

**用户故事**:
> 作为员工，我希望查看自己的通行记录，了解异常情况。

**数据接口**:
```javascript
// 获取通行记录
GET /api/access/v1/mobile/records
Params: { pageNum, pageSize, status, dateRange }
Response: {
  total, records: [{
    userName, deptName, areaName, deviceName,
    accessTime, success, accessMethod, failReason
  }]
}
```

---

## 🔔 实时消息推送

### WebSocket订阅

```javascript
// 连接WebSocket
ws://api.ioedream.com/access/v1/ws

// 订阅消息类型
{
  type: 'SUBSCRIBE',
  channels: ['device-status', 'access-events', 'alerts']
}

// 接收消息格式
{
  type: 'DEVICE_STATUS' | 'ACCESS_EVENT' | 'ALERT',
  data: { ... }
}
```

### 推送通知场景

| 场景 | 推送内容 | 推送对象 |
|------|---------|---------|
| 设备离线 | 设备名称、离线时间 | 管理员 |
| 异常通行 | 人员、设备、原因 | 安保人员 |
| 审批待办 | 申请人、申请类型 | 审批人 |
| 通行成功 | 通行位置、时间 | 本人(可选) |

---

## 🎨 UI设计规范

### 设计原则

1. **简洁高效**: 单手操作、快速完成
2. **状态清晰**: 颜色区分在线/离线/异常
3. **信息层级**: 重要信息突出显示
4. **操作反馈**: 及时的操作成功/失败提示

### 颜色规范

```scss
// 状态颜色
$color-online: #52c41a;   // 在线/成功
$color-offline: #ff4d4f;  // 离线/失败
$color-warning: #faad14;  // 警告/异常
$color-primary: #667eea;  // 主色调

// 主题渐变
$gradient-primary: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
```

### 组件复用

- **导航栏**: 统一使用自定义navbar
- **列表卡片**: 统一圆角、阴影、间距
- **状态标签**: 复用van-tag组件
- **加载状态**: 使用uni-load-more组件
- **筛选弹窗**: 使用uni-popup组件

---

## 📊 性能优化

### 数据加载策略

1. **分页加载**: 通行记录分页，每页20条
2. **下拉刷新**: 支持手动刷新数据
3. **上拉加载**: 滚动到底自动加载更多
4. **缓存策略**: 设备列表、区域列表本地缓存

### 图片优化

- 使用van-image懒加载
- 缩略图展示，点击查看大图
- 加载失败显示占位图

### 网络优化

- WebSocket断线自动重连
- 请求超时30秒自动重试
- 离线状态提示

---

## ✅ 验收标准

### 功能验收
- [ ] 区域列表正常加载和显示
- [ ] 设备状态正确显示(在线/离线)
- [ ] 远程开锁功能正常
- [ ] 通行记录筛选和分页正常
- [ ] 实时监控数据自动刷新
- [ ] 推送通知正常接收

### 性能验收
- [ ] 首屏加载时间 < 3秒
- [ ] 列表滚动流畅无卡顿
- [ ] WebSocket消息延迟 < 5秒

### 兼容性验收
- [ ] iOS 12+ 正常运行
- [ ] Android 8+ 正常运行
- [ ] 微信小程序正常运行

---

## 📚 相关文档

- [门禁模块总体设计](../README.md)
- [前端API接口设计](../04-门禁模块前端API接口设计文档.md)
- [前端界面设计](../05-门禁模块前端界面设计文档.md)

---

**📝 文档维护**
- **创建人**: IOE-DREAM架构团队
- **最后更新**: 2025-12-17
