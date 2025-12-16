package net.lab1024.sa.common.i18n.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.i18n.entity.I18nResourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 国际化资源数据访问层
 * <p>
 * 支持国际化资源数据的完整CRUD操作和高级查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Mapper
public interface I18nResourceDao extends BaseMapper<I18nResourceEntity> {

    /**
     * 根据资源键和语言代码查询资源
     *
     * @param resourceKey 资源键
     * @param languageCode 语言代码
     * @return 国际化资源
     */
    @Select("SELECT * FROM t_i18n_resource WHERE resource_key = #{resourceKey} AND language_code = #{languageCode} " +
            "AND status = 1 AND deleted_flag = 0")
    I18nResourceEntity selectByKeyAndLanguage(@Param("resourceKey") String resourceKey, @Param("languageCode") String languageCode);

    /**
     * 根据资源键、语言代码和设备类型查询资源
     *
     * @param resourceKey 资源键
     * @param languageCode 语言代码
     * @param deviceType 设备类型
     * @return 国际化资源
     */
    @Select("SELECT * FROM t_i18n_resource WHERE resource_key = #{resourceKey} AND language_code = #{languageCode} " +
            "AND (device_type = #{deviceType} OR device_type = 'all') " +
            "AND status = 1 AND deleted_flag = 0 " +
            "ORDER BY CASE WHEN device_type = #{deviceType} THEN 1 ELSE 2 END LIMIT 1")
    I18nResourceEntity selectByKeyAndLanguageAndDeviceType(@Param("resourceKey") String resourceKey,
                                                          @Param("languageCode") String languageCode,
                                                          @Param("deviceType") String deviceType);

