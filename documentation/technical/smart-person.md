# 人员管理公共模块开发文档

> **📋 版本**: v1.0.0
> **📋 创建时间**: 2025-01-10
> **📋 适用范围**: 所有需要人员管理功能的业务模块

---

## 📖 模块概述

### 模块简介
smart-person 是 SmartAdmin 项目的人员管理公共模块，提供统一的人员信息管理、组织架构、角色分配、人员权限等功能，支持多类型人员管理和灵活的组织结构。

### 核心特性
- **多类型人员支持**: 员工、访客、承包商、安保人员等
- **组织架构管理**: 支持多级部门、岗位层级结构
- **角色权限体系**: 基于RBAC的角色权限控制
- **人员区域权限**: 人员与区域的多对多关系管理
- **生命周期管理**: 人员入职、转岗、离职全生命周期
- **人员画像分析**: 基于行为数据的用户画像

---

## 🏗️ 架构设计

### 模块结构

```
smart-person/
├── controller/                    # 人员控制器
│   ├── PersonController.java               # 人员管理控制器
│   ├── DepartmentController.java          # 部门管理控制器
│   ├── PositionController.java           # 岗位管理控制器
│   └── RoleController.java                # 角色管理控制器
│   └── PersonProfileController.java        # 人员画像控制器
├── service/                      # 人员服务层
│   ├── PersonService.java                # 人员管理服务
│   ├── DepartmentService.java          # 部门管理服务
│   ├── PositionService.java             # 岗位管理服务
│   ├── RoleService.java                # 角色管理服务
│   └── PersonProfileService.java        # 人员画像服务
│   └── PersonLifeCycleService.java      # 人员生命周期服务
├── manager/                      # 人员管理层
│   ├── PersonManager.java                # 人员管理器
│   ├── OrganizationManager.java         # 组织架构管理器
│   ├── RolePermissionManager.java      # 角色权限管理器
│   ├── PersonDataAnalysisManager.java   # 人员数据分析管理器
│   └── PersonNotificationManager.java    # 人员通知管理器
├── dao/                          # 人员数据层
│   ├── PersonDao.java                   # 人员DAO
│   ├── DepartmentDao.java               # 部门DAO
│   ├── PositionDao.java                 # 岗位DAO
│   ├── RoleDao.java                     # 角色DAO
│   ├── PersonProfileDao.java             # 人员画像DAO
│   └── PersonRelationDao.java             # 人员关系DAO
│   └── PersonAreaDao.java                # 人员区域关联DAO
├── entity/                       # 人员实体
│   ├── PersonEntity.java                 # 人员实体
│   ├── DepartmentEntity.java             # 部门实体
│   ├── PositionEntity.java               # 岗位实体
│   ├── RoleEntity.java                   # 角色实体
│   ├── PersonProfileEntity.java          # 人员画像实体
│   ├── PersonRelationEntity.java        # 人员关系实体
│   └── PersonAreaEntity.java              # 人员区域关联实体
├── algorithm/                    # 人员算法
│   ├── PersonRecommendationAlgorithm.java     # 人员推荐算法
│   ├── PermissionMatchingAlgorithm.java     # 权限匹配算法
│   ├── PersonClusteringAlgorithm.java        # 人员聚类算法
│   └── UserProfileAnalyzer.java          # 用户画像分析
│   └── AttendanceAnalyzer.java           # 考勤数据分析
│   └── BehaviorAnalyzer.java            # 行为分析算法
├── service/                      # 扩展服务
│   ├── ImportService.java                 # 人员导入服务
│   ├── ExportService.java                # 人员导出服务
│   ├── SyncService.java                 # 人员数据同步服务
│   └── ValidationService.java             # 人员数据验证服务
│   └── PersonStatisticsService.java       # 人员统计服务
│   └── PersonAuditService.java           # 人员审计服务
└── notification/                 # 通知服务
│   ├── PersonNotificationService.java     # 人员通知服务
│   ├── RoleChangeNotificationService.java   # 角色变更通知
│   ├── PersonAbsenceNotificationService.js   # 人员缺勤通知
│   └── BirthdayNotificationService.java    # 生日通知
│   └── AnniversaryNotificationService.js # 入职周年通知
└── integration/                  # 集成接口
│   ├── HrIntegrationService.java           # HR系统集成
│   ├── DeviceIntegrationService.java       # 设备系统集成
│   ├── AreaIntegrationService.java        # 区域系统集成
│   └── WorkflowIntegrationService.java     # 工作流集成
└── security/                    # 安全模块
│   ├── PersonSecurityService.java         # 人员安全服务
│   ├── DataPrivacyService.java           # 数据脱敏服务
│   ├── AccessControlService.java         # 访问控制服务
│   └── BiometricService.java           // 生物识别服务
└── profile/                      # 画像模块
│   ├── ProfileCollector.java           # 画像数据收集器
│   ├── ProfileAnalyzer.java             # 画像分析器
│   ├── ProfileCacheManager.java        # 画像缓存管理器
│   └── ProfileVisualizer.java          # 画像可视化器
```

### 核心设计模式

```java
// 策略模式 - 人员类型处理
@Component
public class PersonTypeHandlerFactory {

    private final Map<PersonType, PersonTypeHandler> handlerMap = new ConcurrentHashMap<>();

    public PersonTypeHandlerFactory(List<PersonTypeHandler> handlers) {
        handlers.forEach(handler ->
            handlerMap.put(handler.getSupportedPersonType(), handler));
    }

    /**
     * 获取人员类型处理器
     */
    public PersonTypeHandler getHandler(PersonType personType) {
        PersonTypeHandler handler = handlerMap.get(personType);
        if (handler == null) {
            throw new UnsupportedOperationException("不支持的人员类型: " + personType);
        }
        return handler;
    }

    /**
     * 处理人员创建
     */
    public CompletableFuture<PersonEntity> handlePersonCreate(PersonCreateDTO createDTO) {
        PersonType personType = createDTO.getPersonType();
        PersonTypeHandler handler = getHandler(personType);
        return handler.createPerson(createDTO);
    }

    /**
     * 处理人员更新
     */
    public CompletableFuture<PersonEntity> handlePersonUpdate(PersonUpdateDTO updateDTO) {
        PersonEntity existingPerson = personDao.selectById(updateDTO.getPersonId());
        if (existingPerson == null) {
            throw new SmartException("人员不存在");
        }

        PersonType personType = PersonType.valueOf(existingPerson.getPersonType());
        PersonTypeHandler handler = getHandler(personType);
        return handler.updatePerson(updateDTO, existingPerson);
    }
}

// 观察者模式 - 人员生命周期事件
@Component
public class PersonLifecycleManager {

    private final Map<String, PersonLifecycleListener> listeners = new ConcurrentHashMap<>();

    /**
     * 注册生命周期监听器
     */
    public void registerListener(String eventType, PersonLifecycleListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * 发布人员事件
     */
    public void publishPersonEvent(PersonLifecycleEvent event) {
        List<PersonLifecycleListener> eventListeners = listeners.get(event.getEventType());
        if (eventListeners != null) {
            eventListeners.forEach(listener -> {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    log.error("处理人员生命周期事件失败: {}", event.getEventType(), e);
                }
            });
        }
    }

    /**
     * 异步发布事件
     */
    @Async("personEventExecutor")
    public void publishPersonEventAsync(PersonLifecycleEvent event) {
        publishPersonEvent(event);
    }
}

// 工厂模式 - 通知服务
@Component
public class PersonNotificationServiceFactory {

    private final Map<String, PersonNotificationService> serviceMap = new ConcurrentHashMap<>();

    public PersonNotificationServiceFactory(List<PersonNotificationService> services) {
        services.forEach(service ->
            serviceMap.put(service.getSupportedNotificationType(), service));
    }

    /**
     * 获取通知服务
     */
    public PersonNotificationService getService(String notificationType) {
        PersonNotificationService service = serviceMap.get(notificationType);
        if (service == null) {
            throw new UnsupportedOperationException("不支持的通知类型: " + notificationType);
        }
        return service;
    }

    /**
     * 发送通知
     */
    public CompletableFuture<Void> sendNotification(PersonNotificationDTO notificationDTO) {
        String notificationType = determineNotificationType(notificationDTO);
        PersonNotificationService service = getService(notificationType);
        return service.sendNotification(notificationDTO);
    }
}
```

---

## 🗄️ 数据库设计
## 📋 IOE-DREAM七微服务架构

**核心架构组成**:
- **Gateway Service (8080)**: API网关
- **Common Service (8088)**: 公共模块微服务
- **DeviceComm Service (8087)**: 设备通讯微服务
- **OA Service (8089)**: OA微服务
- **Access Service (8090)**: 门禁服务
- **Attendance Service (8091)**: 考勤服务
- **Video Service (8092)**: 视频服务
- **Consume Service (8094)**: 消费服务
- **Visitor Service (8095)**: 访客服务

