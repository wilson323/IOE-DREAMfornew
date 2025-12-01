package net.lab1024.sa.notification.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.notification.domain.entity.NotificationRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知发送记录DAO
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Mapper和@Repository注解
 * - 继承BaseMapper提供基础CRUD
 * - 提供丰富的记录查询方法
 * - 支持复杂统计分析和审计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Mapper
@Repository
public interface NotificationRecordDao extends BaseMapper<NotificationRecordEntity> {

    /**
     * 分页查询发送记录
     */
    IPage<NotificationRecordEntity> selectByPage(Page<NotificationRecordEntity> page,
                                                @Param("messageId") Long messageId,
                                                @Param("batchId") String batchId,
                                                @Param("channel") Integer channel,
                                                @Param("messageType") Integer messageType,
                                                @Param("sendStatus") Integer sendStatus,
                                                @Param("deliveryStatus") Integer deliveryStatus,
                                                @Param("recipientUserId") Long recipientUserId,
                                                @Param("senderUserId") Long senderUserId,
                                                @Param("businessType") String businessType,
                                                @Param("serviceProvider") String serviceProvider,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("keyword") String keyword,
                                                @Param("sortField") String sortField,
                                                @Param("sortDirection") String sortDirection);

    /**
     * 根据消息ID查询发送记录
     */
    List<NotificationRecordEntity> selectByMessageId(@Param("messageId") Long messageId);

    /**
     * 根据批次ID查询发送记录
     */
    List<NotificationRecordEntity> selectByBatchId(@Param("batchId") String batchId);

    /**
     * 查询用户的发送记录
     */
    List<NotificationRecordEntity> selectByUserId(@Param("userId") Long userId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("limit") Integer limit);

