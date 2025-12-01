package net.lab1024.sa.monitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.monitor.domain.entity.AlertEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警数据访问层
 *
 * @author IOE-DREAM Team
 */
@Mapper
public interface AlertDao extends BaseMapper<AlertEntity> {

    /**
     * 查询活跃告警
     *
     * @return 活跃告警列表
     */
    List<AlertEntity> selectActiveAlerts();

    /**
     * 根据服务名称查询告警
     *
     * @param serviceName 服务名称
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 告警列表
     */
    List<AlertEntity> selectByServiceName(@Param("serviceName") String serviceName,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 根据告警级别查询告警
     *
     * @param alertLevel 告警级别
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 告警列表
     */
    List<AlertEntity> selectByAlertLevel(@Param("alertLevel") String alertLevel,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询近期告警
     *
     * @param hours 小时数
     * @return 告警列表
     */
    List<AlertEntity> selectRecentAlerts(@Param("hours") Integer hours);

    /**
     * 统计告警数量按级别
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countAlertsByLevel(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 统计告警数量按类型
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countAlertsByType(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计告警数量按服务
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countAlertsByService(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询告警趋势数据
     *
     * @param hours 小时数
     * @return 趋势数据
     */
    List<Map<String, Object>> selectAlertTrends(@Param("hours") Integer hours);

    /**
     * 更新告警解决信息
     *
     * @param alertId      告警ID
     * @param resolveTime  解决时间
     * @param resolveUserId 解决人ID
     * @param resolution   解决说明
     * @return 影响行数
     */
    int updateResolveInfo(@Param("alertId") Long alertId,
                          @Param("resolveTime") LocalDateTime resolveTime,
                          @Param("resolveUserId") Long resolveUserId,
                          @Param("resolution") String resolution);

    /**
     * 更新通知状态
     *
     * @param alertId               告警ID
     * @param notificationStatus    通知状态
     * @param notificationCount     通知次数
     * @param lastNotificationTime  最后通知时间
     * @return 影响行数
     */
    int updateNotificationStatus(@Param("alertId") Long alertId,
                                  @Param("notificationStatus") String notificationStatus,
                                  @Param("notificationCount") Integer notificationCount,
                                  @Param("lastNotificationTime") LocalDateTime lastNotificationTime);

    /**
     * 删除历史告警数据
     *
     * @param beforeTime 时间点之前的记录将被删除
     * @return 删除行数
     */
    int deleteHistoryAlerts(@Param("beforeTime") LocalDateTime beforeTime);
}