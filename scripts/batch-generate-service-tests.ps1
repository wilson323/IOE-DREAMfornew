# =====================================================
# Service层测试用例批量生成脚本
# Version: v1.0.0
# Description: 批量为Service实现类生成测试用例模板
# Created: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$ServiceModule = "",

    [Parameter(Mandatory=$false)]
    [switch]$SkipExisting
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Batch Service Test Generator" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesDir = Join-Path $projectRoot "microservices"

# 查找所有ServiceImpl文件
$serviceImplFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*ServiceImpl.java" |
    Where-Object { $_.FullName -match "\\src\\main\\java\\" }

# 过滤已存在的测试文件
$existingTests = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*ServiceImplTest.java" |
    Where-Object { $_.FullName -match "\\src\\test\\java\\" }

$existingTestNames = $existingTests | ForEach-Object {
    $_.BaseName -replace "Test$", ""
}

$serviceImplFiles = $serviceImplFiles | Where-Object {
    $serviceName = $_.BaseName
    if ($SkipExisting -and ($existingTestNames -contains $serviceName)) {
        return $false
    }
    return $true
}

# 如果指定了服务模块，进行过滤
if (-not [string]::IsNullOrEmpty($ServiceModule)) {
    $serviceImplFiles = $serviceImplFiles | Where-Object {
        $_.FullName -match $ServiceModule
    }
}

Write-Host "[INFO] Found $($serviceImplFiles.Count) ServiceImpl files to process" -ForegroundColor Yellow
Write-Host ""

$generatedCount = 0
$skippedCount = 0
$errorCount = 0

foreach ($serviceImplFile in $serviceImplFiles) {
    try {
        $serviceImplContent = Get-Content $serviceImplFile.FullName -Raw

        # 提取类名
        $className = $serviceImplFile.BaseName
        $testClassName = $className + "Test"

        # 提取包名
        $packageMatch = [regex]::Match($serviceImplContent, "package\s+([^;]+);")
        if (-not $packageMatch.Success) {
            Write-Host "[WARN] Cannot extract package from: $($serviceImplFile.FullName)" -ForegroundColor Yellow
            $skippedCount++
            continue
        }

        $packageName = $packageMatch.Groups[1].Value
        $testPackageName = $packageName -replace "\.service\.impl$", ".service"

        # 确定输出路径 - 修复路径计算逻辑
        $serviceImplDir = $serviceImplFile.DirectoryName
        $baseDir = $serviceImplDir -replace "\\src\\main\\java.*$", ""
        $testBaseDir = Join-Path $baseDir "src\test\java"
        $testPackageDir = $testPackageName -replace "\.", "\"
        $outputPath = Join-Path $testBaseDir $testPackageDir

        # 创建测试目录
        if (-not (Test-Path $outputPath)) {
            New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
        }

        $testFilePath = Join-Path $outputPath "$testClassName.java"

        # 检查文件是否已存在
        if (Test-Path $testFilePath) {
            Write-Host "[SKIP] Test file already exists: $testClassName" -ForegroundColor Gray
            $skippedCount++
            continue
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

        # 提取public方法
        $methodMatches = [regex]::Matches($serviceImplContent, "public\s+(\w+)\s+(\w+)\s*\([^)]*\)")
        $methods = @()
        foreach ($match in $methodMatches) {
            $returnType = $match.Groups[1].Value
            $methodName = $match.Groups[2].Value
            if ($methodName -notmatch "^(equals|hashCode|toString|getClass)$" -and
                $methodName -notmatch "^fallback$") {
                $methods += @{
                    ReturnType = $returnType
                    MethodName = $methodName
                }
            }
        }

        # 生成测试类模板
        $mockFields = ""
        foreach ($dep in $dependencies) {
            $mockFields += "    @Mock`n    private $($dep.Type) $($dep.Name);`n`n"
        }

        $testMethods = ""
        foreach ($method in $methods) {
            $methodName = $method.MethodName
            $testMethods += "    @Test`n    @DisplayName(`"测试${methodName}-成功场景`")`n    void test_${methodName}_Success() {`n        // Given`n        `n        // When`n        `n        // Then`n        `n    }`n`n"
        }

        $serviceVarName = $className.Substring(0,1).ToLower() + $className.Substring(1)

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

$mockFields    @InjectMocks
    private $className $serviceVarName;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

$testMethods}
"@

        # 写入文件
        [System.IO.File]::WriteAllText($testFilePath, $testClassTemplate, [System.Text.UTF8Encoding]::new($false))

        Write-Host "[OK] Generated: $testClassName" -ForegroundColor Green
        $generatedCount++

    } catch {
        Write-Host "[ERROR] Failed to generate test for $($serviceImplFile.Name): $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "[OK] Generated: $generatedCount" -ForegroundColor Green
Write-Host "[SKIP] Skipped: $skippedCount" -ForegroundColor Yellow
Write-Host "[ERROR] Errors: $errorCount" -ForegroundColor Red
Write-Host ""

