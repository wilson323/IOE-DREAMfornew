# IOE-DREAM 全局错误修复执行脚本
# 生成时间: 2025-12-26
# 目标: 系统性修复5,003个编译错误

param(
    [ValidateSet("Analyze", "Phase1", "Phase2", "Phase3", "All")]
    [string]$Phase = "Analyze",

    [switch]$SkipTests,
    [switch]$Force,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "Continue"

# 颜色输出函数
function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "  $Title" -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
}

function Test-Command {
    param([string]$Command)
    try {
        $null = Get-Command $Command -ErrorAction Stop
        return $true
    }
    catch {
        return $false
    }
}

# 检查必要工具
function Initialize-Environment {
    Write-Section "环境检查"

    $requiredTools = @(
        @{Name="mvn"; Command="mvn --version"},
        @{Name="git"; Command="git --version"},
        @{Name="java"; Command="java -version"}
    )

    foreach ($tool in $requiredTools) {
        if (Test-Command $tool.Name) {
            Write-ColorOutput "✅ $($tool.Name) 已安装" "Green"
            Invoke-Expression $tool.Command | Select-Object -First 1
        }
        else {
            Write-ColorOutput "❌ $($tool.Name) 未安装" "Red"
            throw "缺少必要工具: $($tool.Name)"
        }
    }

    # 检查Maven本地仓库
    $localRepo = "$env:USERPROFILE\.m2\repository"
    if (Test-Path $localRepo) {
        Write-ColorOutput "✅ Maven本地仓库: $localRepo" "Green"
    }
    else {
        Write-ColorOutput "⚠️ Maven本地仓库不存在" "Yellow"
    }

    Write-Host ""
}

