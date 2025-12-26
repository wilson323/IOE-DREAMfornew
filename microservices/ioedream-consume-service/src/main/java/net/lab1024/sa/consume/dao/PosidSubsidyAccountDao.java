package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.PosidSubsidyAccountEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * POSID补贴账户表DAO
 *
 * 对应表: POSID_SUBSIDY_ACCOUNT
 * 职责: 补贴账户数据的数据库访问操作
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Mapper
public interface PosidSubsidyAccountDao extends BaseMapper<PosidSubsidyAccountEntity> {

    /**
     * 查询用户的所有补贴账户
     *
     * @param userId 用户ID
     * @return 补贴账户列表（按expire_time升序排序，即将过期优先）
     */
    List<PosidSubsidyAccountEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户指定类型的补贴账户
     *
     * @param userId 用户ID
     * @param subsidyTypeId 补贴类型ID
     * @return 补贴账户列表
     */
    List<PosidSubsidyAccountEntity> selectByUserIdAndTypeId(@Param("userId") Long userId,
                                                             @Param("subsidyTypeId") Long subsidyTypeId);

    /**
     * 查询用户的有效补贴账户（未过期且状态正常）
     *
     * @param userId 用户ID
     * @return 有效补贴账户列表（按expire_time升序，priority升序）
     */
    List<PosidSubsidyAccountEntity> selectValidAccountsByUserId(@Param("userId") Long userId);

    /**
     * 扣减补贴余额（乐观锁）
     *
     * @param subsidyAccountId 补贴账户ID
     * @param amount 扣减金额
     * @return 影响行数
     */
    int deductBalance(@Param("subsidyAccountId") Long subsidyAccountId,
                      @Param("amount") BigDecimal amount);

    /**
     * 查询即将过期的补贴账户
     *
     * @param days 天数（如7天、30天）
     * @return 即将过期的补贴账户列表
     */
    List<PosidSubsidyAccountEntity> selectExpiringAccounts(@Param("days") Integer days);

    /**
     * 查询已过期但未清零的补贴账户
     *
     * @return 已过期未清零的补贴账户列表
     */
    List<PosidSubsidyAccountEntity> selectExpiredAccounts();
}
