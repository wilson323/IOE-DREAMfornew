package net.lab1024.sa.admin.module.smart.biometric.constant;

/**
 * 生物识别系统常量定义
 *
 * 定义生物识别系统使用的常量参数、配置项、错误代码等
 * 提供系统配置和业务逻辑的常量支持
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
public final class BiometricConst {

    // ================================
    // 缓存相关常量
    // ================================

    /**
     * 生物识别缓存前缀
     */
    public static final String BIOMETRIC_CACHE_PREFIX = "biometric:";

    /**
     * 用户生物特征缓存键
     */
    public static final String USER_BIOMETRIC_CACHE_KEY = BIOMETRIC_CACHE_PREFIX + "user:";

    /**
     * 设备生物特征缓存键
     */
    public static final String DEVICE_BIOMETRIC_CACHE_KEY = BIOMETRIC_CACHE_PREFIX + "device:";

    /**
     * 认证结果缓存键
     */
    public static final String AUTH_RESULT_CACHE_KEY = BIOMETRIC_CACHE_PREFIX + "auth:result:";

    /**
     * 缓存过期时间（秒）
     */
    public static final int CACHE_EXPIRE_SECONDS = 3600; // 1小时

    /**
     * 临时缓存过期时间（秒）
     */
    public static final int TEMP_CACHE_EXPIRE_SECONDS = 300; // 5分钟

    // ================================
    // 配置参数键
    // ================================

    /**
     * 生物识别引擎配置键
     */
    public static final String BIOMETRIC_ENGINE_CONFIG_KEY = "biometric.engine.config";

    /**
     * 人脸识别算法配置键
     */
    public static final String FACE_RECOGNITION_CONFIG_KEY = "face.recognition.config";

    /**
     * 指纹识别算法配置键
     */
    public static final String FINGERPRINT_RECOGNITION_CONFIG_KEY = "fingerprint.recognition.config";

    /**
     * 虹膜识别算法配置键
     */
    public static final String IRIS_RECOGNITION_CONFIG_KEY = "iris.recognition.config";

    /**
     * 掌纹识别算法配置键
     */
    public static final String PALMPRINT_RECOGNITION_CONFIG_KEY = "palmprint.recognition.config";

    // ================================
    // API路径常量
    // ================================

    /**
     * 生物识别API路径前缀
     */
    public static final String BIOMETRIC_API_PATH_PREFIX = "/api/biometric";

    /**
     * 用户生物特征管理路径
     */
    public static final String USER_BIOMETRIC_PATH = BIOMETRIC_API_PATH_PREFIX + "/user";

    /**
     * 设备生物特征管理路径
     */
    public static final String DEVICE_BIOMETRIC_PATH = BIOMETRIC_API_PATH_PREFIX + "/device";

    /**
     * 认证服务路径
     */
    public static final String AUTHENTICATION_PATH = BIOMETRIC_API_PATH_PREFIX + "/auth";

    /**
     * 模板管理路径
     */
    public static final String TEMPLATE_PATH = BIOMETRIC_API_PATH_PREFIX + "/template";

    /**
     * 记录查询路径
     */
    public static final String RECORD_PATH = BIOMETRIC_API_PATH_PREFIX + "/record";

    // ================================
    // 事件类型常量
    // ================================

    /**
     * 认证开始事件
     */
    public static final String EVENT_AUTHENTICATION_START = "AUTHENTICATION_START";

    /**
     * 认证成功事件
     */
    public static final String EVENT_AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";

    /**
     * 认证失败事件
     */
    public static final String EVENT_AUTHENTICATION_FAILURE = "AUTHENTICATION_FAILURE";

    /**
     * 模板注册事件
     */
    public static final String EVENT_TEMPLATE_REGISTER = "TEMPLATE_REGISTER";

    /**
     * 模板更新事件
     */
    public static final String EVENT_TEMPLATE_UPDATE = "TEMPLATE_UPDATE";

    /**
     * 模板删除事件
     */
    public static final String EVENT_TEMPLATE_DELETE = "TEMPLATE_DELETE";

    // ================================
    // 系统限制常量
    // ================================

    /**
     * 单次认证最大文件大小（字节）
     */
    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    /**
     * 单个用户最大模板数量
     */
    public static final int MAX_TEMPLATES_PER_USER = 10;

    /**
     * 单个设备最大模板数量
     */
    public static final int MAX_TEMPLATES_PER_DEVICE = 1000;

    /**
     * 认证超时时间（毫秒）
     */
    public static final long AUTHENTICATION_TIMEOUT_MS = 30000; // 30秒

    /**
     * 最大重试次数
     */
    public static final int MAX_RETRY_COUNT = 3;

    /**
     * 批量操作最大数量
     */
    public static final int MAX_BATCH_SIZE = 100;

    // ================================
    // 加密相关常量
    // ================================

    /**
     * SM2算法标识
     */
    public static final String SM2_ALGORITHM = "SM2";

    /**
     * SM3算法标识
     */
    public static final String SM3_ALGORITHM = "SM3";

    /**
     * SM4算法标识
     */
    public static final String SM4_ALGORITHM = "SM4";

    /**
     * 密钥长度（字节）
     */
    public static final int KEY_LENGTH_BYTES = 32;

    /**
     * IV长度（字节）
     */
    public static final int IV_LENGTH_BYTES = 16;

    // ================================
    // 性能参数常量
    // ================================

    /**
     * 默认线程池大小
     */
    public static final int DEFAULT_THREAD_POOL_SIZE = 10;

    /**
     * 最大线程池大小
     */
    public static final int MAX_THREAD_POOL_SIZE = 50;

    /**
     * 队列容量
     */
    public static final int QUEUE_CAPACITY = 1000;

    /**
     * 并发认证最大数量
     */
    public static final int MAX_CONCURRENT_AUTH = 100;

    // ================================
    // 构造函数
    // ================================

    private BiometricConst() {
        // 私有构造函数，防止实例化
    }
}