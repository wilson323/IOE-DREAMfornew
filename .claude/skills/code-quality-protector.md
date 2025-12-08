# 代码质量守护专家技能
## Code Quality Protector

**🎯 技能定位**: IOE-DREAM智慧园区代码质量守护专家，确保代码符合企业级质量标准，预防质量问题，维护代码健康度

**⚡ 技能等级**: ★★★★★ (顶级专家)
**🎯 适用场景**: 代码质量检查、规范审查、质量培训、重构指导、质量度量
**📊 技能覆盖**: 代码规范 | 质量检测 | 重构指导 | 性能优化 | 安全检查 | 测试覆盖

---

## 📋 技能概述

### **核心专长**
- **代码规范检查**: UTF-8编码、命名规范、格式化标准、注释完整性
- **代码质量度量**: 圈复杂度、代码重复率、方法长度、类行数控制
- **性能优化指导**: SQL优化、缓存策略、算法效率、内存管理
- **安全漏洞检测**: SQL注入、XSS攻击、敏感信息泄露、权限控制
- **测试覆盖分析**: 单元测试覆盖率、集成测试、边界测试
- **重构建议提供**: 代码异味识别、重构模式、最佳实践应用

### **解决能力**
- **质量标准执行**: 严格执行企业级代码质量标准
- **质量问题预防**: 在开发阶段预防质量问题的产生
- **质量问题修复**: 系统性识别和修复代码质量问题
- **质量培训指导**: 团队代码质量意识和技能提升
- **质量监控度量**: 持续监控代码质量指标和趋势

---

## 🎯 代码质量标准体系

### 📏 质量指标定义

#### 核心质量指标
| 指标名称 | 目标值 | 说明 | 检查方式 |
|---------|--------|------|----------|
| **代码覆盖率** | ≥80% | 单元测试覆盖率覆盖率 | JaCoCo测试覆盖率分析 |
| **核心业务覆盖率** | =100% | 关键业务逻辑测试覆盖率 | 业务重要性分析 |
| **重复代码率** | ≤3% | 重复代码块比例 | Simian重复代码检测 |
| **圈复杂度** | ≤10 | 方法复杂度控制 | PMD圈复杂度检查 |
| **方法行数** | ≤50 | 单个方法代码行数限制 | 行数统计检查 |
| **类行数** | ≤500 | 单个类代码行数限制 | 类大小分析 |

#### 编码规范指标
| 规范项 | 要求 | 违规影响 | 检查工具 |
|--------|------|----------|----------|
| **编码格式** | UTF-8编码 | 乱码问题 | 文件编码检查 |
| **命名规范** | 驼峰命名法 | 可读性差 | 命名规范检查 |
| **注释完整性** | 公共API必注释 | 维护困难 | 注释覆盖检查 |
| **空行规范** | 合理空行分隔 | 格式混乱 | 代码格式检查 |
| **异常处理** | 完整异常处理 | 程序稳定性 | 异常处理检查 |

### 🛡️ 安全质量指标
| 安全指标 | 目标值 | 说明 | 检查方式 |
|---------|--------|------|----------|
| **SQL注入防护** | 100% | 参数化查询使用 | 代码安全扫描 |
| **XSS攻击防护** | 100% | 输入输出编码 | 安全漏洞检测 |
| **敏感信息保护** | 100% | 敏感数据脱敏 | 敏感信息扫描 |
| **权限控制检查** | 100% | 权限验证完整性 | 权限控制审查 |

---

## 🔍 代码质量检测工具

