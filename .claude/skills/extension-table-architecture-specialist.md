# 扩展表机制架构专家

**版本**: v1.0.0
**创建时间**: 2025-11-25
**最后更新**: 2025-11-25
**作者**: SmartAdmin架构治理委员会
**技能等级**: ★★★ 专家级
**适用角色**: 架构师、高级开发工程师、技术负责人
**预计学时**: 8小时

---

## 📋 技能概述

本技能专门负责确保IOE-DREAM项目中所有业务模块严格遵循**基础实体+扩展表**的架构设计模式，建立统一的扩展表机制架构规范，避免重复设计和架构不一致问题。

### 🎯 核心价值
- **架构一致性**: 确保所有模块遵循统一的扩展表设计模式
- **避免重复开发**: 通过扩展表机制复用基础实体设计
- **维护性提升**: 业务特有功能独立扩展，不影响基础架构
- **扩展性保证**: 新业务模块可以快速基于扩展表机制开发

### 🔧 技能范围
- 扩展表架构设计与实施
- 基础实体与扩展表关系建模
- 扩展表机制代码实现
- 架构一致性检查与修正

---

## 📚 知识要求

### 🎓 必备理论知识

#### 1. **数据库设计理论**
- **实体关系建模**: 掌握ER图设计和实体关系映射
- **数据库范式**: 理解第一范式、第二范式、第三范式在扩展表中的应用
- **索引优化**: 扩展表查询性能优化策略
- **事务管理**: 跨表操作的事务一致性设计

#### 2. **软件架构模式**
- **扩展表模式**: 基础实体+扩展表的组合设计模式
- **领域驱动设计(DDD)**: 实体建模和业务边界划分
- **微服务架构**: 服务边界划分和数据一致性
- **继承vs组合**: 正确选择继承和组合设计原则

#### 3. **业务理解**
- **智慧园区业务**: 门禁、考勤、消费、视频监控等业务域理解
- **一卡通系统**: 统一身份认证和权限管理的业务逻辑
- **多租户架构**: 支持多园区、多业务场景的架构设计

### 🛠️ 技术背景要求

#### **后端技术栈**
- **Java 17+**: 现代Java特性应用
- **Spring Boot 3.x**: 企业级应用开发框架
- **MyBatis-Plus**: ORM框架和复杂查询处理
- **MySQL 8.0+**: 数据库设计和优化

#### **架构工具**
- **UML建模**: 类图、ER图、序列图设计
- **API设计**: RESTful接口设计规范
- **缓存架构**: Redis+Caffeine多级缓存设计
- **DevOps**: CI/CD流程和自动化检查

---

## 🛠️ 操作步骤

### **第一步：基础实体分析 (1小时)**

#### 1.1 识别项目中的基础实体
```bash
# 搜索所有基础实体
find . -name "*Entity.java" -exec grep -l "extends BaseEntity" {} \;

# 分析基础实体的职责和字段
grep -n "private.*" */BaseEntity.java
```

#### 1.2 评估现有基础实体设计
**检查要点**:
- ✅ 基础实体是否包含通用的业务字段
- ✅ 是否遵循合理的继承层次
- ✅ 字段命名是否规范统一
- ✅ 是否包含必要的审计字段

**示例基础实体评估**:
```java
// ✅ 优秀的基础实体设计
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_area")
public class AreaEntity extends BaseEntity {
    @TableId("area_id")
    private Long areaId;

    @NotBlank
    private String areaCode;

    @NotBlank
    private String areaName;

    @NotNull
    private Integer areaType;

    private Long parentId;
    private String path;
    private Integer level;
    // 基础区域通用字段
}
```

### **第二步：扩展表机制设计 (2小时)**

#### 2.1 扩展表设计模式

