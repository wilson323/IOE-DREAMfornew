package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * 订单项实体
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ordering_item")
public class OrderingItemEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long orderingItemId;

    /**
     * 订单ID
     */
    private Long orderingId;

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单图片
     */
    private String menuImage;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 小计金额
     */
    private BigDecimal subtotal;

    /**
     * 备注
     */
    private String remark;

    /**
     * 制作状态：PENDING-待制作，MAKING-制作中，COMPLETED-已完成
     */
    private String makingStatus;
}