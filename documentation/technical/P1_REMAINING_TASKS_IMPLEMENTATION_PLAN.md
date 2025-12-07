# P1级待完成功能实现计划

**生成时间**: 2025-01-30  
**报告版本**: v1.0.0  
**执行阶段**: 阶段2 - P1级前后端移动端实现  
**状态**: 📋 **实施计划**

---

## 📊 待完成功能梳理

### 一、前端页面待完成功能

#### 1.1 支付管理页面（消费模块）⭐ 优先级：P1

**功能需求**:
- ⏳ 支付订单创建（微信支付、支付宝支付）
- ⏳ 支付订单查询（分页、状态筛选）
- ⏳ 退款申请（微信退款、支付宝退款）
- ⏳ 支付统计（今日支付金额、订单数、成功率）

**技术实现**:
- 文件路径: `smart-admin-web-javascript/src/views/business/consume/payment/index.vue`
- API对接: 使用 `consumeApi`（已创建）
- 参考代码: `consume/account/index.vue`、`consume/transaction/index.vue`

**预计工作量**: 1-2天

---

#### 1.2 访客模块页面检查和完善 ⚠️ 优先级：P1

**现有页面检查**:
- ✅ `visitor/index.vue` - 主入口页面（已存在，较完整）
- ✅ `visitor/appointment.vue` - 预约管理页面（已存在，较完整）
- ✅ `visitor/record.vue` - 访客记录页面（已存在）
- ✅ `visitor/statistics.vue` - 统计页面（已存在）
- ✅ `visitor/verification.vue` - 验证页面（已存在）
- ✅ `visitor/registration.vue` - 登记页面（已存在）

**API对接检查**:
- ✅ 使用 `visitorApi`（移动端API）
- ⚠️ 需要检查是否对接PC端API（`visitorPcApi`）
- ⚠️ 需要检查预约查询功能是否完整

**需要完善**:
1. 检查 `visitor/appointment.vue` 是否使用PC端API
2. 检查 `visitor/statistics.vue` 是否对接PC端统计API
3. 确保所有页面使用正确的API文件

**预计工作量**: 0.5-1天

---

#### 1.3 视频模块页面检查和完善 ⚠️ 优先级：P1

**现有页面检查**:
- ✅ `smart-video/device-list.vue` - 设备列表（已存在）
- ✅ `smart-video/monitor-preview.vue` - 监控预览（已存在）
- ✅ `smart-video/video-playback.vue` - 录像回放（已存在）
- ✅ `smart-video/system-overview.vue` - 系统概览（已存在）

**API对接检查**:
- ⚠️ 需要检查是否使用 `videoPcApi`（已创建的PC端API）
- ⚠️ 需要检查设备管理功能是否完整
- ⚠️ 需要检查视频播放功能是否完整

**需要完善**:
1. 检查设备列表页面是否使用 `videoPcApi.queryDevices`
2. 检查视频播放页面是否使用 `videoPcApi.getVideoStream`
3. 确保所有页面使用正确的API文件

**预计工作量**: 0.5-1天

---

### 二、移动端页面待完成功能

#### 2.1 消费模块移动端页面 ⚠️ 优先级：P1

**现有页面检查**:
- ✅ `consume/index.vue` - 消费首页（已存在）
- ✅ `consume/account.vue` - 账户管理（已存在）
- ✅ `consume/payment.vue` - 支付页面（已存在）
- ✅ `consume/record.vue` - 交易记录（已存在）
- ✅ `consume/recharge.vue` - 充值页面（已存在）
- ✅ `consume/refund.vue` - 退款页面（已存在）
- ✅ `consume/transaction.vue` - 交易页面（已存在）
- ✅ `consume/qrcode.vue` - 二维码页面（已存在）

**需要检查**:
1. 检查所有页面是否使用 `consumeApi`（移动端API）
2. 检查API方法是否完整
3. 检查页面功能是否完整

**预计工作量**: 0.5-1天

---

#### 2.2 门禁模块移动端页面 ⚠️ 优先级：P1

**现有页面检查**:
- ✅ `access/area.vue` - 区域管理（已存在）
- ✅ `access/device.vue` - 设备管理（已存在）
- ✅ `access/monitor.vue` - 监控页面（已存在）
- ✅ `access/permission.vue` - 权限管理（已存在）
- ✅ `access/record.vue` - 记录查询（已存在）

