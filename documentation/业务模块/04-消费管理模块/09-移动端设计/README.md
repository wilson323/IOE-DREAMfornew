# 消费管理模块移动端设计

> **版本**: v1.0.0  
> **创建日期**: 2025-12-17  
> **微服务**: ioedream-consume-service (8094)

---

## 📱 移动端功能规划

| 功能 | 移动端 | PC端 | 说明 |
|------|--------|------|------|
| 账户余额查询 | ✅ | ✅ | 核心功能 |
| 消费记录查询 | ✅ | ✅ | 个人记录 |
| 消费统计 | ✅ | ✅ | 月度汇总 |
| 二维码付款 | ✅ | ❌ | 移动专属 |
| 充值申请 | ✅ | ✅ | 在线充值 |
| 挂失/解挂 | ✅ | ✅ | 卡片管理 |
| 补贴查询 | ✅ | ✅ | 补贴记录 |
| 账户管理配置 | ❌ | ✅ | 管理功能 |
| 消费规则配置 | ❌ | ✅ | 管理功能 |
| 补贴发放 | ❌ | ✅ | 管理功能 |

---

## 📁 现有移动端页面

```
smart-app/src/pages/consume/
├── account.vue           # 账户信息
├── record.vue            # 消费记录
├── statistics.vue        # 消费统计
├── qrcode.vue            # 付款二维码
├── recharge.vue          # 充值页面
├── subsidy.vue           # 补贴查询
└── card-manage.vue       # 卡片管理
```

---

## 🎯 核心API

```
基础路径: /api/consume/v1/mobile

GET  /account                    # 账户信息
GET  /account/balance            # 余额查询
GET  /records                    # 消费记录
GET  /statistics                 # 消费统计
GET  /qrcode                     # 获取付款码
POST /recharge                   # 充值申请
POST /card/lost                  # 挂失
POST /card/unlock                # 解挂
GET  /subsidy                    # 补贴记录
```

---

## 💳 支付方式

1. **二维码支付** - 主动/被动扫码
2. **NFC支付** - 手机NFC
3. **人脸支付** - 刷脸消费

---

## 📊 性能要求

| 指标 | 目标值 |
|------|--------|
| 余额查询 | < 1秒 |
| 二维码生成 | < 2秒 |
| 支付响应 | < 3秒 |

---

**📝 文档维护**: IOE-DREAM架构团队 | 2025-12-17
