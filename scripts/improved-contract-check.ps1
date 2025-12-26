# IOE-DREAM 改进的契约检查脚本
# 更精确地检查前后端数据模型一致性

param(
    [switch]$Help
)

function Show-Usage {
    Write-Host "IOE-DREAM 改进的契约检查脚本" -ForegroundColor Green
    Write-Host ""
    Write-Host "用法:" -ForegroundColor Yellow
    Write-Host "  .\scripts\improved-contract-check.ps1"
    Write-Host ""
}

if ($Help) {
    Show-Usage
    exit 0
}

$timestamp = Get-Date -Format "HH:mm:ss"
Write-Host "[$timestamp] 🚀 开始改进的契约检查..." -ForegroundColor Green

# 检查结果
$issues = @()
$warnings = @()
$success = @()

Write-Host "[$timestamp] 📂 检查API路径一致性..." -ForegroundColor Cyan

# 检查前端API文件
$frontendApiFiles = @(
    "smart-admin-web-javascript\src\api\business\access\access-api.js",
    "smart-admin-web-javascript\src\api\business\attendance\attendance-api.js",
    "smart-admin-web-javascript\src\api\business\consume\consume-api.js",
    "smart-admin-web-javascript\src\api\business\video\video-api.js"
)

foreach ($apiFile in $frontendApiFiles) {
    if (Test-Path $apiFile) {
        $content = Get-Content $apiFile -Raw

        # 检查API路径前缀
        if ($content -match "/api/v1/") {
            $success += "✅ $((Split-Path $apiFile -Leaf)) 使用正确的 /api/v1/ 前缀"
        } else {
            $issues += "❌ $((Split-Path $apiFile -Leaf)) 未使用 /api/v1/ 前缀"
        }
    } else {
        $warnings += "⚠️ 前端API文件不存在: $apiFile"
    }
}

# 检查移动端API文件
$mobileApiFiles = @(
    "smart-app\api\access.js",
    "smart-app\api\workflow.js"
)

foreach ($apiFile in $mobileApiFiles) {
    if (Test-Path $apiFile) {
        $content = Get-Content $apiFile -Raw

        # 检查移动端API路径前缀
        if ($content -match "/api/mobile/v1/" -or $content -match "/api/v1/") {
            $success += "✅ $((Split-Path $apiFile -Leaf)) 使用正确的API前缀"
        } else {
            $issues += "❌ $((Split-Path $apiFile -Leaf)) 未使用标准化API前缀"
        }
    } else {
        $warnings += "⚠️ 移动端API文件不存在: $apiFile"
    }
}

Write-Host "[$timestamp] 📋 检查核心模型字段匹配..." -ForegroundColor Cyan

# 检查核心业务模型的关键字段，使用实际存在的实体文件
$models = @(
    @{
        Name = "Device";
        FrontendFields = @("deviceId", "deviceName", "deviceCode", "deviceType", "status");
        BackendFile = "microservices\microservices-common-entity\src\main\java\net\lab1024\sa\common\organization\entity\DeviceEntity.java"
    },
    @{
        Name = "AttendanceRecord";
        FrontendFields = @("recordId", "userId", "punchTime", "punchType", "deviceId");
        BackendFile = "microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\entity\AttendanceRecordEntity.java"
    },
    @{
        Name = "ConsumeTransaction";
        FrontendFields = @("transactionId", "userId", "consumeAmount", "consumeTime", "deviceId");
        BackendFile = "microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\entity\ConsumeTransactionEntity.java"
    },
    @{
        Name = "VideoDevice";
        FrontendFields = @("deviceId", "deviceName", "ipAddress", "port", "status");
        BackendFile = "microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\entity\VideoDeviceEntity.java"
    }
)

foreach ($model in $models) {
    Write-Host "[$timestamp]   检查模型: $($model.Name)" -ForegroundColor Gray

    # 检查后端实体文件是否存在
    if (Test-Path $model.BackendFile) {
        $content = Get-Content $model.BackendFile -Raw -Encoding UTF8
        $foundFields = @()

        foreach ($field in $model.FrontendFields) {
            if ($content -match "\b$field\b") {
                $foundFields += $field
            }
        }

        $matchRate = [math]::Round(($foundFields.Count / $model.FrontendFields.Count) * 100, 1)

        if ($matchRate -ge 60) {
            $success += "✅ $($model.Name) 模型字段匹配良好 ($matchRate%) - $((Split-Path $model.BackendFile -Leaf))"
        } else {
            $warnings += "⚠️ $($model.Name) 模型字段匹配度较低 ($matchRate%) - $((Split-Path $model.BackendFile -Leaf))"
        }
    } else {
        $warnings += "⚠️ 实体文件不存在: $($model.BackendFile)"
    }
}

