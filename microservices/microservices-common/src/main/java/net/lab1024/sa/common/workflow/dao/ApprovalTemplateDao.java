package net.lab1024.sa.common.workflow.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.workflow.entity.ApprovalTemplateEntity;

/**
 * 审批模板DAO
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ApprovalTemplateDao extends BaseMapper<ApprovalTemplateEntity> {

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @return 模板实体
     */
    ApprovalTemplateEntity selectByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 检查模板编码是否存在
     *
     * @param templateCode 模板编码
     * @return 存在返回true，否则返回false
     */
    int existsByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 增加模板使用次数
     *
     * @param id 模板ID
     * @return 更新数量
     */
    int incrementUsageCount(@Param("id") Long id);
}

