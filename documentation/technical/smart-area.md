# 区域管理公共模块开发文档

> **📋 版本**: v1.0.0
> **📋 创建时间**: 2025-01-10
> **📋 适用范围**: 所有需要区域管理功能的业务模块

---

## 📖 模块概述

### 模块简介
smart-area 是 SmartAdmin 项目的区域管理公共模块，提供统一的区域层级管理、设备分组、人员区域归属等功能，支持多级区域结构和空间概念。

### 核心特性
- **多级区域结构**: 支持无限层级的区域树形结构
- **设备分组管理**: 根据区域对设备进行分组和管理
- **人员区域归属**: 支持人员所属区域的灵活配置
- **权限继承机制**: 区域权限自动向下继承
- **空间可视化**: 区域空间关系的可视化管理
- **智能推荐**: 基于使用习惯的区域设备推荐

---

## 🏗️ 架构设计

### 模块结构

```
smart-area/
├── controller/                    # 区域控制器
│   ├── AreaController.java                # 区域管理控制器
│   ├── AreaTreeController.java            # 区域树控制器
│   ├── AreaDeviceController.java         # 区域设备控制器
│   └── AreaUserController.java           # 区域人员控制器
├── service/                      # 区域服务层
│   ├── AreaService.java                    # 区域管理服务
│   ├── AreaTreeService.java                # 区域树服务
│   ├── AreaDeviceService.java              # 区域设备服务
│   └── AreaUserService.java                # 区域人员服务
├── manager/                      # 区域管理层
│   ├── AreaManager.java                    # 区域管理器
│   ├── AreaTreeManager.java                # 区域树管理器
│   ├── AreaPermissionManager.java          # 区域权限管理器
│   └── AreaRecommendationManager.java      # 区域推荐管理器
├── dao/                          # 区域数据层
│   ├── AreaDao.java                         # 区域DAO
│   ├── AreaTreeDao.java                     # 区域树DAO
│   ├── AreaDeviceDao.java                   # 区域设备DAO
│   └── AreaUserDao.java                     # 区域人员DAO
├── entity/                       # 区域实体
│   ├── AreaEntity.java                      # 区域实体
│   ├── AreaTreeEntity.java                  # 区域树实体
│   ├── AreaDeviceEntity.java                # 区域设备实体
│   └── AreaUserEntity.java                  # 区域人员实体
├── algorithm/                    # 区域算法
│   ├── AreaTreeAlgorithm.java                # 区域树算法
│   ├── AreaRecommendationAlgorithm.java     # 区域推荐算法
│   ├── AreaPermissionAlgorithm.java         # 区域权限算法
│   └── AreaSpatialAlgorithm.java             # 区域空间算法
└── visualization/               # 可视化组件
    ├── AreaMapService.java                  # 区域地图服务
    ├── AreaVisualizationService.java         # 区域可视化服务
    └── AreaHeatmapService.java              # 区域热力图服务
```

### 核心设计模式

```java
// 树形结构模式
@Component
public class AreaTreeManager {

    private final Map<Long, AreaTreeNode> nodeMap = new ConcurrentHashMap<>();

    /**
     * 构建区域树
     */
    public AreaTreeNode buildAreaTree(List<AreaEntity> areas) {
        nodeMap.clear();

        // 创建所有节点
        areas.forEach(area -> {
            AreaTreeNode node = AreaTreeNode.builder()
                .areaId(area.getAreaId())
                .areaName(area.getAreaName())
                .parentId(area.getParentId())
                .areaType(area.getAreaType())
                .areaLevel(area.getAreaLevel())
                .sortOrder(area.getSortOrder())
                .status(area.getStatus())
                .children(new ArrayList<>())
                .build();

            nodeMap.put(area.getAreaId(), node);
        });

        // 构建父子关系
        List<AreaTreeNode> rootNodes = new ArrayList<>();
        nodeMap.values().forEach(node -> {
            if (node.getParentId() == null || node.getParentId() == 0L) {
                rootNodes.add(node);
            } else {
                AreaTreeNode parentNode = nodeMap.get(node.getParentId());
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                }
            }
        });

        // 排序子节点
        rootNodes.forEach(this::sortChildren);

        return rootNodes.isEmpty() ? null : rootNodes.get(0);
    }

    /**
     * 获取区域完整路径
     */
    public List<AreaEntity> getAreaPath(Long areaId) {
        List<AreaEntity> path = new ArrayList<>();
        AreaTreeNode node = nodeMap.get(areaId);

        while (node != null) {
            AreaEntity area = areaDao.selectById(node.getAreaId());
            if (area != null) {
                path.add(0, area);
            }
            node = nodeMap.get(node.getParentId());
        }

        return path;
    }

    /**
     * 获取所有子区域ID
     */
    public Set<Long> getAllChildrenIds(Long areaId) {
        Set<Long> childrenIds = new HashSet<>();
        AreaTreeNode node = nodeMap.get(areaId);

        if (node != null) {
            collectChildrenIds(node, childrenIds);
        }

        return childrenIds;
    }

    private void sortChildren(AreaTreeNode node) {
        node.getChildren().sort(Comparator.comparingInt(AreaTreeNode::getSortOrder));
        node.getChildren().forEach(this::sortChildren);
    }

    private void collectChildrenIds(AreaTreeNode node, Set<Long> childrenIds) {
        childrenIds.add(node.getAreaId());
        node.getChildren().forEach(child -> collectChildrenIds(child, childrenIds));
    }
}

// 策略模式 - 区域权限验证
@Component
public class AreaPermissionValidator {

    private final Map<AreaType, AreaPermissionStrategy> strategyMap = new ConcurrentHashMap<>();

    public AreaPermissionValidator(List<AreaPermissionStrategy> strategies) {
        strategies.forEach(strategy ->
            strategyMap.put(strategy.getSupportedAreaType(), strategy));
    }

    /**
     * 验证区域访问权限
     */
    public boolean validateAreaAccess(Long userId, Long areaId, String permission) {
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null) {
            return false;
        }

        AreaPermissionStrategy strategy = strategyMap.get(AreaType.valueOf(area.getAreaType()));
        if (strategy == null) {
            throw new UnsupportedOperationException("不支持的区域类型: " + area.getAreaType());
        }

        return strategy.validate(userId, area, permission);
    }

    /**
     * 获取用户可访问的区域列表
     */
    public List<Long> getUserAccessibleAreaIds(Long userId, String permission) {
        List<Long> allAreaIds = areaDao.selectList(
            new QueryWrapper<AreaEntity>()
                .eq("status", 1)
                .eq("deleted_flag", 0)
        ).stream().map(AreaEntity::getAreaId).collect(Collectors.toList());

        return allAreaIds.stream()
            .filter(areaId -> validateAreaAccess(userId, areaId, permission))
            .collect(Collectors.toList());
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
### 区域表 (t_area)
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
CREATE TABLE t_area (
    area_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '区域ID',
    area_code VARCHAR(100) NOT NULL UNIQUE COMMENT '区域编码',
    area_name VARCHAR(200) NOT NULL COMMENT '区域名称',
    area_type VARCHAR(50) NOT NULL COMMENT '区域类型',
    area_level INT DEFAULT 1 COMMENT '区域层级',
    parent_id BIGINT DEFAULT 0 COMMENT '父区域ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    area_config JSON COMMENT '区域配置JSON',
    spatial_data GEOMETRY COMMENT '空间数据',
    area_desc TEXT COMMENT '区域描述',
    manager_id BIGINT COMMENT '区域负责人ID',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address TEXT COMMENT '详细地址',
    longitude DECIMAL(11, 8) COMMENT '经度',
    latitude DECIMAL(10, 8) COMMENT '纬度',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    version INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    INDEX idx_parent_id (parent_id),
    INDEX idx_area_type (area_type),
    INDEX idx_area_level (area_level),
    INDEX idx_status (status),
    INDEX idx_manager_id (manager_id),
    INDEX idx_location (longitude, latitude),
    SPATIAL INDEX idx_spatial (spatial_data),
    UNIQUE KEY uk_area_code (area_code)
) COMMENT = '区域表';

-- 区域类型枚举值
INSERT INTO t_sys_dict (dict_type, dict_key, dict_value, sort_order, remark) VALUES
('AREA_TYPE', 'CAMPUS', '园区', 1, '整个园区'),
('AREA_TYPE', 'BUILDING', '楼栋', 2, '园区内的楼栋'),
('AREA_TYPE', 'FLOOR', '楼层', 3, '楼栋内的楼层'),
('AREA_TYPE', 'ROOM', '房间', 4, '楼层内的房间'),
('AREA_TYPE', 'OUTDOOR', '室外', 5, '室外区域'),
('AREA_TYPE', 'PARKING', '停车场', 6, '停车场'),
('AREA_TYPE', 'ENTRANCE', '出入口', 7, '出入口区域');
```

### 区域设备关联表 (t_area_device)

```sql
CREATE TABLE t_area_device (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型',
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    bind_user_id BIGINT COMMENT '绑定人ID',
    unbind_time DATETIME COMMENT '解绑时间',
    unbind_user_id BIGINT COMMENT '解绑人ID',
    bind_remark TEXT COMMENT '绑定备注',
    status TINYINT DEFAULT 1 COMMENT '状态：1-绑定，0-解绑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_area_id (area_id),
    INDEX idx_device_id (device_id),
    INDEX idx_device_type (device_type),
    INDEX idx_bind_time (bind_time),
    INDEX idx_status (status),
    UNIQUE KEY uk_area_device (area_id, device_id)
) COMMENT = '区域设备关联表';
```

