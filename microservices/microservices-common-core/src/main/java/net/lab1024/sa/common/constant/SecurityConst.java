package net.lab1024.sa.common.constant;

/**
 * 安全相关常量
 * <p>
 * 定义系统中使用的安全相关常量
 * 严格遵循CLAUDE.md规范：常量统一管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public class SecurityConst {

    private SecurityConst() {
        // 私有构造，禁止实例化
    }

    /**
     * 生物识别加密密钥
     * <p>
     * 注意：生产环境应该从配置中心或环境变量获取
     * </p>
     */
    public static final String BIOMETRIC_ENCRYPTION_KEY = System.getenv("BIOMETRIC_ENCRYPTION_KEY") != null
            ? System.getenv("BIOMETRIC_ENCRYPTION_KEY")
            : "default-biometric-encryption-key-16";

    /**
     * JWT Token密钥
     */
    public static final String JWT_SECRET_KEY = System.getenv("JWT_SECRET_KEY") != null
            ? System.getenv("JWT_SECRET_KEY")
            : "default-jwt-secret-key";

    /**
     * Token过期时间（秒）
     */
    public static final long TOKEN_EXPIRE_TIME = 7200; // 2小时

    /**
     * Refresh Token过期时间（秒）
     */
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 604800; // 7天
}


