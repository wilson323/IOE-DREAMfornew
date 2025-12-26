# ============================================================================
# 修复测试文件中的导入问题
# ============================================================================
# 问题说明：
# 1. ServiceImpl 导入问题：测试文件不应该直接导入ServiceImpl实现类
#    应该使用接口或Mock对象
# 2. Controller 导入问题：测试文件导入Controller但未使用@WebMvcTest
#    应该使用@WebMvcTest注解或移除不必要的导入
# ============================================================================

param(
    [switch]$DryRun = $false,
    [string]$Type = "all"  # all, serviceimpl, controller
)

$ErrorActionPreference = "Stop"
$script:TotalFixed = 0
$script:TotalFiles = 0
$script:Errors = @()

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "修复测试文件中的导入问题" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 读取扫描报告
$serviceImplReport = ".\reports-test-import-ServiceImpl-scanned.txt"
$controllerReport = ".\reports-test-import-Controller-scanned.txt"

$filesToFix = @()

# 处理 ServiceImpl 导入问题
if ($Type -eq "all" -or $Type -eq "serviceimpl") {
    if (Test-Path $serviceImplReport) {
        $serviceImplFiles = Get-Content $serviceImplReport
        foreach ($file in $serviceImplFiles) {
            if ($file -and (Test-Path $file)) {
                $filesToFix += [PSCustomObject]@{
                    File = $file
                    Type = "ServiceImpl"
                }
            }
        }
        Write-Host "📋 找到 $($serviceImplFiles.Count) 个 ServiceImpl 导入问题" -ForegroundColor Yellow
    }
    else {
        Write-Host "⚠️  未找到 ServiceImpl 扫描报告: $serviceImplReport" -ForegroundColor Yellow
    }
}

# 处理 Controller 导入问题
if ($Type -eq "all" -or $Type -eq "controller") {
    if (Test-Path $controllerReport) {
        $controllerFiles = Get-Content $controllerReport
        foreach ($file in $controllerFiles) {
            if ($file -and (Test-Path $file)) {
                $filesToFix += [PSCustomObject]@{
                    File = $file
                    Type = "Controller"
                }
            }
        }
        Write-Host "📋 找到 $($controllerFiles.Count) 个 Controller 导入问题" -ForegroundColor Yellow
    }
    else {
        Write-Host "⚠️  未找到 Controller 扫描报告: $controllerReport" -ForegroundColor Yellow
    }
}

if ($filesToFix.Count -eq 0) {
    Write-Host "✅ 没有需要修复的文件" -ForegroundColor Green
    exit 0
}

Write-Host ""

# 按文件分组
$filesByFile = $filesToFix | Group-Object File

