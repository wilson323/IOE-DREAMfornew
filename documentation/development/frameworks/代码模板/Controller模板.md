# Controller层代码模板

**用途**: 标准的RESTful Controller代码模板
**适用场景**: 所有业务模块的Controller层开发
**版本**: v4.0.0 - IOE-DREAM七微服务重构版
**架构**: 严格遵循IOE-DREAM七微服务+四层架构规范

> **🔥 零容忍警告**: 本模板严格遵循IOE-DREAM架构规范，禁止任何违规操作！

## 📋 IOE-DREAM 七微服务架构概述

**七微服务组成**:
- Gateway Service (8080) - API网关
- Common Service (8088) - 公共模块微服务
- DeviceComm Service (8087) - 设备通讯微服务
- OA Service (8089) - OA微服务
- Access Service (8090) - 门禁服务
- Attendance Service (8091) - 考勤服务
- Video Service (8092) - 视频服务
- Consume Service (8094) - 消费服务
- Visitor Service (8095) - 访客服务

**四层架构**: Controller → Service → Manager → DAO

> **🔥 零容忍警告**: 本模板严格遵循IOE-DREAM架构规范，禁止任何违规操作！

## ⚠️ IOE-DREAM 零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 `@Resource` 注入**依赖（禁止@Autowired）
- ✅ **必须使用 `@RestController` 注解**标识控制器
- ✅ **必须实现RESTful API规范**：正确使用HTTP方法
- ✅ **必须使用 `@Valid` 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**：Controller不能直接调用Manager/DAO
- ❌ **禁止使用 `@Autowired` 注入**（零容忍）
- ❌ **禁止跨层访问**：如Controller直接调用Manager/DAO
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止返回非标准格式的响应**
- ❌ **禁止硬编码配置值**

**架构边界铁律**:
```
✅ Controller → Service → Manager → DAO (正确)
❌ Controller → Manager/DAO (违规跨层访问)
❌ Controller直接访问数据库 (严重架构违规)
```

---

## 📋 基础Controller模板

### 标准CRUD操作模板

```java
package net.lab1024.sa.{module}.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.domain.PageResult;
import net.lab1024.sa.common.common.util.SmartBeanUtil;
import net.lab1024.sa.common.common.util.SmartPageUtil;
import net.lab1024.sa.{module}.domain.entity.{Entity}Entity;
import net.lab1024.sa.{module}.domain.form.{Entity}Form;
import net.lab1024.sa.{module}.domain.query.{Entity}QueryForm;
import net.lab1024.sa.{module}.domain.vo.{Entity}VO;
import net.lab1024.sa.{module}..service.{Entity}Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * {模块名称}管理Controller
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
@RestController
@RequestMapping("/api/v1/{module}")
@RequiredArgsConstructor
@Tag(name = "{模块名称}管理", description = "{模块描述}的增删改查操作")
@Validated
public class {Entity}Controller {

    @Resource  // ✅ 强制使用@Resource注入
    private {Entity}Service {entity}Service;

    /**
     * 新增{实体名称}
     *
     * @param addForm 新增表单
     * @return 操作结果
     */
    @Operation(summary = "新增{实体名称}")
    @PostMapping("/add")
    public ResponseDTO<String> add(@RequestBody @Valid {Entity}Form addForm) {
        return ResponseDTO.ok({entity}Service.add(addForm));
    }

    /**
     * 更新{实体名称}
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    @Operation(summary = "更新{实体名称}")
    @PostMapping("/update")
    public ResponseDTO<String> update(@RequestBody @Valid {Entity}Form updateForm) {
        return ResponseDTO.ok({entity}Service.update(updateForm));
    }

    /**
     * 删除{实体名称}
     *
     * @param idForm ID表单
     * @return 操作结果
     */
    @Operation(summary = "删除{实体名称}")
    @PostMapping("/delete")
    public ResponseDTO<String> delete(@RequestBody @Valid IdForm idForm) {
        return ResponseDTO.ok({entity}Service.delete(idForm.getId()));
    }

    /**
     * 根据ID查询{实体名称}
     *
     * @param id 实体ID
     * @return 实体详情
     */
    @Operation(summary = "根据ID查询{实体名称}")
    @GetMapping("/{id}")
    public ResponseDTO<{Entity}VO> getById(@PathVariable Long id) {
        {Entity}Entity entity = {entity}Service.getById(id);
        {Entity}VO vo = SmartBeanUtil.copy(entity, {Entity}VO.class);
        return ResponseDTO.ok(vo);
    }

    /**
     * 分页查询{实体名称}
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @Operation(summary = "分页查询{实体名称}")
    @PostMapping("/page")
    public ResponseDTO<PageResult<{Entity}VO>> page(@RequestBody @Valid {Entity}QueryForm queryForm) {
        PageResult<{Entity}Entity> pageResult = {entity}Service.page(queryForm);

        // 转换为VO
        List<{Entity}VO> voList = SmartBeanUtil.copyList(pageResult.getRows(), {Entity}VO.class);

        return ResponseDTO.ok(SmartPageUtil.copyPage(pageResult, voList));
    }

    /**
     * 查询{实体名称}列表
     *
     * @param queryForm 查询表单
     * @return 实体列表
     */
    @Operation(summary = "查询{实体名称}列表")
    @PostMapping("/list")
    public ResponseDTO<List<{Entity}VO>> list(@RequestBody @Valid {Entity}QueryForm queryForm) {
        List<{Entity}Entity> entityList = {entity}Service.list(queryForm);
        List<{Entity}VO> voList = SmartBeanUtil.copyList(entityList, {Entity}VO.class);
        return ResponseDTO.ok(voList);
    }
}
```

