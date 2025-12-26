# 📋 IOE-DREAM 日志记录模式完整标准规范

> **版本**: v2.0.0 - 完整版
> **生效日期**: 2025-12-21
> **适用范围**: IOE-DREAM所有微服务和模块
> **规范级别**: 🔴 **强制执行**
> **制定人**: IOE-DREAM架构委员会
> **更新日期**: 2025-12-21

---

## 🎯 核心原则（强制执行）

### 1. **唯一标准原则**
- ✅ **强制统一使用 `@Slf4j` 注解**
- ❌ **绝对禁止使用 `LoggerFactory.getLogger()`**
- ❌ **绝对禁止混合使用两种方式**
- ❌ **绝对禁止自定义Logger实例**

### 2. **代码质量原则**
- ✅ **使用参数化日志**（避免字符串拼接）
- ✅ **统一日志格式**（模块标识 + 操作描述）
- ✅ **合理日志级别**（ERROR/WARN/INFO/DEBUG/TRACE）
- ✅ **敏感信息脱敏**（密码、身份证、手机号等）

---

## 📝 标准使用模式（严格遵循）

### ✅ Controller层标准模板

```java
package net.lab1024.sa.{module}.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/{module}")
@Tag(name = "模块控制器")
public class {Module}Controller {

    @Resource
    private {Module}Service {module}Service;

    @GetMapping("/{id}")
    public ResponseDTO<{Module}VO> get{Module}(@PathVariable Long id) {
        log.info("[{模块}管理] 查询{模块}信息: id={}", id);

        try {
            {Module}Entity entity = {module}Service.getById(id);
            if (entity == null) {
                log.warn("[{模块}管理] {模块}不存在: id={}", id);
                return ResponseDTO.error("{MODULE}_NOT_FOUND", "{模块}不存在");
            }

            log.info("[{模块}管理] 查询成功: id={}, result={}", id, entity.getName());
            return ResponseDTO.ok(convertToVO(entity));

        } catch (Exception e) {
            log.error("[{模块}管理] 查询异常: id={}, error={}", id, e.getMessage(), e);
            throw new BusinessException("{MODULE}_QUERY_ERROR", "查询{模块}失败");
        }
    }

    @PostMapping
    public ResponseDTO<Void> create{Module}(@Valid @RequestBody {Module}CreateForm form) {
        try {
            log.info("[{模块}管理] 创建{模块}开始: name={}, type={}", form.getName(), form.getType());

            {module}Service.create(form);

            log.info("[{模块}管理] 创建成功: id={}, name={}", form.getId(), form.getName());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[{模块}管理] 创建失败: name={}, error={}", form.getName(), e.getMessage(), e);
            throw new BusinessException("{MODULE}_CREATE_ERROR", "创建{模块}失败");
        }
    }
}
```

### ✅ Service层标准模板

```java
package net.lab1024.sa.{module}.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class {Module}ServiceImpl implements {Module}Service {

    @Resource
    private {Module}Dao {module}Dao;
    @Resource
    private {Module}Manager {module}Manager;

    @Override
    public {Module}Entity getById(Long id) {
        log.debug("[{模块}服务] 查询{模块}开始: id={}", id);

        {Module}Entity entity = {module}Dao.selectById(id);

        log.debug("[{模块}服务] 查询{模块}结束: id={}, result={}", id, entity != null ? "found" : "not found");
        return entity;
    }

    @Override
    @Transactional
    public void create({Module}CreateForm form) {
        log.info("[{模块}服务] 创建{模块}开始: name={}, type={}", form.getName(), form.getType());

        try {
            // 业务逻辑验证
            {module}Manager.validateCreateRequest(form);

            // 转换为实体
            {Module}Entity entity = convertToEntity(form);

            // 数据库操作
            {module}Dao.insert(entity);

            log.info("[{模块}服务] 创建{模块}成功: id={}, name={}", entity.getId(), entity.getName());

        } catch (Exception e) {
            log.error("[{模块}服务] 创建{模块}失败: name={}, error={}", form.getName(), e.getMessage(), e);
            throw new BusinessException("{MODULE}_CREATE_ERROR", "创建{模块}失败");
        }
    }
}
```

