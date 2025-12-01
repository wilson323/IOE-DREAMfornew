package net.lab1024.sa.notification.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.notification.domain.entity.NotificationConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 通知配置DAO
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Mapper和@Repository注解
 * - 继承BaseMapper提供基础CRUD
 * - 提供丰富的配置查询方法
 * - 支持用户级和系统级配置管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Mapper
@Repository
public interface NotificationConfigDao extends BaseMapper<NotificationConfigEntity> {

    /**
     * 根据配置键查询配置
     */
    @Select("SELECT * FROM t_notification_config WHERE config_key = #{configKey} AND status = 1")
    NotificationConfigEntity selectByKey(@Param("configKey") String configKey);

    /**
     * 查询用户配置
     */
    @Select("SELECT * FROM t_notification_config WHERE user_id = #{userId} AND config_type = 2 AND status = 1")
    List<NotificationConfigEntity> selectUserConfigs(@Param("userId") Long userId);

    /**
     * 查询系统配置
     */
    @Select("SELECT * FROM t_notification_config WHERE config_type = 1 AND status = 1")
    List<NotificationConfigEntity> selectSystemConfigs();

    /**
     * 查询用户特定配置
     */
    @Select("SELECT * FROM t_notification_config WHERE user_id = #{userId} AND config_key = #{configKey} " +
            "AND config_type = 2 AND status = 1")
    NotificationConfigEntity selectUserConfigByKey(@Param("userId") Long userId, @Param("configKey") String configKey);

    /**
     * 查询渠道配置
     */
    List<NotificationConfigEntity> selectChannelConfigs(@Param("channel") Integer channel,
                                                        @Param("userId") Long userId);

    /**
     * 查询消息类型配置
     */
    List<NotificationConfigEntity> selectMessageTypeConfigs(@Param("messageType") Integer messageType,
                                                           @Param("userId") Long userId);

    /**
     * 查询分类配置
     */
    List<NotificationConfigEntity> selectCategoryConfigs(@Param("category") String category,
                                                         @Param("userId") Long userId);

    /**
     * 查询默认配置
     */
    @Select("SELECT * FROM t_notification_config WHERE is_default = 1 AND status = 1 " +
            "<if test='channel != null'>AND channel = #{channel}</if> " +
            "<if test='messageType != null'>AND message_type = #{messageType}</if>")
    List<NotificationConfigEntity> selectDefaultConfigs(@Param("channel") Integer channel,
                                                       @Param("messageType") Integer messageType);

    /**
     * 查询有效的配置（启用且已审批）
     */
    List<NotificationConfigEntity> selectEffectiveConfigs(@Param("userId") Long userId,
                                                         @Param("channel") Integer channel,
                                                         @Param("messageType") Integer messageType);

    /**
     * 根据条件查询配置列表
     */
    List<NotificationConfigEntity> selectByCondition(@Param("configType") Integer configType,
                                                    @Param("userId") Long userId,
                                                    @Param("channel") Integer channel,
                                                    @Param("messageType") Integer messageType,
                                                    @Param("category") String category,
                                                    @Param("status") Integer status);

    /**
     * 查询配置分类列表
     */
    @Select("SELECT DISTINCT category FROM t_notification_config WHERE category IS NOT NULL AND category != ''")
    List<String> selectCategories();

    /**
     * 查询配置键列表
     */
    List<String> selectConfigKeys(@Param("userId") Long userId, @Param("configType") Integer configType);

    /**
     * 批量更新配置状态
     */
    int batchUpdateStatus(@Param("configIds") List<Long> configIds, @Param("status") Integer status);

    /**
     * 查询用户通知偏好设置
     */
    List<NotificationConfigEntity> selectUserPreferences(@Param("userId") Long userId);

    /**
     * 查询系统通知设置
     */
    List<NotificationConfigEntity> selectSystemSettings(@Param("category") String category);

    /**
     * 查询渠道限额配置
     */
    Map<String, Object> selectChannelLimitConfig(@Param("channel") Integer channel);

    /**
     * 查询频率限制配置
     */
    Map<String, Object> selectRateLimitConfig(@Param("userId") Long userId,
                                              @Param("channel") Integer channel,
                                              @Param("messageType") Integer messageType);

