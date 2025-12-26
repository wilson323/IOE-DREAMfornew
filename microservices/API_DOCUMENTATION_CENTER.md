# IOE-DREAM API 文档导航中心

**📅 更新时间**: 2025-12-20
**🎯 文档目标**: 提供完整的API接口文档导航和使用指南
**👥 目标用户**: 前端开发人员、第三方集成开发人员、API测试人员

---

## 🚀 快速开始

### 1️⃣ 访问Swagger UI

每个微服务都提供了独立的API文档，您可以通过以下地址访问：

| 服务名称 | 端口 | API文档地址 | OpenAPI规范 | 状态 |
|---------|------|------------|-------------|------|
| **API网关** | 8080 | http://localhost:8080/swagger-ui/index.html | [JSON](http://localhost:8080/v3/api-docs) | ✅ 核心入口 |
| **公共业务服务** | 8088 | http://localhost:8088/swagger-ui/index.html | [JSON](http://localhost:8088/v3/api-docs) | ✅ 用户管理 |
| **设备通讯服务** | 8087 | http://localhost:8087/swagger-ui/index.html | [JSON](http://localhost:8087/v3/api-docs) | ✅ 设备协议 |
| **OA办公服务** | 8089 | http://localhost:8089/swagger-ui/index.html | [JSON](http://localhost:8089/v3/api-docs) | ✅ 工作流 |
| **门禁管理服务** | 8090 | http://localhost:8090/swagger-ui/index.html | [JSON](http://localhost:8090/v3/api-docs) | ✅ 智能门禁 |
| **考勤管理服务** | 8091 | http://localhost:8091/swagger-ui/index.html | [JSON](http://localhost:8091/v3/api-docs) | ✅ 考勤打卡 |
| **视频监控服务** | 8092 | http://localhost:8092/swagger-ui/index.html | [JSON](http://localhost:8092/v3/api-docs) | ✅ 视频分析 |
| **消费管理服务** | 8094 | http://localhost:8094/swagger-ui/index.html | [JSON](http://localhost:8094/v3/api-docs) | ✅ 消费支付 |
| **访客管理服务** | 8095 | http://localhost:8095/swagger-ui/index.html | [JSON](http://localhost:8095/v3/api-docs) | ✅ 访客预约 |
| **数据库管理服务** | 8093 | http://localhost:8093/swagger-ui/index.html | [JSON](http://localhost:8093/v3/api-docs) | ✅ 数据备份 |
| **生物模板服务** | 8096 | http://localhost:8096/swagger-ui/index.html | [JSON](http://localhost:8096/v3/api-docs) | ✅ 生物识别 |

### 2️⃣ API文档特性

- ✅ **OpenAPI 3.0标准**: 遵循最新API规范
- ✅ **交互式文档**: 支持在线API测试
- ✅ **多环境支持**: 开发/测试/生产环境切换
- ✅ **详细注释**: 完整的参数说明和示例
- ✅ **分组管理**: 按业务模块分组展示

---

## 📖 API使用指南

### 🔐 认证机制

IOE-DREAM采用基于JWT的无状态认证机制：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 获取Token

1. **获取验证码**
```http
GET /api/v1/auth/getCaptcha
```

2. **用户登录**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "captchaKey": "captcha:uuid",
  "captchaCode": "1234"
}
```

3. **获取Token**
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

### 📊 统一响应格式

所有API接口都采用统一的响应格式：

#### 成功响应
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

#### 错误响应
```json
{
  "code": "USER_NOT_FOUND",
  "message": "用户不存在",
  "data": null,
  "timestamp": 1642123456789
}
```

#### 分页响应
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
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5
  }
}
```

---

## 🏗️ API架构总览

### 🎯 核心业务API分组

#### 1️⃣ 用户管理模块
- **用户认证**: `/api/v1/auth/*`
- **用户管理**: `/api/v1/user/*`
- **权限管理**: `/api/v1/permission/*`
- **角色管理**: `/api/v1/role/*`

#### 2️⃣ 组织架构模块
- **区域管理**: `/api/v1/area/*`
- **部门管理**: `/api/v1/department/*`
- **设备管理**: `/api/v1/device/*`

#### 3️⃣ 门禁管理模块
- **设备管理**: `/api/v1/access/device/*`
- **通行记录**: `/api/v1/access/record/*`
- **权限管理**: `/api/v1/access/permission/*`
- **实时监控**: `/api/v1/access/monitor/*`

#### 4️⃣ 考勤管理模块
- **打卡记录**: `/api/v1/attendance/record/*`
- **排班管理**: `/api/v1/attendance/shift/*`
- **考勤统计**: `/api/v1/attendance/statistics/*`
- **请假管理**: `/api/v1/attendance/leave/*`

