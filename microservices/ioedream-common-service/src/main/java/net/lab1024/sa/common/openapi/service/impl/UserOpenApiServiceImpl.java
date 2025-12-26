package net.lab1024.sa.common.openapi.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.auth.dao.UserDao;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.openapi.domain.request.ChangePasswordRequest;
import net.lab1024.sa.common.openapi.domain.request.LoginRequest;
import net.lab1024.sa.common.openapi.domain.request.UpdateUserProfileRequest;
import net.lab1024.sa.common.openapi.domain.request.UserExtendedInfoRequest;
import net.lab1024.sa.common.openapi.domain.request.UserQueryRequest;
import net.lab1024.sa.common.openapi.domain.response.LoginResponse;
import net.lab1024.sa.common.openapi.domain.response.RefreshTokenResponse;
import net.lab1024.sa.common.openapi.domain.response.TokenValidationResponse;
import net.lab1024.sa.common.openapi.domain.response.UserInfoResponse;
import net.lab1024.sa.common.openapi.domain.response.UserPermissionResponse;
import net.lab1024.sa.common.openapi.domain.response.UserProfileResponse;
import net.lab1024.sa.common.openapi.manager.SecurityManager;
import net.lab1024.sa.common.openapi.service.UserOpenApiService;
import net.lab1024.sa.common.organization.entity.UserEntity;
import net.lab1024.sa.common.security.util.PasswordStrengthValidator;

