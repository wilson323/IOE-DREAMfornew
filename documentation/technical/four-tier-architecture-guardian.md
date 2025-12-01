# 四层架构守护专家

## 核心职责
作为IOE-DREAM项目的四层架构守护专家，确保严格遵循Controller → Service → Manager → DAO架构模式，防止跨层访问和架构违规。

## 核心能力

### 四层架构规范

#### 严格分层调用链
```
Controller层 (接口控制)
    ↓ 严格调用
Service层 (业务逻辑 + 事务管理)
    ↓ 严格调用
Manager层 (复杂业务 + 缓存管理)
    ↓ 严格调用
Repository/DAO层 (数据访问)
```

#### 层级职责定义
**Controller层**:
- 接收HTTP请求，参数校验
- 调用Service层处理业务
- 返回统一响应格式
- **禁止**: 编写业务逻辑、直接访问DAO

**Service层**:
- 业务逻辑处理和事务管理
- 调用Manager层处理复杂业务
- **职责**: 事务边界、业务编排
- **禁止**: 跨层直接访问DAO

**Manager层**:
- 复杂业务逻辑封装
- 跨模块调用协调
- 缓存策略管理
- **职责**: 业务复用、性能优化
- **禁止**: 管理事务、数据访问

**Repository/DAO层**:
- 数据访问操作
- SQL查询和映射
- **职责**: 数据持久化
- **禁止**: 业务逻辑处理

### 架构违规检测

#### 跨层访问检测
```bash
#!/bin/bash
# architecture-violation-check.sh

echo "🔍 执行四层架构违规检查..."

# 1. Controller直接访问DAO检测
echo "检查1: Controller直接访问DAO"
controller_dao_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)

if [ $controller_dao_violations -gt 0 ]; then
    echo "❌ 发现 $controller_dao_violations 处Controller直接访问DAO:"
    grep -r -n "@Resource.*Dao" --include="*Controller.java" .
    echo ""
    echo "修复建议:"
    echo "1. 移除Controller中的DAO注入"
    echo "2. 通过Service层调用DAO"
    echo "3. 在Service层实现数据访问逻辑"
    exit 1
fi

# 2. Service直接访问DAO检测（允许，但建议通过Manager）
echo "检查2: Service直接访问DAO"
service_dao_count=$(grep -r "@Resource.*Dao" --include="*Service.java" . | wc -l)
echo "Service直接访问DAO数量: $service_dao_count (建议通过Manager层)"

# 3. Manager管理事务检测
echo "检查3: Manager层事务管理"
manager_transaction_violations=$(grep -r "@Transactional" --include="*Manager.java" . | wc -l)

if [ $manager_transaction_violations -gt 0 ]; then
    echo "❌ 发现 $manager_transaction_violations 处Manager层管理事务:"
    grep -r -n "@Transactional" --include="*Manager.java" .
    echo ""
    echo "修复建议:"
    echo "1. 将@Transactional注解移到Service层"
    echo "2. Manager层只处理业务逻辑，不管理事务"
    exit 1
fi

echo "🎉 四层架构违规检查通过！"
```

#### 分层依赖验证
```bash
#!/bin/bash
# layer-dependency-validation.sh

echo "🔍 执行分层依赖验证..."

# Controller层依赖验证
echo "验证Controller层依赖..."
controller_files=$(find . -name "*Controller.java")

for controller in $controller_files; do
    # 检查是否有Service依赖
    service_dep=$(grep -c "@Resource.*Service" "$controller")
    dao_dep=$(grep -c "@Resource.*Dao" "$controller")

    if [ $service_dep -eq 0 ]; then
        echo "⚠️ $controller 缺少Service依赖"
    fi

    if [ $dao_dep -gt 0 ]; then
        echo "❌ $controller 直接依赖DAO"
        exit 1
    fi
done

# Service层依赖验证
echo "验证Service层依赖..."
service_files=$(find . -name "*Service.java")

for service in $service_files; do
    # 检查事务注解
    transactional_dep=$(grep -c "@Transactional" "$service")

    if [ $transactional_dep -eq 0 ]; then
        echo "⚠️ $service 缺少事务管理"
    fi
done

echo "🎉 分层依赖验证通过！"
```

### 架构合规模板

