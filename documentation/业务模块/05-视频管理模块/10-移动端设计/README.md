# 视频管理模块移动端设计

> **版本**: v1.0.0  
> **创建日期**: 2025-12-17  
> **微服务**: ioedream-video-service (8092)

---

## 📱 移动端功能规划

| 功能 | 移动端 | PC端 | 说明 |
|------|--------|------|------|
| 实时视频预览 | ✅ | ✅ | 核心功能 |
| 设备状态查看 | ✅ | ✅ | 设备监控 |
| 告警消息查看 | ✅ | ✅ | 告警通知 |
| 告警处理 | ✅ | ✅ | 快速处理 |
| 历史回放 | ✅ | ✅ | 简化版 |
| 云台控制 | ✅ | ✅ | PTZ控制 |
| 截图/录制 | ✅ | ✅ | 证据采集 |
| 设备配置管理 | ❌ | ✅ | 复杂操作 |
| 解码上墙 | ❌ | ✅ | 大屏功能 |
| 行为分析配置 | ❌ | ✅ | 管理功能 |

---

## 📁 现有移动端页面

```
smart-app/src/pages/video/
├── live.vue              # 实时预览
├── device-list.vue       # 设备列表
├── alarm.vue             # 告警消息
├── playback.vue          # 历史回放
├── ptz-control.vue       # 云台控制
└── capture.vue           # 截图录制
```

---

## 🎯 核心API

```
基础路径: /api/video/v1/mobile

GET  /devices                    # 设备列表
GET  /devices/{id}/status        # 设备状态
GET  /live/{deviceId}            # 获取直播流
GET  /playback/{deviceId}        # 获取回放流
POST /ptz/{deviceId}/control     # 云台控制
POST /capture/{deviceId}         # 截图
GET  /alarms                     # 告警列表
POST /alarms/{id}/handle         # 处理告警
```

---

## 🎥 视频流协议

- **直播流**: HLS / WebRTC
- **回放流**: HLS
- **移动端优化**: 自适应码率

---

## 📊 性能要求

| 指标 | 目标值 |
|------|--------|
| 视频加载 | < 3秒 |
| 云台响应 | < 500ms |
| 告警推送 | < 5秒 |

---

**📝 文档维护**: IOE-DREAM架构团队 | 2025-12-17
