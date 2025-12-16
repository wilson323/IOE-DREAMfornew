package net.lab1024.sa.common.theme.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.theme.entity.UserThemeConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户主题配置数据访问层
 * <p>
 * 支持用户主题配置的完整CRUD操作和高级查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Mapper
public interface UserThemeConfigDao extends BaseMapper<UserThemeConfigEntity> {

    /**
     * 根据用户ID查询主题配置
     *
     * @param userId 用户ID
     * @return 主题配置列表
     */
    @Select("SELECT * FROM t_user_theme_config WHERE user_id = #{userId} AND deleted_flag = 0 ORDER BY is_default DESC, sort_order ASC")
    List<UserThemeConfigEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和设备类型查询主题配置
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return 主题配置列表
     */
    @Select("SELECT * FROM t_user_theme_config WHERE user_id = #{userId} AND device_type = #{deviceType} AND deleted_flag = 0 ORDER BY is_default DESC, sort_order ASC")
    List<UserThemeConfigEntity> selectByUserIdAndDeviceType(@Param("userId") Long userId, @Param("deviceType") String deviceType);

    /**
     * 根据用户ID查询默认主题配置
     *
     * @param userId 用户ID
     * @return 默认主题配置
     */
    @Select("SELECT * FROM t_user_theme_config WHERE user_id = #{userId} AND is_default = 1 AND status = 1 AND deleted_flag = 0 LIMIT 1")
    UserThemeConfigEntity selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和设备类型查询默认主题配置
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return 默认主题配置
     */
    @Select("SELECT * FROM t_user_theme_config WHERE user_id = #{userId} AND device_type = #{deviceType} AND is_default = 1 AND status = 1 AND deleted_flag = 0 LIMIT 1")
    UserThemeConfigEntity selectDefaultByUserIdAndDeviceType(@Param("userId") Long userId, @Param("deviceType") String deviceType);

    /**
     * 根据配置ID查询主题配置
     *
     * @param configId 配置ID
     * @return 主题配置
     */
    @Select("SELECT * FROM t_user_theme_config WHERE config_id = #{configId} AND deleted_flag = 0")
    UserThemeConfigEntity selectByConfigId(@Param("configId") Long configId);

    /**
     * 更新用户默认主题配置
     *
     * @param userId 用户ID
     * @param configId 新的默认配置ID
     * @param deviceType 设备类型
     * @return 更新行数
     */
    @Update("UPDATE t_user_theme_config SET is_default = CASE WHEN config_id = #{configId} THEN 1 ELSE 0 END, " +
            "update_time = NOW(), update_user_id = #{userId} " +
            "WHERE user_id = #{userId} AND device_type = #{deviceType} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateDefaultTheme(@Param("userId") Long userId, @Param("configId") Long configId, @Param("deviceType") String deviceType);

    /**
     * 更新最后使用时间
     *
     * @param configId 配置ID
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_user_theme_config SET last_used_time = NOW(), update_user_id = #{userId} WHERE config_id = #{configId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateLastUsedTime(@Param("configId") Long configId, @Param("userId") Long userId);

    /**
     * 批量更新主题配置状态
     *
     * @param configIds 配置ID列表
     * @param status 状态
     * @param userId 操作用户ID
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE t_user_theme_config SET status = #{status}, update_time = NOW(), update_user_id = #{userId} " +
            "WHERE config_id IN " +
            "<foreach collection='configIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted_flag = 0" +
            "</script>")
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateStatus(@Param("configIds") List<Long> configIds, @Param("status") Integer status, @Param("userId") Long userId);

    /**
     * 删除用户主题配置（软删除）
     *
     * @param configId 配置ID
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_user_theme_config SET deleted_flag = 1, update_time = NOW(), update_user_id = #{userId} WHERE config_id = #{configId}")
    @Transactional(rollbackFor = Exception.class)
    int deleteByConfigId(@Param("configId") Long configId, @Param("userId") Long userId);

    /**
     * 删除用户所有主题配置（软删除）
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @param operatorId 操作用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_user_theme_config SET deleted_flag = 1, update_time = NOW(), update_user_id = #{operatorId} " +
            "WHERE user_id = #{userId} AND device_type = #{deviceType} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int deleteByUserIdAndDeviceType(@Param("userId") Long userId, @Param("deviceType") String deviceType, @Param("operatorId") Long operatorId);

    /**
     * 统计用户主题配置数量
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return 配置数量
     */
    @Select("SELECT COUNT(*) FROM t_user_theme_config WHERE user_id = #{userId} AND device_type = #{deviceType} AND deleted_flag = 0")
    int countByUserIdAndDeviceType(@Param("userId") Long userId, @Param("deviceType") String deviceType);

    /**
     * 查询热门主题配置
     *
     * @param limit 限制数量
     * @return 热门主题配置列表
     */
    @Select("SELECT theme_name, theme_color, layout_mode, COUNT(*) as usage_count " +
            "FROM t_user_theme_config WHERE status = 1 AND deleted_flag = 0 " +
            "GROUP BY theme_name, theme_color, layout_mode " +
            "ORDER BY usage_count DESC, MAX(create_time) DESC " +
            "LIMIT #{limit}")
    List<UserThemeConfigEntity> selectPopularThemes(@Param("limit") Integer limit);

    /**
     * 查询系统默认主题配置
     * 用于新用户的初始主题配置
     *
     * @return 系统默认主题配置
     */
    @Select("SELECT * FROM t_user_theme_config WHERE user_id IS NULL AND is_system = 1 AND status = 1 AND deleted_flag = 0 LIMIT 1")
    UserThemeConfigEntity selectSystemDefaultTheme();

    /**
     * 根据主题特征搜索相似配置
     *
     * @param colorIndex 颜色索引
     * @param layoutMode 布局模式
     * @param limit 限制数量
     * @return 相似配置列表
     */
    @Select("SELECT * FROM t_user_theme_config " +
            "WHERE color_index = #{colorIndex} AND layout_mode = #{layoutMode} " +
            "AND status = 1 AND deleted_flag = 0 " +
            "ORDER BY last_used_time DESC " +
            "LIMIT #{limit}")
    List<UserThemeConfigEntity> selectSimilarThemes(@Param("colorIndex") Integer colorIndex,
                                                   @Param("layoutMode") String layoutMode,
                                                   @Param("limit") Integer limit);
}