### 区域人员关联表 (t_area_user)

```sql
CREATE TABLE t_area_user (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_type VARCHAR(20) NOT NULL COMMENT '用户类型',
    relation_type VARCHAR(20) NOT NULL COMMENT '关联类型',
    access_level TINYINT DEFAULT 1 COMMENT '访问级别',
    access_time_config JSON COMMENT '访问时间配置JSON',
    valid_start_time DATETIME COMMENT '有效开始时间',
    valid_end_time DATETIME COMMENT '有效结束时间',
    grant_user_id BIGINT COMMENT '授权人ID',
    grant_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    revoke_user_id BIGINT COMMENT '撤销人ID',
    revoke_time DATETIME COMMENT '撤销时间',
    grant_remark TEXT COMMENT '授权备注',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效，0-已撤销',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_area_id (area_id),
    INDEX idx_user_id (user_id),
    INDEX idx_user_type (user_type),
    INDEX idx_relation_type (relation_type),
    INDEX idx_access_level (access_level),
    INDEX idx_valid_time (valid_start_time, valid_end_time),
    INDEX idx_status (status),
    INDEX idx_grant_time (grant_time),
    UNIQUE KEY uk_area_user (area_id, user_id, relation_type)
) COMMENT = '区域人员关联表';

-- 用户类型枚举值
INSERT INTO t_sys_dict (dict_type, dict_key, dict_value, sort_order, remark) VALUES
('USER_TYPE', 'EMPLOYEE', '员工', 1, '内部员工'),
('USER_TYPE', 'VISITOR', '访客', 2, '外部访客'),
('USER_TYPE', 'CONTRACTOR', '承包商', 3, '外部承包商'),
('USER_TYPE', 'SECURITY', '安保', 4, '安保人员');

-- 关联类型枚举值
INSERT INTO t_sys_dict (dict_type, dict_key, dict_value, sort_order, remark) VALUES
('RELATION_TYPE', 'ACCESS', '访问权限', 1, '可访问该区域'),
('RELATION_TYPE', 'MANAGE', '管理权限', 2, '可管理该区域'),
('RELATION_TYPE', 'MONITOR', '监控权限', 3, '可监控该区域');
```

### 区域配置表 (t_area_config)

```sql
CREATE TABLE t_area_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_desc TEXT COMMENT '配置描述',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密：1-是，0-否',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：1-是，0-否',
    version INT DEFAULT 1 COMMENT '配置版本',
    effective_time DATETIME COMMENT '生效时间',
    expire_time DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-生效，0-失效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_area_id (area_id),
    INDEX idx_config_type (config_type),
    INDEX idx_config_key (config_key),
    INDEX idx_status (status),
    INDEX idx_effective_time (effective_time),
    INDEX idx_expire_time (expire_time),
    UNIQUE KEY uk_area_config (area_id, config_key, version)
) COMMENT = '区域配置表';

-- 默认区域配置
INSERT INTO t_area_config (area_id, config_type, config_key, config_value, config_desc, is_default) VALUES
(0, 'AREA_VISIBILITY', 'show_empty_areas', 'true', '是否显示空区域', 1),
(0, 'DEVICE_MANAGEMENT', 'auto_bind_device', 'false', '自动绑定区域内的设备', 1),
(0, 'USER_MANAGEMENT', 'auto_grant_access', 'false', '自动授予区域访问权限', 1),
(0, 'SECURITY_CONFIG', 'access_control_level', '1', '访问控制级别', 1),
(0, 'NOTIFICATION_CONFIG', 'area_entry_notification', 'true', '区域进入通知', 1);
```

---

## 🔧 后端实现

### 核心控制器 (AreaController)

```java
@RestController
@RequestMapping("/api/area")
@Tag(name = "区域管理", description = "区域管理相关接口")
public class AreaController {

    @Resource
    private AreaService areaService;

    @GetMapping("/page")
    @Operation(summary = "分页查询区域")
    @SaCheckPermission("area:page")
    public ResponseDTO<PageResult<AreaVO>> queryPage(AreaQueryDTO queryDTO) {
        PageResult<AreaVO> result = areaService.queryPage(queryDTO);
        return ResponseDTO.ok(result);
    }

    @PostMapping
    @Operation(summary = "新增区域")
    @SaCheckPermission("area:add")
    public ResponseDTO<String> add(@Valid @RequestBody AreaCreateDTO createDTO) {
        areaService.add(createDTO);
        return ResponseDTO.ok();
    }

    @PutMapping("/{areaId}")
    @Operation(summary = "修改区域")
    @SaCheckPermission("area:update")
    public ResponseDTO<String> update(@PathVariable Long areaId,
                                     @Valid @RequestBody AreaUpdateDTO updateDTO) {
        updateDTO.setAreaId(areaId);
        areaService.update(updateDTO);
        return ResponseDTO.ok();
    }

    @DeleteMapping("/{areaId}")
    @Operation(summary = "删除区域")
    @SaCheckPermission("area:delete")
    public ResponseDTO<String> delete(@PathVariable Long areaId) {
        areaService.delete(areaId);
        return ResponseDTO.ok();
    }

    @GetMapping("/{areaId}")
    @Operation(summary = "获取区域详情")
    @SaCheckPermission("area:detail")
    public ResponseDTO<AreaDetailVO> getDetail(@PathVariable Long areaId) {
        AreaDetailVO detail = areaService.getDetail(areaId);
        return ResponseDTO.ok(detail);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取区域树")
    @SaCheckPermission("area:tree")
    public ResponseDTO<List<AreaTreeVO>> getAreaTree() {
        List<AreaTreeVO> tree = areaService.getAreaTree();
        return ResponseDTO.ok(tree);
    }

    @GetMapping("/path/{areaId}")
    @Operation(summary = "获取区域路径")
    @SaCheckLogin
    public ResponseDTO<List<AreaVO>> getAreaPath(@PathVariable Long areaId) {
        List<AreaVO> path = areaService.getAreaPath(areaId);
        return ResponseDTO.ok(path);
    }

    @PostMapping("/{areaId}/device/bind")
    @Operation(summary = "绑定设备到区域")
    @SaCheckPermission("area:device:bind")
    public ResponseDTO<String> bindDevice(@PathVariable Long areaId,
                                          @Valid @RequestBody AreaDeviceBindDTO bindDTO) {
        areaService.bindDevice(areaId, bindDTO);
        return ResponseDTO.ok();
    }

    @PostMapping("/{areaId}/device/unbind")
    @Operation(summary = "解绑设备从区域")
    @SaCheckPermission("area:device:unbind")
    public ResponseDTO<String> unbindDevice(@PathVariable Long areaId,
                                            @Valid @RequestBody AreaDeviceUnbindDTO unbindDTO) {
        areaService.unbindDevice(areaId, unbindDTO);
        return ResponseDTO.ok();
    }

    @GetMapping("/{areaId}/devices")
    @Operation(summary = "获取区域设备列表")
    @SaCheckPermission("area:device:list")
    public ResponseDTO<PageResult<AreaDeviceVO>> getAreaDevices(
            @PathVariable Long areaId,
            AreaDeviceQueryDTO queryDTO) {
        PageResult<AreaDeviceVO> result = areaService.getAreaDevices(areaId, queryDTO);
        return ResponseDTO.ok(result);
    }

    @PostMapping("/{areaId}/user/grant")
    @Operation(summary = "授予用户区域权限")
    @SaCheckPermission("area:user:grant")
    public ResponseDTO<String> grantUserAccess(@PathVariable Long areaId,
                                              @Valid @RequestBody AreaUserGrantDTO grantDTO) {
        areaService.grantUserAccess(areaId, grantDTO);
        return ResponseDTO.ok();
    }

    @PostMapping("/{areaId}/user/revoke")
    @Operation(summary = "撤销用户区域权限")
    @SaCheckPermission("area:user:revoke")
    public ResponseDTO<String> revokeUserAccess(@PathVariable Long areaId,
                                               @Valid @RequestBody AreaUserRevokeDTO revokeDTO) {
        areaService.revokeUserAccess(areaId, revokeDTO);
        return ResponseDTO.ok();
    }

    @GetMapping("/{areaId}/users")
    @Operation(summary = "获取区域用户列表")
    @SaCheckPermission("area:user:list")
    public ResponseDTO<PageResult<AreaUserVO>> getAreaUsers(
            @PathVariable Long areaId,
            AreaUserQueryDTO queryDTO) {
        PageResult<AreaUserVO> result = areaService.getAreaUsers(areaId, queryDTO);
        return ResponseDTO.ok(result);
    }

    @GetMapping("/user/accessible")
    @Operation(summary = "获取用户可访问的区域")
    @SaCheckLogin
    public ResponseDTO<List<AreaVO>> getUserAccessibleAreas(@RequestParam String permission) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        List<AreaVO> areas = areaService.getUserAccessibleAreas(userId, permission);
        return ResponseDTO.ok(areas);
    }

    @GetMapping("/user/{userId}/accessible")
    @Operation(summary = "获取指定用户可访问的区域")
    @SaCheckPermission("area:user:accessible")
    public ResponseDTO<List<AreaVO>> getUserAccessibleAreasById(
            @PathVariable Long userId,
            @RequestParam String permission) {
        List<AreaVO> areas = areaService.getUserAccessibleAreas(userId, permission);
        return ResponseDTO.ok(areas);
    }

    @PostMapping("/config/{areaId}")
    @Operation(summary = "更新区域配置")
    @SaCheckPermission("area:config:update")
    public ResponseDTO<String> updateAreaConfig(@PathVariable Long areaId,
                                                @Valid @RequestBody AreaConfigUpdateDTO configDTO) {
        areaService.updateAreaConfig(areaId, configDTO);
        return ResponseDTO.ok();
    }

    @GetMapping("/config/{areaId}")
    @Operation(summary = "获取区域配置")
    @SaCheckPermission("area:config:view")
    public ResponseDTO<List<AreaConfigVO>> getAreaConfig(@PathVariable Long areaId) {
        List<AreaConfigVO> configs = areaService.getAreaConfig(areaId);
        return ResponseDTO.ok(configs);
    }
}
```

