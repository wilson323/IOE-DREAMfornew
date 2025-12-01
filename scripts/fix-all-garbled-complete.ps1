# =============================================================================
# IOE-DREAM 全面乱码修复脚本 (PowerShell版本) - 包括文件和文件夹
# =============================================================================
# 功能: 批量修复项目中所有文件的乱码问题，包括文件夹名称
# 作者: Claude Code
# 日期: 2025-11-19
# =============================================================================

$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

Write-Host "`n============================================================================" -ForegroundColor Cyan
Write-Host "🔧 IOE-DREAM 全面乱码修复脚本 (包括文件和文件夹)" -ForegroundColor Cyan
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
    "并行处理" = "并行处理"
    
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
    "事务" = "事务"
    "边界" = "边界"
    "完整" = "完整"
    
    # 特殊乱码字符修复
    "" = "中文"
    "???" = "中文"
    "涓" = "中"
    "鏂" = "新"
    "鎻愪" = "获"
    "搴旂" = "取"
    "閮婂" = "门"
    "閿" = "错"
    "闂" = "问"
}

# 排除的目录
$excludeDirs = @(
    "node_modules",
    ".git",
    "target",
    "dist",
    "venv",
    "__pycache__",
    ".idea",
    ".vscode",
    "build",
    "logs"
)

$fixedFiles = 0
$errorFiles = 0
$totalFiles = 0
$fixedDirs = 0
$encodingConverted = 0
$bomRemoved = 0
$garbledFixed = 0

# 函数：检查是否在排除目录中
function Should-ProcessPath {
    param([string]$path)
    
    foreach ($excludeDir in $excludeDirs) {
        if ($path -like "*\$excludeDir\*" -or $path -like "*\$excludeDir") {
            return $false
        }
    }
    return $true
}

# 函数：修复文件编码
function Fix-FileEncoding {
    param(
        [System.IO.FileInfo]$file
    )
    
    try {
        $needsFix = $false
        $fixType = @()
        
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
                $needsFix = $true
                $fixType += "编码转换"
            } catch {
                # 尝试GB2312
                try {
                    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                    $content = [System.Text.Encoding]::GetEncoding("GB2312").GetString($bytes)
                    $encoding = "GB2312"
                    $needsFix = $true
                    $fixType += "编码转换"
                } catch {
                    Write-Host "  [WARNING] 无法读取文件: $($file.FullName)" -ForegroundColor Yellow
                    return $false
                }
            }
        }
        
        if ($null -eq $content) {
            return $false
        }
        
        $originalContent = $content
        
        # 移除BOM标记
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
            $needsFix = $true
            $fixType += "移除BOM"
        }
        
        # 应用乱码修复映射
        foreach ($key in $encodingFixes.Keys) {
            if ($content -match [regex]::Escape($key)) {
                $content = $content -replace [regex]::Escape($key), $encodingFixes[$key]
                $needsFix = $true
                if ($fixType -notcontains "修复乱码") {
                    $fixType += "修复乱码"
                }
            }
        }
        
        # 修复其他常见乱码模式
        # 修复问号结尾的乱码（如"检查" -> "检查"）
        if ($content -match '[\u4e00-\u9fa5]\?') {
            $content = $content -replace '([\u4e00-\u9fa5])\?', '$1查'
            $needsFix = $true
            if ($fixType -notcontains "修复乱码") {
                $fixType += "修复乱码"
            }
        }
        
        # 如果有修改或需要转换编码，保存文件
        if ($needsFix -or $encoding -ne "UTF-8") {
            # 使用UTF-8无BOM保存
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            
            Write-Host "  [FIXED] $($file.FullName) - $($fixType -join ', ')" -ForegroundColor Green
            
            if ($encoding -ne "UTF-8") {
                $script:encodingConverted++
            }
            if ($fixType -contains "移除BOM") {
                $script:bomRemoved++
            }
            if ($fixType -contains "修复乱码") {
                $script:garbledFixed++
            }
            
            return $true
        }
        
        return $false
        
    } catch {
        Write-Host "  [ERROR] $($file.FullName): $_" -ForegroundColor Red
        return $false
    }
}

