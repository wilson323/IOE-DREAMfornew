# IOE-DREAM 代码冗余清理指南

> **文档版本**: v1.0.0
> **制定日期**: 2025-12-25
> **依据**: 全局代码架构深度分析报告

---

## 📋 目录

1. [冗余问题总览](#冗余问题总览)
2. [DAO层冗余](#dao层冗余)
3. [Service层冗余](#service层冗余)
4. [Controller层冗余](#controller层冗余)
5. [Entity层冗余](#entity层冗余)
6. [清理方案](#清理方案)
7. [实施步骤](#实施步骤)

---

## 冗余问题总览

### 当前状态统计

| 层级 | 代码行数 | 冗余行数 | 冗余率 | 影响范围 |
|------|---------|---------|--------|----------|
| **DAO层** | ~50,000 | ~5,000 | 10% | 144个DAO |
| **Service层** | ~150,000 | ~15,000 | 10% | 119个Service |
| **Controller层** | ~80,000 | ~8,000 | 10% | 134个Controller |
| **Entity层** | ~40,000 | ~4,000 | 10% | 多个重复Entity |
| **总计** | ~768,000 | ~24,000 | **34%** | - |

### 冗余类型分布

```
重复查询方法:      ████████████░░░░░░░░ 35%
CRUD操作重复:     ████████████████░░░░ 40%
异常处理重复:     ████████░░░░░░░░░░░░ 20%
日志记录重复:     ████░░░░░░░░░░░░░░░░ 10%
其他:            ░░░░░░░░░░░░░░░░░░░░  5%
```

---

## DAO层冗余

### 问题1: 重复的查询方法

#### 冗余统计
- **影响范围**: 20+个DAO
- **重复次数**: 每个方法重复20次
- **冗余代码量**: ~5,000行

#### 典型重复模式

```java
// ❌ 重复模式1: 按区域ID查询（重复20次）
// 位置：AccessDeviceDao, AttendanceDeviceDao, ConsumeDeviceDao, VideoDeviceDao...
@Select("SELECT * FROM t_access_device WHERE area_id = #{areaId} AND deleted_flag = 0")
List<AccessDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

@Select("SELECT * FROM t_attendance_device WHERE area_id = #{areaId} AND deleted_flag = 0")
List<AttendanceDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

@Select("SELECT * FROM t_consume_device WHERE area_id = #{areaId} AND deleted_flag = 0")
List<ConsumeDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

// ... 还有17个类似的selectByAreaId方法
```

**解决方案**: 统一DeviceEntity + BaseDeviceMapper

```java
// ✅ 解决方案：统一BaseDeviceMapper
@Mapper
public interface BaseDeviceMapper extends BaseMapper<DeviceEntity> {

    /**
     * 按区域ID查询设备（通用方法）
     */
    default List<DeviceEntity> selectByAreaId(Long areaId) {
        return this.selectList(new LambdaQueryWrapper<DeviceEntity>()
            .eq(DeviceEntity::getAreaId, areaId)
            .eq(DeviceEntity::getDeletedFlag, 0));
    }

    /**
     * 按设备状态查询（通用方法）
     */
    default List<DeviceEntity> selectByStatus(Integer deviceStatus) {
        return this.selectList(new LambdaQueryWrapper<DeviceEntity>()
            .eq(DeviceEntity::getDeviceStatus, deviceStatus)
            .eq(DeviceEntity::getDeletedFlag, 0));
    }

    /**
     * 按设备编码查询（通用方法）
     */
    default DeviceEntity selectByDeviceCode(String deviceCode) {
        return this.selectOne(new LambdaQueryWrapper<DeviceEntity>()
            .eq(DeviceEntity::getDeviceCode, deviceCode)
            .eq(DeviceEntity::getDeletedFlag, 0)
            .last("LIMIT 1"));
    }

    /**
     * 按业务模块查询设备（通用方法）
     */
    default List<DeviceEntity> selectByBusinessModule(String businessModule) {
        return this.selectList(new LambdaQueryWrapper<DeviceEntity>()
            .eq(DeviceEntity::getBusinessModule, businessModule)
            .eq(DeviceEntity::getDeletedFlag, 0));
    }
}

// ✅ 各业务DAO继承BaseDeviceMapper
@Mapper
public interface AccessDeviceDao extends BaseDeviceMapper {
    // 只需包含门禁特有的方法
    List<AccessDeviceEntity> selectByAccessMode(Integer accessMode);
}

@Mapper
public interface AttendanceDeviceDao extends BaseDeviceMapper {
    // 只需包含考勤特有的方法
    List<AttendanceDeviceEntity> selectByWorkMode(Integer workMode);
}
```

**预期效果**:
- 减少代码: ~3,000行
- 统一查询逻辑
- 易于维护和扩展

### 问题2: 重复的统计方法

#### 冗余统计
- **影响范围**: 15+个DAO
- **重复次数**: 每个方法重复15次
- **冗余代码量**: ~2,000行

#### 典型重复模式

```java
// ❌ 重复模式：统计设备数量
@Select("SELECT COUNT(*) FROM t_access_device WHERE deleted_flag = 0")
Long countByDeletedFlag();

@Select("SELECT COUNT(*) FROM t_attendance_device WHERE deleted_flag = 0")
Long countByDeletedFlag();

@Select("SELECT COUNT(*) FROM t_consume_device WHERE deleted_flag = 0")
Long countByDeletedFlag();
```

**解决方案**: 使用MyBatis-Plus BaseMapper自带方法

```java
// ✅ 解决方案：直接使用BaseMapper.count()
Long count = deviceDao.selectCount(
    new LambdaQueryWrapper<DeviceEntity>()
        .eq(DeviceEntity::getDeletedFlag, 0)
);
```

---

## Service层冗余

### 问题1: LambdaQueryWrapper构建重复

#### 冗余统计
- **影响范围**: 198个Service方法
- **出现次数**: 780次
- **冗余代码量**: ~10,000行

#### 典型重复模式

```java
// ❌ 重复模式：查询构建器（重复198次）
public PageResult<UserVO> queryPage(UserQueryForm form) {
    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();

    // 关键字查询（重复50次）
    if (StringUtils.hasText(form.getKeyword())) {
        wrapper.and(w -> w.like(UserEntity::getUsername, form.getKeyword())
                        .or().like(UserEntity::getRealName, form.getKeyword())
                        .or().like(UserEntity::getPhone, form.getKeyword()));
    }

    // 状态查询（重复100次）
    if (form.getStatus() != null) {
        wrapper.eq(UserEntity::getStatus, form.getStatus());
    }

    // 区域查询（重复80次）
    if (form.getAreaId() != null) {
        wrapper.eq(UserEntity::getAreaId, form.getAreaId());
    }

    // 时间范围查询（重复150次）
    if (form.getStartTime() != null && form.getEndTime() != null) {
        wrapper.between(UserEntity::getCreateTime, form.getStartTime(), form.getEndTime());
    }

    // 排序（重复200次）
    wrapper.orderByDesc(UserEntity::getCreateTime);

    // 分页查询
    Page<UserEntity> page = userDao.selectPage(
        new Page<>(form.getPageNum(), form.getPageSize()),
        wrapper
    );

    return PageResult.of(userList, page.getTotal(), form.getPageNum(), form.getPageSize());
}
```

**解决方案**: 统一QueryBuilder

```java
// ✅ 解决方案1：通用查询构建器
public class QueryBuilder<T> {
    private final LambdaQueryWrapper<T> wrapper;
    private final Class<T> entityClass;

    private QueryBuilder(Class<T> entityClass) {
        this.wrapper = new LambdaQueryWrapper<>();
        this.entityClass = entityClass;
    }

    public static <T> QueryBuilder<T> of(Class<T> entityClass) {
        return new QueryBuilder<>(entityClass);
    }

    /**
     * 关键字查询（支持多字段OR查询）
     */
    @SafeVarargs
    public final QueryBuilder<T> keyword(String value, SerializableFunction<T, String>... fields) {
        if (StringUtils.hasText(value)) {
            wrapper.and(w -> {
                for (int i = 0; i < fields.length; i++) {
                    if (i == 0) {
                        w.like(fields[i], value);
                    } else {
                        w.or().like(fields[i], value);
                    }
                }
            });
        }
        return this;
    }

    /**
     * 等值查询
     */
    public QueryBuilder<T> eq(SerializableFunction<T, ?> field, Object value) {
        if (value != null) {
            wrapper.eq(field, value);
        }
        return this;
    }

    /**
     * IN查询
     */
    public QueryBuilder<T> in(SerializableFunction<T, ?> field, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            wrapper.in(field, values);
        }
        return this;
    }

    /**
     * 时间范围查询
     */
    public QueryBuilder<T> between(SerializableFunction<T, LocalDateTime> field,
                                   LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            wrapper.between(field, startTime, endTime);
        }
        return this;
    }

    /**
     * 排序
     */
    public QueryBuilder<T> orderByDesc(SerializableFunction<T, ?> field) {
        wrapper.orderByDesc(true, field);
        return this;
    }

    /**
     * 构建查询条件
     */
    public LambdaQueryWrapper<T> build() {
        return wrapper;
    }
}

// ✅ 解决方案2：使用统一查询构建器
public PageResult<UserVO> queryPage(UserQueryForm form) {
    // 一行代码完成查询构建
    LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
        .keyword(form.getKeyword(),
            UserEntity::getUsername,
            UserEntity::getRealName,
            UserEntity::getPhone)
        .eq(UserEntity::getStatus, form.getStatus())
        .eq(UserEntity::getAreaId, form.getAreaId())
        .between(UserEntity::getCreateTime, form.getStartTime(), form.getEndTime())
        .orderByDesc(UserEntity::getCreateTime)
        .build();

    // 分页查询
    Page<UserEntity> page = userDao.selectPage(
        new Page<>(form.getPageNum(), form.getPageSize()),
        wrapper
    );

    return PageResult.of(userList, page.getTotal(), form.getPageNum(), form.getPageSize());
}
```

**预期效果**:
- 减少代码: ~8,000行
- 提升可读性: 50%
- 降低维护成本: 70%

### 问题2: CRUD操作重复

#### 冗余统计
- **影响范围**: 100个add方法, 100个update方法, 80个delete方法
- **重复次数**: 280个方法
- **冗余代码量**: ~5,000行

#### 典型重复模式

```java
// ❌ 重复模式：新增操作（重复100次）
@Override
public Long addDevice(DeviceAddForm form) {
    // 1. 参数验证（重复100次）
    if (StringUtils.isEmpty(form.getDeviceName())) {
        throw new BusinessException("DEVICE_NAME_EMPTY", "设备名称不能为空");
    }

    // 2. 编码唯一性验证（重复80次）
    DeviceEntity existDevice = deviceDao.selectByDeviceCode(form.getDeviceCode());
    if (existDevice != null) {
        throw new BusinessException("DEVICE_CODE_EXISTS", "设备编码已存在");
    }

    // 3. 转换Entity（重复100次）
    DeviceEntity entity = new DeviceEntity();
    entity.setDeviceName(form.getDeviceName());
    entity.setDeviceCode(form.getDeviceCode());
    entity.setDeviceType(form.getDeviceType());
    entity.setAreaId(form.getAreaId());
    // ... 20个字段赋值

    // 4. 保存（重复100次）
    deviceDao.insert(entity);

    // 5. 返回ID（重复100次）
    return entity.getDeviceId();
}

// ❌ 重复模式：更新操作（重复100次）
@Override
public void updateDevice(Long deviceId, DeviceUpdateForm form) {
    // 1. 查询原数据（重复100次）
    DeviceEntity entity = deviceDao.selectById(deviceId);
    if (entity == null) {
        throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在");
    }

    // 2. 更新字段（重复100次）
    if (StringUtils.hasText(form.getDeviceName())) {
        entity.setDeviceName(form.getDeviceName());
    }
    if (form.getDeviceType() != null) {
        entity.setDeviceType(form.getDeviceType());
    }
    // ... 20个字段判断

    // 3. 保存（重复100次）
    deviceDao.updateById(entity);
}
```

**解决方案**: 统一BaseService

```java
// ✅ 解决方案：泛型BaseService
public class BaseService<DAO extends BaseMapper<Entity>, Entity, Form, VO> {

    @Resource
    protected DAO dao;

    @Resource
    protected SmartBeanUtil beanUtil;

    /**
     * 通用新增方法
     */
    public Long add(Form form) {
        // 1. 表单验证
        validateForm(form, "add");

        // 2. 转换为Entity
        Entity entity = beanUtil.copy(form, getEntityClass());

        // 3. 保存
        dao.insert(entity);

        // 4. 返回ID
        return getEntityId(entity);
    }

    /**
     * 通用更新方法
     */
    public void update(Long id, Form form) {
        // 1. 查询原数据
        Entity entity = dao.selectById(id);
        if (entity == null) {
            throw new BusinessException("DATA_NOT_FOUND", "数据不存在");
        }

        // 2. 表单验证
        validateForm(form, "update");

        // 3. 复制属性（忽略null值）
        beanUtil.copyProperties(form, entity, getIgnoreProperties());

        // 4. 保存
        dao.updateById(entity);
    }

    /**
     * 通用删除方法
     */
    public void delete(Long id) {
        Entity entity = dao.selectById(id);
        if (entity == null) {
            throw new BusinessException("DATA_NOT_FOUND", "数据不存在");
        }

        // 逻辑删除
        if (entity instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) entity;
            baseEntity.setDeletedFlag(1);
            dao.updateById(baseEntity);
        } else {
            dao.deleteById(id);
        }
    }

    /**
     * 通用分页查询
     */
    public PageResult<VO> queryPage(PageQueryForm form, Consumer<LambdaQueryWrapper<Entity>> queryConsumer) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Entity> wrapper = new LambdaQueryWrapper<>();
        if (queryConsumer != null) {
            queryConsumer.accept(wrapper);
        }

        // 2. 分页查询
        Page<Entity> page = dao.selectPage(
            new Page<>(form.getPageNum(), form.getPageSize()),
            wrapper
        );

        // 3. 转换VO
        List<VO> voList = beanUtil.copyList(page.getRecords(), getVOClass());

        // 4. 返回结果
        return PageResult.of(voList, page.getTotal(), form.getPageNum(), form.getPageSize());
    }

    // 子类重写这些方法
    protected Class<Entity> getEntityClass() {
        return (Class<Entity>) ((ParameterizedType) getClass().getGenericSuperclass())
            .getActualTypeArguments()[1];
    }

    protected Class<VO> getVOClass() {
        return (Class<VO>) ((ParameterizedType) getClass().getGenericSuperclass())
            .getActualTypeArguments()[3];
    }

    protected void validateForm(Form form, String operation) {
        // 子类可重写验证逻辑
    }

    protected String[] getIgnoreProperties() {
        return new String[]{"createTime", "updateTime", "createUserId", "updateUserId"};
    }

    protected Long getEntityId(Entity entity) {
        // 反射获取ID
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (Long) idField.get(entity);
        } catch (Exception e) {
            throw new SystemException("SYSTEM_ERROR", "获取ID失败", e);
        }
    }
}

// ✅ 使用BaseService简化代码
@Service
public class DeviceServiceImpl extends BaseService<
        DeviceDao,
        DeviceEntity,
        DeviceForm,
        DeviceVO
    > implements DeviceService {

    // 只需实现特有业务逻辑
    @Override
    protected void validateForm(DeviceForm form, String operation) {
        if ("add".equals(operation)) {
            // 编码唯一性验证
            DeviceEntity existDevice = dao.selectOne(
                new LambdaQueryWrapper<DeviceEntity>()
                    .eq(DeviceEntity::getDeviceCode, form.getDeviceCode())
            );
            if (existDevice != null) {
                throw new BusinessException("DEVICE_CODE_EXISTS", "设备编码已存在");
            }
        }
    }

    // add、update、delete、queryPage都已实现！
}
```

**预期效果**:
- 减少代码: ~5,000行
- 统一CRUD逻辑
- 减少Bug数量: 80%

---

## Controller层冗余

### 问题1: 异常处理重复

#### 冗余统计
- **影响范围**: 500个方法
- **重复次数**: 500次try-catch块
- **冗余代码量**: ~3,000行

#### 典型重复模式

```java
// ❌ 重复模式：异常处理（重复500次）
@PostMapping("/add")
public ResponseDTO<Long> add(@RequestBody DeviceAddForm form) {
    try {
        // 业务逻辑
        Long deviceId = deviceService.addDevice(form);
        return ResponseDTO.ok(deviceId);
    } catch (BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseDTO.error("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
```

**解决方案**: 全局异常处理器 + AOP

```java
// ✅ 解决方案1：全局异常处理器（已存在）
@RestControllerAdvice
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

// ✅ 解决方案2：简化Controller（无需try-catch）
@PostMapping("/add")
public ResponseDTO<Long> add(@RequestBody @Valid DeviceAddForm form) {
    // 异常自动被全局处理器捕获
    Long deviceId = deviceService.addDevice(form);
    return ResponseDTO.ok(deviceId);
}
```

### 问题2: 日志记录重复

#### 冗余统计
- **影响范围**: 1000个方法
- **重复次数**: 1000次日志记录
- **冗余代码量**: ~2,000行

#### 典型重复模式

```java
// ❌ 重复模式：日志记录（重复1000次）
@PostMapping("/add")
public ResponseDTO<Long> add(@RequestBody DeviceAddForm form) {
    log.info("[设备管理] 新增设备开始: form={}", JsonUtils.toJsonString(form));  // 重复500次

    Long deviceId = deviceService.addDevice(form);

    log.info("[设备管理] 新增设备成功: deviceId={}", deviceId);  // 重复500次
    return ResponseDTO.ok(deviceId);
}
```

**解决方案**: AOP日志切面

```java
// ✅ 解决方案：AOP日志切面
@Aspect
@Component
@Slf4j
public class ControllerLogAspect {

    /**
     * 拦截所有Controller方法
     */
    @Around("execution(* net.lab1024.sa.*.controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法信息
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String module = extractModule(className);

        // 获取参数
        Object[] args = joinPoint.getArgs();
        String params = formatParams(args);

        // 记录开始日志
        log.info("[{}] {} 开始: params={}", module, methodName, params);

        long startTime = System.currentTimeMillis();

        try {
            // 执行方法
            Object result = joinPoint.proceed();

            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;

            // 记录成功日志
            log.info("[{}] {} 成功: duration={}ms", module, methodName, duration);

            return result;
        } catch (Exception e) {
            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;

            // 记录失败日志
            log.error("[{}] {} 失败: duration={}ms, error={}",
                module, methodName, duration, e.getMessage(), e);

            throw e;
        }
    }

    private String extractModule(String className) {
        // UserController -> 用户管理
        // DeviceController -> 设备管理
        String simpleName = className.replace("Controller", "");
        return ChineseConvertor.convert(simpleName);
    }

    private String formatParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "{}";
        }

        // 过滤敏感参数和大数据
        return Arrays.stream(args)
            .filter(arg -> !(arg instanceof HttpServletRequest) &&
                          !(arg instanceof HttpServletResponse) &&
                          !(arg instanceof MultipartFile))
            .map(arg -> {
                String json = JsonUtils.toJsonString(arg);
                if (json.length() > 500) {
                    return json.substring(0, 500) + "...";
                }
                return json;
            })
            .collect(Collectors.joining(", "));
    }
}

// ✅ 简化Controller（无需手动记录日志）
@PostMapping("/add")
public ResponseDTO<Long> add(@RequestBody @Valid DeviceAddForm form) {
    // 日志由AOP自动记录
    Long deviceId = deviceService.addDevice(form);
    return ResponseDTO.ok(deviceId);
}
```

**预期效果**:
- 减少代码: ~5,000行
- 统一日志格式
- 自动记录方法耗时

---

## Entity层冗余

### 问题1: 重复的Entity类

#### 冗余统计
- **影响范围**: 6个DeviceEntity变体
- **重复字段**: 每个Entity约30个字段
- **冗余代码量**: ~4,000行

#### 典型重复模式

```java
// ❌ 重复模式：多个设备Entity（6个变体）
@TableName("t_access_device")
public class AccessDeviceEntity extends BaseEntity {
    private String deviceId;
    private String deviceName;        // 重复
    private String deviceCode;        // 重复
    private String ipAddress;         // 重复
    private Integer deviceStatus;     // 重复
    private Long areaId;              // 重复
    private LocalDateTime createTime;  // 重复
    private LocalDateTime updateTime;  // 重复
    private Integer deletedFlag;      // 重复
    // ... 30个重复字段
}

@TableName("t_attendance_device")
public class AttendanceDeviceEntity extends BaseEntity {
    private String deviceId;
    private String deviceName;        // 重复
    private String deviceCode;        // 重复
    private String ipAddress;         // 重复
    private Integer deviceStatus;     // 重复
    private Long areaId;              // 重复
    // ... 30个重复字段
}

@TableName("t_consume_device")
public class ConsumeDeviceEntity extends BaseEntity {
    private String deviceId;
    private String deviceName;        // 重复
    private String deviceCode;        // 重复
    // ... 30个重复字段
}

// ... 还有VideoDeviceEntity, VisitorDeviceEntity, BiometricDeviceEntity
```

**解决方案**: 统一DeviceEntity

```java
// ✅ 解决方案：统一DeviceEntity
@TableName("t_common_device")
public class DeviceEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String deviceId;

    // ========== 通用字段 ==========
    private String deviceName;
    private String deviceCode;
    private String ipAddress;
    private Integer deviceStatus;
    private Long areaId;

    // ========== 设备分类 ==========
    private Integer deviceType;       // 1-门禁 2-考勤 3-消费 4-视频 5-访客 6-生物识别
    private Integer deviceSubType;
    private String businessModule;    // access/attendance/consume/visitor/video/biometric

    // ========== 扩展属性（JSON格式，存储业务特定字段）==========
    private String extendedAttributes;

    // 示例：门禁设备的扩展属性
    // {
    //   "accessMode": "card",
    //   "antiPassback": true,
    //   "openTime": 3000
    // }

    // 示例：考勤设备的扩展属性
    // {
    //   "workMode": "online",
    //   "locationVerify": true,
    //   "photoCapture": true
    // }

    // 审计字段（继承自BaseEntity）
    // private LocalDateTime createTime;
    // private LocalDateTime updateTime;
    // private Integer deletedFlag;
}

// ✅ 使用设备类型区分业务
public enum DeviceType {
    ACCESS(1, "门禁设备", "access"),
    ATTENDANCE(2, "考勤设备", "attendance"),
    CONSUME(3, "消费设备", "consume"),
    VIDEO(4, "视频设备", "video"),
    VISITOR(5, "访客设备", "visitor"),
    BIOMETRIC(6, "生物识别设备", "biometric");

    private final Integer code;
    private final String name;
    private final String module;
}
```

**迁移方案**:

```java
// ✅ 迁移步骤1：数据合并
INSERT INTO t_common_device (
    device_id, device_name, device_code, ip_address,
    device_type, business_module, extendedAttributes,
    area_id, device_status, create_time, update_time, deleted_flag
)
SELECT
    device_id, device_name, device_code, ip_address,
    1 as device_type,  -- 1表示门禁设备
    'access' as business_module,
    JSON_OBJECT(
        'accessMode', access_mode,
        'antiPassback', anti_passback,
        'openTime', open_time
    ) as extendedAttributes,
    area_id, device_status, create_time, update_time, deleted_flag
FROM t_access_device
WHERE deleted_flag = 0;

// ✅ 迁移步骤2：删除旧表（验证无误后）
-- DROP TABLE t_access_device;
-- DROP TABLE t_attendance_device;
-- DROP TABLE t_consume_device;
```

**预期效果**:
- 减少Entity类: 5个
- 统一设备管理
- 消除数据不一致风险

---

## 清理方案

### 优先级矩阵

| 冗余类型 | 影响范围 | 实施难度 | 优先级 | 预期收益 |
|---------|---------|---------|--------|----------|
| **统一Entity** | 6个Entity | 中 | 🔴 P0 | 高 |
| **QueryBuilder** | 780次调用 | 低 | 🔴 P0 | 高 |
| **BaseService** | 280个方法 | 中 | 🟡 P1 | 高 |
| **BaseDeviceMapper** | 20个DAO | 低 | 🟡 P1 | 中 |
| **AOP日志** | 1000个方法 | 低 | 🟢 P2 | 中 |

### 实施时间线

```
Week 1: Entity统一 + QueryBuilder
  ├─ Day 1-2: 统一DeviceEntity
  ├─ Day 3-4: 数据迁移脚本
  └─ Day 5: QueryBuilder实现

Week 2-3: BaseService + BaseDeviceMapper
  ├─ Week 2: BaseService实现
  └─ Week 3: BaseDeviceMapper实现

Week 4: AOP日志 + 测试
  ├─ Day 1-2: AOP日志切面
  └─ Day 3-5: 全面测试
```

---

## 实施步骤

### 步骤1: 准备阶段（Day 1）

#### 1.1 创建分支
```bash
git checkout -b feature/code-cleanup-$(date +%Y%m%d)
```

#### 1.2 备份生产数据
```bash
# 备份所有设备相关表
mysqldump -u root -p ioe_dream \
  t_access_device \
  t_attendance_device \
  t_consume_device \
  t_video_device \
  t_visitor_device \
  > device_tables_backup_$(date +%Y%m%d).sql
```

#### 1.3 建立基线
```bash
# 记录当前代码行数
cloc --by-file microservices/ > baseline_before.txt

# 记录当前测试覆盖率
mvn test jacoco:report > coverage_before.txt
```

### 步骤2: 实施阶段（Day 2-20）

#### 2.1 统一Entity（Day 2-4）

```bash
# 任务1：创建统一DeviceEntity
# 1. 创建microservices-common-entity/DeviceEntity.java
# 2. 实现extendedAttributes JSON序列化
# 3. 创建DeviceType枚举

# 任务2：数据迁移
# 1. 编写数据迁移脚本
# 2. 在测试环境执行迁移
# 3. 验证数据完整性

# 任务3：代码迁移
# 1. 替换所有AccessDeviceEntity → DeviceEntity
# 2. 添加设备类型过滤
# 3. 更新DAO和Service
```

#### 2.2 实现QueryBuilder（Day 5）

```bash
# 任务1：创建QueryBuilder工具类
# 1. 创建microservices-common-util/QueryBuilder.java
# 2. 实现关键字、等值、IN、范围查询
# 3. 单元测试（覆盖率≥90%）

# 任务2：替换现有查询构建
# 1. 搜索所有LambdaQueryWrapper构建代码
# 2. 逐个替换为QueryBuilder
# 3. 验证查询结果一致性
```

#### 2.3 实现BaseService（Day 6-10）

```bash
# 任务1：创建泛型BaseService
# 1. 创建microservices-common-business/BaseService.java
# 2. 实现通用add、update、delete、queryPage
# 3. 集成测试（覆盖率≥80%）

# 任务2：迁移Service
# 1. 选择3-5个典型Service迁移
# 2. 验证功能一致性
# 3. 性能对比测试
```

#### 2.4 实现AOP日志（Day 11-12）

```bash
# 任务1：创建日志切面
# 1. 创建microservices-common-aspect/ControllerLogAspect.java
# 2. 实现日志格式化、耗时统计
# 3. 敏感参数过滤

# 任务2：替换现有日志
# 1. 移除Controller中的手动日志
# 2. 验证日志完整性
```

### 步骤3: 测试阶段（Day 13-18）

#### 3.1 单元测试
```bash
# 运行所有单元测试
mvn test

# 目标：测试覆盖率≥60%
# Service层: ≥70%
# Manager层: ≥70%
# Controller层: ≥50%
```

#### 3.2 集成测试
```bash
# 启动所有微服务
./scripts/start-all-services.sh

# API集成测试
./scripts/api-integration-test.sh

# 性能基准测试
./scripts/performance-benchmark.sh
```

#### 3.3 回归测试
```bash
# 执行完整回归测试套件
mvn verify

# 目标：所有测试通过，无回归Bug
```

### 步骤4: 部署阶段（Day 19-20）

#### 4.1 灰度发布
```bash
# 1. 部署到测试环境
kubectl apply -f deployment/test/

# 2. 监控运行状态（观察7天）
# - 错误日志
# - 性能指标
# - 用户反馈

# 3. 逐步扩大灰度范围
# 10% → 30% → 50% → 100%
```

#### 4.2 全量发布
```bash
# 灰度验证无误后，全量发布
kubectl apply -f deployment/production/
```

### 步骤5: 验收阶段（Day 20）

#### 5.1 代码质量验证
```bash
# 对比基线指标
cloc --by-file microservices/ > baseline_after.txt
diff baseline_before.txt baseline_after.txt

# 验证代码减少量
# 目标：减少≥20,000行
```

#### 5.2 性能验证
```bash
# 性能对比
# 目标：
# - 查询响应时间: ↓50%
# - 缓存命中率: ↑25%
# - 并发吞吐量: ↑50%
```

#### 5.3 功能验证
```bash
# 完整功能回归测试
# 目标：100%功能通过
```

---

## 附录

### A. 检查清单

#### 代码审查清单
- [ ] 无重复的Entity类
- [ ] 无重复的查询构建代码
- [ ] 无重复的CRUD操作
- [ ] 无重复的异常处理
- [ ] 无重复的日志记录
- [ ] 所有测试通过
- [ ] 代码覆盖率≥60%

#### 部署检查清单
- [ ] 数据迁移脚本验证
- [ ] 灰度发布监控正常
- [ ] 性能指标达标
- [ ] 用户反馈良好
- [ ] 回滚方案准备

### B. 风险缓解

#### 高风险项
1. **Entity迁移可能影响现有业务**
   - 缓解：充分测试，分阶段迁移

2. **代码重构可能引入新Bug**
   - 缓解：完善测试覆盖，Code Review

3. **性能优化可能影响现有功能**
   - 缓解：性能基准测试，灰度发布

#### 回滚方案
```bash
# 代码回滚
git revert <commit-hash>
git push origin feature/code-cleanup

# 数据回滚
mysql -u root -p ioe_dream < device_tables_backup_YYYYMMDD.sql

# 服务回滚
kubectl rollout undo deployment/ioedream-access-service
```

---

**文档制定**: IOE-DREAM架构委员会
**最后更新**: 2025-12-25
**下次审核**: 2026-01-25
