package net.lab1024.sa.admin.module.consume.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountExtensionEntity;

/**
 * 消费账户扩展DAO接口
 * <p>
 * 基于扩展表架构，提供账户扩展信息的数据库访问方法
 * 支持与AccountBaseDao的联合查询和操作
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Mapper
public interface AccountExtensionDao extends BaseMapper<AccountExtensionEntity> {

    /**
     * 根据账户ID查询扩展信息
     *
     * @param accountId 账户ID
     * @return 账户扩展信息
     */
    @Select("SELECT * FROM t_account_extension WHERE account_id = #{accountId} AND deleted_flag = 0")
    AccountExtensionEntity selectByAccountId(@Param("accountId") Long accountId);

    /**
     * 根据人员ID查询账户扩展信息
     *
     * @param personId 人员ID
     * @return 账户扩展信息列表
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ab.person_id = #{personId} AND ae.deleted_flag = 0 AND ab.deleted_flag = 0")
    List<AccountExtensionEntity> selectByPersonId(@Param("personId") Long personId);

    /**
     * 根据卡片ID查询账户扩展信息
     *
     * @param cardId 卡片ID
     * @return 账户扩展信息
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.card_id = #{cardId} AND ae.deleted_flag = 0 AND ab.deleted_flag = 0")
    AccountExtensionEntity selectByCardId(@Param("cardId") String cardId);

    /**
     * 根据人脸特征ID查询账户扩展信息
     *
     * @param faceFeatureId 人脸特征ID
     * @return 账户扩展信息
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.face_feature_id = #{faceFeatureId} AND ae.deleted_flag = 0 AND ab.deleted_flag = 0")
    AccountExtensionEntity selectByFaceFeatureId(@Param("faceFeatureId") String faceFeatureId);

    /**
     * 根据指纹特征ID查询账户扩展信息
     *
     * @param fingerprintId 指纹特征ID
     * @return 账户扩展信息
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.fingerprint_id = #{fingerprintId} AND ae.deleted_flag = 0 AND ab.deleted_flag = 0")
    AccountExtensionEntity selectByFingerprintId(@Param("fingerprintId") String fingerprintId);

    /**
     * 查询余额低于提醒阈值的账户
     *
     * @return 账户扩展信息列表
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.consume_reminder = 1 AND ab.balance <= ae.reminder_threshold " +
            "AND ae.deleted_flag = 0 AND ab.deleted_flag = 0 " +
            "AND ab.status = 'ACTIVE'")
    List<AccountExtensionEntity> selectLowBalanceAccounts();

    /**
     * 查询需要自动充值的账户
     *
     * @return 账户扩展信息列表
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.auto_recharge = 1 AND ab.balance <= ae.auto_recharge_threshold " +
            "AND ae.deleted_flag = 0 AND ab.deleted_flag = 0 " +
            "AND ab.status = 'ACTIVE'")
    List<AccountExtensionEntity> selectAutoRechargeAccounts();

    /**
     * 查询被锁定的账户
     *
     * @param currentTime 当前时间
     * @return 被锁定的账户扩展信息列表
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.lock_time > #{currentTime} AND ae.deleted_flag = 0 AND ab.deleted_flag = 0")
    List<AccountExtensionEntity> selectLockedAccounts(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 更新日度消费金额
     *
     * @param accountId        账户ID
     * @param consumeAmount    消费金额
     * @return 更新行数
     */
    @Update("UPDATE t_account_extension SET " +
            "current_daily_amount = COALESCE(current_daily_amount, 0) + #{consumeAmount}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateDailyConsumedAmount(@Param("accountId") Long accountId,
                                  @Param("consumeAmount") BigDecimal consumeAmount,
                                  @Param("updateUserId") Long updateUserId);

    /**
     * 更新月度消费金额
     *
     * @param accountId        账户ID
     * @param consumeAmount    消费金额
     * @return 更新行数
     */
    @Update("UPDATE t_account_extension SET " +
            "current_monthly_amount = COALESCE(current_monthly_amount, 0) + #{consumeAmount}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateMonthlyConsumedAmount(@Param("accountId") Long accountId,
                                   @Param("consumeAmount") BigDecimal consumeAmount,
                                   @Param("updateUserId") Long updateUserId);

    /**
     * 重置日度消费金额（新的一天开始时调用）
     *
     * @return 重置的行数
     */
    @Update("UPDATE t_account_extension SET " +
            "current_daily_amount = 0, " +
            "update_time = NOW() " +
            "WHERE deleted_flag = 0")
    int resetAllDailyConsumedAmount();

    /**
     * 重置月度消费金额（新的一月开始时调用）
     *
     * @return 重置的行数
     */
    @Update("UPDATE t_account_extension SET " +
            "current_monthly_amount = 0, " +
            "update_time = NOW() " +
            "WHERE deleted_flag = 0")
    int resetAllMonthlyConsumedAmount();

    /**
     * 增加登录失败次数
     *
     * @param accountId 账户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_extension SET " +
            "login_fail_count = COALESCE(login_fail_count, 0) + 1, " +
            "update_time = NOW() " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int increaseLoginFailCount(@Param("accountId") Long accountId);

    /**
     * 重置登录失败次数并解锁账户
     *
     * @param accountId 账户ID
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_account_extension SET " +
            "login_fail_count = 0, " +
            "lock_time = NULL, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int resetLoginFailCount(@Param("accountId") Long accountId, @Param("updateUserId") Long updateUserId);

    /**
     * 锁定账户
     *
     * @param accountId 账户ID
     * @param lockTime  锁定时间
     * @return 更新行数
     */
    @Update("UPDATE t_account_extension SET " +
            "lock_time = #{lockTime}, " +
            "update_time = NOW() " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int lockAccount(@Param("accountId") Long accountId, @Param("lockTime") LocalDateTime lockTime);

    /**
     * 更新最后登录信息
     *
     * @param accountId 账户ID
     * @param loginIp   登录IP
     * @return 更新行数
     */
    @Update("UPDATE t_account_extension SET " +
            "last_login_ip = #{loginIp}, " +
            "last_login_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0")
    int updateLastLoginInfo(@Param("accountId") Long accountId, @Param("loginIp") String loginIp);

    /**
     * 检查卡片ID是否已存在（排除指定账户）
     *
     * @param cardId    卡片ID
     * @param accountId 排除的账户ID
     * @return 存在的数量
     */
    @Select("SELECT COUNT(*) FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.card_id = #{cardId} AND ae.account_id != #{accountId} " +
            "AND ae.deleted_flag = 0 AND ab.deleted_flag = 0")
    int countCardIdExists(@Param("cardId") String cardId, @Param("accountId") Long accountId);

    /**
     * 批量查询账户扩展信息
     *
     * @param accountIds 账户ID列表
     * @return 账户扩展信息列表
     */
    @Select("<script>" +
            "SELECT * FROM t_account_extension WHERE account_id IN " +
            "<foreach collection='accountIds' item='accountId' open='(' separator=',' close=')'>" +
            "#{accountId}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    List<AccountExtensionEntity> selectByAccountIds(@Param("accountIds") List<Long> accountIds);

    /**
     * 根据手机号码查询账户扩展信息
     *
     * @param phoneNumber 手机号码
     * @return 账户扩展信息列表
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.phone_number = #{phoneNumber} AND ae.deleted_flag = 0 AND ab.deleted_flag = 0")
    List<AccountExtensionEntity> selectByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * 根据邮箱地址查询账户扩展信息
     *
     * @param email 邮箱地址
     * @return 账户扩展信息列表
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.email = #{email} AND ae.deleted_flag = 0 AND ab.deleted_flag = 0")
    List<AccountExtensionEntity> selectByEmail(@Param("email") String email);

    /**
     * 软删除账户扩展信息
     *
     * @param accountId 账户ID
     * @param updateUserId 更新用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_account_extension SET " +
            "deleted_flag = 1, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE account_id = #{accountId}")
    int softDeleteByAccountId(@Param("accountId") Long accountId, @Param("updateUserId") Long updateUserId);

    /**
     * 统计账户扩展信息数量
     *
     * @return 总数量
     */
    @Select("SELECT COUNT(*) FROM t_account_extension WHERE deleted_flag = 0")
    long countTotal();

    /**
     * 统计启用了各种通知的账户数量
     *
     * @return 通知统计信息（格式：sms_count,email_count,consume_count）
     */
    @Select("SELECT " +
            "SUM(CASE WHEN sms_notification = 1 THEN 1 ELSE 0 END) as sms_count, " +
            "SUM(CASE WHEN email_notification = 1 THEN 1 ELSE 0 END) as email_count, " +
            "SUM(CASE WHEN consume_reminder = 1 THEN 1 ELSE 0 END) as consume_count " +
            "FROM t_account_extension WHERE deleted_flag = 0")
    java.util.Map<String, Object> countNotificationStats();

    /**
     * 分页查询余额低于提醒阈值的账户（性能优化）
     *
     * @param offset   偏移量
     * @param pageSize 页面大小
     * @return 账户扩展信息列表
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.consume_reminder = 1 AND ab.balance <= ae.reminder_threshold " +
            "AND ae.deleted_flag = 0 AND ab.deleted_flag = 0 " +
            "AND ab.status = 'ACTIVE' " +
            "ORDER BY ab.balance ASC " +
            "LIMIT #{offset}, #{pageSize}")
    List<AccountExtensionEntity> selectLowBalanceAccountsPage(@Param("offset") Integer offset,
                                                             @Param("pageSize") Integer pageSize);

    /**
     * 统计余额低于提醒阈值的账户总数（配合分页查询）
     *
     * @return 总数量
     */
    @Select("SELECT COUNT(*) FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.consume_reminder = 1 AND ab.balance <= ae.reminder_threshold " +
            "AND ae.deleted_flag = 0 AND ab.deleted_flag = 0 " +
            "AND ab.status = 'ACTIVE'")
    long countLowBalanceAccounts();

    /**
     * 分页查询需要自动充值的账户（性能优化）
     *
     * @param offset   偏移量
     * @param pageSize 页面大小
     * @return 账户扩展信息列表
     */
    @Select("SELECT ae.* FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.auto_recharge = 1 AND ab.balance <= ae.auto_recharge_threshold " +
            "AND ae.deleted_flag = 0 AND ab.deleted_flag = 0 " +
            "AND ab.status = 'ACTIVE' " +
            "ORDER BY ab.balance ASC " +
            "LIMIT #{offset}, #{pageSize}")
    List<AccountExtensionEntity> selectAutoRechargeAccountsPage(@Param("offset") Integer offset,
                                                                @Param("pageSize") Integer pageSize);

    /**
     * 统计需要自动充值的账户总数（配合分页查询）
     *
     * @return 总数量
     */
    @Select("SELECT COUNT(*) FROM t_account_extension ae " +
            "INNER JOIN t_account_base ab ON ae.account_id = ab.account_id " +
            "WHERE ae.auto_recharge = 1 AND ab.balance <= ae.auto_recharge_threshold " +
            "AND ae.deleted_flag = 0 AND ab.deleted_flag = 0 " +
            "AND ab.status = 'ACTIVE'")
    long countAutoRechargeAccounts();
}