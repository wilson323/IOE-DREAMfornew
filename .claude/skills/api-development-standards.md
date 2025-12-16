---
name: api-development-standards
description: IOE-DREAM项目API开发规范和最佳实践指南
version: 1.0.0
---

# IOE-DREAM API开发规范

> **基于**: Spring Boot 3.5.8 + Vue 3.4 + uni-app 3.0 技术栈
> **目标**: 统一API设计规范，确保前后端接口一致性
> **适用**: IOE-DREAM智慧园区一卡通管理平台所有微服务

## 🎯 快速开始

### 1. API URL设计规范

```yaml
# 标准格式
{base_url}/api/v{version}/{module}/{entity}[/{action}]

# 示例
/api/v1/access/record/query
/api/v1/consume/transaction/execute
/api/v1/mobile/access/check
```

### 2. HTTP方法规范

| 方法 | 用途 | 示例 |
|------|------|------|
| **GET** | 查询操作 | `GET /api/v1/users/{id}` |
| **POST** | 创建/执行 | `POST /api/v1/consume/transaction/execute` |
| **PUT** | 更新资源 | `PUT /api/v1/users/{id}` |
| **DELETE** | 删除资源 | `DELETE /api/v1/users/{id}` |

### 3. 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1703847600000,
  "traceId": "req_123456789"
}
```

## 📋 核心模块API列表

### 门禁管理 (/api/v1/access)
```yaml
# 门禁记录
GET    /api/v1/access/record/query           # 查询通行记录
POST   /api/v1/access/record/create          # 创建通行记录

# 移动端门禁
POST   /api/v1/mobile/access/check            # 移动端门禁检查
POST   /api/v1/mobile/access/qr/verify        # 二维码验证
POST   /api/v1/mobile/access/nfc/verify       # NFC验证
POST   /api/v1/mobile/access/biometric/verify # 生物识别验证

# 设备管理
GET    /api/v1/access/device/query           # 查询设备列表
POST   /api/v1/access/device/add             # 添加设备
PUT    /api/v1/access/device/update          # 更新设备
DELETE /api/v1/access/device/{id}            # 删除设备
```

### 消费管理 (/api/v1/consume)
```yaml
# 交易管理
POST   /api/v1/consume/transaction/execute      # 执行消费交易
GET    /api/v1/consume/transaction/query        # 查询交易记录
GET    /api/v1/consume/transaction/{id}         # 获取交易详情

# 移动端消费
POST   /api/v1/mobile/consume/quick             # 快速消费
POST   /api/v1/mobile/consume/scan              # 扫码消费
POST   /api/v1/mobile/consume/nfc               # NFC消费
POST   /api/v1/mobile/consume/face              # 人脸消费

# 账户管理
GET    /api/v1/consume/account/balance/{userId}   # 查询余额
GET    /api/v1/consume/account/transactions/{userId} # 账户流水
```

### 访客管理 (/api/v1/visitor)
```yaml
# 访客预约
POST   /api/v1/visitor/appointment/create       # 创建预约
GET    /api/v1/visitor/appointment/query        # 查询预约
PUT    /api/v1/visitor/appointment/approve      # 审批预约
DELETE /api/v1/visitor/appointment/{id}         # 取消预约

# 访客登记
POST   /api/v1/visitor/registration/create      # 访客登记
GET    /api/v1/visitor/registration/query       # 查询记录

# 移动端访客
POST   /api/v1/mobile/visitor/checkin          # 移动端签到
POST   /api/v1/mobile/visitor/checkout         # 移动端签退
```

### 考勤管理 (/api/v1/attendance)
```yaml
# 考勤打卡
POST   /api/v1/attendance/clock/in              # 上班打卡
POST   /api/v1/attendance/clock/out             # 下班打卡
GET    /api/v1/attendance/record/query          # 查询考勤记录

# 移动端考勤
POST   /api/v1/mobile/attendance/clock          # 移动端打卡
POST   /api/v1/mobile/attendance/location/verify # 位置验证

