package net.lab1024.sa.oa.workflow.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.oa.workflow.entity.ApprovalTemplateEntity;

/**
 * 审批模板DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ApprovalTemplateDao extends BaseMapper<ApprovalTemplateEntity> {

    /**
     * 根据模板编码查询模板
     */
    ApprovalTemplateEntity selectByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 检查模板编码是否存在
     */
    int existsByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 增加模板使用次数
     */
    @Transactional(rollbackFor = Exception.class)
    int incrementUsageCount(@Param("id") Long id);
}




