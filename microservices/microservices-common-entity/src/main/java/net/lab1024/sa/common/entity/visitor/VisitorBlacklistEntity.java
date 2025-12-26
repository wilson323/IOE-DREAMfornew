package net.lab1024.sa.common.entity.visitor;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 访客黑名单实体
 * <p>
 * 内存优化设计：
 * - 紧凑字段设计，避免冗余
 * - 合理的VARCHAR长度设置
 * - 使用枚举或常量替代重复字符串
 * - 索引优化，提升查询性能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_visitor_blacklist")
@Schema(description = "访客黑名单实体")
public class VisitorBlacklistEntity {

    /**
     * 黑名单记录ID（主键）
     */
    @TableId(value = "blacklist_id", type = IdType.ASSIGN_ID)
    @Schema(description = "黑名单记录ID", example = "1001")
    private Long blacklistId;

    /**
     * 访客ID
     */
    @NotNull(message = "访客ID不能为空")
    @TableField("visitor_id")
    @Schema(description = "访客ID", example = "1001")
    private Long visitorId;

    /**
     * 访客姓名（冗余存储，减少关联查询）
     */
    @NotBlank(message = "访客姓名不能为空")
    @Size(max = 50, message = "访客姓名长度不能超过50个字符")
    @TableField("visitor_name")
    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Size(max = 18, message = "身份证号长度不能超过18个字符")
    @TableField("id_card")
    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    /**
     * 手机号
     */
    @Size(max = 11, message = "手机号长度不能超过11个字符")
    @TableField("phone")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 黑名单类型
     * <p>
     * PERMANENT-永久黑名单
     * TEMPORARY-临时黑名单
     * WARNING-警告名单
     * </p>
     */
    @NotBlank(message = "黑名单类型不能为空")
    @Size(max = 20, message = "黑名单类型长度不能超过20个字符")
    @TableField("blacklist_type")
    @Schema(description = "黑名单类型", example = "PERMANENT")
    private String blacklistType;

    /**
     * 黑名单原因
     */
    @NotBlank(message = "黑名单原因不能为空")
    @Size(max = 200, message = "黑名单原因长度不能超过200个字符")
    @TableField("blacklist_reason")
    @Schema(description = "黑名单原因", example = "违反园区安全管理规定")
    private String blacklistReason;

    /**
     * 操作人ID
     */
    @NotNull(message = "操作人ID不能为空")
    @TableField("operator_id")
    @Schema(description = "操作人ID", example = "2001")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @NotBlank(message = "操作人姓名不能为空")
    @Size(max = 50, message = "操作人姓名长度不能超过50个字符")
    @TableField("operator_name")
    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    /**
     * 生效时间
     */
    @NotNull(message = "生效时间不能为空")
    @TableField("start_time")
    @Schema(description = "生效时间", example = "2025-01-30T00:00:00")
    private LocalDateTime startTime;

    /**
     * 失效时间（临时黑名单专用）
     */
    @TableField("end_time")
    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private LocalDateTime endTime;

    /**
     * 状态
     * <p>
     * 1-生效
     * 0-失效
     * </p>
     */
    @NotNull(message = "状态不能为空")
    @TableField("status")
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "2025-01-30T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2025-01-30T10:00:00")
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;
}
