# IOE-DREAM 扩展表架构设计标准

**基于现有最佳实践的增强和完善**

**创建时间**: 2025-11-25
**版本**: v1.0.0
**适用范围**: IOE-DREAM智慧园区一卡通管理平台所有业务模块

---

## 📋 概述

本文档基于IOE-DREAM项目中已验证的扩展表成功实践，制定统一的扩展表架构设计标准。项目已经在区域管理、设备管理、生物特征等模块中成功实现了扩展表机制，本文档旨在将这些成功实践标准化并推广到全局。

### 🎯 核心目标

- **避免代码冗余**: 通过统一的设计模式减少重复代码
- **提升开发效率**: 基于现有最佳实践提供可复用的模板
- **保证架构一致性**: 确保所有模块遵循统一的设计原则
- **简化维护工作**: 标准化的设计降低系统复杂度

---

## 🏗️ 扩展表架构核心模式

### 模式1: 基础实体 + 扩展表 (Base + Extension)

**适用场景**: 区域管理、账户管理等需要业务扩展的场景

**现有成功案例**:
- `AreaEntity` + `AccessAreaExtEntity` (区域管理)
- `AccountEntity` + `AccountExtensionEntity` (建议扩展)

**设计原则**:
```java
// 基础实体 - 包含通用字段
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_{base_domain}")
public class {BaseDomain}Entity extends BaseEntity {
    @TableId("{base_domain}_id")
    private Long {baseDomain}Id;

    // 通用字段
    private String {baseDomain}Code;
    private String {baseDomain}Name;
    private Integer status;
    // ... 其他通用字段
}

// 扩展实体 - 业务特有字段
@Data
@TableName("t_{base_domain}_{module}_ext")
public class {BaseDomain}{Module}ExtEntity extends BaseEntity {
    @TableId("ext_id")
    private Long extId;

    @TableField("{base_domain}_id")
    private Long {baseDomain}Id;  // 外键关联

    // 模块特有字段
    private {ModuleSpecificFields}
}
```

### 模式2: 基础类 + 业务继承 (Base + Inheritance)

**适用场景**: 设备管理、生物特征等具有明确继承关系的场景

**现有成功案例**:
- `SmartDeviceEntity` + `AccessDeviceEntity` (设备管理)
- `BiometricRecordEntity` + 各种生物特征记录

**设计原则**:
```java
// 基础类 - 提供通用功能和字段
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_{base_type}_device")
public class {BaseType}DeviceEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 通用设备字段
    private String deviceId;
    private String deviceCode;
    private String deviceName;
    private String deviceType;  // 枚举：ACCESS, ATTENDANCE, CONSUME, VIDEO

    // JSON配置字段 - 避免字段冗余
    private String configJson;        // 基础配置
    private String extensionConfig;   // 扩展配置

    // 通用业务方法
    public enum DeviceType { ... }
    public boolean isOnline() { ... }
}

// 业务类 - 继承并扩展特定功能
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_{module}_device")
public class {Module}DeviceEntity extends {BaseType}DeviceEntity {
    // 业务特有字段
    private {ModuleSpecificFields}

    // 业务特有方法
    public boolean supports{Feature}() { ... }
}
```

---

## 📊 数据库设计标准

### 1. 命名规范（基于现有实践增强）

**表命名规范**:
```sql
-- 基础表
t_{business_domain}              -- 例如：t_area, t_device, t_account

-- 扩展表 (现有模式)
t_{base_domain}_{module}_ext     -- 例如：t_area_access_ext
t_{base_domain}_{business}_ext   -- 例如：t_account_attendance_ext

-- 继承表 (现有模式)
t_{module}_device                -- 例如：t_access_device, t_consume_device
t_{module}_{specific}            -- 例如：t_biometric_attendance_extension
```

**字段命名规范**:
```sql
-- 主键字段
{table_name}_id                  -- 例如：area_id, device_id, ext_id

-- 外键字段 (与关联表主键保持一致)
{referenced_table}_id           -- 例如：area_id, device_id, person_id

-- 业务字段 (基于现有成功实践)
{business_feature}_{type}        -- 例如：access_level, consume_limit
{config_field}                   -- 例如：time_config, alert_config
{status_field}                   -- 例如：device_status, account_status

-- JSON配置字段 (避免字段冗余的成功实践)
{feature}_config                 -- 例如：access_config, time_config
{feature}_settings              -- 例如：biometric_settings
{feature}_rules                 -- 例如：validation_rules
```