---

## 🔧 高级Controller模板

### 带权限控制的Controller模板

```java
package net.lab1024.sa.{module}.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.domain.PageResult;
import net.lab1024.sa.common.common.util.SmartBeanUtil;
import net.lab1024.sa.{module}.domain.entity.{Entity}Entity;
import net.lab1024.sa.{module}.domain.form.{Entity}Form;
import net.lab1024.sa.{module}.domain.query.{Entity}QueryForm;
import net.lab1024.sa.{module}.domain.vo.{Entity}VO;
import net.lab1024.sa.{module}.service.{Entity}Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * {模块名称}管理Controller (带权限控制)
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/{module}")
@RequiredArgsConstructor
@SaCheckLogin  // 必须登录
@Tag(name = "{模块名称}管理", description = "{模块描述}的增删改查操作")
@Validated
public class {Entity}Controller {

    @Resource  // ✅ 强制使用@Resource注入
    private {Entity}Service {entity}Service;

    /**
     * 新增{实体名称}
     * 权限要求: {module}:add
     *
     * @param addForm 新增表单
     * @return 操作结果
     */
    @Operation(summary = "新增{实体名称}")
    @PostMapping("/add")
    @SaCheckPermission("{module}:add")
    public ResponseDTO<String> add(@RequestBody @Valid {Entity}Form addForm) {
        log.info("新增{实体名称}, 名称: {}", addForm.getName());
        return ResponseDTO.ok({entity}Service.add(addForm));
    }

    /**
     * 更新{实体名称}
     * 权限要求: {module}:update
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    @Operation(summary = "更新{实体名称}")
    @PostMapping("/update")
    @SaCheckPermission("{module}:update")
    public ResponseDTO<String> update(@RequestBody @Valid {Entity}Form updateForm) {
        log.info("更新{实体名称}, ID: {}, 名称: {}", updateForm.getId(), updateForm.getName());
        return ResponseDTO.ok({entity}Service.update(updateForm));
    }

    /**
     * 删除{实体名称}
     * 权限要求: {module}:delete
     *
     * @param idForm ID表单
     * @return 操作结果
     */
    @Operation(summary = "删除{实体名称}")
    @PostMapping("/delete")
    @SaCheckPermission("{module}:delete")
    public ResponseDTO<String> delete(@RequestBody @Valid IdForm idForm) {
        log.warn("删除{实体名称}, ID: {}", idForm.getId());
        return ResponseDTO.ok({entity}Service.delete(idForm.getId()));
    }

    /**
     * 分页查询{实体名称}
     * 权限要求: {module}:query
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @Operation(summary = "分页查询{实体名称}")
    @PostMapping("/page")
    @SaCheckPermission("{module}:query")
    public ResponseDTO<PageResult<{Entity}VO>> page(@RequestBody @Valid {Entity}QueryForm queryForm) {
        log.debug("分页查询{实体名称}, 参数: {}", queryForm);
        PageResult<{Entity}Entity> pageResult = {entity}Service.page(queryForm);
        List<{Entity}VO> voList = SmartBeanUtil.copyList(pageResult.getRows(), {Entity}VO.class);
        return ResponseDTO.ok(SmartPageUtil.copyPage(pageResult, voList));
    }
}
```

### 带缓存优化的Controller模板

