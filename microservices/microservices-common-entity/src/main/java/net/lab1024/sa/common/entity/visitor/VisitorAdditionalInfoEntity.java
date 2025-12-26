package net.lab1024.sa.common.entity.visitor;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客附加信息实体类
 * <p>
 * 存储访客的携带物品、车牌号等附加信息
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * 字段数量: 4个 (符合≤30字段标准)
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
@TableName("t_visitor_additional_info")
@Schema(description = "访客附加信息实体")
public class VisitorAdditionalInfoEntity extends BaseEntity {

    /**
     * 附加信息ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "附加信息ID")
    private Long additionalInfoId;

    /**
     * 关联的自助登记ID（外键）
     */
    @TableField("registration_id")
    @Schema(description = "自助登记ID")
    private Long registrationId;

    /**
     * 携带物品（JSON格式）
     */
    @TableField("belongings")
    @Schema(description = "携带物品")
    private String belongings;

    /**
     * 车牌号
     */
    @TableField("license_plate")
    @Schema(description = "车牌号", example = "京A12345")
    private String licensePlate;
}
