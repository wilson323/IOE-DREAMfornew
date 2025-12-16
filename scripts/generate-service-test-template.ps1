# =====================================================
# Service层测试用例模板生成脚本
# Version: v1.0.0
# Description: 为Service实现类生成测试用例模板
# Created: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$ServiceImplPath,
    
    [Parameter(Mandatory=$false)]
    [string]$OutputPath = ""
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Service Test Template Generator" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 解析ServiceImpl路径
$serviceImplFile = Get-Item $ServiceImplPath -ErrorAction Stop
$serviceImplContent = Get-Content $serviceImplFile.FullName -Raw

# 提取类名
$className = $serviceImplFile.BaseName
$testClassName = $className -replace "Impl$", "ImplTest"

# 提取包名
$packageMatch = [regex]::Match($serviceImplContent, "package\s+([^;]+);")
$packageName = if ($packageMatch.Success) { $packageMatch.Groups[1].Value } else { "" }
$testPackageName = $packageName -replace "\.service\.impl$", ".service"

# 确定输出路径
if ([string]::IsNullOrEmpty($OutputPath)) {
    $testDir = $serviceImplFile.DirectoryName -replace "\\main\\java", "\test\java"
    $testPackageDir = $testPackageName -replace "\.", "\"
    $OutputPath = Join-Path $testDir $testPackageDir
}

# 创建测试目录
if (-not (Test-Path $OutputPath)) {
    New-Item -ItemType Directory -Path $OutputPath -Force | Out-Null
}

$testFilePath = Join-Path $OutputPath "$testClassName.java"

# 检查文件是否已存在
if (Test-Path $testFilePath) {
    Write-Host "[WARN] Test file already exists: $testFilePath" -ForegroundColor Yellow
    $overwrite = Read-Host "Overwrite? (y/N)"
    if ($overwrite -ne "y" -and $overwrite -ne "Y") {
        Write-Host "[INFO] Skipped" -ForegroundColor Gray
        exit 0
    }
}

# 提取Service接口方法
$methodMatches = [regex]::Matches($serviceImplContent, "public\s+(\w+)\s+(\w+)\s*\([^)]*\)")
$methods = @()
foreach ($match in $methodMatches) {
    $returnType = $match.Groups[1].Value
    $methodName = $match.Groups[2].Value
    if ($methodName -notmatch "^(equals|hashCode|toString|getClass)$") {
        $methods += @{
            ReturnType = $returnType
            MethodName = $methodName
        }
    }
}

# 提取@Resource依赖
$resourceMatches = [regex]::Matches($serviceImplContent, "@Resource\s+private\s+(\w+)\s+(\w+);")
$dependencies = @()
foreach ($match in $resourceMatches) {
    $type = $match.Groups[1].Value
    $name = $match.Groups[2].Value
    $dependencies += @{
        Type = $type
        Name = $name
    }
}

# 生成测试类模板
$testClassTemplate = @"
package $testPackageName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import $packageName.$className;

/**
 * ${className}单元测试
 * <p>
 * 目标覆盖率：≥80%
 * 测试范围：${className}核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("${className}单元测试")
class $testClassName {

$(foreach ($dep in $dependencies) {
    "    @Mock`n    private $($dep.Type) $($dep.Name);`n"
})

    @InjectMocks
    private $className ${className.Substring(0,1).ToLower() + $className.Substring(1)};

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

$(foreach ($method in $methods) {
    $methodName = $method.MethodName
    $returnType = $method.ReturnType
    "    @Test`n    @DisplayName(`"测试${methodName}-成功场景`")`n    void test_${methodName}_Success() {`n        // Given`n        `n        // When`n        `n        // Then`n        `n    }`n`n"
})
}
"@

# 写入文件
[System.IO.File]::WriteAllText($testFilePath, $testClassTemplate, [System.Text.UTF8Encoding]::new($false))

Write-Host "[OK] Test template generated: $testFilePath" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor White
Write-Host "  1. Review and complete the test methods" -ForegroundColor Gray
Write-Host "  2. Add test data setup in setUp() method" -ForegroundColor Gray
Write-Host "  3. Implement test cases for each method" -ForegroundColor Gray
Write-Host "  4. Run tests: mvn test" -ForegroundColor Gray
Write-Host ""

