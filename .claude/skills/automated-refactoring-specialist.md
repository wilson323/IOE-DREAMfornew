# 自动化重构专家

> **版本**: v1.0.0
> **更新时间**: 2025-11-21
> **分类**: AI辅助开发技能 > 代码重构
> **标签**: ["自动化重构", "代码优化", "智能分析", "质量提升", "IOE-DREAM标准"]
> **技能等级**: ★★★ 专家级
> **适用角色**: 高级开发工程师、重构专家、技术架构师
> **前置技能**: ai-code-generation-specialist, code-quality-protector, spring-boot-jakarta-guardian
> **预计学时**: 60-80小时

---

## 📋 技能概述

本技能专门为IOE-DREAM项目提供智能化的代码重构解决方案，基于Java 17 + Spring Boot 3.x + Jakarta技术栈，实现代码质量分析、问题检测、自动化重构等功能。确保代码始终符合repowiki规范和企业级质量标准。

**技术基础**: AST解析 + 静态分析 + 智能重构算法 + 质量度量
**核心目标**: 提升代码质量、减少技术债务、维护编码一致性

---

## 🏗️ 自动化重构架构

### 1. 重构引擎核心

#### 重构引擎配置
```java
package net.lab1024.sa.base.refactor;

import net.lab1024.sa.base.refactor.analyzer.CodeAnalyzer;
import net.lab1024.sa.base.refactor.detector.ProblemDetector;
import net.lab1024.sa.base.refactor.refactor.RefactoringOperation;
import net.lab1024.sa.base.refactor.validator.RefactoringValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自动化重构引擎
 * 严格遵循IOE-DREAM项目的repowiki规范
 */
@Slf4j
@Component
public class AutomatedRefactoringEngine {

    private final CodeAnalyzer codeAnalyzer;
    private final ProblemDetector problemDetector;
    private final RefactoringValidator validator;
    private final Map<String, RefactoringStrategy> refactoringStrategies;

    public AutomatedRefactoringEngine(CodeAnalyzer codeAnalyzer,
                                     ProblemDetector problemDetector,
                                     RefactoringValidator validator) {
        this.codeAnalyzer = codeAnalyzer;
        this.problemDetector = problemDetector;
        this.validator = validator;
        this.refactoringStrategies = new ConcurrentHashMap<>();
        initializeRefactoringStrategies();
    }

    /**
     * 初始化重构策略
     */
    private void initializeRefactoringStrategies() {
        // Jakarta包名重构策略
        refactoringStrategies.put("jakarta-migration", new JakartaMigrationRefactoring());

        // 依赖注入重构策略
        refactoringStrategies.put("dependency-injection", new DependencyInjectionRefactoring());

        // 代码格式化策略
        refactoringStrategies.put("code-formatting", new CodeFormattingRefactoring());

        // 异常处理重构策略
        refactoringStrategies.put("exception-handling", new ExceptionHandlingRefactoring());

        // 日志记录重构策略
        refactoringStrategies.put("logging-standardization", new LoggingStandardizationRefactoring());

        // 命名规范重构策略
        refactoringStrategies.put("naming-convention", new NamingConventionRefactoring());

        // 重复代码重构策略
        refactoringStrategies.put("duplicate-code", new DuplicateCodeRefactoring());

        // 复杂度重构策略
        refactoringStrategies.put("complexity-reduction", new ComplexityReductionRefactoring());

        log.info("重构策略初始化完成，共配置 {} 个策略", refactoringStrategies.size());
    }

    /**
     * 执行自动化重构
     */
    public RefactoringResult refactor(RefactoringRequest request) {
        log.info("开始执行自动化重构: {}", request.getProjectPath());

        try {
            // 1. 分析代码
            CodeAnalysisResult analysisResult = codeAnalyzer.analyze(request);

            // 2. 检测问题
            List<CodeProblem> problems = problemDetector.detect(analysisResult);

            // 3. 制定重构计划
            RefactoringPlan plan = createRefactoringPlan(problems, request);

            // 4. 执行重构
            RefactoringResult result = executeRefactoringPlan(plan, analysisResult);

            // 5. 验证结果
            validateRefactoringResult(result);

            log.info("自动化重构完成，处理了 {} 个问题", result.getFixedProblems().size());
            return result;

        } catch (Exception e) {
            log.error("自动化重构失败", e);
            throw new RefactoringException("重构执行失败", e);
        }
    }

    /**
     * 创建重构计划
     */
    private RefactoringPlan createRefactoringPlan(List<CodeProblem> problems, RefactoringRequest request) {
        RefactoringPlan plan = new RefactoringPlan();

        // 按优先级排序问题
        problems.sort((p1, p2) -> Integer.compare(p2.getSeverity().getValue(), p1.getSeverity().getValue()));

        for (CodeProblem problem : problems) {
            String problemType = problem.getType();
            RefactoringStrategy strategy = refactoringStrategies.get(problemType);

            if (strategy != null && strategy.canHandle(problem)) {
                RefactoringOperation operation = strategy.createOperation(problem);
                plan.addOperation(operation);
            }
        }

        log.info("创建重构计划，包含 {} 个重构操作", plan.getOperations().size());
        return plan;
    }

    /**
     * 执行重构计划
     */
    private RefactoringResult executeRefactoringPlan(RefactoringPlan plan, CodeAnalysisResult analysisResult) {
        RefactoringResult result = new RefactoringResult();

        for (RefactoringOperation operation : plan.getOperations()) {
            try {
                log.debug("执行重构操作: {}", operation.getDescription());

                // 验证操作
                validator.validate(operation, analysisResult);

                // 执行操作
                operation.execute();

                result.addFixedProblem(operation.getProblem());
                log.debug("重构操作执行成功: {}", operation.getDescription());

            } catch (Exception e) {
                log.error("重构操作执行失败: {}", operation.getDescription(), e);
                result.addFailedOperation(operation);
            }
        }

        return result;
    }

    /**
     * 验证重构结果
     */
    private void validateRefactoringResult(RefactoringResult result) {
        // 检查是否有编译错误
        boolean hasCompilationErrors = result.getFailedOperations().stream()
            .anyMatch(op -> op.getError().contains("compilation"));

        if (hasCompilationErrors) {
            log.error("重构后存在编译错误，需要人工干预");
            throw new RefactoringException("重构导致编译错误");
        }

        // 检查重构覆盖率
        int totalProblems = result.getFixedProblems().size() + result.getFailedOperations().size();
        double successRate = totalProblems > 0 ?
            (double) result.getFixedProblems().size() / totalProblems * 100 : 100;

        log.info("重构完成，成功率: {:.2f}%", successRate);
    }

    /**
     * 获取重构策略
     */
    public RefactoringStrategy getRefactoringStrategy(String type) {
        return refactoringStrategies.get(type);
    }
}
```

