# IOE-DREAM 四层架构守护检查清单

**版本**: v1.0
**维护者**: 四层架构守护专家
**更新时间**: 2025-12-20

## 🎯 使用指南

本检查清单适用于：
- ✅ 代码提交前自检
- ✅ 代码审查时使用
- ✅ 架构合规性验证
- ✅ 新人培训参考

**评分标准**:
- 🟢 完全合规 (100分)
- 🟡 基本合规 (80-99分)
- 🔴 需要改进 (60-79分)
- ❌ 严重违规 (<60分)

---

## 📋 四层架构规范

### 架构层级定义
```
Controller层 (接口控制)
    ↓ 严格调用
Service层 (业务逻辑 + 事务管理)
    ↓ 严格调用
Manager层 (复杂业务 + 缓存管理)
    ↓ 严格调用
Repository/DAO层 (数据访问)
```

### 各层职责
| 层级 | 核心职责 | 禁止事项 | 关键注解 |
|------|----------|----------|----------|
| **Controller** | HTTP请求处理、参数验证、响应封装 | 业务逻辑、事务管理、数据库访问 | @RestController、@RequestMapping |
| **Service** | 业务逻辑实现、事务管理、事务边界 | 直接数据库访问、跨层调用 | @Service、@Transactional |
| **Manager** | 复杂流程编排、缓存管理、跨模块协调 | 事务管理、数据库操作、Spring注解 | 纯Java类、无注解 |
| **DAO** | 数据库CRUD、复杂SQL、数据映射 | 业务逻辑、事务处理 | @Mapper、BaseMapper<Entity> |

---

## 🔍 代码提交前检查清单

### 1. Controller层检查

- [ ] **依赖注入正确**
  ```java
  // ✅ 正确：只注入Service层
  @Resource
  private UserService userService;

  // ❌ 错误：注入Manager或DAO
  @Resource
  private UserManager userManager;  // 禁止！
  ```

- [ ] **无业务逻辑**
  ```java
  // ✅ 正确：只做参数校验和调用
  @PostMapping("/add")
  public ResponseDTO<String> addUser(@Valid @RequestBody UserAddForm form) {
      return ResponseDTO.ok(userService.addUser(form));
  }

  // ❌ 错误：包含业务逻辑
  if (form.getAge() < 18) {
      throw new BusinessException("年龄不符合要求");  // 禁止业务逻辑
  }
  ```

- [ ] **事务管理正确**
  ```java
  // ❌ 错误：Controller层不应有事务
  @Transactional  // 禁止！
  public ResponseDTO<String> addUser(...) { ... }
  ```

### 2. Service层检查

- [ ] **依赖注入正确**
  ```java
  // ✅ 正确：注入Manager和DAO
  @Resource
  private UserManager userManager;
  @Resource
  private UserDao userDao;
  ```

- [ ] **事务边界清晰**
  ```java
  // ✅ 正确：Service层管理事务
  @Service
  @Transactional(rollbackFor = Exception.class)
  public class UserServiceImpl implements UserService {

      @Override
      @Transactional(rollbackFor = Exception.class)
      public Long addUser(UserAddForm form) {
          // 业务逻辑
          return userManager.createUser(form);
      }
  }
  ```

- [ ] **无直接数据库访问**
  ```java
  // ❌ 错误：Service层不应直接使用SQL注解
  @Select("SELECT * FROM user WHERE id = #{id}")  // 禁止！
  UserEntity selectById(Long id);
  ```

### 3. Manager层检查

- [ ] **无Spring注解**
  ```java
  // ❌ 错误：Manager层不应有Spring注解
  @Component      // 禁止！
  @Transactional   // 禁止！
  @Service        // 禁止！
  public class UserManager { ... }

  // ✅ 正确：纯Java类
  public class UserManager {
      private final UserDao userDao;

      // 构造函数注入
      public UserManager(UserDao userDao) {
          this.userDao = userDao;
      }
  }
  ```

- [ ] **包结构正确**
  ```
  ✅ 正确路径:
  manager/
    impl/
      UserManagerImpl.java

  ❌ 错误路径:
  service/
    impl/
      UserManagerImpl.java  // 禁止！
  ```

- [ ] **职责边界清晰**
  ```java
  // ✅ 正确：复杂业务逻辑、缓存管理
  public UserVO getUserWithCache(Long userId) {
      // 多级缓存查询
      // 复杂业务组装
      // 跨模块协调
  }
  ```

### 4. DAO层检查

- [ ] **注解使用正确**
  ```java
  // ✅ 正确：使用@Mapper注解
  @Mapper
  public interface UserDao extends BaseMapper<UserEntity> {
      @Transactional(readOnly = true)
      UserEntity selectByUsername(@Param("username") String username);
  }

  // ❌ 错误：使用@Repository注解
  @Repository  // 禁止！
  public interface UserDao extends BaseMapper<UserEntity> { ... }
  ```

- [ ] **命名规范正确**
  ```
  ✅ 正确命名:
  - UserDao
  - DepartmentDao
  - AccessRecordDao

  ❌ 错误命名:
  - UserRepository  // 禁止！
  - UserJpaRepository  // 禁止！
  ```