**标准扩展表模板**:
```sql
-- 扩展表通用模板
CREATE TABLE `t_{module}_ext` (
  `ext_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '扩展ID',
  `{module}_id` BIGINT NOT NULL COMMENT '关联{module}ID',

  -- 业务特有字段区域
  `{business_field1}` {field_type} COMMENT '业务字段1',
  `{business_field2}` {field_type} COMMENT '业务字段2',
  `{business_field3}` {field_type} COMMENT '业务字段3',

  -- 标准扩展字段
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标识',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

  PRIMARY KEY (`ext_id`),
  UNIQUE KEY `uk_{module}_id` (`{module}_id`, `deleted_flag`),
  KEY `idx_{business_field1}` (`{business_field1}`, `deleted_flag`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='{module}扩展表';
```

#### 2.2 扩展表命名规范

**命名规则**:
```sql
-- 基础表: t_{business_domain}
-- 扩展表: t_{business_domain}_ext
-- 关联表: t_{domain1}_{domain2}

-- 示例:
t_area (基础区域表)
t_area_ext (区域扩展表)
t_access_area_ext (门禁区域扩展表)
t_consume_area_ext (消费区域扩展表)
```

#### 2.3 扩展表字段设计原则

**字段设计原则**:
- **业务特有**: 扩展表只包含业务模块特有的字段
- **JSON配置**: 复杂配置使用JSON字段存储
- **类型安全**: 明确定义字段类型和约束
- **索引优化**: 为常用查询字段添加索引

**JSON字段示例**:
```sql
-- 复杂配置字段示例
`time_restrictions` TEXT COMMENT '时间限制配置(JSON格式)',
  -- 示例JSON结构:
  -- {
  --   "work_days": [1,2,3,4,5],
  --   "work_start": "09:00",
  --   "work_end": "18:00",
  --   "lunch_break": {"start": "12:00", "end": "13:00"}
  -- }

`alert_config` TEXT COMMENT '告警配置(JSON格式)',
  -- 示例JSON结构:
  -- {
  --   "temperature_threshold": 75.0,
  --   "offline_timeout": 300,
  --   "notification_methods": ["email", "sms", "push"]
  -- }
```

### **第三步：Java实体层实现 (2小时)**

#### 3.1 扩展表实体设计

**标准扩展实体模板**:
```java
/**
 * {Module}扩展实体类
 * <p>
 * 扩展表机制实现，存储{module}特有的业务字段
 * 通过关联{module}_id与基础实体建立关系
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_{module}_ext")
public class {Module}ExtensionEntity extends BaseEntity {

    /**
     * 扩展ID
     */
    @TableId(type = IdType.AUTO)
    private Long extId;

    /**
     * 关联{module}ID
     */
    @NotNull(message = "{module}ID不能为空")
    private Long {moduleId};

    // ==================== 业务特有字段 ====================

    /**
     * 业务字段1
     */
    private {fieldType} {businessField1};

    /**
     * 业务字段2
     */
    private {fieldType} {businessField2};

    /**
     * 业务字段3 (JSON配置)
     */
    private String {configField};
}
```

#### 3.2 VO对象设计

**组合VO设计模式**:
```java
/**
 * {Module}完整信息VO
 * <p>
 * 组合基础实体和扩展表信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {Module}FullVO {

    /**
     * 基础信息
     */
    private {Module}VO baseInfo;

    /**
     * 扩展信息
     */
    private {Module}ExtensionVO extensionInfo;

    /**
     * 组合字段（可选）
     */
    private String {combinedField};

    /**
     * 创建组合VO
     */
    public static {Module}FullVO create({Module}Entity entity, {Module}ExtensionEntity extension) {
        return {Module}FullVO.builder()
            .baseInfo(convertToVO(entity))
            .extensionInfo(convertToVO(extension))
            .{combinedFieldMethod}()
            .build();
    }
}
```

### **第四步：服务层实现 (1.5小时)**

#### 4.1 扩展表服务设计

**标准扩展服务模板**:
```java
/**
 * {Module}扩展服务
 * <p>
 * 处理{module}的扩展表业务逻辑
 * 采用基础+扩展的组合设计模式
 */
@Service
@Slf4j
public class {Module}ExtensionService {

    @Resource
    private {Module}Dao {module}Dao;

    @Resource
    private {Module}ExtensionDao {module}ExtensionDao;

    @Resource
    private {Module}CacheManager {module}CacheManager;

    /**
     * 获取{module}完整信息
     *
     * @param {moduleId} {module}ID
     * @return 完整信息VO
     */
    @Cacheable(value = "{module}:full", key = "#{{moduleId}}")
    public ResponseDTO<{Module}FullVO> getFullInfo(Long {moduleId}) {
        try {
            // 1. 获取基础信息
            {Module}Entity entity = {module}Dao.selectById({moduleId});
            if (entity == null) {
                return ResponseDTO.error("{module}不存在");
            }

            // 2. 获取扩展信息
            {Module}ExtensionEntity extension = {module}ExtensionDao.selectBy{Module}Id({moduleId});

            // 3. 组装完整信息
            {Module}FullVO fullVO = {Module}FullVO.create(entity, extension);

            return ResponseDTO.ok(fullVO);

        } catch (Exception e) {
            log.error("获取{module}完整信息失败: {moduleId}", e);
            return ResponseDTO.error("获取信息失败: " + e.getMessage());
        }
    }

