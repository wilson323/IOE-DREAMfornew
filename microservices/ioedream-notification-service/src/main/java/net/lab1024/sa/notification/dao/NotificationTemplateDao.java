package net.lab1024.sa.notification.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.notification.domain.entity.NotificationTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 通知模板DAO
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Mapper和@Repository注解
 * - 继承BaseMapper提供基础CRUD
 * - 提供丰富的模板查询方法
 * - 支持版本管理和审批流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Mapper
@Repository
public interface NotificationTemplateDao extends BaseMapper<NotificationTemplateEntity> {

    /**
     * 分页查询模板
     */
    IPage<NotificationTemplateEntity> selectByPage(Page<NotificationTemplateEntity> page,
                                                  @Param("templateCode") String templateCode,
                                                  @Param("templateName") String templateName,
                                                  @Param("channel") Integer channel,
                                                  @Param("messageType") Integer messageType,
                                                  @Param("status") Integer status,
                                                  @Param("approvalStatus") Integer approvalStatus,
                                                  @Param("language") String language,
                                                  @Param("tags") String tags);

    /**
     * 根据模板编码查询
     */
    @Select("SELECT * FROM t_notification_template WHERE template_code = #{templateCode}")
    NotificationTemplateEntity selectByCode(@Param("templateCode") String templateCode);

    /**
     * 查询默认模板
     */
    @Select("SELECT * FROM t_notification_template WHERE channel = #{channel} " +
            "AND message_type = #{messageType} AND is_default = 1 AND status = 1 AND approval_status = 2")
    NotificationTemplateEntity selectDefaultTemplate(@Param("channel") Integer channel,
                                                   @Param("messageType") Integer messageType);

    /**
     * 查询适用的模板（按优先级排序）
     */
    List<NotificationTemplateEntity> selectApplicableTemplates(@Param("channel") Integer channel,
                                                              @Param("messageType") Integer messageType,
                                                              @Param("language") String language);

    /**
     * 查询最新版本的模板
     */
    @Select("SELECT * FROM t_notification_template WHERE template_code = #{templateCode} " +
            "ORDER BY version DESC LIMIT 1")
    NotificationTemplateEntity selectLatestVersion(@Param("templateCode") String templateCode);

    /**
     * 查询模板的所有版本
     */
    @Select("SELECT * FROM t_notification_template WHERE template_code = #{templateCode} " +
            "ORDER BY version DESC")
    List<NotificationTemplateEntity> selectAllVersions(@Param("templateCode") String templateCode);

    /**
     * 根据条件查询模板列表
     */
    List<NotificationTemplateEntity> selectByCondition(@Param("channel") Integer channel,
                                                      @Param("messageType") Integer messageType,
                                                      @Param("status") Integer status,
                                                      @Param("approvalStatus") Integer approvalStatus,
                                                      @Param("language") String language,
                                                      @Param("tags") String tags);

    /**
     * 查询启用的模板
     */
    @Select("SELECT * FROM t_notification_template WHERE status = 1 AND approval_status = 2")
    List<NotificationTemplateEntity> selectEnabledTemplates();

    /**
     * 查询热门模板（按使用次数排序）
     */
    List<NotificationTemplateEntity> selectPopularTemplates(@Param("channel") Integer channel,
                                                           @Param("messageType") Integer messageType,
                                                           @Param("limit") Integer limit);

    /**
     * 批量更新审批状态
     */
    @Update("<script>" +
            "UPDATE t_notification_template SET approval_status = #{approvalStatus}, " +
            "approval_by = #{approvalBy}, approval_time = NOW(), approval_comment = #{approvalComment} " +
            "WHERE template_id IN " +
            "<foreach collection='templateIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateApprovalStatus(@Param("templateIds") List<Long> templateIds,
                                  @Param("approvalStatus") Integer approvalStatus,
                                  @Param("approvalBy") Long approvalBy,
                                  @Param("approvalComment") String approvalComment);

