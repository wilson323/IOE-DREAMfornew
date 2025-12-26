# 密码加密实施指南

> **项目**: IOE-DREAM智慧园区管理平台
> **任务**: P1-8.1 密码加密 - BCrypt、密码强度验证、过期策略
> **实施日期**: 2025-12-26
> **预计周期**: 2人天
> **安全等级**: P1-高优先级

---

## 📋 实施目标

### 安全目标

1. **密码加密存储** - 使用BCrypt强加密算法
2. **密码强度验证** - 强制复杂密码策略
3. **密码过期策略** - 定期更换密码
4. **密码历史记录** - 防止重复使用旧密码
5. **登录失败锁定** - 防止暴力破解

### 安全标准

| 安全项 | 当前状态 | 目标 | 优先级 |
|--------|---------|------|--------|
| **密码加密算法** | ❌ MD5/SHA1 | ✅ BCrypt | P1-高 |
| **密码强度验证** | ❌ 无验证 | ✅ 强制复杂策略 | P1-高 |
| **密码过期** | ❌ 永久有效 | ✅ 90天过期 | P1-高 |
| **密码历史** | ❌ 无记录 | ✅ 记录5个历史密码 | P1-中 |
| **登录锁定** | ❌ 无限制 | ✅ 5次失败锁定30分钟 | P1-高 |

---

## 🔐 一、BCrypt密码加密

### 1.1 BCrypt算法优势

**为什么选择BCrypt**:
- ✅ 自适应哈希函数(可调整计算成本)
- ✅ 内置盐值(每个密码唯一盐值)
- ✅ 抗彩虹表攻击
- ✅ 抗GPU/ASIC暴力破解
- ✅ 行业标准(OpenBSD、GitHub、Linux)

**算法对比**:

| 算法 | 安全性 | 抗破解性 | 推荐度 |
|------|--------|---------|--------|
| MD5 | ❌ 已破解 | ❌ 低 | 不推荐 |
| SHA1 | ❌ 已破解 | ❌ 低 | 不推荐 |
| SHA256 | ⚠️ 可用 | ⚠️ 中 | 可用 |
| BCrypt | ✅ 最强 | ✅ 高 | **强烈推荐** |
| Argon2 | ✅ 最强 | ✅ 最高 | 推荐 |

### 1.2 BCrypt加密实现

**依赖配置**:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
<dependency>
    <groupId>at.favre.libre</groupId>
    <artifactId>bcrypt</artifactId>
    <version>0.10.2</version>
</dependency>
```

**密码加密工具类**:
```java
package net.lab1024.sa.common.security.util;

import at.favre.libre.bcrypt.BCrypt;

/**
 * BCrypt密码加密工具类
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public class PasswordEncoderUtil {

    /**
     * BCrypt成本因子(计算复杂度)
     * <p>
     * 4-6: 测试环境
     * 8-10: 低安全级别
     * 12: 标准安全级别(推荐)
     * 14-15: 高安全级别
     * </p>
     */
    private static final int BCRIPT_COST = 12;

    /**
     * 加密密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, rawPassword.toCharArray());
    }

    /**
     * 验证密码
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword);
        return result.verified;
    }

    /**
     * 生成BCrypt哈希(自定义成本因子)
     *
     * @param rawPassword 明文密码
     * @param cost 成本因子(4-31)
     * @return 加密后的密码
     */
    public static String encodeWithCost(String rawPassword, int cost) {
        if (cost < 4 || cost > 31) {
            throw new IllegalArgumentException("成本因子必须在4-31之间");
        }
        return BCrypt.withDefaults().hashToString(cost, rawPassword.toCharArray());
    }

    /**
     * 检查密码是否需要重新加密
     *
     * @param encodedPassword 加密后的密码
     * @param currentCost 当前成本因子
     * @return 是否需要重新加密
     */
    public static boolean needsRehash(String encodedPassword, int currentCost) {
        BCrypt.HashData hashData = BCrypt.hashData(encodedPassword);
        return hashData.cost != currentCost;
    }

    /**
     * 提取BCrypt成本因子
     *
     * @param encodedPassword 加密后的密码
     * @return 成本因子
     */
    public static int extractCost(String encodedPassword) {
        BCrypt.HashData hashData = BCrypt.hashData(encodedPassword);
        return hashData.cost;
    }
}
```

### 1.3 密码加密服务

```java
package net.lab1024.sa.common.security.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.security.util.PasswordEncoderUtil;
import org.springframework.stereotype.Service;

