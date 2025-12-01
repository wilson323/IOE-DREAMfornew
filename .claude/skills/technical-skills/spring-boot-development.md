# Spring Boot 企业级开发技能

> **版本**: v1.0.0
> **更新时间**: 2025-11-16
> **分类**: 技术开发技能 > 核心技术
> **标签**: ["Spring Boot", "Java", "企业级", "四层架构", "编码规范"]
> **技能等级**: ★★★ 高级
> **适用角色**: 后端开发工程师、系统架构师、技术负责人

---

## 📋 技能概述

本技能规范定义了IOE-DREAM项目中Spring Boot企业级应用开发的标准要求，包括技术架构、编码规范、性能优化等核心能力。

## ⚠️ 核心约束（不可违反）

### 🚫 绝对禁止
```markdown
❌ 禁止使用 @Autowired 注入，必须使用 @Resource
❌ 禁止使用 javax.* 包名，必须使用 jakarta.*
❌ 禁止在Controller中编写业务逻辑
❌ 禁止跨层直接访问（如Controller直接访问Dao）
❌ 禁止使用System.out.println()，必须使用日志框架
❌ 禁止硬编码字符串，必须定义为常量
❌ 禁止忽略异常处理
❌ 禁止使用 @NotEmpty、@Size 等非常用验证注解
❌ 禁止代码复杂度超过10
❌ 禁止重复代码率超过3%
```

### ✅ 必须执行
```markdown
✅ 必须使用 @Resource 注入依赖
✅ 必须为每个公共方法编写注释
✅ 必须处理所有异常情况
✅ 必须使用日志框架记录重要操作
✅ 必须遵循统一的命名规范
✅ 必须保持代码复杂度 ≤ 10
✅ 必须为公共方法编写单元测试
✅ 必须使用 @NotBlank + @NotNull 进行必填字段验证
✅ 必须遵循四层架构规范
✅ 必须代码覆盖率 ≥ 80%
```

## 📚 知识要求

### 📖 理论知识
- **Spring Boot架构**: 深入理解Spring Boot 3.x的自动配置、启动机制、依赖管理
- **Jakarta EE规范**: 掌握Java EE 9+ (Jakarta EE) 规范，包括Servlet、JPA、Bean Validation
- **微服务架构**: 理解微服务设计原则、服务发现、配置管理、负载均衡
- **设计模式**: 熟练应用工厂模式、单例模式、策略模式、模板方法模式

### 💼 业务理解
- **四层架构**: 深入理解Controller-Service-Manager-DAO四层架构设计
- **业务领域建模**: 掌握领域驱动设计(DDD)的基本概念和实践
- **数据一致性**: 理解分布式事务、数据一致性、幂等性设计
- **系统安全**: 了解企业级应用的安全架构和最佳实践

### 🔧 技术背景
- **Java 17**: 掌握Java 17的新特性，包括记录类、密封类、模式匹配等
- **Spring生态**: 熟练使用Spring Security、Spring Data JPA、Spring AMQP等
- **数据库**: 精通MySQL、Redis、消息队列等数据存储技术
- **开发工具**: 熟练使用Maven、Git、IDEA、Docker等开发工具

## 🛠️ 核心技能实现

### 🏗️ 四层架构标准实现

#### Controller层规范
```java
@RestController
@RequestMapping("/api/employee")
@Tag(name = "员工管理", description = "员工的增删改查操作")
@SaCheckLogin
@Slf4j
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @Operation(summary = "查询员工列表")
    @PostMapping("/query")
    @SaCheckPermission("employee:query")
    public ResponseDTO<PageResult<EmployeeVO>> queryEmployees(@RequestBody @Valid EmployeeQueryForm queryForm) {
        log.info("查询员工列表: {}", queryForm);

        PageResult<EmployeeVO> result = employeeService.queryEmployees(queryForm);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增员工")
    @PostMapping("/add")
    @SaCheckPermission("employee:add")
    public ResponseDTO<Long> addEmployee(@RequestBody @Valid EmployeeAddForm addForm) {
        log.info("新增员工: {}", addForm);

        Long employeeId = employeeService.addEmployee(addForm, SmartLoginUtil.getCurrentUserId());
        return ResponseDTO.ok(employeeId);
    }

    @Operation(summary = "更新员工")
    @PutMapping("/update")
    @SaCheckPermission("employee:update")
    public ResponseDTO<Void> updateEmployee(@RequestBody @Valid EmployeeUpdateForm updateForm) {
        log.info("更新员工: {}", updateForm);

        employeeService.updateEmployee(updateForm, SmartLoginUtil.getCurrentUserId());
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除员工")
    @DeleteMapping("/delete/{employeeId}")
    @SaCheckPermission("employee:delete")
    public ResponseDTO<Void> deleteEmployee(@PathVariable @NotNull Long employeeId) {
        log.info("删除员工: employeeId={}", employeeId);

        employeeService.deleteEmployee(employeeId, SmartLoginUtil.getCurrentUserId());
        return ResponseDTO.ok();
    }
}
```

