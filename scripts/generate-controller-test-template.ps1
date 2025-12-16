# =====================================================
# 生成Controller层测试文件模板
# 版本: v1.0.0
# 描述: 为Controller类生成测试文件模板
# =====================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$ControllerPath,

    [Parameter(Mandatory=$false)]
    [switch]$Force = $false
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $ControllerPath)) {
    Write-Host "[ERROR] Controller file not found: $ControllerPath" -ForegroundColor Red
    exit 1
}

# 读取Controller文件
$controllerContent = Get-Content $ControllerPath -Raw -Encoding UTF8

# 提取包名
if ($controllerContent -match "package\s+([\w\.]+);") {
    $packageName = $matches[1]
} else {
    Write-Host "[ERROR] Cannot extract package name from $ControllerPath" -ForegroundColor Red
    exit 1
}

# 提取类名
if ($controllerContent -match "class\s+(\w+Controller)") {
    $className = $matches[1]
    $testClassName = $className + "Test"
} else {
    Write-Host "[ERROR] Cannot extract class name from $ControllerPath" -ForegroundColor Red
    exit 1
}

# 确定测试文件路径
$controllerDir = Split-Path $ControllerPath -Parent
$testDir = $controllerDir -replace "\\main\\java", "\test\java"
$testFilePath = Join-Path $testDir "$testClassName.java"

# 检查文件是否已存在
if ((Test-Path $testFilePath) -and -not $Force) {
    Write-Host "[SKIP] Test file already exists: $testFilePath" -ForegroundColor Yellow
    exit 0
}

# 提取@Resource依赖（用于Mock）
$dependencies = @()
if ($controllerContent -match "@Resource\s+private\s+(\w+)\s+(\w+);") {
    $matches = [regex]::Matches($controllerContent, "@Resource\s+private\s+(\w+)\s+(\w+);")
    foreach ($match in $matches) {
        $dependencies += @{
            Type = $match.Groups[1].Value
            Name = $match.Groups[2].Value
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import $packageName.$className;

/**
 * ${className}单元测试
 * <p>
 * 目标覆盖率：≥70%
 * 测试范围：${className}核心API方法
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
foreach ($dep in $dependencies) {
    $testContent += @"
    @Mock
    private $($dep.Type) $($dep.Name);

"@
}

$testContent += @"
    @InjectMocks
    private ${className} ${className.ToLower()};

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("测试示例-成功场景")
    void test_example_Success() {
        // Given
        // TODO: 准备请求数据
        // when(service.method(any())).thenReturn(ResponseDTO.ok(result));

        // When
        // TODO: 执行Controller方法
        // ResponseDTO<ResultDTO> response = controller.method(request);

        // Then
        // TODO: 验证结果
        // assertEquals(200, response.getCode());
        // assertNotNull(response.getData());
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

