package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 黑名单VO
 * <p>
 * 内存优化设计：
 * - 精简字段数量，避免不必要的数据传输
 * - 使用基本数据类型
 * - 合理的数据类型选择（LocalDateTime而非Date）
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
@Schema(description = "黑名单VO")
public class BlacklistVO {

    /**
     * 黑名单记录ID
     */
    @Schema(description = "黑名单记录ID", example = "1001")
    private Long blacklistId;

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
     * 身份证号（脱敏显示）
     */
    @Schema(description = "身份证号", example = "110101********1234")
    private String maskedIdCard;

    /**
     * 手机号（脱敏显示）
     */
    @Schema(description = "手机号", example = "138****8000")
    private String maskedPhone;

    /**
     * 黑名单类型
     */
    @Schema(description = "黑名单类型", example = "PERMANENT")
    private String blacklistType;

    /**
     * 黑名单类型名称
     */
    @Schema(description = "黑名单类型名称", example = "永久黑名单")
    private String blacklistTypeName;

    /**
     * 黑名单原因
     */
    @Schema(description = "黑名单原因", example = "违反园区安全管理规定")
    private String blacklistReason;

    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    /**
     * 生效时间
     */
    @Schema(description = "生效时间", example = "2025-01-30T00:00:00")
    private LocalDateTime startTime;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private LocalDateTime endTime;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称", example = "生效")
    private String statusName;

    /**
     * 剩余天数（临时黑名单专用）
     */
    @Schema(description = "剩余天数", example = "30")
    private Integer remainingDays;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30T10:00:00")
    private LocalDateTime createTime;
}