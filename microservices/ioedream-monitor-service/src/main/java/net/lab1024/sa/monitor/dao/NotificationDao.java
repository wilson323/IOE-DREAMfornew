package net.lab1024.sa.monitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.monitor.domain.entity.NotificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知数据访问层
 *
 * @author IOE-DREAM Team
 */
@Mapper
public interface NotificationDao extends BaseMapper<NotificationEntity> {

    /**
     * 查询待发送通知
     *
     * @return 待发送通知列表
     */
    List<NotificationEntity> selectPendingNotifications();

    /**
     * 查询发送失败需要重试的通知
     *
     * @param currentTime 当前时间
     * @return 待重试通知列表
     */
    List<NotificationEntity> selectRetryNotifications(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据告警ID查询通知
     *
     * @param alertId 告警ID
     * @return 通知列表
     */
    List<NotificationEntity> selectByAlertId(@Param("alertId") Long alertId);

    /**
     * 根据通知类型查询通知
     *
     * @param notificationType 通知类型
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @return 通知列表
     */
    List<NotificationEntity> selectByNotificationType(@Param("notificationType") String notificationType,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计通知数量按类型
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countNotificationsByType(@Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 统计通知数量按状态
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countNotificationsByStatus(@Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询通知发送统计
     *
     * @param hours 小时数
     * @return 发送统计数据
     */
    List<Map<String, Object>> selectNotificationStats(@Param("hours") Integer hours);

    /**
     * 更新通知发送状态
     *
     * @param notificationId  通知ID
     * @param sendStatus      发送状态
     * @param sendTime        发送时间
     * @param responseContent 响应内容
     * @param retryCount      重试次数
     * @param nextRetryTime   下次重试时间
     * @return 影响行数
     */
    int updateSendStatus(@Param("notificationId") Long notificationId,
                          @Param("sendStatus") String sendStatus,
                          @Param("sendTime") LocalDateTime sendTime,
                          @Param("responseContent") String responseContent,
                          @Param("retryCount") Integer retryCount,
                          @Param("nextRetryTime") LocalDateTime nextRetryTime);

    /**
     * 批量插入通知
     *
     * @param notificationList 通知列表
     * @return 插入行数
     */
    int batchInsert(@Param("notificationList") List<NotificationEntity> notificationList);

    /**
     * 删除历史通知数据
     *
     * @param beforeTime 时间点之前的记录将被删除
     * @return 删除行数
     */
    int deleteHistoryNotifications(@Param("beforeTime") LocalDateTime beforeTime);
}