# 智能测试生成专家

> **版本**: v1.0.0
> **更新时间**: 2025-11-21
> **分类**: AI辅助开发技能 > 测试自动化
> **标签**: ["智能测试生成", "单元测试", "集成测试", "测试覆盖率", "IOE-DREAM标准"]
> **技能等级**: ★★★ 专家级
> **适用角色**: 测试工程师、高级开发工程师、QA专家
> **前置技能**: ai-code-generation-specialist, automated-refactoring-specialist, quality-assurance-expert
> **预计学时**: 50-70小时

---

## 📋 技能概述

本技能专门为IOE-DREAM项目提供智能化的测试代码生成解决方案，基于Java 17 + Spring Boot 3.x + Jakarta技术栈，实现从单元测试、集成测试到API测试的全流程自动化测试生成。确保代码质量和测试覆盖率符合企业级标准。

**技术基础**: JUnit 5 + Mockito + TestContainers + 测试数据生成器
**核心目标**: 提高测试覆盖率、减少测试编写工作量、保障代码质量

---

## 🏗️ 智能测试生成架构

### 1. 测试生成引擎

#### 测试生成引擎核心
```java
package net.lab1024.sa.base.testing.generator;

import net.lab1024.sa.base.testing.analyzer.CodeAnalyzer;
import net.lab1024.sa.base.testing.generator.template.TestTemplateEngine;
import net.lab1024.sa.base.testing.generator.validator.TestValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能测试生成引擎
 * 严格遵循IOE-DREAM项目的测试标准和质量要求
 */
@Slf4j
@Component
public class IntelligentTestGenerator {

    private final CodeAnalyzer codeAnalyzer;
    private final TestTemplateEngine templateEngine;
    private final TestValidator testValidator;
    private final Map<String, TestGenerationStrategy> generationStrategies;

    public IntelligentTestGenerator(CodeAnalyzer codeAnalyzer,
                                  TestTemplateEngine templateEngine,
                                  TestValidator testValidator) {
        this.codeAnalyzer = codeAnalyzer;
        this.templateEngine = templateEngine;
        this.testValidator = testValidator;
        this.generationStrategies = new ConcurrentHashMap<>();
        initializeGenerationStrategies();
    }

    /**
     * 初始化测试生成策略
     */
    private void initializeGenerationStrategies() {
        // Service单元测试生成策略
        generationStrategies.put("service-unit", new ServiceUnitTestStrategy());

        // Controller单元测试生成策略
        generationStrategies.put("controller-unit", new ControllerUnitTestStrategy());

        // Repository单元测试生成策略
        generationStrategies.put("repository-unit", new RepositoryUnitTestStrategy());

        // Manager单元测试生成策略
        generationStrategies.put("manager-unit", new ManagerUnitTestStrategy());

        // 集成测试生成策略
        generationStrategies.put("integration", new IntegrationTestStrategy());

        // API测试生成策略
        generationStrategies.put("api-test", new ApiTestStrategy());

        // 性能测试生成策略
        generationStrategies.put("performance", new PerformanceTestStrategy());

        log.info("测试生成策略初始化完成，共配置 {} 个策略", generationStrategies.size());
    }

    /**
     * 生成测试代码
     */
    public TestGenerationResult generateTests(TestGenerationRequest request) {
        log.info("开始生成测试代码: {}", request.getModuleName());

        try {
            // 1. 分析源代码
            CodeAnalysisResult analysisResult = codeAnalyzer.analyze(request);

            // 2. 制定测试生成计划
            TestGenerationPlan plan = createTestGenerationPlan(analysisResult, request);

            // 3. 执行测试生成
            TestGenerationResult result = executeTestGeneration(plan, analysisResult);

            // 4. 验证生成的测试
            validateGeneratedTests(result);

            log.info("测试代码生成完成，生成 {} 个测试文件", result.getGeneratedTests().size());
            return result;

        } catch (Exception e) {
            log.error("测试代码生成失败", e);
            throw new TestGenerationException("测试生成失败", e);
        }
    }

    /**
     * 创建测试生成计划
     */
    private TestGenerationPlan createTestGenerationPlan(CodeAnalysisResult analysisResult, TestGenerationRequest request) {
        TestGenerationPlan plan = new TestGenerationPlan();

        // 分析需要测试的类
        List<ClassInfo> classesToTest = analysisResult.getClassesToTest();

        for (ClassInfo classInfo : classesToTest) {
            // 根据类类型确定测试策略
            String strategyType = determineStrategyType(classInfo);
            TestGenerationStrategy strategy = generationStrategies.get(strategyType);

            if (strategy != null && strategy.canHandle(classInfo)) {
                TestGenerationTask task = strategy.createTask(classInfo, request);
                plan.addTask(task);
            }
        }

        log.info("创建测试生成计划，包含 {} 个测试任务", plan.getTasks().size());
        return plan;
    }

    /**
     * 确定测试策略类型
     */
    private String determineStrategyType(ClassInfo classInfo) {
        String className = classInfo.getClassName();
        String packageName = classInfo.getPackageName();

        if (packageName.contains(".service.")) {
            if (className.endsWith("ServiceImpl")) {
                return "service-unit";
            }
        } else if (packageName.contains(".controller.")) {
            return "controller-unit";
        } else if (packageName.contains(".dao.") || packageName.contains(".mapper.")) {
            return "repository-unit";
        } else if (packageName.contains(".manager.")) {
            return "manager-unit";
        }

        return "service-unit"; // 默认策略
    }

    /**
     * 执行测试生成
     */
    private TestGenerationResult executeTestGeneration(TestGenerationPlan plan, CodeAnalysisResult analysisResult) {
        TestGenerationResult result = new TestGenerationResult();

        for (TestGenerationTask task : plan.getTasks()) {
            try {
                log.debug("生成测试: {}", task.getDescription());

                // 生成测试代码
                String testContent = generateTestCode(task);

                // 创建测试文件信息
                GeneratedTest test = GeneratedTest.builder()
                    .fileName(task.getFileName())
                    .filePath(task.getFilePath())
                    .content(testContent)
                    .type(task.getTestType())
                    .className(task.getTestClassName())
                    .build();

                result.addGeneratedTest(test);
                log.debug("测试生成成功: {}", task.getFileName());

            } catch (Exception e) {
                log.error("测试生成失败: {}", task.getDescription(), e);
                result.addFailedTask(task);
            }
        }

        return result;
    }

    /**
     * 生成测试代码
     */
    private String generateTestCode(TestGenerationTask task) {
        TestGenerationStrategy strategy = generationStrategies.get(task.getTestType());
        if (strategy == null) {
            throw new TestGenerationException("未找到测试生成策略: " + task.getTestType());
        }

        // 准备模板数据
        Map<String, Object> templateData = prepareTemplateData(task);

        // 生成测试代码
        String testContent = templateEngine.render(strategy.getTemplateName(), templateData);

        // 后处理测试代码
        testContent = strategy.postProcess(testContent, task);

        return testContent;
    }

    /**
     * 准备模板数据
     */
    private Map<String, Object> prepareTemplateData(TestGenerationTask task) {
        Map<String, Object> data = new ConcurrentHashMap<>();

        // 基础信息
        data.put("packageName", task.getPackageName());
        data.put("className", task.getClassName());
        data.put("testClassName", task.getTestClassName());
        data.put("testName", task.getTestName());
        data.put("generateDate", LocalDateTime.now());

        // 类信息
        data.put("classInfo", task.getClassInfo());
        data.put("methods", task.getMethods());
        data.put("dependencies", task.getDependencies());
        data.put("testData", task.getTestData());

        // 配置信息
        data.put("useMockito", task.isUseMockito());
        data.put("useTestContainers", task.isUseTestContainers());
        data.put("useWebMvcTest", task.isUseWebMvcTest());
        data.put("useSpringBootTest", task.isUseSpringBootTest());

        return data;
    }

    /**
     * 验证生成的测试
     */
    private void validateGeneratedTests(TestGenerationResult result) {
        for (GeneratedTest test : result.getGeneratedTests()) {
            try {
                testValidator.validate(test);
            } catch (Exception e) {
                log.error("测试验证失败: {}", test.getFileName(), e);
                result.addFailedTest(test);
            }
        }
    }

    /**
     * 获取生成策略
     */
    public TestGenerationStrategy getGenerationStrategy(String type) {
        return generationStrategies.get(type);
    }
}
```

