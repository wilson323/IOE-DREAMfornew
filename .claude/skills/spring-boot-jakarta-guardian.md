# Spring Boot Jakarta EE 守护专家技能
## Spring Boot Jakarta Guardian

**🎯 技能定位**: IOE-DREAM智慧园区Spring Boot 3.5.8 + Jakarta EE 3.0+技术栈守护专家，确保项目完全符合Jakarta EE规范，预防编译错误和技术栈不兼容问题

**⚡ 技能等级**: ★★★★★★ (顶级专家)
**🎯 适用场景**: Jakarta EE迁移、技术栈升级、依赖管理、编译错误修复、版本兼容性检查、包结构优化
**📊 技能覆盖**: 包名迁移 | 依赖检查 | 编译验证 | 版本兼容 | 技术栈升级 | 错误修复 | 包结构规范

---

## 📋 技能概述

### **核心专长**
- **Jakarta EE 3.0+规范**: 完整的Jakarta EE包名体系理解和应用
- **javax到jakarta迁移**: 系统性的javax包名替换为jakarta包名
- **Spring Boot 3.5.8适配**: Spring Boot 3.5.8与Jakarta EE的兼容性管理
- **依赖版本管理**: 确保所有依赖版本兼容且符合技术栈要求
- **编译错误预防**: 预防和解决因包名不匹配导致的编译问题
- **技术栈升级指导**: 安全的技术栈升级路径和最佳实践
- **技术栈统一检查**: 强制执行统一技术栈规范，禁止违规技术栈

### **解决能力**
- **Jakarta EE合规检查**: 全面检查项目Jakarta EE合规性
- **包名迁移执行**: 系统性执行javax到jakarta的包名迁移
- **编译问题修复**: 解决因包名问题导致的编译和运行时错误
- **技术栈冲突解决**: 解决技术栈版本冲突和兼容性问题
- **升级风险评估**: 评估技术栈升级的风险和影响
- **最佳实践指导**: 提供Jakarta EE迁移和使用的最佳实践
- **技术栈违规检测**: 检测和修复技术栈规范违规问题

---

## 🎯 Jakarta EE 3.0+ 包名映射体系

### 📋 核心包名映射表
| javax包名 | jakarta包名 | 说明 | 影响范围 |
|---------|-------------|------|----------|
| `javax.annotation.Resource` | `jakarta.annotation.Resource` | 依赖注入注解 | 全局依赖注入 |
| `javax.annotation.PostConstruct` | `jakarta.annotation.PostConstruct` | 生命周期注解 | 全局生命周期管理 |
| `javax.annotation.PreDestroy` | `jakarta.annotation.PreDestroy` | 生命周期注解 | 全局生命周期管理 |
| `javax.validation.Valid` | `jakarta.validation.Valid` | 参数验证注解 | 全局参数验证 |
| `javax.validation.constraints.*` | `jakarta.validation.constraints.*` | 验证约束注解 | 全局数据验证 |
| `javax.persistence.Entity` | `jakarta.persistence.Entity` | JPA实体注解 | 数据层实体定义 |
| `javax.persistence.Table` | `jakarta.persistence.Table` | JPA表注解 | 数据层表映射 |
| `javax.persistence.Column` | `jakarta.persistence.Column` | JPA字段注解 | 数据层字段映射 |
| `javax.persistence.Id` | `jakarta.persistence.Id` | JPA主键注解 | 数据层主键定义 |
| `javax.transaction.Transactional` | `jakarta.transaction.Transactional` | 事务注解 | 全局事务管理 |
| `javax.servlet.http.*` | `jakarta.servlet.http.*` | Servlet API | Web层接口 |
| `javax.ejb.*` | `jakarta.ejb.*` | EJB API | 企业级组件 |
| `javax.jms.*` | `jakarta.jms.*` | JMS API | 消息队列 |
| `javax.mail.*` | `jakarta.mail.*` | Mail API | 邮件服务 |