#### Service层规范
```java
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class EmployeeService {

    @Resource
    private EmployeeManager employeeManager;

    @Resource
    private EmployeeDao employeeDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询员工列表
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    public PageResult<EmployeeVO> queryEmployees(EmployeeQueryForm queryForm) {
        // 参数验证
        validateQueryForm(queryForm);

        // 业务逻辑处理
        return employeeManager.queryEmployees(queryForm);
    }

    /**
     * 新增员工
     *
     * @param addForm 新增表单
     * @param operatorId 操作人ID
     * @return 员工ID
     */
    public Long addEmployee(EmployeeAddForm addForm, Long operatorId) {
        // 业务规则验证
        validateEmployeeAddForm(addForm);

        // 检查工号唯一性
        if (employeeDao.existsByEmployeeCode(addForm.getEmployeeCode())) {
            throw new SmartException(UserErrorCode.PARAM_ERROR, "工号已存在");
        }

        // 调用Manager层处理复杂业务逻辑
        return employeeManager.addEmployee(addForm, operatorId);
    }

    /**
     * 更新员工信息
     *
     * @param updateForm 更新表单
     * @param operatorId 操作人ID
     */
    public void updateEmployee(EmployeeUpdateForm updateForm, Long operatorId) {
        // 验证员工是否存在
        EmployeeEntity employee = employeeDao.selectById(updateForm.getEmployeeId());
        if (employee == null) {
            throw new SmartException(UserErrorCode.DATA_NOT_EXIST, "员工不存在");
        }

        // 业务规则验证
        validateEmployeeUpdateForm(updateForm, employee);

        // 调用Manager层处理更新逻辑
        employeeManager.updateEmployee(updateForm, operatorId);
    }
}
```

#### Manager层规范
```java
@Component
@Slf4j
public class EmployeeManager {

    @Resource
    private EmployeeDao employeeDao;

    @Resource
    private DepartmentDao departmentDao;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 新增员工（复杂业务逻辑处理）
     *
     * @param addForm 新增表单
     * @param operatorId 操作人ID
     * @return 员工ID
     */
    @Transactional
    public Long addEmployee(EmployeeAddForm addForm, Long operatorId) {
        // 1. 验证部门是否存在
        DepartmentEntity department = departmentDao.selectById(addForm.getDepartmentId());
        if (department == null || department.getDeletedFlag() == 1) {
            throw new SmartException(UserErrorCode.PARAM_ERROR, "部门不存在");
        }

        // 2. 创建员工实体
        EmployeeEntity employee = EmployeeEntity.builder()
                .employeeId(SnowflakeIdUtil.nextId())
                .employeeCode(addForm.getEmployeeCode())
                .employeeName(addForm.getEmployeeName())
                .departmentId(addForm.getDepartmentId())
                .positionId(addForm.getPositionId())
                .mobilePhone(addForm.getMobilePhone())
                .email(addForm.getEmail())
                .gender(addForm.getGender())
                .employmentStatus(EmploymentStatusEnum.ONBOARD)
                .createUserId(operatorId)
                .build();

        // 3. 保存到数据库
        employeeDao.insert(employee);

        // 4. 发送领域事件
        EmployeeCreatedEvent event = EmployeeCreatedEvent.builder()
                .employeeId(employee.getEmployeeId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employee.getEmployeeName())
                .departmentId(employee.getDepartmentId())
                .operatorId(operatorId)
                .timestamp(new Date())
                .build();

        rabbitTemplate.convertAndSend("employee.exchange", "employee.created", event);

        // 5. 更新缓存
        updateEmployeeCache(employee);

        log.info("员工新增成功: employeeId={}, employeeCode={}",
                employee.getEmployeeId(), employee.getEmployeeCode());

        return employee.getEmployeeId();
    }

    /**
     * 查询员工列表（复杂查询和分页处理）
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    public PageResult<EmployeeVO> queryEmployees(EmployeeQueryForm queryForm) {
        // 构建查询条件
        EmployeeQuery query = EmployeeQuery.builder()
                .keyword(queryForm.getKeyword())
                .departmentId(queryForm.getDepartmentId())
                .positionId(queryForm.getPositionId())
                .employmentStatus(queryForm.getEmploymentStatus())
                .build();

        // 分页查询
        PageResult<EmployeeEntity> pageResult = employeeDao.selectPage(queryForm, query);

        // 转换为VO
        List<EmployeeVO> voList = pageResult.getRows().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

    /**
     * 转换为VO
     *
     * @param employee 员工实体
     * @return 员工VO
     */
    private EmployeeVO convertToVO(EmployeeEntity employee) {
        return EmployeeVO.builder()
                .employeeId(employee.getEmployeeId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employee.getEmployeeName())
                .departmentId(employee.getDepartmentId())
                .positionId(employee.getPositionId())
                .mobilePhone(employee.getMobilePhone())
                .email(employee.getEmail())
                .gender(employee.getGender())
                .employmentStatus(employee.getEmploymentStatus())
                .createTime(employee.getCreateTime())
                .updateTime(employee.getUpdateTime())
                .build();
    }
}
```