### ✅ Manager层标准模板

```java
package net.lab1024.sa.{module}.manager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class {Module}Manager {

    private final {Module}Dao {module}Dao;
    private final GatewayServiceClient gatewayServiceClient;

    public {Module}Manager({Module}Dao {module}Dao, GatewayServiceClient gatewayServiceClient) {
        this.{module}Dao = {module}Dao;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    public void validateCreateRequest({Module}CreateForm form) {
        log.debug("[{模块}管理器] 验证创建请求: name={}", form.getName());

        // 参数验证
        if (StringUtils.isBlank(form.getName())) {
            throw new BusinessException("PARAM_ERROR", "{模块}名称不能为空");
        }

        // 业务规则验证
        {Module}Entity existing = {module}Dao.selectByName(form.getName());
        if (existing != null) {
            log.warn("[{模块}管理器] {模块}名称已存在: name={}", form.getName());
            throw new BusinessException("DUPLICATE_NAME", "{模块}名称已存在");
        }

        log.debug("[{模块}管理器] 验证创建请求通过: name={}", form.getName());
    }

    public {Module}VO getWithUserInfo(Long {module}Id) {
        log.debug("[{模块}管理器] 获取{模块}及用户信息: {module}Id={}", {module}Id);

        {Module}Entity {module} = {module}Dao.selectById({module}Id);
        if ({module} == null) {
            log.warn("[{模块}管理器] {模块}不存在: {module}Id={}", {module}Id);
            return null;
        }

        try {
            UserInfoResponse user = gatewayServiceClient.callCommonService(
                "/api/user/" + {module}.getCreateUserId(),
                HttpMethod.GET,
                null,
                UserInfoResponse.class
            );

            return {Module}VO.builder()
                    .id({module}.getId())
                    .name({module}.getName())
                    .creator(user != null ? user.getName() : "系统")
                    .createTime({module}.getCreateTime())
                    .build();

        } catch (Exception e) {
            log.warn("[{模块}管理器] 获取用户信息失败: {module}Id={}, error={}", {module}Id}, e.getMessage());
            return {Module}VO.builder()
                    .id({module}.getId())
                    .name({module}.getName())
                    .creator("未知用户")
                    .createTime({module}.getCreateTime())
                    .build();
        }
    }
}
```

### ✅ DAO层标准模板

```java
package net.lab1024.sa.{module}.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Slf4j
@Mapper
public interface {Module}Dao extends BaseMapper<{Module}Entity> {

    @Select("SELECT * FROM t_{module}_table WHERE status = 1 AND deleted_flag = 0")
    List<{Module}Entity> selectAllActive();

    @Select("SELECT * FROM t_{module}_table WHERE name = #{name} AND deleted_flag = 0 LIMIT 1")
    {Module}Entity selectByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM t_{module}_table WHERE deleted_flag = 0")
    Long countTotal();
}
```

### ✅ 配置类标准模板

```java
package net.lab1024.sa.{module}.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration("{module}Configuration")
public class {Module}Configuration {

    @Bean
    @ConditionalOnMissingBean({Module}Manager.class)
    public {Module}Manager {module}Manager({Module}Dao {module}Dao, gatewayServiceClient) {
        log.info("[{模块}配置] 注册{Module}Manager Bean");
        return new {Module}Manager({module}Dao, gatewayServiceClient);
    }

    @Bean
    public {Module}Service {module}Service({Module}Dao {module}Dao, {module}Manager {module}Manager) {
        log.info("[{模块}配置] 注册{Module}Service Bean");
        return new {Module}ServiceImpl({module}Dao, {module}Manager);
    }
}
```

### ✅ 工具类标准模板

