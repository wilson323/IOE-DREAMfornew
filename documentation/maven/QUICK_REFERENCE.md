# POM 依赖快速参考指南

## 🚀 快速开始

### 1. 新增公共模块

```bash
# 1. 复制模板
cp documentation/maven/template-pom-common-functional.xml \
   microservices/microservices-common-xxx/pom.xml

# 2. 修改基本信息
# - <artifactId>microservices-common-xxx</artifactId>
# - <name>Microservices Common XXX</name>
# - <description>XXX功能模块：...</description>

# 3. 根据功能调整依赖
# - 保留核心基础依赖 (common-core, lombok, spring-boot-starter)
# - 选择功能特定依赖 (参考模板注释块)
# - 保留测试依赖

# 4. 构建验证
mvn clean install
```

### 2. 新增业务服务

```bash
# 1. 复制模板
cp documentation/maven/template-pom-business-service.xml \
   microservices/ioedream-xxx-service/pom.xml

# 2. 修改基本信息
# - <artifactId>ioedream-xxx-service</artifactId>
# - <name>IOE-DREAM XXX Service</name>
# - <description>XXX服务：...</description>

# 3. 选择细粒度模块依赖
# - common-core: 必须
# - common-data: 需要数据库时
# - common-security: 需要认证时
# - common-cache: 需要缓存时
# - common-monitor: 需要监控时
# - common-storage: 需要文件存储时
# - common-export: 需要导出时
# - common-workflow: 需要工作流时
# - common-permission: 需要权限验证时
# - common-business: 需要业务组件时
# - common-util: 需要工具类时

# 4. 构建验证
mvn clean package
```

---

## 📦 常用依赖速查

### 核心基础库

| 依赖 | 坐标 | 用途 |
|------|------|------|
| **Common Core** | `microservices-common-core` | 最小稳定内核 |
| **Common Data** | `microservices-common-data` | 数据访问层 |
| **Common Security** | `microservices-common-security` | 安全认证 |
| **Common Cache** | `microservices-common-cache` | 缓存管理 |
| **Common Monitor** | `microservices-common-monitor` | 监控模块 |

### Spring Boot Starter

| Starter | 坐标 | 用途 |
|---------|------|------|
| **Web** | `spring-boot-starter-web` | Web 应用 |
| **Security** | `spring-boot-starter-security` | 安全认证 |
| **Validation** | `spring-boot-starter-validation` | 参数验证 |
| **Actuator** | `spring-boot-starter-actuator` | 监控端点 |
| **Test** | `spring-boot-starter-test` | 测试支持 |

### Spring Cloud 组件

| 组件 | 坐标 | 用途 |
|------|------|------|
| **Gateway** | `spring-cloud-starter-gateway` | API 网关 |
| **Nacos Discovery** | `spring-cloud-starter-alibaba-nacos-discovery` | 服务发现 |
| **Nacos Config** | `spring-cloud-starter-alibaba-nacos-config` | 配置中心 |
| **LoadBalancer** | `spring-cloud-starter-loadbalancer` | 负载均衡 |

### 数据访问

| 依赖 | 坐标 | 版本 |
|------|------|------|
| **MyBatis-Plus** | `mybatis-plus-spring-boot3-starter` | 3.5.15 |
| **MySQL Driver** | `mysql-connector-j` | 8.0.35 |
| **Druid** | `druid-spring-boot-3-starter` | 1.2.25 |
| **Flyway** | `flyway-core` | (BOM 管理) |

### 工具库

| 依赖 | 坐标 | 版本 |
|------|------|------|
| **Lombok** | `lombok` | 1.18.32 |
| **Hutool** | `hutool-all` | 5.8.26 |
| **FastJSON2** | `fastjson2` | 2.0.43 |
| **Commons Lang3** | `commons-lang3` | (BOM 管理) |

---

## 🔧 常用 Maven 命令

### 依赖分析

```bash
# 查看依赖树
mvn dependency:tree

# 分析未使用的依赖
mvn dependency:analyze

# 查看有效 POM
mvn help:effective-pom

# 解析依赖
mvn dependency:resolve

# 列出所有依赖
mvn dependency:list
```

### 构建相关

```bash
# 清理构建
mvn clean

# 编译
mvn compile

# 打包
mvn package

# 安装到本地仓库
mvn install

# 跳过测试
mvn install -DskipTests

# 强制检查更新
mvn clean install -U
```

### 版本管理

```bash
# 查看依赖更新
mvn versions:display-dependency-updates

# 查看插件更新
mvn versions:display-plugin-updates

# 锁定 SNAPSHOT 版本
mvn versions:lock-snapshots

# 设置版本
mvn versions:set -DnewVersion=1.0.1
```

### 验证检查

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 运行 PMD 检查
mvn pmd:check

# 跳过 PMD 检查
mvn pmd:check -Dpmd.skip=true

# 运行 Enforcer 检查
mvn enforcer:enforce
```

---

## ⚠️ 常见问题解决

### Q1: 依赖版本冲突

**现象**:
```
[WARNING] The artifact org.common:common-lib:jar:1.0.0 has been located twice in the dependency tree
```

**解决**:
```bash
# 1. 查看依赖树,找出冲突来源
mvn dependency:tree -Dverbose

