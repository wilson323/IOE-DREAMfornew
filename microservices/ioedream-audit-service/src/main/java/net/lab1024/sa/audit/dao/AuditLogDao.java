package net.lab1024.sa.audit.dao;

import net.lab1024.sa.audit.domain.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志数据访问对象
 * <p>
 * 提供审计日志的数据库操作
 * 严格遵循repowiki规范:
 * - 使用@Mapper注解标识MyBatis接口
 * - 提供完整的CRUD操作
 * - 支持复杂查询和统计分析
 * - 包含性能优化的查询方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Mapper
@Repository
public interface AuditLogDao {

    /**
     * 插入审计日志
     *
     * @param auditLog 审计日志实体
     * @return 影响行数
     */
    int insert(AuditLogEntity auditLog);

    /**
     * 批量插入审计日志
     *
     * @param auditLogs 审计日志列表
     * @return 影响行数
     */
    int insertBatch(@Param("auditLogs") List<AuditLogEntity> auditLogs);

    /**
     * 根据ID查询审计日志
     *
     * @param auditId 审计日志ID
     * @return 审计日志实体
     */
    AuditLogEntity selectById(@Param("auditId") Long auditId);

    /**
     * 分页查询审计日志
     *
     * @param userId 用户ID（可选）
     * @param moduleName 模块名称（可选）
     * @param operationType 操作类型（可选）
     * @param resultStatus 结果状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param clientIp 客户端IP（可选）
     * @param keyword 关键词（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 审计日志列表
     */
    List<AuditLogEntity> selectByPage(
            @Param("userId") Long userId,
            @Param("moduleName") String moduleName,
            @Param("operationType") Integer operationType,
            @Param("resultStatus") Integer resultStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("clientIp") String clientIp,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计审计日志总数
     *
     * @param userId 用户ID（可选）
     * @param moduleName 模块名称（可选）
     * @param operationType 操作类型（可选）
     * @param resultStatus 结果状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param clientIp 客户端IP（可选）
     * @param keyword 关键词（可选）
     * @return 总数
     */
    long countByCondition(
            @Param("userId") Long userId,
            @Param("moduleName") String moduleName,
            @Param("operationType") Integer operationType,
            @Param("resultStatus") Integer resultStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("clientIp") String clientIp,
            @Param("keyword") String keyword
    );

    /**
     * 根据用户ID查询审计日志
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 审计日志列表
     */
    List<AuditLogEntity> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 根据会话ID查询审计日志
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 审计日志列表
     */
    List<AuditLogEntity> selectBySessionId(@Param("sessionId") String sessionId, @Param("limit") int limit);

    /**
     * 根据请求ID查询审计日志
     *
     * @param requestId 请求ID
     * @return 审计日志实体
     */
    AuditLogEntity selectByRequestId(@Param("requestId") String requestId);

    /**
     * 查询高风险操作日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 高风险操作日志列表
     */
    List<AuditLogEntity> selectHighRiskOperations(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );

    /**
     * 查询失败操作日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 失败操作日志列表
     */
    List<AuditLogEntity> selectFailureOperations(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );

    /**
     * 查询数据修改操作日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 数据修改操作日志列表
     */
    List<AuditLogEntity> selectDataModificationOperations(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );

    /**
     * 统计用户操作次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作次数
     */
    long countUserOperations(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计模块操作次数
     *
     * @param moduleName 模块名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作次数
     */
    long countModuleOperations(
            @Param("moduleName") String moduleName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计各操作类型的次数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作类型统计列表
     */
    List<OperationTypeStatistics> countByOperationType(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计各模块的操作次数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 模块统计列表
     */
    List<ModuleStatistics> countByModule(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询用户活跃度统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 用户活跃度列表
     */
    List<UserActivityStatistics> selectUserActivityStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );

    /**
     * 查询每日操作统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日操作统计列表
     */
    List<DailyOperationStatistics> selectDailyOperationStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询操作失败原因统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 失败原因统计列表
     */
    List<FailureReasonStatistics> selectFailureReasonStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );

    /**
     * 删除过期审计日志
     *
     * @param beforeTime 时间点
     * @return 删除行数
     */
    int deleteByCreateTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 更新审计日志的扩展属性
     *
     * @param auditId 审计日志ID
     * @param extensions 扩展属性
     * @return 影响行数
     */
    int updateExtensions(@Param("auditId") Long auditId, @Param("extensions") String extensions);

    /**
     * 内部统计类 - 操作类型统计
     */
    class OperationTypeStatistics {
        private Integer operationType;
        private String operationTypeText;
        private Long count;

        // getters and setters
        public Integer getOperationType() { return operationType; }
        public void setOperationType(Integer operationType) { this.operationType = operationType; }
        public String getOperationTypeText() { return operationTypeText; }
        public void setOperationTypeText(String operationTypeText) { this.operationTypeText = operationTypeText; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }

    /**
     * 内部统计类 - 模块统计
     */
    class ModuleStatistics {
        private String moduleName;
        private Long count;

        // getters and setters
        public String getModuleName() { return moduleName; }
        public void setModuleName(String moduleName) { this.moduleName = moduleName; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }

    /**
     * 内部统计类 - 用户活跃度统计
     */
    class UserActivityStatistics {
        private Long userId;
        private String username;
        private Long operationCount;
        private Long successCount;
        private Long failureCount;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Long getOperationCount() { return operationCount; }
        public void setOperationCount(Long operationCount) { this.operationCount = operationCount; }
        public Long getSuccessCount() { return successCount; }
        public void setSuccessCount(Long successCount) { this.successCount = successCount; }
        public Long getFailureCount() { return failureCount; }
        public void setFailureCount(Long failureCount) { this.failureCount = failureCount; }
    }

    /**
     * 内部统计类 - 每日操作统计
     */
    class DailyOperationStatistics {
        private String operationDate;
        private Long totalOperations;
        private Long successOperations;
        private Long failureOperations;
        private Double successRate;

        // getters and setters
        public String getOperationDate() { return operationDate; }
        public void setOperationDate(String operationDate) { this.operationDate = operationDate; }
        public Long getTotalOperations() { return totalOperations; }
        public void setTotalOperations(Long totalOperations) { this.totalOperations = totalOperations; }
        public Long getSuccessOperations() { return successOperations; }
        public void setSuccessOperations(Long successOperations) { this.successOperations = successOperations; }
        public Long getFailureOperations() { return failureOperations; }
        public void setFailureOperations(Long failureOperations) { this.failureOperations = failureOperations; }
        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }
    }

    /**
     * 内部统计类 - 失败原因统计
     */
    class FailureReasonStatistics {
        private String errorCode;
        private String errorMessage;
        private Long count;
        private Double percentage;

        // getters and setters
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }
}