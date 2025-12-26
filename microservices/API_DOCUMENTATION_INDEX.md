# IOE-DREAM API 文档导航中心

**📅 更新时间**: 2025-12-20
**🏗️ 项目**: IOE-DREAM 智慧园区一卡通管理平台
**📚 API版本**: v1.0.0

---

## 🚀 快速访问

### 🌐 Swagger UI 在线文档

| 服务名称 | 端口 | Swagger UI地址 | 描述 |
|---------|------|--------------|------|
| **ioedream-gateway-service** | 8080 | [Swagger UI](http://localhost:8080/swagger-ui/index.html) | API网关服务 |
| **ioedream-common-service** | 8088 | [Swagger UI](http://localhost:8088/swagger-ui/index.html) | 公共业务服务 |
| **ioedream-device-comm-service** | 8087 | [Swagger UI](http://localhost:8087/swagger-ui/index.html) | 设备通讯服务 |
| **ioedream-oa-service** | 8089 | [Swagger UI](http://localhost:8089/swagger-ui/index.html) | OA办公服务 |
| **ioedream-access-service** | 8090 | [Swagger UI](http://localhost:8090/swagger-ui/index.html) | 门禁管理服务 |
| **ioedream-attendance-service** | 8091 | [Swagger UI](http://localhost:8091/swagger-ui/index.html) | 考勤管理服务 |
| **ioedream-video-service** | 8092 | [Swagger UI](http://localhost:8092/swagger-ui/index.html) | 视频监控服务 |
| **ioedream-consume-service** | 8094 | [Swagger UI](http://localhost:8094/swagger-ui/index.html) | 消费管理服务 |
| **ioedream-visitor-service** | 8095 | [Swagger UI](http://localhost:8095/swagger-ui/index.html) | 访客管理服务 |
| **ioedream-biometric-service** | 8096 | [Swagger UI](http://localhost:8096/swagger-ui/index.html) | 生物模板管理服务 |

### 📄 OpenAPI JSON 规范

所有服务的OpenAPI JSON规范可通过以下地址访问：
```
http://localhost:{port}/v3/api-docs
```

---

## 📋 API 模块分类

### 🔐 认证授权模块 (Authentication & Authorization)

**基础路径**: `/api/v1/auth`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/login` | POST | 用户登录 | common-service |
| `/login/getCaptcha` | GET | 获取验证码 | common-service |
| `/login/logout` | GET | 退出登录 | common-service |
| `/login/getLoginInfo` | GET | 获取登录用户信息 | common-service |
| `/login/sendEmailCode/{loginName}` | GET | 发送邮箱验证码 | common-service |
| `/login/getTwoFactorLoginFlag` | GET | 获取双因子登录标识 | common-service |

**访问地址**: [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html)

---

### 👥 用户管理模块 (User Management)

**基础路径**: `/api/v1/user`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/user/query` | GET | 分页查询用户 | common-service |
| `/user/{userId}` | GET | 获取用户详情 | common-service |
| `/user/add` | POST | 新增用户 | common-service |
| `/user/update` | PUT | 更新用户信息 | common-service |
| `/user/delete/{userId}` | DELETE | 删除用户 | common-service |
| `/user/batch-delete` | DELETE | 批量删除用户 | common-service |
| `/user/status/{userId}` | PUT | 启用/禁用用户 | common-service |

**访问地址**: [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html)

---

### 🏢 组织架构模块 (Organization Management)

**基础路径**: `/api/v1/organization`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/area/query` | GET | 分页查询区域 | common-service |
| `/area/tree` | GET | 获取区域树结构 | common-service |
| `/area/add` | POST | 新增区域 | common-service |
| `/department/query` | GET | 分页查询部门 | common-service |
| `/department/tree` | GET | 获取部门树结构 | common-service |
| `/device/query` | GET | 分页查询设备 | common-service |

**访问地址**: [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html)

---

### 🚪 智能门禁模块 (Access Control)

**基础路径**: `/api/v1/access`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/record/query` | GET | 分页查询通行记录 | access-service |
| `/record/batch/upload` | POST | 批量上传通行记录 | access-service |
| `/device/query` | GET | 分页查询门禁设备 | access-service |
| `/device/add` | POST | 新增门禁设备 | access-service |
| `/area/query` | GET | 分页查询门禁区域 | access-service |
| `/area/add` | POST | 新增门禁区域 | access-service |
| `/anti-passback/check` | POST | 反潜回检查 | access-service |
| `/multi-modal/auth` | POST | 多模态认证 | access-service |

**访问地址**: [http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html)

---

### ⏰ 考勤管理模块 (Attendance Management)

**基础路径**: `/api/v1/attendance`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/record/query` | GET | 分页查询考勤记录 | attendance-service |
| `/record/supplement` | POST | 补签考勤记录 | attendance-service |
| `/shift/query` | GET | 分页查询班次 | attendance-service |
| `/shift/add` | POST | 新增班次 | attendance-service |
| `/schedule/query` | GET | 分页查询排班 | attendance-service |
| `/schedule/generate` | POST | 自动生成排班 | attendance-service |
| `/leave/query` | GET | 分页查询请假记录 | attendance-service |
| `/leave/apply` | POST | 申请请假 | attendance-service |
| `/overtime/query` | GET | 分页查询加班记录 | attendance-service |
| `/overtime/apply` | POST | 申请加班 | attendance-service |

**访问地址**: [http://localhost:8091/swagger-ui/index.html](http://localhost:8091/swagger-ui/index.html)

---

### 💳 消费管理模块 (Consume Management)

**基础路径**: `/api/v1/consume`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/account/query` | GET | 分页查询消费账户 | consume-service |
| `/account/{accountId}` | GET | 获取账户详情 | consume-service |
| `/account/recharge` | POST | 账户充值 | consume-service |
| `/consume/execute` | POST | 执行消费 | consume-service |
| `/record/query` | GET | 分页查询消费记录 | consume-service |
| `/record/refund` | POST | 消费退款 | consume-service |
| `/product/query` | GET | 分页查询商品 | consume-service |
| `/product/add` | POST | 新增商品 | consume-service |
| `/area/query` | GET | 分页查询消费区域 | consume-service |

**访问地址**: [http://localhost:8094/swagger-ui/index.html](http://localhost:8094/swagger-ui/index.html)

---

### 👥 访客管理模块 (Visitor Management)

**基础路径**: `/api/v1/visitor`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/appointment/query` | GET | 分页查询访客预约 | visitor-service |
| `/appointment/apply` | POST | 申请访客预约 | visitor-service |
| `/appointment/approve` | POST | 审批访客预约 | visitor-service |
| `/record/query` | GET | 分页查询访客记录 | visitor-service |
| `/record/register` | POST | 访客登记 | visitor-service |
| `/blacklist/query` | GET | 分页查询访客黑名单 | visitor-service |
| `/blacklist/add` | POST | 添加访客黑名单 | visitor-service |
| `/security/verify` | POST | 访客身份验证 | visitor-service |

**访问地址**: [http://localhost:8095/swagger-ui/index.html](http://localhost:8095/swagger-ui/index.html)

---

### 📹 视频监控模块 (Video Surveillance)

**基础路径**: `/api/v1/video`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/device/query` | GET | 分页查询视频设备 | video-service |
| `/device/add` | POST | 新增视频设备 | video-service |
| `/stream/{deviceId}` | GET | 获取视频流 | video-service |
| `/recording/query` | GET | 分页查询录像 | video-service |
| `/ai/analyze` | POST | AI智能分析 | video-service |
| `/face/detect` | POST | 人脸检测 | video-service |
| `/ptz/control` | POST | PTZ云台控制 | video-service |
| `/wall/configure` | POST | 视频墙配置 | video-service |

**访问地址**: [http://localhost:8092/swagger-ui/index.html](http://localhost:8092/swagger-ui/index.html)

---

### 🔧 设备通讯模块 (Device Communication)

**基础路径**: `/api/v1/device`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/protocol/discovery` | GET | 设备协议发现 | device-comm-service |
| `/protocol/send` | POST | 发送协议指令 | device-comm-service |
| `/connection/status` | GET | 获取设备连接状态 | device-comm-service |
| `/biometric/template/sync` | POST | 生物模板同步 | device-comm-service |
| `/monitor/metrics` | GET | 设备监控指标 | device-comm-service |

**访问地址**: [http://localhost:8087/swagger-ui/index.html](http://localhost:8087/swagger-ui/index.html)

---

### 📋 OA办公模块 (Office Automation)

**基础路径**: `/api/v1/oa`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/workflow/start` | POST | 发起工作流 | oa-service |
| `/workflow/query` | GET | 分页查询工作流 | oa-service |
| `/approval/pending` | GET | 获取待审批列表 | oa-service |
| `/approval/process` | POST | 处理审批 | oa-service |
| `/file/upload` | POST | 文件上传 | oa-service |
| `/file/download/{fileId}` | GET | 文件下载 | oa-service |

**访问地址**: [http://localhost:8089/swagger-ui/index.html](http://localhost:8089/swagger-ui/index.html)

---

### 🔍 生物模板管理模块 (Biometric Template Management)

**基础路径**: `/api/v1/biometric`

| 接口 | 方法 | 描述 | 服务 |
|------|------|------|------|
| `/template/upload` | POST | 上传生物模板 | biometric-service |
| `/template/query` | GET | 分页查询生物模板 | biometric-service |
| `/template/sync` | POST | 模板同步到设备 | biometric-service |
| `/feature/extract` | POST | 特征提取 | biometric-service |
| `/template/delete/{templateId}` | DELETE | 删除生物模板 | biometric-service |

**访问地址**: [http://localhost:8096/swagger-ui/index.html](http://localhost:8096/swagger-ui/index.html)

---

## 🔧 通用接口规范

### 📨 统一响应格式

所有API接口都使用统一的响应格式：

```json
{
  "code": 200,           // 业务状态码
  "message": "success",  // 响应消息
  "data": {},            // 响应数据
  "timestamp": 1642123456789  // 时间戳
}
```

### 🏷️ 标准HTTP状态码

| 状态码 | 说明 | 业务场景 |
|--------|------|----------|
| 200 | 成功 | 请求处理成功 |
| 201 | 创建成功 | 资源创建成功 |
| 400 | 请求参数错误 | 参数验证失败 |
| 401 | 未授权 | 需要登录认证 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器内部错误 | 系统异常 |

### 🔒 认证机制

**Token认证**: 所有需要认证的接口都需要在请求头中携带Token
```
Authorization: Bearer {token}
```

**权限控制**: 基于角色的权限控制，通过`@PermissionCheck`注解实现

---

## 📚 API 使用指南

### 1. 认证流程

1. **获取验证码**
   ```
   GET /api/v1/auth/login/getCaptcha
   ```

2. **用户登录**
   ```json
   POST /api/v1/auth/login
   {
     "username": "admin",
     "password": "password",
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

### 2. 分页查询

所有分页查询接口都遵循统一格式：

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
    "list": [],           // 数据列表
    "total": 100,         // 总记录数
    "pageNum": 1,         // 当前页码
    "pageSize": 20,       // 每页大小
    "pages": 5            // 总页数
  }
}
```

### 3. 文件上传

**请求格式**:
```
POST /api/v1/file/upload
Content-Type: multipart/form-data
```

**响应格式**:
```json
{
  "code": 200,
  "data": {
    "fileId": "file_uuid",
    "fileName": "example.jpg",
    "fileSize": 1024000,
    "fileUrl": "http://domain.com/files/example.jpg",
    "filePath": "/files/2025/12/20/example.jpg"
  }
}
```

---

## 🛠️ 开发工具

### Postman 集合

已提供完整的Postman集合，包含：
- 环境变量配置
- 认证流程脚本
- 所有API接口示例

### SDK 支持

提供以下语言的SDK：
- Java SDK (推荐)
- JavaScript/TypeScript SDK
- Python SDK
- Go SDK

---

## 📞 技术支持

### 🐛 问题反馈

如果您在使用API时遇到问题，请通过以下方式联系我们：

- **邮箱**: support@ioedream.com
- **GitHub**: https://github.com/IOE-DREAM/issues
- **文档**: https://docs.ioedream.com

### 🔄 版本更新

API版本更新遵循语义化版本规范：
- **主版本号**: 不兼容的API修改
- **次版本号**: 向下兼容的功能性新增
- **修订号**: 向下兼容的问题修正

---

**💡 温馨提示**:
1. 生产环境请使用HTTPS协议
2. 建议设置合理的请求频率限制
3. 重要操作建议添加幂等性检查
4. 请妥善保管API Token，避免泄露

*本文档由IOE-DREAM团队维护，最后更新于2025-12-20*