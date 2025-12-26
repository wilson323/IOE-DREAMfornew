# IOE-DREAM 全局项目深度分析脚本
# 分析整个项目，识别过时、重复、不需要的文件

Write-Host @"
========================================
IOE-DREAM 全局项目深度分析
========================================
分析时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
"@ -ForegroundColor Cyan

# 统计各类文件
Write-Host "`n📊 正在扫描项目文件..." -ForegroundColor Yellow

$mdFiles = Get-ChildItem -Path . -Recurse -Filter *.md -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }
$javaFiles = Get-ChildItem -Path . -Recurse -Filter *.java -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive|build|target' }
$xmlFiles = Get-ChildItem -Path . -Recurse -Filter *.xml -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive|build|target' }
$shScripts = Get-ChildItem -Path . -Recurse -Filter *.sh -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }
$psScripts = Get-ChildItem -Path . -Recurse -Filter *.ps1 -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }
$pyScripts = Get-ChildItem -Path . -Recurse -Filter *.py -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }
$jsonFiles = Get-ChildItem -Path . -Recurse -Filter *.json -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }
$ymlFiles = Get-ChildItem -Path . -Recurse -Filter *.yml -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }
$yamlFiles = Get-ChildItem -Path . -Recurse -Filter *.yaml -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }

Write-Host @"
📊 项目文件统计
========================================

"@ -ForegroundColor Yellow

Write-Host "文档文件:" -ForegroundColor White
Write-Host "  Markdown (.md):    " -NoNewline; Write-Host $mdFiles.Count -ForegroundColor Cyan
Write-Host "  XML:               " -NoNewline; Write-Host $xmlFiles.Count -ForegroundColor Cyan
Write-Host "  JSON:              " -NoNewline; Write-Host $jsonFiles.Count -ForegroundColor Cyan
Write-Host "  YAML:              " -NoNewline; Write-Host ($ymlFiles.Count + $yamlFiles.Count) -ForegroundColor Cyan

Write-Host "`n代码文件:" -ForegroundColor White
Write-Host "  Java (.java):      " -NoNewline; Write-Host $javaFiles.Count -ForegroundColor Cyan

Write-Host "`n脚本文件:" -ForegroundColor White
Write-Host "  Shell (.sh):       " -NoNewline; Write-Host $shScripts.Count -ForegroundColor Cyan
Write-Host "  PowerShell (.ps1):  " -NoNewline; Write-Host $psScripts.Count -ForegroundColor Cyan
Write-Host "  Python (.py):      " -NoNewline; Write-Host $pyScripts.Count -ForegroundColor Cyan

$totalFiles = $mdFiles.Count + $javaFiles.Count + $xmlFiles.Count + $jsonFiles.Count + $ymlFiles.Count + $yamlFiles.Count + $shScripts.Count + $psScripts.Count + $pyScripts.Count
Write-Host "`n总计文件数: " -NoNewline; Write-Host $totalFiles -ForegroundColor Green

# 分析根目录文件
Write-Host "`n📁 根目录文件分析:" -ForegroundColor Yellow
$rootFiles = Get-ChildItem -Path . -MaxDepth 1 -File | Where-Object { $_.Name -notin @('.git', '.github', 'microservices', 'documentation', 'scripts', 'deploy', 'archive', 'node_modules') }
Write-Host "  根目录文件数: " -NoNewline; Write-Host $rootFiles.Count -ForegroundColor Cyan

# 分析微服务目录
Write-Host "`n🔍 微服务目录分析:" -ForegroundColor Yellow
$microservices = Get-ChildItem -Path ./microservices -Directory
Write-Host "  微服务数量: " -NoNewline; Write-Host $microservices.Count -ForegroundColor Cyan

# 分析documentation目录
if (Test-Path "./documentation") {
    $docFiles = Get-ChildItem -Path ./documentation -Recurse -File
    Write-Host "  documentation文件数: " -NoNewline; Write-Host $docFiles.Count -ForegroundColor Cyan
}

# 识别重复文档模式
Write-Host "`n🔍 重复文档分析:" -ForegroundColor Yellow

# 查找重复的README
$readmeFiles = Get-ChildItem -Path . -Recurse -Filter "README.md" -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }
Write-Host "  README.md文件: " -NoNewline; Write-Host $readmeFiles.Count -ForegroundColor Cyan

# 查找重复的CLAUDE.md
$claudeFiles = Get-ChildItem -Path . -Recurse -Filter "CLAUDE.md" -File | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }
Write-Host "  CLAUDE.md文件: " -NoNewline; Write-Host $claudeFiles.Count -ForegroundColor Cyan

# 查找临时文件
Write-Host "`n🗑️  临时文件识别:" -ForegroundColor Yellow

$tempPatterns = @(
    "*.log",
    "*.tmp",
    "*~",
    "*.bak",
    "*.backup",
    "*.old",
    "*.swp",
    ".DS_Store"
)

$tempFiles = @()
foreach ($pattern in $tempPatterns) {
    $files = Get-ChildItem -Path . -Recurse -Filter $pattern -File -ErrorAction SilentlyContinue | Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive' }
    if ($files) {
        $tempFiles += $files
    }
}

Write-Host "  临时文件数: " -NoNewline; Write-Host $tempFiles.Count -ForegroundColor Red

# 分析已完成
Write-Host @"

========================================
分析完成！准备生成详细报告...
========================================
"@ -ForegroundColor Green