### 2. Service单元测试生成策略

#### Service单元测试策略
```java
package net.lab1024.sa.base.testing.generator.strategy;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.testing.generator.model.*;
import org.springframework.stereotype.Component;

/**
 * Service单元测试生成策略
 */
@Slf4j
@Component
public class ServiceUnitTestStrategy implements TestGenerationStrategy {

    @Override
    public boolean canHandle(ClassInfo classInfo) {
        return classInfo.getPackageName().contains(".service.") &&
               classInfo.getClassName().endsWith("ServiceImpl");
    }

    @Override
    public TestGenerationTask createTask(ClassInfo classInfo, TestGenerationRequest request) {
        String testPackageName = classInfo.getPackageName().replace(".impl", "") + ".impl";
        String testClassName = classInfo.getClassName() + "Test";

        TestGenerationTask task = TestGenerationTask.builder()
            .testType("service-unit")
            .packageName(testPackageName)
            .className(classInfo.getClassName())
            .testClassName(testClassName)
            .testName(classInfo.getClassName() + "ServiceTest")
            .fileName(testClassName + ".java")
            .filePath(testPackageName.replace('.', '/') + "/" + testClassName + ".java")
            .classInfo(classInfo)
            .useMockito(true)
            .useTestContainers(false)
            .useWebMvcTest(false)
            .useSpringBootTest(false)
            .build();

        // 设置方法依赖
        setupServiceDependencies(task);

        return task;
    }

    /**
     * 设置Service依赖
     */
    private void setupServiceDependencies(TestGenerationTask task) {
        ClassInfo classInfo = task.getClassInfo();

        // 添加Manager依赖
        for (MethodInfo method : classInfo.getMethods()) {
            if (method.getName().startsWith("get") || method.getName().startsWith("find") ||
                method.getName().startsWith("query") || method.getName().startsWith("list")) {
                method.setRequiresTransaction(true);
            }
        }

        // 设置依赖注入
        task.addDependency("com.fasterxml.jackson.databind.ObjectMapper");
        task.addDependency("net.lab1024.sa.base.common.domain.PageResult");
        task.addDependency("net.lab1024.sa.base.common.response.ResponseDTO");
    }

    @Override
    public String getTemplateName() {
        return "service-unit-test.ftl";
    }

    @Override
    public String postProcess(String testContent, TestGenerationTask task) {
        // 添加包导入
        testContent = addImports(testContent, task);

        // 添加测试数据
        testContent = addTestData(testContent, task);

        return testContent;
    }

    /**
     * 添加包导入
     */
    private String addImports(String testContent, TestGenerationTask task) {
        StringBuilder imports = new StringBuilder();

        imports.append("package ").append(task.getPackageName()).append(";\n\n");
        imports.append("import org.junit.jupiter.api.Test;\n");
        imports.append("import org.junit.jupiter.api.extension.ExtendWith;\n");
        imports.append("import org.mockito.InjectMocks;\n");
        imports.append("import org.mockito.Mock;\n");
        imports.append("import org.mockito.junit.jupiter.MockitoExtension;\n");
        imports.append("import static org.junit.jupiter.api.Assertions.*;\n");
        imports.append("import static org.mockito.Mockito.*;\n");
        imports.append("import ").append(task.getClassInfo().getPackageName()).append(".").append(task.getClassInfo().getClassName()).append(";\n");

        return imports.toString() + testContent;
    }

    /**
     * 添加测试数据
     */
    private String addTestData(String testContent, TestGenerationTask task) {
        // 在测试类中添加测试数据方法
        String testData = generateTestData(task.getClassInfo());

        // 在类的大括号前插入测试数据方法
        int classEndIndex = testContent.indexOf("class " + task.getTestClassName());
        if (classEndIndex != -1) {
            int braceIndex = testContent.indexOf("{", classEndIndex);
            if (braceIndex != -1) {
                testContent = testContent.substring(0, braceIndex + 1) + "\n" + testData + "\n" + testContent.substring(braceIndex + 1);
            }
        }

        return testContent;
    }

    /**
     * 生成测试数据
     */
    private String generateTestData(ClassInfo classInfo) {
        StringBuilder testData = new StringBuilder();

        testData.append("    // ==================== 测试数据生成器 ====================\n\n");

        for (MethodInfo method : classInfo.getMethods()) {
            if (method.isPublic() && !method.getName().startsWith("set") &&
                !method.getName().startsWith("get")) {
                testData.append(generateMethodTestData(method));
            }
        }

        return testData.toString();
    }

    /**
     * 生成方法测试数据
     */
    private String generateMethodTestData(MethodInfo method) {
        StringBuilder testData = new StringBuilder();

        String methodName = method.getName();
        testData.append("    /**\n");
        testData.append("     * 生成").append(methodName).append("方法的测试数据\n");
        testData.append("     */\n");
        testData.append("    private ").append(method.getReturnType()).append(" generate").append(capitalize(methodName)).append("Data() {\n");

        // 根据返回类型生成测试数据
        switch (method.getReturnType()) {
            case "ResponseDTO":
                testData.append("        return ResponseDTO.ok(generateMock").append(capitalize(methodName)).append("DTO());\n");
                break;
            case "PageResult":
                testData.append("        return PageResult.of(generateMock").append(capitalize(methodName)).append("VOList(), 10L, 1L);\n");
                break;
            case "List":
                testData.append("        return List.of(generateMock").append(capitalize(methodName)).append("Entity());\n");
                break;
            case "Boolean":
                testData.append("        return true;\n");
                break;
            default:
                if (method.getReturnType().endsWith("VO")) {
                    testData.append("        return generateMock").append(capitalize(methodName)).append("VO();\n");
                } else if (method.getReturnType().endsWith("Entity")) {
                    testData.append("        return generateMock").append(capitalize(methodName)).append("Entity();\n");
                }
                break;
        }

        testData.append("    }\n\n");

        return testData.toString();
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
```

