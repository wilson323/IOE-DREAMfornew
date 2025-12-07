# Service层代码模板

**用途**: 标准的Service层业务逻辑代码模板
**适用场景**: 所有业务模块的Service层开发
**版本**: v2.0.0 (基于IOE-DREAM架构规范)

---

## 📋 基础Service模板

### 标准CRUD业务逻辑模板

```java
package net.lab1024.sa.{module}.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.domain.PageResult;
import net.lab1024.sa.common.common.util.SmartBeanUtil;
import net.lab1024.sa.common.common.util.SmartPageUtil;
import net.lab1024.sa.common.common.exception.BusinessException;
import net.lab1024.sa.{module}.domain.entity.{Entity}Entity;
import net.lab1024.sa.{module}.domain.form.{Entity}Form;
import net.lab1024.sa.{module}.domain.query.{Entity}QueryForm;
import net.lab1024.sa.{module}.dao.{Entity}Dao;
import net.lab1024.sa.{module}.manager.{Entity}Manager;
import net.lab1024.sa.{module}.service.{Entity}Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * {模块名称}Service实现
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class {Entity}ServiceImpl implements {Entity}Service {

    private final {Entity}Dao {entity}Dao;
    private final {Entity}Manager {entity}Manager;

    /**
     * 新增{实体名称}
     *
     * @param addForm 新增表单
     * @return 操作结果
     */
    @Override
    public String add({Entity}Form addForm) {
        log.info("开始新增{实体名称}: {}", addForm.getName());

        // 1. 参数验证
        this.validateAddForm(addForm);

        // 2. 检查重复数据
        this.checkDuplicate(addForm, null);

        // 3. 构建实体
        {Entity}Entity entity = SmartBeanUtil.copy(addForm, {Entity}Entity.class);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        entity.setDeletedFlag(false);

        // 4. 保存数据
        int insertCount = {entity}Dao.insert(entity);
        if (insertCount != 1) {
            throw new BusinessException("新增{实体名称}失败");
        }

        // 5. 清除缓存
        {entity}Manager.removeCache();

        log.info("新增{实体名称}成功, ID: {}", entity.getId());
        return ResponseStringConst.SUCCESS;
    }

    /**
     * 更新{实体名称}
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    @Override
    public String update({Entity}Form updateForm) {
        log.info("开始更新{实体名称}: ID={}, 名称={}", updateForm.getId(), updateForm.getName());

        // 1. 参数验证
        this.validateUpdateForm(updateForm);

        // 2. 检查数据是否存在
        {Entity}Entity existingEntity = {entity}Dao.selectById(updateForm.getId());
        if (existingEntity == null || existingEntity.getDeletedFlag()) {
            throw new BusinessException("{实体名称}不存在");
        }

        // 3. 检查重复数据
        this.checkDuplicate(updateForm, updateForm.getId());

        // 4. 更新数据
        {Entity}Entity updateEntity = SmartBeanUtil.copy(updateForm, {Entity}Entity.class);
        updateEntity.setUpdateTime(LocalDateTime.now());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());

        int updateCount = {entity}Dao.updateById(updateEntity);
        if (updateCount != 1) {
            throw new BusinessException("更新{实体名称}失败");
        }

        // 5. 清除缓存
        {entity}Manager.removeCache();
        {entity}Manager.removeEntityCache(updateForm.getId());

        log.info("更新{实体名称}成功, ID: {}", updateForm.getId());
        return ResponseStringConst.SUCCESS;
    }

    /**
     * 删除{实体名称}
     *
     * @param id 实体ID
     * @return 操作结果
     */
    @Override
    public String delete(Long id) {
        log.warn("开始删除{实体名称}, ID: {}", id);

        // 1. 检查数据是否存在
        {Entity}Entity existingEntity = {entity}Dao.selectById(id);
        if (existingEntity == null || existingEntity.getDeletedFlag()) {
            throw new BusinessException("{实体名称}不存在");
        }

        // 2. 检查是否可以删除
        this.checkCanDelete(id);

        // 3. 软删除数据
        {Entity}Entity deleteEntity = new {Entity}Entity();
        deleteEntity.setId(id);
        deleteEntity.setDeletedFlag(true);
        deleteEntity.setUpdateTime(LocalDateTime.now());
        deleteEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());

        int deleteCount = {entity}Dao.updateById(deleteEntity);
        if (deleteCount != 1) {
            throw new BusinessException("删除{实体名称}失败");
        }

        // 4. 清除缓存
        {entity}Manager.removeCache();
        {entity}Manager.removeEntityCache(id);

        log.warn("删除{实体名称}成功, ID: {}", id);
        return ResponseStringConst.SUCCESS;
    }

    /**
     * 根据ID查询{实体名称}
     *
     * @param id 实体ID
     * @return 实体信息
     */
    @Override
    @Transactional(readOnly = true)
    public {Entity}Entity getById(Long id) {
        return {entity}Manager.getByIdWithCache(id);
    }

    /**
     * 分页查询{实体名称}
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<{Entity}Entity> page({Entity}QueryForm queryForm) {
        // 构建查询条件
        LambdaQueryWrapper<{Entity}Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq({Entity}Entity::getDeletedFlag, false);

        // 添加查询条件
        this.buildQueryCondition(queryWrapper, queryForm);

        // 设置排序
        queryWrapper.orderByDesc({Entity}Entity::getCreateTime);

        // 执行分页查询
        Page<{Entity}Entity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        IPage<{Entity}Entity> pageResult = {entity}Dao.selectPage(page, queryWrapper);

        return SmartPageUtil.convertPage(pageResult);
    }

    /**
     * 查询{实体名称}列表
     *
     * @param queryForm 查询条件
     * @return 实体列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<{Entity}Entity> list({Entity}QueryForm queryForm) {
        // 构建查询条件
        LambdaQueryWrapper<{Entity}Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq({Entity}Entity::getDeletedFlag, false);

        // 添加查询条件
        this.buildQueryCondition(queryWrapper, queryForm);

        // 设置排序
        queryWrapper.orderByDesc({Entity}Entity::getCreateTime);

        // 设置查询数量限制
        if (queryForm.getLimit() != null && queryForm.getLimit() > 0) {
            queryWrapper.last("LIMIT " + queryForm.getLimit());
        }

        return {entity}Dao.selectList(queryWrapper);
    }

    // ==================== 私有方法 ====================

    /**
     * 验证新增表单
     */
    private void validateAddForm({Entity}Form addForm) {
        if (addForm == null) {
            throw new BusinessException("新增信息不能为空");
        }

        // 验证必填字段
        if (!StringUtils.hasText(addForm.getName())) {
            throw new BusinessException("名称不能为空");
        }

        // 验证字段长度
        if (addForm.getName().length() > 50) {
            throw new BusinessException("名称长度不能超过50个字符");
        }

        // 验证业务规则
        this.validateBusinessRules(addForm);
    }

    /**
     * 验证更新表单
     */
    private void validateUpdateForm({Entity}Form updateForm) {
        if (updateForm == null || updateForm.getId() == null) {
            throw new BusinessException("更新信息不能为空");
        }

        // 验证必填字段
        if (!StringUtils.hasText(updateForm.getName())) {
            throw new BusinessException("名称不能为空");
        }

        // 验证字段长度
        if (updateForm.getName().length() > 50) {
            throw new BusinessException("名称长度不能超过50个字符");
        }

        // 验证业务规则
        this.validateBusinessRules(updateForm);
    }

    /**
     * 验证业务规则
     */
    private void validateBusinessRules({Entity}Form form) {
        // 这里添加特定的业务验证逻辑
        // 例如：检查状态是否有效、检查关联数据是否存在等
    }

    /**
     * 检查重复数据
     */
    private void checkDuplicate({Entity}Form form, Long excludeId) {
        LambdaQueryWrapper<{Entity}Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq({Entity}Entity::getDeletedFlag, false);
        queryWrapper.eq({Entity}Entity::getName, form.getName());

        if (excludeId != null) {
            queryWrapper.ne({Entity}Entity::getId, excludeId);
        }

        Long count = {entity}Dao.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("名称已存在: " + form.getName());
        }
    }

    /**
     * 检查是否可以删除
     */
    private void checkCanDelete(Long id) {
        // 检查是否有关联数据
        // 例如：检查是否有其他记录引用了当前实体

        // 示例：检查是否有子记录
        LambdaQueryWrapper<{RelatedEntity}Entity> relatedWrapper = new LambdaQueryWrapper<>();
        relatedWrapper.eq({RelatedEntity}Entity::get{Entity}Id, id);
        relatedWrapper.eq({RelatedEntity}Entity::getDeletedFlag, false);

        Long relatedCount = {relatedEntity}Dao.selectCount(relatedWrapper);
        if (relatedCount > 0) {
            throw new BusinessException("存在关联数据，无法删除");
        }
    }

    /**
     * 构建查询条件
     */
    private void buildQueryCondition(LambdaQueryWrapper<{Entity}Entity> queryWrapper, {Entity}QueryForm queryForm) {
        // 名称模糊查询
        if (StringUtils.hasText(queryForm.getName())) {
            queryWrapper.like({Entity}Entity::getName, queryForm.getName());
        }

        // 状态精确查询
        if (queryForm.getStatus() != null) {
            queryWrapper.eq({Entity}Entity::getStatus, queryForm.getStatus());
        }

        // 时间范围查询
        if (queryForm.getStartTime() != null) {
            queryWrapper.ge({Entity}Entity::getCreateTime, queryForm.getStartTime());
        }
        if (queryForm.getEndTime() != null) {
            queryWrapper.le({Entity}Entity::getCreateTime, queryForm.getEndTime());
        }

        // 创建人查询
        if (queryForm.getCreateUserId() != null) {
            queryWrapper.eq({Entity}Entity::getCreateUserId, queryForm.getCreateUserId());
        }
    }
}
```