#### 标准Controller模板
```java
package net.lab1024.sa.admin.module.{module}.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import {module}.domain.request.{Module}QueryRequest;
import {module}.domain.request.{Module}AddRequest;
import {module}.domain.request.{Module}UpdateRequest;
import {module}.domain.vo.{Module}VO;
import {module}.service.{Module}Service;

/**
 * {Module}管理控制器
 *
 * @author IOE-Dream Team
 * @date 2025-11-16
 */
@RestController
@RequestMapping("/api/{module}")
@Tag(name = "{Module}管理", description = "{Module}管理接口")
public class {Module}Controller {

    @Resource
    private {Module}Service {module}Service;

    @GetMapping("/query")
    @SaCheckPermission("{module}:query")
    @Operation(summary = "查询{module}列表")
    public ResponseDTO<PageResult<{Module}VO>> query{Module}(@Valid {Module}QueryRequest request) {
        // ✅ 正确：只做参数校验和调用Service
        return ResponseDTO.ok({module}Service.query{Module}(request));
    }

    @PostMapping("/add")
    @SaCheckPermission("{module}:add")
    @Operation(summary = "添加{module}")
    public ResponseDTO<String> add{Module}(@Valid @RequestBody {Module}AddRequest request) {
        // ✅ 正确：只做参数校验和调用Service
        return ResponseDTO.ok({module}Service.add{Module}(request));
    }

    @PostMapping("/update")
    @SaCheckPermission("{module}:update")
    @Operation(summary = "更新{module}")
    public ResponseDTO<String> update{Module}(@Valid @RequestBody {Module}UpdateRequest request) {
        // ✅ 正确：只做参数校验和调用Service
        return ResponseDTO.ok({module}Service.update{Module}(request));
    }

    @GetMapping("/detail/{id}")
    @SaCheckPermission("{module}:detail")
    @Operation(summary = "获取{module}详情")
    public ResponseDTO<{Module}VO> get{Module}Detail(@PathVariable Long id) {
        // ✅ 正确：只做参数校验和调用Service
        return ResponseDTO.ok({module}Service.get{Module}Detail(id));
    }

    @PostMapping("/delete/{id}")
    @SaCheckPermission("{module}:delete")
    @Operation(summary = "删除{module}")
    public ResponseDTO<String> delete{Module}(@PathVariable Long id) {
        // ✅ 正确：只做参数校验和调用Service
        return ResponseDTO.ok({module}Service.delete{Module}(id));
    }
}
```

#### 标准Service模板
```java
package net.lab1024.sa.admin.module.{module}.service.impl;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.SmartException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import {module}.domain.request.{Module}QueryRequest;
import {module}.domain.request.{Module}AddRequest;
import {module}.domain.request.{Module}UpdateRequest;
import {module}.domain.vo.{Module}VO;
import {module}.domain.entity.{Module}Entity;
import {module}.manager.{Module}Manager;
import {module}.service.{Module}Service;
import {module}.dao.{Module}Dao;

/**
 * {Module}业务服务实现
 *
 * @author IOE-Dream Team
 * @date 2025-11-16
 */
@Service
public class {Module}ServiceImpl implements {Module}Service {

    @Resource
    private {Module}Manager {module}Manager;

    @Resource
    private {Module}Dao {module}Dao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult<{Module}VO> query{Module}({Module}QueryRequest request) {
        // ✅ 正确：Service层管理事务，调用Manager处理复杂业务
        return {module}Manager.query{Module}(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add{Module}({Module}AddRequest request) {
        // ✅ 正确：Service层事务管理，业务逻辑委托给Manager
        try {
            // 业务规则验证
            validate{Module}Rules(request);

            // 委托Manager处理具体业务
            return {module}Manager.add{Module}(request);
        } catch (Exception e) {
            throw new SmartException("添加{module}失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String update{Module}({Module}UpdateRequest request) {
        // ✅ 正确：Service层事务管理，业务逻辑委托给Manager
        validate{Module}Exists(request.getId());
        return {module}Manager.update{Module}(request);
    }

    @Override
    public {Module}VO get{Module}Detail(Long id) {
        // ✅ 正确：查询操作委托给Manager
        return {module}Manager.get{Module}Detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete{Module}(Long id) {
        // ✅ 正确：Service层事务管理，业务逻辑委托给Manager
        validate{Module}Exists(id);
        return {module}Manager.delete{Module}(id);
    }

    /**
     * 验证业务规则
     */
    private void validate{Module}Rules({Module}AddRequest request) {
        // 业务规则验证逻辑
        // 例如：重复性检查、业务约束验证等
    }

    /**
     * 验证记录存在性
     */
    private void validate{Module}Exists(Long id) {
        {Module}Entity entity = {module}Dao.selectById(id);
        if (entity == null || entity.getDeletedFlag()) {
            throw new SmartException("{module}不存在");
        }
    }
}
```