### 3. 测试模板

#### Service单元测试模板
```ftl
<#-- service-unit-test.ftl - Service单元测试模板 -->
<#assign classInfo = classInfo>
<#assign methods = methods>
<#assign testClass = testClassName>
<#assign sourceClass = className>
<#assign entityName = entityName!sourceClass>
<#assign entityNameLower = entityName?lower_case>

package ${packageName};

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import ${classInfo.packageName}.${classInfo.className};
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.response.ResponseDTO;
<#list dependencies as dependency>
import ${dependency};
</#list>

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * ${classInfo.classComment!''}单元测试
 *
 * @author AI Test Generator
 * @date ${generateDate?string('yyyy-MM-dd HH:mm:ss')}
 */
@ExtendWith(MockitoExtension.class)
public class ${testClass} {

    @InjectMocks
    private ${sourceClass} ${entityNameLower}Service;

    @Mock
    private ${entityName}Mapper ${entityNameLower}Mapper;

    @Mock
    private ${entityName}Manager ${entityNameLower}Manager;

    @Mock
    private ObjectMapper objectMapper;

    private ${entityName}Entity testEntity;
    private ${entityName}VO testVO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testEntity = new ${entityName}Entity();
        testEntity.set${primaryField.fieldName?cap_first}(1L);
        testEntity.setCreateTime(LocalDateTime.now());
        testEntity.setUpdateTime(LocalDateTime.now());
        testEntity.setDeletedFlag(0);

        testVO = new ${entityName}VO();
        testVO.set${primaryField.fieldName?cap_first}(1L);
    }

    @AfterEach
    void tearDown() {
        // 清理测试状态
    }

<#list methods as method>
<#if method.public && !method.name?starts_with("set") && !method.name?starts_with("get")>
    /**
     * 测试${method.comment!''}
     */
    @Test
    void test${method.name?cap_first}() {
        // Given
        <#if method.parameters?size gt 0>
        ${method.parameters[0].type} ${method.parameters[0].name} = generate${method.name?cap_first}Data();
        </#if>

        ${method.returnType} expectedResult = generate${method.name?cap_first}Data();

        // Mock依赖
        when(${entityNameLower}Mapper.selectById(any())).thenReturn(testEntity);
        <#if method.returnType == "PageResult">
        when(${entityNameLower}Mapper.selectByPage(any())).thenReturn(createMockPageResult());
        </#if>

        // When
        ${method.returnType} result = ${entityNameLower}Service.${method.name}(
            <#list method.parameters as param>
            ${param.name}<#if param_has_next>, </#if>
            </#list>
        );

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        <#if method.returnType == "ResponseDTO">
        assertNotNull(result.getData());
        </#if>

        // 验证方法调用
        <#if method.returnType == "PageResult">
        verify(${entityNameLower}Mapper).selectByPage(any());
        </#if>
        verify(${entityNameLower}Mapper, times(1)).selectById(any());
    }

    /**
     * 测试${method.comment!''} - 异常情况
     */
    @Test
    void test${method.name?cap_first}_Exception() {
        // Given
        <#if method.parameters?size gt 0>
        ${method.parameters[0].type} ${method.parameters[0].name} = generate${method.name?cap_first}Data();
        </#if>

        // Mock异常
        when(${entityNameLower}Mapper.selectById(any())).thenThrow(new RuntimeException("模拟异常"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            ${entityNameLower}Service.${method.name}(
                <#list method.parameters as param>
                ${param.name}<#if param_has_next>, </#if>
                </#list>
            );
        });
    }

</#if>
</#list>

    // ==================== 集成测试 ====================

    /**
     * 测试事务管理
     */
    @Test
    @Transactional
    void testTransactionManagement() {
        // Given
        ${entityNameLower}Entity entity = new ${entityName}Entity();
        entity.set${primaryField.fieldName?cap_first}(2L);

        // Mock事务行为
        try (MockedStatic<SpringTransactionManager> mocked = mockStatic(SpringTransactionManager.class)) {
            mocked.when(SpringTransactionManager.getCurrentTransactionName()).thenReturn("test-transaction");

            // When
            ${entityNameLower}Service.save(entity);

            // Then
            verifySpringTransactionManager(times(1)).getCurrentTransactionName();
        }
    }

    // ==================== 性能测试 ====================

    /**
     * 性能测试 - 批量查询
     */
    @Test
    void testQueryPerformance() {
        // Given
        int queryCount = 1000;
        when(${entityNameLower}Mapper.selectByPage(any())).thenReturn(createLargePageResult(queryCount));

        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < queryCount; i++) {
            PageResult<${entityName}VO> result = ${entityNameLower}Service.queryByPage(createMockPageParam());
            assertNotNull(result);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        assertTrue(duration < 1000, "批量查询性能测试失败，耗时: " + duration + "ms");
        log.info("批量查询性能测试通过，耗时: {}ms", duration);
    }

    // ==================== 辅助方法 ====================

    private ${entityName}QueryDTO createMockQueryDTO() {
        ${entityName}QueryDTO queryDTO = new ${entityName}QueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        return queryDTO;
    }

    private PageParam createMockPageParam() {
        PageParam pageParam = new PageParam();
        pageParam.setPageSize(10);
        pageParam.setPageNum(1);
        return pageParam;
    }

    private PageResult<${entityName}VO> createMockPageResult() {
        return PageResult.of(Arrays.asList(testVO), 1L, 1L);
    }

    private PageResult<${entityName}VO> createLargePageResult(int count) {
        List<${entityName}VO> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ${entityName}VO vo = new ${entityName}VO();
            vo.set${primaryField.fieldName?cap_first}((long) i);
            list.add(vo);
        }
        return PageResult.of(list, (long) count, 1);
    }

    private ${entityName}CreateDTO createMock${sourceClass}Data() {
        ${entityName}CreateDTO createDTO = new ${entityName}CreateDTO();
        createDTO.set${primaryField.fieldName?cap_first}(1L);
        <#list classInfo.fields as field>
        <#if field.name != primaryField.fieldName>
        createDTO.set${field.fieldName?cap_first}(generateMock${field.fieldName?cap_first}Value());
        </#if>
        </#list>
        return createDTO;
    }

    private ${entityName}UpdateDTO createMock${sourceClass}Data() {
        ${entityName}UpdateDTO updateDTO = new ${entityName}UpdateDTO();
        updateDTO.set${primaryField.fieldName?cap_first}(1L);
        <#list classInfo.fields as field>
        <#if field.name != primaryField.fieldName>
        updateDTO.set${field.fieldName?cap_first}(generateMock${field.fieldName?cap_first}Value());
        </#if>
        </#list>
        return updateDTO;
    }

    private ${entityName}VO createMock${entityName}Data() {
        ${entityName}VO vo = new ${entityName}VO();
        vo.set${primaryField.fieldName?cap_first}(1L);
        <#list classInfo.fields as field>
        <#if field.name != primaryField.fieldName>
        vo.set${field.fieldName?cap_first}(generateMock${field.fieldName?cap_first}Value());
        </#if>
        </#list>
        return vo;
    }

    private ${entityName}Entity createMock${entityName}Data() {
        ${entityName}Entity entity = new ${entityName}Entity();
        entity.set${primaryField.fieldName?cap_first}(1L);
        <#list classInfo.fields as field>
        entity.set${field.fieldName?cap_first}(generateMock${field.fieldName?cap_first}Value());
        </#list>
        return entity;
    }

<#list classInfo.fields as field>
    private ${field.javaType} generateMock${field.fieldName?cap_first}Value() {
        <#switch field.javaType>
        <#case "String">
        return "测试数据";
        </#case>
        <#case "Integer">
        return 1;
        </#case>
        <#case "Long">
        return 1L;
        </#case>
        <#case "BigDecimal">
        return new BigDecimal("100.00");
        </#case>
        <#case "Boolean">
        return true;
        </#case>
        <#case "LocalDateTime">
        return LocalDateTime.now();
        </#case>
        <#case "LocalDate">
        return LocalDate.now();
        </#case>
        <#default>
        return null;
        </#switch>
    }

</#list>
}
```

