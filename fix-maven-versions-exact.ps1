# 精确修复Maven版本硬编码问题
Write-Host "🔧 精确修复Maven版本硬编码问题..." -ForegroundColor Green

$servicesPath = "D:\IOE-DREAM\microservices"
$services = @(
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-consume-service",
    "ioedream-video-service",
    "ioedream-visitor-service",
    "ioedream-device-comm-service"
)

$totalFixes = 0

foreach ($service in $services) {
    $pomPath = Join-Path $servicesPath $service "pom.xml"

    if (Test-Path $pomPath) {
        Write-Host "`n🔧 修复服务: $service" -ForegroundColor Cyan

        $content = Get-Content -Path $pomPath -Raw -Encoding UTF8
        $originalContent = $content
        $serviceFixes = 0

        # 修复MyBatis-Plus版本硬编码
        if ($content -match '(<artifactId>mybatis-plus-spring-boot3-starter</artifactId>\s*<version>\$\{mybatis-plus\.version\}</version>)') {
            $content = $content -replace '(<artifactId>mybatis-plus-spring-boot3-starter</artifactId>\s*<version>\$\{mybatis-plus\.version\}</version>)', '<artifactId>mybatis-plus-spring-boot3-starter</artifactId>'
            Write-Host "  ✅ 移除MyBatis-Plus版本声明" -ForegroundColor Green
            $serviceFixes++
        }

        # 修复Druid版本硬编码
        if ($content -match '(<artifactId>druid-spring-boot-3-starter</artifactId>\s*<version>\$\{druid\.version\}</version>)') {
            $content = $content -replace '(<artifactId>druid-spring-boot-3-starter</artifactId>\s*<version>\$\{druid\.version\}</version>)', '<artifactId>druid-spring-boot-3-starter</artifactId>'
            Write-Host "  ✅ 移除Druid版本声明" -ForegroundColor Green
            $serviceFixes++
        }

        # 修复Lombok版本硬编码
        if ($content -match '(<artifactId>lombok</artifactId>\s*<version>\$\{lombok\.version\}</version>)') {
            $content = $content -replace '(<artifactId>lombok</artifactId>\s*<version>\$\{lombok\.version\}</version>)', '<artifactId>lombok</artifactId>'
            Write-Host "  ✅ 移除Lombok版本声明" -ForegroundColor Green
            $serviceFixes++
        }

        # 修复JUnit版本硬编码
        if ($content -match '(<artifactId>junit-jupiter</artifactId>\s*<version>\$\{junit\.version\}</version>)') {
            $content = $content -replace '(<artifactId>junit-jupiter</artifactId>\s*<version>\$\{junit\.version\}</version>)', '<artifactId>junit-jupiter</artifactId>'
            Write-Host "  ✅ 移除JUnit版本声明" -ForegroundColor Green
            $serviceFixes++
        }

        # 修复Mockito版本硬编码
        if ($content -match '(<artifactId>mockito-core</artifactId>\s*<version>\$\{mockito\.version\}</version>)') {
            $content = $content -replace '(<artifactId>mockito-core</artifactId>\s*<version>\$\{mockito\.version\}</version>)', '<artifactId>mockito-core</artifactId>'
            Write-Host "  ✅ 移除Mockito Core版本声明" -ForegroundColor Green
            $serviceFixes++
        }

        if ($content -match '(<artifactId>mockito-junit-jupiter</artifactId>\s*<version>\$\{mockito\.version\}</version>)') {
            $content = $content -replace '(<artifactId>mockito-junit-jupiter</artifactId>\s*<version>\$\{mockito\.version\}</version>)', '<artifactId>mockito-junit-jupiter</artifactId>'
            Write-Host "  ✅ 移除Mockito JUnit版本声明" -ForegroundColor Green
            $serviceFixes++
        }

        # 修复格式问题（Resilience4j注释问题）
        $content = $content -replace '(?s)\s*<!-- Resilience4j \(容错机制\) -->\s*<dependency>\s*<groupId>io\.github\.resilience4j</groupId>\s*<artifactId>resilience4j-spring-boot3</artifactId>\s*</dependency><!-- Micrometer \(监控指标\) -->', @'
    <!-- Resilience4j (容错机制) -->
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-spring-boot3</artifactId>
    </dependency>

    <!-- Micrometer (监控指标) -->
'@

        # 如果有修改，保存文件
        if ($content -ne $originalContent) {
            # 清理BOM字符
            $utf8WithBOM = [System.Text.UTF8Encoding]::new($true)
            $utf8WithoutBOM = [System.Text.UTF8Encoding]::new($false)
            $bytes = $utf8WithBOM.GetBytes($content)

            # 移除BOM
            if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
                $bytes = $bytes[3..($bytes.Length-1)]
            }

            [System.IO.File]::WriteAllBytes($pomPath, $bytes)
            Write-Host "  📝 已保存修复: $serviceFixes 个问题" -ForegroundColor Yellow
            $totalFixes += $serviceFixes
        } else {
            Write-Host "  ℹ️ 无需修复" -ForegroundColor Blue
        }
    }
}

Write-Host "`n📊 版本硬编码修复完成!" -ForegroundColor Magenta
Write-Host "  总修复数: $totalFixes" -ForegroundColor Green

# 验证编译
Write-Host "`n🔍 验证Maven编译..." -ForegroundColor Green
cd $servicesPath

# 测试编译（不运行测试）
$compileResult = mvn clean compile -q -DskipTests 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ Maven编译成功！" -ForegroundColor Green
    Write-Host "🎉 Maven依赖架构优化完成！" -ForegroundColor Green
} else {
    Write-Host "  ❌ Maven编译失败" -ForegroundColor Red
    Write-Host "  错误信息:" -ForegroundColor Yellow
    $compileLines = $compileResult -split "`n" | Select-String "ERROR" | Select-Object -First 5
    foreach ($line in $compileLines) {
        Write-Host "    $line" -ForegroundColor Red
    }
}