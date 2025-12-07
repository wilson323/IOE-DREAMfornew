package net.lab1024.sa.common.audit.service;

import java.util.Map;

import net.lab1024.sa.common.audit.domain.dto.AuditLogQueryDTO;
import net.lab1024.sa.common.audit.entity.AuditLogEntity;
import net.lab1024.sa.common.domain.PageResult;

/**
 * 审计服务接口
 * <p>
 * 提供审计日志记录、查询、统计等功能
 * 严格遵循CLAUDE.md规范:
 * - Service接口定义核心业务方法
 * - 实现类在service.impl包中
 * - 使用@Resource依赖注入
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AuditService {

    /**
     * 记录审计日志
     *
     * @param auditLog 审计日志实体
     * @return 是否记录成功
     */
    boolean recordAuditLog(AuditLogEntity auditLog);

    /**
     * 创建审计日志
     * <p>
     * 用于设备协议推送等场景，需要返回日志ID
     * </p>
     *
     * @param auditLog 审计日志实体
     * @return 创建的日志ID
     */
    Long createAuditLog(AuditLogEntity auditLog);

    /**
     * 查询审计日志（分页）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AuditLogEntity> queryAuditLogs(AuditLogQueryDTO queryDTO);

    /**
     * 获取审计统计信息
     *
     * @param queryDTO 查询条件
     * @return 统计信息
     */
    Map<String, Object> getAuditStatistics(AuditLogQueryDTO queryDTO);

    /**
     * 导出审计日志
     *
     * @param queryDTO 查询条件
     * @return 导出文件路径
     */
    String exportAuditLogs(AuditLogQueryDTO queryDTO);

    /**
     * 归档审计日志
     *
     * @param beforeDate 归档日期之前的数据
     * @return 归档数量
     */
    int archiveAuditLogs(java.time.LocalDateTime beforeDate);
}
