# AI代码生成专家

> **版本**: v1.0.0
> **更新时间**: 2025-11-21
> **分类**: AI辅助开发技能 > 代码生成
> **标签**: ["AI代码生成", "智能编程", "代码补全", "自动化开发", "IOE-DREAM标准"]
> **技能等级**: ★★★ 专家级
> **适用角色**: AI开发专家、高级开发工程师、技术架构师
> **前置技能**: spring-boot-jakarta-guardian, four-tier-architecture-guardian, code-quality-protector
> **预计学时**: 50-70小时

---

## 📋 技能概述

本技能专门为IOE-DREAM项目提供AI辅助代码生成解决方案，基于项目的技术栈（Java 17 + Spring Boot 3.x + Jakarta + Vue3）和编码规范，实现智能化、标准化的代码生成。涵盖从CRUD操作、业务逻辑到测试用例的全流程自动化代码生成。

**技术基础**: AI模型 + 模板引擎 + 代码分析 + repowiki规范
**核心目标**: 提高开发效率，确保代码质量，维护编码一致性

---

## 🏗️ AI代码生成架构

### 1. 代码生成框架设计

#### 生成器核心配置
```java
package net.lab1024.sa.base.codegen;

import net.lab1024.sa.base.codegen.generator.*;
import net.lab1024.sa.base.codegen.template.TemplateEngine;
import net.lab1024.sa.base.codeanalyzer.CodeAnalyzer;
import net.lab1024.sa.base.codegen.validator.CodeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI代码生成引擎
 * 严格遵循IOE-DREAM项目规范和repowiki标准
 */
@Slf4j
@Component
public class AiCodeGenerationEngine {

    private final TemplateEngine templateEngine;
    private final CodeAnalyzer codeAnalyzer;
    private final CodeValidator codeValidator;
    private final Map<String, GeneratorConfig> generatorConfigs;

    public AiCodeGenerationEngine(TemplateEngine templateEngine,
                               CodeAnalyzer codeAnalyzer,
                               CodeValidator codeValidator) {
        this.templateEngine = templateEngine;
        this.codeAnalyzer = codeAnalyzer;
        this.codeValidator = codeValidator;
        this.generatorConfigs = new ConcurrentHashMap<>();
        initializeGenerators();
    }

    /**
     * 初始化代码生成器配置
     */
    private void initializeGenerators() {
        // Entity生成器
        generatorConfigs.put("entity", GeneratorConfig.builder()
            .templateName("entity.ftl")
            .packageName("net.lab1024.sa.base.common.device.domain.entity")
            .suffix("Entity")
            .validator(new EntityValidator())
            .postProcessor(new EntityPostProcessor())
            .build());

        // Controller生成器
        generatorConfigs.put("controller", GeneratorConfig.builder()
            .templateName("controller.ftl")
            .packageName("net.lab1024.sa.admin.module.{module}.controller")
            .suffix("Controller")
            .validator(new ControllerValidator())
            .postProcessor(new ControllerPostProcessor())
            .build());

        // Service生成器
        generatorConfigs.put("service", GeneratorConfig.builder()
            .templateName("service.ftl")
            .packageName("net.lab1024.sa.admin.module.{module}.service")
            .suffix("Service")
            .validator(new ServiceValidator())
            .postProcessor(new ServicePostProcessor())
            .build());

        // Service实现生成器
        generatorConfigs.put("serviceImpl", GeneratorConfig.builder()
            .templateName("serviceImpl.ftl")
            .packageName("net.lab1024.sa.admin.module.{module}.service.impl")
            .suffix("ServiceImpl")
            .validator(new ServiceImplValidator())
            .postProcessor(new ServiceImplPostProcessor())
            .build());

        // Manager生成器
        generatorConfigs.put("manager", GeneratorConfig.builder()
            .templateName("manager.ftl")
            .packageName("net.lab1024.sa.admin.module.{module}.manager")
            .suffix("Manager")
            .validator(new ManagerValidator())
            .postProcessor(new ManagerPostProcessor())
            .build());

        // DAO生成器
        generatorConfigs.put("dao", GeneratorConfig.builder()
            .templateName("dao.ftl")
            .packageName("net.lab1024.sa.admin.module.{module}.dao")
            .suffix("Dao")
            .validator(new DaoValidator())
            .postProcessor(new DaoPostProcessor())
            .build());

        log.info("代码生成器初始化完成，共配置 {} 个生成器", generatorConfigs.size());
    }

    /**
     * 生成完整模块代码
     */
    public GenerationResult generateModule(ModuleGenerationRequest request) {
        log.info("开始生成模块代码: {}", request.getModuleName());

        try {
            // 1. 验证输入参数
            validateRequest(request);

            // 2. 分析现有代码结构
            CodeAnalysisResult analysisResult = codeAnalyzer.analyze(request);

            // 3. 生成各类文件
            GenerationResult result = new GenerationResult();

            // 生成Entity
            result.addGeneratedFile(generateFile("entity", request));

            // 生成Controller
            result.addGeneratedFile(generateFile("controller", request));

            // 生成Service接口
            result.addGeneratedFile(generateFile("service", request));

            // 生成Service实现
            result.addGeneratedFile(generateFile("serviceImpl", request));

            // 生成Manager
            result.addGeneratedFile(generateFile("manager", request));

            // 生成DAO
            result.addGeneratedFile(generateFile("dao", request));

            // 4. 生成Mapper XML
            result.addGeneratedFile(generateMapperXml(request));

            // 5. 验证生成代码
            validateGeneratedCode(result);

            log.info("模块代码生成完成: {}", request.getModuleName());
            return result;

        } catch (Exception e) {
            log.error("模块代码生成失败: {}", request.getModuleName(), e);
            throw new CodeGenerationException("代码生成失败", e);
        }
    }

    /**
     * 生成单个文件
     */
    private GeneratedFile generateFile(String generatorType, ModuleGenerationRequest request) {
        GeneratorConfig config = generatorConfigs.get(generatorType);
        if (config == null) {
            throw new IllegalArgumentException("未知的生成器类型: " + generatorType);
        }

        // 准备模板数据
        Map<String, Object> templateData = prepareTemplateData(request, generatorType);

        // 生成代码内容
        String codeContent = templateEngine.render(config.getTemplateName(), templateData);

        // 后处理
        codeContent = config.getPostProcessor().postProcess(codeContent, request);

        // 验证生成的代码
        config.getValidator().validate(codeContent, request);

        // 构建文件信息
        String className = request.getEntityName() + config.getSuffix();
        String packageName = config.getPackageName().replace("{module}", request.getModuleName());
        String fileName = className + ".java";
        String filePath = packageName.replace('.', '/') + "/" + fileName;

        return GeneratedFile.builder()
            .fileName(fileName)
            .filePath(filePath)
            .content(codeContent)
            .type(generatorType)
            .build();
    }

    /**
     * 准备模板数据
     */
    private Map<String, Object> prepareTemplateData(ModuleGenerationRequest request, String generatorType) {
        Map<String, Object> data = new ConcurrentHashMap<>();

        // 基础信息
        data.put("moduleName", request.getModuleName());
        data.put("entityName", request.getEntityName());
        data.put("entityNameLower", request.getEntityName().substring(0, 1).toLowerCase() + request.getEntityName().substring(1));
        data.put("tableName", request.getTableName());
        data.put("tableComment", request.getTableComment());
        data.put("packageName", generatorConfigs.get(generatorType).getPackageName().replace("{module}", request.getModuleName()));

        // 字段信息
        data.put("fields", request.getFields());
        data.put("primaryField", getPrimaryField(request.getFields()));

        // 生成器特定信息
        data.put("generatorType", generatorType);
        data.put("generateDate", LocalDateTime.now());
        data.put("author", "AI Code Generator");

        // repowiki规范要求
        data.put("useResource", true);  // 使用@Resource而非@Autowired
        data.put("useJakarta", true);   // 使用jakarta包名
        data.put("useSlf4j", true);     // 使用SLF4J日志
        data.put("useResponseDTO", true); // 使用统一的ResponseDTO

        return data;
    }

    private FieldInfo getPrimaryField(List<FieldInfo> fields) {
        return fields.stream()
            .filter(FieldInfo::isPrimaryKey)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("未找到主键字段"));
    }

    private void validateRequest(ModuleGenerationRequest request) {
        if (StringUtils.isBlank(request.getModuleName())) {
            throw new IllegalArgumentException("模块名称不能为空");
        }
        if (StringUtils.isBlank(request.getEntityName())) {
            throw new IllegalArgumentException("实体名称不能为空");
        }
        if (CollectionUtils.isEmpty(request.getFields())) {
            throw new IllegalArgumentException("字段信息不能为空");
        }
    }

    private void validateGeneratedCode(GenerationResult result) {
        for (GeneratedFile file : result.getGeneratedFiles()) {
            codeValidator.validate(file.getContent(), null);
        }
    }

    private GeneratedFile generateMapperXml(ModuleGenerationRequest request) {
        Map<String, Object> templateData = prepareTemplateData(request, "dao");

        String xmlContent = templateEngine.render("mapper.ftl", templateData);

        String fileName = request.getEntityName() + "Mapper.xml";
        String filePath = "mapper/" + request.getModuleName() + "/" + fileName;

        return GeneratedFile.builder()
            .fileName(fileName)
            .filePath(filePath)
            .content(xmlContent)
            .type("mapperXml")
            .build();
    }
}
```

