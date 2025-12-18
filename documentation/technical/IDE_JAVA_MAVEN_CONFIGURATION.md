# IDE Java & Maven 配置指南

## 📋 项目技术栈版本要求

根据项目 `pom.xml` 配置，需要以下版本：

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| **Java** | **17** (LTS) | 必须使用 Java 17，推荐 OpenJDK 17 或 Oracle JDK 17 |
| **Maven** | **3.9+** | 支持 Maven 3.9.x 或更高版本 |
| **编码** | **UTF-8** | 所有文件编码必须为 UTF-8 |
| **Spring Boot** | 3.5.8 | 项目框架版本 |
| **Spring Cloud** | 2025.0.0 | 微服务框架版本 |

## 🔍 当前系统环境检查

### 已检测到的环境

```powershell
# Java版本
Java: OpenJDK 17.0.17 (Microsoft)
JAVA_HOME: C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot

# Maven版本
Maven: Apache Maven 3.9.11
Maven Home: C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.11
```

✅ **当前环境符合项目要求**

## 🛠️ Cursor IDE 配置步骤

### 1. 安装必要的扩展

在 Cursor 中安装以下扩展（如果未安装）：

1. **Extension Pack for Java** (Microsoft)
   - 包含：Language Support for Java、Debugger for Java、Test Runner for Java 等
   - 扩展ID: `vscjava.vscode-java-pack`

2. **Maven for Java** (Microsoft)
   - Maven 项目管理支持
   - 扩展ID: `vscjava.vscode-maven`

3. **Spring Boot Extension Pack** (VMware)
   - Spring Boot 开发支持
   - 扩展ID: `vmware.vscode-spring-boot`

### 2. 配置 Java 运行时

#### 方法1: 通过设置文件配置（推荐）

创建或编辑 `.vscode/settings.json` 文件：

```json
{
  // Java 配置
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "C:\\Program Files\\Microsoft\\jdk-17.0.17.10-hotspot",
      "default": true
    }
  ],
  
  // Java 编译器配置
  "java.compile.nullAnalysis.mode": "automatic",
  "java.configuration.updateBuildConfiguration": "automatic",
  
  // Maven 配置
  "java.configuration.maven.userSettings": null,
  "maven.executable.path": "C:\\ProgramData\\chocolatey\\lib\\maven\\apache-maven-3.9.11\\bin\\mvn.cmd",
  "maven.terminal.useJavaHome": true,
  
  // 编码配置
  "files.encoding": "utf8",
  "files.eol": "\n",
  
  // Java 项目配置
  "java.project.sourcePaths": ["src/main/java"],
  "java.project.outputPath": "target/classes",
  "java.project.referencedLibraries": [
    "target/**/*.jar",
    "lib/**/*.jar"
  ],
  
  // 代码格式化
  "java.format.settings.url": null,
  "java.format.settings.profile": null,
  
  // 代码检查
  "java.errors.incompleteClasspath.severity": "warning",
  
  // Spring Boot 配置
  "spring-boot.ls.java.home": "C:\\Program Files\\Microsoft\\jdk-17.0.17.10-hotspot"
}
```

#### 方法2: 通过命令面板配置

1. 按 `Ctrl+Shift+P` 打开命令面板
2. 输入 `Java: Configure Java Runtime`
3. 选择 `Add Runtime...`
4. 选择 `JDK 17` 的安装路径

### 3. 配置 Maven 设置

#### 3.1 检查 Maven 设置文件

Maven 设置文件位置：
- 用户级别: `%USERPROFILE%\.m2\settings.xml`
- 全局级别: `%MAVEN_HOME%\conf\settings.xml`

#### 3.2 推荐 Maven 配置

创建或编辑 `%USERPROFILE%\.m2\settings.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0
          http://maven.apache.org/xsd/settings-1.2.0.xsd">
  
  <!-- 本地仓库路径 -->
  <localRepository>${user.home}/.m2/repository</localRepository>
  
  <!-- 镜像配置（可选，加速下载） -->
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>central</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
  
  <!-- 配置文件激活 -->
  <profiles>
    <profile>
      <id>jdk-17</id>
      <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>17</jdk>
      </activation>
      <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.compilerVersion>17</maven.compiler.compilerVersion>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
      </properties>
    </profile>
  </profiles>
  
</settings>
```

