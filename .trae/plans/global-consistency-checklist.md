# IOE-DREAM 全局一致性检查清单

> **版本**: v1.0.0  
> **适用范围**: 所有代码提交、PR审查、架构变更  
> **检查级别**: P0级（必须通过）  
> **更新日期**: 2025-12-14

---

## 🎯 检查目标

确保项目在**架构、技术栈、开发规范、代码规范**四个维度保持全局一致，避免技术债务累积。

---

## 📋 代码提交前检查清单

### 1. 架构一致性检查 ✅/❌

- [ ] **微服务数量限制**
  - [ ] 未创建新的微服务（7微服务架构严格限制）
  - [ ] 新功能已整合到现有微服务

- [ ] **服务间调用规范**
  - [ ] 未使用`@EnableFeignClients`（白名单除外）
  - [ ] 未使用`@FeignClient`直接调用
  - [ ] 使用`GatewayServiceClient`进行服务间调用
  - [ ] 无跨服务直接数据库访问

- [ ] **四层架构边界**
  - [ ] Controller不直接调用DAO
  - [ ] Service不直接访问数据库
  - [ ] Manager类不使用Spring注解（纯Java类）
  - [ ] 无跨层访问

---

### 2. 技术栈一致性检查 ✅/❌

- [ ] **版本统一管理**
  - [ ] 未在子模块中覆盖父POM版本
  - [ ] 所有依赖版本在父POM中定义
  - [ ] 文档中技术栈版本与标准规范一致

- [ ] **依赖管理**
  - [ ] 使用`${project.version}`引用公共模块版本
  - [ ] 无版本冲突
  - [ ] 依赖版本与`microservices/pom.xml`一致

**标准版本参考**:
- Spring Boot: 3.5.8
- Spring Cloud: 2025.0.0
- Spring Cloud Alibaba: 2025.0.0.0
- Java: 17
- MyBatis-Plus: 3.5.15
- MySQL: 8.0.35
- Druid: 1.2.25

---

### 3. 代码规范一致性检查 ✅/❌

- [ ] **依赖注入规范**
  - [ ] 使用`@Resource`而非`@Autowired`
  - [ ] 导入`jakarta.annotation.Resource`
  - [ ] 未使用`org.springframework.beans.factory.annotation.Autowired`

- [ ] **DAO层规范**
  - [ ] 使用`@Mapper`注解
  - [ ] 接口命名为`XxxDao`（非`XxxRepository`）
  - [ ] 继承`BaseMapper<Entity>`
  - [ ] 未使用`@Repository`注解

- [ ] **包名规范**
  - [ ] 使用`jakarta.*`包（jakarta.annotation, jakarta.validation等）
  - [ ] 未使用`javax.*`包（除javax.crypto等JDK自带）
  - [ ] 无包名混用

- [ ] **HTTP方法规范**
  - [ ] 查询接口使用`@GetMapping`
  - [ ] 创建接口使用`@PostMapping`
  - [ ] 更新接口使用`@PutMapping`
  - [ ] 删除接口使用`@DeleteMapping`
  - [ ] 未使用POST进行查询操作

- [ ] **代码行数规范**
  - [ ] 单文件≤400行
  - [ ] 单方法≤50行
  - [ ] 超大文件已拆分

---

### 4. 开发规范一致性检查 ✅/❌

- [ ] **文件编码规范**
  - [ ] PowerShell脚本：UTF-8 without BOM
  - [ ] Java文件：UTF-8
  - [ ] 配置文件：UTF-8

- [ ] **注释规范**
  - [ ] 所有公共方法有JavaDoc注释
  - [ ] 关键业务逻辑有中文注释
  - [ ] 复杂算法有说明

- [ ] **异常处理规范**
  - [ ] 未静默吞掉异常（返回null）
  - [ ] 异常信息详细可诊断
  - [ ] 关键路径有日志记录

- [ ] **事务管理规范**
  - [ ] Service层写操作使用`@Transactional(rollbackFor = Exception.class)`
  - [ ] DAO层查询使用`@Transactional(readOnly = true)`
  - [ ] 事务边界清晰

---

## 🔍 PR审查检查清单

### 架构审查

- [ ] **服务边界检查**
  - [ ] 未创建新微服务
  - [ ] 功能已整合到现有服务
  - [ ] 服务职责边界清晰

- [ ] **调用模式检查**
  - [ ] 服务间调用通过GatewayServiceClient
  - [ ] 无Feign直连（白名单除外）
  - [ ] 无跨服务数据库访问

