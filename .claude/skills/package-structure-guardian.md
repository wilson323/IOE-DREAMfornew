# 包目录结构守护专家技能
## Package Structure Guardian

**🎯 技能定位**: IOE-DREAM智慧园区包目录结构守护专家，严格确保包结构规范执行，防止重复包名、Entity分散存储等包结构违规问题

**⚡ 技能等级**: ★★★★★ (顶级专家)
**🎯 适用场景**: 包结构合规检查、代码重构指导、规范培训、包结构优化、违规修复
**📊 技能覆盖**: 包结构验证 | 重复包名检查 | Entity管理 | 包命名规范 | 重构指导

**重要更新（2025-01-15）**: 基于全局包目录结构分析，新增包结构守护能力，专门解决重复包名和Entity分散存储问题。

---

## 📋 技能概述

### **核心专长**
- **重复包名检测**: 严格检测`access.access.entity`、`consume.consume.entity`等冗余命名
- **Entity统一管理**: 确保所有Entity在公共模块统一管理，防止分散存储
- **包结构规范验证**: 验证包结构是否符合统一标准
- **Manager层规范检查**: 确保Manager使用纯Java类设计
- **包命名规范**: 检查包命名的规范性和一致性
- **自动化重构指导**: 提供包结构重构的具体指导

### **解决能力**
- **包结构违规预防**: 在开发阶段预防包结构违规
- **重复包名修复**: 系统性修复重复包名问题
- **Entity迁移指导**: 指导Entity统一迁移到公共模块
- **包结构标准化**: 推动包结构标准化实施
- **重构方案制定**: 制定详细的包结构重构方案
- **质量保障体系**: 建立包结构质量保障机制

---

## 🎯 包目录结构规范详解

### 📋 统一业务微服务包结构

**标准包结构模板**:
```java
net.lab1024.sa.{service}/
├── config/                   # 配置类
│   ├── DatabaseConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── controller/              # REST控制器
│   ├── {Module}Controller.java
│   └── support/             # 支撑控制器
├── service/                 # 服务接口和实现
│   ├── {Module}Service.java
│   └── impl/
│       └── {Module}ServiceImpl.java
├── manager/                 # 业务编排层
│   ├── {Module}Manager.java
│   └── impl/
│       └── {Module}ManagerImpl.java
├── dao/                     # 数据访问层
│   ├── {Module}Dao.java
│   └── custom/              # 自定义查询
├── domain/                  # 领域对象
│   ├── form/               # 请求表单
│   │   ├── {Module}AddForm.java
│   │   ├── {Module}UpdateForm.java
│   │   └── {Module}QueryForm.java
│   └── vo/                 # 响应视图
│       ├── {Module}VO.java
│       ├── {Module}DetailVO.java
│       └── {Module}ListVO.java
└── {Service}Application.java
```

### 📋 公共模块包结构

**公共模块标准结构**:
```java
net.lab1024.sa.common/
├── core/                    # 核心模块（最小稳定内核，尽量纯 Java）
│   ├── domain/             # 通用领域对象
│   ├── entity/             # 基础实体
│   ├── config/             # 核心配置
│   └── util/               # 核心工具
├── auth/                    # 认证授权
│   ├── entity/
│   ├── dao/
│   ├── service/
│   ├── manager/
│   └── domain/
├── organization/            # 组织架构
│   ├── entity/             # User, Department, Area, Device
│   ├── dao/
│   ├── service/
│   ├── manager/
│   └── domain/
├── dict/                    # 字典管理
├── menu/                    # 菜单管理
├── notification/           # 通知推送
├── scheduler/              # 定时任务
├── audit/                   # 审计日志
└── workflow/               # 工作流
```

### 🚨 严格禁止事项