### 2. 字段设计标准（基于现有字段分析）

**审计字段** - 统一继承BaseEntity，避免重复定义:
```sql
-- 以下字段由BaseEntity提供，无需在子类中重复定义
create_time      BIGINT        NOT NULL COMMENT '创建时间'
update_time      BIGINT        NOT NULL COMMENT '更新时间'
create_user_id   BIGINT        COMMENT '创建用户ID'
update_user_id   BIGINT        COMMENT '更新用户ID'
deleted_flag     TINYINT(1)    DEFAULT 0 COMMENT '删除标识'
version          INT           DEFAULT 0 COMMENT '版本号'
```

**基础实体字段** (基于AreaEntity, SmartDeviceEntity的成功实践):
```sql
-- 基础标识字段
{domain}_code      VARCHAR(50)   NOT NULL COMMENT '{domain}编码'
{domain}_name      VARCHAR(100)  NOT NULL COMMENT '{domain}名称'

-- 状态和层级字段
status            TINYINT(1)    DEFAULT 1 COMMENT '状态：1-启用，0-禁用'
-- 区域特有层级字段
parent_id         BIGINT        COMMENT '父级ID'
path              VARCHAR(500)  COMMENT '路径'
level             INT           DEFAULT 1 COMMENT '层级'
sort_order        INT           DEFAULT 0 COMMENT '排序'

-- 设备特有连接字段 (基于SmartDeviceEntity成功实践)
ip_address        VARCHAR(45)   COMMENT 'IP地址'
port              INT           COMMENT '端口'
protocol_type     VARCHAR(20)   COMMENT '协议类型'
```

**扩展表字段** (基于现有扩展表实践):
```sql
-- 关联字段
{base_domain}_id  BIGINT        NOT NULL COMMENT '关联{domain}ID'

-- 业务特有字段 (避免重复定义的原则)
{module}_level    INT           COMMENT '{module}等级'
{module}_mode     VARCHAR(50)   COMMENT '{module}模式'
{module}_config   TEXT          COMMENT '{module}配置(JSON)'

-- JSON配置字段 (成功避免字段冗余的实践)
time_restrictions   TEXT        COMMENT '时间限制配置(JSON)'
location_rules      TEXT        COMMENT '位置规则配置(JSON)'
alert_config        TEXT        COMMENT '告警配置(JSON)'
extension_data      TEXT        COMMENT '扩展数据(JSON)'
```

### 3. 索引设计标准（基于现有性能优化实践）

**基础表索引**:
```sql
-- 主键索引
PRIMARY KEY ({table_name}_id)

-- 唯一索引
UNIQUE KEY uk_{table}_code ({domain}_code, deleted_flag)

-- 查询索引 (基于现有高频查询分析)
KEY idx_{table}_status (status, deleted_flag)
KEY idx_{table}_parent (parent_id, deleted_flag, sort_order)
KEY idx_{table}_path (path, deleted_flag, status)  -- 区域查询优化
```

**扩展表索引**:
```sql
-- 主键索引
PRIMARY KEY (ext_id)

-- 关联索引 (基于现有关联查询优化)
KEY idx_ext_{base_domain}_id ({base_domain}_id, deleted_flag)
KEY idx_ext_{base_domain}_status ({base_domain}_id, status, deleted_flag)

-- 业务查询索引
KEY idx_ext_{module}_level ({module}_level, status)
KEY idx_ext_{module}_mode ({module}_mode, deleted_flag)
```

---

## 🔧 Java代码设计标准

### 1. 实体类设计标准（基于现有成功模式增强）

**基础实体模板** (基于AreaEntity, SmartDeviceEntity):
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_{base_domain}")
public class {BaseDomain}Entity extends BaseEntity {

    @TableId("{base_domain}_id")
    private Long {baseDomain}Id;