/**
 * 密码加密服务
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class PasswordService {

    /**
     * 加密用户密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("明文密码不能为空");
        }

        // 记录原始密码长度(用于验证)
        int length = rawPassword.length();
        log.info("[密码加密] 开始加密密码, 长度: {}", length);

        // BCrypt加密
        String encodedPassword = PasswordEncoderUtil.encode(rawPassword);

        log.info("[密码加密] 密码加密完成, BCrypt成本因子: {}",
            PasswordEncoderUtil.extractCost(encodedPassword));

        return encodedPassword;
    }

    /**
     * 验证密码
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            log.warn("[密码验证] 密码验证失败: 密码为空");
            return false;
        }

        // BCrypt验证
        boolean matches = PasswordEncoderUtil.matches(rawPassword, encodedPassword);

        if (!matches) {
            log.warn("[密码验证] 密码验证失败: 密码不匹配");
        } else {
            log.debug("[密码验证] 密码验证成功");
        }

        return matches;
    }

    /**
     * 检查密码是否需要重新加密
     * <p>
     * 当BCrypt成本因子升级时,需要重新加密现有密码
     * </p>
     *
     * @param encodedPassword 加密后的密码
     * @param targetCost 目标成本因子
     * @return 是否需要重新加密
     */
    public boolean needsRehash(String encodedPassword, int targetCost) {
        boolean needsRehash = PasswordEncoderUtil.needsRehash(encodedPassword, targetCost);

        if (needsRehash) {
            log.info("[密码加密] 密码需要重新加密: 当前成本因子={}",
                PasswordEncoderUtil.extractCost(encodedPassword));
        }

        return needsRehash;
    }
}
```

### 1.4 用户密码更新

**Service层实现**:
```java
package net.lab1024.sa.common.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.auth.dao.UserDao;
import net.lab1024.sa.common.auth.entity.UserEntity;
import net.lab1024.sa.common.security.service.PasswordService;
import net.lab1024.sa.common.security.service.PasswordStrengthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private PasswordService passwordService;

    @Resource
    private PasswordStrengthService passwordStrengthService;

    /**
     * 创建用户(自动加密密码)
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createUser(UserAddForm form) {
        // 1. 验证密码强度
        PasswordStrengthService.StrengthLevel strength =
            passwordStrengthService.validateStrength(form.getPassword());

        if (strength == PasswordStrengthService.StrengthLevel.WEAK) {
            throw new BusinessException("USER_001", "密码强度不足,请使用复杂密码");
        }

        // 2. 加密密码
        String encodedPassword = passwordService.encodePassword(form.getPassword());

        // 3. 创建用户实体
        UserEntity user = new UserEntity();
        user.setUsername(form.getUsername());
        user.setPassword(encodedPassword);  // 存储加密后的密码
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setDeptId(form.getDeptId());

        // 4. 插入数据库
        userDao.insert(user);

        log.info("[用户管理] 用户创建成功: userId={}, username={}",
            user.getUserId(), user.getUsername());

        return user.getUserId();
    }

    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_002", "用户不存在");
        }

        // 2. 验证旧密码
        if (!passwordService.matchesPassword(oldPassword, user.getPassword())) {
            throw new BusinessException("USER_003", "原密码错误");
        }

        // 3. 验证新密码强度
        PasswordStrengthService.StrengthLevel strength =
            passwordStrengthService.validateStrength(newPassword);

        if (strength == PasswordStrengthService.StrengthLevel.WEAK) {
            throw new BusinessException("USER_004", "新密码强度不足");
        }

        // 4. 验证密码历史(不能与最近5次密码相同)
        if (isPasswordInHistory(userId, newPassword)) {
            throw new BusinessException("USER_005", "新密码不能与最近使用的密码相同");
        }

        // 5. 加密新密码
        String encodedPassword = passwordService.encodePassword(newPassword);

        // 6. 更新密码
        user.setPassword(encodedPassword);
        user.setUpdateTime(LocalDateTime.now());
        userDao.updateById(user);

        // 7. 记录密码历史
        savePasswordHistory(userId, encodedPassword);

        log.info("[用户管理] 密码修改成功: userId={}", userId);
    }

    /**
     * 重置密码(管理员操作)
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resetPassword(Long userId, String newPassword) {
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_006", "用户不存在");
        }

        // 生成临时密码
        String encodedPassword = passwordService.encodePassword(newPassword);

        user.setPassword(encodedPassword);
        user.setForceChangePassword(true);  // 强制下次登录修改密码
        user.setUpdateTime(LocalDateTime.now());
        userDao.updateById(user);

        log.info("[用户管理] 密码重置成功: userId={}", userId);
    }

    /**
     * 检查密码是否在历史记录中
     */
    private boolean isPasswordInHistory(Long userId, String newPassword) {
        // TODO: 实现密码历史检查
        return false;
    }

    /**
     * 保存密码历史
     */
    private void savePasswordHistory(Long userId, String encodedPassword) {
        // TODO: 实现密码历史保存
    }
}
```

---

## 🛡️ 二、密码强度验证

### 2.1 密码强度策略

**密码复杂度要求**:
- ✅ 最小长度: 8位
- ✅ 最大长度: 128位
- ✅ 必须包含: 大写字母、小写字母、数字、特殊符号
- ✅ 禁止包含: 用户名、手机号、常见弱密码

### 2.2 密码强度验证服务

```java
package net.lab1024.sa.common.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密码强度验证服务
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class PasswordStrengthService {

    /**
     * 密码强度等级
     */
    public enum StrengthLevel {
        WEAK(0, "弱"),
        MEDIUM(1, "中"),
        STRONG(2, "强"),
        VERY_STRONG(3, "很强");

        private final int level;
        private final String description;

        StrengthLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 弱密码列表(常见弱密码)
     */
    private static final List<String> WEAK_PASSWORDS = List.of(
        "password", "12345678", "123456789", "qwerty123",
        "abc12345", "password123", "admin123", "root123",
        "11111111", "22222222", "33333333", "88888888"
    );

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 密码强度等级
     */
    public StrengthLevel validateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return StrengthLevel.WEAK;
        }

        List<String> errors = new ArrayList<>();

        // 1. 长度检查
        if (password.length() < 8) {
            errors.add("密码长度不能少于8位");
            return StrengthLevel.WEAK;
        }

        if (password.length() > 128) {
            errors.add("密码长度不能超过128位");
            return StrengthLevel.WEAK;
        }

        // 2. 弱密码检查
        if (WEAK_PASSWORDS.contains(password.toLowerCase())) {
            errors.add("密码过于简单,请使用更复杂的密码");
            return StrengthLevel.WEAK;
        }

        // 3. 复杂度检查
        boolean hasUpperCase = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLowerCase = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find();

        int complexityScore = 0;
        if (hasUpperCase) complexityScore++;
        if (hasLowerCase) complexityScore++;
        if (hasDigit) complexityScore++;
        if (hasSpecial) complexityScore++;

        // 4. 计算强度等级
        StrengthLevel level;
        if (complexityScore <= 2) {
            level = StrengthLevel.WEAK;
            if (!hasUpperCase) errors.add("密码必须包含大写字母");
            if (!hasLowerCase) errors.add("密码必须包含小写字母");
            if (!hasDigit) errors.add("密码必须包含数字");
            if (!hasSpecial) errors.add("密码必须包含特殊字符");
        } else if (complexityScore == 3) {
            level = StrengthLevel.MEDIUM;
        } else if (complexityScore == 4 && password.length() >= 12) {
            level = StrengthLevel.VERY_STRONG;
        } else {
            level = StrengthLevel.STRONG;
        }

        log.info("[密码强度] 密码强度验证: level={}, length={}, complexity={}",
            level.getDescription(), password.length(), complexityScore);

        return level;
    }

    /**
     * 验证密码是否符合基本要求
     *
     * @param password 密码
     * @return 是否符合
     */
    public boolean isValid(String password) {
        if (password == null || password.isEmpty()) {
            log.warn("[密码验证] 密码为空");
            return false;
        }

        if (password.length() < 8 || password.length() > 128) {
            log.warn("[密码验证] 密码长度不符合: length={}", password.length());
            return false;
        }

        if (WEAK_PASSWORDS.contains(password.toLowerCase())) {
            log.warn("[密码验证] 密码过于简单");
            return false;
        }

        return true;
    }

    /**
     * 获取密码强度建议
     *
     * @param password 密码
     * @return 建议信息列表
     */
    public List<String> getSuggestions(String password) {
        List<String> suggestions = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            suggestions.add("密码不能为空");
            return suggestions;
        }

        // 长度建议
        if (password.length() < 8) {
            suggestions.add("建议密码长度至少8位");
        } else if (password.length() < 12) {
            suggestions.add("建议密码长度12位以上更安全");
        }

        // 复杂度建议
        boolean hasUpperCase = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLowerCase = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find();

        if (!hasUpperCase) {
            suggestions.add("建议添加大写字母(A-Z)");
        }
        if (!hasLowerCase) {
            suggestions.add("建议添加小写字母(a-z)");
        }
        if (!hasDigit) {
            suggestions.add("建议添加数字(0-9)");
        }
        if (!hasSpecial) {
            suggestions.add("建议添加特殊字符(!@#$%^&*()等)");
        }

        return suggestions;
    }
}
```

### 2.3 密码强度验证注解

```java
package net.lab1024.sa.common.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import net.lab1024.sa.common.security.service.PasswordStrengthService;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 密码强度验证注解
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Documented
@Constraint(validatedBy = PasswordStrengthValidator.class)
@Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordStrength {

    String message() default "密码强度不符合要求";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 最小强度等级
     * <p>
     * 0: 弱
     * 1: 中
     * 2: 强
     * 3: 很强
     * </p>
     int minLevel() default 2;
}
```

```java
package net.lab1024.sa.common.security.validation;