### 2. 智能代码模板

#### Entity模板（遵循repowiki规范）
```ftl
<#-- entity.ftl - 实体类模板 -->
<#assign fields = request.fields>
<#assign primaryField = fields?filter(f -> f.primaryKey)?first>
<#-- 包导入规范：严格使用jakarta包名 -->
package ${packageName};

import net.lab1024.sa.base.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
<#list request.fields as field>
<#if field.javaType == "BigDecimal">
import java.math.BigDecimal;
</#if>
<#if field.javaType == "LocalDate">
import java.time.LocalDate;
</#if>
<#if field.javaType == "LocalDateTime">
import java.time.LocalDateTime;
</#if>
<#if field.javaType == "LocalTime">
import java.time.LocalTime;
</#if>
</#list>

/**
 * ${tableComment!''}实体
 *
 * @author AI Code Generator
 * @date ${generateDate?string('yyyy-MM-dd HH:mm:ss')}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "${tableName}", indexes = {
<#list request.fields as field>
<#if field.indexed>
    @Index(name = "idx_${field.columnName}", columnList = "${field.columnName}")<#if field_has_next>,</#if>
</#if>
</#list>
})
public class ${entityName} extends BaseEntity {

<#list request.fields as field>
    /**
     * ${field.comment!''}
     */
<#if field.primaryKey>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
</#if>
<#if field.notNull && !field.primaryKey>
    @NotNull(message = "${field.comment!''}不能为空")
</#if>
<#if field.maxLength?? && field.maxLength gt 0>
    @Size(max = ${field.maxLength}, message = "${field.comment!''}长度不能超过${field.maxLength}")
</#if>
<#if field.javaType == "String" && field.minLength?? && field.minLength gt 0>
    @Size(min = ${field.minLength}, max = ${field.maxLength}, message = "${field.comment!''}长度必须在${field.minLength}到${field.maxLength}之间")
</#if>
<#if field.javaType == "String" && field.pattern??>
    @Pattern(regexp = "${field.pattern}", message = "${field.comment!''}格式不正确")
</#if>
<#if field.javaType == "Number" && field.minValue?? && field.maxValue??>
    @Min(value = ${field.minValue}, message = "${field.comment!''}不能小于${field.minValue}")
    @Max(value = ${field.maxValue}, message = "${field.comment!''}不能大于${field.maxValue}")
</#if>
    @Column(name = "${field.columnName}"<#if field.nullable>, nullable = false</#if><#if field.unique>, unique = true</#if><#if field.length gt 0>, length = ${field.length}</#if><#if field.defaultValue??>, columnDefinition = "${field.columnDefinition}"</#if>)
    private ${field.javaType} ${field.fieldName};

</#list>
<#-- 添加业务方法 -->
<#if request.includeBusinessMethods>
<#list request.fields as field>
<#if field.javaType == "String" && field.enumType??>
    /**
     * 获取${field.comment!''}的枚举值
     */
    public ${field.enumType} get${field.fieldName?cap_first}Enum() {
        return StringUtils.isBlank(this.${field.fieldName}) ? null : ${field.enumType}.getByCode(this.${field.fieldName});
    }

    /**
     * 设置${field.comment!''}的枚举值
     */
    public void set${field.fieldName?cap_first}Enum(${field.enumType} enumValue) {
        this.${field.fieldName} = enumValue == null ? null : enumValue.getCode();
    }

</#if>
</#list>
</#if>
}
```