**需要检查**:
1. 检查所有页面是否使用移动端API
2. 检查页面功能是否完整

**预计工作量**: 0.5-1天

---

#### 2.3 考勤模块移动端页面 ⚠️ 优先级：P0（高优先级）

**现有页面**: ❌ 不存在

**需要创建**:
1. `attendance/punch.vue` - 打卡页面
   - GPS定位打卡
   - 位置验证
   - 打卡记录查询

2. `attendance/record.vue` - 考勤记录页面
   - 考勤记录列表
   - 考勤统计
   - 请假/加班/出差申请

**API对接**:
- 需要创建移动端考勤API文件
- 使用 `AttendanceMobileController` 的API

**预计工作量**: 2-3天

---

#### 2.4 访客模块移动端页面 ⚠️ 优先级：P1

**现有页面检查**:
- ✅ `visitor/index.vue` - 访客首页（已存在）
- ✅ `visitor/appointment.vue` - 预约页面（已存在）
- ✅ `visitor/checkin.vue` - 签到页面（已存在）
- ✅ `visitor/checkout.vue` - 签退页面（已存在）
- ✅ `visitor/record.vue` - 记录页面（已存在）
- ✅ `visitor/qrcode.vue` - 二维码页面（已存在）

**需要检查**:
1. 检查所有页面是否使用移动端API
2. 检查页面功能是否完整

**预计工作量**: 0.5-1天

---

#### 2.5 视频模块移动端页面 ⚠️ 优先级：P1

**现有页面检查**:
- ✅ `video/device.vue` - 设备列表（已存在）
- ✅ `video/monitor.vue` - 监控页面（已存在）
- ✅ `video/playback.vue` - 回放页面（已存在）
- ✅ `video/ptz.vue` - PTZ控制（已存在）
- ✅ `video/alert.vue` - 告警页面（已存在）

**需要检查**:
1. 检查所有页面是否使用移动端API
2. 检查页面功能是否完整

**预计工作量**: 0.5-1天

---

## 📋 实施计划

### 阶段1：前端页面完善（1-2天）

#### 任务1.1：实现支付管理页面
**优先级**: P1  
**预计时间**: 1-2天

**实现内容**:
1. 创建 `consume/payment/index.vue`
   - 支付订单列表查询
   - 支付订单创建（微信/支付宝）
   - 退款申请
   - 支付统计

2. 创建组件
   - `PaymentOrderModal.vue` - 支付订单创建弹窗
   - `RefundModal.vue` - 退款申请弹窗
   - `PaymentStatistics.vue` - 支付统计组件

**API对接**:
- `consumeApi.createWechatPayOrder`
- `consumeApi.createAlipayOrder`
- `consumeApi.wechatRefund`
- `consumeApi.alipayRefund`

---

#### 任务1.2：检查访客模块页面
**优先级**: P1  
**预计时间**: 0.5天

**检查内容**:
1. 检查 `visitor/appointment.vue` 是否使用PC端API
2. 检查 `visitor/statistics.vue` 是否对接PC端统计API
3. 更新API调用，使用 `visitorPcApi` 替代 `visitorApi`（PC端功能）

**需要修改的文件**:
- `visitor/appointment.vue` - 预约查询功能
- `visitor/statistics.vue` - 统计功能

---

#### 任务1.3：检查视频模块页面
**优先级**: P1  
**预计时间**: 0.5天

**检查内容**:
1. 检查 `smart-video/device-list.vue` 是否使用PC端API
2. 检查 `smart-video/monitor-preview.vue` 是否使用PC端API
3. 更新API调用，使用 `videoPcApi` 替代现有API

**需要修改的文件**:
- `smart-video/device-list.vue` - 设备查询功能
- `smart-video/monitor-preview.vue` - 视频流获取功能

---

### 阶段2：移动端页面实现（4-6天）

#### 任务2.1：创建考勤模块移动端页面 ⭐ 优先级：P0
**预计时间**: 2-3天

**需要创建**:
1. `attendance/punch.vue` - 打卡页面
   - GPS定位打卡
   - 位置验证
   - 打卡历史查询

2. `attendance/record.vue` - 考勤记录页面
   - 考勤记录列表
   - 考勤统计
   - 请假/加班/出差申请入口