#### DAO层规范
```java
@Mapper
public interface EmployeeDao extends BaseMapper<EmployeeEntity> {

    /**
     * 分页查询员工
     *
     * @param queryForm 查询表单
     * @param query   查询条件
     * @return 分页结果
     */
    PageResult<EmployeeEntity> selectPage(@Param("queryForm") PageForm queryForm,
                                        @Param("query") EmployeeQuery query);

    /**
     * 根据工号查询员工
     *
     * @param employeeCode 员工编码
     * @return 员工实体
     */
    EmployeeEntity selectByEmployeeCode(@Param("employeeCode") String employeeCode);

    /**
     * 检查工号是否存在
     *
     * @param employeeCode 员工编码
     * @return 是否存在
     */
    boolean existsByEmployeeCode(@Param("employeeCode") String employeeCode);

    /**
     * 批量更新员工状态
     *
     * @param employeeIds    员工ID列表
     * @param employmentStatus 就职状态
     * @param updateUserId   更新人ID
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("employeeIds") List<Long> employeeIds,
                           @Param("employmentStatus") Integer employmentStatus,
                           @Param("updateUserId") Long updateUserId);
}
```

### 🔒 安全认证实现

#### Sa-Token权限控制
```java
/**
 * 员工权限常量
 */
public class EmployeePermissionConst {
    /** 查看权限 */
    public static final String QUERY = "employee:query";

    /** 新增权限 */
    public static final String ADD = "employee:add";

    /** 更新权限 */
    public static final String UPDATE = "employee:update";

    /** 删除权限 */
    public static final String DELETE = "employee:delete";

    /** 导出权限 */
    public static final String EXPORT = "employee:export";

    /** 批量操作权限 */
    public static final String BATCH = "employee:batch";
}

// 权限注解使用示例
@SaCheckPermission(EmployeePermissionConst.QUERY)
```

#### 接口加解密
```java
@RestController
@RequestMapping("/api/employee/secure")
@ApiDecrypt
@ApiEncrypt
public class EmployeeSecureController {

    @PostMapping("/sensitive-data")
    @SaCheckPermission("employee:sensitive")
    public ResponseDTO<String> handleSensitiveData(@RequestBody @Valid SensitiveDataForm form) {
        // 自动解密处理
        return ResponseDTO.ok("敏感数据处理成功");
    }
}
```

### 📊 性能优化实现

#### 缓存策略
```java
@Service
@Slf4j
public class EmployeeCacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "employee:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    /**
     * 获取员工缓存
     *
     * @param employeeId 员工ID
     * @return 员工VO
     */
    public EmployeeVO getEmployeeCache(Long employeeId) {
        String cacheKey = CACHE_PREFIX + employeeId;

        try {
            return (EmployeeVO) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.error("获取员工缓存失败: employeeId={}", employeeId, e);
            return null;
        }
    }

    /**
     * 设置员工缓存
     *
     * @param employeeId 员工ID
     * @param employeeVO 员工VO
     */
    public void setEmployeeCache(Long employeeId, EmployeeVO employeeVO) {
        String cacheKey = CACHE_PREFIX + employeeId;

        try {
            redisTemplate.opsForValue().set(cacheKey, employeeVO,
                    CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("设置员工缓存失败: employeeId={}", employeeId, e);
        }
    }

    /**
     * 删除员工缓存
     *
     * @param employeeId 员工ID
     */
    public void deleteEmployeeCache(Long employeeId) {
        String cacheKey = CACHE_PREFIX + employeeId;

        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.error("删除员工缓存失败: employeeId={}", employeeId, e);
        }
    }
}
```

