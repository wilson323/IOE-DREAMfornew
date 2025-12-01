/*
 * 支付密码数据访问层
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.PaymentPasswordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付密码数据访问层
 * 提供支付密码相关的数据访问操作
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Mapper
public interface PaymentPasswordDao extends BaseMapper<PaymentPasswordEntity> {

    /**
     * 根据人员ID查询支付密码
     *
     * @param personId 人员ID
     * @return 支付密码实体
     */
    @Select("SELECT * FROM t_payment_password WHERE person_id = #{personId} AND deleted_flag = 0")
    PaymentPasswordEntity selectByPersonId(@Param("personId") Long personId);

    /**
     * 根据人员ID查询有效的支付密码（未过期且未被锁定）
     *
     * @param personId 人员ID
     * @return 有效的支付密码实体
     */
    @Select("SELECT * FROM t_payment_password WHERE person_id = #{personId} AND status = 'ACTIVE' " +
            "AND (expired_time IS NULL OR expired_time > NOW()) " +
            "AND (lock_until IS NULL OR lock_until < NOW()) " +
            "AND deleted_flag = 0")
    PaymentPasswordEntity selectActiveByPersonId(@Param("personId") Long personId);

    /**
     * 查询即将过期的支付密码（指定天数内）
     *
     * @param days 过期天数
     * @return 即将过期的支付密码列表
     */
    @Select("SELECT * FROM t_payment_password WHERE status = 'ACTIVE' " +
            "AND expired_time IS NOT NULL AND expired_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted_flag = 0 ORDER BY expired_time ASC")
    List<PaymentPasswordEntity> selectExpiringSoon(@Param("days") Integer days);

    /**
     * 查询已过期的支付密码
     *
     * @return 已过期的支付密码列表
     */
    @Select("SELECT * FROM t_payment_password WHERE status = 'ACTIVE' " +
            "AND expired_time IS NOT NULL AND expired_time <= NOW() " +
            "AND deleted_flag = 0 ORDER BY expired_time DESC")
    List<PaymentPasswordEntity> selectExpired();

    /**
     * 查询被锁定的支付密码（锁定时间未过期）
     *
     * @return 被锁定的支付密码列表
     */
    @Select("SELECT * FROM t_payment_password WHERE status = 'LOCKED' " +
            "AND (lock_until IS NULL OR lock_until > NOW()) " +
            "AND deleted_flag = 0 ORDER BY lock_time DESC")
    List<PaymentPasswordEntity> selectLocked();

    /**
     * 批量更新过期状态
     *
     * @return 更新记录数
     */
    @Update("UPDATE t_payment_password SET status = 'EXPIRED', update_time = NOW() " +
            "WHERE status = 'ACTIVE' AND expired_time IS NOT NULL AND expired_time <= NOW() " +
            "AND deleted_flag = 0")
    int batchUpdateExpiredStatus();

    /**
     * 批量解锁过期锁定的密码
     *
     * @return 更新记录数
     */
    @Update("UPDATE t_payment_password SET status = 'ACTIVE', lock_until = NULL, " +
            "lock_reason = NULL, update_time = NOW() " +
            "WHERE status = 'LOCKED' AND lock_until IS NOT NULL AND lock_until <= NOW() " +
            "AND deleted_flag = 0")
    int batchUnlockExpired();

    /**
     * 统计指定时间范围内的验证次数
     *
     * @param personId 人员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 验证次数
     */
    @Select("SELECT COUNT(*) FROM t_payment_password WHERE person_id = #{personId} " +
            "AND last_verify_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted_flag = 0")
    Integer countVerifyByTimeRange(@Param("personId") Long personId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的失败次数
     *
     * @param personId 人员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败次数
     */
    @Select("SELECT COUNT(*) FROM t_payment_password WHERE person_id = #{personId} " +
            "AND last_verify_time BETWEEN #{startTime} AND #{endTime} " +
            "AND failure_count > 0 " +
            "AND deleted_flag = 0")
    Integer countFailureByTimeRange(@Param("personId") Long personId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询验证次数异常的密码记录
     *
     * @param threshold 阈值次数
     * @param timeWindowMinutes 时间窗口（分钟）
     * @return 异常记录列表
     */
    @Select("SELECT * FROM t_payment_password WHERE total_verify_count >= #{threshold} " +
            "AND last_verify_time >= DATE_SUB(NOW(), INTERVAL #{timeWindowMinutes} MINUTE) " +
            "AND deleted_flag = 0 ORDER BY total_verify_count DESC")
    List<PaymentPasswordEntity> selectAbnormalVerification(@Param("threshold") Integer threshold,
                                                        @Param("timeWindowMinutes") Integer timeWindowMinutes);

    /**
     * 查询弱密码记录
     *
     * @return 弱密码记录列表
     */
    @Select("SELECT * FROM t_payment_password WHERE password_score < 60 " +
            "AND status = 'ACTIVE' AND deleted_flag = 0 ORDER BY password_score ASC")
    List<PaymentPasswordEntity> selectWeakPasswords();

    /**
     * 查询启用了生物特征验证的记录
     *
     * @return 生物特征验证记录列表
     */
    @Select("SELECT * FROM t_payment_password WHERE enable_biometric = 1 " +
            "AND status = 'ACTIVE' AND deleted_flag = 0")
    List<PaymentPasswordEntity> selectBiometricEnabled();

    /**
     * 根据生物特征类型查询
     *
     * @param biometricType 生物特征类型
     * @return 记录列表
     */
    @Select("SELECT * FROM t_payment_password WHERE enable_biometric = 1 " +
            "AND FIND_IN_SET(#{biometricType}, biometric_types) > 0 " +
            "AND status = 'ACTIVE' AND deleted_flag = 0")
    List<PaymentPasswordEntity> selectByBiometricType(@Param("biometricType") String biometricType);

    /**
     * 更新最后验证信息
     *
     * @param passwordId 密码ID
     * @param clientIp 客户端IP
     * @param deviceInfo 设备信息
     * @return 更新记录数
     */
    @Update("UPDATE t_payment_password SET last_verify_time = NOW(), " +
            "last_verify_ip = #{clientIp}, last_verify_device = #{deviceInfo}, " +
            "total_verify_count = IFNULL(total_verify_count, 0) + 1 " +
            "WHERE password_id = #{passwordId} AND deleted_flag = 0")
    int updateLastVerifyInfo(@Param("passwordId") Long passwordId,
                             @Param("clientIp") String clientIp,
                             @Param("deviceInfo") String deviceInfo);

    /**
     * 更新验证失败信息
     *
     * @param passwordId 密码ID
     * @param clientIp 客户端IP
     * @param deviceInfo 设备信息
     * @return 更新记录数
     */
    @Update("UPDATE t_payment_password SET last_verify_time = NOW(), " +
            "last_verify_ip = #{clientIp}, last_verify_device = #{deviceInfo}, " +
            "failure_count = IFNULL(failure_count, 0) + 1, " +
            "total_verify_count = IFNULL(total_verify_count, 0) + 1 " +
            "WHERE password_id = #{passwordId} AND deleted_flag = 0")
    int updateFailureInfo(@Param("passwordId") Long passwordId,
                         @Param("clientIp") String clientIp,
                         @Param("deviceInfo") String deviceInfo);

    /**
     * 重置验证失败次数
     *
     * @param passwordId 密码ID
     * @return 更新记录数
     */
    @Update("UPDATE t_payment_password SET failure_count = 0 " +
            "WHERE password_id = #{passwordId} AND deleted_flag = 0")
    int resetFailureCount(@Param("passwordId") Long passwordId);

    /**
     * 查询需要强制更新密码的记录
     *
     * @return 需要强制更新的记录列表
     */
    @Select("SELECT * FROM t_payment_password WHERE force_password_change = 1 " +
            "AND status = 'ACTIVE' AND deleted_flag = 0")
    List<PaymentPasswordEntity> selectForceChangeRequired();

    /**
     * 获取密码统计信息
     *
     * @return 统计信息MAP
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active_count, " +
            "SUM(CASE WHEN status = 'LOCKED' THEN 1 ELSE 0 END) as locked_count, " +
            "SUM(CASE WHEN status = 'DISABLED' THEN 1 ELSE 0 END) as disabled_count, " +
            "SUM(CASE WHEN expired_time <= NOW() THEN 1 ELSE 0 END) as expired_count, " +
            "SUM(CASE WHEN enable_biometric = 1 THEN 1 ELSE 0 END) as biometric_count " +
            "FROM t_payment_password WHERE deleted_flag = 0")
    java.util.Map<String, Object> getPasswordStatistics();
}