**需要创建API文件**:
- `smart-app/src/api/business/attendance/attendance-api.js`

**API对接**:
- 使用 `AttendanceMobileController` 的API

---

#### 任务2.2：检查消费模块移动端页面
**优先级**: P1  
**预计时间**: 0.5-1天

**检查内容**:
1. 检查所有页面是否使用 `consumeApi`
2. 检查API方法是否完整
3. 检查页面功能是否完整

**需要检查的文件**:
- `consume/index.vue`
- `consume/account.vue`
- `consume/payment.vue`
- `consume/record.vue`
- `consume/recharge.vue`
- `consume/refund.vue`
- `consume/transaction.vue`
- `consume/qrcode.vue`

---

#### 任务2.3：检查门禁模块移动端页面
**优先级**: P1  
**预计时间**: 0.5-1天

**检查内容**:
1. 检查所有页面是否使用移动端API
2. 检查页面功能是否完整

---

#### 任务2.4：检查访客模块移动端页面
**优先级**: P1  
**预计时间**: 0.5-1天

**检查内容**:
1. 检查所有页面是否使用移动端API
2. 检查页面功能是否完整

---

#### 任务2.5：检查视频模块移动端页面
**优先级**: P1  
**预计时间**: 0.5-1天

**检查内容**:
1. 检查所有页面是否使用移动端API
2. 检查页面功能是否完整

---

## 📊 工作量统计

### 前端页面完善

| 任务 | 优先级 | 预计时间 |
|------|--------|---------|
| 支付管理页面 | P1 | 1-2天 |
| 访客模块检查 | P1 | 0.5天 |
| 视频模块检查 | P1 | 0.5天 |
| **小计** | - | **2-3天** |

### 移动端页面实现

| 任务 | 优先级 | 预计时间 |
|------|--------|---------|
| 考勤模块页面 | P0 | 2-3天 |
| 消费模块检查 | P1 | 0.5-1天 |
| 门禁模块检查 | P1 | 0.5-1天 |
| 访客模块检查 | P1 | 0.5-1天 |
| 视频模块检查 | P1 | 0.5-1天 |
| **小计** | - | **4-7天** |

**总预计工作量**: **6-10天**

---

## 🎯 实施优先级

### 优先级P0（立即执行）
1. ✅ **考勤模块移动端页面** - 完全缺失，需要创建

### 优先级P1（快速执行）
2. ✅ **支付管理页面** - 消费模块核心功能
3. ✅ **访客模块页面检查** - 确保PC端功能完整
4. ✅ **视频模块页面检查** - 确保PC端功能完整
5. ✅ **移动端页面检查** - 确保各模块移动端功能完整

---

## 📝 实施步骤

### 第一步：实现支付管理页面（1-2天）
1. 创建 `consume/payment/index.vue`
2. 创建支付订单创建组件
3. 创建退款申请组件
4. 实现支付统计功能
5. 对接后端API

### 第二步：检查和完善现有页面（1-2天）
1. 检查访客模块PC端API对接
2. 检查视频模块PC端API对接
3. 检查移动端各模块页面
4. 修复发现的问题

### 第三步：创建考勤模块移动端页面（2-3天）
1. 创建移动端考勤API文件
2. 创建打卡页面
3. 创建考勤记录页面
4. 对接后端API

### 第四步：前后端联调测试（1-2天）
1. 测试所有PC端页面
2. 测试所有移动端页面
3. 修复发现的问题

---

## ✅ 检查清单

### 前端页面检查清单

- [ ] 支付管理页面已创建
- [ ] 访客模块PC端API对接完整
- [ ] 视频模块PC端API对接完整
- [ ] 所有页面使用正确的API文件
- [ ] 所有页面功能完整
- [ ] 所有页面编译0错误0警告

### 移动端页面检查清单

- [ ] 考勤模块移动端页面已创建
- [ ] 消费模块移动端页面功能完整
- [ ] 门禁模块移动端页面功能完整
- [ ] 访客模块移动端页面功能完整
- [ ] 视频模块移动端页面功能完整
- [ ] 所有移动端页面使用正确的API文件

---

**报告生成时间**: 2025-01-30  
**报告生成人**: IOE-DREAM Team  
**报告状态**: 📋 实施计划