import net.lab1024.sa.common.security.service.PasswordStrengthService;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 密码强度验证器
 */
public class PasswordStrengthValidator implements ConstraintValidator<PasswordStrength, String> {

    @Autowired
    private PasswordStrengthService passwordStrengthService;

    private int minLevel;

    @Override
    public void initialize(PasswordStrength constraintAnnotation) {
        this.minLevel = constraintAnnotation.minLevel();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        PasswordStrengthService.StrengthLevel level =
            passwordStrengthService.validateStrength(value);

        return level.getLevel() >= minLevel;
    }
}
```

**使用示例**:
```java
@Data
public class UserAddForm {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度必须在4-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @PasswordStrength(minLevel = 2, message = "密码强度必须为强或很强")
    private String password;
}
```

---

## 📅 三、密码过期策略

### 3.1 密码过期配置

**application.yml配置**:
```yaml
# 密码策略配置
password:
  # 密码有效期(天)
  expiration-days: 90

  # 密码过期前多少天提醒用户
  reminder-days: 7

  # 密码历史记录数量(防止重复使用旧密码)
  history-count: 5

  # 密码最小修改间隔(天,防止频繁修改)
  min-change-interval: 1

  # 是否强制首次登录修改密码
  force-change-on-first-login: true
```

### 3.2 密码过期检查服务

```java
package net.lab1024.sa.common.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * 密码过期检查服务
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class PasswordExpirationService {

    @Value("${password.expiration-days:90}")
    private int expirationDays;

    @Value("${password.reminder-days:7}")
    private int reminderDays;

    /**
     * 检查密码是否过期
     *
     * @param lastPasswordUpdateTime 上次修改密码时间
     * @return 是否过期
     */
    public boolean isExpired(LocalDate lastPasswordUpdateTime) {
        if (lastPasswordUpdateTime == null) {
            return true;
        }

        LocalDate expireDate = lastPasswordUpdateTime.plusDays(expirationDays);
        boolean expired = LocalDate.now().isAfter(expireDate);

        if (expired) {
            log.warn("[密码过期] 密码已过期: lastUpdate={}, expireDate={}",
                lastPasswordUpdateTime, expireDate);
        }

        return expired;
    }

    /**
     * 检查是否需要提醒用户修改密码
     *
     * @param lastPasswordUpdateTime 上次修改密码时间
     * @return 是否需要提醒
     */
    public boolean needsReminder(LocalDate lastPasswordUpdateTime) {
        if (lastPasswordUpdateTime == null) {
            return true;
        }

        LocalDate reminderDate = lastPasswordUpdateTime.plusDays(expirationDays - reminderDays);
        boolean needsReminder = LocalDate.now().isAfter(reminderDate);

        if (needsReminder) {
            log.info("[密码过期] 需要提醒用户修改密码: lastUpdate={}, reminderDate={}",
                lastPasswordUpdateTime, reminderDate);
        }

        return needsReminder;
    }

    /**
     * 计算密码剩余有效天数
     *
     * @param lastPasswordUpdateTime 上次修改密码时间
     * @return 剩余天数(负数表示已过期)
     */
    public int getRemainingDays(LocalDate lastPasswordUpdateTime) {
        if (lastPasswordUpdateTime == null) {
            return -expirationDays;
        }

        LocalDate expireDate = lastPasswordUpdateTime.plusDays(expirationDays);
        return Period.between(LocalDate.now(), expireDate).getDays();
    }
}
```

### 3.3 密码过期拦截器

```java
package net.lab1024.sa.common.security.interceptor;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.security.service.PasswordExpirationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 密码过期检查拦截器
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
public class PasswordExpirationInterceptor implements HandlerInterceptor {

