# IOE-DREAM POM 依赖模板体系

## 📋 目录

- [模板概述](#模板概述)
- [模板分类](#模板分类)
- [使用指南](#使用指南)
- [版本管理](#版本管理)
- [最佳实践](#最佳实践)

---

## 模板概述

IOE-DREAM 项目采用统一的 POM 依赖模板体系，确保：

✅ **版本一致性**: 所有模块使用统一的依赖版本
✅ **依赖最小化**: 按需引入依赖，避免冗余
✅ **构建标准化**: 统一的编译、测试、打包配置
✅ **合规性保障**: 自动化的代码质量检查

---

## 模板分类

### 1. 公共核心模块模板

**位置**: `documentation/maven/template-pom-common-core.xml`

**适用场景**:
- `microservices-common-core` - 最小稳定内核
- 不依赖任何其他内部模块的纯工具库

**依赖特征**:
- ❌ 不依赖 Spring Boot/Cloud
- ❌ 不依赖其他内部模块
- ✅ 只依赖纯 Java 库（Lombok, SLF4J, Commons Lang3 等）

**构建特征**:
- 禁用 `spring-boot-maven-plugin` 的 repackage
- 不生成可执行 JAR

---

### 2. 公共功能模块模板

**位置**: `documentation/maven/template-pom-common-functional.xml`

**适用场景**:
- `microservices-common-data` - 数据访问层
- `microservices-common-security` - 安全认证层
- `microservices-common-cache` - 缓存管理
- `microservices-common-monitor` - 监控模块
- `microservices-common-storage` - 文件存储
- `microservices-common-export` - 导出功能
- `microservices-common-workflow` - 工作流
- `microservices-common-permission` - 权限验证
- `microservices-common-business` - 业务公共组件
- `microservices-common-util` - 工具类模块

**依赖特征**:
- ✅ 依赖 `microservices-common-core`
- ✅ 包含特定功能依赖（如 MyBatis-Plus, Spring Security）
- ✅ 可选依赖其他细粒度模块

**构建特征**:
- 禁用 `spring-boot-maven-plugin` 的 repackage
- 生成库 JAR 供其他模块依赖

---

### 3. 业务服务模块模板

**位置**: `documentation/maven/template-pom-business-service.xml`

**适用场景**:
- `ioedream-access-service` - 门禁服务
- `ioedream-attendance-service` - 考勤服务
- `ioedream-consume-service` - 消费服务
- `ioedream-video-service` - 视频服务
- `ioedream-visitor-service` - 访客服务
- `ioedream-oa-service` - OA服务

**依赖特征**:
- ✅ 依赖多个细粒度公共模块
- ✅ 包含完整 Spring Boot 依赖
- ✅ 包含 Spring Cloud 依赖（Nacos, Gateway 等）
- ✅ 包含业务特定依赖

**构建特征**:
- 启用 `spring-boot-maven-plugin` 的 repackage
- 生成可执行 JAR

---

### 4. 基础设施服务模板

**位置**: `documentation/maven/template-pom-infra-service.xml`

**适用场景**:
- `ioedream-gateway-service` - 网关服务
- `ioedream-common-service` - 公共业务服务
- `ioedream-device-comm-service` - 设备通讯服务

**依赖特征**:
- ✅ 依赖所有细粒度公共模块
- ✅ 包含完整的 Spring Cloud Gateway 依赖
- ✅ 包含特殊功能依赖（如 WebSocket, MQTT）

**构建特征**:
- 启用 `spring-boot-maven-plugin` 的 repackage
- 生成可执行 JAR

---

## 使用指南

### 新增公共功能模块

1. **复制模板**:
   ```bash
   cp documentation/maven/template-pom-common-functional.xml \
      microservices/microservices-common-xxx/pom.xml
   ```

2. **修改基本信息**:
   ```xml
   <artifactId>microservices-common-xxx</artifactId>
   <name>Microservices Common XXX</name>
   <description>XXX功能模块：详细描述</description>
   ```

3. **调整依赖**:
   - 保留 `microservices-common-core` 依赖
   - 添加模块特定功能依赖
   - 移除不需要的依赖

### 新增业务服务

1. **复制模板**:
   ```bash
   cp documentation/maven/template-pom-business-service.xml \
      microservices/ioedream-xxx-service/pom.xml
   ```

2. **修改基本信息**:
   ```xml
   <artifactId>ioedream-xxx-service</artifactId>
   <name>IOE-DREAM XXX Service</name>
   <description>XXX服务：详细描述</description>
   ```

3. **调整细粒度模块依赖**:
   - 根据业务需求选择需要的细粒度模块
   - 遵循依赖最小化原则

---

## 版本管理

### 依赖版本统一管理

所有依赖版本在 **父 POM** 中统一定义:

```xml
<!-- microservices/pom.xml -->
<properties>
  <spring-boot.version>3.5.8</spring-boot.version>
  <spring-cloud.version>2025.0.0</spring-cloud.version>
  <mybatis-plus.version>3.5.15</mybatis-plus.version>
  <!-- ... 更多版本定义 -->
</properties>
```

### 版本更新流程

1. **评估影响范围**: 检查哪些模块会受影响
2. **测试验证**: 在测试环境验证新版本
3. **更新父 POM**: 修改 `microservices/pom.xml` 中的版本属性
4. **全量构建**: 运行 `mvn clean install` 验证所有模块
5. **发布通知**: 通知团队成员版本变更

---

## 最佳实践

### ✅ DO - 推荐做法

1. **使用 `${project.version}` 引用内部模块依赖**:
   ```xml
   <dependency>
     <groupId>net.lab1024.sa</groupId>
     <artifactId>microservices-common-core</artifactId>
     <version>${project.version}</version>
   </dependency>
   ```

2. **不指定版本号引入 Spring 管理的依赖**:
   ```xml
   <!-- ✅ 正确 -->
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   ```

3. **按需引入依赖，避免冗余**:
   - 只引入真正需要的依赖
   - 定期清理未使用的依赖

4. **使用 `<optional>true</optional>` 标记可选依赖**:
   ```xml
   <dependency>
     <groupId>org.projectlombok</groupId>
     <artifactId>lombok</artifactId>
     <optional>true</optional>
   </dependency>
   ```

### ❌ DON'T - 禁止做法

1. **❌ 禁止硬编码版本号**:
   ```xml
   <!-- ❌ 错误 -->
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-web</artifactId>
     <version>3.5.8</version>  <!-- 不应显式指定版本 -->
   </dependency>
   ```

2. **❌ 禁止引入未在父 POM 管理的依赖**:
   - 新增依赖必须先在父 POM 的 `<dependencyManagement>` 中声明
   - 特殊情况需经架构委员会审批

3. **❌ 禁止使用 SNAPSHOT 版本**:
   - 生产环境禁止使用 SNAPSHOT 依赖
   - 开发环境谨慎使用

4. **❌ 禁止循环依赖**:
   - 模块 A → 模块 B → 模块 A
   - 使用 `mvn dependency:tree` 检查依赖树

---

## 依赖冲突解决

### 常见冲突类型

1. **版本冲突**: 同一依赖的不同版本
2. **传递依赖冲突**: 通过不同路径引入的依赖
3. **类冲突**: 不同 JAR 包中的同名类

### 排查工具

```bash
# 查看依赖树
mvn dependency:tree

# 分析依赖
mvn dependency:analyze

# 查看有效 POM
mvn help:effective-pom
```

### 解决策略

1. **使用 `<exclusion>` 排除冲突依赖**:
   ```xml
   <dependency>
     <groupId>com.example</groupId>
     <artifactId>example-lib</artifactId>
     <exclusions>
       <exclusion>
         <groupId>org.conflict</groupId>
         <artifactId>conflict-lib</artifactId>
       </exclusion>
     </exclusions>
   </dependency>
   ```

2. **在父 POM 中强制指定版本**:
   ```xml
   <dependencyManagement>
     <dependencies>
       <dependency>
         <groupId>org.conflict</groupId>
         <artifactId>conflict-lib</artifactId>
         <version>1.0.0</version>  <!-- 强制使用此版本 -->
       </dependency>
     </dependencies>
   </dependencyManagement>
   ```

---

## 相关文档

- **父 POM**: [microservices/pom.xml](../../microservices/pom.xml)
- **构建脚本**: [scripts/build-all.ps1](../../scripts/build-all.ps1)
- **依赖管理**: [DEPENDENCY_MANAGEMENT.md](./DEPENDENCY_MANAGEMENT.md)
- **最佳实践**: [MVP_BEST_PRACTICES.md](./MVP_BEST_PRACTICES.md)

---

**📅 最后更新**: 2025-12-26
**👥 维护者**: IOE-DREAM 架构委员会
