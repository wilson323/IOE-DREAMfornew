package net.lab1024.sa.oa.workflow.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.oa.workflow.entity.ApprovalConfigEntity;

/**
 * 审批配置DAO
 * <p>
 * 说明：
 * - DAO层仅负责数据访问，不包含业务逻辑
 * - 使用@Mapper + BaseMapper 统一规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Mapper
public interface ApprovalConfigDao extends BaseMapper<ApprovalConfigEntity> {

    /**
     * 根据业务类型查询审批配置
     */
    @Select("""
            SELECT *
            FROM t_oa_approval_config
            WHERE deleted = 0
              AND business_type = #{businessType}
            LIMIT 1
            """)
    ApprovalConfigEntity selectByBusinessType(@Param("businessType") String businessType);

    /**
     * 检查业务类型是否已存在
     */
    @Select("""
            SELECT COUNT(1)
            FROM t_oa_approval_config
            WHERE deleted = 0
              AND business_type = #{businessType}
            """)
    int existsByBusinessType(@Param("businessType") String businessType);
}
