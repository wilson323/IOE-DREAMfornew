package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.AlertNotificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警通知记录DAO接口
 * <p>
 * 提供告警通知记录数据的访问操作：
 * - 基础CRUD（继承BaseMapper）
 * - 按告警ID查询通知记录
 * - 按通知状态查询
 * - 查询待重试的通知
 * - 更新通知状态
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AlertNotificationDao extends BaseMapper<AlertNotificationEntity> {

    /**
     * 根据告警ID查询所有通知记录
     *
     * @param alertId 告警ID
     * @return 通知记录列表
     */
    @Select("SELECT * FROM t_alert_notification WHERE alert_id = #{alertId} ORDER BY create_time DESC")
    List<AlertNotificationEntity> selectByAlertId(@Param("alertId") Long alertId);

    /**
     * 根据通知状态查询记录
     *
     * @param notificationStatus 通知状态
     * @return 通知记录列表
     */
    @Select("SELECT * FROM t_alert_notification WHERE notification_status = #{notificationStatus} ORDER BY create_time ASC")
    List<AlertNotificationEntity> selectByStatus(@Param("notificationStatus") Integer notificationStatus);

    /**
     * 查询待重试的通知记录（失败且重试次数未达上限）
     *
     * @param currentTime 当前时间
     * @return 待重试的通知记录列表
     */
    @Select("SELECT * FROM t_alert_notification WHERE notification_status = 3 AND retry_count < max_retry ORDER BY create_time ASC")
    List<AlertNotificationEntity> selectPendingRetry(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 更新通知状态为成功
     *
     * @param notificationId 通知ID
     * @param sendTime       发送时间
     * @return 更新行数
     */
    @Update("UPDATE t_alert_notification SET notification_status = 2, send_time = #{sendTime} WHERE notification_id = #{notificationId}")
    Integer markAsSuccess(@Param("notificationId") Long notificationId,
                         @Param("sendTime") LocalDateTime sendTime);

    /**
     * 更新通知状态为失败
     *
     * @param notificationId 通知ID
     * @param errorMessage   错误信息
     * @param errorCode      错误代码
     * @return 更新行数
     */
    @Update("UPDATE t_alert_notification SET notification_status = 3, error_message = #{errorMessage}, error_code = #{errorCode}, retry_count = retry_count + 1 WHERE notification_id = #{notificationId}")
    Integer markAsFailed(@Param("notificationId") Long notificationId,
                        @Param("errorMessage") String errorMessage,
                        @Param("errorCode") String errorCode);

    /**
     * 统计各通知状态的数量
     *
     * @return 统计结果 [{"notificationStatus": 0, "count": 5}, ...]
     */
    @Select("SELECT notification_status, COUNT(*) as count FROM t_alert_notification GROUP BY notification_status")
    List<Object> countByStatus();

    /**
     * 统计指定告警的通知发送成功率
     *
     * @param alertId 告警ID
     * @return 成功率（0-100）
     */
    @Select("SELECT CAST(SUM(CASE WHEN notification_status = 2 THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS DECIMAL(5,2)) FROM t_alert_notification WHERE alert_id = #{alertId}")
    Double calculateSuccessRate(@Param("alertId") Long alertId);

    /**
     * 查询指定接收人的未读通知
     *
     * @param recipientId 接收人ID
     * @return 未读通知列表
     */
    @Select("SELECT * FROM t_alert_notification WHERE recipient_id = #{recipientId} AND notification_status = 2 AND read_time IS NULL ORDER BY send_time DESC")
    List<AlertNotificationEntity> selectUnreadByRecipient(@Param("recipientId") Long recipientId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param readTime       阅读时间
     * @return 更新行数
     */
    @Update("UPDATE t_alert_notification SET read_time = #{readTime} WHERE notification_id = #{notificationId}")
    Integer markAsRead(@Param("notificationId") Long notificationId,
                      @Param("readTime") LocalDateTime readTime);
}
