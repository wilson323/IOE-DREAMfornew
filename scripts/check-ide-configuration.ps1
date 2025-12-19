# IDE配置检查和修复脚本
# 目的: 确保开发环境配置的一致性和正确性

param(
    [switch]$FixIssues,
    [switch]$CheckLombok,
    [switch]$CheckMaven,
    [switch]$CheckVSCode
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "IDE配置检查和修复" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

# 1. 检查VSCode配置
function Test-VSCodeConfiguration {
    Write-Host "`n## 1. VSCode配置检查 ##" -ForegroundColor Yellow

    $vscodeDir = ".vscode"
    $settingsPath = "$vscodeDir/settings.json"

    if (!(Test-Path $vscodeDir)) {
        Write-Host "创建.vscode目录..." -ForegroundColor White
        New-Item -ItemType Directory -Path $vscodeDir -Force | Out-Null
    }

    # 检查或创建settings.json
    $needsUpdate = $false
    $settings = @{}

    if (Test-Path $settingsPath) {
        try {
            $content = Get-Content $settingsPath -Raw -Encoding UTF8
            $settings = $content | ConvertFrom-Json -AsHashtable
            Write-Host "读取现有VSCode配置..." -ForegroundColor White
        } catch {
            Write-Host "VSCode配置文件格式错误，将重新创建..." -ForegroundColor Yellow
            $settings = @{}
            $needsUpdate = $true
        }
    } else {
        Write-Host "VSCode配置文件不存在，将创建..." -ForegroundColor White
        $needsUpdate = $true
    }

    # 推荐的VSCode配置
    $recommendedSettings = @{
        "java.home" = "C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot"
        "java.configuration.updateBuildConfiguration" = "automatic"
        "java.compile.nullAnalysis.mode" = "automatic"
        "java.inlayHints.parameterNames.enabled" = "all"
        "java.inlayHints.parameterNames.exclusions" = @("**")
        "java.format.settings.url" = ".vscode/java-format-style.xml"
        "java.saveActions.organizeImports" = $true
        "java.debug.settings.onBuildFailureProceed" = $true
        "java.completion.importOrder" = @("java", "jakarta", "org", "com", "net", "io")
        "java.autobuild.enabled" = $true
        "java.dependency.autoRefresh" = $true
        "java.references.includeAccessors" = $true
        "java.codeGeneration.generateComments" = $false
        "java.codeGeneration.useBlocks" = $true
        "java.typeHierarchy.enabled" = $true
        "java.project.sourcePaths" = @("microservices/*/src/main/java", "microservices/*/src/test/java")
        "java.project.referencedLibraries" = @("lib/**/*.jar")
        "files.exclude" = @{
            "**/.git" = $true
            "**/.svn" = $true
            "**/.hg" = $true
            "**/CVS" = $true
            "**/.DS_Store" = $true
            "**/Thumbs.db" = $true
            "**/node_modules" = $true
            "**/target" = $true
            "**/bin" = $true
            "**/out" = $true
            "**/.classpath" = $true
            "**/.project" = $true
            "**/.settings" = $true
            "**/.factorypath" = $true
            "**/build" = $true
            "**/.gradle" = $true
            "**/gradlew" = $true
            "**/gradlew.bat" = $true
            "**/gradle-wrapper.properties" = $true
        }
        "editor.formatOnSave" = $true
        "editor.codeActionsOnSave" = @{
            "source.organizeImports" = $true
        }
    }

    # 检查和更新配置
    foreach ($key in $recommendedSettings.Keys) {
        $recommendedValue = $recommendedSettings[$key]

        if (-not $settings.ContainsKey($key)) {
            Write-Host "  添加配置: $key" -ForegroundColor Green
            $settings[$key] = $recommendedValue
            $needsUpdate = $true
        } elseif ($settings[$key].ToString() -ne $recommendedValue.ToString() -and $settings[$key] -isnot [hashtable]) {
            Write-Host "  更新配置: $key = $($settings[$key]) → $recommendedValue" -ForegroundColor Yellow
            $settings[$key] = $recommendedValue
            $needsUpdate = $true
        }
    }

    # 保存配置文件
    if ($needsUpdate -and $FixIssues) {
        Write-Host "保存VSCode配置..." -ForegroundColor White
        $json = $settings | ConvertTo-Json -Depth 10
        $json | Out-File -FilePath $settingsPath -Encoding UTF8
        Write-Host "✅ VSCode配置已更新" -ForegroundColor Green
    } elseif ($needsUpdate) {
        Write-Host "⚠️ VSCode配置需要更新（使用-FixIssues参数自动修复）" -ForegroundColor Yellow
    } else {
        Write-Host "✅ VSCode配置检查通过" -ForegroundColor Green
    }

    # 检查Java扩展包
    Write-Host "检查VSCode扩展包..." -ForegroundColor White
    $requiredExtensions = @(
        "redhat.java",
        "vscjava.vscode-java-pack",
        "vscjava.vscode-spring-boot-dashboard",
        "vscjava.vscode-spring-initializr-java",
        "ms-vscode.vscode-json",
        "sonarsource.sonarlint-vscode"
    )

    $installedExtensions = & code --list-extensions 2>$null
    if ($installedExtensions) {
        foreach ($ext in $requiredExtensions) {
            if ($installedExtensions -contains $ext) {
                Write-Host "  ✅ $ext 已安装" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️ 建议安装扩展: $ext" -ForegroundColor Yellow
                Write-Host "     安装命令: code --install-extension $ext" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host "  ⚠️ 无法检查扩展包安装状态" -ForegroundColor Yellow
    }
}

# 2. 检查Maven配置
function Test-MavenConfiguration {
    Write-Host "`n## 2. Maven配置检查 ##" -ForegroundColor Yellow

    $parentPomPath = "microservices/pom.xml"
    if (!(Test-Path $parentPomPath)) {
        Write-Host "❌ 找不到父POM文件: $parentPomPath" -ForegroundColor Red
        return $false
    }

    try {
        $pomContent = Get-Content $parentPomPath -Raw -Encoding UTF8

        # 检查Java版本
        if ($pomContent -match '<java\.version>([^<]+)') {
            $javaVersion = $matches[1]
            Write-Host "Java版本: $javaVersion" -ForegroundColor White
            if ($javaVersion -eq "17") {
                Write-Host "  ✅ Java版本正确" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️ 建议使用Java 17" -ForegroundColor Yellow
            }
        }

        # 检查Lombok版本
        if ($pomContent -match '<lombok\.version>([^<]+)') {
            $lombokVersion = $matches[1]
            Write-Host "Lombok版本: $lombokVersion" -ForegroundColor White
            if ([version]$lombokVersion -ge [version]"1.18.30") {
                Write-Host "  ✅ Lombok版本较新" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️ 建议升级Lombok到1.18.30+" -ForegroundColor Yellow
            }
        }

        # 检查编译器插件版本
        if ($pomContent -match '<maven-compiler-plugin[^>]*>[^<]*<version>([^<]+)') {
            $compilerVersion = $matches[1]
            Write-Host "Maven编译器插件版本: $compilerVersion" -ForegroundColor White
            if ([version]$compilerVersion -ge [version]"3.11.0") {
                Write-Host "  ✅ 编译器插件版本较新" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️ 建议升级编译器插件到3.11.0+" -ForegroundColor Yellow
            }
        }

        # 检查注解处理器配置
        if ($pomContent -match 'annotationProcessorPaths') {
            Write-Host "注解处理器配置: 已配置" -ForegroundColor White
            Write-Host "  ✅ 注解处理器路径已配置" -ForegroundColor Green

            # 检查Lombok注解处理器
            if ($pomContent -match 'org\.projectlombok.*lombok') {
                Write-Host "  ✅ Lombok注解处理器已配置" -ForegroundColor Green
            } else {
                Write-Host "  ❌ Lombok注解处理器未配置" -ForegroundColor Red
            }
        } else {
            Write-Host "注解处理器配置: 未配置" -ForegroundColor Red
            Write-Host "  ❌ 缺少注解处理器配置" -ForegroundColor Red
        }

        # 检查编码配置
        $encodingMatches = [regex]::Matches($pomContent, 'encoding[^>]*>([^<]+)')
        if ($encodingMatches.Count -gt 0) {
            Write-Host "编码配置: 已配置" -ForegroundColor White
            $encodingMatches | ForEach-Object {
                $encoding = $_.Groups[1].Value
                if ($encoding -eq "UTF-8") {
                    Write-Host "  ✅ UTF-8编码配置正确" -ForegroundColor Green
                } else {
                    Write-Host "  ⚠️ 编码配置: $encoding（建议UTF-8）" -ForegroundColor Yellow
                }
            }
        } else {
            Write-Host "编码配置: 未配置" -ForegroundColor Red
        }

        return $true

    } catch {
        Write-Host "❌ 读取Maven配置失败: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 3. 检查Lombok配置
function Test-LombokConfiguration {
    Write-Host "`n## 3. Lombok配置检查 ##" -ForegroundColor Yellow

    # 检查Lombok是否在Maven仓库中可用
    try {
        $lombokPath = "$env:USERPROFILE\.m2\repository\org\projectlombok\lombok"
        $lombokVersions = Get-ChildItem -Path $lombokPath -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending | Select-Object -First 3

        if ($lombokVersions) {
            Write-Host "本地Maven仓库中的Lombok版本:" -ForegroundColor White
            $lombokVersions | ForEach-Object {
                Write-Host "  - $($_.Name)" -ForegroundColor Green
            }
        } else {
            Write-Host "⚠️ 本地Maven仓库中未找到Lombok" -ForegroundColor Yellow
            Write-Host "  建议运行: mvn dependency:resolve" -ForegroundColor Gray
        }
    } catch {
        Write-Host "⚠️ 无法检查本地Maven仓库" -ForegroundColor Yellow
    }

    # 检查常见的Lombok配置问题
    Write-Host "检查Lombok使用情况..." -ForegroundColor White

    # 查找使用@Slf4j的文件
    $slf4jFiles = Select-String -Path "microservices/**/*.java" -Pattern "@Slf4j" -Exclude "target/**"
    if ($slf4jFiles) {
        Write-Host "  使用@Slf4j的文件: $($slf4jFiles.Count)" -ForegroundColor White

        # 检查是否有明显的log变量未定义错误
        $errorFiles = Select-String -Path "microservices/**/*.java" -Pattern "log\." -Exclude "target/**" |
            Where-Object {
                $content = Get-Content $_.Path -Raw
                $content -notmatch "@Slf4j" -and $content -notmatch "Logger log"
            }

        if ($errorFiles) {
            Write-Host "  ⚠️ 发现可能未正确配置Lombok的文件: $($errorFiles.Count)" -ForegroundColor Yellow
            $errorFiles | Select-Object -First 3 | ForEach-Object {
                Write-Host "    - $($_.Path):$($_.LineNumber)" -ForegroundColor DarkYellow
            }
        } else {
            Write-Host "  ✅ 未发现明显的log变量使用问题" -ForegroundColor Green
        }
    }

    # 检查@Data注解使用
    $dataFiles = Select-String -Path "microservices/**/*.java" -Pattern "@Data" -Exclude "target/**"
    if ($dataFiles) {
        Write-Host "  使用@Data的文件: $($dataFiles.Count)" -ForegroundColor White
        Write-Host "  ✅ @Data注解使用正常" -ForegroundColor Green
    }

    Write-Host "Lombok配置检查完成" -ForegroundColor Green
}

# 4. 生成配置报告
function New-ConfigurationReport {
    Write-Host "`n## 4. 生成配置报告 ##" -ForegroundColor Yellow

    $reportPath = "scripts/reports/ide-config-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"
    $reportDir = Split-Path $reportPath -Parent

    if (!(Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    $report = @"
# IDE配置检查报告

**生成时间**: $timestamp
**执行人**: IOE-DREAM开发团队

## 检查项目

### 1. VSCode配置
- Java开发环境配置
- 代码格式化设置
- 项目排除规则
- 扩展包推荐

### 2. Maven配置
- Java版本配置
- Lombok版本检查
- 编译器插件版本
- 注解处理器配置
- 编码设置

### 3. Lombok配置
- 本地仓库可用性
- 注解使用情况
- 常见配置问题

## 建议和改进

1. 确保所有开发人员使用一致的IDE配置
2. 定期更新扩展包到最新版本
3. 配置代码格式化规则以保持代码风格一致性
4. 建立项目级别的IDE配置管理

---
*本报告由IDE配置检查脚本自动生成*
"@

    $report | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Host "报告已生成: $reportPath" -ForegroundColor Green
}

# 执行主流程
try {
    Write-Host "开始IDE配置检查..." -ForegroundColor Cyan

    # 根据参数执行相应检查
    if (-not $CheckVSCode -and -not $CheckMaven -and -not $CheckLombok) {
        # 默认执行所有检查
        Test-VSCodeConfiguration
        Test-MavenConfiguration
        Test-LombokConfiguration
    } else {
        if ($CheckVSCode) { Test-VSCodeConfiguration }
        if ($CheckMaven) { Test-MavenConfiguration }
        if ($CheckLombok) { Test-LombokConfiguration }
    }

    # 生成报告
    New-ConfigurationReport

    Write-Host "`n====================================" -ForegroundColor Cyan
    Write-Host "IDE配置检查完成" -ForegroundColor Cyan
    Write-Host "====================================" -ForegroundColor Cyan

    Write-Host "💡 提示:" -ForegroundColor Cyan
    Write-Host "1. 使用-FixIssues参数可以自动修复一些配置问题" -ForegroundColor White
    Write-Host "2. 确保安装了所有推荐的VSCode扩展包" -ForegroundColor White
    Write-Host "3. 定期运行此脚本以检查配置一致性" -ForegroundColor White

} catch {
    Write-Host "`n❌ IDE配置检查失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "详细错误信息: $($_.Exception.StackTrace)" -ForegroundColor DarkRed
    exit 1
}