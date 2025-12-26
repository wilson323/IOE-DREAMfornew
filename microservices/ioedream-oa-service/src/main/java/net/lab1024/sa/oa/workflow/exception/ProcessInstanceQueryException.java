package net.lab1024.sa.oa.workflow.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 流程实例查询异常
 * <p>
 * 专门用于流程实例查询过程中的异常处理
 * 提供详细的查询参数和错误上下文信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessInstanceQueryException extends RuntimeException {

    /**
     * 查询参数
     */
    private Map<String, Object> queryParams;

    /**
     * 查询类型
     */
    private String queryType;

    /**
     * 查询条件
     */
    private String queryCondition;

    /**
     * 查询时间范围
     */
    private String timeRange;

    /**
     * 查询用户ID
     */
    private Long queryUserId;

    /**
     * 查询部门ID
     */
    private Long queryDepartmentId;

    // ==================== 查询类型常量 ====================

    public static final String QUERY_TYPE_BY_ID = "BY_ID"; // 按ID查询
    public static final String QUERY_TYPE_BY_BUSINESS_KEY = "BY_BUSINESS_KEY"; // 按业务Key查询
    public static final String QUERY_TYPE_BY_PROCESS_KEY = "BY_PROCESS_KEY"; // 按流程Key查询
    public static final String QUERY_TYPE_BY_INITIATOR = "BY_INITIATOR"; // 按发起人查询
    public static final String QUERY_TYPE_BY_STATUS = "BY_STATUS"; // 按状态查询
    public static final String QUERY_TYPE_BY_TIME_RANGE = "BY_TIME_RANGE"; // 按时间范围查询
    public static final String QUERY_TYPE_LIST = "LIST"; // 列表查询
    public static final String QUERY_TYPE_COUNT = "COUNT"; // 统计查询
    public static final String QUERY_TYPE_HISTORY = "HISTORY"; // 历史查询

    // ==================== 构造方法 ====================

    public ProcessInstanceQueryException(String queryType, String message, Throwable cause) {
        super(message, cause);
        this.queryType = queryType;
    }

    public ProcessInstanceQueryException(String queryType, Map<String, Object> queryParams, String message, Throwable cause) {
        super(message, cause);
        this.queryType = queryType;
        this.queryParams = queryParams;
    }

    public ProcessInstanceQueryException(String queryType, String queryCondition, Long queryUserId, String message, Throwable cause) {
        super(message, cause);
        this.queryType = queryType;
        this.queryCondition = queryCondition;
        this.queryUserId = queryUserId;
    }

    public ProcessInstanceQueryException(String queryType, Map<String, Object> queryParams, String queryCondition, String timeRange, Long queryUserId, Long queryDepartmentId, String message, Throwable cause) {
        super(message, cause);
        this.queryType = queryType;
        this.queryParams = queryParams;
        this.queryCondition = queryCondition;
        this.timeRange = timeRange;
        this.queryUserId = queryUserId;
        this.queryDepartmentId = queryDepartmentId;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 查询参数无效
     */
    public static ProcessInstanceQueryException invalidParameter(String paramName, String value, String reason) {
        return new ProcessInstanceQueryException(QUERY_TYPE_BY_ID, null,
                "查询参数无效: " + paramName + " = " + value + ", 原因: " + reason, null);
    }

    /**
     * 流程实例ID无效
     */
    public static ProcessInstanceQueryException invalidInstanceId(String instanceId) {
        return new ProcessInstanceQueryException(QUERY_TYPE_BY_ID, null,
                "流程实例ID无效: " + instanceId, null);
    }

    /**
     * 业务Key无效
     */
    public static ProcessInstanceQueryException invalidBusinessKey(String businessKey) {
        return new ProcessInstanceQueryException(QUERY_TYPE_BY_BUSINESS_KEY, null,
                "业务Key无效: " + businessKey, null);
    }

    /**
     * 流程Key无效
     */
    public static ProcessInstanceQueryException invalidProcessKey(String processKey) {
        return new ProcessInstanceQueryException(QUERY_TYPE_BY_PROCESS_KEY, null,
                "流程Key无效: " + processKey, null);
    }

    /**
     * 发起人ID无效
     */
    public static ProcessInstanceQueryException invalidInitiatorId(Long initiatorId) {
        return new ProcessInstanceQueryException(QUERY_TYPE_BY_INITIATOR, null,
                "发起人ID无效: " + initiatorId, null);
    }

    /**
     * 时间范围无效
     */
    public static ProcessInstanceQueryException invalidTimeRange(String startTime, String endTime, String reason) {
        Map<String, Object> params = Map.of("startTime", startTime, "endTime", endTime);
        return new ProcessInstanceQueryException(QUERY_TYPE_BY_TIME_RANGE, params,
                "时间范围无效: " + reason, null);
    }

    /**
     * 查询条件冲突
     */
    public static ProcessInstanceQueryException conflictingConditions(String condition1, String condition2, String reason) {
        Map<String, Object> params = Map.of("condition1", condition1, "condition2", condition2);
        return new ProcessInstanceQueryException(QUERY_TYPE_LIST, params,
                "查询条件冲突: " + condition1 + " 与 " + condition2 + " - " + reason, null);
    }

    /**
     * 权限检查失败
     */
    public static ProcessInstanceQueryException permissionDenied(Long userId, String queryType, String reason) {
        return new ProcessInstanceQueryException(queryType, null, userId,
                "查询权限被拒绝: " + reason, null);
    }

    /**
     * 查询超时
     */
    public static ProcessInstanceQueryException queryTimeout(String queryType, long timeoutMs) {
        return new ProcessInstanceQueryException(queryType, null,
                "查询超时: " + timeoutMs + "ms", null);
    }

    /**
     * 查询结果过大
     */
    public static ProcessInstanceQueryException resultTooLarge(String queryType, int resultCount, int maxAllowed) {
        Map<String, Object> params = Map.of("resultCount", resultCount, "maxAllowed", maxAllowed);
        return new ProcessInstanceQueryException(queryType, params,
                "查询结果过大: " + resultCount + " (最大允许: " + maxAllowed + ")", null);
    }

    /**
     * 分页参数无效
     */
    public static ProcessInstanceQueryException invalidPaginationParams(Integer pageNum, Integer pageSize, String reason) {
        Map<String, Object> params = Map.of("pageNum", pageNum, "pageSize", pageSize);
        return new ProcessInstanceQueryException(QUERY_TYPE_LIST, params,
                "分页参数无效: " + reason, null);
    }

    /**
     * 数据库查询错误
     */
    public static ProcessInstanceQueryException databaseError(String queryType, String sql, Throwable cause) {
        return new ProcessInstanceQueryException(queryType, null,
                "数据库查询错误: " + sql, cause);
    }

    /**
     * 查询缓存错误
     */
    public static ProcessInstanceQueryException cacheError(String queryType, String cacheKey, Throwable cause) {
        return new ProcessInstanceQueryException(queryType, null,
                "查询缓存错误: " + cacheKey, cause);
    }

    /**
     * 查询服务不可用
     */
    public static ProcessInstanceQueryException serviceUnavailable(String queryType, String serviceName, Throwable cause) {
        return new ProcessInstanceQueryException(queryType, null,
                "查询服务不可用: " + serviceName, cause);
    }

    /**
     * 查询结果序列化错误
     */
    public static ProcessInstanceQueryException serializationError(String queryType, String reason, Throwable cause) {
        return new ProcessInstanceQueryException(queryType, null,
                "查询结果序列化错误: " + reason, cause);
    }
}
