# 简化的 Consume Service Controller 修复脚本

$controllerPath = "D:\IOE-DREAM\microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\controller"

# 使用 .NET 方法处理BOM
$utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)

$controllers = Get-ChildItem -Path $controllerPath -Filter "*Controller.java"

Write-Host "开始修复 consume-service Controller 文件..." -ForegroundColor Green

foreach ($controller in $controllers) {
    Write-Host "正在修复: $($controller.Name)" -ForegroundColor Cyan

    try {
        # 读取文件内容（自动处理BOM）
        $content = [IO.File]::ReadAllText($controller.FullName, $utf8WithoutBom)

        $className = $controller.BaseName

        # 修复类声明 - 查找各种模式的孤立大括号
        $patterns = @(
            @(@PermissionCheck\([^)]+\)\s*\{\s*$, "@PermissionCheck(value = `"$className`", description = `"$className 管理权限`")`r`npublic class $className {"),
            (@@Tag\([^)]+\)\s*\{\s*$, "@Tag(name = `"$className`", description = `"$className 管理接口`")`r`npublic class $className {"),
            (@@RequestMapping\([^)]+\)\s*\{\s*$, "@RequestMapping(`/api/v1/consume/$($className.ToLower())`)" + "`r`npublic class $className {"),
            (@}\s*private static final Logger, "} " + "`r`n" + "`r`n    private static final Logger"),
            (@^\s*\{\s*$, "public class $className {")
        )

        foreach ($pattern in $patterns) {
            $content = $content -replace $pattern[0], $pattern[1]
        }

        # 移除 @Slf4j
        $content = $content -replace '@Slf4j\s*\r?\n', ''

        # 写回文件（不带BOM）
        [IO.File]::WriteAllText($controller.FullName, $content, $utf8WithoutBom)

        Write-Host "✓ 修复完成: $($controller.Name)" -ForegroundColor Green

    } catch {
        Write-Host "✗ 修复失败: $($controller.Name) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "Consume Service Controller 修复完成!" -ForegroundColor Green