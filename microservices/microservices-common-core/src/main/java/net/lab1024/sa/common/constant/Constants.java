package net.lab1024.sa.common.constant;

/**
 * 常量定义
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 统一管理系统中的常量，避免硬编码
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public class Constants {
    
    /**
     * 私有构造函数，防止实例化
     */
    private Constants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    /**
     * 缓存键前缀
     * <p>
     * 所有Redis缓存键统一使用此前缀，便于管理和清理
     * </p>
     */
    public static final String CACHE_PREFIX = "ioedream:";

    /**
     * 默认分页大小
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求：默认值为20
     * </p>
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Token过期时间（秒）
     * <p>
     * 默认2小时，可通过配置覆盖
     * </p>
     */
    public static final long TOKEN_EXPIRE_SECONDS = 7200;

    /**
     * 最大上传文件大小（MB）
     * <p>
     * 默认50MB，可通过配置覆盖
     * </p>
     */
    public static final int MAX_UPLOAD_SIZE_MB = 50;
}