### 4. 集成测试策略

#### 集成测试生成策略
```java
package net.lab1024.sa.base.testing.generator.strategy;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.testing.generator.model.*;
import org.springframework.stereotype.Component;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * 集成测试生成策略
 */
@Slf4j
@Component
public class IntegrationTestStrategy implements TestGenerationStrategy {

    @Override
    public boolean canHandle(ClassInfo classInfo) {
        // 支持所有需要集成测试的类
        return true;
    }

    @Override
    public TestGenerationTask createTask(ClassInfo classInfo, TestGenerationRequest request) {
        String testClassName = classInfo.getClassName() + "IntegrationTest";

        TestGenerationTask task = TestGenerationTask.builder()
            .testType("integration")
            .packageName(classInfo.getPackageName() + ".integration")
            .className(classInfo.getClassName())
            .testClassName(testClassName)
            .testName(classInfo.getClassName() + "IntegrationTest")
            .fileName(testClassName + ".java")
            .filePath(classInfo.getPackageName().replace('.', '/') + "/integration/" + testClassName + ".java")
            .classInfo(classInfo)
            .useMockito(false)
            .useTestContainers(true)
            .useWebMvcTest(true)
            .useSpringBootTest(true)
            .build();

        // 设置集成测试配置
        setupIntegrationTestConfig(task);

        return task;
    }

    /**
     * 设置集成测试配置
     */
    private void setupIntegrationTestConfig(TestGenerationTask task) {
        // 添加Spring Boot测试注解
        task.addAnnotation("@SpringBootTest");
        task.addAnnotation("@ActiveProfiles(\"test\")");

        // 添加测试容器配置
        task.addDependency("org.springframework.test.context.DynamicPropertySource");
        task.addDependency("org.springframework.boot.testcontainers.SpringBootTest");
        task.addDependency("org.testcontainers.junit.jupiter.Testcontainers");

        // 添加数据库测试配置
        if (task.isUseTestContainers()) {
            task.addDependency("org.testcontainers.containers.MySQLContainer");
            task.addDependency("org.testcontainers.containers.RedisContainer");
        }
    }

    @Override
    public String getTemplateName() {
        return "integration-test.ftl";
    }

    @Override
    public String postProcess(String testContent, TestGenerationTask task) {
        // 添加集成测试特定的注解
        return addIntegrationTestAnnotations(testContent, task);
    }

    /**
     * 添加集成测试注解
     */
    private String addIntegrationTestAnnotations(String testContent, TestGenerationTask task) {
        StringBuilder annotations = new StringBuilder();

        annotations.append("@SpringBootTest\n");
        annotations.append("@ActiveProfiles(\"test\")\n");
        annotations.append("@TestPropertySource(properties = {\n");
        annotations.append("    \"spring.datasource.url=jdbc:h2:mem:testdb\",\n");
        annotations.append("    \"spring.jpa.hibernate.ddl-auto=create-drop\",\n");
        annotations.append("    \"spring.redis.host=localhost\",\n");
        annotations.append("    \"spring.redis.port=6379\"\n");
        annotations.append("})\n");

        // 替换类声明
        return testContent.replace("public class " + task.getTestClassName(), annotations + "public class " + task.getTestClassName());
    }
}
```

