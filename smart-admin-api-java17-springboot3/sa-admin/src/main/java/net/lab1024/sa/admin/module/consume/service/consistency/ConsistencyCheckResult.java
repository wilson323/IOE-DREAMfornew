package net.lab1024.sa.admin.module.consume.service.consistency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 一致性检查结果
 * 严格遵循repowiki规范：定义数据一致性检查的标准返回格式
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "一致性检查结果")
public class ConsistencyCheckResult {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "检查类型")
    private String checkType;

    @Schema(description = "是否一致")
    private Boolean isConsistent;

    @Schema(description = "检查状态：PENDING-检查中，SUCCESS-成功，FAILED-失败")
    private String status;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

    @Schema(description = "检查耗时（毫秒）")
    private Long duration;

    @Schema(description = "检查结果消息")
    private String message;

    @Schema(description = "差异详情")
    private List<Map<String, Object>> differences;

    @Schema(description = "检查详情")
    private Map<String, Object> details;

    /**
     * 创建成功结果
     *
     * @param recordId 记录ID
     * @param checkType 检查类型
     * @return 成功结果
     */
    public static ConsistencyCheckResult success(Long recordId, String checkType) {
        ConsistencyCheckResult result = new ConsistencyCheckResult();
        result.setRecordId(recordId);
        result.setCheckType(checkType);
        result.setIsConsistent(true);
        result.setStatus("SUCCESS");
        result.setCheckTime(LocalDateTime.now());
        result.setMessage("数据一致性检查通过");
        return result;
    }

    /**
     * 创建失败结果
     *
     * @param recordId 记录ID
     * @param checkType 检查类型
     * @param message 错误消息
     * @return 失败结果
     */
    public static ConsistencyCheckResult failure(Long recordId, String checkType, String message) {
        ConsistencyCheckResult result = new ConsistencyCheckResult();
        result.setRecordId(recordId);
        result.setCheckType(checkType);
        result.setIsConsistent(false);
        result.setStatus("FAILED");
        result.setCheckTime(LocalDateTime.now());
        result.setMessage(message);
        return result;
    }

    /**
     * 创建进行中结果
     *
     * @param recordId 记录ID
     * @param checkType 检查类型
     * @return 进行中结果
     */
    public static ConsistencyCheckResult pending(Long recordId, String checkType) {
        ConsistencyCheckResult result = new ConsistencyCheckResult();
        result.setRecordId(recordId);
        result.setCheckType(checkType);
        result.setStatus("PENDING");
        result.setCheckTime(LocalDateTime.now());
        result.setMessage("数据一致性检查进行中");
        return result;
    }
}