```java
// ❌ 严格禁止的包结构问题

// 1. 重复包名问题
net.lab1024.sa.access.access.entity.AccessDeviceEntity     // 禁止！
net.lab1024.sa.consume.consume.entity.ConsumeRecordEntity   // 禁止！
net.lab1024.sa.attendance.attendance.entity.AttendanceRecordEntity  // 禁止！

// 2. Entity分散存储
net.lab1024.sa.access.entity.AccessDeviceEntity            // 应移至公共模块
net.lab1024.sa.consume.entity.ConsumeRecordEntity           // 应移至公共模块

// 3. Manager使用Spring注解
@Component
public class AccessManagerImpl implements AccessManager {   // 禁止使用Spring注解！
    @Resource
    private UserDao userDao;
}

// 4. 包结构不统一
net.lab1024.sa.video/
├── domain/vo/           // 缺少完整包结构
├── service/
└── VideoServiceApplication.java
```

---

## 🔍 包结构违规检测和修复

### 重复包名检测工具

```java
// 重复包名检测工具
@Component
@Slf4j
public class DuplicatePackageDetector {

    /**
     * 检测重复包名问题
     */
    public DuplicatePackageReport detectDuplicatePackages(String projectPath) {
        DuplicatePackageReport report = new DuplicatePackageReport();

        // 1. 扫描所有Java文件
        List<File> javaFiles = scanJavaFiles(projectPath);

        // 2. 分析包结构
        Map<String, List<String>> packageStructure = analyzePackageStructure(javaFiles);

        // 3. 检测重复包名
        detectRedundantPackageNames(packageStructure, report);

        return report;
    }

    private void detectRedundantPackageNames(Map<String, List<String>> packageStructure, DuplicatePackageReport report) {
        // 检测 access.access.entity 模式
        for (String packageName : packageStructure.keySet()) {
            if (hasRedundantPackageName(packageName)) {
                report.addViolation(new PackageStructureViolation(
                    packageName,
                    "发现重复包名: " + packageName,
                    getCorrectedPackageName(packageName),
                    ViolationSeverity.HIGH
                ));
            }
        }
    }

    private boolean hasRedundantPackageName(String packageName) {
        // 检测 service.service.entity 模式
        String[] parts = packageName.split("\\.");
        if (parts.length >= 4) {
            String serviceName = parts[3]; // net.lab1024.sa.{service}.xxx
            for (int i = 4; i < parts.length; i++) {
                if (serviceName.equals(parts[i])) {
                    return true; // 发现重复
                }
            }
        }
        return false;
    }

    private String getCorrectedPackageName(String packageName) {
        String[] parts = packageName.split("\\.");
        StringBuilder corrected = new StringBuilder();

        // 保留前4部分：net.lab1024.sa.{service}
        for (int i = 0; i < Math.min(4, parts.length); i++) {
            if (i > 0) corrected.append(".");
            corrected.append(parts[i]);
        }

        // 跳过重复部分，添加后续部分
        boolean skipped = false;
        for (int i = 4; i < parts.length; i++) {
            if (parts[i].equals(parts[3]) && !skipped) {
                skipped = true; // 跳过重复部分
                continue;
            }
            corrected.append(".").append(parts[i]);
        }

        return corrected.toString();
    }

    /**
     * 修复重复包名问题
     */
    public void fixDuplicatePackages(DuplicatePackageReport report, String projectPath) {
        for (PackageStructureViolation violation : report.getViolations()) {
            try {
                fixSinglePackageViolation(violation, projectPath);
            } catch (Exception e) {
                log.error("修复包结构违规失败: {}", violation, e);
            }
        }
    }

    private void fixSinglePackageViolation(PackageStructureViolation violation, String projectPath) {
        String oldPackageName = violation.getCurrentPackage();
        String newPackageName = violation.getSuggestedPackage();

        // 1. 重命名目录
        renamePackageDirectory(projectPath, oldPackageName, newPackageName);

        // 2. 更新Java文件中的package声明
        updatePackageDeclarations(projectPath, oldPackageName, newPackageName);

        // 3. 更新import语句
        updateImportStatements(projectPath, oldPackageName, newPackageName);

        log.info("已修复重复包名: {} → {}", oldPackageName, newPackageName);
    }
}
```

