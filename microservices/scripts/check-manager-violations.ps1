# Manager架构违规检查脚本
param(
    [string]$ProjectRoot = "D:\IOE-DREAM\microservices"
)

Write-Host "Starting Manager Architecture Compliance Check..." -ForegroundColor Cyan
Write-Host "Project Root: $ProjectRoot" -ForegroundColor Cyan
Write-Host ""

# 查找所有Manager类文件
$managerFiles = Get-ChildItem -Path $ProjectRoot -Recurse -Filter "*Manager*.java" |
    Where-Object { $_.FullName -notmatch "Test.java" }

Write-Host "Found $($managerFiles.Count) Manager files" -ForegroundColor Blue
Write-Host ""

$violationsFound = $false

foreach ($file in $managerFiles) {
    $content = Get-Content $file.FullName -Raw
    $relativePath = $file.FullName.Replace($ProjectRoot, "").Replace("\", "/").TrimStart("/")

    $hasSpringAnnotation = $false
    $hasFieldInjection = $false
    $isConfigurationFile = $relativePath -match "config.*Configuration.java"

    # 检查Spring注解
    if ($content -match "@Component|@Service|@Repository" -and -not $isConfigurationFile) {
        $hasSpringAnnotation = $true
        $violationsFound = $true
        Write-Host "VIOLATION: $relativePath" -ForegroundColor Red
        Write-Host "  - Uses Spring annotation (@Component/@Service/@Repository)" -ForegroundColor Red
    }

    # 检查字段注入
    if ($content -match "@Resource|@Autowired" -and -not $isConfigurationFile) {
        $hasFieldInjection = $true
        if (-not $hasSpringAnnotation) {
            $violationsFound = $true
            Write-Host "VIOLATION: $relativePath" -ForegroundColor Red
        }
        Write-Host "  - Uses field injection (@Resource/@Autowired)" -ForegroundColor Red
    }

    # 如果没有违规，显示合规
    if (-not $hasSpringAnnotation -and -not $hasFieldInjection -and -not $isConfigurationFile) {
        Write-Host "COMPLIANT: $relativePath" -ForegroundColor Green
    }

    if ($hasSpringAnnotation -or $hasFieldInjection) {
        Write-Host ""
    }
}

Write-Host "======================================" -ForegroundColor Cyan
if ($violationsFound) {
    Write-Host "RESULT: Manager architecture violations found!" -ForegroundColor Red
    Write-Host "ACTION: Please fix all violations according to CLAUDE.md standards" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "RESULT: All Manager classes are compliant!" -ForegroundColor Green
    Write-Host "SUCCESS: No Manager annotation violations found" -ForegroundColor Green
    exit 0
}