    /**
     * 保存{module}完整信息
     *
     * @param dto 完整信息DTO
     * @return 保存结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> saveFullInfo({Module}FullDTO dto) {
        try {
            // 1. 保存基础信息
            {Module}Entity entity = convertToEntity(dto.getBaseInfo());
            {module}Dao.insertOrUpdate(entity);

            // 2. 保存扩展信息
            if (dto.getExtensionInfo() != null) {
                {Module}ExtensionEntity extension = convertToExtensionEntity(dto.getExtensionInfo());
                extension.set{Module}Id(entity.get{Module}Id());
                {module}ExtensionDao.insertOrUpdate(extension);
            }

            // 3. 清除缓存
            {module}CacheManager.evictCache(entity.get{Module}Id());

            return ResponseDTO.ok(entity.get{Module}Id());

        } catch (Exception e) {
            log.error("保存{module}完整信息失败", e);
            throw new {Module}Exception("保存失败: " + e.getMessage());
        }
    }

    /**
     * 批量查询扩展信息
     *
     * @param {module}Ids {module}ID列表
     * @return 扩展信息Map
     */
    public Map<Long, {Module}ExtensionEntity> batchGetExtensions(List<Long> {module}Ids) {
        if ({module}Ids == null || {module}Ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<{Module}ExtensionEntity> extensions = {module}ExtensionDao.selectBy{Module}Ids({module}Ids);

        return extensions.stream()
            .collect(Collectors.toMap(
                {Module}ExtensionEntity::get{Module}Id,
                Function.identity()
            ));
    }
}
```

#### 4.2 DAO层扩展查询

**扩展表DAO设计**:
```java
/**
 * {Module}扩展DAO
 * <p>
 * 处理扩展表的复杂查询操作
 */
@Mapper
public interface {Module}ExtensionDao extends BaseMapper<{Module}ExtensionEntity> {

    /**
     * 根据{module}ID查询扩展信息
     *
     * @param {moduleId} {module}ID
     * @return 扩展实体
     */
    @Select("SELECT * FROM t_{module}_ext WHERE {module}_id = #{moduleId} AND deleted_flag = 0")
    {Module}ExtensionEntity selectBy{Module}Id(@Param("{moduleId}") Long {moduleId});

    /**
     * 批量查询扩展信息
     *
     * @param {module}Ids {module}ID列表
     * @return 扩展实体列表
     */
    @Select("<script>" +
            "SELECT * FROM t_{module}_ext " +
            "WHERE {module}_id IN " +
            "<foreach collection='{module}Ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    List<{Module}ExtensionEntity> selectBy{Module}Ids(@Param("{module}Ids") List<Long> {module}Ids);

    /**
     * 关联查询完整信息
     *
     * @param {moduleId} {module}ID
     * @return 完整信息Map
     */
    @Select("SELECT " +
            "b.* as base_, " +
            "e.* as ext_ " +
            "FROM t_{module} b " +
            "LEFT JOIN t_{module}_ext e ON b.{module}_id = e.{module}_id AND e.deleted_flag = 0 " +
            "WHERE b.{module}_id = #{moduleId} AND b.deleted_flag = 0")
    Map<String, Object> selectFullInfo(@Param("{moduleId}") Long {moduleId});
}
```

### **第五步：缓存层设计 (1小时)**

#### 5.1 扩展表缓存策略

**多级缓存设计**:
```java
/**
 * {Module}扩展缓存管理器
 * <p>
 * 采用L1本地缓存 + L2分布式缓存的组合策略
 * 支持基础信息和扩展信息的分别缓存
 */
@Component
@Slf4j
public class {Module}CacheManager extends BaseCacheManager {

    // 缓存键模板
    private static final String CACHE_KEY_BASE = "{module}:base:{}";
    private static final String CACHE_KEY_EXT = "{module}:ext:{}";
    private static final String CACHE_KEY_FULL = "{module}:full:{}";

    // 本地缓存 (L1)
    private final Cache<String, {Module}Entity> baseCache;
    private final Cache<String, {Module}ExtensionEntity> extCache;

    // 分布式缓存 (L2)
    @Resource
    private UnifiedCacheService distributedCache;