# 2. 在父 POM 中强制指定版本
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.common</groupId>
      <artifactId>common-lib</artifactId>
      <version>1.0.0</version>
    </dependency>
  </dependencies>
</dependencyManagement>

# 3. 或者在冲突模块中使用 exclusion
<dependency>
  <groupId>com.example</groupId>
  <artifactId>example-lib</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.common</groupId>
      <artifactId>common-lib</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

### Q2: 找不到某个类

**现象**:
```
[ERROR] cannot find symbol: class ResponseDTO
```

**解决**:
```bash
# 1. 检查是否引入了正确的模块
# 例如: ResponseDTO 在 microservices-common-core 中

# 2. 确认依赖已安装到本地仓库
mvn clean install -pl microservices/microservices-common-core

# 3. 检查导入路径
import net.lab1024.sa.common.domain.ResponseDTO;
```

### Q3: 循环依赖错误

**现象**:
```
[ERROR] The projects in the reactor contain a cyclic reference
```

**解决**:
```bash
# 1. 查看依赖树,找出循环
mvn dependency:tree

# 2. 抽取公共模块到新模块
# 修改前: A → B → A
# 修改后: A → C ← B

# 3. 或使用事件驱动/接口倒置解耦
```

### Q4: 编译注解处理器错误

**现象**:
```
[ERROR] Annotation processor 'org.projectlombok:lombok' not found
```

**解决**:
```xml
<!-- 确保父 POM 中配置了注解处理器路径 -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

### Q5: 测试失败

**现象**:
```
[ERROR] Tests run: 10, Failures: 1, Errors: 0, Skipped: 0
```

**解决**:
```bash
# 1. 跳过测试构建
mvn install -DskipTests

# 2. 只运行特定的测试
mvn test -Dtest=MyTest

# 3. 运行测试并生成报告
mvn test surefire-report:report

# 4. 查看测试失败原因
# 打开 target/surefire-reports/TEST-*.xml 查看详情
```

---

## 📋 检查清单

### 新增模块检查清单

- [ ] 复制了正确的 POM 模板
- [ ] 修改了 `artifactId`、`name`、`description`
- [ ] 选择了正确的细粒度模块依赖
- [ ] 移除了不需要的依赖注释
- [ ] 验证了依赖树无冲突
- [ ] 运行了完整测试套件
- [ ] 在本地仓库成功安装
- [ ] 更新了父 POM 的 `<modules>` 列表

### 版本升级检查清单

- [ ] 查看了官方 Release Notes
- [ ] 检查了 Breaking Changes
- [ ] 验证了与现有代码的兼容性
- [ ] 在测试环境完整验证
- [ ] 运行了回归测试
- [ ] 评估了性能影响
- [ ] 更新了文档说明
- [ ] 通知了团队成员

### 依赖清理检查清单

- [ ] 运行了 `mvn dependency:analyze`
- [ ] 移除了未使用的依赖
- [ ] 添加了缺失的依赖
- [ ] 解决了版本冲突
- [ ] 验证了构建成功
- [ ] 运行了完整测试套件

---

## 🎯 最佳实践速记

### ✅ DO

1. **使用 `${project.version}` 引用内部模块**
   ```xml
   <version>${project.version}</version>
   ```

2. **不指定 Spring 管理的依赖版本**
   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-web</artifactId>
     <!-- 不指定版本 -->
   </dependency>
   ```

3. **按需引入细粒度模块**
   ```xml
   <!-- ✅ 只引入需要的模块 -->
   <dependency>
     <groupId>net.lab1024.sa</groupId>
     <artifactId>microservices-common-data</artifactId>
   </dependency>
   ```

4. **使用 Optional 标记编译时依赖**
   ```xml
   <dependency>
     <groupId>org.projectlombok</groupId>
     <artifactId>lombok</artifactId>
     <optional>true</optional>
   </dependency>
   ```

### ❌ DON'T

1. **❌ 硬编码版本号**
   ```xml
   <version>3.5.8</version>  <!-- ❌ -->
   ```

2. **❌ 引入整个聚合模块**
   ```xml
   <artifactId>microservices-common</artifactId>  <!-- ❌ 包含所有模块 -->
   ```

3. **❌ 生产环境使用 SNAPSHOT**
   ```xml
   <version>1.0.0-SNAPSHOT</version>  <!-- ❌ -->
   ```

4. **❌ 循环依赖**
   ```
   A → B → A  <!-- ❌ -->
   ```

---

## 📚 相关文档

- **POM 模板体系**: [README.md](./README.md)
- **依赖管理规范**: [DEPENDENCY_MANAGEMENT.md](./DEPENDENCY_MANAGEMENT.md)
- **父 POM 配置**: [../../microservices/pom.xml](../../microservices/pom.xml)
- **项目规范**: [../../CLAUDE.md](../../CLAUDE.md)

---

**📅 最后更新**: 2025-12-26
**👥 维护者**: IOE-DREAM 架构委员会