#### Controller模板（严格遵循repowiki规范）
```ftl
<#-- controller.ftl - 控制器模板 -->
package ${packageName};

import net.lab1024.sa.base.common.response.ResponseDTO;
import net.lab1024.sa.base.common.page.PageParam;
import net.lab1024.sa.base.common.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
<#if request.includeRequestDto>
import ${request.requestDtoPackage}.${request.entityName}QueryDTO;
import ${request.requestDtoPackage}.${request.entityName}CreateDTO;
import ${request.requestDtoPackage}.${request.entityName}UpdateDTO;
</#if>
<#if request.includeVo>
import ${request.voPackage}.${request.entityName}VO;
</#if>
import java.util.List;

/**
 * ${tableComment!''}管理控制器
 *
 * @author AI Code Generator
 * @date ${generateDate?string('yyyy-MM-dd HH:mm:ss')}
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/${request.moduleName}")
@Tag(name = "${tableComment!''}管理", description = "${tableComment!''}的增删改查接口")
public class ${entityName}Controller {

    @Resource
    private ${entityName}Service ${entityNameLower}Service;

    @Operation(summary = "分页查询${tableComment!''}")
    @GetMapping("/page")
    public ResponseDTO<PageResult<${entityName}VO>> queryByPage(
            @Parameter(description = "分页参数") PageParam pageParam,
    <#if request.includeRequestDto>
            @Parameter(description = "查询条件") ${entityName}QueryDTO queryDTO) {
    </#if>
        <#if request.includeRequestDto>
        return ResponseDTO.ok(${entityNameLower}Service.queryByPage(pageParam, queryDTO));
        <#else>
        return ResponseDTO.ok(${entityNameLower}Service.queryByPage(pageParam));
        </#if>
    }

    @Operation(summary = "根据ID查询${tableComment!''}")
    @GetMapping("/{id}")
    public ResponseDTO<${entityName}VO> getById(@Parameter(description = "${primaryField.comment!''}ID") @PathVariable ${primaryField.javaType} id) {
        return ResponseDTO.ok(${entityNameLower}Service.getById(id));
    }

    @Operation(summary = "新增${tableComment!''}")
    @PostMapping
    @SaCheckPermission("${request.moduleName}:${request.entityNameLower}:add")
    public ResponseDTO<${entityName}VO> add(@Valid @RequestBody ${entityName}CreateDTO createDTO) {
        return ResponseDTO.ok(${entityNameLower}Service.add(createDTO));
    }

    @Operation(summary = "更新${tableComment!''}")
    @PutMapping("/{id}")
    @SaCheckPermission("${request.moduleName}:${request.entityNameLower}:update")
    public ResponseDTO<${entityName}VO> update(
            @Parameter(description = "${primaryField.comment!''}ID") @PathVariable ${primaryField.javaType} id,
            @Valid @RequestBody ${entityName}UpdateDTO updateDTO) {
        return ResponseDTO.ok(${entityNameLower}Service.update(id, updateDTO));
    }

    @Operation(summary = "删除${tableComment!''}")
    @DeleteMapping("/{id}")
    @SaCheckPermission("${request.moduleName}:${request.entityNameLower}:delete")
    public ResponseDTO<Boolean> delete(@Parameter(description = "${primaryField.comment!''}ID") @PathVariable ${primaryField.javaType} id) {
        return ResponseDTO.ok(${entityNameLower}Service.delete(id));
    }

    @Operation(summary = "批量删除${tableComment!''}")
    @DeleteMapping("/batch")
    @SaCheckPermission("${request.moduleName}:${request.entityNameLower}:delete")
    public ResponseDTO<Boolean> batchDelete(@RequestBody List<${primaryField.javaType}> ids) {
        return ResponseDTO.ok(${entityNameLower}Service.batchDelete(ids));
    }

<#if request.includeBusinessMethods>
    <#list request.businessMethods as method>
    @Operation(summary = "${method.comment}")
    @${method.httpMethod}("${method.path}")
    @SaCheckPermission("${request.moduleName}:${request.entityNameLower}:${method.permission}")
    public ResponseDTO<${method.returnType}> ${method.name}(
        <#if method.hasPathVariable>
            @Parameter(description = "${method.paramComment}") @PathVariable ${method.paramType} ${method.paramName},
        </#if>
        <#if method.hasRequestBody>
            @Valid @RequestBody ${method.requestBodyType} ${method.requestBodyName}
        </#if>
    ) {
        return ResponseDTO.ok(${entityNameLower}Service.${method.name}(<#if method.hasPathVariable>${method.paramName}</#if>));
    }

    </#list>
</#if>
}
```

