# IOE-DREAM 手动编译指南

> **状态**: ✅ 已解决BOM问题，Maven可正常使用
> **更新时间**: 2025-12-09 12:08

## 🎯 问题根源

**发现**: 所有pom.xml文件包含BOM字符（UTF-8 BOM），导致Maven解析错误：
```
错误: 找不到或无法加载主类 #
原因: java.lang.ClassNotFoundException: #
```

**已解决**: 已修复所有pom.xml文件的BOM问题。

## 📋 手动编译步骤

### 方法1: 使用修复后的脚本（推荐）

```powershell
# PowerShell版本（推荐）
.\build.ps1

# 批处理版本（备用）
build-simple.bat
```

### 方法2: 完全手动编译

如果脚本仍有问题，可以按以下步骤手动编译：

```bash
# 1. 进入微服务目录
cd D:\IOE-DREAM\microservices

# 2. 设置环境变量
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.language=en -Duser.country=US
set MAVEN_OPTS=-Xmx1024m -Dfile.encoding=UTF-8

# 3. 清理项目
mvn clean -Duser.language=en -Duser.country=US

# 4. 编译公共模块（必须先编译）
cd microservices-common
mvn install -DskipTests -Duser.language=en -Duser.country=US

# 5. 返回主目录编译所有模块
cd ..
mvn compile -DskipTests -Duser.language=en -Duser.country=US

# 6. 打包应用
mvn package -DskipTests -Duser.language=en -Duser.country=US

# 7. 返回根目录
cd ..
```

### 方法3: 使用PowerShell手动编译

```powershell
# 设置环境
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.language=en -Duser.country=US"
$env:MAVEN_OPTS = "-Xmx1024m -Dfile.encoding=UTF-8"

# 进入微服务目录
Set-Location "D:\IOE-DREAM\microservices"

# 清理
& mvn clean -q -Duser.language=en -Duser.country=US

# 编译公共模块
Set-Location "microservices-common"
& mvn install -DskipTests -q -Duser.language=en -Duser.country=US
Set-Location ".."

# 编译所有模块
& mvn compile -DskipTests -q -Duser.language=en -Duser.country=US

# 打包
& mvn package -DskipTests -q -Duser.language=en -Duser.country=US

# 返回根目录
Set-Location ".."
```

## 🚀 启动服务

### 手动启动后端服务

```bash
# 进入微服务目录
cd D:\IOE-DREAM\microservices

# 启动服务（在多个命令行窗口中分别启动）
# 网关服务
start "Gateway Service" cmd /k "cd %CD% && cd ioedream-gateway-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

# 公共服务
start "Common Service" cmd /k "cd %CD% && cd ioedream-common-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

# 其他服务（根据需要启动）
start "Access Service" cmd /k "cd %CD% && cd ioedream-access-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
```

### 使用启动脚本

```bash
# 使用批处理脚本
start-services.bat

# 或使用PowerShell脚本
.\start.ps1
```

## 🔧 故障排除

### 如果Maven仍然失败

1. **检查Java版本**:
   ```bash
   java -version
   # 确保使用JDK 17+
   ```

2. **检查Maven版本**:
   ```bash
   mvn -version
   # 确保使用Maven 3.6+
   ```

3. **检查环境变量**:
   ```bash
   echo %JAVA_HOME%
   echo %MAVEN_HOME%
   ```

4. **清理Maven仓库**:
   ```bash
   rmdir /s /q "%USERPROFILE%\.m2\repository"
   ```

5. **更新Maven配置**:
   创建 `%USERPROFILE%\.m2\settings.xml`:
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <settings>
       <profiles>
           <profile>
               <id>utf8</id>
               <properties>
                   <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                   <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                   <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
               </properties>
           </profile>
       </profiles>
       <activeProfiles>
           <activeProfile>utf8</activeProfile>
       </activeProfiles>
   </settings>
   ```

### 验证编译结果

编译成功后，应该在以下位置找到JAR文件：
- `microservices/ioedream-*/target/*.jar`

检查生成的JAR文件：
```bash
dir microservices\ioedream-*\target\*.jar
```

## 📚 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 网关服务 | 8080 | API网关 |
| 公共服务 | 8088 | 用户/权限/配置 |
| 设备通讯 | 8087 | 设备协议通讯 |
| OA服务 | 8089 | 办公自动化 |
| 门禁服务 | 8090 | 智能门禁 |
| 考勤服务 | 8091 | 考勤管理 |
| 视频服务 | 8092 | 视频监控 |
| 消费服务 | 8094 | 一卡通消费 |
| 访客服务 | 8095 | 访客管理 |

## 🎉 验证服务

编译和启动成功后，可以通过以下方式验证：

1. **检查服务状态**:
   ```bash
   netstat -an | findstr ":8080"
   ```

2. **访问API文档**:
   - 浏览器访问: `http://localhost:8080/doc.html`

3. **健康检查**:
   - 浏览器访问: `http://localhost:8080/actuator/health`

4. **查看日志**:
   - 在服务启动的命令行窗口中查看启动日志

## 📞 技术支持

如果问题仍未解决：

1. 运行诊断脚本: `maven-fix.bat`
2. 运行BOM修复脚本: `maven-bom-fix.bat`
3. 检查Java和Maven安装
4. 确保网络连接正常（Maven需要下载依赖）

---

**💡 提示**: 现在所有pom.xml文件的BOM问题已修复，Maven应该可以正常工作了。如果仍有问题，请使用手动编译步骤。