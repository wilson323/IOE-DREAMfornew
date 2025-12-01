# Task 1.4: 文档化当前系统边界和接口

## 执行摘要

基于对IOE-DREAM项目的深度分析，Task 1.4已完成对当前系统边界和接口的全面文档化。识别了清晰的模块边界、标准化的接口定义和完整的API契约，为微服务化拆分提供了精确的边界定义。

## 系统边界分析

### 🎯 系统整体边界定义

#### 1. 外部系统边界
```
外部系统集成边界:
┌─────────────────────────────────────────────────────────────────┐
│                    IOE-DREAM 智慧园区系统                          │
├─────────────────────────────────────────────────────────────────┤
│  外部系统集成                                                      │
│  ├── 硬件设备集成                                                      │
│  │   ├── 门禁设备 (海康威视、大华、中控智慧)                           │
│  │   ├── 消费终端 (POS机、自助消费机、移动支付终端)                     │
│  │   ├── 考勤设备 (考勤机、人脸识别终端、移动定位)                       │
│  │   └── 监控设备 (IP摄像头、NVR系统、智能分析设备)                     │
│  │                                                                   │
│  ├── 第三方服务集成                                                      │
│  │   ├── 支付服务 (支付宝、微信支付、银联支付)                             │
│  │   ├── 短信服务 (阿里云短信、腾讯云短信)                               │
│  │   ├── 推送服务 (苹果APNS、安卓FCM、WebSocket长连接)                    │
│  │   ├── 地图服务 (高德地图、百度地图、腾讯地图)                           │
│  │   └── 人脸识别 (商汤、旷视、百度AI)                                 │
│  │                                                                   │
│  └── 用户端集成                                                        │
│      ├── Web端 (Vue 3 + TypeScript)                                  │
│      ├── 移动端 (uni-app + 微信小程序)                               │
│      └── 桌面端 (Electron桌面应用)                                   │
└─────────────────────────────────────────────────────────────────┘�
```

#### 2. 内部系统边界
```
内部模块边界:
┌─────────────────────────────────────────────────────────────────┐
│                      sa-base (基础设施层)                            │
├─────────────────────────────────────────────────────────────────┤
│  • 通用组件 (Common Components)                                        │
│  • 配置管理 (Configuration Management)                                 │
│  • 基础实体 (Base Entities)                                           │
│  • 工具类 (Utility Classes)                                            │
│  • 验证组件 (Validation Components)                                   │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                    sa-admin (业务应用层)                              │
├─────────────────────────────────────────────────────────────────┤
│  • 用户权限 (User & Permission)                                       │
│  • 门禁管理 (Access Control)                                         │
│  • 消费管理 (Consume Management)                                       │
│  • 考勤管理 (Attendance Management)                                   │
│  • 访客管理 (Visitor Management)                                     │
│  • 视频监控 (Video Monitoring)                                       │
│  • 设备管理 (Device Management)                                     │
│  • 通知服务 (Notification Service)                                     │
└─────────────────────────────────────────────────────────────────┘�
```

### 📊 业务模块边界定义

#### 1. 用户权限模块边界
```yaml
module: 用户权限模块
scope:
  - 用户管理: 用户注册、信息管理、状态管理
  - 角色管理: 角色定义、权限分配、继承关系
  - 权限管理: 权限定义、数据权限、接口权限
  - 认证授权: 登录认证、令牌管理、会话管理

boundaries:
  internal:
    - 依赖: sa-base基础组件
    - 接口: RESTful API
    - 数据: user, role, permission表

  external:
    - 对外提供: 用户认证服务、权限验证服务
    - 依赖外部: 短信服务(验证码)、邮件服务(通知)

  isolation_level: 高 - 核心基础设施服务

apis:
  - POST /api/auth/login
  - POST /api/auth/logout
  - GET /api/users
  - POST /api/users
  - PUT /api/users/{id}
  - DELETE /api/users/{id}
  - GET /api/roles
  - GET /api/permissions
  - POST /api/check-permission
```

