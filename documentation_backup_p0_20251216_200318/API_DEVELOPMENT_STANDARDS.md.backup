# IOE-DREAM API开发规范

> **版本**: v1.0.0
> **生效日期**: 2025-12-15
> **技术栈**: Spring Boot 3.5.8 + Spring Cloud 2025.0.0 + Vue 3.4 + uni-app 3.0
> **适用范围**: IOE-DREAM智慧园区一卡通管理平台所有微服务和前端应用

---

## 📋 目录

1. [API设计原则](#1-api设计原则)
2. [URL设计规范](#2-url设计规范)
3. [HTTP方法规范](#3-http方法规范)
4. [请求响应规范](#4-请求响应规范)
5. [数据模型规范](#5-数据模型规范)
6. [错误处理规范](#6-错误处理规范)
7. [版本控制规范](#7-版本控制规范)
8. [安全规范](#8-安全规范)
9. [文档规范](#9-文档规范)
10. [前端调用规范](#10-前端调用规范)
11. [移动端调用规范](#11-移动端调用规范)

---

## 1. API设计原则

### 1.1 核心原则

**RESTful设计**：严格遵循REST架构风格，使用统一的资源定位符

**统一性**：所有API遵循统一的设计规范和响应格式

**向后兼容**：API变更必须保证向后兼容，新增字段不影响老版本

**幂等性**：GET、PUT、DELETE操作必须是幂等的

**可预测性**：API行为应该是可预测的，一致的命名和行为

### 1.2 设计目标

```yaml
性能目标:
  - 响应时间: < 200ms (P95)
  - 并发支持: 1000+ TPS
  - 可用性: 99.9%

质量目标:
  - 接口一致性: 100%
  - 文档完整性: 100%
  - 测试覆盖率: ≥ 80%
```

---

## 2. URL设计规范

### 2.1 基础URL结构

```yaml
# 标准URL格式
{base_url}/api/v{version}/{module}/{entity}[/{action}][?{query_params}]

# 示例
https://api.ioedream.com/api/v1/access/record/query
https://api.ioedream.com/api/v1/consume/transaction/execute
https://api.ioedream.com/api/v1/mobile/access/check
```

### 2.2 URL命名规范

| 组件 | 规范 | 示例 |
|------|------|------|
| **base_url** | 域名或IP | `https://api.ioedream.com` |
| **api** | 固定前缀 | `/api` |
| **version** | 版本号 | `/v1`, `/v2` |
| **module** | 业务模块 | `/access`, `/consume`, `/visitor` |
| **entity** | 资源实体 | `/record`, `/device`, `/transaction` |
| **action** | 操作动作 | `/query`, `/create`, `/update` |

### 2.3 URL命名约定

```yaml
# 使用小写字母和连字符
✅ /api/v1/access-control/record
✅ /api/v1/consume-transaction/execute

# 使用复数形式表示资源集合
✅ /api/v1/users
✅ /api/v1/devices

# 避免使用文件扩展名
❌ /api/v1/users.json
❌ /api/v1/devices.xml
```

### 2.4 模块划分标准

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| **门禁管理** | `/api/v1/access` | 门禁设备、通行记录、权限验证 |
| **消费管理** | `/api/v1/consume` | 消费交易、账户管理、退款处理 |
| **访客管理** | `/api/v1/visitor` | 访客预约、访客登记、访问记录 |
| **考勤管理** | `/api/v1/attendance` | 考勤打卡、排班管理、加班统计 |
| **视频监控** | `/api/v1/video` | 视频设备、录像回放、AI分析 |
| **设备管理** | `/api/v1/device` | 设备信息、协议适配、状态监控 |
| **用户管理** | `/api/v1/system/user` | 用户信息、权限管理、组织架构 |
| **公共接口** | `/api/v1/support` | 字典管理、文件上传、系统配置 |

---

## 3. HTTP方法规范

### 3.1 方法语义

| HTTP方法 | 操作类型 | 幂等性 | 安全性 | 使用场景 |
|---------|---------|--------|--------|----------|
| **GET** | 查询 | ✅ | ✅ | 获取资源、列表查询、详情查询 |
| **POST** | 创建 | ❌ | ❌ | 创建资源、执行业务操作、批量处理 |
| **PUT** | 更新 | ✅ | ❌ | 全量更新资源、替换资源 |
| **PATCH** | 部分更新 | ❌ | ❌ | 部分更新字段、状态修改 |
| **DELETE** | 删除 | ✅ | ❌ | 删除资源、批量删除 |

### 3.2 使用规范

```java
// GET - 查询操作
@GetMapping("/users/{id}")
public ResponseDTO<UserVO> getUser(@PathVariable Long id) {}

@GetMapping("/users")
public ResponseDTO<PageResult<UserVO>> queryUsers(UserQueryForm form) {}

// POST - 创建操作
@PostMapping("/users")
public ResponseDTO<UserVO> createUser(@Valid @RequestBody UserCreateForm form) {}

@PostMapping("/users/batch")
public ResponseDTO<List<UserVO>> batchCreateUsers(@Valid @RequestBody List<UserCreateForm> forms) {}

// PUT - 更新操作
@PutMapping("/users/{id}")
public ResponseDTO<UserVO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateForm form) {}

// DELETE - 删除操作
@DeleteMapping("/users/{id}")
public ResponseDTO<Void> deleteUser(@PathVariable Long id) {}

@DeleteMapping("/users")
public ResponseDTO<Void> batchDeleteUsers(@RequestParam List<Long> ids) {}
```

### 3.3 特殊场景规范

```yaml
# 文件上传
POST /api/v1/support/file/upload
Content-Type: multipart/form-data

# 文件下载
GET /api/v1/support/file/download/{fileId}

# 批量操作
POST /api/v1/users/batch/create
POST /api/v1/users/batch/update
DELETE /api/v1/users/batch

# 业务操作（非CRUD）
POST /api/v1/consume/transaction/execute
POST /api/v1/access/permission/grant
POST /api/v1/visitor/appointment/approve
```

---

## 4. 请求响应规范

### 4.1 请求格式规范

#### 4.1.1 请求头规范

```yaml
# 标准请求头
Content-Type: application/json;charset=UTF-8
Accept: application/json
Authorization: Bearer {access_token}
X-Request-ID: {unique_request_id}
X-Timestamp: {unix_timestamp}
X-Client-Version: {client_version}

# 移动端特有
X-Platform: ios|android|web
X-Device-ID: {unique_device_id}
X-App-Version: {app_version}
```

#### 4.1.2 请求体格式

```java
// 标准请求DTO
@Data
public class ConsumeTransactionRequestDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    @Schema(description = "消费金额", example = "15.50")
    private BigDecimal amount;

    @Schema(description = "消费方式", example = "FACE", allowableValues = {"CARD", "FACE", "NFC", "QR"})
    private String consumeMode;

    @Schema(description = "设备信息")
    private DeviceInfoDTO deviceInfo;

    @Schema(description = "扩展属性")
    private Map<String, Object> extendedAttributes;
}

// 嵌套对象
@Data
public class DeviceInfoDTO {
    @Schema(description = "设备ID", example = "DEV001")
    private String deviceId;

    @Schema(description = "设备位置", example = "A栋1楼大厅")
    private String location;

    @Schema(description = "GPS坐标")
    private GPSCoordinatesDTO gps;
}
```

### 4.2 响应格式规范

#### 4.2.1 统一响应格式

```java
// 标准响应DTO
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {

    @Schema(description = "业务状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "时间戳", example = "1703847600000")
    private Long timestamp;

    @Schema(description = "请求追踪ID", example = "req_123456789")
    private String traceId;

    // 成功响应
    public static <T> ResponseDTO<T> ok(T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    // 成功响应(无数据)
    public static <T> ResponseDTO<T> ok() {
        return ok(null);
    }

    // 错误响应
    public static <T> ResponseDTO<T> error(Integer code, String message) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(code);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
```

#### 4.2.2 分页响应格式

```java
// 分页结果DTO
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "总记录数", example = "1000")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "总页数", example = "50")
    private Integer pages;

    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrev;

    // 计算分页信息
    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((int) Math.ceil((double) total / pageSize));
        result.setHasNext(pageNum < result.getPages());
        result.setHasPrev(pageNum > 1);
        return result;
    }
}
```

#### 4.2.3 响应示例

```yaml
# 成功响应
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "name": "张三",
    "email": "zhangsan@company.com"
  },
  "timestamp": 1703847600000,
  "traceId": "req_123456789"
}

# 分页响应
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [...],
    "total": 1000,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 50,
    "hasNext": true,
    "hasPrev": false
  },
  "timestamp": 1703847600000,
  "traceId": "req_123456789"
}

# 错误响应
{
  "code": 400,
  "message": "参数验证失败",
  "data": null,
  "timestamp": 1703847600000,
  "traceId": "req_123456789"
}
```

---

## 5. 数据模型规范

### 5.1 实体设计规范

#### 5.1.1 基础实体

```java
// 基础实体 - 所有业务实体继承
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_{$table_name}")
public class BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID", example = "1001")
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "1703847600")
    private Long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "1703847600")
    private Long updateTime;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人ID", example = "1001")
    private Long createUserId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID", example = "1001")
    private Long updateUserId;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    @Version
    @Schema(description = "乐观锁版本号", example = "1")
    private Integer version;
}
```

#### 5.1.2 业务实体示例

```java
// 消费交易实体
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_transaction")
@Schema(description = "消费交易记录")
public class ConsumeTransactionEntity extends BaseEntity {

    @NotNull
    @Schema(description = "交易流水号", example = "TXN2025121500001")
    @TableField("transaction_no")
    private String transactionNo;

    @NotNull
    @Schema(description = "用户ID", example = "1001")
    @TableField("user_id")
    private Long userId;

    @NotNull
    @DecimalMin(value = "0.01")
    @Schema(description = "交易金额", example = "15.50")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "消费方式", example = "FACE")
    @TableField("consume_mode")
    private String consumeMode;

    @Schema(description = "设备ID", example = "DEV001")
    @TableField("device_id")
    private String deviceId;

    @Schema(description = "交易状态", example = "SUCCESS")
    @TableField("status")
    private String status;

    @Schema(description = "交易时间")
    @TableField("transaction_time")
    private LocalDateTime transactionTime;

    @Schema(description = "扩展属性")
    @TableField("extended_attributes")
    private String extendedAttributes; // JSON格式
}
```

### 5.2 DTO设计规范

#### 5.2.1 请求DTO

```java
// 创建请求DTO
@Data
@Schema(description = "消费交易创建请求")
public class ConsumeTransactionCreateDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    @DecimalMax(value = "10000.00", message = "消费金额不能超过10000")
    @Schema(description = "消费金额", example = "15.50")
    private BigDecimal amount;

    @NotBlank(message = "消费方式不能为空")
    @Pattern(regexp = "^(CARD|FACE|NFC|QR)$", message = "消费方式必须是CARD、FACE、NFC、QR之一")
    @Schema(description = "消费方式", example = "FACE", allowableValues = {"CARD", "FACE", "NFC", "QR"})
    private String consumeMode;

    @Schema(description = "设备信息")
    @Valid
    private DeviceInfoDTO deviceInfo;

    @Schema(description = "备注信息")
    @Size(max = 500, message = "备注信息不能超过500字符")
    private String remark;
}

// 查询请求DTO
@Data
@Schema(description = "消费交易查询请求")
public class ConsumeTransactionQueryDTO {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "交易流水号", example = "TXN2025121500001")
    private String transactionNo;

    @Schema(description = "消费方式", example = "FACE")
    private String consumeMode;

    @Schema(description = "交易状态", example = "SUCCESS")
    private String status;

    @Schema(description = "开始时间", example = "2025-12-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    @Schema(description = "每页大小", example = "20", defaultValue = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段", example = "transactionTime")
    private String orderBy = "transactionTime";

    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String orderDirection = "desc";
}
```

#### 5.2.2 响应VO

```java
// 详情响应VO
@Data
@Schema(description = "消费交易详情")
public class ConsumeTransactionDetailVO {

    @Schema(description = "交易ID", example = "1001")
    private Long id;

    @Schema(description = "交易流水号", example = "TXN2025121500001")
    private String transactionNo;

    @Schema(description = "用户信息")
    private UserBasicInfoVO userInfo;

    @Schema(description = "交易金额", example = "15.50")
    private BigDecimal amount;

    @Schema(description = "消费方式", example = "FACE")
    private String consumeMode;

    @Schema(description = "消费方式名称", example = "人脸识别")
    private String consumeModeName;

    @Schema(description = "设备信息")
    private DeviceBasicInfoVO deviceInfo;

    @Schema(description = "交易状态", example = "SUCCESS")
    private String status;

    @Schema(description = "交易状态名称", example = "交易成功")
    private String statusName;

    @Schema(description = "交易时间", example = "2025-12-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "创建时间", example = "2025-12-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

// 列表响应VO
@Data
@Schema(description = "消费交易列表项")
public class ConsumeTransactionListItemVO {

    @Schema(description = "交易ID", example = "1001")
    private Long id;

    @Schema(description = "交易流水号", example = "TXN2025121500001")
    private String transactionNo;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "用户编号", example = "EMP001")
    private String userNo;

    @Schema(description = "交易金额", example = "15.50")
    private BigDecimal amount;

    @Schema(description = "消费方式", example = "FACE")
    private String consumeMode;

    @Schema(description = "消费方式名称", example = "人脸识别")
    private String consumeModeName;

    @Schema(description = "设备位置", example = "A栋1楼餐厅")
    private String deviceLocation;

    @Schema(description = "交易状态", example = "SUCCESS")
    private String status;

    @Schema(description = "交易状态名称", example = "交易成功")
    private String statusName;

    @Schema(description = "交易时间", example = "2025-12-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;
}
```

### 5.3 数据转换规范

```java
// DTO转换器
@Component
public class ConsumeTransactionConverter {

    @Resource
    private UserConverter userConverter;

    @Resource
    private DeviceConverter deviceConverter;

    // Entity -> DetailVO
    public ConsumeTransactionDetailVO toDetailVO(ConsumeTransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        ConsumeTransactionDetailVO vo = new ConsumeTransactionDetailVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置用户信息
        vo.setUserInfo(userConverter.toBasicInfo(entity.getUser()));

        // 设置设备信息
        vo.setDeviceInfo(deviceConverter.toBasicInfo(entity.getDevice()));

        // 设置枚举名称
        vo.setConsumeModeName(ConsumeModeEnum.getNameByCode(entity.getConsumeMode()));
        vo.setStatusName(TransactionStatusEnum.getNameByCode(entity.getStatus()));

        return vo;
    }

    // Entity -> ListItemVO
    public ConsumeTransactionListItemVO toListItemVO(ConsumeTransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        ConsumeTransactionListItemVO vo = new ConsumeTransactionListItemVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置用户基本信息
        vo.setUserName(entity.getUser().getUserName());
        vo.setUserNo(entity.getUser().getUserNo());

        // 设置设备位置
        vo.setDeviceLocation(entity.getDevice().getLocation());

        // 设置枚举名称
        vo.setConsumeModeName(ConsumeModeEnum.getNameByCode(entity.getConsumeMode()));
        vo.setStatusName(TransactionStatusEnum.getNameByCode(entity.getStatus()));

        return vo;
    }
}
```

---

## 6. 错误处理规范

### 6.1 错误码设计

#### 6.1.1 错误码结构

```
错误码格式: {模块码}{错误类型码}{具体错误码}
- 模块码: 2位数字 (01-99)
- 错误类型码: 1位数字 (1-9)
- 具体错误码: 2位数字 (01-99)

示例:
- 1001: 系统通用错误
- 1101: 用户模块参数错误
- 1201: 门禁模块业务错误
- 1301: 消费模块业务错误
```

#### 6.1.2 错误码分类

| 模块码 | 模块名称 | 说明 |
|--------|---------|------|
| **10** | **系统通用** | 系统级错误、认证授权、网络等 |
| **11** | **用户管理** | 用户、权限、组织架构等 |
| **12** | **门禁管理** | 门禁设备、通行记录、权限验证等 |
| **13** | **消费管理** | 消费交易、账户管理、退款等 |
| **14** | **访客管理** | 访客预约、访客登记等 |
| **15** | **考勤管理** | 考勤打卡、排班管理等 |
| **16** | **视频监控** | 视频设备、录像回放等 |
| **17** | **设备管理** | 设备信息、协议适配等 |
| **18** | **OA办公** | 工作流、审批等 |

| 错误类型码 | 类型说明 | HTTP状态码 |
|------------|---------|-----------|
| **1** | 参数错误 | 400 |
| **2** | 认证错误 | 401 |
| **3** | 授权错误 | 403 |
| **4** | 资源不存在 | 404 |
| **5** | 业务错误 | 422 |
| **9** | 系统错误 | 500 |

### 6.2 异常处理实现

#### 6.2.1 自定义异常

```java
// 业务异常基类
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private final Integer code;
    private final String message;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        this(500, message);
    }
}

// 参数验证异常
public class ParamException extends BusinessException {
    public ParamException(String message) {
        super(400, message);
    }
}

// 认证异常
public class AuthenticationException extends BusinessException {
    public AuthenticationException(String message) {
        super(401, message);
    }
}

// 授权异常
public class AuthorizationException extends BusinessException {
    public AuthorizationException(String message) {
        super(403, message);
    }
}

// 资源不存在异常
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
}
```

#### 6.2.2 全局异常处理器

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 业务异常处理
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    // 参数验证异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException e) {

        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    error -> {
                        String defaultMessage = error.getDefaultMessage();
                        // 替换占位符参数
                        if (defaultMessage != null && defaultMessage.contains("{")) {
                            return defaultMessage.replaceAll("\\{[^}]*\\}", "");
                        }
                        return defaultMessage;
                    },
                    (existing, replacement) -> existing
                ));

        log.warn("[参数验证异常] errors={}", errors);
        return ResponseDTO.error(1001, "参数验证失败", errors);
    }

    // 约束违反异常处理
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Map<String, String>> handleConstraintViolationException(
            ConstraintViolationException e) {

        Map<String, String> errors = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                    violation -> {
                        String propertyPath = violation.getPropertyPath().toString();
                        return propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                    },
                    violation -> {
                        String message = violation.getMessage();
                        return message.replaceAll("\\{[^}]*\\}", "");
                    },
                    (existing, replacement) -> existing
                ));

        log.warn("[约束违反异常] errors={}", errors);
        return ResponseDTO.error(1001, "参数验证失败", errors);
    }

    // 系统异常处理
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleException(Exception e) {
        String traceId = MDC.get("traceId");
        log.error("[系统异常] traceId={}, error={}", traceId, e.getMessage(), e);
        return ResponseDTO.error(5000, "系统内部错误，请稍后重试");
    }
}
```

---

## 7. 版本控制规范

### 7.1 API版本策略

#### 7.1.1 版本控制方式

```yaml
# URL路径版本控制（推荐）
/api/v1/users
/api/v2/users

# 请求头版本控制（备选）
Accept: application/vnd.ioedream.v1+json
Accept: application/vnd.ioedream.v2+json

# 查询参数版本控制（不推荐）
/users?version=v1
```

#### 7.1.2 版本兼容性

```yaml
# 版本兼容性矩阵
v1: 基础版本，核心功能
v2: 增强版本，新增字段，向后兼容v1
v3: 重大更新，可能不兼容v1，但兼容v2

# 版本生命周期
- 发布后至少维护2年
- 新版本发布后，旧版本维持1年支持
- 废弃版本提前6个月通知
```

### 7.2 版本演进策略

```java
// v1版本Controller
@RestController
@RequestMapping("/api/v1/users")
@Api(tags = "用户管理v1")
public class UserV1Controller {

    @GetMapping("/{id}")
    public ResponseDTO<UserV1VO> getUser(@PathVariable Long id) {
        // v1版本实现
    }
}

// v2版本Controller - 向后兼容
@RestController
@RequestMapping("/api/v2/users")
@Api(tags = "用户管理v2")
public class UserV2Controller {

    @GetMapping("/{id}")
    public ResponseDTO<UserV2VO> getUser(@PathVariable Long id) {
        // v2版本实现，包含更多字段
        // 同时支持v1客户端访问时降级返回
    }

    @GetMapping("/v1-compat/{id}")
    public ResponseDTO<UserV1VO> getUserV1Compat(@PathVariable Long id) {
        // v1兼容接口，支持v1客户端无缝升级
    }
}
```

---

## 8. 安全规范

### 8.1 认证授权

#### 8.1.1 Token规范

```yaml
# JWT Token结构
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "1001",              # 用户ID
  "username": "zhangsan",     # 用户名
  "roles": ["USER", "EMPLOYEE"], # 角色列表
  "permissions": ["access:read", "consume:create"], # 权限列表
  "iat": 1703847600,          # 签发时间
  "exp": 1703851200,          # 过期时间
  "iss": "ioedream-api",      # 签发者
  "aud": "ioedream-client"    # 受众
}

Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

#### 8.1.2 权限控制

```java
// 方法级权限控制
@RestController
@RequestMapping("/api/v1/consume")
@PreAuthorize("hasRole('CONSUME_USER') or hasRole('CONSUME_MANAGER')")
public class ConsumeController {

    @PostMapping("/transaction/execute")
    @PreAuthorize("hasPermission('consume:transaction:execute')")
    public ResponseDTO<ConsumeResultVO> executeTransaction(
            @Valid @RequestBody ConsumeTransactionCreateDTO request) {
        // 执行消费交易
    }

    @GetMapping("/account/balance")
    @PreAuthorize("hasPermission('consume:account:read')")
    public ResponseDTO<AccountBalanceVO> getAccountBalance(
            @RequestParam Long userId) {
        // 查询账户余额
    }

    // 数据级权限控制
    @GetMapping("/transaction/query")
    @PreAuthorize("@dataPermissionService.hasPermission(authentication, #queryDTO.userId)")
    public ResponseDTO<PageResult<ConsumeTransactionListItemVO>> queryTransactions(
            @Valid ConsumeTransactionQueryDTO queryDTO) {
        // 查询交易记录（只能查询有权限的数据）
    }
}
```

### 8.2 数据安全

#### 8.2.1 数据加密

```java
// 请求加密
@Component
public class RequestEncryptor {

    @Value("${app.api.encrypt.key}")
    private String encryptKey;

    @Value("${app.api.encrypt.enabled:false}")
    private Boolean encryptEnabled;

    public String encrypt(String data) {
        if (!encryptEnabled || StringUtils.isEmpty(data)) {
            return data;
        }

        try {
            // AES加密实现
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptKey.getBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, new byte[12]);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("数据加密失败", e);
            return data;
        }
    }
}

// 响应解密
@Component
public class ResponseDecryptor {

    public String decrypt(String encryptedData) {
        if (StringUtils.isEmpty(encryptedData)) {
            return encryptedData;
        }

        try {
            // AES解密实现
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptKey.getBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, new byte[12]);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (Exception e) {
            log.error("数据解密失败", e);
            return encryptedData;
        }
    }
}
```

#### 8.2.2 敏感数据处理

```java
// 敏感数据脱敏
@Component
public class DataMasking {

    // 手机号脱敏
    public String maskPhone(String phone) {
        if (StringUtils.isEmpty(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    // 身份证脱敏
    public String maskIdCard(String idCard) {
        if (StringUtils.isEmpty(idCard) || idCard.length() < 18) {
            return idCard;
        }
        return idCard.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2");
    }

    // 银行卡号脱敏
    public String maskBankCard(String bankCard) {
        if (StringUtils.isEmpty(bankCard) || bankCard.length() < 16) {
            return bankCard;
        }
        return bankCard.replaceAll("(\\d{4})\\d+(\\d{4})", "$1 **** **** $2");
    }
}

// 在VO中使用脱敏
@Data
public class UserDetailVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户姓名")
    private String name;

    @Schema(description = "手机号（脱敏）")
    private String maskedPhone;

    @Schema(description = "身份证号（脱敏）")
    private String maskedIdCard;

    @Schema(description = "邮箱地址")
    private String email;
}
```

---

## 9. 文档规范

### 9.1 Swagger注解规范

```java
// Controller文档
@RestController
@RequestMapping("/api/v1/consume/transaction")
@Tag(name = "消费交易管理", description = "消费交易相关的API接口")
public class ConsumeTransactionController {

    @Operation(
        summary = "执行消费交易",
        description = "执行消费交易，支持多种支付方式",
        tags = {"消费交易管理"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "交易执行成功",
                     content = @Content(schema = @Schema(implementation = ConsumeResultVO.class))),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping("/execute")
    @PreAuthorize("hasPermission('consume:transaction:execute')")
    public ResponseDTO<ConsumeResultVO> executeTransaction(
            @Parameter(description = "消费交易请求", required = true)
            @Valid @RequestBody ConsumeTransactionCreateDTO request) {
        // 实现逻辑
    }
}

// 实体文档
@Data
@Schema(description = "消费交易创建请求")
public class ConsumeTransactionCreateDTO {

    @Schema(description = "用户ID", example = "1001", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "消费金额", example = "15.50", required = true)
    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "消费方式", example = "FACE", required = true,
             allowableValues = {"CARD", "FACE", "NFC", "QR"})
    private String consumeMode;
}
```

### 9.2 API文档生成

```yaml
# application.yml配置
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
  info:
    title: IOE-DREAM API文档
    description: 智慧园区一卡通管理平台API接口文档
    version: v1.0.0
    contact:
      name: IOE-DREAM开发团队
      email: dev@ioedream.com
    license:
      name: MIT License
      url: https://opensource.org/licenses/MIT
  servers:
    - url: https://api.ioedream.com/api/v1
      description: 生产环境
    - url: https://api-test.ioedream.com/api/v1
      description: 测试环境
    - url: http://localhost:8080/api/v1
      description: 开发环境
```

---

## 10. 前端调用规范

### 10.1 PC端调用规范

#### 10.1.1 Axios配置

```javascript
// src/lib/api.js
import axios from 'axios';
import { getToken, removeToken } from '@/lib/auth';
import { message } from 'ant-design-vue';

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL + '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
  }
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加token
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // 添加请求ID
    config.headers['X-Request-ID'] = generateRequestId();

    // 添加时间戳
    config.headers['X-Timestamp'] = Date.now();

    // 数据加密
    if (config.encrypt) {
      config.data = encryptData(config.data);
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 解密数据
    if (res.dataType === 'ENCRYPT') {
      res.data = decryptData(res.data);
    }

    // 处理成功响应
    if (res.code === 200 || res.success === true) {
      return res;
    }

    // 处理业务错误
    if (res.code === 30007 || res.code === 30008 || res.code === 30012) {
      message.error('登录已过期，请重新登录');
      removeToken();
      window.location.href = '/login';
      return Promise.reject(new Error(res.message));
    }

    // 其他业务错误
    message.error(res.message || '操作失败');
    return Promise.reject(new Error(res.message));
  },
  (error) => {
    // 网络错误处理
    if (error.code === 'ECONNABORTED') {
      message.error('请求超时，请稍后重试');
    } else if (error.response) {
      const status = error.response.status;
      switch (status) {
        case 401:
          message.error('未认证，请登录');
          break;
        case 403:
          message.error('无权限访问');
          break;
        case 404:
          message.error('请求的资源不存在');
          break;
        case 500:
          message.error('服务器内部错误');
          break;
        default:
          message.error('网络错误，请稍后重试');
      }
    } else {
      message.error('网络连接失败');
    }

    return Promise.reject(error);
  }
);

export default api;
```

#### 10.1.2 API调用示例

```javascript
// src/api/consume.js
import api from '@/lib/api';

// 消费交易API
export const consumeApi = {
  // 执行消费交易
  executeTransaction: (data) => {
    return api.post('/consume/transaction/execute', data, {
      encrypt: true // 敏感数据加密
    });
  },

  // 查询交易记录
  queryTransactions: (params) => {
    return api.get('/consume/transaction/query', { params });
  },

  // 获取交易详情
  getTransactionDetail: (id) => {
    return api.get(`/consume/transaction/${id}`);
  },

  // 获取账户余额
  getAccountBalance: (userId) => {
    return api.get(`/consume/account/balance`, { params: { userId } });
  }
};

// Vue组件中使用
import { consumeApi } from '@/api/consume';

export default {
  data() {
    return {
      transactions: [],
      loading: false
    };
  },

  methods: {
    async loadTransactions() {
      this.loading = true;
      try {
        const response = await consumeApi.queryTransactions({
          pageNum: 1,
          pageSize: 20
        });

        if (response.code === 200) {
          this.transactions = response.data.list;
        }
      } catch (error) {
        console.error('加载交易记录失败:', error);
      } finally {
        this.loading = false;
      }
    }
  }
};
```

### 10.2 TypeScript类型定义

```typescript
// src/types/consume.ts
export interface ConsumeTransactionRequest {
  userId: number;
  amount: number;
  consumeMode: 'CARD' | 'FACE' | 'NFC' | 'QR';
  deviceInfo: DeviceInfo;
  remark?: string;
}

export interface DeviceInfo {
  deviceId: string;
  location: string;
  gps?: {
    latitude: number;
    longitude: number;
  };
}

export interface ConsumeTransactionResponse {
  id: number;
  transactionNo: string;
  amount: number;
  status: string;
  statusName: string;
  transactionTime: string;
  userInfo: {
    id: number;
    name: string;
    userNo: string;
  };
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
  hasNext: boolean;
  hasPrev: boolean;
}
```

---

## 11. 移动端调用规范

### 11.1 uni-app请求封装

```javascript
// src/lib/request.js
import { getToken, removeToken } from '@/lib/auth';

const BASE_URL = process.env.VUE_APP_API_BASE_URL + '/api/v1';

// 请求封装
export const request = (options) => {
  return new Promise((resolve, reject) => {
    // 获取token
    const token = getToken();

    // 请求配置
    const config = {
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        'X-Platform': uni.getSystemInfoSync().platform,
        'X-Device-ID': getDeviceId(),
        'X-App-Version': process.env.VUE_APP_VERSION,
        'X-Request-ID': generateRequestId(),
        'X-Timestamp': Date.now(),
        ...options.header
      },
      timeout: options.timeout || 30000
    };

    // 数据加密
    if (options.encrypt) {
      config.data = encryptData(config.data);
    }

    uni.request({
      ...config,
      success: (response) => {
        const res = response.data;

        // 数据解密
        if (res.dataType === 'ENCRYPT') {
          res.data = decryptData(res.data);
        }

        // 处理响应
        handleResponse(res, resolve, reject);
      },
      fail: (error) => {
        handleRequestError(error, reject);
      }
    });
  });
};

// 响应处理
const handleResponse = (res, resolve, reject) => {
  if (res.code === 200 || res.success === true) {
    resolve(res);
    return;
  }

  // Token过期处理
  if (res.code === 30007 || res.code === 30008 || res.code === 30012) {
    uni.showToast({
      title: '登录已过期',
      icon: 'none'
    });
    removeToken();
    uni.reLaunch({
      url: '/pages/login/login'
    });
    reject(new Error(res.message));
    return;
  }

  // 其他错误
  uni.showToast({
    title: res.message || '操作失败',
    icon: 'none'
  });
  reject(new Error(res.message));
};

// 请求错误处理
const handleRequestError = (error, reject) => {
  console.error('请求错误:', error);

  let message = '网络错误，请稍后重试';

  if (error.errMsg) {
    if (error.errMsg.includes('timeout')) {
      message = '请求超时，请稍后重试';
    } else if (error.errMsg.includes('fail')) {
      message = '网络连接失败';
    }
  }

  uni.showToast({
    title: message,
    icon: 'none'
  });

  reject(new Error(message));
};

// 便捷方法
export const get = (url, params = {}) => {
  return request({
    url,
    method: 'GET',
    data: params
  });
};

export const post = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  });
};

export const put = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  });
};

export const del = (url, data = {}) => {
  return request({
    url,
    method: 'DELETE',
    data
  });
};
```

### 11.2 移动端API调用

```javascript
// src/api/consume.js
import { get, post } from '@/lib/request';

export const consumeApi = {
  // 移动端快速消费
  quickConsume: (data) => {
    return post('/mobile/consume/quick', data, {
      encrypt: true
    });
  },

  // 扫码消费
  scanConsume: (qrCode, amount) => {
    return post('/mobile/consume/scan', {
      qrCode,
      amount
    });
  },

  // NFC消费
  nfcConsume: (nfcData, amount) => {
    return post('/mobile/consume/nfc', {
      nfcData,
      amount
    });
  },

  // 人脸识别消费
  faceConsume: (faceData, amount) => {
    return post('/mobile/consume/face', {
      faceData,
      amount
    }, {
      encrypt: true // 生物识别数据需要加密
    });
  },

  // 获取账户余额
  getBalance: () => {
    return get('/mobile/consume/balance');
  },

  // 获取消费记录
  getConsumeRecords: (params = {}) => {
    return get('/mobile/consume/records', params);
  }
};

// Vue页面中使用
import { consumeApi } from '@/api/consume';

export default {
  data() {
    return {
      balance: 0,
      loading: false
    };
  },

  methods: {
    // 刷新余额
    async refreshBalance() {
      try {
        const response = await consumeApi.getBalance();
        if (response.code === 200) {
          this.balance = response.data.balance;
        }
      } catch (error) {
        console.error('获取余额失败:', error);
      }
    },

    // 执行消费
    async executeConsume(amount) {
      this.loading = true;
      uni.showLoading({
        title: '正在支付...'
      });

      try {
        const response = await consumeApi.quickConsume({
          amount: amount,
          consumeMode: 'FACE'
        });

        if (response.code === 200) {
          uni.showToast({
            title: '支付成功',
            icon: 'success'
          });

          // 刷新余额
          await this.refreshBalance();
        }
      } catch (error) {
        uni.showToast({
          title: '支付失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
        uni.hideLoading();
      }
    }
  }
};
```

### 11.3 移动端特殊功能

```javascript
// 生物识别
export const biometricApi = {
  // 人脸识别
  faceRecognize: (imageData) => {
    return post('/mobile/biometric/face/recognize', {
      imageData,
      timestamp: Date.now()
    }, {
      encrypt: true // 生物识别数据必须加密
    });
  },

  // 指纹识别
  fingerprintRecognize: (fingerprintData) => {
    return post('/mobile/biometric/fingerprint/recognize', {
      fingerprintData,
      timestamp: Date.now()
    }, {
      encrypt: true
    });
  }
};

// 位置服务
export const locationApi = {
  // 获取附近设备
  getNearbyDevices: (latitude, longitude, radius = 500) => {
    return get('/mobile/access/devices/nearby', {
      latitude,
      longitude,
      radius
    });
  },

  // 位置签到
  checkIn: (deviceId, location) => {
    return post('/mobile/attendance/checkin', {
      deviceId,
      location: {
        latitude: location.latitude,
        longitude: location.longitude,
        address: location.address
      }
    });
  }
};

// 离线同步
export const syncApi = {
  // 同步离线数据
  syncOfflineData: (offlineData) => {
    return post('/mobile/sync/offline', offlineData);
  },

  // 获取同步时间戳
  getSyncTimestamp: () => {
    return get('/mobile/sync/timestamp');
  }
};
```

---

## 📚 参考资源

### 最佳实践参考

- [Spring Boot REST API Best Practices](https://spring.io/guides/tutorials/rest/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Vue 3 Composition API Best Practices](https://vuejs.org/guide/extras/composition-api-faq.html)
- [uni-app 开发指南](https://uniapp.dcloud.net.cn/)

### 相关文档

- [IOE-DREAM 项目架构设计文档](../architecture/)
- [IOE-DREAM 数据库设计规范](./DATABASE_DESIGN_STANDARDS.md)
- [IOE-DREAM 安全开发规范](./SECURITY_DEVELOPMENT_STANDARDS.md)
- [IOE-DREAM 微服务开发指南](./MICROSERVICES_DEVELOPMENT_GUIDE.md)

---

## 📝 更新日志

| 版本 | 更新时间 | 更新内容 | 更新人 |
|------|---------|---------|--------|
| v1.0.0 | 2025-12-15 | 初始版本，基于Spring Boot 3.5.8 + Vue 3.4技术栈制定 | 架构团队 |

---

## ✅ 检查清单

### API设计检查

- [ ] URL设计符合RESTful规范
- [ ] HTTP方法使用正确
- [ ] 请求响应格式统一
- [ ] 错误处理完善
- [ ] 版本控制策略清晰
- [ ] 安全机制完善

### 文档检查

- [ ] Swagger注解完整
- [ ] API文档生成正常
- [ ] 类型定义准确
- [ ] 示例代码正确

### 前端集成检查

- [ ] 接口调用封装完善
- [ ] 错误处理统一
- [ ] 数据加密解密正确
- [ ] TypeScript类型安全

---

**📞 维护责任人**: IOE-DREAM架构团队
**🔄 更新频率**: 季度更新或重大变更时更新
**📧 反馈渠道**: 架构团队邮箱或项目Issue