package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.BiometricConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 生物识别配置数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface BiometricConfigDao extends BaseMapper<BiometricConfigEntity> {

    /**
     * 根据生物识别类型查询激活配置
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE biometric_type = #{biometricType} " +
            "AND config_status = #{configStatus} AND deleted_flag = 0 ORDER BY config_id DESC LIMIT 1")
    BiometricConfigEntity selectByTypeAndStatus(@Param("biometricType") Integer biometricType,
                                              @Param("configStatus") Integer configStatus);

    /**
     * 查询所有激活配置
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE config_status = 2 AND deleted_flag = 0 ORDER BY biometric_type")
    List<BiometricConfigEntity> selectActiveConfigs();

    /**
     * 根据算法提供商查询配置
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE algorithm_provider = #{provider} " +
            "AND config_status = 2 AND deleted_flag = 0")
    List<BiometricConfigEntity> selectByProvider(@Param("provider") Integer provider);
}