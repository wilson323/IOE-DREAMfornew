# 📋 IOE-DREAM SLF4J 统一使用规范

> **版本**: v2.0.0 - 统一版本
> **生效日期**: 2025-12-21
> **适用范围**: IOE-DREAM所有微服务和模块
> **规范级别**: 🔴 **强制执行**
> **制定人**: IOE-DREAM架构委员会
> **最后更新**: 2025-01-30（企业级统一计划实施）

---

## 🎯 核心原则

### 1. 强制统一原则
- ✅ **统一使用 `@Slf4j` 注解**
- ❌ **禁止使用 `LoggerFactory.getLogger()`**
- ❌ **禁止混合使用两种方式**

### 2. 代码质量原则
- ✅ **使用参数化日志**（避免字符串拼接）
- ✅ **统一日志格式**（模块标识 + 操作描述）
- ✅ **合理日志级别**（ERROR/WARN/INFO/DEBUG/TRACE）

---

## 📝 标准使用模式

### ✅ 正确示例

#### 基础使用
```java
@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/{id}")
    public ResponseDTO<UserVO> getUser(@PathVariable Long id) {
        log.info("[用户管理] 查询用户信息: userId={}", id);

        UserEntity user = userService.getById(id);
        if (user == null) {
            log.warn("[用户管理] 用户不存在: userId={}", id);
            return ResponseDTO.error("USER_NOT_FOUND", "用户不存在");
        }

        log.info("[用户管理] 查询成功: userId={}, username={}", id, user.getUsername());
        return ResponseDTO.ok(convertToVO(user));
    }

    @PostMapping
    public ResponseDTO<Void> createUser(@Valid @RequestBody UserCreateForm form) {
        try {
            log.info("[用户管理] 创建用户: username={}, email={}", form.getUsername(), form.getEmail());

            userService.create(form);

            log.info("[用户管理] 创建成功: userId={}", form.getUserId());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[用户管理] 创建失败: username={}, error={}", form.getUsername(), e.getMessage(), e);
            throw new BusinessException("USER_CREATE_ERROR", "创建用户失败");
        }
    }
}
```

#### Service层使用
```java
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;
    @Resource
    private UserManager userManager;

    @Override
    public UserEntity getById(Long userId) {
        log.debug("[用户服务] 查询用户开始: userId={}", userId);

        UserEntity user = userDao.selectById(userId);

        log.debug("[用户服务] 查询用户结束: userId={}, result={}", userId, user != null ? "found" : "not found");
        return user;
    }

    @Override
    @Transactional
    public void create(UserCreateForm form) {
        log.info("[用户服务] 创建用户开始: username={}", form.getUsername());

        // 业务逻辑
        UserEntity user = convertToEntity(form);
        userDao.insert(user);

        log.info("[用户服务] 创建用户成功: userId={}, username={}", user.getId(), user.getUsername());
    }
}
```

#### Manager层使用
```java
@Slf4j
public class UserManager {

    private final UserDao userDao;
    private final DepartmentDao departmentDao;

    public UserManager(UserDao userDao, DepartmentDao departmentDao) {
        this.userDao = userDao;
        this.departmentDao = departmentDao;
    }

    public UserVO getUserWithDepartment(Long userId) {
        log.debug("[用户管理器] 获取用户及部门信息: userId={}", userId);

        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            log.warn("[用户管理器] 用户不存在: userId={}", userId);
            return null;
        }

        DepartmentEntity department = departmentDao.selectById(user.getDepartmentId());

        UserVO userVO = UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .departmentName(department != null ? department.getName() : null)
            .build();

        log.debug("[用户管理器] 获取用户及部门信息成功: userId={}, department={}", userId,
                 department != null ? department.getName() : "无部门");
        return userVO;
    }
}
```

### ❌ 错误示例

#### 格式错误（已修复但仍需注意）
```java
// ❌ 错误 - getLogger后面有空格（已修复，但需要继续监控）
private static final Logger log = LoggerFactory.getLogger (UserService.class);

// ❌ 错误 - 混合使用
@Slf4j
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class); // 冗余
}
```

#### 日志内容错误
```java
// ❌ 错误 - 字符串拼接
log.info("用户信息: " + user.getUsername() + ", 邮箱: " + user.getEmail());

// ❌ 错误 - 缺少模块标识
log.info("查询用户: userId={}", userId); // 应该加上[模块名]

// ❌ 错误 - 日志级别使用不当
log.info("用户不存在: userId={}", userId); // 应该使用warn
```

---

## 🎨 日志格式标准

### 统一格式模板
```
log.[级别]("[模块名] 操作描述: 参数1={}, 参数2={}", value1, value2);
```

### 模块名映射表
| 微服务 | 模块名前缀 | 示例 |
|--------|-----------|------|
| access-service | `[门禁管理]` | `log.info("[门禁管理] 设备离线: deviceId={}", deviceId)` |
| attendance-service | `[考勤管理]` | `log.info("[考勤管理] 打卡成功: userId={}", userId)` |
| consume-service | `[消费管理]` | `log.info("[消费管理] 支付成功: amount={}", amount)` |
| oa-service | `[OA办公]` | `log.info("[OA办公] 流程启动: processId={}", processId)` |
| video-service | `[视频监控]` | `log.info("[视频监控] 设备上线: deviceId={}", deviceId)` |
| visitor-service | `[访客管理]` | `log.info("[访客管理] 预约成功: visitorId={}", visitorId)` |
| common模块 | `[公共模块]` | `log.info("[公共模块] 缓存刷新完成")` |

