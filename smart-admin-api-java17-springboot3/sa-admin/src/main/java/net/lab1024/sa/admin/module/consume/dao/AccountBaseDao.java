package net.lab1024.sa.admin.module.consume.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountBaseEntity;

/**
 * 消费账户基础DAO接口
 * <p>
 * 基于扩展表架构，提供账户基础信息的数据库访问方法
 * 与AccountExtensionDao配合使用，提供完整的账户功能
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Mapper
public interface AccountBaseDao extends BaseMapper<AccountBaseEntity> {

    /**
     * 根据人员ID查询账户基础信息
     *
     * @param personId 人员ID
     * @return 账户基础信息
     */
    @Select("SELECT * FROM t_account_base WHERE person_id = #{personId} AND deleted_flag = 0")
    AccountBaseEntity selectByPersonId(@Param("personId") Long personId);

    /**
     * 根据账户编号查询账户基础信息
     *
     * @param accountNo 账户编号
     * @return 账户基础信息
     */
    @Select("SELECT * FROM t_account_base WHERE account_no = #{accountNo} AND deleted_flag = 0")
    AccountBaseEntity selectByAccountNo(@Param("accountNo") String accountNo);

    /**
     * 根据员工编号查询账户基础信息
     *
     * @param employeeNo 员工编号
     * @return 账户基础信息
     */
    @Select("SELECT * FROM t_account_base WHERE employee_no = #{employeeNo} AND deleted_flag = 0")
    AccountBaseEntity selectByEmployeeNo(@Param("employeeNo") String employeeNo);

    /**
     * 根据手机号码查询账户基础信息（需要关联扩展表）
     *
     * @param phoneNumber 手机号码
     * @return 账户基础信息列表
     */
    @Select("SELECT ab.* FROM t_account_base ab " +
            "INNER JOIN t_account_extension ae ON ab.account_id = ae.account_id " +
            "WHERE ae.phone_number = #{phoneNumber} AND ab.deleted_flag = 0 AND ae.deleted_flag = 0")
    List<AccountBaseEntity> selectByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * 根据区域ID查询账户基础信息
     *
     * @param regionId 区域ID
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE region_id = #{regionId} AND deleted_flag = 0")
    List<AccountBaseEntity> selectByRegionId(@Param("regionId") String regionId);

    /**
     * 根据部门ID查询账户基础信息
     *
     * @param departmentId 部门ID
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE department_id = #{departmentId} AND deleted_flag = 0")
    List<AccountBaseEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据账户状态查询账户基础信息
     *
     * @param status 账户状态
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE status = #{status} AND deleted_flag = 0")
    List<AccountBaseEntity> selectByStatus(@Param("status") String status);

    /**
     * 根据账户类型查询账户基础信息
     *
     * @param accountType 账户类型
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE account_type = #{accountType} AND deleted_flag = 0")
    List<AccountBaseEntity> selectByAccountType(@Param("accountType") String accountType);

    /**
     * 根据账户等级查询账户基础信息
     *
     * @param accountLevel 账户等级
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE account_level = #{accountLevel} AND deleted_flag = 0")
    List<AccountBaseEntity> selectByAccountLevel(@Param("accountLevel") String accountLevel);

    /**
     * 查询余额低于指定金额的账户
     *
     * @param balance 余额阈值
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE balance <= #{balance} AND deleted_flag = 0")
    List<AccountBaseEntity> selectByLowBalance(@Param("balance") BigDecimal balance);

    /**
     * 查询可用额度低于指定金额的账户
     *
     * @param availableLimit 可用额度阈值
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE available_limit <= #{availableLimit} AND deleted_flag = 0")
    List<AccountBaseEntity> selectByLowAvailableLimit(@Param("availableLimit") BigDecimal availableLimit);

    /**
     * 查询冻结的账户
     *
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE status = 'FROZEN' AND deleted_flag = 0")
    List<AccountBaseEntity> selectFrozenAccounts();

    /**
     * 查询关闭的账户
     *
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE status = 'CLOSED' AND deleted_flag = 0")
    List<AccountBaseEntity> selectClosedAccounts();

    /**
     * 查询活跃的账户
     *
     * @return 账户基础信息列表
     */
    @Select("SELECT * FROM t_account_base WHERE status = 'ACTIVE' AND deleted_flag = 0")
    List<AccountBaseEntity> selectActiveAccounts();

    /**
     * 更新账户余额
     *
     * @param accountId    账户ID
     * @param balance      新余额
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "balance = #{balance}, " +
            "available_limit = (balance + COALESCE(credit_limit, 0) - COALESCE(frozen_amount, 0)), " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateBalance(@Param("accountId") Long accountId,
                     @Param("balance") BigDecimal balance,
                     @Param("updateUserId") Long updateUserId);

    /**
     * 更新账户状态
     *
     * @param accountId    账户ID
     * @param status       新状态
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "status = #{status}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateStatus(@Param("accountId") Long accountId,
                    @Param("status") String status,
                    @Param("updateUserId") Long updateUserId);

    /**
     * 冻结账户
     *
     * @param accountId    账户ID
     * @param freezeReason 冻结原因
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "status = 'FROZEN', " +
            "freeze_time = NOW(), " +
            "freeze_reason = #{freezeReason}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int freezeAccount(@Param("accountId") Long accountId,
                     @Param("freezeReason") String freezeReason,
                     @Param("updateUserId") Long updateUserId);

    /**
     * 解冻账户
     *
     * @param accountId    账户ID
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "status = 'ACTIVE', " +
            "freeze_time = NULL, " +
            "freeze_reason = NULL, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int unfreezeAccount(@Param("accountId") Long accountId, @Param("updateUserId") Long updateUserId);

    /**
     * 关闭账户
     *
     * @param accountId    账户ID
     * @param closeReason  关闭原因
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "status = 'CLOSED', " +
            "close_time = NOW(), " +
            "close_reason = #{closeReason}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int closeAccount(@Param("accountId") Long accountId,
                    @Param("closeReason") String closeReason,
                    @Param("updateUserId") Long updateUserId);

    /**
     * 更新累计消费金额
     *
     * @param accountId           账户ID
     * @param consumeAmount       消费金额
     * @param updateUserId        更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "total_consume_amount = COALESCE(total_consume_amount, 0) + #{consumeAmount}, " +
            "last_consume_time = NOW(), " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateTotalConsumeAmount(@Param("accountId") Long accountId,
                                @Param("consumeAmount") BigDecimal consumeAmount,
                                @Param("updateUserId") Long updateUserId);

    /**
     * 更新累计充值金额
     *
     * @param accountId           账户ID
     * @param rechargeAmount      充值金额
     * @param updateUserId        更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "total_recharge_amount = COALESCE(total_recharge_amount, 0) + #{rechargeAmount}, " +
            "last_recharge_time = NOW(), " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateTotalRechargeAmount(@Param("accountId") Long accountId,
                                 @Param("rechargeAmount") BigDecimal rechargeAmount,
                                 @Param("updateUserId") Long updateUserId);

    /**
     * 更新信用额度
     *
     * @param accountId    账户ID
     * @param creditLimit  新信用额度
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "credit_limit = #{creditLimit}, " +
            "available_limit = (COALESCE(balance, 0) + #{creditLimit} - COALESCE(frozen_amount, 0)), " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateCreditLimit(@Param("accountId") Long accountId,
                         @Param("creditLimit") BigDecimal creditLimit,
                         @Param("updateUserId") Long updateUserId);

    /**
     * 更新冻结金额
     *
     * @param accountId     账户ID
     * @param frozenAmount  新冻结金额
     * @param updateUserId  更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "frozen_amount = #{frozenAmount}, " +
            "available_limit = (COALESCE(balance, 0) + COALESCE(credit_limit, 0) - #{frozenAmount}), " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateFrozenAmount(@Param("accountId") Long accountId,
                          @Param("frozenAmount") BigDecimal frozenAmount,
                          @Param("updateUserId") Long updateUserId);

    /**
     * 更新积分
     *
     * @param accountId    账户ID
     * @param points       新积分
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "points = #{points}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updatePoints(@Param("accountId") Long accountId,
                    @Param("points") Integer points,
                    @Param("updateUserId") Long updateUserId);

    /**
     * 增加积分
     *
     * @param accountId    账户ID
     * @param points       增加的积分
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_base SET " +
            "points = COALESCE(points, 0) + #{points}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int addPoints(@Param("accountId") Long accountId,
                 @Param("points") Integer points,
                 @Param("updateUserId") Long updateUserId);

    /**
     * 检查账户编号是否已存在（排除指定账户）
     *
     * @param accountNo  账户编号
     * @param accountId  排除的账户ID
     * @return 存在的数量
     */
    @Select("SELECT COUNT(*) FROM t_account_base WHERE account_no = #{accountNo} AND account_id != #{accountId} AND deleted_flag = 0")
    int countAccountNoExists(@Param("accountNo") String accountNo, @Param("accountId") Long accountId);

    /**
     * 批量查询账户基础信息
     *
     * @param accountIds 账户ID列表
     * @return 账户基础信息列表
     */
    @Select("<script>" +
            "SELECT * FROM t_account_base WHERE account_id IN " +
            "<foreach collection='accountIds' item='accountId' open='(' separator=',' close=')'>" +
            "#{accountId}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    List<AccountBaseEntity> selectByAccountIds(@Param("accountIds") List<Long> accountIds);

    /**
     * 统计账户基础信息数量
     *
     * @return 总数量
     */
    @Select("SELECT COUNT(*) FROM t_account_base WHERE deleted_flag = 0")
    long countTotal();

    /**
     * 根据状态统计账户数量
     *
     * @return 状态统计信息（格式：active_count, frozen_count, closed_count, suspended_count）
     */
    @Select("SELECT " +
            "SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active_count, " +
            "SUM(CASE WHEN status = 'FROZEN' THEN 1 ELSE 0 END) as frozen_count, " +
            "SUM(CASE WHEN status = 'CLOSED' THEN 1 ELSE 0 END) as closed_count, " +
            "SUM(CASE WHEN status = 'SUSPENDED' THEN 1 ELSE 0 END) as suspended_count " +
            "FROM t_account_base WHERE deleted_flag = 0")
    java.util.Map<String, Object> countByStatus();

    /**
     * 根据账户类型统计账户数量
     *
     * @return 类型统计信息（格式：staff_count, student_count, visitor_count, temp_count）
     */
    @Select("SELECT " +
            "SUM(CASE WHEN account_type = 'STAFF' THEN 1 ELSE 0 END) as staff_count, " +
            "SUM(CASE WHEN account_type = 'STUDENT' THEN 1 ELSE 0 END) as student_count, " +
            "SUM(CASE WHEN account_type = 'VISITOR' THEN 1 ELSE 0 END) as visitor_count, " +
            "SUM(CASE WHEN account_type = 'TEMP' THEN 1 ELSE 0 END) as temp_count " +
            "FROM t_account_base WHERE deleted_flag = 0")
    java.util.Map<String, Object> countByAccountType();

    /**
     * 计算总余额
     *
     * @return 总余额
     */
    @Select("SELECT COALESCE(SUM(balance), 0) FROM t_account_base WHERE status = 'ACTIVE' AND deleted_flag = 0")
    BigDecimal sumTotalBalance();

    /**
     * 计算总信用额度
     *
     * @return 总信用额度
     */
    @Select("SELECT COALESCE(SUM(credit_limit), 0) FROM t_account_base WHERE status = 'ACTIVE' AND deleted_flag = 0")
    BigDecimal sumTotalCreditLimit();

    /**
     * 计算总冻结金额
     *
     * @return 总冻结金额
     */
    @Select("SELECT COALESCE(SUM(frozen_amount), 0) FROM t_account_base WHERE deleted_flag = 0")
    BigDecimal sumTotalFrozenAmount();
}