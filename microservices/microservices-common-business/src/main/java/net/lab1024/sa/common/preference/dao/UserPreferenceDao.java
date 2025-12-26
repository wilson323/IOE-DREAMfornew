package net.lab1024.sa.common.preference.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.preference.entity.UserPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户偏好设置数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Mapper
public interface UserPreferenceDao extends BaseMapper<UserPreferenceEntity> {

    /**
     * 根据用户ID查询偏好设置
     *
     * @param userId 用户ID
     * @return 偏好设置列表
     */
    List<UserPreferenceEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和类别查询偏好设置
     *
     * @param userId 用户ID
     * @param category 类别
     * @return 偏好设置列表
     */
    List<UserPreferenceEntity> selectByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 根据用户ID和键查询偏好设置
     *
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     * @return 偏好设置实体
     */
    UserPreferenceEntity selectByUserIdAndKey(@Param("userId") Long userId, @Param("preferenceKey") String preferenceKey);
}