#### 标准Manager模板
```java
package net.lab1024.sa.admin.module.{module}.manager;

import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.base.common.domain.PageResult;
import {module}.domain.request.{Module}QueryRequest;
import {module}.domain.request.{Module}AddRequest;
import {module}.domain.request.{Module}UpdateRequest;
import {module}.domain.vo.{Module}VO;
import {module}.domain.entity.{Module}Entity;
import {module}.service.{Module}CacheService;
import {module}.dao.{Module}Dao;

/**
 * {Module}业务管理器
 *
 * @author IOE-Dream Team
 * @date 2025-11-16
 */
@Component
public class {Module}Manager {

    @Resource
    private {Module}Dao {module}Dao;

    @Resource
    private {Module}CacheService {module}CacheService;

    @Cacheable(value = "{module}:query", key = "#request.toString()")
    public PageResult<{Module}VO> query{Module}({Module}QueryRequest request) {
        // ✅ 正确：Manager层处理复杂业务逻辑，使用缓存
        Page<{Module}Entity> page = {module}Dao.queryPage(request);
        return PageResult.of({module}Dao.queryVO(page), page.getTotal());
    }

    @CacheEvict(value = "{module}:query", allEntries = true)
    public Long add{Module}({Module}AddRequest request) {
        // ✅ 正确：Manager层处理复杂业务，包括缓存管理
        {Module}Entity entity = build{Module}Entity(request);
        {module}Dao.insert(entity);

        // 缓存相关处理
        {module}CacheService.refreshCache(entity.getId());

        return entity.getId();
    }

    @CacheEvict(value = "{module}:query", allEntries = true)
    public String update{Module}({Module}UpdateRequest request) {
        // ✅ 正确：Manager层处理更新逻辑和缓存
        {Module}Entity entity = build{Module}Entity(request);
        {module}Dao.updateById(entity);

        // 缓存更新
        {module}CacheService.updateCache(entity);

        return "更新成功";
    }

    @Cacheable(value = "{module}:detail", key = "#id")
    public {Module}VO get{Module}Detail(Long id) {
        // ✅ 正确：Manager层处理查询和缓存
        {Module}Entity entity = {module}Dao.selectById(id);
        if (entity == null || entity.getDeletedFlag()) {
            throw new SmartException("{module}不存在");
        }
        return convertToVO(entity);
    }

    @CacheEvict(value = "{module}:query", allEntries = true)
    public String delete{Module}(Long id) {
        // ✅ 正确：Manager层处理删除逻辑和缓存清理
        {Module}Entity entity = {module}Dao.selectById(id);
        if (entity == null || entity.getDeletedFlag()) {
            throw new SmartException("{module}不存在");
        }

        // 软删除
        entity.setDeletedFlag(true);
        {module}Dao.updateById(entity);

        // 缓存清理
        {module}CacheService.clearCache(id);

        return "删除成功";
    }

    /**
     * 构建实体对象
     */
    private {Module}Entity build{Module}Entity({Module}AddRequest request) {
        // 实体构建逻辑
        {Module}Entity entity = new {Module}Entity();
        // 设置属性...
        return entity;
    }

    /**
     * 转换为VO对象
     */
    private {Module}VO convertToVO({Module}Entity entity) {
        // VO转换逻辑
        {Module}VO vo = new {Module}VO();
        // 属性转换...
        return vo;
    }
}
```

#### 标准DAO模板
```java
package net.lab1024.sa.admin.module.{module}.dao;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.base.common.dao.BaseDAO;
import {module}.domain.entity.{Module}Entity;
import {module}.domain.request.{Module}QueryRequest;
import {module}.domain.vo.{Module}VO;

/**
 * {Module}数据访问对象
 *
 * @author IOE-Dream Team
 * @date 2025-11-16
 */
@Repository
public interface {Module}Dao extends BaseDAO<{Module}Entity> {

    /**
     * 分页查询{module}
     */
    default Page<{Module}Entity> queryPage({Module}QueryRequest request) {
        Page<{Module}Entity> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.lambdaQuery()
                .eq({Module}Entity::getDeletedFlag, false)
                .like({Module}Entity::getName, request.getKeyword(),
                      request.getKeyword() != null && !request.getKeyword().isEmpty())
                .orderByDesc({Module}Entity::getCreateTime)
                .page(page);
    }

    /**
     * 查询{module}VO列表
     */
    default List<{Module}VO> queryVO(Page<{Module}Entity> page) {
        // ✅ 正确：DAO层只做数据查询，不包含业务逻辑
        List<{Module}Entity> entities = page.getRecords();
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 实体转VO
     */
    private {Module}VO convertToVO({Module}Entity entity) {
        {Module}VO vo = new {Module}VO();
        // 属性映射...
        return vo;
    }
}
```

### 架构违规自动修复

