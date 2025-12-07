# 设计文档：门禁微服务缺失功能完善

## 架构设计原则

### 1. 四层架构规范
严格遵循Controller→Service→Manager→DAO四层架构模式：

```java
// Controller层 - 只负责参数验证和调用Service
@RestController
public class AccessApprovalController {
    @Resource
    private AccessApprovalService approvalService;

    @PostMapping("/apply")
    public ResponseDTO<String> submitApplication(@Valid @RequestBody ApprovalForm form) {
        return approvalService.submitApplication(form);
    }
}

// Service层 - 业务逻辑处理
@Service
public class AccessApprovalServiceImpl implements AccessApprovalService {
    @Resource
    private ApprovalProcessManager approvalProcessManager;

    public ResponseDTO<String> submitApplication(ApprovalForm form) {
        // 业务逻辑处理
    }
}

// Manager层 - 复杂业务流程编排
@Component
public class ApprovalProcessManager {
    @Resource
    private ApprovalRuleEngine ruleEngine;

    public ApprovalResult processApproval(ApprovalRequest request) {
        // 复杂流程编排
    }
}

// DAO层 - 数据访问
@Repository
public interface ApprovalProcessDao {
    int insert(ApprovalProcessEntity entity);
}
```

### 2. 模块设计架构

#### 2.1 审批流程管理模块
```
approval/                          # 审批流程管理模块
├── controller/
│   └── AccessApprovalController   # 审批控制器
├── service/
│   ├── AccessApprovalService      # 审批服务接口
│   └── impl/
│       └── AccessApprovalServiceImpl # 审批服务实现
├── manager/
│   ├── ApprovalProcessManager     # 审批流程管理器
│   ├── ApprovalRuleEngine         # 审批规则引擎
│   └── ApprovalNotificationManager # 审批通知管理器
├── domain/
│   ├── entity/
│   │   ├── ApprovalProcessEntity  # 审批流程实体
│   │   ├── ApprovalStepEntity     # 审批步骤实体
│   │   └── VisitorReservationEntity # 访客预约实体
│   ├── form/
│   │   ├── ApprovalApplicationForm # 审批申请表单
│   │   └── VisitorReservationForm # 访客预约表单
│   └── vo/
│       ├── ApprovalStatusVO       # 审批状态视图
│       └── VisitorInfoVO          # 访客信息视图
└── repository/
    ├── ApprovalProcessDao         # 审批流程DAO
    ├── ApprovalStepDao            # 审批步骤DAO
    └── VisitorReservationDao      # 访客预约DAO
```

#### 2.2 系统配置模块
```
config/                            # 系统配置模块
├── controller/
│   └── SystemConfigController     # 系统配置控制器
├── service/
│   ├── SystemConfigService        # 系统配置服务
│   ├── UserPermissionService      # 用户权限服务
│   ├── LicenseService             # 许可证服务
│   └── BackupService              # 备份服务
├── manager/
│   ├── ConfigCacheManager         # 配置缓存管理器
│   ├── PermissionManager          # 权限管理器
│   └── BackupManager              # 备份管理器
├── domain/
│   ├── entity/
│   │   ├── SystemConfigEntity     # 系统配置实体
│   │   ├── UserPermissionEntity   # 用户权限实体
│   │   └── LicenseEntity          # 许可证实体
│   └── vo/
│       ├── ConfigItemVO           # 配置项视图
│       └── PermissionTreeVO       # 权限树视图
└── repository/
    ├── SystemConfigDao            # 系统配置DAO
    └── UserPermissionDao          # 用户权限DAO
```

