package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.PosidTransactionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * POSID交易流水表DAO
 *
 * 对应表: POSID_TRANSACTION
 * 职责: 交易流水数据的数据库访问操作
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Mapper
public interface PosidTransactionDao extends BaseMapper<PosidTransactionEntity> {

    /**
     * 根据订单号查询交易
     *
     * @param orderNo 订单号
     * @return 交易实体
     */
    PosidTransactionEntity selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据交易流水号查询交易
     *
     * @param transactionNo 交易流水号
     * @return 交易实体
     */
    PosidTransactionEntity selectByTransactionNo(@Param("transactionNo") String transactionNo);

    /**
     * 查询用户的交易记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    List<PosidTransactionEntity> selectByUserIdAndTimeRange(@Param("userId") Long userId,
                                                             @Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询待同步的离线交易
     *
     * @return 待同步的交易列表
     */
    List<PosidTransactionEntity> selectPendingSyncTransactions();

    /**
     * 查询账户的交易记录
     *
     * @param accountId 账户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    List<PosidTransactionEntity> selectByAccountIdAndTimeRange(@Param("accountId") Long accountId,
                                                                @Param("startTime") LocalDateTime startTime,
                                                                @Param("endTime") LocalDateTime endTime);
}