### 2. 问题检测器

#### 通用问题检测器
```java
package net.lab1024.sa.base.refactor.detector;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.refactor.model.CodeProblem;
import net.lab1024.sa.base.refactor.model.ProblemSeverity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码问题检测器
 * 检测违反repowiki规范的代码问题
 */
@Slf4j
@Component
public class ProblemDetector {

    // Jakarta包名检测模式
    private static final Pattern JAVAX_IMPORT_PATTERN = Pattern.compile(
        "import\\s+javax\\.(validation|servlet|persistence|annotation|xml\\.bind|jms|ejb)\\.",
        Pattern.MULTILINE
    );

    // @Autowired检测模式
    private static final Pattern AUTOWIRED_PATTERN = Pattern.compile(
        "@Autowired",
        Pattern.MULTILINE
    );

    // System.out.println检测模式
    private static final Pattern SYSTEM_OUT_PATTERN = Pattern.compile(
        "System\\.out\\.println",
        Pattern.MULTILINE
    );

    // 长方法检测
    private static final Pattern LONG_METHOD_PATTERN = Pattern.compile(
        "public\\s+\\w+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{",
        Pattern.MULTILINE
    );

    // 复杂表达式检测
    private static final Pattern COMPLEX_EXPRESSION_PATTERN = Pattern.compile(
        "if\\s*\\([^)]{50,}\\)",
        Pattern.MULTILINE
    );

    /**
     * 检测代码问题
     */
    public List<CodeProblem> detect(CodeAnalysisResult analysisResult) {
        List<CodeProblem> problems = new ArrayList<>();

        try {
            for (Path javaFile : analysisResult.getJavaFiles()) {
                detectProblemsInFile(javaFile, problems);
            }
        } catch (Exception e) {
            log.error("检测代码问题失败", e);
        }

        log.info("检测到 {} 个代码问题", problems.size());
        return problems;
    }

    /**
     * 检测单个文件的问题
     */
    private void detectProblemsInFile(Path javaFile, List<CodeProblem> problems) throws IOException {
        String content = Files.readString(javaFile);

        // 检测javax包使用
        detectJavaxImports(javaFile, content, problems);

        // 检测@Autowired使用
        detectAutowiredUsage(javaFile, content, problems);

        // 检测System.out使用
        detectSystemOutUsage(javaFile, content, problems);

        // 检测长方法
        detectLongMethods(javaFile, content, problems);

        // 检测复杂表达式
        detectComplexExpressions(javaFile, content, problems);

        // 检测命名规范
        detectNamingConventions(javaFile, content, problems);
    }

    /**
     * 检测javax包导入
     */
    private void detectJavaxImports(Path javaFile, String content, List<CodeProblem> problems) {
        Matcher matcher = JAVAX_IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            String importStatement = matcher.group();
            int lineNumber = getLineNumber(content, matcher.start());

            CodeProblem problem = CodeProblem.builder()
                .type("jakarta-migration")
                .severity(ProblemSeverity.CRITICAL)
                .message("使用了javax包，需要迁移到jakarta")
                .description("根据repowiki规范，必须使用jakarta包替代javax包")
                .filePath(javaFile.toString())
                .lineNumber(lineNumber)
                .content(importStatement)
                .suggestion("将import javax.* 替换为 jakarta.*")
                .build();

            problems.add(problem);
        }
    }

    /**
     * 检测@Autowired使用
     */
    private void detectAutowiredUsage(Path javaFile, String content, List<CodeProblem> problems) {
        Matcher matcher = AUTOWIRED_PATTERN.matcher(content);
        while (matcher.find()) {
            int lineNumber = getLineNumber(content, matcher.start());

            CodeProblem problem = CodeProblem.builder()
                .type("dependency-injection")
                .severity(ProblemSeverity.CRITICAL)
                .message("使用了@Autowired，需要替换为@Resource")
                .description("根据repowiki规范，必须使用@Resource替代@Autowired")
                .filePath(javaFile.toString())
                .lineNumber(lineNumber)
                .content("@Autowired")
                .suggestion("将@Autowired替换为@Resource")
                .build();

            problems.add(problem);
        }
    }

    /**
     * 检测System.out使用
     */
    private void detectSystemOutUsage(Path javaFile, String content, List<CodeProblem> problems) {
        Matcher matcher = SYSTEM_OUT_PATTERN.matcher(content);
        while (matcher.find()) {
            int lineNumber = getLineNumber(content, matcher.start());

            CodeProblem problem = CodeProblem.builder()
                .type("logging-standardization")
                .severity(ProblemSeverity.HIGH)
                .message("使用了System.out.println，需要使用SLF4J")
                .description("根据repowiki规范，必须使用SLF4J替代System.out")
                .filePath(javaFile.toString())
                .lineNumber(lineNumber)
                .content("System.out.println")
                .suggestion("使用log.info()或其他SLF4J方法")
                .build();

            problems.add(problem);
        }
    }

    /**
     * 检测长方法
     */
    private void detectLongMethods(Path javaFile, String content, List<CodeProblem> problems) {
        String[] lines = content.split("\n");
        int braceCount = 0;
        int methodStartLine = 0;
        String currentMethod = "";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (LONG_METHOD_PATTERN.matcher(line).find()) {
                methodStartLine = i;
                currentMethod = extractMethodName(line);
                braceCount = 1;
            } else if (braceCount > 0) {
                braceCount += countBraces(line);

                if (braceCount == 0) {
                    int methodLength = i - methodStartLine + 1;
                    if (methodLength > 50) {  // 超过50行的方法认为是长方法
                        CodeProblem problem = CodeProblem.builder()
                            .type("complexity-reduction")
                            .severity(ProblemSeverity.MEDIUM)
                            .message("方法过长（" + methodLength + "行），建议拆分")
                            .description("长方法降低代码可读性和可维护性")
                            .filePath(javaFile.toString())
                            .lineNumber(methodStartLine + 1)
                            .content(currentMethod)
                            .suggestion("将长方法拆分为多个小方法")
                            .build();

                        problems.add(problem);
                    }
                }
            }
        }
    }

    /**
     * 检测复杂表达式
     */
    private void detectComplexExpressions(Path javaFile, String content, List<CodeProblem> problems) {
        Matcher matcher = COMPLEX_EXPRESSION_PATTERN.matcher(content);
        while (matcher.find()) {
            String expression = matcher.group();
            int lineNumber = getLineNumber(content, matcher.start());

            CodeProblem problem = CodeProblem.builder()
                .type("complexity-reduction")
                .severity(ProblemSeverity.MEDIUM)
                .message("条件表达式过于复杂")
                .description("复杂的条件表达式降低代码可读性")
                .filePath(javaFile.toString())
                .lineNumber(lineNumber)
                .content(expression)
                .suggestion("将复杂条件拆分为多个变量")
                .build();

            problems.add(problem);
        }
    }

    /**
     * 检测命名规范
     */
    private void detectNamingConventions(Path javaFile, String content, List<CodeProblem> problems) {
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // 检查类名命名
            if (line.matches("public\\s+class\\s+\\w+")) {
                String className = extractClassName(line);
                if (!isProperClassName(className)) {
                    CodeProblem problem = CodeProblem.builder()
                        .type("naming-convention")
                        .severity(ProblemSeverity.MEDIUM)
                        .message("类名不符合大驼峰命名规范")
                        .description("类名应该使用大驼峰命名规范")
                        .filePath(javaFile.toString())
                        .lineNumber(i + 1)
                        .content(className)
                        .suggestion("使用大驼峰命名规范，如：UserService")
                        .build();

                    problems.add(problem);
                }
            }

            // 检查方法名命名
            if (line.matches("(public|private|protected)\\s+(static\\s+)?\\w+\\s+\\w+\\s*\\(")) {
                String methodName = extractMethodName(line);
                if (!isProperMethodName(methodName)) {
                    CodeProblem problem = CodeProblem.builder()
                        .type("naming-convention")
                        .severity(ProblemSeverity.MEDIUM)
                        .message("方法名不符合小驼峰命名规范")
                        .description("方法名应该使用小驼峰命名规范")
                        .filePath(javaFile.toString())
                        .lineNumber(i + 1)
                        .content(methodName)
                        .suggestion("使用小驼峰命名规范，如：getUserById")
                        .build();

                    problems.add(problem);
                }
            }
        }
    }

    // 辅助方法
    private int getLineNumber(String content, int position) {
        return content.substring(0, position).split("\n").length;
    }

    private String extractMethodName(String line) {
        Matcher matcher = Pattern.compile("\\s+(\\w+)\\s*\\(").matcher(line);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String extractClassName(String line) {
        Matcher matcher = Pattern.compile("class\\s+(\\w+)").matcher(line);
        return matcher.find() ? matcher.group(1) : "";
    }

    private boolean isProperClassName(String className) {
        return className.matches("[A-Z][a-zA-Z0-9]*");
    }

    private boolean isProperMethodName(String methodName) {
        return methodName.matches("[a-z][a-zA-Z0-9]*");
    }

    private int countBraces(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == '{') count++;
            else if (c == '}') count--;
        }
        return count;
    }
}
```