### 代码规范检查器
```java
// 代码质量检查核心组件
@Component
@Slf4j
public class CodeQualityChecker {

    private final JavaParser javaParser;
    private final MetricCalculator metricCalculator;
    private final CodeStyleChecker codeStyleChecker;

    /**
     * 执行全面的代码质量检查
     */
    public CodeQualityReport checkCodeQuality(String projectPath) {
        log.info("开始代码质量检查: projectPath={}", projectPath);

        CodeQualityReport report = new CodeQualityReport();

        try {
            // 1. 扫描所有Java文件
            List<File> javaFiles = scanJavaFiles(projectPath);
            log.info("发现Java文件数量: {}", javaFiles.size());

            // 2. 对每个文件执行质量检查
            for (File javaFile : javaFiles) {
                checkFileQuality(javaFile, report);
            }

            // 3. 计算整体质量指标
            calculateOverallMetrics(report);

            // 4. 生成质量评分
            generateQualityScore(report);

            log.info("代码质量检查完成: 总体评分={}", report.getOverallScore());

        } catch (Exception e) {
            log.error("代码质量检查失败", e);
            report.addError("CODE_QUALITY_CHECK_FAILED", "代码质量检查执行失败: " + e.getMessage());
        }

        return report;
    }

    private void checkFileQuality(File javaFile, CodeQualityReport report) {
        try {
            String filePath = javaFile.getAbsolutePath();
            String content = Files.readString(javaFile.toPath());

            // 1. 编码检查
            checkFileEncoding(javaFile, report);

            // 2. 解析Java代码
            CompilationUnit cu = javaParser.parse(content);

            // 3. 检查代码风格
            checkCodeStyle(cu, filePath, report);

            // 4. 检查复杂度指标
            checkComplexityMetrics(cu, filePath, report);

            // 5. 检查命名规范
            checkNamingConventions(cu, filePath, report);

            // 6. 检查注释完整性
            checkCommentCompleteness(cu, filePath, report);

            // 7. 检查安全漏洞
            checkSecurityVulnerabilities(cu, filePath, report);

        } catch (Exception e) {
            log.warn("检查文件质量失败: {}", javaFile.getPath(), e);
            report.addError("FILE_CHECK_FAILED", "文件检查失败: " + javaFile.getPath());
        }
    }

    private void checkFileEncoding(File javaFile, CodeQualityReport report) {
        try {
            String encoding = detectFileEncoding(javaFile);
            if (!"UTF-8".equals(encoding)) {
                report.addViolation(new CodeQualityViolation(
                    javaFile.getAbsolutePath(),
                    1,
                    ViolationType.ENCODING_VIOLATION,
                    "文件编码必须是UTF-8，当前编码: " + encoding,
                    ViolationSeverity.HIGH
                ));
            }
        } catch (IOException e) {
            log.warn("检测文件编码失败: {}", javaFile.getPath(), e);
        }
    }

    private void checkCodeStyle(CompilationUnit cu, String filePath, CodeQualityReport report) {
        // 1. 检查类名规范
        cu.getTypes().forEach(type -> {
            if (type instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) type;

                // 检查类名命名规范
                String className = clazz.getName().asString();
                if (!isValidClassName(className)) {
                    report.addViolation(new CodeQualityViolation(
                        filePath,
                        clazz.getBegin().get().line,
                        ViolationType.NAMING_VIOLATION,
                        "类名不符合规范: " + className,
                        ViolationSeverity.MEDIUM
                    ));
                }

                // 检查类行数
                int classLines = getClassLineCount(clazz);
                if (classLines > 500) {
                    report.addViolation(new CodeQualityViolation(
                        filePath,
                        clazz.getBegin().get().line,
                        ViolationType.SIZE_VIOLATION,
                        "类行数过多: " + classLines + " 行 (建议≤500行)",
                        ViolationSeverity.HIGH
                    ));
                }

                // 检查方法
                checkMethods(clazz, filePath, report);
            }
        });
    }

    private void checkMethods(ClassOrInterfaceDeclaration clazz, String filePath, CodeQualityReport report) {
        clazz.getMethods().forEach(method -> {
            // 检查方法命名规范
            String methodName = method.getName().asString();
            if (!isValidMethodName(methodName)) {
                report.addViolation(new CodeQualityViolation(
                    filePath,
                    method.getBegin().get().line,
                    ViolationType.NAMING_VIOLATION,
                    "方法名不符合规范: " + methodName,
                    ViolationSeverity.MEDIUM
                ));
            }

            // 检查方法行数
            int methodLines = getMethodLineCount(method);
            if (methodLines > 50) {
                report.addViolation(new CodeQualityViolation(
                    filePath,
                    method.getBegin().get().line,
                    ViolationType.SIZE_VIOLATION,
                    "方法行数过多: " + methodLines + " 行 (建议≤50行)",
                    ViolationSeverity.HIGH
                ));
            }

            // 检查圈复杂度
            int cyclomaticComplexity = calculateCyclomaticComplexity(method);
            if (cyclomaticComplexity > 10) {
                report.addViolation(new CodeQualityViolation(
                    filePath,
                    method.getBegin().get().line,
                    ViolationType.COMPLEXITY_VIOLATION,
                    "圈复杂度过高: " + cyclomaticComplexity + " (建议≤10)",
                    ViolationSeverity.HIGH
                ));
            }

            // 检查参数数量
            int parameterCount = method.getParameters().size();
            if (parameterCount > 5) {
                report.addViolation(new CodeQualityViolation(
                    filePath,
                    method.getBegin().get().line,
                    ViolationType.PARAMETER_VIOLATION,
                    "参数数量过多: " + parameterCount + " (建议≤5个)",
                    ViolationSeverity.MEDIUM
                ));
            }
        });
    }

    private void checkSecurityVulnerabilities(CompilationUnit cu, String filePath, CodeQualityReport report) {
        SecurityVulnerabilityScanner scanner = new SecurityVulnerabilityScanner();
        List<SecurityVulnerability> vulnerabilities = scanner.scan(cu);

        for (SecurityVulnerability vuln : vulnerabilities) {
            report.addViolation(new CodeQualityViolation(
                filePath,
                vuln.getLineNumber(),
                ViolationType.SECURITY_VIOLATION,
                vuln.getDescription(),
                ViolationSeverity.CRITICAL
            ));
        }
    }
}
```