---

## 🔧 高级Service模板
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

### 带复杂业务逻辑的Service模板

```java
package net.lab1024.sa.{module}.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.common.exception.BusinessException;
import net.lab1024.sa.common.common.util.SmartBeanUtil;
import net.lab1024.sa.{module}.domain.entity.{Entity}Entity;
import net.lab1024.sa.{module}.domain.form.{Entity}Form;
import net.lab1024.sa.{module}.dao.{Entity}Dao;
import net.lab1024.sa.{module}.manager.{Entity}Manager;
import net.lab1024.sa.{module}.service.{Entity}Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {模块名称}Service实现 (复杂业务逻辑)
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class {Entity}ServiceImpl implements {Entity}Service {

    private final {Entity}Dao {entity}Dao;
    private final {Entity}Manager {entity}Manager;
    private final RelatedService relatedService;

    /**
     * 批量处理{实体名称}
     *
     * @param formList 批量表单
     * @return 处理结果
     */
    @Override
    public String batchProcess(List<{Entity}Form> formList) {
        log.info("开始批量处理{实体名称}, 数量: {}", formList.size());

        if (CollectionUtils.isEmpty(formList)) {
            throw new BusinessException("批量处理数据不能为空");
        }

        // 验证批量数据
        this.validateBatchForm(formList);

        // 按类型分组处理
        Map<String, List<{Entity}Form>> groupedForms = formList.stream()
                .collect(Collectors.groupingBy(form -> this.getOperationType(form)));

        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<String, List<{Entity}Form>> entry : groupedForms.entrySet()) {
            String operationType = entry.getKey();
            List<{Entity}Form> forms = entry.getValue();

            try {
                switch (operationType) {
                    case "ADD":
                        successCount += this.batchAdd(forms);
                        break;
                    case "UPDATE":
                        successCount += this.batchUpdate(forms);
                        break;
                    case "DELETE":
                        successCount += this.batchDelete(forms);
                        break;
                    default:
                        throw new BusinessException("不支持的操作类型: " + operationType);
                }
            } catch (Exception e) {
                log.error("批量处理失败, 操作类型: {}, 数据: {}", operationType, forms, e);
                failCount += forms.size();
            }
        }

        // 清除缓存
        {entity}Manager.removeCache();

        log.info("批量处理完成, 成功: {}, 失败: {}", successCount, failCount);
        return String.format("批量处理完成，成功: %d, 失败: %d", successCount, failCount);
    }

    /**
     * 复杂业务操作
     *
     * @param operationForm 操作表单
     * @return 操作结果
     */
    @Override
    public String complexOperation({Entity}OperationForm operationForm) {
        log.info("开始执行复杂业务操作: {}", operationForm.getOperationType());

        try {
            // 1. 参数验证和前置检查
            this.validateComplexOperation(operationForm);

            // 2. 执行业务逻辑
            switch (operationForm.getOperationType()) {
                case "ACTIVATION":
                    return this.handleActivation(operationForm);
                case "SUSPENSION":
                    return this.handleSuspension(operationForm);
                case "TRANSFER":
                    return this.handleTransfer(operationForm);
                default:
                    throw new BusinessException("不支持的操作类型: " + operationForm.getOperationType());
            }

        } catch (Exception e) {
            log.error("复杂业务操作失败: {}", operationForm.getOperationType(), e);
            throw new BusinessException("操作失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 批量新增
     */
    private int batchAdd(List<{Entity}Form> addForms) {
        List<{Entity}Entity> entityList = addForms.stream()
                .map(form -> {
                    {Entity}Entity entity = SmartBeanUtil.copy(form, {Entity}Entity.class);
                    entity.setCreateTime(LocalDateTime.now());
                    entity.setUpdateTime(LocalDateTime.now());
                    entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
                    entity.setDeletedFlag(false);
                    return entity;
                })
                .collect(Collectors.toList());

        // 批量插入
        int insertCount = {entity}Dao.insertBatch(entityList);
        log.info("批量新增完成, 插入数量: {}", insertCount);

        return insertCount;
    }

    /**
     * 批量更新
     */
    private int batchUpdate(List<{Entity}Form> updateForms) {
        int updateCount = 0;

        for ({Entity}Form form : updateForms) {
            {Entity}Entity updateEntity = SmartBeanUtil.copy(form, {Entity}Entity.class);
            updateEntity.setUpdateTime(LocalDateTime.now());
            updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());

            updateCount += {entity}Dao.updateById(updateEntity);
        }

        log.info("批量更新完成, 更新数量: {}", updateCount);
        return updateCount;
    }

    /**
     * 批量删除
     */
    private int batchDelete(List<{Entity}Form> deleteForms) {
        List<Long> ids = deleteForms.stream()
                .map({Entity}Form::getId)
                .collect(Collectors.toList());

        // 软删除
        int deleteCount = {entity}Dao.batchDelete(ids);
        log.info("批量删除完成, 删除数量: {}", deleteCount);

        return deleteCount;
    }

    /**
     * 处理激活操作
     */
    private String handleActivation({Entity}OperationForm operationForm) {
        // 1. 验证激活条件
        this.validateActivationConditions(operationForm);

        // 2. 执行激活逻辑
        {Entity}Entity entity = {entity}Dao.selectById(operationForm.getEntityId());
        if (entity == null || entity.getDeletedFlag()) {
            throw new BusinessException("{实体名称}不存在");
        }

        entity.setStatus({Entity}Status.ACTIVE);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());

        int updateCount = {entity}Dao.updateById(entity);
        if (updateCount != 1) {
            throw new BusinessException("激活操作失败");
        }

        // 3. 执行后置处理
        this.postActivationProcessing(entity);

        log.info("激活操作完成, ID: {}", operationForm.getEntityId());
        return "激活成功";
    }

    /**
     * 处理暂停操作
     */
    private String handleSuspension({Entity}OperationForm operationForm) {
        // 1. 验证暂停条件
        this.validateSuspensionConditions(operationForm);

        // 2. 执行暂停逻辑
        {Entity}Entity entity = {entity}Dao.selectById(operationForm.getEntityId());
        if (entity == null || entity.getDeletedFlag()) {
            throw new BusinessException("{实体名称}不存在");
        }

        entity.setStatus({Entity}Status.SUSPENDED);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());

        int updateCount = {entity}Dao.updateById(entity);
        if (updateCount != 1) {
            throw new BusinessException("暂停操作失败");
        }

        // 3. 执行后置处理
        this.postSuspensionProcessing(entity);

        log.info("暂停操作完成, ID: {}", operationForm.getEntityId());
        return "暂停成功";
    }

    /**
     * 处理转移操作
     */
    private String handleTransfer({Entity}OperationForm operationForm) {
        // 1. 验证转移条件
        this.validateTransferConditions(operationForm);

        // 2. 执行转移逻辑
        {Entity}Entity entity = {entity}Dao.selectById(operationForm.getEntityId());
        if (entity == null || entity.getDeletedFlag()) {
            throw new BusinessException("{实体名称}不存在");
        }

        // 更新归属信息
        entity.setOwnerId(operationForm.getTargetUserId());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());

        int updateCount = {entity}Dao.updateById(entity);
        if (updateCount != 1) {
            throw new BusinessException("转移操作失败");
        }

        // 3. 记录转移日志
        this.recordTransferLog(entity, operationForm);

        log.info("转移操作完成, ID: {}, 目标用户: {}",
                operationForm.getEntityId(), operationForm.getTargetUserId());
        return "转移成功";
    }

    // 其他辅助方法...
    private String getOperationType({Entity}Form form) {
        if (form.getId() == null) {
            return "ADD";
        }
        return form.getDeletedFlag() ? "DELETE" : "UPDATE";
    }

    private void validateBatchForm(List<{Entity}Form> formList) {
        // 批量数据验证逻辑
    }

    private void validateComplexOperation({Entity}OperationForm operationForm) {
        // 复杂操作前置验证
    }

    private void validateActivationConditions({Entity}OperationForm operationForm) {
        // 激活条件验证
    }

    private void validateSuspensionConditions({Entity}OperationForm operationForm) {
        // 暂停条件验证
    }

    private void validateTransferConditions({Entity}OperationForm operationForm) {
        // 转移条件验证
    }

    private void postActivationProcessing({Entity}Entity entity) {
        // 激活后置处理
    }

    private void postSuspensionProcessing({Entity}Entity entity) {
        // 暂停后置处理
    }

    private void recordTransferLog({Entity}Entity entity, {Entity}OperationForm operationForm) {
        // 记录转移日志
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
- `{RelatedEntity}`: 关联实体类名称

### 2. 必需依赖

**Maven依赖**:
```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- 公共模块 -->
    <dependency>
        <groupId>net.lab1024</groupId>
        <artifactId>sa-base</artifactId>
    </dependency>