    public {Module}CacheManager() {
        // 配置本地缓存 (Caffeine)
        this.baseCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

        this.extCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();
    }

    /**
     * 获取完整信息 (组合缓存策略)
     */
    public {Module}FullVO getFullInfo(Long {moduleId}) {
        String cacheKey = String.format(CACHE_KEY_FULL, {moduleId});

        // L1缓存查找
        {Module}FullVO cached = ({Module}FullVO) distributedCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // L2缓存查找
        {Module}Entity baseEntity = baseCache.getIfPresent(String.format(CACHE_KEY_BASE, {moduleId}));
        {Module}ExtensionEntity extEntity = extCache.getIfPresent(String.format(CACHE_KEY_EXT, {moduleId}));

        if (baseEntity != null) {
            cached = {Module}FullVO.create(baseEntity, extEntity);

            // 写入L2缓存
            distributedCache.set(cacheKey, cached, 30);

            return cached;
        }

        return null;
    }

    /**
     * 清除相关缓存
     */
    public void evictCache(Long {moduleId}) {
        // 清除本地缓存
        baseCache.invalidate(String.format(CACHE_KEY_BASE, {moduleId}));
        extCache.invalidate(String.format(CACHE_KEY_EXT, {moduleId}));

        // 清除分布式缓存
        distributedCache.delete(String.format(CACHE_KEY_BASE, {moduleId}));
        distributedCache.delete(String.format(CACHE_KEY_EXT, {moduleId}));
        distributedCache.delete(String.format(CACHE_KEY_FULL, {moduleId}));

        log.debug("清除{module}缓存: {}", {moduleId});
    }
}
```

### **第六步：API层设计 (0.5小时)**

#### 6.1 统一扩展API

**扩展表API设计模式**:
```java
/**
 * {Module}扩展信息控制器
 * <p>
 * 提供扩展表相关的API接口
 * 遵循RESTful设计规范
 */
@RestController
@RequestMapping("/api/{module}/extension")
@Tag(name = "{Module}扩展管理", description = "{Module}扩展信息管理相关接口")
@Validated
@Slf4j
public class {Module}ExtensionController {

    @Resource
    private {Module}ExtensionService {module}ExtensionService;

    /**
     * 获取{module}完整信息
     *
     * @param {moduleId} {module}ID
     * @return 完整信息
     */
    @GetMapping("/full/{{moduleId}}")
    @Operation(summary = "获取{module}完整信息", description = "获取{module}的基础信息和扩展信息")
    @SaCheckPermission("{module}:extension:query")
    public ResponseDTO<{Module}FullVO> getFullInfo(
            @Parameter(description = "{module}ID", required = true)
            @PathVariable Long {moduleId}) {

        return {module}ExtensionService.getFullInfo({moduleId});
    }

    /**
     * 保存{module}完整信息
     *
     * @param dto 完整信息DTO
     * @return 保存结果
     */
    @PostMapping("/full")
    @Operation(summary = "保存{module}完整信息", description = "保存{module}的基础信息和扩展信息")
    @SaCheckPermission("{module}:extension:save")
    public ResponseDTO<Long> saveFullInfo(@Valid @RequestBody {Module}FullDTO dto) {

        return {module}ExtensionService.saveFullInfo(dto);
    }