### Entity管理检测工具

```java
// Entity管理检测工具
@Component
@Slf4j
public class EntityManagementChecker {

    /**
     * 检查Entity管理规范
     */
    public EntityManagementReport checkEntityManagement(String projectPath) {
        EntityManagementReport report = new EntityManagementReport();

        List<File> entityFiles = scanEntityFiles(projectPath);

        for (File entityFile : entityFiles) {
            checkEntityLocation(entityFile, report);
        }

        return report;
    }

    private List<File> scanEntityFiles(String projectPath) {
        List<File> entityFiles = new ArrayList<>();

        // 扫描所有Entity文件
        File microservicesDir = new File(projectPath, "microservices");
        scanDirectoryForEntities(microservicesDir, entityFiles);

        return entityFiles;
    }

    private void scanDirectoryForEntities(File directory, List<File> entityFiles) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // 检查是否是entity目录
                if (file.getName().equals("entity")) {
                    scanEntityDirectory(file, entityFiles);
                } else {
                    scanDirectoryForEntities(file, entityFiles);
                }
            }
        }
    }

    private void scanEntityDirectory(File entityDir, List<File> entityFiles) {
        File[] entityFiles = entityDir.listFiles((dir, name) -> name.endsWith("Entity.java"));
        if (entityFiles != null) {
            for (File entityFile : entityFiles) {
                entityFiles.add(entityFile);
            }
        }
    }

    private void checkEntityLocation(File entityFile, EntityManagementReport report) {
        String filePath = entityFile.getAbsolutePath();
        String entityName = entityFile.getName().replace(".java", "");

        // 检查是否在业务微服务中（应该移到公共模块）
        if (isInBusinessService(filePath) && !isInCommonModule(filePath)) {
            report.addViolation(new EntityManagementViolation(
                filePath,
                entityName,
                "Entity在业务微服务中，应移至对应公共模块",
                determineTargetCommonModule(entityName),
                ViolationSeverity.HIGH
            ));
        }
    }

    private boolean isInBusinessService(String filePath) {
        return filePath.contains("ioedream-") &&
               !filePath.contains("microservices-common");
    }

    private boolean isInCommonModule(String filePath) {
        return filePath.contains("microservices-common") ||
               filePath.contains("common-business");
    }

    private String determineTargetCommonModule(String entityName) {
        // 根据Entity名称确定目标公共模块
        if (entityName.startsWith("User") || entityName.startsWith("Department") ||
            entityName.startsWith("Area") || entityName.startsWith("Device")) {
            return "net.lab1024.sa.common.organization.entity";
        } else if (entityName.startsWith("Access")) {
            return "net.lab1024.sa.common.access.entity";
        } else if (entityName.startsWith("Consume")) {
            return "net.lab1024.sa.common.consume.entity";
        } else if (entityName.startsWith("Attendance")) {
            return "net.lab1024.sa.common.attendance.entity";
        } else if (entityName.startsWith("Video")) {
            return "net.lab1024.sa.common.video.entity";
        } else if (entityName.startsWith("Visitor")) {
            return "net.lab1024.sa.common.visitor.entity";
        } else {
            return "net.lab1024.sa.common.core.entity"; // 默认核心模块
        }
    }

    /**
     * 生成Entity迁移方案
     */
    public EntityMigrationPlan generateMigrationPlan(EntityManagementReport report) {
        EntityMigrationPlan plan = new EntityMigrationPlan();

        // 按目标模块分组
        Map<String, List<EntityManagementViolation>> violationsByModule =
            report.getViolations().stream()
                .collect(Collectors.groupingBy(EntityManagementViolation::getTargetModule));

        for (Map.Entry<String, List<EntityManagementViolation>> entry : violationsByModule.entrySet()) {
            String targetModule = entry.getKey();
            List<EntityManagementViolation> violations = entry.getValue();

            MigrationStep step = new MigrationStep();
            step.setTargetModule(targetModule);
            step.setEntitiesToMigrate(violations.stream()
                .map(v -> v.getEntityName())
                .collect(Collectors.toList()));
            step.setMigrationOrder(determineMigrationOrder(violations));

            plan.addStep(step);
        }

        return plan;
    }

    private int determineMigrationOrder(List<EntityManagementViolation> violations) {
        // 根据Entity依赖关系确定迁移顺序
        return violations.size(); // 简化实现，实际应分析依赖关系
    }
}
```