### 核心服务层 (AreaService)

```java
@Service
@Transactional(readOnly = true)
public class AreaService {

    @Resource
    private AreaManager areaManager;
    @Resource
    private AreaTreeManager treeManager;
    @Resource
    private AreaDeviceService deviceService;
    @Resource
    private AreaUserService userService;
    @Resource
    private AreaPermissionManager permissionManager;

    public PageResult<AreaVO> queryPage(AreaQueryDTO queryDTO) {
        // 1. 验证查询参数
        validateQueryDTO(queryDTO);

        // 2. 执行查询
        PageResult<AreaEntity> result = areaManager.queryPage(queryDTO);

        // 3. 补充完整信息
        List<AreaVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return PageResult.<AreaVO>builder()
            .records(voList)
            .total(result.getTotal())
            .pageNum(result.getPageNum())
            .pageSize(result.getPageSize())
            .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(AreaCreateDTO createDTO) {
        // 1. 验证区域编码唯一性
        validateAreaCodeUnique(createDTO.getAreaCode());

        // 2. 验证父区域存在性
        if (createDTO.getParentId() != null && createDTO.getParentId() > 0) {
            validateParentAreaExists(createDTO.getParentId());
        }

        // 3. 计算区域层级
        Integer areaLevel = calculateAreaLevel(createDTO.getParentId());

        // 4. 创建区域实体
        AreaEntity area = AreaEntity.builder()
            .areaCode(createDTO.getAreaCode())
            .areaName(createDTO.getAreaName())
            .areaType(createDTO.getAreaType())
            .areaLevel(areaLevel)
            .parentId(createDTO.getParentId() != null ? createDTO.getParentId() : 0L)
            .sortOrder(createDTO.getSortOrder() != null ? createDTO.getSortOrder() : 0)
            .areaConfig(JsonUtils.toJsonString(createDTO.getAreaConfig()))
            .areaDesc(createDTO.getAreaDesc())
            .managerId(createDTO.getManagerId())
            .contactPhone(createDTO.getContactPhone())
            .address(createDTO.getAddress())
            .longitude(createDTO.getLongitude())
            .latitude(createDTO.getLatitude())
            .status(createDTO.getStatus())
            .version(1)
            .build();

        areaManager.add(area);

        // 5. 发布区域创建事件
        eventPublisher.publishEvent(new AreaCreateEvent(area.getAreaId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(AreaUpdateDTO updateDTO) {
        // 1. 验证区域存在性
        AreaEntity area = areaManager.getById(updateDTO.getAreaId());
        if (area == null) {
            throw new SmartException("区域不存在");
        }

        // 2. 验证编码唯一性（排除自身）
        validateAreaCodeUnique(updateDTO.getAreaCode(), updateDTO.getAreaId());

        // 3. 验证父区域（不能是自身或子区域）
        if (updateDTO.getParentId() != null) {
            validateParentArea(updateDTO.getAreaId(), updateDTO.getParentId());
        }

        // 4. 更新区域信息
        AreaEntity updateEntity = AreaEntity.builder()
            .areaId(updateDTO.getAreaId())
            .areaName(updateDTO.getAreaName())
            .areaType(updateDTO.getAreaType())
            .parentId(updateDTO.getParentId() != null ? updateDTO.getParentId() : 0L)
            .sortOrder(updateDTO.getSortOrder())
            .areaConfig(JsonUtils.toJsonString(updateDTO.getAreaConfig()))
            .areaDesc(updateDTO.getAreaDesc())
            .managerId(updateDTO.getManagerId())
            .contactPhone(updateDTO.getContactPhone())
            .address(updateDTO.getAddress())
            .longitude(updateDTO.getLongitude())
            .latitude(updateDTO.getLatitude())
            .status(updateDTO.getStatus())
            .version(area.getVersion() + 1)
            .build();

        areaManager.update(updateEntity);

        // 5. 更新区域层级
        updateAreaLevel(updateDTO.getAreaId());

        // 6. 发布区域更新事件
        eventPublisher.publishEvent(new AreaUpdateEvent(updateDTO.getAreaId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long areaId) {
        // 1. 验证区域存在性
        AreaEntity area = areaManager.getById(areaId);
        if (area == null) {
            throw new SmartException("区域不存在");
        }

        // 2. 验证是否有子区域
        List<AreaEntity> children = areaManager.getChildren(areaId);
        if (!children.isEmpty()) {
            throw new SmartException("该区域包含子区域，无法删除");
        }

        // 3. 验证是否有关联设备
        List<AreaDeviceEntity> areaDevices = deviceService.getAreaDevices(areaId);
        if (!areaDevices.isEmpty()) {
            throw new SmartException("该区域包含设备，无法删除");
        }

        // 4. 软删除区域
        areaManager.softDelete(areaId);

        // 5. 删除区域用户权限
        userService.deleteByAreaId(areaId);

        // 6. 发布区域删除事件
        eventPublisher.publishEvent(new AreaDeleteEvent(areaId));
    }

    public AreaDetailVO getDetail(Long areaId) {
        // 1. 获取区域基本信息
        AreaEntity area = areaManager.getById(areaId);
        if (area == null) {
            throw new SmartException("区域不存在");
        }

        // 2. 获取区域路径
        List<AreaEntity> path = treeManager.getAreaPath(areaId);

        // 3. 获取设备统计
        DeviceStatistics deviceStats = deviceService.getAreaDeviceStatistics(areaId);

        // 4. 获取用户统计
        UserStatistics userStats = userService.getAreaUserStatistics(areaId);

        // 5. 组装详情信息
        AreaDetailVO detail = convertToDetailVO(area);
        detail.setAreaPath(path.stream().map(this::convertToVO).collect(Collectors.toList()));
        detail.setDeviceStatistics(deviceStats);
        detail.setUserStatistics(userStats);

        return detail;
    }

    public List<AreaTreeVO> getAreaTree() {
        // 1. 获取所有区域
        List<AreaEntity> allAreas = areaManager.getAllAreas();

        // 2. 构建区域树
        AreaTreeNode rootNode = treeManager.buildAreaTree(allAreas);

        // 3. 转换为VO
        return convertToTreeVO(rootNode);
    }

    public List<AreaVO> getAreaPath(Long areaId) {
        // 1. 获取区域路径
        List<AreaEntity> path = treeManager.getAreaPath(areaId);

        // 2. 转换为VO
        return path.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindDevice(Long areaId, AreaDeviceBindDTO bindDTO) {
        // 1. 验证区域存在性
        validateAreaExists(areaId);

        // 2. 验证设备存在性
        validateDeviceExists(bindDTO.getDeviceId());

        // 3. 验证设备类型
        validateDeviceType(bindDTO.getDeviceType());

        // 4. 检查是否已绑定
        if (deviceService.isDeviceBound(areaId, bindDTO.getDeviceId())) {
            throw new SmartException("该设备已绑定到本区域");
        }

        // 5. 绑定设备
        deviceService.bindDevice(areaId, bindDTO);

        // 6. 发布设备绑定事件
        eventPublisher.publishEvent(new AreaDeviceBindEvent(areaId, bindDTO.getDeviceId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void unbindDevice(Long areaId, AreaDeviceUnbindDTO unbindDTO) {
        // 1. 验证区域存在性
        validateAreaExists(areaId);

        // 2. 验证设备已绑定
        if (!deviceService.isDeviceBound(areaId, unbindDTO.getDeviceId())) {
            throw new SmartException("该设备未绑定到本区域");
        }

        // 3. 解绑设备
        deviceService.unbindDevice(areaId, unbindDTO);

        // 4. 发布设备解绑事件
        eventPublisher.publishEvent(new AreaDeviceUnbindEvent(areaId, unbindDTO.getDeviceId()));
    }

    public PageResult<AreaDeviceVO> getAreaDevices(Long areaId, AreaDeviceQueryDTO queryDTO) {
        // 1. 验证区域存在性
        validateAreaExists(areaId);

        // 2. 获取包含子区域的区域ID列表
        Set<Long> areaIds = treeManager.getAllChildrenIds(areaId);
        areaIds.add(areaId); // 包含自身

        // 3. 查询设备列表
        queryDTO.setAreaIds(new ArrayList<>(areaIds));
        return deviceService.queryAreaDevices(queryDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void grantUserAccess(Long areaId, AreaUserGrantDTO grantDTO) {
        // 1. 验证区域存在性
        validateAreaExists(areaId);

        // 2. 验证用户存在性
        validateUserExists(grantDTO.getUserId());

        // 3. 验证关联类型
        validateRelationType(grantDTO.getRelationType());

        // 4. 检查是否已授权
        if (userService.hasUserAccess(areaId, grantDTO.getUserId(), grantDTO.getRelationType())) {
            throw new SmartException("该用户已拥有本区域的此权限");
        }

        // 5. 授予权限
        userService.grantUserAccess(areaId, grantDTO);

        // 6. 发布权限授予事件
        eventPublisher.publishEvent(new AreaUserGrantEvent(areaId, grantDTO.getUserId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void revokeUserAccess(Long areaId, AreaUserRevokeDTO revokeDTO) {
        // 1. 验证区域存在性
        validateAreaExists(areaId);

        // 2. 验证用户权限存在
        if (!userService.hasUserAccess(areaId, revokeDTO.getUserId(), revokeDTO.getRelationType())) {
            throw new SmartException("该用户未拥有本区域的此权限");
        }

        // 3. 撤销权限
        userService.revokeUserAccess(areaId, revokeDTO);

        // 4. 发布权限撤销事件
        eventPublisher.publishEvent(new AreaUserRevokeEvent(areaId, revokeDTO.getUserId()));
    }

    public PageResult<AreaUserVO> getAreaUsers(Long areaId, AreaUserQueryDTO queryDTO) {
        // 1. 验证区域存在性
        validateAreaExists(areaId);

        // 2. 获取包含子区域的区域ID列表
        Set<Long> areaIds = treeManager.getAllChildrenIds(areaId);
        areaIds.add(areaId); // 包含自身

        // 3. 查询用户列表
        queryDTO.setAreaIds(new ArrayList<>(areaIds));
        return userService.queryAreaUsers(queryDTO);
    }

    public List<AreaVO> getUserAccessibleAreas(Long userId, String permission) {
        // 1. 获取用户可访问的区域ID列表
        List<Long> accessibleAreaIds = permissionManager.getUserAccessibleAreaIds(userId, permission);

        // 2. 查询区域信息
        List<AreaEntity> areas = areaManager.getAreasByIds(accessibleAreaIds);

        // 3. 构建区域树并过滤
        return buildAccessibleAreaTree(areas, accessibleAreaIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAreaConfig(Long areaId, AreaConfigUpdateDTO configDTO) {
        // 1. 验证区域存在性
        validateAreaExists(areaId);

        // 2. 更新配置
        areaManager.updateAreaConfig(areaId, configDTO);

        // 3. 发布配置更新事件
        eventPublisher.publishEvent(new AreaConfigUpdateEvent(areaId));
    }

    public List<AreaConfigVO> getAreaConfig(Long areaId) {
        // 1. 验证区域存在性
        validateAreaExists(areaId);

        // 2. 获取区域配置
        return areaManager.getAreaConfig(areaId);
    }

    // 私有方法
    private void validateQueryDTO(AreaQueryDTO queryDTO) {
        if (queryDTO.getAreaType() != null) {
            validateAreaType(queryDTO.getAreaType());
        }
    }

    private void validateAreaCodeUnique(String areaCode) {
        validateAreaCodeUnique(areaCode, null);
    }

    private void validateAreaCodeUnique(String areaCode, Long excludeAreaId) {
        boolean exists = areaManager.checkAreaCodeExists(areaCode, excludeAreaId);
        if (exists) {
            throw new SmartException("区域编码已存在");
        }
    }

    private void validateParentAreaExists(Long parentId) {
        AreaEntity parentArea = areaManager.getById(parentId);
        if (parentArea == null) {
            throw new SmartException("父区域不存在");
        }
    }

    private void validateParentArea(Long areaId, Long parentId) {
        if (areaId.equals(parentId)) {
            throw new SmartException("父区域不能是自身");
        }

        // 检查不能选择子区域作为父区域
        Set<Long> childrenIds = treeManager.getAllChildrenIds(areaId);
        if (childrenIds.contains(parentId)) {
            throw new SmartException("不能选择子区域作为父区域");
        }
    }

    private void validateAreaExists(Long areaId) {
        AreaEntity area = areaManager.getById(areaId);
        if (area == null) {
            throw new SmartException("区域不存在");
        }
    }

    private void validateDeviceExists(Long deviceId) {
        DeviceEntity device = deviceManager.getById(deviceId);
        if (device == null) {
            throw new SmartException("设备不存在");
        }
    }

    private void validateDeviceType(String deviceType) {
        try {
            DeviceType.valueOf(deviceType);
        } catch (IllegalArgumentException e) {
            throw new SmartException("不支持的设备类型: " + deviceType);
        }
    }

    private void validateUserExists(Long userId) {
        UserEntity user = userManager.getById(userId);
        if (user == null) {
            throw new SmartException("用户不存在");
        }
    }

    private void validateRelationType(String relationType) {
        try {
            RelationType.valueOf(relationType);
        } catch (IllegalArgumentException e) {
            throw new SmartException("不支持的关联类型: " + relationType);
        }
    }

    private void validateAreaType(String areaType) {
        try {
            AreaType.valueOf(areaType);
        } catch (IllegalArgumentException e) {
            throw new SmartException("不支持的区域类型: " + areaType);
        }
    }

    private Integer calculateAreaLevel(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return 1; // 顶级区域
        }

        AreaEntity parentArea = areaManager.getById(parentId);
        if (parentArea == null) {
            throw new SmartException("父区域不存在");
        }

        return parentArea.getAreaLevel() + 1;
    }

    private void updateAreaLevel(Long areaId) {
        // 重新计算当前区域的层级
        AreaEntity area = areaManager.getById(areaId);
        Integer newLevel = calculateAreaLevel(area.getParentId());

        if (!newLevel.equals(area.getAreaLevel())) {
            area.setAreaLevel(newLevel);
            areaManager.update(area);
        }

        // 递归更新子区域层级
        List<AreaEntity> children = areaManager.getChildren(areaId);
        children.forEach(child -> updateAreaLevel(child.getAreaId()));
    }

    private List<AreaTreeVO> convertToTreeVO(AreaTreeNode rootNode) {
        List<AreaTreeVO> treeVos = new ArrayList<>();
        if (rootNode == null) {
            return treeVos;
        }

        // 构建当前节点VO
        AreaTreeVO vo = AreaTreeVO.builder()
            .areaId(rootNode.getAreaId())
            .areaCode(rootNode.getAreaCode())
            .areaName(rootNode.getAreaName())
            .areaType(rootNode.getAreaType())
            .areaLevel(rootNode.getAreaLevel())
            .parentId(rootNode.getParentId())
            .sortOrder(rootNode.getSortOrder())
            .status(rootNode.getStatus())
            .children(convertToTreeVOList(rootNode.getChildren()))
            .build();

        treeVos.add(vo);
        return treeVos;
    }

    private List<AreaTreeVO> convertToTreeVOList(List<AreaTreeNode> nodes) {
        return nodes.stream()
            .map(node -> AreaTreeVO.builder()
                .areaId(node.getAreaId())
                .areaCode(node.getAreaCode())
                .areaName(node.getAreaName())
                .areaType(node.getAreaType())
                .areaLevel(node.getAreaLevel())
                .parentId(node.getParentId())
                .sortOrder(node.getSortOrder())
                .status(node.getStatus())
                .children(convertToTreeVOList(node.getChildren()))
                .build())
            .collect(Collectors.toList());
    }

    private AreaVO convertToVO(AreaEntity area) {
        AreaVO vo = new AreaVO();
        BeanUtil.copyProperties(area, vo);

        // 添加父区域名称
        if (area.getParentId() != null && area.getParentId() > 0) {
            AreaEntity parentArea = areaManager.getById(area.getParentId());
            if (parentArea != null) {
                vo.setParentAreaName(parentArea.getAreaName());
            }
        }

        return vo;
    }

    private AreaDetailVO convertToDetailVO(AreaEntity area) {
        AreaDetailVO vo = new AreaDetailVO();
        BeanUtil.copyProperties(area, vo);

        // 解析区域配置
        if (StringUtils.isNotBlank(area.getAreaConfig())) {
            try {
                vo.setAreaConfig(JsonUtils.parseObject(area.getAreaConfig(), Map.class));
            } catch (Exception e) {
                log.error("解析区域配置失败", e);
            }
        }

        return vo;
    }

    private List<AreaVO> buildAccessibleAreaTree(List<AreaEntity> allAreas, List<Long> accessibleAreaIds) {
        // 1. 构建区域树
        AreaTreeNode rootNode = treeManager.buildAreaTree(allAreas);

        // 2. 过滤出可访问的区域
        List<AreaVO> accessibleAreas = new ArrayList<>();
        filterAccessibleAreas(rootNode, accessibleAreaIds, accessibleAreas);

        return accessibleAreas;
    }

    private void filterAccessibleAreas(AreaTreeNode node, List<Long> accessibleAreaIds, List<AreaVO> result) {
        if (accessibleAreaIds.contains(node.getAreaId())) {
            AreaVO vo = convertToVO(node);
            vo.setChildren(new ArrayList<>());

            // 添加子区域
            for (AreaTreeNode child : node.getChildren()) {
                AreaVO childVo = convertToVO(child);
                if (accessibleAreaIds.contains(child.getAreaId())) {
                    vo.getChildren().add(childVo);
                    filterAccessibleAreas(child, accessibleAreaIds, vo.getChildren());
                }
            }

            result.add(vo);
        }
    }
}
```