#### Service接口模板
```ftl
<#-- service.ftl - 服务接口模板 -->
package ${packageName};

import net.lab1024.sa.base.common.response.ResponseDTO;
import net.lab1024.sa.base.common.page.PageParam;
import net.lab1024.sa.base.common.page.PageResult;
<#if request.includeRequestDto>
import ${request.requestDtoPackage}.${request.entityName}QueryDTO;
import ${request.requestDtoPackage}.${request.entityName}CreateDTO;
import ${request.requestDtoPackage}.${request.entityName}UpdateDTO;
</#if>
<#if request.includeVo>
import ${request.voPackage}.${entityName}VO;
</#if>
import java.util.List;

/**
 * ${tableComment!''}服务接口
 *
 * @author AI Code Generator
 * @date ${generateDate?string('yyyy-MM-dd HH:mm:ss')}
 */
public interface ${entityName}Service {

    /**
     * 分页查询${tableComment!''}
     *
     * @param pageParam 分页参数
    <#if request.includeRequestDto>
     * @param queryDTO 查询条件
    </#if>
     * @return 分页结果
     */
    ResponseDTO<PageResult<${entityName}VO>> queryByPage(PageParam pageParam<#if request.includeRequestDto>, ${entityName}QueryDTO queryDTO</#if>);

    /**
     * 根据ID查询${tableComment!''}
     *
     * @param id ${primaryField.comment!''}ID
     * @return ${tableComment!''}详情
     */
    ResponseDTO<${entityName}VO> getById(${primaryField.javaType} id);

    /**
     * 新增${tableComment!''}
     *
     * @param createDTO 创建参数
     * @return 新增结果
     */
    ResponseDTO<${entityName}VO> add(${entityName}CreateDTO createDTO);

    /**
     * 更新${tableComment!''}
     *
     * @param id ${primaryField.comment!''}ID
     * @param updateDTO 更新参数
     * @return 更新结果
     */
    ResponseDTO<${entityName}VO> update(${primaryField.javaType} id, ${entityName}UpdateDTO updateDTO);

    /**
     * 删除${tableComment!''}
     *
     * @param id ${primaryField.comment!''}ID
     * @return 删除结果
     */
    ResponseDTO<Boolean> delete(${primaryField.javaType} id);

    /**
     * 批量删除${tableComment!''}
     *
     * @param ids ${primaryField.comment!''}ID列表
     * @return 删除结果
     */
    ResponseDTO<Boolean> batchDelete(List<${primaryField.javaType}> ids);

<#if request.includeBusinessMethods>
    <#list request.businessMethods as method>
    /**
     * ${method.comment}
     *
    <#list method.params as param>
     * @param ${param.name} ${param.comment}
    </#list>
     * @return ${method.returnComment}
     */
    ResponseDTO<${method.returnType}> ${method.name}(<#list method.params as param>${param.type} ${param.name}<#if param_has_next>, </#if></#list>);

    </#list>
</#if>
}
```

