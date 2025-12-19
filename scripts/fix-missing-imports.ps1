# 修复缺失导入的脚本
# 目的: 自动添加缺失的导入语句到Java文件中

param(
    [string]$ServicePath = "D:/IOE-DREAM/microservices/ioedream-consume-service"
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "修复缺失导入" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

# 需要添加的导入语句
$importsToAdd = @(
    "import net.lab1024.sa.consume.domain.vo.PaymentContext;",
    "import net.lab1024.sa.consume.domain.vo.RefundContext;",
    "import net.lab1024.sa.consume.domain.vo.RefundResult;",
    "import net.lab1024.sa.consume.domain.vo.PaymentDetails;",
    "import net.lab1024.sa.consume.domain.vo.MobileConsumeStatisticsVO;",
    "import net.lab1024.sa.consume.domain.vo.MobileAccountInfoVO;"
)

# 查找所有Java文件
$javaFiles = Get-ChildItem -Path $ServicePath -Recurse -Filter "*.java"

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $modified = $false
    $packageLine = ""
    $importSection = @()

    # 解析文件结构
    $lines = $content -split "`n"
    $inImports = $false
    $packageFound = $false

    foreach ($line in $lines) {
        if ($line -match "^package") {
            $packageFound = $true
            $packageLine = $line
            continue
        }

        if ($packageFound -and $line -match "^import") {
            $inImports = $true
            $importSection += $line
            continue
        }

        if ($inImports -and $line -match "^(class|interface|enum|@|public|private|protected)") {
            $inImports = $false
            break
        }
    }

    # 检查是否需要添加导入
    foreach ($import in $importsToAdd) {
        $className = $import -replace ".*import\s+(.*?);", "`$1"

        if ($content -match [regex]::Escape($className) -and $content -notmatch [regex]::Escape($import)) {
            # 文件使用了类但没有导入，需要添加导入
            Write-Host "添加导入到: $($file.Name) - $className" -ForegroundColor Yellow

            # 找到package行和现有import行之间插入导入
            $newContent = ""
            $insertedImports = $false

            foreach ($line in $lines) {
                $newContent += $line + "`n"

                if ($line -match "^package" -and -not $insertedImports) {
                    # 在package后添加空行
                    $newContent += "`n"
                    # 添加缺失的导入
                    foreach ($impToAdd in $importsToAdd) {
                        $impClassName = $impToAdd -replace ".*import\s+(.*?);", "`$1"
                        if ($content -match [regex]::Escape($impClassName) -and $content -notmatch [regex]::Escape($impToAdd)) {
                            $newContent += $impToAdd + "`n"
                        }
                    }
                    $newContent += "`n"
                    $insertedImports = $true
                }
            }

            # 写回文件
            $newContent | Out-File -FilePath $file.FullName -Encoding UTF8
            $modified = $true
        }
    }
}

Write-Host "导入修复完成" -ForegroundColor Green
exit 0