### 核心管理层 (AreaManager)

```java
@Component
public class AreaManager {

    @Resource
    private AreaDao areaDao;
    @Resource
    private AreaConfigDao areaConfigDao;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存常量
    private static final String CACHE_PREFIX = "area:";
    private static final String TREE_CACHE_KEY = "area:tree";
    private static final String USER_AREA_PREFIX = "user:area:";
    private static final Duration CACHE_EXPIRE = Duration.ofMinutes(10);

    @Cacheable(value = "area", key = "'page:' + #queryDTO.hashCode()")
    public PageResult<AreaEntity> queryPage(AreaQueryDTO queryDTO) {
        QueryWrapper<AreaEntity> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(queryDTO.getAreaCode())) {
            queryWrapper.like("area_code", queryDTO.getAreaCode());
        }
        if (StringUtils.isNotBlank(queryDTO.getAreaName())) {
            queryWrapper.like("area_name", queryDTO.getAreaName());
        }
        if (queryDTO.getAreaType() != null) {
            queryWrapper.eq("area_type", queryDTO.getAreaType());
        }
        if (queryDTO.getParentId() != null) {
            queryWrapper.eq("parent_id", queryDTO.getParentId());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }

        queryWrapper.eq("deleted_flag", 0)
                   .orderByAsc("sort_order")
                   .orderByDesc("create_time");

        Page<AreaEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<AreaEntity> result = areaDao.selectPage(page, queryWrapper);

        return PageResult.<AreaEntity>builder()
            .records(result.getRecords())
            .total(result.getTotal())
            .pageNum(result.getCurrent())
            .pageSize(result.getSize())
            .build();
    }

    @Cacheable(value = "area", key = "#areaId")
    public AreaEntity getById(Long areaId) {
        return areaDao.selectById(areaId);
    }

    @CacheEvict(value = "area", allEntries = true)
    public void add(AreaEntity area) {
        areaDao.insert(area);

        // 清除区域树缓存
        clearAreaTreeCache();

        // 缓存区域信息
        cacheArea(area);
    }

    @CacheEvict(value = "area", allEntries = true)
    public void update(AreaEntity area) {
        // 乐观锁更新
        QueryWrapper<AreaEntity> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("area_id", area.getAreaId())
                   .eq("version", area.getVersion() - 1);

        int updateCount = areaDao.update(area, updateWrapper);
        if (updateCount == 0) {
            throw new SmartException("区域信息已变更，请刷新后重试");
        }

        // 更新缓存
        cacheArea(area);

        // 清除区域树缓存
        clearAreaTreeCache();

        // 清除用户区域缓存
        clearUserAreaCache();
    }

    @CacheEvict(value = "area", allEntries = true)
    public void softDelete(Long areaId) {
        AreaEntity area = new AreaEntity();
        area.setAreaId(areaId);
        area.setDeletedFlag(1);
        area.setUpdateTime(LocalDateTime.now());

        areaDao.updateById(area);

        // 清除缓存
        clearAreaCache(areaId);

        // 清除区域树缓存
        clearAreaTreeCache();
    }

    public List<AreaEntity> getChildren(Long parentId) {
        return areaDao.selectList(
            new QueryWrapper<AreaEntity>()
                .eq("parent_id", parentId)
                .eq("status", 1)
                .eq("deleted_flag", 0)
                .orderByAsc("sort_order")
        );
    }

    public List<AreaEntity> getAllAreas() {
        return areaDao.selectList(
            new QueryWrapper<AreaEntity>()
                .eq("status", 1)
                .eq("deleted_flag", 0)
                .orderByAsc("area_level")
                .orderByAsc("sort_order")
        );
    }

    public List<AreaEntity> getAreasByIds(List<Long> areaIds) {
        if (areaIds == null || areaIds.isEmpty()) {
            return Collections.emptyList();
        }

        return areaDao.selectList(
            new QueryWrapper<AreaEntity>()
                .in("area_id", areaIds)
                .eq("status", 1)
                .eq("deleted_flag", 0)
        );
    }

    public boolean checkAreaCodeExists(String areaCode) {
        return checkAreaCodeExists(areaCode, null);
    }

    public boolean checkAreaCodeExists(String areaCode, Long excludeAreaId) {
        QueryWrapper<AreaEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("area_code", areaCode)
                   .eq("deleted_flag", 0);

        if (excludeAreaId != null) {
            queryWrapper.ne("area_id", excludeAreaId);
        }

        return areaDao.selectCount(queryWrapper) > 0;
    }

    public void updateAreaConfig(Long areaId, AreaConfigUpdateDTO configDTO) {
        // 逐条更新配置
        for (AreaConfigUpdateDTO.ConfigItem item : configDTO.getConfigs()) {
            AreaConfigEntity config = AreaConfigEntity.builder()
                .areaId(areaId)
                .configType(item.getConfigType())
                .configKey(item.getConfigKey())
                .configValue(item.getConfigValue())
                .configDesc(item.getConfigDesc())
                .isEncrypted(item.getIsEncrypted() ? 1 : 0)
                .version(1)
                .effectiveTime(item.getEffectiveTime())
                .expireTime(item.getExpireTime())
                .status(1)
                .build();

            // 检查是否已存在
            AreaConfigEntity existingConfig = areaConfigDao.selectOne(
                new QueryWrapper<AreaConfigEntity>()
                    .eq("area_id", areaId)
                    .eq("config_type", item.getConfigType())
                    .eq("config_key", item.getConfigKey())
                    .eq("status", 1)
                    .orderByDesc("version")
                    .last("LIMIT 1")
            );

            if (existingConfig != null) {
                // 更新版本号
                config.setVersion(existingConfig.getVersion() + 1);
                // 失效旧配置
                existingConfig.setStatus(0);
                existingConfig.setUpdateTime(LocalDateTime.now());
                areaConfigDao.updateById(existingConfig);
            }

            areaConfigDao.insert(config);
        }

        // 清除区域缓存
        clearAreaCache(areaId);
    }

    public List<AreaConfigVO> getAreaConfig(Long areaId) {
        List<AreaConfigEntity> configs = areaConfigDao.selectList(
            new QueryWrapper<AreaConfigEntity>()
                .eq("area_id", areaId)
                .eq("status", 1)
                .orderByDesc("version")
                .orderBy("config_key")
        );

        // 按配置类型和键分组，取最新版本
        Map<String, AreaConfigEntity> latestConfigs = new HashMap<>();
        for (AreaConfigEntity config : configs) {
            String key = config.getConfigType() + ":" + config.getConfigKey();
            AreaConfigEntity existing = latestConfigs.get(key);
            if (existing == null || config.getVersion() > existing.getVersion()) {
                latestConfigs.put(key, config);
            }
        }

        return latestConfigs.values().stream()
            .map(this::convertToConfigVO)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "area", key = TREE_CACHE_KEY)
    public List<AreaEntity> getAreaTree() {
        return getAllAreas(); // 返回所有区域用于构建树
    }

    /**
     * 清除区域树缓存
     */
    public void clearAreaTreeCache() {
        redisTemplate.delete(TREE_CACHE_KEY);
    }

    /**
     * 清除用户区域缓存
     */
    public void clearUserAreaCache() {
        Set<String> keys = redisTemplate.keys(USER_AREA_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private void cacheArea(AreaEntity area) {
        String cacheKey = CACHE_PREFIX + area.getAreaId();
        redisTemplate.opsForValue().set(cacheKey, area, CACHE_EXPIRE);
    }

    private void clearAreaCache(Long areaId) {
        String cacheKey = CACHE_PREFIX + areaId;
        redisTemplate.delete(cacheKey);
    }

    private AreaConfigVO convertToConfigVO(AreaConfigEntity config) {
        AreaConfigVO vo = new AreaConfigVO();
        BeanUtil.copyProperties(config, vo);

        // 解密配置值（如果需要）
        if (config.getIsEncrypted() == 1) {
            try {
                String decryptedValue = decryptConfigValue(config.getConfigValue());
                vo.setConfigValue(decryptedValue);
            } catch (Exception e) {
                log.error("解密区域配置失败", e);
                vo.setConfigValue("******");
            }
        }

        return vo;
    }

    private String decryptConfigValue(String encryptedValue) {
        // 使用AES解密配置值
        return AesUtil.decrypt(encryptedValue, getAreaConfigKey());
    }

    private String getAreaConfigKey() {
        // 从安全配置中获取加密密钥
        return securityConfig.getAreaConfigKey();
    }
}
```