    /**
     * 批量更新使用次数
     */
    @Update("<script>" +
            "UPDATE t_notification_template SET usage_count = usage_count + 1, " +
            "last_used_time = NOW() WHERE template_id IN " +
            "<foreach collection='templateIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchIncrementUsageCount(@Param("templateIds") List<Long> templateIds);

    /**
     * 查询模板使用统计
     */
    List<Map<String, Object>> selectTemplateUsageStatistics(@Param("startTime") java.time.LocalDateTime startTime,
                                                           @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 查询渠道模板统计
     */
    List<Map<String, Object>> selectChannelTemplateStatistics(@Param("channel") Integer channel);

    /**
     * 查询消息类型模板统计
     */
    List<Map<String, Object>> selectMessageTypeTemplateStatistics(@Param("messageType") Integer messageType);

    /**
     * 查询模板审批统计
     */
    Map<String, Object> selectTemplateApprovalStatistics();

    /**
     * 查询待审批的模板
     */
    @Select("SELECT * FROM t_notification_template WHERE approval_status = 1 ORDER BY create_time ASC")
    List<NotificationTemplateEntity> selectPendingApprovalTemplates();

    /**
     * 查询用户的模板
     */
    List<NotificationTemplateEntity> selectUserTemplates(@Param("createBy") Long createBy,
                                                        @Param("approvalStatus") Integer approvalStatus);

    /**
     * 查询模板标签列表
     */
    @Select("SELECT DISTINCT tags FROM t_notification_template WHERE tags IS NOT NULL AND tags != ''")
    List<String> selectTags();

    /**
     * 搜索模板
     */
    List<NotificationTemplateEntity> searchTemplates(@Param("keyword") String keyword,
                                                    @Param("channel") Integer channel,
                                                    @Param("messageType") Integer messageType,
                                                    @Param("limit") Integer limit);

    /**
     * 检查模板编码是否存在
     */
    @Select("SELECT COUNT(*) FROM t_notification_template WHERE template_code = #{templateCode} " +
            "<if test='excludeTemplateId != null'>AND template_id != #{excludeTemplateId}</if>")
    int checkTemplateCodeExists(@Param("templateCode") String templateCode,
                               @Param("excludeTemplateId") Long excludeTemplateId);

    /**
     * 查询最近使用的模板
     */
    List<NotificationTemplateEntity> selectRecentlyUsedTemplates(@Param("userId") Long userId,
                                                                 @Param("limit") Integer limit);

    /**
     * 获取下一个版本号
     */
    @Select("SELECT COALESCE(MAX(CAST(version AS UNSIGNED)), 0) + 1 " +
            "FROM t_notification_template WHERE template_code = #{templateCode}")
    Integer getNextVersion(@Param("templateCode") String templateCode);

    /**
     * 复制模板
     */
    int copyTemplate(@Param("sourceTemplateId") Long sourceTemplateId,
                    @Param("newTemplateCode") String newTemplateCode,
                    @Param("newTemplateName") String newTemplateName,
                    @Param("createBy") Long createBy);

    /**
     * 内部类：消息类型定义
     */
    class MessageType {
        public static final Integer SYSTEM_NOTIFICATION = 1;
        public static final Integer BUSINESS_NOTIFICATION = 2;
        public static final Integer ALERT_NOTIFICATION = 3;
        public static final Integer MARKETING_NOTIFICATION = 4;
        public static final Integer VERIFICATION_CODE = 5;
    }

    /**
     * 内部类：渠道定义
     */
    class Channel {
        public static final Integer EMAIL = 1;
        public static final Integer SMS = 2;
        public static final Integer WECHAT = 3;
        public static final Integer INTERNAL = 4;
        public static final Integer PUSH = 5;
        public static final Integer VOICE = 6;
    }

    /**
     * 内部类：审批状态定义
     */
    class ApprovalStatus {
        public static final Integer DRAFT = 0;
        public static final Integer PENDING = 1;
        public static final Integer APPROVED = 2;
        public static final Integer REJECTED = 3;
    }

    /**
     * 内部类：模板状态定义
     */
    class Status {
        public static final Integer DISABLED = 0;
        public static final Integer ENABLED = 1;
    }

    /**
     * 内部类：常用模板编码
     */
    class TemplateCode {
        public static final String EMAIL_WELCOME = "EMAIL_WELCOME";
        public static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
        public static final String EMAIL_PASSWORD_RESET = "EMAIL_PASSWORD_RESET";
        public static final String EMAIL_LOGIN_ALERT = "EMAIL_LOGIN_ALERT";
        public static final String EMAIL_SYSTEM_MAINTENANCE = "EMAIL_SYSTEM_MAINTENANCE";

        public static final String SMS_VERIFICATION = "SMS_VERIFICATION";
        public static final String SMS_LOGIN_CODE = "SMS_LOGIN_CODE";
        public static final String SMS_ORDER_NOTICE = "SMS_ORDER_NOTICE";
        public static final String SMS_APPOINTMENT_REMINDER = "SMS_APPOINTMENT_REMINDER";

        public static final String WECHAT_ACCESS_GRANTED = "WECHAT_ACCESS_GRANTED";
        public static final String WECHAT_ACCESS_DENIED = "WECHAT_ACCESS_DENIED";
        public static final String WECHAT_CONSUMPTION_ALERT = "WECHAT_CONSUMPTION_ALERT";
        public static final String WECHAT_ATTENDANCE_REMINDER = "WECHAT_ATTENDANCE_REMINDER";

        public static final String INTERNAL_WELCOME = "INTERNAL_WELCOME";
        public static final String INTERNAL_SYSTEM_ANNOUNCEMENT = "INTERNAL_SYSTEM_ANNOUNCEMENT";
        public static final String INTERNAL_TASK_ASSIGNMENT = "INTERNAL_TASK_ASSIGNMENT";
        public static final String INTERNAL_APPROVAL_RESULT = "INTERNAL_APPROVAL_RESULT";

        public static final String PUSH_SECURITY_ALERT = "PUSH_SECURITY_ALERT";
        public static final String PUSH_SYSTEM_UPDATE = "PUSH_SYSTEM_UPDATE";
        public static final String PUSH_PROMOTION = "PUSH_PROMOTION";

        public static final String VOICE_EMERGENCY = "VOICE_EMERGENCY";
        public static final String VOICE_SECURITY_ALERT = "VOICE_SECURITY_ALERT";
    }
}