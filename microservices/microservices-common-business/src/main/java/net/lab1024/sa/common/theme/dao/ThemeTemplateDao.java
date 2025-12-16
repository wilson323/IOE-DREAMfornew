package net.lab1024.sa.common.theme.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.theme.entity.ThemeTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 主题模板数据访问层
 * <p>
 * 严格遵循本项目技术栈：
 * - 使用MyBatis-Plus作为ORM框架
 * - 遵循四层架构规范：DAO层只负责数据访问
 * - 使用@Mapper注解，禁用@Mapper
 * - 统一使用BaseMapper<Entity>模式
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Mapper
public interface ThemeTemplateDao extends BaseMapper<ThemeTemplateEntity> {

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @return 主题模板
     */
    @Select("SELECT * FROM t_theme_template WHERE template_code = #{templateCode} AND deleted_flag = 0")
    ThemeTemplateEntity selectByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 根据模板类型查询模板列表
     *
     * @param templateType 模板类型
     * @return 主题模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE template_type = #{templateType} AND is_enabled = 1 AND deleted_flag = 0 ORDER BY is_recommended DESC, sort_order ASC, rating DESC")
    List<ThemeTemplateEntity> selectByTemplateType(@Param("templateType") String templateType);

    /**
     * 根据模板类别查询模板列表
     *
     * @param templateCategory 模板类别
     * @return 主题模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE template_category = #{templateCategory} AND is_enabled = 1 AND deleted_flag = 0 ORDER BY is_recommended DESC, sort_order ASC, rating DESC")
    List<ThemeTemplateEntity> selectByTemplateCategory(@Param("templateCategory") String templateCategory);

    /**
     * 查询推荐模板列表
     *
     * @param limit 限制数量
     * @return 推荐模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE is_recommended = 1 AND is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY sort_order ASC, rating DESC LIMIT #{limit}")
    List<ThemeTemplateEntity> selectRecommendedTemplates(@Param("limit") Integer limit);

    /**
     * 查询官方模板列表
     *
     * @param limit 限制数量
     * @return 官方模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE is_official = 1 AND is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY sort_order ASC, rating DESC LIMIT #{limit}")
    List<ThemeTemplateEntity> selectOfficialTemplates(@Param("limit") Integer limit);

    /**
     * 查询热门模板列表
     *
     * @param limit 限制数量
     * @return 热门模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY usage_count DESC, download_count DESC, rating DESC LIMIT #{limit}")
    List<ThemeTemplateEntity> selectPopularTemplates(@Param("limit") Integer limit);

    /**
     * 查询最新模板列表
     *
     * @param limit 限制数量
     * @return 最新模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY create_time DESC, publish_time DESC LIMIT #{limit}")
    List<ThemeTemplateEntity> selectLatestTemplates(@Param("limit") Integer limit);

    /**
     * 搜索模板
     *
     * @param keyword 关键词
     * @param templateType 模板类型（可选）
     * @param limit 限制数量
     * @return 搜索结果
     */
    @Select("<script>" +
            "SELECT * FROM t_theme_template WHERE is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 " +
            "AND (template_name LIKE CONCAT('%', #{keyword}, '%') OR template_desc LIKE CONCAT('%', #{keyword}, '%') OR template_tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "<if test='templateType != null'>" +
            "AND template_type = #{templateType} " +
            "</if>" +
            "ORDER BY is_recommended DESC, rating DESC, usage_count DESC LIMIT #{limit}" +
            "</script>")
    List<ThemeTemplateEntity> searchTemplates(@Param("keyword") String keyword, @Param("templateType") String templateType, @Param("limit") Integer limit);

