# 设备协议实现指南

## 📋 概述

本文档详细说明了IOE-DREAM项目中设备通讯协议的实现架构、使用方法和开发规范。

**最后更新**: 2025-01-30  
**版本**: v1.0.0  
**适用范围**: 考勤、门禁、消费三个设备协议

---

## 🏗️ 架构设计

### 组件化架构

```
┌─────────────────────────────────────────────────────────┐
│              ProtocolAdapterFactory                     │
│          (协议适配器工厂，管理所有协议处理器)              │
└─────────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
┌───────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
│   Access     │ │ Attendance  │ │  Consume   │
│  Protocol    │ │  Protocol   │ │  Protocol   │
│  Handler     │ │  Handler    │ │  Handler    │
└───────┬──────┘ └──────┬──────┘ └──────┬──────┘
        │               │               │
        └───────────────┼───────────────┘
                        │
            ┌───────────▼───────────┐
            │   MessageRouter       │
            │  (消息路由器，路由到对应处理器) │
            └───────────┬───────────┘
                        │
            ┌───────────▼───────────┐
            │  GatewayServiceClient │
            │  (网关服务客户端，调用业务服务) │
            └───────────────────────┘
```

### 核心组件

#### 1. ProtocolHandler接口
所有协议处理器必须实现的接口，定义了协议处理的标准流程：

```java
public interface ProtocolHandler {
    // 协议类型标识
    String getProtocolType();
    String getManufacturer();
    String getVersion();
    
    // 消息解析
    ProtocolMessage parseMessage(byte[] rawData) throws ProtocolParseException;
    ProtocolMessage parseMessage(String rawData) throws ProtocolParseException;
    
    // 消息验证
    boolean validateMessage(ProtocolMessage message);
    
    // 消息处理
    void processMessage(ProtocolMessage message, Long deviceId) throws ProtocolProcessException;
}
```

#### 2. ProtocolMessage
协议消息的封装类，包含：
- 协议类型
- 消息类型
- 设备编号
- 原始数据（字节数组和十六进制字符串）
- 解析后的业务数据（Map格式）
- 消息状态（PARSED, VALIDATED, PROCESSED）

#### 3. 协议处理器实现

| 处理器 | 协议类型 | 厂商 | 版本 | 文档 |
|--------|---------|------|------|------|
| `AccessProtocolHandler` | ACCESS_ENTROPY_V4_8 | 熵基科技 | V4.8 | 安防PUSH通讯协议 V4.8-20240107 |
| `AttendanceProtocolHandler` | ATTENDANCE_ENTROPY_V4_0 | 熵基科技 | V4.0 | 考勤PUSH通讯协议 V4.0-20210113 |
| `ConsumeProtocolHandler` | CONSUME_ZKTECO_V1_0 | 中控智慧 | V1.0 | 消费PUSH通讯协议 V1.0-20181225 |

---

## 🔄 处理流程

### 标准处理流程

```
1. 接收原始数据（字节数组或十六进制字符串）
   ↓
2. 解析消息（parseMessage）
   - 验证协议头
   - 解析消息类型
   - 解析设备编号
   - 解析业务数据
   ↓
3. 验证消息（validateMessage）
   - 验证消息完整性
   - 验证校验和
   - 验证必填字段
   ↓
4. 处理消息（processMessage）
   - 根据消息类型路由到对应业务处理
   - 调用业务服务API
   - 更新设备状态
   - 记录日志
```

### 消息类型处理

#### 门禁协议（AccessProtocolHandler）

| 消息类型 | 处理逻辑 | 调用的API |
|---------|---------|----------|
| `ACCESS_RECORD` | 处理门禁通行记录 | `POST /api/v1/access/record/create` |
| `DEVICE_STATUS` | 更新设备状态 | `PUT /api/v1/device/status/update` |
| `ALARM_EVENT` | 处理报警事件 | `POST /api/v1/alarm/record/create` |

#### 考勤协议（AttendanceProtocolHandler）

| 消息类型 | 处理逻辑 | 调用的API |
|---------|---------|----------|
| `ATTENDANCE_RECORD` | 处理考勤记录 | `POST /api/v1/attendance/record/create` |
| `DEVICE_STATUS` | 更新设备状态 | `PUT /api/v1/device/status/update` |

#### 消费协议（ConsumeProtocolHandler）

| 消息类型 | 处理逻辑 | 调用的API |
|---------|---------|----------|
| `CONSUME_RECORD` | 处理消费记录 | `POST /api/v1/consume/transaction/execute` |
| `BALANCE_QUERY` | 查询账户余额 | `GET /api/v1/consume/account/balance/user/{userId}` |
| `DEVICE_STATUS` | 更新设备状态 | `PUT /api/v1/device/status/update` |

---

## 💻 使用方法

### 1. 直接使用协议处理器

```java
@Resource
private AccessProtocolHandler accessProtocolHandler;

// 解析消息
ProtocolMessage message = accessProtocolHandler.parseMessage(rawDataBytes);

// 验证消息
if (accessProtocolHandler.validateMessage(message)) {
    // 处理消息
    accessProtocolHandler.processMessage(message, deviceId);
}
```

### 2. 通过MessageRouter使用

