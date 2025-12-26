param(
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"

function Write-Section {
    param([string]$Title)
    Write-Host "" 
    Write-Host "==== $Title ===="
}

function Test-Command {
    param([string]$Name)
    $cmd = Get-Command $Name -ErrorAction SilentlyContinue
    return $null -ne $cmd
}

function Require-Command {
    param([string]$Name)
    if (-not (Test-Command $Name)) {
        Write-Host "[缺失] $Name"
        return $false
    }
    Write-Host "[OK] $Name"
    return $true
}

$failed = $false

Write-Section "基础命令"
if (-not (Require-Command "git")) { $failed = $true }
if (-not (Require-Command "java")) { $failed = $true }
if (-not (Require-Command "mvn")) { $failed = $true }
if (-not (Require-Command "pwsh")) { $failed = $true }
if (-not (Require-Command "docker")) { $failed = $true }

Write-Section "JDK 版本"
try {
    $javaVersionOutput = & java -version 2>&1
    $javaVersionLine = $javaVersionOutput | Select-Object -First 1
    Write-Host $javaVersionLine
    if ($javaVersionLine -notmatch "\"17") {
        Write-Host "[异常] 需要 Java 17"
        $failed = $true
    } else {
        Write-Host "[OK] Java 17"
    }
} catch {
    Write-Host "[异常] 无法获取 java 版本"
    $failed = $true
}

Write-Section "Docker Buildx"
try {
    & docker buildx version | Out-Null
    Write-Host "[OK] docker buildx"
} catch {
    Write-Host "[异常] docker buildx 不可用"
    $failed = $true
}

Write-Section "可选工具"
if (Test-Command "trivy") {
    Write-Host "[OK] trivy"
} else {
    Write-Host "[跳过] trivy 未安装"
}

if (Test-Command "sonar-scanner") {
    Write-Host "[OK] sonar-scanner"
} else {
    Write-Host "[跳过] sonar-scanner 未安装"
}

Write-Section "环境变量"
$requiredEnv = @("GITHUB_ENV", "GITHUB_PATH", "GITHUB_OUTPUT")
foreach ($name in $requiredEnv) {
    $value = [Environment]::GetEnvironmentVariable($name)
    if ([string]::IsNullOrWhiteSpace($value)) {
        Write-Host "[警告] $name 未设置（仅在 CI 运行时注入）"
    } else {
        Write-Host "[OK] $name"
    }
}

Write-Section "结果"
if ($failed) {
    Write-Host "环境检查未通过"
    exit 1
}

Write-Host "环境检查通过"