### 安全漏洞检测器
```java
// 安全漏洞扫描器
@Component
public class SecurityVulnerabilityScanner {

    /**
     * 扫描代码中的安全漏洞
     */
    public List<SecurityVulnerability> scan(CompilationUnit cu) {
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();

        cu.accept(new VoidVisitorAdapter<List<SecurityVulnerability>>() {
            @Override
            public void visit(MethodDeclaration n, List<SecurityVulnerability> vulnerabilities) {
                super.visit(n, vulnerabilities);

                // 1. 检查SQL注入风险
                checkSQLInjection(n, vulnerabilities);

                // 2. 检查硬编码密码
                checkHardcodedPassword(n, vulnerabilities);

                // 3. 检查敏感信息泄露
                checkSensitiveInfoLeakage(n, vulnerabilities);

                // 4. 检查XSS攻击风险
                checkXSSVulnerability(n, vulnerabilities);
            }

            @Override
            public void visit(VariableDeclarator n, List<SecurityVulnerability> vulnerabilities) {
                super.visit(n, vulnerabilities);

                // 检查变量命名中的敏感信息
                checkVariableNaming(n, vulnerabilities);
            }
        }, vulnerabilities);

        return vulnerabilities;
    }

    private void checkSQLInjection(MethodDeclaration method, List<SecurityVulnerability> vulnerabilities) {
        method.getBody().ifPresent(body -> {
            body.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodCallExpr n, Void arg) {
                    super.visit(n, arg);

                    String methodName = n.getNameAsString();
                    List<String> sqlKeywords = Arrays.asList("executeQuery", "executeUpdate", "createStatement");

                    if (sqlKeywords.contains(methodName)) {
                        // 检查是否使用参数化查询
                        if (!isParameterizedQuery(n)) {
                            vulnerabilities.add(new SecurityVulnerability(
                                "SQL注入风险",
                                "方法使用了非参数化查询，存在SQL注入风险",
                                n.getBegin().get().line,
                                SecurityVulnerabilityType.SQL_INJECTION
                            ));
                        }
                    }
                }
            }, null);
        });
    }

    private void checkHardcodedPassword(MethodDeclaration method, List<SecurityVulnerability> vulnerabilities) {
        method.getBody().ifPresent(body -> {
            body.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(StringLiteralExpr n, Void arg) {
                    super.visit(n, arg);

                    String value = n.getValue();
                    if (isPasswordVariable(n) && looksLikePassword(value)) {
                        vulnerabilities.add(new SecurityVulnerability(
                            "硬编码密码",
                            "方法中包含硬编码密码，存在安全风险",
                            n.getBegin().get().line,
                            SecurityVulnerabilityType.HARDCODED_PASSWORD
                        ));
                    }
                }

                @Override
                public void visit(VariableDeclarator n, Void arg) {
                    super.visit(n, arg);

                    String variableName = n.getNameAsString();
                    if (isPasswordVariable(variableName)) {
                        // 检查变量初始化值
                        if (n.getInitializer().isPresent()) {
                            Expression initializer = n.getInitializer().get();
                            if (initializer instanceof StringLiteralExpr) {
                                StringLiteralExpr strLiteral = (StringLiteralExpr) initializer;
                                if (looksLikePassword(strLiteral.getValue())) {
                                    vulnerabilities.add(new SecurityVulnerability(
                                        "硬编码密码",
                                        "变量包含硬编码密码: " + variableName,
                                        n.getBegin().get().line,
                                        SecurityVulnerabilityType.HARDCODED_PASSWORD
                                    ));
                                }
                            }
                        }
                    }
                }
            }, null);
        });
    }

    private void checkXSSVulnerability(MethodDeclaration method, List<SecurityVulnerability> vulnerabilities) {
        method.getBody().ifPresent(body -> {
            body.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodCallExpr n, Void arg) {
                    super.visit(n, arg);

                    String methodName = n.getNameAsString();
                    if ("write".equals(methodName) || "append".equals(methodName)) {
                        // 检查是否进行了输出编码
                        if (!isOutputEncoded(n)) {
                            vulnerabilities.add(new SecurityVulnerability(
                                "XSS攻击风险",
                                "方法输出未进行编码，存在XSS攻击风险",
                                n.getBegin().get().line,
                                SecurityVulnerabilityType.XSS_VULNERABILITY
                            ));
                        }
                    }
                }
            }, null);
        });
    }

    private boolean isParameterizedQuery(MethodCallExpr n) {
        // 检查是否使用了PreparedStatement
        return n.getScope().map(scope ->
            scope.toString().contains("PreparedStatement") ||
            scope.toString().contains("ParameterizedQuery")
        ).orElse(false);
    }

    private boolean isPasswordVariable(Expression expr) {
        // 检查变量名是否包含密码相关的关键词
        String variableName = expr.toString().toLowerCase();
        return variableName.contains("password") ||
               variableName.contains("passwd") ||
               variableName.contains("pwd") ||
               variableName.contains("secret") ||
               variableName.contains("key");
    }

    private boolean looksLikePassword(String value) {
        if (value == null || value.length() < 6) {
            return false;
        }

        // 检查是否看起来像密码（包含数字、字母、特殊字符的组合）
        boolean hasLetter = value.matches(".*[a-zA-Z].*");
        boolean hasDigit = value.matches(".*\\d.*");
        boolean hasSpecialChar = value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        return hasLetter && (hasDigit || hasSpecialChar) && !value.toLowerCase().contains("test");
    }

    private boolean isOutputEncoded(MethodCallExpr n) {
        // 检查是否进行了HTML/JS编码
        return n.getScope().map(scope ->
            scope.toString().contains("escapeHtml") ||
            scope.toString().contains("escapeJavaScript") ||
            scope.toString().contains("encode")
        ).orElse(false);
    }
}
```