### 3. 重构策略实现

#### Jakarta包迁移策略
```java
package net.lab1024.sa.base.refactor.refactor;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.refactor.model.CodeProblem;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Jakarta包迁移重构策略
 * 将javax包替换为jakarta包
 */
@Slf4j
@Component
public class JakartaMigrationRefactoring implements RefactoringStrategy {

    private static final Map<String, String> PACKAGE_MAPPINGS = new HashMap<>();

    static {
        // Jakarta EE 9+ 包映射
        PACKAGE_MAPPINGS.put("javax.validation", "jakarta.validation");
        PACKAGE_MAPPINGS.put("javax.servlet", "jakarta.servlet");
        PACKAGE_MAPPINGS.put("javax.persistence", "jakarta.persistence");
        PACKAGE_MAPPINGS.put("javax.annotation", "jakarta.annotation");
        PACKAGE_MAPPINGS.put("javax.xml.bind", "jakarta.xml.bind");
        PACKAGE_MAPPINGS.put("javax.jms", "jakarta.jms");
        PACKAGE_MAPPINGS.put("javax.ejb", "jakarta.ejb");
        PACKAGE_MAPPINGS.put("javax.faces", "jakarta.faces");
        PACKAGE_MAPPINGS.put("javax.ws.rs", "jakarta.ws.rs");
        PACKAGE_MAPPINGS.put("javax.enterprise", "jakarta.enterprise");
        PACKAGE_MAPPINGS.put("javax.transaction", "jakarta.transaction");
    }

    @Override
    public boolean canHandle(CodeProblem problem) {
        return "jakarta-migration".equals(problem.getType());
    }

    @Override
    public RefactoringOperation createOperation(CodeProblem problem) {
        return new JakartaMigrationOperation(problem);
    }

    @Override
    public int getPriority() {
        return 1; // 最高优先级
    }

    /**
     * Jakarta迁移操作
     */
    private static class JakartaMigrationOperation implements RefactoringOperation {

        private final CodeProblem problem;

        public JakartaMigrationOperation(CodeProblem problem) {
            this.problem = problem;
        }

        @Override
        public void execute() throws RefactoringException {
            try {
                Path filePath = Path.of(problem.getFilePath());
                String content = Files.readString(filePath);

                // 执行包名替换
                String newContent = migratePackages(content);

                // 写回文件
                Files.writeString(filePath, newContent);

                log.info("Jakarta包迁移完成: {}", filePath);

            } catch (IOException e) {
                throw new RefactoringException("Jakarta迁移失败: " + problem.getFilePath(), e);
            }
        }

        private String migratePackages(String content) {
            String newContent = content;

            for (Map.Entry<String, String> mapping : PACKAGE_MAPPINGS.entrySet()) {
                String javaxPackage = mapping.getKey();
                String jakartaPackage = mapping.getValue();

                // 替换import语句
                newContent = newContent.replaceAll(
                    "import\\s+" + javaxPackage.replace(".", "\\.") + "\\.",
                    "import " + jakartaPackage + "."
                );

                // 替换代码中的包引用
                newContent = newContent.replaceAll(
                    javaxPackage.replace(".", "\\.") + "\\.",
                    jakartaPackage + "."
                );
            }

            return newContent;
        }

        @Override
        public CodeProblem getProblem() {
            return problem;
        }

        @Override
        public String getDescription() {
            return "Jakarta包迁移: " + problem.getFilePath();
        }
    }
}
```

