# =====================================================
# 生成Manager层测试文件模板
# 版本: v1.0.0
# 描述: 为Manager类生成测试文件模板
# =====================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$ManagerPath,

    [Parameter(Mandatory=$false)]
    [switch]$Force = $false
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $ManagerPath)) {
    Write-Host "[ERROR] Manager file not found: $ManagerPath" -ForegroundColor Red
    exit 1
}

# 读取Manager文件
$managerContent = Get-Content $ManagerPath -Raw -Encoding UTF8

# 提取包名
if ($managerContent -match "package\s+([\w\.]+);") {
    $packageName = $matches[1]
} else {
    Write-Host "[ERROR] Cannot extract package name from $ManagerPath" -ForegroundColor Red
    exit 1
}

# 提取类名（支持interface和class）
if ($managerContent -match "(?:interface|class)\s+(\w+Manager)(?:Impl)?") {
    $className = $matches[1]
    $testClassName = $className + "Test"
} else {
    Write-Host "[ERROR] Cannot extract class name from $ManagerPath" -ForegroundColor Red
    exit 1
}

# 确定测试文件路径
$managerDir = Split-Path $ManagerPath -Parent
$testDir = $managerDir -replace "\\main\\java", "\test\java"
$testFilePath = Join-Path $testDir "$testClassName.java"

# 检查文件是否已存在
if ((Test-Path $testFilePath) -and -not $Force) {
    Write-Host "[SKIP] Test file already exists: $testFilePath" -ForegroundColor Yellow
    exit 0
}

# 提取构造函数参数（用于Mock）- 仅当是类而非接口时
$constructorParams = @()
$isInterface = $managerContent -match "interface\s+$className"
if (-not $isInterface) {
    if ($managerContent -match "public\s+$className\(([^)]+)\)") {
        $paramsStr = $matches[1]
        if ($paramsStr.Trim() -ne "") {
            $params = $paramsStr -split ","
            foreach ($param in $params) {
                if ($param -match "(\w+(?:\.\w+)*)\s+(\w+)") {
                    $paramType = $matches[1].Trim()
                    $paramName = $matches[2].Trim()
                    $constructorParams += @{
                        Type = $paramType
                        Name = $paramName
                    }
                }
            }
        }
    }
}

# 生成测试文件内容
$testContent = @"
package $packageName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("${className}单元测试")
class ${testClassName} {

"@

# 添加Mock字段
foreach ($param in $constructorParams) {
    $testContent += @"
    @Mock
    private $($param.Type) $($param.Name);

"@
}

if ($isInterface) {
    # 接口需要Mock实现类
    $testContent += @"
    @Mock
    private ${className} ${className.ToLower()};

    @BeforeEach
    void setUp() {
        // 接口测试使用Mock对象
    }

"@
} else {
    # 类需要构造函数注入
    $testContent += @"
    private ${className} ${className.ToLower()};

    @BeforeEach
    void setUp() {
        ${className.ToLower()} = new ${className}(
"@

    # 添加构造函数参数
    if ($constructorParams.Count -gt 0) {
        $paramNames = $constructorParams | ForEach-Object { $_.Name }
        $paramList = $paramNames -join ", "
        $testContent += "            $paramList`n        );`n    }`n`n"
    } else {
        $testContent += "        );`n    }`n`n"
    }
}

# 添加示例测试方法
$testContent += @"
    @Test
    @DisplayName("测试示例-成功场景")
    void test_example_Success() {
        // Given
        // TODO: 准备测试数据

        // When
        // TODO: 执行被测试方法

        // Then
        // TODO: 验证结果
        assertTrue(true);
    }

}
"@

# 确保测试目录存在
$testDirParent = Split-Path $testFilePath -Parent
if (-not (Test-Path $testDirParent)) {
    New-Item -ItemType Directory -Path $testDirParent -Force | Out-Null
}

# 写入测试文件（UTF-8 without BOM）
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($testFilePath, $testContent, $utf8NoBom)

Write-Host "[OK] Generated test file: $testFilePath" -ForegroundColor Green