#### 2.3 高级功能模块
```
advanced/                          # 高级功能模块
├── controller/
│   ├── GlobalApbController        # 全局反潜控制器
│   ├── GlobalLinkageController    # 全局联动控制器
│   └── EmergencyController        # 应急控制器
├── service/
│   ├── GlobalApbService           # 全局反潜服务
│   ├── GlobalLinkageService       # 全局联动服务
│   └── EmergencyService           # 应急服务
├── manager/
│   ├── RuleEngineManager          # 规则引擎管理器
│   ├── DeviceLinkageManager       # 设备联动管理器
│   └── EmergencyManager           # 应急管理器
├── engine/
│   ├── AntiPassbackEngine         # 反潜引擎
│   ├── LinkageEngine              # 联动引擎
│   └── EmergencyEngine            # 应急引擎
└── domain/
    ├── entity/
    │   ├── AntiPassbackRuleEntity # 反潜规则实体
    │   ├── LinkageRuleEntity      # 联动规则实体
    │   └── EmergencyPlanEntity    # 应急预案实体
    └── vo/
        ├── RuleConfigVO           # 规则配置视图
        └── EmergencyStatusVO      # 应急状态视图
```

## 技术架构设计

### 1. 事件驱动架构
```java
// 事件发布者
@Component
public class AccessEventPublisher {
    @Resource
    private ApplicationEventPublisher eventPublisher;

    public void publishAccessEvent(AccessEvent event) {
        eventPublisher.publishEvent(event);
    }
}

// 事件监听者
@Component
public class AccessEventListener {
    @EventListener
    @Async
    public void handleAccessEvent(AccessEvent event) {
        // 处理门禁事件
    }

    @EventListener
    @Async
    public void handleAlarmEvent(AlarmEvent event) {
        // 处理报警事件
    }
}
```

### 2. 规则引擎设计
```java
// 规则引擎接口
public interface RuleEngine<T, R> {
    R execute(T input);
    void addRule(Rule<T, R> rule);
    void removeRule(Rule<T, R> rule);
}

// 反潜规则引擎
@Component
public class AntiPassbackRuleEngine implements RuleEngine<AccessEvent, ApbResult> {
    private final List<AntiPassbackRule> rules = new ArrayList<>();

    @Override
    public ApbResult execute(AccessEvent event) {
        return rules.stream()
                .map(rule -> rule.apply(event))
                .filter(result -> !result.isAllowed())
                .findFirst()
                .orElse(ApbResult.allowed());
    }
}
```

### 3. 设备协议适配框架
```java
// 协议适配器接口
public interface DeviceProtocolAdapter {
    DeviceStatus getDeviceStatus(String deviceId);
    void sendCommand(String deviceId, DeviceCommand command);
    void subscribeEvents(String deviceId, Consumer<DeviceEvent> eventHandler);
}

// 协议适配器工厂
@Component
public class DeviceProtocolAdapterFactory {
    private final Map<String, DeviceProtocolAdapter> adapters;

    public DeviceProtocolAdapter getAdapter(String deviceType) {
        return adapters.get(deviceType);
    }
}
```

### 4. 权限自动分配机制
```java
// 权限自动分配服务
@Service
public class PermissionAutoAssignmentService {

    @EventListener
    public void handleNewAreaAccess(NewAreaAccessEvent event) {
        // 新增门权限自动分配给区域人员
        assignDoorPermissionsToAreaPersons(event.getAreaId(), event.getDoorId());
    }

    @EventListener
    public void handleNewPersonAccess(NewPersonAccessEvent event) {
        // 新增人员自动分配区域门权限
        assignAreaPermissionsToPerson(event.getPersonId(), event.getAreaId());
    }
}
```

## 数据库设计

### 1. 审批流程表设计
```sql
-- 审批流程主表
CREATE TABLE access_approval_process (
    process_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_type VARCHAR(50) NOT NULL COMMENT '流程类型：权限申请/访客预约/紧急权限',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    application_content TEXT COMMENT '申请内容',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    current_step INT DEFAULT 1 COMMENT '当前步骤',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_applicant_status (applicant_id, status),
    INDEX idx_status_time (status, created_time)
) COMMENT '审批流程表';

-- 审批步骤表
CREATE TABLE access_approval_step (
    step_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_id BIGINT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    approver_id BIGINT COMMENT '审批人ID',
    step_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '步骤状态',
    approval_comment TEXT COMMENT '审批意见',
    approval_time TIMESTAMP NULL COMMENT '审批时间',
    INDEX idx_process_step (process_id, step_status)
) COMMENT '审批步骤表';
```