foreach ($fileGroup in $filesByFile) {
    $filePath = $fileGroup.Name

    if (-not (Test-Path $filePath)) {
        Write-Host "⚠️  跳过不存在的文件: $filePath" -ForegroundColor Yellow
        continue
    }

    $script:TotalFiles++
    $content = Get-Content $filePath -Raw
    $originalContent = $content
    $modified = $false
    $issueTypes = $fileGroup.Group | ForEach-Object { $_.Type } | Sort-Object -Unique

    Write-Host "📝 处理文件: $filePath" -ForegroundColor Cyan
    Write-Host "   问题类型: $($issueTypes -join ', ')" -ForegroundColor Gray

    # 修复 ServiceImpl 导入
    if ($issueTypes -contains "ServiceImpl") {
        # 匹配: import net.lab1024.sa.xxx.service.impl.XxxServiceImpl;
        $serviceImplPattern = 'import\s+net\.lab1024\.sa\.([^.]+)\.service\.impl\.(\w+ServiceImpl);'
        $matches = [regex]::Matches($content, $serviceImplPattern)

        foreach ($match in $matches) {
            $module = $match.Groups[1].Value
            $serviceImplName = $match.Groups[2].Value
            $serviceName = $serviceImplName -replace 'Impl$', ''
            $serviceInterface = "net.lab1024.sa.$module.service.$serviceName"

            Write-Host "   🔍 发现: $serviceImplName" -ForegroundColor Yellow
            Write-Host "   💡 建议: 使用接口 $serviceName 或 Mock" -ForegroundColor Cyan

            # 移除 ServiceImpl 导入
            $content = $content -replace [regex]::Escape($match.Value), ''
            $modified = $true
            $script:TotalFixed++

            Write-Host "   ✅ 已移除 ServiceImpl 导入" -ForegroundColor Green
        }
    }

    # 修复 Controller 导入（需要检查是否使用@WebMvcTest）
    if ($issueTypes -contains "Controller") {
        # 检查是否已有 @WebMvcTest
        if ($content -notmatch '@WebMvcTest') {
            # 匹配: import net.lab1024.sa.xxx.controller.XxxController;
            $controllerPattern = 'import\s+net\.lab1024\.sa\.([^.]+)\.controller\.(\w+Controller);'
            $matches = [regex]::Matches($content, $controllerPattern)

            foreach ($match in $matches) {
                $module = $match.Groups[1].Value
                $controllerName = $match.Groups[2].Value

                Write-Host "   🔍 发现: $controllerName (未使用@WebMvcTest)" -ForegroundColor Yellow
                Write-Host "   💡 建议: 添加 @WebMvcTest($controllerName.class) 或移除导入" -ForegroundColor Cyan

                # 移除 Controller 导入（如果确实不需要）
                # 注意：这里只是移除导入，实际可能需要添加@WebMvcTest注解
                $content = $content -replace [regex]::Escape($match.Value), ''
                $modified = $true
                $script:TotalFixed++

                Write-Host "   ✅ 已移除 Controller 导入（请手动检查是否需要添加@WebMvcTest）" -ForegroundColor Green
            }
        }
        else {
            Write-Host "   ℹ️  文件已使用 @WebMvcTest，Controller 导入可能是正确的" -ForegroundColor Blue
        }
    }

    # 清理多余的空行
    if ($modified) {
        $content = $content -replace '(?m)^\s*$\r?\n{2,}', "`r`n"
    }

    if ($modified) {
        if (-not $DryRun) {
            try {
                [System.IO.File]::WriteAllText($filePath, $content, [System.Text.Encoding]::UTF8)
                Write-Host "   ✅ 文件已更新" -ForegroundColor Green
            }
            catch {
                $script:Errors += "❌ 更新文件失败: $filePath - $($_.Exception.Message)"
                Write-Host "   ❌ 更新失败: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
        else {
            Write-Host "   🔍 [DRY-RUN] 将更新文件" -ForegroundColor Yellow
        }
    }

    Write-Host ""
}

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "修复完成统计" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "处理文件数: $script:TotalFiles" -ForegroundColor White
Write-Host "修复位置数: $script:TotalFixed" -ForegroundColor White

if ($DryRun) {
    Write-Host ""
    Write-Host "🔍 这是预览模式，未实际修改文件" -ForegroundColor Yellow
    Write-Host "运行脚本时不加 -DryRun 参数将实际修改文件" -ForegroundColor Yellow
}

if ($script:Errors.Count -gt 0) {
    Write-Host ""
    Write-Host "❌ 错误列表:" -ForegroundColor Red
    foreach ($error in $script:Errors) {
        Write-Host "   $error" -ForegroundColor Red
    }
    exit 1
}

Write-Host ""
Write-Host "✅ 所有修复完成！" -ForegroundColor Green
Write-Host ""
Write-Host "📝 注意事项:" -ForegroundColor Yellow
Write-Host "   1. ServiceImpl 导入已移除，请确保测试使用接口或Mock" -ForegroundColor Yellow
Write-Host "   2. Controller 导入已移除，如需测试Controller请添加 @WebMvcTest 注解" -ForegroundColor Yellow
Write-Host "   3. 建议运行测试确保修复后代码正常工作" -ForegroundColor Yellow