**架构特点**:
- 基于Spring Boot 3.5.8 + Java 17
- 严格遵循企业级微服务规范
- 支持高并发、高可用、水平扩展

**技术栈标准**:
- **数据库**: MySQL 8.0 + Druid连接池
- **缓存**: Redis + Caffeine多级缓存
- **注册中心**: Nacos
- **配置中心**: Nacos Config
- **认证授权**: Sa-Token

## 🏗️ 四层架构规范

**标准架构模式**:
```
Controller (接口控制层)
    ↓
Service (核心业务层)
    ↓
Manager (流程管理层)
    ↓
DAO (数据访问层)
```

**层级职责**:
- **Controller层**: HTTP请求处理、参数验证、权限控制
- **Service层**: 核心业务逻辑、事务管理、业务规则验证
- **Manager层**: 复杂流程编排、多数据组装、第三方服务集成
- **DAO层**: 数据库CRUD操作、SQL查询实现、数据访问边界

**严格禁止跨层访问**: Controller不能直接调用Manager/DAO！
### 人员表 (t_person)
## ⚠️ IOE-DREAM零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 @Resource 注入依赖**
- ✅ **必须使用 @Mapper 注解** (禁止@Repository)
- ✅ **必须使用 Dao 后缀** (禁止Repository)
- ✅ **必须使用 @RestController 注解**
- ✅ **必须使用 @Valid 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**

**严格禁止事项**:
- ❌ **禁止使用 @Autowired 注入**
- ❌ **禁止使用 @Repository 注解**
- ❌ **禁止使用 Repository 后缀命名**
- ❌ **禁止跨层访问**
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止直接访问数据库**

**违规后果**: P0级问题，立即修复，禁止合并！

```sql
CREATE TABLE t_person (
    person_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '人员ID',
    person_code VARCHAR(50) NOT NULL UNIQUE COMMENT '人员编码',
    person_name VARCHAR(100) NOT NULL COMMENT '人员姓名',
    person_type VARCHAR(20) NOT NULL COMMENT '人员类型',
    person_level INT DEFAULT 1 COMMENT '人员级别',
    employee_id VARCHAR(50) COMMENT '员工编号',
    job_number VARCHAR(50) COMMENT '工号',
    gender TINYINT DEFAULT 1 COMMENT '性别：1-男，2-女',
    birth_date DATE COMMENT '出生日期',
    mobile VARCHAR(20) COMMENT '手机号码',
    email VARCHAR(100) COMMENT '邮箱地址',
    id_card VARCHAR(20) COMMENT '身份证号',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    department_id BIGINT COMMENT '所属部门ID',
    position_id BIGINT COMMENT '岗位ID',
    role_ids TEXT COMMENT '角色ID列表',
    work_location_id BIGINT COMMENT '工作地点ID',
    work_status TINYINT DEFAULT 1 COMMENT '工作状态：1-在职，2-离职，3-请假',
    hire_date DATE COMMENT '入职时间',
    leave_date DATE COMMENT '离职时间',
    contract_type VARCHAR(20) COMMENT '合同类型',
    contract_start_date DATE COMMENT '合同开始日期',
    contract_end_date DATE COMMENT '合同结束日期',
    emergency_contact VARCHAR(100) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
    home_address TEXT COMMENT '家庭住址',
    work_phone VARCHAR(20) COMMENT '工作电话',
    education_level VARCHAR(20) COMMENT '教育程度',
    major VARCHAR(100) COMMENT '专业',
    skills TEXT COMMENT '技能列表',
    certifications TEXT COMMENT '认证证书',
    social_profiles JSON COMMENT '社交档案JSON',
    work_experience TEXT COMMENT '工作经历JSON',
    person_profile JSON COMMENT '人员画像JSON',
    contact_info JSON COMMENT '联系信息JSON',
    security_config JSON COMMENT '安全配置JSON',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    version INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    INDEX idx_person_code (person_code),
    INDEX idx_person_type (person_type),
    INDEX idx_employee_id (employee_id),
    INDEX idx_department_id (department_id),
    INDEX idx_position_id (position_id),
    INDEX idx_work_status (work_status),
    INDEX id_card (id_card),
    INDEX mobile (mobile),
    INDEX email (email),
    INDEX hire_date (hire_date),
    INDEX leave_date (leave_date),
    INDEX status (status),
    INDEX deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time)
) COMMENT = '人员表';

-- 人员类型枚举值
INSERT INTO t_sys_dict (dict_type, dict_key, dict_value, sort_order, remark) VALUES
('PERSON_TYPE', 'EMPLOYEE', '员工', 1, '正式员工'),
('PERSON_TYPE', 'INTERN', '实习生', 2, '实习生'),
('PERSON_TYPE', 'CONTRACTOR', '承包商', 3, '外部承包商'),
('PERSON_TYPE', 'VISITOR', '访客', 4, '访客'),
('PERSON_TYPE', 'SECURITY', '安保人员', 5, '安保人员'),
('PERSON_TYPE', 'VENDOR', '供应商', 6, '供应商'),
('PERSON_TYPE', 'PARTNER', '合作伙伴', 7, '合作伙伴');
```

### 部门表 (t_department)

```sql
CREATE TABLE t_department (
    dept_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    dept_code VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编码',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_type VARCHAR(50) COMMENT '部门类型',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_level INT DEFAULT 1 COMMENT '部门层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    manager_id BIGINT COMMENT '负责人ID',
    description TEXT COMMENT '部门描述',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    member_count INT DEFAULT 0 COMMENT '成员数量',
    area_id BIGINT COMMENT '所属区域ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    version INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    INDEX idx_parent_id (parent_id),
    INDEX dept_type (dept_type),
    INDEX dept_level (dept_level),
    INDEX manager_id (manager_id),
    INDEX area_id (area_id),
    INDEX status (status),
    INDEX idx_sort_order (sort_order),
    INDEX deleted_flag (deleted_flag),
    UNIQUE KEY uk_dept_code (dept_code)
) COMMENT = '部门表';

-- 部门类型枚举值
INSERT INTO t_sys_dict (dict_type, dict_key, dict_value, sort_order, remark) VALUES
('DEPT_TYPE', 'COMPANY', '综合部门', 1, '综合部门'),
('DEPT_TYPE', 'TECH', '技术部门', 2, '技术部门'),
('DEPT_TYPE', 'HR', '人力资源', 3, '人力资源部'),
('DEPT_TYPE', 'FINANCE', '财务部门', 4, '财务部门'),
('DEPT_TYPE', 'ADMIN', '行政部', 5, '行政部'),
('DEPT_TYPE', 'MARKETING', '市场部', 6, '市场部');
```

### 岗位表 (t_position)

```sql
CREATE TABLE t_position (
    position_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '岗位ID',
    position_code VARCHAR(50) NOT NULL UNIQUE COMMENT '岗位编码',
    position_name VARCHAR(100) NOT NULL COMMENT '岗位名称',
    position_type VARCHAR(50) COMMENT '岗位类型',
    position_level INT DEFAULT 1 COMMENT '岗位级别',
    parent_id BIGINT DEFAULT 0 COMMENT '父岗位ID',
    dept_id BIGINT COMMENT '所属部门ID',
    work_level TINYINT DEFAULT 1 COMMENT '工作级别',
    skill_requirements TEXT COMMENT '技能要求',
    responsibility TEXT COMMENT '职责描述',
    qualification_requirements TEXT COMMENT '任职要求',
    salary_range JSON COMMENT '薪资范围',
    career_path JSON COMMENT '职业发展路径',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    version INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    INDEX idx_position_code (position_code),
    INDEX position_type (position_type),
    position_level (position_level),
    parent_id (parent_id),
    dept_id (dept_id),
    work_level (work_level),
    idx_sort_order (sort_order),
    INDEX status (status),
    INDEX deleted_flag (deleted_flag),
    UNIQUE KEY uk_position_code (position_code)
) COMMENT = '岗位表';
```

### 角色表 (t_role)

```sql
CREATE TABLE t_role (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_type VARCHAR(20) COMMENT '角色类型',
    role_level TINYINT DEFAULT 1 COMMENT '角色级别',
    role_scope JSON COMMENT '角色范围JSON',
    data_scope JSON COMMENT '数据范围JSON',
    function_permissions JSON COMMENT '功能权限JSON',
    device_permissions JSON COMMENT '设备权限JSON',
    area_permissions JSON COMMENT '区域权限JSON',
    user_permissions JSON COMMENT '用户权限JSON',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：1-是，0-否',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统角色：1-是，0-否',
    max_session_count INT DEFAULT 1 COMMENT '最大会话数',
    session_timeout INT DEFAULT 30 COMMENT '会话超时（分钟）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    version INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    INDEX idx_role_code (role_code),
    INDEX role_type (role_type),
    role_level (role_level),
    idx_is_default (is_default),
    idx_is_system (is_system),
    idx_sort_order (sort_order),
    INDEX status (status),
    INDEX deleted_flag (deleted_flag),
    UNIQUE KEY uk_role_code (role_code)
) COMMENT = '角色表';
```