### 日志级别使用规范

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| **ERROR** | 系统错误、异常处理 | `log.error("[模块名] 系统异常: error={}", e.getMessage(), e)` |
| **WARN** | 警告信息、业务异常 | `log.warn("[模块名] 用户无权限: userId={}", userId)` |
| **INFO** | 业务关键节点、重要操作 | `log.info("[模块名] 用户登录成功: userId={}", userId)` |
| **DEBUG** | 调试信息、详细流程 | `log.debug("[模块名] 方法调用开始: method={}, params={}", method, params)` |
| **TRACE** | 详细追踪、循环迭代 | `log.trace("[模块名] 处理第{}条记录", index)` |

---

## 🔧 IDE配置规范

### IDEA Live Template配置
```
模板名称: slf4j
模板内容:
@Slf4j
public class $CLASS_NAME$ {
    $END$
}
```

### 代码检查规则
1. **禁止LoggerFactory使用检查**
2. **强制@Slf4j注解检查**
3. **日志格式检查**
4. **日志级别使用检查**

---

## 📊 质量检查清单

### 代码提交前检查
- [ ] 使用@Slf4j注解而非LoggerFactory
- [ ] 日志格式符合标准：`[模块名] 操作描述: 参数={} `
- [ ] 使用参数化日志，无字符串拼接
- [ ] 日志级别使用合理
- [ ] 异常日志包含完整堆栈信息

### Code Review检查
- [ ] 无LoggerFactory违规使用
- [ ] 日志内容有意义，便于问题排查
- [ ] 敏感信息已脱敏处理
- [ ] 日志级别使用正确

### CI/CD自动检查
- [ ] 扫描LoggerFactory关键词
- [ ] 检查日志格式规范
- [ ] 验证@Slf4j注解使用

---

## ⚡ 性能优化建议

### 1. 合理使用日志级别
```java
// ✅ 推荐 - 先判断日志级别
if (log.isDebugEnabled()) {
    log.debug("[模块名] 复杂数据处理: {}", heavyDataCalculation());
}

// ❌ 避免 - 不必要的字符串构建
log.debug("[模块名] 处理数据: " + complexObject.toString());
```

### 2. 避免在热路径使用DEBUG日志
```java
// ✅ 推荐 - 生产环境避免
log.info("[模块名] 关键业务操作: result={}", result);
// log.debug("[模块名] 详细调试信息: detail={}", detail); // 仅在开发环境
```

### 3. 敏感信息处理
```java
// ✅ 推荐 - 敏感信息脱敏
log.info("[用户管理] 用户登录: phone={}, ip={}",
         maskPhone(user.getPhone()), getClientIp());

private String maskPhone(String phone) {
    if (phone == null || phone.length() < 11) return "****";
    return phone.substring(0, 3) + "****" + phone.substring(7);
}
```

---

## 🔍 违规检测脚本

### 检查脚本内容
```bash
#!/bin/bash
# slf4j-violation-check.sh

echo "🔍 IOE-DREAM SLF4J 违规检查"
echo "================================"

# 检查LoggerFactory使用
echo "1. 检查LoggerFactory违规使用:"
find ./microservices -name "*.java" -exec grep -l "LoggerFactory.getLogger" {} \; | wc -l
echo "个文件仍在使用LoggerFactory"

# 检查格式问题
echo "2. 检查getLogger空格问题:"
find ./microservices -name "*.java" -exec grep -l "getLogger\s*(" {} \; | wc -l
echo "个文件存在格式问题"

# 检查字符串拼接
echo "3. 检查字符串拼接日志:"
find ./microservices -name "*.java" -exec grep -l "log\.\w\+.*\+.*log\." {} \; | wc -l
echo "个文件存在字符串拼接"

echo "================================"
echo "✅ 检查完成"
```

---

## 📈 监控指标

### 代码质量指标
- **@Slf4j使用率**: 目标100%
- **LoggerFactory违规数**: 目标0
- **日志格式合规率**: 目标95%
- **字符串拼接违规数**: 目标0

### 监控方式
1. **Git Pre-commit Hook**: 自动检查
2. **SonarQube规则**: 代码质量门禁
3. **定期扫描**: 每周自动报告

---

## 🚀 实施计划

### 已完成（2025-12-20）
- ✅ 修复13个严重格式问题
- ✅ 重构consume-service中的13个LoggerFactory
- ✅ 重构oa-service中的部分LoggerFactory
- ✅ 创建统一规范文档

### 持续执行
- 🔧 添加自动化检查脚本
- 📊 生成最终执行报告
- 🎯 长期监控和维护

---

## 📞 支持与反馈

### 规范执行支持
- **架构委员会**: 负责规范解释和仲裁
- **技术团队**: 负责具体实施和检查
- **质量团队**: 负责监控和报告

### 反馈渠道
- **Git Issues**: 规范相关问题
- **技术评审会**: 定期评审规范执行情况
- **团队培训**: 定期进行规范培训

---

**📋 总结**: 本规范旨在统一IOE-DREAM项目的SLF4J使用，提升代码质量和可维护性。所有开发人员必须严格遵守，违规代码将被拒绝合并。

---

**👥 制定**: IOE-DREAM架构委员会
**🏗️ 技术支持**: SmartAdmin核心团队
**✅ 最终解释权**: IOE-DREAM项目架构委员会
**📅 版本**: v1.0.0 - 企业级强制规范