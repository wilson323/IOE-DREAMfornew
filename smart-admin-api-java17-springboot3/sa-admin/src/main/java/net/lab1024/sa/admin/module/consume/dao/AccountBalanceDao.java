package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountBalanceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户余额数据访问层
 * 支持数据一致性管理和乐观锁操作
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Mapper
public interface AccountBalanceDao extends BaseMapper<AccountBalanceEntity> {

    /**
     * 根据账户ID获取余额信息
     */
    @Select("SELECT * FROM t_account_balance WHERE account_id = #{accountId} AND deleted_flag = 0")
    AccountBalanceEntity selectByAccountId(@Param("accountId") Long accountId);

    /**
     * 根据人员ID获取余额信息
     */
    @Select("SELECT * FROM t_account_balance WHERE person_id = #{personId} AND deleted_flag = 0 ORDER BY change_time DESC LIMIT 1")
    AccountBalanceEntity selectByPersonId(@Param("personId") Long personId);

    /**
     * 乐观锁更新余额
     */
    @Update("UPDATE t_account_balance SET " +
            "balance = #{balance}, " +
            "available_balance = #{availableBalance}, " +
            "frozen_amount = #{frozenAmount}, " +
            "last_balance = #{lastBalance}, " +
            "change_amount = #{changeAmount}, " +
            "change_type = #{changeType}, " +
            "change_reason = #{changeReason}, " +
            "order_no = #{orderNo}, " +
            "change_time = #{changeTime}, " +
            "version = version + 1, " +
            "update_time = #{updateTime}, " +
            "update_user_id = #{updateUserId} " +
            "WHERE balance_id = #{balanceId} AND version = #{version} AND deleted_flag = 0")
    int updateWithOptimisticLock(AccountBalanceEntity balanceEntity);

    /**
     * 批量查询需要重新计算的余额记录
     */
    @Select("SELECT * FROM t_account_balance WHERE need_recalculate = 1 AND deleted_flag = 0")
    List<AccountBalanceEntity> selectNeedRecalculate();

    /**
     * 更新一致性状态
     */
    @Update("UPDATE t_account_balance SET " +
            "consistency_status = #{status}, " +
            "last_check_time = #{checkTime}, " +
            "update_time = #{updateTime} " +
            "WHERE balance_id = #{balanceId}")
    int updateConsistencyStatus(@Param("balanceId") Long balanceId,
                              @Param("status") String status,
                              @Param("checkTime") LocalDateTime checkTime,
                              @Param("updateTime") LocalDateTime updateTime);

    /**
     * 计算总消费金额
     */
    @Select("SELECT COALESCE(SUM(CASE WHEN change_type = 'CONSUME' THEN ABS(change_amount) ELSE 0 END), 0) " +
            "FROM t_account_balance WHERE person_id = #{personId} AND deleted_flag = 0")
    BigDecimal calculateTotalConsumedAmount(@Param("personId") Long personId);

    /**
     * 计算总充值金额
     */
    @Select("SELECT COALESCE(SUM(CASE WHEN change_type = 'RECHARGE' THEN change_amount ELSE 0 END), 0) " +
            "FROM t_account_balance WHERE person_id = #{personId} AND deleted_flag = 0")
    BigDecimal calculateTotalRechargedAmount(@Param("personId") Long personId);

    /**
     * 获取余额变动历史
     */
    @Select("SELECT * FROM t_account_balance " +
            "WHERE person_id = #{personId} AND deleted_flag = 0 " +
            "ORDER BY change_time DESC " +
            "LIMIT #{limit}")
    List<AccountBalanceEntity> selectBalanceHistory(@Param("personId") Long personId, @Param("limit") int limit);

    /**
     * 检查数据一致性
     */
    @Select("SELECT COUNT(*) FROM t_account_balance " +
            "WHERE consistency_status = 'INCONSISTENT' AND deleted_flag = 0")
    int countInconsistentRecords();

    /**
     * 获取指定时间范围内的余额变动
     */
    @Select("SELECT * FROM t_account_balance " +
            "WHERE person_id = #{personId} " +
            "AND change_time >= #{startTime} " +
            "AND change_time <= #{endTime} " +
            "AND deleted_flag = 0 " +
            "ORDER BY change_time DESC")
    List<AccountBalanceEntity> selectBalanceByTimeRange(@Param("personId") Long personId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
}