### 代码重构建议器
```java
// 代码重构建议生成器
@Component
public class RefactoringSuggestionGenerator {

    /**
     * 生成重构建议
     */
    public List<RefactoringSuggestion> generateSuggestions(CodeQualityReport report) {
        List<RefactoringSuggestion> suggestions = new ArrayList<>();

        // 1. 分析重复代码
        suggestions.addAll(analyzeDuplicateCode(report));

        // 2. 分析长方法
        suggestions.addAll(analyzeLongMethods(report));

        // 3. 分析大类
        suggestions.addAll(analyzeLargeClasses(report));

        // 4. 分析复杂条件
        suggestions.addAll(analyzeComplexConditions(report));

        // 5. 分析魔法数字
        suggestions.addAll(analyzeMagicNumbers(report));

        // 6. 分析异常处理
        suggestions.addAll(analyzeExceptionHandling(report));

        return suggestions;
    }

    private List<RefactoringSuggestion> analyzeDuplicateCode(CodeQualityReport report) {
        List<RefactoringSuggestion> suggestions = new ArrayList<>();

        for (CodeQualityViolation violation : report.getViolations()) {
            if (violation.getType() == ViolationType.DUPLICATE_CODE) {
                suggestions.add(RefactoringSuggestion.builder()
                    .type(RefactoringType.EXTRACT_METHOD)
                    .title("提取重复代码为方法")
                    .description("检测到重复代码块，建议提取为公共方法以减少代码重复")
                    .priority(RefactoringPriority.HIGH)
                    .filePath(violation.getFilePath())
                    .lineNumber(violation.getLineNumber())
                    .example("// 提取前\n" + violation.getOriginalCode() + "\n\n" +
                              "// 提取后\n" + generateExtractedMethodExample(violation.getOriginalCode()))
                    .benefits(Arrays.asList(
                        "减少代码重复",
                        "提高代码可维护性",
                        "降低维护成本"
                    ))
                    .build());
            }
        }

        return suggestions;
    }

    private List<RefactoringSuggestion> analyzeLongMethods(CodeQualityReport report) {
        List<RefactoringSuggestion> suggestions = new ArrayList<>();

        for (CodeQualityViolation violation : report.getViolations()) {
            if (violation.getType() == ViolationType.SIZE_VIOLATION &&
                violation.getMessage().contains("方法行数过多")) {

                suggestions.add(RefactoringSuggestion.builder()
                    .type(RefactoringType.EXTRACT_METHOD)
                    .title("拆分长方法")
                    .description("方法行数过多，建议拆分为多个小方法以提高可读性和可维护性")
                    .priority(RefactoringPriority.MEDIUM)
                    .filePath(violation.getFilePath())
                    .lineNumber(violation.getLineNumber())
                    .example("// 拆分前：长方法\n" +
                              "public void processOrder(Order order) {\n" +
                              "    // 100+ 行代码\n" +
                              "}\n\n" +
                              "// 拆分后：多个小方法\n" +
                              "public void processOrder(Order order) {\n" +
                              "    validateOrder(order);\n" +
                              "    calculatePrice(order);\n" +
                              "    saveOrder(order);\n" +
                              "    sendNotification(order);\n" +
                              "}\n")
                    .benefits(Arrays.asList(
                        "提高代码可读性",
                        "便于单元测试",
                        "降低圈复杂度"
                    ))
                    .build());
            }
        }

        return suggestions;
    }

    private List<RefactoringSuggestion> analyzeMagicNumbers(CodeQualityReport report) {
        List<RefactoringSuggestion> suggestions = new ArrayList<>();

        for (CodeQualityViolation violation : report.getViolations()) {
            if (violation.getType() == ViolationType.MAGIC_NUMBER) {
                suggestions.add(RefactoringSuggestion.builder()
                    .type(RefactoringType.INTRODUCE_CONSTANT)
                    .title("引入常量替换魔法数字")
                    .description("代码中包含魔法数字，建议定义为常量以提高代码可读性和可维护性")
                    .priority(RefactoringPriority.LOW)
                    .filePath(violation.getFilePath())
                    .lineNumber(violation.getLineNumber())
                    .example("// 修改前\n" +
                              "if (user.getAge() > 18) {\n" +
                              "    // ...\n" +
                              "}\n\n" +
                              "// 修改后\n" +
                              "public static final int ADULT_AGE = 18;\n" +
                              "if (user.getAge() > ADULT_AGE) {\n" +
                              "    // ...\n" +
                              "}\n")
                    .benefits(Arrays.asList(
                        "提高代码可读性",
                        "便于统一修改",
                        "避免硬编码"
                    ))
                    .build());
            }
        }

        return suggestions;
    }
}
```

