package net.lab1024.sa.common.constant;

/**
 * 系统常量类
 * <p>
 * 统一管理系统中的魔法值，避免硬编码
 * 提高代码可读性和可维护性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public final class SystemConstants {

    private SystemConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    /**
     * BCrypt密码编码强度
     */
    public static final int BCRYPT_STRENGTH = 10;

    /**
     * HTTP连接超时时间（毫秒）
     */
    public static final int HTTP_CONNECT_TIMEOUT_MS = 10000; // 10秒

    /**
     * HTTP读取超时时间（毫秒）
     */
    public static final int HTTP_READ_TIMEOUT_MS = 30000; // 30秒

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 协议解析性能阈值（毫秒/条）
     */
    public static final double PROTOCOL_PARSING_PERFORMANCE_THRESHOLD_MS = 10.0;

    /**
     * 协议测试消息数量
     */
    public static final int PROTOCOL_TEST_MESSAGE_COUNT = 1000;
}