```java
@Resource
private MessageRouter messageRouter;

// 自动路由到对应的协议处理器
messageRouter.route(rawDataBytes, deviceId, protocolType);
```

### 3. 通过ProtocolAdapterFactory获取处理器

```java
@Resource
private ProtocolAdapterFactory protocolAdapterFactory;

// 根据协议类型获取处理器
ProtocolHandler handler = protocolAdapterFactory.getHandler("ACCESS_ENTROPY_V4_8");
if (handler != null) {
    ProtocolMessage message = handler.parseMessage(rawDataBytes);
    handler.processMessage(message, deviceId);
}
```

---

## 🧪 测试

### 单元测试

所有协议处理器都有对应的单元测试类：

- `AccessProtocolHandlerTest` - 门禁协议处理器单元测试
- `AttendanceProtocolHandlerTest` - 考勤协议处理器单元测试
- `ConsumeProtocolHandlerTest` - 消费协议处理器单元测试

**测试覆盖范围**：
- 消息解析（正常场景、异常场景）
- 消息验证（有效消息、无效消息）
- 消息处理（各种消息类型）
- 校验和验证
- 协议类型、厂商、版本信息

### 集成测试

`ProtocolIntegrationTest` - 协议集成测试，测试完整的处理流程。

**测试场景**：
- 端到端处理流程
- 多个协议处理器协同工作
- 性能测试（解析1000条消息）
- 异常场景处理

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行单元测试
mvn test -Dtest=*ProtocolHandlerTest

# 运行集成测试
mvn test -Dtest=ProtocolIntegrationTest

# 生成测试覆盖率报告
mvn jacoco:report
```

---

## 📝 开发规范

### 1. 添加新协议处理器

1. **实现ProtocolHandler接口**
   ```java
   @Component
   public class NewProtocolHandler implements ProtocolHandler {
       // 实现所有接口方法
   }
   ```

2. **注册到ProtocolAdapterFactory**
   - 协议处理器使用`@Component`注解，Spring会自动注册
   - 确保`getProtocolType()`返回唯一的协议类型标识

3. **实现核心方法**
   - `parseMessage()` - 根据协议文档解析消息
   - `validateMessage()` - 验证消息完整性和校验和
   - `processMessage()` - 处理业务逻辑，调用业务服务API

4. **编写单元测试**
   - 创建对应的测试类
   - 测试覆盖率≥80%

### 2. 协议解析规范

- **协议头验证**：必须验证协议头，确保消息格式正确
- **字段解析**：根据协议文档定义，正确解析所有字段
- **字节序处理**：注意大端序/小端序的处理
- **异常处理**：解析失败时抛出`ProtocolParseException`

### 3. 校验和验证规范

- **算法实现**：根据协议文档实现正确的校验算法（累加和、CRC等）
- **位置确定**：根据协议文档确定校验和的位置
- **验证失败**：校验和验证失败时返回false，记录警告日志

### 4. 业务处理规范

- **服务调用**：所有业务服务调用必须通过`GatewayServiceClient`
- **错误处理**：服务调用失败时记录错误日志，不抛出异常（避免影响其他消息处理）
- **设备状态**：及时更新设备状态（在线/离线）
- **日志记录**：记录关键操作日志，便于问题排查

---

## ⚠️ 注意事项

### 1. 协议文档依赖

当前实现中的字段映射和校验和算法是基于假设的占位实现。**需要根据实际PDF协议文档完善**：

- [ ] 完善协议解析的字段映射（根据实际PDF文档）
- [ ] 完善校验和验证算法（根据实际PDF文档）

### 2. 测试环境配置

集成测试需要Spring上下文支持，实际运行时需要配置：
- 测试数据库
- Mock网关服务（或配置测试网关）
- 测试设备数据

### 3. 性能考虑

- 协议解析应保持高性能（平均每条消息<10ms）
- 消息处理应异步执行，避免阻塞
- 大量消息处理时考虑批量处理

### 4. 错误处理

- 解析失败：抛出`ProtocolParseException`，记录错误日志
- 验证失败：返回false，记录警告日志
- 处理失败：记录错误日志，不抛出异常（避免影响其他消息）

---

## 📚 相关文档

- [协议兼容性检查报告](./PROTOCOL_COMPATIBILITY_CHECK.md) - 协议兼容性检查结果
- [协议实现总结](./PROTOCOL_IMPLEMENTATION_SUMMARY.md) - 协议实现总结
- [CLAUDE.md](../../../CLAUDE.md) - 项目架构规范

---

## 🔗 API接口清单

### 考勤服务
- `POST /api/v1/attendance/record/create` - 创建考勤记录

### 门禁服务
- `POST /api/v1/access/record/create` - 创建门禁记录

### 消费服务
- `POST /api/v1/consume/transaction/execute` - 执行消费交易
- `GET /api/v1/consume/account/balance/user/{userId}` - 查询账户余额

### 公共服务
- `PUT /api/v1/device/status/update` - 更新设备状态
- `POST /api/v1/alarm/record/create` - 创建报警记录
- `POST /api/v1/audit/log/create` - 创建审计日志

---

**文档维护**: IOE-DREAM Team  
**最后更新**: 2025-01-30

