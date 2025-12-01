# =============================================================================
# IOE-DREAM 全面乱码修复脚本 (增强版)
# =============================================================================
# 功能: 系统性地修复项目中所有文件的乱码问题
# 作者: Claude Code
# 日期: 2025-11-19
# =============================================================================

$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

Write-Host "`n============================================================================" -ForegroundColor Cyan
Write-Host "🔧 IOE-DREAM 全面乱码修复脚本 (增强版)" -ForegroundColor Cyan
Write-Host "⏰ 执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "============================================================================`n" -ForegroundColor Cyan

# 扩展的乱码修复映射表
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
    "开始时间" = "开始时间"
    "结束时间" = "结束时间"
    "并行处理" = "并行处理"
    "检查结果" = "检查结果"
    "批量检查结果" = "批量检查结果"
    "DAILY/MONTHLY/CUSTOM）" = "DAILY/MONTHLY/CUSTOM）"
    
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
    
    # 其他常见乱码
    "" = ""
    "涓" = "中"
    "鏂" = "新"
    "" = ""
    "" = ""
}

# 需要检查的文件类型
$fileExtensions = @("*.java", "*.xml", "*.md", "*.js", "*.ts", "*.vue", "*.json", "*.yml", "*.yaml", "*.properties", "*.txt", "*.ps1", "*.sh")

$fixedFiles = 0
$errorFiles = 0
$totalFiles = 0
$bomRemoved = 0
$encodingConverted = 0
$garbledFixed = 0

# 函数：检测文件编码
function Get-FileEncoding {
    param([string]$FilePath)
    
    try {
        $bytes = [System.IO.File]::ReadAllBytes($FilePath)
        
        # 检测BOM
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            return @{ Encoding = "UTF-8-BOM"; HasBOM = $true }
        }
        if ($bytes.Length -ge 2 -and $bytes[0] -eq 0xFF -and $bytes[1] -eq 0xFE) {
            return @{ Encoding = "UTF-16-LE"; HasBOM = $true }
        }
        if ($bytes.Length -ge 2 -and $bytes[0] -eq 0xFE -and $bytes[1] -eq 0xFF) {
            return @{ Encoding = "UTF-16-BE"; HasBOM = $true }
        }
        
        # 尝试检测UTF-8（无BOM）
        try {
            $test = [System.Text.Encoding]::UTF8.GetString($bytes)
            $reEncoded = [System.Text.Encoding]::UTF8.GetBytes($test)
            if ([System.Linq.Enumerable]::SequenceEqual($bytes, $reEncoded)) {
                return @{ Encoding = "UTF-8"; HasBOM = $false }
            }
        } catch { }
        
        # 尝试GBK
        try {
            $test = [System.Text.Encoding]::GetEncoding("GBK").GetString($bytes)
            return @{ Encoding = "GBK"; HasBOM = $false }
        } catch { }
        
        return @{ Encoding = "UNKNOWN"; HasBOM = $false }
    } catch {
        return @{ Encoding = "ERROR"; HasBOM = $false }
    }
}

# 函数：修复文件乱码
function Fix-FileGarbledCharacters {
    param(
        [string]$FilePath,
        [string]$Content
    )
    
    $originalContent = $Content
    $hasChanges = $false
    
    # 移除BOM标记
    if ($Content.StartsWith([char]0xFEFF)) {
        $Content = $Content.Substring(1)
        $hasChanges = $true
        $script:bomRemoved++
    }
    
    # 应用乱码修复映射
    foreach ($key in $encodingFixes.Keys) {
        if ($Content.Contains($key)) {
            $Content = $Content.Replace($key, $encodingFixes[$key])
            $hasChanges = $true
            $script:garbledFixed++
        }
    }
    
    # 检测并修复其他常见乱码模式
    # 修复问号结尾的乱码（如"检查" -> "检查"）
    $Content = $Content -replace "([\u4e00-\u9fa5])\?", '$1查'
    
    # 修复其他特殊字符乱码
    $Content = $Content -replace "", ""
    $Content = $Content -replace "", ""
    $Content = $Content -replace "", ""
    
    if ($Content -ne $originalContent) {
        $hasChanges = $true
    }
    
    return @{ Content = $Content; HasChanges = $hasChanges }
}

# 主处理逻辑
Write-Host "开始扫描项目文件...`n" -ForegroundColor Blue

