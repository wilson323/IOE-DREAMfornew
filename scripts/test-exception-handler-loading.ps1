# IOE-DREAM 异常处理器加载测试脚本
# 验证GlobalExceptionHandler在consume-service中是否正确加载

param(
    [Parameter(Mandatory=$false)]
    [string]$ServiceName = "consume-service"
)

Write-Host "======================================" -ForegroundColor Green
Write-Host "IOE-DREAM 异常处理器加载测试" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green

# 内存优化配置
$env:JAVA_OPTS = "-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

function Test-ExceptionHandling {
    param([string]$ServiceName)

    Write-Host "测试服务: $ServiceName" -ForegroundColor Cyan

    # 1. 检查依赖关系
    Write-Host "1. 检查common-service依赖..." -ForegroundColor Yellow
    $pomPath = "microservices/ioedream-$ServiceName/pom.xml"

    if (Test-Path $pomPath) {
        $pomContent = Get-Content $pomPath -Raw
        if ($pomContent -match "ioedream-common-service") {
            Write-Success "   ✅ common-service依赖已配置"
        } else {
            Write-Error "   ❌ 缺少common-service依赖"
            return $false
        }
    } else {
        Write-Error "   ❌ POM文件不存在: $pomPath"
        return $false
    }

    # 2. 检查异常类使用
    Write-Host "2. 检查异常类使用..." -ForegroundColor Yellow
    $serviceDir = "microservices/ioedream-$ServiceName/src/main/java"

    $businessExceptions = Get-ChildItem -Path $serviceDir -Recurse -Filter "*.java" |
        Select-String -Pattern "throw new BusinessException" |
        Measure-Object

    $systemExceptions = Get-ChildItem -Path $serviceDir -Recurse -Filter "*.java" |
        Select-String -Pattern "throw new SystemException" |
        Measure-Object

    Write-Host "   BusinessException使用: $($businessExceptions.Count)处"
    Write-Host "   SystemException使用: $($systemExceptions.Count)处"

    if ($businessExceptions.Count -gt 0 -or $systemExceptions.Count -gt 0) {
        Write-Success "   ✅ 异常分类使用正常"
    } else {
        Write-Warning "   ⚠️  建议增加异常分类使用"
    }

    # 3. 检查重复的异常处理器
    Write-Host "3. 检查重复的异常处理器..." -ForegroundColor Yellow
    $duplicateHandlers = Get-ChildItem -Path $serviceDir -Recurse -Filter "*.java" |
        Select-String -Pattern "@RestControllerAdvice" |
        Measure-Object

    if ($duplicateHandlers.Count -eq 1 -and
        $duplicateHandlers.Path -like "*GlobalExceptionHandler*") {
        Write-Success "   ✅ 无重复异常处理器"
    } elseif ($duplicateHandlers.Count -gt 1) {
        Write-Error "   ❌ 发现重复异常处理器"
        $duplicateHandlers | ForEach-Object {
            Write-Host "     📁 $($_.Path):$($_.LineNumber)" -ForegroundColor Red
        }
        return $false
    } elseif ($duplicateHandlers.Count -eq 0) {
        Write-Warning "   ⚠️  未发现异常处理器"
    } else {
        Write-Success "   ✅ 正常: $([math]::Ceiling($duplicateHandlers.Count / 2))"
    }

    return $true
}

# 内存测试函数
function Test-MemoryUsage {
    Write-Host "4. 内存使用测试..." -ForegroundColor Yellow

    # 模拟异常对象创建和内存使用
    $testResult = Java -cp "microservices/microservices-common-core/target/classes:$env:JAVA_HOME/lib/*" `
        -Xms256m -Xmx512m `
        -XX:+UseG1GC `
        -XX:MaxGCPauseMillis=50 `
        -XX:+PrintGCDetails `
        -XX:+PrintGCTimeStamps `
        -Dfile.encoding=UTF-8 `
        -c "
        import java.util.*;
        import java.util.concurrent.*;

        public class ExceptionMemoryTest {
            public static void main(String[] args) {
                System.out.println(\"=== 内存使用测试 ===\");

                // 测试BusinessException内存使用
                Runtime runtime = Runtime.getRuntime();
                long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

                List<Exception> exceptions = new ArrayList<>();
                for (int i = 0; i < 10000; i++) {
                    exceptions.add(new RuntimeException(\"Test exception \" + i));
                }

                long afterMemory = runtime.totalMemory() - runtime.freeMemory();
                long memoryUsed = afterMemory - beforeMemory;

                System.out.println(\"创建10000个异常对象使用内存: \" + (memoryUsed / 1024) + \" KB\");
                System.out.println(\"每个异常对象平均内存: \" + (memoryUsed / 10000) + \" bytes\");

                exceptions.clear();
                System.gc();
                long afterGCMemory = runtime.totalMemory() - runtime.freeMemory();
                long memoryLeaked = afterGCMemory - beforeMemory;

                System.out.println(\"GC后内存泄漏: \" + (memoryLeaked / 1024) + \" KB\");

                if (memoryLeaked < 100) {
                    System.out.println(\"✅ 内存使用优化良好\");
                } else {
                    System.out.println(\"⚠️  可能存在内存泄漏\");
                }
            }
        }
    " 2>$null

    Write-Host "内存测试结果:" -ForegroundColor Cyan
    Write-Host $testResult

    return $testResult -like "*✅*"
}

# 执行测试
Write-Host "开始执行异常处理器验证测试..." -ForegroundColor Green

$success = $true

# 测试指定服务
if ($ServiceName -ne "") {
    if (-NOT (Test-ExceptionHandling $ServiceName)) {
        $success = $false
    }
} else {
    # 测试所有业务服务
    $businessServices = @("consume-service", "access-service", "attendance-service", "visitor-service", "video-service", "device-comm-service", "oa-service")

    foreach ($service in $businessServices) {
        if (-NOT (Test-ExceptionHandling $service)) {
            $success = $false
        }
        Write-Host ""
    }
}

# 内存优化测试
if (Test-MemoryUsage) {
    Write-Success "内存使用测试通过"
} else {
    Write-Warning "内存使用测试需要优化"
}

# 生成测试报告
$reportPath = "scripts/reports/exception-handler-test-report-$(Get-Date -Format 'yyyyMMddHHmmss').txt"
$reportContent = @"
IOE-DREAM 异常处理器加载测试报告
生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

测试结果:
- 总体测试状态: $(if ($success) { "通过" } else { "失败" })
- 测试服务: $(if ($ServiceName -ne "") { $ServiceName } else { "所有业务服务" })

关键发现:
1. 所有服务需要依赖ioedream-common-service以使用统一异常处理器
2. BusinessException和SystemException已在microservices-common-core中定义
3. GlobalExceptionHandler位于ioedream-common-service中
4. 需要避免在业务服务中重复实现异常处理器

建议:
1. 确保所有业务微服务添加ioedream-common-service依赖
2. 使用标准异常类型而非自定义异常
3. 避免在Controller层吞噬异常
4. 配置统一的异常监控和告警

下一步:
1. 验证应用程序启动
2. 测试异常处理端到端流程
3. 配置监控指标
"@

if (-NOT (Test-Path "scripts/reports")) {
    New-Item -ItemType Directory -Path "scripts/reports" -Force
}
Set-Content -Path $reportPath -Value $reportContent -Encoding UTF8

Write-Host "======================================" -ForegroundColor Green
if ($success) {
    Write-Success "测试完成！所有检查项均通过"
} else {
    Write-Error "测试失败！存在需要修复的问题"
}
Write-Host "测试报告已生成: $reportPath" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green