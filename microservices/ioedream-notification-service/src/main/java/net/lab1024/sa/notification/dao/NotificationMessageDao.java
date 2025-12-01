package net.lab1024.sa.notification.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.notification.domain.entity.NotificationMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知消息DAO
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Mapper和@Repository注解
 * - 继承BaseMapper提供基础CRUD
 * - 提供丰富的查询方法
 * - 支持复杂统计和批量操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Mapper
@Repository
public interface NotificationMessageDao extends BaseMapper<NotificationMessageEntity> {

    /**
     * 分页查询通知消息
     */
    IPage<NotificationMessageEntity> selectByPage(Page<NotificationMessageEntity> page,
                                                  @Param("userId") Long userId,
                                                  @Param("recipientUserId") Long recipientUserId,
                                                  @Param("channel") Integer channel,
                                                  @Param("messageType") Integer messageType,
                                                  @Param("sendStatus") Integer sendStatus,
                                                  @Param("priority") Integer priority,
                                                  @Param("businessType") String businessType,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("keyword") String keyword,
                                                  @Param("sortField") String sortField,
                                                  @Param("sortDirection") String sortDirection);

    /**
     * 根据条件查询消息列表
     */
    List<NotificationMessageEntity> selectByCondition(@Param("userId") Long userId,
                                                      @Param("recipientUserId") Long recipientUserId,
                                                      @Param("channel") Integer channel,
                                                      @Param("messageType") Integer messageType,
                                                      @Param("sendStatus") Integer sendStatus,
                                                      @Param("priority") Integer priority,
                                                      @Param("businessType") String businessType,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime,
                                                      @Param("limit") Integer limit);

    /**
     * 查询待发送的消息
     */
    List<NotificationMessageEntity> selectPendingMessages(@Param("channel") Integer channel,
                                                          @Param("scheduleTime") LocalDateTime scheduleTime,
                                                          @Param("limit") Integer limit);

    /**
     * 查询需要重试的失败消息
     */
    List<NotificationMessageEntity> selectRetryMessages(@Param("channel") Integer channel,
                                                        @Param("maxRetryCount") Integer maxRetryCount,
                                                        @Param("limit") Integer limit);

    /**
     * 查询用户未读的站内信
     */
    List<NotificationMessageEntity> selectUnreadInternalMessages(@Param("recipientUserId") Long recipientUserId,
                                                                 @Param("limit") Integer limit);

    /**
     * 批量更新消息状态
     */
    @Update("<script>" +
            "UPDATE t_notification_message SET send_status = #{sendStatus}, " +
            "failure_reason = #{failureReason}, " +
            "sent_time = #{sentTime}, " +
            "external_message_id = #{externalMessageId}, " +
            "send_duration = #{sendDuration} " +
            "WHERE message_id IN " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateSendStatus(@Param("messageIds") List<Long> messageIds,
                              @Param("sendStatus") Integer sendStatus,
                              @Param("failureReason") String failureReason,
                              @Param("sentTime") LocalDateTime sentTime,
                              @Param("externalMessageId") String externalMessageId,
                              @Param("sendDuration") Long sendDuration);

    /**
     * 批量更新重试次数
     */
    @Update("<script>" +
            "UPDATE t_notification_message SET retry_count = retry_count + 1 " +
            "WHERE message_id IN " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchIncrementRetryCount(@Param("messageIds") List<Long> messageIds);

    /**
     * 标记消息为已读
     */
    @Update("UPDATE t_notification_message SET read_status = 1, read_time = NOW() " +
            "WHERE message_id = #{messageId} AND recipient_user_id = #{userId} AND channel = 4")
    int markMessageAsRead(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 批量标记消息为已读
     */
    @Update("<script>" +
            "UPDATE t_notification_message SET read_status = 1, read_time = NOW() " +
            "WHERE recipient_user_id = #{userId} AND channel = 4 AND message_id IN " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchMarkMessagesAsRead(@Param("messageIds") List<Long> messageIds, @Param("userId") Long userId);

    /**
     * 统计消息数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_notification_message WHERE 1=1 " +
            "<if test='userId != null'>" +
            "AND (sender_user_id = #{userId} OR recipient_user_id = #{userId}) " +
            "</if>" +
            "<if test='channel != null'>" +
            "AND channel = #{channel} " +
            "</if>" +
            "<if test='messageType != null'>" +
            "AND message_type = #{messageType} " +
            "</if>" +
            "<if test='sendStatus != null'>" +
            "AND send_status = #{sendStatus} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND create_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND create_time <= #{endTime} " +
            "</if>" +
            "</script>")
    int countMessages(@Param("userId") Long userId,
                      @Param("channel") Integer channel,
                      @Param("messageType") Integer messageType,
                      @Param("sendStatus") Integer sendStatus,
                      @Param("startTime") LocalDateTime startTime,
                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户未读消息数量
     */
    @Select("SELECT COUNT(*) FROM t_notification_message " +
            "WHERE recipient_user_id = #{userId} AND channel = 4 AND read_status = 0")
    int countUnreadInternalMessages(@Param("userId") Long userId);

    /**
     * 查询消息发送统计
     */
    List<Map<String, Object>> selectSendStatistics(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("groupBy") String groupBy);

    /**
     * 查询用户消息统计
     */
    List<Map<String, Object>> selectUserMessageStatistics(@Param("userId") Long userId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询渠道使用统计
     */
    List<Map<String, Object>> selectChannelUsageStatistics(@Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 查询消息类型统计
     */
    List<Map<String, Object>> selectMessageTypeStatistics(@Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询发送成功率统计
     */
    Map<String, Object> selectSuccessRateStatistics(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询发送耗时统计
     */
    Map<String, Object> selectSendDurationStatistics(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期消息
     */
    @Update("DELETE FROM t_notification_message WHERE send_status = 2 AND create_time < #{expireTime}")
    int deleteExpiredMessages(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 查询消息详情（包含关联信息）
     */
    NotificationMessageEntity selectMessageDetail(@Param("messageId") Long messageId);

    /**
     * 内部类：统计查询结果映射
     */
    class StatisticResult {
        public static final String FIELD_CHANNEL = "channel";
        public static final String FIELD_MESSAGE_TYPE = "messageType";
        public static final String FIELD_SEND_STATUS = "sendStatus";
        public static final String FIELD_DATE = "date";
        public static final String FIELD_HOUR = "hour";
        public static final String FIELD_COUNT = "count";
        public static final String FIELD_SUCCESS_RATE = "successRate";
        public static final String FIELD_AVG_DURATION = "avgDuration";
        public static final String FIELD_TOTAL_COST = "totalCost";
    }

    /**
     * 内部类：排序字段定义
     */
    class SortField {
        public static final String CREATE_TIME = "create_time";
        public static final String SCHEDULE_TIME = "schedule_time";
        public static final String SENT_TIME = "sent_time";
        public static final String PRIORITY = "priority";
        public static final String SEND_STATUS = "send_status";
        public static final String MESSAGE_TYPE = "message_type";
        public static final String CHANNEL = "channel";
    }
}