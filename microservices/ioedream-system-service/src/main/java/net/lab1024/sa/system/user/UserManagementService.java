package net.lab1024.sa.system.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户管理服务
 * 支持用户CRUD操作、用户信息管理、密码策略和用户状态管理
 *
 * @author IOE-DREAM
 * @since 2025-11-28
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserManagementService {

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE("正常", "用户状态正常，可正常使用系统"),
        INACTIVE("禁用", "用户被禁用，无法使用系统"),
        LOCKED("锁定", "用户被锁定，通常由于多次登录失败"),
        EXPIRED("已过期", "用户账户已过期"),
        PENDING("待激活", "用户账户待激活"),
        SUSPENDED("暂停", "用户账户被暂时暂停");

        private final String description;
        private final String comment;

        UserStatus(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 用户类型枚举
     */
    public enum UserType {
        ADMIN("管理员", "系统管理员"),
        EMPLOYEE("员工", "普通员工"),
        MANAGER("经理", "部门经理"),
        CONTRACTOR("外包", "外包人员"),
        INTERN("实习生", "实习生"),
        TEMP("临时用户", "临时访问用户");

        private final String description;
        private final String comment;

        UserType(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 用户信息
     */
    public static class UserInfo {
        private String userId;
        private String username;
        private String email;
        private String mobile;
        private String realName;
        private String employeeId;
        private String departmentId;
        private String departmentName;
        private String position;
        private UserType userType;
        private UserStatus status;
        private String avatar;
        private LocalDateTime createTime;
        private LocalDateTime lastLoginTime;
        private LocalDateTime lastUpdateTime;
        private String lastLoginIp;
        private Integer loginCount;
        private LocalDateTime passwordUpdateTime;
        private LocalDateTime accountExpireTime;
        private String supervisorId;
        private String supervisorName;

        // 构造函数
        public UserInfo(String userId, String username, String email, String realName) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.realName = realName;
            this.userType = UserType.EMPLOYEE;
            this.status = UserStatus.ACTIVE;
            this.createTime = LocalDateTime.now();
            this.lastUpdateTime = LocalDateTime.now();
            this.loginCount = 0;
        }

        // Getter和Setter方法
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(String departmentId) {
            this.departmentId = departmentId;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public UserType getUserType() {
            return userType;
        }

        public void setUserType(UserType userType) {
            this.userType = userType;
        }

        public UserStatus getStatus() {
            return status;
        }

        public void setStatus(UserStatus status) {
            this.status = status;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getLastLoginTime() {
            return lastLoginTime;
        }

        public void setLastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
        }

        public LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        public String getLastLoginIp() {
            return lastLoginIp;
        }

        public void setLastLoginIp(String lastLoginIp) {
            this.lastLoginIp = lastLoginIp;
        }

        public Integer getLoginCount() {
            return loginCount;
        }

        public void setLoginCount(Integer loginCount) {
            this.loginCount = loginCount;
        }

        public LocalDateTime getPasswordUpdateTime() {
            return passwordUpdateTime;
        }

        public void setPasswordUpdateTime(LocalDateTime passwordUpdateTime) {
            this.passwordUpdateTime = passwordUpdateTime;
        }

        public LocalDateTime getAccountExpireTime() {
            return accountExpireTime;
        }

        public void setAccountExpireTime(LocalDateTime accountExpireTime) {
            this.accountExpireTime = accountExpireTime;
        }

        public String getSupervisorId() {
            return supervisorId;
        }

        public void setSupervisorId(String supervisorId) {
            this.supervisorId = supervisorId;
        }

        public String getSupervisorName() {
            return supervisorName;
        }

        public void setSupervisorName(String supervisorName) {
            this.supervisorName = supervisorName;
        }
    }

    /**
     * 用户扩展信息
     */
    public static class UserExtendedInfo {
        private String userId;
        private String gender;
        private String birthday;
        private String address;
        private String emergencyContact;
        private String emergencyPhone;
        private String education;
        private String workExperience;
        private String skills;
        private String interests;
        private String socialSecurityNumber;
        private String bankAccount;
        private String workPermitNumber;
        private Map<String, Object> customFields;

        // 构造函数
        public UserExtendedInfo(String userId) {
            this.userId = userId;
            this.customFields = new HashMap<>();
        }

        // Getter和Setter方法
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getEmergencyContact() {
            return emergencyContact;
        }

        public void setEmergencyContact(String emergencyContact) {
            this.emergencyContact = emergencyContact;
        }

        public String getEmergencyPhone() {
            return emergencyPhone;
        }

        public void setEmergencyPhone(String emergencyPhone) {
            this.emergencyPhone = emergencyPhone;
        }

        public String getEducation() {
            return education;
        }

        public void setEducation(String education) {
            this.education = education;
        }

        public String getWorkExperience() {
            return workExperience;
        }

        public void setWorkExperience(String workExperience) {
            this.workExperience = workExperience;
        }

        public String getSkills() {
            return skills;
        }

        public void setSkills(String skills) {
            this.skills = skills;
        }

        public String getInterests() {
            return interests;
        }

        public void setInterests(String interests) {
            this.interests = interests;
        }

        public String getSocialSecurityNumber() {
            return socialSecurityNumber;
        }

        public void setSocialSecurityNumber(String socialSecurityNumber) {
            this.socialSecurityNumber = socialSecurityNumber;
        }

        public String getBankAccount() {
            return bankAccount;
        }

        public void setBankAccount(String bankAccount) {
            this.bankAccount = bankAccount;
        }

        public String getWorkPermitNumber() {
            return workPermitNumber;
        }

        public void setWorkPermitNumber(String workPermitNumber) {
            this.workPermitNumber = workPermitNumber;
        }

        public Map<String, Object> getCustomFields() {
            return customFields;
        }

        public void setCustomFields(Map<String, Object> customFields) {
            this.customFields = customFields;
        }
    }

    /**
     * 创建用户
     */
    public UserInfo createUser(String username, String email, String password,
            String realName, String mobile, String employeeId,
            String departmentId, UserType userType) {
        log.info("创建用户: username={}, email={}, realName={}", username, email, realName);

        // 验证输入参数
        if (!validateUserInfo(username, email, mobile, realName, employeeId)) {
            log.error("用户信息验证失败");
            return null;
        }

        // 检查用户名是否已存在
        if (isUsernameExists(username)) {
            log.error("用户名已存在: {}", username);
            return null;
        }

        // 检查邮箱是否已存在
        if (isEmailExists(email)) {
            log.error("邮箱已存在: {}", email);
            return null;
        }

        // 创建用户对象
        UserInfo user = new UserInfo(
                "USER_" + System.currentTimeMillis(),
                username,
                email,
                realName);

        user.setMobile(mobile);
        user.setEmployeeId(employeeId);
        user.setDepartmentId(departmentId);
        user.setUserType(userType);
        user.setPasswordUpdateTime(LocalDateTime.now());

        // 设置账户过期时间（可选）
        user.setAccountExpireTime(LocalDateTime.now().plusYears(1));

        // 保存用户（实际应该保存到数据库）
        // userRepository.save(user);

        log.info("用户创建成功: userId={}, username={}", user.getUserId(), username);
        return user;
    }

    /**
     * 更新用户基本信息
     */
    @CacheEvict(value = "users", key = "#userId")
    public boolean updateUser(String userId, String email, String mobile, String realName,
            String departmentId, String position) {
        log.info("更新用户信息: userId={}, email={}", userId, email);

        UserInfo user = getUser(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            return false;
        }

        // 更新基本信息
        user.setEmail(email);
        user.setMobile(mobile);
        user.setRealName(realName);
        user.setDepartmentId(departmentId);
        user.setPosition(position);
        user.setLastUpdateTime(LocalDateTime.now());

        log.info("用户信息更新成功: userId={}", userId);
        return true;
    }

    /**
     * 更新用户状态
     */
    @CacheEvict(value = "users", key = "#userId")
    public boolean updateUserStatus(String userId, UserStatus status, String reason) {
        log.info("更新用户状态: userId={}, status={}, reason={}", userId, status, reason);

        UserInfo user = getUser(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            return false;
        }

        UserStatus oldStatus = user.getStatus();
        user.setStatus(status);
        user.setLastUpdateTime(LocalDateTime.now());

        log.info("用户状态更新成功: userId={}, {} -> {}", userId, oldStatus, status);
        return true;
    }

    /**
     * 重置用户密码
     */
    public boolean resetPassword(String userId, String newPassword, String operatorId) {
        log.info("重置用户密码: userId={}, operator={}", userId, operatorId);

        UserInfo user = getUser(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            return false;
        }

        // 验证新密码强度
        if (!validatePasswordStrength(newPassword)) {
            log.error("新密码强度不够");
            return false;
        }

        // 更新密码（实际应该加密后保存）
        user.setPasswordUpdateTime(LocalDateTime.now());

        log.info("用户密码重置成功: userId={}", userId);
        return true;
    }

    /**
     * 锁定用户账户
     */
    public boolean lockUser(String userId, String reason, LocalDateTime lockUntil) {
        log.info("锁定用户账户: userId={}, reason={}, until={}", userId, reason, lockUntil);

        UserInfo user = getUser(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            return false;
        }

        user.setStatus(UserStatus.LOCKED);
        user.setLastUpdateTime(LocalDateTime.now());

        log.info("用户账户锁定成功: userId={}", userId);
        return true;
    }

    /**
     * 解锁用户账户
     */
    public boolean unlockUser(String userId, String operatorId) {
        log.info("解锁用户账户: userId={}, operator={}", userId, operatorId);

        UserInfo user = getUser(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            return false;
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setLastUpdateTime(LocalDateTime.now());

        log.info("用户账户解锁成功: userId={}", userId);
        return true;
    }

    /**
     * 删除用户
     */
    @CacheEvict(value = "users", key = "#userId")
    public boolean deleteUser(String userId, String operatorId, String reason) {
        log.info("删除用户: userId={}, operator={}, reason={}", userId, operatorId, reason);

        UserInfo user = getUser(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            return false;
        }

        // 检查删除权限（实际应该检查操作者权限）

        // 执行软删除
        user.setStatus(UserStatus.INACTIVE);
        user.setLastUpdateTime(LocalDateTime.now());

        log.info("用户删除成功: userId={}", userId);
        return true;
    }

    /**
     * 获取用户信息
     */
    @Cacheable(value = "users", key = "#userId")
    public UserInfo getUser(String userId) {
        log.info("获取用户信息: userId={}", userId);

        // 模拟数据 - 实际应该从数据库查询
        return createSampleUser(userId);
    }

    /**
     * 根据用户名获取用户
     */
    @Cacheable(value = "users", key = "'username:' + #username")
    public UserInfo getUserByUsername(String username) {
        log.info("根据用户名获取用户: username={}", username);

        // 模拟数据 - 实际应该从数据库查询
        return createSampleUser("USER_" + username.hashCode());
    }

    /**
     * 获取用户列表
     */
    @Cacheable(value = "userList", key = "#departmentId + '_' + #status + '_' + #pageNo + '_' + #pageSize")
    public List<UserInfo> getUserList(String departmentId, UserStatus status,
            Integer pageNo, Integer pageSize) {
        log.info("获取用户列表: departmentId={}, status={}, page={}/{}",
                departmentId, status, pageNo, pageSize);

        // 模拟数据 - 实际应该从数据库查询
        List<UserInfo> users = new ArrayList<>();

        for (int i = 1; i <= pageSize; i++) {
            UserInfo user = createSampleUser("USER_" + (pageNo - 1) * pageSize + i);
            if (status != null) {
                user.setStatus(status);
            }
            if (departmentId != null) {
                user.setDepartmentId(departmentId);
            }
            users.add(user);
        }

        return users;
    }

    /**
     * 搜索用户
     */
    public List<UserInfo> searchUsers(String keyword, String departmentId, UserStatus status,
            Integer pageNo, Integer pageSize) {
        log.info("搜索用户: keyword={}, departmentId={}, status={}", keyword, departmentId, status);

        // 模拟数据 - 实际应该从数据库查询
        List<UserInfo> users = new ArrayList<>();

        for (int i = 1; i <= Math.min(pageSize, 10); i++) {
            UserInfo user = createSampleUser("USER_" + i);
            user.setRealName(keyword + " - " + i);
            if (status != null) {
                user.setStatus(status);
            }
            if (departmentId != null) {
                user.setDepartmentId(departmentId);
            }
            users.add(user);
        }

        return users;
    }

    /**
     * 获取用户扩展信息
     */
    @Cacheable(value = "userExtendedInfo", key = "#userId")
    public UserExtendedInfo getUserExtendedInfo(String userId) {
        log.info("获取用户扩展信息: userId={}", userId);

        // 模拟数据 - 实际应该从数据库查询
        UserExtendedInfo extendedInfo = new UserExtendedInfo(userId);
        extendedInfo.setGender("男");
        extendedInfo.setBirthday("1990-01-01");
        extendedInfo.setAddress("北京市朝阳区");
        extendedInfo.setEmergencyContact("张三");
        extendedInfo.setEmergencyPhone("13800138000");
        extendedInfo.setEducation("本科");
        extendedInfo.setSkills("Java, Spring Boot, MySQL");

        return extendedInfo;
    }

    /**
     * 更新用户扩展信息
     */
    @CacheEvict(value = "userExtendedInfo", key = "#userId")
    public boolean updateUserExtendedInfo(String userId, UserExtendedInfo extendedInfo) {
        log.info("更新用户扩展信息: userId={}", userId);

        UserInfo user = getUser(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            return false;
        }

        // 保存扩展信息（实际应该保存到数据库）
        extendedInfo.setUserId(userId);

        log.info("用户扩展信息更新成功: userId={}", userId);
        return true;
    }

    /**
     * 批量操作用户
     */
    public Map<String, Boolean> batchOperateUsers(List<String> userIds, String operation,
            Map<String, Object> params, String operatorId) {
        log.info("批量操作用户: operation={}, count={}, operator={}", operation, userIds.size(), operatorId);

        Map<String, Boolean> results = new HashMap<>();

        for (String userId : userIds) {
            boolean success = false;
            try {
                switch (operation.toLowerCase()) {
                    case "activate":
                        success = updateUserStatus(userId, UserStatus.ACTIVE, "批量激活");
                        break;
                    case "deactivate":
                        success = updateUserStatus(userId, UserStatus.INACTIVE, "批量禁用");
                        break;
                    case "lock":
                        success = updateUserStatus(userId, UserStatus.LOCKED, "批量锁定");
                        break;
                    case "delete":
                        success = deleteUser(userId, operatorId, "批量删除");
                        break;
                    default:
                        log.warn("未知的批量操作: {}", operation);
                        break;
                }
            } catch (Exception e) {
                log.error("批量操作失败: userId={}, operation={}", userId, operation, e);
                success = false;
            }
            results.put(userId, success);
        }

        long successCount = results.values().stream().mapToLong(b -> b ? 1 : 0).sum();
        log.info("批量操作完成: 总数={}, 成功={}", userIds.size(), successCount);

        return results;
    }

    /**
     * 获取用户统计信息
     */
    public Map<String, Object> getUserStatistics(String departmentId, String timeRange) {
        log.info("获取用户统计信息: departmentId={}, timeRange={}", departmentId, timeRange);

        Map<String, Object> statistics = new HashMap<>();

        // 总用户数
        statistics.put("totalUsers", 1250);

        // 活跃用户数
        statistics.put("activeUsers", 980);

        // 今日新增用户数
        statistics.put("todayNewUsers", 15);

        // 本月新增用户数
        statistics.put("monthNewUsers", 120);

        // 用户类型分布
        Map<String, Integer> userTypeDistribution = new HashMap<>();
        userTypeDistribution.put("ADMIN", 10);
        userTypeDistribution.put("MANAGER", 50);
        userTypeDistribution.put("EMPLOYEE", 1180);
        userTypeDistribution.put("CONTRACTOR", 8);
        userTypeDistribution.put("INTERN", 2);
        statistics.put("userTypeDistribution", userTypeDistribution);

        // 用户状态分布
        Map<String, Integer> userStatusDistribution = new HashMap<>();
        userStatusDistribution.put("ACTIVE", 1100);
        userStatusDistribution.put("INACTIVE", 100);
        userStatusDistribution.put("LOCKED", 30);
        userStatusDistribution.put("EXPIRED", 15);
        userStatusDistribution.put("PENDING", 5);
        statistics.put("userStatusDistribution", userStatusDistribution);

        return statistics;
    }

    // 私有辅助方法

    /**
     * 验证用户信息
     */
    private boolean validateUserInfo(String username, String email, String mobile,
            String realName, String employeeId) {
        // 验证用户名
        if (username == null || username.length() < 3 || username.length() > 50) {
            log.error("用户名长度必须在3-50字符之间");
            return false;
        }

        // 验证邮箱格式
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (email == null || !Pattern.matches(emailPattern, email)) {
            log.error("邮箱格式不正确");
            return false;
        }

        // 验证手机号格式（简化）
        if (mobile != null && !mobile.matches("^1[3-9]\\d{9}$")) {
            log.error("手机号格式不正确");
            return false;
        }

        // 验证真实姓名
        if (realName == null || realName.trim().isEmpty()) {
            log.error("真实姓名不能为空");
            return false;
        }

        return true;
    }

    /**
     * 验证密码强度
     */
    private boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // 至少包含大写字母、小写字母、数字
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            if (Character.isLowerCase(c))
                hasLower = true;
            if (Character.isDigit(c))
                hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * 检查用户名是否已存在
     */
    private boolean isUsernameExists(String username) {
        // 模拟检查 - 实际应该查询数据库
        return false;
    }

    /**
     * 检查邮箱是否已存在
     */
    private boolean isEmailExists(String email) {
        // 模拟检查 - 实际应该查询数据库
        return false;
    }

    /**
     * 创建示例用户
     */
    private UserInfo createSampleUser(String userId) {
        UserInfo user = new UserInfo(
                userId,
                "user_" + userId.substring(5),
                userId.toLowerCase() + "@company.com",
                "员工" + userId.substring(5));

        user.setMobile("1380013" + userId.substring(5));
        user.setEmployeeId("EMP" + userId.substring(5));
        user.setDepartmentId("DEPT_001");
        user.setDepartmentName("技术部");
        user.setPosition("软件工程师");
        user.setAvatar("/avatars/" + userId + ".jpg");
        user.setLastLoginTime(LocalDateTime.now().minusHours(2));
        user.setLastLoginIp("192.168.1.100");
        user.setLoginCount(50);

        return user;
    }

    /**
     * 获取用户类型列表
     */
    public List<Map<String, Object>> getUserTypes() {
        return Arrays.stream(UserType.values())
                .map(type -> {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("code", type.name());
                    typeInfo.put("description", type.getDescription());
                    typeInfo.put("comment", type.getComment());
                    return typeInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户状态列表
     */
    public List<Map<String, Object>> getUserStatuses() {
        return Arrays.stream(UserStatus.values())
                .map(status -> {
                    Map<String, Object> statusInfo = new HashMap<>();
                    statusInfo.put("code", status.name());
                    statusInfo.put("description", status.getDescription());
                    statusInfo.put("comment", status.getComment());
                    return statusInfo;
                })
                .collect(Collectors.toList());
    }
}
