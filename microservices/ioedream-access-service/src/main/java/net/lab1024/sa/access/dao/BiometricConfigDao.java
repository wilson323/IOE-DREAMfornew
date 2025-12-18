package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.BiometricConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 鐢熺墿璇嗗埆閰嶇疆鏁版嵁璁块棶灞?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface BiometricConfigDao extends BaseMapper<BiometricConfigEntity> {

    /**
     * 鏍规嵁鐢熺墿璇嗗埆绫诲瀷鏌ヨ婵€娲婚厤缃?
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE biometric_type = #{biometricType} " +
            "AND config_status = #{configStatus} AND deleted_flag = 0 ORDER BY config_id DESC LIMIT 1")
    BiometricConfigEntity selectByTypeAndStatus(@Param("biometricType") Integer biometricType,
                                              @Param("configStatus") Integer configStatus);

    /**
     * 鏌ヨ鎵€鏈夋縺娲婚厤缃?
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE config_status = 2 AND deleted_flag = 0 ORDER BY biometric_type")
    List<BiometricConfigEntity> selectActiveConfigs();

    /**
     * 鏍规嵁绠楁硶鎻愪緵鍟嗘煡璇㈤厤缃?
     */
    @Select("SELECT * FROM t_access_biometric_config WHERE algorithm_provider = #{provider} " +
            "AND config_status = 2 AND deleted_flag = 0")
    List<BiometricConfigEntity> selectByProvider(@Param("provider") Integer provider);
}