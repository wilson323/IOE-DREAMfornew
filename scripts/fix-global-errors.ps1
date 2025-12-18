# IOE-DREAM 全局错误快速修复脚本
# 用途: 修复构建顺序、编码、依赖等P0级问题
# 执行: .\scripts\fix-global-errors.ps1

param(
    [switch]$SkipBuild = $false,
    [switch]$SkipEncoding = $false,
    [switch]$SkipDependency = $false
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 全局错误快速修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"

# 步骤1: 修复构建顺序问题
if (-not $SkipBuild) {
    Write-Host "[步骤1/3] 修复构建顺序问题..." -ForegroundColor Yellow
    Write-Host "  正在构建 microservices-common-core..." -ForegroundColor Gray
    
    Push-Location $PSScriptRoot\..
    
    try {
        # 强制先构建common-core
        mvn clean install -pl microservices/microservices-common-core -am -DskipTests
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  ✗ 构建失败！请检查Maven配置" -ForegroundColor Red
            exit 1
        }
        
        # 验证JAR已安装
        $jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common-core\1.0.0\microservices-common-core-1.0.0.jar"
        if (Test-Path $jarPath) {
            Write-Host "  ✓ JAR已安装: $jarPath" -ForegroundColor Green
        } else {
            Write-Host "  ✗ JAR未找到，但构建成功，可能版本号不同" -ForegroundColor Yellow
        }
        
        Write-Host "  ✓ 构建顺序修复完成" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ 构建过程出错: $_" -ForegroundColor Red
        exit 1
    } finally {
        Pop-Location
    }
    
    Write-Host ""
} else {
    Write-Host "[步骤1/3] 跳过构建顺序修复" -ForegroundColor Gray
    Write-Host ""
}

# 步骤2: 修复编码问题
if (-not $SkipEncoding) {
    Write-Host "[步骤2/3] 修复文件编码问题..." -ForegroundColor Yellow
    
    $javaFiles = Get-ChildItem -Path "$PSScriptRoot\..\microservices" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
    
    $fixedCount = 0
    $errorCount = 0
    
    foreach ($file in $javaFiles) {
        try {
            # 尝试以UTF-8读取
            $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
            
            if ($null -eq $content) {
                # 如果UTF-8读取失败，尝试其他编码
                $content = Get-Content $file.FullName -Raw -Encoding Default -ErrorAction SilentlyContinue
                
                if ($null -ne $content) {
                    # 检测乱码（简单检测：连续3个以上非ASCII字符）
                    $hasEncodingIssue = $content -match "[\u0080-\u00FF]{3,}"
                    
                    if ($hasEncodingIssue) {
                        Write-Host "  发现编码问题: $($file.Name)" -ForegroundColor Yellow
                        
                        # 备份原文件
                        $backupPath = "$($file.FullName).backup.$(Get-Date -Format 'yyyyMMddHHmmss')"
                        Copy-Item $file.FullName $backupPath -ErrorAction SilentlyContinue
                        
                        # 重新保存为UTF-8（BOM）
                        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
                        
                        $fixedCount++
                    }
                }
            }
        } catch {
            $errorCount++
            Write-Host "  处理文件出错: $($file.FullName) - $_" -ForegroundColor Red
        }
    }
    
    Write-Host "  ✓ 编码修复完成: 修复 $fixedCount 个文件, 错误 $errorCount 个" -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host "[步骤2/3] 跳过编码修复" -ForegroundColor Gray
    Write-Host ""
}

# 步骤3: 检查依赖关系
if (-not $SkipDependency) {
    Write-Host "[步骤3/3] 检查依赖关系..." -ForegroundColor Yellow
    
    $services = @(
        "ioedream-access-service",
        "ioedream-attendance-service",
        "ioedream-consume-service",
        "ioedream-visitor-service",
        "ioedream-video-service",
        "ioedream-oa-service"
    )
    
    $missingDeps = @()
    
    foreach ($service in $services) {
        $pomPath = "$PSScriptRoot\..\microservices\$service\pom.xml"
        
        if (Test-Path $pomPath) {
            $pomContent = Get-Content $pomPath -Raw
            
            # 检查是否包含common-core依赖
            if ($pomContent -notmatch "microservices-common-core") {
                $missingDeps += $service
                Write-Host "  ⚠ $service 缺少 microservices-common-core 依赖" -ForegroundColor Yellow
            } else {
                Write-Host "  ✓ $service 依赖正常" -ForegroundColor Green
            }
        }
    }
    
    if ($missingDeps.Count -gt 0) {
        Write-Host "  ⚠ 发现 $($missingDeps.Count) 个服务缺少依赖，请手动修复" -ForegroundColor Yellow
    } else {
        Write-Host "  ✓ 所有服务依赖正常" -ForegroundColor Green
    }
    
    Write-Host ""
} else {
    Write-Host "[步骤3/3] 跳过依赖检查" -ForegroundColor Gray
    Write-Host ""
}

# 总结
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "1. 在IDE中刷新Maven项目" -ForegroundColor Gray
Write-Host "2. 清理并重新构建项目" -ForegroundColor Gray
Write-Host "3. 检查是否还有编译错误" -ForegroundColor Gray
Write-Host ""