### 人员角色关联表 (t_person_role)

```sql
CREATE TABLE t_person_role (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    person_id BIGINT NOT NULL COMMENT '人员ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    relation_type VARCHAR(20) DEFAULT 'PRIMARY' COMMENT '关联类型',
    is_primary TINYINT DEFAULT 0 COMMENT '是否主角色',
    grant_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    grant_user_id BIGINT COMMENT '授权人ID',
    grant_remark TEXT COMMENT '授权备注',
    valid_start_time DATETIME COMMENT '生效时间',
    valid_end_time DATETIME COMMENT '失效时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效，0-已撤销',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_person_id (person_id),
    INDEX role_id (role_id),
    idx_relation_type (relation_type),
    idx_is_primary (is_primary),
    idx_status (status),
    INDEX valid_time_range (valid_start_time, valid_end_time),
    INDEX grant_time (grant_time),
    INDEX deleted_flag (deleted_flag),
    UNIQUE KEY uk_person_role (person_id, role_id, relation_type)
) COMMENT = '人员角色关联表';
```

### 人员区域关联表 (t_person_area)

```sql
CREATE TABLE t_person_area (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    person_id BIGINT NOT NULL COMMENT '人员ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    access_level TINYINT DEFAULT 1 COMMENT '访问级别',
    access_time_config JSON COMMENT '访问时间配置JSON',
    access_reason TEXT COMMENT '访问原因',
    grant_user_id BIGINT COMMENT '授权人ID',
    grant_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    expire_time DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效，0-已失效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_person_id (person_id),
    idx_area_id (area_id),
    idx_access_level (access_level),
    idx_status (status),
    INDEX valid_time_range (valid_start_time, valid_end_time),
    INDEX grant_time (grant_time),
    INDEX expire_time (expire_time),
    INDEX deleted_flag (deleted_flag),
    UNIQUE KEY uk_person_area (person_id, area_id)
) COMMENT = '人员区域关联表';
```

### 人员配置表 (t_person_config)

```sql
CREATE TABLE t_person_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    person_id BIGINT COMMENT '人员ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_desc TEXT COMMENT '配置描述',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密：1-是，0-否',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：1-是，0-否',
    config_version VARCHAR(20) DEFAULT '1.0' COMMENT '配置版本',
    effective_time DATETIME COMMENT '生效时间',
    expire_time DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    UNIQUE KEY uk_person_config (person_id, config_type, config_key, config_version)
    INDEX idx_person_id (person_id),
    idx_config_type (config_type),
    idx_config_key (config_key),
    idx_status (status),
    idx_effective_time (effective_time),
    idx_expire_time (expire_time),
    INDEX deleted_flag (deleted_flag)
) COMMENT = '人员配置表';
```

### 人员事件表 (t_person_event)

```sql
CREATE TABLE t_person_event (
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '事件ID',
    person_id BIGINT NOT NULL COMMENT '人员ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    event_title VARCHAR(200) COMMENT '事件标题',
    event_desc TEXT COMMENT '事件描述',
    event_data JSON COMMENT '事件数据JSON',
    event_level TINYINT DEFAULT 1 COMMENT '事件级别：1-信息，2-警告，3-错误，4-严重',
    source_type VARCHAR(20) COMMENT '来源类型',
    source_id BIGINT COMMENT '来源ID',
    operator_id BIGINT COMMENT '操作员ID',
    event_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    process_time DATETIME COMMENT '处理时间',
    process_result TINYINT DEFAULT 0 COMMENT '处理结果：1-成功，0-失败',
    process_remark TEXT COMMENT '处理备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_person_id (person_id),
    idx_event_type (event_type),
    idx_event_level (event_level),
    idx_source_type (source_type),
    idx_source_id (source_id),
    idx_event_time (event_time),
    idx_process_time (process_time),
    idx_process_result (process_result),
    INDEX create_time (create_time)
) COMMENT = '人员事件表';
```

---

## 🔧 后端实现

### 核心控制器 (PersonController)

```java
@RestController
@RequestMapping("/api/person")
@Tag(name = "人员管理", description = "人员管理相关接口")
public class PersonController {

    @Resource
    private PersonService personService;

    @GetMapping("/page")
    @Operation(summary = "分页查询人员")
    @SaCheckPermission("person:page")
    public ResponseDTO<PageResult<PersonVO>> queryPage(PersonQueryDTO queryDTO) {
        PageResult<PersonVO> result = personService.queryPage(queryDTO);
        return ResponseDTO.ok(result);
    }

    @PostMapping
    @Operation(summary = "新增人员")
    @SaCheckPermission("person:add")
    public ResponseDTO<String> add(@Valid @RequestBody PersonCreateDTO createDTO) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        personService.create(createDTO, userId);
        return ResponseDTO.ok();
    }

    @PutMapping("/{personId}")
    @Operation(summary = "修改人员")
    @SaCheckPermission("person:update")
    public ResponseDTO<String> update(@PathVariable Long personId,
                                     @Valid @RequestBody PersonUpdateDTO updateDTO) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        personService.update(personId, updateDTO, userId);
        return ResponseDTO.ok();
    }

    @DeleteMapping("/{personId}")
    @Operation(summary = "删除人员")
    @SaCheckPermission("person:delete")
    public ResponseDTO<String> delete(@PathVariable Long personId) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        personService.delete(personId, userId);
        return ResponseDTO.ok();
    }

    @GetMapping("/{personId}")
    @Operation(summary = "获取人员详情")
    @SaCheckPermission("person:detail")
    public ResponseDTO<PersonDetailVO> getDetail(@PathVariable Long personId) {
        PersonDetailVO detail = personService.getDetail(personId);
        return ResponseDTO.ok(detail);
    }

    @GetMapping("/profile/{personId}")
    @Operation(summary = "获取人员画像")
    @SaCheckPermission("person:profile:view")
    public ResponseDTO<PersonProfileVO> getProfile(@PathVariable Long personId) {
        PersonProfileVO profile = personService.getProfile(personId);
        return ResponseDTO(profile);
    }

    @PostMapping("/{personId}/profile")
    @Operation(summary = "更新人员画像")
    @SaCheckPermission("person:profile:update")
    public ResponseDTO<String> updateProfile(@PathVariable Long personId,
                                                @Valid @RequestBody PersonProfileUpdateDTO updateDTO) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        personService.updateProfile(personId, updateDTO, userId);
        return ResponseDTO.ok();
    }

    @GetMapping("/{personId}/events")
    @Operation(summary = "获取人员事件")
    @SaCheckPermission("person:events:view")
    public ResponseDTO<PageResult<PersonEventVO>> getPersonEvents(
            @PathVariable Long personId,
            PersonEventQueryDTO queryDTO) {
        PageResult<PersonEventVO> events = personService.getPersonEvents(personId, queryDTO);
        return ResponseDTO.ok(events);
    }

    @PostMapping("/import")
    @Operation(summary = "导入人员")
    @SaCheckPermission("person:import")
    public ResponseDTO<PersonImportResultVO> importPersons(@RequestParam("file") MultipartFile file,
                                                                      @RequestParam(defaultValue = "") String batchCode) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        PersonImportResultVO result = personService.importPersons(file, batchCode, userId);
        return ResponseDTO.ok(result);
    }

    @PostMapping("/export")
    @Operation(summary = "导出人员")
    @SaCheckPermission("person:export")
    public void exportPersons(@Valid @RequestBody PersonExportDTO exportDTO,
                                      HttpServletResponse response) {
        personService.exportPersons(exportDTO, response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取人员统计")
    @SaCheckPermission("person:statistics")
    public ResponseDTO<PersonStatisticsVO> getStatistics() {
        PersonStatisticsVO statistics = personService.getStatistics();
        return ResponseDTO.ok(statistics);
    }
}
```

### 核心服务层 (PersonService)

