package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.consume.entity.PaymentRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付记录数据访问层
 * <p>
 * 企业级支付记录数据访问接口，提供完整的CRUD操作和复杂查询功能
 * 严格遵循CLAUDE.md全局架构规范：
 * - 统一使用 @Mapper 注解，禁止使用 @Mapper
 * - 必须继承 BaseMapper<Entity>
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Mapper
public interface PaymentRecordDao extends BaseMapper<PaymentRecordEntity> {

    /**
     * 根据支付订单号查询支付记录（主键查询的别名方法）
     *
     * @param paymentId 支付订单号（主键）
     * @return 支付记录
     */
    default PaymentRecordEntity selectByPaymentId(String paymentId) {
        return selectById(paymentId);
    }

    /**
     * 根据订单号查询
     *
     * @param orderNo 订单号
     * @return 支付记录
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE order_no = #{orderNo} AND deleted_flag = 0")
    PaymentRecordEntity selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据交易流水号查询
     *
     * @param transactionNo 交易流水号
     * @return 支付记录
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE transaction_no = #{transactionNo} AND deleted_flag = 0")
    PaymentRecordEntity selectByTransactionNo(@Param("transactionNo") String transactionNo);

    /**
     * 根据用户ID查询支付记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 支付记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE user_id = #{userId} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<PaymentRecordEntity> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据用户ID和时间范围查询支付记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE user_id = #{userId} AND " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<PaymentRecordEntity> selectByUserIdAndTime(@Param("userId") Long userId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 根据商户ID查询支付记录
     *
     * @param merchantId 商户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE merchant_id = #{merchantId} AND " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<PaymentRecordEntity> selectByMerchantId(@Param("merchantId") Long merchantId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询对账记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param merchantId 商户ID（可选）
     * @return 对账记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND " +
            "payment_status = 3 AND deleted_flag = 0 " +
            "AND (#{merchantId} IS NULL OR merchant_id = #{merchantId}) " +
            "ORDER BY create_time DESC")
    List<PaymentRecordEntity> selectForReconciliation(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("merchantId") Long merchantId);

    /**
     * 分页查询支付记录
     *
     * @param page 分页对象
     * @param userId 用户ID（可选）
     * @param merchantId 商户ID（可选）
     * @param paymentStatus 支付状态（可选）
     * @param businessType 业务类型（可选）
     * @param paymentMethod 支付方式（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    default IPage<PaymentRecordEntity> selectPageByCondition(Page<PaymentRecordEntity> page,
                                                           @Param("userId") Long userId,
                                                           @Param("merchantId") Long merchantId,
                                                           @Param("paymentStatus") Integer paymentStatus,
                                                           @Param("businessType") Integer businessType,
                                                           @Param("paymentMethod") Integer paymentMethod,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime) {
        LambdaQueryWrapper<PaymentRecordEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(userId != null, PaymentRecordEntity::getUserId, userId)
               .eq(merchantId != null, PaymentRecordEntity::getMerchantId, merchantId)
               .eq(paymentStatus != null, PaymentRecordEntity::getPaymentStatus, paymentStatus)
               .eq(businessType != null, PaymentRecordEntity::getBusinessType, businessType)
               .eq(paymentMethod != null, PaymentRecordEntity::getPaymentMethod, paymentMethod)
               .ge(startTime != null, PaymentRecordEntity::getCreateTime, startTime)
               .le(endTime != null, PaymentRecordEntity::getCreateTime, endTime)
               .eq(PaymentRecordEntity::getDeletedFlag, 0)
               .orderByDesc(PaymentRecordEntity::getCreateTime);

        return selectPage(page, wrapper);
    }

    /**
     * 查询用户支付统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE user_id = #{userId} AND " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<PaymentRecordEntity> selectUserPaymentStatistics(@Param("userId") Long userId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询商户结算统计
     *
     * @param merchantId 商户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE merchant_id = #{merchantId} AND " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND " +
            "payment_status = 3 AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<PaymentRecordEntity> selectMerchantSettlementStatistics(@Param("merchantId") Long merchantId,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询待结算记录
     *
     * @param days 天数
     * @return 待结算记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE " +
            "payment_status = 3 AND settlement_status = 1 AND " +
            "complete_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) AND deleted_flag = 0 " +
            "ORDER BY complete_time ASC")
    List<PaymentRecordEntity> selectPendingSettlement(@Param("days") Integer days);

    /**
     * 查询高风险支付记录
     *
     * @param hours 小时数
     * @return 高风险记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE " +
            "risk_level >= 3 AND create_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "AND deleted_flag = 0 ORDER BY risk_level DESC, create_time DESC")
    List<PaymentRecordEntity> selectHighRiskPayments(@Param("hours") Integer hours);

    /**
     * 查询异常支付记录
     *
     * @param hours 小时数
     * @return 异常记录列表
     */
    @Select("SELECT * FROM t_consume_payment_record WHERE " +
            "(payment_status = 4 OR audit_status = 3) AND " +
            "create_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<PaymentRecordEntity> selectAbnormalPayments(@Param("hours") Integer hours);

    /**
     * 统计支付记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param merchantId 商户ID（可选）
     * @return 统计结果
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN payment_status = 3 THEN payment_amount ELSE 0 END) as success_amount, " +
            "SUM(CASE WHEN payment_status = 4 THEN payment_amount ELSE 0 END) as failed_amount, " +
            "SUM(CASE WHEN payment_status >= 5 THEN refund_amount ELSE 0 END) as refund_amount, " +
            "COUNT(CASE WHEN payment_status = 3 THEN 1 END) as success_count, " +
            "COUNT(CASE WHEN payment_status = 4 THEN 1 END) as failed_count, " +
            "COUNT(CASE WHEN payment_status >= 5 THEN 1 END) as refund_count " +
            "FROM t_consume_payment_record WHERE " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND " +
            "(#{merchantId} IS NULL OR merchant_id = #{merchantId}) AND deleted_flag = 0")
    Map<String, Object> selectPaymentStatistics(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("merchantId") Long merchantId);

    /**
     * 按支付方式统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "payment_method, " +
            "COUNT(*) as count, " +
            "SUM(payment_amount) as total_amount, " +
            "AVG(payment_amount) as avg_amount " +
            "FROM t_consume_payment_record WHERE " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND " +
            "payment_status = 3 AND deleted_flag = 0 " +
            "GROUP BY payment_method ORDER BY count DESC")
    List<Map<String, Object>> selectStatisticsByPaymentMethod(@Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 按业务类型统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "business_type, " +
            "COUNT(*) as count, " +
            "SUM(payment_amount) as total_amount, " +
            "AVG(payment_amount) as avg_amount " +
            "FROM t_consume_payment_record WHERE " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND " +
            "payment_status = 3 AND deleted_flag = 0 " +
            "GROUP BY business_type ORDER BY count DESC")
    List<Map<String, Object>> selectStatisticsByBusinessType(@Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 按小时统计支付量
     *
     * @param date 日期
     * @return 统计结果
     */
    @Select("SELECT " +
            "HOUR(create_time) as hour, " +
            "COUNT(*) as count, " +
            "SUM(CASE WHEN payment_status = 3 THEN payment_amount ELSE 0 END) as amount " +
            "FROM t_consume_payment_record WHERE " +
            "DATE(create_time) = #{date} AND deleted_flag = 0 " +
            "GROUP BY HOUR(create_time) ORDER BY hour")
    List<Map<String, Object>> selectHourlyStatistics(@Param("date") String date);

    /**
     * 批量更新结算状态
     *
     * @param paymentIds 支付记录ID列表
     * @param settlementStatus 结算状态
     * @return 更新行数
     */
    default int updateSettlementStatusBatch(@Param("paymentIds") List<String> paymentIds,
                                           @Param("settlementStatus") Integer settlementStatus) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return 0;
        }