    /**
     * 内部类：常用配置键定义
     */
    class ConfigKey {
        public static final String EMAIL_ENABLED = "notification.email.enabled";
        public static final String EMAIL_SMTP_HOST = "notification.email.smtp.host";
        public static final String EMAIL_SMTP_PORT = "notification.email.smtp.port";
        public static final String EMAIL_USERNAME = "notification.email.username";
        public static final String EMAIL_PASSWORD = "notification.email.password";
        public static final String EMAIL_FROM_ADDRESS = "notification.email.from.address";
        public static final String EMAIL_FROM_NAME = "notification.email.from.name";

        public static final String SMS_ENABLED = "notification.sms.enabled";
        public static final String SMS_PROVIDER = "notification.sms.provider";
        public static final String SMS_ACCESS_KEY = "notification.sms.access.key";
        public static final String SMS_SECRET_KEY = "notification.sms.secret.key";
        public static final String SMS_SIGN_NAME = "notification.sms.sign.name";
        public static final String SMS_TEMPLATE_CODE = "notification.sms.template.code";

        public static final String WECHAT_ENABLED = "notification.wechat.enabled";
        public static final String WECHAT_APP_ID = "notification.wechat.app.id";
        public static final String WECHAT_APP_SECRET = "notification.wechat.app.secret";
        public static final String WECHAT_TOKEN = "notification.wechat.token";
        public static final String WECHAT_ENCODING_AES_KEY = "notification.wechat.encoding.aes.key";

        public static final String PUSH_ENABLED = "notification.push.enabled";
        public static final String PUSH_PROVIDER = "notification.push.provider";
        public static final String PUSH_APP_KEY = "notification.push.app.key";
        public static final String PUSH_APP_SECRET = "notification.push.app.secret";
        public static final String PUSH_MASTER_SECRET = "notification.push.master.secret";

        public static final String VOICE_ENABLED = "notification.voice.enabled";
        public static final String VOICE_PROVIDER = "notification.voice.provider";
        public static final String VOICE_ACCESS_KEY = "notification.voice.access.key";
        public static final String VOICE_SECRET_KEY = "notification.voice.secret.key";
        public static final String VOICE_APP_ID = "notification.voice.app.id";

        public static final String USER_PREFERENCE_EMAIL = "user.preference.email.enabled";
        public static final String USER_PREFERENCE_SMS = "user.preference.sms.enabled";
        public static final String USER_PREFERENCE_WECHAT = "user.preference.wechat.enabled";
        public static final String USER_PREFERENCE_PUSH = "user.preference.push.enabled";
        public static final String USER_PREFERENCE_VOICE = "user.preference.voice.enabled";

        public static final String RATE_LIMIT_DAILY = "notification.rate.limit.daily";
        public static final String RATE_LIMIT_HOURLY = "notification.rate.limit.hourly";
        public static final String RATE_LIMIT_MINUTELY = "notification.rate.limit.minutely";

        public static final String BATCH_SEND_SIZE = "notification.batch.send.size";
        public static final String ASYNC_ENABLED = "notification.async.enabled";
        public static final String RETRY_ENABLED = "notification.retry.enabled";
        public static final String MAX_RETRY_COUNT = "notification.retry.max.count";

        public static final String INTERNAL_MESSAGE_EXPIRE_DAYS = "notification.internal.expire.days";
        public static final String RECORD_RETENTION_DAYS = "notification.record.retention.days";
        public static final String STATISTICS_CACHE_HOURS = "notification.statistics.cache.hours";
    }

    /**
     * 内部类：配置分类定义
     */
    class Category {
        public static final String SYSTEM = "system";
        public static final String EMAIL = "email";
        public static final String SMS = "sms";
        public static final String WECHAT = "wechat";
        public static final String PUSH = "push";
        public static final String VOICE = "voice";
        public static final String USER_PREFERENCE = "user_preference";
        public static final String RATE_LIMIT = "rate_limit";
        public static final String BATCH_CONFIG = "batch_config";
        public static final String RETRY_CONFIG = "retry_config";
        public static final String RETENTION_CONFIG = "retention_config";
    }

    /**
     * 内部类：数据类型定义
     */
    class DataType {
        public static final String STRING = "string";
        public static final String NUMBER = "number";
        public static final String BOOLEAN = "boolean";
        public static final String JSON = "json";
    }
}