### 🔍 包名迁移检测工具
```java
// Jakarta EE包名迁移检测和修复工具
@Component
@Slf4j
public class JakartaMigrationTool {

    private final Map<String, String> JAKARTA_MAPPINGS = Map.of(
        // Annotation
        "javax.annotation.Resource", "jakarta.annotation.Resource",
        "javax.annotation.PostConstruct", "jakarta.annotation.PostConstruct",
        "javax.annotation.PreDestroy", "jakarta.annotation.PreDestroy",
        "javax.annotation.security.RolesAllowed", "jakarta.annotation.security.RolesAllowed",
        "javax.annotation.security.PermitAll", "jakarta.annotation.security.PermitAll",
        "javax.annotation.security.DenyAll", "jakarta.annotation.security.DenyAll",

        // Validation
        "javax.validation.Valid", "jakarta.validation.Valid",
        "javax.validation.constraints", "jakarta.validation.constraints",
        "javax.validation.Constraint", "jakarta.validation.Constraint",
        "javax.validation.Payload", "jakarta.validation.Payload",

        // Persistence (JPA)
        "javax.persistence.Entity", "jakarta.persistence.Entity",
        "javax.persistence.Table", "jakarta.persistence.Table",
        "javax.persistence.Column", "jakarta.persistence.Column",
        "javax.persistence.Id", "jakarta.persistence.Id",
        "javax.persistence.GeneratedValue", "jakarta.persistence.GeneratedValue",
        "javax.persistence.GenerationType", "jakarta.persistence.GenerationType",
        "javax.persistence.ManyToOne", "jakarta.persistence.ManyToOne",
        "javax.persistence.OneToMany", "jakarta.persistence.OneToMany",
        "javax.persistence.OneToOne", "jakarta.persistence.OneToOne",
        "javax.persistence.ManyToMany", "jakarta.persistence.ManyToMany",
        "javax.persistence.JoinColumn", "jakarta.persistence.JoinColumn",
        "javax.persistence.JoinTable", "jakarta.persistence.JoinTable",
        "javax.persistence.EntityManager", "jakarta.persistence.EntityManager",
        "javax.persistence.PersistenceContext", "jakarta.persistence.PersistenceContext",
        "javax.persistence.Query", "jakarta.persistence.Query",
        "javax.persistence.TypedQuery", "jakarta.persistence.TypedQuery",
        "javax.persistence.criteria", "jakarta.persistence.criteria",

        // Transaction
        "javax.transaction.Transactional", "jakarta.transaction.Transactional",
        "javax.transaction.Transactional.TxType", "jakarta.transaction.Transactional.TxType",
        "javax.transaction.TransactionalPropagation", "jakarta.transaction.TransactionalPropagation",
        "javax.transaction.TransactionalIsolation", "jakarta.transaction.TransactionalIsolation",

        // Servlet
        "javax.servlet.http.HttpServletRequest", "jakarta.servlet.http.HttpServletRequest",
        "javax.servlet.http.HttpServletResponse", "jakarta.servlet.http.HttpServletResponse",
        "javax.servlet.http.HttpSession", "jakarta.servlet.http.HttpSession",
        "javax.servlet.ServletException", "jakarta.servlet.ServletException",
        "javax.servlet.Filter", "jakarta.servlet.Filter",
        "javax.servlet.FilterChain", "jakarta.servlet.FilterChain",
        "javax.servlet.ServletContext", "jakarta.servlet.ServletContext",
        "javax.servlet.ServletConfig", "jakarta.servlet.ServletConfig",

        // EJB (if applicable)
        "javax.ejb.Stateless", "jakarta.ejb.Stateless",
        "javax.ejb.Stateful", "jakarta.ejb.Stateful",
        "javax.ejb.Singleton", "jakarta.ejb.Singleton",
        "javax.ejb.Local", "jakarta.ejb.Local",
        "javax.ejb.Remote", "jakarta.ejb.Remote",
        "javax.ejb.EJB", "jakarta.ejb.EJB",

        // JMS (if applicable)
        "javax.jms.ConnectionFactory", "jakarta.jms.ConnectionFactory",
        "javax.jms.Queue", "jakarta.jms.Queue",
        "javax.jms.Topic", "jakarta.jms.Topic",
        "javax.jms.Message", "jakarta.jms.Message",
        "javax.jms.Session", "jakarta.jms.Session",

        // Mail (if applicable)
        "javax.mail.Session", "jakarta.mail.Session",
        "javax.mail.Message", "jakarta.mail.Message",
        "javax.mail.Transport", "jakarta.mail.Transport",
        "javax.mail.internet.MimeMessage", "jakarta.mail.internet.MimeMessage"
    );

    /**
     * 扫描项目中的javax包名使用情况
     */
    public JakartaMigrationReport scanProject(String projectPath) {
        JakartaMigrationReport report = new JakartaMigrationReport();

        log.info("开始扫描Jakarta EE迁移需求: projectPath={}", projectPath);

        try {
            // 1. 扫描所有Java文件
            List<File> javaFiles = scanJavaFiles(projectPath);
            log.info("发现Java文件数量: {}", javaFiles.size());

            // 2. 检查每个文件的javax使用情况
            for (File javaFile : javaFiles) {
                scanFileForJakartaMigration(javaFile, report);
            }

            // 3. 扫描配置文件
            scanConfigurationFiles(projectPath, report);

            // 4. 扫描Maven依赖
            scanMavenDependencies(projectPath, report);

            // 5. 生成迁移计划
            generateMigrationPlan(report);

            log.info("Jakarta EE迁移扫描完成: 发现{}个需要迁移的项目", report.getTotalMigrationItems());

        } catch (Exception e) {
            log.error("Jakarta EE迁移扫描失败", e);
            report.addError("SCAN_FAILED", "扫描项目失败: " + e.getMessage());
        }

        return report;
    }

    private void scanFileForJakartaMigration(File javaFile, JakartaMigrationReport report) {
        try {
            String filePath = javaFile.getAbsolutePath();
            String content = Files.readString(javaFile.toPath());
            String[] lines = content.split("\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // 检查import语句
                if (line.startsWith("import javax.")) {
                    String javaxImport = line.substring(7); // 移除"import "

                    if (JAKARTA_MAPPINGS.containsKey(javaxImport)) {
                        String jakartaImport = JAKARTA_MAPPINGS.get(javaxImport);

                        report.addMigrationItem(new JakartaMigrationItem(
                            filePath,
                            i + 1,
                            MigrationType.IMPORT_REPLACEMENT,
                            javaxImport,
                            jakartaImport,
                            "需要将javax import替换为jakarta import",
                            MigrationPriority.HIGH
                        ));
                    }
                }

                // 检查代码中的完全限定名使用
                checkFullyQualifiedNames(line, filePath, i + 1, report);

                // 检查注解使用
                checkAnnotationUsage(line, filePath, i + 1, report);
            }

        } catch (IOException e) {
            log.warn("扫描文件失败: {}", javaFile.getPath(), e);
        }
    }

    private void checkFullyQualifiedNames(String line, String filePath, int lineNumber, JakartaMigrationReport report) {
        // 检查代码中的javax完全限定名使用
        for (String javaxPackage : JAKARTA_MAPPINGS.keySet()) {
            if (line.contains(javaxPackage) && !line.trim().startsWith("import ")) {
                String jakartaPackage = JAKARTA_MAPPINGS.get(javaxPackage);

                report.addMigrationItem(new JakartaMigrationItem(
                    filePath,
                    lineNumber,
                    MigrationType.FULLY_QUALIFIED_NAME_REPLACEMENT,
                    javaxPackage,
                    jakartaPackage,
                    "需要将javax完全限定名替换为jakarta完全限定名",
                    MigrationPriority.HIGH
                ));
            }
        }
    }

    private void checkAnnotationUsage(String line, String filePath, int lineNumber, JakartaMigrationReport report) {
        // 检查注解使用，特别是没有import的情况
        Pattern annotationPattern = Pattern.compile("@(\\w+)\\(");
        Matcher matcher = annotationPattern.matcher(line);

        while (matcher.find()) {
            String annotationName = matcher.group(1);

            // 检查是否是javax包下的注解
            String potentialJakartaClass = "javax.annotation." + annotationName;
            if (JAKARTA_MAPPINGS.containsKey(potentialJakartaClass)) {
                String jakartaClass = JAKARTA_MAPPINGS.get(potentialJakartaClass);

                report.addMigrationItem(new JakartaMigrationItem(
                    filePath,
                    lineNumber,
                    MigrationType.ANNOTATION_REPLACEMENT,
                    annotationName,
                    annotationName + " (需要import jakarta.annotation." + annotationName + ")",
                    "需要更新注解的import语句",
                    MigrationPriority.HIGH
                ));
            }
        }
    }

    /**
     * 执行自动迁移
     */
    public MigrationResult executeMigration(JakartaMigrationReport report, String projectPath) {
        MigrationResult result = new MigrationResult();

        log.info("开始执行Jakarta EE自动迁移");

        try {
            // 1. 备份原始文件
            backupOriginalFiles(report, projectPath);

            // 2. 执行Java文件迁移
            List<String> migratedFiles = migrateJavaFiles(report, projectPath);
            result.setMigratedJavaFiles(migratedFiles);

            // 3. 执行配置文件迁移
            List<String> migratedConfigFiles = migrateConfigurationFiles(report, projectPath);
            result.setMigratedConfigFiles(migratedConfigFiles);

            // 4. 更新Maven依赖
            boolean mavenUpdated = updateMavenDependencies(report, projectPath);
            result.setMavenDependenciesUpdated(mavenUpdated);

            // 5. 验证迁移结果
            boolean validationResult = validateMigrationResult(report, projectPath);
            result.setValidationSuccessful(validationResult);

            log.info("Jakarta EE自动迁移完成: 迁移了{}个Java文件, {}个配置文件, Maven更新: {}",
                migratedFiles.size(), migratedConfigFiles.size(), mavenUpdated);

        } catch (Exception e) {
            log.error("Jakarta EE自动迁移失败", e);
            result.addError("MIGRATION_FAILED", "迁移执行失败: " + e.getMessage());
        }

        return result;
    }

    private List<String> migrateJavaFiles(JakartaMigrationReport report, String projectPath) {
        List<String> migratedFiles = new ArrayList<>();
        Map<String, List<JakartaMigrationItem>> itemsByFile = report.getMigrationItemsByFile();

        for (Map.Entry<String, List<JakartaMigrationItem>> entry : itemsByFile.entrySet()) {
            String filePath = entry.getKey();
            List<JakartaMigrationItem> items = entry.getValue();

            if (items.isEmpty()) continue;

            try {
                Path fullPath = Paths.get(projectPath, filePath);
                String content = Files.readString(fullPath);
                String originalContent = content;

                // 执行所有迁移项目
                for (JakartaMigrationItem item : items) {
                    switch (item.getMigrationType()) {
                        case IMPORT_REPLACEMENT:
                            content = content.replace("import " + item.getJavaxPackage(),
                                                 "import " + item.getJakartaPackage());
                            break;
                        case FULLY_QUALIFIED_NAME_REPLACEMENT:
                            content = content.replace(item.getJavaxPackage(), item.getJakartaPackage());
                            break;
                        case ANNOTATION_REPLACEMENT:
                            // 需要添加import语句
                            content = addJakartaImport(content, item);
                            break;
                    }
                }

                // 只有当内容发生变化时才写入文件
                if (!content.equals(originalContent)) {
                    Files.writeString(fullPath, content);
                    migratedFiles.add(filePath);
                    log.info("已迁移文件: {}", filePath);
                }

            } catch (IOException e) {
                log.error("迁移文件失败: {}", filePath, e);
            }
        }

        return migratedFiles;
    }

    private String addJakartaImport(String content, JakartaMigrationItem item) {
        String[] lines = content.split("\n");
        List<String> newLines = new ArrayList<>(Arrays.asList(lines));

        // 找到import语句区域
        int lastImportIndex = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith("import ")) {
                lastImportIndex = i;
            } else if (lines[i].trim().isEmpty() && lastImportIndex >= 0) {
                // 遇到import后的第一个空行，在此处插入新的import
                break;
            }
        }

        if (lastImportIndex >= 0) {
            String jakartaImport = "import " + item.getJakartaPackage() + ";";
            newLines.add(lastImportIndex + 1, jakartaImport);
        }

        return String.join("\n", newLines);
    }

    /**
     * 验证Spring Boot 3.5.8兼容性
     */
    public SpringBootCompatibilityReport checkSpringBootCompatibility(String projectPath) {
        SpringBootCompatibilityReport report = new SpringBootCompatibilityReport();

        log.info("检查Spring Boot 3.5.8兼容性");

        try {
            // 1. 检查Spring Boot版本
            checkSpringBootVersion(projectPath, report);

            // 2. 检查Java版本
            checkJavaVersion(projectPath, report);

            // 3. 检查关键依赖版本
            checkKeyDependencies(projectPath, report);

            // 4. 检查配置文件兼容性
            checkConfigurationCompatibility(projectPath, report);

            // 5. 检查已知的兼容性问题
            checkKnownCompatibilityIssues(projectPath, report);

        } catch (Exception e) {
            log.error("Spring Boot兼容性检查失败", e);
            report.addError("COMPATIBILITY_CHECK_FAILED", "兼容性检查失败: " + e.getMessage());
        }

        return report;
    }

    private void checkSpringBootVersion(String projectPath, SpringBootCompatibilityReport report) {
        Path pomPath = Paths.get(projectPath, "pom.xml");
        if (Files.exists(pomPath)) {
            try {
                String pomContent = Files.readString(pomPath);

                // 检查Spring Boot版本
                Pattern versionPattern = Pattern.compile("<spring-boot.version>([^<]+)</spring-boot.version>");
                Matcher matcher = versionPattern.matcher(pomContent);

                if (matcher.find()) {
                    String version = matcher.group(1).trim();

                    if (!version.startsWith("3.5")) {
                        report.addCompatibilityIssue(new CompatibilityIssue(
                            "SPRING_BOOT_VERSION",
                            "Spring Boot版本不匹配，当前: " + version + ", 需要: 3.5.8",
                            CompatibilityPriority.HIGH,
                            "更新spring-boot-starter-parent或spring-boot-dependencies版本"
                        ));
                    }
                }

            } catch (IOException e) {
                log.warn("读取pom.xml失败", e);
            }
        }
    }
}
```