```java
package net.lab1024.sa.{module}.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.domain.PageResult;
import net.lab1024.sa.common.common.util.SmartBeanUtil;
import net.lab1024.sa.common.common.cache.SmartCache;
import net.lab1024.sa.{module}.domain.entity.{Entity}Entity;
import net.lab1024.sa.{module}.domain.form.{Entity}Form;
import net.lab1024.sa.{module}.domain.query.{Entity}QueryForm;
import net.lab1024.sa.{module}.domain.vo.{Entity}VO;
import net.lab1024.sa.{module}.service.{Entity}Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {模块名称}管理Controller (带缓存优化)
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/{module}")
@RequiredArgsConstructor
@Tag(name = "{模块名称}管理", description = "{模块描述}的增删改查操作")
@Validated
public class {Entity}Controller {

    @Resource  // ✅ 强制使用@Resource注入
    private {Entity}Service {entity}Service;

    /**
     * 缓存key前缀
     */
    private static final String CACHE_PREFIX = "{module}:";

    /**
     * 新增{实体名称}
     *
     * @param addForm 新增表单
     * @return 操作结果
     */
    @Operation(summary = "新增{实体名称}")
    @PostMapping("/add")
    public ResponseDTO<String> add(@RequestBody @Valid {Entity}Form addForm) {
        String result = {entity}Service.add(addForm);

        // 清除相关缓存
        clearCache();

        return ResponseDTO.ok(result);
    }

    /**
     * 更新{实体名称}
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    @Operation(summary = "更新{实体名称}")
    @PostMapping("/update")
    public ResponseDTO<String> update(@RequestBody @Valid {Entity}Form updateForm) {
        String result = {entity}Service.update(updateForm);

        // 清除相关缓存
        clearCache();
        clearEntityCache(updateForm.getId());

        return ResponseDTO.ok(result);
    }

    /**
     * 根据ID查询{实体名称} (带缓存)
     *
     * @param id 实体ID
     * @return 实体详情
     */
    @Operation(summary = "根据ID查询{实体名称}")
    @GetMapping("/{id}")
    public ResponseDTO<{Entity}VO> getById(@PathVariable Long id) {
        String cacheKey = CACHE_PREFIX + id;

        // 从缓存获取
        {Entity}VO cachedVO = SmartCache.get(cacheKey, {Entity}VO.class);
        if (cachedVO != null) {
            log.debug("从缓存获取{实体名称}, ID: {}", id);
            return ResponseDTO.ok(cachedVO);
        }

        // 从数据库获取
        {Entity}Entity entity = {entity}Service.getById(id);
        {Entity}VO vo = SmartBeanUtil.copy(entity, {Entity}VO.class);

        // 放入缓存
        SmartCache.set(cacheKey, vo, 30, TimeUnit.MINUTES);
        log.debug("缓存{实体名称}, ID: {}", id);

        return ResponseDTO.ok(vo);
    }

    /**
     * 分页查询{实体名称} (带缓存)
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @Operation(summary = "分页查询{实体名称}")
    @PostMapping("/page")
    public ResponseDTO<PageResult<{Entity}VO>> page(@RequestBody @Valid {Entity}QueryForm queryForm) {
        // 生成缓存key (基于查询参数)
        String cacheKey = generatePageCacheKey(queryForm);

        // 尝试从缓存获取
        PageResult<{Entity}VO> cachedResult = SmartCache.get(cacheKey, PageResult.class);
        if (cachedResult != null) {
            log.debug("从缓存获取分页数据, key: {}", cacheKey);
            return ResponseDTO.ok(cachedResult);
        }

        // 从数据库查询
        PageResult<{Entity}Entity> pageResult = {entity}Service.page(queryForm);
        List<{Entity}VO> voList = SmartBeanUtil.copyList(pageResult.getRows(), {Entity}VO.class);
        PageResult<{Entity}VO> result = SmartPageUtil.copyPage(pageResult, voList);

        // 放入缓存
        SmartCache.set(cacheKey, result, 10, TimeUnit.MINUTES);
        log.debug("缓存分页数据, key: {}", cacheKey);

        return ResponseDTO.ok(result);
    }

    /**
     * 清除{实体名称}相关缓存
     */
    private void clearCache() {
        // 这里可以使用通配符清除模式相关的所有缓存
        log.info("清除{实体名称}相关缓存");
        SmartCache.deleteByPattern(CACHE_PREFIX + "*");
    }

    /**
     * 清除特定实体缓存
     */
    private void clearEntityCache(Long id) {
        String cacheKey = CACHE_PREFIX + id;
        SmartCache.delete(cacheKey);
        log.debug("清除实体缓存, key: {}", cacheKey);
    }

    /**
     * 生成分页查询缓存key
     */
    private String generatePageCacheKey({Entity}QueryForm queryForm) {
        return CACHE_PREFIX + "page:" + queryForm.hashCode();
    }
}
```

