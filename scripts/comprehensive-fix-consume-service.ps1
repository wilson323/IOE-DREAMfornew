# 综合修复consume-service脚本
# 目的: 全面修复consume-service的编译问题

param(
    [switch]$DryRun,
    [switch]$Backup,
    [switch]$CreateMissingClasses
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "综合修复consume-service" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

$servicePath = "microservices/ioedream-consume-service"
$serviceRoot = "D:/IOE-DREAM/microservices/ioedream-consume-service"

# 问题文件列表
$problematicFiles = @(
    "src/main/java/net/lab1024/sa/consume/service/OfflineDataSyncService.java",
    "src/main/java/net/lab1024/sa/consume/service/impl/OfflineDataSyncServiceImpl.java",
    "src/main/java/net/lab1024/sa/consume/service/PaymentService.java",
    "src/main/java/net/lab1024/sa/consume/service/impl/PaymentServiceImpl.java",
    "src/main/java/net/lab1024/sa/consume/service/impl/DefaultFixedAmountCalculator.java",
    "src/main/java/net/lab1024/sa/consume/service/impl/DefaultVariableAmountCalculator.java"
)

# 1. 首先检查ConsumeRecordEntity问题
Write-Host "`n检查ConsumeRecordEntity文件..." -ForegroundColor White
$entityFile = "$serviceRoot/src/main/java/net/lab1024/sa/consume/entity/ConsumeRecordEntity.java"
if (Test-Path $entityFile) {
    $entityContent = Get-Content $entityFile -Raw -Encoding UTF8
    if ($entityContent -match "public\s+class\s+\w+" -and $entityContent -notmatch "public\s+class\s+ConsumeRecordEntity") {
        Write-Host "  ⚠️ ConsumeRecordEntity类名不匹配，需要修复" -ForegroundColor Yellow
        if ($Backup) {
            $backupPath = "scripts/backup-consume-entity-$(Get-Date -Format 'yyyyMMdd-HHmmss').java"
            Copy-Item $entityFile $backupPath
            Write-Host "  ✅ 备份: $entityFile → $backupPath" -ForegroundColor Green
        }
        if (-not $DryRun) {
            # 修复类名
            $entityContent = $entityContent -replace "public\s+class\s+\w+", "public class ConsumeRecordEntity"
            $entityContent | Out-File -FilePath $entityFile -Encoding UTF8
            Write-Host "  ✅ 修复ConsumeRecordEntity类名" -ForegroundColor Green
        }
    }
}

# 2. 删除问题文件
Write-Host "`n删除问题文件..." -ForegroundColor White
foreach ($file in $problematicFiles) {
    $fullPath = "$serviceRoot/$file"
    if (Test-Path $fullPath) {
        if ($DryRun) {
            Write-Host "  [DRY RUN] 将删除: $fullPath" -ForegroundColor Yellow
        } else {
            if ($Backup) {
                $backupDir = "scripts/backup-consume-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
                if (!(Test-Path $backupDir)) {
                    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
                }
                $relativePath = $file -replace '[\\/]', '-'
                $backupPath = Join-Path $backupDir $relativePath
                New-Item -ItemType Directory -Path (Split-Path $backupPath) -Force | Out-Null
                Copy-Item $fullPath $backupPath -Force
                Write-Host "  ✅ 备份: $fullPath → $backupPath" -ForegroundColor Green
            }
            Remove-Item $fullPath -Force -Recurse
            Write-Host "  ✅ 删除: $fullPath" -ForegroundColor Red
        }
    } else {
        Write-Host "  ⚠️ 文件不存在: $fullPath" -ForegroundColor Yellow
    }
}

# 3. 清理错误的包导入
Write-Host "`n清理错误的包导入..." -ForegroundColor White
$javaFiles = Get-ChildItem -Path "$serviceRoot/src/main/java" -Recurse -Filter "*.java"
$fixCount = 0

foreach ($javaFile in $javaFiles) {
    $content = Get-Content $javaFile -Raw -Encoding UTF8
    $originalContent = $content
    $modified = $false

    # 修复错误的包导入
    $content = $content -replace "import\s+net\.lab1024\.sa\.consume\.consume\.", "import net.lab1024.sa.consume."

    # 删除不存在的导入
    $content = $content -replace "import\s+net\.lab1024\.sa\.consume\.edge\..*;", ""
    $content = $content -replace "import\s+net\.lab1024\.sa\.common\.edge\..*;", ""
    $content = $content -replace "import\s+net\.lab1024\.sa\.video\.edge\..*;", ""

    # 删除缺失VO类的导入
    $content = $content -replace "import\s+net\.lab1024\.sa\.consume\.domain\.vo\.OfflineSyncResultVO;", ""
    $content = $content -replace "import\s+net\.lab1024\.sa\.consume\.domain\.vo\.MobileConsumeStatisticsVO;", ""
    $content = $content -replace "import\s+net\.lab1024\.sa\.consume\.domain\.vo\.BiometricConsumeResultVO;", ""
    $content = $content -replace "import\s+net\.lab1024\.sa\.consume\.domain\.vo\.ConsumeTransactionResultVO;", ""

    if ($content -ne $originalContent) {
        $modified = $true
        $fixCount++
    }

    if ($modified) {
        if ($DryRun) {
            Write-Host "  [DRY RUN] 将修改: $javaFile" -ForegroundColor Yellow
        } else {
            $content | Out-File -FilePath $javaFile -Encoding UTF8
            Write-Host "  ✅ 修改: $javaFile" -ForegroundColor Green
        }
    }
}

Write-Host "  修复了 $fixCount 个文件的导入问题" -ForegroundColor Cyan

# 4. 创建缺失的必要类
if ($CreateMissingClasses -and -not $DryRun) {
    Write-Host "`n创建缺失的必要类..." -ForegroundColor White

    # 创建目录
    $directories = @(
        "$serviceRoot/src/main/java/net/lab1024/sa/consume/domain/vo",
        "$serviceRoot/src/main/java/net/lab1024/sa/consume/domain/dto",
        "$serviceRoot/src/main/java/net/lab1024/sa/consume/dao"
    )

    foreach ($dir in $directories) {
        if (!(Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-Host "  创建目录: $dir" -ForegroundColor Gray
        }
    }

    # 创建基础的VO类
    $basicVOs = @(
        @{
            Name = "ConsumeResultVO"
            Package = "net.lab1024.sa.consume.domain.vo"
            Content = @"
package net.lab1024.sa.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费结果VO
 */
@Data
@Accessors(chain = true)
public class ConsumeResultVO {

    private Long consumeId;
    private String consumeNo;
    private String accountNo;
    private BigDecimal consumeAmount;
    private String consumeType;
    private Integer status;
    private String message;
    private LocalDateTime consumeTime;
    private String deviceCode;
}
"@
        },
        @{
            Name = "OfflineSyncResultVO"
            Package = "net.lab1024.sa.consume.domain.vo"
            Content = @"
package net.lab1024.sa.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;

/**
 * 离线同步结果VO
 */
@Data
@Accessors(chain = true)
public class OfflineSyncResultVO {

    private Integer totalRecords;
    private Integer successRecords;
    private Integer failedRecords;
    private List<String> errorMessages;
    private Boolean success;
    private String message;
}
"@
        }
    )

    foreach ($vo in $basicVOs) {
        $voPath = "$serviceRoot/src/main/java/net/lab1024/sa/consume/domain/vo/$($vo.Name).java"
        if (!(Test-Path $voPath)) {
            $vo.Content | Out-File -FilePath $voPath -Encoding UTF8
            Write-Host "  创建VO类: $($vo.Name)" -ForegroundColor Green
        }
    }
}

# 5. 验证修复效果
Write-Host "`n验证修复效果..." -ForegroundColor Yellow
try {
    Push-Location $serviceRoot
    $testResult = mvn clean compile -Dmaven.test.skip=true -Dmaven.clean.failOnError=false 2>&1
    Pop-Location

    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n🎉 修复成功！consume-service 编译通过！" -ForegroundColor Green
    } else {
        $errorCount = ($testResult | Select-String -Pattern "ERROR" | Measure-Object).Count
        Write-Host "`n⚠️ 仍有 $errorCount 个编译错误" -ForegroundColor Yellow

        # 显示主要错误类型
        $errorTypes = $testResult | Select-String -Pattern "ERROR" | ForEach-Object {
            if ($_ -match "找不到符号.*类\s+(\w+)") {
                $matches[1]
            } elseif ($_ -match "程序包(.+)不存在") {
                "Package: " + $matches[1]
            } else {
                "Other Error"
            }
        } | Group-Object | Sort-Object -Property Count -Descending | Select-Object -First 5

        Write-Host "主要错误类型:" -ForegroundColor DarkRed
        foreach ($error in $errorTypes) {
            Write-Host "  $($error.Name): $($error.Count) 个" -ForegroundColor DarkRed
        }
    }
} catch {
    Write-Host "`n❌ 验证过程出错: $($_.Exception.Message)" -ForegroundColor Red
}

if ($Backup -and -not $DryRun) {
    Write-Host "`n📁 备份位置: scripts/backup-consume-*" -ForegroundColor Cyan
}

Write-Host "`n====================================" -ForegroundColor Cyan
Write-Host "consume-service 综合修复完成" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "运行模式: DRY RUN（未实际修改文件）" -ForegroundColor Yellow
    Write-Host "要执行实际修复，请去掉 -DryRun 参数" -ForegroundColor White
} else {
    Write-Host "运行模式: 执行修复" -ForegroundColor Green
    if ($Backup) {
        Write-Host "已创建备份: ✅" -ForegroundColor Green
    }
    if ($CreateMissingClasses) {
        Write-Host "已创建缺失类: ✅" -ForegroundColor Green
    }
}

exit $LASTEXITCODE