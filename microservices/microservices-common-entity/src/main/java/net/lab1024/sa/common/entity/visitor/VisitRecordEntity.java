package net.lab1024.sa.common.entity.visitor;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 访客访问记录实体类
 * <p>
 * 存储访客的签到签离记录和陪同信息
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * 字段数量: 6个 (符合≤30字段标准)
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_visit_record")
@Schema(description = "访客访问记录实体")
public class VisitRecordEntity extends BaseEntity {

    /**
     * 访问记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "访问记录ID")
    private Long recordId;

    /**
     * 关联的自助登记ID（外键）
     */
    @TableField("registration_id")
    @Schema(description = "自助登记ID")
    private Long registrationId;

    /**
     * 签到时间
     */
    @TableField("check_in_time")
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    /**
     * 签离时间
     */
    @TableField("check_out_time")
    @Schema(description = "签离时间")
    private LocalDateTime checkOutTime;

    /**
     * 是否需要陪同 (0-否 1-是)
     */
    @TableField("escort_required")
    @Schema(description = "是否需要陪同", example = "0")
    private Integer escortRequired;

    /**
     * 陪同人姓名
     */
    @TableField("escort_user")
    @Schema(description = "陪同人姓名")
    private String escortUser;
}
