package net.lab1024.sa.common.auth.manager.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 认证 Redis 存储
 * <p>
 * 统一封装 UnifiedAuthenticationManager 中的 Redis 操作：黑名单、会话集合、一次性验证码、TOTP 密钥缓存等。
 * </p>
 */
@Slf4j
public class AuthRedisStore {

    private final StringRedisTemplate redisTemplate;

    public AuthRedisStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("[统一认证] 令牌为空，无法加入黑名单");
            return;
        }
        try {
            String blacklistKey = AuthRedisKeys.TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, "true", AuthRedisKeys.TOKEN_BLACKLIST_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[统一认证] 将令牌加入黑名单失败: error={}", e.getMessage(), e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            String blacklistKey = AuthRedisKeys.TOKEN_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            log.error("[统一认证] 检查令牌黑名单失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    public Set<String> getUserSessionTokens(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        try {
            String sessionKey = AuthRedisKeys.USER_SESSION_PREFIX + userId;
            Set<String> tokens = redisTemplate.opsForSet().members(sessionKey);
            return tokens != null ? tokens : Set.of();
        } catch (Exception e) {
            log.error("[统一认证] 获取用户会话集合失败: userId={}, error={}", userId, e.getMessage(), e);
            return Set.of();
        }
    }

    public void clearUserSessionTokens(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            redisTemplate.delete(AuthRedisKeys.USER_SESSION_PREFIX + userId);
        } catch (Exception e) {
            log.error("[统一认证] 清理用户会话集合失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    public int getLoginRetryCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        try {
            String retryKey = AuthRedisKeys.LOGIN_RETRY_PREFIX + userId;
            String retryCountStr = redisTemplate.opsForValue().get(retryKey);
            return retryCountStr != null ? Integer.parseInt(retryCountStr) : 0;
        } catch (Exception e) {
            log.error("[统一认证] 获取登录失败次数异常: userId={}, error={}", userId, e.getMessage(), e);
            return 0;
        }
    }

    public OneTimeCodeVerifyResult verifyAndConsumeSmsCode(Long userId, String smsCode) {
        if (userId == null || smsCode == null || smsCode.trim().isEmpty()) {
            log.warn("[统一认证] 短信验证码参数为空");
            return OneTimeCodeVerifyResult.INVALID_ARGUMENT;
        }

        String codeKey = AuthRedisKeys.SMS_CODE_PREFIX + userId;
        try {
            String storedCode = redisTemplate.opsForValue().get(codeKey);
            if (storedCode == null) {
                return OneTimeCodeVerifyResult.CODE_NOT_FOUND;
            }
            if (!smsCode.trim().equals(storedCode)) {
                return OneTimeCodeVerifyResult.CODE_MISMATCH;
            }
            redisTemplate.delete(codeKey);
            return OneTimeCodeVerifyResult.SUCCESS;
        } catch (Exception e) {
            log.error("[统一认证] 验证短信验证码异常: userId={}, error={}", userId, e.getMessage(), e);
            return OneTimeCodeVerifyResult.CODE_NOT_FOUND;
        }
    }

    public OneTimeCodeVerifyResult verifyAndConsumeEmailCode(String email, String emailCode) {
        if (email == null || email.trim().isEmpty() || emailCode == null || emailCode.trim().isEmpty()) {
            return OneTimeCodeVerifyResult.INVALID_ARGUMENT;
        }
        String codeKey = AuthRedisKeys.EMAIL_CODE_PREFIX + email;
        try {
            String storedCode = redisTemplate.opsForValue().get(codeKey);
            if (storedCode == null) {
                return OneTimeCodeVerifyResult.CODE_NOT_FOUND;
            }
            if (!emailCode.trim().equals(storedCode)) {
                return OneTimeCodeVerifyResult.CODE_MISMATCH;
            }
            redisTemplate.delete(codeKey);
            return OneTimeCodeVerifyResult.SUCCESS;
        } catch (Exception e) {
            log.error("[统一认证] 验证邮件验证码异常: email={}, error={}", email, e.getMessage(), e);
            return OneTimeCodeVerifyResult.CODE_NOT_FOUND;
        }
    }

    public String getTotpSecret(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(AuthRedisKeys.TOTP_SECRET_PREFIX + userId);
        } catch (Exception e) {
            log.error("[统一认证] 获取TOTP密钥缓存异常: userId={}, error={}", userId, e.getMessage(), e);
            return null;
        }
    }

    public void cacheTotpSecret(Long userId, String totpSecret) {
        if (userId == null || totpSecret == null || totpSecret.trim().isEmpty()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(AuthRedisKeys.TOTP_SECRET_PREFIX + userId, totpSecret, 30, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("[统一认证] 缓存TOTP密钥异常: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
}