---

## 📊 质量度量体系

### 代码质量评分模型
```java
// 代码质量评分计算器
@Component
public class CodeQualityScoreCalculator {

    /**
     * 计算代码质量评分 (0-100分)
     */
    public QualityScore calculateScore(CodeQualityReport report) {
        QualityScore score = new QualityScore();

        // 1. 基础分数计算
        double baseScore = calculateBaseScore(report);
        score.setBaseScore(baseScore);

        // 2. 规范性评分
        double standardScore = calculateStandardScore(report);
        score.setStandardScore(standardScore);

        // 3. 复杂度评分
        double complexityScore = calculateComplexityScore(report);
        score.setComplexityScore(complexityScore);

        // 4. 安全性评分
        double securityScore = calculateSecurityScore(report);
        score.setSecurityScore(securityScore);

        // 5. 可维护性评分
        double maintainabilityScore = calculateMaintainabilityScore(report);
        score.setMaintainabilityScore(maintainabilityScore);

        // 6. 综合评分
        double overallScore = (baseScore * 0.2 + standardScore * 0.3 +
                             complexityScore * 0.2 + securityScore * 0.15 +
                             maintainabilityScore * 0.15);
        score.setOverallScore(overallScore);

        // 7. 等级评定
        score.setGrade(determineGrade(overallScore));

        // 8. 改进建议
        score.setImprovementSuggestions(generateImprovementSuggestions(score));

        return score;
    }

    private double calculateBaseScore(CodeQualityReport report) {
        int totalFiles = report.getTotalFiles();
        if (totalFiles == 0) return 100.0;

        int violations = report.getViolationsBySeverity(ViolationSeverity.CRITICAL).size() +
                       report.getViolationsBySeverity(ViolationSeverity.HIGH).size();

        // 基础分数：严重违规越少，分数越高
        double violationRate = (double) violations / totalFiles;
        return Math.max(0, 100 - violationRate * 20);
    }

    private double calculateStandardScore(CodeQualityReport report) {
        int totalViolations = report.getViolations().size();
        int standardViolations = (int) report.getViolations().stream()
            .filter(v -> v.getType() == ViolationType.NAMING_VIOLATION ||
                           v.getType() == ViolationType.ENCODING_VIOLATION ||
                           v.getType() == ViolationType.FORMAT_VIOLATION)
            .count();

        if (totalViolations == 0) return 100.0;

        double standardViolationRate = (double) standardViolations / totalViolations;
        return Math.max(0, 100 - standardViolationRate * 30);
    }

    private double calculateComplexityScore(CodeQualityReport report) {
        List<CodeQualityViolation> complexityViolations = report.getViolations().stream()
            .filter(v -> v.getType() == ViolationType.COMPLEXITY_VIOLATION)
            .collect(Collectors.toList());

        if (complexityViolations.isEmpty()) return 100.0;

        // 计算平均圈复杂度
        double avgComplexity = complexityViolations.stream()
            .mapToInt(v -> extractComplexityValue(v.getMessage()))
            .average()
            .orElse(10.0);

        // 复杂度评分：平均复杂度越低，分数越高
        return Math.max(0, 100 - (avgComplexity - 1) * 8);
    }

    private double calculateSecurityScore(CodeQualityReport report) {
        int securityViolations = (int) report.getViolations().stream()
            .filter(v -> v.getType() == ViolationType.SECURITY_VIOLATION)
            .count();

        int totalFiles = report.getTotalFiles();
        if (totalFiles == 0) return 100.0;

        // 安全评分：安全违规越少，分数越高
        double securityViolationRate = (double) securityViolations / totalFiles;
        return Math.max(0, 100 - securityViolationRate * 50);
    }

    private QualityGrade determineGrade(double score) {
        if (score >= 90) return QualityGrade.EXCELLENT;
        if (score >= 80) return QualityGrade.GOOD;
        if (score >= 70) return QualityGrade.ACCEPTABLE;
        if (score >= 60) return QualityGrade.NEEDS_IMPROVEMENT;
        return QualityGrade.POOR;
    }

    private List<String> generateImprovementSuggestions(QualityScore score) {
        List<String> suggestions = new ArrayList<>();

        if (score.getStandardScore() < 80) {
            suggestions.add("加强代码规范性，遵循命名和格式标准");
        }

        if (score.getComplexityScore() < 80) {
            suggestions.add("降低方法复杂度，拆分长方法");
        }

        if (score.getSecurityScore() < 80) {
            suggestions.add("修复安全漏洞，加强输入验证和输出编码");
        }

        if (score.getMaintainabilityScore() < 80) {
            suggestions.add("减少代码重复，提高代码可维护性");
        }

        if (score.getOverallScore() < 70) {
            suggestions.add("建议进行全面的代码重构");
        }

        return suggestions;
    }
}
```