</dependencies>
```

### 3. 代码规范检查清单

**Service层检查清单**:
- [ ] 使用 `@Service` 注解
- [ ] 使用 `@RequiredArgsConstructor` 或 `@Resource` 注入依赖
- [ ] 使用 `@Transactional` 注解管理事务
- [ ] 参数验证完整
- [ ] 业务异常处理完善
- [ ] 日志记录适当
- [ ] 缓存使用合理
- [ ] 遵循单一职责原则

---

## 🚨 注意事项

### 1. 事务管理
- **读操作**: 使用 `@Transactional(readOnly = true)`
- **写操作**: 使用 `@Transactional(rollbackFor = Exception.class)`
- **嵌套事务**: 注意事务传播行为

### 2. 异常处理
- **业务异常**: 使用 `BusinessException`
- **参数异常**: 验证失败时抛出具体异常信息
- **系统异常**: 记录详细日志并重新抛出

### 3. 性能优化
- **缓存策略**: 频繁查询使用Manager层缓存
- **批量操作**: 避免循环中的单条操作
- **分页查询**: 大数据量必须分页

---

## 📚 相关文档

- [全局架构规范](../../01-核心规范/架构规范/全局架构规范.md)
- [Java编码规范](../../01-核心规范/开发规范/Java编码规范.md)
- [Controller层模板](./Controller模板.md)
- [Manager层模板](./Manager模板.md)
- [DAO层模板](./DAO模板.md)

---

**模板版本**: v2.0.0
**最后更新**: 2025-12-02
**维护团队**: IOE-DREAM架构委员会

**🎯 使用此模板可以确保Service层代码的规范性和业务逻辑的完整性！**