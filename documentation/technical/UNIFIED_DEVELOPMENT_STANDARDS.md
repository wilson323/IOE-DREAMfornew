# IOE-DREAM 统一开发标准

**版本**: v1.0.0  
**生效日期**: 2025-12-20  
**适用范围**: IOE-DREAM项目所有开发活动  
**规范优先级**: 项目统一开发标准，所有开发必须严格遵循

---

## 📋 核心开发原则

### 1. 文档先行原则
- ✅ **所有新功能开发前必须先完善相关文档**
- ✅ **代码变更必须同步更新文档**
- ✅ **API变更必须先更新接口文档**
- ✅ **配置变更必须更新部署文档**

### 2. 四层架构原则
- ✅ **严格遵循 Controller → Service → Manager → DAO 四层架构**
- ❌ **禁止跨层调用**
- ❌ **禁止在Controller中处理业务逻辑**
- ❌ **禁止在DAO中包含业务逻辑**

### 3. 代码质量原则
- ✅ **单元测试覆盖率 ≥ 80%**
- ✅ **核心业务覆盖率 = 100%**
- ✅ **圈复杂度 ≤ 10**
- ✅ **方法行数 ≤ 50**
- ✅ **类行数 ≤ 500**

---

## 🔧 技术规范

### 1. 依赖注入规范
```java
// ✅ 正确：使用@Resource
@Resource
private UserService userService;

// ❌ 错误：禁止使用@Autowired
@Autowired
private UserService userService;
```

### 2. 数据访问层规范
```java
// ✅ 正确：使用Dao后缀和@Mapper注解
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    // 数据访问方法
}

// ❌ 错误：禁止使用Repository
@Repository
public interface UserRepository extends BaseMapper<UserEntity> {
    // 禁止使用
}
```

### 3. 事务管理规范
```java
// ✅ Service层事务管理
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {
    // 业务逻辑
}

// ✅ DAO层查询事务
@Transactional(readOnly = true)
UserEntity selectByUserId(@Param("userId") Long userId);
```

---

## 📝 编码规范

### 1. 命名规范
- **类名**: 使用PascalCase，如`UserService`
- **方法名**: 使用camelCase，如`getUserById`
- **变量名**: 使用camelCase，如`userId`
- **常量名**: 使用UPPER_SNAKE_CASE，如`MAX_RETRY_COUNT`

### 2. 注释规范
```java
/**
 * 用户服务实现类
 * 
 * @author IOE-DREAM Team
 * @since 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     * @throws BusinessException 业务异常
     */
    public UserEntity getUserById(Long userId) {
        // 实现逻辑
    }
}
```

### 3. 异常处理规范
```java
// ✅ 统一异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }
}
```

---

## 🧪 测试规范

### 1. 单元测试规范
```java
@SpringBootTest
class UserServiceTest {
    
    @Resource
    private UserService userService;
    
    @Test
    void testGetUserById() {
        // Given
        Long userId = 1L;
        
        // When
        UserEntity user = userService.getUserById(userId);
        
        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userId);
    }
}
```

### 2. 集成测试规范
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testGetUser() {
        // 集成测试逻辑
    }
}
```

---

## 📊 质量门禁

### 1. 代码提交前检查
- [ ] 代码格式化检查通过
- [ ] 单元测试全部通过
- [ ] 代码覆盖率达标
- [ ] 静态代码分析通过
- [ ] 文档更新完成

### 2. Pull Request检查
- [ ] 代码审查通过
- [ ] 集成测试通过
- [ ] 性能测试通过
- [ ] 安全扫描通过
- [ ] 文档审查通过

---

## 🔗 相关文档

- [CLAUDE.md - 全局架构规范](../../CLAUDE.md)
- [四层架构详解](./repowiki/zh/content/后端架构/四层架构详解/四层架构详解.md)
- [Java编码规范](./repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md)
- [Vue3开发规范](./repowiki/zh/content/开发规范体系/Vue3开发规范.md)

---

**重要提醒**: 本文档是IOE-DREAM项目的统一开发标准，所有开发人员必须严格遵循。如有疑问，请联系架构师团队。