```java
@Service
@Transactional(readOnly = true)
public class PersonService {

    @Resource
    private PersonManager personManager;
    @Resource
    private PersonTypeHandlerFactory typeHandlerFactory;
    @Resource
    PersonLifecycleManager lifecycleManager;
    @Resource
    PersonAreaService areaService;

    @Transactional(rollbackFor Exception.class)
    public void create(PersonCreateDTO createDTO, Long currentUserId) {
        // 1. 验证人员编码唯一性
        validatePersonCodeUnique(createDTO.getPersonCode());

        // 2. 验证手机号唯一性
        if (StringUtils.isNotBlank(createDTO.getMobile())) {
            validateMobileUnique(createDTO.getMobile());
        }

        // 3. 验证邮箱唯一性
        if (StringUtils.isNotBlank(createDTO.getEmail())) {
            validateEmailUnique(createDTO.getEmail());
        }

        // 4. 验证部门和岗位存在性
        if (createDTO.getDepartmentId() != null) {
            validateDepartmentExists(createDTO.getDepartmentId());
        }
        if (createDTO.getPositionId() != null) {
            validatePositionExists(createDTO.getPositionId());
        }

        // 5. 创建人员
        PersonEntity person = convertToEntity(createDTO);
        person.setCreateUserId(currentUserId);

        // 6. 处理特定人员类型的创建逻辑
        CompletableFuture<PersonEntity> personFuture = typeHandlerFactory
            .handlePersonCreate(createDTO);

        // 7. 等待处理完成
        personFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("人员创建失败", throwable);
                throw new SmartException("人员创建失败: " + throwable.getMessage());
            }
        });

        PersonEntity createdPerson = personFuture.get();

        // 8. 绑定默认角色
        bindDefaultRoles(createdPerson.getPersonId());

        // 9. 发布创建事件
        publishPersonEvent(PersonLifecycleEvent.builder()
            .personId(createdPerson.getPersonId())
            .eventType("PERSON_CREATE")
            .eventData(Map.of(
                "personId", createdPerson.getPersonId(),
                "personName", createdPerson.getPersonName(),
                "personType", createdPerson.getPersonType(),
                "operatorId", currentUserId
            ))
            .eventTime(LocalDateTime.now())
            .build());

        // 10. 更新缓存
        personCacheManager.cachePerson(createdPerson);
    }

    @Transactional(rollbackFor Exception.class)
    public void update(Long personId, PersonUpdateDTO updateDTO, Long currentUserId) {
        // 1. 验证人员存在性
        PersonEntity existingPerson = personManager.getById(personId);
        if (existingPerson == null) {
            throw new SmartException("人员不存在");
        }

        // 2. 验证修改权限
        validateUpdatePermission(personId, currentUserId);

        // 3. 处理特定人员类型的更新逻辑
        CompletableFuture<PersonEntity> personFuture = typeHandlerFactory
            .handlePersonUpdate(updateDTO, existingPerson);

        // 4. 等待处理完成
        PersonEntity updatedPerson = personFuture.get();

        // 5. 更新缓存
        personCacheManager.cachePerson(updatedPerson);

        // 6. 发布更新事件
        publishPersonEvent(PersonLifecycleEvent.builder()
            .personId(updatedPerson.getPersonId())
            .eventType("PERSON_UPDATE")
            .eventData(Map.of(
                "personId", updatedPerson.getPersonId(),
                "personName", updatedPerson.getPersonName(),
                "operatorId", currentUserId
            ))
            .eventTime(LocalDateTime.now())
            .build());

        // 7. 记录操作日志
        operationLogService.logOperation("UPDATE", "更新人员", Map.of(
            "personId", personId,
            "updateDTO", updateDTO
        ));
    }

    @Transactional(rollbackFor Exception.class)
    public void delete(Long personId, Long currentUserId) {
        // 1. 验证人员存在性
        PersonEntity person = personManager.getById(personId);
        if (person == null) {
            throw new SmartException("人员不存在");
        }

        // 2. 验证删除权限
        validateDeletePermission(personId, currentUserId);

        // 3. 软删除所有角色关联
        personRoleService.revokeAllRoles(personId);

        // 4. 软删除所有区域权限
        areaService.revokeAllAreaAccess(personId);

        // 5. 软删除
        personManager.softDelete(personId);

        // 6. 发布删除事件
        publishPersonEvent(PersonLifecycleEvent.builder()
            .personId(personId)
            .eventType("PERSON_DELETE")
            .eventData(Map.of(
                "personId", personId,
                "operatorId", currentUserId
            ))
            .eventTime(LocalDateTime.now())
            .build());

        // 7. 更新缓存
        personCacheManager.clearPersonCache(personId);
    }

    public PersonDetailVO getDetail(Long personId) {
        // 1. 获取基本信息
        PersonEntity person = personManager.getById(personId);
        if (person == null) {
            throw new SmartException("人员不存在");
        }

        // 2. 获取组织信息
        DepartmentEntity department = null;
        if (person.getDepartmentId() != null) {
            department = departmentManager.getById(person.getDepartmentId());
        }

        PositionEntity position = null;
        if (person.getPositionId() != null) {
            position = positionManager.getById(person.getPositionId());
        }

        // 3. 获取角色信息
        List<RoleEntity> roles = personRoleService.getUserRoles(personId);

        // 4. 获取区域权限
        List<AreaEntity> accessibleAreas = areaService.getUserAccessibleAreas(personId);

        // 5. 获取人员画像
        PersonProfileEntity profile = personProfileDao.selectOne(
            new QueryWrapper<PersonProfileEntity>()
                .eq("person_id", personId)
                .orderByDesc("update_time")
                .last("LIMIT 1")
        );

        // 6. 组装详情信息
        PersonDetailVO detail = convertToDetailVO(person);
        detail.setDepartment(convertToVO(department));
        detail.setPosition(convertToVO(position));
        detail.setRoles(roles.stream().map(this::convertToVO).collect(Collectors.toList()));
        detail.setAccessibleAreas(accessibleAreas.stream().map(this::convertToVO).collect(Collectors.toList()));
        detail.setProfile(convertToProfileVO(profile));

        return detail;
    }

    public PersonProfileVO getProfile(Long personId) {
        // 1. 获取人员基本信息
        PersonEntity person = personManager.getById(personId);
        if (person == null) {
            throw new SmartException("人员不存在");
        }

        // 2. 获取画像信息
        PersonProfileEntity profile = personProfileDao.selectOne(
            new QueryWrapper<PersonProfileEntity>()
                .eq("person_id", personId)
                .orderByDesc("update_time")
                .last("LIMIT 1")
        );

        // 3. 分析人员画像数据
        PersonProfileAnalysisResult analysis = profileAnalyzer.analyzeProfile(personId, profile);

        // 4. 组装画像VO
        PersonProfileVO profileVO = convertToProfileVO(profile);
        profile.setAnalysisResult(analysis);

        return profileVO;
    }

    @Transactional(rollbackFor Exception.class)
    public void updateProfile(Long personId, PersonProfileUpdateDTO updateDTO, Long currentUserId) {
        // 1. 验证人员存在性
        validatePersonExists(personId);

        // 2. 更新画像
        PersonProfileEntity profile = convertProfileUpdateToEntity(updateDTO);
        profile.setPersonId(personId);
        profile.setUpdateTime(LocalDateTime.now());

        personProfileDao.updateById(profile);

        // 3. 更新缓存
        personCacheManager.clearProfileCache(personId);

        // 4. 发布画像更新事件
        publishPersonEvent(PersonLifecycleEvent.builder()
            .personId(personId)
            .eventType("PERSON_PROFILE_UPDATE")
            .eventData(Map.of(
                "profileType", updateDTO.getProfileType(),
                "operatorId", currentUserId
            ))
            .eventTime(LocalDateTime.now())
            .build());
    }

    public PageResult<PersonEventVO> getPersonEvents(Long personId, PersonEventQueryDTO queryDTO) {
        // 1. 验证人员存在性
        validatePersonExists(personId);

        // 2. 查询事件
        QueryWrapper<PersonEventEntity> queryWrapper = new QueryWrapper<>();

        if (queryDTO.getEventType() != null) {
            queryWrapper.eq("event_type", queryDTO.getEventType());
        }
        if (queryDTO.getEventLevel() != null) {
            queryWrapper.eq("event_level", queryDTO.getEventLevel());
        }
        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge("event_time", queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le("event_time", queryDTO.getEndTime());
        }
        queryWrapper.orderByDesc("event_time");

        Page<PersonEventEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<PersonEventEntity> result = personEventDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<PersonEventVO> events = result.getRecords().stream()
            .map(this::convertToEventVO)
            .collect(Collectors.toList());

        return PageResult.<PersonEventVO>builder()
            .records(events)
            .total(result.getTotal())
            .pageNum(result.getPageNum())
            .pageSize(result.getPageSize())
            .build();
    }

    public PersonStatisticsVO getStatistics() {
        // 获取人员统计
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", personManager.countAll());
        stats.put("active", personManager.countByStatus(1));
        stats.put("inactive", personManager.countByStatus(0));
        stats.put("employees", personManager.countByType("EMPLOYEE"));
        stats.put("visitors", personManager.countByType("VISITOR"));
        stats.put("contractors", personManager.countByType("CONTRACTOR"));
        stats.put("security", personManager.countByType("SECURITY"));

        // 按类型统计
        Map<String, Map<String, Object>> typeStats = new HashMap<>();
        for (PersonType type : PersonType.values()) {
            typeStats.put(type.name(), typeStats.get(type.name()));
        }

        // 计算工作状态统计
        Map<Integer, Long> workStatusStats = new HashMap<>();
        workStatusStats.put(1, personManager.countByWorkStatus(1)); // 在职
        workStatusStats.put(2, personManager.countByWorkStatus(2)); // 离职
        workStatusStats.put(3, personManager.countByWorkStatus(3)); // 请假

        return PersonStatisticsVO.builder()
            .totalStats(stats)
            .typeStats(typeStats)
            .workStatusStats(workStatusStats)
            .statisticsTime(LocalDateTime.now())
            .build();
    }

    // 私有方法
    private void validatePersonCodeUnique(String personCode) {
        validatePersonCodeUnique(personCode, null);
    }

    private void validatePersonCodeUnique(String personCode, Long excludePersonId) {
        boolean exists = personManager.checkPersonCodeExists(personCode, excludePersonId);
        if (exists) {
            throw new SmartException("人员编码已存在");
        }
    }

    private void validateMobileUnique(String mobile) {
        boolean exists = personManager.checkMobileExists(mobile);
        if (exists) {
            throw new SmartException("手机号已存在");
        }
    }

    private void validateEmailUnique(String email) {
        boolean exists = personManager.checkEmailExists(email);
        if (exists) {
            throw new SmartException("邮箱已存在");
        }
    }

    private void validateDepartmentExists(Long departmentId) {
        DepartmentEntity department = departmentManager.getById(departmentId);
        if (department == null) {
            throw new SmartException("部门不存在");
        }
    }

    private void validatePositionExists(Long positionId) {
        PositionEntity position = positionManager.getById(positionId);
        if (position == null) {
            throw new SmartException("岗位不存在");
        }
    }

    private void validateUpdatePermission(Long personId, Long currentUserId) {
        PersonEntity person = personManager.getById(personId);
        if (person == null) {
            throw new SmartException("人员不存在");
        }

        // 检查是否为本人或是否有修改权限
        if (!person.getPersonId().equals(currentUserId)) {
            // 检查是否有修改权限
            boolean hasPermission = permissionService.hasPermission(
                currentUserId, "person:edit:" + person.getPersonId());
            if (!hasPermission) {
                throw new SmartException("无修改权限");
            }
        }
    }

    private void validateDeletePermission(Long personId, Long currentUserId) {
        PersonEntity person = personManager.getById(personId);
        if (person == null) {
            throw new SmartException("人员不存在");
        }

        // 检查是否为本人或是否有删除权限
        if (!person.getPersonId().equals(currentUserId)) {
            // 检查是否有删除权限
            boolean hasPermission = permissionService.hasPermission(
                currentUserId, "person:delete:" + person.getPersonId());
            if (!hasPermission) {
                throw new SmartException("无删除权限");
            }
        }
    }

    private void bindDefaultRoles(Long personId) {
        // 根据人员类型绑定默认角色
        PersonType personType = personManager.getById(personId).getPersonType();
        List<Long> defaultRoleIds = getDefaultRoleIds(personType);

        for (Long roleId : defaultRoleIds) {
            PersonRoleEntity relation = PersonRoleEntity.builder()
                .personId(personId)
                .roleId(roleId)
                .relationType("SECONDARY")
                .isPrimary(0)
                .status(1)
                .grantTime(LocalDateTime.now())
                .build();

            personRoleDao.insert(relation);
        }
    }

    private List<Long> getDefaultRoleIds(PersonType personType) {
        // 根据人员类型获取默认角色ID
        switch (personType) {
            case EMPLOYEE:
                return getDefaultRoleIds("EMPLOYEE");
            case INTERN:
                return getDefaultRoleIds("INTERN");
            case VISITOR:
                return getDefaultRoleIds("VISITOR");
            case CONTRACTOR:
                return getDefaultRoleIds("CONTRACTOR");
            case SECURITY:
                return getDefaultRoleIds("SECURITY");
            default:
                return getDefaultRoleIds("EMPLOYEE");
        }
    }

    private List<Long> getDefaultRoleIds(String personType) {
        // 从配置中获取默认角色ID
        List<RoleEntity> defaultRoles = roleDao.selectList(
            new QueryWrapper<RoleEntity>()
                .eq("is_default", 1)
                .eq("status", 1)
                .eq("deleted_flag", 0)
                .eq("person_types", personType)
                .orderByAsc("sort_order")
                .limit(5)
        );

        return defaultRoles.stream()
            .map(RoleEntity::getRoleId)
            .collect(Collectors.toList());
    }

    private void publishPersonEvent(PersonLifecycleEvent event) {
        lifecycleManager.publishEvent(event);
    }

    // 转换方法
    private PersonEntity convertToEntity(PersonCreateDTO dto) {
        return PersonEntity.builder()
            .personCode(dto.getPersonCode())
            .personName(dto.getPersonName())
            .personType(dto.getPersonType())
            .personLevel(dto.getPersonLevel())
            .employeeId(dto.getEmployeeId())
            .jobNumber(dto.getJobNumber())
            .gender(dto.getGender())
            .birthDate(dto.getBirthDate())
            .mobile(dto.getMobile())
            .email(dto.getEmail())
            .idCard(dto.getIdCard())
            .avatarUrl(dto.getAvatarUrl())
            .departmentId(dto.getDepartmentId())
            .positionId(dto.getPositionId())
            .workLocationId(dto.getWorkLocationId())
            .workStatus(dto.getWorkStatus())
            .hireDate(dto.getHireDate())
            .leaveDate(dto.getLeaveDate())
            .contractType(dto.getContractType())
            .contractStartDate(dto.getContractStartDate())
            .contractEndDate(dto.getContractEndDate())
            .emergencyContact(dto.getEmergencyContact())
            .emergencyPhone(dto.getEmergencyPhone())
            .homeAddress(dto.getHomeAddress())
            .workPhone(dto.getWorkPhone())
            .educationLevel(dto.getEducationLevel())
            .major(dto.getMajor())
            .skills(JsonUtils.toJsonString(dto.getSkills()))
            .certifications(JsonUtils.toJsonString(dto.getCertifications()))
            .socialProfiles(JsonUtils.toJsonString(dto.getSocialProfiles()))
            .personProfile(JsonUtils.toJsonString(dto.getPersonProfile()))
            .contactInfo(JsonUtils.toJsonString(dto.getContactInfo()))
            .securityConfig(JsonUtils.toJsonString(dto.getSecurityConfig()))
            .status(dto.getStatus())
            .version(1)
            .build();
    }

    private AreaVO convertToVO(AreaEntity area) {
        AreaVO vo = new AreaVO();
        BeanUtil.copyProperties(area, vo);
        return vo;
    }

    private PositionVO convertToVO(PositionEntity position) {
        PositionVO vo = new PositionVO();
        BeanUtil.copyProperties(position, vo);
        return vo;
    }

    private RoleVO convertToVO(RoleEntity role) {
        RoleVO vo = new RoleVO();
        BeanUtil.copyProperties(role, vo);
        return vo;
    }

    private PersonVO convertToVO(PersonEntity person) {
        PersonVO vo = new PersonVO();
        BeanUtil.copyProperties(person, vo);
        return vo;
    }

    private PersonDetailVO convertToDetailVO(PersonEntity person) {
        return PersonDetailVO.builder()
            .personId(person.getPersonId())
            .personCode(person.getPersonCode())
            .personName(person.getPersonName())
            .personType(person.getPersonType())
            .personLevel(person.getPersonLevel())
            .employeeId(person.getEmployeeId())
            .jobNumber(person.getJobNumber())
            .gender(person.getGender())
            .birthDate(person.getBirthDate())
            .mobile(person.getMobile())
            .email(person.getEmail())
            .avatarUrl(person.getAvatarUrl())
            .workStatus(person.getWorkStatus())
            .hireDate(person.getHireDate())
            .leaveDate(person.getLeaveDate())
            .status(person.getStatus())
            .createTime(person.getCreateTime())
            .updateTime(person.getUpdateTime())
            .build();
    }

    private PersonProfileVO convertToProfileVO(PersonProfileEntity profile) {
        PersonProfileVO vo = PersonProfileVO.builder()
            .profileId(profile.getProfileId())
            .workExperience(profile.getWorkExperience())
            .skills(profile.getSkills())
            .certifications(profile.getCertifications())
            .socialProfiles(profile.getSocialProfiles())
            .workHabits(profile.getWorkHabits())
            .learningGoals(profile.getLearningGoals())
            .careerGoals(profile.getCareerGoals())
            .build();
    }
}
```

