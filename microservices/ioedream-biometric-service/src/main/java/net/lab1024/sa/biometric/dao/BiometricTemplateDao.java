package net.lab1024.sa.biometric.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.biometric.domain.entity.BiometricTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生物识别模板数据访问层
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Mapper注解（不使用@Repository）
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * - 所有查询方法使用@Transactional(readOnly = true)
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Mapper
public interface BiometricTemplateDao extends BaseMapper<BiometricTemplateEntity> {

    /**
     * 根据用户ID查询生物识别模板
     *
     * @param userId 用户ID
     * @return 模板列表
     */
    @Select("SELECT * FROM t_biometric_template WHERE user_id = #{userId} AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和生物识别类型查询模板
     *
     * @param userId 用户ID
     * @param biometricType 生物识别类型
     * @return 模板列表
     */
    @Select("SELECT * FROM t_biometric_template WHERE user_id = #{userId} AND biometric_type = #{biometricType} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByUserIdAndType(@Param("userId") Long userId, @Param("biometricType") Integer biometricType);

    /**
     * 根据设备ID查询生物识别模板
     *
     * @param deviceId 设备ID
     * @return 模板列表
     */
    @Select("SELECT * FROM t_biometric_template WHERE device_id = #{deviceId} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 查询用户已激活的模板数量
     *
     * @param userId 用户ID
     * @return 模板数量
     */
    @Select("SELECT COUNT(*) FROM t_biometric_template WHERE user_id = #{userId} AND template_status = 1 AND deleted_flag = 0")
    Integer countActiveTemplatesByUserId(@Param("userId") Long userId);

    /**
     * 查询即将过期的模板
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 模板列表
     */
    @Select("SELECT * FROM t_biometric_template WHERE template_status = 1 AND expire_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectExpiringTemplates(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 更新模板状态
     *
     * @param templateId 模板ID
     * @param templateStatus 模板状态
     * @return 更新行数
     */
    @Update("UPDATE t_biometric_template SET template_status = #{templateStatus}, update_time = NOW() WHERE template_id = #{templateId}")
    int updateTemplateStatus(@Param("templateId") Long templateId, @Param("templateStatus") Integer templateStatus);

    /**
     * 更新模板使用统计
     *
     * @param templateId 模板ID
     * @return 更新行数
     */
    @Update("UPDATE t_biometric_template SET use_count = use_count + 1, last_use_time = NOW(), update_time = NOW() WHERE template_id = #{templateId}")
    int updateUsageStats(@Param("templateId") Long templateId);

    /**
     * 更新验证成功统计
     *
     * @param templateId 模板ID
     * @return 更新行数
     */
    @Update("UPDATE t_biometric_template SET success_count = success_count + 1, update_time = NOW() WHERE template_id = #{templateId}")
    int updateSuccessStats(@Param("templateId") Long templateId);

    /**
     * 更新验证失败统计
     *
     * @param templateId 模板ID
     * @return 更新行数
     */
    @Update("UPDATE t_biometric_template SET fail_count = fail_count + 1, update_time = NOW() WHERE template_id = #{templateId}")
    int updateFailStats(@Param("templateId") Long templateId);

    /**
     * 根据用户ID删除模板
     *
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_biometric_template SET deleted_flag = 1, update_time = NOW() WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和类型删除模板
     *
     * @param userId 用户ID
     * @param biometricType 生物识别类型
     * @return 更新行数
     */
    @Update("UPDATE t_biometric_template SET deleted_flag = 1, update_time = NOW() WHERE user_id = #{userId} AND biometric_type = #{biometricType}")
    int deleteByUserIdAndType(@Param("userId") Long userId, @Param("biometricType") Integer biometricType);

    /**
     * 根据类型和算法版本查询模板
     *
     * @param biometricType 生物识别类型
     * @param algorithmVersion 算法版本
     * @return 模板列表
     */
    @Select("SELECT * FROM t_biometric_template WHERE biometric_type = #{biometricType} AND algorithm_version = #{algorithmVersion} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByTypeAndAlgorithmVersion(@Param("biometricType") Integer biometricType, @Param("algorithmVersion") String algorithmVersion);

    /**
     * 查询模板特征向量
     *
     * @return 模板列表（仅包含特征向量字段）
     */
    @Select("SELECT template_id, user_id, feature_vector FROM t_biometric_template WHERE template_status = 1 AND deleted_flag = 0 AND feature_vector IS NOT NULL")
    List<BiometricTemplateEntity> selectFeatureVectors();

    /**
     * 批量查询模板状态
     *
     * @param templateIds 模板ID列表（逗号分隔）
     * @return 模板列表
     */
    @Select("SELECT template_id, template_status FROM t_biometric_template WHERE template_id IN (${templateIds})")
    List<BiometricTemplateEntity> selectBatchStatus(@Param("templateIds") String templateIds);

    /**
     * 根据生物识别类型查询模板
     *
     * @param biometricType 生物识别类型
     * @return 模板列表
     */
    @Select("SELECT * FROM t_biometric_template WHERE biometric_type = #{biometricType} AND template_status = 1 AND deleted_flag = 0")
    List<BiometricTemplateEntity> selectByBiometricType(@Param("biometricType") Integer biometricType);
}