#### 5️⃣ 消费管理模块
- **账户管理**: `/api/v1/consume/account/*`
- **消费记录**: `/api/v1/consume/record/*`
- **充值管理**: `/api/v1/consume/recharge/*`
- **退款管理**: `/api/v1/consume/refund/*`

#### 6️⃣ 访客管理模块
- **访客预约**: `/api/v1/visitor/appointment/*`
- **访客登记**: `/api/v1/visitor/registration/*`
- **访问记录**: `/api/v1/visitor/record/*`
- **黑名单管理**: `/api/v1/visitor/blacklist/*`

#### 7️⃣ 视频监控模块
- **设备管理**: `/api/v1/video/device/*`
- **实时监控**: `/api/v1/video/live/*`
- **录像回放**: `/api/v1/video/playback/*`
- **AI分析**: `/api/v1/video/ai/*`

#### 8️⃣ OA办公模块
- **流程管理**: `/api/v1/oa/workflow/*`
- **审批管理**: `/api/v1/oa/approval/*`
- **文件管理**: `/api/v1/oa/file/*`
- **通知管理**: `/api/v1/oa/notification/*`

---

## 🔧 API最佳实践

### 1️⃣ 请求头配置

```javascript
// 必须的请求头
headers: {
  'Content-Type': 'application/json',
  'Authorization': 'Bearer ' + token
}

// 可选的请求头
headers: {
  'Accept-Language': 'zh-CN',
  'X-Request-ID': 'unique-request-id',
  'X-Client-Version': '1.0.0'
}
```

### 2️⃣ 错误处理

```javascript
// 统一错误处理
function handleApiResponse(response) {
  if (response.code === 200) {
    return response.data;
  } else if (response.code === 'TOKEN_EXPIRED') {
    // Token过期，重新登录
    redirectToLogin();
    return null;
  } else if (response.code === 'PERMISSION_DENIED') {
    // 权限不足
    showErrorMessage('权限不足');
    return null;
  } else {
    // 其他错误
    showErrorMessage(response.message);
    return null;
  }
}
```

### 3️⃣ 分页查询

```javascript
// 标准分页参数
const queryParams = {
  pageNum: 1,        // 页码，从1开始
  pageSize: 20,      // 每页大小
  keyword: '搜索关键词', // 可选
  sortField: 'createTime', // 排序字段
  sortOrder: 'desc'  // 排序方向
};

// API调用
fetch('/api/v1/user/query', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify(queryParams)
})
.then(response => response.json())
.then(data => {
  const result = handleApiResponse(data);
  if (result) {
    console.log('用户列表:', result.list);
    console.log('总数:', result.total);
  }
});
```

### 4️⃣ 文件上传

```javascript
// 文件上传
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('category', 'avatar');

fetch('/api/v1/file/upload', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token
  },
  body: formData
})
.then(response => response.json())
.then(data => {
  const result = handleApiResponse(data);
  if (result) {
    console.log('文件上传成功:', result.fileUrl);
  }
});
```

---

## 📱 移动端API

### 🎯 移动端专用接口

#### 基础路径: `/api/v1/mobile`

| 接口路径 | 方法 | 描述 |
|---------|------|------|
| `/mobile/login` | POST | 移动端登录 |
| `/mobile/user/info` | GET | 获取用户信息 |
| `/mobile/attendance/punch` | POST | 移动端考勤打卡 |
| `/mobile/visitor/qrcode` | GET | 获取访客二维码 |
| `/mobile/consume/quick` | POST | 快速消费 |

### 📱 移动端特色功能

1. **二维码识别**: 支持访客二维码扫描
2. **生物识别**: 人脸识别、指纹识别
3. **离线缓存**: 支持网络中断时离线操作
4. **推送通知**: 实时推送审批结果和通知

---

## 🔍 API测试工具

### 1️⃣ Postman集合

我们提供了完整的Postman集合，包含所有API接口的示例：

```bash
# 导入Postman集合
1. 下载 IOE-DREAM.postman_collection.json
2. 在Postman中导入集合
3. 配置环境变量：
   - baseUrl: http://localhost:8088
   - token: your_jwt_token_here
```

### 2️⃣ cURL示例

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
  -H "Authorization: Bearer your_token_here"
```

### 3️⃣ JavaScript示例

```javascript
// API客户端封装
class IOEDreamAPI {
  constructor(baseUrl, token) {
    this.baseUrl = baseUrl;
    this.token = token;
  }

