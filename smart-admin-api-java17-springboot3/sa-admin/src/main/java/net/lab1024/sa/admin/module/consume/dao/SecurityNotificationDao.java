/*
 * 安全通知DAO接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.SecurityNotificationLogEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.SystemNotificationConfigEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.UserNotificationPreferenceEntity;
import net.lab1024.sa.admin.module.consume.domain.vo.NotificationStatistics;
import net.lab1024.sa.admin.module.consume.domain.vo.SecurityNotificationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全通知DAO接口
 * 提供通知相关的数据库操作
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Mapper
public interface SecurityNotificationDao extends BaseMapper<SecurityNotificationLogEntity> {

    /**
     * 插入通知日志
     */
    int insertNotificationLog(@Param("log") SecurityNotificationLogEntity logEntity);

    /**
     * 更新通知日志
     */
    int updateNotificationLog(@Param("log") SecurityNotificationLogEntity logEntity);

    /**
     * 根据ID查询通知日志
     */
    SecurityNotificationLogEntity selectNotificationLogById(@Param("id") Long id);

    /**
     * 查询通知历史记录
     */
    List<SecurityNotificationRecord> selectNotificationHistory(@Param("personId") Long personId,
                                                              @Param("notificationType") String notificationType,
                                                              @Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime,
                                                              @Param("limit") Integer limit);

    /**
     * 查询通知统计信息
     */
    NotificationStatistics selectNotificationStatistics(@Param("personId") Long personId,
                                                      @Param("timeRange") String timeRange);

    /**
     * 查询系统通知配置
     */
    SystemNotificationConfigEntity selectSystemNotificationConfig();

    /**
     * 更新系统通知配置
     */
    int updateSystemNotificationConfig(@Param("config") SystemNotificationConfigEntity config);

    /**
     * 根据人员ID查询通知偏好
     */
    UserNotificationPreferenceEntity selectPreferenceByPersonId(@Param("personId") Long personId);

    /**
     * 插入通知偏好
     */
    int insertPreference(@Param("preference") UserNotificationPreferenceEntity preference);

    /**
     * 更新通知偏好
     */
    int updatePreference(@Param("preference") UserNotificationPreferenceEntity preference);

    /**
     * 删除过期的通知日志
     */
    int deleteExpiredNotificationLog(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 统计指定时间段内的通知数量
     */
    Integer countNotificationsByPeriod(@Param("personId") Long personId,
                                      @Param("channel") String channel,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查询失败的通知记录
     */
    List<SecurityNotificationLogEntity> selectFailedNotifications(@Param("limit") Integer limit,
                                                                 @Param("maxRetryCount") Integer maxRetryCount);

    /**
     * 统计各渠道发送数量
     */
    List<NotificationStatistics.ChannelCount> selectChannelStatistics(@Param("personId") Long personId,
                                                                     @Param("timeRange") String timeRange);

    /**
     * 统计各类型通知数量
     */
    List<NotificationStatistics.TypeCount> selectTypeStatistics(@Param("personId") Long personId,
                                                               @Param("timeRange") String timeRange);

    /**
     * 查询最近发送的通知
     */
    List<SecurityNotificationRecord> selectRecentNotifications(@Param("personId") Long personId,
                                                              @Param("limit") Integer limit);

    /**
     * 检查是否在免打扰时间段内
     */
    boolean isDoNotDisturbPeriod(@Param("personId") Long personId,
                                  @Param("currentHour") Integer currentHour);
}