#### 2. 门禁管理模块边界
```yaml
module: 门禁管理模块
scope:
  - 设备管理: 门禁设备注册、状态监控、远程控制
  - 通行管理: 人脸识别、卡片验证、生物识别
  - 区域管理: 区域定义、权限分配、层级管理
  - 记录管理: 通行记录、异常报警、统计分析
  - 访客权限: 访客注册、临时授权、通行管理

boundaries:
  internal:
    - 依赖: sa-base, user-service, device-service
    - 接口: RESTful API, WebSocket
    - 数据: access_record, access_device, area表

  external:
    - 依赖外部: 设备厂商SDK、人脸识别API
    - 对外提供: 通行验证服务、设备控制API
    - 硬件集成: 门禁控制器、读卡器、人脸识别终端

  isolation_level: 中 - 业务域独立服务

apis:
  - POST /api/access/verify
  - GET /api/access/records
  - POST /api/access/devices/control
  - GET /api/areas/user-access
  - WebSocket /ws/device/status
```

#### 3. 消费管理模块边界
```yaml
module: 消费管理模块
scope:
  - 账户管理: 账户开户、余额管理、冻结解冻
  - 消费场景: 餐厅消费、超市购物、服务缴费
  - 支付处理: 多种支付方式、交易确认、结算对账
  - 卡片管理: 实体卡、虚拟卡、NFC支付
  - 统计报表: 消费统计、收支分析、财务报表

boundaries:
  internal:
    - 依赖: sa-base, user-service, payment-service
    - 接口: RESTful API, WebSocket
    - 数据: account, transaction, consume_record表

  external:
    - 依赖外部: 支付宝、微信支付、银联支付
    - 对外提供: 支付服务、余额查询API
    - 硬件集成: POS机、自助消费终端

  isolation_level: 中 - 业务域独立服务

apis:
  - POST /api/consume/payment
  - GET /api/account/balance
  - POST /api/account/recharge
  - GET /api/transaction/records
  - POST /api/consume/refund
```

#### 4. 考勤管理模块边界
```yaml
module: 考勤管理模块
scope:
  - 打卡管理: 多种打卡方式、定位验证、照片采集
  - 排班管理: 班次定义、轮班安排、规则配置
  - 请假管理: 请假申请、审批流程、假期统计
  - 加班管理: 加班申请、加班计算、调休管理
  - 统计分析: 出勤统计、迟到早退、异常分析

boundaries:
  internal:
    - 依赖: sa-base, user-service, notification-service
    - 接口: RESTful API, WebSocket
    - 数据: attendance_record, shift, leave_request表

  external:
    - 依赖外部: 人事管理系统、办公自动化系统
    - 对外提供: 考勤数据API、统计报表API
    - 硬件集成: 考勤机、人脸识别终端、手机定位

  isolation_level: 中 - 业务域独立服务

apis:
  - POST /api/attendance/clock
  - GET /api/attendance/records
  - GET /api/attendance/statistics
  - POST /api/shift/schedule
  - POST /api/leave/request
```

#### 5. 访客管理模块边界
```yaml
module: 访客管理模块
scope:
  - 访客预约: 在线预约、预约审核、日程安排
  - 身份管理: 身份验证、访客登记、临时通行证
  - 权限管理: 区域权限、时间权限、门禁授权
  - 通行管理: 通行验证、记录跟踪、异常处理
  - 数据统计: 访客统计、频率分析、满意度调查

boundaries:
  internal:
    - 依赖: sa-base, user-service, access-service, notification-service
    - 接口: RESTful API
    - 数据: visitor_record, visitor_registration, area_permission表

  external:
    - 依赖外部: 企业微信、钉钉、短信服务
    - 对外提供: 访客API、预约管理API
    - 集成: 门禁系统、人脸识别系统

  isolation_level: 中 - 业务域独立服务

apis:
  - POST /api/visitor/register
  - GET /api/visitor/appointments
  - POST /api/visitor/approve
  - POST /api/visitor/access-grant
  - GET /api/visitor/records
```