---

## 🔧 测试数据生成

### 1. 测试数据生成器

#### 智能测试数据生成器
```java
package net.lab1024.sa.base.testing.generator.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 智能测试数据生成器
 * 根据字段类型生成合理的测试数据
 */
@Slf4j
@Component
public class TestDataGenerator {

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    /**
     * 生成测试数据
     */
    @SuppressWarnings("unchecked")
    public <T> generateTestData(Class<T> type, String fieldName) {
        if (type == String.class) {
            return (T) generateString(fieldName);
        } else if (type == Integer.class || type == int.class) {
            return (T) generateInteger(fieldName);
        } else if (type == Long.class || type == long.class) {
            return (T) generateLong(fieldName);
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) generateBoolean();
        } else if (type == Double.class || type == double.class) {
            return (T) generateDouble(fieldName);
        } else if (type == Float.class || type == float.class) {
            return (T) generateFloat(fieldName);
        } else if (type == BigDecimal.class) {
            return (T) generateBigDecimal(fieldName);
        } else if (type == LocalDate.class) {
            return (T) generateLocalDate();
        } else if (type == LocalDateTime.class) {
            return (T) generateLocalDateTime();
        } else if (type.isEnum()) {
            return (T) generateEnumData(type);
        } else if (Collection.class.isAssignableFrom(type)) {
            return (T) generateCollectionData(type, fieldName);
        } else if (Map.class.isAssignableFrom(type)) {
            return (T) generateMapData(type, fieldName);
        } else {
            log.warn("不支持的测试数据类型: {}", type.getName());
            return null;
        }
    }

    /**
     * 生成字符串数据
     */
    private String generateString(String fieldName) {
        if (fieldName.toLowerCase().contains("name")) {
            return "测试用户";
        } else if (fieldName.toLowerCase().contains("phone")) {
            return "13800138000";
        } else if (fieldName.toLowerCase().contains("email")) {
            return "test@example.com";
        } else if (fieldName.toLowerCase().contains("address")) {
            return "北京市朝阳区";
        } else if (fieldName.toLowerCase().contains("remark")) {
            return "测试备注";
        } else if (fieldName.toLowerCase().contains("description")) {
            return "测试描述";
        } else if (fieldName.toLowerCase().contains("title")) {
            return "测试标题";
        } else {
            return generateRandomString(10);
        }
    }

    /**
     * 生成随机字符串
     */
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成整数数据
     */
    private Integer generateInteger(String fieldName) {
        if (fieldName.toLowerCase().contains("age")) {
            return random.nextInt(100) + 18; // 18-117岁
        } else if (fieldName.toLowerCase().contains("count") || fieldName.toLowerCase().contains("num")) {
            return random.nextInt(1000) + 1;
        } else if (fieldName.toLowerCase().contains("status") || fieldName.toLowerCase().contains("flag")) {
            return random.nextInt(2); // 0-1
        } else {
            return random.nextInt(1000);
        }
    }

    /**
     * 生成长整型数据
     */
    private Long generateLong(String fieldName) {
        if (fieldName.toLowerCase().contains("id")) {
            return (long) random.nextInt(Integer.MAX_VALUE);
        } else {
            return (long) random.nextInt(Integer.MAX_VALUE);
        }
    }

    /**
     * 生成布尔型数据
     */
    private Boolean generateBoolean() {
        return random.nextBoolean();
    }

    /**
     * 生成浮点数数据
     */
    private Double generateDouble(String fieldName) {
        if (fieldName.toLowerCase().contains("price") || fieldName.toLowerCase().contains("amount")) {
            return Math.round(random.nextDouble() * 10000.0) / 100.0; // 两位小数
        } else {
            return random.nextDouble() * 1000.0;
        }
    }

    /**
     * 生成单精度浮点数数据
     */
    private Float generateFloat(String fieldName) {
        return generateDouble(fieldName).floatValue();
    }

    /**
     * 生成BigDecimal数据
     */
    private java.math.BigDecimal generateBigDecimal(String fieldName) {
        if (fieldName.toLowerCase().contains("price") || fieldName.toLowerCase().contains("amount")) {
            return java.math.BigDecimal.valueOf(generateDouble(fieldName));
        } else {
            return java.math.BigDecimal.valueOf(random.nextDouble() * 1000.0);
        }
    }

    /**
     * 生成本地日期
     */
    private LocalDate generateLocalDate() {
        return LocalDate.now().minusDays(random.nextInt(365));
    }

    /**
     * 生成本地日期时间
     */
    private LocalDateTime generateLocalDateTime() {
        return LocalDateTime.now().minusDays(random.nextInt(30))
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60));
    }

    /**
     * 生成枚举数据
     */
    @SuppressWarnings("unchecked")
    private <T> T generateEnumData(Class<T> enumClass) {
        Object[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants.length > 0) {
            return (T) enumConstants[random.nextInt(enumConstants.length)];
        }
        return null;
    }

    /**
     * 生成集合数据
     */
    @SuppressWarnings("unchecked")
    private <T> T generateCollectionData(Class<T> type, String fieldName) {
        try {
            // 获取集合的泛型参数
            ParameterizedType parameterizedType = (ParameterizedType) type.getGenericSuperclass();
            Class<?> elementType = (Class<?>) parameterizedType.getActualTypeArguments()[0];

            // 创建集合实例
            Collection<Object> collection = createCollectionInstance(type);

            // 生成集合元素
            int size = random.nextInt(10) + 1;
            for (int i = 0; i < size; i++) {
                Object element = generateTestData(elementType, fieldName + i);
                if (element != null) {
                    collection.add(element);
                }
            }

            return (T) collection;

        } catch (Exception e) {
            log.error("生成集合数据失败", e);
            return null;
        }
    }

    /**
     * 创建集合实例
     */
    private Collection<Object> createCollectionInstance(Class<?> type) {
        if (type.isInterface()) {
            // 默认返回ArrayList
            return new ArrayList<>();
        } else {
            try {
                return (Collection<Object>) type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
    }

    /**
     * 生成Map数据
     */
    @SuppressWarnings("unchecked")
    private <T> T generateMapData(Class<T> type, String fieldName) {
        try {
            Map<Object, Object> map;

            if (type.isInterface()) {
                // 默认返回HashMap
                map = new HashMap<>();
            } else {
                map = (Map<Object, Object>) type.getDeclaredConstructor().newInstance();
            }

            // 生成键值对
            int size = random.nextInt(5) + 1;
            for (int i = 0; i < size; i++) {
                Object key = generateTestData(Object.class, "key" + i);
                Object value = generateTestData(Object.class, "value" + i);
                if (key != null && value != null) {
                    map.put(key, value);
                }
            }

            return (T) map;

        } catch (Exception e) {
            log.error("生成Map数据失败", e);
            return null;
        }
    }
}
```

