package net.lab1024.sa.consume.dao;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.consume.entity.AccountEntity;

/**
 * 账户DAO接口
 * <p>
 * 用于账户的数据访问操作
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@org.apache.ibatis.annotations.Mapper
public interface AccountDao extends BaseMapper<AccountEntity> {

    /**
     * 根据用户ID查询账户信息
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    AccountEntity selectByUserId(@Param("userId") Long userId);

    // ==================== SAGA事务支持的业务方法 ====================

    /**
     * 根据账户ID获取账户余额
     *
     * @param accountId 账户ID
     * @return 账户余额
     */
    @Select("SELECT balance FROM t_consume_account WHERE account_id = #{accountId} AND deleted_flag = 0 FOR UPDATE")
    BigDecimal getBalanceById(@Param("accountId") Long accountId);

    /**
     * 更新账户余额
     * 支持正数（增加）和负数（减少）
     *
     * @param accountId 账户ID
     * @param amount   变动金额（正数增加，负数减少）
     * @return 影响的行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_consume_account SET balance = balance + #{amount}, " +
            "update_time = NOW(), " +
            "version = version + 1, " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateBalance(@Param("accountId") Long accountId,
                     @Param("amount") BigDecimal amount,
                     @Param("updateUserId") Long updateUserId);

    /**
     * 冻结账户金额
     *
     * @param accountId 账户ID
     * @param amount    冻结金额
     * @return 影响的行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_consume_account SET frozen_amount = frozen_amount + #{amount}, " +
            "update_time = NOW(), " +
            "version = version + 1, " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int freezeAmount(@Param("accountId") Long accountId,
                       @Param("amount") BigDecimal amount,
                       @Param("updateUserId") Long updateUserId);

    /**
     * 解冻账户金额
     *
     * @param accountId 账户ID
     * @param amount    解冻金额
     * @return 影响的行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_consume_account SET frozen_amount = frozen_amount - #{amount}, " +
            "update_time = NOW(), " +
            "version = version + 1, " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int unfreezeAmount(@Param("accountId") Long accountId,
                         @Param("amount") BigDecimal amount,
                         @Param("updateUserId") Long updateUserId);

    /**
     * 更新账户状态
     *
     * @param accountId 账户ID
     * @param status    状态值
     * @param updateUserId 更新人ID
     * @return 影响的行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_consume_account SET status = #{status}, " +
            "update_time = NOW(), " +
            "version = version + 1, " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateStatus(@Param("accountId") Long accountId,
                       @Param("status") Integer status,
                       @Param("updateUserId") Long updateUserId);

    /**
     * 检查账户余额是否充足
     *
     * @param accountId 账户ID
     * @param amount    需要的金额
     * @return 是否充足
     */
    @Select("SELECT CASE WHEN (balance - #{amount}) >= 0 THEN 1 ELSE 0 END " +
            "FROM t_consume_account " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    boolean checkBalanceSufficient(@Param("accountId") Long accountId,
                                   @Param("amount") BigDecimal amount);

    /**
     * 获取可用余额（余额 - 冻结金额）
     *
     * @param accountId 账户ID
     * @return 可用余额
     */
    @Select("SELECT (balance - IFNULL(frozen_amount, 0)) AS available_balance " +
            "FROM t_consume_account " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    BigDecimal getAvailableBalance(@Param("accountId") Long accountId);

    /**
     * 获取账户详细信息（包含余额信息）
     *
     * @param accountId 账户ID
     * @return 账户详情
     */
    @Select("SELECT account_id, user_id, account_no, account_name, account_type, " +
            "balance, frozen_amount, credit_limit, daily_limit, monthly_limit, " +
            "allowance_balance, total_recharge_amount, total_consume_amount, " +
            "total_subsidy_amount, status, last_use_time, " +
            "create_time, update_time, create_user_id, update_user_id, " +
            "deleted_flag, version " +
            "FROM t_consume_account " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    AccountEntity selectAccountDetails(@Param("accountId") Long accountId);
}



