package net.lab1024.sa.common.auth.manager.store;

/**
 * 认证 Redis Key 与 TTL 常量
 */
public final class AuthRedisKeys {

    private AuthRedisKeys() {
    }

    public static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";
    public static final String USER_SESSION_PREFIX = "auth:user:session:";
    public static final String LOGIN_RETRY_PREFIX = "auth:login:retry:";
    public static final String SMS_CODE_PREFIX = "auth:sms:code:";
    public static final String EMAIL_CODE_PREFIX = "auth:email:code:";
    public static final String TOTP_SECRET_PREFIX = "auth:totp:secret:";

    public static final int MAX_LOGIN_RETRY = 5;
    public static final int LOGIN_RETRY_TIMEOUT_SECONDS = 900;
    public static final int SMS_CODE_EXPIRE_SECONDS = 300;
    public static final int EMAIL_CODE_EXPIRE_SECONDS = 600;
    public static final int TOKEN_BLACKLIST_EXPIRE_SECONDS = 604800;
}

