package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import net.lab1024.sa.consume.domain.config.ModeConfig;
import net.lab1024.sa.consume.handler.JSONTypeHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POSID账户类别表实体
 *
 * 对应表: POSID_ACCOUNTKIND
 * 表说明: 账户类别配置表，包含mode_config JSON字段存储6种消费模式配置
 *
 * 核心字段:
 * - kind_id: 类别ID（主键）
 * - kind_code: 类别编码（唯一）
 * - mode_config: 各模式参数配置（JSON字段，使用TypeHandler处理）
 * - date_max_money: 每日最大消费金额
 * - date_max_count: 每日最大消费次数
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Data
@TableName(value = "POSID_ACCOUNTKIND", autoResultMap = true)
public class PosidAccountKindEntity {

    /**
     * 类别ID
     */
    @TableId(type = IdType.AUTO)
    private Long kindId;

    /**
     * 类别编码（唯一）
     */
    @TableField("kind_code")
    private String kindCode;

    /**
     * 类别名称
     */
    @TableField("kind_name")
    private String kindName;

    /**
     * 类别类型（1-员工，2-访客，3-临时）
     */
    @TableField("kind_type")
    private Integer kindType;

    /**
     * 各模式参数配置（JSON字段）
     * 使用自定义TypeHandler处理JSON与Java对象转换
     * 包含6种消费模式配置：
     * - FIXED_AMOUNT: 固定金额模式
     * - FREE_AMOUNT: 自由金额模式
     * - METERED: 计量计费模式
     * - PRODUCT: 商品模式
     * - ORDER: 订餐模式
     * - INTELLIGENCE: 智能模式
     */
    @TableField(value = "mode_config", typeHandler = JSONTypeHandler.class)
    private ModeConfig modeConfig;

    /**
     * 折扣类型（0-无折扣，1-固定折扣，2-阶梯折扣）
     */
    @TableField("discount_type")
    private Integer discountType;

    /**
     * 折扣值
     */
    @TableField("discount_value")
    private BigDecimal discountValue;

    /**
     * 每日最大消费金额
     */
    @TableField("date_max_money")
    private BigDecimal dateMaxMoney;

    /**
     * 每日最大消费次数
     */
    @TableField("date_max_count")
    private Integer dateMaxCount;

    /**
     * 每月最大消费金额
     */
    @TableField("month_max_money")
    private BigDecimal monthMaxMoney;

    /**
     * 优先级（数字越小优先级越高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 删除标记
     */
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