```java
package net.lab1024.sa.{module}.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class {Module}Util {

    private {Module}Util() {
        // 工具类私有构造函数
    }

    public static String validateName(String name) {
        if (StringUtils.isBlank(name)) {
            log.warn("[{模块}工具] 名称验证失败: name为空");
            throw new BusinessException("PARAM_ERROR", "{模块}名称不能为空");
        }

        if (name.length() > 100) {
            log.warn("[{模块}工具] 名称验证失败: name={}, length={}", name, name.length());
            throw new BusinessException("PARAM_ERROR", "{模块}名称长度不能超过100个字符");
        }

        log.debug("[{模块}工具] 名称验证通过: name={}", name);
        return name.trim();
    }

    public static String maskSensitiveData(String data, String type) {
        if (data == null) {
            return "****";
        }

        switch (type) {
            case "phone":
                return data.length() >= 11 ? data.substring(0, 3) + "****" + data.substring(7) : "****";
            case "idCard":
                return data.length() >= 6 ? data.substring(0, 3) + "***********" + data.substring(data.length() - 4) : "****";
            case "email":
                return data.contains("@") ? data.substring(0, 2) + "***" + data.substring(data.indexOf("@") + 1) : "****";
            default:
                return "****";
        }
    }
}
```

### ✅ 测试类标准模板

```java
package net.lab1024.sa.{module}.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class {Module}IntegrationTest {

    @Resource
    private {Module}Service {module}Service;

    @Test
    void testCreate{Module}_Success() {
        log.info("[{模块}测试] 测试创建{模块}成功开始");

        // 测试数据准备
        {Module}CreateForm form = new {Module}CreateForm();
        form.setName("测试{module}");
        form.setType("TEST");

        // 执行测试
        assertDoesNotThrow(() -> {
            {module}Service.create(form);
        });

        log.info("[{模块}测试] 测试创建{模块}成功完成");
    }

    @Test
    void testCreate{Module}_DuplicateName() {
        log.info("[{模块}测试] 测试创建{模块}重复名称开始");

        // 测试数据准备
        {Module}CreateForm form1 = new {Module}CreateForm();
        form1.setName("重复测试");
        {module}Service.create(form1);

        {Module}CreateForm form2 = new {Module}CreateForm();
        form2.setName("重复测试");

        // 验证重复名称异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            {module}Service.create(form2);
        });

        assertEquals("DUPLICATE_NAME", exception.getCode());
        log.info("[{模块}测试] 测试创建{模块}重复名称完成: {}", exception.getMessage());
    }
}
```

---

## 🎨 日志格式标准（严格执行）

### 统一格式模板
```
log.[级别]("[模块名] 操作描述: 参数1={}, 参数2={}", value1, value2);
```

### 模块名映射表
| 微服务 | 模块名前缀 | 日志示例 |
|--------|-----------|----------|
| ioedream-access-service | `[门禁管理]` | `log.info("[门禁管理] 设备离线: deviceId={}", deviceId)` |
| ioedream-attendance-service | `[考勤管理]` | `log.info("[考勤管理] 打卡成功: userId={}", userId)` |
| ioedream-consume-service | `[消费管理]` | `log.info("[消费管理] 支付成功: amount={}", amount)` |
| ioedream-oa-service | `[OA办公]` | `log.info("[OA办公] 流程启动: processId={}", processId)` |
| ioedream-video-service | `[视频监控]` | `log.info("[视频监控] 设备上线: deviceId={}", deviceId)` |
| ioedream-visitor-service | `[访客管理]` | `log.info("[访客管理] 预约成功: visitorId={}", visitorId)` |
| ioedream-biometric-service | `[生物识别]` | `log.info("[生物识别] 模板下发: deviceId={}", deviceId)` |
| ioedream-device-comm-service | `[设备通信]` | `log.info("[设备通信] 协议适配: vendor={}", vendor)` |
| microservices-common | `[公共模块]` | `log.info("[公共模块] 缓存刷新完成")` |

