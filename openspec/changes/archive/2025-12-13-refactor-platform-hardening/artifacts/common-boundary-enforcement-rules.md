# microservices-common 边界强制规则

## 概述

本文档定义 `microservices-common` 的边界强制规则，禁止新增业务域代码进入 common，确保 common 仅包含纯公共能力。

## 核心原则

**microservices-common 的职责**：
- ✅ 基础能力（异常、DTO、工具类）
- ✅ 跨域公共能力（认证授权、链路追踪、网关客户端）
- ✅ 公共接口和 contract（常量、枚举、Manager 接口）

**禁止进入 common 的代码**：
- ❌ 业务域 Entity（如 AccessPermissionApplyEntity）
- ❌ 业务域 DAO（如 AccessPermissionApplyDao）
- ❌ 业务域 Service/Manager 实现（如 AccessPermissionApplyService）
- ❌ 业务域特定的工具类和模型
- ❌ 业务域的配置和常量

## 检查规则

### 规则 1：包名检查

**规则**：common 中的包名必须是通用的，不能包含业务域名称

**禁止的包名**：
```
net.lab1024.sa.common.access.*          ❌ 门禁业务
net.lab1024.sa.common.attendance.*      ❌ 考勤业务
net.lab1024.sa.common.visitor.*         ❌ 访客业务
net.lab1024.sa.common.consume.*         ❌ 消费业务
net.lab1024.sa.common.device.*          ❌ 设备业务
```

**允许的包名**：
```
net.lab1024.sa.common.auth.*            ✅ 认证授权（跨域）
net.lab1024.sa.common.tracing.*         ✅ 链路追踪（跨域）
net.lab1024.sa.common.gateway.*         ✅ 网关客户端（跨域）
net.lab1024.sa.common.workflow.*        ✅ 工作流 contract（跨域）
net.lab1024.sa.common.exception.*       ✅ 异常处理（基础）
net.lab1024.sa.common.dto.*             ✅ DTO 基类（基础）
net.lab1024.sa.common.util.*            ✅ 工具类（基础）
```

**检查命令**：
```bash
# 扫描禁止的包名
find microservices-common -type d -name "access" -o -name "attendance" -o -name "visitor" -o -name "consume" -o -name "device"

# 或使用 grep
grep -r "package net.lab1024.sa.common.access" microservices-common/src
grep -r "package net.lab1024.sa.common.attendance" microservices-common/src
grep -r "package net.lab1024.sa.common.visitor" microservices-common/src
grep -r "package net.lab1024.sa.common.consume" microservices-common/src
grep -r "package net.lab1024.sa.common.device" microservices-common/src
```

### 规则 2：类名检查

**规则**：common 中的类名不能包含业务域特定的名称

**禁止的类名**：
```
AccessPermissionApplyEntity          ❌ 业务域 Entity
AccessPermissionApplyDao             ❌ 业务域 DAO
AccessPermissionApplyService         ❌ 业务域 Service
AttendanceLeaveEntity                ❌ 业务域 Entity
VisitorAppointmentEntity             ❌ 业务域 Entity
RefundApplicationEntity              ❌ 业务域 Entity
```

**允许的类名**：
```
ResponseDTO                          ✅ 通用 DTO
PageResult                           ✅ 通用分页结果
JwtTokenUtil                         ✅ 通用工具
GatewayServiceClient                 ✅ 通用客户端
WorkflowApprovalManager              ✅ 跨域 Manager
```

**检查命令**：
```bash
# 扫描业务域特定的类
grep -r "class.*Entity" microservices-common/src | grep -E "(Access|Attendance|Visitor|Consume|Device|Refund)"
grep -r "class.*Dao" microservices-common/src | grep -E "(Access|Attendance|Visitor|Consume|Device|Refund)"
grep -r "class.*Service" microservices-common/src | grep -E "(Access|Attendance|Visitor|Consume|Device|Refund)"
```

### 规则 3：依赖检查

**规则**：common 不能依赖业务服务的代码

**禁止的依赖**：
```xml
<!-- ❌ 禁止依赖业务服务 -->
<dependency>
    <groupId>com.ioe</groupId>
    <artifactId>ioedream-access-service</artifactId>
</dependency>

<dependency>
    <groupId>com.ioe</groupId>
    <artifactId>ioedream-attendance-service</artifactId>
</dependency>
```

**允许的依赖**：
```xml
<!-- ✅ 允许依赖基础库 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>
```

**检查命令**：
```bash
# 检查 pom.xml 中的依赖
grep -E "(ioedream-access|ioedream-attendance|ioedream-visitor|ioedream-consume|ioedream-device)" microservices-common/pom.xml
```