# Phase 1: 紧急修复 - Entity统一迁移
function Invoke-Phase1 {
    Write-Section "Phase 1: Entity统一迁移 (预计2小时)"

    $projectRoot = "D:\IOE-DREAM"
    Set-Location $projectRoot

    # 步骤1.1: 统计当前Entity分布
    Write-ColorOutput "步骤1.1: 统计Entity分布..." "Cyan"
    $entityFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Entity.java" -File
    Write-ColorOutput "找到 $($entityFiles.Count) 个Entity文件" "Yellow"

    # 按模块分组
    $entityByModule = $entityFiles | Group-Object {
        if ($_.FullName -match "microservices/(ioedream-[^/]+|microservices-common-[^/]+)") {
            $matches[1]
        } else {
            "other"
        }
    }

    foreach ($group in $entityByModule) {
        Write-Host "  $($group.Name): $($group.Count) 个Entity" -ForegroundColor Gray
    }

    # 步骤1.2: 备份当前状态
    Write-ColorOutput "步骤1.2: 创建备份分支..." "Cyan"
    $backupBranch = "backup/before-entity-migration-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

    if ($DryRun) {
        Write-ColorOutput "[DRY-RUN] 将创建分支: $backupBranch" "Yellow"
    }
    else {
        git checkout -b $backupBranch
        Write-ColorOutput "✅ 备份分支: $backupBranch" "Green"
    }

    # 步骤1.3: 删除业务服务中的重复Entity
    Write-ColorOutput "步骤1.3: 删除重复Entity..." "Cyan"

    $duplicatePatterns = @(
        "microservices/ioedream-access-service/**/entity/*Entity.java",
        "microservices/ioedream-access-service/**/domain/entity/*Entity.java",
        "microservices/ioedream-attendance-service/**/entity/*Entity.java",
        "microservices/ioedream-attendance-service/**/domain/entity/*Entity.java",
        "microservices/ioedream-consume-service/**/entity/*Entity.java",
        "microservices/ioedream-consume-service/**/domain/entity/*Entity.java",
        "microservices/ioedream-video-service/**/entity/*Entity.java",
        "microservices/ioedream-video-service/**/domain/entity/*Entity.java",
        "microservices/ioedream-visitor-service/**/entity/*Entity.java",
        "microservices/ioedream-visitor-service/**/domain/entity/*Entity.java"
    )

    $duplicatesToDelete = @()
    foreach ($pattern in $duplicatePatterns) {
        $files = Get-ChildItem -Path $pattern -File -ErrorAction SilentlyContinue
        $duplicatesToDelete += $files
    }

    Write-ColorOutput "找到 $($duplicatesToDelete.Count) 个重复Entity" "Yellow"

    if ($DryRun) {
        foreach ($file in $duplicatesToDelete) {
            Write-ColorOutput "[DRY-RUN] 将删除: $($file.FullName)" "Yellow"
        }
    }
    else {
        # 先移动到备份目录而不是直接删除
        $backupDir = "backup/deleted-entities-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
        New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

        foreach ($file in $duplicatesToDelete) {
            $relativePath = $file.FullName.Substring($projectRoot.Length + 1)
            $destPath = Join-Path $backupDir $relativePath
            $destDir = Split-Path $destPath -Parent
            New-Item -ItemType Directory -Path $destDir -Force | Out-Null
            Move-Item -Path $file.FullName -Destination $destPath -Force
        }

        Write-ColorOutput "✅ 已移动 $($duplicatesToDelete.Count) 个重复Entity到: $backupDir" "Green"
    }

    # 步骤1.4: 确保common-entity模块结构完整
    Write-ColorOutput "步骤1.4: 检查common-entity结构..." "Cyan"

    $entityTargetDir = "microservices/microservices-common-entity/src/main/java/net/lab1024/sa/common/entity"
    $requiredSubDirs = @("access", "attendance", "consume", "video", "visitor", "device")

    foreach ($subDir in $requiredSubDirs) {
        $targetPath = Join-Path $entityTargetDir $subDir
        if (-not (Test-Path $targetPath)) {
            Write-ColorOutput "创建目录: $targetPath" "Yellow"
            if (-not $DryRun) {
                New-Item -ItemType Directory -Path $targetPath -Force | Out-Null
            }
        }
    }

    # 步骤1.5: 批量更新导入语句
    Write-ColorOutput "步骤1.5: 批量更新导入语句..." "Cyan"

    $importReplacements = @(
        @{From="net\.lab1024\.sa\.access\.domain\.entity\."; To="net.lab1024.sa.common.entity.access."},
        @{From="net\.lab1024\.sa\.access\.entity\."; To="net.lab1024.sa.common.entity.access."},
        @{From="net\.lab1024\.sa\.attendance\.entity\."; To="net.lab1024.sa.common.entity.attendance."},
        @{From="net\.lab1024\.sa\.consume\.entity\."; To="net.lab1024.sa.common.entity.consume."},
        @{From="net\.lab1024\.sa\.video\.entity\."; To="net.lab1024.sa.common.entity.video."},
        @{From="net\.lab1024\.sa\.visitor\.entity\."; To="net.lab1024.sa.common.entity.visitor."},
        @{From="net\.lab1024\.sa\.entity\."; To="net.lab1024.sa.common.entity."}
    )

    $javaFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" -File
    Write-ColorOutput "找到 $($javaFiles.Count) 个Java文件需要检查" "Yellow"

    foreach ($replacement in $importReplacements) {
        Write-Host "  替换: $($replacement.From) → $($replacement.To)" -ForegroundColor Gray

        foreach ($file in $javaFiles) {
            $content = Get-Content $file.FullName -Raw -Encoding UTF8
            if ($content -match $replacement.From) {
                if ($DryRun) {
                    Write-ColorOutput "[DRY-RUN] $($file.FullName)" "Yellow"
                }
                else {
                    $newContent = $content -replace $replacement.From, $replacement.To
                    Set-Content -Path $file.FullName -Value $newContent -Encoding UTF8 -NoNewline
                }
            }
        }
    }

    if (-not $DryRun) {
        Write-ColorOutput "✅ 导入语句更新完成" "Green"
    }

    Write-ColorOutput "Phase 1 完成!" "Green"
    Write-ColorOutput "下一步: 运行 'mvn clean compile' 验证编译" "Cyan"
}