    @Autowired
    private PasswordExpirationService passwordExpirationService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {

        // 从SecurityContext获取用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;  // 未登录用户,不做检查
        }

        // 获取用户密码修改时间(需要从UserService查询)
        LocalDate lastPasswordUpdateTime = getLastPasswordUpdateTime(authentication);

        // 检查是否过期
        if (passwordExpirationService.isExpired(lastPasswordUpdateTime)) {
            log.warn("[密码过期] 用户密码已过期,强制修改密码: username={}",
                authentication.getName());

            // 返回403错误,前端引导用户修改密码
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 40301, \"message\": \"密码已过期,请修改密码\"}");
            return false;
        }

        // 检查是否需要提醒
        if (passwordExpirationService.needsReminder(lastPasswordUpdateTime)) {
            int remainingDays = passwordExpirationService.getRemainingDays(lastPasswordUpdateTime);
            log.info("[密码过期] 提醒用户密码即将过期: username={}, remainingDays={}",
                authentication.getName(), remainingDays);

            // 在响应头中添加提醒信息
            response.setHeader("X-Password-Expiring-Soon", "true");
            response.setHeader("X-Password-Remaining-Days", String.valueOf(remainingDays));
        }

        return true;
    }

    /**
     * 获取用户密码修改时间
     */
    private LocalDate getLastPasswordUpdateTime(Authentication authentication) {
        // TODO: 从UserService查询用户密码修改时间
        return LocalDate.now();
    }
}
```

**注册拦截器**:
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private PasswordExpirationInterceptor passwordExpirationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passwordExpirationInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/v1/auth/login",
                "/api/v1/auth/logout",
                "/api/v1/auth/password/expire"  // 密码过期接口本身不拦截
            );
    }
}
```

