# IOE-DREAM API 文档使用指南

**📅 创建时间**: 2025-12-20
**👥 目标用户**: 前端开发人员、第三方集成开发人员、API测试人员
**🎯 使用目标**: 快速掌握IOE-DREAM平台API的使用方法

---

## 🚀 快速开始

### 第一步：访问Swagger UI

1. **启动服务**：确保相关微服务已启动
2. **选择服务**：根据需要访问对应服务的Swagger UI
3. **浏览文档**：在浏览器中打开API文档页面

**示例** - 访问用户管理服务API：
```
http://localhost:8088/swagger-ui/index.html
```

### 第二步：获取认证Token

1. **调用登录接口**：
   ```
   POST /api/v1/auth/login
   ```

2. **请求体示例**：
   ```json
   {
     "username": "admin",
     "password": "admin123",
     "captchaKey": "captcha:uuid",
     "captchaCode": "1234"
   }
   ```

3. **获取Token**：
   ```json
   {
     "code": 200,
     "data": {
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "refreshToken": "refresh_token_here",
       "expiresIn": 7200
     }
   }
   ```

4. **设置请求头**：
   ```
   Authorization: Bearer {token}
   ```

---

## 📖 Swagger UI 使用说明

### 1. 界面介绍

**顶部导航栏**：
- **Tags**: 按模块分类的API接口
- **Servers**: 环境切换（开发/测试/生产）
- **Authorization**: Token认证配置

**接口列表**：
- **路径**: API接口路径
- **方法**: HTTP方法（GET/POST/PUT/DELETE）
- **描述**: 接口功能说明

### 2. 接口测试

**Try it out**:
1. 点击接口右侧的"Try it out"按钮
2. 填写请求参数
3. 点击"Execute"执行测试
4. 查看响应结果

**参数说明**：
- **Required**: 必填参数，标记为红色星号(*)
- **Optional**: 可选参数
- **Schema**: 参数结构和类型说明

---

## 🔐 认证机制详解

### Token认证流程

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant Auth as 认证服务
    participant Gateway as 网关

    Client->>Gateway: 获取验证码
    Gateway->>Auth: /auth/login/getCaptcha
    Auth-->>Gateway: 返回验证码
    Gateway-->>Client: 验证码信息

    Client->>Gateway: 用户登录
    Gateway->>Auth: /auth/login
    Auth-->>Gateway: 返回Token
    Gateway-->>Client: 登录成功

    Note over Client: 后续请求携带Token
    Client->>Gateway: API请求 + Authorization: Bearer Token
    Gateway->>Auth: 验证Token
    Auth-->>Gateway: Token有效
    Gateway-->>Client: 返回API数据
```

### Token使用方法

**HTTP Header设置**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**JavaScript示例**:
```javascript
// 设置axios默认headers
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

// 或者在单个请求中设置
axios.get('/api/v1/user/query', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### Token刷新

当Token即将过期时，可以使用refreshToken获取新Token：

```javascript
POST /api/v1/auth/refresh
{
  "refreshToken": "refresh_token_here"
}
```

---

## 📋 通用数据格式

### 统一响应格式

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  },
  "timestamp": 1642123456789
}
```

**错误响应**:
```json
{
  "code": "USER_NOT_FOUND",
  "message": "用户不存在",
  "data": null,
  "timestamp": 1642123456789
}
```

### 分页响应格式

**请求参数**:
```json
{
  "pageNum": 1,        // 页码，从1开始
  "pageSize": 20,      // 每页大小
  "keyword": "搜索关键词", // 可选
  "sortField": "createTime", // 排序字段
  "sortOrder": "desc"  // 排序方向
}
```

**响应格式**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "username": "admin",
        "createTime": "2025-12-20T10:00:00"
      }
    ],
    "total": 100,         // 总记录数
    "pageNum": 1,         // 当前页码
    "pageSize": 20,       // 每页大小
    "pages": 5            // 总页数
  }
}
```

