# ========================================
# Lombok和MyBatis编译错误修复脚本
# ========================================
# 功能：修复Lombok注解处理器和MyBatis注解导入问题
# 作者：IOE-DREAM架构团队
# 日期：2025-12-02
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复Lombok和MyBatis编译问题" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "D:\IOE-DREAM"
Set-Location $projectRoot

# 步骤1：清理Maven本地缓存中的失败记录
Write-Host "[步骤1/6] 清理Maven失败缓存..." -ForegroundColor Yellow
$m2Repo = "$env:USERPROFILE\.m2\repository"
if (Test-Path "$m2Repo\org\jasypt\jasypt-spring-boot-starter\3.0.5") {
    Remove-Item "$m2Repo\org\jasypt\jasypt-spring-boot-starter\3.0.5" -Recurse -Force
    Write-Host "  ✓ 已清理jasypt缓存" -ForegroundColor Green
}

# 步骤2：检查并修复UserDao的MyBatis注解导入
Write-Host "`n[步骤2/6] 修复MyBatis注解导入..." -ForegroundColor Yellow
$userDaoPath = "microservices\microservices-common\src\main\java\net\lab1024\sa\common\security\dao\UserDao.java"

if (Test-Path $userDaoPath) {
    $content = Get-Content $userDaoPath -Raw

    # 检查是否已经有MyBatis注解导入
    if ($content -notmatch "import org\.apache\.ibatis\.annotations\.Select;") {
        Write-Host "  添加MyBatis注解导入..." -ForegroundColor Gray

        # 在package声明后添加导入
        $content = $content -replace "(package net\.lab1024\.sa\.common\.security\.dao;)", @"
`$1

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
"@

        Set-Content -Path $userDaoPath -Value $content -Encoding UTF8
        Write-Host "  ✓ 已添加MyBatis注解导入" -ForegroundColor Green
    } else {
        Write-Host "  ✓ MyBatis注解导入已存在" -ForegroundColor Green
    }
}

# 步骤3：检查并修复AreaDao重复问题
Write-Host "`n[步骤3/6] 检查AreaDao重复问题..." -ForegroundColor Yellow
$areaDaoPattern = "microservices\microservices-common\src\main\java\net\lab1024\sa\common\organization\dao\AreaDao.java"
$areaDaoFiles = Get-ChildItem -Path "microservices\microservices-common\src\main\java" -Recurse -Filter "AreaDao.java"

if ($areaDaoFiles.Count -gt 1) {
    Write-Host "  发现${$areaDaoFiles.Count}个AreaDao文件，保留正确的版本..." -ForegroundColor Gray

    # 保留organization包下的，删除其他的
    foreach ($file in $areaDaoFiles) {
        if ($file.FullName -notmatch "\\organization\\dao\\") {
            Write-Host "  删除重复文件：$($file.FullName)" -ForegroundColor Gray
            Remove-Item $file.FullName -Force
        }
    }
    Write-Host "  ✓ 已清理重复的AreaDao" -ForegroundColor Green
} else {
    Write-Host "  ✓ 未发现AreaDao重复" -ForegroundColor Green
}

# 步骤4：修复SmartPageUtil的日志问题
Write-Host "`n[步骤4/6] 修复SmartPageUtil日志问题..." -ForegroundColor Yellow
$pageUtilPath = "microservices\microservices-common\src\main\java\net\lab1024\sa\common\util\SmartPageUtil.java"

if (Test-Path $pageUtilPath) {
    $content = Get-Content $pageUtilPath -Raw

    # 检查是否缺少@Slf4j注解或log导入
    if ($content -notmatch "@Slf4j" -and $content -match "\blog\.") {
        Write-Host "  添加@Slf4j注解..." -ForegroundColor Gray

        # 添加lombok.extern.slf4j.Slf4j导入
        if ($content -notmatch "import lombok\.extern\.slf4j\.Slf4j;") {
            $content = $content -replace "(package net\.lab1024\.sa\.common\.util;)", @"
`$1

import lombok.extern.slf4j.Slf4j;
"@
        }

        # 在类声明前添加@Slf4j
        $content = $content -replace "(public class SmartPageUtil)", "@Slf4j`n`$1"

        Set-Content -Path $pageUtilPath -Value $content -Encoding UTF8
        Write-Host "  ✓ 已添加@Slf4j注解" -ForegroundColor Green
    } else {
        Write-Host "  ✓ @Slf4j注解已存在" -ForegroundColor Green
    }
}

# 步骤5：清理编译输出并强制重新编译
Write-Host "`n[步骤5/6] 清理编译输出..." -ForegroundColor Yellow
if (Test-Path "microservices\microservices-common\target") {
    Remove-Item "microservices\microservices-common\target" -Recurse -Force
    Write-Host "  ✓ 已清理target目录" -ForegroundColor Green
}

# 步骤6：重新编译microservices-common模块
Write-Host "`n[步骤6/6] 重新编译microservices-common..." -ForegroundColor Yellow
Write-Host "  执行: mvn clean install -pl microservices/microservices-common -am -DskipTests" -ForegroundColor Gray
Write-Host ""

$buildResult = & mvn clean install -pl microservices/microservices-common -am -DskipTests 2>&1

$buildSuccess = $buildResult -match "BUILD SUCCESS"

if ($buildSuccess) {
    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "  ✓ 修复完成！编译成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "后续操作建议：" -ForegroundColor Cyan
    Write-Host "1. 执行完整项目编译：mvn clean install -DskipTests" -ForegroundColor White
    Write-Host "2. 如果还有错误，请检查IDE的Lombok插件是否已安装" -ForegroundColor White
    Write-Host "3. 可能需要在IDE中执行 'Reimport All Maven Projects'" -ForegroundColor White
} else {
    Write-Host "`n========================================" -ForegroundColor Red
    Write-Host "  ✗ 编译仍然失败" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查以下内容：" -ForegroundColor Yellow
    Write-Host "1. Lombok注解处理器是否在maven-compiler-plugin中配置" -ForegroundColor White
    Write-Host "2. MyBatis-Plus依赖版本是否正确" -ForegroundColor White
    Write-Host "3. Java版本是否为17或更高" -ForegroundColor White
    Write-Host ""
    Write-Host "详细错误信息：" -ForegroundColor Yellow
    $buildResult | Select-String -Pattern "\[ERROR\]" | ForEach-Object {
        Write-Host $_.Line -ForegroundColor Red
    }
}