    /**
     * 根据模块和语言代码查询资源列表
     *
     * @param module 模块
     * @param languageCode 语言代码
     * @return 资源列表
     */
    @Select("SELECT * FROM t_i18n_resource WHERE resource_module = #{module} AND language_code = #{languageCode} " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC, resource_key ASC")
    List<I18nResourceEntity> selectByModuleAndLanguage(@Param("module") String module, @Param("languageCode") String languageCode);

    /**
     * 根据模块、语言代码和设备类型查询资源列表
     *
     * @param module 模块
     * @param languageCode 语言代码
     * @param deviceType 设备类型
     * @return 资源列表
     */
    @Select("SELECT * FROM t_i18n_resource WHERE resource_module = #{module} AND language_code = #{languageCode} " +
            "AND (device_type = #{deviceType} OR device_type = 'all') " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC, resource_key ASC")
    List<I18nResourceEntity> selectByModuleAndLanguageAndDeviceType(@Param("module") String module,
                                                                   @Param("languageCode") String languageCode,
                                                                   @Param("deviceType") String deviceType);

    /**
     * 批量查询资源
     *
     * @param resourceKeys 资源键列表
     * @param languageCode 语言代码
     * @return 资源列表
     */
    @Select("<script>" +
            "SELECT * FROM t_i18n_resource WHERE language_code = #{languageCode} AND resource_key IN " +
            "<foreach collection='resourceKeys' item='key' open='(' separator=',' close=')'>" +
            "#{key}" +
            "</foreach>" +
            "AND status = 1 AND deleted_flag = 0" +
            "</script>")
    List<I18nResourceEntity> selectByKeysAndLanguage(@Param("resourceKeys") List<String> resourceKeys, @Param("languageCode") String languageCode);

    /**
     * 根据语言代码查询所有资源
     *
     * @param languageCode 语言代码
     * @return 资源列表
     */
    @Select("SELECT * FROM t_i18n_resource WHERE language_code = #{languageCode} " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC, resource_key ASC")
    List<I18nResourceEntity> selectByLanguage(@Param("languageCode") String languageCode);

    /**
     * 根据类别查询资源
     *
     * @param category 类别
     * @param languageCode 语言代码
     * @return 资源列表
     */
    @Select("SELECT * FROM t_i18n_resource WHERE resource_category = #{category} AND language_code = #{languageCode} " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC, resource_key ASC")
    List<I18nResourceEntity> selectByCategoryAndLanguage(@Param("category") String category, @Param("languageCode") String languageCode);

    /**
     * 根据分组查询资源
     *
     * @param group 分组
     * @param languageCode 语言代码
     * @return 资源列表
     */
    @Select("SELECT * FROM t_i18n_resource WHERE resource_group = #{group} AND language_code = #{languageCode} " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC, resource_key ASC")
    List<I18nResourceEntity> selectByGroupAndLanguage(@Param("group") String group, @Param("languageCode") String languageCode);

    /**
     * 查询系统默认语言资源
     *
     * @param languageCode 语言代码
     * @return 资源列表
     */
    @Select("SELECT * FROM t_i18n_resource WHERE language_code = #{languageCode} AND is_system = 1 " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC, resource_key ASC")
    List<I18nResourceEntity> selectSystemResourcesByLanguage(@Param("languageCode") String languageCode);

    /**
     * 查询支持的语言列表
     *
     * @return 语言列表
     */
    @Select("SELECT DISTINCT language_code, country_code, COUNT(*) as resource_count " +
            "FROM t_i18n_resource WHERE status = 1 AND deleted_flag = 0 " +
            "GROUP BY language_code, country_code ORDER BY resource_count DESC")
    List<Map<String, Object>> selectSupportedLanguages();

    /**
     * 查询模块列表
     *
     * @param languageCode 语言代码
     * @return 模块列表
     */
    @Select("SELECT DISTINCT resource_module, COUNT(*) as resource_count " +
            "FROM t_i18n_resource WHERE language_code = #{languageCode} AND status = 1 AND deleted_flag = 0 " +
            "GROUP BY resource_module ORDER BY resource_count DESC")
    List<Map<String, Object>> selectModulesByLanguage(@Param("languageCode") String languageCode);

    /**
     * 查询类别列表
     *
     * @param languageCode 语言代码
     * @return 类别列表
     */
    @Select("SELECT DISTINCT resource_category, COUNT(*) as resource_count " +
            "FROM t_i18n_resource WHERE language_code = #{languageCode} AND status = 1 AND deleted_flag = 0 " +
            "GROUP BY resource_category ORDER BY resource_count DESC")
    List<Map<String, Object>> selectCategoriesByLanguage(@Param("languageCode") String languageCode);

    /**
     * 搜索资源
     *
     * @param keyword 关键词
     * @param languageCode 语言代码
     * @param limit 限制数量
     * @return 搜索结果
     */
    @Select("SELECT * FROM t_i18n_resource WHERE language_code = #{languageCode} " +
            "AND (resource_key LIKE CONCAT('%', #{keyword}, '%') OR resource_value LIKE CONCAT('%', #{keyword}, '%') OR resource_desc LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY usage_count DESC, sort_order ASC LIMIT #{limit}")
    List<I18nResourceEntity> searchResources(@Param("keyword") String keyword, @Param("languageCode") String languageCode, @Param("limit") Integer limit);

    /**
     * 更新资源使用统计
     *
     * @param resourceId 资源ID
     * @return 更新行数
     */
    @Update("UPDATE t_i18n_resource SET usage_count = COALESCE(usage_count, 0) + 1, " +
            "last_used_time = NOW(), update_time = NOW() WHERE resource_id = #{resourceId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateUsageStatistics(@Param("resourceId") Long resourceId);

    /**
     * 批量更新资源使用统计
     *
     * @param resourceIds 资源ID列表
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE t_i18n_resource SET usage_count = COALESCE(usage_count, 0) + 1, " +
            "last_used_time = NOW(), update_time = NOW() WHERE resource_id IN " +
            "<foreach collection='resourceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted_flag = 0" +
            "</script>")
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateUsageStatistics(@Param("resourceIds") List<Long> resourceIds);

    /**
     * 查询热门资源
     *
     * @param languageCode 语言代码
     * @param limit 限制数量
     * @return 热门资源列表
     */
    @Select("SELECT * FROM t_i18n_resource WHERE language_code = #{languageCode} " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY usage_count DESC, last_used_time DESC LIMIT #{limit}")
    List<I18nResourceEntity> selectPopularResources(@Param("languageCode") String languageCode, @Param("limit") Integer limit);

    /**
     * 查询待翻译资源
     *
     * @param sourceLanguageCode 源语言代码
     * @param targetLanguageCode 目标语言代码
     * @param limit 限制数量
     * @return 待翻译资源列表
     */
    @Select("SELECT r1.* FROM t_i18n_resource r1 " +
            "LEFT JOIN t_i18n_resource r2 ON r1.resource_key = r2.resource_key AND r2.language_code = #{targetLanguageCode} " +
            "WHERE r1.language_code = #{sourceLanguageCode} AND r2.resource_id IS NULL " +
            "AND r1.status = 1 AND r1.deleted_flag = 0 ORDER BY r1.usage_count DESC LIMIT #{limit}")
    List<I18nResourceEntity> selectPendingTranslations(@Param("sourceLanguageCode") String sourceLanguageCode,
                                                        @Param("targetLanguageCode") String targetLanguageCode,
                                                        @Param("limit") Integer limit);

    /**
     * 查询翻译进度
     *
     * @param languageCode 语言代码
     * @return 翻译进度统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_resources, " +
            "SUM(CASE WHEN translation_status = 'approved' THEN 1 ELSE 0 END) as approved_count, " +
            "SUM(CASE WHEN translation_status = 'reviewed' THEN 1 ELSE 0 END) as reviewed_count, " +
            "SUM(CASE WHEN translation_status = 'translated' THEN 1 ELSE 0 END) as translated_count, " +
            "SUM(CASE WHEN translation_status = 'pending' THEN 1 ELSE 0 END) as pending_count " +
            "FROM t_i18n_resource WHERE language_code = #{languageCode} AND deleted_flag = 0")
    Map<String, Object> selectTranslationProgress(@Param("languageCode") String languageCode);

    /**
     * 删除国际化资源（软删除）
     *
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_i18n_resource SET deleted_flag = 1, update_user_id = #{userId}, update_time = NOW() WHERE resource_id = #{resourceId}")
    @Transactional(rollbackFor = Exception.class)
    int deleteByResourceId(@Param("resourceId") Long resourceId, @Param("userId") Long userId);

    /**
     * 批量删除国际化资源（软删除）
     *
     * @param resourceIds 资源ID列表
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("<script>" +
            "UPDATE t_i18n_resource SET deleted_flag = 1, update_user_id = #{userId}, update_time = NOW() WHERE resource_id IN " +
            "<foreach collection='resourceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    @Transactional(rollbackFor = Exception.class)
    int batchDeleteByResourceIds(@Param("resourceIds") List<Long> resourceIds, @Param("userId") Long userId);

    /**
     * 根据资源键删除所有语言的资源
     *
     * @param resourceKey 资源键
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_i18n_resource SET deleted_flag = 1, update_user_id = #{userId}, update_time = NOW() WHERE resource_key = #{resourceKey}")
    @Transactional(rollbackFor = Exception.class)
    int deleteByResourceKey(@Param("resourceKey") String resourceKey, @Param("userId") Long userId);

    /**
     * 更新翻译状态
     *
     * @param resourceId 资源ID
     * @param status 状态
     * @param reviewerId 审核者ID
     * @return 更新行数
     */
    @Update("UPDATE t_i18n_resource SET translation_status = #{status}, reviewer_id = #{reviewerId}, " +
            "last_review_time = NOW(), update_time = NOW() WHERE resource_id = #{resourceId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateTranslationStatus(@Param("resourceId") Long resourceId, @Param("status") String status, @Param("reviewerId") Long reviewerId);

    /**
     * 查询资源统计信息
     *
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_resources, " +
            "COUNT(DISTINCT language_code) as language_count, " +
            "COUNT(DISTINCT resource_module) as module_count, " +
            "SUM(usage_count) as total_usage, " +
            "AVG(character_count) as avg_character_count " +
            "FROM t_i18n_resource WHERE status = 1 AND deleted_flag = 0")
    Map<String, Object> selectResourceStatistics();

    /**
     * 查询未使用的资源
     *
     * @param days 未使用天数
     * @param limit 限制数量
     * @return 未使用资源列表
     */
    @Select("SELECT * FROM t_i18n_resource WHERE (last_used_time IS NULL OR last_used_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)) " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY COALESCE(last_used_time, create_time) ASC LIMIT #{limit}")
    List<I18nResourceEntity> selectUnusedResources(@Param("days") Integer days, @Param("limit") Integer limit);

    /**
     * 同步资源到其他语言
     *
     * @param sourceLanguageCode 源语言代码
     * @param targetLanguageCode 目标语言代码
     * @param userId 用户ID
     * @return 同步数量
     */
    @Transactional(rollbackFor = Exception.class)
    default int syncResourcesToLanguage(String sourceLanguageCode, String targetLanguageCode, Long userId) {
        List<I18nResourceEntity> sourceResources = selectByLanguage(sourceLanguageCode);
        int syncCount = 0;

        for (I18nResourceEntity source : sourceResources) {
            // 检查目标语言是否已存在
            I18nResourceEntity existing = selectByKeyAndLanguage(source.getResourceKey(), targetLanguageCode);
            if (existing == null) {
                // 创建目标语言资源
                I18nResourceEntity target = new I18nResourceEntity();
                target.setResourceKey(source.getResourceKey());
                target.setLanguageCode(targetLanguageCode);
                target.setCountryCode(source.getCountryCode());
                target.setResourceValue(source.getResourceValue()); // 初始使用源语言值
                target.setResourceDesc(source.getResourceDesc());
                target.setResourceModule(source.getResourceModule());
                target.setResourceCategory(source.getResourceCategory());
                target.setResourceGroup(source.getResourceGroup());
                target.setIsSystem(source.getIsSystem());
                target.setIsDefaultLanguage(0);
                target.setStatus(1);
                target.setSortOrder(source.getSortOrder());
                target.setContextInfo(source.getContextInfo());
                target.setTranslationStatus("pending");
                target.setTranslationSource("sync");
                target.setDeviceType(source.getDeviceType());
                target.setCachePriority(source.getCachePriority());
                target.initializeSystemDefaults();

                insert(target);
                syncCount++;
            }
        }

        return syncCount;
    }
}