    /**
     * 批量获取扩展信息
     *
     * @param {module}Ids {module}ID列表
     * @return 扩展信息列表
     */
    @PostMapping("/batch")
    @Operation(summary = "批量获取扩展信息", description = "根据{module}ID列表批量获取扩展信息")
    @SaCheckPermission("{module}:extension:query")
    public ResponseDTO<List<{Module}ExtensionVO>> batchGetExtensions(
            @Parameter(description = "{module}ID列表", required = true)
            @RequestBody @NotNull List<Long> {module}Ids) {

        Map<Long, {Module}ExtensionEntity> extensionMap =
            {module}ExtensionService.batchGetExtensions({module}Ids);

        List<{Module}ExtensionVO> result = {module}Ids.stream()
            .map(id -> convertToVO(extensionMap.get(id)))
            .collect(Collectors.toList());

        return ResponseDTO.ok(result);
    }
}
```

---

## ⚠️ 注意事项

### 🚨 **必须避免的设计错误**

#### 1. **错误使用继承代替扩展表**
```java
// ❌ 错误：直接继承基础实体重复字段
public class AccessAreaEntity extends BaseEntity {
    private Long areaId;           // 重复AreaEntity的字段
    private String areaCode;       // 重复AreaEntity的字段
    private String areaName;       // 重复AreaEntity的字段
    private Integer accessLevel;  // 门禁特有字段
}

// ✅ 正确：使用扩展表机制
public class AreaEntity extends BaseEntity {
    private Long areaId;
    private String areaCode;
    private String areaName;
    // 基础区域字段
}

// 扩展表 t_access_area_ext 存储accessLevel等门禁特有字段
```

#### 2. **扩展表包含基础字段**
```sql
-- ❌ 错误：扩展表包含基础字段
CREATE TABLE `t_area_ext` (
  `ext_id` BIGINT NOT NULL AUTO_INCREMENT,
  `area_id` BIGINT NOT NULL,
  `area_code` VARCHAR(32),  -- 错误：基础字段
  `area_name` VARCHAR(100), -- 错误：基础字段
  `access_level` INT,        -- 正确：扩展字段
  ...
);

-- ✅ 正确：扩展表只包含扩展字段
CREATE TABLE `t_area_ext` (
  `ext_id` BIGINT NOT NULL AUTO_INCREMENT,
  `area_id` BIGINT NOT NULL,
  `access_level` INT,        -- 正确：扩展字段
  `access_mode` VARCHAR(50),  -- 正确：扩展字段
  ...
);
```

#### 3. **缺乏事务一致性保证**
```java
// ❌ 错误：缺乏事务管理
public ResponseDTO<Long> saveInfo({Module}FullDTO dto) {
    {Module}Entity entity = convertToEntity(dto.getBaseInfo());
    {module}Dao.insert(entity);  // 可能成功

    {Module}ExtensionEntity extension = convertToExtensionEntity(dto.getExtensionInfo());
    {module}ExtensionDao.insert(extension);  // 可能失败，但基础数据已保存
}

// ✅ 正确：使用事务保证一致性
@Transactional(rollbackFor = Exception.class)
public ResponseDTO<Long> saveInfo({Module}FullDTO dto) {
    {Module}Entity entity = convertToEntity(dto.getBaseInfo());
    {module}Dao.insert(entity);

    {Module}ExtensionEntity extension = convertToExtensionEntity(dto.getExtensionInfo());
    {module}ExtensionDao.insert(extension);  // 失败时整个事务回滚
}
```

### 🔒 **安全要求**

#### 1. **数据访问安全**
- 所有扩展表查询必须包含 `deleted_flag = 0` 条件
- 扩展表更新操作需要严格的权限验证
- 敏感配置字段需要加密存储

#### 2. **缓存安全**
- 分布式缓存需要设置合理的过期时间
- 缓存键需要避免冲突和重复
- 敏感数据不适合放入缓存

#### 3. **JSON字段安全**
- JSON配置字段需要进行格式验证
- 防止JSON注入攻击
- 配置字段大小需要限制

---

## 📊 评估标准

### ✅ **完成标准**

#### **架构设计完成度 (100%)**
- [ ] 基础实体设计合理，职责清晰
- [ ] 扩展表机制正确实现，符合设计规范
- [ ] 数据库设计遵循扩展表模式
- [ ] Java实体层正确映射扩展表关系

#### **代码实现质量 (100%)**
- [ ] 服务层完整实现基础+扩展的组合操作
- [ ] 缓存层正确处理扩展信息的缓存策略
- [ ] API层提供完整的扩展信息管理接口
- [ ] 异常处理和事务管理完善

#### **性能优化完成度 (90%)**
- [ ] 扩展表查询性能优化（索引、批量查询）
- [ ] 缓存策略合理，命中率高
- [ ] 数据库连接池配置合理
- [ ] 大数据量操作优化

#### **代码规范符合度 (100%)**
- [ ] 命名规范统一，符合项目标准
- [ ] 注释完整，包含扩展表设计说明
- [ ] 代码结构清晰，易于维护
- [ ] 遵循项目编码规范

### 🎯 **成功标准**

#### **功能完整性**
- **CRUD操作**: 支持基础和扩展信息的完整CRUD操作
- **组合查询**: 支持基础+扩展信息的组合查询
- **批量操作**: 支持批量扩展信息操作
- **缓存支持**: 支持扩展信息的多级缓存

#### **性能指标**
- **查询响应**: 单次完整信息查询 P95 ≤ 100ms
- **批量查询**: 100条扩展信息批量查询 ≤ 500ms
- **缓存命中**: 扩展信息缓存命中率 ≥ 85%
- **并发支持**: 支持至少 100 QPS 的并发访问

#### **可维护性**
- **代码复用**: 扩展表机制代码复用率 ≥ 80%
- **扩展性**: 新业务模块基于扩展表机制开发时间 ≤ 2天
- **一致性**: 所有扩展表实现遵循统一规范
- **文档完整性**: 扩展表设计文档完整度 100%

---

## 🔗 **相关技能引用**

### **前置技能**
- **database-design-specialist**: 数据库设计专家（扩展表依赖数据库设计能力）
- **four-tier-architecture-guardian**: 四层架构守护专家（确保架构一致性）

### **协作技能**
- **code-quality-protector**: 代码质量守护专家（确保扩展表代码质量）
- **business-module-developer**: 业务模块开发专家（扩展表业务逻辑实现）

### **后续技能**
- **automated-code-quality-checker**: 自动化代码质量检查专家（扩展表规范自动检查）
- **tech-stack-unification-specialist**: 技术栈统一化专家（确保扩展表机制技术栈一致）

---

## 💡 **最佳实践**

### **1. 扩展表字段设计**
```sql
-- ✅ 推荐：业务字段明确分类
CREATE TABLE `t_area_ext` (
  `area_id` BIGINT NOT NULL,
  -- 权限控制字段
  `access_level` INT NOT NULL DEFAULT 1 COMMENT '访问级别',
  `access_mode` VARCHAR(50) COMMENT '访问模式',
  -- 设备配置字段
  `device_count` INT NOT NULL DEFAULT 0 COMMENT '关联设备数量',
  `device_config` JSON COMMENT '设备配置',
  -- 安全控制字段
  `guard_required` TINYINT(1) DEFAULT 0 COMMENT '是否需要安保',
  `emergency_access` TINYINT(1) DEFAULT 0 COMMENT '紧急访问权限',
  -- 配置字段
  `time_restrictions` TEXT COMMENT '时间限制配置',
  `alert_config` TEXT COMMENT '告警配置',
  ...
);
```

### **2. 服务层组合模式**
```java
// ✅ 推荐：服务组合模式
@Service
public class AreaService {

