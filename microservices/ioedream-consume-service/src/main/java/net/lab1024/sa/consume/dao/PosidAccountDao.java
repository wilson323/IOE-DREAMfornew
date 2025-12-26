package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.PosidAccountEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * POSID账户表DAO
 *
 * 对应表: POSID_ACCOUNT
 * 职责: 账户数据的数据库访问操作
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Mapper
public interface PosidAccountDao extends BaseMapper<PosidAccountEntity> {

    /**
     * 根据用户ID查询账户
     *
     * @param userId 用户ID
     * @return 账户实体
     */
    PosidAccountEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 根据账户编码查询账户
     *
     * @param accountCode 账户编码
     * @return 账户实体
     */
    PosidAccountEntity selectByAccountCode(@Param("accountCode") String accountCode);

    /**
     * 扣减账户余额（乐观锁）
     *
     * @param accountId 账户ID
     * @param amount 扣减金额
     * @param version 版本号
     * @return 影响行数
     */
    int deductBalance(@Param("accountId") Long accountId,
                      @Param("amount") BigDecimal amount,
                      @Param("version") Integer version);

    /**
     * 增加账户余额（乐观锁）
     *
     * @param accountId 账户ID
     * @param amount 增加金额
     * @param version 版本号
     * @return 影响行数
     */
    int addBalance(@Param("accountId") Long accountId,
                   @Param("amount") BigDecimal amount,
                   @Param("version") Integer version);

    /**
     * 冻结账户金额
     *
     * @param accountId 账户ID
     * @param amount 冻结金额
     * @return 影响行数
     */
    int freezeAmount(@Param("accountId") Long accountId,
                     @Param("amount") BigDecimal amount);

    /**
     * 解冻账户金额
     *
     * @param accountId 账户ID
     * @param amount 解冻金额
     * @return 影响行数
     */
    int unfreezeAmount(@Param("accountId") Long accountId,
                       @Param("amount") BigDecimal amount);
}