        LambdaQueryWrapper<PaymentRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PaymentRecordEntity::getPaymentId, paymentIds);

        PaymentRecordEntity updateEntity = new PaymentRecordEntity();
        updateEntity.setSettlementStatus(settlementStatus);
        if (settlementStatus == 2) { // 已结算
            updateEntity.setSettlementTime(LocalDateTime.now());
        }

        return update(updateEntity, wrapper);
    }

    /**
     * 软删除支付记录
     *
     * @param paymentId 支付记录ID
     * @return 删除行数
     */
    default int softDeleteById(@Param("paymentId") String paymentId) {
        PaymentRecordEntity updateEntity = new PaymentRecordEntity();
        updateEntity.setPaymentId(paymentId);
        updateEntity.setDeletedFlag(1);

        LambdaQueryWrapper<PaymentRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecordEntity::getPaymentId, paymentId);

        return update(updateEntity, wrapper);
    }

    /**
     * 检查订单号是否存在
     *
     * @param orderNo 订单号
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM t_consume_payment_record WHERE order_no = #{orderNo} AND deleted_flag = 0")
    int checkOrderNoExists(@Param("orderNo") String orderNo);

    /**
     * 检查交易流水号是否存在
     *
     * @param transactionNo 交易流水号
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM t_consume_payment_record WHERE transaction_no = #{transactionNo} AND deleted_flag = 0")
    int checkTransactionNoExists(@Param("transactionNo") String transactionNo);
}