### 日志级别使用规范

| 级别 | 使用场景 | 示例 | 日志级别 |
|------|---------|------|----------|
| **ERROR** | 系统错误、异常处理 | `log.error("[模块名] 系统异常: error={}", e.getMessage(), e)` | 严重错误 |
| **WARN** | 警告信息、业务异常 | `log.warn("[模块名] 用户无权限: userId={}", userId)` | 警告信息 |
| **INFO** | 业务关键节点、重要操作 | `log.info("[模块名] 用户登录成功: userId={}", userId)` | 重要信息 |
| **DEBUG** | 调试信息、详细流程 | `log.debug("[模块名] 方法调用开始: method={}, params={}", method, params)` | 调试信息 |
| **TRACE** | 详细追踪、循环迭代 | `log.trace("[模块名] 处理第{}条记录", index)` | 详细追踪 |

### 日志内容标准

#### ✅ 正确示例
```java
// 业务关键节点
log.info("[模块名] 操作开始: 参数={}", param);

// 成功完成
log.info("[模块名] 操作成功: result={}", result);

// 业务异常
log.warn("[模块名] 业务异常: code={}, message={}", code, message);

// 系统异常
log.error("[模块名] 系统异常: error={}", e.getMessage(), e);

// 调试信息
log.debug("[模块名] 方法执行: method={}, args={}", method, Arrays.toString(args));
```

#### ❌ 错误示例（绝对禁止）
```java
// ❌ 字符串拼接
log.info("用户信息: " + user.getUsername() + ", 邮箱: " + user.getEmail());

// ❌ 缺少模块标识
log.info("查询用户: userId={}", userId); // 应该加上[模块名]

// ❌ 日志级别使用不当
log.info("用户不存在: userId={}", userId); // 应该使用warn

// ❌ 敏感信息未脱敏
log.info("用户登录: phone={}, password={}", phone, password); // 密码必须脱敏
```

### 敏感信息脱敏标准

#### 脱敏方法
```java
private String maskPhone(String phone) {
    if (phone == null || phone.length() < 11) return "****";
    return phone.substring(0, 3) + "****" + phone.substring(7);
}

private String maskIdCard(String idCard) {
    if (idCard == null || idCard.length() < 6) return "****";
    return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
}

private String maskEmail(String email) {
    if (email == null || !email.contains("@")) return "****";
    int atIndex = email.indexOf("@");
    return email.substring(0, 2) + "***" + email.substring(atIndex + 1);
}
```

#### 脱敏示例
```java
// ✅ 正确脱敏
log.info("[用户管理] 用户登录: phone={}, ip={}",
         maskPhone(user.getPhone()),
         getClientIp());

log.info("[消费管理] 支付信息: amount={}, cardNo={}",
         amount,
         maskCardNo(payment.getCardNumber()));

// ❌ 错误 - 敏感信息暴露
log.info("[消费管理] 支付信息: amount={}, cardNo={}, password={}",
         amount, payment.getCardNumber(), payment.getPassword());
```

---

## 🔧 IDE配置规范

### IDEA Live Template配置
```
模板名称: slf4j-class
模板内容:
@Slf4j
public class $CLASS_NAME$ {
    $END$
}

模板名称: slf4j-interface
模板内容:
@Slf4j
public interface $CLASS_NAME$ {
    $END$
}
```

### 代码检查规则

#### 1. 强制检查项目
- [ ] 使用@Slf4j注解而非LoggerFactory
- [ ] 日志格式符合标准：`[模块名] 操作描述: 参数={} `
- [ ] 使用参数化日志，无字符串拼接
- [ ] 日志级别使用合理
- [ ] 异常日志包含完整堆栈信息
- [ ] 敏感信息已脱敏处理
- [ ] 无LoggerFactory违规使用
- [ ] 无混合使用情况