---

## 🎨 前端实现

### 区域状态管理 (useAreaStore)

```javascript
// /store/area.js
import { defineStore } from 'pinia'
import { areaApi } from '/@/api/area'

export const useAreaStore = defineStore('area', {
  state: () => ({
    // 区域列表
    areaList: [],
    // 区域树结构
    areaTree: [],
    // 用户可访问的区域
    accessibleAreas: [],
    // 选中区域
    selectedArea: null,
    // 区域设备统计
    areaDeviceStats: new Map(),
    // 区域用户统计
    areaUserStats: new Map(),
    // 区域配置
    areaConfigs: new Map()
  }),

  getters: {
    // 获取区域树形结构
    getAreaTree: (state) => {
      const buildTree = (areas, parentId = 0) => {
        return areas
          .filter(area => area.parentId === parentId)
          .map(area => ({
            ...area,
            children: buildTree(areas, area.areaId)
          }))
          .sort((a, b) => a.sortOrder - b.sortOrder)
      }
      return buildTree(state.areaList)
    },

    // 获取区域完整路径
    getAreaPath: (state) => (areaId) => {
      const path = []
      const findPath = (areas, targetId, currentPath = []) => {
        for (const area of areas) {
          if (area.areaId === targetId) {
            return [...currentPath, area]
          }
          if (area.children && area.children.length > 0) {
            const result = findPath(area.children, targetId, [...currentPath, area])
            if (result) {
              return result
            }
          }
        }
        return null
      }
      return findPath(state.areaTree, areaId)
    },

    // 检查区域是否可访问
    isAreaAccessible: (state) => (areaId, permission = 'access') => {
      return state.accessibleAreas.some(area => area.areaId === areaId)
    },

    // 获取区域设备统计
    getAreaDeviceStats: (state) => (areaId) => {
      return state.areaDeviceStats.get(areaId) || {
        totalDevices: 0,
        onlineDevices: 0,
        offlineDevices: 0,
        deviceTypes: {}
      }
    },

    // 获取区域用户统计
    getAreaUserStats: (state) => (areaId) => {
      return state.areaUserStats.get(areaId) || {
        totalUsers: 0,
        activeUsers: 0,
        userTypes: {}
      }
    }
  },

  actions: {
    // 获取区域列表
    async fetchAreaList(params = {}) {
      try {
        const result = await areaApi.queryPage({
          pageNum: params.pageNum || 1,
          pageSize: params.pageSize || 1000,
          ...params
        })

        this.areaList = result.data.records

        // 如果没有指定parentId，重新构建区域树
        if (!params.parentId) {
          this.areaTree = this.getAreaTree
        }

        return result.data
      } catch (error) {
        console.error('获取区域列表失败:', error)
        throw error
      }
    },

    // 获取区域树
    async fetchAreaTree() {
      try {
        const result = await areaApi.getAreaTree()
        this.areaTree = result.data
        this.areaList = flattenAreaTree(result.data)
        return result.data
      } catch (error) {
        console.error('获取区域树失败:', error)
        throw error
      }
    },

    // 获取用户可访问的区域
    async fetchUserAccessibleAreas(permission = 'access') {
      try {
        const result = await areaApi.getUserAccessibleAreas(permission)
        this.accessibleAreas = result.data
        return result.data
      } catch (error) {
        console.error('获取用户可访问区域失败:', error)
        throw error
      }
    },

    // 获取区域详情
    async fetchAreaDetail(areaId) {
      try {
        const result = await areaApi.getAreaDetail(areaId)
        this.selectedArea = result.data

        // 更新统计数据
        if (result.data.deviceStatistics) {
          this.areaDeviceStats.set(areaId, result.data.deviceStatistics)
        }
        if (result.data.userStatistics) {
          this.areaUserStats.set(areaId, result.data.userStatistics)
        }

        return result.data
      } catch (error) {
        console.error('获取区域详情失败:', error)
        throw error
      }
    },

    // 创建区域
    async createArea(areaData) {
      try {
        const result = await areaApi.add(areaData)
        await this.fetchAreaList() // 重新获取区域列表
        return result.data
      } catch (error) {
        console.error('创建区域失败:', error)
        throw error
      }
    },

    // 更新区域
    async updateArea(areaId, areaData) {
      try {
        const result = await areaApi.update(areaId, areaData)
        await this.fetchAreaList() // 重新获取区域列表
        return result.data
      } catch (error) {
        console.error('更新区域失败:', error)
        throw error
      }
    },

    // 删除区域
    async deleteArea(areaId) {
      try {
        await areaApi.delete(areaId)
        await this.fetchAreaList() // 重新获取区域列表
      } catch (error) {
        console.error('删除区域失败:', error)
        throw error
      }
    },

    // 绑定设备到区域
    async bindDevice(areaId, deviceData) {
      try {
        const result = await areaApi.bindDevice(areaId, deviceData)
        // 更新设备统计
        await this.fetchAreaDeviceStats(areaId)
        return result.data
      } catch (error) {
        console.error('绑定设备失败:', error)
        throw error
      }
    },

    // 解绑设备从区域
    async unbindDevice(areaId, deviceData) {
      try {
        const result = await areaApi.unbindDevice(areaId, deviceData)
        // 更新设备统计
        await this.fetchAreaDeviceStats(areaId)
        return result.data
      } catch (error) {
        console.error('解绑设备失败:', error)
        throw error
      }
    },

    // 授予用户区域权限
    async grantUserAccess(areaId, userData) {
      try {
        const result = await areaApi.grantUserAccess(areaId, userData)
        // 更新用户统计
        await this.fetchAreaUserStats(areaId)
        return result.data
      } catch (error) {
        console.error('授权失败:', error)
        throw error
      }
    },

    // 撤销用户区域权限
    async revokeUserAccess(areaId, userData) {
      try {
        const result = await areaApi.revokeUserAccess(areaId, userData)
        // 更新用户统计
        await this.fetchAreaUserStats(areaId)
        return result.data
      } catch (error) {
        console.error('撤销权限失败:', error)
        throw error
      }
    },

    // 获取区域设备列表
    async fetchAreaDevices(areaId, params = {}) {
      try {
        const result = await areaApi.getAreaDevices(areaId, {
          pageNum: params.pageNum || 1,
          pageSize: params.pageSize || 10,
          ...params
        })
        return result.data
      } catch (error) {
        console.error('获取区域设备失败:', error)
        throw error
      }
    },

    // 获取区域用户列表
    async fetchAreaUsers(areaId, params = {}) {
      try {
        const result = await areaApi.getAreaUsers(areaId, {
          pageNum: params.pageNum || 1,
          pageSize: params.pageSize || 10,
          ...params
        })
        return result.data
      } catch (error) {
        console.error('获取区域用户失败:', error)
        throw error
      }
    },

    // 获取区域设备统计
    async fetchAreaDeviceStats(areaId) {
      try {
        const result = await areaApi.getAreaDeviceStats(areaId)
        this.areaDeviceStats.set(areaId, result.data)
        return result.data
      } catch (error) {
        console.error('获取区域设备统计失败:', error)
      }
    },

    // 获取区域用户统计
    async fetchAreaUserStats(areaId) {
      try {
        const result = await areaApi.getAreaUserStats(areaId)
        this.areaUserStats.set(areaId, result.data)
        return result.data
      } catch (error) {
        console.error('获取区域用户统计失败:', error)
      }
    }
  }
})

// 工具函数：扁平化区域树
function flattenAreaTree(treeNodes) {
  const result = []

  function traverse(nodes) {
    if (!nodes || !Array.isArray(nodes)) return

    for (const node of nodes) {
      result.push(node)
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    }
  }

  traverse(treeNodes)
  return result
}
```