---

## 🔧 常见接口使用示例

### 1. 用户查询

**请求**:
```http
GET /api/v1/user/query?pageNum=1&pageSize=20&keyword=admin
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "userId": 1,
        "username": "admin",
        "realName": "管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "status": 1,
        "createTime": "2025-12-20T10:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  }
}
```

### 2. 门禁记录查询

**请求**:
```http
GET /api/v1/access/record/query?pageNum=1&pageSize=20&startDate=2025-12-20&endDate=2025-12-20
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "recordId": "rec_123456",
        "userId": 1,
        "username": "admin",
        "deviceId": "dev_001",
        "deviceName": "主门禁",
        "areaId": 1001,
        "areaName": "A栋1楼",
        "passTime": "2025-12-20T10:30:00",
        "passType": "CARD",
        "passResult": "SUCCESS",
        "photoUrl": "http://domain.com/photos/pass_001.jpg"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  }
}
```

### 3. 考勤统计查询

**请求**:
```http
GET /api/v1/attendance/statistics?startDate=2025-12-01&endDate=2025-12-20&userId=1
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "admin",
    "workDays": 15,
    "actualDays": 14,
    "leaveDays": 1,
    "lateCount": 2,
    "earlyCount": 1,
    "absentCount": 0,
    "overtimeHours": 8.5,
    "workHours": 120
  }
}
```

---

## 🚨 错误处理

### HTTP状态码

| 状态码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | 正常处理响应数据 |
| 400 | 参数错误 | 检查请求参数格式和内容 |
| 401 | 未授权 | 重新登录获取Token |
| 403 | 权限不足 | 联系管理员分配权限 |
| 404 | 资源不存在 | 检查请求路径和资源ID |
| 500 | 服务器错误 | 记录错误信息，联系技术支持 |

### 业务错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| USER_NOT_FOUND | 用户不存在 | 检查用户ID或用户名 |
| INVALID_PASSWORD | 密码错误 | 重新输入密码 |
| TOKEN_EXPIRED | Token过期 | 刷新Token或重新登录 |
| INSUFFICIENT_BALANCE | 余额不足 | 充值后重试 |
| DEVICE_OFFLINE | 设备离线 | 检查设备连接状态 |

### 错误响应示例

```json
{
  "code": "USER_NOT_FOUND",
  "message": "用户不存在，用户ID: 999",
  "data": null,
  "timestamp": 1642123456789
}
```

---

## 📱 移动端API

### 移动端专用接口

**基础路径**: `/api/v1/mobile`

| 接口 | 方法 | 描述 |
|------|------|------|
| `/mobile/login` | POST | 移动端登录 |
| `/mobile/user/info` | GET | 获取用户信息 |
| `/mobile/attendance/punch` | POST | 移动端考勤打卡 |
| `/mobile/visitor/qrcode` | GET | 获取访客二维码 |
| `/mobile/consume/quick` | POST | 快速消费 |

### 移动端特色功能

1. **二维码识别**: 支持访客二维码扫描
2. **生物识别**: 人脸识别、指纹识别
3. **离线缓存**: 支持网络中断时离线操作
4. **推送通知**: 实时推送审批结果和通知

---

## 🔍 API测试工具

### Postman 使用

1. **导入集合**: 导入提供的Postman集合
2. **环境变量**: 配置服务器地址和认证Token
3. **预请求脚本**: 自动处理Token刷新
4. **测试脚本**: 自动验证响应格式

### cURL 示例

```bash
# 用户登录
curl -X POST "http://localhost:8088/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "captchaKey": "captcha:uuid",
    "captchaCode": "1234"
  }'

# 查询用户列表
curl -X GET "http://localhost:8088/api/v1/user/query?pageNum=1&pageSize=20" \
  -H "Authorization: Bearer {token}"
```

### JavaScript 测试