### 🔧 依赖版本管理工具
```java
// Spring Boot依赖版本管理工具
@Component
@Slf4j
public class DependencyVersionManager {

    /**
     * Spring Boot 3.5.8 推荐的依赖版本
     */
    private static final Map<String, String> RECOMMENDED_VERSIONS = Map.of(
        // Spring Boot
        "org.springframework.boot", "3.5.8",
        "spring-boot-starter-parent", "3.5.8",
        "spring-boot-dependencies", "3.5.8",

        // Spring Framework
        "org.springframework", "6.2.0",
        "spring-core", "6.2.0",
        "spring-context", "6.2.0",
        "spring-web", "6.2.0",
        "spring-webmvc", "6.2.0",
        "spring-data-jpa", "3.3.0",

        // Jakarta EE
        "jakarta.validation", "3.1.0",
        "jakarta.persistence", "3.2.0",
        "jakarta.transaction", "2.1.1",
        "jakarta.servlet", "6.1.0",
        "jakarta.annotation", "2.1.1",

        // Database
        "mysql-connector-java", "8.4.0",
        "com.h2database", "2.3.232",
        "org.postgresql", "42.7.4",

        // MyBatis
        "org.mybatis.spring.boot", "3.0.3",
        "org.mybatis", "3.5.16",
        "com.github.pagehelper", "pagehelper-spring-boot-starter", "2.1.0",

        // Spring Cloud
        "org.springframework.cloud", "2023.0.4",
        "spring-cloud-dependencies", "2023.0.4",
        "org.springframework.cloud.alibaba", "2022.0.0.0",

        // Redis
        "org.springframework.boot", "spring-boot-starter-data-redis", "3.5.8",
        "org.apache.commons", "commons-pool2", "2.12.0",

        // Lombok
        "org.projectlombok", "1.18.34",

        // Testing
        "org.junit.jupiter", "5.11.3",
        "org.mockito", "5.12.0",
        "org.springframework.boot", "spring-boot-starter-test", "3.5.8",

        // Other
        "com.alibaba", "druid-spring-boot-starter", "1.2.20",
        "com.alibaba", "fastjson2", "2.0.53",
        "org.apache.shiro", "shiro-spring-boot-starter", "1.13.0"
    );

    /**
     * 检查并更新依赖版本
     */
    public DependencyUpdateResult checkAndUpdateDependencies(String projectPath) {
        DependencyUpdateResult result = new DependencyUpdateResult();

        log.info("检查并更新依赖版本");

        try {
            Path pomPath = Paths.get(projectPath, "pom.xml");
            if (!Files.exists(pomPath)) {
                result.addError("POM_NOT_FOUND", "未找到pom.xml文件");
                return result;
            }

            String pomContent = Files.readString(pomPath);

            // 解析Maven POM
            MavenProject mavenProject = parseMavenPom(pomPath);

            // 检查依赖版本
            List<DependencyVersionIssue> issues = checkDependencyVersions(mavenProject);
            result.setVersionIssues(issues);

            // 更新版本（如果有问题）
            if (!issues.isEmpty()) {
                String updatedPomContent = updateDependencyVersions(pomContent, issues);
                Files.writeString(pomPath, updatedPomContent);
                result.setUpdated(true);
                result.setUpdatedDependencies(issues.size());
                log.info("已更新{}个依赖版本", issues.size());
            }

            // 验证更新结果
            boolean validationResult = validateUpdatedDependencies(Paths.get(projectPath, "pom.xml"));
            result.setValidationSuccessful(validationResult);

        } catch (Exception e) {
            log.error("依赖版本检查更新失败", e);
            result.addError("DEPENDENCY_UPDATE_FAILED", "依赖版本更新失败: " + e.getMessage());
        }

        return result;
    }

    private List<DependencyVersionIssue> checkDependencyVersions(MavenProject mavenProject) {
        List<DependencyVersionIssue> issues = new ArrayList<>();

        // 检查项目依赖
        for (Dependency dependency : mavenProject.getDependencies()) {
            String groupId = dependency.getGroupId();
            String artifactId = dependency.getArtifactId();
            String version = dependency.getVersion();

            String recommendedVersion = RECOMMENDED_VERSIONS.get(groupId);
            if (recommendedVersion == null) {
                recommendedVersion = RECOMMENDED_VERSIONS.get(artifactId);
            }

            if (recommendedVersion != null && !recommendedVersion.equals(version)) {
                issues.add(new DependencyVersionIssue(
                    groupId,
                    artifactId,
                    version,
                    recommendedVersion,
                    getVersionPriorityDifference(version, recommendedVersion)
                ));
            }
        }

        return issues;
    }

    private String updateDependencyVersions(String pomContent, List<DependencyVersionIssue> issues) {
        String updatedContent = pomContent;

        for (DependencyVersionIssue issue : issues) {
            // 更新依赖版本
            String oldVersionPattern = String.format(
                "<%s>%s</%s>",
                issue.getArtifactId(),
                issue.getCurrentVersion(),
                issue.getArtifactId()
            );

            String newVersionPattern = String.format(
                "<%s>%s</%s>",
                issue.getArtifactId(),
                issue.getRecommendedVersion(),
                issue.getArtifactId()
            );

            updatedContent = updatedContent.replace(oldVersionPattern, newVersionPattern);
        }

        return updatedContent;
    }

    private VersionPriorityDifference getVersionPriorityDifference(String currentVersion, String recommendedVersion) {
        try {
            Version current = new Version(currentVersion);
            Version recommended = new Version(recommendedVersion);

            int majorDiff = current.getMajor() - recommended.getMajor();
            int minorDiff = current.getMinor() - recommended.getMinor();

            if (Math.abs(majorDiff) >= 1) {
                return VersionPriorityDifference.MAJOR;
            } else if (Math.abs(minorDiff) >= 2) {
                return VersionPriorityDifference.MINOR;
            } else {
                return VersionPriorityDifference.PATCH;
            }
        } catch (Exception e) {
            return VersionPriorityDifference.UNKNOWN;
        }
    }

    /**
     * 生成Spring Boot 3.5.8依赖配置模板
     */
    public String generateDependencyTemplate() {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.8</version>
        <relativePath/>
    </parent>

    <groupId>net.lab1024.sa</groupId>
    <artifactId>ioedream-common-service</artifactId>
    <version>1.0.0</version>
    <name>ioedream-common-service</name>
    <description>IOE-DREAM公共服务微服务</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <!-- Spring Boot相关版本 -->
        <spring-cloud.version>2023.0.4</spring-cloud.version>
        <spring-cloud-alibaba.version>2022.0.0.0</spring-cloud-alibaba.version>

        <!-- 数据库相关版本 -->
        <mysql-connector.version>8.4.0</mysql-connector.version>
        <druid.version>1.2.20</druid.version>

        <!-- MyBatis相关版本 -->
        <mybatis-spring-boot.version>3.0.3</mybatis-spring-boot.version>
        <mybatis.version>3.5.16</mybatis.version>
        <pagehelper.version>2.1.0</pagehelper.version>

        <!-- 工具库版本 -->
        <lombok.version>1.18.34</lombok.version>
        <fastjson2.version>2.0.53</fastjson2.version>
        <guava.version>33.3.1-grotham</guava.version>

        <!-- 其他版本 -->
        <shiro.version>1.13.0</shiro.version>
        <jwt.version>4.5.0</jwt.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- 数据库相关 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>${mysql-connector.version}</version>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>

        <!-- MyBatis相关 -->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>${mybatis-spring-boot.version}</version>
        </dependency>

        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper-spring-boot-starter</artifactId>
            <version>${pagehelper.version}</version>
        </dependency>

        <!-- Spring Cloud相关 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>

        <!-- 工具库 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.alibaba.fastjson2</groupId>
            <artifactId>fastjson2</artifactId>
            <version>${fastjson2.version}</version>
        </dependency>

        <!-- 安全相关 -->
        <dependency>
            <groupId>org.apache.shiro</groupId>
            <artifactId>shiro-spring-boot-starter</artifactId>
            <version>${shiro.version}</version>
        </dependency>

        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jwt.version}</version>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

</project>
""";
    }
}
```

