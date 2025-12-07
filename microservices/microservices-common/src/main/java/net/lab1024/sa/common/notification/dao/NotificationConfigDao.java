package net.lab1024.sa.common.notification.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.springframework.transaction.annotation.Transactional;
import net.lab1024.sa.common.notification.domain.entity.NotificationConfigEntity;

/**
 * 通知配置数据访问层
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名（禁止Repository）
 * - 使用@Transactional管理事务
 * - 使用@Select/@Update注解定义SQL
 * </p>
 * <p>
 * 企业级特性：
 * - 支持按配置键查询
 * - 支持按配置类型查询
 * - 支持配置缓存
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface NotificationConfigDao extends BaseMapper<NotificationConfigEntity> {

    /**
     * 根据配置键查询配置
     * <p>
     * 查询指定配置键的配置信息
     * 支持软删除过滤（自动过滤deleted_flag=1的记录）
     * </p>
     *
     * @param configKey 配置键
     * @return 通知配置实体
     */
    @Select("SELECT * FROM t_notification_config WHERE config_key = #{configKey} AND deleted_flag = 0")
    @Transactional(readOnly = true)
    NotificationConfigEntity selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据配置类型查询配置列表
     * <p>
     * 查询指定类型的所有配置
     * 支持软删除过滤
     * </p>
     *
     * @param configType 配置类型
     * @return 通知配置实体列表
     */
    @Select("SELECT * FROM t_notification_config WHERE config_type = #{configType} AND deleted_flag = 0")
    @Transactional(readOnly = true)
    List<NotificationConfigEntity> selectByConfigType(@Param("configType") String configType);

    /**
     * 根据配置类型和状态查询配置列表
     * <p>
     * 查询指定类型和状态的配置
     * 用于获取启用的配置
     * </p>
     *
     * @param configType 配置类型
     * @param status     状态（1-启用 2-禁用）
     * @return 通知配置实体列表
     */
    @Select("SELECT * FROM t_notification_config WHERE config_type = #{configType} AND status = #{status} AND deleted_flag = 0")
    @Transactional(readOnly = true)
    List<NotificationConfigEntity> selectByConfigTypeAndStatus(
            @Param("configType") String configType,
            @Param("status") Integer status);

    /**
     * 更新配置值
     * <p>
     * 更新指定配置的值
     * 支持加密值更新
     * </p>
     *
     * @param configId    配置ID
     * @param configValue 配置值
     * @return 更新记录数
     */
    @Update("UPDATE t_notification_config SET config_value = #{configValue}, update_time = NOW() WHERE config_id = #{configId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateConfigValue(@Param("configId") Long configId, @Param("configValue") String configValue);

    /**
     * 更新配置状态
     * <p>
     * 启用或禁用指定配置
     * </p>
     *
     * @param configId 配置ID
     * @param status   状态（1-启用 2-禁用）
     * @return 更新记录数
     */
    @Update("UPDATE t_notification_config SET status = #{status}, update_time = NOW() WHERE config_id = #{configId} AND deleted_flag = 0")
    @Transactional(rollbackFor = Exception.class)
    int updateConfigStatus(@Param("configId") Long configId, @Param("status") Integer status);
}