### 3. 智能代码分析和生成

#### 代码分析器
```java
package net.lab1024.sa.base.codeanalyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码分析器
 * 分析现有代码结构，生成代码模型
 */
@Slf4j
@Component
public class CodeAnalyzer {

    private static final Pattern ENTITY_PATTERN = Pattern.compile(
        "@Entity\\s*\\(.*?name\\s*=\\s*[\"'](.*?)[\"'].*?\\)\\s*" +
        ".*?public\\s+class\\s+(\\w+)\\s+extends\\s+BaseEntity",
        Pattern.DOTALL);

    private static final Pattern TABLE_PATTERN = Pattern.compile(
        "@Table\\s*\\(.*?name\\s*=\\s*[\"'](.*?)[\"'].*?\\)",
        Pattern.DOTALL);

    private static final Pattern FIELD_PATTERN = Pattern.compile(
        "@Column\\s*\\(.*?name\\s*=\\s*[\"'](.*?)[\"'].*?\\)\\s*" +
        "private\\s+(\\w+)\\s+(\\w+)\\s*;",
        Pattern.DOTALL);

    /**
     * 分析现有代码结构
     */
    public CodeAnalysisResult analyze(ModuleGenerationRequest request) {
        log.info("开始分析代码结构: {}", request.getModuleName());

        CodeAnalysisResult result = new CodeAnalysisResult();

        try {
            // 分析实体类
            analyzeEntities(result, request);

            // 分析包结构
            analyzePackageStructure(result, request);

            // 分析命名规范
            analyzeNamingConventions(result, request);

            // 分析数据库表结构
            analyzeDatabaseStructure(result, request);

        } catch (Exception e) {
            log.error("代码分析失败", e);
            throw new CodeAnalysisException("代码分析失败", e);
        }

        log.info("代码分析完成");
        return result;
    }

    /**
     * 分析实体类
     */
    private void analyzeEntities(CodeAnalysisResult result, ModuleGenerationRequest request) {
        String entityPath = "src/main/java/net/lab1024/sa/admin/module/" + request.getModuleName();

        Path entityDir = Paths.get(entityPath + "/domain/entity");
        if (!Files.exists(entityDir)) {
            log.warn("实体目录不存在: {}", entityDir);
            return;
        }

        try {
            Files.walk(entityDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(entityFile -> analyzeEntity(entityFile, result));

        } catch (IOException e) {
            log.error("读取实体文件失败", e);
        }
    }

    /**
     * 分析单个实体文件
     */
    private void analyzeEntity(Path entityFile, CodeAnalysisResult result) {
        try {
            String content = Files.readString(entityFile);

            // 提取实体信息
            Matcher entityMatcher = ENTITY_PATTERN.matcher(content);
            if (entityMatcher.find()) {
                String tableName = entityMatcher.group(1);
                String className = entityMatcher.group(2);

                EntityInfo entityInfo = EntityInfo.builder()
                    .tableName(tableName)
                    .entityName(className)
                    .fileName(entityFile.getFileName().toString())
                    .filePath(entityFile.toString())
                    .build();

                // 提取表名
                Matcher tableMatcher = TABLE_PATTERN.matcher(content);
                if (tableMatcher.find()) {
                    entityInfo.setTableName(tableMatcher.group(1));
                }

                // 提取字段信息
                extractFields(content, entityInfo);

                result.addEntity(entityInfo);
            }

        } catch (IOException e) {
            log.error("分析实体文件失败: {}", entityFile, e);
        }
    }

    /**
     * 提取字段信息
     */
    private void extractFields(String content, EntityInfo entityInfo) {
        Matcher fieldMatcher = FIELD_PATTERN.matcher(content);
        List<FieldInfo> fields = new ArrayList<>();

        while (fieldMatcher.find()) {
            String columnName = fieldMatcher.group(1);
            String javaType = fieldMatcher.group(2);
            String fieldName = fieldMatcher.group(3);

            FieldInfo fieldInfo = FieldInfo.builder()
                .columnName(columnName)
                .javaType(javaType)
                .fieldName(fieldName)
                .build();

            // 检查是否为主键
            if (fieldName.contains("Id") || columnName.contains("id")) {
                fieldInfo.setPrimaryKey(true);
            }

            fields.add(fieldInfo);
        }

        entityInfo.setFields(fields);
    }

    /**
     * 分析包结构
     */
    private void analyzePackageStructure(CodeAnalysisResult result, ModuleGenerationRequest request) {
        String basePath = "src/main/java/net/lab1024/sa/admin/module/" + request.getModuleName();
        Path moduleDir = Paths.get(basePath);

        if (!Files.exists(moduleDir)) {
            result.setPackageStructureValid(false);
            result.addIssue("模块目录不存在: " + basePath);
            return;
        }

        // 检查标准包结构
        String[] requiredPackages = {"controller", "service", "service/impl", "manager", "dao"};
        List<String> missingPackages = new ArrayList<>();

        for (String pkg : requiredPackages) {
            Path pkgPath = moduleDir.resolve(pkg);
            if (!Files.exists(pkgPath)) {
                missingPackages.add(pkg);
            }
        }

        result.setPackageStructureValid(missingPackages.isEmpty());
        if (!missingPackages.isEmpty()) {
            result.addIssue("缺少标准包结构: " + String.join(", ", missingPackages));
        }
    }

    /**
     * 分析命名规范
     */
    private void analyzeNamingConventions(CodeAnalysisResult result, ModuleGenerationRequest request) {
        List<String> issues = new ArrayList<>();

        // 检查模块名称
        if (!request.getModuleName().matches("^[a-z][a-z0-9]*$")) {
            issues.add("模块名称应符合小写字母和数字的命名规范");
        }

        // 检查实体名称
        if (!request.getEntityName().matches("^[A-Z][a-zA-Z0-9]*$")) {
            issues.add("实体名称应符合大驼峰命名规范");
        }

        // 检查表名
        if (!request.getTableName().matches("^[a-z][a-z0-9_]*$")) {
            issues.add("表名应符合小写字母、数字和下划线的命名规范");
        }

        result.setNamingConventionsValid(issues.isEmpty());
        result.setNamingIssues(issues);
    }

    /**
     * 分析数据库结构
     */
    private void analyzeDatabaseStructure(CodeAnalysisResult result, ModuleGenerationRequest request) {
        // 这里可以连接数据库，分析实际表结构
        // 简化实现，只做基本验证

        if (CollectionUtils.isEmpty(request.getFields())) {
            result.addIssue("字段信息不能为空");
            result.setDatabaseStructureValid(false);
            return;
        }

        // 检查是否有主键
        boolean hasPrimaryKey = request.getFields().stream()
            .anyMatch(FieldInfo::isPrimaryKey);

        if (!hasPrimaryKey) {
            result.addIssue("必须指定主键字段");
            result.setDatabaseStructureValid(false);
        } else {
            result.setDatabaseStructureValid(true);
        }
    }
}
```

