# 公共服务专家技能
## Common Service Specialist

**🎯 技能定位**: IOE-DREAM智慧园区公共服务业务专家，精通用户认证、权限管理、组织架构、审计日志、通知服务等核心公共业务

**⚡ 技能等级**: ★★★★★ (顶级专家)
**🎯 适用场景**: 公共服务开发、身份认证集成、权限体系设计、组织管理、审计监控
**📊 技能覆盖**: 用户认证 | 权限管理 | 组织架构 | 审计日志 | 通知服务 | 任务调度 | 文件管理

---

## 📋 技能概述

### **核心专长**
- **身份认证体系**: Sa-Token、JWT、OAuth2、多因子认证
- **权限管理模型**: RBAC、ABAC、动态权限、数据权限
- **组织架构管理**: 部门树形结构、岗位管理、员工关系
- **审计日志系统**: 操作审计、数据审计、合规性监控
- **通知服务架构**: 多渠道通知、消息队列、实时推送
- **任务调度引擎**: 分布式调度、定时任务、工作流集成

### **解决能力**
- **公共业务开发**: 完整的公共服务业务实现和优化
- **认证授权设计**: 安全可靠的认证授权体系架构
- **组织架构建模**: 灵活的组织架构和企业建模
- **监控告警建设**: 全方位的系统监控和业务告警
- **性能优化**: 高并发公共服务的性能调优

---

## 🎯 业务场景覆盖

### 🔐 用户认证与授权
```java
// Sa-Token认证核心流程
@Service
@Transactional(rollbackFor = Exception.class)
public class AuthServiceImpl implements AuthService {

    @Resource
    private AuthManager authManager;

    @Resource
    private UserDao userDao;

    @Resource
    private PermissionDao permissionDao;

    @Override
    public LoginResultDTO login(LoginRequestDTO request) {
        // 1. 参数验证
        validateLoginRequest(request);

        // 2. 用户身份验证
        UserEntity user = authManager.authenticateUser(request.getUsername(), request.getPassword());

        // 3. 权限数据加载
        List<PermissionEntity> permissions = authManager.loadUserPermissions(user.getUserId());

        // 4. 生成Token
        String token = StpUtil.login(user.getUserId(), () -> {
            // 设置登录设备信息
            StpUtil.getTokenSession().set("deviceInfo", request.getDeviceInfo());
        });

        // 5. 记录登录日志
        authManager.recordLoginLog(user, request);

        return LoginResultDTO.builder()
            .token(token)
            .userInfo(convertToUserVO(user))
            .permissions(convertToPermissionVO(permissions))
            .build();
    }
}
```

### 🏢 组织架构管理
```java
// 组织架构树形结构处理
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationServiceImpl implements OrganizationService {

    @Resource
    private OrganizationManager organizationManager;

    @Resource
    private DepartmentDao departmentDao;

    @Resource
    private EmployeeDao employeeDao;

    @Override
    public DepartmentTreeVO getDepartmentTree(Long departmentId) {
        // 获取部门信息
        DepartmentEntity department = departmentDao.selectById(departmentId);
        if (department == null) {
            throw new BusinessException("DEPT_NOT_FOUND", "部门不存在");
        }

        // 构建树形结构
        return organizationManager.buildDepartmentTree(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDepartment(CreateDepartmentRequestDTO request) {
        // 1. 验证上级部门
        if (request.getParentId() != null) {
            DepartmentEntity parentDept = departmentDao.selectById(request.getParentId());
            if (parentDept == null) {
                throw new BusinessException("PARENT_DEPT_NOT_FOUND", "上级部门不存在");
            }
        }

        // 2. 验证部门编码唯一性
        if (departmentDao.existsByCode(request.getDeptCode())) {
            throw new BusinessException("DEPT_CODE_EXISTS", "部门编码已存在");
        }

        // 3. 创建部门
        DepartmentEntity department = convertToDepartmentEntity(request);
        department.setDeptPath(generateDeptPath(request.getParentId()));
        departmentDao.insert(department);

        // 4. 更新父部门的子部门数量
        if (request.getParentId() != null) {
            organizationManager.updateParentDeptChildrenCount(request.getParentId());
        }
    }
}
```

### 📊 审计日志监控
```java
// 操作审计记录
@Service
@Transactional(rollbackFor = Exception.class)
public class AuditServiceImpl implements AuditService {

    @Resource
    private AuditManager auditManager;

    @Resource
    private AuditLogDao auditLogDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @EventListener
    @Async
    public void handleUserOperationEvent(UserOperationEvent event) {
        // 异步记录审计日志
        AuditLogEntity auditLog = AuditLogEntity.builder()
            .userId(event.getUserId())
            .username(event.getUsername())
            .operation(event.getOperation())
            .resource(event.getResource())
            .method(event.getMethod())
            .params(event.getParams())
            .result(event.getResult())
            .ip(event.getClientIp())
            .userAgent(event.getUserAgent())
            .executeTime(LocalDateTime.now())
            .build();

        auditLogDao.insert(auditLog);

        // 异步处理审计分析
        auditManager.analyzeAuditLog(auditLog);
    }

    @Override
    public PageResult<AuditLogVO> queryAuditLogs(AuditLogQueryDTO query) {
        // 构建查询条件
        LambdaQueryWrapper<AuditLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(query.getUserId()), AuditLogEntity::getUserId, query.getUserId())
               .like(StringUtils.isNotBlank(query.getUsername()), AuditLogEntity::getUsername, query.getUsername())
               .eq(StringUtils.isNotBlank(query.getOperation()), AuditLogEntity::getOperation, query.getOperation())
               .between(query.getStartTime() != null && query.getEndTime() != null,
                       AuditLogEntity::getExecuteTime, query.getStartTime(), query.getEndTime())
               .orderByDesc(AuditLogEntity::getExecuteTime);

        // 分页查询
        Page<AuditLogEntity> page = auditLogDao.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return convertToPageResult(page);
    }
}
```

