# =====================================================
# Manager Bean注册检查脚本
# 版本: v1.0.0
# 描述: 检查所有Service类需要的Manager是否已注册
# 创建时间: 2025-12-11
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$OutputDir = "documentation/technical",
    
    [Parameter(Mandatory=$false)]
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"

# 设置输出文件路径
$outputFile = Join-Path $OutputDir "MANAGER_BEAN_CHECK_REPORT.md"

Write-Host "[INFO] 开始检查Manager Bean注册..." -ForegroundColor Green

# 1. 收集所有Service类中注入的Manager
$serviceManagerDeps = @{}

$serviceFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*ServiceImpl.java" | Where-Object {
    $_.FullName -notmatch "\\test\\" -and $_.FullName -match "service\\impl"
}

Write-Host "[INFO] 扫描 $($serviceFiles.Count) 个Service类..." -ForegroundColor Cyan

foreach ($file in $serviceFiles) {
    try {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        
        # 提取类名
        if ($content -match "public\s+class\s+(\w+ServiceImpl)") {
            $className = $matches[1]
            
            # 确定微服务名称
            $serviceName = "unknown"
            if ($file.FullName -match "ioedream-(\w+)-service") {
                $serviceName = $matches[1]
            }
            
            # 提取@Resource注入的Manager
            $matches = [regex]::Matches($content, "@Resource\s+private\s+(\w+Manager)\s+\w+;")
            
            foreach ($match in $matches) {
                $managerType = $match.Groups[1].Value
                
                if (-not $serviceManagerDeps.ContainsKey($serviceName)) {
                    $serviceManagerDeps[$serviceName] = @{}
                }
                
                if (-not $serviceManagerDeps[$serviceName].ContainsKey($managerType)) {
                    $serviceManagerDeps[$serviceName][$managerType] = @()
                }
                
                $serviceManagerDeps[$serviceName][$managerType] += $className
            }
        }
    } catch {
        if ($Verbose) {
            Write-Host "[WARN] 解析文件失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
}

# 2. 收集所有配置类中注册的Manager Bean
$registeredManagers = @{}

$configFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Configuration.java" | Where-Object {
    $_.FullName -notmatch "\\test\\" -and 
    ($_.FullName -match "ManagerConfiguration" -or $_.FullName -match "SystemManagersConfig" -or $_.FullName -match "AuthManagerConfig")
}

Write-Host "[INFO] 扫描 $($configFiles.Count) 个配置类..." -ForegroundColor Cyan

foreach ($file in $configFiles) {
    try {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        
        # 确定微服务名称
        $serviceName = "unknown"
        if ($file.FullName -match "ioedream-(\w+)-service") {
            $serviceName = $matches[1]
        } elseif ($file.FullName -match "common-service") {
            $serviceName = "common"
        }
        
        # 提取@Bean注册的Manager
        $matches = [regex]::Matches($content, "@Bean[^\n]*\n\s*public\s+(\w+Manager)\s+\w+Manager\s*\(")
        
        foreach ($match in $matches) {
            $managerType = $match.Groups[1].Value
            
            if (-not $registeredManagers.ContainsKey($serviceName)) {
                $registeredManagers[$serviceName] = @()
            }
            
            if ($managerType -notin $registeredManagers[$serviceName]) {
                $registeredManagers[$serviceName] += $managerType
            }
        }
    } catch {
        if ($Verbose) {
            Write-Host "[WARN] 解析配置文件失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
}

# 3. 对比分析
$missingBeans = @{}
$allManagers = @{}

# 汇总所有需要的Manager
foreach ($serviceName in $serviceManagerDeps.Keys) {
    foreach ($managerType in $serviceManagerDeps[$serviceName].Keys) {
        if (-not $allManagers.ContainsKey($managerType)) {
            $allManagers[$managerType] = @{
                Services = @{}
            }
        }
        $allManagers[$managerType].Services[$serviceName] = $serviceManagerDeps[$serviceName][$managerType]
    }
}

# 检查缺失的Bean
foreach ($managerType in $allManagers.Keys) {
    $found = $false
    
    # 检查common-service中是否注册
    if ($registeredManagers.ContainsKey("common") -and $managerType -in $registeredManagers["common"]) {
        $found = $true
    }
    
    # 检查各业务服务中是否注册
    foreach ($serviceName in $allManagers[$managerType].Services.Keys) {
        if ($registeredManagers.ContainsKey($serviceName) -and $managerType -in $registeredManagers[$serviceName]) {
            $found = $true
        }
    }
    
    if (-not $found) {
        $missingBeans[$managerType] = $allManagers[$managerType]
    }
}

# 4. 生成报告
$markdown = @"
# Manager Bean注册检查报告

**生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

---

## 执行摘要

- **检查的Service类**: $($serviceFiles.Count) 个
- **检查的配置类**: $($configFiles.Count) 个
- **需要的Manager类型**: $($allManagers.Count) 个
- **已注册的Manager**: $($allManagers.Count - $missingBeans.Count) 个
- **缺失的Manager**: $($missingBeans.Count) 个

---

## 注册状态统计

"@

$markdown += "| 微服务 | 已注册Manager数量 |\n|--------|------------------|\n"
foreach ($serviceName in ($registeredManagers.Keys | Sort-Object)) {
    $count = $registeredManagers[$serviceName].Count
    $markdown += "| $serviceName-service | $count |\n"
}

if ($missingBeans.Count -gt 0) {
    $markdown += @"


---

## ⚠️ 缺失的Manager Bean

以下Manager在Service中被使用，但未在配置类中注册：

"@
    
    foreach ($managerType in ($missingBeans.Keys | Sort-Object)) {
        $markdown += "### $managerType`n`n"
        $markdown += "**使用的Service**:`n`n"
        foreach ($serviceName in ($missingBeans[$managerType].Services.Keys | Sort-Object)) {
            $serviceList = ($missingBeans[$managerType].Services[$serviceName] | Sort-Object -Unique) -join ", "
            $markdown += "- **$serviceName-service**: $serviceList`n"
        }
        $markdown += "`n**建议**: 在对应的配置类中注册此Manager Bean`n`n"
    }
} else {
    $markdown += @"


---

## ✅ 所有Manager Bean已正确注册

所有Service需要的Manager都已正确注册，无缺失。

"@
}

$markdown += @"


---

## 详细的Manager依赖关系

"@

foreach ($serviceName in ($serviceManagerDeps.Keys | Sort-Object)) {
    $markdown += "### $serviceName-service`n`n"
    $markdown += "| Service类 | Manager依赖 |\n|----------|------------|\n"
    
    foreach ($managerType in ($serviceManagerDeps[$serviceName].Keys | Sort-Object)) {
        $services = ($serviceManagerDeps[$serviceName][$managerType] | Sort-Object -Unique) -join ", "
        $registered = "❌ 未注册"
        
        if ($registeredManagers.ContainsKey($serviceName) -and $managerType -in $registeredManagers[$serviceName]) {
            $registered = "✅ 已注册"
        } elseif ($registeredManagers.ContainsKey("common") -and $managerType -in $registeredManagers["common"]) {
            $registered = "✅ 已在common-service注册"
        }
        
        $markdown += "| $services | $managerType ($registered) |\n"
    }
    $markdown += "`n"
}

# 确保输出目录存在
$outputDir = Split-Path $outputFile -Parent
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

# 写入文件
$markdown | Out-File -FilePath $outputFile -Encoding UTF8 -NoNewline

Write-Host "[OK] 检查报告已生成: $outputFile" -ForegroundColor Green

if ($missingBeans.Count -gt 0) {
    Write-Host "[WARN] 发现 $($missingBeans.Count) 个缺失的Manager Bean" -ForegroundColor Yellow
    foreach ($managerType in $missingBeans.Keys) {
        Write-Host "  - $managerType" -ForegroundColor Yellow
    }
    exit 1
} else {
    Write-Host "[OK] 所有Manager Bean都已正确注册" -ForegroundColor Green
    exit 0
}