### 核心管理层 (PersonManager)

```java
@Component
public class PersonManager {

    @Resource
    private PersonDao personDao;
    @Resource
    private PersonConfigDao personConfigDao;
    @Resource
    RedisTemplate<String, Object> redisTemplate;

    // 缓存常量
    private static final String CACHE_PREFIX = "person:";
    private static final String PROFILE_CACHE_PREFIX = "person:profile:";
    private static final String AREA_PERSON_PREFIX = "area:person:";
    private static final Duration CACHE_EXPIRE = Duration.ofMinutes(10);

    @Cacheable(value = "person", key = "#personId")
    public PersonEntity getById(Long personId) {
        return personDao.selectById(personId);
    }

    @CacheEvict(value = "person", allEntries = true)
    public void add(PersonEntity person) {
        personDao.insert(person);

        // 清除相关缓存
        clearPersonCache(person.getPersonId());
    }

    @CacheEvict(value = "person", allEntries = true)
    public void update(PersonEntity person) {
        // 乐观锁更新
        QueryWrapper<PersonEntity> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("person_id", person.getPersonId())
                   .eq("version", person.getVersion());

        int updateCount = personDao.update(person, updateWrapper);
        if (updateCount == 0) {
            throw new SmartException("人员信息已变更，请刷新后重试");
        }

        // 更新缓存
        cachePerson(person);
    }

    @CacheEvict(value = "person", allEntries = true)
    public void softDelete(Long personId) {
        PersonEntity updateEntity = new PersonEntity();
        updateEntity.setPersonId(personId);
        updateEntity.setDeletedFlag(1);
        updateEntity.setUpdateTime(LocalDateTime.now());
        personDao.updateById(updateEntity);

        // 清除缓存
        clearPersonCache(personId);
    }

    public PageResult<PersonEntity> queryPage(PersonQueryDTO queryDTO) {
        QueryWrapper<PersonEntity> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(queryDTO.getPersonCode())) {
            queryWrapper.like("person_code", queryDTO.getPersonCode());
        }
        if (StringUtils.isNotBlank(queryDTO.getPersonName())) {
            queryWrapper.like("person_name", queryDTO.getPersonName());
        }
        if (queryDTO.getPersonType() != null) {
            queryWrapper.eq("person_type", queryDTO.getPersonType());
        }
        if (queryDTO.getDepartmentId() != null) {
            queryWrapper.eq("department_id", queryDTO.getDepartmentId());
        }
        if (queryDTO.getPositionId() != null) {
            queryWrapper.eq("position_id", queryDTO.getPositionId());
        }
        if (queryDTO.getWorkStatus() != null) {
            queryWrapper.eq("work_status", queryDTO.getWorkStatus());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }
        if (queryDTO.getGender() != null) {
            queryWrapper.eq("gender", queryDTO.getGender());
        }
        if (queryDTO.getWorkLocationId() != null) {
            queryWrapper.eq("work_location_id", queryDTO.getWorkLocationId());
        }
        if (queryDTO.getHireDateStart() != null) {
            queryWrapper.ge("hire_date", queryDTO.getHireDateStart());
        }
        if (queryDTO.getHireDateEnd() != null) {
            queryWrapper.le("hire_date", queryDTO.getHireDateEnd());
        }

        queryWrapper.eq("deleted_flag", 0)
                   .orderByDesc("create_time");

        Page<PersonEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<PersonEntity> result = personDao.selectPage(page, queryWrapper);

        return PageResult.<PersonEntity>builder()
            .records(result.getRecords())
            .total(result.getTotal())
            .pageNum(result.getCurrent())
            .pageSize(result.getSize())
            .build();
    }

    public boolean checkPersonCodeExists(String personCode) {
        return checkPersonCodeExists(personCode, null);
    }

    public boolean checkPersonCodeExists(String personCode, Long excludePersonId) {
        QueryWrapper<PersonEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("person_code", personCode)
                   .eq("deleted_flag", 0);

        if (excludePersonId != null) {
            queryWrapper.ne("person_id", excludePersonId);
        }

        return personDao.selectCount(queryWrapper) > 0;
    }

    public boolean checkMobileExists(String mobile) {
        return personDao.selectCount(
            new QueryWrapper<PersonEntity>()
                .eq("mobile", mobile)
                .eq("deleted_flag", 0)
        ) > 0;
    }

    public boolean checkEmailExists(String email) {
        return personDao.selectCount(
            new QueryWrapper<PersonEntity>()
                .eq("email", email)
                .eq("deleted_flag", 0)
        ) > 0;
    }

    public int countAll() {
        return personDao.selectCount(
            new QueryWrapper<PersonEntity>()
                .eq("deleted_flag", 0)
        );
    }

    public int countByStatus(Integer workStatus) {
        return personDao.selectCount(
            new QueryWrapper<PersonEntity>()
                .eq("work_status", workStatus)
                .eq("deleted_flag", 0)
        );
    }

    public int countByType(String personType) {
        return personDao.selectCount(
            new QueryWrapper<PersonEntity>()
                .eq("person_type", personType)
                .eq("deleted_flag", 0)
        );
    }

    // 缓存方法
    private void cachePerson(PersonEntity person) {
        String cacheKey = CACHE_PREFIX + person.getPersonId();
        redisTemplate.opsForValue().set(cacheKey, person, CACHE_EXPIRE);
    }

    private void clearPersonCache(Long personId) {
        String cacheKey = CACHE_PREFIX + personId;
        redisTemplate.delete(cacheKey);
    }

    private void clearProfileCache(Long personId) {
        String cacheKey = PROFILE_CACHE_PREFIX + personId;
        redisTemplate.delete(cacheKey);
    }

    private void clearAllPersonCache() {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

### 人员画像服务 (PersonProfileService)

```java
@Service
public class PersonProfileService {