#### 2. Code Review检查清单
- [ ] 无LoggerFactory违规使用
- [ ] 日志内容有意义，便于问题排查
- [ ] 敏感信息已脱敏处理
- [ ] 日志级别使用正确
- [ ] @Slf4j注解位置正确
- [ ] 无冗余Logger声明

#### 3. CI/CD自动检查
- [ ] 扫描LoggerFactory关键词
- [ ] 检查日志格式规范
- [ ] 验证@Slf4j注解使用
- [ ] 检查敏感信息脱敏

---

## 📊 质量检查清单

### 开发阶段检查
- [ ] 类声明前添加@Slf4j注解
- [ ] 移除LoggerFactory相关导入
- [ ] 移除Logger声明语句
- [ ] 日志格式符合标准模板
- [ ] 使用参数化日志而非字符串拼接
- [ ] 敏感信息已脱敏
- [ ] 异常处理包含完整日志

### Code Review阶段检查
- [ ] @Slf4j注解位置正确（类声明前）
- [ ] 无LoggerFactory残留
- [ ] 日志格式统一
- [ ] 日志级别使用合理
- [ ] 敏感信息处理合规
- [ ] 无冗余或重复日志

### 测试阶段检查
- [ ] 测试类也使用@Slf4j注解
- [ ] 测试日志包含必要信息
- [ ] 测试异常有适当日志记录
- [ ] 测试数据脱敏处理

### 部署阶段检查
- [ ] 生产环境日志级别配置合理
- [ ] 日志输出格式规范
- [ ] 日志文件轮转配置
- [ ] 日志收集系统集成正常

---

## 🚨 违规检测脚本

### 手动检测命令
```bash
# 1. 检查LoggerFactory违规使用
echo "🔍 检查LoggerFactory违规使用:"
find ./microservices -name "*.java" -exec grep -l "LoggerFactory.getLogger" {} \; | wc -l

# 2. 检查混合使用情况
echo "🔍 检查混合使用情况:"
find ./microservices -name "*.java" -exec grep -l "@Slf4j" {} \; | xargs grep -l "LoggerFactory" {} \; | wc -l

# 3. 检查格式问题
echo "🔍 检查getLogger格式问题:"
find ./microservices -name "*.java" -exec grep -H "getLogger\s*(" {} \; | grep -v "\.class)" | wc -l

# 4. 检查字符串拼接
echo "🔍 检查字符串拼接日志:"
find ./microservices -name "*.java" -exec grep -H "log\.\w\+.*\+.*log\." {} \; | wc -l

# 5. 验证修复结果
echo "🔍 验证修复结果:"
echo "@Slf4j使用情况:"
find ./microservices -name "*.java" -exec grep -l "@Slf4j" {} \; | wc -l
echo "LoggerFactory残留情况:"
find ./microservices -name "*.java" -exec grep -l "LoggerFactory" {} \; | wc -l
```

---

## 📋 实施步骤

### Phase 1: 规范确认（已完成）
- ✅ 完整日志模板及规范制定
- ✅ CLAUDE.md更新要求

### Phase 2: CLAUDE.md规范更新（待执行）
- ⏳ 在CLAUDE.md中明确日志规范要求
- ⏳ 更新架构规范文档

### Phase 3: 手动修复执行（待执行）
- ⏳ 严格按照手动修复每个文件
- ⏳ 每个文件修复后验证编译
- ⏳ 按模块分批执行，确保可控

### Phase 4: 质量验证（待执行）
- ⏳ 全项目扫描验证
- ⏳ 编译测试验证
- ⏳ 日志输出验证

---

## 📞 执行支持

### 问题反馈
- 规范问题请联系架构委员会
- 技术问题请联系开发团队
- 工具问题请联系DevOps团队

### 更新机制
- 定期回顾和更新规范
- 根据项目实践调整优化
- 持续改进日志标准

---

**📋 文档版本历史**:
- v1.0.0: 基础规范版本
- v2.0.0: 完整版标准模板（当前版本）

**🎯 下一步**: 更新CLAUDE.md规范文档，然后开始手动修复363个文件。