#### 重构建议生成器
```bash
#!/bin/bash
# architecture-refactor-suggestions.sh

echo "🔧 生成架构重构建议..."

# 分析Controller违规
echo "=== Controller重构建议 ==="
controller_dao_files=$(grep -r -l "@Resource.*Dao" --include="*Controller.java" .)

for file in $controller_dao_files; do
    echo "文件: $file"
    echo "问题: Controller直接注入DAO"
    echo "建议重构步骤:"
    echo "1. 移除DAO注入"
    echo "2. 添加Service注入"
    echo "3. 将数据访问逻辑移至Service层"
    echo ""
done

# 分析Manager事务违规
echo "=== Manager事务重构建议 ==="
manager_transaction_files=$(grep -r -l "@Transactional" --include="*Manager.java" .)

for file in $manager_transaction_files; do
    echo "文件: $file"
    echo "问题: Manager层管理事务"
    echo "建议重构步骤:"
    echo "1. 移除@Transactional注解"
    echo "2. 将事务管理移至Service层"
    echo "3. Manager层专注业务逻辑处理"
    echo ""
done

echo "📝 重构建议生成完成"
```

### 实时架构监控

#### 分层健康检查
```java
@Component
@Slf4j
public class ArchitectureHealthMonitor {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 检查架构分层健康状态
     */
    @Scheduled(fixedRate = 300000) // 每5分钟检查一次
    public void checkArchitectureHealth() {
        try {
            Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);
            Map<String, Object> services = applicationContext.getBeansWithAnnotation(Service.class);
            Map<String, Object> managers = applicationContext.getComponentsWithAnnotation(Component.class);
            Map<String, Object> repositories = applicationContext.getBeansWithAnnotation(Repository.class);

            // 检查Controller层依赖
            checkControllerDependencies(controllers);

            // 检查Service层事务管理
            checkServiceTransactions(services);

            // 检查Manager层事务违规
            checkManagerTransactionViolations(managers);

            log.info("架构健康检查完成");
        } catch (Exception e) {
            log.error("架构健康检查失败", e);
        }
    }

    private void checkControllerDependencies(Map<String, Object> controllers) {
        controllers.forEach((name, controller) -> {
            Field[] fields = controller.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Resource.class)) {
                    Class<?> fieldType = field.getType();
                    if (fieldType.getSimpleName().endsWith("Dao")) {
                        log.error("架构违规: Controller {} 直接依赖DAO {}",
                                 controller.getClass().getSimpleName(), fieldType.getSimpleName());
                    }
                }
            }
        });
    }

    private void checkServiceTransactions(Map<String, Object> services) {
        services.forEach((name, service) -> {
            Method[] methods = service.getClass().getDeclaredMethods();
            boolean hasTransactional = false;
            for (Method method : methods) {
                if (method.isAnnotationPresent(Transactional.class)) {
                    hasTransactional = true;
                    break;
                }
            }
            if (!hasTransactional) {
                log.warn("建议: Service {} 缺少事务管理注解", service.getClass().getSimpleName());
            }
        });
    }

    private void checkManagerTransactionViolations(Map<String, Object> managers) {
        managers.forEach((name, manager) -> {
            Method[] methods = manager.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Transactional.class)) {
                    log.error("架构违规: Manager {} 方法 {} 管理事务",
                             manager.getClass().getSimpleName(), method.getName());
                }
            }
        });
    }
}
```

### 项目特定约束

#### 门禁模块架构示例
```
access-control/
├── controller/
│   └── AccessControlController.java     # 接口层
├── service/
│   ├── AccessControlService.java        # 业务接口
│   └── impl/
│       └── AccessControlServiceImpl.java # 业务实现
├── manager/
│   ├── DeviceManager.java               # 设备管理
│   ├── PermissionManager.java           # 权限管理
│   └── RecordManager.java               # 记录管理
└── dao/
    ├── AccessControlDao.java           # 数据访问
    └── SmartDeviceDao.java             # 设备数据
```

#### 跨层调用规范
```java
// ✅ 正确的调用链
@RestController
public class AccessControlController {
    @Resource
    private AccessControlService accessControlService;
}

@Service
public class AccessControlServiceImpl implements AccessControlService {
    @Resource
    private DeviceManager deviceManager;

    @Transactional
    public void processAccess() {
        deviceManager.validateDevice();
    }
}

@Component
public class DeviceManager {
    @Resource
    private SmartDeviceDao smartDeviceDao;

    public void validateDevice() {
        // 复杂业务逻辑
    }
}

@Repository
public interface SmartDeviceDao extends BaseDAO<SmartDeviceEntity> {
    // 数据访问操作
}
```

---

*最后更新: 2025-11-16*
*维护者: IOE-DREAM开发团队*