### Manager层规范检查工具

```java
// Manager层规范检查工具
@Component
@Slf4j
public class ManagerLayerChecker {

    /**
     * 检查Manager层规范
     */
    public ManagerLayerReport checkManagerLayerCompliance(String projectPath) {
        ManagerLayerReport report = new ManagerLayerReport();

        List<File> managerFiles = scanManagerFiles(projectPath);

        for (File managerFile : managerFiles) {
            checkManagerFile(managerFile, report);
        }

        return report;
    }

    private List<File> scanManagerFiles(String projectPath) {
        List<File> managerFiles = new ArrayList<>();

        // 扫描所有Manager文件
        File microservicesDir = new File(projectPath, "microservices");
        scanDirectoryForManagers(microservicesDir, managerFiles);

        return managerFiles;
    }

    private void scanDirectoryForManagers(File directory, List<File> managerFiles) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryForManagers(file, managerFiles);
            } else if (file.getName().endsWith("Manager.java") ||
                       file.getName().endsWith("ManagerImpl.java")) {
                managerFiles.add(file);
            }
        }
    }

    private void checkManagerFile(File managerFile, ManagerLayerReport report) {
        try {
            String content = Files.readString(managerFile.toPath());
            List<String> lines = Files.readAllLines(managerFile.toPath());

            // 检查Spring注解使用
            checkSpringAnnotations(managerFile, content, lines, report);

            // 检查依赖注入方式
            checkDependencyInjection(managerFile, content, lines, report);

            // 检查事务注解使用
            checkTransactionAnnotations(managerFile, content, lines, report);

        } catch (IOException e) {
            log.warn("读取Manager文件失败: {}", managerFile.getPath(), e);
        }
    }

    private void checkSpringAnnotations(File managerFile, String content, List<String> lines, ManagerLayerReport report) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // 检查禁止的Spring注解
            if (line.contains("@Component") || line.contains("@Service") ||
                line.contains("@Repository")) {
                report.addViolation(new ManagerLayerViolation(
                    managerFile.getPath(),
                    i + 1,
                    line,
                    "Manager类禁止使用Spring注解，应为纯Java类",
                    ViolationSeverity.HIGH
                ));
            }
        }
    }

    private void checkDependencyInjection(File managerFile, String content, List<String> lines, ManagerLayerReport report) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // 检查@Resource或@Autowired使用
            if (line.contains("@Resource") || line.contains("@Autowired")) {
                report.addViolation(new ManagerLayerViolation(
                    managerFile.getPath(),
                    i + 1,
                    line,
                    "Manager类应使用构造函数注入，禁止使用字段注入",
                    ViolationSeverity.MEDIUM
                ));
            }
        }
    }

    private void checkTransactionAnnotations(File managerFile, String content, List<String> lines, ManagerLayerReport report) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // 检查事务注解
            if (line.contains("@Transactional")) {
                report.addViolation(new ManagerLayerViolation(
                    managerFile.getPath(),
                    i + 1,
                    line,
                    "Manager类不应管理事务，事务应在Service层处理",
                    ViolationSeverity.MEDIUM
                ));
            }
        }
    }

    /**
     * 生成Manager重构方案
     */
    public ManagerRefactoringPlan generateRefactoringPlan(ManagerLayerReport report) {
        ManagerRefactoringPlan plan = new ManagerRefactoringPlan();

        for (ManagerLayerViolation violation : report.getViolations()) {
            RefactoringStep step = new RefactoringStep();
            step.setFilePath(violation.getFilePath());
            step.setViolationType(violation.getViolationType());
            step.setRefactoringAction(determineRefactoringAction(violation));
            plan.addStep(step);
        }

        return plan;
    }

    private String determineRefactoringAction(ManagerLayerViolation violation) {
        switch (violation.getViolationType()) {
            case SPRING_ANNOTATION_USAGE:
                return "移除Spring注解，改为纯Java类";
            case DEPENDENCY_INJECTION_FIELD:
                return "改为构造函数注入";
            case TRANSACTION_ANNOTATION_USAGE:
                return "移除事务注解，移至Service层";
            default:
                return "检查并修正违规";
        }
    }
}
```