# 排班管理
GET    /api/v1/attendance/schedule/query        # 查询排班
POST   /api/v1/attendance/schedule/create       # 创建排班
```

### 用户管理 (/api/v1/system)
```yaml
# 用户管理
GET    /api/v1/system/user/query               # 查询用户列表
POST   /api/v1/system/user/create              # 创建用户
PUT    /api/v1/system/user/update              # 更新用户
DELETE /api/v1/system/user/{id}                # 删除用户

# 权限管理
GET    /api/v1/system/role/query               # 查询角色
POST   /api/v1/system/permission/assign         # 分配权限

# 字典管理
GET    /api/v1/support/dict/getAllDict          # 获取所有字典
GET    /api/v1/support/dict/data/{dictId}      # 获取字典数据
```

## 🔧 开发模板

### Controller模板

```java
@RestController
@RequestMapping("/api/v1/{module}/{entity}")
@Tag(name = "{业务模块}管理", description = "{业务模块}相关的API接口")
@Slf4j
public class {Entity}Controller {

    @Resource
    private {Entity}Service {entity}Service;

    @Operation(summary = "查询{entity}", description = "分页查询{entity}列表")
    @GetMapping("/query")
    @PreAuthorize("hasPermission('{module}:{entity}:read')")
    public ResponseDTO<PageResult<{Entity}VO>> query{Entity}(
            @Valid {Entity}QueryDTO queryDTO) {
        PageResult<{Entity}VO> result = {entity}Service.query{Entity}(queryDTO);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取{entity}详情", description = "根据ID获取{entity}详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('{module}:{entity}:read')")
    public ResponseDTO<{Entity}DetailVO> get{Entity}(@PathVariable Long id) {
        {Entity}DetailVO result = {entity}Service.get{Entity}Detail(id);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "创建{entity}", description = "创建新的{entity}")
    @PostMapping("/create")
    @PreAuthorize("hasPermission('{module}:{entity}:create')")
    public ResponseDTO<{Entity}VO> create{Entity}(
            @Valid @RequestBody {Entity}CreateDTO createDTO) {
        {Entity}VO result = {entity}Service.create{Entity}(createDTO);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "更新{entity}", description = "更新{entity}信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('{module}:{entity}:update')")
    public ResponseDTO<{Entity}VO> update{Entity}(
            @PathVariable Long id,
            @Valid @RequestBody {Entity}UpdateDTO updateDTO) {
        {Entity}VO result = {entity}Service.update{Entity}(id, updateDTO);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "删除{entity}", description = "根据ID删除{entity}")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('{module}:{entity}:delete')")
    public ResponseDTO<Void> delete{Entity}(@PathVariable Long id) {
        {entity}Service.delete{Entity}(id);
        return ResponseDTO.ok();
    }
}
```

### 前端API调用模板

```javascript
// src/api/{module}.js
import request from '@/lib/request';

export const {module}Api = {
  // 查询列表
  query: (params) => {
    return request.get('/api/v1/{module}/{entity}/query', { params });
  },

  // 获取详情
  getDetail: (id) => {
    return request.get(`/api/v1/{module}/{entity}/${id}`);
  },

  // 创建
  create: (data) => {
    return request.post('/api/v1/{module}/{entity}/create', data, {
      encrypt: true // 敏感数据加密
    });
  },

  // 更新
  update: (id, data) => {
    return request.put(`/api/v1/{module}/{entity}/${id}`, data);
  },

  // 删除
  delete: (id) => {
    return request.delete(`/api/v1/{module}/{entity}/${id}`);
  }
};
```

### 移动端API调用模板

```javascript
// src/api/mobile-{module}.js
import { get, post } from '@/lib/request';

export const mobile{Module}Api = {
  // 移动端专用接口
  mobileAction: (data) => {
    return post('/api/v1/mobile/{module}/action', data, {
      encrypt: true // 移动端敏感数据加密
    });
  },

  // 快速操作
  quickOperation: (params) => {
    return get('/api/v1/mobile/{module}/quick', { params });
  },

  // 位置相关
  locationBased: (latitude, longitude, radius = 500) => {
    return get('/api/v1/mobile/{module}/nearby', {
      latitude,
      longitude,
      radius
    });
  }
};
```

## 📝 数据模型示例

### 请求DTO

```java
@Data
@Schema(description = "{entity}创建请求")
public class {Entity}CreateDTO {