### 2. 测试报告生成器

#### 测试覆盖率报告生成器
```java
package net.lab1024.sa.base.testing.report;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.testing.model.TestExecutionResult;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试覆盖率报告生成器
 */
@Slf4j
@Component
public class TestCoverageReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成HTML格式的测试报告
     */
    public void generateHtmlReport(TestExecutionResult result, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath + "/test-coverage-report.html")) {
            writer.write(generateHtmlContent(result));
            log.info("HTML测试报告生成完成: {}", outputPath + "/test-coverage-report.html");
        } catch (IOException e) {
            log.error("生成HTML测试报告失败", e);
        }
    }

    /**
     * 生成HTML内容
     */
    private String generateHtmlContent(TestExecutionResult result) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n")
           .append("<html lang=\"zh-CN\">\n")
           .append("<head>\n")
           .append("    <meta charset=\"UTF-8\">\n")
           .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
           .append("    <title>IOE-DREAM 测试覆盖率报告</title>\n")
           .append("    <style>\n")
           .append(getHtmlStyles())
           .append("    </style>\n")
           .append("</head>\n")
           .append("<body>\n")
           .append(generateHtmlBody(result))
           .append("</body>\n")
           .append("</html>\n");

        return html.toString();
    }

    /**
     * 生成HTML样式
     */
    private String getHtmlStyles() {
        return """
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 8px;
            margin-bottom: 30px;
            text-align: center;
        }
        .summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .summary-card {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
            border-left: 4px solid #28a745;
        }
        .summary-number {
            font-size: 2em;
            font-weight: bold;
            color: #28a745;
        }
        .summary-label {
            color: #666;
            margin-top: 5px;
        }
        .section {
            margin-bottom: 30px;
        }
        .section-title {
            background: #343a40;
            color: white;
            padding: 10px 15px;
            border-radius: 5px;
            margin-bottom: 15px;
        }
        .test-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        .test-table th,
        .test-table td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        .test-table th {
            background-color: #f2f2f2;
            font-weight: bold;
        }
        .status-passed { color: #28a745; font-weight: bold; }
        .status-failed { color: #dc3545; font-weight: bold; }
        .status-skipped { color: #ffc107; font-weight: bold; }
        .test-row:hover { background-color: #f5f5f5; }
        .coverage-good { background-color: #d4edda; }
        .coverage-medium { background-color: #fff3cd; }
        .coverage-poor { background-color: #f8d7da; }
        .file-path {
            font-family: monospace;
            font-size: 0.9em;
            color: #666;
        }
        .test-method {
            font-family: monospace;
            font-size: 0.9em;
        }
        .test-exception {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            border-radius: 4px;
            padding: 10px;
            font-family: monospace;
            font-size: 0.8em;
            overflow-x: auto;
            max-height: 200px;
            overflow-y: auto;
        }
        .progress-bar {
            width: 100%;
            background-color: #e9ecef;
            border-radius: 4px;
            overflow: hidden;
        }
        .progress-fill {
            height: 20px;
            background-color: #28a745;
            transition: width 0.3s ease;
        }
        """;
    }

    /**
     * 生成HTML主体内容
     */
    private String generateHtmlBody(TestExecutionResult result) {
        StringBuilder body = new StringBuilder();

        // 标题部分
        body.append("    <div class=\"header\">\n")
               .append("        <h1>🧪 IOE-DREAM 测试覆盖率报告</h1>\n")
               .append("        <p>生成时间: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("</p>\n")
               .append("    </div>\n");

        // 统计摘要
        body.append(generateSummarySection(result));

        // 覆盖率统计
        body.append(generateCoverageSection(result));

        // 测试执行结果
        body.append(generateTestResultsSection(result));

        return body.toString();
    }

    /**
     * 生成统计摘要部分
     */
    private String generateSummarySection(TestExecutionResult result) {
        StringBuilder summary = new StringBuilder();

        summary.append("    <div class=\"summary\">\n");

        // 总测试数
        summary.append("        <div class=\"summary-card\">\n")
               .append("            <div class=\"summary-number\">").append(result.getTotalTests()).append("</div>\n")
               .append("            <div class=\"summary-label\">总测试数</div>\n")
               .append("        </div>\n");

        // 通过率
        summary.append("        <div class=\"summary-card\">\n")
               .append("            <div class=\"summary-number\">").append(String.format("%.1f%%", result.getSuccessRate())).append("</div>\n")
               .append("            <div class=\"summary-label\">通过率</div>\n")
               .append("        </div>\n");

        // 执行时间
        summary.append("        <div class=\"summary-card\">\n")
               .append("            <div class=\"summary-number\">").append(result.getExecutionTime()).append("ms</div>\n")
               .append("            <div class=\"summary-label\">执行时间</div>\n")
               .append("        </div>\n");

        summary.append("    </div>\n");

        return summary.toString();
    }

    /**
     * 生成覆盖率统计部分
     */
    private String generateCoverageSection(TestExecutionResult result) {
        StringBuilder section = new StringBuilder();

        section.append("    <div class=\"section\">\n")
               .append("        <h2 class=\"section-title\">📊 覆盖率统计</h2>\n")
               .append("        <table class=\"test-table\">\n")
               .append("            <thead>\n")
               .append("                <tr>\n")
               .append("                    <th>模块</th>\n")
               .append("                    <th>类</th>\n")
               .append("                    <th>行覆盖率</th>\n")
               .append("                    <th>分支覆盖率</th>\n")
               .append("                    <th>方法覆盖率</th>\n")
               .append("                </tr>\n")
               .append("            </thead>\n")
               .append("            <tbody>\n");

        Map<String, Map<String, ClassCoverage>> moduleCoverages = result.getCoverageData();
        for (Map.Entry<String, Map<String, ClassCoverage>> moduleEntry : moduleCoverages.entrySet()) {
            String moduleName = moduleEntry.getKey();
            Map<String, ClassCoverage> classes = moduleEntry.getValue();

            for (Map.Entry<String, ClassCoverage> classEntry : classes.entrySet()) {
                String className = classEntry.getKey();
                ClassCoverage coverage = classEntry.getValue();

                String lineCoverageClass = getCoverageClass(coverage.getLineCoverage());
                String branchCoverageClass = getCoverageClass(coverage.getBranchCoverage());
                String methodCoverageClass = getCoverageClass(coverage.getMethodCoverage());

                section.append("                <tr>\n")
                       .append("                    <td>").append(moduleName).append("</td>\n")
                       .append("                    <td class=\"file-path\">").append(className).append("</td>\n")
                       .append("                    <td class=\"").append(lineCoverageClass).append("\">\n")
                       .append("                        ").append(String.format("%.1f%%", coverage.getLineCoverage())).append("\n")
                       .append("                    </td>\n")
                       .append("                    <td class=\"").append(branchCoverageClass).append("\">\n")
                       .append("                        ").append(String.format("%.1f%%", coverage.getBranchCoverage())).append("\n")
                       .append("                    </td>\n")
                       .append("                    <td class=\"").append(methodCoverageClass).append("\">\n")
                       .append("                        ").append(String.format("%.1f%%", coverage.getMethodCoverage())).append("\n")
                       .append("                    </td>\n")
                       .append("                </tr>\n");
            }
        }

        section.append("            </tbody>\n")
               .append("        </table>\n")
               .append("    </div>\n");

        return section.toString();
    }

    /**
     * 生成测试结果部分
     */
    private String generateTestResultsSection(TestExecutionResult result) {
        StringBuilder section = new StringBuilder();

        section.append("    <div class=\"section\">\n")
               .append("        <h2 class=\"section-title\">📋 测试执行结果</h2>\n")
               .append("        <table class=\"test-table\">\n")
               .append("            <thead>\n")
               .append("                <tr>\n")
               .append("                    <th>测试类</th>\n")
               .append("                    <th>测试方法</th>\n")
               .append("                    <th>状态</th>\n")
               .append("                    <th>耗时(ms)</th>\n")
               .append("                    <th>异常信息</th>\n")
               .append("                </tr>\n")
               .append("            </thead>\n")
               .append("            <tbody>\n");

        for (TestExecutionResult.TestResult testResult : result.getTestResults()) {
            String statusClass = "status-" + testResult.getStatus().name().toLowerCase();

            section.append("                <tr class=\"test-row\">\n")
                   .append("                    <td class=\"file-path\">").append(testResult.getClassName()).append("</td>\n")
                   .append("                    <td class=\"test-method\">").append(testResult.getMethodName()).append("</td>\n")
                   .append("                    <td class=\"").append(statusClass).append("\">").append(testResult.getStatus().getDescription()).append("</td>\n")
                   .append("                    <td>").append(testResult.getExecutionTime()).append("</td>\n")
                   .append("                    <td>\n");

            if (testResult.getException() != null) {
                section.append("                        <div class=\"test-exception\">\n")
                       .append(testResult.getException().getMessage()).append("\n")
                       .append("                        </div>\n");
            }

            section.append("                    </td>\n")
                   .append("                </tr>\n");
        }

        section.append("            </tbody>\n")
               .append("        </table>\n")
               .append("    </div>\n");

        return section.toString();
    }

    /**
     * 获取覆盖率样式类
     */
    private String getCoverageClass(double coverage) {
        if (coverage >= 80.0) {
            return "coverage-good";
        } else if (coverage >= 60.0) {
            return "coverage-medium";
        } else {
            return "coverage-poor";
        }
    }
}
```

