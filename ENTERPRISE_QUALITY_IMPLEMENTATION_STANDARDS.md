# IOE-DREAM 企业级高质量实现规范

> **文档版本**: v1.0.0
> **制定日期**: 2025-12-25
> **制定依据**: 基于全局代码架构深度分析报告
> **适用范围**: 所有IOE-DREAM项目开发工作

---

## 📋 目录

1. [核心原则](#核心原则)
2. [代码质量标准](#代码质量标准)
3. [架构规范](#架构规范)
4. [性能标准](#性能标准)
5. [安全标准](#安全标准)
6. [测试标准](#测试标准)
7. [实施路线图](#实施路线图)

---

## 核心原则

### 1.1 SOLID原则强制执行

#### S - 单一职责原则 (Single Responsibility)
```java
// ❌ 违反SRP：Service承担过多职责
@Service
public class DeviceServiceImpl implements DeviceService {
    // 设备CRUD
    // 设备通信
    // 设备协议解析
    // 设备数据采集
    // 设备告警处理
    // ... 10+ 个不同职责
}

// ✅ 遵循SRP：职责分离
@Service
public class DeviceServiceImpl implements DeviceService {
    // 只负责设备CRUD
}

@Service
public class DeviceCommunicationService {
    // 只负责设备通信
}

@Service
public class DeviceProtocolService {
    // 只负责协议解析
}
```

**强制要求**:
- 每个类/接口只有一个变更理由
- Service类不超过500行
- 方法不超过50行
- 每个方法只做一件事

#### O - 开闭原则 (Open-Closed Principle)
```java
// ✅ 使用策略模式支持扩展
public interface DeviceStrategy {
    void connect(DeviceEntity device);
    void disconnect(DeviceEntity device);
    void sendCommand(DeviceEntity device, String command);
}

@Component
public class AccessDeviceStrategy implements DeviceStrategy {
    // 门禁设备特定实现
}

@Component
public class VideoDeviceStrategy implements DeviceStrategy {
    // 视频设备特定实现
}

@Component
public class DeviceStrategyFactory {
    private Map<String, DeviceStrategy> strategyMap;

    // 添加新设备类型只需新增Strategy实现，无需修改工厂
    public DeviceStrategy getStrategy(String deviceType) {
        return strategyMap.get(deviceType);
    }
}
```

**强制要求**:
- 对扩展开放，对修改关闭
- 使用接口和抽象类隔离变化
- 使用策略模式、工厂模式支持扩展

#### L - 里氏替换原则 (Liskov Substitution)
```java
// ✅ 正确的继承关系
public class BaseDeviceService {
    public void connect(DeviceEntity device) {
        // 通用连接逻辑
    }
}

public class AccessDeviceService extends BaseDeviceService {
    @Override
    public void connect(DeviceEntity device) {
        // 可以调用父类方法
        super.connect(device);
        // 门禁特定逻辑
    }
}
```

**强制要求**:
- 子类必须能够替换父类
- 子类不能减弱父类的方法访问权限
- 子类不能抛出比父类更宽泛的异常

#### I - 接口隔离原则 (Interface Segregation)
```java
// ❌ 胖接口：强制实现不必要的方法
public interface DeviceService {
    void add(DeviceEntity device);
    void update(DeviceEntity device);
    void delete(Long deviceId);
    void query(PageQueryForm form);
    void connect(DeviceEntity device);      // 部分设备不需要
    void disconnect(DeviceEntity device);   // 部分设备不需要
    void upgrade(DeviceEntity device);      // 部分设备不需要
}

// ✅ 接口隔离：职责分离
public interface DeviceService {
    void add(DeviceEntity device);
    void update(DeviceEntity device);
    void delete(Long deviceId);
    PageResult<DeviceVO> query(PageQueryForm form);
}

public interface DeviceConnectable {
    void connect(DeviceEntity device);
    void disconnect(DeviceEntity device);
}

public interface DeviceUpgradeable {
    void upgrade(DeviceEntity device);
}
```

**强制要求**:
- 接口方法不超过10个
- 客户端不应依赖它不需要的接口
- 使用多个专用接口代替单一胖接口

#### D - 依赖倒置原则 (Dependency Inversion)
```java
// ❌ 直接依赖具体实现
@Service
public class AccessServiceImpl {
    private AccessDeviceDao accessDeviceDao;  // 依赖具体DAO
    private HikariDataSource dataSource;       // 依赖具体数据源
}

// ✅ 依赖抽象
@Service
public class AccessServiceImpl implements AccessService {
    private final DeviceDao deviceDao;         // 依赖接口
    private final DataSource dataSource;       // 依赖抽象

    @Resource
    private GatewayServiceClient gatewayClient; // 依赖服务抽象
}
```

**强制要求**:
- 高层模块不依赖低层模块，都依赖抽象
- 抽象不依赖具体，具体依赖抽象
- 优先使用接口和抽象类

### 1.2 DRY原则强制执行（Don't Repeat Yourself）

#### 代码重复率上限
- **当前**: 34% (约24,000行重复代码)
- **目标**: ≤10%
- **强制标准**: 新代码重复率≤5%

#### 检测机制
```java
// ❌ 违反DRY：重复的查询构建
LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
if (StringUtils.hasText(form.getKeyword())) {
    wrapper.and(w -> w.like(UserEntity::getUsername, form.getKeyword())
                   .or().like(UserEntity::getRealName, form.getKeyword()));
}
if (form.getStatus() != null) {
    wrapper.eq(UserEntity::getStatus, form.getStatus());
}
if (form.getDepartmentId() != null) {
    wrapper.eq(UserEntity::getDepartmentId, form.getDepartmentId());
}

// ✅ 遵循DRY：使用统一查询构建器
public class UserQueryHelper {
    public static LambdaQueryWrapper<UserEntity> buildQuery(UserQueryForm form) {
        return QueryBuilder.of(UserEntity.class)
            .keyword(form.getKeyword(), UserEntity::getUsername, UserEntity::getRealName)
            .eq(UserEntity::getStatus, form.getStatus())
            .eq(UserEntity::getDepartmentId, form.getDepartmentId())
            .between(UserEntity::getCreateTime, form.getStartTime(), form.getEndTime())
            .build();
    }
}
```

**重复代码识别标准**:
- 3行以上相同或相似代码视为重复
- 使用SonarQube检测重复代码
- Code Review必须检查重复代码

### 1.3 KISS原则强制执行（Keep It Simple, Stupid）

#### 代码复杂度限制
```java
// ❌ 过度复杂：圈复杂度30+
public void processDevice(DeviceEntity device) {
    if (device == null) {
        if (device.getType() == 1) {
            if (device.getStatus() == 1) {
                if (device.getAreaId() != null) {
                    // ... 10层嵌套
                }
            }
        }
    }
}

// ✅ 简化：提取方法
public void processDevice(DeviceEntity device) {
    if (!validateDevice(device)) {
        return;
    }

    switch (device.getType()) {
        case DeviceType.ACCESS:
            processAccessDevice(device);
            break;
        case DeviceType.VIDEO:
            processVideoDevice(device);
            break;
        // ...
    }
}

private boolean validateDevice(DeviceEntity device) {
    return device != null && device.getStatus() == DeviceStatus.ONLINE;
}
```

**强制要求**:
- 圈复杂度≤15（理想≤10）
- 嵌套层级≤3层
- 方法参数≤5个
- 使用卫语句替代嵌套if

### 1.4 YAGNI原则强制执行（You Aren't Gonna Need It）

```java
// ❌ 过度设计：不需要的功能
public class DeviceService {
    // 基础CRUD
    // ✅ 当前需要的功能
    public void add(DeviceEntity device) { }
    public void update(DeviceEntity device) { }
    public void delete(Long deviceId) { }

    // ❌ 目前不需要的功能
    public void exportToPDF() { }              // 需求中未提及
    public void syncToCloud() { }              // 需求中未提及
    public void aiAnalysis() { }               // 需求中未提及
    public void blockchainVerify() { }         // 需求中未提及
}

// ✅ YAGNI：只实现当前需要的功能
public class DeviceService {
    public void add(DeviceEntity device) { }
    public void update(DeviceEntity device) { }
    public void delete(Long deviceId) { }

    // 等真正需要时再添加
}
```

**强制要求**:
- 不实现当前不需要的功能
- 不为未来可能的扩展预留代码
- 删除未使用的代码和导入
- 优先考虑简单方案

---

## 代码质量标准

### 2.1 代码复杂度标准

| 指标 | 当前值 | 目标值 | 强制标准 |
|------|--------|--------|----------|
| 圈复杂度 | 平均15 | ≤10 | ≤15 |
| 方法行数 | 平均50 | ≤30 | ≤50 |
| 类行数 | 平均500 | ≤300 | ≤500 |
| 参数个数 | 平均6 | ≤4 | ≤5 |
| 嵌套层级 | 平均4 | ≤2 | ≤3 |

### 2.2 命名规范

#### 类命名
```java
// ✅ 标准命名
public class UserController { }                 // Controller
public class UserServiceImpl implements UserService { }  // Service实现
public class UserEntity { }                     // Entity
public class UserDao { }                        // DAO
public class UserManager { }                    // Manager
public class UserAddForm { }                    // 新增表单
public class UserVO { }                         // 视图对象

// ❌ 禁止命名
class UserCtrl { }                             // 不完整
class DoUser { }                               // 动词开头
class UserInfo { }                             // Entity不使用Info后缀
class UserUtils { }                            // 工具类使用Util而非Utils
```

#### 方法命名
```java
// ✅ 标准命名
public Long addUser(UserAddForm form) { }              // add: 新增
public void updateUser(Long userId, UserUpdateForm form) { }  // update: 更新
public void deleteUser(Long userId) { }                // delete: 删除
public UserVO getUserById(Long userId) { }             // getUser: 查询单个
public PageResult<UserVO> pageUsers(UserQueryForm form) { } // page: 分页查询
public List<UserVO> listUsers(UserQueryForm form) { }  // list: 查询列表

// ❌ 禁止命名
public User doSave() { }                             // do前缀不明确
public User info() { }                               // 动词不清
public User get() { }                                // 缺少宾语
public void process() { }                            // 过于宽泛
```

#### 变量命名
```java
// ✅ 标准命名
private Long userId;                                // 驼峰命名
private String deviceName;
private LocalDateTime createTime;
private List<DeviceEntity> deviceList;              // 集合使用List/Map后缀
private Map<Long, DeviceEntity> deviceMap;

// ❌ 禁止命名
private Long user_id;                               // 下划线命名（Java不用）
private String DEVICE_NAME;                         // 全大写（常量才用）
private LocalDateTime createtime;                   // 缩写不清
private List<DeviceEntity> devices;                 // 应为deviceList
private Map<Long, DeviceEntity> map;                // 应为deviceMap
```

### 2.3 注释规范

#### 类注释
```java
/**
 * 用户管理服务实现
 *
 * <p>负责用户CRUD、用户权限管理、用户状态管理</p>
 *
 * @author IOE-DREAM团队
 * @since 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {
    // ...
}
```

#### 方法注释
```java
/**
 * 添加用户
 *
 * @param form 用户信息表单
 * @return 用户ID
 * @throws BusinessException 用户名已存在时抛出
 */
@Override
public Long addUser(UserAddForm form) {
    // ...
}
```

**注释原则**:
- 公共API必须有JavaDoc注释
- 复杂业务逻辑必须有注释说明
- 避免无意义的注释（如`// 设置用户名`）
- 代码本身清晰时不需要注释

### 2.4 格式规范

#### 导入顺序
```java
// 1. Java标准库
import java.util.*;
import java.time.*;
import java.util.stream.*;

// 2. 第三方库
import lombok.*;
import org.springframework.*;
import com.baomidou.mybatisplus.*;

// 3. 项目内部
import net.lab1024.sa.common.entity.*;
import net.lab1024.sa.{module}.entity.*;
import net.lab1024.sa.{module}.dao.*;
import net.lab1024.sa.{module}.service.*;
```

#### 代码格式
```java
// ✅ 标准格式
if (condition) {
    doSomething();
} else {
    doOtherThing();
}

for (int i = 0; i < 10; i++) {
    processItem(i);
}

// ❌ 禁止格式
if(condition){doSomething();}else{doOtherThing();}  // 单行过多
for(int i=0;i<10;i++)                              // 缺少空格
    processItem(i);                                // 缩进不正确
```

---

## 架构规范

### 3.1 四层架构强制执行

```
Controller层 → Service层 → Manager层 → DAO层
     ↓            ↓           ↓           ↓
  接口暴露     业务逻辑    业务编排    数据访问
```

#### 层级职责
```java
// ========== Controller层 ==========
@RestController
public class UserController {
    // 职责：
    // 1. 接收HTTP请求
    // 2. 参数验证
    // 3. 调用Service
    // 4. 返回响应

    @PostMapping("/add")
    public ResponseDTO<Long> add(@RequestBody @Valid UserAddForm form) {
        // ✅ 只做参数验证和调用
        Long userId = userService.addUser(form);
        return ResponseDTO.ok(userId);
    }
}

// ========== Service层 ==========
@Service
public class UserServiceImpl implements UserService {
    // 职责：
    // 1. 业务逻辑处理
    // 2. 事务控制
    // 3. 调用Manager编排

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUser(UserAddForm form) {
        // ✅ 业务逻辑
        checkUsernameUnique(form.getUsername());

        // ✅ 调用Manager编排
        Long userId = userManager.addUser(form);

        // ✅ 后置处理
        cacheManager.evictUserCache(userId);

        return userId;
    }
}

// ========== Manager层 ==========
@Component
public class UserManager {
    // 职责：
    // 1. 多DAO编排
    // 2. 复杂业务组合
    // 3. 跨领域协调

    public Long addUser(UserAddForm form) {
        // ✅ DAO编排
        UserEntity entity = BeanUtil.copy(form, UserEntity.class);
        userDao.insert(entity);

        // ✅ 关联数据处理
        if (form.getRoleIds() != null) {
            userRoleManager.saveUserRoles(entity.getUserId(), form.getRoleIds());
        }

        return entity.getUserId();
    }
}

// ========== DAO层 ==========
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    // 职责：
    // 1. 数据访问
    // 2. SQL执行
    // 3. 结果映射

    // ✅ 只做数据访问，不包含业务逻辑
}
```

#### 违规检测
```java
// ❌ 严重违规：跨层访问
@Service
public class UserServiceImpl {
    @Resource
    private UserDao userDao;  // ✅ 正确：Service→Manager→DAO

    @Resource
    private AreaDao areaDao;  // ❌ 违规：Service直接调用DAO（跳过Manager）
}

// ❌ 严重违规：反向调用
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    // ❌ 违规：DAO调用Service
    @Select("SELECT * FROM t_user WHERE user_id = #{userId}")
    @Results({
        @Result(property = "department", column = "department_id",
                one = @One(select = "net.lab1024.sa.DepartmentDao.selectById"))
    })
    UserEntity selectWithDepartment(Long userId);
}
```

### 3.2 依赖管理规范

#### 依赖方向
```
Controller → Service → Manager → DAO
   ↓           ↓          ↓         ↓
   └───────────┴──────────┴─────────┘
            禁止反向依赖
```

#### 循环依赖检测
```java
// ❌ 严重违规：循环依赖
// A → B → A
@Service
public class ServiceA {
    @Resource
    private ServiceB serviceB;
}

@Service
public class ServiceB {
    @Resource
    private ServiceA serviceA;  // ❌ 循环依赖
}

// ✅ 正确：提取公共依赖
@Service
public class ServiceA {
    @Resource
    private CommonService commonService;
}

@Service
public class ServiceB {
    @Resource
    private CommonService commonService;  // ✅ 无循环依赖
}
```

#### 服务间调用规范
```java
// ❌ 禁止：业务服务直接依赖
@Service
public class AccessServiceImpl {
    @Resource
    private UserService userService;  // ❌ 直接依赖其他业务服务
}

// ✅ 正确：通过GatewayClient调用
@Service
public class AccessServiceImpl {
    @Resource
    private GatewayServiceClient gatewayClient;

    public UserVO getUserById(Long userId) {
        return gatewayClient.callCommonService(
            "/api/user/" + userId,
            HttpMethod.GET,
            null,
            new TypeReference<ResponseDTO<UserVO>>() {}
        ).getData();
    }
}
```

### 3.3 实体类设计规范

#### 统一Entity管理
```java
// ✅ 正确：统一Entity在common-entity模块
// 位置：microservices-common-entity
package net.lab1024.sa.common.entity;

@TableName("t_common_device")
public class DeviceEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String deviceId;

    private String deviceName;
    private Integer deviceType;      // 1-门禁 2-考勤 3-消费 4-视频 5-访客
    private Integer deviceSubType;
    private String businessModule;   // access/attendance/consume/visitor/video

    // 扩展属性（JSON格式）
    private String extendedAttributes;

    // ... 通用字段
}

// ❌ 禁止：重复的Entity类
@TableName("t_access_device")  // ❌ 独立表，数据分散
public class AccessDeviceEntity extends BaseEntity {
    private String deviceId;
    private String deviceName;
    // ... 与DeviceEntity重复的字段
}

@TableName("t_consume_device")  // ❌ 独立表，数据分散
public class ConsumeDeviceEntity extends BaseEntity {
    private String deviceId;
    private String deviceName;
    // ... 与DeviceEntity重复的字段
}
```

**Entity设计原则**:
- 统一在`microservices-common-entity`模块管理
- 使用type字段区分业务类型
- 使用extendedAttributes存储业务特定字段（JSON格式）
- Entity行数≤200行，字段数≤30个
- 禁止包含业务逻辑方法

---

## 性能标准

### 4.1 数据库性能

#### 索引优化标准
```sql
-- ✅ 为高频查询字段添加索引
-- 当前：65%查询缺少索引
-- 目标：100%覆盖

-- 1. 单字段索引
CREATE INDEX idx_device_type ON t_common_device(device_type);
CREATE INDEX idx_device_status ON t_common_device(device_status);

-- 2. 复合索引（覆盖常用查询条件）
CREATE INDEX idx_device_area_status ON t_common_device(area_id, device_status, create_time);

-- 3. 唯一索引
CREATE UNIQUE INDEX uk_device_code ON t_common_device(device_code, deleted_flag);

-- 4. 索引验证
-- 检查索引使用情况
EXPLAIN SELECT * FROM t_common_device
WHERE area_id = 1 AND device_status = 1;
-- 确保type=ref或type=index（禁止ALL全表扫描）
```

**索引设计原则**:
- WHERE、ORDER BY、JOIN字段必须有索引
- 复合索引遵循最左前缀原则
- 区分度高的字段优先建索引
- 避免冗余索引和重复索引

#### N+1查询问题
```java
// ❌ N+1查询问题
public List<DeviceVO> getDevicesWithArea(List<Long> deviceIds) {
    List<DeviceEntity> devices = deviceDao.selectBatchIds(deviceIds);  // 1次查询
    List<DeviceVO> voList = new ArrayList<>();

    for (DeviceEntity device : devices) {  // N次循环查询
        AreaEntity area = areaDao.selectById(device.getAreaId());  // ❌ N次查询
        DeviceVO vo = convertToVO(device, area);
        voList.add(vo);
    }
    return voList;
}

// ✅ 批量查询解决N+1问题
public List<DeviceVO> getDevicesWithArea(List<Long> deviceIds) {
    // 1. 查询设备
    List<DeviceEntity> devices = deviceDao.selectBatchIds(deviceIds);

    // 2. 提取所有areaId
    Set<Long> areaIds = devices.stream()
        .map(DeviceEntity::getAreaId)
        .collect(Collectors.toSet());

    // 3. 批量查询区域（1次查询）
    List<AreaEntity> areas = areaDao.selectBatchIds(areaIds);
    Map<Long, AreaEntity> areaMap = areas.stream()
        .collect(Collectors.toMap(AreaEntity::getAreaId, Function.identity()));

    // 4. 组装VO
    return devices.stream()
        .map(device -> convertToVO(device, areaMap.get(device.getAreaId())))
        .collect(Collectors.toList());
}
```

**N+1查询检测**:
- 使用MyBatis-Plus日志检查SQL执行次数
- 1次N+1查询 = 1次主查询 + N次关联查询
- 目标：消除所有N+1查询问题

### 4.2 缓存性能

#### 三级缓存架构
```java
@Service
public class DeviceServiceImpl implements DeviceService {

    // L1: Caffeine本地缓存（容量1000，过期30分钟）
    @Cacheable(value = "device:local", key = "#deviceId", unless = "#result == null")
    public DeviceVO getDeviceById(Long deviceId) {
        // L1缓存命中：响应时间<1ms
        return deviceManager.getDeviceById(deviceId);
    }

    // L2: Redis分布式缓存（过期30分钟）
    @Cacheable(value = "device:redis", key = "'device:' + #deviceId", unless = "#result == null")
    public DeviceVO getDeviceByIdWithRedis(Long deviceId) {
        // L2缓存命中：响应时间<5ms
        return getDeviceById(deviceId);
    }

    // L3: 数据库（持久化）
    public DeviceVO getDeviceFromDB(Long deviceId) {
        // L3数据库查询：响应时间<50ms（有索引）
        DeviceEntity entity = deviceDao.selectById(deviceId);
        return convertToVO(entity);
    }
}
```

**缓存策略**:
- **L1本地缓存**: Caffeine，容量1000，过期30分钟
- **L2分布式缓存**: Redis，过期30分钟
- **L3数据库**: MySQL持久化存储
- **目标缓存命中率**: ≥90%（当前65%）

**缓存更新策略**:
```java
// ✅ CachePut缓存更新
@CachePut(value = "device:redis", key = "'device:' + #device.deviceId")
public DeviceVO updateDevice(DeviceEntity device) {
    deviceDao.updateById(device);
    return convertToVO(device);
}

// ✅ CacheEvict缓存删除
@CacheEvict(value = "device:redis", key = "'device:' + #deviceId")
public void deleteDevice(Long deviceId) {
    deviceDao.deleteById(deviceId);
}

// ✅ 批量缓存删除
@CacheEvict(value = "device:redis", allEntries = true)
public void refreshAllDeviceCache() {
    // 清空所有设备缓存
}
```

### 4.3 并发控制

#### 乐观锁
```java
@TableName("t_common_device")
public class DeviceEntity extends BaseEntity {
    @Version
    private Integer version;  // 乐观锁版本号
}

@Service
public class DeviceServiceImpl {

    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceStatus(Long deviceId, Integer status) {
        DeviceEntity device = deviceDao.selectById(deviceId);
        device.setDeviceStatus(status);

        // ✅ MyBatis-Plus自动使用乐观锁
        int rows = deviceDao.updateById(device);

        if (rows == 0) {
            throw new BusinessException("设备信息已被修改，请重试");
        }
    }
}
```

#### 分布式锁
```java
@Service
public class DeviceServiceImpl {

    @Resource
    private RedissonClient redissonClient;

    public void batchUpdateDevices(List<Long> deviceIds) {
        RLock lock = redissonClient.getLock("device:batch:update");

        try {
            // 尝试加锁，最多等待10秒，锁30秒后自动释放
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                try {
                    // 执行批量更新
                    deviceDao.updateBatchById(deviceIds);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SystemException("SYSTEM_ERROR", "操作被中断", e);
        }
    }
}
```

**并发控制原则**:
- 读取操作：无锁或使用读写锁
- 更新操作：使用乐观锁（version字段）
- 批量操作：使用分布式锁
- 事务隔离级别：READ_COMMITTED

---

## 安全标准

### 5.1 权限控制

#### 统一权限注解
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionCheck {
    /**
     * 权限编码
     */
    String value();

    /**
     * 权限描述
     */
    String description() default "";

    /**
     * 是否需要登录
     */
    boolean requireLogin() default true;
}

// ✅ Controller应用权限注解
@RestController
@RequestMapping("/api/v1/device")
@PermissionCheck(value = "DEVICE_MANAGE", description = "设备管理权限")
public class DeviceController {

    @GetMapping("/{deviceId}")
    @PermissionCheck(value = "DEVICE_VIEW", description = "查看设备权限")
    public ResponseDTO<DeviceVO> getDevice(@PathVariable Long deviceId) {
        // ...
    }

    @PostMapping
    @PermissionCheck(value = "DEVICE_ADD", description = "新增设备权限")
    public ResponseDTO<Long> addDevice(@RequestBody @Valid DeviceAddForm form) {
        // ...
    }

    @DeleteMapping("/{deviceId}")
    @PermissionCheck(value = "DEVICE_DELETE", description = "删除设备权限")
    public ResponseDTO<Void> deleteDevice(@PathVariable Long deviceId) {
        // ...
    }
}
```

**权限覆盖率目标**:
- 当前：60%
- 目标：100%
- 强制：所有敏感接口必须有权限验证

### 5.2 输入验证

#### 参数验证
```java
@Data
public class DeviceAddForm {

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    private String deviceName;

    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    @NotBlank(message = "设备编码不能为空")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "设备编码只能包含大写字母、数字、下划线和连字符")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    private String deviceCode;

    @NotNull(message = "所属区域不能为空")
    private Long areaId;

    @IPAddress(message = "IP地址格式不正确")
    private String ipAddress;

    @Pattern(regexp = "^https?://.*", message = "URL格式不正确")
    private String url;
}

// ✅ Controller使用@Valid验证
@PostMapping
public ResponseDTO<Long> addDevice(@RequestBody @Valid DeviceAddForm form) {
    // Spring自动验证，失败时抛出MethodArgumentNotValidException
    return ResponseDTO.ok(deviceService.addDevice(form));
}
```

#### SQL注入防护
```java
// ❌ SQL注入风险：字符串拼接
@Select("SELECT * FROM t_common_device WHERE device_name = '${deviceName}'")
List<DeviceEntity> selectByName(@Param("deviceName") String deviceName);

// ✅ 使用参数化查询
@Select("SELECT * FROM t_common_device WHERE device_name = #{deviceName}")
List<DeviceEntity> selectByName(@Param("deviceName") String deviceName);

// ✅ 使用MyBatis-Plus（推荐）
LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(DeviceEntity::getDeviceName, deviceName);
List<DeviceEntity> devices = deviceDao.selectList(wrapper);
```

### 5.3 敏感数据处理

#### 敏感信息脱敏
```java
@Slf4j
@Service
public class UserServiceImpl {

    public UserVO getUserById(Long userId) {
        UserEntity user = userDao.selectById(userId);

        // ❌ 禁止：记录敏感信息
        log.info("用户信息: {}", user);  // 可能包含密码、手机号等

        // ✅ 正确：脱敏后记录
        UserVO userVO = convertToVO(user);
        log.info("查询用户: userId={}, username={}, phone={}",
            userId, userVO.getUsername(), maskPhone(userVO.getPhone()));

        return userVO;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
```

**敏感信息列表**:
- 密码：禁止记录和传输
- 手机号：脱敏（138****5678）
- 身份证号：脱敏（110101********1234）
- 银行卡号：脱敏（6222***********1234）
- Token：禁止完整记录

---

## 测试标准

### 6.1 单元测试

#### 测试覆盖率目标
| 模块 | 当前覆盖率 | 目标覆盖率 | 强制标准 |
|------|-----------|-----------|----------|
| Service层 | 30% | ≥80% | ≥60% |
| Manager层 | 25% | ≥75% | ≥60% |
| Controller层 | 20% | ≥50% | ≥40% |
| DAO层 | 40% | ≥70% | ≥50% |

#### 单元测试示例
```java
@Slf4j
@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserService userService;

    @MockBean
    private UserDao userDao;

    @Test
    @DisplayName("测试：添加用户成功")
    void testAddUser_Success() {
        // Given
        UserAddForm form = new UserAddForm();
        form.setUsername("testuser");
        form.setRealName("测试用户");
        form.setPhone("13800138000");

        UserEntity mockUser = new UserEntity();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");

        when(userDao.selectByUsername(anyString())).thenReturn(null);
        when(userDao.insert(any(UserEntity.class))).thenReturn(1);

        // When
        Long userId = userService.addUser(form);

        // Then
        assertNotNull(userId);
        assertEquals(1L, userId);

        log.info("[用户测试] 测试通过: testCase=testAddUser_Success");
    }

    @Test
    @DisplayName("测试：用户名重复时抛出异常")
    void testAddUser_UsernameDuplicate() {
        // Given
        UserAddForm form = new UserAddForm();
        form.setUsername("existinguser");

        UserEntity existingUser = new UserEntity();
        existingUser.setUserId(1L);
        existingUser.setUsername("existinguser");

        when(userDao.selectByUsername("existinguser")).thenReturn(existingUser);

        // When & Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> userService.addUser(form)
        );

        assertEquals("USER_NAME_EXISTS", exception.getCode());
        log.info("[用户测试] 测试通过: testCase=testAddUser_UsernameDuplicate");
    }
}
```

### 6.2 集成测试

#### API集成测试
```java
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("集成测试：添加用户API")
    void testAddUserAPI() throws Exception {
        // Given
        UserAddForm form = new UserAddForm();
        form.setUsername("testuser");
        form.setRealName("测试用户");
        form.setPhone("13800138000");

        String requestBody = objectMapper.writeValueAsString(form);

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/user/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        log.info("[用户集成测试] 响应结果: {}", response);
    }
}
```

---

## 实施路线图

### 阶段1：基础清理（1-2周）

#### P0-1: 清理备份和临时文件
**优先级**: 🔴 最高
**工作量**: 1人天
**执行**:
```bash
# 清理备份文件
find microservices -name "*.backup*" -delete
find microservices -name "*.bak" -delete
find microservices -name "*.original*" -delete

# 预期效果：清理495个备份文件，减少仓库体积约30%
```

#### P0-2: 统一Entity管理
**优先级**: 🔴 最高
**工作量**: 5人天
**方案**:
1. 将所有业务Entity迁移至`microservices-common-entity`
2. 统一使用DeviceEntity、UserEntity等
3. 通过type字段区分业务类型
4. 删除冗余的AccessDeviceEntity、ConsumeDeviceEntity等

**预期效果**:
- 减少Entity数量约40%
- 消除数据不一致风险
- 提升代码复用性

### 阶段2：代码优化（2-4周）

#### P1-1: 实现统一查询构建器
**优先级**: 🟡 高
**工作量**: 3人天
**方案**:
```java
public class QueryBuilder<T> {
    private final LambdaQueryWrapper<T> wrapper;

    public QueryBuilder<T> keyword(SerializableFunction<T, String>... fields, String value) {
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

    public QueryBuilder<T> eq(SerializableFunction<T, ?> field, Object value) {
        if (value != null) {
            wrapper.eq(field, value);
        }
        return this;
    }

    public QueryBuilder<T> in(SerializableFunction<T, ?> field, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            wrapper.in(field, values);
        }
        return this;
    }

    public LambdaQueryWrapper<T> build() {
        return wrapper;
    }

    public static <T> QueryBuilder<T> of(Class<T> entityClass) {
        return new QueryBuilder<>();
    }
}
```

**预期效果**:
- 减少查询构建代码70%
- 提升代码可读性
- 降低维护成本

#### P1-2: 提升测试覆盖率
**优先级**: 🟡 高
**工作量**: 10人天
**目标**:
- Service层测试覆盖率从30%→60%
- Controller层测试覆盖率从20%→50%
- 关键业务逻辑覆盖率达到80%

### 阶段3：性能优化（1-2个月）

#### P2-1: SQL优化
**优先级**: 🟢 中
**工作量**: 5人天
**方案**:
1. 为高频查询字段添加索引（约20个索引）
2. 解决N+1查询问题（约10处）
3. 优化慢查询（目标：响应时间<100ms）

**预期效果**:
- 查询性能提升50%
- 数据库CPU使用率降低30%

#### P2-2: 三级缓存实现
**优先级**: 🟢 中
**工作量**: 5人天
**方案**:
1. L1 Caffeine本地缓存
2. L2 Redis分布式缓存
3. L3数据库持久化
4. 缓存命中率从65%→90%

### 阶段4：架构重构（2-3个月）

#### P3-1: 补全Manager层
**优先级**: 🟢 中
**工作量**: 10人天
**方案**:
1. 补全缺失的Manager层（40%服务缺失）
2. 消除Service直接调用DAO
3. 实现DAO编排逻辑

#### P3-2: 消除循环依赖
**优先级**: 🟢 中
**工作量**: 5人天
**方案**:
1. 识别循环依赖（已发现5处）
2. 提取公共依赖到独立服务
3. 使用GatewayClient解耦服务依赖

---

## 附录

### A. 代码质量检查清单

#### 提交前检查
- [ ] 代码重复率≤5%
- [ ] 圈复杂度≤15
- [ ] 方法行数≤50
- [ ] 类行数≤500
- [ ] 所有敏感接口有权限验证
- [ ] 所有查询有索引支持
- [ ] 单元测试覆盖率≥60%

#### Code Review检查
- [ ] 遵循SOLID原则
- [ ] 遵循DRY原则
- [ ] 遵循KISS原则
- [ ] 遵循YAGNI原则
- [ ] 四层架构合规
- [ ] 无跨层访问
- [ ] 无循环依赖

### B. 质量度量指标

| 指标 | 当前值 | 目标值 | 测量工具 |
|------|--------|--------|----------|
| 代码重复率 | 34% | ≤10% | SonarQube |
| 圈复杂度 | 平均15 | ≤10 | SonarQube |
| 测试覆盖率 | 30% | ≥60% | JaCoCo |
| 缓存命中率 | 65% | ≥90% | Redis监控 |
| 接口响应时间 | 平均800ms | ≤200ms | APM监控 |
| 代码规范合规率 | 70% | ≥95% | CheckStyle |

---

**文档制定**: IOE-DREAM架构委员会
**最后更新**: 2025-12-25
**下次审核**: 2026-01-25
**版本历史**:
- v1.0.0 (2025-12-25): 初始版本，基于全局代码架构分析报告
