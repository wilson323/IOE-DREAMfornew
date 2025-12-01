# IOE-DREAM 项目编译错误系统性修复脚本
# 执行时间: 2025-11-19
# 修复范围: 所有编译错误

Write-Host "🔧 开始系统性修复编译错误..." -ForegroundColor Cyan

$baseDir = "smart-admin-api-java17-springboot3"
$adminDir = "$baseDir/sa-admin/src/main/java"
$baseSrcDir = "$baseDir/sa-base/src/main/java"

# ==================== 步骤1: 修复UTF-8 BOM标记问题 ====================
Write-Host "`n步骤1: 修复UTF-8 BOM标记问题..." -ForegroundColor Yellow

$bomFiles = @(
    "$adminDir/net/lab1024/sa/admin/module/attendance/manager/AttendanceCacheManager.java",
    "$adminDir/net/lab1024/sa/admin/module/attendance/manager/AttendanceRuleEngine.java",
    "$adminDir/net/lab1024/sa/admin/module/attendance/repository/AttendanceRuleRepository.java",
    "$adminDir/net/lab1024/sa/admin/module/attendance/rule/AttendanceRuleEngine.java",
    "$adminDir/net/lab1024/sa/admin/module/attendance/service/AttendanceRuleService.java",
    "$adminDir/net/lab1024/sa/admin/module/attendance/service/impl/AttendanceRuleServiceImpl.java",
    "$adminDir/net/lab1024/sa/admin/module/attendance/service/impl/AttendanceServiceSimpleImpl.java"
)

foreach ($file in $bomFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        # 移除BOM标记
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
            [System.IO.File]::WriteAllText($file, $content, [System.Text.UTF8Encoding]::new($false))
            Write-Host "  ✅ 已修复BOM: $file" -ForegroundColor Green
        }
    }
}

# ==================== 步骤2: 修复@Resources注解问题 ====================
Write-Host "`n步骤2: 修复@Resources注解问题..." -ForegroundColor Yellow

$javaFiles = Get-ChildItem -Path $baseDir -Filter "*.java" -Recurse | Where-Object { $_.FullName -notmatch "\\target\\" }

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $modified = $false

    # 修复 @Resources -> @Resource
    if ($content -match '@Resources\b' -or $content -match 'jakarta\.annotation\.Resources') {
        $content = $content -replace 'jakarta\.annotation\.Resources', 'jakarta.annotation.Resource'
        $content = $content -replace '@Resources\b', '@Resource'
        $modified = $true
    }

    if ($modified) {
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
        Write-Host "  ✅ 已修复注解: $($file.Name)" -ForegroundColor Green
    }
}

# ==================== 步骤3: 修复包导入路径 ====================
Write-Host "`n步骤3: 修复包导入路径..." -ForegroundColor Yellow

# 修复 BaseCacheManager 导入路径
$filesNeedingBaseCacheManager = Get-ChildItem -Path $adminDir -Filter "*.java" -Recurse | Where-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    $content -match 'net\.lab1024\.sa\.base\.common\.manager\.BaseCacheManager'
}

foreach ($file in $filesNeedingBaseCacheManager) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.manager\.BaseCacheManager;', 'import net.lab1024.sa.base.common.cache.BaseCacheManager;'
    $content = $content -replace 'extends BaseCacheManager', 'extends BaseCacheManager'
    Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  ✅ 已修复BaseCacheManager导入: $($file.Name)" -ForegroundColor Green
}

Write-Host "`n✅ 系统性修复完成！" -ForegroundColor Green
Write-Host "请运行编译验证: cd $baseDir; mvn clean compile -DskipTests" -ForegroundColor Cyan

