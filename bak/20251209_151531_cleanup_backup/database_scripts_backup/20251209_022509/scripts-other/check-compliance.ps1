# IOE-DREAM架构合规性快速自查脚本
# 开发者提交代码前必须执行此脚本

Write-Host "===== IOE-DREAM架构合规性自查 =====" -ForegroundColor Cyan
Write-Host "规范依据: CLAUDE.md v4.0.0`n" -ForegroundColor Gray

$violations = 0
$totalChecks = 5

# 检查1: @Repository违规
Write-Host "[1/$totalChecks] 检查@Repository违规..." -ForegroundColor Yellow
$results = Select-String -Path "microservices\*\*.java" -Pattern "import org\.springframework\.stereotype\.Repository" -Recurse -ErrorAction SilentlyContinue
if ($results) {
    Write-Host "  ❌ 发现 $($results.Count) 个违规文件" -ForegroundColor Red
    $results | ForEach-Object { Write-Host "     - $($_.Path):$($_.LineNumber)" -ForegroundColor Red }
    Write-Host "  修复命令: .\scripts\fix-repository-violations.ps1" -ForegroundColor Yellow
    $violations++
} else {
    Write-Host "  ✅ 通过" -ForegroundColor Green
}

# 检查2: @Autowired违规
Write-Host "`n[2/$totalChecks] 检查@Autowired违规..." -ForegroundColor Yellow
$results = Select-String -Path "microservices\*\*.java" -Pattern "import org\.springframework\.beans\.factory\.annotation\.Autowired" -Recurse -ErrorAction SilentlyContinue
if ($results) {
    Write-Host "  ❌ 发现 $($results.Count) 个违规文件" -ForegroundColor Red
    $results | ForEach-Object { Write-Host "     - $($_.Path):$($_.LineNumber)" -ForegroundColor Red }
    Write-Host "  修复方法: 将@Autowired替换为@Resource" -ForegroundColor Yellow
    $violations++
} else {
    Write-Host "  ✅ 通过" -ForegroundColor Green
}

# 检查3: javax包名违规
Write-Host "`n[3/$totalChecks] 检查javax包名违规..." -ForegroundColor Yellow
$results = Select-String -Path "microservices\*\*.java" -Pattern "import javax\.validation|import javax\.annotation" -Recurse -ErrorAction SilentlyContinue
if ($results) {
    Write-Host "  ❌ 发现 $($results.Count) 个违规文件" -ForegroundColor Red
    $results | ForEach-Object { Write-Host "     - $($_.Path):$($_.LineNumber)" -ForegroundColor Red }
    Write-Host "  修复命令: .\scripts\fix-javax-violations.ps1" -ForegroundColor Yellow
    $violations++
} else {
    Write-Host "  ✅ 通过" -ForegroundColor Green
}

# 检查4: HikariCP配置违规
Write-Host "`n[4/$totalChecks] 检查HikariCP配置违规..." -ForegroundColor Yellow
$results = Select-String -Path "microservices\*\application*.yml" -Pattern "hikari:" -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.Path -notmatch "\.backup" }
if ($results) {
    Write-Host "  ❌ 发现 $($results.Count) 个违规文件" -ForegroundColor Red
    $results | ForEach-Object { Write-Host "     - $($_.Path):$($_.LineNumber)" -ForegroundColor Red }
    Write-Host "  修复命令: .\scripts\fix-hikari-to-druid.ps1" -ForegroundColor Yellow
    $violations++
} else {
    Write-Host "  ✅ 通过" -ForegroundColor Green
}

# 检查5: Repository后缀命名
Write-Host "`n[5/$totalChecks] 检查Repository后缀命名违规..." -ForegroundColor Yellow
$results = Get-ChildItem -Path "microservices" -Filter "*Repository.java" -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.FullName -notmatch "\.backup" }
if ($results) {
    Write-Host "  ❌ 发现 $($results.Count) 个违规文件" -ForegroundColor Red
    $results | ForEach-Object { Write-Host "     - $($_.FullName)" -ForegroundColor Red }
    Write-Host "  修复方法: 重命名为Dao后缀" -ForegroundColor Yellow
    $violations++
} else {
    Write-Host "  ✅ 通过" -ForegroundColor Green
}

# 总结
Write-Host "`n" + "="*50 -ForegroundColor Cyan
Write-Host "检查完成: $totalChecks 项检查" -ForegroundColor Cyan

if ($violations -eq 0) {
    Write-Host "✅ 全部通过！代码符合架构规范，可以提交" -ForegroundColor Green
    Write-Host "`n建议执行: mvn clean compile -DskipTests" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "❌ 发现 $violations 类违规，请修复后再提交" -ForegroundColor Red
    Write-Host "`n快速修复:" -ForegroundColor Yellow
    Write-Host "  1. 运行对应的修复脚本" -ForegroundColor Gray
    Write-Host "  2. 重新执行: .\scripts\check-compliance.ps1" -ForegroundColor Gray
    Write-Host "  3. 编译验证: mvn clean compile -DskipTests" -ForegroundColor Gray
    exit 1
}