### 4. 智能代码生成工具

#### 代码生成CLI工具
```java
package net.lab1024.sa.base.codegen.cli;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.codegen.*;
import net.lab1024.sa.base.codeanalyzer.CodeAnalysisResult;
import net.lab1024.sa.base.codeanalyzer.CodeAnalyzer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

import java.util.Scanner;

/**
 * AI代码生成命令行工具
 *
 * 使用示例:
 * java -jar codegen-cli.jar --module user --entity User --table t_user
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "net.lab1024.sa")
@EntityScan(basePackages = "net.lab1024.sa")
public class AiCodeGeneratorCli implements CommandLineRunner {

    private final AiCodeGenerationEngine codeGenerationEngine;
    private final CodeAnalyzer codeAnalyzer;

    public AiCodeGeneratorCli(AiCodeGenerationEngine codeGenerationEngine, CodeAnalyzer codeAnalyzer) {
        this.codeGenerationEngine = codeGenerationEngine;
        this.codeAnalyzer = codeAnalyzer;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== IOE-DREAM AI代码生成工具 ===");

        if (args.length > 0 && "--interactive".equals(args[0])) {
            runInteractiveMode();
        } else if (args.length >= 3) {
            runCommandMode(args);
        } else {
            showUsage();
        }
    }

    /**
     * 交互式模式
     */
    private void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("\n欢迎使用IOE-DREAM AI代码生成工具！");

            // 收集基本信息
            System.out.print("请输入模块名称（如: user）: ");
            String moduleName = scanner.nextLine().trim();

            System.out.print("请输入实体名称（如: User）: ");
            String entityName = scanner.nextLine().trim();

            System.out.print("请输入表名（如: t_user）: ");
            String tableName = scanner.nextLine().trim();

            System.out.print("请输入表注释（如: 用户表）: ");
            String tableComment = scanner.nextLine().trim();

            // 构建请求
            ModuleGenerationRequest request = buildInteractiveRequest(scanner, moduleName, entityName, tableName, tableComment);

            // 执行代码生成
            executeGeneration(request);

        } finally {
            scanner.close();
        }
    }

    /**
     * 命令行模式
     */
    private void runCommandMode(String[] args) {
        String moduleName = args[0];
        String entityName = args[1];
        String tableName = args[2];
        String tableComment = args.length > 3 ? args[3] : "";

        ModuleGenerationRequest request = ModuleGenerationRequest.builder()
            .moduleName(moduleName)
            .entityName(entityName)
            .tableName(tableName)
            .tableComment(tableComment)
            .build();

        executeGeneration(request);
    }

    /**
     * 执行代码生成
     */
    private void executeGeneration(ModuleGenerationRequest request) {
        try {
            log.info("开始生成代码: {}", request.getEntityName());

            // 分析现有代码
            CodeAnalysisResult analysisResult = codeAnalyzer.analyze(request);
            if (!analysisResult.isValid()) {
                log.error("代码分析失败: {}", analysisResult.getIssues());
                return;
            }

            // 生成代码
            GenerationResult result = codeGenerationEngine.generateModule(request);

            // 输出结果
            System.out.println("\n=== 代码生成完成 ===");
            System.out.println("模块: " + request.getModuleName());
            System.out.println("实体: " + request.getEntityName());
            System.out.println("生成文件数: " + result.getGeneratedFiles().size());

            System.out.println("\n生成的文件:");
            for (GeneratedFile file : result.getGeneratedFiles()) {
                System.out.println("  ✓ " + file.getFilePath());
            }

            log.info("代码生成成功完成");

        } catch (Exception e) {
            log.error("代码生成失败", e);
            System.err.println("错误: " + e.getMessage());
        }
    }

    /**
     * 构建交互式请求
     */
    private ModuleGenerationRequest buildInteractiveRequest(Scanner scanner, String moduleName, String entityName, String tableName, String tableComment) {
        ModuleGenerationRequest.ModuleGenerationRequestBuilder builder = ModuleGenerationRequest.builder()
            .moduleName(moduleName)
            .entityName(entityName)
            .tableName(tableName)
            .tableComment(tableComment);

        // 设置默认包结构
        builder.requestDtoPackage("net.lab1024.sa.admin.module." + moduleName + ".domain.dto");
        builder.voPackage("net.lab1024.sa.admin.module." + moduleName + ".domain.vo");

        // 收集字段信息
        System.out.println("\n现在请添加字段信息（输入空行结束）:");

        int fieldOrder = 1;
        while (true) {
            System.out.print("字段 " + fieldOrder + " - 字段名（如: userName）: ");
            String fieldName = scanner.nextLine().trim();

            if (fieldName.isEmpty()) {
                break;
            }

            System.out.print("字段 " + fieldOrder + " - Java类型（如: String）: ");
            String javaType = scanner.nextLine().trim();

            System.out.print("字段 " + fieldOrder + " - 数据库列名（如: user_name）: ");
            String columnName = scanner.nextLine().trim();

            System.out.print("字段 " + fieldOrder + " - 注释（如: 用户名）: ");
            String comment = scanner.nextLine().trim();

            System.out.print("字段 " + fieldOrder + " - 是否主键（y/n）: ");
            boolean isPrimaryKey = "y".equalsIgnoreCase(scanner.nextLine().trim());

            System.out.print("字段 " + fieldOrder + " - 是否允许为空（y/n）: ");
            boolean nullable = "y".equalsIgnoreCase(scanner.nextLine().trim());

            FieldInfo field = FieldInfo.builder()
                .fieldName(fieldName)
                .javaType(javaType)
                .columnName(columnName)
                .comment(comment)
                .primaryKey(isPrimaryKey)
                .nullable(nullable)
                .fieldOrder(fieldOrder)
                .build();

            builder.field(field);
            fieldOrder++;
            System.out.println();
        }

        // 可选功能
        System.out.print("是否包含业务方法？(y/n): ");
        boolean includeBusinessMethods = "y".equalsIgnoreCase(scanner.nextLine().trim());
        builder.includeBusinessMethods(includeBusinessMethods);

        System.out.print("是否生成DTO和VO？(y/n): ");
        boolean includeDtos = "y".equalsIgnoreCase(scanner.nextLine().trim());
        builder.includeRequestDto(includeDtos);
        builder.includeVo(includeDtos);

        return builder.build();
    }

    /**
     * 显示使用说明
     */
    private void showUsage() {
        System.out.println("=== IOE-DREAM AI代码生成工具 ===");
        System.out.println();
        System.out.println("使用方法:");
        System.out.println("  交互式模式:");
        System.out.println("    java -jar codegen-cli.jar --interactive");
        System.out.println();
        System.out.println("  命令行模式:");
        System.out.println("    java -jar codegen-cli.jar <module> <entity> <table> [comment]");
        System.out.println();
        System.out.println("示例:");
        System.out.println("    java -jar codegen-cli.jar user User t_user 用户表");
        System.out.println();
        System.out.println("参数说明:");
        System.out.println("  module     - 模块名称（必需）");
        System.out.println("  entity     - 实体类名（必需）");
        System.out.println("  table      - 数据库表名（必需）");
        System.out.println("  comment    - 表注释（可选）");
    }

    public static void main(String[] args) {
        SpringApplication.run(AiCodeGeneratorCli.class, args);
    }
}
```

