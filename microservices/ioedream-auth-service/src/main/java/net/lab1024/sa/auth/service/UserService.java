package net.lab1024.sa.auth.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.auth.domain.entity.RoleEntity;
import net.lab1024.sa.auth.domain.entity.UserEntity;
import net.lab1024.sa.auth.domain.entity.UserRoleEntity;
import net.lab1024.sa.auth.domain.vo.RegisterRequest;
import net.lab1024.sa.auth.domain.vo.UserCreateRequest;
import net.lab1024.sa.auth.domain.vo.UserUpdateRequest;
import net.lab1024.sa.auth.repository.RoleRepository;
import net.lab1024.sa.auth.repository.UserRepository;
import net.lab1024.sa.auth.repository.UserRoleRepository;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * 用户服务
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 响应结果
     */
    @Transactional
    public ResponseDTO<Void> register(RegisterRequest request) {
        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseDTO.error("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
                return ResponseDTO.error("邮箱已被使用");
            }

            // 检查手机号是否已存在
            if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
                return ResponseDTO.error("手机号已被使用");
            }

            // 创建用户
            UserEntity user = UserEntity.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .realName(request.getRealName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .status(1) // 默认启用
                    .build();

            user = userRepository.save(user);

            // 分配默认角色
            assignDefaultRole(user);

            log.info("用户注册成功: userId={}, username={}", user.getUserId(), user.getUsername());
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("用户注册失败: username={}, error={}", request.getUsername(), e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户注册异常: username={}", request.getUsername(), e);
            return ResponseDTO.error("注册失败，请稍后重试");
        }
    }

    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 响应结果
     */
    @Transactional
    @CacheEvict(value = "user:session", allEntries = true)
    public ResponseDTO<UserEntity> createUser(UserCreateRequest request) {
        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseDTO.error("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
                return ResponseDTO.error("邮箱已被使用");
            }

            // 检查手机号是否已存在
            if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
                return ResponseDTO.error("手机号已被使用");
            }

            // 创建用户
            UserEntity user = UserEntity.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .realName(request.getRealName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .avatarUrl(request.getAvatarUrl())
                    .status(request.getStatus() != null ? request.getStatus() : 1)
                    .build();

            user = userRepository.save(user);

            // 分配角色
            if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                assignRoles(user, request.getRoleIds());
            } else {
                assignDefaultRole(user);
            }

            log.info("用户创建成功: userId={}, username={}", user.getUserId(), user.getUsername());
            return ResponseDTO.ok(user);

        } catch (BusinessException e) {
            log.warn("用户创建失败: username={}, error={}", request.getUsername(), e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户创建异常: username={}", request.getUsername(), e);
            return ResponseDTO.error("创建用户失败，请稍后重试");
        }
    }

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param request 更新用户请求
     * @return 响应结果
     */
    @Transactional
    @CacheEvict(value = { "user:session", "user:permissions" }, allEntries = true)
    public ResponseDTO<UserEntity> updateUser(Long userId, UserUpdateRequest request) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 检查用户名是否被其他用户使用
            if (!user.getUsername().equals(request.getUsername())
                    && userRepository.existsByUsername(request.getUsername())) {
                return ResponseDTO.error("用户名已被其他用户使用");
            }

            // 检查邮箱是否被其他用户使用
            if (request.getEmail() != null
                    && !request.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                return ResponseDTO.error("邮箱已被其他用户使用");
            }

            // 检查手机号是否被其他用户使用
            if (request.getPhone() != null
                    && !request.getPhone().equals(user.getPhone())
                    && userRepository.existsByPhone(request.getPhone())) {
                return ResponseDTO.error("手机号已被其他用户使用");
            }

            // 更新用户信息
            user.setUsername(request.getUsername());
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setAvatarUrl(request.getAvatarUrl());
            user.setStatus(request.getStatus());

            user = userRepository.save(user);

            // 更新角色分配
            if (request.getRoleIds() != null) {
                updateUserRoles(user, request.getRoleIds());
            }

            log.info("用户更新成功: userId={}, username={}", user.getUserId(), user.getUsername());
            return ResponseDTO.ok(user);

        } catch (BusinessException e) {
            log.warn("用户更新失败: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户更新异常: userId={}", userId, e);
            return ResponseDTO.error("更新用户失败，请稍后重试");
        }
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 响应结果
     */
    @Transactional
    @CacheEvict(value = { "user:session", "user:permissions" }, allEntries = true)
    public ResponseDTO<Void> deleteUser(Long userId) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 检查是否为内置用户
            if ("admin".equals(user.getUsername())) {
                return ResponseDTO.error("系统管理员不能删除");
            }

            // 软删除用户
            user.setDeletedFlag(1);
            userRepository.save(user);

            log.info("用户删除成功: userId={}, username={}", userId, user.getUsername());
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("用户删除失败: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户删除异常: userId={}", userId, e);
            return ResponseDTO.error("删除用户失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取用户
     *
     * @param userId 用户ID
     * @return 用户实体响应
     */
    @Cacheable(value = "user:session", key = "#userId", unless = "#result == null")
    public ResponseDTO<UserEntity> getUserById(Long userId) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 隐藏敏感信息
            user.setPassword(null);

            return ResponseDTO.ok(user);

        } catch (BusinessException e) {
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取用户失败: userId={}", userId, e);
            return ResponseDTO.error("获取用户失败，请稍后重试");
        }
    }

    /**
     * 分页查询用户
     *
     * @param keyword  关键词
     * @param status   状态
     * @param pageable 分页参数
     * @return 用户分页结果
     */
    public ResponseDTO<Page<UserEntity>> getUserList(String keyword, Integer status, Pageable pageable) {
        try {
            Page<UserEntity> users;

            if (keyword != null && !keyword.trim().isEmpty()) {
                // 按关键词搜索
                users = userRepository.findByKeywordAndStatus(keyword.trim(), status, pageable);
            } else if (status != null) {
                // 按状态筛选
                users = userRepository.findByStatus(status, pageable);
            } else {
                // 查询所有
                users = userRepository.findAll(pageable);
            }

            // 隐藏敏感信息
            users.getContent().forEach(user -> user.setPassword(null));

            return ResponseDTO.ok(users);

        } catch (Exception e) {
            log.error("查询用户列表失败: keyword={}, status={}", keyword, status, e);
            return ResponseDTO.error("查询用户列表失败，请稍后重试");
        }
    }

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 响应结果
     */
    @Transactional
    public ResponseDTO<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 验证当前密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseDTO.error("当前密码错误");
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordUpdateTime(LocalDateTime.now());
            userRepository.save(user);

            // 强制用户重新登录（清除会话）
            clearUserSession(userId);

            log.info("用户密码修改成功: userId={}", userId);
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("修改密码失败: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码异常: userId={}", userId, e);
            return ResponseDTO.error("修改密码失败，请稍后重试");
        }
    }

    /**
     * 重置密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码
     * @return 响应结果
     */
    @Transactional
    public ResponseDTO<Void> resetPassword(Long userId, String newPassword) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordUpdateTime(LocalDateTime.now());
            userRepository.save(user);

            // 清除用户会话
            clearUserSession(userId);

            log.info("用户密码重置成功: userId={}", userId);
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("重置密码失败: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("重置密码异常: userId={}", userId, e);
            return ResponseDTO.error("重置密码失败，请稍后重试");
        }
    }

    /**
     * 启用/禁用用户
     *
     * @param userId 用户ID
     * @param status 状态（1-启用，0-禁用）
     * @return 响应结果
     */
    @Transactional
    @CacheEvict(value = "user:session", allEntries = true)
    public ResponseDTO<Void> updateUserStatus(Long userId, Integer status) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 检查是否为系统管理员
            if ("admin".equals(user.getUsername()) && status == 0) {
                return ResponseDTO.error("系统管理员不能禁用");
            }

            user.setStatus(status);
            userRepository.save(user);

            // 如果禁用用户，清除会话
            if (status == 0) {
                clearUserSession(userId);
            }

            String action = status == 1 ? "启用" : "禁用";
            log.info("用户{}成功: userId={}, username={}", action, userId, user.getUsername());
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("更新用户状态失败: userId={}, status={}, error={}", userId, status, e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户状态异常: userId={}, status={}", userId, status, e);
            return ResponseDTO.error("更新用户状态失败，请稍后重试");
        }
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    public UserEntity getUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            log.error("根据用户名获取用户失败: username={}", username, e);
            return null;
        }
    }

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    public Set<String> getUserPermissions(Long userId) {
        try {
            List<String> permissions = userRepository.findUserPermissions(userId);
            return new HashSet<>(permissions);
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色集合
     */
    public Set<String> getUserRoles(Long userId) {
        try {
            List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(userId);
            return userRoles.stream()
                    .filter(UserRoleEntity::isValid)
                    .map(ur -> ur.getRole().getRoleCode())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("获取用户角色失败: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 更新用户最后登录信息
     *
     * @param userId     用户ID
     * @param deviceInfo 设备信息
     */
    public void updateLastLogin(Long userId, String deviceInfo) {
        try {
            userRepository.updateLastLoginInfo(userId, LocalDateTime.now(), deviceInfo);
            log.info("更新用户最后登录信息成功: userId={}, deviceInfo={}", userId, deviceInfo);
        } catch (Exception e) {
            log.error("更新用户最后登录信息失败: userId={}, deviceInfo={}", userId, deviceInfo, e);
        }
    }

    // 私有方法

    /**
     * 分配默认角色
     *
     * @param user 用户实体
     */
    private void assignDefaultRole(UserEntity user) {
        Optional<RoleEntity> defaultRole = roleRepository.findByRoleCodeAndStatus("USER", 1);
        if (defaultRole.isPresent()) {
            UserRoleEntity userRole = UserRoleEntity.builder()
                    .userId(user.getUserId())
                    .roleId(defaultRole.get().getRoleId())
                    .grantTime(LocalDateTime.now())
                    .build();

            userRoleRepository.save(userRole);
            log.info("分配默认角色成功: userId={}, roleCode={}", user.getUserId(), "USER");
        }
    }

    /**
     * 分配角色
     *
     * @param user    用户实体
     * @param roleIds 角色ID列表
     */
    private void assignRoles(UserEntity user, List<Long> roleIds) {
        // 删除现有角色
        userRoleRepository.deleteByUserId(user.getUserId());

        // 分配新角色
        List<RoleEntity> roles = roleRepository.findAllById(roleIds);
        List<UserRoleEntity> userRoles = roles.stream()
                .filter(RoleEntity::isEnabled)
                .map(role -> UserRoleEntity.builder()
                        .userId(user.getUserId())
                        .roleId(role.getRoleId())
                        .grantTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        userRoleRepository.saveAll(userRoles);
        log.info("分配角色成功: userId={}, roleCount={}", user.getUserId(), userRoles.size());
    }

    /**
     * 更新用户角色
     *
     * @param user    用户实体
     * @param roleIds 角色ID列表
     */
    private void updateUserRoles(UserEntity user, List<Long> roleIds) {
        // 删除现有角色
        userRoleRepository.deleteByUserId(user.getUserId());

        // 分配新角色
        if (!roleIds.isEmpty()) {
            assignRoles(user, roleIds);
        }
    }

    /**
     * 清除用户会话
     *
     * @param userId 用户ID
     */
    private void clearUserSession(Long userId) {
        // 这里可以集成Redis来清除用户会话
        // 具体实现取决于项目的缓存策略
        log.info("清除用户会话: userId={}", userId);
    }
}
