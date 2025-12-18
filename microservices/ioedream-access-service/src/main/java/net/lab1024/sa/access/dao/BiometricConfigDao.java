package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.BiometricConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 生物识别配置数据访问层
 * <p>
 * 严格遵循DAO设计规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 使用@Mapper注解而非@Repository
 * - 专注于生物识别配置的数据访问操作
 * - 提供常用的查询方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface BiometricConfigDao extends BaseMapper<BiometricConfigEntity> {

    /**
     * 根据生物识别类型和状态查询配置
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE biometric_type = #{biometricType} " +
            "AND config_status = #{configStatus} AND deleted_flag = 0 ORDER BY config_id DESC LIMIT 1")
    BiometricConfigEntity selectByTypeAndStatus(@Param("biometricType") Integer biometricType,
                                              @Param("configStatus") Integer configStatus);

    /**
     * 查询所有激活的配置
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE config_status = 2 AND deleted_flag = 0 ORDER BY biometric_type")
    List<BiometricConfigEntity> selectActiveConfigs();

    /**
     * 根据算法提供者查询配置
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE algorithm_provider = #{provider} " +
            "AND config_status = 2 AND deleted_flag = 0")
    List<BiometricConfigEntity> selectByProvider(@Param("provider") Integer provider);

    /**
     * 查询指定生物识别类型的所有配置
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE biometric_type = #{biometricType} AND deleted_flag = 0 ORDER BY config_id DESC")
    List<BiometricConfigEntity> selectByBiometricType(@Param("biometricType") Integer biometricType);

    /**
     * 查询需要更新的配置（接近过期时间）
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE expire_time <= DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "AND config_status = 2 AND deleted_flag = 0 ORDER BY expire_time ASC")
    List<BiometricConfigEntity> selectConfigsExpiringSoon(@Param("days") Integer days);

    /**
     * 查询自动更新启用的配置
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE auto_update_enabled = 1 " +
            "AND config_status = 2 AND deleted_flag = 0")
    List<BiometricConfigEntity> selectAutoUpdateConfigs();
}