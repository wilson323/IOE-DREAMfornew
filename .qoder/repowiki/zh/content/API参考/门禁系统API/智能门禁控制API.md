# 智能门禁控制API

<cite>
**本文档引用文件**
- [AccessDeviceService.java](file://documentation\technical\smart-access.md#L658-L1060)
- [TcpDeviceConnection.java](file://documentation\technical\smart-access.md#L1577-L1858)
- [DeviceCommunicationController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-device-service_src_main_java_net_lab1024_sa_device_controller_DeviceCommunicationController.java)
- [GlobalInterlockController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-access-service_src_main_java_net_lab1024_sa_access_advanced_controller_GlobalInterlockController.java)
- [GlobalLinkageController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-access-service_src_main_java_net_lab1024_sa_access_advanced_controller_GlobalLinkageController.java)
- [EvacuationController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-access-service_src_main_java_net_lab1024_sa_access_advanced_controller_EvacuationController.java)
- [功能概述.md](file://documentation\03-业务模块\门禁系统\功能概述.md)
</cite>

## 目录
1. [引言](#引言)
2. [核心控制API](#核心控制api)
3. [联动控制API](#联动控制api)
4. [设备通讯服务交互](#设备通讯服务交互)
5. [执行状态反馈与超时处理](#执行状态反馈与超时处理)
6. [应用场景示例](#应用场景示例)
7. [架构与数据流](#架构与数据流)

## 引言

智能门禁控制API是智慧园区一卡通管理平台的核心组成部分，提供对门禁设备的实时远程控制能力。本API允许管理员或授权系统通过HTTP请求对门禁设备执行开门、关门、锁定、解锁等操作，并支持触发复杂的联动控制场景，如“一键疏散”或“全局布防”。API设计遵循RESTful规范，确保了操作的可靠性和可追溯性。

本API与设备通讯服务紧密集成，通过TCP协议与终端设备建立稳定连接，确保控制指令能够可靠地下发到目标设备。系统具备完善的执行状态反馈机制和超时处理策略，确保操作的可监控性和系统的健壮性。

**Section sources**
- [功能概述.md](file://documentation\03-业务模块\门禁系统\功能概述.md#L1224-L1295)

## 核心控制API

核心控制API提供了对单个门禁设备进行直接操作的能力，是实现远程访问控制的基础。

### 远程开门与关门

`remoteOpenDoor` 和 `remoteCloseDoor` 是两个最基础的控制指令，用于远程操作门禁的开关状态。

```mermaid
sequenceDiagram
participant Client as "客户端"
participant AccessService as "门禁服务"
participant DeviceConn as "设备连接"
participant Device as "门禁设备"
Client->>AccessService : POST /api/access/device/{deviceId}/open
AccessService->>AccessService : 验证设备状态(在线/启用)
AccessService->>DeviceConn : 调用connection.openDoor()
DeviceConn->>Device : 发送"OPEN_DOOR"指令
Device-->>DeviceConn : 确认指令接收
DeviceConn-->>AccessService : 返回执行结果
AccessService->>AccessService : 记录操作日志
AccessService-->>Client : 返回成功响应
```

**Diagram sources**
- [AccessDeviceService.java](file://documentation\technical\smart-access.md#L762-L797)
- [TcpDeviceConnection.java](file://documentation\technical\smart-access.md#L1650-L1652)

**Section sources**
- [AccessDeviceService.java](file://documentation\technical\smart-access.md#L762-L797)
- [TcpDeviceConnection.java](file://documentation\technical\smart-access.md#L1650-L1652)

### 设备锁定与解锁

除了简单的开关门，系统还提供了更高级的锁定功能，用于在特定情况下禁止设备操作。

```mermaid
classDiagram
class GlobalInterlockController {
+lockDevice(deviceId, lockType, lockedBy, reason)
+unlockDevice(deviceId, lockedBy)
+forceUnlockDevice(deviceId, reason, operator)
+getDeviceLockStatus(deviceId)
}
class InterlockRuleService {
+createRule(createDTO)
+getRules(queryDTO)
+updateRule(ruleId, updateDTO)
+deleteRule(ruleId)
}
class GlobalInterlockEngine {
+requestDeviceLock(deviceId, lockType, ...)
+releaseDeviceLock(deviceId, lockedBy)
+forceReleaseDeviceLock(deviceId, reason, operator)
+checkDeviceLockStatus(deviceId)
+processInterlockTrigger(triggerDTO)
}
GlobalInterlockController --> InterlockRuleService : "使用"
GlobalInterlockController --> GlobalInterlockEngine : "依赖"
```

**Diagram sources**
- [GlobalInterlockController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-access-service_src_main_java_net_lab1024_sa_access_advanced_controller_GlobalInterlockController.java#L56-L152)

**Section sources**
- [GlobalInterlockController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-access-service_src_main_java_net_lab1024_sa_access_advanced_controller_GlobalInterlockController.java#L56-L152)

## 联动控制API

联动控制API允许通过一个指令触发一系列预定义的自动化操作，实现复杂的业务场景。

### 全局布防与撤防

通过`GlobalLinkageController`，可以触发预设的联动规则，实现“一键布防”或“一键撤防”。

```mermaid
flowchart TD
A[触发联动] --> B{检查联动规则}
B --> |规则存在且启用| C[执行联动动作]
C --> D["锁定: 指定区域的所有门禁"]
C --> E["启动: 指定摄像头的录像"]
C --> F["发送: 通知给安保人员"]
C --> G["更新: 系统状态为“布防中”"]
B --> |规则不存在或禁用| H[返回错误]
D --> I[所有动作执行成功?]
E --> I
F --> I
G --> I
I --> |是| J[返回成功]
I --> |否| K[记录失败日志]
K --> L[返回部分成功]
```

**Diagram sources**
- [GlobalLinkageController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-access-service_src_main_java_net_lab1024_sa_access_advanced_controller_GlobalLinkageController.java#L52-L80)

**Section sources**
- [GlobalLinkageController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-access-service_src_main_java_net_lab1024_sa_access_advanced_controller_GlobalLinkageController.java#L52-L80)

### 一键疏散

`EvacuationController` 提供了紧急情况下的疏散管理功能，可以一键开启所有安全通道。

```mermaid
sequenceDiagram
participant Admin as "管理员"
participant EvacuationController as "疏散控制器"
participant EvacuationService as "疏散服务"
participant InterlockEngine as "互锁引擎"
participant Device as "门禁设备"
Admin->>EvacuationController : POST /api/access/evacuation/events/trigger
EvacuationController->>EvacuationService : triggerEvacuation(triggerDTO)
EvacuationService->>InterlockEngine : processInterlockTrigger(triggerDTO)
InterlockEngine->>InterlockEngine : 评估疏散规则
InterlockEngine->>Device : 发送"OPEN_DOOR"指令到所有安全门
Device-->>InterlockEngine : 确认
InterlockEngine-->>EvacuationService : 返回执行结果
EvacuationService-->>EvacuationController : 返回结果
EvacuationController-->>Admin : 返回成功响应
```

**Diagram sources**
- [EvacuationController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-access-service_src_main_java_net_lab1024_sa_access_advanced_controller_EvacuationController.java#L113-L116)

**Section sources**
- [EvacuationController.java](file://restful_refactor_backup_20251202_014224\microservices_ioedream-access-service_src_main_java_net_lab1024_sa_access_advanced_controller_EvacuationController.java#L113-L116)

## 设备通讯服务交互

智能门禁控制API通过设备通讯服务与物理终端设备进行交互，确保指令的可靠传输。

### TCP协议连接

系统使用TCP协议与门禁设备建立长连接，通过`TcpDeviceConnection`类实现。

```mermaid
classDiagram
class DeviceConnection {
<<interface>>
+connect() boolean
+disconnect() void
+isConnected() boolean
+openDoor() boolean
+closeDoor() boolean
+restart() boolean
+syncTime(dateTime) boolean
+grantAccess(personId, startTime, endTime) boolean
+revokeAccess(personId) boolean
+getDeviceStatus() DeviceStatus
+sendCommand(command, params) boolean
+setEventListener(listener) void
}
class TcpDeviceConnection {
-socket Socket
-reader BufferedReader
-writer BufferedWriter
-connected AtomicBoolean
-heartbeatExecutor ScheduledExecutorService
+connect() boolean
+disconnect() void
+isConnected() boolean
+openDoor() boolean
+closeDoor() boolean
+sendCommand(command, params) boolean
+startMessageReceiver() void
+startHeartbeat() void
+handleMessage(message) void
}
class DeviceProtocolFactory {
+createConnection(device) DeviceConnection
}
DeviceProtocolFactory --> TcpDeviceConnection : "创建"
TcpDeviceConnection ..|> DeviceConnection : "实现"
```

**Diagram sources**
- [TcpDeviceConnection.java](file://documentation\technical\smart-access.md#L1577-L1858)

**Section sources**
- [TcpDeviceConnection.java](file://documentation\technical\smart-access.md#L1577-L1858)

### 指令下发流程

控制指令从API到设备的完整流程如下：

```mermaid
flowchart LR
A[HTTP API请求] --> B[门禁服务验证]
B --> C{设备是否在线?}
C --> |是| D[获取设备连接]
C --> |否| E[返回错误]
D --> F[通过TCP连接发送指令]
F --> G[设备接收并执行]
G --> H[设备返回响应]
H --> I[服务记录日志]
I --> J[返回API响应]
```

**Section sources**
- [AccessDeviceService.java](file://documentation\technical\smart-access.md#L777-L786)
- [TcpDeviceConnection.java](file://documentation\technical\smart-access.md#L1700-L1712)

## 执行状态反馈与超时处理

系统提供了完善的执行状态反馈和超时处理机制，确保操作的可监控性和可靠性。

### 状态反馈

系统通过多种方式提供状态反馈：
1.  **API响应**: HTTP请求的返回体包含操作结果。
2.  **设备状态查询**: 可通过`GET /api/device/{deviceId}/status`查询设备的实时状态。
3.  **事件监听**: `DeviceConnection`接口支持`DeviceEventListener`，可以接收来自设备的通行事件、告警等实时消息。

### 超时处理

系统在多个层面实现了超时处理：
1.  **连接超时**: 在建立TCP连接时设置5秒超时。
2.  **指令超时**: 虽然代码中未显式设置，但业务逻辑中通过`isConnected()`检查连接状态，间接处理了长时间无响应的情况。
3.  **心跳机制**: 通过`startHeartbeat()`方法每30秒发送一次心跳，如果连续多次未收到响应，则认为设备离线，断开连接。

**Section sources**
- [TcpDeviceConnection.java](file://documentation\technical\smart-access.md#L1602-L1603)
- [TcpDeviceConnection.java](file://documentation\technical\smart-access.md#L1764-L1765)

## 应用场景示例

### 远程为访客开门

以下是一个通过API远程为访客打开特定门区门禁，并在30秒后自动关闭的完整场景示例。

```mermaid
sequenceDiagram
participant Operator as "操作员"
participant AccessAPI as "门禁API"
participant Device as "门禁设备"
Operator->>AccessAPI : 1. 调用remoteOpenDoor(deviceId=101)
AccessAPI->>AccessAPI : 2. 验证设备在线且启用
AccessAPI->>Device : 3. 发送"OPEN_DOOR"指令
Device-->>AccessAPI : 4. 确认指令接收
AccessAPI-->>Operator : 5. 返回"开门成功"
Note over Device,Operator : 门已开启，访客进入
AccessAPI->>AccessAPI : 6. 启动30秒倒计时
AccessAPI->>Device : 7. 30秒后发送"CLOSE_DOOR"指令
Device-->>AccessAPI : 8. 确认指令接收
AccessAPI->>AccessAPI : 9. 记录"自动关门"日志
```

**Section sources**
- [AccessDeviceService.java](file://documentation\technical\smart-access.md#L762-L797)
- [AccessDeviceService.java](file://documentation\technical\smart-access.md#L803-L829)

## 架构与数据流

```mermaid
graph TB
subgraph "前端/客户端"
A[Web管理端]
B[移动App]
end
subgraph "API网关"
C[Gateway Service]
end
subgraph "门禁服务"
D[Access Service]
E[GlobalInterlockEngine]
F[GlobalLinkageEngine]
G[EvacuationService]
end
subgraph "设备通讯服务"
H[DeviceComm Service]
I[TcpDeviceConnection]
J[DeviceConnection Pool]
end
subgraph "物理设备"
K[门禁控制器]
L[人脸识别终端]
M[电锁]
end
A --> C
B --> C
C --> D
D --> E
D --> F
D --> G
D --> H
H --> I
I --> J
J --> K
J --> L
J --> M
```

**Diagram sources**
- [功能概述.md](file://documentation\03-业务模块\门禁系统\功能概述.md#L114-L152)

**Section sources**
- [功能概述.md](file://documentation\03-业务模块\门禁系统\功能概述.md#L114-L152)