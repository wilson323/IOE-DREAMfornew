package net.lab1024.sa.common.preference.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.preference.entity.UserPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 用户偏好设置数据访问层
 * <p>
 * 支持用户偏好设置的完整CRUD操作和高级查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Mapper
public interface UserPreferenceDao extends BaseMapper<UserPreferenceEntity> {

    /**
     * 根据用户ID查询偏好设置
     *
     * @param userId 用户ID
     * @return 偏好设置列表
     */
    @Select("SELECT * FROM t_user_preference WHERE user_id = #{userId} AND status = 1 AND deleted_flag = 0 ORDER BY preference_category, sort_order ASC")
    List<UserPreferenceEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和类别查询偏好设置
     *
     * @param userId 用户ID
     * @param category 类别
     * @return 偏好设置列表
     */
    @Select("SELECT * FROM t_user_preference WHERE user_id = #{userId} AND preference_category = #{category} AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<UserPreferenceEntity> selectByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 根据用户ID、类别和键查询偏好设置
     *
     * @param userId 用户ID
     * @param category 类别
     * @param key 键
     * @return 偏好设置
     */
    @Select("SELECT * FROM t_user_preference WHERE user_id = #{userId} AND preference_category = #{category} AND preference_key = #{key} AND status = 1 AND deleted_flag = 0")
    UserPreferenceEntity selectByUserIdAndCategoryAndKey(@Param("userId") Long userId, @Param("category") String category, @Param("key") String key);

    /**
     * 根据用户ID、类别、键和设备类型查询偏好设置
     *
     * @param userId 用户ID
     * @param category 类别
     * @param key 键
     * @param deviceType 设备类型
     * @return 偏好设置
     */
    @Select("SELECT * FROM t_user_preference WHERE user_id = #{userId} AND preference_category = #{category} AND preference_key = #{key} " +
            "AND (device_type = #{deviceType} OR device_type = 'all') AND status = 1 AND deleted_flag = 0 " +
            "ORDER BY CASE WHEN device_type = #{deviceType} THEN 1 ELSE 2 END LIMIT 1")
    UserPreferenceEntity selectByUserIdAndCategoryAndKeyAndDeviceType(@Param("userId") Long userId, @Param("category") String category, @Param("key") String key, @Param("deviceType") String deviceType);

    /**
     * 根据键查询系统默认偏好设置
     *
     * @param key 键
     * @return 默认偏好设置
     */
    @Select("SELECT * FROM t_user_preference WHERE user_id IS NULL AND preference_key = #{key} AND is_system = 1 AND status = 1 AND deleted_flag = 0")
    UserPreferenceEntity selectSystemDefaultByKey(@Param("key") String key);

    /**
     * 批量查询用户偏好设置
     *
     * @param userId 用户ID
     * @param keys 键列表
     * @return 偏好设置列表
     */
    @Select("<script>" +
            "SELECT * FROM t_user_preference WHERE user_id = #{userId} AND preference_key IN " +
            "<foreach collection='keys' item='key' open='(' separator=',' close=')'>" +
            "#{key}" +
            "</foreach>" +
            " AND status = 1 AND deleted_flag = 0" +
            "</script>")
    List<UserPreferenceEntity> selectByUserIdAndKeys(@Param("userId") Long userId, @Param("keys") List<String> keys);

    /**
     * 查询系统默认偏好设置
     *
     * @return 系统默认偏好设置列表
     */
    @Select("SELECT * FROM t_user_preference WHERE user_id IS NULL AND is_system = 1 AND status = 1 AND deleted_flag = 0 ORDER BY preference_category, sort_order ASC")
    List<UserPreferenceEntity> selectSystemDefaults();

    /**
     * 根据分组查询偏好设置
     *
     * @param userId 用户ID
     * @param group 分组
     * @return 偏好设置列表
     */
    @Select("SELECT * FROM t_user_preference WHERE user_id = #{userId} AND preference_group = #{group} AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<UserPreferenceEntity> selectByUserIdAndGroup(@Param("userId") Long userId, @Param("group") String group);

    /**
     * 更新偏好值和统计信息
     *
     * @param preferenceId 偏好ID
     * @param value 新值
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_user_preference SET preference_value = #{value}, last_update_time = NOW(), " +
            "update_count = COALESCE(update_count, 0) + 1, update_user_id = #{userId}, update_time = NOW() " +
            "WHERE preference_id = #{preferenceId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updatePreferenceValue(@Param("preferenceId") Long preferenceId, @Param("value") String value, @Param("userId") Long userId);

    /**
     * 批量更新偏好值
     *
     * @param preferences 偏好值映射
     * @param userId 用户ID
     * @return 更新行数
     */
    @Transactional(rollbackFor = Exception.class)
    default int batchUpdatePreferences(Map<String, String> preferences, Long userId) {
        int updateCount = 0;
        for (Map.Entry<String, String> entry : preferences.entrySet()) {
            UserPreferenceEntity existing = selectByUserIdAndCategoryAndKey(userId, "interface", entry.getKey());
            if (existing != null) {
                updateCount += updatePreferenceValue(existing.getId(), entry.getValue(), userId);
            }
        }
        return updateCount;
    }

    /**
     * 删除用户偏好设置（软删除）
     *
     * @param preferenceId 偏好ID
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_user_preference SET deleted_flag = 1, update_user_id = #{userId}, update_time = NOW() WHERE preference_id = #{preferenceId}")
    @Transactional(rollbackFor = Exception.class)
    int deleteByPreferenceId(@Param("preferenceId") Long preferenceId, @Param("userId") Long userId);

    /**
     * 删除用户指定类别的偏好设置
     *
     * @param userId 用户ID
     * @param category 类别
     * @param operatorId 操作用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_user_preference SET deleted_flag = 1, update_user_id = #{operatorId}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND preference_category = #{category} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int deleteByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category, @Param("operatorId") Long operatorId);

    /**
     * 重置用户偏好设置为默认值
     *
     * @param userId 用户ID
     * @param category 类别（可选）
     * @param operatorId 操作用户ID
     * @return 重置行数
     */
    @Update("<script>" +
            "UPDATE t_user_preference SET preference_value = default_value, last_update_time = NOW(), " +
            "update_count = COALESCE(update_count, 0) + 1, update_user_id = #{operatorId}, update_time = NOW() " +
            "WHERE user_id = #{userId}" +
            "<if test='category != null'>" +
            " AND preference_category = #{category}" +
            "</if>" +
            " AND default_value IS NOT NULL AND deleted_flag = 0" +
            "</script>")
    @Transactional(rollbackFor = Exception.class)
    int resetToDefaults(@Param("userId") Long userId, @Param("category") String category, @Param("operatorId") Long operatorId);

    /**
     * 统计用户偏好设置数量
     *
     * @param userId 用户ID
     * @param category 类别（可选）
     * @return 统计数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_user_preference WHERE user_id = #{userId} AND status = 1 AND deleted_flag = 0" +
            "<if test='category != null'>" +
            " AND preference_category = #{category}" +
            "</if>" +
            "</script>")
    int countUserPreferences(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 查询用户最常用的偏好设置
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 常用偏好设置列表
     */
    @Select("SELECT * FROM t_user_preference WHERE user_id = #{userId} AND status = 1 AND deleted_flag = 0 " +
            "ORDER BY update_count DESC, last_update_time DESC LIMIT #{limit}")
    List<UserPreferenceEntity> selectMostUsedPreferences(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询热门偏好设置
     *
     * @param limit 限制数量
     * @return 热门偏好设置列表
     */
    @Select("SELECT preference_key, preference_value, COUNT(*) as user_count " +
            "FROM t_user_preference WHERE status = 1 AND deleted_flag = 0 " +
            "GROUP BY preference_key, preference_value " +
            "ORDER BY user_count DESC LIMIT #{limit}")
    List<UserPreferenceEntity> selectPopularPreferences(@Param("limit") Integer limit);

    /**
     * 查询用户偏好设置分组
     *
     * @param userId 用户ID
     * @return 分组列表
     */
    @Select("SELECT DISTINCT preference_group FROM t_user_preference " +
            "WHERE user_id = #{userId} AND preference_group IS NOT NULL AND preference_group != '' " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY preference_group")
    List<String> selectUserPreferenceGroups(@Param("userId") Long userId);

    /**
     * 查询偏好设置类别
     *
     * @return 类别列表
     */
    @Select("SELECT DISTINCT preference_category FROM t_user_preference " +
            "WHERE status = 1 AND deleted_flag = 0 ORDER BY preference_category")
    List<String> selectPreferenceCategories();

    /**
     * 搜索用户偏好设置
     *
     * @param userId 用户ID
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 搜索结果
     */
    @Select("SELECT * FROM t_user_preference WHERE user_id = #{userId} " +
            "AND (preference_key LIKE CONCAT('%', #{keyword}, '%') OR preference_desc LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC LIMIT #{limit}")
    List<UserPreferenceEntity> searchPreferences(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 同步系统默认偏好设置到用户
     *
     * @param userId 用户ID
     * @param operatorId 操作用户ID
     * @return 同步数量
     */
    @Transactional(rollbackFor = Exception.class)
    default int syncSystemDefaultsToUser(Long userId, Long operatorId) {
        List<UserPreferenceEntity> systemDefaults = selectSystemDefaults();
        int syncCount = 0;

        for (UserPreferenceEntity systemDefault : systemDefaults) {
            UserPreferenceEntity existing = selectByUserIdAndCategoryAndKeyAndDeviceType(
                userId, systemDefault.getPreferenceCategory(),
                systemDefault.getPreferenceKey(), systemDefault.getDeviceType());

            if (existing == null) {
                // 创建用户偏好设置
                UserPreferenceEntity userPreference = new UserPreferenceEntity();
                userPreference.setUserId(userId);
                userPreference.setPreferenceCategory(systemDefault.getPreferenceCategory());
                userPreference.setPreferenceKey(systemDefault.getPreferenceKey());
                userPreference.setPreferenceValue(systemDefault.getDefaultValue());
                userPreference.setPreferenceType(systemDefault.getPreferenceType());
                userPreference.setDefaultValue(systemDefault.getDefaultValue());
                userPreference.setPreferenceDesc(systemDefault.getPreferenceDesc());
                userPreference.setIsSystem(0); // 用户偏好
                userPreference.setIsVisible(systemDefault.getIsVisible());
                userPreference.setIsEditable(systemDefault.getIsEditable());
                userPreference.setSortOrder(systemDefault.getSortOrder());
                userPreference.setValidationRule(systemDefault.getValidationRule());
                userPreference.setOptions(systemDefault.getOptions());
                userPreference.setPreferenceGroup(systemDefault.getPreferenceGroup());
                userPreference.setDeviceType(systemDefault.getDeviceType());
                userPreference.setStatus(1);

                insert(userPreference);
                syncCount++;
            }
        }

        return syncCount;
    }
}