### 区域树组件 (AreaTree)

```vue
<template>
  <div class="area-tree">
    <a-tree
      v-model:expandedKeys="expandedKeys"
      v-model:selectedKeys="selectedKeys"
      :tree-data="treeData"
      :field-names="fieldNames"
      :show-line="true"
      :show-icon="true"
      :default-expand-all="defaultExpandAll"
      :default-selected-keys="defaultSelectedKeys"
      @select="handleSelect"
      @expand="handleExpand"
    >
      <template #icon="{ expanded }">
        <FileOutlined v-if="!expanded" />
        <FolderOpenOutlined v-else />
      </template>

      <template #title="{ node }">
        <div class="area-node">
          <span class="area-name">{{ node.areaName }}</span>
          <a-tag
            :color="getStatusColor(node.status)"
            size="small"
            class="area-status"
          >
            {{ getStatusText(node.status) }}
          </a-tag>
          <span class="area-info">
            ({{ node.deviceCount || 0 }}设备, {{ node.userCount || 0 }}用户)
          </span>
        </div>
      </template>

      <template #extra="{ node }">
        <a-space>
          <a-button
            type="text"
            size="small"
            @click="handleViewDetail(node)"
            v-permission="['area:detail']"
          >
            <template #icon><EyeOutlined /></template>
          </a-button>
          <a-button
            type="text"
            size="small"
            @click="handleAddDevice(node)"
            v-permission="['area:device:bind']"
          >
            <template #icon><PlusOutlined /></template>
          </a-button>
          <a-button
            type="text"
            size="small"
            @click="handleEditArea(node)"
            v-permission="['area:update']"
          >
            <template #icon><EditOutlined /></template>
          </a-button>
          <a-button
            type="text"
            size="small"
            danger
            @click="handleDeleteArea(node)"
            v-permission="['area:delete']"
          >
            <template #icon><DeleteOutlined /></template>
          </a-button>
        </a-space>
      </template>
    </a-tree>

    <!-- 区域详情弹窗 -->
    <AreaDetailModal
      v-model:visible="detailVisible"
      :area="selectedArea"
      @refresh="handleRefresh"
    />

    <!-- 绑定设备弹窗 -->
    <AreaDeviceBindModal
      v-model:visible="bindDeviceVisible"
      :area="selectedArea"
      @success="handleBindSuccess"
    />

    <!-- 编辑区域弹窗 -->
    <AreaEditModal
      v-model:visible="editVisible"
      :area="selectedArea"
      :parent-areas="parentAreas"
      @success="handleEditSuccess"
    />

    <!-- 新增子区域弹窗 -->
    <AreaCreateModal
      v-model:visible="createVisible"
      :parent-area="selectedArea"
      @success="handleCreateSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAreaStore } from '/@/store/area'
import { message, Modal } from 'ant-design-vue'
import {
  FileOutlined,
  FolderOpenOutlined,
  EyeOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import AreaDetailModal from './AreaDetailModal.vue'
import AreaDeviceBindModal from './AreaDeviceBindModal.vue'
import AreaEditModal from './AreaEditModal.vue'
import AreaCreateModal from './AreaCreateModal.vue'

const props = defineProps({
  height: {
    type: String,
    default: '600px'
  },
  defaultExpandAll: {
    type: Boolean,
    default: false
  },
  defaultSelectedKeys: {
    type: Array,
    default: () => []
  },
  showOperations: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['select', 'refresh'])

const areaStore = useAreaStore()

const expandedKeys = ref([])
const selectedKeys = ref(props.defaultSelectedKeys)
const detailVisible = ref(false)
const bindDeviceVisible = ref(false)
const editVisible = ref(false)
const createVisible = ref(false)
const selectedArea = ref(null)

// 计算属性
const treeData = computed(() => {
  return buildTreeData(areaStore.areaList)
})

const parentAreas = computed(() => {
  // 获取可作为父级的区域列表
  const buildParentOptions = (areas, level = 0) => {
    const options = []

    for (const area of areas) {
      if (area.areaLevel <= level + 1) { // 只能选择相邻层级的区域
        options.push({
          label: area.areaName,
          value: area.areaId,
          disabled: area.areaId === selectedArea.value?.areaId
        })

        if (area.children && area.children.length > 0) {
          options.push({
            label: area.areaName,
            value: area.areaId,
            disabled: area.areaId === selectedArea.value?.areaId,
            children: buildParentOptions(area.children, level + 1)
          })
        }
      }
    }

    return options
  }

  return buildParentOptions(areaStore.areaTree)
})

const fieldNames = {
  children: 'children',
  title: 'areaName',
  key: 'areaId'
}

// 监听props变化
watch(() => props.defaultSelectedKeys, (newKeys) => {
  selectedKeys.value = newKeys
})

// 生命周期
onMounted(async () => {
  await areaStore.fetchAreaTree()
})

// 方法定义
const handleSelect = (selectedKeys, { selected, selectedNodes }) => {
  if (selectedNodes.length > 0) {
    const node = selectedNodes[0]
    emit('select', node)
  }
}

const handleExpand = (expandedKeys, { expanded, node }) => {
  console.log('Node expanded:', expanded, node)
}

const handleViewDetail = async (node) => {
  try {
    selectedArea.value = node
    await areaStore.fetchAreaDetail(node.areaId)
    detailVisible.value = true
  } catch (error) {
    console.error('获取区域详情失败:', error)
    message.error('获取区域详情失败')
  }
}

const handleAddDevice = (node) => {
  selectedArea.value = node
  bindDeviceVisible.value = true
}

const handleEditArea = (node) => {
  selectedArea.value = node
  editVisible.value = true
}

const handleDeleteArea = (node) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除区域"${node.areaName}"吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await areaStore.deleteArea(node.areaId)
        message.success('删除成功')
        emit('refresh')
      } catch (error) {
        console.error('删除区域失败:', error)
        message.error('删除失败: ' + error.message)
      }
    }
  })
}

const handleBindSuccess = () => {
  message.success('设备绑定成功')
  emit('refresh')
}

const handleEditSuccess = () => {
  message.success('区域更新成功')
  emit('refresh')
}

const handleCreateSuccess = () => {
  message.success('区域创建成功')
  emit('refresh')
}

const handleRefresh = () => {
  emit('refresh')
}

const getStatusColor = (status) => {
  return status === 1 ? 'green' : 'red'
}

const getStatusText = (status) => {
  return status === 1 ? '启用' : '禁用'
}

const buildTreeData = (areas) => {
  const buildNode = (area) => {
    const children = areas
      .filter(child => child.parentId === area.areaId)
      .map(buildNode)
      .sort((a, b) => a.sortOrder - b.sortOrder)

    return {
      ...area,
      children,
      deviceCount: calculateDeviceCount(area),
      userCount: calculateUserCount(area)
    }
  }

  const rootAreas = areas.filter(area => !area.parentId || area.parentId === 0)
  return rootAreas.map(buildNode)
}

const calculateDeviceCount = (area) => {
  // 这里可以调用接口获取设备数量，或从store中获取
  return areaStore.getAreaDeviceStats(area.areaId)?.totalDevices || 0
}

const calculateUserCount = (area) => {
  // 这里可以调用接口获取用户数量，或从store中获取
  return areaStore.getAreaUserStats(area.areaId)?.totalUsers || 0
}
</script>

<style lang="less" scoped>
.area-tree {
  padding: 8px;

  .area-node {
    display: flex;
    align-items: center;
    gap: 8px;

    .area-name {
      font-weight: 500;
    }

    .area-status {
      font-size: 12px;
    }

    .area-info {
      color: #999;
      font-size: 12px;
    }
  }

  :deep(.ant-tree-switcher) {
    width: 24px;
  }

  :deep(.ant-tree-node-content-wrapper) {
    height: 32px;
    line-height: 32px;
  }

  :deep(.ant-tree-node-content-wrapper .ant-tree-icon) {
    margin-right: 4px;
  }
}
</style>
```