---

## 📊 包结构质量指标体系

### 核心质量指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **包结构合规率** | 100% | 包结构符合规范比例 | 包结构检查 |
| **重复包名违规数** | 0 | 重复包名问题数量 | 重复包名检测 |
| **Entity统一管理率** | 100% | Entity在公共模块管理比例 | Entity管理检查 |
| **Manager规范率** | 100% | Manager使用纯Java类比例 | Manager规范检查 |
| **包命名一致性** | ≥95% | 包命名规范一致比例 | 包命名检查 |

### 健康指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **目录层级深度** | ≤6层 | 包目录层级深度 | 目录结构分析 |
| **包大小合理性** | ≤15个类/包 | 单个包的类数量 | 包大小统计 |
| **包职责单一性** | ≥90% | 包职责单一符合比例 | 职责分析 |
| **包依赖合理性** | ≤5个依赖/包 | 包依赖数量 | 依赖分析 |

---

## 🛠️ 自动化重构工具

### 包结构重构脚本

```powershell
# fix-package-structure.ps1 - 包结构重构脚本
param(
    [string]$ProjectPath = ".",
    [switch]$DryRun,
    [switch]$FixAll
)

Write-Host "🔍 开始包结构合规性检查..." -ForegroundColor Green

# 1. 检测重复包名
Write-Host "1️⃣ 检测重复包名..." -ForegroundColor Yellow
$duplicateIssues = Detect-DuplicatePackages -ProjectPath $ProjectPath

# 2. 检查Entity管理
Write-Host "2️⃣ 检查Entity管理..." -ForegroundColor Yellow
$entityIssues = Check-EntityManagement -ProjectPath $ProjectPath

# 3. 检查Manager规范
Write-Host "3️⃣ 检查Manager规范..." -ForegroundColor Yellow
$managerIssues = Check-ManagerLayer -ProjectPath $ProjectPath

# 4. 生成报告
$report = @{
    DuplicatePackages = $duplicateIssues
    EntityManagement = $entityIssues
    ManagerLayer = $managerIssues
    TotalIssues = $duplicateIssues.Count + $entityIssues.Count + $managerIssues.Count
}

# 输出检查结果
Write-Host "📊 检查结果统计:" -ForegroundColor Cyan
Write-Host "  - 重复包名问题: $($duplicateIssues.Count)" -ForegroundColor White
Write-Host "  - Entity管理问题: $($entityIssues.Count)" -ForegroundColor White
Write-Host "  - Manager规范问题: $($managerIssues.Count)" -ForegroundColor White
Write-Host "  - 总问题数: $($report.TotalIssues)" -ForegroundColor White

if ($report.TotalIssues -eq 0) {
    Write-Host "✅ 包结构检查通过，未发现问题！" -ForegroundColor Green
    exit 0
}

# 显示问题详情
Write-Host "`n🚨 发现的问题:" -ForegroundColor Red

if ($duplicateIssues.Count -gt 0) {
    Write-Host "`n📦 重复包名问题:" -ForegroundColor Yellow
    $duplicateIssues | ForEach-Object {
        Write-Host "  ❌ $($_.Package)" -ForegroundColor Red
    }
}

