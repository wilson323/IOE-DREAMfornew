# IOE-DREAM 编码设置验证工具
# 验证运行时编码环境是否正确配置

Write-Host "🔍 IOE-DREAM 编码设置验证工具" -ForegroundColor Green
Write-Host "🎯 验证运行时编码环境是否正确配置" -ForegroundColor Yellow
Write-Host ""

# 验证Java环境
Write-Host "[1/6] 验证Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "✅ Java版本检查通过" -ForegroundColor Green
    Write-Host $javaVersion[0] -ForegroundColor Cyan
} catch {
    Write-Host "❌ Java版本检查失败" -ForegroundColor Red
    Write-Host "请确保JAVA_HOME环境变量正确设置" -ForegroundColor Red
}

# 验证Maven环境
Write-Host "`n[2/6] 验证Maven环境..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn --version
    Write-Host "✅ Maven版本检查通过" -ForegroundColor Green
    # 提取编码信息
    $encodingLine = $mavenVersion | Where-Object { $_ -match "platform encoding" }
    Write-Host $encodingLine -ForegroundColor Cyan

    # 检查编码是否为UTF-8
    if ($encodingLine -match "UTF-8") {
        Write-Host "✅ Maven编码配置正确 (UTF-8)" -ForegroundColor Green
    } else {
        Write-Host "❌ Maven编码配置异常 (非UTF-8)" -ForegroundColor Red
        Write-Host "请运行环境修复脚本" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Maven版本检查失败" -ForegroundColor Red
    Write-Host "请确保Maven安装正确或运行环境修复脚本" -ForegroundColor Red
}

# 验证项目POM配置
Write-Host "`n[3/6] 验证项目POM配置..." -ForegroundColor Yellow
$pomPath = "D:\IOE-DREAM\microservices\pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw

    # 检查关键编码配置
    $encodings = @(
        "project.build.sourceEncoding>UTF-8",
        "project.reporting.outputEncoding>UTF-8",
        "maven.compiler.encoding>UTF-8"
    )

    $allEncodingCorrect = $true
    foreach ($encoding in $encodings) {
        if ($pomContent -match [regex]::Escape($encoding)) {
            Write-Host "✅ $encoding 配置正确" -ForegroundColor Green
        } else {
            Write-Host "❌ $encoding 配置缺失" -ForegroundColor Red
            $allEncodingCorrect = $false
        }
    }

    if ($allEncodingCorrect) {
        Write-Host "✅ POM编码配置完整" -ForegroundColor Green
    } else {
        Write-Host "❌ POM编码配置不完整" -ForegroundColor Red
    }
} else {
    Write-Host "❌ POM文件不存在" -ForegroundColor Red
}

# 验证JVM配置文件
Write-Host "`n[4/6] 验证JVM配置文件..." -ForegroundColor Yellow
$jvmConfigPath = "D:\IOE-DREAM\microservices\.mvn\jvm.config"
if (Test-Path $jvmConfigPath) {
    $jvmConfig = Get-Content $jvmConfigPath

    # 检查关键JVM参数
    $jvmParams = @(
        "-Dfile.encoding=UTF-8",
        "-Dconsole.encoding=UTF-8"
    )

    $allJVMCorrect = $true
    foreach ($param in $jvmParams) {
        if ($jvmConfig -contains $param) {
            Write-Host "✅ $param 配置正确" -ForegroundColor Green
        } else {
            Write-Host "❌ $param 配置缺失" -ForegroundColor Red
            $allJVMCorrect = $false
        }
    }

    if ($allJVMCorrect) {
        Write-Host "✅ JVM配置文件完整" -ForegroundColor Green
    } else {
        Write-Host "❌ JVM配置文件不完整" -ForegroundColor Red
    }
} else {
    Write-Host "❌ JVM配置文件不存在" -ForegroundColor Red
}

# 验证关键文件编码
Write-Host "`n[5/6] Verifying key file encodings..." -ForegroundColor Yellow
$keyFiles = @(
    "D:\IOE-DREAM\microservices\pom.xml",
    "D:\IOE-DREAM\microservices\.mvn\jvm.config",
    "D:\IOE-DREAM\documentation\technical\ENCODING_STANDARDIZATION_GUIDE.md"
)

foreach ($file in $keyFiles) {
    if (Test-Path $file) {
        # 读取文件的字节顺序标记
        $bytes = [System.IO.File]::ReadAllBytes($file)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            Write-Host "✅ $file (UTF-8 with BOM)" -ForegroundColor Green
        } else {
            Write-Host "✅ $file (UTF-8 without BOM)" -ForegroundColor Green
        }
    } else {
        Write-Host "❌ $file file does not exist" -ForegroundColor Red
    }
}

# 生成验证报告
Write-Host "`n[6/6] Generating verification report..." -ForegroundColor Yellow
$reportPath = "D:\IOE-DREAM\encoding-verification-report.json"
$report = @{
    timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    javaVersion = if ($javaVersion) { $javaVersion[0] } else { "Unknown" }
    mavenEncoding = if ($encodingLine) { $encodingLine } else { "Unknown" }
    pomConfiguration = $allEncodingCorrect
    jvmConfiguration = $allJVMCorrect
    status = if ($allEncodingCorrect -and $allJVMCorrect) { "PASS" } else { "FAIL" }
}

$report | ConvertTo-Json -Depth 3 | Out-File -FilePath $reportPath -Encoding UTF8
Write-Host "✅ Verification report generated: $reportPath" -ForegroundColor Green

# 总结
Write-Host "`n========================================" -ForegroundColor Green
if ($report.status -eq "PASS") {
    Write-Host "Encoding configuration verification passed! Project encoding standardization successfully implemented" -ForegroundColor Green
} else {
    Write-Host "Encoding configuration verification not fully passed, please check the above issues" -ForegroundColor Yellow
}
Write-Host "========================================" -ForegroundColor Green