---

## 🏗️ 架构设计规范

### 四层架构实现

#### Controller层 - 接口控制层
```java
@RestController
@RequestMapping("/api/v1/common/auth")
@Tag(name = "认证管理")
@Validated
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseDTO<LoginResultDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResultDTO result = authService.login(request);
        return ResponseDTO.ok(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    @SaCheckLogin
    public ResponseDTO<Void> logout() {
        StpUtil.logout();
        return ResponseDTO.ok();
    }

    @GetMapping("/refresh")
    @Operation(summary = "刷新Token")
    @SaCheckLogin
    public ResponseDTO<String> refreshToken() {
        String newToken = StpUtil.getTokenSession().getTimeout() <= 3600 ? StpUtil.getTokenValue() : StpUtil.renewTimeout(2592000);
        return ResponseDTO.ok(newToken);
    }
}
```

#### Service层 - 核心业务层
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    @Resource
    private UserManager userManager;

    @Resource
    private DepartmentDao departmentDao;

    @Resource
    private RoleDao roleDao;

    @Override
    public UserDetailVO getUserDetail(Long userId) {
        // 业务规则验证
        validateUserAccess(userId);

        // 核心业务逻辑
        return userManager.buildUserDetail(userId);
    }

    private void validateUserAccess(Long userId) {
        UserEntity currentUser = userManager.getCurrentUser();
        if (!currentUser.getIsAdmin() && !currentUser.getUserId().equals(userId)) {
            throw new BusinessException("NO_PERMISSION", "无权限查看该用户信息");
        }
    }
}
```

#### Manager层 - 复杂流程管理层
```java
// ✅ 正确：Manager类为纯Java类，不使用Spring注解
public class UserManager {