---

## 📊 Jakarta EE迁移质量指标

### 核心质量指标
| 指标名称 | 目标值 | 说明 | 检查方式 |
|---------|--------|------|----------|
| **Jakarta EE合规率** | 100% | 使用jakarta包名的比例 | 包名合规检查 |
| **javax使用率** | 0% | 遗留javax包名的比例 | 遗留检查 |
| **编译成功率** | 100% | 编译无错误的比例 | 编译验证 |
| **运行时兼容性** | 100% | 运行无异常的比例 | 兼容性测试 |
| **依赖版本合规率** | 100% | 使用推荐版本的比例 | 依赖版本检查 |

### 迁移质量指标
| 指标名称 | 目标值 | 说明 | 检查方式 |
|---------|--------|------|----------|
| **迁移完成率** | 100% | 需要迁移的项目完成比例 | 迁移进度检查 |
| **迁移准确率** | 100% | 迁移转换准确的比例 | 迁移准确性验证 |
| **备份完整性** | 100% | 原始文件备份完整性 | 备份验证 |
| **回滚成功率** | 100% | 回滚操作成功率 | 回滚机制测试 |

### 版本管理
- **主版本**: v1.0.0 - 初始版本
- **文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
- **创建时间**: 2025-12-08
- **最后更新**: 2025-12-08
- **变更类型**: MAJOR - 新技术栈守护技能

