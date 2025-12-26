package net.lab1024.sa.common.entity.consume;

import java.math.BigDecimal;
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
 * 消费账户类别实体类
 * <p>
 * 管理消费账户的类别配置，支持6种消费模式
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 账户类别配置（员工/访客/临时）
 * - 消费模式设置（固定金额/自由金额/计量计费等）
 * - 折扣规则配置
 * - 消费限额管理
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
@TableName("t_consume_account_kind")
@Schema(description = "消费账户类别实体")
public class ConsumeAccountKindEntity extends BaseEntity {

    /**
     * 类别ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "类别ID")
    private Long kindId;

    /**
     * 类别编码（唯一）
     */
    @Schema(description = "类别编码")
    private String kindCode;

    /**
     * 类别名称
     */
    @Schema(description = "类别名称")
    private String kindName;

    /**
     * 类别类型（1-员工 2-访客 3-临时）
     */
    @Schema(description = "类别类型")
    private Integer kindType;

    /**
     * 消费模式（FIXED_AMOUNT-固定金额 FREE_AMOUNT-自由金额 METERED-计量计费 PRODUCT-商品 ORDER-订餐 INTELLIGENCE-智能）
     */
    @Schema(description = "消费模式")
    private String consumeMode;

    /**
     * 模式配置（JSON格式，存储模式特定的配置参数）
     */
    @TableField(exist = false)
    @Schema(description = "模式配置")
    private String modeConfig;

    /**
     * 折扣类型（0-无折扣 1-固定折扣 2-阶梯折扣）
     */
    @Schema(description = "折扣类型")
    private Integer discountType;

    /**
     * 折扣值
     */
    @Schema(description = "折扣值")
    private BigDecimal discountValue;

    /**
     * 每日最大消费金额
     */
    @Schema(description = "每日最大消费金额")
    private BigDecimal dateMaxMoney;

    /**
     * 每日最大消费次数
     */
    @Schema(description = "每日最大消费次数")
    private Integer dateMaxCount;

    /**
     * 每月最大消费金额
     */
    @Schema(description = "每月最大消费金额")
    private BigDecimal monthMaxMoney;

    /**
     * 每月最大消费次数
     */
    @Schema(description = "每月最大消费次数")
    private Integer monthMaxCount;

    /**
     * 优先级（数字越小优先级越高）
     */
    @Schema(description = "优先级")
    private Integer priority;

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
