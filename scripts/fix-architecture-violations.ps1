# IOE-DREAM架构违规修复脚本
# 用于修复@Autowired、@Repository等违规问题

# 检查函数
function Check-ArchitectureViolations {
    Write-Host "🔍 检查架构违规..." -ForegroundColor Yellow

    # 检查@Autowired违规
    $autowiredFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@Autowired" | Select-Object -Unique Path
    Write-Host "❌ 发现 @Autowired 违规: $($autowiredFiles.Count) 个文件" -ForegroundColor Red

    # 检查@Repository违规
    $repositoryFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@Repository" | Select-Object -Unique Path
    Write-Host "❌ 发现 @Repository 违规: $($repositoryFiles.Count) 个文件" -ForegroundColor Red

    # 检查javax包名违规
    $javaxFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "javax\." | Select-Object -Unique Path
    Write-Host "❌ 发现 javax 包名违规: $($javaxFiles.Count) 个文件" -ForegroundColor Red

    return @{
        AutowiredFiles = $autowiredFiles
        RepositoryFiles = $repositoryFiles
        JavaxFiles = $javaxFiles
    }
}

# 修复函数
function Repair-ArchitectureViolations {
    param($violations)

    Write-Host "🔧 开始修复架构违规..." -ForegroundColor Yellow

    # 修复@Autowired违规
    foreach ($file in $violations.AutowiredFiles) {
        Write-Host "修复 @Autowired: $($file.Path)" -ForegroundColor Green
        (Get-Content $file.Path) -replace '@Autowired', '@Resource' | Set-Content $file.Path
    }

    # 修复@Repository违规
    foreach ($file in $violations.RepositoryFiles) {
        Write-Host "修复 @Repository: $($file.Path)" -ForegroundColor Green
        (Get-Content $file.Path) -replace '@Repository', '@Mapper' | Set-Content $file.Path
    }

    # 修复javax包名违规
    foreach ($file in $violations.JavaxFiles) {
        Write-Host "修复 javax 包名: $($file.Path)" -ForegroundColor Green
        (Get-Content $file.Path) -replace 'javax\.', 'jakarta.' | Set-Content $file.Path
    }
}

# 主函数
function Main {
    Write-Host "🚀 IOE-DREAM架构违规修复工具" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan

    # 检查违规
    $violations = Check-ArchitectureViolations

    # 显示详细信息
    Write-Host "`n📋 违规详情:" -ForegroundColor Yellow
    $violations.AutowiredFiles | ForEach-Object { Write-Host "  - @Autowired: $($_.Path)" -ForegroundColor Gray }
    $violations.RepositoryFiles | ForEach-Object { Write-Host "  - @Repository: $($_.Path)" -ForegroundColor Gray }
    $violations.JavaxFiles | ForEach-Object { Write-Host "  - javax: $($_.Path)" -ForegroundColor Gray }

    # 确认修复
    $choice = Read-Host "`n❓ 是否要修复这些违规? (y/n)"
    if ($choice -eq 'y' -or $choice -eq 'Y') {
        Repair-ArchitectureViolations $violations
        Write-Host "✅ 修复完成!" -ForegroundColor Green
    } else {
        Write-Host "❌ 修复已取消" -ForegroundColor Red
    }
}

# 执行主函数
Main