    private final UserDao userDao;
    private final DepartmentDao departmentDao;
    private final RoleDao roleDao;
    private final GatewayServiceClient gatewayServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    // 构造函数注入依赖
    public UserManager(UserDao userDao, DepartmentDao departmentDao,
                      RoleDao roleDao, GatewayServiceClient gatewayServiceClient,
                      RedisTemplate<String, Object> redisTemplate) {
        this.userDao = userDao;
        this.departmentDao = departmentDao;
        this.roleDao = roleDao;
        this.gatewayServiceClient = gatewayServiceClient;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserDetailVO buildUserDetail(Long userId) {
        // 多级缓存查询
        UserEntity user = getUserWithCache(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 获取部门信息
        DepartmentEntity department = getDepartmentWithCache(user.getDepartmentId());

        // 获取角色信息
        List<RoleEntity> roles = getUserRolesWithCache(userId);

        // 获取权限信息
        List<PermissionEntity> permissions = getUserPermissionsWithCache(userId);

        return UserDetailVO.builder()
            .userInfo(convertToUserVO(user))
            .department(convertToDepartmentVO(department))
            .roles(convertToRoleVO(roles))
            .permissions(convertToPermissionVO(permissions))
            .build();
    }

    private UserEntity getUserWithCache(Long userId) {
        String cacheKey = "user:info:" + userId;

        // 尝试从Redis缓存获取
        UserEntity cachedUser = (UserEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        // 从数据库查询
        UserEntity user = userDao.selectById(userId);
        if (user != null) {
            // 缓存30分钟
            redisTemplate.opsForValue().set(cacheKey, user, Duration.ofMinutes(30));
        }

        return user;
    }

    private List<PermissionEntity> getUserPermissionsWithCache(Long userId) {
        String cacheKey = "user:permissions:" + userId;

        // 尝试从Redis缓存获取
        List<PermissionEntity> cachedPermissions = (List<PermissionEntity>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }

        // 通过网关调用其他服务获取权限
        ResponseDTO<List<PermissionEntity>> response = gatewayServiceClient.callAccessService(
            "/api/v1/access/user/" + userId + "/permissions",
            HttpMethod.GET,
            null,
            new TypeReference<ResponseDTO<List<PermissionEntity>>>() {}
        );

        List<PermissionEntity> permissions = response.getData();
        if (permissions != null) {
            // 缓存15分钟
            redisTemplate.opsForValue().set(cacheKey, permissions, Duration.ofMinutes(15));
        }

        return permissions != null ? permissions : Collections.emptyList();
    }
}
```

#### DAO层 - 数据访问层
```java
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    @Transactional(readOnly = true)
    UserEntity selectByUsername(@Param("username") String username);

    @Transactional(readOnly = true)
    boolean existsByUsername(@Param("username") String username);

    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Transactional(readOnly = true)
    List<UserEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    @Transactional(rollbackFor = Exception.class)
    int updateLastLoginInfo(@Param("userId") Long userId,
                           @Param("lastLoginTime") LocalDateTime lastLoginTime,
                           @Param("lastLoginIp") String lastLoginIp);

    @Select("SELECT * FROM t_common_user WHERE status = 1 AND deleted_flag = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<UserEntity> selectRecentUsers(@Param("limit") int limit);
}
```

---

## 📊 技能质量指标体系

### 核心质量指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **认证成功率** | ≥99.5% | 用户认证成功比例 | 认证成功率监控 |
| **权限验证准确率** | ≥99.9% | 权限判断准确性 | 权限验证测试 |
| **审计日志完整性** | 100% | 操作记录完整性 | 日志覆盖率分析 |
| **通知到达率** | ≥95% | 消息通知到达率 | 通知到达监控 |
| **响应时间** | ≤200ms | API接口响应时间 | 性能监控 |

### 版本管理
- **主版本**: v1.0.0 - 初始版本
- **文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
- **创建时间**: 2025-12-08
- **最后更新**: 2025-12-08
- **变更类型**: MAJOR - 新技能创建

---

## 🛠️ 开发规范和最佳实践

### 代码规范
```java
// ✅ 正确的依赖注入方式
@Service
public class SomeServiceImpl implements SomeService {
    @Resource
    private SomeManager someManager;  // 统一使用@Resource
}

// ❌ 错误的依赖注入方式
@Service
public class SomeServiceImpl implements SomeService {
    @Autowired  // 禁止使用@Autowired
    private SomeManager someManager;
}

// ✅ 正确的DAO接口定义
@Mapper
public interface SomeDao extends BaseMapper<SomeEntity> {
    // 使用@Mapper注解，继承BaseMapper
}

// ❌ 错误的DAO接口定义
@Repository  // 禁止使用@Repository
public interface SomeRepository extends JpaRepository<SomeEntity, Long> {
    // 禁止使用Repository和JPA
}
```

### 事务管理
```java
// ✅ 正确的事务注解使用
@Service
@Transactional(rollbackFor = Exception.class)  // Service层默认事务
public class SomeServiceImpl implements SomeService {

    @Transactional(rollbackFor = Exception.class)
    public void someWriteOperation() {
        // 写操作方法
    }

    @Transactional(readOnly = true)
    public void someReadOperation() {
        // 只读操作方法
    }
}

// ✅ DAO层事务注解
@Mapper
public interface SomeDao extends BaseMapper<SomeEntity> {

    @Transactional(rollbackFor = Exception.class)
    int updateSomeData(@Param("param") String param);

    @Transactional(readOnly = true)
    SomeEntity selectSomeData(@Param("id") Long id);
}
```

### 异常处理
```java
// ✅ 业务异常处理
@Service
public class SomeServiceImpl implements SomeService {

    public void someOperation() {
        try {
            // 业务逻辑
            someManager.processData();
        } catch (BusinessException e) {
            log.warn("业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new SystemException("SYSTEM_ERROR", "系统繁忙，请稍后重试", e);
        }
    }
}

// ✅ 全局异常处理
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e) {
        log.error("[系统异常] error={}", e.getMessage(), e);
        return ResponseDTO.error("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
```

---

## 🔗 相关文档参考

### 核心架构文档
- **📋 CLAUDE.md**: 全局架构规范 (强制遵循)
- **🏗️ 四层架构详解**: Controller→Service→Manager→DAO架构模式
- **🔧 依赖注入规范**: 统一使用@Resource注解
- **📦 DAO层规范**: 统一使用Dao后缀和@Mapper注解

### 技术栈文档
- **Spring Boot 3.5.8**: 微服务框架文档
- **Sa-Token**: 认证授权框架文档
- **MyBatis-Plus**: ORM框架文档
- **Nacos**: 服务注册发现和配置中心

### 业务模块文档
- **🏢 企业OA系统**: OA办公相关业务
- **🔒 安全体系规范**: 认证授权和安全管理
- **📊 审计监控规范**: 审计日志和监控告警

---

**📋 重要提醒**:
1. 本技能严格遵循IOE-DREAM四层架构规范
2. 所有代码示例使用Jakarta EE 3.0+包名规范
3. 统一使用@Resource依赖注入，禁止使用@Autowired
4. 统一使用@Mapper注解和Dao后缀命名
5. 必须配置完整的Spring Boot 3.5.8 + Spring Cloud 2025.0.0技术栈

**让我们一起建设安全、可靠、高效的公共服务体系！** 🚀

---
**文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
**创建时间**: 2025-12-08
**最后更新**: 2025-12-08
**技能等级**: ★★★★★ (顶级专家)
**适用架构**: Spring Boot 3.5.8 + Spring Cloud 2025.0.0 + Sa-Token + MyBatis-Plus