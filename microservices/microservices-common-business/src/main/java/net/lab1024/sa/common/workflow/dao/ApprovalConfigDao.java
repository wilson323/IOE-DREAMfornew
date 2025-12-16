package net.lab1024.sa.common.workflow.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.workflow.entity.ApprovalConfigEntity;

/**
 * 审批配置DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ApprovalConfigDao extends BaseMapper<ApprovalConfigEntity> {

    /**
     * 根据业务类型查询审批配置
     */
    ApprovalConfigEntity selectByBusinessType(@Param("businessType") String businessType);

    /**
     * 根据业务类型和模块查询审批配置
     */
    ApprovalConfigEntity selectByBusinessTypeAndModule(
            @Param("businessType") String businessType,
            @Param("module") String module);

    /**
     * 检查业务类型是否存在
     */
    int existsByBusinessType(@Param("businessType") String businessType);
}
