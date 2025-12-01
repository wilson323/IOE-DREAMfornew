# AI智能重构模板库

## 🔧 模板1: SmartAdmin v4 Result类标准结构

### 标准Result类模板
```java
package {package_path};

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {class_name}结果类
 * 遵循SmartAdmin v4 Result类设计规范
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {class_name}Result {

    /**
     * 操作是否成功
     */
    @Builder.Default
    private boolean success = false;

    /**
     * 错误信息（失败时使用）
     */
    private String errorMessage;

    /**
     * 错误代码（可选）
     */
    private String errorCode;

    // AI根据业务需求自动推断并添加相关字段
    // {business_fields}

    /**
     * 创建成功结果
     */
    public static {class_name}Result success() {
        return {class_name}Result.builder()
                .success(true)
                .build();
    }

    /**
     * 创建成功结果（带数据）
     */
    public static {class_name}Result success({data_type} data) {
        return {class_name}Result.builder()
                .success(true)
                .{data_field}(data)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static {class_name}Result failure(String errorMessage) {
        return {class_name}Result.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
```

## 🔧 模板2: SmartAdmin v4 Entity类标准结构

### 标准Entity类模板
```java
package {package_path};

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * {table_comment}实体类
 * 遵循SmartAdmin v4 Entity设计规范
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("{table_name}")
public class {entity_name} extends BaseEntity {

    // AI根据数据库表结构自动生成字段定义
    {table_fields}

    // 注意：不要重复定义BaseEntity已有的审计字段：
    // - createTime (创建时间)
    // - updateTime (更新时间)
    // - createUserId (创建人ID)
    // - updateUserId (更新人ID)
    // - deletedFlag (删除标识)
    // - version (版本号)
}
```

### 字段定义示例
```java
/**
 * 设备名称
 */
@TableField("device_name")
private String deviceName;

/**
 * 设备状态
 * @see DeviceStatusEnum
 */
@TableField("device_status")
private Integer deviceStatus;

/**
 * 创建时间 - 注意：BaseEntity已包含，不要重复定义
 * private LocalDateTime createTime; // ❌ 错误：重复定义
 */
```

## 🔧 模板3: SmartAdmin v4 Controller类标准结构

### 标准Controller类模板
```java
package {package_path};

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.response.ResponseDTO;
import net.lab1024.sa.base.common.validate.ValidateGroup;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * {module_name}控制器
 * 遵循SmartAdmin v4 Controller设计规范
 */
@Slf4j
@RestController
@RequestMapping("/api/{module_path}")
@Tag(name = "{module_name}相关接口", description = "{module_description}")
@Validated
public class {controller_name} {

    @Resource
    private {service_name} {service_instance};

    /**
     * 分页查询{module_name}
     */
    @Operation(summary = "分页查询{module_name}")
    @GetMapping("/page")
    @SaCheckPermission("{module_permission}:query")
    public ResponseDTO<PageResult<{entity_name}VO>> queryPage(
            @Validated(ValidateGroup.class) {entity_name}QueryForm queryForm) {

        try {
            PageResult<{entity_name}VO> pageResult = {service_instance}.queryPage(queryForm);
            return ResponseDTO.ok(pageResult);
        } catch (Exception e) {
            log.error("查询{module_name}分页失败", e);
            return ResponseDTO.userErrorParam("查询失败");
        }
    }

    /**
     * 添加{module_name}
     */
    @Operation(summary = "添加{module_name}")
    @PostMapping("/add")
    @SaCheckPermission("{module_permission}:add")
    public ResponseDTO<String> add(@RequestBody @Valid {entity_name}Form addForm) {

        try {
            {service_instance}.add(addForm);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("添加{module_name}失败", e);
            return ResponseDTO.userErrorParam("添加失败");
        }
    }

    /**
     * 更新{module_name}
     */
    @Operation(summary = "更新{module_name}")
    @PostMapping("/update")
    @SaCheckPermission("{module_permission}:update")
    public ResponseDTO<String> update(@RequestBody @Valid {entity_name}Form updateForm) {

        try {
            {service_instance}.update(updateForm);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("更新{module_name}失败", e);
            return ResponseDTO.userErrorParam("更新失败");
        }
    }

    /**
     * 删除{module_name}
     */
    @Operation(summary = "删除{module_name}")
    @PostMapping("/delete")
    @SaCheckPermission("{module_permission}:delete")
    public ResponseDTO<String> delete(@RequestBody @Valid Long id) {

        try {
            {service_instance}.delete(id);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("删除{module_name}失败", e);
            return ResponseDTO.userErrorParam("删除失败");
        }
    }
}
```

## 🔧 模板4: SmartAdmin v4 Service类标准结构

### 标准Service类模板
```java
package {package_path};

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

/**
 * {module_name}服务层
 * 遵循SmartAdmin v4 Service设计规范
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class {service_name} {

    @Resource
    private {manager_name} {manager_instance};

    /**
     * 分页查询{module_name}
     */
    public PageResult<{entity_name}VO> queryPage({entity_name}QueryForm queryForm) {
        log.info("分页查询{module_name}, 参数: {}", queryForm);

        try {
            return {manager_instance}.queryPage(queryForm);
        } catch (Exception e) {
            log.error("分页查询{module_name}失败", e);
            throw new BusinessException("查询失败");
        }
    }

    /**
     * 添加{module_name}
     */
    public void add({entity_name}Form addForm) {
        log.info("添加{module_name}, 参数: {}", addForm);

        try {
            // AI自动推断业务逻辑
            {business_logic}
        } catch (Exception e) {
            log.error("添加{module_name}失败", e);
            throw new BusinessException("添加失败");
        }
    }

    /**
     * 更新{module_name}
     */
    public void update({entity_name}Form updateForm) {
        log.info("更新{module_name}, 参数: {}", updateForm);

        try {
            // AI自动推断业务逻辑
            {business_logic}
        } catch (Exception e) {
            log.error("更新{module_name}失败", e);
            throw new BusinessException("更新失败");
        }
    }

    /**
     * 删除{module_name}
     */
    public void delete(Long id) {
        log.info("删除{module_name}, ID: {}", id);

        try {
            // AI自动推断删除逻辑（软删除）
            {manager_instance}.delete(id);
        } catch (Exception e) {
            log.error("删除{module_name}失败", e);
            throw new BusinessException("删除失败");
        }
    }
}
```