#### 依赖注入重构策略
```java
package net.lab1024.sa.base.refactor.refactor;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.refactor.model.CodeProblem;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 依赖注入重构策略
 * 将@Autowired替换为@Resource
 */
@Slf4j
@Component
public class DependencyInjectionRefactoring implements RefactoringStrategy {

    @Override
    public boolean canHandle(CodeProblem problem) {
        return "dependency-injection".equals(problem.getType());
    }

    @Override
    public RefactoringOperation createOperation(CodeProblem problem) {
        return new DependencyInjectionOperation(problem);
    }

    @Override
    public int getPriority() {
        return 1; // 最高优先级
    }

    /**
     * 依赖注入重构操作
     */
    private static class DependencyInjectionOperation implements RefactoringOperation {

        private final CodeProblem problem;

        public DependencyInjectionOperation(CodeProblem problem) {
            this.problem = problem;
        }

        @Override
        public void execute() throws RefactoringException {
            try {
                Path filePath = Path.of(problem.getFilePath());
                String content = Files.readString(filePath);

                // 替换@Autowired为@Resource
                String newContent = content.replaceAll("@Autowired", "@Resource");

                // 写回文件
                Files.writeString(filePath, newContent);

                log.info("依赖注入重构完成: {}", filePath);

            } catch (IOException e) {
                throw new RefactoringException("依赖注入重构失败: " + problem.getFilePath(), e);
            }
        }

        @Override
        public CodeProblem getProblem() {
            return problem;
        }

        @Override
        public String getDescription() {
            return "依赖注入重构: " + problem.getFilePath();
        }
    }
}
```