---

## 🔧 代码生成配置

### 1. 生成器配置管理

#### 统一配置类
```java
package net.lab1024.sa.base.codegen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

/**
 * 代码生成配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "codegen")
public class CodeGeneratorConfig {

    /**
     * 是否启用AI代码生成
     */
    private boolean enabled = true;

    /**
     * 输出目录
     */
    private String outputDir = "generated-code";

    /**
     * 模板目录
     */
    private String templateDir = "templates/codegen";

    /**
     * 默认作者
     */
    private String author = "AI Code Generator";

    /**
     * 是否覆盖现有文件
     */
    private boolean overwriteExisting = false;

    /**
     * 生成器特定配置
     */
    private Map<String, GeneratorProperties> generators = new HashMap<>();

    @Data
    public static class GeneratorProperties {
        private boolean enabled = true;
        private String template;
        private String packageName;
        private String suffix;
        private Map<String, Object> customProperties = new HashMap<>();
    }

    /**
     * 获取生成器配置
     */
    public GeneratorProperties getGeneratorProperties(String generatorType) {
        return generators.getOrDefault(generatorType, new GeneratorProperties());
    }
}
```

### 2. 模板引擎配置

#### FreeMarker配置
```java
package net.lab1024.sa.base.codegen.template;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;

/**
 * FreeMarker模板引擎
 */
@Slf4j
@Component
public class TemplateEngine {

    private Configuration configuration;

    @PostConstruct
    public void init() {
        try {
            configuration = new Configuration(Configuration.VERSION_2_3_31);

            // 设置模板加载路径
            configuration.setDirectoryForTemplateLoading(new java.io.File("templates/codegen"));

            // 设置默认编码
            configuration.setDefaultEncoding("UTF-8");

            // 设置模板异常处理器
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

            // 设置数字格式
            configuration.setNumberFormat("0.######");

            log.info("FreeMarker模板引擎初始化完成");

        } catch (Exception e) {
            log.error("FreeMarker模板引擎初始化失败", e);
            throw new RuntimeException("模板引擎初始化失败", e);
        }
    }

    /**
     * 渲染模板
     */
    public String render(String templateName, Map<String, Object> data) {
        try {
            freemarker.template.Template template = configuration.getTemplate(templateName);

            // 使用StringWriter捕获输出
            java.io.StringWriter writer = new java.io.StringWriter();
            template.process(data, writer);

            return writer.toString();

        } catch (Exception e) {
            log.error("模板渲染失败: {}", templateName, e);
            throw new TemplateRenderException("模板渲染失败: " + templateName, e);
        }
    }

    /**
     * 渲染字符串模板
     */
    public String renderString(String templateContent, Map<String, Object> data) {
        try {
            freemarker.template.Template template = new freemarker.template.Template(
                "stringTemplate",
                new StringReader(templateContent),
                configuration
            );

            java.io.StringWriter writer = new java.io.StringWriter();
            template.process(data, writer);

            return writer.toString();

        } catch (Exception e) {
            log.error("字符串模板渲染失败", e);
            throw new TemplateRenderException("字符串模板渲染失败", e);
        }
    }
}
```