    /**
     * {domain}编码
     */
    @TableField("{domain}_code")
    private String {domain}Code;

    /**
     * {domain}名称
     */
    @TableField("{domain}_name")
    private String {domain}Name;

    /**
     * 状态：1-启用，0-禁用
     */
    @TableField("status")
    private Integer status;

    // 区域特有字段 (如需要)
    @TableField("parent_id")
    private Long parentId;

    @TableField("path")
    private String path;

    @TableField("level")
    private Integer level;

    @TableField("sort_order")
    private Integer sortOrder;

    // 设备特有字段 (如需要)
    @TableField("ip_address")
    private String ipAddress;

    @TableField("port")
    private Integer port;

    @TableField("protocol_type")
    private String protocolType;

    // 基于现有成功实践的业务方法
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(this.status);
    }

    public boolean isRoot() {
        return this.parentId == null || this.parentId == 0;
    }

    /**
     * 获取层级深度
     */
    public int getDepth() {
        return this.path != null ? this.path.split(",").length : 1;
    }
}
```

**扩展实体模板** (基于AccessAreaExtEntity成功实践):
```java
@Data
@TableName("t_{base_domain}_{module}_ext")
public class {BaseDomain}{Module}ExtEntity extends BaseEntity {

    @TableId("ext_id")
    private Long extId;

    /**
     * 关联{domain}ID
     */
    @TableField("{base_domain}_id")
    private Long {baseDomain}Id;

    /**
     * {module}等级
     */
    @TableField("{module}_level")
    private Integer {module}Level;

    /**
     * {module}模式 (JSON数组)
     */
    @TableField("{module}_mode")
    private String {module}Mode;

    /**
     * 是否需要特殊处理
     */
    @TableField("special_required")
    private Boolean specialRequired;

    /**
     * 时间限制配置 (JSON)
     */
    @TableField("time_restrictions")
    private String timeRestrictions;

    /**
     * 告警配置 (JSON)
     */
    @TableField("alert_config")
    private String alertConfig;

    /**
     * 扩展配置 (JSON)
     */
    @TableField("extension_config")
    private String extensionConfig;

    // 基于现有成功实践的业务方法
    public boolean isHigh{Module}() {
        return this.{module}Level != null && this.{module}Level >= 2;
    }

    public boolean supports{Mode}(String mode) {
        return this.{module}Mode != null && this.{module}Mode.contains(mode);
    }

    public boolean hasTimeRestrictions() {
        return StringUtils.isNotBlank(this.timeRestrictions);
    }

    /**
     * 设置默认配置 (避免配置冗余)
     */
    public void setDefaultConfig() {
        if (this.{module}Level == null) {
            this.{module}Level = 1;
        }
        if (this.specialRequired == null) {
            this.specialRequired = false;
        }
    }
}
```

### 2. DAO层设计标准（基于现有DAO模式增强）

**扩展表DAO模板** (基于现有DAO最佳实践):
```java
@Mapper
public interface {BaseDomain}{Module}ExtDao extends BaseMapper<{BaseDomain}{Module}ExtEntity> {

    /**
     * 根据{domain}ID查询扩展信息
     */
    @Select("SELECT * FROM t_{base_domain}_{module}_ext " +
            "WHERE {base_domain}_id = #{baseDomainId} AND deleted_flag = 0")
    {BaseDomain}{Module}ExtEntity selectBy{BaseDomain}Id(@Param("baseDomainId") Long baseDomainId);