### 4. 重构结果报告

#### 重构报告生成器
```java
package net.lab1024.sa.base.refactor.report;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.refactor.model.*;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 重构报告生成器
 */
@Slf4j
@Component
public class RefactoringReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成HTML格式的重构报告
     */
    public void generateHtmlReport(RefactoringResult result, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath + "/refactoring-report.html")) {
            writer.write(generateHtmlContent(result));
            log.info("HTML重构报告生成完成: {}", outputPath + "/refactoring-report.html");
        } catch (IOException e) {
            log.error("生成HTML报告失败", e);
        }
    }

    /**
     * 生成JSON格式的重构报告
     */
    public void generateJsonReport(RefactoringResult result, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath + "/refactoring-report.json")) {
            writer.write(generateJsonContent(result));
            log.info("JSON重构报告生成完成: {}", outputPath + "/refactoring-report.json");
        } catch (IOException e) {
            log.error("生成JSON报告失败", e);
        }
    }

    /**
     * 生成HTML内容
     */
    private String generateHtmlContent(RefactoringResult result) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n")
           .append("<html lang=\"zh-CN\">\n")
           .append("<head>\n")
           .append("    <meta charset=\"UTF-8\">\n")
           .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
           .append("    <title>IOE-DREAM 代码重构报告</title>\n")
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
            border-left: 4px solid #007bff;
        }
        .summary-number {
            font-size: 2em;
            font-weight: bold;
            color: #007bff;
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
        .problem-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        .problem-table th,
        .problem-table td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        .problem-table th {
            background-color: #f2f2f2;
            font-weight: bold;
        }
        .severity-critical { color: #dc3545; font-weight: bold; }
        .severity-high { color: #fd7e14; font-weight: bold; }
        .severity-medium { color: #ffc107; font-weight: bold; }
        .severity-low { color: #28a745; }
        .problem-row:hover { background-color: #f5f5f5; }
        .file-path {
            font-family: monospace;
            font-size: 0.9em;
            color: #666;
        }
        .code-snippet {
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 4px;
            padding: 10px;
            font-family: monospace;
            font-size: 0.9em;
            overflow-x: auto;
        }
        """;
    }

    /**
     * 生成HTML主体内容
     */
    private String generateHtmlBody(RefactoringResult result) {
        StringBuilder body = new StringBuilder();

        // 标题部分
        body.append("    <div class=\"header\">\n")
           .append("        <h1>🔧 IOE-DREAM 代码重构报告</h1>\n")
           .append("        <p>生成时间: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("</p>\n")
           .append("    </div>\n");

        // 统计摘要
        body.append(generateSummarySection(result));

        // 修复的问题
        body.append(generateFixedProblemsSection(result.getFixedProblems()));

        // 失败的操作
        if (!result.getFailedOperations().isEmpty()) {
            body.append(generateFailedOperationsSection(result.getFailedOperations()));
        }

        return body.toString();
    }

    /**
     * 生成统计摘要部分
     */
    private String generateSummarySection(RefactoringResult result) {
        Map<ProblemSeverity, Long> severityCount = result.getFixedProblems().stream()
            .collect(Collectors.groupingBy(
                CodeProblem::getSeverity,
                Collectors.counting()
            ));

        StringBuilder summary = new StringBuilder();
        summary.append("    <div class=\"summary\">\n");

        // 总计
        summary.append("        <div class=\"summary-card\">\n")
           .append("            <div class=\"summary-number\">").append(result.getFixedProblems().size()).append("</div>\n")
           .append("            <div class=\"summary-label\">修复问题总数</div>\n")
           .append("        </div>\n");

        // 成功率
        double successRate = result.getSuccessRate();
        summary.append("        <div class=\"summary-card\">\n")
           .append("            <div class=\"summary-number\">").append(String.format("%.1f%%", successRate)).append("</div>\n")
           .append("            <div class=\"summary-label\">修复成功率</div>\n")
           .append("        </div>\n");

        // 按严重程度统计
        for (Map.Entry<ProblemSeverity, Long> entry : severityCount.entrySet()) {
            String severityClass = "severity-" + entry.getKey().name().toLowerCase();
            summary.append("        <div class=\"summary-card\">\n")
               .append("            <div class=\"summary-number ").append(severityClass).append("\">").append(entry.getValue()).append("</div>\n")
               .append("            <div class=\"summary-label\">").append(entry.getKey().getDescription()).append("</div>\n")
               .append("        </div>\n");
        }

        summary.append("    </div>\n");

        return summary.toString();
    }

    /**
     * 生成修复问题部分
     */
    private String generateFixedProblemsSection(List<CodeProblem> fixedProblems) {
        StringBuilder section = new StringBuilder();

        section.append("    <div class=\"section\">\n")
               .append("        <h2 class=\"section-title\">✅ 修复的问题 (" + fixedProblems.size() + "个)</h2>\n")
               .append("        <table class=\"problem-table\">\n")
               .append("            <thead>\n")
               .append("                <tr>\n")
               .append("                    <th>严重程度</th>\n")
               .append("                    <th>问题类型</th>\n")
               .append("                    <th>文件</th>\n")
               .append("                    <th>行号</th>\n")
               .append("                    <th>问题描述</th>\n")
               .append("                    <th>修复建议</th>\n")
               .append("                </tr>\n")
               .append("            </thead>\n")
               .append("            <tbody>\n");

        for (CodeProblem problem : fixedProblems) {
            String severityClass = "severity-" + problem.getSeverity().name().toLowerCase();

            section.append("                <tr class=\"problem-row\">\n")
                   .append("                    <td class=\"").append(severityClass).append("\">").append(problem.getSeverity().getDescription()).append("</td>\n")
                   .append("                    <td>").append(problem.getType()).append("</td>\n")
                   .append("                    <td class=\"file-path\">").append(getRelativePath(problem.getFilePath())).append("</td>\n")
                   .append("                    <td>").append(problem.getLineNumber()).append("</td>\n")
                   .append("                    <td>").append(problem.getMessage()).append("</td>\n")
                   .append("                    <td>").append(problem.getSuggestion()).append("</td>\n")
                   .append("                </tr>\n");
        }

        section.append("            </tbody>\n")
               .append("        </table>\n")
               .append("    </div>\n");

        return section.toString();
    }

    /**
     * 生成失败操作部分
     */
    private String generateFailedOperationsSection(List<RefactoringOperation> failedOperations) {
        StringBuilder section = new StringBuilder();

        section.append("    <div class=\"section\">\n")
               .append("        <h2 class=\"section-title\">❌ 失败的操作 (" + failedOperations.size() + "个)</h2>\n")
               .append("        <table class=\"problem-table\">\n")
               .append("            <thead>\n")
               .append("                <tr>\n")
               .append("                    <th>操作描述</th>\n")
               .append("                    <th>文件</th>\n")
               .append("                    <th>错误信息</th>\n")
               .append("                </tr>\n")
               .append("            </thead>\n")
               .append("            <tbody>\n");

        for (RefactoringOperation operation : failedOperations) {
            section.append("                <tr class=\"problem-row\">\n")
                   .append("                    <td>").append(operation.getDescription()).append("</td>\n")
                   .append("                    <td class=\"file-path\">").append(getRelativePath(operation.getProblem().getFilePath())).append("</td>\n")
                   .append("                    <td class=\"code-snippet\">").append(operation.getError().getMessage()).append("</td>\n")
                   .append("                </tr>\n");
        }

        section.append("            </tbody>\n")
               .append("        </table>\n")
               .append("    </div>\n");

        return section.toString();
    }

    /**
     * 获取相对路径
     */
    private String getRelativePath(String fullPath) {
        if (fullPath.contains("/src/")) {
            return fullPath.substring(fullPath.indexOf("/src/"));
        }
        return fullPath;
    }

    /**
     * 生成JSON内容
     */
    private String generateJsonContent(RefactoringResult result) {
        // 简化实现，实际应该使用JSON库
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\",\n");
        json.append("  \"fixedProblems\": ").append(result.getFixedProblems().size()).append(",\n");
        json.append("  \"failedOperations\": ").append(result.getFailedOperations().size()).append(",\n");
        json.append("  \"successRate\": ").append(result.getSuccessRate()).append("\n");
        json.append("}\n");
        return json.toString();
    }
}
```