---

## ⚠️ 最佳实践与注意事项

### ✅ 推荐实践

1. **代码生成原则**
   - 严格遵循repowiki规范
   - 使用@Resource而非@Autowired
   - 使用jakarta包名
   - 统一的响应格式ResponseDTO

2. **模板设计原则**
   - 模块化和可重用性
   - 参数化配置
   - 清晰的注释说明
   - 错误处理机制

3. **生成质量保证**
   - 代码格式化
   - 语法验证
   - 规范检查
   - 自动化测试

4. **使用建议**
   - 定期更新模板
   - 收集使用反馈
   - 持续优化生成逻辑
   - 建立代码审查机制

### ❌ 避免的陷阱

1. **代码质量问题**
   - 不要生成不符合规范的代码
   - 避免硬编码和魔法数字
   - 不要忽略异常处理
   - 避免生成无用代码

2. **维护问题**
   - 不要让模板过于复杂
   - 避免重复的模板逻辑
   - 不要忽视文档更新
   - 避免版本兼容性问题

3. **使用问题**
   - 不要完全依赖自动生成
   - 避免过度生成无用文件
   - 不要忽略人工审查
   - 避免不合理的代码结构

---

## 📊 评估标准

### 🎯 技能掌握评估

#### 理论知识 (30%)
- [ ] AI代码生成原理和技术
- [ ] 模板引擎和代码分析
- [ ] repowiki规范体系
- [ ] 代码质量保证方法

#### 实践能力 (50%)
- [ ] 能够设计代码生成模板
- [ ] 熟练使用模板引擎
- [ ] 能够实现代码分析器
- [ ] 掌握生成器配置和管理

#### 问题解决 (20%)
- [ ] 生成代码质量问题排查
- [ ] 模板优化和调试
- [ ] 生成器性能优化
- [ ] 用户需求分析和实现

### 📈 质量标准

- **代码生成准确率**: > 95%
- **代码规范符合度**: 100%
- **生成效率**: > 80%（相比手工编写）
- **用户满意度**: > 90%

---

## 🔗 相关技能

- **前置技能**: spring-boot-jakarta-guardian, four-tier-architecture-guardian
- **相关技能**: automated-refactoring-specialist, intelligent-testing-specialist
- **进阶技能**: code-quality-protector, development-standards-specialist

---

## 💡 持续学习方向

1. **高级模板技术**: 自定义DSL、元编程
2. **智能代码分析**: 静态分析、代码理解
3. **AI辅助编程**: GPT集成、智能补全
4. **低代码平台**: 可视化开发、拖拉拽

---

**⚠️ 重要提醒**: AI代码生成工具应该作为辅助手段，不能完全替代人工编码。所有生成的代码都需要经过人工审查和测试，确保符合IOE-DREAM项目的质量标准和安全要求。