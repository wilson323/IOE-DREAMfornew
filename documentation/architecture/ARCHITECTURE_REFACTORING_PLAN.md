# IOE-DREAM 架构重构开发计划

> **版本**: v1.0.0
> **创建日期**: 2025-12-02
> **作者**: 架构师团队
> **状态**: 待执行
> **重点**: 后端架构优化，前端/移动端保持不变

---

## 📋 目录

1. [重构范围界定](#1-重构范围界定)
2. [重构目标](#2-重构目标)
3. [阶段一：公共库瘦身](#3-阶段一公共库瘦身)
4. [阶段二：Service层迁移](#4-阶段二service层迁移)
5. [阶段三：依赖优化](#5-阶段三依赖优化)
6. [阶段四：API契约统一](#6-阶段四api契约统一)
7. [阶段五：测试验证](#7-阶段五测试验证)
8. [开发规范详解](#8-开发规范详解)
9. [风险控制](#9-风险控制)
10. [验收标准](#10-验收标准)

---

## 1. 重构范围界定

### 1.1 重构范围（需要改动）

| 模块 | 改动类型 | 改动程度 | 说明 |
|------|---------|---------|------|
| **microservices-common** | 瘦身重构 | 🔴 大 | 移除@Service实现，保留Entity/DAO/DTO/Manager |
| **ioedream-common-service** | 功能承接 | 🔴 大 | 承接从common移出的Service实现 |
| **其他后端微服务** | 依赖调整 | 🟡 中 | 调整依赖和import路径 |
| **pom.xml** | 依赖优化 | 🟡 中 | 清理不必要的依赖 |

### 1.2 保持不变范围

| 模块 | 说明 |
|------|------|
| **smart-admin-web-javascript** | ✅ 前端Web应用保持不变 |
| **smart-app** | ✅ 移动端应用保持不变 |
| **microservices/frontend/web-main** | ✅ 微前端主应用保持不变 |
| **microservices/mobile/uni-app-mobile** | ✅ 移动端保持不变 |
| **API接口路径** | ✅ 保持现有API路径不变，确保前端兼容 |
| **数据库结构** | ✅ 数据库表结构保持不变 |

### 1.3 重构原则

1. **前端零改动**: 所有API路径、请求/响应格式保持不变
2. **渐进式重构**: 分阶段执行，每阶段可独立验证
3. **向后兼容**: 重构过程中保持系统可用
4. **充分测试**: 每个改动都要有对应的测试验证

---

## 2. 重构目标

### 2.1 核心目标

| 目标 | 当前状态 | 目标状态 | 验收标准 |
|------|---------|---------|---------|
| **公共库职责** | 包含13个@Service | 0个@Service | grep -r "@Service" 返回0 |
| **公共库依赖** | 依赖spring-boot-starter-web | 无Web依赖 | pom.xml中无web依赖 |
| **编译状态** | 有编译错误 | 0编译错误 | mvn compile 成功 |
| **API兼容性** | - | 100%兼容 | 前端无需任何改动 |

### 2.2 不改变的内容

- ❌ 不改变API接口路径
- ❌ 不改变请求/响应格式
- ❌ 不改变前端代码
- ❌ 不改变移动端代码
- ❌ 不改变数据库结构
- ❌ 不改变业务逻辑

---

## 3. 阶段一：公共库瘦身

### 3.1 任务清单

| 任务ID | 任务描述 | 预计工时 | 优先级 |
|--------|---------|---------|--------|
| T1-001 | 识别microservices-common中所有@Service类 | 1h | P0 |
| T1-002 | 在ioedream-common-service中创建对应Service接口 | 2h | P0 |
| T1-003 | 迁移Service实现到ioedream-common-service | 4h | P0 |
| T1-004 | 删除microservices-common中的@Service类 | 1h | P0 |
| T1-005 | 更新microservices-common的pom.xml | 1h | P0 |
| T1-006 | 验证编译和功能 | 2h | P0 |

### 3.2 需要迁移的Service列表

```
microservices-common中需要移除的@Service类:
├── audit/service/impl/AuditLogServiceImpl.java
├── config/service/impl/ConfigServiceImpl.java
├── device/service/impl/CommonDeviceServiceImpl.java
├── dict/service/impl/DictTypeServiceImpl.java
├── dict/service/impl/DictDataServiceImpl.java
├── file/service/impl/FileServiceImpl.java
├── menu/service/impl/MenuServiceImpl.java
├── organization/service/impl/DepartmentServiceImpl.java
├── security/service/impl/CommonRbacServiceImpl.java
└── workflow/service/impl/WorkflowEngineServiceImpl.java
```

### 3.3 开发规范

#### 3.3.1 Service迁移规范

```java
// ============================================================
// 迁移前 (microservices-common) - 需要删除
// ============================================================
package net.lab1024.sa.common.dict.service.impl;

@Service  // ❌ 公共库中禁止使用@Service
public class DictTypeServiceImpl implements DictTypeService {
    @Resource
    private DictTypeDao dictTypeDao;
    
    // 业务逻辑...
}

// ============================================================
// 迁移后 (ioedream-common-service) - 正确位置
// ============================================================
package net.lab1024.sa.common.dict.service.impl;

@Service  // ✅ 微服务中使用@Service
public class DictTypeServiceImpl implements DictTypeService {
    @Resource
    private DictTypeDao dictTypeDao;  // 从microservices-common引入
    
    // 业务逻辑保持不变
}
```

#### 3.3.2 包结构规范

```
迁移后的目录结构:

microservices-common (JAR库):
└── net.lab1024.sa.common/
    ├── dict/
    │   ├── entity/DictTypeEntity.java       ✅ 保留
    │   ├── dao/DictTypeDao.java             ✅ 保留
    │   ├── domain/form/DictTypeAddForm.java ✅ 保留
    │   ├── domain/vo/DictTypeVO.java        ✅ 保留
    │   ├── manager/DictTypeManager.java     ✅ 保留
    │   └── service/DictTypeService.java     ✅ 保留(接口)
    │   └── service/impl/                    ❌ 删除整个impl目录

ioedream-common-service (微服务):
└── net.lab1024.sa.common/
    ├── dict/
    │   ├── controller/DictController.java   ✅ 保留
    │   └── service/impl/                    ✅ 新增/迁移
    │       └── DictTypeServiceImpl.java     ✅ 从common迁移
```

#### 3.3.3 Import路径规范

```java
// ioedream-common-service中的Service实现
package net.lab1024.sa.common.dict.service.impl;

// ✅ 从microservices-common引入
import net.lab1024.sa.common.dict.dao.DictTypeDao;
import net.lab1024.sa.common.dict.entity.DictTypeEntity;
import net.lab1024.sa.common.dict.domain.form.DictTypeAddForm;
import net.lab1024.sa.common.dict.domain.vo.DictTypeVO;
import net.lab1024.sa.common.dict.manager.DictTypeManager;
import net.lab1024.sa.common.dict.service.DictTypeService;

// ✅ Spring注解
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class DictTypeServiceImpl implements DictTypeService {
    // 实现...
}
```

---

## 4. 阶段二：Service层迁移

### 4.1 迁移步骤详解

#### 步骤1: 创建目标目录

```powershell
# 在ioedream-common-service中创建目录结构
$basePath = "microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common"

$modules = @(
    "audit/service/impl",
    "config/service/impl",
    "device/service/impl",
    "dict/service/impl",
    "file/service/impl",
    "menu/service/impl",
    "organization/service/impl",
    "security/service/impl",
    "workflow/service/impl"
)

foreach ($module in $modules) {
    $path = "$basePath/$module"
    if (!(Test-Path $path)) {
        New-Item -ItemType Directory -Path $path -Force
    }
}
```

#### 步骤2: 逐个迁移Service

**迁移顺序（按依赖关系）**:
1. `CommonRbacServiceImpl` - 基础权限服务
2. `DictTypeServiceImpl`, `DictDataServiceImpl` - 字典服务
3. `ConfigServiceImpl` - 配置服务
4. `MenuServiceImpl` - 菜单服务
5. `DepartmentServiceImpl` - 部门服务
6. `FileServiceImpl` - 文件服务
7. `AuditLogServiceImpl` - 审计服务
8. `CommonDeviceServiceImpl` - 设备服务
9. `WorkflowEngineServiceImpl` - 工作流服务

#### 步骤3: 每个Service的迁移检查清单

```markdown
## Service迁移检查清单

### 迁移前检查
- [ ] 确认Service接口在microservices-common中
- [ ] 确认DAO/Entity/Form/VO在microservices-common中
- [ ] 确认Manager在microservices-common中
- [ ] 记录Service的所有依赖

### 迁移执行
- [ ] 在ioedream-common-service中创建ServiceImpl
- [ ] 复制原有实现代码
- [ ] 调整import路径
- [ ] 添加@Service注解
- [ ] 确保@Resource注入正确

### 迁移后验证
- [ ] 编译通过
- [ ] 单元测试通过
- [ ] Controller调用正常
- [ ] API接口响应正确
```

### 4.2 开发规范

#### 4.2.1 Service实现规范

```java
/**
 * 字典类型服务实现
 * 
 * @author IOE-DREAM Team
 * @since 2025-12-02
 * @version 1.0.0
 * 
 * 开发规范:
 * 1. 必须使用@Service注解
 * 2. 必须使用@Resource进行依赖注入
 * 3. 必须使用@Transactional管理事务
 * 4. 必须有完整的JavaDoc注释
 * 5. 方法不超过50行
 * 6. 类不超过400行
 */
@Service
@Slf4j
public class DictTypeServiceImpl implements DictTypeService {

    @Resource
    private DictTypeDao dictTypeDao;
    
    @Resource
    private DictTypeManager dictTypeManager;

    /**
     * 新增字典类型
     * 
     * @param form 新增表单
     * @return 新增结果
     * 
     * 事务规范: 写操作必须使用@Transactional
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> add(DictTypeAddForm form) {
        // 1. 参数校验
        // 2. 业务逻辑
        // 3. 数据持久化
        // 4. 返回结果
    }

    /**
     * 查询字典类型列表
     * 
     * @param queryForm 查询条件
     * @return 分页结果
     * 
     * 事务规范: 只读操作使用readOnly=true
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<DictTypeVO>> queryPage(DictTypeQueryForm queryForm) {
        // 查询逻辑
    }
}
```

#### 4.2.2 事务管理规范

```java
// ✅ 正确: 写操作使用事务
@Transactional(rollbackFor = Exception.class)
public ResponseDTO<Long> add(XxxAddForm form) { }

// ✅ 正确: 更新操作使用事务
@Transactional(rollbackFor = Exception.class)
public ResponseDTO<Void> update(XxxUpdateForm form) { }

// ✅ 正确: 删除操作使用事务
@Transactional(rollbackFor = Exception.class)
public ResponseDTO<Void> delete(Long id) { }

// ✅ 正确: 只读操作使用readOnly
@Transactional(readOnly = true)
public ResponseDTO<XxxVO> getById(Long id) { }

// ✅ 正确: 批量操作使用事务
@Transactional(rollbackFor = Exception.class)
public ResponseDTO<Void> batchDelete(List<Long> ids) { }

// ❌ 错误: 查询操作不应该使用写事务
@Transactional(rollbackFor = Exception.class)  // 应该用readOnly=true
public ResponseDTO<List<XxxVO>> list() { }
```

---

## 5. 阶段三：依赖优化

### 5.1 microservices-common的pom.xml优化

```xml
<!-- 优化前 (需要移除的依赖) -->
<dependencies>
    <!-- ❌ 移除: 公共库不应该依赖Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- ❌ 移除: 公共库不应该依赖Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>

<!-- 优化后 (保留的依赖) -->
<dependencies>
    <!-- ✅ 保留: 数据访问 -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>
    
    <!-- ✅ 保留: 数据库连接池 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- ✅ 保留: 工具库 -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
    </dependency>
    
    <!-- ✅ 保留: Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    
    <!-- ✅ 保留: 验证 -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
    </dependency>
</dependencies>
```

### 5.2 依赖检查规范

```powershell
# 检查公共库是否包含不应该有的依赖
cd microservices/microservices-common
mvn dependency:tree | Select-String "spring-boot-starter-web"
mvn dependency:tree | Select-String "spring-boot-starter-security"

# 期望结果: 无输出
```

---

## 6. 阶段四：API契约统一

### 6.1 API兼容性保证

**核心原则**: 所有API路径、请求格式、响应格式保持不变

```java
// API路径保持不变
@RestController
@RequestMapping("/api/v1/dict")  // ✅ 路径不变
public class DictController {

    @GetMapping("/type/list")     // ✅ 路径不变
    public ResponseDTO<List<DictTypeVO>> listTypes() {
        // 实现
    }
}

// 响应格式保持不变
public class ResponseDTO<T> {
    private Integer code;      // ✅ 字段不变
    private String message;    // ✅ 字段不变
    private T data;            // ✅ 字段不变
}
```

### 6.2 前端兼容性验证

```javascript
// 前端API调用示例 (保持不变)
// smart-admin-web-javascript/src/api/support/dict-api.js

export const dictApi = {
  // ✅ 这些调用保持不变
  getTypeList() {
    return request.get('/api/v1/dict/type/list');
  },
  
  getDataList(typeCode) {
    return request.get('/api/v1/dict/data/list', { params: { typeCode } });
  }
};
```

---

## 7. 阶段五：测试验证

### 7.1 测试清单

| 测试类型 | 测试内容 | 验收标准 |
|---------|---------|---------|
| **编译测试** | 全项目编译 | mvn compile 成功 |
| **单元测试** | Service层测试 | 覆盖率≥80% |
| **集成测试** | Controller层测试 | 所有API可访问 |
| **兼容性测试** | 前端功能测试 | 前端无需改动即可正常使用 |
| **回归测试** | 全功能回归 | 所有业务功能正常 |

### 7.2 测试命令

```powershell
# 1. 编译测试
cd D:\IOE-DREAM\microservices
mvn clean compile -DskipTests

# 2. 单元测试
mvn test

# 3. 集成测试
mvn verify

# 4. 启动服务验证
cd ioedream-common-service
mvn spring-boot:run
```

### 7.3 前端兼容性验证步骤

```markdown
## 前端兼容性验证步骤

1. 启动后端服务
   - 启动 ioedream-gateway (8080)
   - 启动 ioedream-common-service (8088)

2. 启动前端服务
   - cd smart-admin-web-javascript
   - npm run localhost

3. 验证功能
   - [ ] 登录功能正常
   - [ ] 字典管理正常
   - [ ] 菜单管理正常
   - [ ] 用户管理正常
   - [ ] 权限管理正常
   - [ ] 文件上传正常
   - [ ] 审计日志正常

4. 验证标准
   - 前端代码无任何改动
   - 所有API调用正常
   - 业务功能正常
```

---

## 8. 开发规范详解

### 8.1 代码风格规范

#### 8.1.1 命名规范

```java
// ✅ 类名: PascalCase
public class UserServiceImpl {}
public class DictTypeEntity {}

// ✅ 方法名: camelCase
public void getUserById(Long id) {}
public List<UserVO> listUsers() {}

// ✅ 常量: UPPER_SNAKE_CASE
public static final String DEFAULT_PASSWORD = "123456";
public static final int MAX_RETRY_COUNT = 3;

// ✅ 变量: camelCase
private Long userId;
private String userName;

// ❌ 错误示例
public class user_service_impl {}  // 类名错误
public void GetUserById() {}       // 方法名错误
private Long user_id;              // 变量名错误
```

#### 8.1.2 注释规范

```java
/**
 * 用户服务实现类
 * 
 * <p>提供用户相关的业务操作，包括：</p>
 * <ul>
 *   <li>用户增删改查</li>
 *   <li>用户状态管理</li>
 *   <li>用户权限分配</li>
 * </ul>
 * 
 * @author IOE-DREAM Team
 * @since 2025-12-02
 * @version 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {

    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID，不能为空
     * @return 用户信息，不存在返回null
     * @throws IllegalArgumentException 当id为null时抛出
     */
    @Override
    public UserVO getById(Long id) {
        // 实现
    }
}
```

#### 8.1.3 异常处理规范

```java
/**
 * 异常处理规范示例
 */
@Service
public class UserServiceImpl implements UserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> add(UserAddForm form) {
        // ✅ 正确: 业务异常使用ResponseDTO返回
        if (userDao.existsByUsername(form.getUsername())) {
            return ResponseDTO.error("USER_EXISTS", "用户名已存在");
        }
        
        try {
            // 业务逻辑
            UserEntity entity = convertToEntity(form);
            userDao.insert(entity);
            return ResponseDTO.ok(entity.getId());
        } catch (DuplicateKeyException e) {
            // ✅ 正确: 捕获特定异常
            log.error("用户新增失败，用户名重复: {}", form.getUsername(), e);
            return ResponseDTO.error("DUPLICATE_KEY", "数据重复");
        } catch (Exception e) {
            // ✅ 正确: 记录日志后抛出
            log.error("用户新增失败: {}", form, e);
            throw new BusinessException("用户新增失败", e);
        }
    }
}
```

### 8.2 数据库操作规范

#### 8.2.1 DAO层规范

```java
/**
 * 用户数据访问接口
 * 
 * 规范要求:
 * 1. 必须使用@Mapper注解
 * 2. 必须继承BaseMapper
 * 3. 禁止使用@Repository
 * 4. 自定义方法使用LambdaQueryWrapper
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查询
     * 
     * @param username 用户名
     * @return 用户实体
     */
    default UserEntity selectByUsername(String username) {
        return selectOne(new LambdaQueryWrapper<UserEntity>()
            .eq(UserEntity::getUsername, username)
            .eq(UserEntity::getDeletedFlag, 0));
    }

    /**
     * 分页查询
     * 
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default IPage<UserEntity> selectPageByCondition(
            IPage<UserEntity> page, 
            LambdaQueryWrapper<UserEntity> queryWrapper) {
        return selectPage(page, queryWrapper);
    }
}
```

#### 8.2.2 查询优化规范

```java
// ✅ 正确: 使用LambdaQueryWrapper
LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(UserEntity::getStatus, 1)
       .like(StringUtils.isNotBlank(name), UserEntity::getName, name)
       .orderByDesc(UserEntity::getCreateTime);

// ❌ 错误: 使用字符串字段名
QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
wrapper.eq("status", 1)  // 字符串容易写错
       .like("name", name);

// ✅ 正确: 分页查询
IPage<UserEntity> page = new Page<>(pageNum, pageSize);
IPage<UserEntity> result = userDao.selectPage(page, wrapper);

// ❌ 错误: 查询全部后内存分页
List<UserEntity> all = userDao.selectList(wrapper);
List<UserEntity> pageList = all.subList(start, end);  // 性能问题
```

### 8.3 日志规范

```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Override
    public ResponseDTO<UserVO> getById(Long id) {
        // ✅ 正确: 使用占位符
        log.info("查询用户, id={}", id);
        
        // ❌ 错误: 字符串拼接
        log.info("查询用户, id=" + id);
        
        // ✅ 正确: 异常日志
        try {
            // 业务逻辑
        } catch (Exception e) {
            log.error("查询用户失败, id={}", id, e);  // 异常对象放最后
            throw e;
        }
        
        // ✅ 正确: 条件日志
        if (log.isDebugEnabled()) {
            log.debug("用户详情: {}", JSON.toJSONString(user));
        }
    }
}
```

---

## 9. 风险控制

### 9.1 风险清单

| 风险ID | 风险描述 | 影响程度 | 发生概率 | 缓解措施 |
|--------|---------|---------|---------|---------|
| R-001 | 迁移过程中服务不可用 | 高 | 中 | 分阶段迁移，保持双版本 |
| R-002 | API兼容性问题 | 高 | 低 | 严格保持API不变 |
| R-003 | 依赖冲突 | 中 | 中 | 充分测试依赖树 |
| R-004 | 事务边界问题 | 高 | 低 | 完善事务配置 |

### 9.2 回滚方案

```powershell
# 每个阶段开始前创建Git标签
git tag -a "pre-refactor-phase1" -m "重构阶段1开始前"

# 如需回滚
git checkout pre-refactor-phase1
```

---

## 10. 验收标准

### 10.1 技术验收标准

| 验收项 | 验收标准 | 验收方法 |
|--------|---------|---------|
| 编译 | 0错误0警告 | mvn compile |
| 测试 | 覆盖率≥80% | mvn test |
| 代码规范 | 0违规 | checkstyle检查 |
| API兼容 | 100%兼容 | 前端功能测试 |
| 性能 | 无下降 | 性能测试对比 |

### 10.2 业务验收标准

| 验收项 | 验收标准 |
|--------|---------|
| 用户登录 | 正常登录、退出 |
| 字典管理 | 增删改查正常 |
| 菜单管理 | 增删改查正常 |
| 权限管理 | 角色、权限分配正常 |
| 文件管理 | 上传、下载正常 |
| 审计日志 | 记录、查询正常 |

---

## 附录

### A. 执行时间表

| 阶段 | 任务 | 预计工时 | 开始时间 | 结束时间 |
|------|------|---------|---------|---------|
| 阶段一 | 公共库瘦身 | 11h | Day 1 | Day 2 |
| 阶段二 | Service迁移 | 16h | Day 2 | Day 4 |
| 阶段三 | 依赖优化 | 4h | Day 4 | Day 4 |
| 阶段四 | API契约统一 | 4h | Day 5 | Day 5 |
| 阶段五 | 测试验证 | 8h | Day 5 | Day 6 |

### B. 相关文档

- [最优架构设计说明书](./OPTIMAL_ARCHITECTURE_DESIGN.md)
- [CLAUDE.md](../../CLAUDE.md)

---

**文档维护**: 架构师团队
**最后更新**: 2025-12-02

