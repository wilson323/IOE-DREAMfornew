package net.lab1024.sa.common.consume.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款记录数据访问层
 * <p>
 * 企业级退款记录数据访问接口，提供完整的CRUD操作和复杂查询功能
 * 严格遵循CLAUDE.md全局架构规范：
 * - 统一使用 @Mapper 注解，禁止使用 @Repository
 * - 必须继承 BaseMapper<Entity>
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Mapper
public interface PaymentRefundRecordDao extends BaseMapper<PaymentRefundRecordEntity> {

    /**
     * 根据退款单号查询
     *
     * @param refundNo 退款单号
     * @return 退款记录
     */
    @Select("SELECT * FROM t_consume_payment_refund_record WHERE refund_no = #{refundNo} AND deleted_flag = 0")
    PaymentRefundRecordEntity selectByRefundNo(@Param("refundNo") String refundNo);

    /**
     * 根据退款流水号查询
     *
     * @param refundTransactionNo 退款流水号
     * @return 退款记录
     */
    @Select("SELECT * FROM t_consume_payment_refund_record WHERE refund_transaction_no = #{refundTransactionNo} AND deleted_flag = 0")
    PaymentRefundRecordEntity selectByRefundTransactionNo(@Param("refundTransactionNo") String refundTransactionNo);