---

## 🛠️ 开发规范和最佳实践

### Jakarta EE迁移最佳实践
```java
// ✅ 正确的Jakarta EE使用示例 - 强制技术栈规范
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Resource  // 🔴 强制：使用jakarta.annotation.Resource，禁止javax.annotation.Resource
    private UserService userService;

    @PostMapping("/create")
    public ResponseDTO<UserVO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        // 🔴 强制：@Valid 来自 jakarta.validation.Valid，禁止javax.validation.Valid
        UserVO user = userService.createUser(request);
        return ResponseDTO.ok(user);
    }
}

@Service
@Transactional(rollbackFor = Exception.class)  // 🔴 强制：使用jakarta.transaction.Transactional
public class UserServiceImpl implements UserService {

    @Resource  // 🔴 强制：使用jakarta.annotation.Resource，禁止javax.annotation.Resource
    private UserDao userDao;

    @Override
    @Transactional(readOnly = true)  // 只读事务
    public UserDetailVO getUserDetail(Long userId) {
        UserEntity user = userDao.selectById(userId);
        return convertToUserVO(user);
    }

    // ❌ 严格禁止示例
    // 1. ❌ 禁止使用javax包名下的任何注解
    // 2. ❌ 禁止import javax.annotation.*
    // 3. ❌ 禁止import javax.validation.*
    // 4. ❌ 禁止import javax.transaction.*
    // 5. ❌ 禁止import javax.persistence.*
    // 6. ❌ 禁止import javax.servlet.*
}

@Mapper  // 🔴 强制：使用MyBatis注解，不需要Jakarta相关注解
public interface UserDao extends BaseMapper<UserEntity> {

    @Select("SELECT * FROM t_user WHERE status = 1")
    List<UserEntity> selectActiveUsers();
}

// ❌ 严重错误示例 - 技术栈违规
@RestController
public class BadController {
    @Autowired  // 🔴 严重违规：必须使用@Resource
    private UserService service;

    @PostMapping("/create")
    public ResponseDTO<UserVO> create(@javax.validation.Valid @RequestBody CreateUserRequestDTO request) {
        // 🔴 严重违规：必须使用jakarta.validation.Valid
        return service.create(request);
    }
}
```