    @Resource
    private PersonProfileDao profileDao;
    @Resource
    private PersonManager personManager;
    @Resource
    private PersonDataAnalysisManager dataAnalysisManager;
    @Resource
    PersonRecommendationEngine recommendationEngine;

    @Cacheable(value = "person:profile", key = "#personId")
    public PersonProfileEntity getById(Long personId) {
        return profileDao.selectOne(
            new QueryWrapper<PersonProfileEntity>()
                .eq("person_id", personId)
                .orderByDesc("update_time")
                .last("LIMIT 1")
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long personId, PersonProfileUpdateDTO updateDTO) {
        // 1. 验证人员存在性
        validatePersonExists(personId);

        // 2. 更新画像
        PersonProfileEntity profile = convertProfileUpdateToEntity(updateDTO);
        profile.setPersonId(personId);
        profile.setUpdateTime(LocalDateTime.now());

        profileDao.updateById(profile);

        // 3. 清除缓存
        personManager.clearProfileCache(personId);

        // 4. 发布画像更新事件
        publishProfileUpdateEvent(personId, updateDTO);
    }

    @Cacheable(value = "person:profile", key = "#personId")
    public PersonProfileVO getProfile(Long personId) {
        PersonProfileEntity profile = getById(personId);
        if (profile == null) {
            return null;
        }

        // 获取分析结果
        ProfileAnalysisResult analysis = dataAnalysisManager.analyzeProfile(personId, profile);

        // 组装画像VO
        PersonProfileVO profileVO = convertToProfileVO(profile);
        profileVO.setAnalysisResult(analysis);

        return profileVO;
    }

    @Async("profileExecutor")
    public CompletableFuture<Void> analyzeAndSaveProfile(Long personId) {
        return CompletableFuture.runAsync(() -> {
            try {
                PersonProfileEntity profile = getById(personId);
                if (profile != null) {
                    ProfileAnalysisResult analysis = dataAnalysisManager.analyzeProfile(personId, profile);

                    // 更新分析结果
                    if (profile.getAnalysisResult() != null) {
                        PersonProfileUpdateDTO updateDTO = new ProfileAnalysisResultToProfileUpdateDTO(
                            profile.getAnalysisResult());
                        updateProfile(personId, updateDTO);
                    }

                    // 发布分析事件
                    ProfileAnalysisEvent event = ProfileAnalysisEvent.builder()
                        .personId(personId)
                        .analysisResult(analysis)
                        .analysisTime(LocalDateTime.now())
                        .build();

                    eventPublisher.publishEvent(event);
                }
            } catch (Exception e) {
                    log.error("分析人员画像失败: {}", personId, e);
                }
            }
        });
    }

    @Scheduled(cron = "0 0 1 * * ? * ?") // 每天凌晨1点执行
    public void analyzeAllPersonProfiles() {
        List<Long> allPersonIds = personManager.getAllPersonIds();

        for (Long personId : allPersonIds) {
            analyzeAndSaveProfile(personId);
        }
    }

    // 私有方法
    private void validatePersonExists(Long personId) {
        if (personManager.getById(personId) == null) {
            throw new SmartException("人员不存在");
        }
    }

    private void publishProfileUpdateEvent(Long personId, PersonProfileUpdateDTO updateDTO) {
        ProfileUpdateEvent event = ProfileUpdateEvent.builder()
            .personId(personId)
            .updateData(updateDTO)
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEvent(event);
    }

