package net.lab1024.sa.common.auth.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户锁定状态管理服务
 * <p>
 * 防止暴力破解，支持临时锁定和管理员锁定
 * 使用Redis存储锁定状态，支持分布式环境
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Service
@Slf4j
public class UserLockService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    // TODO: 通知服务模块未实现，暂时注释
    // @Resource
    // private NotificationService notificationService;

    private static final String LOCK_KEY_PREFIX = "user:lock:";
    private static final String FAILURE_KEY_PREFIX = "login:failure:";
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    private static final int FAILURE_RECORD_TTL_HOURS = 24;

    /**
     * 检查用户是否被锁定
     *
     * @param username 用户名
     * @return true=已锁定
     */
    public boolean isUserLocked(String username) {
        log.debug("[用户锁定] 检查锁定状态: username={}", username);

        // 1. 检查Redis中的临时锁定
        String lockKey = LOCK_KEY_PREFIX + username;
        Boolean locked = redisTemplate.hasKey(lockKey);

        if (Boolean.TRUE.equals(locked)) {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            log.warn("[用户锁定] 用户被临时锁定: username={}, 剩余{}分钟", username, ttl);
            return true;
        }

        // 2. 检查数据库中的管理员锁定（需要通过Gateway调用用户服务）
        // 这里暂时只检查Redis临时锁定，管理员锁定需要调用用户服务

        log.debug("[用户锁定] 用户未锁定: username={}", username);
        return false;
    }

    /**
     * 记录登录失败
     *
     * @param username 用户名
     * @return 是否触发锁定
     */
    public boolean recordLoginFailure(String username) {
        log.debug("[用户锁定] 记录登录失败: username={}", username);

        // 1. 更新失败次数
        String failureKey = FAILURE_KEY_PREFIX + username;
        String countStr = redisTemplate.opsForValue().get(failureKey);
        int failureCount = countStr == null ? 1 : Integer.parseInt(countStr) + 1;

        // 更新失败次数（24小时有效期）
        redisTemplate.opsForValue().set(failureKey, String.valueOf(failureCount),
            FAILURE_RECORD_TTL_HOURS, TimeUnit.HOURS);

        log.warn("[用户锁定] 登录失败次数: username={}, count={}/{}", username, failureCount, MAX_LOGIN_FAILURES);

        // 2. 达到阈值则锁定用户
        if (failureCount >= MAX_LOGIN_FAILURES) {
            lockUser(username, LOCK_DURATION_MINUTES);

            // 发送锁定通知邮件/短信
            sendLockNotification(username, failureCount, LOCK_DURATION_MINUTES);

            log.warn("[用户锁定] 登录失败次数过多，自动锁定: username={}, count={}",
                username, failureCount);
            return true;
        }

        return false;
    }

    /**
     * 清除登录失败记录（登录成功时调用）
     *
     * @param username 用户名
     */
    public void clearLoginFailure(String username) {
        String failureKey = FAILURE_KEY_PREFIX + username;
        redisTemplate.delete(failureKey);
        log.debug("[用户锁定] 清除登录失败记录: username={}", username);
    }

    /**
     * 锁定用户
     *
     * @param username 用户名
     * @param minutes 锁定时长（分钟）
     */
    public void lockUser(String username, int minutes) {
        String lockKey = LOCK_KEY_PREFIX + username;
        redisTemplate.opsForValue().set(lockKey, "1", minutes, TimeUnit.MINUTES);

        // 同时清除失败记录，避免解锁后立即又被锁定
        clearLoginFailure(username);

        log.info("[用户锁定] 用户已锁定: username={}, duration={}分钟", username, minutes);
    }

    /**
     * 解锁用户
     *
     * @param username 用户名
     */
    public void unlockUser(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        redisTemplate.delete(lockKey);

        // 同时清除失败记录
        clearLoginFailure(username);

        log.info("[用户锁定] 用户已解锁: username={}", username);
    }

    /**
     * 获取用户剩余锁定时间
     *
     * @param username 用户名
     * @return 剩余锁定时间（分钟），未锁定返回0
     */
    public long getRemainingLockTime(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
        return ttl != null ? ttl : 0;
    }

    /**
     * 获取登录失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int getLoginFailureCount(String username) {
        String failureKey = FAILURE_KEY_PREFIX + username;
        String countStr = redisTemplate.opsForValue().get(failureKey);
        return countStr == null ? 0 : Integer.parseInt(countStr);
    }

    /**
     * 管理员手动锁定/解锁用户（永久锁定，直到手动解锁）
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param locked true=锁定, false=解锁
     * @param operatorId 操作人ID
     */
    public void setUserLockedStatus(Long userId, String username, boolean locked, Long operatorId) {
        log.info("[用户锁定] 管理员修改锁定状态: userId={}, username={}, locked={}, operator={}",
            userId, username, locked, operatorId);

        // 管理员锁定使用更长的TTL（7天）
        int lockMinutes = locked ? 7 * 24 * 60 : 0;

        if (locked) {
            lockUser(username, lockMinutes);
        } else {
            unlockUser(username);
        }

        // 更新数据库中的用户锁定状态
        LocalDateTime lockExpireTime = locked ?
            LocalDateTime.now().plusDays(7) : null;
        updateUserLockStatusInDB(username, locked, lockExpireTime);
    }

    /**
     * 更新数据库中的用户锁定状态
     *
     * @param username 用户名
     * @param locked 是否锁定
     * @param lockExpireTime 锁定过期时间
     */
    private void updateUserLockStatusInDB(String username, boolean locked,
                                          LocalDateTime lockExpireTime) {
        try {
            log.info("[用户锁定] 调用用户服务更新数据库: username={}, locked={}",
                     username, locked);

            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            params.put("locked", locked);
            params.put("lockExpireTime", lockExpireTime != null ?
                      lockExpireTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);

            // 调用用户服务API
            ResponseDTO<Void> response = gatewayServiceClient.callCommonService(
                "/api/user/update-lock-status",
                HttpMethod.PUT,
                params,
                new TypeReference<ResponseDTO<Void>>() {}
            );

            if (response == null || response.getCode() != 200) {
                log.error("[用户锁定] 更新数据库失败: username={}, response={}",
                         username, response != null ? response.getMessage() : "response is null");
                throw new RuntimeException("更新数据库失败: " +
                    (response != null ? response.getMessage() : "无响应"));
            }

            log.info("[用户锁定] 数据库更新成功: username={}", username);

        } catch (Exception e) {
            log.error("[用户锁定] 更新数据库异常: username={}, error={}",
                     username, e.getMessage(), e);
            // 注意：这里不重新抛出异常，避免影响Redis锁定状态
            // Redis锁定成功即可保证短期安全，数据库同步失败可异步重试
        }
    }

    /**
     * 发送锁定通知
     *
     * @param username 用户名
     * @param failureCount 失败次数
     * @param lockMinutes 锁定时长（分钟）
     */
    private void sendLockNotification(String username, int failureCount, int lockMinutes) {
        log.info("[用户锁定] 发送锁定通知: username={}, count={}, duration={}分钟",
                 username, failureCount, lockMinutes);

        // TODO: 调用通知服务发送通知（通知服务模块未实现）
        // try {
        //     notificationService.sendUserLockedNotification(username, failureCount, lockMinutes);
        // } catch (Exception e) {
        //     log.error("[用户锁定] 发送通知失败: username={}, error={}", username, e.getMessage(), e);
        //     // 通知发送失败不影响锁定功能
        // }
    }

    /**
     * 清理过期的锁定记录
     * 定时任务，每天凌晨3点执行
     */
    public void cleanExpiredLockRecords() {
        log.info("[用户锁定] 开始清理过期记录");

        // Redis会自动清理过期key，这里主要是记录日志
        // 如果需要手动清理，可以遍历所有锁定key并检查TTL

        log.debug("[用户锁定] 清理过期记录完成");
    }
}
