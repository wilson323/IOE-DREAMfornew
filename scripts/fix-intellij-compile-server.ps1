# IOE-DREAM IntelliJ IDEA 编译服务器修复脚本
# 功能：修复编译服务器连接超时问题
# 作者：AI Assistant
# 日期：2025-01-30

$ErrorActionPreference = "Continue"
$workspaceRoot = $PSScriptRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IntelliJ IDEA 编译服务器修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 检查编译服务器进程
Write-Host "[步骤1] 检查编译服务器进程..." -ForegroundColor Yellow

$compileServerProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*jps-launcher*" -or
    $_.CommandLine -like "*compile-server*" -or
    $_.Path -like "*IntelliJ*"
}

if ($compileServerProcesses) {
    Write-Host "  发现编译服务器进程:" -ForegroundColor Yellow
    foreach ($proc in $compileServerProcesses) {
        Write-Host "    PID: $($proc.Id) - $($proc.ProcessName)" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "  是否终止这些进程? (Y/N): " -ForegroundColor Yellow -NoNewline
    $response = Read-Host
    if ($response -eq "Y" -or $response -eq "y") {
        foreach ($proc in $compileServerProcesses) {
            try {
                Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
                Write-Host "    ✓ 已终止进程 PID: $($proc.Id)" -ForegroundColor Green
            } catch {
                Write-Host "    ✗ 无法终止进程 PID: $($proc.Id): $_" -ForegroundColor Red
            }
        }
        Start-Sleep -Seconds 2
    }
} else {
    Write-Host "  ✓ 没有发现编译服务器进程" -ForegroundColor Green
}

Write-Host ""

# 步骤2: 检查端口占用
Write-Host "[步骤2] 检查端口占用..." -ForegroundColor Yellow

$ports = @(56742, 56743, 56744, 56745)  # 常见的编译服务器端口
$occupiedPorts = @()

foreach ($port in $ports) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        Write-Host "  ✗ 端口 $port 被占用 (PID: $($connection.OwningProcess))" -ForegroundColor Red
        $occupiedPorts += $port
    } else {
        Write-Host "  ✓ 端口 $port 可用" -ForegroundColor Green
    }
}

if ($occupiedPorts.Count -gt 0) {
    Write-Host ""
    Write-Host "  是否终止占用端口的进程? (Y/N): " -ForegroundColor Yellow -NoNewline
    $response = Read-Host
    if ($response -eq "Y" -or $response -eq "y") {
        foreach ($port in $occupiedPorts) {
            $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
            if ($connection) {
                try {
                    Stop-Process -Id $connection.OwningProcess -Force -ErrorAction SilentlyContinue
                    Write-Host "    ✓ 已终止占用端口 $port 的进程" -ForegroundColor Green
                } catch {
                    Write-Host "    ✗ 无法终止进程: $_" -ForegroundColor Red
                }
            }
        }
        Start-Sleep -Seconds 2
    }
}

Write-Host ""

# 步骤3: 清理编译服务器缓存
Write-Host "[步骤3] 清理编译服务器缓存..." -ForegroundColor Yellow

$compileServerDirs = @(
    "$env:LOCALAPPDATA\JetBrains\IntelliJIdea2025.2\compile-server",
    "$env:LOCALAPPDATA\JetBrains\IntelliJIdea2025.2\log\build-log",
    "$env:LOCALAPPDATA\JetBrains\IntelliJIdea2025.2\caches"
)

foreach ($dir in $compileServerDirs) {
    if (Test-Path $dir) {
        try {
            Write-Host "  清理目录: $dir" -ForegroundColor Gray
            Get-ChildItem -Path $dir -Recurse -ErrorAction SilentlyContinue | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
            Write-Host "    ✓ 已清理" -ForegroundColor Green
        } catch {
            Write-Host "    ⚠ 清理失败: $_" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✓ 目录不存在: $dir" -ForegroundColor Gray
    }
}

Write-Host ""

# 步骤4: 清理项目编译缓存
Write-Host "[步骤4] 清理项目编译缓存..." -ForegroundColor Yellow

$projectCacheDirs = @(
    "$workspaceRoot\.idea\workspace.xml",
    "$workspaceRoot\out",
    "$workspaceRoot\target"
)

foreach ($item in $projectCacheDirs) {
    if (Test-Path $item) {
        try {
            if (Test-Path $item -PathType Container) {
                Write-Host "  清理目录: $item" -ForegroundColor Gray
                Get-ChildItem -Path $item -Recurse -ErrorAction SilentlyContinue | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
            } else {
                Write-Host "  清理文件: $item" -ForegroundColor Gray
                Remove-Item -Path $item -Force -ErrorAction SilentlyContinue
            }
            Write-Host "    ✓ 已清理" -ForegroundColor Green
        } catch {
            Write-Host "    ⚠ 清理失败: $_" -ForegroundColor Yellow
        }
    }
}

Write-Host ""

# 步骤5: 检查Maven配置
Write-Host "[步骤5] 检查Maven配置..." -ForegroundColor Yellow

$mavenHome = $env:MAVEN_HOME
if (-not $mavenHome) {
    $mavenHome = $env:M2_HOME
}

if ($mavenHome) {
    Write-Host "  ✓ Maven Home: $mavenHome" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Maven Home 未设置（使用系统PATH中的Maven）" -ForegroundColor Yellow
}

# 检查Maven是否可用
try {
    $mavenVersion = & mvn -version 2>&1 | Select-Object -First 1
    Write-Host "  ✓ Maven版本: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Maven不可用: $_" -ForegroundColor Red
}

Write-Host ""

# 步骤6: 生成修复建议
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复建议" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "1. 在IntelliJ IDEA中执行以下操作:" -ForegroundColor Yellow
Write-Host "   a) File → Invalidate Caches / Restart..." -ForegroundColor White
Write-Host "   b) 选择 'Invalidate and Restart'" -ForegroundColor White
Write-Host "   c) 等待IDE重启并重新索引项目" -ForegroundColor White
Write-Host ""

Write-Host "2. 重新导入Maven项目:" -ForegroundColor Yellow
Write-Host "   a) 打开 Maven 工具窗口 (View → Tool Windows → Maven)" -ForegroundColor White
Write-Host "   b) 点击 'Reload All Maven Projects' 按钮" -ForegroundColor White
Write-Host "   c) 等待Maven项目重新导入完成" -ForegroundColor White
Write-Host ""

Write-Host "3. 配置Maven Runner:" -ForegroundColor Yellow
Write-Host "   a) File → Settings → Build, Execution, Deployment → Build Tools → Maven → Runner" -ForegroundColor White
Write-Host "   b) 取消勾选 'Delegate IDE build/run actions to Maven'" -ForegroundColor White
Write-Host "   c) 或者勾选 'Delegate IDE build/run actions to Maven' 使用Maven构建" -ForegroundColor White
Write-Host ""

Write-Host "4. 如果问题仍然存在，尝试以下方法:" -ForegroundColor Yellow
Write-Host "   a) 关闭IntelliJ IDEA" -ForegroundColor White
Write-Host "   b) 删除项目目录下的 .idea 文件夹（备份后删除）" -ForegroundColor White
Write-Host "   c) 重新打开项目，让IDE重新生成配置" -ForegroundColor White
Write-Host ""

Write-Host "5. 使用命令行构建验证:" -ForegroundColor Yellow
Write-Host "   cd $workspaceRoot" -ForegroundColor White
Write-Host "   mvn clean install -pl microservices/microservices-common -am -DskipTests" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