### 技术栈审查

- [ ] **版本一致性检查**
  - [ ] 依赖版本与标准规范一致
  - [ ] 未硬编码版本号
  - [ ] 文档引用技术栈标准规范

### 代码规范审查

- [ ] **依赖注入检查**
  - [ ] 0个@Autowired（测试类除外）
  - [ ] 100%使用@Resource

- [ ] **DAO层检查**
  - [ ] 0个@Repository
  - [ ] 100%使用@Mapper

- [ ] **包名检查**
  - [ ] 0个javax.*（除JDK自带）
  - [ ] 100%使用jakarta.*

- [ ] **HTTP方法检查**
  - [ ] 查询接口使用GET
  - [ ] 无POST查询接口

---

## 🚨 常见违规模式及修复

### 违规模式1: @Autowired使用

```java
// ❌ 错误
@Autowired
private UserDao userDao;

// ✅ 正确
@Resource
private UserDao userDao;
```

### 违规模式2: Repository命名

```java
// ❌ 错误
@Repository
public interface UserRepository extends BaseMapper<UserEntity> { }

// ✅ 正确
@Mapper
public interface UserDao extends BaseMapper<UserEntity> { }
```

### 违规模式3: POST查询接口

```java
// ❌ 错误
@PostMapping("/query")
public ResponseDTO<PageResult<UserVO>> queryUsers(@RequestBody UserQueryForm form)

// ✅ 正确
@GetMapping("/page")
public ResponseDTO<PageResult<UserVO>> page(
    @RequestParam(required = false) String keyword,
    @RequestParam(defaultValue = "1") Integer pageNum,
    @RequestParam(defaultValue = "10") Integer pageSize
)
```

### 违规模式4: Feign直连

```java
// ❌ 错误
@FeignClient(name = "ioedream-common-service")
public interface UserServiceClient { }

// ✅ 正确
@Resource
private GatewayServiceClient gatewayServiceClient;

public UserVO getUser(Long userId) {
    return gatewayServiceClient.callCommonService(
        "/api/v1/users/" + userId,
        HttpMethod.GET,
        null,
        UserVO.class
    );
}
```

### 违规模式5: 跨层访问

```java
// ❌ 错误 - Controller直接调用DAO
@RestController
public class UserController {
    @Resource
    private UserDao userDao;  // 禁止！
    
    @GetMapping("/{id}")
    public ResponseDTO<UserVO> getById(@PathVariable Long id) {
        return ResponseDTO.ok(userDao.selectById(id));  // 禁止！
    }
}

// ✅ 正确 - 通过Service层
@RestController
public class UserController {
    @Resource
    private UserService userService;
    
    @GetMapping("/{id}")
    public ResponseDTO<UserVO> getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}
```

---

## 📊 自动化检查命令

### PowerShell快速检查

```powershell
# 检查@Autowired违规
Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "^\s*@Autowired\b" |
    Where-Object { $_.Path -notmatch "Test\.java$" } |
    Select-Object Path, LineNumber, Line

# 检查Feign违规
Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "@EnableFeignClients|@FeignClient" |
    Select-Object Path, LineNumber, Line

# 检查Repository违规
Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "@Repository|Repository\s+extends" |
    Select-Object Path, LineNumber, Line

# 检查POST查询接口
Get-ChildItem -Path "microservices" -Recurse -Filter "*Controller.java" |
    Select-String -Pattern '@PostMapping\("/get|@PostMapping\("/query|@PostMapping\("/list' |
    Select-Object Path, LineNumber, Line
```

---

## ✅ 通过标准

### 代码提交前

- ✅ **所有P0级检查项通过**
- ✅ **无架构违规**
- ✅ **代码规范一致**

### PR合并前

- ✅ **架构审查通过**
- ✅ **技术栈审查通过**
- ✅ **代码规范审查通过**
- ✅ **自动化检查通过**

---

## 📚 参考文档

- [全局一致性优化路线图](./global-consistency-optimization-roadmap.md)
- [CLAUDE.md - 全局架构规范](../../CLAUDE.md)
- [技术栈标准规范](../../documentation/technical/PR_REVIEW_TECHNOLOGY_STACK_CHECKLIST.md)
- [Java编码规范](../../documentation/technical/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md)

---

**最后更新**: 2025-12-14  
**维护团队**: IOE-DREAM 架构委员会
