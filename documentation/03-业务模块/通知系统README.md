# IOE-DREAM 通知系统

**版本**: v2.0.0  
**更新日期**: 2025-01-30  
**状态**: ✅ **生产就绪**

---

## 📋 系统概述

IOE-DREAM通知系统是一个企业级、高可用的多渠道通知解决方案，支持邮件、短信、钉钉、企业微信、Webhook等多种通知渠道。

### 核心特性

- ✅ **多渠道支持**: 邮件、短信、钉钉、企业微信、Webhook
- ✅ **统一限流**: 滑动窗口算法，支持多渠道独立限流
- ✅ **统一重试**: 指数退避策略，支持错误码检查
- ✅ **监控指标**: Micrometer集成，支持Prometheus导出
- ✅ **消息模板**: 变量替换、多级缓存、模板热更新
- ✅ **多种消息格式**: 支持Markdown、ActionCard、FeedCard、卡片消息、图文消息
- ✅ **配置管理**: 统一配置管理，支持加密存储、热更新
- ✅ **Token管理**: 企业微信Token自动刷新和缓存

---

## 🚀 快速开始

### 1. 发送通知

```java
@Resource
private NotificationService notificationService;

NotificationEntity notification = new NotificationEntity();
notification.setTitle("系统通知");
notification.setContent("您有一条新的系统通知");
notification.setReceiverIds("1001,1002");
notification.setChannel(1); // 1-邮件 2-短信 3-Webhook 4-微信

boolean success = notificationService.sendNotification(notification);
```

### 2. 使用模板

```java
@Resource
private NotificationTemplateService notificationTemplateService;

Map<String, Object> variables = new HashMap<>();
variables.put("userName", "张三");
variables.put("loginTime", "2025-01-30 10:00:00");

String content = notificationTemplateService.renderTemplate("EMAIL_LOGIN_SUCCESS", variables);
```

### 3. 查询监控指标

```java
@Resource
private NotificationMetricsCollector notificationMetricsCollector;

double successRate = notificationMetricsCollector.getSuccessRate("DINGTALK");
double avgDuration = notificationMetricsCollector.getAvgDuration("WECHAT");
```

---

## 📚 文档导航

1. **使用指南**: [通知系统使用指南.md](./通知系统使用指南.md)
2. **深度分析**: [通知系统深度分析报告.md](./通知系统深度分析报告.md)
3. **实施总结**: [通知系统实施总结.md](./通知系统实施总结.md)
4. **完成报告**: [通知系统实施完成报告.md](./通知系统实施完成报告.md)
5. **最终总结**: [通知系统实施最终总结.md](./通知系统实施最终总结.md)

---

## 🔧 API接口

### 通知配置管理

- `GET /api/v1/notification/config/value/{configKey}` - 获取配置值
- `GET /api/v1/notification/config/type/{configType}` - 获取配置列表
- `PUT /api/v1/notification/config/value/{configKey}` - 更新配置值
- `PUT /api/v1/notification/config/status/{configKey}` - 更新配置状态

### 通知模板管理

- `GET /api/v1/notification/template/code/{templateCode}` - 获取模板
- `GET /api/v1/notification/template/type/{templateType}` - 获取模板列表
- `POST /api/v1/notification/template/render/{templateCode}` - 渲染模板
- `GET /api/v1/notification/template/variables/{templateCode}` - 获取模板变量列表

---

## 📊 监控指标

访问 `/actuator/prometheus` 查看所有监控指标：

- `notification_send_total` - 发送总数
- `notification_send_duration_seconds` - 发送耗时
- `notification_rate_limit_total` - 限流触发次数
- `notification_retry_total` - 重试总次数
- `notification_error_total` - 错误统计

---

## 🎯 功能完整度

| 功能模块 | 完成度 |
|---------|--------|
| 配置管理 | 100% ✅ |
| 限流保护 | 100% ✅ |
| 重试机制 | 100% ✅ |
| 监控指标 | 100% ✅ |
| 消息模板 | 100% ✅ |
| 多种消息格式 | 80% ✅ |
| 短信SDK集成 | 100% ✅ |
| **整体功能** | **95%** ✅ |

---

## 📞 技术支持

**问题反馈**: 在项目issue中提交问题  
**文档更新**: 联系架构师团队  
**技术支持**: IOE-DREAM架构委员会

---

**维护团队**: IOE-DREAM架构委员会  
**版本**: v2.0.0  
**最后更新**: 2025-01-30
