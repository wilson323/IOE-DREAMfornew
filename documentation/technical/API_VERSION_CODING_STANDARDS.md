# API版本编码规范

**文档版本**: v1.0.0
**更新日期**: 2025-01-30
**适用范围**: IOE-DREAM所有微服务代码

---

## 📋 目录

- [1. Jakarta EE 9+ 编码规范](#1-jakarta-ee-9-编码规范)
- [2. OpenAPI 3.0 编码规范](#2-openapi-30-编码规范)
- [3. 违规检查机制](#3-违规检查机制)
- [4. 常见问题FAQ](#4-常见问题faq)

---

## 1. Jakarta EE 9+ 编码规范

### 1.1 依赖注入规范

#### ✅ 推荐用法

```java
import jakarta.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private UserManager userManager;
}
```

#### ❌ 禁止用法

```java
// ❌ 禁止：使用 Java EE 已过时的包
import javax.annotation.Resource;  // 编译错误，CI/CD拒绝

// ❌ 禁止：使用 @Autowired（应使用 @Resource）
import org.springframework.beans.factory.annotation.Autowired;
@Autowired  // 违反架构规范
private UserDao userDao;
```

### 1.2 实体类规范

#### ✅ 推荐用法

```java
package net.lab1024.sa.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Entity
@Table(name = "t_user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50")
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotNull(message = "状态不能为空")
    @Column(name = "status", nullable = false)
    private Integer status;
}
```

#### ❌ 禁止用法

```java
// ❌ 禁止：使用 Java EE 包
import javax.persistence.Entity;      // 编译错误
import javax.validation.constraints.NotNull;  // 编译错误
```

### 1.3 Controller层规范

#### ✅ 推荐用法

```java
package net.lab1024.sa.access.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.common.domain.ResponseDTO;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    @Resource
    private AccessService accessService;

    @PostMapping("/verify")
    public ResponseDTO<AccessResultVO> verifyAccess(
        @Valid @RequestBody AccessVerifyForm form) {

        return ResponseDTO.ok(accessService.verifyAccess(form));
    }
}
```

### 1.4 Service层规范

#### ✅ 推荐用法

```java
package net.lab1024.sa.access.service.impl;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.access.service.AccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AccessServiceImpl implements AccessService {

    @Resource
    private AccessDao accessDao;

    @Resource
    private AccessManager accessManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessResultVO verifyAccess(@NotNull AccessVerifyForm form) {
        // 业务逻辑
        return result;
    }
}
```

### 1.5 Manager层规范

#### ✅ 推荐用法

```java
package net.lab1024.sa.access.manager;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AccessManager {

    private final AccessDao accessDao;
    private final DeviceManager deviceManager;

    // Manager类使用构造函数注入（纯Java类风格）
    @Resource
    public void setAccessDao(AccessDao accessDao) {
        this.accessDao = accessDao;
    }

    @Resource
    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public AccessResultVO verifyAccess(Long userId, Long deviceId) {
        // 复杂业务逻辑编排
        return result;
    }
}
```

---

## 2. OpenAPI 3.0 编码规范

### 2.1 Schema注解基础规范

#### ✅ 推荐用法

```java
package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "门禁通行结果VO")
public class AccessResultVO {

    @Schema(description = "通行记录ID", example = "1")
    private Long recordId;

    @Schema(description = "用户ID", required = true, example = "1001")
    private Long userId;

    @Schema(description = "用户名", required = true, example = "张三")
    private String username;

    @Schema(description = "通行时间", required = true, example = "2025-01-30T10:30:00")
    private LocalDateTime accessTime;

    @Schema(description = "通行状态", required = true, example = "1")
    private Integer accessStatus; // 1-成功 2-失败

    @Schema(description = "通行结果", example = "成功")
    private String resultMessage;
}
```

#### ❌ 禁止用法

```java
// ❌ 禁止：使用 OpenAPI 3.1 API
@Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
private String username;  // CI/CD拒绝合并

// ❌ 禁止：缺少必要的description
@Schema(required = true)
private String username;  // 代码规范检查不通过
```

### 2.2 Form类Schema规范

#### ✅ 推荐用法

```java
package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "门禁验证表单")
public class AccessVerifyForm {

    @Schema(description = "用户ID", required = true, example = "1001")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "设备ID", required = true, example = "DEV001")
    @NotBlank(message = "设备ID不能为空")
    @Size(max = 50, message = "设备ID长度不能超过50")
    private String deviceId;

    @Schema(description = "认证方式", required = true, example = "1", allowableValues = {"1", "2", "3"})
    @NotNull(message = "认证方式不能为空")
    private Integer authType; // 1-人脸 2-指纹 3-刷卡
}
```

### 2.3 Controller方法注解规范

#### ✅ 推荐用法

```java
package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.domain.ResponseDTO;

@RestController
@RequestMapping("/api/v1/access")
@Tag(name = "门禁管理", description = "门禁通行相关接口")
public class AccessController {

    @Resource
    private AccessService accessService;

    @PostMapping("/verify")
    @Operation(summary = "验证门禁通行", description = "验证用户是否有权通过指定门禁设备")
    public ResponseDTO<AccessResultVO> verifyAccess(
        @Parameter(description = "门禁验证表单", required = true)
        @Valid @RequestBody AccessVerifyForm form) {

        return ResponseDTO.ok(accessService.verifyAccess(form));
    }

    @GetMapping("/record/{recordId}")
    @Operation(summary = "查询通行记录", description = "根据ID查询单条通行记录详情")
    public ResponseDTO<AccessRecordDetailVO> getRecord(
        @Parameter(description = "通行记录ID", required = true, example = "1")
        @PathVariable Long recordId) {

        return ResponseDTO.ok(accessService.getRecord(recordId));
    }
}
```

### 2.4 常用Schema属性规范

#### 必填字段

```java
// ✅ 正确：必填字段标准写法
@Schema(description = "用户名", required = true, example = "admin")
@NotBlank(message = "用户名不能为空")
@Size(max = 50, message = "用户名长度不能超过50")
private String username;

// ✅ 正确：数字类型必填字段
@Schema(description = "用户ID", required = true, example = "1001")
@NotNull(message = "用户ID不能为空")
private Long userId;
```

#### 可选字段

```java
// ✅ 正确：可选字段标准写法
@Schema(description = "邮箱地址", example = "admin@example.com")
private String email;  // 默认为可选，无需写required = false

// ✅ 正确：有默认值的可选字段
@Schema(description = "页码", example = "1")
private Integer pageNum = 1;
```

#### 枚举类型字段

```java
// ✅ 正确：枚举类型字段
@Schema(
    description = "认证方式",
    required = true,
    example = "1",
    allowableValues = {"1", "2", "3"}
)
private Integer authType; // 1-人脸 2-指纹 3-刷卡
```

#### 日期时间字段

```java
// ✅ 正确：日期时间字段
@Schema(
    description = "通行时间",
    required = true,
    example = "2025-01-30T10:30:00",
    type = "string",
    format = "date-time"
)
private LocalDateTime accessTime;
```

---

## 3. 违规检查机制

### 3.1 CI/CD自动化检查

**检查时机**:
- 代码提交到main/develop分支时
- 创建Pull Request时
- 合并代码前

**检查范围**:
- `javax.annotation.*` 包使用
- `javax.persistence.*` 包使用
- `javax.validation.*` 包使用
- OpenAPI 3.1 API使用（`requiredMode`）

**违规后果**:
- CI/CD构建失败
- 代码拒绝合并
- 需要修复后重新提交

### 3.2 本地预检查

开发者提交代码前应先运行本地检查：

```bash
# 检查javax包使用
grep -r "import javax\.annotation\." microservices/ --include="*.java"
grep -r "import javax\.persistence\." microservices/ --include="*.java"
grep -r "import javax\.validation\." microservices/ --include="*.java"

# 检查OpenAPI版本
grep -r "requiredMode" microservices/ --include="*.java"
```

### 3.3 IDE配置建议

**IntelliJ IDEA配置**:

1. 禁止导入javax包：
   - Settings → Editor → Code Style → Java → Imports
   - 添加到禁止导入列表：`javax.annotation.*`、`javax.persistence.*`、`javax.validation.*`

2. 自动补全优化：
   - 优先推荐jakarta包
   - 标记javax包为过时

---

## 4. 常见问题FAQ

### Q1: 为什么要从Java EE迁移到Jakarta EE？

**A**: Java EE已捐赠给Eclipse基金会并重命名为Jakarta EE，`javax.*`命名空间由于版权原因无法继续使用。Spring Boot 3.x版本已完全采用Jakarta EE 9+，因此必须进行迁移。

### Q2: OpenAPI 3.0和3.1有什么区别？

**A**:
- OpenAPI 3.0: `required = true`（推荐，与Swagger 2.x兼容）
- OpenAPI 3.1: `requiredMode = Schema.RequiredMode.REQUIRED`（不推荐，与当前Swagger版本不兼容）

项目当前使用Swagger 2.x，只支持OpenAPI 3.0规范。

### Q3: 如何快速检查代码是否符合规范？

**A**: 运行以下命令：
```bash
# 一键检查所有规范
./scripts/api-version-check.sh
```

### Q4: 如果不小心使用了javax包怎么办？

**A**:
1. IDE会显示编译错误
2. CI/CD会拒绝合并
3. 修复方法：将`import javax.xxx`改为`import jakarta.xxx`

### Q5: 能否在特殊情况下使用javax包？

**A**: ❌ 不可以。所有代码100%必须使用Jakarta EE，无例外。

---

## 5. 参考资源

### 5.1 官方文档

- [Jakarta EE 9 Specification](https://jakarta.ee/specifications/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Spring Boot 3.x Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)

### 5.2 项目文档

- [CLAUDE.md - API版本规范](/CLAUDE.md#-api版本规范-2025-01-30新增)
- [README.md - 技术栈现代化升级](/README.md#-技术栈现代化升级2025-01-30)
- [`.github/workflows/api-version-check.yml`](/.github/workflows/api-version-check.yml)

### 5.3 最佳实践

- [Spring Boot with Jakarta EE](https://spring.io/blog/2019/09/03/jakarta-ee-9-with-spring)
- [OpenAPI 3.0 Best Practices](https://swagger.io/docs/specification/best-practices/)

---

**文档维护**: IOE-DREAM架构委员会
**最后更新**: 2025-01-30
**版本历史**:
- v1.0.0 (2025-01-30): 初始版本，建立Jakarta EE和OpenAPI 3.0编码规范
