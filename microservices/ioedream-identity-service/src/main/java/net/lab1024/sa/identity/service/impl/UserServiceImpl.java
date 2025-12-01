package net.lab1024.sa.identity.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.identity.domain.entity.UserEntity;
import net.lab1024.sa.identity.repository.UserRepository;
import net.lab1024.sa.identity.service.UserService;

/**
 * 用户服务实现类
 *
 * @since 2025-11-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "user", key = "#userId", unless = "#result == null")
    public ResponseDTO<UserEntity> getUserById(Long userId) {
        UserEntity user = userRepository.selectById(userId);
        if (user != null) {
            user.setPassword(null); // 隐藏密码
        }
        return ResponseDTO.ok(user);
    }

    @Override
    @Cacheable(value = "user:username", key = "#username")
    public UserEntity getUserByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username);
        return userRepository.selectOne(wrapper);
    }

    @Override
    public PageResult<UserEntity> getUserPage(PageParam pageParam, String keyword, Integer status) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(UserEntity::getUsername, keyword)
                    .or().like(UserEntity::getRealName, keyword)
                    .or().like(UserEntity::getEmail, keyword));
        }
        if (status != null) {
            wrapper.eq(UserEntity::getStatus, status);
        }
        wrapper.orderByDesc(UserEntity::getCreateTime);

        // 转换PageParam为IPage
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserEntity> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageParam.getPageNum(), pageParam.getPageSize());

        com.baomidou.mybatisplus.core.metadata.IPage<UserEntity> resultPage = userRepository.selectPage(page, wrapper);

        // 转换为PageResult
        PageResult<UserEntity> pageResult = new PageResult<>();
        pageResult.setList(resultPage.getRecords());
        pageResult.setTotal((long) resultPage.getTotal());
        pageResult.setPageNum(resultPage.getCurrent());
        pageResult.setPageSize(resultPage.getSize());

        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createUser(UserEntity user, List<Long> roleIds) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            return ResponseDTO.error("用户名已存在");
        }
        if (user.getEmail() != null && existsByEmail(user.getEmail())) {
            return ResponseDTO.error("邮箱已存在");
        }

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 加密密码
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user.setCreateTime(LocalDateTime.now());

        int result = userRepository.insert(user);
        if (result > 0) {
            log.info("用户创建成功: userId={}, username={}", user.getUserId(), user.getUsername());
            return ResponseDTO.ok("用户创建成功");
        } else {
            return ResponseDTO.error("用户创建失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user", key = "#user.userId")
    public ResponseDTO<String> updateUser(UserEntity user, List<Long> roleIds) {
        UserEntity existingUser = userRepository.selectById(user.getUserId());
        if (existingUser == null) {
            return ResponseDTO.error("用户不存在");
        }

        // 防止修改敏感字段
        user.setPassword(existingUser.getPassword());
        user.setCreateTime(existingUser.getCreateTime());

        int result = userRepository.updateById(user);
        if (result > 0) {
            log.info("用户更新成功: userId={}, username={}", user.getUserId(), user.getUsername());
            return ResponseDTO.ok("用户更新成功");
        } else {
            return ResponseDTO.error("用户更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user", key = "#userId")
    public ResponseDTO<String> deleteUser(Long userId) {
        UserEntity user = userRepository.selectById(userId);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }

        int result = userRepository.deleteById(userId);
        if (result > 0) {
            log.info("用户删除成功: userId={}, username={}", userId, user.getUsername());
            return ResponseDTO.ok("用户删除成功");
        } else {
            return ResponseDTO.error("用户删除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user", key = "#userId")
    public ResponseDTO<String> updateUserStatus(Long userId, Integer status) {
        UserEntity user = userRepository.selectById(userId);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }

        user.setStatus(status);
        int result = userRepository.updateById(user);

        if (result > 0) {
            String action = status == 1 ? "启用" : "禁用";
            log.info("用户{}成功: userId={}, username={}", action, userId, user.getUsername());
            return ResponseDTO.ok(action + "成功");
        } else {
            String action = status == 1 ? "启用" : "禁用";
            return ResponseDTO.error(action + "失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> resetPassword(Long userId, String newPassword) {
        UserEntity user = userRepository.selectById(userId);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordUpdateTime(LocalDateTime.now());

        int result = userRepository.updateById(user);
        if (result > 0) {
            log.info("密码重置成功: userId={}", userId);
            return ResponseDTO.ok("密码重置成功");
        } else {
            return ResponseDTO.error("密码重置失败");
        }
    }

    @Override
    @Cacheable(value = "user:permissions", key = "#userId")
    public Set<String> getUserPermissions(Long userId) {
        // 暂时返回空集合，后续集成权限系统
        return Set.of();
    }

    @Override
    @Cacheable(value = "user:roles", key = "#userId")
    public Set<String> getUserRoles(Long userId) {
        // 暂时返回空集合，后续集成角色系统
        return Set.of();
    }

    @Override
    @Cacheable(value = "user:exists:username", key = "#username")
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username);
        return userRepository.selectCount(wrapper) > 0;
    }

    @Override
    @Cacheable(value = "user:exists:email", key = "#email")
    public boolean existsByEmail(String email) {
        if (email == null)
            return false;
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getEmail, email);
        return userRepository.selectCount(wrapper) > 0;
    }
}