---

## ⚠️ 最佳实践与注意事项

### ✅ 推荐实践

1. **重构原则**
   - 小步快跑，频繁验证
   - 保持测试绿色
   - 优先解决高优先级问题
   - 记录重构原因和效果

2. **安全策略**
   - 重构前备份代码
   - 版本控制管理
   - 分支进行重构
   - 代码审查机制

3. **质量保证**
   - 编译验证
   - 单元测试覆盖
   - 集成测试验证
   - 性能基准测试

4. **团队协作**
   - 重构计划沟通
   - 代码知识分享
   - 重构结果总结
   - 持续改进机制

### ❌ 避免的陷阱

1. **重构风险**
   - 不要一次性重构过多
   - 避免重构关键路径代码
   - 不要忽视测试覆盖
   - 避免破坏现有功能

2. **技术问题**
   - 不要过度重构
   - 避免引入新的复杂性
   - 不要忽视性能影响
   - 避免破坏API兼容性

3. **流程问题**
   - 不要跳过代码审查
   - 避免缺乏文档记录
   - 不要忽视团队沟通
   - 避免急功近利

---

## 📊 评估标准

### 🎯 技能掌握评估

#### 理论知识 (30%)
- [ ] 重构原则和最佳实践
- [ ] 代码质量度量方法
- [ ] 静态分析和AST解析
- [ ] 设计模式和重构模式