    @NotNull(message = "{field}不能为空")
    @Schema(description = "{field}说明", example = "1001")
    private Long {field};

    @Size(max = 100, message = "{field}长度不能超过100字符")
    @Schema(description = "{field}说明", example = "示例值")
    private String {field};
}
```

### 响应VO

```java
@Data
@Schema(description = "{entity}详情")
public class {Entity}DetailVO {

    @Schema(description = "ID", example = "1001")
    private Long id;

    @Schema(description = "{field}说明", example = "示例值")
    private String {field};

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
```

## 🚨 常见错误和解决方案

### 1. 跨域问题

```java
// Web配置
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### 2. 参数验证

```java
// 全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException e) {

        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage
                ));

        return ResponseDTO.error(400, "参数验证失败", errors);
    }
}
```

### 3. Token过期处理

```javascript
// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const res = response.data;

    // Token过期处理
    if (res.code === 30007 || res.code === 30008) {
      removeToken();
      window.location.href = '/login';
    }

    return res;
  },
  (error) => {
    // 错误处理
    return Promise.reject(error);
  }
);
```

## 📊 性能优化建议

### 1. 后端优化

```yaml
# 数据库查询
- 使用分页查询，避免全表扫描
- 合理使用索引，提高查询效率
- 使用连接池，优化数据库连接

# 缓存策略
- Redis缓存热点数据
- 本地缓存减少网络请求
- 多级缓存提升性能

# 响应优化
- 压缩响应数据
- 异步处理耗时操作
- 批量接口减少请求次数
```

### 2. 前端优化

```javascript
// 请求优化
- 使用防抖减少重复请求
- 实现请求取消机制
- 批量请求合并处理

// 数据处理
- 虚拟滚动处理大数据
- 图片懒加载
- 组件按需加载

// 缓存策略
- HTTP缓存合理配置
- 本地数据缓存
- 接口结果缓存
```

## 🔍 接口检查清单

### 开发前检查

- [ ] URL设计符合RESTful规范
- [ ] HTTP方法使用正确
- [ ] 请求参数验证完整
- [ ] 响应数据格式统一
- [ ] 错误处理机制完善
- [ ] 权限控制配置正确

### 开发后检查

- [ ] Swagger注解完整
- [ ] 单元测试覆盖
- [ ] 接口性能测试
- [ ] 安全漏洞检查
- [ ] 文档更新完整

### 部署前检查

- [ ] 环境配置正确
- [ ] 监控告警配置
- [ ] 日志记录完善
- [ ] 版本兼容性确认
- [ ] 灰度发布准备

## 📚 相关文档

- [详细API开发规范](../../../documentation/technical/API_DEVELOPMENT_STANDARDS.md)
- [数据库设计规范](../../../documentation/technical/DATABASE_DESIGN_STANDARDS.md)
- [安全开发规范](../../../documentation/technical/SECURITY_DEVELOPMENT_STANDARDS.md)
- [微服务开发指南](../../../documentation/technical/MICROSERVICES_DEVELOPMENT_GUIDE.md)

## 🤝 贡献指南

1. 遵循本规范进行API开发
2. 新增接口需要更新文档
3. 发现问题请及时反馈
4. 建议改进请提交PR

---

**维护团队**: IOE-DREAM架构团队
**更新频率**: 季度更新或重大变更时更新
**联系方式**: 项目Issue或架构团队邮箱