    /**
     * 根据原支付记录ID查询退款记录
     *
     * @param paymentId 原支付记录ID
     * @return 退款记录列表
     */
    @Select("SELECT * FROM t_consume_payment_refund_record WHERE payment_id = #{paymentId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<PaymentRefundRecordEntity> selectByPaymentId(@Param("paymentId") String paymentId);

    /**
     * 根据用户ID查询退款记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 退款记录列表
     */
    @Select("SELECT * FROM t_consume_payment_refund_record WHERE user_id = #{userId} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<PaymentRefundRecordEntity> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据用户ID和时间范围查询退款记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款记录列表
     */
    @Select("SELECT * FROM t_consume_payment_refund_record WHERE user_id = #{userId} AND " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<PaymentRefundRecordEntity> selectByUserIdAndTime(@Param("userId") Long userId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 查询待审核退款记录
     *
     * @return 待审核退款记录列表
     */
    @Select("SELECT * FROM t_consume_payment_refund_record WHERE " +
            "refund_status IN (1, 2) AND deleted_flag = 0 ORDER BY create_time ASC")
    List<PaymentRefundRecordEntity> selectPendingAudit();

    /**
     * 查询待处理退款记录
     *
     * @return 待处理退款记录列表
     */
    @Select("SELECT * FROM t_consume_payment_refund_record WHERE " +
            "refund_status = 3 AND deleted_flag = 0 ORDER BY audit_time ASC")
    List<PaymentRefundRecordEntity> selectPendingProcess();

    /**
     * 分页查询退款记录
     *
     * @param page 分页对象
     * @param userId 用户ID（可选）
     * @param paymentId 支付记录ID（可选）
     * @param refundStatus 退款状态（可选）
     * @param refundType 退款类型（可选）
     * @param applicantId 申请人ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    default IPage<PaymentRefundRecordEntity> selectPageByCondition(Page<PaymentRefundRecordEntity> page,
                                                              @Param("userId") Long userId,
                                                              @Param("paymentId") String paymentId,
                                                              @Param("refundStatus") Integer refundStatus,
                                                              @Param("refundType") Integer refundType,
                                                              @Param("applicantId") Long applicantId,
                                                              @Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime) {
        LambdaQueryWrapper<PaymentRefundRecordEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(userId != null, PaymentRefundRecordEntity::getUserId, userId)
               .eq(paymentId != null, PaymentRefundRecordEntity::getPaymentId, paymentId)
               .eq(refundStatus != null, PaymentRefundRecordEntity::getRefundStatus, refundStatus)
               .eq(refundType != null, PaymentRefundRecordEntity::getRefundType, refundType)
               .eq(applicantId != null, PaymentRefundRecordEntity::getApplicantId, applicantId)
               .ge(startTime != null, PaymentRefundRecordEntity::getCreateTime, startTime)
               .le(endTime != null, PaymentRefundRecordEntity::getCreateTime, endTime)
               .eq(PaymentRefundRecordEntity::getDeletedFlag, 0)
               .orderByDesc(PaymentRefundRecordEntity::getCreateTime);

        return selectPage(page, wrapper);
    }

    /**
     * 查询退款统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(refund_amount) as total_refund_amount, " +
            "SUM(actual_refund_amount) as total_actual_amount, " +
            "SUM(refund_fee) as total_refund_fee, " +
            "COUNT(CASE WHEN refund_status = 6 THEN 1 END) as success_count, " +
            "COUNT(CASE WHEN refund_status = 7 THEN 1 END) as failed_count " +
            "FROM t_consume_payment_refund_record WHERE " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND deleted_flag = 0")
    List<java.util.Map<String, Object>> selectRefundStatistics(@Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 按退款类型统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "refund_type, " +
            "COUNT(*) as count, " +
            "SUM(refund_amount) as total_amount, " +
            "AVG(refund_amount) as avg_amount " +
            "FROM t_consume_payment_refund_record WHERE " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND " +
            "refund_status = 6 AND deleted_flag = 0 " +
            "GROUP BY refund_type ORDER BY count DESC")
    List<java.util.Map<String, Object>> selectStatisticsByRefundType(@Param("startTime") LocalDateTime startTime,
                                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 按退款原因统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "refund_reason_type, " +
            "COUNT(*) as count, " +
            "SUM(refund_amount) as total_amount " +
            "FROM t_consume_payment_refund_record WHERE " +
            "create_time >= #{startTime} AND create_time <= #{endTime} AND " +
            "refund_status = 6 AND deleted_flag = 0 " +
            "GROUP BY refund_reason_type ORDER BY count DESC")
    List<java.util.Map<String, Object>> selectStatisticsByRefundReason(@Param("startTime") LocalDateTime startTime,
                                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查询高频退款用户
     *
     * @param days 天数
     * @param minRefundCount 最小退款次数
     * @return 高频退款用户列表
     */
    @Select("SELECT " +
            "user_id, " +
            "COUNT(*) as refund_count, " +
            "SUM(refund_amount) as total_refund_amount " +
            "FROM t_consume_payment_refund_record WHERE " +
            "create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) AND " +
            "refund_status = 6 AND deleted_flag = 0 " +
            "GROUP BY user_id HAVING COUNT(*) >= #{minRefundCount} " +
            "ORDER BY refund_count DESC")
    List<java.util.Map<String, Object>> selectHighFrequencyRefundUsers(@Param("days") Integer days,
                                                                       @Param("minRefundCount") Integer minRefundCount);

    /**
     * 查询高风险退款记录
     *
     * @param hours 小时数
     * @return 高风险退款记录列表
     */
    @Select("SELECT * FROM t_consume_payment_refund_record WHERE " +
            "risk_level >= 3 AND create_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "AND deleted_flag = 0 ORDER BY risk_level DESC, create_time DESC")
    List<PaymentRefundRecordEntity> selectHighRiskRefunds(@Param("hours") Integer hours);

    /**
     * 批量更新退款状态
     *
     * @param refundIds 退款记录ID列表
     * @param refundStatus 退款状态
     * @return 更新行数
     */
    default int updateRefundStatusBatch(@Param("refundIds") List<String> refundIds,
                                       @Param("refundStatus") Integer refundStatus) {
        if (refundIds == null || refundIds.isEmpty()) {
            return 0;
        }

        LambdaQueryWrapper<PaymentRefundRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PaymentRefundRecordEntity::getRefundId, refundIds);

        PaymentRefundRecordEntity updateEntity = new PaymentRefundRecordEntity();
        updateEntity.setRefundStatus(refundStatus);
        if (refundStatus == 6) { // 退款成功
            updateEntity.setCompleteTime(LocalDateTime.now());
        }

        return update(updateEntity, wrapper);
    }

    /**
     * 审核退款记录
     *
     * @param refundId 退款记录ID
     * @param auditStatus 审核状态
     * @param auditorId 审核人ID
     * @param auditComment 审核意见
     * @return 更新行数
     */
    default int auditRefund(@Param("refundId") String refundId,
                          @Param("auditStatus") Integer auditStatus,
                          @Param("auditorId") Long auditorId,
                          @Param("auditComment") String auditComment) {
        PaymentRefundRecordEntity updateEntity = new PaymentRefundRecordEntity();
        updateEntity.setRefundId(refundId);
        updateEntity.setRefundStatus(auditStatus);
        updateEntity.setAuditorId(auditorId);
        updateEntity.setAuditTime(LocalDateTime.now());
        updateEntity.setAuditComment(auditComment);

        LambdaQueryWrapper<PaymentRefundRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRefundRecordEntity::getRefundId, refundId);

        return update(updateEntity, wrapper);
    }

    /**
     * 处理退款记录
     *
     * @param refundId 退款记录ID
     * @param processorId 处理人ID
     * @return 更新行数
     */
    default int processRefund(@Param("refundId") String refundId,
                           @Param("processorId") Long processorId) {
        PaymentRefundRecordEntity updateEntity = new PaymentRefundRecordEntity();
        updateEntity.setRefundId(refundId);
        updateEntity.setRefundStatus(5); // 退款中
        updateEntity.setProcessorId(processorId);
        updateEntity.setProcessTime(LocalDateTime.now());

        LambdaQueryWrapper<PaymentRefundRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRefundRecordEntity::getRefundId, refundId);

        return update(updateEntity, wrapper);
    }

    /**
     * 完成退款处理
     *
     * @param refundId 退款记录ID
     * @param thirdPartyRefundNo 第三方退款单号
     * @return 更新行数
     */
    default int completeRefund(@Param("refundId") String refundId,
                             @Param("thirdPartyRefundNo") String thirdPartyRefundNo) {
        PaymentRefundRecordEntity updateEntity = new PaymentRefundRecordEntity();
        updateEntity.setRefundId(refundId);
        updateEntity.setRefundStatus(6); // 退款成功
        updateEntity.setCompleteTime(LocalDateTime.now());
        updateEntity.setThirdPartyRefundNo(thirdPartyRefundNo);

        LambdaQueryWrapper<PaymentRefundRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRefundRecordEntity::getRefundId, refundId);

        return update(updateEntity, wrapper);
    }

    /**
     * 软删除退款记录
     *
     * @param refundId 退款记录ID
     * @return 删除行数
     */
    default int softDeleteById(@Param("refundId") String refundId) {
        PaymentRefundRecordEntity updateEntity = new PaymentRefundRecordEntity();
        updateEntity.setRefundId(refundId);
        updateEntity.setDeletedFlag(1);

        LambdaQueryWrapper<PaymentRefundRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRefundRecordEntity::getRefundId, refundId);

        return update(updateEntity, wrapper);
    }

    /**
     * 检查退款单号是否存在
     *
     * @param refundNo 退款单号
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM t_consume_payment_refund_record WHERE refund_no = #{refundNo} AND deleted_flag = 0")
    int checkRefundNoExists(@Param("refundNo") String refundNo);

    /**
     * 检查退款流水号是否存在
     *
     * @param refundTransactionNo 退款流水号
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM t_consume_payment_refund_record WHERE refund_transaction_no = #{refundTransactionNo} AND deleted_flag = 0")
    int checkRefundTransactionNoExists(@Param("refundTransactionNo") String refundTransactionNo);

    /**
     * 获取用户退款统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_refund_count, " +
            "SUM(refund_amount) as total_refund_amount, " +
            "SUM(actual_refund_amount) as total_actual_amount, " +
            "COUNT(CASE WHEN refund_status = 6 THEN 1 END) as success_count, " +
            "COUNT(CASE WHEN refund_status = 7 THEN 1 END) as failed_count " +
            "FROM t_consume_payment_refund_record WHERE " +
            "user_id = #{userId} AND create_time >= #{startTime} AND create_time <= #{endTime} AND deleted_flag = 0")
    java.util.Map<String, Object> getUserRefundStatistics(@Param("userId") Long userId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);
}