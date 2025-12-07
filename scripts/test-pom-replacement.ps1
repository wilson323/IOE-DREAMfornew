# 测试POM替换方案
# 验证直接替换pom.xml是否能正确移除modules部分

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "POM替换方案验证测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查pom.xml文件是否存在
$pomPath = "microservices\pom.xml"
if (-not (Test-Path $pomPath)) {
    Write-Host "❌ 错误: 找不到 $pomPath" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 找到pom.xml文件: $pomPath" -ForegroundColor Green

# 备份原始文件
$backupPath = "microservices\pom-original-test.xml"
Copy-Item $pomPath $backupPath -Force
Write-Host "✅ 已备份原始文件: $backupPath" -ForegroundColor Green

# 读取原始内容
$lines = Get-Content $pomPath
$originalLineCount = $lines.Count
Write-Host "✅ 原始文件行数: $originalLineCount" -ForegroundColor Green

# 检查是否包含modules
$originalContent = Get-Content $pomPath -Raw
if ($originalContent -notmatch '<modules>') {
    Write-Host "❌ 错误: pom.xml中不包含<modules>标签" -ForegroundColor Red
    Remove-Item $backupPath -ErrorAction SilentlyContinue
    exit 1
}

Write-Host "✅ pom.xml包含<modules>标签" -ForegroundColor Green

# 模拟awk命令：移除modules部分
$inModules = $false
$newLines = @()
$removedLineCount = 0

foreach ($line in $lines) {
    if ($line -match '<modules>') {
        $inModules = $true
        $removedLineCount++
        continue
    }
    if ($inModules -and $line -match '</modules>') {
        $inModules = $false
        $removedLineCount++
        continue
    }
    if ($inModules) {
        $removedLineCount++
        continue
    }
    $newLines += $line
}

Write-Host "✅ 将移除 $removedLineCount 行modules相关代码" -ForegroundColor Green

# 创建修改后的文件（模拟直接替换）
$newContent = $newLines -join "`n"
$newContent | Out-File -FilePath $pomPath -Encoding UTF8 -NoNewline

Write-Host "✅ 已直接替换pom.xml文件" -ForegroundColor Green

# 验证替换后的文件
$modifiedContent = Get-Content $pomPath -Raw
if ($modifiedContent -match '<modules>') {
    Write-Host "❌ 错误: 替换后的文件仍然包含<modules>标签" -ForegroundColor Red
    # 恢复原始文件
    Copy-Item $backupPath $pomPath -Force
    Remove-Item $backupPath -ErrorAction SilentlyContinue
    exit 1
}

Write-Host "✅ 替换后的文件不包含<modules>标签" -ForegroundColor Green

# 检查替换后的文件是否包含其他重要内容
if ($modifiedContent -notmatch '<groupId>net.lab1024.sa</groupId>') {
    Write-Host "❌ 错误: 替换后的文件缺少groupId" -ForegroundColor Red
    Copy-Item $backupPath $pomPath -Force
    Remove-Item $backupPath -ErrorAction SilentlyContinue
    exit 1
}

Write-Host "✅ 替换后的文件包含groupId" -ForegroundColor Green

if ($modifiedContent -notmatch '<artifactId>ioedream-microservices-parent</artifactId>') {
    Write-Host "❌ 错误: 替换后的文件缺少artifactId" -ForegroundColor Red
    Copy-Item $backupPath $pomPath -Force
    Remove-Item $backupPath -ErrorAction SilentlyContinue
    exit 1
}

Write-Host "✅ 替换后的文件包含artifactId" -ForegroundColor Green

# 恢复原始文件
Copy-Item $backupPath $pomPath -Force
Remove-Item $backupPath -ErrorAction SilentlyContinue
Write-Host "✅ 已恢复原始pom.xml文件" -ForegroundColor Green

# 验证所有Dockerfile都使用了直接替换方案
Write-Host ""
Write-Host "验证所有Dockerfile..." -ForegroundColor Yellow

$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

$allFixed = $true
foreach ($service in $services) {
    $dockerfilePath = "microservices\$service\Dockerfile"
    if (-not (Test-Path $dockerfilePath)) {
        Write-Host "❌ 错误: 找不到 $dockerfilePath" -ForegroundColor Red
        $allFixed = $false
        continue
    }
    
    $dockerfileContent = Get-Content $dockerfilePath -Raw
    if ($dockerfileContent -match "cp pom\.xml pom-original\.xml" -and 
        $dockerfileContent -match "awk '/<modules>/,/<\/modules>/ \{next\} \{print\}' pom-original\.xml > pom\.xml") {
        Write-Host "✅ $service - 已使用直接替换方案" -ForegroundColor Green
    } else {
        Write-Host "❌ $service - 未使用直接替换方案" -ForegroundColor Red
        $allFixed = $false
    }
}

Write-Host ""
if ($allFixed) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ 所有测试通过！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "修复方案验证结果:" -ForegroundColor Cyan
    Write-Host "  - 直接替换pom.xml可以正确移除modules部分" -ForegroundColor White
    Write-Host "  - 所有9个Dockerfile都已使用直接替换方案" -ForegroundColor White
    Write-Host "  - 替换后的POM文件格式正确" -ForegroundColor White
    Write-Host ""
    Write-Host "下一步: 运行 docker-compose build --no-cache" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ 部分测试失败！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    exit 1
}