---

## 🛠️ 开发规范和最佳实践

### 编码规范检查清单
```java
// 编码规范检查工具
@Component
public class CodeStandardsChecker {

    /**
     * 编码规范检查清单
     */
    public CodeStandardsReport checkStandards(String projectPath) {
        CodeStandardsReport report = new CodeStandardsReport();

        List<File> javaFiles = scanJavaFiles(projectPath);

        for (File javaFile : javaFiles) {
            checkFileStandards(javaFile, report);
        }

        return report;
    }

    private void checkFileStandards(File javaFile, CodeStandardsReport report) {
        try {
            String content = Files.readString(javaFile.toPath());
            String[] lines = content.split("\n");

            // 1. 检查UTF-8编码
            checkUTF8Encoding(javaFile, report);

            // 2. 检查行尾空格
            checkTrailingSpaces(lines, javaFile, report);

            // 3. 检查空行规范
            checkEmptyLineRules(lines, javaFile, report);

            // 4. 检查括号规范
            checkBracketRules(lines, javaFile, report);

            // 5. 检查命名规范
            checkNamingRules(content, javaFile, report);

            // 6. 检查注释规范
            checkCommentRules(content, javaFile, report);

        } catch (IOException e) {
            log.warn("检查文件规范失败: {}", javaFile.getPath(), e);
        }
    }

    private void checkUTF8Encoding(File javaFile, CodeStandardsReport report) {
        try {
            byte[] bytes = Files.readAllBytes(javaFile.toPath());
            String encoding = detectEncoding(bytes);

            if (!"UTF-8".equals(encoding)) {
                report.addViolation(new StandardsViolation(
                    javaFile.getPath(),
                    1,
                    "文件编码必须为UTF-8，当前为: " + encoding,
                    StandardsType.ENCODING
                ));
            }
        } catch (IOException e) {
            log.warn("检测文件编码失败: {}", javaFile.getPath(), e);
        }
    }

    private void checkTrailingSpaces(String[] lines, File javaFile, CodeStandardsReport report) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.endsWith(" ") || line.endsWith("\t")) {
                report.addViolation(new StandardsViolation(
                    javaFile.getPath(),
                    i + 1,
                    "行尾包含多余的空格或制表符",
                    StandardsType.TRAILING_SPACES
                ));
            }
        }
    }

    private void checkNamingRules(String content, File javaFile, CodeStandardsReport report) {
        // 检查类名（大驼峰）
        Pattern classPattern = Pattern.compile("class\\s+([A-Z][a-zA-Z0-9]*)");
        Matcher classMatcher = classPattern.matcher(content);
        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            if (!isValidClassName(className)) {
                report.addViolation(new StandardsViolation(
                    javaFile.getPath(),
                    findLineNumber(content, classMatcher.start()),
                    "类名不符合大驼峰规范: " + className,
                    StandardsType.CLASS_NAMING
                ));
            }
        }

        // 检查方法名（小驼峰）
        Pattern methodPattern = Pattern.compile("(?:public|private|protected)\\s+(?:static\\s+)?(?:\\w+\\s+)?(\\w+)\\s*\\(");
        Matcher methodMatcher = methodPattern.matcher(content);
        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(1);
            if (!isValidMethodName(methodName)) {
                report.addViolation(new StandardsViolation(
                    javaFile.getPath(),
                    findLineNumber(content, methodMatcher.start()),
                    "方法名不符合小驼峰规范: " + methodName,
                    StandardsType.METHOD_NAMING
                ));
            }
        }
    }

    private boolean isValidClassName(String className) {
        return className.matches("^[A-Z][a-zA-Z0-9]*$");
    }

    private boolean isValidMethodName(String methodName) {
        return methodName.matches("^[a-z][a-zA-Z0-9]*$");
    }

    private int findLineNumber(String content, int position) {
        String beforePosition = content.substring(0, position);
        return beforePosition.split("\n").length;
    }
}
```

