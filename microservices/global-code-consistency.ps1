# 全局代码一致性企业级梳理脚本
# 系统性解决所有语法错误和代码规范问题

Write-Host "=== 全局代码一致性企业级梳理脚本 ===" -ForegroundColor Cyan

# 1. 首先清理所有BOM字符
Write-Host "步骤1: 清理全局BOM字符..." -ForegroundColor Yellow

$allJavaFiles = Get-ChildItem -Path "." -Name "*.java" -Recurse
$bomCount = 0

foreach ($file in $allJavaFiles) {
    $fullPath = Join-Path "." $file

    try {
        $bytes = [System.IO.File]::ReadAllBytes($fullPath)

        if ($bytes.Length -gt 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
            [System.IO.File]::WriteAllBytes($fullPath, $bytesWithoutBom)
            $bomCount++
            Write-Host "✅ 移除BOM: $file" -ForegroundColor Green
        }
    } catch {
        Write-Host "❌ 处理失败: $file" -ForegroundColor Red
    }
}

Write-Host "BOM清理完成，处理了 $bomCount 个文件" -ForegroundColor Green

# 2. 修复常见的语法错误
Write-Host "`n步骤2: 修复常见语法错误..." -ForegroundColor Yellow

$syntaxFixedCount = 0

foreach ($file in $allJavaFiles) {
    $fullPath = Join-Path "." $file

    try {
        $content = Get-Content -Path $fullPath -Raw -Encoding UTF8
        $originalContent = $content

        # 修复常见的语法错误
        $content = $content -replace 'iimport', 'import'
        $content = $content -replace 'i\r?\nimport', 'import'
        $content = $content -replace '\r\n\s*i\r?\nimport', '`r`nimport'

        # 修复多余的大括号和语法错误
        $content = $content -replace '@Configuration\s*\{\s*public', '@Configuration public'
        $content = $content -replace '@Configuration\s*\{\s*public', '@Configuration public'
        $content = $content -replace '@Configuration\s*\{\s*public', '@Configuration public'
        $content = $content -replace '@Configuration\s*\{\s*public', '@Configuration public'
        $content = $content -replace '@Configuration\s*\{\s*public', '@Configuration public'

        # 修复泛型语法错误
        $content = $content -replace '<\s*>', '<Object>'
        $content = $content -replace '<\s*,\s*>', '<Object, Object>'
        $content = $content -replace '<\s*,\s*,\s*>', '<Object, Object, Object>'

        if ($content -ne $originalContent) {
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($fullPath, $content, $utf8NoBom)
            $syntaxFixedCount++
            Write-Host "✅ 修复语法: $file" -ForegroundColor Green
        }

    } catch {
        Write-Host "❌ 修复失败: $file - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "语法错误修复完成，处理了 $syntaxFixedCount 个文件" -ForegroundColor Green

# 3. 检查架构合规性
Write-Host "`n步骤3: 检查架构合规性..." -ForegroundColor Yellow

$architecturalIssues = @()

foreach ($file in $allJavaFiles) {
    $fullPath = Join-Path "." $file

    try {
        $content = Get-Content -Path $fullPath -Raw -Encoding UTF8

        # 检查违规使用
        if ($content -match '@Autowired') {
            $architecturalIssues += "使用@Autowired: $file"
        }

        if ($content -match '@Repository') {
            $architecturalIssues += "使用@Repository: $file"
        }

        if ($content -match 'javax\.(annotation|validation|persistence|servlet)') {
            $architecturalIssues += "使用javax包: $file"
        }

        # 检查DAO命名规范
        if ($content -match 'interface\s+\w*Repository\b') {
            $architecturalIssues += "Repository命名违规: $file"
        }

    } catch {
        Write-Host "❌ 检查失败: $file" -ForegroundColor Red
    }
}

if ($architecturalIssues.Count -gt 0) {
    Write-Host "发现架构合规性问题:" -ForegroundColor Red
    foreach ($issue in $architecturalIssues) {
        Write-Host "  ⚠️ $issue" -ForegroundColor Yellow
    }
} else {
    Write-Host "✅ 架构合规性检查通过" -ForegroundColor Green
}

# 4. 统一代码风格
Write-Host "`n步骤4: 统一代码风格..." -ForegroundColor Yellow

$styleFixedCount = 0

foreach ($file in $allJavaFiles) {
    $fullPath = Join-Path "." $file

    try {
        $content = Get-Content -Path $fullPath -Raw -Encoding UTF8
        $originalContent = $content

        # 统一Logger声明
        $content = $content -replace 'private static final Logger log = LoggerFactory\.getLogger\(\w+\.class\);', 'private static final Logger log = LoggerFactory.getLogger(SmartRequestUtil.class);'

        # 统一异常处理
        $content = $content -replace 'throw new BusinessException\("([^"]+)"\)', 'throw new BusinessException("BUSINESS_ERROR", "$1")'

        # 统一导入顺序
        # 这里可以添加更多的代码风格统一规则

        if ($content -ne $originalContent) {
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($fullPath, $content, $utf8NoBom)
            $styleFixedCount++
        }

    } catch {
        Write-Host "❌ 风格统一失败: $file" -ForegroundColor Red
    }
}

Write-Host "代码风格统一完成，处理了 $styleFixedCount 个文件" -ForegroundColor Green

Write-Host "`n=== 全局代码梳理统计 ===" -ForegroundColor Cyan
Write-Host "✅ BOM字符清理: $bomCount 个文件" -ForegroundColor Green
Write-Host "🔧 语法错误修复: $syntaxFixedCount 个文件" -ForegroundColor Green
Write-Host "🎨 代码风格统一: $styleFixedCount 个文件" -ForegroundColor Green
Write-Host "📋 架构问题发现: $($architecturalIssues.Count) 个问题" -ForegroundColor $(if($architecturalIssues.Count -gt 0){"Red"}else{"Green"})

Write-Host "`n全局代码梳理完成！准备验证编译..." -ForegroundColor Cyan