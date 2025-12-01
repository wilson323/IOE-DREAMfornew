package net.lab1024.sa.admin.module.consume.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeAuditLogEntity;

/**
 * 消费审计日志 DAO接口
 * 提供审计日志的数据库操作
 * 严格遵循repowiki规范：DAO层专注于数据访问
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */
@Mapper
@Component
public interface ConsumeAuditLogDao extends BaseMapper<ConsumeAuditLogEntity> {

    /**
     * 查询审计日志
     *
     * @param personId 人员ID（可选）
     * @param operationType 操作类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量（可选）
     * @return 审计日志列表
     */
    List<ConsumeAuditLogEntity> queryAuditLogs(
            @Param("personId") Long personId,
            @Param("operationType") String operationType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit
    );

    /**
     * 查询高风险操作
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 高风险操作列表
     */
    List<ConsumeAuditLogEntity> selectHighRiskOperations(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 根据订单号查询审计日志
     *
     * @param orderNo 订单号
     * @return 审计日志列表
     */
    List<ConsumeAuditLogEntity> selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 统计操作类型分布
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作类型统计结果
     */
    List<java.util.Map<String, Object>> countByOperationType(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计风险等级分布
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风险等级统计结果
     */
    List<java.util.Map<String, Object>> countByRiskLevel(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除过期的审计日志
     *
     * @param expireTime 过期时间点
     * @return 删除的记录数
     */
    int deleteExpiredLogs(@Param("expireTime") LocalDateTime expireTime);
}