  async request(method, path, data = null) {
    const url = `${this.baseUrl}${path}`;
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.token}`
      }
    };

    if (data) {
      options.body = JSON.stringify(data);
    }

    const response = await fetch(url, options);
    return response.json();
  }

  // 用户登录
  async login(username, password, captchaKey, captchaCode) {
    return this.request('POST', '/api/v1/auth/login', {
      username,
      password,
      captchaKey,
      captchaCode
    });
  }

  // 查询用户列表
  async getUserList(pageNum = 1, pageSize = 20, keyword = '') {
    return this.request('POST', '/api/v1/user/query', {
      pageNum,
      pageSize,
      keyword
    });
  }
}

// 使用示例
const api = new IOEDreamAPI('http://localhost:8088', 'your_token_here');

// 登录
api.login('admin', 'admin123', 'captcha:uuid', '1234')
  .then(result => {
    if (result.code === 200) {
      console.log('登录成功:', result.data);
    }
  });

// 查询用户
api.getUserList(1, 20, 'admin')
  .then(result => {
    if (result.code === 200) {
      console.log('用户列表:', result.data.list);
    }
  });
```

---

## 🚨 错误码参考

### 🔐 认证相关错误码

| 错误码 | 说明 | 解决方案 |
|---------|------|----------|
| `TOKEN_EXPIRED` | Token过期 | 刷新Token或重新登录 |
| `TOKEN_INVALID` | Token无效 | 重新登录 |
| `CAPTCHA_INVALID` | 验证码错误 | 刷新验证码重试 |
| `USER_NOT_FOUND` | 用户不存在 | 检查用户名 |
| `PASSWORD_ERROR` | 密码错误 | 重新输入密码 |

### 🔒 权限相关错误码

| 错误码 | 说明 | 解决方案 |
|---------|------|----------|
| `PERMISSION_DENIED` | 权限不足 | 联系管理员分配权限 |
| `RESOURCE_NOT_FOUND` | 资源不存在 | 检查资源ID |
| `OPERATION_FORBIDDEN` | 操作被禁止 | 检查用户权限 |

### 💼 业务相关错误码

| 错误码 | 说明 | 解决方案 |
|---------|------|----------|
| `DEVICE_OFFLINE` | 设备离线 | 检查设备连接 |
| `INSUFFICIENT_BALANCE` | 余额不足 | 充值后重试 |
| `ACCESS_DENIED` | 访问拒绝 | 检查访问权限 |
| `QUOTA_EXCEEDED` | 配额超限 | 等待或申请提升配额 |

---

## 📊 API监控

### 🎯 性能指标

- **响应时间**: 接口平均响应时间 < 500ms
- **可用性**: 接口可用性 > 99.9%
- **并发数**: 支持每秒1000+并发请求
- **错误率**: 错误率 < 0.1%

### 📈 监控工具

- **Prometheus**: 指标收集和存储
- **Grafana**: 可视化监控面板
- **Zipkin**: 分布式链路追踪
- **Sentry**: 错误监控和报警

---

## 🔄 版本管理

### 📋 API版本策略

- **URL版本控制**: `/api/v1/`, `/api/v2/`
- **向后兼容**: 新版本保持对旧版本的兼容
- **废弃通知**: 提前3个月通知API废弃
- **版本升级**: 提供详细的迁移指南

### 🔄 版本升级流程

1. **检查兼容性**: 确认新版本与现有代码兼容
2. **更新文档**: 参考最新API文档
3. **测试验证**: 在测试环境充分测试
4. **逐步迁移**: 分阶段进行生产环境迁移

---

## 📞 技术支持

### 🆘 获取帮助

- **API文档**: [Swagger UI](http://localhost:8088/swagger-ui/index.html)
- **GitHub Issues**: [提交问题](https://github.com/IOE-DREAM/issues)
- **技术交流群**: 扫描二维码加入技术交流群
- **邮箱支持**: api-support@ioedream.com

### 📝 反馈建议

如果您在使用过程中有任何建议或问题，欢迎反馈：

1. **功能建议**: 提交GitHub Issue
2. **Bug报告**: 详细描述问题和复现步骤
3. **性能问题**: 提供相关性能数据
4. **文档改进**: 指出文档中的错误或不清晰之处

---

**📚 文档维护**: 本文档将随着API的更新持续维护，建议定期查看最新版本
**🔄 最后更新**: 2025-12-20 22:30
**📧 联系方式**: api-support@ioedream.com

---

## 📚 完整文档体系

### 📖 相关文档
- **[API测试示例](./API_TEST_EXAMPLES.md)** - 详细的API测试用例和脚本
- **[API开发规范](./documentation/technical/API_DEVELOPMENT_STANDARDS.md)** - API设计和开发规范
- **[API集成指南](./documentation/api/API_INTEGRATION_GUIDE.md)** - 第三方系统集成指南

### 🔧 开发工具
- **Swagger Editor**: 在线OpenAPI 3.0编辑器 (https://editor.swagger.io/)
- **Postman**: API测试和文档生成工具
- **Insomnia**: 轻量级API客户端
- **HTTPie**: 命令行HTTP客户端

### 📊 监控和调试
- **API监控**: 实时监控API性能和可用性
- **日志分析**: 详细的API调用日志记录
- **错误追踪**: 自动化的错误收集和分析
- **性能优化**: 基于数据的API性能优化建议