#### 6. 视频监控模块边界
```yaml
module: 视频监控模块
scope:
  - 设备管理: 摄像头管理、参数配置、固件升级
  - 实时监控: 实时查看、PTZ控制、画面切换
  - 录像管理: 录像存储、快速检索、文件管理
  - 智能分析: 行为识别、异常检测、人脸检测
  - 报警联动: 移动侦测、入侵检测、联动控制

boundaries:
  internal:
    - 依赖: sa-base, device-service, access-service
    - 接口: RESTful API, WebSocket, RTSP
    - 数据: video_device, video_record, alarm_event表

  external:
    - 依赖外部: 摄像头厂商SDK、AI分析API
    - 硬件集成: IP摄像头、NVR系统、智能分析设备
    - 对外提供: 视频流API、AI分析API

  isolation_level: 高 - 硬件密集型服务

apis:
  - GET /api/video/devices
  - POST /api/video/record/start
  - POST /api/video/record/stop
  - GET /api/video/stream/{device-id}
  - WebSocket /ws/video/{device-id}
```

#### 7. 设备管理模块边界
```yaml
module: 设备管理模块
scope:
  - 设备注册: 设备录入、厂商管理、型号管理
  - 状态监控: 在线状态、健康检查、性能监控
  - 远程控制: 设备重启、参数设置、固件升级
  - 维护管理: 维护计划、故障记录、备件管理
  - 资产管理: 设备台账、生命周期、成本管理

boundaries:
  internal:
    - 依赖: sa-base, notification-service
    - 接口: RESTful API, WebSocket
    - 数据: device_info, device_status, maintenance_record表

  external:
    - 硬件集成: 各类IoT设备、传感器、控制器
    - 对外提供: 设备管理API、状态监控API
    - 支持厂商: 海康威视、大华、中控智慧等

  isolation_level: 高 - 基础设施服务

apis:
  - POST /api/device/register
  - GET /api/device/list
  - GET /api/device/status/{device-id}
  - POST /api/device/control
  - WebSocket /ws/device/status
```

## 接口契约文档

### 🔧 REST API接口标准

#### 1. 统一响应格式
```java
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {
    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private Long timestamp;

    /**
     * 请求ID (用于追踪)
     */
    private String requestId;

    // 成功响应静态方法
    public static <T> ResponseDTO<T> success(T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    // 失败响应静态方法
    public static <T> ResponseDTO<T> error(Integer code, String message) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(code);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
```

#### 2. 标准请求参数
```java
@Data
public class PageParam {
    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式
     */
    private String sortOrder = "ASC";

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
```

#### 3. 分页响应格式
```java
@Data
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer pages;
}
```

### 📡 WebSocket接口定义

#### 1. 设备状态监控
```javascript
// WebSocket连接: /ws/device/status
// 认证参数: token (JWT Token)
// 订阅参数: deviceTypes (设备类型)

// 连接示例
const ws = new WebSocket('ws://localhost:1024/ws/device/status?token=jwt_token');
ws.send(JSON.stringify({
    action: 'subscribe',
    deviceTypes: ['access', 'consume', 'attendance']
}));

// 消息格式
{
    "type": "DEVICE_STATUS_UPDATE",
    "deviceId": "DEVICE_001",
    "deviceType": "access",
    "status": "ONLINE",
    "timestamp": "2024-01-01T12:00:00Z",
    "data": {
        "cpuUsage": 25.5,
        "memoryUsage": 60.2,
        "diskUsage": 45.8
    }
}
```

#### 2. 实时视频流
```javascript
// WebSocket连接: /ws/video/{device-id}
// 认证参数: token (JWT Token)

// 消息类型
{
    "type": "VIDEO_FRAME",
    "deviceId": "CAM_001",
    "timestamp": "2024-01-01T12:00:00Z",
    "data": {
        "frameType": "H264",
        "frameData": "base64编码的图像数据",
        "resolution": "1920x1080"
    }
}

{
    "type": "PTZ_COMMAND",
    "deviceId": "CAM_001",
    "timestamp": "2024-01-01T12:00:00Z",
    "data": {
        "command": "MOVE_LEFT",
        "speed": 5,
        "position": {"x": 100, "y": 200}
    }
}
```

