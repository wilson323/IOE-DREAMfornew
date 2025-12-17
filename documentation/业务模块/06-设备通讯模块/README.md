# IOE-DREAM 设备通讯服务模块

> **版本**: v1.0.0  
> **微服务**: ioedream-device-comm-service (8087)  
> **创建日期**: 2025-12-17

---

## 📋 模块概述

设备通讯服务负责与所有硬件设备的通信，包括门禁控制器、考勤终端、消费机、摄像头等设备的数据采集和指令下发。

---

## 🏗️ 核心功能

| 功能 | 说明 |
|------|------|
| 设备注册 | 设备自动发现和注册 |
| 心跳管理 | 设备在线状态监控 |
| 数据采集 | 实时采集设备数据 |
| 指令下发 | 远程控制设备 |
| 协议适配 | 支持多种设备协议 |
| 数据转发 | 将数据分发到业务服务 |

---

## 📁 代码结构

```
ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/
├── DeviceCommApplication.java
├── config/
│   ├── DeviceProtocolConfiguration.java
│   └── MqttConfiguration.java
├── controller/
│   ├── DeviceRegisterController.java
│   └── DeviceCommandController.java
├── protocol/
│   ├── AccessProtocolHandler.java      # 门禁协议
│   ├── AttendanceProtocolHandler.java  # 考勤协议
│   ├── ConsumeProtocolHandler.java     # 消费协议
│   └── VideoProtocolHandler.java       # 视频协议
├── service/
│   ├── DeviceHeartbeatService.java
│   ├── DeviceDataCollectorService.java
│   └── DeviceCommandService.java
└── manager/
    ├── DeviceConnectionManager.java
    └── DeviceStatusManager.java
```

---

## 🔌 支持的设备协议

| 协议 | 设备类型 | 说明 |
|------|----------|------|
| ZK Protocol | 门禁/考勤 | 中控设备协议 |
| MQTT | IoT设备 | 轻量级消息协议 |
| ONVIF | 摄像头 | 视频设备协议 |
| TCP/IP | 消费机 | 自定义TCP协议 |

---

## 📊 性能指标

| 指标 | 目标值 |
|------|--------|
| 设备连接数 | ≥ 5000 |
| 数据采集延迟 | < 500ms |
| 指令下发延迟 | < 1s |
| 心跳检测间隔 | 30s |

---

**📝 文档维护**: IOE-DREAM架构团队 | 2025-12-17