if ($entityIssues.Count -gt 0) {
    Write-Host "`n🏗️ Entity管理问题:" -ForegroundColor Yellow
    $entityIssues | ForEach-Object {
        Write-Host "  ❌ $($_.EntityName) - $($_.FilePath)" -ForegroundColor Red
    }
}

if ($managerIssues.Count -gt 0) {
    Write-Host "`n⚙️ Manager规范问题:" -ForegroundColor Yellow
    $managerIssues | ForEach-Object {
        Write-Host "  ❌ $($_.FileName) - 行$($_.LineNumber): $($_.Issue)" -ForegroundColor Red
    }
}

# 修复确认
if ($FixAll -and !$DryRun) {
    Write-Host "`n🔧 开始自动修复..." -ForegroundColor Green

    # 修复重复包名
    if ($duplicateIssues.Count -gt 0) {
        Write-Host "修复重复包名..." -ForegroundColor Yellow
        Fix-DuplicatePackages -Issues $duplicateIssues -ProjectPath $ProjectPath
    }

    # 生成Entity迁移方案
    if ($entityIssues.Count -gt 0) {
        Write-Host "生成Entity迁移方案..." -ForegroundColor Yellow
        $migrationPlan = New-EntityMigrationPlan -Issues $entityIssues
        Save-MigrationPlan -Plan $migrationPlan -OutputPath "./entity-migration-plan.json"
        Write-Host "Entity迁移方案已保存至: ./entity-migration-plan.json" -ForegroundColor Green
    }

    # 修复Manager规范
    if ($managerIssues.Count -gt 0) {
        Write-Host "修复Manager规范..." -ForegroundColor Yellow
        Fix-ManagerLayer -Issues $managerIssues -ProjectPath $ProjectPath
    }

    Write-Host "✅ 自动修复完成！" -ForegroundColor Green
} elseif ($DryRun) {
    Write-Host "`n💡 这是试运行模式，使用 -FixAll 参数执行实际修复" -ForegroundColor Cyan
}

function Detect-DuplicatePackages {
    param([string]$ProjectPath)

    $issues = @()
    $javaFiles = Get-ChildItem -Path $ProjectPath -Recurse -Filter "*.java"

    foreach ($file in $javaFiles) {
        $content = Get-Content $file.FullName -Raw

        # 检查重复包名模式
        if ($content -match 'package\s+net\.lab1024\.sa\.\w+\.(\w+)\.(\w+)') {
            $serviceName = $matches[1]
            $subPackage = $matches[2]

            if ($serviceName -eq $subPackage) {
                $issues += @{
                    Package = $matches[0]
                    File = $file.FullName
                    Service = $serviceName
                    Corrected = "net.lab1024.sa.$serviceName.$subPackage"
                }
            }
        }
    }

    return $issues
}

function Check-EntityManagement {
    param([string]$ProjectPath)

    $issues = @()
    $entityFiles = Get-ChildItem -Path $ProjectPath -Recurse -Filter "*Entity.java"

    foreach ($file in $entityFiles) {
        # 检查是否在业务微服务中
        if ($file.FullName -match 'ioedream-\w+-service' -and
            $file.FullName -notmatch 'microservices-common') {
            $entityName = $file.BaseName
            $targetModule = Determine-TargetModule -EntityName $entityName

            $issues += @{
                EntityName = $entityName
                FilePath = $file.FullName
                Issue = "Entity应在公共模块中"
                TargetModule = $targetModule
            }
        }
    }

    return $issues
}

function Determine-TargetModule {
    param([string]$EntityName)

    switch -Regex ($EntityName) {
        '^User|^Department|^Area|^Device' { return "organization" }
        '^Access' { return "access" }
        '^Consume' { return "consume" }
        '^Attendance' { return "attendance" }
        '^Video' { return "video" }
        '^Visitor' { return "visitor" }
        default { return "core" }
    }
}
```

### Entity迁移助手

```java
// Entity迁移助手工具
@Component
@Slf4j
public class EntityMigrationHelper {