    private ProfileAnalysisResult convertProfileAnalysisResultToProfileUpdateDTO(ProfileAnalysisResult analysis) {
        ProfileAnalysisResult.PersonBehavior behavior = analysis.getPersonBehavior();
        ProfileAnalysisResult.WorkPattern workPattern = analysis.getWorkPattern();
        ProfileAnalysisResult.SkillSkillAnalysis skillAnalysis = analysis.getKillSkillAnalysis();
        ProfileAnalysisResult.TimeAnalysis timeAnalysis = analysis.getTimeAnalysis();
        ProfileAnalysisResult.RelationshipAnalysis relationshipAnalysis = analysis.getRelationshipAnalysis();

        return ProfileAnalysisResult.PersonBehaviorBuilder()
            .workPattern(workPattern)
            .killSkillAnalysis(skillAnalysis)
            .timeAnalysis(timeAnalysis)
            .relationshipAnalysis(relationshipAnalysis)
            .build();
    }

    private PersonProfileVO convertToProfileVO(PersonProfileEntity profile) {
        PersonProfileVO vo = PersonProfileVO.builder()
            .profileId(profile.getProfileId())
            .workExperience(profile.getWorkExperience())
            .skills(profile.getSkills())
            .certifications(profile.getCertifications())
            .socialProfiles(profile.getSocialProfiles())
            .workHabits(profile.getWorkHabits())
            .learningGoals(profile.getLearningGoals())
            .careerGoals(profile.getCareerGoals())
            .profileVersion(profile.getProfileVersion())
            .lastAnalysisTime(profile.getLastAnalysisTime())
            .build();

        return vo;
    }
}
```

---

## 🎨 前端实现

### 人员状态管理 (usePersonStore)

```javascript
// /store/person.js
import { defineStore } from 'pinia'
import { personApi } from '/@/api/person'
import { SmartUser } from '/@/utils/auth'

export const usePersonStore = defineStore('person', {
  state: () => ({
    // 人员列表
    personList: [],
    // 人员树形结构
    personTree: [],
    // 当前用户信息
    currentUser: null,
    // 用户可访问的区域列表
    accessibleAreas: [],
    // 人员统计
    statistics: {
      totalCount: 0,
      activeCount: 0,
      typeStats: {},
      statusStats: {},
      genderStats: {},
      ageStats: {}
    },
    // 人员缓存
    personCache: new Map(),
    // 搜索结果
    searchResults: [],
    // 分页信息
    pagination: {
      current: 1,
      pageSize: 20,
      total: 0
    }
  }),

  getters: {
    // 获取人员完整信息
    getFullPerson: (state) => (personId) => {
      const person = state.personCache.get(personId)
      if (!person) return null

      return {
        ...person,
        profile: state.personProfile.get(personId),
        department: state.departmentCache.get(person.departmentId),
        position: state.positionCache.get(person.positionId),
        roles: state.roleCache.get(personId),
        areas: state.areaAccessCache.get(personId) || [],
        workStatusText: getWorkStatusText(person.workStatus),
        personTypeText: getPersonTypeText(person.personType),
        avatar: person.avatarUrl || getDefaultAvatar(person.personType)
      }
    },

    // 获取人员统计数据
    getPersonStatistics: (state) => {
      return {
        total: state.statistics.totalCount,
        active: state.statistics.activeCount,
        inactive: state.statistics.inactiveCount,
        typeStats: Object.fromEntries(state.statistics.typeStats),
        statusStats: Object.fromEntries(state.statistics.statusStats),
        genderStats: Object.fromEntries(state.statistics.genderStats),
        ageStats: Object.fromEntries(state.statistics.ageStats),
        statisticsTime: state.statistics.statisticsTime
      }
    },

    // 获取工作状态文本
    getWorkStatusText: (status) => {
      const statusMap = {
        1: '在职',
        2: '离职',
        3: '请假',
        4: '停职',
        5: '禁用'
      }
      return statusMap[status] || '未知'
    },

    // 获取人员类型文本
    getPersonTypeText: (type) => {
      const typeMap = {
        'EMPLOYEE': '员工',
        'INTERN': '实习生',
        'CONTRACTOR': '承包商',
        'VISITOR': '访客',
        'SECURITY': '安保人员',
        'VENDOR': '供应商',
        'PARTNER': '合作伙伴'
      }
      return typeMap[type] || '未知'
    },

    // 获取默认头像
    getDefaultAvatar: (personType) => {
      const avatarMap = {
        'EMPLOYEE': '/static/avatar/employee.png',
        'INTERN': '/static/intern.png',
        'CONTRACTOR': '/static/contractor.png',
        'VISITOR': '/static/visitor.png',
        'SECURITY': '/static/security.png'
      }
      return avatarMap[personType] || '/static/default-avatar.png'
    },

    // 搜索人员
    searchPersons: (keyword) => {
      if (!keyword.trim()) {
        searchResults.value = state.personList
        return
      }

      const filteredList = state.personList.filter(person =>
        person.personName.includes(keyword) ||
        person.employeeId?.includes(keyword) ||
        person.mobile?.includes(keyword) ||
        person.email?.includes(keyword)
      )

      searchResults.value = filteredList
        .sort((a, b) => {
          const nameA = a.personName.toLowerCase()
          const nameB = b.personName.toLowerCase()
          return nameA.localeCompare(nameB)
        })

      return searchResults.value
    },

    // 清空搜索结果
    clearSearchResults: () => {
      searchResults.value = []
    }
  }),

  actions: {
    // 初始化用户信息
    async initCurrentUser() {
      try {
        const userInfo = SmartUser.getUserInfo()
        if (userInfo) {
          this.currentUser = {
            userId: userInfo.getUserId(),
            userName: userInfo.getUserName(),
            personId: userInfo.getPersonId(),
            userLevel: userInfo.getUserLevel(),
            permissions: userInfo.getPermissions()
          }

          // 获取人员详细信息
          await this.fetchPersonDetail(userInfo.getPersonId())
        }
      } catch (error) {
          console.error('初始化用户信息失败:', error)
          console.error('获取用户信息失败:', error)
        }
    },

    // 获取人员详情
    async fetchPersonDetail(personId) {
      try {
        const result = await personApi.getDetail(personId)
        this.selectedArea = result.data
      } catch (error) {
        console.error('获取人员详情失败:', error)
        throw error
      }
    },

    // 获取人员列表
    async function fetchPersonList(params = {}) {
      try {
        const result = await personApi.queryPage({
          pageNum: params.pageNum || 1,
          pageSize: params.pageSize || 20,
          ...params
        })

        this.personList = result.data.records
        this.pagination.current = result.data.pageNum
        this.pagination.total = result.data.total

        return result.data
      } catch (error) {
        console.error('获取人员列表失败:', error)
        throw error
      }
    },

    // 创建人员
    async createPerson(personData) {
      try {
        const result = await personApi.add(personData)
        await this.fetchPersonList()
        return result.data
      } catch (error) {
        console.error('创建人员失败:', error)
        throw error
      }
    },

    // 更新人员
    async updatePerson(personId, personData) {
      try {
        const result = await personApi.update(personId, personData)
        await this.fetchPersonList()
        await this.fetchPersonDetail(personId)
        return result.data
      } catch (error) {
        console.error('更新人员失败:', error)
        throw error
      }
    },

    // 删除人员
    async deletePerson(personId) {
      try {
        const result = personApi.delete(personId)
        await this.fetchPersonList()
        return result.data
      } catch (error) {
        console.error('删除人员失败:', error)
        throw error
      }
    },

    // 搜索人员
    async function searchPersons(keyword) {
      await this.fetchPersonList({ personName: keyword })
      return this.searchResults
    },

    // 绑止搜索
    stopSearching() {
      this.clearSearchResults()
    }
  }
})
```

### 人员画像组件 (PersonProfile)

```vue
<template>
  <div class="person-profile">
    <!-- 基本信息 -->
    <a-descriptions :column="1" :label="基本信息" size="small" bordered>
      <a-descriptions-item label="姓名">{{ personDetail.personName }}</a-descriptions-item>
      <a-descriptions-item label="工号">{{ personDetail.employeeId }}</a-descriptions-item>
      <a-descriptions-item label="邮箱">{{ personDetail.email }}</a-descriptions-item>
      <a-descriptions-item label="手机">{{ personDetail.mobile }}</a-descriptions-item>
      <a-descriptions-item label="性别">{{ getGenderText(personDetail.gender) }}</a-descriptions-item>
      <a-descriptions-item label="入职时间">
        {{ formatDate(personDetail.hireDate) }}
      </a-descriptions-item>
      <a-descriptions-item label="部门">
        {{ personDetail.departmentName || '未分配' }}
      </a-descriptions-item>
      <a-descriptions-item label="岗位">
        {{ personDetail.positionName || '未分配' }}
      </a-descriptions-item>
      <a-descriptions-item label="工作状态">
        <a-tag :color="getWorkStatusColor(personDetail.workStatus)">
          {{ getWorkStatusText(personDetail.workStatus) }}
        </a-tag>
      </a-descriptions-item>
    </a-descriptions>

    <!-- 人员画像信息 -->
    <a-row :gutter="16" v-if="personDetail.profile">
      <a-col :span="8">
        <a-statistic
          title="工作技能"
          :value="getSkillCount(personDetail.profile?.skills?.length || 0)"
        />
      </a-col>
      <a-col :span="8">
        <a-statistic
          title="认证证书"
          :value="getCertCount(personDetail.profile?.certifications?.length || 0)"
        />
      </a-col>
      <a-col :span="8">
        <a-statistic
          title="社交平台"
          :value="getSocialCount(personDetail.profile?.socialProfiles?.length || 0)"
        />
      </a-col>
    </a-row>

    <!-- 教育背景 -->
    <a-row :gutter="16" v-if="personDetail.profile">
      <a-col :span="8">
        <a-statistic
          title="教育程度"
          :value="getEducationLevel(personDetail.profile?.educationLevel || '未知')"/>
        />
      </a-col>
      <a-col :span="8">
        <a-statistic
          title="专业"
          :value="personDetail.profile?.major || '无专业'"/>
        />
      </a-col>
      <a-col :span="8">
        <a-statistic
          title="工作经验"
          :value="getWorkExperienceYears(personDetail.profile?.workExperience?.length || 0) || 0}}年"
        />
      </a-col>
    </a-row>

    <!-- 职业路径 -->
    <a-row :gutter="16" v-if="personDetail.profile?.careerGoals?.goals?.length || 0">
      <a-col :span="24">
        <h4>职业发展目标</h4>
        <a-timeline>
          <a-timeline-item
            v-for="(goal, index) in personDetail.profile?.careerGoals || []"
            :key="index"
            :color="getCareerGoalColor(goal.type)"
          >
            <template #dot>
              <component :is="getCareerGoalIcon(goal.type)" />
            </template>
            <template #content>
              <h5>{{ goal.title }}</h5>
              <p>{{ goal.description }}</p>
              <small>{{ formatDate(goal.deadline) }}</small>
            </template>
          </a-timeline-item>
        </a-timeline>
      </a-col>
    </a-row>

    <!-- 技能标签 -->
    <a-row :gutter="8" v-if="personDetail.profile?.skills?.length > 0">
      <div class="skill-tags">
        <a-tag
          v-for="(skill, index) in personDetail.profile.skills"
          :key="index"
          :color="getSkillColor(skill.level)"
        >
          {{ skill.name }}
        </a-tag>
      </div>
    </a-row>

    <!-- 社交平台 -->
    <a-row :gutter="8" v-if="personDetail.profile?.socialProfiles?.length > 0">
      <div class="social-links">
        <a
          v-for="(social, index) in personDetail.profile?.socialProfiles || []"
          :key="index"
          :href="social.url"
          target="_blank"
          class="social-link"
        >
          <component :is="getSocialIcon(social.platform)" />
          {{ social.platform }}
        </a>
      </div>
    </a-row>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { usePersonStore } from '/@/store/person'