    /**
     * 查询批量发送记录
     */
    List<NotificationRecordEntity> selectBatchRecords(@Param("businessType") String businessType,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询失败的发送记录
     */
    List<NotificationRecordEntity> selectFailedRecords(@Param("channel") Integer channel,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("limit") Integer limit);

    /**
     * 查询重试记录
     */
    List<NotificationRecordEntity> selectRetryRecords(@Param("channel") Integer channel,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 批量更新配送状态
     */
    @Update("<script>" +
            "UPDATE t_notification_record SET delivery_status = #{deliveryStatus}, " +
            "delivery_time = #{deliveryTime}, user_action = #{userAction}, " +
            "user_action_time = #{userActionTime}, last_track_time = NOW() " +
            "WHERE record_id IN " +
            "<foreach collection='recordIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateDeliveryStatus(@Param("recordIds") List<Long> recordIds,
                                  @Param("deliveryStatus") Integer deliveryStatus,
                                  @Param("deliveryTime") LocalDateTime deliveryTime,
                                  @Param("userAction") String userAction,
                                  @Param("userActionTime") LocalDateTime userActionTime);

    /**
     * 更新最后跟踪时间
     */
    @Update("<script>" +
            "UPDATE t_notification_record SET last_track_time = NOW() " +
            "WHERE record_id IN " +
            "<foreach collection='recordIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateLastTrackTime(@Param("recordIds") List<Long> recordIds);

    /**
     * 统计发送数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_notification_record WHERE 1=1 " +
            "<if test='channel != null'>" +
            "AND channel = #{channel} " +
            "</if>" +
            "<if test='messageType != null'>" +
            "AND message_type = #{messageType} " +
            "</if>" +
            "<if test='sendStatus != null'>" +
            "AND send_status = #{sendStatus} " +
            "</if>" +
            "<if test='deliveryStatus != null'>" +
            "AND delivery_status = #{deliveryStatus} " +
            "</if>" +
            "<if test='recipientUserId != null'>" +
            "AND recipient_user_id = #{recipientUserId} " +
            "</if>" +
            "<if test='businessType != null'>" +
            "AND business_type = #{businessType} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND create_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND create_time <= #{endTime} " +
            "</if>" +
            "</script>")
    int countRecords(@Param("channel") Integer channel,
                     @Param("messageType") Integer messageType,
                     @Param("sendStatus") Integer sendStatus,
                     @Param("deliveryStatus") Integer deliveryStatus,
                     @Param("recipientUserId") Long recipientUserId,
                     @Param("businessType") String businessType,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计发送成功率
     */
    Map<String, Object> selectSendSuccessRate(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             @Param("groupBy") String groupBy);

    /**
     * 统计配送率
     */
    Map<String, Object> selectDeliveryRate(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("channel") Integer channel);

    /**
     * 统计用户行为（打开率、点击率等）
     */
    Map<String, Object> selectUserActionStatistics(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("channel") Integer channel);

    /**
     * 查询发送耗时统计
     */
    Map<String, Object> selectSendDurationStatistics(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime,
                                                     @Param("channel") Integer channel);

    /**
     * 查询成本统计
     */
    Map<String, Object> selectCostStatistics(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("groupBy") String groupBy);

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
     * 查询服务商统计
     */
    List<Map<String, Object>> selectProviderStatistics(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查询时间趋势统计
     */
    List<Map<String, Object>> selectTimeTrendStatistics(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("groupBy") String groupBy);

    /**
     * 查询用户发送统计
     */
    List<Map<String, Object>> selectUserSendStatistics(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("limit") Integer limit);

    /**
     * 查询失败原因统计
     */
    List<Map<String, Object>> selectFailureReasonStatistics(@Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime,
                                                           @Param("channel") Integer channel);

    /**
     * 查询地理分布统计
     */
    List<Map<String, Object>> selectGeoDistributionStatistics(@Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询设备类型统计
     */
    List<Map<String, Object>> selectDeviceTypeStatistics(@Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询业务类型统计
     */
    List<Map<String, Object>> selectBusinessTypeStatistics(@Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 查询发送速度统计
     */
    Map<String, Object> selectSendSpeedStatistics(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("channel") Integer channel);

    /**
     * 查询并发统计
     */
    List<Map<String, Object>> selectConcurrencyStatistics(@Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime,
                                                          @Param("interval") Integer interval);

    /**
     * 查询重试统计
     */
    Map<String, Object> selectRetryStatistics(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询批量发送统计
     */
    List<Map<String, Object>> selectBatchSendStatistics(@Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询热力图数据
     */
    List<Map<String, Object>> selectHeatmapData(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期记录
     */
    @Update("DELETE FROM t_notification_record WHERE create_time < #{expireTime}")
    int deleteExpiredRecords(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 查询记录详情
     */
    NotificationRecordEntity selectRecordDetail(@Param("recordId") Long recordId);

    /**
     * 内部类：统计查询结果映射
     */
    class StatisticResult {
        public static final String FIELD_DATE = "date";
        public static final String FIELD_HOUR = "hour";
        public static final String FIELD_CHANNEL = "channel";
        public static final String FIELD_MESSAGE_TYPE = "messageType";
        public static final String FIELD_SEND_STATUS = "sendStatus";
        public static final String FIELD_DELIVERY_STATUS = "deliveryStatus";
        public static final String FIELD_USER_ACTION = "userAction";
        public static final String FIELD_COUNT = "count";
        public static final String FIELD_SUCCESS_RATE = "successRate";
        public static final String FIELD_DELIVERY_RATE = "deliveryRate";
        public static final String FIELD_OPEN_RATE = "openRate";
        public static final String FIELD_CLICK_RATE = "clickRate";
        public static final String FIELD_AVG_DURATION = "avgDuration";
        public static final String FIELD_TOTAL_COST = "totalCost";
        public static final String FIELD_AVG_COST = "avgCost";
        public static final String FIELD_USER_ID = "userId";
        public static final String FIELD_SEND_COUNT = "sendCount";
        public static final String FIELD_SUCCESS_COUNT = "successCount";
        public static final String FIELD_FAILURE_COUNT = "failureCount";
        public static final String FIELD_RETRY_COUNT = "retryCount";
        public static final String FIELD_BATCH_ID = "batchId";
        public static final String FIELD_BATCH_SIZE = "batchSize";
        public static final String FIELD_LOCATION = "location";
        public static final String FIELD_DEVICE = "device";
        public static final String FIELD_BUSINESS_TYPE = "businessType";
        public static final String FIELD_SERVICE_PROVIDER = "serviceProvider";
    }

    /**
     * 内部类：统计分组方式
     */
    class GroupBy {
        public static final String DATE = "date";
        public static final String HOUR = "hour";
        public static final String CHANNEL = "channel";
        public static final String MESSAGE_TYPE = "messageType";
        public static final String SEND_STATUS = "sendStatus";
        public static final String USER_ID = "userId";
        public static final String BUSINESS_TYPE = "businessType";
        public static final String SERVICE_PROVIDER = "serviceProvider";
    }
}