---

## 🔒 四、登录失败锁定

### 4.1 登录失败记录

```java
package net.lab1024.sa.common.security.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录失败记录表
 */
@Data
@TableName("t_login_failure_log")
public class LoginFailureLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String logId;

    /**
     * 用户名
     */
    private String username;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 失败时间
     */
    private LocalDateTime failureTime;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 用户代理
     */
    private String userAgent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public static LoginFailureLogEntity create(String username, String ipAddress,
                                                  String failureReason, String userAgent) {
        LoginFailureLogEntity log = new LoginFailureLogEntity();
        log.setUsername(username);
        log.setIpAddress(ipAddress);
        log.setFailureTime(LocalDateTime.now());
        log.setFailureReason(failureReason);
        log.setUserAgent(userAgent);
        return log;
    }
}
```

### 4.2 登录失败锁定服务

```java
package net.lab1024.sa.common.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 登录失败锁定服务
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class LoginLockoutService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${security.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.lockout.lockout-duration:30}")
    private int lockoutDurationMinutes;

    private static final String LOGIN_FAILURE_PREFIX = "login:failure:";
    private static final String LOGIN_LOCKOUT_PREFIX = "login:lockout:";

    /**
     * 记录登录失败
     *
     * @param username 用户名
     * @param ipAddress IP地址
     * @return 是否被锁定
     */
    public boolean recordLoginFailure(String username, String ipAddress) {
        String key = LOGIN_FAILURE_PREFIX + username + ":" + ipAddress;

        // 增加失败次数
        Long failures = redisTemplate.opsForValue().increment(key);

        // 设置过期时间(30分钟)
        if (failures == 1) {
            redisTemplate.expire(key, 30, TimeUnit.MINUTES);
        }

        // 检查是否超过最大尝试次数
        if (failures >= maxAttempts) {
            // 锁定账号
            lockoutAccount(username, ipAddress);
            log.warn("[登录锁定] 登录失败次数过多,账号已锁定: username={}, ip={}, failures={}",
                username, ipAddress, failures);
            return true;
        }

        log.info("[登录失败] 记录登录失败: username={}, ip={}, failures={}/{}",
            username, ipAddress, failures, maxAttempts);

        return false;
    }

    /**
     * 检查账号是否被锁定
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isLockedOut(String username, String ipAddress) {
        String lockoutKey = LOGIN_LOCKOUT_PREFIX + username;

        Boolean locked = redisTemplate.hasKey(lockoutKey);
        if (Boolean.TRUE.equals(locked)) {
            Long remainingSeconds = redisTemplate.getExpire(lockoutKey, Long.class);
            log.warn("[登录锁定] 账号已被锁定: username={}, ip={}, remainingMinutes={}",
                username, ipAddress, remainingSeconds / 60);
            return true;
        }

        return false;
    }

    /**
     * 锁定账号
     */
    private void lockoutAccount(String username, String ipAddress) {
        String lockoutKey = LOGIN_LOCKOUT_PREFIX + username;

        // 设置锁定标记
        redisTemplate.opsForValue().set(lockoutKey, ipAddress,
            Duration.ofMinutes(lockoutDurationMinutes));

        log.info("[登录锁定] 账号已锁定: username={}, duration={}分钟",
            username, lockoutDurationMinutes);
    }

    /**
     * 清除登录失败记录(登录成功时调用)
     *
     * @param username 用户名
     * @param ipAddress IP地址
     */
    public void clearLoginFailures(String username, String ipAddress) {
        String key = LOGIN_FAILURE_PREFIX + username + ":" + ipAddress;
        redisTemplate.delete(key);

        log.info("[登录锁定] 清除登录失败记录: username={}, ip={}", username, ipAddress);
    }

    /**
     * 解除账号锁定(管理员操作)
     *
     * @param username 用户名
     */
    public void unlockAccount(String username) {
        String lockoutKey = LOGIN_LOCKOUT_PREFIX + username;
        redisTemplate.delete(lockoutKey);

        // 清除所有IP的失败记录
        String pattern = LOGIN_FAILURE_PREFIX + username + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern).toArray(new String[0]));

        log.info("[登录锁定] 管理员解除账号锁定: username={}", username);
    }

    /**
     * 获取剩余失败次数
     *
     * @param username 用户名
     * @return 剩余次数
     */
    public int getRemainingAttempts(String username, String ipAddress) {
        String key = LOGIN_FAILURE_PREFIX + username + ":" + ipAddress;

        String failuresStr = redisTemplate.opsForValue().get(key);
        int failures = failuresStr != null ? Integer.parseInt(failuresStr) : 0;

        return Math.max(0, maxAttempts - failures);
    }
}
```