### 2. 系统配置表设计
```sql
-- 系统配置表
CREATE TABLE system_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    description VARCHAR(500) COMMENT '配置描述',
    is_encrypted TINYINT(1) DEFAULT 0 COMMENT '是否加密',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (config_type)
) COMMENT '系统配置表';

-- 用户权限表
CREATE TABLE user_permission (
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    permission_type VARCHAR(50) NOT NULL COMMENT '权限类型',
    granted_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expire_time TIMESTAMP NULL COMMENT '过期时间',
    UNIQUE KEY uk_user_resource (user_id, resource_type, resource_id),
    INDEX idx_user_type (user_id, resource_type),
    INDEX idx_expire (expire_time)
) COMMENT '用户权限表';
```

### 3. 高级功能规则表设计
```sql
-- 反潜规则表
CREATE TABLE anti_passback_rule (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL,
    area_id BIGINT NOT NULL,
    rule_type VARCHAR(50) NOT NULL COMMENT '反潜类型',
    time_window INT NOT NULL COMMENT '时间窗口(秒)',
    is_enabled TINYINT(1) DEFAULT 1,
    rule_config JSON COMMENT '规则配置',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_area_enabled (area_id, is_enabled)
) COMMENT '反潜规则表';

-- 联动规则表
CREATE TABLE linkage_rule (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL,
    trigger_condition JSON NOT NULL COMMENT '触发条件',
    linkage_action JSON NOT NULL COMMENT '联动动作',
    is_enabled TINYINT(1) DEFAULT 1,
    priority INT DEFAULT 100 COMMENT '优先级',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enabled_priority (is_enabled, priority)
) COMMENT '联动规则表';
```

## 性能优化设计

### 1. 缓存策略
```java
@Service
public class AccessCacheService {

    @Cacheable(value = "device:status", key = "#deviceId")
    public DeviceStatus getDeviceStatus(String deviceId) {
        // 设备状态缓存
    }

    @Cacheable(value = "permission:user", key = "#userId")
    public List<Permission> getUserPermissions(Long userId) {
        // 用户权限缓存
    }

    @CacheEvict(value = "permission:user", key = "#userId")
    public void clearUserPermissionCache(Long userId) {
        // 清除用户权限缓存
    }
}
```

### 2. 异步处理
```java
@Service
public class AsyncEventProcessor {

    @Async("accessEventExecutor")
    public CompletableFuture<Void> processAccessEvent(AccessEvent event) {
        // 异步处理门禁事件
        return CompletableFuture.completedFuture(null);
    }

    @Async("alarmExecutor")
    public CompletableFuture<Void> processAlarmEvent(AlarmEvent event) {
        // 异步处理报警事件
        return CompletableFuture.completedFuture(null);
    }
}
```

### 3. 批量操作优化
```java
@Service
public class BatchOperationService {

    @Transactional
    public void batchUpdateDeviceStatus(List<DeviceStatusUpdate> updates) {
        // 批量更新设备状态
        updates.stream()
                .collect(Collectors.groupingBy(DeviceStatusUpdate::getDeviceType))
                .forEach(this::updateDevicesByType);
    }
}
```

## 安全设计

### 1. 数据加密
```java
@Component
public class DataEncryptionService {

    @Value("${app.encryption.key}")
    private String encryptionKey;

    public String encryptSensitiveData(String data) {
        // 敏感数据加密
    }

    public String decryptSensitiveData(String encryptedData) {
        // 敏感数据解密
    }
}
```

### 2. 审计日志
```java
@Aspect
@Component
public class AccessAuditAspect {

    @Around("@annotation(auditable)")
    public Object auditAccessOperation(ProceedingJoinPoint joinPoint, Auditable auditable) {
        // 记录门禁操作审计日志
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            recordAuditLog(auditable.operation(), "SUCCESS", startTime);
            return result;
        } catch (Exception e) {
            recordAuditLog(auditable.operation(), "FAILURE", startTime, e.getMessage());
            throw e;
        }
    }
}
```

这个设计文档提供了完整的技术架构设计，确保实现门禁系统的所有缺失功能，同时保持架构的一致性和可扩展性。