---

## 🧪 测试策略

### 1. 单元测试

```java
@SpringBootTest
class AreaServiceTest {

    @Resource
    private AreaService areaService;

    @Resource
    private AreaDao areaDao;

    @Test
    void testCreateArea() {
        // 准备测试数据
        AreaCreateDTO createDTO = new AreaCreateDTO();
        createDTO.setAreaCode("TEST001");
        createDTO.setAreaName("测试区域");
        createDTO.setAreaType("BUILDING");
        createDTO.setParentId(null); // 顶级区域
        createDTO.setSortOrder(1);
        createDTO.setStatus(1);

        // 执行测试
        assertDoesNotThrow(() -> areaService.add(createDTO));

        // 验证结果
        AreaEntity area = areaDao.selectOne(
            new QueryWrapper<AreaEntity>()
                .eq("area_code", "TEST001")
        );
        assertNotNull(area);
        assertEquals("测试区域", area.getAreaName());
        assertEquals(1, area.getAreaLevel());
        assertEquals(1, area.getStatus());
    }

    @Test
    void testCreateSubArea() {
        // 先创建父区域
        AreaEntity parentArea = createTestArea("PARENT001", "父区域", null);

        // 创建子区域
        AreaCreateDTO createDTO = new AreaCreateDTO();
        createDTO.setAreaCode("CHILD001");
        createDTO.setAreaName("子区域");
        createDTO.setAreaType("FLOOR");
        createDTO.setParentId(parentArea.getAreaId());
        createDTO.setSortOrder(1);
        createDTO.setStatus(1);

        // 执行测试
        assertDoesNotThrow(() -> areaService.add(createDTO));

        // 验证结果
        AreaEntity childArea = areaDao.selectOne(
            new QueryWrapper<AreaEntity>()
                .eq("area_code", "CHILD001")
        );
        assertNotNull(childArea);
        assertEquals(parentArea.getAreaId(), childArea.getParentId());
        assertEquals(2, childArea.getAreaLevel());
    }

    @Test
    void testGetAreaPath() {
        // 创建三级区域结构
        AreaEntity level1 = createTestArea("L1", "一级区域", null);
        AreaEntity level2 = createTestArea("L2", "二级区域", level1.getAreaId());
        AreaEntity level3 = createTestArea("L3", "三级区域", level2.getAreaId());

        // 获取路径
        List<AreaVO> path = areaService.getAreaPath(level3.getAreaId());

        // 验证路径
        assertEquals(3, path.size());
        assertEquals("一级区域", path.get(0).getAreaName());
        assertEquals("二级区域", path.get(1).getAreaName());
        assertEquals("三级区域", path.get(2).getAreaName());
    }

    @Test
    void testDeleteArea() {
        // 创建测试区域
        AreaEntity area = createTestArea("DELETE_TEST", "删除测试区域", null);

        // 删除区域
        assertDoesNotThrow(() -> areaService.delete(area.getAreaId()));

        // 验证软删除
        AreaEntity deletedArea = areaDao.selectById(area.getAreaId());
        assertNotNull(deletedArea);
        assertEquals(1, deletedArea.getDeletedFlag());
    }

    @Test
    void testDeleteAreaWithChildren() {
        // 创建父区域和子区域
        AreaEntity parent = createTestArea("PARENT", "父区域", null);
        createTestArea("CHILD", "子区域", parent.getAreaId());

        // 尝试删除父区域，应该抛出异常
        SmartException exception = assertThrows(SmartException.class, () -> {
            areaService.delete(parent.getAreaId());
        });

        assertTrue(exception.getMessage().contains("包含子区域"));
    }

    @Test
    void testBindDevice() {
        // 创建区域
        AreaEntity area = createTestArea("DEVICE_TEST", "设备测试区域", null);

        // 绑定设备
        AreaDeviceBindDTO bindDTO = new AreaDeviceBindDTO();
        bindDTO.setDeviceId(123L);
        bindDTO.setDeviceType("CAMERA");
        bindDTO.setBindRemark("测试绑定");

        assertDoesNotThrow(() -> areaService.bindDevice(area.getAreaId(), bindDTO));

        // 验证绑定结果
        List<AreaDeviceEntity> areaDevices = areaDeviceDao.getAreaDevices(area.getAreaId());
        assertFalse(areaDevices.isEmpty());
        assertEquals(123L, areaDevices.get(0).getDeviceId());
        assertEquals("CAMERA", areaDevices.get(0).getDeviceType());
        assertEquals(1, areaDevices.get(0).getStatus());
    }

    @Test
    void testGrantUserAccess() {
        // 创建区域
        AreaEntity area = createTestArea("USER_TEST", "用户测试区域", null);

        // 授予权限
        AreaUserGrantDTO grantDTO = new AreaUserGrantDTO();
        grantDTO.setUserId(456L);
        grantDTO.setUserType("EMPLOYEE");
        grantDTO.setRelationType("ACCESS");
        grantDTO.setAccessLevel(1);
        grantDTO.setGrantRemark("测试授权");

        assertDoesNotThrow(() -> areaService.grantUserAccess(area.getAreaId(), grantDTO));

        // 验证授权结果
        List<AreaUserEntity> areaUsers = areaUserDao.getAreaUsers(area.getAreaId());
        assertFalse(areaUsers.isEmpty());
        assertEquals(456L, areaUsers.get(0).getUserId());
        assertEquals("ACCESS", areaUsers.get(0).getRelationType());
        assertEquals(1, areaUsers.get(0).getAccessLevel());
        assertEquals(1, areaUsers.get(0).getStatus());
    }

    @Test
    void testGetUserAccessibleAreas() {
        // 创建区域树
        AreaEntity root = createTestArea("ROOT", "根区域", null);
        AreaEntity child1 = createTestArea("CHILD1", "子区域1", root.getAreaId());
        AreaEntity child2 = createTestArea("CHILD2", "子区域2", root.getAreaId());

        // 为用户授予权限（只有CHILD1区域）
        AreaUserGrantDTO grantDTO = new AreaUserGrantDTO();
        grantDTO.setUserId(789L);
        grantDTO.setRelationType("ACCESS");
        areaService.grantUserAccess(child1.getAreaId(), grantDTO);

        // 获取用户可访问的区域
        List<AreaVO> accessibleAreas = areaService.getUserAccessibleAreas(789L, "ACCESS");

        // 验证结果
        assertTrue(accessibleAreas.stream().anyMatch(area -> area.getAreaId().equals(root.getAreaId())));
        assertTrue(accessibleAreas.stream().anyMatch(area -> area.getAreaId().equals(child1.getAreaId())));
        assertFalse(accessibleAreas.stream().anyMatch(area -> area.getAreaId().equals(child2.getAreaId())));
    }

    private AreaEntity createTestArea(String code, String name, Long parentId) {
        AreaEntity area = AreaEntity.builder()
            .areaCode(code)
            .areaName(name)
            .areaType("BUILDING")
            .parentId(parentId)
            .areaLevel(parentId == null ? 1 : 2)
            .sortOrder(1)
            .status(1)
            .build();
        areaDao.insert(area);
        return area;
    }
}
```

