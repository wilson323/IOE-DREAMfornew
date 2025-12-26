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

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费补贴账户实体类
 * <p>
 * 管理用户的多补贴账户体系，支持多种补贴类型
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 餐饮补贴管理
 * - 交通补贴管理
 * - 通讯补贴管理
 * - 补贴余额查询
 * - 补贴扣款优先级
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
@TableName("t_consume_subsidy_account")
@Schema(description = "消费补贴账户实体")
public class ConsumeSubsidyAccountEntity extends BaseEntity {

    /**
     * 补贴账户ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "补贴账户ID")
    private Long subsidyAccountId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 补贴类型ID（关联ConsumeSubsidyTypeEntity）
     */
    @Schema(description = "补贴类型ID")
    private Long subsidyTypeId;

    /**
     * 账户编码
     */
    @Schema(description = "账户编码")
    private String accountCode;

    /**
     * 账户名称
     */
    @Schema(description = "账户名称")
    private String accountName;

    /**
     * 补贴余额
     */
    @Schema(description = "补贴余额")
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    @Schema(description = "冻结金额")
    private BigDecimal frozenAmount;

    /**
     * 初始金额
     */
    @Schema(description = "初始金额")
    private BigDecimal initialAmount;

    /**
     * 累计发放金额
     */
    @Schema(description = "累计发放金额")
    private BigDecimal totalGranted;

    /**
     * 累计使用金额
     */
    @Schema(description = "累计使用金额")
    private BigDecimal totalUsed;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 账户状态（1-正常 2-冻结 3-已过期 4-已清零）
     */
    @Schema(description = "账户状态")
    private Integer accountStatus;

    /**
     * 账户状态描述
     */
    @TableField(exist = false)
    @Schema(description = "账户状态描述")
    private String accountStatusDesc;

    /**
     * 发放批次号
     */
    @Schema(description = "发放批次号")
    private String grantBatchNo;

    /**
     * 发放人ID
     */
    @Schema(description = "发放人ID")
    private Long grantUserId;

    /**
     * 发放人姓名
     */
    @TableField(exist = false)
    @Schema(description = "发放人姓名")
    private String grantUserName;

    /**
     * 发放时间
     */
    @Schema(description = "发放时间")
    private LocalDateTime grantTime;

    /**
     * 清零时间
     */
    @Schema(description = "清零时间")
    private LocalDateTime clearTime;

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