---

## 🔗 相关文档参考

### 核心架构文档
- **📋 CLAUDE.md**: 全局架构规范 (强制遵循)
- **🏗️ 四层架构详解**: Controller→Service→Manager→DAO架构模式
- **🔧 依赖注入规范**: 统一使用@Resource注解
- **📦 DAO层规范**: 统一使用Dao后缀和@Mapper注解

### 质量保障文档
- **📊 代码质量规范**: 企业级代码编写规范
- **🔍 代码审查清单**: 代码审查标准和检查清单
- **🛠️ 重构最佳实践**: 代码重构指导原则
- **📈 性能优化指南**: 代码性能优化建议

### 开发工具文档
- **Checkstyle**: 代码风格检查工具
- **PMD**: 代码质量分析工具
- **FindBugs/SpotBugs**: 缺陷检测工具
- **SonarQube**: 代码质量分析平台

---

**📋 重要提醒**:
1. 本技能严格守护IOE-DREAM代码质量标准
2. 所有代码必须符合企业级质量要求
3. 持续进行代码质量监控和改进
4. 定期进行代码审查和重构
5. 建立质量度量和评分体系
6. 重视安全漏洞修复和预防
7. 保持测试覆盖率和质量

**让我们一起建设高质量、高标准的代码体系！** 🚀

---
**文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
**创建时间**: 2025-12-08
**最后更新**: 2025-12-08
**技能等级**: ★★★★★★ (顶级专家)
**适用架构**: 企业级Java代码质量保障体系