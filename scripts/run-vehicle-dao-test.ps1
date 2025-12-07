# ===================================================================
# VehicleDaoTest 测试执行脚本
# ===================================================================

param(
    [switch]$Coverage,
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# 项目根目录
$PROJECT_ROOT = Split-Path -Parent $PSScriptRoot
$TEST_MODULE = Join-Path $PROJECT_ROOT "microservices\microservices-common"
$REPORT_DIR = Join-Path $PROJECT_ROOT "test-reports"
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"

# 创建报告目录
if (-not (Test-Path $REPORT_DIR)) {
    New-Item -ItemType Directory -Path $REPORT_DIR | Out-Null
}

Write-Info "=========================================="
Write-Info "VehicleDaoTest 测试执行"
Write-Info "=========================================="
Write-Info "测试模块: microservices-common"
Write-Info "测试类: VehicleDaoTest"
Write-Info "时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Info ""

# 检查Maven是否可用
Write-Info "检查Maven环境..."
$mvnCmd = $null
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mvnCmd = "mvn"
    Write-Success "找到Maven命令: mvn"
} elseif (Test-Path (Join-Path $PROJECT_ROOT "mvnw.cmd")) {
    $mvnCmd = Join-Path $PROJECT_ROOT "mvnw.cmd"
    Write-Success "找到Maven Wrapper: mvnw.cmd"
} else {
    Write-Error "未找到Maven，请安装Maven或使用Maven Wrapper"
    exit 1
}

# 切换到测试模块目录
Push-Location $TEST_MODULE
try {
    Write-Info "当前目录: $(Get-Location)"
    Write-Info ""

    # 构建测试命令
    $testCmd = "$mvnCmd test -Dtest=VehicleDaoTest"
    if ($Coverage) {
        $testCmd += " jacoco:report"
        Write-Info "启用代码覆盖率报告"
    }
    if ($Verbose) {
        $testCmd += " -X"
        Write-Info "启用详细输出"
    }

    Write-Info "执行测试命令: $testCmd"
    Write-Info ""

    # 执行测试
    $testOutput = Invoke-Expression $testCmd 2>&1
    $exitCode = $LASTEXITCODE

    # 保存测试输出
    $outputFile = Join-Path $REPORT_DIR "vehicle-dao-test-$TIMESTAMP.log"
    $testOutput | Out-File -FilePath $outputFile -Encoding UTF8

    Write-Info ""
    Write-Info "测试输出已保存到: $outputFile"

    # 显示测试结果摘要
    Write-Info ""
    Write-Info "=========================================="
    Write-Info "测试执行结果"
    Write-Info "=========================================="

    if ($exitCode -eq 0) {
        Write-Success "测试执行成功！"

        # 查找测试报告
        $surefireReport = Join-Path $TEST_MODULE "target\surefire-reports"
        if (Test-Path $surefireReport) {
            Write-Info "测试报告位置: $surefireReport"

            # 查找XML报告
            $xmlReports = Get-ChildItem -Path $surefireReport -Filter "*.xml" -ErrorAction SilentlyContinue
            if ($xmlReports) {
                Write-Info "找到测试报告文件:"
                foreach ($report in $xmlReports) {
                    Write-Info "  - $($report.Name)"
                }
            }
        }

        if ($Coverage) {
            $coverageReport = Join-Path $TEST_MODULE "target\site\jacoco\index.html"
            if (Test-Path $coverageReport) {
                Write-Success "代码覆盖率报告: $coverageReport"
            }
        }
    } else {
        Write-Error "测试执行失败，退出码: $exitCode"
        Write-Info "请查看详细日志: $outputFile"
    }

    Write-Info ""
    Write-Info "=========================================="

    # 显示最后50行输出
    Write-Info ""
    Write-Info "测试输出摘要（最后50行）:"
    Write-Info "----------------------------------------"
    $testOutput | Select-Object -Last 50 | ForEach-Object { Write-Host $_ }

} catch {
    Write-Error "测试执行异常: $_"
    exit 1
} finally {
    Pop-Location
}

Write-Info ""
Write-Info "测试执行完成！"
