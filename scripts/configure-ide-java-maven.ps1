# IDE Java & Maven 自动配置脚本
# 创建时间: 2025-01-30
# 用途: 自动配置 Cursor/VS Code 的 Java 和 Maven 设置

param(
    [switch]$CheckOnly,
    [switch]$Force
)

Write-Host "=== IDE Java & Maven 配置工具 ===" -ForegroundColor Cyan
Write-Host ""

# 检查模式
if ($CheckOnly) {
    Write-Host "检查模式: 仅诊断环境，不进行配置" -ForegroundColor Yellow
    Write-Host ""
    
    # 检查 Java
    Write-Host "1. Java 环境检查:" -ForegroundColor Yellow
    try {
        $javaVersion = java -version 2>&1 | Select-String "version"
        if ($javaVersion) {
            Write-Host "   ✓ Java 已安装" -ForegroundColor Green
            Write-Host "   $javaVersion" -ForegroundColor Gray
            
            # 检查版本
            if ($javaVersion -match 'version "17') {
                Write-Host "   ✓ Java 版本符合要求 (17)" -ForegroundColor Green
            } else {
                Write-Host "   ⚠ Java 版本不符合要求，需要 Java 17" -ForegroundColor Yellow
            }
        } else {
            Write-Host "   ✗ Java 未安装或不在 PATH 中" -ForegroundColor Red
        }
    } catch {
        Write-Host "   ✗ 无法检查 Java: $_" -ForegroundColor Red
    }
    
    # 检查 JAVA_HOME
    Write-Host "`n2. JAVA_HOME 检查:" -ForegroundColor Yellow
    if ($env:JAVA_HOME) {
        Write-Host "   ✓ JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
        if (Test-Path $env:JAVA_HOME) {
            Write-Host "   ✓ JAVA_HOME 路径有效" -ForegroundColor Green
        } else {
            Write-Host "   ✗ JAVA_HOME 路径不存在" -ForegroundColor Red
        }
    } else {
        Write-Host "   ⚠ JAVA_HOME 未设置" -ForegroundColor Yellow
    }
    
    # 检查 Maven
    Write-Host "`n3. Maven 环境检查:" -ForegroundColor Yellow
    try {
        $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
        if ($mavenVersion) {
            Write-Host "   ✓ Maven 已安装" -ForegroundColor Green
            Write-Host "   $mavenVersion" -ForegroundColor Gray
            
            # 检查版本
            if ($mavenVersion -match 'Apache Maven 3\.(9|1[0-9])') {
                Write-Host "   ✓ Maven 版本符合要求 (3.9+)" -ForegroundColor Green
            } else {
                Write-Host "   ⚠ Maven 版本可能不符合要求，推荐 3.9+" -ForegroundColor Yellow
            }
        } else {
            Write-Host "   ✗ Maven 未安装或不在 PATH 中" -ForegroundColor Red
        }
    } catch {
        Write-Host "   ✗ 无法检查 Maven: $_" -ForegroundColor Red
    }
    
    # 检查 Maven 使用的 Java
    Write-Host "`n4. Maven Java 配置检查:" -ForegroundColor Yellow
    try {
        $mavenJava = mvn -version 2>&1 | Select-String "Java version"
        if ($mavenJava) {
            Write-Host "   $mavenJava" -ForegroundColor Gray
            if ($mavenJava -match 'Java version: 17') {
                Write-Host "   ✓ Maven 使用 Java 17" -ForegroundColor Green
            } else {
                Write-Host "   ⚠ Maven 未使用 Java 17" -ForegroundColor Yellow
            }
        }
    } catch {
        Write-Host "   ⚠ 无法检查 Maven Java 配置" -ForegroundColor Yellow
    }
    
    # 检查 IDE 配置
    Write-Host "`n5. IDE 配置检查:" -ForegroundColor Yellow
    $vscodeSettings = ".vscode\settings.json"
    if (Test-Path $vscodeSettings) {
        Write-Host "   ✓ .vscode/settings.json 已存在" -ForegroundColor Green
        $settings = Get-Content $vscodeSettings -Raw | ConvertFrom-Json -ErrorAction SilentlyContinue
        if ($settings) {
            if ($settings.'java.configuration.runtimes') {
                Write-Host "   ✓ Java Runtime 已配置" -ForegroundColor Green
            } else {
                Write-Host "   ⚠ Java Runtime 未配置" -ForegroundColor Yellow
            }
            if ($settings.'maven.executable.path') {
                Write-Host "   ✓ Maven 路径已配置" -ForegroundColor Green
            } else {
                Write-Host "   ⚠ Maven 路径未配置" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "   ⚠ .vscode/settings.json 不存在" -ForegroundColor Yellow
    }
    
    Write-Host "`n检查完成！" -ForegroundColor Green
    exit 0
}

# 获取当前 Java 和 Maven 路径
$javaHome = $env:JAVA_HOME
$mavenPath = $null

# 查找 Maven 路径
try {
    $mavenVersionOutput = mvn -version 2>&1
    $mavenHomeLine = $mavenVersionOutput | Select-String "Maven home:"
    if ($mavenHomeLine) {
        $mavenPath = ($mavenHomeLine -split "Maven home: ")[1].Trim()
    }
} catch {
    Write-Host "⚠ 无法自动检测 Maven 路径" -ForegroundColor Yellow
}

# 如果未找到，尝试常见路径
if (-not $mavenPath) {
    $commonMavenPaths = @(
        "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.11\bin\mvn.cmd",
        "C:\Program Files\Apache\maven\bin\mvn.cmd",
        "$env:USERPROFILE\apache-maven\bin\mvn.cmd"
    )
    
    foreach ($path in $commonMavenPaths) {
        if (Test-Path $path) {
            $mavenPath = $path
            break
        }
    }
}

# 验证路径
if (-not $javaHome -or -not (Test-Path $javaHome)) {
    Write-Host "❌ 错误: 未找到有效的 JAVA_HOME" -ForegroundColor Red
    Write-Host "   请先设置 JAVA_HOME 环境变量" -ForegroundColor Yellow
    exit 1
}

if (-not $mavenPath -or -not (Test-Path $mavenPath)) {
    Write-Host "⚠ 警告: 未找到 Maven 可执行文件" -ForegroundColor Yellow
    Write-Host "   将跳过 Maven 路径配置" -ForegroundColor Yellow
}

Write-Host "检测到的配置:" -ForegroundColor Cyan
Write-Host "  JAVA_HOME: $javaHome" -ForegroundColor Gray
if ($mavenPath) {
    Write-Host "  Maven: $mavenPath" -ForegroundColor Gray
}
Write-Host ""

# 创建 .vscode 目录
$vscodeDir = ".vscode"
if (-not (Test-Path $vscodeDir)) {
    New-Item -ItemType Directory -Path $vscodeDir -Force | Out-Null
    Write-Host "✓ 创建 .vscode 目录" -ForegroundColor Green
}

# 读取现有配置
$settingsFile = "$vscodeDir\settings.json"
$existingSettings = @{}

if (Test-Path $settingsFile) {
    try {
        $existingContent = Get-Content $settingsFile -Raw
        $existingSettings = $existingContent | ConvertFrom-Json -AsHashtable -ErrorAction SilentlyContinue
        if ($existingSettings) {
            Write-Host "✓ 读取现有配置" -ForegroundColor Green
        }
    } catch {
        Write-Host "⚠ 无法解析现有配置，将创建新配置" -ForegroundColor Yellow
    }
}

# 合并配置
$settings = $existingSettings.Clone()

# Java 配置
$javaRuntimes = @(
    @{
        name = "JavaSE-17"
        path = $javaHome
        default = $true
    }
)
$settings['java.configuration.runtimes'] = $javaRuntimes
$settings['java.compile.nullAnalysis.mode'] = "automatic"
$settings['java.configuration.updateBuildConfiguration'] = "automatic"

# Maven 配置
if ($mavenPath) {
    $settings['maven.executable.path'] = $mavenPath
    $settings['maven.terminal.useJavaHome'] = $true
}

# 编码配置
$settings['files.encoding'] = "utf8"
$settings['files.eol'] = "`n"

# Java 项目配置
$settings['java.project.sourcePaths'] = @("src/main/java")
$settings['java.project.outputPath'] = "target/classes"
$settings['java.project.referencedLibraries'] = @(
    "target/**/*.jar",
    "lib/**/*.jar"
)

# 文件排除
if (-not $settings['files.exclude']) {
    $settings['files.exclude'] = @{}
}
$settings['files.exclude']['**/target'] = $true
$settings['files.exclude']['**/.classpath'] = $true
$settings['files.exclude']['**/.project'] = $true
$settings['files.exclude']['**/.settings'] = $true
$settings['files.exclude']['**/.factorypath'] = $true

# Spring Boot 配置
$settings['spring-boot.ls.java.home'] = $javaHome

# 保存配置
try {
    $jsonContent = $settings | ConvertTo-Json -Depth 10
    $jsonContent | Out-File -FilePath $settingsFile -Encoding UTF8 -Force
    Write-Host "✓ 配置已保存到 $settingsFile" -ForegroundColor Green
} catch {
    Write-Host "❌ 保存配置失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 配置完成 ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "1. 重启 Cursor IDE 使配置生效" -ForegroundColor White
Write-Host "2. 打开任意 Java 文件，等待 Java 扩展初始化" -ForegroundColor White
Write-Host "3. 运行 'Java: Clean Java Language Server Workspace' 命令（如果需要）" -ForegroundColor White
Write-Host "4. 验证配置: .\scripts\configure-ide-java-maven.ps1 -CheckOnly" -ForegroundColor White
Write-Host ""
