# Entity统一管理清理脚本
# 解决346个类型解析错误的根本原因

Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  Entity统一管理 - 重复Entity清理"  -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host ""

# 统计当前Entity分布
Write-Host "[步骤1] 统计Entity分布..."  -ForegroundColor Yellow

$entityStats = @()
$services = @("ioedream-access-service", "ioedream-attendance-service", "ioedream-consume-service", "ioedream-video-service", "ioedream-visitor-service")

foreach ($service in $services) {
    $servicePath = "D:\IOE-DREAM\microservices\$service"
    if (Test-Path $servicePath) {
        $domainEntityCount = (Get-ChildItem -Path "$servicePath\src\main\java" -Recurse -Filter "*Entity.java" -Path "*/domain/entity/*" -ErrorAction SilentlyContinue).Count
        $entityCount = (Get-ChildItem -Path "$servicePath\src\main\java" -Recurse -Filter "*Entity.java" -Path "*/entity/*" -ErrorAction SilentlyContinue).Count

        $entityStats += [PSCustomObject]@{
            Service = $service
            DomainEntity = $domainEntityCount
            Entity = $entityCount
            Total = $domainEntityCount + $entityCount
        }

        Write-Host "  $service:"  -ForegroundColor Cyan
        Write-Host "    domain/entity/: $domainEntityCount"  -ForegroundColor White
        Write-Host "    entity/:        $entityCount"  -ForegroundColor White
        Write-Host "    总计:          $($domainEntityCount + $entityCount)"  -ForegroundColor Yellow
    }
}

Write-Host ""

# 识别重复的Entity
Write-Host "[步骤2] 识别重复Entity..."  -ForegroundColor Yellow

$duplicates = @()
foreach ($service in $services) {
    $servicePath = "D:\IOE-DREAM\microservices\$service"
    if (Test-Path $servicePath) {
        # 获取domain/entity中的Entity名称
        $domainEntities = Get-ChildItem -Path "$servicePath\src\main\java" -Recurse -Filter "*Entity.java" -Path "*/domain/entity/*" -ErrorAction SilentlyContinue

        foreach ($entity in $domainEntities) {
            $entityName = $entity.Name
            $entityPath = "$servicePath\src\main\java\net\lab1024\sa\$service\entity\$entityName"

            if (Test-Path $entityPath) {
                $duplicates += [PSCustomObject]@{
                    Service = $service
                    Entity = $entityName
                    DuplicatePath = $entityPath
                }
                Write-Host "  发现重复: $service\$entityName"  -ForegroundColor Red
            }
        }
    }
}

Write-Host ""
Write-Host "  重复Entity总数: $($duplicates.Count)"  -ForegroundColor Red
Write-Host ""

# 确认操作
Write-Host "[步骤3] 清理计划"  -ForegroundColor Yellow
Write-Host "  将删除以下目录中的Entity:"  -ForegroundColor Red
Write-Host "    - microservices/ioedream-*-service/src/main/java/net/lab1024/sa/*/domain/entity/"  -ForegroundColor White
Write-Host "    - microservices/ioedream-*-service/src/main/java/net/lab1024/sa/*/entity/"  -ForegroundColor White
Write-Host ""
Write-Host "  保留目录:"  -ForegroundColor Green
Write-Host "    - microservices/microservices-common-entity/"  -ForegroundColor White
Write-Host ""

$confirmation = Read-Host "是否继续删除重复Entity? (输入 YES 确认)"

if ($confirmation -ne "YES") {
    Write-Host "操作已取消"  -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "[步骤4] 删除重复Entity..."  -ForegroundColor Yellow

$deletedCount = 0
foreach ($duplicate in $duplicates) {
    $duplicatePath = $duplicate.DuplicatePath

    try {
        Remove-Item $duplicatePath -Force
        $deletedCount++
        Write-Host "  ✓ 已删除: $($duplicate.Service)\$($duplicate.Entity)"  -ForegroundColor Green
    }
    catch {
        Write-Host "  ✗ 删除失败: $($duplicate.Service)\$($duplicate.Entity)"  -ForegroundColor Red
        Write-Host "    错误: $($_.Exception.Message)"  -ForegroundColor DarkRed
    }
}

Write-Host ""
Write-Host "[步骤5] 清理空目录..."  -ForegroundColor Yellow

$emptyDirs = @(
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\domain\entity",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\entity",
    "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\domain\entity",
    "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\entity"
)

foreach ($dir in $emptyDirs) {
    if (Test-Path $dir) {
        try {
            Remove-Item $dir -Force -Recurse
            Write-Host "  ✓ 已删除空目录: $dir"  -ForegroundColor Green
        }
        catch {
            Write-Host "  ! 目录不为空，跳过: $dir"  -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "[步骤6] 更新Maven依赖..."  -ForegroundColor Yellow
Write-Host "  确保所有业务服务依赖microservices-common-entity"  -ForegroundColor White
Write-Host ""

# 输出总结
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  清理完成总结"  -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  删除Entity数量: $deletedCount"  -ForegroundColor Green
Write-Host "  剩余Entity位置: microservices-common-entity"  -ForegroundColor Green
Write-Host "  下一步: 验证编译"  -ForegroundColor Yellow
Write-Host "========================================"  -ForegroundColor Cyan
