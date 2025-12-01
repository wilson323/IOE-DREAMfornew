package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.RefundRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 退款记录DAO
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Mapper
public interface RefundRecordDao extends BaseMapper<RefundRecordEntity> {

    /**
     * 根据退款ID查询退款记录
     *
     * @param refundId 退款ID
     * @return 退款记录
     */
    @Select("SELECT * FROM t_consume_refund_record WHERE refund_id = #{refundId} AND deleted_flag = 0")
    RefundRecordEntity selectByRefundId(@Param("refundId") String refundId);

    /**
     * 根据支付ID查询退款记录
     *
     * @param paymentId 支付ID
     * @return 退款记录列表
     */
    @Select("SELECT * FROM t_consume_refund_record WHERE payment_id = #{paymentId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<RefundRecordEntity> selectByPaymentId(@Param("paymentId") String paymentId);

    /**
     * 根据消费记录ID查询退款记录
     *
     * @param consumeRecordId 消费记录ID
     * @return 退款记录列表
     */
    @Select("SELECT * FROM t_consume_refund_record WHERE consume_record_id = #{consumeRecordId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<RefundRecordEntity> selectByConsumeRecordId(@Param("consumeRecordId") Long consumeRecordId);

    /**
     * 根据第三方退款ID查询退款记录
     *
     * @param thirdPartyRefundId 第三方退款ID
     * @return 退款记录
     */
    @Select("SELECT * FROM t_consume_refund_record WHERE third_party_refund_id = #{thirdPartyRefundId} AND deleted_flag = 0")
    RefundRecordEntity selectByThirdPartyRefundId(@Param("thirdPartyRefundId") String thirdPartyRefundId);

    /**
     * 根据退款ID更新退款记录
     *
     * @param entity 退款记录实体（包含refundId）
     * @return 更新行数
     */
    @Update({
        "<script>",
        "UPDATE t_consume_refund_record",
        "<set>",
        "<if test='status != null'>, status = #{status}</if>",
        "<if test='thirdPartyRefundId != null'>, third_party_refund_id = #{thirdPartyRefundId}</if>",
        "<if test='refundTime != null'>, refund_time = #{refundTime}</if>",
        "<if test='refundChannel != null'>, refund_channel = #{refundChannel}</if>",
        "<if test='refundWay != null'>, refund_way = #{refundWay}</if>",
        "<if test='refundFee != null'>, refund_fee = #{refundFee}</if>",
        "<if test='processTime != null'>, process_time = #{processTime}</if>",
        "<if test='operatorId != null'>, operator_id = #{operatorId}</if>",
        "<if test='operatorName != null'>, operator_name = #{operatorName}</if>",
        "<if test='extendInfo != null'>, extend_info = #{extendInfo}</if>",
        "<if test='remark != null'>, remark = #{remark}</if>",
        ", update_time = #{updateTime}",
        ", update_user_id = #{updateUserId}",
        "</set>",
        "WHERE refund_id = #{refundId}",
        "</script>"
    })
    int updateByRefundId(RefundRecordEntity entity);

    /**
     * 查询指定状态下的退款记录数量
     *
     * @param status 退款状态
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM t_consume_refund_record WHERE status = #{status} AND deleted_flag = 0")
    Long countByStatus(@Param("status") String status);

    /**
     * 查询指定时间范围内的退款统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as refund_count, " +
            "SUM(refund_amount) as total_refund_amount, " +
            "AVG(refund_amount) as avg_refund_amount " +
            "FROM t_consume_refund_record " +
            "WHERE status = 'SUCCESS' AND refund_time >= #{startTime} AND refund_time <= #{endTime} " +
            "AND deleted_flag = 0")
    java.util.Map<String, Object> selectRefundStatisticsByTimeRange(
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 查询用户退款记录
     *
     * @param consumeRecordId 消费记录ID
     * @param userId 用户ID
     * @return 退款记录列表
     */
    @Select("SELECT * FROM t_consume_refund_record " +
            "WHERE consume_record_id = #{consumeRecordId} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<RefundRecordEntity> selectRefundRecordsByConsumeRecordId(@Param("consumeRecordId") Long consumeRecordId);

    /**
     * 查询退款金额大于指定金额的记录
     *
     * @param minAmount 最小金额
     * @return 退款记录列表
     */
    @Select("SELECT * FROM t_consume_refund_record " +
            "WHERE refund_amount >= #{minAmount} AND status = 'SUCCESS' AND deleted_flag = 0 " +
            "ORDER BY refund_amount DESC")
    List<RefundRecordEntity> selectRefundRecordsByMinAmount(@Param("minAmount") java.math.BigDecimal minAmount);

    /**
     * 查询待处理的退款记录
     *
     * @return 待处理的退款记录列表
     */
    @Select("SELECT * FROM t_consume_refund_record " +
            "WHERE status IN ('PENDING', 'PROCESSING') AND deleted_flag = 0 " +
            "ORDER BY create_time ASC")
    List<RefundRecordEntity> selectPendingRefundRecords();
}