---

## ⚠️ 最佳实践与注意事项

### ✅ 推荐实践

1. **测试编写原则**
   - 遵循AAA原则（Arrange, Act, Assert）
   - 保持测试的独立性
   - 使用有意义的测试数据
   - 覆盖边界条件

2. **测试设计原则**
   - 单一职责原则
   - 快速执行原则
   - 可重复性原则
   - 自动化验证

3. **测试覆盖策略**
   - 语句覆盖率 > 80%
   - 分支覆盖率 > 70%
   - 方法覆盖率 > 90%
   - 关键路径100%覆盖

4. **测试数据管理**
   - 使用Builder模式
   - 参数化测试
   - 随机化数据
   - 测试数据隔离

### ❌ 避免的陷阱

1. **测试质量问题**
   - 避免测试依赖数据库
   - 不要忽略异常测试
   - 避免测试用例重复
   - 不要忽略边界条件

2. **性能问题**
   - 避免慢速测试
   - 不要忽略测试性能监控
   - 避免内存泄漏
   - 不要忽略并发测试

3. **维护问题**
   - 避免脆弱的测试
   - 不要忽视测试文档
   - 避免测试代码重复
   - 不要忽视测试重构

---

## 📊 评估标准

### 🎯 技能掌握评估

#### 理论知识 (30%)
- [ ] 测试金字塔理论和实践
- [ ] JUnit 5和Mockito使用
- [ ] TestContainers集成测试
- [ ] 测试驱动开发(TDD)

