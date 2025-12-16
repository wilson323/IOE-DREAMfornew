# API参考

<cite>
**本文档引用的文件**
- [ResponseDTO.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\domain\ResponseDTO.java)
- [LoginController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\login\controller\LoginController.java)
- [sa-base.yaml](file://smart-admin-api-java17-springboot3\sa-base\src\main\resources\dev\sa-base.yaml)
- [UserErrorCode.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\code\UserErrorCode.java)
- [SystemErrorCode.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\code\SystemErrorCode.java)
- [UnexpectedErrorCode.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\code\UnexpectedErrorCode.java)
- [AdminApiEncryptController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\support\AdminApiEncryptController.java)
- [axios.js](file://smart-admin-web-javascript\src\lib\axios.js)
- [api-encrypt-api.js](file://smart-admin-web-javascript\src\api\support\api-encrypt-api.js)
</cite>

## 目录
1. [简介](#简介)
2. [统一响应格式](#统一响应格式)
3. [认证机制](#认证机制)
4. [API错误码体系](#api错误码体系)
5. [API加密机制](#api加密机制)
6. [API版本管理](#api版本管理)
7. [API测试指南](#api测试指南)
8. [API使用示例](#api使用示例)
9. [最佳实践](#最佳实践)

## 简介

本API参考文档为前后端开发人员提供了完整的接口说明，详细描述了所有RESTful API端点的使用方法。文档涵盖了HTTP方法、URL路径、请求参数、请求体结构、响应格式和状态码等关键信息。

系统采用SpringBoot 3和Vue3技术栈，后端基于Sa-Token实现认证授权，前端使用Axios进行HTTP请求。API设计遵循RESTful规范，通过Swagger提供接口文档，确保前后端开发的高效协作。

**API基础信息**
- **基础URL**: `http://your-domain.com/api`
- **协议**: HTTPS
- **内容类型**: `application/json`
- **字符编码**: UTF-8

**Section sources**
- [LoginController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\login\controller\LoginController.java)

## 统一响应格式

所有API接口返回统一的响应格式，通过`ResponseDTO<T>`类实现，确保前后端数据交互的一致性和可预测性。

### 响应结构

```json
{
  "code": 0,
  "level": "user",
  "msg": "操作成功",
  "ok": true,
  "data": {},
  "dataType": 1
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | Integer | 是 | 返回码，0表示成功，非0表示错误 |
| level | String | 否 | 错误级别：system(系统级)、user(用户级)、unexpected(未预期) |
| msg | String | 是 | 返回消息，描述操作结果 |
| ok | Boolean | 是 | 操作是否成功 |
| data | T | 否 | 返回数据，泛型类型 |
| dataType | Integer | 否 | 数据类型：1-正常数据，2-加密数据 |

### 响应示例

**成功响应**
```json
{
  "code": 0,
  "msg": "操作成功",
  "ok": true,
  "data": {
    "id": 1,
    "name": "张三",
    "email": "zhangsan@example.com"
  }
}
```

**失败响应**
```json
{
  "code": 30001,
  "level": "user",
  "msg": "参数错误",
  "ok": false,
  "data": null
}
```

**Section sources**
- [ResponseDTO.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\domain\ResponseDTO.java)

## 认证机制

系统采用Sa-Token框架实现认证授权，通过JWT令牌进行用户身份验证。

### 认证流程

```mermaid
sequenceDiagram
participant 前端 as 前端应用
participant 后端 as 后端API
participant 数据库 as 数据库
前端->>后端 : POST /login {username, password}
后端->>数据库 : 验证用户凭证
数据库-->>后端 : 用户信息
后端->>后端 : 生成JWT令牌
后端-->>前端 : ResponseDTO {token, userInfo}
前端->>前端 : 存储令牌到localStorage
前端->>后端 : 后续请求携带Authorization头
后端->>后端 : 验证令牌有效性
后端-->>前端 : 返回请求数据
```

**Diagram sources**
- [LoginController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\login\controller\LoginController.java)

### 令牌传递方式

在每次请求的HTTP头中携带JWT令牌：

```
Authorization: Bearer <token>
```

### 配置参数

| 配置项 | 值 | 说明 |
|--------|-----|------|
| token名称 | Authorization | 令牌头名称 |
| token前缀 | Bearer | 令牌前缀 |
| 有效期 | 2592000秒(30天) | 令牌有效时间 |
| 自动续签 | true | 是否开启自动续签 |
| 多地登录 | false | 是否允许多地同时登录 |

**Section sources**
- [sa-base.yaml](file://smart-admin-api-java17-springboot3\sa-base\src\main\resources\dev\sa-base.yaml)

## API错误码体系

系统定义了完善的错误码体系，分为系统级、用户级和未预期错误三类，便于快速定位问题。

### 错误码分类

| 错误类型 | 范围 | 说明 |
|----------|------|------|
| 系统错误 | 10001-20000 | 系统级错误，需要高度重视 |
| 未预期错误 | 20001-30000 | 不可能发生的情况，需要紧急处理 |
| 用户错误 | 30001-40000 | 用户操作引起的错误 |

### 用户级错误码

| 错误码 | 消息 | 说明 |
|--------|------|------|
| 30001 | 参数错误 | 请求参数不符合要求 |
| 30002 | 数据不存在 | 请求的数据不存在 |
| 30003 | 数据已存在 | 数据已存在，无法重复创建 |
| 30004 | 操作太快 | 操作过于频繁，请稍后再试 |
| 30005 | 无权限访问 | 当前用户没有访问权限 |
| 30007 | 登录失效 | 未登录或登录已过期 |
| 30008 | 用户状态异常 | 用户账户状态异常 |
| 30010 | 登录被锁定 | 连续登录失败被锁定 |
| 30012 | 长时间未操作 | 长时间未操作需要重新登录 |

### 系统级错误码

| 错误码 | 消息 | 说明 |
|--------|------|------|
| 10001 | 系统似乎出现了点小问题 | 系统内部错误 |

### 未预期错误码

| 错误码 | 消息 | 说明 |
|--------|------|------|
| 20001 | 业务繁忙，请稍后重试 | 业务处理繁忙 |
| 20002 | 付款单id发生了异常 | ID生成异常 |

**Section sources**
- [UserErrorCode.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\code\UserErrorCode.java)
- [SystemErrorCode.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\code\SystemErrorCode.java)
- [UnexpectedErrorCode.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\code\UnexpectedErrorCode.java)

## API加密机制

系统支持接口数据加密，确保敏感信息传输的安全性。

### 加密方式

支持两种加密算法：
- **SM4**: 国密算法，符合国内安全标准
- **AES**: 国际标准加密算法

### 加密注解

| 注解 | 说明 |
|------|------|
| @ApiEncrypt | 标记返回数据需要加密 |
| @ApiDecrypt | 标记请求参数需要解密 |

### 前端实现

```javascript
// 加密请求
export const postEncryptRequest = (url, data) => {
  return request({
    data: { encryptData: encryptData(data) },
    url,
    method: 'post',
  });
};

// 解密响应
if (response.data.dataType === DATA_TYPE_ENUM.ENCRYPT.value) {
  let decryptStr = decryptData(response.data.data);
  response.data.data = JSON.parse(decryptStr);
}
```

### 后端实现

```java
@RestController
public class AdminApiEncryptController extends SupportBaseController {
    
    @ApiDecrypt
    @PostMapping("/apiEncrypt/testRequestEncrypt")
    public ResponseDTO<JweForm> testRequestEncrypt(@RequestBody @Valid JweForm form) {
        return ResponseDTO.ok(form);
    }
    
    @ApiEncrypt
    @PostMapping("/apiEncrypt/testResponseEncrypt")
    public ResponseDTO<JweForm> testResponseEncrypt(@RequestBody @Valid JweForm form) {
        return ResponseDTO.ok(form);
    }
}
```

**Section sources**
- [AdminApiEncryptController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\support\AdminApiEncryptController.java)
- [axios.js](file://smart-admin-web-javascript\src\lib\axios.js)
- [api-encrypt-api.js](file://smart-admin-web-javascript\src\api\support\api-encrypt-api.js)

## API版本管理

系统采用URL路径进行版本管理，确保API的向后兼容性。

### 版本策略

- **版本格式**: `/api/v{version}/{endpoint}`
- **当前版本**: v3
- **版本升级**: 重大变更时升级主版本号
- **向后兼容**: 保证旧版本API在一定时间内可用

### 版本迁移

当API发生不兼容变更时：
1. 保留旧版本API
2. 发布新版本API
3. 提供迁移文档
4. 逐步废弃旧版本

## API测试指南

提供完整的API测试指南，帮助开发者快速验证接口功能。

### 使用Postman测试

1. **导入集合**: 将API文档导出为Postman集合
2. **设置环境**: 配置基础URL和认证令牌
3. **执行测试**: 运行预设的测试用例
4. **查看结果**: 检查响应状态码和数据

### 测试用例示例

**登录接口测试**
```json
{
  "name": "测试登录接口",
  "request": {
    "method": "POST",
    "url": "/login",
    "body": {
      "username": "admin",
      "password": "123456"
    }
  },
  "tests": [
    "状态码应为200",
    "响应中应包含token",
    "响应中应包含用户信息"
  ]
}
```

### 前端测试

使用Jest进行前端API测试：

```javascript
test('测试登录接口', async () => {
  const response = await loginApi.login({
    username: 'admin',
    password: '123456'
  });
  
  expect(response.code).toBe(0);
  expect(response.data.token).toBeDefined();
  expect(response.data.userInfo).toBeDefined();
});
```

## API使用示例

提供详细的API使用示例，涵盖成功和失败场景。

### 登录接口

**请求**
```
POST /login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**成功响应**
```json
{
  "code": 0,
  "msg": "操作成功",
  "ok": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "role": "admin"
    }
  }
}
```

**失败响应（参数错误）**
```json
{
  "code": 30001,
  "level": "user",
  "msg": "参数错误",
  "ok": false,
  "data": null
}
```

### 获取用户信息接口

**请求**
```
GET /user/info
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**成功响应**
```json
{
  "code": 0,
  "msg": "操作成功",
  "ok": true,
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "createTime": "2023-01-01 10:00:00"
  }
}
```

**失败响应（登录失效）**
```json
{
  "code": 30007,
  "level": "user",
  "msg": "您还未登录或登录失效，请重新登录！",
  "ok": false,
  "data": null
}
```

## 最佳实践

提供API集成的最佳实践建议，帮助开发者高效使用API。

### 错误处理

```javascript
async function queryData() {
  try {
    const res = await employeeApi.queryAll();
    dataList.value = res.data;
  } catch (e) {
    // 记录错误日志
    smartSentry.captureError(e);
    // 不需要手动显示错误信息，axios拦截器已处理
  } finally {
    loading.value = false;
  }
}
```

### 重试机制

对于网络不稳定的情况，建议实现重试机制：

```javascript
async function requestWithRetry(apiCall, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await apiCall();
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
    }
  }
}
```

### 缓存策略

合理使用缓存提高性能：

```javascript
// 实现简单的内存缓存
const cache = new Map();
const CACHE_TTL = 5 * 60 * 1000; // 5分钟

async function getCachedData(key, apiCall) {
  const cached = cache.get(key);
  if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
    return cached.data;
  }
  
  const data = await apiCall();
  cache.set(key, { data, timestamp: Date.now() });
  return data;
}
```

### 安全建议

1. **敏感数据加密**: 对密码、身份证等敏感信息进行加密传输
2. **权限控制**: 严格验证用户权限，防止越权访问
3. **输入验证**: 对所有输入参数进行严格验证
4. **日志记录**: 记录关键操作日志，便于审计和排查问题