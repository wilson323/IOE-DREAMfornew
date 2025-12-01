package net.lab1024.sa.system.config.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.system.config.domain.entity.ConfigEntity;

/**
 * 系统配置DAO
 * <p>
 * 提供系统配置的数据访问操作
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Mapper
public interface ConfigDao extends BaseMapper<ConfigEntity> {

    /**
     * 分页查询配置
     *
     * @param queryForm 查询条件
     * @return 配置列表
     */
    List<ConfigEntity> selectConfigPage(@Param("query") Map<String, Object> queryForm);

    /**
     * 根据条件查询配置列表
     *
     * @param queryForm 查询条件
     * @return 配置列表
     */
    List<ConfigEntity> selectConfigList(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<ConfigEntity> selectAllConfigs();

    /**
     * 根据ID查询配置详情
     *
     * @param configId 配置ID
     * @return 配置详情
     */
    ConfigEntity selectConfigDetail(@Param("configId") Long configId);

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 配置
     */
    ConfigEntity selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据配置分组查询配置
     *
     * @param configGroup 配置分组
     * @return 配置列表
     */
    List<ConfigEntity> selectByConfigGroup(@Param("configGroup") String configGroup);

    /**
     * 根据配置分组查询启用的配置
     *
     * @param configGroup 配置分组
     * @return 配置列表
     */
    List<ConfigEntity> selectEnabledByConfigGroup(@Param("configGroup") String configGroup);

    /**
     * 检查配置键是否唯一
     *
     * @param configKey 配置键
     * @param excludeId 排除的配置ID
     * @return 是否唯一（0-不唯一，1-唯一）
     */
    int checkConfigKeyUnique(@Param("configKey") String configKey,
            @Param("excludeId") Long excludeId);

    /**
     * 修改配置状态
     *
     * @param configId 配置ID
     * @param status   状态
     * @param userId   操作人ID
     * @return 影响行数
     */
    int updateConfigStatus(@Param("configId") Long configId,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 批量修改配置状态
     *
     * @param configIds 配置ID列表
     * @param status    状态
     * @param userId    操作人ID
     * @return 影响行数
     */
    int batchUpdateConfigStatus(@Param("configIds") List<Long> configIds,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 重置配置为默认值
     *
     * @param configId 配置ID
     * @param userId   操作人ID
     * @return 影响行数
     */
    int resetConfigToDefault(@Param("configId") Long configId,
            @Param("userId") Long userId);

    /**
     * 批量重置配置为默认值
     *
     * @param configIds 配置ID列表
     * @param userId    操作人ID
     * @return 影响行数
     */
    int batchResetConfigToDefault(@Param("configIds") List<Long> configIds,
            @Param("userId") Long userId);

    /**
     * 统计配置数量
     *
     * @param queryForm 查询条件
     * @return 统计数量
     */
    Long countConfig(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取配置统计信息
     *
     * @return 配置统计信息
     */
    Map<String, Object> getConfigStatistics();

    /**
     * 根据状态查询配置
     *
     * @param status 状态
     * @return 配置列表
     */
    List<ConfigEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据配置类型查询配置
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    List<ConfigEntity> selectByConfigType(@Param("configType") String configType);

    /**
     * 根据配置名称模糊查询配置
     *
     * @param configName 配置名称
     * @return 配置列表
     */
    List<ConfigEntity> selectByConfigName(@Param("configName") String configName);

    /**
     * 查询系统内置配置
     *
     * @return 系统内置配置列表
     */
    List<ConfigEntity> selectSystemConfigs();

    /**
     * 查询自定义配置
     *
     * @return 自定义配置列表
     */
    List<ConfigEntity> selectCustomConfigs();

    /**
     * 查询加密配置
     *
     * @return 加密配置列表
     */
    List<ConfigEntity> selectEncryptConfigs();

    /**
     * 查询只读配置
     *
     * @return 只读配置列表
     */
    List<ConfigEntity> selectReadonlyConfigs();

    /**
     * 获取配置分组列表
     *
     * @return 配置分组列表
     */
    List<Map<String, Object>> selectConfigGroups();

    /**
     * 获取配置类型列表
     *
     * @return 配置类型列表
     */
    List<Map<String, Object>> selectConfigTypes();

    /**
     * 获取指定分组的配置统计
     *
     * @return 分组统计
     */
    List<Map<String, Object>> selectGroupStatistics();

    /**
     * 获取指定类型的配置统计
     *
     * @return 类型统计
     */
    List<Map<String, Object>> selectTypeStatistics();

    /**
     * 批量插入配置
     *
     * @param configList 配置列表
     * @return 影响行数
     */
    int batchInsertConfig(@Param("configList") List<ConfigEntity> configList);

    /**
     * 批量更新配置
     *
     * @param configList 配置列表
     * @return 影响行数
     */
    int batchUpdateConfig(@Param("configList") List<ConfigEntity> configList);

    /**
     * 批量删除配置（逻辑删除）
     *
     * @param configIds 配置ID列表
     * @param userId    操作人ID
     * @return 影响行数
     */
    int batchDeleteConfig(@Param("configIds") List<Long> configIds,
            @Param("userId") Long userId);

    /**
     * 获取最近的配置
     *
     * @param limit 限制数量
     * @return 最近的配置列表
     */
    List<ConfigEntity> selectRecentConfigs(@Param("limit") Integer limit);

    /**
     * 获取配置的最大排序号
     *
     * @param configGroup 配置分组
     * @return 最大排序号
     */
    Integer getMaxSortOrder(@Param("configGroup") String configGroup);

    /**
     * 更新配置排序
     *
     * @param configId  配置ID
     * @param sortOrder 排序号
     * @param userId    操作人ID
     * @return 影响行数
     */
    int updateSortOrder(@Param("configId") Long configId,
            @Param("sortOrder") Integer sortOrder,
            @Param("userId") Long userId);

    /**
     * 批量更新排序
     *
     * @param sortUpdates 排序更新列表
     * @param userId      操作人ID
     * @return 影响行数
     */
    int batchUpdateSortOrder(@Param("sortUpdates") List<Map<String, Object>> sortUpdates,
            @Param("userId") Long userId);

    /**
     * 获取配置缓存数据
     *
     * @return 配置缓存数据
     */
    List<Map<String, Object>> selectConfigCacheData();

    /**
     * 根据配置键获取缓存数据
     *
     * @param configKey 配置键
     * @return 缓存数据
     */
    Map<String, Object> selectCacheDataByKey(@Param("configKey") String configKey);

    /**
     * 根据配置分组获取缓存数据
     *
     * @param configGroup 配置分组
     * @return 缓存数据
     */
    List<Map<String, Object>> selectCacheDataByGroup(@Param("configGroup") String configGroup);

    /**
     * 导出配置数据
     *
     * @param queryForm 查询条件
     * @return 导出数据
     */
    List<Map<String, Object>> exportConfigData(@Param("query") Map<String, Object> queryForm);

    /**
     * 批量修改配置值
     *
     * @param configUpdates 配置更新列表
     * @param userId        操作人ID
     * @return 影响行数
     */
    int batchUpdateConfigValue(@Param("configUpdates") List<Map<String, Object>> configUpdates,
            @Param("userId") Long userId);

    /**
     * 获取配置的有效性检查结果
     *
     * @return 有效性检查结果
     */
    List<Map<String, Object>> selectConfigValidationResults();

    /**
     * 获取配置的使用情况
     *
     * @param configKey 配置键
     * @return 使用情况
     */
    Map<String, Object> getConfigUsage(@Param("configKey") String configKey);

    /**
     * 检查配置是否可删除
     *
     * @param configId 配置ID
     * @return 是否可删除（0-不可删除，1-可删除）
     */
    int checkConfigDeletable(@Param("configId") Long configId);

    /**
     * 获取配置的依赖关系
     *
     * @param configId 配置ID
     * @return 依赖关系
     */
    List<Map<String, Object>> selectConfigDependencies(@Param("configId") Long configId);

    /**
     * 验证配置值格式
     *
     * @param configId    配置ID
     * @param configValue 配置值
     * @return 验证结果（0-无效，1-有效）
     */
    int validateConfigValue(@Param("configId") Long configId,
            @Param("configValue") String configValue);

    /**
     * 获取配置的变更历史
     *
     * @param configId 配置ID
     * @param limit    限制数量
     * @return 变更历史
     */
    List<Map<String, Object>> selectConfigChangeHistory(@Param("configId") Long configId,
            @Param("limit") Integer limit);

    /**
     * 备份配置
     *
     * @param configIds 配置ID列表
     * @param userId    操作人ID
     * @return 影响行数
     */
    int backupConfigs(@Param("configIds") List<Long> configIds,
            @Param("userId") Long userId);

    /**
     * 恢复配置
     *
     * @param backupId 备份ID
     * @param userId   操作人ID
     * @return 影响行数
     */
    int restoreConfigs(@Param("backupId") Long backupId,
            @Param("userId") Long userId);
}
