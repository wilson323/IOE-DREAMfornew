package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.ConsumeAccountTransactionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户变动记录DAO
 * <p>
 * 提供账户变动记录的数据访问操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Mapper
public interface ConsumeAccountTransactionDao extends BaseMapper<ConsumeAccountTransactionEntity> {

    /**
     * 根据交易流水号查询变动记录
     *
     * @param transactionNo 交易流水号
     * @return 账户变动记录
     */
    @Select("SELECT * FROM t_consume_account_transaction" +
            " WHERE transaction_no = #{transactionNo}" +
            "   AND deleted_flag = 0" +
            " LIMIT 1")
    ConsumeAccountTransactionEntity selectByTransactionNo(@Param("transactionNo") String transactionNo);

    /**
     * 根据业务编号查询变动记录
     *
     * @param businessNo 业务编号
     * @return 账户变动记录列表
     */
    @Select("SELECT * FROM t_consume_account_transaction" +
            " WHERE business_no = #{businessNo}" +
            "   AND deleted_flag = 0" +
            " ORDER BY transaction_time DESC")
    List<ConsumeAccountTransactionEntity> selectByBusinessNo(@Param("businessNo") String businessNo);

    /**
     * 根据关联订单号查询变动记录
     *
     * @param relatedOrderNo 关联订单号
     * @return 账户变动记录列表
     */
    @Select("SELECT * FROM t_consume_account_transaction" +
            " WHERE related_order_no = #{relatedOrderNo}" +
            "   AND deleted_flag = 0" +
            " ORDER BY transaction_time DESC")
    List<ConsumeAccountTransactionEntity> selectByRelatedOrderNo(@Param("relatedOrderNo") String relatedOrderNo);

    /**
     * 查询账户的变动记录
     *
     * @param accountId 账户ID
     * @return 账户变动记录列表
     */
    @Select("SELECT * FROM t_consume_account_transaction" +
            " WHERE account_id = #{accountId}" +
            "   AND deleted_flag = 0" +
            " ORDER BY transaction_time DESC")
    List<ConsumeAccountTransactionEntity> selectByAccountId(@Param("accountId") Long accountId);

    /**
     * 查询用户的变动记录
     *
     * @param userId 用户ID
     * @return 账户变动记录列表
     */
    @Select("SELECT * FROM t_consume_account_transaction" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            " ORDER BY transaction_time DESC")
    List<ConsumeAccountTransactionEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询指定类型的变动记录
     *
     * @param transactionType 交易类型
     * @param accountId 账户ID
     * @return 账户变动记录列表
     */
    @Select("SELECT * FROM t_consume_account_transaction" +
            " WHERE transaction_type = #{transactionType}" +
            "   AND account_id = #{accountId}" +
            "   AND deleted_flag = 0" +
            " ORDER BY transaction_time DESC")
    List<ConsumeAccountTransactionEntity> selectByTypeAndAccount(
            @Param("transactionType") String transactionType,
            @Param("accountId") Long accountId);

    /**
     * 查询指定时间范围的变动记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param accountId 账户ID
     * @return 账户变动记录列表
     */
    @Select("SELECT * FROM t_consume_account_transaction" +
            " WHERE account_id = #{accountId}" +
            "   AND transaction_time >= #{startTime}" +
            "   AND transaction_time <= #{endTime}" +
            "   AND deleted_flag = 0" +
            " ORDER BY transaction_time DESC")
    List<ConsumeAccountTransactionEntity> selectByTimeRange(
            @Param("accountId") Long accountId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计账户总充值金额
     *
     * @param accountId 账户ID
     * @return 总充值金额
     */
    @Select("SELECT SUM(amount) FROM t_consume_account_transaction" +
            " WHERE account_id = #{accountId}" +
            "   AND transaction_type = 'RECHARGE'" +
            "   AND transaction_status = 1" +
            "   AND deleted_flag = 0")
    java.math.BigDecimal sumRechargeByAccountId(@Param("accountId") Long accountId);

    /**
     * 统计账户总消费金额
     *
     * @param accountId 账户ID
     * @return 总消费金额
     */
    @Select("SELECT SUM(ABS(amount)) FROM t_consume_account_transaction" +
            " WHERE account_id = #{accountId}" +
            "   AND transaction_type IN ('CONSUME', 'DEDUCT')" +
            "   AND transaction_status = 1" +
            "   AND deleted_flag = 0")
    java.math.BigDecimal sumConsumeByAccountId(@Param("accountId") Long accountId);

    /**
     * 查询失败的变动记录
     *
     * @param accountId 账户ID
     * @return 失败记录列表
     */
    @Select("SELECT * FROM t_consume_account_transaction" +
            " WHERE account_id = #{accountId}" +
            "   AND transaction_status = 3" +
            "   AND deleted_flag = 0" +
            " ORDER BY transaction_time DESC")
    List<ConsumeAccountTransactionEntity> selectFailedByAccountId(@Param("accountId") Long accountId);

    /**
     * 查询最近的变动记录
     *
     * @param accountId 账户ID
     * @param limit 限制数量
     * @return 账户变动记录列表
     */
    @Select("SELECT * FROM t_consume_account_transaction" +
            " WHERE account_id = #{accountId}" +
            "   AND deleted_flag = 0" +
            " ORDER BY transaction_time DESC" +
            " LIMIT #{limit}")
    List<ConsumeAccountTransactionEntity> selectRecentByAccountId(
            @Param("accountId") Long accountId,
            @Param("limit") int limit);

    /**
     * 统计变动记录数量（按交易类型）
     *
     * @param transactionType 交易类型
     * @param accountId 账户ID
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM t_consume_account_transaction" +
            " WHERE transaction_type = #{transactionType}" +
            "   AND account_id = #{accountId}" +
            "   AND deleted_flag = 0")
    int countByTypeAndAccount(
            @Param("transactionType") String transactionType,
            @Param("accountId") Long accountId);
}