### 配置文件更新示例
```yaml
# application.yml - Spring Boot 3.5.8 配置
spring:
  application:
    name: ioedream-common-service

  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/smart_admin_v3?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root1234

  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 3000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

# 使用Jakarta Validation
validation:
  enabled: true
```

### Maven依赖管理最佳实践
```xml
<!-- 推荐的Spring Boot 3.5.8依赖配置 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.8</version>
    <relativePath/>
</parent>

<dependencies>
    <!-- 自动使用Jakarta EE依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- 不要再手动添加jakarta依赖，Spring Boot会自动管理 -->
</dependencies>
```

---

## 🔗 相关文档参考

### 核心架构文档
- **📋 CLAUDE.md**: 全局架构规范 (强制遵循)
- **🏗️ 四层架构详解**: Controller→Service→Manager→DAO架构模式
- **🔧 依赖注入规范**: 统一使用@Resource注解
- **📦 DAO层规范**: 统一使用Dao后缀和@Mapper注解

### Jakarta EE文档
- **Jakarta EE 10.0官方文档**: Jakarta EE规范文档
- **Spring Boot 3.5.8官方文档**: Spring Boot最新版本文档
- **Spring Framework 6.2文档**: Spring Framework核心文档

### 迁移指南文档
- **Spring Boot 3.0迁移指南**: 官方迁移指导
- **JDK 8到17迁移指南**: Java版本升级指南
- **Jakarta EE官方迁移指南**: Jakarta EE迁移最佳实践

---

**📋 重要提醒**:
1. 本技能严格守护IOE-DREAM的Spring Boot 3.5.8 + Jakarta EE 3.0+技术栈
2. 所有代码必须100%使用jakarta包名，禁止javax包名
3. 依赖版本必须符合Spring Boot 3.5.8兼容性要求
4. 定期检查技术栈版本和兼容性问题
5. 在升级前进行充分的测试和验证
6. 保持完整的迁移记录和回滚机制

**让我们一起建设现代化的Jakarta EE技术栈！** 🚀

---
**文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
**创建时间**: 2025-12-08
**最后更新**: 2025-12-08
**技能等级**: ★★★★★★ (顶级专家)
**适用架构**: Spring Boot 3.5.8 + Jakarta EE 3.0+