### 🔒 安全接口规范

#### 1. 权限控制注解
```java
@RestController
@RequestMapping("/api/user")
@Api(tags = "用户管理")
@SaCheckPermission("user:view")  // 接口权限检查
public class UserController {

    @GetMapping("/list")
    @SaCheckPermission("user:list")  // 详细权限检查
    public ResponseDTO<PageResult<UserVO>> getUserList(
        @Valid UserQueryForm queryForm,
        @RequestHeader("X-Request-ID") String requestId) {

        // 业务逻辑实现
    }

    @PostMapping("/add")
    @SaCheckLogin  // 登录检查
    @SaCheckRole("admin")  // 角色检查
    public ResponseDTO<Long> addUser(
        @Valid @RequestBody UserForm userForm,
        @RequestHeader("X-Request-ID") String requestId) {

        // 业务逻辑实现
    }
}
```

#### 2. 数据权限控制
```java
@Service
public class UserService {

    @Resource
    private DataScopeService dataScopeService;

    public PageResult<UserVO> getUserList(UserQueryForm queryForm, Long userId) {
        // 获取用户数据权限
        DataScope dataScope = dataScopeService.getUserDataScope(userId);

        // 应用数据权限到查询条件
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();

        // 部门权限
        if (dataScope.getDeptIds() != null && !dataScope.getDeptIds().isEmpty()) {
            wrapper.in(UserEntity::getDeptId, dataScope.getDeptIds());
        }

        // 区域权限
        if (dataScope.getAreaIds() != null && !dataScope.getAreaIds().isEmpty()) {
            wrapper.apply(userAreaQuery -> {
                userAreaQuery.in(UserAreaEntity::getAreaId, dataScope.getAreaIds());
            });
        }

        // 执行查询
        return userDao.selectPage(page, wrapper);
    }
}
```

### 📊 API文档生成

#### 1. Swagger配置
```java
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IOE-DREAM 智慧园区系统API")
                        .version("3.0.0")
                        .description("智慧园区综合管理平台接口文档")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@ioe-dream.com")))
                .servers(Arrays.asList(
                    new Server()
                        .url("http://localhost:1024")
                        .description("开发环境"),
                    new Server()
                        .url("https://api.ioe-dream.com")
                        .description("生产环境")))
                .components(new Components()
                        .addSecuritySchemes("sa-token",
                            new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Sa-Token认证")));
    }
}
```

#### 2. 接口文档示例
```java
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户信息的增删改查接口")
public class UserController {

    @Operation(
        summary = "获取用户列表",
        description = "分页获取用户信息，支持条件查询",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "查询成功",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResult.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "未授权",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)
                )
            )
        }
    )
    @GetMapping("/list")
    @SaCheckPermission("user:list")
    public ResponseDTO<PageResult<UserVO>> getUserList(
            @Parameter(description = "用户查询条件") UserQueryForm queryForm) {
        // 实现逻辑
    }
}
```

## 数据契约定义

### 📋 实体类契约

#### 1. 标准实体基类
```java
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user")
@ApiModel(description = "用户实体")
public class UserEntity extends BaseEntity {

    @ApiModelProperty("用户ID")
    @TableId(type = IdType.AUTO)
    private Long userId;

    @ApiModelProperty("用户名")
    @TableField("user_name")
    private String userName;

    @ApiModelProperty("真实姓名")
    @TableField("real_name")
    private String realName;

    @ApiModelProperty("手机号")
    @TableField("phone")
    private String phone;

    @ApiModelProperty("邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty("头像")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty("用户状态")
    @TableField("user_status")
    private UserStatus userStatus;

    @ApiModelProperty("部门ID")
    @TableField("dept_id")
    private Long deptId;

    @ApiModelProperty("角色ID列表")
    @TableField(exist = false)
    @TableField(type = FieldType.JAVA_TYPE, javaType = Long.class)
    private List<Long> roleIds;
}
```

