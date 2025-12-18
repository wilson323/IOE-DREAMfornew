package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.BiometricTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 鐢熺墿璇嗗埆妯℃澘鏁版嵁璁块棶灞?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface BiometricTemplateDao extends BaseMapper<BiometricTemplateEntity> {

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ鐢熺墿璇嗗埆妯℃澘
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE user_id = #{userId} AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 鏍规嵁鐢ㄦ埛ID鍜岀敓鐗╄瘑鍒被鍨嬫煡璇㈡ā鏉?
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE user_id = #{userId} AND biometric_type = #{biometricType} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByUserIdAndType(@Param("userId") Long userId, @Param("biometricType") Integer biometricType);

    /**
     * 鏍规嵁璁惧ID鏌ヨ鐢熺墿璇嗗埆妯℃澘
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE device_id = #{deviceId} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 鏌ヨ鐢ㄦ埛宸叉縺娲荤殑妯℃澘鏁伴噺
     */
    @Select("SELECT COUNT(*) FROM t_access_biometric_template WHERE user_id = #{userId} AND template_status = 1 AND deleted_flag = 0")
    Integer countActiveTemplatesByUserId(@Param("userId") Long userId);

    /**
     * 鏌ヨ鍗冲皢杩囨湡鐨勬ā鏉?
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE template_status = 1 AND expire_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectExpiringTemplates(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 鏇存柊妯℃澘鐘舵€?
     */
    @Update("UPDATE t_access_biometric_template SET template_status = #{templateStatus}, update_time = NOW() WHERE template_id = #{templateId}")
    int updateTemplateStatus(@Param("templateId") Long templateId, @Param("templateStatus") Integer templateStatus);

    /**
     * 鏇存柊妯℃澘浣跨敤缁熻
     */
    @Update("UPDATE t_access_biometric_template SET use_count = use_count + 1, last_use_time = NOW(), update_time = NOW() WHERE template_id = #{templateId}")
    int updateUsageStats(@Param("templateId") Long templateId);

    /**
     * 鏇存柊楠岃瘉鎴愬姛缁熻
     */
    @Update("UPDATE t_access_biometric_template SET success_count = success_count + 1, update_time = NOW() WHERE template_id = #{templateId}")
    int updateSuccessStats(@Param("templateId") Long templateId);

    /**
     * 鏇存柊楠岃瘉澶辫触缁熻
     */
    @Update("UPDATE t_access_biometric_template SET fail_count = fail_count + 1, update_time = NOW() WHERE template_id = #{templateId}")
    int updateFailStats(@Param("templateId") Long templateId);

    /**
     * 鏍规嵁鐢ㄦ埛ID鍒犻櫎妯℃澘
     */
    @Update("UPDATE t_access_biometric_template SET deleted_flag = 1, update_time = NOW() WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 鏍规嵁绫诲瀷鍜岀畻娉曠増鏈煡璇㈡ā鏉?
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE biometric_type = #{biometricType} AND algorithm_version = #{algorithmVersion} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByTypeAndAlgorithmVersion(@Param("biometricType") Integer biometricType, @Param("algorithmVersion") String algorithmVersion);

    /**
     * 鏌ヨ妯℃澘鐗瑰緛鍚戦噺
     */
    @Select("SELECT template_id, user_id, feature_vector FROM t_access_biometric_template WHERE template_status = 1 AND deleted_flag = 0 AND feature_vector IS NOT NULL")
    List<BiometricTemplateEntity> selectFeatureVectors();

    /**
     * 鎵归噺鏌ヨ妯℃澘鐘舵€?
     */
    @Select("SELECT template_id, template_status FROM t_access_biometric_template WHERE template_id IN (${templateIds})")
    List<BiometricTemplateEntity> selectBatchStatus(@Param("templateIds") String templateIds);

    /**
     * 鏍规嵁鐢熺墿璇嗗埆绫诲瀷鏌ヨ妯℃澘
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE biometric_type = #{biometricType} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByBiometricType(@Param("biometricType") Integer biometricType);
}