## 🔧 模板5: SmartAdmin v4 Manager类标准结构

### 标准Manager类模板
```java
package {package_path};

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import jakarta.annotation.Resource;

/**
 * {module_name}管理器
 * 遵循SmartAdmin v4 Manager设计规范
 * 负责复杂业务逻辑和跨模块调用
 */
@Slf4j
@Component
public class {manager_name} {

    @Resource
    private {dao_name} {dao_instance};

    // 根据需要注入其他Manager
    // @Resource
    // private OtherManager otherManager;

    /**
     * 分页查询{module_name}
     */
    public PageResult<{entity_name}VO> queryPage({entity_name}QueryForm queryForm) {
        log.info("Manager层分页查询{module_name}, 参数: {}", queryForm);

        // 构建查询条件
        LambdaQueryWrapper<{entity_name}> queryWrapper = new LambdaQueryWrapper<>();

        // AI自动根据表单字段生成查询条件
        {query_conditions}

        // 分页查询
        Page<{entity_name}> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        Page<{entity_name}> result = {dao_instance}.selectPage(page, queryWrapper);

        // 转换为VO
        List<{entity_name}VO> voList = SmartBeanUtil.copyList(result.getRecords(), {entity_name}VO.class);

        return PageResult.of(result.getTotal(), voList);
    }

    /**
     * 添加{module_name}
     */
    public void add({entity_name}Form addForm) {
        log.info("Manager层添加{module_name}, 参数: {}", addForm);

        // 转换为实体
        {entity_name} entity = SmartBeanUtil.copy(addForm, {entity_name}.class);

        // AI自动推断业务校验
        {business_validation}

        // 保存到数据库
        {dao_instance}.insert(entity);
    }

    /**
     * 更新{module_name}
     */
    public void update({entity_name}Form updateForm) {
        log.info("Manager层更新{module_name}, 参数: {}", updateForm);

        // 查询现有数据
        {entity_name} existing = {dao_instance}.selectById(updateForm.getId());
        if (existing == null) {
            throw new BusinessException("数据不存在");
        }

        // 转换并更新
        {entity_name} entity = SmartBeanUtil.copy(updateForm, {entity_name}.class);

        // AI自动推断更新逻辑
        {update_logic}

        {dao_instance}.updateById(entity);
    }

    /**
     * 删除{module_name}（软删除）
     */
    public void delete(Long id) {
        log.info("Manager层删除{module_name}, ID: {}", id);

        {entity_name} entity = {dao_instance}.selectById(id);
        if (entity == null) {
            throw new BusinessException("数据不存在");
        }

        // 执行软删除
        entity.setDeletedFlag(true);
        {dao_instance}.updateById(entity);
    }
}
```

## 🔧 模板6: SmartAdmin v4 DAO类标准结构

### 标准DAO接口模板
```java
package {package_path};

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.base.common.dao.BaseDao;
import {entity_package}.{entity_name};
import org.apache.ibatis.annotations.Mapper;

/**
 * {module_name}数据访问层
 * 遵循SmartAdmin v4 DAO设计规范
 */
@Mapper
public interface {dao_name} extends BaseDao<{entity_name}> {

    // AI自动根据业务需求生成自定义查询方法
    // {custom_queries}
}
```

## 🔧 模板7: SmartAdmin v4 Enum类标准结构

### 标准Enum类模板
```java
package {package_path};

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enums.SmartEnum;

/**
 * {enum_description}枚举
 * 遵循SmartAdmin v4 Enum设计规范
 */
@Getter
@AllArgsConstructor
public enum {enum_name} implements SmartEnum {

    // AI根据业务需求自动生成枚举值
    {enum_values}

    private final Integer value;
    private final String description;

    /**
     * 根据值获取枚举
     */
    public static {enum_name} getByValue(Integer value) {
        for ({enum_name} item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}
```

## 🎯 AI智能应用指南

### 1. 自动重构触发条件
- 检测到重复类定义
- 发现缺少标准注解
- Entity类未继承BaseEntity
- 包名不符合jakarta规范

### 2. 智能字段推断
基于以下信息自动生成字段定义：
- 数据库表结构
- 业务表单字段
- VO展示字段
- API接口参数

### 3. 业务逻辑生成
根据以下模式自动生成基础业务逻辑：
- 标准CRUD操作
- 数据校验规则
- 异常处理机制
- 日志记录规范

### 4. 权限控制自动生成
基于Controller方法自动生成权限注解：
- 查询: `{module}:query`
- 添加: `{module}:add`
- 更新: `{module}:update`
- 删除: `{module}:delete`

## 🚨 重要注意事项

1. **严格遵循SmartAdmin v4规范**
2. **确保所有重构都经过测试验证**
3. **保持业务逻辑的完整性**
4. **遵循渐进式重构原则**
5. **确保向后兼容性**