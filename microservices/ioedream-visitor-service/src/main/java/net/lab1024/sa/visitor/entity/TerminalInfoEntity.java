package net.lab1024.sa.visitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客终端信息实体类
 * <p>
 * 存储访客卡和自助终端相关信息
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * 字段数量: 5个 (符合≤30字段标准)
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
@TableName("t_visitor_terminal_info")
@Schema(description = "访客终端信息实体")
public class TerminalInfoEntity extends BaseEntity {

    /**
     * 终端信息ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "终端信息ID")
    private Long terminalInfoId;

    /**
     * 关联的自助登记ID（外键）
     */
    @TableField("registration_id")
    @Schema(description = "自助登记ID")
    private Long registrationId;

    /**
     * 终端ID
     */
    @TableField("terminal_id")
    @Schema(description = "终端ID")
    private String terminalId;

    /**
     * 终端位置
     */
    @TableField("terminal_location")
    @Schema(description = "终端位置", example = "A栋大厅")
    private String terminalLocation;

    /**
     * 访客卡号
     */
    @TableField("visitor_card")
    @Schema(description = "访客卡号")
    private String visitorCard;

    /**
     * 访客卡打印状态 (0-未打印 1-打印中 2-已打印 3-打印失败)
     */
    @TableField("card_print_status")
    @Schema(description = "访客卡打印状态", example = "0")
    private Integer cardPrintStatus;
}
