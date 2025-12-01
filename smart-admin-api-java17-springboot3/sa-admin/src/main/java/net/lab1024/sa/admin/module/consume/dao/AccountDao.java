package net.lab1024.sa.admin.module.consume.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;

/**
 * 账户数据访问层
 * 负责消费账户的数据库操作
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Mapper
public interface AccountDao extends BaseMapper<AccountEntity> {

    /**
     * 使用乐观锁扣减余额
     *
     * @param accountId  账户ID
     * @param oldBalance 原余额
     * @param newBalance 新余额
     * @param version    版本号
     * @return 更新行数
     */
    int deductBalanceWithVersion(@Param("accountId") Long accountId,
            @Param("oldBalance") BigDecimal oldBalance,
            @Param("newBalance") BigDecimal newBalance,
            @Param("version") Integer version);

    /**
     * 使用乐观锁增加余额
     *
     * @param accountId  账户ID
     * @param oldBalance 原余额
     * @param newBalance 新余额
     * @param version    版本号
     * @return 更新行数
     */
    int addBalanceWithVersion(@Param("accountId") Long accountId,
            @Param("oldBalance") BigDecimal oldBalance,
            @Param("newBalance") BigDecimal newBalance,
            @Param("version") Integer version);

    /**
     * 使用乐观锁更新冻结金额
     *
     * @param accountId       账户ID
     * @param oldFrozenAmount 原冻结金额
     * @param newFrozenAmount 新冻结金额
     * @param version         版本号
     * @return 更新行数
     */
    int updateFrozenAmountWithVersion(@Param("accountId") Long accountId,
            @Param("oldFrozenAmount") BigDecimal oldFrozenAmount,
            @Param("newFrozenAmount") BigDecimal newFrozenAmount,
            @Param("version") Integer version);

    /**
     * 增加累计消费金额
     *
     * @param accountId 账户ID
     * @param amount    消费金额
     * @return 更新行数
     */
    int incrementTotalConsumeAmount(@Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount);

    /**
     * 原子性增加累计充值金额
     * 使用原子性SQL操作，避免并发问题
     *
     * @param accountId 账户ID
     * @param amount    充值金额
     * @return 更新行数
     */
    int incrementTotalRechargeAmount(@Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount);

    /**
     * 增加当前日度消费金额
     *
     * @param accountId 账户ID
     * @param amount    消费金额
     * @return 更新行数
     */
    int incrementCurrentDailyAmount(@Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount);

    /**
     * 增加当前月度消费金额
     *
     * @param accountId 账户ID
     * @param amount    消费金额
     * @return 更新行数
     */
    int incrementCurrentMonthlyAmount(@Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount);

    /**
     * 更新最后消费时间
     *
     * @param accountId       账户ID
     * @param lastConsumeTime 最后消费时间
     * @return 更新行数
     */
    int updateLastConsumeTime(@Param("accountId") Long accountId,
            @Param("lastConsumeTime") LocalDateTime lastConsumeTime);

    /**
     * 更新最后充值时间
     *
     * @param accountId        账户ID
     * @param lastRechargeTime 最后充值时间
     * @return 更新行数
     */
    int updateLastRechargeTime(@Param("accountId") Long accountId,
            @Param("lastRechargeTime") LocalDateTime lastRechargeTime);

    /**
     * 获取指定时间范围内的消费金额
     *
     * @param personId  人员ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 消费金额
     */
    BigDecimal getConsumeAmountByTimeRange(@Param("personId") Long personId,

@Param("startTime") LocalDateTime startTime,
@Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定时间范围内的总消费金额（所有账户）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 总消费金额
     */
    BigDecimal getTotalConsumeAmountByTimeRange(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取所有账户的总余额
     *
     * @return 总余额
     */
    BigDecimal getTotalBalance();

    /**
     * 获取所有账户的总充值金额
     *
     * @return 总充值金额
     */
    BigDecimal getTotalRechargeAmount();
}
