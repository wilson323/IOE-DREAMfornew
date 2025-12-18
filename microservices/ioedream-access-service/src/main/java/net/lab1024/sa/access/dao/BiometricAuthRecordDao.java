package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.BiometricAuthRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * 鐢熺墿璇嗗埆楠岃瘉璁板綍鏁版嵁璁块棶灞?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface BiometricAuthRecordDao extends BaseMapper<BiometricAuthRecordEntity> {

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ楠岃瘉璁板綍
     */
    @Select("SELECT * FROM t_access_biometric_auth_record WHERE user_id = #{userId} AND deleted_flag = 0 ORDER BY auth_time DESC")
    List<BiometricAuthRecordEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 鏍规嵁璁惧ID鏌ヨ楠岃瘉璁板綍
     */
    @Select("SELECT * FROM t_access_biometric_auth_record WHERE device_id = #{deviceId} AND deleted_flag = 0 ORDER BY auth_time DESC")
    List<BiometricAuthRecordEntity> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 鏍规嵁楠岃瘉缁撴灉鏌ヨ璁板綍
     */
    @Select("SELECT * FROM t_access_biometric_auth_record WHERE auth_result = #{authResult} AND deleted_flag = 0 ORDER BY auth_time DESC")
    List<BiometricAuthRecordEntity> selectByAuthResult(@Param("authResult") Integer authResult);

    /**
     * 鏌ヨ鍙枒鎿嶄綔璁板綍
     */
    @Select("SELECT * FROM t_access_biometric_auth_record WHERE suspicious_operation = 1 AND deleted_flag = 0 ORDER BY auth_time DESC")
    List<BiometricAuthRecordEntity> selectSuspiciousOperations();

    /**
     * 鏌ヨ闇€瑕佷汉宸ュ鏍哥殑璁板綍
     */
    @Select("SELECT * FROM t_access_biometric_auth_record WHERE manual_review_status = #{status} AND deleted_flag = 0 ORDER BY auth_time DESC")
    List<BiometricAuthRecordEntity> selectByManualReviewStatus(@Param("status") Integer status);

    /**
     * 鏍规嵁鏃堕棿鑼冨洿鏌ヨ璁板綍
     */
    @Select("SELECT * FROM t_access_biometric_auth_record WHERE auth_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0 ORDER BY auth_time DESC")
    List<BiometricAuthRecordEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 鏍规嵁鐢ㄦ埛ID鍜屾椂闂磋寖鍥存煡璇㈣褰?
     */
    @Select("SELECT * FROM t_access_biometric_auth_record WHERE user_id = #{userId} AND auth_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0 ORDER BY auth_time DESC")
    List<BiometricAuthRecordEntity> selectByUserIdAndTimeRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 鏌ヨ楠岃瘉鎴愬姛鐜囩粺璁?
     */
    @Select("SELECT COUNT(*) as total_count, " +
            "SUM(CASE WHEN auth_result = 1 THEN 1 ELSE 0 END) as success_count, " +
            "AVG(CASE WHEN auth_result = 1 THEN match_score ELSE 0 END) as avg_match_score " +
            "FROM t_access_biometric_auth_record " +
            "WHERE deleted_flag = 0 " +
            "AND user_id = #{userId} " +
            "AND auth_time BETWEEN #{startTime} AND #{endTime}")
    List<java.util.Map<String, Object>> selectAuthStatistics(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 鏌ヨ骞冲潎楠岃瘉鏃堕棿
     */
    @Select("SELECT AVG(auth_duration) as avg_duration FROM t_access_biometric_auth_record WHERE deleted_flag = 0 AND biometric_type = #{biometricType}")
    BigDecimal selectAverageAuthDuration(@Param("biometricType") Integer biometricType);

    /**
     * 鏌ヨ楂橀澶辫触璁惧
     */
    @Select("SELECT device_id, COUNT(*) as fail_count " +
            "FROM t_access_biometric_auth_record " +
            "WHERE auth_result = 2 AND deleted_flag = 0 " +
            "GROUP BY device_id " +
            "HAVING COUNT(*) >= #{threshold} " +
            "ORDER BY fail_count DESC")
    List<java.util.Map<String, Object>> selectHighFailureDevices(@Param("threshold") Integer threshold);

    /**
     * 鏇存柊浜哄伐澶嶆牳鐘舵€?
     */
    @Update("UPDATE t_access_biometric_auth_record SET manual_review_status = #{status}, reviewer_id = #{reviewerId}, review_time = NOW(), update_time = NOW() WHERE auth_id = #{authId}")
    int updateManualReviewStatus(@Param("authId") String authId, @Param("status") Integer status, @Param("reviewerId") Long reviewerId);

    /**
     * 鏇存柊澶嶆牳鎰忚
     */
    @Update("UPDATE t_access_biometric_auth_record SET review_comment = #{reviewComment}, update_time = NOW() WHERE auth_id = #{authId}")
    int updateReviewComment(@Param("authId") String authId, @Param("reviewComment") String reviewComment);

    /**
     * 娓呯悊杩囨湡璁板綍
     */
    @Update("UPDATE t_access_biometric_auth_record SET deleted_flag = 1, update_time = NOW() WHERE auth_time < #{expireTime} AND deleted_flag = 0")
    int cleanExpiredRecords(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 鏌ヨ璁惧楠岃瘉瓒嬪娍
     */
    @Select("SELECT DATE(auth_time) as date, " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN auth_result = 1 THEN 1 ELSE 0 END) as success_count " +
            "FROM t_access_biometric_auth_record " +
            "WHERE device_id = #{deviceId} AND deleted_flag = 0 " +
            "AND auth_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(auth_time) " +
            "ORDER BY date DESC")
    List<java.util.Map<String, Object>> selectDeviceAuthTrend(@Param("deviceId") String deviceId, @Param("days") Integer days);
}