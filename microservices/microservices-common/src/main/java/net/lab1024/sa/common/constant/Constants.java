package net.lab1024.sa.common.constant;

/**
 * 系统常量定义 - 企业级标准
 * 统一管理系统中使用的常量值
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-30
 */
public class Constants {

    /**
     * 默认页码
     */
    public static final Long DEFAULT_PAGE_NUM = 1L;

    /**
     * 默认每页大小
     */
    public static final Long DEFAULT_PAGE_SIZE = 10L;

    /**
     * 最大每页大小
     */
    public static final Long MAX_PAGE_SIZE = 500L;

    /**
     * UTF-8编码
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * JSON内容类型
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * 表单内容类型
     */
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    /**
     * 文件上传内容类型
     */
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    /**
     * 成功状态码
     */
    public static final Integer SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final Integer ERROR_CODE = 500;

    /**
     * 参数错误状态码
     */
    public static final Integer PARAM_ERROR_CODE = 400;

    /**
     * 认证失败状态码
     */
    public static final Integer AUTH_ERROR_CODE = 401;

    /**
     * 权限不足状态码
     */
    public static final Integer PERMISSION_ERROR_CODE = 403;

    /**
     * 默认语言
     */
    public static final String DEFAULT_LOCALE = "zh_CN";

    /**
     * 日期时间格式
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 缓存前缀
     */
    public static final String CACHE_PREFIX = "ioedream:";

    /**
     * 用户缓存前缀
     */
    public static final String USER_CACHE_PREFIX = CACHE_PREFIX + "user:";

    /**
     * 权限缓存前缀
     */
    public static final String PERMISSION_CACHE_PREFIX = CACHE_PREFIX + "permission:";

    /**
     * 菜单缓存前缀
     */
    public static final String MENU_CACHE_PREFIX = CACHE_PREFIX + "menu:";

    /**
     * 字典缓存前缀
     */
    public static final String DICT_CACHE_PREFIX = CACHE_PREFIX + "dict:";

    /**
     * 配置缓存前缀
     */
    public static final String CONFIG_CACHE_PREFIX = CACHE_PREFIX + "config:";

    // 私有构造函数，防止实例化
    private Constants() {
        throw new IllegalStateException("常量类不能被实例化");
    }
}