    /**
     * 批量查询扩展信息 (基于现有批量操作实践)
     */
    @Select("<script>" +
            "SELECT * FROM t_{base_domain}_{module}_ext " +
            "WHERE {base_domain}_id IN " +
            "<foreach collection='baseDomainIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    List<{BaseDomain}{Module}ExtEntity> selectBy{BaseDomain}Ids(@Param("baseDomainIds") List<Long> baseDomainIds);

    /**
     * 关联查询基础信息和扩展信息 (基于现有JOIN查询优化)
     */
    @Select("SELECT " +
            "base.{base_domain}_id, base.{domain}_code, base.{domain}_name, " +
            "ext.ext_id, ext.{module}_level, ext.{module}_mode, ext.time_restrictions " +
            "FROM t_{base_domain} base " +
            "LEFT JOIN t_{base_domain}_{module}_ext ext ON base.{base_domain}_id = ext.{base_domain}_id " +
            "WHERE base.deleted_flag = 0 " +
            "<if test='status != null'>" +
            "AND base.status = #{status} " +
            "</if>" +
            "<if test='{module}Level != null'>" +
            "AND ext.{module}_level >= #{moduleLevel} " +
            "</if>")
    List<{BaseDomain}{Module}VO> select{BaseDomain}{Module}List(
            @Param("status") Integer status,
            @Param("{module}Level") Integer {module}Level);

    /**
     * 统计查询 (基于现有统计方法抽象)
     */
    @Select("SELECT COUNT(*) FROM t_{base_domain}_{module}_ext " +
            "WHERE deleted_flag = 0 " +
            "<if test='{module}Level != null'>" +
            "AND {module}_level = #{moduleLevel}" +
            "</if>")
    int countBy{Module}Level(@Param("{module}Level") Integer {module}Level);

    /**
     * 插入或更新 (基于现有upsert模式)
     */
    @Insert("<script>" +
            "INSERT INTO t_{base_domain}_{module}_ext " +
            "({base_domain}_id, {module}_level, {module}_mode, time_restrictions, " +
            " create_time, update_time, create_user_id) " +
            "VALUES (#{baseDomainId}, #{moduleLevel}, #{moduleMode}, #{timeRestrictions}, " +
            " #{createTime}, #{updateTime}, #{createUserId}) " +
            "ON DUPLICATE KEY UPDATE " +
            "{module}_level = VALUES({module}_level), " +
            "{module}_mode = VALUES({module}_mode), " +
            "time_restrictions = VALUES(time_restrictions), " +
            "update_time = VALUES(update_time), " +
            "update_user_id = VALUES(update_user_id)" +
            "</script>")
    int insertOrUpdate(@Param("baseDomainId") Long baseDomainId,
                       @Param("{module}Level") Integer {module}Level,
                       @Param("{module}Mode") String {module}Mode,
                       @Param("timeRestrictions") String timeRestrictions,
                       @Param("createTime") Long createTime,
                       @Param("updateTime") Long updateTime,
                       @Param("createUserId") Long createUserId);
}
```

### 3. Service层设计标准（基于现有Service模式增强）

**扩展表Service模板**:
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class {BaseDomain}{Module}ExtService extends BaseService {

    @Resource
    private {BaseDomain}{Module}ExtDao {baseDomain}{Module}ExtDao;

    @Resource
    private {BaseDomain}Dao {baseDomain}Dao;

    @Resource
    private {BaseDomain}{Module}CacheManager {baseDomain}{Module}CacheManager;

    /**
     * 获取{domain}的{module}扩展信息
     */
    public ResponseDTO<{BaseDomain}{Module}VO> get{BaseDomain}{Module}Info(Long {baseDomain}Id) {
        try {
            // 缓存检查 (基于现有缓存模式)
            {BaseDomain}{Module}VO cachedResult = {baseDomain}{Module}CacheManager.getInfo({baseDomain}Id);
            if (cachedResult != null) {
                return ResponseDTO.ok(cachedResult);
            }

            // 数据库查询
            {BaseDomain}Entity baseEntity = {baseDomain}Dao.selectById({baseDomain}Id);
            if (baseEntity == null) {
                return ResponseDTO.error("Data", "{domain}不存在");
            }

            {BaseDomain}{Module}ExtEntity extEntity = {baseDomain}{Module}ExtDao.selectBy{BaseDomain}Id({baseDomain}Id);

            // 组装结果 (基于现有组装模式)
            {BaseDomain}{Module}VO result = new {BaseDomain}{Module}VO();
            BeanUtils.copyProperties(baseEntity, result);
            if (extEntity != null) {
                BeanUtils.copyProperties(extEntity, result);
                result.setExtensionConfig(parseJsonConfig(extEntity.getExtensionConfig()));
            }

            // 缓存结果
            {baseDomain}{Module}CacheManager.setInfo({baseDomain}Id, result);

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取{domain}{module}扩展信息失败", e);
            return ResponseDTO.error("System", "系统异常");
        }
    }

    /**
     * 批量更新{module}扩展信息 (基于现有批量操作实践)
     */
    public ResponseDTO<Boolean> batchUpdate{Module}Extension(
            List<{BaseDomain}{Module}UpdateDTO> updateList) {
        try {
            if (CollectionUtils.isEmpty(updateList)) {
                return ResponseDTO.error("ParamError", "参数不能为空");
            }

            // 批量处理 (基于现有批量事务模式)
            for ({BaseDomain}{Module}UpdateDTO updateDTO : updateList) {
                update{Module}ExtensionInternal(updateDTO);
            }

            // 清理缓存 (基于现有缓存失效策略)
            List<Long> {baseDomain}Ids = updateList.stream()
                    .map({BaseDomain}{Module}UpdateDTO::get{BaseDomain}Id)
                    .collect(Collectors.toList());
            {baseDomain}{Module}CacheManager.batchEvict({baseDomain}Ids);

            return ResponseDTO.ok(true);
        } catch (Exception e) {
            log.error("批量更新{module}扩展信息失败", e);
            throw new SmartException("批量更新失败");
        }
    }

    /**
     * 内部更新方法 (基于现有内部方法模式)
     */
    private void update{Module}ExtensionInternal({BaseDomain}{Module}UpdateDTO updateDTO) {
        Long currentTime = System.currentTimeMillis();

        // 设置默认值 (避免配置冗余)
        if (updateDTO.get{Module}Level() == null) {
            updateDTO.set{Module}Level(1);
        }

        {baseDomain}{Module}ExtDao.insertOrUpdate(
                updateDTO.get{BaseDomain}Id(),
                updateDTO.get{Module}Level(),
                updateDTO.get{Module}Mode(),
                updateDTO.getTimeRestrictions(),
                currentTime,
                currentTime,
                LoginContext.getUserId()
        );
    }

    /**
     * 解析JSON配置 (基于现有JSON处理模式)
     */
    private Map<String, Object> parseJsonConfig(String jsonConfig) {
        if (StringUtils.isBlank(jsonConfig)) {
            return new HashMap<>();
        }
        try {
            return JSON.parseObject(jsonConfig, Map.class);
        } catch (Exception e) {
            log.warn("解析{module}配置失败: {}", jsonConfig, e);
            return new HashMap<>();
        }
    }
}
```

---

## 🚫 代码冗余避免标准

### 1. 字段冗余避免（基于现有成功实践）

**✅ 正确做法**:
```java
// 基础字段统一继承BaseEntity，避免重复定义
public class AreaEntity extends BaseEntity {
    // 以下字段由BaseEntity提供，无需重复定义
    // private Long createTime;     // ❌ 冗余
    // private Long updateTime;     // ❌ 冗余
    // private Integer deletedFlag; // ❌ 冗余
}

// 扩展表只包含业务特有字段
public class AccessAreaExtEntity extends BaseEntity {
    private Long areaId;          // ✅ 业务字段
    private String accessConfig;  // ✅ JSON配置避免字段冗余
}
```

**❌ 错误做法**:
```java
// 重复定义审计字段
public class SomeEntity extends BaseEntity {
    private Long createTime;      // ❌ BaseEntity已包含
    private Long updateTime;      // ❌ BaseEntity已包含
    private Integer deletedFlag;  // ❌ BaseEntity已包含
}

// 为每个配置项创建独立字段
public class SomeExtensionEntity {
    private String workdayStartTime;  // ❌ 应该使用JSON配置
    private String workdayEndTime;    // ❌ 应该使用JSON配置
    private String weekendStartTime;  // ❌ 应该使用JSON配置
    private String weekendEndTime;    // ❌ 应该使用JSON配置
}
```

### 2. 方法冗余避免（基于现有最佳实践）

**✅ 正确做法**:
```java
// 使用JSON配置避免方法冗余
public class AreaExtensionEntity {
    private String timeRestrictions;  // JSON: {"workdays":["07:00-09:00"],"weekends":["09:00-21:00"]}

    // 通用的配置解析方法
    public Map<String, Object> getTimeRestrictions() {
        return parseJsonConfig(this.timeRestrictions);
    }

    // 通用的判断方法
    public boolean hasTimeRestrictions() {
        return StringUtils.isNotBlank(this.timeRestrictions);
    }
}
```

**❌ 错误做法**:
```java
// 为每个时间场景创建独立方法
public class AreaExtensionEntity {
    public boolean isWorkdayTimeInRange() {
        // ❌ 重复的时间判断逻辑
    }

    public boolean isWeekendTimeInRange() {
        // ❌ 重复的时间判断逻辑
    }

    public boolean isHolidayTimeInRange() {
        // ❌ 重复的时间判断逻辑
    }
}
```

### 3. 配置冗余避免（基于现有成功模式）

**✅ 正确做法**:
```java
// 使用分层配置避免冗余
public class DeviceEntity {
    private String configJson;        // 基础配置
    private String extensionConfig;   // 扩展配置

    // 提供配置获取的通用方法
    public Map<String, Object> getBaseConfig() {
        return parseJsonConfig(this.configJson);
    }

    public Map<String, Object> getExtensionConfig() {
        return parseJsonConfig(this.extensionConfig);
    }

    // 合并配置
    public Map<String, Object> getFullConfig() {
        Map<String, Object> fullConfig = new HashMap<>(getBaseConfig());
        fullConfig.putAll(getExtensionConfig());
        return fullConfig;
    }
}
```

---

## 📋 实施检查清单

### ✅ 设计阶段检查

**基础表设计**:
- [ ] 继承BaseEntity，避免重复定义审计字段
- [ ] 包含必要的基础标识字段 (code, name, status)
- [ ] 遵循统一的字段命名规范
- [ ] 设计合理的索引策略

**扩展表设计**:
- [ ] 外键字段名与关联表主键保持一致
- [ ] 业务特有字段避免与基础表重复
- [ ] 复杂配置使用JSON字段存储
- [ ] 提供业务方法封装复杂逻辑

### ✅ 代码实现检查

**实体类**:
- [ ] 避免重复定义BaseEntity已包含的字段
- [ ] 提供合理的默认值设置方法
- [ ] 实现必要的业务判断方法
- [ ] 使用JSON配置避免字段冗余

**DAO层**:
- [ ] 提供高效的关联查询方法
- [ ] 实现批量操作方法
- [ ] 使用合理的索引优化查询
- [ ] 避免重复的SQL实现

**Service层**:
- [ ] 实现统一的缓存管理策略
- [ ] 提供批量事务处理
- [ ] 封装复杂的业务逻辑
- [ ] 使用JSON处理避免配置冗余

### ✅ 性能优化检查

**查询优化**:
- [ ] 合理使用索引覆盖查询
- [ ] 避免N+1查询问题
- [ ] 使用批量查询替代循环查询
- [ ] 实现查询结果缓存

**缓存策略**:
- [ ] 实现分层缓存 (L1+L2)
- [ ] 提供合理的缓存失效策略
- [ ] 避免缓存穿透和雪崩
- [ ] 监控缓存命中率

---

## 📊 效果预期

基于现有的成功实践和标准化改进，预期效果：

### 🎯 代码质量提升
- **字段冗余减少**: 通过统一BaseEntity继承，减少审计字段冗余
- **方法重复减少**: 通过JSON配置和通用方法，减少配置类方法冗余
- **配置重复减少**: 通过分层配置设计，减少配置定义冗余

### 🚀 开发效率提升
- **设计模板复用**: 标准化的设计模式可直接复用
- **代码生成支持**: 基于模板可生成标准代码框架
- **学习成本降低**: 统一的模式降低团队学习成本

### 🔧 维护成本降低
- **架构一致性**: 统一的设计降低维护复杂度
- **问题定位快速**: 标准化的模式便于问题定位
- **扩展容易**: 基于现有模式的扩展更加容易

---

**文档维护**: 本文档将基于项目实践持续更新和完善
**标准执行**: 所有新模块开发必须严格遵循本标准
**版本管理**: 标准变更需要版本控制和影响评估