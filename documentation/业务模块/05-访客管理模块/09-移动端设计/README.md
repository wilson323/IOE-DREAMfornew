# 访客管理模块移动端设计

> **版本**: v1.0.0  
> **创建日期**: 2025-12-17  
> **微服务**: ioedream-visitor-service (8095)

---

## 📱 移动端功能规划

| 功能 | 移动端 | PC端 | 说明 |
|------|--------|------|------|
| 访客预约申请 | ✅ | ✅ | 核心功能 |
| 预约审批 | ✅ | ✅ | 审批操作 |
| 预约记录查询 | ✅ | ✅ | 我的预约 |
| 访客二维码 | ✅ | ❌ | 移动专属 |
| 访客签到/签退 | ✅ | ✅ | 自助签到 |
| 来访通知 | ✅ | ✅ | 推送提醒 |
| 访客记录查询 | ✅ | ✅ | 来访记录 |
| 物流预约 | ✅ | ✅ | 物流管理 |
| 访客配置管理 | ❌ | ✅ | 管理功能 |
| 黑名单管理 | ❌ | ✅ | 安全管理 |

---

## 📁 现有移动端页面

```
smart-app/src/pages/visitor/
├── appointment.vue       # 预约申请
├── approval.vue          # 预约审批
├── my-appointments.vue   # 我的预约
├── qrcode.vue            # 访客二维码
├── check-in.vue          # 访客签到
├── records.vue           # 来访记录
└── logistics.vue         # 物流预约
```

---

## 🎯 核心API

```
基础路径: /api/visitor/v1/mobile

POST /appointment                # 提交预约
GET  /appointment/mine           # 我的预约列表
GET  /appointment/{id}           # 预约详情
POST /appointment/{id}/approve   # 审批通过
POST /appointment/{id}/reject    # 审批拒绝
GET  /qrcode/{appointmentId}     # 获取访客二维码
POST /check-in                   # 访客签到
POST /check-out                  # 访客签退
GET  /records                    # 来访记录
POST /logistics                  # 物流预约
```

---

## 🔔 通知场景

1. **预约提交** - 通知被访人审批
2. **审批结果** - 通知访客
3. **访客到达** - 通知被访人
4. **访客离开** - 记录通知

---

## 📊 性能要求

| 指标 | 目标值 |
|------|--------|
| 预约提交 | < 2秒 |
| 二维码生成 | < 1秒 |
| 签到响应 | < 2秒 |

---

**📝 文档维护**: IOE-DREAM架构团队 | 2025-12-17
