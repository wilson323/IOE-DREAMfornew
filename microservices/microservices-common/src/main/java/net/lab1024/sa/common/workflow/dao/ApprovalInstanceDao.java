package net.lab1024.sa.common.workflow.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * 审批流程实例DAO接口（别名）
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解，禁止使用@Repository注解
 * - 继承WorkflowInstanceDao提供基础CRUD功能
 * - 只负责数据访问，不包含业务逻辑
 * </p>
 * <p>
 * 注意：此接口是WorkflowInstanceDao的别名，用于审批模块
 * 实际实现使用WorkflowInstanceDao
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Mapper
public interface ApprovalInstanceDao extends WorkflowInstanceDao {
    // 所有方法继承自WorkflowInstanceDao
    // 注意：selectById 返回 WorkflowInstanceEntity，需要时进行类型转换
}