### 4. 验证配置

#### 4.1 验证 Java 配置

在 Cursor 终端中执行：

```powershell
# 检查 Java 版本
java -version

# 检查 JAVA_HOME
echo $env:JAVA_HOME

# 应该输出: C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot
```

#### 4.2 验证 Maven 配置

在 Cursor 终端中执行：

```powershell
# 检查 Maven 版本
mvn -version

# 检查 Maven 是否能找到 Java
mvn -version | Select-String "Java version"

# 应该显示: Java version: 17.0.17
```

#### 4.3 验证项目构建

```powershell
# 进入项目根目录
cd D:\IOE-DREAM

# 清理并编译（跳过测试）
mvn clean compile -DskipTests

# 如果成功，说明配置正确
```

### 5. 配置工作区设置

在项目根目录创建 `.vscode/settings.json`（如果不存在）：

```json
{
  // 项目特定的 Java 配置
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "C:\\Program Files\\Microsoft\\jdk-17.0.17.10-hotspot",
      "default": true
    }
  ],
  
  // Maven 可执行文件路径
  "maven.executable.path": "C:\\ProgramData\\chocolatey\\lib\\maven\\apache-maven-3.9.11\\bin\\mvn.cmd",
  
  // 文件编码
  "files.encoding": "utf8",
  
  // 排除不需要索引的文件
  "files.exclude": {
    "**/target": true,
    "**/.classpath": true,
    "**/.project": true,
    "**/.settings": true,
    "**/.factorypath": true
  },
  
  // Java 项目配置
  "java.project.sourcePaths": ["src/main/java"],
  "java.project.outputPath": "target/classes",
  "java.project.referencedLibraries": [
    "target/**/*.jar"
  ]
}
```

## 🔧 常见问题排查

### 问题1: Java 版本不匹配

**症状**: IDE 提示 Java 版本错误

**解决方案**:
1. 检查 `java -version` 输出
2. 确保 JAVA_HOME 指向 Java 17
3. 在 Cursor 设置中重新配置 Java Runtime

### 问题2: Maven 找不到依赖

**症状**: Maven 下载依赖失败或很慢

**解决方案**:
1. 配置 Maven 镜像（使用阿里云镜像）
2. 清理本地仓库: `mvn dependency:purge-local-repository`
3. 重新下载: `mvn dependency:resolve`

### 问题3: 编码问题

**症状**: 中文注释显示乱码

**解决方案**:
1. 确保所有文件编码为 UTF-8
2. 在 `.vscode/settings.json` 中设置 `"files.encoding": "utf8"`
3. 重启 Cursor IDE

### 问题4: 构建顺序问题

**症状**: 编译时提示找不到 `microservices-common` 类

**解决方案**:
1. 先构建公共模块: `mvn clean install -pl microservices/microservices-common -am -DskipTests`
2. 然后构建业务服务
3. 或使用统一构建脚本: `.\scripts\build-all.ps1`

## 📝 快速配置脚本

创建了自动配置脚本 `scripts\configure-ide-java-maven.ps1`，可以自动配置 IDE 设置。

执行方式：

```powershell
.\scripts\configure-ide-java-maven.ps1
```

## ✅ 配置检查清单

完成配置后，请检查以下项目：

- [ ] Java 17 已安装并配置
- [ ] JAVA_HOME 环境变量已设置
- [ ] Maven 3.9+ 已安装
- [ ] Cursor Java 扩展已安装
- [ ] `.vscode/settings.json` 已配置
- [ ] Maven `settings.xml` 已配置（可选）
- [ ] 项目可以正常编译: `mvn clean compile`
- [ ] IDE 可以正确识别 Java 项目
- [ ] 代码补全和跳转功能正常

## 📚 相关文档

- [项目技术栈快速参考](../TECHNOLOGY_STACK_QUICK_REFERENCE.md)
- [构建顺序强制标准](../BUILD_ORDER_MANDATORY_STANDARD.md)
- [开发环境配置指南](../repowiki/zh/content/开发指南.md)

## 🔄 更新记录

- 2025-01-30: 创建 IDE 配置指南，基于项目实际技术栈版本