# 函数：检查并修复文件夹名称乱码
function Fix-DirectoryName {
    param(
        [System.IO.DirectoryInfo]$dir
    )
    
    try {
        $dirName = $dir.Name
        $parentPath = $dir.Parent.FullName
        $hasGarbled = $false
        $newName = $dirName
        
        # 检查是否包含乱码模式
        foreach ($key in $encodingFixes.Keys) {
            if ($dirName -match [regex]::Escape($key)) {
                $newName = $newName -replace [regex]::Escape($key), $encodingFixes[$key]
                $hasGarbled = $true
            }
        }
        
        if ($hasGarbled -and $newName -ne $dirName) {
            $newPath = Join-Path $parentPath $newName
            
            # 重命名文件夹
            Rename-Item -Path $dir.FullName -NewName $newName -ErrorAction Stop
            Write-Host "  [FIXED] 文件夹: $($dir.FullName) -> $newPath" -ForegroundColor Green
            $script:fixedDirs++
            return $true
        }
        
        return $false
        
    } catch {
        Write-Host "  [ERROR] 文件夹: $($dir.FullName): $_" -ForegroundColor Red
        return $false
    }
}

# 第一步：修复文件夹名称
Write-Host "第一步: 检查并修复文件夹名称乱码..." -ForegroundColor Blue
Write-Host "----------------------------------------------------------------------" -ForegroundColor Blue

$allDirs = Get-ChildItem -Path $ProjectRoot -Recurse -Directory -ErrorAction SilentlyContinue | 
    Where-Object { Should-ProcessPath $_.FullName } |
    Sort-Object -Property FullName -Descending

foreach ($dir in $allDirs) {
    if (Fix-DirectoryName $dir) {
        # 文件夹已重命名，需要更新后续处理
    }
}

Write-Host "修复文件夹: $fixedDirs 个`n" -ForegroundColor Cyan

# 第二步：修复所有文件
Write-Host "第二步: 修复所有文件内容乱码..." -ForegroundColor Blue
Write-Host "----------------------------------------------------------------------" -ForegroundColor Blue

# 文件扩展名列表
$fileExtensions = @("*.java", "*.xml", "*.md", "*.js", "*.ts", "*.vue", "*.json", "*.yml", "*.yaml", "*.properties", "*.txt", "*.ps1", "*.sh", "*.py", "*.sql")

foreach ($ext in $fileExtensions) {
    $files = Get-ChildItem -Path $ProjectRoot -Recurse -Filter $ext -ErrorAction SilentlyContinue |
        Where-Object { Should-ProcessPath $_.FullName }
    
    Write-Host "检查 $ext 文件: $($files.Count) 个" -ForegroundColor Yellow
    
    foreach ($file in $files) {
        $totalFiles++
        
        if (Fix-FileEncoding $file) {
            $fixedFiles++
        }
        
        # 每处理100个文件显示进度
        if ($totalFiles % 100 -eq 0) {
            Write-Host "进度: $totalFiles 文件已处理..." -ForegroundColor Cyan
        }
    }
}

Write-Host "`n============================================================================" -ForegroundColor Cyan
Write-Host "📊 修复结果汇总" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "总文件数: $totalFiles" -ForegroundColor White
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "修复文件夹数: $fixedDirs" -ForegroundColor Green
Write-Host "编码转换数: $encodingConverted" -ForegroundColor Cyan
Write-Host "BOM移除数: $bomRemoved" -ForegroundColor Cyan
Write-Host "乱码修复数: $garbledFixed" -ForegroundColor Cyan
Write-Host "错误文件数: $errorFiles" -ForegroundColor $(if ($errorFiles -gt 0) { "Red" } else { "Green" })
Write-Host "============================================================================`n" -ForegroundColor Cyan

# 第三步：验证修复结果
Write-Host "第三步: 验证修复结果..." -ForegroundColor Blue
Write-Host "----------------------------------------------------------------------" -ForegroundColor Blue

$garbledPatterns = @("检查", "结果", "涓", "鏂", "", "???")
$foundIssues = $false

foreach ($ext in $fileExtensions) {
    $files = Get-ChildItem -Path $ProjectRoot -Recurse -Filter $ext -ErrorAction SilentlyContinue |
        Where-Object { Should-ProcessPath $_.FullName }
    
    foreach ($file in $files) {
        try {
            $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction Stop
            
            foreach ($pattern in $garbledPatterns) {
                if ($content -match [regex]::Escape($pattern)) {
                    Write-Host "  [WARNING] 仍发现乱码: $($file.FullName) (模式: $pattern)" -ForegroundColor Yellow
                    $foundIssues = $true
                    break
                }
            }
        } catch {
            # 跳过无法读取的文件
        }
    }
}

if ($foundIssues) {
    Write-Host "`n[WARNING] 部分文件仍包含乱码，请手动检查`n" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "`n[SUCCESS] 所有文件和文件夹乱码修复完成！`n" -ForegroundColor Green
    exit 0
}

