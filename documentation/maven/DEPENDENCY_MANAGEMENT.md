# IOE-DREAM 依赖管理规范

## 📋 目录

- [依赖管理原则](#依赖管理原则)
- [版本管理](#版本管理)
- [依赖分类](#依赖分类)
- [依赖冲突解决](#依赖冲突解决)
- [依赖安全](#依赖安全)
- [最佳实践](#最佳实践)

---

## 依赖管理原则

### 1. 版本统一管理原则

**核心原则**: 所有依赖版本必须在父 POM 中统一定义

✅ **正确做法**:
```xml
<!-- 父 POM (microservices/pom.xml) -->
<properties>
  <mybatis-plus.version>3.5.15</mybatis-plus.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
      <version>${mybatis-plus.version}</version>
    </dependency>
  </dependencies>
</dependencyManagement>

<!-- 子模块 POM -->
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
  <!-- ✅ 不指定版本,使用父 POM 管理的版本 -->
</dependency>
```

❌ **错误做法**:
```xml
<!-- 子模块 POM -->
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
  <!-- ❌ 硬编码版本号 -->
  <version>3.5.15</version>
</dependency>
```

### 2. 依赖最小化原则

**核心原则**: 只引入真正需要的依赖,避免冗余

✅ **正确做法**:
```xml
<!-- 只引入需要的细粒度模块 -->
<dependency>
  <groupId>net.lab1024.sa</groupId>
  <artifactId>microservices-common-data</artifactId>
</dependency>
```

❌ **错误做法**:
```xml
<!-- 引入整个聚合模块,导致不必要的依赖 -->
<dependency>
  <groupId>net.lab1024.sa</groupId>
  <artifactId>microservices-common</artifactId>
  <!-- ❌ 包含所有细粒度模块,造成依赖冗余 -->
</dependency>
```

### 3. 依赖层次化原则

**核心原则**: 严格遵循模块依赖层次,禁止循环依赖

```
依赖层次:
第1层: microservices-common-core (无内部依赖)
第2层: microservices-common-entity (依赖 core)
第3层: microservices-common-business (依赖 entity)
第4层: 业务服务 (依赖以上各层)
```

❌ **禁止循环依赖**:
```
❌ Service A → Service B → Service A
❌ Module X → Module Y → Module X
```

### 4. 显式依赖原则

**核心原则**: 显式声明所有直接依赖,不要依赖传递依赖

✅ **正确做法**:
```xml
<!-- 显式声明使用的依赖 -->
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-lang3</artifactId>
</dependency>
```

❌ **错误做法**:
```xml
<!-- 依赖其他模块时,隐式使用了 commons-lang3 -->
<dependency>
  <groupId>com.example</groupId>
  <artifactId>example-lib</artifactId>
  <!-- ❌ 未显式声明 commons-lang3,依赖传递可能被破坏 -->
</dependency>
```

---

## 版本管理

### 版本号规范

IOE-DREAM 使用语义化版本号 (Semantic Versioning):

```
格式: MAJOR.MINOR.PATCH
示例: 1.0.0

MAJOR: 不兼容的 API 修改
MINOR: 向下兼容的功能新增
PATCH: 向下兼容的 Bug 修复
```

### 版本号命名约定

| 版本类型 | 命名规则 | 示例 | 说明 |
|---------|---------|------|------|
| **稳定版** | `X.Y.Z` | `1.0.0` | 生产环境使用 |
| **里程碑版** | `X.Y.Z-M` | `1.0.0-M1` | 里程碑版本 |
| **发布候选版** | `X.Y.Z-RC` | `1.0.0-RC1` | 候选版本 |
| **快照版** | `X.Y.Z-SNAPSHOT` | `1.0.0-SNAPSHOT` | 开发中版本 |

⚠️ **禁止生产环境使用 SNAPSHOT 版本**

### 第三方依赖版本管理

#### 官方推荐的版本组合

```xml
<!-- Spring Boot 3.5.8 推荐版本组合 -->
<properties>
  <spring-boot.version>3.5.8</spring-boot.version>
  <spring-cloud.version>2025.0.0</spring-cloud.version>
  <spring-cloud-alibaba.version>2025.0.0.0</spring-cloud-alibaba.version>

  <!-- 数据库 -->
  <mysql.version>8.0.35</mysql.version>
  <mybatis-plus.version>3.5.15</mybatis-plus.version>
  <druid.version>1.2.25</druid.version>

  <!-- 工具库 -->
  <lombok.version>1.18.32</lombok.version>
  <hutool.version>5.8.26</hutool.version>
</properties>
```

#### 版本升级评估清单

升级依赖前必须评估:

- [ ] 查看官方 Release Notes
- [ ] 检查 Breaking Changes
- [ ] 验证与现有代码的兼容性
- [ ] 运行完整测试套件
- [ ] 在测试环境验证
- [ ] 评估性能影响
- [ ] 检查安全性漏洞修复

---

## 依赖分类

### 1. 按来源分类

#### 1.1 Spring 生态依赖

**由 Spring Boot BOM 管理**:
```xml
<!-- 无需指定版本 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**常用依赖列表**:
- `spring-boot-starter-web` - Web 支持
- `spring-boot-starter-data-redis` - Redis 支持
- `spring-boot-starter-security` - 安全支持
- `spring-boot-starter-validation` - 参数验证
- `spring-boot-starter-actuator` - 监控支持

#### 1.2 Spring Cloud 依赖

**由 Spring Cloud Dependencies BOM 管理**:
```xml
<!-- 无需指定版本 -->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

#### 1.3 第三方库依赖

**必须在父 POM 中显式管理版本**:
```xml
<!-- 父 POM -->
<properties>
  <mybatis-plus.version>3.5.15</mybatis-plus.version>
</properties>

<!-- 子模块 -->
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
  <!-- 不指定版本,使用父 POM 的版本 -->
</dependency>
```

### 2. 按作用域分类

| Scope | 说明 | 使用场景 |
|-------|------|---------|
| **compile** (默认) | 编译、测试、运行都依赖 | 大部分依赖 |
| **provided** | 编译、测试依赖,运行时由容器提供 | Servlet API, Jakarta EE API |
| **runtime** | 测试、运行依赖,编译时不需要 | JDBC 驱动,日志实现 |
| **test** | 仅测试依赖 | JUnit, Mockito |
| **system** | 从本地文件系统引入 | ⚠️ 禁止使用 |

✅ **正确的 Scope 使用**:
```xml
<!-- Servlet API (provided) -->
<dependency>
  <groupId>jakarta.servlet</groupId>
  <artifactId>jakarta.servlet-api</artifactId>
  <scope>provided</scope>
</dependency>

<!-- MySQL 驱动 (runtime) -->
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <scope>runtime</scope>
</dependency>

<!-- 测试框架 (test) -->
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
```

### 3. 按可选性分类

#### Optional 依赖

**标记为可选的依赖不会传递给依赖此模块的项目**

```xml
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <optional>true</optional>
  <!-- ✅ Lombok 只在编译时需要,不需要传递 -->
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-devtools</artifactId>
  <optional>true</optional>
  <!-- ✅ 开发工具不需要传递 -->
</dependency>
```

**使用场景**:
- 编译时依赖（如 Lombok）
- 开发时依赖（如 DevTools）
- 可选功能依赖（如特定数据库驱动）

#### Exclusions 排除依赖

**显式排除不需要的传递依赖**

```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>example-lib</artifactId>
  <exclusions>
    <!-- 排除冲突的依赖 -->
    <exclusion>
      <groupId>org.conflict</groupId>
      <artifactId>conflict-lib</artifactId>
    </exclusion>
    <!-- 排除不需要的功能 -->
    <exclusion>
      <groupId>org.unnecessary</groupId>
      <artifactId>unnecessary-lib</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

---

## 依赖冲突解决

### 冲突类型

#### 1. 版本冲突

**现象**: 同一依赖的不同版本被引入

**排查工具**:
```bash
# 查看依赖树
mvn dependency:tree

# 查看依赖分析
mvn dependency:analyze

# 查看有效 POM
mvn help:effective-pom
```

**解决策略**:

**策略1: 在父 POM 中强制指定版本**
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.conflict</groupId>
      <artifactId>conflict-lib</artifactId>
      <version>1.0.0</version>
      <!-- ✅ 强制使用此版本,覆盖传递依赖 -->
    </dependency>
  </dependencies>
</dependencyManagement>
```

**策略2: 使用 Exclusion 排除冲突版本**
```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>example-lib</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.conflict</groupId>
      <artifactId>conflict-lib</artifactId>
      <!-- ✅ 排除,使用其他路径的版本 -->
    </exclusion>
  </exclusions>
</dependency>
```

#### 2. 类冲突 (ClassCastException)

**现象**: 不同 JAR 包中的同名类导致运行时异常

**常见场景**:
- `javax.*` vs `jakarta.*` (Java EE 迁移)
- 不同版本的 ASM 字节码库
- 不同版本的 JSON 库

**解决策略**:

1. **使用 `mvn dependency:tree` 找出冲突来源**
2. **使用 Exclusion 排除冲突 JAR**
3. **在父 POM 中统一版本**

#### 3. 循环依赖

**现象**: 模块 A → B → A

**解决策略**:

1. **抽取公共模块到新模块 C**
   ```
   修改前: A → B → A
   修改后: A → C ← B
   ```

2. **使用事件驱动架构解耦**
   ```
   修改前: Service A → Service B
   修改后: Service A → 消息队列 → Service B
   ```

3. **使用接口倒置 (DIP 原则)**
   ```
   修改前: A 依赖 B 的实现
   修改后: A 和 B 都依赖接口模块 I
   ```

---

## 依赖安全

### 安全漏洞扫描

#### 定期扫描

**使用 OWASP Dependency Check**:
```bash
mvn org.owasp:dependency-check-maven:check
```

**查看扫描报告**:
```bash
# 报告位置
target/dependency-check-report.html
```

#### 漏洞响应流程

1. **评估严重程度** (Critical/High/Medium/Low)
2. **查找修复版本** (官方 Release Notes)
3. **验证修复版本** (测试环境)
4. **更新依赖版本** (父 POM)
5. **全量回归测试** (所有模块)
6. **发布安全补丁** (生产环境)

### 安全依赖配置

#### 禁止不安全的依赖

```xml
<!-- ❌ 禁止使用存在已知漏洞的版本 -->
<dependency>
  <groupId>com.example</groupId>
  <artifactId>vulnerable-lib</artifactId>
  <version>1.0.0</version>
  <!-- 存在 CVE-2024-12345 漏洞 -->
</dependency>
```

#### 强制使用安全版本

```xml
<dependencyManagement>
  <dependencies>
    <!-- ✅ 强制使用修复版本 -->
    <dependency>
      <groupId>com.example</groupId>
      <artifactId>vulnerable-lib</artifactId>
      <version>1.0.1</version>
      <!-- 已修复 CVE-2024-12345 -->
    </dependency>
  </dependencies>
</dependencyManagement>
```

### 依赖校验

#### GPG 校验

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-gpg-plugin</artifactId>
  <version>3.1.0</version>
  <executions>
    <execution>
      <id>sign-artifacts</id>
      <phase>verify</phase>
      <goals>
        <goal>sign</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

#### Checksum 校验

```bash
# 生成 SHA256 校验和
sha256sum target/xxx-service-1.0.0.jar > xxx.jar.sha256

# 验证校验和
sha256sum -c xxx.jar.sha256
```

---

## 最佳实践

### ✅ DO - 推荐做法

#### 1. 使用 BOM 管理版本

```xml
<dependencyManagement>
  <dependencies>
    <!-- Spring Boot BOM -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>${spring-boot.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>

    <!-- Spring Cloud BOM -->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-dependencies</artifactId>
      <version>${spring-cloud.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>

    <!-- Spring Cloud Alibaba BOM -->
    <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-alibaba-dependencies</artifactId>
      <version>${spring-cloud-alibaba.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

#### 2. 定期清理未使用依赖

```bash
# 分析未使用的依赖
mvn dependency:analyze

# 输出示例:
# [WARNING] Used undeclared dependencies:
# [WARNING]   org.slf4j:slf4j-api:jar:1.7.36
# [WARNING] Unused declared dependencies:
# [WARNING]   com.google.guava:guava:jar:31.1-jre
```

#### 3. 使用 Enforcer 强制依赖规则

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-enforcer-plugin</artifactId>
  <version>3.5.0</version>
  <executions>
    <execution>
      <id>enforce-dependencies</id>
      <goals>
        <goal>enforce</goal>
      </goals>
      <configuration>
        <rules>
          <!-- 禁止 SNAPSHOT 依赖 -->
          <requireReleaseDeps>
            <message>不允许使用 SNAPSHOT 依赖</message>
          </requireReleaseDeps>

          <!-- 禁止循环依赖 -->
          <banCircularDependencies/>

          <!-- 依赖收敛检查 -->
          <dependencyConvergence/>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

#### 4. 使用依赖锁定

```bash
# 生成依赖锁定文件
mvn dependency:go-offline

# 生成锁文件
mvn versions:lock-snapshots

# 使用锁文件构建
mvn deploy -Dmaven.deploy.skip=true -DdependencyLock=enabled
```

### ❌ DON'T - 禁止做法

#### 1. 禁止硬编码版本号

```xml
<!-- ❌ 错误 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <version>3.5.8</version>
</dependency>

<!-- ✅ 正确 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <!-- 不指定版本,使用 BOM 管理 -->
</dependency>
```

#### 2. 禁止引入未使用的依赖

```xml
<!-- ❌ 错误: 引入但未使用 -->
<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <!-- 代码中完全没有使用 Guava -->
</dependency>
```

#### 3. 禁止使用过时的依赖

```xml
<!-- ❌ 错误: 使用已停止维护的版本 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <version>2.7.18</version>
  <!-- Spring Boot 2.x 已停止维护 -->
</dependency>

<!-- ✅ 正确: 使用当前维护的版本 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <!-- Spring Boot 3.x 当前维护版本 -->
</dependency>
```

#### 4. 禁止 SNAPSHOT 依赖上生产

```xml
<!-- ❌ 错误: 生产环境使用 SNAPSHOT -->
<dependency>
  <groupId>com.example</groupId>
  <artifactId>example-lib</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- ✅ 正确: 使用稳定版本 -->
<dependency>
  <groupId>com.example</groupId>
  <artifactId>example-lib</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## 相关文档

- **POM 模板体系**: [README.md](./README.md)
- **父 POM 配置**: [../../microservices/pom.xml](../../microservices/pom.xml)
- **构建脚本**: [../../scripts/build-all.ps1](../../scripts/build-all.ps1)

---

**📅 最后更新**: 2025-12-26
**👥 维护者**: IOE-DREAM 架构委员会
