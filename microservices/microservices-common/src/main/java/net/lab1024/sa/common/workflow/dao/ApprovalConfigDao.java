package net.lab1024.sa.common.workflow.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.workflow.entity.ApprovalConfigEntity;

/**
 * 审批配置DAO
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
public interface ApprovalConfigDao extends BaseMapper<ApprovalConfigEntity> {

    /**
     * 根据业务类型查询审批配置
     * <p>
     * 查询启用状态且当前时间在生效时间范围内的配置
     * </p>
     *
     * @param businessType 业务类型
     * @return 审批配置实体
     */
    ApprovalConfigEntity selectByBusinessType(@Param("businessType") String businessType);

    /**
     * 根据业务类型和模块查询审批配置
     *
     * @param businessType 业务类型
     * @param module 所属模块
     * @return 审批配置实体
     */
    ApprovalConfigEntity selectByBusinessTypeAndModule(
            @Param("businessType") String businessType,
            @Param("module") String module);

    /**
     * 检查业务类型是否存在
     *
     * @param businessType 业务类型
     * @return 存在返回true，否则返回false
     */
    int existsByBusinessType(@Param("businessType") String businessType);
}

