@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: SA-ADMIN编译错误快速修复脚本 (Windows版本)
:: 作者: Claude Code Assistant
:: 创建时间: 2025-11-30

echo ==========================================
echo 🚀 SA-ADMIN编译错误快速修复脚本
echo ==========================================
echo.

:: 进入脚本所在目录
cd /d "%~dp0"
echo 📍 当前目录: %CD%
echo.

:: 1. 清理编译缓存
echo 🧹 步骤1: 清理编译缓存...
call mvn clean
if %errorlevel% equ 0 (
    echo ✅ 编译缓存清理成功
) else (
    echo ❌ 编译缓存清理失败
    pause
    exit /b 1
)
echo.

:: 2. 添加@Slf4j注解到缺失的文件
echo 📝 步骤2: 添加缺失的@Slf4j注解...

:: 需要添加@Slf4j的文件列表
set "files_with_missing_log[0]=src\main\java\net\lab1024\sa\admin\module\attendance\service\AttendanceLocationService.java"

set i=0
:process_log_files
if defined files_with_missing_log[%i%] (
    set "file=!files_with_missing_log[%i%]!"
    if exist "!file!" (
        findstr /C:"@Slf4j" "!file!" >nul
        if !errorlevel! neq 0 (
            findstr /C:"import lombok.extern.slf4j.Slf4j;" "!file!" >nul
            if !errorlevel! neq 0 (
                :: 在package后添加import
                powershell -Command "(Get-Content '!file!') -replace '(^package.*;$', '$1`r`nimport lombok.extern.slf4j.Slf4j;') | Set-Content '!file!'"
            )
            :: 在public class前添加@Slf4j
            powershell -Command "(Get-Content '!file!') -replace '(^public class)', '@Slf4j`r`n$1' | Set-Content '!file!'"
            echo ✅ 已为 !file! 添加 @Slf4j 注解
        ) else (
            echo ℹ️  !file! 已有 @Slf4j 注解，跳过
        )
    ) else (
        echo ⚠️  文件不存在: !file!
    )
    set /a i+=1
    goto process_log_files
)
echo.

:: 3. 添加@EqualsAndHashCode(callSuper=false)注解
echo 🏷️  步骤3: 添加@EqualsAndHashCode注解...

:: 需要添加@EqualsAndHashCode的文件列表
set "files_equals_hashcode[0]=src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\LeaveTypesEntity.java"
set "files_equals_hashcode[1]=src\main\java\net\lab1024\sa\admin\module\attendance\domain\query\ShiftsQuery.java"
set "files_equals_hashcode[2]=src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\AttendanceRulesEntity.java"
set "files_equals_hashcode[3]=src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\ExceptionApplicationsEntity.java"
set "files_equals_hashcode[4]=src\main\java\net\lab1024\sa\admin\module\smart\video\domain\entity\VideoRecordingEntity.java"
set "files_equals_hashcode[5]=src\main\java\net\lab1024\sa\admin\module\attendance\domain\query\TimePeriodsQuery.java"
set "files_equals_hashcode[6]=src\main\java\net\lab1024\sa\admin\module\hr\domain\form\EmployeeQueryForm.java"
set "files_equals_hashcode[7]=src\main\java\net\lab1024\sa\admin\module\smart\video\domain\form\VideoDeviceQueryForm.java"
set "files_equals_hashcode[8]=src\main\java\net\lab1024\sa\admin\module\consume\domain\entity\RefundRecordEntity.java"
set "files_equals_hashcode[9]=src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\ExceptionApprovalsEntity.java"
set "files_equals_hashcode[10]=src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\ClockRecordsEntity.java"
set "files_equals_hashcode[11]=src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\TimePeriodsEntity.java"
set "files_equals_hashcode[12]=src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\ShiftsEntity.java"
set "files_equals_hashcode[13]=src\main\java\net\lab1024\sa\admin\module\oa\document\domain\form\DocumentQueryForm.java"

set i=0
:process_equals_files
if defined files_equals_hashcode[%i%] (
    set "file=!files_equals_hashcode[%i%]!"
    if exist "!file!" (
        findstr /C:"@EqualsAndHashCode" "!file!" >nul
        if !errorlevel! neq 0 (
            :: 在类声明前添加注解
            powershell -Command "(Get-Content '!file!') -replace '(^public class)', '@EqualsAndHashCode(callSuper = false)`r`n$1' | Set-Content '!file!'"
            echo ✅ 已为 !file! 添加 @EqualsAndHashCode 注解
        ) else (
            echo ℹ️  !file! 已有 @EqualsAndHashCode 注解，跳过
        )
    ) else (
        echo ⚠️  文件不存在: !file!
    )
    set /a i+=1
    if !i! lss 14 goto process_equals_files
)
echo.

:: 4. 尝试编译并检查错误数量变化
echo 🔍 步骤4: 重新编译并检查结果...

:: 重新编译
call mvn compile -q > compile_result.log 2>&1

:: 统计错误数量
findstr /C:"ERROR" compile_result.log > nul
if !errorlevel! equ 0 (
    for /f %%i in ('find /c "ERROR" compile_result.log') do set error_count=%%i
) else (
    set error_count=0
)

findstr /C:"WARNING" compile_result.log > nul
if !errorlevel! equ 0 (
    for /f %%i in ('find /c "WARNING" compile_result.log') do set warning_count=%%i
) else (
    set warning_count=0
)

echo.
echo ==========================================
echo 📊 编译结果统计
echo ==========================================
echo ❌ 错误数量: %error_count%
echo ⚠️  警告数量: %warning_count%

if %error_count% equ 0 (
    echo.
    echo 🎉 恭喜！编译成功，没有错误！
    pause
    exit /b 0
) else if %error_count% lss 100 (
    echo.
    echo 📈 进展：错误数量已减少！
    echo 💡 接下来需要手动修复剩余的错误
) else (
    echo.
    echo ⚠️  错误数量仍然较多，需要进一步排查
)

echo.
echo 📄 详细编译日志已保存到: compile_result.log
echo.
echo 🔧 下一步建议：
echo 1. 查看 compile_result.log 了解具体错误
echo 2. 按照优先级修复剩余错误
echo 3. 参考编译错误分析报告进行详细修复
echo.
echo ==========================================

pause