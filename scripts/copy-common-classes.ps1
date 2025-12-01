# 批量复制单体架构common类到微服务common模块
$SourceDir = "D:\IOE-DREAM\smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common"
$TargetDir = "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common"

# 需要复制的关键文件列表
$filesToCopy = @(
    # util包
    "util\SmartStringUtil.java",
    "util\SmartBeanUtil.java",
    "util\SmartResponseUtil.java",
    "util\SmartVerificationUtil.java",
    "util\SmartIpUtil.java",
    "util\SmartBigDecimalUtil.java",
    "util\SmartEnumUtil.java",
    "util\SmartRequestUtil.java",
    "util\SmartLogUtil.java",
    "util\JsonUtil.java",

    # cache包
    "cache\BaseCacheManager.java",
    "cache\CacheService.java",
    "cache\RedisUtil.java",
    "cache\CacheResult.java",
    "cache\BatchCacheResult.java",
    "cache\CacheModule.java",
    "cache\BusinessDataType.java",

    # exception包
    "exception\BusinessException.java",

    # code包
    "code\ErrorCode.java",
    "code\UserErrorCode.java",
    "code\SystemErrorCode.java",
    "code\ErrorCodeRangeContainer.java",
    "code\ErrorCodeRegister.java",
    "code\UnexpectedErrorCode.java",

    # enumeration包
    "enumeration\BaseEnum.java",
    "enumeration\DataTypeEnum.java",
    "enumeration\GenderEnum.java",
    "enumeration\SystemEnvironmentEnum.java",
    "enumeration\UserTypeEnum.java",

    # annotation包
    "annotation\NoNeedLogin.java",
    "annotation\SaCheckLogin.java",
    "annotation\SaCheckPermission.java",

    # domain包剩余重要类
    "domain\RequestUser.java",
    "domain\UserPermission.java",
    "domain\SystemEnvironment.java",
    "domain\RequestUrlVO.java",
    "domain\DataScopePlugin.java",

    # json包
    "json\deserializer\LongJsonDeserializer.java",
    "json\serializer\LongJsonSerializer.java",
    "json\serializer\FileKeySerializer.java",
    "json\deserializer\FileKeyVoDeserializer.java",
    "json\deserializer\DictDataDeserializer.java",
    "json\serializer\BigDecimalNullZeroSerializer.java",

    # swagger包
    "swagger\SchemaEnum.java",
    "swagger\SmartOperationCustomizer.java",
    "swagger\SchemaEnumPropertyCustomizer.java",

    # controller包
    "controller\SupportBaseController.java",

    # page包
    "page\PageForm.java"
)

Write-Host "开始复制关键类文件..." -ForegroundColor Green

foreach ($file in $filesToCopy) {
    $sourceFile = Join-Path $SourceDir $file
    $targetFile = Join-Path $TargetDir $file

    if (Test-Path $sourceFile) {
        # 确保目标目录存在
        $targetDir = Split-Path $targetFile -Parent
        if (-not (Test-Path $targetDir)) {
            New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
        }

        # 读取源文件内容
        $content = Get-Content $sourceFile -Raw -Encoding UTF8

        # 修复包名
        $content = $content -replace 'package net\.lab1024\.sa\.base\.common', 'package net.lab1024.sa.common'

        # 写入目标文件
        Set-Content -Path $targetFile -Value $content -Encoding UTF8
        Write-Host "✓ 复制: $file" -ForegroundColor Green
    } else {
        Write-Host "✗ 文件不存在: $file" -ForegroundColor Red
    }
}

Write-Host "复制完成!" -ForegroundColor Cyan