#### 数据库优化
```java
// 实体类定义（遵循项目规范）
@Entity
@Table(name = "t_employee")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity extends BaseEntity {

    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_code", nullable = false, unique = true, length = 32)
    private String employeeCode;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @Column(name = "gender", nullable = false)
    private Integer gender;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "employment_status", nullable = false)
    private Integer employmentStatus;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "leave_date")
    private LocalDate leaveDate;

    @Column(name = "remark", length = 500)
    private String remark;
}

// 数据库索引优化SQL
/*
-- 员工表索引优化
CREATE INDEX idx_employee_code ON t_employee(employee_code);
CREATE INDEX idx_employee_name ON t_employee(employee_name);
CREATE INDEX idx_department_id ON t_employee(department_id);
CREATE INDEX idx_position_id ON t_employee(position_id);
CREATE INDEX idx_employment_status ON t_employee(employment_status);
CREATE INDEX idx_create_time ON t_employee(create_time);

-- 复合索引
CREATE INDEX idx_dept_status ON t_employee(department_id, employment_status);
CREATE INDEX idx_name_mobile ON t_employee(employee_name, mobile_phone);
*/
```

## 📊 质量标准

### ⏱️ 开发效率要求
- **CRUD接口开发**: 单个实体CRUD 2小时内完成
- **复杂业务接口**: 单个复杂接口1天内完成
- **单元测试编写**: 代码测试比达到1:1
- **Bug修复**: 一般Bug 30分钟内修复

### 🎯 代码质量要求
- **代码规范**: 100%符合项目编码规范
- **圈复杂度**: 单个方法圈复杂度不超过10
- **代码重复率**: 重复代码率不超过3%
- **测试覆盖率**: 单元测试覆盖率不低于80%

### 🔍 系统性能要求
- **响应时间**: 95%的请求响应时间 < 500ms
- **吞吐量**: 支持至少 1000 TPS
- **资源利用率**: CPU利用率 < 80%，内存利用率 < 85%
- **可用性**: 系统可用性达到99.9%

## 📋 开发检查清单

### ✅ 开发前检查
- [ ] 是否已阅读相关业务文档和规范
- [ ] 是否理解技术架构要求
- [ ] 是否确认数据库设计方案
- [ ] 是否准备好开发环境

### ✅ 编码中检查
- [ ] 是否使用 @Resource 依赖注入
- [ ] 是否使用 jakarta.* 包名
- [ ] 是否遵循四层架构规范
- [ ] 是否使用@Slf4j 日志框架
- [ ] 是否处理所有异常情况
- [ ] 是否使用 @Valid 参数验证
- [ ] 是否添加 @SaCheckPermission 权限注解

### ✅ 开发后检查
- [ ] 代码是否能正常编译
- [ ] 单元测试是否全部通过
- [ ] 集成测试是否通过
- [ ] 是否更新相关文档
- [ ] 是否通过代码审查

## 🔗 相关技能

### 📚 相关技能
- **[Vue3开发技能](./vue3-development.md)** - 前端组件开发
- **[数据库设计技能](./database-design.md)** - 数据库设计和优化
- **[系统运维技能](./system-operations.md)** - 系统部署和运维
- **[API设计技能](./api-design.md)** - RESTful API设计

### 🚀 进阶路径
1. **架构师**: 负责系统架构设计和技术选型
2. **技术专家**: 深入研究特定技术领域
3. **团队负责人**: 带领开发团队完成项目
4. **开源贡献**: 参与开源项目贡献代码

### 📖 参考资料
- **[Java编码规范](../../../docs/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md)**
- **[四层架构详解](../../../docs/repowiki/zh/content/后端架构/四层架构详解.md)**
- **[系统安全规范](../../../docs/repowiki/zh/content/安全体系/)**
- **[通用开发检查清单](../../../docs/CHECKLISTS/通用开发检查清单.md)**

---

**✅ 技能认证完成标准**:
- 能够独立设计和实现Spring Boot企业级应用
- 熟练掌握四层架构设计和实现
- 能够进行性能优化和安全加固
- 具备系统设计和架构能力
- 通过技能评估测试（理论 + 实操 + 项目实战）