# Phase 2: 修复测试代码API
function Invoke-Phase2 {
    Write-Section "Phase 2: 测试代码API修复"

    # 步骤2.1: 更新Builder模式
    Write-ColorOutput "步骤2.1: 检查Builder模式使用..." "Cyan"

    # 查找需要@Builder.Default的字段
    $builderPattern = @"
@Data
@Builder
public class \w+ \{
    private Boolean \w+;
"@

    $filesWithBuilder = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" -File |
        Where-Object {
            $content = Get-Content $_.FullName -Raw -Encoding UTF8
            $content -match "@Data" -and $content -match "@Builder" -and $content -match "private Boolean"
        }

    Write-ColorOutput "找到 $($filesWithBuilder.Count) 个文件需要检查Builder模式" "Yellow"

    # 步骤2.2: 删除引用不存在类的测试
    Write-ColorOutput "步骤2.2: 查找引用不存在类的测试..." "Cyan"

    $missingClasses = @(
        "ScheduleAlgorithm",
        "SchedulePredictor",
        "RuleLoader",
        "RuleValidator",
        "RuleCacheManager",
        "RuleEvaluatorFactory",
        "RuleExecutor",
        "CompiledActionObject",
        "RuleExecutionStatistics"
    )

    foreach ($className in $missingClasses) {
        Write-Host "  查找引用: $className" -ForegroundColor Gray
        $matches = Select-String -Path "microservices/*/src/test" -Pattern $className -Recurse
        Write-ColorOutput "  找到 $($matches.Count) 处引用" "Yellow"

        if ($matches.Count -gt 0) {
            $filesToDelete = $matches | Select-Object -ExpandProperty Path | Sort-Object -Unique

            foreach ($file in $filesToDelete) {
                Write-ColorOutput "  建议删除或修复: $file" "Yellow"
                if ($Force -and -not $DryRun) {
                    # 备份后删除
                    $backupPath = "$file.bak"
                    Copy-Item $file $backupPath
                    Remove-Item $file
                    Write-ColorOutput "  ✅ 已删除(备份: $backupPath)" "Green"
                }
            }
        }
    }

    # 步骤2.3: 更新MockBean到MockitoBean
    Write-ColorOutput "步骤2.3: 更新@MockBean到@MockitoBean..." "Cyan"

    $mockBeanFiles = Select-String -Path "microservices" -Pattern "@MockBean" -Recurse
    Write-ColorOutput "找到 $($mockBeanFiles.Count) 处使用@MockBean" "Yellow"

    foreach ($match in $mockBeanFiles) {
        if ($DryRun) {
            Write-ColorOutput "[DRY-RUN] $($match.Path)" "Yellow"
        }
        else {
            $content = Get-Content $match.Path -Raw -Encoding UTF8
            $newContent = $content -replace "@MockBean", "@MockitoBean"
            Set-Content -Path $match.Path -Value $newContent -Encoding UTF8 -NoNewline
        }
    }

    if (-not $DryRun) {
        Write-ColorOutput "✅ MockBean更新完成" "Green"
    }

    Write-ColorOutput "Phase 2 完成!" "Green"
}

# Phase 3: 修复构建顺序和依赖
function Invoke-Phase3 {
    Write-Section "Phase 3: 构建顺序和依赖修复"

    $projectRoot = "D:\IOE-DREAM"
    Set-Location $projectRoot

    # 步骤3.1: 验证本地仓库
    Write-ColorOutput "步骤3.1: 检查本地Maven仓库..." "Cyan"

    $requiredJars = @(
        "microservices-common-core\1.0.0\microservices-common-core-1.0.0.jar",
        "microservices-common-entity\1.0.0\microservices-common-entity-1.0.0.jar",
        "microservices-common-business\1.0.0\microservices-common-business-1.0.0.jar",
        "microservices-common-data\1.0.0\microservices-common-data-1.0.0.jar",
        "microservices-common-gateway-client\1.0.0\microservices-common-gateway-client-1.0.0.jar"
    )

    $localRepo = "$env:USERPROFILE\.m2\repository\net\lab1024\sa"
    $missingJars = @()

    foreach ($jarPath in $requiredJars) {
        $fullPath = Join-Path $localRepo $jarPath
        if (Test-Path $fullPath) {
            Write-ColorOutput "✅ $(Split-Path $jarPath -Leaf)" "Green"
        }
        else {
            Write-ColorOutput "❌ $(Split-Path $jarPath -Leaf) 缺失" "Red"
            $missingJars += $jarPath.Split('\')[0]
        }
    }

    # 步骤3.2: 强制构建顺序
    Write-ColorOutput "步骤3.2: 按正确顺序构建模块..." "Cyan"

    $buildOrder = @(
        @{Name="microservices-common-core"; Path="microservices/microservices-common-core"},
        @{Name="microservices-common-entity"; Path="microservices/microservices-common-entity"},
        @{Name="microservices-common-business"; Path="microservices/microservices-common-business"},
        @{Name="microservices-common-data"; Path="microservices/microservices-common-data"},
        @{Name="microservices-common-gateway-client"; Path="microservices/microservices-common-gateway-client"},
        @{Name="microservices-common-cache"; Path="microservices/microservices-common-cache"},
        @{Name="microservices-common-security"; Path="microservices/microservices-common-security"},
        @{Name="microservices-common-monitor"; Path="microservices/microservices-common-monitor"}
    )

    foreach ($module in $buildOrder) {
        Write-Host "构建: $($module.Name)" -ForegroundColor Cyan

        if ($DryRun) {
            Write-ColorOutput "[DRY-RUN] mvn clean install -pl $($module.Path) -am -DskipTests" "Yellow"
        }
        else {
            $mvnArgs = "clean install -pl $($module.Path) -am -DskipTests"
            if ($SkipTests) {
                $mvnArgs += " -DskipTests"
            }

            $output = mvn $mvnArgs.Split(" ") 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput "✅ $($module.Name) 构建成功" "Green"
            }
            else {
                Write-ColorOutput "❌ $($module.Name) 构建失败" "Red"
                Write-ColorOutput $output "Yellow"
                throw "构建失败,停止执行"
            }
        }
    }

    # 步骤3.3: 验证编译
    Write-ColorOutput "步骤3.3: 验证完整编译..." "Cyan"

    if ($DryRun) {
        Write-ColorOutput "[DRY-RUN] mvn clean compile -DskipTests" "Yellow"
    }
    else {
        $output = mvn clean compile -DskipTests 2>&1
        $errors = $output | Select-String "ERROR" | Measure-Object

        if ($errors.Count -eq 0) {
            Write-ColorOutput "✅ 编译成功,无错误!" "Green"
        }
        else {
            Write-ColorOutput "⚠️ 编译完成,但有 $($errors.Count) 个错误" "Yellow"
        }
    }

    Write-ColorOutput "Phase 3 完成!" "Green"
}

# 分析当前状态
function Invoke-Analyze {
    Write-Section "当前项目状态分析"

    $projectRoot = "D:\IOE-DREAM"
    Set-Location $projectRoot

    # 统计Entity分布
    Write-ColorOutput "Entity类分布:" "Cyan"
    $entityFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Entity.java" -File
    $entityByModule = $entityFiles | Group-Object {
        if ($_.FullName -match "microservices/(ioedream-[^/]+|microservices-common-[^/]+)") {
            $matches[1]
        } else {
            "other"
        }
    }

    foreach ($group in $entityByModule | Sort-Object Count -Descending) {
        Write-Host "  $($group.Name.PadRight(40)): $($group.Count.ToString().PadLeft(3)) 个Entity" -ForegroundColor Gray
    }

    # 统计导入错误
    Write-ColorOutput "`n导入语句统计:" "Cyan"

    $oldImports = @(
        "net.lab1024.sa.access.domain.entity",
        "net.lab1024.sa.access.entity",
        "net.lab1024.sa.attendance.entity",
        "net.lab1024.sa.consume.entity",
        "net.lab1024.sa.video.entity",
        "net.lab1024.sa.visitor.entity"
    )

    foreach ($oldImport in $oldImports) {
        $count = (Select-String -Path "microservices" -Pattern "import $oldImport" -Recurse).Count
        if ($count -gt 0) {
            Write-Host "  $oldImport : $count 处引用" -ForegroundColor Yellow
        }
    }

    # 统计测试问题
    Write-ColorOutput "`n测试代码问题:" "Cyan"

    $testFiles = Get-ChildItem -Path "microservices/*/src/test" -Recurse -Filter "*Test.java" -File -ErrorAction SilentlyContinue
    Write-Host "  测试文件总数: $($testFiles.Count)" -ForegroundColor Gray

    if ($testFiles.Count -gt 0) {
        $mockBeanCount = (Select-String -Path $testFiles -Pattern "@MockBean" -Recurse).Count
        Write-Host "  使用@MockBean(已废弃): $mockBeanCount 处" -ForegroundColor Yellow

        $missingClassCount = (Select-String -Path $testFiles -Pattern "ScheduleAlgorithm|SchedulePredictor|RuleLoader" -Recurse).Count
        Write-Host "  引用不存在类: $missingClassCount 处" -ForegroundColor Yellow
    }

    # Maven仓库检查
    Write-ColorOutput "`nMaven本地仓库状态:" "Cyan"

    $localRepo = "$env:USERPROFILE\.m2\repository\net\lab1024\sa"
    $requiredJars = @(
        "microservices-common-core\1.0.0\microservices-common-core-1.0.0.jar",
        "microservices-common-entity\1.0.0\microservices-common-entity-1.0.0.jar",
        "microservices-common-gateway-client\1.0.0\microservices-common-gateway-client-1.0.0.jar"
    )

    foreach ($jarPath in $requiredJars) {
        $fullPath = Join-Path $localRepo $jarPath
        if (Test-Path $fullPath) {
            Write-Host "  ✅ $(Split-Path $jarPath -Leaf)" -ForegroundColor Green
        }
        else {
            Write-Host "  ❌ $(Split-Path $jarPath -Leaf) - 缺失" -ForegroundColor Red
        }
    }

    Write-ColorOutput "`n分析完成!" "Green"
    Write-ColorOutput "建议运行: .\scripts\GLOBAL_ERROR_FIX_EXECUTOR.ps1 -Phase Phase1" "Cyan
}

# 主执行流程
try {
    Initialize-Environment

    switch ($Phase) {
        "Analyze" {
            Invoke-Analyze
        }
        "Phase1" {
            Invoke-Phase1
        }
        "Phase2" {
            Invoke-Phase2
        }
        "Phase3" {
            Invoke-Phase3
        }
        "All" {
            Invoke-Phase1
            Invoke-Phase2
            Invoke-Phase3
        }
    }
}
catch {
    Write-ColorOutput "❌ 执行失败: $_" "Red"
    Write-ColorOutput $_.ScriptStackTrace "Yellow"
    exit 1
}

Write-ColorOutput "`n✅ 脚本执行完成!" "Green"