/**
 * 用户管理开放API服务实现类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class UserOpenApiServiceImpl implements UserOpenApiService {


    @Resource
    private UserDao userDao;
    @Resource
    private SecurityManager securityManager;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public LoginResponse authenticate(LoginRequest request, String clientIp) {
        log.info("[开放API] 用户认证请求: username={}, clientIp={}", request.getUsername(), clientIp);

        // 1. 参数验证
        if (request.getLoginType() == null) {
            request.setLoginType("password");
        }

        // 2. 根据登录类型进行认证
        UserEntity user = null;
        switch (request.getLoginType()) {
            case "password":
                user = authenticateByPassword(request.getUsername(), request.getPassword());
                break;
            case "sms":
                user = authenticateBySms(request.getPhone(), request.getCaptcha());
                break;
            case "oauth":
                user = authenticateByOAuth(request.getThirdPartyType(), request.getThirdPartyToken());
                break;
            default:
                throw new BusinessException("INVALID_LOGIN_TYPE", "不支持的登录类型");
        }

        if (user == null) {
            throw new BusinessException("INVALID_CREDENTIALS", "用户名或密码错误");
        }

        // 3. 检查用户状态
        validateUserStatus(user);

        // 4. 生成访问令牌
        String accessToken = jwtTokenUtil.generateAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUserId());

        // 5. 更新用户登录信息
        updateUserLoginInfo(user, clientIp);

        // 6. 获取用户权限和角色
        List<String> permissions = securityManager.getUserPermissions(user.getUserId());
        List<String> roles = securityManager.getUserRoles(user.getUserId());

        // 7. 构建响应
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .departmentId(securityManager.getUserDepartmentId(user.getUserId()))
                .departmentName(securityManager.getUserDepartmentName(user.getUserId()))
                .position(securityManager.getUserPosition(user.getUserId()))
                .employeeNo(securityManager.getUserEmployeeNo(user.getUserId()))
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .accessTokenExpireTime(LocalDateTime.now().plusSeconds(7200))
                .refreshTokenExpireTime(LocalDateTime.now().plusDays(7))
                .userInfo(userInfo)
                .permissions(permissions)
                .roles(roles)
                .build();
    }

    @Override
    public RefreshTokenResponse refreshToken(String refreshToken) {
        log.info("[开放API] 刷新访问令牌");

        // 1. 验证刷新令牌
        if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "无效的刷新令牌");
        }

        // 2. 从刷新令牌中获取用户信息
        Long userId = jwtTokenUtil.getUserIdFromRefreshToken(refreshToken);
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 3. 检查用户状态
        validateUserStatus(user);

        // 4. 生成新的访问令牌
        String newAccessToken = jwtTokenUtil.generateAccessToken(user.getUserId(), user.getUsername());
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(user.getUserId());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .accessTokenExpireTime(LocalDateTime.now().plusSeconds(7200))
                .refreshTokenExpireTime(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Override
    public void logout(String token, String clientIp) {
        log.info("[开放API] 用户退出登录, clientIp={}", clientIp);

        // 1. 验证访问令牌
        if (!jwtTokenUtil.validateAccessToken(token)) {
            return; // 令牌已无效，直接返回
        }

        // 2. 从令牌中获取用户信息
        Long userId = jwtTokenUtil.getUserIdFromAccessToken(token);

        // 3. 将令牌加入黑名单
        jwtTokenUtil.revokeToken(token);

        // 4. 记录退出日志
        log.info("[开放API] 用户退出登录成功: userId={}, clientIp={}", userId, clientIp);
    }

    @Override
    public UserProfileResponse getUserProfile(String token) {
        log.info("[开放API] 获取用户详细信息");

        // 1. 验证访问令牌
        if (!jwtTokenUtil.validateAccessToken(token)) {
            throw new BusinessException("INVALID_TOKEN", "无效的访问令牌");
        }

        // 2. 获取用户信息
        Long userId = jwtTokenUtil.getUserIdFromAccessToken(token);
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 3. 获取用户权限和角色
        List<UserProfileResponse.PermissionInfo> permissions = securityManager
                .getUserPermissionsWithDetails(userId)
                .stream()
                .map(this::convertToPermissionInfo)
                .collect(Collectors.toList());

        List<UserProfileResponse.RoleInfo> roles = securityManager
                .getUserRolesWithDetails(userId)
                .stream()
                .map(this::convertToRoleInfo)
                .collect(Collectors.toList());

        // 4. 构建响应
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .gender(securityManager.getUserGender(userId))
                .birthday(securityManager.getUserBirthday(userId) != null
                        ? securityManager.getUserBirthday(userId).toString()
                        : null)
                .departmentId(securityManager.getUserDepartmentId(userId))
                .departmentName(securityManager.getUserDepartmentName(userId))
                .position(securityManager.getUserPosition(userId))
                .employeeNo(securityManager.getUserEmployeeNo(userId))
                .status(user.getStatus())
                .accountLocked(user.getAccountLocked() != null && user.getAccountLocked() ? 1 : 0) // Boolean转Integer（getAccountLocked返回Boolean）
                .lockReason(user.getLockReason())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .passwordUpdateTime(user.getPasswordUpdateTime())
                .accountExpireTime(user.getAccountExpireTime())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .remark(user.getRemark())
                .permissions(permissions)
                .roles(roles)
                .build();
    }

    @Override
    public UserProfileResponse updateUserProfile(String token, UpdateUserProfileRequest request, String clientIp) {
        log.info("[开放API] 更新用户信息, clientIp={}", clientIp);

        // 1. 验证访问令牌
        if (!jwtTokenUtil.validateAccessToken(token)) {
            throw new BusinessException("INVALID_TOKEN", "无效的访问令牌");
        }

        // 2. 获取用户信息
        Long userId = jwtTokenUtil.getUserIdFromAccessToken(token);
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 3. 更新用户信息
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getRemark() != null) {
            user.setRemark(request.getRemark());
        }

        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateUserId(userId);

        // 4. 保存更新
        userDao.updateById(user);

        // 5. 更新扩展信息
        UserExtendedInfoRequest extendedInfo = new UserExtendedInfoRequest();
        extendedInfo.setGender(request.getGender());
        if (request.getBirthday() != null) {
            extendedInfo.setBirthday(java.time.LocalDate.parse(request.getBirthday()));
        }
        extendedInfo.setDepartmentId(request.getDepartmentId());
        extendedInfo.setPosition(request.getPosition());
        extendedInfo.setEmployeeNo(request.getEmployeeNo());
        extendedInfo.setRemark(request.getRemark());
        securityManager.updateUserExtendedInfo(userId, extendedInfo);

        log.info("[开放API] 用户信息更新成功: userId={}", userId);

        // 6. 返回更新后的用户信息
        return getUserProfile(token);
    }

    @Override
    public void changePassword(String token, ChangePasswordRequest request, String clientIp) {
        log.info("[开放API] 用户修改密码, clientIp={}", clientIp);

        // 1. 验证访问令牌
        if (!jwtTokenUtil.validateAccessToken(token)) {
            throw new BusinessException("INVALID_TOKEN", "无效的访问令牌");
        }

        // 2. 验证参数
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("PASSWORD_MISMATCH", "新密码与确认密码不匹配");
        }

        // 3. 获取用户信息
        Long userId = jwtTokenUtil.getUserIdFromAccessToken(token);
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 4. 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_OLD_PASSWORD", "原密码错误");
        }

        // 5. 检查新密码是否与原密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("SAME_PASSWORD", "新密码不能与原密码相同");
        }

        // 5.5. 验证新密码强度（P1-8.1）
        PasswordStrengthValidator.ValidationResult validationResult =
                PasswordStrengthValidator.validate(request.getNewPassword());
        if (!validationResult.isValid()) {
            log.warn("[开放API] 密码强度验证失败: userId={}, errors={}",
                    user.getUserId(), validationResult.getErrorMessage());
            throw new BusinessException("PASSWORD_WEAK",
                    "密码强度不符合要求: " + validationResult.getErrorMessage());
        }
        log.info("[开放API] 密码强度验证通过: userId={}, strength={}",
                user.getUserId(), validationResult.getStrength().getDescription());

        // 6. 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateUserId(userId);

        userDao.updateById(user);

        log.info("[开放API] 密码修改成功: userId={}", userId);
    }

    @Override
    public UserInfoResponse getUserById(Long userId, String token) {
        log.info("[开放API] 查询用户信息: userId={}", userId);

        // 1. 验证访问令牌
        if (!jwtTokenUtil.validateAccessToken(token)) {
            throw new BusinessException("INVALID_TOKEN", "无效的访问令牌");
        }

        // 2. 检查权限
        Long currentUserId = jwtTokenUtil.getUserIdFromAccessToken(token);
        if (!userId.equals(currentUserId) && !hasPermission(token, "user:view")) {
            throw new BusinessException("PERMISSION_DENIED", "无权限查看用户信息");
        }

        // 3. 获取用户信息
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 4. 构建响应
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .gender(securityManager.getUserGender(userId))
                .departmentId(securityManager.getUserDepartmentId(userId))
                .departmentName(securityManager.getUserDepartmentName(userId))
                .position(securityManager.getUserPosition(userId))
                .employeeNo(securityManager.getUserEmployeeNo(userId))
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .remark(user.getRemark())
                .build();
    }

    @Override
    public PageResult<UserInfoResponse> getUserList(UserQueryRequest request, String token) {
        log.info("[开放API] 查询用户列表: pageNum={}, pageSize={}, keyword={}",
                request.getPageNum(), request.getPageSize(), request.getKeyword());

        // 1. 验证访问令牌
        if (!jwtTokenUtil.validateAccessToken(token)) {
            throw new BusinessException("INVALID_TOKEN", "无效的访问令牌");
        }

        // 2. 检查权限
        if (!hasPermission(token, "user:list")) {
            throw new BusinessException("PERMISSION_DENIED", "无权限查看用户列表");
        }

        // 3. 构建查询条件
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(UserEntity::getUsername, keyword)
                    .or()
                    .like(UserEntity::getRealName, keyword)
                    .or()
                    .like(UserEntity::getPhone, keyword)
                    .or()
                    .like(UserEntity::getEmail, keyword));
        }

        if (request.getStatus() != null) {
            queryWrapper.eq(UserEntity::getStatus, request.getStatus());
        }

        if (request.getDepartmentId() != null) {
            queryWrapper.eq(UserEntity::getDepartmentId, request.getDepartmentId());
        }

        queryWrapper.orderByDesc(UserEntity::getCreateTime);

        // 4. 分页查询
        Page<UserEntity> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<UserEntity> userPage = userDao.selectPage(page, queryWrapper);

        // 5. 转换结果
        List<UserInfoResponse> userInfoList = userPage.getRecords().stream()
                .map(this::convertToUserInfoResponse)
                .collect(Collectors.toList());

        // 6. 构建分页响应
        return PageResult.of(
                userInfoList,
                userPage.getTotal(),
                (int) userPage.getCurrent(),
                (int) userPage.getSize());
    }

    @Override
    public UserPermissionResponse getUserPermissions(Long userId, String token) {
        log.info("[开放API] 查询用户权限: userId={}", userId);

        // 1. 验证访问令牌
        if (!jwtTokenUtil.validateAccessToken(token)) {
            throw new BusinessException("INVALID_TOKEN", "无效的访问令牌");
        }

        // 2. 检查权限
        Long currentUserId = jwtTokenUtil.getUserIdFromAccessToken(token);
        if (!userId.equals(currentUserId) && !hasPermission(token, "permission:view")) {
            throw new BusinessException("PERMISSION_DENIED", "无权限查看用户权限");
        }

        // 3. 获取用户信息
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 4. 获取权限和角色详情
        List<UserPermissionResponse.PermissionDetail> permissions = securityManager
                .getUserPermissionsWithDetails(userId)
                .stream()
                .map(this::convertToPermissionDetail)
                .collect(Collectors.toList());

        List<UserPermissionResponse.RoleDetail> roles = securityManager
                .getUserRolesWithDetails(userId)
                .stream()
                .map(this::convertToRoleDetail)
                .collect(Collectors.toList());

        // 5. 构建响应
        return UserPermissionResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .permissions(permissions)
                .roles(roles)
                .build();
    }

    @Override
    public List<String> getUserRoles(Long userId, String token) {
        log.info("[开放API] 查询用户角色: userId={}", userId);

        // 1. 验证访问令牌
        if (!jwtTokenUtil.validateAccessToken(token)) {
            throw new BusinessException("INVALID_TOKEN", "无效的访问令牌");
        }

        // 2. 检查权限
        Long currentUserId = jwtTokenUtil.getUserIdFromAccessToken(token);
        if (!userId.equals(currentUserId) && !hasPermission(token, "role:view")) {
            throw new BusinessException("PERMISSION_DENIED", "无权限查看用户角色");
        }

        // 3. 获取角色列表
        return securityManager.getUserRoles(userId);
    }

    @Override
    public boolean checkPermission(String token, String permission) {
        log.info("[开放API] 检查用户权限: permission={}", permission);

        // 1. 验证访问令牌
        if (!jwtTokenUtil.validateAccessToken(token)) {
            return false;
        }

        // 2. 获取用户ID并检查权限
        Long userId = jwtTokenUtil.getUserIdFromAccessToken(token);
        return securityManager.hasPermission(userId, permission);
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        log.info("[开放API] 验证访问令牌");

        try {
            // 1. 验证令牌
            if (!jwtTokenUtil.validateAccessToken(token)) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .tokenType("access_token")
                        .errorMessage("令牌无效或已过期")
                        .build();
            }

            // 2. 获取令牌信息
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            String username = jwtTokenUtil.getUsernameFromToken(token);
            Long expireTimestamp = jwtTokenUtil.getExpirationFromAccessToken(token);
            Long issueTimestamp = jwtTokenUtil.getIssuedAtFromAccessToken(token);
            LocalDateTime expireTime = expireTimestamp != null
                    ? java.time.Instant.ofEpochMilli(expireTimestamp).atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
                    : null;
            LocalDateTime issueTime = issueTimestamp != null
                    ? java.time.Instant.ofEpochMilli(issueTimestamp).atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
                    : null;
            long remainingTime = jwtTokenUtil.getRemainingTimeFromAccessToken(token);

            // 3. 检查用户是否存在且有效
            UserEntity user = userDao.selectById(userId);

            if (user == null || user.getStatus() != 1) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .tokenType("access_token")
                        .errorMessage("用户不存在或已被禁用")
                        .build();
            }

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .username(username)
                    .expireTime(expireTime)
                    .remainingTime(remainingTime)
                    .tokenType("access_token")
                    .issueTime(issueTime)
                    .build();

        } catch (Exception e) {
            log.error("[开放API] 令牌验证异常: {}", e.getMessage(), e);
            return TokenValidationResponse.builder()
                    .valid(false)
                    .tokenType("access_token")
                    .errorMessage("令牌验证失败")
                    .build();
        }
    }

    // ========== 私有方法 ==========

    /**
     * 用户名密码认证
     */
    private UserEntity authenticateByPassword(String username, String password) {
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getUsername, username);
        UserEntity user = userDao.selectOne(queryWrapper);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    /**
     * 短信验证码认证
     */
    private UserEntity authenticateBySms(String phone, String captcha) {
        // TODO: 实现短信验证码认证逻辑
        throw new BusinessException("NOT_IMPLEMENTED", "短信验证码认证暂未实现");
    }

    /**
     * OAuth认证
     */
    private UserEntity authenticateByOAuth(String thirdPartyType, String thirdPartyToken) {
        // TODO: 实现OAuth认证逻辑
        throw new BusinessException("NOT_IMPLEMENTED", "OAuth认证暂未实现");
    }

    /**
     * 验证用户状态
     */
    private void validateUserStatus(UserEntity user) {
        if (user.getStatus() != 1) {
            throw new BusinessException("USER_DISABLED", "用户已被禁用");
        }
        Boolean isLocked = user.getAccountLocked();
        if (isLocked != null && isLocked) {
            throw new BusinessException("USER_LOCKED", "用户账户已被锁定：" + user.getLockReason());
        }
        if (user.getAccountExpireTime() != null && user.getAccountExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("ACCOUNT_EXPIRED", "用户账户已过期");
        }
    }

    /**
     * 更新用户登录信息
     */
    private void updateUserLoginInfo(UserEntity user, String clientIp) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        // loginFailCount, lockTime, unlockTime字段在UserEntity中不存在，需要从其他表或扩展字段获取
        user.setLockReason(null);
        user.setAccountLocked(false); // Boolean类型，false表示未锁定

        userDao.updateById(user);
    }

    /**
     * 检查用户权限
     */
    private boolean hasPermission(String token, String permission) {
        Long userId = jwtTokenUtil.getUserIdFromAccessToken(token);
        return securityManager.hasPermission(userId, permission);
    }

    /**
     * 转换为用户信息响应
     */
    private UserInfoResponse convertToUserInfoResponse(UserEntity user) {
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .gender(securityManager.getUserGender(user.getUserId()))
                .departmentId(securityManager.getUserDepartmentId(user.getUserId()))
                .departmentName(securityManager.getUserDepartmentName(user.getUserId()))
                .position(securityManager.getUserPosition(user.getUserId()))
                .employeeNo(securityManager.getUserEmployeeNo(user.getUserId()))
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .remark(user.getRemark())
                .build();
    }

    /**
     * 转换为权限信息
     */
    private UserProfileResponse.PermissionInfo convertToPermissionInfo(Object permission) {
        // TODO: 实现权限信息转换逻辑
        UserProfileResponse.PermissionInfo info = new UserProfileResponse.PermissionInfo();
        BeanUtils.copyProperties(permission, info);
        return info;
    }

    /**
     * 转换为角色信息
     */
    private UserProfileResponse.RoleInfo convertToRoleInfo(Object role) {
        // TODO: 实现角色信息转换逻辑
        UserProfileResponse.RoleInfo info = new UserProfileResponse.RoleInfo();
        BeanUtils.copyProperties(role, info);
        return info;
    }

    /**
     * 转换为权限详情
     */
    private UserPermissionResponse.PermissionDetail convertToPermissionDetail(Object permission) {
        // TODO: 实现权限详情转换逻辑
        UserPermissionResponse.PermissionDetail detail = new UserPermissionResponse.PermissionDetail();
        BeanUtils.copyProperties(permission, detail);
        return detail;
    }

    /**
     * 转换为角色详情
     */
    private UserPermissionResponse.RoleDetail convertToRoleDetail(Object role) {
        // TODO: 实现角色详情转换逻辑
        UserPermissionResponse.RoleDetail detail = new UserPermissionResponse.RoleDetail();
        BeanUtils.copyProperties(role, detail);
        return detail;
    }
}

