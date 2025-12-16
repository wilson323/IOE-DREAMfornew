# 批量替换@PreAuthorize为PermissionCheck的脚本

$files = @(
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\edge\controller\EdgeSecurityController.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\EnhancedAccessSecurityController.java",
    "D:\IOE-DREAM\microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\edge\controller\EdgeVideoController.java",
    "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\controller\ScheduleController.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AntiPassbackController.java",
    "D:\IOE-DREAM\microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\controller\VideoPTZController.java",
    "D:\IOE-DREAM\microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\controller\VideoFaceController.java",
    "D:\IOE-DREAM\microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\controller\VideoRecordingController.java",
    "D:\IOE-DREAM\microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\controller\VideoStreamController.java",
    "D:\IOE-DREAM\microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\controller\VideoDeviceController.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\DeviceHealthController.java",
    "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\controller\AttendanceRecordController.java",
    "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\controller\AttendanceMobileController.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessRecordController.java",
    "D:\IOE-DREAM\microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\controller\ReportController.java",
    "D:\IOE-DREAM\microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\controller\ReconciliationController.java",
    "D:\IOE-DREAM\microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\controller\PaymentController.java",
    "D:\IOE-DREAM\microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\controller\ConsumeController.java",
    "D:\IOE-DREAM\microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\controller\AccountController.java",
    "D:\IOE-DREAM\microservices\ioedream-visitor-service\src\main\java\net\lab1024\sa\visitor\controller\VisitorController.java",
    "D:\IOE-DREAM\microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\controller\VideoPlayController.java",
    "D:\IOE-DREAM\microservices\ioedream-video-service\src\main\java\net\lab1024\sa\video\controller\VideoMobileController.java"
)

$processed = 0
$total = $files.Count

Write-Host "开始批量处理 $total 个文件..." -ForegroundColor Green

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "处理文件: $($file.Split('\')[-1])" -ForegroundColor Yellow

        try {
            # 读取文件内容
            $content = Get-Content $file -Raw -Encoding UTF8

            # 记录是否有修改
            $modified = $false

            # 替换导入语句
            if ($content -match "import org\.springframework\.security\.access\.prepost\.PreAuthorize;") {
                $content = $content -replace "import org\.springframework\.security\.access\.prepost\.PreAuthorize;", "import net.lab1024.sa.common.permission.annotation.PermissionCheck;"
                $modified = $true
            }

            # 添加类级别权限注解
            if ($content -match "@RestController" -and $content -notMatch "@PermissionCheck\(value = ") {
                # 找到@RestController注解的位置，在其后添加PermissionCheck
                $lines = $content -split "`n"
                $newLines = @()
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    $newLines += $lines[$i]
                    if ($lines[$i] -match "@RestController") {
                        # 确定模块类型
                        $moduleType = ""
                        $moduleDesc = ""
                        if ($file -match "access") { $moduleType = "ACCESS"; $moduleDesc = "门禁管理" }
                        elseif ($file -match "attendance") { $moduleType = "ATTENDANCE"; $moduleDesc = "考勤管理" }
                        elseif ($file -match "consume") { $moduleType = "CONSUME"; $moduleDesc = "消费管理" }
                        elseif ($file -match "visitor") { $moduleType = "VISITOR"; $moduleDesc = "访客管理" }
                        elseif ($file -match "video") { $moduleType = "VIDEO"; $moduleDesc = "视频管理" }
                        $newLines += "@PermissionCheck(value = `"$moduleType`", description = `"$moduleDesc`")"
                        $modified = $true
                    }
                }
                $content = $newLines -join "`n"
            }

            # 批量替换@PreAuthorize注解
            if ($content -match "@PreAuthorize") {
                # 使用正则表达式替换各种@PreAuthorize模式
                $content = $content -replace '@PreAuthorize\("hasRole\(''([^']+)''\)"\)', '@PermissionCheck(value = "$1", description = "权限操作")'
                $content = $content -replace '@PreAuthorize\("hasRole\(''([^']+)''\) or hasRole\(''([^']+)''\)"\)', '@PermissionCheck(value = {"$1", "$2"}, description = "权限操作")'
                $content = $content -replace '@PreAuthorize\("hasRole\(''([^']+)''\) or hasRole\(''([^']+)''\) or hasRole\(''([^']+)''\)"\)', '@PermissionCheck(value = {"$1", "$2", "$3"}, description = "权限操作")'
                $modified = $true
            }

            # 如果有修改，保存文件
            if ($modified) {
                Set-Content $file $content -Encoding UTF8 -NoNewline
                Write-Host "  ✓ 已修改" -ForegroundColor Green
            } else {
                Write-Host "  - 无需修改" -ForegroundColor Gray
            }

            $processed++
        } catch {
            Write-Host "  ✗ 处理失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "文件不存在: $file" -ForegroundColor Red
    }
}

Write-Host "`n处理完成! 共处理了 $processed/$total 个文件" -ForegroundColor Green