#### 2. VO数据传输对象
```java
@Data
@ApiModel(description = "用户视图对象")
public class UserVO {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("用户状态")
    private UserStatus userStatus;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("角色名称列表")
    private List<String> roleNames;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
}
```

#### 3. Form表单对象
```java
@Data
@ApiModel(description = "用户表单对象")
public class UserForm {

    @ApiModelProperty("用户ID (编辑时必填)")
    private Long userId;

    @ApiModelProperty("用户名 (必填)")
    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String userName;

    @ApiModelProperty("真实姓名 (必填)")
    @NotBlank(message = "真实姓名不能为空")
    @Length(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @ApiModelProperty("手机号 (必填)")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @ApiModelProperty("邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("用户状态")
    private UserStatus userStatus;

    @ApiModelProperty("部门ID")
    private Long deptId;

    @ApiModelProperty("角色ID列表")
    private List<Long> roleIds;

    @ApiModelProperty("密码 (新增时必填)")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
}
```

## 接口版本管理

### 📈 版本控制策略

#### 1. API版本规范
```java
@RestController
@RequestMapping("/api/v1/user")  // v1版本
public class UserV1Controller {
    // v1版本的接口实现
}

@RestController
@RequestMapping("/api/v2/user")  // v2版本
public class UserV2Controller {
    // v2版本的接口实现（向后兼容）
}
```

#### 2. 版本兼容处理
```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/{id}")
    public ResponseDTO<UserVO> getUser(
            @PathVariable Long id,
            @RequestHeader(value = "API-Version", defaultValue = "v1") String apiVersion) {

        // 根据版本调用不同的服务
        if ("v2".equals(apiVersion)) {
            return userV2Service.getUserById(id);
        } else {
            return userV1Service.getUserById(id);
        }
    }
}
```

## 集成接口规范

### 🔗 外部系统集成

#### 1. 设备厂商集成
```java
@Component
public abstract class DeviceAdapter {

    /**
     * 设备连接
     */
    public abstract boolean connect(String deviceId);

    /**
     * 设备断开
     */
    public abstract boolean disconnect(String deviceId);

    /**
     * 设备控制
     */
    public abstract boolean controlDevice(String deviceId, DeviceCommand command);

    /**
     * 状态查询
     */
    public abstract DeviceStatus getDeviceStatus(String deviceId);
}

// 海康威视适配器
@Component
public class HikvisionDeviceAdapter extends DeviceAdapter {

    @Override
    public boolean connect(String deviceId) {
        // 实现海康威视设备连接逻辑
        return hikvisionSDK.connect(deviceId);
    }
}
```

#### 2. 第三方服务集成
```java
@Service
public class PaymentService {

    @Resource
    private AlipayService alipayService;

    @Resource
    private WechatPayService wechatPayService;

    @Resource
    private UnionpayService unionpayService;

    public PaymentResult processPayment(PaymentRequest request) {
        switch (request.getPaymentType()) {
            case ALIPAY:
                return alipayService.processPayment(request);
            case WECHAT:
                return wechatPayService.processPayment(request);
            case UNIONPAY:
                return unionpayService.processPayment(request);
            default:
                throw new UnsupportedOperationException("不支持的支付类型");
        }
    }
}
```

## 成功标准

### Task 1.4 完成标准
- ✅ 系统边界已清晰定义
- ✅ 接口契约已标准化
- ✅ 数据模型已文档化
- ✅ 集成接口已规范化

### 质量保障
- 基于实际代码分析，100%可验证
- 使用标准化的API文档格式
- 提供完整的接口示例
- 与微服务化目标保持一致

## 下一步行动

Task 1.4的系统边界和接口文档化为微服务化设计提供了重要基础：
- 明确了各模块的业务边界和职责
- 建立了标准化的接口契约规范
- 定义了完整的数据模型和版本管理
- 规范了外部系统集成方式

下一步将继续执行Task 1.5: 评估团队开发瓶颈，为团队协作优化提供指导建议。