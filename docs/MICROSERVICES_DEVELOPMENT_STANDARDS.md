# IOE-DREAM 微服务开发规范手册

> **版本**: v1.0.0
> **发布日期**: 2025-11-30
> **适用范围**: IOE-DREAM智能管理系统所有微服务开发
> **目标**: 确保企业级高质量交付，实现标准化、规范化、工程化开发

---

## 📖 目录

1. [概述](#概述)
2. [开发环境规范](#开发环境规范)
3. [代码规范](#代码规范)
4. [架构设计规范](#架构设计规范)
5. [API设计规范](#api设计规范)
6. [数据规范](#数据规范)
7. [安全规范](#安全规范)
8. [测试规范](#测试规范)
9. [部署规范](#部署规范)
10. [监控与运维规范](#监控与运维规范)

---

## 🎯 概述

### 1.1 规范目标
- **统一标准**: 确保所有微服务遵循统一的技术标准和开发规范
- **质量保证**: 建立完整的质量保证体系，确保代码质量和系统稳定性
- **可维护性**: 提高代码可读性和可维护性，降低维护成本
- **团队协作**: 规范团队开发流程，提高协作效率

### 1.2 技术栈标准
```yaml
核心技术栈:
  - Java: 17
  - Spring Boot: 3.5.7
  - Spring Cloud: 2023.0.3
  - Spring Cloud Alibaba: 2022.0.0.0

基础设施:
  - 注册中心: Nacos
  - 配置中心: Nacos Config
  - 服务网关: Spring Cloud Gateway
  - 熔断器: Sentinel
  - 负载均衡: Ribbon

数据库:
  - 数据库: MySQL 8.0.33
  - 连接池: Druid 1.2.21
  - ORM: MyBatis Plus 3.5.7
  - 缓存: Redis

工具库:
  - JSON: FastJSON 2.0.57
  - 工具类: Hutool 5.8.39
  - 文档处理: POI 5.4.1
  - 接口文档: Knife4j 4.6.0
  - 认证授权: Sa-Token 1.44.0
```

---

## 🔧 开发环境规范

### 2.1 IDE配置
- **推荐IDE**: IntelliJ IDEA 2023.2+
- **编码格式**: UTF-8
- **缩进**: 4个空格，禁止使用Tab
- **行宽限制**: 120字符
- **文件换行**: LF (Unix风格)

### 2.2 Maven配置
```xml
<!-- 所有微服务必须继承的父POM -->
<parent>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>smart-admin-microservices</artifactId>
    <version>1.0.0</version>
</parent>
```

### 2.3 环境变量
```properties
# 必须配置的环境变量
JAVA_VERSION=17
SPRING_PROFILES_ACTIVE=dev
LOGGING_LEVEL_ROOT=INFO
```

---

## 📝 代码规范

### 3.1 命名规范

#### 3.1.1 包命名
```
net.lab1024.sa.{服务名}.{层级}
```
**示例**:
- `net.lab1024.sa.user.controller`
- `net.lab1024.sa.user.service.impl`
- `net.lab1024.sa.user.domain.entity`

#### 3.1.2 类命名
```java
// 实体类: {业务名}Entity
public class UserEntity {}

// 数据传输对象: {业务名}DTO
public class UserCreateDTO {}

// 视图对象: {业务名}VO
public class UserDetailVO {}

// 服务接口: {业务名}Service
public interface UserService {}

// 服务实现: {业务名}ServiceImpl
public class UserServiceImpl {}

// 控制器: {业务名}Controller
public class UserController {}

// 工具类: {业务名}Util / {功能}Utils
public class DateUtil {}
```

#### 3.1.3 方法命名
```java
// 查询方法: get/query/find/list
public User getUserById(Long id);
public List<User> queryUsers(UserQuery query);
public List<User> findActiveUsers();
public List<User> listAllUsers();

// 创建方法: create/add/save
public User createUser(UserCreateDTO dto);
public void addUser(UserEntity user);
public User saveUser(UserEntity user);

// 更新方法: update/modify
public User updateUser(Long id, UserUpdateDTO dto);
public void modifyUserStatus(Long id, Integer status);

// 删除方法: delete/remove
public void deleteUser(Long id);
public void removeUser(Long id);

// 业务方法: 使用动词+名词形式
public boolean validateUserLogin(String username, String password);
public void sendWelcomeEmail(UserEntity user);
```

#### 3.1.4 常量命名
```java
// 全大写，下划线分隔
public static final String DEFAULT_CHARSET = "UTF-8";
public static final int MAX_RETRY_COUNT = 3;
public static final long DEFAULT_TIMEOUT = 5000L;
```

### 3.2 注释规范

#### 3.2.1 类注释
```java
/**
 * 用户管理服务
 *
 * <p>
 * 提供用户相关的业务功能：
 * - 用户注册、登录、注销
 * - 用户信息查询、更新
 * - 用户权限管理
 * - 用户状态管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-30
 */
@Service
public class UserServiceImpl implements UserService {
    // ...
}
```

#### 3.2.2 方法注释
```java
/**
 * 根据用户ID查询用户信息
 *
 * @param userId 用户ID，不能为空
 * @return 用户详细信息，如果用户不存在返回null
 * @throws IllegalArgumentException 当userId为空时抛出
 * @see UserVO 用户信息视图对象
 */
public UserVO getUserById(Long userId) {
    // ...
}
```

#### 3.2.3 复杂逻辑注释
```java
// 检查用户权限：超级管理员拥有所有权限，普通用户需要验证具体权限
if (user.getRole() == UserRole.SUPER_ADMIN) {
    return true; // 超级管理员直接通过
}

// 验证用户的具体权限，需要同时满足以下条件：
// 1. 用户状态为激活
// 2. 权限未被禁用
// 3. 权限在有效期内
return user.isActive()
    && !permission.isDisabled()
    && permission.isValid();
```

### 3.3 代码结构规范

#### 3.3.1 类成员顺序
```java
public class UserService {
    // 1. 静态常量
    private static final String LOG_PREFIX = "[UserService]";

    // 2. 静态变量
    private static Map<String, Object> cache = new ConcurrentHashMap<>();

    // 3. 实例变量（按访问级别排序：private -> protected -> public）
    @Autowired
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${user.max.retry.count}")
    private Integer maxRetryCount;

    // 4. 构造方法
    public UserService() {
        // ...
    }

    // 5. 静态方法
    public static String generateUserToken() {
        // ...
    }

    // 6. 实例方法（按访问级别排序：private -> protected -> public）
    private void validateUserDTO(UserCreateDTO dto) {
        // ...
    }

    public User createUser(UserCreateDTO dto) {
        // ...
    }

    // 7. getter/setter方法

    // 8. 重写方法（toString, equals, hashCode等）
}
```

#### 3.3.2 异常处理规范
```java
// 使用自定义业务异常
@Service
public class UserServiceImpl implements UserService {

    public User createUser(UserCreateDTO dto) {
        try {
            validateUserDTO(dto);
            checkUserExists(dto.getUsername());

            UserEntity user = buildUserEntity(dto);
            userMapper.insert(user);

            return convertToVO(user);
        } catch (ValidationException e) {
            log.warn("用户创建失败，参数验证不通过: {}", e.getMessage());
            throw e; // 重新抛出业务异常
        } catch (DuplicateKeyException e) {
            log.error("用户创建失败，用户名已存在: {}", dto.getUsername());
            throw new BusinessErrorException(ErrorCode.USER_ALREADY_EXISTS, "用户名已存在");
        } catch (Exception e) {
            log.error("用户创建失败，系统异常: {}", dto.getUsername(), e);
            throw new SystemErrorException("用户创建失败，请稍后重试");
        }
    }
}
```

---

## 🏗️ 架构设计规范

### 4.1 微服务拆分原则

#### 4.1.1 服务拆分原则
1. **单一职责原则**: 每个微服务只负责一个业务领域
2. **领域驱动设计**: 按业务领域边界拆分服务
3. **高内聚低耦合**: 服务内部高内聚，服务之间低耦合
4. **数据独立**: 每个服务拥有独立的数据库
5. **故障隔离**: 服务之间故障不相互影响

#### 4.1.2 服务分层规范
```
┌─────────────────────────────┐
│        用户接口层 (API)       │  ← Controller, REST API
├─────────────────────────────┤
│        应用服务层 (Service)   │  ← 业务逻辑编排，事务控制
├─────────────────────────────┤
│        领域服务层 (Manager)   │  ← 核心业务逻辑，跨实体操作
├─────────────────────────────┤
│        数据访问层 (DAO)       │  ← 数据库操作，Mapper接口
└─────────────────────────────┘
```

### 4.2 依赖注入规范

#### 4.2.1 注入方式优先级
1. **构造器注入** (推荐) - 确保依赖不可变
2. **@Resource注解** - 按名称注入
3. **@Autowired注解** - 按类型注入

```java
// ✅ 推荐：构造器注入
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserServiceImpl(UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }
}

// ✅ 可接受：@Resource注入
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
}

// ❌ 避免：@Autowired字段注入
@Service
public class UserServiceImpl implements UserService {
    @Autowired  // 避免使用
    private UserMapper userMapper;
}
```

### 4.3 配置管理规范

#### 4.3.1 配置文件结构
```yaml
# application.yml
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api

spring:
  application:
    name: ${SERVICE_NAME:user-service}
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:public}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
      config:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:public}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yaml
```

#### 4.3.2 配置属性类
```java
@ConfigurationProperties(prefix = "ioedream.user")
@Component
@Data
public class UserProperties {

    /**
     * 用户默认头像URL
     */
    private String defaultAvatar = "https://example.com/default-avatar.png";

    /**
     * 密码加密配置
     */
    private Password password = new Password();

    /**
     * 缓存配置
     */
    private Cache cache = new Cache();

    @Data
    public static class Password {
        private String algorithm = "BCrypt";
        private int strength = 10;
    }

    @Data
    public static class Cache {
        private long expireTime = 3600L;
        private String keyPrefix = "user:";
    }
}
```

---

## 🔌 API设计规范

### 5.1 RESTful API规范

#### 5.1.1 URL设计规范
```
# 基础URL格式
https://{domain}/api/{version}/{资源名}

# 示例
GET    /api/v1/users          # 获取用户列表
GET    /api/v1/users/{id}     # 获取单个用户
POST   /api/v1/users          # 创建用户
PUT    /api/v1/users/{id}     # 更新用户
DELETE /api/v1/users/{id}     # 删除用户

# 嵌套资源
GET    /api/v1/users/{id}/orders     # 获取用户的订单列表
POST   /api/v1/users/{id}/orders     # 为用户创建订单
```

#### 5.1.2 HTTP状态码规范
```java
// 成功响应
ResponseEntity.ok(data);                    // 200 OK
ResponseEntity.status(HttpStatus.CREATED).body(data);  // 201 Created

// 客户端错误
ResponseEntity.badRequest().body(error);      // 400 Bad Request
ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);  // 401 Unauthorized
ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);    // 403 Forbidden
ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);    // 404 Not Found
ResponseEntity.status(HttpStatus.CONFLICT).body(error);    // 409 Conflict

// 服务端错误
ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);  // 500 Internal Server Error
```

#### 5.1.3 统一响应格式
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 响应码
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
     * 时间戳
     */
    private Long timestamp;

    /**
     * 请求ID
     */
    private String requestId;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
```

### 5.2 接口文档规范

#### 5.2.1 Swagger注解
```java
@RestController
@RequestMapping("/api/v1/users")
@Api(tags = "用户管理接口")
@Slf4j
public class UserController {

    @ApiOperation(value = "创建用户", notes = "根据用户创建DTO创建新用户")
    @ApiResponses({
        @ApiResponse(code = 201, message = "创建成功", response = UserVO.class),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 409, message = "用户已存在")
    })
    @PostMapping
    public ApiResponse<UserVO> createUser(
            @ApiParam(value = "用户创建DTO", required = true)
            @Valid @RequestBody UserCreateDTO dto) {

        UserVO user = userService.createUser(dto);
        return ApiResponse.success(user);
    }

    @ApiOperation(value = "根据ID获取用户", notes = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUserById(
            @ApiParam(value = "用户ID", required = true, example = "123")
            @PathVariable Long id) {

        UserVO user = userService.getUserById(id);
        return ApiResponse.success(user);
    }
}
```

#### 5.2.2 参数验证
```java
@Data
@ApiModel(description = "用户创建DTO")
public class UserCreateDTO {

    @ApiModelProperty(value = "用户名", required = true, example = "张三")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    private String username;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_!@#$%^&*]{6,20}$", message = "密码只能包含字母、数字和特殊字符")
    private String password;

    @ApiModelProperty(value = "邮箱", required = true, example = "zhangsan@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @ApiModelProperty(value = "手机号", required = true, example = "13812345678")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
```

---

## 🗄️ 数据规范

### 6.1 数据库设计规范

#### 6.1.1 表命名规范
```sql
-- 表名：业务模块_具体功能，使用下划线分隔
user_info           -- 用户基础信息表
user_role           -- 用户角色关联表
order_detail        -- 订单详情表
product_category    -- 产品分类表
```

#### 6.1.2 字段命名规范
```sql
-- 字段名：使用下划线分隔，见名知意
id                  -- 主键ID
user_id            -- 用户ID（外键）
user_name          -- 用户名
create_time        -- 创建时间
update_time        -- 更新时间
deleted_flag       -- 删除标志（0：未删除，1：已删除）
```

#### 6.1.3 审计字段规范
```sql
-- 每个业务表都应该包含的审计字段
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
create_user_id BIGINT COMMENT '创建人ID',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_user_id BIGINT COMMENT '更新人ID',
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志（0：未删除，1：已删除）',
version INT DEFAULT 1 COMMENT '版本号（用于乐观锁）'
```

### 6.2 实体类设计规范

#### 6.2.1 基础实体类
```java
@Data
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志（0：未删除，1：已删除）
     */
    @TableLogic
    private Integer deletedFlag;

    /**
     * 版本号（用于乐观锁）
     */
    @Version
    private Integer version;
}
```

#### 6.2.2 业务实体类
```java
@Data
@TableName("user_info")
@ApiModel(description = "用户实体")
public class UserEntity extends BaseEntity {

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    @TableField("user_name")
    private String username;

    /**
     * 密码（加密存储）
     */
    @ApiModelProperty(value = "密码")
    @TableField("password")
    private String password;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    @TableField("phone")
    private String phone;

    /**
     * 用户状态（0：禁用，1：启用）
     */
    @ApiModelProperty(value = "用户状态")
    @TableField("status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @ApiModelProperty(value = "最后登录时间")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
}
```

### 6.3 数据访问层规范

#### 6.3.1 Mapper接口
```java
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    UserEntity selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    UserEntity selectByEmail(@Param("email") String email);

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param status 状态
     * @return 更新行数
     */
    int updateStatusByIds(@Param("userIds") List<Long> userIds, @Param("status") Integer status);

    /**
     * 统计用户数量（按状态分组）
     *
     * @return 用户统计结果
     */
    List<UserStatisticsVO> countUserByStatus();
}
```

#### 6.3.2 SQL映射文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="net.lab1024.sa.user.mapper.UserMapper">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="net.lab1024.sa.user.domain.entity.UserEntity">
        <id column="id" property="id"/>
        <result column="user_name" property="username"/>
        <result column="password" property="password"/>
        <result column="email" property="email"/>
        <result column="phone" property="phone"/>
        <result column="status" property="status"/>
        <result column="last_login_time" property="lastLoginTime"/>
        <result column="create_user_id" property="createUserId"/>
        <result column="create_time" property="createTime"/>
        <result column="update_user_id" property="updateUserId"/>
        <result column="update_time" property="updateTime"/>
        <result column="deleted_flag" property="deletedFlag"/>
        <result column="version" property="version"/>
    </resultMap>

    <!-- 根据用户名查询用户 -->
    <select id="selectByUsername" resultMap="BaseResultMap">
        SELECT *
        FROM user_info
        WHERE user_name = #{username}
          AND deleted_flag = 0
    </select>

    <!-- 批量更新用户状态 -->
    <update id="updateStatusByIds">
        UPDATE user_info
        SET status = #{status},
            update_time = NOW()
        WHERE id IN
        <foreach collection="userIds" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
          AND deleted_flag = 0
    </update>

    <!-- 统计用户数量（按状态分组） -->
    <select id="countUserByStatus" resultType="net.lab1024.sa.user.domain.vo.UserStatisticsVO">
        SELECT
            status,
            COUNT(*) as count
        FROM user_info
        WHERE deleted_flag = 0
        GROUP BY status
        ORDER BY status
    </select>

</mapper>
```

---

## 🛡️ 安全规范

### 7.1 认证授权规范

#### 7.1.1 Sa-Token配置
```java
@Configuration
@EnableConfigurationProperties({SaTokenProperties.class})
public class SaTokenConfig {

    /**
     * Sa-Token配置
     */
    @Bean
    public SaTokenConfig saTokenConfig() {
        SaTokenConfig config = new SaTokenConfig();

        // Token有效期（单位：秒）
        config.setTokenTimeout(24 * 60 * 60); // 24小时

        // Token最低活跃频率（单位：秒），如果token在此时间内没有访问过，就会被冻结
        config.setActiveTimeout(-1); // 永不冻结

        // 是否允许同一账号同时登录
        config.setIsConcurrent(false);

        // 在多人登录同一账号时，是否共用一个token
        config.setIsShare(false);

        // token风格
        config.setTokenStyle("uuid");

        // 是否输出操作日志
        config.setIsLog(false);

        return config;
    }

    /**
     * 权限认证拦截器
     */
    @Bean
    public SaInterceptor saInterceptor() {
        return new SaInterceptor(handle -> {
            // 指定需要拦截的路径
            SaRouter.match("/**")
                    .notMatch("/api/v1/auth/login", "/api/v1/auth/register", "/doc.html", "/webjars/**", "/swagger-resources/**")
                    .check(r -> StpUtil.checkLogin());

            // 角色权限检查
            SaRouter.match("/api/v1/admin/**")
                    .check(r -> StpUtil.checkRole("admin"));

            // 权限码检查
            SaRouter.match("/api/v1/users/**")
                    .check(r -> StpUtil.checkPermission("user:manage"));
        });
    }
}
```

#### 7.1.2 权限注解使用
```java
@RestController
@RequestMapping("/api/v1/users")
@Api(tags = "用户管理接口")
@Slf4j
public class UserController {

    @ApiOperation(value = "获取用户列表")
    @SaCheckPermission("user:list")
    @GetMapping
    public ApiResponse<PageResult<UserVO>> getUserList(UserQuery query) {
        // ...
    }

    @ApiOperation(value = "创建用户")
    @SaCheckPermission("user:create")
    @PostMapping
    public ApiResponse<UserVO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        // ...
    }

    @ApiOperation(value = "删除用户")
    @SaCheckPermission("user:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // ...
    }

    @ApiOperation(value = "获取管理员信息")
    @SaCheckRole("admin")
    @GetMapping("/admin/info")
    public ApiResponse<AdminVO> getAdminInfo() {
        // ...
    }
}
```

### 7.2 数据安全规范

#### 7.2.1 密码加密
```java
@Component
public class PasswordEncoder {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

#### 7.2.2 敏感数据脱敏
```java
@Data
public class UserVO {

    private Long id;
    private String username;

    /**
     * 脱敏邮箱
     */
    public String getEmail() {
        return email != null ? email.replaceAll("(\\w{2})\\w+(@\\w+)", "$1***$2") : null;
    }

    private String email;

    /**
     * 脱敏手机号
     */
    public String getPhone() {
        return phone != null ? phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") : null;
    }

    private String phone;
}
```

---

## 🧪 测试规范

### 8.1 单元测试规范

#### 8.1.1 测试类结构
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserCreateDTO userCreateDTO;

    @BeforeEach
    void setUp() {
        userCreateDTO = UserCreateDTO.builder()
                .username("testuser")
                .password("123456")
                .email("test@example.com")
                .phone("13812345678")
                .build();
    }

    @Test
    @DisplayName("创建用户 - 成功")
    void testCreateUser_Success() {
        // Given
        UserEntity savedUser = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phone("13812345678")
                .status(1)
                .build();

        when(userMapper.selectByUsername("testuser")).thenReturn(null);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(userMapper.insert(any(UserEntity.class))).thenReturn(1);

        // When
        UserVO result = userService.createUser(userCreateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userMapper).selectByUsername("testuser");
        verify(passwordEncoder).encode("123456");
        verify(userMapper).insert(any(UserEntity.class));
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void testCreateUser_UsernameAlreadyExists() {
        // Given
        UserEntity existingUser = UserEntity.builder()
                .username("testuser")
                .build();

        when(userMapper.selectByUsername("testuser")).thenReturn(existingUser);

        // When & Then
        BusinessErrorException exception = assertThrows(BusinessErrorException.class,
                () -> userService.createUser(userCreateDTO));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_ALREADY_EXISTS);
        assertThat(exception.getMessage()).isEqualTo("用户名已存在");
    }
}
```

#### 8.1.2 集成测试
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("创建用户API测试")
    void testCreateUserAPI() {
        // Given
        UserCreateDTO dto = UserCreateDTO.builder()
                .username("integrationuser")
                .password("123456")
                .email("integration@example.com")
                .phone("13812345678")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserCreateDTO> entity = new HttpEntity<>(dto, headers);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/v1/users", entity, ApiResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCode()).isEqualTo(200);

        // 验证用户是否真的创建成功
        UserVO createdUser = userService.getUserByUsername("integrationuser");
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo("integrationuser");
    }
}
```

### 8.2 性能测试规范

#### 8.2.1 JMH基准测试
```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class UserServiceBenchmark {

    private UserService userService;
    private UserCreateDTO userCreateDTO;

    @Setup
    public void setup() {
        userService = new UserServiceImpl();
        userCreateDTO = UserCreateDTO.builder()
                .username("benchmarkuser")
                .password("123456")
                .email("benchmark@example.com")
                .phone("13812345678")
                .build();
    }

    @Benchmark
    public void benchmarkCreateUser() {
        userService.createUser(userCreateDTO);
    }

    @Benchmark
    public void benchmarkGetUserById() {
        userService.getUserById(1L);
    }
}
```

---

## 📦 部署规范

### 9.1 Docker化规范

#### 9.1.1 Dockerfile
```dockerfile
# 使用官方OpenJDK 17镜像
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 复制应用jar包
COPY target/ioedream-user-service-1.0.0.jar app.jar

# 创建非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# 暴露端口
EXPOSE 8081

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### 9.1.2 Docker Compose
```yaml
version: '3.8'

services:
  # 用户服务
  user-service:
    build: ./ioedream-user-service
    container_name: ioedream-user-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - DB_HOST=mysql
      - DB_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - mysql
      - redis
      - nacos
    networks:
      - ioe-dream-network
    restart: unless-stopped

  # Nacos注册中心
  nacos:
    image: nacos/nacos-server:v2.3.0
    container_name: ioedream-nacos
    ports:
      - "8848:8848"
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=nacos123
    depends_on:
      - mysql
    networks:
      - ioe-dream-network
    restart: unless-stopped

  # MySQL数据库
  mysql:
    image: mysql:8.0.33
    container_name: ioedream-mysql
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=mysql123
      - MYSQL_DATABASE=ioedream
    volumes:
      - mysql-data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - ioe-dream-network
    restart: unless-stopped

  # Redis缓存
  redis:
    image: redis:7.2.3-alpine
    container_name: ioedream-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis-data:/data
    networks:
      - ioe-dream-network
    restart: unless-stopped

volumes:
  mysql-data:
  redis-data:

networks:
  ioe-dream-network:
    driver: bridge
```

### 9.2 Kubernetes部署规范

#### 9.2.1 部署配置
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: ioe-dream
  labels:
    app: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: ioedream/user-service:1.0.0
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: NACOS_SERVER_ADDR
          value: "nacos:8848"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: host
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: ioe-dream
spec:
  selector:
    app: user-service
  ports:
  - protocol: TCP
    port: 8081
    targetPort: 8081
  type: ClusterIP

---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: user-service-ingress
  namespace: ioe-dream
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - api.ioedream.com
    secretName: ioe-dream-tls
  rules:
  - host: api.ioedream.com
    http:
      paths:
      - path: /api/v1/users
        pathType: Prefix
        backend:
          service:
            name: user-service
            port:
              number: 8081
```

---

## 📊 监控与运维规范

### 10.1 健康检查规范

#### 10.1.1 健康检查配置
```java
@Component
public class UserHealthIndicator implements HealthIndicator {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            // 检查数据库连接
            long userCount = userService.count();
            log.info("用户服务健康检查 - 数据库连接正常，用户数量: {}", userCount);

            // 检查Redis连接
            redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(10));
            log.info("用户服务健康检查 - Redis连接正常");

            // 检查核心功能
            userService.checkCoreFunctions();
            log.info("用户服务健康检查 - 核心功能正常");

            return Health.up()
                    .withDetail("userCount", userCount)
                    .withDetail("cacheStatus", "available")
                    .withDetail("coreFunctions", "normal")
                    .build();

        } catch (Exception e) {
            log.error("用户服务健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

#### 10.1.2 指标监控
```java
@Component
public class UserMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter userCreateCounter;
    private final Timer userQueryTimer;
    private final Gauge userTotalGauge;

    public UserMetrics(MeterRegistry meterRegistry, UserService userService) {
        this.meterRegistry = meterRegistry;

        // 创建计数器
        this.userCreateCounter = Counter.builder("user.create.count")
                .description("用户创建总数")
                .register(meterRegistry);

        // 创建计时器
        this.userQueryTimer = Timer.builder("user.query.duration")
                .description("用户查询耗时")
                .register(meterRegistry);

        // 创建仪表盘
        this.userTotalGauge = Gauge.builder("user.total.count")
                .description("用户总数")
                .register(meterRegistry, userService, UserService::count);
    }

    public void incrementUserCreate() {
        userCreateCounter.increment();
    }

    public Timer.Sample startUserQuery() {
        return Timer.start(meterRegistry);
    }

    public void recordUserQuery(Timer.Sample sample) {
        sample.stop(userQueryTimer);
    }
}
```

### 10.2 日志规范

#### 10.2.1 日志配置
```yaml
# logback-spring.xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="!prod">
        <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/ioedream-user-service.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/ioedream-user-service.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{36}] - %msg%n</pattern>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

#### 10.2.2 日志使用规范
```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public User createUser(UserCreateDTO dto) {
        log.info("[UserService.createUser] 开始创建用户，用户名: {}", dto.getUsername());

        try {
            // 参数验证
            validateUserDTO(dto);
            log.debug("[UserService.createUser] 参数验证通过");

            // 检查用户是否存在
            UserEntity existingUser = userMapper.selectByUsername(dto.getUsername());
            if (existingUser != null) {
                log.warn("[UserService.createUser] 用户名已存在: {}", dto.getUsername());
                throw new BusinessErrorException(ErrorCode.USER_ALREADY_EXISTS, "用户名已存在");
            }
            log.debug("[UserService.createUser] 用户名唯一性检查通过");

            // 创建用户
            UserEntity user = buildUserEntity(dto);
            int result = userMapper.insert(user);

            if (result > 0) {
                log.info("[UserService.createUser] 用户创建成功，用户ID: {}, 用户名: {}",
                        user.getId(), user.getUsername());
                // 记录指标
                userMetrics.incrementUserCreate();
                return convertToVO(user);
            } else {
                log.error("[UserService.createUser] 用户创建失败，数据库插入返回0");
                throw new SystemErrorException("用户创建失败，请稍后重试");
            }

        } catch (BusinessErrorException e) {
            log.warn("[UserService.createUser] 用户创建失败，业务异常: {}, 用户名: {}",
                    e.getMessage(), dto.getUsername());
            throw e;
        } catch (Exception e) {
            log.error("[UserService.createUser] 用户创建失败，系统异常，用户名: {}",
                    dto.getUsername(), e);
            throw new SystemErrorException("用户创建失败，请稍后重试");
        }
    }
}
```

---

## 📋 附录

### A. 常用工具类

#### A.1 响应结果构建器
```java
public class ResponseBuilder {

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static ApiResponse<Void> success(String message) {
        return success(null, message);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return ApiResponse.error(errorCode.getCode(), message);
    }
}
```

#### A.2 分页结果包装器
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    public static <T> PageResult<T> of(IPage<T> page) {
        return PageResult.<T>builder()
                .records(page.getRecords())
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }
}
```

### B. 错误码定义

```java
public enum ErrorCode {

    // 通用错误码 (1000-1999)
    SUCCESS(200, "成功"),
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    UNAUTHORIZED(1002, "未授权"),
    FORBIDDEN(1003, "禁止访问"),
    NOT_FOUND(1004, "资源不存在"),

    // 用户相关错误码 (2000-2999)
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_ALREADY_EXISTS(2002, "用户已存在"),
    USER_DISABLED(2003, "用户已禁用"),
    PASSWORD_ERROR(2004, "密码错误"),

    // 业务相关错误码 (3000-3999)
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    INSUFFICIENT_BALANCE(3003, "余额不足");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
```

---

> **文档维护**: 本规范文档由IOE-DREAM技术团队维护，如有疑问或建议请联系技术负责人。
> **更新频率**: 每季度评审一次，根据实际使用情况进行调整和完善。
> **生效日期**: 2025年11月30日

---

*🎯 遵循本规范，确保IOE-DREAM微服务项目的高质量交付！*