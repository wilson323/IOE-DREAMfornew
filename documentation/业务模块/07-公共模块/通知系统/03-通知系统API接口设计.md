# 通知系统API接口设计

> **版本**: v1.0.0  
> **基础路径**: `/api/common/v1/notification`

---

## 📤 发送通知API

### 发送单条通知
```
POST /send
```
```json
{
  "templateCode": "ACCESS_ALERT",
  "targetUserId": 1001,
  "channels": ["SMS", "WEBSOCKET"],
  "params": {"deviceName": "主门", "alertType": "离线"}
}
```

### 批量发送
```
POST /batch
```

---

## 📋 通知查询API

### 获取通知列表
```
GET /list?pageNum=1&pageSize=20&status=UNREAD
```

### 标记已读
```
PUT /{notificationId}/read
```

### 全部已读
```
PUT /read-all
```

---

## 📝 模板管理API

### 获取模板列表
```
GET /templates
```

### 创建模板
```
POST /templates
```

---

## 🔔 WebSocket推送

```
连接: ws://api.ioedream.com/ws/notification
订阅: {"type": "SUBSCRIBE", "userId": 1001}
```

---

**📝 文档维护**: IOE-DREAM架构团队 | 2025-12-17