#### 实践能力 (50%)
- [ ] 能够设计重构策略
- [ ] 熟练使用静态分析工具
- [ ] 能够实现自动化重构
- [ ] 掌握重构验证方法

#### 问题解决 (20%)
- [ ] 复杂代码结构优化
- [ ] 技术债务清理
- [ ] 性能优化重构
- [ ] 架构演进重构

### 📈 质量标准

- **重构成功率**: > 95%
- **代码质量提升**: 明显改善
- **编译错误率**: 0%
- **测试覆盖率**: 不降低

---

## 🔗 相关技能

- **前置技能**: ai-code-generation-specialist, code-quality-protector
- **相关技能**: intelligent-testing-specialist, performance-tuning-specialist
- **进阶技能**: architecture-design-specialist, system-optimization-specialist

---

## 💡 持续学习方向

1. **高级重构技术**: 重构模式库、智能重构
2. **AI辅助重构**: 机器学习代码分析、自动化重构决策
3. **大型重构**: 架构重构、数据库重构
4. **团队重构**: 重构文化、重构流程

---

**⚠️ 重要提醒**: 自动化重构是强大的工具，但不能完全替代人工判断。所有重构操作都应该经过仔细验证，确保符合IOE-DREAM项目的技术标准和业务需求。重构后一定要进行充分的测试验证。