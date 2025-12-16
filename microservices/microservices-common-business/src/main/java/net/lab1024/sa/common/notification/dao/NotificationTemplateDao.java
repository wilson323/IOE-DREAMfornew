package net.lab1024.sa.common.notification.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.springframework.transaction.annotation.Transactional;
import net.lab1024.sa.common.notification.domain.entity.NotificationTemplateEntity;

/**
 * 通知模板数据访问层
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名（禁止Repository）
 * - 使用@Transactional管理事务
 * - 使用@Select注解定义SQL
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface NotificationTemplateDao extends BaseMapper<NotificationTemplateEntity> {

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @return 通知模板实体
     */
    @Select("SELECT * FROM t_notification_template WHERE template_code = #{templateCode} AND deleted_flag = 0")
    @Transactional(readOnly = true)
    NotificationTemplateEntity selectByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 根据模板类型查询模板列表
     *
     * @param templateType 模板类型
     * @return 通知模板实体列表
     */
    @Select("SELECT * FROM t_notification_template WHERE template_type = #{templateType} AND deleted_flag = 0")
    @Transactional(readOnly = true)
    List<NotificationTemplateEntity> selectByTemplateType(@Param("templateType") Integer templateType);

    /**
     * 根据模板类型和状态查询模板列表
     *
     * @param templateType 模板类型
     * @param status       状态（1-启用 2-禁用）
     * @return 通知模板实体列表
     */
    @Select("SELECT * FROM t_notification_template WHERE template_type = #{templateType} AND status = #{status} AND deleted_flag = 0")
    @Transactional(readOnly = true)
    List<NotificationTemplateEntity> selectByTemplateTypeAndStatus(
            @Param("templateType") Integer templateType,
            @Param("status") Integer status);
}
