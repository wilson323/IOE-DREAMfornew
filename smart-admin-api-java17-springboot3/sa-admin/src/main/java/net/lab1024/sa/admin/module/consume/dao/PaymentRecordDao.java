package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.PaymentRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 支付记录DAO
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Mapper
public interface PaymentRecordDao extends BaseMapper<PaymentRecordEntity> {

    /**
     * 根据支付ID查询支付记录
     *
     * @param paymentId 支付ID
     * @return 支付记录
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE payment_id = #{paymentId} AND deleted_flag = 0")
    PaymentRecordEntity selectByPaymentId(@Param("paymentId") String paymentId);

    /**
     * 根据消费记录ID查询支付记录
     *
     * @param consumeRecordId 消费记录ID
     * @return 支付记录
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE consume_record_id = #{consumeRecordId} AND deleted_flag = 0 ORDER BY create_time DESC LIMIT 1")
    PaymentRecordEntity selectByConsumeRecordId(@Param("consumeRecordId") Long consumeRecordId);

    /**
     * 根据用户ID查询支付记录
     *
     * @param userId 用户ID
     * @return 支付记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE user_id = #{userId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<PaymentRecordEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据第三方交易ID查询支付记录
     *
     * @param thirdPartyTransactionId 第三方交易ID
     * @return 支付记录
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE third_party_transaction_id = #{thirdPartyTransactionId} AND deleted_flag = 0")
    PaymentRecordEntity selectByThirdPartyTransactionId(@Param("thirdPartyTransactionId") String thirdPartyTransactionId);

    /**
     * 根据支付ID更新支付记录
     *
     * @param entity 支付记录实体（包含paymentId）
     * @return 更新行数
     */
    @Update({
        "<script>",
        "UPDATE t_consume_payment_record",
        "<set>",
        "<if test='paymentType != null'>, payment_type = #{paymentType}</if>",
        "<if test='subject != null'>, subject = #{subject}</if>",
        "<if test='body != null'>, body = #{body}</if>",
        "<if test='status != null'>, status = #{status}</if>",
        "<if test='thirdPartyTransactionId != null'>, third_party_transaction_id = #{thirdPartyTransactionId}</if>",
        "<if test='prepayId != null'>, prepay_id = #{prepayId}</if>",
        "<if test='qrCode != null'>, qr_code = #{qrCode}</if>",
        "<if test='formData != null'>, form_data = #{formData}</if>",
        "<if test='orderString != null'>, order_string = #{orderString}</if>",
        "<if test='paymentTime != null'>, payment_time = #{paymentTime}</if>",
        "<if test='paymentChannel != null'>, payment_channel = #{paymentChannel}</if>",
        "<if test='paymentIp != null'>, payment_ip = #{paymentIp}</if>",
        "<if test='extendInfo != null'>, extend_info = #{extendInfo}</if>",
        "<if test='remark != null'>, remark = #{remark}</if>",
        ", update_time = #{updateTime}",
        ", update_user_id = #{updateUserId}",
        "</set>",
        "WHERE payment_id = #{paymentId}",
        "</script>"
    })
    int updateByPaymentId(PaymentRecordEntity entity);

    /**
     * 查询指定状态下的支付记录数量
     *
     * @param status 支付状态
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM t_consume_payment_record WHERE status = #{status} AND deleted_flag = 0")
    Long countByStatus(@Param("status") String status);

    /**
     * 查询超时未支付的记录
     *
     * @param timeoutMinutes 超时分钟数
     * @return 支付记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE status = 'PENDING' " +
            "AND create_time < DATE_SUB(NOW(), INTERVAL #{timeoutMinutes} MINUTE) " +
            "AND deleted_flag = 0")
    List<PaymentRecordEntity> selectTimeoutPendingPayments(@Param("timeoutMinutes") Integer timeoutMinutes);

    /**
     * 查询指定时间范围内的支付统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付统计信息
     */
    @Select("SELECT payment_type, COUNT(*) as count, SUM(amount) as total_amount " +
            "FROM t_consume_payment_record " +
            "WHERE status = 'SUCCESS' AND payment_time >= #{startTime} AND payment_time <= #{endTime} " +
            "AND deleted_flag = 0 " +
            "GROUP BY payment_type")
    List<java.util.Map<String, Object>> selectPaymentStatisticsByTimeRange(
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime);
}