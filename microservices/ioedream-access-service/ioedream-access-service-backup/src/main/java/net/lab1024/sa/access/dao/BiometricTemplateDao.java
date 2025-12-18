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
 * 生物识别模板数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface BiometricTemplateDao extends BaseMapper<BiometricTemplateEntity> {

    /**
     * 根据用户ID查询生物识别模板
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE user_id = #{userId} AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和生物识别类型查询有效模板
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE user_id = #{userId} AND biometric_type = #{biometricType} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByUserIdAndType(@Param("userId") Long userId, @Param("biometricType") Integer biometricType);

    /**
     * 根据设备ID查询生物识别模板
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE device_id = #{deviceId} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 查询用户有效模板数量
     */
    @Select("SELECT COUNT(*) FROM t_access_biometric_template WHERE user_id = #{userId} AND template_status = 1 AND deleted_flag = 0")
    Integer countActiveTemplatesByUserId(@Param("userId") Long userId);

    /**
     * 查询即将过期的模板
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE template_status = 1 AND expire_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectExpiringTemplates(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 更新模板状态
     */
    @Update("UPDATE t_access_biometric_template SET template_status = #{templateStatus}, update_time = NOW() WHERE template_id = #{templateId}")
    int updateTemplateStatus(@Param("templateId") Long templateId, @Param("templateStatus") Integer templateStatus);

    /**
     * 更新模板使用统计
     */
    @Update("UPDATE t_access_biometric_template SET use_count = use_count + 1, last_use_time = NOW(), update_time = NOW() WHERE template_id = #{templateId}")
    int updateUsageStats(@Param("templateId") Long templateId);

    /**
     * 更新认证成功统计
     */
    @Update("UPDATE t_access_biometric_template SET success_count = success_count + 1, update_time = NOW() WHERE template_id = #{templateId}")
    int updateSuccessStats(@Param("templateId") Long templateId);

    /**
     * 更新认证失败统计
     */
    @Update("UPDATE t_access_biometric_template SET fail_count = fail_count + 1, update_time = NOW() WHERE template_id = #{templateId}")
    int updateFailStats(@Param("templateId") Long templateId);

    /**
     * 根据用户ID删除模板
     */
    @Update("UPDATE t_access_biometric_template SET deleted_flag = 1, update_time = NOW() WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据生物识别类型和算法版本查询有效模板
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE biometric_type = #{biometricType} AND algorithm_version = #{algorithmVersion} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByTypeAndAlgorithmVersion(@Param("biometricType") Integer biometricType, @Param("algorithmVersion") String algorithmVersion);

    /**
     * 查询模板特征向量
     */
    @Select("SELECT template_id, user_id, feature_vector FROM t_access_biometric_template WHERE template_status = 1 AND deleted_flag = 0 AND feature_vector IS NOT NULL")
    List<BiometricTemplateEntity> selectFeatureVectors();

    /**
     * 批量查询模板状态
     */
    @Select("SELECT template_id, template_status FROM t_access_biometric_template WHERE template_id IN (${templateIds})")
    List<BiometricTemplateEntity> selectBatchStatus(@Param("templateIds") String templateIds);

    /**
     * 根据生物识别类型查询模板
     */
    @Select("SELECT * FROM t_access_biometric_template WHERE biometric_type = #{biometricType} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByBiometricType(@Param("biometricType") Integer biometricType);
}