package net.lab1024.sa.visitor.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 黑名单查询表单
 * <p>
 * 内存优化设计：
 * - 精简字段数量，避免不必要的数据传输
 * - 使用基本数据类型
 * - 合理的验证规则
 * - 避免嵌套对象，保持扁平结构
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "黑名单查询表单")
public class BlacklistQueryForm {

    /**
     * 访客ID
     */
    @Schema(description = "访客ID", example = "1001")
    private Long visitorId;

    /**
     * 访客姓名
     */
    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 黑名单类型
     */
    @Schema(description = "黑名单类型", example = "PERMANENT", allowableValues = {"PERMANENT", "TEMPORARY"})
    private String blacklistType;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;

    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    /**
     * 开始时间（创建时间）
     */
    @Schema(description = "开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime startTime;

    /**
     * 结束时间（创建时间）
     */
    @Schema(description = "结束时间", example = "2025-12-31T23:59:59")
    private LocalDateTime endTime;

    /**
     * 黑名单开始时间
     */
    @Schema(description = "黑名单开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime blacklistStartTime;

    /**
     * 黑名单结束时间
     */
    @Schema(description = "黑名单结束时间", example = "2025-12-31T23:59:59")
    private LocalDateTime blacklistEndTime;

    /**
     * 关键字搜索（访客姓名、手机号、身份证号、原因）
     */
    @Schema(description = "关键字搜索", example = "张三")
    private String keyword;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1", minimum = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20", minimum = "1", maximum = "100")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "createTime", allowableValues = {"createTime", "updateTime", "blacklistId"})
    private String sortBy;

    /**
     * 排序方向
     */
    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection;
}