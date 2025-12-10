# Maven环境紧急修复方案

## 🚨 问题确认
- `java.lang.ClassNotFoundException: #` 错误
- 所有Maven命令都失败
- 需要管理员权限才能修复

## 🎯 立即解决方案

### 步骤1：以管理员身份运行PowerShell
1. 右键点击"开始"菜单
2. 选择"PowerShell (管理员)"
3. 执行以下命令：

```powershell
# 停止所有Java进程
Stop-Process -Name java -Force -ErrorAction SilentlyContinue

# 强制删除Maven安装目录
Remove-Item -Path "C:\ProgramData\chocolatey\lib\maven" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "C:\ProgramData\chocolatey\lib-bkp\maven" -Recurse -Force -ErrorAction SilentlyContinue

# 清理Maven用户配置
Remove-Item -Path "$env:USERPROFILE\.m2" -Recurse -Force -ErrorAction SilentlyContinue
```

### 步骤2：重新安装Maven
```powershell
# 清理Chocolatey缓存
choco clean all

# 重新安装Maven
choco install maven -y --force

# 验证安装
mvn --version
```

### 步骤3：项目测试
```powershell
# 切换到项目根目录
cd D:\IOE-DREAM

# 清理并构建
mvn clean compile -DskipTests -Dfile.encoding=UTF-8

# 如果上述命令仍然失败，使用最小配置构建
mvn clean compile -DskipTests -Dfile.encoding=UTF-8 -Dmaven.test.skip=true
```

## 🚀 备选方案：直接下载Maven

如果上述方法仍然失败，直接下载并手动安装：

1. 访问 https://maven.apache.org/download.cgi
2. 下载 `apache-maven-3.9.11-bin.zip`
3. 解压到 `C:\maven`
4. 设置环境变量：
   - `MAVEN_HOME=C:\maven\apache-maven-3.9.11`
   - 在PATH中添加 `%MAVEN_HOME%\bin`

## 📋 验证命令
```bash
# 验证Maven版本
mvn --version

# 验证Java版本
java -version

# 验证项目构建
mvn help:effective-pom
```

## ⚡ 快速测试命令
在项目根目录下执行：
```bash
mvn archetype:generate -DgroupId=test -DartifactId=test -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
cd test
mvn clean compile
```

如果以上所有步骤都执行成功，说明Maven环境已修复！