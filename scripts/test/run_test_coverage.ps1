# 测试覆盖率检查脚本 (Windows PowerShell版本)
# 用于运行测试并生成覆盖率报告

param(
    [string]$Service = "",
    [int]$CoverageTarget = 80,
    [switch]$SkipTests = $false
)

# 设置错误处理
$ErrorActionPreference = "Stop"

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    
    $colorMap = @{
        "Red" = "Red"
        "Green" = "Green"
        "Yellow" = "Yellow"
        "White" = "White"
    }
    
    Write-Host $Message -ForegroundColor $colorMap[$Color]
}

# 项目根目录
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location $ProjectRoot

Write-ColorOutput "========================================" "Green"
Write-ColorOutput "IOE-DREAM 测试覆盖率检查" "Green"
Write-ColorOutput "========================================" "Green"
Write-Host ""

# 检查Maven是否安装
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-ColorOutput "错误: Maven未安装，请先安装Maven" "Red"
    exit 1
}

# 检查JaCoCo插件是否配置
Write-ColorOutput "检查JaCoCo配置..." "Yellow"
$pomPath = Join-Path $ProjectRoot "microservices\pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw
    if ($pomContent -notmatch "jacoco-maven-plugin") {
        Write-ColorOutput "警告: 未找到JaCoCo插件配置，将使用默认配置" "Yellow"
    }
}

# 服务列表
$Services = @(
    "microservices\ioedream-consume-service",
    "microservices\ioedream-access-service",
    "microservices\ioedream-attendance-service",
    "microservices\ioedream-visitor-service",
    "microservices\ioedream-video-service",
    "microservices\ioedream-common-service",
    "microservices\ioedream-device-comm-service",
    "microservices\ioedream-oa-service"
)

# 如果指定了服务，只测试该服务
if ($Service -ne "") {
    $Services = @("microservices\$Service")
}

# 运行测试并生成覆盖率报告
Write-ColorOutput "开始运行测试..." "Green"
Write-Host ""

$totalCoverage = 0
$serviceCount = 0
$failedServices = @()

foreach ($servicePath in $Services) {
    $fullPath = Join-Path $ProjectRoot $servicePath
    if (-not (Test-Path $fullPath)) {
        Write-ColorOutput "跳过: $servicePath 不存在" "Yellow"
        continue
    }
    
    $serviceName = Split-Path -Leaf $servicePath
    Write-ColorOutput "----------------------------------------" "Green"
    Write-ColorOutput "测试服务: $serviceName" "Green"
    Write-ColorOutput "----------------------------------------" "Green"
    
    Set-Location $fullPath
    
    try {
        if ($SkipTests) {
            Write-ColorOutput "跳过测试执行" "Yellow"
        } else {
            # 运行测试并生成覆盖率报告
            mvn clean test jacoco:report -DskipTests=$SkipTests
            
            if ($LASTEXITCODE -ne 0) {
                throw "测试执行失败"
            }
        }
        
        # 检查覆盖率报告
        $reportPath = Join-Path $fullPath "target\site\jacoco\index.html"
        if (Test-Path $reportPath) {
            Write-ColorOutput "覆盖率报告已生成: $reportPath" "Green"
            
            # 尝试解析覆盖率（如果JaCoCo生成了XML报告）
            $xmlReportPath = Join-Path $fullPath "target\site\jacoco\jacoco.xml"
            if (Test-Path $xmlReportPath) {
                [xml]$xmlReport = Get-Content $xmlReportPath
                $coverage = $xmlReport.report.counter | Where-Object { $_.type -eq "INSTRUCTION" }
                if ($coverage) {
                    $missed = [int]$coverage.missed
                    $covered = [int]$coverage.covered
                    $total = $missed + $covered
                    if ($total -gt 0) {
                        $coveragePercent = [math]::Round(($covered / $total) * 100, 2)
                        Write-ColorOutput "代码覆盖率: $coveragePercent%" "Green"
                        
                        if ($coveragePercent -lt $CoverageTarget) {
                            Write-ColorOutput "警告: 覆盖率低于目标 $CoverageTarget%" "Yellow"
                        }
                        
                        $totalCoverage += $coveragePercent
                        $serviceCount++
                    }
                }
            }
        } else {
            Write-ColorOutput "警告: 未找到覆盖率报告" "Yellow"
        }
        
    } catch {
        Write-ColorOutput "错误: $serviceName 测试失败: $_" "Red"
        $failedServices += $serviceName
    }
    
    Write-Host ""
}

Set-Location $ProjectRoot

# 输出总结
Write-ColorOutput "========================================" "Green"
Write-ColorOutput "测试覆盖率检查完成" "Green"
Write-ColorOutput "========================================" "Green"
Write-Host ""

if ($serviceCount -gt 0) {
    $averageCoverage = [math]::Round($totalCoverage / $serviceCount, 2)
    Write-ColorOutput "平均覆盖率: $averageCoverage%" "Green"
    Write-ColorOutput "目标覆盖率: $CoverageTarget%" "Green"
    
    if ($averageCoverage -lt $CoverageTarget) {
        Write-ColorOutput "警告: 平均覆盖率低于目标" "Yellow"
        exit 1
    }
}

if ($failedServices.Count -gt 0) {
    Write-ColorOutput "失败的服务: $($failedServices -join ', ')" "Red"
    exit 1
}

Write-ColorOutput "所有测试通过！" "Green"
exit 0