foreach ($ext in $fileExtensions) {
    $files = Get-ChildItem -Path $ProjectRoot -Recurse -Filter $ext -ErrorAction SilentlyContinue | 
        Where-Object { 
            $_.FullName -notmatch "\\node_modules\\" -and 
            $_.FullName -notmatch "\\.git\\" -and
            $_.FullName -notmatch "\\target\\" -and
            $_.FullName -notmatch "\\dist\\" -and
            $_.FullName -notmatch "\\venv\\" -and
            $_.FullName -notmatch "\\__pycache__\\"
        }
    
    Write-Host "检查 $ext 文件: $($files.Count) 个" -ForegroundColor Cyan
    
    foreach ($file in $files) {
        $totalFiles++
        
        try {
            # 检测文件编码
            $encodingInfo = Get-FileEncoding -FilePath $file.FullName
            $needsConversion = $false
            $needsBomRemoval = $false
            
            if ($encodingInfo.Encoding -ne "UTF-8" -and $encodingInfo.Encoding -ne "UNKNOWN" -and $encodingInfo.Encoding -ne "ERROR") {
                $needsConversion = $true
            }
            
            if ($encodingInfo.HasBOM) {
                $needsBomRemoval = $true
            }
            
            # 读取文件内容
            $content = $null
            $readEncoding = $null
            
            if ($encodingInfo.Encoding -eq "GBK" -or $encodingInfo.Encoding -eq "GB2312") {
                try {
                    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                    $content = [System.Text.Encoding]::GetEncoding("GBK").GetString($bytes)
                    $readEncoding = "GBK"
                } catch {
                    $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
                    $readEncoding = "UTF-8"
                }
            } else {
                try {
                    $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction Stop
                    $readEncoding = "UTF-8"
                } catch {
                    try {
                        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                        $content = [System.Text.Encoding]::UTF8.GetString($bytes)
                        $readEncoding = "UTF-8"
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
            
            # 修复乱码
            $fixResult = Fix-FileGarbledCharacters -FilePath $file.FullName -Content $content
            $content = $fixResult.Content
            $hasChanges = $fixResult.HasChanges
            
            # 如果需要转换编码或修复乱码，保存文件
            if ($hasChanges -or $needsConversion -or $needsBomRemoval) {
                # 使用UTF-8无BOM保存
                $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
                [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
                $fixedFiles++
                
                $fixMessages = @()
                if ($needsConversion) {
                    $fixMessages += "编码转换: $($encodingInfo.Encoding) -> UTF-8"
                    $encodingConverted++
                }
                if ($needsBomRemoval) {
                    $fixMessages += "移除BOM"
                }
                if ($hasChanges -and $fixResult.HasChanges) {
                    $fixMessages += "修复乱码"
                }
                
                Write-Host "  [FIXED] $($file.Name) - $($fixMessages -join ', ')" -ForegroundColor Green
            }
            
            # 每处理100个文件显示进度
            if ($totalFiles % 100 -eq 0) {
                Write-Host "进度: $totalFiles 文件已处理..." -ForegroundColor Cyan
            }
            
        } catch {
            Write-Host "  [ERROR] $($file.FullName): $_" -ForegroundColor Red
            $errorFiles++
        }
    }
}

Write-Host "`n============================================================================" -ForegroundColor Cyan
Write-Host "📊 修复结果汇总" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "总文件数: $totalFiles" -ForegroundColor White
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "编码转换数: $encodingConverted" -ForegroundColor Cyan
Write-Host "BOM移除数: $bomRemoved" -ForegroundColor Cyan
Write-Host "乱码修复数: $garbledFixed" -ForegroundColor Cyan
Write-Host "错误文件数: $errorFiles" -ForegroundColor $(if ($errorFiles -gt 0) { "Red" } else { "Green" })
Write-Host "============================================================================`n" -ForegroundColor Cyan

if ($errorFiles -eq 0) {
    Write-Host "[SUCCESS] 所有文件乱码修复完成！`n" -ForegroundColor Green
    exit 0
} else {
    Write-Host "[WARNING] 部分文件修复失败，请检查错误信息`n" -ForegroundColor Yellow
    exit 1
}

