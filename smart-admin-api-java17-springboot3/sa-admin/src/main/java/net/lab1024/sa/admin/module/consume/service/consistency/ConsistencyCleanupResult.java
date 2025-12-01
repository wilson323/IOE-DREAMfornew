package net.lab1024.sa.admin.module.consume.service.consistency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 一致性清理结果
 * 严格遵循repowiki规范：定义数据一致性清理的标准返回格式
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "一致性清理结果")
public class ConsistencyCleanupResult {

    @Schema(description = "清理任务ID")
    private Long taskId;

    @Schema(description = "清理类型")
    private String cleanupType;

    @Schema(description = "清理状态：PENDING-清理中，SUCCESS-成功，FAILED-失败，PARTIAL-部分成功")
    private String status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "清理耗时（毫秒）")
    private Long duration;

    @Schema(description = "总记录数")
    private Integer totalRecords;

    @Schema(description = "成功清理记录数")
    private Integer successCount;

    @Schema(description = "失败记录数")
    private Integer failureCount;

    @Schema(description = "清理结果消息")
    private String message;

    @Schema(description = "清理详情")
    private List<Map<String, Object>> details;

    @Schema(description = "错误信息列表")
    private List<String> errors;

    /**
     * 创建成功结果
     *
     * @param taskId 任务ID
     * @param cleanupType 清理类型
     * @param totalRecords 总记录数
     * @param successCount 成功数量
     * @return 成功结果
     */
    public static ConsistencyCleanupResult success(Long taskId, String cleanupType,
                                                   Integer totalRecords, Integer successCount) {
        ConsistencyCleanupResult result = new ConsistencyCleanupResult();
        result.taskId = taskId;
        result.cleanupType = cleanupType;
        result.status = "SUCCESS";
        result.startTime = LocalDateTime.now();
        result.endTime = LocalDateTime.now();
        result.totalRecords = totalRecords;
        result.successCount = successCount;
        result.failureCount = totalRecords - successCount;
        result.message = "数据清理完成";
        return result;
    }

    /**
     * 创建失败结果
     *
     * @param taskId 任务ID
     * @param cleanupType 清理类型
     * @param message 错误消息
     * @return 失败结果
     */
    public static ConsistencyCleanupResult failure(Long taskId, String cleanupType, String message) {
        ConsistencyCleanupResult result = new ConsistencyCleanupResult();
        result.taskId = taskId;
        result.cleanupType = cleanupType;
        result.status = "FAILED";
        result.startTime = LocalDateTime.now();
        result.endTime = LocalDateTime.now();
        result.message = message;
        return result;
    }

    /**
     * 创建部分成功结果
     *
     * @param taskId 任务ID
     * @param cleanupType 清理类型
     * @param totalRecords 总记录数
     * @param successCount 成功数量
     * @param errors 错误信息
     * @return 部分成功结果
     */
    public static ConsistencyCleanupResult partial(Long taskId, String cleanupType,
                                                   Integer totalRecords, Integer successCount,
                                                   List<String> errors) {
        ConsistencyCleanupResult result = new ConsistencyCleanupResult();
        result.taskId = taskId;
        result.cleanupType = cleanupType;
        result.status = "PARTIAL";
        result.startTime = LocalDateTime.now();
        result.endTime = LocalDateTime.now();
        result.totalRecords = totalRecords;
        result.successCount = successCount;
        result.failureCount = totalRecords - successCount;
        result.message = "数据清理部分完成";
        result.errors = errors;
        return result;
    }

    // 手动添加setter和getter方法以确保编译通过
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getCleanupType() { return cleanupType; }
    public void setCleanupType(String cleanupType) { this.cleanupType = cleanupType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }

    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

    public Integer getFailureCount() { return failureCount; }
    public void setFailureCount(Integer failureCount) { this.failureCount = failureCount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<Map<String, Object>> getDetails() { return details; }
    public void setDetails(List<Map<String, Object>> details) { this.details = details; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}