import { SmartUser } from '/@/utils/auth'
import { formatDate } from '/@/utils/format'
import {
  FileOutlined,
  TeamOutlined,
  UserOutlined,
  CalendarOutlined,
  EnvironmentOutlined
} from '@ant-design-vue/es'

const props = defineProps({
  personId: {
    type: Number,
    required: true
  }
})

const personStore = usePersonStore()

const selectedArea = ref(null)
const detailVisible = ref(false)

// 计算属性
const personDetail = computed(() => {
  return personStore.getFullPerson(props.personId)
})

const skillCount = computed(() => {
  return personDetail.profile?.skills?.length || 0
})

const certCount = computed(() => {
  return personDetail.profile?.certifications?.length || 0
})

const socialCount = computed(() => {
  return personDetail.profile?.socialProfiles?.length || 0
})

const educationLevel = computed(() => {
  return personDetail.profile?.educationLevel || '未知'
})

const workExperienceYears = computed(() => {
  const exp = personDetail.profile?.workExperience || []
  return exp.length > 0 ? exp.reduce((sum, exp) / exp.length) : 0
})

const workStatusText = (status) => {
  const statusMap = {
    1: '在职',
    2: '离职',
    3: '请假',
    4: '停职',
    5: '禁用'
  }
  return statusMap[status] || '未知'
})

const getDefaultAvatar = (personType) => {
  const avatarMap = {
    'EMPLOYEE': '/static/employee.png',
    'INTERN': '/static/intern.png',
    'CONTRACTOR': '/static/contractor.png',
    'VISITOR': '/static/visitor.png',
    'SECURITY': '/static/security.png',
    'VENDOR': '/static/vendor.png'
  }
  return avatarMap[personType] || '/static/default-avatar.png'
}

const getSkillColor = (level) => {
  const colorMap = {
    'BEGINNER': 'green',
    'INTERMEDIATE': 'blue',
    'ADVANCED': 'purple',
    'EXPERT': 'red'
  }
  return colorMap[level] || 'default'
}

const getCareerGoalColor = (type) => {
  const colorMap = {
    'PROMOTION': '#1890ff',
    'MANAGEMENT': '#52c41a',
    'TECHNICAL': '#722edf3',
    'LEADERSHIP': '#13c2c2'
  }
  return colorMap[type] || '#1890ff'
}

const getSocialIcon = (platform) => {
  const iconMap = {
    'linkedin': 'linkedin',
    'github': 'github',
    'wechat': 'wechat',
    'weixin-mini': 'wechat-mini',
    'email': 'envelope',
    'phone': 'phone',
    'global': 'global'
  }
  return iconMap[platform] || 'global'
}

// 监听生命周期事件
onMounted(() => {
  personStore.initCurrentUser()
})

// 生命周期方法
const handleDeletePerson = (personId) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除人员"${personStore.getFullPerson(personId)?.personName}"吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      personStore.deletePerson(personId)
      message.success('删除成功')
    },
    onCancel: () => {
      // 取消操作
    }
  })
}

// 刷新数据
const handleRefresh = () => {
  personStore.fetchPersonList()
  personStore.fetchCurrentUser()
}
</script>

<style lang="less" scoped>
.person-profile {
  .profile-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    border-radius: 8px;
  }

  .profile-content {
    .profile-info {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 16px;
    }

    .profile-basic {
      .profile-photo {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        border: 2px solid #e8e8e8;
        object-fit: cover;
      }

      .profile-info-text {
        flex: 1;
      }
    }

    .profile-actions {
      grid-column: span: 2;
    }
  }

    .profile-social {
      grid-column: span: 1;
    }
  }

    .profile-badges {
      grid-column: span: 3;
    }

    .profile-tags {
      grid-column: span: 6;
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }
  }

  .profile-experience {
    grid-column: span: 12;
      margin-top: 16px;
    }

    .profile-education {
      grid-column: span: 6;
      margin-top: 8px;
    }

    .profile-career {
      grid-column: span: 6;
      margin-top: 8px;
    }
  }
}

.skill-tags,
.social-links {
  margin-top: 8px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.skill-tag {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.social-link {
  padding: 4px 8px;
  border-radius: 4px;
  background: #f5f5f5;
  color: #1890ff;
  text-decoration: none;
  transition: all 0.3s;
}

.social-link:hover {
  background: #e6f7ff;
  color: #1890ff;
  text-decoration: none;
}

.person-profile {
  transition: all 0.3s;
}

.person-profile:hover {
  box-shadow: 0 4px 12px 16px rgba(0, 0, 0.15);
}
</style>
```

---

## 📋 检查清单

### 开发前检查

- [ ] 是否已明确人员类型分类需求？
- [ ] 是否已确认组织架构设计？
- [ ] 是否已了解人员区域归属需求？
- [ ] 是否已设计人员画像数据结构？
- [ ] 是否已确认权限继承机制？

### 开发中检查

- [ ] 是否正确使用人员类型处理器？
- [ ] 是否实现了完整的生命周期管理？
- [ ] 是否添加了人员事件监听？
- [ ] 是否实现了人员画像分析？
- [ ] 是否进行了人员数据脱敏？

### 部署前检查

- [ ] 人员数据是否安全？
- [ ] 人员权限是否正确配置？
- [ ] 画像分析是否正常运行？
- [ | 缓存策略是否合理？
- [ ] 监控是否配置完善？

---

**📞 技术支持**：架构师团队

**📚 相关文档**：
- [设备管理公共模块](./smart-device.md)
- [权限管理公共模块](./smart-permission.md)
- [审批流程公共模块](./smart-workflow.md)
- [数据分析模块](./smart-analytics.md)
- [综合开发规范文档](../DEV_STANDARDS.md)

---

*最后更新：2025-01-10*
*维护者：SmartAdmin开发团队*