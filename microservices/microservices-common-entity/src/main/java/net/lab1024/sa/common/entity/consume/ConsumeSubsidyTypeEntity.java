package net.lab1024.sa.common.entity.consume;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费补贴类型实体类
 * <p>
 * 管理补贴类型配置，支持多种补贴类型
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 餐饮补贴类型
 * - 交通补贴类型
 * - 通讯补贴类型
 * - 补贴优先级配置
 * - 补贴使用规则
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_consume_subsidy_type")
@Schema(description = "消费补贴类型实体")
public class ConsumeSubsidyTypeEntity extends BaseEntity {

    /**
     * 补贴类型ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "补贴类型ID")
    private Long subsidyTypeId;

    /**
     * 类型编码（唯一）
     * 示例：MEAL-餐补，TRAFFIC-交通补，COMMUNICATION-通讯补
     */
    @Schema(description = "类型编码")
    private String typeCode;

    /**
     * 类型名称
     */
    @Schema(description = "类型名称")
    private String typeName;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 优先级（数字越小优先级越高）
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 是否可累计（0-不可累计 1-可累计）
     */
    @Schema(description = "是否可累计")
    private Integer accumulative;

    /**
     * 是否可转让（0-不可转让 1-可转让）
     */
    @Schema(description = "是否可转让")
    private Integer transferable;

    /**
     * 是否启用（0-禁用 1-启用）
     */
    @Schema(description = "是否启用")
    private Integer enabled;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sortOrder;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