---

## 📝 使用说明

### 1. 模板替换规则

**替换变量**:
- `{module}`: 模块名称 (如: access, attendance, consume)
- `{Entity}`: 实体类名称 (如: AccessDevice, AttendanceRecord)
- `{实体名称}`: 实体中文名称 (如: 访问设备, 考勤记录)
- `{模块描述}`: 模块功能描述 (如: 门禁设备管理, 考勤记录管理)

### 2. 导入依赖

**必需依赖** (pom.xml):
```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Sa-Token 权限框架 -->
    <dependency>
        <groupId>cn.dev33</groupId>
        <artifactId>sa-token-spring-boot-starter</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Swagger API文档 -->
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-boot-starter-api</artifactId>
    </dependency>

    <!-- 公共模块 -->
    <dependency>
        <groupId>net.lab1024</groupId>
        <artifactId>sa-base</artifactId>
    </dependency>
</dependencies>
```

### 3. 基础类说明

**ResponseDTO**: 统一响应对象
```java
public class ResponseDTO<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ResponseDTO<T> ok(T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    public static <T> ResponseDTO<T> error(String message) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(500);
        response.setMessage(message);
        return response;
    }
}
```

**PageResult**: 分页结果对象
```java
public class PageResult<T> {
    private Long total;
    private List<T> rows;
    private Integer pageNum;
    private Integer pageSize;
}
```

### 4. IOE-DREAM架构合规检查清单

**Controller层P0级检查清单**（零容忍违规）:
- [ ] ✅ 使用 `@RestController` 注解
- [ ] ✅ 使用 `@RequestMapping` 定义基础路径
- [ ] ✅ **强制使用 `@Resource` 注入Service**（禁止@Autowired）
- [ ] ✅ 使用 `@Valid` 验证请求参数
- [ ] ✅ 使用 `ResponseDTO` 封装返回结果
- [ ] ✅ 使用 `@Operation` 注解添加API文档
- [ ] ✅ 遵循RESTful API设计规范（GET查询、POST创建、PUT更新、DELETE删除）
- [ ] ❌ **确保无 @Autowired 违规使用**
- [ ] ❌ **确保无跨层访问**（Controller不直接调用Manager/DAO）
- [ ] ❌ **确保无业务逻辑在Controller中**

**IOE-DREAM企业级特性检查清单**:
- [ ] 权限控制注解正确使用（@SaCheckLogin/@SaCheckPermission）
- [ ] 接口限流防刷（@RateLimiter）
- [ ] 异步处理机制（@Async）
- [ ] 熔断降级配置（@CircuitBreaker）
- [ ] 分布式追踪注解（@NewSpan）
- [ ] 敏感数据脱敏处理
- [ ] 操作审计日志记录

**代码质量检查清单**:
- [ ] 添加适当的日志记录（@Slf4j）
- [ ] 异常处理完善（GlobalExceptionHandler）
- [ ] 参数验证完整（@Valid）
- [ ] API文档完整（Swagger/Knife4j）
- [ ] 单元测试覆盖（≥80%）
- [ ] 性能监控埋点（Micrometer）

---

## 🚨 注意事项

### 1. 严格遵循架构规范
- **禁止跨层访问**: Controller不能直接访问Manager或DAO
- **必须使用@Resource**: 禁止使用@Autowired
- **统一的响应格式**: 必须使用ResponseDTO
- **参数验证**: 必须使用@Valid进行参数验证

### 2. 安全注意事项
- **权限控制**: 敏感操作必须添加权限检查
- **参数安全**: 防止SQL注入和XSS攻击
- **日志安全**: 避免在日志中记录敏感信息
- **输入验证**: 严格验证所有输入参数

### 3. 性能优化
- **缓存使用**: 频繁查询的数据使用缓存
- **分页查询**: 大数据量查询必须分页
- **异步处理**: 耗时操作使用异步处理
- **连接池**: 合理配置数据库连接池

---

## 📚 相关文档

- [全局架构规范](../../01-核心规范/架构规范/全局架构规范.md)
- [Java编码规范](../../01-核心规范/开发规范/Java编码规范.md)
- [API设计规范](../../01-核心规范/开发规范/API设计规范.md)
- [Service层模板](./Service模板.md)
- [Manager层模板](./Manager模板.md)
- [DAO层模板](./DAO模板.md)

---

**模板版本**: v2.0.0
**最后更新**: 2025-12-02
**维护团队**: IOE-DREAM架构委员会

**🎯 使用此模板可以确保Controller层代码的规范性和一致性！**