- [ ] **继承关系正确**
  ```java
  // ✅ 正确：继承BaseMapper
  public interface UserDao extends BaseMapper<UserEntity> { ... }

  // ❌ 错误：继承JpaRepository
  public interface UserDao extends JpaRepository<UserEntity, Long> { ... }  // 禁止！
  ```

---

## 🔧 依赖注入规范检查

### 统一使用@Resource
```java
// ✅ 正确：统一使用@Resource
@Resource
private UserService userService;
@Resource
private UserManager userManager;

// ❌ 错误：禁止使用@Autowired
@Autowired  // 禁止！
private UserService userService;

// ❌ 错误：禁止构造函数注入（除非是Manager类）
public UserServiceImpl(UserManager userManager) {  // 禁止！
    this.userManager = userManager;
}
```

### Manager类特殊处理
```java
// ✅ 正确：Manager类使用构造函数注入
public class UserManager {
    private final UserDao userDao;

    // Manager类使用构造函数注入（纯Java类）
    public UserManager(UserDao userDao) {
        this.userDao = userDao;
    }
}

// ✅ 正确：在配置类中注册Manager Bean
@Configuration
public class ManagerConfiguration {
    @Bean
    public UserManager userManager(UserDao userDao) {
        return new UserManager(userDao);
    }
}
```

---

## 📦 包结构规范检查

### 标准包结构
```
net.lab1024.sa.{module}/
├── controller/           # Controller层
│   └── ModuleController.java
├── service/              # Service层
│   ├── ModuleService.java
│   └── impl/
│       └── ModuleServiceImpl.java
├── manager/              # Manager层
│   ├── ModuleManager.java
│   └── impl/
│       └── ModuleManagerImpl.java
├── dao/                  # DAO层
│   └── ModuleDao.java
└── domain/               # 领域对象
    ├── form/             # 请求表单
    └── vo/               # 响应视图
```

### 包命名规范
```
✅ 正确包名:
- controller
- service
- service.impl
- manager
- manager.impl
- dao
- domain.form
- domain.vo

❌ 错误包名:
- repository
- repository.impl
- manager.service  // 混乱命名
- dao.impl         // DAO是接口，不需要impl
```

---

## 🚫 禁止事项检查清单

### 严格禁止的架构违规
- [ ] **禁止跨层访问**: Controller直接调用Manager/DAO
- [ ] **禁止层内包含**: Manager层包含事务管理
- [ ] **禁止错误注解**: DAO使用@Repository
- [ ] **禁止错误依赖**: 使用@Autowired
- [ ] **禁止错误包名**: Manager放在service包下

### 代码示例
```java
// ❌ 严重违规示例
@Controller
public class UserController {
    @Autowired  // 违规1：使用@Autowired
    private UserDao userDao;  // 违规2：跨层访问

    @Transactional  // 违规3：Controller层事务
    public void addUser() {
        userDao.insert(user);  // 违规4：直接数据库操作
    }
}

@Repository  // 违规5：错误注解
public interface UserDao {
    @Select("...")  // 违规6：DAO层包含业务逻辑
}
```

---

## 🔍 快速检查命令

### 自动化检查
```bash
# 运行架构合规性检查
./scripts/architecture-violations-fix.sh

# 自动修复常见问题
./scripts/auto-fix-architecture-violations.sh

# 检查特定违规
grep -r "@Autowired" microservices/ --include="*.java"
grep -r "@Repository" microservices/ --include="*.java"
find microservices/ -name "*Manager*.java" -path "*/service/*"
```

### IDE检查配置
```json
// .vscode/settings.json
{
    "java.checkstyle.configuration": "checkstyle.xml",
    "editor.codeActionsOnSave": {
        "source.organizeImports": true
    }
}
```

---

## 📊 合规性评分

### 评分标准
| 检查项 | 分值 | 得分 | 备注 |
|--------|------|------|------|
| 四层架构调用链 | 25分 | | Controller→Service→Manager→DAO |
| 依赖注入规范 | 20分 | | 统一使用@Resource |
| DAO层规范 | 20分 | | @Mapper+BaseMapper |
| 包结构规范 | 15分 | | 正确的包路径 |
| 注解使用规范 | 10分 | | 无违规注解 |
| 事务边界清晰 | 10分 | | Service层管理事务 |
| **总分** | **100分** | | |

### 评级标准
- 🟢 优秀 (90-100分): 完全合规，可作为示例
- 🟡 良好 (80-89分): 基本合规，有少量问题
- 🔴 中等 (60-79分): 需要改进，有架构违规
- ❌ 不合格 (<60分): 严重违规，必须重构

---

## 🎯 持续改进建议

### 团队实践
1. **代码审查**: 将架构合规性作为PR的必查项
2. **定期培训**: 每月进行四层架构规范培训
3. **示例代码**: 维护合规的示例代码库
4. **工具集成**: 将检查脚本集成到CI/CD

### 个人实践
1. **提交前检查**: 使用本清单进行自检
2. **学习规范**: 深入理解CLAUDE.md规范
3. **参考示例**: 查看合规的代码示例
4. **主动改进**: 发现问题及时修复

---

## 📞 支持与反馈

**架构专家**: 四层架构守护专家
**问题反馈**: 通过项目Issue提交
**定期咨询**: 每周架构审查会议

**让我们一起维护IOE-DREAM的架构质量！** 🚀

---

*最后更新: 2025-12-20*
*版本: v1.0*
*下次更新: 根据项目发展需要*