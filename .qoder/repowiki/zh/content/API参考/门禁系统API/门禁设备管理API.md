# 门禁设备管理API

<cite>
**本文档引用文件**   
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java)
- [AccessDeviceService.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\AccessDeviceService.java)
- [AccessDeviceServiceImpl.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\impl\AccessDeviceServiceImpl.java)
- [AccessDeviceAddForm.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceAddForm.java)
- [AccessDeviceUpdateForm.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceUpdateForm.java)
- [AccessDeviceQueryForm.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceQueryForm.java)
- [AccessDeviceVO.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\vo\AccessDeviceVO.java)
- [ResponseDTO.java](file://microservices\microservices-common\src\main\java\net\lab1024\sa\common\dto\ResponseDTO.java)
</cite>

## 目录
1. [API概览](#api概览)
2. [设备增删改查](#设备增删改查)
3. [设备状态管理](#设备状态管理)
4. [设备查询与分页](#设备查询与分页)
5. [请求体与响应体结构](#请求体与响应体结构)
6. [错误码与异常处理](#错误码与异常处理)
7. [设备状态变更事件处理](#设备状态变更事件处理)
8. [使用示例](#使用示例)

## API概览

门禁设备管理API提供了一套完整的设备生命周期管理功能，包括设备的增删改查、状态同步、配置管理等操作。API严格遵循RESTful规范，使用统一的响应格式ResponseDTO，确保前后端交互的一致性和可预测性。

所有API端点均位于`/api/v1/access/device`路径下，需要`ACCESS_MANAGER`角色权限才能访问。API支持JSON格式的请求和响应，使用标准的HTTP方法（GET、POST、PUT、DELETE）来执行相应的操作。

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java#L48-L49)

## 设备增删改查

### 创建设备

通过POST请求创建新的门禁设备。

**HTTP方法**: POST  
**URL路径**: `/api/v1/access/device/add`  
**认证要求**: 需要`ACCESS_MANAGER`角色  
**请求体**: `AccessDeviceAddForm`对象

**请求参数说明**:
- `deviceName`: 设备名称，必填，最大长度100字符
- `deviceCode`: 设备编号，必填，最大长度50字符，必须唯一
- `areaId`: 区域ID，必填，外键引用
- `ipAddress`: IP地址，必填，最大长度50字符
- `port`: 端口号，必填
- `enabledFlag`: 启用标志，0-禁用，1-启用，默认为1
- `remark`: 备注，可选，最大长度500字符

**成功响应**: 返回新创建设备的ID  
**失败响应**: 返回相应的错误码和消息

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java#L149-L161)
- [AccessDeviceAddForm.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceAddForm.java)

### 更新设备

通过PUT请求更新现有设备信息。

**HTTP方法**: PUT  
**URL路径**: `/api/v1/access/device/update`  
**认证要求**: 需要`ACCESS_MANAGER`角色  
**请求体**: `AccessDeviceUpdateForm`对象

**请求参数说明**:
- `deviceId`: 设备ID，主键，必填
- `deviceName`: 设备名称，必填，最大长度100字符
- `deviceCode`: 设备编号，必填，最大长度50字符，在更新时必须保证唯一性（排除当前设备）
- `areaId`: 区域ID，必填，外键引用
- `ipAddress`: IP地址，必填，最大长度50字符
- `port`: 端口号，必填
- `enabledFlag`: 启用标志，0-禁用，1-启用
- `remark`: 备注，可选，最大长度500字符

**成功响应**: 返回`true`表示更新成功  
**失败响应**: 返回相应的错误码和消息

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java#L169-L181)
- [AccessDeviceUpdateForm.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceUpdateForm.java)

### 删除设备

通过DELETE请求删除指定设备（软删除）。

**HTTP方法**: DELETE  
**URL路径**: `/api/v1/access/device/{deviceId}`  
**认证要求**: 需要`ACCESS_MANAGER`角色  
**路径参数**: 
- `deviceId`: 设备ID，必填

**成功响应**: 返回`true`表示删除成功  
**失败响应**: 返回相应的错误码和消息

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java#L189-L202)

### 查询设备详情

通过GET请求获取指定设备的详细信息。

**HTTP方法**: GET  
**URL路径**: `/api/v1/access/device/{deviceId}`  
**认证要求**: 需要`ACCESS_MANAGER`角色  
**路径参数**: 
- `deviceId`: 设备ID，必填

**成功响应**: 返回`AccessDeviceVO`对象  
**失败响应**: 返回相应的错误码和消息

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java#L128-L141)

## 设备状态管理

### 更新设备状态

通过POST请求更新设备的启用状态。

**HTTP方法**: POST  
**URL路径**: `/api/v1/access/device/status/update`  
**认证要求**: 需要`ACCESS_MANAGER`角色  
**请求参数**:
- `deviceId`: 设备ID，必填
- `status`: 状态值，必填，只能为0（禁用）或1（启用）

**成功响应**: 返回`true`表示状态更新成功  
**失败响应**: 返回相应的错误码和消息

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java#L211-L225)

## 设备查询与分页

### 分页查询设备

通过GET请求分页查询门禁设备列表。

**HTTP方法**: GET  
**URL路径**: `/api/v1/access/device/query`  
**认证要求**: 需要`ACCESS_MANAGER`角色  
**查询参数**:
- `pageNum`: 页码，从1开始，默认1
- `pageSize`: 每页大小，默认20
- `keyword`: 关键词，用于搜索设备名称或设备编号
- `areaId`: 区域ID，用于筛选特定区域的设备
- `deviceStatus`: 设备状态，可选值：ONLINE（在线）、OFFLINE（离线）、FAULT（故障）
- `enabledFlag`: 启用状态，1-启用，0-禁用

**成功响应**: 返回`PageResult<AccessDeviceVO>`对象，包含分页信息和设备列表  
**失败响应**: 返回相应的错误码和消息

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java#L74-L120)
- [AccessDeviceQueryForm.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceQueryForm.java)

## 请求体与响应体结构

### 请求体结构

#### AccessDeviceAddForm (创建设备)
```json
{
  "deviceName": "主入口门禁",
  "deviceCode": "ACC001",
  "areaId": 4001,
  "ipAddress": "192.168.1.100",
  "port": 8080,
  "enabledFlag": 1,
  "remark": "主入口人脸识别门禁"
}
```

#### AccessDeviceUpdateForm (更新设备)
```json
{
  "deviceId": 1001,
  "deviceName": "主入口门禁",
  "deviceCode": "ACC001",
  "areaId": 4001,
  "ipAddress": "192.168.1.100",
  "port": 8080,
  "enabledFlag": 1,
  "remark": "主入口人脸识别门禁"
}
```

#### AccessDeviceQueryForm (查询设备)
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "keyword": "门禁",
  "areaId": 4001,
  "deviceStatus": "ONLINE",
  "enabledFlag": 1
}
```

### 响应体结构

#### AccessDeviceVO (设备详情)
```json
{
  "deviceId": 1001,
  "deviceName": "主入口门禁",
  "deviceCode": "ACC001",
  "deviceType": "ACCESS",
  "areaId": 4001,
  "areaName": "主楼一层",
  "ipAddress": "192.168.1.100",
  "port": 8080,
  "deviceStatus": "ONLINE",
  "enabledFlag": 1,
  "lastOnlineTime": "2025-01-30T10:30:00",
  "createTime": "2025-01-30T09:00:00",
  "updateTime": "2025-01-30T10:30:00"
}
```

#### 统一响应格式 ResponseDTO
所有API响应都包装在`ResponseDTO`对象中，确保统一的响应结构：
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {}
}
```
- `success`: 布尔值，表示操作是否成功
- `code`: 字符串，错误码或成功码
- `message`: 字符串，描述信息
- `data`: 任意类型，实际返回的数据

**本节来源**
- [AccessDeviceAddForm.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceAddForm.java)
- [AccessDeviceUpdateForm.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceUpdateForm.java)
- [AccessDeviceQueryForm.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceQueryForm.java)
- [AccessDeviceVO.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\vo\AccessDeviceVO.java)
- [ResponseDTO.java](file://microservices\microservices-common\src\main\java\net\lab1024\sa\common\dto\ResponseDTO.java)

## 错误码与异常处理

API使用统一的错误码系统来处理各种异常情况。以下是常见的错误码及其含义：

| 错误码 | HTTP状态 | 描述 |
|--------|----------|------|
| SUCCESS | 200 | 操作成功 |
| QUERY_DEVICES_ERROR | 500 | 查询设备失败 |
| DEVICE_NOT_FOUND | 404 | 设备不存在 |
| DEVICE_TYPE_ERROR | 400 | 设备类型不匹配 |
| ADD_DEVICE_ERROR | 500 | 添加设备失败 |
| DEVICE_CODE_EXISTS | 400 | 设备编号已存在 |
| AREA_NOT_FOUND | 404 | 区域不存在 |
| UPDATE_DEVICE_ERROR | 500 | 更新设备失败 |
| DELETE_DEVICE_ERROR | 500 | 删除设备失败 |
| UPDATE_DEVICE_STATUS_ERROR | 500 | 更新设备状态失败 |
| INVALID_STATUS | 400 | 状态值无效 |
| PARAM_ERROR | 400 | 参数错误 |

异常处理遵循以下原则：
1. 所有异常都会被Controller捕获并转换为统一的ResponseDTO格式
2. 敏感的异常信息不会暴露给客户端，只返回通用的错误消息
3. 详细的错误日志会记录在服务端，便于问题排查

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java)
- [AccessDeviceServiceImpl.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\impl\AccessDeviceServiceImpl.java)

## 设备状态变更事件处理

设备状态变更事件的处理流程如下：

1. **请求接收**: Controller接收到更新设备状态的POST请求
2. **权限验证**: 通过`@PreAuthorize("hasRole('ACCESS_MANAGER')")`注解验证用户权限
3. **参数验证**: 使用`@RequestParam`和`@NotNull`注解验证请求参数
4. **业务处理**: 调用`AccessDeviceService.updateDeviceStatus()`方法
5. **数据库操作**: 
   - 查询设备是否存在
   - 验证状态值的有效性（只能为0或1）
   - 更新设备的`enabledFlag`字段
   - 执行数据库更新操作
6. **结果返回**: 根据操作结果返回相应的ResponseDTO

对于异常情况的处理：
- **设备注册失败**: 当添加设备时，如果设备编号已存在，会返回`DEVICE_CODE_EXISTS`错误码
- **心跳超时**: 系统会定期检查设备的最后在线时间，如果超过预设阈值，则自动将设备状态更新为`OFFLINE`
- **网络异常**: 当无法与设备通信时，会记录错误日志并尝试重连，同时更新设备状态为`FAULT`

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java#L211-L225)
- [AccessDeviceServiceImpl.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\impl\AccessDeviceServiceImpl.java#L298-L332)

## 使用示例

### 创建新设备

```http
POST /api/v1/access/device/add HTTP/1.1
Content-Type: application/json
Authorization: Bearer <token>

{
  "deviceName": "东门门禁",
  "deviceCode": "ACC002",
  "areaId": 4002,
  "ipAddress": "192.168.1.101",
  "port": 8080,
  "enabledFlag": 1,
  "remark": "东门人脸识别门禁"
}
```

**成功响应**:
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "操作成功",
  "data": 1002
}
```

### 批量导入设备

虽然API没有直接的批量导入端点，但可以通过循环调用创建设备API来实现批量导入：

```python
import requests

devices = [
    {"deviceName": "西门门禁", "deviceCode": "ACC003", "areaId": 4003, "ipAddress": "192.168.1.102", "port": 8080},
    {"deviceName": "南门门禁", "deviceCode": "ACC004", "areaId": 4004, "ipAddress": "192.168.1.103", "port": 8080}
]

for device in devices:
    response = requests.post(
        "http://localhost:8080/api/v1/access/device/add",
        json=device,
        headers={"Authorization": "Bearer <token>"}
    )
    if response.json()["success"]:
        print(f"设备 {device['deviceName']} 创建成功，ID: {response.json()['data']}")
    else:
        print(f"设备 {device['deviceName']} 创建失败: {response.json()['message']}")
```

### 获取设备实时状态

```http
GET /api/v1/access/device/1001 HTTP/1.1
Authorization: Bearer <token>
```

**成功响应**:
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {
    "deviceId": 1001,
    "deviceName": "主入口门禁",
    "deviceCode": "ACC001",
    "deviceType": "ACCESS",
    "areaId": 4001,
    "areaName": "主楼一层",
    "ipAddress": "192.168.1.100",
    "port": 8080,
    "deviceStatus": "ONLINE",
    "enabledFlag": 1,
    "lastOnlineTime": "2025-01-30T10:30:00",
    "createTime": "2025-01-30T09:00:00",
    "updateTime": "2025-01-30T10:30:00"
  }
}
```

### 远程重启设备

远程重启设备可以通过先禁用再启用设备来实现：

```http
POST /api/v1/access/device/status/update HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

deviceId=1001&status=0
```

等待几秒后：

```http
POST /api/v1/access/device/status/update HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

deviceId=1001&status=1
```

**本节来源**
- [AccessDeviceController.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java)
- [AccessDeviceServiceImpl.java](file://microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\impl\AccessDeviceServiceImpl.java)