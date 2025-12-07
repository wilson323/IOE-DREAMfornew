# IOE-DREAM 架构规范检查脚本
# 检查Controller层是否直接调用DAO/Manager
# 检查@Autowired违规使用
# 检查@Repository违规使用

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 架构规范检查" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查Controller层是否直接注入DAO
Write-Host "[1/3] 检查Controller层架构边界..." -ForegroundColor Yellow
$controllerDaoViolations = Get-ChildItem -Path "microservices" -Recurse -Filter "*Controller.java" |
    Select-String -Pattern "@Resource\s+private\s+\w+Dao\s+\w+;" |
    Select-Object -ExpandProperty Path -Unique

if ($controllerDaoViolations) {
    Write-Host "  ❌ 发现Controller直接注入DAO的违规: $($controllerDaoViolations.Count) 处" -ForegroundColor Red
    $controllerDaoViolations | ForEach-Object { Write-Host "    - $_" -ForegroundColor Red }
} else {
    Write-Host "  ✅ Controller层架构边界检查通过" -ForegroundColor Green
}

# 2. 检查@Autowired使用
Write-Host ""
Write-Host "[2/3] 检查@Autowired违规使用..." -ForegroundColor Yellow
$autowiredViolations = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "@Autowired" |
    Select-Object -ExpandProperty Path -Unique

if ($autowiredViolations) {
    Write-Host "  ❌ 发现@Autowired违规使用: $($autowiredViolations.Count) 处" -ForegroundColor Red
    $autowiredViolations | ForEach-Object { Write-Host "    - $_" -ForegroundColor Red }
} else {
    Write-Host "  ✅ @Autowired检查通过（全部使用@Resource）" -ForegroundColor Green
}

# 3. 检查@Repository和Repository后缀
Write-Host ""
Write-Host "[3/3] 检查DAO命名规范..." -ForegroundColor Yellow

# 检查Repository后缀
$repositorySuffixViolations = Get-ChildItem -Path "microservices" -Recurse -Filter "*Repository.java" |
    Select-Object -ExpandProperty FullName

# 检查@Repository注解
$repositoryAnnotationViolations = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "@Repository" |
    Select-Object -ExpandProperty Path -Unique

$totalViolations = $repositorySuffixViolations.Count + $repositoryAnnotationViolations.Count

if ($totalViolations -gt 0) {
    Write-Host "  ❌ 发现DAO命名规范违规: $totalViolations 处" -ForegroundColor Red
    if ($repositorySuffixViolations) {
        Write-Host "    Repository后缀违规: $($repositorySuffixViolations.Count) 处" -ForegroundColor Red
        $repositorySuffixViolations | ForEach-Object { Write-Host "      - $_" -ForegroundColor Red }
    }
    if ($repositoryAnnotationViolations) {
        Write-Host "    @Repository注解违规: $($repositoryAnnotationViolations.Count) 处" -ForegroundColor Red
        $repositoryAnnotationViolations | ForEach-Object { Write-Host "      - $_" -ForegroundColor Red }
    }
} else {
    Write-Host "  ✅ DAO命名规范检查通过（全部使用Dao后缀和@Mapper）" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "检查完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

