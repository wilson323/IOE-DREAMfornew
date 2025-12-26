# 设备通讯协议组件化架构文档

## 📋 概述

本文档描述了IOE-DREAM设备通讯协议组件化架构的设计和实现。该架构支持多种设备厂商的PUSH协议，包括：

- **考勤PUSH协议**（熵基科技 V4.0）
- **门禁PUSH协议**（熵基科技 V4.8）
- **消费PUSH协议**（中控智慧 V1.0）

## 🏗️ 架构设计

### 核心组件

```
┌─────────────────────────────────────────────────────────┐
│                   设备推送数据                            │
│              (TCP/HTTP/UDP)                              │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│              TcpPushServer / ProtocolController         │
│               (接收设备推送)                             │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│                    MessageRouter                        │
│                  (消息路由器)                            │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│              ProtocolAdapterFactory                     │
│              (协议适配器工厂)                            │
└──────────────────┬──────────────────────────────────────┘
                   │
        ┌──────────┴──────────┬──────────────┐
        ▼                     ▼              ▼
┌──────────────┐    ┌──────────────┐  ┌──────────────┐
│ Attendance   │    │ Access       │  │ Consume      │
│ Protocol     │    │ Protocol     │  │ Protocol     │
│ Handler      │    │ Handler      │  │ Handler      │
└──────────────┘    └──────────────┘  └──────────────┘
```

### 包结构

```
net.lab1024.sa.devicecomm.protocol/
├── handler/                    # 协议处理器
│   ├── ProtocolHandler.java   # 协议处理器接口
│   ├── ProtocolParseException.java
│   ├── ProtocolProcessException.java
│   └── impl/                  # 具体实现
│       ├── AttendanceProtocolHandler.java
│       ├── AccessProtocolHandler.java
│       └── ConsumeProtocolHandler.java
├── adapter/                    # 协议适配器
│   └── ProtocolAdapterFactory.java
├── router/                     # 消息路由
│   └── MessageRouter.java
├── server/                     # 服务器
│   └── TcpPushServer.java
├── message/                    # 协议消息
│   └── ProtocolMessage.java
└── enums/                      # 枚举
    └── ProtocolTypeEnum.java
```

## 🔧 使用说明

### 1. 协议处理器注册

所有协议处理器使用`@Component`注解，由Spring自动注册到`ProtocolAdapterFactory`：

```java
@Component
public class AttendanceProtocolHandler implements ProtocolHandler {
    // ...
}
```

### 2. 接收设备推送（HTTP方式）

通过HTTP接口接收设备推送：

```bash
POST /api/v1/device/protocol/push
Content-Type: application/octet-stream
参数:
  - protocolType: ATTENDANCE_ENTROPY_V4.0
  - deviceId: 1
Body: 原始协议数据（字节数组）
```

### 3. 自动识别协议类型

根据设备类型和厂商自动识别协议：

```bash
POST /api/v1/device/protocol/push/auto
Content-Type: application/octet-stream
参数:
  - deviceType: ATTENDANCE
  - manufacturer: 熵基科技
  - deviceId: 1
Body: 原始协议数据（字节数组）
```

### 4. TCP服务器接收推送

TCP服务器在服务启动时自动启动，监听配置的端口（默认8088）：

```yaml
device:
  protocol:
    tcp:
      port: 8088
```

## 📝 协议实现说明

### 考勤协议（AttendanceProtocolHandler）

- **协议类型**: `ATTENDANCE_ENTROPY_V4.0`
- **厂商**: 熵基科技
- **版本**: V4.0
- **文档**: 考勤PUSH通讯协议 （熵基科技） V4.0-20210113

**支持的消息类型**:
- `ATTENDANCE_RECORD` - 考勤记录
- `DEVICE_STATUS` - 设备状态

### 门禁协议（AccessProtocolHandler）

- **协议类型**: `ACCESS_ENTROPY_V4.8`
- **厂商**: 熵基科技
- **版本**: V4.8
- **文档**: 安防PUSH通讯协议 （熵基科技）V4.8-20240107

**支持的消息类型**:
- `ACCESS_EVENT` - 门禁事件
- `DEVICE_STATUS` - 设备状态
- `ALARM_EVENT` - 报警事件

### 消费协议（ConsumeProtocolHandler）

- **协议类型**: `CONSUME_ZKTECO_V1.0`
- **厂商**: 中控智慧
- **版本**: V1.0
- **文档**: 消费PUSH通讯协议 （中控智慧） V1.0-20181225

**支持的消息类型**:
- `CONSUME_RECORD` - 消费记录
- `DEVICE_STATUS` - 设备状态
- `BALANCE_QUERY` - 余额查询

## ⚠️ 重要提示

### 协议解析实现

当前实现的协议解析逻辑是基于协议文档的**通用结构**，实际使用时需要：

1. **根据实际PDF协议文档**完善协议头、消息体、校验和等字段的解析
2. **实现具体的业务数据处理**（如保存考勤记录、门禁事件、消费记录等）
3. **完善错误处理和异常恢复机制**

### TODO项

以下功能需要根据实际需求完善：

1. **协议识别**: TCP服务器需要实现协议自动识别逻辑
2. **设备ID映射**: 需要实现设备编号到设备ID的映射
3. **业务数据处理**: 需要调用对应的业务服务保存数据
4. **校验和验证**: 需要根据实际协议文档实现校验和验证
5. **响应消息构建**: 需要根据实际协议文档完善响应消息格式

## 🔄 扩展新协议

要添加新的协议支持，需要：

1. **创建协议处理器**，实现`ProtocolHandler`接口
2. **添加协议类型枚举**到`ProtocolTypeEnum`
3. **使用`@Component`注解**，由Spring自动注册
4. **实现协议解析和处理逻辑**

示例：

```java
@Component
public class NewProtocolHandler implements ProtocolHandler {
    @Override
    public String getProtocolType() {
        return "NEW_PROTOCOL_V1.0";
    }
    
    // 实现其他接口方法...
}
```

## 📚 相关文档

- [CLAUDE.md](../../../CLAUDE.md) - 项目架构规范
- [设备通讯服务规范](../../../documentation/architecture/) - 架构设计文档