#### 实践能力 (50%)
- [ ] 能够设计测试策略
- [ ] 熟练编写各种类型测试
- [ ] 能够实现测试数据生成
- [ ] 掌握测试报告生成

#### 问题解决 (20%)
- [ ] 测试覆盖率提升
- [ ] 测试性能优化
- - 测试调试技巧
- [ ] 测试自动化实现

### 📈 质量标准

- **单元测试覆盖率**: > 80%
- **集成测试覆盖率**: > 60%
- **API测试覆盖率**: > 70%
- **测试执行效率**: < 5分钟

---

## 🔗 相关技能

- **前置技能**: ai-code-generation-specialist, automated-refactoring-specialist
- **相关技能**: code-quality-protector, performance-tuning-specialist
- **进阶技能**: devops-expert, monitoring-alerting-specialist

---

## 💡 持续学习方向

1. **高级测试技术**: BDD测试、契约测试、混沌工程
2. **测试自动化**: CI/CD集成、自动化报告
3. **性能测试**: 负载测试、压力测试、并发测试
4. **智能测试**: AI辅助测试用例生成、智能缺陷定位

---

**⚠️ 重要提醒**: 测试是质量保障的重要环节，但不是唯一手段。测试生成工具应该作为辅助手段，不能完全替代人工测试设计和编写。所有生成的测试都需要人工审查和验证，确保测试的有效性和可靠性。测试代码本身也需要定期重构和优化。