### 规则 4：导入检查

**规则**：common 中的类不能导入业务域特定的类

**禁止的导入**：
```java
// ❌ 禁止导入业务域类
import net.lab1024.sa.access.domain.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.attendance.domain.entity.AttendanceLeaveEntity;
import net.lab1024.sa.visitor.domain.entity.VisitorAppointmentEntity;
```

**允许的导入**：
```java
// ✅ 允许导入通用类
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.stereotype.Component;
```

**检查命令**：
```bash
# 扫描禁止的导入
grep -r "import.*access.*entity" microservices-common/src
grep -r "import.*attendance.*entity" microservices-common/src
grep -r "import.*visitor.*entity" microservices-common/src
grep -r "import.*consume.*entity" microservices-common/src
grep -r "import.*device.*entity" microservices-common/src
```

## 自动化检查脚本

### Maven 插件配置

在 `microservices-common/pom.xml` 中添加以下配置：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>3.0.0</version>
    <executions>
        <execution>
            <id>enforce-common-boundary</id>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <bannedDependencies>
                        <excludes>
                            <exclude>com.ioe:ioedream-access-service</exclude>
                            <exclude>com.ioe:ioedream-attendance-service</exclude>
                            <exclude>com.ioe:ioedream-visitor-service</exclude>
                            <exclude>com.ioe:ioedream-consume-service</exclude>
                            <exclude>com.ioe:ioedream-device-comm-service</exclude>
                        </excludes>
                        <message>microservices-common 不能依赖业务服务</message>
                    </bannedDependencies>
                </rules>
                <fail>true</fail>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Shell 脚本检查

```bash
#!/bin/bash

# 检查禁止的包名
echo "检查禁止的包名..."
FORBIDDEN_PACKAGES=("access" "attendance" "visitor" "consume" "device")
for pkg in "${FORBIDDEN_PACKAGES[@]}"; do
    if grep -r "package net.lab1024.sa.common.$pkg" microservices-common/src > /dev/null; then
        echo "❌ 发现禁止的包名: net.lab1024.sa.common.$pkg"
        exit 1
    fi
done

# 检查禁止的依赖
echo "检查禁止的依赖..."
if grep -E "(ioedream-access|ioedream-attendance|ioedream-visitor|ioedream-consume|ioedream-device)" microservices-common/pom.xml > /dev/null; then
    echo "❌ 发现禁止的依赖"
    exit 1
fi

echo "✅ 所有检查通过"
```

## 检查清单

### 代码审查检查清单

- [ ] 新增代码不在禁止的包名中
- [ ] 新增类名不包含业务域特定的名称
- [ ] 新增代码不依赖业务服务
- [ ] 新增代码不导入业务域特定的类
- [ ] 新增代码遵循 common 的职责定义

### CI/CD 检查清单

- [ ] Maven Enforcer Plugin 配置已启用
- [ ] 禁止依赖检查在编译时执行
- [ ] 包名和类名检查在代码审查时执行
- [ ] 导入检查在静态分析时执行

## 违规处理

### 发现违规时的处理流程

1. **立即停止合并**：PR 不能合并
2. **通知开发者**：说明违规内容和原因
3. **要求修改**：将代码移到对应的业务服务
4. **重新审查**：修改后重新审查和测试
5. **记录违规**：在变更日志中记录

### 违规示例和修正

**示例 1：业务域 Entity 进入 common**

```java
// ❌ 错误：在 microservices-common 中定义业务域 Entity
package net.lab1024.sa.common.access.entity;

public class AccessPermissionApplyEntity {
    // ...
}
```

**修正**：
```java
// ✅ 正确：在业务服务中定义
package net.lab1024.sa.access.domain.entity;

public class AccessPermissionApplyEntity {
    // ...
}
```

**示例 2：业务域依赖进入 common**

```xml
<!-- ❌ 错误：common 依赖业务服务 -->
<dependency>
    <groupId>com.ioe</groupId>
    <artifactId>ioedream-access-service</artifactId>
</dependency>
```

**修正**：
```xml
<!-- ✅ 正确：业务服务依赖 common -->
<dependency>
    <groupId>com.ioe</groupId>
    <artifactId>microservices-common</artifactId>
</dependency>
```

## 相关文档

- [microservices-common 拆分迁移顺序](./common-refactor-migration-order.md)
- [API 契约基线](../../documentation/api/API-CONTRACT-BASELINE.md)
- [开发规范](../../documentation/technical/UNIFIED_DEVELOPMENT_STANDARDS.md)
