@echo off
echo 开始快速修复编译错误...

cd /d "D:\IOE-DREAM\smart-admin-api-java17-springboot3"

echo 1. 添加JWT依赖到父pom.xml...
powershell -Command "(Get-Content pom.xml) -replace '    <!-- Dependencies -->', '    <!-- Dependencies -->\n        <!-- JWT Token -->\n        <dependency>\n            <groupId>io.jsonwebtoken</groupId>\n            <artifactId>jjwt-api</artifactId>\n            <version>0.11.5</version>\n        </dependency>\n        <dependency>\n            <groupId>io.jsonwebtoken</groupId>\n            <artifactId>jjwt-impl</artifactId>\n            <version>0.11.5</version>\n            <scope>runtime</scope>\n        </dependency>\n        <dependency>\n            <groupId>io.jsonwebtoken</groupId>\n            <artifactId>jjwt-jackson</artifactId>\n            <version>0.11.5</version>\n            <scope>runtime</scope>\n        </dependency>' | Set-Content pom.xml"

echo 2. 创建CacheKeyConst类...
echo package net.lab1024.sa.base.common.constant; > "sa-base\src\main\java\net\lab1024\sa\base\common\constant\CacheKeyConst.java"
echo. >> "sa-base\src\main\java\net\lab1024\sa\base\common\constant\CacheKeyConst.java"
echo public class CacheKeyConst { >> "sa-base\src\main\java\net\lab1024\sa\base\common\constant\CacheKeyConst.java"
echo     public static final String USER_CACHE = "user:"; >> "sa-base\src\main\java\net\lab1024\sa\base\common\constant\CacheKeyConst.java"
echo     public static final String MENU_CACHE = "menu:"; >> "sa-base\src\main\java\net\lab1024\sa\base\common\constant\CacheKeyConst.java"
echo     public static final String DEPT_CACHE = "dept:"; >> "sa-base\src\main\java\net\lab1024\sa\base\common\constant\CacheKeyConst.java"
echo } >> "sa-base\src\main\java\net\lab1024\sa\base\common\constant\CacheKeyConst.java"

echo 修复完成！
pause