Write-Host "[$timestamp] 🔍 检查HTTP方法一致性..." -ForegroundColor Cyan

# 检查关键Controller的HTTP方法
$controllerFiles = @(
    "microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java",
    "microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\controller\AttendanceRecordController.java",
    "microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\controller\VideoDeviceController.java"
)

foreach ($controllerFile in $controllerFiles) {
    if (Test-Path $controllerFile) {
        $content = Get-Content $controllerFile -Raw -Encoding UTF8

        # 检查查询接口是否使用POST方法（复杂查询推荐）
        if ($content -match '@PostMapping.*query') {
            $success += "✅ $((Split-Path $controllerFile -Leaf)) 查询接口使用POST方法（推荐）"
        } elseif ($content -match '@GetMapping.*query') {
            $warnings += "⚠️ $((Split-Path $controllerFile -Leaf)) 查询接口使用GET方法（建议改为POST）"
        }

        # 检查是否有正确的错误处理
        if ($content -match 'ResponseDTO\.error') {
            $success += "✅ $((Split-Path $controllerFile -Leaf)) 使用统一错误响应格式"
        } else {
            $warnings += "⚠️ $((Split-Path $controllerFile -Leaf)) 可能缺少统一错误处理"
        }
    } else {
        $warnings += "⚠️ Controller文件不存在: $controllerFile"
    }
}

Write-Host "[$timestamp] 🔍 检查Form和VO模型一致性..." -ForegroundColor Cyan

# 检查Form和VO模型
$formFiles = @(
    "microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\form\AccessDeviceQueryForm.java",
    "microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\domain\form\AttendanceRecordQueryForm.java",
    "microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\domain\form\VideoDeviceQueryForm.java"
)

foreach ($formFile in $formFiles) {
    if (Test-Path $formFile) {
        $content = Get-Content $formFile -Raw -Encoding UTF8

        if ($content -match 'private.*Integer.*pageNum' -and $content -match 'private.*Integer.*pageSize') {
            $success += "✅ $((Split-Path $formFile -Leaf)) 包含标准分页字段"
        } else {
            $warnings += "⚠️ $((Split-Path $formFile -Leaf)) 可能缺少标准分页字段"
        }
    }
}

Write-Host "[$timestamp] 📊 生成检查报告..." -ForegroundColor Cyan

# 生成报告
$report = @{
    Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Summary = @{
        Success = $success.Count
        Warnings = $warnings.Count
        Errors = $issues.Count
        Total = $success.Count + $warnings.Count + $issues.Count
    }
    Results = @{
        Success = $success
        Warnings = $warnings
        Errors = $issues
    }
}

$reportPath = "improved-contract-check-report.json"
$report | ConvertTo-Json -Depth 3 | Out-File -FilePath $reportPath -Encoding UTF8

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "   🎉 改进的契约检查完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host ""
Write-Host "📈 检查统计:" -ForegroundColor Yellow
Write-Host "   ✅ 成功: $($report.Summary.Success)" -ForegroundColor Green
Write-Host "   ⚠️  警告: $($report.Summary.Warnings)" -ForegroundColor Yellow
Write-Host "   ❌ 错误: $($report.Summary.Errors)" -ForegroundColor Red
Write-Host "   📋 总计: $($report.Summary.Total)" -ForegroundColor Cyan

if ($report.Summary.Errors -gt 0) {
    Write-Host ""
    Write-Host "❌ 发现的问题:" -ForegroundColor Red
    foreach ($error in $issues) {
        Write-Host "   $error" -ForegroundColor Red
    }
}

if ($report.Summary.Warnings -gt 0) {
    Write-Host ""
    Write-Host "⚠️ 警告信息:" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host "   $warning" -ForegroundColor Yellow
    }
}

if ($report.Summary.Success -gt 0) {
    Write-Host ""
    Write-Host "✅ 成功项目:" -ForegroundColor Green
    foreach ($successItem in $success) {
        Write-Host "   $successItem" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "📄 详细报告已保存到: $reportPath" -ForegroundColor Gray

# 返回适当的退出码
if ($report.Summary.Errors -gt 0) {
    Write-Host ""
    Write-Host "❌ 存在错误，请修复后重新检查" -ForegroundColor Red
    exit 1
} elseif ($report.Summary.Warnings -gt 0) {
    Write-Host ""
    Write-Host "⚠️ 存在警告，建议优化" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host ""
    Write-Host "🎉 所有检查通过！" -ForegroundColor Green
    exit 0
}