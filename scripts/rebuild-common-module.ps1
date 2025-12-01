# 重建microservices-common模块，只复制必要的基础类
$SourceDir = "D:\IOE-DREAM\smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common"
$TargetDir = "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common"

Write-Host "开始重建microservices-common模块..." -ForegroundColor Green

# 创建基本目录结构
$dirs = @(
    "domain",
    "util",
    "exception",
    "code",
    "enumeration"
)

foreach ($dir in $dirs) {
    $fullPath = Join-Path $TargetDir $dir
    New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
    Write-Host "✓ 创建目录: $dir" -ForegroundColor Green
}

# 核心基础类列表（按依赖顺序）
$coreClasses = @(
    # 基础枚举和接口
    @{Source="enumeration\BaseEnum.java"; Target="enumeration\BaseEnum.java"},
    @{Source="enumeration\DataTypeEnum.java"; Target="enumeration\DataTypeEnum.java"},
    @{Source="enumeration\UserTypeEnum.java"; Target="enumeration\UserTypeEnum.java"},

    # 错误码体系
    @{Source="code\ErrorCode.java"; Target="code\ErrorCode.java"},
    @{Source="code\UserErrorCode.java"; Target="code\UserErrorCode.java"},
    @{Source="code\SystemErrorCode.java"; Target="code\SystemErrorCode.java"},

    # 异常类
    @{Source="exception\BusinessException.java"; Target="exception\BusinessException.java"},

    # 核心Domain对象
    @{Source="domain\PageParam.java"; Target="domain\PageParam.java"},
    @{Source="domain\PageResult.java"; Target="domain\PageResult.java"},
    @{Source="domain\ResponseDTO.java"; Target="domain\ResponseDTO.java"},
    @{Source="domain\CacheResult.java"; Target="domain\CacheResult.java"},
    @{Source="domain\RequestUser.java"; Target="domain\RequestUser.java"},
    @{Source="domain\UserPermission.java"; Target="domain\UserPermission.java"},

    # 核心工具类
    @{Source="util\SmartStringUtil.java"; Target="util\SmartStringUtil.java"},
    @{Source="util\SmartBeanUtil.java"; Target="util\SmartBeanUtil.java"},
    @{Source="util\SmartPageUtil.java"; Target="util\SmartPageUtil.java"},
    @{Source="util\SmartResponseUtil.java"; Target="util\SmartResponseUtil.java"},
    @{Source="util\SmartVerificationUtil.java"; Target="util\SmartVerificationUtil.java"}
)

Write-Host "开始复制核心基础类..." -ForegroundColor Cyan

foreach ($classInfo in $coreClasses) {
    $sourceFile = Join-Path $SourceDir $classInfo.Source
    $targetFile = Join-Path $TargetDir $classInfo.Target

    if (Test-Path $sourceFile) {
        try {
            # 读取源文件内容，使用UTF8编码
            $content = Get-Content $sourceFile -Raw -Encoding UTF8 -ErrorAction Stop

            if ($content) {
                # 修复包名
                $content = $content -replace 'package net\.lab1024\.sa\.base\.common', 'package net.lab1024.sa.common'

                # 修复import语句
                $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.([^;]+);', 'import net.lab1024.sa.common.$1;'

                # 写入目标文件
                Set-Content -Path $targetFile -Value $content -Encoding UTF8 -NoNewline -ErrorAction Stop
                Write-Host "✓ 复制: $($classInfo.Target)" -ForegroundColor Green
            } else {
                Write-Host "✗ 空文件: $($classInfo.Source)" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "✗ 复制失败: $($classInfo.Source) - $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "✗ 文件不存在: $($classInfo.Source)" -ForegroundColor Red
    }
}

Write-Host "重建完成!" -ForegroundColor Cyan

# 验证复制结果
Write-Host "验证复制结果..." -ForegroundColor Yellow
$copiedFiles = Get-ChildItem -Path $TargetDir -Filter "*.java" -Recurse
Write-Host "共复制了 $($copiedFiles.Count) 个Java文件" -ForegroundColor Green

# 检查包名是否正确
$packageErrors = $copiedFiles | Select-String -Pattern "package net\.lab1024\.sa\.base\.common"
if ($packageErrors) {
    Write-Host "发现未修复的包名:" -ForegroundColor Red
    $packageErrors | ForEach-Object { Write-Host "  $($_.Path)" -ForegroundColor Yellow }
} else {
    Write-Host "✓ 所有包名已正确修复" -ForegroundColor Green
}