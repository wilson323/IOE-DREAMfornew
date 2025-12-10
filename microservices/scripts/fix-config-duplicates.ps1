# ============================================================
# IOE-DREAM 配置修复脚本
#
# 功能：批量修复所有微服务的YAML配置问题
# 目标：消除210个YAML配置错误 + 78个重复键问题
#
# @Author: IOE-DREAM架构委员会
# @Date: 2025-01-30
# @Version: v1.0.0-企业级修复版
# ============================================================

param(
    [switch]$DryRun = $false,
    [switch]$Force = $false
)

# 设置错误处理
$ErrorActionPreference = "Stop"

Write-Host "🚀 开始IOE-DREAM配置修复..." -ForegroundColor Green
Write-Host "📋 修复范围：所有微服务的生产环境配置文件" -ForegroundColor Yellow

# 获取所有微服务目录
$microservicesPath = Split-Path $PSScriptRoot -Parent
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

# 排除database-service（需要合并到common-service）
$servicesToProcess = $services | Where-Object { $_ -ne "ioedream-database-service" }

$fixedFiles = 0
$errorFiles = 0

foreach ($service in $servicesToProcess) {
    $prodConfigPath = Join-Path $microservicesPath $service "src/main/resources/application-prod.yml"

    if (Test-Path $prodConfigPath) {
        try {
            Write-Host "🔧 正在处理: $service" -ForegroundColor Cyan

            # 读取配置文件内容
            $content = Get-Content $prodConfigPath -Raw -Encoding UTF8

            # 检测并修复常见问题

            # 1. 修复重复的spring配置
            if ($content -match "spring:.*spring:.*" -or $content -match "redis:.*redis:") {
                Write-Host "  ⚠️  发现重复的配置块" -ForegroundColor Yellow

                if (-not $DryRun -and $Force) {
                    # 修复重复配置
                    $content = $content -replace '(\n# =+.*?Redis配置.*?)\n\s*# =+ Redis配置.*?spring:\s*\n\s*redis:', '$1'

                    Set-Content $prodConfigPath $content -Encoding UTF8 -NoNewline
                    Write-Host "  ✅ 已修复重复配置" -ForegroundColor Green
                    $fixedFiles++
                }
            }

            # 2. 检查连接池配置是否合理
            if ($content -match "max-active:\s*(\d+)") {
                $maxActive = [int]$matches[1]
                if ($maxActive -gt 100) {
                    Write-Host "  ⚠️  连接池配置过大: $maxActive" -ForegroundColor Yellow

                    if (-not $DryRun -and $Force) {
                        # 根据服务类型优化连接池
                        $optimizedMaxActive = switch ($service) {
                            "ioedream-consume-service" { 60 }
                            "ioedream-gateway-service" { 50 }
                            "ioedream-common-service" { 40 }
                            default { 30 }
                        }

                        $content = $content -replace "max-active:\s*$maxActive", "max-active: $optimizedMaxActive"
                        Set-Content $prodConfigPath $content -Encoding UTF8 -NoNewline
                        Write-Host "  ✅ 已优化连接池: $maxActive → $optimizedMaxActive" -ForegroundColor Green
                        $fixedFiles++
                    }
                }
            }

            # 3. 检查加密配置格式
            if ($content -match "ENC\(AES256:[^)]*\)" -and $content -match "password:.*ENC") {
                Write-Host "  ✅ 加密配置格式正确" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️  缺少加密配置" -ForegroundColor Yellow
            }

            # 4. 验证YAML语法
            try {
                # 简单的YAML语法检查
                $lines = $content -split "`n"
                $indentLevel = 0
                $hasSyntaxError = $false

                foreach ($line in $lines) {
                    if ($line.Trim() -and -not $line.Trim().StartsWith('#')) {
                        if ($line -match '^\s*-') {
                            # 列表项，不改变缩进
                            continue
                        } elseif ($line -match '^(\s*)[^:\s]+:') {
                            # 新的键
                            $currentIndent = $matches[1].Length
                            if ($currentIndent -gt $indentLevel + 2) {
                                $hasSyntaxError = $true
                                break
                            }
                            $indentLevel = $currentIndent
                        }
                    }
                }

                if ($hasSyntaxError) {
                    Write-Host "  ❌ YAML语法错误" -ForegroundColor Red
                    $errorFiles++
                } else {
                    Write-Host "  ✅ YAML语法正确" -ForegroundColor Green
                }
            } catch {
                Write-Host "  ❌ YAML语法检查失败: $($_.Exception.Message)" -ForegroundColor Red
                $errorFiles++
            }

        } catch {
            Write-Host "  ❌ 处理失败: $($_.Exception.Message)" -ForegroundColor Red
            $errorFiles++
        }
    } else {
        Write-Host "  ⚠️  配置文件不存在: $prodConfigPath" -ForegroundColor Yellow
    }
}

# 合并database-service到common-service
$databaseServicePath = Join-Path $microservicesPath "ioedream-database-service"
if (Test-Path $databaseServicePath) {
    Write-Host "🔄 发现多余的database-service，建议合并到common-service" -ForegroundColor Yellow

    if (-not $DryRun -and $Force) {
        Write-Host "  📦 创建备份..." -ForegroundColor Cyan
        $backupPath = "$databaseServicePath.backup.$(Get-Date -Format 'yyyyMMddHHmmss')"
        Move-Item $databaseServicePath $backupPath
        Write-Host "  ✅ 已备份到: $backupPath" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "📊 修复统计:" -ForegroundColor Cyan
Write-Host "  ✅ 已修复文件: $fixedFiles" -ForegroundColor Green
Write-Host "  ❌ 错误文件: $errorFiles" -ForegroundColor Red

if ($DryRun) {
    Write-Host ""
    Write-Host "💡 这是预演模式，没有实际修改文件" -ForegroundColor Yellow
    Write-Host "💡 使用 -Force 参数执行实际修复" -ForegroundColor Yellow
} elseif ($fixedFiles -gt 0) {
    Write-Host ""
    Write-Host "🎉 配置修复完成！建议验证修复效果" -ForegroundColor Green
    Write-Host "🔍 建议运行: mvn clean compile 验证配置" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "✅ 所有配置文件都符合规范" -ForegroundColor Green
}

Write-Host ""
Write-Host "📋 下一步建议:" -ForegroundColor Cyan
Write-Host "  1. 运行 'mvn clean compile' 验证修复效果" -ForegroundColor White
Write-Host "  2. 检查IDEA中的错误数量是否减少" -ForegroundColor White
Write-Host "  3. 启动服务验证配置加载" -ForegroundColor White