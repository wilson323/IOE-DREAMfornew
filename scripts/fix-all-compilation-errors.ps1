# 全局编译错误修复脚本
# 用途：系统化修复所有编译错误
# 执行：.\scripts\fix-all-compilation-errors.ps1

$ErrorActionPreference = "Stop"

Write-Output "=========================================="
Write-Output "全局编译错误修复脚本"
Write-Output "=========================================="
Write-Output ""

# 切换到microservices目录
$originalDir = Get-Location
Set-Location "microservices"

try {
    # 步骤1：收集所有编译错误
    Write-Output "📋 步骤1：收集所有编译错误..."
    $compileOutput = mvn clean compile -DskipTests 2>&1 | Out-String
    $errors = $compileOutput | Select-String -Pattern "ERROR.*找不到符号|ERROR.*程序包.*不存在" -AllMatches

    if ($errors.Count -eq 0) {
        Write-Output "✅ 未发现编译错误！"
        exit 0
    }

    Write-Output "发现 $($errors.Count) 个编译错误"
    Write-Output ""

    # 步骤2：分析错误类型
    Write-Output "📋 步骤2：分析错误类型..."

    $missingPackages = @{}
    $missingSymbols = @{}

    foreach ($error in $errors) {
        $errorLine = $error.Line

        # 提取缺失的包
        if ($errorLine -match "程序包(.+?)不存在") {
            $package = $matches[1].Trim()
            if (-not $missingPackages.ContainsKey($package)) {
                $missingPackages[$package] = @()
            }
            $missingPackages[$package] += $errorLine
        }

        # 提取缺失的符号
        if ($errorLine -match "找不到符号.*符号:\s*类\s+(\S+)") {
            $symbol = $matches[1].Trim()
            if (-not $missingSymbols.ContainsKey($symbol)) {
                $missingSymbols[$symbol] = @()
            }
            $missingSymbols[$symbol] += $errorLine
        }
    }

    Write-Output "缺失的包: $($missingPackages.Count) 个"
    Write-Output "缺失的符号: $($missingSymbols.Count) 个"
    Write-Output ""

    # 步骤3：生成修复建议
    Write-Output "📋 步骤3：生成修复建议..."
    Write-Output ""

    # 常见依赖映射
    $dependencyMap = @{
        "jakarta.validation.constraints" = @{
            GroupId    = "jakarta.validation"
            ArtifactId = "jakarta.validation-api"
        }
        "org.springframework.web.client" = @{
            GroupId    = "org.springframework"
            ArtifactId = "spring-web"
        }
        "org.springframework.web"        = @{
            GroupId    = "org.springframework"
            ArtifactId = "spring-web"
        }
    }

    # 符号到依赖的映射
    $symbolMap = @{
        "RestTemplate" = @{
            GroupId    = "org.springframework"
            ArtifactId = "spring-web"
        }
        "NotBlank"     = @{
            GroupId    = "jakarta.validation"
            ArtifactId = "jakarta.validation-api"
        }
        "NotNull"      = @{
            GroupId    = "jakarta.validation"
            ArtifactId = "jakarta.validation-api"
        }
    }

    # 输出修复建议
    Write-Output "修复建议："
    Write-Output "----------------------------------------"

    $suggestions = @{}

    foreach ($package in $missingPackages.Keys) {
        if ($dependencyMap.ContainsKey($package)) {
            $dep = $dependencyMap[$package]
            $key = "$($dep.GroupId):$($dep.ArtifactId)"
            if (-not $suggestions.ContainsKey($key)) {
                $suggestions[$key] = @{
                    GroupId    = $dep.GroupId
                    ArtifactId = $dep.ArtifactId
                    Reason     = "缺少包: $package"
                }
            }
        }
    }

    foreach ($symbol in $missingSymbols.Keys) {
        if ($symbolMap.ContainsKey($symbol)) {
            $dep = $symbolMap[$symbol]
            $key = "$($dep.GroupId):$($dep.ArtifactId)"
            if (-not $suggestions.ContainsKey($key)) {
                $suggestions[$key] = @{
                    GroupId    = $dep.GroupId
                    ArtifactId = $dep.ArtifactId
                    Reason     = "缺少符号: $symbol"
                }
            }
        }
    }

    foreach ($suggestion in $suggestions.Values) {
        Write-Output ""
        Write-Output "依赖: $($suggestion.GroupId):$($suggestion.ArtifactId)"
        Write-Output "原因: $($suggestion.Reason)"
        Write-Output "XML:"
        Write-Output "<dependency>"
        Write-Output "  <groupId>$($suggestion.GroupId)</groupId>"
        Write-Output "  <artifactId>$($suggestion.ArtifactId)</artifactId>"
        Write-Output "</dependency>"
    }

    Write-Output ""
    Write-Output "=========================================="
    Write-Output "修复建议生成完成"
    Write-Output "=========================================="

}
finally {
    Set-Location $originalDir
}

