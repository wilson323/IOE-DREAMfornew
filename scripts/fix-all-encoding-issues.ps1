# =============================================================================
# IOE-DREAM 全局乱码修复脚本 (PowerShell版本)
# =============================================================================
# 功能: 批量修复项目中所有Java文件的乱码问题
# 作者: Claude Code
# 日期: 2025-11-19
# =============================================================================

$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

Write-Host "`n============================================================================" -ForegroundColor Cyan
Write-Host "🔧 IOE-DREAM 全局乱码修复脚本" -ForegroundColor Cyan
Write-Host "⏰ 执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "============================================================================`n" -ForegroundColor Cyan

# 乱码修复映射表
$encodingFixes = @{
    # 常见乱码模式修复
    "检查" = "检查"
    "结果" = "结果"
    "不一致" = "不一致"
    "时间" = "时间"
    "处理" = "处理"
    "不能为空" = "不能为空"
    "长度不能超过" = "长度不能超过"
    "格式：YYYY-MM）" = "格式：YYYY-MM）"
    "一致性" = "一致性"
    "完整性" = "完整性"
    "对账" = "对账"
    
    # GBK乱码修复（常见模式）
    "考勤" = "考勤"
    "服务" = "服务"
    "实现" = "实现"
    "管理" = "管理"
    "查询" = "查询"
    "打卡" = "打卡"
    "员工" = "员工"
    "记录" = "记录"
    "不能" = "不能"
    "为空" = "为空"
    "失败" = "失败"
    "验证" = "验证"
    "位置" = "位置"
    "超出" = "超出"
    "允许" = "允许"
    "范围" = "范围"
    "设备" = "设备"
    "列表" = "列表"
    "日期" = "日期"
    "分页" = "分页"
    "条件" = "条件"
    "按考勤" = "按考勤"
    "倒序" = "倒序"
    "排列" = "排列"
    "执行" = "执行"
    "转换" = "转换"
    "根据" = "根据"
    "不存在" = "不存在"
    "参数" = "参数"
    "异常" = "异常"
    "统一" = "统一"
    "响应" = "响应"
    "格式" = "格式"
    "集成" = "集成"
    "缓存" = "缓存"
    "管理器" = "管理器"
    "规则" = "规则"
    "引入" = "引入"
    "严格" = "严格"
    "遵循" = "遵循"
    "规范" = "规范"
    "负责" = "负责"
    "业务" = "业务"
    "逻辑" = "逻辑"
    "处理" = "处理"
    "事务" = "事务"
    "边界" = "边界"
    "完整" = "完整"
    "验证" = "验证"
    "异常" = "异常"
    "统一" = "统一"
    "响应" = "响应"
    "格式" = "格式"
}

$fixedFiles = 0
$errorFiles = 0
$totalFiles = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue

Write-Host "发现 $($javaFiles.Count) 个Java文件需要检查`n" -ForegroundColor Blue

foreach ($file in $javaFiles) {
    $totalFiles++
    
    try {
        # 读取文件内容（尝试多种编码）
        $content = $null
        $encoding = $null
        
        # 尝试UTF-8
        try {
            $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction Stop
            $encoding = "UTF-8"
        } catch {
            # 尝试GBK
            try {
                $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                $content = [System.Text.Encoding]::GetEncoding("GBK").GetString($bytes)
                $encoding = "GBK"
            } catch {
                # 尝试GB2312
                try {
                    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                    $content = [System.Text.Encoding]::GetEncoding("GB2312").GetString($bytes)
                    $encoding = "GB2312"
                } catch {
                    Write-Host "  [WARNING] 无法读取文件: $($file.FullName)" -ForegroundColor Yellow
                    $errorFiles++
                    continue
                }
            }
        }
        
        if ($null -eq $content) {
            continue
        }
        
        $originalContent = $content
        $hasChanges = $false
        
        # 移除BOM标记
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
            $hasChanges = $true
        }
        
        # 应用乱码修复映射
        foreach ($key in $encodingFixes.Keys) {
            if ($content -match [regex]::Escape($key)) {
                $content = $content -replace [regex]::Escape($key), $encodingFixes[$key]
                $hasChanges = $true
            }
        }
        
        # 修复ReconciliationService.java中的特定乱码
        if ($file.Name -eq "ReconciliationService.java") {
            $content = $content -replace "一致性检\?", "一致性检查"
            $content = $content -replace "格式：YYYY-MM\?", "格式：YYYY-MM）"
            $content = $content -replace "一致\?", "一致性"
            $content = $content -replace "检查结\?", "检查结果"
            $content = $content -replace "批量检查结\?", "批量检查结果"
            $content = $content -replace "不一\?", "不一致"
            $content = $content -replace "开始时\?", "开始时间"
            $content = $content -replace "结束时间", "结束时间"
            $content = $content -replace "DAILY/MONTHLY/CUSTOM\?", "DAILY/MONTHLY/CUSTOM）"
            $content = $content -replace "完整\?", "完整性"
            $content = $content -replace "对\?", "对账"
            $content = $content -replace "并行处\?", "并行处理"
            
            # 删除接口定义后的实现类代码（第84行开始）
            $lines = $content -split "`n"
            $newLines = @()
            $inInterface = $true
            
            foreach ($line in $lines) {
                if ($line -match "^\s*\}\s*$" -and $inInterface) {
                    $newLines += $line
                    $inInterface = $false
                    continue
                }
                
                if ($inInterface) {
                    $newLines += $line
                } elseif ($line -match "^\s*@Resource|^\s*private|^\s*//|^\s*/\*\*|^\s*\*|^\s*@") {
                    # 跳过实现类的代码
                    continue
                } else {
                    break
                }
            }
            
            $content = $newLines -join "`n"
            $hasChanges = $true
        }
        
        # 如果有修改，保存文件
        if ($hasChanges -or $encoding -ne "UTF-8") {
            # 使用UTF-8无BOM保存
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            $fixedFiles++
            
            if ($encoding -ne "UTF-8") {
                Write-Host "  [FIXED] $($file.FullName) (编码: $encoding -> UTF-8)" -ForegroundColor Green
            } else {
                Write-Host "  [FIXED] $($file.FullName) (乱码修复)" -ForegroundColor Green
            }
        }
        
        # 每处理100个文件显示进度
        if ($totalFiles % 100 -eq 0) {
            Write-Host "进度: $totalFiles / $($javaFiles.Count) 文件已处理..." -ForegroundColor Cyan
        }
        
    } catch {
        Write-Host "  [ERROR] $($file.FullName): $_" -ForegroundColor Red
        $errorFiles++
    }
}

Write-Host "`n============================================================================" -ForegroundColor Cyan
Write-Host "📊 修复结果汇总" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "总文件数: $totalFiles" -ForegroundColor White
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "错误文件数: $errorFiles" -ForegroundColor $(if ($errorFiles -gt 0) { "Red" } else { "Green" })
Write-Host "============================================================================`n" -ForegroundColor Cyan

if ($errorFiles -eq 0) {
    Write-Host "[SUCCESS] 所有文件乱码修复完成！`n" -ForegroundColor Green
    exit 0
} else {
    Write-Host "[WARNING] 部分文件修复失败，请检查错误信息`n" -ForegroundColor Yellow
    exit 1
}