    /**
     * 执行Entity迁移
     */
    public MigrationResult migrateEntities(EntityMigrationPlan plan, String projectPath) {
        MigrationResult result = new MigrationResult();

        for (MigrationStep step : plan.getSteps()) {
            try {
                executeMigrationStep(step, projectPath, result);
            } catch (Exception e) {
                log.error("Entity迁移失败: {}", step, e);
                result.addFailure(step, e.getMessage());
            }
        }

        return result;
    }

    private void executeMigrationStep(MigrationStep step, String projectPath, MigrationResult result) {
        String targetModule = step.getTargetModule();
        List<String> entities = step.getEntitiesToMigrate();

        // 1. 创建目标目录
        createTargetDirectories(targetModule, projectPath);

        // 2. 迁移Entity文件
        for (String entityName : entities) {
            migrateEntityFile(entityName, targetModule, projectPath, result);
        }

        // 3. 更新import语句
        updateImportStatements(entities, targetModule, projectPath, result);

        result.addSuccess(step, "成功迁移 " + entities.size() + " 个Entity到 " + targetModule);
    }

    private void createTargetDirectories(String targetModule, String projectPath) {
        String[] moduleParts = targetModule.split("\\.");
        StringBuilder pathBuilder = new StringBuilder(projectPath);

        // 构建目录路径
        for (int i = 0; i < 6; i++) { // net.lab1024.sa.common.{module}.entity
            pathBuilder.append("/").append(moduleParts[i]);
        }

        File targetDir = new File(pathBuilder.toString());
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            if (created) {
                log.info("创建目标目录: {}", targetDir.getAbsolutePath());
            }
        }
    }

    private void migrateEntityFile(String entityName, String targetModule, String projectPath, MigrationResult result) {
        // 查找源文件
        File sourceFile = findEntityFile(entityName, projectPath);
        if (sourceFile == null) {
            result.addWarning("未找到Entity文件: " + entityName);
            return;
        }

        // 构建目标路径
        String targetPath = buildTargetPath(entityName, targetModule, projectPath);
        File targetFile = new File(targetPath);

        try {
            // 移动文件
            Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 更新package声明
            updatePackageDeclaration(targetFile, targetModule);

            log.info("Entity迁移完成: {} → {}", sourceFile.getName(), targetPath);
            result.addMigratedEntity(entityName, sourceFile.getAbsolutePath(), targetPath);

        } catch (IOException e) {
            log.error("Entity迁移失败: {} → {}", sourceFile, targetFile, e);
            result.addError("迁移失败: " + entityName, e.getMessage());
        }
    }

    private File findEntityFile(String entityName, String projectPath) {
        // 在业务微服务中查找Entity文件
        File microservicesDir = new File(projectPath, "microservices");

        return Files.walk(microservicesDir.toPath())
                .filter(path -> path.toString().endsWith(entityName + ".java"))
                .filter(path -> !path.toString().contains("microservices-common"))
                .map(Path::toFile)
                .findFirst()
                .orElse(null);
    }

    private String buildTargetPath(String entityName, String targetModule, String projectPath) {
        String[] moduleParts = targetModule.split("\\.");
        StringBuilder pathBuilder = new StringBuilder(projectPath);

        // 构建文件路径
        pathBuilder.append("/microservices/microservices-common-business/src/main/java");
        for (int i = 0; i < moduleParts.length; i++) {
            pathBuilder.append("/").append(moduleParts[i]);
        }
        pathBuilder.append("/").append(entityName).append(".java");

        return pathBuilder.toString();
    }

    private void updatePackageDeclaration(File entityFile, String targetModule) throws IOException {
        String content = Files.readString(entityFile.toPath());

        // 更新package声明
        content = content.replaceFirst(
            "package\\s+net\\.lab1024\\.sa\\.[^;]+;",
            "package " + targetModule + ";"
        );

        Files.writeString(entityFile.toPath(), content);
    }

    private void updateImportStatements(List<String> entities, String targetModule, String projectPath, MigrationResult result) {
        // 扫描所有Java文件，更新import语句
        Files.walk(Paths.get(projectPath))
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        updateFileImports(path, entities, targetModule, result);
                    } catch (IOException e) {
                        log.warn("更新import语句失败: {}", path, e);
                    }
                });
    }

    private void updateFileImports(Path filePath, List<String> entities, String targetModule, MigrationResult result) throws IOException {
        String content = Files.readString(filePath);
        boolean modified = false;

        for (String entityName : entities) {
            // 查找旧的import语句
            String oldImportPattern = "import\\s+net\\.lab1024\\.sa\\.[^;]*" + entityName + ";";
            String newImportStatement = "import " + targetModule + "." + entityName + ";";

            if (content.matches(".*" + oldImportPattern + ".*")) {
                content = content.replaceAll(oldImportPattern, newImportStatement);
                modified = true;
                result.addUpdatedImport(filePath.toString(), entityName);
            }
        }

        if (modified) {
            Files.writeString(filePath, content);
        }
    }
}
```

---

## 🎯 实施指南

### P0级立即执行（1周内）

#### 1. 重复包名修复
```bash
# 使用自动化脚本修复
./scripts/fix-package-structure.ps1 -FixAll