    /**
     * 根据作者查询模板
     *
     * @param authorId 作者ID
     * @return 作者模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE author_id = #{authorId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<ThemeTemplateEntity> selectByAuthorId(@Param("authorId") Long authorId);

    /**
     * 查询支持暗黑模式的模板
     *
     * @return 支持暗黑模式的模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE supports_dark_mode = 1 AND is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY is_recommended DESC, sort_order ASC")
    List<ThemeTemplateEntity> selectDarkModeTemplates();

    /**
     * 查询免费模板
     *
     * @param limit 限制数量
     * @return 免费模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE (price IS NULL OR price = 0) AND is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY is_recommended DESC, rating DESC LIMIT #{limit}")
    List<ThemeTemplateEntity> selectFreeTemplates(@Param("limit") Integer limit);

    /**
     * 查询付费模板
     *
     * @param limit 限制数量
     * @return 付费模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE price > 0 AND is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY price ASC, rating DESC LIMIT #{limit}")
    List<ThemeTemplateEntity> selectPaidTemplates(@Param("limit") Integer limit);

    /**
     * 根据复杂度查询模板
     *
     * @param complexityLevel 复杂度等级
     * @return 指定复杂度的模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE complexity_level = #{complexityLevel} AND is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY sort_order ASC, rating DESC")
    List<ThemeTemplateEntity> selectByComplexityLevel(@Param("complexityLevel") Integer complexityLevel);

    /**
     * 根据评分查询高质量模板
     *
     * @param minRating 最低评分
     * @param limit 限制数量
     * @return 高质量模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE rating >= #{minRating} AND is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY rating DESC, usage_count DESC LIMIT #{limit}")
    List<ThemeTemplateEntity> selectHighRatedTemplates(@Param("minRating") Double minRating, @Param("limit") Integer limit);

    /**
     * 更新模板使用统计
     *
     * @param templateId 模板ID
     * @return 更新行数
     */
    @Update("UPDATE t_theme_template SET usage_count = COALESCE(usage_count, 0) + 1, last_used_time = NOW(), update_time = NOW() WHERE template_id = #{templateId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateUsageStatistics(@Param("templateId") Long templateId);

    /**
     * 更新模板下载统计
     *
     * @param templateId 模板ID
     * @return 更新行数
     */
    @Update("UPDATE t_theme_template SET download_count = COALESCE(download_count, 0) + 1, update_time = NOW() WHERE template_id = #{templateId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateDownloadStatistics(@Param("templateId") Long templateId);

    /**
     * 更新模板评分
     *
     * @param templateId 模板ID
     * @param newRating 新评分
     * @return 更新行数
     */
    @Update("UPDATE t_theme_template SET rating = (CASE WHEN rating_count IS NULL OR rating_count = 0 THEN #{newRating} ELSE (rating * rating_count + #{newRating}) / (rating_count + 1) END), rating_count = COALESCE(rating_count, 0) + 1, update_time = NOW() WHERE template_id = #{templateId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateRating(@Param("templateId") Long templateId, @Param("newRating") Integer newRating);

    /**
     * 删除主题模板（软删除）
     *
     * @param templateId 模板ID
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_theme_template SET deleted_flag = 1, update_user_id = #{userId}, update_time = NOW() WHERE template_id = #{templateId}")
    @Transactional(rollbackFor = Exception.class)
    int deleteByTemplateId(@Param("templateId") Long templateId, @Param("userId") Long userId);

    /**
     * 批量删除主题模板（软删除）
     *
     * @param templateIds 模板ID列表
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("<script>" +
            "UPDATE t_theme_template SET deleted_flag = 1, update_user_id = #{userId}, update_time = NOW() WHERE template_id IN " +
            "<foreach collection='templateIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    @Transactional(rollbackFor = Exception.class)
    int batchDeleteByTemplateIds(@Param("templateIds") List<Long> templateIds, @Param("userId") Long userId);

    /**
     * 发布模板
     *
     * @param templateId 模板ID
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_theme_template SET template_status = 'published', publish_time = NOW(), update_user_id = #{userId}, update_time = NOW() WHERE template_id = #{templateId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int publishTemplate(@Param("templateId") Long templateId, @Param("userId") Long userId);

    /**
     * 设置为推荐模板
     *
     * @param templateId 模板ID
     * @param recommended 是否推荐
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_theme_template SET is_recommended = #{recommended}, update_user_id = #{userId}, update_time = NOW() WHERE template_id = #{templateId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateRecommendedStatus(@Param("templateId") Long templateId, @Param("recommended") Integer recommended, @Param("userId") Long userId);

    /**
     * 获取模板统计信息
     *
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_templates, " +
            "COUNT(CASE WHEN is_official = 1 THEN 1 END) as official_templates, " +
            "COUNT(CASE WHEN is_recommended = 1 THEN 1 END) as recommended_templates, " +
            "COUNT(CASE WHEN price IS NULL OR price = 0 THEN 1 END) as free_templates, " +
            "COUNT(CASE WHEN price > 0 THEN 1 END) as paid_templates, " +
            "COUNT(CASE WHEN supports_dark_mode = 1 THEN 1 END) as dark_mode_templates, " +
            "SUM(usage_count) as total_usage, " +
            "SUM(download_count) as total_downloads, " +
            "AVG(rating) as avg_rating, " +
            "AVG(performance_score) as avg_performance_score " +
            "FROM t_theme_template WHERE is_enabled = 1 AND deleted_flag = 0")
    Map<String, Object> selectTemplateStatistics();

    /**
     * 获取模板类型统计
     *
     * @return 模板类型统计
     */
    @Select("SELECT template_type, COUNT(*) as count, AVG(rating) as avg_rating, SUM(usage_count) as total_usage FROM t_theme_template WHERE is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 GROUP BY template_type ORDER BY count DESC")
    List<Map<String, Object>> selectTemplateTypeStatistics();

    /**
     * 获取模板类别统计
     *
     * @return 模板类别统计
     */
    @Select("SELECT template_category, COUNT(*) as count, AVG(rating) as avg_rating, SUM(usage_count) as total_usage FROM t_theme_template WHERE is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 GROUP BY template_category ORDER BY count DESC")
    List<Map<String, Object>> selectTemplateCategoryStatistics();

    /**
     * 查询未使用的模板
     *
     * @param days 未使用天数
     * @param limit 限制数量
     * @return 未使用模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE (last_used_time IS NULL OR last_used_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)) AND is_enabled = 1 AND deleted_flag = 0 ORDER BY COALESCE(last_used_time, create_time) ASC LIMIT #{limit}")
    List<ThemeTemplateEntity> selectUnusedTemplates(@Param("days") Integer days, @Param("limit") Integer limit);

    /**
     * 查询评分最高的模板
     *
     * @param limit 限制数量
     * @return 高评分模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE rating_count >= 5 AND is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY rating DESC, rating_count DESC LIMIT #{limit}")
    List<ThemeTemplateEntity> selectTopRatedTemplates(@Param("limit") Integer limit);

    /**
     * 根据标签查询模板
     *
     * @param tag 标签
     * @return 包含该标签的模板列表
     */
    @Select("SELECT * FROM t_theme_template WHERE template_tags LIKE CONCAT('%', #{tag}, '%') AND is_enabled = 1 AND template_status = 'published' AND deleted_flag = 0 ORDER BY is_recommended DESC, sort_order ASC")
    List<ThemeTemplateEntity> selectByTag(@Param("tag") String tag);

    /**
     * 查询模板依赖关系
     *
     * @param templateId 模板ID
     * @return 依赖的模板ID列表
     */
    @Select("SELECT dependencies FROM t_theme_template WHERE template_id = #{templateId} AND deleted_flag = 0")
    String selectDependencies(@Param("templateId") Long templateId);
}