### 2. 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AreaIntegrationTest {

    @Resource
    private TestRestTemplate restTemplate;

    @Test
    void testAreaManagementFlow() {
        String token = authenticate("admin", "123456");

        // 1. 创建父区域
        AreaCreateDTO parentDTO = new AreaCreateDTO();
        parentDTO.setAreaCode("INTEGRATION_PARENT");
        parentDTO.setAreaName("集成测试父区域");
        parentDTO.setAreaType("CAMPUS");
        parentDTO.setSortOrder(1);
        parentDTO.setStatus(1);

        ResponseEntity<ResponseDTO<String>> createParentResponse = restTemplate.exchange(
            "/api/area",
            HttpMethod.POST,
            createEntityWithToken(token, parentDTO),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, createParentResponse.getStatusCodeValue());

        // 2. 创建子区域
        AreaCreateDTO childDTO = new AreaCreateDTO();
        childDTO.setAreaCode("INTEGRATION_CHILD");
        childDTO.setAreaName("集成测试子区域");
        childDTO.setAreaType("BUILDING");
        childDTO.setParentId(1L); // 假设父区域ID为1
        childDTO.setSortOrder(1);
        childDTO.setStatus(1);

        ResponseEntity<ResponseDTO<String>> createChildResponse = restTemplate.exchange(
            "/api/area",
            HttpMethod.POST,
            createEntityWithToken(token, childDTO),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, createChildResponse.getStatusCodeValue());

        // 3. 获取区域树
        ResponseEntity<ResponseDTO<List<AreaTreeVO>>> treeResponse = restTemplate.exchange(
            "/api/area/tree",
            HttpMethod.GET,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<List<AreaTreeVO>>>() {}
        );

        assertEquals(200, treeResponse.getStatusCodeValue());
        assertFalse(treeResponse.getBody().getData().isEmpty());

        // 4. 绑定设备到父区域
        AreaDeviceBindDTO bindDTO = new AreaDeviceBindDTO();
        bindDTO.setDeviceId(123L);
        bindDTO.setDeviceType("CAMERA");

        ResponseEntity<ResponseDTO<String>> bindResponse = restTemplate.exchange(
            "/api/area/1/device/bind",
            HttpMethod.POST,
            createEntityWithToken(token, bindDTO),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, bindResponse.getStatusCodeValue());

        // 5. 获取区域设备列表
        ResponseEntity<ResponseDTO<PageResult<AreaDeviceVO>>> devicesResponse = restTemplate.exchange(
            "/api/area/1/devices",
            HttpMethod.GET,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<PageResult<AreaDeviceVO>>>() {}
        );

        assertEquals(200, devicesResponse.getStatusCodeValue());
        assertTrue(devicesResponse.getBody().getData().getTotal() > 0);

        // 6. 授予用户区域权限
        AreaUserGrantDTO grantDTO = new AreaUserGrantDTO();
        grantDTO.setUserId(456L);
        grantDTO.setUserType("EMPLOYEE");
        grantDTO.setRelationType("ACCESS");

        ResponseEntity<ResponseDTO<String>> grantResponse = restTemplate.exchange(
            "/api/area/1/user/grant",
            HttpMethod.POST,
            createEntityWithToken(token, grantDTO),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, grantResponse.getStatusCodeValue());

        // 7. 获取用户可访问区域
        ResponseEntity<ResponseDTO<List<AreaVO>>> accessibleResponse = restTemplate.exchange(
            "/api/area/user/456/accessible?permission=ACCESS",
            HttpMethod.GET,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<List<AreaVO>>>() {}
        );

        assertEquals(200, accessibleResponse.getStatusCodeValue());
        assertFalse(accessibleResponse.getBody().getData().isEmpty());

        // 8. 删除子区域
        ResponseEntity<ResponseDTO<String>> deleteChildResponse = restTemplate.exchange(
            "/api/area/2",
            HttpMethod.DELETE,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, deleteChildResponse.getStatusCodeValue());

        // 9. 删除父区域（现在应该成功）
        ResponseEntity<ResponseDTO<String>> deleteParentResponse = restTemplate.exchange(
            "/api/area/1",
            HttpMethod.DELETE,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, deleteParentResponse.getStatusCodeValue());
    }
}
```

---

## 📋 检查清单

### 开发前检查

- [ ] 是否已明确区域层级结构需求？
- [ ] 是否已确认区域类型分类？
- [ ] 是否已了解设备和人员的区域归属需求？
- [ ] 是否已确认区域权限继承机制？

### 开发中检查

- [ ] 是否实现了多级区域树结构？
- [ ] 是否添加了区域设备绑定功能？
- [ ] 是否实现了区域用户权限管理？
- [ ] 是否添加了区域配置管理？
- [ ] 是否实现了区域路径查询？

### 部署前检查

- [ ] 区域树结构是否正确？
- [ ] 区域权限继承是否正常？
- [ ] 设备绑定功能是否正常？
- [ ] 用户权限管理是否正确？
- [ ] 区域配置是否生效？

---

**📞 技术支持**：架构师团队

**📚 相关文档**：
- [设备管理公共模块](./smart-device.md)
- [权限管理公共模块](./smart-permission.md)
- [人员管理公共模块](./smart-person.md)
- [审批流程公共模块](./smart-workflow.md)
- [综合开发规范文档](../DEV_STANDARDS.md)

---

*最后更新：2025-01-10*
*维护者：SmartAdmin开发团队*