# 验证修复结果
./scripts/check-package-structure.ps1
```

#### 2. Entity迁移准备
```bash
# 生成Entity迁移方案
./scripts/generate-entity-migration-plan.ps1

# 检查迁移依赖关系
./scripts/analyze-entity-dependencies.ps1
```

### P1级快速优化（2周内）

#### 3. Manager层规范化
```bash
# 检查Manager规范
./scripts/check-manager-standards.ps1

# 生成重构方案
./scripts/generate-manager-refactor-plan.ps1
```

### 质量保障措施

#### 自动化检查集成
```yaml
# CI/CD 集成检查
stages:
  - package-structure-check

package_structure_check:
  stage: package-structure-check
  script:
    - ./scripts/fix-package-structure.ps1 -DryRun
    - ./scripts/check-package-structure.ps1
  artifacts:
    reports:
      junit: package-structure-report.xml
```

#### Git Hooks集成
```bash
# pre-commit hook
#!/bin/bash
echo "🔍 包结构合规性检查..."
./scripts/check-package-structure.ps1

if [ $? -ne 0 ]; then
    echo "❌ 包结构检查失败，请修复后提交"
    exit 1
fi

echo "✅ 包结构检查通过"
```

---

## 📚 相关文档参考

### 核心规范文档
- **📋 CLAUDE.md**: 全局架构规范 (强制遵循)
- **📦 后端包目录结构优化报告**: 详细分析和优化方案
- **🏗️ 四层架构详解**: Controller→Service→Manager→DAO架构模式

### 工具和脚本
- **🔧 fix-package-structure.ps1**: 包结构自动化修复脚本
- **🏗️ Entity迁移助手**: Entity统一迁移工具
- **⚙️ Manager规范检查器**: Manager层规范检查工具

### 最佳实践指南
- **📊 包结构设计原则**: 包结构设计最佳实践
- **🛠️ 重构安全指南**: 安全的包结构重构方法
- **📋 质量检查清单**: 包结构质量保障清单

---

**📋 重要提醒**:
1. 本技能严格守护IOE-DREAM包目录结构规范
2. 立即修复所有重复包名问题（P0级优先级）
3. 统一Entity管理，禁止分散存储
4. 确保Manager使用纯Java类设计
5. 建立包结构质量保障长效机制
6. 定期进行包结构合规性检查

**让我们一起建设规范、清晰的包目录结构！** 🚀

---
**文档版本**: v1.0.0 - 包目录结构守护版
**创建时间**: 2025-01-15
**最后更新**: 2025-01-15
**技能等级**: ★★★★★ (顶级专家)
**适用架构**: Spring Boot 3.5.8 + 微服务架构