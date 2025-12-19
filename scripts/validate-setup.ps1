# IOE-DREAM 环境验证 PowerShell 脚本
# 用于Windows环境下的环境配置验证

param(
    [switch]$Quiet
)

function Write-ColorOutput {
    param(
        [string]$Message,
        [ConsoleColor]$Color = "White"
    )

    if (-not $Quiet) {
        Write-Host $Message -ForegroundColor $Color
    }
}

function Write-Section {
    param([string]$Title)
    Write-ColorOutput "🔍 $Title" "Cyan"
    Write-ColorOutput "======================================" "Cyan"
}

function Write-Success {
    param([string]$Message)
    Write-ColorOutput "✅ $Message" "Green"
}

function Write-Warning {
    param([string]$Message)
    Write-ColorOutput "⚠️  $Message" "Yellow"
}

function Write-Error {
    param([string]$Message)
    Write-ColorOutput "❌ $Message" "Red"
}

# 验证项目结构
Write-Section "检查项目结构"

if (Test-Path "microservices") {
    Write-Success "microservices目录存在"
} else {
    Write-Error "microservices目录不存在"
    exit 1
}

if (Test-Path "microservices\pom.xml") {
    Write-Success "根pom.xml存在"
} else {
    Write-Error "根pom.xml不存在"
    exit 1
}

# 验证Java环境
Write-Section "验证Java环境"

try {
    $javaVersion = & java -version 2>&1
    if ($javaVersion -match '"(\d+)\.(\d+).*" -and $matches[1] -eq "17") {
        Write-Success "Java 17 环境正常"
        Write-ColorOutput "   版本: $javaVersion" "Gray"
    } else {
        Write-Warning "Java版本不是17 (当前版本已安装，但可能不是Java 17)"
    }
} catch {
    Write-Error "未找到Java环境"
}

# 验证Maven环境
Write-Section "验证Maven环境"

try {
    $mavenVersion = & mvn -version | Select-Object -First 1
    Write-Success "Maven环境正常"
    Write-ColorOutput "   版本: $mavenVersion" "Gray"
} catch {
    Write-Error "未找到Maven环境"
}

# 验证CI/CD配置
Write-Section "检查CI/CD配置"

if (Test-Path ".github\workflows\ci-gatekeeper.yml") {
    Write-Success "CI/CD工作流存在"
} else {
    Write-Warning "CI/CD工作流不存在"
}

# 验证Git hooks
Write-Section "检查Git hooks"

if (Test-Path ".git\hooks\pre-commit") {
    Write-Success "Pre-commit hook已安装"
} else {
    Write-Warning "Pre-commit hook未安装"
}

# 验证检查脚本
Write-Section "检查检查脚本"

$scripts = @(
    ".github\scripts\check-structure-consistency.sh",
    ".github\scripts\check-api-contract.sh",
    ".github\scripts\check-governance.sh"
)

$missingScripts = 0
foreach ($script in $scripts) {
    $scriptName = Split-Path $script -Leaf
    if (Test-Path $script) {
        Write-Success "$scriptName 存在"
    } else {
        Write-Error "$scriptName 不存在"
        $missingScripts++
    }
}

# 验证PowerShell脚本
Write-Section "检查PowerShell脚本"

$psScripts = @(
    "scripts\validate-setup.ps1",
    "scripts\run-full-check.ps1",
    "scripts\quick-fix.ps1"
)

foreach ($script in $psScripts) {
    $scriptName = Split-Path $script -Leaf
    if (Test-Path $script) {
        Write-Success "$scriptName 存在"
    } else {
        Write-Warning "$scriptName 不存在"
    }
}

# 运行快速架构检查
Write-Section "运行快速架构检查"

try {
    # 在Windows环境下运行WSL或Git Bash来执行shell脚本
    if (Get-Command wsl -ErrorAction SilentlyContinue) {
        $result = wsl bash ".github/scripts/check-structure-consistency.sh" 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Success "结构一致性检查通过"
        } else {
            Write-Warning "结构一致性检查有问题"
        }
    } elseif (Get-Command bash -ErrorAction SilentlyContinue) {
        $result = bash ".github/scripts/check-structure-consistency.sh" 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Success "结构一致性检查通过"
        } else {
            Write-Warning "结构一致性检查有问题"
        }
    } else {
        Write-Warning "无法运行架构检查（需要WSL或Git Bash）"
    }
} catch {
    Write-Warning "无法运行架构检查: $_"
}

# 验证IDE配置
Write-Section "检查IDE配置"

if (Test-Path ".vscode\settings.json") {
    Write-Success "VS Code配置存在"

    # 检查Java配置
    try {
        $vscodeConfig = Get-Content ".vscode\settings.json" | ConvertFrom-Json
        if ($vscodeConfig.java.home) {
            Write-Success "VS Code Java配置正确"
            Write-ColorOutput "   Java路径: $($vscodeConfig.java.home)" "Gray"
        } else {
            Write-Warning "VS Code Java配置不完整"
        }
    } catch {
        Write-Warning "无法解析VS Code配置"
    }
} else {
    Write-Warning "VS Code配置不存在"
}

if (Test-Path ".idea\modules.xml") {
    Write-Success "IntelliJ IDEA配置存在"
} else {
    Write-Warning "IntelliJ IDEA配置不存在"
}

# 生成验证报告
Write-Section "验证结果总结"

Write-Host ""
if ($missingScripts -eq 0) {
    Write-ColorOutput "🎉 环境配置完整！" "Green"
    Write-Host ""
    Write-ColorOutput "💡 下一步操作：" "Cyan"
    Write-Host "   1. 运行完整检查: .\scripts\run-full-check.ps1"
    Write-Host "   2. 提交代码时会自动运行pre-commit检查"
    Write-Host "   3. CI/CD会在push时自动运行架构检查"
    Write-Host "   4. 查看 .\ARCHITECTURE_GATEKEEPER.md 了解详细信息"
} else {
    Write-ColorOutput "⚠️  发现 $missingScripts 个配置问题" "Yellow"
    Write-Host ""
    Write-ColorOutput "💡 修复建议：" "Cyan"
    Write-Host "   1. 运行安装脚本: .\scripts\setup-gatekeeper.sh"
    Write-Host "   2. 重新运行此验证脚本"
    Write-Host "   3. 根据提示修复缺失的配置文件"
}

Write-Host ""
Write-ColorOutput "📋 验证完成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss UTC')" "Gray"