```javascript
// 使用fetch API
fetch('http://localhost:8088/api/v1/user/query?pageNum=1&pageSize=20', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  if (data.code === 200) {
    console.log('查询成功:', data.data);
  } else {
    console.error('查询失败:', data.message);
  }
})
.catch(error => {
  console.error('请求异常:', error);
});
```

---

## 📊 API监控

### 性能指标

- **响应时间**: 接口平均响应时间应小于500ms
- **可用性**: 接口可用性应大于99.9%
- **并发数**: 支持每秒1000+并发请求
- **错误率**: 错误率应小于0.1%

### 监控工具

- **Prometheus**: 指标收集和存储
- **Grafana**: 可视化监控面板
- **Zipkin**: 分布式链路追踪
- **Sentry**: 错误监控和报警

---

## 📚 最佳实践

### 1. Token管理

```javascript
// Token存储建议
const tokenStorage = {
  // 使用localStorage存储Token
  setToken: function(token) {
    localStorage.setItem('ioedream_token', token);
    localStorage.setItem('ioedream_token_expire', Date.now() + 7200000);
  },

  // 获取Token
  getToken: function() {
    const token = localStorage.getItem('ioedream_token');
    const expire = localStorage.getItem('ioedream_token_expire');

    if (!token || !expire || Date.now() > expire) {
      return null;
    }

    return token;
  },

  // 清除Token
  clearToken: function() {
    localStorage.removeItem('ioedream_token');
    localStorage.removeItem('ioedream_token_expire');
  }
};
```

### 2. 错误处理

```javascript
// 统一错误处理
const apiRequest = async (url, options = {}) => {
  try {
    const token = tokenStorage.getToken();
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.headers
      }
    });

    const data = await response.json();

    if (data.code === 200) {
      return data.data;
    } else if (data.code === 'TOKEN_EXPIRED') {
      // Token过期，尝试刷新
      const newToken = await refreshToken();
      if (newToken) {
        // 重新发起请求
        return apiRequest(url, options);
      } else {
        // 刷新失败，跳转登录
        window.location.href = '/login';
      }
    } else {
      throw new Error(data.message);
    }
  } catch (error) {
    console.error('API请求失败:', error);
    throw error;
  }
};
```

### 3. 请求重试

```javascript
// 请求重试机制
const retryRequest = async (url, options = {}, maxRetries = 3) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await apiRequest(url, options);
    } catch (error) {
      if (i === maxRetries - 1) {
        throw error;
      }
      // 指数退避重试
      await new Promise(resolve => setTimeout(resolve, Math.pow(2, i) * 1000));
    }
  }
};
```

---

## 🔄 版本兼容性

### API版本策略

- **URL版本控制**: `/api/v1/`, `/api/v2/`
- **Header版本控制**: `Accept: application/vnd.api.v1+json`
- **向后兼容**: 新版本保持对旧版本的兼容
- **废弃通知**: 提前3个月通知API废弃

### 版本升级指南

1. **检查兼容性**: 确认新版本与现有代码兼容
2. **更新文档**: 参考最新API文档
3. **测试验证**: 在测试环境充分测试
4. **逐步迁移**: 分阶段进行生产环境迁移

---

## 📞 技术支持

### 获取帮助

- **API文档**: [https://docs.ioedream.com/api](https://docs.ioedream.com/api)
- **GitHub Issues**: [https://github.com/IOE-DREAM/issues](https://github.com/IOE-DREAM/issues)
- **技术交流群**: 扫描二维码加入技术交流群
- **邮箱支持**: api-support@ioedream.com

### 反馈建议

如果您在使用过程中有任何建议或问题，欢迎反馈：

1. **功能建议**: 提交GitHub Issue
2. **Bug报告**: 详细描述问题和复现步骤
3. **性能问题**: 提供相关性能数据
4. **文档改进**: 指出文档中的错误或不清晰之处

---

*本指南将随着API的更新持续维护，建议定期查看最新版本*