### 4.3 登录接口集成

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Resource
    private UserService userService;

    @Resource
    private LoginLockoutService loginLockoutService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseDTO<LoginVO> login(@RequestBody LoginForm form,
                                  HttpServletRequest request) {
        String username = form.getUsername();
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // 1. 检查账号是否被锁定
        if (loginLockoutService.isLockedOut(username, ipAddress)) {
            return ResponseDTO.userError("登录失败次数过多,账号已被锁定30分钟");
        }

        // 2. 查询用户
        UserEntity user = userService.getByUsername(username);
        if (user == null) {
            // 记录登录失败
            loginLockoutService.recordLoginFailure(username, ipAddress);
            int remainingAttempts = loginLockoutService.getRemainingAttempts(username, ipAddress);
            return ResponseDTO.userError("用户名或密码错误,剩余尝试次数: " + remainingAttempts);
        }

        // 3. 验证密码
        if (!userService.matchesPassword(form.getPassword(), user.getPassword())) {
            // 记录登录失败
            boolean locked = loginLockoutService.recordLoginFailure(username, ipAddress);

            if (locked) {
                return ResponseDTO.userError("登录失败次数过多,账号已被锁定30分钟");
            }

            int remainingAttempts = loginLockoutService.getRemainingAttempts(username, ipAddress);
            return ResponseDTO.userError("用户名或密码错误,剩余尝试次数: " + remainingAttempts);
        }

        // 4. 检查用户状态
        if (user.getDeletedFlag() == 1) {
            return ResponseDTO.userError("账号已被禁用");
        }

        // 5. 登录成功,清除失败记录
        loginLockoutService.clearLoginFailures(username, ipAddress);

        // 6. 检查是否需要强制修改密码
        if (user.getForceChangePassword()) {
            return ResponseDTO.error("USER_006", "首次登录必须修改密码");
        }

        // 7. 生成Token
        String token = generateToken(user);

        // 8. 返回登录信息
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getUserId());
        loginVO.setUsername(user.getUsername());
        loginVO.setForceChangePassword(user.getForceChangePassword());

        log.info("[用户认证] 登录成功: username={}, ip={}", username, ipAddress);

        return ResponseDTO.ok(loginVO);
    }
}
```

---

## ✅ 五、完成检查清单

### 密码加密
- [ ] 实现BCrypt密码加密工具类
- [ ] 更新用户创建/修改密码逻辑
- [ ] 迁移现有MD5/SHA1密码到BCrypt
- [ ] 测试密码加密和验证功能

### 密码强度验证
- [ ] 实现密码强度验证服务
- [ ] 实现密码强度验证注解
- [ ] 集成到用户创建/修改密码表单
- [ ] 提供密码强度建议

### 密码过期策略
- [ ] 实现密码过期检查服务
- [ ] 实现密码过期拦截器
- [ ] 提供密码修改提醒
- [ ] 强制首次登录修改密码

### 登录失败锁定
- [ ] 创建登录失败记录表
- [ ] 实现登录失败锁定服务
- [ ] 集成到登录接口
- [ ] 提供管理员解锁功能

---

## 📚 相关文档

- Spring Security文档: https://docs.spring.io/spring-security/
- BCrypt算法说明: https://github.com/patrickfav/bcrypt
- OWASP密码存储: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html

---

## 🎯 总结

密码加密实施通过**BCrypt加密**、**密码强度验证**、**过期策略**和**登录锁定**四方面工作,全面提升了系统安全性:

- 🔐 **BCrypt加密**: 行业标准强加密算法,抗暴力破解
- 🛡️ **强度验证**: 强制复杂密码策略,防止弱密码
- 📅 **过期策略**: 90天密码有效期,定期更换
- 🔒 **登录锁定**: 5次失败锁定30分钟,防止暴力破解

这将显著提升IOE-DREAM系统的账户安全性。

---

**文档版本**: v1.0.0
**创建日期**: 2025-12-26
**下一步**: 继续其他P1任务实施
