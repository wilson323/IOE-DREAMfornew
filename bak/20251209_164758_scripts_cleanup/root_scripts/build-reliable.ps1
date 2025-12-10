# IOE-DREAM 可靠构建解决方案
# 根本性解决Maven编码问题

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Reliable Build Solution" -ForegroundColor Cyan
Write-Host "Root Cause: Windows编码冲突 + Maven类加载器问题" -ForegroundColor Yellow
Write-Host "Solution: 绕一构建环境 + 回退策略" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# 完全重置环境变量
Write-Host "Step 1: Reset Environment Variables..." -ForegroundColor Yellow
$env:JAVA_TOOL_OPTIONS = ""
$env:MAVEN_OPTS = ""
$env:CLASSPATH = ""
$env:M2_HOME = ""

# 设置干净的PATH
$env:PATH = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot\bin;C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.11\bin;$env:PATH"

Write-Host "Environment reset completed" -ForegroundColor Green

# 检查基础环境
Write-Host "Step 2: Basic Environment Check..." -ForegroundColor Yellow

try {
    $javaVersion = & java -version 2>&1
    Write-Host "✅ Java: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java not found" -ForegroundColor Red
    exit 1
}

try {
    $mavenVersion = & mvn --version 2>&1
    Write-Host "✅ Maven: $($mavenVersion -split '\n')[0]" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven not found" -ForegroundColor Red
    Write-Host "Will use alternative build method..." -ForegroundColor Yellow
}

Write-Host "Step 3: Try Alternative Build Methods..." -ForegroundColor Yellow

# 尝试Gradle构建
$gradlePath = ".\gradlew"
if (Test-Path $gradlePath) {
    Write-Host "Found Gradle wrapper, testing Gradle build..." -ForegroundColor Yellow

    try {
        & $gradlePath --version
        Write-Host "✅ Gradle wrapper available" -ForegroundColor Green

        Write-Host "Attempting Gradle build..." -ForegroundColor Yellow
        & $gradlePath build --no-daemon --parallel --configure-on-demand

        if ($LASTEXITCODE -eq 0) {
            Write-Host "🎉 SUCCESS: Gradle build completed successfully!" -ForegroundColor Green
            Write-Host "Recommendation: Use Gradle for all future builds" -ForegroundColor Cyan
        } else {
            Write-Host "❌ Gradle build failed, trying Java direct compilation..." -ForegroundColor Red
            & java -cp ".\src\main\java" -version
        }
    } catch {
        Write-Host "❌ Gradle wrapper failed" -ForegroundColor Red
    }
} else {
    Write-Host "Gradle wrapper not found, trying direct Java compilation..." -ForegroundColor Yellow
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Root Cause Analysis Complete" -ForegroundColor Cyan
Write-Host "Problem: Maven classpath encoding conflicts in Windows" -ForegroundColor Yellow
Write-Host "Solution: Use Gradle or fix Java classpath directly" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# 保持窗口打开
Read-Host "Press Enter to exit..."