# 修复编译问题 - 步骤1: 修复MyBatis注解导入
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复MyBatis注解导入问题" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$projectRoot = "D:\IOE-DREAM"
Set-Location $projectRoot

# 修复UserDao的MyBatis注解
$userDaoPath = "microservices\microservices-common\src\main\java\net\lab1024\sa\common\security\dao\UserDao.java"

Write-Host "`n正在修复UserDao..." -ForegroundColor Yellow

if (Test-Path $userDaoPath) {
    $lines = Get-Content $userDaoPath
    $newLines = @()
    $importAdded = $false

    foreach ($line in $lines) {
        $newLines += $line

        # 在package声明后添加导入
        if ($line -match "^package " -and -not $importAdded) {
            $newLines += ""
            $newLines += "import org.apache.ibatis.annotations.Select;"
            $newLines += "import org.apache.ibatis.annotations.Update;"
            $newLines += "import org.apache.ibatis.annotations.Delete;"
            $newLines += "import org.apache.ibatis.annotations.Insert;"
            $importAdded = $true
        }
    }

    # 保存文件
    $newLines | Out-File -FilePath $userDaoPath -Encoding UTF8 -Force
    Write-Host "✓ UserDao已修复" -ForegroundColor Green
}

Write-Host "`n✓ 步骤1完成" -ForegroundColor Green

