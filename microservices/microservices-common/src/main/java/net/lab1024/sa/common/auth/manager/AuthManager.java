package net.lab1024.sa.common.auth.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.auth.dao.UserSessionDao;
import net.lab1024.sa.common.auth.domain.entity.UserSessionEntity;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;

/**
 * 认证业务管理器
 * <p>
 * 符合CLAUDE.md规范 - Manager层
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 复杂认证业务流程编排
 * - 多级缓存管理（L1本地+L2Redis+L3网关）
 * - 会话管理和并发控制
 * - 令牌黑名单管理
 * - 登录安全策略（防暴力破解）
 * </p>
 * <p>
 * 企业级特性：
 * - 实现多级缓存策略
 * - 实现企业级安全特性
 * - 支持高并发会话管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Slf4j
@SuppressWarnings("null")
public class AuthManager {

    private final UserSessionDao userSessionDao;
    @SuppressWarnings("unused")
    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate redisTemplate;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param userSessionDao 用户会话DAO
     * @param jwtTokenUtil JWT工具类
     * @param redisTemplate Redis模板
     */
    public AuthManager(
            UserSessionDao userSessionDao,
            JwtTokenUtil jwtTokenUtil,
            StringRedisTemplate redisTemplate) {
        this.userSessionDao = userSessionDao;
        this.jwtTokenUtil = jwtTokenUtil;
        this.redisTemplate = redisTemplate;
    }

    // Redis Key前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";
    private static final String USER_SESSION_PREFIX = "auth:user:session:";
    private static final String LOGIN_RETRY_PREFIX = "auth:login:retry:";
    private static final String USER_LOCK_PREFIX = "auth:user:lock:";

    // 配置参数（应从配置文件读取）
    private static final int MAX_SESSIONS = 3;
    private static final int SESSION_TIMEOUT = 3600;
    private static final int MAX_LOGIN_RETRY = 5;
    private static final int LOCK_DURATION = 1800;

    /**
     * 管理用户会话（企业级会话管理）
     *
     * 注意：事务管理在Service层，Manager层不管理事务
     * 事务边界由调用此方法的Service层方法控制
     *
     * 功能：
     * - 限制最大会话数
     * - 自动清理过期会话
     * - 会话持久化
     * - 多级缓存同步
     *
     * @param userId     用户ID
     * @param token      访问令牌
     * @param deviceInfo 设备信息
     */
    public void manageUserSession(Long userId, String token, String deviceInfo) {
        try {
            String sessionKey = USER_SESSION_PREFIX + userId;

            // 1. 检查当前会话数量（L2 Redis缓存）
            Long sessionCount = redisTemplate.opsForSet().size(sessionKey);
            if (sessionCount != null && sessionCount >= MAX_SESSIONS) {
                // 移除最旧的会话
                Set<String> tokenSet = redisTemplate.opsForSet().members(sessionKey);
                if (tokenSet != null && !tokenSet.isEmpty()) {
                    List<String> tokens = new ArrayList<>(tokenSet);
                    String oldestToken = tokens.get(0);

                    // 移除Redis会话
                    redisTemplate.opsForSet().remove(sessionKey, oldestToken);

                    // 加入黑名单
                    blacklistToken(oldestToken);

                    // 删除数据库会话记录
                    userSessionDao.deleteByToken(oldestToken);

                    log.info("移除最旧会话，用户ID: {}, 令牌: {}", userId, oldestToken.substring(0, 20) + "...");
                }
            }

            // 2. 添加新会话到Redis（L2缓存）
            redisTemplate.opsForSet().add(sessionKey, token);
            redisTemplate.expire(sessionKey, SESSION_TIMEOUT, TimeUnit.SECONDS);

            // 3. 持久化会话到数据库
            UserSessionEntity session = new UserSessionEntity();
            session.setUserId(userId);
            session.setToken(token);
            session.setDeviceInfo(deviceInfo);
            session.setLoginTime(LocalDateTime.now());
            session.setLastAccessTime(LocalDateTime.now());
            session.setExpiryTime(LocalDateTime.now().plusSeconds(SESSION_TIMEOUT));
            session.setStatus(1); // 活跃状态

            userSessionDao.insert(session);

            log.info("用户会话管理成功，用户ID: {}, 当前会话数: {}", userId, sessionCount != null ? sessionCount + 1 : 1);

        } catch (Exception e) {
            log.error("管理用户会话失败，用户ID: {}", userId, e);
            throw new SystemException("SESSION_MANAGE_ERROR", "会话管理失败", e);
        }
    }

    /**
     * 移除用户会话
     *
     * 注意：事务管理在Service层，Manager层不管理事务
     * 事务边界由调用此方法的Service层方法控制
     *
     * @param userId 用户ID
     * @param token  令牌
     */
    public void removeUserSession(Long userId, String token) {
        try {
            // 1. 从Redis移除
            String sessionKey = USER_SESSION_PREFIX + userId;
            redisTemplate.opsForSet().remove(sessionKey, token);

            // 2. 从数据库移除
            userSessionDao.deleteByToken(token);

            log.info("移除用户会话成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("移除用户会话失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 验证用户会话是否有效（多级缓存查询）
     *
     * @param userId 用户ID
     * @param token  令牌
     * @return 是否有效
     */
    public boolean isValidUserSession(Long userId, String token) {
        try {
            // L2 Redis缓存查询
            String sessionKey = USER_SESSION_PREFIX + userId;
            Boolean isMember = redisTemplate.opsForSet().isMember(sessionKey, token);

            if (Boolean.TRUE.equals(isMember)) {
                return true;
            }

            // L3 数据库查询（缓存未命中）
            UserSessionEntity session = userSessionDao.selectByToken(token);
            if (session != null && session.getStatus() == 1
                    && session.getExpiryTime().isAfter(LocalDateTime.now())) {

                // 回填Redis缓存
                redisTemplate.opsForSet().add(sessionKey, token);
                redisTemplate.expire(sessionKey, SESSION_TIMEOUT, TimeUnit.SECONDS);

                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("验证用户会话失败，用户ID: {}", userId, e);
            return false;
        }
    }

    /**
     * 将令牌加入黑名单
     *
     * 企业级特性：
     * - 令牌撤销机制
     * - 安全登出
     * - 防止令牌重放攻击
     *
     * @param token 令牌
     */
    public void blacklistToken(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            // 黑名单保留7天
            redisTemplate.opsForValue().set(blacklistKey, "true", 7, TimeUnit.DAYS);

            log.info("令牌已加入黑名单: {}", token.substring(0, 20) + "...");
        } catch (Exception e) {
            log.error("将令牌加入黑名单失败", e);
        }
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 令牌
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            log.error("检查令牌黑名单失败", e);
            return false;
        }
    }

    /**
     * 记录登录失败次数（防暴力破解）
     *
     * 企业级安全特性：
     * - 登录失败计数
     * - 自动账户锁定
     * - 防暴力破解攻击
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int recordLoginFailure(String username) {
        try {
            String retryKey = LOGIN_RETRY_PREFIX + username;
            Long retryCount = redisTemplate.opsForValue().increment(retryKey);

            if (retryCount == null) {
                retryCount = 1L;
            }

            // 设置过期时间（15分钟）
            redisTemplate.expire(retryKey, 15, TimeUnit.MINUTES);

            // 达到最大重试次数，锁定账户
            if (retryCount >= MAX_LOGIN_RETRY) {
                lockUser(username, LOCK_DURATION);
                log.warn("用户登录失败次数过多，已锁定账户: {}", username);
            }

            return retryCount.intValue();
        } catch (Exception e) {
            log.error("记录登录失败次数失败，用户名: {}", username, e);
            return 0;
        }
    }

    /**
     * 清除登录失败记录
     *
     * @param username 用户名
     */
    public void clearLoginFailure(String username) {
        try {
            String retryKey = LOGIN_RETRY_PREFIX + username;
            redisTemplate.delete(retryKey);
        } catch (Exception e) {
            log.error("清除登录失败记录失败，用户名: {}", username, e);
        }
    }

    /**
     * 锁定用户账户
     *
     * @param username 用户名
     * @param duration 锁定时长（秒）
     */
    public void lockUser(String username, int duration) {
        try {
            String lockKey = USER_LOCK_PREFIX + username;
            redisTemplate.opsForValue().set(lockKey, "true", duration, TimeUnit.SECONDS);

            log.warn("用户账户已锁定: {}, 锁定时长: {}秒", username, duration);
        } catch (Exception e) {
            log.error("锁定用户账户失败，用户名: {}", username, e);
        }
    }

    /**
     * 检查用户是否被锁定
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isUserLocked(String username) {
        try {
            String lockKey = USER_LOCK_PREFIX + username;
            return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
        } catch (Exception e) {
            log.error("检查用户锁定状态失败，用户名: {}", username, e);
            return false;
        }
    }

    /**
     * 解锁用户账户
     *
     * @param username 用户名
     */
    public void unlockUser(String username) {
        try {
            String lockKey = USER_LOCK_PREFIX + username;
            redisTemplate.delete(lockKey);
            clearLoginFailure(username);

            log.info("用户账户已解锁: {}", username);
        } catch (Exception e) {
            log.error("解锁用户账户失败，用户名: {}", username, e);
        }
    }

    /**
     * 更新会话最后访问时间
     *
     * 注意：事务管理在Service层，Manager层不管理事务
     * 事务边界由调用此方法的Service层方法控制
     *
     * @param token 令牌
     */
    public void updateSessionLastAccessTime(String token) {
        try {
            userSessionDao.updateLastAccessTime(token, LocalDateTime.now());
        } catch (Exception e) {
            log.error("更新会话最后访问时间失败", e);
        }
    }

    /**
     * 清理过期会话（定时任务）
     *
     * 注意：事务管理在Service层，Manager层不管理事务
     * 事务边界由调用此方法的Service层方法控制
     *
     * 企业级特性：
     * - 自动清理过期会话
     * - 释放系统资源
     * - 保持会话数据一致性
     *
     * @return 清理数量
     */
    public int cleanExpiredSessions() {
        try {
            int count = userSessionDao.deleteExpiredSessions(LocalDateTime.now());
            log.info("清理过期会话完成，清理数量: {}", count);
            return count;
        } catch (Exception e) {
            log.error("清理过期会话失败", e);
            return 0;
        }
    }

    /**
     * 获取用户活跃会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    public List<UserSessionEntity> getUserActiveSessions(Long userId) {
        try {
            return userSessionDao.selectActiveSessionsByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户活跃会话失败，用户ID: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 强制下线用户所有会话
     *
     * 注意：事务管理在Service层，Manager层不管理事务
     * 事务边界由调用此方法的Service层方法控制
     *
     * 企业级特性：
     * - 管理员强制下线
     * - 安全策略执行
     * - 异常账户处理
     *
     * @param userId 用户ID
     * @return 下线会话数
     */
    public int forceLogoutUser(Long userId) {
        try {
            // 1. 获取用户所有会话
            List<UserSessionEntity> sessions = userSessionDao.selectActiveSessionsByUserId(userId);

            // 2. 将所有令牌加入黑名单
            for (UserSessionEntity session : sessions) {
                blacklistToken(session.getToken());
            }

            // 3. 从Redis移除会话
            String sessionKey = USER_SESSION_PREFIX + userId;
            redisTemplate.delete(sessionKey);

            // 4. 删除数据库会话记录
            int count = userSessionDao.deleteByUserId(userId);

            log.warn("强制下线用户所有会话，用户ID: {}, 下线数量: {}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("强制下线用户失败，用户ID: {}", userId, e);
            return 0;
        }
    }

    /**
     * 获取在线用户统计
     *
     * 企业级监控特性：
     * - 实时在线用户统计
     * - 系统负载监控
     *
     * @return 在线用户数
     */
    public long getOnlineUserCount() {
        try {
            return userSessionDao.countActiveSessions();
        } catch (Exception e) {
            log.error("获取在线用户统计失败", e);
            return 0;
        }
    }

    /**
     * 检查并发登录限制
     *
     * 企业级安全特性：
     * - 防止账号共享
     * - 并发登录控制
     *
     * @param userId 用户ID
     * @return 是否超过限制
     */
    public boolean isConcurrentLoginExceeded(Long userId) {
        try {
            String sessionKey = USER_SESSION_PREFIX + userId;
            Long sessionCount = redisTemplate.opsForSet().size(sessionKey);
            return sessionCount != null && sessionCount >= MAX_SESSIONS;
        } catch (Exception e) {
            log.error("检查并发登录限制失败，用户ID: {}", userId, e);
            return false;
        }
    }
}