    // 基础服务
    @Resource private BaseAreaService baseAreaService;

    // 扩展服务
    @Resource private AreaExtensionService extensionService;

    // 组合查询
    public AreaFullVO getFullArea(Long areaId) {
        AreaEntity base = baseAreaService.getById(areaId);
        AreaExtensionEntity ext = extensionService.getByAreaId(areaId);

        return AreaFullVO.builder()
            .baseInfo(convertToVO(base))
            .extensionInfo(convertToVO(ext))
            .build();
    }
}
```

### **3. 缓存策略优化**
```java
// ✅ 推荐：分层缓存策略
@Component
public class AreaExtensionCacheManager {

    // L1本地缓存 (热数据)
    private final Cache<Long, AreaExtensionEntity> localCache;

    // L2分布式缓存 (温数据)
    @Resource private UnifiedCacheService distributedCache;

    public AreaExtensionEntity get(Long areaId) {
        // 先查本地缓存
        AreaExtensionEntity cached = localCache.getIfPresent(areaId);
        if (cached != null) {
            return cached;
        }

        // 再查分布式缓存
        String key = "area:ext:" + areaId;
        cached = (AreaExtensionEntity) distributedCache.get(key);
        if (cached != null) {
            localCache.put(areaId, cached);
            return cached;
        }

        return null;
    }
}
```

### **4. API设计规范**
```java
// ✅ 推荐：RESTful API设计
@RestController
@RequestMapping("/api/areas")
public class AreaController {

    // 基础信息API
    @GetMapping("/{areaId}")
    public ResponseDTO<AreaVO> getArea(@PathVariable Long areaId) { }

    // 扩展信息API
    @GetMapping("/{areaId}/extension")
    public ResponseDTO<AreaExtensionVO> getExtension(@PathVariable Long areaId) { }

    // 完整信息API
    @GetMapping("/{areaId}/full")
    public ResponseDTO<AreaFullVO> getFullInfo(@PathVariable Long areaId) { }

    // 批量查询API
    @PostMapping("/batch/extensions")
    public ResponseDTO<List<AreaExtensionVO>> batchGetExtensions(@RequestBody List<Long> areaIds) { }
}
```

---

**使用指南**: 当需要设计新业务模块的架构时，调用此技能确保采用正确的扩展表机制。当发现现有模块存在架构不一致问题时，使用此技能进行规范化改造。