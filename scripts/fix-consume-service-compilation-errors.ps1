# 消费服务模块编译错误批量修复脚本 (PowerShell版本)
# 作者: IOE-DREAM架构委员会
# 版本: 1.0.0
# 日期: 2025-12-22

param(
    [switch]$SkipBackup,
    [switch]$DryRun
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔧 消费服务模块编译错误修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$ErrorActionPreference = "Stop"

$ConsumeServiceDir = "microservices/ioedream-consume-service"
$BackupDir = "backup/$(Get-Date -Format 'yyyyMMdd_HHmmss')"

# 创建备份目录
if (-not $SkipBackup) {
    Write-Host "📁 创建备份目录: $BackupDir" -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null

    Write-Host "💾 备份原始文件..." -ForegroundColor Yellow
    $FilesToModify = @(
        "$ConsumeServiceDir/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductImportExportService.java"
        "$ConsumeServiceDir/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductPriceService.java"
        "$ConsumeServiceDir/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductQueryService.java"
        "$ConsumeServiceDir/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductServiceImpl_Refactored.java"
        "$ConsumeServiceDir/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductStockService.java"
        "$ConsumeServiceDir/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductValidationService.java"
    )

    foreach ($file in $FilesToModify) {
        if (Test-Path $file) {
            $backupFile = Join-Path $BackupDir (Split-Path $file -Leaf)
            Copy-Item $file $backupFile -Force
            Write-Host "  ✓ 备份: $(Split-Path $file -Leaf)" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "🔨 开始修复编译错误..." -ForegroundColor Yellow

# 修复1: 异常类构造函数错误
Write-Host "📝 修复异常类构造函数错误..." -ForegroundColor Cyan

$ExceptionPatterns = @(
    @{ Pattern = 'new ConsumeProductException\("([^"]*)"\)', Replacement = 'new ConsumeProductException(ConsumeProductException.ErrorCode.INVALID_PARAMETER, "$1")' },
    @{ Pattern = 'new ConsumeProductException\("([^"]*)", ([^)]*)\)', Replacement = 'new ConsumeProductException(ConsumeProductException.ErrorCode.INVALID_PARAMETER, "$1", $2)' }
)

foreach ($file in $FilesToModify) {
    if (Test-Path $file) {
        Write-Host "  修复: $(Split-Path $file -Leaf)" -ForegroundColor Green
        $content = Get-Content $file -Raw

        foreach ($pattern in $ExceptionPatterns) {
            $content = $content -replace $pattern.Pattern, $pattern.Replacement
        }

        if (-not $DryRun) {
            Set-Content $file $content -Encoding UTF8
        }
    }
}

# 修复2: 工具类导入
Write-Host "📝 修复工具类导入问题..." -ForegroundColor Cyan

foreach ($file in $FilesToModify) {
    if (Test-Path $file) {
        Write-Host "  修复导入: $(Split-Path $file -Leaf)" -ForegroundColor Green
        $content = Get-Content $file -Raw

        # 删除错误的导入
        $content = $content -replace 'import net\.lab1024\.sa\.consume\.util\.[\s\S]*?;', ''

        # 添加正确的BeanUtils导入
        if ($content -match 'BeanUtils\.') {
            if ($content -notmatch 'import org\.springframework\.beans\.BeanUtils;') {
                $content = $content -replace '(import lombok\..*;)', "`$1`r`nimport org.springframework.beans.BeanUtils;"
            }
        }

        # 修复方法调用
        $content = $content -replace 'BeanUtil\.', 'BeanUtils.'

        # 添加ArrayList导入
        if ($content -match 'ArrayList' -and $content -notmatch 'import java\.util\.ArrayList;') {
            if ($content -match 'import java\.') {
                $content = $content -replace '(import java\.[^;]+;)', "`$1`r`nimport java.util.ArrayList;"
            }
        }

        if (-not $DryRun) {
            Set-Content $file $content -Encoding UTF8
        }
    }
}

# 修复3: 类型引用错误
Write-Host "📝 修复类型引用错误..." -ForegroundColor Cyan

$TypeFixes = @(
    @{ Pattern = 'ConsumeAddForm', Replacement = 'ConsumeProductAddForm' }
)

foreach ($file in $FilesToModify) {
    if (Test-Path $file) {
        Write-Host "  修复类型: $(Split-Path $file -Leaf)" -ForegroundColor Green
        $content = Get-Content $file -Raw

        foreach ($fix in $TypeFixes) {
            $content = $content -replace $fix.Pattern, $fix.Replacement
        }

        if (-not $DryRun) {
            Set-Content $file $content -Encoding UTF8
        }
    }
}

# 修复4: 实体类字段映射
Write-Host "📝 修复实体类字段映射..." -ForegroundColor Cyan

$FieldFixes = @(
    @{ Pattern = '\.getStock\(\)', Replacement = '.getStockQuantity()' },
    @{ Pattern = '\.setStock\(([^)]*)\)', Replacement = '.setStockQuantity($1)' },
    @{ Pattern = '\.getProductSort\(\)', Replacement = '.getRecommendSort()' },
    @{ Pattern = '\.setProductSort\(([^)]*)\)', Replacement = '.setRecommendSort($1)' },
    @{ Pattern = 'ConsumeProductEntity::getStock', Replacement = 'ConsumeProductEntity::getStockQuantity' },
    @{ Pattern = 'ConsumeProductEntity::getProductSort', Replacement = 'ConsumeProductEntity::getRecommendSort' }
)

foreach ($file in $FilesToModify) {
    if (Test-Path $file) {
        Write-Host "  修复字段: $(Split-Path $file -Leaf)" -ForegroundColor Green
        $content = Get-Content $file -Raw

        foreach ($fix in $FieldFixes) {
            $content = $content -replace $fix.Pattern, $fix.Replacement
        }

        if (-not $DryRun) {
            Set-Content $file $content -Encoding UTF8
        }
    }
}

# 修复5: MyBatis-Plus使用错误
Write-Host "📝 修复MyBatis-Plus使用错误..." -ForegroundColor Cyan

$MyBatisFixes = @(
    @{ Pattern = 'selectPage\(([^,]*)QueryForm, ([^)]*)\)', Replacement = 'selectPage(new Page<>($1.getPageNum(), $1.getPageSize()), $2)' }
)

foreach ($file in $FilesToModify) {
    if (Test-Path $file) {
        Write-Host "  修复MyBatis: $(Split-Path $file -Leaf)" -ForegroundColor Green
        $content = Get-Content $file -Raw

        foreach ($fix in $MyBatisFixes) {
            $content = $content -replace $fix.Pattern, $fix.Replacement
        }

        # 确保有Page导入
        if ($content -match 'Page<' -and $content -notmatch 'import com\.baomidou\.mybatisplus\.extension\.plugins\.pagination\.Page;') {
            $content = $content -replace '(import com\.baomidou\.mybatisplus\.core\.conditions\.query\.LambdaQueryWrapper;)', "`$1`r`nimport com.baomidou.mybatisplus.extension.plugins.pagination.Page;"
        }

        if (-not $DryRun) {
            Set-Content $file $content -Encoding UTF8
        }
    }
}

# 修复6: BigDecimal过时API
Write-Host "📝 修复BigDecimal过时API..." -ForegroundColor Cyan

$BigDecimalFixes = @(
    @{ Pattern = 'BigDecimal\.ROUND_HALF_UP', Replacement = 'RoundingMode.HALF_UP' }
)

foreach ($file in $FilesToModify) {
    if (Test-Path $file) {
        Write-Host "  修复BigDecimal: $(Split-Path $file -Leaf)" -ForegroundColor Green
        $content = Get-Content $file -Raw

        foreach ($fix in $BigDecimalFixes) {
            $content = $content -replace $fix.Pattern, $fix.Replacement
        }

        # 添加RoundingMode导入
        if ($content -match 'RoundingMode\.' -and $content -notmatch 'import java\.math\.RoundingMode;') {
            $content = $content -replace '(import java\.math\.BigDecimal;)', "`$1`r`nimport java.math.RoundingMode;"
        }

        if (-not $DryRun) {
            Set-Content $file $content -Encoding UTF8
        }
    }
}

# 修复7: Form类方法调用
Write-Host "📝 修复Form类方法调用..." -ForegroundColor Cyan

$FormFixes = @(
    @{ Pattern = '\.getMinStock\(\)', Replacement = '.getStockStatus()' },
    @{ Pattern = '\.getOrderField\(\)', Replacement = '.getSortBy()' }
)

foreach ($file in $FilesToModify) {
    if (Test-Path $file) {
        Write-Host "  修复Form: $(Split-Path $file -Leaf)" -ForegroundColor Green
        $content = Get-Content $file -Raw

        foreach ($fix in $FormFixes) {
            $content = $content -replace $fix.Pattern, $fix.Replacement
        }

        if (-not $DryRun) {
            Set-Content $file $content -Encoding UTF8
        }
    }
}

Write-Host ""
if ($DryRun) {
    Write-Host "🔍 试运行模式 - 未修改文件" -ForegroundColor Yellow
} else {
    Write-Host "✅ 编译错误修复完成！" -ForegroundColor Green
}

Write-Host ""
Write-Host "📋 修复摘要:" -ForegroundColor Cyan
Write-Host "  - 异常类构造函数: 统一使用ErrorCode模式" -ForegroundColor White
Write-Host "  - 工具类导入: 替换为Spring标准BeanUtils" -ForegroundColor White
Write-Host "  - 类型引用: 修正Form类名称" -ForegroundColor White
Write-Host "  - 字段映射: 统一Entity字段名" -ForegroundColor White
Write-Host "  - MyBatis-Plus: 修正分页查询语法" -ForegroundColor White
Write-Host "  - BigDecimal: 使用现代RoundingMode" -ForegroundColor White
Write-Host "  - Form类: 修正方法调用" -ForegroundColor White

if (-not $SkipBackup) {
    Write-Host ""
    Write-Host "📁 备份位置: $BackupDir" -ForegroundColor Yellow
    Write-Host "🔄 如需回滚，请使用备份文件" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🧪 建议下一步操作:" -ForegroundColor Cyan
Write-Host "  1. 运行编译验证: mvn clean compile" -ForegroundColor White
Write-Host "  2. 运行单元测试: mvn test" -ForegroundColor White
Write-Host "  3. 运行集成测试: mvn integration-test" -ForegroundColor White

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🎉 修复脚本执行完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan